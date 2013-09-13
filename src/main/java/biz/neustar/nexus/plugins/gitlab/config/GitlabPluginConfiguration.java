/**
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
package biz.neustar.nexus.plugins.gitlab.config;

import org.sonatype.sisu.goodies.eventbus.EventBus;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

/**
 * Gitlab Plugin Configuration
 */
public interface GitlabPluginConfiguration extends EventBus {

    Configuration getConfiguration();

}
