/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.mock.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UserDirectoryService implements
		org.sakaiproject.user.api.UserDirectoryService {

	Map<String, org.sakaiproject.mock.domain.User> users = new HashMap<String, org.sakaiproject.mock.domain.User>();
	org.sakaiproject.mock.domain.User currentUser;
	
	public UserEdit addUser(String id, String eid)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException {
		org.sakaiproject.mock.domain.User user = new org.sakaiproject.mock.domain.User();
		user.setId(id);
		user.setEid(eid);
		users.put(id, user);
		return user;
	}

	public User addUser(String id, String eid, String firstName,
			String lastName, String email, String pw, String type,
			ResourceProperties properties) throws UserIdInvalidException,
			UserAlreadyDefinedException, UserPermissionException {
				org.sakaiproject.mock.domain.User user = new org.sakaiproject.mock.domain.User(pw, id, eid, firstName + " " + lastName, email,
						firstName, lastName, lastName + ", " + firstName,  eid, null, null, null, null, properties, null, null, null);
				user.setId(id);
				user.setEid(eid);
				users.put(id, user);
				return user;
	}

	public boolean allowAddUser() {
		return true;
	}

	public boolean allowRemoveUser(String id) {
		return true;
	}

	public boolean allowUpdateUser(String id) {
		return true;
	}

	public boolean allowUpdateUserEmail(String id) {
		return true;
	}

	public boolean allowUpdateUserName(String id) {
		return true;
	}

	public boolean allowUpdateUserPassword(String id) {
		return true;
	}

	public boolean allowUpdateUserType(String id) {
		return true;
	}

	public User authenticate(String eid, String password) {
		org.sakaiproject.mock.domain.User user = new org.sakaiproject.mock.domain.User();
		user.setId(eid);
		user.setEid(eid);
		return user;
	}

	public void cancelEdit(UserEdit user) {
	}

	public void commitEdit(UserEdit user) throws UserAlreadyDefinedException {
	}

	public int countSearchUsers(String criteria) {
		return 0;
	}

	public int countUsers() {
		return 0;
	}

	public void destroyAuthentication() {
	}

	public UserEdit editUser(String id) throws UserNotDefinedException,
			UserPermissionException, UserLockedException {
		return null;
	}

	public Collection findUsersByEmail(String email) {
		return null;
	}

	public User getAnonymousUser() {
		return null;
	}

	public User getCurrentUser() {
		return currentUser;
	}
	
	public void setCurrentUser(org.sakaiproject.mock.domain.User currentUser) {
		this.currentUser = currentUser;
	}

	public User getUser(String id) throws UserNotDefinedException {
		return users.get(id);
	}

	public User getUserByEid(String eid) throws UserNotDefinedException {
		return users.get(eid);
	}

	public String getUserEid(String id) throws UserNotDefinedException {
		return null;
	}

	public String getUserId(String eid) throws UserNotDefinedException {
		org.sakaiproject.mock.domain.User user = (org.sakaiproject.mock.domain.User)getUserByEid(eid);
		if(user == null) return null;
		return user.getEid();
	}

	public List getUsers() {
		List<org.sakaiproject.mock.domain.User> userList = new ArrayList<org.sakaiproject.mock.domain.User>();
		for(Iterator<Entry<String, org.sakaiproject.mock.domain.User>> iter = users.entrySet().iterator(); iter.hasNext();) {
			userList.add(iter.next().getValue());
		}
		return userList;
	}

	public List getUsers(Collection ids) {
		List<org.sakaiproject.mock.domain.User> userList = new ArrayList<org.sakaiproject.mock.domain.User>();
		for(Iterator<Entry<String, org.sakaiproject.mock.domain.User>> iter = users.entrySet().iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.User user = iter.next().getValue();
			if(ids.contains(user.getId())) userList.add(user);
		}
		return userList;
	}

	public List getUsers(int first, int last) {
		return getUsers();
	}

	public List<User> getUsersByEids(Collection<String> eids)
	{
		List<User> userList = new ArrayList<User>();
		for(Iterator<Entry<String, org.sakaiproject.mock.domain.User>> iter = users.entrySet().iterator(); iter.hasNext();) {
			org.sakaiproject.mock.domain.User user = iter.next().getValue();
			if(eids.contains(user.getEid())) userList.add(user);
		}
		return userList;
	}

	public UserEdit mergeUser(Element el) throws UserIdInvalidException {
		return null;
	}

	public void removeUser(UserEdit user) throws UserPermissionException {
		users.remove(user.getId());
	}

	public List searchUsers(String criteria, int first, int last) {
		return null;
	}

	public String userReference(String id) {
		return "/user/" + id;
	}

	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments) {
		return null;
	}

	public Entity getEntity(Reference ref) {
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	public String getEntityDescription(Reference ref) {
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	public String getEntityUrl(Reference ref) {
		return null;
	}

	public HttpAccess getHttpAccess() {
		return null;
	}

	public String getLabel() {
		return this.getClass().getName();
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	public boolean willArchiveMerge() {
		return false;
	}

}
