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

/**
 * <p>
 * DigestMessage is one message in a digest
 * </p>
 */
public interface DigestMessage
{
	/**
	 * Access the to (user id) of the message.
	 * 
	 * @return The to (user id) of the message.
	 */
	String getTo();

	/**
	 * Set the to (user id) of the message.
	 * 
	 * @param to
	 *        The to (user id) of the message.
	 */
	void setTo(String to);

	/**
	 * Access the subject of the message.
	 * 
	 * @return The subject of the message.
	 */
	String getSubject();

	/**
	 * Set the subject of the message
	 * 
	 * @param subject
	 *        The subject of the message.
	 */
	void setSubject(String subject);

	/**
	 * Access the body of the message.
	 * 
	 * @return The body of the message.
	 */
	String getBody();

	/**
	 * Set the body of the message
	 * 
	 * @param body
	 *        The subject of the message.
	 */
	void setBody(String body);
}
