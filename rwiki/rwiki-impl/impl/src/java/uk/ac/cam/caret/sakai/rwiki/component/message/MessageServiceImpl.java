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

package uk.ac.cam.caret.sakai.rwiki.component.message;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.MessageDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.dao.PagePresenceDao;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Message;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence;

/**
 * @author ieb
 */
@Slf4j
public class MessageServiceImpl implements MessageService
{

	private MessageDao messageDao;

	private PagePresenceDao pagePresenceDao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#updatePresence(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void updatePresence(String session, String user, String page,
			String space)
	{
		PagePresence pp = pagePresenceDao.findBySession(session);
		if (pp != null)
		{
			pp.setUser(user);
			pp.setPagename(page);
			pp.setPagespace(space);
			pp.setLastseen(new Date());
			pagePresenceDao.update(pp);
		}
		else
		{
			pp = pagePresenceDao.createPagePresence(page, space, session, user);
			pagePresenceDao.update(pp);
		}
		log.debug("Page Presence " + space + ":" + page + ":" + user + ":"
				+ session);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#addMessage(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	public void addMessage(String session, String user, String page,
			String space, String message)
	{
		Message messageobj = messageDao.createMessage(space, page, session,
				user, message);
		messageDao.update(messageobj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getSessionMessages(java.lang.String)
	 */
	public List getSessionMessages(String session)
	{
		return messageDao.findBySession(session);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getMessagesInSpace(java.lang.String)
	 */
	public List getMessagesInSpace(String space)
	{
		return messageDao.findBySpace(space);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getMessagesInPage(java.lang.String,
	 *      java.lang.String)
	 */
	public List getMessagesInPage(String space, String page)
	{
		return messageDao.findByPage(space, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getUsersInSpace(java.lang.String)
	 */
	public List getUsersInSpace(String space)
	{
		List l = pagePresenceDao.findBySpace(space);
		log.info("Found " + l.size() + " users in " + space);
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getUsersOnPage(java.lang.String,
	 *      java.lang.String)
	 */
	public List getUsersOnPage(String space, String page)
	{
		List l = pagePresenceDao.findByPage(space, page);
		log.info("Found " + l.size() + " users in " + space + " on " + page);
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService#getMessagesInSpaceOnly(java.lang.String,
	 *      java.lang.String)
	 */
	public List getUsersInSpaceOnly(String pageSpace, String pageName)
	{
		log
				.info("Searching for users in " + pageSpace + " but not "
						+ pageName);
		return pagePresenceDao.findBySpaceOnly(pageSpace, pageName);
	}

	/**
	 * @return Returns the messageDao.
	 */
	public MessageDao getMessageDao()
	{
		return messageDao;
	}

	/**
	 * @param messageDao
	 *        The messageDao to set.
	 */
	public void setMessageDao(MessageDao messageDao)
	{
		this.messageDao = messageDao;
	}

	/**
	 * @return Returns the pagePresenceDao.
	 */
	public PagePresenceDao getPagePresenceDao()
	{
		return pagePresenceDao;
	}

	/**
	 * @param pagePresenceDao
	 *        The pagePresenceDao to set.
	 */
	public void setPagePresenceDao(PagePresenceDao pagePresenceDao)
	{
		this.pagePresenceDao = pagePresenceDao;
	}

}
