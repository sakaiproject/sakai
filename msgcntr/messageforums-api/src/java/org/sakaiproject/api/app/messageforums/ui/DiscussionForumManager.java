package org.sakaiproject.api.app.messageforums.ui;

import java.util.List;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public interface DiscussionForumManager
{
  /**
   * Retrieve discussion forum area
   * 
   * @return
   */
  Area getDiscussionForumArea();

  /**
   * @param message
   */
  void saveMessage(Message message);

  /**
   * @param message
   */
  void deleteMessage(Message message);

  /**
   * @param id
   * @return
   */
  Message getMessageById(String id);

  /**
   * @param topic
   * @return
   */
  int getTotalNoMessages(Topic topic);

  /**
   * @param topic
   * @return
   */
  int getUnreadNoMessages(String userId, Topic topic);

  /**
   * @return
   */
  public List getDiscussionForums();

  /**
   * @param topicId
   * @return
   */
  public DiscussionForum getForumById(String topicId);

  /**
   * @param topicId
   * @return
   */
  public List getMessagesByTopicId(String topicId);

  /**
   * @param topicId
   * @return
   */
  public DiscussionTopic getTopicById(String topicId);

  /**
   * @return
   */
  public boolean hasNextTopic(DiscussionTopic topic);

  /**
   * @return
   */
  public boolean hasPreviousTopic(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public DiscussionTopic getNextTopic(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public DiscussionTopic getPreviousTopic(DiscussionTopic topic);
  
  /**
   * @return
   */
  public boolean isInstructor();

  /**
   * @return
   */
  DiscussionForum createForum();

  /**
   *
   * @return
   */
  DiscussionTopic createTopic();

  void saveForum(DiscussionForum forum);
  
  void saveTopic(DiscussionTopic topic);
}
