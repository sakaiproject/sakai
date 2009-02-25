/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.mock;

import java.util.List;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * @author ieb
 *
 */
public class MockSessionManager implements SessionManager
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#getActiveUserCount(int)
	 */
	public int getActiveUserCount(int secs)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#getCurrentSession()
	 */
	public Session getCurrentSession()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#getCurrentSessionUserId()
	 */
	public String getCurrentSessionUserId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#getCurrentToolSession()
	 */
	public ToolSession getCurrentToolSession()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#getSession(java.lang.String)
	 */
	public Session getSession(String sessionId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#setCurrentSession(org.sakaiproject.tool.api.Session)
	 */
	public void setCurrentSession(Session s)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#setCurrentToolSession(org.sakaiproject.tool.api.ToolSession)
	 */
	public void setCurrentToolSession(ToolSession s)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#startSession()
	 */
	public Session startSession()
	{
		return new MockSession();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.tool.api.SessionManager#startSession(java.lang.String)
	 */
	public Session startSession(String id)
	{
		return new MockSession();
	}

	public List<Session> getSessions() {
		return null;
	}

}
