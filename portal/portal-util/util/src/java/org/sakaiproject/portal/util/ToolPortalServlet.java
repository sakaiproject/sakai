package org.sakaiproject.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.api.*;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.util.StringUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ToolPortalServlet extends HttpServlet {
    /**
     * Create a ToolPortal (Servlet) with all of its dependencies.
     *
     * See ToolPortal and web.xml in portal-tool for the primary use. This class is here now to facilitate direct tool
     * testing without creating a clone.
     */
    public ToolPortalServlet(
            ActiveToolManager activeToolManager,
            ServerConfigurationService serverConfigurationService,
            SessionManager sessionManager,
            SiteService siteService
    ) {
        super();
        this.activeToolManager = activeToolManager;
        this.siteService = siteService;
        this.serverConfigurationService = serverConfigurationService;
        this.sessionManager = sessionManager;
    }

    protected final ActiveToolManager activeToolManager;
    protected final ServerConfigurationService serverConfigurationService;
    protected final SessionManager sessionManager;
    protected final SiteService siteService;


    /**
     * Access the Servlet's information display.
     *
     * @return servlet information.
     */
    @Override
    public String getServletInfo() {
        return "Sakai Tool Portal";
    }

    /**
     * Initialize the servlet.
     *
     * @param config The servlet config.
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        log.info("init()");
    }

    /**
     * Shutdown the servlet.
     */
    @Override
    public void destroy() {
        log.info("destroy()");

        super.destroy();
    }

    /**
     * Respond to navigation / access requests.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     * @throws ServletException.
     * @throws IOException.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            // get the Sakai session
            Session session = sessionManager.getCurrentSession();

            // our path is /placement-id/tool-destination, but we want to
            // include anchors and parameters in the destination...
            String path = URLUtils.getSafePathInfo(req);
            if ((path == null) || (path.length() <= 1)) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // get the placement id, ignoring the first "/"
            String[] parts = StringUtil.splitFirst(path.substring(1), "/");
            String placementId = parts[0];

            // get the toolPath if specified
            String toolPath = null;
            if (parts.length == 2) toolPath = "/" + parts[1];

            boolean success = doTool(req, res, session, placementId, req.getContextPath()
                    + req.getServletPath() + "/" + placementId, toolPath);

        } catch (Exception t) {
            doError(req, res, t);
        }
    }

    /**
     * Process a tool request
     *
     * @param req
     * @param res
     * @param session
     * @param placementId
     * @param toolContextPath
     * @param toolPathInfo
     * @return true if the processing was successful, false if nt
     * @throws ToolException
     * @throws IOException
     */
    protected boolean doTool(HttpServletRequest req, HttpServletResponse res,
                             Session session, String placementId, String toolContextPath,
                             String toolPathInfo) throws ToolException, IOException {
        // find the tool from some site
        // TODO: all placements are from sites? -ggolden
        ToolConfiguration siteTool = siteService.findTool(placementId);
        if (siteTool == null) return false;

        // find the tool registered for this
        ActiveTool tool = activeToolManager.getActiveTool(siteTool.getToolId());
        if (tool == null) return false;

        // permission check - visit the site (unless the tool is configured to
        // bypass)
        if (tool.getAccessSecurity() == Tool.AccessSecurity.PORTAL) {
            Site site = null;
            try {
                site = siteService.getSiteVisit(siteTool.getSiteId());
            } catch (IdUnusedException e) {
                return false;
            } catch (PermissionException e) {
                // TODO: login here?
                return false;
            }
        }

        // if the path is not set, and we are expecting one, we need to compute
        // the path and redirect
        // we expect a path only if the tool has a registered home -ggolden
        if ((toolPathInfo == null) && (tool.getHome() != null)) {
            // what path? The one last visited, or home
            ToolSession toolSession = sessionManager.getCurrentSession().getToolSession(
                    placementId);
            String redirectPath = (String) toolSession
                    .getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);
            if (redirectPath == null) {
                redirectPath = tool.getHome();
            }

            // redirect with this tool path
            String redirectUrl = serverConfigurationService.getServerUrl()
                    + toolContextPath + redirectPath;
            res.sendRedirect(res.encodeRedirectURL(redirectUrl));
            return true;
        }

        // store the path as the current path, if we are doing this
        if (tool.getHome() != null) {
            ToolSession toolSession = sessionManager.getCurrentSession().getToolSession(
                    placementId);
            toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION,
                    toolPathInfo);
        }

        // prepare for the forward
        req.setAttribute(ToolURL.MANAGER, new ToolURLManagerImpl(res));

        // let the tool do the the work (forward)
        tool.forward(req, res, siteTool, toolContextPath, toolPathInfo);

        return true;
    }

    protected void doError(HttpServletRequest req, HttpServletResponse res, Throwable t) {
        ErrorReporter err = new ErrorReporter();
        err.report(req, res, t);
    }

    /**
     * Respond to data posting requests.
     *
     * @param req The servlet request.
     * @param res The servlet response.
     * @throws ServletException.
     * @throws IOException.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        doGet(req, res);
    }
}
