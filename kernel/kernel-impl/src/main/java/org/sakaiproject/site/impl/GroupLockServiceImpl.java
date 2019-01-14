package org.sakaiproject.site.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.sakaiproject.site.api.GroupLock;
import org.sakaiproject.site.api.GroupLockService;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

@Slf4j
public class GroupLockServiceImpl implements GroupLockService {

	@Setter
	private SessionFactory sessionFactory;

	@Override
	@Transactional(readOnly = true)
	public boolean isLocked(String groupId, GroupLock.LockMode lockMode) {
		if(StringUtils.isEmpty(groupId)){
			return false;
		}
		Query query = sessionFactory.getCurrentSession().getNamedQuery("findGroupWithLock");
		query.setString("groupId", groupId);
		List<GroupLock.LockMode> modes = Arrays.asList(lockMode, GroupLock.LockMode.ALL);//by default we check the received mode plus ALL
		if( GroupLock.LockMode.ALL == lockMode ){//if all we should check any
			modes = Stream.of(GroupLock.LockMode.values())
	                .collect(Collectors.toList());
		}
		query.setParameterList("modes", modes);
		List<GroupLock> results = (List<GroupLock>) query.list();
		return CollectionUtils.isNotEmpty(results);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isLockedByItem(String groupId, String itemId) {//an element can only lock a group in one mode, so we are not checking the mode
		if(StringUtils.isEmpty(groupId)){
			return false;
		}
		Query query = sessionFactory.getCurrentSession().getNamedQuery("findGroupLockWithItem");
		query.setString("groupId", groupId);
		query.setString("itemId", itemId);
		List<GroupLock> results = (List<GroupLock>) query.list();
		return CollectionUtils.isNotEmpty(results);
	}

	@Override
	@Transactional
	public void lockGroup(String groupId, String itemId, GroupLock.LockMode lockMode) {
		if(StringUtils.isBlank(groupId) || StringUtils.isBlank(itemId)) {
			log.warn("lockGroup: null or empty group or item id");
			return;
		}

		GroupLock gl = new GroupLock();
		gl.setGroupId(groupId);
		gl.setItemId(itemId);
		gl.setLockMode(lockMode);
		updateGroupLock(gl);
	}

	@Override
	@Transactional
	public void unlockGroup(String groupId, String itemId, GroupLock.LockMode lockMode) {
		if(StringUtils.isBlank(groupId) || StringUtils.isBlank(itemId)) {
			log.warn("lockGroup: null or empty group or item id");
			return;
		}

		GroupLock gl = new GroupLock();
		gl.setGroupId(groupId);
		gl.setItemId(itemId);
		gl.setLockMode(lockMode);
		deleteGroupLock(gl);
	}
	
	@Transactional
	private void deleteGroupLock(GroupLock gl) {
		try {
			sessionFactory.getCurrentSession().delete(sessionFactory.getCurrentSession().merge(gl));
		} catch (Exception e) {
			log.warn("Could not delete GroupLock " + gl + ", " + e.getMessage(), e);
		}
	}

	@Transactional
	private void updateGroupLock(GroupLock gl) {
		if (gl == null) return;
		sessionFactory.getCurrentSession().merge(gl);
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getItemsLockingGroup(String groupId) {
		if(StringUtils.isEmpty(groupId)){
			return null;
		}
		Query query = sessionFactory.getCurrentSession().getNamedQuery("findItemsLockingGroup");
		query.setString("groupId", groupId);
		return (List<String>) query.list();
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getGroupsLockedByItem(String itemId) {
		if(StringUtils.isEmpty(itemId)){
			return null;
		}
		Query query = sessionFactory.getCurrentSession().getNamedQuery("findGroupsLockedByItem");
		query.setString("itemId", itemId);
		return (List<String>) query.list();
	}

}