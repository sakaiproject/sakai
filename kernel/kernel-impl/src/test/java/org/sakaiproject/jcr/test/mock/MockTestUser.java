/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.jcr.test.mock;

import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class MockTestUser implements User
{

	public static final String SUPER = "superuser";

	public static final String AUTH = "user";

	public static final String ANON = "anonymous";

	public static final User ANONUSER = new MockTestUser(ANON);

	public static final User SUPERUSER = new MockTestUser(SUPER);

	public static final User AUTHUSER = new MockTestUser(AUTH);

	private String name;

	/**
	 * @param string
	 */
	public MockTestUser(String name)
	{
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#checkPassword(java.lang.String)
	 */
	public boolean checkPassword(String pw)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getCreatedBy()
	 */
	public User getCreatedBy()
	{
		return new MockTestUser(SUPER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getCreatedTime()
	 */
	public Time getCreatedTime()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getDisplayId()
	 */
	public String getDisplayId()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getDisplayName()
	 */
	public String getDisplayName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getEid()
	 */
	public String getEid()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getEmail()
	 */
	public String getEmail()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getFirstName()
	 */
	public String getFirstName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getLastName()
	 */
	public String getLastName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getModifiedBy()
	 */
	public User getModifiedBy()
	{
		return new MockTestUser(SUPER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getModifiedTime()
	 */
	public Time getModifiedTime()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getSortName()
	 */
	public String getSortName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.User#getType()
	 */
	public String getType()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getId()
	 */
	public String getId()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 */
	public String getReference()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
	 */
	public String getReference(String rootProperty)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl()
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
	 */
	public String getUrl(String rootProperty)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.Entity#toXml(org.w3c.dom.Document,
	 *      java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o)
	{
		return 0;
	}

}
