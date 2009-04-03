/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.james;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.net.URLEncoder;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.sakaiproject.alias.cover.AliasService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.cover.MailArchiveService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * <p>
 * SakaiMailet watches incoming mail (via James) and sends mail to the appropriate mail archive channel in Sakai.
 * </p>
 */
public class SakaiMailet extends GenericMailet
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("sakaimailet");

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SakaiMailet.class);

	/** The user name of the postmaster user - the one who posts incoming mail. */
	public static final String POSTMASTER = "postmaster";
	
	// used when parsing email header parts
	private static final String NAME_PREFIX = "name=";

	/**
	 * Called when created.
	 */
	public void init() throws MessagingException
	{
		M_log.info("init()");
	}

	/**
	 * Called when leaving.
	 */
	public void destroy()
	{
		M_log.info("destroy()");

		super.destroy();

	} // destroy

	/**
	 * Process incoming mail.
	 * 
	 * @param mail
	 *        ...
	 */
	public void service(Mail mail) throws MessagingException
	{
		// get the postmaster user
		User postmaster = null;
		try
		{
			postmaster = UserDirectoryService.getUser(POSTMASTER);
		}
		catch (UserNotDefinedException e)
		{
			M_log.warn("service: no postmaster");
			mail.setState(Mail.GHOST);
			return;
		}

		try
		{
			// set the current user to postmaster
			Session s = SessionManager.getCurrentSession();
			if (s != null)
			{
				s.setUserId(postmaster.getId());
			}
			else
			{
				M_log.warn("service - no SessionManager.getCurrentSession, cannot set to postmaser user");
			}

			MimeMessage msg = mail.getMessage();
			String id = msg.getMessageID();

			Address[] fromAddresses = msg.getFrom();
			String from = null;
			String fromAddr = null;
			if ((fromAddresses != null) && (fromAddresses.length == 1))
			{
				from = fromAddresses[0].toString();
				if (fromAddresses[0] instanceof InternetAddress)
				{
					fromAddr = ((InternetAddress) (fromAddresses[0])).getAddress();
				}
			}
			else
			{
				from = mail.getSender().toString();
				fromAddr = mail.getSender().toInternetAddress().getAddress();
			}

			Collection to = mail.getRecipients();

			Date sent = msg.getSentDate();

			String subject = StringUtil.trimToNull(msg.getSubject());
			if (subject == null) subject = "<no subject>";

			Enumeration headers = msg.getAllHeaderLines();
			List mailHeaders = new Vector();
			while (headers.hasMoreElements())
			{
				String line = (String) headers.nextElement();
				// check if string starts with "Content-Type", ignoring case
				if (line.regionMatches(true, 0, MailArchiveService.HEADER_CONTENT_TYPE, 0, MailArchiveService.HEADER_CONTENT_TYPE.length()))
				{
					String contentType = line.substring(0, MailArchiveService.HEADER_CONTENT_TYPE.length() );
					mailHeaders.add(line.replaceAll(contentType, MailArchiveService.HEADER_OUTER_CONTENT_TYPE));
				}
				mailHeaders.add(line);
			}

			M_log.debug(id + " : mail: from:" + from + " sent: " + TimeService.newTime(sent.getTime()).toStringLocalFull() + " subject: "
					+ subject);

			// process for each recipient
			Iterator it = to.iterator();
			while (it.hasNext())
			{
				String mailId = null;
				try
				{
					MailAddress recipient = (MailAddress) it.next();
					M_log.debug(id + " : checking to: " + recipient);

					// the recipient's mail id
					mailId = recipient.getUser();

					// eat the no-reply
					if ("no-reply".equalsIgnoreCase(mailId))
					{
						mail.setState(Mail.GHOST);
						continue;
					}

					// find the channel (mailbox) that this is adressed to
					// for now, check only for it being a site or alias to a site.
					// %%% - add user and other later -ggolden
					MailArchiveChannel channel = null;

					// first, assume the mailId is a site id
					String channelRef = MailArchiveService.channelReference(mailId, SiteService.MAIN_CONTAINER);
					try
					{
						channel = MailArchiveService.getMailArchiveChannel(channelRef);
					}
					catch (IdUnusedException goOn)
					{
					}

					// next, if not a site, see if it's an alias to a site or channel
					if (channel == null)
					{
						// if not an alias, it will throw the IdUnusedException caught below
						Reference ref = EntityManager.newReference(AliasService.getTarget(mailId));

						// if ref is a site
						if (ref.getType().equals(SiteService.APPLICATION_ID))
						{
							// now we have a site reference, try for it's channel
							channelRef = MailArchiveService.channelReference(ref.getId(), SiteService.MAIN_CONTAINER);
						}

						// if ref is a channel
						else if (ref.getType().equals(MailArchiveService.APPLICATION_ID))
						{
							// ref is a channel
							channelRef = ref.getReference();
						}

						// ref is something else
						else
						{
							M_log.info(id + " : mail rejected: unknown address: " + mailId);

							throw new IdUnusedException(mailId);
						}

						// if there's no channel for this site, it will throw the IdUnusedException caught below
						channel = MailArchiveService.getMailArchiveChannel(channelRef);
					}

					// skip disabled channels
					if (!channel.getEnabled())
					{
						if (from.startsWith(POSTMASTER))
						{
							mail.setState(Mail.GHOST);
						}
						else
						{
							String errMsg = rb.getString("err_email_off") + "\n\n";
							String mailSupport = StringUtil.trimToNull( ServerConfigurationService.getString("mail.support") );
							if ( mailSupport != null )
								errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";

							mail.setErrorMessage(errMsg);
						}

						M_log.info(id + " : mail rejected: channel not enabled: " + mailId);

						continue;
					}

					// for non-open channels, make sure the from is a member
					if (!channel.getOpen())
					{
						// see if our fromAddr is the email address of any of the users who are permitted to add messages to the channel.
						if (!fromValidUser(fromAddr, channel))
						{
							M_log.info(id + " : mail rejected: from: " + fromAddr + " not authorized for site: " + mailId);

							String errMsg = rb.getString("err_not_member") + "\n\n";
							String mailSupport = StringUtil.trimToNull( ServerConfigurationService.getString("mail.support") );
							if ( mailSupport != null )
								errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
							mail.setErrorMessage(errMsg);
							continue;
						}
					}

					// prepare the message 
					StringBuilder bodyBuf[] = new StringBuilder[2];
					bodyBuf[0] = new StringBuilder();
					bodyBuf[1] = new StringBuilder();
					List attachments = attachments = EntityManager.newReferenceList();
					
					try
					{
						StringBuilder bodyContentType = new StringBuilder();
						Integer embedCount = parseParts(msg, id, bodyBuf, bodyContentType, attachments, new Integer(-1));
						
						if (bodyContentType.length() > 0)
						{
							// save the content type of the message body - which may be different from the
							// overall MIME type of the message (multipart, etc)
							mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE + ": " + bodyContentType);
						}
					}
					catch (MessagingException e)
					{
						M_log.warn("service(): msg.getContent() threw: " + e);
					}
					catch (IOException e)
					{
						M_log.warn("service(): msg.getContent() threw: " + e);
					}
					
					// post the message to the group's channel
					String body[] = new String[2];
					body[0] = bodyBuf[0].toString(); // plain/text
					body[1] = bodyBuf[1].toString(); // html/text
					
					if (channel.getReplyToList())
					{
						List modifiedHeaders = new Vector();
						for (String header: (List<String>)mailHeaders) 
						{
							if (header != null && !header.startsWith("Reply-To:"))
							{
								modifiedHeaders.add(header);
							}
						}
						// Note: can't use recipient, since it's host may be configured as mailId@myhost.james
						MailAddress replyTo = new MailAddress( mailId, mail.getRemoteHost() );
						M_log.debug("Set Reply-To address to "+ replyTo.toString());
						modifiedHeaders.add("Reply-To: "+ replyTo.toString());
  
						// post the message to the group's channel
						channel.addMailArchiveMessage(subject, from.toString(), TimeService.newTime(sent.getTime()), modifiedHeaders,
								attachments, body);
					}
					else
					{
						// post the message to the group's channel
						channel.addMailArchiveMessage(subject, from.toString(), TimeService.newTime(sent.getTime()), mailHeaders,
								attachments, body);
					}
															
					M_log.debug(id + " : delivered to:" + mailId);

					// all is happy - ghost the message to stop further processing
					mail.setState(Mail.GHOST);
				}
				catch (IdUnusedException goOn)
				{
					// if this is to the postmaster, and there's no site, channel or alias for the postmaster, then quietly eat the message
					if (POSTMASTER.equals(mailId))
					{
						mail.setState(Mail.GHOST);
						continue;
					}

					if (from.startsWith(POSTMASTER + "@"))
					{
						mail.setState(Mail.GHOST);
						continue;
					}

					M_log.info(id + " : mail rejected: " + goOn.toString());
					String errMsg = rb.getString("err_addr_unknown") + "\n\n";
					String mailSupport = StringUtil.trimToNull( ServerConfigurationService.getString("mail.support") );
					if ( mailSupport != null )
						errMsg +=(String) rb.getFormattedMessage("err_questions",  new Object[]{mailSupport})+"\n";
					mail.setErrorMessage(errMsg);
				}
				catch (PermissionException e)
				{
					M_log.info(id + " : " + e);
				}
				catch (Exception  ex)
				{
					M_log.info(id + " : " + ex);
				}
			}
		}
		finally
		{
			// clear out any current current bindings
			ThreadLocalManager.clear();
		}
	}

	/**
	 * Check if the fromAddr email address is recognized as belonging to a user who has permission to add to the channel.
	 * 
	 * @param fromAddr
	 *        The email address to check.
	 * @param channel
	 *        The mail archive channel.
	 * @return True if the email address is from a user who is authorized to add mail, false if not.
	 */
	protected boolean fromValidUser(String fromAddr, MailArchiveChannel channel)
	{
		if ((fromAddr == null) || (fromAddr.length() == 0)) return false;

		// find the users with this email address
		Collection users = UserDirectoryService.findUsersByEmail(fromAddr);

		// if none found
		if ((users == null) || (users.isEmpty())) return false;

		// see if any of them are allowed to add
		for (Iterator i = users.iterator(); i.hasNext();)
		{
			User u = (User) i.next();
			if (channel.allowAddMessage(u)) return true;
		}

		return false;
	}

	/**
	 * Create an attachment, adding it to the list of attachments.
	 */
	protected Reference createAttachment(List attachments, String type, String fileName, byte[] body, String id)
	{
		// we just want the file name part - strip off any drive and path stuff
		String name = Validator.getFileName(fileName);
		String resourceName = Validator.escapeResourceName(fileName);

		// make a set of properties to add for the new resource
		ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
		props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		props.addProperty(ResourceProperties.PROP_DESCRIPTION, fileName);

		// make an attachment resource for this URL
		try
		{
			ContentResource attachment = ContentHostingService.addAttachmentResource(resourceName, type, body, props);

			// add a dereferencer for this to the attachments
			Reference ref = EntityManager.newReference(attachment.getReference());
			attachments.add(ref);

			M_log.debug(id + " : attachment: " + ref.getReference() + " size: " + body.length);

			return ref;
		}
		catch (Exception any)
		{
			M_log.warn(id + " : exception adding attachment resource: " + name + " : " + any.toString());
			return null;
		}
	}

	/**
	 * Read in a stream from the mime body into a byte array
	 */
	protected byte[] readBody(int approxSize, InputStream stream)
	{
		// the size is APPROXIMATE, and is sometimes wrong -
		// so read the body into a ByteArrayOutputStream
		// that will grow if necessary
		if (approxSize <= 0) return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream(approxSize);
		byte[] buff = new byte[10000];
		try
		{
			int lenRead = 0;
			int count = 0;
			while (count >= 0)
			{
				count = stream.read(buff, 0, buff.length);
				if (count <= 0) break;
				baos.write(buff, 0, count);
				lenRead += count;
			}
		}
		catch (IOException e)
		{
			M_log.warn("readBody(): " + e);
		}

		return baos.toByteArray();
	}

	/**
	 * Breaks email messages into parts which can be saved as files (saves as attachments) or viewed as plain text (added to body of message).
	 * 
	 * @param p
	 *        The message-part embedded in a message..
	 * @param id
	 *        The string containing the message's id.
	 * @param bodyBuf
	 *        The string-buffers in which the plain/text and/or html/text message body is being built.
	 * @param bodyContentType
	 *        The value of the Content-Type header for the mesage body.
	 * @param attachments
	 *        The ReferenceVector in which references to attachments are collected.
	 * @param embedCount
	 *        An Integer that counts embedded messages (outer message is zero).
	 * @return Value of embedCount (updated if this call processed any embedded messages).
	 */
	protected Integer parseParts(Part p, String id, StringBuilder bodyBuf[], 
										  StringBuilder bodyContentType, List attachments,	Integer embedCount) 
			throws MessagingException, IOException
	{
		// increment embedded message counter
		if (p instanceof Message)
		{
			embedCount = new Integer( embedCount.intValue() + 1 );
		}
		
		String type = p.getContentType();

		// discard if content-type is unknown
		if (type == null || type.equals(""))
		{
			M_log.warn(this+" message with unknown content-type discarded");
		}
		
		// add plain text to bodyBuf[0]
		else if (p.isMimeType("text/plain") && p.getFileName() == null)
		{
			Object o = p.getContent();
			String txt = null;
			String innerContentType = p.getContentType();

			if (o instanceof String)
			{
				txt = (String) p.getContent();
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			else if (o instanceof InputStream)
			{
				InputStream in = (InputStream) o;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[in.available()];
				for (int len = in.read(buf); len != -1; len = in.read(buf))
					out.write(buf, 0, len);
				String charset = (new ContentType(innerContentType)).getParameter("charset");
				txt = out.toString(MimeUtility.javaCharset(charset));
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}
			
 			// remove extra line breaks added by mac Mail, perhaps others
			// characterized by a space followed by a line break
			txt = txt.replaceAll(" \n", " ");

			// make sure previous message parts ended with newline
			if (bodyBuf[0].length() > 0 && bodyBuf[0].charAt(bodyBuf[0].length() - 1) != '\n')
				bodyBuf[0].append("\n");
			
			bodyBuf[0].append(txt);
		}

		// add html text to bodyBuf[1]
		else if (p.isMimeType("text/html") && p.getFileName() == null)
		{
			Object o = p.getContent();
			String txt = null;
			String innerContentType = p.getContentType();

			if (o instanceof String)
			{
				txt = (String) p.getContent();
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			else if (o instanceof InputStream)
			{
				InputStream in = (InputStream) o;
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[in.available()];
				for (int len = in.read(buf); len != -1; len = in.read(buf))
					out.write(buf, 0, len);
				String charset = (new ContentType(innerContentType)).getParameter("charset");
				txt = out.toString(MimeUtility.javaCharset(charset));
				if (bodyContentType != null && bodyContentType.length() == 0) 
					bodyContentType.append(innerContentType);
			}

			// remove bad image tags and naughty javascript
			txt = Web.cleanHtml( txt );
			
			bodyBuf[1].append(txt);
		}
				
		// process subparts of multiparts
		else if (p.isMimeType("multipart/*"))
		{
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
			{
				embedCount = parseParts(mp.getBodyPart(i), id, bodyBuf, bodyContentType, attachments, embedCount);
			}
		}
		
		// Discard parts with mime-type application/applefile. If an e-mail message contains an attachment is sent from
		// a macintosh, you may get two parts, one for the data fork and one for the resource fork. The part that
		// corresponds to the resource fork confuses users, this has mime-type application/applefile. The best thing
		// is to discard it.
		else if (p.isMimeType("application/applefile"))
		{
			M_log.warn(this+" message with application/applefile discarded");
		}
		
		// discard enriched text version of the message.
		// Sakai only uses the plain/text or html/text version of the message.
		else if (p.isMimeType("text/enriched") && p.getFileName() == null)
		{
			M_log.warn(this+" message with text/enriched discarded");
		}
		
		// everything else gets treated as an attachment
		else
		{
			String name = p.getFileName();
			
			// look for filenames not parsed by getFileName() 
			if ( name == null && type.indexOf(NAME_PREFIX) != -1 )
			{
				name = type.substring( type.indexOf(NAME_PREFIX)+NAME_PREFIX.length() );
			}
			// ContentType can't handle filenames with spaces or UTF8 characters
			if ( name != null )
			{
				String decodedName = MimeUtility.decodeText( name ); // first decode RFC 2047
				type = type.replace( name, URLEncoder.encode(decodedName, "UTF-8") );
				name = decodedName;
			}
			
			ContentType cType = new ContentType(type);
			String disposition = p.getDisposition();
			int approxSize = p.getSize();

			if (name == null)
			{
				name = "unknown";
				// if file's parent is multipart/alternative,
				// provide a better name for the file
				if (p instanceof BodyPart)
				{
					Multipart parent = ((BodyPart) p).getParent();
					if (parent != null)
					{
						String pType = parent.getContentType();
						ContentType pcType = new ContentType(pType);
						if (pcType.getBaseType().equalsIgnoreCase("multipart/alternative"))
						{
							name = "message" + embedCount;
						}
					}
				}
				if (p.isMimeType("text/html"))
				{
					name += ".html";
				}
				else if (p.isMimeType("text/richtext"))
				{
					name += ".rtx";
				}
				else if (p.isMimeType("text/rtf"))
				{
					name += ".rtf";
				}
				else if (p.isMimeType("text/enriched"))
				{
					name += ".etf";
				}
				else if (p.isMimeType("text/plain"))
				{
					name += ".txt";
				}
				else if (p.isMimeType("text/xml"))
				{
					name += ".xml";
				}
				else if (p.isMimeType("message/rfc822"))
				{
					name += ".txt"; 
				}
			}

			// read the attachments bytes, and create it as an attachment in content hosting
			byte[] bodyBytes = readBody(approxSize, p.getInputStream());
			if ((bodyBytes != null) && (bodyBytes.length > 0))
			{
				// can we ignore the attachment it it's just whitespace chars??
				Reference attachment = createAttachment(attachments, cType.getBaseType(), name, bodyBytes, id);

				// add plain/text attachment reference (if plain/text message)
				if (attachment != null && bodyBuf[0].length() > 0)
					bodyBuf[0].append("[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]\n\n");
					
				// add html/text attachment reference (if html/text message)
				if (attachment != null && bodyBuf[1].length() > 0)
					bodyBuf[1].append("<p>[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]</p>");
					
				// add plain/text attachment reference (if no plain/text and no html/text)
				if (attachment != null && bodyBuf[0].length() == 0 && bodyBuf[1].length() == 0)
					bodyBuf[0].append("[see attachment: \"" + name + "\", size: " + bodyBytes.length + " bytes]\n\n");
			}
		}
		
		return embedCount;
	}

}
