package org.sakaiproject.adduser;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Simple servlet that allows a HTML single page webapp to work as a helper in Sakai.
 * All relative URLs are passed through to the webapp and there is a special URL of
 * <code>/finish</code> which redirects the client to the helper done URL.
 */
public class AddUserServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // We want to lose all the Sakai stuff.
        addCSRFToken(request, response);
        String pathInfo = request.getPathInfo();
        if (pathInfo == null && !request.getRequestURI().endsWith("/")) {
            // We need a trailing slash on all request for root to make relative URLs
            // work correctly
            String siteId = ComponentManager.get(ToolManager.class).getCurrentPlacement().getContext();
            // Pass the siteId in on the URL, we can't use the JavaScript siteId value as in MyWorkspace
            // you can use Worksite Setup to add participants to multiple sites.
            response.sendRedirect(request.getRequestURI()+"/?siteId="+ siteId);
        } else if (pathInfo != null && pathInfo.startsWith("/finish")) {
            ToolSession toolSession = ComponentManager.get(SessionManager.class).getCurrentToolSession();
            String url = toolSession.getAttribute("sakai.add.user"+ Tool.HELPER_DONE_URL).toString();
            response.sendRedirect(url);
        } else {
            if (pathInfo == null) {
              pathInfo = "/";
            }
            request.setAttribute(Tool.NATIVE_URL, "true");
            request.getRequestDispatcher(pathInfo).forward(request, response);
        }
    }

    private void addCSRFToken(HttpServletRequest request, HttpServletResponse response) {
        Session session = ComponentManager.get(SessionManager.class).getCurrentSession();
        String token = (String)session.getAttribute("XSRF-TOKEN");
        if (token == null) {
            // Generate a new token.
            token = UUID.randomUUID().toString();
            session.setAttribute("XSRF-TOKEN", token);
        }
        Cookie cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setMaxAge(60 * 60);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
