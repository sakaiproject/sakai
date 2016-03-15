/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.tool.beans;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailsender.AttachmentException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.logic.ComposeLogic;
import org.sakaiproject.mailsender.logic.ConfigLogic;
import org.sakaiproject.mailsender.logic.ExternalLogic;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.sakaiproject.mailsender.model.EmailEntry;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;
import org.springframework.web.multipart.MultipartFile;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class EmailBean
{
	public static final String EMAIL_SENT = "emailSent";
	public static final String EMAIL_FAILED = "emailFailed";
	public static final String EMAIL_CANCELLED = "emailCancelled";

	private Map<String, MultipartFile> multipartMap;
	private final Log log = LogFactory.getLog(EmailBean.class);
	private ComposeLogic composeLogic;
	private ConfigLogic configLogic;
	private ExternalLogic externalLogic;
	private EmailEntry emailEntry;
	private TargettedMessageList messages;
    private MessageLocator messageLocator;
    private ServerConfigurationService configService;

	public EmailBean() { }

	public EmailBean(ComposeLogic composeLogic, ConfigLogic configLogic,
			ExternalLogic externalLogic, Map<String, MultipartFile> multipartMap,
			TargettedMessageList messages, MessageLocator messageLocator)
	{
		this.composeLogic = composeLogic;
		this.configLogic = configLogic;
		this.externalLogic = externalLogic;
		this.multipartMap = multipartMap;
		this.messages = messages;
        this.messageLocator = messageLocator;
	}

	public void setMultipartMap(Map<String, MultipartFile> multipartMap)
	{
		this.multipartMap = multipartMap;
	}

	public void setComposeLogic(ComposeLogic composeLogic)
	{
		this.composeLogic = composeLogic;
	}

	public void setConfigLogic(ConfigLogic configLogic)
	{
		this.configLogic = configLogic;
	}

	public void setExternalLogic(ExternalLogic externalLogic)
	{
		this.externalLogic = externalLogic;
	}

	public void setMessages(TargettedMessageList messages)
	{
		this.messages = messages;
	}

	public void setMessageLocator(MessageLocator messageLocator)
	{
		this.messageLocator = messageLocator;
	}

	public void setConfigService(ServerConfigurationService configService)
	{
		this.configService = configService;
	}

	public String cancelEmail()
	{
		return EMAIL_CANCELLED;
	}

	public EmailEntry getNewEmail()
	{
		if (emailEntry == null)
		{
			emailEntry = new EmailEntry(configLogic.getConfig());
		}
		return emailEntry;
	}

	public String sendEmail()
	{
		// make sure we have a minimum of data required
		if (emailEntry == null || emailEntry.getConfig() == null)
		{
			messages.addMessage(new TargettedMessage("error.nothing.send"));
			return EMAIL_FAILED;
		}

		ConfigEntry config = emailEntry.getConfig();
		User curUser = externalLogic.getCurrentUser();

		String fromEmail = "";
		String fromDisplay = "";
		if (curUser != null)
		{
			fromEmail = curUser.getEmail();
			fromDisplay = curUser.getDisplayName();
		}

		if (fromEmail == null || fromEmail.trim().length() == 0)
		{
			messages.addMessage(new TargettedMessage("no.from.address"));
			return EMAIL_FAILED;
		}

		

		HashMap<String, String> emailusers = new HashMap<String, String>();

		// compile the list of emails to send to
		compileEmailList(fromEmail, emailusers);

		// handle the other recipients
		List<String> emailOthers = emailEntry.getOtherRecipients();
		String[] allowedDomains = StringUtil.split(configService.getString("sakai.mailsender.other.domains"), ",");
		
		List<String> invalids = new ArrayList<String>();
		// add other recipients to the message
		for (String email : emailOthers)
		{
			if (allowedDomains != null && allowedDomains.length > 0) 
			{
				// check each "other" email to ensure it ends with an accepts domain
				for (String domain : allowedDomains)
				{
					if (email.endsWith(domain))
					{
						emailusers.put(email, null);
					}
					else
					{
						invalids.add(email);
					}
				}
			}
			else
			{
				emailusers.put(email, null);
			}
		}

		String content = emailEntry.getContent();

		if (emailEntry.getConfig().isAppendRecipientList()) {
		    content = content + compileRecipientList(emailusers);
		}

		String subjectContent = emailEntry.getSubject();
		if (subjectContent == null || subjectContent.trim().length() == 0)
		{
			subjectContent = messageLocator.getMessage("no.subject");
		}

		String subject = ((config.getSubjectPrefix() != null) ? config.getSubjectPrefix() : "")
				+ subjectContent;

		try
		{
			if (invalids.size() == 0)
			{
				List<Attachment> attachments = new ArrayList<Attachment>();
				if (multipartMap != null && !multipartMap.isEmpty()) {
					for (Entry<String, MultipartFile> entry : multipartMap.entrySet()) {
						MultipartFile mf = entry.getValue();
		                String filename = mf.getOriginalFilename();
		                try
		                {
		                    File f = File.createTempFile(filename, null);
		                    mf.transferTo(f);
		                    Attachment attachment = new Attachment(f, filename);
		                    attachments.add(attachment);
		                }
		                catch (IOException ioe)
		                {
		                    throw new AttachmentException(ioe.getMessage());
		                }
					}
				}
				// send the message
				invalids = externalLogic.sendEmail(config, fromEmail, fromDisplay,
						emailusers, subject, content, attachments);
			}

			// append to the email archive
			String siteId = externalLogic.getSiteID();
			String fromString = fromDisplay + " <" + fromEmail + ">";
			addToArchive(config, fromString, subject, siteId);

			// build output message for results screen
			for (Entry<String, String> entry : emailusers.entrySet())
			{
				String compareAddr = null;
				String addrStr = null;
				if (entry.getValue() != null && entry.getValue().trim().length() > 0)
				{
					addrStr = entry.getValue();
					compareAddr = "\"" + entry.getValue() + "\" <" + entry.getKey() + ">";
				}
				else
				{
					addrStr = entry.getKey();
					compareAddr = entry.getKey();
				}
				if (!invalids.contains(compareAddr))
				{
					messages.addMessage(new TargettedMessage("verbatim", new String[] { addrStr },
							TargettedMessage.SEVERITY_CONFIRM));
				}
			}
		}
		catch (MailsenderException me)
		{
			//Print this exception
			log.warn(me);
			messages.clear();
			List<Map<String, Object[]>> msgs = me.getMessages();
			if (msgs != null)
			{
				for (Map<String, Object[]> msg : msgs)
				{
					for(Map.Entry<String, Object[]> e : msg.entrySet())
					{
						messages.addMessage(new TargettedMessage(e.getKey(), e.getValue(),
								TargettedMessage.SEVERITY_ERROR));
					}
				}
			}
			else
			{
				messages.addMessage(new TargettedMessage("verbatim",
						new String[] { me.getMessage() },
						TargettedMessage.SEVERITY_ERROR));
			}
			return EMAIL_FAILED;
		}
		catch (AttachmentException ae)
		{
			messages.clear();
			messages.addMessage(new TargettedMessage("error.attachment", new String[] { ae
					.getMessage() }, TargettedMessage.SEVERITY_ERROR));
			return EMAIL_FAILED;
		}

		// Display Users with Bad Emails if the option is turned on.
		boolean showBadEmails = config.isDisplayInvalidEmails();
		if (showBadEmails && invalids != null && invalids.size() > 0)
		{
			// add the message for the result screen
			String names = invalids.toString();
			messages.addMessage(new TargettedMessage("invalid.email.addresses",
					new String[] { names.substring(1, names.length() - 1) },
					TargettedMessage.SEVERITY_INFO));
		}

		return EMAIL_SENT;
	}

	private void addToArchive(ConfigEntry config, String fromString, String subject, String siteId)
	{
		if (emailEntry.getConfig().isAddToArchive())
		{
			StringBuilder attachment_info = new StringBuilder("<br/>");
			int i = 1;
			for (MultipartFile file : multipartMap.values())
			{
				if (file.getSize() > 0)
				{
					attachment_info.append("<br/>");
					attachment_info.append("Attachment #").append(i).append(": ").append(
							file.getName()).append("(").append(file.getSize()).append(" Bytes)");
					i++;
				}
			}
			String emailarchive = "/mailarchive/channel/" + siteId + "/main";
			String content = Web.cleanHtml(emailEntry.getContent()) + attachment_info.toString();
			externalLogic.addToArchive(config, emailarchive, fromString, subject, content);
		}
	}

	/**
	 * Compiles a list of email recipients from role, group and section selections.
	 *
	 * @param fromEmail
	 * @param emailusers
	 * @return Non-null <code>List</code> of users that have bad email addresses.
	 */
	private HashSet<String> compileEmailList(String fromEmail, HashMap<String, String> emailusers)
	{
		HashSet<String> badEmails = new HashSet<String>();
		if (emailEntry.isAllIds()) {
			try
			{
				addEmailUsers(fromEmail, emailusers, composeLogic.getUsers());
			}
			catch (IdUnusedException e)
			{
				log.warn(e.getMessage(), e);
				badEmails.add(e.getMessage());
			}
		} else {
			// check for roles and add users
			for (String roleId : emailEntry.getRoleIds().keySet())
			{
				try
				{
					List<User> users = composeLogic.getUsersByRole(roleId);
					addEmailUsers(fromEmail, emailusers, users);
				}
				catch (IdUnusedException e)
				{
					log.warn(e.getMessage(), e);
					badEmails.add(roleId);
				}
			}

			// check for sections and add users
			for (String sectionId : emailEntry.getSectionIds().keySet())
			{
				try
				{
					List<User> users = composeLogic.getUsersByGroup(sectionId);
					addEmailUsers(fromEmail, emailusers, users);
				}
				catch (IdUnusedException e)
				{
					log.warn(e.getMessage(), e);
				}
			}

			// check for groups and add users
			for (String groupId : emailEntry.getGroupIds().keySet())
			{
				try
				{
					List<User> users = composeLogic.getUsersByGroup(groupId);
					addEmailUsers(fromEmail, emailusers, users);
				}
				catch (IdUnusedException e)
				{
					log.warn(e.getMessage(), e);
				}
			}

			for (String userId : emailEntry.getUserIds().keySet())
			{
				User user = externalLogic.getUser(userId);
				addEmailUser(fromEmail, emailusers, user);
			}
		}
		return badEmails;
	}

	/**
	 * Add users to the email list and perform validation. Will not add "fromEmail" to the list.
	 *
	 * @param fromEmail
	 * @param emailusers
	 * @param users
	 * @return Non-null <code>List</code> of users that didn't pass validation
	 */
	private void addEmailUsers(String fromEmail, HashMap<String, String> emailusers,
			List<User> users)
	{
		for (User user : users)
		{
			addEmailUser(fromEmail, emailusers, user);
		}
	}

	/**
	 * Add user to the email list and perform validation. Will not add "fromEmail" or duplicates to
	 * the list.
	 *
	 * @param fromEmail
	 * @param emailusers
	 * @param users
	 * @return Non-null <code>List</code> of users that didn't pass validation
	 */
	private void addEmailUser(String fromEmail, HashMap<String, String> emailusers,
			User user)
	{
		if (!fromEmail.equals(user.getEmail()))
		{
			emailusers.put(user.getEmail(), user.getDisplayName());
		}
	}

	private String compileRecipientList(Map<String, String> recipients)
	{
		StringBuilder recipientList = new StringBuilder();
		recipientList.append("<br>");
		recipientList.append(messageLocator.getMessage("message.sent.to") + " ");
		Iterator iter = recipients.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<String, String> entry = (Map.Entry)iter.next();
			String email = entry.getKey();
			String name = entry.getValue();
			if (name != null)
			{
				recipientList.append(name);
			}
			else
			{
				recipientList.append(email);
			}
			if (iter.hasNext()) recipientList.append(", "); 
		}
		
		return recipientList.toString();
	}
}
