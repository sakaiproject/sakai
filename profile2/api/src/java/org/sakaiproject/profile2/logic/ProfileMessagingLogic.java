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

import java.util.List;

import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.model.MessageParticipant;
import org.sakaiproject.profile2.model.MessageThread;

/**
 * An interface for dealing with messaging in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileMessagingLogic {

	/**
	 * Get the number of all unread messages for this user, across all all message threads.
	 *
	 * @param userId		uuid of the user to retrieve the count for
	 */
	public int getAllUnreadMessagesCount(final String userId);
	
	/**
	 * Get the number of threads with unread messages.
	 * <p>For instance, if a user has two message threads, each with one unread message in each thread, this will return 2, as expected.
	 * <br />However, if a user has two message threads, each with 5 unread messages in each thread, this will return 2, not 10.
	 * <br />This is because we are interested in the number of threads with unread messages not the total unread messages. See {@link ProfileLogic#getAllUnreadMessagesCount(String)} if you want that instead.</p>
	 * @param userId		uuid of the user to retrieve the count for
	 * @return
	 */
	public int getThreadsWithUnreadMessagesCount(final String userId);
	
	/**
	 * Get the number of all messages sent from this user
	 * 
	 * @param userId	uuid of the user to retrieve the count for
	 * @return
	 */
	public int getSentMessagesCount(final String userId);
	
	/**
	 * Gets a MessageThread, first gets the item, then injects the latest Message into it before returning
	 * TODO This needs to be optimised to get the latest message property in the same query.
	 * @param id	id of the thread
	 * @return
	 */
	public MessageThread getMessageThread(final String threadId);
	
		
	/**
	 * Gets a list of MessageThreads with messages to a given user, each containing the most recent messages in each thread
	 * TODO This needs to be optimised to get the latest message property in the same query.
	 * @param userId	user to get the list of messages for
	 * @return
	 */
	public List<MessageThread> getMessageThreads(final String userId);
	
	/**
	 * Gets the count of the message threads for a user
	 * @param userId	user to get the count of message threads for
	 * @return
	 */
	public int getMessageThreadsCount(final String userId);
	
	/**
	 * Gets a list of the messages contained in this thread, sorted by date posted.
	 * @param threadId	id of the thread to get the messages for
	 * @return
	 */
	public List<Message> getMessagesInThread(final String threadId);
	
	/**
	 * Gets the count of the messages in a thread
	 * @param threadId	thread to get the count for
	 * @return
	 */
	public int getMessagesInThreadCount(final String threadId);
	
	/**
	 * Gets a Message from the database
	 * @param id	id of the message
	 * @return
	 */
	public Message getMessage(final String id);
	
	/**
	 * Send a message
	 * <p>TODO this should be optimised for foreign key constraints</p>
	 * @param uuidTo		uuid of recipient
	 * @param uuidFrom		uuid of sender
	 * @param threadId		threadId, a uuid that should be generated via {@link ProfileUtils.generateUuid()}
	 * @param subject		message subject
	 * @param messageStr	message body
	 * @return
	 */
	public boolean sendNewMessage(final String uuidTo, final String uuidFrom, final String threadId, final String subject, final String messageStr);
	
	/**
	 * Sends a reply to a thread, returns the Message just sent
	 * @param threadId		id of the thread
	 * @param reply			the message
	 * @param userId		uuid of user who is sending the message
	 * @return
	 */
	public Message replyToThread(final String threadId, final String reply, final String userId);
	
	/**
	 * Toggle a single message as read/unread
	 * @param participant	the MessageParticipant record as this is the item that stores read/unread status
	 * @param status		boolean if to be toggled as read/unread
	 * @return
	 */
	public boolean toggleMessageRead(MessageParticipant participant, final boolean status);
	
	/**
	 * Get a MessageParticipant record
	 * @param messageId		message id to get the record for
	 * @param userUuid		uuid to get the record for
	 * @return
	 */
	public MessageParticipant getMessageParticipant(final String messageId, final String userUuid);
	
	
	/**
	 * Get a list of all participants in a thread
	 * @param threadId		id of the thread
	 * @return
	 */
	public List<String> getThreadParticipants(final String threadId);
	
	/**
	 * Is the user a participant in this thread?
	 * @param threadId		id of the thread
	 * @param userId		id of the user
	 * @return
	 */
	public boolean isThreadParticipant(final String threadId, final String userId);
	
	/**
	 * Get the subject of a thread
	 * @param threadId		id of the thread
	 * @return
	 */
	public String getThreadSubject(final String threadId);
	
}
