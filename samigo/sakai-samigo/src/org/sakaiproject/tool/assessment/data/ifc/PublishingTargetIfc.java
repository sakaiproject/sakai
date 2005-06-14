package org.sakaiproject.tool.assessment.data.ifc;

import java.util.HashMap;

import org.sakaiproject.service.framework.log.Logger;
import org.sakaiproject.service.legacy.site.SiteService;

/**
 * @author <a href="mailto:jlannan.iupui.edu">Jarrod Lannan</a>
 * @version $id
 */
public interface PublishingTargetIfc
{
  public HashMap getTargets();

  /**
   * @return Returns the siteService.
   */
  public SiteService getSiteService();

  /**
   * @param siteService The siteService to set.
   */
  public void setSiteService(SiteService siteService);

  /**
   * @return Returns the log.
   */
  public Logger getLog();

  /**
   * @param log The log to set.
   */
  public void setLog(Logger log);
}