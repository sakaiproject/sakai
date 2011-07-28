/*
 * Created on Feb 24, 2005
 */
package uk.ac.cam.caret.sakai.rsf.bridge;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *  
 */
public class SakaiNavConversion {

  public static Site siteForPID(SiteService siteservice, String pid) {
    Site togo = null;
    try {
      ToolConfiguration tc = siteservice.findTool(pid);
      String siteID = tc.getSiteId();
      togo = siteservice.getSite(siteID);
    }
    catch (Exception iue) {
      // In Mercury, we may get an NPE since there is no ToolConfiguration
    }
    return togo;
  }
  
  public static SitePage pageForToolConfig(SiteService siteservice,
      ToolConfiguration tc) {
    SitePage page = tc.getContainingPage();
    if (page == null) {
      page = siteservice.findPage(tc.getPageId());
    }
    return page;
  }


}

