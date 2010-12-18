/*
 * Created on 31 Oct 2006
 */
package uk.ac.cam.caret.sakai.rsf.util;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.util.UniversalRuntimeException;

public class PermissionChecker {
  private SecurityService securityService = null;
  private ToolManager toolmanager = null;
  private SiteService siteService = null;

  public void setSecurityService(SecurityService service) {
    securityService = service;
  }

  public void setToolManager(ToolManager toolmanager) {
    this.toolmanager = toolmanager;
  }
  
  public void setSiteService(SiteService service) {
    siteService = service;
  }
  
  public boolean checkLockOnCurrentUserAndSite(String authzfunction) {
    String context = toolmanager.getCurrentPlacement().getContext();
    
    try {
      Site site = siteService.getSite(context);
      return securityService.unlock(authzfunction, site.getReference());
    } catch (IdUnusedException e) {
      throw UniversalRuntimeException.accumulate(e, "Could not fetch the site for context " + context);
    }
    
  }

}
