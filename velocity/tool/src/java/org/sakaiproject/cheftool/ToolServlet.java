/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.cheftool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.kernel.session.Session;
import org.sakaiproject.api.kernel.session.ToolSession;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.api.kernel.tool.ActiveTool;
import org.sakaiproject.api.kernel.tool.Tool;
import org.sakaiproject.api.kernel.tool.ToolException;
import org.sakaiproject.api.kernel.tool.cover.ActiveToolManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.cheftool.api.Alert;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.service.framework.log.cover.Logger;
import org.sakaiproject.service.framework.session.SessionState;
import org.sakaiproject.service.framework.session.cover.UsageSessionService;
import org.sakaiproject.util.ParameterParser;

/**
* <p>ToolServlet is a Servlet that support CHEF tools.</p>
* <p>Extending VmServlet provides support for component location and use of the Velocity Template Engine.</p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public abstract class ToolServlet extends VmServlet
{
	/** ToolSession attribute name holding the helper id, if we are in helper mode. NOTE: promote to Tool -ggolden */
	protected static final String HELPER_ID = "sakai.tool.helper.id";

	/**
	 * Add some standard references to the vm context.
	 * @param request The render request.
	 * @param response The render response.
	 */
	protected void setVmStdRef(
		HttpServletRequest request,
		HttpServletResponse response)
	{
		super.setVmStdRef(request, response);

		// add the tool mode
		setVmReference("sakai_toolMode", getToolMode(request), request);

		// add alert
		setVmReference("sakai_alert", getAlert(request), request);

		// add menu
		setVmReference("sakai_menu", getMenu(request), request);

	} // setVmStdRef

	/*******************************************************************************
	* tool mode support
	********************************************************************************
	* Tool mode is stored in the servlet session state.
	*******************************************************************************/

	/** The mode value when no mode has been set. */
	protected final String TOOL_MODE_DEFAULT = "Default";

	/** The mode attribute name base - postfix with the portlet mode. */
	protected final String TOOL_MODE_ATTR = "sakai.toolMode";

	/** The special panel name for the title. */
	protected final String TITLE_PANEL = "Title";

	/**
	 * Set the tool mode.
	 * @param toolMode The new tool mode.
	 * @param req The portlet request.
	 */
	protected void setToolMode(String toolMode, HttpServletRequest req)
	{
		// update the attribute in session state
		getState(req).setAttribute(TOOL_MODE_ATTR, toolMode);

	} // setToolMode

	/**
	 * Access the tool mode for the current Portlet mode.
	 * @return the tool mode for the current Portlet mode.
	 * @param req The portlet request.
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
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
		throws ServletException
	{
		doGet(req, res);

	} // doPost

	/**
	 * Respond to a request by dispatching to a portlet like "do" method based on the portlet mode and tool mode
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException
	{
// Note: this not needed (?) since we are sync'ing in prepState -ggolden
//		// jump right to disptch, no state init or actions, for title.
//		// Note: this is to avoid concurrent prepState() when Title and Main come in together. -ggolden
//		String panel = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(ActionURL.PARAM_PANEL);
//		if (TITLE_PANEL.equals(panel))
//		{
//			// dispatch
//			toolModeDispatch("doView", getToolMode(req), req, res);
//			return;
//		}

		// detect a helper done
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		String helper = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(HELPER_ID);
		if (helper != null)
		{
			// clear our helper id indicator from session
			toolSession.removeAttribute(HELPER_ID);
			// TODO: now what - we need to process the return from the helper - need a method to call -ggolden
		}

		// get the sakai.tool.helper.id helper id from the tool session
		// if defined and it's not our tool id, we need to defer to the helper
		helper = (String) toolSession.getAttribute(HELPER_ID);
		Tool me = ToolManager.getCurrentTool();
		if ((helper != null) && (!helper.equals(me.getId())))
		{
			// map the request to the helper
			ActiveTool tool = ActiveToolManager.getActiveTool(helper);
			String context = req.getContextPath() + req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
			String toolPath = "";
			tool.help(req, res, context, toolPath);
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
	 * @param helperId The helper tool id.
	 */
	protected void startHelper(HttpServletRequest req, String helperId)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		toolSession.setAttribute(HELPER_ID, helperId);
		
		// the done URL
		String doneUrl = req.getContextPath() + req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo()) + "?" + HELPER_ID + "=done";
		toolSession.setAttribute(helperId + Tool.HELPER_DONE_URL, doneUrl);
	}

	/**
	 * Dispatch to a "do" method based on reflection.
	 * @param methodBase The base name of the method to call.
	 * @param methodExt The end name of the method to call.
	 * @param req The HttpServletRequest.
	 * @param res The HttpServletResponse
	 * @throws PortletExcption, IOException, just like the "do" methods.
	 */
	protected void toolModeDispatch(
		String methodBase,
		String methodExt,
		HttpServletRequest req,
		HttpServletResponse res) throws ToolException
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

	/*******************************************************************************
	* action model support
	********************************************************************************
	* Dispatch by reflection to the method named in the value of the "sakai_action" parameter.
	* Option to use "sakai_action_ACTION" to dispatch to ACTION.
	*******************************************************************************/

	/** The request parameter name root that has the action name following. */
	protected final static String PARAM_ACTION_COMBO = "sakai_action_";

	/** The request parameter name whose value is the action. */
	protected final static String PARAM_ACTION = "sakai_action";

	/**
	 * Process a Portlet action.
	 */
	protected void processAction(
		HttpServletRequest req,
		HttpServletResponse res)
	{
		// see if there's an action parameter, whose value has the action to use
		String action = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(PARAM_ACTION);

		// if that's not present, see if there's a combination name with the action encoded in the name
		if (action == null)
		{
			Enumeration names = req.getParameterNames();
			while (names.hasMoreElements())
			{
				String name = (String) names.nextElement();
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
	 * @param methodBase The base name of the method to call.
	 * @param methodExt The end name of the method to call.
	 * @param req The ActionRequest.
	 * @param res The ActionResponse
	 * @throws PortletExcption, IOException, just like the "do" methods.
	 */
	protected void actionDispatch(
		String methodBase,
		String methodExt,
		HttpServletRequest req,
		HttpServletResponse res)
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
			getServletContext().log(
				"Exception calling method " + methodName + " " + e);
		}
		catch (IllegalAccessException e)
		{
			getServletContext().log(
				"Exception calling method " + methodName + " " + e);
		}
		catch (InvocationTargetException e)
		{
			String xtra = "";
			if (e.getCause() != null) xtra = " (Caused by " + e.getCause() + ")";
			getServletContext().log(
				"Exception calling method " + methodName + " " + e + xtra);
		}

	} // actionDispatch

	/*******************************************************************************
	* session state support
	********************************************************************************
	* SessionState is a cover for the portlet's attributes in the session.
	* Attributes are those that are portlet scoped.
	* Attributes names are protected with a namespace.
	*******************************************************************************/

	/** The state attribute name used to store the marker of have been initialized. */
	protected static final String ALERT_STATE_INITED = "sakai.inited";

	/**
	 * Access the "pid" - portlet window id, tool id, from the request
	 * @param req The current request.
	 * @return the "pid" - portlet window id, tool id, from the request
	 */
	protected String getPid(HttpServletRequest req)
	{
		String pid = (String) req.getAttribute(Tool.PLACEMENT_ID);
		return pid;
	}

	/**
	 * Access the SessionState for the current request.
	 * Note: this is scoped only for the current request.
	 * @param req The current portlet request.
	 * @return The SessionState objet for the current request.
	 */
	protected SessionState getState(HttpServletRequest req)
	{
		// key the state based on the pid, if present.  If not we will use the servlet's class name
		String key = getPid(req);
		if (key == null)
		{
			key = this.toString() + ".";
			Logger.warn(this + "getState(): using servlet key: " + key);
		}

		SessionState rv = UsageSessionService.getSessionState(key);

		if (rv == null)
		{
			Logger.warn(this + "getState(): no state found for key: " + key + " " + req.getPathInfo() + " " + req.getQueryString() + " " + req.getRequestURI());
		}

		return rv;
	}

	/**
	 * Prepare state, either for first time or update
	 * @param req The current portlet request.
	 * @param res The current response.
	 */
	protected void prepState(HttpServletRequest req, HttpServletResponse res)
	{
		SessionState state = getState(req);

		// If two requests from the same session to the same tool (pid) come in at the same time, we might
		// get two threads in here doing initState() at once, which would not be good.
		// We need to sync. on something...  but we have no object that maps to a session/tool instance
		// (state is a cover freshly created each time)
		// lets try to sync on the Sakai session.  That's more than we need but not too bad. -ggolden
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
	 * Initialize for the first time the session state for this session.
	 * If overridden in a sub-class, make sure to call super.
	 * @param state The session state.
	 * @param req The current request.
	 * @param res The current response.
	 */
	protected void initState(
		SessionState state,
		HttpServletRequest req,
		HttpServletResponse res)
	{}

	/**
	 * Update for this request processing the session state.
	 * If overridden in a sub-class, make sure to call super.
	 * @param state The session state.
	 * @param req The current request.
	 * @param res The current response.
	 */
	protected void updateState(
		SessionState state,
		HttpServletRequest req,
		HttpServletResponse res)
	{}

	/*******************************************************************************
	* alert support
	********************************************************************************
	* Alerts are messages displayed to the user in the portlet output in a standard way.
	* Alerts are added as needed.
	* The Alert text reference is placed into the velocity context, and then cleared.
	*******************************************************************************/

	/** The state attribute name used to store the Alert. */
	protected static final String ALERT_ATTR = "sakai.alert";

	/**
	 * Access the Alert for the current request.
	 * @param req The current portlet request.
	 * @return The Alert objet for the current request.
	 */
	protected Alert getAlert(HttpServletRequest req)
	{
		// find the alert in state, if it's not there, make it.
		SessionState state = getState(req);
		Alert alert = (Alert) state.getAttribute(ALERT_ATTR);
		if (alert == null)
		{
			alert = new AlertImpl();
			state.setAttribute(ALERT_ATTR, alert);
		}

		return alert;

	} // getAlert

	/**
	 * Access the Alert in this state - will create one if needed.
	 * @param state The state in which to find the alert.
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

	/*******************************************************************************
	* menu support
	********************************************************************************
	* Menus are sets of commands to be displayed in the user interface.
	*******************************************************************************/

	/** The state attribute name used to store the Menu. */
	protected static final String MENU_ATTR = "sakai.menu";

	/**
	 * Access the Menu for the current request.
	 * @param req The current portlet request.
	 * @return The Menu objet for the current request.
	 */
	protected Menu getMenu(HttpServletRequest req)
	{
		// find the menu in state, if it's not there, make it.
		SessionState state = getState(req);
		Menu menu = (Menu) state.getAttribute(MENU_ATTR);
		if (menu == null)
		{
			menu = new MenuImpl();
			state.setAttribute(MENU_ATTR, menu);
		}

		return menu;

	} // getMenu

} // class ToolServlet



