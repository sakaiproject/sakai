package org.sakaiproject.api.app.messageforums.ui;

import java.util.List;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
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
  Message getMessageById(Long id);

  /**
   * @param topic
   * @return
   */
  int getTotalNoMessages(Topic topic);

  /**
   * @param topic
   * @return
   */
  int getUnreadNoMessages(Topic topic);

  /**
   * @return
   */
  public List getDiscussionForums();

  /**
   * @param topicId
   * @return
   */
  public DiscussionForum getForumById(Long forumId);

  /**
   * @param forumId
   * @return
   */
  public DiscussionForum getForumByUuid(String forumId);

  /**
   * @param topicId
   * @return
   */
  public List getMessagesByTopicId(Long topicId);

  /**
   * @param topicId
   * @return
   */
  public DiscussionTopic getTopicById(Long topicId);
  public DiscussionTopic getTopicByUuid(String uuid);

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
  public DiscussionForum createForum();

  /**
   * @param forum
   */
  public void deleteForum(DiscussionForum forum);

  /**
   * @param forum
   *          TODO
   * @return
   */
  public DiscussionTopic createTopic(DiscussionForum forum);

  /**
   * @param forum
   */
  public void saveForum(DiscussionForum forum);
  
  
  /**
   * @param forum
   */
  public void saveForumAsDraft(DiscussionForum forum);


  /**
   * @param topic
   */
  public void saveTopic(DiscussionTopic topic);

  /**
   * @param topic
   */
  public void deleteTopic(DiscussionTopic topic);

  /**
   * @param area
   * @return
   */
  public List getDefaultControlPermissions();

  /**
   * @param area
   * @return
   */
  public List getDefaultMessagePermissions();

  /**
   * @param forum
   * @return
   */
  public List getForumControlPermissions(DiscussionForum forum);

  /**
   * @param forum
   * @return
   */
  public List getForumMessagePermissions(DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public List getTopicControlPermissions(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public List getTopicMessagePermissions(DiscussionTopic topic);

  /**
   * @param controlPermission
   */
  public void saveDefaultControlPermissions(List controlpermissions);

  /**
   * @param messagePermissions
   */
  public void saveDefaultMessagePermissions(List messagePermissions);

  /**
   * @param forum
   * @param controlPermissions
   */
  public void saveForumControlPermissions(DiscussionForum forum,
      List controlPermissions);

  /**
   * @param forum
   * @param messagePermissions
   */
  public void saveForumMessagePermissions(DiscussionForum forum,
      List messagePermissions);

  /**
   * @param topic
   * @param controlPermissions
   */
  public void saveTopicControlPermissions(DiscussionTopic topic,
      List controlPermissions);

  /**
   * @param topic
   * @param messagePermissions
   */
  public void saveTopicMessagePermissions(DiscussionTopic topic,
      List messagePermissions);

}
