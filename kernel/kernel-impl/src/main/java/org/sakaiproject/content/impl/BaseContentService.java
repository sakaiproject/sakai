/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.content.api.providers.SiteContentAdvisorProvider;
import org.sakaiproject.content.api.providers.SiteContentAdvisorTypeRegistry;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess;
import org.sakaiproject.content.types.FileUploadType;
import org.sakaiproject.content.types.FolderType;
import org.sakaiproject.content.types.HtmlDocumentType;
import org.sakaiproject.content.types.TextDocumentType;
import org.sakaiproject.content.types.UrlResourceType;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.EntityReader;
import org.sakaiproject.entity.api.serialize.EntityReaderHandler;
import org.sakaiproject.entity.api.serialize.EntitySerializer;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.CopyrightException;
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
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadBound;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.Blob;
import org.sakaiproject.util.DefaultEntityHandler;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SAXEntityReader;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <p>
 * BaseContentService is an abstract base implementation of the Sakai ContentHostingService.
 * </p>
 */
public abstract class BaseContentService implements ContentHostingService, CacheRefresher, ContextObserver, EntityTransferrer, 
SiteContentAdvisorProvider, SiteContentAdvisorTypeRegistry
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseContentService.class);

	protected static final long END_OF_TIME = 8000L * 365L * 24L * 60L * 60L * 1000L;
	protected static final long START_OF_TIME = 365L * 24L * 60L * 60L * 1000L;

	protected static final Pattern contextPattern = Pattern.compile("\\A/(group/|user/|~)(.+?)/");

	private static final String PROP_AVAIL_NOTI = "availableNotified";

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** A Storage object for persistent storage. */
	protected Storage m_storage = null;

	/** A Cache for this service - ContentResource and ContentCollection keyed by reference. */
	protected Cache m_cache = null;

	/**
	 * The quota for content resource body bytes (in Kbytes) for any hierarchy in the /user/ or /group/ areas, or 0 if quotas are not enforced.
	 */
	protected long m_siteQuota = 0;

	private boolean m_useSmartSort = true;

	static
	{
		ROOT_COLLECTIONS.add(COLLECTION_SITE);
		ROOT_COLLECTIONS.add(COLLECTION_USER);
		ROOT_COLLECTIONS.add(COLLECTION_DROPBOX);
		ROOT_COLLECTIONS.add(COLLECTION_PUBLIC);
		ROOT_COLLECTIONS.add(COLLECTION_PRIVATE);
		ROOT_COLLECTIONS.add(ATTACHMENTS_COLLECTION);
		ROOT_COLLECTIONS.add(COLLECTION_MELETE_DOCS);
	}

	/** Optional path to external file system file store for body binary. */
	protected String m_bodyPath = null;

	/** Optional set of folders just within the m_bodyPath to distribute files among. */
	protected String[] m_bodyVolumes = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: MemoryService. */
	protected MemoryService m_memoryService = null;

	/**
	 * Dependency: MemoryService.
	 * 
	 * @param service
	 *        The MemoryService.
	 */
	public void setMemoryService(MemoryService service)
	{
		m_memoryService = service;
	}

	/** Dependency: AliasService. */
	protected AliasService m_aliasService = null;

	/**
	 * Dependency: AliasService.
	 * 
	 * @param service
	 *        The AliasService.
	 */
	public void setAliasService(AliasService service)
	{
		m_aliasService = service;
	}

	/** Dependency: SiteService. */
	protected SiteService m_siteService = null;

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		m_siteService = service;
	}

	/** Dependency: NotificationService. */
	protected NotificationService m_notificationService = null;

	/**
	 * Dependency: NotificationService.
	 * 
	 * @param service
	 *        The NotificationService.
	 */
	public void setNotificationService(NotificationService service)
	{
		m_notificationService = service;
	}

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService m_serverConfigurationService = null;

	/**
	 * Dependency: ServerConfigurationService.
	 * 
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		m_serverConfigurationService = service;
	}

	/** Dependency: EntityManager. */
	protected EntityManager m_entityManager = null;

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		m_entityManager = service;
	}

	/** Dependency: AuthzGroupService. */
	protected AuthzGroupService m_authzGroupService = null;

	/**
	 * Dependency: AuthzGroupService.
	 * 
	 * @param service
	 *        The AuthzGroupService.
	 */
	public void setAuthzGroupService(AuthzGroupService service)
	{
		m_authzGroupService = service;
	}

	/** Dependency: SecurityService. */
	protected SecurityService m_securityService = null;

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		m_securityService = service;
	}

	/**
	 * Set the site quota.
	 * 
	 * @param quota
	 *        The site quota (as a string).
	 */
	public void setSiteQuota(String quota)
	{
		try
		{
			m_siteQuota = Long.parseLong(quota);
		}
		catch (Throwable t)
		{
		}
	}

	/** Configuration: cache, or not. */
	protected boolean m_caching = false;

	/**
	 * Configuration: cache, or not. 
	 * 
	 * @param value
	 *        True/false
	 */
	public void setCaching(String value)
	{
		try
		{
			m_caching = new Boolean(value).booleanValue();
		}
		catch (Throwable t)
		{
		}
	}

	/** Configuration: Do we protect attachments in sites with the site AuthZGroup. */
	protected boolean m_siteAttachments = true; // Default to true for Sakai 2.5 and later

	/**
	 * Configuration: Do we protect attachments in sites with the site AuthZGroup. 
	 * 
	 * @param value
	 *        true - We protect the site scoped attachments with the site's AZG
	 *        false - We use the /content/attachment hierarchy to protect attachments
	 *
	 *        Default is true.
	 */
	public void setSiteAttachments(String value)
	{
		try
		{
			m_siteAttachments = new Boolean(value).booleanValue();
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * Configuration: set the external file system path for body storage If set, the resource binary database table will not be used.
	 * 
	 * @param value
	 *        The complete path to the root of the external file system storage area for resource body bytes.
	 */
	public void setBodyPath(String value)
	{
		m_bodyPath = value;
	}

	/**
	 * Configuration: set the external file system volume folders (folder just within the bodyPath) as a comma separated list of folder names. 
	 * If set, files will be distributed over these folders.  A single semicolon (';') can be added to the end of the list of values to indicate 
	 * that leading and trailing whitespace should be preserved from each volume name.  Without the semicolon, leading and trailing whitespace 
	 * will be trimmed from each name in the list.
	 * 
	 * @param value
	 *        The comma separated list of folder names within body path to distribute files among.
	 */
	public void setBodyVolumes(String value)
	{
		boolean trimWhitespace = true;
		try
		{
			List<String> list = new ArrayList<String>();
			if(value != null && value.trim().endsWith(";"))
			{
				trimWhitespace = false;
				value = value.substring(0, value.lastIndexOf(";"));
			}

			if(value != null && ! value.trim().equals(""))
			{
				String[] bodyVolumes = StringUtil.split(value, ",");
				for(int i = 0; i < bodyVolumes.length; i++)
				{
					String name = bodyVolumes[i];
					if(name == null || name.trim().equals(""))
					{
						continue;
					}
					list.add(trimWhitespace ? name.trim() : name);
				}
			}
			this.m_bodyVolumes = new String[list.size()];
			for(int i = 0; i < list.size(); i++)
			{
				this.m_bodyVolumes[i] = list.get(i);
			}
		}
		catch (Throwable t)
		{
		}
	}

	/** Configuration: short refs */
	protected boolean m_shortRefs = true;

	/**
	 * Configuration: set the short refs
	 * 
	 * @param value
	 *        The short refs value.
	 */
	public void setShortRefs(String value)
	{
		try
		{
			m_shortRefs = new Boolean(value).booleanValue();
		}
		catch (Throwable t)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isShortRefs()
	{
		return m_shortRefs;
	}

	/** Configuration: allow use of alias for site id in references. */
	protected boolean m_siteAlias = true;

	/**
	 * Configuration: set the alias for site
	 * 
	 * @param value
	 *        The alias for site value.
	 */
	public void setSiteAlias(String value)
	{
		try
		{
			m_siteAlias = new Boolean(value).booleanValue();
		}
		catch (Throwable t)
		{
		}
	}

	/** Dependency: allowGroupResources setting */
	protected boolean m_allowGroupResources = true;

	/**
	 * Dependency: allowGroupResources
	 * 
	 * @param allowGroupResources
	 *        the setting
	 */
	public void setAllowGroupResources(boolean allowGroupResources)
	{
		m_allowGroupResources = allowGroupResources;
	}
	/**
	 * Get
	 * 
	 * @return allowGroupResources
	 */
	public boolean getAllowGroupResources()
	{
		return m_allowGroupResources;
	}

	/** flag indicating whether entities can be hidden (scheduled or otherwise) */
	protected boolean m_availabilityChecksEnabled = true;

	/**
	 * Configuration: set a flag indicating whether entities can be hidden (scheduled or otherwise)
	 * 
	 * @param value
	 *        The value indicating whether entities can be hidden.
	 */
	public void setAvailabilityChecksEnabled(boolean value)
	{
		m_availabilityChecksEnabled = value;
	}

	/**
	 * Access flag indicating whether entities can be hidden (scheduled or otherwise).
	 * @return true if the availability features are enabled, false otherwise.
	 */
	public boolean isAvailabilityEnabled()
	{
		return m_availabilityChecksEnabled;
	}

	/** flag indicating whether custom sort order based on "priority" is enabled */
	protected boolean m_prioritySortEnabled = true;

	/**
	 * Configuration: set a flag indicating whether custom sort order based on "priority" is enabled
	 * 
	 * @param value
	 *        The value indicating whether custom sort order is enabled.
	 */
	public void setPrioritySortEnabled(boolean value)
	{
		m_prioritySortEnabled = value;
	}

	/**
	 * Configuration: set a flag indicating whether custom sort order based on "priority" is enabled
	 * 
	 * @param value
	 *        The value indicating whether custom sort order is enabled.
	 */
	public boolean getPrioritySortEnabled()
	{
		return m_prioritySortEnabled;
	}

	/**
	 * Access flag indicating whether sorting by "priority" is enabled.
	 * @return true if the custom sort by priority is enabled, false otherwise.
	 */ 
	public boolean isSortByPriorityEnabled()
	{
		return m_prioritySortEnabled;
	}

	/**
	 * Dependency: the ResourceTypeRegistry
	 */
	protected ResourceTypeRegistry m_resourceTypeRegistry;

	/**
	 * Dependency: inject the ResourceTypeRegistry
	 * @param registry
	 */
	public void setResourceTypeRegistry(ResourceTypeRegistry registry)
	{
		m_resourceTypeRegistry = registry;
	}

	/**
	 * @return the ResourceTypeRegistry
	 */
	public ResourceTypeRegistry getResourceTypeRegistry()
	{
		return m_resourceTypeRegistry;
	}

	protected boolean useResourceTypeRegistry = true;


	public EntitySerializer collectionSerializer;

	public EntitySerializer resourceSerializer;

	public void setUseResourceTypeRegistry(boolean useRegistry)
	{
		useResourceTypeRegistry = useRegistry;
	}

	public boolean usingResourceTypeRegistry()
	{
		return useResourceTypeRegistry;
	}

	protected boolean filesizeColumnExists = false;
	protected boolean filesizeColumnReady = false;

	public boolean readyToUseFilesizeColumn()
	{
		return filesizeColumnExists && filesizeColumnReady;
	}

	public boolean m_useContextQueryForCollectionSize = false;

	/**
	 * @return the useContextQueryForCollectionSize
	 */
	public boolean isUseContextQueryForCollectionSize() 
	{
		return m_useContextQueryForCollectionSize;
	}

	/**
	 * @param useContextQueryForCollectionSize the useContextQueryForCollectionSize to set
	 */
	public void setUseContextQueryForCollectionSize(boolean useContextQueryForCollectionSize) 
	{
		this.m_useContextQueryForCollectionSize = useContextQueryForCollectionSize;
	}

	protected boolean convertToContextQueryForCollectionSize;

	/**
	 * @param convertToContextQueryForCollectionSize the convertToContextQueryForCollectionSize to set
	 */
	public void setConvertToContextQueryForCollectionSize(boolean convertToContextQueryForCollectionSize) 
	{
		this.convertToContextQueryForCollectionSize = convertToContextQueryForCollectionSize;
	}




	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// Get resource bundle
			String resourceClass = m_serverConfigurationService.getString(RESOURCECLASS, DEFAULT_RESOURCECLASS);
			String resourceBundle = m_serverConfigurationService.getString(RESOURCEBUNDLE, DEFAULT_RESOURCEBUNDLE);
			rb = new Resource().getLoader(resourceClass, resourceBundle);

			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct a storage helper and read
			m_storage = newStorage();
			m_storage.open();

			M_log.info("Loaded Storage as "+m_storage+" for "+this);

			// make the cache
			if (m_caching)
			{
				m_cache = m_memoryService
				.newCache(
						"org.sakaiproject.content.api.ContentHostingService.cache",
						this, getAccessPoint(true));
			}

			// register a transient notification for resources
			NotificationEdit edit = m_notificationService.addTransientNotification();

			// set functions
			edit.setFunction(EVENT_RESOURCE_AVAILABLE);
			edit.addFunction(EVENT_RESOURCE_WRITE);

			// set the filter to any site related resource
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + "group" + Entity.SEPARATOR);
			// %%% is this the best we can do? -ggolden

			// set the action
			edit.setAction(new SiteEmailNotificationContent());

			NotificationEdit dbNoti = m_notificationService.addTransientNotification();

			// set functions
			dbNoti.setFunction(EVENT_RESOURCE_ADD);
			dbNoti.addFunction(EVENT_RESOURCE_WRITE);

			// set the filter to any site related resource
			dbNoti.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + "group-user" + Entity.SEPARATOR);
			// %%% is this the best we can do? -ggolden

			// set the action
			dbNoti.setAction(new DropboxNotification());


			StringBuilder buf = new StringBuilder();
			if (m_bodyVolumes != null)
			{
				for (int i = 0; i < m_bodyVolumes.length; i++)
				{
					buf.append(m_bodyVolumes[i]);
					buf.append(", ");
				}
			}

			// The entity producer is registerd by the thrird party manager

			m_entityManager.registerEntityProducer(this,
					ContentHostingService.REFERENCE_ROOT);

			// register functions
			FunctionManager.registerFunction(AUTH_RESOURCE_ADD);
			FunctionManager.registerFunction(AUTH_RESOURCE_READ);
			FunctionManager.registerFunction(AUTH_RESOURCE_WRITE_ANY);
			FunctionManager.registerFunction(AUTH_RESOURCE_WRITE_OWN);
			FunctionManager.registerFunction(AUTH_RESOURCE_REMOVE_ANY);
			FunctionManager.registerFunction(AUTH_RESOURCE_REMOVE_OWN);
			FunctionManager.registerFunction(AUTH_RESOURCE_ALL_GROUPS);
			FunctionManager.registerFunction(AUTH_RESOURCE_HIDDEN);

			FunctionManager.registerFunction(AUTH_DROPBOX_OWN);
			FunctionManager.registerFunction(AUTH_DROPBOX_MAINTAIN);


			M_log.info("init(): site quota: " + m_siteQuota + " body path: " + m_bodyPath + " volumes: "
					+ buf.toString());
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

		if(usingResourceTypeRegistry())
		{
			this.getResourceTypeRegistry().register(new FileUploadType());
			this.getResourceTypeRegistry().register(new FolderType());
			this.getResourceTypeRegistry().register(new TextDocumentType());
			this.getResourceTypeRegistry().register(new HtmlDocumentType());
			this.getResourceTypeRegistry().register(new UrlResourceType());
		}

		this.m_useSmartSort = m_serverConfigurationService.getBoolean("content.smartSort", true);

	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		if ( m_storage != null ) {
			m_storage.close();
		}
		m_storage = null;

		if ((m_caching) && (m_cache != null))
		{
			m_cache.destroy();
			m_cache = null;
		}

		M_log.info("destroy()");

	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation - for collections
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Storage user for collections - in the resource side, not container
	 */
	protected class CollectionStorageUser implements StorageUser, SAXEntityReader, EntityReaderHandler, EntityReader
	{
		private Map<String,Object> m_services;

		private EntityReaderHandler entityReaderAdapter;

		public Entity newContainer(String ref)
		{
			return null;
		}

		public Entity newContainer(Element element)
		{
			return null;
		}

		public Entity newContainer(Entity other)
		{
			return null;
		}

		public Entity newResource(Entity container, String id, Object[] others)
		{
			return new BaseCollectionEdit(id);
		}

		public Entity newResource(Entity container, Element element)
		{
			return new BaseCollectionEdit(element);
		}

		public Entity newResource(Entity container, Entity other)
		{
			return new BaseCollectionEdit((ContentCollection) other);
		}

		public Edit newContainerEdit(String ref)
		{
			return null;
		}

		public Edit newContainerEdit(Element element)
		{
			return null;
		}

		public Edit newContainerEdit(Entity other)
		{
			return null;
		}

		public Edit newResourceEdit(Entity container, String id, Object[] others)
		{
			BaseCollectionEdit rv = new BaseCollectionEdit(id);
			rv.activate();
			return rv;
		}

		public Edit newResourceEdit(Entity container, Element element)
		{
			BaseCollectionEdit rv = new BaseCollectionEdit(element);
			rv.activate();
			return rv;
		}

		public Edit newResourceEdit(Entity container, Entity other)
		{
			BaseCollectionEdit rv = new BaseCollectionEdit((ContentCollection) other);
			rv.activate();
			return rv;
		}

		/**
		 * Collect the fields that need to be stored outside the XML (for the resource).
		 * 
		 * @return An array of field values to store in the record outside the XML (for the resource).
		 */
		public Object[] storageFields(Entity r)
		{
			Object[] rv = new Object[1];
			rv[0] = StringUtil.referencePath(((ContentCollection) r).getId());
			return rv;
		}

		/**
		 * Check if this resource is in draft mode.
		 * 
		 * @param r
		 *        The resource.
		 * @return true if the resource is in draft mode, false if not.
		 */
		public boolean isDraft(Entity r)
		{
			return false;
		}

		/**
		 * Access the resource owner user id.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource owner user id.
		 */
		public String getOwnerId(Entity r)
		{
			return null;
		}

		/**
		 * Access the resource date.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource date.
		 */
		public Time getDate(Entity r)
		{
			return null;
		}

		/***********************************************************************
		 * SAXEntityReader
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getDefaultHandler(java.util.Map)
		 */
		public DefaultEntityHandler getDefaultHandler(final Map<String, Object> services)
		{
			return new DefaultEntityHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
						{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if (entity == null)
						{
							if ("collection".equals(qName))
							{
								BaseCollectionEdit bre = new BaseCollectionEdit();
								entity = bre;
								setContentHandler(bre.getContentHandler(services), uri,
										localName, qName, attributes);
							}
							else
							{
								M_log.warn("Unexpected Element in XML [" + qName + "]");
							}

						}
					}
						}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getServices()
		 */
		public Map<String, Object> getServices()
		{
			if (m_services == null)
			{
				m_services = new HashMap<String, Object>();
			}
			return m_services;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#accept(java.lang.String)
		 */
		public boolean accept(byte[] blob)
		{
			return  collectionSerializer.accept(blob);
		}





		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#parseContainer(java.lang.String)
		 */
		public Entity parse(String xml, byte[] blob) throws EntityParseException
		{
			BaseCollectionEdit bce = new BaseCollectionEdit();
			collectionSerializer.parse(bce,blob);
			return bce;
		}
		/* (non-Javadoc)
		 * @see org.sakaiproject.entity.api.serialize.EntityReaderHandler#parse(org.sakaiproject.entity.api.Entity, java.lang.String, byte[])
		 */
		public Entity parse(Entity container, String xml, byte[] blob) throws EntityParseException
		{
			BaseCollectionEdit bce = new BaseCollectionEdit();
			collectionSerializer.parse(bce,blob);
			return bce;
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#toString(org.sakaiproject.entity.api.Entity)
		 */
		public byte[] serialize(Entity entry) throws EntityParseException
		{
			if ( entry instanceof SerializableEntity ) {

				return collectionSerializer.serialize((SerializableEntity)entry);
			}
			throw new EntityParseException("Unable to serialze entity to native format, entity is not serializable");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.entity.api.EntityReader#getHandler()
		 */
		public EntityReaderHandler getHandler()
		{
			return entityReaderAdapter;
		}

		/**
		 * @return the entityReaderAdapter
		 */
		public EntityReaderHandler getEntityReaderAdapter()
		{
			return entityReaderAdapter;
		}

		/**
		 * @param entityReaderAdapter the entityReaderAdapter to set
		 */
		public void setEntityReaderAdapter(EntityReaderHandler entityReaderAdapter)
		{
			this.entityReaderAdapter = entityReaderAdapter;
		}




	} // class CollectionStorageUser

	/**
	 * Storage user for resources - in the resource side, not container
	 */
	protected class ResourceStorageUser implements StorageUser, SAXEntityReader, EntityReaderHandler, EntityReader
	{
		private Map<String, Object> m_services;

		private EntityReaderHandler entityReaderAdapter;

		public Entity newContainer(String ref)
		{
			return null;
		}

		public Entity newContainer(Element element)
		{
			return null;
		}

		public Entity newContainer(Entity other)
		{
			return null;
		}

		public Entity newResource(Entity container, String id, Object[] others)
		{
			return new BaseResourceEdit(id);
		}

		public Entity newResource(Entity container, Element element)
		{
			return new BaseResourceEdit(element);
		}

		public Entity newResource(Entity container, Entity other)
		{
			return new BaseResourceEdit((ContentResource) other);
		}

		public Edit newContainerEdit(String ref)
		{
			return null;
		}

		public Edit newContainerEdit(Element element)
		{
			return null;
		}

		public Edit newContainerEdit(Entity other)
		{
			return null;
		}

		public Edit newResourceEdit(Entity container, String id, Object[] others)
		{
			BaseResourceEdit rv = new BaseResourceEdit(id);
			rv.activate();
			return rv;
		}

		public Edit newResourceEdit(Entity container, Element element)
		{
			BaseResourceEdit rv = new BaseResourceEdit(element);
			rv.activate();
			return rv;
		}

		public Edit newResourceEdit(Entity container, Entity other)
		{
			BaseResourceEdit rv = new BaseResourceEdit((ContentResource) other);
			rv.activate();
			return rv;
		}

		/**
		 * Collect the fields that need to be stored outside the XML (for the resource).
		 * 
		 * @return An array of field values to store in the record outside the XML (for the resource).
		 */
		public Object[] storageFields(Entity r)
		{
			if(filesizeColumnExists)
			{
				// include the file path field if we are doing body in the file system
				if (m_bodyPath != null)
				{
					Object[] rv = new Object[5];
					rv[0] = StringUtil.referencePath(((ContentResource) r).getId());
					rv[1] = ((BasicGroupAwareEdit) r).getContext();
					rv[2] = new Integer(((ContentResource) r).getContentLength());
					rv[3] = ((BasicGroupAwareEdit) r).getResourceType();
					rv[4] = StringUtil.trimToZero(((BaseResourceEdit) r).m_filePath);
					return rv;
				}

				// otherwise don't include the file path field
				else
				{
					Object[] rv = new Object[4];
					rv[0] = StringUtil.referencePath(((ContentResource) r).getId());
					rv[1] = ((BasicGroupAwareEdit) r).getContext();
					rv[2] = new Integer(((ContentResource) r).getContentLength());
					rv[3] = ((BasicGroupAwareEdit) r).getResourceType();
					return rv;
				}
			}
			else
			{
				// include the file path field if we are doing body in the file system
				if (m_bodyPath != null)
				{
					Object[] rv = new Object[2];
					rv[0] = StringUtil.referencePath(((ContentResource) r).getId());
					rv[1] = StringUtil.trimToZero(((BaseResourceEdit) r).m_filePath);
					return rv;
				}

				// otherwise don't include the file path field
				else
				{
					Object[] rv = new Object[1];
					rv[0] = StringUtil.referencePath(((ContentResource) r).getId());
					return rv;
				}

			}
		}

		/**
		 * Check if this resource is in draft mode.
		 * 
		 * @param r
		 *        The resource.
		 * @return true if the resource is in draft mode, false if not.
		 */
		public boolean isDraft(Entity r)
		{
			return false;
		}

		/**
		 * Access the resource owner user id.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource owner user id.
		 */
		public String getOwnerId(Entity r)
		{
			return null;
		}

		/**
		 * Access the resource date.
		 * 
		 * @param r
		 *        The resource.
		 * @return The resource date.
		 */
		public Time getDate(Entity r)
		{
			return null;
		}

		/***********************************************************************
		 * SAXEntityReader
		 */
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getDefaultHandler(java.util.Map)
		 */
		public DefaultEntityHandler getDefaultHandler(final Map<String, Object> services)
		{
			return new DefaultEntityHandler()
			{

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
						{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if (entity == null)
						{
							if ("resource".equals(qName))
							{
								BaseResourceEdit bre = new BaseResourceEdit();
								entity = bre;
								setContentHandler(bre.getContentHandler(services), uri,
										localName, qName, attributes);
							}
							else
							{
								M_log.warn("Unexpected Element in XML [" + qName + "]");
							}

						}
					}
						}

			};
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.util.SAXEntityReader#getServices()
		 */
		public Map<String, Object> getServices()
		{
			if (m_services == null)
			{
				m_services = new HashMap<String, Object>();
			}
			return m_services;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#accept(java.lang.String)
		 */
		public boolean accept(byte[] blob)
		{
			return resourceSerializer.accept(blob);
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#parseContainer(java.lang.String)
		 */
		public Entity parse(String xml, byte[] blob) throws EntityParseException
		{
			BaseResourceEdit bre = new BaseResourceEdit();
			resourceSerializer.parse(bre,blob);
			return bre;
		}
		/* (non-Javadoc)
		 * @see org.sakaiproject.entity.api.serialize.EntityReaderHandler#parse(org.sakaiproject.entity.api.Entity, java.lang.String, byte[])
		 */
		public Entity parse(Entity container, String xml, byte[] blob) throws EntityParseException
		{
			BaseResourceEdit bre = new BaseResourceEdit();
			resourceSerializer.parse(bre,blob);
			return bre;
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.util.EntityReader#toString(org.sakaiproject.entity.api.Entity)
		 */
		public byte[] serialize(Entity entry) throws EntityParseException
		{
			if ( entry instanceof SerializableEntity ) {
				return resourceSerializer.serialize((SerializableEntity) entry);
			}
			throw new EntityParseException("Unable to parse entity, entity does not implement SerializableEntity ");
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.entity.api.EntityReader#getHandler()
		 */
		public EntityReaderHandler getHandler()
		{
			return entityReaderAdapter;
		}

		/**
		 * @return the entityReaderAdapter
		 */
		public EntityReaderHandler getEntityReaderAdapter()
		{
			return entityReaderAdapter;
		}

		/**
		 * @param entityReaderAdapter the entityReaderAdapter to set
		 */
		public void setEntityReaderAdapter(EntityReaderHandler entityReaderAdapter)
		{
			this.entityReaderAdapter = entityReaderAdapter;
		}





	} // class ResourceStorageUser

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ContentHostingService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected abstract Storage newStorage();

	/**
	 * Determine whether the entityId parameter identifies a collection (as opposed to a resource).  
	 * This method does not necessarily verify that a ContentEntity with this id exists.  
	 * It merely determines whether the id could identify a collection.
	 * @param entityId
	 * @return true if the entityId could identify a collection, false otherwise.
	 */
	public boolean isCollection(String entityId)
	{
		return (entityId != null && entityId.endsWith(Entity.SEPARATOR));
	}

	/**
	 * @param id
	 *        id of the resource to set the UUID for
	 * @param uuid
	 *        the new UUID of the resource
	 */
	protected abstract void setUuidInternal(String id, String uuid);

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : m_serverConfigurationService.getAccessUrl()) + m_relativeAccessPoint;

	} // getAccessPoint

	/**
	 * If the id is for a resource in a dropbox, change the function to a dropbox check, which is to check for write.<br />
	 * You have full or no access to a dropbox.
	 * 
	 * @param lock
	 *        The lock we are checking.
	 * @param id
	 *        The resource id.
	 * @return The lock to check.
	 */
	protected String convertLockIfDropbox(String lock, String id)
	{
		// if this resource is a dropbox, you need dropbox maintain permission
		if (id.startsWith(COLLECTION_DROPBOX))
		{
			// only for /group-user/SITEID/USERID/ refs.
			String[] parts = StringUtil.split(id, "/");
			if (parts.length >= 3)
			{
				return AUTH_DROPBOX_MAINTAIN;
			}
		}

		return lock;
	}

	/**
	 * Check whether an id would identify an entity in a dropbox.  Does not determine existence of the entity, just whether its id indicates it is a dropbox or contained within a dropbox.
	 * @return true if the entity is a dropbox or in a dropbox, false otherwise. 
	 */
	public boolean isInDropbox(String entityId)
	{
		return entityId.startsWith("/group-user");
	}

	public boolean isSiteLevelDropbox(String id) 
	{
		boolean isSiteLevelDropbox = (id != null) && isInDropbox(id);
		if(isSiteLevelDropbox)
		{
			String[] parts = id.split(Entity.SEPARATOR);
			isSiteLevelDropbox = parts.length == 3;
		}
		return isSiteLevelDropbox;
	}

	public boolean isIndividualDropbox(String id) 
	{
		boolean isIndividualDropbox = (id != null) && isInDropbox(id);
		if(isIndividualDropbox)
		{
			String[] parts = id.split(Entity.SEPARATOR);
			isIndividualDropbox = parts.length == 4;
		}
		return isIndividualDropbox;
	}

	public boolean isInsideIndividualDropbox(String id) 
	{
		boolean isIndividualDropbox = (id != null) && isInDropbox(id);
		if(isIndividualDropbox)
		{
			String[] parts = id.split(Entity.SEPARATOR);
			isIndividualDropbox = parts.length > 4;
		}
		return isIndividualDropbox;
	}

	public String getSiteLevelDropboxId(String id)
	{
		String dropboxId = null;
		if(isSiteLevelDropbox(id))
		{
			String[] parts = id.split(Entity.SEPARATOR);
			dropboxId = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR;
		}
		return dropboxId;
	}

	/**
	 * Access the name of the individual dropbox that contains a particular entity, or null if the entity is not inside an individual dropbox.
	 * @param entityId The id for an entity
	 * @return
	 */
	public String getIndividualDropboxId(String entityId)
	{
		String dropboxId = null;
		if(entityId != null && isInDropbox(entityId))
		{
			String[] parts = entityId.split(Entity.SEPARATOR);
			if(parts.length >= 4)
			{
				dropboxId = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2]  + Entity.SEPARATOR + parts[3] + Entity.SEPARATOR;
			}
		}
		return dropboxId;
	}

	/**
	 * Check whether the resource is hidden.
	 * @param id
	 * @return
	 * @throws IdUnusedException
	 */
	protected boolean availabilityCheck(String id) throws IdUnusedException
	{
		// item is available if avaialability checks are <b>NOT</b> enabled OR if it's in /attachment
		boolean available = (! m_availabilityChecksEnabled) || isAttachmentResource(id);

		GroupAwareEntity entity = null;
		//boolean isCollection = id.endsWith(Entity.SEPARATOR);
		while(!available && entity == null && id != null && ! id.trim().equals(""))
		{
			if(ROOT_COLLECTIONS.contains(id))
			{
				available = true;
			}
			else
			{
				try
				{
					if (isCollection(id))
					{
						entity = findCollection(id);
					}
					else
					{
						entity = findResource(id);
					}
				}
				catch (TypeException ignore)
				{
					if(isCollection(id))
					{
						M_log.warn("trying to get collection, found resource: " + id);
					}
					else
					{
						M_log.warn("trying to get resource, found collection: " + id);
					}
				}

				if (entity == null)
				{
					id = isolateContainingId(id);
					// isCollection = true;
				}
			}
		}

		if(!available && entity != null)
		{
			String creator = entity.getProperties().getProperty(ResourceProperties.PROP_CREATOR);
			String userId = SessionManager.getCurrentSessionUserId();

			// if we are in a roleswapped state, we want to ignore the creator check since it would not necessarily reflect an alternate role
			String[] refs = StringUtil.split(id, Entity.SEPARATOR);
			String roleswap = null;
			for (int i = 0; i < refs.length; i++)
			{
				roleswap = (String)SessionManager.getCurrentSession().getAttribute("roleswap/site/" + refs[i]);
				if (roleswap!=null)
					break;
			}
			if (roleswap==null)
			{
				// available if user is creator
				available = ( creator != null && userId != null && creator.equals(userId) ) 
				|| ( creator == null && userId == null );
			}
			if(! available)
			{
				// available if user has permission to view hidden entities
				String lock = AUTH_RESOURCE_HIDDEN;
				available = SecurityService.unlock(lock, entity.getReference());

				if(! available)
				{
					// available if not hidden or in a hidden collection
					available = entity.isAvailable();
				}

			}
		}

		return available;

	}

	/**
	 * Determine whether an entity is available to this user at this time, taking into account whether the item is hidden and the user's 
	 * status with respect to viewing hidden entities in this context.
	 * @param entityId
	 * @return true if the item is not hidden or it's hidden but the user has permissions to view hidden items in this context (site? folder? group?), 
	 * and false otherwise. 
	 */
	public boolean isAvailable(String entityId)
	{
		boolean available = true;
		try
		{
			available = availabilityCheck(entityId);
		}
		catch(IdUnusedException e)
		{
			available = false;
		}
		return available;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param id
	 *        The resource id string, or null if no resource is involved.
	 * @return true if permitted, false if not.
	 */
	protected boolean unlockCheck(String lock, String id)
	{
		boolean isAllowed = SecurityService.isSuperUser();
		if(! isAllowed)
		{
			lock = convertLockIfDropbox(lock, id);

			// make a reference from the resource id, if specified
			String ref = null;
			if (id != null)
			{
				ref = getReference(id);
			}

			isAllowed = ref != null && SecurityService.unlock(lock, ref);

			if(isAllowed && lock != null && (lock.startsWith("content.") || lock.startsWith("dropbox.")) && m_availabilityChecksEnabled)
			{
				try 
				{
					isAllowed = availabilityCheck(id);
				} 
				catch (IdUnusedException e) 
				{
					// ignore because we would have caught this earlier.
					M_log.debug("BaseContentService.unlockCheck(" + lock + "," + id + ") IdUnusedException " + e);
				}
			}	
		}

		return isAllowed;

	} // unlockCheck

	/**
	 * Throws a PermissionException if the resource with the given Id is explicitly locked
	 * 
	 * @param id
	 * @throws PermissionException
	 */
	protected void checkExplicitLock(String id) throws PermissionException
	{
		String uuid = this.getUuid(id);

		if (uuid != null && this.isLocked(uuid))
		{
			// TODO: WebDAV locks need to be more sophisticated than this
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), "remove", id);
		}
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param id
	 *        The resource id string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	protected void unlock(String lock, String id) throws PermissionException
	{
		if(SecurityService.isSuperUser())
		{
			return;
		}

		lock = convertLockIfDropbox(lock, id);

		// make a reference from the resource id, if specified
		String ref = null;
		if (id != null)
		{
			ref = getReference(id);
		}

		if (!SecurityService.unlock(lock, ref))
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), lock, ref);
		}
		boolean available = false;
		try 
		{
			available = availabilityCheck(id);
		} 
		catch (IdUnusedException e) 
		{
			// ignore. this was checked earlier in the call
		}
		if(! available)
		{
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), lock, ref);
		}

	} // unlock

	/**
	 * Check security permission for all contained collections of the given collection (if any) (not the collection itself)
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource id string, or null if no resource is involved.
	 * @exception PermissionException
	 *            Thrown if the user does not have access
	 */
	/*
	 * protected void unlockContained(String lock, ContentCollection collection) throws PermissionException { if (SecurityService == null) return; Iterator it = collection.getMemberResources().iterator(); while (it.hasNext()) { Object mbr = it.next(); if
	 * (mbr == null) continue; // for a contained collection, check recursively if (mbr instanceof ContentCollection) { unlockContained(lock, (ContentCollection) mbr); } // for resources, check else if (mbr instanceof ContentResource) { unlock(lock,
	 * ((ContentResource) mbr).getId()); } } } // unlockContained
	 */

	/**
	 * Create the live properties for a collection.
	 * 
	 * @param c
	 *        The collection.
	 */
	protected void addLiveCollectionProperties(ContentCollectionEdit c)
	{
		ResourcePropertiesEdit p = c.getPropertiesEdit();
		String current = SessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_CREATOR, current);
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

		p.addProperty(ResourceProperties.PROP_IS_COLLECTION, "true");

	} // addLiveCollectionProperties

	/**
	 * Create the live properties for a collection.
	 * 
	 * @param c
	 *        The collection.
	 */
	protected void addLiveUpdateCollectionProperties(ContentCollectionEdit c)
	{
		ResourcePropertiesEdit p = c.getPropertiesEdit();
		String current = SessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

	} // addLiveUpdateCollectionProperties

	/**
	 * Create the live properties for a resource.
	 * 
	 * @param r
	 *        The resource.
	 */
	protected void addLiveResourceProperties(ContentResourceEdit r)
	{
		ResourcePropertiesEdit p = r.getPropertiesEdit();

		String current = SessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_CREATOR, current);
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

		p.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(r.getContentLength()));
		p.addProperty(ResourceProperties.PROP_CONTENT_TYPE, r.getContentType());

		p.addProperty(ResourceProperties.PROP_IS_COLLECTION, "false");

	} // addLiveResourceProperties

	/**
	 * Update the live properties for a resource when modified (for a resource).
	 * 
	 * @param r
	 *        The resource.
	 */
	protected void addLiveUpdateResourceProperties(ContentResourceEdit r)
	{
		ResourcePropertiesEdit p = r.getPropertiesEdit();

		String current = SessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = TimeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

		p.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(r.getContentLength()));
		p.addProperty(ResourceProperties.PROP_CONTENT_TYPE, r.getContentType());

	} // addLiveUpdateResourceProperties

	/**
	 * Make sure that the entire set of properties are present, adding whatever is needed, replacing nothing that's there already.
	 * 
	 * @param r
	 *        The resource.
	 */
	protected void assureResourceProperties(ContentResourceEdit r)
	{
		ResourcePropertiesEdit p = r.getPropertiesEdit();

		String current = SessionManager.getCurrentSessionUserId();
		String now = TimeService.newTime().toString();

		if (p.getProperty(ResourceProperties.PROP_CREATOR) == null)
		{
			p.addProperty(ResourceProperties.PROP_CREATOR, current);
		}

		if (p.getProperty(ResourceProperties.PROP_CREATION_DATE) == null)
		{
			p.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		}

		if (p.getProperty(ResourceProperties.PROP_MODIFIED_BY) == null)
		{
			p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);
		}

		if (p.getProperty(ResourceProperties.PROP_MODIFIED_DATE) == null)
		{
			p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);
		}

		// these can just be set
		p.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(r.getContentLength()));
		p.addProperty(ResourceProperties.PROP_CONTENT_TYPE, r.getContentType());
		p.addProperty(ResourceProperties.PROP_IS_COLLECTION, "false");

	} // assureResourceProperties

	/**
	 * Add properties for a resource.
	 * 
	 * @param r
	 *        The resource.
	 * @param props
	 *        The properties.
	 */
	protected void addProperties(ResourcePropertiesEdit p, ResourceProperties props)
	{
		if (props == null) return;

		Iterator it = props.getPropertyNames();
		while (it.hasNext())
		{
			String name = (String) it.next();

			// skip any live properties
			if (!props.isLiveProperty(name))
			{
				p.addProperty(name, props.getProperty(name));
			}
		}

	} // addProperties

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Collections
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * check permissions for addCollection().
	 * 
	 * @param id
	 *        The id of the new collection.
	 * @return true if the user is allowed to addCollection(id), false if not.
	 */
	public boolean allowAddCollection(String id)
	{
		// collection must also end in the separator (we fix it)
		if (!id.endsWith(Entity.SEPARATOR))
		{
			id = id + Entity.SEPARATOR;
		}

		// check security
		return unlockCheck(AUTH_RESOURCE_ADD, id);

	} // allowAddCollection

	/**
	 * Create a new collection with the given resource id.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @param properties
	 *        A java Properties object with the properties to add to the new collection.
	 * @exception IdUsedException
	 *            if the id is already in use.
	 * @exception IdInvalidException
	 *            if the id is invalid.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection, or add a member to a collection.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @return a new ContentCollection object.
	 */
	public ContentCollection addCollection(String id, ResourceProperties properties) throws IdUsedException, IdInvalidException,
	PermissionException, InconsistentException
	{
		ContentCollectionEdit edit = addCollection(id);

		// add the provided of properties
		addProperties(edit.getPropertiesEdit(), properties);

		// commit the change
		commitCollection(edit);

		return edit;

	} // addCollection

	public ContentCollection addCollection(String id, ResourceProperties properties, Collection groups) 
	throws IdUsedException, IdInvalidException, PermissionException, InconsistentException 
	{
		ContentCollectionEdit edit = addCollection(id);

		// add the provided of properties
		addProperties(edit.getPropertiesEdit(), properties);
		try
		{
			if(groups == null || groups.isEmpty())
			{
				((BasicGroupAwareEdit) edit).clearGroupAccess();
			}
			else
			{
				((BasicGroupAwareEdit) edit).setGroupAccess(groups);
			}
		}
		catch(InconsistentException e)
		{
			// ignore
		}

		// commit the change
		commitCollection(edit);

		return edit;

	}

	public ContentCollection addCollection(String id, ResourceProperties properties, Collection groups, boolean hidden, Time releaseDate, Time retractDate) 
	throws IdUsedException, IdInvalidException, PermissionException, InconsistentException 
	{
		ContentCollectionEdit edit = addCollection(id);

		// add the provided of properties
		addProperties(edit.getPropertiesEdit(), properties);
		try
		{
			if(groups == null || groups.isEmpty())
			{
				((BasicGroupAwareEdit) edit).clearGroupAccess();
			}
			else
			{
				((BasicGroupAwareEdit) edit).setGroupAccess(groups);
			}
		}
		catch(InconsistentException e)
		{
			// ignore
		}
		edit.setAvailability(hidden, releaseDate, retractDate);

		// commit the change
		commitCollection(edit);

		return edit;

	}

	/**
	 * Create a new collection with the given resource id, locked for update. Must commitCollection() to make official, or cancelCollection() when done!
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUsedException
	 *            if the id is already in use.
	 * @exception IdInvalidException
	 *            if the id is invalid.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection, or add a member to a collection.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @return a new ContentCollection object.
	 */
	public ContentCollectionEdit addCollection(String id) throws IdUsedException, IdInvalidException, PermissionException,
	InconsistentException
	{
		// check the id's validity (this may throw IdInvalidException)
		// use only the "name" portion, separated at the end
		String justName = isolateName(id);
		Validator.checkResourceId(justName);

		// collection must also end in the separator (we fix it)
		if (!id.endsWith(Entity.SEPARATOR))
		{
			id = id + Entity.SEPARATOR;
		}

		String containerId = isolateContainingId(id);
		ThreadLocalManager.set("members@" + containerId, null);
		ThreadLocalManager.set("getCollections@" + containerId, null);
		//ThreadLocalManager.set("getResources@" + containerId, null);

		// check security
		unlock(AUTH_RESOURCE_ADD, id);

		return addValidPermittedCollection(id);
	}

	/**
	 * @param collectionId
	 * @param name
	 * @return
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUnusedException
	 *            if the collectionId does not identify an existing collection. 
	 * @exception IdUnusedException
	 *            if the collection id for the proposed name already exists in this collection.
	 * @exception IdLengthException
	 *            if the new collection id exceeds the maximum number of characters for a valid collection id.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 */
	public ContentCollectionEdit addCollection(String collectionId, String name)
	throws PermissionException, IdUnusedException, IdUsedException, 
	IdLengthException, IdInvalidException, TypeException
	{
		// check the id's validity (this may throw IdInvalidException)
		// use only the "name" portion, separated at the end
		Validator.checkResourceId(name);
		checkCollection(collectionId);

		String id = collectionId + name.trim();
		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new IdLengthException(id);
		}


		// collection must also end in the separator (we fix it)
		if (!id.endsWith(Entity.SEPARATOR))
		{
			id = id + Entity.SEPARATOR;
		}

		// check security
		unlock(AUTH_RESOURCE_ADD, id);

		ContentCollectionEdit edit = null;

		try
		{
			edit = addValidPermittedCollection(id);
		}
		catch(InconsistentException e)
		{
			throw new IdUnusedException(collectionId);
		}

		return edit;
	}
	/**
	 * Create a new collection with the given resource id, locked for update. Must commitCollection() to make official, or cancelCollection() when done!
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUsedException
	 *            if the id is already in use.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @return a new ContentCollection object.
	 */
	protected ContentCollectionEdit addValidPermittedCollection(String id) throws IdUsedException, InconsistentException
	{
		// make sure the containing collection exists
		String container = isolateContainingId(id);
		ContentCollection containingCollection = m_storage.getCollection(container);
		if (containingCollection == null)
		{
			// make any missing collections
			generateCollections(container);

			// try again
			containingCollection = m_storage.getCollection(container);
			if (containingCollection == null) throw new InconsistentException(id);
		}

		// reserve the collection in storage - it will fail if the id is in use
		BaseCollectionEdit edit = (BaseCollectionEdit) m_storage.putCollection(id);
		if (edit == null)
		{
			throw new IdUsedException(id);
		}

		// add live properties
		addLiveCollectionProperties(edit);

		// track event
		edit.setEvent(EVENT_RESOURCE_ADD);

		return edit;
	}

	/**
	 * check permissions for getCollection().
	 * 
	 * @param id
	 *        The id of the collection.
	 * @return true if the user is allowed to getCollection(id), false if not.
	 */
	public boolean allowGetCollection(String id)
	{
		return unlockCheck(AUTH_RESOURCE_READ, id);

	} // allowGetCollection

	/**
	 * Check access to the collection with this local resource id.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUnusedException
	 *            if the id does not exist.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to see this collection (or read through containing collections).
	 */
	public void checkCollection(String id) throws IdUnusedException, TypeException, PermissionException
	{
		unlock(AUTH_RESOURCE_READ, id);

		ContentCollection collection = findCollection(id);
		if (collection == null) throw new IdUnusedException(id);

	} // checkCollection

	/**
	 * Access the collection with this local resource id. The collection internal members and properties are accessible from the returned Colelction object.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUnusedException
	 *            if the id does not exist.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to see this collection (or read through containing collections).
	 * @return The ContentCollection object found.
	 */
	public ContentCollection getCollection(String id) throws IdUnusedException, TypeException, PermissionException
	{
		unlock(AUTH_RESOURCE_READ, id);

		ContentCollection collection = findCollection(id);
		if (collection == null) throw new IdUnusedException(id);

		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_READ, collection.getReference(), false));

		return collection;

	} // getCollection

	/**
	 * Access a List of ContentEntity objects (resources and collections) in this path (and below) if the current user has access to the collection.
	 * 
	 * @param id
	 *        A collection id.
	 * @return a List of the ContentEntity objects.
	 */
	public List getAllEntities(String id)
	{
		List rv = new ArrayList();

		// get the collection members
		try
		{
			ContentCollection collection = getCollection(id);
			if (collection != null)
			{
				getAllEntities(collection, rv, true);
			}
		}
		catch (TypeException e)
		{
			// should result in an empty list
		}
		catch (IdUnusedException e)
		{
			// should result in an empty list
		}
		catch (PermissionException e)
		{
			// should result in an empty list
		}

		return rv;

	} // getAllEntities

	/**
	 * Access a List of all the ContentResource objects in this collection (and below).
	 * 
	 * @param collection
	 *        The collection.
	 * @param rv
	 *        The list in which to accumulate resource objects.
	 * @param includeCollections TODO
	 */
	protected void getAllEntities(ContentCollection collection, List rv, boolean includeCollections)
	{
		if(includeCollections)
		{
			rv.add(collection);
		}

		List members = collection.getMemberResources();

		// process members
		for (Iterator iMbrs = members.iterator(); iMbrs.hasNext();)
		{
			ContentEntity next = (ContentEntity) iMbrs.next();

			// if resource, add it if permitted

			if (next instanceof ContentResource)
			{
				rv.add(next);
			}

			// if collection, again
			else
			{
				getAllEntities((ContentCollection) next, rv, includeCollections);
			}
		}

	} // getAllEntities


	/**
	 * Access a List of all the ContentResource objects in this path (and below) which the current user has access.
	 * 
	 * @param id
	 *        A collection id.
	 * @return a List of the ContentResource objects.
	 */
	public List getAllResources(String id)
	{
		List rv = new ArrayList();

		// get the collection members
		try
		{
			ContentCollection collection = findCollection(id);
			if (collection != null)
			{
				getAllResources(collection, rv, false);
			}
		}
		catch (TypeException e)
		{
		}

		return rv;

	} // getAllResources

	/**
	 * Access a List of all the ContentResource objects in this collection (and below) which the current user has access.
	 * 
	 * @param collection
	 *        The collection.
	 * @param rv
	 *        The list in which to accumulate resource objects.
	 * @param includeCollections TODO
	 */
	protected void getAllResources(ContentCollection collection, List rv, boolean includeCollections)
	{
		if(includeCollections)
		{
			if (unlockCheck(AUTH_RESOURCE_READ, collection.getId()))
			{
				rv.add(collection);
			}
		}

		List members = collection.getMemberResources();

		// process members
		for (Iterator iMbrs = members.iterator(); iMbrs.hasNext();)
		{
			ContentEntity next = (ContentEntity) iMbrs.next();

			// if resource, add it if permitted

			if (next instanceof ContentResource)
			{
				if (unlockCheck(AUTH_RESOURCE_READ, next.getId()))
				{
					rv.add(next);
				}
			}

			// if collection, again
			else
			{
				getAllResources((ContentCollection) next, rv, includeCollections);
			}
		}

	} // getAllResources

	/**
	 * Access the collection with this local resource id. Internal find does the guts of finding without security or event tracking. The collection internal members and properties are accessible from the returned Colelction object.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @return The ContentCollection object found, or null if not.
	 */
	protected ContentCollection findCollection(String id) throws TypeException
	{
		ContentCollection collection = null;
		try
		{
			collection = (ContentCollection) ThreadLocalManager.get("findCollection@" + id);
		}
		catch(ClassCastException e)
		{
			throw new TypeException(id);
		}

		if(collection == null)
		{
			collection = m_storage.getCollection(id);

			if(collection != null)
			{
				ThreadLocalManager.set("findCollection@" + id, collection);	// new BaseCollectionEdit(collection));
			}
		}
		else
		{
			collection = new BaseCollectionEdit(collection);
		}

		//		// if not caching
		//		if ((!m_caching) || (m_cache == null) || (m_cache.disabled()))
		//		{
		//			// TODO: current service caching
		//			collection = m_storage.getCollection(id);
		//		}
		//		else
		//		{
		//			// if we have it cached, use it (hit or miss)
		//			String key = getReference(id);
		//			if (m_cache.containsKey(key))
		//			{
		//				Object o = m_cache.get(key);
		//				if ((o != null) && (!(o instanceof ContentCollection))) throw new TypeException(id);
		//
		//				collection = (ContentCollection) o;
		//			}
		//
		//			// if not in the cache, see if we have it in our info store
		//			else
		//			{
		//				collection = m_storage.getCollection(id);
		//
		//				// cache it (hit or miss)
		//				m_cache.put(key, collection);
		//			}
		//		}

		return collection;

	} // findCollection

	/**
	 * check permissions for editCollection()
	 * 
	 * @param id
	 *        The id of the collection.
	 * @return true if the user is allowed to update the collection, false if not.
	 */
	public boolean allowUpdateCollection(String id)
	{
		boolean isAllowed = allowUpdate(id);

		if(isAllowed)
		{
			try
			{
				checkExplicitLock(id);
			}
			catch(PermissionException e)
			{
				isAllowed = false;
			}
		}

		return isAllowed;

	} // allowUpdateCollection

	/**
	 * check permissions for revising collections or resources
	 * @param id The id of the collection.
	 * @return true if the user is allowed to update the collection, false if not.
	 */
	public boolean allowUpdate(String id)
	{
		String currentUser = SessionManager.getCurrentSessionUserId();
		String owner = "";

		try
		{
			ResourceProperties props = getProperties(id);
			owner = props.getProperty(ResourceProperties.PROP_CREATOR);
		}
		catch ( Exception e ) 
		{
			// PermissionException can be thrown if not AUTH_RESOURCE_READ
			return false;
		}

		// check security to delete any collection
		if ( unlockCheck(AUTH_RESOURCE_WRITE_ANY, id) )
			return true;

		// check security to delete own collection
		else if ( currentUser != null && currentUser.equals(owner) 
				&& unlockCheck(AUTH_RESOURCE_WRITE_OWN, id) )
			return true;

		// check security to delete own collection for anonymous users
		else if ( currentUser == null && owner == null && 
				unlockCheck(AUTH_RESOURCE_WRITE_OWN, id) )
			return true;

		// otherwise not authorized
		else
			return false;

	} // allowUpdate

	/**
	 * check permissions for removeCollection(). Note: for just this collection, not the members on down.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @return true if the user is allowed to removeCollection(id), false if not.
	 */
	public boolean allowRemoveCollection(String id)
	{
		return allowRemove(id);

	} // allowRemoveCollection

	/**
	 * check permissions for removing collections or resources
	 * Note: for just this collection, not the members on down.
	 * @param id The id of the collection.
	 * @return true if the user is allowed to removeCollection(id), false if not.
	 */
	protected boolean allowRemove(String id)
	{
		String ref = getReference(id);
		String currentUser = SessionManager.getCurrentSessionUserId();
		String owner = "";

		try
		{
			ResourceProperties props = getProperties(id);
			owner = props.getProperty(ResourceProperties.PROP_CREATOR);
		}
		catch ( Exception e ) 
		{
			// PermissionException can be thrown if not RESOURCE_AUTH_READ
			return false;
		}

		// check security to delete any collection
		if ( unlockCheck(AUTH_RESOURCE_REMOVE_ANY, id) )
			return true;

		// check security to delete own collection
		else if ( currentUser != null && currentUser.equals(owner) && 
				unlockCheck(AUTH_RESOURCE_REMOVE_OWN, id) )
			return true;

		// check security to delete own collection for anonymous users
		else if ( currentUser == null && owner == null && 
				unlockCheck(AUTH_RESOURCE_REMOVE_OWN, id) )
			return true;

		// otherwise not authorized
		else
			return false;

	} // allowRemove

	/**
	 * Remove just a collection. It must be empty.
	 * 
	 * @param collection
	 *        The collection to remove.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to remove this collection, read through any containing
	 * @exception InconsistentException
	 *            if the collection has members, so that the removal would leave things in an inconsistent state.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and an attempt to access the resource body of any collection member fails.
	 */
	public void removeCollection(ContentCollectionEdit edit) throws TypeException, PermissionException, InconsistentException,
	ServerOverloadException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("removeCollection(): closed ContentCollectionEdit", e);
			return;
		}

		// check security 
		if ( ! allowRemoveCollection(edit.getId()) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, edit.getReference());

		// clear thread-local cache SAK-12126
		ThreadLocalManager.set("members@" + edit.getId(), null);
		ThreadLocalManager.set("getResources@" + edit.getId(), null);

		// check for members
		List members = edit.getMemberResources();
		if (!members.isEmpty()) throw new InconsistentException(edit.getId());

		// complete the edit
		m_storage.removeCollection(edit);

		// close the edit object
		((BaseCollectionEdit) edit).closeEdit();

		((BaseCollectionEdit) edit).setRemoved();

		// remove the old version from thread-local cache.
		ThreadLocalManager.set("findCollection@" + edit.getId(), null);

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(edit.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			M_log.debug("removeCollection: removing realm for : " + edit.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
			M_log.debug("removeCollection: removing realm for : " + edit.getReference() + " : " + ignore);
		}

		// track it (no notification)
		String ref = edit.getReference(null);
		EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, ref, true,
				NotificationService.NOTI_NONE));
		EventTrackingService.cancelDelays(ref);

	} // removeCollection

	/**
	 * Remove a collection and all members of the collection, internal or deeper.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUnusedException
	 *            if the id does not exist.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to remove this collection, read through any containing
	 * @exception InUseException
	 *            if the collection or a contained member is locked by someone else. collections, or remove any members of the collection.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and an attempt to access the resource body of any collection member fails.
	 */
	public void removeCollection(String id) throws IdUnusedException, TypeException, PermissionException, InUseException,
	ServerOverloadException
	{
		// check security 
		if ( ! allowRemoveCollection(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id) );

		// find the collection
		ContentCollection thisCollection = findCollection(id);
		if (thisCollection == null) throw new IdUnusedException(id);

		// check security: can we remove members (if any)
		// Note: this will also be done in clear(), except some might get deleted before one is not allowed.
		// unlockContained(AUTH_RESOURCE_REMOVE, thisCollection);

		// get an edit
		ContentCollectionEdit edit = editCollection(id);

		// clear thread-local cache SAK-12126
		ThreadLocalManager.set("members@" + id, null);
		ThreadLocalManager.set("getResources@" + id, null);

		// clear of all members (recursive)
		// Note: may fail if something's in use or not permitted. May result in a partial clear.
		try
		{
			((BaseCollectionEdit) edit).clear();

			// remove
			removeCollection(edit);
		}
		catch (InconsistentException e)
		{
			M_log.warn("removeCollection():", e);
		}
		finally
		{
			// if we don't get the remove done, we need to cancel here
			if (((BaseCollectionEdit) edit).isActiveEdit())
			{
				cancelCollection(edit);
			}
		}

	} // removeCollection

	/**
	 * Commit the changes made, and release the lock. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentCollectionEdit object to commit.
	 * @throws PermissionException 
	 */
	public void commitCollection(ContentCollectionEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("commitCollection(): closed ContentCollectionEdit", e);
			return;
		}

		if(AccessMode.GROUPED == edit.getAccess())
		{
			verifyGroups(edit, edit.getGroups());
		}

		if(this.m_prioritySortEnabled)
		{
			ResourcePropertiesEdit props = edit.getPropertiesEdit();
			String sortBy = props.getProperty(ResourceProperties.PROP_CONTENT_PRIORITY);
			if(sortBy == null)
			{
				// add a default value that sorts new items after existing items, with new folders before new resources
				ContentCollection container = edit.getContainingCollection();
				int count = container.getMemberCount();
				props.addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, Integer.toString(count + 1));
			}
		}

		// update the properties for update
		addLiveUpdateCollectionProperties(edit);

		// complete the edit
		m_storage.commitCollection(edit);

		// close the edit object
		((BaseCollectionEdit) edit).closeEdit();

		// the collection has changed so we must remove the old version from thread-local cache
		ThreadLocalManager.set("findCollection@" + edit.getId(), null);
		String containerId = isolateContainingId(edit.getId());
		ThreadLocalManager.set("findCollection@" + containerId, null);
		ThreadLocalManager.set("members@" + containerId, null);
		ThreadLocalManager.set("getCollections@" + containerId, null);
		//ThreadLocalManager.set("getResources@" + containerId, null);

		// track it (no notification)
		String ref = edit.getReference(null);
		EventTrackingService.post(EventTrackingService.newEvent(((BaseCollectionEdit) edit)
				.getEvent(), ref, true, NotificationService.NOTI_NONE));

	} // commitCollection

	private void postAvailableEvent(GroupAwareEntity entity, String ref, int priority)
	{
		// cancel all scheduled available events for this entity. 
		EventTrackingService.cancelDelays(ref, EVENT_RESOURCE_AVAILABLE);

		// if resource isn't available yet, schedule an event to tell when it becomes available
		if (!entity.isAvailable())
		{
			EventTrackingService.delay(EventTrackingService.newEvent(EVENT_RESOURCE_AVAILABLE, ref,
					false, priority), entity.getReleaseDate());
			entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.FALSE.toString());
		}
		else
		{
			// for available resources, make sure this event hasn't been fired before. this check is
			// to ensure an available event isn't sent multiple times for the same resource.
			String notified = entity.getProperties().getProperty(PROP_AVAIL_NOTI);

			// do not post an available event for updates
			if (!Boolean.TRUE.toString().equalsIgnoreCase(notified) && 
					!EVENT_RESOURCE_WRITE.equals(((BaseResourceEdit) entity).getEvent()))
			{
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_AVAILABLE,
						ref, false, priority));
				entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.TRUE.toString());
			}
		}
	}

	/**
	 * Recursively traverse the heirarchy of ContentEntity objects contained within a collection and remove access groups if they 
	 * are not included in the set defined for the initial collection.  The branching stops whenever we verify a ContentCollection 
	 * with "grouped" access or a ContentResource. 
	 * @param collection 
	 * @param groups
	 */
	protected void verifyGroups(ContentCollection collection, Collection groups) 
	{
		Collection members = collection.getMemberResources();
		if(members == null || members.isEmpty())
		{
			return;
		}
		Iterator memberIt = members.iterator();
		while(memberIt.hasNext())
		{
			ContentEntity member = (ContentEntity) memberIt.next();
			if(AccessMode.GROUPED == member.getAccess())
			{
				adjustGroups(member, groups);
			}

			if(member instanceof ContentCollection)
			{
				// recursive call
				verifyGroups((ContentCollectionEdit) member, groups);
			}
		}

	}

	protected void adjustGroups(ContentEntity member, Collection groups) 
	{
		// check groups and then return
		Collection subgroups = member.getGroups();
		if(groups.containsAll(subgroups))
		{
			// this entity's groups are OK, so do nothing
		}
		else
		{
			Collection newgroups = new ArrayList();
			Iterator groupIt = subgroups.iterator();
			while(groupIt.hasNext())
			{
				String groupRef = (String) groupIt.next();
				if(groups.contains(groupRef))
				{
					newgroups.add(groupRef);
				}
			}
			if(member instanceof ContentResource)
			{
				ContentResourceEdit edit = m_storage.editResource(member.getId());
				try 
				{
					if(newgroups.isEmpty())
					{
						edit.clearGroupAccess();
					}
					else
					{
						edit.setGroupAccess(newgroups);
					}
					// addLiveUpdateResourceProperties(edit);
					m_storage.commitResource(edit);
					// close the edit object
					((BaseResourceEdit) edit).closeEdit();
				} 
				catch (InconsistentException e) 
				{
					// If change of groups is consistent in superfolder, this should not occur here
					m_storage.cancelResource(edit);
					M_log.warn("verifyGroups(): ", e);
				} 
				catch (PermissionException e) 
				{
					// If user has permission to change groups in superfolder, this should not occur here
					m_storage.cancelResource(edit);
					M_log.warn("verifyGroups(): ", e);
				} 
				catch (ServerOverloadException e) 
				{
					M_log.warn("verifyGroups(): ", e);
				}
			}
			else
			{
				ContentCollectionEdit edit = m_storage.editCollection(member.getId());
				try 
				{
					if(newgroups.isEmpty())
					{
						edit.clearGroupAccess();
					}
					else
					{
						edit.setGroupAccess(newgroups);
					}
					// addLiveUpdateCollectionProperties(edit);
					m_storage.commitCollection(edit);
				} 
				catch (InconsistentException e) 
				{
					// If change of groups is consistent in superfolder, this should not occur here
					m_storage.cancelCollection(edit);
					M_log.warn("verifyGroups(): ", e);
				} 
				catch (PermissionException e) 
				{
					// If user has permission to change groups in superfolder, this should not occur here
					m_storage.cancelCollection(edit);
					M_log.warn("verifyGroups(): ", e);
				}
			}
		}

	}

	/**
	 * Cancel the changes made object, and release the lock. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentCollectionEdit object to commit.
	 */
	public void cancelCollection(ContentCollectionEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("cancelCollection(): closed ContentCollectionEdit", e);
			return;
		}

		// release the edit lock
		m_storage.cancelCollection(edit);

		// if the edit is newly created during an add collection process, remove it from the storage
		if (((BaseCollectionEdit) edit).getEvent().equals(EVENT_RESOURCE_ADD))
		{
			removeRecursive(edit);
			m_storage.removeCollection(edit);
		}

		// close the edit object
		((BaseCollectionEdit) edit).closeEdit();

	} // cancelCollection

	/**
	 * used to remove any members of a collection whoes add was canceled.
	 * 
	 * @param parent
	 */
	protected void removeRecursive(ContentCollection parent)
	{
		List members = parent.getMemberResources();

		for (Iterator i = members.iterator(); i.hasNext();)
		{
			Object resource = i.next();
			try
			{
				if (resource instanceof ContentResource)
				{
					removeResource(((ContentResource) resource).getId());
				}
				else if (resource instanceof ContentCollection)
				{
					ContentCollection collection = (ContentCollection) resource;
					removeRecursive(collection);
					removeCollection(collection.getId());
				}
			}
			catch (IdUnusedException e)
			{
				M_log.warn("failed to removed canceled collection child", e);
			}
			catch (TypeException e)
			{
				M_log.warn("failed to removed canceled collection child", e);
			}
			catch (PermissionException e)
			{
				M_log.warn("failed to removed canceled collection child", e);
			}
			catch (InUseException e)
			{
				M_log.warn("failed to removed canceled collection child", e);
			}
			catch (ServerOverloadException e)
			{
				M_log.warn("failed to removed canceled collection child", e);
			}
		}
	}

	protected void cacheEntities(List entities)
	{
		if(entities == null)
		{
			return;
		}

		for(ContentEntity entity : (List<ContentEntity>) entities)
		{
			if(entity == null)
			{
				// do nothing
			}
			else if(entity instanceof ContentResource)
			{
				ThreadLocalManager.set("findResource@" + entity.getId(), entity);
			}
			else if(entity instanceof ContentCollection)
			{
				ThreadLocalManager.set("findCollection@" + entity.getId(), entity);
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Resources
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * check permissions for addResource().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to addResource(id), false if not.
	 */
	public boolean allowAddResource(String id)
	{
		// resource must also NOT end with a separator characters (we fix it)
		if (id.endsWith(Entity.SEPARATOR))
		{
			id = id.substring(0, id.length() - 1);
		}

		// check security
		boolean isAllowed = unlockCheck(AUTH_RESOURCE_ADD, id);

		if(isAllowed)
		{
			// check for explicit locks
			try 
			{
				checkExplicitLock(id);
			} 
			catch (PermissionException e) 
			{
				isAllowed = false;
			}
		}

		return isAllowed;

	} // allowAddResource

	/**
	 * Create a new resource with the given resource id and attributes, including group awareness.
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A java Properties object with the properties to add to the new resource.
	 * @param groups
	 *        A collection (String) of references to Group objects representing the site subgroups that should have access to this entity.
	 *        May be empty to indicate access is not limited to a group or groups.
	 * @param priority
	 *        The notification priority for this commit.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUsedException
	 *            if the resource id is already in use.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addResource(String id, String type, byte[] content, ResourceProperties properties, Collection groups, int priority)
	throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException,
	ServerOverloadException
	{
		id = (String) fixTypeAndId(id, type).get("id");
		ContentResourceEdit edit = addResource(id);
		edit.setContentType(type);
		edit.setContent(content);
		addProperties(edit.getPropertiesEdit(), properties);
		// commit the change
		if(groups == null || groups.isEmpty())
		{
			// access is inherited (the default)
		}
		else
		{
			edit.setGroupAccess(groups);
			// TODO: Need to deal with failure here
		}

		try
		{
			commitResource(edit, priority);
		}
		catch(OverQuotaException e)
		{
			M_log.debug("OverQuotaException " + e);
			try
			{
				removeResource(edit.getId());
			}
			catch(Exception e1)
			{
				// ignore -- no need to remove the resource if it doesn't exist
				M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
			}
			throw e;
		}
		catch(ServerOverloadException e)
		{
			M_log.debug("ServerOverloadException " + e);
			try
			{
				removeResource(edit.getId());
			}
			catch(Exception e1)
			{
				// ignore -- no need to remove the resource if it doesn't exist
				M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
			}
			throw e;
		}

		return edit;

	} // addResource

	/**
	 * Create a new resource with the given resource id and attributes but no group awareness.
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A java Properties object with the properties to add to the new resource.
	 * @param priority
	 *        The notification priority for this commit.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUsedException
	 *            if the resource id is already in use.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addResource(String id, String type, byte[] content, ResourceProperties properties, int priority)
	throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException,
	ServerOverloadException
	{
		Collection no_groups = new ArrayList();
		return addResource(id, type, content, properties, no_groups, priority);
	}

	/**
	 * Create a new resource with the given resource name used as a resource id within the specified collection or (if that id is already in use) with a resource id based on a variation on the name to achieve a unique id, provided a unique id can be found
	 * before a limit is reached on the number of attempts to achieve uniqueness.  Used to create a group-aware resource.
	 * 
	 * @param name
	 *        The name of the new resource (such as a filename).
	 * @param collectionId
	 *        The id of the collection to which the resource should be added.
	 * @param limit
	 *        The maximum number of attempts at finding a unique id based on the given name.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A ResourceProperties object with the properties to add to the new resource.
	 * @param groups
	 *        A collection (String) of references to Group objects representing the site subgroups that should have access to this entity.
	 *        May be empty to indicate access is not limited to a group or groups.
	 * @param priority
	 *        The notification priority for this commit.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUniquenessException
	 *            if a unique resource id cannot be found before the limit on the number of attempts is reached.
	 * @exception IdLengthException
	 *            if the resource id exceeds the maximum number of characters for a valid resource id.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addResource(String name, String collectionId, int limit, String type, byte[] content,
			ResourceProperties properties, Collection groups, boolean hidden, Time releaseDate, Time retractDate, int priority) 
	throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, 
	InconsistentException, OverQuotaException, ServerOverloadException
	{
		try
		{
			collectionId = collectionId.trim();
			name = Validator.escapeResourceName(name.trim());
			checkCollection(collectionId);
		}
		catch (IdUnusedException e)
		{
			throw new InconsistentException(collectionId);
		}
		catch (TypeException e)
		{
			throw new InconsistentException(collectionId);
		}

		String id = collectionId + name;
		id = (String) fixTypeAndId(id, type).get("id");
		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new IdLengthException(id);
		}

		ContentResourceEdit edit = null;

		try
		{
			edit = addResource(id);
			edit.setContentType(type);
			edit.setContent(content);
			addProperties(edit.getPropertiesEdit(), properties);
			if(groups == null || groups.isEmpty())
			{
				// access is inherited (the default)
			}
			else
			{
				edit.setGroupAccess(groups);
				// TODO: Need to deal with failure here
			}
			edit.setAvailability(hidden, releaseDate, retractDate);

			try
			{
				// commit the change
				commitResource(edit, priority);
			}
			catch(OverQuotaException e)
			{
				M_log.debug("OverQuotaException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
			catch(ServerOverloadException e)
			{
				M_log.debug("ServerOverloadException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
		}
		catch (IdUsedException e)
		{
			try
			{
				checkResource(id);
			}
			catch (IdUnusedException inner_e)
			{
				// TODO: What does this condition actually represent? What exception should be thrown?
				throw new IdUniquenessException(id);
			}
			catch (TypeException inner_e)
			{
				throw new InconsistentException(id);
			}

			SortedSet siblings = new TreeSet();
			try
			{
				ContentCollection collection = findCollection(collectionId);
				siblings.addAll(collection.getMembers());
			}
			catch (TypeException inner_e)
			{
				throw new InconsistentException(collectionId);
			}

			int index = name.lastIndexOf(".");
			String base = name;
			String ext = "";
			if (index > 0 && !"Url".equalsIgnoreCase(type))
			{
				base = name.substring(0, index);
				ext = name.substring(index);
			}
			boolean trying = true;
			int attempts = 1;
			while (trying) // see end of loop for condition that enforces attempts <= limit)
			{
				String new_id = collectionId + base + "-" + attempts + ext;
				if (new_id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
				{
					throw new IdLengthException(new_id);
				}
				if (!siblings.contains(new_id))
				{
					try
					{
						edit = addResource(new_id);
						edit.setContentType(type);
						edit.setContent(content);
						if(groups == null || groups.isEmpty())
						{
							// access is inherited (the default)
						}
						else
						{
							edit.setGroupAccess(groups);
							// TODO: Need to deal with failure here
						}

						addProperties(edit.getPropertiesEdit(), properties);
						// commit the change
						commitResource(edit, priority);

						trying = false;
					}
					catch (IdUsedException ignore)
					{
						// try again
					}
				}
				attempts++;
				if (attempts > limit)
				{
					throw new IdUniquenessException(new_id);
				}
			}
		}
		return edit;

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public ContentResourceEdit addResource(String collectionId, String basename, String extension, int maximum_tries) 
	throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, 
	IdUnusedException, OverQuotaException, ServerOverloadException
	{
		// check the id's validity (this may throw IdInvalidException)
		// use only the "name" portion, separated at the end
		try
		{
			checkCollection(collectionId);
		}
		catch (TypeException e)
		{
			throw new IdUnusedException(collectionId);
		}

		if(basename == null)
		{
			throw new IdInvalidException("");
		}

		if(extension == null)
		{
			extension = "";
		}
		else
		{
			extension = extension.trim();
			if(extension.equals("") || extension.startsWith("."))
			{
				// do nothing
			}
			else
			{
				extension = "." + extension;
			}
		}

		basename = Validator.escapeResourceName(basename.trim());
		extension = Validator.escapeResourceName(extension);

		String name = basename + extension;
		String id = collectionId + name;
		if(id.length() > ContentHostingService.MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new IdLengthException(id);
		}

		BaseResourceEdit edit = null;

		int attempts = 0;
		boolean done = false;
		while(!done  && attempts < maximum_tries)
		{
			try
			{
				edit = (BaseResourceEdit) addResource(id);
				done = true;

				// add live properties
				addLiveResourceProperties(edit);

				ResourceProperties props = edit.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

				// track event
				edit.setEvent(EVENT_RESOURCE_ADD);
			}
			catch(InconsistentException inner_e)
			{
				throw new IdInvalidException(id);
			}
			catch(IdUsedException e)
			{
				SortedSet siblings = new TreeSet();
				try
				{
					ContentCollection collection = findCollection(collectionId);
					siblings.addAll(collection.getMembers());
				}
				catch (TypeException inner_e)
				{
					throw new IdUnusedException(collectionId);
				}

				boolean trying = true;

				// see end of loop for condition that enforces attempts <= limit)
				do
				{
					attempts++;
					name = basename + "-" + attempts + extension;
					id = collectionId + name;

					if (attempts > maximum_tries)
					{
						throw new IdUniquenessException(id);
					}

					if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
					{
						throw new IdLengthException(id);
					}
				}
				while (siblings.contains(id));
			}

		}

		ThreadLocalManager.set("members@" + collectionId, null);
		//ThreadLocalManager.set("getCollections@" + collectionId, null);
		ThreadLocalManager.set("getResources@" + collectionId, null);

		//		if (edit == null)
		//		{
		//			throw new IdUniquenessException(id);
		//		}

		return edit;

	}

	/**
	 * Create a new resource with the given resource name used as a resource id within the specified collection or (if that id is already in use) with a resource id based on a variation on the name to achieve a unique id, provided a unique id can be found
	 * before a limit is reached on the number of attempts to achieve uniqueness. Used to create a resource that is not group aware.
	 * 
	 * @param name
	 *        The name of the new resource (such as a filename).
	 * @param collectionId
	 *        The id of the collection to which the resource should be added.
	 * @param limit
	 *        The maximum number of attempts at finding a unique id based on the given name.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A ResourceProperties object with the properties to add to the new resource.
	 * @param priority
	 *        The notification priority for this commit.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUniquenessException
	 *            if a unique resource id cannot be found before the limit on the number of attempts is reached.
	 * @exception IdLengthException
	 *            if the resource id exceeds the maximum number of characters for a valid resource id.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addResource(String name, String collectionId, int limit, String type, byte[] content,
			ResourceProperties properties, int priority) 
	throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, 
	InconsistentException, OverQuotaException, ServerOverloadException
	{
		Collection no_groups = new ArrayList();
		return addResource(name, collectionId, limit, type, content, properties, no_groups, false, null, null, priority);
	}
	/**
	 * Create a new resource with the given resource id, locked for update. Must commitResource() to make official, or cancelResource() when done!
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUsedException
	 *            if the resource id is already in use.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @return a new ContentResource object.
	 */
	public ContentResourceEdit addResource(String id) throws PermissionException, IdUsedException, IdInvalidException,
	InconsistentException
	{
		// check the id's validity (this may throw IdInvalidException)
		// use only the "name" portion, separated at the end
		String justName = isolateName(id);
		Validator.checkResourceId(justName);
		// resource must also NOT end with a separator characters (we fix it)
		if (id.endsWith(Entity.SEPARATOR))
		{
			id = id.substring(0, id.length() - 1);
		}

		// check security
		checkExplicitLock(id);
		unlock(AUTH_RESOURCE_ADD, id);

		// make sure the containing collection exists
		String container = isolateContainingId(id);
		ContentCollection containingCollection = m_storage.getCollection(container);
		if (containingCollection == null)
		{
			// make any missing collections
			generateCollections(container);

			// try again
			containingCollection = m_storage.getCollection(container);
			if (containingCollection == null) throw new InconsistentException(id);
		}

		// reserve the resource in storage - it will fail if the id is in use
		BaseResourceEdit edit = (BaseResourceEdit) m_storage.putResource(id);
		if (edit == null)
		{
			throw new IdUsedException(id);
		}

		// add live properties
		addLiveResourceProperties(edit);

		// track event
		edit.setEvent(EVENT_RESOURCE_ADD);

		return edit;

	} // addResource

	/**
	 * Create a new resource with the given resource name used as a resource id within the specified collection or (if that id is already in use) with a resource id based on a variation on the name to achieve a unique id, provided a unique id can be found
	 * before a limit is reached on the number of attempts to achieve uniqueness.  Used to create a group-aware resource.
	 * 
	 * @param name
	 *        The name of the new resource (such as a filename).
	 * @param collectionId
	 *        The id of the collection to which the resource should be added.
	 * @param limit
	 *        The maximum number of attempts at finding a unique id based on the given name.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A ResourceProperties object with the properties to add to the new resource.
	 * @param groups
	 *        A collection (String) of references to Group objects representing the site subgroups that should have access to this entity.
	 *        May be empty to indicate access is not limited to a group or groups.
	 * @param priority
	 *        The notification priority for this commit.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection.
	 * @exception IdUniquenessException
	 *            if a unique resource id cannot be found before the limit on the number of attempts is reached.
	 * @exception IdLengthException
	 *            if the resource id exceeds the maximum number of characters for a valid resource id.
	 * @exception IdInvalidException
	 *            if the resource id is invalid.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addResource(String name, String collectionId, int limit, String type, byte[] content,
			ResourceProperties properties, Collection groups, int priority) 
	throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, 
	InconsistentException, OverQuotaException, ServerOverloadException
	{
		try
		{
			collectionId = collectionId.trim();
			name = Validator.escapeResourceName(name.trim());
			checkCollection(collectionId);
		}
		catch (IdUnusedException e)
		{
			throw new InconsistentException(collectionId);
		}
		catch (TypeException e)
		{
			throw new InconsistentException(collectionId);
		}

		String id = collectionId + name;
		id = (String) fixTypeAndId(id, type).get("id");
		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new IdLengthException(id);
		}

		ContentResourceEdit edit = null;

		try
		{
			edit = addResource(id);
			edit.setContentType(type);
			edit.setContent(content);
			addProperties(edit.getPropertiesEdit(), properties);
			if(groups == null || groups.isEmpty())
			{
				// access is inherited (the default)
			}
			else
			{
				edit.setGroupAccess(groups);
				// TODO: Need to deal with failure here
			}

			try
			{
				// commit the change
				commitResource(edit, priority);
			}
			catch(OverQuotaException e)
			{
				M_log.debug("OverQuotaException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
			catch(ServerOverloadException e)
			{
				M_log.debug("ServerOverloadException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					M_log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
		}
		catch (IdUsedException e)
		{
			try
			{
				checkResource(id);
			}
			catch (IdUnusedException inner_e)
			{
				// TODO: What does this condition actually represent? What exception should be thrown?
				throw new IdUniquenessException(id);
			}
			catch (TypeException inner_e)
			{
				throw new InconsistentException(id);
			}

			SortedSet siblings = new TreeSet();
			try
			{
				ContentCollection collection = findCollection(collectionId);
				siblings.addAll(collection.getMembers());
			}
			catch (TypeException inner_e)
			{
				throw new InconsistentException(collectionId);
			}

			int index = name.lastIndexOf(".");
			String base = name;
			String ext = "";
			if (index > 0 && !"Url".equalsIgnoreCase(type))
			{
				base = name.substring(0, index);
				ext = name.substring(index);
			}
			boolean trying = true;
			int attempts = 1;
			while (trying) // see end of loop for condition that enforces attempts <= limit)
			{
				String new_id = collectionId + base + "-" + attempts + ext;
				if (new_id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
				{
					throw new IdLengthException(new_id);
				}
				if (!siblings.contains(new_id))
				{
					try
					{
						edit = addResource(new_id);
						edit.setContentType(type);
						edit.setContent(content);
						if(groups == null || groups.isEmpty())
						{
							// access is inherited (the default)
						}
						else
						{
							edit.setGroupAccess(groups);
							// TODO: Need to deal with failure here
						}

						addProperties(edit.getPropertiesEdit(), properties);
						// commit the change
						commitResource(edit, priority);

						trying = false;
					}
					catch (IdUsedException ignore)
					{
						// try again
					}
				}
				attempts++;
				if (attempts > limit)
				{
					throw new IdUniquenessException(new_id);
				}
			}
		}
		return edit;

	}

	/**
	 * check permissions for addAttachmentResource().
	 * 
	 * @return true if the user is allowed to addAttachmentResource(), false if not.
	 */
	public boolean allowAddAttachmentResource()
	{
		return unlockCheck(AUTH_RESOURCE_ADD, ATTACHMENTS_COLLECTION);

	} // allowAddAttachmentResource

	/**
	 * Check whether a resource id or collection id references an entity in the attachments collection. This method makes no guarantees that a resource actually exists with this id.
	 * 
	 * @param id
	 *        Assumed to be a valid resource id or collection id.
	 * @return true if the id (assuming it is a valid id for an existing resource or collection) references an entity in the hidden attachments area created through one of this class's addAttachmentResource methods.
	 */
	public boolean isAttachmentResource(String id)
	{
		// TODO: Should we check whether this is a valid resource id?
		return id.startsWith(ATTACHMENTS_COLLECTION);
	}

	/**
	 * Create a new resource as an attachment to some other resource in the system. The new resource will be placed into a newly created collecion in the attachment collection, with an auto-generated id, and given the specified resource name within this
	 * collection.
	 * 
	 * @param name
	 *        The name of the new resource, i.e. a partial id relative to the collection where it will live.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A ResourceProperties object with the properties to add to the new resource.
	 * @exception IdUsedException
	 *            if the resource name is already in use (not likely, as the containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *            if the resource name is invalid.
	 * @exception InconsistentException
	 *            if the containing collection (or it's containing collection...) does not exist.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection, or add a member to a collection.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addAttachmentResource(String name, String type, byte[] content, ResourceProperties properties)
	throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException,
	ServerOverloadException
	{
		// make sure the name is valid
		Validator.checkResourceId(name);

		// resource must also NOT end with a separator characters (we fix it)
		if (name.endsWith(Entity.SEPARATOR))
		{
			name = name.substring(0, name.length() - 1);
		}

		// form a name based on the attachments collection, a unique folder id, and the given name
		String collection = ATTACHMENTS_COLLECTION + IdManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;

		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new RuntimeException(ID_LENGTH_EXCEPTION);
		}

		// add this collection
		ContentCollectionEdit edit = addCollection(collection);
		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		commitCollection(edit);

		// and add the resource
		return addResource(id, type, content, properties, new ArrayList(), NotificationService.NOTI_NONE);

	} // addAttachmentResource

	/**
	 * Create a new resource as an attachment to some other resource in the system. The new resource will be placed into a newly created collecion in the attachment collection, with an auto-generated id, and given the specified resource name within this
	 * collection.
	 * 
	 * @param name
	 *        The name of the new resource, i.e. a partial id relative to the collection where it will live.
	 * @param site
	 *        The string identifier for the site where the attachment is being added.
	 * @param tool
	 *        The display-name for the tool through which the attachment is being added within the site's attachments collection.
	 * @param type
	 *        The mime type string of the resource.
	 * @param content
	 *        An array containing the bytes of the resource's content.
	 * @param properties
	 *        A ResourceProperties object with the properties to add to the new resource.
	 * @exception IdUsedException
	 *            if the resource name is already in use (not likely, as the containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *            if the resource name is invalid.
	 * @exception InconsistentException
	 *            if the containing collection (or it's containing collection...) does not exist.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection, or add a member to a collection.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource addAttachmentResource(String name, String site, String tool, String type, byte[] content,
			ResourceProperties properties) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException,
			OverQuotaException, ServerOverloadException
			{
		// ignore site if it is not valid
		if (site == null || site.trim().equals(""))
		{
			return addAttachmentResource(name, type, content, properties);
		}
		site = site.trim();
		String siteId = Validator.escapeResourceName(site);

		// if tool is not valid, use "_anon_"
		if (tool == null || tool.trim().equals(""))
		{
			tool = "_anon_";
		}
		tool = tool.trim();
		String toolId = Validator.escapeResourceName(tool);

		// make sure the name is valid
		Validator.checkResourceId(name);

		// resource must also NOT end with a separator characters (we fix it)
		if (name.endsWith(Entity.SEPARATOR))
		{
			name = name.substring(0, name.length() - 1);
		}

		String siteCollection = ATTACHMENTS_COLLECTION + siteId + Entity.SEPARATOR;
		try
		{
			checkCollection(siteCollection);
		}
		catch (Exception e)
		{
			// add this collection
			ContentCollectionEdit siteEdit = addCollection(siteCollection);
			try
			{
				String siteTitle = m_siteService.getSite(site).getTitle();
				siteEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, siteTitle);
			}
			catch (Exception e1)
			{
				siteEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, site);
			}
			commitCollection(siteEdit);
		}

		String toolCollection = siteCollection + toolId + Entity.SEPARATOR;
		try
		{
			checkCollection(toolCollection);
		}
		catch (Exception e)
		{
			// add this collection
			ContentCollectionEdit toolEdit = addCollection(toolCollection);
			toolEdit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, tool);
			commitCollection(toolEdit);
		}

		// form a name based on the attachments collection, a unique folder id, and the given name
		String collection = toolCollection + IdManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;

		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new RuntimeException(ID_LENGTH_EXCEPTION);
		}

		// add this collection
		ContentCollectionEdit edit = addCollection(collection);
		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		commitCollection(edit);

		// and add the resource
		return addResource(id, type, content, properties, new ArrayList(), NotificationService.NOTI_NONE);

			} // addAttachmentResource

	/**
	 * Create a new resource as an attachment to some other resource in the system, locked for update. Must commitResource() to make official, or cancelResource() when done! The new resource will be placed into a newly created collecion in the attachment
	 * collection, with an auto-generated id, and given the specified resource name within this collection.
	 * 
	 * @param name
	 *        The name of the new resource, i.e. a partial id relative to the collection where it will live.
	 * @exception IdUsedException
	 *            if the resource name is already in use (not likely, as the containing collection is auto-generated!)
	 * @exception IdInvalidException
	 *            if the resource name is invalid.
	 * @exception InconsistentException
	 *            if the containing collection (or it's containing collection...) does not exist.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection, or add a member to a collection.
	 * @return a new ContentResource object.
	 */
	public ContentResourceEdit addAttachmentResource(String name) throws IdInvalidException, InconsistentException,
	IdUsedException, PermissionException
	{
		// make sure the name is valid
		Validator.checkResourceId(name);

		// resource must also NOT end with a separator characters (we fix it)
		if (name.endsWith(Entity.SEPARATOR))
		{
			name = name.substring(0, name.length() - 1);
		}

		// form a name based on the attachments collection, a unique folder id, and the given name
		String collection = ATTACHMENTS_COLLECTION + IdManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;

		// add this collection
		ContentCollectionEdit edit = addCollection(collection);
		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		commitCollection(edit);

		return addResource(id);

	} // addAttachmentResource

	/**
	 * check permissions for updateResource().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to updateResource(id), false if not.
	 */
	public boolean allowUpdateResource(String id)
	{
		return allowUpdate(id);

	} // allowUpdateResource

	/**
	 * Update the body and or content type of an existing resource with the given resource id.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @param type
	 *        The mime type string of the resource (if null, no change).
	 * @param content
	 *        An array containing the bytes of the resource's content (if null, no change).
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource to the containing collection or write the resource.
	 * @exception IdUnusedException
	 *            if the resource id is not defined.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object.
	 */
	public ContentResource updateResource(String id, String type, byte[] content) throws PermissionException, IdUnusedException,
	TypeException, InUseException, OverQuotaException, ServerOverloadException
	{
		// find a resource that is this resource
		ContentResourceEdit edit = editResource(id);

		edit.setContentType(type);
		edit.setContent(content);

		// commit the change
		commitResource(edit, NotificationService.NOTI_NONE);

		return edit;

	} // updateResource

	/**
	 * Access the resource with this resource id, locked for update. For non-collection resources only. Must commitEdit() to make official, or cancelEdit() when done! The resource content and properties are accessible from the returned Resource object.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read the resource or read through any containing collection.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @return the ContentResource object found.
	 */
	public ContentResourceEdit editResource(String id) throws PermissionException, IdUnusedException, TypeException, InUseException
	{
		// check security (throws if not permitted)
		checkExplicitLock(id);

		// check security 
		if ( ! allowUpdateResource(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_WRITE_ANY, getReference(id));

		// check for existance
		if (!m_storage.checkResource(id))
		{
			throw new IdUnusedException(id);
		}

		// ignore the cache - get the collection with a lock from the info store
		BaseResourceEdit resource = (BaseResourceEdit) m_storage.editResource(id);
		if (resource == null) throw new InUseException(id);

		resource.setEvent(EVENT_RESOURCE_WRITE);

		ThreadLocalManager.set(String.valueOf(resource), resource);

		return resource;

	} // editResource

	/**
	 * Access the resource with this resource id, locked for update. For non-collection resources only. Must commitEdit() to make official, or cancelEdit() when done! The resource content and properties are accessible from the returned Resource object.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read the resource or read through any containing collection.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @return the ContentResource object found.
	 */
	protected ContentResourceEdit editResourceForDelete(String id) throws PermissionException, IdUnusedException, TypeException, InUseException
	{
		// check security (throws if not permitted)
		checkExplicitLock(id);

		// check security 
		if ( ! allowRemoveResource(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id));

		// check for existance
		if (!m_storage.checkResource(id))
		{
			throw new IdUnusedException(id);
		}

		// ignore the cache - get the collection with a lock from the info store
		BaseResourceEdit resource = (BaseResourceEdit) m_storage.editResource(id);
		if (resource == null) throw new InUseException(id);

		resource.setEvent(EVENT_RESOURCE_REMOVE);

		ThreadLocalManager.set(String.valueOf(resource), resource);

		return resource;

	} // editResourceForDelete

	/**
	 * check permissions for getResource().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to getResource(id), false if not.
	 */
	public boolean allowGetResource(String id)
	{
		return unlockCheck(AUTH_RESOURCE_READ, id);

	} // allowGetResource

	/**
	 * Check access to the resource with this local resource id. For non-collection resources only.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read the resource or read through any containing collection.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 */
	public void checkResource(String id) throws PermissionException, IdUnusedException, TypeException
	{
		// check security
		unlock(AUTH_RESOURCE_READ, id);

		ContentResource resource = findResource(id);
		if (resource == null) throw new IdUnusedException(id);

	} // checkResource

	/**
	 * Access the resource with this resource id. For non-collection resources only. The resource content and properties are accessible from the returned Resource object.
	 * 
	 * @param id
	 *        The resource id.
	 * @exception PermissionException
	 *            if the user does not have permissions to read the resource or read through any containing collection.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @return the ContentResource object found.
	 */
	public ContentResource getResource(String id) throws PermissionException, IdUnusedException, TypeException
	{
		// check security
		unlock(AUTH_RESOURCE_READ, id);

		ContentResource resource = findResource(id);
		if (resource == null) throw new IdUnusedException(id);

		// track event
		// EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_READ, resource.getReference(), false));

		return resource;

	} // getResource

	/**
	 * Access the collection with this local resource id, locked for update. Must commitCollection() to make official, or cancelCollection() when done! The collection internal members and properties are accessible from the returned Collection object.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @exception IdUnusedException
	 *            if the id does not exist.
	 * @exception TypeException
	 *            if the resource exists but is not a collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to see this collection (or read through containing collections).
	 * @exception InUseException
	 *            if the Collection is locked by someone else.
	 * @return The ContentCollection object found.
	 */
	public ContentCollectionEdit editCollection(String id) throws IdUnusedException, TypeException, PermissionException,
	InUseException
	{
		checkExplicitLock(id);

		// check security 
		if ( ! allowUpdateCollection(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_WRITE_ANY, getReference(id));

		// check for existance
		if (!m_storage.checkCollection(id))
		{
			throw new IdUnusedException(id);
		}

		// ignore the cache - get the collection with a lock from the info store
		BaseCollectionEdit collection = (BaseCollectionEdit) m_storage.editCollection(id);
		if (collection == null) throw new InUseException(id);

		collection.setEvent(EVENT_RESOURCE_WRITE);

		ThreadLocalManager.set(String.valueOf(collection), collection);

		return collection;

	} // editCollection

	/**
	 * Access the resource with this resource id. For non-collection resources only. Internal find that doesn't do security or event tracking The resource content and properties are accessible from the returned Resource object.
	 * 
	 * @param id
	 *        The resource id.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @return the ContentResource object found, or null if there's a problem.
	 */
	protected ContentResource findResource(String id) throws TypeException
	{
		ContentResource resource = null;
		try
		{
			resource = (ContentResource) ThreadLocalManager.get("findResource@" + id);
		}
		catch(ClassCastException e)
		{
			throw new TypeException(id);
		}

		if(resource == null)
		{
			resource = m_storage.getResource(id);

			if(resource != null)
			{
				ThreadLocalManager.set("findResource@" + id, resource); 	// new BaseResourceEdit(resource));
			}
		}
		else
		{
			resource = new BaseResourceEdit(resource);
		}

		//		// if not caching
		//		if ((!m_caching) || (m_cache == null) || (m_cache.disabled()))
		//		{
		//			// TODO: current service caching
		//			resource = m_storage.getResource(id);
		//		}
		//
		//		else
		//		{
		//			// if we have it cached, use it (hit or miss)
		//			String key = getReference(id);
		//			if (m_cache.containsKey(key))
		//			{
		//				Object o = m_cache.get(key);
		//				if ((o != null) && (!(o instanceof ContentResource))) throw new TypeException(id);
		//
		//				resource = (ContentResource) o;
		//			}
		//
		//			// if not in the cache, see if we have it in our info store
		//			else
		//			{
		//				resource = m_storage.getResource(id);
		//
		//				// cache it (hit or miss)
		//				m_cache.put(key, resource);
		//			}
		//		}


		return resource;

	} // findResource

	/**
	 * check permissions for removeResource().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to removeResource(id), false if not.
	 */
	public boolean allowRemoveResource(String id)
	{
		// check security
		boolean isAllowed = allowRemove(id);

		if(isAllowed)
		{
			try
			{
				checkExplicitLock(id);
			}
			catch(PermissionException e)
			{
				isAllowed = false;
			}
		}

		return isAllowed;

	} // allowRemoveResource

	/**
	 * Remove a resource. For non-collection resources only.
	 * 
	 * @param id
	 *        The resource id.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 */
	public void removeResource(String id) throws PermissionException, IdUnusedException, TypeException, InUseException
	{
		BaseResourceEdit edit = (BaseResourceEdit) editResourceForDelete(id);
		removeResource(edit);

	} // removeResource

	/**
	 * Remove a resource that is locked for update.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to remove.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 */
	public void removeResource(ContentResourceEdit edit) throws PermissionException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("removeResource(): closed ContentResourceEdit", e);
			return;
		}

		String id = edit.getId();

		// check security (throws if not permitted)
		checkExplicitLock(id);
		if ( ! allowRemoveResource(edit.getId()) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, edit.getReference());


		// htripath -store the metadata information into a delete table
		// assumed uuid is not null as checkExplicitLock(id) throws exception when null
		String uuid = this.getUuid(id);
		String userId = SessionManager.getCurrentSessionUserId().trim();
		addResourceToDeleteTable(edit, uuid, userId);

		// complete the edit
		m_storage.removeResource(edit);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		if(! readyToUseFilesizeColumn())
		{
			removeSizeCache(edit);
		}

		((BaseResourceEdit) edit).setRemoved();

		// remove old version of this edit from thread-local cache
		ThreadLocalManager.set("findResource@" + edit.getId(), null);

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(edit.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			M_log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
			M_log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + ignore);
		}

		// track it (no notification)
		String ref = edit.getReference(null);
		EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, ref, true,
				NotificationService.NOTI_NONE));
		EventTrackingService.cancelDelays(ref);

	} // removeResource


	/**
	 * Store the resource in a separate delete table
	 * 
	 * @param edit
	 * @param uuid
	 * @param userId
	 * @exception PermissionException
	 * @exception ServerOverloadException
	 *            if server is configured to save resource body in filesystem and attempt to read from filesystem fails.
	 */
	public void addResourceToDeleteTable(ContentResourceEdit edit, String uuid, String userId) throws PermissionException
	{
		String id = edit.getId();
		String content_type = edit.getContentType();
		byte[] content = null;
		try
		{
			content = edit.getContent();
		}
		catch (ServerOverloadException e)
		{
			String this_method = this + ".addResourceToDeleteTable()";
			M_log.warn("\n\n" + this_method + "\n" + this_method + ": Unable to access file in server filesystem\n" + this_method
					+ ": May be orphaned file: " + id + "\n" + this_method + "\n\n");
		}
		ResourceProperties properties = edit.getProperties();

		ContentResource newResource = addDeleteResource(id, content_type, content, properties, uuid, userId,
				NotificationService.NOTI_OPTIONAL);
	}

	public ContentResource addDeleteResource(String id, String type, byte[] content, ResourceProperties properties, String uuid,
			String userId, int priority) throws PermissionException
			{
		id = (String) fixTypeAndId(id, type).get("id");
		// resource must also NOT end with a separator characters (fix it)
		if (id.endsWith(Entity.SEPARATOR))
		{
			id = id.substring(0, id.length() - 1);
		}
		// check security-unlock to add record
		unlock(AUTH_RESOURCE_ADD, id);

		// reserve the resource in storage - it will fail if the id is in use
		BaseResourceEdit edit = (BaseResourceEdit) m_storage.putDeleteResource(id, uuid, userId);
		// add live properties-do we need this? - done to have uniformity with main table
		if (edit != null)
		{
			addLiveResourceProperties(edit);
		}
		// track event - do we need this? no harm to keep track
		edit.setEvent(EVENT_RESOURCE_ADD);

		edit.setContentType(type);
		if (content != null)
		{
			edit.setContent(content);
		}
		addProperties(edit.getPropertiesEdit(), properties);

		// complete the edit - update xml which contains properties xml and store the file content
		m_storage.commitDeleteResource(edit, uuid);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		return edit;

			} // addDeleteResource

	/**
	 * check permissions for rename(). Note: for just this collection, not the members on down.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @return true if the user is allowed to rename(id), false if not.
	 */
	public boolean allowRename(String id, String new_id)
	{
		M_log.warn("allowRename(" + id + ") - Rename not implemented");
		return false;

		// return unlockCheck(AUTH_RESOURCE_ADD, new_id) &&
		// unlockCheck(AUTH_RESOURCE_REMOVE, id);

	} // allowRename

	/**
	 * Rename a collection or resource.
	 * 
	 * @param id
	 *        The id of the collection.
	 * @param new_id
	 *        The desired id of the collection.
	 * @return The full id of the resource after the rename is completed.
	 * @exception IdUnusedException
	 *            if the id does not exist.
	 * @exception TypeException
	 *            if the resource exists but is not a collection or resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to rename
	 * @exception InUseException
	 *            if the id or a contained member is locked by someone else. collections, or remove any members of the collection.
	 * @exception IdUsedException
	 *            if copied item is a collection and the new id is already in use or if the copied item is not a collection and a unique id cannot be found in some arbitrary number of attempts (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 */
	public String rename(String id, String new_id) throws IdUnusedException, TypeException, PermissionException, InUseException,
	OverQuotaException, InconsistentException, IdUsedException, ServerOverloadException
	{
		// Note - this could be implemented in this base class using a copy and a delete
		// and then overridden in those derived classes which can support
		// a direct rename operation.

		// check security for remove resource (own or any)
		if ( ! allowRemove(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id));

		// check security for read resource
		unlock(AUTH_RESOURCE_READ, id);

		// check security for add resource
		unlock(AUTH_RESOURCE_ADD, new_id);

		boolean isCollection = false;
		boolean isRootCollection = false;
		ContentResourceEdit thisResource = null;
		ContentCollectionEdit thisCollection = null;

		if (M_log.isDebugEnabled()) M_log.debug("copy(" + id + "," + new_id + ")");

		if (m_storage.checkCollection(id))
		{
			isCollection = true;
			// find the collection
			thisCollection = editCollection(id);
			if (isRootCollection(id))
			{
				cancelCollection(thisCollection);
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), null, null);
			}
		}
		else
		{
			thisResource = editResource(id);
		}

		if (thisResource == null && thisCollection == null)
		{
			throw new IdUnusedException(id);
		}

		if (isCollection)
		{
			new_id = copyCollection(thisCollection, new_id);
			removeCollection(thisCollection);
		}
		else
		{
			new_id = copyResource(thisResource, new_id);
			removeResource(thisResource);
		}
		return new_id;

	} // rename

	/**
	 * check permissions for copy().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @param new_id
	 *        The desired id of the new resource.
	 * @return true if the user is allowed to copy(id,new_id), false if not.
	 */
	public boolean allowCopy(String id, String new_id)
	{
		return unlockCheck(AUTH_RESOURCE_ADD, new_id) && unlockCheck(AUTH_RESOURCE_READ, id);
	}

	/**
	 * Copy a collection or resource from one location to another. Creates a new collection with an id similar to new_folder_id and recursively copies all nested collections and resources within thisCollection to the new collection.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @param folder_id
	 *        The id of the folder in which the copy should be created.
	 * @return The full id of the new copy of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception IdLengthException
	 *            if the new id of the copied item (or any nested item) is longer than the maximum length of an id.
	 * @exception InconsistentException
	 *            if the destination folder (folder_id) is contained within the source folder (id).
	 * @exception IdUsedException
	 *            if a unique resource id cannot be found after some arbitrary number of attempts (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 */
	public String copyIntoFolder(String id, String folder_id) throws PermissionException, IdUnusedException, TypeException,
	InUseException, IdLengthException, IdUniquenessException, OverQuotaException, InconsistentException, IdUsedException,
	ServerOverloadException
	{
		if (folder_id.startsWith(id))
		{
			throw new InconsistentException(id + " is contained within " + folder_id);
		}
		String new_id = newName(id, folder_id);
		if (new_id.length() >= MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new IdLengthException(new_id);
		}

		// Should use copyIntoFolder if possible
		boolean isCollection = false;
		boolean isRootCollection = false;
		ContentResource thisResource = null;

		if (M_log.isDebugEnabled()) M_log.debug("copy(" + id + "," + new_id + ")");

		// find the collection
		ContentCollection thisCollection = null;
		try
		{
			thisCollection = getCollection(id);
		}
		catch (TypeException e)
		{
			thisCollection = null;
        }
        catch (IdUnusedException e)
        {
            thisCollection = null;
        }
        catch (PermissionException e)
        {
            return null;
        }
		if (thisCollection == null)
		{
            try
            {
                thisResource = getResource(id);
            }
            catch (PermissionException e)
            {
                return null;
            }
        }
		else
		{
			isCollection = true;
			if (isRootCollection(id))
			{
				throw new PermissionException(null, null, null);
			}
		}

		if (thisResource == null && thisCollection == null)
		{
			throw new IdUnusedException(id);
		}

		if (isCollection)
		{
                        new_id = deepcopyCollection(thisCollection, new_id);
		}
		else
		{
		        new_id = copyResource(thisResource, new_id);
		}
		return new_id;
	}

	/**
	 * Calculate a candidate for a resource id for a resource being copied/moved into a new folder.
	 * 
	 * @param id
	 * @param folder_id
	 * @exception PermissionException
	 *            if the user does not have permissions to read the properties for the existing resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 */
	protected String newName(String id, String folder_id) throws PermissionException, IdUnusedException
	{
		String filename = isolateName(id);
		if (filename == null || filename.length() == 0)
		{
			ResourceProperties props = getProperties(id);
			filename = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		filename = Validator.escapeResourceName(filename);
		if(! folder_id.endsWith(Entity.SEPARATOR))
		{
			folder_id += Entity.SEPARATOR;
		}

		return folder_id + filename;
	}

	/**
	 * Move a resource or collection to a (different) folder. This may be accomplished by renaming the resource or by recursively renaming the collection and all enclosed members (no matter how deep) to effectively change their locations. Alternatively,
	 * it may be accomplished by copying the resource and recursively copying collections from their existing collection to the new collection and ultimately deleting the original resource(s) and/or collections(s).
	 * 
	 * @param id
	 *        The id of the resource or collection to be moved.
	 * @param folder_id
	 *        The id of the folder to which the resource should be moved.
	 * @return The full id of the resource after the move is completed.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception InconsistentException
	 *            if the destination folder (folder_id) is contained within the source folder (id).
	 * @exception IdUsedException
	 *            if a unique resource id cannot be found after some arbitrary number of attempts (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 */
	public String moveIntoFolder(String id, String folder_id) throws PermissionException, IdUnusedException, TypeException,
	InUseException, OverQuotaException, IdUsedException, InconsistentException, ServerOverloadException
	{
		if (folder_id.startsWith(id))
		{
			throw new InconsistentException(id + " is contained within " + folder_id);
		}
		String new_id = newName(id, folder_id);

		// check security for delete existing resource (any or own)
		if ( ! allowRemove(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id));

		// check security for read existing resource
		unlock(AUTH_RESOURCE_READ, id);

		// check security for add new resource
		unlock(AUTH_RESOURCE_ADD, new_id);

		boolean isCollection = false;
		boolean isRootCollection = false;
		ContentResourceEdit thisResource = null;
		ContentCollectionEdit thisCollection = null;

		if (M_log.isDebugEnabled()) M_log.debug("moveIntoFolder(" + id + "," + new_id + ")");

		if (m_storage.checkCollection(id))
		{
			isCollection = true;
			// find the collection
			thisCollection = editCollection(id);
			if (isRootCollection(id))
			{
				cancelCollection(thisCollection);
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), null, null);
			}
		}
		else
		{
			thisResource = editResource(id);
		}

		if (thisResource == null && thisCollection == null)
		{
			throw new IdUnusedException(id);
		}

		if (isCollection)
		{
			new_id = moveCollection(thisCollection, new_id);
		}
		else
		{
			new_id = moveResource(thisResource, new_id);
		}
		return new_id;

	} // moveIntoFolder

	/**
	 * Move a collection to a new folder. Moves the existing collection or creates a new collection with an id similar to the new_folder_id (in which case the original collection is removed) and recursively moves all nested collections and resources
	 * within thisCollection to the new collection. When finished, thisCollection no longer exists, but the collection identified by the return value has the same structure and all of the members the original had (or copies of them).
	 * 
	 * @param thisCollection
	 *        The collection to be copied
	 * @param new_folder_id
	 *        The desired id of the collection after it is moved.
	 * @return The full id of the moved collection.
	 * @exception PermissionException
	 *            if the user does not have permissions to perform the operations
	 * @exception IdUnusedException
	 *            if the collection id is not found.
	 * @exception TypeException
	 *            if the resource is not a collection.
	 * @exception InUseException
	 *            if the collection is locked by someone else.
	 * @exception IdUsedException
	 *            if a unique resource id cannot be found after some arbitrary number of attempts to find a unique variation of the new_id (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to save content bodies in the server's filesystem and an error occurs trying to access the filesystem.
	 */
	protected String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) throws PermissionException,
	IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
	{
		String name = isolateName(new_folder_id);

		ResourceProperties properties = thisCollection.getProperties();
		ResourcePropertiesEdit newProps = duplicateResourceProperties(properties, thisCollection.getId());
		// newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		String displayName = newProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

		if (displayName == null && name != null)
		{
			newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
			displayName = name;
		}

		if (M_log.isDebugEnabled()) M_log.debug("copyCollection adding colletion=" + new_folder_id + " name=" + name);

		String base_id = new_folder_id + "-";
		boolean still_trying = true;
		int attempt = 0;
		try
		{
			while (still_trying && attempt < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
			{
				try
				{
					ContentCollection newCollection = addCollection(new_folder_id, newProps);

					// use the creator and creation-date of the original instead of the copy
					BaseCollectionEdit collection = (BaseCollectionEdit) m_storage.editCollection(newCollection.getId());
					ResourcePropertiesEdit props = collection.getPropertiesEdit();
					String creator = properties.getProperty(ResourceProperties.PROP_CREATOR);
					if (creator != null && !creator.trim().equals(""))
					{
						props.addProperty(ResourceProperties.PROP_CREATOR, creator);
					}
					String created = properties.getProperty(ResourceProperties.PROP_CREATION_DATE);
					if (created != null)
					{
						props.addProperty(ResourceProperties.PROP_CREATION_DATE, created);
					}
					m_storage.commitCollection(collection);

					if (M_log.isDebugEnabled()) M_log.debug("moveCollection successful");
					still_trying = false;
				}
				catch (IdUsedException e)
				{
					try
					{
						ContentCollection test_for_exists = getCollection(new_folder_id);
					}
					catch (Exception ee)
					{
						throw e;
					}
					attempt++;
					if (attempt >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
					{
						throw e;
					}
					new_folder_id = base_id + attempt;
					// newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name + "-" + attempt);
					newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, displayName + "-" + attempt);
				}
			}

			List members = thisCollection.getMembers();

			if (M_log.isDebugEnabled()) M_log.debug("moveCollection size=" + members.size());

			Iterator memberIt = members.iterator();
			while (memberIt.hasNext())
			{
				String member_id = (String) memberIt.next();
				moveIntoFolder(member_id, new_folder_id);
			}

			removeCollection(thisCollection);
		}
		catch (InconsistentException e)
		{
			throw new TypeException(new_folder_id);
		}
		catch (IdInvalidException e)
		{
			throw new TypeException(new_folder_id);
		}

		return new_folder_id;

	} // moveCollection

	/**
	 * Move a resource to a new folder. Either creates a new resource with an id similar to the new_folder_id and and removes the original resource, or renames the resource with an id similar to the new id, which effectively moves the resource to a new
	 * location.
	 * 
	 * @param thisResource
	 *        The resource to be copied
	 * @param new_id
	 *        The desired id of the resource after it is moved.
	 * @return The full id of the moved resource (which may be a variation on the new_id to ensure uniqueness within the new folder.
	 * @exception PermissionException
	 *            if the user does not have permissions to perform the operations
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception IdUsedException
	 *            if a unique resource id cannot be found after some arbitrary number of attempts to find a unique variation of the new_id (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to save content bodies in the server's filesystem and an error occurs trying to access the filesystem.
	 */
	protected String moveResource(ContentResourceEdit thisResource, String new_id) throws PermissionException, IdUnusedException,
	TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
	{
		String fileName = isolateName(new_id);
		String folderId = isolateContainingId(new_id);

		ResourceProperties properties = thisResource.getProperties();
		String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName == null && fileName != null)
		{
			displayName = fileName;
		}
		String new_displayName = displayName;

		if (M_log.isDebugEnabled()) M_log.debug("moveResource displayname=" + new_displayName + " fileName=" + fileName);

		String basename = fileName;
		String extension = "";
		int index = fileName.lastIndexOf(".");
		if (index >= 0)
		{
			basename = fileName.substring(0, index);
			extension = fileName.substring(index);
		}

		boolean still_trying = true;
		int attempt = 0;

		while (still_trying && attempt < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
		{
			// copy the resource to the new location
			try
			{
				ContentResourceEdit edit = addResource(new_id);
				edit.setContentType(thisResource.getContentType());
				edit.setContent(thisResource.streamContent());
				edit.setResourceType(thisResource.getResourceType());
				edit.setAvailability(thisResource.isHidden(), thisResource.getReleaseDate(), thisResource.getRetractDate());

				//((BaseResourceEdit) edit).m_filePath = ((BaseResourceEdit) thisResource).m_filePath;
				//((BaseResourceEdit) thisResource).m_filePath = null;
				//				Collection groups = thisResource.getGroups();
				//				if(groups == null || groups.isEmpty())
				//				{
				//					// do nothing
				//				}
				//				else
				//				{
				//					edit.setGroupAccess(groups);
				//				}

				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				Iterator<String> nameIt = properties.getPropertyNames();
				while(nameIt.hasNext())
				{
					String name = nameIt.next();
					props.addProperty(name, properties.getProperty(name));
				}
				//addProperties(props, properties);
				//				String creator = properties.getProperty(ResourceProperties.PROP_CREATOR);
				//				if (creator != null && !creator.trim().equals(""))
				//				{
				//					props.addProperty(ResourceProperties.PROP_CREATOR, creator);
				//				}
				//				String created = properties.getProperty(ResourceProperties.PROP_CREATION_DATE);
				//				if (created != null)
				//				{
				//					props.addProperty(ResourceProperties.PROP_CREATION_DATE, created);
				//				}
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, new_displayName);

				String oldUuid = getUuid(thisResource.getId());
				setUuidInternal(new_id, oldUuid);

				m_storage.commitResource(edit);
				// close the edit object
				((BaseResourceEdit) edit).closeEdit();

				// track it (no notification)
				String ref = edit.getReference(null);
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_ADD, ref, true,
						NotificationService.NOTI_NONE));

				// TODO - we don't know whether to post a future notification or not 
				postAvailableEvent(edit, ref, NotificationService.NOTI_NONE);

				m_storage.removeResource(thisResource);

				// track it (no notification)
				String thisRef = thisResource.getReference(null);
				EventTrackingService.cancelDelays(thisRef);
				EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, thisRef, true,
						NotificationService.NOTI_NONE));

				if (M_log.isDebugEnabled()) M_log.debug("moveResource successful");
				still_trying = false;
			}
			catch (InconsistentException e)
			{
				throw new TypeException(new_id);
			}
			catch (IdInvalidException e)
			{
				throw new TypeException(new_id);
			}
			catch (IdUsedException e)
			{
				try
				{
					ContentResource test_for_exists = getResource(new_id);
				}
				catch (Exception ee)
				{
					throw e;
				}
				if (attempt >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
				{
					throw e;
				}
				attempt++;
				new_id = folderId + basename + "-" + attempt + extension;
				new_displayName = displayName + " (" + attempt + ")";
			}
		}

		//removeResource(thisResource);

		return new_id;

	} // moveResource

	/**
	 * Copy a resource or collection.
	 * 
	 * @param id
	 *        The id of the resource.
	 * @param new_id
	 *        The desired id of the new resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception IdUsedException
	 *            if copied item is a collection and the new id is already in use or if the copied item is not a collection and a unique id cannot be found in some arbitrary number of attempts (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @see copyIntoFolder(String, String) method (preferred method for invocation from a tool).
	 */
	public String copy(String id, String new_id) throws PermissionException, IdUnusedException, TypeException, InUseException,
	OverQuotaException, IdUsedException, ServerOverloadException
	{
		// Should use copyIntoFolder if possible
		boolean isCollection = false;
		boolean isRootCollection = false;
		ContentResource thisResource = null;

		if (M_log.isDebugEnabled()) M_log.debug("copy(" + id + "," + new_id + ")");

		// find the collection
		ContentCollection thisCollection = findCollection(id);
		if (thisCollection != null)
		{
			isCollection = true;
			if (isRootCollection(id))
			{
				throw new PermissionException(null, null, null);
			}
		}
		else
		{
			thisResource = findResource(id);
		}

		if (thisResource == null && thisCollection == null)
		{
			throw new IdUnusedException(id);
		}

		if (isCollection)
		{
			new_id = copyCollection(thisCollection, new_id);
		}
		else
		{
			new_id = copyResource(thisResource, new_id);
		}
		return new_id;

	}

	/**
	 * Get a duplicate copy of resource properties This copies everything except for the DISPLAYNAME - DISPLAYNAME is only copied if it is different than the file name as derived from the id (path) Note to Chuck - should the add operations check for empty
	 * Display and set it to the file name rather than putting all the code all over the place.
	 */
	private ResourcePropertiesEdit duplicateResourceProperties(ResourceProperties properties, String id)
	{
		ResourcePropertiesEdit resourceProperties = newResourceProperties();

		if (properties == null) return resourceProperties;

		// If there is a distinct display name, we keep it
		// If the display name is the "file name" we pitch it and let the name change
		String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		String resourceName = isolateName(id);
		if (displayName == null) displayName = resourceName;
		if (displayName.length() == 0) displayName = resourceName;

		// loop throuh the properties
		Iterator propertyNames = properties.getPropertyNames();
		while (propertyNames.hasNext())
		{
			String propertyName = (String) propertyNames.next();
			resourceProperties.addProperty(propertyName, properties.getProperty(propertyName));
			/*
			if (!properties.isLiveProperty(propertyName))
			{
				if (propertyName.equals(ResourceProperties.PROP_DISPLAY_NAME))
				{
					if (!displayName.equals(resourceName))
					{
						resourceProperties.addProperty(propertyName, displayName);
					}
				}
				else
				{
					resourceProperties.addProperty(propertyName, properties.getProperty(propertyName));
				} // if-else
			} // if
			 */
		} // while
		return resourceProperties;

	} // duplicateResourceProperties

	/**
	 * Copy a resource.
	 * 
	 * @param thisResource
	 *        The resource to be copied
	 * @param new_id
	 *        The desired id of the new resource.
	 * @return The full id of the new copy of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the resource is a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception OverQuotaException
	 *            if copying the resource would exceed the quota.
	 * @exception IdUsedException
	 *            if a unique id cannot be found in some arbitrary number of attempts (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 */
	public String copyResource(ContentResource resource, String new_id) throws PermissionException, IdUnusedException,
	TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
	{
		String fileName = isolateName(new_id);
		fileName = Validator.escapeResourceName(fileName);
		String folderId = isolateContainingId(new_id);

		ResourceProperties properties = resource.getProperties();
		String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName == null && fileName != null)
		{
			displayName = fileName;
		}
		String new_displayName = displayName;
		if (M_log.isDebugEnabled()) M_log.debug("copyResource displayname=" + new_displayName + " fileName=" + fileName);

		String basename = fileName;
		String extension = "";
		int index = fileName.lastIndexOf(".");
		if (index >= 0)
		{
			basename = fileName.substring(0, index);
			extension = fileName.substring(index);
		}

		boolean still_trying = true;
		int attempt = 0;

		while (still_trying && attempt < MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
		{
			// copy the resource to the new location
			ContentResourceEdit edit = null;
			try
			{
				edit = addResource(new_id);
				edit.setContentType(resource.getContentType());

				// use stream instead of byte array
				// edit.setContent(resource.getContent());
				edit.setContent(resource.streamContent());

				edit.setResourceType(resource.getResourceType());
				ResourcePropertiesEdit newProps = edit.getPropertiesEdit();

				addProperties(newProps, properties);
				newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, new_displayName);
				//				Collection groups = resource.getGroups();
				//				if(groups == null || groups.isEmpty())
				//				{
				//					// do nothing
				//				}
				//				else
				//				{
				//					edit.setGroupAccess(groups);
				//				}
				edit.setAvailability(resource.isHidden(), resource.getReleaseDate(), resource.getRetractDate());

				commitResource(edit,NotificationService.NOTI_NONE);
				// close the edit object
				((BaseResourceEdit) edit).closeEdit();

				if (M_log.isDebugEnabled()) M_log.debug("copyResource successful");
				still_trying = false;
			}
			catch (InconsistentException e)
			{
				throw new TypeException(new_id);
			}
			catch (IdInvalidException e)
			{
				throw new TypeException(new_id);
			}
			catch (IdUsedException e)
			{
				try
				{
					ContentResource test_for_exists = getResource(new_id);
				}
				catch (Exception ee)
				{
					throw e;
				}
				if (attempt >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
				{
					throw e;
				}
				attempt++;
				new_id = folderId + basename + "-" + attempt + extension;
				new_displayName = displayName + " (" + attempt + ")";
				// Could come up with a naming convention to add versions here
			}
		}
		return new_id;

	} // copyResource

	/**
	 * Copy a collection.
	 * 
	 * @param thisCollection
	 *        The collection to be copied
	 * @param new_id
	 *        The desired id of the new collection.
	 * @return The full id of the new copy of the resource.
	 * @exception PermissionException
	 *            if the user does not have permissions to perform the operations
	 * @exception IdUnusedException
	 *            if the collection id is not found.
	 * @exception TypeException
	 *            if the resource is not a collection.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception IdUsedException
	 *            if the new collection id is already in use.
	 */
	public String copyCollection(ContentCollection thisCollection, String new_id) throws PermissionException, IdUnusedException,
	TypeException, InUseException, OverQuotaException, IdUsedException
	{
		List members = thisCollection.getMemberResources();

		if (M_log.isDebugEnabled()) M_log.debug("copyCollection size=" + members.size());

		if (members.size() > 0)
		{
			// recurse to copy everything in the folder?
			throw new PermissionException(null, null, null);
		}

		String name = isolateName(new_id);

		ResourceProperties properties = thisCollection.getProperties();
		ResourcePropertiesEdit newProps = duplicateResourceProperties(properties, thisCollection.getId());
		newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

		if (M_log.isDebugEnabled()) M_log.debug("copyCollection adding colletion=" + new_id + " name=" + name);

		try
		{
			ContentCollection newCollection = addCollection(new_id, newProps);
			if (M_log.isDebugEnabled()) M_log.debug("copyCollection successful");
		}
		catch (InconsistentException e)
		{
			throw new TypeException(new_id);
		}
		catch (IdInvalidException e)
		{
			throw new TypeException(new_id);
		}
		/*
		 * catch (IdUsedException e) // Why is this the case?? { throw new PermissionException(null, null); }
		 */
		return new_id;

	} // copyCollection

	/**
	 * Make a deep copy of a collection. Creates a new collection with an id similar to new_folder_id and recursively copies all nested collections and resources within thisCollection to the new collection.
	 * 
	 * @param thisCollection
	 *        The collection to be copied
	 * @param new_folder_id
	 *        The desired id of the new collection.
	 * @return The full id of the copied collection (which may be a slight variation on the desired id to ensure uniqueness).
	 * @exception PermissionException
	 *            if the user does not have permissions to perform the operations
	 * @exception IdUnusedException
	 *            if the collection id is not found. ???
	 * @exception TypeException
	 *            if the resource is not a collection.
	 * @exception InUseException
	 *            if the collection is locked by someone else.
	 * @exception IdUsedException
	 *            if a unique id cannot be found for the new collection after some arbitrary number of attempts to find a unique variation of the new_folder_id (@see MAXIMUM_ATTEMPTS_FOR_UNIQUENESS).
	 * @exception ServerOverloadException
	 *            if the server is configured to save content bodies in the server's filesystem and an error occurs trying to access the filesystem.
	 */
	protected String deepcopyCollection(ContentCollection thisCollection, String new_folder_id) throws PermissionException,
	IdUnusedException, TypeException, InUseException, IdLengthException, IdUniquenessException, OverQuotaException,
	IdUsedException, ServerOverloadException
	{
		String name = isolateName(new_folder_id);
		ResourceProperties properties = thisCollection.getProperties();
		ResourcePropertiesEdit newProps = duplicateResourceProperties(properties, thisCollection.getId());
		if(newProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME) == null)
		{
			name = Validator.escapeResourceName(name);

			newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
		}

		if (M_log.isDebugEnabled()) M_log.debug("copyCollection adding colletion=" + new_folder_id + " name=" + name);

		String base_id = new_folder_id + "-";
		boolean still_trying = true;
		int attempt = 0;
		ContentCollection newCollection = null;
		try
		{
			try
			{
				newCollection = addCollection(new_folder_id, newProps);

				if (M_log.isDebugEnabled()) M_log.debug("moveCollection successful");
				still_trying = false;
			}
			catch (IdUsedException e)
			{
				try
				{
					checkCollection(new_folder_id);
				}
				catch (Exception ee)
				{
					throw new IdUniquenessException(new_folder_id);
				}
			}
			String containerId = this.isolateContainingId(new_folder_id);
			ContentCollection containingCollection = findCollection(containerId);
			SortedSet siblings = new TreeSet();
			siblings.addAll(containingCollection.getMembers());

			while (still_trying)
			{
				attempt++;
				if (attempt >= MAXIMUM_ATTEMPTS_FOR_UNIQUENESS)
				{
					throw new IdUniquenessException(new_folder_id);
				}
				new_folder_id = base_id + attempt;
				if (!siblings.contains(new_folder_id))
				{
					newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name + "-" + attempt);
					try
					{
						newCollection = addCollection(new_folder_id, newProps);
						still_trying = false;
					}
					catch (IdUsedException inner_e)
					{
						// try again
					}
				}
			}

			List members = thisCollection.getMembers();

			if (M_log.isDebugEnabled()) M_log.debug("moveCollection size=" + members.size());

			Iterator memberIt = members.iterator();
			while (memberIt.hasNext())
			{
				String member_id = (String) memberIt.next();
                		if (isAvailable(member_id)){
                    			copyIntoFolder(member_id, new_folder_id);
			    	}
            }

		}
		catch (InconsistentException e)
		{
			throw new TypeException(new_folder_id);
		}
		catch (IdInvalidException e)
		{
			throw new TypeException(new_folder_id);
		}

		return new_folder_id;

	} // deepcopyCollection

	/**
	 * Commit the changes made, and release the lock. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to commit.
	 * @exception OverQuotaException
	 *            if this would result in being over quota (the edit is then cancled).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @exception PermissionException 
	 * 			 if the user is trying to make a change for which they lack permission.
	 */
	public void commitResource(ContentResourceEdit edit) throws OverQuotaException, ServerOverloadException
	{
		commitResource(edit, NotificationService.NOTI_OPTIONAL);

	} // commitResource

	/**
	 * Commit the changes made, and release the lock. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to commit.
	 * @param priority
	 *        The notification priority of this commit.
	 * @exception OverQuotaException
	 *            if this would result in being over quota (the edit is then cancled).
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 */
	public void commitResource(ContentResourceEdit edit, int priority) throws OverQuotaException, ServerOverloadException
	{

		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("commitResource(): closed ContentResourceEdit", e);
			return;
		}

		// check for over quota.
		if (overQuota(edit))
		{
			cancelResource(edit);
			throw new OverQuotaException(edit.getReference());
		}

		commitResourceEdit(edit, priority);

		if(! readyToUseFilesizeColumn())
		{
			addSizeCache(edit);
		}

	} // commitResource

	/**
	 * Commit the changes made, and release the lock - no quota check. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to commit.
	 * @param priority
	 *        The notification priority of this commit.
	 * @throws PermissionException 
	 */
	protected void commitResourceEdit(ContentResourceEdit edit, int priority) throws ServerOverloadException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("commitResourceEdit(): closed ContentResourceEdit", e);
			return;
		}

		if(this.m_prioritySortEnabled)
		{
			// ((BasicGroupAwareEdit) edit).setPriority();
		}

		// update the properties for update
		addLiveUpdateResourceProperties(edit);

		// complete the edit
		m_storage.commitResource(edit);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		// must remove old version of this edit from thread-local cache
		// so we get new version if we try to retrieve it in same thread
		ThreadLocalManager.set("findResource@" + edit.getId(), null);
		String containerId = isolateContainingId(edit.getId());
		ThreadLocalManager.set("findCollection@" +  containerId, null);
		ThreadLocalManager.set("members@" + containerId, null);
		//ThreadLocalManager.set("getCollections@" + containerId, null);
		ThreadLocalManager.set("getResources@" + containerId, null);

		// only send notifications if the resource is available
		// an 'available' event w/ notification will be sent when the resource becomes available

		String ref = edit.getReference(null);

		// Cancel any previously scheduled delayed available events
		EventTrackingService.cancelDelays(ref, ((BaseResourceEdit) edit).getEvent());

		// Send a notification with the initial event if this is a revise event and the resource is already available
		int immediate_priority = (EVENT_RESOURCE_WRITE.equals(((BaseResourceEdit) edit).getEvent()) && edit.isAvailable()) ? 
				priority : NotificationService.NOTI_NONE;

		EventTrackingService.post(EventTrackingService.newEvent(((BaseResourceEdit) edit).getEvent(),
				ref, true, immediate_priority));

		// Post an available event for now or later
		postAvailableEvent(edit, ref, priority);

	} // commitResourceEdit

	/**
	 * Test a collection of Group object for the specified group reference
	 * @param groups The collection (Group) of groups
	 * @param groupRef The string group reference to find.
	 * @return true if found, false if not.
	 */
	protected boolean groupCollectionContainsRefString(Collection groups, String groupRef)
	{
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			Group group = (Group) i.next();
			if (group.getReference().equals(groupRef)) return true;
		}

		return false;
	}



	/**
	 * Cancel the changes made object, and release the lock. The Object is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to commit.
	 */
	public void cancelResource(ContentResourceEdit edit)
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			M_log.warn("cancelResource(): closed ContentResourceEdit", e);
			return;
		}

		// release the edit lock
		m_storage.cancelResource(edit);

		// if the edit is newly created during an add resource process, remove it from the storage
		if (((BaseResourceEdit) edit).getEvent().equals(EVENT_RESOURCE_ADD))
		{
			m_storage.removeResource(edit);
		}

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

	} // cancelResource

	/**
	 * check permissions for getProperties().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to getProperties(id), false if not.
	 */
	public boolean allowGetProperties(String id)
	{
		return unlockCheck(AUTH_RESOURCE_READ, id);

	} // allowGetProperties

	/**
	 * Access the properties of a resource with this resource id, either collection or resource.
	 * 
	 * @param id
	 *        The resource id.
	 * @exception PermissionException
	 *            if the user does not have permissions to read properties on this object or read through containing collections.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @return the ResourceProperties object for this resource.
	 */
	public ResourceProperties getProperties(String id) throws PermissionException, IdUnusedException
	{
		unlock(AUTH_RESOURCE_READ, id);

		boolean collectionHint = id.endsWith(Entity.SEPARATOR);

		Entity o = null;

		try
		{
			if (collectionHint)
			{
				o = findCollection(id);
			}
			else
			{
				o = findResource(id);
			}
		}
		catch (TypeException ignore)
		{
		}

		// unlikely, but...
		if (o == null) throw new IdUnusedException(id);

		// track event - removed for clarity of the event log -ggolden
		// EventTrackingService.post(EventTrackingService.newEvent(EVENT_PROPERTIES_READ, getReference(id)));

		return o.getProperties();

	} // getProperties

	/**
	 * check permissions for addProperty().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to addProperty(id), false if not.
	 */
	public boolean allowAddProperty(String id)
	{
		boolean isAllowed = allowUpdate(id);
		if(isAllowed)
		{
			try 
			{
				checkExplicitLock(id);
			} 
			catch (PermissionException e) 
			{
				isAllowed = false;
			}
		}

		return isAllowed;

	} // allowAddProperty

	/**
	 * Add / update a property for a resource, either collection or resource.
	 * 
	 * @param id
	 *        The resource id.
	 * @param name
	 *        The properties name to add or update
	 * @param value
	 *        The new value for the property.
	 * @exception PermissionException
	 *            if the user does not have premissions to write properties on this object or read through containing collections.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if any property requested cannot be set (it may be live).
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return the ResourceProperties object for this resource.
	 */
	public ResourceProperties addProperty(String id, String name, String value) throws PermissionException, IdUnusedException,
	TypeException, InUseException, ServerOverloadException
	{
		// check security 
		checkExplicitLock(id);
		if ( ! allowAddProperty(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_WRITE_ANY, getReference(id));

		boolean collectionHint = id.endsWith(Entity.SEPARATOR);
		Edit o = null;
		if (collectionHint)
		{
			o = editCollection(id);
		}
		else
		{
			o = editResource(id);
		}

		// unlikely, but...
		if (o == null) throw new IdUnusedException(id);

		// get the properties
		ResourcePropertiesEdit props = o.getPropertiesEdit();

		// check for TypeException updating live properties
		if (props.isLiveProperty(name)) throw new TypeException(name);

		// add the property
		props.addProperty(name, value);

		// commit the change
		if (o instanceof ContentResourceEdit)
		{
			commitResourceEdit((ContentResourceEdit) o, NotificationService.NOTI_NONE);
		}
		if (o instanceof ContentCollectionEdit)
		{
			commitCollection((ContentCollectionEdit) o);
		}

		return props;

	} // addProperty

	/**
	 * check permissions for removeProperty().
	 * 
	 * @param id
	 *        The id of the new resource.
	 * @return true if the user is allowed to removeProperty(id), false if not.
	 */
	public boolean allowRemoveProperty(String id)
	{
		boolean isAllowed = allowUpdate(id);

		if(isAllowed)
		{
			try
			{
				checkExplicitLock(id);
			}
			catch(PermissionException e)
			{
				isAllowed = false;
			}
		}

		return isAllowed;

	} // allowRemoveProperty

	/**
	 * Remove a property from a resource, either collection or resource.
	 * 
	 * @param id
	 *        The resource id.
	 * @param name
	 *        The property name to be removed from the resource.
	 * @exception PermissionException
	 *            if the user does not have premissions to write properties on this object or read through containing collections.
	 * @exception IdUnusedException
	 *            if the resource id is not found.
	 * @exception TypeException
	 *            if the property named cannot be removed.
	 * @exception InUseException
	 *            if the resource is locked by someone else.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return the ResourceProperties object for this resource.
	 */
	public ResourceProperties removeProperty(String id, String name) throws PermissionException, IdUnusedException, TypeException,
	InUseException, ServerOverloadException
	{
		// check security 
		checkExplicitLock(id);
		if ( ! allowRemoveProperty(id) )
			throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_WRITE_ANY, getReference(id));

		boolean collectionHint = id.endsWith(Entity.SEPARATOR);
		Edit o = null;
		if (collectionHint)
		{
			o = editCollection(id);
		}
		else
		{
			o = editResource(id);
		}

		// unlikely, but...
		if (o == null) throw new IdUnusedException(id);

		// get the properties
		ResourcePropertiesEdit props = o.getPropertiesEdit();

		// check for TypeException updating live properties
		if (props.isLiveProperty(name)) throw new TypeException(name);

		// remove the property
		props.removeProperty(name);

		// commit the change
		if (o instanceof ContentResourceEdit)
		{
			commitResourceEdit((ContentResourceEdit) o, NotificationService.NOTI_NONE);
		}
		if (o instanceof ContentCollectionEdit)
		{
			commitCollection((ContentCollectionEdit) o);
		}

		return props;

	} // removeProperty

	/**
	 * Access the resource URL from a resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return The resource URL.
	 */
	public String getUrl(String id)
	{
		// escape just the is part, not the access point
		return getUrl(id, PROP_ALTERNATE_REFERENCE); // getAccessPoint(false) + Validator.escapeUrl(id);

	} // getUrl

	/**
	 * Access the alternate URL which can be used to access the entity.
	 * 
	 * @param id
	 *        The resource id.
	 * @param rootProperty
	 *        The name of the entity property whose value controls which alternate reference URL is requested. If null, the native 'raw' URL is requested.
	 * @return The resource URL.
	 */
	public String getUrl(String id, String rootProperty)
	{
		// escape just the is part, not the access point
		// return getAccessPoint(false) + Validator.escapeUrl(id);
		return m_serverConfigurationService.getAccessUrl() + getAlternateReferenceRoot(id, rootProperty) + m_relativeAccessPoint
		+ Validator.escapeUrl(convertIdToUserEid(id));

	} // getUrl

	/**
	 * Compute an alternate root for a reference, based on the value of the specified property.
	 * 
	 * @param rootProperty
	 *        The property name.
	 * @return The alternate root, or "" if there is none.
	 */
	protected String getAlternateReferenceRoot(String id, String rootProperty)
	{
		// null means don't do this
		if (rootProperty == null) return "";

		// if id is missing or blank or root, skip as well
		if ((id == null) || id.equals("/") || id.equals("")) return "";

		// find the property - "" if not found
		String alternateRoot = null;
		try
		{
			// TODO: Can this be done without a security check??
			// findResource(id).getProperties().getProperty(...) ??
			alternateRoot = StringUtil.trimToNull(getProperties(id).getProperty(rootProperty));
		}
		catch (PermissionException e)
		{
			// ignore
		}
		catch (IdUnusedException e)
		{
			// ignore
		}
		if (alternateRoot == null) return "";

		// make sure it start with a separator and does not end with one
		if (!alternateRoot.startsWith(Entity.SEPARATOR)) alternateRoot = Entity.SEPARATOR + alternateRoot;
		if (alternateRoot.endsWith(Entity.SEPARATOR))
			alternateRoot = alternateRoot.substring(0, alternateRoot.length() - Entity.SEPARATOR.length());

		return alternateRoot;
	}

	/**
	 * Access the internal reference from a resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return The internal reference from a resource id.
	 */
	public String getReference(String id)
	{
		return getAccessPoint(true) + id;

	} // getReference

	/**
	 * Access the resource id of the collection which contains this collection or resource.
	 * 
	 * @param id
	 *        The resource id (reference, or URL) of the ContentCollection or ContentResource
	 * @return the resource id (reference, or URL, depending on the id parameter) of the collection which contains this resource.
	 */
	public String getContainingCollectionId(String id)
	{
		return isolateContainingId(id);

	} // getContainingCollectionId

	/**
	 * Get the depth of the resource/collection object in the hireachy based on the given collection id
	 * 
	 * @param resourceId
	 *        The Id of the resource/collection object to be tested
	 * @param baseCollectionId
	 *        The Id of the collection as the relative root level
	 * @return the integer value reflecting the relative hierarchy depth of the test resource/collection object based on the given base collection level
	 */
	public int getDepth(String resourceId, String baseCollectionId)
	{
		if (resourceId.indexOf(baseCollectionId) == -1)
		{
			// the resource object is not a member of base collection
			return -1;
		}
		else
		{
			int i = 1;
			// the resource object is a member of base collection
			String s = resourceId.substring(baseCollectionId.length());
			while (s.indexOf(Entity.SEPARATOR) != -1)
			{
				if (s.indexOf(Entity.SEPARATOR) != (s.length() - 1))
				{
					// the resource seperator character is not the last character
					i++;
					s = s.substring(s.indexOf(Entity.SEPARATOR) + 1);
				}
				else
				{
					s = "";
				}
			}
			return i;
		}

	} // getDepth

	/**
	 * Test if this id (reference, or URL) refers to the root collection.
	 * 
	 * @param id
	 *        The resource id (reference, or URL) of a ContentCollection
	 * @return true if this is the root collection
	 */
	public boolean isRootCollection(String id)
	{
		boolean rv = false;

		// test for the root local id
		if (id.equals("/"))
		{
			rv = true;
		}

		// test for the root reference
		else if (id.equals(getAccessPoint(true) + "/"))
		{
			rv = true;
		}

		// test for the root URL
		else if (id.equals(getAccessPoint(false) + "/"))
		{
			rv = true;
		}

		// if (M_log.isDebugEnabled()) M_log.debug("isRootCollection: id: " + id + " rv: " + rv);

		return rv;

	} // isRootCollection

	/**
	 * Construct a stand-alone, not associated with any particular resource, ResourceProperties object.
	 * 
	 * @return The new ResourceProperties object.
	 */
	public ResourcePropertiesEdit newResourceProperties()
	{
		return new BaseResourcePropertiesEdit();

	} // newResourceProperties

	/**
	 * @inheritDoc
	 */
	public Comparator newContentHostingComparator(String property, boolean ascending)
	{
		return new ContentHostingComparator(property, ascending, m_useSmartSort);
	}

	/**
	 * Return a map of Worksite collections roots that the user has access to.
	 * 
	 * @return Map of worksite resource root id (String) to worksite title (String)
	 */
	public Map getCollectionMap()
	{
		// the return map
		Map rv = new HashMap();

		// get the sites the user has access to
		List mySites = m_siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null,
				org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);

		// add in the user's myworkspace site, if we can find it and if the user
		// is not anonymous
		String userId = SessionManager.getCurrentSessionUserId();
		if ( userId != null )
		{
			try
			{
				mySites.add(m_siteService.getSite(m_siteService.getUserSiteId(userId)));
			}
			catch (IdUnusedException e)
			{
			}
		}

		// check each one for dropbox and resources
		for (Iterator i = mySites.iterator(); i.hasNext();)
		{
			Site site = (Site) i.next();

			// test dropbox
			if (site.getToolForCommonId("sakai.dropbox") != null)
			{
				String collectionId = getDropboxCollection(site.getId());
				String title = site.getTitle() + " " + rb.getString("gen.drop");
				rv.put(collectionId, title);
			}

			// test resources
			if (site.getToolForCommonId("sakai.resources") != null)
			{

				String collectionId = getSiteCollection(site.getId());
				String title = site.getTitle() + " " + rb.getString("gen.reso");
				rv.put(collectionId, title);
			}
		}

		return rv;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "content";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/** stream content requests if true, read all into memory and send if false. */
	protected static final boolean STREAM_CONTENT = true;

	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/**
	 * Process the access request for a resource.
	 * 
	 * @param req
	 * @param res
	 * @param ref
	 * @param copyrightAcceptedRefs
	 * @throws PermissionException
	 * @throws IdUnusedException
	 * @throws ServerOverloadException
	 * @throws CopyrightException
	 */
	protected void handleAccessResource(HttpServletRequest req, HttpServletResponse res, Reference ref,
			Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
			EntityAccessOverloadException, EntityCopyrightException
			{
		// we only access resources, not collections
		if (ref.getId().endsWith(Entity.SEPARATOR)) throw new EntityNotDefinedException(ref.getReference());

		// need read permission
		if (!allowGetResource(ref.getId()))
			throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), AUTH_RESOURCE_READ, ref.getReference());

		BaseResourceEdit resource = null;
		try
		{
			resource = (BaseResourceEdit) getResource(ref.getId());
		}
		catch (IdUnusedException e)
		{
			throw new EntityNotDefinedException(e.getId());
		}
		catch (PermissionException e)
		{
			throw new EntityPermissionException(e.getUser(), e.getLock(), e.getResource());
		}
		catch (TypeException e)
		{
			throw new EntityNotDefinedException(ref.getReference());
		}

		// if this entity requires a copyright agreement, and has not yet been set, get one
		if (resource.requiresCopyrightAgreement() && !copyrightAcceptedRefs.contains(resource.getReference()))
		{
			throw new EntityCopyrightException(ref.getReference());
		}

		try
		{
			// changed to int from long because res.setContentLength won't take long param -- JE
			int len = resource.getContentLength();
			String contentType = resource.getContentType();

			// for url content type, encode a redirect to the body URL
			if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
			{
				byte[] content = resource.getContent();
				if ((content == null) || (content.length == 0))
				{
					throw new IdUnusedException(ref.getReference());
				}

				String one = new String(content);
				String two = "";
				for (int i = 0; i < one.length(); i++)
				{
					if (one.charAt(i) == '+')
					{
						two += "%2b";
					}
					else
					{
						two += one.charAt(i);
					}
				}
				res.sendRedirect(two);
			}

			else
			{
				// use the last part, the file name part of the id, for the download file name
				String fileName = Web.encodeFileName( req, Validator.getFileName(ref.getId()) );

				String disposition = null;

				if (Validator.letBrowserInline(contentType))
				{
					disposition = "inline; filename=\"" + fileName + "\"";
				}
				else
				{
					disposition = "attachment; filename=\"" + fileName + "\"";
				}

				// NOTE: Only set the encoding on the content we have to.
				// Files uploaded by the user may have been created with different encodings, such as ISO-8859-1;
				// rather than (sometimes wrongly) saying its UTF-8, let the browser auto-detect the encoding.
				// If the content was created through the WYSIWYG editor, the encoding does need to be set (UTF-8).
				String encoding = resource.getProperties().getProperty(ResourceProperties.PROP_CONTENT_ENCODING);
				if (encoding != null && encoding.length() > 0)
				{
					contentType = contentType + "; charset=" + encoding;
				}

				// stream the content using a small buffer to keep memory managed
				if (STREAM_CONTENT)
				{
					InputStream content = null;
					OutputStream out = null;

					try
					{
						content = resource.streamContent();
						if (content == null)
						{
							throw new IdUnusedException(ref.getReference());
						}

						res.setContentType(contentType);
						res.addHeader("Content-Disposition", disposition);
						res.addHeader("Accept-Ranges", "none");
						res.setContentLength(len);

						// set the buffer of the response to match what we are reading from the request
						if (len < STREAM_BUFFER_SIZE)
						{
							res.setBufferSize(len);
						}
						else
						{
							res.setBufferSize(STREAM_BUFFER_SIZE);
						}

						out = res.getOutputStream();

						// chunk
						byte[] chunk = new byte[STREAM_BUFFER_SIZE];
						int lenRead;
						while ((lenRead = content.read(chunk)) != -1)
						{
							out.write(chunk, 0, lenRead);
						}
					}
					catch (ServerOverloadException e)
					{
						throw e;
					}
					catch (Throwable ignore)
					{
					}
					finally
					{
						// be a good little program and close the stream - freeing up valuable system resources
						if (content != null)
						{
							content.close();
						}

						if (out != null)
						{
							try
							{
								out.close();
							}
							catch (Throwable ignore)
							{
							}
						}
					}
				}

				// read the entire content into memory and send it from there
				else
				{
					byte[] content = resource.getContent();
					if (content == null)
					{
						throw new IdUnusedException(ref.getReference());
					}

					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);
					res.setContentLength(len);

					// Increase the buffer size for more speed. - don't - we don't want a 20 meg buffer size,right? -ggolden
					// res.setBufferSize(len);

					OutputStream out = null;
					try
					{
						out = res.getOutputStream();
						out.write(content);
						out.flush();
						out.close();
					}
					catch (Throwable ignore)
					{
					}
					finally
					{
						if (out != null)
						{
							try
							{
								out.close();
							}
							catch (Throwable ignore)
							{
							}
						}
					}
				}
			}

			// track event
			EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_READ, resource.getReference(null), false));
		}
		catch (Throwable t)
		{
			throw new EntityNotDefinedException(ref.getReference());
		}
			}

	/**
	 * Process the access request for a collection, producing the "apache" style HTML file directory listing (complete with index.html redirect if found).
	 * 
	 * @param req
	 * @param res
	 * @param ref
	 * @param copyrightAcceptedRefs
	 * @throws PermissionException
	 * @throws IdUnusedException
	 * @throws ServerOverloadException
	 */
	protected void handleAccessCollection(HttpServletRequest req, HttpServletResponse res, Reference ref,
			Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
			EntityAccessOverloadException, EntityCopyrightException
			{
		// we only access resources, not collections
		if (!ref.getId().endsWith(Entity.SEPARATOR)) throw new EntityNotDefinedException(ref.getReference());

		// first, check for an index.html in the collection - redirect here if found
		if (m_storage.checkResource(ref.getId() + "index.html"))
		{
			String addr = Web.returnUrl(req, req.getPathInfo()) + "index.html";
			try
			{
				res.sendRedirect(addr);
				return;
			}
			catch (IOException e)
			{
				M_log.warn("handleAccessCollection: redirecting to " + addr + " : " + e);
			}
		}

		// need read permission
		if (!allowGetResource(ref.getId()))
			throw new EntityPermissionException(SessionManager.getCurrentSessionUserId(), AUTH_RESOURCE_READ, ref.getReference());

		BaseCollectionEdit collection = null;
		try
		{
			collection = (BaseCollectionEdit) getCollection(ref.getId());
		}
		catch (IdUnusedException e)
		{
			throw new EntityNotDefinedException(e.getId());
		}
		catch (PermissionException e)
		{
			throw new EntityPermissionException(e.getUser(), e.getLock(), e.getResource());
		}
		catch (TypeException e)
		{
			throw new EntityNotDefinedException(ref.getReference());
		}

		try
		{
			// use the helper
			CollectionAccessFormatter.format(collection, ref, req, res, getAccessPoint(true), getAccessPoint(false));

			// track event
			// EventTrackingService.post(EventTrackingService.newEvent(EVENT_RESOURCE_READ, collection.getReference(), false));
		}
		catch (Throwable t)
		{
			throw new EntityNotDefinedException(ref.getReference());
		}
			}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException
					{
				// if the id is null, the request was for just ".../content"
				String refId = ref.getId();
				if (refId == null) refId = "";

				// test if the given reference is a resource
				if (m_storage.checkResource(refId))
				{
					handleAccessResource(req, res, ref, copyrightAcceptedRefs);
					return;
				}

				// test for a collection
				if (m_storage.checkCollection(refId))
				{
					handleAccessCollection(req, res, ref, copyrightAcceptedRefs);
					return;
				}

				// finally, try a collection that was missing it's final separator
				if (!refId.endsWith(Entity.SEPARATOR))
				{
					// would it be a collection if we added the missing separator?
					if (m_storage.checkCollection(refId + Entity.SEPARATOR))
					{
						// redirect to this
						// Note: if the request had no trailing separator, getPathInfo still returns "/" - avoid ending up with "...//" -ggolden
						String addr = Web.returnUrl(req, req.getPathInfo())
						+ ("/".equals(req.getPathInfo()) ? "" : Entity.SEPARATOR);
						try
						{
							res.sendRedirect(addr);
							return;
						}
						catch (IOException e)
						{
							M_log.warn("handleAccess: redirecting to " + addr + " : " + e);
							throw new EntityNotDefinedException(ref.getReference());
						}
					}

					// nothing we know of...
					throw new EntityNotDefinedException(ref.getReference());
				}
					}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		Entity rv = null;

		ResourceProperties props = null;
		try
		{
			props = getProperties(ref.getId());
			boolean isCollection = false;
			try
			{
				isCollection = props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
			}
			catch (EntityPropertyNotDefinedException ignore)
			{
				// do nothing -- it's not a collection unless PROP_IS_COLLECTION is defined 
			}
			catch (EntityPropertyTypeException e)
			{
				// Log this and assume it's not a collection
				M_log.warn("EntityPropertyTypeException: PROP_IS_COLLECTION not boolean for " + ref.getReference());
			}
			if (isCollection)
			{
				try
				{
					rv = getCollection(ref.getId());
				}
				catch (TypeException e)
				{
					// in that case try to get it as a resource
					rv = getResource(ref.getId());
				}
			}
			else
			{
				try
				{
					rv = getResource(ref.getId());
				}
				catch (TypeException e)
				{
					// in that case try to get it as a collection
					rv = getCollection(ref.getId());
				}
			}
		}
		catch (PermissionException e)
		{
			M_log.warn("PermissionException " + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			M_log.warn("IdUnusedException " + ref.getReference());
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			M_log.warn("TypeException " + ref.getReference());
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		return getUrl(convertIdToUserEid(ref.getId()));
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		// form a key for thread-local caching
		String threadLocalKey = "getEntityAuthzGroups@" + userId + "@" + ref.getReference();
		Collection rv = (Collection) ThreadLocalManager.get(threadLocalKey);
		if (rv != null)
		{
			return new ArrayList(rv);
		}

		// use the resources realm, all container (folder) realms

		rv = new ArrayList();
		rv.addAll(getEntityHierarchyAuthzGroups(ref));

		try
		{
			boolean isDropbox = false;
			boolean attachmentOverride = false;
			// special check for group-user : the grant's in the user's My Workspace site
			String parts[] = StringUtil.split(ref.getId(), Entity.SEPARATOR);
			if ((parts.length > 3) && (parts[1].equals("group-user")))
			{
				rv.add(m_siteService.siteReference(m_siteService.getUserSiteId(parts[3])));
				isDropbox = true;
			}

			// If this is a site-scoped attachment, use the site grant as the only grant
			// Old attachments format: (use /content/attachment realm)
			//   /content/attachment/guid/filename.pd
			// New attachment format:
			//   /content/attachment/siteid/type/guid/filename.pd
			// But since we need to protect all paths from 
			//   /content/attachment/siteid/
			// and below we simply check to see f the guid is a valid site ID.
			if ( m_siteAttachments && (parts.length >= 3) && (parts[1].equals("attachment")))
			{
				String siteId = parts[2];
				if ( m_siteService.siteExists(siteId) )
				{
					rv.clear();  // Ignore the hierarchical inheritance in /attachment
					rv.add(m_siteService.siteReference(siteId));
					attachmentOverride = true;  // Nothing else is needed
				}
			}

			ContentEntity entity = null;
			if(ref.getId().endsWith(Entity.SEPARATOR)) 
			{
				entity = findCollection(ref.getId());
			} 
			else 
			{
				entity = findResource(ref.getId());
			}
			if(entity == null)
			{
				String refId = ref.getId();
				while (entity == null && refId != null && ! refId.trim().equals(""))
				{
					refId = isolateContainingId(refId);
					if(refId != null && ! refId.trim().equals(""))
					{
						entity = findCollection(refId);
					}
				}
			}

			boolean inherited = false;
			AccessMode access = entity.getAccess();

			if ( attachmentOverride )
			{
				// No further inheritance
			}
			else if(AccessMode.INHERITED.equals(access))
			{
				inherited = true;
				access = entity.getInheritedAccess();
			}
			if(isDropbox || AccessMode.SITE == access || AccessMode.INHERITED == access)
			{
				// site
				ref.addSiteContextAuthzGroup(rv);
			}
			else if(AccessMode.GROUPED.equals(access))
			{
				Site site = m_siteService.getSite(ref.getContext());
				boolean useSiteAsContext = false;
				if(site != null && userId != null)
				{
					useSiteAsContext = site.isAllowed(userId, AUTH_RESOURCE_ALL_GROUPS);
				}
				if(useSiteAsContext)
				{
					ref.addSiteContextAuthzGroup(rv);
				}
				else if(inherited)
				{
					rv.addAll(entity.getInheritedGroups());
				}
				else
				{
					rv.addAll(entity.getGroups());
				}
			}
		}
		catch (Throwable e)
		{
		}

		// cache in the thread
		ThreadLocalManager.set(threadLocalKey, new ArrayList(rv));

		if (M_log.isDebugEnabled())
		{
			M_log.debug("getEntityAuthzGroups for: ref: " + ref.getReference() + " user: " + userId);
			for (Iterator i = rv.iterator(); i.hasNext();)
			{
				M_log.debug("** -- " + i.next());
			}
		}
		return rv;
	}

	protected Collection getEntityHierarchyAuthzGroups(Reference ref) 
	{
		Collection rv = new TreeSet();

		// add the root
		rv.add(getReference("/"));

		// try the resource, all the folders above it (don't include /)
		String paths[] = StringUtil.split(ref.getId(), Entity.SEPARATOR);
		boolean container = ref.getId().endsWith(Entity.SEPARATOR);
		if (paths.length > 1)
		{
			String root = getReference(Entity.SEPARATOR + paths[1] + Entity.SEPARATOR);
			rv.add(root);

			for (int next = 2; next < paths.length; next++)
			{
				root += paths[next];
				if ((next < paths.length - 1) || container)
				{
					root +=  Entity.SEPARATOR;
				}
				rv.add(root);
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// start with an element with our very own name
		Element element = doc.createElement(ContentHostingService.class.getName());
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		// the root collection for the site
		String siteCollectionId = getSiteCollection(siteId);

		try
		{
			// get the collection for the site
			ContentCollection collection = getCollection(siteCollectionId);

			archiveCollection(collection, doc, stack, archivePath, siteCollectionId, results);
		}
		catch (Exception any)
		{
			results.append("Error archiving collection from site: " + siteId + " " + any.toString() + "\n");
		}

		stack.pop();

		return results.toString();

	} // archive

	/**
	 * {@inheritDoc}
	 */
	public String archiveResources(List attachments, Document doc, Stack stack, String archivePath)
	{
		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		// start with an element with our very own name
		Element element = doc.createElement(APPLICATION_ID);
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		for (Iterator i = attachments.iterator(); i.hasNext();)
		{
			Reference ref = (Reference) i.next();
			try
			{
				ContentResource resource = (ContentResource) ref.getEntity();

				if (resource != null)
				{
					results.append(archiveResource(resource, doc, stack, archivePath, null));
				}
			}
			catch (Exception any)
			{
				results.append("Error archiving resource: " + ref + " " + any.toString() + "\n");
				M_log.warn("archveResources: exception archiving resource: " + ref + ": ", any);
			}
		}

		stack.pop();

		return results.toString();
	}

	/**
	 * Replace the WT user id with the new qualified id
	 * 
	 * @param el
	 *        The XML element holding the perproties
	 * @param useIdTrans
	 *        The HashMap to track old WT id to new CTools id
	 */
	protected void WTUserIdTrans(Element el, Map userIdTrans)
	{
		NodeList children4 = el.getChildNodes();
		int length4 = children4.getLength();
		for (int i4 = 0; i4 < length4; i4++)
		{
			Node child4 = children4.item(i4);
			if (child4.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element4 = (Element) child4;
				if (element4.getTagName().equals("property"))
				{
					String creatorId = "";
					String modifierId = "";
					if (element4.hasAttribute("CHEF:creator"))
					{
						if ("BASE64".equalsIgnoreCase(element4.getAttribute("enc")))
						{
							creatorId = Xml.decodeAttribute(element4, "CHEF:creator");
						}
						else
						{
							creatorId = element4.getAttribute("CHEF:creator");
						}
						String newCreatorId = (String) userIdTrans.get(creatorId);
						if (newCreatorId != null)
						{
							Xml.encodeAttribute(element4, "CHEF:creator", newCreatorId);
							element4.setAttribute("enc", "BASE64");
						}
					}
					else if (element4.hasAttribute("CHEF:modifiedby"))
					{
						if ("BASE64".equalsIgnoreCase(element4.getAttribute("enc")))
						{
							modifierId = Xml.decodeAttribute(element4, "CHEF:modifiedby");
						}
						else
						{
							modifierId = element4.getAttribute("CHEF:modifiedby");
						}
						String newModifierId = (String) userIdTrans.get(modifierId);
						if (newModifierId != null)
						{
							Xml.encodeAttribute(element4, "CHEF:creator", newModifierId);
							element4.setAttribute("enc", "BASE64");
						}
					}
				}
			}
		}

	} // WTUserIdTrans

	/**
	 * Merge the resources from the archive into the given site.
	 * 
	 * @param siteId
	 *        The id of the site getting imported into.
	 * @param root
	 *        The XML DOM tree of content to merge.
	 * @param archviePath
	 *        The path to the folder where we are reading auxilary files.
	 * @return A log of status messages from the archive.
	 */
	public String merge(String siteId, Element root, String archivePath, String mergeId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		// get the system name: FROM_WT, FROM_CT, FROM_SAKAI
		String source = "";
		// root: <service> node
		Node parent = root.getParentNode(); // parent: <archive> node containing "system"
		if (parent.getNodeType() == Node.ELEMENT_NODE)
		{
			Element parentEl = (Element) parent;
			source = parentEl.getAttribute("system");
		}

		// prepare the buffer for the results log
		StringBuilder results = new StringBuilder();

		try
		{
			NodeList children = root.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// for "collection" kids
				if (element.getTagName().equals("collection"))
				{
					// replace the WT userid when needed
					if (!userIdTrans.isEmpty())
					{
						// replace the WT user id with new user id
						WTUserIdTrans(element, userIdTrans);
					}

					// from the relative id form a full id for the target site,
					// updating the xml element
					String relId = StringUtil.trimToNull(element.getAttribute("rel-id"));
					if (relId == null)
					{
						// Note: the site's root collection will have a "" rel-id, which will be null.
						continue;
					}
					String id = getSiteCollection(siteId) + relId;
					element.setAttribute("id", id);

					// collection: add if missing, else merge in
					ContentCollection c = mergeCollection(element);
					if (c == null)
					{
						results.append("collection: " + id + " already exists and was not replaced.\n");
					}
					else
					{
						results.append("collection: " + id + " imported.\n");
					}
				}

				// for "resource" kids
				else if (element.getTagName().equals("resource"))
				{
					// a flag showing if continuing merging this resource
					boolean goAhead = true;

					// check if the person who last modified this source has the right role
					// if not, set the goAhead flag to be false when fromSakai or fromCT
					if (source.equalsIgnoreCase("Sakai 1.0") || source.equalsIgnoreCase("CT"))
					{
						NodeList children2 = element.getChildNodes();
						int length2 = children2.getLength();
						for (int i2 = 0; i2 < length2; i2++)
						{
							Node child2 = children2.item(i2);
							if (child2.getNodeType() == Node.ELEMENT_NODE)
							{
								Element element2 = (Element) child2;

								// get the "channel" child
								if (element2.getTagName().equals("properties"))
								{
									NodeList children3 = element2.getChildNodes();
									final int length3 = children3.getLength();
									for (int i3 = 0; i3 < length3; i3++)
									{
										Node child3 = children3.item(i3);
										if (child3.getNodeType() == Node.ELEMENT_NODE)
										{
											Element element3 = (Element) child3;

											// for "message" children
											if (element3.getTagName().equals("property"))
											{
												if (element3.getAttribute("name").equalsIgnoreCase("CHEF:modifiedby"))
												{
													if ("BASE64".equalsIgnoreCase(element3.getAttribute("enc")))
													{
														String creatorId = Xml.decodeAttribute(element3, "value");
														if (!userListAllowImport.contains(creatorId)) goAhead = false;
													}
													else
													{
														String creatorId = element3.getAttribute("value");
														if (!userListAllowImport.contains(creatorId)) goAhead = false;
													}
												}
											}
										}
									}
								}
							}
						}
					} // the end to if fromSakai or fromCT

					if (goAhead)
					{
						// replace the WT userid when needed
						if (!userIdTrans.isEmpty())
						{
							// replace the WT user id with new user id
							WTUserIdTrans(element, userIdTrans);
						}

						// from the relative id form a full id for the target site,
						// updating the xml element
						String id = StringUtil.trimToNull(element.getAttribute("id"));
						String relId = StringUtil.trimToNull(element.getAttribute("rel-id"));

						// escape the invalid characters
						id = Validator.escapeQuestionMark(id);
						relId = Validator.escapeQuestionMark(relId);

						// if it's attachment, assign a new attachment folder
						if (id.startsWith(ATTACHMENTS_COLLECTION))
						{
							String oldRef = getReference(id);

							// take the name from after /attachment/whatever/
							id = ATTACHMENTS_COLLECTION + IdManager.createUuid()
							+ id.substring(id.indexOf('/', ATTACHMENTS_COLLECTION.length()));

							// record the rename
							attachmentNames.put(oldRef, id);
						}

						// otherwise move it into the site
						else
						{
							if (relId == null)
							{
								M_log.warn("mergeContent(): no rel-id attribute in resource");
								continue;
							}

							id = getSiteCollection(siteId) + relId;
						}

						element.setAttribute("id", id);

						ContentResource r = null;

						// if the body-location attribute points at another file for the body, get this
						String bodyLocation = StringUtil.trimToNull(element.getAttribute("body-location"));
						if (bodyLocation != null)
						{
							// the file name is relative to the archive file
							String bodyPath = StringUtil.fullReference(archivePath, bodyLocation);

							// get a stream from the file
							FileInputStream in = new FileInputStream(bodyPath);

							// read the bytes
							Blob body = new Blob();
							body.read(in);

							// resource: add if missing
							r = mergeResource(element, body.getBytes());
						}

						else
						{
							// resource: add if missing
							r = mergeResource(element);
						}

						if (r == null)
						{
							results.append("resource: " + id + " already exists and was not replaced.\n");
						}
						else
						{
							results.append("resource: " + id + " imported.\n");
						}
					}
				}
			}
		}
		catch (Exception any)
		{
			results.append("import interrputed: " + any.toString() + "\n");
			M_log.warn("mergeContent(): exception: ", any);
		}

		return results.toString();

	} // merge

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
		// default to import all resources
		boolean toBeImported = true;

		// set up the target collection
		ContentCollection toCollection = null;
		try
		{
			toCollection = getCollection(toContext);
		}
		catch(IdUnusedException e)
		{
			// not such collection yet, add one
			try
			{
				toCollection = addCollection(toContext);
			}
			catch(IdUsedException ee)
			{
				M_log.warn(this + toContext, ee);
			}
			catch(IdInvalidException ee)
			{
				M_log.warn(this + toContext, ee);
			}
			catch (PermissionException ee)
			{
				M_log.warn(this + toContext, ee);
			}
			catch (InconsistentException ee)
			{
				M_log.warn(this + toContext, ee);
			}
		}
		catch (TypeException e)
		{
			M_log.warn(this + toContext, e);
		}
		catch (PermissionException e)
		{
			M_log.warn(this + toContext, e);
		}

		if (toCollection != null)
		{
			// get the list of all resources for importing
			try
			{
				// get the root collection
				ContentCollection oCollection = getCollection(fromContext);

				// Get the collection members from the 'new' collection
				List oResources = oCollection.getMemberResources();
				for (int i = 0; i < oResources.size(); i++)
				{
					// get the original resource
					Entity oResource = (Entity) oResources.get(i);
					String oId = oResource.getId();

					if (resourceIds != null && resourceIds.size() > 0)
					{
						// only import those with ids inside the list
						toBeImported = false;
						for (int j = 0; j < resourceIds.size() && !toBeImported; j++)
						{
							if (((String) resourceIds.get(j)).equals(oId))
							{
								toBeImported = true;
							}
						}
					}

					if (toBeImported)
					{
						String oId2 = oResource.getId();
						String nId = "";

						int ind = oId2.indexOf(fromContext);
						if (ind != -1)
						{
							String str1 = "";
							String str2 = "";
							if (ind != 0)
							{
								// the substring before the fromContext string
								str1 = oId2.substring(0, ind);
							}
							if (!((ind + fromContext.length()) > oId2.length()))
							{
								// the substring after the fromContext string
								str2 = oId2.substring(ind + fromContext.length(), oId2.length());
							}
							// get the new resource id; fromContext is replaced with toContext
							nId = str1 + toContext + str2;
						}

						ResourceProperties oProperties = oResource.getProperties();
						boolean isCollection = false;
						try
						{
							isCollection = oProperties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
						}
						catch (Exception e)
						{
						}

						if (isCollection)
						{
							// add collection
							try
							{
								ContentCollectionEdit edit = addCollection(nId);
								// import properties
								ResourcePropertiesEdit p = edit.getPropertiesEdit();
								p.clear();
								p.addAll(oProperties);
								// complete the edit
								m_storage.commitCollection(edit);
								((BaseCollectionEdit) edit).closeEdit();
							}
							catch (IdUsedException e)
							{
							}
							catch (IdInvalidException e)
							{
							}
							catch (PermissionException e)
							{
							}
							catch (InconsistentException e)
							{
							}

							transferCopyEntities(oResource.getId(), nId, resourceIds);
						}
						else
						{
							try
							{
								// add resource
								ContentResourceEdit edit = addResource(nId);
								edit.setContentType(((ContentResource) oResource).getContentType());
								edit.setContent(((ContentResource) oResource).streamContent());
								//edit.setContent(((ContentResource) oResource).getContent());
								// import properties
								ResourcePropertiesEdit p = edit.getPropertiesEdit();
								p.clear();
								p.addAll(oProperties);
								// complete the edit
								m_storage.commitResource(edit);
								((BaseResourceEdit) edit).closeEdit();
							}
							catch (PermissionException e)
							{
							}
							catch (IdUsedException e)
							{
							}
							catch (IdInvalidException e)
							{
							}
							catch (InconsistentException e)
							{
							}
							catch (ServerOverloadException e)
							{
							}
						} // if
					} // if
				} // for
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
		}

	} // importResources

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		String id = null;
		String context = "";

		// for content hosting resources and collections
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// parse out the local resource id
			id = reference.substring(REFERENCE_ROOT.length(), reference.length());
		}

		// not mine
		else
		{
			return false;
		}

		// recognize a short reference
		if (m_shortRefs)
		{
			// ignoring the first separator, get the first item separated from the rest
			String prefix[] = StringUtil.splitFirst((id.length() > 1) ? id.substring(1) : "", Entity.SEPARATOR);
			if (prefix.length > 0)
			{
				// the following are recognized as full reference prefixe; if seen, the short ref feature is not applied
				if (!(prefix[0].equals("group") || prefix[0].equals("user") || prefix[0].equals("group-user")
						|| prefix[0].equals("public") || prefix[0].equals("private") || prefix[0].equals("attachment")))
				{
					String newPrefix = null;

					// a "~" starts a /user/ reference
					if (prefix[0].startsWith("~"))
					{
						newPrefix = Entity.SEPARATOR + "user" + Entity.SEPARATOR + prefix[0].substring(1);
					}

					// otherwise a /group/ reference
					else
					{
						newPrefix = Entity.SEPARATOR + "group" + Entity.SEPARATOR + prefix[0];
					}

					// reattach the tail (if any) to get the new id (if no taik, make sure we end with a separator if id started out with one)
					id = newPrefix
					+ ((prefix.length > 1) ? (Entity.SEPARATOR + prefix[1])
							: (id.endsWith(Entity.SEPARATOR) ? Entity.SEPARATOR : ""));
				}
			}
		}

		// parse out the associated site id, with alias checking
		String parts[] = StringUtil.split(id, Entity.SEPARATOR);
		boolean checkForAlias = true;
		boolean checkForUserIdEid = false;
		if (parts.length >= 3)
		{
			if (parts[1].equals("group"))
			{
				context = parts[2];
			}
			else if (parts[1].equals("user"))
			{
				context = m_siteService.getUserSiteId(parts[2]);

				// for user sites, don't check for alias
				checkForAlias = false;

				// enable user id/eid checking
				checkForUserIdEid = true;
			}
			else if (parts[1].equals("group-user"))
			{
				// use just the group context
				context = parts[2];
			}

			// if a user site, recognize ID or EID
			if (checkForUserIdEid && (parts[2] != null) && (parts[2].length() > 0))
			{
				try
				{
					// if successful, the context is already a valid user id
					UserDirectoryService.getUser(parts[2]);
				}
				catch (UserNotDefinedException tryEid)
				{
					try
					{
						// try using it as an EID
						String userId = UserDirectoryService.getUserId(parts[2]);

						// switch to the ID
						parts[2] = userId;
						context = m_siteService.getUserSiteId(userId);
						String newId = StringUtil.unsplit(parts, Entity.SEPARATOR);

						// add the trailing separator if needed
						if (id.endsWith(Entity.SEPARATOR)) newId += Entity.SEPARATOR;

						id = newId;
					}
					catch (UserNotDefinedException notEid)
					{
						// if context was not a valid EID, leave it alone
					}
				}
			}

			// recognize alias for site id - but if a site id exists that matches the requested site id, that's what we will use
			if (m_siteAlias && checkForAlias && (context != null) && (context.length() > 0))
			{
				if (!m_siteService.siteExists(context))
				{
					try
					{
						String target = m_aliasService.getTarget(context);

						// just to stay well clear of infinite looping (the newReference will call us for content references)
						// ignore any targets that are to the content service -ggolden
						if (!(target.startsWith(REFERENCE_ROOT) || target.startsWith(getUrl(""))))
						{
							Reference targetRef = m_entityManager.newReference(target);
							boolean changed = false;

							// for a site reference
							if (SiteService.APPLICATION_ID.equals(targetRef.getType()))
							{
								// use the ref's id, i.e. the site id
								context = targetRef.getId();
								changed = true;
							}

							// for mail archive reference
							// TODO: taken from MailArchiveService.APPLICATION_ID to (fake) reduce a dependency -ggolden
							else if ("sakai:mailarchive".equals(targetRef.getType()))
							{
								// use the ref's context as the site id
								context = targetRef.getContext();
								changed = true;
							}

							// if changed, update the id
							if (changed)
							{
								parts[2] = context;
								String newId = StringUtil.unsplit(parts, Entity.SEPARATOR);

								// add the trailing separator if needed
								if (id.endsWith(Entity.SEPARATOR)) newId += Entity.SEPARATOR;

								id = newId;
							}
						}
					}
					catch (IdUnusedException noAlias)
					{
					}
				}
			}
		}

		// if we end up with no id, or blank, use the root
		if ((id == null) || (id.length() == 0)) id = "/";

		ref.set(APPLICATION_ID, null, id, null, context);

		// because short refs or id/eid or alias processing may recognize a reference that is not the real reference,
		// update the ref's string to reflect the real reference
		ref.updateReference(REFERENCE_ROOT + id);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		String rv = "Content: " + ref.getId();

		try {
			ResourceProperties props;

			props = getProperties(ref.getId());

			if (props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION))
			{
				ContentCollection c = getCollection(ref.getId());
				rv = "Collection: " + c.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME) + " ("
				+ c.getId() + ")\n" + " Created: "
				+ c.getProperties().getPropertyFormatted(ResourceProperties.PROP_CREATION_DATE) + " by "
				+ c.getProperties().getPropertyFormatted(ResourceProperties.PROP_CREATOR) + "(User Id:"
				+ c.getProperties().getProperty(ResourceProperties.PROP_CREATOR) + ")\n"
				+ StringUtil.limit(c.getProperties().getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION), 30);
			}
			else
			{
				ContentResource r = getResource(ref.getId());
				rv = "Resource: " + r.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME) + " (" + r.getId()
				+ ")\n" + " Created: " + r.getProperties().getPropertyFormatted(ResourceProperties.PROP_CREATION_DATE)
				+ " by " + r.getProperties().getPropertyFormatted(ResourceProperties.PROP_CREATOR) + "(User Id:"
				+ r.getProperties().getProperty(ResourceProperties.PROP_CREATOR) + ")\n"
				+ StringUtil.limit(r.getProperties().getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION), 30);
			}
		} catch (PermissionException e) {
			M_log.warn("PermissionEception:", e);
		} catch (IdUnusedException e) {
			M_log.warn("IdUnusedException:", e);
		} catch (EntityPropertyNotDefinedException e) {
			M_log.warn("EntityPropertyNotDefinedException:", e);
		} catch (EntityPropertyTypeException e) {
			M_log.warn("EntityPropertyTypeException:", e);
		} catch (TypeException e) {
			M_log.warn("TypeException:", e);
		}
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		ResourceProperties props = null;

		try
		{
			props = getProperties(ref.getId());
		}
		catch (PermissionException e)
		{
		}
		catch (IdUnusedException e)
		{
		}

		return props;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.resources" };
		return toolIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextCreated(String context, boolean toolPlacement)
	{
		if (toolPlacement)
		{
			enableResources(context);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement)
		{
			enableResources(context);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		// TODO This avoids disabling the collection if the tool still exists, but ...
		// does it catch the case where the tool is being deleted from the site?
		if (toolPlacement)
		{
			disableResources(context);
		}
	}

	/**
	 * Make sure a home in resources exists for the site.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void enableResources(String context)
	{
		unlockCheck(SITE_UPDATE_ACCESS, context);

		// it would be called
		String id = getSiteCollection(context);

		// does it exist?
		try
		{
			Site site = m_siteService.getSite(context);
			try
			{
				ContentCollection collection = findCollection(id);	// getCollection(id);	// 

				if(collection == null)
				{
					// make it
					try
					{
						ContentCollectionEdit edit = addValidPermittedCollection(id);
						edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, site.getTitle());
						commitCollection(edit);
						collection = findCollection(id);
					}
					catch (IdUsedException e)
					{
						M_log.warn("enableResources: " + e);
						collection = findCollection(id);
					}
					catch (InconsistentException e)
					{
						// Because the id is coming from getSiteCollection(), this will never occur.
						// If it does, we better get alerted to it.
						M_log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
				}

				// do we need to update the title?
				if (!site.getTitle().equals(collection.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME)))
				{
					try
					{
						ContentCollectionEdit edit = editCollection(id);
						edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, site.getTitle());
						commitCollection(edit);
					}
					catch (IdUnusedException e)
					{
						M_log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
					catch (PermissionException e)
					{
						M_log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
					catch (InUseException e)
					{
						M_log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
				}
			}
			catch (TypeException e)
			{
				M_log.warn("enableResources: " + e);
				throw new RuntimeException(e);
			}
		}
		catch (IdUnusedException e)
		{
			// TODO: -ggolden
			M_log.warn("enableResources: " + e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Remove resources area for a site.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableResources(String context)
	{
		// TODO: we do nothing now - resources hang around after the tool is removed from the site or the site is deleted -ggolden
	}

	/**
	 * Make sure a home in resources for dropbox exists for the site.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void enableDropbox(String context)
	{
		// create it and the user folders within
		createDropboxCollection(context);
	}

	/**
	 * Remove resources area for a site.
	 * 
	 * @param site
	 *        The site.
	 */
	protected void disableDropbox(String context)
	{
		// TODO: we do nothing now - dropbox resources hang around after the tool is removed from the site or the site is deleted -ggolden
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * etc
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Archive the collection, then the members of the collection - recursively for collection members.
	 * 
	 * @param collection
	 *        The collection whose members are to be archived.
	 * @param doc
	 *        The document to contain the xml.
	 * @param stack
	 *        The stack of elements, the top of which will be the containing element of the "collection" or "resource" element.
	 * @param storagePath
	 *        The path to the folder where we are writing files.
	 * @param siteCollectionId
	 *        The resource id of the site collection.
	 * @param results
	 *        A log of messages from the archive.
	 */
	protected void archiveCollection(ContentCollection collection, Document doc, Stack stack, String storagePath,
			String siteCollectionId, StringBuilder results)
	{
		// first the collection
		Element el = collection.toXml(doc, stack);

		// store the relative file id in the xml
		el.setAttribute("rel-id", collection.getId().substring(siteCollectionId.length()));

		results.append("archiving collection: " + collection.getId() + "\n");

		// now each member
		List members = collection.getMemberResources();
		if ((members == null) || (members.size() == 0)) return;
		for (int i = 0; i < members.size(); i++)
		{
			Object member = members.get(i);
			if (member instanceof ContentCollection)
			{
				archiveCollection((ContentCollection) member, doc, stack, storagePath, siteCollectionId, results);
			}
			else if (member instanceof ContentResource)
			{
				results.append(archiveResource((ContentResource) member, doc, stack, storagePath, siteCollectionId));
			}
		}

	} // archiveCollection

	/**
	 * Archive a singe resource
	 * 
	 * @param resource
	 *        The content resource to archive
	 * @param doc
	 *        The XML document.
	 * @param stack
	 *        The stack of elements.
	 * @param storagePath
	 *        The path to the folder where we are writing files.
	 * @param siteCollectionId
	 *        The resource id of the site collection (optional).
	 * @return A log of messages from the archive.
	 */
	protected String archiveResource(ContentResource resource, Document doc, Stack stack, String storagePath,
			String siteCollectionId)
	{
		byte[] content = null;
		try
		{
			// TODO use stream instead of byte array
			// get the content bytes
			content = resource.getContent();
		}
		catch (ServerOverloadException e)
		{
			M_log.warn("archiveResource(): while reading body for: " + resource.getId() + " : " + e);
			// return "failed to archive resource: " + resource.getId() + " body temporarily unavailable due to server error\n"
		}

		// form the xml
		Element el = resource.toXml(doc, stack);

		// remove the content from the xml
		el.removeAttribute("body");

		// write the content to a file
		String fileName = IdManager.createUuid();
		Blob b = new Blob();
		b.append(content);
		try
		{
			FileOutputStream out = new FileOutputStream(storagePath + fileName);
			b.write(out);
			out.close();
		}
		catch (Exception e)
		{
			M_log.warn("archiveResource(): while writing body for: " + resource.getId() + " : " + e);
		}

		// store the file name in the xml
		el.setAttribute("body-location", fileName);

		// store the relative file id in the xml
		if (siteCollectionId != null)
		{
			el.setAttribute("rel-id", resource.getId().substring(siteCollectionId.length()));
		}

		return "archiving resource: " + resource.getId() + " body in file: " + fileName + "\n";
	}

	/**
	 * Merge in a collection from an XML DOM definition. Take whole if not defined already. Ignore if already here.
	 * 
	 * @param element
	 *        The XML DOM element containing the collection definition.
	 * @exception PermissionException
	 *            if the user does not have permission to add a collection.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception IdInvalidException
	 *            if the id is not valid.
	 * @return a new ContentCollection object, or null if it was not created.
	 */
	protected ContentCollection mergeCollection(Element element) throws PermissionException, InconsistentException,
	IdInvalidException
	{
		// read the collection object
		BaseCollectionEdit collectionFromXml = new BaseCollectionEdit(element);
		String id = collectionFromXml.getId();

		// add it
		BaseCollectionEdit edit = null;
		try
		{
			edit = (BaseCollectionEdit) addCollection(id);
		}
		catch (IdUsedException e)
		{
			// ignore if it exists
			return null;
		}

		// transfer from the XML read object to the edit
		edit.set(collectionFromXml);

		try
		{
			Time createTime = edit.getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
		}
		catch(EntityPropertyNotDefinedException epnde)
		{
			String now = TimeService.newTime().toString();
			edit.getProperties().addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		}
		catch(EntityPropertyTypeException epte)
		{
			M_log.error(epte);
		}

		// setup the event
		edit.setEvent(EVENT_RESOURCE_ADD);

		// commit the change
		commitCollection(edit);

		return edit;

	} // mergeCollection

	/**
	 * Merge in a resource from an XML DOM definition. Ignore if already defined. Take whole if not.
	 * 
	 * @param element
	 *        The XML DOM element containing the collection definition.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception IdInvalidException
	 *            if the id is not valid.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @exception ServerOverloadException
	 *            if the server is configured to write the resource body to the filesystem and the save fails.
	 * @return a new ContentResource object, or null if it was not created.
	 */
	protected ContentResource mergeResource(Element element) throws PermissionException, InconsistentException, IdInvalidException,
	OverQuotaException, ServerOverloadException
	{
		return mergeResource(element, null);

	} // mergeResource

	/**
	 * Merge in a resource from an XML DOM definition and a body bytes array. Ignore if already defined. Take whole if not.
	 * 
	 * @param element
	 *        The XML DOM element containing the collection definition.
	 * @param body
	 *        The body bytes.
	 * @exception PermissionException
	 *            if the user does not have permission to add a resource.
	 * @exception InconsistentException
	 *            if the containing collection does not exist.
	 * @exception IdInvalidException
	 *            if the id is not valid.
	 * @exception OverQuotaException
	 *            if this would result in being over quota.
	 * @return a new ContentResource object, or null if it was not created.
	 */
	protected ContentResource mergeResource(Element element, byte[] body) throws PermissionException, InconsistentException,
	IdInvalidException, OverQuotaException, ServerOverloadException
	{
		// make the resource object
		BaseResourceEdit resourceFromXml = new BaseResourceEdit(element);
		String id = resourceFromXml.getId();

		// get it added
		BaseResourceEdit edit = null;
		try
		{
			edit = (BaseResourceEdit) addResource(id);
		}
		catch (IdUsedException e)
		{
			// ignore the add if it exists already
			return null;
		}

		// transfer the items of interest (content type, properties) from the XML read object to the edit.
		edit.setContentType(resourceFromXml.getContentType());
		ResourcePropertiesEdit p = edit.getPropertiesEdit();
		p.clear();
		p.addAll(resourceFromXml.getProperties());

		// if body is provided, use it
		if (body != null)
		{
			edit.setContent(body);
		}

		// setup the event
		edit.setEvent(EVENT_RESOURCE_ADD);

		// commit the change - Note: we do properties differently
		assureResourceProperties(edit);

		// check for over quota.
		if (overQuota(edit))
		{
			throw new OverQuotaException(edit.getReference());
		}

		// complete the edit
		m_storage.commitResource(edit);

		if(! readyToUseFilesizeColumn())
		{
			addSizeCache(edit);
		}

		// track it
		String ref = edit.getReference(null);
		EventTrackingService.post(EventTrackingService.newEvent(((BaseResourceEdit) edit).getEvent(), ref, true,
				NotificationService.NOTI_NONE));
		postAvailableEvent(edit, ref, NotificationService.NOTI_NONE);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		return edit;

	} // mergeResource

	/**
	 * Find the containing collection id of a given resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the containing collection id.
	 */
	protected String isolateContainingId(String id)
	{
		// take up to including the last resource path separator, not counting one at the very end if there
		return id.substring(0, id.lastIndexOf('/', id.length() - 2) + 1);

	} // isolateContainingId

	/**
	 * Find the resource name of a given resource id.
	 * 
	 * @param id
	 *        The resource id.
	 * @return the resource name.
	 */
	protected String isolateName(String id)
	{
		if (id == null) return null;
		if (id.length() == 0) return null;

		// take after the last resource path separator, not counting one at the very end if there
		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1, (lastIsSeparator ? id.length() - 1 : id.length()));

	} // isolateName

	/**
	 * Check the fixed type and id infomation: The same or better content type based on the known type for this id's extension, if any. The same or added extension id based on the know MIME type, if any Only if the type is the unknown type already.
	 * 
	 * @param id
	 *        The resource id with possible file extension to check.
	 * @param type
	 *        The content type.
	 * @return the best guess content type based on this resource's id and resource id with extension based on this resource's MIME type.
	 */
	protected Map fixTypeAndId(String id, String type)
	{
		// the HashMap holds the id and mime type
		HashMap extType = new HashMap();
		extType.put("id", id);
		if (type == null) type = "";
		extType.put("type", type);
		String extension = Validator.getFileExtension(id);

		if (extension.length() != 0)
		{
			// if there's a file extension and a blank, null or unknown(application/binary) mime type,
			// fix the mime type by doing a lookup based on the extension
			if (((type == null) || (type.length() == 0) || (ContentTypeImageService.isUnknownType(type))))
			{
				extType.put("type", ContentTypeImageService.getContentType(extension));
			}
		}
		else
		{
			// if there is no file extension, but a non-null, non-blank mime type, do a lookup based on the mime type and add an extension
			// if there is no extension, find one according to the MIME type and add it.
			if ((type != null) && (!type.equals("")) && (!ContentTypeImageService.isUnknownType(type)))
			{
				extension = ContentTypeImageService.getContentTypeExtension(type);
				if (extension.length() > 0)
				{
					id = id + "." + extension;
					extType.put("id", id);
				}
			}
			else
			{
				// if mime type is null or mime type is empty or mime and there is no extension
				if ((type == null) || (type.equals("")))
				{
					extType.put("type", "application/binary");
				}
				// htripath- SAK-1811 remove '.bin' extension from binary file without any extension e.g makeFile
				// id = id + ".bin";
				extType.put("id", id);
			}
		}

		return extType;

	} // fixTypeAndId

	/**
	 * Test if this resource edit would place the account" over quota.
	 * 
	 * @param edit
	 *        The proposed resource edit.
	 * @return true if this change would palce the "account" over quota, false if not.
	 */

	protected boolean overQuota(ContentResourceEdit edit)
	{
		// Note: This implementation is hard coded to just check for a quota in the "/user/"
		// or "/group/" area. -ggolden

		// Note: this does NOT count attachments (/attachments/*) nor dropbox (/group-user/<site id>/<user id>/*) -ggolden

		// quick exits if we are not doing site quotas
		// if (m_siteQuota == 0)
		// return false;

		// some quick exits, if we are not doing user quota, or if this is not a user or group resource
		// %%% These constants should be from somewhere else -ggolden
		if (!((edit.getId().startsWith("/user/")) || (edit.getId().startsWith("/group/")))) return false;

		// expect null, "user" | "group", user/groupid, rest...
		String[] parts = StringUtil.split(edit.getId(), Entity.SEPARATOR);
		if (parts.length <= 2) return false;

		// get this collection
		String id = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR;
		ContentCollection collection = null;
		try
		{
			collection = findCollection(id);
		}
		catch (TypeException ignore)
		{
		}

		if (collection == null) return false;

		long quota = getQuota(collection);

		if (quota == 0)
		{
			return false;
		}

		long size = 0;

		if(readyToUseFilesizeColumn())
		{
			size = collection.getBodySizeK();
		}
		else
		{
			size = getCachedBodySizeK((BaseCollectionEdit)collection);
		}

		// find the resource being edited
		ContentResource inThere = null;
		try
		{
			inThere = findResource(edit.getId());
		}
		catch (TypeException ignore)
		{
		}

		if (inThere != null)
		{
			// reduce the size by the existing size
			size -= bytes2k(inThere.getContentLength());
		}

		// add in the new size
		size += bytes2k(edit.getContentLength());

		return (size >= quota);

	} // overQuota
	/**
	 * @param collection
	 * @return
	 */


	/*
	 * Size Cache.
	 * This caches the size of the collection and all children for 10 miutes from first 
	 * created, keeping a track of addtions and removals to the collection.
	 * 
	 * It only works where the same collection id is supplied and does not 
	 * consider the size under nested collections or update modifcations 
	 * on all nested collections.
	 * 
	 * It is a temporary fix to eliminate GC collection issues with the size calculations
	 */

	protected class SizeHolder {

		public long ttl = System.currentTimeMillis()+600000L;
		public long size = 0;

	}
	private Map<String, SizeHolder> quotaMap = new ConcurrentHashMap<String, SizeHolder>();

	private Map<String, SiteContentAdvisorProvider> siteContentAdvisorsProviders = new HashMap<String, SiteContentAdvisorProvider>();

	private long getCachedBodySizeK(BaseCollectionEdit collection) {
		return getCachedSizeHolder(collection,true).size;
	}
	private void addCachedBodySizeK(BaseCollectionEdit collection, long increment) {
		SizeHolder sh = getCachedSizeHolder(collection,false);
		if ( sh != null ) {
			sh.size += increment;
		}
	}



	/**
	 * @param collection
	 * @return
	 */

	private SizeHolder getCachedSizeHolder(BaseCollectionEdit collection,boolean create)
	{
		String id = collection.getId();
		SizeHolder sh = quotaMap.get(id);
		boolean scan = false;
		long now = System.currentTimeMillis();
		if ( sh != null ) {
			M_log.debug("Cache Hit ["+id+"] size=["+sh.size+"] ttl=["+(sh.ttl-now)+"]");
			if ( sh.ttl < now ) {
				M_log.debug("Cache Expire ["+id+"]");
				quotaMap.remove(id);
				sh = null;
				scan = true;
			}
		} else {
			M_log.debug("Cache Miss ["+id+"]");

		}

		if ( create && sh == null  ) {
			M_log.debug("Cache Create ["+id+"]");
			// get the content size of all resources in this hierarchy
			long size = collection.getBodySizeK();
			// the above can take a long time, just check that annother thread
			// hasnt just done the same, if it has then sh != null so we should not
			// add a new one in, and will have waisted our time.
			sh = quotaMap.get(id);
			if ( sh == null ) {
				sh = new SizeHolder();
				quotaMap.put(id,sh);
				sh.size  = size;
				scan = true;
			}
		} 
		if ( scan ) {
			// when we remove one, scan for old ones.
			for ( Iterator<String> i = quotaMap.keySet().iterator(); i.hasNext(); ) {
				String k = i.next();
				SizeHolder s = quotaMap.get(k);
				if ( s.ttl < now ) {
					M_log.debug("Cache Scan Expire ["+id+"]");
					quotaMap.remove(k);
				}
			}

		}
		return sh;
	}
	protected void removeSizeCache(ContentResourceEdit edit)
	{
		// Note: This implementation is hard coded to just check for a quota in the "/user/"
		// or "/group/" area. -ggolden

		// Note: this does NOT count attachments (/attachments/*) nor dropbox (/group-user/<site id>/<user id>/*) -ggolden

		// quick exits if we are not doing site quotas
		// if (m_siteQuota == 0)
		// return false;

		// some quick exits, if we are not doing user quota, or if this is not a user or group resource
		// %%% These constants should be from somewhere else -ggolden
		if (!((edit.getId().startsWith("/user/")) || (edit.getId().startsWith("/group/")))) return;

		// expect null, "user" | "group", user/groupid, rest...
		String[] parts = StringUtil.split(edit.getId(), Entity.SEPARATOR);
		if (parts.length <= 2) return;

		// get this collection
		String id = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR;
		ContentCollection collection = null;
		try
		{
			collection = findCollection(id);
		}
		catch (TypeException ignore)
		{
		}

		if (collection == null) return;

		addCachedBodySizeK((BaseCollectionEdit)collection, -bytes2k(edit.getContentLength()));

	} // updateSizeCache();
	protected void addSizeCache(ContentResourceEdit edit)
	{
		// Note: This implementation is hard coded to just check for a quota in the "/user/"
		// or "/group/" area. -ggolden

		// Note: this does NOT count attachments (/attachments/*) nor dropbox (/group-user/<site id>/<user id>/*) -ggolden

		// quick exits if we are not doing site quotas
		// if (m_siteQuota == 0)
		// return false;

		// some quick exits, if we are not doing user quota, or if this is not a user or group resource
		// %%% These constants should be from somewhere else -ggolden
		if (!((edit.getId().startsWith("/user/")) || (edit.getId().startsWith("/group/")))) return;

		// expect null, "user" | "group", user/groupid, rest...
		String[] parts = StringUtil.split(edit.getId(), Entity.SEPARATOR);
		if (parts.length <= 2) return;

		// get this collection
		String id = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR;
		ContentCollection collection = null;
		try
		{
			collection = findCollection(id);
		}
		catch (TypeException ignore)
		{
		}

		if (collection == null) return;

		addCachedBodySizeK((BaseCollectionEdit)collection, bytes2k(edit.getContentLength()));

	} // updateSizeCache();

	/**
	 * Convert bytes to Kbytes, rounding up, and counting even 0 bytes as 1 k.
	 * 
	 * @param bytes
	 *        The size in bytes.
	 * @return The size in Kbytes, rounded up.
	 */
	protected long bytes2k(long bytes)
	{
		return ((bytes - 1) / 1024) + 1;

	} // bytes2k

	/**
	 * gets the quota for a site collection or for a user's my workspace collection
	 *
	 * @param collection the collection on which to test for a quota.  this can be the collection for a site
	 * or a user's workspace collection
	 * @return the quota in kb
	 */
	public long getQuota(ContentCollection collection) {
		long quota = 0;

		// parse a string like /user/344454534543534535353543535
		String[] parts = StringUtil.split(collection.getId(), Entity.SEPARATOR);
		if (parts.length >= 3) {
			String siteId = null;

			// SITE_ID come in 2 forms (~siteid||siteid)
			if (parts[1].equals("user")) {
				siteId = "~" + parts[2];
			} else {
				siteId = parts[2];
			}

			String siteType = null;
			// get the site type
			try {
				siteType = m_siteService.getSite(siteId).getType();
			} catch (IdUnusedException e) {
				M_log.warn("SiteService could not find the site type");
			}

			// use this quota unless we have one more specific
			if (siteType != null) {
				quota = Long.parseLong(m_serverConfigurationService.getString("content.quota." + siteType, Long.toString(m_siteQuota)));
			}
		} else {
			quota = m_siteQuota;
		}

		// see if this collection has a quota property
		try
		{
			long siteSpecific = collection.getProperties().getLongProperty(
					ResourceProperties.PROP_COLLECTION_BODY_QUOTA);

			quota = siteSpecific;
		}
		catch (EntityPropertyNotDefinedException ignore)
		{
			// don't log or anything, this just means that this site doesn't have this quota property.
		}
		catch (Exception ignore)
		{
			M_log.warn("getQuota: reading quota property of : " + collection.getId() + " : " + ignore);
		}

		return quota;
	}

	/**
	 * Attempt to create any collections needed so that the parameter collection exists.
	 * 
	 * @param target
	 *        The collection that we want to exist.
	 */
	protected void generateCollections(String target)
	{
		try
		{
			// check each collection from the root
			String[] parts = StringUtil.split(target, "/");
			String id = "/";

			for (int i = 1; i < parts.length; i++)
			{
				// grow the id to the next collection
				id = id + parts[i] + "/";

				// does it exist?
				ContentCollection collection = findCollection(id);

				// if not, can we make it
				if (collection == null)
				{
					ContentCollectionEdit edit = addValidPermittedCollection(id);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, parts[i]);
					commitCollection(edit);
				}
			}
		}
		// if we cannot, give up
		catch (Exception any)
		{
			M_log.warn("generateCollections: " + any.getMessage(), any);
		}

	} // generateCollections

	/**
	 * {@inheritDoc}
	 */
	public String getSiteCollection(String siteId)
	{
		String rv = null;

		if (m_siteService.isUserSite(siteId))
		{
			rv = COLLECTION_USER + m_siteService.getSiteUserId(siteId) + "/";
		}

		else if (!m_siteService.isSpecialSite(siteId))
		{
			rv = COLLECTION_SITE + siteId + "/";
		}

		else
		{
			// ???
			rv = "/";
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPubView(String id)
	{
		boolean pubView = SecurityService.unlock(UserDirectoryService.getAnonymousUser(), AUTH_RESOURCE_READ, getReference(id));
		return pubView;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInheritingPubView(String id)
	{
		// the root does not inherit... and makes a bad ref if we try to isolateContainingId()
		if (isRootCollection(id)) return false;

		// check for pubview on the container
		String containerId = isolateContainingId(id);
		boolean pubView = SecurityService.unlock(UserDirectoryService.getAnonymousUser(), AUTH_RESOURCE_READ,
				getReference(containerId));
		return pubView;
	}

	/**
	 * Set this resource or collection to the pubview setting.
	 * 
	 * @param id
	 *        The resource or collection id.
	 * @param pubview
	 *        The desired public view setting.
	 */
	public void setPubView(String id, boolean pubview)
	{
		// TODO: check efficiency here -ggolden

		String ref = getReference(id);

		// edit the realm
		AuthzGroup edit = null;

		try
		{
			edit = m_authzGroupService.getAuthzGroup(ref);
		}
		catch (GroupNotDefinedException e)
		{
			// if no realm yet, and we need one, make one
			if (pubview)
			{
				try
				{
					edit = m_authzGroupService.addAuthzGroup(ref);
				}
				catch (Exception ee)
				{
				}
			}
		}

		// if we have no realm and don't need one, we are done
		if ((edit == null) && (!pubview)) return;

		// if we need a realm and didn't get an edit, exception
		if ((edit == null) && pubview) return;

		boolean changed = false;
		boolean delete = false;

		// align the realm with our positive setting
		if (pubview)
		{
			// make sure the anon role exists and has "content.read" - the only client of pubview
			Role role = edit.getRole(AuthzGroupService.ANON_ROLE);
			if (role == null)
			{
				try
				{
					role = edit.addRole(AuthzGroupService.ANON_ROLE);
				}
				catch (RoleAlreadyDefinedException ignore)
				{
				}
			}

			if (!role.isAllowed(AUTH_RESOURCE_READ))
			{
				role.allowFunction(AUTH_RESOURCE_READ);
				changed = true;
			}
		}

		// align the realm with our negative setting
		else
		{
			// get the role
			Role role = edit.getRole(AuthzGroupService.ANON_ROLE);
			if (role != null)
			{
				if (role.isAllowed(AUTH_RESOURCE_READ))
				{
					changed = true;
					role.disallowFunction(AUTH_RESOURCE_READ);
				}

				if (role.allowsNoFunctions())
				{
					edit.removeRole(role.getId());
					changed = true;
				}
			}

			// if "empty", we can delete the realm
			if (edit.isEmpty()) delete = true;
		}

		// if we want the realm deleted
		if (delete)
		{
			try
			{
				m_authzGroupService.removeAuthzGroup(edit);
			}
			catch (AuthzPermissionException e)
			{
			}
		}

		// if we made a change
		else if (changed)
		{
			try
			{
				m_authzGroupService.save(edit);
			}
			catch (GroupNotDefinedException e)
			{
				// TODO: IdUnusedException
			}
			catch (AuthzPermissionException e)
			{
				// TODO: PermissionException
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List findResources(String type, String primaryMimeType, String subMimeType)
	{
		List globalList = new ArrayList();

		Map othersites = getCollectionMap();
		Iterator siteIt = othersites.keySet().iterator();
		while (siteIt.hasNext())
		{
			String collId = (String) siteIt.next();
			String displayName = (String) othersites.get(collId);
			List artifacts = getFlatResources(collId);
			globalList.addAll(filterArtifacts(artifacts, type, primaryMimeType, subMimeType, true));
		}

		return globalList;
	}

	/**
	 * get all the resources under a given directory.
	 * 
	 * @param parentId
	 * @return List of all the ContentResource objects under this directory.
	 */
	protected List getFlatResources(String parentId)
	{
		return getAllResources(parentId);
	}

	/**
	 * Eliminate from the collection any duplicates as well as any items that are contained within another item whose resource-id is in the collection.
	 * 
	 * @param resourceIds
	 *        A collection of strings (possibly empty) identifying items and/or collections.
	 */
	public void eliminateDuplicates(Collection resourceIds)
	{
		Collection dups = new ArrayList();

		// eliminate exact duplicates
		Set others = new TreeSet(resourceIds);

		// eliminate items contained in other items
		Iterator itemIt = resourceIds.iterator();
		while (itemIt.hasNext())
		{
			String item = (String) itemIt.next();
			Iterator otherIt = others.iterator();
			while (otherIt.hasNext())
			{
				String other = (String) otherIt.next();
				if (other.startsWith(item))
				{
					if (item.equals(other))
					{
						continue;
					}

					// item contains other
					otherIt.remove();
				}
			}
		}

		// if any items have been removed, update the original collection
		if (resourceIds.size() > others.size())
		{
			resourceIds.clear();
			resourceIds.addAll(others);
		}

	} // eliminate duplicates

	protected List filterArtifacts(List artifacts, String type, String primaryMimeType, String subMimeType)
	{
		return filterArtifacts(artifacts, type, primaryMimeType, subMimeType, false);
	}

	protected List filterArtifacts(List artifacts, String type, String primaryMimeType, String subMimeType, boolean checkPerms)
	{
		for (Iterator i = artifacts.iterator(); i.hasNext();)
		{
			ContentResource resource = (ContentResource) i.next();
			//check for read permissions...
			if (!checkPerms || unlockCheck(AUTH_RESOURCE_READ, resource.getId())) 
			{
				String currentType = resource.getProperties().getProperty(ResourceProperties.PROP_STRUCTOBJ_TYPE);
				String mimeType = resource.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE);

				if (type != null && !type.equals(ResourceProperties.FILE_TYPE))
				{
					// process StructuredObject type
					if (currentType == null)
					{
						i.remove();
					}
					else if (!currentType.equals(type))
					{
						i.remove();
					}
				}
				else if (currentType != null && type != null && type.equals(ResourceProperties.FILE_TYPE))
				{
					// this one is a structured object, get rid of it
					i.remove();
				}
				else
				{
					String[] parts = mimeType.split("/");
					String currentPrimaryType = parts[0];
					String currentSubtype = null;
					if (parts.length > 1) currentSubtype = parts[1];

					// check the mime type match
					if (primaryMimeType != null && !primaryMimeType.equals(currentPrimaryType))
					{
						i.remove();
					}
					else if (subMimeType != null && !subMimeType.equals(currentSubtype))
					{
						i.remove();
					}
				}
			}
			else {
				i.remove();
			}
		}
		return artifacts;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dropbox Stuff
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected static final String DEFAULT_RESOURCECLASS = "org.sakaiproject.localization.util.ContentProperties";
	protected static final String DEFAULT_RESOURCEBUNDLE = "org.sakaiproject.localization.bundle.content.content";
	protected static final String RESOURCECLASS = "resource.class.content";
	protected static final String RESOURCEBUNDLE = "resource.bundle.content";
	private ResourceLoader rb = null;

	protected static final String DROPBOX_ID = " Drop Box";

	public static final String SITE_UPDATE_ACCESS = "site.upd";

	protected static final String GROUP_LIST = "sakai:authzGroup";

	protected static final String GROUP_NAME = "sakai:group_name";

	public static final String ACCESS_MODE = "sakai:access_mode";

	public static final String RELEASE_DATE = "sakai:release_date";

	public static final String RETRACT_DATE = "sakai:retract_date";

	public static final String HIDDEN = "sakai:hidden";

	public static final String CUSTOM_ORDER = "sakai:custom_order";

	public static final String CUSTOM_RANK = "sakai:rank_element";

	public static final String MEMBER_ID = "sakai:member_id";

	public static final String RANK = "sakai:rank";



	/**
	 * @inheritDoc
	 */
	public String getDropboxCollection()
	{
		return getDropboxCollection(ToolManager.getCurrentPlacement().getContext());
	}

	/**
	 * @inheritDoc
	 */
	public String getDropboxCollection(String siteId)
	{
		String rv = null;

		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return rv;
		}

		// form the site's dropbox collection
		rv = COLLECTION_DROPBOX + siteId + "/";

		// for maintainers, use the site level
		if (isDropboxMaintainer(siteId))
		{
			// return the site's dropbox collection
			return rv;
		}

		// Anonymous users do not get drop boxes
		String userId = SessionManager.getCurrentSessionUserId();
		if ( userId == null ) return rv;

		// form the current user's dropbox collection within this site's
		rv += StringUtil.trimToZero(userId) + "/";
		return rv;
	}

	/**
	 * Access the default dropbox collection display name for the current request. If the current user has permission to modify the site's dropbox collection, this is returned. Otherwise, the current user's collection within the site's dropbox is
	 * returned.
	 * 
	 * @return The default dropbox collection display name for the current request.
	 */
	public String getDropboxDisplayName()
	{
		return getDropboxDisplayName(ToolManager.getCurrentPlacement().getContext());
	}

	/**
	 * Access the default dropbox collection display name for the site. If the current user has permission to modify the site's dropbox collection, this is returned. Otherwise, the current user's collection within the site's dropbox is returned.
	 * 
	 * @param siteId
	 *        the Site id.
	 * @return The default dropbox collection display name for the site.
	 */
	public String getDropboxDisplayName(String siteId)
	{
		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return null;
		}

		// form the site's dropbox collection
		String id = COLLECTION_DROPBOX + siteId + "/";

		// for maintainers, use the site level dropbox
		if (isDropboxMaintainer(siteId))
		{
			// return the site's dropbox collection
			return siteId + DROPBOX_ID;
		}

		// return the current user's sort name
		return UserDirectoryService.getCurrentUser().getSortName();
	}

	/**
	 * Create the site's dropbox collection and one for each qualified user that the current user can make.
	 */
	public void createDropboxCollection()
	{
		createDropboxCollection(ToolManager.getCurrentPlacement().getContext());
	}

	/**
	 * Create the site's dropbox collection and one for each qualified user that the current user can make.
	 * 
	 * @param siteId
	 *        the Site id.
	 */
	public void createDropboxCollection(String siteId)
	{
		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return;
		}

		// do our ONE security check to see if the current user can create the
		// dropbox and all inner folders
		if (!isDropboxMaintainer(siteId))
		{
			createIndividualDropbox(siteId);
			return;
		}

		// form the site's dropbox collection
		String dropbox = COLLECTION_DROPBOX + siteId + "/";

		try
		{
			// try to create if it doesn't exist
			if (findCollection(dropbox) == null)
			{
				ContentCollectionEdit edit = addValidPermittedCollection(dropbox);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				try 
				{
					Site site = m_siteService.getSite(siteId);
				} 
				catch (IdUnusedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// these need to be moved to language bundle
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, siteId + DROPBOX_ID);
				props.addProperty(ResourceProperties.PROP_DESCRIPTION, rb.getString("use2"));
				// props.addProperty(ResourceProperties.PROP_DESCRIPTION, PROP_SITE_DROPBOX_DESCRIPTION);

				commitCollection(edit);
			}
		}
		catch (TypeException e)
		{
			M_log.warn("createDropboxCollection: TypeException: " + dropbox);
			return;
		}
		catch (IdUsedException e)
		{
			M_log.warn("createDropboxCollection: IdUsedException: " + dropbox);
			return;
		}
		catch (InconsistentException e)
		{
			M_log.warn("createDropboxCollection(): InconsistentException: " + dropbox);
			M_log.warn("createDropboxCollection(): InconsistentException: " + e.getMessage());
			return;
		}
		//		catch (PermissionException e) 
		//		{
		//			M_log.warn("createDropboxCollection(): PermissionException: " + dropbox);
		//			return;
		//		}

		SortedSet<String> members = new TreeSet<String>();
		try
		{
			ContentCollection topDropbox = findCollection(dropbox);
			members.addAll((List<String>) topDropbox.getMembers());
		}
		catch(TypeException e)
		{
			// do nothing
		}

		// The AUTH_DROPBOX_OWN is granted within the site, so we can ask for all the users who have this ability
		// using just the dropbox collection
		List users = SecurityService.unlockUsers(AUTH_DROPBOX_OWN, getReference(dropbox));
		for (Iterator it = users.iterator(); it.hasNext();)
		{
			User user = (User) it.next();

			// the folder id for this user's dropbox in this group
			String userFolder = dropbox + user.getId() + "/";

			if(members.contains(userFolder))
			{
				members.remove(userFolder);
				continue;
			}
			// see if it exists - add if it doesn't
			try
			{
				if (findCollection(userFolder) == null)
				{
					ContentCollectionEdit edit = addValidPermittedCollection(userFolder);
					ResourcePropertiesEdit props = edit.getPropertiesEdit();
					props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, user.getSortName());
					props.addProperty(ResourceProperties.PROP_DESCRIPTION, rb.getString("use1"));
					// props.addProperty(ResourceProperties.PROP_DESCRIPTION, PROP_MEMBER_DROPBOX_DESCRIPTION);
					commitCollection(edit);
				}
			}
			catch (TypeException e)
			{
				M_log.warn("createDropboxCollectionn(): TypeException: " + userFolder);
			}
			catch (IdUsedException e)
			{
				M_log.warn("createDropboxCollectionn(): idUsedException: " + userFolder);
			}
			catch (InconsistentException e)
			{
				M_log.warn("createDropboxCollection(): InconsistentException: " + userFolder);
			}
			//			catch (PermissionException e) 
			//			{
			//				M_log.warn("createDropboxCollection(): PermissionException: " + userFolder);
			//			}

			// the SortedSet "members" now contains id's for all folders that are not associated with a particular member of the site
		}
	}

	/**
	 * Create an individual dropbox collection for the current user if the site-level dropbox exists
	 * and the current user has AUTH_DROPBOX_OWN for the site.
	 * 
	 * @param siteId
	 *        the Site id.
	 */
	public void createIndividualDropbox(String siteId) 
	{
		String dropbox = COLLECTION_DROPBOX + siteId + "/";

		try 
		{
			if (findCollection(dropbox) == null)
			{
				try
				{
					ContentCollectionEdit edit = addValidPermittedCollection(dropbox);
					commitCollection(edit);
				}
				catch(IdUsedException e)
				{
					// hmmmm ... couldn't find it, but it's already in use???  let's bail out.
					return;
				}
				catch(InconsistentException e)
				{
					return;
				}
			}


			User user = UserDirectoryService.getCurrentUser();

			// the folder id for this user's dropbox in this group
			String userFolder = dropbox + user.getId() + "/";

			if(SecurityService.unlock(AUTH_DROPBOX_OWN, getReference(dropbox)))
			{
				// see if it exists - add if it doesn't
				try
				{
					if (findCollection(userFolder) == null)
					{
						ContentCollectionEdit edit = addValidPermittedCollection(userFolder);
						ResourcePropertiesEdit props = edit.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, user.getSortName());
						props.addProperty(ResourceProperties.PROP_DESCRIPTION, rb.getString("use1"));
						// props.addProperty(ResourceProperties.PROP_DESCRIPTION, PROP_MEMBER_DROPBOX_DESCRIPTION);
						commitCollection(edit);
					}
				}
				catch (TypeException e)
				{
					M_log.warn("createIndividualDropbox(): TypeException: " + userFolder);
				}
				catch (IdUsedException e)
				{
					M_log.warn("createIndividualDropbox(): idUsedException: " + userFolder);
				}
				catch (InconsistentException e)
				{
					M_log.warn("createIndividualDropbox(): InconsistentException: " + userFolder);
				} 
				//				catch (PermissionException e) 
				//				{
				//					M_log.warn("createIndividualDropbox(): PermissionException: " + userFolder);
				//				}
			}

		} 
		catch (TypeException e) 
		{
			M_log.warn("createIndividualDropbox(): TypeException: " + dropbox);
		}

	}

	/**
	 * Determine whether the default dropbox collection id for this user in this site 
	 * is the site's entire dropbox collection or just the current user's collection 
	 * within the site's dropbox.	 
	 * @return True if user sees all dropboxes in the site, false otherwise.
	 */
	public boolean isDropboxMaintainer()
	{
		return isDropboxMaintainer(ToolManager.getCurrentPlacement().getContext());
	}

	/**
	 * Determine whether the default dropbox collection id for this user in some site is the site's entire dropbox collection or just the current user's collection within the site's dropbox.
	 * 
	 * @return True if user sees all dropboxes in the site, false otherwise.
	 */
	public boolean isDropboxMaintainer(String siteId)
	{
		String dropboxId = null;

		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return false;
		}

		// if the user has dropbox maintain in the site, they are the dropbox maintainer
		// (dropbox maintain in their myWorkspace just gives them access to their own dropbox)
		return SecurityService.unlock(AUTH_DROPBOX_MAINTAIN, m_siteService.siteReference(siteId));
	}

	/******************************************************************************************************************************************************************************************************************************************************
	 * Group awareness implementation
	 *****************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Access a collection (Group) of groups to which this user has access and whose members have "content.read" permission in the collection. 
	 * In effect, this method returns a collection that identifies groups that are defined for the collection (locally or inherited) that 
	 * this user can access. If access to the collection is determined by group-membership, the return is limited to groups that have 
	 * access to the specified collection. If access is not defined by groups (i.e. it is "site" access), the return includes all groups
	 * defined in the site for which this user has read permission.
	 * 
	 * @param collectionId
	 *        The id for the collection.
	 */
	public Collection getGroupsWithReadAccess(String collectionId)
	{
		Collection rv = new ArrayList();

		String refString = getReference(collectionId);
		Reference ref = m_entityManager.newReference(refString);
		Collection groups = getGroupsAllowFunction(AUTH_RESOURCE_READ, ref.getReference());
		if(groups != null && ! groups.isEmpty())
		{
			rv.addAll(groups);
		}
		return rv;
	}

	/**
	 * Access a collection (Group) of groups to which this user has access and whose members have "content.new" permission in the collection. 
	 * In effect, this method returns a collection that identifies groups that are defined for the collection (locally or inherited) in which 
	 * this user has permission to add content entities. If access to the collection is determined by group-membership, the return is limited 
	 * to groups that have "add" permission in the specified collection. If access is not defined by groups (i.e. it is "site" access), the return 
	 * includes all groups defined in the site for which this user has add permission in this collection.
	 * 
	 * @param collectionId
	 *        The id for the collection.
	 */
	public Collection getGroupsWithAddPermission(String collectionId)
	{
		Collection rv = new ArrayList();

		String refString = getReference(collectionId);
		Reference ref = m_entityManager.newReference(refString);
		Collection groups = getGroupsAllowFunction(AUTH_RESOURCE_ADD, ref.getReference());
		if(groups != null && ! groups.isEmpty())
		{
			rv.addAll(groups);
		}
		return rv;		
	}

	/**
	 * Access a collection (Group) of groups to which this user has access and whose members have "content.delete" permission in the collection. 
	 * In effect, this method returns a collection that identifies groups that are defined for the collection (locally or inherited) in which 
	 * this user has permission to remove content entities. If access to the collection is determined by group-membership, the return is limited 
	 * to groups that have "remove" permission in the specified collection. If access is not defined by groups (i.e. it is "site" access), the return 
	 * includes all groups defined in the site for which this user has remove permission in this collection.
	 * 
	 * @param collectionId
	 *        The id for the collection.
	 */
	public Collection getGroupsWithRemovePermission(String collectionId)
	{
		Collection rv = new ArrayList();
		String owner = "";
		String currentUser = SessionManager.getCurrentSessionUserId();

		try
		{
			ResourceProperties props = getProperties(collectionId);
			owner = props.getProperty(ResourceProperties.PROP_CREATOR);
		}
		catch ( Exception e ) 
		{
			// assume user is not owner
		}

		String refString = getReference(collectionId);
		Reference ref = m_entityManager.newReference(refString);

		Collection groups = null;
		if ( currentUser != null && currentUser.equals(owner) )
			groups = getGroupsAllowFunction(AUTH_RESOURCE_REMOVE_OWN, ref.getReference());
		else if ( currentUser == null && owner == null )
			groups = getGroupsAllowFunction(AUTH_RESOURCE_REMOVE_OWN, ref.getReference());
		else
			groups = getGroupsAllowFunction(AUTH_RESOURCE_REMOVE_ANY, ref.getReference());

		if(groups != null && ! groups.isEmpty())
		{
			rv.addAll(groups);
		}
		return rv;		
	}


	/**
	 * Get a collection (Group) of groups that are defined in the containing context of a resource and that this user can access 
	 * in the way described by a function string.
	 * 
	 * @param function
	 *        The function to check
	 * @param refString
	 *        The reference for the resource.
	 */
	protected Collection getGroupsAllowFunction(String function, String refString)
	{
		Collection rv = new ArrayList();

		Collection groups = new ArrayList();
		Collection groupRefs = new TreeSet();
		if(this.m_allowGroupResources)
		{
			ContentEntity entity;
			try
			{
				Reference ref = m_entityManager.newReference(refString);
				Site site = m_siteService.getSite(ref.getContext());

				if(ref.getId().endsWith(Entity.SEPARATOR))
				{
					entity = findCollection(ref.getId());
				}
				else
				{
					entity = findResource(ref.getId());
				}

				if(entity != null)
				{
					if(AccessMode.INHERITED == entity.getAccess())
					{
						groups.addAll(entity.getInheritedGroupObjects());
						groupRefs.addAll(entity.getInheritedGroups());
					}
					else
					{
						groups.addAll(entity.getGroupObjects());
						groupRefs.addAll(entity.getGroups());
					}
				}

				if(groups.isEmpty())
				{
					// get the channel's site's groups
					groups.addAll(site.getGroups());
					for (Iterator i = groups.iterator(); i.hasNext();)
					{
						Group group = (Group) i.next();
						groupRefs.add(group.getReference());
					}
				}

				if(SecurityService.isSuperUser())
				{
					rv.addAll(groups);
				}
				else if(SecurityService.unlock(AUTH_RESOURCE_ALL_GROUPS, site.getReference()) && entity != null && unlockCheck(function, entity.getId()))
				{
					rv.addAll(groups);
				}
				else
				{
					Collection hierarchy = getEntityHierarchyAuthzGroups(ref);
					String userId = SessionManager.getCurrentSessionUserId();

					for (Iterator i = groups.iterator(); i.hasNext();)
					{
						Group group = (Group) i.next();
						if(group == null)
						{
							continue;
						}
						Collection azGroups = new ArrayList(hierarchy);
						azGroups.add(group.getReference());

						// check whether this user can take this action (function) on this resource
						// based on membership in this group.  If so, add the group.
						if (m_authzGroupService.isAllowed(userId, function, azGroups))
						{
							rv.add(group);
						}
					}
				}

			}
			catch (TypeException e1)
			{
				// ignore
			}
			catch(IdUnusedException e)
			{
				// ignore
			}

		}
		return rv;
	}

	/**
	 * If the id is to the /user/ area, make an id that is based on the user EID not ID, if the EID is available.
	 * @param id The resource id.
	 * @return The modified id.
	 */
	protected String convertIdToUserEid(String id)
	{
		if (id.startsWith("/user/"))
		{
			try
			{
				int pos = id.indexOf('/', 6);
				String userId = id.substring(6, pos);
				String userEid = UserDirectoryService.getUserEid(userId);
				String rv = "/user/" + userEid + id.substring(pos);
				return rv;
			}
			catch (StringIndexOutOfBoundsException e) {}
			catch (UserNotDefinedException e) {}
		}

		return id;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ContentEntity implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public abstract class BasicGroupAwareEdit implements GroupAwareEdit, ThreadBound
	{
		/** Store the resource id */
		protected String m_id = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/** When true, the collection has been removed. */
		protected boolean m_isRemoved = false;

		/** The access mode for this entity (e.g., "group" vs "site") */ 
		protected AccessMode m_access = AccessMode.INHERITED;

		/** The date/time after which the entity should no longer be generally available */
		protected Time m_retractDate = null;

		/** The date/time before which the entity should not be generally available */
		protected Time m_releaseDate = null;

		/** The availability of the item */
		protected boolean m_hidden = false;

		/** The Collection of group-ids for groups with access to this entity. */
		protected Collection m_groups = new ArrayList();

		/** The "priority" of this entity in its containing collection, if a custom sort order is defined for that collection */
		protected int m_customOrderRank = 0;

		/** The "type" in the ResourceTypeRegistry that defines properties of this ContentEntity */
		protected String m_resourceType;

		/**
		 * @inheritDoc
		 */
		public Collection getGroups()
		{
			return new ArrayList(m_groups);
		}

		/**
		 * @param context
		 * @return
		 */
		public String getContext() 
		{
			String context = null;
			Matcher contextMatcher = contextPattern.matcher(this.m_id);
			if(contextMatcher.find())
			{
				String root = contextMatcher.group(1);
				context = contextMatcher.group(2);
				if(! root.equals("group/"))
				{
					context = "~" + context;
				}
			}
			return context;
		}

		/**
		 * @inheritDoc
		 */
		public void clearGroupAccess() throws InconsistentException, PermissionException 
		{
			if(this.m_access != AccessMode.GROUPED)
			{
				throw new InconsistentException(this.getReference());
			}

			this.m_access = AccessMode.INHERITED;
			this.m_groups.clear();

		}

		/**
		 * @inheritDoc
		 */
		public void clearPublicAccess() throws InconsistentException, PermissionException 
		{
			setPubView(this.m_id, false);
			this.m_access = AccessMode.INHERITED;
			this.m_groups.clear();

		}

		public void setPublicAccess() throws PermissionException
		{
			setPubView(this.m_id, true);
			this.m_access = AccessMode.INHERITED;
			this.m_groups.clear();
		}

		/**
		 * @inheritDoc
		 */
		public void setGroupAccess(Collection groups) throws InconsistentException, PermissionException 
		{
			if (groups == null || groups.isEmpty())
			{
				throw new InconsistentException(this.getReference());
			}

			if(isInheritingPubView(this.m_id))
			{
				throw new InconsistentException(this.getReference());
			}

			if(isPubView(this.m_id))
			{
				setPubView(this.m_id, false);
			}

			SortedSet groupRefs = new TreeSet();
			if(this.getInheritedAccess() == AccessMode.GROUPED)
			{
				groupRefs.addAll(this.getInheritedGroups());
			}
			else
			{
				try
				{
					Reference ref = m_entityManager.newReference(this.getReference());
					Site site = m_siteService.getSite(ref.getContext());
					Iterator iterator = site.getGroups().iterator();
					while(iterator.hasNext())
					{
						Group group = (Group) iterator.next();
						groupRefs.add(group.getReference());
					}
				}
				catch (IdUnusedException e)
				{

				} 
			}

			Collection newGroups = new ArrayList();
			Iterator groupIt = groups.iterator();
			while(groupIt.hasNext())
			{
				String groupRef = null;
				Object obj = groupIt.next();
				if(obj instanceof String)
				{
					groupRef = (String) obj;
				}
				else if(obj instanceof Group)
				{
					groupRef = ((Group) obj).getReference();
				}
				if(! groupRefs.contains(groupRef))
				{
					throw new InconsistentException(this.getReference());
				}
				newGroups.add(groupRef);
			}

			this.m_access = AccessMode.GROUPED;
			this.m_groups.clear();
			this.m_groups.addAll(newGroups);

		}



		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getGroupObjects()
		 */
		public Collection getGroupObjects()
		{
			if(m_groups == null)
			{
				m_groups = new ArrayList();
			}
			Collection groups = new ArrayList();
			Iterator it = m_groups.iterator();
			while(it.hasNext())
			{
				String ref = (String) it.next();
				Group group = m_siteService.findGroup(ref);
				if(group != null)
				{
					groups.add(group);
				}
			}
			return groups;

		}

		/**
		 * @inheritDoc
		 */
		public AccessMode getAccess()
		{
			return m_access;
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedGroups()
		 */
		public Collection getInheritedGroups() 
		{
			Collection groups = new ArrayList();
			ContentEntity next = ((ContentEntity) this).getContainingCollection();
			while(next != null && AccessMode.INHERITED.equals(next.getAccess()))
			{
				next = next.getContainingCollection();
			}
			if(next != null && AccessMode.GROUPED.equals(next.getAccess()))
			{
				groups.addAll(next.getGroups());
			}
			return groups;
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedAccess()
		 */
		public AccessMode getInheritedAccess() 
		{
			AccessMode access = AccessMode.INHERITED;
			ContentCollection parent = ((ContentEntity) this).getContainingCollection();
			if(parent != null)
			{
				access = parent.getAccess();
			}
			while(AccessMode.INHERITED == access && parent != null)
			{
				access = parent.getAccess();
				parent = parent.getContainingCollection();
			}
			if(AccessMode.INHERITED == access)
			{
				access = AccessMode.SITE;
			}
			return access;
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedGroupObjects()
		 */
		public Collection getInheritedGroupObjects() 
		{
			Collection groups = new ArrayList();
			Collection groupRefs = getInheritedGroups();
			Iterator it = groupRefs.iterator();
			while(it.hasNext())
			{
				String groupRef = (String) it.next();
				Group group = m_siteService.findGroup(groupRef);
				groups.add(group);				
			}
			return groups;
		}

		/** 
		 * Determine whether current user can update the group assignments (add/remove groups) for the current resource.
		 * This is based on whether the user has adequate rights defined for the group (AUTH_RESOURCE_ADD) or for the 
		 * containing collection of the resource (AUTH_RESOURCE_ADD).  
		 * @param group The group ionvolved in the query.
		 * @return true if allowed, false otherwise.
		 */
		protected boolean allowGroupUpdate(Group group)
		{
			String resourceRef = getReference();
			return allowGroupUpdate(group, resourceRef);
		}

		/** 
		 * Determine whether current user can update the group assignments (add/remove groups) for a specified resource.
		 * This is based on whether the user has adequate rights defined for the group (AUTH_RESOURCE_ADD) or for the 
		 * containing collection of the resource (AUTH_RESOURCE_ADD).  
		 * @param group The group ionvolved in the query.
		 * @param resourceRef A reference string for the resource.
		 * @return true if allowed, false otherwise.
		 */
		protected boolean allowGroupUpdate(Group group, String resourceRef)
		{
			String collectionId = getContainingCollectionId(resourceRef);
			return unlockCheck(AUTH_RESOURCE_ADD, group.getReference()) || unlockCheck(AUTH_RESOURCE_ADD, collectionId);
		}

		public Time getReleaseDate()
		{
			return m_releaseDate;
		}

		public Time getRetractDate()
		{
			// TODO Auto-generated method stub
			return m_retractDate;
		}

		public boolean isAvailable() 
		{
			boolean available = !m_hidden;

			if(available && (this.m_releaseDate != null || this.m_retractDate != null))
			{
				Time now = TimeService.newTime();
				if(this.m_releaseDate != null)
				{
					available = this.m_releaseDate.before(now);
				}
				if(available && this.m_retractDate != null)
				{
					available = this.m_retractDate.after(now);
				}
			}
			if(!available)
			{
				return available;
			}
			ContentCollection parent = ((ContentEntity) this).getContainingCollection();
			if(parent == null)
			{
				return available;
			}
			return parent.isAvailable();
		}

		public boolean isHidden() 
		{
			return this.m_hidden;
		}

		public void setReleaseDate(Time time)
		{
			if(time == null)
			{
				m_releaseDate = null;
			}
			else
			{
				m_releaseDate = TimeService.newTime(time.getTime());
			}
			m_hidden = false;
		}

		public void setRetractDate(Time time)
		{
			if(time == null)
			{
				m_retractDate = null;
			}
			else
			{
				m_retractDate = TimeService.newTime(time.getTime());
			}
			m_hidden = false;
		}

		public void setAvailability(boolean hidden, Time releaseDate, Time retractDate) 
		{
			m_hidden = hidden;
			if(hidden)
			{
				this.m_releaseDate = null;
				this.m_retractDate = null;
			}
			else
			{
				if(releaseDate == null)
				{
					this.m_releaseDate = null;
				}
				else
				{
					this.m_releaseDate = TimeService.newTime(releaseDate.getTime());
				}
				if(retractDate == null)
				{
					this.m_retractDate = null;
				}
				else
				{
					this.m_retractDate = TimeService.newTime(retractDate.getTime());
				}
			}

		}

		public void setHidden() 
		{
			m_hidden = true;
			this.m_releaseDate = null;
			this.m_retractDate = null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ContentEntity#getResourceType()
		 */
		public String getResourceType()
		{
			return m_resourceType;
		}

		public ContentCollection getContainingCollection()
		{
			ContentCollection container = null;
			String containerId = isolateContainingId(this.getId());
			try
			{
				container = findCollection(containerId);
			}
			catch (TypeException e)
			{
			}
			return container;
		}

		public void setPriority()
		{
			ResourcePropertiesEdit props = getPropertiesEdit();
			String sortBy = props.getProperty(ResourceProperties.PROP_CONTENT_PRIORITY);
			if(sortBy == null)
			{
				// add a default value that sorts new items after existing items, with new folders before new resources
				String containingCollectionId = isolateContainingId(this.m_id);
				int count = 1;
				if(containingCollectionId != null)
				{
					try 
					{
						count = getCollectionSize(containingCollectionId) + 1;
					} 
					catch (IdUnusedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (TypeException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
					catch (PermissionException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(! m_id.endsWith(Entity.SEPARATOR))
				{
					count += ContentHostingService.CONTENT_RESOURCE_PRIORITY_OFFSET;
				}
				props.addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, Integer.toString(count));
			}
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ContentResourceEdit#setResourceType(java.lang.String)
		 */
		public void setResourceType(String type)
		{
			m_resourceType = type;
		}

	}	// BasicGroupAwareEntity

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ContentCollection implementation
	 *********************************************************************************************************************************************************************************************************************************************************/
	public class BaseCollectionEdit extends BasicGroupAwareEdit implements ContentCollectionEdit, SessionBindingListener, SerializableEntity,  SerializableCollectionAccess
	{
		private boolean m_sessionBound = false;
		/**
		 * Construct with an id.
		 * 
		 * @param id
		 *        The unique channel id.
		 */
		public BaseCollectionEdit(String id)
		{
			// set the id
			m_id = id;

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			m_resourceType = ResourceType.TYPE_FOLDER;

		} // BaseCollectionEdit

		/**
		 * @param services
		 * @return
		 */
		public ContentHandler getContentHandler(Map<String, Object> services)
		{
			final Entity thisEntity = this;
			return new DefaultEntityHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.sakaiproject.util.DefaultEntityHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
						{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if ("collection".equals(qName) && entity == null)
						{
							m_id = attributes.getValue("id");
							m_resourceType = ResourceType.TYPE_FOLDER;

							String refStr = getReference(m_id);
							Reference ref = m_entityManager.newReference(refStr);
							String context = ref.getContext();
							Site site = null;
							try
							{
								site = m_siteService.getSite(ref.getContext());
							}
							catch (IdUnusedException e)
							{

							}

							// extract access
							AccessMode access = AccessMode.INHERITED;
							String access_mode = attributes.getValue(ACCESS_MODE);
							if (access_mode != null && !access_mode.trim().equals(""))
							{
								access = AccessMode.fromString(access_mode);
							}

							m_access = access;
							if (m_access == null || AccessMode.SITE == m_access)
							{
								m_access = AccessMode.INHERITED;
							}

							// extract release date
							// m_releaseDate = TimeService.newTime(0);
							String date0 = attributes.getValue(RELEASE_DATE);
							if (date0 != null && !date0.trim().equals(""))
							{
								m_releaseDate = TimeService.newTimeGmt(date0);
								if (m_releaseDate.getTime() <= START_OF_TIME)
								{
									m_releaseDate = null;
								}
							}

							// extract retract date
							// m_retractDate = TimeService.newTimeGmt(9999,12,
							// 31, 23, 59, 59, 999);
							String date1 = attributes.getValue(RETRACT_DATE);
							if (date1 != null && !date1.trim().equals(""))
							{
								m_retractDate = TimeService.newTimeGmt(date1);
								if (m_retractDate.getTime() >= END_OF_TIME)
								{
									m_retractDate = null;
								}
							}

							String hidden = attributes.getValue(HIDDEN);
							m_hidden = hidden != null && !hidden.trim().equals("")
							&& !Boolean.FALSE.toString().equalsIgnoreCase(hidden);
							entity = thisEntity;
						}
						else if (GROUP_LIST.equals(qName))
						{
							String groupRef = attributes.getValue(GROUP_NAME);
							if (groupRef != null)
							{
								m_groups.add(groupRef);
							}
						}
						else if ("rightsAssignment".equals(qName))
						{

						}
						else
						{
							M_log.warn("Unexpected Element " + qName);
						}

					}
						}
			};
		}


		/**
		 * Construct as a copy of another.
		 * 
		 * @param other
		 *        The other to copy.
		 */
		public BaseCollectionEdit(ContentCollection other)
		{
			set(other);
			m_resourceType = ResourceType.TYPE_FOLDER;

		} // BaseCollectionEdit

		/**
		 * Construct from info in XML in a DOM element.
		 * 
		 * @param el
		 *        The XML DOM element.
		 */
		public BaseCollectionEdit(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			m_id = el.getAttribute("id");
			m_resourceType = ResourceType.TYPE_FOLDER;

			//			String refStr = getReference(m_id);
			//			Reference ref = m_entityManager.newReference(refStr);
			//			String context = ref.getContext();
			//			Site site = null;
			//			try
			//			{
			//				site = m_siteService.getSite(ref.getContext());
			//			}
			//			catch (IdUnusedException e)
			//			{
			//				
			//			}

			// the children (properties)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
					if(m_prioritySortEnabled)
					{
						// setPriority();
					}
				}
				// look for groups 
				else if(element.getTagName().equals(GROUP_LIST))
				{
					String groupRef = element.getAttribute(GROUP_NAME);
					if(groupRef != null)
					{
						m_groups.add(groupRef);
					} 
				}
				else if(element.getTagName().equals("rightsAssignment"))
				{

				}
			}

			// extract access
			AccessMode access = AccessMode.INHERITED;
			String access_mode = el.getAttribute(ACCESS_MODE);
			if(access_mode != null && !access_mode.trim().equals(""))
			{
				access = AccessMode.fromString(access_mode);
			}

			m_access = access;
			if(m_access == null || AccessMode.SITE == m_access)
			{
				m_access = AccessMode.INHERITED;
			}

			// extract release date
			// m_releaseDate = TimeService.newTime(0);
			String date0 = el.getAttribute(RELEASE_DATE);
			if(date0 != null && !date0.trim().equals(""))
			{
				m_releaseDate = TimeService.newTimeGmt(date0);
				if(m_releaseDate.getTime() <= START_OF_TIME)
				{
					m_releaseDate = null;
				}
			}

			// extract retract date
			// m_retractDate = TimeService.newTimeGmt(9999,12, 31, 23, 59, 59, 999);
			String date1 = el.getAttribute(RETRACT_DATE);
			if(date1 != null && !date1.trim().equals(""))
			{
				m_retractDate = TimeService.newTimeGmt(date1);
				if(m_retractDate.getTime() >= END_OF_TIME)
				{
					m_retractDate = null;
				}
			}

			String hidden = el.getAttribute(HIDDEN);
			m_hidden = hidden != null && ! hidden.trim().equals("") && ! Boolean.FALSE.toString().equalsIgnoreCase(hidden);

		} // BaseCollectionEdit

		/**
		 * 
		 */
		public BaseCollectionEdit()
		{
			m_properties = new BaseResourcePropertiesEdit();
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The other object to take values from.
		 */
		protected void set(ContentCollection other)
		{
			// set the id
			m_id = other.getId();

			// copy other's access mode and list of groups
			m_access = other.getAccess();
			m_groups.clear();
			m_groups.addAll(other.getGroups());
			chh = other.getContentHandler();
			chh_vce = other.getVirtualContentEntity();

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(other.getProperties());

			m_hidden = other.isHidden();

			if(m_hidden || other.getReleaseDate() == null)
			{
				m_releaseDate = null;
			}
			else
			{
				m_releaseDate = TimeService.newTime(other.getReleaseDate().getTime());
			}
			if(m_hidden || other.getRetractDate() == null)
			{
				m_retractDate = null;
			}
			else
			{
				m_retractDate = TimeService.newTime(other.getRetractDate().getTime());
			}

		} // set

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelCollection(this);
			}

		} // finalize

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ContentEntity#getUrl(boolean)
		 */
		public String getUrl(boolean relative)
		{
			return getAccessPoint(relative) + convertIdToUserEid(m_id);
		}

		/**
		 * Access the URL which can be used to access the resource.
		 * 
		 * @return The URL which can be used to access the resource.
		 */
		public String getUrl()
		{
			return getUrl(false);

		} // getUrl

		/**
		 * Access the internal reference which can be used to access the resource from within the system.
		 * 
		 * @return The the internal reference which can be used to access the resource from within the system.
		 */
		public String getReference()
		{
			return getAccessPoint(true) + m_id;

		} // getReference

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_id;

		} // getId

		/**
		 * Access a List of the collection's internal members, each a resource id string.
		 * 
		 * @return a List of the collection's internal members, each a resource id string (may be empty).
		 */
		public List getMembers()
		{
			// get the objects
			Collection<String> memberResourceIds = m_storage.getMemberResourceIds(this.m_id);
			Collection<String> memberCollectionIds = m_storage.getMemberCollectionIds(this.m_id);

			// form the list of just ids
			List<String> mbrs = new ArrayList<String>();
			if(memberResourceIds != null)
			{
				mbrs.addAll(memberResourceIds);
			}
			if(memberCollectionIds != null)
			{
				mbrs.addAll(memberCollectionIds);
			}

			//if (mbrs.size() == 0) return mbrs;

			// sort? %%%
			// Collections.sort(mbrs);

			return mbrs;

		} // getMembers

		/**
		 * Access the size of all the resource body bytes within this collection in Kbytes.
		 * 
		 * @return The size of all the resource body bytes within this collection in Kbytes.
		 */
		public long getBodySizeK()
		{
			long size = 0;

			if(readyToUseFilesizeColumn())
			{
				String context = getContext();
				if(context != null)
				{
					size = getSizeForContext(context)/1000L;
				}
			}
			else
			{
				// get the member objects
				List members = getMemberResources();

				// for each member
				for (Iterator it = members.iterator(); it.hasNext();)
				{
					Object obj = it.next();
					if (obj == null) continue;

					// do not count the size of virtual objects
					if (obj instanceof BaseCollectionEdit && ((BaseCollectionEdit)obj).getVirtualContentEntity() != null) continue;

					// if a resource, add the body size
					if (obj instanceof ContentResource)
					{
						size += bytes2k(((ContentResource) obj).getContentLength());
					}

					// if a collection, count it's size
					else
					{
						size += ((BaseCollectionEdit) obj).getBodySizeK();
					}
				}
			}
			// if (M_log.isDebugEnabled())
			// M_log.debug("getBodySizeK(): collection: " + getId() + " size: " + size);

			return size;

		} // getBodySizeK

		/**
		 * Access a List of the collections' internal members as full ContentResource or ContentCollection objects.
		 * 
		 * @return a List of the full objects of the members of the collection.
		 */
		public List getMemberResources()
		{
			List mbrs = (List) ThreadLocalManager.get("members@" + this.m_id);
			if(mbrs == null)
			{
				mbrs = new ArrayList();

				// if not caching
				if ((!m_caching) || (m_cache == null) || (m_cache.disabled()))
				{
					// TODO: current service caching
					mbrs.addAll(m_storage.getCollections(this));
					mbrs.addAll(m_storage.getResources(this));
				}

				else
				{
					// if the cache is complete for this collection, use it
					if (m_cache.isComplete(getReference()))
					{
						// get just this collection's members
						mbrs.addAll(m_cache.getAll(getReference()));
					}

					// otherwise get all the members from storage
					else
					{
						// Note: while we are getting from storage, storage might change. These can be processed
						// after we get the storage entries, and put them in the cache, and mark the cache complete.
						// -ggolden
						synchronized (m_cache)
						{
							// if we were waiting and it's now complete...
							if (m_cache.isComplete(getReference()))
							{
								// get just this collection's members
								mbrs.addAll(m_cache.getAll(getReference()));
							}
							else
							{
								// save up any events to the cache until we get past this load
								m_cache.holdEvents();

								// read from storage - resources and collections, but just those
								// whose path is this's path (i.e. just mine!)
								mbrs.addAll(m_storage.getCollections(this));
								mbrs.addAll(m_storage.getResources(this));

								// update the cache, and mark it complete
								for (int i = 0; i < mbrs.size(); i++)
								{
									Entity mbr = (Entity) mbrs.get(i);
									m_cache.put(mbr.getReference(), mbr);
								}

								m_cache.setComplete(getReference());

								// now we are complete, process any cached events
								m_cache.processEvents();
							}
						}
					}
				}

				ThreadLocalManager.set("members@" + this.m_id, mbrs);
			}

			//if (mbrs.size() == 0) return mbrs;

			// sort %%%
			// Collections.sort(mbrs);

			cacheEntities(mbrs); 

			return mbrs;

		} // getMemberResources

		protected List copyEntityList(List entities)
		{
			List list = new ArrayList();

			for(ContentEntity entity : (List<ContentEntity>)entities)
			{
				ContentEntity copy = null;
				if(entity instanceof ContentResource)
				{
					copy = new BaseResourceEdit((ContentResource) entity);
					ThreadLocalManager.set("findResource@" + entity.getId(), entity);	// new BaseResourceEdit((ContentResource) entity));
				}
				else if(entity instanceof ContentCollection)
				{
					copy = new BaseCollectionEdit((ContentCollection) entity);
					ThreadLocalManager.set("findCollection@" + entity.getId(), entity); 	// new BaseCollectionEdit((ContentCollection) entity));
				}
				if(copy != null)
				{
					list.add(copy);
				}
			}

			return list;
		}

		/**
		 * Access the collection's properties.
		 * 
		 * @return The collection's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;

		} // getProperties

		/**
		 * Set the collection as removed.
		 */
		protected void setRemoved()
		{
			m_isRemoved = true;

		} // setRemoved

		/**
		 * Clear all the members of the collection, all the way down. Security has already been checked!
		 */
		protected void clear() throws IdUnusedException, PermissionException, InconsistentException, TypeException, InUseException,
		ServerOverloadException
		{
			// get this collection's members
			List mbrs = getMemberResources();
			for (int i = 0; i < mbrs.size(); i++)
			{
				Object mbr = mbrs.get(i);
				if (mbr == null) continue;

				// for a contained collection, clear its members first - if any are in use, the show's over
				if (mbr instanceof ContentCollection)
				{
					((BaseCollectionEdit) mbr).clear();
				}

				// now remove this member
				if (mbr instanceof ContentCollection)
				{
					// if this is not allowed or in use, we throw and the show's over.
					removeCollection(((ContentCollection) mbr).getId());
				}
				else if (mbr instanceof ContentResource)
				{
					// if this is not allowed or in use, we throw and the show's over.
					removeResource(((ContentResource) mbr).getId());
				}
			}

		} // clear

		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 * 
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element collection = doc.createElement("collection");

			if (stack.isEmpty())
			{
				doc.appendChild(collection);
			}
			else
			{
				((Element) stack.peek()).appendChild(collection);
			}

			stack.push(collection);

			collection.setAttribute("id", m_id);
			collection.setAttribute("resource-type", ResourceType.TYPE_FOLDER);

			if(m_access == null || AccessMode.SITE == m_access)
			{
				m_access = AccessMode.INHERITED;
			}
			collection.setAttribute(ACCESS_MODE, m_access.toString());

			collection.setAttribute(HIDDEN, Boolean.toString(m_hidden));
			if(!m_hidden && m_releaseDate != null)
			{
				// add release-date 
				collection.setAttribute(RELEASE_DATE, m_releaseDate.toString());
			}
			if(!m_hidden && m_retractDate != null)
			{
				// add retract-date
				collection.setAttribute(RETRACT_DATE, m_retractDate.toString());
			}


			// properties
			m_properties.toXml(doc, stack);

			stack.pop();

			// add groups
			if ((m_groups != null) && (m_groups.size() > 0))
			{
				Iterator groupIt = m_groups.iterator();
				while( groupIt.hasNext())
				{
					// how does this get to be a Group instead of a groupRef???
					String groupRef = (String) groupIt.next();
					Element sect = doc.createElement(GROUP_LIST);
					sect.setAttribute(GROUP_NAME, groupRef);
					collection.appendChild(sect);
				}
			}

			return collection;

		} // toXml


		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
			m_sessionBound = true;
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			m_sessionBound  = false;
			if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelCollection(this);
			}

		} // valueUnbound

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.ContentEntity#isResource()
		 */
		public boolean isResource()
		{
			// TODO: this may need a different implementation in the handler
			return false;
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.ContentEntity#isCollection()
		 */
		public boolean isCollection()
		{
			// TODO: this may need a different implementation in the handler
			return true;
		}

		public void setPriorityMap(Map priorities) 
		{
			if(m_prioritySortEnabled)
			{
				ResourcePropertiesEdit myProps = getPropertiesEdit();
				myProps.addProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT, Boolean.TRUE.toString());
				Iterator nameIt = priorities.keySet().iterator();
				while(nameIt.hasNext())
				{
					String name = (String) nameIt.next();
					Integer priority = (Integer) priorities.get(name);

					try
					{
						if(name.endsWith(Entity.SEPARATOR))
						{
							ContentCollectionEdit entity = editCollection(name);
							ResourcePropertiesEdit props = entity.getPropertiesEdit();
							props.addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, priority.toString());
							//commitCollection(entity);
							// complete the edit
							m_storage.commitCollection(entity);

							// close the edit object
							((BaseCollectionEdit) entity).closeEdit();

							// the collection has changed so we must remove the old version from thread-local cache
							ThreadLocalManager.set("findCollection@" + entity.getId(), null);
						}
						else
						{
							ContentResourceEdit entity = editResource(name);
							ResourcePropertiesEdit props = entity.getPropertiesEdit();
							props.addProperty(ResourceProperties.PROP_CONTENT_PRIORITY, priority.toString());

							// complete the edit
							m_storage.commitResource(entity);

							// close the edit object
							((BaseResourceEdit) entity).closeEdit();

							// must remove old version of this edit from thread-local cache
							// so we get new version if we try to retrieve it in same thread
							ThreadLocalManager.set("findResource@" + entity.getId(), null);

							// close the edit object
							((BaseResourceEdit) entity).closeEdit();
						}
					}
					catch(TypeException e)
					{
						// TODO Auto-generated catch block
						M_log.warn("TypeException",e);
					} 
					catch (IdUnusedException e) 
					{
						// TODO Auto-generated catch block
						M_log.warn("IdUnusedException",e);
					} 
					catch (PermissionException e) 
					{
						// TODO Auto-generated catch block
						M_log.warn("PermissionException",e);
					} 
					catch (InUseException e) 
					{
						// TODO Auto-generated catch block
						M_log.warn("InUseException",e);
					} 
					catch (ServerOverloadException e) 
					{
						// TODO Auto-generated catch block
						M_log.warn("ServerOverloadException",e);
					}
				}

			}
		}

		public int getMemberCount() 
		{
			int count = 0;
			Integer countObj = (Integer) ThreadLocalManager.get("getMemberCount@" + this.m_id);
			if(countObj == null)
			{
				count = m_storage.getMemberCount(this.m_id);
				ThreadLocalManager.set("getMemberCount@" + this.m_id, new Integer(count));
			}
			else
			{
				count = countObj.intValue();
			}
			return count;
		}

		/*************************************************************************************************************************************************************
		 * ContentHostingHandler Support
		 */

		/**
		 * Real storage does not have handlers
		 */
		private ContentHostingHandler chh = null;
		private ContentEntity chh_vce = null; // the wrapped virtual content entity
		public ContentHostingHandler getContentHandler() {return chh;}
		public void setContentHandler(ContentHostingHandler chh) {this.chh = chh;}
		public ContentEntity getVirtualContentEntity() {return chh_vce;}
		public void setVirtualContentEntity(ContentEntity ce) {this.chh_vce = ce;}

		public ContentEntity getMember(String nextId)
		{
			ContentEntity ce  = m_storage.getCollection(nextId);
			if ( ce == null ) {
				try
				{
					ce = m_storage.getResource(nextId);
				}
				catch (TypeException e)
				{
					M_log.error("Type Exception ",e);
				}
			}
			return ce;
			/*
			List l = getMemberResources();
			for ( Iterator li = l.iterator(); li.hasNext(); ) {
				ContentEntity ce = (ContentEntity) li.next();
				if ( nextId.equals(ce.getId())) {
					return ce;
				}
			}
			return null;
			 */
		}


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableAccess()
		 */
		public AccessMode getSerializableAccess()
		{
			return m_access;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableGroup()
		 */
		public Collection<String> getSerializableGroup()
		{
			return m_groups;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableHidden()
		 */
		public boolean getSerializableHidden()
		{
			return m_hidden;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableId()
		 */
		public String getSerializableId()
		{
			return m_id;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableProperties()
		 */
		public SerializableEntity getSerializableProperties()
		{
			return (SerializableEntity)m_properties;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableReleaseDate()
		 */
		public Time getSerializableReleaseDate()
		{
			return m_releaseDate;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#getSerializableRetractDate()
		 */
		public Time getSerializableRetractDate()
		{
			return m_retractDate;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableAccess(org.sakaiproject.content.api.GroupAwareEntity.AccessMode)
		 */
		public void setSerializableAccess(AccessMode access)
		{
			m_access = access;

		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableGroups(java.util.List)
		 */
		public void setSerializableGroups(Collection<String> groups)
		{
			m_groups = groups;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableHidden(boolean)
		 */
		public void setSerializableHidden(boolean hidden)
		{
			m_hidden = hidden;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableId(java.lang.String)
		 */
		public void setSerializableId(String id)
		{
			m_id = id;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableReleaseDate(org.sakaiproject.time.api.Time)
		 */
		public void setSerializableReleaseDate(Time releaseDate)
		{
			m_releaseDate = releaseDate;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableResourceType(java.lang.String)
		 */
		public void setSerializableResourceType(String resourceType)
		{
			m_resourceType = resourceType;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess#setSerializableRetractDate(org.sakaiproject.time.api.Time)
		 */
		public void setSerializableRetractDate(Time retractDate)
		{
			m_retractDate = retractDate;
		}

		public void unbind() {
			if ( !m_sessionBound && m_active ) {
				M_log.warn("Edit Object not closed correctly, Cancelling "+this.getId());
				cancelCollection(this);
			}			
		}




	} // class BaseCollectionEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ContentResource implementation
	 *********************************************************************************************************************************************************************************************************************************************************/
	public class BaseResourceEdit extends BasicGroupAwareEdit implements ContentResourceEdit, SessionBindingListener, SerializableEntity, SerializableResourceAccess
	{

		/** The content type. */
		protected String m_contentType = null;

		/** The body. May be missing - not yet read (null) */
		protected byte[] m_body = null;

		/** The content length of the body, consult only if the body is missing (null) */
		protected int m_contentLength = 0;

		/** When true, someone changed the body content with setContent() */
		protected boolean m_bodyUpdated = false;

		/** The file system path, post root, for file system stored body binary. */
		protected String m_filePath = null;

		protected InputStream m_contentStream;

		private boolean m_sessionBound = true;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The local resource id.
		 */
		public BaseResourceEdit(String id)
		{
			m_id = id;

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			// allocate a file path if needed
			if (m_bodyPath != null)
			{
				setFilePath(TimeService.newTime());
			}
		} // BaseResourceEdit




		/**
		 * Construct as a copy of another
		 * 
		 * @param other
		 *        The other to copy.
		 */
		public BaseResourceEdit(ContentResource other)
		{
			set(other);
		} // BaseResourceEdit

		/**
		 * Set the file path for this resource
		 * 
		 * @param time
		 *        The time on which to based the path.
		 */
		protected void setFilePath(Time time)
		{
			// compute file path: use now in ms mod m_bodyVolumes.length to pick a volume from m_bodyVolumes (if defined)
			// add /yyyy/DDD/HH year / day of year / hour / and a unique id for the final file name.
			// Don't include the body path.
			String volume = "/";
			if ((m_bodyVolumes != null) && (m_bodyVolumes.length > 0))
			{
				volume += m_bodyVolumes[(int) (Math.abs(time.getTime()) % ((long) m_bodyVolumes.length))];
				volume += "/";
			}

			m_filePath = volume + time.toStringFilePath() + IdManager.createUuid();
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The other object to take values from.
		 */
		protected void set(ContentResource other)
		{
			m_id = other.getId();
			m_contentType = other.getContentType();
			m_contentLength = other.getContentLength();
			m_resourceType = other.getResourceType();
			chh = other.getContentHandler();
			chh_vce = other.getVirtualContentEntity();

			this.m_contentStream = ((BaseResourceEdit) other).m_contentStream;

			m_filePath = ((BaseResourceEdit) other).m_filePath;

			// if there's a body in the other, reference it, else leave this one null
			// Note: this treats the body byte array as immutable, so to update it one
			// *must* call setContent() not just getContent and mess with the bytes. -ggolden
			byte[] content = ((BaseResourceEdit) other).m_body;
			if (content != null)
			{
				m_contentLength = content.length;
				m_body = content;
			}

			// copy other's access mode and list of groups
			m_access = other.getAccess();
			m_groups.clear();
			m_groups.addAll(other.getGroups());

			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(other.getProperties());

			m_hidden = other.isHidden();

			if(m_hidden || other.getReleaseDate() == null)
			{
				m_releaseDate = null;
			}
			else
			{
				m_releaseDate = TimeService.newTime(other.getReleaseDate().getTime());
			}
			if(m_hidden || other.getRetractDate() == null)
			{
				m_retractDate = null;
			}
			else
			{
				m_retractDate = TimeService.newTime(other.getRetractDate().getTime());
			}

		} // set

		/**
		 * 
		 */
		public BaseResourceEdit()
		{
			// we ignore the container
			m_properties = new BaseResourcePropertiesEdit();
		}
		/**
		 * Construct from information in XML in a DOM element.
		 * 
		 * @param el
		 *        The XML DOM element.
		 */
		public BaseResourceEdit(Element el)
		{
			m_properties = new BaseResourcePropertiesEdit();

			m_id = el.getAttribute("id");
			String contentType = StringUtil.trimToNull(el.getAttribute("content-type"));
			setContentType(contentType);
			m_contentLength = 0;
			try
			{
				m_contentLength = Integer.parseInt(el.getAttribute("content-length"));
			}
			catch (Exception ignore)
			{
			}
			ResourceTypeRegistry registry = getResourceTypeRegistry();
			String typeId = StringUtil.trimToNull(el.getAttribute("resource-type"));
			if(typeId == null || registry.getType(typeId) == null)
			{
				typeId = registry.mimetype2resourcetype(contentType);
			}
			setResourceType(typeId);

			String enc = StringUtil.trimToNull(el.getAttribute("body"));
			if (enc != null)
			{
				byte[] decoded = null;
				try
				{
					decoded = Base64.decodeBase64(enc.getBytes("UTF-8"));
				}
				catch (UnsupportedEncodingException e)
				{
					M_log.error(e);
				}
				m_body = new byte[m_contentLength];
				System.arraycopy(decoded, 0, m_body, 0, m_contentLength);
			}

			m_filePath = StringUtil.trimToNull(el.getAttribute("filePath"));

			// the children (properties)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);
					if(m_prioritySortEnabled)
					{
						// setPriority();
					}
				}
				// look for groups
				else if(element.getTagName().equals(GROUP_LIST))
				{
					m_groups.add(element.getAttribute(GROUP_NAME)); 
				}

				// extract access
				AccessMode access = AccessMode.INHERITED;
				String access_mode = el.getAttribute(ACCESS_MODE);
				if(access_mode != null && !access_mode.trim().equals(""))
				{
					access = AccessMode.fromString(access_mode);
				}
				m_access = access;
				if(m_access == null || AccessMode.SITE == m_access)
				{
					m_access = AccessMode.INHERITED;
				}

				String hidden = el.getAttribute(HIDDEN);
				m_hidden = hidden != null && ! hidden.trim().equals("") && ! Boolean.FALSE.toString().equalsIgnoreCase(hidden);

				if(m_hidden)
				{
					m_releaseDate = null;
					m_retractDate = null;
				}
				else
				{
					// extract release date
					String date0 = el.getAttribute(RELEASE_DATE);
					if(date0 != null && !date0.trim().equals(""))
					{
						m_releaseDate = TimeService.newTimeGmt(date0);
						if(m_releaseDate.getTime() <= START_OF_TIME)
						{
							m_releaseDate = null;
						}
					}

					// extract retract date
					String date1 = el.getAttribute(RETRACT_DATE);
					if(date1 != null && !date1.trim().equals(""))
					{
						m_retractDate = TimeService.newTimeGmt(date1);
						if(m_retractDate.getTime() >= END_OF_TIME)
						{
							m_retractDate = null;
						}
					}
				}

			}
		} // BaseResourceEdit
		/**
		 * @param services
		 * @return
		 */
		public ContentHandler getContentHandler(Map<String, Object> services)
		{
			final Entity thisEntity = this;
			return new DefaultEntityHandler()
			{
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.sakaiproject.util.DefaultEntityHandler#startElement(java.lang.String,
				 *      java.lang.String, java.lang.String,
				 *      org.xml.sax.Attributes)
				 */
				@Override
				public void startElement(String uri, String localName, String qName,
						Attributes attributes) throws SAXException
						{
					if (doStartElement(uri, localName, qName, attributes))
					{
						if ("resource".equals(qName) && entity == null)
						{
							m_id = attributes.getValue("id");
							String contentType = StringUtil.trimToNull(attributes
									.getValue("content-type"));
							setContentType(contentType);
							m_contentLength = 0;
							try
							{
								m_contentLength = Integer.parseInt(attributes
										.getValue("content-length"));
							}
							catch (Exception ignore)
							{
							}
							ResourceTypeRegistry registry = getResourceTypeRegistry();
							String typeId = StringUtil.trimToNull(attributes
									.getValue("resource-type"));
							if (typeId == null || registry.getType(typeId) == null)
							{
								typeId = registry.mimetype2resourcetype(contentType);
							}
							setResourceType(typeId);

							String enc = StringUtil.trimToNull(attributes
									.getValue("body"));
							if (enc != null)
							{
								byte[] decoded = null;
								try
								{
									decoded = Base64.decodeBase64(enc.getBytes("UTF-8"));
								}
								catch (UnsupportedEncodingException e)
								{
									M_log.error(e);
								}
								m_body = new byte[m_contentLength];
								System.arraycopy(decoded, 0, m_body, 0, m_contentLength);
							}

							m_filePath = StringUtil.trimToNull(attributes
									.getValue("filePath"));
							AccessMode access = AccessMode.INHERITED;
							String access_mode = attributes.getValue(ACCESS_MODE);
							if (access_mode != null && !access_mode.trim().equals(""))
							{
								access = AccessMode.fromString(access_mode);
							}
							m_access = access;
							if (m_access == null || AccessMode.SITE == m_access)
							{
								m_access = AccessMode.INHERITED;
							}

							String hidden = attributes.getValue(HIDDEN);
							m_hidden = hidden != null && !hidden.trim().equals("")
							&& !Boolean.FALSE.toString().equalsIgnoreCase(hidden);

							if (m_hidden)
							{
								m_releaseDate = null;
								m_retractDate = null;
							}
							else
							{
								// extract release date
								String date0 = attributes.getValue(RELEASE_DATE);
								if (date0 != null && !date0.trim().equals(""))
								{
									m_releaseDate = TimeService.newTimeGmt(date0);
									if (m_releaseDate.getTime() <= START_OF_TIME)
									{
										m_releaseDate = null;
									}
								}

								// extract retract date
								String date1 = attributes.getValue(RETRACT_DATE);
								if (date1 != null && !date1.trim().equals(""))
								{
									m_retractDate = TimeService.newTimeGmt(date1);
									if (m_retractDate.getTime() >= END_OF_TIME)
									{
										m_retractDate = null;
									}
								}
							}
							entity = thisEntity;
						}
						else if (GROUP_LIST.equals(qName))
						{
							m_groups.add(attributes.getValue(GROUP_NAME));
						}
						else
						{
							M_log.warn("Unexpected Element " + qName);
						}

					}
						}
			};
		}


		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelResource(this);
			}

		} // finalize

		/**
		 * @inheritDoc
		 */
		public String getUrl()
		{
			return getUrl(false, PROP_ALTERNATE_REFERENCE);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference()
		{
			return getReference(PROP_ALTERNATE_REFERENCE);
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ContentEntity#getUrl(boolean)
		 */
		public String getUrl(boolean relative)
		{
			return getUrl(relative, PROP_ALTERNATE_REFERENCE);
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(boolean relative, String rootProperty)
		{
			return (relative ? m_serverConfigurationService.getAccessPath() : m_serverConfigurationService.getAccessUrl()) 
			+ getAlternateReferenceRoot(rootProperty) + m_relativeAccessPoint
			+ convertIdToUserEid(m_id);
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl(false, rootProperty);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getAlternateReferenceRoot(rootProperty) + m_relativeAccessPoint + m_id;
		}

		/**
		 * Compute an alternate root for a reference, based on the value of the specified property.
		 * 
		 * @param rootProperty
		 *        The property name.
		 * @return The alternate root, or "" if there is none.
		 */
		protected String getAlternateReferenceRoot(String rootProperty)
		{
			// null means don't do this
			if (rootProperty == null) return "";

			// find the property - "" if not found
			String alternateRoot = StringUtil.trimToNull(getProperties().getProperty(PROP_ALTERNATE_REFERENCE));
			if (alternateRoot == null) return "";

			// make sure it start with a separator and does not end with one
			if (!alternateRoot.startsWith(SEPARATOR)) alternateRoot = SEPARATOR + alternateRoot;
			if (alternateRoot.endsWith(SEPARATOR))
				alternateRoot = alternateRoot.substring(0, alternateRoot.length() - SEPARATOR.length());

			return alternateRoot;
		}

		/**
		 * @inheritDoc
		 */
		protected boolean requiresCopyrightAgreement()
		{
			// check my properties
			return m_properties.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null;
		}

		/**
		 * Access the id of the resource.
		 * 
		 * @return The id.
		 */
		public String getId()
		{
			return m_id;

		} // getId

		/**
		 * Access the content byte length.
		 * 
		 * @return The content byte length.
		 */
		public int getContentLength()
		{
			// Use the CHH delegate, if there is one.
			if (chh_vce != null) return ((ContentResource)chh_vce).getContentLength();

			// if we have a body, use it's length
			if (m_body != null) return m_body.length;

			// otherwise, use the content length
			return m_contentLength;

		} // getContentLength

		/**
		 * Access the resource MIME type.
		 * 
		 * @return The resource MIME type.
		 */
		public String getContentType()
		{
			// Use the CHH delegate, if there is one.
			if (chh_vce != null && chh_vce instanceof ContentResource) return ((ContentResource)chh_vce).getContentType();

			return ((m_contentType == null) ? "" : m_contentType);
		} // getContentType

		/**
		 * Access the content bytes of the resource.
		 * 
		 * @return An array containing the bytes of the resource's content.
		 * @exception ServerOverloadException
		 *            if server is configured to store resource body in filesystem and error occurs trying to read from filesystem.
		 */
		public byte[] getContent() throws ServerOverloadException
		{
			// Use the CHH delegate, if there is one.
			if (chh_vce != null) return ((ContentResource)chh_vce).getContent();

			// return the body bytes
			byte[] rv = m_body;

			if ((rv == null) && (m_contentLength > 0))
			{
				// todo: try to get the body from the stream


				// TODO: we do not store the body with the object, so as not to cache the body bytes -ggolden
				rv = m_storage.getResourceBody(this);
				// m_body = rv;
			}

			return rv;

		} // getContent

		/**
		 * Access the content as a stream. Please close the stream when done as it may be holding valuable system resources.
		 * 
		 * @return an InputStream through which the bytes of the resource can be read.
		 */
		public InputStream streamContent() throws ServerOverloadException
		{
			InputStream rv = null;

			if (m_body != null)
			{
				rv = new ByteArrayInputStream(m_body);
			}
			else if (m_contentStream != null)
			{
				return m_contentStream;
			}
			else
			{
				rv = m_storage.streamResourceBody(this);
			}

			return rv;
		}

		/**
		 * Access the resource's properties.
		 * 
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties()
		{
			return m_properties;

		} // getProperties

		/**
		 * Set the resource as removed.
		 */
		protected void setRemoved()
		{
			m_isRemoved = true;

		} // setRemoved

		/**
		 * Set the content byte length.
		 * 
		 * @param length
		 *        The content byte length.
		 */
		public void setContentLength(int length)
		{
			m_contentLength = length;

		} // setContentLength

		/**
		 * Set the resource MIME type.
		 * 
		 * @param type
		 *        The resource MIME type.
		 */
		public void setContentType(String type)
		{
			type = (String) fixTypeAndId(getId(), type).get("type");
			m_contentType = type;

		} // setContentType

		/**
		 * Set the resource content.
		 * 
		 * @param content
		 *        An array containing the bytes of the resource's content.
		 */
		public void setContent(byte[] content)
		{
			if (content == null)
			{
				M_log.warn("setContent(): null content");
				return;
			}

			// only if different
			if (StringUtil.different(content, m_body))
			{
				// take the new body and length
				m_body = content;
				m_contentLength = m_body.length;

				// mark me as having a changed body
				m_bodyUpdated = true;
			}

		} // setContent

		/* (non-Javadoc)
		 * @see org.sakaiproject.content.api.ContentResourceEdit#setContent(java.io.OutputStream)
		 */
		public void setContent(InputStream stream)
		{
			if (stream == null)
			{
				M_log.warn("setContent(): null stream");
				return;
			}

			m_contentStream = stream;
			// m_contentLength = 
		}


		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 * 
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element resource = doc.createElement("resource");

			if (stack.isEmpty())
			{
				doc.appendChild(resource);
			}
			else
			{
				((Element) stack.peek()).appendChild(resource);
			}

			stack.push(resource);

			resource.setAttribute("id", m_id);
			resource.setAttribute("content-type", m_contentType);
			resource.setAttribute("resource-type", m_resourceType);

			// body may not be loaded; if not use m_contentLength
			int contentLength = m_contentLength;
			if (m_body != null) contentLength = m_body.length;
			resource.setAttribute("content-length", Integer.toString(contentLength));

			if (m_filePath != null) resource.setAttribute("filePath", m_filePath);

			// if there's no body bytes (len = 0?) m_body will still be null, so just skip it
			if (m_body != null)
			{
				String enc = null;
				try
				{
					enc = new String(Base64.encodeBase64(m_body),"UTF-8");
				}
				catch (UnsupportedEncodingException e)
				{
					M_log.error(e);
				}
				resource.setAttribute("body", enc);
			}

			// add access
			if(m_access == null || AccessMode.SITE == m_access)
			{
				m_access = AccessMode.INHERITED;
			}
			resource.setAttribute(ACCESS_MODE, m_access.toString());

			resource.setAttribute(HIDDEN, Boolean.toString(m_hidden));
			if(!m_hidden && m_releaseDate != null)
			{
				// add release-date 
				resource.setAttribute(RELEASE_DATE, m_releaseDate.toString());
			}
			if(!m_hidden && m_retractDate != null)
			{
				// add retract-date
				resource.setAttribute(RETRACT_DATE, m_retractDate.toString());
			}

			// properties
			m_properties.toXml(doc, stack);

			stack.pop();

			// add groups
			if ((m_groups != null) && (m_groups.size() > 0))
			{
				Iterator groupIt = m_groups.iterator();
				while( groupIt.hasNext())
				{
					String groupRef = (String) groupIt.next();
					Element sect = doc.createElement(GROUP_LIST);
					sect.setAttribute(GROUP_NAME, groupRef);
					resource.appendChild(sect);
				}
			}

			return resource;

		} // toXml

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * Access the resource's properties for modification
		 * 
		 * @return The resource's properties.
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			return m_properties;

		} // getPropertiesEdit

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;

		} // activate

		/**
		 * Check to see if the edit is still active, or has already been closed.
		 * 
		 * @return true if the edit is active, false if it's been closed.
		 */
		public boolean isActiveEdit()
		{
			return m_active;

		} // isActiveEdit

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;

		} // closeEdit

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		public void valueBound(SessionBindingEvent event)
		{
			m_sessionBound = true;
		}

		public void valueUnbound(SessionBindingEvent event)
		{
			m_sessionBound  = false;
			if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelResource(this);
			}

		} // valueUnbound

		public boolean isResource()
		{
			// TODO: this may need a different implementation in the handler
			return true;
		}

		public boolean isCollection()
		{
			// TODO: this may need a different implementation in the handler
			return false;
		}

		/**
		 * real content resources dont have handlers
		 */
		private ContentHostingHandler chh = null;
		private ContentEntity chh_vce = null; // the wrapped virtual content entity

		public ContentHostingHandler getContentHandler() {return chh;}
		public void setContentHandler(ContentHostingHandler chh) {this.chh = chh;}
		public ContentEntity getVirtualContentEntity() {return chh_vce;}
		public void setVirtualContentEntity(ContentEntity ce) {this.chh_vce = ce;}

		/**
		 * ContentResources cant have members, so this always returns null
		 */
		public ContentEntity getMember(String nextId)
		{
			return null;
		}



		/** Serializable Resource Access */


		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getResourceTypeRegistry()
		 */
		public ResourceTypeRegistry getResourceTypeRegistry()
		{
			return m_resourceTypeRegistry;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableAccess()
		 */
		public AccessMode getSerializableAccess()
		{
			return m_access;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableBody()
		 */
		public byte[] getSerializableBody()
		{
			if ( m_body != null ) {
				M_log.warn("Serializing Body to Entiry Blob, this is bad and will make Sakai crawl");
			}
			return m_body;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableContentLength()
		 */
		public long getSerializableContentLength()
		{
			return m_contentLength;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableContentType()
		 */
		public String getSerializableContentType()
		{
			return m_contentType;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableFilePath()
		 */
		public String getSerializableFilePath()
		{
			return m_filePath;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableGroup()
		 */
		public Collection<String> getSerializableGroup()
		{
			return m_groups;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableHidden()
		 */
		public boolean getSerializableHidden()
		{
			return m_hidden;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableId()
		 */
		public String getSerializableId()
		{
			return m_id;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableProperties()
		 */
		public SerializableEntity getSerializableProperties()
		{
			return (SerializableEntity)m_properties;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableReleaseDate()
		 */
		public Time getSerializableReleaseDate()
		{
			return m_releaseDate;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableResourceType()
		 */
		public String getSerializableResourceType()
		{
			return m_resourceType;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#getSerializableRetractDate()
		 */
		public Time getSerializableRetractDate()
		{
			return m_retractDate;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableAccess(org.sakaiproject.content.api.GroupAwareEntity.AccessMode)
		 */
		public void setSerializableAccess(AccessMode access)
		{
			m_access = access;			
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableBody(byte[])
		 */
		public void setSerializableBody(byte[] body)
		{
			if ( body != null ) {
				M_log.warn("Body serialization from Entity, this is bad and will slow Sakai right down ");
			}
			m_body = body;			
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableContentLength(long)
		 */
		public void setSerializableContentLength(long contentLength)
		{
			if ( contentLength > (long)Integer.MAX_VALUE ) {
				M_log.warn("File is longer than "+Integer.MAX_VALUE+", length may be truncated ");
			}
			m_contentLength = (int)contentLength;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableContentType(java.lang.String)
		 */
		public void setSerializableContentType(String contentType)
		{
			m_contentType = contentType;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableFilePath(java.lang.String)
		 */
		public void setSerializableFilePath(String filePath)
		{
			m_filePath = filePath;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableGroups(java.util.Collection)
		 */
		public void setSerializableGroups(Collection<String> groups)
		{
			m_groups = groups;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableHidden(boolean)
		 */
		public void setSerializableHidden(boolean hidden)
		{
			m_hidden = hidden;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableId(java.lang.String)
		 */
		public void setSerializableId(String id)
		{
			m_id = id;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableReleaseDate(org.sakaiproject.time.api.Time)
		 */
		public void setSerializableReleaseDate(Time releaseDate)
		{
			m_releaseDate = releaseDate;
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableResourceType(java.lang.String)
		 */
		public void setSerializableResourceType(String resourceType)
		{
			m_resourceType = resourceType;			
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableRetractDate(org.sakaiproject.time.api.Time)
		 */
		public void setSerializableRetractDate(Time retractDate)
		{
			m_retractDate = retractDate;
		}


		public void unbind() {
			if ( !m_sessionBound && m_active ) {
				M_log.warn("Edit Object not closed correctly, Cancelling "+this.getId());
				cancelResource(this);
			}			
		}

	} // BaseResourceEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open and be ready to read / write.
		 */
		public void open();

		/**
		 * Get a count of all members of a collection, where 'member' means the collection
		 * is the immediate parent of the item.  The count is not recursive and it will 
		 * include all resources and collections whose immediate parent is the collection
		 * identified by the parameter.
		 */
		public int getMemberCount(String collectionId);

		/** 
		 * Access a collection of string identifiers for all ContentResource entities
		 * that are members of the ContentCollection identified by the parameter.
		 * @param collectionId
		 * @return
		 */
		public Collection<String> getMemberResourceIds(String collectionId);

		/** 
		 * Access a collection of string identifiers for all ContentCollection entities
		 * that are members of the ContentCollection identified by the parameter.
		 * @param collectionId
		 * @return
		 */
		public Collection<String> getMemberCollectionIds(String collectionId);

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Return the identified collection, or null if not found.
		 */
		public ContentCollection getCollection(String id);

		/**
		 * Return true if the identified collection exists.
		 */
		public boolean checkCollection(String id);

		/**
		 * Get a list of all getCollections within a collection.
		 */
		public List getCollections(ContentCollection collection);

		/**
		 * Keep a new collection.
		 */
		public ContentCollectionEdit putCollection(String collectionId);

		/**
		 * Get a collection locked for update
		 */
		public ContentCollectionEdit editCollection(String collectionId);

		/**
		 * Commit a collection edit.
		 */
		public void commitCollection(ContentCollectionEdit edit);

		/**
		 * Cancel a collection edit.
		 */
		public void cancelCollection(ContentCollectionEdit edit);

		/**
		 * Forget about a collection.
		 */
		public void removeCollection(ContentCollectionEdit collection);

		/**
		 * Return the identified resource, or null if not found.
		 * @throws TypeException 
		 */
		public ContentResource getResource(String id) throws TypeException;

		/**
		 * Return true if the identified resource exists.
		 */
		public boolean checkResource(String id);

		/**
		 * Get a list of all resources within a collection.
		 */
		public List getResources(ContentCollection collection);

		/**
		 * 
		 * @param collectionId
		 * @return
		 */
		public List getFlatResources(String collectionId);

		/**
		 * Keep a new resource.
		 */
		public ContentResourceEdit putResource(String resourceId);

		/**
		 * Get a resource locked for update
		 */
		public ContentResourceEdit editResource(String resourceId);

		/**
		 * Commit a resource edit.
		 */
		public void commitResource(ContentResourceEdit edit) throws ServerOverloadException;

		/**
		 * Cancel a resource edit.
		 */
		public void cancelResource(ContentResourceEdit edit);

		/**
		 * Forget about a resource.
		 */
		public void removeResource(ContentResourceEdit resource);

		/**
		 * Read the resource's body.
		 * 
		 * @exception ServerOverloadException
		 *            if server is configured to save resource body in filesystem and an error occurs while trying to access the filesystem.
		 */
		public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException;

		/**
		 * Stream the resource's body.
		 * 
		 * @exception ServerOverloadException
		 *            if server is configured to save resource body in filesystem and an error occurs while trying to access the filesystem.
		 */
		public InputStream streamResourceBody(ContentResource resource) throws ServerOverloadException;

		/**
		 * Return a single character representing the access mode of the resource or collection identified by the parameter, or null if not found.
		 * @param id
		 * @return A character identifying the access mode for the content entity, one of 's' for site, 'p' for public or 'g' for group.
		 */
		//public char getAccessMode(String id);

		// htripath-storing into shadow table before deleting the resource
		public void commitDeleteResource(ContentResourceEdit edit, String uuid);

		public ContentResourceEdit putDeleteResource(String resourceId, String uuid, String userId);

		/**
		 * Retrieve a collection of ContentResource objects pf a particular resource-type.  The collection will 
		 * contain no more than the number of items specified as the pageSize, where pageSize is a non-negative 
		 * number less than or equal to 1028. The resources will be selected in ascending order by resource-id.
		 * If the resources of the specified resource-type in the ContentHostingService in ascending order by 
		 * resource-id are indexed from 0 to M and this method is called with parameters of N for pageSize and 
		 * I for page, the resources returned will be those with indexes (I*N) through ((I+1)*N - 1).  For example,
		 * if pageSize is 1028 and page is 0, the resources would be those with indexes of 0 to 1027.  
		 * @param resourceType
		 * @param pageSize
		 * @param page
		 * @return
		 */
		public Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page);

	} // Storage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * CacheRefresher implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Get a new value for this key whose value has already expired in the cache.
	 * 
	 * @param key
	 *        The key whose value has expired and needs to be refreshed.
	 * @param oldValue
	 *        The old exipred value of the key.
	 * @param event
	 *        The event which triggered this refresh.
	 * @return a new value for use in the cache for this key; if null, the entry will be removed.
	 */
	public Object refresh(Object key, Object oldValue, Event event)
	{
		Object rv = null;

		// key is a reference
		Reference ref = m_entityManager.newReference((String) key);
		String id = ref.getId();

		if (M_log.isDebugEnabled()) M_log.debug("refresh(): key " + key + " id : " + ref.getId());

		// get from storage only (not cache!)
		boolean collectionHint = id.endsWith(Entity.SEPARATOR);
		if (collectionHint)
		{
			rv = m_storage.getCollection(id);
		}
		else
		{
			try
			{
				rv = m_storage.getResource(id);
			}
			catch (TypeException e)
			{
				M_log.error("Type Exception",e);
			}
		}

		return rv;

	} // refresh


	/* Content Hosting Handlers are not implemented in the Base Content Service */
	public boolean isContentHostingHandlersEnabled()
	{
		return false;
	} // isContentHostingHandlersEnabled


	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.SiteContentAdvisorProvider#getContentAdvisor(org.sakaiproject.site.api.Site)
	 */
	public SiteContentAdvisor getContentAdvisor(Site site)
	{
		if ( site == null ) {
			return null;
		}
		SiteContentAdvisorProvider scap = siteContentAdvisorsProviders.get(site.getType());
		if ( scap == null ) {
			return null;
		}
		return scap.getContentAdvisor(site);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.SiteContentAdvisorTypeRegistry#registerSiteContentAdvisorProvidor(org.sakaiproject.content.api.SiteContentAdvisorProvider, java.lang.String)
	 */
	public void registerSiteContentAdvisorProvidor(SiteContentAdvisorProvider advisor, String type)
	{
		siteContentAdvisorsProviders.put(type, advisor);		
	}

	/**
	 * @return the collectionSerializer
	 */
	public EntitySerializer getCollectionSerializer()
	{
		return collectionSerializer;
	}

	/**
	 * @param collectionSerializer the collectionSerializer to set
	 */
	public void setCollectionSerializer(EntitySerializer collectionSerializer)
	{
		this.collectionSerializer = collectionSerializer;
	}

	/**
	 * @return the resourceSerializer
	 */
	public EntitySerializer getResourceSerializer()
	{
		return resourceSerializer;
	}

	/**
	 * @param resourceSerializer the resourceSerializer to set
	 */
	public void setResourceSerializer(EntitySerializer resourceSerializer)
	{
		this.resourceSerializer = resourceSerializer;
	}


	protected long getSizeForContext(String context) 
	{
		return 0;
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		try
		{
			if(cleanup == true)
			{
				// Get the root collection
				ContentCollection oCollection = getCollection(toContext);

				if(oCollection != null)
				{
					// Get the collection members from the old collection
					List oResources = oCollection.getMemberResources();

					for (int i = 0; i < oResources.size(); i++)
					{
						// Get the original resource
						Entity oResource = (Entity) oResources.get(i);

						String oId = oResource.getId();

						ResourceProperties oProperties = oResource.getProperties();

						boolean isCollection = false;

						try
						{
							isCollection = oProperties.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION);
						}
						catch (Exception e)
						{
							M_log.debug("Get Folder Collection" + e);
						}

						if (isCollection)
						{
							try
							{
								ContentCollectionEdit edit = editCollection(oId);

								m_storage.removeCollection(edit);
							}
							catch (Exception ee)
							{
								M_log.debug("remove folders resources" + ee);
							}
						}
						else 
						{
							try
							{
								BaseResourceEdit edit = (BaseResourceEdit) editResourceForDelete(oId);

								m_storage.removeResource(edit);
							}
							catch (Exception ee)
							{
								M_log.debug("remove others resources" + ee);
							}
						}

					}


				}
			}
		}
		catch (Exception e)
		{
			M_log.debug("BaseContentService Resources transferCopyEntities Error" + e);
		}
		transferCopyEntities(fromContext, toContext, ids);
	}
} // BaseContentService

