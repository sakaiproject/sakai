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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.time.api.Time;

@Slf4j
public class ResourcesItem
{
	protected byte[] content;
	protected String contentType;
	protected String resourceType;
	protected boolean collection = false;
	protected AccessMode accessMode = AccessMode.INHERITED;
	protected String createdBy;
	protected Time createdTime;
	protected String displayName;
	protected String entityId;
	protected String collectionId;
	protected String siteCollectionId;
	protected Set groups;
	protected String modifiedBy;
	protected Time modifiedTime;
	protected Map propertyValues = new Hashtable();
	protected int prioritySortOrder;
	protected Time retractDate;
	protected String uuid;
	protected int version;
	protected boolean hasPrioritySort = false;
	protected boolean hidden = false;
	protected Time releaseDate;
	protected int notification;
	protected boolean hasQuota = false;
	protected boolean canSetQuota = false;
	protected String quota;
	protected String description;
	protected boolean useReleaseDate;
	protected boolean useRetractDate;
	protected String copyrightStatus;
	protected String copyrightInfo;
	protected boolean copyrightAlert = false;
	
	/**
	 * @param entityId
	 * @param collectionId
	 * @param propertyValues
	 * @param resourceType
	 */
	public ResourcesItem(String entityId, String collectionId, String resourceType, Map propertyValues)
	{
		super();
		this.entityId = entityId;
		this.collectionId = collectionId;
		this.resourceType = resourceType;
		if(propertyValues != null)
		{
			this.propertyValues.putAll(propertyValues);
		}
	}
	
	public ResourcesItem(ContentEntity entity)
	{
		ResourceProperties props = entity.getProperties();
		this.accessMode = entity.getAccess();
		//this.canSetQuota = 
		//this.collection = 
		this.collectionId = entity.getContainingCollection().getId();
		this.createdBy = props.getProperty(ResourceProperties.PROP_CREATOR);
		this.modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
		this.entityId = entity.getId();
		
		try
		{
			this.createdTime = props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
			this.modifiedTime = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
		}
		catch (EntityPropertyNotDefinedException e1)
		{
			// TODO Auto-generated catch block
			log.warn("EntityPropertyNotDefinedException for createdTime or modifiedTime of " + this.entityId);
		}
		catch (EntityPropertyTypeException e1)
		{
			// TODO Auto-generated catch block
			log.warn("EntityPropertyTypeException for createdTime or modifiedTime of " + this.entityId);
		}
		this.displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		this.description = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
		
		this.groups = new TreeSet( entity.getGroupObjects() );
		// this.hasQuota = 
		this.hidden = entity.isHidden();
		// this.notification = entity.
		// this.prioritySortOrder = props.getLongProperty()
		// this.propertyValues = props;
		// this.quota = 
		this.releaseDate = entity.getReleaseDate();
		this.retractDate = entity.getRetractDate();
		this.useReleaseDate = (this.releaseDate != null);
		this.useRetractDate = (this.retractDate != null);
		this.resourceType = entity.getResourceType();
		// this.siteCollectionId = 
		// this.version
		
		if(entity.isCollection())
		{
			ContentCollection collection = (ContentCollection) entity;
			// this.hasPrioritySort = collection.;
			this.collection = true;
		}
		else
		{
			this.collection = false;
			ContentResource resource = (ContentResource) entity;
			try
			{
				this.content = resource.getContent();
			}
			catch (ServerOverloadException e)
			{
				// TODO Auto-generated catch block
				log.warn("ServerOverloadException ", e);
			}
			this.copyrightStatus = props.getProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
			this.copyrightInfo = props.getProperty(ResourceProperties.PROP_COPYRIGHT);
			String crAlert = props.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
			if(crAlert != null && Boolean.TRUE.toString().equalsIgnoreCase(crAlert))
			{
				this.copyrightAlert = true;
			}
			else
			{
				this.copyrightAlert = false;
			}
			
			// this.uuid = resource.
		}			
	}
	
	public ResourcesItem(String entityId, String collectionId, String resourceType, ResourceToolActionPipe pipe)
	{
		super();
		this.entityId = entityId;
		this.collectionId = collectionId;
		this.resourceType = resourceType;
		this.content = pipe.getContent();
		this.propertyValues.putAll(pipe.getRevisedResourceProperties());
		
		if(pipe.getRevisedMimeType() == null)
		{
			// this.propertyValues.remove(ResourceProperties.PROP_CONTENT_TYPE);
		}
		else
		{
			this.propertyValues.put(ResourceProperties.PROP_CONTENT_TYPE, pipe.getRevisedMimeType());
		}
	}
	
	public void update(ResourceToolActionPipe pipe)
	{
		// TODO: update the ResourcesItem based on the pipe
	}

	/**
	 * @return the useRetractDate
	 */
	public boolean useRetractDate()
	{
		return this.useRetractDate;
	}

	/**
	 * @param useRetractDate the useRetractDate to set
	 */
	public void setUseRetractDate(boolean useRetractDate)
	{
		this.useRetractDate = useRetractDate;
	}

	/**
	 * @return the useReleaseDate
	 */
	public boolean useReleaseDate()
	{
		return this.useReleaseDate;
	}

	/**
	 * @param useReleaseDate the useReleaseDate to set
	 */
	public void setUseReleaseDate(boolean useReleaseDate)
	{
		this.useReleaseDate = useReleaseDate;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the accessMode
	 */
	public AccessMode getAccessMode()
	{
		return this.accessMode;
	}
	
	/**
	 * @param accessMode the accessMode to set
	 */
	public void setAccessMode(AccessMode accessMode)
	{
		this.accessMode = accessMode;
	}
	
	/**
	 * @return the canSetQuota
	 */
	public boolean isCanSetQuota()
	{
		return this.canSetQuota;
	}
	
	/**
	 * @param canSetQuota the canSetQuota to set
	 */
	public void setCanSetQuota(boolean canSetQuota)
	{
		this.canSetQuota = canSetQuota;
	}
	
	/**
	 * @return the collection
	 */
	public boolean isCollection()
	{
		return this.collection;
	}
	
	/**
	 * @param collection the collection to set
	 */
	public void setCollection(boolean collection)
	{
		this.collection = collection;
	}
	
	/**
	 * @return the collectionId
	 */
	public String getCollectionId()
	{
		return this.collectionId;
	}
	
	/**
	 * @param collectionId the collectionId to set
	 */
	public void setCollectionId(String collectionId)
	{
		this.collectionId = collectionId;
	}
	
	/**
	 * @return the content
	 */
	public byte[] getContent()
	{
		return this.content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(byte[] content)
	{
		this.content = content;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy()
	{
		return this.createdBy;
	}
	
	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}
	
	/**
	 * @return the createdTime
	 */
	public Time getCreatedTime()
	{
		return this.createdTime;
	}
	
	/**
	 * @param createdTime the createdTime to set
	 */
	public void setCreatedTime(Time createdTime)
	{
		this.createdTime = createdTime;
	}
	
	/**
	 * @return the displayName
	 */
	public String getDisplayName()
	{
		return this.displayName;
	}
	
	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	/**
	 * @return the entityId
	 */
	public String getEntityId()
	{
		return this.entityId;
	}
	
	/**
	 * @param entityId the entityId to set
	 */
	public void setEntityId(String entityId)
	{
		this.entityId = entityId;
	}
	
	/**
	 * @return the groups
	 */
	public Set getGroups()
	{
		return this.groups;
	}
	
	/**
	 * @param groups the groups to set
	 */
	public void setGroups(Set groups)
	{
		this.groups = groups;
	}
	
	/**
	 * @return the hasPrioritySort
	 */
	public boolean hasPrioritySort()
	{
		return this.hasPrioritySort;
	}
	/**
	 * @param hasPrioritySort the hasPrioritySort to set
	 */
	public void setHasPrioritySort(boolean hasPrioritySort)
	{
		this.hasPrioritySort = hasPrioritySort;
	}
	
	/**
	 * @return the hasQuota
	 */
	public boolean hasQuota()
	{
		return this.hasQuota;
	}
	
	/**
	 * @param hasQuota the hasQuota to set
	 */
	public void setHasQuota(boolean hasQuota)
	{
		this.hasQuota = hasQuota;
	}
	
	/**
	 * @return the hidden
	 */
	public boolean isHidden()
	{
		return this.hidden;
	}
	
	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	
	/**
	 * @return the modifiedBy
	 */
	public String getModifiedBy()
	{
		return this.modifiedBy;
	}
	
	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy)
	{
		this.modifiedBy = modifiedBy;
	}
	
	/**
	 * @return the modifiedTime
	 */
	public Time getModifiedTime()
	{
		return this.modifiedTime;
	}
	
	/**
	 * @param modifiedTime the modifiedTime to set
	 */
	public void setModifiedTime(Time modifiedTime)
	{
		this.modifiedTime = modifiedTime;
	}
	
	/**
	 * @return the notification
	 */
	public int getNotification()
	{
		return this.notification;
	}
	
	/**
	 * @param notification the notification to set
	 */
	public void setNotification(int notification)
	{
		this.notification = notification;
	}
	
	/**
	 * @return the prioritySortOrder
	 */
	public int getPrioritySortOrder()
	{
		return this.prioritySortOrder;
	}
	
	/**
	 * @param prioritySortOrder the prioritySortOrder to set
	 */
	public void setPrioritySortOrder(int prioritySortOrder)
	{
		this.prioritySortOrder = prioritySortOrder;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public String getPropertyValue(String name)
	{
		String rv = null;
		Object value = this.propertyValues.get(name);
		if(value == null)
		{
			// do nothing, return null 
		}
		else if (value instanceof String)
		{
			rv = (String) value;
		}
		else if (value instanceof List)
		{
			List list = (List) value;
			if(list.isEmpty())
			{
				// do nothing, return null
			}
			else
			{
				rv = (String) list.get(0);
			}
		}
		return rv;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public List getPropertyValues(String name)
	{
		List rv = new Vector();
		Object value = this.propertyValues.get(name);
		if(value == null)
		{
			// do nothing, return empty list 
		}
		else if (value instanceof String)
		{
			rv.add(value);
		}
		else if (value instanceof List)
		{
			rv.addAll((Collection) value);
		}
		return rv;
	}
	
	
	
	/**
	 * @param name
	 * @param value
	 */
	public void setPropertyValue(String name, List value)
	{
		this.propertyValues.put(name, value);
	}
	
	/**
	 * @param name
	 * @param index
	 * @param value
	 */
	public void setPropertyValue(String name, int index, String value)
	{
		Object obj = this.propertyValues.get(name);
		if(obj != null && obj instanceof List)
		{
			List list = (List) obj;
			if(index < 0)
			{
				// throw exception??
			}
			else if(index < list.size())
			{
				list.add(index, value);
			}
			else if(index == list.size())
			{
				list.add(value);
			}
			else
			{
				// throw exception??
			}
		}
		else 
		{
			if(index > 0)
			{
				// throw exception??
			}
			else
			{
				List list = new Vector();
				this.propertyValues.put(name, list);
				list.add(value);
			}
		}
	}
	
	/**
	 * @return the propertyValues
	 */
	public Map getPropertyValues()
	{
		return this.propertyValues;
	}
	
	/**
	 * @param propertyValues the propertyValues to set
	 */
	public void setPropertyValues(Map propertyValues)
	{
		this.propertyValues = propertyValues;
	}
	
	/**
	 * @return the quota
	 */
	public String getQuota()
	{
		return this.quota;
	}
	
	/**
	 * @param quota the quota to set
	 */
	public void setQuota(String quota)
	{
		this.quota = quota;
	}
	
	/**
	 * @return the releaseDate
	 */
	public Time getReleaseDate()
	{
		return this.releaseDate;
	}
	
	/**
	 * @param releaseDate the releaseDate to set
	 */
	public void setReleaseDate(Time releaseDate)
	{
		this.releaseDate = releaseDate;
	}
	
	/**
	 * @return the resourceType
	 */
	public String getResourceType()
	{
		return this.resourceType;
	}
	
	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType)
	{
		this.resourceType = resourceType;
	}
	
	/**
	 * @return the retractDate
	 */
	public Time getRetractDate()
	{
		return this.retractDate;
	}
	
	/**
	 * @param retractDate the retractDate to set
	 */
	public void setRetractDate(Time retractDate)
	{
		this.retractDate = retractDate;
	}
	
	/**
	 * @return the siteCollectionId
	 */
	public String getSiteCollectionId()
	{
		return this.siteCollectionId;
	}
	
	/**
	 * @param siteCollectionId the siteCollectionId to set
	 */
	public void setSiteCollectionId(String siteCollectionId)
	{
		this.siteCollectionId = siteCollectionId;
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid()
	{
		return this.uuid;
	}
	
	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}
	
	/**
	 * @return the version
	 */
	public int getVersion()
	{
		return this.version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}

	/**
     * @return the copyrightAlert
     */
    public boolean hasCopyrightAlert()
    {
    	return copyrightAlert;
    }

	/**
     * @param copyrightAlert the copyrightAlert to set
     */
    public void setCopyrightAlert(boolean copyrightAlert)
    {
    	this.copyrightAlert = copyrightAlert;
    }

	/**
     * @return the copyrightInfo
     */
    public String getCopyrightInfo()
    {
    	return copyrightInfo;
    }

	/**
     * @param copyrightInfo the copyrightInfo to set
     */
    public void setCopyrightInfo(String copyrightInfo)
    {
    	this.copyrightInfo = copyrightInfo;
    }

	/**
     * @return the copyrightStatus
     */
    public String getCopyrightStatus()
    {
    	return copyrightStatus;
    }

	/**
     * @param copyrightStatus the copyrightStatus to set
     */
    public void setCopyrightStatus(String copyrightStatus)
    {
    	this.copyrightStatus = copyrightStatus;
    }

	/**
     * @return the contentType
     */
    public String getContentType()
    {
    	return contentType;
    }

	/**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType)
    {
    	this.contentType = contentType;
    }

}