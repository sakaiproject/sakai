/**
 * 
 */
package org.sakaiproject.dash.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 *
 */
public class AvailabilityCheck implements Serializable {
	
	protected Long id;
	protected String entityReference;
	protected String entityTypeId;
	protected Date scheduledTime;
	
	/**
	 * 
	 */
	public AvailabilityCheck() {
		super();
	}

	/**
	 * @param entityReference
	 * @param entityTypeId TODO
	 * @param scheduledTime
	 */
	public AvailabilityCheck(String entityReference, String entityTypeId, Date scheduledTime) {
		super();
		this.entityReference = entityReference;
		this.scheduledTime = scheduledTime;
		this.entityTypeId = entityTypeId;
	}

	/**
	 * @param id
	 * @param entityReference
	 * @param entityTypeId TODO
	 * @param scheduledTime
	 */
	public AvailabilityCheck(Long id, String entityReference, String entityTypeId, Date scheduledTime) {
		super();
		this.id = id;
		this.entityReference = entityReference;
		this.scheduledTime = scheduledTime;
		this.entityTypeId = entityTypeId;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the entityReference
	 */
	public String getEntityReference() {
		return entityReference;
	}

	/**
	 * @return the entityTypeId
	 */
	public String getEntityTypeId() {
		return entityTypeId;
	}

	/**
	 * @return the scheduledTime
	 */
	public Date getScheduledTime() {
		return scheduledTime;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param entityReference the entityReference to set
	 */
	public void setEntityReference(String entityReference) {
		this.entityReference = entityReference;
	}

	/**
	 * @param entityTypeId the entityTypeId to set
	 */
	public void setEntityTypeId(String entityTypeId) {
		this.entityTypeId = entityTypeId;
	}

	/**
	 * @param scheduledTime the scheduledTime to set
	 */
	public void setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AvailabilityCheck [");
		
		builder.append("id=");
		builder.append(id);
		builder.append(", ");
	
		builder.append("entityReference=");
		builder.append(entityReference);
		builder.append(", ");
	
		builder.append("entityTypeId=");
		builder.append(entityTypeId);
		builder.append(", ");
	
		builder.append("scheduledTime=");
		builder.append(scheduledTime);
		
		builder.append("]");
		return builder.toString();
	}

}
