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

import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ieb
 *
 */
public class MockUser implements UserEdit
{

	private String id;

	/**
	 * @param id
	 */
	public MockUser(String id)
	{
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#restrictEditEmail()
	 */
	public void restrictEditEmail()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#restrictEditFirstName()
	 */
	public void restrictEditFirstName()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#restrictEditLastName()
	 */
	public void restrictEditLastName()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#restrictEditPassword()
	 */
	public void restrictEditPassword()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#restrictEditType()
	 */
	public void restrictEditType()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setEid(java.lang.String)
	 */
	public void setEid(String eid)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setEmail(java.lang.String)
	 */
	public void setEmail(String email)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setFirstName(java.lang.String)
	 */
	public void setFirstName(String name)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setId(java.lang.String)
	 */
	public void setId(String id)
	{
	

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setLastName(java.lang.String)
	 */
	public void setLastName(String name)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setPassword(java.lang.String)
	 */
	public void setPassword(String pw)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.UserEdit#setType(java.lang.String)
	 */
	public void setType(String type)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#checkPassword(java.lang.String)
	 */
	public boolean checkPassword(String pw)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getCreatedBy()
	 */
	public User getCreatedBy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getCreatedTime()
	 */
	public Time getCreatedTime()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getDisplayId()
	 */
	public String getDisplayId()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getDisplayName()
	 */
	public String getDisplayName()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getEid()
	 */
	public String getEid()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getEmail()
	 */
	public String getEmail()
	{
		// TODO Auto-generated method stub
		return id+"@xxx.com";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getFirstName()
	 */
	public String getFirstName()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getLastName()
	 */
	public String getLastName()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getModifiedBy()
	 */
	public User getModifiedBy()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getModifiedTime()
	 */
	public Time getModifiedTime()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getSortName()
	 */
	public String getSortName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.user.api.User#getType()
	 */
	public String getType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getId()
	 */
	public String getId()
	{
		// TODO Auto-generated method stub
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 */
	public String getReference()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
	 */
	public String getReference(String rootProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
	 */
	public String getUrl(String rootProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#toXml(org.w3c.dom.Document, java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#getPropertiesEdit()
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#isActiveEdit()
	 */
	public boolean isActiveEdit()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
