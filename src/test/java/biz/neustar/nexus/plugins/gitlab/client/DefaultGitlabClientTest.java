package biz.neustar.nexus.plugins.gitlab.client;

import static org.junit.Assert.assertNotNull;

import java.rmi.RemoteException;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.junit.Test;


public class DefaultGitlabClientTest {


    @Test
    public void testInitialize() throws InitializationException, RemoteException {
        DefaultGitlabDao gitlab = new DefaultGitlabDao();
        assertNotNull(gitlab);
    }



}
