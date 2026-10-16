/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://opensource.org/licenses/ecl2
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.email.impl.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;

/** Real Spring-wired service, real asynchronous SMTP delivery, no mocked internal services. */
public class EmailServiceDeliveryTest {
    private static final String TEXT = "Meeting confirmation — 日本語, español";
    private static final String HTML = "<p>" + TEXT + "</p>";
    private static final String CALENDAR = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n";
    private static Wiser smtp;
    private static EmailServiceTestConfiguration configuration;
    private static EmailService emailService;
    private static final Properties ORIGINAL_PROPERTIES = new Properties();

    @BeforeClass
    public static void startMailService() throws Exception {
        for (String name : List.of("mail.smtp.host", "mail.smtp.port", "mail.smtp.from")) {
            if (System.getProperty(name) != null) {
                ORIGINAL_PROPERTIES.setProperty(name, System.getProperty(name));
            }
        }
        smtp = Wiser.create(new SMTPServer.Builder().bindAddress(InetAddress.getLoopbackAddress())
                .port(0).hostName("localhost"));
        smtp.start();
        configuration = new EmailServiceTestConfiguration(smtp.getServer().getPortAllocated());
        emailService = configuration.emailService();
    }

    @AfterClass
    public static void stopMailService() {
        if (configuration != null) {
            configuration.close();
        }
        if (smtp != null) {
            smtp.stop();
        }
        for (String name : List.of("mail.smtp.host", "mail.smtp.port", "mail.smtp.from")) {
            if (ORIGINAL_PROPERTIES.containsKey(name)) {
                System.setProperty(name, ORIGINAL_PROPERTIES.getProperty(name));
            } else {
                System.clearProperty(name);
            }
        }
    }

    @Before
    public void clearInbox() {
        smtp.getMessages().clear();
    }

    @Test
    public void deliversSignupHtmlAndCalendarAlternative() throws Exception {
        EmailMessage message = message(HTML);
        message.setContentType("text/html");
        message.setHeader("multipart-subtype", "alternative");
        message.addAttachment(new Attachment(new ByteArrayDataSource(CALENDAR, "text/calendar; charset=UTF-8"), "meeting.ics"));
        emailService.send(message, true);

        MimeMessage received = receive(1);
        assertTrue(received.isMimeType("multipart/alternative"));
        Multipart parts = (Multipart) received.getContent();
        assertEquals(2, parts.getCount());
        assertEquals(HTML, parts.getBodyPart(0).getContent());
        assertEquals(CALENDAR, new String(parts.getBodyPart(1).getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        assertEquals("meeting.ics", parts.getBodyPart(1).getFileName());
    }

    @Test
    public void deliversSamigoMultipartNotification() throws Exception {
        User student = mock(User.class);
        when(student.getEmail()).thenReturn("student@example.com");
        List<String> headers = List.of("From: sender@example.com", "To: student@example.com", "Subject: " + TEXT,
                "Content-Type: multipart/alternative; boundary=\"sakai-boundary\"", "MIME-Version: 1.0");
        String body = "This message is for MIME-compliant mail readers.\n\n"
                + "--sakai-boundary\nContent-Type: text/plain\n\n" + TEXT
                + "\n\n--sakai-boundary\nContent-Type: text/html\n\n" + HTML
                + "\n\n--sakai-boundary--\n\n";
        emailService.sendToUsers(List.of(student), headers, body);

        MimeMessage received = receive(1);
        assertTrue(received.isMimeType("multipart/alternative"));
        Multipart parts = (Multipart) received.getContent();
        assertEquals(2, parts.getCount());
        assertEquals(TEXT, ((String) parts.getBodyPart(0).getContent()).trim());
        assertEquals(HTML, ((String) parts.getBodyPart(1).getContent()).trim());
    }

    @Test
    public void deliversHtmlWithBinaryAttachment() throws Exception {
        byte[] data = {0, 1, 2, 10, 13, 127, (byte) 255};
        EmailMessage message = message(HTML);
        message.setContentType("text/html");
        message.addAttachment(new Attachment(new ByteArrayDataSource(data, "application/octet-stream"), "meeting.bin"));
        emailService.send(message, true);

        MimeMessage received = receive(1);
        assertTrue(received.isMimeType("multipart/mixed"));
        Multipart parts = (Multipart) received.getContent();
        assertEquals(HTML, parts.getBodyPart(0).getContent());
        assertArrayEquals(data, parts.getBodyPart(1).getInputStream().readAllBytes());
        assertEquals("meeting.bin", parts.getBodyPart(1).getFileName());
    }

    @Test
    public void deliversPlainTextToBatchedRecipientsWithoutExposingBcc() throws Exception {
        EmailMessage message = message(TEXT + "\r\n");
        message.addRecipient(RecipientType.BCC, "hidden@example.com");
        emailService.send(message, true);

        receive(2);
        assertEquals(List.of("student@example.com", "hidden@example.com"),
                smtp.getMessages().stream().map(m -> m.getEnvelopeReceiver()).toList());
        for (int i = 0; i < 2; i++) {
            MimeMessage received = decode(i);
            assertEquals(TEXT + "\r\n", received.getContent());
            assertNull(received.getHeader("Bcc"));
        }
    }

    private static EmailMessage message(String body) throws Exception {
        EmailMessage message = new EmailMessage("sender@example.com", TEXT, body);
        message.addRecipient(RecipientType.TO, "student@example.com");
        return message;
    }

    private static MimeMessage receive(int expected) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (smtp.getMessages().size() < expected && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertEquals("Email did not reach the local SMTP server", expected, smtp.getMessages().size());
        MimeMessage received = decode(0);
        assertEquals(TEXT, received.getSubject());
        return received;
    }

    private static MimeMessage decode(int index) throws Exception {
        return new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(smtp.getMessages().get(index).getData()));
    }
}
