/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/DiscussionForumService.java $
 * $Id: DiscussionForumService.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;

public interface DiscussionForumService extends EntityProducer  
{
	public static final String SERVICE_NAME = DiscussionForumService.class.getName();
	
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "messageforum";
	
	// Events for the (Private) Messages tool 
	public static final String EVENT_MESSAGES_ADD = "messages.new";

	public static final String EVENT_MESSAGES_FOLDER_ADD = "messages.newfolder";

	public static final String EVENT_MESSAGES_FOLDER_REVISE = "messages.revisefolder";

	public static final String EVENT_MESSAGES_READ = "messages.read";
	
	public static final String EVENT_MESSAGES_UNREAD = "messages.unread";

	public static final String EVENT_MESSAGES_REMOVE = "messages.delete";
	
	public static final String EVENT_MESSAGES_MOVE_TO_DELETED_FOLDER = "messages.movedtodeletefolder";    
	
	public static final String EVENT_MESSAGES_FOLDER_REMOVE = "messages.deletefolder";
	
	public static final String EVENT_MESSAGES_RESPONSE = "messages.reply";

	public static final String EVENT_MESSAGES_FORWARD = "messages.forward";

	// Events for the (Discussion) Forums tool
	public static final String EVENT_FORUMS_ADD = "forums.new";

	public static final String EVENT_FORUMS_FORUM_ADD = "forums.newforum";

	public static final String EVENT_FORUMS_TOPIC_ADD = "forums.newtopic";

	public static final String EVENT_FORUMS_READ = "forums.read";

	public static final String EVENT_FORUMS_TOPIC_READ = "forums.topic.read";

	public static final String EVENT_FORUMS_RESPONSE = "forums.response";

	public static final String EVENT_FORUMS_REMOVE = "forums.delete";
	
	public static final String EVENT_FORUMS_FORUM_REMOVE = "forums.deleteforum";
	
	public static final String EVENT_FORUMS_TOPIC_REMOVE = "forums.deletetopic";
	
	public static final String EVENT_FORUMS_REVISE = "forums.revise";

	public static final String EVENT_FORUMS_FORUM_REVISE = "forums.reviseforum";

	public static final String EVENT_FORUMS_TOPIC_REVISE = "forums.revisetopic";

	public static final String EVENT_FORUMS_GRADE = "forums.grade";

	public static final String EVENT_FORUMS_MOVE_THREAD = "forums.movethread";

	/** Used to determine if MessageCenter tool part of site */
	public static final String MESSAGE_CENTER_ID = "sakai.messagecenter";
	
	public static final String FORUMS_TOOL_ID = "sakai.forums";
	
	public static final String MESSAGES_TOOL_ID = "sakai.messages";
}