package org.sakaiproject.api.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public interface UIPermissionsManager
{

  /**
   * @param forum
   * @return
   */
  public boolean isNewTopic(DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponse(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isMovePostings(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isChangeSettings(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isPostToGradebook(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isRead(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isReviseAny(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isReviseOwn(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isDeleteAny(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isDeleteOwn(DiscussionTopic topic);

  /**
   * @param topic
   * @return
   */
  public boolean isMarkAsRead(DiscussionTopic topic);
}
