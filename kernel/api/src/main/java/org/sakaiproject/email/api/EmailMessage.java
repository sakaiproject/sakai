/**********************************************************************************
 * Copyright 2008 Sakai Foundation
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
package org.sakaiproject.email.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.sakaiproject.email.api.EmailAddress.RecipientType;

import lombok.Getter;
import lombok.Setter;

/**
 * Value object for sending emails. Mimics javax.mail.internet.MimeMessage without having a
 * dependency on javax.mail<br>
 *
 * <p>
 * Sending a message can be done by specifying recipients and/or <em>actual</em> recipients. If only
 * recipients (to, cc, bcc) are specified, those are the people that will recieve the message and
 * will see each other listed in the to, cc and bcc fields. If actual recipients are specified, any
 * other recipients will be ignored but will be added to the email headers appropriately. This
 * allows for mailing to lists and hiding recipients (recipients: mylist@somedomain.edu,
 * actualRecipients: [long list of students].
 * </p>
 *
 * <p>
 * The default content type for a message is {@link ContentType#TEXT_PLAIN}. The content type only
 * applies to the message body.
 * </p>
 * <p>
 * The default character set for a message is UTF-8.
 * </p>
 *
 * @see javax.mail.Transport#send(Message)
 * @see javax.mail.Transport#send(Message, Address[])
 * @see javax.mail.internet.InternetAddress
 */
@Getter
@Setter
public class EmailMessage
{
	/**
	 * Who this message is from
	 */
	private EmailAddress from;

	/**
	 * Addressee(s) for replies
	 */
	private List<EmailAddress> replyTo;

	/**
	 * Recipients of message
	 */
	private Map<RecipientType, List<EmailAddress>> recipients = new HashMap<>();

	/**
	 * Subject of message
	 */
	private String subject;

	/**
	 * Body content of message
	 */
	private String body;

	/**
	 * Attachments to consider for message
	 */
	private List<Attachment> attachments;

	/**
	 * Arbitrary headers for message
	 */
	private Map<String, String> headers;

	/**
	 * Mime type of message. Defaults to text/plain.
	 *
	 * @see org.sakaiproject.email.api.ContentType
	 */
	private String contentType = ContentType.TEXT_PLAIN;

	/**
	 * Character set of text in message
	 *
	 * @see org.sakaiproject.email.api.CharacterSet
	 */
	private String characterSet = CharacterSet.UTF_8;

	/**
	 * Format of this message if in plain text.
	 *
	 * @see org.sakaiproject.email.api.PlainTextFormat
	 */
	private String format;

	/**
	 * Default constructor.
	 */
	public EmailMessage()
	{
	}

	public EmailMessage(String from, String subject, String body)
	{
		setFrom(from);
		setSubject(subject);
		setBody(body);
	}

	/**
	 * Set the sender of this message.
	 *
	 * @param email
	 *            Email address of sender.
	 */
	public void setFrom(String email)
	{
		this.from = new EmailAddress(email);
	}

    /**
     * Set the sender of this message.
     *
     * @param emailAddress
     *            {@link EmailAddress} of message sender.
     */
    public void setFrom(EmailAddress emailAddress)
    {
    	this.from = emailAddress;
    }

	/**
	 * Set recipient for replies.
	 *
	 * @param emailAddress
	 *            Email string of reply to recipient.
	 */
	public void addReplyTo(EmailAddress emailAddress)
	{
		if (replyTo == null)
		{
			replyTo = new ArrayList<EmailAddress>();
		}
		replyTo.add(emailAddress);
	}

	/**
	 * Get recipients of this message that are associated to a certain type
	 *
	 * @param type
	 * @return
	 * @see javax.mail.Message.RecipientType
	 */
	public List<EmailAddress> getRecipients(RecipientType type)
	{
		List<EmailAddress> retval = null;
		if (recipients != null)
		{
			retval = recipients.get(type);
		}
		return retval;
	}

	/**
	 * Add a recipient to this message.
	 *
	 * @param type
	 *            How to address the recipient.
	 * @param email
	 *            Email to send to.
	 */
	public void addRecipient(RecipientType type, String email)
	{
		List<EmailAddress> addresses = recipients.get(type);
		if (addresses == null)
		{
			addresses = new ArrayList<EmailAddress>();
		}
		addresses.add(new EmailAddress(email));
		recipients.put(type, addresses);
	}

	/**
	 * Add a recipient to this message.
	 *
	 * @param type
	 *            How to address the recipient.
	 * @param name
	 *            Name of recipient.
	 * @param email
	 *            Email to send to.
	 */
	public void addRecipient(RecipientType type, String name, String email)
	{
		List<EmailAddress> addresses = recipients.get(type);
		if (addresses == null)
		{
			addresses = new ArrayList<EmailAddress>();
		}
		addresses.add(new EmailAddress(email, name));
		recipients.put(type, addresses);
	}

	/**
	 * Add multiple recipients to this message.
	 *
	 * @param type
	 *            How to address the recipients.
	 * @param addresses
	 *            List of {@link EmailAddress} to add to this message.
	 */
	public void addRecipients(RecipientType type, List<EmailAddress> addresses)
	{
		List<EmailAddress> currentAddresses = recipients.get(type);
		if (currentAddresses == null)
		{
			recipients.put(type, addresses);
		}
		else
		{
			currentAddresses.addAll(addresses);
		}
	}

	/**
	 * Set the recipients of this message. This will replace any existing recipients of the same
	 * type.
	 *
	 * @param type
	 *            How to address the recipients.
	 * @param addresses
	 *            List of {@link EmailAddress} to add to this message.
	 */
	public void setRecipients(RecipientType type, List<EmailAddress> addresses)
	{
		if (addresses != null)
		{
			recipients.put(type, addresses);
		}
		else
		{
			recipients.remove(type);
		}
	}

	/**
	 * Set the recipients of this messsage. This will replace any existing recipients
	 *
	 * @param recipients
	 */
	public void setRecipients(Map<RecipientType, List<EmailAddress>> recipients)
	{
		this.recipients = recipients;
	}

	/**
	 * Get all recipients as a flattened list. This is intended to be used for determining the
	 * recipients for an SMTP route.
	 *
	 * @return list of recipient addresses associated to this message
	 */
	public List<EmailAddress> getAllRecipients()
	{
		List<EmailAddress> rcpts = new ArrayList<EmailAddress>();

		if (recipients.containsKey(RecipientType.TO))
		{
			rcpts.addAll(recipients.get(RecipientType.TO));
		}

		if (recipients.containsKey(RecipientType.CC))
		{
			rcpts.addAll(recipients.get(RecipientType.CC));
		}

		if (recipients.containsKey(RecipientType.BCC))
		{
			rcpts.addAll(recipients.get(RecipientType.BCC));
		}

		if (recipients.containsKey(RecipientType.ACTUAL))
		{
			rcpts.addAll(recipients.get(RecipientType.ACTUAL));
		}

		return rcpts;
	}

	/**
	 * Add an attachment to this message.
	 *
	 * @param attachment
	 *            File to attach to this message.
	 */
	public void addAttachment(Attachment attachment)
	{
		if (attachment != null)
		{
			if (attachments == null)
			{
				attachments = new ArrayList<Attachment>();
			}
			attachments.add(attachment);
		}
	}

	/**
	 * Add an attachment to this message. Same as addAttachment(new Attachment(file)).
	 *
	 * @param file
	 */
	public void addAttachment(File file)
	{
		addAttachment(new Attachment(file, file.getPath()));
	}

	/**
	 * Flattens the headers down to "key: value" strings.
	 *
	 * @return List of properly formatted headers. List will be 0 length if no headers found. Does
	 *         not return null
	 */
	public List<String> extractHeaders()
	{
		return headers != null ? headers.keySet().stream().filter(Objects::nonNull)
			.map(k -> k + ": " + headers.get(k)).collect(Collectors.toList())
				: Collections.<String>emptyList();
	}

	/**
	 * Remove a header from this message. Does nothing if header is not found.
	 *
	 * @param key
	 */
	public void removeHeader(String key)
	{
		if (headers != null && !headers.isEmpty() && headers.containsKey(key))
		{
			headers.remove(key);
		}
	}

	/**
	 * Add a header to this message. If the key is found in the headers of this message, the value
	 * is appended to the previous value found and separated by a space. A key of null will not be
	 * added. If value is null, previous entries of the matching key will be removed.
	 *
	 * @param key
	 *            The key of the header.
	 * @param value
	 *            The value of the header.
	 */
	public void addHeader(String key, String value)
	{
		if (headers == null || headers.get(key) == null)
		{
			setHeader(key, value);
		}
		else if (key != null && value != null)
		{
			String prevVal = headers.get(key);
			prevVal += " " + value;
			headers.put(key, prevVal);
		}
		else if (value == null)
		{
			removeHeader(key);
		}
	}

	/**
	 * Sets a header to this message. Any previous value for this key will be replaced. If value is
	 * null, previous entries of the matching key will be removed.
	 *
	 * @param key
	 *            The key of the header.
	 * @param value
	 *            The value of the header.
	 */
	public void setHeader(String key, String value)
	{
		if (key != null && value != null)
		{
			if (headers == null)
			{
				headers = new HashMap<String, String>();
			}

			headers.put(key, value);
		}
	}

	/**
	 * Sets headers on this message. The expected format of each header is key: value.
	 *
	 * @param headers
	 */
	public void setHeaders(List<String> headers)
	{
		if (headers != null)
		{
			for (String header : headers)
			{
				int splitPoint = header.indexOf(":");
				String key = header.substring(0, splitPoint);
				String value = null;
				if (splitPoint != header.length() - 1)
				{
					value = header.substring(splitPoint + 1).trim();
				}
				setHeader(key, value);
			}
		}
	}
}
