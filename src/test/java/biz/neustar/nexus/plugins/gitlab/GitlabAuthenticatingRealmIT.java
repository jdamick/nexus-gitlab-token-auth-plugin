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
package biz.neustar.nexus.plugins.gitlab;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.sisu.filetasks.FileTaskBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;
import static org.sonatype.sisu.goodies.common.Varargs.$;

/**
 *
 * see: https://github.com/sonatype/nexus-testsuite-guide/blob/master/guide/src/
 * test/java/org/sonatype/nexus/testsuite/guide/nrpits/README.md
 */
@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class GitlabAuthenticatingRealmIT extends NexusRunningParametrizedITSupport {

    @Inject
    private FileTaskBuilder overlays;

    private MockWebServer server = new MockWebServer();


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return firstAvailableTestParameters(
                systemTestParameters(),
                testParameters(
                        $("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle")
                )
        ).load();
    }
    /*
    @Parameterized.Parameters
    public static Collection<Object[]> hardcodedParameters() {
        return ParametersLoaders.firstAvailableTestParameters(ParametersLoaders.systemTestParameters(),
                ParametersLoaders.defaultTestParameters(),
                ParametersLoaders.testParameters($("org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.3.0"))).load();
    }
    */

    protected Model getPomInfo() {
        File pomFile = new File("pom.xml");
        Model model = null;
        try {
            model = (new MavenXpp3Reader()).read(new FileReader(pomFile));
            model.setPomFile(pomFile);
        } catch (Exception ex) {
            // nothing
        }
        return model;
    }

    protected byte[] loadBody(String fileName) {
        RandomAccessFile bodyFile = null;
        try {
            bodyFile = new RandomAccessFile(testData().resolveFile(fileName), "r");
            byte[] body = new byte[(int) bodyFile.length()];
            bodyFile.readFully(body);
            return body;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (bodyFile != null) {
                try {
                    bodyFile.close();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }
    }

    // Based on docs at: https://github.com/sonatype/nexus-testsuite-guide

    @Override
    protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
        try {
            // request 1 - good
            server.enqueue(new MockResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .setBody(loadBody("good_user.json")));
            // request 2 - bad, no user found
            server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(404).setStatus("Not Found"));
            // request 3 - bad, auth failed against gitlab
            server.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(401).setStatus("Unauthorized"));
            server.play();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        System.out.println("MOCK SERVER: " + server.getHostName() + " " + server.getPort());

        // override the format of the nexus.log file
        configuration.setLogPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");

        // configure logging level of example plugins running in nexus
        configuration.setLogLevel("DEBUG");
        configuration.setStartTimeout(120);

        Model model = getPomInfo();
        String pluginName = model.getArtifactId() + "-" + model.getVersion();
        File plugin = new File("target/" + pluginName + "-bundle.zip");
        assertTrue(plugin.exists());

        configuration.addPlugins(plugin);

        // why does this work in the examples.. pom is missing for me..
        /*
        configuration.addPlugins(
                artifactResolver()
                .resolveFromDependencyManagement("biz.neustar.nexus", "nexus-gitlab-token-auth-plugin", "nexus-plugin", (String)null, "zip", "bundle")
                        //.resolvePluginFromDependencyManagement("biz.neustar.nexus", "nexus-gitlab-token-auth-plugin")
        );
        */


        // from the mock server
        RandomAccessFile tempConfig = null;
        RandomAccessFile gitlabConfig = null;
        try {
            File tempPluginConfig = testData().resolveFile("gitlab-plugin-temp.xml");
            tempConfig = new RandomAccessFile(tempPluginConfig, "r");
            gitlabConfig = new RandomAccessFile(tempPluginConfig.getParent() + "/gitlab-plugin.xml", "rw");
            gitlabConfig.setLength(0); // truncate.
            String line = tempConfig.readLine();
            while (line != null) {
                // GITLAB_URL
                gitlabConfig.write(line
                        .replace("GITLAB_URL", "http://" + server.getHostName() + ":" + server.getPort()).getBytes(
                                "UTF-8"));
                gitlabConfig.write('\n');
                line = tempConfig.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (tempConfig != null) {
                    tempConfig.close();
                }
            } catch (Exception e) {
                // ignore
            }
            try {
                if (gitlabConfig != null) {
                    gitlabConfig.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // <realm>NexusGitlabAuthenticationRealm</realm>
        configuration.addOverlays(
                overlays.copy().file(file(testData().resolveFile("gitlab-plugin.xml"))).to()
                        .directory(path("sonatype-work/nexus/conf")),
                overlays.copy().file(file(testData().resolveFile("security-configuration.xml"))).to()
                        .directory(path("sonatype-work/nexus/conf")));

        return configuration;
    }

    public GitlabAuthenticatingRealmIT(String nexusBundleCoordinates) {
        super(nexusBundleCoordinates);
    }


    /*
     *
     */

    @Test
    public void testPlugin() throws Exception {
        assertTrue(nexus().isRunning());

        URL nexusUrl = nexus().getUrl();
        URI uri = new URIBuilder().setHost(nexusUrl.getHost()).setPath(nexusUrl.getPath()).setPort(nexusUrl.getPort())
                .setQuery(nexusUrl.getQuery()).setScheme(nexusUrl.getProtocol()).setUserInfo("jdamick", "asdfasdfasdf")
                .build().resolve("content/groups/public/");
        HttpClient httpclient = new DefaultHttpClient();

        {// request 1
            HttpGet req1 = new HttpGet(uri);
            HttpResponse resp1 = httpclient.execute(req1);
            assertEquals(200, resp1.getStatusLine().getStatusCode());

            RecordedRequest request = server.takeRequest(); // 1 request recorded
            assertEquals("/api/v3/user?private_token=asdfasdfasdf", request.getPath());
            req1.releaseConnection();
        }

        // failure checks
        { // request 2
            HttpGet req2 = new HttpGet(uri);
            HttpResponse resp2 = httpclient.execute(req2);
            assertEquals(401, resp2.getStatusLine().getStatusCode());
            req2.releaseConnection();
        }

        { // request 3
            HttpGet req3 = new HttpGet(uri);
            HttpResponse resp3 = httpclient.execute(req3);
            assertEquals(401, resp3.getStatusLine().getStatusCode());
            req3.releaseConnection();
        }
    }
}
