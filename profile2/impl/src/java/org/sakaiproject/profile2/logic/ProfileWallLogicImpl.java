/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.model.WallItemComment;
import org.sakaiproject.profile2.types.EmailType;
import org.sakaiproject.profile2.types.PrivacyType;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * Implementation of ProfileWallLogic API for Profile2 wall.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 * @deprecated The wall functionality will be removed from Sakai for the 13 release.
 */
@Deprecated
@Slf4j
public class ProfileWallLogicImpl implements ProfileWallLogic {

	/**
	 * Creates a new instance of <code>ProfileWallLogicImpl</code>.
	 */
	public ProfileWallLogicImpl() {
		
	}
	
	private boolean addNewItemToWall(int itemType, String itemText, final String userUuid) {

		final WallItem wallItem = new WallItem();

		wallItem.setUserUuid(userUuid);
		wallItem.setCreatorUuid(userUuid);
		wallItem.setType(itemType);
		wallItem.setDate(new Date());
		// this string is mapped to a localized resource string in GUI
		wallItem.setText(itemText);

		return dao.addNewWallItemForUser(userUuid, wallItem);

	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addNewCommentToWallItem(WallItemComment wallItemComment) {
		if (dao.addNewCommentToWallItem(wallItemComment)) {
			String ref = "/profile/wall/item/comment/" + wallItemComment.getId();
			sakaiProxy.postEvent(ProfileConstants.EVENT_WALL_ITEM_COMMENT_NEW, ref, false);
			return true;
		} else {
		    return false;
		}
	}
	
	private void notifyConnections(int itemType, String itemText, final String userUuid) {
		
		// get the connections of the creator of this content
		final List<Person> connections = connectionsLogic.getConnectionsForUser(userUuid);

		if (null == connections || 0 == connections.size()) {
			// there are therefore no walls to post event to
			return;
		}

		// set corresponding message type and exit if type unknown
		final EmailType itemMessageType;
		switch (itemType) {
			case ProfileConstants.WALL_ITEM_TYPE_EVENT:
				itemMessageType = EmailType.EMAIL_NOTIFICATION_WALL_EVENT_NEW;
				break;
			case ProfileConstants.WALL_ITEM_TYPE_STATUS:
				itemMessageType = EmailType.EMAIL_NOTIFICATION_WALL_STATUS_NEW;
				break;
			default:
				log.warn("not sending email due to unknown wall item type: " + itemType);
				return;
		}
		
		Thread thread = new Thread() {
			@Override
			public void run() {

				List<String> uuidsToEmail = new ArrayList<String>();

				for (Person connection : connections) {

					// only send email if user has preference set
					if (true == preferencesLogic.isPreferenceEnabled(connection.getUuid(), itemMessageType.toPreference())) {
						uuidsToEmail.add(connection.getUuid());
					}
				}

				sendWallNotificationEmailToConnections(uuidsToEmail, userUuid, itemMessageType);
			}
		};
		thread.start();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNewEventToWall(String event, final String userUuid) {
		if (addNewItemToWall(ProfileConstants.WALL_ITEM_TYPE_EVENT, event, userUuid)) {
			notifyConnections(ProfileConstants.WALL_ITEM_TYPE_EVENT, event, userUuid);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNewStatusToWall(String status, String userUuid) {
		if (addNewItemToWall(ProfileConstants.WALL_ITEM_TYPE_STATUS, status, userUuid)) {
			notifyConnections(ProfileConstants.WALL_ITEM_TYPE_STATUS, status, userUuid);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean postWallItemToWall(final String userUuid, final WallItem wallItem) {
		// post to wall
		if (false == dao.addNewWallItemForUser(userUuid, wallItem)) {
			return false;
		}

		String ref = "/profile/" + wallItem.getUserUuid() + "/wall/item/" + wallItem.getId();
		sakaiProxy.postEvent(ProfileConstants.EVENT_WALL_ITEM_NEW, ref, false);

		// don't email user if they've posted on their own wall
		if (!StringUtils.equals(sakaiProxy.getCurrentUserId(), userUuid)) {
			sendWallNotificationEmailToUser(userUuid, wallItem.getCreatorUuid(), EmailType.EMAIL_NOTIFICATION_WALL_POST_MY_NEW);
		}
		// and if they have posted on their own wall, let connections know
		else {
			// get the connections of the user associated with the wall
			final List<Person> connections = connectionsLogic.getConnectionsForUser(userUuid);

			if (null != connections) {

				Thread thread = new Thread() {
					@Override
					public void run() {

						List<String> uuidsToEmail = new ArrayList<String>();

						for (Person connection : connections) {

							// only send email if user has preference set
							if (preferencesLogic.isPreferenceEnabled(connection.getUuid(), EmailType.EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW.toPreference())) {
								uuidsToEmail.add(connection.getUuid());
							}
						}

						sendWallNotificationEmailToConnections(uuidsToEmail, userUuid, EmailType.EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW);
					}
				};
				thread.start();
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeWallItemFromWall(WallItem wallItem) {

		if (dao.removeWallItemFromWall(wallItem)) {
			String ref = "/profile/wall/item/remove/" + wallItem.getId();
			sakaiProxy.postEvent(ProfileConstants.EVENT_WALL_ITEM_REMOVE, ref, false);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WallItem getWallItem(long wallItemId) {
		return dao.getWallItem(wallItemId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WallItemComment getWallItemComment(final long wallItemCommentId) {
		return dao.getWallItemComment(wallItemCommentId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<WallItem> getWallItemsForUser(String userUuid, ProfilePrivacy privacy) {

		if (null == userUuid) {
			throw new IllegalArgumentException("must provide user id");
		}

		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (null == currentUserUuid) {
			throw new SecurityException(
					"You must be logged in to make a request for a user's wall items.");
		}

		if (null == privacy) {
			return new ArrayList<WallItem>();
		}

		if (false == StringUtils.equals(userUuid, currentUserUuid) && false == sakaiProxy.isSuperUser()) {
			if (false == privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYWALL)) {
				return new ArrayList<WallItem>();
			}
		}

		List<WallItem> wallItems = dao.getWallItemsForUser(userUuid);
		
		// filter wall items
		List<WallItem> filteredWallItems = new ArrayList<WallItem>();
		for (WallItem wallItem : wallItems) {
			// current user is always allowed to see their wall items
			if (true == StringUtils.equals(userUuid, currentUserUuid) || 
					true == sakaiProxy.isSuperUser()) {
				filteredWallItems.add(wallItem);
			// don't allow friend-of-a-friend if not connected
			} else if (privacyLogic.isActionAllowed(wallItem.getCreatorUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_MYWALL)) {
				filteredWallItems.add(wallItem);
			}
		}
		
		// wall items are comparable and need to be in order
		Collections.sort(filteredWallItems);
				
		return filteredWallItems;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<WallItem> getWallItemsForUser(String userUuid) {
		return getWallItemsForUser(userUuid, privacyLogic
				.getPrivacyRecordForUser(userUuid));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWallItemsCount(String userUuid) {
		return getWallItemsCount(userUuid, privacyLogic
				.getPrivacyRecordForUser(userUuid));
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWallItemsCount(String userUuid, ProfilePrivacy privacy) {

		final String currentUserUuid = sakaiProxy.getCurrentUserId();
		if (null == sakaiProxy.getCurrentUserId()) {
			throw new SecurityException(
					"You must be logged in to make a request for a user's wall items.");
		}

		if (null == privacy) {
			return 0;
		}

		if (false == StringUtils.equals(userUuid, currentUserUuid) && false == sakaiProxy.isSuperUser()) {

			if (false == privacyLogic.isActionAllowed(userUuid, currentUserUuid, PrivacyType.PRIVACY_OPTION_MYWALL)) {
				return 0;
			}
		}
				
		List<WallItem> wallItems = dao.getWallItemsForUser(userUuid);
		
		// filter wall items
		List<WallItem> filteredWallItems = new ArrayList<WallItem>();
		for (WallItem wallItem : wallItems) {
			// current user is always allowed to see their wall items
			if (true == StringUtils.equals(userUuid, currentUserUuid) || 
					true == sakaiProxy.isSuperUser()) {
				filteredWallItems.add(wallItem);
			// don't allow friend-of-a-friend if not connected
			} else if (privacyLogic.isActionAllowed(wallItem.getCreatorUuid(), currentUserUuid, PrivacyType.PRIVACY_OPTION_MYWALL)) {
				filteredWallItems.add(wallItem);
			}
		}
		
		return filteredWallItems.size();
	}
	
	private void sendWallNotificationEmailToConnections(List<String> toUuids, final String fromUuid, final EmailType messageType) {
		
		// create the map of replacement values for this email template
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
		replacementValues.put("senderWallLink", linkLogic.getEntityLinkToProfileWall(fromUuid));
		replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
		replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
		replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());
		
		String emailTemplateKey = null;
		
		if (EmailType.EMAIL_NOTIFICATION_WALL_EVENT_NEW == messageType) {
			emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_WALL_EVENT_NEW;			
		} else if (EmailType.EMAIL_NOTIFICATION_WALL_POST_CONNECTION_NEW == messageType) {
			emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_WALL_POST_CONNECTION_NEW;
		} else if (EmailType.EMAIL_NOTIFICATION_WALL_STATUS_NEW == messageType) {
			emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_WALL_STATUS_NEW;
		}
		
		if (null != emailTemplateKey) {
			// send individually to personalize email
			for (String toUuid : toUuids) {
				// this just keeps overwriting profileLink with current toUuid
				replacementValues.put("wallLink", linkLogic.getEntityLinkToProfileWall(toUuid));
				sakaiProxy.sendEmail(toUuid, emailTemplateKey, replacementValues);
			}
		} else {
			log.warn("not sending email, unknown message type for sendWallNotificationEmailToConnections: " + messageType);
		}
	}
	
	private void sendWallNotificationEmailToUser(String toUuid, final String fromUuid, final EmailType messageType) {

		// check if email preference enabled
		if (!preferencesLogic.isPreferenceEnabled(toUuid, messageType.toPreference())) {
			return;
		}

		// create the map of replacement values for this email template
		Map<String, String> replacementValues = new HashMap<String, String>();
		replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
		replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
		replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
		replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());
		
		String emailTemplateKey = null;
		
		if (EmailType.EMAIL_NOTIFICATION_WALL_POST_MY_NEW == messageType) {
			emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_WALL_POST_MY_NEW;
			
			replacementValues.put("wallLink", linkLogic.getEntityLinkToProfileWall(toUuid));
		}
		
		if (null != emailTemplateKey) {
			sakaiProxy.sendEmail(toUuid, emailTemplateKey, replacementValues);
		} else {
			log.warn("not sending email, unknown message type for sendWallNotificationEmailToUser: " + messageType);
		}

	}
		
	@Setter
	private ProfileDao dao;
	
	@Setter
	private ProfilePrivacyLogic privacyLogic;
	
	@Setter
	private ProfileConnectionsLogic connectionsLogic;
	
	@Setter
	private ProfileLinkLogic linkLogic;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;
	
	@Setter
	private SakaiProxy sakaiProxy;
	
}
