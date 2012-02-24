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

import java.util.Enumeration;

import org.sakaiproject.tool.api.ToolSession;

/**
 * @author ieb
 *
 */
public class MockToolSession implements ToolSession
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#clearAttributes()
	 */
	public void clearAttributes()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getCreationTime()
	 */
	public long getCreationTime()
	{
		return 1001;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getId()
	 */
	public String getId()
	{
		return "toolSessionID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime()
	{
		return 1002;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getPlacementId()
	 */
	public String getPlacementId()
	{
		return "ToolSession.placementId";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getUserEid()
	 */
	public String getUserEid()
	{
		return "ToolSession.userEID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#getUserId()
	 */
	public String getUserId()
	{
		return "ToolSession.userID";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.ToolSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value)
	{
		// TODO Auto-generated method stub

	}

}
