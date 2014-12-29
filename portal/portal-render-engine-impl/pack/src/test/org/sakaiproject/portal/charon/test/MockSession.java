/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon.test;

import java.util.Collection;
import java.util.Enumeration;

import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;

/**
 * @author ieb
 *
 */
public class MockSession implements Session
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#clear()
	 */
	public void clear()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#clearExcept(java.util.Collection)
	 */
	public void clearExcept(Collection names)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getAttributeNames()
	 */
	public Enumeration getAttributeNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getContextSession(java.lang.String)
	 */
	public ContextSession getContextSession(String contextId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getCreationTime()
	 */
	public long getCreationTime()
	{
		return 2001;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getId()
	 */
	public String getId()
	{
		// TODO Auto-generated method stub
		return "Session.getID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getLastAccessedTime()
	 */
	public long getLastAccessedTime()
	{
		// TODO Auto-generated method stub
		return 2002;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval()
	{
		// TODO Auto-generated method stub
		return 2003;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getToolSession(java.lang.String)
	 */
	public ToolSession getToolSession(String placementId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getUserEid()
	 */
	public String getUserEid()
	{
		// TODO Auto-generated method stub
		return "ToolSession.getUserEID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#getUserId()
	 */
	public String getUserId()
	{
		// TODO Auto-generated method stub
		return "ToolSession.getUserID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#invalidate()
	 */
	public void invalidate()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#setActive()
	 */
	public void setActive()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int interval)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#setUserEid(java.lang.String)
	 */
	public void setUserEid(String eid)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.Session#setUserId(java.lang.String)
	 */
	public void setUserId(String uid)
	{
		// TODO Auto-generated method stub

	}

}
