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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
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
		// create a cache with max items = 10000 and TTL (live and idle) = 1 hour
		long ttl = config.getCacheValidationInterval() * 60; // minutes to seconds
		Cache cache = new Cache(REST_RESPONSE_CACHE, 10000, false, false, ttl, ttl);
		ehCacheManager.addCache(cache);
	}


/*
	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getNestedGroups(String username) throws RemoteException {
		Cache cache = getCache();
		Element elem = cache.get("nestedgroups" + username);
		if (elem != null) {
			if (LOG.isDebugEnabled()) LOG.debug("getNestedGroups(" + username + ") from cache");
			return (Set<String>) elem.getObjectValue();
		}

		Set<String> groups = super.getNestedGroups(username);
		cache.put(new Element("nestedgroups" + username, groups));
		return groups;
	}

	@Override
	public User getUser(String userid) throws RemoteException {
		Cache cache = getCache();
		Element elem = cache.get("user" + userid);
		if (elem != null) {
			if (LOG.isDebugEnabled()) LOG.debug("getUser(" + userid + ") from cache");
			return (User) elem.getObjectValue();
		}

		User user = super.getUser(userid);
		cache.put(new Element("user" + userid, user));
		return user;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Role> getAllGroups() throws RemoteException {
		Cache cache = getCache();
		Element elem = cache.get("allgroups");
		if (elem != null) {
			if (LOG.isDebugEnabled()) LOG.debug("getAllGroups from cache");
			return (Set<Role>) elem.getObjectValue();
		}

		Set<Role> groups = super.getAllGroups();
		cache.put(new Element("allgroups", groups));
		return groups;
	}
*/
	protected Cache getCache() {
		return ehCacheManager.getCache(REST_RESPONSE_CACHE);
	}
}
