/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/content/util/BasicResourceType.java $
 * $Id: BasicResourceType.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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


package org.sakaiproject.content.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;

/**
 * 
 *
 */
public class BasicResourceType implements ResourceType
{
	/**
	 * @author jreng
	 *
	 */
	public interface SizeLabeler 
	{
		/**
		 * Returns null to indicate that the Resources tool should display the byte count 
		 * or member count for the entity (depending on whether the entity is a 
		 * ContentResource or ContentCollection). If a different measure of the "size" of 
		 * the entity is needed, overrid this method to return a short string (no more than 
		 * 18 characters) describing the "size" of the entity as appropriate.
		 */
		public String getSizeLabel(ContentEntity entity);

		/**
		 * Returns null to indicate that the Resources tool should display the byte count 
		 * or member count for the entity (depending on whether the entity is a 
		 * ContentResource or ContentCollection). If a different measure of the "size" of 
		 * the entity is needed, overrid this method to return a short string (no more than 
		 * 80 characters) describing the "size" of the entity as appropriate.
		 */
		public String getLongSizeLabel(ContentEntity entity);

	}

	/**
	 * Localizer provides a way for the registrant to take charge of localizing labels 
	 * without extending BasicResourceType.  In defining types, a registrant can create
	 * instances of BasicResourceType, implement the Localizer interface with methods
	 * that provide localized strings, and set the localizer.  Subsequent invocation of
	 * BasicResourceType.getLabel() and BasicResourceType.getLocalizedHoverText(entity) 
	 * will use the Localizer to supply labels.
	 */
	public interface Localizer
	{
		/**
		 *
		 * @return
		 */
		public String getLabel();
		
		/**
		 *
		 * @param member
		 * @return
		 */
		public String getLocalizedHoverText(ContentEntity entity);
	}
	
	protected Map<ActionType,List<ResourceToolAction>> actionTypeMap = new HashMap<ActionType,List<ResourceToolAction>>();
	
	protected Map<String, ResourceToolAction> actionIdMap = new HashMap<String, ResourceToolAction>();

	protected String id;

	protected String iconLocation;
	protected String iconClass;
	
	protected Localizer localizer = null;
	protected SizeLabeler sizeLabeler = null;
	
	protected boolean hasRightsDialog = true;
	protected boolean hasPublicDialog = true;
	protected boolean hasOptionalPropertiesDialog = true;
	protected boolean hasNotificationDialog = true;
	protected boolean hasGroupsDialog = true;
	protected boolean hasDescription = true;
	protected boolean hasAvailabilityDialog = true;

	/**
	 * 
	 */
	public BasicResourceType(String id)
	{
		this.id = id;
	}
	
	/**
	 *
	 * @param action
	 */
	public void addAction(ResourceToolAction action)
	{
		this.actionIdMap.put(action.getId(), action);
		List<ResourceToolAction> actionList = this.actionTypeMap.get(action.getActionType());
		if(actionList == null)
		{
			actionList = new Vector<ResourceToolAction>();
			this.actionTypeMap.put(action.getActionType(), actionList);
		}
		actionList.add(action);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getAction(java.lang.String)
	 */
	public ResourceToolAction getAction(String actionId)
	{
		return this.actionIdMap.get(actionId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(org.sakaiproject.content.api.ResourceToolAction.ActionType)
	 */
	public List<ResourceToolAction> getActions(ActionType type)
	{
		List<ResourceToolAction> rv = new Vector<ResourceToolAction>();
		if(this.actionTypeMap.get(type) != null)
		{
			rv.addAll(this.actionTypeMap.get(type));
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getActions(java.util.List)
	 */
	public List<ResourceToolAction> getActions(List<ActionType> types)
	{
		List<ResourceToolAction> rv = new Vector<ResourceToolAction>();
		if(types == null)
		{
			return rv;
		}
		Iterator<ActionType> typeIt = types.iterator();
		while(typeIt.hasNext())
		{
			ActionType type = typeIt.next();
			rv.addAll(getActions(type));
			
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getIconLocation()
	 */
	public String getIconLocation(ContentEntity entity)
	{
		return iconLocation;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getIconClass()
	 */
	public String getIconClass(ContentEntity entity)
	{
		return iconClass;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getId()
	 */
	public String getId()
	{
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLabel()
	 */
	public String getLabel()
	{
		String rv = null;
		if(this.localizer != null)
		{
			rv = this.localizer.getLabel();
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceType#getLocalizedHoverText(org.sakaiproject.content.api.ContentEntity)
	 */
	public String getLocalizedHoverText(ContentEntity entity)
	{
		String rv = null;
		if(this.localizer != null)
		{
			rv = this.localizer.getLocalizedHoverText(entity);
		}
		return rv;
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasAvailabilityDialog()
     */
    public boolean hasAvailabilityDialog()
    {
	    return hasAvailabilityDialog;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasDescription()
     */
    public boolean hasDescription()
    {
	    return hasDescription;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasGroupsDialog()
     */
    public boolean hasGroupsDialog()
    {
	    return hasGroupsDialog;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasNotificationDialog()
     */
    public boolean hasNotificationDialog()
    {
	    return hasNotificationDialog;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasOptionalPropertiesDialog()
     */
    public boolean hasOptionalPropertiesDialog()
    {
	    return hasOptionalPropertiesDialog;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasPublicDialog()
     */
    public boolean hasPublicDialog()
    {
	    return hasPublicDialog;
    }

	/* (non-Javadoc)
     * @see org.sakaiproject.content.api.ResourceType#hasRightsDialog()
     */
    public boolean hasRightsDialog()
    {
	    return hasRightsDialog;
    }

	/**
     * @param hasAvailabilityDialog the hasAvailabilityDialog to set
     */
    public void setHasAvailabilityDialog(boolean hasAvailabilityDialog)
    {
    	this.hasAvailabilityDialog = hasAvailabilityDialog;
    }

	/**
     * @param hasDescription the hasDescription to set
     */
    public void setHasDescription(boolean hasDescription)
    {
    	this.hasDescription = hasDescription;
    }

	/**
     * @param hasGroupsDialog the hasGroupsDialog to set
     */
    public void setHasGroupsDialog(boolean hasGroupsDialog)
    {
    	this.hasGroupsDialog = hasGroupsDialog;
    }

	/**
     * @param hasNotificationDialog the hasNotificationDialog to set
     */
    public void setHasNotificationDialog(boolean hasNotificationDialog)
    {
    	this.hasNotificationDialog = hasNotificationDialog;
    }

	/**
     * @param hasOptionalPropertiesDialog the hasOptionalPropertiesDialog to set
     */
    public void setHasOptionalPropertiesDialog(boolean hasOptionalPropertiesDialog)
    {
    	this.hasOptionalPropertiesDialog = hasOptionalPropertiesDialog;
    }

	/**
     * @param hasPublicDialog the hasPublicDialog to set
     */
    public void setHasPublicDialog(boolean hasPublicDialog)
    {
    	this.hasPublicDialog = hasPublicDialog;
    }

	/**
     * @param hasRightsDialog the hasRightsDialog to set
     */
    public void setHasRightsDialog(boolean hasRightsDialog)
    {
    	this.hasRightsDialog = hasRightsDialog;
    }

	/**
     * @param iconLocation the iconLocation to set
     */
    public void setIconLocation(String iconLocation)
    {
    	this.iconLocation = iconLocation;
    }
    
	/**
     * @param iconClass the iconClass to set
     */
    public void setIconClass(String iconClass)
    {
    	this.iconClass = iconClass;
    }

	/**
     * @param id the id to set
     */
    public void setId(String id)
    {
    	this.id = id;
    }

	/**
     * @param localizer the localizer to set
     */
    public void setLocalizer(Localizer localizer)
    {
    	this.localizer = localizer;
    }

	public boolean isExpandable()
    {
	    return (this instanceof ExpandableResourceType);
    }

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 18 characters) describing the "size" of the entity as appropriate.
	 */
	public String getSizeLabel(ContentEntity entity) 
	{
		String label = null;
		if(this.sizeLabeler != null)
		{
			label = this.sizeLabeler.getSizeLabel(entity);
		}
		return label;
	}

	/**
	 * Returns null to indicate that the Resources tool should display the byte count 
	 * or member count for the entity (depending on whether the entity is a 
	 * ContentResource or ContentCollection). If a different measure of the "size" of 
	 * the entity is needed, overrid this method to return a short string (no more than 
	 * 80 characters) describing the "size" of the entity as appropriate.
	 */
	public String getLongSizeLabel(ContentEntity entity) 
	{
		String label = null;
		if(this.sizeLabeler != null)
		{
			label = this.sizeLabeler.getLongSizeLabel(entity);
		}
		return label;
	}

	/**
	 * Provide a SizeLabeler object to supply custom implementations of 
	 * getSizeLabel(ContentEntity) and getLongSizeLabel(ContentEntity). 
	 * @param labeler
	 */
	public void setSizeLabeler(SizeLabeler labeler) 
	{
		this.sizeLabeler = labeler;
	}

}
