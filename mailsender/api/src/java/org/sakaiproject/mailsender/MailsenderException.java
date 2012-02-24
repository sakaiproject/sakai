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
package org.sakaiproject.mailsender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * General exception for Mailsender. This exception can store messages to be sent up to the UI for
 * display to the user. This is terrible design but somewhat necessary until the updated Email API
 * is more widely in place.
 */
public class MailsenderException extends Exception
{
	private static final long serialVersionUID = 1L;
	private List<Map<String, Object[]>> messages;

	public MailsenderException()
	{
	}

	public MailsenderException(String code, String value)
	{
		addMessage(code, value);
	}

	public MailsenderException(List<Map<String, Object[]>> messages)
	{
		this.messages = messages;
	}

	public MailsenderException(String message, Exception cause)
	{
		super(message, cause);
	}

	public boolean hasMessages()
	{
		return (messages != null && messages.size() > 0);
	}

	public void addMessage(Map<String, Object[]> message)
	{
		if (messages == null)
		{
			messages = new ArrayList<Map<String, Object[]>>();
		}
		messages.add(message);
	}

	public MailsenderException addMessage(String code)
	{
		addMessage(code, "");
		return this;
	}

	public MailsenderException addMessage(String code, String value)
	{
		addMessage(code, new String[] { value });
		return this;
	}

	public MailsenderException addMessage(String code, Object[] value)
	{
		HashMap<String, Object[]> hm = new HashMap<String, Object[]>();
		hm.put(code, value);
		addMessage(hm);
		return this;
	}

	public List<Map<String, Object[]>> getMessages()
	{
		return messages;
	}
}
