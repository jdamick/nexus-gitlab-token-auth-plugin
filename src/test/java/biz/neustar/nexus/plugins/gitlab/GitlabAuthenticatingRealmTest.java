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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonatype.sisu.filetasks.builder.FileRef.file;
import static org.sonatype.sisu.filetasks.builder.FileRef.path;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.inject.Inject;

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
import org.sonatype.nexus.testsuite.support.ParametersLoaders;
import org.sonatype.sisu.filetasks.FileTaskBuilder;


/**
 *
 * see: https://github.com/sonatype/nexus-testsuite-guide/blob/master/guide/src/test/java/org/sonatype/nexus/testsuite/guide/nrpits/README.md
 */
@NexusStartAndStopStrategy( NexusStartAndStopStrategy.Strategy.EACH_TEST )
public class GitlabAuthenticatingRealmTest extends NexusRunningParametrizedITSupport {

    @Inject
    private FileTaskBuilder overlays;

    @Parameterized.Parameters
    public static Collection<Object[]> hardcodedParameters()
    {
        return ParametersLoaders.firstAvailableTestParameters(
            ParametersLoaders.systemTestParameters(),
            ParametersLoaders.defaultTestParameters(),
            ParametersLoaders.testParameters(
                $(
                    "org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.3.0"
                )
            )
        ).load();
    }

    /*
    @Parameterized.Parameters
    public static Collection<Object[]> hardcodedParameters() {
        List<Object[]> result = Arrays.asList( new Object[][]{
            //{ "org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.3.0" },
            { "org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.3.1" }
        } );
        return result;
    }
    */
/*
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return firstAvailableTestParameters(
          systemTestParameters(),
          //
          testParameters(
              $("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle")
          )
          //
          ParametersLoaders.testParameters(new String[] {"org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.0",
                   "org.sonatype.nexus:nexus-oss-webapp:zip:bundle:2.1"} )
      ).load();
    }
*/
    protected Model getPomInfo() {
        File pomFile = new File("pom.xml");
        Model model = null;
        try {
            model = (new MavenXpp3Reader()).read(new FileReader(pomFile));
            model.setPomFile(pomFile);
        } catch(Exception ex) {
            // nothing
        }
        return model;
    }

    @Override
    protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
      // override the format of the nexus.log file
      configuration.setLogPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");

      // configure logging level of example plugins running in nexus
      //configuration.setLogLevel("org.sonatype.nexus.examples", "DEBUG");
      configuration.setLogLevel("DEBUG");
      configuration.setStartTimeout(120);
      // install the plugin we are testing
      //configuration.addPlugins(arg0)
      /* */

      Model model = getPomInfo();
      String pluginName = model.getArtifactId() + "-" + model.getVersion();
System.out.println("plugin name: " + pluginName);
      File plugin = new File("target/"+ pluginName + "-bundle.zip");
      assertTrue(plugin.exists());

      configuration.addPlugins(
              plugin
              //artifactResolver().resolveArtifact( "biz.neustar.nexus:nexus-gitlab-token-auth-plugin:zip:bundle:" + "0.1.0" )
          /*artifactResolver().resolvePluginFromDependencyManagement(
              "biz.neustar.nexus", "nexus-gitlab-token-auth-plugin"
          )*/
      );

      // <realm>NexusGitlabAuthenticationRealm</realm>
      configuration.addOverlays(
              overlays.copy()
                  .file(file(testData().resolveFile("gitlab-plugin.xml")))
                  .to().directory(path("sonatype-work/nexus/plugin-repository/" + pluginName + "/")),
              overlays.copy()
                  .file(file(testData().resolveFile("security-configuration.xml")))
                  .to().directory(path("sonatype-work/nexus/conf"))
      );
      /*
       * .addOverlays(
                overlays.copy()
                    .file( file( testData().resolveFile( "security-configuration.xml" ) ) )
                    .to().directory( path( "sonatype-work/nexus/conf" ) )
            );
       */

      return configuration;
    }

    public GitlabAuthenticatingRealmTest(String nexusBundleCoordinates) {
        super(nexusBundleCoordinates);
    }

    private GitlabAuthenticatingRealm realm;


    /*
     * https://github.com/sonatype/nexus-testsuite-guide
     */


    @Test
    public void testPlugin() throws Exception {
        assertTrue(nexus().isRunning());

        URL nexusUrl = nexus().getUrl();
        URI uri = new URIBuilder()
            .setHost(nexusUrl.getHost())
            .setPath(nexusUrl.getPath())
            .setPort(nexusUrl.getPort())
            .setQuery(nexusUrl.getQuery())
            .setScheme(nexusUrl.getProtocol())
            //.setUserInfo("jdamick", "rEyQEHRhTNqBAALWubqZ")
            .build().resolve("content/groups/public/");
System.out.println("\nGET TO: " + uri);
        HttpClient httpclient = new DefaultHttpClient();

        HttpGet get = new HttpGet(uri);
        HttpResponse resp = httpclient.execute(get);
System.out.println("status: "+ resp.getStatusLine().getReasonPhrase());
        assertEquals(200, resp.getStatusLine().getStatusCode());
    }

/*
    @Before
    public void setup() {
        realm = new GitlabAuthenticatingRealm();

    }

    @Test
    public void checkActiveFlag() throws Exception {
        assertFalse(GitlabAuthenticatingRealm.isActive());
        realm.initialize();
        assertTrue(GitlabAuthenticatingRealm.isActive());
    }

    @After
    public void teardown() {
        realm.dispose();
    }
*/
}
