package org.sakaiproject.wicket.tool;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WicketServlet;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.util.Web;

public class SakaiWicketServlet extends WicketServlet {

    public static final String FIRST_PAGE = "first-page";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        final String contextPath = req.getContextPath();        
        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(req) {
            public String getContextPath() {
                return contextPath;
            }
        };        
        
        if ("GET".equals(req.getMethod())) {
            String myFirstPage = getInitParameter( FIRST_PAGE );
            String myPathInfo = req.getPathInfo();
                
            if ( (myPathInfo == null) && (myFirstPage != null) && (!myFirstPage.equals("/")) ) {
                if (!myFirstPage.startsWith("/")) {
                    myFirstPage = "/" + myFirstPage;
                }
                resp.sendRedirect(contextPath + myFirstPage);
                //resp.sendRedirect(myFirstPage);
            } else if (myPathInfo != null && (myPathInfo.startsWith("/WEB-INF/") || myPathInfo.equals("/WEB-INF"))) {
                resp.sendRedirect(contextPath + "/");
            } /*else {
                doNormalService(resp, wrappedRequest, req, myPathInfo);
            } */
            req.removeAttribute(Tool.NATIVE_URL);         
        }
        
        super.service(req, resp);
    } // service

    /*
    protected void doNormalService(HttpServletResponse response, HttpServletRequest request, HttpServletRequest origrequest, String pathInfo) throws ServletException, IOException {

        if (doHelper(response, origrequest, pathInfo)) {
            return;
        }

        RequestDispatcher dispatcher;
        if (pathInfo == null)
            dispatcher = request.getRequestDispatcher("");
        else
            dispatcher = request.getRequestDispatcher(pathInfo);
        dispatcher.forward(request, response);
    }    
    
    private boolean doHelper(HttpServletResponse response, HttpServletRequest request, String pathInfo) throws ToolException {
        Pattern myPattern = Pattern.compile(".* /([^/]+)\\.helper(?:/.*)?");
        Matcher myMatcher = myPattern.matcher(pathInfo);
        if (myMatcher.matches()) {
            String myHelperId = myMatcher.group(1);
            ActiveTool helperTool = ActiveToolManager.getActiveTool(myHelperId);

            // String panel = request.getParameter(PARAM_PANEL);
            // if (panel == null || panel.equals("") || panel.equals("null"))
            // panel = MAIN_PANEL;
            // String helperId = HELPER_ID + panel;
            // ToolSession toolSession = SessionManager.getCurrentToolSession();
            // toolSession.setAttribute(helperId, helperTool.getId());

            String[] parts = pathInfo.split("/");
            // /portal/tool/e1477edc-a133-4dbb-0073-68eee38670f1/sakai.filepicker.helper
            String context = "/portal/tool/" + SakaiUtils.getCurrentPlacementId() + "/" + helperTool.getId() + ".helper";
            String toolPath = Web.makePath(parts, 2, parts.length);
            request.setAttribute("sakai.filtered", "sakai.filtered");
            request.removeAttribute(Tool.NATIVE_URL);
            helperTool.help(request, response, context, toolPath);
            request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
            return true;
        }
        return false;
    }
*/


} // class
