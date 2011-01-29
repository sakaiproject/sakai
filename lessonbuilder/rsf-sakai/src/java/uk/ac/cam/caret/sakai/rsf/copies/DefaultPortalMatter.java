/*
 * Created on 1 Sep 2008
 */
package uk.ac.cam.caret.sakai.rsf.copies;

import org.sakaiproject.component.cover.ServerConfigurationService;

public class DefaultPortalMatter {
  public static String getDefaultPortalMatter() {
    // This code copied from CharonPortal.java/ToolPortal.java setupForward
    // It should really be available in a standard Sakai API.
    String skin = ServerConfigurationService.getString("skin.default");
    String skinRepo = ServerConfigurationService.getString("skin.repo");
    String headCssToolBase = "<link href=\""
            + skinRepo
            + "/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
    String headCssToolSkin = "<link href=\"" + skinRepo + "/" + skin
            + "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n";
    String headCss = headCssToolBase + headCssToolSkin;
    //String headJs = "<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>\n";
    //String head = headCss + headJs;
    return headCss;
  }
}
