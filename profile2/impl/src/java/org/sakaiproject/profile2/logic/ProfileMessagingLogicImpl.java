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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;
import org.sakaiproject.profile2.types.EmailType;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Implementation of ProfileMessagingLogic for Profile2.
 * 
 * @author Steve Swinsburg (s.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileMessagingLogicImpl implements ProfileMessagingLogic {

	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getAllUnreadMessagesCount(final String userId) {
		return dao.getAllUnreadMessagesCount(userId);
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getThreadsWithUnreadMessagesCount(final String userId) {
		return dao.getThreadsWithUnreadMessagesCount(userId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getSentMessagesCount(final String userId) {
		return dao.getSentMessagesCount(userId);
	}

	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean sendNewMessage(final String uuidTo, final String uuidFrom, final String threadId, final String subject, final String messageStr) {

		String messageSubject = StringUtils.defaultIfBlank(subject, ProfileConstants.DEFAULT_PRIVATE_MESSAGE_SUBJECT);

		//setup thread
		MessageThread thread = new MessageThread();
		thread.setId(threadId);
		thread.setSubject(messageSubject);

		//setup message
		Message message = new Message();
		message.setId(ProfileUtils.generateUuid());
		message.setFrom(uuidFrom);
		message.setMessage(messageStr);
		message.setDatePosted(new Date());
		message.setThread(thread.getId());
		//saveNewMessage(message);
		
		
		//setup participants
		//at present we have one for the receipient and one for sender.
		//in future we may have multiple recipients and will need to check the existing list of thread participants 
		List<String> threadParticipants = new ArrayList<String>();
		threadParticipants.add(uuidTo);
		threadParticipants.add(uuidFrom);

		List<MessageParticipant> participants = new ArrayList<MessageParticipant>();
		for(String threadParticipant : threadParticipants){
			MessageParticipant p = new MessageParticipant();
			p.setMessageId(message.getId());
			p.setUuid(threadParticipant);
			if(StringUtils.equals(threadParticipant, message.getFrom())) {
				p.setRead(true); //sender 
			} else {
				p.setRead(false);
			}
			p.setDeleted(false);
			
			participants.add(p);
		}
		
		if(saveAllNewMessageParts(thread, message, participants)) {
			sendMessageEmailNotification(threadParticipants, uuidFrom, threadId, messageSubject, messageStr, EmailType.EMAIL_NOTIFICATION_MESSAGE_NEW);
            //post event
            sakaiProxy.postEvent(ProfileConstants.EVENT_MESSAGE_SENT, "/profile/" + uuidTo, true);
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public Message replyToThread(final String threadId, final String reply, final String uuidFrom) {
		
		try {
			
			//create the message and save it
			Message message = new Message();
			message.setId(ProfileUtils.generateUuid());
			message.setFrom(uuidFrom);
			message.setMessage(reply);
			message.setDatePosted(new Date());
			message.setThread(threadId);
			dao.saveNewMessage(message);
			
			//get the thread subject
			String subject = getMessageThread(threadId).getSubject();
			
			//get a unique list of participants in this thread, and save a record for each participant for this new message
			List<String> uuids = getThreadParticipants(threadId);
			for(String uuidTo : uuids) {
				MessageParticipant participant = getDefaultMessageParticipantRecord(message.getId(), uuidTo);
				if(StringUtils.equals(uuidFrom, uuidTo)) {
					participant.setRead(true); //sender 
				} else {
					// Fire message sent events for each recipient
					sakaiProxy.postEvent(ProfileConstants.EVENT_MESSAGE_SENT, "/profile/" + uuidTo, true);
				}

				dao.saveNewMessageParticipant(participant);
			}
			
			//send email notifications
			sendMessageEmailNotification(uuids, uuidFrom, threadId, subject, reply, EmailType.EMAIL_NOTIFICATION_MESSAGE_REPLY);
			
			return message;
		} catch (Exception e) {
			log.error("ProfileLogic.replyToThread(): Couldn't send reply: " + e.getClass() + " : " + e.getMessage());
		}
		
		return null;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<MessageThread> getMessageThreads(final String userId) {
		
		List<MessageThread> threads = dao.getMessageThreads(userId);
	  	
	  	//get latest message for each thread
	  	for(MessageThread thread : threads) {
	  		thread.setMostRecentMessage(dao.getLatestMessageInThread(thread.getId()));
	  	}
	  	
	  	return threads;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getMessageThreadsCount(final String userId) {
		return dao.getMessageThreadsCount(userId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<Message> getMessagesInThread(final String threadId) {
		return dao.getMessagesInThread(threadId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public int getMessagesInThreadCount(final String threadId) {
		return dao.getMessagesInThreadCount(threadId);
	}
	
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public Message getMessage(final String id) {
		return dao.getMessage(id);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public MessageThread getMessageThread(final String threadId) {
		
		MessageThread thread = dao.getMessageThread(threadId);
		if(thread == null){
			return null;
		}
		
		//add the latest message for this thread
		thread.setMostRecentMessage(dao.getLatestMessageInThread(threadId));
		
		return thread;
	}
	
	


	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean toggleMessageRead(MessageParticipant participant, final boolean status) {
		return dao.toggleMessageRead(participant, status);
	}
	

	/**
 	 * {@inheritDoc}
 	 */
	/*
	public boolean toggleAllMessagesInThreadAsRead(final String threadId, final String userUuid, final boolean read) {
		// TODO Auto-generated method stub
		return false;
	}
	*/
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid) {
		return dao.getMessageParticipant(messageId, userUuid);
	}

	
	
	/**
	 * Create a default MessageParticipant object for a message and user. This is so they can mark messages as unread/delete them. Not persisted until actioned.
	 * @param messageId
	 * @param userUuid
	 * @return
	 */
	private MessageParticipant getDefaultMessageParticipantRecord(final String messageId, final String userUuid) {
		
		MessageParticipant participant = new MessageParticipant();
		participant.setMessageId(messageId);
		participant.setUuid(userUuid);
		participant.setRead(false);
		participant.setDeleted(false);
		
		return participant;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public List<String> getThreadParticipants(final String threadId) {
		return dao.getThreadParticipants(threadId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public boolean isThreadParticipant(final String threadId, final String userId) {
		return getThreadParticipants(threadId).contains(userId);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public String getThreadSubject(final String threadId) {
		MessageThread thread = dao.getMessageThread(threadId);
		return thread.getSubject();
	}

	
	/**
	 * Sends an email notification to the users. Used for messages. This formats the data and calls {@link SakaiProxy.sendEmail(List<String> userIds, String emailTemplateKey, Map<String,String> replacementValues)}
	 * @param toUuids		list of users to send the message to - this will be formatted depending on their email preferences for this message type so it is safe to pass all users you need
	 * @param fromUuid		uuid from
	 * @param directId		the id of the item, used for direct links back to this item, if required.
	 * @param subject		subject of message
	 * @param messageStr	body of message
	 * @param messageType	the message type to send. Retrieves the emailTemplateKey based on this value
	 */
	private void sendMessageEmailNotification(final List<String> toUuids, final String fromUuid, final String directId, final String subject, final String messageStr, final EmailType messageType) {
		
		//is email notification enabled for this message type? Reformat the recipient list
		for(Iterator<String> it = toUuids.iterator(); it.hasNext();) {
			if(!preferencesLogic.isPreferenceEnabled(it.next(), messageType.toPreference())) {
				it.remove();
			}
		}
		
		//the sender is a message participant but we don't want email confirmations for them, so remove
		toUuids.remove(fromUuid);
		
		//new message
		if(messageType == EmailType.EMAIL_NOTIFICATION_MESSAGE_NEW) {
			
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_MESSAGE_NEW;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("messageSubject", subject);
			replacementValues.put("messageBody", messageStr);
			replacementValues.put("messageLink", linkLogic.getEntityLinkToProfileMessages(directId));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuids, emailTemplateKey, replacementValues);
			return;
		} 
		
		//reply
		if (messageType == EmailType.EMAIL_NOTIFICATION_MESSAGE_REPLY) {
				
			String emailTemplateKey = ProfileConstants.EMAIL_TEMPLATE_KEY_MESSAGE_REPLY;
			
			//create the map of replacement values for this email template
			Map<String,String> replacementValues = new HashMap<String,String>();
			replacementValues.put("senderDisplayName", sakaiProxy.getUserDisplayName(fromUuid));
			replacementValues.put("localSakaiName", sakaiProxy.getServiceName());
			replacementValues.put("messageSubject", subject);
			replacementValues.put("messageBody", messageStr);
			replacementValues.put("messageLink", linkLogic.getEntityLinkToProfileMessages(directId));
			replacementValues.put("localSakaiUrl", sakaiProxy.getPortalUrl());
			replacementValues.put("toolName", sakaiProxy.getCurrentToolTitle());

			sakaiProxy.sendEmail(toUuids, emailTemplateKey, replacementValues);
			return;
		}
	}
	
	/*
	 * helper method to save a message once all parts have been created. takes care of rollbacks incase of failure (TODO)
	 */
	private boolean saveAllNewMessageParts(MessageThread thread, Message message, List<MessageParticipant> participants) {
		dao.saveNewThread(thread);
		dao.saveNewMessage(message);
		dao.saveNewMessageParticipants(participants);

		return true;
	}
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	@Setter
	private ProfileLinkLogic linkLogic;
	
	@Setter
	private ProfilePreferencesLogic preferencesLogic;
	
	@Setter
	private ProfileDao dao;
	
}
