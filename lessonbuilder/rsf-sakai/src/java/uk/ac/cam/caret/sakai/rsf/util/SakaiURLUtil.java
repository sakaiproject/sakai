/*
 * Created on 31 Oct 2006
 */
package uk.ac.cam.caret.sakai.rsf.util;

import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

public class SakaiURLUtil {
  public static final String getHelperDoneURL(Tool tool,
      SessionManager sessionManager) {
    String url = (String) sessionManager.getCurrentToolSession().getAttribute(
        tool.getId() + Tool.HELPER_DONE_URL);
    if (url == null) {
      url = "/";
    }
    return url;
  }
}
