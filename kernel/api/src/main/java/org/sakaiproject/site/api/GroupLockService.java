package org.sakaiproject.site.api;

import java.util.List;

public interface GroupLockService {

	public boolean isLocked(String groupId, GroupLock.LockMode lockMode);
	public boolean isLockedByItem(String groupId, String itemId);
	public void lockGroup(String groupId, String itemId, GroupLock.LockMode lockMode);
	public void unlockGroup(String groupId, String itemId, GroupLock.LockMode lockMode);

	public List<String> getItemsLockingGroup(String groupId);
	public List<String> getGroupsLockedByItem(String itemId);

}
