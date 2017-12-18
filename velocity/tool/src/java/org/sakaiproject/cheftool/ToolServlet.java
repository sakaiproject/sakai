/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.cheftool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.cheftool.api.Alert;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.Web;
import org.sakaiproject.vm.ActionURL;

/**
 * <p>
 * ToolServlet is a Servlet that support CHEF tools.
 * </p>
 * <p>
 * Extending VmServlet provides support for component location and use of the Velocity Template Engine.
 * </p>
 */
@SuppressWarnings("deprecation")
@Slf4j
public abstract class ToolServlet extends VmServlet
{
	private static final long serialVersionUID = 1L;

	/** ToolSession attribute name holding the helper id, if we are in helper mode. NOTE: promote to Tool -ggolden */
	protected static final String HELPER_ID = "sakai.tool.helper.id";

   /** used to pull helper info from the request url path **/
   private static final String HELPER_EXT = ".helper";

   /**
	 * Add some standard references to the vm context.
	 * 
	 * @param request
	 *        The render request.
	 * @param response
	 *        The render response.
	 */
	protected void setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	{
		super.setVmStdRef(request, response);

		// add the tool mode
		setVmReference("sakai_toolMode", getToolMode(request), request);

		// add alert
		setVmReference("sakai_alert", getAlert(request), request);

		// add menu
		setVmReference("sakai_menu", getMenu(request), request);

	} // setVmStdRef

	/**********************************************************************************************************************************************************************************************************************************************************
	 * tool mode support ******************************************************************************* Tool mode is stored in the servlet session state.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The mode value when no mode has been set. */
	protected final String TOOL_MODE_DEFAULT = "Default";

	/** The mode attribute name base - postfix with the portlet mode. */
	protected final String TOOL_MODE_ATTR = "sakai.toolMode";

	/** The special panel name for the title. */
	protected final String TITLE_PANEL = "Title";

	/** The special panel name for the main. */
	protected final String MAIN_PANEL = "Main";

	/**
	 * Set the tool mode.
	 * 
	 * @param toolMode
	 *        The new tool mode.
	 * @param req
	 *        The portlet request.
	 */
	protected void setToolMode(String toolMode, HttpServletRequest req)
	{
		// update the attribute in session state
		getState(req).setAttribute(TOOL_MODE_ATTR, toolMode);

	} // setToolMode

	/**
	 * Access the tool mode for the current Portlet mode.
	 * 
	 * @return the tool mode for the current Portlet mode.
	 * @param req
	 *        The portlet request.
	 */
	protected String getToolMode(HttpServletRequest req)
	{
		String toolMode = (String) getState(req).getAttribute(TOOL_MODE_ATTR);

		// use the default mode if nothing set
		if (toolMode == null)
		{
			toolMode = TOOL_MODE_DEFAULT;
		}

		return toolMode;

	} // getToolMode

	/**
	 * Respond to a request by dispatching to a portlet like "do" method based on the portlet mode and tool mode
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		doGet(req, res);

	} // doPost

	/**
	 * Respond to a request by dispatching to a portlet like "do" method based on the portlet mode and tool mode
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		// get the panel
		String panel = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(ActionURL.PARAM_PANEL);
		
		if (panel == null || panel.equals("") || panel.equals("null")) {
			panel = MAIN_PANEL;
		} else {
			// sanitize value
            panel = panel.replaceAll("[\r\n]","");
		}
			
		// HELPER_ID needs the panel appended
		String helperId = HELPER_ID + panel;

		// detect a helper done
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		String helper = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(helperId);
		if (helper != null)
		{
			// clear our helper id indicator from session
			toolSession.removeAttribute(helperId);

			// redirect to the same URL w/o the helper done indication, otherwise this is left in the browser and can be re-processed later
			String newUrl = req.getContextPath() + req.getServletPath() 
					+ (req.getPathInfo() == null ? "" : req.getPathInfo()) 
					+ "?" + ActionURL.PARAM_PANEL + "=" + panel;
			try
			{
				res.sendRedirect(newUrl);
			}
			catch (IOException e)
			{
				log.warn("redirecting after helper done detection  to: " + newUrl + " : " + e.toString());
			}
			return;
		}

		// get the sakai.tool.helper.id helper id from the tool session
		// if defined and it's not our tool id, we need to defer to the helper
		helper = (String) toolSession.getAttribute(helperId);
		Tool me = ToolManager.getCurrentTool();
		if ((helper != null) && (!helper.equals(me.getId())))
		{
			toolSession.removeAttribute(helperId);
			String newUrl = req.getContextPath() + req.getServletPath() +
			(req.getPathInfo() == null ? "" : req.getPathInfo()) + "/" + helper + HELPER_EXT;

			try
			{
				res.sendRedirect(newUrl);
			}
			catch (IOException e)
			{
				log.warn("redirecting to helper to: " + newUrl + " : " + e.toString());
			}
			return;
		}

		// see if we have a helper request
		if (sendToHelper(req, res, req.getPathInfo())) 
		{
			return;
		}

		// init or update the session state
		prepState(req, res);

		// see if there's an action to process
		processAction(req, res);

		// if not redirected
		if (!res.isCommitted())
		{
			// dispatch
			toolModeDispatch("doView", getToolMode(req), req, res);
		}

	} // doGet

	/**
	 * Setup for a helper tool - all subsequent requests will be directed there, till the tool is done.
	 * 
	 * @param helperId
	 *        The helper tool id.
	 */
	protected void startHelper(HttpServletRequest req, String helperId, String panel)
	{
		if (panel == null) panel = MAIN_PANEL;

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		toolSession.setAttribute(HELPER_ID + panel, helperId);
               
		// the done URL - this url and the extra parameter to indicate done
		// also make sure the panel is indicated - assume that it needs to be main, assuming that helpers are taking over the entire tool response
		String doneUrl = req.getContextPath() + req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()) + "?"
				+ HELPER_ID + panel + "=done" + "&" + ActionURL.PARAM_PANEL + "=" + panel;

		toolSession.setAttribute(helperId + Tool.HELPER_DONE_URL, doneUrl);
	}

	/**
	 * Setup for a helper tool - all subsequent requests will be directed there, till the tool is done.
	 * 
	 * @param helperId
	 *        The helper tool id.
	 */
	protected void startHelper(HttpServletRequest req, String helperId)
	{
		startHelper(req, helperId, MAIN_PANEL);
	}

	/**
	 * Dispatch to a "do" method based on reflection.
	 * 
	 * @param methodBase
	 *        The base name of the method to call.
	 * @param methodExt
	 *        The end name of the method to call.
	 * @param req
	 *        The HttpServletRequest.
	 * @param res
	 *        The HttpServletResponse
	 * @throws PortletExcption,
	 *         IOException, just like the "do" methods.
	 */
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		String methodName = null;
		try
		{
			// the method signature
			Class[] signature = new Class[2];
			signature[0] = HttpServletRequest.class;
			signature[1] = HttpServletResponse.class;

			// the method name
			methodName = methodBase + methodExt;

			// find a method of this class with this name and signature
			Method method = getClass().getMethod(methodName, signature);

			// the parameters
			Object[] args = new Object[2];
			args[0] = req;
			args[1] = res;

			// make the call
			method.invoke(this, args);

		}
		catch (NoSuchMethodException e)
		{
			throw new ToolException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new ToolException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new ToolException(e);
		}

	} // toolModeDispatch

	/**********************************************************************************************************************************************************************************************************************************************************
	 * action model support ******************************************************************************* Dispatch by reflection to the method named in the value of the "sakai_action" parameter. Option to use "sakai_action_ACTION" to dispatch to ACTION.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The request parameter name root that has the action name following. */
	protected final static String PARAM_ACTION_COMBO = "sakai_action_";

	/** The request parameter name whose value is the action. */
	protected final static String PARAM_ACTION = "sakai_action";

	/**
	 * Process a Portlet action.
	 */
	@SuppressWarnings("unchecked")
	protected void processAction(HttpServletRequest req, HttpServletResponse res)
	{
		// see if there's an action parameter, whose value has the action to use
		String action = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(PARAM_ACTION);

		// if that's not present, see if there's a combination name with the action encoded in the name
		if (action == null)
		{
			Enumeration<String> names = req.getParameterNames();
			while (names.hasMoreElements())
			{
				String name = names.nextElement();
				if (name.startsWith(PARAM_ACTION_COMBO))
				{
					action = name.substring(PARAM_ACTION_COMBO.length());
					break;
				}
			}
		}

		// process the action if present
		if (action != null)
		{
			actionDispatch("processAction", action, req, res);
		}

	} // processAction

	/**
	 * Dispatch to a "processAction" method based on reflection.
	 * 
	 * @param methodBase
	 *        The base name of the method to call.
	 * @param methodExt
	 *        The end name of the method to call.
	 * @param req
	 *        The ActionRequest.
	 * @param res
	 *        The ActionResponse
	 * @throws PortletExcption,
	 *         IOException, just like the "do" methods.
	 */
	protected void actionDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
	{
		String methodName = null;
		try
		{
			// the method signature
			Class[] signature = new Class[2];
			signature[0] = HttpServletRequest.class;
			signature[1] = HttpServletResponse.class;

			// the method name
			methodName = methodBase + methodExt;

			// find a method of this class with this name and signature
			Method method = getClass().getMethod(methodName, signature);

			// the parameters
			Object[] args = new Object[2];
			args[0] = req;
			args[1] = res;

			// make the call
			method.invoke(this, args);

		}
		catch (NoSuchMethodException e)
		{
			getServletContext().log("Exception calling method " + methodName + " " + e);
		}
		catch (IllegalAccessException e)
		{
			getServletContext().log("Exception calling method " + methodName + " " + e);
		}
		catch (InvocationTargetException e)
		{
			String xtra = "";
			if (e.getCause() != null) xtra = " (Caused by " + e.getCause() + ")";
			getServletContext().log("Exception calling method " + methodName + " " + e + xtra);
		}

	} // actionDispatch

	/**********************************************************************************************************************************************************************************************************************************************************
	 * session state support ******************************************************************************* SessionState is a cover for the portlet's attributes in the session. Attributes are those that are portlet scoped. Attributes names are protected
	 * with a namespace.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The state attribute name used to store the marker of have been initialized. */
	protected static final String ALERT_STATE_INITED = "sakai.inited";

	/**
	 * Access the "pid" - portlet window id, tool id, from the request
	 * 
	 * @param req
	 *        The current request.
	 * @return the "pid" - portlet window id, tool id, from the request
	 */
	protected String getPid(HttpServletRequest req)
	{
		String pid = (String) req.getAttribute(Tool.PLACEMENT_ID);
		return pid;
	}

	/**
	 * Access the SessionState for the current request. Note: this is scoped only for the current request.
	 * 
	 * @param req
	 *        The current portlet request.
	 * @return The SessionState objet for the current request.
	 */
	protected SessionState getState(HttpServletRequest req)
	{
		// key the state based on the pid, if present. If not we will use the servlet's class name
		String key = getPid(req);
		if (key == null)
		{
			key = this.toString() + ".";
			log.warn("getState(): using servlet key: " + key);
		}

		SessionState rv = UsageSessionService.getSessionState(key);

		if (rv == null)
		{
			log.warn("getState(): no state found for key: " + key + " " + req.getPathInfo() + " " + req.getQueryString() + " "
					+ req.getRequestURI());
		}

		return rv;
	}

	/**
	 * Prepare state, either for first time or update
	 * 
	 * @param req
	 *        The current portlet request.
	 * @param res
	 *        The current response.
	 */
	protected void prepState(HttpServletRequest req, HttpServletResponse res)
	{
		SessionState state = getState(req);

		// If two requests from the same session to the same tool (pid) come in at the same time, we might
		// get two threads in here doing initState() at once, which would not be good.
		// We need to sync. on something... but we have no object that maps to a session/tool instance
		// (state is a cover freshly created each time)
		// lets try to sync on the Sakai session. That's more than we need but not too bad. -ggolden
		Session session = SessionManager.getCurrentSession();
		synchronized (session)
		{
			// if this is the first time, init it
			if (state.getAttribute(ALERT_STATE_INITED) == null)
			{
				initState(state, req, res);

				// mark this state as initialized
				state.setAttribute(ALERT_STATE_INITED, new Boolean(true));
			}

			// othewise update it
			else
			{
				updateState(state, req, res);
			}
		}

	} // initState

	/**
	 * Initialize for the first time the session state for this session. If overridden in a sub-class, make sure to call super.
	 * 
	 * @param state
	 *        The session state.
	 * @param req
	 *        The current request.
	 * @param res
	 *        The current response.
	 */
	protected void initState(SessionState state, HttpServletRequest req, HttpServletResponse res)
	{
	}

	/**
	 * Update for this request processing the session state. If overridden in a sub-class, make sure to call super.
	 * 
	 * @param state
	 *        The session state.
	 * @param req
	 *        The current request.
	 * @param res
	 *        The current response.
	 */
	protected void updateState(SessionState state, HttpServletRequest req, HttpServletResponse res)
	{
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * alert support ******************************************************************************* Alerts are messages displayed to the user in the portlet output in a standard way. Alerts are added as needed. The Alert text reference is placed into the
	 * velocity context, and then cleared.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The state attribute name used to store the Alert. */
	protected static final String ALERT_ATTR = "sakai.alert";

	/**
	 * Access the Alert for the current request.
	 * 
	 * @param req
	 *        The current portlet request.
	 * @return The Alert objet for the current request.
	 */
	protected Alert getAlert(HttpServletRequest req)
	{
		// find the alert in state, if it's not there, make it.
		SessionState state = getState(req);
		Alert alert = null;

		// In case we crossed class loader boundaries
		try
		{
			alert = (Alert) state.getAttribute(ALERT_ATTR);
		}
		catch(Exception e) 
		{
			alert = null;
		}
		if (alert == null)
		{
			alert = new AlertImpl();
			state.setAttribute(ALERT_ATTR, alert);
		}

		return alert;

	} // getAlert

	/**
	 * Access the Alert in this state - will create one if needed.
	 * 
	 * @param state
	 *        The state in which to find the alert.
	 * @return The Alert objet.
	 */
	protected Alert getAlert(SessionState state)
	{
		// find the alert in state, if it's not there, make it.
		Alert alert = (Alert) state.getAttribute(ALERT_ATTR);
		if (alert == null)
		{
			alert = new AlertImpl();
			state.setAttribute(ALERT_ATTR, alert);
		}

		return alert;

	} // getAlert

	/**********************************************************************************************************************************************************************************************************************************************************
	 * menu support ******************************************************************************* Menus are sets of commands to be displayed in the user interface.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** The state attribute name used to store the Menu. */
	protected static final String MENU_ATTR = "sakai.menu";

	/**
	 * Access the Menu for the current request.
	 * 
	 * @param req
	 *        The current portlet request.
	 * @return The Menu objet for the current request.
	 */
	protected Menu getMenu(HttpServletRequest req)
	{
		// find the menu in state, if it's not there, make it.
		SessionState state = getState(req);
		Menu menu = null;
		try
		{
			menu = (Menu) state.getAttribute(MENU_ATTR);
		}
		catch(Exception e)
		{
			menu = null;
		}
		if (menu == null)
		{
			menu = new MenuImpl();
			state.setAttribute(MENU_ATTR, menu);
		}

		return menu;

	} // getMenu

   /**
 * @param req
 * @param res
 * @param target
 * @return
 * @throws ToolException
 */
protected boolean sendToHelper(HttpServletRequest req, HttpServletResponse res, String target) throws ToolException 
   {
      String path = req.getPathInfo();
      if (path == null) 
      {
         path = "/";
      }

      // 0 parts means the path was just "/", otherwise parts[0] = "",
      // parts[1] = item id, parts[2] if present is "edit"...
      String[] parts = path.split("/");

      if (parts.length < 2) 
      {
         return false;
      }

      if (!parts[1].endsWith(HELPER_EXT))
      {
    	  return false;
      }
      

      // calc helper id
      int posEnd = parts[1].lastIndexOf(".");
      
      String helperId = target.substring(1, posEnd + 1);
      ActiveTool helperTool = ActiveToolManager.getActiveTool(helperId);

      String context = req.getContextPath() + req.getServletPath() + Web.makePath(parts, 1, 2);
      String toolPath = Web.makePath(parts, 2, parts.length);
      helperTool.help(req, res, context, toolPath);

      return true; // was handled as helper call
   }

} // class ToolServlet

