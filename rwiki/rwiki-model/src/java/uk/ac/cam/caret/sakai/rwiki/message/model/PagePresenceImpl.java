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

import uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence;

/**
 * @author ieb
 */
public class PagePresenceImpl implements PagePresence
{
	private String id;

	private String sessionid;

	private String user;

	private String pagespace;

	private String pagename;

	private Date lastseen;

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getId()
	 */
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setId(java.lang.String)
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getLastseen()
	 */
	public Date getLastseen()
	{
		return lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setLastseen(java.util.Date)
	 */
	public void setLastseen(Date lastseen)
	{
		this.lastseen = lastseen;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getPagename()
	 */
	public String getPagename()
	{
		return pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setPagename(java.lang.String)
	 */
	public void setPagename(String pagename)
	{
		this.pagename = pagename;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getPagespace()
	 */
	public String getPagespace()
	{
		return pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setPagespace(java.lang.String)
	 */
	public void setPagespace(String pagespace)
	{
		this.pagespace = pagespace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getSessionid()
	 */
	public String getSessionid()
	{
		return sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setSessionid(java.lang.String)
	 */
	public void setSessionid(String sessionid)
	{
		this.sessionid = sessionid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#getUser()
	 */
	public String getUser()
	{
		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.message.api.model.PagePresence#setUser(java.lang.String)
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	// ==========================
	public String getAge()
	{

		Date now = new Date();
		long lnow = now.getTime();
		lnow = lnow - this.lastseen.getTime();

		lnow = lnow / 1000L;
		if (lnow < 60L)
		{

			return String.valueOf(lnow) + "s";
		}
		lnow = lnow / 60L;
		if (lnow < 60L)
		{
			return String.valueOf(lnow) + "m";
		}
		lnow = lnow / 60L;
		if (lnow < 24L)
		{
			return String.valueOf(lnow) + "h";
		}
		lnow = lnow / 24L;
		if (lnow < 365L)
		{
			return String.valueOf(lnow) + "d";
		}
		lnow = lnow / 365L;
		return String.valueOf(lnow) + "y";
	}

}
