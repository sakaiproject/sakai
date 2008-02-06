/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.email.impl;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
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
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * BasicEmailService implements the EmailService.
 * </p>
 */
public abstract class BasicEmailService implements EmailService
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

	protected static final String CONTENT_TYPE = "text/plain";

	/** Protocol name for smtp. */
	protected static final String SMTP_PROTOCOL = "smtp";

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies Note: keep these in sync with the TestEmailService, to make switching between them easier -ggolden
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

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
			m_smtpFrom = POSTMASTER + "@" + serverConfigurationService().getServerName();
		}

		// promote these to the system properties, to keep others (James) from messing with them
		if (m_smtp != null) System.setProperty(SMTP_HOST, m_smtp);
		if (m_smtpPort != null) System.setProperty(SMTP_PORT, m_smtpPort);
		System.setProperty(SMTP_FROM, m_smtpFrom);

		M_log.info("init(): smtp: " + m_smtp + ((m_smtpPort != null) ? (":" + m_smtpPort) : "") + " bounces to: " + m_smtpFrom
				+ " maxRecipients: " + m_maxRecipients + " testMode: " + m_testMode);
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
			InternetAddress[] replyTo, List additionalHeaders)
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
		{
			props.put(SMTP_PORT, m_smtpPort);
		}

		// set the mail envelope return address
		props.put(SMTP_FROM, m_smtpFrom);

		Session session = Session.getDefaultInstance(props, null);

		try
		{
			// see if we have a message-id in the additional headers
			String mid = null;
			if (additionalHeaders != null)
			{
				Iterator i = additionalHeaders.iterator();
				while (i.hasNext())
				{
					String header = (String) i.next();
					if (header.toLowerCase().startsWith("message-id: "))
					{
						mid = header.substring(12);
					}
				}
			}

			// use the special extension that can set the id
			MimeMessage msg = new MyMessage(session, mid);

			// the FULL content-type header, for example:
			// Content-Type: text/plain; charset=windows-1252; format=flowed
			String contentType = null;

			// the character set, for example, windows-1252 or UTF-8
			String charset = null;

			// set the additional headers on the message
			// but treat Content-Type specially as we need to check the charset
			// and we already dealt with the message id
			if (additionalHeaders != null)
			{
				Iterator i = additionalHeaders.iterator();
				while (i.hasNext())
				{
					String header = (String) i.next();
					if (header.toLowerCase().startsWith("content-type: "))
					{
						contentType = header;
					}
					else if (!header.toLowerCase().startsWith("message-id: "))
					{
						msg.addHeaderLine(header);
					}
				}
			}

			// date
			if (msg.getHeader("Date") == null)
			{
				msg.setSentDate(new Date(System.currentTimeMillis()));
			}

			msg.setFrom(from);

			if (msg.getHeader("To") == null)
			{
				if (headerTo != null)
				{
					msg.setRecipients(Message.RecipientType.TO, headerTo);
				}
			}

			if ((replyTo != null) && (msg.getHeader("Reply-To") == null))
			{
				msg.setReplyTo(replyTo);
			}

			// figure out what charset encoding to use
			//
			// first try to use the charset from the forwarded
			// Content-Type header (if there is one).
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

			if (charset != null && canUseCharset(content, charset))
			{
				// use the charset from the Content-Type header
			}
			else if (canUseCharset(content, "ISO-8859-1"))
			{
				if (contentType != null && charset != null) contentType = contentType.replaceAll(charset, "ISO-8859-1");
				charset = "ISO-8859-1";
			}
			else if (canUseCharset(content, "windows-1252"))
			{
				if (contentType != null && charset != null) contentType = contentType.replaceAll(charset, "windows-1252");
				charset = "windows-1252";
			}
			else
			{
				// catch-all - UTF-8 should be able to handle anything
				if (contentType != null && charset != null) 
					contentType = contentType.replaceAll(charset, "UTF-8");
				else if (contentType != null)
					contentType += "; charset=UTF-8";
				charset = "UTF-8";
			}

			if ((subject != null) && (msg.getHeader("Subject") == null))
			{
				msg.setSubject(subject, charset);
			}

			// fill in the body of the message
			msg.setText(content, charset);

			// if we have a full Content-Type header, set it NOW
			// (after setting the body of the message so that format=flowed is preserved)
			if (contentType != null)
			{
				msg.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
				msg.addHeaderLine(contentType);
			}

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
					buf.append(" headerTo:");
					for (int i = 0; i < headerTo.length; i++)
					{
						buf.append(" ");
						buf.append(headerTo[i]);
					}
				}

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
		catch (MessagingException e)
		{
			M_log.warn("Email.sendMail: exception: " + e, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void send(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List additionalHeaders)
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
	public void sendToUsers(Collection users, Collection headers, String message)
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
		Vector addresses = new Vector();
		for (Iterator i = users.iterator(); i.hasNext();)
		{
			User user = (User) i.next();
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
		Collection messageSets = new Vector();
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
				toAddresses[posInToAddresses] = (Address) addresses.elementAt(posInAddresses);
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
			for (Iterator i = messageSets.iterator(); i.hasNext();)
			{
				Address[] toAddresses = (Address[]) i.next();

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
			for (Iterator i = headers.iterator(); i.hasNext();)
			{
				String header = (String) i.next();
				buf.append(" ");
				buf.append(cleanUp(header));
			}
			buf.append("]");
			for (Iterator i = messageSets.iterator(); i.hasNext();)
			{
				Address[] toAddresses = (Address[]) i.next();
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

	protected String usersToStr(Collection users)
	{
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		if (users != null)
		{
			for (Iterator i = users.iterator(); i.hasNext();)
			{
				User user = (User) i.next();
				buf.append(user.getDisplayName() + "<" + user.getEmail() + "> ");
			}
		}

		buf.append("]");

		return buf.toString();
	}

	/**
	 * test version of sendMail
	 */
	protected void testSendMail(InternetAddress from, InternetAddress[] to, String subject, String content,
			InternetAddress[] headerTo, InternetAddress[] replyTo, List additionalHeaders)
	{
		M_log.info("sendMail: from: " + from + " to: " + arrayToStr(to) + " subject: " + subject + " headerTo: "
				+ arrayToStr(headerTo) + " replyTo: " + arrayToStr(replyTo) + " content: " + content + " additionalHeaders: "
				+ listToStr(additionalHeaders));
	}

	/**
	 * test version of send
	 */
	protected void testSend(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List additionalHeaders)
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

	// inspired by http://java.sun.com/products/javamail/FAQ.html#msgid
	protected class MyMessage extends MimeMessage
	{
		protected String m_id = null;

		public MyMessage(Session session, String id)
		{
			super(session);
			m_id = id;
		}

		public MyMessage(Session session, Collection headers, String message)
		{
			super(session);

			try
			{
				// the FULL content-type header, for example: Content-Type: text/plain; charset=windows-1252; format=flowed
				String contentType = null;

				// see if we have a message-id: in the headers, or content-type:, otherwise move the headers into the message
				if (headers != null)
				{
					Iterator i = headers.iterator();
					while (i.hasNext())
					{
						String header = (String) i.next();

						if (header.toLowerCase().startsWith("message-id: "))
						{
							m_id = header.substring(12);
						}

						else if (header.toLowerCase().startsWith("content-type: "))
						{
							contentType = header;
						}

						else
						{
							try
							{
								addHeaderLine(header);
							}
							catch (MessagingException e)
							{
								M_log.warn("Email.MyMessage: exception: " + e, e);
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
				else if (canUseCharset(message, "ISO-8859-1"))
				{
					if (contentType != null && charset != null) contentType = contentType.replaceAll(charset, "ISO-8859-1");
					charset = "ISO-8859-1";
				}
				else if (canUseCharset(message, "windows-1252"))
				{
					if (contentType != null && charset != null) contentType = contentType.replaceAll(charset, "windows-1252");
					charset = "windows-1252";
				}
				else
				{
					// catch-all - UTF-8 should be able to handle anything
					if (contentType != null && charset != null) 
						contentType = contentType.replaceAll(charset, "UTF-8");
					else if (contentType != null)
						contentType += "; charset=UTF-8";
					else
						contentType = "Content-Type: text/plain; charset=UTF-8";
					charset = "UTF-8";
				}
				
				if (contentType.contains("multipart/")) {
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
				M_log.warn("Email.MyMessage: exception: " + e, e);
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
	}
}
