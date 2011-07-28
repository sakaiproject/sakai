/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;

import uk.org.ponder.rsac.servlet.StaticHttpServletFactory;
import uk.org.ponder.util.Logger;

public class SakaiHttpServletFactory extends StaticHttpServletFactory {
  private HttpServletRequest request;
  private HttpServletResponse response;

  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setHttpServletResponse(HttpServletResponse response) {
    this.response = response;
  }

  private String entityref = null;

  public void setEntityReference(String entityref) {
    this.entityref = entityref;
  }

  /**
   * Since it seems we can no longer apply servlet mappings in our web.xml as of Sakai
   * 2.0, we perform this feat manually, using the resourceurlbase init parameter, and
   * this string which represents the offset path for resource handled by RSF. Any paths
   * falling outside this will be treated as static, and sent to the resourceurlbase.
   */
  public static final String FACES_PATH = "faces";
  private String extrapath;

  // Use old-style initialisation semantics since this bean is populated by
  // inchuck.
  private boolean initted = false;

  private void checkInit() {
    if (!initted) {
      initted = true;
      init();
    }
  }

  public void init() {
    // only need to perform request demunging if this has not come to us
    // via the AccessRegistrar.
    if (entityref.equals("")) {
      extrapath = "/" + computePathInfo(request);
      final StringBuffer requesturl = request.getRequestURL();
      // now handled with implicitNullPathRedirect in RSF proper
      // if (extrapath.equals("")) {
      // extrapath = defaultview;
      // requesturl.append('/').append(FACES_PATH).append(extrapath);
      // }

      HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
        public String getPathInfo() {
          return extrapath;
        }

        public StringBuffer getRequestURL() {
          StringBuffer togo = new StringBuffer();
          togo.append(requesturl);
          return togo;
        }
      };
      request = wrapper;
    }
  }

  public HttpServletRequest getHttpServletRequest() {
    checkInit();
    return request;
  }

  public HttpServletResponse getHttpServletResponse() {
    checkInit();
    return response;
  }

  private static final String PORTAL_TOOL = "/portal/tool/";

  public static String computePathInfo(HttpServletRequest request) {
    String requesturl = request.getRequestURL().toString();
    String extrapath = request.getPathInfo();
    if (extrapath == null) {
      extrapath = "";
    }
    if (extrapath.length() > 0 && extrapath.charAt(0) == '/') {
      extrapath = extrapath.substring(1);
    }
    int earlypos = requesturl.indexOf('/' + FACES_PATH);
    // within a Sakai helper, the request wrapper is even FURTHER screwed up
    if ("".equals(extrapath) && earlypos >= 0) {
      extrapath = requesturl.substring(earlypos + 1);
    }
    // Now, the Sakai "PathInfo" is *longer* than we would expect were we
    // mapped properly, since it will include what we call the "FACES_PATH",
    // as inserted there by RequestParser when asked for the baseURL.
    if (extrapath.startsWith(FACES_PATH)) {
      extrapath = extrapath.substring(FACES_PATH.length());
    }
    // The Websphere dispatching environment is entirely broken, and never gives us 
    // any information on pathinfo. Make our best attempt to guess what it should be
    // in certain common situations.
    if ("websphere".equals(ServerConfigurationService.getString("servlet.container"))) {
      try { // Resolve RSF-129. Override all previous decisions if we can detect a global
            // /portal/tool request
        URL url = new URL(requesturl);
        String path = url.getPath();
        if (path.startsWith(PORTAL_TOOL)) {
          int nextslashpos = path.indexOf('/', PORTAL_TOOL.length() + 1);
          if (nextslashpos != -1) {
            extrapath = path.substring(nextslashpos + 1);
            int furtherslashpos = extrapath.indexOf('/');
            int helperpos = extrapath.indexOf(".helper");
            if (helperpos != -1) {
              if (helperpos < furtherslashpos) {
                extrapath = extrapath.substring(furtherslashpos + 1);  
              }
              else if (furtherslashpos == -1) {
                extrapath = "";
              }
            }
          }
          else {
            extrapath = "";
          }
        }
      }
      catch (MalformedURLException e) {
        Logger.log.info("Malformed input request URL", e);
      }
    }

    Logger.log.info("Beginning ToolSinkTunnelServlet service with requestURL of "
        + requesturl + " and extra path of " + extrapath);

    return extrapath;
  }

}
