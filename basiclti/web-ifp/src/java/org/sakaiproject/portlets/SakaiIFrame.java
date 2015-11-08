/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portlets;

import java.lang.Integer;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.WindowState;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

import javax.servlet.ServletRequest;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;

// Velocity
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.app.VelocityEngine;

import org.sakaiproject.component.cover.ComponentManager;

// lti service
import org.sakaiproject.lti.api.LTIService;


/**
 * a simple SakaiIFrame Portlet
 */
public class SakaiIFrame extends GenericPortlet {

	private static final Log M_log = LogFactory.getLog(SakaiIFrame.class);
	
	private LTIService m_ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");

	// This is old-style internationalization (i.e. not dynamic based
	// on user preference) to do that would make this depend on
	// Sakai Unique APIs. :(
	// private static ResourceBundle rb =  ResourceBundle.getBundle("iframe");
	protected static ResourceLoader rb = new ResourceLoader("iframe");

	protected final FormattedText validator = new FormattedText();

	private final VelocityHelper vHelper = new VelocityHelper();

	VelocityEngine vengine = null;

	private PortletContext pContext;

	// TODO: Perhaps these constancts should come from portlet.xml

	/** The source URL, in state, config and context. */
	protected final static String SOURCE = "source";

	/** The value in state and context for the source URL to actually used, as computed from special and URL. */
	protected final static String URL = "url";

	/** The height, in state, config and context. */
	protected final static String HEIGHT = "height";

	/** The custom height from user input * */
	protected final static String CUSTOM_HEIGHT = "customNumberField";

	protected final String POPUP = "popup";
	protected final String MAXIMIZE = "sakai:maximize";

	protected final static String TITLE = "title";

	private static final String FORM_PAGE_TITLE = "title-of-page";

	private static final String FORM_TOOL_TITLE = "title-of-tool";

	private static final int MAX_TITLE_LENGTH = 99;

	private static String ALERT_MESSAGE = "sakai:alert-message";

	public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";

	// If the property is final, the property wins.  If it is not final,
	// the portlet preferences take precedence.
	public String getTitleString(RenderRequest request)
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getTitle();
	}

	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		pContext = config.getPortletContext();
		try {
			vengine = vHelper.makeEngine(pContext);
		}
		catch(Exception e)
		{
			throw new PortletException("Cannot initialize Velocity ", e);
		}
		M_log.info("iFrame Portlet vengine="+vengine+" rb="+rb);

	}

	private void addAlert(ActionRequest request,String message) {
		PortletSession pSession = request.getPortletSession(true);
		pSession.setAttribute(ALERT_MESSAGE, message);
	}

	private void sendAlert(RenderRequest request, Context context) {
		PortletSession pSession = request.getPortletSession(true);
		String str = (String) pSession.getAttribute(ALERT_MESSAGE);
		pSession.removeAttribute(ALERT_MESSAGE);
		if ( str != null && str.length() > 0 ) context.put("alertMessage", validator.escapeHtml(str, false));
	}

	// Render the portlet - this is not supposed to change the state of the portlet
	// Render may be called many times so if it changes the state - that is tacky
	// Render will be called when someone presses "refresh" or when another portlet
	// onthe same page is handed an Action.
	public void doView(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			response.setContentType("text/html");

			// System.out.println("==== doView called ====");

			// Grab that underlying request to get a GET parameter
			ServletRequest req = (ServletRequest) ThreadLocalManager.get(CURRENT_HTTP_REQUEST);
			String popupDone = req.getParameter("sakai.popup");

			PrintWriter out = response.getWriter();
			Placement placement = ToolManager.getCurrentPlacement();
			response.setTitle(placement.getTitle());
			String source = placement.getPlacementConfig().getProperty(SOURCE);
			if ( source == null ) source = "";
			String height = placement.getPlacementConfig().getProperty(HEIGHT);
			if ( height == null ) height = "1200px";
			boolean maximize = "true".equals(placement.getPlacementConfig().getProperty(MAXIMIZE));

			boolean popup = false; // Comes from content item
			boolean oldPopup = "true".equals(placement.getPlacementConfig().getProperty(POPUP));

			// Retrieve the corresponding content item and tool to check the launch
			Map<String, Object> content = null;
			Map<String, Object> tool = null;
			Long key = getContentIdFromSource(source);
			if ( key == null ) {
				out.println(rb.getString("get.info.notconfig"));
				M_log.warn("Cannot find content id placement="+placement.getId()+" source="+source);
				return;
			}
			try {
				content = m_ltiService.getContent(key);
				// If we are supposed to popup (per the content), do so and optionally
				// copy the calue into the placement to communicate with the portal
				popup = getLongNull(content.get("newpage")) == 1;
				if ( oldPopup != popup ) {
					placement.getPlacementConfig().setProperty(POPUP, popup ? "true" : "false");
					placement.save();
				}
				String launch = (String) content.get("launch");
				Long tool_id = getLongNull(content.get("tool_id"));
				if ( launch == null && tool_id != null ) {
					tool = m_ltiService.getTool(tool_id);
					launch = (String) tool.get("launch");
				}

				// Force http:// to pop-up if we are https://
				String serverUrl = ServerConfigurationService.getServerUrl();
				if ( request.isSecure() || ( serverUrl != null && serverUrl.startsWith("https://") ) ) {
					if ( launch != null && launch.startsWith("http://") ) popup = true;
				}
			} catch (Exception e) {
				out.println(rb.getString("get.info.notconfig"));
				e.printStackTrace();
				return;
			}

			if ( source != null && source.trim().length() > 0 ) {

				Context context = new VelocityContext();
				context.put("tlang", rb);
				context.put("validator", validator);
				context.put("source",source);
				context.put("height",height);
				sendAlert(request,context);
				context.put("popupdone", Boolean.valueOf(popupDone != null));
				context.put("popup", Boolean.valueOf(popup));
				context.put("maximize", Boolean.valueOf(maximize));

				vHelper.doTemplate(vengine, "/vm/main.vm", context, out);
			} else {
				out.println(rb.getString("get.info.notconfig"));
			}

			// System.out.println("==== doView complete ====");
		}

	public void doEdit(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);

			Context context = new VelocityContext();
			context.put("tlang", rb);
			context.put("validator", validator);
			sendAlert(request,context);

			PortletURL url = response.createActionURL();
			context.put("actionUrl", url.toString());
			context.put("doCancel", "sakai.cancel");
			context.put("doUpdate", "sakai.update");

			// get current site
			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = "";

			// find the right LTIContent object for this page
			String source = placement.getPlacementConfig().getProperty(SOURCE);
			Long key = getContentIdFromSource(source);
			if ( key == null ) {
				out.println(rb.getString("get.info.notconfig"));
				M_log.warn("Cannot find content id placement="+placement.getId()+" source="+source);
				return;
			}

			Map<String, Object> content = m_ltiService.getContent(key);
			if ( content == null ) {
				out.println(rb.getString("get.info.notconfig"));
				M_log.warn("Cannot find content item placement="+placement.getId()+" key="+key);
				return;
			}

			// attach the ltiToolId to each model attribute, so that we could have the tool configuration page for multiple tools
			String foundLtiToolId = content.get(m_ltiService.LTI_TOOL_ID).toString();
			Map<String, Object> tool = m_ltiService.getTool(Long.valueOf(foundLtiToolId));
			if ( tool == null ) {
				out.println(rb.getString("get.info.notconfig"));
				M_log.warn("Cannot find tool placement="+placement.getId()+" key="+foundLtiToolId);
				return;
			}

			String[] contentToolModel=m_ltiService.getContentModel(Long.valueOf(foundLtiToolId));
			String formInput=m_ltiService.formInput(content, contentToolModel);
			context.put("formInput", formInput);
			
			vHelper.doTemplate(vengine, "/vm/edit.vm", context, out);
		}

	public void doHelp(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			// System.out.println("==== doHelp called ====");
			// sendToJSP(request, response, "/help.jsp");
			JSPHelper.sendToJSP(pContext, request, response, "/help.jsp");
			// System.out.println("==== doHelp done ====");
		}

	// Process action is called for action URLs / form posts, etc
	// Process action is called once for each click - doView may be called many times
	// Hence an obsession in process action with putting things in session to 
	// Send to the render process.
	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			// System.out.println("==== processAction called ====");

			PortletSession pSession = request.getPortletSession(true);

			// Our first challenge is to figure out which action we want to take
			// The view selects the "next action" either as a URL parameter
			// or as a hidden field in the POST data - we check both

			String doCancel = request.getParameter("sakai.cancel");
			String doUpdate = request.getParameter("sakai.update");

			// Our next challenge is to pick which action the previous view
			// has told us to do.  Note that the view may place several actions
			// on the screen and the user may have an option to pick between
			// them.  Make sure we handle the "no action" fall-through.

			pSession.removeAttribute("error.message");

			if ( doCancel != null ) {
				response.setPortletMode(PortletMode.VIEW);
			} else if ( doUpdate != null ) {
				processActionEdit(request, response);
			} else {
				// System.out.println("Unknown action");
				response.setPortletMode(PortletMode.VIEW);
			}

			// System.out.println("==== End of ProcessAction  ====");
		}

	public void processActionEdit(ActionRequest request, ActionResponse response)
		throws PortletException, IOException 
		{
			// TODO: Check Role

			// Stay in EDIT mode unless we are successful
			response.setPortletMode(PortletMode.EDIT);

			Placement placement = ToolManager.getCurrentPlacement();
			// get the site toolConfiguration, if this is part of a site.
			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
			String id = request.getParameter(LTIService.LTI_ID);
			String toolId = request.getParameter(LTIService.LTI_TOOL_ID);
			Properties reqProps = new Properties();
			Enumeration names = request.getParameterNames();
			while (names.hasMoreElements())
			{
				String name = (String) names.nextElement();
				reqProps.setProperty(name, request.getParameter(name));
			}
			Object retval = m_ltiService.updateContent(Long.parseLong(id), reqProps);
			String fa_icon = (String)request.getParameter(LTIService.LTI_FA_ICON);
			if ( fa_icon != null && fa_icon.length() > 0 ) {
				placement.getPlacementConfig().setProperty("imsti.fa_icon",fa_icon);
			}

			placement.save();

			response.setPortletMode(PortletMode.VIEW);
		}

	/** Valid digits for custom height from user input **/
	protected static final String VALID_DIGITS = "0123456789";

	/* Parse the source URL to extract the content identifier */
	private Long getContentIdFromSource(String source)
	{
		int pos = source.indexOf("/content:");
		if ( pos < 1 ) return null;
		pos = pos + "/content:".length();
		if ( pos < source.length() ) {
			String sContentId = source.substring(pos);
			try {
				Long key = new Long(sContentId);
				return key;
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Check if the string from user input contains any characters other than digits
	 * 
	 * @param height
	 *        String from user input
	 * @return True if all are digits. Or False if any is not digit.
	 */
	private boolean checkDigits(String height)
	{
		for (int i = 0; i < height.length(); i++)
		{
			if (VALID_DIGITS.indexOf(height.charAt(i)) == -1) return false;
		}
		return true;
	}

	private Long getLongNull(Object key) {
		if (key == null) return null;

		if (key instanceof Number)
			return new Long(((Number) key).longValue());

		if (key instanceof String) {
			try {
				return new Long((String) key);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
}
