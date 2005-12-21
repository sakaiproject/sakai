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
 
  public boolean isPrivateAreaEnabled();
  public void saveArea(Area area);
  public Area createArea(String typeId);
  public void deleteArea(Area area);
  public Area getAreaByContextIdAndTypeId(String typeId);
  public Area getAreaByType(final String typeId);
  public Area getPrivateArea();
  public Area getDiscusionArea();

}
