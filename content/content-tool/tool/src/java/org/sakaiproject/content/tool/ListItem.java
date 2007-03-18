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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
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
    
    protected static Comparator DEFAULT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true);
    protected static final Comparator PRIORITY_SORT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);

	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	protected static final long ONE_WEEK = 1000L * 60L * 60L * 24L * 7L;

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
	protected ResourceType resourceTypeDef = null;
	protected boolean isEmpty = true;
	protected boolean isExpanded = false;
	protected boolean isPubviewPossible;
	protected boolean isPubviewInherited = false;
	protected boolean isPubview = false;
	protected boolean isSortable = false;
	protected boolean isTooBig = false;
	protected String size = "";
	protected String createdBy;
	protected String modifiedTime;
	protected int depth;
	protected Map<String, ResourceToolAction> multipleItemActions = new HashMap<String, ResourceToolAction>();

	protected boolean canSelect = false;

	protected ContentEntity entity;

	protected AccessMode accessMode;
	protected AccessMode effectiveAccess;
	protected Collection<Group> groups = new Vector<Group>();
	protected Collection<Group> inheritedGroups = new Vector<Group>();
	protected Collection<Group> possibleGroups = new Vector<Group>();
	protected Collection<Group> allowedRemoveGroups = new Vector<Group>();
	protected Collection<Group> allowedAddGroups = new Vector<Group>();
	protected Map<String,Group> possibleGroupsMap = new HashMap<String, Group>();

	protected boolean hidden;
	protected boolean isAvailable;
	protected boolean useReleaseDate;
	protected Time releaseDate;
	protected boolean useRetractDate;
	protected Time retractDate;
	
	/**
	 * @param entity
	 */
	public ListItem(ContentEntity entity)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		this.entity = entity;
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
			setSortable(contentService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < ResourcesAction.EXPANDABLE_FOLDER_SIZE_LIMIT);
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
		
		this.accessMode = entity.getAccess();
		this.effectiveAccess = entity.getInheritedAccess();
		this.groups.clear();
		this.groups.addAll(entity.getGroupObjects());
		this.inheritedGroups.clear();
		this.inheritedGroups.addAll(entity.getInheritedGroupObjects());
		Reference ref = EntityManager.newReference(entity.getReference());
		if(ref != null && ref.getContext() != null)
		{
			try
	        {
		        Site site = SiteService.getSite(ref.getContext());
		        setPossibleGroups(site.getGroups());
	        }
	        catch (IdUnusedException e)
	        {
		        logger.warn("IdUnusedException for a site in resources: " + ref.getContext() + " (" + ref.getReference() + ")");
	        }
		}
        
		Collection<Group> groupsWithRemovePermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(id);
			Collection<Group> more = contentService.getGroupsWithRemovePermission(ref.getContainer());
			if(more != null && ! more.isEmpty())
			{
				groupsWithRemovePermission.addAll(more);
			}
		}
		else if(AccessMode.GROUPED == this.effectiveAccess)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(ref.getContainer());
		}
		else
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(contentService.getSiteCollection(ref.getContext()));
		}
		this.allowedRemoveGroups.clear();
		this.allowedRemoveGroups.addAll(groupsWithRemovePermission);
		
		Collection<Group> groupsWithAddPermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(id);
			Collection<Group> more = contentService.getGroupsWithAddPermission(ref.getContainer());
			if(more != null && ! more.isEmpty())
			{
				groupsWithAddPermission.addAll(more);
			}
		}
		else if(AccessMode.GROUPED == this.effectiveAccess)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(ref.getContainer());
		}
		else
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(contentService.getSiteCollection(ref.getContext()));
		}
		this.allowedAddGroups.clear();
		this.allowedAddGroups.addAll(groupsWithAddPermission);
		


        this.isPubviewInherited = contentService.isInheritingPubView(id);
		if (!this.isPubviewInherited) 
		{
			this.isPubview = contentService.isPubView(id);
		}
		
		this.hidden = entity.isHidden();
		Time releaseDate = entity.getReleaseDate();
		if(releaseDate == null)
		{
			this.useReleaseDate = false;
			this.releaseDate = TimeService.newTime();
		}
		else
		{
			this.useReleaseDate = true;
			this.releaseDate = releaseDate;
		}
		Time retractDate = entity.getRetractDate();
		if(retractDate == null)
		{
			this.useRetractDate = false;
		}
		else
		{
			this.useRetractDate = true;
			this.retractDate = retractDate;
		}
		this.isAvailable = entity.isAvailable();

	}
	
	public ListItem(ResourceToolActionPipe pipe, ListItem parent, Time defaultRetractTime)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		this.entity = null;
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = pipe.getAction().getTypeId();
		this.resourceTypeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(resourceTypeDef != null)
		{
			this.hoverText = resourceTypeDef.getLocalizedHoverText(null);
			this.iconLocation = resourceTypeDef.getIconLocation();
			String[] args = { resourceTypeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
		}

		this.collection = ResourceType.TYPE_FOLDER.equals(resourceType);
		this.id = "";
		this.name = this.otherActionsLabel;
		this.permissions = parent.getPermissions();
		this.selected = false;
		

		if(this.collection)
		{
        	int collection_size = 0;
        	String[] args = { Integer.toString(0) };
	        setSize(rb.getFormattedMessage("size.items", args));
 			setIsEmpty(true);
			setSortable(false);
			setIsTooBig(false);
		}
		else 
		{
			this.mimetype = pipe.getMimeType();
			if(this.mimetype == null)
			{
				
			}
			else if(this.iconLocation == null)
			{
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
			String size = "";
			if(pipe.getContent() != null)
			{
				long size_long = pipe.getContent().length;
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
		
		Time now = TimeService.newTime();
		User creator = UserDirectoryService.getCurrentUser();
		if(creator != null)
		{
			String createdBy = creator.getDisplayName();
			setCreatedBy(createdBy);
			setModifiedBy(createdBy);
		}
		// setCreatedBy(props.getProperty(ResourceProperties.PROP_CREATOR));
		this.setModifiedTime(now.getDisplay());
		this.setCreatedTime(now.getDisplay());
		
		this.accessMode = AccessMode.INHERITED;
		this.effectiveAccess = parent.getEffectiveAccess();
		this.groups.clear();
		//this.groups.addAll();
		this.inheritedGroups.clear();
		if(parent.getAccessMode() == AccessMode.GROUPED)
		{
			this.inheritedGroups.addAll(parent.getGroups());
		}
		else
		{
			this.inheritedGroups.addAll(parent.getInheritedGroups());
		}
		setPossibleGroups(parent.getPossibleGroups());
        
		this.allowedRemoveGroups = new Vector(parent.allowedRemoveGroups);		
		this.allowedAddGroups = new Vector(parent.allowedAddGroups);
		

        this.isPubviewInherited = parent.isPubviewInherited || parent.isPubview;
		
		this.hidden = false;
		this.useReleaseDate = false;
		Time releaseDate = TimeService.newTime();
		this.useRetractDate = false;
		Time retractDate = TimeService.newTime(defaultRetractTime.getTime());
		this.isAvailable = parent.isAvailable();

	}

	private void setModifiedBy(String createdBy2) {
		// TODO Auto-generated method stub
		
	}

	private void setCreatedTime(String display) {
		// TODO Auto-generated method stub
		
	}

	private Collection<Group> getAllowedRemoveGroupRefs() 
	{
		// TODO Auto-generated method stub
		return new TreeSet<Group>(this.allowedAddGroups);
	}

	/**
	 * @param entityId
	 */
	public ListItem(String entityId)
	{
		this.id = entityId;
	}
	
	/**
	 * @param entity
	 * @param parent
	 * @param registry
	 * @param expandAll
	 * @param expandedFolders
	 * @param items_to_be_moved
	 * @param items_to_be_copied
	 * @param depth
	 * @param userSelectedSort
	 * @param preventPublicDisplay
	 * @return
	 */
	public static ListItem getListItem(ContentEntity entity, ListItem parent, ResourceTypeRegistry registry, boolean expandAll, Set<String> expandedFolders, List<String> items_to_be_moved, List<String> items_to_be_copied, int depth, Comparator userSelectedSort, boolean preventPublicDisplay)
	{
		ListItem item = null;
		boolean isCollection = entity.isCollection();
		
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		
		boolean isAvailabilityEnabled = contentService.isAvailabilityEnabled();
        
        Reference ref = EntityManager.newReference(entity.getReference());

        item = new ListItem(entity);
        item.setPubviewPossible(! preventPublicDisplay);
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
        		item.setPermissions(ResourcesAction.getPermissions(entity.getId(), null));
        	}
        	else
        	{
        		// permissions are same as parent
        		item.setPermissions(ResourcesAction.getPermissions(entity.getId(), parent.getPermissions()));
        	}
        }
        else if(GroupAwareEntity.AccessMode.GROUPED == entity.getAccess())
        {
        	// permissions are determined by group(s)
        	item.setPermissions(ResourcesAction.getPermissions(entity.getId(), null));
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
		       	
				Comparator comparator = null;
				if(userSelectedSort != null)
				{
					comparator = userSelectedSort;
				}
				else
				{
					boolean hasCustomSort = false;
					try
					{
						hasCustomSort = collection.getProperties().getBooleanProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT);
					}
					catch(Exception e)
					{
						// ignore -- let value be false
					}
					if(hasCustomSort)
					{
						comparator = PRIORITY_SORT_COMPARATOR;
					}
					else
					{
						comparator = DEFAULT_COMPARATOR;
					}
				}
				
				Collections.sort(children, comparator);

	        	Iterator<ContentEntity> childIt = children.iterator();
	        	while(childIt.hasNext())
	        	{
	        		ContentEntity childEntity = childIt.next();
					if(isAvailabilityEnabled && ! contentService.isAvailable(childEntity.getId()))
					{
						continue;
					}

	        		ListItem child = getListItem(childEntity, item, registry, expandAll, expandedFolders, items_to_be_moved, items_to_be_copied, depth + 1, userSelectedSort, preventPublicDisplay);
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
    	for(ResourceToolAction action : addActions)
    	{
    		if(action instanceof ServiceLevelAction && ((ServiceLevelAction) action).isMultipleItemAction())
    		{
    			this.multipleItemActions.put(action.getId(), action);
    		}
    	}
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
    	for(ResourceToolAction action : otherActions)
    	{
    		if(action instanceof ServiceLevelAction && ((ServiceLevelAction) action).isMultipleItemAction())
    		{
    			this.multipleItemActions.put(action.getId(), action);
    		}
    	}
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

	/**
     * @return
     */
    public ContentEntity getEntity()
    {
	    // TODO Auto-generated method stub
	    return this.entity;
    }

	/**
     * @param action
     */
    public void addMultipleItemAction(ResourceToolAction action)
    {
	    this.multipleItemActions.put(action.getId(), action);
    }
    
    public boolean hasMultipleItemActions()
    {
    	return ! this.multipleItemActions.isEmpty();
    }
    
    public boolean hasMultipleItemAction(String key)
    {
    	return this.multipleItemActions.containsKey(key);
    }
    
    public ResourceToolAction getMultipleItemAction(String key)
    {
    	return this.multipleItemActions.get(key);
    }

	/**
     * @return
     */
    public Map<String, ResourceToolAction> getMultipleItemActions()
    {
	    return this.multipleItemActions;
    }

	/**
     * @return the accessMode
     */
    public AccessMode getAccessMode()
    {
    	return accessMode;
    }

	/**
     * @param accessMode the accessMode to set
     */
    public void setAccessMode(AccessMode accessMode)
    {
    	this.accessMode = accessMode;
    }

	/**
     * @return the groups
     */
    public Collection<Group> getGroups()
    {
    	return new Vector<Group>(groups);
    }
    
    /**
     * @return
     */
    public Collection<String> getInheritedGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.inheritedGroups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }

    /**
     * @return
     */
    public Collection<String> getGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.groups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }

	/**
     * @param groups the groups to set
     */
    public void setGroups(Collection<Group> groups)
    {
    	this.groups.clear();
    	this.groups.addAll(groups);
    }

	/**
     * @return the inheritedGroups
     */
    public Collection<Group> getInheritedGroups()
    {
    	return new Vector<Group>(inheritedGroups);
    }

	/**
     * @param inheritedGroups the inheritedGroups to set
     */
    public void setInheritedGroups(Collection<Group> inheritedGroups)
    {
    	this.inheritedGroups.clear();
    	this.inheritedGroups.addAll(inheritedGroups);
    }

	/**
     * @return the possibleGroups
     */
    public Collection<Group> getPossibleGroups()
    {
    	return new Vector<Group>(possibleGroups);
    }
    
	 /**
	  * Does this entity inherit grouped access mode with a single group that has access?
	  * @return true if this entity inherits grouped access mode with a single group that has access, and false otherwise.
	  */
	 public boolean isSingleGroupInherited()
	 {
		 //Collection groups = getInheritedGroups();
		 return // AccessMode.INHERITED.toString().equals(this.m_access) && 
		 AccessMode.GROUPED == this.effectiveAccess && 
		 this.inheritedGroups != null && 
		 this.inheritedGroups.size() == 1; 
		 // && this.m_oldInheritedGroups != null 
		 // && this.m_oldInheritedGroups.size() == 1;
	 }

	 /**
	  * Is this entity's access restricted to the site (not pubview) and are there no groups defined for the site?
	  * @return
	  */
	 public boolean isSiteOnly()
	 {
		 boolean isSiteOnly = false;
		 isSiteOnly = !isGroupPossible() && !isPubviewPossible();
		 return isSiteOnly;
	 }

	 /**
	  * @return
	  */
	 public boolean isSitePossible()
	 {
		 return !this.isPubviewInherited && !isGroupInherited() && !isSingleGroupInherited();
	 }

    /**
     * @return
     */
    public boolean isPubviewPossible()
    {
    	return isPubviewPossible;
    }

	/**
     * @return
     */
    public Collection<String> getPossibleGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.possibleGroups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }

	/**
     * @param possibleGroups the possibleGroups to set
     */
    public void setPossibleGroups(Collection<Group> possibleGroups)
    {
    	this.possibleGroups.clear();
    	this.possibleGroups.addAll(possibleGroups);
        for(Group group : this.possibleGroups)
        {
        	this.possibleGroupsMap.put(group.getId(), group);
        }
    }

	/**
     * @return the effectiveAccess
     */
    public AccessMode getEffectiveAccess()
    {
    	return effectiveAccess;
    }

	/**
     * @param effectiveAccess the effectiveAccess to set
     */
    public void setEffectiveAccess(AccessMode effectiveAccess)
    {
    	this.effectiveAccess = effectiveAccess;
    }
    
    /**
     * @return
     */
    public boolean isGroupPossible()
    {
    	return this.allowedAddGroups != null && ! this.allowedAddGroups.isEmpty();
    }
    
    /**
     * @param group
     * @return
     */
    public boolean isPossible(Group group)
    {
    	boolean isPossible = false;
    	
    	Collection<Group> groupsToCheck = this.possibleGroups;
    	if(AccessMode.GROUPED == this.effectiveAccess)
    	{
    		groupsToCheck = this.inheritedGroups;
    	}
    	
    	for(Group gr : groupsToCheck)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			isPossible = true;
    			break;
    		}
    	}
    	
    	return isPossible;
    }
    
    /**
     * @param group
     * @return
     */
    public boolean allowedRemove(Group group)
    {
    	boolean allowed = false;
    	
    	for(Group gr : this.allowedRemoveGroups)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			allowed = true;
    			break;
    		}
    	}
    	
    	return allowed;
    }
    
    public boolean isGroupInherited()
    {
    	return AccessMode.GROUPED == this.effectiveAccess && AccessMode.INHERITED == this.accessMode;
    }
    
    /**
     * @param group
     * @return
     */
    public boolean isLocal(Group group)
    {
    	boolean isLocal = false;
    	
    	for(Group gr : this.groups)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			isLocal = true;
    			break;
    		}
    	}
    	
    	return isLocal;
    }

	/**
     * @return the isPubview
     */
    public boolean isPubview()
    {
    	return isPubview;
    }

	/**
     * @param isPubview the isPubview to set
     */
    public void setPubview(boolean isPubview)
    {
    	this.isPubview = isPubview;
    }

	/**
     * @return the isPubviewInherited
     */
    public boolean isPubviewInherited()
    {
    	return isPubviewInherited;
    }

	/**
     * @param isPubviewInherited the isPubviewInherited to set
     */
    public void setPubviewInherited(boolean isPubviewInherited)
    {
    	this.isPubviewInherited = isPubviewInherited;
    }

	/**
     * @return the hidden
     */
    public boolean isHidden()
    {
    	return hidden;
    }

	/**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden)
    {
    	this.hidden = hidden;
    }

	/**
     * @return the releaseDate
     */
    public Time getReleaseDate()
    {
    	if(this.releaseDate == null)
    	{
    		this.releaseDate = TimeService.newTime();
    	}
    	return releaseDate;
    }

	/**
     * @param releaseDate the releaseDate to set
     */
    public void setReleaseDate(Time releaseDate)
    {
    	this.releaseDate = releaseDate;
    }

	/**
     * @return the retractDate
     */
    public Time getRetractDate()
    {
    	if(this.retractDate == null)
    	{
    		this.retractDate = TimeService.newTime(TimeService.newTime().getTime() + ONE_WEEK);
    	}
    	return retractDate;
    }

	/**
     * @param retractDate the retractDate to set
     */
    public void setRetractDate(Time retractDate)
    {
    	this.retractDate = retractDate;
    }

	/**
     * @return the useReleaseDate
     */
    public boolean useReleaseDate()
    {
    	return useReleaseDate;
    }

	/**
     * @param useReleaseDate the useReleaseDate to set
     */
    public void setUseReleaseDate(boolean useReleaseDate)
    {
    	this.useReleaseDate = useReleaseDate;
    }

	/**
     * @return the useRetractDate
     */
    public boolean useRetractDate()
    {
    	return useRetractDate;
    }

	/**
     * @param useRetractDate the useRetractDate to set
     */
    public void setUseRetractDate(boolean useRetractDate)
    {
    	this.useRetractDate = useRetractDate;
    }

	/**
     * @return the isAvailable
     */
    public boolean isAvailable()
    {
    	return isAvailable;
    }

	/**
     * @param isAvailable the isAvailable to set
     */
    public void setAvailable(boolean isAvailable)
    {
    	this.isAvailable = isAvailable;
    }

	/**
     * @param isPubviewPossible the isPubviewPossible to set
     */
    public void setPubviewPossible(boolean isPubviewPossible)
    {
    	this.isPubviewPossible = isPubviewPossible;
    }

	/**
     * @param new_groups
     * @return
     */
	public SortedSet<String> convertToRefs(Collection<String> groupIds) 
	{
		SortedSet<String> groupRefs = new TreeSet<String>();
		for(String groupId : groupIds)
		{
			Group group = (Group) this.possibleGroupsMap.get(groupId);
			if(group != null)
			{
				groupRefs.add(group.getReference());
			}
		}
		return groupRefs;

	}

	/**
     * @param new_groups
     */
    public void setGroupsById(Collection<String> groupIds)
    {
    	this.groups.clear();
    	if(groupIds != null && ! groupIds.isEmpty())
    	{
	    	for(String groupId : groupIds)
	    	{
	    		Group group = this.possibleGroupsMap.get(groupId);
	    		this.groups.add(group);
	     	}
    	}
    }

	/**
	 * @return
	 */
	public ResourceType getResourceTypeDef() 
	{
		if(resourceTypeDef == null)
		{
			if(resourceType == null)
			{
				resourceType = ResourceType.TYPE_UPLOAD;
			}
			ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			resourceTypeDef = registry.getType(this.resourceType);
		}
		return resourceTypeDef;
	}

	/**
	 * @param resourceTypeDef
	 */
	public void setResourceTypeDef(ResourceType resourceTypeDef) 
	{
		this.resourceTypeDef = resourceTypeDef;
		
		// make sure typeDef and typeId are consistent
		if(resourceTypeDef != null)
		{
			this.resourceType = resourceTypeDef.getId();
		}
	}

}

