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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.api.Alert;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.EditorConfiguration;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.vm.ActionURL;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * VelocityPortletPaneledAction ...
 * </p>
 */
@SuppressWarnings("deprecation")
@Slf4j
public abstract class VelocityPortletPaneledAction extends ToolServlet
{

	private static final long serialVersionUID = 1L;

	/** message bundle */
	private static ResourceLoader rb = new ResourceLoader("velocity-tool");

	protected static final String BUTTON = "eventSubmit_";

	/** The currently active helper mode static class. */
	public static final String STATE_HELPER = "vppa.helper";

	protected static final String STATE_MODE = "mode";

	protected static final String STATE_OBSERVER = "obsever";

	protected static final String STATE_ACTION = "action";

	protected static final String STATE_NEW_PANEL = "state:new_panel";
	
	/** The name of the context variable containing the identifier for the site's root content collection */
	protected static final String CONTEXT_SITE_COLLECTION_ID = "vppa_site_collection_id";

	/** The name of the context variable containing the access URL for the site's root content collection */
	protected static final String CONTEXT_SITE_COLLECTION_URL = "vppa_site_collection_url";

	/** The panel name of the main panel - append the tool's id. */
	protected static final String LAYOUT_MAIN = "Main";
	
	/** The name of the param used for CSRF protection */
	protected static final String SAKAI_CSRF_TOKEN = "sakai_csrf_token";

	/** Constants to handle helper situations */
        protected static final String HELPER_LINK_MODE = "link_mode";
        protected static final String HELPER_MODE_DONE = "helper.done";

	private ContentHostingService contentHostingService;
	private FormattedText formattedText;

	public VelocityPortletPaneledAction() {
		contentHostingService = (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
		formattedText = ComponentManager.get(FormattedText.class);
	}
	
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		HttpServletRequest req = rundata.getRequest();
		Session session = SessionManager.getCurrentSession();
		
		if (getVmReference("is_wireless_device", req) == null)
		{
			Object c = session.getAttribute("is_wireless_device");
			setVmReference("is_wireless_device", c, req);
		}
		
		// Set a CSRF token for velocity-based forms
		if (getVmReference(SAKAI_CSRF_TOKEN, req) == null)
		{
			Object csrfToken = session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
			setVmReference(SAKAI_CSRF_TOKEN, csrfToken, req);
		}
	}

	/**
	 * Compute the courier update html element id for the main panel - add "." and other names for inner panels.
	 * 
	 * @param toolId
	 *        The tool (portlet) id.
	 * @return The courier update html element id for the main panel.
	 */
	public static String mainPanelUpdateId(String toolId)
	{
		// TODO: who should be responsible for "Main" here? It's a Portal thing... -ggolden
		return ComponentManager.get(FormattedText.class).escapeJavascript("Main" + toolId);

	} // mainPanelUpdateId

	/**
	 * Compute the courier update html element id for the title panel.
	 * 
	 * @param toolId
	 *        The tool (portlet) id.
	 * @return The courier update html element id for the title panel.
	 */
	public static String titlePanelUpdateId(String toolId)
	{
		// TODO: who should be responsible for "Title" here? It's a Portal thing... -ggolden
		return ComponentManager.get(FormattedText.class).escapeJavascript("Title" + toolId);

	} // titlePanelUpdateId

	/**
	 * Add another string to the alert message.
	 * Defaults to removing duplicates from the alert message
	 * 
	 * @param state
	 *        The session state.
	 * @param message
	 *        The string to add.
	 */

	public static void addAlert(SessionState state, String message) {
		
		addAlert(state, message, true);
	}

	/**
	 * Add another string to the alert message.
	 * 
	 * @param state
	 *        The session state.
	 * @param message
	 *        The string to add.
	 * @param removeDuplicates
	 * 		  Remove duplicates from the alert
	 */
	public static void addAlert(SessionState state, String message, boolean removeDuplicates)
	{
		String soFar = (String) state.getAttribute(STATE_MESSAGE);
		if (soFar == null)
		{
			soFar = message;
		}
		else if (!removeDuplicates || !soFar.contains(message))
		{
			soFar += "<br/>" + message;
		}
		state.setAttribute(STATE_MESSAGE, soFar);

	} // addAlert

	/**
	 * Add another string to the flash notification message.
	 *
	 * @param state
	 *        The session state.
	 * @param message
	 *        The string to add.
	 */
	public static void addFlashNotif(SessionState state, String message)
	{
		String soFar = (String) state.getAttribute(STATE_NOTIF);
		if (soFar != null)
		{
			soFar = soFar + "\n\n" + message;
		}
		else
		{
			soFar = message;
		}
		state.setAttribute(STATE_NOTIF, soFar);

	} // addAlert

	/**
	 * Switch to a new panel
	 * 
	 * @param state
	 *        The session state.
	 * @param newPanel
	 *        The new panel name
	 */
	public static void switchPanel(SessionState state, String newPanel)
	{
		state.setAttribute(STATE_NEW_PANEL, newPanel);

	} // addAlert

	/**
	 * Initialize for the first time the session state for this session. If overridden in a sub-class, make sure to call super.
	 * 
	 * @param state
	 *        The session state.
	 * @param req
	 *        The current portlet request.
	 * @param res
	 *        The current portlet response.
	 */
	protected void initState(SessionState state, HttpServletRequest req, HttpServletResponse res)
	{
		super.initState(state, req, res);

		// call the old initState:
		VelocityPortlet portlet = (VelocityPortlet) req.getAttribute(ATTR_PORTLET);
		JetspeedRunData rundata = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);

		initState(state, portlet, rundata);

	} // initState

	/**
	 * Update for this request processing the session state. If overridden in a sub-class, make sure to call super.
	 * 
	 * @param state
	 *        The session state.
	 * @param req
	 *        The current portlet request.
	 * @param res
	 *        The current portlet response.
	 */
	protected void updateState(SessionState state, HttpServletRequest req, HttpServletResponse res)
	{
		super.updateState(state, req, res);

		// the old way has just initState, so...
		VelocityPortlet portlet = (VelocityPortlet) req.getAttribute(ATTR_PORTLET);
		JetspeedRunData rundata = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);

		initState(state, portlet, rundata);

	} // updateState

	/**
	 * Dispatch to a "do" method based on reflection. Override ToolServlet to support the old "build" ways.
	 * 
	 * @param methodBase
	 *        The base name of the method to call.
	 * @param methodExt
	 *        The end name of the method to call.
	 * @param req
	 *        The HttpServletRequest.
	 * @param res
	 *        The HttpServletResponse
	 */
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		// the context wraps our real vm attribute set
		Context context = (Context) req.getAttribute(ATTR_CONTEXT);

		// other wrappers
		VelocityPortlet portlet = (VelocityPortlet) req.getAttribute(ATTR_PORTLET);
		JetspeedRunData rundata = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);

		// "panel" is used to identify the specific panel in the URL
		context.put("param_panel", ActionURL.PARAM_PANEL);

		// set the "action"
		context.put("action", getState(req).getAttribute(STATE_ACTION));

		// set the "pid"
		context.put("param_pid", ActionURL.PARAM_PID);
		context.put("pid", getPid(req));
		
		String collectionId = contentHostingService.getSiteCollection(ToolManager.getCurrentPlacement().getContext());
		context.put(CONTEXT_SITE_COLLECTION_ID, collectionId);
		
		// indicate which WYSIWYG editor to use in legacy tools
		String editor = EditorConfiguration.getWysiwigEditor();
		
		context.put("sakai_editor", editor);
		context.put("editorConfig", new EditorConfiguration());

		UsageSession session = UsageSessionService.getSession();
		if (session != null)
		{
            // SAK-23047 Set the proper country code in the chef_start generated markup
            String userId = session.getUserId();
            ResourceLoader rl = new ResourceLoader(userId);
            Locale locale = rl.getLocale();
            String languageCode = locale.getLanguage();
            String countryCode = locale.getCountry();
            if(countryCode != null && countryCode.length() > 0) {
                languageCode += "-" + countryCode;
            }
            context.put("language",languageCode);
            context.put("dir", rl.getOrientation(locale));

			String userTheme = "sakaiUserTheme-notSet";
			boolean sakaiThemesEnabled = ServerConfigurationService.getBoolean("portal.themes", true);
			if ( sakaiThemesEnabled ) {
				String thisUser = SessionManager.getCurrentSessionUserId();
				PreferencesService preferencesService = ComponentManager.get(PreferencesService.class);

				Preferences prefs = preferencesService.getPreferences(thisUser);

				if ( prefs != null ) {
					userTheme = StringUtils.defaultIfEmpty(prefs.getProperties(PreferencesService.USER_SELECTED_UI_THEME_PREFS).getProperty("theme"), "sakaiUserTheme-notSet");
				}
			}

			context.put("userTheme", userTheme);

			String browserId = session.getBrowserId();
			if (UsageSession.WIN_IE.equals(browserId) || UsageSession.WIN_MZ.equals(browserId)
					|| UsageSession.WIN_NN.equals(browserId) || UsageSession.MAC_MZ.equals(browserId)
					|| UsageSession.MAC_NN.equals(browserId))
			{
				context.put("wysiwyg", "true");
			}
		}

		try
		{
			// dispatch panels (Note: panel must be in the URL, not the body - this is not from the parsed params)
			String panel = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(ActionURL.PARAM_PANEL);

			/*
			 * TODO: float support from before... // special case for floating and the Main panel: if (LAYOUT_MAIN.equals(panel)) { if (handleFloat(portlet, context, rundata, state)) return; }
			 */
			if (panel == null || "".equals(panel) || "null".equals(panel))
			{
				// default to main panel
				panel = LAYOUT_MAIN;
			} else {
				// sanitize value
				panel = panel.replaceAll("[\r\n]","");
			}

			context.put("panel", panel);

			// form a method name "build" + panel name (first letter caps) + "PanelContext"
			// buildPanelContext( VelocityPortlet, Context, ControllerState, RunData )
			Class[] types = new Class[4];
			types[0] = VelocityPortlet.class;
			types[1] = Context.class;
			types[2] = RunData.class;
			types[3] = SessionState.class;

			// let our extension classes override the pannel name for the method
			String methodName = panelMethodName(panel);

			Method method = getClass().getMethod(methodName, types);

			Object[] args = new Object[4];
			args[0] = portlet;
			args[1] = context;
			args[2] = rundata;
			args[3] = getState(req);
			String template = (String) method.invoke(this, args);

			// if the method did something like a redirect, we don't want to try to put out any more
			if (!res.isCommitted())
			{
				if (template == null)
				{
					// pick the template for the panel - the base + "-" + panel
					template = (String) getContext(rundata).get("template") + "-" + panel;
				}

				// the vm file needs a path and an extension
				if(!template.equals(MODE_PERMISSIONS)) {
					template = "/vm/" + template;
				}
				template += ".vm";

				// setup for old style alert
				StringBuilder buf = new StringBuilder();
				String msg = (String) getState(req).getAttribute(STATE_MESSAGE);
				if (msg != null)
				{
					buf.append(msg);
					getState(req).removeAttribute(STATE_MESSAGE);
				}
				Alert alert = getAlert(req);
				if (!alert.isEmpty())
				{
					buf.append(alert.peekAlert());
					setVmReference(ALERT_ATTR, alert, req);
				}
				if (buf.length() > 0)
				{
					setVmReference("alertMessage", buf.toString(), req);
				}
				//set up for duplicate site alert
				StringBuilder sbNotif = new StringBuilder();
				String sNotif = (String) getState(req).getAttribute(STATE_NOTIF);
				if (sNotif != null)
				{
							sbNotif.append(sNotif);
					getState(req).removeAttribute(STATE_NOTIF);
				}
				if (sbNotif.length() > 0)
				{
					setVmReference("flashNotif", sbNotif.toString(), req);
					setVmReference("flashNotifCloseTitle",rb.getString("flashNotifCloseTitle"),req);
				}

				// setup for old style validator
				setVmReference("validator", m_validator, req);
				setVmReference("formattedText", formattedText, req);

				// set standard no-cache headers
				setNoCacheHeaders(res);

				// add a standard header
				includeVm("chef_header.vm", req, res);

				includeVm(template, req, res);

				// add a standard footer
				includeVm("chef_footer.vm", req, res);
			}
		}
		catch (NoSuchMethodException e)
		{
			try {
				res.sendError(HttpServletResponse.SC_BAD_REQUEST, "NoSuchMethodException for panel name");
			} catch (IOException e1) {
				// ignore
			}
		}
		catch (IllegalAccessException e)
		{
			throw new ToolException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new ToolException(e);
		}
		catch (ServletException e)
		{
			throw new ToolException(e);
		}

	} // toolModeDispatch

	/**
	 * Allow extension classes to control which build method gets called for this pannel
	 * @param panel
	 * @return
	 */
	protected String panelMethodName(String panel)
	{
		return "build" + panel + "PanelContext";
	}

	/**
	 * Process a Portlet action.
	 */
	public void processAction(HttpServletRequest req, HttpServletResponse res)
	{
		// lets use the parsed params
		JetspeedRunData rundata = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);
		ParameterParser params = rundata.getParameters();

		// see if there's an action parameter, whose value has the action to use
		String action = params.get(PARAM_ACTION);

		// if that's not present, see if there's a combination name with the action encoded in the name
		if (action == null)
		{
			Iterator<String> names = params.getNames();
			while (names.hasNext())
			{
				String name = names.next();
				if (name.startsWith(BUTTON))
				{
					action = name.substring(BUTTON.length());
					break;
				}
			}
		}
		else
		{ 
			// SAK-18148 look for first non empty action
			if ("".equals(action))
			{
				String[] actions = params.getStrings(PARAM_ACTION);
				if (actions != null) 
				{
					for (int i = 0; i < actions.length; i++)
					{
						if (!"".equals(actions[i]))
						{
							action = actions[i];
							break;
						}
					}
				}
			}
		}

		// process the action if present
		if (action != null)
		{
			if (!checkCSRFToken(req, rundata, action)) return;
				
			// if we have an active helper, send the action there
			String helperClass = (String) getState(req).getAttribute(STATE_HELPER);
			if (helperClass != null)
			{
				helperActionDispatch("", action, req, res, helperClass);
			}
			else
			{
				actionDispatch("", action, req, res);
			}

			// Handle shortcut return from a tool helper between its post and redirect
			// Helper does this in an Action method:
			// SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);
			// and then returns

			ToolSession toolSession = SessionManager.getCurrentToolSession();
			if (HELPER_MODE_DONE.equals(toolSession.getAttribute(HELPER_LINK_MODE)))
			{
				Tool tool = ToolManager.getCurrentTool();
	
				String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
				toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
				toolSession.removeAttribute(HELPER_LINK_MODE);
	
				if ( url != null ) 
				{
					try
					{
						res.sendRedirect(url);
						return;
					}
					catch (IOException e)
					{
						log.warn("IOException: ", e);
					}
				}
			}

			// Continue non-shortcut processing
			// redirect to the tool's registration's url, with the tool id (pid) and panel
			// Tool tool = (Tool) req.getAttribute(ATTR_TOOL);
			// TODO: redirect url? pannel? placement id?
			String url = Web.returnUrl(req, null);
			
			String panel = ((ParameterParser) req.getAttribute(ATTR_PARAMS)).getString(ActionURL.PARAM_PANEL);
			String newPanel = (String) getState(req).getAttribute(STATE_NEW_PANEL);
			getState(req).removeAttribute(STATE_NEW_PANEL);

			if ( newPanel != null ) panel = newPanel;

			if (panel == null || panel.equals("") || panel.equals("null")) {
				panel = MAIN_PANEL;
			} else {
				// sanitize value
				panel = panel.replaceAll("[\r\n]","");
			}
			String redirect = url + "?" + ActionURL.PARAM_PANEL + "=" + panel;

			try
			{
				//to prevent the 'response already committed' error
				if(!(res.isCommitted())) {
					res.sendRedirect(redirect);
				}
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			log.debug("processAction: no action");
		}

	} // processAction

	public boolean checkCSRFToken(HttpServletRequest request, RunData rundata, String action)
	{
		ParameterParser params = rundata.getParameters();

		// if user if manipulating data via POST, check for presence of CSRF token
		if ("POST".equals(rundata.getRequest().getMethod()))
		{
			// check if tool id is in list of tools to skip the CSRF check
			Placement placement = ToolManager.getCurrentPlacement();
			String toolId = null;
			if (placement != null)
			{
				toolId = placement.getToolId();
			}
			
			boolean skipCSRFCheck = false;
			String[] insecureTools = ServerConfigurationService.getStrings("velocity.csrf.insecure.tools");
			if (toolId != null && insecureTools != null)
			{
				for (int i = 0; i < insecureTools.length; i++)
				{
					if (StringUtils.equalsIgnoreCase(toolId, insecureTools[i]))
					{
						if (log.isDebugEnabled())
						{
							log.debug("Will skip all CSRF checks on toolId=" + toolId);
						}
						skipCSRFCheck = true;
						break;
					}
				}
			}
						
			// if the user is not logged in, then do not worry about csrf
			Session session = SessionManager.getCurrentSession();
			boolean loggedIn = session.getUserId() != null;
			
			if (loggedIn && !skipCSRFCheck)
			{
				// If the attribute is missing, it is likely an internal error,
				// not an error in the tool
				Object sessionAttr = SessionManager.getCurrentSession().getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
				if ( sessionAttr == null )
				{
					log.warn("Missing CSRF Token session attribute: " + action + "; toolId=" + toolId);
					return false;
				}
				
				String csrfToken = params.getString(SAKAI_CSRF_TOKEN);
				String sessionToken = sessionAttr.toString();
				if (csrfToken == null || sessionToken == null || !StringUtils.equals(csrfToken, sessionToken)) 
				{
					log.warn("CSRF Token mismatched or missing on velocity action: " + action + "; toolId=" + toolId);
					return false;
				}
				if (log.isDebugEnabled())
				{
					log.debug("CSRF token (" + csrfToken + ") matches on action: " + action + "; toolId=" + toolId);
				}
			}
		}
		return true;
	}

	/**
	 * Dispatch to a "processAction" method based on reflection.
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
	protected void actionDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
	{
		String methodName = null;
		try
		{
			// the method signature
			Class[] signature = new Class[2];
			signature[0] = RunData.class;
			signature[1] = Context.class;

			// the method name
			methodName = methodBase + methodExt;

			// find a method of this class with this name and signature
			Method method = getClass().getMethod(methodName, signature);

			// parameters - the context for a "do" should not be used, so we will send in null
			Object[] args = new Object[2];
			args[0] = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);
			args[1] = null;

			// make the call
			method.invoke(this, args);
		}
		catch (NoSuchMethodException e)
		{
			// try for a single parameter call
			try
			{
				// the method signature
				Class[] signature = new Class[1];
				signature[0] = RunData.class;

				// the method name
				methodName = methodBase + methodExt;

				// find a method of this class with this name and signature
				Method method = getClass().getMethod(methodName, signature);

				// parameters - the context for a "do" should not be used, so we will send in null
				Object[] args = new Object[1];
				args[0] = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);

				// make the call
				method.invoke(this, args);
			}
			catch (NoSuchMethodException e2)
			{
				log.warn("Exception calling method " + methodName + " " + e2);
			}
			catch (IllegalAccessException e2)
			{
				log.warn("Exception calling method " + methodName + " " + e2);
			}
			catch (InvocationTargetException e2)
			{
				String xtra = "";
				if (e2.getCause() != null) xtra = " (Caused by " + e2.getCause() + ")";
				log.warn("Exception calling method " + methodName + " " + e2 + xtra, e2);

			}
		}
		catch (IllegalAccessException e)
		{
			log.warn("Exception calling method " + methodName + " " + e);
		}
		catch (InvocationTargetException e)
		{
			String xtra = "";
			if (e.getCause() != null) xtra = " (Caused by " + e.getCause() + ")";
			log.warn("Exception calling method " + methodName + " " + e + xtra, e);
		}

	} // actionDispatch

	/**
	 * Dispatch to a "processAction" method based on reflection in a helper class.
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
	protected void helperActionDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res,
			String className)
	{
		String methodName = null;
		try
		{
			// the method signature
			Class[] signature = new Class[1];
			signature[0] = RunData.class;

			// the method name
			methodName = methodBase + methodExt;

			Class cls = Class.forName(className);

			// find a method of this class with this name and signature
			Method method = cls.getMethod(methodName, signature);

			// parameters - the context for a "do" should not be used, so we will send in null
			Object[] args = new Object[1];
			args[0] = (JetspeedRunData) req.getAttribute(ATTR_RUNDATA);

			// make the call
			method.invoke(this, args);
		}
		catch (ClassNotFoundException e)
		{
			log.warn("Exception helper class not found " + e);
		}
		catch (NoSuchMethodException e)
		{
			log.warn("Exception calling method " + methodName + " " + e);
		}
		catch (IllegalAccessException e)
		{
			log.warn("Exception calling method " + methodName + " " + e);
		}
		catch (InvocationTargetException e)
		{
			String xtra = "";
			if (e.getCause() != null) xtra = " (Caused by " + e.getCause() + ")";
			log.warn("Exception calling method " + methodName + " " + e + xtra);
		}

	} // helperActionDispatch

	/**
	 * This is used to get "template" from the map, the default template registered for the tool in chef_tools.xreg.
	 */
	protected Map<String,String> getContext(RunData data)
	{
		// get template from the servlet config
		String template = getServletConfig().getInitParameter("template");
		Map<String,String> rv = new HashMap<String,String>();
		rv.put("template", template);

		return rv;
	}

	// OPTIONS SUPPORT
	public static final String STATE_OBSERVER2 = "obsever2";

	public static final String STATE_PRESENCE_OBSERVER = "presence_observer";

	public static final String STATE_FLOAT = "float";

	public static final String STATE_TOOL = "tool";
	public static final String STATE_TOOL_KEY = "tool_key";
	public static final String STATE_BUNDLE_KEY = "bundle_key";

	public static final String STATE_MESSAGE = "message";
	public static final String STATE_NOTIF = "notification";

	/** Standard modes. */
	public static final String MODE_OPTIONS = "options";
	public static final String MODE_PERMISSIONS = "permissions";

	/**
	 * Handle a request to set options.
	 */
	public void doOptions(RunData runData, Context context)
	{
		// ignore if not allowed
		if (!allowedToOptions())
		{
			return;
			//msg = "you do not have permission to set options for this Worksite.";
		}

		Placement placement = ToolManager.getCurrentPlacement();
		String pid = null;
		if (placement != null) pid = placement.getId();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(pid);

		// go into options mode
		state.setAttribute(STATE_MODE, MODE_OPTIONS);

		// if we're not in the main panel for this tool, schedule an update of the main panel
		String currentPanelId = runData.getParameters().getString(ActionURL.PARAM_PANEL);
		if (!LAYOUT_MAIN.equals(currentPanelId))
		{
			String mainPanelId = mainPanelUpdateId(pid);
			schedulePeerFrameRefresh(mainPanelId);
		}

	} // doOptions

	protected String build_permissions_context(VelocityPortlet portlet, Context context, RunData data, SessionState state) {
		String toolKey = (String) state.getAttribute(STATE_TOOL_KEY);
		context.put("toolKey", toolKey);
		String bundleKey = (String) state.getAttribute(STATE_BUNDLE_KEY);
		if(StringUtils.isNotBlank(bundleKey)){
			context.put("bundleKey", bundleKey);
		}
		context.put("permissions", rb.getString("permissions"));
		return MODE_PERMISSIONS;
	}

	/**
	 * Complete the options process with a save.
	 */
	protected void saveOptions()
	{
		// ask the current placement to save
		Placement placement = ToolManager.getCurrentPlacement();
		if (placement != null)
		{
			placement.save();
		}

	} // saveOptions

	/**
	 * Cancel the options process.
	 */
	protected void cancelOptions()
	{
		// TODO: how to indicate that we need to get clean placement options into the current tool?

	} // cancelOptions

	/**
	 * Add the options to the menu bar, if allowed.
	 * 
	 * @param bar
	 *        The menu bar to add to,
	 * @param ref
	 *        The resource reference to base the security decision upon.
	 */
	protected void addOptionsMenu(Menu bar, JetspeedRunData data) // %%% don't need data -ggolden
	{
		if (allowedToOptions())
		{
			bar.add(new MenuEntry(rb.getString("options"), "doOptions"));
		}

	} // addOptionsMenu

	/**
	 * Check if the current user is allowed to do options for the current context (site based)
	 * @return true if the user is allowed to modify the current context's options, false if not.
	 */
	protected boolean allowedToOptions()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		String context = null;
		if (placement != null) context = placement.getContext();

		// TODO: stolen from site -ggolden
		if (SecurityService.unlock("site.upd", "/site/" + context))
		{
			return true;
		}

		return false;
	}

	/**
	 * Handle the "reset tool" option from the Title bar.
	 */
	public void doReset(RunData runData, Context context)
	{
		// access the portlet element id to find "our" state (i.e. the state for this portlet)
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear this state
		resetTool(state);

		// // make sure the Main panel is updated
		String main = VelocityPortletPaneledAction.mainPanelUpdateId(peid);
		schedulePeerFrameRefresh(main);

	} // doReset

	/**
	 * Reset the tool (state) to "home" conditions. Default here is to clear everything from state.
	 * 
	 * @param state
	 *        The tool's session state.
	 */
	protected void resetTool(SessionState state)
	{
		state.clear();

	} // resetTool

	/**
	 * Add some standard references to the vm context.
	 * 
	 * @param request
	 *        The render request.
	 * @param response
	 *        The render response.
	 */
	@SuppressWarnings("unchecked")
	protected void setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	{
		// pick up all the super ones
		super.setVmStdRef(request, response);

		// add a "$config" which accesses the config service
		setVmReference("config", ServerConfigurationService.getInstance(), request);

		// add the pid
		setVmReference("pid", getPid(request), request);

		// add the css version
		final String query = PortalUtils.getCDNQuery();
		setVmReference("portalCdnQuery", query, request);

		// check for a scheduled peer frame or focus refresh
		ToolSession session = SessionManager.getCurrentToolSession();
		if (session != null)
		{
			if (session.getAttribute(ATTR_TOP_REFRESH) != null)
			{
				setVmReference("topRefresh", Boolean.TRUE, request);
				session.removeAttribute(ATTR_TOP_REFRESH);
			}

			Set<String> ids = (Set<String>) session.getAttribute(ATTR_FRAME_REFRESH);
			if (ids != null)
			{
				setVmReference("frameRefresh", ids, request);
				session.removeAttribute(ATTR_FRAME_REFRESH);
			}

			String focusPath = (String) session.getAttribute(ATTR_FRAME_FOCUS);
			if (focusPath != null)
			{
				setVmReference("focusChange", focusPath, request);
				session.removeAttribute(ATTR_FRAME_FOCUS);
			}
		}
		Tool tool = ToolManager.getCurrentTool();
		if (tool != null)
		{
			setVmReference("toolTitle", tool.getTitle(), request);
		}
	}

	/** A Context bound into the request attributes. */

	protected final static String ATTR_CONTEXT = "sakai.wrapper.context";

	/** A PortletConfig bound into the request attributes. */
	protected final static String ATTR_CONFIG = "sakai.wrapper.config";

	/** A VelocityPortlet bound into the request attributes. */
	protected final static String ATTR_PORTLET = "sakai.wrapper.portlet";

	/** A JetspeedRunData bound into the request attributes. */
	protected final static String ATTR_RUNDATA = "sakai.wrapper.rundata";

	/**
	 * Respond to a request by dispatching to a portlet like "do" method based on the portlet mode and tool mode
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException
	{
		// set in VmServlet
		ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);

		// we will need some covers... Note: parameters are parsed (i.e. files are read) here
		Context context = new Context(this, req);
		Placement placement = ToolManager.getCurrentPlacement();
		PortletConfig config = new PortletConfig(getServletConfig(), placement.getPlacementConfig(), placement.getTool()
				.getRegisteredConfig(), placement);
		VelocityPortlet portlet = new VelocityPortlet(getPid(req), config);
		JetspeedRunData rundata = new JetspeedRunData(req, getState(req), getPid(req), params);

		req.setAttribute(ATTR_CONTEXT, context);
		req.setAttribute(ATTR_CONFIG, config);
		req.setAttribute(ATTR_PORTLET, portlet);
		req.setAttribute(ATTR_RUNDATA, rundata);

		super.doGet(req, res);
	}

	// Set up RunData if it's not already set up    

        protected void checkRunData(HttpServletRequest req)
	{
		if (req.getAttribute(ATTR_RUNDATA) != null)
		    return;

		// set in VmServlet
		ParameterParser params = (ParameterParser) req.getAttribute(ATTR_PARAMS);
		JetspeedRunData rundata = new JetspeedRunData(req, getState(req), getPid(req), params);
		req.setAttribute(ATTR_RUNDATA, rundata);
	}

	/** Tool session attribute name used to schedule a peer frame refresh. */
	public static final String ATTR_FRAME_REFRESH = "sakai.vppa.frame.refresh";

	/** Tool session attribute name used to schedule a whole page refresh. */
	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

	/** Tool session attribute name used to schedule a focus change. */
	public static final String ATTR_FRAME_FOCUS = "sakai.vppa.frame.focus";

	/**
	 * Schedule a refresh for whole page
	 */
	protected void scheduleTopRefresh()
	{
		ToolSession session = SessionManager.getCurrentToolSession();

		// add to (or create) our set of ids to refresh
		if (session.getAttribute(ATTR_TOP_REFRESH) == null)
		{
			session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
		}
	}

	/**
	 * Schedule a refresh for a peer frame.
	 * 
	 * @param id
	 *        The peer frame's id.
	 */
	@SuppressWarnings("unchecked")
	protected void schedulePeerFrameRefresh(String id)
	{
		ToolSession session = SessionManager.getCurrentToolSession();

		// add to (or create) our set of ids to refresh
		Set<String> soFar = (Set<String>) session.getAttribute(ATTR_FRAME_REFRESH);
		if (soFar == null)
		{
			soFar = new HashSet<String>();
			session.setAttribute(ATTR_FRAME_REFRESH, soFar);
		}
		soFar.add(id);
	}

	/**
	 * Schedule a focus change.
	 * 
	 * @param path
	 *        The desired focus path elements
	 */
	protected void scheduleFocusRefresh(String[] path)
	{
		ToolSession session = SessionManager.getCurrentToolSession();

		// make the js string from the elements
		String jsArray = "[";
		for (int i = 0; i < path.length; i++)
		{
			if (i > 0)
			{
				jsArray = jsArray + ",";
			}
			jsArray = jsArray + " \"" + path[i] + "\"";
		}
		jsArray = jsArray + " ]";

		// save it for the next display
		session.setAttribute(ATTR_FRAME_FOCUS, jsArray);
	}
   
   /**
    ** Return a String array containing the "m", "d", "y" characters (corresponding to month, day, year) 
    ** in the locale specific order
    **/
	private static String[] DEFAULT_FORMAT_ARRAY = new String[] {"m","d","y"};
   public String[] getDateFormatString()
   {
      SimpleDateFormat sdf = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT, rb.getLocale());
      String[] formatArray = sdf.toPattern().split("[/\\-\\.]");
      for (int i=0; i<formatArray.length; i++)
         formatArray[i] = formatArray[i].trim().substring(0,1).toLowerCase();
			
      if ( formatArray.length != DEFAULT_FORMAT_ARRAY.length )
      {
         log.warn("Unknown date format string (using default): " 
                    + sdf.toPattern() );
         return DEFAULT_FORMAT_ARRAY;
      }
      else
      {
         return formatArray;
      } 
   }

	private static final String[] DEFAULT_TIME_FORMAT_ARRAY = new String[] {"h", "m", "a"};

	/**
	 ** Return a String array containing the "h", "m", "a", or "H" characters (corresponding to hour, minute, am/pm, or 24-hour)
	 ** in the locale specific order
	 **/
	public String[] getTimeFormatString()
	{
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, rb.getLocale());
		String format = sdf.toPattern();

		Set<String> formatSet = new LinkedHashSet<String>();
		char curChar;
		char lastChar = 0;
		for (int i = 0; i < format.length(); i++)
		{
			curChar = format.charAt(i);
			if ((curChar == 'h' || curChar == 'm' || curChar == 'a' || curChar == 'H') && curChar != lastChar)
			{
				formatSet.add(String.valueOf(curChar));
				lastChar = curChar;
			}
		}

		String[] formatArray = formatSet.toArray(new String[formatSet.size()]);
		if (formatArray.length != DEFAULT_TIME_FORMAT_ARRAY.length
				&& formatArray.length != DEFAULT_TIME_FORMAT_ARRAY.length - 1)
		{
			log.warn("Unknown time format string (using default): " + format);
			return DEFAULT_TIME_FORMAT_ARRAY.clone();
		}
		else
		{
			return formatArray;
		}
	}
} // class VelocityPortletPaneledAction

