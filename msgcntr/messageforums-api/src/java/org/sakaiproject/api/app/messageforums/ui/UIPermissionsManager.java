package org.sakaiproject.api.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public interface UIPermissionsManager
{

  /**
   * @return
   */
  public boolean isNewForum();
  
  /**
   * @return
   */
  public boolean isChangeSettings(DiscussionForum forum);
  
  /**     
   * @param forum
   * @return
   */
  public boolean isNewTopic(DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum );

  /**
   * @param topic
   * @return
   */
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum);
}
