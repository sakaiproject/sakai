package org.sakaiproject.site.api;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SAKAI_GROUP_LOCK")
@NamedQueries({
	@NamedQuery(name = "findGroupWithLock", query = "from GroupLock where groupId = :groupId and lockMode IN :modes"),
	@NamedQuery(name = "findGroupLockWithItem", query = "from GroupLock where groupId = :groupId and itemId = :itemId "),
	@NamedQuery(name = "findItemsLockingGroup", query = "select itemId from GroupLock where groupId = :groupId"),
	@NamedQuery(name = "findGroupsLockedByItem", query = "select itemId from GroupLock where itemId = :itemId")
})

@Data
@NoArgsConstructor
@IdClass(GroupLock.class)
public class GroupLock implements Serializable {

	private static final long serialVersionUID = 4120846768406071240L;

	public enum LockMode {ALL, MODIFY, DELETE};//ALL acts like all modes when locking and as ANY (checks all) when querying

	@Id
	@Column(name = "GROUP_ID", length = 99, nullable = false)
	private String groupId;

	@Id
	@Column(name = "ITEM_ID", length = 200, nullable = false)
	private String itemId;

	@Id
	@Column(name = "LOCK_MODE", length = 32, nullable = false)
	@Enumerated(value = EnumType.STRING)
	private LockMode lockMode;

	public GroupLock(String groupId, LockMode lockMode, String itemId) {
		this.groupId = groupId;
		this.lockMode = lockMode;
		this.itemId = itemId;
	}
}
