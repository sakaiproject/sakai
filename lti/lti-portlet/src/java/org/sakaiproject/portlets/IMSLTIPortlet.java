/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.portlets;

import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_ALLOWROSTER;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_ASSIGNMENT;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_KEY;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_ON;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_PLACEMENTSECRET;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_RELEASEEMAIL;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_RELEASENAME;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI_PORTLET_TOOLTITLE;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletRequest;

import org.sakaiproject.lti.LocalEventTrackingService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.lti.util.SimpleEncryption;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portlet.util.PortletHelper;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.tsugi.lti.LTIUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * a simple IMSLTIPortlet Portlet
 */
@SuppressWarnings("deprecation")
@Slf4j
public class IMSLTIPortlet extends GenericPortlet {

	private static ResourceLoader rb = new ResourceLoader("basiclti");

	private PortletContext pContext;

	private ArrayList<String> fieldList = new ArrayList<String>();

	public static final String EVENT_LTI_CONFIG = "basiclti.config";

	private static String LEAVE_SECRET_ALONE = "__dont_change_secret__";

	public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";

	public final static String CURRENT_HTTP_REQUEST = "org.sakaiproject.util.RequestFilter.http_request";

	public static final String SITE_NAME = "ui.service";

	public static final String SAKAI = "Sakai";

	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		pContext = config.getPortletContext();

		// Populate the list of fields
		fieldList.add("launch");
		fieldList.add("secret");
		fieldList.add(LTI_PORTLET_KEY);
		fieldList.add("frameheight");
		fieldList.add("toolorder");
		fieldList.add("debug");
		fieldList.add("description");
		fieldList.add(LTI_PORTLET_TOOLTITLE);
		fieldList.add("custom");
		fieldList.add(LTI_PORTLET_RELEASENAME);
		fieldList.add(LTI_PORTLET_RELEASEEMAIL);
		fieldList.add(LTI_PORTLET_ASSIGNMENT);
		fieldList.add("newpage");
		fieldList.add(LTI_PORTLET_ALLOWROSTER);
		fieldList.add("splash");
		fieldList.add("fa_icon");
	}

	// If the property is final, the property wins.  If it is not final,
	// the portlet preferences take precedence.
	public String getTitleString(RenderRequest request)
	{
		return getCorrectProperty(request, "tooltitle", null);
	}

	// Render the portlet - this is not supposed to change the state of the portlet
	// Render may be called many times so if it changes the state - that is tacky
	// Render will be called when someone presses "refresh" or when another portlet
	// on the same page is handed an Action.
	public void doView(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {

			log.debug("==== doView called ====");

			response.setContentType("text/html; charset=UTF-8");

			// Grab that underlying request to get a GET parameter
			ServletRequest req = (ServletRequest) ThreadLocalManager.get(CURRENT_HTTP_REQUEST);

			PrintWriter out = response.getWriter();

			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);

			String context = getContext();
			Placement placement = ToolManager.getCurrentPlacement();

			// Get the properties
			Properties sakaiProperties = getSakaiProperties();
			String placementSecret = getSakaiProperty(sakaiProperties,"imsti."+LTI_PORTLET_PLACEMENTSECRET);
			String allowRoster = getSakaiProperty(sakaiProperties,"imsti."+LTI_PORTLET_ALLOWROSTER);
			String assignment = getSakaiProperty(sakaiProperties,"imsti."+LTI_PORTLET_ASSIGNMENT);
			String launch = getSakaiProperty(sakaiProperties,"imsti.launch");

			if ( placementSecret == null &&
			   ( SakaiLTIUtil.outcomesEnabled() ||
				 LTI_PORTLET_ON.equals(allowRoster) ) ) {
				String uuid = UUID.randomUUID().toString();
				Date date = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
				String date_secret = sdf.format(date);
				placement.getPlacementConfig().setProperty("imsti."+LTI_PORTLET_PLACEMENTSECRET, uuid);
				placement.getPlacementConfig().setProperty("imsti.placementsecretdate", date_secret);
				placement.save();
			}

			// Check to see if our launch will be successful
			String[] retval = SakaiLTIUtil.postLaunchHTML(placement.getId(), rb);
			if ( retval.length > 1 ) {
				String iframeUrl = "/access/lti/site/"+context+"/"+placement.getId();
				String frameHeight =  getCorrectProperty(request, "frameheight", null);
				log.debug("fh={}", frameHeight);
				String newPage =  getCorrectProperty(request, "newpage", null);
				String serverUrl = SakaiLTIUtil.getOurServerUrl();
				boolean forcePopup = false;
				if ( request.isSecure() || ( serverUrl != null && serverUrl.startsWith("https://") ) ) {
					if ( launch != null && launch.startsWith("http://") ) {
						forcePopup = true;
					}
				}

				// Change "newpage" if forcePopup so the portal will do our pop up next time
				if ( forcePopup && ! LTI_PORTLET_ON.equals(newPage) ) {
					placement.getPlacementConfig().setProperty("imsti.newpage",LTI_PORTLET_ON);
					placement.save();
				}

				String maximize =  getCorrectProperty(request, "maximize", null);
				StringBuffer text = new StringBuffer();

				Session session = SessionManager.getCurrentSession();
				session.setAttribute("sakai:maximized-url",iframeUrl);
				log.debug("Setting sakai:maximized-url={}", iframeUrl);

				if ( LTI_PORTLET_ON.equals(newPage) || forcePopup ) {
					String windowOpen = "window.open('"+iframeUrl+"','LTI');"; 			
					String siteName = ServerConfigurationService.getString(SITE_NAME, SAKAI);
					title = title!=null ? title : rb.getString("tool.name", "your tool");
					String newPageLaunchText = rb.getFormattedMessage("new.page.launch", new Object[]{ComponentManager.get(FormattedText.class).escapeHtml(title, false), ComponentManager.get(FormattedText.class).escapeHtml(siteName, false)});
					text.append(newPageLaunchText);
					text.append("</p>\n");
					text.append("<input type=\"submit\" onclick=\""+windowOpen+"\" target=\"LTI\" value=\""+rb.getString("launch.go.to")+ title + "\"/>");
				} else {
					if ( LTI_PORTLET_ON.equals(maximize) ) {
						text.append("<script type=\"text/javascript\" language=\"JavaScript\">\n");
						text.append("try { portalMaximizeTool(); } catch (err) { }\n");
						text.append("</script>\n");
					}
			                String submit_uuid = UUID.randomUUID().toString().replace("-","_");
					String allowAttr = ServerConfigurationService.getBrowserFeatureAllowString();
					text.append("<iframe id=\"LtiLaunchFrame_");
					text.append(submit_uuid);
					text.append("\" height=\"");
					if ( frameHeight == null ) frameHeight = "1200";
					text.append(frameHeight);
					text.append("\" \n");
					text.append("width=\"100%\" frameborder=\"0\" marginwidth=\"0\"\n");
					text.append("marginheight=\"0\" scrolling=\"auto\"\n");
					text.append(" allowfullscreen=\"true\" webkitallowfullscreen=\"true\" mozallowfullscreen=\"true\"\n");
					text.append(" allow=\"").append(allowAttr).append("\"\n");
					text.append("src=\""+iframeUrl+"\">\n");
					text.append(rb.getString("noiframes"));
					text.append("<br>");
					text.append("<a href=\""+iframeUrl+"\">");
					text.append(rb.getString("noiframe.press.here"));
					text.append("</a>\n");
					text.append("</iframe>\n");

					// Add support for lti resize messages
					text.append("<script>\n");
					text.append("window.addEventListener('message', function(e) {\n");
					text.append("  try {\n");
					text.append("    var message = JSON.parse(e.data);\n");
					text.append("    var idval = 'LtiLaunchFrame_");
					text.append(submit_uuid);
					text.append("';\n");
					text.append("    if ( message.subject == 'lti.frameResize' ) {\n");
					text.append("      var height = message.height;\n");
					text.append("      document.getElementById(idval).height = height;\n");
					text.append("      window.console && console.debug('Received lti.frameResize height='+height);\n");
					text.append("    }\n");
					text.append("    else if ( message.subject == 'lti.pageRefresh' ) {\n");
					text.append("      location.reload(true);\n");
					text.append("    }\n");
					text.append("  } catch (error) {\n");
					text.append("   window.console && console.debug('lti.frameResize of '+idval+' failed height='+height);\n");
					text.append("   window.console && console.debug(e.data);\n");
					text.append("  }\n");
					text.append("});\n");
					text.append("</script>\n");
				}
				out.println(text);
				log.debug("==== doView complete ====");
				return;
			} else {
				out.println(rb.getString("not.configured"));
			}

			clearErrorMessage(request);
			log.debug("==== doView complete ====");
		}

	// Prepare the edit screen with data
	public void prepareEdit(RenderRequest request)
	{
		// Hand up the tool properties
		Placement placement = ToolManager.getCurrentPlacement();
		Properties config = placement.getConfig();
		log.debug("placement={}", placement.getId());
		log.debug("placement.toolId={}", placement.getToolId());
		log.debug("properties={}", config);
		for (String element : fieldList) {
			String propertyName = placement.getToolId() + "." + element;
			String propValue = ServerConfigurationService.getString(propertyName,null);
			if ( "splash".equals(element) && propValue == null ) {
				propValue = ServerConfigurationService.getString(placement.getToolId() + ".overridesplash",null);
			}
			if ( propValue != null && propValue.trim().length() > 0 ) {
				log.debug("Forcing Final = {}", propertyName);
				config.setProperty("final."+element,"true");
			}
		}
		request.setAttribute("imsti.properties", config);

		// Hand up the old values
		Properties oldValues = new Properties();
		Map map = getErrorMap(request);
		String errorMsg = getErrorMessage(request);
		request.setAttribute("error.message", errorMsg);
		addProperty(oldValues, request, "launch", "");
		for (String element : fieldList) {
			if ( "launch".equals(element) ) continue;
			String propKey = "imsti."+element;
			// addProperty(oldValues, request, element, null);
			String propValue = getCorrectProperty(request, element, null);

			if ( map != null ) {
				if ( map.containsKey(propKey) ) {
					Object obj = null;
					try {
						String[] arr = (String []) map.get(propKey);
						obj = arr[0];
					} catch(Exception e) {
						obj = null;
					}
					if ( obj instanceof String ) propValue = (String) obj;
				}
			}
			if ( propValue != null ) {
				if ( "secret".equals(element)) {
					propValue = LEAVE_SECRET_ALONE;
				}
				oldValues.setProperty(propKey, ComponentManager.get(FormattedText.class).escapeHtml(propValue,false));
			}
		}

		request.setAttribute("imsti.oldvalues", oldValues);

		String allowRoster = ServerConfigurationService.getString(SakaiLTIUtil.LTI_ROSTER_ENABLED, SakaiLTIUtil.LTI_ROSTER_ENABLED_DEFAULT);
		request.setAttribute("allowRoster", new Boolean("true".equals(allowRoster)));

		// For outcomes we check for tools in the site before offering the options
		String allowOutcomes = ServerConfigurationService.getString(SakaiLTIUtil.LTI_OUTCOMES_ENABLED, SakaiLTIUtil.LTI_OUTCOMES_ENABLED_DEFAULT);

		GradingService gradingService = (GradingService) ComponentManager.get("org.sakaiproject.grading.api.GradingService");
		request.setAttribute("isGradebookGroupEnabled", gradingService.isGradebookGroupEnabled(getContext()));

		boolean foundLessons = false;
		boolean foundGradebook = false;
		ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
		try {
			Site site = SiteService.getSite(toolConfig.getSiteId());
			for (SitePage page : (List<SitePage>)site.getPages()) {
				for(ToolConfiguration tool : (List<ToolConfiguration>) page.getTools()) {
					String tid = tool.getToolId();
					if ( "sakai.lessonbuildertool".equals(tid) ) foundLessons = true;
					if ( tid != null && tid.startsWith("sakai.gradebook") ) foundGradebook = true;
				}
			}
		} catch (IdUnusedException ex) {
			log.warn("Could not load site.", ex);
		}

		if ( ! foundGradebook ) allowOutcomes = "false";

		request.setAttribute("allowOutcomes", new Boolean("true".equals(allowOutcomes)));
		if ( "true".equals(allowOutcomes) ) {
			List<String> assignments = getGradeBookAssignments();
			if ( assignments != null && assignments.size() > 0 ) request.setAttribute("assignments", assignments);
		}

		clearErrorMessage(request);
	}

	public void addProperty(Properties values, RenderRequest request,
			String propName, String defaultValue)
	{
		String propValue = getCorrectProperty(request, propName, defaultValue);
		if ( propValue != null ) {
			values.setProperty("imsti."+propName,propValue);
		}
	}

	// Get Property - Precedence is frozen server configuration, sakai tool properties,
	//     portlet preferences, sakai tool properties, and then default
	public String getCorrectProperty(PortletRequest request, String propName, String defaultValue)
	{
		Placement placement = ToolManager.getCurrentPlacement();
		String propertyName = placement.getToolId() + "." + propName;
		String propValue = ServerConfigurationService.getString(propertyName,null);
		if ( propValue != null && propValue.trim().length() > 0 ) {
			log.debug("Sakai.home {}={}", propName, propValue);
			return propValue;
		}

		Properties config = placement.getConfig();
		propValue = getSakaiProperty(config, "imsti."+propName);
		if ( propValue != null && "true".equals(config.getProperty("final."+propName)) )
		{
			log.debug("Frozen {} ={}", propName, propValue);
			return propValue;
		}

		PortletPreferences prefs = request.getPreferences();
		propValue = prefs.getValue("imsti."+propName, null);
		if ( propValue != null ) {
			log.debug("Portlet {} ={}", propName, propValue);
			return propValue;
		}

		propValue = getSakaiProperty(config, "imsti."+propName);
		if ( propValue != null ) {
			log.debug("Tool {} ={}", propName, propValue);
			return propValue;
		}

		if ( defaultValue != null ) {
			log.debug("Default {} ={}", propName, defaultValue);
			return defaultValue;
		}
		log.debug("Fell through {}", propName);
		return null;
	}

	// isPropertyFinal() - if it comes from the Server configuration or
	//     the final.propName is set to true
	public boolean isPropertyFinal(String propName)
	{
		Placement placement = ToolManager.getCurrentPlacement();
		String propertyName = placement.getToolId() + "." + propName;
		String propValue = ServerConfigurationService.getString(propertyName,null);
		if ( propValue != null && propValue.trim().length() > 0 ) {
			return true;
		}

		Properties config = placement.getConfig();
		propValue = getSakaiProperty(config, "imsti."+propName);
		if ( propValue != null && "true".equals(config.getProperty("final."+propName)) )
		{
			return true;
		}
		return false;
	}

	public void doEdit(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {

			response.setContentType("text/html");
			log.debug("==== doEdit called ====");

			PortletSession pSession = request.getPortletSession(true);

			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);

			// Debug
			String inputData = (String) pSession.getAttribute("sakai.descriptor");
			if ( inputData != null ) log.debug("descriptor.length()={}", inputData.length());
			String url = (String) pSession.getAttribute("sakai.url");
			log.debug("sakai.url={}", url);

			String view = (String) pSession.getAttribute("sakai.view");
			log.debug("sakai.view={}", view);
			if ( "edit.reset".equals(view) ) {
				sendToJSP(request, response, "/editreset.jsp");
			} else {
				prepareEdit(request);
				sendToJSP(request, response, "/edit.jsp");
			}

			clearErrorMessage(request);
			log.debug("==== doEdit called ====");
		}

	public void doHelp(RenderRequest request, RenderResponse response)
		throws PortletException, IOException {
			log.debug("==== doHelp called ====");

			String title = getTitleString(request);
			if ( title != null ) response.setTitle(title);
			sendToJSP(request, response, "/help.jsp");

			clearErrorMessage(request);
			log.debug("==== doHelp done  ====");
		}

	public void processAction(ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			log.debug("==== processAction called ====");

			String action = request.getParameter("sakai.action");
			log.debug("sakai.action = {}", action);

			PortletSession pSession = request.getPortletSession(true);

			// Clear before Action
			clearErrorMessage(request);

			String view = (String) pSession.getAttribute("sakai.view");
			log.debug("sakai.view={}", view);

			if ( action == null ) {
				// Do nothing
			} else if ( action.equals("main") ) {
				response.setPortletMode(PortletMode.VIEW);
			} else if ( action.equals("edit") ) {
				pSession.setAttribute("sakai.view", "edit");
			} else if ( action.equals("edit.reset") ) {
				pSession.setAttribute("sakai.view","edit.reset");
			} else if (action.equals("edit.setup")){
				pSession.setAttribute("sakai.view","edit.setup");
			} else if ( action.equals("edit.clear") ) {
				clearSession(request);
				response.setPortletMode(PortletMode.VIEW);
				pSession.setAttribute("sakai.view", "main");
			} else if ( action.equals("edit.do.reset") ) {
				processActionReset(action,request, response);
			} else if ( action.equals("edit.save") ) {
				processActionSave(action,request, response);
			}
			log.debug("==== End of ProcessAction ====");
		}

	private void clearSession(PortletRequest request)
	{
		PortletSession pSession = request.getPortletSession(true);

		pSession.removeAttribute("sakai.url");
		pSession.removeAttribute("sakai.widget");
		pSession.removeAttribute("sakai.descriptor");
		pSession.removeAttribute("sakai.attemptdescriptor");

		for (String element : fieldList) {
			pSession.removeAttribute("sakai."+element);
		}
	}

	public void processActionReset(String action,ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			// TODO: Check Role
			log.debug("Removing preferences....");
			clearSession(request);
			PortletSession pSession = request.getPortletSession(true);
			PortletPreferences prefs = request.getPreferences();
			try {
				prefs.reset("sakai.descriptor");
				for (String element : fieldList) {
					prefs.reset("imsti."+element);
					prefs.reset("sakai:imsti."+element);
				}
				log.debug("Preference removed");
			} catch (ReadOnlyException e) {
				setErrorMessage(request, rb.getString("error.modify.prefs")) ;
				return;
			}
			prefs.store();

			// Go back to the main edit page
			pSession.setAttribute("sakai.view", "edit");
		}

	public void processActionEdit(String action,ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

		}

	public Properties getSakaiProperties()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getConfig();
	}

	// Empty or all whitespace properties are null
	public String getSakaiProperty(Properties config, String key)
	{
		String propValue = config.getProperty(key);
		if ( propValue != null && propValue.trim().length() < 1 ) propValue = null;
		return propValue;
	}

	// Insure that if we have frozen properties - we never accept form data
	public String getFormParameter(ActionRequest request, Properties sakaiProperties, String propName)
	{
		String propValue = getCorrectProperty(request, propName, null);
		if ( propValue == null || ! isPropertyFinal(propName) )
		{
			propValue = request.getParameter("imsti."+propName);
		}
		log.debug("Form/Final imsti.{}={}", propName, propValue);
		if (propValue != null ) propValue = propValue.trim();
		return propValue;
	}

	public void processActionSave(String action,ActionRequest request, ActionResponse response)
		throws PortletException, IOException {

			PortletSession pSession = request.getPortletSession(true);
			Properties sakaiProperties = getSakaiProperties();

			String launch_url  = getFormParameter(request,sakaiProperties,"launch");
			if ( launch_url != null && launch_url.trim().length() < 1 ) launch_url = null;

			if ( launch_url == null ) {
				setErrorMessage(request, rb.getString("error.no.input") );
				return;
			} else if ( launch_url.startsWith("http://") || launch_url.startsWith("https://") ) {
				try {
					URL testUrl = new URL(launch_url);
					URI testUri = new URI(launch_url);
				}
				catch(Exception e) {
					setErrorMessage(request, rb.getString("error.bad.url") );
					return;
				}
			} else {
				setErrorMessage(request, rb.getString("error.bad.url") );
				return;
			}

			// Prepare to store preferences
			PortletPreferences prefs = request.getPreferences();
			boolean changed = false;

			// Make Sure the Assignment is a legal one
			String assignment = getFormParameter(request,sakaiProperties,"assignment");
			String newAssignment = getFormParameter(request,sakaiProperties,"newassignment");
			String oldPlacementSecret = getSakaiProperty(sakaiProperties,"imsti."+LTI_PORTLET_PLACEMENTSECRET);
			String allowOutcomes = ServerConfigurationService.getString(SakaiLTIUtil.LTI_OUTCOMES_ENABLED, SakaiLTIUtil.LTI_OUTCOMES_ENABLED_DEFAULT);
			String allowRoster = ServerConfigurationService.getString(SakaiLTIUtil.LTI_ROSTER_ENABLED, SakaiLTIUtil.LTI_ROSTER_ENABLED_DEFAULT);
			if ( "true".equals(allowOutcomes) && newAssignment != null && newAssignment.trim().length() > 1 ) {
				if ( addGradeBookItem(request, newAssignment) ) {
					log.debug("Success!");
					assignment = newAssignment;
				}
			}

			log.debug("old placementsecret={}", oldPlacementSecret);
			if ( oldPlacementSecret == null &&
					("true".equals(allowOutcomes) ||
                     "true".equals(allowRoster) ) ) {
				try {
					String uuid = UUID.randomUUID().toString();
					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_FORMAT);
					String date_secret = sdf.format(date);
					prefs.setValue("sakai:imsti."+LTI_PORTLET_PLACEMENTSECRET, uuid);
					prefs.setValue("sakai:imsti.placementsecretdate", date_secret);
					log.debug("placementsecret set to={} data={}", uuid, date_secret);
					changed = true;
				} catch (ReadOnlyException e) {
					setErrorMessage(request, rb.getString("error.modify.prefs") );
					return;
				}
			}

			if ( "true".equals(allowOutcomes) && assignment != null && assignment.trim().length() > 1 ) {
				List<String> assignments = getGradeBookAssignments();
				boolean found = false;
				if ( assignments != null ) for ( String assn : assignments ) {
					if ( assn.equals(assignment) ) {
						found = true;
						break;
					}
				}
				if ( ! found ) {
					setErrorMessage(request, rb.getString("error.gradable.badassign") +
							" " + ComponentManager.get(FormattedText.class).escapeHtml(assignment,false));
					return;
				}
			}

			String imsTIHeight  = getFormParameter(request,sakaiProperties,"frameheight");
			if ( imsTIHeight != null && imsTIHeight.trim().length() < 1 ) imsTIHeight = null;
			if ( imsTIHeight != null ) {
				try {
					int x = Integer.parseInt(imsTIHeight);
					if ( x < 0 ) {
						setErrorMessage(request, rb.getString("error.bad.height") );
						return;
					}
				}
				catch(Exception e) {
					setErrorMessage(request, rb.getString("error.bad.height") );
					return;
				}
			}

			// Passed the sanity checks - time to save it all!

			String context = getContext();
			Placement placement = ToolManager.getCurrentPlacement();

			// Update the Page Title (button text)
			String imsTIPageTitle  = getFormParameter(request,sakaiProperties,"tooltitle");
			String prefsPageTitle = prefs.getValue("sakai:imsti.tooltitle", null);
			imsTIPageTitle = imsTIPageTitle == null ? "" : imsTIPageTitle.trim();
			prefsPageTitle = prefsPageTitle == null ? "" : prefsPageTitle.trim();

			if ( ! imsTIPageTitle.equals(prefsPageTitle) ) {
				try {
					if ( imsTIPageTitle.length() > 99 ) imsTIPageTitle = imsTIPageTitle.substring(0,99);
					ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
					Site site = SiteService.getSite(toolConfig.getSiteId());
					SitePage page = site.getPage(toolConfig.getPageId());
					if ( imsTIPageTitle.length() > 1 ) {
						page.setTitle(imsTIPageTitle.trim());
						page.setTitleCustom(true);
					} else {
						page.setTitle("");
						page.setTitleCustom(false);
					}
					SiteService.save(site);
				} catch (Exception e) {
					setErrorMessage(request, rb.getString("error.page.title"));
					return;
				}
			}

			// Store preferences
			for (String element : fieldList) {
				String formParm  = getFormParameter(request,sakaiProperties,element);
				if ( "assignment".equals(element) ) formParm = assignment;

				if ( "secret".equals(element) ) {
					if ( LEAVE_SECRET_ALONE.equals(formParm) ) continue;
					String key = ServerConfigurationService.getString(SakaiLTIUtil.LTI_ENCRYPTION_KEY, null);
					if (key != null) {
						try {
							if ( formParm != null && formParm.trim().length() > 0 ) {
									formParm = SimpleEncryption.encrypt(key, formParm);
									// LTI-195 convert old-style encrypted secrets
									prefs.reset("sakai:imsti.encryptedsecret");
							}
						} catch (RuntimeException re) {
							log.warn("Failed to encrypt secret, falling back to plaintext: {}", re.getMessage());
						}
					}
				}

				try {
					prefs.setValue("sakai:imsti."+element, formParm);
					changed = true;
				} catch (ReadOnlyException e) {
					setErrorMessage(request, rb.getString("error.modify.prefs") );
					return;
				}
			}

			// track event and store
			if ( changed ) {
				// 2.6 Event Tracking
				Event event = LocalEventTrackingService.newEvent(EVENT_LTI_CONFIG, launch_url, context, true, NotificationService.NOTI_OPTIONAL);
				// 2.5 Event Tracking
				// Event event = EventTrackingService.newEvent(EVENT_LTI_CONFIG, launch_url, true);
				LocalEventTrackingService.post(event);
				prefs.store();
			}

			pSession.setAttribute("sakai.view", "main");
			response.setPortletMode(PortletMode.VIEW);
		}

	/**
	 * Get the current site page our current tool is placed on.
	 *
	 * @return The site page id on which our tool is placed.
	 */
	protected String getCurrentSitePageId()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		ToolConfiguration tool = SiteService.findTool(placement.getId());
		if (tool != null)
		{
			return tool.getPageId();
		}
		return null;
	}

	// TODO: Local cleverness ???
	private void sendToJSP(RenderRequest request, RenderResponse response,
			String jspPage) throws PortletException {
		response.setContentType(request.getResponseContentType());
		if (jspPage != null && jspPage.length() != 0) {
			try {
				PortletRequestDispatcher dispatcher = pContext
					.getRequestDispatcher(jspPage);
				dispatcher.include(request, response);
			} catch (IOException e) {
				throw new PortletException("Sakai Dispatch unabble to use "
						+ jspPage, e);
			}
		}
	}

	// Error Message
	public void clearErrorMessage(PortletRequest request)
	{
		PortletHelper.clearErrorMessage(request);
	}

	public Map getErrorMap(PortletRequest request)
	{
		return PortletHelper.getErrorMap(request);
	}

	public String getErrorOutput(PortletRequest request)
	{
		return PortletHelper.getErrorOutput(request);
	}

	public void setErrorMessage(PortletRequest request, String errorMsg)
	{
		PortletHelper.setErrorMessage(request,errorMsg);
	}

	public String getErrorMessage(PortletRequest request)
	{
		return PortletHelper.getErrorMessage(request);
	}


	public void setErrorMessage(PortletRequest request, String errorMsg, Throwable t)
	{
		PortletHelper.setErrorMessage(request,errorMsg,t);
	}

	private String getContext()
	{
		String retval = ToolManager.getCurrentPlacement().getContext();
		return retval;
	}

	// Create an item in the Gradebook
	protected boolean addGradeBookItem(ActionRequest request, String assignmentName)
	{
		try
		{
			GradingService gradingService = (GradingService)  ComponentManager.get("org.sakaiproject.grading.api.GradingService");

			if ( ! ((gradingService.currentUserHasEditPerm(getContext()) || gradingService.currentUserHasGradingPerm(getContext())) && gradingService.currentUserHasGradeAllPerm(getContext()) ) ) return false;

			// add assignment to gradebook
			Assignment asn = new Assignment();
			asn.setPoints(Double.valueOf(100));
			asn.setExternallyMaintained(false);
			asn.setName(assignmentName);
			asn.setReleased(true);
			asn.setUngraded(false);
			String gradebookUid = getContext();
			gradingService.addAssignment(gradebookUid, getContext(), asn);
			return true;
		}
		catch (ConflictingAssignmentNameException e)
		{
			return true;
		}
		catch (Exception e)
		{
			log.warn("Exception (may be because GradeBook has not yet been added to the Site) {}", e.getMessage());
			setErrorMessage(request, rb.getString("error.gradable.badcreate") + ":" + e.getMessage() );
			log.warn("{}:addGradeItem {}", this, e.getMessage());
		}
		return false;
	}

	// get all assignments from the Gradebook
	protected List<String> getGradeBookAssignments()
	{
		List<String> retval = new ArrayList<String>();
        GradingService gradingService = (GradingService)  ComponentManager
            .get("org.sakaiproject.grading.api.GradingService");

        if ( ! ((gradingService.currentUserHasEditPerm(getContext()) || gradingService.currentUserHasGradingPerm(getContext())) && gradingService.currentUserHasGradeAllPerm(getContext()) ) ) return null;
        String gradebookUid = getContext();
        List gradebookAssignments = gradingService.getAssignments(gradebookUid, getContext(), SortType.SORT_BY_NONE);

        // filtering out anything externally provided
        for (Iterator i=gradebookAssignments.iterator(); i.hasNext();)
        {
            org.sakaiproject.grading.api.Assignment gAssignment = (org.sakaiproject.grading.api.Assignment) i.next();
            if ( gAssignment.getExternallyMaintained() ) continue;
            retval.add(gAssignment.getName());
        }
        return retval;
	}

}
