/**
 * 
 */
package org.sakaiproject.dash.logic;

import java.util.Date;

/**
 * 
 *
 */
public class TaskLock {
	
	public static final String CHECK_AVAILABILITY_OF_HIDDEN_ITEMS = "CHECK_AVAILABILITY_OF_HIDDEN_ITEMS";
	public static final String EXPIRE_AND_PURGE_OLD_DASHBOARD_ITEMS = "EXPIRE_AND_PURGE_OLD_DASHBOARD_ITEMS";
	public static final String UPDATE_REPEATING_EVENTS = "UPDATE_REPEATING_EVENTS";
	
	protected Long id = 0L;
	protected String task;
	protected String serverId;
	protected Date claimTime;
	protected boolean hasLock = false;
	protected Date lastUpdate;
	
	/**
	 * @param id
	 * @param task
	 * @param serverId
	 * @param claimTime
	 * @param hasLock
	 * @param lastUpdate
	 */
	public TaskLock(Long id, String task, String serverId, Date claimTime,
			boolean hasLock, Date lastUpdate) {
		super();
		this.id = id;
		this.task = task;
		this.serverId = serverId;
		this.claimTime = claimTime;
		this.hasLock = hasLock;
		this.lastUpdate = lastUpdate;
	}

	/**
	 * 
	 * @param task
	 * @param serverId
	 * @param claimTime
	 * @param hasLock
	 * @param lastUpdate
	 */
	public TaskLock(String task, String serverId, Date claimTime,
			boolean hasLock, Date lastUpdate) {
		super();
		this.task = task;
		this.serverId = serverId;
		this.claimTime = claimTime;
		this.hasLock = hasLock;
		this.lastUpdate = lastUpdate;
	}
	
	

	/**
	 * 
	 */
	public TaskLock() {
		super();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the task
	 */
	public String getTask() {
		return task;
	}

	/**
	 * @return the serverId
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * @return the claimTime
	 */
	public Date getClaimTime() {
		return claimTime;
	}

	/**
	 * @return the hasLock
	 */
	public boolean isHasLock() {
		return hasLock;
	}

	/**
	 * @return the lastUpdate
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(String task) {
		this.task = task;
	}

	/**
	 * @param serverId the serverId to set
	 */
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	/**
	 * @param claimTime the claimTime to set
	 */
	public void setClaimTime(Date claimTime) {
		this.claimTime = claimTime;
	}

	/**
	 * @param hasLock the hasLock to set
	 */
	public void setHasLock(boolean hasLock) {
		this.hasLock = hasLock;
	}

	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskLock [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (task != null) {
			builder.append("task=");
			builder.append(task);
			builder.append(", ");
		}
		if (serverId != null) {
			builder.append("serverId=");
			builder.append(serverId);
			builder.append(", ");
		}
		if (claimTime != null) {
			builder.append("claimTime=");
			builder.append(claimTime);
			builder.append(", ");
		}
		builder.append("hasLock=");
		builder.append(hasLock);
		builder.append(", ");
		if (lastUpdate != null) {
			builder.append("lastUpdate=");
			builder.append(lastUpdate);
		}
		builder.append("]");
		return builder.toString();
	}
	
}
