/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

public class UserIdMatcherTest {

    @Test
    public void testDefaultMatch() {
        Configuration config = new Configuration();
        config.setUserIdField("id");
        UserIdMatcher matcher = new UserIdMatcher(config);
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("id", "bob");

        assertTrue(matcher.matches(userInfo, "bob"));
        assertFalse(matcher.matches(userInfo, "jim"));
    }

    @Test
    public void testDefaultFieldMatch() {
        Configuration config = new Configuration();
        UserIdMatcher matcher = new UserIdMatcher(config);
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("email", "bob@bobsbugsbegone.com");

        assertTrue(matcher.matches(userInfo, "bob@bobsbugsbegone.com"));
        assertFalse(matcher.matches(userInfo, "bob"));
    }


    @Test
    public void testDefaultMatchWithNulls() {
        Configuration config = new Configuration();
        config.setUserIdField("user");
        UserIdMatcher matcher = new UserIdMatcher(config);
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("id", "bob");

        assertFalse(matcher.matches(userInfo, "bob"));
        assertFalse(matcher.matches(userInfo, "jim"));
    }

    @Test
    public void testSubMatch() {
        Configuration config = new Configuration();
        config.setUserIdField("external_uid");
        config.setUserIdFieldMatch("uid=(\\w*).*");
        UserIdMatcher matcher = new UserIdMatcher(config);
        Map<String, String> userInfo = new HashMap<String, String>();
        userInfo.put("external_uid", "uid=jdamick,ou=Neustar,ou=Staff,o=Neustar");

        assertTrue(matcher.matches(userInfo, "jdamick"));
        assertFalse(matcher.matches(userInfo, "jim"));
    }
}
