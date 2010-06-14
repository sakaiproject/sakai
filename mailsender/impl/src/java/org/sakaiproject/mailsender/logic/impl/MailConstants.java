/**********************************************************************************
 * Copyright 2009 Sakai Foundation
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

public interface MailConstants
{
	/** Property name of email service smtp host */
	String SAKAI_SMTP_HOST = "smtp@org.sakaiproject.email.api.EmailService";

	/** Property name of email service smtp port */
	String SAKAI_SMTP_PORT = "smtpPort@org.sakaiproject.email.api.EmailService";

	/** Property name of email service connection timeout */
	String SAKAI_SMTP_CONNECTION_TIMEOUT = "smtpConnectionTimeout@org.sakaiproject.email.api.EmailService";

	/** Property name of email service timeout */
	String SAKAI_SMTP_TIMEOUT = "smtpTimeout@org.sakaiproject.email.api.EmailService";

	/** Property name of email service smtp user */
	String SAKAI_SMTP_USER = "smtpUser@org.sakaiproject.email.api.EmailService";

	/** Property name of email service smtp password */
	String SAKAI_SMTP_PASSWORD = "smtpPassword@org.sakaiproject.email.api.EmailService";

	/** Property name of email service smtp ssl usage flag */
	String SAKAI_SMTP_USE_SSL = "smtpUseSSL@org.sakaiproject.email.api.EmailService";

	/** Property name of email service flag to enable/disable transport "send" call */
	String SAKAI_SMTP_ALLOW_TRANSPORT = "allowTransport@org.sakaiproject.email.api.EmailService";

	/**
	 * Property name to set partial sending of message. If set to true, and a message has some valid
	 * and some invalid addresses, send the message anyway, reporting the partial failure with a
	 * SendFailedException. If set to false (the default), the message is not sent to any of the
	 * recipients if there is an invalid recipient address.
	 */
	String MAIL_SMTP_SENDPARTIAL = "mail.smtp.sendpartial";

	/**
	 * Property name for connection timeout. Socket connection timeout value in milliseconds.
	 * Default is infinite timeout.
	 */
	String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

	/**
	 * Property name for timeout. Socket I/O timeout value in milliseconds. Default is infinite
	 * timeout.
	 */
	String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
}
