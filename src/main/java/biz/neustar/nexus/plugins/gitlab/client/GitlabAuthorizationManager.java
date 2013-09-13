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
package biz.neustar.nexus.plugins.gitlab.client;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.Collections;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

/**
 *
 */
@Component(role = AuthorizationManager.class, hint = "Gitlab")
public class GitlabAuthorizationManager extends AbstractReadOnlyAuthorizationManager {

    @Requirement
    private CrowdClientHolder crowdClientHolder;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public GitlabAuthorizationManager() {
        logger.info("GitlabAuthorizationManager is starting...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Privilege getPrivilege(String privilegeId) throws NoSuchPrivilegeException {
        throw new NoSuchPrivilegeException("Gitlab plugin doesn't support privileges");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role getRole(String roleId) throws NoSuchRoleException {
        if (crowdClientHolder.isConfigured()) {
            try {
                Role role = crowdClientHolder.getRestClient().getGroup(roleId);
                role.setSource(getSource());
                return role;
            } catch (RemoteException e) {
                throw new NoSuchRoleException("Failed to get role " + roleId + " from Crowd.", e);
            }
        } else {
            throw new NoSuchRoleException("Crowd plugin is not configured.");
        }
    }

    @Override
    public String getSource() {
        return GitlabUserManager.SOURCE;
    }

    @Override
    public Set<Privilege> listPrivileges() {
        return Collections.emptySet();
    }

    @Override
    public Set<Role> listRoles() {
        if (crowdClientHolder.isConfigured()) {
            try {
            	Set<Role> roles = crowdClientHolder.getRestClient().getAllGroups();
            	for (Role role : roles) {
            		role.setSource(getSource());
            	}
                return roles;
            } catch (RemoteException e) {
                logger.error("Unable to load roles", e);
                return null;
            }
        }
        //UnconfiguredNotifier.unconfigured();
        return Collections.emptySet();
    }

}
