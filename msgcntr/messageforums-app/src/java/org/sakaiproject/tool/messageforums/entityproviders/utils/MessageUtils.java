/**
 * Copyright (c) 2003-2013 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.messageforums.entityproviders.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.tool.messageforums.entityproviders.sparsepojos.SparseMessage;
import org.sakaiproject.tool.messageforums.entityproviders.sparsepojos.SparseThread;


/**
 * A helper class for arranging messages into their proper parent child relationships.
 * 
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
public class MessageUtils {
	
	/**
	 * Extracts the threads (messages with no parent) from the supplied messages and sets
	 * the message totals up on each of them.
	 * 
	 * @param sparseForum The SparseForum to extract the threads from
	 * @param forumManager We need this to the get the read stati for the accumulated message ids.
	 * @param userId We need this to the get the read stati for the accumulated message ids.
	 */
	public List<SparseThread> getThreadsWithCounts(List<SparseMessage> messages,DiscussionForumManager forumManager, String userId) {
		
		List<Long> messageIds = new ArrayList<Long>();
		
		// Find the top level threads, the messages with no parent, basically.
		List<SparseThread> threads = new ArrayList<SparseThread>();
		for (SparseMessage message : messages) {
			
			if(message.isDraft() || message.isDeleted()) {
				continue;
			}
			
			messageIds.add(message.getMessageId());
			if(message.getReplyTo() == null) {
				threads.add(new SparseThread(message));
			}
		}
		
		Map<Long,Boolean> readStati = forumManager.getReadStatusForMessagesWithId(messageIds, userId);
		
		for(SparseMessage thread : threads) {
			boolean read = readStati.get(thread.getMessageId());
			Counts counts = new Counts(1,(read) ? 1 : 0);
			thread.setRead(read);
			// We don't want to add the messages and bulk up the resulting JSON feed. We
			// still want the total and unread messages counts for this thread though.
			setupCounts(thread,messages,readStati,counts);
			thread.setTotalMessages(counts.total);
			thread.setReadMessages(counts.read);
		}
		
		return threads;
	}
	
	/**
	 * Sets up the message hierarchy for topMessage.
	 * 
	 * @param topMessage The topmost message that we want to setup the message graph for
	 * @param messages The flat list of messages that we want to insert into the hierarchy
	 * @param forumManager We need this to the get the read stati for the accumulated message ids.
	 * @param userId We need this to the get the read stati for the accumulated message ids.
	 */
	public void attachReplies(SparseMessage topMessage, List<SparseMessage> messages,DiscussionForumManager forumManager, String userId) {
		
		List<Long> messageIds = new ArrayList<Long>();
		
		for (SparseMessage message : messages) {
			messageIds.add(message.getMessageId());
		}
		
		Map<Long,Boolean> readStati = forumManager.getReadStatusForMessagesWithId(messageIds, userId);
		boolean read = readStati.get(topMessage.getMessageId());
		topMessage.setRead(read);
		Counts counts = new Counts(1,(read) ? 1 : 0);
		addReplies(topMessage,messages,readStati,counts);
		topMessage.setTotalMessages(counts.total);
		topMessage.setReadMessages(counts.read);
	}
	
	/**
	 * Does a depth first recursion into the messages list, looking for replies
	 * to the specified parent.
	 * 
	 * @param parent
	 * @param messages
	 */
	private void addReplies(SparseMessage parent,List<SparseMessage> messages, Map<Long,Boolean> readStati,Counts counts) {
		
		for (SparseMessage message : messages) {
			
			if(message.isDraft() || message.isDeleted()) {
				continue;
			}
			
			if(message.getReplyTo() != null
				&& message.getReplyTo().equals(parent.getMessageId())) {
				
				counts.total = counts.total + 1;
				
				boolean read = readStati.get(message.getMessageId());
				
				message.setRead(read);
				
				if(read) {
					counts.read = counts.read + 1;
				}
					
				parent.addReply(message);
				
				// Recurse
				addReplies(message,messages, readStati,counts);
			}
		}
	}
	
	/**
	 * Recursively iterates over the messages and increments the counts accumulator if any of them
	 * are a reply to the parent. The idea is to finally exit with a set of totals for an ancestor.
	 * 
	 * @param parent The message for which to look for replies.
	 * @param messages The flatted list of messages to search.
	 * @param readStati The pre-retrieved list of read stati for the messsage list.
	 * @param counts An accumulator of message counts
	 */
	private void setupCounts(SparseMessage parent,List<SparseMessage> messages, Map<Long,Boolean> readStati,Counts counts) {
		
		for (SparseMessage message : messages) {
			
			if(message.isDraft() || message.isDeleted()) {
				continue;
			}
			
			if(message.getReplyTo() != null
				&& message.getReplyTo().equals(parent.getMessageId())) {
				
				counts.total = counts.total + 1;
				
				if(readStati.get(message.getMessageId())) {
					counts.read = counts.read + 1;
				}
				
				// Recurse
				setupCounts(message,messages, readStati,counts);
			}
		}
	}
	
	public class Counts {
		
		public int total = 0;
		public int read = 0;
		
		public Counts(int total,int read) {
			this.total = total;
			this.read = read;
		}
	}
}
