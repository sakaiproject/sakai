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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl.test;

import java.time.Instant;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;

/**
 * MockContentEntity
 *
 */
public class MockContentEntity implements ContentEntity, GroupAwareEdit
{
	protected String entityId;
	protected String containingCollectionId;
	protected String reference;
	protected String resourceType;
	protected ResourceProperties resourceProperties;
	protected AccessMode accessMode;
	protected AccessMode inheritedAccess;
	protected Map<String, Group> groupMap = new HashMap<String, Group>();
	protected Map<String, Group> inheritedGroupMap = new HashMap<String, Group>();
	protected boolean isAvailable;
	protected boolean isHidden;
	protected Time releaseDate;
	protected Time retractDate;
	protected boolean isPublic;
	protected boolean inheritsPubview;
	protected Map<String, MockContentEntity> memberMap = new HashMap<String, MockContentEntity>();
	protected boolean isActiveEdit;
	protected Set<String> roleIds = new LinkedHashSet<String>();

	public MockContentEntity() {
		this.resourceProperties = new BaseResourcePropertiesEdit();
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getContainingCollection()
	 */
	public ContentCollection getContainingCollection()
	{
		MockContentCollection rv = new MockContentCollection(this.containingCollectionId);
		
		// need to add members?
		
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getMember(java.lang.String)
	 */
	// TODO: why is this in ContentEntity????
	public ContentEntity getMember(String nextId)
	{
		return this.memberMap.get(nextId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getContentHandler()
	 */
	public ContentHostingHandler getContentHandler()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#setContentHandler(org.sakaiproject.content.api.ContentHostingHandler)
	 */
	public void setContentHandler(ContentHostingHandler chh)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#setVirtualContentEntity(org.sakaiproject.content.api.ContentEntity)
	 */
	public void setVirtualContentEntity(ContentEntity ce)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getUrl(boolean)
	 */
	public String getUrl(boolean relative)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getVirtualContentEntity()
	 */
	public ContentEntity getVirtualContentEntity()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference(java.lang.String)
	 */
	public String getReference(String rootProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getUrl(java.lang.String)
	 */
	public String getUrl(String rootProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#toXml(org.w3c.dom.Document, java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setPublicAccess()
	 */
	public void setPublicAccess() throws InconsistentException, PermissionException
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#getResourceType()
	 */
	public String getResourceType()
	{
		return this.resourceType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#isCollection()
	 */
	public boolean isCollection()
	{
		return (this instanceof ContentCollection);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentEntity#isResource()
	 */
	public boolean isResource()
	{
		return (this instanceof ContentResource);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getAccess()
	 */
	public AccessMode getAccess()
	{
		return this.accessMode;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getGroupObjects()
	 */
	public Collection getGroupObjects()
	{
		return this.groupMap.values();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getGroups()
	 */
	public Collection getGroups()
	{
		return this.groupMap.keySet();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedAccess()
	 */
	public AccessMode getInheritedAccess()
	{
		return this.inheritedAccess;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedGroupObjects()
	 */
	public Collection getInheritedGroupObjects()
	{
		return this.inheritedGroupMap.values();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedGroups()
	 */
	public Collection getInheritedGroups()
	{
		return this.inheritedGroupMap.keySet();
	}

	public Set<String> getRoleAccessIds() {
		return roleIds;
	}

	public Set<String> getInheritedRoleAccessIds() {
		return new LinkedHashSet<String>();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getReleaseDate()
	 */
	public Time getReleaseDate()
	{
		return this.releaseDate;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#getRetractDate()
	 */
	public Time getRetractDate()
	{
		return this.retractDate;
	}
	
	
	
	public Date getReleaseTime() {
		
		return new Date(this.releaseDate.getTime());
	}

	
	public Date getRetractTime() {
		return new Date(this.getRetractDate().getTime());
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#isAvailable()
	 */
	public boolean isAvailable()
	{
		return this.isAvailable;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEntity#isHidden()
	 */
	public boolean isHidden()
	{
		return this.isHidden;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getId()
	 */
	public String getId()
	{
		return this.entityId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		return this.resourceProperties;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Entity#getReference()
	 */
	public String getReference()
	{
		return this.reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#clearGroupAccess()
	 */
	public void clearGroupAccess() throws InconsistentException, PermissionException
	{
		if(false)
		{
			throw new PermissionException("userId", "content.revise", this.entityId);
		}
		if(this.accessMode != AccessMode.GROUPED)
		{
			throw new InconsistentException(entityId);
		}
		this.accessMode = AccessMode.INHERITED;
		this.groupMap.clear();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#clearPublicAccess()
	 */
	public void clearPublicAccess() throws PermissionException
	{
		if(! this.isPublic)
		{
			throw new PermissionException(null, null, entityId);
		}
		this.isPublic = false;
		this.accessMode = AccessMode.INHERITED;
		this.groupMap.clear();
		
	}

	public void addRoleAccess(String roleId) throws InconsistentException, PermissionException {
		roleIds.add(roleId);
	}

	public void removeRoleAccess(String roleId) throws InconsistentException, PermissionException {
		roleIds.remove(roleId);
	}

	public void clearRoleAccess() throws PermissionException {
		roleIds.clear();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setAvailability(boolean, org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time)
	 */
	public void setAvailability(boolean hidden, Time releaseDate, Time retractDate)
	{
		this.isHidden = hidden;
		if(hidden)
		{
			this.releaseDate = null;
			this.retractDate = null;
		}
		else
		{
			if(releaseDate != null)
			{
				this.releaseDate = TimeService.newTime(releaseDate.getTime());
			}
			if(retractDate != null)
			{
				this.retractDate = TimeService.newTime(retractDate.getTime());
			}
		}
	}

	@Override
	public void setAvailabilityInstant(boolean hidden, Instant releaseDate, Instant retractDate) {
		setAvailability(hidden, TimeService.newTime(releaseDate.toEpochMilli()), TimeService.newTime(retractDate.toEpochMilli()));	
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setGroupAccess(java.util.Collection)
	 */
	public void setGroupAccess(Collection groups) throws InconsistentException, PermissionException
	{
		if(false)
		{
			throw new PermissionException("userId", "content.revise", this.entityId);
		}
		if(groups == null || groups.isEmpty() || this.inheritsPubview)
		{
			throw new InconsistentException(entityId);
		}
		
		this.groupMap.clear();
		
		for(String groupId : (Collection<String>) groups)
		{
			Group group = SiteService.findGroup(groupId);
			if(group != null)
			{
				this.groupMap.put(groupId, group);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setHidden()
	 */
	public void setHidden()
	{
		this.isHidden = true;
		this.releaseDate = null;
		this.retractDate = null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setReleaseDate(org.sakaiproject.time.api.Time)
	 */
	public void setReleaseDate(Time time)
	{
		this.releaseDate = TimeService.newTime(time.getTime());
	}

	public void setReleaseTime(Date time) 
	{
		this.releaseDate = TimeService.newTime(time.getTime());
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setResourceType(java.lang.String)
	 */
	public void setResourceType(String typeId)
	{
		this.resourceType = typeId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.GroupAwareEdit#setRetractDate(org.sakaiproject.time.api.Time)
	 */
	public void setRetractDate(Time time)
	{
		this.retractDate = TimeService.newTime(time.getTime());
	}
	
	public void setRetractTime(Date time)
	{
		this.retractDate = TimeService.newTime(time.getTime());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#getPropertiesEdit()
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		return (ResourcePropertiesEdit) this.resourceProperties;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Edit#isActiveEdit()
	 */
	public boolean isActiveEdit()
	{
		return this.isActiveEdit;
	}
	@Override
	public Instant getReleaseInstant() {
		return Instant.ofEpochMilli(this.releaseDate.getTime());
	}
	@Override
	public Instant getRetractInstant() {
		return Instant.ofEpochMilli(this.releaseDate.getTime());
	}
	@Override
	public void setReleaseInstant(Instant date) {
		this.releaseDate = TimeService.newTime(date.toEpochMilli());
		
	}
	@Override
	public void setRetractInstant(Instant time) {
		this.retractDate = TimeService.newTime(time.toEpochMilli());
		
	}
}
