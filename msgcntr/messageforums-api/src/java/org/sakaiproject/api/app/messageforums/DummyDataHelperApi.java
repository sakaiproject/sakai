/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/DummyDataHelperApi.java $
 * $Id: DummyDataHelperApi.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

import java.util.List;

public interface DummyDataHelperApi
{
  public Area getPrivateArea();
  public Area getDiscussionForumArea();
  public boolean isPrivateAreaUnabled();
  public DiscussionForum getForumById(Long forumId);
  public List getMessagesByTopicId(Long topicId);
  public DiscussionTopic getTopicById(Long topicId);
  public boolean hasNextTopic(DiscussionTopic topic);
  public boolean hasPreviousTopic(DiscussionTopic topic);
  public DiscussionTopic getNextTopic(DiscussionTopic topic);
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic);
  public Message getMessageById(Long id);
 
}