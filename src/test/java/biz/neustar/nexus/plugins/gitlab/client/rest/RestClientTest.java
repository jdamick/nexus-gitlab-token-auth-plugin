/**
 * Copyright 2000-2013 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.nexus.plugins.gitlab.client.rest;

import biz.neustar.nexus.plugins.gitlab.config.v1_0_0.Configuration;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.sonatype.security.usermanagement.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    static final String user = "{\"name\":\"Damick, Jeffrey\",\"username\":\"jeffrey.damick\"," +
            "\"id\":2,\"state\":\"active\",\"avatar_url\":\"\",\"created_at\":\"2012-06-13T21:13:47.000Z\"," +
            "\"is_admin\":true,\"bio\":\"\",\"skype\":\"\",\"linkedin\":\"\",\"twitter\":\"\",\"website_url\":\"\"," +
            "\"email\":\"jeffrey.damick@neustar.biz\",\"theme_id\":5,\"color_scheme_id\":1,\"projects_limit\":200," +
            "\"identities\":[{\"provider\":\"ldap\",\"extern_uid\":\"uid=jdamick,ou=Neustar,ou=Staff,o=Neustar\"}]," +
            "\"can_create_group\":true,\"can_create_project\":true,\"private_token\":\"xCasdfa32342FDvdfgF\"}";

    @Test
    public void testClient() throws Exception {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody(user));
        server.play();

        boolean usingMockServer = true;
        Configuration conf = new Configuration();
        String token = System.getProperty("TOKEN");
        if (token != null) {
            conf.setGitlabServerUrl(GITLAB);
            usingMockServer = false;
        } else {
            token = "1234alskfadlskfj";
            conf.setGitlabServerUrl("http://" + server.getHostName() + ":"+server.getPort());
        }
        RestClient client = new RestClient(conf);
        GitlabUser user = client.getUser("jeffrey.damick@neustar.biz", token);
        assertNotNull(user);
        assertNotNull(user.toUser().getUserId());
        assertEquals("jeffrey.damick", user.toUser().getUserId());

        if (usingMockServer) {
            RecordedRequest request = server.takeRequest(); // 1 request recorded
            assertEquals("/api/v3/user?private_token=" + token, request.getPath());
        }
    }
}
