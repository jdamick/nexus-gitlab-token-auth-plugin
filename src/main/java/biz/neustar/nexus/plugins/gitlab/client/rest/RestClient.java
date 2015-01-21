/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package biz.neustar.nexus.plugins.gitlab.client.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.nexus.plugins.gitlab.GitlabAuthenticatingRealm;
import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.ApacheHttpClientState;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

/**
 *
 */
public class RestClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);
	private static final String GITLAB_API_PATH = "/api/v3/";

	private final ObjectMapper objMapper = new ObjectMapper();
	private final Client client;
	private final URI serverURL;
	private final UserIdMatcher userIdMatcher;

	public RestClient(Configuration config) throws URISyntaxException {
	    this.userIdMatcher = new UserIdMatcher(config);
		DefaultApacheHttpClientConfig clientConfig = new DefaultApacheHttpClientConfig();
		clientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, Boolean.TRUE);
		clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, new Integer(config.getHttpTimeout()));
		clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, new Integer(config.getHttpTimeout()));
		clientConfig.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE, new Integer(config.getHttpMaxConnections()));

		// /api/v3/user?private_token=<fill in>
		serverURL = new URI(config.getGitlabServerUrl()).resolve(GITLAB_API_PATH);

		ApacheHttpClientState httpState = new ApacheHttpClientState();
		httpState.clearCredentials();
		if (StringUtils.isNotBlank(config.getHttpProxyHost()) && config.getHttpProxyPort() > 0) {
			clientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_PROXY_URI,
			        config.getHttpProxyHost() + ':' + config.getHttpProxyPort());

			if (config.getHttpProxyUsername() != null && config.getHttpProxyPassword() != null) {
				httpState.setProxyCredentials(null, config.getHttpProxyHost(), config.getHttpProxyPort(),
				        config.getHttpProxyUsername(), config.getHttpProxyPassword());
			}
		}

		clientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_HTTP_STATE, httpState);
        clientConfig.getClasses().add(JacksonJsonProvider.class);

		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("Gitlab HTTP Client config");
		    LOGGER.debug(config.getGitlabServerUrl());
		    LOGGER.debug(serverURL.toString());
		    LOGGER.debug("PROPERTY_THREADPOOL_SIZE: {}", clientConfig.getProperty(ClientConfig.PROPERTY_THREADPOOL_SIZE));
		    LOGGER.debug("PROPERTY_READ_TIMEOUT: {}", clientConfig.getProperty(ClientConfig.PROPERTY_READ_TIMEOUT));
		    LOGGER.debug("PROPERTY_CONNECT_TIMEOUT: {}", clientConfig.getProperty(ClientConfig.PROPERTY_CONNECT_TIMEOUT));
		    LOGGER.debug("PROPERTY_PROXY_URI: {}", clientConfig.getProperty(ApacheHttpClientConfig.PROPERTY_PROXY_URI));
		}

		client = ApacheHttpClient.create(clientConfig);
	}


	/**
	 * @param userid
	 * @param token
	 * @return a <code>org.sonatype.security.usermanagement.User</code> from Gitlab by a userid
	 * @throws RemoteException
	 */
	public GitlabUser getUser(String userId, String token) throws RemoteException {
	    LOGGER.debug("getUser({}, xxxx)", String.valueOf(userId));

		WebResource r = client.resource(serverURL.resolve("user?private_token=" + token));
		try {
			GitlabUser response = r.get(GitlabUser.class);
		    if (userIdMatcher.matches(response.getIdentities(), userId)) {
		        LOGGER.debug(GitlabAuthenticatingRealm.GITLAB_MSG + response.toString());
		        return response;
		    } else {
		        throw new RemoteException("User Id (" + userId + ") doesn't match");
		    }
		} catch (UniformInterfaceException uie) {
			throw handleError(uie);
		}
	}

	private RemoteException handleError(UniformInterfaceException uie) {
		ClientResponse response = uie.getResponse();
		String error = response.getEntity(String.class);
		if (StringUtils.isNotBlank(error)) {
			LOGGER.error(GitlabAuthenticatingRealm.GITLAB_MSG + "Error: {}", error);
		}
		return new RemoteException("Error in a Gitlab REST call", uie);
	}
}
