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

import java.net.URISyntaxException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import biz.neustar.nexus.plugins.gitlab.client.rest.CachingRestClient;
import biz.neustar.nexus.plugins.gitlab.client.rest.RestClient;
import biz.neustar.nexus.plugins.gitlab.config.GitlabPluginConfiguration;
import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

/**
 *
 */
@Component(role = GitlabDao.class, hint = "default")
public class DefaultGitlabDao extends AbstractLogEnabled implements GitlabDao, Initializable {

    private boolean configured = false;
    private Configuration configuration;
    private RestClient restClient;

    @Requirement
    private GitlabPluginConfiguration gitlabPluginConfiguration;

    @Override
    public void initialize() throws InitializationException {
        configuration = gitlabPluginConfiguration.getConfiguration();
        if (configuration != null) {
			try {
				restClient = new CachingRestClient(configuration);
			} catch (URISyntaxException use) {
				throw new InitializationException("Rest client init failed", use);
			}
            configured = true;
        }
    }


    @Override
    public RestClient getRestClient() {
    	return restClient;
    }
}
