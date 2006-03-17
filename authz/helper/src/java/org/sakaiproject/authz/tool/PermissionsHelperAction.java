package org.sakaiproject.tool.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.api.kernel.tool.ToolException;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.service.framework.session.SessionState;
import org.sakaiproject.service.legacy.security.PermissionsHelper;
import org.sakaiproject.util.java.ResourceLoader;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: Aug 11, 2005
 * Time: 11:29:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class PermissionsHelperAction extends VelocityPortletPaneledAction {
   private static ResourceLoader rb = new ResourceLoader("announcement");

   protected void toolModeDispatch(String methodBase, String methodExt,
                                   HttpServletRequest req, HttpServletResponse res) throws ToolException {
      SessionState sstate = getState(req);
      ToolSession toolSession = SessionManager.getCurrentToolSession();

      String mode = (String) sstate.getAttribute(PermissionsAction.STATE_MODE);
      Object started = toolSession.getAttribute(PermissionsHelper.STARTED);

      if (mode == null && started != null) {
         toolSession.removeAttribute(PermissionsHelper.STARTED);
         Tool tool = ToolManager.getCurrentTool();

         String url = (String) SessionManager.getCurrentToolSession().getAttribute(
               tool.getId() + Tool.HELPER_DONE_URL);

         SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

         try {
            res.sendRedirect(url);
         }
         catch (IOException e) {
            Log.warn("chef", this + " : ", e);
         }
         return;
      }

      super.toolModeDispatch(methodBase, methodExt, req,  res);
   }

   /**
    * Default is to use when Portal starts up
    */
   public String buildMainPanelContext(VelocityPortlet portlet,
      Context context,
      RunData rundata,
      SessionState sstate)
   {
      String mode = (String) sstate.getAttribute(PermissionsAction.STATE_MODE);

      if (mode == null) {
         initHelper(portlet, context, rundata, sstate);
      }

      String template = PermissionsAction.buildHelperContext(portlet, context, rundata, sstate);
      if (template == null)
      {
         addAlert(sstate, rb.getString("java.alert.prbset"));
      }
      else
      {
         return template;
      }

      return null;
   }

   protected void initHelper(VelocityPortlet portlet, Context context,
                             RunData rundata, SessionState state) {
      ToolSession toolSession = SessionManager.getCurrentToolSession();

      String prefix = (String)toolSession.getAttribute(PermissionsHelper.PREFIX);
      String siteRef = (String)toolSession.getAttribute(PermissionsHelper.SITE_REF);
      String description = (String)toolSession.getAttribute(PermissionsHelper.DESCRIPTION);

      toolSession.setAttribute(PermissionsHelper.STARTED, new Boolean(true));

      // setup for editing the permissions of the site for this tool, using the roles of this site, too
      state.setAttribute(PermissionsAction.STATE_REALM_ID, siteRef);
      state.setAttribute(PermissionsAction.STATE_REALM_ROLES_ID, siteRef);

      // ... with this description
      state.setAttribute(PermissionsAction.STATE_DESCRIPTION, description);

      // ... showing only locks that are prpefixed with this
      state.setAttribute(PermissionsAction.STATE_PREFIX, prefix);

      // start the helper
      state.setAttribute(PermissionsAction.STATE_MODE, PermissionsAction.MODE_MAIN);
   }
}
