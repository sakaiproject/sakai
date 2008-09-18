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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class MockUserDirectoryService implements UserDirectoryService
{

	private static final Log log = LogFactory.getLog(MockUserDirectoryService.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#addUser(java.lang.String,
	 *      java.lang.String)
	 */
	public UserEdit addUser(String id, String eid) throws UserIdInvalidException,
			UserAlreadyDefinedException, UserPermissionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#addUser(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      org.sakaiproject.entity.api.ResourceProperties)
	 */
	public User addUser(String id, String eid, String firstName, String lastName,
			String email, String pw, String type, ResourceProperties properties)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowAddUser()
	 */
	public boolean allowAddUser()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowRemoveUser(java.lang.String)
	 */
	public boolean allowRemoveUser(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowUpdateUser(java.lang.String)
	 */
	public boolean allowUpdateUser(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowUpdateUserEmail(java.lang.String)
	 */
	public boolean allowUpdateUserEmail(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowUpdateUserName(java.lang.String)
	 */
	public boolean allowUpdateUserName(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowUpdateUserPassword(java.lang.String)
	 */
	public boolean allowUpdateUserPassword(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#allowUpdateUserType(java.lang.String)
	 */
	public boolean allowUpdateUserType(String id)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#authenticate(java.lang.String,
	 *      java.lang.String)
	 */
	public User authenticate(String eid, String password)
	{
		if (eid == null)
		{
			log.debug("Authenticate null ");
			return null;
		}
		if (MockTestUser.SUPER.equals(eid))
		{
			log.debug("Authenticate eid=[" + eid + "] as superuser");
			return MockTestUser.SUPERUSER;
		}
		if (MockTestUser.AUTH.equals(eid))
		{
			log.debug("Authenticate eid=[" + eid + "] as auth");
			return MockTestUser.AUTHUSER;
		}
		log.debug("Authenticate null ");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#cancelEdit(org.sakaiproject.user.api.UserEdit)
	 */
	public void cancelEdit(UserEdit user)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#commitEdit(org.sakaiproject.user.api.UserEdit)
	 */
	public void commitEdit(UserEdit user) throws UserAlreadyDefinedException
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#countSearchUsers(java.lang.String)
	 */
	public int countSearchUsers(String criteria)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#countUsers()
	 */
	public int countUsers()
	{
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#destroyAuthentication()
	 */
	public void destroyAuthentication()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#editUser(java.lang.String)
	 */
	public UserEdit editUser(String id) throws UserNotDefinedException,
			UserPermissionException, UserLockedException
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#findUsersByEmail(java.lang.String)
	 */
	public Collection findUsersByEmail(String email)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getAnonymousUser()
	 */
	public User getAnonymousUser()
	{
		return MockTestUser.ANONUSER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getCurrentUser()
	 */
	public User getCurrentUser()
	{
		throw new RuntimeException(
				"JCRService should not use Current User if possible, except in login module");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUser(java.lang.String)
	 */
	public User getUser(String id) throws UserNotDefinedException
	{
		log.debug("Getting " + id);
		if (MockTestUser.AUTH.equals(id))
		{
			return MockTestUser.AUTHUSER;
		}
		if (MockTestUser.SUPER.equals(id))
		{
			return MockTestUser.SUPERUSER;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUserByEid(java.lang.String)
	 */
	public User getUserByEid(String eid) throws UserNotDefinedException
	{
		return getUser(eid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUserEid(java.lang.String)
	 */
	public String getUserEid(String id) throws UserNotDefinedException
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUserId(java.lang.String)
	 */
	public String getUserId(String eid) throws UserNotDefinedException
	{
		return eid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUsers()
	 */
	public List getUsers()
	{
		List<User> l = new ArrayList<User>();
		l.add(MockTestUser.SUPERUSER);
		l.add(MockTestUser.AUTHUSER);
		l.add(MockTestUser.ANONUSER);
		return l;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUsers(java.util.Collection)
	 */
	public List getUsers(Collection ids)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#getUsers(int, int)
	 */
	public List getUsers(int first, int last)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#mergeUser(org.w3c.dom.Element)
	 */
	public UserEdit mergeUser(Element el) throws UserIdInvalidException,
			UserAlreadyDefinedException, UserPermissionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#removeUser(org.sakaiproject.user.api.UserEdit)
	 */
	public void removeUser(UserEdit user) throws UserPermissionException
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#searchUsers(java.lang.String,
	 *      int, int)
	 */
	public List searchUsers(String criteria, int first, int last)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.user.api.UserDirectoryService#userReference(java.lang.String)
	 */
	public String userReference(String id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String,
	 *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
	 *      java.util.List)
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath,
			List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
	 */
	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference,
	 *      java.lang.String)
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
	 */
	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
	 */
	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
	 */
	public String getLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String,
	 *      org.w3c.dom.Element, java.lang.String, java.lang.String,
	 *      java.util.Map, java.util.Map, java.util.Set)
	 */
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String,
	 *      org.sakaiproject.entity.api.Reference)
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
	 */
	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public List<User> getUsersByEids(Collection<String> eids)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
