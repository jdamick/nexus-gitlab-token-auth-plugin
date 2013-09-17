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
	private final boolean cacheEnabled;

	public CachingRestClient(Configuration config) throws URISyntaxException {
		super(config);

		ehCacheManager = CacheManager.getInstance();
		this.cacheEnabled = config.getCacheValidationInterval() > 0;
		long ttl = config.getCacheValidationInterval() * 60; // minutes to seconds
		Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, ttl, ttl);
		ehCacheManager.addCache(cache);
		cache.setMemoryStoreEvictionPolicy(new LfuPolicy());
	}

	@Override
	public GitlabUser getUser(String userId, String token) throws RemoteException {
		final String key = String.format("getUser:%s:%s", userId, token);
		GitlabUser user = getCacheEntry(key);
		if (user != null) {
			LOG.debug("Cache Hit: getUser({}) from cache", userId);
			return user;
		}

		user = super.getUser(userId, token);
		store(key, user);
		return user;
	}

	@SuppressWarnings("unchecked")
    protected <T> T getCacheEntry(String key) {
	    Element elem = cache().get(key);
	    T result = null;
	    if (cacheEnabled && elem != null) {
	        result = (T) elem.getObjectValue();
	    }
	    return result;
	}

	protected void store(String key, Object obj) {
	    if (cacheEnabled) {
	        cache().put(new Element(key, obj));
	    }
	}

	protected Cache cache() {
		return ehCacheManager.getCache(REST_RESPONSE_CACHE);
	}
}
