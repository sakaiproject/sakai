package org.sakaiproject.mailarchive;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.server.SMTPServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * This is designed to test the basic handling on mail.
 */
@RunWith(MockitoJUnitRunner.class)
public class SakaiMessageHandlerTest {

    @Mock
    private InternationalizedMessages rb;

    @Mock
    private ServerConfigurationService serverConfigurationService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private AliasService aliasService;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private SiteService siteService;

    @Mock
    private TimeService timeService;

    @Mock
    private ThreadLocalManager threadLocalManager;

    @Mock
    private ContentHostingService contentHostingService;

    @Mock
    private MailArchiveService mailArchiveService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Session session;

    private SakaiMessageHandlerFactory messageHandlerFactory;

    @Before
    public void setUp() throws Exception {
        messageHandlerFactory = new SakaiMessageHandlerFactory();
        messageHandlerFactory.setInternationalizedMessages(rb);
        messageHandlerFactory.setServerConfigurationService(serverConfigurationService);
        messageHandlerFactory.setEntityManager(entityManager);
        messageHandlerFactory.setAliasService(aliasService);
        messageHandlerFactory.setUserDirectoryService(userDirectoryService);
        messageHandlerFactory.setSiteService(siteService);
        messageHandlerFactory.setTimeService(timeService);
        messageHandlerFactory.setThreadLocalManager(threadLocalManager);
        messageHandlerFactory.setContentHostingService(contentHostingService);
        messageHandlerFactory.setMailArchiveService(mailArchiveService);
        messageHandlerFactory.setSessionManager(sessionManager);

        // Binding to port 0 means that it picks a random port to listen on.
        when(serverConfigurationService.getInt("smtp.port", 25)).thenReturn(0);
        when(serverConfigurationService.getBoolean("smtp.enabled", false)).thenReturn(true);
        when(serverConfigurationService.getString("sakai.version", "unknown")).thenReturn("1.2.3");
        when(serverConfigurationService.getServerName()).thenReturn("sakai.example.com");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        messageHandlerFactory.init();
    }

    @After
    public void tearDown() throws Exception {
        messageHandlerFactory.destroy();
    }


    @Test
    public void testStarted() {
        // This just checks that the server got started.
        Assert.assertNotNull("No server started up.", messageHandlerFactory.getServer());
    }

    @Test(expected = SMTPException.class)
    public void testRejectedDomain() throws IOException {
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("test@gmail.com");
    }

    // Would be good to check the exception code here.
    @Test(expected = SMTPException.class)
    public void testRejectedAddress() throws Exception {
        when(aliasService.getTarget("user")).thenThrow(IdUnusedException.class);
        when(rb.getString("err_addr_unknown")).thenReturn("err_addr_unknown");
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("user@sakai.example.com");
    }

    // This shouldn't end up in the archive.
    @Test()
    public void testIgnorePostmaster() throws Exception {
        when(aliasService.getTarget("postmaster")).thenThrow(IdUnusedException.class);
        when(rb.getString("err_addr_unknown")).thenReturn("err_addr_unknown");
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("postmaster@sakai.example.com");
        writeData(client, "/simple-email.txt");
    }

    @Test
    public void testSiteIdAccept() throws Exception {
        User sender = mock(User.class);
        when(userDirectoryService.findUsersByEmail("sender@example.com")).thenReturn(Collections.singleton(sender));

        MailArchiveChannel channel = mock(MailArchiveChannel.class);
        when(channel.getEnabled()).thenReturn(true);
        when(channel.getOpen()).thenReturn(false);
        when(channel.allowAddMessage(sender)).thenReturn(true);
        when(channel.getId()).thenReturn("channelId");
        when(channel.getContext()).thenReturn("siteId");

        when(mailArchiveService.channelReference("siteId", SiteService.MAIN_CONTAINER)).thenReturn("/site/siteId");
        when(mailArchiveService.getMailArchiveChannel("/site/siteId")).thenReturn(channel);

        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("siteId@sakai.example.com");
        writeData(client, "/simple-email.txt");

        verify(channel, times(1)).addMailArchiveMessage(anyString(), eq("sender@example.com"), any(), any(), any(), any());
    }

    @Test
    // Check that the BATV parsing is working correctly
    public void testSiteIdAcceptBatv() throws Exception {
        User sender = mock(User.class);
        when(userDirectoryService.findUsersByEmail("sender@example.com")).thenReturn(Collections.singleton(sender));

        MailArchiveChannel channel = mock(MailArchiveChannel.class);
        when(channel.getEnabled()).thenReturn(true);
        when(channel.getOpen()).thenReturn(false);
        when(channel.allowAddMessage(sender)).thenReturn(true);
        when(channel.getId()).thenReturn("channelId");
        when(channel.getContext()).thenReturn("siteId");

        when(mailArchiveService.channelReference("siteId", SiteService.MAIN_CONTAINER)).thenReturn("/site/siteId");
        when(mailArchiveService.getMailArchiveChannel("/site/siteId")).thenReturn(channel);

        SmartClient client = createClient();
        client.from("prvs=2987A7B7C7=sender@example.com");
        client.to("siteId@sakai.example.com");
        // BATV is only in the envelope so the mail message should be the same.
        writeData(client, "/simple-email.txt");

        verify(channel, times(1)).addMailArchiveMessage(anyString(), eq("sender@example.com"), any(), any(), any(), any());
    }

    // Check that sending to multiple addressess at the same time works.
    @Test
    public void testMultipleSiteIdAccept() throws Exception {
        User sender = mock(User.class);
        when(userDirectoryService.findUsersByEmail("sender@example.com")).thenReturn(Collections.singleton(sender));

        MailArchiveChannel channel1 = mock(MailArchiveChannel.class);
        when(channel1.getEnabled()).thenReturn(true);
        when(channel1.getOpen()).thenReturn(false);
        when(channel1.allowAddMessage(sender)).thenReturn(true);
        when(channel1.getId()).thenReturn("channelId");
        when(channel1.getContext()).thenReturn("siteId1");

        MailArchiveChannel channel2 = mock(MailArchiveChannel.class);
        when(channel2.getEnabled()).thenReturn(true);
        when(channel2.getOpen()).thenReturn(false);
        when(channel2.allowAddMessage(sender)).thenReturn(true);
        when(channel2.getId()).thenReturn("channelId");
        when(channel2.getContext()).thenReturn("siteId2");

        when(mailArchiveService.channelReference("siteId1", SiteService.MAIN_CONTAINER)).thenReturn("/site/siteId1");
        when(mailArchiveService.getMailArchiveChannel("/site/siteId1")).thenReturn(channel1);
        when(mailArchiveService.channelReference("siteId2", SiteService.MAIN_CONTAINER)).thenReturn("/site/siteId2");
        when(mailArchiveService.getMailArchiveChannel("/site/siteId2")).thenReturn(channel2);

        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("siteId1@sakai.example.com");
        client.to("siteId2@sakai.example.com");
        writeData(client, "/simple-email.txt");

        verify(channel1, times(1)).addMailArchiveMessage(anyString(), eq("sender@example.com"), any(), any(), any(), any());
        verify(channel2, times(1)).addMailArchiveMessage(anyString(), eq("sender@example.com"), any(), any(), any(), any());
    }

    @Test
    public void testIgnoreNoReply() throws Exception {
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("no-reply@sakai.example.com");
        writeData(client, "/no-reply.txt");
    }

    @Test(expected = SMTPException.class)
    public void testNoPermission() throws Exception {
        // Here there is no permission, this should never happen in real life
        String reference = "/mailarchive/no-permission/main";
        when(mailArchiveService.channelReference("no-permission", SiteService.MAIN_CONTAINER)).thenReturn(reference);
        when(mailArchiveService.getMailArchiveChannel(reference)).thenThrow(PermissionException.class);
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("no-permission@sakai.example.com");
        writeData(client, "/no-permission.txt");
    }

    @Test
    public void testPostmaster() throws Exception {
        // Here there is no postmaster which may happen and should be dealt with
        String reference = "/mailarchive/no-permission/main";
        when(mailArchiveService.channelReference("no-permission", SiteService.MAIN_CONTAINER)).thenReturn(reference);
        when(mailArchiveService.getMailArchiveChannel(reference)).thenThrow(PermissionException.class);
        when(userDirectoryService.getUser("postmaster")).thenThrow(UserNotDefinedException.class);
        SmartClient client = createClient();
        client.from("sender@example.com");
        client.to("no-permission@sakai.example.com");
        writeData(client, "/no-permission.txt");
    }

    /**
     * Just creates a client connected to the test server.
     *
     * @return A connected client.
     * @throws IOException If it failed to connecto to the server.
     */
    public SmartClient createClient() throws IOException {
        SMTPServer server = messageHandlerFactory.getServer();
        return new SmartClient("localhost", server.getPort(), "test");
    }

    public void writeData(SmartClient client, String resource) throws IOException {
        client.dataStart();
        InputStream is = getClass().getResourceAsStream(resource);
        if(is == null) {
            throw new FileNotFoundException("Couldn't find in classpath: "+ resource);
        }
        byte[] bytes = IOUtils.toByteArray(is);
        client.dataWrite(bytes, bytes.length);
        client.dataEnd();
    }
}
