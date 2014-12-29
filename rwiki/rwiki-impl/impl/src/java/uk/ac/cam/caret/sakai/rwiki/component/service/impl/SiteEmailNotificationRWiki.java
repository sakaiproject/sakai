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

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.email.api.DigestService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SiteEmailNotification;

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

	private FormattedText formattedText = (FormattedText) ComponentManager.get(FormattedText.class);

	private ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class);

	private static ResourceLoader rl = new ResourceLoader("uk.ac.cam.caret.sakai.rwiki.component.bundle.Messages");

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
		StringBuilder message = new StringBuilder();
		message.append(rl.getString("SiteEmailNotificationRWiki.5"))
		    .append(mc.title)
		    .append(rl.getString("SiteEmailNotificationRWiki.6"))
			.append(ServerConfigurationService.getString("ui.service", "Sakai"))
			.append(" (")
			.append(ServerConfigurationService.getPortalUrl())
			.append(")  \n\n")
			.append(rl.getString("SiteEmailNotificationRWiki.13"))
			.append(mc.title)
			.append("\" > ")
			.append(getWikiToolPageName(event))
			.append("  > ")
			.append(mc.localName)
			.append("\n")
			.append(rl.getString("SiteEmailNotificationRWiki.16"))
			.append(mc.moddate)
			.append("\n")
			.append(rl.getString("SiteEmailNotificationRWiki.18"))
			.append(mc.user)
			.append("\n \n")
			.append(rl.getString("SiteEmailNotificationRWiki.19"))
			.append(mc.localName)
			.append(" ")
			.append(mc.url)
			.append(" \n \n")
			.append(rl.getString("SiteEmailNotificationRWiki.4"))
			.append(mc.content)
			.append("\n"); 
		log.debug("Message is " + message);
		return message.toString();
	}

	protected String getHtmlMessage(Event event) {
		MessageContent mc = getMessageContent(event);
		StringBuilder sb = new StringBuilder();
		sb.append(rl.getString("SiteEmailNotificationRWiki.5"))
			.append(mc.title)
			.append(rl.getString("SiteEmailNotificationRWiki.6"))
			.append(ServerConfigurationService.getString("ui.service", "Sakai"))
			.append(" (")
			.append(ServerConfigurationService.getPortalUrl()) 
			.append(")  \n\n")
			.append(rl.getString("SiteEmailNotificationRWiki.13"))
			.append(mc.title)
			.append("\" > ")
			.append(getWikiToolPageName(event))
			.append("  > ")
			.append(mc.localName)
			.append("\n")
			.append(rl.getString("SiteEmailNotificationRWiki.16"))
			.append(mc.moddate)
			.append("\n")
			.append(rl.getString("SiteEmailNotificationRWiki.18"))
			.append(mc.user)
			.append("\n \n")
			.append(rl.getString("SiteEmailNotificationRWiki.19"))
			.append(mc.localName)
			.append(" ")
			.append(mc.url)
			.append(" \n \n")
			.append(rl.getString("SiteEmailNotificationRWiki.4"))
			.append(mc.content)
			.append("\n"); 
		String message = formattedText.escapeHtml(sb.toString(), true);
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
		Date date = new Date(Long.parseLong(props.getProperty(RWikiEntity.RP_VERSION)));
		messageContent.moddate = DateFormat.getDateInstance(DateFormat.FULL, rl.getLocale()).format(date);
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
	protected List<String> getHeaders(Event e) {
		List<String> rv = super.getHeaders(e);
		Reference ref = entityManager.newReference(e.getResource());
		ResourceProperties props = ref.getProperties();

		String pageName = props.getProperty(RWikiEntity.RP_NAME);
		String realm = props.getProperty(RWikiEntity.RP_REALM);
		String localName = NameHelper.localizeName(pageName, realm);

		StringBuilder subjectHeader = new StringBuilder();
		// This string "Subject: " should not be translated is processed by smtp 
		subjectHeader.append("Subject: ")
			.append(rl.getString("SiteEmailNotificationRWiki.27"))
			.append(localName)
			.append(rl.getString("SiteEmailNotificationRWiki.28"));
		
		// the Subject
		rv.add(subjectHeader.toString());

		// from
		rv.add(getFrom(e));

		// to
		rv.add(getTo(e));

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String title, boolean shouldUseHtml) {
		// tag the message
		StringBuilder rv = new StringBuilder();
		if (shouldUseHtml) {
			rv.append("<hr/><br/>")
				.append(rl.getString("SiteEmailNotificationRWiki.30"))
				.append(ServerConfigurationService.getString("ui.service", "Sakai"))
				.append(" (<a href=\"")
				.append(ServerConfigurationService.getPortalUrl())
				.append("\">")
				.append(ServerConfigurationService.getPortalUrl())
				.append("</a>")
				.append(rl.getString("SiteEmailNotificationRWiki.3"))
				.append(rl.getString("SiteEmailNotificationRWiki.35"))
				.append(rl.getString("SiteEmailNotificationRWiki.36"))
				.append("<br/>");
		} else {
			rv.append("----------------------\n")
				.append(rl.getString("SiteEmailNotificationRWiki.30"))
				.append(ServerConfigurationService.getString("ui.service", "Sakai"))
				.append(" (")
				.append(ServerConfigurationService.getPortalUrl())
				.append(rl.getString("SiteEmailNotificationRWiki.3"))
				.append(rl.getString("SiteEmailNotificationRWiki.35"))
				.append(rl.getString("SiteEmailNotificationRWiki.36"));
		}
		return rv.toString();
	}

	protected int getOption(User user, String notificationId, String resourceFilter, int eventPriority, Event event) {

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

	/**
	 * Get the wiki tool's page name
	 * 
	 * @param event Event the event
	 * @return the name of the wiki tool's page
	 */
	private String getWikiToolPageName(Event event) {
		String toolName = "Wiki";
		Tool tool = toolManager.getCurrentTool();
		if (tool != null) {
			toolName = tool.getTitle();
			String toolId = tool.getId(); // sakai.rwiki
			// get the site id
			String siteId = getSite();
			if (StringUtils.isEmpty(siteId)) {
				Reference ref = entityManager.newReference(event.getResource());
				siteId = getSiteId(ref.getContext());
			}
			if (StringUtils.isNotEmpty(siteId)) {
				try {
					Site site = siteService.getSite(siteId);
					List<SitePage> pages = site.getPages();
					for (SitePage p : pages) {
						Collection<ToolConfiguration> toolConfigurations = p.getTools(new String[] {toolId});
						// if tool exists in this page, get the page title
						if (CollectionUtils.isNotEmpty(toolConfigurations)) {
							toolName = p.getTitle();
							break;
						}
					}
				} catch (IdUnusedException e) {
					log.error("Site not found while getting wiki name", e);
				}
			}
		}
		return toolName;
	}
}
