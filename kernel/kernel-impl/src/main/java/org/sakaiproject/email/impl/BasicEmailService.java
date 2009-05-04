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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.email.impl;

import java.io.UnsupportedEncodingException;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.CharacterSet;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailHeaders;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.RecipientType;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * BasicEmailService implements the EmailService.
 * </p>
 */
public class BasicEmailService implements EmailService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BasicEmailService.class);

	protected static final String POSTMASTER = "postmaster";

	/** As defined in the com.sun.mail.smtp part of javamail. */

	/** The SMTP server to connect to. */
	protected static final String SMTP_HOST = "mail.smtp.host";

	/** The SMTP server port to connect to, if the connect() method doesn't explicitly specify one. Defaults to 25. */
	protected static final String SMTP_PORT = "mail.smtp.port";

	/** Email address to use for SMTP MAIL command. This sets the envelope return address. Defaults to msg.getFrom() or InternetAddress.getLocalAddress(). NOTE: mail.smtp.user was previously used for this. */
	protected static final String SMTP_FROM = "mail.smtp.from";

	/**
	 * If set to true, and a message has some valid and some invalid addresses, send the message anyway, reporting the partial failure with a SendFailedException. If set to false (the default), the message is not sent to any of the recipients if there is
	 * an invalid recipient address.
	 */
	protected static final String SMTP_SENDPARTIAL = "mail.smtp.sendpartial";

	/** Socket connection timeout value in milliseconds. Default is infinite timeout. */
	protected static final String SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
	
	/** Socket I/O timeout value in milliseconds. Default is infinite timeout. */
	protected static final String SMTP_TIMEOUT = "mail.smtp.timeout";
	
	/**
	 * Hostname used in outgoing SMTP HELO commands.
	 */
	protected static final String SMTP_LOCALHOST = "mail.smtp.localhost";

	protected static final String CONTENT_TYPE = ContentType.TEXT_PLAIN;

	/** Protocol name for smtp. */
	protected static final String SMTP_PROTOCOL = "smtp";

	protected ServerConfigurationService serverConfigurationService;

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService)
	{
		this.serverConfigurationService = serverConfigurationService;
	}

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
		// if no m_mailfrom set, set to the postmaster
		if (m_smtpFrom == null)
		{
			m_smtpFrom = POSTMASTER + "@" + serverConfigurationService.getServerName();
		}
		// initialize smtp timeout values
		m_smtpConnectionTimeout = serverConfigurationService.getString(SMTP_CONNECTIONTIMEOUT, null);
		m_smtpTimeout = serverConfigurationService.getString(SMTP_TIMEOUT, null);

		// promote these to the system properties, to keep others (James) from messing with them
		if (m_smtp != null) System.setProperty(SMTP_HOST, m_smtp);
		if (m_smtpPort != null) System.setProperty(SMTP_PORT, m_smtpPort);
		System.setProperty(SMTP_FROM, m_smtpFrom);
		if (m_smtpConnectionTimeout != null) System.setProperty(SMTP_CONNECTIONTIMEOUT, m_smtpConnectionTimeout);
		if (m_smtpTimeout != null) System.setProperty(SMTP_TIMEOUT, m_smtpTimeout);

		M_log.info("init(): smtp: " + m_smtp + ((m_smtpPort != null) ? (":" + m_smtpPort) : "") + " bounces to: " + m_smtpFrom
				+ " maxRecipients: " + m_maxRecipients + " testMode: " + m_testMode
				+ ((m_smtpConnectionTimeout != null) ? (" smtpConnectionTimeout: " + m_smtpConnectionTimeout) : "")
				+ ((m_smtpTimeout != null) ? (" smtpTimeout: " + m_smtpTimeout) : ""));
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
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
			List<String> additionalHeaders, List<Attachment> attachments)
	{
		// some timing for debug
		long start = 0;
		if (M_log.isDebugEnabled()) start = System.currentTimeMillis();

		// if in test mode, use the test method
		if (m_testMode)
		{
			testSendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);
			return;
		}

		if (m_smtp == null)
		{
			M_log.warn("sendMail: smtp not set");
			return;
		}

		if (from == null)
		{
			M_log.warn("sendMail: null from");
			return;
		}

		if (to == null)
		{
			M_log.warn("sendMail: null to");
			return;
		}

		if (content == null)
		{
			M_log.warn("sendMail: null content");
			return;
		}

		Properties props = new Properties();

		// set the server host
		props.put(SMTP_HOST, m_smtp);

		// set the port, if specified
		if (m_smtpPort != null)
			props.put(SMTP_PORT, m_smtpPort);
		
		// set smtp connection timeout, if specified
		if (m_smtpConnectionTimeout != null)
		{
			props.put(SMTP_CONNECTIONTIMEOUT, m_smtpConnectionTimeout);
		}
		
		// set smtp socket I/O timeout, if specified
		if (m_smtpTimeout != null)
		{
			props.put(SMTP_TIMEOUT, m_smtpTimeout);
		}
		
		// Set localhost name
		if (m_smtpLocalhost != null)
			props.put(SMTP_LOCALHOST, m_smtpLocalhost);

		// set the mail envelope return address
		props.put(SMTP_FROM, m_smtpFrom);

		Session session = Session.getDefaultInstance(props, null);

		try
		{
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

			// set the additional headers on the message
			// but treat Content-Type specially as we need to check the charset
			// and we already dealt with the message id
			if (additionalHeaders != null)
			{
				for (String header : additionalHeaders)
				{
					if (header.toLowerCase().startsWith(EmailHeaders.CONTENT_TYPE.toLowerCase() + ": "))
						contentTypeHeader = header;
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

			// figure out what charset encoding to use
			//
			// first try to use the charset from the forwarded
			// Content-Type header (if there is one).
			//
			// if that charset doesn't work, try a couple others.
			// the character set, for example, windows-1252 or UTF-8
			String charset = extractCharset(contentTypeHeader);

			if (charset != null && canUseCharset(content, charset))
			{
				// use the charset from the Content-Type header
			}
			else if (canUseCharset(content, CharacterSet.ISO_8859_1))
			{
				if (contentTypeHeader != null && charset != null)
					contentTypeHeader = contentTypeHeader.replaceAll(charset, CharacterSet.ISO_8859_1);
				else if (contentTypeHeader != null)
					contentTypeHeader += "; charset=" + CharacterSet.ISO_8859_1;
				charset = CharacterSet.ISO_8859_1;
			}
			else if (canUseCharset(content, CharacterSet.WINDOWS_1252))
			{
				if (contentTypeHeader != null && charset != null)
					contentTypeHeader = contentTypeHeader.replaceAll(charset, CharacterSet.WINDOWS_1252);
				else if (contentTypeHeader != null)
					contentTypeHeader += "; charset=" + CharacterSet.WINDOWS_1252;
				charset = CharacterSet.ISO_8859_1;
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
			setContent(content, attachments, msg, contentType, charset);

			// if we have a full Content-Type header, set it NOW
			// (after setting the body of the message so that format=flowed is preserved)
			// if there attachments, the messsage type will default to multipart/mixed and should
			// stay that way.
			if ((attachments == null || attachments.size() == 0) && contentTypeHeader != null)
			{
				msg.addHeaderLine(contentTypeHeader);
				msg.addHeaderLine(EmailHeaders.CONTENT_TRANSFER_ENCODING + ": quoted-printable");
			}

			if (M_log.isDebugEnabled()) {
				M_log.debug("HeaderLines received were: ");
				Enumeration allHeaders = msg.getAllHeaderLines();
				while(allHeaders.hasMoreElements()) {
					M_log.debug((String)allHeaders.nextElement());
				}
			}
			
			sendMessageAndLog(from, to, subject, headerTo, start, msg);
		}
		catch (MessagingException e)
		{
			M_log.warn("Email.sendMail: exception: " + e.getMessage(), e);
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

		if (fromStr == null)
		{
			M_log.warn("send: null fromStr");
			return;
		}

		if (toStr == null)
		{
			M_log.warn("send: null toStr");
			return;
		}

		if (content == null)
		{
			M_log.warn("send: null content");
			return;
		}

		try
		{
			InternetAddress from = new InternetAddress(fromStr);

			StringTokenizer tokens = new StringTokenizer(toStr, ", ");
			InternetAddress[] to = new InternetAddress[tokens.countTokens()];

			int i = 0;
			while (tokens.hasMoreTokens())
			{
				String next = (String) tokens.nextToken();
				to[i] = new InternetAddress(next);

				i++;
			} // cycle through and collect all of the Internet addresses from the list.

			InternetAddress[] headerTo = null;
			if (headerToStr != null)
			{
				headerTo = new InternetAddress[1];
				headerTo[0] = new InternetAddress(headerToStr);
			}

			InternetAddress[] replyTo = null;
			if (replyToStr != null)
			{
				replyTo = new InternetAddress[1];
				replyTo[0] = new InternetAddress(replyToStr);
			}

			sendMail(from, to, subject, content, headerTo, replyTo, additionalHeaders);

		}
		catch (AddressException e)
		{
			M_log.warn("send: " + e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendToUsers(Collection<User> users, Collection<String> headers, String message)
	{
		if (headers == null)
		{
			M_log.warn("sendToUsers: null headers");
			return;
		}

		if (m_testMode)
		{
			M_log.info("sendToUsers: users: " + usersToStr(users) + " headers: " + listToStr(headers) + " message:\n" + message);
			return;
		}

		if (m_smtp == null)
		{
			M_log.warn("sendToUsers: smtp not set");
			return;
		}


		if (users == null)
		{
			M_log.warn("sendToUsers: null users");
			return;
		}

		if (message == null)
		{
			M_log.warn("sendToUsers: null message");
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
					if (M_log.isDebugEnabled()) M_log.debug("sendToUsers: " + e);
				}
			}
		}

		// if we have none
		if (addresses.isEmpty()) return;

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

		// get a session for our smtp setup, include host, port, reverse-path, and set partial delivery
		Properties props = new Properties();
		props.put(SMTP_HOST, m_smtp);
		if (m_smtpPort != null) props.put(SMTP_PORT, m_smtpPort);
		props.put(SMTP_FROM, m_smtpFrom);
		props.put(SMTP_SENDPARTIAL, "true");
		if (m_smtpConnectionTimeout != null) props.put(SMTP_CONNECTIONTIMEOUT, m_smtpConnectionTimeout);
		if (m_smtpTimeout != null) props.put(SMTP_TIMEOUT, m_smtpTimeout);
		Session session = Session.getInstance(props);

		// form our Message
		MimeMessage msg = new MyMessage(session, headers, message);

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
			if (M_log.isDebugEnabled()) time1 = System.currentTimeMillis();
			Transport transport = session.getTransport(SMTP_PROTOCOL);

			if (M_log.isDebugEnabled()) time2 = System.currentTimeMillis();
			msg.saveChanges();

			if (M_log.isDebugEnabled()) time3 = System.currentTimeMillis();
			transport.connect();

			if (M_log.isDebugEnabled()) time4 = System.currentTimeMillis();

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
						if (M_log.isDebugEnabled()) timeTmp = System.currentTimeMillis();
						transport.close();
						if (M_log.isDebugEnabled()) timeExtraClose += (System.currentTimeMillis() - timeTmp);

						if (M_log.isDebugEnabled()) timeTmp = System.currentTimeMillis();
						transport.connect();
						if (M_log.isDebugEnabled())
						{
							timeExtraConnect += (System.currentTimeMillis() - timeTmp);
							numConnects++;
						}
					}
				}
				catch (SendFailedException e)
				{
					if (M_log.isDebugEnabled()) M_log.debug("sendToUsers: " + e);
				}
				catch (MessagingException e)
				{
					M_log.warn("sendToUsers: " + e);
				}
			}

			if (M_log.isDebugEnabled()) time5 = System.currentTimeMillis();
			transport.close();

			if (M_log.isDebugEnabled()) time6 = System.currentTimeMillis();
		}
		catch (MessagingException e)
		{
			M_log.warn("sendToUsers:" + e);
		}

		// log
		if (M_log.isInfoEnabled())
		{
			StringBuilder buf = new StringBuilder();
			buf.append("sendToUsers: headers[");
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

			if (M_log.isDebugEnabled())
			{
				buf.append(" times[ ");
				buf.append(" getransport:" + (time2 - time1) + " savechanges:" + (time3 - time2) + " connect(#" + numConnects + "):"
						+ ((time4 - time3) + timeExtraConnect) + " send:" + (((time5 - time4) - timeExtraConnect) - timeExtraClose)
						+ " close:" + ((time6 - time5) + timeExtraClose) + " total: " + (time6 - time1) + " ]");
			}

			M_log.info(buf.toString());
		}
	}

	public void send(EmailMessage msg)
	{
		try
		{
			// convert EmailAddress things to InternetAddress things
			InternetAddress from = new InternetAddress(msg.getFrom().getAddress(), true);
			from.setPersonal(msg.getFrom().getPersonal());
			InternetAddress[] to = emails2Internets(msg.getRecipients(RecipientType.TO));
			InternetAddress[] cc = emails2Internets(msg.getRecipients(RecipientType.CC));
			InternetAddress[] bcc = emails2Internets(msg.getRecipients(RecipientType.BCC));
			InternetAddress[] actual = emails2Internets(msg.getRecipients(RecipientType.ACTUAL));
			InternetAddress[] replyTo = emails2Internets(msg.getReplyTo());

			// check that some actual addresses were given. if not, use a compilation of to, cc, bcc
			if (actual.length == 0)
			{
				actual = new InternetAddress[to.length + cc.length + bcc.length];
				int count = 0;
				for (InternetAddress t : to)
					actual[count++] = t;
				for (InternetAddress c : cc)
					actual[count++] = c;
				for (InternetAddress b : bcc)
					actual[count++] = b;
			}

			// rebundle addresses to expected param type
			HashMap<RecipientType, InternetAddress[]> headerTo = new HashMap<RecipientType, InternetAddress[]>();
			headerTo.put(RecipientType.TO, to);
			headerTo.put(RecipientType.CC, cc);
			headerTo.put(RecipientType.BCC, bcc);

			// convert headers to expected format
			List<String> headers = headerMap2List(msg.getHeaders());

			// build the content type
			String contentType = EmailHeaders.CONTENT_TYPE + ": " + msg.getContentType();
			if (msg.getCharacterSet() != null && msg.getCharacterSet().trim().length() != 0)
				contentType += "; charset=" + msg.getCharacterSet();
			// message format is only used when content type is text/plain as specified in the rfc
			if (ContentType.TEXT_PLAIN.equals(msg.getCharacterSet()) && msg.getFormat() != null
					&& msg.getFormat().trim().length() != 0)
				contentType += "; format=" + msg.getFormat();
			// add the content type to the headers
			headers.add(contentType);

			// send the message
			sendMail(from, actual, msg.getSubject(), msg.getBody(), headerTo, replyTo, headers, msg
					.getAttachments());
		}
		catch (AddressException ae)
		{
			M_log.warn("Email.send: exception: " + ae.getMessage(), ae);
		}
		catch (UnsupportedEncodingException uee)
		{
			M_log.warn("Email.send: exception: " + uee.getMessage(), uee);
		}
	}

	protected List<String> headerMap2List(Map<String, String> headers)
	{
		List<String> retval = null;
		if (headers != null && !headers.isEmpty())
		{
			retval = new ArrayList<String>();
			for (String key : headers.keySet())
			{
				String value = headers.get(key);
				retval.add(key + ": " + value);
			}
		}
		return retval;
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
	protected InternetAddress[] emails2Internets(List<EmailAddress> emails)
			throws AddressException, UnsupportedEncodingException
	{
		InternetAddress[] addrs = null;
		if (emails != null)
		{
			addrs = new InternetAddress[emails.size()];
			for (int i = 0; i < emails.size(); i++)
			{
				EmailAddress email = emails.get(i);
				addrs[i] = new InternetAddress(email.getAddress(), true);
				addrs[i].setPersonal(email.getPersonal());
			}
		}
		else
		{
			addrs = new InternetAddress[0];
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
			for (Iterator i = map.keySet().iterator(); i.hasNext(); )
			{
				Object key = i.next();
				sb.append("[").append(key).append(":");
				Object value = map.get(key);
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
	 * 
	 * @param content
	 * @param attachments
	 * @param msg
	 * @param charset
	 * @throws MessagingException
	 */
	protected void setContent(String content, List<Attachment> attachments, MimeMessage msg,
			String contentType, String charset) throws MessagingException
	{
		ArrayList<MimeBodyPart> embeddedAttachments = new ArrayList<MimeBodyPart>();
		if (attachments != null && attachments.size() > 0)
		{
			// Add attachments to messages
			for (Attachment attachment : attachments)
			{
				// attach the file to the message
				embeddedAttachments.add(createAttachmentPart(attachment));
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
			Multipart multipart = new MimeMultipart();

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
	 * @param multipart
	 * @param attachment
	 * @throws MessagingException
	 */
	private MimeBodyPart createAttachmentPart(Attachment attachment) throws MessagingException
	{
		MimeBodyPart attachPart = new MimeBodyPart();
		FileDataSource source = new FileDataSource(attachment.getFile());
		attachPart = new MimeBodyPart();
		attachPart.setDataHandler(new DataHandler(source));
		attachPart.setFileName(attachment.getFile().getPath());
		return attachPart;
	}

	/**
	 * test version of sendMail
	 */
	protected void testSendMail(InternetAddress from, InternetAddress[] to, String subject, String content,
			Map<RecipientType, InternetAddress[]> headerTo, InternetAddress[] replyTo, List<String> additionalHeaders)
	{
		M_log.info("sendMail: from: " + from + " to: " + arrayToStr(to) + " subject: " + subject + " headerTo: "
				+ mapToStr(headerTo) + " replyTo: " + arrayToStr(replyTo) + " content: " + content + " additionalHeaders: "
				+ listToStr(additionalHeaders));
	}

	/**
	 * test version of send
	 */
	protected void testSend(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List<String> additionalHeaders)
	{
		M_log.info("send: from: " + fromStr + " to: " + toStr + " subject: " + subject + " headerTo: " + headerToStr + " replyTo: "
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

	protected void sendMessageAndLog(InternetAddress from, InternetAddress[] to, String subject,
			Map<RecipientType, InternetAddress[]> headerTo, long start, MimeMessage msg)
			throws MessagingException
	{
		long preSend = 0;
		if (M_log.isDebugEnabled()) preSend = System.currentTimeMillis();

		Transport.send(msg, to);

		long end = 0;
		if (M_log.isDebugEnabled()) end = System.currentTimeMillis();

		if (M_log.isInfoEnabled())
		{
			StringBuilder buf = new StringBuilder();
			buf.append("Email.sendMail: from: ");
			buf.append(from);
			buf.append(" subject: ");
			buf.append(subject);
			buf.append(" to:");
			for (int i = 0; i < to.length; i++)
			{
				buf.append(" ");
				buf.append(to[i]);
			}
			if (headerTo != null)
			{
				if (headerTo.containsKey(RecipientType.TO))
				{
					buf.append(" headerTo{to}:");
					InternetAddress[] headerToTo = headerTo.get(RecipientType.TO);
					for (int i = 0; i < headerToTo.length; i++)
					{
						buf.append(" ");
						buf.append(headerToTo[i]);
					}
				}
				if (headerTo.containsKey(RecipientType.CC))
				{
					buf.append(" headerTo{cc}:");
					InternetAddress[] headerToCc = headerTo.get(RecipientType.CC);
					for (int i = 0; i < headerToCc.length; i++)
					{
						buf.append(" ");
						buf.append(headerToCc[i]);
					}
				}
				if (headerTo.containsKey(RecipientType.BCC))
				{
					buf.append(" headerTo{bcc}:");
					InternetAddress[] headerToBcc = headerTo.get(RecipientType.BCC);
					for (int i = 0; i < headerToBcc.length; i++)
					{
						buf.append(" ");
						buf.append(headerToBcc[i]);
					}
				}
			}
			try
			{
				if (msg.getContent() instanceof Multipart)
				{
					Multipart parts = (Multipart) msg.getContent();
					buf.append(" with ").append(parts.getCount() - 1).append(" attachments");
				}
			}
			catch (IOException ioe) {}

			if (M_log.isDebugEnabled())
			{
				buf.append(" time: ");
				buf.append("" + (end - start));
				buf.append(" in send: ");
				buf.append("" + (end - preSend));
			}

			M_log.info(buf.toString());
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
								M_log.warn("Email.MyMessage: exception: " + e.getMessage(), e);
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

				if (charset != null && canUseCharset(message, charset))
				{
					// use the charset from the Content-Type header
				}
				else if (canUseCharset(message, CharacterSet.ISO_8859_1))
				{
					if (contentType != null && charset != null)
						contentType = contentType.replaceAll(charset, CharacterSet.ISO_8859_1);
					else if (contentType != null)
						contentType += "; " + CharacterSet.ISO_8859_1;
					charset = CharacterSet.ISO_8859_1;
				}
				else if (canUseCharset(message, CharacterSet.WINDOWS_1252))
				{
					if (contentType != null && charset != null)
						contentType = contentType.replaceAll(charset, CharacterSet.WINDOWS_1252);
					else if (contentType != null)
						contentType += "; " + CharacterSet.WINDOWS_1252;
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
				
				if (contentType != null 
				        && contentType.contains("multipart/")) {
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
						mimeType += " ; charset="+charset;
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
				M_log.warn("Email.MyMessage: exception: " + e.getMessage(), e);
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
				  M_log.warn("Email.MyMessage: exception: " + e, e);
				  addHeaderLine(header);
			 } 
			 catch (UnsupportedEncodingException e)
			 {
				  M_log.warn("Email.MyMessage: exception: " + e, e);
				  addHeaderLine(header);
			 }
		} 
	}
}
