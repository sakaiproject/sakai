/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.site.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseSite is a base implementation of the Site API Site.
 * </p>
 */
public class BaseSite implements Site
{
	/** Our log (commons). */
	private static Logger M_log = LoggerFactory.getLogger(BaseSite.class);

	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The event code for this edit. */
	protected String m_event = null;

	/** Active flag. */
	protected boolean m_active = false;

	/** List of groups deleted in this edit pass. */
	protected Collection m_deletedGroups = new Vector();

	/** The site id. */
	protected String m_id = null;

	/** The site title. */
	protected String m_title = null;

	/** The site short description. */
	protected String m_shortDescription = null;

	/** The HTML-safe version of the short description */
	protected String m_htmlShortDescription = null;

	/** The site description. */
	protected String m_description = null;

	/** The HTML-safe version of the description */
	protected String m_htmlDescription = null;

	/** Track whether this description has been loaded. */
	protected boolean m_descriptionLoaded = false;
	
	/** Track whether this site has been fully loaded. */
	protected boolean m_fullyLoaded = false;

	/** The name of the role given to users who join a joinable site. */
	protected String m_joinerRole = null;

	/** Is this site joinable. */
	protected boolean m_joinable = false;

	/** Published or not. */
	protected boolean m_published = false;

	/** The icon url. */
	protected String m_icon = null;

	/** The site info url. */
	protected String m_info = null;

	/** The properties. */
	protected ResourcePropertiesEdit m_properties = null;

	/** The list of site pages for this site. */
	protected ResourceVector m_pages = null;

	/** Set true while the pages have not yet been read in for a site. */
	protected boolean m_pagesLazy = false;

	/** The skin to use for this site. */
	protected String m_skin = null;

	/** The pubView flag. */
	protected boolean m_pubView = false;

	/** The site type. */
	protected String m_type = null;

	/** The created user id. */
	protected String m_createdUserId = null;

	/** The last modified user id. */
	protected String m_lastModifiedUserId = null;

	/** The time created. */
	protected Time m_createdTime = null;

	/** The time last modified. */
	protected Time m_lastModifiedTime = null;

	/** The list of site groups for this site. */
	protected ResourceVector m_groups = null;

	/** Set true while the groups have not yet been read in for a site. */
	protected boolean m_groupsLazy = false;

	/** The azg from the AuthzGroupService that is my AuthzGroup impl. */
	protected AuthzGroup m_azg = null;

	private AuthzGroupService authzGroupService;

	/**
	 * Set to true if we have changed our azg, so it need to be written back on
	 * save.
	 */
	protected boolean m_azgChanged = false;

	/**
	 * Set to true to use the site's page order, or false to let a toolOrder
	 * override the page order.
	 */
	protected boolean m_customPageOrdered = false;

	private BaseSiteService siteService;

	private SessionManager sessionManager;

	private UserDirectoryService userDirectoryService;

	/** Softly deleted data */
	protected boolean m_isSoftlyDeleted = false;
	protected Date m_softlyDeletedDate = null;

	/** flag to use Dynamic tool categorization instead of toolorder.xml */
	public static final String DYNAMIC_TOOL_CATEGORIZATION = "portal.toolcategories.dynamic";

	/**
	 * Construct.
	 * 
	 * @param id
	 *        The site id.
	 */
	public BaseSite(BaseSiteService siteService, String id)
	{
		setupServices(siteService, sessionManager, userDirectoryService);

		m_id = id;

		// setup for properties
		m_properties = new BaseResourcePropertiesEdit();

		// set up the page list
		m_pages = new ResourceVector();

		// set up the groups collection
		m_groups = new ResourceVector();

		// if the id is not null (a new site, rather than a reconstruction)
		// add the automatic (live) properties
		if (m_id != null) {
			siteService.addLiveProperties(this);
		}

	}

	/**
	 * Construct from another Site, exact.
	 * 
	 * @param site
	 *        The other site to copy values from.
	 * @param exact
	 *        If true, we copy ids - else we generate new ones for site, page
	 *        and tools.
	 */
	public BaseSite(BaseSiteService siteService, Site other)
	{
		this(siteService, other, true);
	}

	/**
	 * Construct from another Site.
	 * 
	 * @param site
	 *        The other site to copy values from.
	 * @param exact
	 *        If true, we copy ids - else we generate new ones for site, page
	 *        and tools.
	 */
	public BaseSite(BaseSiteService siteService, Site other, boolean exact)
	{
		setupServices(siteService, sessionManager, userDirectoryService);
		BaseSite bOther = (BaseSite) other;
		set(bOther, exact);
	}

	/**
	 * Construct from an existing definition, in xml.
	 * 
	 * @param el
	 *        The message in XML in a DOM element.
	 */
	public BaseSite(BaseSiteService siteService, Element el, TimeService timeService)
	{
		setupServices(siteService, sessionManager, userDirectoryService);

		// setup for properties
		m_properties = new BaseResourcePropertiesEdit();

		// setup for page list
		m_pages = new ResourceVector();

		// setup for the groups list
		m_groups = new ResourceVector();

		m_id = el.getAttribute("id");
		m_title = StringUtils.trimToNull(el.getAttribute("title"));

		// description might be encripted
		String tmpDesc = StringUtils.trimToNull(el.getAttribute("description"));
		if (tmpDesc == null)
		{
			tmpDesc = StringUtils.trimToNull(Xml.decodeAttribute(el,
					"description-enc"));
		}
		setDescription(tmpDesc);
		m_descriptionLoaded = true;

		// short description might be encripted
		String tmpShortDesc = StringUtils.trimToNull(el.getAttribute("short-description"));
		if (tmpShortDesc == null)
		{
			tmpShortDesc = StringUtils.trimToNull(Xml.decodeAttribute(el,
					"short-description-enc"));
		}
		setShortDescription(tmpShortDesc);

		m_joinable = Boolean.valueOf(el.getAttribute("joinable")).booleanValue();
		m_joinerRole = StringUtils.trimToNull(el.getAttribute("joiner-role"));

		String published = StringUtils.trimToNull(el.getAttribute("published"));
		if (published == null)
		{
			// read the old "status" (this file 1.42 and before) 1-un 2-pub
			published = StringUtils.trimToNull(el.getAttribute("status"));
			if (published != null)
			{
				published = Boolean.valueOf("2".equals(published)).toString();
			}
		}

		m_published = Boolean.valueOf(published).booleanValue();

		m_icon = StringUtils.trimToNull(el.getAttribute("icon"));
		m_info = StringUtils.trimToNull(el.getAttribute("info"));
		m_skin = StringUtils.trimToNull(el.getAttribute("skin"));

		m_createdUserId = StringUtils.trimToNull(el.getAttribute("created-id"));
		m_lastModifiedUserId = StringUtils.trimToNull(el.getAttribute("modified-id"));

		String time = StringUtils.trimToNull(el.getAttribute("created-time"));
		if (time != null)
		{
			m_createdTime = timeService.newTimeGmt(time);
		}

		time = StringUtils.trimToNull(el.getAttribute("modified-time"));
		if (time != null)
		{
			m_lastModifiedTime = timeService.newTimeGmt(time);
		}

		String customOrder = StringUtils.trimToNull(el.getAttribute("customPageOrdered"));
		if (customOrder == null)
		{
			m_customPageOrdered = false;
		}
		else
		{
			m_customPageOrdered = Boolean.valueOf(customOrder).booleanValue();
		}

		// get pubView setting - but old versions (pre 1.42 of this file) won't
		// have it and will have a property instead
		String pubViewValue = StringUtils.trimToNull(el.getAttribute("pubView"));

		// get the type - but old versions (pre 1.42 of this file) won't have it
		// and will have a property instead
		String typeValue = StringUtils.trimToNull(el.getAttribute("type"));

		// the children (properties and page list)
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("properties"))
			{
				// re-create properties
				m_properties = new BaseResourcePropertiesEdit(element);

				// look for pubview (pre 1.42 of this file) in properties
				if (pubViewValue == null)
				{
					pubViewValue = m_properties.getProperty("CTNG:site-include");
					if (pubViewValue == null)
					{
						pubViewValue = m_properties.getProperty("site-include");
					}
				}
				m_properties.removeProperty("CTNG:site-include");
				m_properties.removeProperty("site-include");

				// look for type (pre 1.42 of this file) in properties (two
				// possibilities)
				if (typeValue == null)
				{
					typeValue = m_properties.getProperty("SAKAI:site-type");
					if (typeValue == null)
					{
						typeValue = m_properties.getProperty("CTNG:site-type");
					}
				}
				m_properties.removeProperty("SAKAI:site-type");
				m_properties.removeProperty("CTNG:site-type");

				// look for short description (pre 1.42 of this file) in
				// properties
				if (m_shortDescription == null)
				{
					m_shortDescription = m_properties
							.getProperty("CTNG:short-description");

					if (m_shortDescription == null)
					{
						m_shortDescription = m_properties
								.getProperty("short-description");
					}
				}
				m_properties.removeProperty("CTNG:short-description");
				m_properties.removeProperty("short-description");

				// pull out some properties into fields to convert old (pre
				// 1.42) versions
				if (m_createdUserId == null)
				{
					m_createdUserId = m_properties.getProperty("CHEF:creator");
				}
				if (m_lastModifiedUserId == null)
				{
					m_lastModifiedUserId = m_properties.getProperty("CHEF:modifiedby");
				}
				if (m_createdTime == null)
				{
					try
					{
						m_createdTime = m_properties.getTimeProperty("DAV:creationdate");
					}
					catch (Exception ignore)
					{
					}
				}
				if (m_lastModifiedTime == null)
				{
					try
					{
						m_lastModifiedTime = m_properties
								.getTimeProperty("DAV:getlastmodified");
					}
					catch (Exception ignore)
					{
					}
				}
				m_properties.removeProperty("CHEF:creator");
				m_properties.removeProperty("CHEF:modifiedby");
				m_properties.removeProperty("DAV:creationdate");
				m_properties.removeProperty("DAV:getlastmodified");
			}

			// look for the page list
			else if (element.getTagName().equals("pages"))
			{
				NodeList pagesNodes = element.getChildNodes();
				for (int p = 0; p < pagesNodes.getLength(); p++)
				{
					Node pageNode = pagesNodes.item(p);
					if (pageNode.getNodeType() != Node.ELEMENT_NODE) continue;
					Element pageEl = (Element) pageNode;
					if (!pageEl.getTagName().equals("page")) continue;

					BaseSitePage page = new BaseSitePage(siteService,pageEl, this);
					m_pages.add(page);
				}

				// TODO: else if ( "groups")
			}
		}

		// set the pubview, now it's found in either the attribute or the
		// properties
		if (pubViewValue != null)
		{
			m_pubView = Boolean.valueOf(pubViewValue).booleanValue();
		}
		else
		{
			m_pubView = false;
		}

		// set the type, now it's found in either the attribute or the
		// properties
		m_type = typeValue;
	}

	/**
	 * ReConstruct.
	 * 
	 * @param id
	 * @param title
	 * @param type
	 * @param shortDesc
	 * @param description
	 * @param iconUrl
	 * @param infoUrl
	 * @param skin
	 * @param published
	 * @param joinable
	 * @param pubView
	 * @param joinRole
	 * @param isSpecial
	 * @param isUser
	 * @param createdBy
	 * @param createdOn
	 * @param modifiedBy
	 * @param modifiedOn
	 */
	public BaseSite(BaseSiteService siteService, String id, String title, String type, String shortDesc,
			String description, String iconUrl, String infoUrl, String skin,
			boolean published, boolean joinable, boolean pubView, String joinRole,
			boolean isSpecial, boolean isUser, String createdBy, Time createdOn,
			String modifiedBy, Time modifiedOn, boolean customPageOrdered,
			boolean isSoftlyDeleted, Date softlyDeletedDate, SessionManager sessionManager, UserDirectoryService userDirectoryService)
	{
		// Since deferred description loading is the edge case, assume the description is real.
		// This could be masked by extending String and using instanceof, or extending BaseSite to mark lazy instances,
		// but it not sure which is cleanest for now.
		this(siteService, id, title, type, shortDesc, description, iconUrl, infoUrl, skin, published, joinable, pubView, joinRole,
				isSpecial, isUser, createdBy, createdOn, modifiedBy, modifiedOn, customPageOrdered, isSoftlyDeleted, softlyDeletedDate,
				true, sessionManager, userDirectoryService);
	}

	public BaseSite(BaseSiteService siteService, String id, String title, String type, String shortDesc,
			String description, String iconUrl, String infoUrl, String skin,
			boolean published, boolean joinable, boolean pubView, String joinRole,
			boolean isSpecial, boolean isUser, String createdBy, Time createdOn,
			String modifiedBy, Time modifiedOn, boolean customPageOrdered,
			boolean isSoftlyDeleted, Date softlyDeletedDate, boolean descriptionLoaded, SessionManager sessionManager, UserDirectoryService userDirectoryService)
	{
		setupServices(siteService, sessionManager, userDirectoryService);

		// setup for properties
		m_properties = new BaseResourcePropertiesEdit();

		// set up the page list
		m_pages = new ResourceVector();

		// set up the groups collection
		m_groups = new ResourceVector();

		m_id = id;
		m_title = title;
		m_type = type;
		setShortDescription(shortDesc);
		setDescription(description);
		m_descriptionLoaded = descriptionLoaded;
		m_icon = iconUrl;
		m_info = infoUrl;
		m_skin = skin;
		m_published = published;
		m_joinable = joinable;
		m_pubView = pubView;
		m_joinerRole = joinRole;
		// TODO: isSpecial
		// TODO: isUser
		m_createdUserId = createdBy;
		m_lastModifiedUserId = modifiedBy;
		m_createdTime = createdOn;
		m_lastModifiedTime = modifiedOn;
		m_customPageOrdered = customPageOrdered;

		// setup for properties, but mark them lazy since we have not yet
		// established them from data
		((BaseResourcePropertiesEdit) m_properties).setLazy(true);

		m_pagesLazy = true;
		m_groupsLazy = true;
		
		// soft site deletions - new sites get defaults
		m_isSoftlyDeleted = isSoftlyDeleted;
		m_softlyDeletedDate = softlyDeletedDate;
		
	}

	/**
	 * Sets up the services needed by the BaseSite to operate
	 * @param siteService the BSS
	 * @param sessionManager the SM
	 * @param userDirectoryService the UDS
	 * @throws java.lang.IllegalStateException if the services would be null
	 */
	void setupServices(BaseSiteService siteService, SessionManager sessionManager, UserDirectoryService userDirectoryService) {
		this.siteService = siteService;
		if (this.siteService == null) {
			this.siteService = (BaseSiteService) ComponentManager.get(SiteService.class);
			if (this.siteService == null) {
				throw new IllegalStateException("Cannot get the SiteService when constructing BaseSite");
			}
		}
		this.authzGroupService = this.siteService.authzGroupService();
		this.sessionManager = sessionManager;
		if (this.sessionManager == null) {
			this.sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
			if (this.sessionManager == null) {
				throw new IllegalStateException("Cannot get the SessionManager when constructing BaseSite");
			}
		}
		this.userDirectoryService = userDirectoryService;
		if (this.userDirectoryService == null) {
			this.userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
			if (this.userDirectoryService == null) {
				throw new IllegalStateException("Cannot get the UserDirectoryService when constructing BaseSite");
			}
		}
	}

	/**
	 * Set me to be a deep copy of other (all but my id).
	 * 
	 * Note that this no longer triggers lazy loading as of KNL-1011. This should
	 * not cause any issues because the getters still trigger fetching by default.
	 * If a copy is made of a site that is not fully loaded, it should stay lazy,
	 * rather than accidentally triggering fetches.
	 *
	 * @param other
	 *        the other to copy.
	 * @param exact
	 *        If true, we copy ids - else we generate new ones for site, page
	 *        and tools.
	 */
	protected void set(BaseSite other, boolean exact)
	{
		// if exact, set the id, else assume the id was already set
		if (exact)
		{
			m_id = other.m_id;
		}

		m_title = other.m_title;
		m_shortDescription = other.m_shortDescription;
		m_htmlShortDescription = other.m_htmlShortDescription;
		m_description = other.m_description;
		m_htmlDescription = other.m_htmlDescription;
		m_descriptionLoaded = other.m_descriptionLoaded;
		m_joinable = other.m_joinable;
		m_joinerRole = other.m_joinerRole;
		m_published = other.m_published;
		m_icon = other.m_icon;
		m_info = other.m_info;
		m_skin = other.m_skin;
		m_type = other.m_type;
		m_pubView = other.m_pubView;
		m_customPageOrdered = other.m_customPageOrdered;
		if (this.siteService == null) {
			this.siteService = (BaseSiteService) ComponentManager.get(SiteService.class);
			if (this.siteService == null) {
				M_log.error("Cannot set the SiteService when set from BaseSite");
			}
		}
		sessionManager = other.sessionManager;
		if (this.sessionManager == null) {
			this.sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
			if (this.sessionManager == null) {
				M_log.error("Cannot set the SessionManager when set from BaseSite");
			}
		}
		userDirectoryService = other.userDirectoryService;
		if (this.userDirectoryService == null) {
			this.userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
			if (this.userDirectoryService == null) {
				M_log.error("Cannot set the UserDirectoryService when set from BaseSite");
			}
		}

		//site copies keep soft site deletion flags
		m_isSoftlyDeleted = other.m_isSoftlyDeleted;
		m_softlyDeletedDate = other.m_softlyDeletedDate;
		
		if (exact)
		{
			m_createdUserId = other.m_createdUserId;
		}
		else
		{
			m_createdUserId = userDirectoryService.getCurrentUser().getId();
		}
		m_lastModifiedUserId = other.m_lastModifiedUserId;
		if (other.m_createdTime != null)
			m_createdTime = (Time) other.m_createdTime.clone();
		if (other.m_lastModifiedTime != null)
			m_lastModifiedTime = (Time) other.m_lastModifiedTime.clone();

		// We make sure to avoid triggering fetching by passing false to getProperties
		m_properties = new BaseResourcePropertiesEdit();
		ResourceProperties pOther = other.getProperties(false);
		if (exact)
		{
			m_properties.addAll(pOther);
		}
		else
		{
			Iterator l = pOther.getPropertyNames();
			while (l.hasNext())
			{
				String pOtherName = (String) l.next();
				m_properties.addProperty(pOtherName, pOther.getProperty(pOtherName)
						.replaceAll(other.getId(), getId()));
			}
		}
		((BaseResourcePropertiesEdit) m_properties)
				.setLazy(((BaseResourceProperties) pOther).isLazy());

		// deep copy the pages, but avoid triggering fetching by passing false to getPages
		List<BaseSitePage> otherPages = new ArrayList<BaseSitePage>(other.getPages(false));
		List<BaseSitePage> copiedPages = new ArrayList<BaseSitePage>(otherPages.size());
		for (BaseSitePage page : otherPages) {
		    copiedPages.add(new BaseSitePage(siteService, page, this, exact));
		}
		m_pages = new ResourceVector(copiedPages);
		m_pagesLazy = other.m_pagesLazy;

		// deep copy the groups, but avoid triggering fetching by passing false to getGroups
		m_groups = new ResourceVector();
		for (Iterator iGroups = other.getGroups(false).iterator(); iGroups.hasNext();)
		{
			Group group = (Group) iGroups.next();
			m_groups.add(new BaseGroup(siteService, group, this, exact));
		}
		m_groupsLazy = other.m_groupsLazy;

		m_fullyLoaded = other.m_fullyLoaded;
	}

	/**
	 * @inheritDoc
	 */
	public String getId()
	{
		if (m_id == null) return "";
		return m_id;
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl()
	{
		Session s = sessionManager.getCurrentSession();
		String controllingPortal = (String) s.getAttribute("sakai-controlling-portal");
		String siteString = "/site/";
		if (controllingPortal != null)
		{
			siteString = "/" + controllingPortal + "/";
		}
		return siteService
				.serverConfigurationService().getPortalUrl()
				+ siteString + m_id;
	}

	/**
	 * @inheritDoc
	 */
	public String getReference()
	{
		return siteService.siteReference(m_id);
	}

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
	 * @inheritDoc
	 */
	public ResourceProperties getProperties()
	{
		// Default to loading the properties if lazy
		return getProperties(true);
	}

	/**
	 * Access the Site's properties, with control over fetching of lazy collections.
	 *
	 * The allowFetch flag is typically passed as true, but passed as false for
	 * fine-grained control while building copies, etc. This signature is not provided
	 * on the Site interface and is only intended for use within the implementation package.
	 *
	 * @param allowFetch
	 *        when true, fetch properties if not loaded;
	 *        when false, avoid fetching and return the properties collection as-is
	 * @return The Site's properties.
	 *
	 */
	public ResourceProperties getProperties(boolean allowFetch)
	{
		// if lazy, resolve unless requested to avoid fetching (as for copy constructor)
		if (allowFetch && ((BaseResourceProperties) m_properties).isLazy())
		{
			siteService.storage().readSiteProperties(
					this, m_properties);
			((BaseResourcePropertiesEdit) m_properties).setLazy(false);
		}

		return m_properties;
	}

	/**
	 * {@inheritDoc}
	 */
	public User getCreatedBy()
	{
		try
		{
			return userDirectoryService.getUser(m_createdUserId);
		}
		catch (Exception e)
		{
			return userDirectoryService.getAnonymousUser();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public User getModifiedBy()
	{
		try
		{
			return userDirectoryService.getUser(m_lastModifiedUserId);
		}
		catch (Exception e)
		{
			return userDirectoryService.getAnonymousUser();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Time getCreatedTime()
	{
		return m_createdTime;
	}

	public Date getCreatedDate() {
		return new Date(m_createdTime.getTime());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Time getModifiedTime()
	{
		return m_lastModifiedTime;
	}

	public Date getModifiedDate() {
		return new Date(m_lastModifiedTime.getTime());
	}
	/**
	 * @inheritDoc
	 */
	public String getTitle()
	{
		// if set here, use the setting
		if (m_title != null) return m_title;

		// if not otherwise set, use the id
		return getId();
	}

	/**
	 * @inheritDoc
	 */
	public String getShortDescription()
	{
		return m_shortDescription;
	}

	/** HTML escape and store the site's short description. */
	protected void escapeShortDescription()
	{
		m_htmlShortDescription = Web.escapeHtml(m_shortDescription);
	}

	/**
	 * @inheritDoc
	 */
	public String getHtmlShortDescription()
	{
		return m_htmlShortDescription;
	}

	/** HTML escape and store the site's full description. */
	protected void escapeDescription()
	{
		m_htmlDescription = Web.escapeHtml(m_description);
	}

	/**
	 * @inheritDoc
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * @inheritDoc
	 */
	public String getHtmlDescription()
	{
		return m_htmlDescription;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isJoinable()
	{
		return m_joinable;
	}

	/**
	 * @inheritDoc
	 */
	public String getJoinerRole()
	{
		return m_joinerRole;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPublished()
	{
		return m_published;
	}

	/**
	 * @inheritDoc
	 */
	public String getSkin()
	{
		return m_skin;
	}

	/**
	 * @inheritDoc
	 */
	public String getIconUrl()
	{
		return m_icon;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIconUrlFull()
	{
		return siteService
				.convertReferenceUrl(m_icon);
	}

	/**
	 * @inheritDoc
	 */
	public String getInfoUrl()
	{
		return m_info;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInfoUrlFull()
	{
		if (m_info == null) return null;

		return siteService
				.convertReferenceUrl(m_info);
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPages()
	{
		// Default to loading the pages if lazy
		return getPages(true);
	}

	/**
	 * Access the Site's list of pages, with control over fetching of lazy collections.
	 *
	 * The allowFetch flag is typically passed as true, but passed as false for
	 * fine-grained control while building copies, etc. This signature is not provided
	 * on the Site interface and is only intended for use within the implementation package.
	 *
	 * @param allowFetch
	 *        when true, fetch pages if not loaded;
	 *        when false, avoid fetching and return the page list as-is
	 * @return The Site's list of SitePages.
	 *
	 */
	public List getPages(boolean allowFetch)
	{
		if (allowFetch && m_pagesLazy)
		{
			siteService.storage().readSitePages(this,
					m_pages);
			m_pagesLazy = false;
		}

		return m_pages;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getGroups()
	{
		// Default to loading the groups if lazy
		return getGroups(true);
	}

	/**
	 * Access the Site's list of groups, with control over fetching of lazy collections.
	 *
	 * The allowFetch flag is typically passed as true, but passed as false for
	 * fine-grained control while building copies, etc. This signature is not provided
	 * on the Site interface and is only intended for use within the implementation package.
	 *
	 * @param allowFetch
	 *        when true, fetch groups if not loaded;
	 *        when false, avoid fetching and return the group list as-is
	 * @return The Site's list of Groups.
	 *
	 */
	public Collection getGroups(boolean allowFetch)
	{
		// Avoid fetching if requested (as for copy constructor)
		if (allowFetch && m_groupsLazy)
		{
			siteService.storage().readSiteGroups(
					this, m_groups);
			m_groupsLazy = false;
		}

		return m_groups;
	}

	/**
	 * {@inheritDoc}
	 */
    public Collection<String> getMembersInGroups(Set<String> groupIds) {
        @SuppressWarnings("unchecked")
        Collection<Group> siteGroups = getGroups();
		HashSet<String> siteGroupRefs = new HashSet<String>(siteGroups.size());
        for (Group group : siteGroups) {
            if (groupIds == null || // null groupIds includes all groups in the site
                    groupIds.contains(group.getId())) {
                siteGroupRefs.add(group.getReference());
            }
        }
		Collection<String> membersInGroups = authzGroupService.getAuthzUsersInGroups(siteGroupRefs);
		return membersInGroups;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Group> getGroupsWithMember(String userId)
	{
		Collection<Group> rv = new Vector<Group>();
		rv = getGroupsWithMembers(new String[] {userId});
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection<Group> getGroupsWithMembers(String[] userIds)
	{
		Collection<Group> siteGroups = getGroups();
		ArrayList<String> siteGroupRefs = new ArrayList<String>(siteGroups.size());
		for ( Iterator it=siteGroups.iterator(); it.hasNext(); )
			siteGroupRefs.add( ((Group)it.next()).getReference() );
		
		List groups = authzGroupService.getAuthzUserGroupIds(siteGroupRefs, userIds[0]);
		Collection<Group> rv = new Vector<Group>();
		
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			Member m = null;
			Group g = getGroup( (String)i.next() );
			
			if ( g != null )
			{
				for (int j=0; j<userIds.length;j++)
				{
					m = g.getMember(userIds[j]);
					if ((m == null) || (!m.isActive()))
					{
						break;
					}
				}
				if ((m != null) && (m.isActive()))
				{
					rv.add(g);
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getGroupsWithMemberHasRole(String userId, String role)
	{
		Collection siteGroups = getGroups();
		ArrayList<String> siteGroupRefs = new ArrayList<String>(siteGroups.size());
		for ( Iterator it=siteGroups.iterator(); it.hasNext(); )
			siteGroupRefs.add( ((Group)it.next()).getReference() );
			
		List groups = authzGroupService.getAuthzUserGroupIds(siteGroupRefs, userId);
		Collection<Group> rv = new Vector<Group>();
		for (Iterator i = groups.iterator(); i.hasNext();)
		{
			Member m = null;
			Group g = getGroup( (String)i.next() );
			if ( g != null )
				m = g.getMember(userId);
			if ((m != null) && (m.isActive()) && (m.getRole().getId().equals(role)))
				rv.add(g);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasGroups()
	{
		Collection groups = getGroups();
		return !groups.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void loadAll()
	{
		// Load up the full description if needed. If we fail to find the site in the database,
		// mark the description as loaded anyway to avoid retrying if multiple calls come in.
		if (!m_descriptionLoaded)
		{
			Site fullSite = siteService.storage().get(getId());
			if (fullSite != null)
			{
				setDescription(fullSite.getDescription());
			}
			m_descriptionLoaded = true;
		}

		// first, pages
		getPages();

		// KNL-259 - Avoiding single-page fetch of properties by way of BaseToolConfiguration constructor
		siteService.storage().readSitePageProperties(this);
		for (Iterator i = getPages().iterator(); i.hasNext();)
		{
			BaseSitePage page = (BaseSitePage) i.next();
			((BaseResourcePropertiesEdit) page.m_properties).setLazy(false);
		}

		// next, tools from all pages, all at once
		siteService.storage().readSiteTools(this);

		// get groups, all at once
		getGroups();

		// now all properties
		siteService.storage()
				.readAllSiteProperties(this);
		
		m_fullyLoaded = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getOrderedPages()
	{
		// if we are set to use our custom page order, do so
		if (m_customPageOrdered) return getPages();

		List order = siteService
				.serverConfigurationService().getToolOrder(getType());
		if (order.isEmpty()) return getPages();

		Map<String, String> pageCategoriesByTool = siteService.serverConfigurationService().getToolToCategoryMap(
				getType());

		boolean dynamicToolCategorization = siteService.serverConfigurationService().getBoolean(DYNAMIC_TOOL_CATEGORIZATION, false);

		// get a copy we can modify without changing the site!
		List pages = new Vector(getPages());

		// find any pages that include the tool type for each tool in the
		// ordering, move them into the newOrder and remove from the old
		List newOrder = new Vector();

		// for each entry in the order
		for (Iterator i = order.iterator(); i.hasNext();)
		{
			String toolId = (String) i.next();

			// find any pages that have this tool
			for (Iterator p = pages.iterator(); p.hasNext();)
			{
				SitePage page = (SitePage) p.next();
				// Dont remove if use dynamic categorization flag is set.
				if(!dynamicToolCategorization){
					// Remove dynamic page category property so that it is added back by the category mentioned in toolorder.xml	
					page.getProperties().removeProperty(SitePage.PAGE_CATEGORY_PROP);
				}
				List tools = page.getTools();
				for (Iterator t = tools.iterator(); t.hasNext();)
				{
					ToolConfiguration tool = (ToolConfiguration) t.next();
					if (tool.getToolId().equals(toolId))
					{
						// this page has this tool, so move it from the pages to
						// the newOrder
						newOrder.add(page);
						if(!dynamicToolCategorization){
							// Add property if it is categorized in toolorder.xml
							if (pageCategoriesByTool.get(toolId) != null)
							{
								page.getProperties().addProperty(SitePage.PAGE_CATEGORY_PROP,
										pageCategoriesByTool.get(toolId));
							}
						}
						p.remove();
						break;
					}
				}
			}
		}

		// add any remaining
		newOrder.addAll(pages);

		return newOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	public SitePage getPage(String id)
	{
		return (SitePage) ((ResourceVector) getPages()).getById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public ToolConfiguration getTool(String id)
	{
		// search the pages
		for (Iterator iPages = getPages().iterator(); iPages.hasNext();)
		{
			SitePage page = (SitePage) iPages.next();
			ToolConfiguration tool = page.getTool(id);

			if (tool != null) return tool;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getTools(String commonToolId)
	{
		String[] toolIds = new String[1];
		toolIds[0] = commonToolId;
		return getTools(toolIds);
	}

	/**
	 * {@inheritDoc}
	 */
	public ToolConfiguration getToolForCommonId(String commonToolId)
	{
		Collection col = getTools(commonToolId);
		if (col == null) return null;
		if (col.size() == 0) return null;
		return (ToolConfiguration) col.iterator().next(); // Return first
															// element
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getTools(String[] toolIds)
	{
		List rv = new Vector();
		if ((toolIds == null) || (toolIds.length == 0)) return rv;

		// search the pages
		for (Iterator iPages = getPages().iterator(); iPages.hasNext();)
		{
			SitePage page = (SitePage) iPages.next();
			rv.addAll(page.getTools(toolIds));
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Group getGroup(String id)
	{
		if (id == null) return null;

		// if this is a reference, starting with a "/", parse it, make sure it's
		// a group, in this site, and pull the id
		if (id.startsWith(Entity.SEPARATOR))
		{
			Reference ref = siteService
					.entityManager().newReference(id);
			if ((SiteService.APPLICATION_ID.equals(ref.getType()))
					&& (SiteService.GROUP_SUBTYPE.equals(ref.getSubType()))
					&& (m_id.equals(ref.getContainer())))
			{
				return (Group) ((ResourceVector) getGroups()).getById(ref.getId());
			}

			return null;
		}

		return (Group) ((ResourceVector) getGroups()).getById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getType()
	{
		return m_type;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isType(Object type)
	{
		if (type == null) return true;

		String myType = getType();

		if (type instanceof String[])
		{
			for (int i = 0; i < ((String[]) type).length; i++)
			{
				String test = ((String[]) type)[i];
				if ((test != null) && (test.equals(myType)))
				{
					return true;
				}
			}
		}

		else if (type instanceof Collection)
		{
			return ((Collection) type).contains(myType);
		}

		else if (type instanceof String)
		{
			return type.equals(myType);
		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseSite other = (BaseSite) obj;
		if (m_id == null) {
			if (other.m_id != null)
				return false;
		} else if (!m_id.equals(other.m_id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
		return result;
	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		if (!(obj instanceof Site)) throw new ClassCastException();

		// if the object are the same, say so
		if (obj == this) return 0;

		// start the compare by comparing their sort names
		int compare = getTitle().compareTo(((Site) obj).getTitle());

		// if these are the same
		if (compare == 0)
		{
			// sort based on (unique) id
			compare = getId().compareTo(((Site) obj).getId());
		}

		return compare;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPubView()
	{
		return m_pubView;
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element site = doc.createElement("site");
		if (stack.isEmpty())
		{
			doc.appendChild(site);
		}
		else
		{
			((Element) stack.peek()).appendChild(site);
		}

		site.setAttribute("id", getId());
		if (m_title != null) site.setAttribute("title", m_title);

		// encode the short description
		if (m_shortDescription != null)
			Xml.encodeAttribute(site, "short-description-enc", m_shortDescription);

		// encode the description
		if (m_description != null)
			Xml.encodeAttribute(site, "description-enc", m_description);

		site.setAttribute("joinable", Boolean.valueOf(m_joinable).toString());
		if (m_joinerRole != null) site.setAttribute("joiner-role", m_joinerRole);
		site.setAttribute("published", Boolean.valueOf(m_published).toString());
		if (m_icon != null) site.setAttribute("icon", m_icon);
		if (m_info != null) site.setAttribute("info", m_info);
		if (m_skin != null) site.setAttribute("skin", m_skin);
		site.setAttribute("pubView", Boolean.valueOf(m_pubView).toString());
		site.setAttribute("customPageOrdered", Boolean.valueOf(m_customPageOrdered)
				.toString());
		site.setAttribute("type", m_type);

		site.setAttribute("created-id", m_createdUserId);
		site.setAttribute("modified-id", m_lastModifiedUserId);
		site.setAttribute("created-time", m_createdTime.toString());
		site.setAttribute("modified-time", m_lastModifiedTime.toString());

		// properties
		stack.push(site);
		getProperties().toXml(doc, stack);
		stack.pop();

		// site pages
		Element list = doc.createElement("pages");
		site.appendChild(list);
		stack.push(list);
		for (Iterator iPages = getPages().iterator(); iPages.hasNext();)
		{
			BaseSitePage page = (BaseSitePage) iPages.next();
			page.toXml(doc, stack);
		}
		stack.pop();

		// TODO: site groups

		return site;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		m_title = StringUtils.trimToNull(title);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setShortDescription(String shortDescripion)
	{
		m_shortDescription = StringUtils.trimToNull(shortDescripion);
		escapeShortDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String description)
	{
		m_description = StringUtils.trimToNull(description);
		escapeDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJoinable(boolean joinable)
	{
		m_joinable = joinable;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJoinerRole(String role)
	{
		m_joinerRole = role;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPublished(boolean published)
	{
		m_published = published;

	}

	/**
	 * {@inheritDoc}
	 */
	public void setSkin(String skin)
	{
		if (Validator.checkSiteSkin(skin)) {
			m_skin = skin;			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIconUrl(String url)
	{
		m_icon = StringUtils.trimToNull(url);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInfoUrl(String url)
	{
		m_info = StringUtils.trimToNull(url);
	}

	/**
	 * {@inheritDoc}
	 */
	public SitePage addPage()
	{
		BaseSitePage page = new BaseSitePage(siteService,this);
		getPages().add(page);

		return page;
	}

	/**
	 * @inheritDoc
	 */
	public void removePage(SitePage page)
	{
		getPages().remove(page);
	}

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
	 * @inheritDoc
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		// if lazy, resolve
		if (((BaseResourceProperties) m_properties).isLazy())
		{
			siteService.storage().readSiteProperties(
					this, m_properties);
			((BaseResourcePropertiesEdit) m_properties).setLazy(false);
		}

		return m_properties;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setType(String type)
	{
		if (Validator.checkSiteType(type)) {
			m_type = type;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPubView(boolean pubView)
	{
		m_pubView = pubView;

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCustomPageOrdered()
	{
		return m_customPageOrdered;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCustomPageOrdered(boolean setting)
	{
		m_customPageOrdered = setting;
	}

	/**
	 * Enable editing.
	 */
	protected void activate()
	{
		m_active = true;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isActiveEdit()
	{
		return m_active;
	}

	/**
	 * Close the edit object - it cannot be used after this.
	 */
	protected void closeEdit()
	{
		m_active = false;
	}

	/**
	 * @inheritDoc
	 */
	public void regenerateIds()
	{
		// deep copy the pages
		ResourceVector newPages = new ResourceVector();
		for (Iterator iPages = getPages().iterator(); iPages.hasNext();)
		{
			BaseSitePage page = (BaseSitePage) iPages.next();
			newPages.add(new BaseSitePage(siteService,page, this, false));
		}

		m_pages = newPages;
	}

	/**
	 * {@inheritDoc}
	 */
	public Group addGroup()
	{
		Group rv = new BaseGroup(siteService, this);
		m_groups.add(rv);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeGroup(Group group)
	{
		// remove it
		m_groups.remove(group);

		// track so we can clean up related on commit
		m_deletedGroups.add(group);
	}

	/**
	 * Access (find if needed) the azg from the AuthzGroupService that
	 * implements my grouping.
	 * 
	 * @return My azg.
	 */
	protected AuthzGroup getAzg()
	{
		if (m_azg == null)
		{
			try
			{
				m_azg = authzGroupService.getAuthzGroup(getReference());
			}
			catch (GroupNotDefinedException e)
			{
				try
				{
					// create the site's azg, but don't store it yet (that
					// happens if save is called)

					// try the site created-by user for the maintain role in the
					// site
					String userId = getCreatedBy().getId();
					if (userId != null)
					{
						// make sure it's valid
						try
						{
							userDirectoryService.getUser(userId);
						}
						catch (UserNotDefinedException e1)
						{
							userId = null;
						}
					}

					// use the current user if needed
					if (userId == null)
					{
						User user = userDirectoryService.getCurrentUser();
						userId = user.getId();
					}

					// find the template for the new azg
					String groupAzgTemplate = siteService.siteAzgTemplate(this);
					AuthzGroup template = null;
					try
					{
						template = authzGroupService.getAuthzGroup(groupAzgTemplate);
					}
					catch (Exception e1)
					{
						try
						{
							// if the template is not defined, try the fall back
							// template
							template = authzGroupService.getAuthzGroup("!site.template");
						}
						catch (Exception e2)
						{
						}
					}

					m_azg = authzGroupService.newAuthzGroup(getReference(), template,
							userId);
					m_azgChanged = true;
				}
				catch (Exception t)
				{
					M_log.warn("getAzg: " + t);
				}
			}
		}

		return m_azg;
	}

	public void addMember(String userId, String roleId, boolean active, boolean provided)
	{
		m_azgChanged = true;
		getAzg().addMember(userId, roleId, active, provided);
	}

	public Role addRole(String id) throws RoleAlreadyDefinedException
	{
		m_azgChanged = true;
		return getAzg().addRole(id);
	}

	public Role addRole(String id, Role other) throws RoleAlreadyDefinedException
	{
		m_azgChanged = true;
		return getAzg().addRole(id, other);
	}

	public String getMaintainRole()
	{
		return getAzg().getMaintainRole();
	}

	public Member getMember(String userId)
	{
		return getAzg().getMember(userId);
	}

	public Set getMembers()
	{
		return getAzg().getMembers();
	}

	public String getProviderGroupId()
	{
		return getAzg().getProviderGroupId();
	}

	public Role getRole(String id)
	{
		return getAzg().getRole(id);
	}

	public Set getRoles()
	{
		return getAzg().getRoles();
	}

	public Set getRolesIsAllowed(String function)
	{
		return getAzg().getRolesIsAllowed(function);
	}

	public Role getUserRole(String userId)
	{
		return getAzg().getUserRole(userId);
	}

	public Set getUsers()
	{
		return getAzg().getUsers();
	}

	public Set getUsersHasRole(String role)
	{
		return getAzg().getUsersHasRole(role);
	}

	public Set getUsersIsAllowed(String function)
	{
		return getAzg().getUsersIsAllowed(function);
	}

	public boolean hasRole(String userId, String role)
	{
		return getAzg().hasRole(userId, role);
	}

	public boolean isAllowed(String userId, String function)
	{
		return getAzg().isAllowed(userId, function);
	}

	public boolean isEmpty()
	{
		return getAzg().isEmpty();
	}

	public void removeMember(String userId)
	{
		m_azgChanged = true;
		getAzg().removeMember(userId);
	}

	public void removeMembers()
	{
		m_azgChanged = true;
		getAzg().removeMembers();
	}

	public void removeRole(String role)
	{
		m_azgChanged = true;
		getAzg().removeRole(role);
	}

	public void removeRoles()
	{
		m_azgChanged = true;
		getAzg().removeRoles();
	}

	public void setMaintainRole(String role)
	{
		m_azgChanged = true;
		getAzg().setMaintainRole(role);
	}

	public void setProviderGroupId(String id)
	{
		m_azgChanged = true;
		getAzg().setProviderGroupId(id);
	}

	public boolean keepIntersection(AuthzGroup other)
	{
		boolean changed = getAzg().keepIntersection(other);
		if (changed) m_azgChanged = true;
		return changed;
	}

	public boolean isSoftlyDeleted() {
		return m_isSoftlyDeleted;
	}
	
	public Date getSoftlyDeletedDate() {
		return m_softlyDeletedDate;
	}
	
	public void setSoftlyDeleted(boolean flag) {
		m_isSoftlyDeleted = flag;
		if(flag) {
			m_softlyDeletedDate = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
		} else {
			m_softlyDeletedDate = null;
		}
	}

	/**
	 * Check if this Site's description field has been populated.
	 * Note that this is intentionally not exposed through the Site interface to keep it
	 * within the implementation package. The specifics of other lazy loading are not exposed
	 * through the Site interface; the collections are simply empty if not loaded. The
	 * SiteService encourages calls to {@link SiteService#getSite(String) getSite} and the
	 * Site interface exposes {@link Site#loadAll() loadAll} to ensure all loading.
	 *
	 * @return true if the description has been loaded for this Site object
	 */
	public boolean isDescriptionLoaded()
	{
		return m_descriptionLoaded;
	}
	
	/**
	 * Check whether this Site has been fully populated.
	 * Note that this is intentionally not exposed through the Site interface to keep it
	 * within the implementation package. The specifics of other lazy loading are not exposed
	 * through the Site interface; the collections are simply empty if not loaded. The
	 * SiteService encourages calls to {@link SiteService#getSite(String) getSite} and the
	 * Site interface exposes {@link Site#loadAll() loadAll} to ensure all loading.
	 *
	 * @return true if the Site object has been fully loaded (by loadAll)
	 */
	public boolean isFullyLoaded()
	{
		return m_fullyLoaded;
	}

	public void setFullyLoaded(boolean flag) {
		m_fullyLoaded = flag;
	}


}
