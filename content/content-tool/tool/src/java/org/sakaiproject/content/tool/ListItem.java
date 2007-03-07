/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * ListItem
 *
 */
public class ListItem
{
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");

	/** Resource bundle using current language locale */
    private static ResourceLoader trb = new ResourceLoader("types");

    private static final Log logger = LogFactory.getLog(ResourcesAction.class);
    

	protected String name;
	protected String id;
	protected List<ResourceToolAction> addActions;
	protected List<ResourceToolAction> otherActions;
	protected String otherActionsLabel;
	protected List<ListItem> members;
	protected Set<ContentPermissions> permissions;
	protected boolean selected;
	protected boolean collection;
	protected String hoverText;
	protected String accessUrl;
	protected String iconLocation;
	protected String mimetype;
	protected String resourceType;
	protected boolean isEmpty = true;
	protected boolean isExpanded = false;
	protected boolean isSortable = false;
	protected boolean isTooBig = false;
	protected String size = "";
	protected String createdBy;
	protected String modifiedTime;
	protected int depth;

	protected boolean canSelect = false;
	
	
	/**
	 * @param entity
	 */
	public ListItem(ContentEntity entity)
	{
		ResourceProperties props = entity.getProperties();
		this.accessUrl = entity.getUrl();
		this.collection = entity.isCollection();
		this.id = entity.getId();
		this.name = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		this.permissions = new TreeSet<ContentPermissions>();
		this.selected = false;
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = entity.getResourceType();
		ResourceType typeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(typeDef != null)
		{
			this.hoverText = typeDef.getLocalizedHoverText(entity);
			this.iconLocation = typeDef.getIconLocation();
			String[] args = { typeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
		}

		if(this.collection)
		{
			ContentCollection collection = (ContentCollection) entity;
        	int collection_size = collection.getMemberCount();
        	if(collection_size == 1)
        	{
        		setSize(rb.getString("size.item"));
        	}
        	else
        	{
	        	String[] args = { Integer.toString(collection_size) };
	        	setSize(rb.getFormattedMessage("size.items", args));
        	}
			setIsEmpty(collection_size < 1);
			setSortable(ContentHostingService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < ResourcesAction.EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(collection_size > ResourcesAction.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				setIsTooBig(true);
			}
		}
		else 
		{
			ContentResource resource = (ContentResource) entity;
			this.mimetype = resource.getContentType();
			if(this.mimetype == null)
			{
				
			}
			if(this.iconLocation == null)
			{
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
			String size = "";
			if(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH) != null)
			{
				long size_long = 0;
                try
                {
	                size_long = props.getLongProperty(ResourceProperties.PROP_CONTENT_LENGTH);
                }
                catch (EntityPropertyNotDefinedException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("EntityPropertyNotDefinedException for size of " + this.id);
                }
                catch (EntityPropertyTypeException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("EntityPropertyTypeException for size of " + this.id);
                }
				NumberFormat formatter = NumberFormat.getInstance(rb.getLocale());
				formatter.setMaximumFractionDigits(1);
				if(size_long > 700000000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)) };
					size = rb.getFormattedMessage("size.gb", args);
				}
				else if(size_long > 700000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L)) };
					size = rb.getFormattedMessage("size.mb", args);
				}
				else if(size_long > 700L)
				{
					String[] args = { formatter.format(1.0 * size_long / 1024L) };
					size = rb.getFormattedMessage("size.kb", args);
				}
				else 
				{
					String[] args = { formatter.format(size_long) };
					size = rb.getFormattedMessage("size.bytes", args);
				}
			}
			setSize(size);

		}
		
		User creator = ResourcesAction.getUserProperty(props, ResourceProperties.PROP_CREATOR);
		if(creator != null)
		{
			String createdBy = creator.getDisplayName();
			setCreatedBy(createdBy);
		}
		// setCreatedBy(props.getProperty(ResourceProperties.PROP_CREATOR));
		this.setModifiedTime(props.getPropertyFormatted(ResourceProperties.PROP_MODIFIED_DATE));
				
	}
	
	public static ListItem getListItem(ContentEntity entity, ListItem parent, ResourceTypeRegistry registry, boolean expandAll, Set<String> expandedFolders, List<String> items_to_be_moved, List<String> items_to_be_copied, int depth)
	{
		ListItem item = null;
		boolean isCollection = entity.isCollection();
        
        Reference ref = EntityManager.newReference(entity.getReference());

        item = new ListItem(entity);
        item.setDepth(depth);
        
        /*
         * calculate permissions for this entity.  If its access mode is 
         * GROUPED, we need to calculate permissions based on current user's 
         * role in group. Otherwise, we inherit from containing collection
         * and check to see if additional permissions are set on this entity
         * that were't set on containing collection...
         */
        if(GroupAwareEntity.AccessMode.INHERITED == entity.getAccess())
        {
        	// permissions are same as parent or site
        	if(parent == null)
        	{
        		// permissions are same as site
        		item.setPermissions(ResourcesAction.getPermissions(entity.getId()));
        	}
        	else
        	{
        		// permissions are same as parent
        		item.setPermissions(parent.getPermissions());
        	}
        }
        else if(GroupAwareEntity.AccessMode.GROUPED == entity.getAccess())
        {
            // TODO: Calculate permissions
        	// permissions are determined by group(s)
        }

        if(isCollection)
        {
        	ContentCollection collection = (ContentCollection) entity;
        	
        	if(item.isTooBig)
        	{
        		// do nothing
        	}
			else if(expandAll)
        	{
        		expandedFolders.add(entity.getId());
        	}

			if(expandedFolders.contains(entity.getId()))
			{
				item.setExpanded(true);

		       	List<ContentEntity> children = collection.getMemberResources();
	        	Iterator<ContentEntity> childIt = children.iterator();
	        	while(childIt.hasNext())
	        	{
	        		ContentEntity childEntity = childIt.next();
	        		ListItem child = getListItem(childEntity, item, registry, expandAll, expandedFolders, items_to_be_moved, items_to_be_copied, depth + 1);
	        		item.addMember(child);
	        	}
			}
			
			item.setAddActions(ResourcesAction.getAddActions(entity, item.getPermissions(), registry, items_to_be_moved, items_to_be_copied));
			//this.members = coll.getMembers();
			item.setIconLocation( ContentTypeImageService.getContentTypeImage("folder"));
        }
        
		item.setOtherActions(ResourcesAction.getActions(entity, item.getPermissions(), registry, items_to_be_moved, items_to_be_copied));
		
		return item;
	}


	/**
	 * 
	 */
	public ListItem(String entityId)
	{
		this.id = entityId;
	}

	/**
	 * @return the mimetype
	 */
	public String getMimetype()
	{
		return this.mimetype;
	}

	/**
	 * @param mimetype the mimetype to set
	 */
	public void setMimetype(String mimetype)
	{
		this.mimetype = mimetype;
	}

	/**
	 * @return the iconLocation
	 */
	public String getIconLocation()
	{
		return this.iconLocation;
	}

	/**
	 * @param iconLocation the iconLocation to set
	 */
	public void setIconLocation(String iconLocation)
	{
		this.iconLocation = iconLocation;
	}

	/**
	 * @return the hoverText
	 */
	public String getHoverText()
	{
		return this.hoverText;
	}
	
	public boolean isEmpty()
	{
		return this.isEmpty;
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
	 * @return the permissions
	 */
	public Set<ContentPermissions> getPermissions()
	{
		return this.permissions;
	}
	
	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(Collection<ContentPermissions> permissions)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		
		this.permissions.clear();
		this.permissions.addAll(permissions);
	}
	
	/**
	 * @param permission
	 */
	public void addPermission(ContentPermissions permission)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		this.permissions.add(permission);
	}
	
	public boolean canRead()
	{
		return isPermitted(ContentPermissions.READ);
	}
	
	/**
	 * @param permission
	 * @return
	 */
	public boolean isPermitted(ContentPermissions permission)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		return this.permissions.contains(permission);
	}
	
	/**
	 * @return
	 */
	public String getId() 
	{
		return id;
	}
	
	/**
	 * @param id
	 */
	public void setId(String id) 
	{
		this.id = id;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}
	
	public List<ListItem> getMembers() 
	{
		return members;
	}
	
	public void setMembers(List<ListItem> members) 
	{
		if(this.members == null)
		{
			this.members = new Vector<ListItem>();
		}
		this.members.clear();
		this.members.addAll(members);
	}
	
	public void setSelected(boolean selected) 
	{
		this.selected = selected;
	}
	
	public boolean isSelected() 
	{
		return selected;
	}
	
	/**
	 * @param hover
	 */
	public void setHoverText(String hover)
	{
		this.hoverText = hover;
	}

	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl()
	{
		return this.accessUrl;
	}

	/**
	 * @param accessUrl the accessUrl to set
	 */
	public void setAccessUrl(String accessUrl)
	{
		this.accessUrl = accessUrl;
	}

	/**
     * @param child
     */
    public void addMember(ListItem member)
    {
        if(this.members == null)
        {
        	this.members = new Vector<ListItem>();
        }
        this.members.add(member);
    }

	/**
     * @param isEmpty
     */
    public void setIsEmpty(boolean isEmpty)
    {
        this.isEmpty = isEmpty;
    }

	/**
     * @param isSortable
     */
    public void setSortable(boolean isSortable)
    {
        this.isSortable  = isSortable;
    }

	public boolean isTooBig()
	{
		return this.isTooBig;
	}
	
	/**
     * @param b
     */
    public void setIsTooBig(boolean isTooBig)
    {
        this.isTooBig = isTooBig;
    }

	/**
     * @return the isExpanded
     */
    public boolean isExpanded()
    {
    	return isExpanded;
    }

	/**
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(boolean isExpanded)
    {
    	this.isExpanded = isExpanded;
    }

	/**
     * @param string
     */
    public void setSize(String size)
    {
        this.size = size;
    }

	/**
     * @return the size
     */
    public String getSize()
    {
    	return size;
    }

	/**
     * @return the addActions
     */
    public List<ResourceToolAction> getAddActions()
    {
    	return addActions;
    }

	/**
     * @param addActions the addActions to set
     */
    public void setAddActions(List<ResourceToolAction> addActions)
    {
    	this.addActions = addActions;
    }

	/**
     * @return the createdBy
     */
    public String getCreatedBy()
    {
    	return createdBy;
    }

	/**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy)
    {
    	this.createdBy = createdBy;
    }

	/**
     * @return the modifiedTime
     */
    public String getModifiedTime()
    {
    	return modifiedTime;
    }

	/**
     * @param modifiedTime the modifiedTime to set
     */
    public void setModifiedTime(String modifiedTime)
    {
    	this.modifiedTime = modifiedTime;
    }

	/**
     * @return the otherActions
     */
    public List<ResourceToolAction> getOtherActions()
    {
    	return otherActions;
    }

	/**
     * @param otherActions the otherActions to set
     */
    public void setOtherActions(List<ResourceToolAction> otherActions)
    {
    	this.otherActions = otherActions;
    }

	/**
     * @return the depth
     */
    public int getDepth()
    {
    	return depth;
    }

	/**
     * @param depth the depth to set
     */
    public void setDepth(int depth)
    {
    	this.depth = depth;
    }

	/**
     * @return the otherActionsLabel
     */
    public String getOtherActionsLabel()
    {
    	return otherActionsLabel;
    }

	/**
     * @param otherActionsLabel the otherActionsLabel to set
     */
    public void setOtherActionsLabel(String otherActionsLabel)
    {
    	this.otherActionsLabel = otherActionsLabel;
    }

	/**
     * @param canSelect
     */
    public void setCanSelect(boolean canSelect)
    {
	    this.canSelect  = canSelect;
    }
    
    /**
     * @return
     */
    public boolean canSelect()
    {
    	return canSelect;
    }
    
	/**
     * @param item
     * @return
     */
    public List<ListItem> convert2list()
    {
    	List<ListItem> list = new Vector<ListItem>();
    	Stack<ListItem> processStack = new Stack<ListItem>();
    	
    	processStack.push(this);
    	while(! processStack.empty())
    	{
    		ListItem parent = processStack.pop();
    		list.add(parent);
    		List<ListItem> children = parent.getMembers();
    		if(children != null)
    		{
    			for(int i = children.size() - 1; i >= 0; i--)
    			{
    				ListItem child = children.get(i);
    				processStack.push(child);
    			}
    		}
    	}
    	
	    return list;
	    
    }	// convert2list
    
}

