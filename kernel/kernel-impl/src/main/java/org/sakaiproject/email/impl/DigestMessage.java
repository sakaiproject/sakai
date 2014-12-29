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

package org.sakaiproject.email.impl;

/**
 * <p>
 * DigestMessage is one message in a digest
 * </p>
 */
public class DigestMessage implements org.sakaiproject.email.api.DigestMessage
{
	/** The id of the User who gets this message. */
	protected String m_to = null;

	/** The subject. */
	protected String m_subject = null;

	/** The body. */
	protected String m_body = null;

	/**
	 * Construct.
	 * 
	 * @param to
	 *        The id of the User who gets this message.
	 * @param subject
	 *        The subject.
	 * @param body
	 *        The body.
	 */
	public DigestMessage(String to, String subject, String body)
	{
		m_to = to;
		m_subject = subject;
		m_body = body;
	}

	/**
	 * Access the to (user id) of the message.
	 * 
	 * @return The to (user id) of the message.
	 */
	public String getTo()
	{
		return m_to;
	}

	/**
	 * Set the to (user id) of the message.
	 * 
	 * @param subject
	 *        The to (user id) of the message.
	 */
	public void setTo(String to)
	{
		m_to = to;
	}

	/**
	 * Access the subject of the message.
	 * 
	 * @return The subject of the message.
	 */
	public String getSubject()
	{
		return m_subject;
	}

	/**
	 * Set the subject of the message
	 * 
	 * @param subject
	 *        The subject of the message.
	 */
	public void setSubject(String subject)
	{
		m_subject = subject;
	}

	/**
	 * Access the body of the message.
	 * 
	 * @return The body of the message.
	 */
	public String getBody()
	{
		return m_body;
	}

	/**
	 * Set the body of the message
	 * 
	 * @param body
	 *        The subject of the message.
	 */
	public void setBody(String body)
	{
		m_body = body;
	}
}
