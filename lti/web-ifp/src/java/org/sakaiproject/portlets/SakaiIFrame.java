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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.List;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
// lti service
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.portlet.util.JSPHelper;
import org.sakaiproject.portlet.util.VelocityHelper;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

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

	protected final FormattedText formattedText = ComponentManager.get(FormattedText.class);

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
		if ( str != null && str.length() > 0 ) context.put("alertMessage", formattedText.escapeHtml(str, false));
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

			PrintWriter out = response.getWriter();
			Placement placement = ToolManager.getCurrentPlacement();
			response.setTitle(placement.getTitle());
			String source = placement.getPlacementConfig().getProperty(SOURCE);
			if ( source == null ) source = "";
			String height = placement.getPlacementConfig().getProperty(HEIGHT);
			if ( height == null ) height = "1200px";

			// SAK-47878 - Cleanup legacy popup value if we can since we don't want to use the portal for popup
			String popup = placement.getPlacementConfig().getProperty("popup");
			if ( "true".equals(popup) ) {
				placement.getPlacementConfig().setProperty("popup", "false");
				placement.save();
			}

			boolean newpage = false;

			// Retrieve the corresponding content item and tool to check the launch
			Map<String, Object> content = null;
			Map<String, Object> tool = null;
			Long contentId = getContentIdFromSource(source);
			if ( contentId == null ) {
				out.println(rb.getString("get.info.notconfig"));
				log.warn("Cannot find content id placement={} source={}", placement.getId(), source);
				return;
			}
			try {
				content = m_ltiService.getContent(contentId, placement.getContext());
				// SAK-32665 - We get null when an LTI tool is added to a template
				// like !user because the content item points at !user and not the
				// current site.
				if ( content == null ) {
					content = patchContentItem(contentId, placement);
					source = placement.getPlacementConfig().getProperty(SOURCE);
				}

				if ( content == null ) {
					out.println(rb.getString("get.info.notconfig"));
					return;
				}

				Long tool_id = getLongNull(content.get(LTIService.LTI_TOOL_ID));
				if (tool_id != null) {
					tool = m_ltiService.getTool(tool_id, placement.getContext());
					m_ltiService.filterContent(content, tool);
				}

				height = SakaiLTIUtil.getFrameHeight(tool, content, height);
				newpage = SakaiLTIUtil.getNewpage(tool, content, newpage);

				String launch = (String) content.get("launch");
				// Force http:// to pop-up if we are https://
				String serverUrl = ServerConfigurationService.getServerUrl();
				if ( request.isSecure() || ( serverUrl != null && serverUrl.startsWith("https://") ) ) {
					if ( launch != null && launch.startsWith("http://") ) newpage = true;
				}
			} catch (Exception e) {
				out.println(rb.getString("get.info.notconfig"));
				log.error(e.getMessage(), e);
				return;
			}

			if ( source != null && source.trim().length() > 0 ) {

				Context context = new VelocityContext();
				context.put("tlang", rb);
				context.put("validator", formattedText);
				context.put("source",source);
				context.put("height",height);
				context.put("newpage", Boolean.valueOf(newpage));
				context.put("browser-feature-allow", ServerConfigurationService.getBrowserFeatureAllowString());
				sendAlert(request,context);

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
	private Map<String, Object> patchContentItem(Long contentId, Placement placement)
	{
		final boolean isSuperUser = SecurityService.isSuperUser();

		// Look up the content item, bypassing authz checks
		Map<String, Object> content = m_ltiService.getContentDao(contentId);
		if ( content == null ) return null;
		Long tool_id = getLongNull(content.get("tool_id"));

		// Look up the tool associated with the Content Item
		// checking Authz to see is we can touch this tool
		String siteId = placement.getContext();
		Map<String, Object> tool = m_ltiService.getTool(tool_id, siteId);

		// If this is an admin action, create a new copy of the tool
		if ( tool == null && isSuperUser ) {
			tool = m_ltiService.getToolDao(tool_id, null, true);
			if (tool != null) {
				// Clean up the tool before attempting to duplicate it
				tool.remove(LTIService.LTI_CREATED_AT);
				tool.remove(LTIService.LTI_UPDATED_AT);
				tool.put(LTIService.LTI_SITE_ID, siteId);

				Object retval = m_ltiService.insertToolDao(tool, siteId, true, true);
				if (retval instanceof String) {
					log.error("Unable to create new tool id: {}, site: {}", tool_id, siteId);
					return null;
				}
				else if (retval instanceof Long){
					// Load the newly-duplicated lti_tool
					tool_id = (long) retval;
					tool = m_ltiService.getToolDao(tool_id, null, true);
					log.info("Copied tool_id {} into site {}", tool_id, siteId);
				}
				else {
					log.error("Attempted to copy tool, siteId: {}, retval: {}", siteId, retval);
					return null;
				}
			}
		}
		// Don't think we are willing to copy a tool for a non-admin user
		else if ( tool == null ) {
			return null;
		}

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
		props.put(LTIService.LTI_SITE_ID, siteId);
		props.put(LTIService.LTI_PLACEMENT, placement.getId());

		// The current user may not be a maintainer in the current site, but we want to still be able to
		// correct the source on the LTI tool
		Object retval = m_ltiService.insertContentDao(props, siteId, (isSuperUser || m_ltiService.isAdmin(siteId)), true);
		if ( retval == null || retval instanceof String ) {
			log.error("Unable to insert LTILinkItem tool={} placement={}",tool_id,placement.getId());
			placement.getPlacementConfig().setProperty(SOURCE,"");
			placement.save();
			return null;
		}

		Long contentKey = (Long) retval;
		Map<String,Object> newContent = m_ltiService.getContent(contentKey, siteId);
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
			context.put("validator", formattedText);
			sendAlert(request,context);

			PortletURL url = response.createActionURL();
			context.put("actionUrl", url.toString());
			context.put("doCancel", "sakai.cancel");
			context.put("doUpdate", "sakai.update");
			context.put("doChoose", "sakai.choose");

			// get current site
			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			// find the right LTIContent object for this page
			String source = placement.getPlacementConfig().getProperty(SOURCE);

			// In case we need a tool picker
			List<Map<String, Object>> tools = m_ltiService.getToolsLaunch(siteId);
			context.put("tools", tools);

			Long key = getContentIdFromSource(source);
			if ( key == null ) {
				log.warn("Cannot find content id placement={} source={}", placement.getId(), source);
				vHelper.doTemplate(vengine, "/vm/pick.vm", context, out);
				return;
			}

			Map<String, Object> content = m_ltiService.getContent(key, placement.getContext());
			if ( content == null ) {
				log.warn("Cannot find content item placement={} key={}", placement.getId(), key);
				vHelper.doTemplate(vengine, "/vm/pick.vm", context, out);
				return;
			}

			// attach the ltiToolId to each model attribute, so that we could have the tool configuration page for multiple tools
			String foundLtiToolId = content.get(m_ltiService.LTI_TOOL_ID).toString();
			Map<String, Object> tool = m_ltiService.getTool(Long.valueOf(foundLtiToolId), placement.getContext());
			if ( tool == null ) {
				log.warn("Cannot find tool placement={} key={}", placement.getId(), foundLtiToolId);
				vHelper.doTemplate(vengine, "/vm/pick.vm", context, out);
				return;
			}

			String[] contentToolModel = m_ltiService.getContentModelIfConfigurable(Long.valueOf(foundLtiToolId), placement.getContext());
			if (contentToolModel != null) {
				String formInput = m_ltiService.formInput(content, contentToolModel);
				context.put("formInput", formInput);
			} else {
				String noCustomizations = rb.getString("gen.info.nocustom");
				context.put("noCustomizations", noCustomizations);
			}
			
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
			String doChoose = request.getParameter("sakai.choose");

			// Our next challenge is to pick which action the previous view
			// has told us to do.  Note that the view may place several actions
			// on the screen and the user may have an option to pick between
			// them.  Make sure we handle the "no action" fall-through.

			pSession.removeAttribute("error.message");

			if ( doCancel != null ) {
				response.setPortletMode(PortletMode.VIEW);
			} else if ( doUpdate != null || doChoose != null ) {
				processActionEdit(request, response);
			} else {
				log.debug("Unknown action");
				response.setPortletMode(PortletMode.VIEW);
			}

			log.debug("==== End of ProcessAction  ====");
		}

	public void processActionEdit(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			// TODO: Check Role

			// Stay in EDIT mode unless we are successful
			response.setPortletMode(PortletMode.EDIT);

			String doChoose = request.getParameter("sakai.choose");

			// get current placement and site
			Placement placement = ToolManager.getCurrentPlacement();
			String siteId = placement.getContext();

			String id = request.getParameter(LTIService.LTI_ID);
			String toolIdStr = request.getParameter(LTIService.LTI_TOOL_ID);
			Long toolId = NumberUtils.toLong(toolIdStr, new Long(-1));

			if ( doChoose != null && id == null ) {
				if ( toolId < 1 ) {
					addAlert(request, rb.getString("edit.please.select"));
					return;
				}

				Properties reqProps = new Properties();
				Object retval = m_ltiService.insertToolContent(id, toolIdStr, reqProps, siteId);
				if (retval instanceof String) {
					addAlert(request, (String) retval);
					return;
				} else if ( retval instanceof Long ) {
					Long contentKey = (Long) retval;
					Map<String,Object> newContent = m_ltiService.getContent(contentKey, siteId);
					String contentUrl = m_ltiService.getContentLaunch(newContent);
					if ( newContent == null || contentUrl == null ) {
						log.error("Unable to set contentUrl tool={} placement={}",toolIdStr,placement.getId());
						addAlert(request, rb.getString("edit.save.url.fail"));
						return;
					}
					placement.getPlacementConfig().setProperty(SOURCE,contentUrl);
					placement.save();
					response.setPortletMode(PortletMode.VIEW);
				} else {
					log.error("Unexpected return type from insertToolContent={} tool={} placement={}",retval, toolIdStr,placement.getId());
					addAlert(request, rb.getString("edit.save.retval.fail")+retval);
				}

				return;
			}

			// Update our source and its data
			Properties reqProps = new Properties();
			Enumeration<String> names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				reqProps.setProperty(name, request.getParameter(name));
			}

			m_ltiService.updateContent(Long.parseLong(id), reqProps, placement.getContext());
			String fa_icon = (String) request.getParameter(LTIService.LTI_FA_ICON);
			if ( fa_icon != null && fa_icon.length() > 0 ) {
				placement.getPlacementConfig().setProperty("imsti.fa_icon",fa_icon);
			}

			// get the site toolConfiguration, if this is part of a site.
			ToolConfiguration toolConfig = SiteService.findTool(placement.getId());

			String title = reqProps.getProperty("title");
			if (StringUtils.isNotBlank(title)) {
				// Set the title for the page
				toolConfig.getContainingPage().setTitleCustom(true);
				toolConfig.getContainingPage().setTitle(title);

				try {
					SiteService.save(SiteService.getSite(toolConfig.getSiteId()));
				} catch (Exception e) {
					log.error("Failed to save site", e);
				}
			}

			placement.save();

			response.setPortletMode(PortletMode.VIEW);
		}

	/** Valid digits for custom height from user input **/
	protected static final String VALID_DIGITS = "0123456789";

	/* Parse the source URL to extract the content identifier */
	private Long getContentIdFromSource(String source)
	{
		if ( source == null ) return null;
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
