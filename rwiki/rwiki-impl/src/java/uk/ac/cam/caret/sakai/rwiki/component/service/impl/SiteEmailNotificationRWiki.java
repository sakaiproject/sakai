/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation,
 *                  University of Cambridge
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.current.cover.CurrentService;
import org.sakaiproject.service.framework.email.cover.EmailService;
import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.digest.DigestMessage;
import org.sakaiproject.service.legacy.digest.cover.DigestService;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.entity.ResourceProperties;
import org.sakaiproject.service.legacy.event.Event;
import org.sakaiproject.service.legacy.notification.Notification;
import org.sakaiproject.service.legacy.notification.NotificationAction;
import org.sakaiproject.service.legacy.notification.cover.NotificationService;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.service.legacy.time.cover.TimeService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.util.notification.SiteEmailNotification;

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
	private Logger logger = org.sakaiproject.service.framework.log.cover.Logger
			.getInstance();

	private RenderService renderService = null;

	private RWikiObjectService rwikiObjectService = null;

	private PreferenceService preferenceService = null;
	
	/**
	 * Construct.
	 */
	public SiteEmailNotificationRWiki(RWikiObjectService rwikiObjectService,
			RenderService renderService, PreferenceService preferenceService) {
		this.renderService = renderService;
		this.rwikiObjectService = rwikiObjectService;
		this.preferenceService = preferenceService;
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationRWiki(RWikiObjectService rwikiObjectService,
			RenderService renderService, PreferenceService preferenceService, String siteId) {
		super(siteId);
		this.renderService = renderService;
		this.rwikiObjectService = rwikiObjectService;
		this.preferenceService = preferenceService;
	}

	/**
	 * @inheritDoc
	 */
	protected List getRecipients(Event event) {
		// get the resource reference
		Reference ref = EntityManager.newReference(event.getResource());

		// use either the configured site, or if not configured, the site
		// (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		String sitestart = Entity.SEPARATOR + "site" + Entity.SEPARATOR;
		if (siteId.startsWith(sitestart)) {
			siteId = siteId.substring(sitestart.length());
			int nextslash = siteId.indexOf(Entity.SEPARATOR);
			if (nextslash != -1) {
				siteId = siteId.substring(0, nextslash);
			}
		}

		logger.info("Finding recipients for " + siteId);
		// if the site is published, use the list of users who can SITE_VISIT
		// the site,
		// else use the list of users who can SITE_VISIT_UNP the site.
		try {
			Site site = SiteService.getSite(siteId);
			String ability = SiteService.SITE_VISIT;
			if (!site.isPublished()) {
				ability = SiteService.SITE_VISIT_UNPUBLISHED;
			}

			// get the list of users
			List users = SecurityService.unlockUsers(ability, SiteService
					.siteReference(siteId));
			logger.info("Got " + users.size() + " for site " + siteId);

			// get the list of users who have the appropriate access to the
			// resource
			if (getResourceAbility() != null) {
				List users2 = SecurityService.unlockUsers(getResourceAbility(),
						ref.getReference());

				// find intersection of users and user2
				users.retainAll(users2);
			}

			logger.info("Got " + users.size() + " for site " + siteId
					+ " after filter");

			return users;
		} catch (Exception any) {
			logger.info(" Failed to find list of recipients ", any);
			return new Vector();
		}
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility() {
		return null;
		// return RWikiSecurityService.SECURE_READ;
	}

	/**
	 * @inheritDoc
	 */
	public NotificationAction getClone() {
		SiteEmailNotificationRWiki clone = new SiteEmailNotificationRWiki(
				rwikiObjectService, renderService, preferenceService);
		clone.set(this);

		return clone;
	}

	public String getSiteId(String context) {
		if (context.startsWith("/site/")) {
			context = context.substring("/site/".length());
		}
		int il = context.indexOf("/");
		if (il != -1) {
			context = context.substring(0, il);
		}
		return context;
	}

	/**
	 * @inheritDoc
	 */
	protected String getMessage(Event event) {
		// get the content & properties
		Reference ref = EntityManager.newReference(event.getResource());
		ResourceProperties props = ref.getProperties();

		// get the function
		//String function = event.getEvent();

		// use either the configured site, or if not configured, the site
		// (context) of the resource
		String siteId = (getSite() != null) ? getSite() : getSiteId(ref
				.getContext());

		// get a site title
		String title = siteId;
		try {
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		} catch (Exception ignore) {
		}

		// get the URL and resource name.
		//StringBuffer buf = new StringBuffer();
		String url = ServerConfigurationService.getAccessUrl() + ref.getUrl() + "html";

		String pageName = props.getProperty(RWikiEntity.RP_NAME);
		String realm = props.getProperty(RWikiEntity.RP_REALM);
		String localName = NameHelper.localizeName(pageName, realm);
		String user = props.getProperty(RWikiEntity.RP_USER);
		String moddate = new Date(Long.parseLong(props
				.getProperty(RWikiEntity.RP_VERSION))).toString();
		String content = "";
		try {
			RWikiEntity rwe = (RWikiEntity) rwikiObjectService.getEntity(ref);
			RWikiObject rwikiObject = rwe.getRWikiObject();
			
			String pageSpace = NameHelper.localizeSpace(pageName, realm);
			ComponentPageLinkRenderImpl cplr = new ComponentPageLinkRenderImpl(
					pageSpace);
			content = renderService.renderPage(rwikiObject, pageSpace,
					cplr);
			content = DigestHtml.digest(content);
			if ( content.length() > 1000 ) {
				content = content.substring(0,1000);
			}
			
		} catch (Exception ex) {

		}

		String message = "A Wiki Page has been changed in the \"" + title
				+ "\" site at "
				+ ServerConfigurationService.getString("ui.service", "Sakai")
				+ " (" + ServerConfigurationService.getPortalUrl() + ")  "
				+ " \n" + " \n" + "	Location: site \"" + title
				+ "\" > Wiki  > " + localName + "\n" + " Modified at: "
				+ moddate + "\n" + " Modified by User: " + user + "\n" + " \n"
				+ " 	Page: " + localName + " " + url + " \n" + " \n"
				+ " Content: \n" + content + "\n";

		return message;
	}

	/**
	 * @inheritDoc
	 */
	protected List getHeaders(Event e) {
		List rv = new ArrayList(1);
		Reference ref = EntityManager.newReference(e.getResource());
		ResourceProperties props = ref.getProperties();

		String pageName = props.getProperty(RWikiEntity.RP_NAME);
		String realm = props.getProperty(RWikiEntity.RP_REALM);
		String localName = NameHelper.localizeName(pageName, realm);

		String subjectHeader = "Subject: The wiki page " + localName
				+ " has been modified";
		// the Subject
		rv.add(subjectHeader);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newline, String title) {
		// tag the message
		String rv = "----------------------\n"
				+ "This automatic notification message was sent by "
				+ ServerConfigurationService.getString("ui.service", "Sakai")
				+ " ("
				+ ServerConfigurationService.getPortalUrl()
				+ ") from the "
				+ title
				+ " site.\n"
				+ "You can modify how you receive notifications at My Workspace > Preferences.";
		/*
		 * String rv = newline + "------" + newline + rb.getString("this") + " " +
		 * ServerConfigurationService.getString("ui.service", "Sakai") + " (" +
		 * ServerConfigurationService.getPortalUrl() + ") " +
		 * rb.getString("forthe") + " " + title + " " + rb.getString("site") +
		 * newline + rb.getString("youcan") + newline;
		 */
		return rv;
	}

	protected Thread senderThread = null;

	protected Vector sendList = new Vector();

	public class EmailMessage {
		private String from = null;

		private String email = null;

		private String subject = null;

		private String message = null;

		private String sendTo = null;

		private List headers = null;

		private String replyTo = null;

		public EmailMessage(String from, String email, String subject,
				String message, String sendTo, String replyTo, List headers) {
			this.from = from;
			this.email = email;
			this.subject = subject;
			this.message = message;
			this.sendTo = sendTo;
			this.headers = headers;
			this.replyTo = replyTo;
		}

		public void send() {
			EmailService.send(from, email, subject, message, sendTo, replyTo,
					headers);
		}

		public String toString() {
			String newline = "\n";
			StringBuffer sb = new StringBuffer();
			sb.append("From:").append(from).append(newline);
			sb.append("To:").append(sendTo).append(newline);
			sb.append("Reply To:").append(replyTo).append(newline);
			sb.append("Subject:").append(subject).append(newline);
			sb.append("Send To:").append(sendTo).append(newline);
			for (Iterator i = headers.iterator(); i.hasNext();) {
				sb.append(i.next()).append(newline);
			}
			sb.append(newline).append(message).append(newline);
			return sb.toString();
		}
	}

	/**
	 * This class represents a one shot sender, that will keep runing as long as
	 * there are messages waiting in the queue.
	 * 
	 * @author ieb
	 * 
	 */
	public class Sender implements Runnable {

		public void run() {
			while (sendList.size() > 0) {
				List l = (List) sendList.clone();
				for (Iterator i = l.iterator(); i.hasNext();) {
					EmailMessage em = null;
					try {
						em = (EmailMessage) i.next();
						em.send();
						sendList.remove(em);
					} catch (Exception ex) {
						if (em != null) {
							try {
								sendList.remove(em);
							} catch (Exception ex2) {
							}
						}
					}

				}
			}
			senderThread = null;
			logger.debug(" Sender Thread Death");
		}

	}

	/**
	 * Do the notification.
	 * 
	 * @param notification
	 *            The notification responding to the event.
	 * @param event
	 *            The event that matched criteria to cause the notification.
	 */
	public void notify(Notification notification, Event event) {
		// ignore events marked for no notification
		if (event.getPriority() == NotificationService.NOTI_NONE)
			return;

		if ( CurrentService.getInThread(RWikiObjectService.SMALL_CHANGE_IN_THREAD) != null ) 
			return;
			
		// get the list of potential recipients
		List recipients = getRecipients(event);

		// filter to actual immediate recipients
		List immediate = immediateRecipients(recipients, notification, event);

		// and the list of digest recipients
		List digest = digestRecipients(recipients, notification, event);

		// we may be done
		if ((immediate.size() == 0) && (digest.size() == 0))
			return;

		// get the email elements
		String message = getMessage(event);
		List headers = getHeaders(event);

		// for From:, use the From: in the headers, else use no-reply@server
		String from = findHeaderValue("From", headers);
		if (from == null) {
			from = "\""
					+ ServerConfigurationService.getString("ui.service",
							"Sakai") + "\"<no-reply@"
					+ ServerConfigurationService.getServerName() + ">";
		}

		// message body details
		boolean isBodyHTML = isBodyHTML(event);
		String newline = (isBodyHTML) ? "<br />\n" : "\n";

		// header to the individual recipient?
		boolean toRecipient = headerToRecipient();

		// for the immediates
		if (immediate.size() > 0) {
			// get a site title
			// use either the configured site, or if not configured, the site
			// (context) of the resource
			Reference ref = EntityManager.newReference(event.getResource());
			//Entity r = ref.getEntity();
			String title = (getSite() != null) ? getSite() : getSiteId(ref
					.getContext());

			org.sakaiproject.service.legacy.site.SiteService siteService = org.sakaiproject.service.legacy.site.cover.SiteService
					.getInstance();
			try {
				org.sakaiproject.service.legacy.site.Site site = siteService
						.getSite(title);
				title = site.getTitle();
			} catch (Exception ignore) {
			}

			// tag the message
			String messageForImmediates = message + getTag(newline, title);

			// send to each immediate - one at a time
			// NOTE: sending to them all at once caused problems - some SMTP
			// servers have a to: limit which we exceeded,
			// and if there's a bad email address in there it could cancel the
			// entire mailing. -ggolden
			for (Iterator ii = immediate.iterator(); ii.hasNext();) {
				User user = (User) ii.next();
				String email = user.getEmail();
				if ((email != null) && (email.length() > 0)) {
					EmailMessage em = new EmailMessage(from, email,
							findHeaderValue("Subject", headers),
							messageForImmediates, (toRecipient ? email : null),
							null, headers);
					logger.debug("Sending " + em);
					sendList.add(em);
				}
			}
			if (senderThread == null) {
				senderThread = new Thread(new Sender());
				senderThread.start();
			}
		}

		// for the digesters
		if (digest.size() > 0) {
			// modify the message to add missing parts (no tag - this is added
			// at the end of the digest)
			// date, subject, to, all may be in the additionalHeaders
			String messageForDigest = "From: " + from + "\n";
			String item = findHeader("Date", headers);
			if (item != null) {
				messageForDigest += item;
			} else {
				messageForDigest += "Date: "
						+ TimeService.newTime().toStringLocalFullZ() + "\n";
			}

			item = findHeader("To", headers);
			if (item != null)
				messageForDigest += item;

			item = findHeader("Cc", headers);
			if (item != null)
				messageForDigest += item;

			item = findHeader("Subject", headers);
			if (item != null)
				messageForDigest += item;

			messageForDigest += "\n" + message;

			for (Iterator iDigests = digest.iterator(); iDigests.hasNext();) {
				User user = (User) iDigests.next();

				// digest the message
				DigestMessage msg = new DigestMessage(user.getId(),
						findHeaderValue("Subject", headers), messageForDigest);
				DigestService.digest(msg);
			}
		}
	}

	protected int getOption(User user, Notification notification, Event event) {
		
		// FIXME I don't think this should be here, but it certainly shouldn't be in preferenceService
		// We really want a entity reference to page name, without going via the db!
		String resourceReference = event.getResource();
								
		if (resourceReference == null 
				|| !resourceReference.startsWith(RWikiObjectService.REFERENCE_ROOT) 
				|| resourceReference.length() == RWikiObjectService.REFERENCE_ROOT.length()) { 
			return NotificationService.PREF_IMMEDIATE;
		}
		
		resourceReference = resourceReference.substring(RWikiObjectService.REFERENCE_ROOT.length(), resourceReference.lastIndexOf('.'));
		
		String preference = preferenceService.findPreferenceAt(user.getId(), resourceReference, PreferenceService.MAIL_NOTIFCIATION );
		
		if (preference == null || "".equals(preference)) {
			return NotificationService.PREF_IMMEDIATE;
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
		
		return NotificationService.PREF_IMMEDIATE;
	}
	

}
