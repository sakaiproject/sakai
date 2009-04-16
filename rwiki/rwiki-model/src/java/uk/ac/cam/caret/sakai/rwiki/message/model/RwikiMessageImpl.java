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
package uk.ac.cam.caret.sakai.rwiki.message.model;

import java.util.Date;

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.Message;

/**
 * @author ieb
 */
public class RwikiMessageImpl implements Message
{
	private String id;

	private String sessionid;

	private String user;

	private String pagespace;

	private String pagename;

	private Date lastseen;

	private String message;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getId()
	 */
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getLastseen()
	 */
	public Date getLastseen()
	{
		return lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setLastseen(java.util.Date)
	 */
	public void setLastseen(Date lastseen)
	{
		this.lastseen = lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getMessage()
	 */
	public String getMessage()
	{
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setMessage(java.lang.String)
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getPagename()
	 */
	public String getPagename()
	{
		return pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setPagename(java.lang.String)
	 */
	public void setPagename(String pagename)
	{
		this.pagename = pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getPagespace()
	 */
	public String getPagespace()
	{
		return pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setPagespace(java.lang.String)
	 */
	public void setPagespace(String pagespace)
	{
		this.pagespace = pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getSessionid()
	 */
	public String getSessionid()
	{
		return sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setSessionid(java.lang.String)
	 */
	public void setSessionid(String sessionid)
	{
		this.sessionid = sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#getUser()
	 */
	public String getUser()
	{
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.notitication.api.model.ChatMessage#setUser(java.lang.String)
	 */
	public void setUser(String user)
	{
		this.user = user;
	}
}
