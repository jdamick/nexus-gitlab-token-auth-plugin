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

import java.net.URISyntaxException;
import java.rmi.RemoteException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.LfuPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

/**
 *
 */
public class CachingRestClient extends RestClient {
	private static final String REST_RESPONSE_CACHE = CachingRestClient.class.getName();
	private static final Logger LOG = LoggerFactory.getLogger(CachingRestClient.class);

	private final CacheManager ehCacheManager;

	public CachingRestClient(Configuration config) throws URISyntaxException {
		super(config);

		ehCacheManager = CacheManager.getInstance();
		long ttl = config.getCacheValidationInterval() * 60; // minutes to seconds
		Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, ttl, ttl);
		cache.setMemoryStoreEvictionPolicy(new LfuPolicy());
		ehCacheManager.addCache(cache);
	}

	@Override
	public GitlabUser getUser(String userId, String token) throws RemoteException {
		Cache cache = cache();
		final String key = String.format("getUser:%s:%s", userId, token);
		Element elem = cache.get(key);
		if (elem != null) {
			LOG.debug("Cache Hit: getUser({}) from cache", userId);
			return (GitlabUser) elem.getObjectValue();
		}

		GitlabUser user = super.getUser(userId, token);
		cache.put(new Element(key, user));
		return user;
	}

	protected Cache cache() {
		return ehCacheManager.getCache(REST_RESPONSE_CACHE);
	}
}
