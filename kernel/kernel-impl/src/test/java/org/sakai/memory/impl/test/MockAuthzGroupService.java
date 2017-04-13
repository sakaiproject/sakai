package org.sakai.memory.impl.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupAdvisor;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupFullException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.javax.PagingPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MockAuthzGroupService implements AuthzGroupService {

	public Map<String, List<String>> getProviderIDsForRealms(List<String> realmIDs) {
		return null;
	}

	public AuthzGroup addAuthzGroup(String id) throws GroupIdInvalidException,
			GroupAlreadyDefinedException, AuthzPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public AuthzGroup addAuthzGroup(String id, AuthzGroup other,
			String maintainUserId) throws GroupIdInvalidException,
			GroupAlreadyDefinedException, AuthzPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean allowAdd(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowJoinGroup(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowRemove(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUnjoinGroup(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdate(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public String authzGroupReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	public int countAuthzGroups(String criteria) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Set getAllowedFunctions(String role, Collection azGroups) {
		// TODO Auto-generated method stub
		return null;
	}

	public AuthzGroup getAuthzGroup(String id) throws GroupNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getAuthzGroupIds(String providerId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getAuthzGroups(String criteria, PagingPosition page) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getAuthzGroupsIsAllowed(String userId, String function,
			Collection azGroups) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getAuthzUserGroupIds(ArrayList authzGroupIds, String userid) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set getProviderIds(String authzGroupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Integer> getUserCountIsAllowed(String function,
			Collection<String> azGroups) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserRole(String userId, String azGroupId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Map<String, String> getUserRoles(String userId, Collection<String> azGroupIds) {
		return null;
	}
	
	public Set<String> getUsersIsAllowed(String function, Collection<String> azGroups) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<String[]> getUsersIsAllowedByGroup(String function,
			Collection<String> azGroups) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getUsersRole(Collection userIds, String azGroupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAllowed(String userId, String function, String azGroupId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAllowed(String userId, String function, Collection azGroups) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String encodeDummyUserForRole(String roleId) throws IllegalArgumentException {
		return roleId;
	}

	public String decodeRoleFromDummyUser(String dummyUserId) throws IllegalArgumentException {
		return dummyUserId;
	}

	public void joinGroup(String authzGroupId, String role)
			throws GroupNotDefinedException, AuthzPermissionException {
		// TODO Auto-generated method stub

	}

	public void joinGroup(String authzGroupId, String role, int maxSize)
			throws GroupNotDefinedException, AuthzPermissionException,
			GroupFullException {
		// TODO Auto-generated method stub

	}

	public AuthzGroup newAuthzGroup(String id, AuthzGroup other,
			String maintainUserId) throws GroupAlreadyDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void refreshUser(String userId) {
		// TODO Auto-generated method stub

	}

	public void removeAuthzGroup(AuthzGroup azGroup)
			throws AuthzPermissionException {
		// TODO Auto-generated method stub

	}

	public void removeAuthzGroup(String id) throws AuthzPermissionException {
		// TODO Auto-generated method stub

	}

	public void save(AuthzGroup azGroup) throws GroupNotDefinedException,
			AuthzPermissionException {
		// TODO Auto-generated method stub

	}

	public void unjoinGroup(String authzGroupId)
			throws GroupNotDefinedException, AuthzPermissionException {
		// TODO Auto-generated method stub

	}

	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

    public Collection<String> getAuthzUsersInGroups(Set<String> groupIds) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public void addAuthzGroupAdvisor(AuthzGroupAdvisor advisor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean removeAuthzGroupAdvisor(AuthzGroupAdvisor advisor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<AuthzGroupAdvisor> getAuthzGroupAdvisors() {
		// TODO Auto-generated method stub
		return null;
	}

    public Set getMaintainRoles() {
        return null;
    }

	public Set<String> getAdditionalRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRoleName(String roleId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getRoleGroupName(String roleGroupId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isRoleAssignable(String roleId) {
		// TODO Auto-generated method stub
		return false;
	}
}
