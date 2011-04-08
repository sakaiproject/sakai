/******************************************************************************
 * DashboardItem.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.model;

import java.io.Serializable;
import java.util.Date;

/**
 * This is a sample POJO (data storage object) 
 * @author Sakai App Builder -AZ
 */
public class DashboardItem implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected Long id;
	protected Integer itemType;
	protected String title;
	protected String description;
	protected String entityId;
	protected String entityType;
	protected String accessUrl;
	protected Date dueDate;
	protected String status;
	protected String locationId; // Sakai locationId
	protected String locationName;
	protected String locationUrl;
	protected String creatorId;// Sakai userId
	protected String creatorName; 
	protected Date createdDate;
	
	/**
	 * Default constructor
	 */
	public DashboardItem() {
	}

	/**
	 * 
	 * @param title
	 * @param description
	 * @param entityId
	 * @param entityType TODO
	 * @param accessUrl
	 * @param locationId
	 * @param creatorId
	 * @param creatorName TODO
	 * @param actions
	 * @param relevantDates
	 */
	public DashboardItem(String title, Integer itemType, String description, String entityId, String entityType, 
			String accessUrl, String locationId, String locationUrl, String locationName, String status,
			Date dueDate, String creatorId, String creatorName) {
		this.title = title;
		this.itemType = itemType;
		this.locationId = locationId;
		this.locationName = locationName;
		this.locationUrl = locationUrl;
		this.entityId = entityId;
		this.accessUrl = accessUrl;
		this.description = description;
		this.status = status;
		this.dueDate = dueDate;
		this.creatorName = creatorName;
		this.creatorId = creatorId;
		this.entityType = entityType;
		
	}

	/**
	 * Getters and Setters
	 */

	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl() {
		return accessUrl;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * @return the creatorId
	 */
	public String getCreatorId() {
		return creatorId;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatorName() {
		return creatorName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the dueDate
	 */
	public Date getDueDate() {
		return dueDate;
	}

	/**
	 * @return the entityId
	 */
	public String getEntityId() {
		return entityId;
	}

	/**
	 * @return the entityType
	 */
	public String getEntityType() {
		return entityType;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the type
	 */
	public Integer getItemType() {
		return itemType;
	}

	/**
	 * @return the locationId
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * @return the locationUrl
	 */
	public String getLocationUrl() {
		return locationUrl;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param accessUrl the accessUrl to set
	 */
	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}

	/**
	 * @param createdDate the creationDate to set
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @param creatorId the creatorId to set
	 */
	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	/**
	 * @param entityType the entityType to set
	 */
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param type the type to set
	 */
	public void setItemType(Integer type) {
		this.itemType = type;
	}

	/**
	 * @param locationId the locationId to set
	 */
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	/**
	 * @param locationName the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * @param locationUrl the locationUrl to set
	 */
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}


}
