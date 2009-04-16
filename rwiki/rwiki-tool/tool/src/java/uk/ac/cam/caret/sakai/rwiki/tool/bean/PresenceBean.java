/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService;

/**
 * @author ieb
 */
public class PresenceBean
{
	private String pageName;

	private String pageSpace;

	private MessageService messageService;

	/**
	 * @return Returns the pageName.
	 */
	public String getPageName()
	{
		return pageName;
	}

	/**
	 * @param pageName
	 *        The pageName to set.
	 */
	public void setPageName(String pageName)
	{
		this.pageName = pageName;
	}

	/**
	 * @return Returns the pageSpace.
	 */
	public String getPageSpace()
	{
		return pageSpace;
	}

	/**
	 * @param pageSpace
	 *        The pageSpace to set.
	 */
	public void setPageSpace(String pageSpace)
	{
		this.pageSpace = pageSpace;
	}

	/**
	 * @return Returns the messageService.
	 */
	public MessageService getMessageService()
	{
		return messageService;
	}

	/**
	 * @param messageService
	 *        The messageService to set.
	 */
	public void setMessageService(MessageService messageService)
	{
		this.messageService = messageService;
	}

	/**
	 * returns a list of users on the page, ordered by last seen
	 * 
	 * @return
	 */
	public List getPagePresence()
	{
		return messageService.getUsersOnPage(pageSpace, pageName);
	}

	/**
	 * returns a list of users in the space, ordered by last seen
	 * 
	 * @return
	 */
	public List getSpacePresence()
	{
		return messageService.getUsersInSpaceOnly(pageSpace, pageName);
	}

	public List getPageMessages()
	{
		return messageService.getMessagesInPage(pageSpace, pageName);
	}

	public List getSpaceMessages()
	{
		return messageService.getMessagesInSpace(pageSpace);
	}

}
