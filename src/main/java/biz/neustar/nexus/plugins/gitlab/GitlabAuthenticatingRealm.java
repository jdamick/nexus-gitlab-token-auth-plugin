/*
 * Copyright (c) 2013 Neustar, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package biz.neustar.nexus.plugins.gitlab;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.usermanagement.User;

import biz.neustar.nexus.plugins.gitlab.client.GitlabDao;
import biz.neustar.nexus.plugins.gitlab.client.rest.GitlabUser;

@Component(role = Realm.class, hint = GitlabAuthenticatingRealm.ROLE, description = "Gitlab Token Authentication Realm")
public class GitlabAuthenticatingRealm extends AuthorizingRealm implements Initializable, Disposable {

    private static final String GITLAB_MSG = "[Gitlab] ";
	public static final String ROLE = "NexusGitlabAuthenticationRealm";
	private static final String DEFAULT_MESSAGE = "Could not retrieve info from Gitlab.";
	private static AtomicBoolean active = new AtomicBoolean(false);

	@Requirement
	private GitlabDao gitlab;

	private final static Logger LOGGER = LoggerFactory.getLogger(GitlabAuthenticatingRealm.class);

	// testing only.
	static boolean isActive() {
		return active.get();
	}

	@Override
    public void dispose() {
		active.set(false);
		LOGGER.info(GITLAB_MSG + "Realm deactivated.");
	}

	@Override
	public String getName() {
		return GitlabAuthenticatingRealm.class.getName();
	}

	@Override
    public void initialize() throws InitializationException {
	    LOGGER.info(GITLAB_MSG + "Realm activated.");
		active.set(true);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
			throws AuthenticationException {

		if (!(authenticationToken instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException("Token of type " + authenticationToken.getClass().getName()
					+ " is not supported.  A " + UsernamePasswordToken.class.getName() + " is required.");
		}
		UsernamePasswordToken userPass = (UsernamePasswordToken) authenticationToken;
		String token = new String(userPass.getPassword());
		if (token.isEmpty()) {
		    LOGGER.debug(GITLAB_MSG + "token for {} is empty", userPass.getUsername());
		    return null;
		}

		try {
		    LOGGER.debug(GITLAB_MSG + "authenticating {}", userPass.getUsername());
		    GitlabUser gitlabUser = gitlab.getRestClient().getUser(userPass.getUsername(), token);
		    User user = gitlabUser.toUser();
		    if (user.getUserId() == null || user.getUserId().isEmpty()) {
		        throw new AuthenticationException(DEFAULT_MESSAGE + " for " + userPass.getUsername());
		    }
		    return new SimpleAuthenticationInfo(gitlabUser /*userPass.getPrincipal()*/,
		            userPass.getCredentials(), getName());
		} catch (Exception e) {
		    throw new AuthenticationException(DEFAULT_MESSAGE, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		// only authorize users from this realm
	    if (principals.getRealmNames().contains(this.getName())) {
	        //String username = (String) principals.getPrimaryPrincipal();
	        GitlabUser user = (GitlabUser) principals.getPrimaryPrincipal();
	        LOGGER.debug(GITLAB_MSG + "authorizing {}", user.getUsername());
            Set<String> groups = gitlab.getGitlabPluginConfiguration().getDefaultRoles();
            if (user.isActive()) {
                groups.addAll(gitlab.getGitlabPluginConfiguration().getAdminRoles());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(GITLAB_MSG + "User: " + user.getUsername() + " gitlab authorization to groups: " +
                    StringUtils.join(groups.iterator(), ", "));
            }
	        return new SimpleAuthorizationInfo(groups);
	    }
	    return null;
	}
}
