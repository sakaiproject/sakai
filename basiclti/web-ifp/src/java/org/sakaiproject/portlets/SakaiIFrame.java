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

import java.io.PrintWriter;
import java.io.IOException;

import java.util.Map;
import java.util.Properties;
import java.util.Enumeration;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

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
@Slf4j
public class SakaiIFrame extends GenericPortlet {
	
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
		log.info("iFrame Portlet vengine={} rb={}", vengine, rb);

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

			log.debug("==== doView called ====");

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
				log.warn("Cannot find content id placement={} source={}", placement.getId(), source);
				return;
			}
			try {
				content = m_ltiService.getContent(key, placement.getContext());
				// SAK-32665 - We get null when an LTI tool is added to a template
				// like !user because the content item points at !user and not the
				// current site.
				if ( content == null ) {
					content = patchContentItem(key, placement);
					source = placement.getPlacementConfig().getProperty(SOURCE);
					key = getContentIdFromSource(source);
				}

				// If content is still null after patching, let the NPE happen
				Long tool_id = getLongNull(content.get("tool_id"));
				// If we are supposed to popup (per the content), do so and optionally
				// copy the calue into the placement to communicate with the portal
				if (tool_id != null) {
					tool = m_ltiService.getTool(tool_id, placement.getContext());
					m_ltiService.filterContent(content, tool);
				}
				Object popupValue = content.get("newpage");
				popup = getLongNull(popupValue) == 1;
				if ( oldPopup != popup ) {
					placement.getPlacementConfig().setProperty(POPUP, popup ? "true" : "false");
					placement.save();
				}
				String launch = (String) content.get("launch");
				// Force http:// to pop-up if we are https://
				String serverUrl = ServerConfigurationService.getServerUrl();
				if ( request.isSecure() || ( serverUrl != null && serverUrl.startsWith("https://") ) ) {
					if ( launch != null && launch.startsWith("http://") ) popup = true;
				}
			} catch (Exception e) {
				out.println(rb.getString("get.info.notconfig"));
				log.error(e.getMessage(), e);
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
		}


	/**
	 * Patch the content item if it was copied from the !user template.
	 *
	 * We only do this once is there is a source, and we cannot get it,
	 * we either make a new content item from the tool or we empty the
	 * source property.
	 */
	private Map<String, Object> patchContentItem(Long key, Placement placement)
	{
		// Get out tool configuration so we can fix things up...
		ToolConfiguration toolConfig = SiteService.findTool(placement.getId());

		// Look up the content item, bypassing authz checks
		Map<String, Object> content = m_ltiService.getContentDao(key);
		if ( content == null ) return null;
		Long tool_id = getLongNull(content.get("tool_id"));

		// Look up the tool associated with the Content Item
		// checking Authz to see is we can touch this tool
		Map<String, Object> tool = m_ltiService.getTool(tool_id, placement.getContext());
		if ( tool == null ) return null;

		// Now make a content item from this tool inheriting from the other content item
		Properties props = new Properties();
		for (Map.Entry<String, Object> entry : content.entrySet()) {
			String k = entry.getKey();
			Object value = entry.getValue();
			if ( value == null ) continue;
			if ( k.endsWith("_at") ) continue;
			props.put(k, value.toString());
		}
		props.put(LTIService.LTI_TOOL_ID, tool_id.toString());
		props.put(LTIService.LTI_SITE_ID, placement.getContext());
		props.put(LTIService.LTI_PLACEMENT, placement.getId());

		Object retval = m_ltiService.insertContent(props, placement.getContext());
		if ( retval instanceof String ) {
			log.error("Unable to insert LTILinkItem tool={} placement={}",tool_id,placement.getId());
			placement.getPlacementConfig().setProperty(SOURCE,"");
			placement.save();
			return null;
		}

		Long contentKey = (Long) retval;
		Map<String,Object> newContent = m_ltiService.getContent(contentKey, placement.getContext());
		String contentUrl = m_ltiService.getContentLaunch(newContent);
		if ( newContent == null || contentUrl == null ) {
			log.error("Unable to set contentUrl tool={} placement={}",tool_id,placement.getId());
			placement.getPlacementConfig().setProperty(SOURCE,"");
			placement.save();
			return null;
		}
		placement.getPlacementConfig().setProperty(SOURCE,contentUrl);
		placement.save();

		log.debug("Patched contentUrl tool={} placement={} url={}",tool_id,placement.getId(),contentUrl);

		return newContent;
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
				log.warn("Cannot find content id placement={} source={}", placement.getId(), source);
				return;
			}

			Map<String, Object> content = m_ltiService.getContent(key, placement.getContext());
			if ( content == null ) {
				out.println(rb.getString("get.info.notconfig"));
				log.warn("Cannot find content item placement={} key={}", placement.getId(), key);
				return;
			}

			// attach the ltiToolId to each model attribute, so that we could have the tool configuration page for multiple tools
			String foundLtiToolId = content.get(m_ltiService.LTI_TOOL_ID).toString();
			Map<String, Object> tool = m_ltiService.getTool(Long.valueOf(foundLtiToolId), placement.getContext());
			if ( tool == null ) {
				out.println(rb.getString("get.info.notconfig"));
				log.warn("Cannot find tool placement={} key={}", placement.getId(), foundLtiToolId);
				return;
			}

			String[] contentToolModel=m_ltiService.getContentModel(Long.valueOf(foundLtiToolId), placement.getContext());
			String formInput=m_ltiService.formInput(content, contentToolModel);
			context.put("formInput", formInput);
			
			vHelper.doTemplate(vengine, "/vm/edit.vm", context, out);
		}

	public void doHelp(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			log.debug("==== doHelp called ====");
			// sendToJSP(request, response, "/help.jsp");
			JSPHelper.sendToJSP(pContext, request, response, "/help.jsp");
			log.debug("==== doHelp done ====");
		}

	// Process action is called for action URLs / form posts, etc
	// Process action is called once for each click - doView may be called many times
	// Hence an obsession in process action with putting things in session to 
	// Send to the render process.
	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			log.debug("==== processAction called ====");

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
				log.debug("Unknown action");
				response.setPortletMode(PortletMode.VIEW);
			}

			log.debug("==== End of ProcessAction  ====");
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
			Object retval = m_ltiService.updateContent(Long.parseLong(id), reqProps, placement.getContext());
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
