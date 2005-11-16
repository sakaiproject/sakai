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

  public boolean isPrivateAreaEnabled();
  public void saveArea(Area area);
  public void deleteArea(Area area);
  public Area getPrivateArea();
  public Area getDiscussionForumArea();

}
