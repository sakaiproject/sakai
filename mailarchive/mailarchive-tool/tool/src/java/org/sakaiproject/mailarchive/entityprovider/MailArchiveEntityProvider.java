/**********************************************************************************
 * $URL:  $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.mailarchive.entityprovider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.api.MailArchiveMessageHeader;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.MergedList;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * Allows some basic functions on email archive. Due to limitations of
 * EntityBroker the internal URLs of the mailArchiveMessages service can't be
 * exposed directly, so we have to map them, with assumptions about characters
 * used in IDs. Basically we pack together the
 * {siteId}:{channelId}:{mailArchiveMessageId} into the ID.
 *
 */
@Slf4j
public class MailArchiveEntityProvider extends AbstractEntityProvider implements
		EntityProvider, Outputable, Inputable, Describeable, ActionsExecutable {

	public final static String ENTITY_PREFIX = "mailarchive";

	private static ResourceLoader rb = new ResourceLoader("email");
	public static int DEFAULT_NUM_MAILMESSAGES = 3;
	public static int DEFAULT_DAYS_IN_PAST = 10;

	/**
	 * Prefix for this provider
	 */
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	// output formats
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	// input formats
	public String[] getHandledInputFormats() {
		return new String[] { Formats.XML, Formats.JSON };
	}

	/**
	 * Get the list of MailArchiveChannels for a site
	 *
	 * @param siteId
	 *            - siteId requested
	 * @return
	 */
	private List<?> getMailArchiveChannels(String siteId) {

		// get currentUserId for permissions checks
		String currentUserId = sessionManager.getCurrentSessionUserId();

		if (log.isDebugEnabled()) {
			log.debug("siteId: " + siteId);
			log.debug("currentUserId: " + currentUserId);
		}

		// get Mail channels for given site
		List<String> channels = getSiteMailChannels(siteId);

		return channels;
	}

	/**
	 * return MailChannel ids for given site
	 * 
	 * @param siteId
	 * @return
	 * @throws org.sakaiproject.mailarchive.entityprovider.EntityNotFoundException
	 */
	private List<String> getSiteMailChannels(String siteId)
			throws EntityNotFoundException {
		// check current user has mail.read permissions for this site
		if (!securityService.unlock(MailArchiveService.SECURE_MAIL_READ,
				siteService.siteReference(siteId))) {
			throw new SecurityException("You do not have access to site: "
					+ siteId);
		}

		// get the channels
		List<String> channels = getChannels(siteId);
		if (channels.size() == 0) {
			throw new EntityNotFoundException(
					"No mail archive channels found for site: " + siteId,
					siteId);
		}

		if (log.isDebugEnabled()) {
			log.debug("channels: " + channels.toString());
			log.debug("num channels: " + channels.size());
		}

		return channels;
	}

	/**
	 * Get the list of mailArchiveMessages for a site
	 *
	 * @param siteId
	 *            - siteId requested
	 * @return
	 */
	private List<Object> getMailArchiveMessages(String siteId)
			throws PermissionException, IdUnusedException {

		// get currentUserId for permissions checks
		String currentUserId = sessionManager.getCurrentSessionUserId();

		if (log.isDebugEnabled()) {
			log.debug("siteId: " + siteId);
			log.debug("currentUserId: " + currentUserId);
		}

		// get Mail channels for given site
		List<String> channels = getSiteMailChannels(siteId);

		String siteTitle = getSiteTitle(siteId);

		// get the mailArchiveMessages for each channel
		List<Message> mailArchiveMessages = new ArrayList<Message>();

		// for each channel
		for (String channelRef : channels) {
			try {
				MailArchiveChannel channel = mailArchiveService
						.getMailArchiveChannel(channelRef);
				// no filter for now
				mailArchiveMessages.addAll(channel
						.getMessages(null, true, null));
			} catch (PermissionException e) {
				throw new PermissionException(currentUserId,
						MailArchiveService.SECURE_MAIL_READ, channelRef);
			} catch (IdUnusedException e) {
				throw new IdUnusedException("User: " + currentUserId
						+ " cannot find mailArchiveMessage channel: "
						+ channelRef + ". ");
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("mailArchiveMessages.size(): "
					+ mailArchiveMessages.size());
		}

		return converToDecoratedMailArchiveMessages(currentUserId, siteTitle,
				mailArchiveMessages);
	}

	/**
	 * get site title based on siteId
	 * 
	 * @param siteId
	 * @return
	 */
	private String getSiteTitle(String siteId) {
		Site site = null;
		String siteTitle = null;

		// get site
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("No site found for the siteid:"
					+ siteId + " : " + e.getMessage());
		}

		// get site title
		siteTitle = site.getTitle();
		return siteTitle;
	}

	/**
	 * convert raw mailArchiveMessages into decorated mailArchiveMessages
	 * 
	 * @param currentUserId
	 * @param siteTitle
	 * @param mailArchiveMessages
	 * @return
	 */
	private List<Object> converToDecoratedMailArchiveMessages(
			String currentUserId, String siteTitle,
			List<Message> mailArchiveMessages) {
		List<DecoratedMailArchiveMessage> DecoratedMailArchiveMessages = new ArrayList<DecoratedMailArchiveMessage>();

		for (Message m : mailArchiveMessages) {
			MailArchiveMessage a = (MailArchiveMessage) m;
			DecoratedMailArchiveMessage da = createDecoratedMailArchiveMessage(
					a, siteTitle);
			DecoratedMailArchiveMessages.add(da);
		}

		// sort
		Collections.sort(DecoratedMailArchiveMessages);

		// reverse so it is date descending. This could be dependent on a
		// parameter that specifies the sort order
		Collections.reverse(DecoratedMailArchiveMessages);

		List<Object> rv = new ArrayList<Object>();
		rv.addAll(DecoratedMailArchiveMessages);
		return rv;
	}

	private DecoratedMailArchiveMessage createDecoratedMailArchiveMessage(
			MailArchiveMessage a, String siteTitle) {
		String reference = a.getReference();
		String mailArchiveMessageId = a.getId();
		Reference ref = entityManager.newReference(reference);
		String siteId = ref.getContext();
		String channel = ref.getContainer();

		DecoratedMailArchiveMessage da = new DecoratedMailArchiveMessage(
				siteId, channel, mailArchiveMessageId);

		da.setSubject(a.getMailArchiveHeader().getSubject());
		da.setBody(a.getBody());
		da.setCreatedByDisplayName(a.getHeader().getFrom().getDisplayName());
		da.setCreatedOn(new Date(a.getHeader().getDate().getTime()));
		da.setSiteId(siteId);
		da.setSiteTitle(siteTitle);
		da.setHeaders(a.getMailArchiveHeader().getMailHeaders());

		// get attachments
		List<DecoratedAttachment> attachments = new ArrayList<DecoratedAttachment>();
		for (Reference attachment : (List<Reference>) a.getHeader()
				.getAttachments()) {
			String url = attachment.getUrl();
			String name = attachment.getProperties().getPropertyFormatted(
					attachment.getProperties().getNamePropDisplayName());
			String attachId = attachment.getId();
			String type = attachment.getProperties().getProperty(
					attachment.getProperties().getNamePropContentType());
			String attachRef = attachment.getReference();
			DecoratedAttachment decoratedAttachment = new DecoratedAttachment(
					attachId, name, type, url, attachRef);
			attachments.add(decoratedAttachment);
		}
		da.setAttachments(attachments);
		return da;
	}

	/**
	 * Return a list of DecoratedAttachment objects
	 * 
	 * @param attachments
	 *            List of Reference objects
	 * @return
	 */
	private List<DecoratedAttachment> decorateAttachments(
			List<Reference> attachments) {
		List<DecoratedAttachment> decoAttachments = new ArrayList<DecoratedAttachment>();
		for (Reference attachment : attachments) {
			DecoratedAttachment da = new DecoratedAttachment();
			da.setId(Validator.escapeHtml(attachment.getId()));
			da.setName(Validator
					.escapeHtml(attachment.getProperties()
							.getPropertyFormatted(
									attachment.getProperties()
											.getNamePropDisplayName())));
			da.setType(attachment.getProperties().getProperty(
					attachment.getProperties().getNamePropContentType()));

			da.setUrl(attachment.getUrl());
			da.setRef(attachment.getEntity().getReference());
			decoAttachments.add(da);
		}
		return decoAttachments;
	}

	/**
	 * Gets an mailArchiveMessage based on the id and site
	 * 
	 * @param entityId
	 *            id of the mailArchiveMessage
	 * @param siteId
	 *            siteid
	 * @return
	 */
	private DecoratedMailArchiveMessage findEntityById(String entityId,
			String siteId) {
		MailArchiveMessage tempMsg = null;
		DecoratedMailArchiveMessage DecoratedMailArchiveMessage = new DecoratedMailArchiveMessage();
		if (entityId != null) {
			try {
				MailArchiveChannel mailArchiveMessageChannel = mailArchiveService
						.getMailArchiveChannel("/mailArchiveMessage/channel/"
								+ siteId + "/main");
				tempMsg = (MailArchiveMessage) mailArchiveMessageChannel
						.getMessage(entityId);
			} catch (Exception e) {
				log.error("Error finding mailArchiveMessage: " + entityId
						+ " in site: " + siteId + "." + e.getClass() + ":"
						+ e.getStackTrace());
			}
		}
		DecoratedMailArchiveMessage.setSiteId(tempMsg.getId());
		DecoratedMailArchiveMessage.setBody(tempMsg.getBody());
		MailArchiveMessageHeader header = tempMsg.getMailArchiveHeader();
		DecoratedMailArchiveMessage.setSubject(header.getSubject());

		List attachments = header.getAttachments();
		List<DecoratedAttachment> attachmentUrls = decorateAttachments(attachments);

		DecoratedMailArchiveMessage.setAttachments(attachmentUrls);
		DecoratedMailArchiveMessage.setCreatedOn(new Date(header.getDate()
				.getTime()));
		DecoratedMailArchiveMessage.setCreatedByDisplayName(header.getFrom()
				.getDisplayName());
		DecoratedMailArchiveMessage.setSiteId(siteId);
		DecoratedMailArchiveMessage.setHeaders(header.getMailHeaders());

		return DecoratedMailArchiveMessage;
	}

	/**
	 * Utility routine used to get an integer named value from a map or supply a
	 * default value if none is found.
	 */

	private int getIntegerParameter(Map<?, ?> params, String paramName,
			int defaultValue) {
		String intValString = (String) params.get(paramName);

		if (StringUtils.trimToNull(intValString) != null) {
			return Integer.parseInt(intValString);
		} else {
			return defaultValue;
		}
	}

	/**
	 * Utility to get the date for n days ago
	 * 
	 * @param n
	 *            number of days in the past
	 * @return
	 */
	private Date getTimeForDaysInPast(int n) {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -n);

		return cal.getTime();
	}

	/**
	 * Helper to get the channels for a site.
	 * <p>
	 * If user site and not superuser, returns all available channels for this
	 * user.<br />
	 * 
	 * @param siteId
	 * @return
	 */
	private List<String> getChannels(String siteId) {

		List<String> channels = new ArrayList<String>();

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			// this should have been caught and dealt with already so just
			// return empty list
			return channels;
		}
		if (site != null) {
			ToolConfiguration toolConfig = site
					.getToolForCommonId("sakai.mailbox");

			if (toolConfig != null) {
				Properties props = toolConfig.getPlacementConfig();
				if (props.isEmpty()) {
					props = toolConfig.getConfig();
				}

				if (props != null) {
					channels = Collections.singletonList(mailArchiveService
							.channelReference(siteId,
									SiteService.MAIN_CONTAINER));
				}
			}
		}

		return channels;
	}

	/*
	 * Callback class so that we can form references in a generic way.
	 */
	private final class MailArchiveChannelReferenceMaker implements
			MergedList.ChannelReferenceMaker {
		public String makeReference(String siteId) {
			return mailArchiveService.channelReference(siteId,
					SiteService.MAIN_CONTAINER);
		}
	}

	/**
	 * get EmailArchive messages for site
	 */
	@EntityCustomAction(action = "siteMessages", viewKey = EntityView.VIEW_LIST)
	public List<Object> getMailArchiveMessagesForSite(EntityView view,
			Map<String, Object> params) {
		log.info("getMailArchiveMessagesForSite");

		List<Object> rv = new ArrayList<Object>();

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			rv.add("siteId must be set in order to get the mailArchiveMessages for a site, via the URL /mailArchiveMessage/site/siteId");
			return rv;
		}

		// check this is a valid site
		if (!siteService.siteExists(siteId)) {
			rv.add("Invalid siteId: " + siteId);
			return rv;
		}

		try {
			rv = getMailArchiveMessages(siteId);
			return rv;
		} catch (Exception e) {
			rv.add(e.getMessage() + " for siteId = " + siteId);
			return rv;
		}
	}

	/**
	 * get MailArchiveMessageChannel object for given site
	 * 
	 */
	@EntityCustomAction(action = "siteChannels", viewKey = EntityView.VIEW_LIST)
	public List<?> getMailArchiveChannelsForSite(EntityView view,
			Map<String, Object> params) {
		log.info(" getMailArchiveChannelsForSite ");
		List<String> rv = new ArrayList<String>();

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			rv.add("siteId must be set in order to get the mailArchiveMessages for a site, via the URL /mailArchiveMessage/site/siteId");
			return rv;
		}

		// check this is a valid site
		if (!siteService.siteExists(siteId)) {
			rv.add("Invalid siteId: " + siteId);
			return rv;
		}

		try {
			rv = getChannels(siteId);
		} catch (Exception e) {
			// return exception string
			rv.add(e.getMessage() + " for site id " + siteId);
		}
		return rv;
	}

	/**
	 * get MailArchiveMessageChannel object for given site
	 * 
	 */
	@EntityCustomAction(action = "channelMessages", viewKey = EntityView.VIEW_LIST)
	public List<Object> getMailArchiveMessagesForChannel(EntityView view,
			Map<String, Object> params) {

		log.info(" getMailArchiveMessagesForChannel ");

		List<Object> rv = new ArrayList<Object>();

		// get currentUserId for permissions checks
		String currentUserId = sessionManager.getCurrentSessionUserId();
		// get siteId
		String siteId = view.getPathSegment(2);
		// get channelId
		String channelId = view.getPathSegment(3);

		if (siteId == null || siteId.length() == 0) {
			rv.add("You must supply the siteId.");
			return rv;
		}
		if (channelId == null || channelId.length() == 0) {
			rv.add("You must supply an channelId");
			return rv;
		}

		String siteTitle = getSiteTitle(siteId);

		String ref = mailArchiveService.channelReference(siteId, channelId);

		List<Message> mailArchiveMessages = new ArrayList<Message>();
		try {
			MailArchiveChannel channel = mailArchiveService
					.getMailArchiveChannel(ref);
			// no filter for now
			mailArchiveMessages.addAll(channel.getMessages(null, true, null));
		} catch (IdUnusedException e) {
			rv.add("Couldn't find MailArchive channel " + ref);
			return rv;
		} catch (PermissionException e) {
			rv.add("You don't have permissions to access this channel " + ref);
			return rv;
		}

		try {
			return converToDecoratedMailArchiveMessages(currentUserId,
					siteTitle, mailArchiveMessages);
		} catch (Exception e) {
			// report error
			rv.add(e.getMessage() + " for userId = " + currentUserId
					+ " and site title = " + siteTitle);
			return rv;
		}
	}

	// The reason this is EntityView.VIEW_LIST, is we want the URL pattern to be
	// /mailArchiveMessage/channel/.... rather
	// than //mailArchiveMessage/{id}/channel.

	/**
	 * This handles mailArchiveMessages, URLs should be like,
	 * /mailArchiveMessage/msg/{context}/{channelId}/{mailArchiveMessageId} an
	 * example would be
	 * /mailArchiveMessage/msg/21b1984d-af58-43da-8583-f4adee769aa2
	 * /main/5641323b-761a-4a4d-8761-688f4928141b . Context is normally the site
	 * ID and the channelId is normally "main" unless there are multiple
	 * channels in a site. This is an alternative to using the packed IDs.
	 *
	 */
	@EntityCustomAction(action = "message", viewKey = EntityView.VIEW_LIST)
	public Object message(EntityView view, Map<String, Object> params)
			throws EntityPermissionException {
		log.info("message");

		// This is all more complicated because entitybroker isn't very flexible
		// and mailArchiveMessages can only be loaded once you've got the
		// channel in which they reside first.
		String siteId = view.getPathSegment(2);
		String channelId = view.getPathSegment(3);
		String mailArchiveMessageId = view.getPathSegment(4);

		if (siteId == null || channelId == null || mailArchiveMessageId == null) {
			return "Please provide all required params siteId, channelId, and mailArchiveMessageId. ";
		}

		try {
			return getMailArchiveMessage(siteId, channelId,
					mailArchiveMessageId);
		} catch (Exception e) {
			return e.getMessage() + " for siteId = " + siteId
					+ " , channelId = " + channelId
					+ ", mailArchiveMessageId = " + mailArchiveMessageId;
		}
	}

	/**
	 * Get a DecoratedMailArchiveMessage given the siteId, channelId and
	 * mailArchiveMessageId
	 * 
	 * @param siteId
	 * @param channelId
	 * @param mailArchiveMessageId
	 * @return
	 */
	private DecoratedMailArchiveMessage getMailArchiveMessage(String siteId,
			String channelId, String mailArchiveMessageId) {
		if (mailArchiveMessageId == null || mailArchiveMessageId.length() == 0) {
			throw new IllegalArgumentException(
					"You must supply an mailArchiveMessageId");
		}
		if (siteId == null || siteId.length() == 0) {
			throw new IllegalArgumentException("You must supply the siteId.");
		}
		if (channelId == null || channelId.length() == 0) {
			throw new IllegalArgumentException("You must supply an channelId");
		}
		String ref = mailArchiveService.channelReference(siteId, channelId);
		try {
			MailArchiveChannel channel = mailArchiveService
					.getMailArchiveChannel(ref);
			MailArchiveMessage message = channel
					.getMailArchiveMessage(mailArchiveMessageId);
			return createDecoratedMailArchiveMessage(message, null);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Couldn't find: " + e.getId(),
					e.getId());
		} catch (PermissionException e) {
			throw new EntityException(
					"You don't have permissions to access this channel.",
					e.getResource(), 403);
		}
	}

	/**
	 * Model class for an attachment
	 */
	@NoArgsConstructor
	public class DecoratedAttachment {

		@Getter
		@Setter
		private String id;
		@Getter
		@Setter
		private String name;
		@Getter
		@Setter
		private String type;
		@Getter
		@Setter
		private String url;
		@Getter
		@Setter
		private String ref;

		public DecoratedAttachment(String name, String url) {
			this.name = name;
			this.url = url;
		}

		public DecoratedAttachment(String id, String name, String type,
				String url, String ref) {
			this.id = id;
			this.name = name;
			this.type = type;
			this.url = url;
			this.setRef(ref);
		}
	}

	@Override
	public Object getSampleEntity() {
		return new DecoratedMailArchiveMessage();
	}

	public Object getEntity(EntityReference ref) {
		// This is the packed ID.
		String id = ref.getId();
		if (id != null) {
			String parts[] = id.split(":");
			if (parts.length == 3) {
				String siteId = parts[0];
				String channelId = parts[1];
				String mailArchiveMessageId = parts[2];
				return getMailArchiveMessage(siteId, channelId,
						mailArchiveMessageId);
			}
		}
		return null;
	}

	/**
	 * Class to hold only the fields that we want to return
	 */
	@NoArgsConstructor
	public class DecoratedMailArchiveMessage implements Comparable<Object> {

		@Getter
		@Setter
		private List headers;
		@Getter
		@Setter
		private String subject;;
		@Getter
		@Setter
		private String body;
		@Getter
		@Setter
		private String createdByDisplayName;
		@Getter
		@Setter
		private Date createdOn;
		@Getter
		@Setter
		private List<DecoratedAttachment> attachments;
		@Getter
		@Setter
		private String siteId;
		@Getter
		@Setter
		private String mailArchiveMessageId;
		@Getter
		@Setter
		private String siteTitle;
		@Getter
		@Setter
		private String channel;

		/**
		 * As we are packing these fields into the ID, we need all of them.
		 * 
		 * @param siteId
		 * @param channel
		 * @param mailArchiveMessageId
		 */
		public DecoratedMailArchiveMessage(String siteId, String channel,
				String mailArchiveMessageId) {
			this.siteId = siteId;
			this.channel = channel;
			this.mailArchiveMessageId = mailArchiveMessageId;
		}

		public String getId() {
			return String.format("%s:%s:%s", siteId, channel,
					mailArchiveMessageId);
		}

		// default sort by date ascending
		public int compareTo(Object o) {
			Date field = ((DecoratedMailArchiveMessage) o).getCreatedOn();
			int lastCmp = createdOn.compareTo(field);
			return (lastCmp != 0 ? lastCmp : createdOn.compareTo(field));
		}

	}

	@Setter
	private EntityManager entityManager;

	@Setter
	private SecurityService securityService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private SiteService siteService;

	@Setter
	private MailArchiveService mailArchiveService;

	@Setter
	private UserDirectoryService userDirectoryService;

	@Setter
	private TimeService timeService;

	@Setter
	private ToolManager toolManager;

}
