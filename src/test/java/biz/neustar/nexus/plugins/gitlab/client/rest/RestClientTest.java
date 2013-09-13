/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.sonatype.security.usermanagement.User;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;

public class RestClientTest {
    private static final String GITLAB = "https://git.nexgen.neustar.biz";

    @Test
    public void convertUserName() throws Exception {
        // with spaces
        User user = new GitlabUser("Jeff Damick").toUser();
        assertEquals("Jeff", user.getFirstName());
        assertEquals("Damick", user.getLastName());

        user = new GitlabUser("Damick, Jeff").toUser();
        assertEquals("Jeff", user.getFirstName());
        assertEquals("Damick", user.getLastName());

        user = new GitlabUser("Jeff Van Damick").toUser();
        assertEquals("Jeff", user.getFirstName());
        assertEquals("Van Damick", user.getLastName());


        user = new GitlabUser("Damick").toUser();
        assertEquals("Damick", user.getFirstName());
        assertEquals("", user.getLastName());

        user = new GitlabUser("").toUser();
        assertEquals("", user.getFirstName());
        assertEquals("", user.getLastName());
    }

    @Test
    public void testClient() throws Exception {
        Configuration conf = new Configuration();
        conf.setGitlabServerUrl(GITLAB);
        RestClient client = new RestClient(conf);
        GitlabUser user = client.getUser("jdamick", "rEyQEHRhTNqBAALWubqZ");
        assertNotNull(user);
        assertNotNull(user.toUser().getUserId());
    }
}
