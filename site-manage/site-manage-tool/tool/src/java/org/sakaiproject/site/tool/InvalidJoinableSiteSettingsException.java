/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import org.sakaiproject.util.ResourceLoader;

// Introduced for SAK-26626.
/**
 * Thrown when joinable site settings are invalid.
 * Stores resource message keys/params within the exception, this way user facing messages can be decided at the time of the exception.
 * Feel free to reuse this pattern elsewhere in Sakai ;)
 * @author bbailla2
 */
public class InvalidJoinableSiteSettingsException extends RuntimeException
{
	protected String messageKey = "";
	protected Object[] messageArgs = null;

	/**
	 * @param message the Exception message
	 * @param messageKey the Resource Loader key to be used for user facing messages
	 */
	public InvalidJoinableSiteSettingsException(String message, String messageKey)
	{
		super(message);
		this.messageKey = messageKey;
	}

	/**
	 * @param message the Exception message
	 * @param messageKey the Resource Loader key to be used for user facing messages
	 * @param messageArgs the arguments to be passed to the Resource Loader to populate user facing messages
	 */
	public InvalidJoinableSiteSettingsException(String message, String messageKey, Object[] messageArgs)
	{
		super(message);
		this.messageKey = messageKey;
		this.messageArgs = messageArgs;
	}

	public String getMessageKey()
	{
		return messageKey;
	}

	public Object[] getMessageArgs()
	{
		return messageArgs;
	}

	/**
	 * @param rb the ResourceLoader to fetch a formatted message from
	 * @return the formatted message from the specified ResourceLoader using this object's messageKey and messageArgs
	 */
	public String getFormattedMessage(ResourceLoader rb)
	{
		if (messageArgs == null)
		{
			return rb.getString(messageKey);
		}
		return rb.getFormattedMessage(messageKey, messageArgs);
	}
}
