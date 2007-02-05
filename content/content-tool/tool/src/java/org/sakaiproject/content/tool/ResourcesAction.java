/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceHelperAction;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentResourceFilter;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.InteractionAction;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.ResourceToolAction.ActionType;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Term;
import org.sakaiproject.site.cover.CourseManagementService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Element;

/**
* <p>ResourceAction is a ContentHosting application</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class ResourcesAction 
	extends PagedResourceHelperAction // VelocityPortletPaneledAction
{
	public static final List<ActionType> CONTENT_NEW_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> PASTE_COPIED_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> PASTE_MOVED_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> CONTENT_NEW_FOR_PARENT_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> CONTENT_READ_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> CONTENT_MODIFY_ACTIONS = new Vector<ActionType>();
	public static final List<ActionType> CONTENT_DELETE_ACTIONS = new Vector<ActionType>();
	
	// may need to distinguish permission on entity vs permission on its containing collection
	static
	{
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_UPLOAD);
		CONTENT_NEW_ACTIONS.add(ActionType.NEW_FOLDER);
		CONTENT_NEW_ACTIONS.add(ActionType.CREATE);
		
		PASTE_COPIED_ACTIONS.add(ActionType.PASTE_COPIED);
		PASTE_MOVED_ACTIONS.add(ActionType.PASTE_MOVED);
		
		CONTENT_NEW_FOR_PARENT_ACTIONS.add(ActionType.DUPLICATE);
		
		CONTENT_READ_ACTIONS.add(ActionType.VIEW_CONTENT);
		CONTENT_READ_ACTIONS.add(ActionType.VIEW_METADATA);
		CONTENT_READ_ACTIONS.add(ActionType.COPY);
		
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_METADATA);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REVISE_CONTENT);
		CONTENT_MODIFY_ACTIONS.add(ActionType.REPLACE_CONTENT);
		
		CONTENT_DELETE_ACTIONS.add(ActionType.MOVE);
		CONTENT_DELETE_ACTIONS.add(ActionType.DELETE);
	}
	
	public static final List<ActionType> ACTIONS_ON_FOLDERS = new Vector<ActionType>();
	
	static
	{
		ACTIONS_ON_FOLDERS.add(ActionType.VIEW_METADATA);
		ACTIONS_ON_FOLDERS.add(ActionType.REVISE_METADATA);
		ACTIONS_ON_FOLDERS.add(ActionType.DUPLICATE);
		ACTIONS_ON_FOLDERS.add(ActionType.COPY);
		ACTIONS_ON_FOLDERS.add(ActionType.MOVE);
		ACTIONS_ON_FOLDERS.add(ActionType.DELETE);
		// ACTIONS_ON_FOLDERS.add(ActionType.PASTE_MOVED);
	}
	
	public static final List<ActionType> ACTIONS_ON_RESOURCES = new Vector<ActionType>();
	
	static
	{
		ACTIONS_ON_RESOURCES.add(ActionType.VIEW_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.VIEW_METADATA);
		ACTIONS_ON_RESOURCES.add(ActionType.REVISE_METADATA);
		ACTIONS_ON_RESOURCES.add(ActionType.REVISE_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.REPLACE_CONTENT);
		ACTIONS_ON_RESOURCES.add(ActionType.DUPLICATE);
		ACTIONS_ON_RESOURCES.add(ActionType.COPY);
		ACTIONS_ON_RESOURCES.add(ActionType.MOVE);
		ACTIONS_ON_RESOURCES.add(ActionType.DELETE);
	}
	
	public static final List<ActionType> CREATION_ACTIONS = new Vector<ActionType>();
	
	static 
	{
		CREATION_ACTIONS.add(ActionType.NEW_UPLOAD);
		CREATION_ACTIONS.add(ActionType.NEW_FOLDER);
		CREATION_ACTIONS.add(ActionType.CREATE);
		CREATION_ACTIONS.add(ActionType.PASTE_MOVED);
	}
	
	public class Labeler
	{
		public String getLabel(ResourceToolAction action)
		{
			String label = action.getLabel();
			if(label == null)
			{
				switch(action.getActionType())
				{
				case NEW_UPLOAD:
					label = trb.getString("create.uploads");
					break;
				case NEW_FOLDER:
					label = trb.getString("create.folder");
					break;
				case CREATE:
					ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
					ResourceType typedef = registry.getType(action.getTypeId());
					String[] args = { typedef.getLabel() };
					label = trb.getFormattedMessage("create.unknown", args);
					break;
				case COPY:
					label = trb.getString("action.copy");
					break;
				case DUPLICATE:
					label = trb.getString("action.duplicate");
					break;
				case DELETE:
					label = trb.getString("action.delete");
					break;
				case MOVE:
					label = trb.getString("action.move");
					break;
				case VIEW_METADATA:
					label = trb.getString("action.info");
					break;
				case REVISE_METADATA:
					label = trb.getString("action.props");
					break;
				case VIEW_CONTENT:
					label = trb.getString("action.access");
					break;
				case REVISE_CONTENT:
					label = trb.getString("action.revise");
					break;
				case REPLACE_CONTENT:
					label = trb.getString("action.replace");
					break;
				default:
					logger.info("No label provided for ResourceToolAction: " + action.getTypeId() + ResourceToolAction.ACTION_DELIMITER + action.getId());
					label = action.getId();
					break;
				}
			}
			return label;
		}
	}
	
	public class ListItem
	{
		protected String name;
		protected String id;
		protected List actions;
		protected List members;
		protected Set permissions;
		protected boolean selected;
		protected boolean collection;
		protected String hoverText;
		protected String accessUrl;
		protected String iconLocation;
		protected String mimetype;
		private String resourceType;
		
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
			// this.permissions
			this.selected = false;
			
			ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			this.resourceType = entity.getResourceType();
			ResourceType typeDef = registry.getType(resourceType);
			this.hoverText = this.name;
			if(typeDef != null)
			{
				this.hoverText = typeDef.getLocalizedHoverText(entity);
			}

			if(this.collection)
			{
				ContentCollection coll = (ContentCollection) entity;
				this.members = coll.getMembers();
				this.iconLocation = ContentTypeImageService.getContentTypeImage("folder");
			}
			else 
			{
				ContentResource resource = (ContentResource) entity;
				this.mimetype = resource.getContentType();
				if(this.mimetype == null)
				{
					
				}
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
				
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
		public Set getPermissions()
		{
			return this.permissions;
		}
		
		/**
		 * @param permissions the permissions to set
		 */
		public void setPermissions(Set permissions)
		{
			this.permissions = permissions;
		}
		
		/**
		 * @param permission
		 */
		public void addPermission(String permission)
		{
			if(this.permissions == null)
			{
				this.permissions = new TreeSet();
			}
			this.permissions.add(permission);
		}
		
		/**
		 * @param permission
		 * @return
		 */
		public boolean isPermitted(String permission)
		{
			if(this.permissions == null)
			{
				this.permissions = new TreeSet();
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
		
		public List getMembers() 
		{
			return members;
		}
		
		public void setMembers(List members) 
		{
			this.members = members;
		}
		
		public void setSelected(boolean selected) 
		{
			this.selected = selected;
		}
		
		public boolean isSelected() 
		{
			return selected;
		}
		
		public List getActions()
		{
			return actions;
		}
		
		public void setActions(List actions)
		{
			this.actions = actions;
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
	}
	
	/**
	 * Action
	 *
	 */
	public class Action
	{
		protected String label;
		protected String actionId;
		
		/**
		 * @return the actionId
		 */
		public String getActionId()
		{
			return this.actionId;
		}
		
		/**
		 * @param actionId the actionId to set
		 */
		
		public void setActionId(String actionId)
		{
			this.actionId = actionId;
		}
		
		/**
		 * @return the label
		 */
		public String getLabel()
		{
			return this.label;
		}
		
		/**
		 * @param label the label to set
		 */
		public void setLabel(String label)
		{
			this.label = label;
		}
		
	}
	
	public class EditItem
	{
		protected byte[] content;
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
		protected String resourceType;
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
		
		/**
		 * @param entityId
		 * @param collectionId
		 * @param propertyValues
		 * @param resourceType
		 */
		public EditItem(String entityId, String collectionId, String resourceType, Map propertyValues)
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
		
		public EditItem(ContentEntity entity)
		{
			ResourceProperties props = entity.getProperties();
			this.accessMode = entity.getAccess();
			//this.canSetQuota = 
			//this.collection = 
			this.collectionId = entity.getContainingCollection().getId();
			this.createdBy = props.getProperty(ResourceProperties.PROP_CREATOR);
			this.modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
			try
			{
				this.createdTime = props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				this.modifiedTime = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
			}
			catch (EntityPropertyNotDefinedException e1)
			{
				// TODO Auto-generated catch block
				logger.warn("EntityPropertyNotDefinedException ", e1);
			}
			catch (EntityPropertyTypeException e1)
			{
				// TODO Auto-generated catch block
				logger.warn("EntityPropertyTypeException ", e1);
			}
			this.displayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			this.description = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
			this.entityId = entity.getId();
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
					logger.warn("ServerOverloadException ", e);
				}
				// this.uuid = resource.
			}
		}
		
		public EditItem(String entityId, String collectionId, String resourceType, ResourceToolActionPipe pipe)
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
			// TODO: update the EditItem based on the pipe
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

	}
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");

	/** Resource bundle using current language locale */
    private static ResourceLoader trb = new ResourceLoader("types");

    private static final Log logger = LogFactory.getLog(ResourcesAction.class);

	/** Name of state attribute containing a list of opened/expanded collections */
	private static final String STATE_EXPANDED_COLLECTIONS = "resources.expanded_collections";

	/** Name of state attribute for status of initialization.  */
	private static final String STATE_INITIALIZED = "resources.initialized";

	/** The content hosting service in the State. */
	private static final String STATE_CONTENT_SERVICE = "resources.content_service";

	/** The content type image lookup service in the State. */
	private static final String STATE_CONTENT_TYPE_IMAGE_SERVICE = "resources.content_type_image_service";
	
	private static final String STATE_RESOURCES_TYPE_REGISTRY = "resources.type_registry";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_MODE_RESOURCES = "resources.resources_mode";

	/** The resources, helper or dropbox mode. */
	public static final String STATE_RESOURCES_HELPER_MODE = "resources.resources_helper_mode";

	/** state attribute for the maximum size for file upload */
	static final String STATE_FILE_UPLOAD_MAX_SIZE = "resources.file_upload_max_size";
	
	/** state attribute indicating whether users in current site should be denied option of making resources public */
	private static final String STATE_PREVENT_PUBLIC_DISPLAY = "resources.prevent_public_display";

	/** The name of a state attribute indicating whether the resources tool/helper is allowed to show all sites the user has access to */
	public static final String STATE_SHOW_ALL_SITES = "resources.allow_user_to_see_all_sites";

	/** The name of a state attribute indicating whether the wants to see other sites if that is enabled */
	public static final String STATE_SHOW_OTHER_SITES = "resources.user_chooses_to_see_other_sites";

	/** The user copyright string */
	private static final String	STATE_MY_COPYRIGHT = "resources.mycopyright";

	/** copyright path -- MUST have same value as AccessServlet.COPYRIGHT_PATH */
	public static final String COPYRIGHT_PATH = Entity.SEPARATOR + "copyright";

	/** The collection id being browsed. */
	private static final String STATE_COLLECTION_ID = "resources.collection_id";

	/** The id of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_ID = "resources.collection_home";

	/** The display name of the "home" collection (can't go up from here.) */
	private static final String STATE_HOME_COLLECTION_DISPLAY_NAME = "resources.collection_home_display_name";
	
	/** name of state attribute for the default retract time */
	protected static final String STATE_DEFAULT_RETRACT_TIME = "resources.default_retract_time";

	/** The collection id path */
	private static final String STATE_COLLECTION_PATH = "resources.collection_path";

	/** The name of the state attribute containing BrowseItems for all content collections the user has access to */
	private static final String STATE_COLLECTION_ROOTS = "resources.collection_rootie_tooties";

	/** The sort by */
	private static final String STATE_SORT_BY = "resources.sort_by";

	/** The sort ascending or decending */
	private static final String STATE_SORT_ASC = "resources.sort_asc";

	/** The copy flag */
	private static final String STATE_COPY_FLAG = "resources.copy_flag";

	/** The cut flag */
	private static final String STATE_CUT_FLAG = "resources.cut_flag";

	/** The can-paste flag */
	private static final String STATE_PASTE_ALLOWED_FLAG = "resources.can_paste_flag";

	/** The move flag */
	private static final String STATE_MOVE_FLAG = "resources.move_flag";

	/** The select all flag */
	private static final String STATE_SELECT_ALL_FLAG = "resources.select_all_flag";

	/** The name of the state attribute indicating whether the hierarchical list is expanded */
	private static final String STATE_EXPAND_ALL_FLAG = "resources.expand_all_flag";

	/** The name of the state attribute indicating whether the hierarchical list needs to be expanded */
	private static final String STATE_NEED_TO_EXPAND_ALL = "resources.need_to_expand_all";

	/** The name of the state attribute containing a java.util.Set with the id's of selected items */
	private static final String STATE_LIST_SELECTIONS = "resources.ignore_delete_selections";

	/** The root of the navigation breadcrumbs for a folder, either the home or another site the user belongs to */
	private static final String STATE_NAVIGATION_ROOT = "resources.navigation_root";

	/************** the more context *****************************************/

	/** The more id */
	private static final String STATE_MORE_ID = "resources.more_id";

	/** The more collection id */
	private static final String STATE_MORE_COLLECTION_ID = "resources.more_collection_id";

	/************** the edit context *****************************************/

	/** The edit id */
	public static final String STATE_EDIT_ID = "resources.edit_id";
	public static final String STATE_STACK_EDIT_ID = "resources.stack_edit_id";
	public static final String STATE_EDIT_COLLECTION_ID = "resources.stack_edit_collection_id";
	public static final String STATE_STACK_EDIT_COLLECTION_ID = "resources.stack_edit_collection_id";

	private static final String STATE_EDIT_ALERTS = "resources.edit_alerts";
	private static final String STATE_STACK_EDIT_ITEM = "resources.stack_edit_item";
	private static final String STATE_STACK_EDIT_INTENT = "resources.stack_edit_intent";

	private static final String STATE_STACK_EDIT_ITEM_TITLE = "resources.stack_title";

	/************** the create contexts *****************************************/

	public static final String STATE_SUSPENDED_OPERATIONS_STACK = "resources.suspended_operations_stack";
	public static final String STATE_SUSPENDED_OPERATIONS_STACK_DEPTH = "resources.suspended_operations_stack_depth";

	public static final String STATE_CREATE_TYPE = "resources.create_type";
	public static final String STATE_CREATE_COLLECTION_ID = "resources.create_collection_id";
	public static final String STATE_CREATE_NUMBER = "resources.create_number";

	public static final String STATE_STACK_CREATE_TYPE = "resources.stack_create_type";
	public static final String STATE_STACK_CREATE_COLLECTION_ID = "resources.stack_create_collection_id";
	public static final String STATE_STACK_CREATE_NUMBER = "resources.stack_create_number";

	private static final String STATE_STACK_CREATE_ITEMS = "resources.stack_create_items";
	private static final String STATE_STACK_CREATE_ACTUAL_COUNT = "resources.stack_create_actual_count";

	private static final String STATE_CREATE_ALERTS = "resources.create_alerts";
	protected static final String STATE_CREATE_MESSAGE = "resources.create_message";
	private static final String STATE_CREATE_MISSING_ITEM = "resources.create_missing_item";

	private static final String MIME_TYPE_DOCUMENT_PLAINTEXT = "text/plain";
	private static final String MIME_TYPE_DOCUMENT_HTML = "text/html";
	public static final String MIME_TYPE_STRUCTOBJ = "application/x-osp";

	public static final String TYPE_FOLDER = "folder";
	public static final String TYPE_UPLOAD = "file";
	public static final String TYPE_URL = "Url";
	public static final String TYPE_HTML = MIME_TYPE_DOCUMENT_HTML;
	public static final String TYPE_TEXT = MIME_TYPE_DOCUMENT_PLAINTEXT;

	private static final int CREATE_MAX_ITEMS = 10;

	private static final int INTEGER_WIDGET_LENGTH = 12;
	private static final int DOUBLE_WIDGET_LENGTH = 18;

	private static final 	Pattern INDEXED_FORM_FIELD_PATTERN = Pattern.compile("(.+)\\.(\\d+)");

	/************** the metadata extension of edit/create contexts *****************************************/

	private static final String STATE_METADATA_GROUPS = "resources.metadata.types";

	private static final String INTENT_REVISE_FILE = "revise";
	private static final String INTENT_REPLACE_FILE = "replace";

	/** State attribute for where there is at least one attachment before invoking attachment tool */
	public static final String STATE_HAS_ATTACHMENT_BEFORE = "resources.has_attachment_before";

	/** The name of the state attribute containing a list of new items to be attached */
	private static final String STATE_HELPER_NEW_ITEMS = "resources.helper_new_items";

	/** The name of the state attribute indicating that the list of new items has changed */
	private static final String STATE_HELPER_CHANGED = "resources.helper_changed";


	/** The name of the optional state attribute indicating the id of the collection that should be treated as the "home" collection */
	public static final String STATE_ATTACH_COLLECTION_ID = "resources.attach_collection_id";

	/** The name of the state attribute containing the name of the tool that invoked Resources as attachment helper */
	public static final String STATE_ATTACH_TOOL_NAME = "resources.attach_tool_name";

	/** The name of the state attribute for "new-item" attachment indicating the type of item */
	public static final String STATE_ATTACH_TEXT = "resources.attach_text";

	/** The name of the state attribute for "new-item" attachment indicating the id of the item to edit */
	public static final String STATE_ATTACH_ITEM_ID = "resources.attach_collection_id";

	/************** the helper context (file-picker) *****************************************/

	/**
	 *  State attribute for the Vector of References, one for each attachment.
	 *  Using tools can pre-populate, and can read the results from here. 
	 */
	public static final String STATE_ATTACHMENTS = "resources.state_attachments";
	
	/**
	 *  The name of the state attribute indicating that the file picker should return links to
	 *  existing resources in an existing collection rather than copying it to the hidden attachments
	 *  area.  If this value is not set, all attachments are to copies in the hidden attachments area.
	 */
	public static final String STATE_ATTACH_LINKS = "resources.state_attach_links";

	/** 
	 * The name of the state attribute for the maximum number of items to attach. The attribute value will be an Integer, 
	 * usually CARDINALITY_SINGLE or CARDINALITY_MULTIPLE. 
	 */
	public static final String STATE_ATTACH_CARDINALITY = "resources.state_attach_cardinality";

	/** A constant indicating maximum of one item can be attached. */
	public static final Integer CARDINALITY_SINGLE = FilePickerHelper.CARDINALITY_SINGLE;

	/** A constant indicating any the number of attachments is unlimited. */
	public static final Integer CARDINALITY_MULTIPLE = FilePickerHelper.CARDINALITY_MULTIPLE;

	/**
	 *  The name of the state attribute for the title when a tool uses Resources as attachment helper (for create or attach but not for edit mode) 
	 */
	public static final String STATE_ATTACH_TITLE = "resources.state_attach_title_text";

	/** 
	 * The name of the state attribute for the instructions when a tool uses Resources as attachment helper 
	 * (for create or attach but not for edit mode) 
	 */
	public static final String STATE_ATTACH_INSTRUCTION = "resources.state_attach_instruction_text";

	/** 
	 * State Attribute for the org.sakaiproject.content.api.ContentResourceFilter
	 * object that the current filter should honor.  If this is set to null, then all files will
	 * be selectable and viewable 
	 */
	   public static final String STATE_ATTACH_FILTER = "resources.state_attach_filter";

	/**
	 * @deprecated use STATE_ATTACH_TITLE and STATE_ATTACH_INSTRUCTION instead
	 */
	public static final String STATE_FROM_TEXT = "attachment.from_text";

	/**
	 *  the name of the state attribute indicating that the user canceled out of the helper.  Is set only if the user canceled out of the helper. 
	 */
	public static final String STATE_HELPER_CANCELED_BY_USER = "resources.state_attach_canceled_by_user";
	
	/**
	 *  The name of the state attribute indicating that dropboxes should be shown as places from which
	 *  to select attachments. The value should be a List of user-id's.  The file picker will attempt to show 
	 *  the dropbox for each user whose id is included in the list. If this 
	 */
	public static final String STATE_ATTACH_SHOW_DROPBOXES = "resources.state_attach_show_dropboxes";

	/**
	 *  The name of the state attribute indicating that the current user's workspace Resources collection 
	 *  should be shown as places from which to select attachments. The value should be "true".  The file picker will attempt to show 
	 *  the workspace if this attribute is set to "true". 
	 */
	public static final String STATE_ATTACH_SHOW_WORKSPACE = "resources.state_attach_show_workspace";
	
	
	/************** the columns context *****************************************/

	public static final String STATE_COLUMN_ITEM_ID = "resources.state_column_item_id";

	
	
	/************** the delete context *****************************************/

	/** The delete ids */
	private static final String STATE_DELETE_IDS = "resources.delete_ids";

	/** The not empty delete ids */
	private static final String STATE_NOT_EMPTY_DELETE_IDS = "resource.not_empty_delete_ids";

	/** The name of the state attribute containing a list of ChefBrowseItem objects corresponding to resources selected for deletion */
	private static final String STATE_DELETE_ITEMS = "resources.delete_items";

	/** The name of the state attribute containing a list of ChefBrowseItem objects corresponding to nonempty folders selected for deletion */
	private static final String STATE_DELETE_ITEMS_NOT_EMPTY = "resources.delete_items_not_empty";

	/** The name of the state attribute containing a list of ChefBrowseItem objects selected for deletion that cannot be deleted */
	private static final String STATE_DELETE_ITEMS_CANNOT_DELETE = "resources.delete_items_cannot_delete";

	/************** the cut items context *****************************************/

	/** The cut item ids */
	private static final String STATE_CUT_IDS = "resources.revise_cut_ids";

	/************** the copied items context *****************************************/

	/** The copied item ids */
	private static final String STATE_COPIED_IDS = "resources.revise_copied_ids";

	/** The copied item id */
	private static final String STATE_COPIED_ID = "resources.revise_copied_id";

	/************** the moved items context *****************************************/

	/** The copied item ids */
	private static final String STATE_MOVED_IDS = "resources.revise_moved_ids";
	
	/************** the reorder context *****************************************/

	protected static final String STATE_REORDER_FOLDER = "resources.reorder_folder_id";

	/** The property (column) to sort by in the reorder context */
	protected static final String STATE_REORDER_SORT_BY = "resources.reorder_sort_by";
	
	/** The sort ascending or decending for the reorder context */
	protected static final String STATE_REORDER_SORT_ASC = "resources.sort_asc";

	/** Modes. */
	private static final String MODE_LIST = "list";
	private static final String MODE_EDIT = "edit";
	private static final String MODE_DAV = "webdav";
	private static final String MODE_CREATE = "create";
	private static final String MODE_CREATE_WIZARD = "createWizard";
	private static final String MODE_DELETE_FINISH = "deleteFinish";
	private static final String MODE_REVISE_METADATA = "revise_metadata";

	public  static final String MODE_HELPER = "helper";
	private static final String MODE_DELETE_CONFIRM = "deleteConfirm";
	private static final String MODE_MORE = "more";
	private static final String MODE_PROPERTIES = "properties";
	private static final String MODE_REORDER = "reorder";

	private static final String STATE_LIST_PREFERENCE = "resources.state_list_preference";
	private static final String LIST_COLUMNS = "columns";
	private static final String LIST_HIERARCHY = "hierarchy";

	/** modes for attachment helper */
	public static final String MODE_ATTACHMENT_SELECT = "resources.attachment_select";
	public static final String MODE_ATTACHMENT_CREATE = "resources.attachment_create";
	public static final String MODE_ATTACHMENT_NEW_ITEM = "resources.attachment_new_item";
	public static final String MODE_ATTACHMENT_EDIT_ITEM = "resources.attachment_edit_item";
	public static final String MODE_ATTACHMENT_CONFIRM = "resources.attachment_confirm";
	public static final String MODE_ATTACHMENT_SELECT_INIT = "resources.attachment_select_initialized";
	public static final String MODE_ATTACHMENT_CREATE_INIT = "resources.attachment_create_initialized";
	public static final String MODE_ATTACHMENT_NEW_ITEM_INIT = "resources.attachment_new_item_initialized";
	public static final String MODE_ATTACHMENT_EDIT_ITEM_INIT = "resources.attachment_edit_item_initialized";
	public static final String MODE_ATTACHMENT_CONFIRM_INIT = "resources.attachment_confirm_initialized";
	public static final String MODE_ATTACHMENT_DONE = "resources.attachment_done";

	/** vm files for each mode. */
	private static final String TEMPLATE_LIST = "content/chef_resources_list";
	private static final String TEMPLATE_EDIT = "content/chef_resources_edit";
	private static final String TEMPLATE_CREATE = "content/chef_resources_create";
	private static final String TEMPLATE_DAV = "content/chef_resources_webdav";
	private static final String TEMPLATE_ITEMTYPE = "content/chef_resources_itemtype";
	private static final String TEMPLATE_SELECT = "content/chef_resources_select";
	private static final String TEMPLATE_ATTACH = "content/chef_resources_attach";

	private static final String TEMPLATE_MORE = "content/chef_resources_more";
	private static final String TEMPLATE_DELETE_CONFIRM = "content/chef_resources_deleteConfirm";
	private static final String TEMPLATE_DELETE_FINISH = "content/sakai_resources_deleteFinish";
	private static final String TEMPLATE_PROPERTIES = "content/chef_resources_properties";
	// private static final String TEMPLATE_REPLACE = "_replace";
	private static final String TEMPLATE_REORDER = "content/chef_resources_reorder";

	private static final String TEMPLATE_REVISE_METADATA = "content/sakai_resources_properties";

	/** the site title */
	private static final String STATE_SITE_TITLE = "site_title";

	/** copyright related info */
	private static final String COPYRIGHT_TYPES = "copyright_types";
	private static final String COPYRIGHT_TYPE = "copyright_type";
	private static final String DEFAULT_COPYRIGHT = "default_copyright";
	private static final String COPYRIGHT_ALERT = "copyright_alert";
	private static final String DEFAULT_COPYRIGHT_ALERT = "default_copyright_alert";
	private static final String COPYRIGHT_FAIRUSE_URL = "copyright_fairuse_url";
	private static final String NEW_COPYRIGHT_INPUT = "new_copyright_input";
	private static final String COPYRIGHT_SELF_COPYRIGHT = rb.getString("cpright2");
	private static final String COPYRIGHT_NEW_COPYRIGHT = rb.getString("cpright3");
	private static final String COPYRIGHT_ALERT_URL = ServerConfigurationService.getAccessUrl() + COPYRIGHT_PATH;
	
	/** state attribute indicating whether we're using the Creative Commons dialog instead of the "old" copyright dialog */
	protected static final String STATE_USING_CREATIVE_COMMONS = "resources.usingCreativeCommons";

	private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

	/** The default value for whether to show all sites in file-picker (used if global value can't be read from server config service) */
	public static final boolean SHOW_ALL_SITES_IN_FILE_PICKER = false;

	/** The default value for whether to show all sites in resources tool (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_RESOURCES = false;

	/** The default value for whether to show all sites in dropbox (used if global value can't be read from server config service) */
	private static final boolean SHOW_ALL_SITES_IN_DROPBOX = false;

	/** The default number of members for a collection at which this tool should refuse to expand the collection. Used only if value can't be read from config service. */
	protected static final int EXPANDABLE_FOLDER_SIZE_LIMIT = 256;

	/** Name of state attribute indicating number of members for a collection at which this tool should refuse to expand the collection. */
	private static final String STATE_EXPANDABLE_FOLDER_SIZE_LIMIT = "resources.expandable_folder_size_limit";

	protected static final String STATE_SHOW_REMOVE_ACTION = "resources.show_remove_action";

	protected static final String STATE_SHOW_MOVE_ACTION = "resources.show_move_action";

	protected static final String STATE_SHOW_COPY_ACTION = "resources.show_copy_action";

	protected static final String STATE_HIGHLIGHTED_ITEMS = "resources.highlighted_items";

	/** The default number of site collections per page. */
	protected static final int DEFAULT_PAGE_SIZE = 50;

	protected static final String PARAM_PAGESIZE = "collections_per_page";

	protected static final String STATE_TOP_MESSAGE_INDEX = "resources.top_message_index";

	protected static final String STATE_REMOVED_ATTACHMENTS = "resources.removed_attachments";
	
	/********* Global constants *********/

	/** The null/empty string */
	private static final String NULL_STRING = "";

	/** The string used when pasting the same resource to the same folder */
	private static final String DUPLICATE_STRING = rb.getString("copyof") + " ";

	/** The string used when pasting shirtcut of the same resource to the same folder */
	private static final String SHORTCUT_STRING = rb.getString("shortcut");

	/** The copyright character (Note: could be "\u00a9" if we supported UNICODE for specials -ggolden */
	private static final String COPYRIGHT_SYMBOL = rb.getString("cpright1");

	/** The String of new copyright */
	private static final String NEW_COPYRIGHT = "newcopyright";

	/** The resource not exist string */
	private static final String RESOURCE_NOT_EXIST_STRING = rb.getString("notexist1");

	/** The title invalid string */
	private static final String RESOURCE_INVALID_TITLE_STRING = rb.getString("titlecannot");

	/** The copy, cut, paste not operate on collection string */
	private static final String RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING = rb.getString("notsupported");

	/** The maximum number of suspended operations that can be on the stack. */
	private static final int MAXIMUM_SUSPENDED_OPERATIONS_STACK_DEPTH = 10;

	/** portlet configuration parameter values**/
	public static final String RESOURCES_MODE_RESOURCES = "resources";
	public static final String RESOURCES_MODE_DROPBOX = "dropbox";
	public static final String RESOURCES_MODE_HELPER = "helper";

	/** The from state name */
	private static final String STATE_FROM = "resources.from";

	private static final String STATE_ENCODING = "resources.encoding";

	private static final String DELIM = "@";

	/** string used to represent "public" access mode in UI elements */
	protected static final String PUBLIC_ACCESS = "public";

	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	protected static final long ONE_WEEK = 1000L * 60L * 60L * 24L * 7L;

	/************************** Comparators **************************/
	
	protected static final String STATE_LIST_VIEW_SORT = "resources.list_view_sort";

	protected static final String STATE_REORDER_SORT = "resources.reorder_sort";

	protected static final String STATE_DEFAULT_SORT = "resources.default_sort";

	protected static final String STATE_EXPANDED_FOLDER_SORT_MAP = "resources.expanded_folder_sort_map";

	protected static final String STATE_CREATE_WIZARD_ACTION = "resources.create_wizard_action";

	protected static final String STATE_CREATE_WIZARD_ITEM = "resources.create_wizard_item";

	protected static final String STATE_CREATE_WIZARD_COLLECTION_ID = "resources.create_wizard_collection_id";

	public static final String UTF_8_ENCODING = "UTF-8";

	protected static final String STATE_DELETE_SET = "resources.delete_set";
	protected static final String STATE_NON_EMPTY_DELETE_SET = "resources.non-empty_delete_set";

	protected static final String STATE_REVISE_PROPERTIES_ENTITY_ID = "resources.revise_properties_entity_id";
	protected static final String STATE_REVISE_PROPERTIES_ITEM = "resources.revise_properties_item";
	protected static final String STATE_REVISE_PROPERTIES_ACTION = "resources.revise_properties_action";
	
	protected static final String STATE_ITEM_TO_BE_COPIED = "resources.item_to_be_copied";
	protected static final String STATE_ITEM_TO_BE_MOVED = "resources.item_to_be_moved";


	/**
	* Build the context for normal display
	*/
	public String buildMainPanelContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		
		// get CHS
		org.sakaiproject.content.api.ContentHostingService contentHostingService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute(STATE_CONTENT_SERVICE);

		context.put("copyright_alert_url", COPYRIGHT_ALERT_URL);
		context.put("ACTION_DELIMITER", ResourceToolAction.ACTION_DELIMITER);
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe != null)
		{
			if(pipe.isActionCanceled())
			{
				state.setAttribute(STATE_MODE, MODE_LIST);
			}
			else if(pipe.isErrorEncountered())
			{
				String msg = pipe.getErrorMessage();
				if(msg != null && ! msg.trim().equals(""))
				{
					addAlert(state, msg);
				}
				state.setAttribute(STATE_MODE, MODE_LIST);
			}
			else if(pipe.isActionCompleted())
			{
				finishAction(state, toolSession, pipe);
			}
			toolSession.removeAttribute(ResourceToolAction.DONE);
		}

		String template = null;
		
		// place if notification is enabled and current site is not of My Workspace type
		boolean isUserSite = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("notification", new Boolean(!isUserSite && notificationEnabled(state)));
		// get the mode
		String mode = (String) state.getAttribute (STATE_MODE);
		String helper_mode = (String) state.getAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
		if (!MODE_HELPER.equals(mode) && helper_mode != null)
		{
			// not in helper mode, but a helper context is needed

			// if the mode is not done, defer to the helper context
			if (!mode.equals(ResourcesAction.MODE_ATTACHMENT_DONE))
			{
				template = ResourcesAction.buildHelperContext(portlet, context, data, state);
				// template = AttachmentAction.buildHelperContext(portlet, context, runData, sstate);
				return template;
			}

			// clean up
			state.removeAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
			state.removeAttribute(ResourcesAction.STATE_ATTACHMENTS);
		}

		if (mode.equals (MODE_LIST))
		{
			String list_pref = (String) state.getAttribute(STATE_LIST_PREFERENCE);
			if(list_pref == null)
			{
				list_pref = LIST_HIERARCHY;
			}
			if(LIST_COLUMNS.equals(list_pref))
			{
				// build the context for list view
				template = buildColumnsContext (portlet, context, data, state);
			}
			else
			{
				// build the context for list view
				template = buildChefListContext (portlet, context, data, state);
			}
		}
		else if (mode.equals (MODE_HELPER))
		{
			// build the context for add item
			template = buildHelperContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_CREATE))
		{
			// build the context for create item
			template = buildCreateContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_CREATE_WIZARD))
		{
			template = buildCreateWizardContext(portlet, context, data, state);
		}
		else if (mode.equals (MODE_DELETE_CONFIRM))
		{
			// build the context for the basic step of delete confirm page
			template = buildDeleteConfirmContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_DELETE_FINISH))
		{
			// build the context for the basic step of delete confirm page
			template = buildDeleteFinishContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_MORE))
		{
			// build the context to display the property list
			template = buildMoreContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_EDIT))
		{
			// build the context to display the property list
			template = buildEditContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_OPTIONS))
		{
			template = buildOptionsPanelContext (portlet, context, data, state);
		}
		else if (mode.equals (MODE_REORDER))
		{
			template = buildReorderContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_DAV))
		{
			template = buildWebdavContext (portlet, context, data, state);
		}
		else if(mode.equals(MODE_REVISE_METADATA))
		{
			template = buildReviseMetadataContext(portlet, context, data, state);
		}

		return template;

	}	// buildMainPanelContext

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	private String buildReviseMetadataContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		// complete the create wizard
		String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		String encoding = data.getRequest().getCharacterEncoding();

		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
		String refstr = ContentHostingService.getReference(entityId);
		Reference ref = EntityManager.newReference(refstr);
		ContentEntity entity = (ContentEntity) ref.getEntity();

		EditItem item = new  EditItem(entity);
		if(item.getReleaseDate() == null)
		{
			item.setReleaseDate(TimeService.newTime());
		}
		if(item.getRetractDate() == null)
		{
			item.setRetractDate(defaultRetractDate);
		}
		
		context.put("item", item);
		state.setAttribute(STATE_REVISE_PROPERTIES_ITEM, item);
		
		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);
		
		return TEMPLATE_REVISE_METADATA;
	}

	/**
	 * @param state
	 * @param toolSession
	 * @param pipe
	 */
	protected void finishAction(SessionState state, ToolSession toolSession, ResourceToolActionPipe pipe)
	{
		ResourceToolAction action = pipe.getAction();
		// use ActionType for this 
		switch(action.getActionType())
		{
		case CREATE:
			state.setAttribute(STATE_MODE, MODE_CREATE_WIZARD);
			break;
		case NEW_UPLOAD:
			createResources(pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			break;
		case NEW_FOLDER:
			createFolders(state, pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			break;
		case REVISE_CONTENT:
			reviseContent(pipe);
			toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);
			state.setAttribute(STATE_MODE, MODE_LIST);
			break;
		default:
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}

	/**
	 * @param pipe
	 * @param state 
	 */
	protected void createFolders(SessionState state, ResourceToolActionPipe pipe)
	{
		String collectionId = pipe.getContentEntity().getId();
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			String name = fp.getFileName();
			if(name == null || name.trim().equals(""))
			{
				continue;
			}
			try
			{
				ContentCollectionEdit edit = ContentHostingService.addCollection(collectionId, name);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				ContentHostingService.commitCollection(edit);
			}
			catch (PermissionException e)
			{
				addAlert(state, trb.getString("alert.perm"));
				break;
			}
			catch (IdInvalidException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdInvalidException ", e);
			}
			catch (IdUsedException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.exists", args));
				// logger.warn("IdUsedException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUnusedException ", e);
				break;
			}
			catch (IdLengthException e)
			{
				String[] args = { name };
				addAlert(state, trb.getFormattedMessage("alert.toolong", args));
				logger.warn("IdLengthException ", e);
			}
			catch (TypeException e)
			{
				// TODO Auto-generated catch block
				logger.warn("TypeException id=" + collectionId + name, e);
			}
		}
	}

	/**
	 * @param pipe
	 * @param action
	 */
	private void reviseContent(ResourceToolActionPipe pipe)
	{
		ResourceToolAction action = pipe.getAction();
		ContentEntity entity = pipe.getContentEntity();
		try
		{
			ContentResourceEdit edit = ContentHostingService.editResource(entity.getId());
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			// update content
			edit.setContent(pipe.getRevisedContent());
			// update properties
			if(action instanceof InteractionAction)
			{
				InteractionAction iAction = (InteractionAction) action;
				Map revprops = pipe.getRevisedResourceProperties();
				List propkeys = iAction.getRequiredPropertyKeys();
				if(propkeys != null)
				{
					Iterator keyIt = propkeys.iterator();
					while(keyIt.hasNext())
					{
						String key = (String) keyIt.next();
						String value = (String) revprops.get(key);
						if(value == null)
						{
							props.removeProperty(key);
						}
						else
						{
							// should we support multivalued properties?
							props.addProperty(key, value);
						}
					}
				}
			}
			// update mimetype
			edit.setContentType(pipe.getRevisedMimeType());
			ContentHostingService.commitResource(edit);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			logger.warn("PermissionException ", e);
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			logger.warn("TypeException ", e);
		}
		catch (InUseException e)
		{
			// TODO Auto-generated catch block
			logger.warn("InUseException ", e);
		}
		catch (OverQuotaException e)
		{
			// TODO Auto-generated catch block
			logger.warn("OverQuotaException ", e);
		}
		catch (ServerOverloadException e)
		{
			// TODO Auto-generated catch block
			logger.warn("ServerOverloadException ", e);
		}
	}

	/**
	 * @param pipe
	 */
	private void createResources(ResourceToolActionPipe pipe)
	{
		MultiFileUploadPipe mfp = (MultiFileUploadPipe) pipe;
		Iterator<ResourceToolActionPipe> pipeIt = mfp.getPipes().iterator();
		while(pipeIt.hasNext())
		{
			ResourceToolActionPipe fp = pipeIt.next();
			String collectionId = pipe.getContentEntity().getId();
			String name = fp.getFileName();
			if(name == null || name.trim().equals(""))
			{
				continue;
			}
			String basename = name.trim();
			String extension = "";
			if(name.contains("."))
			{
				String[] parts = name.split("\\.");
				basename = parts[0];
				if(parts.length > 1)
				{
					extension = parts[parts.length - 1];
				}
				
				for(int i = 1; i < parts.length - 1; i++)
				{
					basename += "." + parts[i];
					// extension = parts[i + 1];
				}
			}
			try
			{
				ContentResourceEdit resource = ContentHostingService.addResource(collectionId,basename,extension,MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				resource.setContent(fp.getRevisedContent());
				resource.setContentType(fp.getRevisedMimeType());
				resource.setResourceType(pipe.getAction().getTypeId());
				ContentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
			}
			catch (PermissionException e)
			{
				// TODO Auto-generated catch block
				logger.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUsedException ", e);
			}
			catch (IdInvalidException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdInvalidException ", e);
			}
			catch (IdUniquenessException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUniquenessException ", e);
			}
			catch (IdLengthException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdLengthException ", e);
			}
			catch (OverQuotaException e)
			{
				// TODO Auto-generated catch block
				logger.warn("OverQuotaException ", e);
			}
			catch (ServerOverloadException e)
			{
				// TODO Auto-generated catch block
				logger.warn("ServerOverloadException ", e);
			}
		}
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	private String buildDeleteFinishContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("tlang",rb);
		context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

		//%%%% FIXME
		context.put ("collectionPath", state.getAttribute (STATE_COLLECTION_PATH));

		List deleteItems = (List) state.getAttribute(STATE_DELETE_SET);
		List nonEmptyFolders = (List) state.getAttribute(STATE_NON_EMPTY_DELETE_SET);

		context.put ("deleteItems", deleteItems);

		Iterator it = nonEmptyFolders.iterator();
		while(it.hasNext())
		{
			ListItem folder = (ListItem) it.next();
			String[] args = { folder.getName() };
			String msg = rb.getFormattedMessage("folder.notempty", args) + " ";
			addAlert(state, msg);
		}

		//  %%STATE_MODE_RESOURCES%%
		//not show the public option when in dropbox mode
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// not show the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", ContentHostingService.newResourceProperties ());

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_DELETE_FINISH;

	}

	/**
	 * Iterate over attributes in ToolSession and remove all attributes starting with a particular prefix.
	 * @param toolSession
	 * @param prefix
	 */
	protected void cleanup(ToolSession toolSession, String prefix) 
	{
		Enumeration attributeNames = toolSession.getAttributeNames();
		while(attributeNames.hasMoreElements())
		{
			String aName = (String) attributeNames.nextElement();
			if(aName.startsWith(prefix))
			{
				toolSession.removeAttribute(aName);
			}
		}
		
	}

	public String buildCreateWizardContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		String template = "content/sakai_resources_cwiz_finish";
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe.isActionCanceled())
		{
			// go back to list view
			
		}
		else if(pipe.isErrorEncountered())
		{
			// report the error?
			
		}
		else
		{
			// complete the create wizard
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String encoding = data.getRequest().getCharacterEncoding();

			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}
	
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}

			ContentEntity collection = pipe.getContentEntity();

			List items = newEditItems(collection.getId(), pipe.getAction().getTypeId(), encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, new Integer(1));

			ChefEditItem item = (ChefEditItem) items.get(0);
			item.setContent(pipe.getContent());
			item.setMimeType(pipe.getMimeType());
			
			context.put("item", item);
			
			state.setAttribute(STATE_CREATE_WIZARD_ITEM, item);
			
			if(ContentHostingService.isAvailabilityEnabled())
			{
				context.put("availability_is_enabled", Boolean.TRUE);
			}
			
			context.put("SITE_ACCESS", AccessMode.SITE.toString());
			context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
			context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
			context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);
		}
		return template;
	}

	/**
	 * Build the context to establish a custom-ordering of resources/folders within a folder.
	 */
	public String buildColumnsContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		context.put("tlang",rb);
		
		// need to check permissions
		
		// get the id of the item currently selected
		String selectedItemId = (String) state.getAttribute(STATE_COLUMN_ITEM_ID);
		if(selectedItemId == null)
		{
			selectedItemId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		}
		context.put("selectedItemId", selectedItemId);
		String folderId = null;
		
		// need a list of folders (ListItem objects) for one root in context as $folders
		List folders = new Vector();
		ContentCollection collection = null;
		ContentEntity selectedItem = null;
		
		// need a list of roots (ListItem objects) in context as $roots
		List roots = new Vector();
		Map othersites = ContentHostingService.getCollectionMap();
		Iterator it = othersites.keySet().iterator();
		while(it.hasNext())
		{
			String rootId = (String) it.next();
			String rootName = (String) othersites.get(rootId);
			ListItem root = new ListItem(rootId);
			root.setName(rootName);
			root.setHoverText(rootName);
			root.setAccessUrl(ContentHostingService.getUrl(rootId));
			root.setIconLocation(ContentTypeImageService.getContentTypeImage("folder"));
			
			if(selectedItemId != null && selectedItemId.startsWith(rootId))
			{
				root.setSelected(true);
				folderId = rootId;
				try
				{
					selectedItem = ContentHostingService.getCollection(rootId);
				}
				catch (IdUnusedException e)
				{
					// TODO Auto-generated catch block
					logger.warn("IdUnusedException ", e);
				}
				catch (TypeException e)
				{
					// TODO Auto-generated catch block
					logger.warn("TypeException ", e);
				}
				catch (PermissionException e)
				{
					// TODO Auto-generated catch block
					logger.warn("PermissionException ", e);
				}
			}
			roots.add(root);
		}
		// sort by name?
		context.put("roots", roots);
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		
		while(folderId != null)
		{
			String collectionId = folderId;
			folderId = null;

			List folder = new Vector();
			try 
			{
				if(collection == null)
				{
					collection = ContentHostingService.getCollection(collectionId);
				}
				List members = collection.getMemberResources();
				collection = null;
				Iterator memberIt = members.iterator();
				while(memberIt.hasNext())
				{
					ContentEntity member = (ContentEntity) memberIt.next();
					String itemId = member.getId();
					ListItem item = new ListItem(member);
					if(selectedItemId != null && (selectedItemId.equals(itemId) || (member.isCollection() && selectedItemId.startsWith(itemId))))
					{
						selectedItem = member;
						item.setSelected(true);
						if(member.isCollection())
						{
							folderId = itemId;
						}
					}
					else
					{
						item.setSelected(false);
					}
					folder.add(item);
				}
				folders.add(folder);
				
				
			} 
			catch (IdUnusedException e) 
			{
				// TODO Auto-generated catch block
				logger.warn("IdUnusedException " + e.getMessage());
			} 
			catch (TypeException e) 
			{
				// TODO Auto-generated catch block
				logger.warn("TypeException " + e.getMessage());
			} 
			catch (PermissionException e) 
			{
				// TODO Auto-generated catch block
				logger.warn("PermissionException " + e.getMessage());
			}
			
		}
		context.put("folders", folders);
		
		if(selectedItem != null)
		{
			String resourceType = ResourceType.TYPE_UPLOAD;
			Reference ref = EntityManager.newReference(selectedItem.getReference());
			List actions = new Vector();
			if(selectedItem.isCollection())
			{
				resourceType = ResourceType.TYPE_FOLDER;
			}
			else
			{
				ContentResource resource = (ContentResource) selectedItem;
				// String mimetype = resource.getContentType();
				resourceType = resource.getResourceType();
			}
			
			// get the registration for the current item's type 
			ResourceType typeDef = registry.getType(resourceType);
			
			// if copy or move is in progress AND user has content.new for this folder, user can paste in the collection 
			// (the paste action will only be defined for collections)
			String item_to_be_moved = (String) state.getAttribute(STATE_ITEM_TO_BE_MOVED);
			if(item_to_be_moved != null)
			{
				List<ResourceToolAction> conditionalContentNewActions = typeDef.getActions(PASTE_MOVED_ACTIONS);
				if(conditionalContentNewActions != null)
				{
					actions.addAll(conditionalContentNewActions);
				}
			}

			String item_to_be_copied = (String) state.getAttribute(STATE_ITEM_TO_BE_COPIED);
			if(item_to_be_copied != null)
			{
				List<ResourceToolAction> conditionalContentNewActions = typeDef.getActions(PASTE_COPIED_ACTIONS);
				if(conditionalContentNewActions != null)
				{
					actions.addAll(conditionalContentNewActions);
				}
			}

			// certain actions are defined elsewhere but pertain only to collections
			if(selectedItem.isCollection())
			{
				// if item is collection and user has content.new for item, user can create anything 
				{
					// iterate over resource-types and get all the registered types and find actions requiring "content.new" permission
					Collection types = registry.getTypes();
					Iterator<ActionType> actionTypeIt = CONTENT_NEW_ACTIONS.iterator();
					while(actionTypeIt.hasNext())
					{
						ActionType actionType = actionTypeIt.next();
						Iterator typeIt = types.iterator();
						while(typeIt.hasNext())
						{
							ResourceType type = (ResourceType) typeIt.next();
							
							List<ResourceToolAction> createActions = type.getActions(actionType);
							if(createActions != null)
							{
								actions.addAll(createActions);
							}
						}
					}
				}
				
			}

			// if user has content.read, user can view content, view metadata and/or copy
			List<ResourceToolAction> contentReadActions = typeDef.getActions(CONTENT_READ_ACTIONS);
			if(contentReadActions != null)
			{
				actions.addAll(contentReadActions);
			}
			
			// if user has content.modify, user can revise metadata, revise content, and/or replace content
			List<ResourceToolAction> contentModifyActions = typeDef.getActions(CONTENT_MODIFY_ACTIONS);
			if(contentModifyActions != null)
			{
				actions.addAll(contentModifyActions);
			}
			
			// if user has content.delete, user can move item or delete item
			List<ResourceToolAction> contentDeleteActions = typeDef.getActions(CONTENT_DELETE_ACTIONS);
			if(contentDeleteActions != null)
			{
				actions.addAll(contentDeleteActions);
			}
			
			// if user has content.new for item's parent and content.read for item, user can duplicate item
			List<ResourceToolAction> contentNewOnParentActions = typeDef.getActions(CONTENT_NEW_FOR_PARENT_ACTIONS);
			if(contentNewOnParentActions != null)
			{
				actions.addAll(contentNewOnParentActions);
			}
			// filter -- remove actions that are not available to the current user in the context of this item
			Iterator<ResourceToolAction> actionIt = actions.iterator();
			while(actionIt.hasNext())
			{
				ResourceToolAction action = actionIt.next();
				if(! action.available(ref.getContext()))
				{
					actionIt.remove();
				}
			}
			context.put("actions", actions);
			context.put("labeler", new Labeler());
		}
		
		return "content/sakai_resources_columns";
	}
	
	public void doReviseProperties(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		if(user_action.equals("save"))
		{
			String entityId = (String) state.getAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID);
			EditItem item = (EditItem) state.getAttribute(STATE_REVISE_PROPERTIES_ITEM);
			ResourceToolAction action = (ResourceToolAction) state.getAttribute(STATE_REVISE_PROPERTIES_ACTION);
			String name = params.getString("name");
			String description = params.getString("description");
			String resourceType = action.getTypeId();
			// rights
			String copyright = params.getString("copyright");
			String newcopyright = params.getString("newcopyright");
			boolean copyrightAlert = params.getBoolean("copyrightAlert");
			
			// availability
			boolean hidden = params.getBoolean("hidden");
			boolean use_start_date = params.getBoolean("use_start_date");
			boolean use_end_date = params.getBoolean("use_end_date");
			Time releaseDate = null;
			Time retractDate = null;
			
			if(use_start_date)
			{
				int begin_year = params.getInt("release_year");
				int begin_month = params.getInt("release_month");
				int begin_day = params.getInt("release_day");
				int begin_hour = params.getInt("release_hour");
				int begin_min = params.getInt("release_min");
				String release_ampm = params.getString("release_ampm");
				if("pm".equals(release_ampm))
				{
					begin_hour += 12;
				}
				else if(begin_hour == 12)
				{
					begin_hour = 0;
				}
				releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
			}
			
			if(use_end_date)
			{
				int end_year = params.getInt("retract_year");
				int end_month = params.getInt("retract_month");
				int end_day = params.getInt("retract_day");
				int end_hour = params.getInt("retract_hour");
				int end_min = params.getInt("retract_min");
				String retract_ampm = params.getString("retract_ampm");
				if("pm".equals(retract_ampm))
				{
					end_hour += 12;
				}
				else if(end_hour == 12)
				{
					end_hour = 0;
				}
				retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
			}
			
			// access
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			String access_mode = params.getString("access_mode");
			SortedSet groups = new TreeSet();
			
			if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
			{
				// we inherit more than one group and must check whether group access changes at this item
				String[] access_groups = params.getStrings("access_groups");
				
				SortedSet new_groups = new TreeSet();
				if(access_groups != null)
				{
					new_groups.addAll(Arrays.asList(access_groups));
				}
				//new_groups = item.convertToRefs(new_groups);
				
//				Collection inh_grps = null;
//				//item.getInheritedGroupRefs();
//				boolean groups_are_inherited = (new_groups.size() == inh_grps.size()) && inh_grps.containsAll(new_groups);
//				
//				if(groups_are_inherited)
//				{
//					new_groups.clear();
//					item.setEntityGroupRefs(new_groups);
//					item.setAccess(AccessMode.INHERITED.toString());
//				}
//				else
//				{
//					item.setEntityGroupRefs(new_groups);
//					item.setAccess(AccessMode.GROUPED.toString());
//				}
//				
//				item.setPubview(false);
			}
			else if(PUBLIC_ACCESS.equals(access_mode))
			{
//				if(! preventPublicDisplay.booleanValue() && ! item.isPubviewInherited())
//				{
//					item.setPubview(true);
//					item.setAccess(AccessMode.INHERITED.toString());
//				}
			}
			else if(AccessMode.INHERITED.toString().equals(access_mode))
			{
//				item.setAccess(AccessMode.INHERITED.toString());
//				item.clearGroups();
//				item.setPubview(false);
			}

			// notification
			int noti = NotificationService.NOTI_NONE;
			// %%STATE_MODE_RESOURCES%%
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
			{
				// set noti to none if in dropbox mode
				noti = NotificationService.NOTI_NONE;
			}
			else
			{
				// read the notification options
				String notification = params.getString("notify");
				if ("r".equals(notification))
				{
					noti = NotificationService.NOTI_REQUIRED;
				}
				else if ("o".equals(notification))
				{
					noti = NotificationService.NOTI_OPTIONAL;
				}
			}
			

			// set to public access if allowed and requested
			if(!preventPublicDisplay.booleanValue() && PUBLIC_ACCESS.equals(access_mode))
			{
				ContentHostingService.setPubView(entityId, true);
			}
			

			try 
			{
				if(item.isCollection())
				{
					ContentCollectionEdit entity = ContentHostingService.editCollection(entityId);
					ResourcePropertiesEdit resourceProperties = entity.getPropertiesEdit();
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
					resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
					entity.setAvailability(hidden, releaseDate, retractDate);
					ContentHostingService.commitCollection(entity);
				}
				else
				{
					ContentResourceEdit entity = ContentHostingService.editResource(entityId);
					entity.setResourceType(resourceType);
					ResourcePropertiesEdit resourceProperties = entity.getPropertiesEdit();
					resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
					resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
					entity.setAvailability(hidden, releaseDate, retractDate);
					ContentHostingService.commitResource(entity, noti);
				}

				state.setAttribute(STATE_MODE, MODE_LIST);
			} 
			catch (IdUnusedException e) 
			{
				logger.warn("IdUnusedException", e);
			} 
			catch (TypeException e) 
			{
				logger.warn("TypeException", e);
			} 
			catch (PermissionException e) 
			{
				logger.warn("PermissionException", e);
			} 
			catch (ServerOverloadException e) 
			{
				logger.warn("ServerOverloadException", e);
			}
			catch (OverQuotaException e)
			{
				// TODO Auto-generated catch block
				logger.warn("OverQuotaException ", e);
			}
			catch (InUseException e)
			{
				// TODO Auto-generated catch block
				logger.warn("InUseException ", e);
			}
			
		}
		else if(user_action.equals("cancel"))
		{
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}
	
	public void doShowMembers(RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		// get the item to be expanded
		String itemId = params.getString("item");
		if(itemId != null)
		{
			// put the itemId into state
			state.setAttribute(STATE_COLUMN_ITEM_ID, itemId);
		}
	}
	
	public void doColumns(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_COLUMNS);
	}
	
	public void doHierarchy(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
	}
	
	public void doCompleteCreateWizard(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		ChefEditItem item = (ChefEditItem) state.getAttribute(STATE_CREATE_WIZARD_ITEM);
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String user_action = params.getString("user_action");
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		if(user_action == null)
		{
			
		}
		else if(user_action.equals("save"))
		{
			String collectionId = (String) state.getAttribute(STATE_CREATE_WIZARD_COLLECTION_ID);
			ContentCollection collection;
			try 
			{
				collection = ContentHostingService.getCollection(collectionId );
				
				// title
				String name = params.getString("name");
				
				// create resource
				ContentResourceEdit resource = ContentHostingService.addResource (collectionId + name);
				
				String resourceType = null;
				if(pipe != null)
				{
					ResourceToolAction action = pipe.getAction();
					if(action == null)
					{
						
					}
					else 
					{
						if(action instanceof InteractionAction)
						{
							InteractionAction iAction = (InteractionAction) action;
							iAction.finalizeAction(EntityManager.newReference(resource.getReference()), pipe.getInitializationId());
						}
						resourceType = action.getTypeId();
					}
				}
				
				resource.setResourceType(resourceType);
				resource.setContent(pipe.getRevisedContent());		// item.getContent()
				resource.setContentType(pipe.getRevisedMimeType());		// item.getMimeType()
				
				
//						resourceProperties,
//						groups,
//						hidden,
//						releaseDate,
//						retractDate,
//						item.getNotification());
				
				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				resourceProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				
				Map values = pipe.getRevisedResourceProperties();
				Iterator valueIt = values.keySet().iterator();
				while(valueIt.hasNext())
				{
					String pname = (String) valueIt.next();
					String pvalue = (String) values.get(pname);
					resourceProperties.addProperty(pname, pvalue);
				}
				
				// description
				String description = params.getString("description");
				resourceProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
				
				// rights
				String copyright = params.getString("copyright");
				String newcopyright = params.getString("newcopyright");
				boolean copyrightAlert = params.getBoolean("copyrightAlert");
				
				// availability
				boolean hidden = params.getBoolean("hidden");
				boolean use_start_date = params.getBoolean("use_start_date");
				boolean use_end_date = params.getBoolean("use_end_date");
				Time releaseDate = null;
				Time retractDate = null;
				
				if(use_start_date)
				{
					int begin_year = params.getInt("release_year");
					int begin_month = params.getInt("release_month");
					int begin_day = params.getInt("release_day");
					int begin_hour = params.getInt("release_hour");
					int begin_min = params.getInt("release_min");
					String release_ampm = params.getString("release_ampm");
					if("pm".equals(release_ampm))
					{
						begin_hour += 12;
					}
					else if(begin_hour == 12)
					{
						begin_hour = 0;
					}
					releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
				}
				
				if(use_end_date)
				{
					int end_year = params.getInt("retract_year");
					int end_month = params.getInt("retract_month");
					int end_day = params.getInt("retract_day");
					int end_hour = params.getInt("retract_hour");
					int end_min = params.getInt("retract_min");
					String retract_ampm = params.getString("retract_ampm");
					if("pm".equals(retract_ampm))
					{
						end_hour += 12;
					}
					else if(end_hour == 12)
					{
						end_hour = 0;
					}
					retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
				}
				
				resource.setAvailability(hidden, releaseDate, retractDate);
				
				// access
				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				String access_mode = params.getString("access_mode");
				SortedSet groups = new TreeSet();
				
				if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
				{
					// we inherit more than one group and must check whether group access changes at this item
					String[] access_groups = params.getStrings("access_groups");
					
					SortedSet new_groups = new TreeSet();
					if(access_groups != null)
					{
						new_groups.addAll(Arrays.asList(access_groups));
					}
					new_groups = item.convertToRefs(new_groups);
					
					Collection inh_grps = item.getInheritedGroupRefs();
					boolean groups_are_inherited = (new_groups.size() == inh_grps.size()) && inh_grps.containsAll(new_groups);
					
					if(groups_are_inherited)
					{
						new_groups.clear();
						item.setEntityGroupRefs(new_groups);
						item.setAccess(AccessMode.INHERITED.toString());
					}
					else
					{
						item.setEntityGroupRefs(new_groups);
						item.setAccess(AccessMode.GROUPED.toString());
					}
					
					item.setPubview(false);
				}
				else if(PUBLIC_ACCESS.equals(access_mode))
				{
					if(! preventPublicDisplay.booleanValue() && ! item.isPubviewInherited())
					{
						item.setPubview(true);
						item.setAccess(AccessMode.INHERITED.toString());
					}
				}
				else if(AccessMode.INHERITED.toString().equals(access_mode))
				{
					item.setAccess(AccessMode.INHERITED.toString());
					item.clearGroups();
					item.setPubview(false);
				}
				
				// update resource with access info

				// notification
				int noti = NotificationService.NOTI_NONE;
				// %%STATE_MODE_RESOURCES%%
				if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
				{
					// set noti to none if in dropbox mode
					noti = NotificationService.NOTI_NONE;
				}
				else
				{
					// read the notification options
					String notification = params.getString("notify");
					if ("r".equals(notification))
					{
						noti = NotificationService.NOTI_REQUIRED;
					}
					else if ("o".equals(notification))
					{
						noti = NotificationService.NOTI_OPTIONAL;
					}
				}
				
				ContentHostingService.commitResource(resource, noti);
				
				toolSession.removeAttribute(ResourceToolAction.ACTION_PIPE);

				// set to public access if allowed and requested
				if(!preventPublicDisplay.booleanValue() && PUBLIC_ACCESS.equals(access_mode))
				{
					ContentHostingService.setPubView(resource.getId(), true);
				}
				
				// show folder if in hierarchy view
				SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				expandedCollections.add(collectionId);

				state.setAttribute(STATE_MODE, MODE_LIST);
			} 
			catch (IdUnusedException e) 
			{
				logger.warn("IdUnusedException", e);
			} 
			catch (TypeException e) 
			{
				logger.warn("TypeException", e);
			} 
			catch (PermissionException e) 
			{
				logger.warn("PermissionException", e);
			} 
			catch (IdInvalidException e) 
			{
				logger.warn("IdInvalidException", e);
			} 
			catch (InconsistentException e) 
			{
				logger.warn("InconsistentException", e);
			} 
			catch (ServerOverloadException e) 
			{
				logger.warn("ServerOverloadException", e);
			}
			catch (IdUsedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUsedException ", e);
			}
			catch (OverQuotaException e)
			{
				// TODO Auto-generated catch block
				logger.warn("OverQuotaException ", e);
			}
			
		}
		else if(user_action.equals("cancel"))
		{
			if(pipe != null)
			{
				ResourceToolAction action = pipe.getAction();
				if(action == null)
				{
					
				}
				else 
				{
					if(action instanceof InteractionAction)
					{
						InteractionAction iAction = (InteractionAction) action;
						iAction.cancelAction(null, pipe.getInitializationId());
					}
				}
			}
			state.setAttribute(STATE_MODE, MODE_LIST);
		}
	}
	
	public void doDispatchAction(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		
		// get the parameter-parser
		ParameterParser params = data.getParameters();
		
		String action_string = params.getString("action");
		String selectedItemId = params.getString("selectedItemId");
		
		String[] parts = action_string.split(ResourceToolAction.ACTION_DELIMITER);
		String typeId = parts[0];
		String actionId = parts[1];
		
		// ResourceType type = getResourceType(selectedItemId, state);
		ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
		if(registry == null)
		{
			registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
		}
		ResourceType type = registry.getType(typeId); 
		
		Reference reference = EntityManager.newReference(ContentHostingService.getReference(selectedItemId));
		
		ResourceToolAction action = type.getAction(actionId);
		if(action == null)
		{
			
		}
		else if(action instanceof InteractionAction)
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();
			// toolSession.setAttribute(ResourceToolAction.ACTION_ID, actionId);
			// toolSession.setAttribute(ResourceToolAction.RESOURCE_TYPE, typeId);
			
			state.setAttribute(STATE_CREATE_WIZARD_COLLECTION_ID, selectedItemId);
			
			ContentEntity entity = (ContentEntity) reference.getEntity();
			InteractionAction iAction = (InteractionAction) action;
			String intitializationId = iAction.initializeAction(reference);
			
			ResourceToolActionPipe pipe = registry.newPipe(intitializationId, action);
			pipe.setContentEntity(entity);
			pipe.setHelperId(iAction.getHelperId());
			
			toolSession.setAttribute(ResourceToolAction.ACTION_PIPE, pipe);

			ResourceProperties props = entity.getProperties();

			List propKeys = iAction.getRequiredPropertyKeys();
			if(propKeys != null)
			{
				Iterator it = propKeys.iterator();
				while(it.hasNext())
				{
					String key = (String) it.next();
					Object value = props.get(key);
					if(value == null)
					{
						// do nothing
					}
					else if(value instanceof String)
					{
						pipe.setResourceProperty(key, (String) value);
					}
					else if(value instanceof List)
					{
						pipe.setResourceProperty(key, (List) value);
					}
				}
			}
			
			if(entity.isResource())
			{
				try 
				{
					pipe.setMimeType(((ContentResource) entity).getContentType());
					pipe.setContent(((ContentResource) entity).getContent());
				} 
				catch (ServerOverloadException e) 
				{
					logger.warn(this + ".doDispatchAction ServerOverloadException", e);
				}
			}

			startHelper(data.getRequest(), iAction.getHelperId());
		}
		else if(action instanceof ServiceLevelAction)
		{
			ServiceLevelAction sAction = (ServiceLevelAction) action;
			sAction.initializeAction(reference);
			switch(sAction.getActionType())
			{
				case COPY:
					state.setAttribute(STATE_ITEM_TO_BE_COPIED, selectedItemId);
					break;
				case DUPLICATE:
					duplicateItem(state, selectedItemId, ContentHostingService.getContainingCollectionId(selectedItemId));
					break;
				case DELETE:
					deleteItem(state, selectedItemId);
					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// need new context
						state.setAttribute (STATE_MODE, MODE_DELETE_FINISH);
					}
					break;
				case MOVE:
					state.setAttribute(STATE_ITEM_TO_BE_MOVED, selectedItemId);
					break;
				case VIEW_METADATA:
					break;
				case REVISE_METADATA:
					state.setAttribute(STATE_REVISE_PROPERTIES_ENTITY_ID, selectedItemId);
					state.setAttribute(STATE_REVISE_PROPERTIES_ACTION, action);
					state.setAttribute (STATE_MODE, MODE_REVISE_METADATA);
					break;
				case CUSTOM_TOOL_ACTION:
					// do nothing
					break;
				case NEW_UPLOAD:
					break;
				case NEW_FOLDER:
					break;
				case CREATE:
					break;
				case REVISE_CONTENT:
					break;
				case REPLACE_CONTENT:
					break;
				case PASTE_MOVED:
					pasteItem(state, selectedItemId);
					break;
				case PASTE_COPIED:
					pasteItem(state, selectedItemId);
					break;
				default:
					break;
			}
			// not quite right for actions involving user interaction in Resources tool.
			// For example, with delete, this should be after the confirmation and actual deletion
			// Need mechanism to remember to do it later
			sAction.finalizeAction(reference);
			
		}
	}
	
	/**
	 * @param state
	 */
	protected void pasteItem(SessionState state, String collectionId)
	{
		String item_to_be_moved = (String) state.getAttribute(STATE_ITEM_TO_BE_MOVED);
		String item_to_be_copied = (String) state.getAttribute(STATE_ITEM_TO_BE_COPIED);
	
		if(item_to_be_moved != null)
		{
			try
			{
				// paste moved item into collection 
				ContentHostingService.moveIntoFolder(item_to_be_moved, collectionId);
				// TODO expand collection
				
				// remove the state attribute
				state.removeAttribute(STATE_ITEM_TO_BE_MOVED);
			}
			catch (PermissionException e)
			{
				
				//addAlert(state, trb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}
			catch (InUseException e)
			{
				//addAlert(state, rb.getString("someone") + " " + originalDisplayName);
			}
			catch (TypeException e)
			{
				//addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
			}
			catch (InconsistentException e)
			{
				//addAlert(state, rb.getString("recursive") + " " + itemId);
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}	// try-catch
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doMoveitems ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		else if(item_to_be_copied != null)
		{
			try
			{
				ContentHostingService.copyIntoFolder(item_to_be_copied, collectionId);
				// if no errors
				// TODO expand collection
				
				// remove the state attribute
				state.removeAttribute(STATE_ITEM_TO_BE_COPIED);
			}
			catch (PermissionException e)
			{
				// TODO Auto-generated catch block
				logger.warn("PermissionException ", e);
			}
			catch (IdUnusedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUnusedException ", e);
			}
			catch (IdLengthException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdLengthException ", e);
			}
			catch (IdUniquenessException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUniquenessException ", e);
			}
			catch (TypeException e)
			{
				// TODO Auto-generated catch block
				logger.warn("TypeException ", e);
			}
			catch (InUseException e)
			{
				// TODO Auto-generated catch block
				logger.warn("InUseException ", e);
			}
			catch (OverQuotaException e)
			{
				// TODO Auto-generated catch block
				logger.warn("OverQuotaException ", e);
			}
			catch (IdUsedException e)
			{
				// TODO Auto-generated catch block
				logger.warn("IdUsedException ", e);
			}
			catch (ServerOverloadException e)
			{
				// TODO Auto-generated catch block
				logger.warn("ServerOverloadException ", e);
			}
			catch (InconsistentException e)
			{
				// TODO Auto-generated catch block
				logger.warn("InconsistentException ", e);
			}
			// paste copied item into collection 
			// duplicateItem(state, item_to_be_copied, collectionId);
		}
		else
		{
			// report error?
		}
	}

	protected ResourceType getResourceType(String id, SessionState state)
	{
		ResourceType type = null;
		
		boolean isCollection = false;
		String typeId = TYPE_UPLOAD;
		ResourceProperties properties;
		try 
		{
			properties = ContentHostingService.getProperties(id);
			isCollection = properties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
			if(isCollection)
			{
				typeId = "folder";
			}
			else 
			{
				ContentResource resource = ContentHostingService.getResource(id);
				String mimetype = resource.getContentType();
				if(TYPE_HTML.equals(mimetype) || TYPE_URL.equals(mimetype) || TYPE_TEXT.equals(mimetype))
				{
					typeId = mimetype;
				}
			}
			
			ResourceTypeRegistry registry = (ResourceTypeRegistry) state.getAttribute(STATE_RESOURCES_TYPE_REGISTRY);
			if(registry == null)
			{
				registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
				state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, registry);
			}
			type = registry.getType(typeId); 
		} 
		catch (PermissionException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (IdUnusedException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		catch (EntityPropertyNotDefinedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (EntityPropertyTypeException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (TypeException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return type;

	}
	
	/**
	 * Build the context to establish a custom-ordering of resources/folders within a folder.
	 */
	public String buildReorderContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) 
	{
		context.put("tlang",rb);
		
		String folderId = (String) state.getAttribute(STATE_REORDER_FOLDER);
		context.put("folderId", folderId);
		
		// save expanded folder lists
		SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		String need_to_expand_all = (String) state.getAttribute(STATE_NEED_TO_EXPAND_ALL);

		// create temporary expanded folder lists for this invocation of getListView
		Map tempExpandedFolderSortMap = new Hashtable();
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, tempExpandedFolderSortMap);
		SortedSet tempExpandedCollections = new TreeSet();
		tempExpandedCollections.add(folderId);
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, tempExpandedCollections);

		Set highlightedItems = new TreeSet();
		List all_roots = new Vector();
		List this_site = new Vector();

		List members = getListView(folderId, highlightedItems, (ChefBrowseItem) null, true, state);

		// restore expanded folder lists 
		expandedCollections.addAll(tempExpandedCollections);
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		expandedFolderSortMap.putAll(tempExpandedFolderSortMap);
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);

		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		boolean atHome = false;

		context.put("atHome", Boolean.toString(atHome));

		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		
		String sortBy = (String) state.getAttribute(STATE_REORDER_SORT_BY);
		context.put("sortBy", sortBy);
		String sortAsc = (String) state.getAttribute(STATE_REORDER_SORT_ASC);
		context.put("sortAsc", sortAsc);
		// Comparator comparator = (Comparator) state.getAttribute(STATE_REORDER_SORT);

		String rootTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (folderId.equals(homeCollectionId))
		{
			atHome = true;
			String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
			rootTitle = siteTitle + " " + rb.getString("gen.reso");
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				rootTitle = ContentHostingService.getCollection(folderId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
		}

		if(members != null && members.size() > 0)
		{
			ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
			root.addMembers(members);
			root.setName(rootTitle);
			this_site.add(root);
			all_roots.add(root);
		}
		context.put ("this_site", this_site);
		
		return TEMPLATE_REORDER;
	}

	/**
	* Build the context for the old list view, which does not use the resource type registry
	*/
	public String buildChefListContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);

		context.put("expandedCollections", state.getAttribute(STATE_EXPANDED_COLLECTIONS));

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);

		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		context.put ("service", contentService);

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%

		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		if (dropboxMode)
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		else
		{
			//context.put("dropboxMode", Boolean.FALSE);
		}

		// make sure the channedId is set
		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
			context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				context.put("collectionDisplayName", contentService.getCollection(collectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
		}
		if(!inMyWorkspace && !dropboxMode && atHome && SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			context.put("showPermissions", Boolean.TRUE.toString());
			//buildListMenu(portlet, context, data, state);
		}

		context.put("atHome", Boolean.toString(atHome));

		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		boolean showRemoveAction = false;
		boolean showMoveAction = false;
		boolean showCopyAction = false;

		Set highlightedItems = new TreeSet();

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				logger.warn(this + "IdUnusedException: " + collectionId);
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					logger.warn(this + "IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					logger.warn(this + "IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					logger.warn(this + "InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				logger.warn(this + "TypeException.");
				throw ex;				
			}
			catch(PermissionException ex)
			{
				logger.warn(this + "PermissionException.");
				throw ex;
			}
			
			String copyFlag = (String) state.getAttribute (STATE_COPY_FLAG);
			if (copyFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("copyFlag", copyFlag);
				List copiedItems = (List) state.getAttribute(STATE_COPIED_IDS);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				highlightedItems.addAll(copiedItems);
				// context.put("copiedItems", copiedItems);
			}

			String moveFlag = (String) state.getAttribute (STATE_MOVE_FLAG);
			if (moveFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("moveFlag", moveFlag);
				List movedItems = (List) state.getAttribute(STATE_MOVED_IDS);
				highlightedItems.addAll(movedItems);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				// context.put("movedItems", movedItems);
			}

			SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			
			//ContentCollection coll = contentService.getCollection(collectionId);
			expandedCollections.add(collectionId);

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			List all_roots = new Vector();
			List this_site = new Vector();
			
			List members = getListView(collectionId, highlightedItems, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);

			// List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
			if(members != null && members.size() > 0)
			{
				ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
				showRemoveAction = showRemoveAction || root.hasDeletableChildren();
				showMoveAction = showMoveAction || root.hasDeletableChildren();
				showCopyAction = showCopyAction || root.hasCopyableChildren();

				if(atHome && dropboxMode)
				{
					root.setName(siteTitle + " " + rb.getString("gen.drop"));
				}
				else if(atHome)
				{
					root.setName(siteTitle + " " + rb.getString("gen.reso"));
				}
				context.put("site", root);
				root.addMembers(members);
				this_site.add(root);
				all_roots.add(root);
			}
			context.put ("this_site", this_site);

			boolean show_all_sites = false;
			//List other_sites = new Vector();

			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}

			if(atHome && show_all_sites)
			{
				state.setAttribute(STATE_HIGHLIGHTED_ITEMS, highlightedItems);
				// TODO: see call to prepPage below.  That also calls readAllResources.  Are both calls necessary?
				//other_sites.addAll(readAllResources(state));
				//all_roots.addAll(other_sites);

				List messages = prepPage(state);
				context.put("other_sites", messages);
				all_roots.addAll(messages);

				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				/*
				boolean goFPButton = state.getAttribute(STATE_FIRST_PAGE_EXISTS) != null;
				context.put("goFPButton", Boolean.toString(goFPButton));
				boolean goLPButton = state.getAttribute(STATE_LAST_PAGE_EXISTS) != null;
				context.put("goLPButton", Boolean.toString(goLPButton));
				*/

				context.put("pagesize", state.getAttribute(STATE_PAGESIZE));
				// context.put("pagesizes", PAGESIZES);


			}

			// context.put ("other_sites", other_sites);
			state.setAttribute(STATE_COLLECTION_ROOTS, all_roots);
			// context.put ("root", root);

			if(state.getAttribute(STATE_PASTE_ALLOWED_FLAG) != null)
			{
				context.put("paste_place_showing", state.getAttribute(STATE_PASTE_ALLOWED_FLAG));
			}

			if(showRemoveAction)
			{
				context.put("showRemoveAction", Boolean.TRUE.toString());
			}

			if(showMoveAction)
			{
				context.put("showMoveAction", Boolean.TRUE.toString());
			}

			if(showCopyAction)
			{
				context.put("showCopyAction", Boolean.TRUE.toString());
			}

		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			logger.warn(this + "TypeException.");
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(PermissionException e)
		{
			addAlert(state, rb.getString("notpermis1"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}

		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentService.newResourceProperties ());

		try
		{
			// TODO: why 'site' here?
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
			logger.debug(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		// pick the "show" template based on the standard template name
		// String template = (String) getContext(data).get("template");

		return TEMPLATE_LIST;

	}	// buildListContext

	/**
	* Build the context for the new list view, which uses the resources type registry
	*/
	public String buildListContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);

		context.put("expandedCollections", state.getAttribute(STATE_EXPANDED_COLLECTIONS));

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);

		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		context.put ("service", contentService);

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%

		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));
		if (dropboxMode)
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		else
		{
			//context.put("dropboxMode", Boolean.FALSE);
		}

		// make sure the channedId is set
		String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
			context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		else
		{
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				context.put("collectionDisplayName", contentService.getCollection(collectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
		}
		if(!inMyWorkspace && !dropboxMode && atHome && SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			context.put("showPermissions", Boolean.TRUE.toString());
			//buildListMenu(portlet, context, data, state);
		}

		context.put("atHome", Boolean.toString(atHome));

		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		boolean showRemoveAction = false;
		boolean showMoveAction = false;
		boolean showCopyAction = false;

		Set highlightedItems = new TreeSet();

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				logger.warn(this + "IdUnusedException: " + collectionId);
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					logger.warn(this + "IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					logger.warn(this + "IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					logger.warn(this + "InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				logger.warn(this + "TypeException.");
				throw ex;				
			}
			catch(PermissionException ex)
			{
				logger.warn(this + "PermissionException.");
				throw ex;
			}
			
			String copyFlag = (String) state.getAttribute (STATE_COPY_FLAG);
			if (copyFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("copyFlag", copyFlag);
				List copiedItems = (List) state.getAttribute(STATE_COPIED_IDS);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				highlightedItems.addAll(copiedItems);
				// context.put("copiedItems", copiedItems);
			}

			String moveFlag = (String) state.getAttribute (STATE_MOVE_FLAG);
			if (moveFlag.equals (Boolean.TRUE.toString()))
			{
				context.put ("moveFlag", moveFlag);
				List movedItems = (List) state.getAttribute(STATE_MOVED_IDS);
				highlightedItems.addAll(movedItems);
				// context.put ("copiedItem", state.getAttribute (STATE_COPIED_ID));
				// context.put("movedItems", movedItems);
			}

			SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			
			//ContentCollection coll = contentService.getCollection(collectionId);
			expandedCollections.add(collectionId);

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			//List all_roots = new Vector();
			List this_site = new Vector();
			
			List members = getListView(collectionId, highlightedItems, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);

			// List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
			if(members != null && members.size() > 0)
			{
				ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
				showRemoveAction = showRemoveAction || root.hasDeletableChildren();
				showMoveAction = showMoveAction || root.hasDeletableChildren();
				showCopyAction = showCopyAction || root.hasCopyableChildren();

				if(atHome && dropboxMode)
				{
					root.setName(siteTitle + " " + rb.getString("gen.drop"));
				}
				else if(atHome)
				{
					root.setName(siteTitle + " " + rb.getString("gen.reso"));
				}
				context.put("site", root);
				root.addMembers(members);
				this_site.add(root);
				//all_roots.add(root);
			}
			context.put ("this_site", this_site);

			boolean show_all_sites = false;
			//List other_sites = new Vector();

			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}

			if(atHome && show_all_sites)
			{
				state.setAttribute(STATE_HIGHLIGHTED_ITEMS, highlightedItems);
				// TODO: see call to prepPage below.  That also calls readAllResources.  Are both calls necessary?
				//other_sites.addAll(readAllResources(state));
				//all_roots.addAll(other_sites);

				List messages = prepPage(state);
				context.put("other_sites", messages);

				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				/*
				boolean goFPButton = state.getAttribute(STATE_FIRST_PAGE_EXISTS) != null;
				context.put("goFPButton", Boolean.toString(goFPButton));
				boolean goLPButton = state.getAttribute(STATE_LAST_PAGE_EXISTS) != null;
				context.put("goLPButton", Boolean.toString(goLPButton));
				*/

				context.put("pagesize", state.getAttribute(STATE_PAGESIZE));
				// context.put("pagesizes", PAGESIZES);


			}

			// context.put ("other_sites", other_sites);
			//state.setAttribute(STATE_COLLECTION_ROOTS, all_roots);
			// context.put ("root", root);

			if(state.getAttribute(STATE_PASTE_ALLOWED_FLAG) != null)
			{
				context.put("paste_place_showing", state.getAttribute(STATE_PASTE_ALLOWED_FLAG));
			}

			if(showRemoveAction)
			{
				context.put("showRemoveAction", Boolean.TRUE.toString());
			}

			if(showMoveAction)
			{
				context.put("showMoveAction", Boolean.TRUE.toString());
			}

			if(showCopyAction)
			{
				context.put("showCopyAction", Boolean.TRUE.toString());
			}

		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			logger.warn(this + "TypeException.");
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(PermissionException e)
		{
			addAlert(state, rb.getString("notpermis1"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}

		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentService.newResourceProperties ());

		try
		{
			// TODO: why 'site' here?
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
			logger.debug(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		// pick the "show" template based on the standard template name
		// String template = (String) getContext(data).get("template");

		return TEMPLATE_LIST;

	}	// buildListContext

	/**
	* Build the context for the helper view
	*/
	public static String buildHelperContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		if(state.getAttribute(STATE_INITIALIZED) == null)
		{
			initStateAttributes(state, portlet);
			if(state.getAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER) != null)
			{
				state.removeAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER);
			}
		}
		String mode = (String) state.getAttribute(STATE_MODE);
		if(state.getAttribute(STATE_MODE_RESOURCES) == null && MODE_HELPER.equals(mode))
		{
			state.setAttribute(ResourcesAction.STATE_MODE_RESOURCES, ResourcesAction.MODE_HELPER);
		}

		Set selectedItems = (Set) state.getAttribute(STATE_LIST_SELECTIONS);
		if(selectedItems == null)
		{
			selectedItems = new TreeSet();
			state.setAttribute(STATE_LIST_SELECTIONS, selectedItems);
		}
		context.put("selectedItems", selectedItems);

		String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
		boolean need_to_push = false;

		if(MODE_ATTACHMENT_SELECT.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_SELECT_INIT;
		}
		else if(MODE_ATTACHMENT_CREATE.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_CREATE_INIT;
		}
		else if(MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_NEW_ITEM_INIT;
		}
		else if(MODE_ATTACHMENT_EDIT_ITEM.equals(helper_mode))
		{
			need_to_push = true;
			helper_mode = MODE_ATTACHMENT_EDIT_ITEM_INIT;
		}

		Map current_stack_frame = null;

		if(need_to_push)
		{
			current_stack_frame = pushOnStack(state);
			current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);

			state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());
			state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);

			if(MODE_ATTACHMENT_EDIT_ITEM_INIT.equals(helper_mode))
			{
				String attachmentId = (String) state.getAttribute(STATE_EDIT_ID);
				if(attachmentId != null)
				{
					current_stack_frame.put(STATE_STACK_EDIT_ID, attachmentId);
					String collectionId = ContentHostingService.getContainingCollectionId(attachmentId);
					current_stack_frame.put(STATE_STACK_EDIT_COLLECTION_ID, collectionId);

					ChefEditItem item = getEditItem(attachmentId, collectionId, data);

					if (state.getAttribute(STATE_MESSAGE) == null)
					{
						// got resource and sucessfully populated item with values
						state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
						current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
					}
				}
			}
			else
			{
				List attachments = (List) state.getAttribute(STATE_ATTACHMENTS);
				if(attachments == null)
				{
					attachments = EntityManager.newReferenceList();
				}

				List attached = new Vector();

				Iterator it = attachments.iterator();
				while(it.hasNext())
				{
					try
					{
						Reference ref = (Reference) it.next();
						String itemId = ref.getId();
						ResourceProperties properties = ref.getProperties();
						String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						String containerId = ref.getContainer();
						String accessUrl = ContentHostingService.getUrl(itemId);
						String contentType = properties.getProperty(ResourceProperties.PROP_CONTENT_TYPE);

						AttachItem item = new AttachItem(itemId, displayName, containerId, accessUrl);
						item.setContentType(contentType);
						attached.add(item);
					}
					catch(Exception ignore) {}
				}
				current_stack_frame.put(STATE_HELPER_NEW_ITEMS, attached);
			}
		}
		else
		{
			current_stack_frame = peekAtStack(state);
			if(current_stack_frame.get(STATE_STACK_EDIT_INTENT) == null)
			{
				current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);
			}
		}
		if(helper_mode == null)
		{
			helper_mode = (String) current_stack_frame.get(STATE_RESOURCES_HELPER_MODE);
		}
		else
		{
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}

		String helper_title = (String) current_stack_frame.get(STATE_ATTACH_TITLE);
		if(helper_title == null)
		{
			helper_title = (String) state.getAttribute(STATE_ATTACH_TITLE);
			if(helper_title != null)
			{
				current_stack_frame.put(STATE_ATTACH_TITLE, helper_title);
			}
		}
		if(helper_title != null)
		{
			context.put("helper_title", helper_title);
		}

		String helper_instruction = (String) current_stack_frame.get(STATE_ATTACH_INSTRUCTION);
		if(helper_instruction == null)
		{
			helper_instruction = (String) state.getAttribute(STATE_ATTACH_INSTRUCTION);
			if(helper_instruction != null)
			{
				current_stack_frame.put(STATE_ATTACH_INSTRUCTION, helper_instruction);
			}
		}
		if(helper_instruction != null)
		{
			context.put("helper_instruction", helper_instruction);
		}

		String title = (String) current_stack_frame.get(STATE_STACK_EDIT_ITEM_TITLE);
		if(title == null)
		{
			title = (String) state.getAttribute(STATE_ATTACH_TEXT);
			if(title != null)
			{
				current_stack_frame.put(STATE_STACK_EDIT_ITEM_TITLE, title);
			}
		}
		if(title != null && title.trim().length() > 0)
		{
			context.put("helper_subtitle", title);
		}

		String template = null;
		if(MODE_ATTACHMENT_SELECT_INIT.equals(helper_mode))
		{
			template = buildSelectAttachmentContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_CREATE_INIT.equals(helper_mode))
		{
			template = buildCreateContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
		{
			template = buildItemTypeContext(portlet, context, data, state);
		}
		else if(MODE_ATTACHMENT_EDIT_ITEM_INIT.equals(helper_mode))
		{
			template = buildEditContext(portlet, context, data, state);
		}
		return template;
	}

	public static String buildItemTypeContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		context.put("tlang",rb);

		initStateAttributes(state, portlet);
		Map current_stack_frame = peekAtStack(state);

		String mode = (String) state.getAttribute(STATE_MODE);
		if(mode == null || mode.trim().length() == 0)
		{
			mode = MODE_HELPER;
			state.setAttribute(STATE_MODE, mode);
		}
		String helper_mode = null;
		if(MODE_HELPER.equals(mode))
		{
			helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
			if(helper_mode == null || helper_mode.trim().length() == 0)
			{
				helper_mode = MODE_ATTACHMENT_NEW_ITEM;
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);
			}
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
			if(MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
			{
				context.put("attaching_this_item", Boolean.TRUE.toString());
			}
			state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());
		}
		
		String msg = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (msg != null)
		{
			context.put("itemAlertMessage", msg);
			state.removeAttribute(STATE_CREATE_MESSAGE);
		}

		context.put("max_upload_size", state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE));

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}
		context.put("collectionId", collectionId);
		
		String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
		if(itemType == null || "".equals(itemType))
		{
			itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
			if(itemType == null || "".equals(itemType))
			{
				itemType = TYPE_UPLOAD;
			}
			current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
		}

		context.put("itemType", itemType);
		
		Integer numberOfItems = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(numberOfItems == null)
		{
			numberOfItems = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, numberOfItems);
		}
		if(numberOfItems == null)
		{
			numberOfItems = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, numberOfItems);
		}
		context.put("numberOfItems", numberOfItems);
		context.put("max_number", new Integer(1));
		
		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		// TODO: does this method filter groups for this subcollection??
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			Site site;
			try 
			{
				site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			} 
			catch (IdUnusedException e1) 
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String encoding = data.getRequest().getCharacterEncoding();
			List inherited_access_groups = new Vector();
			
			AccessMode inherited_access = AccessMode.INHERITED;
			try 
			{
				ContentCollection parent = ContentHostingService.getCollection(collectionId);
				inherited_access = parent.getInheritedAccess();
				inherited_access_groups.addAll(parent.getInheritedGroups());
			} 
			catch (IdUnusedException e) 
			{
			} 
			catch (TypeException e) 
			{
			} 
			catch (PermissionException e) 
			{
			}
			
			boolean isInDropbox = ContentHostingService.isInDropbox(collectionId);

			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}

			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);

		}
		context.put("new_items", new_items);
		current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);
		context.put("TYPE_HTML", TYPE_HTML);
		context.put("TYPE_TEXT", TYPE_TEXT);
		context.put("TYPE_URL", TYPE_URL);

		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		return TEMPLATE_ITEMTYPE;
	}

	/**
	 * Access the top item on the suspended-operations stack
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The top item on the stack, or null if the stack is empty.
	 */
	private static Map peekAtStack(SessionState state)
	{
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(! operations_stack.isEmpty())
		{
			current_stack_frame = (Map) operations_stack.peek();
		}
		return current_stack_frame;

	}

	/**
	 * Returns true if the suspended operations stack contains no elements.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return true if the suspended operations stack contains no elements
	 */
	private static boolean isStackEmpty(SessionState state)
	{
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		return operations_stack.isEmpty();
	}

	/**
	 * Push an item of the suspended-operations stack.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The new item that has just been added to the stack, or null if depth limit is exceeded.
	 */
	private static Map pushOnStack(SessionState state)
	{
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(operations_stack.size() < MAXIMUM_SUSPENDED_OPERATIONS_STACK_DEPTH)
		{
			current_stack_frame = (Map) operations_stack.push(new Hashtable());
		}
		Object helper_mode = state.getAttribute(STATE_RESOURCES_HELPER_MODE);
		if(helper_mode != null)
		{
			current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}
		return current_stack_frame;

	}

	/**
	 * Remove and return the top item from the suspended-operations stack.
	 * @param state The current session state, including the STATE_SUSPENDED_OPERATIONS_STACK attribute.
	 * @return The item that has just been removed from the stack, or null if the stack was empty.
	 */
	private static Map popFromStack(SessionState state)
	{
		Map current_stack_frame = null;
		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);
		if(operations_stack == null)
		{
			operations_stack = new Stack();
			state.setAttribute(STATE_SUSPENDED_OPERATIONS_STACK, operations_stack);
		}
		if(! operations_stack.isEmpty())
		{
			current_stack_frame = (Map) operations_stack.pop();
			if(operations_stack.isEmpty())
			{
				String canceled = (String) current_stack_frame.get(STATE_HELPER_CANCELED_BY_USER);
				if(canceled != null)
				{
					state.setAttribute(STATE_HELPER_CANCELED_BY_USER, canceled);
				}
			}
		}
		return current_stack_frame;

	}

	private static void resetCurrentMode(SessionState state)
	{
		String mode = (String) state.getAttribute(STATE_MODE);
		if(isStackEmpty(state))
		{
			if(MODE_HELPER.equals(mode))
			{
				cleanupState(state);
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_DONE);
			}
			else
			{
				state.setAttribute(STATE_MODE, MODE_LIST);
				state.removeAttribute(STATE_RESOURCES_HELPER_MODE);
			}
			return;
		}
		Map current_stack_frame = peekAtStack(state);
		String helper_mode = (String) current_stack_frame.get(STATE_RESOURCES_HELPER_MODE);
		if(helper_mode != null)
		{
			state.setAttribute(STATE_RESOURCES_HELPER_MODE, helper_mode);
		}

	}

	/**
	* Build the context for selecting attachments
	*/
	public static String buildSelectAttachmentContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);

		initStateAttributes(state, portlet);

		Map current_stack_frame = peekAtStack(state);
		if(current_stack_frame == null)
		{
			current_stack_frame = pushOnStack(state);
		}

		state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, ResourcesAction.class.getName());

		Set highlightedItems = new TreeSet();

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		context.put("attached", new_items);
		context.put("last", new Integer(new_items.size() - 1));

		Integer max_cardinality = (Integer) current_stack_frame.get(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
			if(max_cardinality == null)
			{
				max_cardinality = CARDINALITY_MULTIPLE;
			}
			current_stack_frame.put(STATE_ATTACH_CARDINALITY, max_cardinality);
		}
		context.put("max_cardinality", max_cardinality);

		if(new_items.size() >= max_cardinality.intValue())
		{
			context.put("disable_attach_links", Boolean.TRUE.toString());
		}

		if(state.getAttribute(STATE_HELPER_CHANGED) != null)
		{
			context.put("list_has_changed", "true");
		}

		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);

		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		// context.put ("service", contentService);

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		// context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		boolean atHome = false;

		// %%STATE_MODE_RESOURCES%%
		boolean dropboxMode = RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES));

		// make sure the channedId is set
		String collectionId = (String) state.getAttribute(STATE_ATTACH_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		}

		context.put ("collectionId", collectionId);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

		String siteTitle = (String) state.getAttribute (STATE_SITE_TITLE);
		if (collectionId.equals(homeCollectionId))
		{
			atHome = true;
			//context.put ("collectionDisplayName", state.getAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME));
		}
		else
		{
			/*
			// should be not PermissionException thrown at this time, when the user can successfully navigate to this collection
			try
			{
				context.put("collectionDisplayName", contentService.getCollection(collectionId).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			}
			catch (IdUnusedException e){}
			catch (TypeException e) {}
			catch (PermissionException e) {}
			*/
		}

		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);

		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		context.put ("currentSortedBy", sortedBy);
		context.put ("currentSortAsc", sortedAsc);
		context.put("TRUE", Boolean.TRUE.toString());

		// String current_user_id = UserDirectoryService.getCurrentUser().getId();

		try
		{
			try
			{
				contentService.checkCollection (collectionId);
				context.put ("collectionFlag", Boolean.TRUE.toString());
			}
			catch(IdUnusedException ex)
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("ResourcesAction.buildSelectAttachment (static) : IdUnusedException: " + collectionId);
				}
				try
				{
					ContentCollectionEdit coll = contentService.addCollection(collectionId);
					contentService.commitCollection(coll);
				}
				catch(IdUsedException inner)
				{
					// how can this happen??
					logger.warn("ResourcesAction.buildSelectAttachment (static) : IdUsedException: " + collectionId);
					throw ex;
				}
				catch(IdInvalidException inner)
				{
					logger.warn("ResourcesAction.buildSelectAttachment (static) : IdInvalidException: " + collectionId);
					// what now?
					throw ex;
				}
				catch(InconsistentException inner)
				{
					logger.warn("ResourcesAction.buildSelectAttachment (static) : InconsistentException: " + collectionId);
					// what now?
					throw ex;
				}
			}
			catch(TypeException ex)
			{
				logger.warn("ResourcesAction.buildSelectAttachment (static) : TypeException.");
				throw ex;				
			}
			catch(PermissionException ex)
			{
				logger.warn("ResourcesAction.buildSelectAttachment (static) : PermissionException.");
				throw ex;
			}
		
			SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			expandedCollections.add(collectionId);

			state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);

			List this_site = new Vector();
			User[] submitters = (User[]) state.getAttribute(STATE_ATTACH_SHOW_DROPBOXES);
			if(submitters != null)
			{
				String dropboxId = ContentHostingService.getDropboxCollection();
				if(dropboxId == null)
				{
					ContentHostingService.createDropboxCollection();
					dropboxId = ContentHostingService.getDropboxCollection();
				}

				if(dropboxId == null)
				{
					// do nothing
				}
				else if(ContentHostingService.isDropboxMaintainer())
				{
					for(int i = 0; i < submitters.length; i++)
					{
						User submitter = submitters[i];
						String dbId = dropboxId + StringUtil.trimToZero(submitter.getId()) + "/";
						try
						{
							ContentCollection db = ContentHostingService.getCollection(dbId);
							expandedCollections.add(dbId);
							List dbox = getListView(dbId, highlightedItems, (ChefBrowseItem) null, false, state); 
							// getBrowseItems(dbId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
							if(dbox != null && dbox.size() > 0)
							{
								ChefBrowseItem root = (ChefBrowseItem) dbox.remove(0);
								// context.put("site", root);
								root.setName(submitter.getDisplayName() + " " + rb.getString("gen.drop"));
								root.addMembers(dbox);
								this_site.add(root);
							}
						}
						catch(IdUnusedException e)
						{
							// ignore a user's dropbox if it's not defined
						}
					}
				}
				else
				{
					try
					{
						ContentCollection db = ContentHostingService.getCollection(dropboxId);
						expandedCollections.add(dropboxId);
						List dbox = getListView(dropboxId, highlightedItems, (ChefBrowseItem) null, false, state); 
						// List dbox = getBrowseItems(dropboxId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
						if(dbox != null && dbox.size() > 0)
						{
							ChefBrowseItem root = (ChefBrowseItem) dbox.remove(0);
							// context.put("site", root);
							root.setName(ContentHostingService.getDropboxDisplayName());
							root.addMembers(dbox);
							this_site.add(root);
						}
					}
					catch(IdUnusedException e)
					{
						// if an id is unused, ignore it
					}
				}
			}
			List members = getListView(collectionId, highlightedItems, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
			// List members = getBrowseItems(collectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, navRoot.equals(homeCollectionId), state);
			if(members != null && members.size() > 0)
			{
				ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
				if(atHome && dropboxMode)
				{
					root.setName(siteTitle + " " + rb.getString("gen.drop"));
				}
				else if(atHome)
				{
					root.setName(siteTitle + " " + rb.getString("gen.reso"));
				}
				context.put("site", root);
				root.addMembers(members);
				this_site.add(root);
			}


			context.put ("this_site", this_site);

			List other_sites = new Vector();
			boolean show_all_sites = false;

			String allowed_to_see_other_sites = (String) state.getAttribute(STATE_SHOW_ALL_SITES);
			String show_other_sites = (String) state.getAttribute(STATE_SHOW_OTHER_SITES);
			context.put("show_other_sites", show_other_sites);
			if(Boolean.TRUE.toString().equals(allowed_to_see_other_sites))
			{
				context.put("allowed_to_see_other_sites", Boolean.TRUE.toString());
				show_all_sites = Boolean.TRUE.toString().equals(show_other_sites);
			}

			if(show_all_sites)
			{

				state.setAttribute(STATE_HIGHLIGHTED_ITEMS, highlightedItems);
				other_sites.addAll(readAllResources(state));

				List messages = prepPage(state);
				context.put("other_sites", messages);

				if (state.getAttribute(STATE_NUM_MESSAGES) != null)
				{
					context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());
					context.put("allMsgNumberInt", state.getAttribute(STATE_NUM_MESSAGES));
				}

				context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());

				// find the position of the message that is the top first on the page
				if ((state.getAttribute(STATE_TOP_MESSAGE_INDEX) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
				{
					int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_MESSAGE_INDEX)).intValue() + 1;
					context.put("topMsgPos", Integer.toString(topMsgPos));
					int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
					if (state.getAttribute(STATE_NUM_MESSAGES) != null)
					{
						int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
						if (btmMsgPos > allMsgNumber)
							btmMsgPos = allMsgNumber;
					}
					context.put("btmMsgPos", Integer.toString(btmMsgPos));
				}

				boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
				context.put("goPPButton", Boolean.toString(goPPButton));
				boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
				context.put("goNPButton", Boolean.toString(goNPButton));

				/*
				boolean goFPButton = state.getAttribute(STATE_FIRST_PAGE_EXISTS) != null;
				context.put("goFPButton", Boolean.toString(goFPButton));
				boolean goLPButton = state.getAttribute(STATE_LAST_PAGE_EXISTS) != null;
				context.put("goLPButton", Boolean.toString(goLPButton));
				*/

				context.put("pagesize", state.getAttribute(STATE_PAGESIZE));
				// context.put("pagesizes", PAGESIZES);




				// List other_sites = new Vector();
				/*
				 * NOTE: This does not (and should not) get all sites for admin.
				 *       Getting all sites for admin is too big a request and
				 *       would result in too big a display to render in html.
				 */
				/*
				Map othersites = ContentHostingService.getCollectionMap();
				Iterator siteIt = othersites.keySet().iterator();
				while(siteIt.hasNext())
				{
					String displayName = (String) siteIt.next();
					String collId = (String) othersites.get(displayName);
					if(! collectionId.equals(collId))
					{
						members = getBrowseItems(collId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
						if(members != null && members.size() > 0)
						{
							ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
							root.addMembers(members);
							root.setName(displayName);
							other_sites.add(root);
						}
					}
				}

				context.put ("other_sites", other_sites);
				*/
			}

			// context.put ("root", root);
			context.put("expandedCollections", expandedCollections);
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("cannotfind"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(TypeException e)
		{
			// logger.warn(this + "TypeException.");
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}
		catch(PermissionException e)
		{
			addAlert(state, rb.getString("notpermis1"));
			context.put ("collectionFlag", Boolean.FALSE.toString());
		}

		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", contentService.newResourceProperties ());

		try
		{
			// TODO: why 'site' here?
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			context.put("siteTitle", site.getTitle());
		}
		catch (IdUnusedException e)
		{
			// logger.warn(this + e.toString());
		}

		context.put("expandallflag", state.getAttribute(STATE_EXPAND_ALL_FLAG));
		state.removeAttribute(STATE_NEED_TO_EXPAND_ALL);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		// justDelivered(state);

		// pick the template based on whether client wants links or copies
		String template = TEMPLATE_SELECT;
		Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
		if(attach_links == null)
		{
			attach_links = state.getAttribute(STATE_ATTACH_LINKS);
			if(attach_links != null)
			{
				current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
			}
		}
		if(attach_links == null)
		{
			// user wants copies in hidden attachments area
			template = TEMPLATE_ATTACH;
		}

		return template;

	}	// buildSelectAttachmentContext

	/**
	* Expand all the collection resources and put in EXPANDED_COLLECTIONS attribute.
	*/
	public void doList ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (STATE_MODE, MODE_LIST);

	}	// doList


	/**
	* Build the context for add display
	*/
	public String buildWebdavContext (	VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		boolean inMyWorkspace = SiteService.isUserSite(ToolManager.getCurrentPlacement().getContext());
		context.put("inMyWorkspace", Boolean.toString(inMyWorkspace));

		context.put("server_url", ServerConfigurationService.getServerUrl());
		context.put("site_id", ToolManager.getCurrentPlacement().getContext());
		context.put("site_title", state.getAttribute(STATE_SITE_TITLE));
		context.put("user_id", UserDirectoryService.getCurrentUser().getEid());
		if (ContentHostingService.isShortRefs())
		{
			// with short refs, this is prettier
			context.put ("dav_group", "/dav/");
			context.put ("dav_user", "/dav/~");
		}
		else
		{
			context.put ("dav_group", "/dav/group/");
			context.put ("dav_user", "/dav/user/");
		}

		String webdav_instructions = ServerConfigurationService.getString("webdav.instructions.url");
		context.put("webdav_instructions" ,webdav_instructions);

		// TODO: get browser id from somewhere.
		//Session session = SessionManager.getCurrentSession();
		//String browserId = session.;
		String browserID = UsageSessionService.getSession().getBrowserId();
		if(browserID.equals(UsageSession.WIN_IE))
		{
			context.put("isWinIEBrowser", Boolean.TRUE.toString());
		}

		return TEMPLATE_DAV;

	}	// buildWebdavContext

	/**
	* Build the context for delete confirmation page
	*/
	public String buildDeleteConfirmContext (	VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put ("collectionId", state.getAttribute (STATE_COLLECTION_ID) );

		//%%%% FIXME
		context.put ("collectionPath", state.getAttribute (STATE_COLLECTION_PATH));

		List deleteItems = (List) state.getAttribute(STATE_DELETE_ITEMS);
		List nonEmptyFolders = (List) state.getAttribute(STATE_DELETE_ITEMS_NOT_EMPTY);

		context.put ("deleteItems", deleteItems);

		Iterator it = nonEmptyFolders.iterator();
		while(it.hasNext())
		{
			ChefBrowseItem folder = (ChefBrowseItem) it.next();
			String[] args = { folder.getName() };
			addAlert(state, rb.getFormattedMessage("folder.notempty", args) + " ");
		}

		//  %%STATE_MODE_RESOURCES%%
		//not show the public option when in dropbox mode
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// not show the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("homeCollection", (String) state.getAttribute (STATE_HOME_COLLECTION_ID));
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));
		context.put ("resourceProperties", ContentHostingService.newResourceProperties ());

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_DELETE_CONFIRM;

	}	// buildDeleteConfirmContext

	/**
	* Build the context to show the list of resource properties
	*/
	public static String buildMoreContext (	VelocityPortlet portlet,
									Context context,
									RunData data,
									SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService service = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		context.put ("service", service);

		Map current_stack_frame = peekAtStack(state);

		String id = (String) current_stack_frame.get(STATE_MORE_ID);
		context.put ("id", id);
		String collectionId = (String) current_stack_frame.get(STATE_MORE_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String homeCollectionId = (String) (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		List cPath = getCollectionPath(state);
		context.put ("collectionPath", cPath);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);
		context.put("navRoot", navRoot);
		
		ChefEditItem item = getEditItem(id, collectionId, data);
		context.put("item", item);

		// for the resources of type URL or plain text, show the content also
		try
		{
			ResourceProperties properties = service.getProperties (id);
			context.put ("properties", properties);

			String isCollection = properties.getProperty (ResourceProperties.PROP_IS_COLLECTION);
			if ((isCollection != null) && isCollection.equals (Boolean.FALSE.toString()))
			{
				String copyrightAlert = properties.getProperty(properties.getNamePropCopyrightAlert());
				context.put("hasCopyrightAlert", copyrightAlert);

				String type = properties.getProperty (ResourceProperties.PROP_CONTENT_TYPE);
				if (type.equalsIgnoreCase (MIME_TYPE_DOCUMENT_PLAINTEXT) || type.equalsIgnoreCase (MIME_TYPE_DOCUMENT_HTML) || type.equalsIgnoreCase (ResourceProperties.TYPE_URL))
				{
					ContentResource moreResource = service.getResource (id);
					// read the body
					String body = "";
					byte[] content = null;
					try
					{
						content = moreResource.getContent();
						if (content != null)
						{
							body = new String(content);
						}
					}
					catch(ServerOverloadException e)
					{
						// this represents server's file system is temporarily unavailable
						// report problem to user? log problem?
					}
					context.put ("content", body);
				}	// if
			}	// if

			else
			{
				// setup for quota - ADMIN only, collection only
				if (SecurityService.isSuperUser())
				{
					try
					{
						// Getting the quota as a long validates the property
						long quota = properties.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
						context.put("hasQuota", Boolean.TRUE);
						context.put("quota", properties.getProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA));
					}
					catch (Exception any) {}
				}
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state,RESOURCE_NOT_EXIST_STRING);
			context.put("notExistFlag", new Boolean(true));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getString("typeex") + " ");
		}
		catch (PermissionException e)
		{
			addAlert(state," " + rb.getString("notpermis2") + " " + id + ". ");
		}	// try-catch

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			context.put("notExistFlag", new Boolean(false));
		}
		
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		else
		{
			context.put("dropboxMode", Boolean.FALSE);
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			context.put("preventPublicDisplay", preventPublicDisplay);
			if(preventPublicDisplay.equals(Boolean.FALSE))
			{
				// find out about pubview
				boolean pubview = ContentHostingService.isInheritingPubView(id);
				if (!pubview) pubview = ContentHostingService.isPubView(id);
				context.put("pubview", new Boolean(pubview));
			}

		}
		
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		if (state.getAttribute(COPYRIGHT_TYPES) != null)
		{
			List copyrightTypes = (List) state.getAttribute(COPYRIGHT_TYPES);
			context.put("copyrightTypes", copyrightTypes);
		}

		metadataGroupsIntoContext(state, context);

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_MORE;

	}	// buildMoreContext

	/**
	* Build the context to edit the editable list of resource properties
	*/
	public static String buildEditContext (VelocityPortlet portlet,
										Context context,
										RunData data,
										SessionState state)
	{

		context.put("tlang",rb);
		// find the ContentTypeImage service

		Map current_stack_frame = peekAtStack(state);

		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));
		context.put ("from", state.getAttribute (STATE_FROM));
		context.put ("mycopyright", (String) state.getAttribute (STATE_MY_COPYRIGHT));

		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);


		String collectionId = (String) current_stack_frame.get(STATE_STACK_EDIT_COLLECTION_ID);
		context.put ("collectionId", collectionId);
		String id = (String) current_stack_frame.get(STATE_STACK_EDIT_ID);
		if(id == null)
		{
			id = (String) state.getAttribute(STATE_EDIT_ID);
			if(id == null)
			{
				id = "";
			}
			current_stack_frame.put(STATE_STACK_EDIT_ID, id);
		}
		context.put ("id", id);
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		if(homeCollectionId == null)
		{
			homeCollectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			state.setAttribute(STATE_HOME_COLLECTION_ID, homeCollectionId);
		}
		context.put("homeCollectionId", homeCollectionId);
		List collectionPath = getCollectionPath(state);
		context.put ("collectionPath", collectionPath);

		if(homeCollectionId.equals(id))
		{
			context.put("atHome", Boolean.TRUE.toString());
		}

		String intent = (String) current_stack_frame.get(STATE_STACK_EDIT_INTENT);
		if(intent == null)
		{
			intent = INTENT_REVISE_FILE;
			current_stack_frame.put(STATE_STACK_EDIT_INTENT, intent);
		}
		context.put("intent", intent);
		context.put("REVISE", INTENT_REVISE_FILE);
		context.put("REPLACE", INTENT_REPLACE_FILE);

		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		// TODO: does this method filter groups for this subcollection??
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			context.put("theGroupsInThisSite", groups);
		}
		
		// put the item into context
		ChefEditItem item = (ChefEditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
		if(item == null)
		{
			item = getEditItem(id, collectionId, data);
			if(item == null)
			{
				// what??
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// got resource and sucessfully populated item with values
				state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
				current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
			}
		}
		
		item.setPossibleGroups(groups);

		context.put("item", item);

		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}

		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		// String template = (String) getContext(data).get("template");

		return TEMPLATE_EDIT;

	}	// buildEditContext

	/**
	* Navigate in the resource hireachy
	*/
	public static void doNavigate ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		if (state.getAttribute (STATE_SELECT_ALL_FLAG)!=null && state.getAttribute (STATE_SELECT_ALL_FLAG).equals (Boolean.TRUE.toString()))
		{
			state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
		}

		if (state.getAttribute (STATE_EXPAND_ALL_FLAG)!=null && state.getAttribute (STATE_EXPAND_ALL_FLAG).equals (Boolean.TRUE.toString()))
		{
			state.setAttribute (STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());
		}

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String collectionId = data.getParameters().getString ("collectionId");
		String navRoot = data.getParameters().getString("navRoot");
		state.setAttribute(STATE_NAVIGATION_ROOT, navRoot);

		// the exception message

		try
		{
			ContentHostingService.checkCollection(collectionId);
		}
		catch(PermissionException e)
		{
			addAlert(state, " " + rb.getString("notpermis3") + " " );
		}
		catch (IdUnusedException e)
		{
			addAlert(state, " " + rb.getString("notexist2") + " ");
		}
		catch (TypeException e)
		{
			addAlert(state," " + rb.getString("notexist2") + " ");
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			String oldCollectionId = (String) state.getAttribute(STATE_COLLECTION_ID);
			// update this folder id in the set to be event-observed
			removeObservingPattern(oldCollectionId, state);
			addObservingPattern(collectionId, state);

			state.setAttribute(STATE_COLLECTION_ID, collectionId);
			
			SortedSet currentMap = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(currentMap == null)
			{
				currentMap = new TreeSet();
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
			}
			
			Map sortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
			if(sortMap == null)
			{
				sortMap = new Hashtable();
				state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, sortMap);
			}
			
			Iterator it = currentMap.iterator();
			while(it.hasNext())
			{
				String id = (String) it.next();
				if(id.startsWith(collectionId))
				{
					it.remove();
					sortMap.remove(id);
					removeObservingPattern(id, state);
				}
			}
			
			if(!currentMap.contains(collectionId))
			{
				currentMap.add (collectionId);

				// add this folder id into the set to be event-observed
				addObservingPattern(collectionId, state);
			}
			//state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new Hashtable());
		}

	}	// doNavigate

	/**
	* Show information about WebDAV
	*/
	public void doShow_webdav ( RunData data )
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		state.setAttribute (STATE_MODE, MODE_DAV);

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

	}	// doShow_webdav

	/**
	 * initiate creation of one or more resource items (folders, file uploads, html docs, text docs, or urls)
	 * default type is folder
	 */
	public static void doCreate(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemType = params.getString("itemType");
		if(itemType == null || "".equals(itemType))
		{
			itemType = TYPE_UPLOAD;
		}

		String stackOp = params.getString("suspended-operations-stack");

		Map current_stack_frame = null;
		if(stackOp != null && stackOp.equals("peek"))
		{
			current_stack_frame = peekAtStack(state);
		}
		else
		{
			current_stack_frame = pushOnStack(state);
		}

		String encoding = data.getRequest().getCharacterEncoding();

		String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
		if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
		{
			defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
			state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);

		String collectionId = params.getString ("collectionId");
		current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);

		Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
		if(defaultRetractDate == null)
		{
			defaultRetractDate = TimeService.newTime();
			state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
		}

		List new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);

		current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);

		current_stack_frame.put(STATE_STACK_CREATE_NUMBER, new Integer(1));

		state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
		current_stack_frame.put(STATE_CREATE_MISSING_ITEM, new HashSet());

		current_stack_frame.put(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_CREATE_INIT);
		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_CREATE_INIT);

	}	// doCreate
	
	protected static List newEditItems(String collectionId, String itemtype, String encoding, String defaultCopyrightStatus, boolean preventPublicDisplay, Time defaultRetractDate, int number)
	{
		List new_items = new Vector();
		
		ContentCollection collection = null;
		AccessMode inheritedAccess = AccessMode.INHERITED;
//		Collection inheritedGroups = new Vector();
		try
		{
			collection = ContentHostingService.getCollection(collectionId);
			
			inheritedAccess = collection.getAccess();
//			inheritedGroups = collection.getGroups();
			if(AccessMode.INHERITED == inheritedAccess)
			{
				inheritedAccess = collection.getInheritedAccess();
//				inheritedGroups = collection.getInheritedGroups();
			}
		}
		catch(PermissionException e)
		{
			//alerts.add(rb.getString("notpermis4"));
			logger.warn("ResourcesAction.newEditItems() PermissionException ", e);
		} 
		catch (IdUnusedException e) 
		{
			// TODO Auto-generated catch block
			logger.warn("ResourcesAction.newEditItems() IdUnusedException ", e);
		} 
		catch (TypeException e) 
		{
			// TODO Auto-generated catch block
			logger.warn("ResourcesAction.newEditItems() TypeException ", e);
		}
		
		boolean isUserSite = false;
		String refstr = collection.getReference();
		Reference ref = EntityManager.newReference(refstr);
		String contextId = ref.getContext();
		if(contextId != null)
		{
			isUserSite = SiteService.isUserSite(contextId);
		}

		boolean pubviewset = ContentHostingService.isInheritingPubView(collectionId) || ContentHostingService.isPubView(collectionId);
		
		
		//Collection possibleGroups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		boolean isInDropbox = ContentHostingService.isInDropbox(collectionId);
		
		Collection possibleGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
		Site site = null;
		Collection site_groups = null;
		
		try 
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		} 
		catch (IdUnusedException e) 
		{
			logger.warn("resourcesAction.newEditItems() IdUnusedException ", e);
		}
		if(site != null)
		{
			site_groups = site.getGroups();
		}
		else
		{
			site_groups = new Vector();
		}
				
		Collection inherited_access_groups = collection.getGroups();
		if(inherited_access_groups == null || inherited_access_groups.isEmpty())
		{
			inherited_access_groups = collection.getInheritedGroups();
		}
		if(inherited_access_groups == null)
		{
			inherited_access_groups = new Vector();
		}

		Collection allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId); // null;
//		if(AccessMode.GROUPED == inheritedAccess)
//		{
//			allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
//		}
//		else
//		{
//			allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(ContentHostingService.getSiteCollection(site.getId()));
//		}
		if(allowedAddGroups == null)
		{
			allowedAddGroups = new Vector();
		}

		for(int i = 0; i < CREATE_MAX_ITEMS; i++)
		{
			ChefEditItem item = new ChefEditItem(itemtype);
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}
			item.setInDropbox(isInDropbox);

			if(inheritedAccess == null || AccessMode.SITE == inheritedAccess)
			{
				item.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setInheritedAccess(inheritedAccess.toString());
			}
			item.setAllSiteGroups(site_groups);
			item.setInheritedGroupRefs(inherited_access_groups);
			item.setAllowedAddGroupRefs(allowedAddGroups);
			
			item.setHidden(false);
			item.setUseReleaseDate(false);
			item.setReleaseDate(TimeService.newTime());
			item.setUseRetractDate(false);
			item.setRetractDate(defaultRetractDate);
			item.setInWorkspace(isUserSite);

			item.setCopyrightStatus(defaultCopyrightStatus);
			new_items.add(item);
			// item.setPossibleGroups(new Vector(possibleGroups));
//			if(inheritedGroups != null)
//			{
//				item.setInheritedGroups(inheritedGroups);
//			}
			
			if(preventPublicDisplay)
			{
				item.setPubviewPossible(false);
				item.setPubviewInherited(false);
				item.setPubview(false);
			}
			else
			{
				item.setPubviewPossible(true);
				item.setPubviewInherited(pubviewset);
				//item.setPubview(pubviewset);
			}

		}

		return new_items;
	}

	
	public static void addCreateContextAlert(SessionState state, String message)
	{
		String soFar = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (soFar != null)
		{
			soFar = soFar + " " + message;
		}
		else
		{
			soFar = message;
		}
		state.setAttribute(STATE_CREATE_MESSAGE, soFar);

	} // addItemTypeContextAlert

	/**
	 * initiate creation of one or more resource items (file uploads, html docs, text docs, or urls -- not folders)
	 * default type is file upload
	 */
	/**
	 * @param data
	 */
	public static void doCreateitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		Map current_stack_frame = peekAtStack(state);
		boolean pop = false;
		
		String collectionId = params.getString("collectionId");
		String itemType = params.getString("itemType");
		String flow = params.getString("flow");
		
		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		Set missing = new HashSet();
		if(flow == null || flow.equals("cancel"))
		{
			pop = true;
		}
		else if(flow.equals("updateNumber"))
		{
			captureMultipleValues(state, params, false);
			int number = params.getInt("numberOfItems");
			Integer numberOfItems = new Integer(number);
			current_stack_frame.put(ResourcesAction.STATE_STACK_CREATE_NUMBER, numberOfItems);

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());

			List items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
				if(defaultRetractDate == null)
				{
					defaultRetractDate = TimeService.newTime();
					state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
				}

				items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);

			}
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, items);
			Iterator it = items.iterator();
			while(it.hasNext())
			{
				ChefEditItem item = (ChefEditItem) it.next();
				item.clearMissing();
			}
			state.removeAttribute(STATE_MESSAGE);
		}
		else if(flow.equals("create") && TYPE_FOLDER.equals(itemType))
		{
			// Get the items
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				// Save the items
				createFolders(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);

				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && TYPE_UPLOAD.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && MIME_TYPE_DOCUMENT_HTML.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create") && MIME_TYPE_DOCUMENT_PLAINTEXT.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createFiles(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop =true;
				}
			}
		}
		else if(flow.equals("create") && TYPE_URL.equals(itemType))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts.isEmpty())
			{
				createUrls(state);
				alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
				if(alerts.isEmpty())
				{
					pop = true;
				}
			}
		}
		else if(flow.equals("create"))
		{
			captureMultipleValues(state, params, true);
			alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
			if(alerts == null)
			{
				alerts = new HashSet();
				state.setAttribute(STATE_CREATE_ALERTS, alerts);
			}
			alerts.add("Invalid item type");
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		else if(flow.equals("showOptional"))
		{
			captureMultipleValues(state, params, false);
			int twiggleNumber = params.getInt("twiggleNumber", 0);
			String metadataGroup = params.getString("metadataGroup");
			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
				if(defaultRetractDate == null)
				{
					defaultRetractDate = TimeService.newTime();
					state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
				}

				new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

			}
			if(new_items != null && new_items.size() > twiggleNumber)
			{
				ChefEditItem item = (ChefEditItem) new_items.get(twiggleNumber);
				if(item != null)
				{
					item.showMetadataGroup(metadataGroup);
				}
			}

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
			Iterator it = new_items.iterator();
			while(it.hasNext())
			{
				ChefEditItem item = (ChefEditItem) it.next();
				item.clearMissing();
			}
		}
		else if(flow.equals("hideOptional"))
		{
			captureMultipleValues(state, params, false);
			int twiggleNumber = params.getInt("twiggleNumber", 0);
			String metadataGroup = params.getString("metadataGroup");
			List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
			if(new_items == null)
			{
				String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
				if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
				{
					defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
					state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
				}

				String encoding = data.getRequest().getCharacterEncoding();

				Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
				if(defaultRetractDate == null)
				{
					defaultRetractDate = TimeService.newTime();
					state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
				}

				new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);
				current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
			}
			if(new_items != null && new_items.size() > twiggleNumber)
			{
				ChefEditItem item = (ChefEditItem) new_items.get(twiggleNumber);
				if(item != null)
				{
					item.hideMetadataGroup(metadataGroup);
				}
			}

			// clear display of error messages
			state.setAttribute(STATE_CREATE_ALERTS, new HashSet());
			Iterator it = new_items.iterator();
			while(it.hasNext())
			{
				ChefEditItem item = (ChefEditItem) it.next();
				item.clearMissing();
			}
		}

		alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		
		Iterator alertIt = alerts.iterator();
		while(alertIt.hasNext())
		{
			String alert = (String) alertIt.next();
			addCreateContextAlert(state, alert);
			//addAlert(state, alert);
		}
		alerts.clear();
		current_stack_frame.put(STATE_CREATE_MISSING_ITEM, missing);

		if(pop)
		{
			List new_items = (List) current_stack_frame.get(ResourcesAction.STATE_HELPER_NEW_ITEMS);
			String helper_changed = (String) state.getAttribute(STATE_HELPER_CHANGED);
			if(Boolean.TRUE.toString().equals(helper_changed))
			{
				// get list of attachments?
				if(new_items != null)
				{
					List attachments = (List) state.getAttribute(STATE_ATTACHMENTS);
					if(attachments == null)
					{
						attachments = EntityManager.newReferenceList();
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					Iterator it = new_items.iterator();
					while(it.hasNext())
					{
						AttachItem item = (AttachItem) it.next();
						try 
						{	
							ContentResource resource = ContentHostingService.getResource(item.getId());
							if (checkSelctItemFilter(resource, state))
							{
								attachments.add(resource.getReference());
							}
							else
							{
								it.remove();
								addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
							}
						} 
						catch (PermissionException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						} 
						catch (IdUnusedException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						} 
						catch (TypeException e) 
						{
							addAlert(state, (String) rb.getFormattedMessage("filter", new Object[]{item.getDisplayName()}));
						}
						
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(item.getId()));

		               }
				}
			}
			popFromStack(state);
			resetCurrentMode(state);

			if(!ResourcesAction.isStackEmpty(state) && new_items != null)
			{
				current_stack_frame = peekAtStack(state);
				List old_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
				if(old_items == null)
				{
					old_items = new Vector();
					current_stack_frame.put(STATE_HELPER_NEW_ITEMS, old_items);
				}
				old_items.addAll(new_items);
			}
		}

	}	// doCreateitem

	private static void createLink(RunData data, SessionState state)
	{
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String field = params.getString("field");
		if(field == null)
		{

		}
		else
		{
			//current_stack_frame.put(ResourcesAction.STATE_ATTACH_FORM_FIELD, field);
		}

		//state.setAttribute(ResourcesAction.STATE_MODE, ResourcesAction.MODE_HELPER);
		state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
		state.setAttribute(ResourcesAction.STATE_ATTACH_CARDINALITY, ResourcesAction.CARDINALITY_SINGLE);

		// put a copy of the attachments into the state

		// state.setAttribute(ResourcesAction.STATE_ATTACHMENTS, EntityManager.newReferenceList());
		// whether there is already an attachment
		/*
		if (attachments.size() > 0)
		{
			sstate.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
		}
		else
		{
			sstate.setAttribute(ResourcesAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.FALSE);
		}
		*/

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

	}

	/**
	 * Add a new folder to ContentHosting for each ChefEditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createFolders(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}
			String encoding = (String) state.getAttribute(STATE_ENCODING);
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			
			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			new_items = newEditItems(collectionId, TYPE_FOLDER, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);

			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

		}
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		int numberOfFolders = 1;
		numberOfFolders = number.intValue();

		outerloop: for(int i = 0; i < numberOfFolders; i++)
		{
			ChefEditItem item = (ChefEditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}
			String newCollectionId = collectionId + Validator.escapeResourceName(item.getName()) + Entity.SEPARATOR;

			if(newCollectionId.length() > ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH)
			{
				alerts.add(rb.getString("toolong") + " " + newCollectionId);
				continue outerloop;
			}


			try
			{
				ContentCollectionEdit collection = ContentHostingService.addCollection (newCollectionId);
				ResourcePropertiesEdit resourceProperties = collection.getPropertiesEdit();
				resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
				resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());
				List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
				saveMetadata(resourceProperties, metadataGroups, item);

				SortedSet groups = new TreeSet(item.getEntityGroupRefs());
				groups.retainAll(item.getAllowedAddGroupRefs());
				if(groups.isEmpty())
				{
					// do nothing
					// nothing to clear since it's a new entity
				}
				else
				{
					collection.setGroupAccess(groups);
				}

				if(ContentHostingService.isAvailabilityEnabled())
				{
					if(item.isHidden())
					{
						collection.setHidden();
					}
					else
					{
						if(item.useReleaseDate())
						{
							collection.setReleaseDate(item.getReleaseDate());
						}
						if(item.useRetractDate())
						{
							collection.setRetractDate(item.getRetractDate());
						}
					}
				}
				
				
				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(!preventPublicDisplay.booleanValue() && item.isPubview())
				{
					ContentHostingService.setPubView(collection.getId(), true);
				}
				
				ContentHostingService.commitCollection(collection);
				
			}
			catch (IdUsedException e)
			{
				alerts.add(rb.getString("resotitle") + " " + item.getName() + " " + rb.getString("used4"));
			}
			catch (IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
			}
			catch (PermissionException e)
			{
				alerts.add(rb.getString("notpermis5") + " " + item.getName());
			}
			catch (InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
			}	// try-catch
		}

		SortedSet currentMap = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(currentMap == null)
		{
			currentMap = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
		}
		if(!currentMap.contains(collectionId))
		{
			currentMap.add(collectionId);
			//state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);

			// add this folder id into the set to be event-observed
			addObservingPattern(collectionId, state);
		}

		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createFolders

	/**
	 * Add a new file to ContentHosting for each ChefEditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createFiles(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String encoding = (String) state.getAttribute(STATE_ENCODING);
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			
			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			new_items = newEditItems(collectionId, TYPE_FOLDER, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);

			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);

		}
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		int numberOfItems = 1;
		numberOfItems = number.intValue();
		outerloop: for(int i = 0; i < numberOfItems; i++)
		{
			ChefEditItem item = (ChefEditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}

			ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();

			resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
			resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());

			resourceProperties.addProperty (ResourceProperties.PROP_COPYRIGHT, item.getCopyrightInfo());
			resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, item.getCopyrightStatus());
			if (item.hasCopyrightAlert())
			{
				resourceProperties.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.toString(item.hasCopyrightAlert()));
			}
			else
			{
				resourceProperties.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
			}
			
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.addResourceProperties(resourceProperties);

			resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
			if(item.isHtml() || item.isPlaintext())
			{
				resourceProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, UTF_8_ENCODING);
			}
			List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
			saveMetadata(resourceProperties, metadataGroups, item);
			String filename = Validator.escapeResourceName(item.getFilename().trim());
			if("".equals(filename))
			{
				filename = Validator.escapeResourceName(item.getName().trim());
			}


			resourceProperties.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, filename);
			
			SortedSet groups = new TreeSet(item.getEntityGroupRefs());
			groups.retainAll(item.getAllowedAddGroupRefs());
			
			boolean hidden = false;

			Time releaseDate = null;
			Time retractDate = null;
			
			if(ContentHostingService.isAvailabilityEnabled())
			{
				hidden = item.isHidden();
				
				if(item.useReleaseDate())
				{
					releaseDate = item.getReleaseDate();
				}
				if(item.useRetractDate())
				{
					retractDate = item.getRetractDate();
				}
			}
			
			try
			{
				ContentResource resource = ContentHostingService.addResource (filename,
																			collectionId,
																			MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
																			item.getMimeType(),
																			item.getContent(),
																			resourceProperties,
																			groups,
																			hidden,
																			releaseDate,
																			retractDate,
																			item.getNotification());

				item.setAdded(true);
				
				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(!preventPublicDisplay.booleanValue() && item.isPubview())
				{
					ContentHostingService.setPubView(resource.getId(), true);
				}

				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM_INIT.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(resource.getId()));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
						if(attach_links == null)
						{
							attach_links = state.getAttribute(STATE_ATTACH_LINKS);
							if(attach_links != null)
							{
								current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
							}
						}

						if(attach_links == null)
						{
							attachItem(resource.getId(), state);
						}
						else
						{
							attachLink(resource.getId(), state);
						}
					}
				}
			}
			catch(PermissionException e)
			{
				alerts.add(rb.getString("notpermis12"));
				continue outerloop;
			}
			catch(IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
				continue outerloop;
			}
			catch(IdLengthException e)
			{
				alerts.add(rb.getString("toolong") + " " + e.getMessage());
				continue outerloop;
			}
			catch(IdUniquenessException e)
			{
				alerts.add("Could not add this item to this folder");
				continue outerloop;
			}
			catch(InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
				continue outerloop;
			}
			catch(OverQuotaException e)
			{
				alerts.add(rb.getString("overquota"));
				continue outerloop;
			}
			catch(ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
			catch(RuntimeException e)
			{
				logger.warn("ResourcesAction.createFiles ***** Unknown Exception ***** " + e.getMessage());
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}

		}
		SortedSet currentMap = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(currentMap == null)
		{
			currentMap = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
		}
		currentMap.add(collectionId);

		// add this folder id into the set to be event-observed
		addObservingPattern(collectionId, state);
		
		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createFiles

	/**
	 * Search a flat list of ResourcesMetadata properties for one whose localname matches "field".
	 * If found and the field can have additional instances, increment the count for that item.
	 * @param field
	 * @param properties
	 * @return true if the field is found, false otherwise.
	 */
	protected static boolean addInstance(String field, List properties)
	{
		Iterator propIt = properties.iterator();
		boolean found = false;
		while(!found && propIt.hasNext())
		{
			ResourcesMetadata property = (ResourcesMetadata) propIt.next();
			if(field.equals(property.getDottedname()))
			{
				found = true;
				property.incrementCount();
			}
		}
		return found;
	}

	public static void doAttachitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		Map current_stack_frame = peekAtStack(state);

		Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
		if(attach_links == null)
		{
			attach_links = state.getAttribute(STATE_ATTACH_LINKS);
			if(attach_links != null)
			{
				current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
			}
		}

		if(attach_links == null)
		{
			attachItem(itemId, state);
		}
		else
		{
			attachLink(itemId, state);
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		// popFromStack(state);
		// resetCurrentMode(state);

	}

	public static void doAttachupload(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1024 * 1024;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
		}

		FileItem fileitem = null;
		try
		{
			fileitem = params.getFileItem("upload");
		}
		catch(Exception e)
		{

		}
		if(fileitem == null)
		{
			// "The user submitted a file to upload but it was too big!"
			addAlert(state, rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
		}
		else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
		{
			addAlert(state, rb.getString("choosefile7"));
		}
		else if (fileitem.getFileName().length() > 0)
		{
			String filename = Validator.getFileName(fileitem.getFileName());
			byte[] bytes = fileitem.get();
			String contentType = fileitem.getContentType();

			if(bytes.length >= max_bytes)
			{
				addAlert(state, rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else if(bytes.length > 0)
			{
				// we just want the file name part - strip off any drive and path stuff
				String name = Validator.getFileName(filename);
				String resourceId = Validator.escapeResourceName(name);

				// make a set of properties to add for the new resource
				ResourcePropertiesEdit props = ContentHostingService.newResourceProperties();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
				props.addProperty(ResourceProperties.PROP_DESCRIPTION, filename);

				// make an attachment resource for this URL
				try
				{
					String siteId = ToolManager.getCurrentPlacement().getContext();

					String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
						if(toolName == null)
						{
							toolName = ToolManager.getCurrentPlacement().getTitle();
						}
						current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
					}

					ContentResource attachment = ContentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

					List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
					if(new_items == null)
					{
						new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
						if(new_items == null)
						{
							new_items = new Vector();
						}
						current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
					}

					String containerId = ContentHostingService.getContainingCollectionId (attachment.getId());
					String accessUrl = attachment.getUrl();

					AttachItem item = new AttachItem(attachment.getId(), filename, containerId, accessUrl);
					item.setContentType(contentType);
					new_items.add(item);
					//check -- jim
					state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());

					current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis4"));
				}
				catch(OverQuotaException e)
				{
					addAlert(state, rb.getString("overquota"));
				}
				catch(ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch(IdInvalidException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(InconsistentException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(IdUsedException ignore)
				{
					// other exceptions should be caught earlier
				}
				catch(RuntimeException e)
				{
					logger.debug("ResourcesAction.doAttachupload ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}
			else
			{
				addAlert(state, rb.getString("choosefile7"));
			}
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		//popFromStack(state);
		//resetCurrentMode(state);

	}	// doAttachupload

	public static void doAttachurl(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		String url = params.getCleanString("url");

		ResourcePropertiesEdit resourceProperties = ContentHostingService.newResourceProperties ();
		resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, url);
		resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, url);

		resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());

		try
		{
			url = validateURL(url);

			byte[] newUrl = url.getBytes();
			String newResourceId = Validator.escapeResourceName(url);

			String siteId = ToolManager.getCurrentPlacement().getContext();
			String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
			if(toolName == null)
			{
				toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = ToolManager.getCurrentPlacement().getTitle();
				}
				current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
			}

			ContentResource attachment = ContentHostingService.addAttachmentResource(newResourceId, siteId, toolName, ResourceProperties.TYPE_URL, newUrl, resourceProperties);

			List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
				if(new_items == null)
				{
					new_items = new Vector();
				}
				current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
			}

			String containerId = ContentHostingService.getContainingCollectionId (attachment.getId());
			String accessUrl = attachment.getUrl();

			AttachItem item = new AttachItem(attachment.getId(), url, containerId, accessUrl);
			item.setContentType(ResourceProperties.TYPE_URL);
			new_items.add(item);
			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		catch(MalformedURLException e)
		{
			// invalid url
			addAlert(state, rb.getString("validurl") + " \"" + url + "\" " + rb.getString("invalid"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis4"));
		}
		catch(OverQuotaException e)
		{
			addAlert(state, rb.getString("overquota"));
		}
		catch(ServerOverloadException e)
		{
			addAlert(state, rb.getString("failed"));
		}
		catch(IdInvalidException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(IdUsedException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(InconsistentException ignore)
		{
			// other exceptions should be caught earlier
		}
		catch(RuntimeException e)
		{
			logger.debug("ResourcesAction.doAttachurl ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT_INIT);
		// popFromStack(state);
		// resetCurrentMode(state);

	}

	public static void doRemoveitem(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		Map current_stack_frame = peekAtStack(state);

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String itemId = params.getString("itemId");

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		AttachItem item = null;
		boolean found = false;

		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(found && item != null)
		{
			new_items.remove(item);
			List removed = (List) state.getAttribute(STATE_REMOVED_ATTACHMENTS);
			if(removed == null)
			{
				removed = new Vector();
				state.setAttribute(STATE_REMOVED_ATTACHMENTS, removed);
			}
			removed.add(item);

			state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
		}

	}	// doRemoveitem

	public static void doAddattachments(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}
		List removed = (List) current_stack_frame.get(STATE_REMOVED_ATTACHMENTS);
		if(removed == null)
		{
			removed = (List) state.getAttribute(STATE_REMOVED_ATTACHMENTS);
			if(removed == null)
			{
				removed = new Vector();
			}
			current_stack_frame.put(STATE_REMOVED_ATTACHMENTS, removed);
		}
		Iterator removeIt = removed.iterator();
		while(removeIt.hasNext())
		{
			AttachItem item = (AttachItem) removeIt.next();
			try
			{
				if(ContentHostingService.isAttachmentResource(item.getId()))
				{
					ContentResourceEdit edit = ContentHostingService.editResource(item.getId());
					ContentHostingService.removeResource(edit);
					ContentCollectionEdit coll = ContentHostingService.editCollection(item.getCollectionId());
					ContentHostingService.removeCollection(coll);
				}
			}
			catch(Exception ignore)
			{
				// log failure
			}
		}
		state.removeAttribute(STATE_REMOVED_ATTACHMENTS);

		// add to the attachments vector
		List attachments = EntityManager.newReferenceList();

		Iterator it = new_items.iterator();
		while(it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();

			try
			{
				Reference ref = EntityManager.newReference(ContentHostingService.getReference(item.getId()));
				attachments.add(ref);
			}
			catch(Exception e)
			{
			}
		}
		cleanupState(state);
		state.setAttribute(STATE_ATTACHMENTS, attachments);

		// end up in main mode
		popFromStack(state);
		resetCurrentMode(state);
		current_stack_frame = peekAtStack(state);

		String field = null;

		// if there is at least one attachment
		if (attachments.size() > 0)
		{
			//check -- jim
			state.setAttribute(AttachmentAction.STATE_HAS_ATTACHMENT_BEFORE, Boolean.TRUE);
			if(current_stack_frame == null)
			{
			
			}
			else
			{
				//field = (String) current_stack_frame.get(STATE_ATTACH_FORM_FIELD);
			}
		}

		if(field != null)
		{
			int index = 0;
			String fieldname = field;
			Matcher matcher = INDEXED_FORM_FIELD_PATTERN.matcher(field.trim());
			if(matcher.matches())
			{
				fieldname = matcher.group(0);
				index = Integer.parseInt(matcher.group(1));
			}

			// we are trying to attach a link to a form field and there is at least one attachment
			if(new_items == null)
			{
				new_items = (List) current_stack_frame.get(ResourcesAction.STATE_HELPER_NEW_ITEMS);
				if(new_items == null)
				{
					new_items = (List) state.getAttribute(ResourcesAction.STATE_HELPER_NEW_ITEMS);
				}
			}
			ChefEditItem edit_item = null;
			List edit_items = (List) current_stack_frame.get(ResourcesAction.STATE_STACK_CREATE_ITEMS);
			if(edit_items == null)
			{
				edit_item = (ChefEditItem) current_stack_frame.get(ResourcesAction.STATE_STACK_EDIT_ITEM);
			}
			else
			{
				edit_item = (ChefEditItem) edit_items.get(0);
			}
			if(edit_item != null)
			{
				Reference ref = (Reference) attachments.get(0);
				//edit_item.setPropertyValue(fieldname, index, ref);
			}
		}
	}

	public static void attachItem(String itemId, SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}

		boolean found = false;
		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			try
			{
				ContentResource res = contentService.getResource(itemId);
				ResourceProperties props = res.getProperties();

				ResourcePropertiesEdit newprops = contentService.newResourceProperties();
				newprops.set(props);

				byte[] bytes = res.getContent();
				String contentType = res.getContentType();
				String filename = Validator.getFileName(itemId);
				String resourceId = Validator.escapeResourceName(filename);

				String siteId = ToolManager.getCurrentPlacement().getContext();
				String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = ToolManager.getCurrentPlacement().getTitle();
					}
					current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
				}

				ContentResource attachment = ContentHostingService.addAttachmentResource(resourceId, siteId, toolName, contentType, bytes, props);

				String displayName = newprops.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (attachment.getId());
				String accessUrl = attachment.getUrl();

				AttachItem item = new AttachItem(attachment.getId(), displayName, containerId, accessUrl);
				item.setContentType(contentType);
				new_items.add(item);
				state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis4"));
			}
			catch(OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch(IdInvalidException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(TypeException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUnusedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUsedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(InconsistentException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
	}

	public static void attachLink(String itemId, SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_HELPER_NEW_ITEMS);
		if(new_items == null)
		{
			new_items = (List) state.getAttribute(STATE_HELPER_NEW_ITEMS);
			if(new_items == null)
			{
				new_items = new Vector();
			}
			current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
		}

		Integer max_cardinality = (Integer) current_stack_frame.get(STATE_ATTACH_CARDINALITY);
		if(max_cardinality == null)
		{
			max_cardinality = (Integer) state.getAttribute(STATE_ATTACH_CARDINALITY);
			if(max_cardinality == null)
			{
				max_cardinality = CARDINALITY_MULTIPLE;
			}
			current_stack_frame.put(STATE_ATTACH_CARDINALITY, max_cardinality);
		}

		boolean found = false;
		Iterator it = new_items.iterator();
		while(!found && it.hasNext())
		{
			AttachItem item = (AttachItem) it.next();
			if(item.getId().equals(itemId))
			{
				found = true;
			}
		}

		if(!found)
		{
			try
			{
				ContentResource res = contentService.getResource(itemId);
				ResourceProperties props = res.getProperties();

				String contentType = res.getContentType();
				String filename = Validator.getFileName(itemId);
				String resourceId = Validator.escapeResourceName(filename);

				String siteId = ToolManager.getCurrentPlacement().getContext();
				String toolName = (String) current_stack_frame.get(STATE_ATTACH_TOOL_NAME);
				if(toolName == null)
				{
					toolName = (String) state.getAttribute(STATE_ATTACH_TOOL_NAME);
					if(toolName == null)
					{
						toolName = ToolManager.getCurrentPlacement().getTitle();
					}
					current_stack_frame.put(STATE_ATTACH_TOOL_NAME, toolName);
				}

				String displayName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				String containerId = contentService.getContainingCollectionId (itemId);
				String accessUrl = res.getUrl();

				AttachItem item = new AttachItem(itemId, displayName, containerId, accessUrl);
				item.setContentType(contentType);
				new_items.add(item);
				state.setAttribute(STATE_HELPER_CHANGED, Boolean.TRUE.toString());
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis4"));
			}
			catch(TypeException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(IdUnusedException ignore)
			{
				// other exceptions should be caught earlier
			}
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.attachItem ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}
		}
		current_stack_frame.put(STATE_HELPER_NEW_ITEMS, new_items);
	}

	/**
	 * Add a new URL to ContentHosting for each ChefEditItem in the state attribute named STATE_STACK_CREATE_ITEMS.
	 * The number of items to be added is indicated by the state attribute named STATE_STACK_CREATE_NUMBER, and
	 * the items are added to the collection identified by the state attribute named STATE_STACK_CREATE_COLLECTION_ID.
	 * @param state
	 */
	protected static void createUrls(SessionState state)
	{
		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}

		Map current_stack_frame = peekAtStack(state);

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}

		int numberOfItems = 1;
		numberOfItems = number.intValue();

		outerloop: for(int i = 0; i < numberOfItems; i++)
		{
			ChefEditItem item = (ChefEditItem) new_items.get(i);
			if(item.isBlank())
			{
				continue;
			}

			byte[] newUrl = item.getFilename().getBytes();
			String name = Validator.escapeResourceName(item.getName());

			SortedSet groups = new TreeSet(item.getEntityGroupRefs());
			groups.retainAll(item.getAllowedAddGroupRefs());
			
			boolean hidden = false;

			Time releaseDate = null;
			Time retractDate = null;
			
			if(ContentHostingService.isAvailabilityEnabled())
			{
				hidden = item.isHidden();
				
				if(item.useReleaseDate())
				{
					releaseDate = item.getReleaseDate();
				}
				if(item.useRetractDate())
				{
					retractDate = item.getRetractDate();
				}
			}
			
			try
			{
				ContentResourceEdit resource = ContentHostingService.addResource(name);

				ResourcePropertiesEdit resourceProperties = resource.getPropertiesEdit();
				resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
				resourceProperties.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());

				resourceProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION, Boolean.FALSE.toString());
				List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
				saveMetadata(resourceProperties, metadataGroups, item);

				item.setAdded(true);

				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
				
				if(!preventPublicDisplay.booleanValue() && item.isPubview())
				{
					ContentHostingService.setPubView(resource.getId(), true);
				}
				
				ContentHostingService.commitResource(resource);

				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(resource.getId()));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						Object attach_links = current_stack_frame.get(STATE_ATTACH_LINKS);
						if(attach_links == null)
						{
							attach_links = state.getAttribute(STATE_ATTACH_LINKS);
							if(attach_links != null)
							{
								current_stack_frame.put(STATE_ATTACH_LINKS, attach_links);
							}
						}

						if(attach_links == null)
						{
							attachItem(resource.getId(), state);
						}
						else
						{
							attachLink(resource.getId(), state);
						}
					}
				}

			}
			catch(PermissionException e)
			{
				alerts.add(rb.getString("notpermis12"));
				continue outerloop;
			}
			catch(IdInvalidException e)
			{
				alerts.add(rb.getString("title") + " " + e.getMessage ());
				continue outerloop;
			}
//			catch(IdLengthException e)
//			{
//				alerts.add(rb.getString("toolong") + " " + e.getMessage());
//				continue outerloop;
//			}
//			catch(IdUniquenessException e)
//			{
//				alerts.add("Could not add this item to this folder");
//				continue outerloop;
//			}
			catch(InconsistentException e)
			{
				alerts.add(RESOURCE_INVALID_TITLE_STRING);
				continue outerloop;
			}
			catch(OverQuotaException e)
			{
				alerts.add(rb.getString("overquota"));
				continue outerloop;
			}
			catch(ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
			catch (IdUsedException e)
			{
				// TODO Auto-generated catch block
				logger.debug("IdUsedException " + e.getMessage());
			}
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.createFiles ***** Unknown Exception ***** " + e.getMessage());
				alerts.add(rb.getString("failed"));
				continue outerloop;
			}
		}

		SortedSet currentMap = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(currentMap == null)
		{
			currentMap = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, currentMap);
		}
		if(!currentMap.contains(collectionId))
		{
			currentMap.add (collectionId);

			// add this folder id into the set to be event-observed
			addObservingPattern(collectionId, state);
		}

		state.setAttribute(STATE_CREATE_ALERTS, alerts);

	}	// createUrls

	/**
	* Build the context for creating folders and items
	*/
	public static String buildCreateContext (VelocityPortlet portlet,
												Context context,
												RunData data,
												SessionState state)
	{
		context.put("tlang",rb);
		// find the ContentTypeImage service
		context.put ("contentTypeImageService", state.getAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE));

		context.put("TYPE_FOLDER", TYPE_FOLDER);
		context.put("TYPE_UPLOAD", TYPE_UPLOAD);
		context.put("TYPE_HTML", TYPE_HTML);
		context.put("TYPE_TEXT", TYPE_TEXT);
		context.put("TYPE_URL", TYPE_URL);
		
		context.put("SITE_ACCESS", AccessMode.SITE.toString());
		context.put("GROUP_ACCESS", AccessMode.GROUPED.toString());
		context.put("INHERITED_ACCESS", AccessMode.INHERITED.toString());
		context.put("PUBLIC_ACCESS", PUBLIC_ACCESS);

		context.put("max_upload_size", state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE));

		Map current_stack_frame = peekAtStack(state);

		if(ContentHostingService.isAvailabilityEnabled())
		{
			context.put("availability_is_enabled", Boolean.TRUE);
		}
		
		String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
		if(itemType == null || itemType.trim().equals(""))
		{
			itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
			if(itemType == null || itemType.trim().equals(""))
			{
				itemType = TYPE_UPLOAD;
			}
			current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
		}
		context.put("itemType", itemType);

		String collectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
		if(collectionId == null || collectionId.trim().length() == 0)
		{
			collectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			if(collectionId == null || collectionId.trim().length() == 0)
			{
				collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			}
			current_stack_frame.put(STATE_STACK_CREATE_COLLECTION_ID, collectionId);
		}
		context.put("collectionId", collectionId);

		String msg = (String) state.getAttribute(STATE_CREATE_MESSAGE);
		if (msg != null)
		{
			context.put("createAlertMessage", msg);
			state.removeAttribute(STATE_CREATE_MESSAGE);
		}

		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		
		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String encoding = data.getRequest().getCharacterEncoding();
			
			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		}
		context.put("new_items", new_items);
		
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		context.put("numberOfItems", number);
		context.put("max_number", new Integer(CREATE_MAX_ITEMS));
		String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		context.put("homeCollectionId", homeCollectionId);
		List collectionPath = getCollectionPath(state);
		context.put ("collectionPath", collectionPath);

		if(homeCollectionId.equals(collectionId))
		{
			context.put("atHome", Boolean.TRUE.toString());
		}

		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			List theGroupsInThisSite = new Vector();
			for(int i = 0; i < CREATE_MAX_ITEMS; i++)
			{
				theGroupsInThisSite.add(groups.iterator());
			}
			context.put("theGroupsInThisSite", theGroupsInThisSite);
		}
		
		// copyright
		copyrightChoicesIntoContext(state, context);

		// put schema for metadata into context
		metadataGroupsIntoContext(state, context);

		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_RESOURCES.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			context.put("dropboxMode", Boolean.FALSE);
		}
		else if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// notshow the public option or notification when in dropbox mode
			context.put("dropboxMode", Boolean.TRUE);
		}
		context.put("siteTitle", state.getAttribute(STATE_SITE_TITLE));

		/*
		Collection groups = ContentHostingService.getGroupsWithReadAccess(collectionId);
		if(! groups.isEmpty())
		{
			context.put("siteHasGroups", Boolean.TRUE.toString());
			context.put("theGroupsInThisSite", groups);
		}
		*/

		Set missing = (Set) current_stack_frame.remove(STATE_CREATE_MISSING_ITEM);
		context.put("missing", missing);

		// String template = (String) getContext(data).get("template");
		return TEMPLATE_CREATE;

	}

	/**
	* show the resource properties
	*/
	public static void doMore ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		ParameterParser params = data.getParameters ();

		Map current_stack_frame = pushOnStack(state);

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		// the hosted item ID
		String id = NULL_STRING;

		// the collection id
		String collectionId = NULL_STRING;

		try
		{
			id = params.getString ("id");
			if (id!=null)
			{
				// set the collection/resource id for more context
				current_stack_frame.put(STATE_MORE_ID, id);
			}
			else
			{
				// get collection/resource id from the state object
				id =(String) current_stack_frame.get(STATE_MORE_ID);
			}

			collectionId = params.getString ("collectionId");
			current_stack_frame.put(STATE_MORE_COLLECTION_ID, collectionId);

			if (collectionId.equals ((String) state.getAttribute(STATE_HOME_COLLECTION_ID)))
			{
				try
				{
					// this is a test to see if the collection exists.  If not, it is created.
					ContentCollection collection = ContentHostingService.getCollection (collectionId);
				}
				catch (IdUnusedException e )
				{
					try
					{
						String homeCollectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
						ContentCollectionEdit edit = ContentHostingService.addCollection(homeCollectionId);
						
						// default copyright
						String mycopyright = (String) state.getAttribute (STATE_MY_COPYRIGHT);

						ResourcePropertiesEdit resourceProperties = edit.getPropertiesEdit();
						resourceProperties.addProperty (ResourceProperties.PROP_DISPLAY_NAME, ContentHostingService.getProperties (homeCollectionId).getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME));
						ContentHostingService.commitCollection(edit);
					}
					catch (IdUsedException ee)
					{
						addAlert(state, rb.getString("idused"));
					}
					catch (IdUnusedException ee)
					{
						addAlert(state,RESOURCE_NOT_EXIST_STRING);
					}
					catch (IdInvalidException ee)
					{
						addAlert(state, rb.getString("title") + " " + ee.getMessage ());
					}
					catch (PermissionException ee)
					{
						addAlert(state, rb.getString("permisex"));
					}
					catch (InconsistentException ee)
					{
						addAlert(state, RESOURCE_INVALID_TITLE_STRING);
					}
				}
				catch (TypeException e )
				{
					addAlert(state, rb.getString("typeex"));
				}
				catch (PermissionException e )
				{
					addAlert(state, rb.getString("permisex"));
				}
			}
		}
		catch (NullPointerException eE)
		{
			addAlert(state," " + rb.getString("nullex") + " " + id + ". ");
		}
		
		ChefEditItem item = getEditItem(id, collectionId, data);
		
		

		// is there no error?
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// go to the more state
			state.setAttribute(STATE_MODE, MODE_MORE);

		}	// if-else

	}	// doMore

	/**
	* doDelete to delete the selected collection or resource items
	*/
	public void doDelete ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		ParameterParser params = data.getParameters ();

		List Items = (List) state.getAttribute(STATE_DELETE_ITEMS);

		// Vector deleteIds = (Vector) state.getAttribute (STATE_DELETE_IDS);

		// delete the lowest item in the hireachy first
		Hashtable deleteItems = new Hashtable();
		// String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		int maxDepth = 0;
		int depth = 0;

		Iterator it = Items.iterator();
		while(it.hasNext())
		{
			ChefBrowseItem item = (ChefBrowseItem) it.next();
			depth = ContentHostingService.getDepth(item.getId(), item.getRoot());
			if (depth > maxDepth)
			{
				maxDepth = depth;
			}
			List v = (List) deleteItems.get(new Integer(depth));
			if(v == null)
			{
				v = new Vector();
			}
			v.add(item);
			deleteItems.put(new Integer(depth), v);
		}

		boolean isCollection = false;
		for (int j=maxDepth; j>0; j--)
		{
			List v = (List) deleteItems.get(new Integer(j));
			if (v==null)
			{
				v = new Vector();
			}
			Iterator itemIt = v.iterator();
			while(itemIt.hasNext())
			{
				ChefBrowseItem item = (ChefBrowseItem) itemIt.next();
				try
				{
					if (item.isFolder())
					{
						ContentHostingService.removeCollection(item.getId());
					}
					else
					{
						ContentHostingService.removeResource(item.getId());
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis6") + " " + item.getName() + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("wrongtype"));
				}
				catch (ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch (InUseException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("locked"));
				}// try - catch
				catch(RuntimeException e)
				{
					logger.debug("ResourcesAction.doDelete ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}	// for

		}	// for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);

			if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
			}

		}	// if-else

	}	// doDelete

	/**
	* doDelete to delete the selected collection or resource items
	*/
	public void doFinalizeDelete( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		ParameterParser params = data.getParameters ();

		List items = (List) state.getAttribute(STATE_DELETE_SET);

		// Vector deleteIds = (Vector) state.getAttribute (STATE_DELETE_IDS);

		// delete the lowest item in the hireachy first
		Hashtable deleteItems = new Hashtable();
		// String collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		int maxDepth = 0;
		int depth = 0;

		Iterator it = items.iterator();
		while(it.hasNext())
		{
			ListItem item = (ListItem) it.next();
			String[] parts = item.getId().split(Entity.SEPARATOR);
			depth = parts.length;
			if (depth > maxDepth)
			{
				maxDepth = depth;
			}
			List v = (List) deleteItems.get(new Integer(depth));
			if(v == null)
			{
				v = new Vector();
			}
			v.add(item);
			deleteItems.put(new Integer(depth), v);
		}

		boolean isCollection = false;
		for (int j=maxDepth; j>0; j--)
		{
			List v = (List) deleteItems.get(new Integer(j));
			if (v==null)
			{
				v = new Vector();
			}
			Iterator itemIt = v.iterator();
			while(itemIt.hasNext())
			{
				ListItem item = (ListItem) itemIt.next();
				try
				{
					if (item.isCollection())
					{
						ContentHostingService.removeCollection(item.getId());
					}
					else
					{
						ContentHostingService.removeResource(item.getId());
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis6") + " " + item.getName() + ". ");
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}
				catch (TypeException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("wrongtype"));
				}
				catch (ServerOverloadException e)
				{
					addAlert(state, rb.getString("failed"));
				}
				catch (InUseException e)
				{
					addAlert(state, rb.getString("deleteres") + " " + item.getName() + " " + rb.getString("locked"));
				}// try - catch
				catch(RuntimeException e)
				{
					logger.debug("ResourcesAction.doDelete ***** Unknown Exception ***** " + e.getMessage());
					addAlert(state, rb.getString("failed"));
				}
			}	// for

		}	// for

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			state.setAttribute (STATE_MODE, MODE_LIST);
			state.removeAttribute(STATE_DELETE_SET);
			state.removeAttribute(STATE_NON_EMPTY_DELETE_SET);

			if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
			}

		}	// if-else

	}	// doDelete

	/**
	* doCancel to return to the previous state
	*/
	public static void doCancel ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		if(!isStackEmpty(state))
		{
			Map current_stack_frame = peekAtStack(state);
			current_stack_frame.put(STATE_HELPER_CANCELED_BY_USER, Boolean.TRUE.toString());

			popFromStack(state);
		}

		resetCurrentMode(state);

	}	// doCancel

	/**
	* Edit the editable collection/resource properties
	*/
	public static void doEdit ( RunData data )
	{
		ParameterParser params = data.getParameters ();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Map current_stack_frame = pushOnStack(state);

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		String id = NULL_STRING;
		id = params.getString ("id");
		if(id == null || id.length() == 0)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile2"));
			return;
		}

		current_stack_frame.put(STATE_STACK_EDIT_ID, id);

		String collectionId = (String) params.getString("collectionId");
		if(collectionId == null)
		{
			collectionId = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
			state.setAttribute(STATE_HOME_COLLECTION_ID, collectionId);
		}
		current_stack_frame.put(STATE_STACK_EDIT_COLLECTION_ID, collectionId);

		ChefEditItem item = getEditItem(id, collectionId, data);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// got resource and sucessfully populated item with values
			// state.setAttribute (STATE_MODE, MODE_EDIT);
			state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_EDIT_ITEM_INIT);
			state.setAttribute(STATE_EDIT_ALERTS, new HashSet());
			current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);

		}
		else
		{
			popFromStack(state);
		}

	}	// doEdit

	public static ChefEditItem getEditItem(String id, String collectionId, RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		Stack operations_stack = (Stack) state.getAttribute(STATE_SUSPENDED_OPERATIONS_STACK);

		Map current_stack_frame = peekAtStack(state);

		ChefEditItem item = null;

		// populate an ChefEditItem object with values from the resource and return the ChefEditItem
		try
		{
			ResourceProperties properties = ContentHostingService.getProperties(id);

			boolean isCollection = false;
			try
			{
				isCollection = properties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
			}
			catch(Exception e)
			{
				// assume isCollection is false if property is not set
			}

			ContentEntity entity = null;
			String itemType = "";
			byte[] content = null;
			if(isCollection)
			{
				itemType = "folder";
				entity = ContentHostingService.getCollection(id);
			}
			else
			{
				entity = ContentHostingService.getResource(id);
				itemType = ((ContentResource) entity).getContentType();
				content = ((ContentResource) entity).getContent();
			}

			String itemName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			item = new ChefEditItem(id, itemName, itemType);
			
			item.setInDropbox(ContentHostingService.isInDropbox(id));
			boolean isUserSite = false;
			String refstr = entity.getReference();
			Reference ref = EntityManager.newReference(refstr);
			String contextId = ref.getContext();
			if(contextId != null)
			{
				isUserSite = SiteService.isUserSite(contextId);
			}
			item.setInWorkspace(isUserSite);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(item.getItemNum(), properties);
			item.setRights(rightsObj);

			String encoding = data.getRequest().getCharacterEncoding();
			if(encoding != null)
			{
				item.setEncoding(encoding);
			}

			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}
			item.setCopyrightStatus(defaultCopyrightStatus);

			if(content != null)
			{
				item.setContent(content);
			}

			String dummyId = collectionId.trim();
			if(dummyId.endsWith(Entity.SEPARATOR))
			{
				dummyId += "dummy";
			}
			else
			{
				dummyId += Entity.SEPARATOR + "dummy";
			}

			String containerId = ContentHostingService.getContainingCollectionId (id);
			item.setContainer(containerId);

			boolean canRead = ContentHostingService.allowGetCollection(id);
			boolean canAddFolder = ContentHostingService.allowAddCollection(id);
			boolean canAddItem = ContentHostingService.allowAddResource(id);
			boolean canDelete = ContentHostingService.allowRemoveResource(id);
			boolean canRevise = ContentHostingService.allowUpdateResource(id);
			item.setCanRead(canRead);
			item.setCanRevise(canRevise);
			item.setCanAddItem(canAddItem);
			item.setCanAddFolder(canAddFolder);
			item.setCanDelete(canDelete);
			// item.setIsUrl(isUrl);
			
			AccessMode access = ((GroupAwareEntity) entity).getAccess();
			if(access == null)
			{
				item.setAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setAccess(access.toString());
			}

			AccessMode inherited_access = ((GroupAwareEntity) entity).getInheritedAccess();
			if(inherited_access == null || inherited_access.equals(AccessMode.SITE))
			{
				item.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				item.setInheritedAccess(inherited_access.toString());
			}
			
			Site site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			Collection site_groups = site.getGroups();
			item.setAllSiteGroups(site_groups);
			
			List access_groups = new Vector(((GroupAwareEntity) entity).getGroups());
			item.setEntityGroupRefs(access_groups);
//			if(access_groups != null)
//			{
//				
//				Iterator it = access_groups.iterator();
//				while(it.hasNext())
//				{
//					String groupRef = (String) it.next();
//					Group group = site.getGroup(groupRef);
//					item.addGroup(group.getId());
//				}
//			}

			List inherited_access_groups = new Vector(((GroupAwareEntity) entity).getInheritedGroups());
			item.setInheritedGroupRefs(inherited_access_groups);
//			if(inherited_access_groups != null)
//			{
//				Iterator it = inherited_access_groups.iterator();
//				while(it.hasNext())
//				{
//					String groupRef = (String) it.next();
//					Group group = site.getGroup(groupRef);
//					item.addInheritedGroup(group.getId());
//				}
//			}
			
			Collection allowedRemoveGroups = null;
			if(AccessMode.GROUPED == access)
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(id);
				Collection more = ContentHostingService.getGroupsWithRemovePermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedRemoveGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(collectionId);
			}
			else
			{
				allowedRemoveGroups = ContentHostingService.getGroupsWithRemovePermission(ContentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedRemoveGroupRefs(allowedRemoveGroups);
			
			Collection allowedAddGroups = null;
			if(AccessMode.GROUPED == access)
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(id);
				Collection more = ContentHostingService.getGroupsWithAddPermission(collectionId);
				if(more != null && ! more.isEmpty())
				{
					allowedAddGroups.addAll(more);
				}
			}
			else if(AccessMode.GROUPED == inherited_access)
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(collectionId);
			}
			else
			{
				allowedAddGroups = ContentHostingService.getGroupsWithAddPermission(ContentHostingService.getSiteCollection(site.getId()));
			}
			item.setAllowedAddGroupRefs(allowedAddGroups);
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			if(preventPublicDisplay.booleanValue())
			{
				item.setPubviewPossible(false);
				item.setPubviewInherited(false);
				item.setPubview(false);
			}
			else
			{
				item.setPubviewPossible(true);
				// find out about pubview
				boolean pubviewset = ContentHostingService.isInheritingPubView(id);
				item.setPubviewInherited(pubviewset);
				boolean pubview = pubviewset;
				if (!pubviewset) 
				{
					pubview = ContentHostingService.isPubView(id);
					item.setPubview(pubview);
				}
			}

			if(entity.isHidden())
			{
				item.setHidden(true);
				//item.setReleaseDate(null);
				//item.setRetractDate(null);
			}
			else
			{
				item.setHidden(false);
				Time releaseDate = entity.getReleaseDate();
				if(releaseDate == null)
				{
					item.setUseReleaseDate(false);
					item.setReleaseDate(TimeService.newTime());
				}
				else
				{
					item.setUseReleaseDate(true);
					item.setReleaseDate(releaseDate);
				}
				Time retractDate = entity.getRetractDate();
				if(retractDate == null)
				{
					item.setUseRetractDate(false);
					Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
					if(defaultRetractDate == null)
					{
						defaultRetractDate = TimeService.newTime();
						state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
					}
					item.setRetractDate(defaultRetractDate);
				}
				else
				{
					item.setUseRetractDate(true);
					item.setRetractDate(retractDate);
				}
			}

			if(item.isUrl())
			{
				String url = new String(content);
				item.setFilename(url);
			}
			else if(item.isHtml() || item.isPlaintext() || item.isFileUpload())
			{
				String filename = properties.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME);
				if(filename == null)
				{
					// this is a hack to deal with the fact that original filenames were not saved for some time.
					if(containerId != null && item.getId().startsWith(containerId) && containerId.length() < item.getId().length())
					{
						filename = item.getId().substring(containerId.length());
					}
				}

				if(filename == null)
				{
					item.setFilename(itemName);
				}
				else
				{
					item.setFilename(filename);
				}
			}

			String description = properties.getProperty(ResourceProperties.PROP_DESCRIPTION);
			item.setDescription(description);

			try
			{
				Time creTime = properties.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				String createdTime = creTime.toStringLocalShortDate() + " " + creTime.toStringLocalShort();
				item.setCreatedTime(createdTime);
			}
			catch(Exception e)
			{
				String createdTime = properties.getProperty(ResourceProperties.PROP_CREATION_DATE);
				item.setCreatedTime(createdTime);
			}
			try
			{
				String createdBy = getUserProperty(properties, ResourceProperties.PROP_CREATOR).getDisplayName();
				item.setCreatedBy(createdBy);
			}
			catch(Exception e)
			{
				String createdBy = properties.getProperty(ResourceProperties.PROP_CREATOR);
				item.setCreatedBy(createdBy);
			}
			try
			{
				Time modTime = properties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
				String modifiedTime = modTime.toStringLocalShortDate() + " " + modTime.toStringLocalShort();
				item.setModifiedTime(modifiedTime);
			}
			catch(Exception e)
			{
				String modifiedTime = properties.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
				item.setModifiedTime(modifiedTime);
			}
			try
			{
				String modifiedBy = getUserProperty(properties, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
				item.setModifiedBy(modifiedBy);
			}
			catch(Exception e)
			{
				String modifiedBy = properties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
				item.setModifiedBy(modifiedBy);
			}

			String url = ContentHostingService.getUrl(id);
			item.setUrl(url);

			String size = "";
			if(properties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH) != null)
			{
				size = properties.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH) + " (" + Validator.getFileSizeWithDividor(properties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)) +" bytes)";
			}
			item.setSize(size);

			String copyrightStatus = properties.getProperty(properties.getNamePropCopyrightChoice());
			if(copyrightStatus == null || copyrightStatus.trim().equals(""))
			{
				copyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);

			}
			item.setCopyrightStatus(copyrightStatus);
			String copyrightInfo = properties.getPropertyFormatted(properties.getNamePropCopyright());
			item.setCopyrightInfo(copyrightInfo);
			String copyrightAlert = properties.getProperty(properties.getNamePropCopyrightAlert());

			if("true".equalsIgnoreCase(copyrightAlert))
			{
				item.setCopyrightAlert(true);
			}
			else
			{
				item.setCopyrightAlert(false);
			}
			
			Map metadata = new Hashtable();
			List groups = (List) state.getAttribute(STATE_METADATA_GROUPS);
			if(groups != null && ! groups.isEmpty())
			{
				Iterator it = groups.iterator();
				while(it.hasNext())
				{
					MetadataGroup group = (MetadataGroup) it.next();
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String name = prop.getFullname();
						String widget = prop.getWidget();
						if(widget.equals(ResourcesMetadata.WIDGET_DATE) || widget.equals(ResourcesMetadata.WIDGET_DATETIME) || widget.equals(ResourcesMetadata.WIDGET_TIME))
						{
							Time time = TimeService.newTime();
							try
							{
								time = properties.getTimeProperty(name);
							}
							catch(Exception ignore)
							{
								// use "now" as default in that case
							}
							metadata.put(name, time);
						}
						else
						{
							String value = properties.getPropertyFormatted(name);
							metadata.put(name, value);
						}
					}
				}
				item.setMetadata(metadata);
			}
			else
			{
				item.setMetadata(new Hashtable());
			}
			// for collections only
			if(item.isFolder())
			{
				// setup for quota - ADMIN only, site-root collection only
				if (SecurityService.isSuperUser())
				{
					String siteCollectionId = ContentHostingService.getSiteCollection(contextId);
					if(siteCollectionId.equals(entity.getId()))
					{
						item.setCanSetQuota(true);
						try
						{
							long quota = properties.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
							item.setHasQuota(true);
							item.setQuota(Long.toString(quota));
						}
						catch (Exception any)
						{
						}
					}
				}
			}

		}
		catch (IdUnusedException e)
		{
			addAlert(state, RESOURCE_NOT_EXIST_STRING);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis2") + " " + id + ". " );
		}
		catch(TypeException e)
		{
			addAlert(state," " + rb.getString("typeex") + " "  + id);
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch(RuntimeException e)
		{
			logger.debug("ResourcesAction.getEditItem ***** Unknown Exception ***** " + e.getMessage());
			addAlert(state, rb.getString("failed"));
		}

		return item;

	}
   
	protected static String validateURL(String url) throws MalformedURLException
	{
		if (url.equals (NULL_STRING))
		{
			// ignore the empty url field
		}
		else if (url.indexOf ("://") == -1)
		{
			// if it's missing the transport, add http://
			url = "http://" + url;
		}

		if(!url.equals(NULL_STRING))
		{
			// valid protocol?
			try
			{
				// test to see if the input validates as a URL.
				// Checks string for format only.
				URL u = new URL(url);
			}
			catch (MalformedURLException e1)
			{
				try
				{
					Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
					Matcher matcher = pattern.matcher(url);
					if(matcher.matches())
					{
						// if URL has "unknown" protocol, check remaider with
						// "http" protocol and accept input it that validates.
						URL test = new URL("http://" + matcher.group(2));
					}
					else
					{
						throw e1;
					}
				}
				catch (MalformedURLException e2)
				{
					throw e1;
				}
			}
		}
		return url;
	}

	/**
	 * Retrieve values for an item from edit context.  Edit context contains just one item at a time of a known type
	 * (folder, file, text document, etc).  This method retrieves the data apppropriate to the
	 * type and updates the values of the ChefEditItem stored as the STATE_STACK_EDIT_ITEM attribute in state.
	 * @param state
	 * @param params
	 * @param item
	 */
	protected static void captureValues(SessionState state, ParameterParser params)
	{
		Map current_stack_frame = peekAtStack(state);

		ChefEditItem item = (ChefEditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);
		Set alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_EDIT_ALERTS, alerts);
		}
		String flow = params.getString("flow");
		boolean intentChanged = "intentChanged".equals(flow);
		String check_fileName = params.getString("check_fileName");
		boolean expectFile = "true".equals(check_fileName);
		String intent = params.getString("intent");
		String oldintent = (String) current_stack_frame.get(STATE_STACK_EDIT_INTENT);
		boolean upload_file = expectFile && item.isFileUpload() || ((item.isHtml() || item.isPlaintext()) && !intentChanged && INTENT_REPLACE_FILE.equals(intent) && INTENT_REPLACE_FILE.equals(oldintent));
		boolean revise_file = (item.isHtml() || item.isPlaintext()) && !intentChanged && INTENT_REVISE_FILE.equals(intent) && INTENT_REVISE_FILE.equals(oldintent);

		String name = params.getString("name");
		if(name == null || "".equals(name.trim()))
		{
			alerts.add(rb.getString("titlenotnull"));
			// addAlert(state, rb.getString("titlenotnull"));
		}
		else
		{
			item.setName(name.trim());
		}

		String description = params.getString("description");
		if(description == null)
		{
			item.setDescription("");
		}
		else
		{
			item.setDescription(description);
		}

		item.setContentHasChanged(false);

		if(upload_file)
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1096 * 1096;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1096 * 1096;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1096 * 1096;
			}
			/*
			 // params.getContentLength() returns m_req.getContentLength()
			if(params.getContentLength() >= max_bytes)
			{
				alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else
			*/
			{
				// check for file replacement
				FileItem fileitem = params.getFileItem("fileName");
				if(fileitem == null)
				{
					// "The user submitted a file to upload but it was too big!"
					alerts.clear();
					alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
					//item.setMissing("fileName");
				}
				else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
				{
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						// "The user submitted the form, but didn't select a file to upload!"
						alerts.add(rb.getString("choosefile") + ". ");
						//item.setMissing("fileName");
					}
				}
				else if (fileitem.getFileName().length() > 0)
				{
					String filename = Validator.getFileName(fileitem.getFileName());
					byte[] bytes = fileitem.get();
					String contenttype = fileitem.getContentType();

					if(bytes.length >= max_bytes)
					{
						alerts.clear();
						alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
						// item.setMissing("fileName");
					}
					else if(bytes.length > 0)
					{
						item.setContent(bytes);
						item.setContentHasChanged(true);
						item.setMimeType(contenttype);
						item.setFilename(filename);
					}
				}
			}
		}
		else if(revise_file)
		{
			// check for input from editor (textarea)
			String content = params.getString("content");
			if(content != null)
			{
				item.setContent(content);
				item.setContentHasChanged(true);
			}
		}
		else if(item.isUrl())
		{
			String url = params.getString("Url");
			if(url == null || url.trim().equals(""))
			{
				item.setFilename("");
				alerts.add(rb.getString("validurl"));
			}
			else
			{
				// valid protocol?
				item.setFilename(url);
				try
				{
					// test format of input
					URL u = new URL(url);
				}
				catch (MalformedURLException e1)
				{
					try
					{
						// if URL did not validate, check whether the problem was an
						// unrecognized protocol, and accept input if that's the case.
						Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
						Matcher matcher = pattern.matcher(url);
						if(matcher.matches())
						{
							URL test = new URL("http://" + matcher.group(2));
						}
						else
						{
							url = "http://" + url;
							URL test = new URL(url);
							item.setFilename(url);
						}
					}
					catch (MalformedURLException e2)
					{
						// invalid url
						alerts.add(rb.getString("validurl"));
					}
				}
			}
		}
		else if(item.isFolder())
		{
			if(item.canSetQuota())
			{
				// read the quota fields
				String setQuota = params.getString("setQuota");
				boolean hasQuota = params.getBoolean("hasQuota");
				item.setHasQuota(hasQuota);
				if(hasQuota)
				{
					int q = params.getInt("quota");
					item.setQuota(Integer.toString(q));
				}
			}
		}

		if(! item.isFolder() && ! item.isUrl())
		{
			String mime_category = params.getString("mime_category");
			String mime_subtype = params.getString("mime_subtype");

			if(mime_category != null && mime_subtype != null)
			{
				String mimetype = mime_category + "/" + mime_subtype;
				if(! mimetype.equals(item.getMimeType()))
				{
					item.setMimeType(mimetype);
					item.setContentTypeHasChanged(true);
				}
			}
		}

		if(item.isFileUpload() || item.isHtml() || item.isPlaintext())
		{
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.captureValues(params);

			boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
			
			if(usingCreativeCommons)
			{
				String ccOwnership = params.getString("ccOwnership");
				if(ccOwnership != null)
				{
					item.setRightsownership(ccOwnership);
				}
				String ccTerms = params.getString("ccTerms");
				if(ccTerms != null)
				{
					item.setLicense(ccTerms);
				}
				String ccCommercial = params.getString("ccCommercial");
				if(ccCommercial != null)
				{
					item.setAllowCommercial(ccCommercial);
				}
				String ccModification = params.getString("ccModification");
				if(ccCommercial != null)
				{
					item.setAllowModifications(ccModification);
				}
				String ccRightsYear = params.getString("ccRightsYear");
				if(ccRightsYear != null)
				{
					item.setRightstyear(ccRightsYear);
				}
				String ccRightsOwner = params.getString("ccRightsOwner");
				if(ccRightsOwner != null)
				{
					item.setRightsowner(ccRightsOwner);
				}

				/*
				ccValues.ccOwner = new Array();
				ccValues.myRights = new Array();
				ccValues.otherRights = new Array();
				ccValues.ccCommercial = new Array();
				ccValues.ccModifications = new Array();
				ccValues.ccRightsYear = new Array();
				ccValues.ccRightsOwner = new Array();
				*/
			}
			else
			{
				// check for copyright status
				// check for copyright info
				// check for copyright alert
	
				String copyrightStatus = StringUtil.trimToNull(params.getString ("copyrightStatus"));
				String copyrightInfo = StringUtil.trimToNull(params.getCleanString ("copyrightInfo"));
				String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert"));
	
				if (copyrightStatus != null)
				{
					if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyrightStatus.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
					{
						if (copyrightInfo != null)
						{
							item.setCopyrightInfo( copyrightInfo );
						}
						else
						{
							alerts.add(rb.getString("specifycp2"));
							// addAlert(state, rb.getString("specifycp2"));
						}
					}
					else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyrightStatus.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
					{
						item.setCopyrightInfo((String) state.getAttribute (STATE_MY_COPYRIGHT));
					}
	
					item.setCopyrightStatus( copyrightStatus );
				}
				item.setCopyrightAlert(copyrightAlert != null);
			}
		}
		if(!  RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			String hidden = params.getString("hidden");
			String use_start_date = params.getString("use_start_date");
			String use_end_date = params.getString("use_end_date");
			String release_month = params.getString("release_month");
			String release_day = params.getString("release_day");
			String release_year = params.getString("release_year");
			String release_hour = params.getString("release_hour");
			String release_min = params.getString("release_minute");
			String release_ampm = params.getString("release_ampm");
			
			try
			{
				String release_time = params.getString("release_time");
				String retract_month = params.getString("retract_month");
				String retract_day = params.getString("retract_day");
				String retract_year = params.getString("retract_year");
				String retract_time = params.getString("retract_time");
				String retract_hour = params.getString("retract_hour");
				String retract_min = params.getString("retract_minute");
				String retract_ampm = params.getString("retract_ampm");
				
				int begin_year = Integer.parseInt(release_year);
				int begin_month = Integer.parseInt(release_month);
				int begin_day = Integer.parseInt(release_day);
				int begin_hour = Integer.parseInt(release_hour);
				int begin_min = Integer.parseInt(release_min);
				if("pm".equals(release_ampm))
				{
					begin_hour += 12;
				}
				else if(begin_hour == 12)
				{
					begin_hour = 0;
				}
				Time releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
				item.setReleaseDate(releaseDate);
				
				int end_year = Integer.parseInt(retract_year);
				int end_month = Integer.parseInt(retract_month);
				int end_day = Integer.parseInt(retract_day);
				int end_hour = Integer.parseInt(retract_hour);
				int end_min = Integer.parseInt(retract_min);
				if("pm".equals(retract_ampm))
				{
					end_hour += 12;
				}
				else if(begin_hour == 12)
				{
					end_hour = 0;
				}
				Time retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
				item.setRetractDate(retractDate);
				
				item.setHidden(Boolean.TRUE.toString().equalsIgnoreCase(hidden));
				item.setUseReleaseDate(Boolean.TRUE.toString().equalsIgnoreCase(use_start_date));
				item.setUseRetractDate(Boolean.TRUE.toString().equalsIgnoreCase(use_end_date));
			}
			catch(NumberFormatException e)
			{
				// no values retrieved from date widget, or values are not numbers 
			}
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			String access_mode = params.getString("access_mode");
			
			if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
			{
				// we inherit more than one group and must check whether group access changes at this item
				String[] access_groups = params.getStrings("access_groups");
				
				SortedSet new_groups = new TreeSet();
				if(access_groups != null)
				{
					new_groups.addAll(Arrays.asList(access_groups));
				}
				new_groups = item.convertToRefs(new_groups);
				
				Collection inh_grps = item.getInheritedGroupRefs();
				boolean groups_are_inherited = (new_groups.size() == inh_grps.size()) && inh_grps.containsAll(new_groups);
				
				if(groups_are_inherited)
				{
					new_groups.clear();
					item.setEntityGroupRefs(new_groups);
					item.setAccess(AccessMode.INHERITED.toString());
				}
				else
				{
					item.setEntityGroupRefs(new_groups);
					item.setAccess(AccessMode.GROUPED.toString());
				}
				
				item.setPubview(false);
			}
			else if(PUBLIC_ACCESS.equals(access_mode))
			{
				if(! preventPublicDisplay.booleanValue() && ! item.isPubviewInherited())
				{
					item.setPubview(true);
					item.setAccess(AccessMode.INHERITED.toString());
				}
			}
			else if(AccessMode.INHERITED.toString().equals(access_mode))
			{
				item.setAccess(AccessMode.INHERITED.toString());
				item.clearGroups();
				item.setPubview(false);
			}
		}

		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// set noti to none if in dropbox mode
			noti = NotificationService.NOTI_NONE;
		}
		else
		{
			// read the notification options
			String notification = params.getString("notify");
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("o".equals(notification))
			{
				noti = NotificationService.NOTI_OPTIONAL;
			}
		}
		item.setNotification(noti);

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			Iterator groupIt = metadataGroups.iterator();
			while(groupIt.hasNext())
			{
				MetadataGroup group = (MetadataGroup) groupIt.next();
				if(group.isShowing())
				{
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String propname = prop.getFullname();
						if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
						{
							int year = 0;
							int month = 0;
							int day = 0;
							int hour = 0;
							int minute = 0;
							int second = 0;
							int millisecond = 0;
							String ampm = "";

							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								year = params.getInt(propname + "_year", year);
								month = params.getInt(propname + "_month", month);
								day = params.getInt(propname + "_day", day);


							}
							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								hour = params.getInt(propname + "_hour", hour);
								minute = params.getInt(propname + "_minute", minute);
								second = params.getInt(propname + "_second", second);
								millisecond = params.getInt(propname + "_millisecond", millisecond);
								ampm = params.getString(propname + "_ampm").trim();

								if("pm".equalsIgnoreCase("ampm"))
								{
									if(hour < 12)
									{
										hour += 12;
									}
								}
								else if(hour == 12)
								{
									hour = 0;
								}
							}
							if(hour > 23)
							{
								hour = hour % 24;
								day++;
							}

							Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
							item.setMetadataItem(propname,value);

						}
						else
						{

							String value = params.getString(propname);
							if(value != null)
							{
								item.setMetadataItem(propname, value);
							}
						}
					}
				}
			}
		}
		current_stack_frame.put(STATE_STACK_EDIT_ITEM, item);
		state.setAttribute(STATE_EDIT_ALERTS, alerts);

	}	// captureValues

	/**
	 * Retrieve from an html form all the values needed to create a new resource
	 * @param item The ChefEditItem object in which the values are temporarily stored.
	 * @param index The index of the item (used as a suffix in the name of the form element)
	 * @param state
	 * @param params
	 * @param markMissing Indicates whether to mark required elements if they are missing.
	 * @return
	 */
	public static Set captureValues(ChefEditItem item, int index, SessionState state, ParameterParser params, boolean markMissing)
	{
		Map current_stack_frame = peekAtStack(state);

		Set item_alerts = new HashSet();
		boolean blank_entry = true;
		item.clearMissing();

		String name = params.getString("name" + index);
		if(name == null || name.trim().equals(""))
		{
			if(markMissing)
			{
				item_alerts.add(rb.getString("titlenotnull"));
				item.setMissing("name");
			}
			item.setName("");
			// addAlert(state, rb.getString("titlenotnull"));
		}
		else
		{
			item.setName(name);
			blank_entry = false;
		}

		String description = params.getString("description" + index);
		if(description == null || description.trim().equals(""))
		{
			item.setDescription("");
		}
		else
		{
			item.setDescription(description);
			blank_entry = false;
		}

		item.setContentHasChanged(false);

		if(item.isFileUpload())
		{
			String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
			int max_bytes = 1024 * 1024;
			try
			{
				max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
			}
			catch(Exception e)
			{
				// if unable to parse an integer from the value
				// in the properties file, use 1 MB as a default
				max_file_size_mb = "1";
				max_bytes = 1024 * 1024;
			}
			/*
			 // params.getContentLength() returns m_req.getContentLength()
			if(params.getContentLength() >= max_bytes)
			{
				item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else
			*/
			{
				// check for file replacement
				FileItem fileitem = null;
				try
				{
					fileitem = params.getFileItem("fileName" + index);
				}
				catch(Exception e)
				{
					// this is an error in Firefox, Mozilla and Netscape
					// "The user didn't select a file to upload!"
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}
				if(fileitem == null)
				{
					// "The user submitted a file to upload but it was too big!"
					item_alerts.clear();
					item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
					item.setMissing("fileName");
				}
				else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
				{
					if(item.getContent() == null || item.getContent().length <= 0)
					{
						// "The user submitted the form, but didn't select a file to upload!"
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}
				else if (fileitem.getFileName().length() > 0)
				{
					String filename = Validator.getFileName(fileitem.getFileName());
					byte[] bytes = fileitem.get();
					String contenttype = fileitem.getContentType();

					if(bytes.length >= max_bytes)
					{
						item_alerts.clear();
						item_alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
						item.setMissing("fileName");
					}
					else if(bytes.length > 0)
					{
						item.setContent(bytes);
						item.setContentHasChanged(true);
						item.setMimeType(contenttype);
						item.setFilename(filename);
						blank_entry = false;
					}
					else
					{
						item_alerts.add(rb.getString("choosefile") + " " + (index + 1) + ". ");
						item.setMissing("fileName");
					}
				}

			}
		}
		else if(item.isPlaintext())
		{
			// check for input from editor (textarea)
			String content = params.getString("content" + index);
			if(content != null)
			{
				item.setContentHasChanged(true);
				item.setContent(content);
				blank_entry = false;
			}
			item.setMimeType(MIME_TYPE_DOCUMENT_PLAINTEXT);
		}
		else if(item.isHtml())
		{
			// check for input from editor (textarea)
			String content = params.getCleanString("content" + index);
			StringBuffer alertMsg = new StringBuffer();
			content = FormattedText.processHtmlDocument(content, alertMsg);
			if (alertMsg.length() > 0)
			{
				item_alerts.add(alertMsg.toString());
			}
			if(content != null && !content.equals(""))
			{
				item.setContent(content);
				item.setContentHasChanged(true);
				blank_entry = false;
			}
			item.setMimeType(MIME_TYPE_DOCUMENT_HTML);
		}
		else if(item.isUrl())
		{
			item.setMimeType(ResourceProperties.TYPE_URL);
			String url = params.getString("Url" + index);
			if(url == null || url.trim().equals(""))
			{
				item.setFilename("");
				item_alerts.add(rb.getString("specifyurl"));
				item.setMissing("Url");
			}
			else
			{
				item.setFilename(url);
				blank_entry = false;
				// is protocol supplied and, if so, is it recognized?
				try
				{
					// check format of input
					URL u = new URL(url);
				}
				catch (MalformedURLException e1)
				{
					try
					{
						// if URL did not validate, check whether the problem was an
						// unrecognized protocol, and accept input if that's the case.
						Pattern pattern = Pattern.compile("\\s*([a-zA-Z0-9]+)://([^\\n]+)");
						Matcher matcher = pattern.matcher(url);
						if(matcher.matches())
						{
							URL test = new URL("http://" + matcher.group(2));
						}
						else
						{
							url = "http://" + url;
							URL test = new URL(url);
							item.setFilename(url);
						}
					}
					catch (MalformedURLException e2)
					{
						// invalid url
						item_alerts.add(rb.getString("validurl"));
						item.setMissing("Url");
					}
				}
			}
		}
		if(item.isFileUpload() || item.isHtml() || item.isPlaintext())
		{
			BasicRightsAssignment rightsObj = item.getRights();
			rightsObj.captureValues(params);
			
			boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());
			
			if(usingCreativeCommons)
			{
				String ccOwnership = params.getString("ccOwnership" + index);
				if(ccOwnership != null)
				{
					item.setRightsownership(ccOwnership);
				}
				String ccTerms = params.getString("ccTerms" + index);
				if(ccTerms != null)
				{
					item.setLicense(ccTerms);
				}
				String ccCommercial = params.getString("ccCommercial" + index);
				if(ccCommercial != null)
				{
					item.setAllowCommercial(ccCommercial);
				}
				String ccModification = params.getString("ccModification" + index);
				if(ccCommercial != null)
				{
					item.setAllowModifications(ccModification);
				}
				String ccRightsYear = params.getString("ccRightsYear" + index);
				if(ccRightsYear != null)
				{
					item.setRightstyear(ccRightsYear);
				}
				String ccRightsOwner = params.getString("ccRightsOwner" + index);
				if(ccRightsOwner != null)
				{
					item.setRightsowner(ccRightsOwner);
				}

				/*
				ccValues.ccOwner = new Array();
				ccValues.myRights = new Array();
				ccValues.otherRights = new Array();
				ccValues.ccCommercial = new Array();
				ccValues.ccModifications = new Array();
				ccValues.ccRightsYear = new Array();
				ccValues.ccRightsOwner = new Array();
				*/
			}
			else
			{
				// check for copyright status
				// check for copyright info
				// check for copyright alert
	
				String copyrightStatus = StringUtil.trimToNull(params.getString ("copyright" + index));
				String copyrightInfo = StringUtil.trimToNull(params.getCleanString ("newcopyright" + index));
				String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert" + index));
	
				if (copyrightStatus != null)
				{
					if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyrightStatus.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
					{
						if (copyrightInfo != null)
						{
							item.setCopyrightInfo( copyrightInfo );
						}
						else
						{
							item_alerts.add(rb.getString("specifycp2"));
							// addAlert(state, rb.getString("specifycp2"));
						}
					}
					else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyrightStatus.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
					{
						item.setCopyrightInfo((String) state.getAttribute (STATE_MY_COPYRIGHT));
					}
	
					item.setCopyrightStatus( copyrightStatus );
				}
				item.setCopyrightAlert(copyrightAlert != null);
			}

		}

		if(!  RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			String hidden = params.getString("hidden" + index);
			String use_start_date = params.getString("use_start_date" + index);
			String use_end_date = params.getString("use_end_date" + index);
			String release_month = params.getString("release_month" + index);
			String release_day = params.getString("release_day" + index);
			String release_year = params.getString("release_year" + index);
			String release_hour = params.getString("release" + index + "_hour");
			String release_min = params.getString("release" + index + "_minute");
			String release_ampm = params.getString("release" + index + "_ampm");
			
			String retract_month = params.getString("retract_month" + index);
			String retract_day = params.getString("retract_day" + index);
			String retract_year = params.getString("retract_year" + index);
			String retract_hour = params.getString("retract" + index + "_hour");
			String retract_min = params.getString("retract" + index + "_minute");
			String retract_ampm = params.getString("retract" + index + "_ampm");
			
			try
			{
				int begin_year = Integer.parseInt(release_year);
				int begin_month = Integer.parseInt(release_month);
				int begin_day = Integer.parseInt(release_day);
				int begin_hour = Integer.parseInt(release_hour);
				int begin_min = Integer.parseInt(release_min);
				if("pm".equals(release_ampm))
				{
					begin_hour += 12;
				}
				else if(begin_hour == 12)
				{
					begin_hour = 0;
				}
				Time releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
				item.setReleaseDate(releaseDate);
				
				int end_year = Integer.parseInt(retract_year);
				int end_month = Integer.parseInt(retract_month);
				int end_day = Integer.parseInt(retract_day);
				int end_hour = Integer.parseInt(retract_hour);
				int end_min = Integer.parseInt(retract_min);
				if("pm".equals(retract_ampm))
				{
					end_hour += 12;
				}
				else if(begin_hour == 12)
				{
					end_hour = 0;
				}
				Time retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
				item.setRetractDate(retractDate);
	
				item.setHidden(Boolean.TRUE.toString().equalsIgnoreCase(hidden));
				item.setUseReleaseDate(Boolean.TRUE.toString().equalsIgnoreCase(use_start_date));
				item.setUseRetractDate(Boolean.TRUE.toString().equalsIgnoreCase(use_end_date));
			}
			catch(NumberFormatException e)
			{
				// no values retrieved from date widget, or values are not numbers
			}
			
			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
			if(preventPublicDisplay == null)
			{
				preventPublicDisplay = Boolean.FALSE;
				state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
			}
			
			String access_mode = params.getString("access_mode" + index);
			
			if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
			{
				// we inherit more than one group and must check whether group access changes at this item
				String[] access_groups = params.getStrings("access_groups" + index);
				
				SortedSet new_groups = new TreeSet();
				if(access_groups != null)
				{
					new_groups.addAll(Arrays.asList(access_groups));
				}
				new_groups = item.convertToRefs(new_groups);
				
				Collection inh_grps = item.getInheritedGroupRefs();
				boolean groups_are_inherited = (new_groups.size() == inh_grps.size()) && inh_grps.containsAll(new_groups);
				
				if(groups_are_inherited)
				{
					new_groups.clear();
					item.setEntityGroupRefs(new_groups);
					item.setAccess(AccessMode.INHERITED.toString());
				}
				else
				{
					item.setEntityGroupRefs(new_groups);
					item.setAccess(AccessMode.GROUPED.toString());
				}
				
				item.setPubview(false);
			}
			else if(PUBLIC_ACCESS.equals(access_mode))
			{
				if(! preventPublicDisplay.booleanValue() && ! item.isPubviewInherited())
				{
					item.setPubview(true);
					item.setAccess(AccessMode.INHERITED.toString());
				}
			}
			else if(AccessMode.INHERITED.toString().equals(access_mode) )
			{
				item.setAccess(AccessMode.INHERITED.toString());
				item.clearGroups();
				item.setPubview(false);
			}
		}

		int noti = NotificationService.NOTI_NONE;
		// %%STATE_MODE_RESOURCES%%
		if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase((String) state.getAttribute(STATE_MODE_RESOURCES)))
		{
			// set noti to none if in dropbox mode
			noti = NotificationService.NOTI_NONE;
		}
		else
		{
			// read the notification options
			String notification = params.getString("notify" + index);
			if ("r".equals(notification))
			{
				noti = NotificationService.NOTI_REQUIRED;
			}
			else if ("o".equals(notification))
			{
				noti = NotificationService.NOTI_OPTIONAL;
			}
		}
		
		item.setNotification(noti);

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			Iterator groupIt = metadataGroups.iterator();
			while(groupIt.hasNext())
			{
				MetadataGroup group = (MetadataGroup) groupIt.next();
				if(item.isGroupShowing(group.getName()))
				{
					Iterator propIt = group.iterator();
					while(propIt.hasNext())
					{
						ResourcesMetadata prop = (ResourcesMetadata) propIt.next();
						String propname = prop.getFullname();
						if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
						{
							int year = 0;
							int month = 0;
							int day = 0;
							int hour = 0;
							int minute = 0;
							int second = 0;
							int millisecond = 0;
							String ampm = "";

							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								year = params.getInt(propname + "_" + index + "_year", year);
								month = params.getInt(propname + "_" + index + "_month", month);
								day = params.getInt(propname + "_" + index + "_day", day);
							}
							if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
								prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
							{
								hour = params.getInt(propname + "_" + index + "_hour", hour);
								minute = params.getInt(propname + "_" + index + "_minute", minute);
								second = params.getInt(propname + "_" + index + "_second", second);
								millisecond = params.getInt(propname + "_" + index + "_millisecond", millisecond);
								ampm = params.getString(propname + "_" + index + "_ampm").trim();

								if("pm".equalsIgnoreCase(ampm))
								{
									if(hour < 12)
									{
										hour += 12;
									}
								}
								else if(hour == 12)
								{
									hour = 0;
								}
							}
							if(hour > 23)
							{
								hour = hour % 24;
								day++;
							}

							Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
							item.setMetadataItem(propname,value);

						}
						else
						{
							String value = params.getString(propname + "_" + index);
							if(value != null)
							{
								item.setMetadataItem(propname, value);
							}
						}
					}
				}
			}
		}
		item.markAsBlank(blank_entry);

		return item_alerts;

	}

	/**
	 * Retrieve values for one or more items from create context.  Create context contains up to ten items at a time
	 * all of the same type (folder, file, text document, etc).  This method retrieves the data
	 * apppropriate to the type and updates the values of the ChefEditItem objects stored as the STATE_STACK_CREATE_ITEMS
	 * attribute in state. If the third parameter is "true", missing/incorrect user inputs will generate error messages
	 * and attach flags to the input elements.
	 * @param state
	 * @param params
	 * @param markMissing Should this method generate error messages and add flags for missing/incorrect user inputs?
	 */
	protected static void captureMultipleValues(SessionState state, ParameterParser params, boolean markMissing)
	{
		Map current_stack_frame = peekAtStack(state);
		Integer number = (Integer) current_stack_frame.get(STATE_STACK_CREATE_NUMBER);
		if(number == null)
		{
			number = (Integer) state.getAttribute(STATE_CREATE_NUMBER);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}
		if(number == null)
		{
			number = new Integer(1);
			current_stack_frame.put(STATE_STACK_CREATE_NUMBER, number);
		}

		List new_items = (List) current_stack_frame.get(STATE_STACK_CREATE_ITEMS);
		if(new_items == null)
		{
			String collectionId = params.getString("collectionId");
			String defaultCopyrightStatus = (String) state.getAttribute(DEFAULT_COPYRIGHT);
			if(defaultCopyrightStatus == null || defaultCopyrightStatus.trim().equals(""))
			{
				defaultCopyrightStatus = ServerConfigurationService.getString("default.copyright");
				state.setAttribute(DEFAULT_COPYRIGHT, defaultCopyrightStatus);
			}

			String itemType = (String) current_stack_frame.get(STATE_STACK_CREATE_TYPE);
			if(itemType == null || itemType.trim().equals(""))
			{
				itemType = (String) state.getAttribute(STATE_CREATE_TYPE);
				if(itemType == null || itemType.trim().equals(""))
				{
					itemType = TYPE_UPLOAD;
				}
				current_stack_frame.put(STATE_STACK_CREATE_TYPE, itemType);
			}
			
			String encoding = (String) state.getAttribute(STATE_ENCODING);

			Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);

			Time defaultRetractDate = (Time) state.getAttribute(STATE_DEFAULT_RETRACT_TIME);
			if(defaultRetractDate == null)
			{
				defaultRetractDate = TimeService.newTime();
				state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractDate);
			}

			new_items = newEditItems(collectionId, itemType, encoding, defaultCopyrightStatus, preventPublicDisplay.booleanValue(), defaultRetractDate, CREATE_MAX_ITEMS);
			current_stack_frame.put(STATE_STACK_CREATE_ITEMS, new_items);
		}

		Set alerts = (Set) state.getAttribute(STATE_CREATE_ALERTS);
		if(alerts == null)
		{
			alerts = new HashSet();
			state.setAttribute(STATE_CREATE_ALERTS, alerts);
		}
		int actualCount = 0;
		Set first_item_alerts = null;

		String max_file_size_mb = (String) state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1024 * 1024;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
		}

		/*
		// params.getContentLength() returns m_req.getContentLength()
		if(params.getContentLength() > max_bytes)
		{
			alerts.add(rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			state.setAttribute(STATE_CREATE_ALERTS, alerts);

			return;
		}
		*/
		for(int i = 0; i < number.intValue(); i++)
		{
			ChefEditItem item = (ChefEditItem) new_items.get(i);
			Set item_alerts = captureValues(item, i, state, params, markMissing);
			if(i == 0)
			{
				first_item_alerts = item_alerts;
			}
			else if(item.isBlank())
			{
				item.clearMissing();
			}
			if(! item.isBlank())
			{
				alerts.addAll(item_alerts);
				actualCount ++;
			}
		}
		if(actualCount > 0)
		{
			ChefEditItem item = (ChefEditItem) new_items.get(0);
			if(item.isBlank())
			{
				item.clearMissing();
			}
		}
		else if(markMissing)
		{
			alerts.addAll(first_item_alerts);
		}
		state.setAttribute(STATE_CREATE_ALERTS, alerts);
		current_stack_frame.put(STATE_STACK_CREATE_ACTUAL_COUNT, Integer.toString(actualCount));

	}	// captureMultipleValues

	protected static void capturePropertyValues(ParameterParser params, ChefEditItem item, List properties)
	{
		// use the item's properties if they're not supplied
		if(properties == null)
		{
			properties = item.getProperties();
		}
		// if max cardinality > 1, value is a list (Iterate over members of list)
		// else value is an object, not a list

		// if type is nested, object is a Map (iterate over name-value pairs for the properties of the nested object)
		// else object is type to store value, usually a string or a date/time

		Iterator it = properties.iterator();
		while(it.hasNext())
		{
			ResourcesMetadata prop = (ResourcesMetadata) it.next();
			String propname = prop.getDottedname();

			if(ResourcesMetadata.WIDGET_NESTED.equals(prop.getWidget()))
			{
				// do nothing
			}
			else if(ResourcesMetadata.WIDGET_BOOLEAN.equals(prop.getWidget()))
			{
				String value = params.getString(propname);
				if(value == null || Boolean.FALSE.toString().equals(value))
				{
					prop.setValue(0, Boolean.FALSE.toString());
				}
				else
				{
					prop.setValue(0, Boolean.TRUE.toString());
				}
			}
			else if(ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
			{
				int year = 0;
				int month = 0;
				int day = 0;
				int hour = 0;
				int minute = 0;
				int second = 0;
				int millisecond = 0;
				String ampm = "";

				if(prop.getWidget().equals(ResourcesMetadata.WIDGET_DATE) ||
					prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
				{
					year = params.getInt(propname + "_year", year);
					month = params.getInt(propname + "_month", month);
					day = params.getInt(propname + "_day", day);
				}
				if(prop.getWidget().equals(ResourcesMetadata.WIDGET_TIME) ||
					prop.getWidget().equals(ResourcesMetadata.WIDGET_DATETIME))
				{
					hour = params.getInt(propname + "_hour", hour);
					minute = params.getInt(propname + "_minute", minute);
					second = params.getInt(propname + "_second", second);
					millisecond = params.getInt(propname + "_millisecond", millisecond);
					ampm = params.getString(propname + "_ampm");

					if("pm".equalsIgnoreCase(ampm))
					{
						if(hour < 12)
						{
							hour += 12;
						}
					}
					else if(hour == 12)
					{
						hour = 0;
					}
				}
				if(hour > 23)
				{
					hour = hour % 24;
					day++;
				}

				Time value = TimeService.newTimeLocal(year, month, day, hour, minute, second, millisecond);
				prop.setValue(0, value);
			}
			else if(ResourcesMetadata.WIDGET_ANYURI.equals(prop.getWidget()))
			{
				String value = params.getString(propname);
				if(value != null && ! value.trim().equals(""))
				{
					Reference ref = EntityManager.newReference(ContentHostingService.getReference(value));
					prop.setValue(0, ref);
				}
			}
			else
			{
				String value = params.getString(propname);
				if(value != null)
				{
					prop.setValue(0, value);
				}
			}
		}

	}	// capturePropertyValues

	/**
	* Modify the properties
	*/
	public static void doSavechanges ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String flow = params.getString("flow").trim();

		if(flow == null || "cancel".equals(flow))
		{
			doCancel(data);
			return;
		}

		// get values from form and update STATE_STACK_EDIT_ITEM attribute in state
		captureValues(state, params);

		Map current_stack_frame = peekAtStack(state);

		ChefEditItem item = (ChefEditItem) current_stack_frame.get(STATE_STACK_EDIT_ITEM);

		if(flow.equals("showMetadata"))
		{
			doShow_metadata(data);
			return;
		}
		else if(flow.equals("hideMetadata"))
		{
			doHide_metadata(data);
			return;
		}
		else if(flow.equals("intentChanged"))
		{
			doToggle_intent(data);
			return;
		}
		else if(flow.equals("linkResource"))
		{
			// captureMultipleValues(state, params, false);
			createLink(data, state);
			//Map new_stack_frame = pushOnStack(state);
			//new_stack_frame.put(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
			state.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);

			return;
		}


		Set alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);

		if(alerts.isEmpty())
		{
			// populate the property list
			try
			{
				// get an edit
				ContentCollectionEdit cedit = null;
				ContentResourceEdit redit = null;
				GroupAwareEdit gedit = null;
				ResourcePropertiesEdit pedit = null;

				if(item.isFolder())
				{
					cedit = ContentHostingService.editCollection(item.getId());
					gedit = cedit;
					pedit = cedit.getPropertiesEdit();
				}
				else
				{
					redit = ContentHostingService.editResource(item.getId());
					gedit = redit;
					pedit = redit.getPropertiesEdit();
				}
				
				try
				{
					Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
					if(preventPublicDisplay == null)
					{
						preventPublicDisplay = Boolean.FALSE;
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
					}
					
					if(! preventPublicDisplay.booleanValue())
					{
						ContentHostingService.setPubView(gedit.getId(), item.isPubview());
					}
					
					if(! AccessMode.GROUPED.toString().equals(item.getAccess()) && AccessMode.GROUPED == gedit.getAccess())
					{
						gedit.clearGroupAccess();
					}
					else if(AccessMode.GROUPED.toString().equals(item.getAccess()) && ! item.getEntityGroupRefs().isEmpty())
					{
						gedit.setGroupAccess(item.getEntityGroupRefs());
					}
					else
					{
						gedit.clearGroupAccess();
					}
				}
				catch(InconsistentException e)
				{
					// TODO: Should this be reported to user??
					logger.debug("ResourcesAction.doSavechanges ***** InconsistentException changing groups ***** " + e.getMessage());
				}
								
				if(ContentHostingService.isAvailabilityEnabled())
				{
					Time releaseDate = null;
					Time retractDate = null;
					
					boolean hidden = item.isHidden();
					
					if(item.useReleaseDate())
					{
						releaseDate = item.getReleaseDate();
					}
					if(item.useRetractDate())
					{
						retractDate = item.getRetractDate();
					}
					gedit.setAvailability(hidden, releaseDate, retractDate);
				}
						
				if(item.isFolder())
				{
				}
				else
				{
					if(item.isUrl())
					{
						redit.setContent(item.getFilename().getBytes());
					}
					else if(item.contentHasChanged())
					{
						redit.setContentType(item.getMimeType());
						redit.setContent(item.getContent());
					}
					else if(item.contentTypeHasChanged())
					{
						redit.setContentType(item.getMimeType());
					}

					BasicRightsAssignment rightsObj = item.getRights();
					rightsObj.addResourceProperties(pedit);
										
					String copyright = StringUtil.trimToNull(params.getString ("copyright"));
					String newcopyright = StringUtil.trimToNull(params.getCleanString (NEW_COPYRIGHT));
					String copyrightAlert = StringUtil.trimToNull(params.getString("copyrightAlert"));
					if (copyright != null)
					{
						if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) != null && copyright.equals(state.getAttribute(COPYRIGHT_NEW_COPYRIGHT)))
						{
							if (newcopyright != null)
							{
								pedit.addProperty (ResourceProperties.PROP_COPYRIGHT, newcopyright);
							}
							else
							{
								alerts.add(rb.getString("specifycp2"));
								// addAlert(state, rb.getString("specifycp2"));
							}
						}
						else if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) != null && copyright.equals (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT)))
						{
							String mycopyright = (String) state.getAttribute (STATE_MY_COPYRIGHT);
							pedit.addProperty (ResourceProperties.PROP_COPYRIGHT, mycopyright);
						}

						pedit.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, copyright);
					}

					if (copyrightAlert != null)
					{
						pedit.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, copyrightAlert);
					}
					else
					{
						pedit.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
					}
				}

				if (!(item.isFolder() && (item.getId().equals ((String) state.getAttribute (STATE_HOME_COLLECTION_ID)))))
				{
					pedit.addProperty (ResourceProperties.PROP_DISPLAY_NAME, item.getName());
				}	// the home collection's title is not modificable

				pedit.addProperty (ResourceProperties.PROP_DESCRIPTION, item.getDescription());
				// deal with quota (collections only)
				if ((cedit != null) && item.canSetQuota())
				{
					if (item.hasQuota())
					{
						// set the quota
						pedit.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, item.getQuota());
					}
					else
					{
						// clear the quota
						pedit.removeProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
					}
				}

				List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);

				state.setAttribute(STATE_EDIT_ALERTS, alerts);
				saveMetadata(pedit, metadataGroups, item);
				alerts = (Set) state.getAttribute(STATE_EDIT_ALERTS);

				// commit the change
				if (cedit != null)
				{
					ContentHostingService.commitCollection(cedit);
					
				}
				else
				{
					ContentHostingService.commitResource(redit, item.getNotification());
				}

				current_stack_frame.put(STATE_STACK_EDIT_INTENT, INTENT_REVISE_FILE);

				Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
				if(preventPublicDisplay == null)
				{
					preventPublicDisplay = Boolean.FALSE;
					state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
				}
			}
			catch (TypeException e)
			{
				alerts.add(rb.getString("typeex") + " "  + item.getId());
				// addAlert(state," " + rb.getString("typeex") + " "  + item.getId());
			}
			catch (IdUnusedException e)
			{
				alerts.add(RESOURCE_NOT_EXIST_STRING);
				// addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}
			catch (PermissionException e)
			{
				alerts.add(rb.getString("notpermis10") + " " + item.getId());
				// addAlert(state, rb.getString("notpermis10") + " " + item.getId() + ". " );
			}
			catch (InUseException e)
			{
				alerts.add(rb.getString("someone") + " " + item.getId());
				// addAlert(state, rb.getString("someone") + " " + item.getId() + ". ");
			}
			catch (ServerOverloadException e)
			{
				alerts.add(rb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
				alerts.add(rb.getString("changing1") + " " + item.getId() + " " + rb.getString("changing2"));
				// addAlert(state, rb.getString("changing1") + " " + item.getId() + " " + rb.getString("changing2"));
			}
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doSavechanges ***** Unknown Exception ***** " + e.getMessage());
				logger.debug("ResourcesAction.doSavechanges ***** Unknown Exception ***** ", e);
				alerts.add(rb.getString("failed"));
			}
		}	// if - else

		if(alerts.isEmpty())
		{
			// modify properties sucessful
			String mode = (String) state.getAttribute(STATE_MODE);
			popFromStack(state);
			resetCurrentMode(state);
		}	//if-else
		else
		{
			Iterator alertIt = alerts.iterator();
			while(alertIt.hasNext())
			{
				String alert = (String) alertIt.next();
				addAlert(state, alert);
			}
			alerts.clear();
			state.setAttribute(STATE_EDIT_ALERTS, alerts);
			// state.setAttribute(STATE_CREATE_MISSING_ITEM, missing);
		}

	}	// doSavechanges

	/**
	 * @param pedit
	 * @param metadataGroups
	 * @param metadata
	 */
	private static void saveMetadata(ResourcePropertiesEdit pedit, List metadataGroups, ChefEditItem item)
	{
		if(metadataGroups != null && !metadataGroups.isEmpty())
		{
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(it.hasNext())
			{
				group = (MetadataGroup) it.next();
				Iterator props = group.iterator();
				while(props.hasNext())
				{
					ResourcesMetadata prop = (ResourcesMetadata) props.next();

					if(ResourcesMetadata.WIDGET_DATETIME.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_DATE.equals(prop.getWidget()) || ResourcesMetadata.WIDGET_TIME.equals(prop.getWidget()))
					{
						Time val = (Time)item.getMetadata().get(prop.getFullname());
						if(val != null)
						{
							pedit.addProperty(prop.getFullname(), val.toString());
						}
					}
					else
					{
						String val = (String) item.getMetadata().get(prop.getFullname());
						pedit.addProperty(prop.getFullname(), val);
					}
				}
			}
		}

	}

	/**
	 * @param data
	 */
	protected static void doToggle_intent(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		String intent = params.getString("intent");
		Map current_stack_frame = peekAtStack(state);
		current_stack_frame.put(STATE_STACK_EDIT_INTENT, intent);

	}	// doToggle_intent

	/**
	 * @param data
	 */
	public static void doHideOtherSites(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

	}


	/**
	 * @param data
	 */
	public static void doShowOtherSites(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.TRUE.toString());
	}

	/**
	 * @param data
	 */
	public static void doHide_metadata(RunData data)
	{
		ParameterParser params = data.getParameters ();
		String name = params.getString("metadataGroup");

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			boolean found = false;
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(!found && it.hasNext())
			{
				group = (MetadataGroup) it.next();
				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
			}
			if(found)
			{
				group.setShowing(false);
			}
		}

	}	// doHide_metadata

	/**
	 * @param data
	 */
	public static void doShow_metadata(RunData data)
	{
		ParameterParser params = data.getParameters ();
		String name = params.getString("metadataGroup");

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && ! metadataGroups.isEmpty())
		{
			boolean found = false;
			MetadataGroup group = null;
			Iterator it = metadataGroups.iterator();
			while(!found && it.hasNext())
			{
				group = (MetadataGroup) it.next();
				found = (name.equals(Validator.escapeUrl(group.getName())) || name.equals(group.getName()));
			}
			if(found)
			{
				group.setShowing(true);
			}
		}

	}	// doShow_metadata

	/**
	* Sort based on the given property
	*/
	public static void doSort ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String criteria = params.getString ("criteria");

		if (criteria.equals ("title"))
		{
			criteria = ResourceProperties.PROP_DISPLAY_NAME;
		}
		else if (criteria.equals ("size"))
		{
			criteria = ResourceProperties.PROP_CONTENT_LENGTH;
		}
		else if (criteria.equals ("created by"))
		{
			criteria = ResourceProperties.PROP_CREATOR;
		}
		else if (criteria.equals ("last modified"))
		{
			criteria = ResourceProperties.PROP_MODIFIED_DATE;
		}
		else if (criteria.equals("priority") && ContentHostingService.isSortByPriorityEnabled())
		{
			// if error, use title sort
			criteria = ResourceProperties.PROP_CONTENT_PRIORITY;
		}
		else
		{
			criteria = ResourceProperties.PROP_DISPLAY_NAME;
		}

		String sortBy_attribute = STATE_SORT_BY;
		String sortAsc_attribute = STATE_SORT_ASC;
		String comparator_attribute = STATE_LIST_VIEW_SORT;
		
		if(state.getAttribute(STATE_MODE).equals(MODE_REORDER))
		{
			sortBy_attribute = STATE_REORDER_SORT_BY;
			sortAsc_attribute = STATE_REORDER_SORT_ASC;
			comparator_attribute = STATE_REORDER_SORT;
		}
		// current sorting sequence
		String asc = NULL_STRING;
		if (!criteria.equals (state.getAttribute (sortBy_attribute)))
		{
			state.setAttribute (sortBy_attribute, criteria);
			asc = Boolean.TRUE.toString();
			state.setAttribute (sortAsc_attribute, asc);
		}
		else
		{
			// current sorting sequence
			asc = (String) state.getAttribute (sortAsc_attribute);

			//toggle between the ascending and descending sequence
			if (asc.equals (Boolean.TRUE.toString()))
			{
				asc = Boolean.FALSE.toString();
			}
			else
			{
				asc = Boolean.TRUE.toString();
			}
			state.setAttribute (sortAsc_attribute, asc);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			Comparator comparator = ContentHostingService.newContentHostingComparator(criteria, Boolean.getBoolean(asc));
			state.setAttribute(comparator_attribute, comparator);
			
			// sort sucessful
			// state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSort

	/**
	* Sort based on the given property
	*/
	public static void doReorder ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		
		boolean isPrioritySortEnabled = ContentHostingService.isSortByPriorityEnabled();


		String folderId = params.getString ("folderId");
		if(folderId == null)
		{
			addAlert(state, "error");
		}
		
		String sortBy = (String) state.getAttribute(STATE_REORDER_SORT_BY);
		if(sortBy == null)
		{
			sortBy = ResourceProperties.PROP_CONTENT_PRIORITY;
			state.setAttribute(STATE_REORDER_SORT_BY, sortBy);
		}
		String sortedAsc = (String) state.getAttribute (STATE_REORDER_SORT_ASC);
		if(sortedAsc == null)
		{
			sortedAsc = Boolean.TRUE.toString();
			state.setAttribute(STATE_REORDER_SORT_ASC, sortedAsc);
		}

		Comparator comparator = ContentHostingService.newContentHostingComparator(sortBy, Boolean.getBoolean(sortedAsc));
		state.setAttribute(STATE_REORDER_SORT, comparator);

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute(STATE_REORDER_FOLDER, folderId);
			state.setAttribute (STATE_MODE, MODE_REORDER);

		}	// if-else

	}	// doReorder

	/**
	* Sort based on the given property
	*/
	public static void doSaveOrder ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		String flow = params.getString("flow");
		
		if("save".equalsIgnoreCase(flow))
		{
			String folderId = params.getString ("folderId");
			if(folderId == null)
			{
				// TODO: log error
				// TODO: move strings to rb
				addAlert(state, "Unable to complete Sort");
			}
			else
			{
				try
				{
					ContentCollectionEdit collection = ContentHostingService.editCollection(folderId);
					List memberIds = collection.getMembers();
					Map priorities = new Hashtable();
					Iterator it = memberIds.iterator();
					while(it.hasNext())
					{
						String memberId = (String) it.next();
						int position = params.getInt("position_" + Validator.escapeUrl(memberId));
						priorities.put(memberId, new Integer(position));
					}
					collection.setPriorityMap(priorities);
					
					ContentHostingService.commitCollection(collection);
					
					SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
					if(expandedCollections == null)
					{
						expandedCollections = (SortedSet) new TreeSet();
						state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
					}
					expandedCollections.add(folderId);
					
					Comparator comparator = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);
					Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
					if(expandedFolderSortMap == null)
					{
						expandedFolderSortMap = new Hashtable();
						state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
					}
					expandedFolderSortMap.put(folderId, comparator);
				}
				catch(IdUnusedException e)
				{
					// TODO: log error
					// TODO: move strings to rb
					addAlert(state, "Unable to complete Sort");
				}
				catch(TypeException e)
				{
					// TODO: log error
					// TODO: move strings to rb
					addAlert(state, "Unable to complete Sort");
				}
				catch(PermissionException e)
				{
					// TODO: log error
					// TODO: move strings to rb
					addAlert(state, "Unable to complete Sort");
				}
				catch(InUseException e)
				{
					// TODO: log error
					// TODO: move strings to rb
					addAlert(state, "Unable to complete Sort");
				}
			}
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_LIST);

		}	// if-else

	}	// doSaveOrder

	/**
	* set the state name to be "deletecofirm" if any item has been selected for deleting
	*/
	public void doDeleteconfirm ( RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		Set deleteIdSet  = new TreeSet();
		String[] deleteIds = data.getParameters ().getStrings ("selectedMembers");
		if (deleteIds == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile3"));
		}
		else
		{
			deleteIdSet.addAll(Arrays.asList(deleteIds));
			deleteItems(state, deleteIdSet);
		}

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_DELETE_CONFIRM);
			state.setAttribute(STATE_LIST_SELECTIONS, deleteIdSet);
		}


	}	// doDeleteconfirm

	/**
	 * @param state
	 * @param deleteIdSet
	 * @param deleteIds
	 */
	protected void deleteItems(SessionState state, Set deleteIdSet)
	{
		List deleteItems = new Vector();
		List notDeleteItems = new Vector();
		List nonEmptyFolders = new Vector();
		List roots = (List) state.getAttribute(STATE_COLLECTION_ROOTS);
		if(roots == null)
		{
			
		}
		Iterator rootIt = roots.iterator();
		while(rootIt.hasNext())
		{
			ChefBrowseItem root = (ChefBrowseItem) rootIt.next();

			List members = root.getMembers();
			Iterator memberIt = members.iterator();
			while(memberIt.hasNext())
			{
				ChefBrowseItem member = (ChefBrowseItem) memberIt.next();
				if(deleteIdSet.contains(member.getId()))
				{
					if(member.isFolder())
					{
						if(ContentHostingService.allowRemoveCollection(member.getId()))
						{
							deleteItems.add(member);
							if(! member.isEmpty())
							{
								nonEmptyFolders.add(member);
							}
						}
						else
						{
							notDeleteItems.add(member);
						}
					}
					else if(ContentHostingService.allowRemoveResource(member.getId()))
					{
						deleteItems.add(member);
					}
					else
					{
						notDeleteItems.add(member);
					}
				}
			}
		}

		if(! notDeleteItems.isEmpty())
		{
			String notDeleteNames = "";
			boolean first_item = true;
			Iterator notIt = notDeleteItems.iterator();
			while(notIt.hasNext())
			{
				ChefBrowseItem item = (ChefBrowseItem) notIt.next();
				if(first_item)
				{
					notDeleteNames = item.getName();
					first_item = false;
				}
				else if(notIt.hasNext())
				{
					notDeleteNames += ", " + item.getName();
				}
				else
				{
					notDeleteNames += " and " + item.getName();
				}
			}
			addAlert(state, rb.getString("notpermis14") + notDeleteNames);
		}


		/*
				//htripath-SAK-1712 - Set new collectionId as resources are not deleted under 'more' requirement.
				if(state.getAttribute(STATE_MESSAGE) == null){
				  String newCollectionId=ContentHostingService.getContainingCollectionId(currentId);
				  state.setAttribute(STATE_COLLECTION_ID, newCollectionId);
				}
		*/

		state.setAttribute (STATE_DELETE_ITEMS, deleteItems);
		state.setAttribute (STATE_DELETE_ITEMS_NOT_EMPTY, nonEmptyFolders);
	}

	/**
	 * @param state
	 * @param deleteIdSet
	 */
	protected void deleteItem(SessionState state, String itemId)
	{
		List deleteItems = new Vector();
		List notDeleteItems = new Vector();
		List nonEmptyFolders = new Vector();
		
		boolean isFolder = itemId.endsWith(Entity.SEPARATOR);
		
		try
		{
			ContentEntity entity = null;
			if(isFolder)
			{
				entity = ContentHostingService.getCollection(itemId);
			}
			else
			{
				entity = ContentHostingService.getResource(itemId);
			}
			
			ListItem member = new ListItem(entity);
			
			if(isFolder)
			{
				ContentCollection collection = (ContentCollection) entity;
				if(ContentHostingService.allowRemoveCollection(itemId))
				{
					deleteItems.add(member);
					if(collection.getMemberCount() > 0)
					{
						nonEmptyFolders.add(member);
					}
				}
				else
				{
					notDeleteItems.add(member);
				}
			}
			else if(ContentHostingService.allowRemoveResource(member.getId()))
			{
				deleteItems.add(member);
			}
			else
			{
				notDeleteItems.add(member);
			}
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			logger.warn("TypeException ", e);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			logger.warn("PermissionException ", e);
		}
		

		if(! notDeleteItems.isEmpty())
		{
			String notDeleteNames = "";
			boolean first_item = true;
			Iterator notIt = notDeleteItems.iterator();
			while(notIt.hasNext())
			{
				ListItem item = (ListItem) notIt.next();
				if(first_item)
				{
					notDeleteNames = item.getName();
					first_item = false;
				}
				else if(notIt.hasNext())
				{
					notDeleteNames += ", " + item.getName();
				}
				else
				{
					notDeleteNames += " and " + item.getName();
				}
			}
			addAlert(state, rb.getString("notpermis14") + notDeleteNames);
		}

		if(state.getAttribute(STATE_MESSAGE) == null)
		{
			state.setAttribute (STATE_DELETE_SET, deleteItems);
			state.setAttribute (STATE_NON_EMPTY_DELETE_SET, nonEmptyFolders);
		}
	}


	/**
	* set the state name to be "cut" if any item has been selected for cutting
	*/
	public void doCut ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String[] cutItems = data.getParameters ().getStrings ("selectedMembers");
		if (cutItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile5"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			Vector cutIdsVector = new Vector ();
			String nonCutIds = NULL_STRING;

			String cutId = NULL_STRING;
			for (int i = 0; i < cutItems.length; i++)
			{
				cutId = cutItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (cutId);
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					else
					{
						if (ContentHostingService.allowRemoveResource (cutId))
						{
							cutIdsVector.add (cutId);
						}
						else
						{
							nonCutIds = nonCutIds + " " + properties.getProperty (ResourceProperties.PROP_DISPLAY_NAME) + "; ";
						}
					}
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				if (nonCutIds.length ()>0)
				{
					addAlert(state, rb.getString("notpermis16") +" " + nonCutIds);
				}

				if (cutIdsVector.size ()>0)
				{
					state.setAttribute (STATE_CUT_FLAG, Boolean.TRUE.toString());
					if (((String) state.getAttribute (STATE_SELECT_ALL_FLAG)).equals (Boolean.TRUE.toString()))
					{
						state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());
					}

					Vector copiedIds = (Vector) state.getAttribute (STATE_COPIED_IDS);
					for (int i = 0; i < cutIdsVector.size (); i++)
					{
						String currentId = (String) cutIdsVector.elementAt (i);
						if ( copiedIds.contains (currentId))
						{
							copiedIds.remove (currentId);
						}
					}
					if (copiedIds.size ()==0)
					{
						state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
					}

					state.setAttribute (STATE_COPIED_IDS, copiedIds);

					state.setAttribute (STATE_CUT_IDS, cutIdsVector);
				}
			}
		}	// if-else

	}	// doCut

	/**
	* set the state name to be "copy" if any item has been selected for copying
	*/
	public void doCopy ( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		Vector copyItemsVector = new Vector ();

		String[] copyItems = data.getParameters ().getStrings ("selectedMembers");
		if (copyItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			String copyId = NULL_STRING;
			for (int i = 0; i < copyItems.length; i++)
			{
				copyId = copyItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (copyId);
					/*
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					*/
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.TRUE.toString());

				copyItemsVector.addAll(Arrays.asList(copyItems));
				ContentHostingService.eliminateDuplicates(copyItemsVector);
				state.setAttribute (STATE_COPIED_IDS, copyItemsVector);

			}	// if-else
		}	// if-else

	}	// doCopy

	/**
	* Handle user's selection of items to be moved.
	*/
	public void doMove ( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List moveItemsVector = new Vector();

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		String[] moveItems = data.getParameters ().getStrings ("selectedMembers");
		if (moveItems == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			String moveId = NULL_STRING;
			for (int i = 0; i < moveItems.length; i++)
			{
				moveId = moveItems[i];
				try
				{
					ResourceProperties properties = ContentHostingService.getProperties (moveId);
					/*
					if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
					{
						String alert = (String) state.getAttribute(STATE_MESSAGE);
						if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
						{
							addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
						}
					}
					*/
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("notpermis15"));
				}
				catch (IdUnusedException e)
				{
					addAlert(state,RESOURCE_NOT_EXIST_STRING);
				}	// try-catch
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_MOVE_FLAG, Boolean.TRUE.toString());

				moveItemsVector.addAll(Arrays.asList(moveItems));

				ContentHostingService.eliminateDuplicates(moveItemsVector);

				state.setAttribute (STATE_MOVED_IDS, moveItemsVector);

			}	// if-else
		}	// if-else

	}	// doMove


	/**
	 * If copy-flag is set to false, erase the copied-id's list and set copied flags to false
	 * in all the browse items.  If copied-id's list is empty, set copy-flag to false and set
	 * copied flags to false in all the browse items. If copy-flag is set to true and copied-id's
	 * list is not empty, update the copied flags of all browse items so copied flags for the
	 * copied items are set to true and all others are set to false.
	 */
	protected void setCopyFlags(SessionState state)
	{
		String copyFlag = (String) state.getAttribute(STATE_COPY_FLAG);
		List copyItemsVector = (List) state.getAttribute(STATE_COPIED_IDS);

		if(copyFlag == null)
		{
			copyFlag = Boolean.FALSE.toString();
			state.setAttribute(STATE_COPY_FLAG, copyFlag);
		}

		if(copyFlag.equals(Boolean.TRUE.toString()))
		{
			if(copyItemsVector == null)
			{
				copyItemsVector = new Vector();
				state.setAttribute(STATE_COPIED_IDS, copyItemsVector);
			}
			if(copyItemsVector.isEmpty())
			{
				state.setAttribute(STATE_COPY_FLAG, Boolean.FALSE.toString());
			}
		}
		else
		{
			copyItemsVector = new Vector();
			state.setAttribute(STATE_COPIED_IDS, copyItemsVector);
		}

		List roots = (List) state.getAttribute(STATE_COLLECTION_ROOTS);
		Iterator rootIt = roots.iterator();
		while(rootIt.hasNext())
		{
			ChefBrowseItem root = (ChefBrowseItem) rootIt.next();
			boolean root_copied = copyItemsVector.contains(root.getId());
			root.setCopied(root_copied);

			List members = root.getMembers();
			Iterator memberIt = members.iterator();
			while(memberIt.hasNext())
			{
				ChefBrowseItem member = (ChefBrowseItem) memberIt.next();
				boolean member_copied = copyItemsVector.contains(member.getId());
				member.setCopied(member_copied);
			}
		}
		// check -- jim
		state.setAttribute(STATE_COLLECTION_ROOTS, roots);

	}	// setCopyFlags

	/**
	* Expand all the collection resources.
	*/
	static public void doExpandall ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		// expansion actually occurs in getBrowseItems method.
		state.setAttribute(STATE_EXPAND_ALL_FLAG,  Boolean.TRUE.toString());
		state.setAttribute(STATE_NEED_TO_EXPAND_ALL, Boolean.TRUE.toString());

	}	// doExpandall

	/**
	* Unexpand all the collection resources
	*/
	public static void doUnexpandall ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, new TreeSet());
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new Hashtable());
		state.setAttribute(STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

	}	// doUnexpandall

	/**
	* Populate the state object, if needed - override to do something!
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData data)
	{
		super.initState(state, portlet, data);

		if(state.getAttribute(STATE_INITIALIZED) == null)
		{
			initCopyContext(state);
			initMoveContext(state);
		}

		initStateAttributes(state, portlet);

	}	// initState

	/**
	* Remove the state variables used internally, on the way out.
	*/
	static private void cleanupState(SessionState state)
	{
		state.removeAttribute(STATE_FROM_TEXT);
		state.removeAttribute(STATE_HAS_ATTACHMENT_BEFORE);
		state.removeAttribute(STATE_ATTACH_SHOW_DROPBOXES);
		state.removeAttribute(STATE_ATTACH_COLLECTION_ID);

		state.removeAttribute(COPYRIGHT_FAIRUSE_URL);
		state.removeAttribute(COPYRIGHT_NEW_COPYRIGHT);
		state.removeAttribute(COPYRIGHT_SELF_COPYRIGHT);
		state.removeAttribute(COPYRIGHT_TYPES);
		state.removeAttribute(DEFAULT_COPYRIGHT_ALERT);
		state.removeAttribute(DEFAULT_COPYRIGHT);
		state.removeAttribute(STATE_EXPANDED_COLLECTIONS);
		state.removeAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		state.removeAttribute(STATE_FILE_UPLOAD_MAX_SIZE);
		state.removeAttribute(NEW_COPYRIGHT_INPUT);
		state.removeAttribute(STATE_COLLECTION_ID);
		state.removeAttribute(STATE_COLLECTION_PATH);
		state.removeAttribute(STATE_CONTENT_SERVICE);
		state.removeAttribute(STATE_CONTENT_TYPE_IMAGE_SERVICE);
		//state.removeAttribute(STATE_STACK_EDIT_INTENT);
		state.removeAttribute(STATE_EXPAND_ALL_FLAG);
		state.removeAttribute(STATE_HELPER_NEW_ITEMS);
		state.removeAttribute(STATE_HELPER_CHANGED);
		state.removeAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
		state.removeAttribute(STATE_HOME_COLLECTION_ID);
		state.removeAttribute(STATE_LIST_SELECTIONS);
		state.removeAttribute(STATE_MY_COPYRIGHT);
		state.removeAttribute(STATE_NAVIGATION_ROOT);
		state.removeAttribute(STATE_PASTE_ALLOWED_FLAG);
		state.removeAttribute(STATE_SELECT_ALL_FLAG);
		state.removeAttribute(STATE_SHOW_ALL_SITES);
		state.removeAttribute(STATE_SITE_TITLE);
		state.removeAttribute(STATE_SORT_ASC);
		state.removeAttribute(STATE_SORT_BY);
		state.removeAttribute(STATE_INITIALIZED);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_HELPER);

	}	// cleanupState


	public static void initStateAttributes(SessionState state, VelocityPortlet portlet)
	{
		if (state.getAttribute (STATE_INITIALIZED) != null) return;

		if (state.getAttribute(STATE_FILE_UPLOAD_MAX_SIZE) == null)
		{
			state.setAttribute(STATE_FILE_UPLOAD_MAX_SIZE, ServerConfigurationService.getString("content.upload.max", "1"));
		}
		
//		for(int i = 0; i < 255; i++)
//		{
//			try {
//				if(i < 10)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/user/x00" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/user/x00" + i + "/\")");
//				}
//				else if(i < 100)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/user/x0" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/user/x0" + i + "/\")");
//				}
//				else 
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/user/x" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/user/x" + i + "/\")");
//				}
//			} catch (IdUsedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IdInvalidException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (PermissionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InconsistentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		for(int i = 0; i < 255; i++)
//		{
//			try {
//				if(i < 10)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/public/x00" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/public/x00" + i + "/\")");
//				}
//				else if(i < 100)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/public/x0" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/public/x0" + i + "/\")");
//				}
//				else 
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/public/x" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/public/x" + i + "/\")");
//				}
//			} catch (IdUsedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IdInvalidException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (PermissionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InconsistentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		for(int i = 0; i < 257; i++)
//		{
//			try {
//				if(i < 10)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/group/x00" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/group/x00" + i + "/\")");
//				}
//				else if(i < 100)
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/group/x0" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/group/x0" + i + "/\")");
//				}
//				else 
//				{
//					ContentCollectionEdit edit = ContentHostingService.addCollection("/group/x" + i + "/");
//					ContentHostingService.commitCollection(edit);
//					System.out.println("addCollection(\"/group/x" + i + "/\")");
//				}
//			} catch (IdUsedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IdInvalidException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (PermissionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InconsistentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//

		PortletConfig config = portlet.getPortletConfig();
		try
		{
			Integer size = new Integer(config.getInitParameter(PARAM_PAGESIZE));
			if(size == null || size.intValue() < 1)
			{
				size = new Integer(DEFAULT_PAGE_SIZE);
			}
			state.setAttribute(STATE_PAGESIZE, size);
		}
		catch(Exception any)
		{
			state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		}

		// state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, "");

		state.setAttribute (STATE_CONTENT_SERVICE, ContentHostingService.getInstance());
		state.setAttribute (STATE_CONTENT_TYPE_IMAGE_SERVICE, ContentTypeImageService.getInstance());
		state.setAttribute(STATE_RESOURCES_TYPE_REGISTRY, ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry"));

		TimeBreakdown timeBreakdown = (TimeService.newTime()).breakdownLocal ();
		String mycopyright = COPYRIGHT_SYMBOL + " " + timeBreakdown.getYear () +", " + UserDirectoryService.getCurrentUser().getDisplayName () + ". All Rights Reserved. ";
		state.setAttribute (STATE_MY_COPYRIGHT, mycopyright);

		if(state.getAttribute(STATE_MODE) == null)
		{
			state.setAttribute (STATE_MODE, MODE_LIST);
			state.setAttribute (STATE_FROM, NULL_STRING);
		}
		state.setAttribute (STATE_SORT_BY, ResourceProperties.PROP_DISPLAY_NAME);

		state.setAttribute (STATE_SORT_ASC, Boolean.TRUE.toString());
		
		state.setAttribute(STATE_DEFAULT_SORT, ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true));

		state.setAttribute (STATE_SELECT_ALL_FLAG, Boolean.FALSE.toString());

		state.setAttribute (STATE_EXPAND_ALL_FLAG, Boolean.FALSE.toString());

		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		state.setAttribute (STATE_COLLECTION_PATH, new Vector ());

		// %%STATE_MODE_RESOURCES%%
		// In helper mode, calling tool should set attribute STATE_MODE_RESOURCES
		String resources_mode = (String) state.getAttribute(STATE_MODE_RESOURCES);
		if(resources_mode == null)
		{
			// get resources mode from tool registry
			resources_mode = portlet.getPortletConfig().getInitParameter("resources_mode");
			if(resources_mode != null)
			{
				state.setAttribute(STATE_MODE_RESOURCES, resources_mode);
			}
		}

		boolean show_other_sites = false;
		if(RESOURCES_MODE_HELPER.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.helper", SHOW_ALL_SITES_IN_FILE_PICKER);
		}
		else if(RESOURCES_MODE_DROPBOX.equals(resources_mode))
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.dropbox", SHOW_ALL_SITES_IN_DROPBOX);
		}
		else
		{
			show_other_sites = ServerConfigurationService.getBoolean("resources.show_all_collections.tool", SHOW_ALL_SITES_IN_RESOURCES);
		}
		
		/** set attribute for the maximum size at which the resources tool will expand a collection. */
		int expandableFolderSizeLimit = ServerConfigurationService.getInt("resources.expanded_folder_size_limit", EXPANDABLE_FOLDER_SIZE_LIMIT);
		state.setAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT, new Integer(expandableFolderSizeLimit));
		
		/** This attribute indicates whether "Other Sites" twiggle should show */
		state.setAttribute(STATE_SHOW_ALL_SITES, Boolean.toString(show_other_sites));
		/** This attribute indicates whether "Other Sites" twiggle should be open */
		state.setAttribute(STATE_SHOW_OTHER_SITES, Boolean.FALSE.toString());

		// set the home collection to the parameter, if present, or the default if not
		String home = StringUtil.trimToNull(portlet.getPortletConfig().getInitParameter("home"));
		state.setAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME, home);
		if ((home == null) || (home.length() == 0))
		{
			// no home set, see if we are in dropbox mode
			if (RESOURCES_MODE_DROPBOX.equalsIgnoreCase(resources_mode))
			{
				home = ContentHostingService.getDropboxCollection();

				// if it came back null, we will pretend not to be in dropbox mode
				if (home != null)
				{
					state.setAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME, ContentHostingService.getDropboxDisplayName());

					// create/update the collection of folders in the dropbox
					ContentHostingService.createDropboxCollection();
				}
			}

			// if we still don't have a home,
			if ((home == null) || (home.length() == 0))
			{
				home = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());

				// TODO: what's the 'name' of the context? -ggolden
				// we'll need this to create the home collection if needed
				state.setAttribute (STATE_HOME_COLLECTION_DISPLAY_NAME, ToolManager.getCurrentPlacement().getContext()
						/*SiteService.getSiteDisplay(ToolManager.getCurrentPlacement().getContext()) */);
			}
		}
		state.setAttribute (STATE_HOME_COLLECTION_ID, home);
		state.setAttribute (STATE_COLLECTION_ID, home);
		state.setAttribute (STATE_NAVIGATION_ROOT, home);

		// state.setAttribute (STATE_COLLECTION_ID, state.getAttribute (STATE_HOME_COLLECTION_ID));

		if (state.getAttribute(STATE_SITE_TITLE) == null)
		{
			String title = "";
			try
			{
				title = ((Site) SiteService.getSite(ToolManager.getCurrentPlacement().getContext())).getTitle();
			}
			catch (IdUnusedException e)
			{	// ignore
			}
			state.setAttribute(STATE_SITE_TITLE, title);
		}

		SortedSet expandedCollections = new TreeSet();
		//expandedCollections.add (state.getAttribute (STATE_HOME_COLLECTION_ID));
		state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, new Hashtable());
		
		if(state.getAttribute(STATE_USING_CREATIVE_COMMONS) == null)
		{
			String usingCreativeCommons = ServerConfigurationService.getString("copyright.use_creative_commons");
			if( usingCreativeCommons != null && usingCreativeCommons.equalsIgnoreCase(Boolean.TRUE.toString()))
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.TRUE.toString());
			}
			else
			{
				state.setAttribute(STATE_USING_CREATIVE_COMMONS, Boolean.FALSE.toString());
			}
		}

		if (state.getAttribute(COPYRIGHT_TYPES) == null)
		{
			if (ServerConfigurationService.getStrings("copyrighttype") != null)
			{
				state.setAttribute(COPYRIGHT_TYPES, new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("copyrighttype"))));
			}
		}

		if (state.getAttribute(DEFAULT_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright") != null)
			{
				state.setAttribute(DEFAULT_COPYRIGHT, ServerConfigurationService.getString("default.copyright"));
			}
		}

		if (state.getAttribute(DEFAULT_COPYRIGHT_ALERT) == null)
		{
			if (ServerConfigurationService.getString("default.copyright.alert") != null)
			{
				state.setAttribute(DEFAULT_COPYRIGHT_ALERT, ServerConfigurationService.getString("default.copyright.alert"));
			}
		}

		if (state.getAttribute(NEW_COPYRIGHT_INPUT) == null)
		{
			if (ServerConfigurationService.getString("newcopyrightinput") != null)
			{
				state.setAttribute(NEW_COPYRIGHT_INPUT, ServerConfigurationService.getString("newcopyrightinput"));
			}
		}

		if (state.getAttribute(COPYRIGHT_FAIRUSE_URL) == null)
		{
			if (ServerConfigurationService.getString("fairuse.url") != null)
			{
				state.setAttribute(COPYRIGHT_FAIRUSE_URL, ServerConfigurationService.getString("fairuse.url"));
			}
		}

		if (state.getAttribute(COPYRIGHT_SELF_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.own") != null)
			{
				state.setAttribute(COPYRIGHT_SELF_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.own"));
			}
		}

		if (state.getAttribute(COPYRIGHT_NEW_COPYRIGHT) == null)
		{
			if (ServerConfigurationService.getString("copyrighttype.new") != null)
			{
				state.setAttribute(COPYRIGHT_NEW_COPYRIGHT, ServerConfigurationService.getString("copyrighttype.new"));
			}
		}

		// get resources mode from tool registry
		String optional_properties = portlet.getPortletConfig().getInitParameter("optional_properties");
		if(optional_properties != null && "true".equalsIgnoreCase(optional_properties))
		{
			initMetadataContext(state);
		}
		
		state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.FALSE);
		String[] siteTypes = ServerConfigurationService.getStrings("prevent.public.resources");
		String siteType = null;
		Site site;
		try
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			siteType = site.getType();
			if(siteTypes != null)
			{
				for(int i = 0; i < siteTypes.length; i++)
				{
					if ((StringUtil.trimToNull(siteTypes[i])).equals(siteType))
					{
						state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, Boolean.TRUE);
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			// allow public display
		}
		catch(NullPointerException e)
		{
			// allow public display
		}
		
		Time defaultRetractTime = TimeService.newTime(TimeService.newTime().getTime() + ONE_WEEK);
		Time guess = null;
		Time now = TimeService.newTime();
		if(siteType != null && siteType.equalsIgnoreCase("course"))
		{
			List terms = CourseManagementService.getTerms();
			boolean found = false;
			Term term = null;
			Iterator termIt = terms.iterator();
			while(termIt.hasNext())
			{
				term = (Term) termIt.next();
				if(term.getEndTime().after(now))
				{
					if(guess == null || term.getEndTime().before(guess))
					{
						guess = term.getEndTime();
					}
				}
			}
			if(guess != null)
			{
				defaultRetractTime = guess;
			}
		}
		state.setAttribute(STATE_DEFAULT_RETRACT_TIME, defaultRetractTime);
		
		if(state.getAttribute(STATE_LIST_PREFERENCE) == null)
		{
			state.setAttribute(STATE_LIST_PREFERENCE, LIST_HIERARCHY);
		}
			
		state.setAttribute (STATE_INITIALIZED, Boolean.TRUE.toString());

	}

	/**
	* Setup our observer to be watching for change events for the collection
 	*/
 	private void updateObservation(SessionState state, String peid)
 	{
// 		ContentObservingCourier observer = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//
// 		// the delivery location for this tool
// 		String deliveryId = clientWindowId(state, peid);
// 		observer.setDeliveryId(deliveryId);
	}

	/**
	 * Add additional resource pattern to the observer
	 *@param pattern The pattern value to be added
	 *@param state The state object
	 */
	private static void addObservingPattern(String pattern, SessionState state)
	{
//		// get the observer and add the pattern
//		ContentObservingCourier o = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//		o.addResourcePattern(ContentHostingService.getReference(pattern));
//
//		// add it back to state
//		state.setAttribute(STATE_OBSERVER, o);

	}	// addObservingPattern

	/**
	 * Remove a resource pattern from the observer
	 *@param pattern The pattern value to be removed
	 *@param state The state object
	 */
	private static void removeObservingPattern(String pattern, SessionState state)
	{
//		// get the observer and remove the pattern
//		ContentObservingCourier o = (ContentObservingCourier) state.getAttribute(STATE_OBSERVER);
//		o.removeResourcePattern(ContentHostingService.getReference(pattern));
//
//		// add it back to state
//		state.setAttribute(STATE_OBSERVER, o);

	}	// removeObservingPattern

	/**
	* initialize the copy context
	*/
	private static void initCopyContext (SessionState state)
	{
		state.setAttribute (STATE_COPIED_IDS, new Vector ());

		state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent

	/**
	* initialize the copy context
	*/
	private static void initMoveContext (SessionState state)
	{
		state.setAttribute (STATE_MOVED_IDS, new Vector ());

		state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());

	}	// initCopyContent


	/**
	* initialize the cut context
	*/
	private void initCutContext (SessionState state)
	{
		state.setAttribute (STATE_CUT_IDS, new Vector ());

		state.setAttribute (STATE_CUT_FLAG, Boolean.FALSE.toString());

	}	// initCutContent

	/**
	* find out whether there is a duplicate item in testVector
	* @param testVector The Vector to be tested on
	* @param testSize The integer of the test range
	* @return The index value of the duplicate ite
	*/
	private int repeatedName (Vector testVector, int testSize)
	{
		for (int i=1; i <= testSize; i++)
		{
			String currentName = (String) testVector.get (i);
			for (int j=i+1; j <= testSize; j++)
			{
				String comparedTitle = (String) testVector.get (j);
				if (comparedTitle.length()>0 && currentName.length()>0 && comparedTitle.equals (currentName))
				{
					return j;
				}
			}
		}
		return 0;

	}   // repeatedName

	/**
	* Is the id already exist in the current resource?
	* @param testVector The Vector to be tested on
	* @param testSize The integer of the test range
	* @parma isCollection Looking for collection or not
	* @return The index value of the exist id
	*/
	private int foundInResource (Vector testVector, int testSize, String collectionId, boolean isCollection)
	{
		try
		{
			ContentCollection c = ContentHostingService.getCollection(collectionId);
			Iterator membersIterator = c.getMemberResources().iterator();
			while (membersIterator.hasNext())
			{
				ResourceProperties p = ((Entity) membersIterator.next()).getProperties();
				String displayName = p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				if (displayName != null)
				{
					String collectionOrResource = p.getProperty(ResourceProperties.PROP_IS_COLLECTION);
					for (int i=1; i <= testSize; i++)
					{
						String testName = (String) testVector.get(i);
						if ((testName != null) && (displayName.equals (testName))
						      &&  ((isCollection && collectionOrResource.equals (Boolean.TRUE.toString()))
								        || (!isCollection && collectionOrResource.equals(Boolean.FALSE.toString()))))
						{
							return i;
						}
					}	// for
				}
			}
		}
		catch (IdUnusedException e){}
		catch (TypeException e){}
		catch (PermissionException e){}

		return 0;

	}	// foundInResource

	/**
	* empty String Vector object with the size sepecified
	* @param size The Vector object size -1
	* @return The Vector object consists of null Strings
	*/
	private static Vector emptyVector (int size)
	{
		Vector v = new Vector ();
		for (int i=0; i <= size; i++)
		{
			v.add (i, "");
		}
		return v;

	}	// emptyVector

	/**
	*  Setup for customization
	**/
	public String buildOptionsPanelContext( VelocityPortlet portlet,
											Context context,
											RunData data,
											SessionState state)
	{
		context.put("tlang",rb);
		String home = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(home));
		String siteId = ref.getContext();

		context.put("form-submit", BUTTON + "doConfigure_update");
		context.put("form-cancel", BUTTON + "doCancel_options");
		context.put("description", "Setting options for Resources in worksite "
				+ SiteService.getSiteDisplay(siteId));

		// pick the "-customize" template based on the standard template name
		String template = (String)getContext(data).get("template");
		return template + "-customize";

	}	// buildOptionsPanelContext

	/**
	* Handle the configure context's update button
	*/
	public void doConfigure_update(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(peid);

		// we are done with customization... back to the main (browse) mode
		state.setAttribute(STATE_MODE, MODE_LIST);

		// commit the change
		// saveOptions();
		cancelOptions();

	}   // doConfigure_update

	/**
	* doCancel_options called for form input tags type="submit" named="eventSubmit_doCancel"
	* cancel the options process
	*/
	public void doCancel_options(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData)data).getJs_peid();
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(peid);

		// cancel the options
		cancelOptions();

		// we are done with customization... back to the main (MODE_LIST) mode
		state.setAttribute(STATE_MODE, MODE_LIST);

	}   // doCancel_options

	/**
	* Add the collection id into the expanded collection list
	 * @throws PermissionException
	 * @throws TypeException
	 * @throws IdUnusedException
	*/
	public static void doExpand_collection(RunData data) throws IdUnusedException, TypeException, PermissionException
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		SortedSet expandedItems = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedItems == null)
		{
			expandedItems = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedItems);
		}

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = params.getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		String id = params.getString("collectionId");
		expandedItems.add(id);

		// add this folder id into the set to be event-observed
		addObservingPattern(id, state);

	}	// doExpand_collection

	/**
	* Remove the collection id from the expanded collection list
	*/
	static public void doCollapse_collection(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		SortedSet expandedItems = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedItems == null)
		{
			expandedItems = new TreeSet();
		}
		Map folderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		if(folderSortMap == null)
		{
			folderSortMap = new Hashtable();
			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, folderSortMap);
		}

		//get the ParameterParser from RunData
		ParameterParser params = data.getParameters ();
		String collectionId = params.getString("collectionId");

		// save the current selections
		Set selectedSet  = new TreeSet();
		String[] selectedItems = data.getParameters ().getStrings ("selectedMembers");
		if(selectedItems != null)
		{
			selectedSet.addAll(Arrays.asList(selectedItems));
		}
		state.setAttribute(STATE_LIST_SELECTIONS, selectedSet);

		SortedSet newSet = new TreeSet();
		Iterator l = expandedItems.iterator();
		while (l.hasNext ())
		{
			// remove the collection id and all of the subcollections
//		    Resource collection = (Resource) l.next();
//			String id = (String) collection.getId();
		    String id = (String) l.next();

			if (id.indexOf (collectionId)==-1)
			{
	//			newSet.put(id,collection);
				newSet.add(id);
			}
			else
			{
				folderSortMap.remove(id);
			}
		}

		state.setAttribute(STATE_EXPANDED_COLLECTIONS, newSet);

		// remove this folder id into the set to be event-observed
		removeObservingPattern(collectionId, state);

	}	// doCollapse_collection

	/**
	 * @param state
	 * @param homeCollectionId
	 * @param currentCollectionId
	 * @return
	 */
	public static List getCollectionPath(SessionState state)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);
		// make sure the channedId is set
		String currentCollectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		if(! isStackEmpty(state))
		{
			Map current_stack_frame = peekAtStack(state);
			String createCollectionId = (String) current_stack_frame.get(STATE_STACK_CREATE_COLLECTION_ID);
			if(createCollectionId == null)
			{
				createCollectionId = (String) state.getAttribute(STATE_CREATE_COLLECTION_ID);
			}
			if(createCollectionId != null)
			{
				currentCollectionId = createCollectionId;
			}
			else
			{
				String editCollectionId = (String) current_stack_frame.get(STATE_EDIT_COLLECTION_ID);
				if(editCollectionId == null)
				{
					editCollectionId = (String) state.getAttribute(STATE_EDIT_COLLECTION_ID);
				}
				if(editCollectionId != null)
				{
					currentCollectionId = editCollectionId;
				}
				else
				{
					String infoCollectionId = (String) current_stack_frame.get(STATE_MORE_COLLECTION_ID);
					if(infoCollectionId == null)
					{
						infoCollectionId = (String) state.getAttribute(STATE_MORE_COLLECTION_ID);
					}
					if(infoCollectionId != null)
					{
						currentCollectionId = infoCollectionId;
					}
				}
			}
		}
		String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);
		String navRoot = (String) state.getAttribute(STATE_NAVIGATION_ROOT);

		LinkedList collectionPath = new LinkedList();

		String previousCollectionId = "";
		Vector pathitems = new Vector();
		while ((currentCollectionId != null) && (!currentCollectionId.equals(navRoot)) && (!currentCollectionId.equals(previousCollectionId)) && (!contentService.isRootCollection(previousCollectionId)))
		{
			pathitems.add(currentCollectionId);
			previousCollectionId = currentCollectionId;
			currentCollectionId = contentService.getContainingCollectionId(currentCollectionId);
		}
		
		if(navRoot != null)
		{
			pathitems.add(navRoot);

			if(!navRoot.equals(homeCollectionId))
			{
				pathitems.add(homeCollectionId);
			}
		}

		Iterator items = pathitems.iterator();
		while(items.hasNext())
		{
			String id = (String) items.next();
			try
			{
				ResourceProperties props = contentService.getProperties(id);
				String name = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				ChefPathItem item = new ChefPathItem(id, name);

				boolean canRead = contentService.allowGetCollection(id) || contentService.allowGetResource(id);
				item.setCanRead(canRead);

				String url = contentService.getUrl(id);
				item.setUrl(url);

				item.setLast(collectionPath.isEmpty());
				if(id.equals(homeCollectionId))
				{
					item.setRoot(homeCollectionId);
				}
				else
				{
					item.setRoot(navRoot);
				}

				try
				{
					boolean isFolder = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
					item.setIsFolder(isFolder);
				}
				catch (EntityPropertyNotDefinedException e1)
				{
				}
				catch (EntityPropertyTypeException e1)
				{
				}

				collectionPath.addFirst(item);

			}
			catch (PermissionException e)
			{
			}
			catch (IdUnusedException e)
			{
			}
		}
		return collectionPath;
	}

	/**
	 * Get the items in this folder that should be seen.
	 * @param collectionId - String version of
	 * @param expandedCollections - Hash of collection resources
	 * @param sortedBy  - pass through to ContentHostingComparator
	 * @param sortedAsc - pass through to ContentHostingComparator
	 * @param parent - The folder containing this item
	 * @param isLocal - true if navigation root and home collection id of site are the same, false otherwise
	 * @param state - The session state
	 * @return a List of ChefBrowseItem objects
	 */
	protected static List getListView(String collectionId, Set highlightedItems, ChefBrowseItem parent, boolean isLocal, SessionState state)
	{
		// find the ContentHosting service
		org.sakaiproject.content.api.ContentHostingService contentService = (org.sakaiproject.content.api.ContentHostingService) state.getAttribute (STATE_CONTENT_SERVICE);

		boolean need_to_expand_all = Boolean.TRUE.toString().equals((String)state.getAttribute(STATE_NEED_TO_EXPAND_ALL));
		
		Comparator userSelectedSort = (Comparator) state.getAttribute(STATE_LIST_VIEW_SORT);
		Comparator defaultComparator = (Comparator) state.getAttribute(STATE_DEFAULT_SORT);
		
		Map expandedFolderSortMap = (Map) state.getAttribute(STATE_EXPANDED_FOLDER_SORT_MAP);
		if(expandedFolderSortMap == null)
		{
			expandedFolderSortMap = new Hashtable();
			state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
		}
		
		SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
		if(expandedCollections == null)
		{
			expandedCollections = new TreeSet();
			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
		}
		String mode = (String) state.getAttribute (STATE_MODE);

		List newItems = new LinkedList();
		try
		{
			// get the collection
			// try using existing resource first
			ContentCollection collection = null;

			// get the collection
			collection = contentService.getCollection(collectionId);
			if(need_to_expand_all || expandedCollections.contains(collectionId))
			{
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
						comparator = contentService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);
					}
					else
					{
						comparator = defaultComparator;
					}
				}
				expandedFolderSortMap.put(collectionId, comparator);
				expandedCollections.add(collectionId);
				// state.setAttribute(STATE_EXPANDED_FOLDER_SORT_MAP, expandedFolderSortMap);
			}

			String dummyId = collectionId.trim();
			if(dummyId.endsWith(Entity.SEPARATOR))
			{
				dummyId += "dummy";
			}
			else
			{
				dummyId += Entity.SEPARATOR + "dummy";
			}

			boolean canRead = false;
			boolean canDelete = false;
			boolean canRevise = false;
			boolean canAddFolder = false;
			boolean canAddItem = false;
			boolean canUpdate = false;
			int depth = 0;

			if(parent == null || ! parent.canRead())
			{
				canRead = contentService.allowGetCollection(collectionId);
			}
			else
			{
				canRead = parent.canRead();
			}
			if(parent == null || ! parent.canDelete())
			{
				canDelete = contentService.allowRemoveCollection(collectionId);
			}
			else
			{
				canDelete = parent.canDelete();
			}
			if(parent == null || ! parent.canRevise())
			{
				canRevise = contentService.allowUpdateCollection(collectionId);
			}
			else
			{
				canRevise = parent.canRevise();
			}
			if(parent == null || ! parent.canAddFolder())
			{
				canAddFolder = contentService.allowAddCollection(dummyId);
			}
			else
			{
				canAddFolder = parent.canAddFolder();
			}
			if(parent == null || ! parent.canAddItem())
			{
				canAddItem = contentService.allowAddResource(dummyId);
			}
			else
			{
				canAddItem = parent.canAddItem();
			}
			if(parent == null || ! parent.canUpdate())
			{
				canUpdate = AuthzGroupService.allowUpdate(collectionId);
			}
			else
			{
				canUpdate = parent.canUpdate();
			}
			if(parent != null)
			{
				depth = parent.getDepth() + 1;
			}

			if(canAddItem)
			{
				state.setAttribute(STATE_PASTE_ALLOWED_FLAG, Boolean.TRUE.toString());
			}
			// each child will have it's own delete status based on: delete.own or delete.any
			boolean hasDeletableChildren = true; 
         
			// may have perms to copy in another folder, even if no perms in this folder
			boolean hasCopyableChildren = canRead; 

			String homeCollectionId = (String) state.getAttribute(STATE_HOME_COLLECTION_ID);

			ResourceProperties cProperties = collection.getProperties();
			String folderName = cProperties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if(collectionId.equals(homeCollectionId))
			{
				folderName = (String) state.getAttribute(STATE_HOME_COLLECTION_DISPLAY_NAME);
			}
			ChefBrowseItem folder = new ChefBrowseItem(collectionId, folderName, "folder");
			if(parent == null)
			{
				folder.setRoot(collectionId);
			}
			else
			{
				folder.setRoot(parent.getRoot());
			}
			
			boolean isInDropbox = contentService.isInDropbox(collectionId);
			folder.setInDropbox(isInDropbox);
			
			BasicRightsAssignment rightsObj = new BasicRightsAssignment(folder.getItemNum(), cProperties);
			folder.setRights(rightsObj);
			
			AccessMode access = collection.getAccess();
			if(access == null || AccessMode.SITE == access)
			{
				folder.setAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				folder.setAccess(access.toString());
			}
			
			AccessMode inherited_access = collection.getInheritedAccess();
			if(inherited_access == null || AccessMode.SITE == inherited_access)
			{
				folder.setInheritedAccess(AccessMode.INHERITED.toString());
			}
			else
			{
				folder.setInheritedAccess(inherited_access.toString());
			}
			
			Collection access_groups = collection.getGroupObjects();
			if(access_groups == null)
			{
				access_groups = new Vector();
			}
			folder.setGroups(access_groups);
			Collection inherited_access_groups = collection.getInheritedGroupObjects();
			if(inherited_access_groups == null)
			{
				inherited_access_groups = new Vector();
			}
			folder.setInheritedGroups(inherited_access_groups);
			
			if(parent != null && (parent.isPubview() || parent.isPubviewInherited()))
			{
				folder.setPubviewInherited(true);
				folder.setPubview(false);
			}
			else if(contentService.isPubView(folder.getId()))
			{
				folder.setPubview(true);
			}

			if(highlightedItems == null || highlightedItems.isEmpty())
			{
				// do nothing
			}
			else if(parent != null && parent.isHighlighted())
			{
				folder.setInheritsHighlight(true);
				folder.setHighlighted(true);
			}
			else if(highlightedItems.contains(collectionId))
			{
				folder.setHighlighted(true);
				folder.setInheritsHighlight(false);
			}

			String containerId = contentService.getContainingCollectionId (collectionId);
			folder.setContainer(containerId);

			folder.setCanRead(canRead);
			folder.setCanRevise(canRevise);
			folder.setCanAddItem(canAddItem);
			folder.setCanAddFolder(canAddFolder);
			folder.setCanDelete(canDelete);
			folder.setCanUpdate(canUpdate);
			
			folder.setAvailable(collection.isAvailable());

			try
			{
				Time createdTime = cProperties.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
				String createdTimeString = createdTime.toStringLocalShortDate();
				folder.setCreatedTime(createdTimeString);
			}
			catch(Exception e)
			{
				String createdTimeString = cProperties.getProperty(ResourceProperties.PROP_CREATION_DATE);
				folder.setCreatedTime(createdTimeString);
			}
			try
			{
				String createdBy = getUserProperty(cProperties, ResourceProperties.PROP_CREATOR).getDisplayName();
				folder.setCreatedBy(createdBy);
			}
			catch(Exception e)
			{
				String createdBy = cProperties.getProperty(ResourceProperties.PROP_CREATOR);
				folder.setCreatedBy(createdBy);
			}
			try
			{
				Time modifiedTime = cProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
				String modifiedTimeString = modifiedTime.toStringLocalShortDate();
				folder.setModifiedTime(modifiedTimeString);
			}
			catch(Exception e)
			{
				String modifiedTimeString = cProperties.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
				folder.setModifiedTime(modifiedTimeString);
			}
			try
			{
				String modifiedBy = getUserProperty(cProperties, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
				folder.setModifiedBy(modifiedBy);
			}
			catch(Exception e)
			{
				String modifiedBy = cProperties.getProperty(ResourceProperties.PROP_MODIFIED_BY);
				folder.setModifiedBy(modifiedBy);
			}

			String url = contentService.getUrl(collectionId);
			folder.setUrl(url);
			
			// get the "size' of the collection, meaning the number of members one level down
			int collection_size = collection.getMemberCount(); // newMembers.size();
			folder.setIsEmpty(collection_size < 1);
			folder.setSortable(ContentHostingService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < EXPANDABLE_FOLDER_SIZE_LIMIT);
			Integer expansionLimit = (Integer) state.getAttribute(STATE_EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(expansionLimit == null)
			{
				expansionLimit = new Integer(EXPANDABLE_FOLDER_SIZE_LIMIT);
			}
			folder.setIsTooBig(collection_size > expansionLimit.intValue());
				
			folder.setDepth(depth);
			newItems.add(folder);

			if(need_to_expand_all || expandedFolderSortMap.keySet().contains(collectionId))
			{
				// Get the collection members from the 'new' collection
				List newMembers = collection.getMemberResources();
				
				Comparator comparator = userSelectedSort;
				if(comparator == null)
				{
					comparator = (Comparator) expandedFolderSortMap.get(collectionId);
					if(comparator == null)
					{
						comparator = defaultComparator;
					}
				}

				Collections.sort(newMembers, comparator);

				// loop thru the (possibly) new members and add to the list
				Iterator it = newMembers.iterator();
				while(it.hasNext())
				{
					ContentEntity resource = (ContentEntity) it.next();
					ResourceProperties props = resource.getProperties();

					String itemId = resource.getId();
					
					if(contentService.isAvailabilityEnabled() && ! contentService.isAvailable(itemId))
					{
						continue;
					}

					if(resource.isCollection())
					{
						List offspring = getListView(itemId, highlightedItems, folder, isLocal, state);

						if(! offspring.isEmpty())
						{
							ChefBrowseItem child = (ChefBrowseItem) offspring.get(0);
							hasDeletableChildren = hasDeletableChildren || child.hasDeletableChildren();
							hasCopyableChildren = hasCopyableChildren || child.hasCopyableChildren();
						}

						// add all the items in the subfolder to newItems
						newItems.addAll(offspring);
					}
					else
					{
						AccessMode access_mode = ((GroupAwareEntity) resource).getAccess();
						if(access_mode == null)
						{
							access_mode = AccessMode.INHERITED;
						}
						else if(access_mode == AccessMode.GROUPED)
						{
							if(! contentService.allowGetResource(resource.getId()))
							{
								continue;
							}
						}
						
						String itemType = ((ContentResource)resource).getContentType();
						String itemName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
						ChefBrowseItem newItem = new ChefBrowseItem(itemId, itemName, itemType);
						
						boolean isLocked = contentService.isLocked(itemId);
						newItem.setLocked(isLocked);
						
						boolean isAvailable = folder.isAvailable();
						if(isAvailable)
						{
							isAvailable = resource.isAvailable();
						}
						newItem.setAvailable(isAvailable);

						newItem.setAccess(access_mode.toString());
						newItem.setInheritedAccess(folder.getEffectiveAccess());

						newItem.setInDropbox(isInDropbox);
						
						BasicRightsAssignment rightsObj2 = new BasicRightsAssignment(newItem.getItemNum(), props);
						newItem.setRights(rightsObj2);
						Collection groups = ((GroupAwareEntity) resource).getGroupObjects();
						if(groups == null)
						{
							groups = new Vector();
						}
						Collection inheritedGroups = folder.getGroups();
						if(inheritedGroups == null || inheritedGroups.isEmpty())
						{
							inheritedGroups = folder.getInheritedGroups();
						}
						newItem.setGroups(groups);	
						newItem.setInheritedGroups(inheritedGroups);

						newItem.setContainer(collectionId);
						newItem.setRoot(folder.getRoot());

						// delete and revise permissions based on item (not parent)
						newItem.setCanDelete(contentService.allowRemoveResource(itemId) && ! isLocked);
						newItem.setCanRevise(contentService.allowUpdateResource(itemId)); 
						newItem.setCanRead(canRead);
						newItem.setCanCopy(canRead); // may have perms to copy in another folder, even if no perms in this folder
						newItem.setCanAddItem(canAddItem); // true means this user can add an item in the folder containing this item (used for "duplicate")

						if(highlightedItems == null || highlightedItems.isEmpty())
						{
							// do nothing
						}
						else if(folder.isHighlighted())
						{
							newItem.setInheritsHighlight(true);
							newItem.setHighlighted(true);
						}
						else if(highlightedItems.contains(itemId))
						{
							newItem.setHighlighted(true);
							newItem.setInheritsHighlight(false);
						}

						try
						{
							Time createdTime = props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
							String createdTimeString = createdTime.toStringLocalShortDate();
							newItem.setCreatedTime(createdTimeString);
						}
						catch(Exception e)
						{
							String createdTimeString = props.getProperty(ResourceProperties.PROP_CREATION_DATE);
							newItem.setCreatedTime(createdTimeString);
						}
						try
						{
							String createdBy = getUserProperty(props, ResourceProperties.PROP_CREATOR).getDisplayName();
							newItem.setCreatedBy(createdBy);
						}
						catch(Exception e)
						{
							String createdBy = props.getProperty(ResourceProperties.PROP_CREATOR);
							newItem.setCreatedBy(createdBy);
						}
						try
						{
							Time modifiedTime = props.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
							String modifiedTimeString = modifiedTime.toStringLocalShortDate();
							newItem.setModifiedTime(modifiedTimeString);
						}
						catch(Exception e)
						{
							String modifiedTimeString = props.getProperty(ResourceProperties.PROP_MODIFIED_DATE);
							newItem.setModifiedTime(modifiedTimeString);
						}
						try
						{
							String modifiedBy = getUserProperty(props, ResourceProperties.PROP_MODIFIED_BY).getDisplayName();
							newItem.setModifiedBy(modifiedBy);
						}
						catch(Exception e)
						{
							String modifiedBy = props.getProperty(ResourceProperties.PROP_MODIFIED_BY);
							newItem.setModifiedBy(modifiedBy);
						}

						if(folder.isPubview() || folder.isPubviewInherited())
						{
							newItem.setPubviewInherited(true);
							newItem.setPubview(false);
						}
						else if(contentService.isPubView(resource.getId()))
						{
							newItem.setPubview(true);
						}

						String size = props.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH);
						newItem.setSize(size);

						String target = Validator.getResourceTarget(props.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
						newItem.setTarget(target);

						String newUrl = contentService.getUrl(itemId);
						newItem.setUrl(newUrl);

						try
						{
							boolean copyrightAlert = props.getBooleanProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
							newItem.setCopyrightAlert(copyrightAlert);
						}
						catch(Exception e)
						{}
						newItem.setDepth(depth + 1);

						if (checkItemFilter((ContentResource)resource, newItem, state)) 
						{
							newItems.add(newItem);
						}
					}
				}

			}
			folder.seDeletableChildren(hasDeletableChildren);
			folder.setCopyableChildren(hasCopyableChildren);
			// return newItems;
		}
		catch (IdUnusedException ignore)
		{
			// this condition indicates a site that does not have a resources collection (mercury?)
		}
		catch (TypeException e)
		{
			addAlert(state, "TypeException.");
		}
		catch (PermissionException e)
		{
			// ignore -- we'll just skip this collection since user lacks permission to access it.
			//addAlert(state, "PermissionException");
		}

		return newItems;

	}	// getListView
	
	public ListItem getListItem(String entityId, ListItem parent, boolean expandAll, Set expandedFolders)
	{
		ListItem item = null;
		ContentEntity entity = null;
		try
		{
			boolean isCollection = ContentHostingService.isCollection(entityId);
			if(isCollection)
			{
				entity = ContentHostingService.getCollection(entityId);
				
			}
			else
			{
				entity = ContentHostingService.getResource(entityId);
			}
			
			Reference ref = EntityManager.newReference(entity.getReference());

			item = new ListItem(entity);
			
			/*
			 * calculate permissions for this entity.  If its access mode is 
			 * GROUPED, we need to calculate permissions based on current user's 
			 * role in group. Otherwise, we inherit from containing collection
			 * and check to see if additional permissions are set on this entity
			 * that were't set on containing collection...
			 */
			Set permissions = new TreeSet();
			// TODO: Calculate permissions
			if(GroupAwareEntity.AccessMode.INHERITED == entity.getAccess())
			{
				// permissions are same as parent or site
				if(parent == null)
				{
					// permissions are same as site
					permissions = getPermissions(ref.getContext());
				}
				else
				{
					// permissions are same as parent
					permissions.addAll(parent.getPermissions());
				}
			}
			else if(GroupAwareEntity.AccessMode.GROUPED == entity.getAccess())
			{
				// permissions are determined by group(s)
			}
			item.setPermissions(permissions);
			
			if(isCollection)
			{
				List childNames = ((ContentCollection) entity).getMembers();
				Iterator childIt = childNames.iterator();
				while(childIt.hasNext())
				{
					String childId = (String) childIt.next();
					ListItem child = getListItem(childId, item, expandAll, expandedFolders);
				}
			}
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			logger.warn("IdUnusedException ", e);
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			logger.warn("TypeException ", e);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			logger.warn("PermissionException ", e);
		}
		
		return item;
	}
	/**
	 * @param context
	 * @return
	 */
	protected Set getPermissions(String id)
	{
		Set permissions = new TreeSet();
		if(ContentHostingService.isCollection(id))
		{
			if(ContentHostingService.allowAddCollection(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			if(ContentHostingService.allowGetCollection(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_READ);
			}
			/*
			if(ContentHostingService.allowAddCollection(id) || ContentHostingService.allowAddResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			if(ContentHostingService.allowAddCollection(id) || ContentHostingService.allowAddResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			if(ContentHostingService.allowAddCollection(id) || ContentHostingService.allowAddResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			if(ContentHostingService.allowAddCollection(id) || ContentHostingService.allowAddResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			*/
		}
		else
		{
			if(ContentHostingService.allowAddResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_ADD);
			}
			if(ContentHostingService.allowGetResource(id))
			{
				permissions.add(ContentHostingService.AUTH_RESOURCE_READ);
			}
		}
		
		return permissions;
	}

	public ListItem expandChildren(ListItem item, ListItem parent, boolean expandAll, Set expandedFolders)
	{
		List newItems = new Vector();
		
		
		
		return item;
		
	}

	protected static boolean checkItemFilter(ContentResource resource, ChefBrowseItem newItem, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACH_FILTER);
	
	      if (filter != null) 
	      {
	    	  	if (newItem != null) 
	    	  	{
	    	  		newItem.setCanSelect(filter.allowSelect(resource));
	    	  	}
	    	  	return filter.allowView(resource);
	      }
	      else if (newItem != null) 
	      {
	    	  	newItem.setCanSelect(true);
	      }

	      return true;
	}

	protected static boolean checkSelctItemFilter(ContentResource resource, SessionState state) 
	{
		ContentResourceFilter filter = (ContentResourceFilter)state.getAttribute(STATE_ATTACH_FILTER);
		
		if (filter != null)
		{
			return filter.allowSelect(resource);
		}
		return true;
	}

    /**
	 * set the state name to be "copy" if any item has been selected for copying
	 */
	public void doCopyitem ( RunData data )
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		String itemId = data.getParameters ().getString ("itemId");

		if (itemId == null)
		{
			// there is no resource selected, show the alert message to the user
			addAlert(state, rb.getString("choosefile6"));
			state.setAttribute (STATE_MODE, MODE_LIST);
		}
		else
		{
			try
			{
				ResourceProperties properties = ContentHostingService.getProperties (itemId);
				/*
				if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
				{
					String alert = (String) state.getAttribute(STATE_MESSAGE);
					if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
					{
						addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
					}
				}
				*/
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis15"));
			}
			catch (IdUnusedException e)
			{
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}	// try-catch

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.TRUE.toString());

				state.setAttribute (STATE_COPIED_ID, itemId);
			}	// if-else
		}	// if-else

	}	// doCopyitem

	/**
	* Paste the previously copied item(s)
	*/
	public static void doPasteitems ( RunData data)
	{
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		List items = (List) state.getAttribute(STATE_COPIED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator itemIter = items.iterator();
		while (itemIter.hasNext())
		{
			// get the copied item to be pasted
			String itemId = (String) itemIter.next();

			String originalDisplayName = NULL_STRING;

			try
			{
				String id = ContentHostingService.copyIntoFolder(itemId, collectionId);
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					String helper_mode = (String) state.getAttribute(STATE_RESOURCES_HELPER_MODE);
					if(helper_mode != null && MODE_ATTACHMENT_NEW_ITEM.equals(helper_mode))
					{
						// add to the attachments vector
						List attachments = EntityManager.newReferenceList();
						Reference ref = EntityManager.newReference(ContentHostingService.getReference(id));
						attachments.add(ref);
						cleanupState(state);
						state.setAttribute(STATE_ATTACHMENTS, attachments);
					}
					else
					{
						if(state.getAttribute(STATE_ATTACH_LINKS) == null)
						{
							attachItem(id, state);
						}
						else
						{
							attachLink(id, state);
						}
					}
				}
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("someone") + " " + originalDisplayName);
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(IdLengthException e)
			{
				addAlert(state, rb.getString("toolong") + " " + e.getMessage());
			}
			catch(IdUniquenessException e)
			{
				addAlert(state, "Could not add this item to this folder");
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch(InconsistentException e)
			{
				addAlert(state, rb.getString("recursive") + " " + itemId);
			}
			catch (OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}	// try-catch
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doPasteitems ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// delete sucessful
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
				}
				else
				{
					state.setAttribute (STATE_MODE, MODE_LIST);
				}

				// try to expand the collection
				SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(expandedCollections == null)
				{
					expandedCollections = new TreeSet();
					state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
				}
				if(! expandedCollections.contains(collectionId))
				{
					expandedCollections.add(collectionId);
				}

				// reset the copy flag
				if (((String)state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
				{
					state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
				}
			}

		}

	}	// doPasteitems

	/**
	* Paste the item(s) selected to be moved
	*/
	public static void doMoveitems ( RunData data)
	{
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		List items = (List) state.getAttribute(STATE_MOVED_IDS);

		String collectionId = params.getString ("collectionId");

		Iterator itemIter = items.iterator();
		while (itemIter.hasNext())
		{
			// get the copied item to be pasted
			String itemId = (String) itemIter.next();

			String originalDisplayName = NULL_STRING;

			try
			{
				/*
				ResourceProperties properties = ContentHostingService.getProperties (itemId);
				originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

				// copy, cut and paste not operated on collections
				if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
				{
					String alert = (String) state.getAttribute(STATE_MESSAGE);
					if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
					{
						addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
					}
				}
				else
				*/
				{
					ContentHostingService.moveIntoFolder(itemId, collectionId);
				}	// if-else
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
			}
			catch (IdUnusedException e)
			{
				addAlert(state,RESOURCE_NOT_EXIST_STRING);
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("someone") + " " + originalDisplayName);
			}
			catch (TypeException e)
			{
				addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
			}
			catch (InconsistentException e)
			{
				addAlert(state, rb.getString("recursive") + " " + itemId);
			}
			catch(IdUsedException e)
			{
				addAlert(state, rb.getString("toomany"));
			}
			catch(ServerOverloadException e)
			{
				addAlert(state, rb.getString("failed"));
			}
			catch (OverQuotaException e)
			{
				addAlert(state, rb.getString("overquota"));
			}	// try-catch
			catch(RuntimeException e)
			{
				logger.debug("ResourcesAction.doMoveitems ***** Unknown Exception ***** " + e.getMessage());
				addAlert(state, rb.getString("failed"));
			}

			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				// delete sucessful
				String mode = (String) state.getAttribute(STATE_MODE);
				if(MODE_HELPER.equals(mode))
				{
					state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
				}
				else
				{
					state.setAttribute (STATE_MODE, MODE_LIST);
				}

				// try to expand the collection
				SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
				if(! expandedCollections.contains(collectionId))
				{
					expandedCollections.add(collectionId);
				}

				// reset the copy flag
				if (((String)state.getAttribute (STATE_MOVE_FLAG)).equals (Boolean.TRUE.toString()))
				{
					state.setAttribute (STATE_MOVE_FLAG, Boolean.FALSE.toString());
				}
			}

		}

	}	// doMoveitems


	/**
	* Paste the previously copied item(s)
	*/
	public static void doPasteitem ( RunData data)
	{
		ParameterParser params = data.getParameters ();

		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		// get the copied item to be pasted
		String itemId = params.getString("itemId");

		String collectionId = params.getString ("collectionId");

		duplicateItem(state, itemId, collectionId);

	}	// doPasteitem

	/**
	 * @param state
	 * @param itemId
	 * @param collectionId
	 */
	protected static void duplicateItem(SessionState state, String itemId, String collectionId)
	{
		String originalDisplayName = NULL_STRING;

		try
		{
			ResourceProperties properties = ContentHostingService.getProperties (itemId);
			originalDisplayName = properties.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);

			// copy, cut and paste not operated on collections
			if (properties.getProperty (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
			{
				String alert = (String) state.getAttribute(STATE_MESSAGE);
				if (alert == null || ((alert != null) && (alert.indexOf(RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING) == -1)))
				{
					addAlert(state, RESOURCE_INVALID_OPERATION_ON_COLLECTION_STRING);
				}
			}
			else
			{
				// paste the resource
				ContentResource resource = ContentHostingService.getResource (itemId);
				ResourceProperties p = ContentHostingService.getProperties(itemId);
				String displayName = DUPLICATE_STRING + p.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

				String newItemId = ContentHostingService.copyIntoFolder(itemId, collectionId);

				ContentResourceEdit copy = ContentHostingService.editResource(newItemId);
				ResourcePropertiesEdit pedit = copy.getPropertiesEdit();
				pedit.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName);
				ContentHostingService.commitResource(copy, NotificationService.NOTI_NONE);

			}	// if-else
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notpermis8") + " " + originalDisplayName + ". ");
		}
		catch (IdUnusedException e)
		{
			addAlert(state,RESOURCE_NOT_EXIST_STRING);
		}
		catch (IdUsedException e)
		{
			addAlert(state, rb.getString("notaddreso") + " " + originalDisplayName + " " + rb.getString("used2"));
		}
		catch(IdLengthException e)
		{
			addAlert(state, rb.getString("toolong") + " " + e.getMessage());
		}
		catch(IdUniquenessException e)
		{
			addAlert(state, "Could not add this item to this folder");
		}
		catch (InconsistentException ee)
		{
			addAlert(state, RESOURCE_INVALID_TITLE_STRING);
		}
		catch(InUseException e)
		{
			addAlert(state, rb.getString("someone") + " " + originalDisplayName + ". ");
		}
		catch(OverQuotaException e)
		{
			addAlert(state, rb.getString("overquota"));
		}
		catch(ServerOverloadException e)
		{
			// this represents temporary unavailability of server's filesystem
			// for server configured to save resource body in filesystem
			addAlert(state, rb.getString("failed"));
		}
		catch (TypeException e)
		{
			addAlert(state, rb.getString("pasteitem") + " " + originalDisplayName + " " + rb.getString("mismatch"));
		}	// try-catch

		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// delete sucessful
			String mode = (String) state.getAttribute(STATE_MODE);
			if(MODE_HELPER.equals(mode))
			{
				state.setAttribute(STATE_RESOURCES_HELPER_MODE, MODE_ATTACHMENT_SELECT);
			}
			else
			{
				state.setAttribute (STATE_MODE, MODE_LIST);
			}

			// try to expand the collection
			SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
			if(STATE_EXPANDED_COLLECTIONS == null)
			{
				expandedCollections = new TreeSet();
				state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
			}
			if(! expandedCollections.contains(collectionId))
			{
				expandedCollections.add(collectionId);
			}

			// reset the copy flag
			if (((String)state.getAttribute (STATE_COPY_FLAG)).equals (Boolean.TRUE.toString()))
			{
				state.setAttribute (STATE_COPY_FLAG, Boolean.FALSE.toString());
			}
		}
	}

	/**
	* Fire up the permissions editor for the current folder's permissions
	*/
	public void doFolder_permissions(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		ParameterParser params = data.getParameters();

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		// get the current collection id and the related site
		String collectionId = params.getString("collectionId"); //(String) state.getAttribute (STATE_COLLECTION_ID);
		String title = "";
		try
		{
			title = ContentHostingService.getProperties(collectionId).getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("notread"));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("notfindfol"));
		}

		// the folder to edit
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(collectionId));
		state.setAttribute(PermissionsHelper.TARGET_REF, ref.getReference());

		// use the folder's context (as a site) for roles
		String siteRef = SiteService.siteReference(ref.getContext());
		state.setAttribute(PermissionsHelper.ROLES_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis") + " " + title);

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "content.");

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

	}	// doFolder_permissions

	/**
	* Fire up the permissions editor for the tool's permissions
	*/
	public void doPermissions(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		// cancel copy if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_COPY_FLAG)))
		{
			initCopyContext(state);
		}

		// cancel move if there is one in progress
		if(! Boolean.FALSE.toString().equals(state.getAttribute (STATE_MOVE_FLAG)))
		{
			initMoveContext(state);
		}

		// should we save here?
		state.setAttribute(STATE_LIST_SELECTIONS, new TreeSet());

		// get the current home collection id and the related site
		String collectionId = (String) state.getAttribute (STATE_HOME_COLLECTION_ID);
		Reference ref = EntityManager.newReference(ContentHostingService.getReference(collectionId));
		String siteRef = SiteService.siteReference(ref.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis1")
				+ SiteService.getSiteDisplay(ref.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "content.");

		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

	}	// doPermissions

	/**
	* is notification enabled?
	*/
	protected boolean notificationEnabled(SessionState state)
	{
		return true;

	}	// notificationEnabled

	/**
	 * Processes the HTML document that is coming back from the browser
	 * (from the formatted text editing widget).
	 * @param state Used to pass in any user-visible alerts or errors when processing the text
	 * @param strFromBrowser The string from the browser
	 * @return The formatted text
	 */
	private String processHtmlDocumentFromBrowser(SessionState state, String strFromBrowser)
	{
		StringBuffer alertMsg = new StringBuffer();
		String text = FormattedText.processHtmlDocument(strFromBrowser, alertMsg);
		if (alertMsg.length() > 0) addAlert(state, alertMsg.toString());
		return text;
	}

	/**
	 *
	 * Whether a resource item can be replaced
	 * @param p The ResourceProperties object for the resource item
	 * @return true If it can be replaced; false otherwise
	 */
	private static boolean replaceable(ResourceProperties p)
	{
		boolean rv = true;

		if (p.getPropertyFormatted (ResourceProperties.PROP_IS_COLLECTION).equals (Boolean.TRUE.toString()))
		{
			rv = false;
		}
		else if (p.getProperty (ResourceProperties.PROP_CONTENT_TYPE).equals (ResourceProperties.TYPE_URL))
		{
			rv = false;
		}
		String displayName = p.getPropertyFormatted (ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName.indexOf(SHORTCUT_STRING) != -1)
		{
			rv = false;
		}

		return rv;

	}	// replaceable

	/**
	 *
	 * put copyright info into context
	 */
	private static void copyrightChoicesIntoContext(SessionState state, Context context)
	{
		boolean usingCreativeCommons = state.getAttribute(STATE_USING_CREATIVE_COMMONS) != null && state.getAttribute(STATE_USING_CREATIVE_COMMONS).equals(Boolean.TRUE.toString());		
		
		if(usingCreativeCommons)
		{
			
			String ccOwnershipLabel = "Who created this resource?";
			List ccOwnershipList = new Vector();
			ccOwnershipList.add("-- Select --");
			ccOwnershipList.add("I created this resource");
			ccOwnershipList.add("Someone else created this resource");
			
			String ccMyGrantLabel = "Terms of use";
			List ccMyGrantOptions = new Vector();
			ccMyGrantOptions.add("-- Select --");
			ccMyGrantOptions.add("Use my copyright");
			ccMyGrantOptions.add("Use Creative Commons License");
			ccMyGrantOptions.add("Use Public Domain Dedication");
			
			String ccCommercialLabel = "Allow commercial use?";
			List ccCommercialList = new Vector();
			ccCommercialList.add("Yes");
			ccCommercialList.add("No");
			
			String ccModificationLabel = "Allow Modifications?";
			List ccModificationList = new Vector();
			ccModificationList.add("Yes");
			ccModificationList.add("Yes, share alike");
			ccModificationList.add("No");
			
			String ccOtherGrantLabel = "Terms of use";
			List ccOtherGrantList = new Vector();
			ccOtherGrantList.add("Subject to fair-use exception");
			ccOtherGrantList.add("Public domain (created before copyright law applied)");
			ccOtherGrantList.add("Public domain (copyright has expired)");
			ccOtherGrantList.add("Public domain (government document not subject to copyright)");
			
			String ccRightsYear = "Year";
			String ccRightsOwner = "Copyright owner";
			
			String ccAcknowledgeLabel = "Require users to acknowledge author's rights before access?";
			List ccAcknowledgeList = new Vector();
			ccAcknowledgeList.add("Yes");
			ccAcknowledgeList.add("No");
			
			String ccInfoUrl = "";
			
			int year = TimeService.newTime().breakdownLocal().getYear();
			String username = UserDirectoryService.getCurrentUser().getDisplayName(); 

			context.put("usingCreativeCommons", Boolean.TRUE);
			context.put("ccOwnershipLabel", ccOwnershipLabel);
			context.put("ccOwnershipList", ccOwnershipList);
			context.put("ccMyGrantLabel", ccMyGrantLabel);
			context.put("ccMyGrantOptions", ccMyGrantOptions);
			context.put("ccCommercialLabel", ccCommercialLabel);
			context.put("ccCommercialList", ccCommercialList);
			context.put("ccModificationLabel", ccModificationLabel);
			context.put("ccModificationList", ccModificationList);
			context.put("ccOtherGrantLabel", ccOtherGrantLabel);
			context.put("ccOtherGrantList", ccOtherGrantList);
			context.put("ccRightsYear", ccRightsYear);
			context.put("ccRightsOwner", ccRightsOwner);
			context.put("ccAcknowledgeLabel", ccAcknowledgeLabel);
			context.put("ccAcknowledgeList", ccAcknowledgeList);
			context.put("ccInfoUrl", ccInfoUrl);
			context.put("ccThisYear", Integer.toString(year));
			context.put("ccThisUser", username);
		}
		else
		{
			//copyright
			if (state.getAttribute(COPYRIGHT_FAIRUSE_URL) != null)
			{
				context.put("fairuseurl", state.getAttribute(COPYRIGHT_FAIRUSE_URL));
			}
			if (state.getAttribute(NEW_COPYRIGHT_INPUT) != null)
			{
				context.put("newcopyrightinput", state.getAttribute(NEW_COPYRIGHT_INPUT));
			}
	
			if (state.getAttribute(COPYRIGHT_TYPES) != null)
			{
				List copyrightTypes = (List) state.getAttribute(COPYRIGHT_TYPES);
				context.put("copyrightTypes", copyrightTypes);
				context.put("copyrightTypesSize", new Integer(copyrightTypes.size() - 1));
				context.put("USE_THIS_COPYRIGHT", copyrightTypes.get(copyrightTypes.size() - 1));
			}
		}
		
		Boolean preventPublicDisplay = (Boolean) state.getAttribute(STATE_PREVENT_PUBLIC_DISPLAY);
		if(preventPublicDisplay == null)
		{
			preventPublicDisplay = Boolean.FALSE;
			state.setAttribute(STATE_PREVENT_PUBLIC_DISPLAY, preventPublicDisplay);
		}
		context.put("preventPublicDisplay", preventPublicDisplay);
		
	}	// copyrightChoicesIntoContext

	/**
	 * Add variables and constants to the velocity context to render an editor
	 * for inputing and modifying optional metadata properties about a resource.
	 */
	private static void metadataGroupsIntoContext(SessionState state, Context context)
	{

		context.put("STRING", ResourcesMetadata.WIDGET_STRING);
		context.put("TEXTAREA", ResourcesMetadata.WIDGET_TEXTAREA);
		context.put("BOOLEAN", ResourcesMetadata.WIDGET_BOOLEAN);
		context.put("INTEGER", ResourcesMetadata.WIDGET_INTEGER);
		context.put("DOUBLE", ResourcesMetadata.WIDGET_DOUBLE);
		context.put("DATE", ResourcesMetadata.WIDGET_DATE);
		context.put("TIME", ResourcesMetadata.WIDGET_TIME);
		context.put("DATETIME", ResourcesMetadata.WIDGET_DATETIME);
		context.put("ANYURI", ResourcesMetadata.WIDGET_ANYURI);
		context.put("WYSIWYG", ResourcesMetadata.WIDGET_WYSIWYG);

		context.put("today", TimeService.newTime());

		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups != null && !metadataGroups.isEmpty())
		{
			context.put("metadataGroups", metadataGroups);
		}

	}	// metadataGroupsIntoContext

	/**
	 * initialize the metadata context
	 */
	private static void initMetadataContext(SessionState state)
	{
		// define MetadataSets map
		List metadataGroups = (List) state.getAttribute(STATE_METADATA_GROUPS);
		if(metadataGroups == null)
		{
			metadataGroups = new Vector();
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		// define DublinCore
		if( !metadataGroups.contains(new MetadataGroup(rb.getString("opt_props"))) )
		{
			MetadataGroup dc = new MetadataGroup( rb.getString("opt_props") );
			// dc.add(ResourcesMetadata.PROPERTY_DC_TITLE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_DESCRIPTION);
			dc.add(ResourcesMetadata.PROPERTY_DC_ALTERNATIVE);
			dc.add(ResourcesMetadata.PROPERTY_DC_CREATOR);
			dc.add(ResourcesMetadata.PROPERTY_DC_PUBLISHER);
			dc.add(ResourcesMetadata.PROPERTY_DC_SUBJECT);
			dc.add(ResourcesMetadata.PROPERTY_DC_CREATED);
			dc.add(ResourcesMetadata.PROPERTY_DC_ISSUED);
			// dc.add(ResourcesMetadata.PROPERTY_DC_MODIFIED);
			// dc.add(ResourcesMetadata.PROPERTY_DC_TABLEOFCONTENTS);
			dc.add(ResourcesMetadata.PROPERTY_DC_ABSTRACT);
			dc.add(ResourcesMetadata.PROPERTY_DC_CONTRIBUTOR);
			// dc.add(ResourcesMetadata.PROPERTY_DC_TYPE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_FORMAT);
			// dc.add(ResourcesMetadata.PROPERTY_DC_IDENTIFIER);
			// dc.add(ResourcesMetadata.PROPERTY_DC_SOURCE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_LANGUAGE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_COVERAGE);
			// dc.add(ResourcesMetadata.PROPERTY_DC_RIGHTS);
			dc.add(ResourcesMetadata.PROPERTY_DC_AUDIENCE);
			dc.add(ResourcesMetadata.PROPERTY_DC_EDULEVEL);
			
			/* Filesystem and file-like mount points */
			dc.add(ResourcesMetadata.PROPERTY_FSMOUNT_ACTIVE);
				
			metadataGroups.add(dc);
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		/*
		// define DublinCore
		if(!metadataGroups.contains(new MetadataGroup("Test of Datatypes")))
		{
			MetadataGroup dc = new MetadataGroup("Test of Datatypes");
			dc.add(ResourcesMetadata.PROPERTY_DC_TITLE);
			dc.add(ResourcesMetadata.PROPERTY_DC_DESCRIPTION);
			dc.add(ResourcesMetadata.PROPERTY_DC_ANYURI);
			dc.add(ResourcesMetadata.PROPERTY_DC_DOUBLE);
			dc.add(ResourcesMetadata.PROPERTY_DC_DATETIME);
			dc.add(ResourcesMetadata.PROPERTY_DC_TIME);
			dc.add(ResourcesMetadata.PROPERTY_DC_DATE);
			dc.add(ResourcesMetadata.PROPERTY_DC_BOOLEAN);
			dc.add(ResourcesMetadata.PROPERTY_DC_INTEGER);
			metadataGroups.add(dc);
			state.setAttribute(STATE_METADATA_GROUPS, metadataGroups);
		}
		*/
	}

	/**
	 * Internal class that encapsulates all information about a resource that is needed in the browse mode.
	 * This is being phased out as we switch to the resources type registry.
	 */
	public static class ChefBrowseItem
	{
		protected static Integer seqnum = new Integer(0);
		private String m_itemnum;
		
		// attributes of all resources
		protected String m_name;
		protected String m_id;
		protected String m_type;
		
		protected SortedSet m_allSiteGroups;
		protected SortedSet m_inheritedGroupRefs;
		protected SortedSet m_entityGroupRefs;
		protected SortedSet m_allowedRemoveGroupRefs;
		protected SortedSet m_allowedAddGroupRefs;
		protected Map m_allSiteGroupsMap;
		
		protected boolean m_canRead;
		protected boolean m_canRevise;
		protected boolean m_canDelete;
		protected boolean m_canCopy;
		protected boolean m_isCopied;
		protected boolean m_canAddItem;
		protected boolean m_canAddFolder;
		protected boolean m_canSelect;
		
		protected boolean m_available;
		
		protected boolean m_inDropbox;

		protected List m_members;
		protected boolean m_isEmpty;
		protected boolean m_isHighlighted;
		protected boolean m_inheritsHighlight;
		protected String m_createdBy;
		protected String m_createdTime;
		protected String m_modifiedBy;
		protected String m_modifiedTime;
		protected String m_size;
		protected String m_target;
		protected String m_container;
		protected String m_root;
		protected int m_depth;
		protected boolean m_hasDeletableChildren;
		protected boolean m_hasCopyableChildren;
		protected boolean m_copyrightAlert;
		protected String m_url;
		protected boolean m_isLocal;
		protected boolean m_isAttached;
		private boolean m_isMoved;
		private boolean m_canUpdate;
		private boolean m_toobig;
		protected String m_access;
		protected String m_inheritedAccess;
		protected Collection m_groups;
		
		protected Collection m_oldInheritedGroups;
		protected Collection m_oldPossibleGroups;
		protected BasicRightsAssignment m_rights;

		protected boolean m_pubview;
		protected boolean m_pubview_inherited;
		protected boolean m_pubview_possible;
		protected boolean m_sortable;
		protected boolean m_locked = false;
		
		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public ChefBrowseItem(String id, String name, String type)
		{
			m_name = name;
			m_id = id;
			m_type = type;
			
			Integer snum; 
			synchronized(seqnum)
			{
				snum = seqnum;
				seqnum = new Integer((seqnum.intValue() + 1) % 10000);
			}
			m_itemnum = "Item00000000".substring(0,10 - snum.toString().length()) + snum.toString();

			m_allowedRemoveGroupRefs = new TreeSet();
			m_allowedAddGroupRefs = new TreeSet();
			m_allSiteGroups = new TreeSet(new Comparator()
			{
				protected final String DELIM = "::";
				public int compare(Object arg0, Object arg1) 
				{
					Group group0 = (Group) arg0;
					Group group1 = (Group) arg1;
					String string0 = group0.getTitle() + DELIM + group0.getId();
					String string1 = group1.getTitle() + DELIM + group1.getId();
					
					return string0.compareTo(string1);
				}
			});
			m_entityGroupRefs = new TreeSet();
			m_inheritedGroupRefs = new TreeSet();
			m_allSiteGroupsMap = new Hashtable();

			// set defaults
			m_rights = new BasicRightsAssignment(m_itemnum, false);
			m_members = new LinkedList();
			m_canRead = false;
			m_canRevise = false;
			m_canDelete = false;
			m_canCopy = false;
			m_available = true;
			m_isEmpty = true;
			m_toobig = false;
			m_isCopied = false;
			m_isMoved = false;
			m_isAttached = false;
			m_canSelect = true; // default is true.
			m_hasDeletableChildren = false;
			m_hasCopyableChildren = false;
			m_createdBy = "";
			m_modifiedBy = "";
			// m_createdTime = TimeService.newTime().toStringLocalDate();
			// m_modifiedTime = TimeService.newTime().toStringLocalDate();
			m_size = "";
			m_depth = 0;
			m_copyrightAlert = false;
			m_url = "";
			m_target = "";
			m_root = "";

			m_pubview = false;
			m_pubview_inherited = false;
			m_pubview_possible = true;
			
			m_isHighlighted = false;
			m_inheritsHighlight = false;

			m_canAddItem = false;
			m_canAddFolder = false;
			m_canUpdate = false;
			
			m_access = AccessMode.INHERITED.toString();
			m_groups = new Vector();
		
		}

		public void setLocked(boolean isLocked) 
		{
			m_locked  = isLocked;
			
		}
		
		public boolean isLocked()
		{
			return m_locked;
		}

		public String getItemNum()
		{
			return m_itemnum;
		}
		
		public boolean isAvailable()
		{
			return m_available;
		}

		public void setAvailable(boolean available)
		{
			m_available = available;
		}

		public boolean isInherited(Group group)
		{
			return this.m_inheritedGroupRefs.contains(group.getReference());
		}
		
		public boolean isLocal(Group group)
		{
			return this.m_entityGroupRefs.contains(group.getReference());
		}
		
		public boolean isPossible(Group group)
		{
			boolean rv = false;
			
			if(AccessMode.GROUPED.toString().equals(this.m_inheritedAccess))
			{
				rv = this.m_inheritedGroupRefs.contains(group.getReference());
			}
			else
			{
				rv = this.m_allSiteGroupsMap.containsKey(group.getReference());
			}
			
			return rv;
		}
		
		public boolean allowedRemove(Group group)
		{
			return this.m_allowedRemoveGroupRefs.contains(group.getReference());
		}
		
		public SortedSet getAllowedRemoveGroupRefs() 
		{
			return m_allowedRemoveGroupRefs;
		}

		public void setAllowedRemoveGroupRefs(Collection allowedRemoveGroupRefs) 
		{
			importGroupRefs(allowedRemoveGroupRefs, this.m_allowedRemoveGroupRefs);
		}

		public void addAllowedRemoveGroupRef(String allowedRemoveGroupRef) 
		{
			addGroupRefToCollection(allowedRemoveGroupRef, m_allowedRemoveGroupRefs);
		}

		public boolean allowedAdd(Group group)
		{
			return this.m_allowedAddGroupRefs.contains(group.getReference());
		}
		
		public SortedSet getAllowedAddGroupRefs() 
		{
			return m_allowedAddGroupRefs;
		}

		public void setAllowedAddGroupRefs(Collection allowedAddGroupRefs) 
		{
			importGroupRefs(allowedAddGroupRefs, this.m_allowedAddGroupRefs);
		}

		public void addAllowedAddGroupRef(String allowedAddGroupRef) 
		{
			addGroupRefToCollection(allowedAddGroupRef, m_allowedAddGroupRefs);
		}

		public List getAllSiteGroups() 
		{
			return new Vector(m_allSiteGroups);
		}

		public void setAllSiteGroups(Collection allSiteGroups) 
		{
			this.m_allSiteGroups.clear();
			this.m_allSiteGroupsMap.clear();
			addAllSiteGroups(allSiteGroups);
		}

		public void addAllSiteGroups(Collection allSiteGroups) 
		{
			Iterator it = allSiteGroups.iterator();
			while(it.hasNext())
			{
				Group group = (Group) it.next();
				if(! m_allSiteGroupsMap.containsKey(group.getReference()))
				{
					this.m_allSiteGroups.add(group);
					m_allSiteGroupsMap.put(group.getReference(), group);
					m_allSiteGroupsMap.put(group.getId(), group);
				}
			}
		}

		public SortedSet getEntityGroupRefs() 
		{
			return m_entityGroupRefs;
		}

		public void setEntityGroupRefs(Collection entityGroupRefs) 
		{
			importGroupRefs(entityGroupRefs, this.m_entityGroupRefs);
		}

		public void addEntityGroupRef(String entityGroupRef) 
		{
			addGroupRefToCollection(entityGroupRef, m_entityGroupRefs);
		}

		public SortedSet getInheritedGroupRefs() 
		{
			return m_inheritedGroupRefs;
		}

		public void setInheritedGroupRefs(Collection inheritedGroupRefs) 
		{
			importGroupRefs(inheritedGroupRefs, this.m_inheritedGroupRefs);
		}

		public void addInheritedGroupRef(String inheritedGroupRef) 
		{
			addGroupRefToCollection(inheritedGroupRef, m_inheritedGroupRefs);
		}

		protected void importGroupRefs(Collection groupRefs, Collection collection) 
		{
			collection.clear();
			Iterator it = groupRefs.iterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if(obj instanceof Group)
				{
					addGroupRefToCollection(((Group) obj).getReference(), collection);
				}
				else if(obj instanceof String)
				{
					addGroupRefToCollection((String) obj, collection);
				}
			}
		}

		protected void addGroupRefToCollection(String groupRef, Collection collection) 
		{
			Group group = (Group) m_allSiteGroupsMap.get(groupRef);
			if(group != null)
			{
				if(! collection.contains(group.getReference()))
				{
					collection.add(group.getReference());
				}
			}
		}

		public void setIsTooBig(boolean toobig)
		{
			m_toobig = toobig;
		}

		public boolean isTooBig()
		{
			return m_toobig;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/**
		 * @param root
		 */
		public void setRoot(String root)
		{
			m_root = root;
		}

		/**
		 * @return
		 */
		public String getRoot()
		{
			return m_root;
		}

		/**
		 * @return
		 */
		public List getMembers()
		{
			List rv = new LinkedList();
			if(m_members != null)
			{
				rv.addAll(m_members);
			}
			return rv;
		}

		/**
		 * @param members
		 */
		public void addMembers(Collection members)
		{
			if(m_members == null)
			{
				m_members = new LinkedList();
			}
			m_members.addAll(members);
		}

		/**
		 * @return
		 */
		public boolean canAddItem()
		{
			return m_canAddItem;
		}

		/**
		 * @return
		 */
		public boolean canDelete()
		{
			return m_canDelete;
		}

		/**
		 * @return
		 */
		public boolean canRead()
		{
			return m_canRead;
		}

      public boolean canSelect() {
         return m_canSelect;
      }

		/**
		 * @return
		 */
		public boolean canRevise()
		{
			return m_canRevise;
		}

		/**
		 * @return
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @return
		 */
		public int getDepth()
		{
			return m_depth;
		}

		/**
		 * @param depth
		 */
		public void setDepth(int depth)
		{
			m_depth = depth;
		}

		/**
		 * @param canCreate
		 */
		public void setCanAddItem(boolean canAddItem)
		{
			m_canAddItem = canAddItem;
		}

		/**
		 * @param canDelete
		 */
		public void setCanDelete(boolean canDelete)
		{
			m_canDelete = canDelete;
		}

		/**
		 * @param canRead
		 */
		public void setCanRead(boolean canRead)
		{
			m_canRead = canRead;
		}

      public void setCanSelect(boolean canSelect) {
         m_canSelect = canSelect;
      }

		/**
		 * @param canRevise
		 */
		public void setCanRevise(boolean canRevise)
		{
			m_canRevise = canRevise;
		}

		/**
		 * @return
		 */
		public boolean isFolder()
		{
			return TYPE_FOLDER.equals(m_type);
		}

		/**
		 * @return
		 */
		public String getType()
		{
			return m_type;
		}

		/**
		 * @return
		 */
		public boolean canAddFolder()
		{
			return m_canAddFolder;
		}

		/**
		 * @param b
		 */
		public void setCanAddFolder(boolean canAddFolder)
		{
			m_canAddFolder = canAddFolder;
		}

		/**
		 * @return
		 */
		public boolean canCopy()
		{
			return m_canCopy;
		}

		/**
		 * @param canCopy
		 */
		public void setCanCopy(boolean canCopy)
		{
			m_canCopy = canCopy;
		}

		/**
		 * @return
		 */
		public boolean hasCopyrightAlert()
		{
			return m_copyrightAlert;
		}

		/**
		 * @param copyrightAlert
		 */
		public void setCopyrightAlert(boolean copyrightAlert)
		{
			m_copyrightAlert = copyrightAlert;
		}

		/**
		 * @return
		 */
		public String getUrl()
		{
			return m_url;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

		/**
		 * @return
		 */
		public boolean isCopied()
		{
			return m_isCopied;
		}

		/**
		 * @param isCopied
		 */
		public void setCopied(boolean isCopied)
		{
			m_isCopied = isCopied;
		}

		/**
		 * @return
		 */
		public boolean isMoved()
		{
			return m_isMoved;
		}

		/**
		 * @param isCopied
		 */
		public void setMoved(boolean isMoved)
		{
			m_isMoved = isMoved;
		}

		/**
		 * @return
		 */
		public String getCreatedBy()
		{
			return m_createdBy;
		}

		/**
		 * @return
		 */
		public String getCreatedTime()
		{
			return m_createdTime;
		}

		/**
		 * @return
		 */
		public String getModifiedBy()
		{
			return m_modifiedBy;
		}

		/**
		 * @return
		 */
		public String getModifiedTime()
		{
			return m_modifiedTime;
		}

		/**
		 * @return
		 */
		public String getSize()
		{
			if(m_size == null)
			{
				m_size = "";
			}
			return m_size;
		}

		/**
		 * @param creator
		 */
		public void setCreatedBy(String creator)
		{
			m_createdBy = creator;
		}

		/**
		 * @param time
		 */
		public void setCreatedTime(String time)
		{
			m_createdTime = time;
		}

		/**
		 * @param modifier
		 */
		public void setModifiedBy(String modifier)
		{
			m_modifiedBy = modifier;
		}

		/**
		 * @param time
		 */
		public void setModifiedTime(String time)
		{
			m_modifiedTime = time;
		}

		/**
		 * @param size
		 */
		public void setSize(String size)
		{
			m_size = size;
		}

		/**
		 * @return
		 */
		public String getTarget()
		{
			return m_target;
		}

		/**
		 * @param target
		 */
		public void setTarget(String target)
		{
			m_target = target;
		}

		/**
		 * @return
		 */
		public boolean isEmpty()
		{
			return m_isEmpty;
		}

		/**
		 * @param isEmpty
		 */
		public void setIsEmpty(boolean isEmpty)
		{
			m_isEmpty = isEmpty;
		}

		/**
		 * @return
		 */
		public String getContainer()
		{
			return m_container;
		}

		/**
		 * @param container
		 */
		public void setContainer(String container)
		{
			m_container = container;
		}

		public void setIsLocal(boolean isLocal)
		{
			m_isLocal = isLocal;
		}

		public boolean isLocal()
		{
			return m_isLocal;
		}

		/**
		 * @return Returns the isAttached.
		 */
		public boolean isAttached()
		{
			return m_isAttached;
		}
		/**
		 * @param isAttached The isAttached to set.
		 */
		public void setAttached(boolean isAttached)
		{
			this.m_isAttached = isAttached;
		}

		/**
		 * @return Returns the hasCopyableChildren.
		 */
		public boolean hasCopyableChildren()
		{
			return m_hasCopyableChildren;
		}

		/**
		 * @param hasCopyableChildren The hasCopyableChildren to set.
		 */
		public void setCopyableChildren(boolean hasCopyableChildren)
		{
			this.m_hasCopyableChildren = hasCopyableChildren;
		}

		/**
		 * @return Returns the hasDeletableChildren.
		 */
		public boolean hasDeletableChildren()
		{
			return m_hasDeletableChildren;
		}

		/**
		 * @param hasDeletableChildren The hasDeletableChildren to set.
		 */
		public void seDeletableChildren(boolean hasDeletableChildren)
		{
			this.m_hasDeletableChildren = hasDeletableChildren;
		}

		/**
		 * @return Returns the canUpdate.
		 */
		public boolean canUpdate()
		{
			return m_canUpdate;
		}

		/**
		 * @param canUpdate The canUpdate to set.
		 */
		public void setCanUpdate(boolean canUpdate)
		{
			m_canUpdate = canUpdate;
		}

		public void setHighlighted(boolean isHighlighted)
		{
			m_isHighlighted = isHighlighted;
		}

		public boolean isHighlighted()
		{
			return m_isHighlighted;
		}

		public void setInheritsHighlight(boolean inheritsHighlight)
		{
			m_inheritsHighlight = inheritsHighlight;
		}

		public boolean inheritsHighlighted()
		{
			return m_inheritsHighlight;
		}

		/**
		 * Access the access mode for this item.
		 * @return The access mode.
		 */
		public String getAccess()
		{
			return m_access;
		}

		/**
		 * Access the access mode for this item.
		 * @return The access mode.
		 */
		public String getInheritedAccess()
		{
			return m_inheritedAccess;
		}
		
		public String getEntityAccess()
		{
			String rv = AccessMode.INHERITED.toString();
			boolean sameGroups = true;
			if(AccessMode.GROUPED.toString().equals(m_access))
			{
				Iterator it = getGroups().iterator();
				while(sameGroups && it.hasNext())
				{
					Group g = (Group) it.next();
					sameGroups = inheritsGroup(g.getReference());
				}
				it = getInheritedGroups().iterator();
				while(sameGroups && it.hasNext())
				{
					Group g = (Group) it.next();
					sameGroups = hasGroup(g.getReference());
				}
				if(!sameGroups)
				{
					rv = AccessMode.GROUPED.toString();
				}
			}
			return rv;
		}
		
		public String getEffectiveAccess()
		{
			String rv = this.m_access;
			if(AccessMode.INHERITED.toString().equals(rv))
			{
				rv = this.m_inheritedAccess;
			}
			if(AccessMode.INHERITED.toString().equals(rv))
			{
				rv = AccessMode.SITE.toString();
			}
			return rv;
		}
		
		public String getEffectiveGroups()
		{
			String rv = rb.getString("access.site1");
			
			if(this.isPubviewInherited())
			{
				rv = rb.getString("access.public1");
			}
			else if(this.isPubview())
			{
				rv = rb.getString("access.public1");
			}
			else if(this.isInDropbox())
			{
				rv = rb.getString("access.dropbox1");
			}
			else if(AccessMode.GROUPED.toString().equals(getEffectiveAccess()))
			{
				rv = (String) rb.getFormattedMessage("access.group1",  new Object[]{getGroupNames()});
			}
			return rv;
		}
		
		public Collection getPossibleGroups()
		{
			return m_oldPossibleGroups;
		}
		
		public void setPossibleGroups(Collection groups)
		{
			m_oldPossibleGroups = groups;
		}
		
		public String getGroupNames()
		{
			String rv = "";
			
			Collection groupRefs = this.m_entityGroupRefs;
			if(groupRefs == null || groupRefs.isEmpty())
			{
				groupRefs = this.m_inheritedGroupRefs;
			}
			Iterator it = groupRefs.iterator();
			while(it.hasNext())
			{
				String groupRef = (String) it.next();
				Group group = (Group) this.m_allSiteGroupsMap.get(groupRef);
				if(group != null)
				{
					if(rv.length() == 0)
					{
						rv += group.getTitle();
					}
					else
					{
						rv += ", " + group.getTitle();
					}
				}
			}
			
			// TODO: After updating getBrowserItems, get rid of this part
			if(rv.length() == 0)
			{
				Collection groups = getGroups();
				if(groups == null || groups.isEmpty())
				{
					groups = getInheritedGroups();
				}
				
				Iterator grit = groups.iterator();
				while(grit.hasNext())
				{
					Group g = (Group) grit.next();
					rv += g.getTitle();
					if(grit.hasNext())
					{
						rv += ", ";
					}
				}
			}
			
			return rv;
		}

		/**
		 * Set the access mode for this item.
		 * @param access
		 */
		public void setAccess(String access)
		{
			m_access = access;
		}

		/**
		 * Set the access mode for this item.
		 * @param access
		 */
		public void setInheritedAccess(String access)
		{
			m_inheritedAccess = access;
		}

		/**
		 * Access a list of Group objects that can access this item.
		 * @return Returns the groups.
		 */
		public List getGroups()
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			return new Vector(m_groups);
		}
		
		/**
		 * Access a list of Group objects that can access this item.
		 * @return Returns the groups.
		 */
		public List getInheritedGroups()
		{
			if(m_oldInheritedGroups == null)
			{
				m_oldInheritedGroups = new Vector();
			}
			return new Vector(m_oldInheritedGroups);
		}
		
		/**
		 * Determine whether a group has access to this item. 
		 * @param groupRef The internal reference string that uniquely identifies the group.
		 * @return true if the group has access, false otherwise.
		 */
		public boolean hasGroup(String groupRef)
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			boolean found = false;
			Iterator it = m_groups.iterator();
			while(it.hasNext() && !found)
			{
				Group gr = (Group) it.next();
				found = gr.getReference().equals(groupRef);
			}
	
			return found;
		}

		/**
		 * Determine whether a group has access to this item. 
		 * @param groupRef The internal reference string that uniquely identifies the group.
		 * @return true if the group has access, false otherwise.
		 */
		public boolean inheritsGroup(String groupRef)
		{
			if(m_oldInheritedGroups == null)
			{
				m_oldInheritedGroups = new Vector();
			}
			boolean found = false;
			Iterator it = m_oldInheritedGroups.iterator();
			while(it.hasNext() && !found)
			{
				Group gr = (Group) it.next();
				found = gr.getReference().equals(groupRef);
			}
	
			return found;
		}

		/**
		 * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
		 * @param groups The groups to set.
		 */
		public void setGroups(Collection groups)
		{
			if(groups == null)
			{
				return;
			}
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			m_groups.clear();
			Iterator it = groups.iterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if(obj instanceof Group && ! hasGroup(((Group) obj).getReference()))
				{
					m_groups.add(obj);
				}
				else if(obj instanceof String && ! hasGroup((String) obj))
				{
					addGroup((String) obj);
				}
			}
		}
		
		/**
		 * Replace the current list of groups with this list of Group objects representing the groups that have access to this item.
		 * @param groups The groups to set.
		 */
		public void setInheritedGroups(Collection groups)
		{
			if(groups == null)
			{
				return;
			}
			if(m_oldInheritedGroups == null)
			{
				m_oldInheritedGroups = new Vector();
			}
			m_oldInheritedGroups.clear();
			Iterator it = groups.iterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if(obj instanceof Group && ! inheritsGroup(((Group) obj).getReference()))
				{
					m_oldInheritedGroups.add(obj);
				}
				else if(obj instanceof String && ! hasGroup((String) obj))
				{
					addInheritedGroup((String) obj);
				}
			}
		}
		
		/**
		 * Add a string reference identifying a Group to the list of groups that have access to this item.
		 * @param groupRef
		 */
		public void addGroup(String groupId)
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			if(m_container == null)
			{
				if(m_id == null)
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}
				else
				{
					m_container = ContentHostingService.getContainingCollectionId(m_id);
				}
				if(m_container == null || m_container.trim() == "")
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}

			}
			boolean found = false;
			Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
			Iterator it = groups.iterator();
			while( it.hasNext() && !found )
			{
				Group group = (Group) it.next();
				if(group.getId().equals(groupId))
				{
					if(! hasGroup(group.getReference()))
					{
						m_groups.add(group);
					}
					found = true;
				}
			}

		}
		
		/**
		 * Add a Group to the list of groups that have access to this item.
		 * @param group The Group object to be added
		 */
		public void addGroup(Group group) 
		{
			if(m_groups == null)
			{
				m_groups = new Vector();
			}
			if(! hasGroup(group.getReference()))
			{
				m_groups.add(group);
			}
		}


		
		/**
		 * Add a string reference identifying a Group to the list of groups that have access to this item.
		 * @param groupRef
		 */
		public void addInheritedGroup(String groupId)
		{
			if(m_oldInheritedGroups == null)
			{
				m_oldInheritedGroups = new Vector();
			}
			if(m_container == null)
			{
				if(m_id == null)
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}
				else
				{
					m_container = ContentHostingService.getContainingCollectionId(m_id);
				}
				if(m_container == null || m_container.trim() == "")
				{
					m_container = ContentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
				}

			}
			boolean found = false;
			Collection groups = ContentHostingService.getGroupsWithReadAccess(m_container);
			Iterator it = groups.iterator();
			while( it.hasNext() && !found )
			{
				Group group = (Group) it.next();
				String gid = group.getId();
				String gref = group.getReference();
				if(gid.equals(groupId) || gref.equals(groupId))
				{
					if(! inheritsGroup(group.getReference()))
					{
						m_oldInheritedGroups.add(group);
					}
					found = true;
				}
			}

		}
		
		/**
		 * Remove all groups from the item.
		 */
		public void clearGroups()
		{
			if(this.m_groups == null)
			{
				m_groups = new Vector();
			}
			m_groups.clear();
		}

		/**
		 * Remove all inherited groups from the item.
		 */
		public void clearInheritedGroups()
		{
			if(m_oldInheritedGroups == null)
			{
				m_oldInheritedGroups = new Vector();
			}
			m_oldInheritedGroups.clear();
		}

		/**
		 * @return Returns the pubview.
		 */
		public boolean isPubview() 
		{
			return m_pubview;
		}
		/**
		 * @param pubview The pubview to set.
		 */
		public void setPubview(boolean pubview) 
		{
			m_pubview = pubview;
		}
		
		/**
		 * @param pubview The pubview to set.
		 */
		public void setPubviewPossible(boolean possible) 
		{
			m_pubview_possible = possible;
		}
		
		/**
		 * @return Returns the pubviewset.
		 */
		public boolean isPubviewInherited() 
		{
			return m_pubview_inherited;
		}
		
		/**
		 * 
		 *
		 */
		public boolean isPubviewPossible()
		{
			return m_pubview_possible;
		}
		
		/**
		 * @param pubviewset The pubviewset to set.
		 */
		public void setPubviewInherited(boolean pubviewset) 
		{
			m_pubview_inherited = pubviewset;
		}

		/**
		 * @return Returns the rights.
		 */
		public BasicRightsAssignment getRights()
		{
			return m_rights;
		}

		/**
		 * @param rights The rights to set.
		 */
		public void setRights(BasicRightsAssignment rights)
		{
			this.m_rights = rights;
		}

		/**
		 * @return Returns true if the item is in a dropbox (assuming it's been initialized correctly).
		 */
		public boolean isInDropbox() 
		{
			return m_inDropbox;
		}

		/**
		 * @param inDropbox The value for inDropbox to set.
		 */
		public void setInDropbox(boolean inDropbox) 
		{
			this.m_inDropbox = inDropbox;
		}

		public boolean isSortable()
		{
			return m_sortable;
		}
		
		public void setSortable(boolean sortable)
		{
			m_sortable = sortable;
		}
		
	}	// inner class ChefBrowseItem


	/**
	 * Inner class encapsulates information about resources (folders and items) for editing
	 * This is being phased out as we switch to the resources type registry.
	 */
	public static class ChefEditItem
		extends ChefBrowseItem
	{
		protected String m_copyrightStatus;
		protected String m_copyrightInfo;
		// protected boolean m_copyrightAlert;
		
		protected String m_filename;
		protected byte[] m_content;
		protected String m_encoding = UTF_8_ENCODING;

		protected String m_mimetype;
		protected String m_description;
		protected Map m_metadata;
		protected boolean m_hasQuota;
		protected boolean m_canSetQuota;
		protected String m_quota;
		protected boolean m_isUrl;
		protected boolean m_contentHasChanged;
		protected boolean m_contentTypeHasChanged;
		protected int m_notification = NotificationService.NOTI_NONE;

		protected List m_properties;

		protected Set m_metadataGroupsShowing;

		protected Set m_missingInformation;
		protected boolean m_hasBeenAdded;
		protected ResourcesMetadata m_form;
		protected boolean m_isBlank;
		protected String m_instruction;
		protected String m_ccRightsownership;
		protected String m_ccLicense;
		protected String m_ccCommercial;
		protected String m_ccModification;
		protected String m_ccRightsOwner;
		protected String m_ccRightsYear;
		
		protected boolean m_hidden;
		protected Time m_releaseDate;
		protected Time m_retractDate;
		protected boolean m_useReleaseDate;
		protected boolean m_useRetractDate;
		private boolean m_isInUserSite;

		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public ChefEditItem(String id, String name, String type)
		{
			super(id, name, type);
			
			m_filename = "";
			m_contentHasChanged = false;
			m_contentTypeHasChanged = false;
			m_metadata = new Hashtable();
			m_metadataGroupsShowing = new HashSet();
			m_mimetype = type;
			m_content = null;
			m_encoding = UTF_8_ENCODING;
			m_notification = NotificationService.NOTI_NONE;
			m_hasQuota = false;
			m_canSetQuota = false;
			m_missingInformation = new HashSet();
			m_hasBeenAdded = false;
			m_properties = new Vector();
			m_isBlank = true;
			m_ccRightsownership = "";
			m_ccLicense = "";
			// m_copyrightStatus = ServerConfigurationService.getString("default.copyright");
			
			m_hidden = false;
			m_releaseDate = TimeService.newTime();
			m_retractDate = TimeService.newTime();
			m_useReleaseDate = false;
			m_useRetractDate = false;

		
		}
		
		public void setInWorkspace(boolean isInUserSite) 
		{
			m_isInUserSite = isInUserSite;
		}
		
		public boolean isInWorkspace()
		{
			return m_isInUserSite;
		}

		public void setHidden(boolean hidden) 
		{
			this.m_hidden = hidden;
		}
		
		public boolean isHidden()
		{
			return this.m_hidden;
		}
		
		public SortedSet convertToRefs(Collection groupIds) 
		{
			SortedSet groupRefs = new TreeSet();
			Iterator it = groupIds.iterator();
			while(it.hasNext())
			{
				String groupId = (String) it.next();
				Group group = (Group) this.m_allSiteGroupsMap.get(groupId);
				if(group != null)
				{
					groupRefs.add(group.getReference());
				}
			}
			return groupRefs;
			
		}

		public void setRightsowner(String ccRightsOwner)
		{
			m_ccRightsOwner = ccRightsOwner;
		}
		
		public String getRightsowner()
		{
			return m_ccRightsOwner;
		}

		public void setRightstyear(String ccRightsYear)
		{
			m_ccRightsYear = ccRightsYear;
		}
		
		public String getRightsyear()
		{
			return m_ccRightsYear;
		}

		public void setAllowModifications(String ccModification)
		{
			m_ccModification = ccModification;
		}
		
		public String getAllowModifications()
		{
			return m_ccModification;
		}

		public void setAllowCommercial(String ccCommercial)
		{
			m_ccCommercial = ccCommercial;
		}
		
		public String getAllowCommercial()
		{
			return m_ccCommercial;
		}

		/**
		 * 
		 * @param license
		 */
		public void setLicense(String license)
		{
			m_ccLicense = license;
		}
		
		/**
		 * 
		 * @return
		 */
		public String getLicense()
		{
			return m_ccLicense;
		}

		/**
		 * Set the character encoding type that will be used when converting content body between strings and byte arrays.
		 * Default is UTF_8_ENCODING.
		 * @param encoding A valid name for a character set encoding scheme (@see java.lang.Charset)
		 */
		public void setEncoding(String encoding)
		{
			m_encoding = encoding;
		}

		/**
		 * Get the character encoding type that is used when converting content body between strings and byte arrays.
		 * Default is "UTF-8".
		 * @return The name of the character set encoding scheme (@see java.lang.Charset)
		 */
		public String getEncoding()
		{
			return m_encoding;
		}

		/**
		 * Set marker indicating whether current item is a blank entry
		 * @param isBlank
		 */
		public void markAsBlank(boolean isBlank)
		{
			m_isBlank = isBlank;
		}

		/**
		 * Access marker indicating whether current item is a blank entry
		 * @return true if current entry is blank, false otherwise
		 */
		public boolean isBlank()
		{
			return m_isBlank;
		}

		/**
		 * @param properties
		 */
		public void setProperties(List properties)
		{
			m_properties = properties;

		}

		public List getProperties()
		{
			return m_properties;
		}

		/**
		 * @param id
		 * @param name
		 * @param type
		 */
		public ChefEditItem(String type)
		{
			this(null, "", type);
		}
		
		/**
		 * @param id
		 */
		public void setId(String id)
		{
			m_id = id;
		}

		/**
		 * Show the indicated metadata group for the item
		 * @param group
		 */
		public void showMetadataGroup(String group)
		{
			m_metadataGroupsShowing.add(group);
		}

		/**
		 * Hide the indicated metadata group for the item
		 * @param group
		 */
		public void hideMetadataGroup(String group)
		{
			m_metadataGroupsShowing.remove(group);
			m_metadataGroupsShowing.remove(Validator.escapeUrl(group));
		}

		/**
		 * Query whether the indicated metadata group is showing for the item
		 * @param group
		 * @return true if the metadata group is showing, false otherwise
		 */
		public boolean isGroupShowing(String group)
		{
			return m_metadataGroupsShowing.contains(group) || m_metadataGroupsShowing.contains(Validator.escapeUrl(group));
		}

		/**
		 * @return
		 */
		public boolean isFileUpload()
		{
			return !isFolder() && !isUrl() && !isHtml() && !isPlaintext();
		}

		/**
		 * @param type
		 */
		public void setType(String type)
		{
			m_type = type;
		}

		/**
		 * @param mimetype
		 */
		public void setMimeType(String mimetype)
		{
			m_mimetype = mimetype;
		}
		
		public String getRightsownership()
		{
			return m_ccRightsownership;
		}
		
		public void setRightsownership(String owner)
		{
			m_ccRightsownership = owner;
		}

		/**
		 * @return
		 */
		public String getMimeType()
		{
			return m_mimetype;
		}

		public String getMimeCategory()
		{
			if(this.m_mimetype == null || this.m_mimetype.equals(""))
			{
				return "";
			}
			int index = this.m_mimetype.indexOf("/");
			if(index < 0)
			{
				return this.m_mimetype;
			}
			return this.m_mimetype.substring(0, index);
		}

		public String getMimeSubtype()
		{
			if(this.m_mimetype == null || this.m_mimetype.equals(""))
			{
				return "";
			}
			int index = this.m_mimetype.indexOf("/");
			if(index < 0 || index + 1 == this.m_mimetype.length())
			{
				return "";
			}
			return this.m_mimetype.substring(index + 1);
		}

		/**
		 * @return Returns the copyrightInfo.
		 */
		public String getCopyrightInfo() {
			return m_copyrightInfo;
		}
		/**
		 * @param copyrightInfo The copyrightInfo to set.
		 */
		public void setCopyrightInfo(String copyrightInfo) {
			m_copyrightInfo = copyrightInfo;
		}
		/**
		 * @return Returns the copyrightStatus.
		 */
		public String getCopyrightStatus() {
			return m_copyrightStatus;
		}
		/**
		 * @param copyrightStatus The copyrightStatus to set.
		 */
		public void setCopyrightStatus(String copyrightStatus) {
			m_copyrightStatus = copyrightStatus;
		}
		/**
		 * @return Returns the description.
		 */
		public String getDescription() {
			return m_description;
		}
		/**
		 * @param description The description to set.
		 */
		public void setDescription(String description) {
			m_description = description;
		}
		/**
		 * @return Returns the filename.
		 */
		public String getFilename() {
			return m_filename;
		}
		/**
		 * @param filename The filename to set.
		 */
		public void setFilename(String filename) {
			m_filename = filename;
		}
		/**
		 * @return Returns the metadata.
		 */
		public Map getMetadata() {
			return m_metadata;
		}
		/**
		 * @param metadata The metadata to set.
		 */
		public void setMetadata(Map metadata) {
			m_metadata = metadata;
		}
		/**
		 * @param name
		 * @param value
		 */
		public void setMetadataItem(String name, Object value)
		{
			m_metadata.put(name, value);
		}
		public boolean isSitePossible()
		{
			return !m_pubview_inherited && !isGroupInherited() && !isSingleGroupInherited();
		}
		
		public boolean isGroupPossible()
		{
			// Collection groups = getPossibleGroups();
			// return ! groups.isEmpty();
			return this.m_allowedAddGroupRefs != null && ! this.m_allowedAddGroupRefs.isEmpty();

		}
		
		public boolean isGroupInherited()
		{
			return AccessMode.INHERITED.toString().equals(this.m_access) && AccessMode.GROUPED.toString().equals(m_inheritedAccess);
		}
		
		/**
		 * Does this entity inherit grouped access mode with a single group that has access?
		 * @return true if this entity inherits grouped access mode with a single group that has access, and false otherwise.
		 */
		public boolean isSingleGroupInherited()
		{
			//Collection groups = getInheritedGroups();
			return // AccessMode.INHERITED.toString().equals(this.m_access) && 
					AccessMode.GROUPED.toString().equals(this.m_inheritedAccess) && 
					this.m_inheritedGroupRefs != null && 
					this.m_inheritedGroupRefs.size() == 1; 
					// && this.m_oldInheritedGroups != null 
					// && this.m_oldInheritedGroups.size() == 1;
		}
		
		public String getSingleGroupTitle()
		{
			return (String) rb.getFormattedMessage("access.title4", new Object[]{getGroupNames()});
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
		 * @return Returns the content.
		 */
		public byte[] getContent() 
		{
			return m_content;
		}
		/**
		 * @return Returns the content as a String.
		 */
		public String getContentstring()
		{
			String rv = "";
			if(m_content != null && m_content.length > 0)
			{
				try
				{
					rv = new String( m_content, m_encoding );
				}
				catch(UnsupportedEncodingException e)
				{
					rv = new String( m_content );
				}
			}
			return rv;
		}
		/**
		 * @param content The content to set.
		 */
		public void setContent(byte[] content) {
			m_content = content;
		}
		/**
		 * @param content The content to set.
		 */
		public void setContent(String content) {
			try
			{
				m_content = content.getBytes(m_encoding);
			}
			catch(UnsupportedEncodingException e)
			{
				m_content = content.getBytes();
			}
		}
		/**
		 * @return Returns the canSetQuota.
		 */
		public boolean canSetQuota() {
			return m_canSetQuota;
		}
		/**
		 * @param canSetQuota The canSetQuota to set.
		 */
		public void setCanSetQuota(boolean canSetQuota) {
			m_canSetQuota = canSetQuota;
		}
		/**
		 * @return Returns the hasQuota.
		 */
		public boolean hasQuota() {
			return m_hasQuota;
		}
		/**
		 * @param hasQuota The hasQuota to set.
		 */
		public void setHasQuota(boolean hasQuota) {
			m_hasQuota = hasQuota;
		}
		/**
		 * @return Returns the quota.
		 */
		public String getQuota() {
			return m_quota;
		}
		/**
		 * @param quota The quota to set.
		 */
		public void setQuota(String quota) {
			m_quota = quota;
		}
		/**
		 * @return true if content-type of item indicates it represents a URL, false otherwise
		 */
		public boolean isUrl()
		{
			return TYPE_URL.equals(m_type) || ResourceProperties.TYPE_URL.equals(m_mimetype);
		}
		/**
		 * @return true if content-type of item is "text/text" (plain text), false otherwise
		 */
		public boolean isPlaintext()
		{
			return MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_mimetype) || MIME_TYPE_DOCUMENT_PLAINTEXT.equals(m_type);
		}
		/**
		 * @return true if content-type of item is "text/html" (an html document), false otherwise
		 */
		public boolean isHtml()
		{
			return MIME_TYPE_DOCUMENT_HTML.equals(m_mimetype) || MIME_TYPE_DOCUMENT_HTML.equals(m_type);
		}

		public boolean contentHasChanged()
		{
			return m_contentHasChanged;
		}

		public void setContentHasChanged(boolean changed)
		{
			m_contentHasChanged = changed;
		}

		public boolean contentTypeHasChanged()
		{
			return m_contentTypeHasChanged;
		}

		public void setContentTypeHasChanged(boolean changed)
		{
			m_contentTypeHasChanged = changed;
		}

		public void setNotification(int notification)
		{
			m_notification = notification;
		}

		public int getNotification()
		{
			return m_notification;
		}

		public Object getValue(String name, int index)
		{
			List list = getList(name);
			Object rv = null;
			try
			{
				rv = list.get(index);
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				// return null
			}
			return rv;

		}

		/**
         *
         * @param name
         * @return
         */
        private List getList(String name)
        {
	        // TODO Auto-generated method stub
	        return null;
        }

		/**
		 * Add a property name to the list of properties missing from the input.
		 * @param propname The name of the property.
		 */
		public void setMissing(String propname)
		{
			m_missingInformation.add(propname);
		}

		/**
		 * Query whether a particular property is missing
		 * @param propname The name of the property
		 * @return The value "true" if the property is missing, "false" otherwise.
		 */
		public boolean isMissing(String propname)
		{
			return m_missingInformation.contains(propname) || m_missingInformation.contains(Validator.escapeUrl(propname));
		}

		/**
		 * Empty the list of missing properties.
		 */
		public void clearMissing()
		{
			m_missingInformation.clear();
		}

		public void setAdded(boolean added)
		{
			m_hasBeenAdded = added;
		}

		public boolean hasBeenAdded()
		{
			return m_hasBeenAdded;
		}

		/**
		 * @return the releaseDate
		 */
		public Time getReleaseDate() 
		{
			return m_releaseDate;
		}

		/**
		 * @param releaseDate the releaseDate to set
		 */
		public void setReleaseDate(Time releaseDate) 
		{
			this.m_releaseDate = releaseDate;
		}

		/**
		 * @return the retractDate
		 */
		public Time getRetractDate() 
		{
			return m_retractDate;
		}

		/**
		 * @param retractDate the retractDate to set
		 */
		public void setRetractDate(Time retractDate) 
		{
			this.m_retractDate = retractDate;
		}

		/**
		 * @return the useReleaseDate
		 */
		public boolean useReleaseDate() 
		{
			return m_useReleaseDate;
		}

		/**
		 * @param useReleaseDate the useReleaseDate to set
		 */
		public void setUseReleaseDate(boolean useReleaseDate) 
		{
			this.m_useReleaseDate = useReleaseDate;
		}

		/**
		 * @return the useRetractDate
		 */
		public boolean useRetractDate() 
		{
			return m_useRetractDate;
		}

		/**
		 * @param useRetractDate the useRetractDate to set
		 */
		public void setUseRetractDate(boolean useRetractDate) 
		{
			this.m_useRetractDate = useRetractDate;
		}


	}	// inner class ChefEditItem


	/**
	 * Inner class encapsulates information about folders (and final item?) in a collection path (a.k.a. breadcrumb)
	 * This is being phased out as we switch to the resources type registry.
	 */
	public static class ChefPathItem
	{
		protected String m_url;
		protected String m_name;
		protected String m_id;
		protected boolean m_canRead;
		protected boolean m_isFolder;
		protected boolean m_isLast;
		protected String m_root;
		protected boolean m_isLocal;

		public ChefPathItem(String id, String name)
		{
			m_id = id;
			m_name = name;
			m_canRead = false;
			m_isFolder = false;
			m_isLast = false;
			m_url = "";
			m_isLocal = true;
		}

		/**
		 * @return
		 */
		public boolean canRead()
		{
			return m_canRead;
		}

		/**
		 * @return
		 */
		public String getId()
		{
			return m_id;
		}

		/**
		 * @return
		 */
		public boolean isFolder()
		{
			return m_isFolder;
		}

		/**
		 * @return
		 */
		public boolean isLast()
		{
			return m_isLast;
		}

		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @param canRead
		 */
		public void setCanRead(boolean canRead)
		{
			m_canRead = canRead;
		}

		/**
		 * @param id
		 */
		public void setId(String id)
		{
			m_id = id;
		}

		/**
		 * @param isFolder
		 */
		public void setIsFolder(boolean isFolder)
		{
			m_isFolder = isFolder;
		}

		/**
		 * @param isLast
		 */
		public void setLast(boolean isLast)
		{
			m_isLast = isLast;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/**
		 * @return
		 */
		public String getUrl()
		{
			return m_url;
		}

		/**
		 * @param url
		 */
		public void setUrl(String url)
		{
			m_url = url;
		}

		/**
		 * @param root
		 */
		public void setRoot(String root)
		{
			m_root = root;
		}

		/**
		 * @return
		 */
		public String getRoot()
		{
			return m_root;
		}

		public void setIsLocal(boolean isLocal)
		{
			m_isLocal = isLocal;
		}

		public boolean isLocal()
		{
			return m_isLocal;
		}

	}	// inner class ChefPathItem

	/**
	 *
	 * inner class encapsulates information about groups of metadata tags (such as DC, LOM, etc.)
	 *
	 */
	public static class MetadataGroup
		extends Vector
	{
		/**
		 *
		 */
		private static final long serialVersionUID = -821054142728929236L;
		protected String m_name;
		protected boolean m_isShowing;

		/**
		 * @param name
		 */
		public MetadataGroup(String name)
		{
			super();
			m_name = name;
			m_isShowing = false;
		}

		/**
		 * @return
		 */
		public boolean isShowing()
		{
			return m_isShowing;
		}

		/**
		 * @param isShowing
		 */
		public void setShowing(boolean isShowing)
		{
			m_isShowing = isShowing;
		}


		/**
		 * @return
		 */
		public String getName()
		{
			return m_name;
		}

		/**
		 * @param name
		 */
		public void setName(String name)
		{
			m_name = name;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 * needed to determine List.contains()
		 */
		public boolean equals(Object obj)
		{
			MetadataGroup mg = (MetadataGroup) obj;
			boolean rv = (obj != null) && (m_name.equals(mg));
			return rv;
		}

	}

	public static class AttachItem
	{
		protected String m_id;
		protected String m_displayName;
		protected String m_accessUrl;
		protected String m_collectionId;
		protected String m_contentType;

		/**
		 * @param id
		 * @param displayName
		 * @param collectionId
		 * @param accessUrl
		 */
		public AttachItem(String id, String displayName, String collectionId, String accessUrl)
		{
			m_id = id;
			m_displayName = displayName;
			m_collectionId = collectionId;
			m_accessUrl = accessUrl;

		}

		/**
		 * @return Returns the accessUrl.
		 */
		public String getAccessUrl()
		{
			return m_accessUrl;
		}
		/**
		 * @param accessUrl The accessUrl to set.
		 */
		public void setAccessUrl(String accessUrl)
		{
			m_accessUrl = accessUrl;
		}
		/**
		 * @return Returns the collectionId.
		 */
		public String getCollectionId()
		{
			return m_collectionId;
		}
		/**
		 * @param collectionId The collectionId to set.
		 */
		public void setCollectionId(String collectionId)
		{
			m_collectionId = collectionId;
		}
		/**
		 * @return Returns the id.
		 */
		public String getId()
		{
			return m_id;
		}
		/**
		 * @param id The id to set.
		 */
		public void setId(String id)
		{
			m_id = id;
		}
		/**
		 * @return Returns the name.
		 */
		public String getDisplayName()
		{
			String displayName = m_displayName;
			if(displayName == null || displayName.trim().equals(""))
			{
				displayName = isolateName(m_id);
			}
			return displayName;
		}
		/**
		 * @param name The name to set.
		 */
		public void setDisplayName(String name)
		{
			m_displayName = name;
		}

		/**
		 * @return Returns the contentType.
		 */
		public String getContentType()
		{
			return m_contentType;
		}

		/**
		 * @param contentType
		 */
		public void setContentType(String contentType)
		{
			this.m_contentType = contentType;

		}

	}	// Inner class AttachItem

	public static class ElementCarrier
	{
		protected Element element;
		protected String parent;

		public ElementCarrier(Element element, String parent)
		{
			this.element = element;
			this.parent = parent;

		}

		public Element getElement()
		{
			return element;
		}

		public void setElement(Element element)
		{
			this.element = element;
		}

		public String getParent()
		{
			return parent;
		}

		public void setParent(String parent)
		{
			this.parent = parent;
		}

	}

	/**
	* Develop a list of all the site collections that there are to page.
	* Sort them as appropriate, and apply search criteria.
	*/
	protected static List readAllResources(SessionState state)
	{
		List other_sites = new Vector();

		String collectionId = (String) state.getAttribute (STATE_ATTACH_COLLECTION_ID);
		if(collectionId == null)
		{
			collectionId = (String) state.getAttribute (STATE_COLLECTION_ID);
		}
//		SortedSet expandedCollections = (SortedSet) state.getAttribute(STATE_EXPANDED_COLLECTIONS);
//		if(expandedCollections == null)
//		{
//			expandedCollections = new TreeSet();
//			state.setAttribute(STATE_EXPANDED_COLLECTIONS, expandedCollections);
//		}
		
		// set the sort values
		String sortedBy = (String) state.getAttribute (STATE_SORT_BY);
		String sortedAsc = (String) state.getAttribute (STATE_SORT_ASC);
		
		Boolean showRemove = (Boolean) state.getAttribute(STATE_SHOW_REMOVE_ACTION);
		boolean showRemoveAction = showRemove != null && showRemove.booleanValue();
		
		Boolean showMove = (Boolean) state.getAttribute(STATE_SHOW_MOVE_ACTION);
		boolean showMoveAction = showMove != null && showMove.booleanValue();
		
		Boolean showCopy = (Boolean) state.getAttribute(STATE_SHOW_COPY_ACTION);
		boolean showCopyAction = showCopy != null && showCopy.booleanValue();
		
		Set highlightedItems = (Set) state.getAttribute(STATE_HIGHLIGHTED_ITEMS);
		

		// add user's personal workspace
		User user = UserDirectoryService.getCurrentUser();
		String userId = user.getId();
		String userName = user.getDisplayName();
		String wsId = SiteService.getUserSiteId(userId);
		String wsCollectionId = ContentHostingService.getSiteCollection(wsId);
		if(! collectionId.equals(wsCollectionId))
		{
			List members = getListView(wsCollectionId, highlightedItems, (ChefBrowseItem) null, false, state);

            //List members = getBrowseItems(wsCollectionId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
            if(members != null && members.size() > 0)
		    {
		        ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
				showRemoveAction = showRemoveAction || root.hasDeletableChildren();
				showMoveAction = showMoveAction || root.hasDeletableChildren();
				showCopyAction = showCopyAction || root.hasCopyableChildren();
				
		        root.addMembers(members);
		        root.setName(userName + " " + rb.getString("gen.wsreso"));
		        other_sites.add(root);
		    }
		}
		
        	// add all other sites user has access to
		/*
		 * NOTE: This does not (and should not) get all sites for admin.  
		 *       Getting all sites for admin is too big a request and
		 *       would result in too big a display to render in html.
		 */
		Map othersites = ContentHostingService.getCollectionMap();
		Iterator siteIt = othersites.keySet().iterator();
		SortedSet sort = new TreeSet();
		while(siteIt.hasNext())
		{
              String collId = (String) siteIt.next();
              String displayName = (String) othersites.get(collId);
              sort.add(displayName + DELIM + collId);
		}
		
		Iterator sortIt = sort.iterator();
		while(sortIt.hasNext())
		{
			String item = (String) sortIt.next();
			String displayName = item.substring(0, item.lastIndexOf(DELIM));
			String collId = item.substring(item.lastIndexOf(DELIM) + 1);
			if(! collectionId.equals(collId) && ! wsCollectionId.equals(collId))
			{
				List members = getListView(collId, highlightedItems, (ChefBrowseItem) null, false, state);

				// List members = getBrowseItems(collId, expandedCollections, highlightedItems, sortedBy, sortedAsc, (ChefBrowseItem) null, false, state);
				if(members != null && members.size() > 0)
				{
					ChefBrowseItem root = (ChefBrowseItem) members.remove(0);
					root.addMembers(members);
					root.setName(displayName);
					other_sites.add(root);
				}
              }
          }
		
		return other_sites;
	}
	
	/**
	* Prepare the current page of site collections to display.
	* @return List of ChefBrowseItem objects to display on this page.
	*/
	protected static List prepPage(SessionState state)
	{
		List rv = new Vector();

		// access the page size
		int pageSize = ((Integer) state.getAttribute(STATE_PAGESIZE)).intValue();

		// cleanup prior prep
		state.removeAttribute(STATE_NUM_MESSAGES);

		// are we going next or prev, first or last page?
		boolean goNextPage = state.getAttribute(STATE_GO_NEXT_PAGE) != null;
		boolean goPrevPage = state.getAttribute(STATE_GO_PREV_PAGE) != null;
		boolean goFirstPage = state.getAttribute(STATE_GO_FIRST_PAGE) != null;
		boolean goLastPage = state.getAttribute(STATE_GO_LAST_PAGE) != null;
		state.removeAttribute(STATE_GO_NEXT_PAGE);
		state.removeAttribute(STATE_GO_PREV_PAGE);
		state.removeAttribute(STATE_GO_FIRST_PAGE);
		state.removeAttribute(STATE_GO_LAST_PAGE);

		// are we going next or prev message?
		boolean goNext = state.getAttribute(STATE_GO_NEXT) != null;
		boolean goPrev = state.getAttribute(STATE_GO_PREV) != null;
		state.removeAttribute(STATE_GO_NEXT);
		state.removeAttribute(STATE_GO_PREV);

		// read all channel messages
		List allMessages = readAllResources(state);

		if (allMessages == null)
		{
			return rv;
		}
		
		String messageIdAtTheTopOfThePage = null;
		Object topMsgId = state.getAttribute(STATE_TOP_PAGE_MESSAGE_ID);
		if(topMsgId == null)
		{
			// do nothing
		}
		else if(topMsgId instanceof Integer)
		{
			messageIdAtTheTopOfThePage = ((Integer) topMsgId).toString();
		}
		else if(topMsgId instanceof String)
		{
			messageIdAtTheTopOfThePage = (String) topMsgId;
		}

		// if we have no prev page and do have a top message, then we will stay "pinned" to the top
		boolean pinToTop = (	(messageIdAtTheTopOfThePage != null)
							&&	(state.getAttribute(STATE_PREV_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// if we have no next page and do have a top message, then we will stay "pinned" to the bottom
		boolean pinToBottom = (	(messageIdAtTheTopOfThePage != null)
							&&	(state.getAttribute(STATE_NEXT_PAGE_EXISTS) == null)
							&&	!goNextPage && !goPrevPage && !goNext && !goPrev && !goFirstPage && !goLastPage);

		// how many messages, total
		int numMessages = allMessages.size();

		if (numMessages == 0)
		{
			return rv;
		}

		// save the number of messges
		state.setAttribute(STATE_NUM_MESSAGES, new Integer(numMessages));

		// find the position of the message that is the top first on the page
		int posStart = 0;
		if (messageIdAtTheTopOfThePage != null)
		{
			// find the next page
			posStart = findResourceInList(allMessages, messageIdAtTheTopOfThePage);

			// if missing, start at the top
			if (posStart == -1)
			{
				posStart = 0;
			}
		}
		
		// if going to the next page, adjust
		if (goNextPage)
		{
			posStart += pageSize;
		}

		// if going to the prev page, adjust
		else if (goPrevPage)
		{
			posStart -= pageSize;
			if (posStart < 0) posStart = 0;
		}
		
		// if going to the first page, adjust
		else if (goFirstPage)
		{
			posStart = 0;
		}
		
		// if going to the last page, adjust
		else if (goLastPage)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// pinning
		if (pinToTop)
		{
			posStart = 0;
		}
		else if (pinToBottom)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// get the last page fully displayed
		if (posStart + pageSize > numMessages)
		{
			posStart = numMessages - pageSize;
			if (posStart < 0) posStart = 0;
		}

		// compute the end to a page size, adjusted for the number of messages available
		int posEnd = posStart + (pageSize-1);
		if (posEnd >= numMessages) posEnd = numMessages-1;
		int numMessagesOnThisPage = (posEnd - posStart) + 1;

		// select the messages on this page
		for (int i = posStart; i <= posEnd; i++)
		{
			rv.add(allMessages.get(i));
		}

		// save which message is at the top of the page
		ChefBrowseItem itemAtTheTopOfThePage = (ChefBrowseItem) allMessages.get(posStart);
		state.setAttribute(STATE_TOP_PAGE_MESSAGE_ID, itemAtTheTopOfThePage.getId());
		state.setAttribute(STATE_TOP_MESSAGE_INDEX, new Integer(posStart));


		// which message starts the next page (if any)
		int next = posStart + pageSize;
		if (next < numMessages)
		{
			state.setAttribute(STATE_NEXT_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_NEXT_PAGE_EXISTS);
		}

		// which message ends the prior page (if any)
		int prev = posStart - 1;
		if (prev >= 0)
		{
			state.setAttribute(STATE_PREV_PAGE_EXISTS, "");
		}
		else
		{
			state.removeAttribute(STATE_PREV_PAGE_EXISTS);
		}

		if (state.getAttribute(STATE_VIEW_ID) != null)
		{
			int viewPos = findResourceInList(allMessages, (String) state.getAttribute(STATE_VIEW_ID));
	
			// are we moving to the next message
			if (goNext)
			{
				// advance
				viewPos++;
				if (viewPos >= numMessages) viewPos = numMessages-1;
			}
	
			// are we moving to the prev message
			if (goPrev)
			{
				// retreat
				viewPos--;
				if (viewPos < 0) viewPos = 0;
			}
			
			// update the view message
			state.setAttribute(STATE_VIEW_ID, ((ChefBrowseItem) allMessages.get(viewPos)).getId());
			
			// if the view message is no longer on the current page, adjust the page
			// Note: next time through this will get processed
			if (viewPos < posStart)
			{
				state.setAttribute(STATE_GO_PREV_PAGE, "");
			}
			else if (viewPos > posEnd)
			{
				state.setAttribute(STATE_GO_NEXT_PAGE, "");
			}
			
			if (viewPos > 0)
			{
				state.setAttribute(STATE_PREV_EXISTS,"");
			}
			else
			{
				state.removeAttribute(STATE_PREV_EXISTS);
			}
			
			if (viewPos < numMessages-1)
			{
				state.setAttribute(STATE_NEXT_EXISTS,"");
			}
			else
			{
				state.removeAttribute(STATE_NEXT_EXISTS);
			}			
		}

		return rv;

	}	// prepPage
	
	/**
	* Find the resource with this id in the list.
	* @param messages The list of messages.
	* @param id The message id.
	* @return The index position in the list of the message with this id, or -1 if not found.
	*/
	protected static int findResourceInList(List resources, String id)
	{
		for (int i = 0; i < resources.size(); i++)
		{
			// if this is the one, return this index
			if (((ChefBrowseItem) (resources.get(i))).getId().equals(id)) return i;
		}

		// not found
		return -1;

	}	// findResourceInList

	protected static User getUserProperty(ResourceProperties props, String name)
	{
		String id = props.getProperty(name);
		if (id != null)
		{
			try
			{
				return UserDirectoryService.getUser(id);
			}
			catch (UserNotDefinedException e)
			{
			}
		}
		
		return null;
	}

	/**
	 * Find the resource name of a given resource id or filepath.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the resource name.
	 */
	protected static String isolateName(String id)
	{
		if (id == null) return null;
		if (id.length() == 0) return null;

		// take after the last resource path separator, not counting one at the very end if there
		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1, (lastIsSeparator ? id.length() - 1 : id.length()));

	} // isolateName


}	// ResourcesAction
