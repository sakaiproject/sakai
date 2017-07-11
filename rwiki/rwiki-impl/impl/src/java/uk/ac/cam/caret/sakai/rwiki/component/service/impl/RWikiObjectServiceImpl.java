/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.hibernate.HibernateException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.dao.impl.ListProxy;
import uk.ac.cam.caret.sakai.rwiki.component.model.impl.RWikiEntityImpl;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiPermissionsImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.EntityHandler;
import uk.ac.cam.caret.sakai.rwiki.service.api.PageLinkRenderer;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiPermissions;
import uk.ac.cam.caret.sakai.rwiki.service.exception.CreatePermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.ReadPermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author andrew
 */

// FIXME: Component
public class RWikiObjectServiceImpl implements RWikiObjectService
{

	private static Logger log = LoggerFactory.getLogger(RWikiObjectServiceImpl.class);

	private RWikiCurrentObjectDao cdao;

	private RWikiHistoryObjectDao hdao;

	// dependancy
	/**
	 * Contains a map of handler beans injected
	 */
	private Map m_handlers = null;

	public String createTemplatePageName = "default_template"; //$NON-NLS-1$

	private RWikiSecurityService wikiSecurityService;

	private RenderService renderService;

	private PreferenceService preferenceService;

	private EntityManager entityManager;

	private NotificationService notificationService;

	private SessionManager sessionManager;

	private EventTrackingService eventTrackingService;

	private SiteService siteService;

	private ThreadLocalManager threadLocalManager;

	private TimeService timeService;

	private DigestService digestService;

	private SecurityService securityService;

	/** Configuration: to run the ddl on init or not. */
	protected boolean autoDdl = false;

	private AliasService aliasService;

	private UserDirectoryService userDirectoryService;
	
	private int maxReferencesStringSize = 4000;

	private boolean trackReads = ServerConfigurationService.getBoolean("wiki.trackreads", false);
   
	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		autoDdl = Boolean.valueOf(value).booleanValue();
	}
	/**
	 * Register this as an EntityProducer
	 */
	public void init()
	{
		log.debug("init start"); //$NON-NLS-1$
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		entityManager = (EntityManager) load(cm, EntityManager.class.getName());
		notificationService = (NotificationService) load(cm,
				NotificationService.class.getName());
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		eventTrackingService = (EventTrackingService) load(cm,
				EventTrackingService.class.getName());
		siteService = (SiteService) load(cm, SiteService.class.getName());
		threadLocalManager = (ThreadLocalManager) load(cm,
				ThreadLocalManager.class.getName());
		timeService = (TimeService) load(cm, TimeService.class.getName());
		digestService = (DigestService) load(cm, DigestService.class.getName());
		securityService = (SecurityService) load(cm, SecurityService.class
				.getName());
		wikiSecurityService = (RWikiSecurityService) load(cm,
				RWikiSecurityService.class.getName());
		renderService = (RenderService) load(cm, RenderService.class.getName());
		preferenceService = (PreferenceService) load(cm,
				PreferenceService.class.getName());
		userDirectoryService = (UserDirectoryService) load(cm,UserDirectoryService.class.getName());
		entityManager.registerEntityProducer(this,
				RWikiObjectService.REFERENCE_ROOT);
		if (ServerConfigurationService.getBoolean("wiki.notification", true)) //$NON-NLS-1$
		{
			// Email notification
			// register a transient notification for resources
			NotificationEdit edit = notificationService
					.addTransientNotification();

			// set functions
			edit.setFunction(RWikiObjectService.EVENT_RESOURCE_ADD);
			edit.addFunction(RWikiObjectService.EVENT_RESOURCE_WRITE);
			edit.addFunction(RWikiObjectService.EVENT_RESOURCE_READ);

			// set the filter to any site related resource
			edit.setResourceFilter(RWikiObjectService.REFERENCE_ROOT);
			// %%% is this the best we can do? -ggolden

			// set the action
			edit.setAction(new SiteEmailNotificationRWiki(this,
					this.renderService, this.preferenceService,
					this.siteService, this.securityService, this.entityManager,
					this.threadLocalManager, this.timeService,
					this.digestService, this.userDirectoryService));
		}
		
		
		try
		{
			if (autoDdl)
			{
				SqlService.getInstance().ddl(this.getClass().getClassLoader(),
						"sakai_rwiki");
			}
		}
		catch (Exception ex)
		{
			log.error("Perform additional SQL setup", ex);
		}
		
		maxReferencesStringSize = ServerConfigurationService.getInt("wiki.maxReferences",4000);

		
		log.debug("init end"); //$NON-NLS-1$

	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name); //$NON-NLS-1$
		}
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#getRWikiObject(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public RWikiCurrentObject getRWikiObject(String name, String realm)
			throws PermissionException
	{
		return getRWikiObject(name, realm, null, createTemplatePageName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#getRWikiObject(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public RWikiCurrentObject getRWikiObject(String name, String realm,
			RWikiObject ignore, String templatePage) throws PermissionException
	{
		long start = System.currentTimeMillis();
		String user = sessionManager.getCurrentSessionUserId();
		try
		{

			if (log.isDebugEnabled())
			{
				log.debug("Looking for object with name " + name + " in realm " //$NON-NLS-1$ //$NON-NLS-2$
						+ realm + " for user " + user); //$NON-NLS-1$
			}

			// May throw Permission Exception...
			// only globalise if not already
			name = NameHelper.globaliseName(name, realm);
			long start2 = System.currentTimeMillis();
			RWikiCurrentObject returnable;
			try
			{
				returnable = cdao.findByGlobalName(name);
			}
			finally
			{
				long finish = System.currentTimeMillis();
				TimeLogger.printTimer("dao.findByGlobalName: " + name, start2, //$NON-NLS-1$
						finish);
			}

			if (returnable == null)
			{

				String permissionsReference = wikiSecurityService
						.createPermissionsReference(realm);

				if (!wikiSecurityService
						.checkCreatePermission(permissionsReference))
				{
					throw new CreatePermissionException("User: " + user //$NON-NLS-1$
							+ " cannot create pages in realm: " + realm); //$NON-NLS-1$
				}
				returnable = cdao.createRWikiObject(name, realm);
				// zero in on the correct space.
				String pageSpace = NameHelper.localizeSpace(name, realm);
				String defTemplate = NameHelper.globaliseName(templatePage,
						pageSpace);
				RWikiCurrentObject template = cdao
						.findByGlobalName(defTemplate);
				if (template != null)
				{
					returnable.setContent(template.getContent());
					returnable.setPermissions(template.getPermissions());
					returnable.setGroupWrite(true); 
					returnable.setGroupRead(true);  //SAK SAK-8234
					returnable.setUser(user);
					returnable.setOwner(user);
					returnable.setRealm(realm);
					returnable.setReferenced(template.getReferenced());
					returnable.setSha1(template.getSha1());
				}
				return returnable;
			}
			else if (wikiSecurityService
					.checkRead((RWikiEntity) getEntity(returnable)))
			{
				// Allowed to read this object
				return returnable;
			}
			else
			{
				throw new ReadPermissionException(user, returnable);
			}
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("dao.GetRWikiObject: " + name + ", " + user //$NON-NLS-1$ //$NON-NLS-2$
					+ ", " + realm, start, finish); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#findByGlobalNameAndContents(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public List search(String criteria, String realm)
			throws PermissionException
	{

		String user = sessionManager.getCurrentSessionUserId();

		String permissionsReference = wikiSecurityService
				.createPermissionsReference(realm);
		if (!wikiSecurityService.checkSearchPermission(permissionsReference))
		{
			throw new ReadPermissionException(user, realm);
		}

		return cdao.findByGlobalNameAndContents(criteria, user, realm);
	}

	public RWikiCurrentObjectDao getRWikiCurrentObjectDao()
	{
		return cdao;
	}

	public void setRWikiCurrentObjectDao(RWikiCurrentObjectDao cdao)
	{
		this.cdao = cdao;
	}

	public RWikiHistoryObjectDao getRWikiHistoryObjectDao()
	{
		return hdao;
	}

	public void setRWikiHistoryObjectDao(RWikiHistoryObjectDao hdao)
	{
		this.hdao = hdao;
	}

	public void update(String name, String realm, Date version, String content,
			RWikiPermissions permissions) throws PermissionException,
			VersionException
	{

		String user = sessionManager.getCurrentSessionUserId();
		update(name, user, realm, version, content, permissions);
	}

	/**
	 * This will update an object setting the modified by and owner using the
	 * supplied user and using the <b>current user</b> for permissions. The
	 * reason this is private and is in the service at all, is that we need to
	 * be able to move rwiki objects about on behalf of annother user
	 * 
	 * @param name
	 * @param user
	 *        The user to set owner and modified by, normally the current user
	 * @param realm
	 * @param version
	 * @param content
	 * @param permissions
	 * @throws PermissionException
	 * @throws VersionException
	 */
	private void update(String name, String user, String realm, Date version,
			String content, RWikiPermissions permissions)
			throws PermissionException, VersionException, RuntimeException
	{

		// May throw ReadPermissionException...

		RWikiCurrentObject rwo = getRWikiObject(name, realm);
		RWikiHistoryObject rwho = null;

		if (wikiSecurityService.checkUpdate((RWikiEntity) getEntity(rwo)))
		{
			rwho = updateContent(rwo, content, version);
		}
		else
		{
			throw new UpdatePermissionException("User: " + user //$NON-NLS-1$
					+ " doesn't have permission to update: " + name); //$NON-NLS-1$
		}

		if (permissions != null)
		{
			if (wikiSecurityService.checkAdmin((RWikiEntity) getEntity(rwo)))
			{
				rwo.setPermissions(permissions);
			}
			else
			{
				throw new UpdatePermissionException("User: " + user //$NON-NLS-1$
						+ " doesn't have permission to update and admin: " //$NON-NLS-1$
						+ name);
			}
		}

		rwo.setUser(user);
		if (rwo.getOwner() == null)
		{
			rwo.setOwner(user);
		}
		try
		{
			cdao.update(rwo, rwho);
			Entity e = getEntity(rwo);
			int revision = 1;
			try 
			{
				revision = rwo.getRevision().intValue();
			}
			catch ( Exception ex ) 
			{
			}

			int notiPriority = NotificationService.PREF_IMMEDIATE;
        	if (RWikiObjectService.SMALL_CHANGE_IN_THREAD.equals(threadLocalManager
					.get(RWikiObjectService.SMALL_CHANGE_IN_THREAD)))
        	{
        		notiPriority = NotificationService.PREF_NONE;
        	}

			if ( revision == 1 ) 
			{
				eventTrackingService.post(eventTrackingService.newEvent(
					EVENT_RESOURCE_ADD, e.getReference(), true,
					notiPriority));
			}
			else 
			{
				eventTrackingService.post(eventTrackingService.newEvent(
					EVENT_RESOURCE_WRITE, e.getReference(), true,
					notiPriority));
			}
		}
		catch (HibernateOptimisticLockingFailureException e)
		{
			throw new VersionException("Version has changed since: " + version, //$NON-NLS-1$
					e);
		}
		catch (HibernateException e)
		{
			log.info("Caught hibernate exception, update failed."+e.getMessage());
			throw new RuntimeException("An update could not be made to this wiki page. A possible cause is that you have too many links.");
		}

	}

	public void update(String name, String realm, Date version, String content)
			throws PermissionException, VersionException
	{
		// May throw ReadPermissionException...
		update(name, realm, version, content, null);
	}

	public void update(String name, String realm, Date version,
			RWikiPermissions permissions) throws PermissionException,
			VersionException
	{
		if (permissions == null)
		{
			throw new IllegalArgumentException("permissions must not be null"); //$NON-NLS-1$
		}
		String user = sessionManager.getCurrentSessionUserId();

		RWikiCurrentObject rwo = getRWikiObject(name, realm);

		if (wikiSecurityService.checkAdmin((RWikiEntity) getEntity(rwo)))
		{
			RWikiHistoryObject rwho = hdao.createRWikiHistoryObject(rwo);
			rwo.setRevision(Integer.valueOf(rwo.getRevision().intValue() + 1));
			rwo.setPermissions(permissions);
			rwo.setVersion(version);
			try
			{
				cdao.update(rwo, rwho);
				// track it
				Entity e = getEntity(rwo);
            	int revision = 1;
            	try
            	{
               	 	revision = rwo.getRevision().intValue();
            	}
            	catch ( Exception ex )
            	{
            	}

            	int notiPriority = NotificationService.PREF_IMMEDIATE;
            	if (threadLocalManager.get(RWikiObjectService.SMALL_CHANGE_IN_THREAD) != null)
            	{
            		notiPriority = NotificationService.PREF_NONE;
            	}

            	if ( revision == 1 )
            	{
               		 eventTrackingService.post(eventTrackingService.newEvent(
                       		 EVENT_RESOURCE_ADD, e.getReference(), true,
                       		 notiPriority));
            	}
            	else
            	{
               		 eventTrackingService.post(eventTrackingService.newEvent(
                       		 EVENT_RESOURCE_WRITE, e.getReference(), true,
                       		notiPriority));
            	}
			}
			catch (HibernateOptimisticLockingFailureException e)
			{
				throw new VersionException("Version has changed since: " //$NON-NLS-1$
						+ version, e);
			}

		}
		else
		{
			throw new UpdatePermissionException("User: " + user //$NON-NLS-1$
					+ " doesn't have permission to update and admin: " + name); //$NON-NLS-1$
		}

	}

	private RWikiHistoryObject updateContent(RWikiCurrentObject rwo,
			String content, Date version)
	{
		// We set the version in order to allow hibernate to tell us if the
		// object has been changed since we last looked at it.
		if (version != null)
		{
			rwo.setVersion(version);
		}

		if (content != null && !content.equals(rwo.getContent()))
		{
			// create a history instance
			RWikiHistoryObject rwho = hdao.createRWikiHistoryObject(rwo);

			// set the content and increment the revision
			rwo.setContent(content.replaceAll("\r\n?", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
			rwo.setRevision(Integer.valueOf(rwo.getRevision().intValue() + 1));

			// render to get a list of links
			final HashSet referenced = new HashSet();

			// Links should be globalised against the page space!
			final String currentSpace = NameHelper.localizeSpace(rwo.getName(),
					rwo.getRealm());

			PageLinkRenderer plr = new PageLinkRenderer()
			{
				public void appendLink(StringBuffer buffer, String name, String view, String anchor, boolean autoGenerated)
				{
					if (!autoGenerated) 
					{
						this.appendLink(buffer, name, view, anchor);
					}
				}
				public void appendLink(StringBuffer buffer, String name,
						String view)
				{
					this.appendLink(buffer, name, view, null);
				}

				public void appendLink(StringBuffer buffer, String name,
						String view, String anchor)
				{
					referenced
							.add(NameHelper.globaliseName(name, currentSpace));
				}

				public void appendCreateLink(StringBuffer buffer, String name,
						String view)
				{
					referenced
							.add(NameHelper.globaliseName(name, currentSpace));
				}

				public boolean isCachable()
				{
					return false; // should not cache this render op
				}

				public boolean canUseCache()
				{
					return false;
				}

				public void setCachable(boolean cachable)
				{
					//do nothing
				}

				public void setUseCache(boolean b)
				{
					//do nothing
				}

				

			};

			renderService.renderPage(rwo, currentSpace, plr);

			// process the references
			StringBuffer sb = extractReferences(rwo, referenced);
			rwo.setReferenced(sb.toString());

			return rwho;
		}
		return null;

	}
	
	/**
	 *  Add page references to the rwiki object.  Limit length of the 
	 *  string to save to a fixed value that will cover the common cases 
	 *  without using resources for degenerate cases.
	 * @param rwo - The rwiki object
	 * @param referenced - the hash of the references to save.
	 */
	public StringBuffer extractReferences(RWikiCurrentObject rwo, final HashSet referenced) {
		
		StringBuffer sb = new StringBuffer();
		Iterator i = referenced.iterator();
		String next = null;
		while (i.hasNext())
		{
			next = (String) i.next();
			int referencedLength = sb.length()+4+next.length();
			if (referencedLength >= maxReferencesStringSize) { // SAK-12115
				break;
			}
			else
			{
				sb.append("::").append(next); //$NON-NLS-1$
			}
		}
		sb.append("::"); //$NON-NLS-1$
		return sb;
	}


	public boolean exists(String name, String space)
	{
		long start = System.currentTimeMillis();
		try
		{

			String globalName = NameHelper.globaliseName(name, space);

			return cdao.exists(globalName);

		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("Exists: " + name, start, finish); //$NON-NLS-1$

		}
	}

	public List findChangedSince(Date since, String realm)
	{
		//
		// if (!securityService.checkSearchPermission(user, realm)) {
		// throw new ReadPermissionException(user, realm);
		// }

		// TODO Permissions ?
		return cdao.findChangedSince(since, realm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#findReferencingPages(java.lang.String)
	 */
	public List findReferencingPages(String name)
	{
		// TODO Permissions ?
		return cdao.findReferencingPages(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#revert(java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Date, int)
	 */
	public void revert(String name, String realm, Date version, int revision)
	{
		// TODO Permissions ?
		RWikiCurrentObject rwikiObject = getRWikiObject(name, realm);

		String content = hdao.getRWikiHistoryObject(rwikiObject, revision)
				.getContent();

		update(name, realm, version, content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#getRWikiObject(java.lang.String)
	 */
	public RWikiCurrentObject getRWikiObject(RWikiObject reference)
	{
		// TODO Permissions ?
		return cdao.getRWikiCurrentObject(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#getRWikiHistoryObject(java.lang.String,
	 *      int)
	 */
	public RWikiHistoryObject getRWikiHistoryObject(RWikiObject reference,
			int revision)
	{
		// TODO Permissions ?
		return hdao.getRWikiHistoryObject(reference, revision);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#findRWikiHistoryObjects(java.lang.String)
	 */
	public List findRWikiHistoryObjects(RWikiObject reference)
	{
		// TODO Permissions ?
		return hdao.findRWikiHistoryObjects(reference);
	}

	public List findRWikiHistoryObjectsInReverse(RWikiObject reference)
	{
		// TODO Permissions ?
		return hdao.findRWikiHistoryObjectsInReverse(reference);
	}

	/**
	 * @return Returns the createTemplatePageName.
	 */
	public String getCreateTemplatePageName()
	{
		return createTemplatePageName;
	}

	/**
	 * @param createTemplatePageName
	 *        The createTemplatePageName to set.
	 */
	public void setCreateTemplatePageName(String createTemplatePageName)
	{
		this.createTemplatePageName = createTemplatePageName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#findRWikiSubPages(java.lang.String)
	 */
	public List findRWikiSubPages(String globalParentPageName)
	{
		// TODO Permissions ?
		return cdao.findRWikiSubPages(globalParentPageName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#updateNewComment(java.lang.String,
	 *      java.lang.String, java.lang.String, java.util.Date,
	 *      java.lang.String)
	 */
	public void updateNewComment(String name, String realm, Date version,
			String content) throws PermissionException, VersionException
	{

		int retries = 0;
		while (retries < 5)
		{
			try
			{
				RWikiObject lastComment = cdao.findLastRWikiSubPage(name);
				int cnum = 0;
				if (lastComment != null)
				{
					String lastCommentName = lastComment.getName();
					int lastp = lastCommentName.lastIndexOf("."); //$NON-NLS-1$
					if (lastp >= 0)
					{
						try {
							cnum = Integer.parseInt(lastCommentName
								.substring(lastp + 1)) + 1;
						} catch ( Exception ex) {
							// this is Ok
						}
					}
				}
				String newCommentName = MessageFormat.format(
						"{0}.{1,number,000}", new Object[] { name, //$NON-NLS-1$
								Integer.valueOf(cnum) });
				update(newCommentName, realm, version, content);
				break;
			}
			catch (VersionException e)
			{
				if (retries >= 5) throw e;
				retries++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#createListProxy(java.util.List,
	 *      uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy)
	 */
	public List createListProxy(List commentsList, ObjectProxy lop)
	{
		return new ListProxy(commentsList, lop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#createNewRWikiCurrentObject()
	 */
	public RWikiObject createNewRWikiCurrentObject()
	{
		// RWikiCurrentObjectImpl rwco = new RWikiCurrentObjectImpl();
		// rwco.setRwikiObjectContentDao(c)
		return cdao.createRWikiObject("dummy", "dummy"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService#createNewRWikiPermissionsImpl()
	 */
	public RWikiPermissions createNewRWikiPermissionsImpl()
	{
		return new RWikiPermissionsImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return REFERENCE_LABEL;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextCreated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableWiki(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableWiki(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		disableWiki(context);
	}

	/**
	 * {@inheritDoc} Archive all the wiki pages in the site as a single
	 * collection
	 */
	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments)
	{

		// TODO Permissions ?

		// prepare the buffer for the results log
		StringBuffer results = new StringBuffer();
		results.append(Messages.getString("RWikiObjectServiceImpl.32")).append(siteId).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		log.debug("archiving Wiki Pages for " + siteId); //$NON-NLS-1$
		int npages = 0;
		int nversions = 0;

		try
		{
			String defaultRealm = siteService.getSite(siteId).getReference();

			wikiSecurityService
					.checkAdminPermission(RWikiObjectService.REFERENCE_ROOT
							+ defaultRealm);
			// start with an element with our very own name
			Element element = doc.createElement(APPLICATION_ID);
			((Element) stack.peek()).appendChild(element);
			stack.push(element);

			try
			{

				List l = cdao.findRWikiSubPages("/site/" + siteId); //$NON-NLS-1$
				for (Iterator i = l.iterator(); i.hasNext();)
				{

					RWikiObject rwo = (RWikiObject) i.next();
					RWikiEntity rwe = (RWikiEntity) getEntity(rwo);
					log.debug("Archiving " + rwo.getName()); //$NON-NLS-1$
					rwe.toXml(doc, stack);
					npages++;
					List lh = this.findRWikiHistoryObjects(rwo);
					if (lh != null)
					{
						for (Iterator ih = lh.iterator(); ih.hasNext();)
						{
							RWikiObject rwoh = (RWikiObject) ih.next();
							RWikiEntity rwoeh = (RWikiEntity) getEntity(rwoh);
							log.debug("Archiving " + rwoh.getName() //$NON-NLS-1$
									+ " version " + rwoh.getVersion()); //$NON-NLS-1$
							rwoeh.toXml(doc, stack);
							nversions++;
						}
					}
				}
			}
			catch (Exception any)
			{
				any.printStackTrace();
				results.append(Messages.getString("RWikiObjectServiceImpl.31") + siteId //$NON-NLS-1$
						+ " " + any.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			results.append(Messages.getString("RWikiObjectServiceImpl.30")).append(npages).append( //$NON-NLS-1$
					Messages.getString("RWikiObjectServiceImpl.43")).append(nversions).append(Messages.getString("RWikiObjectServiceImpl.44")); //$NON-NLS-1$ //$NON-NLS-2$
			stack.pop();
		}
		catch (IdUnusedException ex)
		{

		}

		return results.toString();
	}

	/**
	 * {@inheritDoc} The archive contains the current version, followed by
	 * historical versions If any of these aer out of order, only versions upto
	 * the first encoundered version will be merged. If the page exists, then
	 * only version that dont exist, and are not already present will be added
	 * in practice this means all the pages in the set will be rejected.
	 */
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		log.info(" wiki Merge"); //$NON-NLS-1$
		// TODO Permissions ?
		// stolen :) from BaseContentService
		// get the system name: FROM_WT, FROM_CT, FROM_SAKAI
		// String source = null;
		// root: <service> node
		// Node parent = root.getParentNode(); // parent: <archive> node
		// containing
		// "system"
		// if (parent.getNodeType() == Node.ELEMENT_NODE) {
		// Element parentEl = (Element) parent;
		// source = parentEl.getAttribute("system");
		// }

		// prepare the buffer for the results log
		StringBuffer results = new StringBuffer();
		int nversions_reject = 0;
		int npages = 0;
		int nversions = 0;
		int npages_fail = 0;
		int npages_errors = 0;
		try
		{
			String defaultRealm = siteService.getSite(siteId).getReference();

			wikiSecurityService
					.checkAdminPermission(RWikiObjectService.REFERENCE_ROOT
							+ defaultRealm);

			NodeList children = root.getChildNodes();
			final int length = children.getLength();
			log.info("Archive has " + length + " pages "); //$NON-NLS-1$ //$NON-NLS-2$
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				try
				{

					RWikiCurrentObject archiverwo = cdao.createRWikiObject(
							"dummy", "dummy"); //$NON-NLS-1$ //$NON-NLS-2$
					RWikiEntity rwe = (RWikiEntity) getEntity(archiverwo);
					rwe.fromXml(element, defaultRealm);
					log.info(" Merging " + archiverwo.getRevision() + ":" //$NON-NLS-1$ //$NON-NLS-2$
							+ rwe.getReference());

					// clear the ID to remove hibernate session issues and
					// recreate
					// a new id issues
					archiverwo.setId(null);

					String pageName = archiverwo.getName();

					if (exists(pageName, defaultRealm))
					{
						// page exists, add to history, if the version does not
						// exist
						RWikiObject rwo = getRWikiObject(pageName, defaultRealm);
						if (archiverwo.getRevision().intValue() >= rwo
								.getRevision().intValue())
						{
							nversions_reject++;
							results
									.append(Messages.getString("RWikiObjectServiceImpl.29")) //$NON-NLS-1$
									.append(rwo.getName())
									.append(Messages.getString("RWikiObjectServiceImpl.28")) //$NON-NLS-1$
									.append(rwo.getRevision())
									.append(
											Messages.getString("RWikiObjectServiceImpl.54")) //$NON-NLS-1$
									.append(archiverwo.getRevision())
									.append(
											Messages.getString("RWikiObjectServiceImpl.55")) //$NON-NLS-1$
									.append(
											Messages.getString("RWikiObjectServiceImpl.56")) //$NON-NLS-1$
									.append(
											Messages.getString("RWikiObjectServiceImpl.57")); //$NON-NLS-1$
						}
						else
						{
							RWikiHistoryObject rwho = getRWikiHistoryObject(
									rwo, archiverwo.getRevision().intValue());
							if (rwho == null)
							{
								rwho = hdao
										.createRWikiHistoryObject(archiverwo);
								// connect to the correct master object
								rwho.setRwikiobjectid(rwo.getId());
								// save
								hdao.update(rwho);

								rwho = getRWikiHistoryObject(rwo, archiverwo
										.getRevision().intValue());
								nversions++;
							}
							else
							{
								nversions_reject++;
								results
										.append(Messages.getString("RWikiObjectServiceImpl.58")) //$NON-NLS-1$
										.append(rwo.getName())
										.append(
												Messages.getString("RWikiObjectServiceImpl.59")) //$NON-NLS-1$
										.append(rwo.getRevision())
										.append(
												Messages.getString("RWikiObjectServiceImpl.60") //$NON-NLS-1$
														+ Messages.getString("RWikiObjectServiceImpl.61") //$NON-NLS-1$
														+ Messages.getString("RWikiObjectServiceImpl.62") //$NON-NLS-1$
														+ Messages.getString("RWikiObjectServiceImpl.63")); //$NON-NLS-1$
							}
						}

					}
					else
					{
						// page does not exist, create
						String newUser = (String) userIdTrans.get(archiverwo
								.getOwner());
						if (newUser == null) newUser = archiverwo.getOwner();

						// go direct, if we use the utility methods, all sorts
						// of
						// things get reset, which is bad
						cdao.update(archiverwo, null);

						RWikiObject savedrwo = getRWikiObject(archiverwo
								.getName(), archiverwo.getRealm());
						if (archiverwo.getSha1().equals(savedrwo.getSha1()))
						{
							npages++;
						}
						else
						{
							npages_errors++;
							results.append(Messages.getString("RWikiObjectServiceImpl.64")).append( //$NON-NLS-1$
									savedrwo.getName()).append(Messages.getString("RWikiObjectServiceImpl.65")) //$NON-NLS-1$
									.append(savedrwo.getRevision()).append(
											Messages.getString("RWikiObjectServiceImpl.66")).append( //$NON-NLS-1$
											savedrwo.getVersion().getTime())
									.append(Messages.getString("RWikiObjectServiceImpl.67")).append( //$NON-NLS-1$
											savedrwo.getVersion()).append("\n"); //$NON-NLS-1$
							results
									.append(
											Messages.getString("RWikiObjectServiceImpl.69")) //$NON-NLS-1$
									.append(archiverwo.getSha1()).append(
											Messages.getString("RWikiObjectServiceImpl.70")).append( //$NON-NLS-1$
											savedrwo.getSha1()).append(
											Messages.getString("RWikiObjectServiceImpl.71")).append( //$NON-NLS-1$
											archiverwo.getContent()).append(
											Messages.getString("RWikiObjectServiceImpl.72")).append( //$NON-NLS-1$
											savedrwo.getContent()).append("\n"); //$NON-NLS-1$
						}
					}
				}
				catch (Exception ex)
				{
					npages_fail++;
					log.error("Failed to add page ", ex); //$NON-NLS-1$
					results.append(Messages.getString("RWikiObjectServiceImpl.75")).append( //$NON-NLS-1$
							element.getAttribute(Messages.getString("RWikiObjectServiceImpl.76"))).append( //$NON-NLS-1$
							Messages.getString("RWikiObjectServiceImpl.77")).append( //$NON-NLS-1$
							element.getAttribute(Messages.getString("RWikiObjectServiceImpl.78"))).append( //$NON-NLS-1$
							Messages.getString("RWikiObjectServiceImpl.79")).append(ex.getMessage()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

				}
			}
		}
		catch (IdUnusedException ex)
		{
			results.append(Messages.getString("RWikiObjectServiceImpl.81")).append( //$NON-NLS-1$
					siteId).append(" :").append(ex.getMessage()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		results.append(Messages.getString("RWikiObjectServiceImpl.84")).append(Messages.getString("RWikiObjectServiceImpl.85")) //$NON-NLS-1$ //$NON-NLS-2$
				.append(npages).append(Messages.getString("RWikiObjectServiceImpl.86")).append(nversions) //$NON-NLS-1$
				.append(Messages.getString("RWikiObjectServiceImpl.87")).append(nversions_reject).append( //$NON-NLS-1$
						Messages.getString("RWikiObjectServiceImpl.88")).append(npages_fail).append(Messages.getString("RWikiObjectServiceImpl.89")) //$NON-NLS-1$ //$NON-NLS-2$
				.append(npages_errors).append(Messages.getString("RWikiObjectServiceImpl.90")); //$NON-NLS-1$
		return results.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.rwiki" }; //$NON-NLS-1$
		return toolIds;
	}

	/**
	 * {@inheritDoc} Only the current version of a page is imported, history is
	 * left behind.
	 */
	public void transferCopyEntities(String fromContext, String toContext,
			List ids)
	{
		log.debug("==================Doing WIki transfer"); //$NON-NLS-1$
		if (fromContext.equals(toContext))
		{
			log
					.debug("===================Source and Target Context are identical, transfer ignored"); //$NON-NLS-1$
			return;
		}
		
		// FIXME this needs to be moved out to a method!
		if (!fromContext.startsWith("/")) //$NON-NLS-1$
		{
			fromContext = "/site/" + fromContext; //$NON-NLS-1$
		}
		if (!toContext.startsWith("/")) //$NON-NLS-1$
		{
			toContext = "/site/" + toContext; //$NON-NLS-1$
		}
		if (fromContext.endsWith("/") && fromContext.length() > 1) { //$NON-NLS-1$
			fromContext = fromContext.substring(0, fromContext.length() - 1);
		}
		if (toContext.endsWith("/") && toContext.length() > 1) { //$NON-NLS-1$
			toContext = toContext.substring(0, toContext.length() - 1);
		}


		log.debug("=================Locating Pages in from Content of " //$NON-NLS-1$
				+ fromContext);
		List pages = findRWikiSubPages(fromContext.length() > 1 ? fromContext + "/" : fromContext); //$NON-NLS-1$
		log.debug("=================Found " + pages.size() + " Pages"); //$NON-NLS-1$ //$NON-NLS-2$

		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			RWikiObject rwo = (RWikiObject) i.next();
			RWikiEntity rwe = (RWikiEntity) getEntity(rwo);
			wikiSecurityService.checkAdmin(rwe);
			// might want to check admin on source and target site ?
			boolean transfer = true;
			// if the list exists, is this id in the list ?
			if (ids != null && ids.size() > 0)
			{
				transfer = false;
				for (Iterator j = ids.iterator(); j.hasNext() && !transfer;)
				{
					String id = (String) j.next();
					if (id.equals(rwo.getRwikiobjectid()))
					{
						transfer = true;
					}
				}
			}
			// ok to transfer
			if (transfer)
			{
				String pageName = rwo.getName();
				log.debug("================Transfering page " + pageName //$NON-NLS-1$
						+ " from " + rwo.getRealm() + " to " + toContext); //$NON-NLS-1$ //$NON-NLS-2$
				// relocate the page name
				pageName = NameHelper.localizeName(pageName, NameHelper
						.localizeSpace(pageName, rwo.getRealm()));
				pageName = NameHelper.globaliseName(pageName, toContext);
				try
				{
					// create a brand new page containing the content,
					// this does not copy prior versions
					RWikiCurrentObject transferPage = null;

					if (exists(pageName, toContext))
					{
						transferPage = getRWikiObject(pageName, toContext);
						update(pageName, toContext, transferPage.getVersion(),
								rwo.getContent(), rwo.getPermissions());
					}
					else
					{
						String user = sessionManager.getCurrentSessionUserId();
						String permissionsReference = wikiSecurityService
								.createPermissionsReference(toContext);

						if (!wikiSecurityService
								.checkCreatePermission(permissionsReference))
						{
							throw new CreatePermissionException("User: " + user //$NON-NLS-1$
									+ " cannot create pages in realm: " //$NON-NLS-1$
									+ pageName);
						}
						update(pageName, toContext, new Date(), rwo
								.getContent(), rwo.getPermissions());
					}

				}
				catch (Throwable t)
				{
					log.error("================Failed to import " + pageName //$NON-NLS-1$
							+ " from " + fromContext + " to " + toContext); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else
			{
				log.debug("=============Ignoring transfer of " + rwo.getName()); //$NON-NLS-1$
			}
		}

	}

	/**
	 * {@inheritDoc} The parsing process iterates though a list of regular
	 * expressions to generate a match
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (!reference.startsWith(REFERENCE_ROOT)) return false;
		// example reference: /wiki/site/c7bc194b-b215-4281-a1ac-8ed2ca2014e6/home.
		String[] parts = StringUtils.split(reference, Entity.SEPARATOR);
		String context = null;
		
		// the first part will be null, then next the service, and the fourth will be the (worksite) context
		if ( parts.length > 2 )
		{
			context = parts[2];
			if ( context.endsWith(".") )
			context = context.substring(0, context.length()-1 );
		}
		
		// Translate context alias into site id if necessary
		if ((context != null) && (context.length() > 0))
		{
			if (!siteService.siteExists(context))
			{
				try
				{
					String newContext = aliasService.getTarget(context);
					if (newContext.startsWith(SiteService.REFERENCE_ROOT)) // only support site aliases
					{
						reference = reference.replaceFirst(SiteService.REFERENCE_ROOT + Entity.SEPARATOR + context, newContext);
						ref.updateReference(reference);
					}
				}
				catch (Exception e)
				{
					log.debug(".parseEntityReference(): " + e.toString());
				}
			}
		}
		

		EntityHandler eh = findEntityReferenceMatch(reference);
		if (eh != null) {
			eh.setReference(APPLICATION_ID, ref, reference);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		checkReference(ref);
		EntityHandler eh = findEntityHandler(ref);

		Entity e = getEntity(ref, eh);
		return eh.getDescription(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		checkReference(ref);
		EntityHandler eh = findEntityHandler(ref);
		Entity e = getEntity(ref, eh);
		return eh.getProperties(e);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		checkReference(ref);
		EntityHandler eh = findEntityHandler(ref);
		return getEntity(ref, eh);
	}

	/**
	 * {@inheritDoc} The format of the URL is controlled by a MessageFormat
	 * String injected into urlFormat. The parameters are 0 = global Page Name
	 */
	public String getEntityUrl(Reference ref)
	{
		checkReference(ref);
		EntityHandler eh = findEntityHandler(ref);
		Entity entity = getEntity(ref, eh);
		return eh.getUrl(entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		checkReference(ref);
		EntityHandler eh = findEntityHandler(ref);
		return eh.getAuthzGroups(ref, userId);
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req,
					HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) 
					throws EntityPermissionException, 
					EntityNotDefinedException, 
					EntityAccessOverloadException, 
					EntityCopyrightException

					{

				// Check for session in request parameter
				String sessionId = req.getParameter("session");
				if (sessionId != null )
				{
					Session session = sessionManager.getSession( sessionId );
					if ( session != null )
						sessionManager.setCurrentSession( session );
				}
				
				try
				{
					checkReference(ref);
				}
				catch (Throwable t)
				{
					throw new EntityNotDefinedException(ref.getId());
				}
				try
				{

					EntityHandler eh = findEntityHandler(ref);
					Entity entity = getEntity(ref, eh);
					String user = req.getRemoteUser();
					if (entity instanceof RWikiEntity)
					{
						RWikiEntity rwe = (RWikiEntity) entity;
						if (!rwe.isContainer())
						{
							RWikiObject rwo = rwe.getRWikiObject();
							
							if (wikiSecurityService
									.checkRead(rwe))
							{
								String space = NameHelper.localizeSpace(rwo.getName(), rwo.getRealm());
								RWikiEntity sideBar = null;
								if (exists("view_right", space))
								{
									try
									{
										RWikiObject rwoSB = getRWikiObject("view_right", space);
										sideBar = (RWikiEntity) getEntity(rwoSB);
										if (!wikiSecurityService.checkRead(sideBar))
										{
											sideBar = null;
										}
									}
									catch (Exception ex)
									{
										sideBar = null;
									}
								}
                        
								if ( trackReads )
									eventTrackingService.post(
											eventTrackingService.newEvent(
																					RWikiObjectService.EVENT_RESOURCE_READ, ref.getReference(), false,
																					NotificationService.PREF_NONE));
								eh.outputContent(entity, sideBar, req, res);
							}
							else
							{
								throw new org.sakaiproject.exception.PermissionException(
										user, RWikiSecurityService.SECURE_READ,
										ref.getReference());
							}
						}
						else
						{
							// this is a container, read on the site
							if (wikiSecurityService.checkGetPermission(ref
									.getReference()))
							{
								String space = rwe.getReference();
								RWikiEntity sideBar = null;
								if (exists("view_right", space))
								{
									try
									{
										RWikiObject rwoSB = getRWikiObject("view_right", space);
										sideBar = (RWikiEntity) getEntity(rwoSB);
										if (!wikiSecurityService.checkRead(sideBar))
										{
											sideBar = null;
										}
									}
									catch (Exception ex)
									{
										sideBar = null;
									}
								}
                        
								if ( trackReads )
									eventTrackingService.post(
											eventTrackingService.newEvent(
																					RWikiObjectService.EVENT_RESOURCE_READ, ref.getReference(), false,
																					NotificationService.PREF_NONE));
								eh.outputContent(entity, sideBar, req, res);
							}
							else
							{
								throw new org.sakaiproject.exception.PermissionException(
										user, RWikiSecurityService.SECURE_READ,
										ref.getReference());
							}
						}
					}
					else
					{
						throw new EntityNotDefinedException(ref.getReference());
					}
				}
				catch (org.sakaiproject.exception.PermissionException p ) {
					throw new EntityPermissionException(p.getUser(),p.getLock(),p.getResource());
				}
				catch ( EntityNotDefinedException e) {
					throw e;
				}
				catch (Throwable t)
				{
					log.warn("Error getting wiki page via access :" //$NON-NLS-1$
							+ ref.getReference());
					log.debug("Stack trace was ", t); //$NON-NLS-1$
					throw new RuntimeException(ref.getReference(), t);
				}
			}
		};
	}

	/**
	 * see if the reference matches one of the regeistered regex patterns
	 * 
	 * @param reference
	 * @return the Entity handler that shoul be used to generate content and
	 *         format the URL
	 */
	private EntityHandler findEntityReferenceMatch(String reference)
	{
		if (!reference.startsWith(REFERENCE_ROOT)) return null;
		for (Iterator i = m_handlers.keySet().iterator(); i.hasNext();)
		{
			String s = (String) i.next();
			EntityHandler eh = (EntityHandler) m_handlers.get(s);
			if (eh.matches(reference)) return eh;
		}
		return null;
	}

	private void checkReference(Reference ref)
	{
		if (!APPLICATION_ID.equals(ref.getType()))
			throw new RuntimeException(
					"Wiki page does not exist, sorry "); //$NON-NLS-1$

	}

	/**
	 * Looks up the entity handler based on sybtype, the registerd subtype must
	 * match the key in the m_handlers map
	 * 
	 * @param ref
	 * @return
	 */
	private EntityHandler findEntityHandler(Reference ref)
	{
		if (!APPLICATION_ID.equals(ref.getType())) return null;
		String subtype = ref.getSubType();
		return (EntityHandler) m_handlers.get(subtype);
	}

	/**
	 * Get the entity, already having looked up the entity handler
	 * 
	 * @param eh
	 * @return
	 */
	private Entity getEntity(Reference ref, EntityHandler eh)
	{

		RWikiObject rwo = this.getRWikiCurrentObjectDao().findByGlobalName(
				ref.getId());

		int revision = eh.getRevision(ref);
		if (rwo != null && revision != -1
				&& revision != rwo.getRevision().intValue())
		{
			RWikiObject hrwo = this.getRWikiHistoryObjectDao()
					.getRWikiHistoryObject(rwo, revision);
			if (hrwo != null)
			{
				rwo = hrwo;
			}

		}
		RWikiEntity rwe = null;
		if (rwo == null)
		{
			rwe = (RWikiEntity) getReferenceEntity(ref);
		}
		else
		{
			rwe = (RWikiEntity) getEntity(rwo);
		}
		return rwe;
	}

	public Entity getReferenceEntity(Reference ref)
	{
		return new RWikiEntityImpl(ref);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param rwo
	 * @return
	 */
	public Entity getEntity(RWikiObject rwo)
	{
		return new RWikiEntityImpl(rwo);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param rwo
	 * @return
	 */
	public Reference getReference(RWikiObject rwo)
	{
		return entityManager.newReference(getEntity(rwo).getReference());
	}

	/**
	 * @return Returns the handlers.
	 */
	public Map getHandlers()
	{
		return m_handlers;
	}

	/**
	 * @param m_handlers
	 *        The handlers to set.
	 */
	public void setHandlers(Map m_handlers)
	{
		this.m_handlers = m_handlers;
	}

	/**
	 * Disable the tool from the site
	 * 
	 * @param context
	 */
	private void disableWiki(String context)
	{

		// ? we are not going to delete the content, so do nothing 
	}

	/**
	 * Enable the tool in the site
	 * 
	 * @param context
	 */
	private void enableWiki(String context)
	{
		// we could perform pre-populate at this stage

	}

	/**
	 * {@inheritDoc}
	 */
	public List findAllChangedSince(Date time, String basepath)
	{
		// TODO: Put authz in place
		return cdao.findAllChangedSince(time, basepath);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkRead(RWikiObject rwo)
	{
		return wikiSecurityService.checkRead((RWikiEntity) getEntity(rwo));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkUpdate(RWikiObject rwo)
	{
		return wikiSecurityService.checkUpdate((RWikiEntity) getEntity(rwo));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkCreate(RWikiObject rwo)
	{
		return wikiSecurityService.checkCreate((RWikiEntity) getEntity(rwo));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean checkAdmin(RWikiObject rwo)
	{
		return wikiSecurityService.checkAdmin((RWikiEntity) getEntity(rwo));
	}

	/**
	 * {@inheritDoc}
	 */
	public List findAllPageNames()
	{
		return cdao.findAllPageNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public String createReference(String pageName)
	{
		return RWikiObjectService.REFERENCE_ROOT + pageName + "."; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public PageLinkRenderer getComponentPageLinkRender(String pageSpace, boolean withBreadCrumb)
	{
		return new ComponentPageLinkRenderImpl(pageSpace,withBreadCrumb);
	}
	/**
	 * @return the aliasService
	 */
	public AliasService getAliasService()
	{
		return aliasService;
	}
	/**
	 * @param aliasService the aliasService to set
	 */
	public void setAliasService(AliasService aliasService)
	{
		this.aliasService = aliasService;
	}
	
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		try
		{
			if(cleanup == true)
			{
				//TODO
			}
		}
		catch (Exception e)
		{
			log.info("Rwiki transferCopyEntities Error" + e);
		}
		transferCopyEntities(fromContext, toContext, ids);
	}
}
