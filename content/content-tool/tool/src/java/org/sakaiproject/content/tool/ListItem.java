/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandlerResolver;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.ExpandableResourceType;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.metadata.logic.MetadataService;
import org.sakaiproject.content.metadata.model.MetadataType;
import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.SakaiException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * ListItem
 * 
 * This class is for displaying a {@link ContentEntity} in the user interface.
 * The typical lifecycle of the object is to:
 * <ul>
 * <li>Create a new instance of from a {@link ContentEntity} using {@link #ListItem(ContentEntity)}.</li>
 * <li>Update the newly created object, probably from a HTTP request {@link #captureProperties(ParameterParser, String)}.</li>
 * <li>Push the changes back to a {@link ContentEntity} using {@link #updateContentResourceEdit(ContentResourceEdit)} so it can be saved.</li>
 * </ul>
 *
 */
@Slf4j
public class ListItem
{
    /** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");

	/** Resource bundle using current language locale */
    private static ResourceLoader trb = new ResourceLoader("types");
    
    protected static final Comparator<ContentEntity> DEFAULT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true);

	protected static boolean optionalPropertiesEnabled = false;

    protected static final Comparator<ContentEntity> PRIORITY_SORT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);
    
    /** Default content type for unknown extensions. */
    protected static final String UNKNOWN_TYPE = "application/octet-stream";

    /** The role that is used to define pubview or public access */
    public static final String PUBVIEW_ROLE = AuthzGroupService.ANON_ROLE;

	public static final String DOT = "_";
	private static final String PROP_HIDDEN_TRUE = "true"; // SAK-23044

	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	public static final long ONE_DAY = 24L * 60L * 60L * 1000L;
	
	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	public static final long ONE_WEEK = 7L * ONE_DAY;
	
	public static final int EXPANDABLE_FOLDER_NAV_SIZE_LIMIT = ServerConfigurationService.getInt("sakai.content.resourceLimit", 0);  //SAK-21955

	/**
	 * Services
	 */
	private static final MetadataService metadataService = (MetadataService) ComponentManager.get(MetadataService.class.getCanonicalName());
	private static SecurityService securityService  = ComponentManager.get(SecurityService.class);
	private static final org.sakaiproject.tool.api.ToolManager toolManager = (org.sakaiproject.tool.api.ToolManager) ComponentManager.get(org.sakaiproject.tool.api.ToolManager.class.getCanonicalName());

	/** 
	 ** Comparator for sorting Group objects
	 **/
	private static class GroupComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			return ((Group)o1).getTitle().compareToIgnoreCase( ((Group)o2).getTitle() );
		}
	}
	
	// sort groups before display
	private static GroupComparator groupComparator = new GroupComparator();
	
	/**
	 * @param entity
	 * @param parent
	 * @param registry
	 * @param expandAll Should we expand all the contained collections inside this one.
	 * @param expandedCollections
	 * @param items_to_be_moved
	 * @param items_to_be_copied
	 * @param depth
	 * @param userSelectedSort
	 * @param preventPublicDisplay
	 * @param addFilter TODO
	 * @return
	 */
	public static ListItem 	getListItem(ContentEntity entity, ListItem parent, ResourceTypeRegistry registry, boolean expandAll, Set<String>expandedCollections, List<String> items_to_be_moved, List<String> items_to_be_copied, int depth, Comparator userSelectedSort, boolean preventPublicDisplay, ContentResourceFilter addFilter)
	{
		ListItem item = null;
			
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		
		boolean isAvailabilityEnabled = contentService.isAvailabilityEnabled();
		
        if(entity == null)
        {
        	item = new ListItem("");
        	return item;
        }
        else
        {
        	item = new ListItem(entity);
            //item.m_reference = EntityManager.newReference(entity.getReference());
        }
        
        boolean isCollection = entity.isCollection();
        
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
					String typeId = entity.getResourceType();
					if(typeId != null && registry != null)
					{
						ResourceType typeDef = registry.getType(typeId);
						if(typeDef != null && typeDef.isExpandable())
						{
							ServiceLevelAction expandAction = ((ExpandableResourceType) typeDef).getExpandAction();
							if(expandAction != null && expandAction.available(entity))
							{
								expandAction.initializeAction(item.m_reference);
								
							expandedCollections.add(entity.getId());
					       		
					       		expandAction.finalizeAction(item.m_reference);
							}
						}
					}
	         	}
			if(expandedCollections.contains(entity.getId()))
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
		        		if(childEntity.getAccess() == AccessMode.GROUPED)
		        		{
		        			if(childEntity.isCollection())
		        			{
		        				if(! contentService.allowGetCollection(childEntity.getId()))
		        				{
			        				continue;
		        				}
		        			}
		        			else
		        			{
		        				if(!contentService.allowGetResource(childEntity.getId()))
		        				{
		        					continue;
		        				}
		        			}
		        		}
		        		
						if(isAvailabilityEnabled && ! contentService.isAvailable(childEntity.getId()))
						{
							continue;
						}
	
					ListItem child = getListItem(childEntity, item, registry, expandAll, expandedCollections, items_to_be_moved, items_to_be_copied, depth + 1, userSelectedSort, preventPublicDisplay, addFilter);
		        		if(items_to_be_copied != null && items_to_be_copied.contains(child.id))
		        		{
		        			child.setSelectedForCopy(true);
		        		}
		        		if(items_to_be_moved != null && items_to_be_moved.contains(child.id))
		        		{
		        			child.setSelectedForMove(true);
		        		}
		        		item.addMember(child);
		        	}
				}
	 			
				List<ResourceToolAction> myAddActions = ResourcesAction.getAddActions(entity, item.getPermissions(), registry);
				if(addFilter != null)
				{
					myAddActions = addFilter.filterAllowedActions(myAddActions);
				}
				item.setAddActions(myAddActions );
				//this.members = coll.getMembers();
				item.setIconLocation( ContentTypeImageService.getContentTypeImage("folder"));
	        }
        List<ResourceToolAction> otherActions = ResourcesAction.getActions(entity, item.getPermissions(), registry);
        List<ResourceToolAction> pasteActions = ResourcesAction.getPasteActions(entity, item.getPermissions(), registry, items_to_be_moved, items_to_be_copied);

        if(pasteActions != null && ! pasteActions.isEmpty())
        {
        	if(otherActions == null)
        	{
        		otherActions = new ArrayList<ResourceToolAction>(pasteActions);
        	}
        	else
        	{
        		otherActions.addAll(0, pasteActions);
        	}
        }
        
		item.setOtherActions(otherActions);
		
		item.setPasteActions(pasteActions);
		
		return item;
	}

	protected boolean selectedForMove = false;

	protected boolean selectedForCopy = false;
	
	protected String name;
	protected String id;
	protected List<ResourceToolAction> addActions;
	protected List<ResourceToolAction> otherActions;
	protected List<ResourceToolAction> pasteActions;
	protected String otherActionsLabel;
	protected List<ListItem> members;
	protected Set<ContentPermissions> permissions;
	protected boolean selected;
	protected boolean collection;
	protected String hoverText;
	protected String expandLabel;
	protected String accessUrl;
	protected String iconLocation;
	protected String iconClass;
	protected String mimetype;
	protected String resourceType;
	protected ResourceType resourceTypeDef = null;
	protected boolean expandable = false;
	protected boolean isEmpty = true;
	protected boolean isExpanded = false;
	protected boolean isHot = false;
	protected boolean isSortable = false;
	protected boolean isTooBig = false;
	protected boolean isTooBigNav = false;
	protected boolean isCourseSite = false;
	protected String size = "";
	protected String sizzle = "";
	protected String createdBy;
	protected String createdTime;
	protected String modifiedBy;
	protected String modifiedTime;
	protected int depth;

	protected String chhmountpoint; // Content Hosting Handler bean name

	protected Map<String, ResourceToolAction> multipleItemActions = new HashMap<String, ResourceToolAction>();

	protected boolean canSelect = true;
	
	/** 
	 * Access settings
	 * Access mode can be "grouped" or "inherited". Inherited access mode
	 * can be "site" or "grouped". Site access implies that the site's
	 * permissions apply, possibly with changes due to custom folder 
	 * permissions in the hierarchy.   
	 */
	protected ContentEntity entity;
	protected Reference m_reference;
	protected AccessMode accessMode;
	protected AccessMode inheritedAccessMode;
	protected Collection<Group> groups = new ArrayList<Group>();
	protected Set<String> roleIds = new LinkedHashSet<String>();
	protected Set<String> inheritedRoleIds = new LinkedHashSet<String>();
	protected Collection<Group> inheritedGroups = new ArrayList<Group>();
	protected Collection<Group> possibleGroups = new ArrayList<Group>();
	protected Collection<Group> allowedRemoveGroups = null;
	protected Collection<Group> allowedAddGroups = null;
	protected Map<String,Group> siteGroupsMap = new HashMap<String, Group>();

	protected boolean isPubviewPossible;

	protected boolean hidden;
	protected boolean hiddenWithAccessibleContent;
	protected boolean isAvailable;
	protected boolean useReleaseDate;
	protected Time releaseDate;
	protected boolean useRetractDate;

	protected Time retractDate;
	public boolean useConditionalRelease = false;
	private String submittedFunctionName;
	private String submittedResourceFilter = "";
	private String selectedConditionKey;
	private String conditionArgument;
	private String conditionAssignmentPoints;

	private String notificationId;
	private Collection<String> accessControlList;

	protected boolean numberFieldIsInvalid;
	protected boolean numberFieldIsOutOfRange;
	
	protected String description;
	protected String copyrightInfo = "";
	protected String copyrightStatus;
	protected boolean copyrightAlert;

	protected ListItem parent;

	public String containingCollectionId;

	protected boolean isUserSite = false;
	protected boolean isDropbox = false;

	protected boolean isSiteCollection = false;
	protected boolean hasQuota = false;
	protected boolean canSetQuota = false;
	private boolean isAdmin = false;
	private Boolean allowHtmlInline;
	private Boolean allowHtmlInlineInherited;

	protected String quota;

	protected boolean nameIsMissing = false;

	private String expandIconLocation;
	
	protected String htmlFilter;

	protected int notification = NotificationService.NOTI_NONE;

	protected List<MetadataType> metadataGroups;
	protected Map<String, Object> metadataValues;
	protected String metadataValidationFails="";

	private int constructor;

	protected long dropboxHighlight;

	protected Time lastChange = null;

	private org.sakaiproject.content.api.ContentHostingService contentService;
	
	public String getConditionAssignmentPoints() {
		return conditionAssignmentPoints;
	}
	
	public void setConditionAssignmentPoints(String conditionAssignmentPoints) {
		this.conditionAssignmentPoints = conditionAssignmentPoints;
	}

	/**
	 * @param entity
	 */
	public ListItem(ContentEntity entity)
	{
		this.constructor = 2;
		set(entity);
	}

	/**
     * @param entity
     */
    protected void set(ContentEntity entity)
    {
		this.entity = entity;
		if(entity == null)
		{
			return;
		}
	    String refstr = entity.getReference();
		this.isSiteCollection = this.siteCollection(refstr);

		boolean isUserSite = isInWorkspace(parent, refstr);
		setUserSite(isUserSite);
		
		if(m_reference == null)
		{
			m_reference = EntityManager.newReference(refstr);
		}
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		if(entity.getContainingCollection() == null)
		{
			this.containingCollectionId = null;
		}
		else
		{
			this.containingCollectionId = entity.getContainingCollection().getId();
		}
		if(this.id != null)
		{
			this.isDropbox = contentService.isInDropbox(id);
		}
		else if(this.containingCollectionId != null)
		{
			this.isDropbox = contentService.isInDropbox(this.containingCollectionId);
		}

		ResourceProperties props = entity.getProperties();
		this.accessUrl = entity.getUrl();
		this.collection = entity.isCollection();
		this.id = entity.getId();
		this.name = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if(name == null || name.trim().equals(""))
		{
			
			String siteCollectionId = contentService.getSiteCollection(m_reference.getContext());
			if(siteCollectionId != null && siteCollectionId.equals(id))
			{
				String context = m_reference.getContext();
				Site site = getSiteObject(context);
				if(site != null)
				{
	                String siteTitle = site.getTitle();
	                if(siteTitle == null || siteTitle.trim().equals(""))
	                {
	                	siteTitle = site.getId();
	                }
					name = trb.getFormattedMessage("title.resources", new String[]{siteTitle});
				}
			}
		}
		this.description = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
		this.useConditionalRelease = Boolean.parseBoolean(props.getProperty(ConditionService.PROP_CONDITIONAL_RELEASE));
		this.notificationId = props.getProperty(ConditionService.PROP_CONDITIONAL_NOTIFICATION_ID);
		this.accessControlList = props.getPropertyList(ContentHostingService.CONDITIONAL_ACCESS_LIST);

		
		if(this.isDropbox)
		{
			try
			{
				lastChange  = props.getTimeProperty(org.sakaiproject.content.api.ContentHostingService.PROP_DROPBOX_CHANGE_TIMESTAMP);
//				long oneDayAgo = TimeService.newTime().getTime() - dropboxHighlight * ONE_DAY;
//				
//				if(lastChange != null && lastChange.getTime() > oneDayAgo)
//				{
//					setHot(true);
//				}
			}
			catch(Exception e)
			{
				// ignore
			}
			
			String context = m_reference.getContext();
			Site site = getSiteObject(context);
			if(site != null)
			{
				String siteType = site.getType();
				String courseSiteType = ServerConfigurationService.getString("courseSiteType");
				if(siteType != null && courseSiteType!= null && courseSiteType.equals(siteType))
				{
					this.isCourseSite = true;
				}
			}			
		}
		
		this.useConditionalRelease = Boolean.parseBoolean(props.getProperty(ConditionService.PROP_CONDITIONAL_RELEASE));
		this.notificationId = props.getProperty(ConditionService.PROP_CONDITIONAL_NOTIFICATION_ID);
		this.accessControlList = props.getPropertyList(ContentHostingService.CONDITIONAL_ACCESS_LIST);
		//this.submittedFunctionName = props.getProperty(ContentHostingService.PROP_SUBMITTED_FUNCTION_NAME);
		//this.submittedResourceFilter = props.getProperty(ContentHostingService.PROP_SUBMITTED_RESOURCE_FILTER);
		//this.selectedConditionKey = props.getProperty(ContentHostingService.PROP_SELECTED_CONDITION_KEY);
		//this.conditionArgument = props.getProperty(ContentHostingService.PROP_CONDITIONAL_RELEASE_ARGUMENT);

		
		this.permissions = new TreeSet<ContentPermissions>();
		this.selected = false;
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = entity.getResourceType();
		ResourceType typeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(typeDef != null)
		{
			this.hoverText = typeDef.getLocalizedHoverText(entity);
			this.iconLocation = typeDef.getIconLocation(entity);
			this.iconClass = typeDef.getIconClass(entity);
			if(typeDef.isExpandable())
			{
				this.expandIconLocation = ((ExpandableResourceType) typeDef).getIconLocation(this.entity, this.isExpanded);
				this.iconClass = ((ExpandableResourceType) typeDef).getIconClass(this.entity, this.isExpanded);
				this.expandLabel = ((ExpandableResourceType) typeDef).getLocalizedHoverText(this.entity, this.isExpanded);
			}
			String[] args = { typeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
		}

		if(this.collection)
		{
			ContentCollection collection = (ContentCollection) entity;
			String shortSizeStr = null;
			if(typeDef != null)
			{
				shortSizeStr = typeDef.getSizeLabel(entity);
			}
        	int collection_size = collection.getMemberCount();
			if(shortSizeStr == null)
			{
	        	if(collection_size == 1)
	        	{
	        		shortSizeStr = rb.getString("size.item");
	        	}
	        	else
	        	{
		        	String[] args = { Integer.toString(collection_size) };
		        	shortSizeStr = rb.getFormattedMessage("size.items", args);
	        	}
			}
			else if(shortSizeStr.length() > ResourceType.MAX_LENGTH_SHORT_SIZE_LABEL)
			{
				shortSizeStr = shortSizeStr.substring(0, ResourceType.MAX_LENGTH_SHORT_SIZE_LABEL);
			}
			setIsEmpty(collection_size < 1);
			setSize(shortSizeStr);
			String longSizeStr = null;
			if(typeDef != null)
			{
				longSizeStr = typeDef.getLongSizeLabel(entity);
			}
			if(longSizeStr == null)
			{
				longSizeStr = shortSizeStr;
			}
			else if(longSizeStr.length() > ResourceType.MAX_LENGTH_LONG_SIZE_LABEL)
			{
				
				longSizeStr = longSizeStr.substring(0, ResourceType.MAX_LENGTH_LONG_SIZE_LABEL);
			}
			setSizzle(longSizeStr);
			
			setSortable(contentService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(collection_size > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				setIsTooBig(true);
			}
			
			//SAK-21955
			//Prevent concurrent mode failures in admin Resource tool when clicking on resources that are too large.  Similar to 'isTooBig' but defined in properties
			//To enable add the property sakai.content.resourceLimit=<int> to sakai.properties where int is the limit of an accessible resource folder
			String siteId = getSiteContext(refstr);
			if(this.EXPANDABLE_FOLDER_NAV_SIZE_LIMIT != 0 && siteId != null && ("!admin".equals(siteId) || SiteService.getUserSiteId("admin").contains(siteId)) && (collection_size > this.EXPANDABLE_FOLDER_NAV_SIZE_LIMIT))
			{
				setIsTooBigNav(true);
			}
			else{
				setIsTooBigNav(false);
			}
			
			//does this collection allow inlineHTML?
			setAllowHtmlInline(isAllowInline(collection));
			setAllowHtmlInlineInherited(Boolean.FALSE);
			
			// setup for quota - ADMIN only, site-root collection only
			if (SecurityService.isSuperUser())
			{
				setIsAdmin(true);
				String siteCollectionId = contentService.getSiteCollection(m_reference.getContext());
				String dropBoxCollectionId = org.sakaiproject.content.api.ContentHostingService.COLLECTION_DROPBOX + m_reference.getContext() + Entity.SEPARATOR;
				if(siteCollectionId.equals(entity.getId()) || (entity.getId().startsWith(dropBoxCollectionId) && entity.getId().split(Entity.SEPARATOR).length<=4))
				{
					setCanSetQuota(true);
					try
					{
						// Getting the quota as a long validates the property
						long quota = props.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
						setHasQuota(true);
						setQuota(Long.toString(quota));
					}
					catch (Exception any)
					{
					}
				}
			}
		}
		else 
		{
			ContentResource resource = (ContentResource) entity;
			this.mimetype = resource.getContentType().replaceAll("\"", "");
			if(this.mimetype == null)
			{
				this.mimetype = props.getProperty(ResourceProperties.PROP_CONTENT_TYPE);
			}
			if(this.mimetype == null)
			{
				
			}
			else if(this.iconLocation == null)
			{
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
			if(this.iconClass == null && this.mimetype != null)
			{ 
				this.iconClass = ContentTypeImageService.getContentTypeImageClass(this.mimetype);
			}
			if (SecurityService.isSuperUser())
			{
				setIsAdmin(true);
			}
			
			//does this object or its parent collection allow inlineHTML?
			setAllowHtmlInline(isAllowInline(resource));
			setAllowHtmlInlineInherited(isAllowInline(resource.getContainingCollection()));
			
			String size = null;
			String sizzle = null;
			if(typeDef != null)
			{
				size = typeDef.getSizeLabel(entity);
				sizzle = typeDef.getLongSizeLabel(entity);
			}
			if(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH) != null)
			{
				if (size == null)
				{
					size = getSizeLabel(entity);
				}
				if (sizzle == null)
				{
					sizzle = getLongSizeLabel(entity);
				}
			}
			setSize(size);
			setSizzle(sizzle);
			
			this.copyrightStatus = props.getProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
			if(props.getProperty(ResourceProperties.PROP_COPYRIGHT) != null)
			{
				this.copyrightInfo = props.getProperty(ResourceProperties.PROP_COPYRIGHT);
			}
			try 
			{
				this.copyrightAlert = props.getBooleanProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
			} 
			catch (EntityPropertyNotDefinedException e) 
			{
				this.copyrightAlert = false;
			} 
			catch (EntityPropertyTypeException e) 
			{
				this.copyrightAlert = false;
			}
		}
		
		User creator = ResourcesAction.getUserProperty(props, ResourceProperties.PROP_CREATOR);
		if(creator != null)
		{
			String createdBy = creator.getDisplayName();
			setCreatedBy(createdBy);
		}
		User modifier = ResourcesAction.getUserProperty(props, ResourceProperties.PROP_MODIFIED_BY);
		if(modifier != null)
		{
			String modifiedBy = modifier.getDisplayName();
			setModifiedBy(modifiedBy);
		}
		// setCreatedBy(props.getProperty(ResourceProperties.PROP_CREATOR));
		this.setModifiedTime(props.getPropertyFormatted(ResourceProperties.PROP_MODIFIED_DATE));
		this.setCreatedTime(props.getPropertyFormatted(ResourceProperties.PROP_CREATION_DATE));
		
		Site site = null;
		ArrayList<Group> site_groups = new ArrayList<Group>();
		
		String context = getSiteContext(refstr);
		if(context != null) {
			site = getSiteObject(context);
		}
		if(site != null)
		{
			for(Group gr : (Collection<Group>) site.getGroups())
			{
				if(gr == null)
				{
					// ignore?
				}
				else if(gr.getId().equals(site.getId()))
				{
					// ignore
				}
				else
				{
					site_groups.add(gr);
				}
			}
			
			Collections.sort( site_groups, groupComparator );
		}

		setSiteGroups(site_groups);
		
		this.accessMode = entity.getAccess();
		this.inheritedAccessMode = entity.getInheritedAccess();
		//this.effectiveAccess = entity.getInheritedAccess();
		this.groups.clear();
		this.groups.addAll(entity.getGroupObjects());
		this.inheritedGroups.clear();
		this.inheritedGroups.addAll(entity.getInheritedGroupObjects());
		
		if(this.inheritedAccessMode == AccessMode.GROUPED)
		{
			setPossibleGroups(this.inheritedGroups);
		}
		else 
		{
			setPossibleGroups(site_groups);
		}

		this.setPubviewPossible(true);
		this.initialiseRoleIds(entity);

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

		try {
		    this.hiddenWithAccessibleContent = props.getBooleanProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT);
		} catch (Exception e) {
		    this.hiddenWithAccessibleContent = false;
		}
		this.htmlFilter = entity.getProperties().getProperty(ResourceProperties.PROP_ADD_HTML);
		if (this.htmlFilter == null)
		{
			this.htmlFilter = "auto";
		}
	}

	/**
	 * Determine whether or not the given entity is configured to allow inline HTML.
	 * 
	 * @param entity
	 * @return
	 */
	private boolean isAllowInline(ContentEntity entity) {
		if (entity == null)
			return false;
			
		try {
			return entity.getProperties().getBooleanProperty(ResourceProperties.PROP_ALLOW_INLINE);
		} catch (EntityPropertyNotDefinedException e) {
			return false;
		} catch (EntityPropertyTypeException e) {
			return false;
		}
	}

	private void initAllowedAddGroups() 
	{
		if(this.allowedAddGroups == null)
		{
			this.allowedAddGroups = new ArrayList<Group>(); 
		}
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		if(m_reference == null)
		{
			String refStr = contentService.getReference(this.id);
			m_reference = EntityManager.newReference(refStr);
		}
		Collection<Group> groupsWithAddPermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(id);
			Collection<Group> more = contentService.getGroupsWithAddPermission(m_reference.getContainer());
			if(more != null && ! more.isEmpty())
			{
				groupsWithAddPermission.addAll(more);
			}
		}
		else if(AccessMode.GROUPED == this.inheritedAccessMode)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(m_reference.getContainer());
		}
		else if(contentService.getSiteCollection(m_reference.getContext()) != null)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(contentService.getSiteCollection(m_reference.getContext()));
		}
		this.allowedAddGroups.clear();
		if(groupsWithAddPermission != null)
		{
			this.allowedAddGroups.addAll(groupsWithAddPermission);
		}
	}

	private Site getSiteObject(String context) 
	{
		// should /content be caching an object belonging to SiteService?
		Site site = (Site) ThreadLocalManager.get("context@" + context);
		if(site == null)
		{
		    try
		    {
		        site = SiteService.getSite(context);
		        ThreadLocalManager.set("context@" + context, site);
		    }
		    catch (IdUnusedException e)
		    {
		        log.warn("IdUnusedException context == " + context);
		    }
		}
		return site;
	}

	private String getSiteDropboxId(String id) 
	{
		String rv = null;
		if(id != null)
		{
			String parts[] = id.split("/");
			if(parts.length >= 3)
			{
				rv = "/" + parts[1] + "/" + parts[2] + "/";
			}
		}
		return rv;
	}

	protected void setSizzle(String sizzle) 
	{
		this.sizzle = sizzle;
	}

	public void setQuota(String quota) 
	{
		this.quota = quota;
		
	}

	public void setHasQuota(boolean hasQuota) 
	{
		this.hasQuota = hasQuota;
	}

	public void setCanSetQuota(boolean canSetQuota) 
	{
		this.canSetQuota = canSetQuota;
	}

	public void setUserSite(boolean isUserSite) 
	{
		this.isUserSite = isUserSite;
	}

	public ListItem(ResourceToolActionPipe pipe, ListItem parent, Time defaultRetractTime)
	{
		this.constructor = 3;
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		this.entity = null;
		this.containingCollectionId = parent.getId();
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = pipe.getAction().getTypeId();
		this.resourceTypeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(resourceTypeDef != null)
		{
			this.hoverText = resourceTypeDef.getLocalizedHoverText(null);
			this.iconLocation = resourceTypeDef.getIconLocation(this.entity);
			this.iconClass = resourceTypeDef.getIconClass(this.entity);
			String[] args = { resourceTypeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
			// NOTE: Don't do this at home kids, this is hackery of the worst order!
			// Resources of type HTML & Text take on default file names:
			String nameValue = null;
			if (getResourceType().endsWith("HtmlDocumentType")) {
			    nameValue = trb.getString("new.type.html");
			} else if (getResourceType().endsWith("TextDocumentType")) {
			    nameValue = trb.getString("new.type.text");
			} else {
			    nameValue = trb.getFormattedMessage("create.unknown", args); 
			}
			this.name = nameValue;
		}

		this.collection = ResourceType.TYPE_FOLDER.equals(resourceType);
		this.id = "";
		this.parent = parent;
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
			setIsTooBigNav(false);
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
			if(this.iconClass == null && this.mimetype != null)
			{ 
				this.iconClass = ContentTypeImageService.getContentTypeImageClass(this.mimetype);
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
		
		if(this.id != null && ! this.id.trim().equals(""))
		{
			this.isDropbox = contentService.isInDropbox(id);
		}
		else if(this.containingCollectionId != null)
		{
			this.isDropbox = contentService.isInDropbox(this.containingCollectionId);
		}
		else
		{
			this.isDropbox = parent.isDropbox;
		}
		
		this.isCourseSite = parent.isCourseSite();
		
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
		
		this.setSiteGroups(parent.siteGroupsMap.values());
		this.accessMode = AccessMode.INHERITED;
		this.inheritedAccessMode = parent.getEffectiveAccess();
		//this.effectiveAccess = parent.getEffectiveAccess();
		this.groups.clear();
		//this.groups.addAll();
		this.inheritedGroups.clear();
		if(parent.getAccessMode() == AccessMode.GROUPED)
		{
			this.inheritedGroups.addAll(parent.getGroups());
			this.setPossibleGroups(parent.getGroups());
		}
		else
		{
			this.inheritedGroups.addAll(parent.getInheritedGroups());
			this.setPossibleGroups(parent.getPossibleGroups());
		}

		this.inheritedRoleIds.addAll(parent.inheritedRoleIds);
		this.inheritedRoleIds.addAll(parent.roleIds);

		if(this.inheritsRoles())
		{
			this.roleIds.clear();
		}
		else
		{
			this.setPubview(contentService.isPubView(id));
		}
		
		this.hidden = false;
		this.useReleaseDate = false;
		this.useRetractDate = false;
		this.isAvailable = parent.isAvailable();
		
		String refstr = contentService.getReference(id);
		this.isSiteCollection = this.siteCollection(refstr);

		boolean isUserSite = isInWorkspace(parent, refstr);
		setUserSite(isUserSite);

	}

	/**
	 * @param parent
	 * @param refstr
	 * @return
	 */
	protected boolean isInWorkspace(ListItem parent, String refstr) 
	{
		Reference ref = EntityManager.newReference(refstr);
		String contextId = ref.getContext();
		boolean isUserSite = (parent != null && parent.isUserSite()) 
						|| (contextId != null && SiteService.isUserSite(contextId)) 
						|| (refstr != null && refstr.trim().startsWith("/content/user/")) 
						|| (this.containingCollectionId != null && this.containingCollectionId.trim().startsWith("/content/user/"));
		return isUserSite;
	}

	/**
	 * @param entityId
	 */
	public ListItem(String entityId)
	{
		this.constructor = 1;
		this.id = entityId;
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		
		ContentEntity entity = null;
		try
        {
			if(contentService.isCollection(entityId))
			{
				entity = contentService.getCollection(entityId);
		        set(entity);
			}
			else
			{
				entity = contentService.getResource(entityId);
		        set(entity);
			}
        }
        catch (IdUnusedException e)
        {
            // TODO Auto-generated catch block
            log.warn("IdUnusedException " + e);
        }
        catch (TypeException e)
        {
            // TODO Auto-generated catch block
            log.warn("TypeException " + e);
        }
        catch (PermissionException e)
        {
            // TODO Auto-generated catch block
            log.warn("PermissionException " + e);
        }

		this.containingCollectionId = contentService.getContainingCollectionId(entityId);
		
		String refstr = contentService.getReference(id);
		this.isSiteCollection = this.siteCollection(refstr);
		
		boolean isUserSite = isInWorkspace(parent, refstr);
		setUserSite(isUserSite);

	}

	/**
     * @param child
     */
    public void addMember(ListItem member)
    {
        if(this.members == null)
        {
        	this.members = new ArrayList<ListItem>();
        }
        this.members.add(member);
    }

	/**
     * @param action
     */
    public void addMultipleItemAction(ResourceToolAction action)
    {
	    this.multipleItemActions.put(action.getId(), action);
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
	
	public boolean canChangeDisplayName()
	{
		// don't allow changing name of site collections
		boolean allowed = ! this.isSiteCollection();
		
		if(allowed && id != null)
		{
			// don't allow changing name of root collections
			if(ContentHostingService.isRootCollection(this.id))
			{
				allowed = false;
			}
			
			// don't allow changing names of system-provided dropbox folders 
			// (maintainer's dropbox root and access-user's dropbox root)
			// but do allow changing subfolders within access-user's dropbox.
			if(allowed && this.isDropbox())
			{
			 //Admin can always change dropbox folder name
			 if (isAdmin) {
			   allowed = true;
			 } else {			
				int depth = ContentHostingService.getDepth(this.id, org.sakaiproject.content.api.ContentHostingService.COLLECTION_DROPBOX);
				allowed = (depth > 2);
			}
			}
		}
		
		return allowed;
	}
	
	/**
     * @param group
     * @return
     */
    public boolean allowedRemove(Group group)
    {
    	boolean allowed = false;
    	
    	// instead of getting all groups with remove access, could we query for THIS group?
    	// or is this more efficient because we get them all at once?
    	if(this.allowedRemoveGroups == null)
    	{
    		initAllowedRemoveGroups();
    	}
    	for(Group gr : this.allowedRemoveGroups)
    	{
    		if(gr == null)
    		{
    			// ignore?
    		}
    		else if(gr.getId().equals(group.getId()))
    		{
    			allowed = true;
    			break;
    		}
    	}
    	
    	return allowed;
    }

	protected void initAllowedRemoveGroups() 
	{
		if(this.allowedRemoveGroups == null)
		{
			this.allowedRemoveGroups = new ArrayList<Group>(); 
		}
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		if(m_reference == null)
		{
			String refStr = contentService.getReference(this.id);
			m_reference = EntityManager.newReference(refStr);
		}
		Collection<Group> groupsWithRemovePermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(id);
			String container = m_reference.getContainer();
			if(container != null)
			{
				Collection<Group> more = contentService.getGroupsWithRemovePermission(container);
				if(more != null && ! more.isEmpty())
				{
					groupsWithRemovePermission.addAll(more);
				}
			}
		}
		else if(AccessMode.GROUPED == this.inheritedAccessMode)
		{
			if(this.parent != null && this.parent.allowedRemoveGroups != null)
			{
				groupsWithRemovePermission = new ArrayList(this.parent.allowedRemoveGroups);
			}
			else if(m_reference.getContainer() != null)
			{
				groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(m_reference.getContainer());
			}
		}
		else if(m_reference.getContext() != null && contentService.getSiteCollection(m_reference.getContext()) != null)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(contentService.getSiteCollection(m_reference.getContext()));
		}
		this.allowedRemoveGroups.clear();
		if(groupsWithRemovePermission != null)
		{
			this.allowedRemoveGroups.addAll(groupsWithRemovePermission);
		}
	}

	public boolean canRead()
	{
		return isPermitted(ContentPermissions.READ);
	}

        public boolean getCanRevise()
        {
                return isPermitted(ContentPermissions.REVISE);
        } 


	/**
     * @return
     */
    public boolean canSelect()
    {
    	return canSelect;
    }

	protected void captureAccess(ParameterParser params, String index) 
	{
		String access_mode = params.getString("access_mode" + index);
		
		if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
		{
			// we inherit more than one group and must check whether group access changes at this item
			String[] access_groups = params.getStrings("access_groups" + index);
			
			SortedSet<String> new_groups = new TreeSet<String>();
			if(access_groups != null)
			{
				new_groups.addAll(Arrays.asList(access_groups));
			}
			SortedSet<String> new_group_refs = convertToRefs(new_groups);
			
			Collection inh_grps = getInheritedGroupRefs();
			boolean groups_are_inherited = (new_group_refs.size() == inh_grps.size()) && inh_grps.containsAll(new_group_refs);
			
			if(groups_are_inherited)
			{
				new_groups.clear();
				setGroupsById(new_groups);
				setAccessMode(AccessMode.INHERITED);
			}
			else
			{
				setGroupsById(new_groups);
				setAccessMode(AccessMode.GROUPED);
			}
			
			setPubview(false);
		}
		else if(ResourcesAction.PUBLIC_ACCESS.equals(access_mode))
		{
			if(! isPubviewInherited())
			{
				setPubview(true);
				setAccessMode(AccessMode.INHERITED);
			}
		}
		else if(AccessMode.INHERITED.toString().equals(access_mode))
		{
			captureAccessRoles(params, index);
			setAccessMode(AccessMode.INHERITED);
			this.groups.clear();
		}
	}

	/**
	 * Set up the access roles as defined by checkboxes in the form.
	 * Should only be called if it is compatible with the form submission
	 *    e.g.  when access mode is not set to groups.
	 */
	protected void captureAccessRoles(ParameterParser params, String index) {
		Set<String> formRoleIds = new LinkedHashSet<String>();

		String[] rolesArray = params.getStrings("access_roles" + index);
		if (rolesArray != null) {
			formRoleIds.addAll(Arrays.asList(rolesArray));
			formRoleIds.retainAll(availableRoleIds());
		}

		this.roleIds = formRoleIds;
	}

	protected void captureAvailability(ParameterParser params, String index) 
	{
		// availability
		String hiddenParam = params.getString("hidden" + index);
		this.hidden = PROP_HIDDEN_TRUE.equalsIgnoreCase(hiddenParam);
		this.hiddenWithAccessibleContent = "hidden_with_accessible_content".equals(hiddenParam);
		boolean use_start_date = params.getBoolean("use_start_date" + index);
		boolean use_end_date = params.getBoolean("use_end_date" + index);
		
		this.useReleaseDate = use_start_date;
		if(use_start_date)
		{
			int begin_year = params.getInt("release_year" + index);
			int begin_month = params.getInt("release_month" + index);
			int begin_day = params.getInt("release_day" + index);
			int begin_hour = params.getInt("release_hour" + index);
			int begin_min = params.getInt("release_minute" + index);
			String release_ampm = params.getString("release_ampm" + index);
			if("pm".equals(release_ampm))
			{
				if( begin_hour < 12)
				{
					begin_hour += 12;
				}
			}
			else if(begin_hour == 12)
			{
				begin_hour = 0;
			}
			this.releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
		}
		else
		{
			this.releaseDate = null;
		}
		
		this.useRetractDate = use_end_date;
		if(use_end_date)
		{
			int end_year = params.getInt("retract_year" + index);
			int end_month = params.getInt("retract_month" + index);
			int end_day = params.getInt("retract_day" + index);
			int end_hour = params.getInt("retract_hour" + index);
			int end_min = params.getInt("retract_minute" + index);
			String retract_ampm = params.getString("retract_ampm" + index);
			if("pm".equals(retract_ampm))
			{
				if(end_hour < 12)
				{
					end_hour += 12;
				}
			}
			else if(end_hour == 12)
			{
				end_hour = 0;
			}
			this.retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
		}
		else
		{
			this.retractDate = null;
		}
		
		String selectedConditionValue = params.get("selectCondition" + index);
		if (selectedConditionValue == null) return;
		String[] conditionTokens = selectedConditionValue.split("\\|");
		int selectedIndex = Integer.valueOf(conditionTokens[0]);
		if ((selectedIndex == 9) || (selectedIndex == 10)) {
			this.conditionArgument = params.get("assignment_grade" + index);
			Double argument = null;
			try {
				argument = Double.valueOf(this.conditionArgument);
			} catch (NumberFormatException nfe) {
				this.numberFieldIsInvalid = true;
				//Not much we can do if its not a number
				return;
			}
			
			String submittedResourceFilter = params.get("selectResource" + index);
			// the number of grade points are tagging along for the ride. chop this off.
			String[] resourceTokens = submittedResourceFilter.split("/");
			this.conditionAssignmentPoints = resourceTokens[4];
			Double assignmentPoints = Double.valueOf(conditionAssignmentPoints);
			if ((argument > assignmentPoints) || (argument < 0)) {
				this.numberFieldIsOutOfRange = true;
			}
		}

		
	}
	
	public String getConditionArgument() {
		return conditionArgument;
	}

	public void setConditionArgument(String conditionArgument) {
		this.conditionArgument = conditionArgument;
	}

	public boolean isUseConditionalRelease() {
		return useConditionalRelease;
	}

	public void setUseConditionalRelease(boolean useConditionalRelease) {
		this.useConditionalRelease = useConditionalRelease;
	}

	public String getSubmittedFunctionName() {
		return submittedFunctionName;
	}

	public void setSubmittedFunctionName(String submittedFunctionName) {
		this.submittedFunctionName = submittedFunctionName;
	}

	public String getSubmittedResourceFilter() {
		return submittedResourceFilter;
	}

	public void setSubmittedResourceFilter(String submittedResourceFilter) {
		this.submittedResourceFilter = submittedResourceFilter;
	}

	public String getSelectedConditionKey() {
		return selectedConditionKey;
	}

	public void setSelectedConditionKey(String selectedConditionKey) {
		this.selectedConditionKey = selectedConditionKey;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	protected void captureCopyright(ParameterParser params, String index) 
	{
		// rights
		String copyright = params.getString("copyright" + index);
		if(copyright == null || copyright.trim().length() == 0)
		{
			// do nothing -- there must be no copyright dialog
		}
		else
		{
			this.copyrightInfo = copyright;
			
			String newcopyright = params.getString("newcopyright" + index);
			
			if(newcopyright == null || newcopyright.trim().length() == 0)
			{
				this.copyrightStatus = null;
			}
			else
			{
				this.copyrightStatus = newcopyright;
			}
			
			boolean copyrightAlert = params.getBoolean("copyrightAlert" + index);
			this.copyrightAlert = copyrightAlert;
		}
	}

	protected void captureDescription(ParameterParser params, String index) 
	{
		// description
		String description = params.getString("description" + index);
		if(description != null)
		{
			StringBuilder errorMessages = new StringBuilder();
			description = FormattedText.processFormattedText(description, errorMessages);
			// what to do with errorMessages
			if(errorMessages.length() > 0)
			{
				log.warn("ListItem.captureDescription() containingCollectionId: " + this.containingCollectionId + " id: " + this.id + " error in FormattedText.processFormattedText(): " + errorMessages.toString());
			}
			this.setDescription(description);
		}
	}

	protected void captureCHHMountpoint(ParameterParser params, String index) 
	{
		// content hosting handler bean name
		String chhmountpoint = params.getString(ContentHostingHandlerResolver.CHH_BEAN_NAME);
		this.setCHHMountpoint(chhmountpoint);
	}

	public void captureProperties(ParameterParser params, String index) 
	{
		captureCHHMountpoint(params, index);
		captureDisplayName(params, index);
		captureDescription(params, index);
		captureCopyright(params, index);
		captureAccess(params, index);
		captureAvailability(params, index);
		if (isAdmin) {
			captureHtmlInline(params, index);
		}
		if(this.canSetQuota)
		{
			captureQuota(params, index);
		}
		if(! isUrl() && ! isCollection() && this.mimetype != null)
		{
			captureMimetypeChange(params, index);
		}
		if (isHtml())
		{
			captureHtmlChange(params, index);
		}
		if(this.metadataGroups != null && !this.metadataGroups.isEmpty())
		{
			this.captureOptionalPropertyValues(params, index);
		}
	}

	protected void captureHtmlInline(ParameterParser params, String index) {
		log.debug("got allow inline of " + params.getBoolean("allowHtmlInline" + index));
		this.allowHtmlInline = params.getBoolean("allowHtmlInline" + index);
	}

	protected void captureHtmlChange(ParameterParser params, String index) 
	{
		String htmlFilter = params.getString("html_filter" + index);
		if(htmlFilter != null)
		{
			this.htmlFilter = htmlFilter;
		}
		
	}
	
	//Validates a mimetype change
	protected void validateMimetype() {
		//If the mime type matches up with a default unknown type, set it to the unknown type
		if (ContentTypeImageService.getContentTypeImage(null).equals(ContentTypeImageService.getContentTypeImage(this.mimetype))){
			//Should get this from the api but it's not in there, just in the impl
			this.mimetype = UNKNOWN_TYPE;
		}
	}

	protected void captureMimetypeChange(ParameterParser params, String index) 
	{
		String mimeCategory = params.getString("mime_category" + index);
		if(mimeCategory != null)
		{
			String mimeSubtype = params.getString("mime_subtype" + index);
			if(mimeSubtype != null)
			{
				String mimeType = mimeCategory.trim() + "/" + mimeSubtype.trim();
				if(mimeType.equals(this.mimetype))
				{
					
				}
				else
				{
					this.mimetype = mimeType;
				}
			}
		}
		validateMimetype();
	}

	protected void captureQuota(ParameterParser params, String index) 
	{
		String setQuota = params.getString("setQuota" + index);
		if(setQuota != null)
		{
			this.hasQuota = params.getBoolean("hasQuota" + index);
			if(this.hasQuota)
			{
				String quota = params.getString("quota" + index);
				if(quota != null && quota.trim().matches("^\\d+$"))
				{
					this.quota = quota.trim();
				}
			}
			else
			{
				this.quota = null;
			}		
		}
							
	}

	protected void captureDisplayName(ParameterParser params, String index) 
	{
		String displayName = params.getString("displayName" + index);
		if(displayName == null || displayName.trim().equals(""))
		{
			if(this.name == null || this.name.trim().equals(""))
			{
				String[] delimiters = {"/", "\\"};
				displayName = this.id;
				if(displayName != null)
				{
					for(int i = 0; i < delimiters.length; i++)
					{
						if(displayName.lastIndexOf(delimiters[i]) >= 0)
						{
							displayName = displayName.substring(displayName.lastIndexOf(delimiters[i]) + 1);
						}
					}
				}
				this.setName(displayName);
			}
		}
		else
		{
			this.setName(displayName);
		}
	}

	/**
     * @param item
     * @return
     */
    public List<ListItem> convert2list()
    {
    	List<ListItem> list = new ArrayList<ListItem>();
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
     * @param new_groups
     * @return
     */
	public SortedSet<String> convertToRefs(Collection<String> groupIds) 
	{
		SortedSet<String> groupRefs = new TreeSet<String>();
		for(String groupId : groupIds)
		{
			Group group = (Group) this.siteGroupsMap.get(groupId);
			if(group != null)
			{
				groupRefs.add(group.getReference());
			}
		}
		return groupRefs;

	}

	/**
	 * When setting access on a resource the available options will change depending on what properties it inherits.
	 *   E.g. if groups are inherited it is not possible to broaden access, so display a message to that extent.
	 * @return the instruction string
	 */
	public String getAccessInstruction()
	{
		String label = "";
		
		if(isSiteOnly())
		{
			checkParent();
			//Site has *NO* groups and public-view is *NOT* enabled on the server 
			if(this.parent == null)
			{
				label = trb.getString("access.site.noparent");
				log.warn("ListItem.getLongAccessLabel(): Unable to display label because isSiteOnly == true and parent == null and constructor == " + this.constructor);  
			}
			else
			{
				label = trb.getFormattedMessage("access.site.nochoice", new String[]{parent.getName()});
			}
		}
		else if(isGroupInherited())
		{
			//Grouped access is inherited
			label = getMultiGroupLabel();
		}
		else if(isCollection())
		{
			label = trb.getString("edit.who.fldr");
		}
		else
		{
			label = trb.getString("edit.who");
		}
		
		return label;
	}

	/**
     * 
     */
    protected void checkParent()
    {
	    // Public access inherited from parent 
	    if(parent == null)
	    {
	    	if(this.containingCollectionId == null)
	    	{
	    		if(this.id == null)
	    		{
					log.warn("ListItem.checkParent(): parent == null, containingCollectionId == null, id == null and constructor == " + this.constructor, new Throwable());  
	    			return;
	    		}
	    		this.containingCollectionId = ContentHostingService.getContainingCollectionId(this.id);
	    	}
	    	try
	        {
	            parent = new ListItem(ContentHostingService.getCollection(this.containingCollectionId));
	        }
	        catch (IdUnusedException e)
	        {
	            // TODO Auto-generated catch block
	            log.warn("IdUnusedException ", e);
	        }
	        catch (TypeException e)
	        {
	            // TODO Auto-generated catch block
	            log.warn("TypeException ", e);
	        }
	        catch (PermissionException e)
	        {
	            // TODO Auto-generated catch block
	            log.warn("PermissionException ", e);
	        }
	    }
    }
	
	/**
     * @return the accessMode
     */
    public AccessMode getAccessMode()
    {
    	return accessMode;
    }
	
	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl()
	{
		return this.accessUrl;
	}
	
	/**
     * @return the addActions
     */
    public List<ResourceToolAction> getAddActions()
    {
    	return addActions;
    }
	
	/**
     * @return the createdBy
     */
    public String getCreatedBy()
    {
    	if(createdBy == null)
    	{
    		createdBy = "";
    	}
    	return createdBy;
    }
    
	/**
	 * @return
	 */
	public List<ListItem> getCollectionPath()
	{
		LinkedList<ListItem> path = new LinkedList<ListItem>();
		if(contentService == null)
		{
			contentService = (org.sakaiproject.content.api.ContentHostingService) ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		}
		
		ContentCollection containingCollection = null;
		ContentEntity entity = this.getEntity();
		if(entity == null)
		{
			try 
			{
				containingCollection = contentService.getCollection(this.containingCollectionId);

			} 
			catch (IdUnusedException e) 
			{
				log.warn("IdUnusedException " + e);
			} 
			catch (TypeException e) 
			{
				log.warn("TypeException " + e);
			} 
			catch (PermissionException e) 
			{
				log.warn("PermissionException " + e);
			}
			
		}
		else
		{
			containingCollection = entity.getContainingCollection();
		}
		
		ListItem previousItem = null;
		while(containingCollection != null && ! contentService.isRootCollection(containingCollection.getId()))
		{
			if(previousItem != null)
			{
				path.addFirst(previousItem);
			}
			previousItem = new ListItem(containingCollection);
			containingCollection = containingCollection.getContainingCollection();
		}
//		if(containingCollection != null)
//		{
//			path.addFirst(new ListItem(containingCollection));
//		}
		
		return path;
	}
	
	/**
     * @return the depth
     */
    public int getDepth()
    {
    	return depth;
    }
	
	/**
	 * @return
	 */
	public String getDescription() 
	{
		return description;
	}
	
	/**
     * @return the effectiveAccess
     */
    public AccessMode getEffectiveAccess()
    {
    	AccessMode access = this.accessMode;
    	
		if(AccessMode.INHERITED == access)
		{
			access = this.inheritedAccessMode;
		}

		return access;
	}
    
    /**
     * @return
     */
    public Collection<Group> getEffectiveGroups()
    {
    	Collection<Group> groups = new ArrayList<Group>();
    	
    	
    	
    	return groups;
    }
	
	/**
     * @return
     */
    public ContentEntity getEntity()
    {
	    return this.entity;
    }
    
    public String[] getGroupNameArray(boolean includeParentName)
    {
    	Collection<Group> groups = this.groups;
    	if(AccessMode.INHERITED == this.accessMode)
    	{
    		groups = this.inheritedGroups;
    	}
    	
    	int size = groups.size();
		if(includeParentName)
		{
			size += 1;
		}
    	String[] names = new String[size];
    	
    	int index = 0;
    	if(includeParentName)
    	{
    		ListItem parentItem = this.getParent();
            names[index] = parentItem != null ? parentItem.name : "";
    		index++;
    	}
    	for(Group group : groups)
    	{
    		if(group == null)
    		{
    			// ignore
    		}
    		else
    		{
	    		names[index] = Validator.escapeHtml(group.getTitle());
	    		index++;
    		}
    	}
    	
    	return names;
    }

    /**
     * @deprecated Use #getShortAccessLabel instead
     */
    public String getEffectiveAccessLabel()
    {
        return getShortAccessLabel();
    }

    /**
     * Provides a short description of the access rights, for example when listing in a table.
     * This might not include details of all access rights for space reasons.
     * @return the description String
     */
    public String getShortAccessLabel()
    {
        return getAccessLabel(false);
    }

    /**
     * Provides a description of the access rights which is more verbose than #getShortAccessLabel
     * @return the description String
     */
    public String getLongAccessLabel()
    {
        return getAccessLabel(true);
    }

    private String getAccessLabel(final boolean useLongerLabel)
    {
        String label;

        if(AccessMode.GROUPED == this.getEffectiveAccess())
        {
            label = accessLabelForGroups(useLongerLabel);
        }
        else if (this.inheritsRoles() || this.hasRoles())
        {
            label = accessLabelForRoles(useLongerLabel);
        }
        else if(this.isDropbox)
        {
            label = useLongerLabel ? rb.getString("access.dropbox1") : rb.getString("access.dropbox");
        }
        else
        {
            // Site access
            label = useLongerLabel ? rb.getString("access.site1") : rb.getString("access.site");
        }

        return label;
    }

    /**
     * Constructs a nice language representation of the roles that are defined agains the list item
     * If there are more than 2 roles defined it will show "Role_A and 5 others".
     * @param useLongerLabel set to true if you want a label that is a natural sentence.
     *   e.g. "Oxford members" vs "Visible to Oxford members."
     * @return 
     */
    public String accessLabelForRoles(boolean useLongerLabel)
    {
        String label;
        List<String> roleIds = new ArrayList<String>(this.roleIds);
        roleIds.addAll(this.inheritedRoleIds);

        if (roleIds.size() == 0)
        {
            log.warn("ListItem: Constructing a roles access label with no roles defined");
            return "";
        }

        roleIds = pubviewAtFrontOfList(roleIds);

        if (useLongerLabel)
        {
            List<String> roleLabels = new ArrayList<String>();
            for (String roleId : roleIds) {
                roleLabels.add(getLabelForRole(roleId));
            }

            if(roleIds.size() > 6)
            {
                label = rb.getFormattedMessage("access.roleLabel.long.X", roleLabels.toArray());
            }
            else
            {
                label = rb.getFormattedMessage("access.roleLabel.long." + roleIds.size(), roleLabels.toArray());
            }
        }
        else
        {
            String firstLabel = getLabelForRole(roleIds.get(0));
            // Decide how to format the string based on how many roles there are
            switch (roleIds.size())
            {
                case 1:
                    label = firstLabel;
                    break;
                case 2:
                    String secondLabel = getLabelForRole(roleIds.get(1));
                    String[] twoLabelParams = {firstLabel, secondLabel};
                    label = rb.getFormattedMessage("access.roleLabel.two", twoLabelParams);
                    break;
                default:
                    String[] multiLabelParams = {firstLabel, Integer.toString(roleIds.size())};
                    label = rb.getFormattedMessage("access.roleLabel.moreThanTwo", multiLabelParams);
                    break;
            }
        }

        return label;
    }

    /** Gets a label for a given roleId as defined in the resource bundle **/
    private String getLabelForRole(String roleId) {
        return rb.getString(String.format("access.role%s", roleId));
    }

    /**
     * If pubview is in a list of roles then put it at the front of the list, if we are going to talk
     * about any roles and we are restricted for space then it is important that the fact that the
     * resource is publically viewable is known.
     * @param roleIds a list of role ids
     * @return
     */
    private List<String> pubviewAtFrontOfList(List<String> roleIds) {
        // Put pubview at the front of the list
        String chosenId;
        if (roleIds.contains(PUBVIEW_ROLE))
        {
            chosenId = PUBVIEW_ROLE;
        }
        else
        {
            chosenId = roleIds.iterator().next();
        }
        roleIds.remove(chosenId);

        List<String> reorderedRoleIds = new ArrayList<String>();
        reorderedRoleIds.add(chosenId);
        reorderedRoleIds.addAll(roleIds);
        return reorderedRoleIds;
    }

    /**
     * Provides a description of the groups that have been assigned to this ListItem
     * @param useLongerLabel provides a long description if true and a short one if false
     * @return the description
     */
    private String accessLabelForGroups(boolean useLongerLabel)
    {
        final String groupNames = getGroupNamesAsString();
        if (useLongerLabel)
        {
            return rb.getFormattedMessage("access.group1",  new Object[]{groupNames});
        }
        else
        {
            return groupNames.isEmpty() ? rb.getString("access.group.missing") : rb.getString("access.group");
        }
    }
    
    private String getGroupNamesAsString()
    {
    	StringBuffer names = new StringBuffer();
    	String[] groups = getGroupNameArray(false);
    	for(int i = 0; i < groups.length; i++)
    	{
    		if(i > 0 && i < groups.length)
    		{
    			names.append(", ");
    		}
    		names.append(groups[i]);
    	}
    	return names.toString();
    }

    /**
     * @deprecated
     */
    public String getEffectiveGroupsLabel()
    {
        return getShortAccessLabel();
    }

	protected int getNumberOfGroups()
    {
		int size = 0;
    	
    	if(AccessMode.INHERITED == this.accessMode)
    	{
    		size = this.inheritedGroups.size();
    	}
    	else
    	{
    		size = this.groups.size();
    	}
    	
    	return size;
    }
	
	public String getMultiGroupLabel()
	{
		int size = getNumberOfGroups();
		String label = "";
		if(size > 9)
		{
			label = trb.getFormattedMessage("access.groupsX",  getGroupNameArray(true));
		}
		else
		{
			label = trb.getFormattedMessage("access.groups" + size,  getGroupNameArray(true));
		}
		return label;
	}

	/**
     * @return
     */
    public Collection<String> getGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.groups)
    	{
    		if(group != null)
    		{
    			refs.add(group.getReference());
    		}
    	}
    	return refs;
    }

    /**
     * This indicates whether any of the groups defined on the entity no longer exist
     * @return true if there is a mismatch and false otherwise
     */
    public boolean groupsAreMissing() {
        return entity != null && entity.getGroups().size() != groups.size();
    }

	/**
     * @return the groups
     */
    public Collection<Group> getGroups()
    {
    	return new ArrayList<Group>(groups);
    }

	/**
	 * @return the hoverText
	 */
	public String getHoverText()
	{
		return this.hoverText;
	}

	/**
	 * @return the iconLocation
	 */
	public String getIconLocation()
	{
		return this.iconLocation;
	}
	
	/**
	 * @return the iconClass
	 */
	public String getIconClass()
	{
		return this.iconClass;
	}

	/**
	 * @return
	 */
	public String getId() 
	{
		return id;
	}

	/**
     * @return
     */
    public Collection<String> getInheritedGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.inheritedGroups)
    	{
    		if(group != null)
    		{
    			refs.add(group.getReference());
    		}
    	}
    	return refs;
    }

	/**
     * @return the inheritedGroups
     */
    public Collection<Group> getInheritedGroups()
    {
    	return new ArrayList<Group>(inheritedGroups);
    }

	public List<ListItem> getMembers() 
	{
		return members;
	}

	/**
	 * @return the mimetype
	 */
	public String getMimetype()
	{
		return this.mimetype;
	}
	
	/**
     * @return the modifiedTime
     */
    public String getModifiedTime()
    {
    	return modifiedTime;
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

	public String getName() 
	{
		return name;
	}

	/**
     * @return the otherActions
     */
    public List<ResourceToolAction> getOtherActions()
    {
    	return otherActions;
    }

	/**
     * @return the otherActionsLabel
     */
    public String getOtherActionsLabel()
    {
    	return otherActionsLabel;
    }

	/**
	 * @return the permissions
	 */
	public Set<ContentPermissions> getPermissions()
	{
		return this.permissions;
	}

	/**
     * @return
     */
    public Collection<String> getPossibleGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.possibleGroups)
    	{
    		if(group != null)
    		{
    			refs.add(group.getReference());
    		}
    	}
    	return refs;
    }

	/**
     * @return the possibleGroups
     */
    public Collection<Group> getPossibleGroups()
    {
    	return new ArrayList<Group>(possibleGroups);
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
     * @return the size
     */
    public String getSize()
    {
    	return size;
    }
    
    public String getTarget()
    {
    	return Validator.getResourceTarget(this.mimetype);
    }

	public boolean hasMultipleItemAction(String key)
    {
    	return this.multipleItemActions.containsKey(key);
    }

	public boolean hasMultipleItemActions()
    {
    	return ! this.multipleItemActions.isEmpty();
    }

	/**
     * @return the isAvailable
     */
    public boolean isAvailable()
    {
    	return isAvailable;
    }

	/**
	 * @return the collection
	 */
	public boolean isCollection()
	{
		return this.collection;
	}

	public boolean isEmpty()
	{
		return this.isEmpty;
	}
    
    /**
     * @return the isExpanded
     */
    public boolean isExpanded()
    {
    	return isExpanded;
    }
    
	public boolean isGroupInherited()
    {
    	return AccessMode.GROUPED == this.inheritedAccessMode;
    }

	/**
     * @return
     */
    public boolean isGroupPossible()
    {
    	boolean rv = false;
    	
    	//SAK-18986 do we have any possible groups? if so, groups are possible.
    	//each of the possible groups are checked later.
    	if(getPossibleGroups().size() > 0) {
    		return true;
    	}
    	
    	if(this.accessMode == AccessMode.INHERITED && parent != null)
    	{
    		rv = parent.isGroupPossible();
    	}
    	else
    	{
	    	// can this be done more efficiently without getting all groups with add allowed?
	    	if(this.allowedAddGroups == null)
	    	{
	    		initAllowedAddGroups();
	    	}
	    	rv = this.allowedAddGroups != null && ! this.allowedAddGroups.isEmpty();
    	}
    	return rv;
    }

	/**
     * @return the hidden
     */
    public boolean isHidden()
    {
    	return hidden;
    }
    
    public boolean isHtml()
    {
    	return "text/html".equals(mimetype);
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
    		if(gr == null)
    		{
    			// ignore
    		}
    		else if(gr.getId().equals(group.getId()))
    		{
    			isLocal = true;
    			break;
    		}
    	}
    	
    	return isLocal;
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
     * @param group
     * @return
     */
    public boolean isPossible(Group group)
    {
    	if (group == null || group.getContainingSite() == null) return false;
    	
    	String userId = UserDirectoryService.getCurrentUser().getId();
    	if (securityService.unlock(userId,ContentHostingService.AUTH_RESOURCE_ALL_GROUPS, group.getContainingSite().getReference())) return true;
    			
    	return securityService.unlock(userId,SiteService.SITE_VISIT,group.getReference());
    }
    
	/**
     * @return the isPubview
     */
    public boolean isPubview()
    {
        return this.roleIds.contains(PUBVIEW_ROLE);
    }

	/**
     * @return the isPubviewInherited
     */
    public boolean isPubviewInherited()
    {
        return this.inheritedRoleIds.contains(PUBVIEW_ROLE);
    }

	/**
     * @return
     */
    public boolean isPubviewPossible()
    {
        return availableRoleIds().contains(PUBVIEW_ROLE);
    }

    /**
     * Sets the initial list of role ids and inherited role ids defined in the List Item, including pubview
     * @param entity the entity to get the roleIds frome
     */
    protected void initialiseRoleIds(ContentEntity entity) {
        for (String roleId : availableRoleIds()) {
            if (contentService.isInheritingRoleView(entity.getId(), roleId)) {
                this.inheritedRoleIds.add(roleId);
            } else if (contentService.isRoleView(entity.getId(), roleId)) {
                this.roleIds.add(roleId);
            }
        }
    }

    /**
     * Returns the list of roleIds currently defined for this List Item, this will include the Pubview (anon) role
     * @return
     */
    public Set<String> getRoleIds() {
        return roleIds;
    }

    /**
     * Gets the list of inheritedRoleIds currently defined for this List Item, which may include the Pubview (anon) role.
     * @return a set of role ids
     */
    public Set<String> getInheritedRoleIds() {
        return this.inheritedRoleIds;
    }

    /**
     * Asks the Server Configuration Service to get a list of available roles with the prefix "resources.enabled.roles""
     * We should expect language strings for these to be defined in the bundles.
     * @return a set of role ids that can be used
     */
    public Set<String> availableRoleIds() {
        String[] configStrings = ServerConfigurationService.getStrings("resources.enabled.roles");

        LinkedHashSet<String> availableIds = new LinkedHashSet<String>();

        if(configStrings != null) {
            availableIds.addAll(Arrays.asList(configStrings));
        } else {
            // By default just include the public
            availableIds.add(PUBVIEW_ROLE);
        }

        return availableIds;
    }

    /**
     * Uses availableRoleIds to determine whether roles are available to be used, includes roleIds.
     * @return ture if no roles are available, false otherwise.
     */
    public boolean rolesAreAvailable() {
        return !availableRoleIds().isEmpty();
    }

    /**
     * Checks whether the List Item has the role access set for this role.
     * @param roleId the role to check.
     * @return true if the role is enabled, false otherwise.
     */
    public boolean hasRoleEnabled(String roleId) {
        return this.roleIds.contains(roleId);
    }

    /**
     * Checks whether the List Item has any inherited roles defined.
     * @return true if the List Item inherits roles, false otherwise
     */
    public boolean inheritsRoles() {
        return this.inheritedRoleIds != null && !this.inheritedRoleIds.isEmpty();
    }

    /**
     * Checks whether the List Item inherits a given role.
     * Used in the UI to determine whether some elements should be displayed
     * @param roleId  the id of the role to check for inheritance
     * @return        true if the List Item inherits the role, false otherwise
     */
    public boolean inheritsRole(String roleId) {
        return this.inheritedRoleIds != null && this.inheritedRoleIds.contains(roleId);
    }

    /**
     * Checks whether the List Item has any roles defined.
     * @return true if the List Item has roles, false otherwise
     */
    public boolean hasRoles() {
        return this.roleIds != null && !this.roleIds.isEmpty();
    }

	public boolean isSelected() 
	{
		return selected;
	}
    
    /**
	  * Does this entity inherit grouped access mode with a single group that has access?
	  * @return true if this entity inherits grouped access mode with a single group that has access, and false otherwise.
	  */
	 public boolean isSingleGroupInherited()
	 {
		 //Collection groups = getInheritedGroups();
		 return // AccessMode.INHERITED.toString().equals(this.m_access) && 
		 AccessMode.GROUPED == this.inheritedAccessMode && 
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
		 return !this.isPubviewInherited() && !isGroupInherited() && !isSingleGroupInherited();
	 }

	public boolean isTooBig()
	{
		return this.isTooBig;
	}
	
    
         /**
	 * Hides href on resource folders based on a configurable limit sakai.content.resourceLimit
	 * 
	 * @param isTooBigNav flag
	 */
	public boolean isTooBigNav()
	{
		return this.isTooBigNav;
	}
	
	public boolean isUrl()
	{
		return this.resourceType != null && this.resourceType.equals(ResourceType.TYPE_URL);
	}

	/**
     * @param accessMode the accessMode to set
     */
    public void setAccessMode(AccessMode accessMode)
    {
    	this.accessMode = accessMode;
    }

	/**
	 * @param accessUrl the accessUrl to set
	 */
	public void setAccessUrl(String accessUrl)
	{
		this.accessUrl = accessUrl;
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
     * @param isAvailable the isAvailable to set
     */
    public void setAvailable(boolean isAvailable)
    {
    	this.isAvailable = isAvailable;
    }

	 /**
     * @param canSelect
     */
    public void setCanSelect(boolean canSelect)
    {
	    this.canSelect  = canSelect;
    }

    /**
	 * @param collection the collection to set
	 */
	public void setCollection(boolean collection)
	{
		this.collection = collection;
	}

	/**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy)
    {
    	this.createdBy = createdBy;
    }

	protected void setCreatedTime(String createdTime) 
	{
		this.createdTime = createdTime;
	}

	/**
     * @param depth the depth to set
     */
    public void setDepth(int depth)
    {
    	this.depth = depth;
    }

	/**
	 * @param description
	 */
	public void setDescription(String description) 
	{
		this.description = description;
	}

	/**
     * @param chhmountpoint the chhmountpoint (bean name) to set
     */
    public void setCHHMountpoint(String chhmountpoint)
    {
    	this.chhmountpoint = chhmountpoint;
    }

	/**
    
    /**
     * Sets expanded status of the list item.  Also updates the iconLocation 
     * if the item is of an expandable resource type.
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(boolean isExpanded)
    {
    	this.isExpanded = isExpanded;
    	// need resourceTypeDef to update the iconLocation
    	if(this.resourceTypeDef == null && this.resourceType != null)
    	{
    		// need registry to get the resourceTypeDef if it's null and the typeDef's name isn't
    		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get(ResourceTypeRegistry.class);
    		if(registry != null)
    		{
    			this.resourceTypeDef = registry.getType(this.resourceType);
    		}
    	}
    	// iconLocation needs updating only if it's an expandable type
    	if(this.resourceTypeDef != null && this.resourceTypeDef instanceof ExpandableResourceType)
    	{
 			this.expandIconLocation = ((ExpandableResourceType) resourceTypeDef).getIconLocation(this.entity, this.isExpanded);
 			this.iconClass = ((ExpandableResourceType) resourceTypeDef).getIconClass(this.entity, this.isExpanded);
			this.expandLabel = ((ExpandableResourceType) resourceTypeDef).getLocalizedHoverText(this.entity, this.isExpanded);
		}
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
     * @param new_groups
     */
    public void setGroupsById(Collection<String> groupIds)
    {
    	this.groups.clear();
    	if(groupIds != null && ! groupIds.isEmpty())
    	{
	    	for(String groupId : groupIds)
	    	{
	    		Group group = this.siteGroupsMap.get(groupId);
	    		this.groups.add(group);
	     	}
    	}
    }
    
    /**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden)
    {
    	this.hidden = hidden;
    }
	
	/**
	 * @return the hiddenWithAccessibleContent
	 */
	public boolean isHiddenWithAccessibleContent()
	{
		return this.hiddenWithAccessibleContent;
	}

	/**
	 * @param hover
	 */
	public void setHoverText(String hover)
	{
		this.hoverText = hover;
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
	 * @param id
	 */
	public void setId(String id) 
	{
		this.id = id;
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
     * @param isEmpty
     */
    public void setIsEmpty(boolean isEmpty)
    {
        this.isEmpty = isEmpty;
    }

	/**
     * @param b
     */
    public void setIsTooBig(boolean isTooBig)
    {
        this.isTooBig = isTooBig;
    }
    
	/**
	* Hides href on resource folders based on a configurable limit sakai.content.resourceLimit
	* 
	* @param isTooBigNav
	*/
	public void setIsTooBigNav(boolean isTooBigNav)
    	{
       		this.isTooBigNav = isTooBigNav;
    	}

	public void setMembers(List<ListItem> members) 
	{
		if(this.members == null)
		{
			this.members = new ArrayList<ListItem>();
		}
		this.members.clear();
		this.members.addAll(members);
	}

	/**
	 * @param mimetype the mimetype to set
	 */
	public void setMimetype(String mimetype)
	{
		this.mimetype = mimetype;
	}

	protected void setModifiedBy(String modifiedBy) 
	{
		this.modifiedBy = modifiedBy;
	}

	/**
     * @param modifiedTime the modifiedTime to set
     */
    public void setModifiedTime(String modifiedTime)
    {
    	this.modifiedTime = modifiedTime;
    }

	public void setName(String name) 
	{
		this.name = name;
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
		if(this.otherActions == null)
		{
			this.otherActions = new ArrayList<ResourceToolAction>();
		}
		this.otherActions.clear();
	    if(otherActions != null)
    	{
    		this.otherActions.addAll(otherActions);
    	}
    }

	/**
     * @param otherActionsLabel the otherActionsLabel to set
     */
    public void setOtherActionsLabel(String otherActionsLabel)
    {
    	this.otherActionsLabel = otherActionsLabel;
    }

	protected void setPasteActions(List<ResourceToolAction> pasteActions)
    {
		if(this.pasteActions == null)
		{
			this.pasteActions = new ArrayList<ResourceToolAction>();
		}
		this.pasteActions.clear();
	    if(pasteActions != null)
    	{
    		this.pasteActions.addAll(pasteActions);
    	}
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
     * @param possibleGroups the possibleGroups to set
     */
    public void setPossibleGroups(Collection<Group> possibleGroups)
    {
    	this.possibleGroups.clear();
    	this.possibleGroups.addAll(possibleGroups);
    	// TODO remove site itself?
    }
    
    public void setSiteGroups(Collection<Group> siteGroups)
    {
    	this.siteGroupsMap.clear();
        for(Group group : siteGroups)
        {
        	if(group != null)
        	{
        		this.siteGroupsMap.put(group.getId(), group);
        	}
        }
    }

	/**
     * @param isPubview the isPubview to set
     */
    public void setPubview(boolean isPubview)
    {
        if(isPubview) {
            this.roleIds.add(PUBVIEW_ROLE);
        } else {
            this.roleIds.remove(PUBVIEW_ROLE);
        }
    }

	/**
     * @param isPubviewInherited the isPubviewInherited to set
     */
    public void setPubviewInherited(boolean isPubviewInherited)
    {
        if(isPubviewInherited) {
            this.inheritedRoleIds.add(PUBVIEW_ROLE);
        } else {
            this.inheritedRoleIds.remove(PUBVIEW_ROLE);
        }
    }

	/**
     * @param isPubviewPossible the isPubviewPossible to set
     */
    public void setPubviewPossible(boolean isPubviewPossible)
    {
        this.isPubviewPossible = isPubviewPossible;
    }

	/**
     * @param releaseDate the releaseDate to set
     */
    public void setReleaseDate(Time releaseDate)
    {
    	this.releaseDate = releaseDate;
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

	/**
     * @param retractDate the retractDate to set
     */
    public void setRetractDate(Time retractDate)
    {
    	this.retractDate = retractDate;
    }

	public void setSelected(boolean selected) 
	{
		this.selected = selected;
	}

	public void setSelectedForCopy(boolean selectedForCopy)
    {
	    this.selectedForCopy = selectedForCopy;
    }

	public void setSelectedForMove(boolean selectedForMove)
    {
	    this.selectedForMove = selectedForMove;
    }

	/**
     * @param string
     */
    public void setSize(String size)
    {
        this.size = size;
    }

	/**
     * @param isSortable
     */
    public void setSortable(boolean isSortable)
    {
        this.isSortable  = isSortable;
    }

	/**
     * @param useReleaseDate the useReleaseDate to set
     */
    public void setUseReleaseDate(boolean useReleaseDate)
    {
    	this.useReleaseDate = useReleaseDate;
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
    	return useReleaseDate;
    }

	/**
     * @return the useRetractDate
     */
    public boolean useRetractDate()
    {
    	return useRetractDate;
    }

	public String getCopyrightInfo() 
	{
		return copyrightInfo;
	}

	public void setCopyrightInfo(String copyright) 
	{
		this.copyrightInfo = copyright;
	}

	public boolean hasCopyrightAlert() 
	{
		return copyrightAlert;
	}

	public void setCopyrightAlert(boolean copyrightAlert) 
	{
		this.copyrightAlert = copyrightAlert;
	}

	public void updateContentCollectionEdit(ContentCollectionEdit edit) 
	{
		log.debug("updateContentCollectionEdit()");
		ResourcePropertiesEdit props = edit.getPropertiesEdit();
		setDisplayNameOnEntity(props);
		setDescriptionOnEntity(props);
		//setCopyrightOnEntity(props);
		setConditionalReleaseOnEntity(props);
		setAccessOnEntity(edit);
		setAvailabilityOnEntity(props, edit);
		setQuotaOnEntity(props);
		setHtmlInlineOnEntity(props, edit);
		
		if(isOptionalPropertiesEnabled())
		{
			this.setMetadataPropertiesOnEntity(props);
		}
	}
	
	protected void setQuotaOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.canSetQuota)
		{
			if(SecurityService.isSuperUser())
			{
				if(this.hasQuota)
				{
					if(this.quota != null && this.quota.trim().matches("^\\d+$"))
					{
						props.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, this.quota.trim());
					}
				}
				else
				{
					props.removeProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
				}
			}
		}
	}

	
	private void setHtmlInlineOnEntity(ResourcePropertiesEdit props, ContentCollectionEdit topFolder) 
	{
		log.debug("setHtmlInlineOnEntity() with allowHtmlInline: " + allowHtmlInline);
		if(SecurityService.isSuperUser())
		{
			if(allowHtmlInline != null)
			{
				props.addProperty(ResourceProperties.PROP_ALLOW_INLINE, this.allowHtmlInline.toString());
				
			}
			List<String> children = topFolder.getMembers();
			for (int i = 0; i < children.size(); i++) {
				String resId = children.get(i);
				if (resId.endsWith("/")) {
					setPropertyOnFolderRecursively(resId, ResourceProperties.PROP_ALLOW_INLINE, allowHtmlInline.booleanValue());
				}
			}
		}
	}
	
	private void setHtmlInlineOnEntity(ResourcePropertiesEdit props, ContentResourceEdit topFolder) 
	{
		log.debug("setHtmlInlineOnEntity() with allowHtmlInline: " + allowHtmlInline);
		if(SecurityService.isSuperUser())
		{
			if(allowHtmlInline != null)
			{
				props.addProperty(ResourceProperties.PROP_ALLOW_INLINE, this.allowHtmlInline.toString());
				
			}
			
		}
	}

	
	/**
	 * Set a property on a content hosting item and all its children (recursively).
	 * @param contentId The ID in the of the ContentEntity.
	 * @param property The property name to set.
	 * @param value The value to set the property to.
	 */
	private void setPropertyOnFolderRecursively(String contentId, String property, boolean value) {
		try {
			if (ContentHostingService.isCollection(contentId)) {
				// collection
				ContentCollectionEdit col = ContentHostingService.editCollection(contentId);

				ResourcePropertiesEdit resourceProperties = col.getPropertiesEdit();
				resourceProperties.addProperty(property, String.valueOf(value));
				ContentHostingService.commitCollection(col);

				List<String> children = col.getMembers();
				for (int i = 0; i < children.size(); i++) {
					String resId = children.get(i);
					if (ContentHostingService.isCollection(resId)) {
						setPropertyOnFolderRecursively(resId, property, value);
					}
				}
			} else {
				// resource
				ContentResourceEdit res = ContentHostingService.editResource(contentId);
				ResourcePropertiesEdit resourceProperties = res.getPropertiesEdit();
				resourceProperties.addProperty(property, String.valueOf(value));
				ContentHostingService.commitResource(res, NotificationService.NOTI_NONE);
			}
		} catch (SakaiException se) {
			log.warn(String.format("Failed to set property '%s' on '%s' ", property, contentId), se);
		}
	}
	
	
	protected void setAvailabilityOnEntity(ResourcePropertiesEdit props, GroupAwareEdit edit)
	{
		if ( this.hiddenWithAccessibleContent ) {
			props.addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, PROP_HIDDEN_TRUE);
		} else {
			props.removeProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT);
		}
		edit.setAvailability(hidden, releaseDate, retractDate);
	}
	
	protected void setConditionalReleaseOnEntity(ResourcePropertiesEdit props) 
	{
		props.addProperty(ConditionService.PROP_CONDITIONAL_RELEASE, Boolean.toString(this.useConditionalRelease));
		props.addProperty(ConditionService.PROP_CONDITIONAL_NOTIFICATION_ID, this.notificationId);
		props.removeProperty(ContentHostingService.CONDITIONAL_ACCESS_LIST);
		if (this.accessControlList != null) {
			for (String id : this.accessControlList) {
				props.addPropertyToList(ContentHostingService.CONDITIONAL_ACCESS_LIST, id);
			}
		}
	}


	protected void setCopyrightOnEntity(ResourcePropertiesEdit props) 
	{
		if(copyrightInfo == null || copyrightInfo.trim().length() == 0)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
		}
		else
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT_CHOICE, copyrightInfo);
		}
		if(copyrightStatus == null || copyrightStatus.trim().length() == 0)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT);
		}
		else
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT, copyrightStatus);
		}
		if (copyrightAlert)
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString());
		}
		else
		{
			props.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
		}
		
	}

	protected void setAccessOnEntity(GroupAwareEdit edit) 
	{
		try 
		{
			if(this.accessMode == AccessMode.GROUPED) {
				if (this.groups != null && ! this.groups.isEmpty()) {
					edit.setGroupAccess(groups);
				} else {
					edit.clearGroupAccess();
				}
			} else {
				if (AccessMode.GROUPED == edit.getAccess()) {
					edit.clearGroupAccess();
				}
				setAccessRoles(edit);
			}
		} 
		catch (InconsistentException e) 
		{
			log.warn("InconsistentException " + e);
		} 
		catch (PermissionException e) 
		{
			log.warn("PermissionException " + e);
		}
	}

	/**
	 * Sets the access roles on the entity when saving the ListItem.
	 * @param entityEdit the Edit object of the underlying ListItem that is being saved.
	 * @throws PermissionException if the current user doesn't have permission to add or remove roles.
	 * @throws InconsistentException if the current entity inherits an access mode such as group access.
	 */
	protected void setAccessRoles(GroupAwareEdit entityEdit) throws PermissionException, InconsistentException {

		Set<String> rolesToSave = new LinkedHashSet<String>(roleIds);
		rolesToSave.retainAll(availableRoleIds());

		Set<String> currentRoles = entityEdit.getRoleAccessIds();

		Set<String> rolesToAdd = new LinkedHashSet<String>(rolesToSave);
		rolesToAdd.removeAll(currentRoles);
		rolesToAdd.removeAll(inheritedRoleIds);
		for (String role : rolesToAdd) {
			entityEdit.addRoleAccess(role);
		}

		Set<String> rolesToRemove = new LinkedHashSet<String>(currentRoles);
		rolesToRemove.removeAll(rolesToSave);
		rolesToRemove.removeAll(inheritedRoleIds);
		for (String role : rolesToRemove) {
			entityEdit.removeRoleAccess(role);
		}
	}

	protected void setDescriptionOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.description != null)
		{
			props.addProperty(ResourceProperties.PROP_DESCRIPTION, this.description);
		}
	}

	protected void setCHHMountpoint(ResourcePropertiesEdit props) 
	{
		if(this.chhmountpoint != null)
		{
			props.addProperty(ContentHostingHandlerResolver.CHH_BEAN_NAME, this.chhmountpoint);
		}
	}

	public void updateContentResourceEdit(ContentResourceEdit edit) 
	{
		ResourcePropertiesEdit props = edit.getPropertiesEdit();
		setCHHMountpoint(props);
		setDisplayNameOnEntity(props);
		setDescriptionOnEntity(props);
		setConditionalReleaseOnEntity(props);
		setCopyrightOnEntity(props);
		setHtmlFilterOnEntity(props);
		setAccessOnEntity(edit);
		setAvailabilityOnEntity(props, edit);
		setHtmlInlineOnEntity(props, edit);
		
		if(! isUrl() && ! isCollection() && this.mimetype != null)
		{
			setMimetypeOnEntity(edit, props);
		}
		if(isOptionalPropertiesEnabled())
		{
			this.setMetadataPropertiesOnEntity(props);
		}
	}

	protected void setHtmlFilterOnEntity(ResourcePropertiesEdit props) {
		if (isHtml())
		{
			props.addProperty(ResourceProperties.PROP_ADD_HTML, this.htmlFilter);
		}
		else
		{
			props.removeProperty(ResourceProperties.PROP_ADD_HTML);
		}
	}

	
	protected void setMimetypeOnEntity(ContentResourceEdit edit, ResourcePropertiesEdit props) 
	{
		if(this.mimetype != null)
		{
			props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, this.mimetype);
			edit.setContentType(this.mimetype);
		}
	}

	protected void setDisplayNameOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.name != null)
		{
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, this.name);
		}
	}

	public String getCopyrightStatus() 
	{
		return copyrightStatus;
	}

	public void setCopyrightStatus(String copyrightStatus) 
	{
		this.copyrightStatus = copyrightStatus;
	}

	public boolean isUserSite() 
	{
		return isUserSite;
	}

	public boolean canSetQuota() 
	{
		return canSetQuota;
	}
	
	
	
	public boolean isAdmin() {
		return isAdmin;
	}
	
	public void setIsAdmin(boolean admin) {
		isAdmin = admin;
	}
	
	
	public boolean hasQuota() 
	{
		return hasQuota;
	}

	public String getQuota() 
	{
		return quota;
	}

	public boolean isSiteCollection() 
	{
		return isSiteCollection;
	}

	public void setSiteCollection(boolean isSiteCollection) 
	{
		this.isSiteCollection = isSiteCollection;
	}

	public String getResourceType() 
	{
		return resourceType;
	}

	public void setResourceType(String resourceType) 
	{
		this.resourceType = resourceType;
	}

	public String getCreatedTime() 
	{
		return createdTime;
	}

	public String getModifiedBy() 
	{
		if (modifiedBy == null)
		{
			modifiedBy = "";
		}
		return modifiedBy;
	}

	 public String getMimeCategory()
	 {
		 if(this.mimetype == null || "".equals(this.mimetype))
		 {
			 return "";
		 }
		 int index = this.mimetype.indexOf("/");
		 if(index < 0)
		 {
			 return this.mimetype;
		 }
		 return this.mimetype.substring(0, index);
	 }

	 public String getMimeSubtype()
	 {
		 if(this.mimetype == null || "".equals(this.mimetype))
		 {
			 return "";
		 }
		 int index = this.mimetype.indexOf("/");
		 if(index < 0 || index + 1 == this.mimetype.length())
		 {
			 return "";
		 }
		 return this.mimetype.substring(index + 1);
	 }

	public String getSizzle() 
	{
		return sizzle;
	}

	public boolean isDropbox() 
	{
		return isDropbox;
	}

	public void setDropbox(boolean isDropbox) 
	{
		this.isDropbox = isDropbox;
	}

	public List<ResourceToolAction> getPasteActions()
    {
    	return pasteActions;
    }

	public AccessMode getInheritedAccessMode()
    {
    	return inheritedAccessMode;
    }

	public void setInheritedAccessMode(AccessMode inheritedAccessMode)
    {
    	this.inheritedAccessMode = inheritedAccessMode;
    }

	protected boolean siteCollection(String refStr)
	{
		boolean site = false;
		
		if( m_reference == null )
		{
			m_reference = EntityManager.newReference(refStr);
		}
		String context = m_reference.getContext();
		// what happens if context is null??
		if (context!=null){
			String siteCollection = ContentHostingService.getSiteCollection(context);
			if(m_reference.getId().equals(siteCollection))
			{
				site = true;
			}
		}
		return site;
	}
	
	/**
	 * return site id based on given reference String
	 * @param refStr
	 * @return
	 */
	protected String getSiteContext(String refStr)
	{
		String rv = null;
		
		if( m_reference == null )
		{
			m_reference = EntityManager.newReference(refStr);
		}
		rv = m_reference.getContext();
		
		return rv;
	}

	public ListItem getParent()
    {
		checkParent();
	    return this.parent;
    }

	public void setNameIsMissing(boolean nameIsMissing)
    {
	    this.nameIsMissing = nameIsMissing;
    }

	public boolean isNameMissing()
    {
    	return nameIsMissing;
    }

	public boolean isSelectedForCopy()
    {
    	return selectedForCopy;
    }

	public boolean isSelectedForMove()
    {
    	return selectedForMove;
    }

	public List<String> checkRequiredProperties()
    {
		List<String> alerts = new ArrayList<String>();
		String name = getName();
		if(name == null || name.trim().equals(""))
		{
			setNameIsMissing(true);
			alerts.add(rb.getString("edit.missing"));
		}
		
		Time release = releaseDate;
		Time retract = retractDate;
		if (retract != null && retract.before(release))
		{
			alerts.add(rb.getString("edit.retractBeforeRelease"));
		}
		
		
	    return alerts;
    }

	/**
     * @return the expandable
     */
    public boolean isExpandable()
    {
    	return expandable;
    }

	/**
     * @param expandable the expandable to set
     */
    public void setExpandable(boolean expandable)
    {
    	this.expandable = expandable;
    }

	/**
     * @return the expandLabel
     */
    public String getExpandLabel()
    {
    	return expandLabel;
    }

	/**
     * @param expandLabel the expandLabel to set
     */
    public void setExpandLabel(String expandLabel)
    {
    	this.expandLabel = expandLabel;
    }

	/**
     * @return the expandIconLocation
     */
    public String getExpandIconLocation()
    {
    	return expandIconLocation;
    }

	/**
     * @param expandIconLocation the expandIconLocation to set
     */
    public void setExpandIconLocation(String expandIconLocation)
    {
    	this.expandIconLocation = expandIconLocation;
    }

	public void setNotification(int noti)
    {
	    this.notification = noti;
    }

	/**
     * @return the notification
     */
    public int getNotification()
    {
    	return notification;
    }
    
	/**
	 * initialize the metadata context
	 */
    public void initMetadataGroups()
	{
		//TODO get only metadata related to the current entity type
		metadataGroups = metadataService.getMetadataAvailable(toolManager.getCurrentPlacement().getContext(), "");
		metadataValues = new HashMap<String, Object>(metadataGroups.size());
		if (this.entity != null)
		{
			for (MetadataType metadataGroup : metadataGroups)
			{
				Object metadataValue = metadataGroup.getConverter().fromProperties(wrapResourcePropertiesInMap(this.entity.getProperties()));
				metadataValues.put(metadataGroup.getUniqueName(), metadataValue);
			}
		} else
		{
			for (MetadataType metadataGroup : metadataGroups)
			{
				metadataValues.put(metadataGroup.getUniqueName(), metadataGroup.getDefaultValue());
			}
		}
	}

	/**
	 * Provides a huge wrapper around ResourceProperties to use it as a Map
	 *
	 * @param resourceProperties ResourceProperties to wrap
	 * @return the wrapped value
	 */
	private Map<String, Object> wrapResourcePropertiesInMap(final ResourceProperties resourceProperties)
	{
		return new AbstractMap<String, Object>()
		{
			public boolean isEmpty()
			{
				return !resourceProperties.getPropertyNames().hasNext();
			}

			public boolean containsKey(Object key)
			{
				return resourceProperties.get((String) key) != null;
			}

			public Object get(Object key)
			{
				return resourceProperties.get((String) key);
			}

			@Override
			public Set<Entry<String, Object>> entrySet()
			{
				return new AbstractSet<Entry<String, Object>>()
				{
					@Override
					public Iterator<Entry<String, Object>> iterator()
					{
						return new Iterator<Entry<String, Object>>()
						{
							private final Iterator<String> propertiesNames = resourceProperties.getPropertyNames();

							public boolean hasNext()
							{
								return propertiesNames.hasNext();
							}

							public Entry<String, Object> next()
							{
								return new Entry<String, Object>()
								{
									private final String key = propertiesNames.next();

									public String getKey()
									{
										return key;
									}

									public Object getValue()
									{
										return resourceProperties.get(key);
									}

									public Object setValue(Object value)
									{
										throw new UnsupportedOperationException();
									}
								};
							}

							public void remove()
							{
								throw new UnsupportedOperationException();
							}
						};
					}

					@Override
					public int size()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	protected void captureOptionalPropertyValues(ParameterParser params, String index)
	{
		metadataValues = new HashMap<String, Object>(metadataGroups.size());
		StringBuilder metadataValidation = new StringBuilder();
		metadataValidationFails = "";
		for (MetadataType metadataGroup : metadataGroups)
		{
			Object metadataValue = metadataGroup.getConverter().fromHttpForm(wrapParametersInMap(params), index);
			if(metadataGroup.getValidator() != null && !metadataGroup.getValidator().validate(metadataValue)) {
				if(metadataValidation.length() > 0)
					metadataValidation.append(",");
				metadataValidation.append(metadataGroup.getName());
			}
			metadataValidationFails = metadataValidation.toString();
			
			metadataValues.put(metadataGroup.getUniqueName(), metadataValue);
		}
	}

	/**
	 * Provides a huge wrapper around parameterParser to use it as a Map
	 *
	 * @param params ParameterParser to wrap
	 * @return the wrapped value
	 */
	private Map<String, ?> wrapParametersInMap(final ParameterParser params)
	{
		return new AbstractMap<String, Object>()
		{
			public boolean isEmpty()
			{
				return !params.getNames().hasNext();
			}

			public boolean containsKey(Object key)
			{
				return params.get((String) key) != null;
			}

			public Object get(Object key)
			{
				String[] value = params.getStrings((String) key);
				if (value == null || value.length == 0)
					return null;
				else if (value.length > 1)
				{
					return value;
				} else
					return value[0];
			}

			@Override
			public Set<Entry<String, Object>> entrySet()
			{
				return new AbstractSet<Entry<String, Object>>()
				{
					@Override
					public Iterator<Entry<String, Object>> iterator()
					{
						return new Iterator<Entry<String, Object>>()
						{
							private final Iterator<String> parametersNames = params.getNames();

							public boolean hasNext()
							{
								return parametersNames.hasNext();
							}

							public Entry<String, Object> next()
							{
								return new Entry<String, Object>()
								{
									private final String key = parametersNames.next();

									public String getKey()
									{
										return key;
									}

									public Object getValue()
									{
										String[] value = params.getStrings(key);
										if (value == null || value.length == 0)
											return null;
										else if (value.length > 1)
										{
											return value;
										} else
											return value[0];
									}

									public Object setValue(Object value)
									{
										throw new UnsupportedOperationException();
									}
								};
							}

							public void remove()
							{
								throw new UnsupportedOperationException();
							}
						};
					}

					@Override
					public int size()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Add the metadata content in the entity properties
	 * <p/>
	 * Converts and validate the user given values before adding then to the entity
	 * INFO: For the suppress warning, see the content of the method
	 *
	 * @param props Entity properties
	 */
	@SuppressWarnings("unchecked")
	protected void setMetadataPropertiesOnEntity(ResourcePropertiesEdit props)
	{
		if (this.metadataValues == null)
		{
			return;
		}

		for (MetadataType metadataType : this.metadataGroups)
		{
			/*
			 * There is no way to be sure of the metadata type of the entry, so a "cast" is required.
			 * In this case we can't cast to "?" so here goes some unchecked operations.
			 */
			metadataType.getValidator().validate(metadataValues.get(metadataType.getUniqueName()));
			Map<String, ?> values =  metadataType.getConverter().toProperties(metadataValues.get(metadataType.getUniqueName()));
			for(Map.Entry<String, ?> entry : values.entrySet()) {
				if (entry.getValue() == null) {
					props.removeProperty(entry.getKey());
				} else if (entry.getValue() instanceof String) {
					// Handle string values.
					props.addProperty(entry.getKey(), (String) entry.getValue());
				} else if (entry.getValue() instanceof Collection) {
					// Handle collection values.
					if (((Collection<String>) entry.getValue()).isEmpty()) {
						props.removeProperty(entry.getKey());
					} else {
						for (String value : (Collection<String>) entry.getValue()) {
							props.addPropertyToList(entry.getKey(), value);
						}
					}
				} else {
					// Warn about other types.
					log.warn("Unable to save metadata with key: "+ entry.getKey()+ " value: "+ entry.getValue());
				}
			}
		}
	}

	public static void setOptionalPropertiesEnabled(boolean b)
    {
	    optionalPropertiesEnabled = b;
    }

	/**
     * @return the optionalPropertiesEnabled
     */
    public static boolean isOptionalPropertiesEnabled()
    {
    	return optionalPropertiesEnabled;
    }

	public List<MetadataType> getMetadataGroups()
	{
		return metadataGroups;
	}

	public Map<String, Object> getMetadataValues()
	{
		return metadataValues;
	}

	private boolean typeSupportsOptionalProperties()
    {
		boolean typeSupportsOptionalProperties = true;
		ResourceType typeDef = this.getResourceTypeDef();
		if(typeDef != null)
		{
			typeSupportsOptionalProperties = typeDef.hasOptionalPropertiesDialog();
		}
	    return typeSupportsOptionalProperties;
    }

	/**
	 * @return the isHot
	 */
	public boolean isHot(String dropboxHighlight) 
	{
		boolean hot = false;
		try
		{
			if(dropboxHighlight != null && ! dropboxHighlight.trim().equals("") && this.lastChange != null)
			{
				long days = Long.parseLong(dropboxHighlight);
				long minTime = TimeService.newTime().getTime() - days * ONE_DAY;
				hot = this.lastChange.getTime() > minTime;
			}
		}
		catch(Exception e)
		{
			hot = false;
		}
		
		return hot;
	}

	/**
	 * @param isHot the isHot to set
	 */
	public void setHot(boolean isHot) {
		this.isHot = isHot;
	}

	private String getIndividualDropboxId(String id) 
	{
		String rv = null;
		if(id != null)
		{
			String parts[] = id.split("/");
			if(parts.length >= 4)
			{
				rv = "/" + parts[1] + "/" + parts[2] + "/" + parts[3] + "/";
			}
		}
		return rv;
	}

	/**
	 * Determine whether the user is a Dropbox maintainer for the root-level dropbox (provided the current item is
	 * an individual dropbox or an item inside an individual dropbox). 
	 * @return true if the user is a site-level maintainer for the dropbox (provided the current item is
	 * an individual dropbox or an item inside an individual dropbox), and false otherwise.
	 */
	public boolean userIsMaintainer()
	{
		boolean userIsMaintainer = false;
		if(this.isDropbox)
		{
			String dropboxId = null;
			// When modifying an ListItem's properties the id contains the full path.
			if(id != null && !id.trim().equals(""))
			{
				dropboxId = getIndividualDropboxId(id);
			}
			// When uploading a new item the ListItem's id just contains the filename and the containingCollectionId
			// contains the dropbox into which it is going.
			if(dropboxId == null && containingCollectionId != null && ! containingCollectionId.trim().equals(""))
			{
				dropboxId = getIndividualDropboxId(containingCollectionId);
			}
			else if(parent != null)
			{
				dropboxId = getIndividualDropboxId(parent.getId());
			}
			if(dropboxId != null)
			{
				User currentUser = UserDirectoryService.getCurrentUser();
				String userEid = currentUser.getEid();
				String userId = currentUser.getId();
				userIsMaintainer = ! ((userEid == null || dropboxId.contains(userEid)) || (userId == null || dropboxId.contains(userId)));
			}
		}
		return userIsMaintainer;
	}

	/**
	 * @return the dropboxHighlight
	 */
	public long getDropboxHighlight() {
		return dropboxHighlight;
	}

	/**
	 * @param dropboxHighlight the dropboxHighlight to set
	 */
	public void setDropboxHighlight(long dropboxHighlight) {
		this.dropboxHighlight = dropboxHighlight;
	}

	/**
	 * @return the lastChange
	 */
	public Time getLastChange() {
		return lastChange;
	}

	/**
	 * @param lastChange the lastChange to set
	 */
	public void setLastChange(Time lastChange) {
		this.lastChange = lastChange;
	}

	/**
	 * @return the isCourseSite
	 */
	public boolean isCourseSite() 
	{
		return isCourseSite;
	}

	/**
	 * @param isCourseSite the isCourseSite to set
	 */
	public void setCourseSite(boolean isCourseSite) 
	{
		this.isCourseSite = isCourseSite;
	}
	
	/**
	 * This code was refactored out of {@link ListItem#set(ContentEntity)}with the idea that it would end up
	 * in a ResourceType class and that the resource type registry would handle the builtin types as well 
	 * (ContentCollection & ContentResource) rather than handling some stuff in the registry and some in the tool.
	 * @see #getSizeLabel(ContentEntity)
	 */
	protected String getLongSizeLabel(ContentEntity entity) {
		String sizzle = "";
		ResourceProperties props = entity.getProperties();
		try
		{
			long size_long = props.getLongProperty(ResourceProperties.PROP_CONTENT_LENGTH);
			sizzle = formatLongSize(size_long);
		}
		catch (EntityPropertyNotDefinedException e)
		{
			log.info("EntityPropertyNotDefinedException for size of " + entity.getId());
		}
		catch(EntityPropertyTypeException e)
		{
			log.info("EntityPropertyTypeException not long of " + entity.getId());
		}
		return sizzle;
	}

	/**
	 * Utility method to get a verbose filesize string.
	 * @param size_long The size to be displayed (bytes).
	 * @return A long human readable filesize.
	 */
	public static String formatLongSize(long size_long) {
		// This method needs to be moved somewhere more sensible.
		String sizzle = "";
		NumberFormat formatter = NumberFormat.getInstance(rb.getLocale());
		formatter.setMaximumFractionDigits(1);
		if(size_long > 700000000L)
		{
			String[] argyles = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)), formatter.format(size_long) };
			sizzle = rb.getFormattedMessage("size.gbytes", argyles);
		}
		else if(size_long > 700000L)
		{
			String[] argyles = { formatter.format(1.0 * size_long / (1024L * 1024L)), formatter.format(size_long) };
			sizzle = rb.getFormattedMessage("size.mbytes", argyles);
		}
		else if(size_long > 700L)
		{
			String[] argyles = { formatter.format(1.0 * size_long / 1024L), formatter.format(size_long) };
			sizzle = rb.getFormattedMessage("size.kbytes", argyles);
		}
		else 
		{
			String[] args = { formatter.format(size_long) };
			sizzle = rb.getFormattedMessage("size.bytes", args);
		}
		return sizzle;
	}


	/**
	 * @see #getLongSizeLabel(ContentEntity)
	 */
	protected String getSizeLabel(ContentEntity entity) {
		String size = "";
		ResourceProperties props = entity.getProperties();
		long size_long = 0;
		try
		{
			size_long = props.getLongProperty(ResourceProperties.PROP_CONTENT_LENGTH);
			size = formatSize(size_long);
		}
		catch (EntityPropertyNotDefinedException e)
		{
			log.warn("EntityPropertyNotDefinedException for size of " + entity.getId());
		}
		catch (EntityPropertyTypeException e)
		{
			size = props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH);
		}
		return size;
	}

	/**
	 * Utility method to get a nice short filesize string.
	 * @param size_long The size to be displayed (bytes).
	 * @return A short human readable filesize.
	 */
	public static String formatSize(long size_long) {
		// This method needs to be moved somewhere more sensible.
		String size = "";
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
		{		String[] args = { formatter.format(1.0 * size_long / 1024L) };
		size = rb.getFormattedMessage("size.kb", args);
		}
		else 
		{
			String[] args = { formatter.format(size_long) };
			size = rb.getFormattedMessage("size.bytes", args);
		}
		return size;
	}

	public String getHtmlFilter() {
		return htmlFilter;
	}
	
	public boolean isAllowHtmlInline() {
		return allowHtmlInline;
	}

	public void setAllowHtmlInline(boolean allowHtmlInline) {
		this.allowHtmlInline = allowHtmlInline;
	}
	
	/**
	 * Specifies whether or not the item has inherited the "allowHtmlInline" property from its
	 * parent collection.
	 * 
	 * @return the allowHtmlInlineInherited
	 */
	public Boolean isAllowHtmlInlineInherited() {
		return allowHtmlInlineInherited;
	}

	/**
	 * @param allowHtmlInlineInherited the allowHtmlInlineInherited to set
	 * @see #getAllowHtmlInlineInherited()
	 */
	public void setAllowHtmlInlineInherited(Boolean allowHtmlInlineInherited) {
		this.allowHtmlInlineInherited = allowHtmlInlineInherited;
	}

	/**
	 * Get dropbox owner
	 * @param id
	 * @return
	 */
	public String getDropboxOwner() 
	{
		String rv = null;
		if(id != null && isDropbox)
		{
			String parts[] = id.split("/");
			if(parts.length >= 4)
			{
			rv = parts[3];
			}
		}
		return rv;
	}
	
	public String getServiceName()
	{
		// This is used when asking if the styles of the service should be used.
		return ServerConfigurationService.getString("ui.service", "Sakai");
	}
	
}

