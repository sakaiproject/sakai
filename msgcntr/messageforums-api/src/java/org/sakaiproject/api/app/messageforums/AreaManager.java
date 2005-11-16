/**
 * 
 */
package org.sakaiproject.api.app.messageforums;

/**
 * @author rshastri
 *
 */
public interface AreaManager
{
  /**
   * @return
   */
  public Area getArea();
  
  /**
   * @param contextId
   * @return
   */
  public Area getArea(String contextId);
  
  /**
   * @param forum
   * @param typeuuid
   */
  public void addForum(BaseForum forum,String typeuuid);
  
  /**
   * @param forum
   * @param typeuuid
   */
  public void removeForum(BaseForum forum, String typeuuid);
  
  public void addOpenForum(OpenForum forum);
  public void removeOpenForum(OpenForum forum);
  public void addPrivateForum(PrivateForum forum);
  public void removePrivateForum(PrivateForum forum);  
  public void addDiscussionnForum(DiscussionForum forum);
  public void removeDiscussionForum(DiscussionForum forum);
}
