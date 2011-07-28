/*
 * Created on 20 Jun 2008
 */
package uk.ac.cam.caret.sakai.rsf.copies;

import javax.servlet.http.HttpServletRequest;

/**
 * Copies of definitions in the utility class org.sakaiproject.util.Web which are used
 * within SakaiRSF. Copied here to avoid dependency risk of concrete JAR dependence from
 * non-Sakai code.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class Web {

  /**
   * Compute the URL that would return to this server based on the current request. Note:
   * this method is duplicated in the kernel/request RequestFilter.java
   * 
   * @param req The request.
   * @return The URL back to this server based on the current request.
   */
  public static String serverUrl(HttpServletRequest req) {
    String transport = null;
    int port = 0;
    boolean secure = false;

    // if force.url.secure is set (to a https port number), use https and this port
    String forceSecure = System.getProperty("sakai.force.url.secure");
    if (forceSecure != null) {
      transport = "https";
      port = Integer.parseInt(forceSecure);
      secure = true;
    }

    // otherwise use the request scheme and port
    else {
      transport = req.getScheme();
      port = req.getServerPort();
      secure = req.isSecure();
    }

    StringBuilder url = new StringBuilder();
    url.append(transport);
    url.append("://");
    url.append(req.getServerName());
    if (((port != 80) && (!secure)) || ((port != 443) && secure)) {
      url.append(":");
      url.append(port);
    }

    return url.toString();
  }

  /**
   * Return a string based on value that is safe to place into a javascript / html
   * identifier: anything not alphanumeric change to 'x'. If the first character is not
   * alphabetic, a letter 'i' is prepended.
   * 
   * @param value The string to escape.
   * @return value fully escaped using javascript / html identifier rules.
   */
  public static String escapeJavascript(String value) {
    if (value == null || "".equals(value))
      return "";
    try {
      StringBuilder buf = new StringBuilder();

      // prepend 'i' if first character is not a letter
      if (!java.lang.Character.isLetter(value.charAt(0))) {
        buf.append("i");
      }

      // change non-alphanumeric characters to 'x'
      for (int i = 0; i < value.length(); i++) {
        char c = value.charAt(i);
        if (!java.lang.Character.isLetterOrDigit(c)) {
          buf.append("x");
        }
        else {
          buf.append(c);
        }
      }

      String rv = buf.toString();
      return rv;
    }
    catch (Exception e) {
      return value;
    }
  }

}
