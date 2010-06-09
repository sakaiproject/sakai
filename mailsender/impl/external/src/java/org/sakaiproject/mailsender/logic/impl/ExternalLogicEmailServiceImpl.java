/**********************************************************************************
 * Copyright 2008-2009 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.mailsender.logic.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.email.api.AddressValidationException;
import org.sakaiproject.email.api.Attachment;
import org.sakaiproject.email.api.ContentType;
import org.sakaiproject.email.api.EmailAddress;
import org.sakaiproject.email.api.EmailMessage;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.email.api.NoRecipientsException;
import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.mailsender.AttachmentException;
import org.sakaiproject.mailsender.MailsenderException;
import org.sakaiproject.mailsender.model.ConfigEntry;
import org.springframework.web.multipart.MultipartFile;

public class ExternalLogicEmailServiceImpl extends ExternalLogicImpl
{
	private EmailService emailService;

	public List<String> sendEmail(ConfigEntry config, String fromEmail, String fromName,
			Map<String, String> to, String subject, String content,
			Map<String, MultipartFile> attachments) throws MailsenderException, AttachmentException
	{
		ArrayList<EmailAddress> tos = new ArrayList<EmailAddress>();
		for (Entry<String, String> entry : to.entrySet())
		{
			tos.add(new EmailAddress(entry.getKey(), entry.getValue()));
		}

		EmailMessage msg = new EmailMessage();

		String replyToName = null;
		String replyToEmail = null;
		// set the "reply to" based on config
		if (ConfigEntry.ReplyTo.no_reply_to.name().equals(config.getReplyTo()))
		{
			replyToName = getCurrentSiteTitle();
			replyToEmail = "";
		}
		else
		{
			replyToName = fromName;
			replyToEmail = fromEmail;
		}

		msg.setFrom(new EmailAddress(replyToEmail, replyToName));

		msg.setSubject(subject);
		// set content type based on editor used
		if (config.useRichTextEditor())
		{
			msg.setContentType(ContentType.TEXT_HTML);
		}
		else
		{
			msg.setContentType(ContentType.TEXT_PLAIN);
		}
		msg.setBody(content);

		ArrayList<File> tempFiles = new ArrayList<File>();
		for (Entry<String, MultipartFile> entry : attachments.entrySet())
		{
			try
			{
				// get info about the uploaded file
				MultipartFile mf = entry.getValue();
				String filename = mf.getOriginalFilename();

				// store the file in temp space
				File f = File.createTempFile(filename, null);
				f.deleteOnExit();
				tempFiles.add(f);
				mf.transferTo(f);
				Attachment attachment = new Attachment(f, filename);
				msg.addAttachment(attachment);
			}
			catch (IOException ioe)
			{
				throw new AttachmentException(ioe.getMessage());
			}
		}


		// send a copy
		if (config.isSendMeACopy())
		{
			msg.addRecipient(RecipientType.CC, fromName, fromEmail);
		}

		// add all recipients to the bcc field
		msg.addRecipients(RecipientType.BCC, tos);

		// add a special header for tracking
		msg.addHeader("X-Mailer", "sakai-mailsender");
		msg.addHeader("Content-Transfer-Encoding", "quoted-printable");

		try
		{
			List<EmailAddress> invalids = emailService.send(msg);
			List<String> rets = EmailAddress.toStringList(invalids);
			return rets;
		}
		catch (AddressValidationException e)
		{
			MailsenderException me = new MailsenderException(e.getMessage(), e);
			me.addMessage("invalid.email.addresses", EmailAddress.toString(e
					.getInvalidEmailAddresses()));
			throw me;
		}
		catch (NoRecipientsException e)
		{
			MailsenderException me = new MailsenderException(e.getMessage(), e);
			me.addMessage("error.no.valid.recipients", "");
			throw me;
		}
	}

	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}
}
