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
  
  public boolean isPrivateAreaEnabled();
  public void saveArea(Area area);
  public void deleteArea(Area area);
  public Area getPrivateArea();
  public Area getDiscussionForumArea();

}
