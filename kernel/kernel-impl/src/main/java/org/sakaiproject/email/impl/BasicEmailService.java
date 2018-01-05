/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.email.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.CharacterSet;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.email.api.EmailHeaders;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * BasicEmailService implements the EmailService.
 * </p>
 */
@Slf4j
public class BasicEmailService implements EmailService
{
	protected static final String PROTOCOL_SMTP = "smtp";
	protected static final String PROTOCOL_SMTPS = "smtps";

	protected static final String POSTMASTER = "postmaster";

	/** As defined in the com.sun.mail.smtp part of javamail. */

	/** The SMTP server to connect to. */
	public static final String MAIL_HOST_T = "mail.%1$s.host";

	/**
	 * The SMTP server port to connect to, if the connect() method doesn't explicitly specify one.
	 * Defaults to 25.
	 */
	public static final String MAIL_PORT_T = "mail.%1$s.port";

	/**
	 * Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults
	 * to msg.getFrom() or InternetAddress.getLocalAddress(). NOTE: mail.smtp.user was previously
	 * used for this.
	 */
	public static final String MAIL_FROM_T = "mail.%1$s.from";

	/**
	 * If set to true, and a message has some valid and some invalid addresses, send the message
	 * anyway, reporting the partial failure with a SendFailedException. If set to false (the
	 * default), the message is not sent to any of the recipients if there is an invalid recipient
	 * address.
	 */
	public static final String MAIL_SENDPARTIAL_T = "mail.%1$s.sendpartial";

	/** Socket connection timeout value in milliseconds. Default is infinite timeout. */
	public static final String MAIL_CONNECTIONTIMEOUT_T = "mail.%1$s.connectiontimeout";

	/** Socket I/O timeout value in milliseconds. Default is infinite timeout. */
	public static final String MAIL_TIMEOUT_T = "mail.%1$s.timeout";

	/** Whether to authenticate when connecting to the mail server */
	public static final String MAIL_AUTH_T = "mail.%1$s.auth";

	/** Whether to enable the starting TLS */
	public static final String MAIL_TLS_ENABLE_T = "mail.%1$s.starttls.enable";

	/** What socket factory to use when connecting over SSL */
	public static final String MAIL_SOCKET_FACTORY_CLASS_T = "mail.%1$s.socketFactory.class";

	/** Whether to fallback to a different protocol if first attempt fails. */
	public static final String MAIL_SOCKET_FACTORY_FALLBACK_T = "mail.%1$s.socketFactory.fallback";

	/** Hostname used in outgoing SMTP HELO commands. */
	public static final String MAIL_LOCALHOST_T = "mail.%1$s.localhost";

	/** Class name of SSL socket factory */
	public static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	/** Whether to turn on mail debugging */
	public static final String MAIL_DEBUG = "mail.debug";

	public static final String MAIL_SENDFROMSAKAI = "mail.sendfromsakai";
	public static final String MAIL_SENDFROMSAKAI_EXCEPTIONS = "mail.sendfromsakai.exceptions";
	public static final String MAIL_SENDFROMSAKAI_FROMTEXT = "mail.sendfromsakai.fromtext";
	public static final String MAIL_SENDFROMSAKAI_MAXSIZE = "mail.sendfromsakai.maxsize";

	protected static final String CONTENT_TYPE = ContentType.TEXT_PLAIN;

	protected ServerConfigurationService serverConfigurationService;

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

	/** The protocol to use when connecting to the mail server */
	private String protocol;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration Note: keep these in sync with the TestEmailService, to make switching between them easier -ggolden
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Configuration: smtp server to use. */
	protected String m_smtp = null;

	/**
	 * Configuration: smtp server to use.
	 * 
	 * @param value
	 *        The smtp server string.
	 */
	public void setSmtp(String value)
	{
		m_smtp = value;
	}

	/** Configuration: smtp server port to use. */
	protected String m_smtpPort = null;

	/**
	 * Configuration: smtp server port to use.
	 * 
	 * @param value
	 *        The smtp server port string.
	 */
	public void setSmtpPort(String value)
	{
		m_smtpPort = value;
	}

	/** Configuration: smtp user for use with authenticated SMTP. */
	protected String m_smtpUser = null;

	/**
	 * Configuration: smtp user for use with authenticated SMTP.
	 * 
	 * @param value
	 *        The smtp user string.
	 */
	public void setSmtpUser(String value)
	{
	 	m_smtpUser = value;
	}

	/** Configuration: smtp password for use with authenticated SMTP. */
	protected String m_smtpPassword = null;

  	/**
	 * Configuration: smtp password for use with authenticated SMTP.
	 * 
	 * @param value
	 *        The smtp password string.
	 */
 	public void setSmtpPassword(String value)
	{
		m_smtpPassword = value;
	}

 	/** Configuration: send over SSL (or not) */
 	protected boolean m_smtpUseSSL;

	/**
	 * Configuration: send over SSL (or not)
	 * 
	 * @param useSSL
	 *            The setting
	 */
	public void setSmtpUseSSL(boolean useSSL)
	{
		m_smtpUseSSL = useSSL;
	}

	/** Configuration: send using TLS (or not). */
	protected boolean m_smtpUseTLS = false;

	/**
	 * Configuration: send using TLS (or not)
	 * 
	 * @param value
	 *        The setting
	 */
	public void setSmtpUseTLS(boolean value)
	{
		m_smtpUseTLS = value;
	}

	/** Configuration: set the mail.debug property so we can get proper output from javamail (or not). */
	protected boolean m_smtpDebug = false;

	/**
	 * Configuration: set the mail.debug property so we can get proper output from javamail (or not).
	 * 
	 * @param value
	 *        The setting
	 */
	public void setSmtpDebug(boolean value)
	{
		m_smtpDebug = value;
	}

	/** Configuration: optional smtp mail envelope return address. */
	protected String m_smtpFrom = null;

	/**
	 * Configuration: smtp mail envelope return address.
	 * 
	 * @param value
	 *        The smtp mail from address string.
	 */
	public void setSmtpFrom(String value)
	{
		m_smtpFrom = value;
	}

	/** Configuration: set to go into test mode, where mail is not really sent out. */
	protected boolean m_testMode = false;

	/**
	 * Configuration: set test mode.
	 * 
	 * @param value
	 *        The test mode value
	 */
	public void setTestMode(boolean value)
	{
		m_testMode = value;
	}

	/**
	 * Configuration: turns off transport sending.  This can be turned off to allow pr
	 * happen normally but only stop calling Transport.send.  Allows the code to run t
	 * checks and validations that testMode = true does not.
	 */
	protected boolean allowTransport = true;

	public void setAllowTransport(boolean allowTransport)
	{
		this.allowTransport = allowTransport;
	}

	/** The max # recipients to include in each message. */
	protected int m_maxRecipients = 100;

	/**
	 * Set max # recipients to include in each message.
	 * 
	 * @param setting
	 *        The max # recipients to include in each message. (as an integer string).
	 */
	public void setMaxRecipients(String setting)
	{
		m_maxRecipients = Integer.parseInt(setting);

		// validate - if invalid, restore to the default
		if (m_maxRecipients < 1) m_maxRecipients = 100;
	}

	/** Configuration: use a connection to the SMTP for only one message (or not). */
	protected boolean m_oneMessagePerConnection = false;

	/**
	 * Configuration: set use a connection to the SMTP for only one message (or not)
	 * 
	 * @param value
	 *        The setting
	 */
	public void setOneMessagePerConnection(boolean value)
	{
		m_oneMessagePerConnection = value;
	}

	/** Hostname to use for SMTP HELO commands */
	protected String m_smtpLocalhost = null;

	/**
	 * Hostname to use for SMTP HELO commands.
	 * RFC1123 section 5.2.5 and RFC2821 section 4.1.1.1
	 * 
	 *  @param value
	 *  		The hostname (eg foo.example.com)
	 */
	public void setSmtpLocalhost(String value)
	{
		m_smtpLocalhost = value;
	}

	/** Configuration: send partial email or fail on any errors */
	protected boolean m_sendPartial = true;

	public void setSendPartial(boolean sendPartial)
	{
		m_sendPartial = sendPartial;
	}

	/** Configuration: Socket connection timeout value in milliseconds. Default is infinite timeout. */
	protected String m_smtpConnectionTimeout = null;
	
	/** Configuration: Socket I/O timeout value in milliseconds. Default is infinite timeout. */
	protected String m_smtpTimeout = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// set the protocol to be used
		if (m_smtpUseSSL)
		{
			protocol = PROTOCOL_SMTPS;
		}
		else
		{
			protocol = PROTOCOL_SMTP;
		}

		// if no m_mailfrom set, set to the postmaster
		if (m_smtpFrom == null)
		{
			m_smtpFrom = POSTMASTER + "@" + serverConfigurationService.getServerName();
		}
		// initialize timeout values
		m_smtpConnectionTimeout = serverConfigurationService.getString(propName(MAIL_CONNECTIONTIMEOUT_T), null);
		m_smtpTimeout = serverConfigurationService.getString(propName(MAIL_TIMEOUT_T), null);

		// check for smtp protocol labeled values for backwards compatibility
		if (PROTOCOL_SMTPS.equals(protocol))
		{
			if (m_smtpConnectionTimeout == null)
				m_smtpConnectionTimeout = serverConfigurationService.getString(propName(MAIL_CONNECTIONTIMEOUT_T, PROTOCOL_SMTP), null);
		
			if (m_smtpTimeout == null)
				m_smtpTimeout = serverConfigurationService.getString(propName(MAIL_TIMEOUT_T, PROTOCOL_SMTP), null);
		}

		// promote these to the system properties, to keep others (James) from messing with them
		if (m_smtp != null) System.setProperty(propName(MAIL_HOST_T), m_smtp);
		if (m_smtpPort != null) System.setProperty(propName(MAIL_PORT_T), m_smtpPort);
		System.setProperty(propName(MAIL_FROM_T), m_smtpFrom);
		if (m_smtpConnectionTimeout != null) System.setProperty(propName(MAIL_CONNECTIONTIMEOUT_T), m_smtpConnectionTimeout);
		if (m_smtpTimeout != null) System.setProperty(propName(MAIL_TIMEOUT_T), m_smtpTimeout);

		log.info("init(): smtp: " + m_smtp + ((m_smtpPort != null) ? (":" + m_smtpPort) : "") + " bounces to: " + m_smtpFrom
				+ " maxRecipients: " + m_maxRecipients + " testMode: " + m_testMode
				+ ((m_smtpConnectionTimeout != null) ? (" smtpConnectionTimeout: " + m_smtpConnectionTimeout) : "")
				+ ((m_smtpTimeout != null) ? (" smtpTimeout: " + m_smtpTimeout) : ""));
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: org.sakai.service.email.EmailService
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public void sendMail(InternetAddress from, InternetAddress[] to, String subject, String content, InternetAddress[] headerTo,
			InternetAddress[] replyTo, List<String> additionalHeaders)
	{
		HashMap<RecipientType, InternetAddress[]> recipients = null;
		if (headerTo != null)
		{
			recipients = new HashMap<RecipientType, InternetAddress[]>();
			recipients.put(RecipientType.TO, headerTo);
		}
		sendMail(from, to, subject, content, recipients, replyTo, additionalHeaders, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendMail(InternetAddress from, InternetAddress[] to, String subject, String content,
			Map<RecipientType, InternetAddress[]> headerTo, InternetAddress[] replyTo,
			List<String> additionalHeaders, List<Attachment> attachments) {
		try {
			sendMailMessagingException(from, to, subject, content, headerTo, replyTo, additionalHeaders, attachments);
		}
		catch (MessagingException e)
		{
			log.error("Email.sendMail: exception: " + e.getMessage(), e);
		}
	}

	public void sendMailMessagingException(InternetAddress from, InternetAddress[] to, String subject, String content,
			Map<RecipientType, InternetAddress[]> headerTo, InternetAddress[] replyTo,
			List<String> additionalHeaders, List<Attachment> attachments) throws MessagingException
			{
		// some timing for debug
		long start = 0;
		if (log.isDebugEnabled()) start = System.currentTimeMillis();

		// if in test mode, use the test method
		if (m_testMode)
		{
			testSendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders, attachments);
			return;
		}

		if (m_smtp == null)
		{
			log.error("Unable to send mail as no smtp server is defined. Please set smtp@org.sakaiproject.email.api.EmailService value in sakai.properties");
			return;
		}

		if (from == null)
		{
			log.warn("sendMail: null from");
			return;
		}

		if (to == null)
		{
			log.warn("sendMail: null to");
			return;
		}

		if (content == null)
		{
			log.warn("sendMail: null content");
			return;
		}

		Properties props = createMailSessionProperties();

		Session session = Session.getInstance(props);

		// see if we have a message-id in the additional headers
		String mid = null;
		if (additionalHeaders != null)
		{
			for (String header : additionalHeaders)
			{
				if (header.toLowerCase().startsWith(EmailHeaders.MESSAGE_ID.toLowerCase() + ": "))
				{
					// length of 'message-id: ' == 12
					mid = header.substring(12);
					break;
				}
			}
		}

		// use the special extension that can set the id
		MimeMessage msg = new MyMessage(session, mid);

		// the FULL content-type header, for example:
		// Content-Type: text/plain; charset=windows-1252; format=flowed
		String contentTypeHeader = null;

		// If we need to force the container to use a certain multipart subtype
		//    e.g. 'alternative'
		// then sneak it through in the additionalHeaders
		String multipartSubtype = null;

		// set the additional headers on the message
		// but treat Content-Type specially as we need to check the charset
		// and we already dealt with the message id
		if (additionalHeaders != null)
		{
			for (String header : additionalHeaders)
			{
				if (header.toLowerCase().startsWith(EmailHeaders.CONTENT_TYPE.toLowerCase() + ": "))
					contentTypeHeader = header;
				else if (header.toLowerCase().startsWith(EmailHeaders.MULTIPART_SUBTYPE.toLowerCase() + ": "))
					multipartSubtype = header.substring(header.indexOf(":") + 1).trim();
				else if (!header.toLowerCase().startsWith(EmailHeaders.MESSAGE_ID.toLowerCase() + ": "))
					msg.addHeaderLine(header);
			}
		}

		// date
		if (msg.getHeader(EmailHeaders.DATE) == null)
			msg.setSentDate(new Date(System.currentTimeMillis()));

		// set the message sender
		msg.setFrom(from);

		// set the message recipients (headers)
		setRecipients(headerTo, msg);

		// set the reply to
		if ((replyTo != null) && (msg.getHeader(EmailHeaders.REPLY_TO) == null))
			msg.setReplyTo(replyTo);

		// update to be Postmaster if necessary
		checkFrom(msg);

		// figure out what charset encoding to use
		//
		// first try to use the charset from the forwarded
		// Content-Type header (if there is one).
		//
		// if that charset doesn't work, try a couple others.
		// the character set, for example, windows-1252 or UTF-8
		String charset = extractCharset(contentTypeHeader);

		if (charset != null && canUseCharset(content, charset) && canUseCharset(subject, charset))
		{
			// use the charset from the Content-Type header
		}
		else if (canUseCharset(content, CharacterSet.ISO_8859_1) && canUseCharset(subject, CharacterSet.ISO_8859_1))
		{
			if (contentTypeHeader != null && charset != null)
				contentTypeHeader = contentTypeHeader.replaceAll(charset, CharacterSet.ISO_8859_1);
			else if (contentTypeHeader != null)
				contentTypeHeader += "; charset=" + CharacterSet.ISO_8859_1;
			charset = CharacterSet.ISO_8859_1;
		}
		else if (canUseCharset(content, CharacterSet.WINDOWS_1252) && canUseCharset(subject, CharacterSet.WINDOWS_1252))
		{
			if (contentTypeHeader != null && charset != null)
				contentTypeHeader = contentTypeHeader.replaceAll(charset, CharacterSet.WINDOWS_1252);
			else if (contentTypeHeader != null)
				contentTypeHeader += "; charset=" + CharacterSet.WINDOWS_1252;
			charset = CharacterSet.WINDOWS_1252;
		}
		else
		{
			// catch-all - UTF-8 should be able to handle anything
			if (contentTypeHeader != null && charset != null) 
				contentTypeHeader = contentTypeHeader.replaceAll(charset, CharacterSet.UTF_8);
			else if (contentTypeHeader != null)
				contentTypeHeader += "; charset=" + CharacterSet.UTF_8;
			else
				contentTypeHeader = EmailHeaders.CONTENT_TYPE + ": "
						+ ContentType.TEXT_PLAIN + "; charset="
						+ CharacterSet.UTF_8;
			charset = CharacterSet.UTF_8;
		}

		if ((subject != null) && (msg.getHeader(EmailHeaders.SUBJECT) == null))
			msg.setSubject(subject, charset);

		// extract just the content type value from the header
		String contentType = null;
		if (contentTypeHeader != null)
		{
			int colonPos = contentTypeHeader.indexOf(":");
			contentType = contentTypeHeader.substring(colonPos + 1).trim();
		}
		setContent(content, attachments, msg, contentType, charset, multipartSubtype);

		// if we have a full Content-Type header, set it NOW
		// (after setting the body of the message so that format=flowed is preserved)
		// if there attachments, the messsage type will default to multipart/mixed and should
		// stay that way.
		if ((attachments == null || attachments.size() == 0) && contentTypeHeader != null)
		{
			msg.addHeaderLine(contentTypeHeader);
			msg.addHeaderLine(EmailHeaders.CONTENT_TRANSFER_ENCODING + ": quoted-printable");
		}

		if (log.isDebugEnabled()) {
			log.debug("HeaderLines received were: ");
			Enumeration<String> allHeaders = msg.getAllHeaderLines();
			while(allHeaders.hasMoreElements()) {
				log.debug((String)allHeaders.nextElement());
			}
		}

		sendMessageAndLog(to, start, msg, session);
			}



	/**
	 * fix up From and ReplyTo if we need it to be from Postmaster
	 */
	private void checkFrom(MimeMessage msg) {

	    String sendFromSakai = serverConfigurationService.getString(MAIL_SENDFROMSAKAI, "true");
	    String sendExceptions = serverConfigurationService.getString(MAIL_SENDFROMSAKAI_EXCEPTIONS, null);
	    InternetAddress from = null;
	    InternetAddress[] replyTo = null;

	    try {
		Address[] fromA = msg.getFrom();
		if (fromA == null || fromA.length == 0) {
		    log.info("message from missing");
		    return;
		} else if (fromA.length > 1) {
		    log.info("message from more than 1");
		    return;
		} else if (fromA instanceof InternetAddress[]) {
		    from = (InternetAddress) fromA[0];
		} else {
		    log.info("message from not InternetAddress");
		    return;
		}
		
		Address[] replyToA = msg.getReplyTo();
		if (replyToA == null)
		    replyTo = null;
		else if (replyToA instanceof InternetAddress[])
		    replyTo = (InternetAddress[]) replyToA;
		else {
		    log.info("message replyto isn't internet address");
		    return;
		}
		
		// should we replace from address with a Sakai address?
		if (sendFromSakai != null && !sendFromSakai.equalsIgnoreCase("false")) {
		    // exceptions -- addresses to leave alone. Our own addresses are always exceptions.
		    // you can also configure a regexp of exceptions.
		    if (!from.getAddress().toLowerCase().endsWith("@" + serverConfigurationService.getServerName().toLowerCase()) && 
			(sendExceptions == null || sendExceptions.equals("") || !from.getAddress().toLowerCase().matches(sendExceptions))) {
			
			// not an exception. do the replacement.
			// First, decide on the replacement address. The config variable
			// may be the replacement address. If not, use postmaster
			if (sendFromSakai.indexOf("@") < 0)
			    sendFromSakai = POSTMASTER + "@" + serverConfigurationService.getServerName();
		    
			// put the original from into reply-to, unless a replyto exists
			if (replyTo == null || replyTo.length == 0 || replyTo[0].getAddress().equals("")) {
			    replyTo = new InternetAddress[1];
			    replyTo[0] = from;
			    msg.setReplyTo(replyTo);
			}
			// for some reason setReplyTo doesn't work, though setFrom does. Have to create the
			// actual header line
			if (msg.getHeader(EmailHeaders.REPLY_TO) == null)
			    msg.addHeader(EmailHeaders.REPLY_TO, from.getAddress());
			
			// and use the new from address
			// biggest issue is the "personal address", i.e. the comment text

			String origFromText = from.getPersonal();
			String origFromAddress = from.getAddress();
			String fromTextPattern = serverConfigurationService.getString(MAIL_SENDFROMSAKAI_FROMTEXT, "{}");

			String fromText = null;
			if (origFromText != null && !origFromText.equals(""))
			    fromText = fromTextPattern.replace("{}", origFromText + " (" + origFromAddress + ")");
			else
			    fromText = fromTextPattern.replace("{}", origFromAddress);
		    
			from = new InternetAddress(sendFromSakai);
			try {
			    from.setPersonal(fromText);
			} catch (Exception e) {}
			
			msg.setFrom(from);
		    }
		}
	    } catch (javax.mail.internet.AddressException e) {
		log.info("checkfrom address exception " + e);
	    } catch (javax.mail.MessagingException e) {
		log.info("checkfrom messaging exception " + e);
	    }

	}


	/**
	 * {@inheritDoc}
	 */
	public void send(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List<String> additionalHeaders)
	{
		// if in test mode, use the test method
		if (m_testMode)
		{
			testSend(fromStr, toStr, subject, content, headerToStr, replyToStr, additionalHeaders);
			return;
		}

		// email should not be sent if from/to/content is empty or null
		if (StringUtils.isBlank(fromStr))
		{
			log.warn("send: null/empty fromStr");
			return;
		}

		if (StringUtils.isBlank(toStr))
		{
			log.warn("send: null/empty toStr");
			return;
		}

		if (StringUtils.isBlank(content))
		{
			log.warn("send: null/empty content");
			return;
		}

		try
		{
			InternetAddress from = new InternetAddress(fromStr);

			InternetAddress[] to = InternetAddress.parse(toStr);

			InternetAddress[] headerTo = null;
			if (StringUtils.isNotBlank(headerToStr)) {
				headerTo = InternetAddress.parse(headerToStr);
			} else {
				headerTo = InternetAddress.parse(toStr);
			}

			InternetAddress[] replyTo = null;
			if (StringUtils.isNotBlank(replyToStr)) {
				replyTo = InternetAddress.parse(replyToStr);
			}

			sendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);
		}
		catch (AddressException e)
		{
			log.warn("send: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendToUsers(Collection<User> users, Collection<String> headers, String message)
	{
		if (headers == null)
		{
			log.warn("sendToUsers: null headers");
			return;
		}

		if (m_testMode)
		{
			log.info("sendToUsers: users: " + usersToStr(users) + " headers: " + listToStr(headers) + " message:\n" + message);
			return;
		}

		if (m_smtp == null)
		{
			log.warn("sendToUsers: smtp not set");
			return;
		}

		if (users == null)
		{
			log.warn("sendToUsers: null users");
			return;
		}

		if (message == null)
		{
			log.warn("sendToUsers: null message");
			return;
		}

		// form the list of to: addresses from the users users collection
		ArrayList<InternetAddress> addresses = new ArrayList<InternetAddress>();
		for (User user : users)
		{
			String email = user.getEmail();
			if ((email != null) && (email.length() > 0))
			{
				try
				{
					addresses.add(new InternetAddress(email));
				}
				catch (AddressException e)
				{
					if (log.isDebugEnabled()) log.debug("sendToUsers: " + e);
				}
			}
		}

		// if we have none
		if (addresses.isEmpty()) return;

		// get separate sets
		List<Address[]> messageSets = getMessageSets(addresses);
		
		// get a session for our smtp setup, include host, port, reverse-path, and set partial delivery
		Properties props = createMailSessionProperties();

		Session session = Session.getInstance(props);

		// form our Message
		MimeMessage msg = new MyMessage(session, headers, message);

		// fix From and ReplyTo if necessary
		checkFrom(msg);

		// transport the message
		transportMessage(session, messageSets, headers, msg);
	}

	private List<Address[]> getMessageSets(List<InternetAddress> addresses) {
		// how many separate messages do we need to send to keep each one at or under m_maxRecipients?
		int numMessageSets = ((addresses.size() - 1) / m_maxRecipients) + 1;

		// make an array for each and store them all in the collection
		ArrayList<Address[]> messageSets = new ArrayList<Address[]>();
		int posInAddresses = 0;
		for (int i = 0; i < numMessageSets; i++)
		{
			// all but the last one are max size
			int thisSize = m_maxRecipients;
			if (i == numMessageSets - 1)
			{
				thisSize = addresses.size() - ((numMessageSets - 1) * m_maxRecipients);
			}

			// size an array
			Address[] toAddresses = new Address[thisSize];
			messageSets.add(toAddresses);

			// fill the array
			int posInToAddresses = 0;
			while (posInToAddresses < thisSize)
			{
				toAddresses[posInToAddresses] = (Address) addresses.get(posInAddresses);
				posInToAddresses++;
				posInAddresses++;
			}
		}
		return messageSets;
	}
	
	private void transportMessage(Session session, List<Address[]> messageSets, Collection<String> headers, MimeMessage msg) {
		// transport the message
		long time1 = 0;
		long time2 = 0;
		long time3 = 0;
		long time4 = 0;
		long time5 = 0;
		long time6 = 0;
		long timeExtraConnect = 0;
		long timeExtraClose = 0;
		long timeTmp = 0;
		int numConnects = 1;
		try
		{
			if (log.isDebugEnabled()) time1 = System.currentTimeMillis();
			Transport transport = session.getTransport(protocol);

			if (log.isDebugEnabled()) time2 = System.currentTimeMillis();
			msg.saveChanges();

			if (log.isDebugEnabled()) time3 = System.currentTimeMillis();
			if(m_smtpUser != null && m_smtpPassword != null)
				transport.connect(m_smtp,m_smtpUser,m_smtpPassword);
			else
				transport.connect();

			if (log.isDebugEnabled()) time4 = System.currentTimeMillis();

			// loop the send for each message set
			for (Iterator<Address[]> i = messageSets.iterator(); i.hasNext();)
			{
				Address[] toAddresses = i.next();

				try
				{
					transport.sendMessage(msg, toAddresses);

					// if we need to use the connection for just one send, and we have more, close and re-open
					if ((m_oneMessagePerConnection) && (i.hasNext()))
					{
						if (log.isDebugEnabled()) timeTmp = System.currentTimeMillis();
						transport.close();
						if (log.isDebugEnabled()) timeExtraClose += (System.currentTimeMillis() - timeTmp);

						if (log.isDebugEnabled()) timeTmp = System.currentTimeMillis();
						transport.connect();
						if (log.isDebugEnabled())
						{
							timeExtraConnect += (System.currentTimeMillis() - timeTmp);
							numConnects++;
						}
					}
				}
				catch (SendFailedException e)
				{
					if (log.isDebugEnabled()) log.debug("transportMessage: " + e);
				}
				catch (MessagingException e)
				{
					log.warn("transportMessage: " + e);
				}
			}

			if (log.isDebugEnabled()) time5 = System.currentTimeMillis();
			transport.close();

			if (log.isDebugEnabled()) time6 = System.currentTimeMillis();
		}
		catch (MessagingException e)
		{
			log.warn("transportMessage:" + e);
		}

		// log
		if (log.isInfoEnabled())
		{
			StringBuilder buf = new StringBuilder();
			buf.append("transportMessage: headers[");
			for (String header : headers)
			{
				buf.append(" ");
				buf.append(cleanUp(header));
			}
			buf.append("]");
			for (Address[] toAddresses : messageSets)
			{
				buf.append(" to[ ");
				for (int a = 0; a < toAddresses.length; a++)
				{
					buf.append(" ");
					buf.append(toAddresses[a]);
				}
				buf.append("]");
			}

			if (log.isDebugEnabled())
			{
				buf.append(" times[ ");
				buf.append(" getransport:" + (time2 - time1) + " savechanges:" + (time3 - time2) + " connect(#" + numConnects + "):"
						+ ((time4 - time3) + timeExtraConnect) + " send:" + (((time5 - time4) - timeExtraConnect) - timeExtraClose)
						+ " close:" + ((time6 - time5) + timeExtraClose) + " total: " + (time6 - time1) + " ]");
			}

			log.info(buf.toString());
		}
	}

	private Properties createMailSessionProperties()
	{
		Properties props = new Properties();

		props.put(propName(MAIL_HOST_T), m_smtp);
		// Set localhost name
		if (m_smtpLocalhost != null)
			props.put(propName(MAIL_LOCALHOST_T), m_smtpLocalhost);
		if (m_smtpPort != null) props.put(propName(MAIL_PORT_T), m_smtpPort);
		props.put(propName(MAIL_FROM_T), m_smtpFrom);
		props.put(propName(MAIL_SENDPARTIAL_T), Boolean.valueOf(m_sendPartial));
		if (m_smtpConnectionTimeout != null) props.put(propName(MAIL_CONNECTIONTIMEOUT_T), m_smtpConnectionTimeout);
		if (m_smtpTimeout != null) props.put(propName(MAIL_TIMEOUT_T), m_smtpTimeout);

		// smtpUser and smtpPassword are set, so assume mail.smtp.auth
		if(m_smtpUser != null && m_smtpPassword != null)
			props.put(propName(MAIL_AUTH_T), Boolean.TRUE.toString());

		if(m_smtpUseTLS)
			props.put(propName(MAIL_TLS_ENABLE_T), Boolean.TRUE.toString());

		if (m_smtpUseSSL)
		{
			props.put(propName(MAIL_SOCKET_FACTORY_CLASS_T), SSL_FACTORY);
			props.put(propName(MAIL_SOCKET_FACTORY_FALLBACK_T), Boolean.FALSE.toString());
		}

		if (m_smtpDebug)
			props.put(MAIL_DEBUG, Boolean.TRUE.toString());

		return props;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.email.api.EmailService#send(EmailMessage)
	 * For temporary backward compatibility
	 */
	public List<EmailAddress> send(EmailMessage msg) throws AddressValidationException,
	NoRecipientsException
	{
		List<EmailAddress> addresses = new ArrayList<EmailAddress>();
		try {
			addresses = send(msg,true);
		}
		catch (MessagingException e) {
			log.error("Email.sendMail: exception: " + e.getMessage(), e);
		}
		return addresses;
	}
	/**
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.email.api.EmailService#send(EmailMessage)
	 */
	public List<EmailAddress> send(EmailMessage msg, boolean messagingException) throws AddressValidationException,
			NoRecipientsException, MessagingException
	{
		ArrayList<EmailAddress> invalids = new ArrayList<EmailAddress>();

		InternetAddress from = null;
		// convert and validate the 'from' address
		try
		{
			from = new InternetAddress(msg.getFrom().getAddress(), true);
			from.setPersonal(msg.getFrom().getPersonal());
		}
		catch (AddressException e)
		{
			throw new AddressValidationException("Invalid 'FROM' address: "
					+ msg.getFrom().getAddress(), msg.getFrom());
		}
		catch (UnsupportedEncodingException e)
		{
			throw new AddressValidationException("Invalid 'FROM' address: "
					+ msg.getFrom().getAddress(), msg.getFrom());
		}
		
		// convert and validate reply to addresses
		InternetAddress[] replyTo = emails2Internets(msg.getReplyTo(), invalids);
		if (!invalids.isEmpty())
		{
			throw new AddressValidationException("Invalid 'REPLY TO' address", invalids);
		}
		
		/*
		 * LOOK - IF THERE ARE ANY INVALID RECIPIENT, AN EXCEPTION IS THROWN AND THE METHOD EXITS
		 */
		// convert and validate the 'to' addresses
		InternetAddress[] to = emails2Internets(msg.getRecipients(RecipientType.TO), invalids);

		// convert and validate 'cc' addresses
		InternetAddress[] cc = emails2Internets(msg.getRecipients(RecipientType.CC), invalids);

		// convert and validate 'bcc' addresses
		InternetAddress[] bcc = emails2Internets(msg.getRecipients(RecipientType.BCC), invalids);

		// convert and validate actual email addresses
		InternetAddress[] actual = emails2Internets(msg.getRecipients(RecipientType.ACTUAL),
				invalids);

		// check that some actual addresses were given. if not, use a compilation of to, cc, bcc
		if (actual.length == 0)
		{
			int total = to.length + cc.length + bcc.length;
			if (total == 0)
			{
				throw new NoRecipientsException("No valid recipients found on message.  Check for invalid email addresses returned from this method.");
			}

			actual = new InternetAddress[total];
			int count = 0;
			for (InternetAddress t : to)
			{
				actual[count++] = t;
			}
			for (InternetAddress c : cc)
			{
				actual[count++] = c;
			}
			for (InternetAddress b : bcc)
			{
				actual[count++] = b;
			}
		}

		// rebundle addresses to expected param type
		HashMap<RecipientType, InternetAddress[]> headerTo = new HashMap<RecipientType, InternetAddress[]>();
		headerTo.put(RecipientType.TO, to);
		headerTo.put(RecipientType.CC, cc);
		headerTo.put(RecipientType.BCC, bcc);

		// convert headers to expected format
		List<String> headers = msg.extractHeaders();

		// build the content type
		String contentType = EmailHeaders.CONTENT_TYPE + ": " + msg.getContentType();
		if (msg.getCharacterSet() != null && msg.getCharacterSet().trim().length() != 0)
		{
			contentType += "; charset=" + msg.getCharacterSet();
		}
		// message format is only used when content type is text/plain as specified in the rfc
		if (ContentType.TEXT_PLAIN.equals(msg.getCharacterSet()) && msg.getFormat() != null
				&& msg.getFormat().trim().length() != 0)
		{
			contentType += "; format=" + msg.getFormat();
		}
		// add the content type to the headers
		headers.add(contentType);

		// send the message
		try {
			sendMailMessagingException(from, actual, msg.getSubject(),
					msg.getBody(), headerTo, replyTo, headers,
					msg.getAttachments());
		} catch (MessagingException e) {
			// Just log it, if user doesn't want it thrown
			if (messagingException == false) {
				log.error("Email.sendMail: exception: " + e.getMessage(), e);
			} else {
				throw e;
			}
		}
		return invalids;
	}


	/**
	 * Converts a {@link java.util.List} of {@link EmailAddress} to
	 * {@link javax.mail.internet.InternetAddress}.
	 *
	 * @param emails
	 * @return Array will be the same size as the list with converted addresses. If list is null,
	 *         the array returned will be 0 length (non-null).
	 * @throws AddressException
	 * @throws UnsupportedEncodingException
	 */
	protected InternetAddress[] emails2Internets(List<EmailAddress> emails,
			List<EmailAddress> invalids)
	{
		// set the default return value
		InternetAddress[] addrs = new InternetAddress[0];

		if (emails != null && !emails.isEmpty())
		{
			ArrayList<InternetAddress> laddrs = new ArrayList<InternetAddress>();
			for (int i = 0; i < emails.size(); i++)
			{
				EmailAddress email = emails.get(i);
				try
				{
					InternetAddress ia = new InternetAddress(email.getAddress(), true);
					ia.setPersonal(email.getPersonal());
					laddrs.add(ia);
				}
				catch (AddressException e)
				{
					invalids.add(email);
				}
				catch (UnsupportedEncodingException e)
				{
					invalids.add(email);
				}
			}
			if (!laddrs.isEmpty())
			{
				addrs = laddrs.toArray(addrs);
			}
		}

		return addrs;
	}
	
	protected String cleanUp(String str)
	{
		StringBuilder buf = new StringBuilder(str);
		for (int i = 0; i < buf.length(); i++)
		{
			if (buf.charAt(i) == '\n' || buf.charAt(i) == '\r') buf.replace(i, i + 1, " ");
		}

		return buf.toString();
	}

	protected String listToStr(Collection list)
	{
		if (list == null) return "";
		return arrayToStr(list.toArray());
	}

	protected String arrayToStr(Object[] array)
	{
		StringBuilder buf = new StringBuilder();
		if (array != null)
		{
			buf.append("[");
			for (int i = 0; i < array.length; i++)
			{
				if (i != 0) buf.append(", ");
				buf.append(array[i].toString());
			}
			buf.append("]");
		}
		else
		{
			buf.append("");
		}

		return buf.toString();
	}

	/**
	 * Flatten a {@link java.util.Map} to a String
	 * 
	 * @param map
	 * @return A string representation of the {@link java.util.Map}.  Examples of results include:
	 *         Standard key/value pairs: [[key1:value1], [key2:value2]]
	 *         List values: [[key1:value1, value2], [key2:value3, value4]]
	 *         Map values: [[key1:[key2:value1]], [key3:[key4:value2]]]
	 */
	protected String mapToStr(Map map)
	{
		StringBuilder sb = new StringBuilder();
		if (map != null)
		{
			sb.append("[");
			for (Iterator i = map.entrySet().iterator(); i.hasNext(); )
			{
				Entry entry = (Entry) i.next();
				Object key = entry.getValue();
				sb.append("[").append(key).append(":");
				Object value = entry.getValue();
				if (value instanceof Collection)
				{
					sb.append(listToStr((Collection) value));
				}
				else if (value instanceof Object[])
				{
					sb.append(arrayToStr((Object[]) value));
				}
				else if (value instanceof Map)
				{
					sb.append(mapToStr((Map) value));
				}
				else
				{
					sb.append(value);
				}
				sb.append("]");

				if (i.hasNext())
					sb.append(", ");
			}
			sb.append("]");
		}
		return sb.toString();
	}

	protected String usersToStr(Collection<User> users)
	{
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		if (users != null)
		{
			for (User user : users)
			{
				buf.append(user.getDisplayName() + "<" + user.getEmail() + "> ");
			}
		}

		buf.append("]");

		return buf.toString();
	}

	/**
	 * Sets the content for a message. Also attaches files to the message.
	 * @throws MessagingException
	 */
	protected void setContent(String content, List<Attachment> attachments, MimeMessage msg,
			String contentType, String charset, String multipartSubtype) throws MessagingException
	{
		ArrayList<MimeBodyPart> embeddedAttachments = new ArrayList<MimeBodyPart>();
		if (attachments != null && attachments.size() > 0)
		{
			int maxAttachmentSize = serverConfigurationService.getInt(MAIL_SENDFROMSAKAI_MAXSIZE, 25000000);
			int attachmentRunningTotal = 0;

			// Add attachments to messages
			for (Attachment attachment : attachments)
			{
				// attach the file to the message
				MimeBodyPart mbp = createAttachmentPart(attachment);
				int mbpSize = mbp.getSize();
				if ( (attachmentRunningTotal + mbpSize) < maxAttachmentSize )
				{
					embeddedAttachments.add(mbp);
					attachmentRunningTotal = attachmentRunningTotal + mbpSize;
				}
				else
				{
					log.debug("Removed attachment from mail message because it was too large: " + mbpSize);
				}
			}
		}

		// if no direct attachments, keep the message simple and add the content as text.
		if (embeddedAttachments.size() == 0)
		{
			// if no contentType specified, go with text/plain
			if (contentType == null)
				msg.setText(content, charset);
			else
				msg.setContent(content, contentType);
		}
		// the multipart was constructed (ie. attachments available), use it as the message content
		else
		{
			// create a multipart container
			Multipart multipart = (multipartSubtype != null) ? new MimeMultipart(multipartSubtype) : new MimeMultipart();

			// create a body part for the message text
			MimeBodyPart msgBodyPart = new MimeBodyPart();
			if (contentType == null)
				msgBodyPart.setText(content, charset);
			else
				msgBodyPart.setContent(content, contentType);

			// add the message part to the container
			multipart.addBodyPart(msgBodyPart);

			// add attachments
			for (MimeBodyPart attachPart : embeddedAttachments)
			{
				multipart.addBodyPart(attachPart);
			}

			// set the multipart container as the content of the message
			msg.setContent(multipart);
		}
	}

	/**
	 * Attaches a file as a body part to the multipart message
	 *
	 * @param attachment
	 * @throws MessagingException
	 */
	private MimeBodyPart createAttachmentPart(Attachment attachment) throws MessagingException
	{
		DataSource source = attachment.getDataSource();
		MimeBodyPart attachPart = new MimeBodyPart();

		attachPart.setDataHandler(new DataHandler(source));
		attachPart.setFileName(attachment.getFilename());

		if (attachment.getContentTypeHeader() != null) {
			attachPart.setHeader("Content-Type", attachment.getContentTypeHeader());
		}

		if (attachment.getContentDispositionHeader() != null) {
			attachPart.setHeader("Content-Disposition", attachment.getContentDispositionHeader());
		}

		return attachPart;
	}

	/**
	 * test version of sendMail
	 */
	protected void testSendMail(InternetAddress from, InternetAddress[] to, String subject, String content,
			Map<RecipientType, InternetAddress[]> headerTo, InternetAddress[] replyTo, List<String> additionalHeaders, List<Attachment> attachments)
	{
		log.info("sendMail: from: {} to: {} subject: {} headerTo: {} replyTo: {} content: {} additionalHeaders: {}",
				   from, arrayToStr(to), subject, mapToStr(headerTo), arrayToStr(replyTo), content, listToStr(additionalHeaders));
		//If the attachments isn't empty do something with them
		if (CollectionUtils.isNotEmpty(attachments)){
			for (Attachment attachment:attachments) {
				//If it's text, we'll dump out the file contents, otherwise just the name
				String attachmentContent = "BINARY";
				if (attachment.getContentTypeHeader() != null && attachment.getContentTypeHeader().startsWith("text/")) {
					try {
						attachmentContent = IOUtils.toString(attachment.getDataSource().getInputStream(), "UTF-8"); 
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log.debug("sendMail: error accessing attachment content",e);
					}
				}

				log.info("sendMail: attachment name: {} type header: {} body:{}{}",
							attachment.getFilename(),attachment.getContentTypeHeader(),  System.lineSeparator(), attachmentContent);
			}
		}
	}

	/**
	 * test version of send
	 */
	protected void testSend(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List<String> additionalHeaders)
	{
		log.info("send: from: " + fromStr + " to: " + toStr + " subject: " + subject + " headerTo: " + headerToStr + " replyTo: "
				+ replyToStr + " content: " + content + " additionalHeaders: " + listToStr(additionalHeaders));
	}

	/** Returns true if the given content String can be encoded in the given charset */
	protected static boolean canUseCharset(String content, String charsetName)
	{
		try
		{
			return Charset.forName(charsetName).newEncoder().canEncode(content);
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected void sendMessageAndLog(InternetAddress[] to, long start, MimeMessage msg, Session session)
			throws MessagingException
	{
		long preSend = 0;
		if (log.isDebugEnabled()) preSend = System.currentTimeMillis();

		if (allowTransport)
		{
			msg.saveChanges();

			transportMessage(session, getMessageSets(new ArrayList<>(Arrays.asList(to))), new ArrayList<>(), msg);
		}

		long end = 0;
		if (log.isDebugEnabled()) end = System.currentTimeMillis();

		if (log.isInfoEnabled())
		{
			StringBuilder buf = new StringBuilder();
			buf.append("Email.sendMail:");
			appendAddresses(buf, msg.getFrom(), " from:");
			buf.append("subject: ");
			buf.append(msg.getSubject());
			appendAddresses(buf, to, " to:");
			appendAddresses(buf, msg.getRecipients(Message.RecipientType.TO), " headerTo{to}:");
			appendAddresses(buf, msg.getRecipients(Message.RecipientType.CC), " headerTo{cc}:");
			appendAddresses(buf, msg.getRecipients(Message.RecipientType.BCC), " headerTo{bcc}:");
			appendAddresses(buf, msg.getReplyTo(), " replyTo:");

			try
			{
				if (msg.getContent() instanceof Multipart)
				{
					Multipart parts = (Multipart) msg.getContent();
					buf.append(" with ").append(parts.getCount() - 1).append(" attachments");
				}
			}
			catch (IOException ioe) {}

			if (log.isDebugEnabled())
			{
				buf.append(" time: ");
				buf.append("" + (end - start));
				buf.append(" in send: ");
				buf.append("" + (end - preSend));
			}

			log.info(buf.toString());
		}
	}


	/**
	 * Utility method to append addresses to a StringBuilder.
	 * @param buffer The string builder to append to.
	 * @param addresses The addresses to append.
	 * @param label The label for these addresses.
	 */
	private void appendAddresses(StringBuilder buffer, Address[] addresses, String label) {
		if (addresses != null)
		{
			buffer.append(label);
			for (Address address : addresses)
			{
				buffer.append(" ");
				buffer.append(toEmail(address));
			}
		}
	}

	/**
	 * @param address The address, hopefully an {@link InternetAddress}
	 * @return The email address if it can, otherwise the whole address.
	 */
	private String toEmail(Address address)
	{
		if (address instanceof InternetAddress)
		{
			return ((InternetAddress) address).getAddress();
		}
		else
		{
			return address.toString();
		}
	}


	protected void setRecipients(Map<RecipientType, InternetAddress[]> headerTo, MimeMessage msg)
			throws MessagingException
	{
		if (headerTo != null)
		{
			if (msg.getHeader(EmailHeaders.TO) == null && headerTo.containsKey(RecipientType.TO))
			{
				msg.setRecipients(Message.RecipientType.TO, headerTo.get(RecipientType.TO));
			}
			if (msg.getHeader(EmailHeaders.CC) == null && headerTo.containsKey(RecipientType.CC))
			{
				msg.setRecipients(Message.RecipientType.CC, headerTo.get(RecipientType.CC));
			}
			if (headerTo.containsKey(RecipientType.BCC))
			{
				msg.setRecipients(Message.RecipientType.BCC, headerTo.get(RecipientType.BCC));
			}
		}
	}

	protected String extractCharset(String contentType)
	{
		String charset = null;
		if (contentType != null)
		{
			// try and extract the charset from the Content-Type header
			int charsetStart = contentType.toLowerCase().indexOf("charset=");
			if (charsetStart != -1)
			{
				int charsetEnd = contentType.indexOf(";", charsetStart);
				if (charsetEnd == -1)
					charsetEnd = contentType.length();
				charset = contentType.substring(charsetStart + "charset=".length(), charsetEnd).trim();
			}
		}
		return charset;
	}

	/**
	 * inspired by http://java.sun.com/products/javamail/FAQ.html#msgid
	 * 
	 * From the FAQ<br>
	 * <p>Q: I set a particular value for the Message-ID header of my new message, but
	 * when I send this message that header is rewritten.</p>
	 * <p>A: A new value for the Message-ID field is
	 * set when the saveChanges method is called (usually implicitly when a message is sent),
	 * overwriting any value you set yourself. If you need to set your own Message-ID and have it
	 * retained, you will have to create your own MimeMessage subclass, override the updateMessageID
	 * method and use an instance of this subclass.</p>
	 */
	protected class MyMessage extends MimeMessage
	{
		protected String m_id = null;

		public MyMessage(Session session, String id)
		{
			super(session);
			m_id = id;
		}

		public MyMessage(Session session, Collection<String> headers, String message)
		{
			super(session);

			try
			{
				// the FULL content-type header, for example: Content-Type: text/plain; charset=windows-1252; format=flowed
				String contentType = null;

				// see if we have a message-id: in the headers, or content-type:, otherwise move the headers into the message
				if (headers != null)
				{
					for (String header : headers)
					{
						if (header.toLowerCase().startsWith("message-id: "))
						{
							m_id = header.substring(12);
						}
						else if (header.toLowerCase().startsWith("content-type: "))
						{
							contentType = header;
						}
						else if (header.toLowerCase().startsWith("from: ")) 
						{
							addEncodedHeader(header, "From: ");
						}
						else if (header.toLowerCase().startsWith("to: "))
						{
							addEncodedHeader(header, "To: ");
						}
						else if (header.toLowerCase().startsWith("cc: ")) 
						{
							addEncodedHeader(header, "Cc: ");
						}
						else
						{
							try
							{
								addHeaderLine(header);
							}
							catch (MessagingException e)
							{
								log.warn("Email.MyMessage: exception: " + e.getMessage(), e);
							}
						}
					}
				}

				// make sure we have a date, use now if needed
				if (getHeader("Date") == null)
				{
					setSentDate(new Date(System.currentTimeMillis()));
				}

				// figure out what charset encoding to use
				// the character set, for example, windows-1252 or UTF-8
				String charset = null;

				// first try to use the charset from the forwarded Content-Type header (if there is one).
				// if that charset doesn't work, try a couple others.
				if (contentType != null)
				{
					// try and extract the charset from the Content-Type header
					int charsetStart = contentType.toLowerCase().indexOf("charset=");
					if (charsetStart != -1)
					{
						int charsetEnd = contentType.indexOf(";", charsetStart);
						if (charsetEnd == -1) charsetEnd = contentType.length();
						charset = contentType.substring(charsetStart + "charset=".length(), charsetEnd).trim();
					}
				}

				if (charset != null && canUseCharset(message, charset) && canUseCharset(getSubject(), charset))
				{
					// use the charset from the Content-Type header
				}
				else if (canUseCharset(message, CharacterSet.ISO_8859_1) && canUseCharset(getSubject(), CharacterSet.ISO_8859_1))
				{
					if (contentType != null && charset != null)
						contentType = contentType.replaceAll(charset, CharacterSet.ISO_8859_1);
					else if (contentType != null)
						contentType += "; charset=" + CharacterSet.ISO_8859_1;
					charset = CharacterSet.ISO_8859_1;
				}
				else if (canUseCharset(message, CharacterSet.WINDOWS_1252) && canUseCharset(getSubject(), CharacterSet.WINDOWS_1252))
				{
					if (contentType != null && charset != null)
						contentType = contentType.replaceAll(charset, CharacterSet.WINDOWS_1252);
					else if (contentType != null)
						contentType += "; charset=" + CharacterSet.WINDOWS_1252;
					charset = CharacterSet.WINDOWS_1252;
				}
				else
				{
					// catch-all - UTF-8 should be able to handle anything
					if (contentType != null && charset != null) 
						contentType = contentType.replaceAll(charset, CharacterSet.UTF_8);
					else if (contentType != null)
						contentType += "; charset=" + CharacterSet.UTF_8;
					else
						contentType = EmailHeaders.CONTENT_TYPE + ": "
								+ ContentType.TEXT_PLAIN + "; charset="
								+ CharacterSet.UTF_8;
					charset = CharacterSet.UTF_8;
				}
				
				if (contentType != null && contentType.contains("multipart/")) {
					MimeMultipart multiPartContent = new MimeMultipart("alternative");
					int indexOfStartOfBoundary = contentType.indexOf("boundary=\"") + 10;
					String headerStartingWithBoundary = contentType.substring(indexOfStartOfBoundary);
					String boundary = headerStartingWithBoundary.substring(0, headerStartingWithBoundary.indexOf("\""));
					String[] parts = message.split("--" + boundary + "(--)?\n");
					// the zeroth part is the line about how this is a MIME message, so we won't use it
					for (int i = 1; i < parts.length - 1; i++) {
						String[] partLines = parts[i].split("\n");
						StringBuilder partText = new StringBuilder();
						for (int j = 1; j < partLines.length; j++) {
							partText.append(partLines[j] + "\n");
						}
						MimeBodyPart bodyPart = new MimeBodyPart();
						String mimeType = partLines[0].contains("text/html") ? "text/html" : "text/plain";
						mimeType += "; charset="+charset;
						bodyPart.setContent(partText.toString(), mimeType);
						multiPartContent.addBodyPart(bodyPart);
					}
					setContent(multiPartContent);
				} else {
					// fill in the body of the message
					setText(message, charset);
				}

				// make sure correct charset is used for subject
				if ( getSubject() != null ) 
					setSubject(getSubject(), charset); 

				// if we have a full Content-Type header, set it NOW (after setting the body of the message so that format=flowed is preserved)
				if (contentType != null  && !contentType.contains("multipart/"))
				{
					// addHeaderLine("Content-Transfer-Encoding: quoted-printable");
					addHeaderLine(contentType);
				}
			}
			catch (MessagingException e)
			{
				log.warn("Email.MyMessage: exception: " + e.getMessage(), e);
			}
		}

		protected void updateHeaders() throws MessagingException
		{
			super.updateHeaders();
			if (m_id != null)
			{
				setHeader("Message-Id", m_id);
			}
		}

		/** Encode (To,From,Cc) mail headers to safely include UTF-8 characters
		 **/
		private void addEncodedHeader(String header, String name) throws MessagingException 
		{
			 try 
			 {
				  final String value = header.substring(name.length());
				  
				  // check for header format that may include UTF-8 characters
				  int index = value.lastIndexOf("<");
				  if (index == -1) 
				  {
						addHeaderLine(header);
				  } 
				  
				  // UTF-8 characters may exists -- encode header string
				  else 
				  {
						if ((index != 0) && (' ' == value.charAt(index - 1))) 
						{
							 index--;
						}
						
						final String title = value.substring(0, index);
						final String email = value.substring(index);
						
						// Accomodate Section 2.1.1 of http://tools.ietf.org/html/rfc2822 (line length should not exceed 78 characters and must not exceed 998)
						String tempTitle = MimeUtility.encodeText(title, "UTF-8", null);
						if ( name.length() + tempTitle.length() + email.length() > 78 )
							tempTitle = tempTitle.replace(" ", "\n ");
						
						final String[] lines = 
							(name + tempTitle + email).split("\r\n|\r|\n");
						for (String temp: lines) 
						{
							 addHeaderLine(temp);
						}
				  }
			 } 
			 catch (MessagingException e) 
			 {
				  log.error("Email.MyMessage: exception: " + e, e);
				  addHeaderLine(header);
			 } 
			 catch (UnsupportedEncodingException e)
			 {
				  log.error("Email.MyMessage: exception: " + e, e);
				  addHeaderLine(header);
			 }
		} 
	}

	public String propName(String propNameTemplate)
	{
		return propName(propNameTemplate, PROTOCOL_SMTP);
	}

	public String propName(String propNameTemplate, String protocol)
	{
		String formattedName = String.format(propNameTemplate, protocol);
		return formattedName;
	}
}
