package org.sakaiproject.email.api;

import java.io.File;
import java.util.List;
import java.util.Map;


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
 * @see javax.mail.Transport#send(MimeMessage)
 * @see javax.mail.Transport#send(MimeMessage, Address[])
 * @see javax.mail.internet.InternetAddress
 */
public interface EmailMessage
{
	/**
	 * Get the sender of this message.
	 * 
	 * @return The sender of this message.
	 */
	public EmailAddress getFrom();

	/**
	 * Set the sender of this message.
	 * 
	 * @param email
	 *            Email address of sender.
	 */
	public void setFrom(String email);

	/**
	 * Set the sender of this message.
	 * 
	 * @param emailAddress
	 *            {@link EmailAddress} of message sender.
	 */
	public void setFrom(EmailAddress emailAddress);

	/**
	 * Get recipient for replies.
	 * 
	 * @return {@link EmailAddress} of reply to recipient.
	 */
	public List<EmailAddress> getReplyTo();

	/**
	 * Set recipient for replies.
	 * 
	 * @param email
	 *            Email string of reply to recipient.
	 */
	public void addReplyTo(EmailAddress emailAddress);

	/**
	 * Set recipient for replies.
	 * 
	 * @param email
	 *            {@link EmailAddress} of reply to recipient.
	 */
	public void setReplyTo(List<EmailAddress> replyTo);

	/**
	 * Get intended recipients of this message.
	 * 
	 * @return List of {@link EmailAddress} that will receive this messagel
	 */
	public Map<RecipientType, List<EmailAddress>> getRecipients();

	/**
	 * Get recipients of this message that are associated to a certain type
	 * 
	 * @param type
	 * @return
	 * @see RecipientType
	 */
	public List<EmailAddress> getRecipients(RecipientType type);

	/**
	 * Add a recipient to this message.
	 * 
	 * @param type
	 *            How to address the recipient.
	 * @param email
	 *            Email to send to.
	 */
	public void addRecipient(RecipientType type, String email);

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
	public void addRecipient(RecipientType type, String name, String email);

	/**
	 * Add multiple recipients to this message.
	 * 
	 * @param type
	 *            How to address the recipients.
	 * @param addresses
	 *            List of {@link EmailAddress} to add to this message.
	 */
	public void addRecipients(RecipientType type, List<EmailAddress> addresses);

	/**
	 * Set the recipients of this message. This will replace any existing recipients of the same
	 * type.
	 * 
	 * @param type
	 *            How to address the recipients.
	 * @param addresses
	 *            List of {@link EmailAddress} to add to this message.
	 */
	public void setRecipients(RecipientType type, List<EmailAddress> addresses);

	/**
	 * Set the recipients of this messsage. This will replace any existing recipients
	 * 
	 * @param recipients
	 */
	public void setRecipients(Map<RecipientType, List<EmailAddress>> recipients);

	/**
	 * Get all recipients as a flattened list. This is intended to be used for determining the
	 * recipients for an SMTP route.
	 * 
	 * @return list of recipient addresses associated to this message
	 */
	public List<EmailAddress> getAllRecipients();

	/**
	 * Get the subject of this message.
	 * 
	 * @return The subject of this message. May be empty or null value.
	 */
	public String getSubject();

	/**
	 * Set the subject of this message.
	 * 
	 * @param subject
	 *            Subject for this message. Empty and null values allowed.
	 */
	public void setSubject(String subject);

	/**
	 * Get the body content of this message.
	 * 
	 * @return The body content of this message.
	 */
	public String getBody();

	/**
	 * Set the body content of this message.
	 * 
	 * @param body
	 *            The content of this message.
	 */
	public void setBody(String body);

	/**
	 * Get the attachments on this message
	 * 
	 * @return List of {@link Attachment} attached to this message.
	 */
	public List<Attachment> getAttachments();

	/**
	 * Add an attachment to this message.
	 * 
	 * @param attachment
	 *            File to attach to this message.
	 */
	public void addAttachment(Attachment attachment);

	/**
	 * Add an attachment to this message. Same as addAttachment(new Attachment(file)).
	 * 
	 * @param file
	 */
	public void addAttachment(File file);

	/**
	 * Set the attachments of this message. Will replace any existing attachments.
	 * 
	 * @param attachments
	 *            The attachments to set on this message.
	 */
	public void setAttachments(List<Attachment> attachments);

	/**
	 * Get the headers of this message.
	 * 
	 * @return {@link java.util.Map} of headers set on this message.
	 */
	public Map<String, String> getHeaders();

	/**
	 * Flattens the headers down to "key: value" strings.
	 * 
	 * @return List of properly formatted headers. List will be 0 length if no headers found. Does
	 *         not return null
	 */
	public List<String> extractHeaders();

	/**
	 * Remove a header from this message. Does nothing if header is not found.
	 * 
	 * @param key
	 */
	public void removeHeader(String key);

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
	public void addHeader(String key, String value);

	/**
	 * Sets a header to this message. Any previous value for this key will be replaced. If value is
	 * null, previous entries of the matching key will be removed.
	 * 
	 * @param key
	 *            The key of the header.
	 * @param value
	 *            The value of the header.
	 */
	public void setHeader(String key, String value);

	/**
	 * Set the headers of this message. Will replace any existing headers.
	 * 
	 * @param headers
	 *            The headers to use on this message.
	 */
	public void setHeaders(Map<String, String> headers);

	/**
	 * Sets headers on this message. The expected format of each header is key: value.
	 * 
	 * @param headers
	 */
	public void setHeaders(List<String> headers);

	/**
	 * Get the mime type of this message.
	 * 
	 * @return {@link org.sakaiproject.email.api.ContentType} of this message.
	 */
	public String getContentType();

	/**
	 * Set the mime type of this message.
	 * 
	 * @param mimeType
	 *            The mime type to use for this message.
	 * @see org.sakaiproject.email.api.ContentType
	 */
	public void setContentType(String mimeType);

	/**
	 * Get the character set for text in this message. Used for the subject and body.
	 * 
	 * @return The character set used for this message.
	 */
	public String getCharacterSet();

	/**
	 * Set the character set for text in this message.
	 * 
	 * @param characterSet
	 *            The character set used to render text in this message.
	 * @see org.sakaproject.email.api.CharacterSet
	 */

	/**
	 * Gets the format of this message.
	 * 
	 * @return
	 */
	public String getFormat();

	/**
	 * Set the format of this message if content type is text/plain
	 * 
	 * @param format
	 * @see org.sakaiproject.email.api.PlainTextFormat
	 * @see org.sakaiproject.email.api.ContentType
	 */
	public void setFormat(String format);
}