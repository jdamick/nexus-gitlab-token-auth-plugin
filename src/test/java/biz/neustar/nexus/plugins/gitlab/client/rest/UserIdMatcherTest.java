/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserIdMatcherTest {

    @Test
    public void testDefaultMatch() {
        Configuration config = new Configuration();
        config.setUserIdField("id");
        UserIdMatcher matcher = new UserIdMatcher(config);

        Map<String, String> gitlab = new HashMap<>();
        gitlab.put("provider", "gitlab");
        gitlab.put("id", "bob");

        List<Map<String, String>> identities = new ArrayList<>();
        identities.add(gitlab);

        assertTrue(matcher.matches(identities, "bob"));
        assertFalse(matcher.matches(identities, "jim"));
    }

    @Test
    public void testDefaultFieldMatch() {
        Configuration config = new Configuration();
        UserIdMatcher matcher = new UserIdMatcher(config);

        Map<String, String> gitlab = new HashMap<>();
        gitlab.put("provider", "gitlab");
        gitlab.put("email", "bob@bobsbugsbegone.com");

        List<Map<String, String>> identities = new ArrayList<>();
        identities.add(gitlab);

        assertTrue(matcher.matches(identities, "bob@bobsbugsbegone.com"));
        assertFalse(matcher.matches(identities, "bob"));
    }


    @Test
    public void testDefaultMatchWithNulls() {
        Configuration config = new Configuration();
        config.setUserIdField("user");
        UserIdMatcher matcher = new UserIdMatcher(config);

        Map<String, String> gitlab = new HashMap<>();
        gitlab.put("provider", "gitlab");
        gitlab.put("id", "bob");

        List<Map<String, String>> identities = new ArrayList<>();
        identities.add(gitlab);

        assertFalse(matcher.matches(identities, "bob"));
        assertFalse(matcher.matches(identities, "jim"));
    }

    @Test
    public void testSubMatch() {
        Configuration config = new Configuration();
        config.setUserIdField("external_uid");
        config.setUserIdFieldMatch("uid=(\\w*).*");
        config.setUserIdProvider("ldap");
        UserIdMatcher matcher = new UserIdMatcher(config);

        Map<String, String> ldap = new HashMap<>();
        ldap.put("provider", "ldap");
        ldap.put("external_uid", "uid=jdamick,ou=Neustar,ou=Staff,o=Neustar");

        List<Map<String, String>> identities = new ArrayList<>();
        identities.add(ldap);

        assertTrue(matcher.matches(identities, "jdamick"));
        assertFalse(matcher.matches(identities, "jim"));
    }
}
