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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.mime.MimeTypes;

import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.antivirus.api.VirusFoundException;
import org.sakaiproject.antivirus.api.VirusScanIncompleteException;
import org.sakaiproject.antivirus.api.VirusScanner;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conditions.api.ConditionService;
import org.sakaiproject.content.api.*;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.api.providers.SiteContentAdvisor;
import org.sakaiproject.content.api.providers.SiteContentAdvisorProvider;
import org.sakaiproject.content.api.providers.SiteContentAdvisorTypeRegistry;
import org.sakaiproject.content.impl.serialize.api.SerializableCollectionAccess;
import org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess;
import org.sakaiproject.content.util.ZipContentUtil;
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
import org.sakaiproject.entity.api.EntityTransferrerRefMigrator;
import org.sakaiproject.entity.api.HardDeleteAware;
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
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
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
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadBound;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.DefaultEntityHandler;
import org.sakaiproject.util.Resource;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SAXEntityReader;
import org.sakaiproject.util.SingleStorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.sakaiproject.util.api.LinkMigrationHelper;

/**
 * <p>
 * BaseContentService is an abstract base implementation of the Sakai ContentHostingService.
 * </p>
 */
@Slf4j
public abstract class BaseContentService implements ContentHostingService, CacheRefresher, ContextObserver, EntityTransferrer, 
SiteContentAdvisorProvider, SiteContentAdvisorTypeRegistry, EntityTransferrerRefMigrator, HardDeleteAware
{
	protected static final long END_OF_TIME = 8000L * 365L * 24L * 60L * 60L * 1000L;
	protected static final long START_OF_TIME = 365L * 24L * 60L * 60L * 1000L;

	/** Maximum length of a URL which we allow for redirection 
	 * (c/f http://www.boutell.com/newfaq/misc/urllength.html) */
	protected static final long MAX_URL_LENGTH = 8192;

	protected static final Pattern contextPattern = Pattern.compile("\\A/(group/|user/|~)(.+?)/");

	/** sakai.properties setting to enable secure inline html (true by default) */
	protected static final String SECURE_INLINE_HTML = "content.html.forcedownload";
	
	private static final String PROP_AVAIL_NOTI = "availableNotified";

	/** MIME multipart separation string */
	protected static final String MIME_SEPARATOR = "SAKAI_MIME_BOUNDARY";

    protected static final String DEFAULT_RESOURCE_QUOTA = "content.quota.";
    protected static final String DEFAULT_DROPBOX_QUOTA = "content.dropbox.quota.";

    /**
     * This is the name of the sakai.properties property for the VIRUS_SCAN_PERIOD,
     * this is how long (in seconds) the virus scan service will wait between checking to see if there
     * is new content that need to be scanned, default=3600
     */
    public static final String VIRUS_SCAN_CHECK_PERIOD_PROPERTY = "virus.scan.check.period";

    /**
     * This is the name of the sakai.properties property for the VIRUS_SCAN_DELAY,
     * this is how long (in seconds) the virus scan service will wait after starting up
     * before it does the first check for scanning, default=300
     */
    public static final String VIRUS_SCAN_START_DELAY_PROPERTY = "virus.scan.start.delay";


	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** A Storage object for persistent storage. */
	protected Storage m_storage = null;

	/**
	 * The quota for content resource body bytes (in Kbytes) for any hierarchy in the /user/ or /group/ areas, or 0 if quotas are not enforced.
	 */
	protected long m_siteQuota = 1048576;
    /**
     * The quota for content dropbox body bytes (in Kbytes), or 0 if quotas are not enforced.
     */
	protected long m_dropBoxQuota = 1048576;

	private boolean m_useSmartSort = true;

	private boolean m_useMimeMagic = true;

	List <String> m_ignoreExtensions = null;
	List <String> m_ignoreMimeTypes = null;

	private static final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());
	
	// This is the date format for Last-Modified header
	public static final String RFC1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final Locale LOCALE_US = Locale.US;

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
	protected String m_bodyPathDeleted = null;

	/** Optional set of folders just within the m_bodyPath to distribute files among. */
	protected String[] m_bodyVolumes = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: MemoryService. */
	protected MemoryService m_memoryService = null;

    	/**
	 * Use a timer for repeating actions
	 */
	private Timer virusScanTimer = new Timer(true);

    /** How long to wait between virus scan checks (seconds) */
    private int VIRUS_SCAN_PERIOD = 300;

    /** How long to wait between virus scan checks (seconds) */
    public void setVIRUS_SCAN_PERIOD(int scan_period) {
        VIRUS_SCAN_PERIOD = scan_period;
    }

    /** How long to wait before the first virus scan check (seconds) */
    private int VIRUS_SCAN_DELAY = 300;
    /** How long to wait before the first virus scan check (seconds) */
    public void setVIRUS_SCAN_DELAY(int virus_scan_delay) {
        VIRUS_SCAN_DELAY = virus_scan_delay;
    }

    private List<String> virusScanQueue = new Vector();

    private final static SecurityAdvisor ALLOW_ADVISOR;

	static {
		ALLOW_ADVISOR = new SecurityAdvisor(){
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		};
	}

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

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
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

	protected LinkMigrationHelper linkMigrationHelper;
	
	public void setLinkMigrationHelper(LinkMigrationHelper linkMigrationHelper) {
		this.linkMigrationHelper = linkMigrationHelper;
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
	
	
	private IdManager idManager;
	
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
	
	private FunctionManager functionManager;
	
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}
	
	private ThreadLocalManager threadLocalManager;
	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
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

	
	protected ContentTypeImageService contentTypeImageService;
	
	public void setContentTypeImageService(ContentTypeImageService contentTypeImageService) {
		this.contentTypeImageService = contentTypeImageService;
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

	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
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

	/** Dependency: CollectionAccessFormatter. */
	protected CollectionAccessFormatter m_collectionAccessFormatter = null;

	/**
	 * Dependency: CollectionAccessFormatter.
	 *
	 * @param service
	 *        The CollectionAccessFormatter.
	 */
	public void setCollectionAccessFormatter(CollectionAccessFormatter service)
	{
		m_collectionAccessFormatter = service;
	}

	/** Dependency: ContentFilterService */
	protected ContentFilterService m_contentFilterService;

	/**
	 * Dependency: ContentFilterService.
	 *
	 * @param service
	 *        The ContentFilterService.
	 */
	public void setContentFilterService(ContentFilterService service)
	{
		m_contentFilterService = service;
	}


	/**
	 * Set the site quota.
	 * 
	 * @param quota
	 *        The site quota (as a string).
	 */
	public void setSiteQuota(Long quota)
	{
		if (quota != null)
		{
			m_siteQuota = quota;
		}
	}
	
	/**
	 * Set the dropbox quota.
	 * 
	 * @param quota
	 *        The dropbox quota (as a string).
	 */
	public void setDropBoxQuota(Long quota)
	{
		if (quota != null)
		{
			m_dropBoxQuota = quota;
		}
	}
	
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
	private VirusScanner virusScanner;

	public void setVirusScanner(VirusScanner virusScanner) {
		this.virusScanner = virusScanner;
	}

	private TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
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
			m_siteAttachments = Boolean.valueOf(value).booleanValue();
		}
		catch (Exception t)
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
	 * Configuration: set the external file system path for body storage for deleted files. 
	 * 
	 * @param value
	 *        The complete path to the root of the external file system storage area for resource body bytes of deleted resources.
	 */
	public void setBodyPathDeleted(String value)
	{
		m_bodyPathDeleted = value;
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
		catch (Exception t)
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
	public void setShortRefs(boolean value)
	{
			m_shortRefs = value;
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
			m_siteAlias = Boolean.valueOf(value).booleanValue();
		}
		catch (Exception t)
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

			log.info("Loaded Storage as "+m_storage+" for "+this);

			// register a transient notification for resources
			NotificationEdit edit = m_notificationService.addTransientNotification();

			// set functions
			edit.setFunction(EVENT_RESOURCE_AVAILABLE);
			edit.addFunction(EVENT_RESOURCE_WRITE);

			// set the filter to any site related resource
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + "group" + Entity.SEPARATOR);
			// %%% is this the best we can do? -ggolden

			// set the action
			SiteEmailNotificationContent siteEmailNotificationContent = new SiteEmailNotificationContent(m_securityService,
					m_serverConfigurationService, this, m_entityManager, m_siteService);
			edit.setAction(siteEmailNotificationContent);

			NotificationEdit dbNoti = m_notificationService.addTransientNotification();

			// set functions
			dbNoti.setFunction(EVENT_RESOURCE_AVAILABLE);
			dbNoti.addFunction(EVENT_RESOURCE_WRITE);

			// set the filter to any site related resource
			dbNoti.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + "group-user" + Entity.SEPARATOR);
			// %%% is this the best we can do? -ggolden

			// set the action
			DropboxNotification dropboxNotification = new DropboxNotification(m_securityService, this, m_entityManager, m_siteService,
					userDirectoryService, m_serverConfigurationService);
			dbNoti.setAction(dropboxNotification);

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
			functionManager.registerFunction(AUTH_RESOURCE_ADD, true);
			functionManager.registerFunction(AUTH_RESOURCE_READ, true);
			functionManager.registerFunction(AUTH_RESOURCE_WRITE_ANY, true);
			functionManager.registerFunction(AUTH_RESOURCE_WRITE_OWN, true);
			functionManager.registerFunction(AUTH_RESOURCE_REMOVE_ANY, true);
			functionManager.registerFunction(AUTH_RESOURCE_REMOVE_OWN, true);
			functionManager.registerFunction(AUTH_RESOURCE_ALL_GROUPS, true);
			functionManager.registerFunction(AUTH_RESOURCE_HIDDEN, true);

			functionManager.registerFunction(AUTH_DROPBOX_OWN, false);
			functionManager.registerFunction(AUTH_DROPBOX_GROUPS, false);
			functionManager.registerFunction(AUTH_DROPBOX_MAINTAIN, false);

			// quotas
			m_siteQuota = Long.parseLong(m_serverConfigurationService.getString("content.quota", Long.toString(m_siteQuota)));
            m_dropBoxQuota = Long.parseLong(m_serverConfigurationService.getString("content.dropbox.quota", Long.toString(m_dropBoxQuota)));

			log.info("init(): site quota: " + m_siteQuota + ", dropbox quota: " + m_dropBoxQuota + ", body path: " + m_bodyPath + " volumes: "+ buf.toString());

            int virusScanPeriod = m_serverConfigurationService.getInt(VIRUS_SCAN_CHECK_PERIOD_PROPERTY, VIRUS_SCAN_PERIOD);
            int virusScanDelay = m_serverConfigurationService.getInt(VIRUS_SCAN_START_DELAY_PROPERTY, VIRUS_SCAN_DELAY);

            virusScanDelay += new Random().nextInt(60); // add some random delay to get the servers out of sync
            virusScanTimer.schedule(new VirusTimerTask(), (virusScanDelay * 1000), (virusScanPeriod * 1000) );

		}
		catch (Exception t)
		{
			log.error("init(): ", t);
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

		log.info("destroy()");

	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation - for collections
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Storage user for collections - in the resource side, not container
	 */
	protected class CollectionStorageUser implements SingleStorageUser, SAXEntityReader, EntityReaderHandler, EntityReader
	{
		private Map<String,Object> m_services;

		private EntityReaderHandler entityReaderAdapter;

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
								log.warn("Unexpected Element in XML [" + qName + "]");
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
	protected class ResourceStorageUser implements SingleStorageUser, SAXEntityReader, EntityReaderHandler, EntityReader
	{
		private Map<String, Object> m_services;

		private EntityReaderHandler entityReaderAdapter;

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
					rv[2] = Long.valueOf(((ContentResource) r).getContentLength());
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
					rv[2] = Long.valueOf(((ContentResource) r).getContentLength());
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
								log.warn("Unexpected Element in XML [" + qName + "]");
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
			ResourceProperties props = bre.getProperties();
			if(props != null) {
				String oldDisplayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				bre.setOldDisplayName(oldDisplayName);
			}
			return bre;
		}
		/* (non-Javadoc)
		 * @see org.sakaiproject.entity.api.serialize.EntityReaderHandler#parse(org.sakaiproject.entity.api.Entity, java.lang.String, byte[])
		 */
		public Entity parse(Entity container, String xml, byte[] blob) throws EntityParseException
		{
			BaseResourceEdit bre = new BaseResourceEdit();
			resourceSerializer.parse(bre,blob);
			ResourceProperties props = bre.getProperties();
			if(props != null) {
				String oldDisplayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				bre.setOldDisplayName(oldDisplayName);
			}
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
		//the id could be null
		if (id == null) {
			return null;
		}
		
		// if this resource is a dropbox, you need dropbox maintain permission
		// Changed in SAK-11647 to enable group-aware dropboxes
		if (id.startsWith(COLLECTION_DROPBOX))
		{
			// only for /group-user/SITEID/USERID/ refs.
			String[] parts = StringUtil.split(id, "/");
			if (parts.length >= 3)
			{
				boolean authDropboxGroupsCheck=true;
				String ref = getReference(id);

				if (parts.length>=4)
				{
					//Http servlet access to dropbox resources
					String userId=parts[3];
					if ((userId==null) || isDropboxMaintainer(parts[2]) || (!isDropboxOwnerInCurrentUserGroups(ref,userId)))
					{
						authDropboxGroupsCheck=false;
					}
				}

				//Before SAK-11647 any dropbox id asked for dropbox.maintain permission.
				//Now we must support groups permission, so we ask for this permission too.
				//Groups permission gives full access to dropboxes of users in current user's groups. 
				//A different logic can be achieved here depending of lock parameter received.
				if (m_securityService.unlock(AUTH_DROPBOX_GROUPS, ref))
				{
					if (authDropboxGroupsCheck)
					{
						return AUTH_DROPBOX_GROUPS;
					}
					else
					{
						return AUTH_DROPBOX_MAINTAIN;
					}
				}
				else
				{
					return AUTH_DROPBOX_MAINTAIN;
				}
			}
		}

		return lock;
	}
	
	/**
	 * Checks if a dropbox owner is in any group with current user, so AUTH_DROPBOX_GROUPS is rightly applied.
	 * @return true if the dropbox owner is in the group, false otherwise. 
	 */
	public boolean isDropboxOwnerInCurrentUserGroups(String refString, String userId)
	{
		String currentUser = sessionManager.getCurrentSessionUserId();
		
		List<Group> site_groups = new ArrayList<Group>();
		Reference ref = m_entityManager.newReference(refString);
		try
		{
			Site site = m_siteService.getSite(ref.getContext());
	
			site_groups.addAll(site.getGroupsWithMembers(new String[]{currentUser,userId}));
			if (site_groups.size()>0)
			{
				return true;
			}
		}
		catch (IdUnusedException e)
		{
		}
		
		return false;
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

	public boolean isInSiteCollection(String entityId)
	{
		return entityId.startsWith(COLLECTION_SITE);
	}

	public boolean isSiteLevelCollection(String id)
	{
		boolean isSiteLevelCollection = (id != null) && isInSiteCollection(id);
		if(isSiteLevelCollection)
		{
			String[] parts = id.split(Entity.SEPARATOR);
			isSiteLevelCollection = parts.length == 3 ;
		}
		return isSiteLevelCollection;
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
		boolean available = (! m_availabilityChecksEnabled) || (isAttachmentResource(id) && !isCollection(id));
		// while site owners can validly look at attachment collections, it's odd, and there's no 
		// way in UI that we know to do it. However admins can definitely see it from resources
		// so warn except for admins. This check will return true for site owners even though
		// the warning is issued.
		if (isAttachmentResource(id) && isCollection(id) && !m_securityService.isSuperUser())
		    log.warn("availability check for attachment collection " + id);

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
						log.warn("trying to get collection, found resource: " + id);
					}
					else
					{
						log.warn("trying to get resource, found collection: " + id);
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
			String userId = sessionManager.getCurrentSessionUserId();
			
			// if we are in a roleswapped state, we want to ignore the creator check since it would not necessarily reflect an alternate role
			// FIXME - unsafe check (vulnerable to collision of siteids that are the same as path elements in a resource)
			String[] refs = StringUtil.split(id, Entity.SEPARATOR);
			String roleswap = null;
			for (int i = 0; i < refs.length; i++)
			{
				roleswap = m_securityService.getUserEffectiveRole("/site/" + refs[i]);
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
				available = m_securityService.unlock(lock, entity.getReference());

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
		boolean isAllowed = m_securityService.isSuperUser();
		if(! isAllowed)
		{
			//SAK-11647 - Changes in this function.
			lock = convertLockIfDropbox(lock, id);

			// make a reference from the resource id, if specified
			String ref = null;
			if (id != null)
			{
				ref = getReference(id);
			}

			isAllowed = ref != null && m_securityService.unlock(lock, ref);

			if(isAllowed && lock != null && (lock.startsWith("content.") || lock.startsWith("dropbox.")) && m_availabilityChecksEnabled)
			{
				try 
				{
					isAllowed = availabilityCheck(id);
				} 
				catch (IdUnusedException e) 
				{
					// ignore because we would have caught this earlier.
					log.debug("BaseContentService.unlockCheck(" + lock + "," + id + ") IdUnusedException " + e);
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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), "remove", id);
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
		if(m_securityService.isSuperUser())
		{
			return;
		}

		//SAK-11647 - Changes in this function.
		lock = convertLockIfDropbox(lock, id);

		// make a reference from the resource id, if specified
		String ref = null;
		if (id != null)
		{
			ref = getReference(id);
		}

		if (!m_securityService.unlock(lock, ref))
		{
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), lock, ref);
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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), lock, ref);
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
		String current = sessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_CREATOR, current);
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = timeService.newTime().toString();
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
		String current = sessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = timeService.newTime().toString();
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

		String current = sessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_CREATOR, current);
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = timeService.newTime().toString();
		p.addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		p.addProperty(ResourceProperties.PROP_MODIFIED_DATE, now);

		p.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long.toString(r.getContentLength()));
		p.addProperty(ResourceProperties.PROP_CONTENT_TYPE, r.getContentType());

		p.addProperty(ResourceProperties.PROP_IS_COLLECTION, "false");

		if (StringUtils.isBlank(p.getProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE))) {
			String copyright = m_serverConfigurationService.getString("copyright.type.default", "not_determined");
			// if copyright is null don't set a default copyright
			if (copyright != null) {
				String[] copyrightTypes = m_serverConfigurationService.getStrings("copyright.types");
				if (copyrightTypes != null && copyrightTypes.length > 0) {
					List<String> l = Arrays.asList(copyrightTypes);
					if (l.contains(copyright)) {
						p.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, copyright);
					} else {
						log.warn("Cannot set the default copyright " + copyright + " on " + r.getId() + " does not match any copyright types");
					}
				} else {
					log.warn("Cannot set the default copyright " + copyright + " on " + r.getId() + " no copyright types are defined");
				}
			}
		}

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

		String current = sessionManager.getCurrentSessionUserId();
		p.addProperty(ResourceProperties.PROP_MODIFIED_BY, current);

		String now = timeService.newTime().toString();
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

		String current = sessionManager.getCurrentSessionUserId();
		String now = timeService.newTime().toString();

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
		threadLocalManager.set("members@" + containerId, null);
		threadLocalManager.set("getCollections@" + containerId, null);
		//threadLocalManager.set("getResources@" + containerId, null);

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
			throw new IdLengthException(id, MAXIMUM_RESOURCE_ID_LENGTH);
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
	 * @param collectionId
	 * @param name
	 * @param limit
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
	 * @exception IdUniquenessException
	 *            if we still can't find a unique after 99 attempts at incrementing the id.
	 */
	public ContentCollectionEdit addCollection(String collectionId, String name, int limit)
			throws PermissionException, IdUnusedException, IdUsedException,
			IdLengthException, IdInvalidException, TypeException, IdUniquenessException
	{
		String new_name = "";

		try
		{
			return addCollection(collectionId, name);
		}
		catch (IdUsedException e)
		{
			log.debug("Failed to create collection with id {} will now try incrementing id", new_name);

			for (int attempts = 1; attempts <= limit; attempts++)
			{
				new_name = StringUtils.trim(name) + "-" + attempts;
				try
				{
					return addCollection(collectionId, new_name);
				}
				catch (IdUsedException ee)
				{
					log.debug("Failed to create unique collection with name: {}", new_name);
				}
			}
		}

		throw new IdUniquenessException(new_name);
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
	 * Access a List of all the deleted ContentResource objects in this path (and below) which the current user has access.
	 * 
	 * @param id
	 *        A collection id.
	 * @return a List of the ContentResource objects.
	 * @throws PermissionException 
	 * @throws TypeException 
	 * @throws IdUnusedException 
	 */
	public List getAllDeletedResources(String id)
	{
		List rv = new ArrayList();
		try {
			ContentCollection collection = getCollection(id);
			if ( unlockCheck(AUTH_RESOURCE_WRITE_ANY, id) ) {
				return m_storage.getDeletedResources(collection);
			} else {
				List l = m_storage.getDeletedResources(collection);
				String currentUserId = sessionManager.getCurrentSessionUserId(); 
				// Check if the file was removed by the current user
				for (Object o:l) {
					BaseResourceEdit e = (BaseResourceEdit)o;
					String removedBy = e.getProperties().getProperty(ResourceProperties.PROP_CREATOR);
					if (removedBy != null && removedBy.equals(currentUserId)) {
						rv.add(e);
					}
				}
			}
		} catch (IdUnusedException iue) {
			log.warn("getAllDeletedResources: cannot retrieve collection for : " + id);
		} catch (TypeException te) {
			log.warn("getAllDeletedResources: resource with id: " + id + " not a collection");
		} catch (PermissionException pe) {
			log.warn("getAllDeletedResources: access to resource with id: " + id + " failed : " + pe);
		}
		return rv;
	} // getAllDeletedResources


	/**
	 * Access a List of all the ContentResource objects in this path (and below) which the current user has access.
	 * 
	 * @param id
	 *        A collection id.
	 * @return a List of the ContentResource objects.
	 */
	public List<ContentResource> getAllResources(String id)
	{
		List<ContentResource> rv = new ArrayList<ContentResource>();

		if (isRootCollection(id))
		{
			// There are performance issues with returning every single resources in one collection as well
			// as issues in Sakai where actions incorrectly happen for the whole of the content service
			// instead of just those of a site.
			throw new IllegalArgumentException("Fetching of "+ id+ " is not allowed");
		}

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
			collection = (ContentCollection) threadLocalManager.get("findCollection@" + id);
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
				threadLocalManager.set("findCollection@" + id, collection);	// new BaseCollectionEdit(collection));
			}
		}
		else
		{
			collection = new BaseCollectionEdit(collection);
		}

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
		String currentUser = sessionManager.getCurrentSessionUserId();
		String owner = "";

		if (m_securityService.isSuperUser(currentUser)) {
			//supper users should always get a 404 rather than a permission exception
			return true;
		}
		
		try
		{
			ResourceProperties props = getProperties(id);
			owner = props.getProperty(ResourceProperties.PROP_CREATOR);
		}
		catch (PermissionException e ) 
		{
			// PermissionException can be thrown if not AUTH_RESOURCE_READ
			return false;
		} catch (IdUnusedException e) {
			//Also non admin users should get a permission exception is the resource doesn't exist
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
		
		String currentUser = sessionManager.getCurrentSessionUserId();
		String owner = "";
		
		//Supper users always have the permission
		if (m_securityService.isSuperUser()) {
			return true;
		}
		
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
			log.error("removeCollection(): closed ContentCollectionEdit", e);
			return;
		}

		// check security 
		if ( ! allowRemoveCollection(edit.getId()) )
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, edit.getReference());

		// clear thread-local cache SAK-12126
		threadLocalManager.set("members@" + edit.getId(), null);
		threadLocalManager.set("getResources@" + edit.getId(), null);
		threadLocalManager.set("getCollections@" + edit.getId(), null);

		// check for members
		List members = edit.getMemberResources();
		if (!members.isEmpty()) throw new InconsistentException(edit.getId());

		// complete the edit
		m_storage.removeCollection(edit);

		// close the edit object
		((BaseCollectionEdit) edit).closeEdit();

		((BaseCollectionEdit) edit).setRemoved();

		// remove the old version from thread-local cache.
		threadLocalManager.set("findCollection@" + edit.getId(), null);

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(edit.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			log.debug("removeCollection: removing realm for : " + edit.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
			log.debug("removeCollection: removing realm for : " + edit.getReference() + " : " + ignore);
		}

		// track it (no notification)
		String ref = edit.getReference(null);
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, ref, true,
				NotificationService.NOTI_NONE));
		eventTrackingService.cancelDelays(ref);

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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
		threadLocalManager.set("members@" + id, null);
		threadLocalManager.set("getResources@" + id, null);
		threadLocalManager.set("getCollections@" + edit.getId(), null);

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
			log.error("removeCollection():", e);
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
			log.error("commitCollection(): closed ContentCollectionEdit", e);
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

		if(((BasicGroupAwareEdit) edit).isVisibilityUpdated()) {
			// post EVENT_RESOURCE_UPD_VISIBILITY event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(EVENT_RESOURCE_UPD_VISIBILITY, edit.getReference(), true));
		}
		
		if(((BasicGroupAwareEdit) edit).isAccessUpdated()) {
			// post EVENT_RESOURCE_UPD_ACCESS event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(EVENT_RESOURCE_UPD_ACCESS, edit.getReference(), true));
		}
		
		// update the properties for update
		addLiveUpdateCollectionProperties(edit);

		// complete the edit
		m_storage.commitCollection(edit);

		// close the edit object
		((BaseCollectionEdit) edit).closeEdit();

		// the collection has changed so we must remove the old version from thread-local cache
		threadLocalManager.set("findCollection@" + edit.getId(), null);
		String containerId = isolateContainingId(edit.getId());
		threadLocalManager.set("findCollection@" + containerId, null);
		threadLocalManager.set("members@" + containerId, null);
		threadLocalManager.set("getCollections@" + containerId, null);
		//threadLocalManager.set("getResources@" + containerId, null);

		// track it (no notification)
		String ref = edit.getReference(null);
		eventTrackingService.post(eventTrackingService.newEvent(((BaseCollectionEdit) edit)
				.getEvent(), ref, true, NotificationService.NOTI_NONE));

	} // commitCollection

	private void postAvailableEvent(GroupAwareEntity entity, String ref, int priority)
	{
		// cancel all scheduled available events for this entity. 
		eventTrackingService.cancelDelays(ref, EVENT_RESOURCE_AVAILABLE);

		if ( ! entity.isAvailable() )
		{
			// schedule an event to tell when resource becomes available
			if (entity.getReleaseDate() != null)
			{
				eventTrackingService.delay(eventTrackingService.newEvent(EVENT_RESOURCE_AVAILABLE, ref,
																							false, priority), entity.getReleaseDate());
				entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.FALSE.toString());
			}
			// schedule an event to tell when resource becomes unavailable
			if ( entity.getRetractDate() != null )
			{
				eventTrackingService.delay(eventTrackingService.newEvent(EVENT_RESOURCE_UNAVAILABLE, ref,
																							false, priority), entity.getRetractDate());
				entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.FALSE.toString());
			}
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
				eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_AVAILABLE,
						ref, false, priority));
				entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.TRUE.toString());
			}
			
			// schedule an event to tell when resource becomes unavailable
			if ( entity.getRetractDate() != null )
			{
				eventTrackingService.delay(eventTrackingService.newEvent(EVENT_RESOURCE_UNAVAILABLE, ref,
																							false, priority), entity.getRetractDate());
				entity.getProperties().addProperty(PROP_AVAIL_NOTI, Boolean.FALSE.toString());
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
					log.error("verifyGroups(): ", e);
				} 
				catch (PermissionException e) 
				{
					// If user has permission to change groups in superfolder, this should not occur here
					m_storage.cancelResource(edit);
					log.error("verifyGroups(): ", e);
				} 
				catch (ServerOverloadException e) 
				{
					log.error("verifyGroups(): ", e);
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
					log.error("verifyGroups(): ", e);
				} 
				catch (PermissionException e) 
				{
					// If user has permission to change groups in superfolder, this should not occur here
					m_storage.cancelCollection(edit);
					log.error("verifyGroups(): ", e);
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
			log.error("cancelCollection(): closed ContentCollectionEdit", e);
			return;
		}

		// release the edit lock
		m_storage.cancelCollection(edit);

		// if the edit is newly created during an add collection process, remove it from the storage
    String event = ((BaseCollectionEdit) edit).getEvent();
		if (EVENT_RESOURCE_ADD.equals(event))
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
				log.error("failed to removed canceled collection child", e);
			}
			catch (TypeException e)
			{
				log.error("failed to removed canceled collection child", e);
			}
			catch (PermissionException e)
			{
				log.error("failed to removed canceled collection child", e);
			}
			catch (InUseException e)
			{
				log.error("failed to removed canceled collection child", e);
			}
			catch (ServerOverloadException e)
			{
				log.error("failed to removed canceled collection child", e);
			}
		}
	}

	protected void cacheEntities(List<? extends ContentEntity> entities)
	{
		if(entities == null)
		{
			return;
		}

		for(ContentEntity entity : entities)
		{
			if(entity == null)
			{
				// do nothing
			}
			else if(entity instanceof ContentResource)
			{
				threadLocalManager.set("findResource@" + entity.getId(), entity);
			}
			else if(entity instanceof ContentCollection)
			{
				threadLocalManager.set("findCollection@" + entity.getId(), entity);
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
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, byte[], org.sakaiproject.entity.api.ResourceProperties, java.util.Collection, int)
	 */
	public ContentResource addResource(String id, String type, byte[] content, ResourceProperties properties, Collection groups, int priority)
	throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException,
	ServerOverloadException {
	
	ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
	return addResource(id, type, contentStream, properties, groups, priority);
	}
	
	/**
	* @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, java.io.InputStream, org.sakaiproject.entity.api.ResourceProperties, java.util.Collection, int)
	*/
	public ContentResource addResource(String id, String type, InputStream content, ResourceProperties properties, Collection groups, int priority)
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
			log.debug("OverQuotaException " + e);
			try
			{
				removeResource(edit.getId());
			}
			catch(Exception e1)
			{
				// ignore -- no need to remove the resource if it doesn't exist
				log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
			}
			throw e;
		}
		catch(ServerOverloadException e)
		{
			log.debug("ServerOverloadException " + e);
			try
			{
				removeResource(edit.getId());
			}
			catch(Exception e1)
			{
				// ignore -- no need to remove the resource if it doesn't exist
				log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
			}
			throw e;
		}

		return edit;

	} // addResource

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, byte[], org.sakaiproject.entity.api.ResourceProperties, int)
	 */
	public ContentResource addResource(String id, String type, byte[] content, ResourceProperties properties, int priority)
	throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException,
	ServerOverloadException {
		ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
		return addResource(id, type, contentStream, properties, priority);
	}
	
	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, java.io.InputStream, org.sakaiproject.entity.api.ResourceProperties, int)
	 */
	public ContentResource addResource(String id, String type, InputStream content, ResourceProperties properties, int priority)
	throws PermissionException, IdUsedException, IdInvalidException, InconsistentException, OverQuotaException,
	ServerOverloadException
	{
		Collection no_groups = new ArrayList();
		return addResource(id, type, content, properties, no_groups, priority);
	}

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, int, java.lang.String, byte[], org.sakaiproject.entity.api.ResourceProperties, java.util.Collection, boolean, org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time, int)
	 */
	public ContentResource addResource(String name, String collectionId, int limit, String type, byte[] content,
			ResourceProperties properties, Collection groups, boolean hidden, Time releaseDate, Time retractDate, int priority) 
			throws PermissionException, IdUniquenessException, IdLengthException, IdInvalidException, 
			InconsistentException, OverQuotaException, ServerOverloadException {
		
		ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
		return addResource(name, collectionId, limit, type, contentStream, properties, groups,
				hidden, releaseDate, retractDate, priority);
	}

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addResource(java.lang.String, java.lang.String, int, java.lang.String, java.io.InputStream, org.sakaiproject.entity.api.ResourceProperties, java.util.Collection, boolean, org.sakaiproject.time.api.Time, org.sakaiproject.time.api.Time, int)
	 */
	public ContentResource addResource(String name, String collectionId, int limit, String type, InputStream content,
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
			throw new IdLengthException(id, MAXIMUM_RESOURCE_ID_LENGTH);
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
				log.debug("OverQuotaException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
			catch(ServerOverloadException e)
			{
				log.debug("ServerOverloadException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
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

			SortedSet<String> siblings = new TreeSet<String>();
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
					throw new IdLengthException(new_id, MAXIMUM_RESOURCE_ID_LENGTH);
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
			throw new IdLengthException(id, MAXIMUM_RESOURCE_ID_LENGTH);
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
				SortedSet<String> siblings = new TreeSet<String>();
				try
				{
					ContentCollection collection = findCollection(collectionId);
					siblings.addAll(collection.getMembers());
				}
				catch (TypeException inner_e)
				{
					throw new IdUnusedException(collectionId);
				}

				// see end of loop for condition that enforces attempts <= limit)
				do
				{
					attempts++;
					name = basename + "-" + attempts + extension;
					id = collectionId + name;

					if (attempts >= maximum_tries)
					{
						throw new IdUniquenessException(id);
					}

					if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
					{
						throw new IdLengthException(id, MAXIMUM_RESOURCE_ID_LENGTH);
					}
				}
				while (siblings.contains(id));
			}

		}

		threadLocalManager.set("members@" + collectionId, null);
		//threadLocalManager.set("getCollections@" + collectionId, null);
		threadLocalManager.set("getResources@" + collectionId, null);

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
			throw new IdLengthException(id, MAXIMUM_RESOURCE_ID_LENGTH);
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
				log.debug("OverQuotaException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
				}
				throw e;
			}
			catch(ServerOverloadException e)
			{
				log.debug("ServerOverloadException " + e);
				try
				{
					removeResource(edit.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + edit.getId() + "\n" + e1); 
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

			SortedSet<String> siblings = new TreeSet<String>();
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
					throw new IdLengthException(new_id, MAXIMUM_RESOURCE_ID_LENGTH);
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
		//the id could be null
		if (id == null) {
			return false;
		}
		
		// TODO: Should we check whether this is a valid resource id?
		return id.startsWith(ATTACHMENTS_COLLECTION);
	}

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String, java.lang.String, byte[], org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String type, byte[] content, ResourceProperties properties)
		throws IdInvalidException, InconsistentException, IdUsedException, PermissionException, OverQuotaException,
		ServerOverloadException {
		
		ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
		return addAttachmentResource(name, type, contentStream, properties);
	}
	
	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String, java.lang.String, InputStream, org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String type, InputStream content, ResourceProperties properties)
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
		String collection = ATTACHMENTS_COLLECTION + idManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;

		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new RuntimeException(ID_LENGTH_EXCEPTION);
		}

		// add this collection
		addAndCommitAttachmentCollection(collection, name, null);

		// and add the resource
		return addResource(id, type, content, properties, new ArrayList(), NotificationService.NOTI_NONE);

	} // addAttachmentResource

	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte[], org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String site, String tool, String type, byte[] content,
			ResourceProperties properties) throws IdInvalidException, InconsistentException, IdUsedException, PermissionException,
			OverQuotaException, ServerOverloadException
	{
		ByteArrayInputStream contentStream = new ByteArrayInputStream(content);
		return addAttachmentResource(name, site, tool, type, contentStream, properties);
	}
	
	/**
	 * @see org.sakaiproject.content.api.ContentHostingService#addAttachmentResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.io.InputStream, org.sakaiproject.entity.api.ResourceProperties)
	 */
	public ContentResource addAttachmentResource(String name, String site, String tool, String type, InputStream content,
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

		String siteTitle = site;
		String siteCreator = null;

		try {
			Site m_site = m_siteService.getSite(site);
			siteTitle = m_site.getTitle();
			siteCreator = m_site.getCreatedBy().getId();
		} catch (IdUnusedException e1) {
			log.debug("Site {} was null, defaulting to regular title", site);
		}

		try
		{
			checkCollection(siteCollection);
		}
		catch (Exception e)
		{
			addAndCommitAttachmentCollection(siteCollection, siteTitle, siteCreator);
		}

		String toolCollection = siteCollection + toolId + Entity.SEPARATOR;
		try
		{
			checkCollection(toolCollection);
		}
		catch (Exception e)
		{
			addAndCommitAttachmentCollection(toolCollection, tool, siteCreator);
		}

		// form a name based on the attachments collection, a unique folder id, and the given name
		String collection = toolCollection + idManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;

		if (id.length() > MAXIMUM_RESOURCE_ID_LENGTH)
		{
			throw new RuntimeException(ID_LENGTH_EXCEPTION);
		}

		addAndCommitAttachmentCollection(collection, name, siteCreator);

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
		String collection = ATTACHMENTS_COLLECTION + idManager.createUuid() + Entity.SEPARATOR;
		String id = collection + name;
		
		addAndCommitAttachmentCollection(collection, name, null);

		return addResource(id);

	} // addAttachmentResource
	
	/**
	 * addAndCommitAttachmentCollection - Helper to Add and Commit an Attachment Collection, used by many methods
	 * @param collection - Value of collection to create
	 * @param name - Name of collection
	 * @param createdBy - Id of user creating or null to leave unchaned
	 * 
	 * @throws IdUsedException
	 * @throws IdInvalidException
	 * @throws PermissionException
	 * @throws InconsistentException
	 */

	private void addAndCommitAttachmentCollection(String collection, String name, String createdBy) throws IdUsedException, IdInvalidException, PermissionException, InconsistentException {
		// add this collection
		ContentCollectionEdit edit = addCollection(collection);
		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

		//Set the created by to someone else unless null
		if (createdBy != null) {
			edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_CREATOR, createdBy);
		}
		
		commitCollection(edit);	
	}

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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

		threadLocalManager.set(String.valueOf(resource), resource);
		
		ResourceProperties props = resource.getProperties();
		if(props != null) {
			resource.setOldDisplayName(props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
		}

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
	public ContentResourceEdit editDeletedResource(String id) throws PermissionException, IdUnusedException, TypeException, InUseException
	{
	  // check security 
//      if ( ! allowUpdateResource(id) )
//		   throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
//                                       AUTH_RESOURCE_WRITE_ANY, getReference(id));

		// ignore the cache - get the collection with a lock from the info store
		BaseResourceEdit resource = (BaseResourceEdit) m_storage.editDeletedResource(id);
		if (resource == null) throw new InUseException(id);

		resource.setEvent(EVENT_RESOURCE_WRITE);

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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

		threadLocalManager.set(String.valueOf(resource), resource);

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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

		threadLocalManager.set(String.valueOf(collection), collection);

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
			resource = (ContentResource) threadLocalManager.get("findResource@" + id);
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
				threadLocalManager.set("findResource@" + id, resource); 	// new BaseResourceEdit(resource));
			}
		}
		else
		{
			resource = new BaseResourceEdit(resource);
		}

		


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
	public void removeResource(String id) throws PermissionException, IdUnusedException, 
		TypeException, InUseException
	
	{
		BaseResourceEdit edit = (BaseResourceEdit) editResourceForDelete(id);
		try
		{
			removeResource(edit);
		}
		finally
		{
			// If the edit wasn't committed unlock the resource.
			if (edit.isActiveEdit())
			{
				cancelResource(edit);
			}
		}

	} // removeResource

	/**
     * Remove a resource that is locked for update.
     * 
     * @param edit
     *        The ContentResourceEdit object to remove.
     * @exception PermissionException
     *            if the user does not have permissions to read a containing collection, or to remove this resource.
     */
    public void removeResource(ContentResourceEdit edit) throws PermissionException {
	    removeResource(edit, true);
	}

	/**
     * Allows removing a resource while leaving the content alone,
     * this mostly matters for resources copied by reference
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to remove.
	 * @param removeContent if true, removes the content as well (default),
	 *        else only removes the resource record from the DB
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 */
	protected void removeResource(ContentResourceEdit edit, boolean removeContent) throws PermissionException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			log.error("removeResource(): closed ContentResourceEdit", e);
			return;
		}

		String id = edit.getId();

		// check security (throws if not permitted)
		checkExplicitLock(id);
		if ( ! allowRemoveResource(edit.getId()) )
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, edit.getReference());


		// htripath -store the metadata information into a delete table
		// assumed uuid is not null as checkExplicitLock(id) throws exception when null

		try {
			String uuid = this.getUuid(id);
			String userId = sessionManager.getCurrentSessionUserId().trim();
			addResourceToDeleteTable(edit, uuid, userId);
			edit.setContentLength(0);  // we stop removing it entry from the DB 
		} catch (ServerOverloadException soe) {
			log.debug("removeResource: could not save deleted resource, restore for this resource is not possible " + soe );  
		}

		// complete the edit
		m_storage.removeResource(edit, removeContent);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		if(! readyToUseFilesizeColumn())
		{
			removeSizeCache(edit);
		}

		((BaseResourceEdit) edit).setRemoved();

		// remove old version of this edit from thread-local cache
		threadLocalManager.set("findResource@" + edit.getId(), null);

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(edit.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
			log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + ignore);
		}

		// track it (no notification)
		String ref = edit.getReference(null);
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, ref, true,
				NotificationService.NOTI_NONE));
		eventTrackingService.cancelDelays(ref);

	} // removeResource

	public void removeDeletedResource(String id) throws PermissionException, IdUnusedException, TypeException, InUseException
	{
		BaseResourceEdit edit = (BaseResourceEdit) editDeletedResource(id); 
		removeDeletedResource(edit);
	
	} // removeResource
	
	/**
	 * Remove a resource from the deleted table.
	 * 
	 * @param edit
	 *        The ContentResourceEdit object to remove.
	 * @exception PermissionException
	 *            if the user does not have permissions to read a containing collection, or to remove this resource.
	 */
	public void removeDeletedResource(ContentResourceEdit edit) throws PermissionException
	{
		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			log.error("removeDeletedResource(): closed ContentResourceEdit", e);
			return;
		}

		// check security (throws if not permitted)
//		if ( ! allowRemoveResource(edit.getId()) )
//		   throw new PermissionException(SessionManager.getCurrentSessionUserId(), 
//                                       AUTH_RESOURCE_REMOVE_ANY, edit.getReference());
//

		// complete the edit
		m_storage.removeDeletedResource(edit);

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();
		
		((BaseResourceEdit) edit).setRemoved();

		// remove old version of this edit from thread-local cache
		threadLocalManager.set("findResource@" + edit.getId(), null);

		// remove any realm defined for this resource
		try
		{
			m_authzGroupService.removeAuthzGroup(m_authzGroupService.getAuthzGroup(edit.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
			log.debug("removeResource: removing realm for : " + edit.getReference() + " : " + ignore);
		}

	} // removeDeletedResource

	public void restoreResource(String id) throws PermissionException, IdUsedException, IdUnusedException,
		IdInvalidException,	InconsistentException, OverQuotaException, ServerOverloadException, 
		TypeException, InUseException
	{
		ContentResourceEdit deleResource = null;
		try {
			deleResource = editDeletedResource(id);

			ContentResourceEdit newResource;
			try {
				newResource = addResource(id);
			} catch (IdUsedException iue) {
				log.error("restoreResource: cannot restore resource " + id, iue);
				throw iue;
			}
			newResource.setContentType(deleResource.getContentType());
			newResource.setContentLength(deleResource.getContentLength());
			newResource.setResourceType(deleResource.getResourceType());
			newResource.setAvailability(deleResource.isHidden(), deleResource.getReleaseDate(),deleResource.getRetractDate());
			newResource.setContent(m_storage.streamDeletedResourceBody(deleResource));
			try {
				// If you're storing the file in DB this breaks as it removes the restored file.
				removeDeletedResource(deleResource);
				// close the edit object
				((BaseResourceEdit) deleResource).closeEdit();
			} catch (PermissionException pe) {
				log.error("restoreResource: access to resource not permitted" + id, pe);
				try
				{
					removeResource(newResource.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + deleResource.getId() + "\n" + e1);
				}
				throw pe;
			}
			try {
				addProperties(newResource.getPropertiesEdit(), deleResource.getProperties());
				commitResource(newResource, NotificationService.NOTI_NONE);

			} catch (ServerOverloadException e) {
				log.debug("ServerOverloadException " + e);
				try
				{
					removeResource(newResource.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + newResource.getId() + "\n" + e1);
				}
				throw e;
			} catch (OverQuotaException e) {
				log.debug("OverQuotaException " + e);
				try
				{
					removeResource(newResource.getId());
				}
				catch(Exception e1)
				{
					// ignore -- no need to remove the resource if it doesn't exist
					log.debug("Unable to remove partially completed resource: " + newResource.getId() + "\n" + e1);
				}
				throw e;
			}
		} catch (IdUnusedException iue) {
			log.error("restoreResource: cannot locate deleted resource " + id, iue);
			throw iue;
		} catch (TypeException te) {
			log.error("restoreResource: invalid type " + id, te);
			throw te;
		} catch (InUseException ie) {
			log.error("restoreResource: resource in use " + id, ie);
			throw ie;
		} catch (PermissionException pe) {
			log.error("restoreResource: access to resource not permitted" + id, pe);
			throw pe;
		} finally {
			// Unlock if something went wrong.
			if (deleResource != null && deleResource.isActiveEdit()) {
				m_storage.cancelDeletedResource(deleResource);
			}
		}
	}
	
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
	public void addResourceToDeleteTable(ContentResourceEdit edit, String uuid, String userId) throws PermissionException, ServerOverloadException
	{
		if (m_bodyPathDeleted == null) {
			return;
		}
		String id = edit.getId();
		String content_type = edit.getContentType();

		String resource_type = edit.getResourceType();

		// KNL-245 do not read the resource body, as this is not subsequently written out
		
		ResourceProperties properties = edit.getProperties();

		InputStream content = null;
		try
		{
			content = edit.streamContent();
			addDeleteResource(id, 
				content_type, content, resource_type, edit.getReleaseDate(), edit.getRetractDate(), 
				properties, uuid, userId,
				NotificationService.NOTI_OPTIONAL);
		}
		finally
		{
			if (content != null)
			{
				try
				{
					content.close();
				}
				catch (IOException e)
				{
					log.error("Failed to close when saving deleted content stream.", e);
				}
			}
		}
	}

	public ContentResource addDeleteResource(String id, String type, InputStream inputStream, String resourceType, Time releaseDate, Time retractDate, ResourceProperties properties, String uuid, String userId, int priority) throws PermissionException, ServerOverloadException
			{
		id = (String) fixTypeAndId(id, type).get("id");
		// resource must also NOT end with a separator characters (fix it)
		if (id.endsWith(Entity.SEPARATOR))
		{
			id = id.substring(0, id.length() - 1);
		}
		// check security-unlock to add record
		unlock(AUTH_RESOURCE_ADD, id);

		// In the future we may wish to allow multiple copies of the file in the recycle bin.
		// Remove Deleted Resource prevents id collision as #restoreResource(String) doesn't allow you to
		// specify which version of a file you want to restore.
		try {
			removeDeletedResource(id);
		} catch (Exception ex) {
			// There is no collision
		}
		
		// reserve the resource in storage - it will fail if the id is in use
		BaseResourceEdit edit = (BaseResourceEdit) m_storage.putDeleteResource(id, uuid, userId);
		// added for NPE static code review -AZ
		if (edit == null) {
		    throw new NullPointerException("putDeleteResource returned a null value, this is unrecoverable");
		}

		// add live properties-do we need this? - done to have uniformity with main table
		addLiveResourceProperties(edit);

		// track event - do we need this? no harm to keep track
		edit.setEvent(EVENT_RESOURCE_ADD);

		edit.setContentType(type);
		edit.setResourceType(resourceType);
		edit.setReleaseDate(releaseDate);
		edit.setRetractDate(retractDate);
		if (inputStream != null)
		{
			edit.setContent(inputStream);
		}
		addProperties(edit.getPropertiesEdit(), properties);

		// complete the edit - update xml which contains properties xml and store the file content
		m_storage.commitDeletedResource(edit, uuid);

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
	    // check security for remove resource (own or any)
	    boolean allowed = false;
        if ( allowRemove(id) ) {
            // check security for read resource
            if ( unlockCheck(AUTH_RESOURCE_READ, id) ) {
                // check security for add resource
                if ( unlockCheck(AUTH_RESOURCE_ADD, new_id) ) {
                    allowed = true;
                }
            }
        }
		if (log.isDebugEnabled()) log.debug("content allowRename("+id+", "+new_id+") = "+allowed);
		return allowed;

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
	 * @deprecated DO NOT USE THIS, it does not work and will ALWAYS throw an UnsupportedOperationException - https://jira.sakaiproject.org/browse/KNL-1078
	 */
	public String rename(String id, String new_id) throws IdUnusedException, TypeException, PermissionException, InUseException,
	OverQuotaException, InconsistentException, IdUsedException, ServerOverloadException
	{
	    throw new UnsupportedOperationException("the rename() method is not properly implemented and should NOT be used - https://jira.sakaiproject.org/browse/KNL-1078");
	    /* Commented out for https://jira.sakaiproject.org/browse/KNL-1078
	     * 
		// Note - this could be implemented in this base class using a copy and a delete
		// and then overridden in those derived classes which can support
		// a direct rename operation.

		// check security for remove resource (own or any)
		if ( ! allowRemove(id) )
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id));

		// check security for read resource
		unlock(AUTH_RESOURCE_READ, id);

		// check security for add resource
		unlock(AUTH_RESOURCE_ADD, new_id);

		boolean isCollection = false;
		ContentResourceEdit thisResource = null;
		ContentCollectionEdit thisCollection = null;

		if (log.isDebugEnabled()) log.debug("rename(" + id + "," + new_id + ")");

		if (m_storage.checkCollection(id))
		{
			isCollection = true;
			// find the collection
			thisCollection = editCollection(id);
			if (isRootCollection(id))
			{
				cancelCollection(thisCollection);
				throw new PermissionException(sessionManager.getCurrentSessionUserId(), null, null);
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

	    // makes a copy each time content is renamed
        if (isCollection) {
            // NOTE: this does NOT do a deep copy (i.e. the collection is copied but the content is not)
            new_id = copyCollection(thisCollection, new_id);
            removeCollection(thisCollection);
        } else {
            // rename should do a reference copy only and not remove the content after
            new_id = copyResource(thisResource, new_id, true); // set referenceCopy
            removeResource(thisResource, false); // force content to not be removed
        }
		return new_id;
		*/

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
			throw new IdLengthException(new_id, MAXIMUM_RESOURCE_ID_LENGTH);
		}

		// Should use copyIntoFolder if possible
		boolean isCollection = false;
		ContentResource thisResource = null;

		if (log.isDebugEnabled()) log.debug("copy(" + id + "," + new_id + ")");

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
        if (thisCollection == null)
        {
        	thisResource = getResource(id);

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
					AUTH_RESOURCE_REMOVE_ANY, getReference(id));

		// check security for read existing resource
		unlock(AUTH_RESOURCE_READ, id);

		// check security for add new resource
		unlock(AUTH_RESOURCE_ADD, new_id);

		boolean isCollection = false;
		ContentResourceEdit thisResource = null;
		ContentCollectionEdit thisCollection = null;

		if (log.isDebugEnabled()) log.debug("moveIntoFolder(" + id + "," + new_id + ")");

		if (m_storage.checkCollection(id))
		{
			isCollection = true;
			// find the collection
			thisCollection = editCollection(id);
			if (isRootCollection(id))
			{
				cancelCollection(thisCollection);
				throw new PermissionException(sessionManager.getCurrentSessionUserId(), null, null);
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

		if (log.isDebugEnabled()) log.debug("moveSCollection adding colletion=" + new_folder_id + " name=" + name);

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
					
					if 	(isPubView(thisCollection.getId()))
					{
						collection.setPublicAccess();
					}
					collection.setAvailability(thisCollection.isHidden(), thisCollection.getReleaseDate(), thisCollection.getReleaseDate());
					m_storage.commitCollection(collection);

					if (log.isDebugEnabled()) log.debug("moveCollection successful");
					still_trying = false;
				}
				catch (IdUsedException e)
				{
					try
					{
						getCollection(new_folder_id);
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

			List<String> members = thisCollection.getMembers();

			if (log.isDebugEnabled()) log.debug("moveCollection size=" + members.size());

			Iterator<String> memberIt = members.iterator();
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
	 * @throws IllegalArgumentException if the new_id is null
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
	    if (StringUtils.isBlank(new_id)) {
	        throw new IllegalArgumentException("new_id must not be null");
	    }

	    String fileName = isolateName(new_id);
		String folderId = isolateContainingId(new_id);

		ResourceProperties properties = thisResource.getProperties();
		String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName == null)
		{
			displayName = fileName;
		}
		String new_displayName = displayName;

		if (log.isDebugEnabled()) log.debug("moveResource displayname=" + new_displayName + " fileName=" + fileName);

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
				// NOTE: we always do a reference copy on a move if we can
				boolean referenceCopy = false;
				if (edit instanceof BaseResourceEdit) {
			        ((BaseResourceEdit)edit).setReferenceCopy(thisResource.getId());
			        // need to manually update the content length or it ends up as 0
			        ((BaseResourceEdit)edit).setContentLength(thisResource.getContentLength());
			        // need to manually update the file path or it will be regenerated
			        ((BaseResourceEdit) edit).m_filePath = ((BaseResourceEdit) thisResource).m_filePath;
			        referenceCopy = true;
				} else {
	                edit.setContent(thisResource.streamContent());
				}
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
				eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_ADD, ref, true,
						NotificationService.NOTI_NONE));

				// TODO - we don't know whether to post a future notification or not 
				postAvailableEvent(edit, ref, NotificationService.NOTI_NONE);

				// we need to not remove the content if we just did a reference copy above (or remove the content when there was no reference copy)
				m_storage.removeResource(thisResource, !referenceCopy);

				// track it (no notification)
				String thisRef = thisResource.getReference(null);
				eventTrackingService.cancelDelays(thisRef);
				eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_REMOVE, thisRef, true,
						NotificationService.NOTI_NONE));

				if (log.isDebugEnabled()) log.debug("moveResource successful");
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
					getResource(new_id);
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
	 * @see #copyIntoFolder(String, String) method (preferred method for invocation from a tool).
	 */
	public String copy(String id, String new_id) throws PermissionException, IdUnusedException, TypeException, InUseException,
	OverQuotaException, IdUsedException, ServerOverloadException
	{
		// Should use copyIntoFolder if possible
		boolean isCollection = false;
		
		ContentResource thisResource = null;

		if (log.isDebugEnabled()) log.debug("copy(" + id + "," + new_id + ")");

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


		// loop through the properties
		Iterator<String> propertyNames = properties.getPropertyNames();
		while (propertyNames.hasNext())
		{
			String propertyName = (String) propertyNames.next();
			String propertyValue = properties.getProperty(propertyName);
			log.debug("copying: " + propertyName + " with value " + propertyValue);
			resourceProperties.addProperty(propertyName, propertyValue);
			
		} // while
		return resourceProperties;

	} // duplicateResourceProperties

	/**
	 * Copy a resource.
	 * 
	 * @param resource The resource to be copied
	 * @param new_id The desired id of the new resource.
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
	    return copyResource(resource, new_id, false);
	}

	/**
	 * Copy a resource with an option to do a reference copy
	 * 
	 * @param resource
	 * @param new_id
	 * @param referenceCopy if true, then do not copy the actual content (only make a reference copy which points to it),
	 *                      if false, then copy like normal (duplicate the content)
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
	protected String copyResource(ContentResource resource, String new_id, boolean referenceCopy) throws PermissionException, IdUnusedException,
    TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
    {
		if (log.isDebugEnabled())
		{
			log.debug("copyResource: " + resource.getId() + " to " + new_id + ", reference="+referenceCopy);
		}
		
        if (StringUtils.isBlank(new_id)) {
            throw new IllegalArgumentException("new_id must not be null");
        }

        String fileName = isolateName(new_id);
		fileName = Validator.escapeResourceName(fileName);
		String folderId = isolateContainingId(new_id);

		ResourceProperties properties = resource.getProperties();
		String displayName = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (displayName == null)
		{
			displayName = fileName;
		}
		String new_displayName = displayName;
		if (log.isDebugEnabled()) log.debug("copyResource displayname=" + new_displayName + " fileName=" + fileName);

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
				// this duplicates a lot of the code from BaseResourceEdit.set()
				edit.setContentType(resource.getContentType());

				if (referenceCopy && edit instanceof BaseResourceEdit) {
				    // do a reference copy so the actual content is not duplicated
				    ((BaseResourceEdit)edit).setReferenceCopy(resource.getId());
                    if (log.isDebugEnabled()) log.debug("copyResource doing a reference copy of "+resource.getId());
				} else {
	                // use stream instead of byte array
	                // edit.setContent(resource.getContent());
	                edit.setContent(resource.streamContent());
                    if (log.isDebugEnabled()) log.debug("copyResource doing a normal copy");
				}

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

				commitResource(edit, NotificationService.NOTI_NONE);
				// close the edit object
				((BaseResourceEdit) edit).closeEdit();

				if (log.isDebugEnabled()) log.debug("copyResource successful");
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
					getResource(new_id);
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
	 *            if the user does not have permissions to perform the operations OR the collection has members in it
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

		if (log.isDebugEnabled()) log.debug("copyCollection size=" + members.size());

		if (members.size() > 0)
		{
			// recurse to copy everything in the folder?
			throw new PermissionException(null, null, null);
		}

		String name = isolateName(new_id);

		ResourceProperties properties = thisCollection.getProperties();
		ResourcePropertiesEdit newProps = duplicateResourceProperties(properties, thisCollection.getId());
		newProps.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

		if (log.isDebugEnabled()) log.debug("copyCollection adding colletion=" + new_id + " name=" + name);
		boolean isHidden = false;
		if (isPubView(thisCollection.getId()))
		{
			isHidden = true;
		}
		try
		{
			addCollection(new_id, newProps, null, isHidden, null, null);
			
			if (log.isDebugEnabled()) log.debug("copyCollection successful");
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
	 * Make a deep copy of a collection. 
	 * Creates a new collection with an id similar to new_folder_id and recursively copies all nested collections and resources within thisCollection to the new collection.
	 * Only used in "copyIntoFolder" for now
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

		if (log.isDebugEnabled()) log.debug("deepCopyCollection adding colletion=" + new_folder_id + " name=" + name);

		String base_id = new_folder_id + "-";
		boolean still_trying = true;
		int attempt = 0;
		ContentCollection newCollection = null;
		try
		{
			try
			{
				newCollection = addCollection(new_folder_id, newProps);
				// use the creator and creation-date of the original instead of the copy
				BaseCollectionEdit collection = (BaseCollectionEdit) m_storage.editCollection(newCollection.getId());
				if 	(isPubView(thisCollection.getId()))
				{
					collection.setPublicAccess();
				}
				collection.setAvailability(thisCollection.isHidden(), thisCollection.getReleaseDate(), thisCollection.getReleaseDate());
				m_storage.commitCollection(collection);
				
				if (log.isDebugEnabled()) log.debug("deepCopyCollection  top level created successful");
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
			SortedSet<String> siblings = new TreeSet<String>();
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
						BaseCollectionEdit collection = (BaseCollectionEdit) m_storage.editCollection(newCollection.getId());
						if 	(isPubView(thisCollection.getId()))
						{
							collection.setPublicAccess();
						}
						collection.setAvailability(thisCollection.isHidden(), thisCollection.getReleaseDate(), thisCollection.getReleaseDate());
						m_storage.commitCollection(collection);
						still_trying = false;
					}
					catch (IdUsedException inner_e)
					{
						// try again
					}
				}
			}

			List<String> members = thisCollection.getMembers();

			if (log.isDebugEnabled()) log.debug("deepCopyCollection size=" + members.size());

			Iterator<String> memberIt = members.iterator();
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

	private boolean hasContentType(String resourceId) {

		String contentType = null;
		
		try {
			contentType = getResource(resourceId).getContentType();
		} catch (PermissionException e) {
		} catch (IdUnusedException e) {
		} catch (TypeException e) {
		}
		
		return contentType != null && !contentType.isEmpty();
    }
	
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
	public void commitResource(ContentResourceEdit edit, int priority) throws OverQuotaException, ServerOverloadException, VirusFoundException
	{

		// check for closed edit
		if (!edit.isActiveEdit())
		{
			Exception e = new Exception();
			log.error("commitResource(): closed ContentResourceEdit", e);
			return;
		}
		
        boolean hasContentTypeAlready = hasContentType(edit.getId());
        
        //use magic to fix mimetype
        //Don't process for special TYPE_URL type
        String currentContentType = edit.getContentType();
        m_useMimeMagic = m_serverConfigurationService.getBoolean("content.useMimeMagic", m_useMimeMagic);
        m_ignoreExtensions = Arrays.asList(ArrayUtils.nullToEmpty(m_serverConfigurationService.getStrings("content.mimeMagic.ignorecontent.extensions")));
        m_ignoreMimeTypes = Arrays.asList(ArrayUtils.nullToEmpty(m_serverConfigurationService.getStrings("content.mimeMagic.ignorecontent.mimetypes")));

        if (m_useMimeMagic && DETECTOR != null && !ResourceProperties.TYPE_URL.equals(currentContentType) && !hasContentTypeAlready) {
            try (
                    TikaInputStream buff = TikaInputStream.get(edit.streamContent());
            ) {
                //we have to make the stream resetable so tika can read some of it and reset for saving.
                //Also have to give the tika stream to the edit object since tika can invalidate the original 
                //stream and replace it with a new stream.
                edit.setContent(buff);

                final Metadata metadata = new Metadata();
                //This might not want to be set as it would advise the detector
                metadata.set(Metadata.RESOURCE_NAME_KEY, edit.getId());
                metadata.set(Metadata.CONTENT_TYPE, currentContentType);
                String newmatch = "";
                //If we are ignoring the content for this extension, don't give it any data
                if (m_ignoreExtensions != null && m_ignoreExtensions.contains(FilenameUtils.getExtension(edit.getId()))) {
                    newmatch = DETECTOR.detect(null, metadata).toString();
                }
                else {
                  newmatch = DETECTOR.detect(TikaInputStream.get(buff), metadata).toString();
                  //Redetect without the content as we're ignoring this mime type
                  if (m_ignoreMimeTypes != null && m_ignoreMimeTypes.contains(newmatch)) {
                    newmatch = DETECTOR.detect(null, metadata).toString();
                  }
                }
                
                if (log.isDebugEnabled()) {
                    log.debug("Magic: Setting content type from " + currentContentType + " to " + newmatch);
                }
                edit.setContentType(newmatch);
                commitResourceEdit(edit, priority);
            } catch (Exception e) {
				log.warn("Exception when trying to get the resource's data: " + e);
			} 
        }
        else {
        	commitResourceEdit(edit, priority);
        }
        
        // Queue up content for virus scanning
        if (virusScanner.getEnabled()) {
            virusScanQueue.add(edit.getId());
        }

        /**
		 *  check for over quota.
		 *  We do this after the commit so we can actual tell its size
		 */
		if (overQuota(edit))
		{
			try {
				//the edit is closed so we need to refetch it
				ContentResourceEdit edit2 = editResource(edit.getId());
				removeResource(edit2);
			} catch (PermissionException e1) {
				log.error(e1.getMessage(), e1);
			} catch (IdUnusedException e1) {
				log.error(e1.getMessage(), e1);
			} catch (TypeException e1) {
				log.error(e1.getMessage(), e1);
			} catch (InUseException e1) {
				log.error(e1.getMessage(), e1);
			}
			throw new OverQuotaException(edit.getReference());
		}
		
		if(! readyToUseFilesizeColumn())
		{
			addSizeCache(edit);
		}

	} // commitResource

    /**
	 * This timer task is run by the timer thread based on the period set above
	 */
	protected class VirusTimerTask extends TimerTask {
		public void run() {
			try {
				log.debug("running timer task");
                enableAzgSecurityAdvisor();
                processVirusQueue();
            } catch (Exception e) {
				log.error("Virus scan failure: " + e.getMessage(), e);
			} finally {
                disableAzgSecurityAdvisor();
            }
		}
    }

    public void processVirusQueue() {
        // grab the queue - any new stuff will be processed next time
		List<String> queue = new Vector();
		synchronized (virusScanQueue)
		{
			queue.addAll(virusScanQueue);
			virusScanQueue.clear();
		}

        Session session = sessionManager.getCurrentSession();

        for (String contentId : queue) {
            // process the queue of digest requests
            try {
                virusScanner.scanContent(contentId);
            } catch (VirusFoundException e) {
                //this file is infected we need to remove if
            	ContentResourceEdit edit2 = null;
                try {
                    //the edit is closed so we need to refetch it
                    edit2 = editResource(contentId);
                    ResourceProperties props = edit2.getProperties();
                    // we need to set the session userId or removeResource fails
			        String owner = props.getProperty(ResourceProperties.PROP_CREATOR);
                    User user = userDirectoryService.getUser(owner);
                    session.setUserEid(user.getEid());
                    session.setUserId(user.getId());
                    removeResource(edit2);
                } catch (PermissionException e1) {
                    log.error(e1.getMessage(), e1);
                } catch (IdUnusedException e1) {
                    log.error(e1.getMessage(), e1);
                } catch (TypeException e1) {
                    log.error(e1.getMessage(), e1);
                } catch (InUseException e1) {
                    log.error(e1.getMessage(), e1);
                } catch (UserNotDefinedException e1) {
                    log.error(e1.getMessage(), e1);
                }
                finally {
                	//safety first!
                	if (edit2 != null && edit2.isActiveEdit()) {
    					cancelResource(edit2);
                	}
                }
            } catch (VirusScanIncompleteException e1) {
                log.info("virus scanning did not complete adding resource: " + contentId + " back to queue");
                virusScanQueue.add(contentId);
            }
        }


    }
	private boolean checkUpdateContentEncoding(ContentResourceEdit edit) {
		if (edit == null) {
			return false;
		}
		log.debug("checkUpdateContentEncoding(" + edit.getId() + ")");

		InputStream content = null;
		boolean updated = false;
		try
		{
			//no point in doing this for 0 size resources
			if (edit.getContentLength() == 0)
			{
				return false;
			}
			
			String contentEncoding = edit.getProperties().getProperty(ResourceProperties.PROP_CONTENT_ENCODING);
			if (contentEncoding == null)
			{
				contentEncoding = "";
			}
			String encoding = null;
			CharsetDetector detector = new CharsetDetector();
			content = edit.streamContent();
			//we don't want the whole file the first couple of bytes should do
			int len = 1000;
			byte[] contentBytes = new byte[len];
			if (content.markSupported()) 
			{			
				detector.setText(content);
			} else {
				 content.read(contentBytes);
				 detector.setText(contentBytes);
			}
			CharsetMatch match = detector.detect();
			//KNL-714 match can be null -DH
			if (match != null)
			{
				encoding = match.getName();
			}
			else
			{
				return false;
			}
			//KNL-682 do not set content as UTF-32LE or UTF-16
			if (encoding.indexOf("UTF-16") > -1 || encoding.indexOf("UTF-32") > -1) {
				encoding = "UTF-8";
			}

			int confidence = match.getConfidence();
			//KNL-683 we need a relatively good confidence before we change the encoding
			int threshold = m_serverConfigurationService.getInt("content.encodingDetection.threshold", 70);
			log.debug("detected character encoding of " + encoding + " with confidence of " + confidence + " origional was" + contentEncoding);
			if (encoding != null && !contentEncoding.equals(encoding) && (confidence >= threshold))
			{
				ResourcePropertiesEdit rpe = edit.getPropertiesEdit();
				rpe.removeProperty(ResourceProperties.PROP_CONTENT_ENCODING);
				rpe.addProperty(ResourceProperties.PROP_CONTENT_ENCODING, encoding);
				updated = true;
			} 
			
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} catch (ServerOverloadException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			if (content != null) {
				try {
					content.close();
				} catch (IOException e) {
					//not much we can do
				}
			}
		}
		return updated;
	}

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
			log.error("commitResourceEdit(): closed ContentResourceEdit", e);
			return;
		}

		if(this.m_prioritySortEnabled)
		{
		    ((BasicGroupAwareEdit) edit).setPriority();
		}

		// update the properties for update
		addLiveUpdateResourceProperties(edit);

		boolean titleUpdated = false;
		if(edit instanceof BaseResourceEdit) {
			String oldDisplayName = ((BaseResourceEdit) edit).getOldDisplayName();
			ResourceProperties props = edit.getProperties();
			if(props != null) {
				String newDisplayName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				if(oldDisplayName == null || newDisplayName == null) {
					// do nothing
				} else if(! oldDisplayName.equals(newDisplayName)) {
					// DisplayName has changed -- post event
					titleUpdated = true;
				}
			}
		}

		// Flag whether we have a body update or not. This will save expensive DB/IO if we don't need to check the encoding.
		boolean contentUpdated = ((BaseResourceEdit) edit).m_body != null || ((BaseResourceEdit) edit).m_contentStream != null;

		// complete the edit
		m_storage.commitResource(edit);

		// Now that the data is committed, we can update the encoding if needed.
		// Check the content type if this is an HTML or TEXT file upload.
		if (contentUpdated && ResourceType.TYPE_UPLOAD.equals(edit.getResourceType()) && 
			(ResourceType.MIME_TYPE_HTML.equals(edit.getContentType()) || ResourceType.MIME_TYPE_TEXT.equals(edit.getContentType()))) {

			// Any body bytes lying around erroneously should be thrown away
			// because they would already be committed. We also purge the stream
			// reference because it would be used up. Additional calls to
			// streamContent() will generate new ones from storage like we need.
			((BaseResourceEdit) edit).m_body = null;
			((BaseResourceEdit) edit).m_contentStream = null;

			if (edit.isActiveEdit() && checkUpdateContentEncoding(edit)) {

				// The encoding was changed, so we have to flush the metadata.
				// Since we already cleaned up, we won't write the body again.
				m_storage.commitResource(edit);
			}
		}

		// close the edit object
		((BaseResourceEdit) edit).closeEdit();

		// must remove old version of this edit from thread-local cache
		// so we get new version if we try to retrieve it in same thread
		threadLocalManager.set("findResource@" + edit.getId(), null);
		String containerId = isolateContainingId(edit.getId());
		threadLocalManager.set("findCollection@" +  containerId, null);
		threadLocalManager.set("members@" + containerId, null);
		//threadLocalManager.set("getCollections@" + containerId, null);
		threadLocalManager.set("getResources@" + containerId, null);

		// only send notifications if the resource is available
		// an 'available' event w/ notification will be sent when the resource becomes available

		String ref = edit.getReference(null);

		// Cancel any previously scheduled delayed available events
		eventTrackingService.cancelDelays(ref, ((BaseResourceEdit) edit).getEvent());

		// Send a notification with the initial event if this is a revise event and the resource is already available
		int immediate_priority = (EVENT_RESOURCE_WRITE.equals(((BaseResourceEdit) edit).getEvent()) && edit.isAvailable()) ? 
				priority : NotificationService.NOTI_NONE;

		eventTrackingService.post(eventTrackingService.newEvent(((BaseResourceEdit) edit).getEvent(),
				ref, true, immediate_priority));

		// Post an available event for now or later
		postAvailableEvent(edit, ref, priority);

		if(titleUpdated) {
			// post EVENT_RESOURCE_UPD_TITLE event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(EVENT_RESOURCE_UPD_TITLE, edit.getReference(), true, priority));
		}
		
		if(((BasicGroupAwareEdit) edit).isVisibilityUpdated()) {
			// post EVENT_RESOURCE_UPD_VISIBILITY event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(EVENT_RESOURCE_UPD_VISIBILITY, edit.getReference(), true, priority));
		}
		
		if(((BasicGroupAwareEdit) edit).isAccessUpdated()) {
			// post EVENT_RESOURCE_UPD_ACCESS event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(EVENT_RESOURCE_UPD_ACCESS, edit.getReference(), true, priority));
		}
		
	} // commitResourceEdit

	/**
	 * Test a collection of Group object for the specified group reference
	 * @param groups The collection (Group) of groups
	 * @param groupRef The string group reference to find.
	 * @return true if found, false if not.
	 */
	protected boolean groupCollectionContainsRefString(Collection<Group> groups, String groupRef)
	{
		for (Iterator<Group> i = groups.iterator(); i.hasNext();)
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
			log.error("cancelResource(): closed ContentResourceEdit", e);
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
		// eventTrackingService.post(eventTrackingService.newEvent(EVENT_PROPERTIES_READ, getReference(id)));

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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), 
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
			alternateRoot = StringUtils.trimToNull(getProperties(id).getProperty(rootProperty));
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

		// if (log.isDebugEnabled()) log.debug("isRootCollection: id: " + id + " rv: " + rv);

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
	public Map<String, String> getCollectionMap()
	{
		// the return map
		Map<String, String> rv = new HashMap<String, String>();

		// get the sites the user has access to
		List<Site> mySites = m_siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null,
				org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);

		// add in the user's myworkspace site, if we can find it and if the user
		// is not anonymous
		String userId = sessionManager.getCurrentSessionUserId();
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
		for (Iterator<Site> i = mySites.iterator(); i.hasNext();)
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

	/** The chunk size used when streaming (100K). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/**
	 * Process the access request for a resource.
	 * 
	 * @param req
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
			Collection<String> copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
			EntityAccessOverloadException, EntityCopyrightException
	{
		// we only access resources, not collections
		if (ref.getId().endsWith(Entity.SEPARATOR)) throw new EntityNotDefinedException(ref.getReference());

		// need read permission
		if (!allowGetResource(ref.getId()))
			throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), AUTH_RESOURCE_READ, ref.getReference());

		ContentResource resource = null;
		try
		{
			resource = getResource(ref.getId());
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
		if (((BaseResourceEdit)resource).requiresCopyrightAgreement() && !copyrightAcceptedRefs.contains(resource.getReference()))
		{
			throw new EntityCopyrightException(ref.getReference());
		}
		
		// Wrap up the resource if we need to.
		resource = m_contentFilterService.wrap(resource);

		// Set some headers to tell browsers to revalidate and check for updated files
		res.addHeader("Cache-Control", "must-revalidate, private");
		res.addHeader("Expires", "-1");

		try
		{
			long len = resource.getContentLength();
			String contentType = resource.getContentType();
			ResourceProperties rp = resource.getProperties();
			long lastModTime = 0;

			try {
				Time modTime = rp.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
				lastModTime = modTime.getTime();
			} catch (Exception e1) {
				log.info("Could not retrieve modified time for: " + resource.getId());
			}
			
			// KNL-1316 tell the browser when our file was last modified for caching reasons
			if (lastModTime > 0) {
				SimpleDateFormat rfc1123Date = new SimpleDateFormat(RFC1123_DATE, LOCALE_US);
				rfc1123Date.setTimeZone(TimeZone.getTimeZone("GMT"));
				res.addHeader("Last-Modified", rfc1123Date.format(lastModTime));
			}

			// for url content type, encode a redirect to the body URL
			if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL))
			{
				if (len < MAX_URL_LENGTH) {
	
					byte[] content = resource.getContent();
					if ((content == null) || (content.length == 0))
					{
						throw new IdUnusedException(ref.getReference());
					}
	
					// An invalid URI format will get caught by the outermost catch block 
					URI uri = new URI(new String(content, "UTF-8"));
					eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_READ, resource.getReference(null), false));
					
					//SAK-23587 process any macros present in this URL
					String decodedUrl = URLDecoder.decode(uri.toString(), "UTF-8");
					decodedUrl = expandMacros(decodedUrl);
					
					res.sendRedirect(decodedUrl);
					
				} else {
					// we have a text/url mime type, but the body is too long to issue as a redirect
					throw new EntityNotDefinedException(ref.getReference());
				}
			}

			else
			{
				// use the last part, the file name part of the id, for the download file name
				String fileName = Validator.getFileName(ref.getId());
				String disposition = null;

				if (Validator.letBrowserInline(contentType))
				{
					// if this is an html file we have more checks
				    String lcct = contentType.toLowerCase();
				    if ( ( lcct.startsWith("text/") || lcct.startsWith("image/") 
				            || lcct.contains("html") || lcct.contains("script") ) && 
				            m_serverConfigurationService.getBoolean(SECURE_INLINE_HTML, true)) {
				        // increased checks to handle more mime-types - https://jira.sakaiproject.org/browse/KNL-749

						boolean fileInline = false;
						boolean folderInline = false;

						try {
							fileInline = rp.getBooleanProperty(ResourceProperties.PROP_ALLOW_INLINE);
						}
						catch (EntityPropertyNotDefinedException e) {
							// we expect this so nothing to do!
						}

						if (!fileInline) 
						try
						{
							folderInline = resource.getContainingCollection().getProperties().getBooleanProperty(ResourceProperties.PROP_ALLOW_INLINE);
						}
						catch (EntityPropertyNotDefinedException e) {
							// we expect this so nothing to do!
						}		
						
						if (fileInline || folderInline) {
							disposition = Web.buildContentDisposition(fileName, false);
						}
					} else {
						disposition = Web.buildContentDisposition(fileName, false);
					}
				}
				
				// drop through to attachment
				if (disposition == null)
				{
					disposition = Web.buildContentDisposition(fileName, true);
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

				// KNL-1316 let's see if the user already has a cached copy. Code copied and modified from Tomcat DefaultServlet.java
				long headerValue = req.getDateHeader("If-Modified-Since");
				if (headerValue != -1 && (lastModTime < headerValue + 1000)) {
					// The entity has not been modified since the date specified by the client. This is not an error case.
					res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					return; 
				}

				// If there is a direct link to the asset, no sense streaming it.
				// Send the asset directly to the load-balancer or to the client
				URI directLinkUri = m_storage.getDirectLink(resource);

				ArrayList<Range> ranges = parseRange(req, res, len);
				if (directLinkUri != null || req.getHeader("Range") == null || (ranges == null) || (ranges.isEmpty())) {
					res.addHeader("Accept-Ranges", "none");
					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);
					// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4187336
					if (len <= Integer.MAX_VALUE) {
						res.setContentLength((int)len);
					} else {
						res.addHeader("Content-Length", Long.toString(len));
					}
					
					// SAK-30455: Track event now so the direct link still records a content.read
					eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_READ, resource.getReference(null), false));

					// Bypass loading the asset and just send the user a link to it.
					if (directLinkUri != null) {
						if (m_serverConfigurationService.getBoolean("cloud.content.sendfile", false)) {
							int hostLength = new String(directLinkUri.getScheme() + "://" + directLinkUri.getHost()).length();
							String linkPath = "/sendfile" + directLinkUri.toString().substring(hostLength);
							if (log.isDebugEnabled()) {
								log.debug("X-Sendfile: " + linkPath);
							}

							// Nginx uses X-Accel-Redirect and Apache and others use X-Sendfile
							res.addHeader("X-Accel-Redirect", linkPath);
							res.addHeader("X-Sendfile", linkPath);
							return;
						}
						else if (m_serverConfigurationService.getBoolean("cloud.content.directurl", true)) {
							res.sendRedirect(directLinkUri.toString());
							return;
						}
					}

					// stream the content using a small buffer to keep memory managed
					InputStream content = null;
					OutputStream out = null;
	
					try
					{
						content = resource.streamContent();
						if (content == null)
						{
							throw new IdUnusedException(ref.getReference());
						}


						// set the buffer of the response to match what we are reading from the request
						if (len < STREAM_BUFFER_SIZE)
						{
							res.setBufferSize((int)len);
						}
						else
						{
							res.setBufferSize(STREAM_BUFFER_SIZE);
						}
	
						out = res.getOutputStream();
	
						copyRange(content, out, 0, len-1);
					}
					catch (ServerOverloadException e)
					{
						throw e;
					}
					catch (Exception ignore)
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
							catch (Exception ignore)
							{
							}
						}
					}
		        } 
		        else 
		        {
		            // Output partial content. Adapted from Apache Tomcat 5.5.27 DefaultServlet.java
		            res.addHeader("Accept-Ranges", "bytes");
		            res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

		            if (ranges.size() == 1) {

		            	// Single response
		            	
		                Range range = (Range) ranges.get(0);
		                res.addHeader("Content-Range", "bytes "
		                                   + range.start
		                                   + "-" + range.end + "/"
		                                   + range.length);
		                long length = range.end - range.start + 1;
		                if (length < Integer.MAX_VALUE) {
		                    res.setContentLength((int) length);
		                } else {
		                    // Set the content-length as String to be able to use a long
		                    res.setHeader("content-length", "" + length);
		                }

						res.addHeader("Content-Disposition", disposition);

		                if (contentType != null) {
		                    res.setContentType(contentType);
		                }

						// stream the content using a small buffer to keep memory managed
						InputStream content = null;
						OutputStream out = null;
		
						try
						{
							content = resource.streamContent();
							if (content == null)
							{
								throw new IdUnusedException(ref.getReference());
							}

							// set the buffer of the response to match what we are reading from the request
							if (len < STREAM_BUFFER_SIZE)
							{
								res.setBufferSize((int)len);
							}
							else
							{
								res.setBufferSize(STREAM_BUFFER_SIZE);
							}
		
							out = res.getOutputStream();

							copyRange(content, out, range.start, range.end);

						}
						catch (ServerOverloadException e)
						{
							throw e;
						}
						catch (SocketException e)
						{
							//a socket exception usualy means the client aborted the connection or similar
							if (log.isDebugEnabled())
							{
								log.debug("SocketExcetion", e);
							}
						}
						catch (Exception ignore)
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
								catch (IOException ignore)
								{
									// ignore
								}
							}
						}
		              
		            } else {

		            	// Multipart response

		            	res.setContentType("multipart/byteranges; boundary=" + MIME_SEPARATOR);

						// stream the content using a small buffer to keep memory managed
						OutputStream out = null;
		
						try
						{
							// set the buffer of the response to match what we are reading from the request
							if (len < STREAM_BUFFER_SIZE)
							{
								res.setBufferSize((int)len);
							}
							else
							{
								res.setBufferSize(STREAM_BUFFER_SIZE);
							}
		
							out = res.getOutputStream();

			            	copyRanges(resource, out, ranges.iterator(), contentType);

						}
						catch (SocketException e)
						{
							//a socket exception usualy means the client aborted the connection or similar
							if (log.isDebugEnabled())
							{
								log.debug("SocketExcetion", e);
							}
						}
						catch (Exception ignore)
						{
							log.error("Swallowing exception", ignore);
						}
						finally
						{
							// be a good little program and close the stream - freeing up valuable system resources
							if (out != null)
							{
								try
								{
									out.close();
								}
								catch (IOException ignore)
								{
									// ignore
								}
							}
						}
		              
		            } // output multiple ranges

		        } // output partial content 

			} // output resource

		}
		catch (Exception t)
		{
			throw new EntityNotDefinedException(ref.getReference(), t);
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
				log.warn("handleAccessCollection: redirecting to " + addr + " : " + e);
			}
		}

		// need read permission
		if (!allowGetResource(ref.getId()))
			throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), AUTH_RESOURCE_READ, ref.getReference());

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
			m_collectionAccessFormatter.format(collection, ref, req, res, rb, this);

			// track event
			// eventTrackingService.post(eventTrackingService.newEvent(EVENT_RESOURCE_READ, collection.getReference(), false));
		}
		catch (Exception t)
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
							log.warn("handleAccess: redirecting to " + addr + " : " + e);
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
				// Logger this and assume it's not a collection
				log.warn("EntityPropertyTypeException: PROP_IS_COLLECTION not boolean for " + ref.getReference());
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
			log.warn("PermissionException " + ref.getReference());
		}
		catch (IdUnusedException e)
		{
			log.warn("IdUnusedException " + ref.getReference());
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			log.warn("TypeException " + ref.getReference());
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
	    // static code review possible NPE fix -AZ
	    if (ref == null || ref.getId() == null) {
	        log.warn("ref passed into getEntityAuthzGroups is not valid (ref or ref.getId is null): " + ref);
	        return null;
	    }

	    // double check that it's mine
		if (!APPLICATION_ID.equals(ref.getType())) return null;

		// form a key for thread-local caching
		String threadLocalKey = "getEntityAuthzGroups@" + userId + "@" + ref.getReference();
		Collection rv = (Collection) threadLocalManager.get(threadLocalKey);
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
			// special check for group-user : the grant's in the user's Home site
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
					rv.add(getReference(ref.getId())); // SAK-15657
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

			// this piece of code only comes into effect when the ref id is not resolveable as is
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

			// this will ensure the NPE does not happen
			if (entity == null) {
	            log.warn("ref ("+ref+") is not resolveable as an entity (it is null)");
	            return null;
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
				String siteReference = m_siteService.siteReference(ref.getContext());
				boolean useSiteAsContext = false;
				if(siteReference != null && userId != null)
				{
					useSiteAsContext = m_securityService.unlock(userId, AUTH_RESOURCE_ALL_GROUPS, siteReference);
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
		catch (Exception e)
		{
		}

		// cache in the thread
		threadLocalManager.set(threadLocalKey, new ArrayList(rv));

		if (log.isDebugEnabled())
		{
			log.debug("getEntityAuthzGroups for: ref: " + ref.getReference() + " user: " + userId);
			for (Iterator i = rv.iterator(); i.hasNext();)
			{
				log.debug("** -- " + i.next());
			}
		}
		return rv;
	}

	protected Collection getEntityHierarchyAuthzGroups(Reference ref) 
	{
		Collection<String> rv = new TreeSet<String>();

		// add the root
		rv.add(getReference("/"));

		// try the resource, all the folders above it (don't include /)
		String paths[] = StringUtil.split(ref.getId(), Entity.SEPARATOR);
		boolean container = ref.getId().endsWith(Entity.SEPARATOR);
		if (paths.length > 1)
		{
			String root = getReference(Entity.SEPARATOR + paths[1] + Entity.SEPARATOR);
			rv.add(root);
			StringBuilder rootBuilder = new StringBuilder();
			rootBuilder.append(root);
			
			for (int next = 2; next < paths.length; next++)
			{
				rootBuilder.append(paths[next]);
				if ((next < paths.length - 1) || container)
				{
					rootBuilder.append(Entity.SEPARATOR);
				}
				rv.add(rootBuilder.toString());
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
		Element element = doc.createElement(ContentHostingService.class.getName());
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
				log.warn("archveResources: exception archiving resource: " + ref + ": ", any);
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
					String relId = StringUtils.trimToNull(element.getAttribute("rel-id"));
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
						String id = StringUtils.trimToNull(element.getAttribute("id"));
						String relId = StringUtils.trimToNull(element.getAttribute("rel-id"));

						// escape the invalid characters
						id = Validator.escapeQuestionMark(id);
						relId = Validator.escapeQuestionMark(relId);

						// if it's attachment, assign a new attachment folder
						if (id.startsWith(ATTACHMENTS_COLLECTION))
						{
							String oldRef = getReference(id);

							// take the name from after /attachment/whatever/
							id = ATTACHMENTS_COLLECTION + idManager.createUuid()
							+ id.substring(id.indexOf('/', ATTACHMENTS_COLLECTION.length()));

							// record the rename
							attachmentNames.put(oldRef, id);
						}

						// otherwise move it into the site
						else
						{
							if (relId == null)
							{
								log.warn("mergeContent(): no rel-id attribute in resource");
								continue;
							}

							id = getSiteCollection(siteId) + relId;
						}

						element.setAttribute("id", id);

						ContentResource r = null;

						// if the body-location attribute points at another file for the body, get this
						String bodyLocation = StringUtils.trimToNull(element.getAttribute("body-location"));
						if (bodyLocation != null)
						{
							// the file name is relative to the archive file
							String bodyPath = StringUtil.fullReference(archivePath, bodyLocation);

							// get a stream from the file
							FileInputStream in = new FileInputStream(bodyPath);

							// resource: add if missing
							r = mergeResource(element, in);
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
			log.error("mergeContent(): exception: ", any);
		}

		return results.toString();

	} // merge

	/**
	 * {@inheritDoc}
	 */
	public void updateEntityReferences(String toContext, Map transversalMap){
		//TODO: is there any content that needs reference updates?
		String fromContext = (String) transversalMap.get("/fromContext");
		String thisKey = null;
		try {
			List thisTargetResourceList = getAllResources(fromContext);
			Iterator sourceResourceIterator = thisTargetResourceList.iterator();
			String tId;
			String rContent;
			while(sourceResourceIterator.hasNext()){
				ContentResource thisContentResource = (ContentResource) sourceResourceIterator.next();
				tId = thisContentResource.getId();
				String sourceType = thisContentResource.getContentType();
				if(sourceType.startsWith("text/html")){
					String oldReference = tId;
					tId = siteIdSplice(tId, toContext);
					ContentResource oldSiteContentResource = getResource(oldReference);
					byte[] thisResourceContentRaw = oldSiteContentResource.getContent();
					rContent = new String(thisResourceContentRaw);
					StringBuffer saveOldEntity = new StringBuffer(rContent);
					Iterator contentKeys = transversalMap.keySet().iterator();
					while(contentKeys.hasNext()){							
						String oldValue = (String) contentKeys.next();
						if(!oldValue.equals("/fromContext")){
							String newValue = "";
							newValue = (String) transversalMap.get(oldValue);
							if(newValue.length()>0){
								rContent = linkMigrationHelper.migrateOneLink(oldValue, newValue, rContent);
								}
								}
							}
					try {
						rContent = linkMigrationHelper.bracketAndNullifySelectedLinks(rContent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						log.debug ("Forums LinkMigrationHelper.editLinks failed" + e);
					}					
					try {
						if(!saveOldEntity.toString().equals(rContent)){
							ContentResourceEdit edit = editResource(tId);
							edit.setContent(rContent.getBytes());
							m_storage.commitResource(edit);
						}
					} catch (InUseException e6) {
						// TODO Auto-generated catch block
						log.error(this + thisKey, e6);
					}catch (PermissionException e1) {
						log.error(this + thisKey, e1);
					} catch (IdUnusedException e2) {
						log.error(this + thisKey, e2);
					} catch (TypeException e3) {
						log.error(this + thisKey, e3);
					} 
	
				}
			}
		} catch (PermissionException e1) {
			log.error(this + thisKey, e1);
		} catch (IdUnusedException e2) {
			log.error(this + thisKey, e2);
		} catch (TypeException e3) {
			log.error(this + thisKey, e3);
		} catch (ServerOverloadException e4) {
			log.error(this + thisKey, e4);
		}
		
	}
	
	private String siteIdExtract(String ref){
		String[] components = ref.split("/");
		return components[2];
	}
	
	private String siteIdSplice(String ref, String siteId){
		String[] components = ref.split("/");
		StringBuffer splicedString = new StringBuffer();
		for(int i=1;i< components.length;i++){
			splicedString.append("/");
			if(i==2){
				splicedString.append(siteId);
			}else{
				splicedString.append(components[i]);
			}
		}
		return splicedString.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds){
		transferCopyEntitiesRefMigrator(fromContext, toContext, resourceIds);
	}


	public Map<String, String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List resourceIds)

	{
		Map transversalMap = new HashMap();
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
			ContentCollectionEdit toCollectionEdit = null;
			
			// not such collection yet, add one
			try
			{
				toCollectionEdit = addCollection(toContext);
				m_storage.commitCollection(toCollectionEdit);
				((BaseCollectionEdit) toCollectionEdit).closeEdit();
				
				//try this again now to get an activated collection
				try
				{
					toCollection = getCollection(toContext);
				}
				catch (IdUnusedException eee)
				{
					log.error(this + toContext, eee);
				}
				catch (TypeException eee)
				{
					log.error(this + toContext, eee);
				}
			}
			catch(IdUsedException ee)
			{
				log.error(this + toContext, ee);
			}
			catch(IdInvalidException ee)
			{
				log.error(this + toContext, ee);
			}
			catch (PermissionException ee)
			{
				log.error(this + toContext, ee);
			}
			catch (InconsistentException ee)
			{
				log.error(this + toContext, ee);
			}
			finally
			{
				//safety first!
				if (toCollectionEdit != null && toCollectionEdit.isActiveEdit()) {
					((BaseCollectionEdit) toCollectionEdit).closeEdit();
				}
			}
		}
		catch (TypeException e)
		{
			log.error(this + toContext, e);
		}
		catch (PermissionException e)
		{
			log.error(this + toContext, e);
		}

		if (toCollection != null)
		{
			// get the list of all resources for importing
			try
			{
				// get the root collection
				ContentCollection oCollection = getCollection(fromContext);

				// Copy the Resource Properties from Root Collection to New Root Collection
				ResourceProperties oCollectionProperties = oCollection.getProperties();
				ContentCollectionEdit toCollectionEdit = (ContentCollectionEdit) toCollection;
				ResourcePropertiesEdit toColPropEdit = toCollectionEdit.getPropertiesEdit();
				toColPropEdit.clear();
				toColPropEdit.addAll(oCollectionProperties);
				hideImportedContent(toCollectionEdit);
				m_storage.commitCollection(toCollectionEdit);

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
						String nUrl = "";

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
								edit.setAvailability(((ContentCollection) oResource).isHidden(), ((ContentCollection) oResource).getReleaseDate(), ((ContentCollection) oResource).getRetractDate());
								// SAK-23305
								hideImportedContent(edit);
								// complete the edit
								m_storage.commitCollection(edit);
								((BaseCollectionEdit) edit).closeEdit();
								nUrl = edit.getUrl();
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
							transversalMap.put(oResource.getId(), nId);
							transversalMap.put(oResource.getUrl(), nUrl);
							transversalMap.putAll(transferCopyEntitiesRefMigrator(oResource.getId(), nId, resourceIds));
						}
						else
						{
							try
							{
								// add resource
								ContentResourceEdit edit = addResource(nId);
								edit.setContentType(((ContentResource) oResource).getContentType());
								edit.setResourceType(((ContentResource) oResource).getResourceType());
								edit.setContent(((ContentResource) oResource).streamContent());
								edit.setAvailability(((ContentResource) oResource).isHidden(), ((ContentResource) oResource).getReleaseDate(), ((ContentResource) oResource).getRetractDate());
								//edit.setContent(((ContentResource) oResource).getContent());
								// import properties
								ResourcePropertiesEdit p = edit.getPropertiesEdit();
								p.clear();
								p.addAll(oProperties);
								// SAK-23305
								hideImportedContent(edit);
								// complete the edit
								m_storage.commitResource(edit);
								((BaseResourceEdit) edit).closeEdit();
								nUrl = edit.getUrl();
								transversalMap.put(oResource.getId(), nId);
								transversalMap.put(oResource.getUrl(), nUrl);

								ContentChangeHandler cch = m_resourceTypeRegistry.getContentChangeHandler(((ContentResource) oResource).getResourceType());
								if (cch!=null){
									cch.copy(((ContentResource) oResource));
								}
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
		transversalMap.put("/fromContext", fromContext);
		return transversalMap;
	} // importResources

	/**
	 * Hide imported content -- SAK-23305
	 * @param edit Object either a ContentResourceEdit or ContentCollectionEdit object
	 */
	private void hideImportedContent(Object edit)
	{
		if (m_serverConfigurationService.getBoolean("content.import.hidden", false))
		{
			ContentResourceEdit resource = null;
			ContentCollectionEdit collection = null;
			String containingCollectionId = null;
			if (edit instanceof ContentResourceEdit) 
			{
				resource = (ContentResourceEdit) edit;
				containingCollectionId = resource.getContainingCollection().getId();
			}
			else if (edit instanceof ContentCollectionEdit)
			{
				collection = (ContentCollectionEdit) edit;
				containingCollectionId = collection.getContainingCollection().getId();
			}
			if (resource != null || collection != null)
			{
				/*
				 * If this is "reuse content" during worksite setup, the site collection at this time is
				 * /group/!admin/ for all content including ones in the folders, so count how many "/" in
				 * the collection ID. If <= 3, then it's a top-level item and needs to be hidden.
				 */
				int slashcount = StringUtils.countMatches(containingCollectionId, "/");
				if (slashcount <= 3)
				{
					if (resource != null)
					{
						resource.setHidden();
					}
					else if (collection != null)
					{
						collection.setHidden();
					}
				}
			}
		}
	}

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
					userDirectoryService.getUser(parts[2]);
				}
				catch (UserNotDefinedException tryEid)
				{
					try
					{
						// try using it as an EID
						String userId = userDirectoryService.getUserId(parts[2]);

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
			log.error("PermissionEception:", e);
		} catch (IdUnusedException e) {
			log.error("IdUnusedException:", e);
		} catch (EntityPropertyNotDefinedException e) {
			log.error("EntityPropertyNotDefinedException:", e);
		} catch (EntityPropertyTypeException e) {
			log.error("EntityPropertyTypeException:", e);
		} catch (TypeException e) {
			log.error("TypeException:", e);
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
						log.warn("enableResources: " + e);
						collection = findCollection(id);
					}
					catch (InconsistentException e)
					{
						// Because the id is coming from getSiteCollection(), this will never occur.
						// If it does, we better get alerted to it.
						log.warn("enableResources: " + e);
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
						log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
					catch (PermissionException e)
					{
						log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
					catch (InUseException e)
					{
						log.warn("enableResources: " + e);
						throw new RuntimeException(e);
					}
				}
			}
			catch (TypeException e)
			{
				log.warn("enableResources: " + e);
				throw new RuntimeException(e);
			}
		}
		catch (IdUnusedException e)
		{
			// TODO: -ggolden
			log.warn("enableResources: " + e);
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
		// form the xml
		Element el = resource.toXml(doc, stack);

		// remove the content from the xml
		el.removeAttribute("body");

		// write the content to a file
		String fileName = idManager.createUuid();
		InputStream stream = null;
		FileOutputStream out = null;
		try
		{
			stream = resource.streamContent();
			out = new FileOutputStream(storagePath + fileName);
			byte[] chunk = new byte[STREAM_BUFFER_SIZE];
			int lenRead;
			while ((lenRead = stream.read(chunk)) != -1)
			{
				out.write(chunk, 0, lenRead);
			}
		}
		catch (IOException e)
		{
			log.warn("archiveResource(): while writing body for: " + resource.getId() + " : " + e);
		} catch (ServerOverloadException e) {
			log.warn("archiveResource(): while writing body for: " + resource.getId() + " : " + e);
		}
		finally
		{
			if (stream != null)
			{
				try
				{
					stream.close();
				}
				catch (IOException e)
				{
					log.error("IOException ", e);
				}
			}

			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					log.error("IOException ", e);
				}
			}
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
			String now = timeService.newTime().toString();
			edit.getProperties().addProperty(ResourceProperties.PROP_CREATION_DATE, now);
		}
		catch(EntityPropertyTypeException epte)
		{
			log.error(epte.getMessage(), epte);
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
	 * @deprecated Use {@link #mergeResource(Element, InputStream)}. (KNL-898)
	 */
	protected ContentResource mergeResource(Element element) throws PermissionException, InconsistentException, IdInvalidException,
	OverQuotaException, ServerOverloadException
	{
		return mergeResource(element, (InputStream) null);

	} // mergeResource

	/**
	 * Merge in a resource from an XML DOM definition and a body bytes array. Ignore if already defined. Take whole if not.
	 * 
	 * @param element
	 *        The XML DOM element containing the collection definition.
	 * @param in
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
	protected ContentResource mergeResource(Element element, InputStream in) throws PermissionException, InconsistentException,
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

		// if input stream is provided, use it
		if (in != null)
		{
			edit.setContent(in);
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
		eventTrackingService.post(eventTrackingService.newEvent(((BaseResourceEdit) edit).getEvent(), ref, true,
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
		
		String[] idSegments;
		idSegments = id.split("/");
		String filename = idSegments.length > 0 ? idSegments[idSegments.length - 1]:id;
		String extension = Validator.getFileExtension(filename);

		if (extension.length() != 0)
		{
			// if there's a file extension and a blank, null or unknown(application/binary) mime type,
			// fix the mime type by doing a lookup based on the extension
			if (((type == null) || (type.length() == 0) || (contentTypeImageService.isUnknownType(type))))
			{
				extType.put("type", contentTypeImageService.getContentType(extension));
			}
		}
		else
		{
			// if there is no file extension, but a non-null, non-blank mime type, do a lookup based on the mime type and add an extension
			// if there is no extension, find one according to the MIME type and add it.
			if ((type != null) && (!type.equals("")) && (!contentTypeImageService.isUnknownType(type)))
			{
				extension = contentTypeImageService.getContentTypeExtension(type);
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
		if (!((edit.getId().startsWith(COLLECTION_USER)) || isInSiteCollection(edit.getId()) || edit.getId().startsWith(COLLECTION_DROPBOX))) return false;

		// expect null, "user" | "group", user/groupid, rest...
		String[] parts = StringUtil.split(edit.getId(), Entity.SEPARATOR);
		if (parts.length <= 2) return false;

		// get this collection
		String id = Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR;
		ContentCollection collection = null;
		try
		{
			collection = findCollection(id);
			// Limit size per user inside dropbox
			if (edit.getId().startsWith(COLLECTION_DROPBOX)) {
				try {
					// if successful, the context is already a valid user id
					userDirectoryService.getUser(parts[3]);
					collection = findCollection(id + parts[3] + Entity.SEPARATOR);
				} catch (UserNotDefinedException tryEid) {
					// Nothing to do
				}
			}
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
			log.error("File size column is not ready. Unable to calculate size of collection. Something is wrong with this instance of Sakai. Please check for other startup errors.");
			return false;
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

	protected static class SizeHolder {

		public long ttl = System.currentTimeMillis()+600000L;
		public long size = 0;

	}
	private Map<String, SizeHolder> quotaMap = new ConcurrentHashMap<String, SizeHolder>();

	private Map<String, SiteContentAdvisorProvider> siteContentAdvisorsProviders = new HashMap<String, SiteContentAdvisorProvider>();

	/**
	 * @param collection
	 * @return
	 */

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
		if (!((edit.getId().startsWith("/user/")) || isInSiteCollection(edit.getId()))) return;

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
		if (!((edit.getId().startsWith("/user/")) || isInSiteCollection(edit.getId()))) return;

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
	 * gets the quota for a site collection or for a user's Home collection
	 *
	 * @param collection the collection on which to test for a quota.  this can be the collection for a site
	 * or a user's workspace collection
	 * @return the quota in kb
	 */
	public long getQuota(ContentCollection collection) {
		long quota = m_siteQuota;
		String default_quota = DEFAULT_RESOURCE_QUOTA;
		ContentCollection parentCollection = null;
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
			if (collection.getId().startsWith(COLLECTION_DROPBOX)) {
				default_quota = DEFAULT_DROPBOX_QUOTA;
				quota = m_dropBoxQuota;
				try {
					parentCollection = findCollection(Entity.SEPARATOR + parts[1] + Entity.SEPARATOR + parts[2] + Entity.SEPARATOR);
				} catch (TypeException tex) {
				}
			}
			String siteType = null;
			// get the site type
			try {
				siteType = m_siteService.getSite(siteId).getType();
			} catch (IdUnusedException e) {
				log.error("Quota calculation could not find the site '"+ siteId + "' to determine the type.", e);
			}

			// use this quota unless we have one more specific
			if (siteType != null) {
				quota = Long.parseLong(m_serverConfigurationService.getString(default_quota + siteType, Long.toString(collection.getId().startsWith(COLLECTION_DROPBOX)?m_dropBoxQuota:m_siteQuota)));
			}
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
			// Look for dropBox quota in parent collection
			try {
				if (parentCollection!=null) {
					long siteSpecific = parentCollection.getProperties().getLongProperty(
							ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
	
					quota = siteSpecific;
				}
			} catch (EntityPropertyTypeException ignoretex) {
				// don't log or anything, this just means that this site doesn't have this quota property.
			} catch (EntityPropertyNotDefinedException ignoreex) {
				// don't log or anything, this just means that this site doesn't have this quota property.
			}
		}
		catch (Exception ignore)
		{
			log.warn("getQuota: reading quota property of : " + collection.getId() + " : " + ignore);
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
			StringBuilder idBuilder = new StringBuilder();
			idBuilder.append(id);
			for (int i = 1; i < parts.length; i++)
			{
				// grow the id to the next collection
				idBuilder.append(parts[i] + "/");

				// does it exist?
				ContentCollection collection = findCollection(idBuilder.toString());

				// if not, can we make it
				if (collection == null)
				{
					ContentCollectionEdit edit = addValidPermittedCollection(idBuilder.toString());
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, parts[i]);
					commitCollection(edit);
				}
			}
		}
		// if we cannot, give up
		catch (Exception any)
		{
			log.error("generateCollections: " + any.getMessage(), any);
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
		User anon = userDirectoryService.getAnonymousUser();
		return m_securityService.unlock(anon, AUTH_RESOURCE_READ, getReference(id));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInheritingPubView(String id)
	{
		// the root does not inherit... and makes a bad ref if we try to isolateContainingId()
		if (isRootCollection(id)) return false;

		// check for access on the container
		String containerId = isolateContainingId(id);
		return isPubView(containerId);
	}

	/**
	 * @inheritDoc
	 * @see org.sakaiproject.content.api.ContentHostingService#setPubView(String, boolean)
	 */
	public void setPubView(String id, boolean pubview)
	{
		try {
			setRoleView(id, AuthzGroupService.ANON_ROLE, pubview);
		} catch (AuthzPermissionException e) {
			// Catching to prevent breaking the existing implementation
			log.warn("BaseContentService#setPubView: Did not have permission to create a realm for " + getReference(id));
		}
	}

	/**
	 * @inheritDoc
	 * @see org.sakaiproject.content.api.ContentHostingService#setRoleView(String, String, boolean)
	 */
	public void setRoleView(String id, String roleId, boolean grantAccess) throws AuthzPermissionException {

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
			if (grantAccess)
			{
				try
				{
					edit = m_authzGroupService.addAuthzGroup(ref);
				} catch (GroupIdInvalidException e1) {
					log.warn("BaseContentService#setRoleView: Failed to add AZG (" + ref + "): " + e1);
				} catch (GroupAlreadyDefinedException e1) {
					log.warn("BaseContentService#setRoleView: Failed to add AZG (" + ref + "): " + e1);
				}
			}
		}

		// if we have no realm and don't need one, we are done
		// if we need a realm and didn't get an edit, exception
		if (edit == null) {
		    return;
		}

		// this makes no sense... -AZ
//		// if we have no realm and don't need one, we are done
//		if ((edit == null) && (!pubview)) return;
//
//		// if we need a realm and didn't get an edit, exception
//		if ((edit == null) && pubview) return;

		boolean changed = false;
		boolean delete = false;

		// align the realm with our positive setting
		if (grantAccess)
		{
			// make sure the role exists and has "content.read"
			Role role = edit.getRole(roleId);
			if (role == null)
			{
				try
				{
					role = edit.addRole(roleId);
				}
				catch (RoleAlreadyDefinedException e)
				{
					throw new IllegalStateException("BaseContentService#setRoleView: Received RoleAlreadyDefined on non-existent role", e);
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
			Role role = edit.getRole(roleId);
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
				log.error("BaseContentService#setRoleView: The group we were using stopped existing: " + e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.sakaiproject.content.api.ContentHostingService#isRoleView(String, String)
	 */
	public boolean isRoleView(final String id, final String roleId) {
		if(roleId == null) {
			return false;
		}
		String dummyUserId = m_authzGroupService.encodeDummyUserForRole(roleId);
		return m_securityService.unlock(dummyUserId, AUTH_RESOURCE_READ, getReference(id));
	}

	/**
	 * {@inheritDoc}
	 * @see org.sakaiproject.content.api.ContentHostingService#isInheritingRoleView(String, String)
	 */
	public boolean isInheritingRoleView(final String id, final String roleId) {
		// the root does not inherit... and makes a bad ref if we try to isolateContainingId()
		if (isRootCollection(id)) return false;

		// check for access on the container
		String containerId = isolateContainingId(id);
		return isRoleView(containerId, roleId);
	}

	/**
	 * {@inheritDoc}
	 * @see org.sakaiproject.content.api.ContentHostingService#getRoleViews(String)
	 */
	public Set<String> getRoleViews(final String id) {
		String ref = getReference(id);
		LinkedHashSet<String> roleIds = new LinkedHashSet<String>();
		AuthzGroup realm = null;

		try {
			realm = m_authzGroupService.getAuthzGroup(ref);
		} catch (GroupNotDefinedException e) {
			// if there is no authz group then no roles can have been defined.
			return roleIds;
		}

		Set<Role> roles = realm.getRoles();
		for (Role role : roles) {
			if(role.isAllowed(AUTH_RESOURCE_READ)) {
				roleIds.add(role.getId());
			}
		}

		return roleIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ContentResource> findResources(String type, String primaryMimeType, String subMimeType,  Set<String> contextIds)
	{
		List globalList = new ArrayList();

		Iterator siteIt = contextIds.iterator();
		while (siteIt.hasNext())
		{
			String collId = getSiteCollection( (String)siteIt.next() );
			List artifacts = getFlatResources(collId);
			globalList.addAll(filterArtifacts(artifacts, type, primaryMimeType, subMimeType, true));
		}

		return globalList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ContentResource> findResources(String type, String primaryMimeType, String subMimeType)
	{
		List globalList = new ArrayList();

		Map othersites = getCollectionMap();
		Iterator siteIt = othersites.entrySet().iterator();
		while (siteIt.hasNext())
		{
			Entry entry = (Entry) siteIt.next();
			String collId = (String) entry.getKey();
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
		return getDropboxCollection(toolManager.getCurrentPlacement().getContext());
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

		// for maintainers or users with groups access, use the site level
		if ((isDropboxMaintainer(siteId))||(isDropboxGroups(siteId)))
		{
			// return the site's dropbox collection
			return rv;
		}

		// Anonymous users do not get drop boxes
		String userId = sessionManager.getCurrentSessionUserId();
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
		return getDropboxDisplayName(toolManager.getCurrentPlacement().getContext());
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
		return getDisplayName(null);
	}

	/**
	 * Create the site's dropbox collection and one for each qualified user that the current user can make.
	 */
	public void createDropboxCollection()
	{
		createDropboxCollection(toolManager.getCurrentPlacement().getContext());
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
		if (!isDropboxMaintainer(siteId) && !isDropboxGroups(siteId))
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
					log.error(e.getMessage(), e);
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
			log.warn("createDropboxCollection: TypeException: " + dropbox);
			return;
		}
		catch (IdUsedException e)
		{
			log.warn("createDropboxCollection: IdUsedException: " + dropbox);
			return;
		}
		catch (InconsistentException e)
		{
			log.warn("createDropboxCollection(): InconsistentException: " + dropbox);
			log.warn("createDropboxCollection(): InconsistentException: " + e.getMessage());
			return;
		}
		//		catch (PermissionException e) 
		//		{
		//			log.warn("createDropboxCollection(): PermissionException: " + dropbox);
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
			log.warn("createDropboxCollection(): File exists where dropbox collection is expected: "+ dropbox);
		}

		// The AUTH_DROPBOX_OWN is granted within the site, so we can ask for all the users who have this ability
		// using just the dropbox collection
		List users = m_securityService.unlockUsers(AUTH_DROPBOX_OWN, getReference(dropbox));
		for (Iterator it = users.iterator(); it.hasNext();)
		{
			User user = (User) it.next();

			// the folder id for this user's dropbox in this group
			String userFolder = dropbox + user.getId() + "/";

			// see if it exists - add if it doesn't
			try
			{
				
				if (!members.remove(userFolder))
				{
					if (findCollection(userFolder) == null) // This check it probably redundant
					{
						ContentCollectionEdit edit = addValidPermittedCollection(userFolder);
						ResourcePropertiesEdit props = edit.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME,getDisplayName(user));
						props.addProperty(ResourceProperties.PROP_DESCRIPTION, rb.getString("use1"));
						commitCollection(edit);
					}
				}
			}
			catch (TypeException e)
			{
				log.warn("createDropboxCollectionn(): TypeException: " + userFolder);
			}
			catch (IdUsedException e)
			{
				log.warn("createDropboxCollectionn(): idUsedException: " + userFolder);
			}
			catch (InconsistentException e)
			{
				log.warn("createDropboxCollection(): InconsistentException: " + userFolder);
			}
		}
		// Attempt to remove all empty dropboxes that are no longer members of the site.
		for(String member : members) {
			try
			{
				ContentCollection folder = getCollection(member);
				if (folder.getMemberCount() == 0)
				{
					removeCollection(member);
					log.info("createDropboxCollection(): Removed the empty dropbox collection for member: " + member);
				} else {
				    log.warn("createDropboxCollection(): Could not remove the dropbox collection for member (" + member +") because the root contains "+folder.getMemberCount()+" members");
				}
			}
			catch(IdUnusedException e)
			{
				log.warn("createDropboxCollection(): Could not find collection to delete: " + member);
			}
			catch(PermissionException e)
			{
				log.warn("createDropboxCollection(): Unable to delete collection due to lack of permission: " + member);
			}
			catch(InUseException e)
			{
				log.warn("createDropboxCollection(): Unable to delete collection as collection is in use: " + member);
			}
			catch(ServerOverloadException e)
			{
				log.warn("createDropboxCollection(): Unable to delete collection as server is overloaded: " + member);
			}
			catch(TypeException e)
			{
				log.warn("createDropboxCollection(): Unable to delete as it doesn't appear to be a collection: " + member);
			}
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


			User user = userDirectoryService.getCurrentUser();

			// the folder id for this user's dropbox in this group
			String userFolder = dropbox + user.getId() + "/";

			if(m_securityService.unlock(AUTH_DROPBOX_OWN, getReference(dropbox)))
			{
				// see if it exists - add if it doesn't
				try
				{
					if (findCollection(userFolder) == null)
					{
						ContentCollectionEdit edit = addValidPermittedCollection(userFolder);
						ResourcePropertiesEdit props = edit.getPropertiesEdit();
						props.addProperty(ResourceProperties.PROP_DISPLAY_NAME,getDisplayName(user));
						props.addProperty(ResourceProperties.PROP_DESCRIPTION, rb.getString("use1"));
						// props.addProperty(ResourceProperties.PROP_DESCRIPTION, PROP_MEMBER_DROPBOX_DESCRIPTION);
						commitCollection(edit);
					}
				}
				catch (TypeException e)
				{
					log.warn("createIndividualDropbox(): TypeException: " + userFolder);
				}
				catch (IdUsedException e)
				{
					log.warn("createIndividualDropbox(): idUsedException: " + userFolder);
				}
				catch (InconsistentException e)
				{
					log.warn("createIndividualDropbox(): InconsistentException: " + userFolder);
				} 
				//				catch (PermissionException e) 
				//				{
				//					log.warn("createIndividualDropbox(): PermissionException: " + userFolder);
				//				}
			}

		} 
		catch (TypeException e) 
		{
			log.warn("createIndividualDropbox(): TypeException: " + dropbox);
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
		return isDropboxMaintainer(toolManager.getCurrentPlacement().getContext());
	}

	/**
	 * Determine whether the default dropbox collection id for this user in some site is the site's entire dropbox collection or just the current user's collection within the site's dropbox.
	 * 
	 * @return True if user sees all dropboxes in the site, false otherwise.
	 */
	public boolean isDropboxMaintainer(String siteId)
	{
		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return false;
		}

		// if the user has dropbox maintain in the site, they are the dropbox maintainer
		// (dropbox maintain in their myWorkspace just gives them access to their own dropbox)
		return m_securityService.unlock(AUTH_DROPBOX_MAINTAIN, m_siteService.siteReference(siteId));
	}

	/**
	 * Determine whether the user has the dropbox.groups permission 
	 * 
	 * @return True if user has dropbox.groups permission, false otherwise.
	 */
	public boolean isDropboxGroups(String siteId)
	{
		String dropboxId = null;

		// make sure we are in a worksite, not a workspace
		if (m_siteService.isUserSite(siteId) || m_siteService.isSpecialSite(siteId))
		{
			return false;
		}

		// if the user has dropbox maintain in the site, they are the dropbox maintainer
		// (dropbox maintain in their myWorkspace just gives them access to their own dropbox)
		return m_securityService.unlock(AUTH_DROPBOX_GROUPS, m_siteService.siteReference(siteId));
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
		String currentUser = sessionManager.getCurrentSessionUserId();

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

				if(m_securityService.isSuperUser())
				{
					rv.addAll(groups);
				}
				else if(m_securityService.unlock(AUTH_RESOURCE_ALL_GROUPS, site.getReference()) && entity != null && unlockCheck(function, entity.getId()))
				{
					rv.addAll(groups);
				}
				else
				{
					Collection hierarchy = getEntityHierarchyAuthzGroups(ref);
					String userId = sessionManager.getCurrentSessionUserId();

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
		//SAK-18908/KNL-584 id could be null
		if (id == null) {
			return null;
		}
		
		if (id.startsWith("/user/"))
		{
			try
			{
				int pos = id.indexOf('/', 6);
				String userId = id.substring(6, pos);
				String userEid = userDirectoryService.getUserEid(userId);
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

		protected boolean m_visibilityUpdated = false;

		protected boolean m_accessUpdated;;

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
			if(this.m_groups.size() > 0) {
				this.m_accessUpdated = true;
			}
			this.m_access = AccessMode.INHERITED;
			this.m_groups.clear();

		}

		/**
		 * @inheritDoc
		 */
		public void clearPublicAccess() throws PermissionException {
			try {
				removeRoleAccess(AuthzGroupService.ANON_ROLE);
			} catch (InconsistentException e) {
				log.error("BasicGroupAwareEdit#clearPublicAccess: the anon role was not defined: " + e);
			}
		}

		public void setPublicAccess() throws PermissionException, InconsistentException
		{
			addRoleAccess(AuthzGroupService.ANON_ROLE);
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEdit#addRoleAccess(String)
		 */
		public void addRoleAccess(String roleId) throws InconsistentException, PermissionException {
			if (roleId == null || roleId.isEmpty()) {
				throw new InconsistentException("BasicGroupAwareEdit#addRoleAccess - Must specify a role to remove for content " + this.getReference());
			}

			if (!this.getInheritedGroups().isEmpty()) {
				throw new InconsistentException(String.format("BasicGroupAwareEdit#addRoleAccess: could not assign role %s because content %s inherits group access.", roleId, this.getReference()));
			}

			if (getInheritedRoleAccessIds().contains(roleId)) {
				throw new InconsistentException(String.format("BasicGroupAwareEdit#addRoleAccess: could not assign role %s because content %s inherits role access.", roleId, this.getReference()));
			}

			if (!(isRoleView(this.m_id, roleId))) {
				try {
					setRoleView(this.m_id, roleId, true);
				} catch (AuthzPermissionException e) {
					throw new PermissionException(e.getUser(), e.getFunction(), e.getResource());
				}
				this.m_accessUpdated = true;
				this.m_access = AccessMode.INHERITED;
				this.m_groups.clear();
			}
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEdit#removeRoleAccess(String)
		 */
		public void removeRoleAccess(String roleId) throws InconsistentException, PermissionException {
			if (roleId == null || roleId.isEmpty()) {
				throw new InconsistentException("BasicGroupAwareEdit#removeRoleAccess - Must specify a role to remove for content " + this.getReference());
			}

			if (isRoleView(this.m_id, roleId)) {
				try {
					setRoleView(this.m_id, roleId, false);
				} catch (AuthzPermissionException e) {
					throw new PermissionException(e.getUser(), e.getFunction(), e.getResource());
				}
				this.m_accessUpdated = true;
				this.m_access = AccessMode.INHERITED;
				this.m_groups.clear();
			}
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEdit#clearRoleAccess()
		 */
		public void clearRoleAccess() throws PermissionException {
			Set<String> roles = getRoleViews(this.m_id);
			for (String role : roles) {
				try {
					setRoleView(this.m_id, role, false);
				} catch (AuthzPermissionException e) {
					throw new PermissionException(e.getUser(), e.getFunction(), e.getResource());
				}
			}
			if (roles.size() > 0) {
				this.m_accessUpdated = true;
			}
			this.m_access = AccessMode.INHERITED;
			this.m_groups.clear();
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getRoleAccessIds() ()
		 */
		public Set<String> getRoleAccessIds()
		{
			return getRoleViews(this.m_id);
		}

		/**
		 * @inheritDoc
		 * @see org.sakaiproject.content.api.GroupAwareEntity#getInheritedRoleAccessIds() ()
		 */
		public Set<String> getInheritedRoleAccessIds()
		{
			Set<String> roleIds = new LinkedHashSet<String>();
			if (isRootCollection(this.m_id)) {
				// we are at the root so there is nothing to inherit
				return roleIds;
			}
			ContentEntity next = this.getContainingCollection();

			while (next != null && next.getAccess() == AccessMode.INHERITED) {
				roleIds.addAll(next.getRoleAccessIds());
				next = next.getContainingCollection();
			}
			return roleIds;
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

			if(!getRoleAccessIds().isEmpty())
			{
				clearRoleAccess();
			}

			SortedSet groupRefs = new TreeSet();
			if(this.getInheritedAccess() == AccessMode.GROUPED)
			{
				this.m_accessUpdated = true;
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

			if(this.m_access != AccessMode.GROUPED || !(newGroups.containsAll(this.m_groups) && this.m_groups.containsAll(newGroups))) {
				this.m_accessUpdated = true;
				
				this.m_access = AccessMode.GROUPED;
				this.m_groups.clear();
				this.m_groups.addAll(newGroups);
			}
			
		}


		/**
		 * Loads a collection of group references. Any items not found aren't 
		 * included in the returned collection;
		 * @param groupRefs The group references to load.
		 * @return The group objects corresponding to the group references. Will
		 * not contain <code>null</code>.
		 */
		private Collection<Group> findGroupObjects(Collection<String> groupRefs)
		{
			Collection<Group> groups = new ArrayList<Group>();
			for (String groupRef: groupRefs)
			{
				Group group = m_siteService.findGroup(groupRef);
				if (group != null)
				{
					groups.add(group);
				}
			}
			return groups;
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
			return findGroupObjects(m_groups);

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
			return findGroupObjects(getInheritedGroups());
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

		public Date getReleaseTime()
		{
			Date date = null;
			if (m_releaseDate != null) {
				date = new Date(m_releaseDate.getTime());
			}
			return date;
		}
		
		public Time getRetractDate()
		{
			return m_retractDate;
		}
		
		public Date getRetractTime()
		{
			Date date = null;
			if (m_retractDate != null) {
				date = new Date(m_retractDate.getTime());
			}
			return date;
		}
		
		@Override
		public Instant getReleaseInstant() {
			Instant instant = null;
			if (m_releaseDate != null) {
				instant =  Instant.ofEpochMilli(m_releaseDate.getTime());
			}
			return instant;
		}

		@Override
		public Instant getRetractInstant() {
			Instant instant = null;
			if (m_retractDate != null) {
				instant = instant.ofEpochMilli(m_retractDate.getTime());
			}
			return instant;
		}

		@Override
		public void setReleaseInstant(Instant date) {
			setReleaseDate(timeService.newTime(date.toEpochMilli()));
			
		}

		@Override
		public void setRetractInstant(Instant time) {
			setRetractDate(timeService.newTime(time.toEpochMilli()));
			
		}


		/**
		 * @return true if a change has been maded in any settings affecting visibility 
		 * for this resource, or false otherwise.
		 */
		public boolean isVisibilityUpdated() {
			return m_visibilityUpdated;
		}

		/**
		 * @return true if a change has been made in any settings affecting whether this 
		 * entity can be accessed publicly, by members of a single site, or by members of 
		 * one or more authz groups, or false otherwise.
		 */
		public boolean isAccessUpdated() {
			return m_accessUpdated;
		}

		private boolean isConditionallyReleased(ContentEntity entity) {
			return StringUtils.equalsIgnoreCase("true", entity.getProperties().getProperty(ConditionService.PROP_CONDITIONAL_RELEASE));
		}

		public boolean isAvailable() 
		{
			boolean available = !this.isHidden();
			boolean isHiddenWebFolder = false;
			ContentEntity currentEntity = null;
			try {
				currentEntity = isCollection(this.m_id)?findCollection(m_id):findResource(m_id);
			} catch (TypeException te) {
				return false;
			}

			while (available && currentEntity != null) {
			
				if(available && (currentEntity.getReleaseDate() != null || currentEntity.getRetractDate() != null || isConditionallyReleased(currentEntity)))
				{
					Time now = timeService.newTime();
					if (currentEntity.getReleaseDate() != null) {
						available = currentEntity.getReleaseDate().before(now);
					}
					if (available && currentEntity.getRetractDate() != null) {
						available = currentEntity.getRetractDate().after(now);
					}
					if (available && isConditionallyReleased(currentEntity)) {
						// first check for global rule satisfaction
						String satisfiesRule = currentEntity.getProperties().getProperty("resource.satisfies.rule");
						if (satisfiesRule == null) {
							Collection<?> acl = (Collection<?>) currentEntity.getProperties().get("conditional_access_list");
							if (acl == null) {
								available = false;
							} else {
								// acl acts as a white list for availability
								available = acl.contains(sessionManager.getCurrentSessionUserId());
							}
						} else {
							available = Boolean.parseBoolean(satisfiesRule);
						}
					}
				}
				if (!available) {
					return available;
				}
				//Only check this case for the actual folder, not the parents
				if (available && !isHiddenWebFolder && this.m_id.equals(currentEntity.getId()) && currentEntity.getId().endsWith(Entity.SEPARATOR)) {
					isHiddenWebFolder = isAttachmentResource(currentEntity.getId()) ||
					    "true".equals(currentEntity.getProperties().getProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT));
				}
				currentEntity = currentEntity.getContainingCollection();
				available = currentEntity!=null?!currentEntity.isHidden():available;
			}
			return (available && isHiddenWebFolder && this.getId().endsWith(Entity.SEPARATOR))?!available:available;
		}

		public boolean isHidden() 
		{
			return this.m_hidden;
		}

		public void setReleaseDate(Time time)
		{
			if(time == null)
			{
				if(m_releaseDate != null) {
					this.m_visibilityUpdated = true;
				}
				m_releaseDate = null;
			}
			else
			{
				if(m_releaseDate == null || m_releaseDate.compareTo(time) != 0) {
					this.m_visibilityUpdated = true;
				}
				m_releaseDate = timeService.newTime(time.getTime());
			}
			m_hidden = false;
		}

		public void setReleaseTime(Date date)
		{
			setReleaseDate(timeService.newTime(date.getTime()));
		}
		
		public void setRetractDate(Time time)
		{
			if(time == null)
			{
				if(m_retractDate != null) {
					this.m_visibilityUpdated = true;
				}
				m_retractDate = null;
			}
			else
			{
				if(m_retractDate == null || m_retractDate.compareTo(time) != 0) {
					this.m_visibilityUpdated = true;
				}
				m_retractDate = timeService.newTime(time.getTime());
			}
			m_hidden = false;
		}

		public void setRetractTime(Date time)
		{
			setRetractDate(timeService.newTime(time.getTime()));
		}
		
		public void setAvailability(boolean hidden, Time releaseDate, Time retractDate) 
		{
			if(m_hidden != hidden) {
				this.m_visibilityUpdated = true;
			} else if(releaseDate == null && m_releaseDate != null) {
				this.m_visibilityUpdated = true;
			} else if(releaseDate != null && m_releaseDate == null) {
				this.m_visibilityUpdated = true;
			} else if(m_releaseDate != null && releaseDate != null && m_releaseDate.compareTo(releaseDate) != 0) {
				this.m_visibilityUpdated = true;
			} else if(retractDate == null && m_retractDate != null) {
				this.m_visibilityUpdated = true;
			} else if(retractDate != null && m_retractDate == null) {
				this.m_visibilityUpdated = true;
			} else if(m_retractDate != null && retractDate != null && m_retractDate.compareTo(retractDate) != 0) {
				this.m_visibilityUpdated = true;
			}
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
					this.m_releaseDate = timeService.newTime(releaseDate.getTime());
				}
				if(retractDate == null)
				{
					this.m_retractDate = null;
				}
				else
				{
					this.m_retractDate = timeService.newTime(retractDate.getTime());
				}
			}

		}

		@Override
		public void setAvailabilityInstant(boolean hidden, Instant releaseDate, Instant retractDate) {
			setAvailability(hidden, timeService.newTime(releaseDate.toEpochMilli()), timeService.newTime(retractDate.toEpochMilli()));	
		}
		
		public void setHidden() 
		{
			if(!m_hidden) {
				this.m_visibilityUpdated = true;
			}
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
						log.error(e.getMessage(), e);
					} 
					catch (TypeException e) 
					{
						log.error(e.getMessage(), e);
					} 
					catch (PermissionException e) 
					{
						log.error(e.getMessage(), e);
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

		public boolean isConditionallyReleased() {
			try {
				return this.m_properties.getBooleanProperty(ConditionService.PROP_CONDITIONAL_RELEASE);
			} catch (EntityPropertyNotDefinedException e) {
				return false;
			} catch (EntityPropertyTypeException e) {
				return false;
			}
		}

		public void setConditionallyReleased(boolean isConditionallyReleased) {
			try {
				Boolean oldValue = this.m_properties.getBooleanProperty(ConditionService.PROP_CONDITIONAL_RELEASE);
				if(oldValue.booleanValue() != isConditionallyReleased) {
					this.m_visibilityUpdated = true;
				}
			} catch (EntityPropertyNotDefinedException e) {
				// oldValue is false
				if(isConditionallyReleased) {
					this.m_visibilityUpdated = true;
				}
			} catch (EntityPropertyTypeException e) {
				// assume oldValue is false
				if(isConditionallyReleased) {
					this.m_visibilityUpdated = true;
				}
			}
			m_properties.addProperty(ConditionService.PROP_CONDITIONAL_RELEASE, Boolean.toString(isConditionallyReleased));
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
							// m_releaseDate = timeService.newTime(0);
							String date0 = attributes.getValue(RELEASE_DATE);
							if (date0 != null && !date0.trim().equals(""))
							{
								m_releaseDate = timeService.newTimeGmt(date0);
								if (m_releaseDate.getTime() <= START_OF_TIME)
								{
									m_releaseDate = null;
								}
							}

							// extract retract date
							// m_retractDate = timeService.newTimeGmt(9999,12,
							// 31, 23, 59, 59, 999);
							String date1 = attributes.getValue(RETRACT_DATE);
							if (date1 != null && !date1.trim().equals(""))
							{
								m_retractDate = timeService.newTimeGmt(date1);
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
							log.warn("Unexpected Element " + qName);
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
					    setPriority();
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
			// m_releaseDate = timeService.newTime(0);
			String date0 = el.getAttribute(RELEASE_DATE);
			if(date0 != null && !date0.trim().equals(""))
			{
				m_releaseDate = timeService.newTimeGmt(date0);
				if(m_releaseDate.getTime() <= START_OF_TIME)
				{
					m_releaseDate = null;
				}
			}

			// extract retract date
			// m_retractDate = timeService.newTimeGmt(9999,12, 31, 23, 59, 59, 999);
			String date1 = el.getAttribute(RETRACT_DATE);
			if(date1 != null && !date1.trim().equals(""))
			{
				m_retractDate = timeService.newTimeGmt(date1);
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
				m_releaseDate = timeService.newTime(other.getReleaseDate().getTime());
			}
			if(m_hidden || other.getRetractDate() == null)
			{
				m_retractDate = null;
			}
			else
			{
				m_retractDate = timeService.newTime(other.getRetractDate().getTime());
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
			return getAccessPoint(relative) + Web.escapeUrl(convertIdToUserEid(m_id));
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
				if(context != null || m_id.startsWith(COLLECTION_DROPBOX))
				{
					size = getSizeForContext(context!=null?context:m_id)/1000L;
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
			// if (log.isDebugEnabled())
			// log.debug("getBodySizeK(): collection: " + getId() + " size: " + size);

			return size;

		} // getBodySizeK

		/**
		 * Access a List of the collections' internal members as full ContentResource or ContentCollection objects.
		 * 
		 * @return a List of the full objects of the members of the collection.
		 */
		public List<ContentEntity> getMemberResources()
		{
			List<ContentEntity> mbrs = (List<ContentEntity>) threadLocalManager.get("members@" + this.m_id);
			if(mbrs == null)
			{
				mbrs = new ArrayList();

				// TODO: current service caching
				mbrs.addAll(m_storage.getCollections(this));
				mbrs.addAll(m_storage.getResources(this));

				threadLocalManager.set("members@" + this.m_id, mbrs);
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
					threadLocalManager.set("findResource@" + entity.getId(), entity);	// new BaseResourceEdit((ContentResource) entity));
				}
				else if(entity instanceof ContentCollection)
				{
					copy = new BaseCollectionEdit((ContentCollection) entity);
					threadLocalManager.set("findCollection@" + entity.getId(), entity); 	// new BaseCollectionEdit((ContentCollection) entity));
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
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

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

		public void setPriorityMap(Map<String, Integer> priorities) 
		{
			if(m_prioritySortEnabled)
			{
				ResourcePropertiesEdit myProps = getPropertiesEdit();
				myProps.addProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT, Boolean.TRUE.toString());
				Iterator nameIt = priorities.entrySet().iterator();
				while(nameIt.hasNext())
				{
					Entry entry = (Entry) nameIt.next();
					String name = (String) entry.getKey();
					Integer priority = (Integer) entry.getValue();
					
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
							threadLocalManager.set("findCollection@" + entity.getId(), null);
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
							threadLocalManager.set("findResource@" + entity.getId(), null);

							// close the edit object
							((BaseResourceEdit) entity).closeEdit();
						}
					}
					catch(TypeException e)
					{
						// TODO Auto-generated catch block
						log.error("TypeException",e);
					} 
					catch (IdUnusedException e) 
					{
						// TODO Auto-generated catch block
						log.error("IdUnusedException",e);
					} 
					catch (PermissionException e) 
					{
						// TODO Auto-generated catch block
						log.error("PermissionException",e);
					} 
					catch (InUseException e) 
					{
						// TODO Auto-generated catch block
						log.error("InUseException",e);
					} 
					catch (ServerOverloadException e) 
					{
						// TODO Auto-generated catch block
						log.error("ServerOverloadException",e);
					}
				}

			}
		}

		public int getMemberCount() 
		{
			int count = 0;
			Integer countObj = (Integer) threadLocalManager.get("getMemberCount@" + this.m_id);
			if(countObj == null)
			{
				count = m_storage.getMemberCount(this.m_id);
				threadLocalManager.set("getMemberCount@" + this.m_id, Integer.valueOf(count));
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
					log.error("Type Exception ",e);
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
				//log.warn("Edit Object not closed correctly, Cancelling "+this.getId());
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
		protected long m_contentLength = 0;

		/** When true, someone changed the body content with setContent() */
		protected boolean m_bodyUpdated = false;

		/** The file system path, post root, for file system stored body binary. */
		protected String m_filePath = null;

		protected InputStream m_contentStream;

		private boolean m_sessionBound = true;

		protected String m_oldDisplayName = null;

		/**
		 * Indicates this resource is a reference copy of an existing resource,
		 * this id will be the resource it is a copy of
		 */
		protected String referenceCopy = null;
        /**
         * Indicates this resource is a reference copy of an existing resource,
         * this id will be the resource it is a copy of
         * WARNING: this will null out the content values (stream and body)
		 * 
		 * @param referenceCopy the id of the resource this is a copy of
		 */
        public void setReferenceCopy(String referenceCopy) {
            this.referenceCopy = referenceCopy;
            this.m_contentStream = null;
            this.m_body = null;
        }

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
				setFilePath(timeService.newTime());
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

		public BaseResourceEdit(ContentResource other, boolean reference) {
		    set(other, reference);
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

			m_filePath = volume + time.toStringFilePath() + idManager.createUuid();
		}

		/**
		 * Take all values from this object
		 * 
		 * @param other
		 *        The other object to take values from.
		 */
		protected void set(ContentResource other) {
		    set(other, false);
		}

		/**
		 * Set the values in this edit to equal the values in the passing object
		 * 
		 * @param other the object to take values from.
		 * @param reference if true then make a reference copy (i.e. do not duplicate the actual 
		 */
        protected void set(ContentResource other, boolean reference) {
			m_id = other.getId();
			m_contentType = other.getContentType();
			m_contentLength = other.getContentLength();
			m_resourceType = other.getResourceType();
			chh = other.getContentHandler();
			chh_vce = other.getVirtualContentEntity();

			if (reference && other.getId() != null) {
			    // populate the reference copy key, skip populating the actual content
			    this.referenceCopy = other.getId();
			    this.m_contentStream = null;
			    this.m_body = null;
			} else {
                // copy the actual content
			    this.referenceCopy = null;
                this.m_contentStream = ((BaseResourceEdit) other).m_contentStream;

                // if there's a body in the other, reference it, else leave this one null
                // Note: this treats the body byte array as immutable, so to update it one
                // *must* call setContent() not just getContent and mess with the bytes. -ggolden
                byte[] content = ((BaseResourceEdit) other).m_body;
                if (content != null)
                {
                    m_contentLength = content.length;
                    m_body = content;
                }
			}

			m_filePath = ((BaseResourceEdit) other).m_filePath;

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
				m_releaseDate = timeService.newTime(other.getReleaseDate().getTime());
			}
			if(m_hidden || other.getRetractDate() == null)
			{
				m_retractDate = null;
			}
			else
			{
				m_retractDate = timeService.newTime(other.getRetractDate().getTime());
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
		 * Construct from information in XML in a DOM element. Limited to body size of <= 2G.
		 * 
		 * @param el
		 *        The XML DOM element.
		 */
		public BaseResourceEdit(Element el)
		{
			m_properties = new BaseResourcePropertiesEdit();

			m_id = el.getAttribute("id");
			String contentType = StringUtils.trimToNull(el.getAttribute("content-type"));
			setContentType(contentType);
			m_contentLength = 0;
			try
			{
				m_contentLength = Long.parseLong(el.getAttribute("content-length"));
			}
			catch (Exception ignore)
			{
			}
			ResourceTypeRegistry registry = getResourceTypeRegistry();
			String typeId = StringUtils.trimToNull(el.getAttribute("resource-type"));
			if(typeId == null || registry.getType(typeId) == null)
			{
				typeId = registry.mimetype2resourcetype(contentType);
			}
			setResourceType(typeId);

			if (m_contentLength <= Integer.MAX_VALUE)
			{
				String enc = StringUtils.trimToNull(el.getAttribute("body"));
				if (enc != null)
				{
					byte[] decoded = null;
					try
					{
						decoded = Base64.decodeBase64(enc.getBytes("UTF-8"));
					}
					catch (UnsupportedEncodingException e)
					{
						log.error(e.getMessage(), e);
					}
					
					m_body = new byte[(int) m_contentLength];
					System.arraycopy(decoded, 0, m_body, 0, (int) m_contentLength);
				}
			}

			m_filePath = StringUtils.trimToNull(el.getAttribute("filePath"));

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
					    setPriority();
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
						m_releaseDate = timeService.newTimeGmt(date0);
						if(m_releaseDate.getTime() <= START_OF_TIME)
						{
							m_releaseDate = null;
						}
					}

					// extract retract date
					String date1 = el.getAttribute(RETRACT_DATE);
					if(date1 != null && !date1.trim().equals(""))
					{
						m_retractDate = timeService.newTimeGmt(date1);
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
							String contentType = StringUtils.trimToNull(attributes
									.getValue("content-type"));
							setContentType(contentType);
							m_contentLength = 0;
							try
							{
								m_contentLength = Long.parseLong(attributes
										.getValue("content-length"));
							}
							catch (Exception ignore)
							{
							}
							ResourceTypeRegistry registry = getResourceTypeRegistry();
							String typeId = StringUtils.trimToNull(attributes
									.getValue("resource-type"));
							if (typeId == null || registry.getType(typeId) == null)
							{
								typeId = registry.mimetype2resourcetype(contentType);
							}
							setResourceType(typeId);

							if (m_contentLength <= Integer.MAX_VALUE)
							{
								String enc = StringUtils.trimToNull(attributes
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
										log.error(e.getMessage(), e);
									}
									m_body = new byte[(int) m_contentLength];
									System.arraycopy(decoded, 0, m_body, 0, (int) m_contentLength);
								}
							}

							m_filePath = StringUtils.trimToNull(attributes
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
									m_releaseDate = timeService.newTimeGmt(date0);
									if (m_releaseDate.getTime() <= START_OF_TIME)
									{
										m_releaseDate = null;
									}
								}

								// extract retract date
								String date1 = attributes.getValue(RETRACT_DATE);
								if (date1 != null && !date1.trim().equals(""))
								{
									m_retractDate = timeService.newTimeGmt(date1);
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
							log.warn("Unexpected Element " + qName);
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
			+ Web.escapeUrl(getAlternateReferenceRoot(rootProperty) + m_relativeAccessPoint
			+ convertIdToUserEid(m_id));
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
			String alternateRoot = StringUtils.trimToNull(getProperties().getProperty(PROP_ALTERNATE_REFERENCE));
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
			// check the copyright alert setting
			// return true only if the copyright alert property is set and value is true
			try
			{
				return m_properties.getBooleanProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
			}
			catch (Exception e)
			{
				// if there is no such copyright alert property, return false
				return false;
			}
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
		public long getContentLength()
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
		 * Access the content bytes of the resource. As this reads the entire content into memory, only use this method
		 * when the resource is known to be relatively small. For larger files and all files that exceed 2G in size, use
		 * streamContent() instead.
		 * 
		 * @return An array containing the bytes of the resource's content.
		 * @exception ServerOverloadException
		 *            if server is configured to store resource body in filesystem and error occurs trying to read from filesystem,
		 *            or the file is too large to read into a byte array (exceeds 2G in size).
		 */
		public byte[] getContent() throws ServerOverloadException
		{
			// Use the CHH delegate, if there is one.
			if (chh_vce != null) return ((ContentResource)chh_vce).getContent();

			// return the body bytes
			byte[] rv = m_body;

			if (rv == null)
			{
				// todo: try to get the body from the stream
				if (m_contentLength == 0)
				{
					rv = new byte[0];
				} 
				else if (m_contentLength > 0)
				{
					// TODO: we do not store the body with the object, so as not to cache the body bytes -ggolden
					rv = m_storage.getResourceBody(this);
					// m_body = rv;
				}
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
		public void setContentLength(long length)
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
				log.warn("setContent(): null content");
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
				log.warn("setContent(): null stream");
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
			long contentLength = m_contentLength;
			if (m_body != null) contentLength = m_body.length;
			resource.setAttribute("content-length", Long.toString(contentLength));

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
					log.error(e.getMessage(), e);
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
			if (log.isDebugEnabled()) log.debug("valueUnbound()");

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
				log.warn("Serializing Body to Entiry Blob, this is bad and will make Sakai crawl");
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
				log.warn("Body serialization from Entity, this is bad and will slow Sakai right down ");
			}
			m_body = body;			
		}




		/* (non-Javadoc)
		 * @see org.sakaiproject.content.impl.serialize.api.SerializableResourceAccess#setSerializableContentLength(long)
		 */
		public void setSerializableContentLength(long contentLength)
		{
			if (m_bodyPath == null && contentLength > Integer.MAX_VALUE ) {
				log.warn("File is longer than "+Integer.MAX_VALUE+", may be truncated if not stored in filesystem ");
			}
			m_contentLength = contentLength;
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
				//log.warn("Edit Object not closed correctly, Cancelling "+this.getId());
				cancelResource(this);
			}			
		}

		/**
		 * @return the oldDisplayName
		 */
		public String getOldDisplayName() {
			return m_oldDisplayName;
		}

		/**
		 * @param oldDisplayName the oldDisplayName to set
		 */
		public void setOldDisplayName(String oldDisplayName) {
			this.m_oldDisplayName = oldDisplayName;
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
		 * Get a direct link to the asset so it doesn't have to be streamed.
		 * @param resource
		 * @return URI or null if no direct link is available
		 */
		public URI getDirectLink(ContentResource resource);

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
		public List<ContentCollectionEdit> getCollections(ContentCollection collection);

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
		public List<ContentResourceEdit> getResources(ContentCollection collection);

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
		 * Forget about a resource (and associated content).
		 * 
		 * @param resource the resource to remove
		 */
		public void removeResource(ContentResourceEdit resource);

		/**
         * Forget about a resource with the option to leave the content in place
		 * 
         * @param resource the resource to remove
		 * @param removeContent if true, then also remove the content
		 */
		public void removeResource(ContentResourceEdit resource, boolean removeContent);

		/**
		 * Read the resource's body.
		 * 
		 * @exception ServerOverloadException
		 *            if server is configured to save resource body in filesystem and an error occurs while trying to access the filesystem.
		 */
		public byte[] getResourceBody(ContentResource resource) throws ServerOverloadException;

		/*
		 * Stream the resource's body for deleted resource.
		 * 
		 * @exception ServerOverloadException
		 *            if server is configured to save resource body in filesystem and an error occurs while trying to access the filesystem.
		 */
		public InputStream streamDeletedResourceBody(ContentResource resource) throws ServerOverloadException;

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
		 * @throws ServerOverloadException 

		 */
		//public char getAccessMode(String id);

		// htripath-storing into shadow table before deleting the resource
		public void commitDeletedResource(ContentResourceEdit edit, String uuid) throws ServerOverloadException;

		public ContentResourceEdit putDeleteResource(String resourceId, String uuid, String userId);

		public List getDeletedResources(ContentCollection collection);      
		public ContentResourceEdit editDeletedResource(String resourceId);      
		public void removeDeletedResource(ContentResourceEdit edit); 
		public void cancelDeletedResource(ContentResourceEdit edit);
		
		/**
		 * Retrieve a collection of ContentResource objects pf a particular resource-type.  The collection will 
		 * contain no more than the number of items specified as the pageSize, where pageSize is a non-negative 
		 * number less than or equal to 1028. The resources will be selected in ascending order by resource-id.
		 * If the resources of the specified resource-type in the ContentHostingService in ascending order by 
		 * resource-id are indexed from 0 to M and this method is called with parameters of N for pageSize and 
		 * I for page, the resources returned will be those with indexes (I*N) through ((I+1)*N - 1).  For example,
		 * if pageSize is 1028 and page is 0, the resources would be those with indexes of 0 to 1027.  
		 *
		 * @param resourceType select resources where CONTENT_RESOURCE.RESOURCE_TYPE_ID equals resourceType
		 * @param pageSize (page) size of results
		 * @param page (page) increment of results
		 * @return collection of ContentResource
		 */
		public Collection<ContentResource> getResourcesOfType(String resourceType, int pageSize, int page);
      
		/**
		 * Retrieve a collection of ContentResource objects of a particular resource-type in a set of contexts.
		 *
		 * @param resourceType select resources where CONTENT_RESOURCE.RESOURCE_TYPE_ID equals resourceType
		 * @param contextIds	 select resources where CONTENT_RESOURCE.CONTEXT in [context,...]
		 * @return collection of ContentResource
		 */
		public Collection<ContentResource> getContextResourcesOfType(String resourceType, Set<String> contextIds);
		
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

		if (log.isDebugEnabled()) log.debug("refresh(): key " + key + " id : " + ref.getId());

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
				log.error("Type Exception",e);
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
		transferCopyEntitiesRefMigrator(fromContext, toContext, ids, cleanup);
	}

	public Map<String,String> transferCopyEntitiesRefMigrator(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		Map transversalMap = new HashMap();
		try
		{
			if(cleanup == true)
			{
				// Get the root collection
				ContentCollection oCollection = getCollection(toContext);

				if (!isSiteLevelCollection(oCollection.getId())) {
					throw new IllegalArgumentException("transferCopyEntitiesRefMigrator operation rejected on non site collection: " + oCollection.getId());
				}

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
							log.debug("Get Folder Collection" + e);
						}

						if (isCollection)
						{
							try
							{
								this.removeCollection(oId);
							}
							catch (Exception ee)
							{
								log.debug("remove folders resources" + ee);
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
								log.debug("remove others resources" + ee);
							}
						}

					}


				}
			}
		}
		catch (Exception e)
		{
			log.debug("BaseContentService Resources transferCopyEntities Error" + e);
		}
		transversalMap.putAll(transferCopyEntitiesRefMigrator(fromContext, toContext, ids));
		
		return transversalMap;
	}

	// Code lightly adapted from Apache Tomcat 5.5.27 catalina default servlet
	
	/**
	 * Range inner class. From Apache Tomcat DefaultServlet.java 
	 *
	 */
    protected class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ( (start >= 0) && (end >= 0) && (start <= end)
                     && (length > 0) );
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList<Range> parseRange(HttpServletRequest request,
                                HttpServletResponse response,
                                long fileLength)
        throws IOException {

    	/* Commented out pending implementation of last-modified / if-modified.
    	 * See http://jira.sakaiproject.org/jira/browse/SAK-3916
    	
        // Checking If-Range

    	String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (Exception e) {
                ;
            }

            String eTag = getETag(resourceAttributes);
            long lastModified = resourceAttributes.getLastModified();

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim()))
                    return FULL;

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000))
                    return FULL;

            }

        }
        
    	*/
    	
        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null)
            return null;
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList result = new ArrayList();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                        (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                            (rangeDefinition.substring
                             (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

    /**
     * Copy the partial contents of the specified input stream to the specified
     * output stream.
     * 
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream,
                                  OutputStream ostream,
                                  long start, long end) {

    	try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[STREAM_BUFFER_SIZE];
        int len = buffer.length;
        while ( (bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;
    }

  
    /**
     * Copy the contents of the specified input stream to the specified
     * output stream in a set of chunks as per the specified ranges.
     *
     * @param InputStream The input stream to read from
     * @param out The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/output error occurs
     */
    protected void copyRanges(ContentResource content, OutputStream out,
                      Iterator ranges, String contentType)
        throws IOException {

        IOException exception = null;
                        
        while ( (exception == null) && (ranges.hasNext()) ) {

            Range currentRange = (Range) ranges.next();
                  
            // Writing MIME header.
            IOUtils.write("\r\n--" + MIME_SEPARATOR + "\r\n", out);
            if (contentType != null)
                IOUtils.write("Content-Type: " + contentType + "\r\n", out);
            IOUtils.write("Content-Range: bytes " + currentRange.start
                           + "-" + currentRange.end + "/"
                           + currentRange.length + "\r\n", out);
            IOUtils.write("\r\n", out);

            // Printing content
			InputStream in = null;
			try {
				in = content.streamContent();
			} catch (ServerOverloadException se) {
				exception = new IOException("ServerOverloadException reported getting inputstream");
				throw exception;
			}
			
            InputStream istream =
                new BufferedInputStream(in, STREAM_BUFFER_SIZE);
          
            exception = copyRange(istream, out, currentRange.start, currentRange.end);

            try {
                istream.close();
            } catch (IOException e) {
            	// ignore
            }
        }

        IOUtils.write("\r\n--" + MIME_SEPARATOR + "--\r\n", out);
        
        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur with no need for additional security permissions.
	 */
	protected void enableAzgSecurityAdvisor()
	{
		// put in a security advisor so we can do our azg work without need of further permissions
		// TODO: could make this more specific to the AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP permission -ggolden
		m_securityService.pushAdvisor(ALLOW_ADVISOR);
	}

    	/**
	 * Disabled the security advisor.
	 */
	protected void disableAzgSecurityAdvisor()
	{
        SecurityAdvisor popped = m_securityService.popAdvisor(ALLOW_ADVISOR);
		if (!ALLOW_ADVISOR.equals(popped)) {
			if (popped == null)
			{
				log.warn("Someone has removed our advisor.");
			}
			else
			{
				log.warn("Removed someone elses advisor, adding it back.");
				m_securityService.pushAdvisor(popped);
			}
		}
	}
    
    /**
     * Expand the supplied resource under its parent collection.
     * If the zip is bigger than the max zip size specified in properties extraction will
     * NOT occur. See KNL-273 and KNL-900.
     *
     * @param resourceId The zip file resource that we want to expand
     * @exception Exception Anything thrown by ZipContentUtil gets passed upwards.
     */
    public void expandZippedResource(String resourceId) throws Exception {
        int maxZipExtractSize = ZipContentUtil.getMaxZipExtractFiles();
        ZipContentUtil extractZipArchive = new ZipContentUtil();

        // KNL-900 Total size of files should be checked before unzipping (KNL-273)
		Map<String, Long> zipManifest = extractZipArchive.getZipManifest(resourceId);
        if (zipManifest == null) {
            log.error("Zip file for resource ("+resourceId+") has no zip manifest, cannot extract");
        } else if (zipManifest.size() >= maxZipExtractSize) {
            log.warn("Zip file for resource ("+resourceId+") is too large to be expanded, size("+zipManifest.size()+") exceeds the max=("+maxZipExtractSize+") as specified in setting content.zip.expand.maxfiles");
        } else {
            // zip is not too large to extract so check if files are too large
            long totalSize = 0;
            for (Long entrySize : zipManifest.values()) {
                totalSize += entrySize;
            }

            // Get a context
            ContentResourceEdit resource = editResource(resourceId);
            // Set the updated length for quota checking
            resource.setContentLength(totalSize);
            if (log.isDebugEnabled()) log.debug(String.format("Resource is: [%s] Size is [%d]",resourceId, totalSize));
            // check for over quota.
            if (overQuota(resource)) {
                log.error("Zip file for resource ("+resourceId+") would be too large after unzip so it cannot be expanded, totalSize("+totalSize+") exceeds the resource quota");
                throw new OverQuotaException(resource.getReference());
            }
            // zip files are not too large to extract so do the extract
            extractZipArchive.extractArchive(resourceId);
            cancelResource(resource); //commitResource(resource); // KNL-1220
        }
    }
    
    
	private static final String MACRO_USER_ID             = "${USER_ID}";
	private static final String MACRO_USER_EID            = "${USER_EID}";
	private static final String MACRO_USER_FIRST_NAME     = "${USER_FIRST_NAME}";
	private static final String MACRO_USER_LAST_NAME      = "${USER_LAST_NAME}";

	private static final String MACRO_DEFAULT_ALLOWED = "${USER_ID},${USER_EID},${USER_FIRST_NAME},${USER_LAST_NAME}";

	/**
     * Expands a URL that may contain a set of predefined macros, into the full URL. 
     * This should only ever happen when its about to be redirected to, ie never stored and never displayed
     * so that people dont accidentally send an expanded URL containing personally identifying information to someone else, for example.
     * @param url original url that may contain macros
     * @return url with macros expanded
     * 
     * Note that much of this is from the web content tool though site related properties have been removed. This is actually called from /access/ which has no site context so
     * any lookups of site_id or user role (which infers a site) will not work. The site_id may be able to be passed in, as the original resource does have context,
     * however that needs to be more fully explored for security reasons and as such, has not been included.
     * 
     * See SAK-23587
     */
    public String expandMacros(String url) {
    	
    	if(log.isDebugEnabled()){
    		log.debug("Original url: " + url);
    	}
    	
    	if (!StringUtils.contains(url, "${")) {
			return url;
		}
    	
    	//handled explicitly like this for backwards compatibility since comma separated strings from SCS are not supported in all versions of Sakai yet.
    	String allowedMacros = m_serverConfigurationService.getString("content.allowed.macros", MACRO_DEFAULT_ALLOWED);
    	List<String> macros = new ArrayList<String>();
    	if(StringUtils.isNotBlank(allowedMacros)) {
    		macros = Arrays.asList(StringUtils.split(allowedMacros, ','));
    	}
    	
    	for(String macro: macros) {
    		url = StringUtils.replace(url, macro, getMacroValue(macro));
    	}
    	
    	if(log.isDebugEnabled()){
    		log.debug("Expanded url: " + url);
    	}
    	
    	return url;
    }
    
    /**
     * Helper to get the value for a given macro.
     * @param macroName
     * @return
     */
    private String getMacroValue(String macroName) {
		try {
			if (macroName.equals(MACRO_USER_ID)) {
				return userDirectoryService.getCurrentUser().getId();
			}
			if (macroName.equals(MACRO_USER_EID)) {
				return userDirectoryService.getCurrentUser().getEid();
			}
			if (macroName.equals(MACRO_USER_FIRST_NAME)) {
				return userDirectoryService.getCurrentUser().getFirstName();
			}
			if (macroName.equals(MACRO_USER_LAST_NAME)) {
				return userDirectoryService.getCurrentUser().getLastName();
			}
		}
		catch (Exception e) {
			log.error("Error resolving macro:" + macroName + ": " + e.getClass() + ": " + e.getCause());
			return "";
		}
		
		//unsupported, use macro name as is.
		return macroName;
	}

		/*
		*  Return a direct link to the asset so we can bypass streaming the asset in the JVM
		*/
		public URI getDirectLinkToAsset(ContentResource resource) {
			return m_storage.getDirectLink(resource);
		}

    /**
     * Implementation of HardDeleteAware to allow content to be fully purged
     */
    public void hardDelete(String siteId) {
    	
		/* Needs to cater for both db and filesystem storage, and there are a couple of situations to be handled
		 * 1. FS storage. File content is actually deleted so we can just issue a delete on the files and we are done.
		 * 2. FS storage + restore function enabled. File is deleted as per 1 however a copy of file is retained in bodyPathDeleted location
		 * 3. DB storage. Binary is (meant to be) deleted. Backup binary is created.
		 * 
		 * Therefore we need to delete the files (1 handled), then get any backed up files and delete them also (2 and 3 handled). Then delete the collection to finalise things.
		 */

		if (m_siteService.isSpecialSite(siteId)) {
			log.error("hardDelete rejected special site: {}", siteId);
			return;
		}
		// Get collection for the site and check validity
		String collectionId = getSiteCollection(siteId);
		if (!isSiteLevelCollection(collectionId)) {
			log.error("hardDelete rejected on non site collection: {}", collectionId);
			return;
		}
		log.info("hardDelete proceeding on collectionId: {}", collectionId);

		//handle 1
		try {
			List<ContentResource> resources = getAllResources(collectionId);
	    	for(ContentResource resource: resources) {
				log.debug("Removing resource: " + resource.getId());
	    		removeResource(resource.getId());
	    	}
		} catch (Exception e) {
			log.warn("Failed to remove content.", e);
		}

    	//handle2
		//only for 2.10 - comment this out for 2.9 and below
		try {
	    	List<ContentResource> deletedResources = getAllDeletedResources(collectionId);
	    	for(ContentResource deletedResource: deletedResources) {
				log.debug("Removing deleted resource: " + deletedResource.getId());
	    		removeDeletedResource(deletedResource.getId());
	    	}
		} catch (Exception e) {
			log.warn("Failed to remove some content.", e);
		}
		
		//cleanup
		try {
			log.debug("Removing collection: " + collectionId);
			removeCollection(collectionId);
		} catch (Exception e) {
			log.warn("Failed to remove collection {}.", collectionId, e);
		}
    }


	private String getDisplayName(User userIn) {
		User user = (userIn== null)?userDirectoryService.getCurrentUser():userIn ;
		String displayId = user.getDisplayId();
		if (displayId != null && displayId.length() > 0) {
			return user.getSortName() + " (" + displayId + ")";
		}
		else {
			return user.getSortName();
		}
	}

} // BaseContentService
