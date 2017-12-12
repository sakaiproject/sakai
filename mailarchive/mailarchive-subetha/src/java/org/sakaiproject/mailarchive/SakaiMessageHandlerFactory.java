/**
 * Copyright (c) 2014-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.mailarchive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.*;
import org.subethamail.smtp.server.SMTPServer;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

import static org.sakaiproject.mailarchive.api.MailArchiveService.*;

/**
 * This contains lots of the code from the original SakaiMailet.
 * It should do rejection at RCPT time rather than having to generate bounce messages itself.
 */
@Slf4j
public class SakaiMessageHandlerFactory implements MessageHandlerFactory {
    /**
     * The user name of the postmaster user - the one who posts incoming mail.
     */
    public static final String POSTMASTER = "postmaster";

    private SMTPServer server;

    private InternationalizedMessages rb;
    private ServerConfigurationService serverConfigurationService;
    private EntityManager entityManager;
    private AliasService aliasService;
    private UserDirectoryService userDirectoryService;
    private SiteService siteService;
    private TimeService timeService;
    private ThreadLocalManager threadLocalManager;
    private ContentHostingService contentHostingService;
    private MailArchiveService mailArchiveService;
    private SessionManager sessionManager;

    public void setInternationalizedMessages(InternationalizedMessages rb) {
        this.rb = rb;
    }

    public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
        this.threadLocalManager = threadLocalManager;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setAliasService(AliasService aliasService) {
        this.aliasService = aliasService;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setMailArchiveService(MailArchiveService mailArchiveService) {
        this.mailArchiveService = mailArchiveService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // used when parsing email header parts
    private static final String NAME_PREFIX = "name=";

    public void init() {
        Objects.requireNonNull(rb, "ResourceLoader must be set");
        Objects.requireNonNull(serverConfigurationService, "ServerConfigurationService must be set");
        Objects.requireNonNull(entityManager, "EntityManager must be set");
        Objects.requireNonNull(aliasService, "AliasService must be set");
        Objects.requireNonNull(userDirectoryService, "UserDirectoryService must be set");
        Objects.requireNonNull(siteService, "SiteService must be set");
        Objects.requireNonNull(timeService, "TimeService must be set");
        Objects.requireNonNull(threadLocalManager, "ThreadLocalManager must be set");
        Objects.requireNonNull(contentHostingService, "ContentHostingService must be set");
        Objects.requireNonNull(mailArchiveService, "MailArchiveService must be set");
        Objects.requireNonNull(sessionManager, "SessionManager must be set");

        if (serverConfigurationService.getBoolean("smtp.enabled", false)) {
            server = new SMTPServer(this);

            server.setHostName(serverConfigurationService.getServerName());
            server.setPort(serverConfigurationService.getInt("smtp.port", 25));
            server.setSoftwareName("SubEthaSMTP - Sakai (" + serverConfigurationService.getString("sakai.version", "unknown") +
                    ")");
            // We don't support smtp.dns.1 and smtp.dns.2
            server.setMaxConnections(100);
            server.start();
        }
    }

    public void destroy() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Get the started SMTP server. This is mainly here so that in set setup we can query it.
     * @return The started test server or <code>null</code> if there isn't one.
     */
    public SMTPServer getServer() {
        return server;
    }

    @Override
    public MessageHandler create(MessageContext ctx) {
        return new MessageHandler() {
            private String from;
            private Collection<Recipient> recipients = new LinkedList<>();

            @Override
            public void from(String from) throws RejectException {
                try {
                    SplitEmailAddress address = SplitEmailAddress.parse(from);
                    this.from = address.getLocal() + "@" + address.getDomain();
                } catch (IllegalArgumentException iae) {
                    log.debug("Not allowing return path of: {}", from);
                    throw new RejectException("Not allowing return path of: "+ from);
                }
            }

            @Override
            public void recipient(String to) throws RejectException {
                SplitEmailAddress address = SplitEmailAddress.parse(to);

                if (serverConfigurationService.getServerName().equalsIgnoreCase(address.getDomain())) {
                    // || serverConfigurationService.getServerNameAliases().contains(address.getDomain())) {
                    Recipient recipient = new Recipient();
                    recipient.address = address;
                    recipient.channel = getMailArchiveChannel(address.getLocal());
                    if (recipient.channel != null) {
                        recipients.add(recipient);
                    }
                } else {
                    // TODO Correct SMTP error?
                    throw new RejectException(551, "Don't accept mail for: " + address.getDomain());
                }

            }

            @Override
            public void data(InputStream data) throws RejectException, IOException {
                // Want to buffer a little bit in memory and then write it all out to disk if it's large.
                // TODO Switch to buffer that will switch to a file later on if the input is too big.
                // BufferedInputStream smallBuffer = new BufferedInputStream(data, 65535);
                // smallBuffer.
                // SharedFileInputStream fis = new SharedFileInputStream(null);

                org.sakaiproject.tool.api.Session session = sessionManager.getCurrentSession();
                try {
                    session.setUserId(POSTMASTER);
                    session.setUserEid(POSTMASTER);

                    // TODO Proper properties.
                    // The reads the entire body of the message into a byte array which is far from optimal.
                    MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()), data);

                    // Date can be null, need to fallback to better replacement
                    Date sent = msg.getSentDate();
                    if (sent == null) {
                        sent = new Date();
                        msg.setSentDate(sent);
                        log.debug("No Date header, defaulting to now.");
                    }
                    // Make sure the message has an ID
                    String id = msg.getMessageID();
                    if (id == null) {
                        id = "<"+ UUID.randomUUID()+ "@"+ serverConfigurationService.getServerName()+ ">";
                        msg.setHeader("Message-ID", id);
                        log.debug("No Message-ID header, generated: "+ id);
                    }

                    String subject = StringUtils.trimToNull(msg.getSubject());

                    Enumeration headers = msg.getAllHeaderLines();
                    List<String> mailHeaders = new ArrayList<>();
                    while (headers.hasMoreElements()) {
                        String line = (String) headers.nextElement();
                        // check if string starts with "Content-Type", ignoring case
                        if (line.regionMatches(true, 0, HEADER_CONTENT_TYPE, 0, HEADER_CONTENT_TYPE.length())) {
                            String contentType = line.substring(0, HEADER_CONTENT_TYPE.length());
                            mailHeaders.add(line.replaceAll(contentType, HEADER_OUTER_CONTENT_TYPE));
                        }
                        // don't copy null subject lines. we'll add a real one below
                        if (!(line.regionMatches(true, 0, HEADER_SUBJECT, 0, HEADER_SUBJECT.length()) &&
                                subject == null))
                            mailHeaders.add(line);

                    }

                    //Add headers for a null subject, keep null in DB
                    if (subject == null) {
                        mailHeaders.add(HEADER_SUBJECT + ": <" + rb.getString("err_no_subject") + ">");
                    }

                    if (log.isDebugEnabled()) {
                        log.debug(id + " : mail: from:" + from + " sent: " + timeService.newTime(sent.getTime()).toStringLocalFull()
                                + " subject: " + subject);
                    }

                    // process for each recipient
                    for (Recipient recipient : recipients) {
                        String mailId = recipient.address.getLocal();
                        try {
                            MailArchiveChannel channel = recipient.channel;
                            // Should be redundant as we shouldn't ever have null channels.
                            if (channel == null) return;

                            // prepare the message
                            StringBuilder bodyBuf[] = new StringBuilder[2];
                            bodyBuf[0] = new StringBuilder();
                            bodyBuf[1] = new StringBuilder();
                            List<Reference> attachments = entityManager.newReferenceList();
                            String siteId = null;
                            if (siteService.siteExists(channel.getContext())) {
                                siteId = channel.getContext();
                            }

                            try {
                                StringBuilder bodyContentType = new StringBuilder();
                                parseParts(siteId, msg, id, bodyBuf, bodyContentType, attachments, -1);

                                if (bodyContentType.length() > 0) {
                                    // save the content type of the message body - which may be different from the
                                    // overall MIME type of the message (multipart, etc)
                                    mailHeaders.add(HEADER_INNER_CONTENT_TYPE + ": " + bodyContentType);
                                }
                            } catch (MessagingException e) {
                                // NOTE: if this happens it just means we don't get the extra header, not the end of the world
                                log.warn("MessagingException: service(): msg.getContent() threw: " + e, e);
                            } catch (IOException e) {
                                // NOTE: if this happens it just means we don't get the extra header, not the end of the world
                                log.warn("IOException: service(): msg.getContent() threw: " + e, e);
                            }

                            mailHeaders.add("List-Id: <" + channel.getId() + "." + channel.getContext()
                                    + "." + serverConfigurationService.getServerName() + ">");

                            List<String> archiveHeaders = mailHeaders;
                            if (channel.getReplyToList()) {
                                archiveHeaders = mailHeaders.stream()
                                        .filter(h -> !h.startsWith("Reply-To:")).collect(Collectors.toList());
                                // Note: can't use recipient, since it's host may be configured as mailId@myhost.james
                                String mailHost = serverConfigurationService.getServerName();

                                // InternetAddress does validation
                                InternetAddress replyTo = new InternetAddress(mailId + "@" + mailHost);
                                if (log.isDebugEnabled()) {
                                    log.debug("Set Reply-To address to " + replyTo.toString());
                                }
                                archiveHeaders.add("Reply-To: " + replyTo.toString());

                            }
                            // post the message to the group's channel
                            String body[] = new String[2];
                            body[0] = bodyBuf[0].toString(); // plain/text
                            body[1] = bodyBuf[1].toString(); // html/text

                            try {
                                // post the message to the group's channel
                                channel.addMailArchiveMessage(subject, from, timeService.newTime(sent.getTime()),
                                    archiveHeaders, attachments, body);
                            } catch (PermissionException pe) {
                                // INDICATES that the current user does not have permission to add or get the mail archive message from the current channel
                                // This generally should not happen because the current user should be the postmaster
                                log.warn("mailarchive PermissionException message service failure: (id=" + id + ") (mailId=" + mailId + ") : " + pe, pe);
                            }

                            if (log.isDebugEnabled()) {
                                log.debug(id + " : delivered to:" + mailId);
                            }
                        } catch (Exception ex) {
                            // INDICATES that some general exception has occurred which we did not expect
                            // This definitely should NOT happen
                            log.error("mailarchive General message service exception: (id=" + id + ") (mailId=" + mailId + ") : " + ex, ex);
                        }
                    }
                } catch (MessagingException me) {
                    // TODO
                    throw new RejectException();
                } finally {
                    session.clear();
                    // clear out any current current bindings
                    threadLocalManager.clear();
                }
            }

            /**
             * This checks that we should accept mail for the supplied recipient
             * @param mailId The host local mail ID.
             * @return The channel that the mail should be appended to or <code>null</code> if it should be dropped.
             * @throws RejectException If the channel couldn't be found or the sender isn't allowed to post to it.
             */
            protected MailArchiveChannel getMailArchiveChannel(String mailId) throws RejectException {

                org.sakaiproject.tool.api.Session session = sessionManager.getCurrentSession();
                try {

                    session.setUserId(POSTMASTER);
                    session.setUserEid(POSTMASTER);

                    // eat the no-reply
                    if ("no-reply".equalsIgnoreCase(mailId)) {
                        if (log.isInfoEnabled()) {
                            log.info("Incoming message mailId (" + mailId + ") set to no-reply, mail processing cancelled");
                        }
                        return null;
                    }

                    // find the channel (mailbox) that this is addressed to
                    // for now, check only for it being a site or alias to a site.
                    // %%% - add user and other later -ggolden
                    MailArchiveChannel channel = null;

                    // first, assume the mailId is a site id
                    String channelRef = mailArchiveService.channelReference(mailId, SiteService.MAIN_CONTAINER);
                    try {
                        channel = mailArchiveService.getMailArchiveChannel(channelRef);
                        if (log.isDebugEnabled()) {
                            log.debug("Incoming message mailId (" + mailId + ") IS a valid site channel reference");
                        }
                    } catch (IdUnusedException goOn) {
                        // INDICATES the incoming message is NOT for a currently valid site
                        if (log.isDebugEnabled()) {
                            log.debug("Incoming message mailId (" + mailId + ") is NOT a valid site channel reference, will attempt more matches");
                        }
                    }

                    // next, if not a site, see if it's an alias to a site or channel
                    if (channel == null) {
                        // if not an alias, it will throw the IdUnusedException caught below
                        Reference ref = entityManager.newReference(aliasService.getTarget(mailId));

                        if (ref.getType().equals(SiteService.APPLICATION_ID)) {
                            // ref is a site
                            // now we have a site reference, try for it's channel
                            channelRef = mailArchiveService.channelReference(ref.getId(), SiteService.MAIN_CONTAINER);
                            if (log.isDebugEnabled()) {
                                log.debug("Incoming message mailId (" + mailId + ") IS a valid site reference (" + ref.getId() + ")");
                            }
                        } else if (ref.getType().equals(APPLICATION_ID)) {
                            // ref is a channel
                            channelRef = ref.getReference();
                            if (log.isDebugEnabled()) {
                                log.debug("Incoming message mailId (" + mailId + ") IS a valid channel reference (" + ref.getId() + ")");
                            }
                        } else {
                            // ref cannot be be matched
                            if (log.isInfoEnabled()) {
                                log.info("Mail rejected: unknown address: " + mailId + " : mailId (" + mailId + ") does NOT match site, alias, or other current channel");
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Incoming message mailId (" + mailId + ") is NOT a valid does NOT match site, alias, or other current channel reference (" + ref.getId() + "), message rejected");
                            }
                            throw new IdUnusedException(mailId);
                        }

                        // if there's no channel for this site, it will throw the IdUnusedException caught below
                        channel = mailArchiveService.getMailArchiveChannel(channelRef);
                        if (log.isDebugEnabled()) {
                            log.debug("Incoming message mailId (" + mailId + ") IS a valid channel (" + channelRef + "), found channel: " + channel);
                        }
                    }
                    if (channel == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Incoming message mailId (" + mailId + "), channelRef (" + channelRef + ") could not be resolved and is null.");
                        }
                        // this should never happen but it is here just in case
                        throw new IdUnusedException(mailId);
                    }

                    // skip disabled channels
                    if (!channel.getEnabled()) {
                        // INDICATES that the channel is NOT currently enabled so no messages can be received
                        if (!from.startsWith(POSTMASTER)) {
                            // BOUNCE REPLY - send a message back to the user to let them know their email failed
                            String errMsg = rb.getString("err_email_off") + "\n\n";
                            String mailSupport = StringUtils.trimToNull(serverConfigurationService.getString("mail.support"));
                            if (mailSupport != null) {
                                errMsg += rb.getFormattedMessage("err_questions", mailSupport) + "\n";
                            }
                            throw new RejectException(450, errMsg);
                        }

                        if (log.isInfoEnabled()) {
                            log.info("Mail rejected: channel (" + channelRef + ") not enabled: " + mailId);
                        }
                        return null;
                    }

                    // for non-open channels, make sure the from is a member
                    if (!channel.getOpen()) {
                        // see if our fromAddr is the email address of any of the users who are permitted to add messages to the channel.
                        if (fromInvalidUser(from, channel)) {
                            // INDICATES user is not allowed to send messages to this group
                            if (log.isInfoEnabled()) {
                                log.info("Mail rejected: from: " + from + " not authorized for site: " + mailId + " and channel (" + channelRef + ")");
                            }
                            // BOUNCE REPLY - send a message back to the user to let them know their email failed
                            String errMsg = rb.getString("err_not_member") + "\n\n";
                            String mailSupport = StringUtils.trimToNull(serverConfigurationService.getString("mail.support"));
                            if (mailSupport != null) {
                                errMsg += rb.getFormattedMessage("err_questions", mailSupport) + "\n";
                            }
                            throw new RejectException(550, errMsg);
                        }
                    }
                    return channel;
                } catch (IdUnusedException e) {
                    // if this is to the postmaster, and there's no site, channel or alias for the postmaster,
                    // then quietly eat the message
                    if (!POSTMASTER.equals(mailId) && !from.startsWith(POSTMASTER + "@")) {
                        String errMsg = rb.getString("err_addr_unknown") + "\n\n";
                        String mailSupport = StringUtils.trimToNull(serverConfigurationService.getString("mail.support"));
                        if (mailSupport != null) {
                            errMsg += rb.getFormattedMessage("err_questions", mailSupport) + "\n";
                        }
                        throw new RejectException(550, errMsg);
                    }
                } catch (PermissionException e) {
                    try {
                        // Check that we have a postmaster, if we don't then just swallow all mail with warning in logs.
                        userDirectoryService.getUser(POSTMASTER);

                        // INDICATES the channel is valid but the user has no permission to access it
                        // This generally should not happen because the current user should be the postmaster
                        log.warn("No access to alias, this should never happen.", e);
                        // BOUNCE REPLY - send a message back to the user to let them know their email failed
                        String errMsg = rb.getString("err_not_member") + "\n\n";
                        String mailSupport = StringUtils.trimToNull(serverConfigurationService.getString("mail.support"));
                        if (mailSupport != null) {
                            errMsg += rb.getFormattedMessage("err_questions", mailSupport) + "\n";
                        }
                        throw new RejectException(550, errMsg);
                    } catch (UserNotDefinedException unde) {
                        log.warn(String.format("no postmaster, incoming mail will not be processed until a " +
                                "postmaster user (id=%s) exists in this Sakai instance", POSTMASTER));
                    }
                } finally {
                    session.clear();
                }

                // Ignore
                return null;
            }

            @Override
            public void done() {

            }
        };
    }

    /**
     * Check if the fromAddr email address is recognized as belonging to a user who has permission to add to the channel.
     *
     * @param fromAddr The email address to check.
     * @param channel  The mail archive channel.
     * @return <code>false</code> if the email address is from a user who is authorized to add mail,
               <code>true</code> if not.
     */
    protected boolean fromInvalidUser(String fromAddr, MailArchiveChannel channel) {
        if ((fromAddr == null) || (fromAddr.length() == 0)) return true;

        // find the users with this email address
        Collection<User> users = userDirectoryService.findUsersByEmail(fromAddr);

        // Allow if any user can post messages
        return !users.stream().anyMatch(channel::allowAddMessage);
    }

    /**
     * Breaks email messages into parts which can be saved as files (saves as attachments) or viewed as plain text (added to body of message).
     *
     * @param siteId          Site associated with attachments, if any
     * @param p               The message-part embedded in a message..
     * @param id              The string containing the message's id.
     * @param bodyBuf         The string-buffers in which the plain/text and/or html/text message body is being built.
     * @param bodyContentType The value of the Content-Type header for the mesage body.
     * @param attachments     The ReferenceVector in which references to attachments are collected.
     * @param embedCount      An Integer that counts embedded messages (outer message is zero).
     * @return Value of embedCount (updated if this call processed any embedded messages).
     */
    protected Integer parseParts(String siteId, Part p, String id, StringBuilder bodyBuf[],
                                 StringBuilder bodyContentType, List<Reference> attachments, Integer embedCount)
            throws MessagingException, IOException {
        // increment embedded message counter
        if (p instanceof Message) {
            embedCount = embedCount + 1;
        }

        String type = p.getContentType();

        // discard if content-type is unknown
        if (type == null || "".equals(type)) {
            log.warn(this + " message with unknown content-type discarded");
        }

        // add plain text to bodyBuf[0]
        else if (p.isMimeType("text/plain") && p.getFileName() == null) {
            Object o;
            // let them convert to text if possible
            // but if bad encaps get the stream and do it ourselves
            try {
                o = p.getContent();
            } catch (java.io.UnsupportedEncodingException ignore) {
                o = p.getInputStream();
            }

            String txt = null;
            String innerContentType = p.getContentType();

            if (o instanceof String) {
                txt = (String) p.getContent();
                if (bodyContentType != null && bodyContentType.length() == 0)
                    bodyContentType.append(innerContentType);
            } else if (o instanceof InputStream) {
                InputStream in = (InputStream) o;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[in.available()];
                for (int len = in.read(buf); len != -1; len = in.read(buf))
                    out.write(buf, 0, len);
                String charset = (new ContentType(innerContentType)).getParameter("charset");
                // RFC 2045 says if no char set specified use US-ASCII.
                // If specified but illegal that's less clear. The common case is X-UNKNOWN.
                // my sense is that UTF-8 is most likely these days but the sample we got
                // was actually ISO 8859-1. Could also justify using US-ASCII. Duh...
                if (charset == null)
                    charset = "us-ascii";
                try {
                    txt = out.toString(MimeUtility.javaCharset(charset));
                } catch (java.io.UnsupportedEncodingException ignore) {
                    txt = out.toString("UTF-8");
                }
                if (bodyContentType != null && bodyContentType.length() == 0)
                    bodyContentType.append(innerContentType);
            }

            // remove extra line breaks added by mac Mail, perhaps others
            // characterized by a space followed by a line break
            if (txt != null) {
                txt = txt.replaceAll(" \n", " ");
            }

            // make sure previous message parts ended with newline
            if (bodyBuf[0].length() > 0 && bodyBuf[0].charAt(bodyBuf[0].length() - 1) != '\n')
                bodyBuf[0].append("\n");

            bodyBuf[0].append(txt);
        }

        // add html text to bodyBuf[1]
        else if (p.isMimeType("text/html") && p.getFileName() == null) {
            Object o;
            // let them convert to text if possible
            // but if bad encaps get the stream and do it ourselves
            try {
                o = p.getContent();
            } catch (java.io.UnsupportedEncodingException ignore) {
                o = p.getInputStream();
            }
            String txt = null;
            String innerContentType = p.getContentType();

            if (o instanceof String) {
                txt = (String) p.getContent();
                if (bodyContentType != null && bodyContentType.length() == 0)
                    bodyContentType.append(innerContentType);
            } else if (o instanceof InputStream) {
                InputStream in = (InputStream) o;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[in.available()];
                for (int len = in.read(buf); len != -1; len = in.read(buf))
                    out.write(buf, 0, len);
                String charset = (new ContentType(innerContentType)).getParameter("charset");
                if (charset == null)
                    charset = "us-ascii";
                try {
                    txt = out.toString(MimeUtility.javaCharset(charset));
                } catch (java.io.UnsupportedEncodingException ignore) {
                    txt = out.toString("UTF-8");
                }
                if (bodyContentType != null && bodyContentType.length() == 0)
                    bodyContentType.append(innerContentType);
            }

            // remove bad image tags and naughty javascript
            if (txt != null) {
                txt = Web.cleanHtml(txt);
            }

            bodyBuf[1].append(txt);
        }

        // process subparts of multiparts
        else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                embedCount = parseParts(siteId, mp.getBodyPart(i), id, bodyBuf, bodyContentType, attachments, embedCount);
            }
        }

        // Discard parts with mime-type application/applefile. If an e-mail message contains an attachment is sent from
        // a macintosh, you may get two parts, one for the data fork and one for the resource fork. The part that
        // corresponds to the resource fork confuses users, this has mime-type application/applefile. The best thing
        // is to discard it.
        else if (p.isMimeType("application/applefile")) {
            log.warn(this + " message with application/applefile discarded");
        }

        // discard enriched text version of the message.
        // Sakai only uses the plain/text or html/text version of the message.
        else if (p.isMimeType("text/enriched") && p.getFileName() == null) {
            log.warn(this + " message with text/enriched discarded");
        }

        // everything else gets treated as an attachment
        else {
            String name = p.getFileName();

            // look for filenames not parsed by getFileName()
            if (name == null && type.contains(NAME_PREFIX)) {
                name = type.substring(type.indexOf(NAME_PREFIX) + NAME_PREFIX.length());
            }
            // ContentType can't handle filenames with spaces or UTF8 characters
            if (name != null) {
                String decodedName = MimeUtility.decodeText(name); // first decode RFC 2047
                type = type.replace(name, URLEncoder.encode(decodedName, "UTF-8"));
                name = decodedName;
            }

            ContentType cType = null;
            try {
            	cType = new ContentType(type);
            } catch (ParseException e) {
            	cType = new ContentType("application/octet-stream");
            }

            if (name == null) {
                name = "unknown";
                // if file's parent is multipart/alternative,
                // provide a better name for the file
                if (p instanceof BodyPart) {
                    Multipart parent = ((BodyPart) p).getParent();
                    if (parent != null) {
                        String pType = parent.getContentType();
                        ContentType pcType = new ContentType(pType);
                        if (pcType.getBaseType().equalsIgnoreCase("multipart/alternative")) {
                            name = "message" + embedCount;
                        }
                    }
                }
                if (p.isMimeType("text/html")) {
                    name += ".html";
                } else if (p.isMimeType("text/richtext")) {
                    name += ".rtx";
                } else if (p.isMimeType("text/rtf")) {
                    name += ".rtf";
                } else if (p.isMimeType("text/enriched")) {
                    name += ".etf";
                } else if (p.isMimeType("text/plain")) {
                    name += ".txt";
                } else if (p.isMimeType("text/xml")) {
                    name += ".xml";
                } else if (p.isMimeType("message/rfc822")) {
                    name += ".txt";
                }
            }

            // read the attachments bytes, and create it as an attachment in content hosting
            if (p.getSize() > 0) {
                // can we ignore the attachment it it's just whitespace chars??
                Optional<ContentResource> attachment = createAttachment(siteId, attachments, cType.getBaseType(), name, p.getInputStream(), id);

                // add plain/text attachment reference (if plain/text message)
                if (attachment.isPresent() && bodyBuf[0].length() > 0)
                    bodyBuf[0].append("[see attachment: \"").append(name).append("\", size: ").append(attachment.get().getContentLength()).append(" bytes]\n\n");

                // add html/text attachment reference (if html/text message)
                if (attachment.isPresent() && bodyBuf[1].length() > 0)
                    bodyBuf[1].append("<p>[see attachment: \"").append(name).append("\", size: ").append(attachment.get().getContentLength()).append(" bytes]</p>");

                // add plain/text attachment reference (if no plain/text and no html/text)
                if (attachment.isPresent() && bodyBuf[0].length() == 0 && bodyBuf[1].length() == 0)
                    bodyBuf[0].append("[see attachment: \"").append(name).append("\", size: ").append(attachment.get().getContentLength()).append(" bytes]\n\n");
            }
        }

        return embedCount;
    }

    /**
     * Create an attachment, adding it to the list of attachments.
     * @return An optional containing the attachment or an empty optional.
     */
    protected Optional<ContentResource> createAttachment(String siteId, List<Reference> attachments, String type, String fileName, InputStream in, String id) {
        // we just want the file name part - strip off any drive and path stuff
        String name = FilenameUtils.getName(fileName);  //Validator.getFileName(fileName);
        String resourceName = Validator.escapeResourceName(fileName);

        // make a set of properties to add for the new resource
        ResourcePropertiesEdit props = contentHostingService.newResourceProperties();
        props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
        props.addProperty(ResourceProperties.PROP_DESCRIPTION, fileName);

        // make an attachment resource for this URL
        try {
            ContentResource attachment;
            if (siteId == null) {
                attachment = contentHostingService.addAttachmentResource(resourceName, type, in, props);
            } else {
                attachment = contentHostingService.addAttachmentResource(
                        resourceName, siteId, null, type, in, props);
            }

            // add a dereferencer for this to the attachments
            Reference ref = entityManager.newReference(attachment.getReference());
            attachments.add(ref);

            log.debug(id + " : attachment: " + ref.getReference() + " size: " + attachment.getContentLength());

            return Optional.of(attachment);
        } catch (Exception any) {
            log.warn(id + " : exception adding attachment resource: " + name + " : " + any.toString());
            return Optional.empty();
        }
    }

    /**
     * This is just a holder so we can keep the address and the channel we think it's going to in one place.
     */
    protected class Recipient {
        protected SplitEmailAddress address;
        protected MailArchiveChannel channel;
    }
}
