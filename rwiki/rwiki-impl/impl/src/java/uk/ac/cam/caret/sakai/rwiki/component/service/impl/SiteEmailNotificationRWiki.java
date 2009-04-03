/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.Web;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RenderService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.utils.DigestHtml;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * <p>
 * SiteEmailNotificationRWiki fills the notification message and headers with
 * details from the content change that triggered the notification event.
 * </p>
 * 
 * @author Sakai Software Development Team
 * @author ieb
 */
public class SiteEmailNotificationRWiki extends SiteEmailNotification {
	public class MessageContent {

		public String title;
		public String user;
		public String content;
		public String moddate;
		public String localName;
		public String url;
		public String contentHTML;

	}

	private static Log log = LogFactory
			.getLog(SiteEmailNotificationRWiki.class);

	private RenderService renderService = null;

	private RWikiObjectService rwikiObjectService = null;

	private PreferenceService preferenceService = null;

	private SiteService siteService;

	private SecurityService securityService;

	private EntityManager entityManager;

	private ThreadLocalManager threadLocalManager;

	private TimeService timeService;

	private DigestService digestService;

	private UserDirectoryService userDirectoryService;


	/**
	 * Construct.
	 */
	public SiteEmailNotificationRWiki(RWikiObjectService rwikiObjectService,
			RenderService renderService, PreferenceService preferenceService,
			SiteService siteService, SecurityService securityService,
			EntityManager entityManager, ThreadLocalManager threadLocalManager,
			TimeService timeService, DigestService digestService, UserDirectoryService userDirectoryService) {
		this.renderService = renderService;
		this.rwikiObjectService = rwikiObjectService;
		this.preferenceService = preferenceService;
		this.siteService = siteService;
		this.securityService = securityService;
		this.entityManager = entityManager;
		this.threadLocalManager = threadLocalManager;
		this.timeService = timeService;
		this.digestService = digestService;
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationRWiki(RWikiObjectService rwikiObjectService,
			RenderService renderService, PreferenceService preferenceService,
			SiteService siteService, SecurityService securityService,
			EntityManager entityManager, ThreadLocalManager threadLocalManager,
			TimeService timeService, DigestService digestService, UserDirectoryService userDirectoryService, String siteId) {
		super(siteId);
		this.renderService = renderService;
		this.rwikiObjectService = rwikiObjectService;
		this.preferenceService = preferenceService;
		this.siteService = siteService;
		this.securityService = securityService;
		this.entityManager = entityManager;
		this.threadLocalManager = threadLocalManager;
		this.timeService = timeService;
		this.digestService = digestService;
		this.userDirectoryService = userDirectoryService;

	}

	/**
	 * @inheritDoc
	 */
	public NotificationAction getClone() {
		SiteEmailNotificationRWiki clone = new SiteEmailNotificationRWiki(
				rwikiObjectService, renderService, preferenceService,
				siteService, securityService, entityManager,
				threadLocalManager, timeService, digestService, userDirectoryService);
		clone.set(this);

		return clone;
	}

	protected String getSiteId(String context) {
		if (context.startsWith("/site/")) //$NON-NLS-1$
		{
			context = context.substring("/site/".length()); //$NON-NLS-1$
		}
		int il = context.indexOf("/"); //$NON-NLS-1$
		if (il != -1) {
			context = context.substring(0, il);
		}
		return context;
	}

	/**
	 * @inheritDoc
	 */
	protected String getPlainMessage(Event event) {

		MessageContent mc = getMessageContent(event);
		
		String message = Messages.getString("SiteEmailNotificationRWiki.5") + mc.title + Messages.getString("SiteEmailNotificationRWiki.6") //$NON-NLS-1$ //$NON-NLS-2$
				+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ")  " + " \n" + " \n" + Messages.getString("SiteEmailNotificationRWiki.13") + mc.title + "\" > Wiki  > " + mc.localName + "\n" + Messages.getString("SiteEmailNotificationRWiki.16") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				+ mc.moddate
				+ "\n" + Messages.getString("SiteEmailNotificationRWiki.18") + mc.user + "\n" + " \n" + " 	Page: " + mc.localName + " " + mc.url + " \n" + " \n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				+ Messages.getString("SiteEmailNotificationRWiki.4") + mc.content + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

		log.debug("Message is " + message);

		return message;
	}

	
	protected String getHtmlMessage(Event event) {

		MessageContent mc = getMessageContent(event);
		String message = 
			    Messages.getString("SiteEmailNotificationRWiki.5") + mc.title + Messages.getString("SiteEmailNotificationRWiki.6") //$NON-NLS-1$ //$NON-NLS-2$
				+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ ")  " + " \n" + " \n" + Messages.getString("SiteEmailNotificationRWiki.13") + mc.title + "\" > Wiki  > " + mc.localName + "\n" + Messages.getString("SiteEmailNotificationRWiki.16") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				+ mc.moddate
				+ "\n" + Messages.getString("SiteEmailNotificationRWiki.18") + mc.user + "\n" + " \n" + " 	Page: " + mc.localName + " " + mc.url + " \n" + " \n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				+ Messages.getString("SiteEmailNotificationRWiki.4");
		message = Web.escapeHtml(message, true) + mc.contentHTML;
		log.debug("Message is " + message);
		return message;
	}

	private MessageContent getMessageContent(Event event) {
		MessageContent messageContent = new MessageContent();
		// get the content & properties
		Reference ref = entityManager.newReference(event.getResource());
		ResourceProperties props = ref.getProperties();

		// get the function
		// String function = event.getEvent();

		// use either the configured site, or if not configured, the site
		// (context) of the resource
		String siteId = (getSite() != null) ? getSite() : getSiteId(ref
				.getContext());
		try {
			Site site = siteService.getSite(siteId);
			messageContent.title = site.getTitle();
		} catch (IdUnusedException e) {
			messageContent.title = siteId;
		}

		// get the URL and resource name.
		// StringBuffer buf = new StringBuffer();
		messageContent.url = ref.getUrl() + "html"; //$NON-NLS-1$
		
		String pageName = props.getProperty(RWikiEntity.RP_NAME);
		String realm = props.getProperty(RWikiEntity.RP_REALM);
		messageContent.localName = NameHelper.localizeName(pageName, realm);
		String userId = props.getProperty(RWikiEntity.RP_USER);
		try {
			User u = userDirectoryService.getUser(userId);
			messageContent.user = u.getDisplayId();
		} catch (UserNotDefinedException e) {
			messageContent.user = userId;
		}
		messageContent.moddate = new Date(Long.parseLong(props
				.getProperty(RWikiEntity.RP_VERSION))).toString();
		try {
			RWikiEntity rwe = (RWikiEntity) rwikiObjectService.getEntity(ref);
			RWikiObject rwikiObject = rwe.getRWikiObject();

			String pageSpace = NameHelper.localizeSpace(pageName, realm);
			ComponentPageLinkRenderImpl cplr = new ComponentPageLinkRenderImpl(
					pageSpace, true);
			messageContent.contentHTML = renderService.renderPage(rwikiObject, pageSpace, cplr);
			messageContent.content = DigestHtml.digest(messageContent.contentHTML);

		} catch (Exception ex) {

		}
		return messageContent;
	}

	@Override
	protected String plainTextContent(Event event) {
		return getPlainMessage(event);
	}

	@Override
	protected String htmlContent(Event event) {
		return getHtmlMessage(event);
	}

	/**
	 * @inheritDoc
	 */
	protected List getHeaders(Event e) {
		List rv = super.getHeaders(e);
		Reference ref = entityManager.newReference(e.getResource());
		ResourceProperties props = ref.getProperties();

		String pageName = props.getProperty(RWikiEntity.RP_NAME);
		String realm = props.getProperty(RWikiEntity.RP_REALM);
		String localName = NameHelper.localizeName(pageName, realm);

		String subjectHeader = Messages
				.getString("SiteEmailNotificationRWiki.27") + localName + Messages.getString("SiteEmailNotificationRWiki.28"); //$NON-NLS-1$ //$NON-NLS-2$
		// the Subject
		rv.add(subjectHeader);

		// from
		rv.add(getFrom(e));

		// to
		rv.add(getTo(e));

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newline, String title) {
		// tag the message
		String rv = "----------------------\n" + Messages.getString("SiteEmailNotificationRWiki.30") //$NON-NLS-1$ //$NON-NLS-2$
				+ ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ Messages.getString("SiteEmailNotificationRWiki.3") + title + Messages.getString("SiteEmailNotificationRWiki.35") //$NON-NLS-1$ //$NON-NLS-2$
				+ Messages.getString("SiteEmailNotificationRWiki.36"); //$NON-NLS-1$
		/*
		 * String rv = newline + "------" + newline + rb.getString("this") + " " +
		 * ServerConfigurationService.getString("ui.service", "Sakai") + " (" +
		 * ServerConfigurationService.getPortalUrl() + ") " +
		 * rb.getString("forthe") + " " + title + " " + rb.getString("site") +
		 * newline + rb.getString("youcan") + newline;
		 */
		return rv;
	}

	protected int getOption(User user, Notification notification, Event event) {

		// FIXME I don't think this should be here, but it certainly shouldn't
		// be in preferenceService
		// We really want a entity reference to page name, without going via the
		// db!
		String resourceReference = event.getResource();

		if (resourceReference == null
				|| !resourceReference
						.startsWith(RWikiObjectService.REFERENCE_ROOT)
				|| resourceReference.length() == RWikiObjectService.REFERENCE_ROOT
						.length()) {
			return NotificationService.PREF_IGNORE;
		}

		resourceReference = resourceReference.substring(
				RWikiObjectService.REFERENCE_ROOT.length(), resourceReference
						.lastIndexOf('.'));

		String preference = preferenceService.findPreferenceAt(user.getId(),
				resourceReference, PreferenceService.MAIL_NOTIFCIATION);

		if (preference == null || "".equals(preference)) //$NON-NLS-1$
		{
			return NotificationService.PREF_IGNORE;
		}

		if (PreferenceService.NONE_PREFERENCE.equals(preference)) {
			return NotificationService.PREF_IGNORE;
		}

		if (PreferenceService.DIGEST_PREFERENCE.equals(preference)) {
			return NotificationService.PREF_DIGEST;
		}

		if (PreferenceService.SEPARATE_PREFERENCE.equals(preference)) {
			return NotificationService.PREF_IMMEDIATE;
		}

		return NotificationService.PREF_IGNORE;
	}

	/**
	 * @inheritDoc
	 */
	protected List getRecipients(Event event) {
		// get the resource reference
		Reference ref = entityManager.newReference(event.getResource());

		// use either the configured site, or if not configured, the site
		// (context) of the resource
		String siteId = getSite();

		if (siteId == null) {
			siteId = getSiteId(ref.getContext());
		}

		// if the site is published, use the list of users who can SITE_VISIT
		// the site,
		// else use the list of users who can SITE_VISIT_UNP the site.
		try {
			Site site = siteService.getSite(siteId);
			String ability = SiteService.SITE_VISIT;
			if (!site.isPublished()) {
				ability = SiteService.SITE_VISIT_UNPUBLISHED;
			}

			// get the list of users who can do the right kind of visit
			List users = securityService.unlockUsers(ability, ref
					.getReference());

			// get the list of users who have the appropriate access to the
			// resource
			if (getResourceAbility() != null) {
				List users2 = securityService.unlockUsers(getResourceAbility(),
						ref.getReference());

				// find intersection of users and user2
				users.retainAll(users2);
			}

			// add any other users
			addSpecialRecipients(users, ref);

			return users;
		} catch (Exception any) {
			log.error("Exception in getRecipients()", any); //$NON-NLS-1$
			return new Vector();
		}
	}

}
