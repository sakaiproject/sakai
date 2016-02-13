/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.email.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.sakaiproject.email.api.EmailAddress.RecipientType;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * EmailService is an interface to sending emails.
 * </p>
 */
public interface EmailService
{
	/**
	 * Creates and sends a generic text MIME message to the address contained in to.
	 * 
	 * @param from
	 *        The address this message is to be listed as coming from.
	 * @param to
	 *        The address(es) this message should be sent to.
	 * @param subject
	 *        The subject of this message.
	 * @param content
	 *        The body of the message.
	 * @param headerTo
	 *        If specified, this is placed into the message header, but "to" is used for the recipients.
	 * @param replyTo
	 *        If specified, this is the reply to header address(es).
	 * @param additionalHeaders
	 *        Additional email headers to send (List of String). For example, content type or forwarded headers (may be null)
	 */
	void sendMail(InternetAddress from, InternetAddress[] to, String subject, String content, InternetAddress[] headerTo,
			InternetAddress[] replyTo, List<String> additionalHeaders);

	/**
	 * <p>
	 * Creates and sends a generic text MIME message to the address contained in <code>to</code>.
	 * </p>
	 * 
	 * <p>
	 * Some character set constants are available in {@link CharacterSet}<br>
	 * The content type should be of the format "text/plain; charset=windows-1252; format=flowed"
	 * </p>
	 * 
	 * @param from
	 *            The address this message is to be listed as coming from.
	 * @param to
	 *            The address(es) this message should be sent to. These addresses are used in the
	 *            SMTP routing.
	 * @param subject
	 *            The subject of this message.
	 * @param content
	 *            The body of the message.
	 * @param headerTo
	 *            If specified, this is placed into the message header, but "to" is used for the
	 *            actual recipients.
	 * @param replyTo
	 *            If specified, the reply-to header value.
	 * @param additionalHeaders
	 *            Additional email headers to send (List of String). For example, content type or
	 *            forwarded headers (may be null)
	 * @param attachments
	 * 
	 */
	void sendMail(InternetAddress from, InternetAddress[] to, String subject, String content,
			Map<RecipientType, InternetAddress[]> headerTo, InternetAddress[] replyTo,
			List<String> additionalHeaders, List<Attachment> attachments);
	
	/**
	 * Creates and sends a generic text MIME message to the address contained in to.
	 * 
	 * @param fromStr
	 *        The address this message is to be listed as coming from, cannot be blank.
	 * @param toStr
	 *        The address(es) this message should be sent to. cannot be blank.
	 * @param subject
	 *        The subject of this message, may be null.
	 * @param content
	 *        The body of the message, cannot be blank.
	 * @param headerToStr
	 *        If specified, the address(es) are placed into the message header otherwise toStr is used.
	 * @param replyToStr
	 *        If specified, the address(es) are placed into the reply-to header value, may be null.
	 * @param additionalHeaders
	 *        Additional email headers to send (List of String). For example, content type or forwarded headers (may be null)
	 */
	void send(String fromStr, String toStr, String subject, String content, String headerToStr, String replyToStr,
			List<String> additionalHeaders);

	/**
	 * Send a single message to a set of Users.
	 * 
	 * @param users
	 *        Collection (of User) to send the message to (for those with valid email addresses).
	 * @param headers
	 *        List (of String, form "name: value") of headers for the message.
	 * @param message
	 *        String body of the message.
	 */
	void sendToUsers(Collection<User> users, Collection<String> headers, String message);

	/**
	 * Sends a single message to a set of users.
	 * 
	 * @deprecated
	 * @param message
	 *            {@link EmailMessage} that contains the parameters to create a message to the
	 *            specified recipients.
	 * @throws AddressValidationException
	 *             If any addresses are found to be invalid that prevent all the messages from being send.
	 *             Examples are reply-to address and from address.. This is checked when
	 *             converting to {@link javax.mail.internet.InternetAddress}.
	 * @throws NoRecipientsException
	 * @return {@link java.util.List} of recipients that were found to be invalid per to
	 *         {@link javax.mail.internet.InternetAddress}.
	 */
	List<EmailAddress> send(EmailMessage message) throws AddressValidationException,
			NoRecipientsException;

	/**
	 * Sends a single message to a set of users.
	 * 
	 * @param message
	 *            {@link EmailMessage} that contains the parameters to create a message to the
	 *            specified recipients.
	 * @param messagingException
	 *            Whether or not to throw a messaging exception
	 * @throws AddressValidationException
	 *             If any addresses are found to be invalid that prevent all the messages from being send.
	 *             Examples are reply-to address and from address.. This is checked when
	 *             converting to {@link javax.mail.internet.InternetAddress}.
	 * @throws NoRecipientsException
	 * @throws MessagingException
	 * @return {@link java.util.List} of recipients that were found to be invalid per to
	 *         {@link javax.mail.internet.InternetAddress}.
	 */

	List<EmailAddress> send(EmailMessage message, boolean messagingException) throws AddressValidationException,
			NoRecipientsException, MessagingException;
}