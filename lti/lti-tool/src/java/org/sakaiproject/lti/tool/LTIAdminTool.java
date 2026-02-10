/** ********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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
 ********************************************************************************* */
package org.sakaiproject.lti.tool;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.FormatStyle;

import java.net.URLEncoder;

import java.text.MessageFormat;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.tsugi.lti.ContentItem;
import org.tsugi.lti.LTIConstants;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.lti13.DeepLinkResponse;
import org.sakaiproject.lti13.LineItemUtil;
import org.sakaiproject.lti13.util.SakaiLineItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import org.tsugi.util.Base64DoubleUrlEncodeSafe;
import static org.tsugi.lti.LTIUtil.getObject;
import static org.tsugi.lti.LTIUtil.getString;

import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.lti.api.LTIExportService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.beans.LtiContentBean;
import org.sakaiproject.lti.beans.LtiToolSiteBean;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.foorm.SakaiFoorm;
import org.sakaiproject.time.api.UserTimeService;

// We need to interact with the RequestFilter
import org.sakaiproject.util.RequestFilter;
import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti.LTIUtil;

/**
 * <p>
 * LTIAdminTool is a Simple Velocity-based Tool
 * </p>
 */
@Slf4j
public class LTIAdminTool extends VelocityPortletPaneledAction {

	/**
	 * Resource bundle using current language locale
	 */
	protected static ResourceLoader rb = new ResourceLoader("ltitool");

	private static String STATE_POST = "lti:state_post";
	private static String STATE_SUCCESS = "lti:state_success";
	private static String STATE_ID = "lti:state_id";
	private static String STATE_TOOL_ID = "lti:state_tool_id";
	private static String STATE_CONTENT_ID = "lti:state_content_id";
	private static String STATE_REDIRECT_URL = "lti:state_redirect_url";
	private static String STATE_CONTENT_ITEM = "lti:state_content_item";
	private static String STATE_CONTENT_ITEM_FAILURES = "lti:state_content_item_failures";
	private static String STATE_CONTENT_ITEM_SUCCESSES = "lti:state_content_item_successes";
	private static String STATE_LINE_ITEM = "lti:state_line_item";
	private static String STATE_CONTENT_ITEM_CARTRIDGE_URL = "lti:state_content_item_cartridge_url";
	private static String STATE_CONTENT_ITEM_IMPORT_RETURN_URL = "lti:state_content_item_import_return_url";

	private static String ALLOW_MAINTAINER_ADD_SYSTEM_TOOL = "lti:allow_maintainer_add_system_tool";
	private static String ALLOW_MAINTAINER_ADD_TOOL_SITE = "lti:allow_maintainer_add_tool_site";

	//accepted parameters for page, sort and search actions
	private static String PARAM_ID = "id";
	private static String PARAM_CRITERIA = "criteria";
	private static String PARAM_PAGE_EVENT = "page_event";
	private static String PARAM_PAGE = "pagesize";
	private static String PARAM_SEARCH_FIELD = "field";
	private static String PARAM_SEARCH_VALUE = "search";

	//default elements per page
	private static int ELEMENTS_PER_PAGE = 50;

	//available paging events
	private static String PAGE_EVENT_FIRST = "first";
	private static String PAGE_EVENT_PREV = "prev";
	private static String PAGE_EVENT_NEXT = "next";
	private static String PAGE_EVENT_LAST = "last";

	//attributes stored in the state/context
	private static String ATTR_FILTER_ID = "FILTER_ID";
	private static String ATTR_SORT_CRITERIA = "SORT_CRITERIA";
	private static String ATTR_LAST_SORTED_FIELD = "LAST_SORTED_FIELD";
	private static String ATTR_ASCENDING_ORDER = "ASCENDING_ORDER";
	private static String ATTR_SORT_INDEX = "SORT_INDEX";
	private static String ATTR_SORT_PAGESIZE = "SORT_PAGESIZE";
	private static String ATTR_SEARCH_LAST_FIELD = "SEARCH_LAST_FIELD";
	private static String ATTR_SEARCH_MAP = "search_map";

	// Parameters for Content Item Flows
	private static String FLOW_PARAMETER = "flow";
	private static String FLOW_PARAMETER_LESSONS = "lessons";
	private static String FLOW_PARAMETER_EDITOR = "editor";
	private static String FLOW_PARAMETER_ASSIGNMENT = "assignment";
	private static String FLOW_PARAMETER_IMPORT = "import";
	private static String SECRETONLY_PARAMETER = "secretonly";

	/**
	 * Service Implementations
	 */
	protected static ToolManager toolManager = null;
	protected static LTIService ltiService = null;
	protected static ServerConfigurationService serverConfigurationService = null;
	protected static UserTimeService userTimeService = null;

	protected static SakaiFoorm foorm = new SakaiFoorm();

	// Should be RequestFilter.SAKAI_SERVERID
	public final static String SAKAI_SERVERID = "sakai.serverId";

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices() {
		if (toolManager == null) {
			toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");
		}

		if (ltiService == null) {
			ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		}
		if (serverConfigurationService == null) {
			serverConfigurationService = (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
		}
		if (userTimeService == null) {
			userTimeService = (UserTimeService) ComponentManager.get("org.sakaiproject.time.api.UserTimeService");
		}
	}

	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata) {
		super.initState(state, portlet, rundata);
		getServices();

		String siteId = toolManager.getCurrentPlacement().getContext();
		// get site tool mode from tool registry
		if (portlet.getPortletConfig().getInitParameter("helper") != null) {
			// If we are coming from elsewhere in SiteAction under the SiteAction url space, it communicates the
			// site we are to use in this value

			// If we are coming from a directly constructed URL (i.e. like from Lessons in an iframe),  the URL includes a tool placement
			// and the request filter sets up site in state from the URL.

			String helperSiteId = (String) state.getAttribute(HELPER_ID + ".siteId");
			if (helperSiteId != null) {
				siteId = helperSiteId;
			}
		}
		state.setAttribute("SITE_ID", siteId);

	}

	private String getSiteId(SessionState state) {
		return (String) state.getAttribute("SITE_ID");

	}

	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildErrorPanelContext(VelocityPortlet portlet, Context context,
			RunData rundata, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));

		String alertMessage = (String) state.getAttribute(STATE_MESSAGE);
		state.removeAttribute(STATE_MESSAGE);
		log.warn("alertMessage = {}", alertMessage);
		context.put("alertMessage", alertMessage);

		state.removeAttribute(STATE_ID);
		state.removeAttribute(STATE_TOOL_ID);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_REDIRECT_URL);
		return "lti_error";
	}

	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildFinishedPanelContext(VelocityPortlet portlet, Context context,
			RunData rundata, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_ID);
		state.removeAttribute(STATE_TOOL_ID);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_REDIRECT_URL);
		return "lti_finished";
	}

	public String buildMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		// default to site view
		return buildToolSystemPanelContext(portlet, context, data, state);
	}

	/**
	 * Filter action : allows to filter tool site list with the given tool_id
	 *
	 * Accepted parameters : id
	 *
	 * @param data
	 */
	public void doFilter(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString(PARAM_ID);
		if (StringUtils.isNotEmpty(id)) {
			state.setAttribute(ATTR_FILTER_ID, id);
		}
	}

	/**
	 * Sort action : allows to order the tool site list by the given field
	 * (column name). Ascending/Descending order will be detected automatically.
	 *
	 * Accepted parameters : criteria
	 *
	 * @param data
	 */
	public void doSort(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String lastSortedField = (String) state.getAttribute(ATTR_LAST_SORTED_FIELD);
		Boolean ascendingOrder = (Boolean) state.getAttribute(ATTR_ASCENDING_ORDER);

		String criteria = data.getParameters().getString(PARAM_CRITERIA);

		String ret = null;
		boolean changeSortingOrder = StringUtils.isNotEmpty(criteria);
		if (StringUtils.isNotEmpty(criteria) && !criteria.equals(lastSortedField)) {
			changeSortingOrder = false;
			ascendingOrder = true;
		}
		if (changeSortingOrder) {
			ascendingOrder = !ascendingOrder;
		}
		if (StringUtils.isNotEmpty(criteria)) {
			ret = criteria;
			ret += (ascendingOrder ? " ASC" : " DESC");
			state.setAttribute(ATTR_LAST_SORTED_FIELD, criteria);
		}
		state.setAttribute(ATTR_ASCENDING_ORDER, ascendingOrder);
		state.setAttribute(ATTR_SORT_CRITERIA, ret);
	}

	/**
	 * Change page action : allow to move through pages
	 *
	 * Accepted parameters : page_event, (optional)pagesize Allowed events :
	 * first, prev, next, last
	 *
	 * @param data
	 */
	public void doChangePage(RunData data) {
		//also check if page size has changed
		doChangePageSize(data);

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Integer index = (Integer) state.getAttribute(ATTR_SORT_INDEX);
		try {
			if (index == null) {
				index = 0;
			}
			String event = data.getParameters().getString(PARAM_PAGE_EVENT);
			if (StringUtils.isNotEmpty(event)) {
				Integer pageSize = (Integer) state.getAttribute(ATTR_SORT_PAGESIZE);
				if (PAGE_EVENT_FIRST.equals(event)) {
					index = 0;
				}
				if (PAGE_EVENT_PREV.equals(event)) {
					index = Math.max(0, index - pageSize);
				}
				if (PAGE_EVENT_NEXT.equals(event)) {
					index += pageSize;
				}
				if (PAGE_EVENT_LAST.equals(event)) {
					index = -1;
				}
			}
		} catch (Exception ex) {
		}
		state.setAttribute(ATTR_SORT_INDEX, index);
	}

	/**
	 * Change page size action : allows to change the page size
	 *
	 * Accepted parameters : pagesize
	 *
	 * @param data
	 */
	public void doChangePageSize(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Integer pageSize = (Integer) state.getAttribute(ATTR_SORT_PAGESIZE);
		try {
			String param = data.getParameters().getString(PARAM_PAGE);
			if (StringUtils.isNotEmpty(param)) {
				pageSize = Integer.parseInt(param);
			}
		} catch (Exception ex) {
		}
		if (pageSize == null || pageSize < 0) {
			pageSize = ELEMENTS_PER_PAGE;
		}
		state.setAttribute(ATTR_SORT_PAGESIZE, pageSize);
	}

	/**
	 * Search action : allows to search by a field (column) and value. One
	 * action by one search, but multiple search (in different columns) will be
	 * accumulative
	 *
	 * Accepted parameters : field, search
	 *
	 * @param data
	 */
	public void doSearch(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String searchField = data.getParameters().getString(PARAM_SEARCH_FIELD);
		String searchValue = data.getParameters().getString(PARAM_SEARCH_VALUE);
		Map<String, String> searchMap = (Map<String, String>) state.getAttribute(ATTR_SEARCH_MAP);
		if (searchMap == null) {
			searchMap = new HashMap<String, String>();
		}
		if (StringUtils.isNotEmpty(searchField)) {
			if (StringUtils.isNotEmpty(searchValue)) {
				searchValue = searchValue.replace(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND, LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_AND);
				searchValue = searchValue.replace(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR, LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_OR);
				searchMap.put(searchField, searchValue);
			} else {
				searchMap.remove(searchField);
			}
		}
		state.setAttribute(ATTR_SEARCH_MAP, searchMap);
		state.setAttribute(ATTR_SEARCH_LAST_FIELD, searchField);
		state.setAttribute(ATTR_SORT_INDEX, 0);
	}

	/**
	 * Reset all paging/sorting/searching fields in the state
	 *
	 * @param data
	 */
	public void doReset(RunData data) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(ATTR_FILTER_ID, null);
		state.setAttribute(ATTR_SEARCH_MAP, null);
		state.setAttribute(ATTR_SEARCH_LAST_FIELD, null);
		state.setAttribute(ATTR_SORT_INDEX, 0);
		state.setAttribute(ATTR_LAST_SORTED_FIELD, null);
		state.setAttribute(ATTR_ASCENDING_ORDER, true);
	}

	public String buildToolSitePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("allowMaintainerAddToolSite", serverConfigurationService.getBoolean(ALLOW_MAINTAINER_ADD_TOOL_SITE, true));
		context.put("getContext", toolManager.getCurrentPlacement().getContext());
		context.put("doEndHelper", BUTTON + "doEndHelper");
		state.removeAttribute(STATE_POST);

		String order = (String) state.getAttribute(ATTR_SORT_CRITERIA);

		Integer pageSize = (Integer) state.getAttribute(ATTR_SORT_PAGESIZE);
		if (pageSize == null) {
			pageSize = ELEMENTS_PER_PAGE;
		}

		//build search clause based on parameters and put some of them in the context
		String search = buildSearch(data, context);

		//check for tool filter
		String filterId = (String) state.getAttribute(ATTR_FILTER_ID);
		if (StringUtils.isNotEmpty(filterId)) {
			search = "tool_id:" + filterId + ((search != null) ? (LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND + search) : "");
		}

		//count all contents
		int count_contents = ltiService.countContents(search, getSiteId(state));
		context.put("count_contents", count_contents);

		//if no contents detected
		Integer totalCount = count_contents;
		if (count_contents == 0) {
			//count all contents without search
			totalCount = ltiService.countContents(null, getSiteId(state));
		}
		context.put("hasContents", (totalCount > 0));

		//get paging index
		Integer index = (Integer) state.getAttribute(ATTR_SORT_INDEX);
		if (index == null) {
			index = 0;
		}
		if (index == -1) {
			index = (count_contents - 1) / pageSize * pageSize;
		} else if (index >= count_contents) {
			index = Math.max(0, count_contents - 1);
		}
		int lastIndex = index + pageSize - 1;

		//put all in the context
		context.put("sortIndex", (index + 1));
		context.put("sortLastIndex", Math.min(lastIndex + 1, count_contents));
		context.put("sortPageSize", pageSize);
		context.put(ATTR_LAST_SORTED_FIELD, state.getAttribute(ATTR_LAST_SORTED_FIELD));
		context.put(ATTR_ASCENDING_ORDER, state.getAttribute(ATTR_ASCENDING_ORDER));

		// this is for the "site tools" panel - using Beans directly for better performance and type safety
		List<LtiContentBean> contentBeans = new ArrayList<>();
		if (count_contents > 0) {
			Map<String, String> siteURLMap = new HashMap<String, String>(); //cache for site URL
			contentBeans = ltiService.getContentsAsBeans(search, order, index, lastIndex, getSiteId(state));
			
			// Create maps for additional properties indexed by content ID
			Map<Long, String> toolUrlMap = new HashMap<>();
			Map<Long, String> siteUrlMap = new HashMap<>();
			Map<Long, String> formattedDateMap = new HashMap<>();
			
			for (LtiContentBean contentBean : contentBeans) {
				Long contentId = contentBean.getId();
				
				// Validate placement
				String plstr = contentBean.getPlacement();
				ToolConfiguration tool = SiteService.findTool(plstr);
				if (tool == null) {
					// TODO: Review this closely
					// Note: Invalid placement detected - tool configuration not found
					// The setPlacement() method already exists in LtiContentBean (via Lombok)
					// but we're just validating here, not fixing invalid placements
				}

				// Get site URL based on site ID
				String siteId = contentBean.getSiteId();
				try {
					// Look for it in the cache
					String url = siteURLMap.get(siteId);
					if (url == null) {
						url = SiteService.getSite(siteId).getUrl();
						siteURLMap.put(siteId, url);
					}
					siteUrlMap.put(contentId, url);
				} catch (Exception e) {
					log.error("Error getting URL for site: {}", siteId);
				}

				// Format the date for display
				Object created_at = contentBean.getCreatedAt();
				String formattedDate = null;
				if (created_at instanceof Date) {
					formattedDate = userTimeService.dateTimeFormat(((Date) created_at), rb.getLocale(), java.text.DateFormat.MEDIUM);
				} else if (created_at instanceof LocalDateTime) {
					LocalDateTime ldt = (LocalDateTime) created_at;
					// Form stores these as UTC
					Instant ldtInstant = ldt.toInstant(ZoneOffset.UTC);
					formattedDate = userTimeService.dateTimeFormat(ldtInstant, FormatStyle.MEDIUM, FormatStyle.SHORT);
				} else {
					formattedDate = created_at.toString();
				}
				formattedDateMap.put(contentId, formattedDate);

				// Get LTI URL based on site ID and tool ID
				String toolUrl = LTIService.LAUNCH_PREFIX + siteId + "/content:" + contentBean.getId();
				toolUrlMap.put(contentId, toolUrl);
			}
			
			// Add maps to context for template access
			context.put("toolUrlMap", toolUrlMap);
			context.put("siteUrlMap", siteUrlMap);
			context.put("formattedDateMap", formattedDateMap);
		}
		
		// Pass Beans directly to template - much more efficient!
		context.put("contents", contentBeans);
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);

		//export csv/excel links
		context.put("export_url_csv", ltiService.getExportUrl(toolManager.getCurrentPlacement().getContext(), filterId, LTIExportService.ExportType.CSV));
		context.put("export_url_excel", ltiService.getExportUrl(toolManager.getCurrentPlacement().getContext(), filterId, LTIExportService.ExportType.EXCEL));

		//attribution column (just header name)
		String attribution_name = serverConfigurationService.getString(LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME, LTIService.LTI_SITE_ATTRIBUTION_PROPERTY_NAME_DEFAULT);
		if (StringUtils.isNotEmpty(attribution_name)) {
			//check if property is a translation key
			String aux = rb.getString(attribution_name);
			if (StringUtils.isNotEmpty(aux)) {
				attribution_name = aux;
			}
			context.put("attribution_name", attribution_name);
		}

		// top navigation menu
		Menu menu = new MenuImpl(portlet, data, "LTIAdminTool");
		menu.add(new MenuEntry(rb.getString("tool.in.system"), true, "doNav_tool_system"));
		menu.add(new MenuEntry(rb.getString("tool.in.site"), false, "doNav_tool_site"));
		context.put("menu", menu);

		return "lti_tool_site";
	}

	public String buildToolSystemPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String contextString = toolManager.getCurrentPlacement().getContext();
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("doEndHelper", BUTTON + "doEndHelper");
		if (ltiService.isAdmin(getSiteId(state))
				&& serverConfigurationService.getString(SakaiLTIUtil.LTI_ENCRYPTION_KEY, null) == null) {
			context.put("configMessage", rb.getString("error.tool.no.encryption.key"));
		}

		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("allowMaintainerAddSystemTool", Boolean.valueOf(serverConfigurationService.getBoolean(ALLOW_MAINTAINER_ADD_SYSTEM_TOOL, true)));
		context.put("getContext", contextString);

		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_POST);

		// this is for the system tool panel - using Beans directly for better performance and type safety
		List<LtiToolBean> toolBeans = ltiService.getToolsAsBeans(null, null, 0, 0, getSiteId(state));
		
		// Create map for additional tool properties indexed by tool ID
		Map<Long, Map<String, Object>> toolPropsMap = new HashMap<>();
		
		for (LtiToolBean toolBean : toolBeans) {
			Long toolId = toolBean.getId();
			String privacyUrl = LTIService.LAUNCH_PREFIX + getSiteId(state) + "/tool:" + toolId + "?" +
					SakaiLTIUtil.MESSAGE_TYPE_PARAMETER + "=" + SakaiLTIUtil.MESSAGE_TYPE_PARAMETER_PRIVACY;
			
			// Create properties map for this tool
			Map<String, Object> additionalProps = new HashMap<>();
			additionalProps.put("privacy_url", privacyUrl);
			
			// Add deployment count for system tools
			if (StringUtils.isBlank(toolBean.getSiteId())) {
				List<LtiToolSiteBean> toolSiteBeans = ltiService.getToolSitesByToolIdAsBeans(String.valueOf(toolId), getSiteId(state));
				additionalProps.put("lti_count_deployment", toolSiteBeans.size());
			}
			
			toolPropsMap.put(toolId, additionalProps);
		}
		
		// Add tool properties map to context
		context.put("toolPropsMap", toolPropsMap);

		// Pass Beans directly to template - much more efficient!
		context.put("ltiTools", toolBeans);

		// top navigation menu
		Menu menu = new MenuImpl(portlet, data, "LTIAdminTool");
		menu.add(new MenuEntry(rb.getString("tool.in.system"), false, "doNav_tool_system"));
		menu.add(new MenuEntry(rb.getString("tool.in.site"), true, "doNav_tool_site"));
		context.put("menu", menu);

		//reset paging/sorting/searching state
		doReset(data);

		return "lti_tool_system";
	}

	public void doEndHelper(RunData data, Context context) {
		// Request a shortcut transfer back to the tool we are helping
		// This working depends on SAK-20898
		SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);

		// In case the above fails...
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		switchPanel(state, "Main");
	}

	public String buildToolPostInsertPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.view"));
			return "lti_error";
		}
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		String[] mappingForm = ltiService.getToolModel(getSiteId(state));
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = Long.valueOf(id);
		LtiToolBean toolBean = ltiService.getToolAsBean(key, getSiteId(state));
		if (toolBean == null) {
			return "lti_error";
		}
		context.put("clientId", toolBean.getLti13ClientId());

		String keySetUrl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/keyset";
		context.put("keySetUrl", keySetUrl);
		String tokenUrl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/token/" + toolBean.getId();
		context.put("tokenUrl", tokenUrl);
		String authOIDC = SakaiLTIUtil.getOurServerUrl() + "/imsoidc/lti13/oidc_auth";
		context.put("authOIDC", authOIDC);

		String site_id = toolBean.getSiteId();
		String issuerURL = SakaiLTIUtil.getIssuer(site_id);
		context.put("issuerURL", issuerURL);

		String deploymentId = SakaiLTIUtil.getDeploymentId(site_id);
		context.put("deploymentId", deploymentId);

		String configUrl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/sakai_config";
		configUrl += "?key=" + URLEncoder.encode(toolBean.getId().toString());
		configUrl += "&clientId=" + URLEncoder.encode(toolBean.getLti13ClientId());
		configUrl += "&issuerURL=" + URLEncoder.encode(issuerURL);
		configUrl += "&deploymentId=" + URLEncoder.encode(deploymentId);
		context.put("configUrl", configUrl);
		if ( configUrl.length() > 30 ) {
			String configUrlShort = configUrl.substring(0,30) + " ...";
			context.put("configUrlShort", configUrlShort);
		}

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_post_insert";
	}

	public String buildToolViewPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.view"));
			return "lti_error";
		}
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		String[] mappingForm = ltiService.getToolModel(getSiteId(state));
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = Long.valueOf(id);
		LtiToolBean toolBean = ltiService.getToolAsBean(key, getSiteId(state));
		if (toolBean == null) {
			return "lti_error";
		}

		Long launchType = LTIUtil.toLongNull(toolBean.getLti13());
		context.put("launchType", launchType);
		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)) ? Boolean.TRUE : Boolean.FALSE);

		// Hide sensitive fields in the Bean before form generation
		toolBean.setSecret(LTIService.SECRET_HIDDEN);
		toolBean.setConsumerkey(LTIService.SECRET_HIDDEN);

		// Make sure this is ready to handle LTI 1.3 launches
		minimalLTI13(toolBean);

		String formOutput = ltiService.formOutput(toolBean, mappingForm);

		context.put("formOutput", formOutput);

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_view";
	}

	// Is this obsolete and can be removed?
	public String buildAutoInsertPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.view"));
			return "lti_error";
		}
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		context.put("doToolAction", BUTTON + "doAutoInsert");

		return "lti_tool_auto_insert";
	}

	// Make sure this is ready to handle LTI 1.3 launches
	private boolean minimalLTI13(Map<String, Object> tool) {
		boolean retval = false;

		Long tool_id = LTIUtil.toLongNull(tool.get(LTIService.LTI_ID));
		String site_id = (String) tool.get(LTIService.LTI_SITE_ID);

		String clientId = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_CLIENT_ID));
		if (clientId == null ) {
			clientId = UUID.randomUUID().toString();
			tool.put(LTIService.LTI13_CLIENT_ID, clientId);
			retval = true;
		}

		String keyset = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_LMS_KEYSET));
		if (keyset == null ) {
			keyset = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/keyset";
			tool.put(LTIService.LTI13_LMS_KEYSET, keyset);
			retval = true;
		}

		String endpoint = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_LMS_ENDPOINT));
		if (endpoint == null ) {
			endpoint = SakaiLTIUtil.getOurServerUrl() + "/imsoidc/lti13/oidc_auth";
			tool.put(LTIService.LTI13_LMS_ENDPOINT, endpoint);
			retval = true;
		}

		String tokenurl = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_LMS_TOKEN));
		if (tokenurl == null && tool_id != null ) {
			tokenurl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/token/" + tool_id;
			tool.put(LTIService.LTI13_LMS_TOKEN, tokenurl);
		}

		String deployment_id = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_LMS_DEPLOYMENT_ID));
		if ( deployment_id == null ) {
			deployment_id = SakaiLTIUtil.getDeploymentId(site_id);
			tool.put(LTIService.LTI13_LMS_DEPLOYMENT_ID, deployment_id);
			retval = true;
		}

		String issuer = StringUtils.trimToNull((String) tool.get(LTIService.LTI13_LMS_ISSUER));
		if ( issuer == null ) {
			issuer = SakaiLTIUtil.getIssuer(site_id);
			tool.put(LTIService.LTI13_LMS_ISSUER, issuer);
			retval = true;
		}

		return retval;
	}

	// Bean version of minimalLTI13 - Make sure this is ready to handle LTI 1.3 launches
	private boolean minimalLTI13(org.sakaiproject.lti.beans.LtiToolBean toolBean) {
		boolean retval = false;

		Long tool_id = toolBean.id;
		String site_id = toolBean.siteId;

		String clientId = StringUtils.trimToNull(toolBean.lti13ClientId);
		if (clientId == null ) {
			clientId = UUID.randomUUID().toString();
			toolBean.lti13ClientId = clientId;
			retval = true;
		}

		String keyset = StringUtils.trimToNull(toolBean.lti13LmsKeyset);
		if (keyset == null ) {
			keyset = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/keyset";
			toolBean.lti13LmsKeyset = keyset;
			retval = true;
		}

		String endpoint = StringUtils.trimToNull(toolBean.lti13LmsEndpoint);
		if (endpoint == null ) {
			endpoint = SakaiLTIUtil.getOurServerUrl() + "/imsoidc/lti13/oidc_auth";
			toolBean.lti13LmsEndpoint = endpoint;
			retval = true;
		}

		String tokenurl = StringUtils.trimToNull(toolBean.lti13LmsToken);
		if (tokenurl == null && tool_id != null ) {
			tokenurl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/token/" + tool_id;
			toolBean.lti13LmsToken = tokenurl;
		}

		String deployment_id = StringUtils.trimToNull(toolBean.lti13LmsDeploymentId);
		if ( deployment_id == null ) {
			deployment_id = SakaiLTIUtil.getDeploymentId(site_id);
			toolBean.lti13LmsDeploymentId = deployment_id;
			retval = true;
		}

		String issuer = StringUtils.trimToNull(toolBean.lti13LmsIssuer);
		if ( issuer == null ) {
			issuer = SakaiLTIUtil.getIssuer(site_id);
			toolBean.lti13LmsIssuer = issuer;
			retval = true;
		}

		return retval;
	}

	// Make a tool with a title and sent to tool_insert (update) to dynamic register
	public void doAutoInsert(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.view"));
			switchPanel(state, "Error");
			return;
		}

		String title = data.getParameters().getString("title");
		if (title == null || title.length() < 1) {
			addAlert(state, rb.getString("tool.lti13.auto.start.title.required"));
			switchPanel(state, "AutoInsert");
			return;
		}

		// Build a minimal tool
		String clientId = UUID.randomUUID().toString();
		Map<String, Object> tool = new HashMap<String, Object>();
		tool.put(LTIService.LTI_TITLE, title);
		tool.put(LTIService.LTI_LAUNCH, "https://example.com/draft-tool-in-progress");
		tool.put(LTIService.LTI13_CLIENT_ID, clientId);

		minimalLTI13(tool);

		Object retval = ltiService.insertTool(tool, getSiteId(state));
		if (retval instanceof String) {
			addAlert(state, rb.getString("tool.lti13.auto.start.insert.fail")+" "+retval);
			switchPanel(state, "Error");
			return;
		}

		Long key = Long.valueOf(retval.toString());

		String newPanel = "ToolEdit&autoStart=true&id=" + key;
		switchPanel(state, newPanel);
	}

	public String buildAutoStartPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.view"));
			return "lti_error";
		}
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		String[] mappingForm = ltiService.getToolModel(getSiteId(state));
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = Long.valueOf(id);
		LtiToolBean toolBean = ltiService.getToolAsBean(key, getSiteId(state));
		if (toolBean == null) {
			return "lti_error";
		}

		context.put("tool_id", key);
		context.put("doAction", BUTTON + "doLaunchAdvantageAuto");

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_auto_launch";
	}

	public void doLaunchAdvantageAuto(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String toolConfigUrl = data.getParameters().getString("toolConfigUrl");
		if ( StringUtils.isNotBlank(toolConfigUrl) ) toolConfigUrl = toolConfigUrl.trim();

		// Retrieve the tool
		Long toolKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if (toolKey == 0 || toolKey < 0) {
			addAlert(state, rb.getString("error.tool.not.found"));
			switchPanel(state, "Error");
			return;
		}

		LtiToolBean toolBean = ltiService.getToolAsBean(toolKey, getSiteId(state));
		if (toolBean == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			switchPanel(state, "Error");
			return;
		}

		// Make sure this is ready to handle LTI 1.3 launches
		boolean changed = minimalLTI13(toolBean);

		// Construct and time-stamp the one-time registration token and store it
		String registration_token = LTI13Util.timeStamp(UUID.randomUUID().toString());
		toolBean.lti13AutoToken = registration_token;

		// Update the tool
		Object retval = ltiService.updateTool(toolKey, toolBean, getSiteId(state));
		if (retval instanceof String) {
			addAlert(state, (String) retval);
			switchPanel(state, "Error");
			return;
		}

		String site_id = toolBean.siteId;
		String clientId = toolBean.lti13ClientId;
		String issuerURL = SakaiLTIUtil.getIssuer(site_id);
		String deploymentId = SakaiLTIUtil.getDeploymentId(site_id);

		String sakaiConfigUrl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/well_known";
		sakaiConfigUrl += "?key=" + URLEncoder.encode(toolKey.toString());
		sakaiConfigUrl += "&clientId=" + URLEncoder.encode(clientId);
		sakaiConfigUrl += "&issuerURL=" + URLEncoder.encode(issuerURL);
		sakaiConfigUrl += "&deploymentId=" + URLEncoder.encode(deploymentId);

		String forwardUrl = toolConfigUrl;
		if (forwardUrl.indexOf("?") > 0) {
			forwardUrl += "&";
		} else {
			forwardUrl += "?";
		}
		forwardUrl += "openid_configuration=" + URLEncoder.encode(sakaiConfigUrl);
		forwardUrl += "&registration_token=" + URLEncoder.encode(registration_token);

		state.setAttribute(STATE_REDIRECT_URL, forwardUrl);
		switchPanel(state, "Forward");
	}

	public String buildToolDeletePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolDelete");
		String[] mappingForm = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*|^id:.*", null);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = Long.valueOf(id);

		// Retrieve the tool using a WHERE clause so the counts get computed
		List<LtiToolBean> toolBeans = ltiService.getToolsAsBeans("lti_tools.id = " + key, null, 0, 0, getSiteId(state));
		if (toolBeans == null || toolBeans.size() < 1) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		LtiToolBean toolBean = toolBeans.get(0);
		String formOutput = ltiService.formOutput(toolBean, mappingForm);
		context.put("formOutput", formOutput);
		context.put("tool", toolBean);
		context.put("tool_count", toolBean.getLtiContentCount());
		context.put("tool_unique_site_count", toolBean.getLtiSiteCount());

		List<LtiToolSiteBean> toolSiteBeans = ltiService.getToolSitesByToolIdAsBeans(id, getSiteId(state));
		context.put("tool_site_count", toolSiteBeans.size());

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_delete";
	}

	public void doToolDelete(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			switchPanel(state, "ToolSystem");
			return;
		}
		Long key = Long.valueOf(id);

		// Delete the tool and all dependencies
		List<String> errors = ltiService.deleteToolAndDependencies(key, getSiteId(state));

		String errorNote = "";
		for (String errstr : errors) {
			log.error(errstr);
			errorNote += "<br/>" + errstr;
		}

		if (errors.size() < 1) {
			switchPanel(state, "ToolSystem");
		} else {
			addAlert(state, rb.getString("error.delete.fail") + errorNote);
			switchPanel(state, "ToolSystem");
		}
	}

	public String buildToolSiteDeployPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return "lti_error";
		}

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));

		String tool_id = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		if (tool_id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_main";
		}
		context.put("toolId", tool_id);

		LtiToolBean toolBean = ltiService.getToolAsBean(Long.valueOf(tool_id), getSiteId(state));
		if (toolBean == null) {
			return "lti_error";
		}
		context.put("tool", toolBean);

		String[] mappingForm = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*", null);
		String formOutput = ltiService.formOutput(toolBean, mappingForm);
		context.put("formOutput", formOutput);

		List<LtiToolSiteBean> ltiToolSiteBeans = ltiService.getToolSitesByToolIdAsBeans(tool_id, getSiteId(state));
		context.put("ltiToolSites", ltiToolSiteBeans);

		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_site_deploy";
	}

	public String buildToolSiteInsertPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return "lti_error";
		}

		context.put("doToolAction", BUTTON + "doToolSiteInsert");

		String toolId = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		if (toolId == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_main";
		}
		context.put("toolId", toolId);

		// Get previous form inputs
		Properties previousPost = null;
		if (state.getAttributeNames().contains(STATE_POST)) {
			previousPost = (Properties) state.getAttribute(STATE_POST);
		} else {
			previousPost = data.getParameters().getProperties();
		}
		context.put("previousPost", previousPost);

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));

		// Remove previous form inputs
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_site_insert";
	}

	public void doToolSiteInsert(RunData data, Context context) {

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return;
		}

		// Retrieve input data
		Properties reqProps = data.getParameters().getProperties();

		// Retrieve the tool id
		String toolId = reqProps.getProperty("tool_id");

		// Get the site IDs from the input form
		// Remove inputs that are empty and duplicates
		String textareaValue = reqProps.getProperty("tool_site_ids");
		// Split the string at every line break, whether it's a Unix (\n) or Windows (\r\n) line ending.
		String[] linesArray = textareaValue.split("\\r?\\n");
		Set<String> uniqueInputSiteIds = Arrays.stream(linesArray)
				.map(String::trim)
				.filter(siteId -> !siteId.isEmpty())
				.collect(Collectors.toSet());

		// Check whether the input siteIds are valid
		List<String> invalidSiteIds = uniqueInputSiteIds.stream()
				.filter(inputSiteId -> !SiteService.siteExists(inputSiteId))
				.collect(Collectors.toList());

		if (!invalidSiteIds.isEmpty()) {
			state.setAttribute(STATE_POST, reqProps);
			addAlert(state, rb.getString("error.siteId.not.found") + " Invalid Site Ids=" + invalidSiteIds);
			switchPanel(state, "ToolSiteInsert&tool_id=" + toolId);
			return;
		}

		// Get toolSites from the database
		List<LtiToolSiteBean> toolSiteBeans = ltiService.getToolSitesByToolIdAsBeans(toolId, getSiteId(state));
		if (toolSiteBeans == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			switchPanel(state, "Error");
			return;
		}

		// Check if the tool has already been deployed to one of the input sites.
		Set<String> associatedSiteIds = toolSiteBeans.stream()
				.map(LtiToolSiteBean::getSiteId)
				.collect(Collectors.toSet());

		List<String> deployedSiteIds = uniqueInputSiteIds.stream()
				.filter(associatedSiteIds::contains)
				.collect(Collectors.toList());

		if (!deployedSiteIds.isEmpty()) {
			state.setAttribute(STATE_POST, reqProps);
			addAlert(state, rb.getString("error.tool.site.exist") + " SiteIds=" + deployedSiteIds);
			switchPanel(state, "ToolSiteInsert&tool_id=" + toolId);
			return;
		}

		// Save to DB
		List<String> insertSuccessSiteIds = new ArrayList<>();
		List<String> insertErrorMessages = new ArrayList<>();

		for (String inputSiteId : uniqueInputSiteIds) {
			Properties props = new Properties();
			props.setProperty("tool_id", toolId);
			props.setProperty("SITE_ID", inputSiteId);
			props.setProperty("notes", reqProps.getProperty("notes"));

			Object retval = ltiService.insertToolSite(props, getSiteId(state));

			if (retval instanceof String) {	// Error
				insertErrorMessages.add("SiteId=" + inputSiteId + ", retval=" + retval + ".");

			} else if ( retval instanceof Long ) { // Success
				insertSuccessSiteIds.add(inputSiteId);

			} else { // Unexpected Error
				log.error("Unexpected return type from insertToolSite={}, toolId={}, inputSiteId={}", retval, toolId, inputSiteId);
				insertErrorMessages.add("SiteId=" + inputSiteId + ", retval=" + retval + ".");
			}
		}

		if (!insertErrorMessages.isEmpty()) {
			addAlert(state, rb.getString("error.tool.site.insert") + " Error Info=" + insertErrorMessages);
		}

		state.setAttribute(STATE_SUCCESS, rb.getFormattedMessage("tool.site.deploy.success", insertSuccessSiteIds.size()));
		switchPanel(state, "ToolSiteDeploy&tool_id=" + toolId);
	}

	public String buildToolSiteEditPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return "lti_error";
		}

		context.put("doToolAction", BUTTON + "doToolSiteEdit");

		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		context.put("id", id);

		LtiToolSiteBean toolSiteBean = ltiService.getToolSiteAsBean(Long.valueOf(id), getSiteId(state));
		if (toolSiteBean == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}

		String toolId = String.valueOf(toolSiteBean.getToolId());
		context.put("toolId", toolId);

		// Display tool attributes (Read-only)
		LtiToolBean toolBean = ltiService.getToolAsBean(Long.valueOf(toolId), getSiteId(state));
		String[] mappingFormOutput = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*", null);
		String formOutput = ltiService.formOutput(toolBean, mappingFormOutput);

		// Display siteId attribute (Read-only)
		String[] mappingFormOutput2 = foorm.filterForm(ltiService.getToolSiteModel(getSiteId(state)), "^SITE_ID:.*", null);
		String formOutput2 = ltiService.formOutput(toolSiteBean, mappingFormOutput2);
		formOutput += formOutput2;
		context.put("formOutput", formOutput);

		String[] mappingFormInput = foorm.filterForm(ltiService.getToolSiteModel(getSiteId(state)), null, "^SITE_ID:.*");
		String formInput = ltiService.formInput(toolSiteBean, mappingFormInput);
		context.put("formInput", formInput);

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_site_edit";
	}

	public void doToolSiteEdit(RunData data, Context context) {

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return;
		}

		// Retrieve input data
		Properties reqProps = data.getParameters().getProperties();

		// Retrieve primary key
		String id = reqProps.getProperty(LTIService.LTI_ID);

		// find the tool id
		Map<String, Object> toolSite = ltiService.getToolSiteById(Long.valueOf(id), getSiteId(state));
		String toolId = String.valueOf(toolSite.get(LTIService.LTI_TOOL_ID));

		// Save to DB
		Object retval = ltiService.updateToolSite(Long.valueOf(id), reqProps, getSiteId(state));

		if (retval instanceof String) {	// Error
			addAlert(state, rb.getString("error.tool.site.edit") + " retval=" + retval);

		} else if ( retval instanceof Boolean ) { // Success
			state.setAttribute(STATE_SUCCESS, rb.getString("tool.site.edit.success") + " SiteId=" + toolSite.get(LTIService.LTI_SITE_ID));

		} else { // Unexpected Error
			log.error("Unexpected return type from updateToolSite={}, id={}, current siteId={}", retval, id, getSiteId(state));
			addAlert(state, rb.getString("error.tool.site.edit") + " retval=" + retval);
		}

		switchPanel(state, "ToolSiteDeploy&tool_id=" + toolId);
	}

	public String buildToolSiteDeletePanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return "lti_error";
		}

		context.put("doToolAction", BUTTON + "doToolSiteDelete");

		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}

		LtiToolSiteBean toolSiteBean = ltiService.getToolSiteAsBean(Long.valueOf(id), getSiteId(state));
		if (toolSiteBean == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}

		context.put("toolSite", toolSiteBean);

		String toolId = String.valueOf(toolSiteBean.getToolId());

		// Display tool attributes (Read-only)
		LtiToolBean toolBean = ltiService.getToolAsBean(Long.valueOf(toolId), getSiteId(state));
		String[] mappingFormOutput = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*", null);
		String formOutput = ltiService.formOutput(toolBean, mappingFormOutput);

		// Display toolSite attributes (Read-only)
		String[] mappingFormOutput2 = foorm.filterForm(ltiService.getToolSiteModel(getSiteId(state)), "^SITE_ID:.*|^notes:.*", null);
		String formOutput2 = ltiService.formOutput(toolSiteBean, mappingFormOutput2);
		formOutput += formOutput2;
		context.put("formOutput", formOutput);

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_site_delete";
	}

	public void doToolSiteDelete(RunData data, Context context) {

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isAdmin(getSiteId(state))) {
			addAlert(state, rb.getString("error.admin.deploy"));
			switchPanel(state, "Error");
			return;
		}

		// Retrieve input data
		Properties reqProps = data.getParameters().getProperties();

		// Retrieve primary key
		String id = reqProps.getProperty(LTIService.LTI_ID);

		// find the tool id
		Map<String, Object> toolSite = ltiService.getToolSiteById(Long.valueOf(id), getSiteId(state));
		String toolId = String.valueOf(toolSite.get(LTIService.LTI_TOOL_ID));

		// Save to DB
		boolean retval = ltiService.deleteToolSite(Long.valueOf(id), getSiteId(state));

		if (retval) {	// Success
			state.setAttribute(STATE_SUCCESS, rb.getString("tool.site.delete.success") + " SiteId=" + toolSite.get(LTIService.LTI_SITE_ID));
		} else { // Fail
			addAlert(state, rb.getString("error.tool.site.delete") + " retval=" + retval);
		}

		switchPanel(state, "ToolSiteDeploy&tool_id=" + toolId);
	}

	public String buildToolTransferPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolTransfer");
		String[] mappingForm = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*|^id:.*", null);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = Long.valueOf(id);

		// Retrieve the tool using a WHERE clause so the counts get computed
		List<LtiToolBean> toolBeans = ltiService.getToolsAsBeans("lti_tools.id = " + key, null, 0, 0, getSiteId(state));
		if (toolBeans == null || toolBeans.size() < 1) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_main";
		}

		LtiToolBean toolBean = toolBeans.get(0);
		String formOutput = ltiService.formOutput(toolBean, mappingForm);
		context.put("formOutput", formOutput);
		context.put("toolBean", toolBean);  // Pass Bean directly for template access
		context.put("tool", toolBean.asMap());  // Keep Map for template compatibility
		context.put("tool_id", key);
		context.put("tool_count", toolBean.getLtiContentCount());
		context.put("tool_unique_site_count", toolBean.getLtiSiteCount());

		String contextString = toolManager.getCurrentPlacement().getContext();
		List<LtiToolBean> systemToolBeans = getAvailableToolsAsBeans(getSiteId(state), contextString);
		context.put("tools", systemToolBeans);

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_transfer";
	}

	public void doToolTransfer(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.transfer"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String new_tool_id = StringUtils.trimToNull(data.getParameters().getString("new_tool_id"));
		if (new_tool_id == null) {
			addAlert(state, rb.getString("error.transfer.missing"));
			switchPanel(state, "ToolSystem");
			return;
		}

		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			switchPanel(state, "ToolSystem");
			return;
		}
		Long key = Long.valueOf(id);
		Long new_key = Long.valueOf(new_tool_id);

		Object retval = ltiService.transferToolContentLinks(key, new_key, getSiteId(state));
		if ( retval instanceof String ) {
			addAlert(state, (String) retval);
			switchPanel(state, "ToolSystem");
			return;
		}
		String success = MessageFormat.format(rb.getString("tool.transfer.success"), retval);
        state.setAttribute(STATE_SUCCESS, success);
		switchPanel(state, "ToolSystem");
	}

	/**
	 * All required fields for inserting a tool should be listed in the start page
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	public String buildToolInsertPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		context.put("doToolAction", BUTTON + "doToolInsert");

		// Get previous form inputs
		Properties previousPost = null;
		if (state.getAttributeNames().contains(STATE_POST)) {
			previousPost = (Properties) state.getAttribute(STATE_POST);
		}
		context.put("previousPost", previousPost);

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));
		if (!ltiService.isAdmin(getSiteId(state))) {
			context.put("siteId", getSiteId(state));
		}

		// Remove previous form inputs
		state.removeAttribute(STATE_POST);
		return "lti_tool_insert";
	}

	public void doToolInsert(RunData data, Context context) {

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// Retrieve input data
		Properties reqProps = data.getParameters().getProperties();
		String toolTitle = reqProps.getProperty("title");
		String inputSiteId = reqProps.getProperty("siteId").trim();

		// Check input data
		List<String> inputErrors = new ArrayList<>();
		if (toolTitle.isEmpty()) {
			inputErrors.add(rb.getString("tool.new.insert.start.title.required"));
		}

		if (!inputSiteId.isEmpty() && !SiteService.siteExists(inputSiteId)) {
			inputErrors.add("Invalid Site Id=" + inputSiteId);
		}

		if (!ltiService.isAdmin(getSiteId(state)) && !getSiteId(state).equals(inputSiteId)) {
			inputErrors.add(rb.getString("tool.new.insert.start.siteId.cannot.edit"));
			reqProps.setProperty("siteId", getSiteId(state));
		}

		if (!inputErrors.isEmpty()) {
			state.setAttribute(STATE_POST, reqProps);
			addAlert(state, inputErrors.toString());
			switchPanel(state, "ToolInsert");
			return;
		}

		// Build a minimal tool
		Map<String, Object> tool = new HashMap<>();
		tool.put(LTIService.LTI_TITLE, toolTitle);
		if (!inputSiteId.isEmpty()) {
			tool.put(LTIService.LTI_SITE_ID, inputSiteId);
			// If siteId is valid and not empty, then the tool is always be visible in the site.
			tool.put(LTIService.LTI_VISIBLE, 0);
		}
		tool.put(LTIService.LTI_LAUNCH, "https://example.com/draft-tool-in-progress");

		Object retval = ltiService.insertTool(tool, getSiteId(state));
		if (retval instanceof String) {
			addAlert(state, rb.getString("tool.new.insert.start.fail") + " retval=" + retval);
			switchPanel(state, "Error");
			return;
		}

		long key = Long.parseLong(retval.toString());
		switchPanel(state, "ToolEdit&id=" + key);
	}

	public String buildToolEditPanelContext(VelocityPortlet portlet, Context context, RunData data, SessionState state) {

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (StringUtils.isEmpty(id)) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		context.put("toolId", id);

		LtiToolBean toolBean = ltiService.getToolAsBean(Long.valueOf(id), getSiteId(state));
		if (toolBean == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		// Hide the old tool secret unless it is incomplete
		if (!LTIService.LTI_SECRET_INCOMPLETE.equals(toolBean.secret)) {
			toolBean.secret = LTIService.SECRET_HIDDEN;
		}

		// Set this up in case it becomes LTI 1.3
		minimalLTI13(toolBean);

		String siteId = toolBean.siteId;
		context.put("issuerURL", SakaiLTIUtil.getIssuer(siteId));

		// If siteId is not blank, there is no option for visibility since the tool is always be visible in the site; otherwise, the tool can be visible or stealth
		String excludePattern = StringUtils.isNotEmpty(siteId) ? "^visible:.*" : "^SITE_ID:.*";
		String[] mappingForm = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), null, excludePattern);
		String formInput = ltiService.formInput(toolBean, mappingForm);
		context.put("formInput", formInput);

		context.put("isAdmin", ltiService.isAdmin(getSiteId(state)));
		context.put("isEdit", Boolean.TRUE);

		Placement placement = toolManager.getCurrentPlacement();
		String autoStartUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId()
				+ "?panel=AutoStart"
				+ "&id=" + toolBean.id;
		context.put("autoStartUrl", autoStartUrl);

		String autoRegistrationUrl = SakaiLTIUtil.getOurServerUrl() + "/imsblis/lti13/get_registration?key="+toolBean.id;
		context.put("autoRegistrationUrl", autoRegistrationUrl);

		// Tool Deployment
		context.put("canDeploy", ltiService.isAdmin(getSiteId(state)) && StringUtils.isEmpty(siteId));
		String deployUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId()
				+ "?panel=ToolSiteDeploy"
				+ "&tool_id=" + toolBean.id;
		context.put("deployUrl", deployUrl);

		context.put("doToolAction", BUTTON + "doToolEdit");

		return "lti_tool_edit";
	}

	public void doToolEdit(RunData data, Context context) {

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			switchPanel(state, "Error");
			return;
		}

		Properties reqProps = data.getParameters().getProperties();

		String newSecret = reqProps.getProperty(LTIService.LTI_SECRET);
		if (LTIService.SECRET_HIDDEN.equals(newSecret)) {
			reqProps.remove(LTIService.LTI_SECRET);
		}

		// Retrieve the tool
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id.isEmpty()) {
			addAlert(state, rb.getString("error.id.not.found"));
			switchPanel(state, "Error");
			return;
		}

		Map<String, Object> tool = ltiService.getTool(Long.valueOf(id), getSiteId(state));
		if (tool == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			switchPanel(state, "Error");
			return;
		}

		// Handle the incoming LTI 1.3 data
		boolean isLti13 = "1".equals(reqProps.getProperty("lti13"));
		String form_lti13_client_id = StringUtils.trimToNull(reqProps.getProperty(LTIService.LTI13_CLIENT_ID));

		boolean displayPostInsert = false;
		if (isLti13 && StringUtils.isEmpty(form_lti13_client_id)) {
			reqProps.setProperty(LTIService.LTI13_CLIENT_ID, UUID.randomUUID().toString());
			displayPostInsert = true;
		}

		// Update the tool
		Object retval = ltiService.updateTool(Long.valueOf(id), reqProps, getSiteId(state));

		if (retval instanceof String) {
			addAlert(state, (String) retval);
			switchPanel(state, "ToolEdit&id=" + id);
			return;
		}

		state.setAttribute(STATE_SUCCESS, rb.getString("success.updated"));
		if ( displayPostInsert ) {
			switchPanel(state, "ToolPostInsert&id=" + id);
		} else {
			switchPanel(state, "ToolSystem");
		}
	}

	/**
	 * Content related methods ------------------------------
	 */
	public String buildContentPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.view"));
			return "lti_error";
		}
		List<LtiContentBean> contentBeans = ltiService.getContentsAsBeans(null, null, 0, 5000, getSiteId(state));
		
		// Validate placement for each content bean
		for (LtiContentBean contentBean : contentBeans) {
			String plstr = contentBean.getPlacement();
			ToolConfiguration tool = SiteService.findTool(plstr);
			if (tool == null) {
				// TODO: Review this closely
				// Note: Invalid placement detected - tool configuration not found
				// The setPlacement() method already exists in LtiContentBean (via Lombok)
				// but we're just validating here, not fixing invalid placements
			}
		}
		
		// Pass Beans directly to template - much more efficient and type-safe!
		context.put("contents", contentBeans);
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("getContext", toolManager.getCurrentPlacement().getContext());
		state.removeAttribute(STATE_SUCCESS);
		return "lti_content";
	}

	public List<Map<String, Object>> getAvailableTools (String ourSite, String contextString) {

		Boolean isAdmin = ltiService.isAdmin(ourSite);
		List<LtiToolBean> toolBeans = null;
		if ( isAdmin ) {
			toolBeans = ltiService.getToolsAsBeans(null, null, 0, 0, ourSite);
		} else {
			toolBeans = ltiService.getToolsLaunchAsBeans(ourSite);
		}

		// only list the tools available in the system
		List<Map<String, Object>> systemTools = new ArrayList<Map<String, Object>>();
		for (LtiToolBean toolBean : toolBeans) {
			Map<String, Object> tool = toolBean.asMap();
			String siteId = StringUtils.trimToNull(toolBean.getSiteId());
			if (siteId == null) {
				// add tool for whole system
				systemTools.add(tool);
			} else if (siteId.equals(contextString)) {
				// add the tool for current site only
				systemTools.add(tool);
			} else if (ltiService.isAdmin(ourSite)) {
				// if in Admin's my workspace, show all tools
				systemTools.add(tool);
			}
	}
	systemTools = systemTools.stream().sorted((m1, m2) -> String.valueOf(m1.get("title")).compareTo(String.valueOf(m2.get("title")))).collect(Collectors.toList());
	return systemTools;

}

public List<LtiToolBean> getAvailableToolsAsBeans(String ourSite, String contextString) {

	Boolean isAdmin = ltiService.isAdmin(ourSite);
	List<LtiToolBean> toolBeans = null;
	if ( isAdmin ) {
		toolBeans = ltiService.getToolsAsBeans(null, null, 0, 0, ourSite);
	} else {
		toolBeans = ltiService.getToolsLaunchAsBeans(ourSite);
	}

	// only list the tools available in the system
	List<LtiToolBean> systemToolBeans = new ArrayList<>();
	for (LtiToolBean toolBean : toolBeans) {
		String siteId = StringUtils.trimToNull(toolBean.getSiteId());
		if (siteId == null) {
			// add tool for whole system
			systemToolBeans.add(toolBean);
		} else if (siteId.equals(contextString)) {
			// add the tool for current site only
			systemToolBeans.add(toolBean);
		} else if (ltiService.isAdmin(ourSite)) {
			// if in Admin's my workspace, show all tools
			systemToolBeans.add(toolBean);
		}
	}
	systemToolBeans = systemToolBeans.stream().sorted(Comparator.comparing(t -> String.valueOf(t.getTitle()))).collect(Collectors.toList());
	return systemToolBeans;

}

	public String buildContentPutPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		String contextString = toolManager.getCurrentPlacement().getContext();
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String stateToolId = (String) state.getAttribute(STATE_TOOL_ID);
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("doAction", BUTTON + "doContentPut");
		state.removeAttribute(STATE_SUCCESS);

		List<Map<String, Object>> systemTools = getAvailableTools (getSiteId(state), contextString);
		context.put("tools", systemTools);

		Object previousData = null;

		String toolId = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		if (toolId == null) {
			toolId = stateToolId;
		}
		// output the tool id value to context
		context.put("tool_id", toolId);

		Long key = null;
		if (toolId != null) {
			try {
				key = Long.valueOf(toolId);
			} catch (NumberFormatException e) {
				//Reset toolId and key
				key = null;
				toolId = null;
			}
		}
		Map<String, Object> tool = null;
		if (key != null) {
			tool = ltiService.getTool(key, getSiteId(state));
			if (tool == null) {
				addAlert(state, rb.getString("error.tool.not.found"));
				return "lti_content_insert";
			}
		}

		String contentId = data.getParameters().getString(LTIService.LTI_ID);
		if (contentId == null) {
			contentId = (String) state.getAttribute(STATE_CONTENT_ID);
		}

		if (contentId == null) {  // Insert
			if (toolId == null) {
				return "lti_content_insert";
			}
			previousData = (Properties) state.getAttribute(STATE_POST);

			// Edit
		} else {
			context.put("oldContentId", contentId);
			Long contentKey = Long.valueOf(contentId);
			Map<String, Object> content = ltiService.getContent(contentKey, getSiteId(state));
			if (content == null) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_content";
			}

			if (key == null) {
				key = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
				if (key != null) {
					tool = ltiService.getTool(key, getSiteId(state));
				}
			}
			previousData = content;

			// whether the content has a site link created already?
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration siteLinkTool = SiteService.findTool(plstr);
			if (siteLinkTool != null) {
				context.put(LTIService.LTI_PLACEMENT, plstr);
			}
			// Hide content secret unless it is incomplete
			if (content.get(LTIService.LTI_SECRET) != null) {
				content.put(LTIService.LTI_SECRET, LTIService.SECRET_HIDDEN);
			}	
		}

		// We will handle the tool_id field ourselves in the Velocity code
		String[] contentForm = foorm.filterForm(null, ltiService.getContentModel(key, getSiteId(state)), null, "^tool_id:.*");
		if (contentForm == null || key == null) {
			if ( contentId != null) {
				return "lti_content_insert";
			}
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		if (previousData == null) {
			Properties defaultData = new Properties();
			String tool_fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
			if (StringUtils.isEmpty(defaultData.getProperty(LTIService.LTI_FA_ICON)) && StringUtils.isNotEmpty(tool_fa_icon) ) {
				defaultData.setProperty(LTIService.LTI_FA_ICON, tool_fa_icon);
			}

			String toolTitle = (String) tool.get(LTIService.LTI_TITLE);
			if (StringUtils.isEmpty(defaultData.getProperty(LTIService.LTI_TITLE)) && StringUtils.isNotEmpty(toolTitle) ) {
				defaultData.setProperty(LTIService.LTI_TITLE, toolTitle);
			}

			String toolDescription = (String) tool.get(LTIService.LTI_DESCRIPTION);
			if (StringUtils.isEmpty(defaultData.getProperty(LTIService.LTI_DESCRIPTION)) && StringUtils.isNotEmpty(toolDescription) ) {
				defaultData.setProperty(LTIService.LTI_DESCRIPTION, toolDescription);
			}
			previousData = defaultData;

		}

		String formInput = ltiService.formInput(previousData, contentForm);

		context.put("formInput", formInput);
		context.put(LTIService.LTI_TOOL_ID, key);
		if (tool != null) {
			context.put("tool_description", tool.get(LTIService.LTI_DESCRIPTION));
			Long visible = LTIUtil.toLong(tool.get(LTIService.LTI_VISIBLE));
			context.put("tool_visible", visible);
		}

		String flow = data.getParameters().getString(FLOW_PARAMETER);
		context.put("flow", flow);
		return "lti_content_insert";
	}

	// Insert or edit depending on whether an id is present or not
	public void doContentPut(RunData data, Context context) {
		Properties reqProps = data.getParameters().getProperties();
		doContentPutInternal(data, context, reqProps);
	}

	private void doContentPutInternal(RunData data, Context context, Properties reqProps) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		state.removeAttribute(STATE_POST);

		String id = reqProps.getProperty(LTIService.LTI_ID);
		String toolId = reqProps.getProperty(LTIService.LTI_TOOL_ID);

		// Encrypt the lti_content secret(SAK-46566)
		String newSecret = reqProps.getProperty(LTIService.LTI_SECRET);
		if (LTIService.SECRET_HIDDEN.equals(newSecret)) {
			reqProps.remove(LTIService.LTI_SECRET);
		} else if (StringUtils.isNotBlank(newSecret)) {
			newSecret = SakaiLTIUtil.encryptSecret(newSecret);
			reqProps.put(LTIService.LTI_SECRET, newSecret);
		}		
		// Does an insert when id is null and update when is is not null
		Object retval = ltiService.insertToolContent(id, toolId, reqProps, getSiteId(state));

		Long contentKey = null;
		Map<String, Object> content = null;
		if (retval instanceof String) {
			addAlert(state, (String) retval);
			switchPanel(state, "Error");
			state.setAttribute(STATE_POST, reqProps);
			state.setAttribute(STATE_CONTENT_ID, id);
			return;
		} else if (retval instanceof Boolean) {
			//If it's true retrieve the previous content?
			if ((Boolean) retval == true) {
				content = ltiService.getContent(Long.parseLong(id), getSiteId(state));
				if (content == null) {
					addAlert(state, rb.getString("error.content.not.found"));
					switchPanel(state, "Error");
					state.setAttribute(STATE_POST, reqProps);
					state.setAttribute(STATE_CONTENT_ID, id);
					return;
				}
			} else {
				// TODO: returns false, should it do anyhing else?
				log.error("insertToolContent returned false for {}", id);
			}
		} else {
			// the return value is the content key Long value
			id = ((Long) retval).toString();
			contentKey = Long.valueOf(id);
			content = ltiService.getContent(contentKey, getSiteId(state));
			if (content == null) {
				addAlert(state, rb.getString("error.content.not.found"));
				switchPanel(state, "Error");
				state.setAttribute(STATE_POST, reqProps);
				state.setAttribute(STATE_CONTENT_ID, id);
				return;
			}
		}

		// Decode the returnUrl - this handles encoded, plaintext, or already encoded
		String returnUrl = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(reqProps.getProperty("returnUrl"));
		String flow = data.getParameters().getString(FLOW_PARAMETER);
		if (returnUrl != null) {
			String newPanelState = "Error-not set";
			if (id != null) {
				if (returnUrl.startsWith("about:blank")) { // Redirect to the item
					if (content != null) {
						String launch = (String) ltiService.getContentLaunch(content);
						if (launch != null) {
							returnUrl = launch;
						}
					}
					newPanelState = "Forward";
				} else {
					if (returnUrl.indexOf("?") > 0) {
						returnUrl += "&ltiItemId=/blti/" + retval;
					} else {
						returnUrl += "?ltiItemId=/blti/" + retval;
					}
					if (returnUrl.indexOf("panel=PostContentConfig") > 0) {
						newPanelState = "Forward";
					} else {
						newPanelState = "Redirect";
					}
				}
			}

			SakaiLineItem sakaiLineItem = (SakaiLineItem) state.getAttribute(STATE_LINE_ITEM);
			state.removeAttribute(STATE_LINE_ITEM);
			Long toolKey = Long.valueOf(toolId);
			handleLineItem(state, sakaiLineItem, toolKey, content);

			//Append the LTI item description to the URL so Lessons can use it.
			String ltiToolDescription = reqProps.getProperty(LTIService.LTI_DESCRIPTION);
			if(StringUtils.isNotEmpty(ltiToolDescription)){
				returnUrl += "&ltiItemDescription=" + URLEncoder.encode(ltiToolDescription);
			}

			log.debug("flow={} newPanelState={} to returnUrl={}", flow, newPanelState, returnUrl);
			switchPanel(state, newPanelState);
			state.setAttribute(STATE_REDIRECT_URL, returnUrl);
			return;
		}

		String success = null;
		if (id == null) {
			success = rb.getString("success.created");
		} else {
			success = rb.getString("success.updated");
		}
		state.setAttribute(STATE_SUCCESS, success);

		String title = reqProps.getProperty(LTIService.LTI_TITLE);

		// Take the title from the content (or tool) definition
		if (title == null || title.trim().length() < 1) {
			if (content != null) {
				title = (String) content.get(ltiService.LTI_TITLE);
			}
		}

		if (reqProps.getProperty("add_site_link") != null) {
			// this is to add site link:
			retval = ltiService.insertToolSiteLink(id, title, getSiteId(state));
			if (retval instanceof String) {
				String prefix = ((String) retval).substring(0, 1);
				addAlert(state, ((String) retval).substring(1));
				if ("0".equals(prefix)) {
					if (ToolUtils.isInlineRequest(data.getRequest())) {
						switchPanel(state, "ToolSite");
					} else {
						switchPanel(state, "Refresh");
					}
				} else if ("1".equals(prefix)) {
					switchPanel(state, "Error");
				}
				return;
			} else if (retval instanceof Boolean) {
				if (((Boolean) retval).booleanValue()) {
					if (ToolUtils.isInlineRequest(data.getRequest())) {
						switchPanel(state, "ToolSite");
					} else {
						switchPanel(state, "Refresh");
					}
				} else {
					switchPanel(state, "Error");
				}
				return;
			}

			state.setAttribute(STATE_SUCCESS, rb.getString("success.link.add"));
		}

		switchPanel(state, "ToolSite");
	}

	// In Sakai-21 when producing a content item for assignments, we will let
	// assignments finish this job.  This logic is for Sakai-20 and Sakai-19 and for
	// non-assignments content items in Sakai-21 and later
	private void handleLineItem(SessionState state, SakaiLineItem sakaiLineItem, Long toolKey, Map<String, Object> content)
	{
		if ( sakaiLineItem == null ) return;

		// When we are doing an implicit line item creation, the title is the key
		// for gradebook column lookup
		String ltiToolTitle = (String) content.get(LTIService.LTI_TITLE);
		if(StringUtils.isNotEmpty(ltiToolTitle)){
			sakaiLineItem.label = ltiToolTitle;
		}

		try {
			Assignment assn = LineItemUtil.createLineItem(getSiteId(state), toolKey, content, sakaiLineItem);
			if ( assn == null ) {
				log.warn("Could not create gradebook column while processing LineItem");
			}
		} catch (Exception e) { // Probably a duplicate title
			log.warn("Could not create gradebook column while processing LineItem");
		}
	}

	// This is where we receive the Response from the external ContentItem / DeepLink
	// producer - the producer will post to this URL so we need to carefully re-establish the
	// session cookie
	public void doSingleContentItemResponse(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// If we find our way to an error panel, we need to pass along the session
		// to make sure it is properly re-established
		String sakaiSession = data.getParameters().getString(RequestFilter.ATTR_SESSION);
		String errorPanel = "Error";
		String finishedPanel = "Finished";
		if ( sakaiSession != null ) {
			errorPanel = errorPanel + "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
			finishedPanel = finishedPanel + "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
		}

		// Check for a returned error message from LTI
		String lti_errormsg = data.getParameters().getString("lti_errormsg");
		if (lti_errormsg != null && lti_errormsg.trim().length() > 0) {
			addAlert(state, lti_errormsg);
			switchPanel(state, errorPanel);
			return;
		}

		String flow = data.getParameters().getString(FLOW_PARAMETER);
		log.debug("doSingleContentItemResponse flow={}", flow);

		// Check for a returned "note" from LTI
		String lti_msg = data.getParameters().getString("lti_msg");
		if (lti_msg != null) {
			state.setAttribute(STATE_SUCCESS, lti_msg);
		}

		// Retrieve the tool associated with the content item
		Long toolKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if (toolKey == 0 || toolKey < 0) {
			addAlert(state, rb.getString("error.contentitem.missing"));
			switchPanel(state, errorPanel);
			return;
		}

		LtiToolBean toolBean = ltiService.getToolAsBean(toolKey, getSiteId(state));
		if (toolBean == null) {
			addAlert(state, rb.getString("error.contentitem.missing"));
			switchPanel(state, errorPanel);
			return;
		}

		// Decode the string if it was encoded and sanity check our (within Sakai) returnUrl
		String returnUrl = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(data.getParameters().getString("returnUrl"));
		if (returnUrl == null) {
			addAlert(state, rb.getString("error.contentitem.missing.returnurl"));
			switchPanel(state, errorPanel);
			return;
		}

		// Come up with the new content item from the DeepLinkResponse or ContentItem
		Properties reqProps;

		String id_token = data.getParameters().getString(LTI13JwtUtil.JWT);
		boolean isDeepLink;
		try {
			isDeepLink = DeepLinkResponse.isRequest(id_token);
		} catch (Exception e) {
			addAlert(state, rb.getString("error.deeplink.bad") + " (" + e.getMessage() + ")");
			switchPanel(state, errorPanel);
			return;
		}

		// doSingleContentItemResponse
		state.removeAttribute(STATE_LINE_ITEM);
		if ( isDeepLink ) {

			// Parse and validate the incoming DeepLink
			String keyset = toolBean.getLti13ToolKeyset();
			if (keyset == null) {
				addAlert(state, rb.getString("error.tool.missing.keyset"));
				switchPanel(state, errorPanel);
				return;
			}

			DeepLinkResponse dlr;
			try {
				dlr = SakaiLTIUtil.getDeepLinkFromToken(toolBean, id_token);  // Also checks security
			} catch (Exception e) {
				addAlert(state, rb.getString("error.deeplink.bad") + " (" + e.getMessage() + ")");
				switchPanel(state, errorPanel);
				return;
			}

			if ( FLOW_PARAMETER_IMPORT.equals(flow) ) {
				JSONObject item = dlr.getItemOfType(DeepLinkResponse.TYPE_FILEITEM);
				if (item == null) {
					addAlert(state, rb.getString("error.deeplink.no.fileitem"));
					switchPanel(state, finishedPanel);
					return;
				}
				handleImportCartridge(data, context, state, errorPanel, returnUrl, item, sakaiSession);
				return;
			} else {

				JSONObject item = dlr.getItemOfType(DeepLinkResponse.TYPE_LTILINKITEM);
				if (item == null) {
					addAlert(state, rb.getString("error.deeplink.no.ltilink"));
					switchPanel(state, finishedPanel);
					return;
				}

				reqProps = extractLTIDeepLink(item, toolBean.asMap(), toolKey);
				reqProps.setProperty("returnUrl", returnUrl);

				// Create the gradebook column if we need to do so
				JSONObject lineItem = getObject(item, DeepLinkResponse.LINEITEM);

				SakaiLineItem sakaiLineItem = null;
				if ( lineItem != null ) {
					String lineItemStr = lineItem.toString();
					try {
						sakaiLineItem = (SakaiLineItem) new ObjectMapper().readValue(lineItemStr, SakaiLineItem.class);
						state.setAttribute(STATE_LINE_ITEM, sakaiLineItem);
					} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
						log.warn("Could not parse input as SakaiLineItem {}",lineItemStr);
					}
				}
			}

		} else {

			// Parse and validate the incoming ContentItem
			ContentItem contentItem;
			try {
				contentItem = SakaiLTIUtil.getContentItemFromRequest(toolBean);
			} catch (Exception e) {
				addAlert(state, rb.getString("error.contentitem.bad") + " (" + e.getMessage() + ")");
				switchPanel(state, errorPanel);
				return;
			}

			// Example of how to pull back the data Properties we passed in above
			// Properties dataProps = contentItem.getDataProperties();
			// log.debug("dataProps={}", dataProps);
			// dataProps={remember=always bring a towel}

			// Extract the content item data
			if ( FLOW_PARAMETER_IMPORT.equals(flow) ) {
				JSONObject item = contentItem.getItemOfType(ContentItem.TYPE_FILEITEM);
				if (item == null) {
					addAlert(state, rb.getString("error.contentitem.no.fileitem"));
					switchPanel(state, errorPanel);
					return;
				}
				handleImportCartridge(data, context, state, errorPanel, returnUrl, item, sakaiSession);
				return;
			} else {

				JSONObject item = contentItem.getItemOfType(ContentItem.TYPE_LTILINKITEM);
				if (item == null) {
					addAlert(state, rb.getString("error.contentitem.no.ltilink"));
					switchPanel(state, errorPanel);
					return;
				}

				// Prepare data for the next phase
				reqProps = extractLTIContentItem(item, toolBean.asMap(), toolKey);
				reqProps.setProperty("returnUrl", returnUrl);

				// Extract the lineItem material
				String label = reqProps.getProperty(LTIService.LTI_TITLE);
				JSONObject lineItem = getObject(item, ContentItem.LINEITEM);
				SakaiLineItem sakaiLineItem = null;
				if ( lineItem != null ) {
					String lineItemStr = lineItem.toString();
					try {
						sakaiLineItem = (SakaiLineItem) new ObjectMapper().readValue(lineItemStr, SakaiLineItem.class);
						state.setAttribute(STATE_LINE_ITEM, sakaiLineItem);
					} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
						log.warn("Could not parse input as SakaiLineItem {}",lineItemStr);
						sakaiLineItem = new SakaiLineItem();
					}
				}

				if ( label != null && lineItem != null ) {
					sakaiLineItem.label = label;
					Double scoreMaximum = ContentItem.getScoreMaximum(lineItem);
					if ( scoreMaximum != null ) sakaiLineItem.scoreMaximum = scoreMaximum;
					state.setAttribute(STATE_LINE_ITEM, sakaiLineItem);
				}
			}
		}

		// Prepare to forward
		state.removeAttribute(STATE_POST);
		String title = reqProps.getProperty(LTIService.LTI_TITLE);
		String url = reqProps.getProperty("launch");


		// If we are not complete, we forward back to the configuration screen
		boolean complete = title != null && url != null;
		if (!complete) {
			log.debug("Forwarding to ContentConfig toolKey={}", toolKey);
			state.setAttribute(STATE_POST, reqProps);
			String contentConfig =  "ContentConfig";
			if (sakaiSession != null) {
				contentConfig +=  '&' + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
			}
			switchPanel(state, contentConfig);
			return;
		}

		// Time to store our content item and redirect back to our helpee
		log.debug("Content Item complete toolKey={}", toolKey);
		doContentPutInternal(data, context, reqProps);

		String redirectPanel = "Redirect";
		if (sakaiSession != null) {
			redirectPanel +=  '&' + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
		}
		switchPanel(state, redirectPanel);
	}

	public void handleImportCartridge(RunData data, Context context, SessionState state,
		String errorPanel, String returnUrl, JSONObject item, String sakaiSession)
	{
		String contentItemUrl = (String) item.get("url");
		if (contentItemUrl == null) {
			addAlert(state, rb.getString("error.deeplink.no.import.url"));
			switchPanel(state, errorPanel);
			return;
		}
		state.setAttribute(STATE_CONTENT_ITEM_CARTRIDGE_URL, contentItemUrl);
		state.setAttribute(STATE_CONTENT_ITEM_IMPORT_RETURN_URL, returnUrl);

		String importPanel = "ImportReturn";
		if (sakaiSession != null) {
			importPanel +=  "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
		}
		switchPanel(state, importPanel);
	}

	public String buildImportReturnPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));

		String returnUrl = (String) state.getAttribute(STATE_CONTENT_ITEM_IMPORT_RETURN_URL);
		String content_item_url = (String) state.getAttribute(STATE_CONTENT_ITEM_CARTRIDGE_URL);
		state.removeAttribute(STATE_CONTENT_ITEM_CARTRIDGE_URL);
		state.removeAttribute(STATE_CONTENT_ITEM_IMPORT_RETURN_URL);

		context.put("returnUrl", returnUrl);
		context.put("content_item_url", content_item_url);
		return "lti_import_return";
	}


	// This is where we receive a multiple item the Response from the external ContentItem / DeepLink
	// producer - the producer will post to this URL so we need to carefully re-establish the
	// session cookie
	public void doMultipleContentItemResponse(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// If we find our way to an error panel, we need to pass along the session
		// to make sure it is properly re-established
		String sakaiSession = data.getParameters().getString(RequestFilter.ATTR_SESSION);
		String errorPanel = "Error";
		String finishedPanel = "Finished";
		if ( sakaiSession != null ) {
			errorPanel = errorPanel + "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
			finishedPanel = finishedPanel + "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession;
		}

		// Check for a returned error message from LTI
		String lti_errormsg = data.getParameters().getString("lti_errormsg");
		if (lti_errormsg != null && lti_errormsg.trim().length() > 0) {
			addAlert(state, lti_errormsg);
			switchPanel(state, errorPanel);
			return;
		}

		// Check for a returned "note" from LTI
		String lti_msg = data.getParameters().getString("lti_msg");
		if (lti_msg != null) {
			state.setAttribute(STATE_SUCCESS, rb.getString("success.deleted"));
		}

		String flow = data.getParameters().getString(FLOW_PARAMETER);
		if (flow == null) {
			addAlert(state, rb.getString("error.missing.flow"));
			switchPanel(state, errorPanel);
			return;
		}

		String returnUrl = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(data.getParameters().getString("returnUrl"));
		log.debug("flow={} returnUrl={}", flow, returnUrl);

		// Retrieve the tool associated with the content item
		Long toolKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if (toolKey == 0 || toolKey < 0) {
			addAlert(state, rb.getString("error.contentitem.missing"));
			switchPanel(state, errorPanel);
			return;
		}
		LtiToolBean toolBean = ltiService.getToolAsBean(toolKey, getSiteId(state));
		if (toolBean == null) {
			addAlert(state, rb.getString("error.contentitem.missing"));
			switchPanel(state, errorPanel);
			return;
		}

		Properties reqProps;
		JSONArray new_content = new JSONArray();
		int goodcount = 0;
		List<String> failures = new ArrayList<String>();

		// doMultipleContentItemResponse
		// Check if this is Deep Link 1.0 or 2.0
		String id_token = data.getParameters().getString(LTI13JwtUtil.JWT);
		boolean isDeepLink;
		try {
			isDeepLink = DeepLinkResponse.isRequest(id_token);
		} catch (Exception e) {
			addAlert(state, rb.getString("error.deeplink.bad") + " (" + e.getMessage() + ")");
			switchPanel(state, errorPanel);
			return;
		}

		state.removeAttribute(STATE_LINE_ITEM);
		Long contentKey = null;  // Save for later
		if ( isDeepLink ) {
			// Parse and validate the incoming DeepLink
			String keyset = toolBean.getLti13ToolKeyset();
			if (keyset == null) {
				addAlert(state, rb.getString("error.tool.missing.keyset"));
				switchPanel(state, errorPanel);
				return;
			}

			DeepLinkResponse dlr;
			try {
				dlr = SakaiLTIUtil.getDeepLinkFromToken(toolBean, id_token);  // Also checks security
			} catch (Exception e) {
				addAlert(state, rb.getString("error.deeplink.bad") + " (" + e.getMessage() + ")");
				switchPanel(state, errorPanel);
				return;
			}

			JSONArray links = dlr.getDeepLinks();
			if (links == null) {
				addAlert(state, rb.getString("error.deeplink.no.ltilink"));
				switchPanel(state, finishedPanel);
				return;
			}

			for (Object obj : links) {
				if ( ! (obj instanceof JSONObject) ) continue;
				JSONObject item = (JSONObject) obj;
				reqProps = extractLTIDeepLink(item, toolBean.asMap(), toolKey);

				String type = getString(item, DeepLinkResponse.TYPE);
				if (!DeepLinkResponse.TYPE_LTILINKITEM.equals(type)) {
					goodcount++;
					new_content.add(item);
					continue;
				}

				// We need to establish resource link ids for the LTILinkItems
				reqProps = extractLTIContentItem(item, toolBean.asMap(), toolKey);

				String title = reqProps.getProperty(LTIService.LTI_TITLE);
				if (title == null) {
					reqProps.setProperty(LTIService.LTI_TITLE, rb.getString("contentitem.generic.title"));
				}

				String url = reqProps.getProperty("launch");
				if (url == null) {
					log.error("LTILink Item missing launch url {}", toolKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.missing.url"));
					continue;
				}

				// Time to store our content item
				log.debug("Inserting LTILinkItem toolKey={}", toolKey);

				// Does an insert when id is null and update when is is not null
				Object retval = ltiService.insertContent(reqProps, getSiteId(state));
				if (retval instanceof String) {
					log.error("Unable to insert LTILinkItem tool={}", toolKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.content.insert"));
					continue;
				}
				contentKey = (Long) retval;
				String contentUrl = null;
				LtiContentBean contentBean = ltiService.getContentAsBean(contentKey, getSiteId(state));
				if (contentBean != null) {
					contentUrl = ltiService.getContentLaunch(contentBean);
					if (contentUrl != null && contentUrl.startsWith("/")) {
						contentUrl = SakaiLTIUtil.getOurServerUrl() + contentUrl;
					}
				}
				if (contentUrl == null) {
					log.error("Unable to get launch url from contentitem content={}", contentKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.content.launch"));
					continue;
				}
				item.put("launch", contentUrl);

				JSONObject lineItem = getObject(item, DeepLinkResponse.LINEITEM);

				// Create the grade column if necessary - We do it right here instead of using state
				SakaiLineItem sakaiLineItem = null;
				if ( lineItem != null ) {
					String lineItemStr = lineItem.toString();
					try {
						sakaiLineItem = (SakaiLineItem) new ObjectMapper().readValue(lineItemStr, SakaiLineItem.class);
						if ( FLOW_PARAMETER_ASSIGNMENT.equals(flow) ) {
							state.setAttribute(STATE_LINE_ITEM, sakaiLineItem);
						} else {
							handleLineItem(state, sakaiLineItem, toolKey, contentBean != null ? contentBean.asMap() : null);
						}
					} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
						log.warn("Could not parse input as SakaiLineItem {}",lineItemStr);
					}
				}

				item.put("content_key", contentKey);
				item.put("content_newpage", reqProps.getProperty(LTIService.LTI_NEWPAGE));
				item.put("tool_key", toolKey);
				item.put("tool_title", toolBean.getTitle());
				item.put("tool_newpage", (toolBean.getNewpage() != null) ? Long.valueOf(toolBean.getNewpage()) : Long.valueOf(0));
				new_content.add(item);
				goodcount++;
			}

		} else {

			// Parse and validate the incoming ContentItem
			ContentItem contentItem = null;
			try {
				contentItem = SakaiLTIUtil.getContentItemFromRequest(toolBean);
			} catch (Exception e) {
				addAlert(state, rb.getString("error.contentitem.bad") + " (" + e.getMessage() + ")");
				switchPanel(state, errorPanel);
				return;
			}

			JSONArray graph = contentItem.getGraph();
			// Loop through the array of returned items
			// LTI LinkItems need to be inserted
			for (Object i : graph) {
				if (!(i instanceof JSONObject)) {
					continue;
				}
				JSONObject item = (JSONObject) i;
				String type = getString(item, LTIConstants.TYPE);
				if (!ContentItem.TYPE_LTILINKITEM.equals(type)) {
					goodcount++;
					new_content.add(item);
					continue;
				}

				// We need to establish resource link ids for the LTILinkItems
				reqProps = extractLTIContentItem(item, toolBean.asMap(), toolKey);

				String title = reqProps.getProperty(LTIService.LTI_TITLE);
				if (title == null) {
					reqProps.setProperty(LTIService.LTI_TITLE, rb.getString("contentitem.generic.title"));
				}

				String url = reqProps.getProperty("launch");
				if (url == null) {
					log.error("LTILink Item missing launch url {}", toolKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.missing.url"));
					continue;
				}

				// Time to store our content item
				log.debug("Inserting LTILinkItem toolKey={}", toolKey);

				// Does an insert when id is null and update when is is not null
				Object retval = ltiService.insertContent(reqProps, getSiteId(state));
				if (retval instanceof String) {
					log.error("Unable to insert LTILinkItem tool={}", toolKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.content.insert"));
					continue;
				}
				contentKey = (Long) retval;
				String contentUrl = null;
				LtiContentBean contentBean = ltiService.getContentAsBean(contentKey, getSiteId(state));
				if (contentBean != null) {
					contentUrl = ltiService.getContentLaunch(contentBean);
					if (contentUrl != null && contentUrl.startsWith("/")) {
						contentUrl = SakaiLTIUtil.getOurServerUrl() + contentUrl;
					}
				}
				if (contentUrl == null) {
					log.error("Unable to get launch url from contentitem content={}", contentKey);
					log.debug("{}", item);
					failures.add(rb.getString("error.contentitem.content.launch"));
					continue;
				}
				item.put("launch", contentUrl);

				// Extract the lineItem material
				String label = reqProps.getProperty(LTIService.LTI_TITLE);
				JSONObject lineItem = getObject(item, ContentItem.LINEITEM);
				SakaiLineItem sakaiLineItem = null;
				if ( lineItem != null ) {
					String lineItemStr = lineItem.toString();
					try {
						sakaiLineItem = (SakaiLineItem) new ObjectMapper().readValue(lineItemStr, SakaiLineItem.class);
						Double scoreMaximum = ContentItem.getScoreMaximum(lineItem);
						if ( scoreMaximum != null ) sakaiLineItem.scoreMaximum = scoreMaximum;
						if ( FLOW_PARAMETER_ASSIGNMENT.equals(flow) ) {
							state.setAttribute(STATE_LINE_ITEM, sakaiLineItem);
						} else {
							handleLineItem(state, sakaiLineItem, toolKey, contentBean != null ? contentBean.asMap() : null);
						}
					} catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
						log.warn("Could not parse input as SakaiLineItem {}",lineItemStr);
					}
				}

				item.put("content_key", contentKey);
				item.put("content_newpage", reqProps.getProperty(LTIService.LTI_NEWPAGE));
				item.put("tool_key", toolKey);
				item.put("tool_title", toolBean.getTitle());
				item.put("tool_newpage", (toolBean.getNewpage() != null) ? Long.valueOf(toolBean.getNewpage()) : Long.valueOf(0));
				new_content.add(item);
				goodcount++;
			}
		}

		String forward;
		if ( flow.equals(FLOW_PARAMETER_LESSONS) ) {
			if  (contentKey == null ) {
				log.warn("Returning content item to Lessons, but contentKey={}", contentKey);
			}
			if (returnUrl.indexOf("?") > 0) {
			   returnUrl += "&ltiItemId=/blti/" + contentKey;
			} else {
				returnUrl += "?ltiItemId=/blti/" + contentKey;
			}

			log.debug("Lessons flow, redirecting to returnUrl {}", returnUrl);
			state.setAttribute(STATE_REDIRECT_URL, returnUrl);
			forward = "Redirect";
		} else if ( flow.equals(FLOW_PARAMETER_ASSIGNMENT) ) {
			forward = "AssignmentDone";
		} else if ( flow.equals(FLOW_PARAMETER_EDITOR) ) {
			forward = "CKEditorDone";
		} else {
			log.warn("Unhandled flow type {}", flow);
			addAlert(state, "buildContentItemGenericMainPanelContext: Unhandled flow type "+flow);
			switchPanel(state, "Error");
			return;
		}

		log.debug("Forwarding to {}", forward);
		state.setAttribute(STATE_CONTENT_ITEM, new_content);
		state.setAttribute(STATE_CONTENT_ITEM_FAILURES, failures);
		state.setAttribute(STATE_CONTENT_ITEM_SUCCESSES, Integer.valueOf(goodcount));

		if (sakaiSession == null) {
			switchPanel(state, forward);
		} else {
			switchPanel(state, forward + "&" + RequestFilter.ATTR_SESSION + "=" + sakaiSession);
		}
	}

	public Properties extractLTIContentItem(JSONObject item, Map<String, Object> tool, Long toolKey) {

		// Parse the returned information to insert a Content Item
		/* {
			"@type": "LtiLinkItem",
			"@id": ":item2",
			"text": "The mascot for the Sakai Project",
			"title": "The fearsome mascot of the Sakai Project",
			"url": "http:\/\/localhost:8888\/sakai-api-test\/tool.php?sakai=98765",
			"mediaType" : "application/vnd.ims.lti.v1.ltilink",
			"icon": {
				"@id": "fa-bullseye",
				"width": 50,
				"height": 50
			},
			"lineItem" : {
				"@type" : "LineItem",
				"label" : "Chapter 12 quiz",
				"reportingMethod" : "res:totalScore",
				"assignedActivity" : {
				"@id" : "http://toolprovider.example.com/assessment/66400",
				"activity_id" : "a-9334df-33"
			},
			"custom": {
				"imscert": "launch\u00bbWtSsVIge"
			}
		   }
		} */
		String title = getString(item, ContentItem.TITLE);
		String text = getString(item, ContentItem.TEXT);
		String url = getString(item, ContentItem.URL);
		// If the URL is empty, assume it is the same as the launch URL
		if (url == null) {
			url = (String) tool.get(LTIService.LTI_LAUNCH);
		}

		JSONObject lineItem = getObject(item, ContentItem.LINEITEM);
		JSONObject custom = getObject(item, ContentItem.CUSTOM);
		String custom_str = "";
		if (custom != null) {
			Iterator<String> i = custom.keySet().iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				String value = (String) custom.get(key);
				custom_str += key + "=" + value + "\n";
			}
		}

		// Much prefer this be an icon style like LTI 2.0
		JSONObject iconObject = getObject(item, ContentItem.ICON);
		String icon = getString(iconObject, "fa_icon");

		JSONObject placementAdvice = getObject(item, ContentItem.PLACEMENTADVICE);
		String  presentationDocumentTarget = getString(placementAdvice, ContentItem.PRESENTATION_DOCUMENT_TARGET);

		// Prepare data for the next phase
		Properties reqProps = new Properties();
		reqProps.setProperty(LTIService.LTI_CONTENTITEM, item.toString());
		reqProps.setProperty("tool_id", toolKey + "");
		if (url != null) {
			reqProps.setProperty("launch", url);
		}
		if (title == null) {
			title = text;
		}
		if (text == null) {
			text = title;
		}
		if (title != null) {
			reqProps.setProperty(LTIService.LTI_TITLE, title);
		}
		if (text != null) {
			reqProps.setProperty(LTIService.LTI_DESCRIPTION, text);
		}
		if (icon != null) {
			reqProps.setProperty(LTIService.LTI_FA_ICON, icon);
		}
		if (custom_str.length() > 0) {
			reqProps.setProperty(LTIService.LTI_CUSTOM, custom_str);
		}
		reqProps.setProperty(LTIService.LTI_NEWPAGE, ContentItem.PRESENTATION_DOCUMENT_TARGET_IFRAME.equals(presentationDocumentTarget) ? "0" : "1");

		return reqProps;
	}

	public Properties extractLTIDeepLink(JSONObject item, Map<String, Object> tool, Long toolKey) {

		// Parse the returned information to insert a Content Item from a Deep Link
		/* {
                "type": "ltiResourceLink",
                "title": "Breakout",
                "url": "http:\/\/localhost:8888\/tsugi\/mod\/breakout\/",
                "presentation": {
                    "documentTarget": "iframe",
                    "width": 500,
                    "height": 600
                },
                "icon": {
                    "url": "http:\/\/localhost:8888\/tsugi-static\/font-awesome-4.7.0\/png\/gamepad.png",
                    "fa_icon": "fa-gamepad",
                    "width": 100,
                    "height": 100
                },
                "thumbnail": {
                    "url": "https:\/\/lti.example.com\/thumb.jpg",
                    "width": 90,
                    "height": 90
                },
                "lineItem": {
                    "scoreMaximum": 10,
                    "label": "Breakout",
                    "resourceId": "breakout",
                    "tag": "originality",
                    "guid": "http:\/\/localhost:8888\/tsugi\/lti\/activity\/breakout"
                },
				"available": {
					"startDateTime": "2018-02-06T20:05:02Z",
					"endDateTime": "2018-03-07T20:05:02Z"
				},
				"submission": {
					"endDateTime": "2018-03-06T20:05:02Z"
				},
				"custom": {
					"quiz_id": "az-123",
					"duedate": "$Resource.submission.endDateTime"
				},
                "window": {
                    "targetName": "examplePublisherContent"
                },
                "iframe": {
                    "height": 890
                }
            }
		*/
		String title = getString(item, DeepLinkResponse.TITLE);

		String text = getString(item, DeepLinkResponse.TEXT);
		String url = getString(item, DeepLinkResponse.URL);
		// If the URL is empty, assume it is the same as the launch URL
		if (url == null) {
			url = (String) tool.get(LTIService.LTI_LAUNCH);
		}

		JSONObject custom = getObject(item, DeepLinkResponse.CUSTOM);
		String custom_str = "";
		if (custom != null) {
			Iterator<String> i = custom.keySet().iterator();
			while (i.hasNext()) {
				String key = (String) i.next();
				String value = (String) custom.get(key);
				custom_str += key + "=" + value + "\n";
			}
		}

		// Much prefer this be an icon style like LTI 2.0
		JSONObject iconObject = getObject(item, DeepLinkResponse.ICON);
		String icon = getString(iconObject, "fa_icon");
		if (icon == null) {
			icon = getString(iconObject, "url");
			if (icon != null) {
				if (!icon.startsWith("fa-")) {
					icon = null;
				}
			}
		}

		// Prepare data for the next phase
		Properties reqProps = new Properties();
		reqProps.setProperty(LTIService.LTI_CONTENTITEM, item.toString());
		reqProps.setProperty("tool_id", toolKey + "");
		if (url != null) {
			reqProps.setProperty("launch", url);
		}
		if (title == null) {
			title = text;
		}
		if (text == null) {
			text = title;
		}
		if (title != null) {
			reqProps.setProperty(LTIService.LTI_TITLE, title);
		}
		if (text != null) {
			reqProps.setProperty(LTIService.LTI_DESCRIPTION, text);
		}
		if (icon != null) {
			reqProps.setProperty(LTIService.LTI_FA_ICON, icon);
		}
		if (custom_str.length() > 0) {
			reqProps.setProperty(LTIService.LTI_CUSTOM, custom_str);
		}
		// The tool does not express a target preference one way or the other in DeepLink
		reqProps.setProperty(LTIService.LTI_NEWPAGE, "1");

		return reqProps;
	}

	public String buildCKEditorDonePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		JSONArray new_content = (JSONArray) state.getAttribute(STATE_CONTENT_ITEM);
		List<String> failures = (List<String>) state.getAttribute(STATE_CONTENT_ITEM_FAILURES);
		Integer goodcount = (Integer) state.getAttribute(STATE_CONTENT_ITEM_SUCCESSES);
		state.removeAttribute(STATE_CONTENT_ITEM);
		state.removeAttribute(STATE_CONTENT_ITEM_FAILURES);
		state.removeAttribute(STATE_CONTENT_ITEM_SUCCESSES);
		context.put("new_content", new_content);
		context.put("goodcount", goodcount);
		if (failures.size() > 0) {
			context.put("failures", failures);
		}
		return "lti_editor_done";
	}

	public String buildRedirectPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if (returnUrl == null) {
			return "lti_content_redirect";
		}
		log.debug("Redirecting parent frame back to={}", returnUrl);
		if (!returnUrl.startsWith("about:blank")) {
			context.put("returnUrl", returnUrl);
		}
		return "lti_content_redirect";
	}

	public String buildForwardPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if (returnUrl == null) {
			return "lti_content_redirect";
		}
		log.debug("Forwarding frame to={}", returnUrl);
		context.put("forwardUrl", returnUrl);
		return "lti_content_redirect";
	}

	// Configure/Select Sakai Content item and forward back to our caller when done

	// This can be entered from many paths
	// - buildContentItemGenericMainPanelContext comes here when it is placing a
	//   non CI/DL Sakai content item
	// - From doSingleContentItemResponse after it has received a CI/DL response
	//   and it is ready to make the Sakai contentItem a SakaiLineItem may be in the state
	//
	// It needs a returnUrl to know where to go after its work is done.
	//
	// Add New: panel=ContentConfig&tool_id=14
	//    - For a normal launchable tool, this makes the Sakai content item
	//    - For a CI/DL provider this will launch the provider, route the response
	//      to doSingleContentItemResponse which will route back here
	//      A SakaiLineItem may be in the state
	//
	// Edit existing launchable Sakai content item: panel=ContentConfig&id=12
	// Note that this pattern is used in Lessons when editing an existing item.

	public String buildContentConfigPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_LINE_ITEM);

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		state.removeAttribute(STATE_POST);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		String returnUrl = data.getParameters().getString("returnUrl");
		if (returnUrl == null && previousPost != null) {
			returnUrl = previousPost.getProperty("returnUrl");
		}
		if (returnUrl == null) {
			addAlert(state, rb.getString("error.missing.return"));
			return "lti_error";
		}

		String flow = data.getParameters().getString(FLOW_PARAMETER);
		if (flow == null && previousPost != null) {
			flow = previousPost.getProperty(FLOW_PARAMETER);
		}
		String secretonly = data.getParameters().getString(SECRETONLY_PARAMETER);
		if (secretonly == null && previousPost != null) {
			secretonly = previousPost.getProperty(SECRETONLY_PARAMETER);
		}
		log.debug("buildContentConfigPanelContext flow={} secretonly={}", flow, secretonly);

		Map<String, Object> content = null;
		Map<String, Object> tool = null;

		Long toolKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));

		Long contentKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_ID));
		if (contentKey == null && previousPost != null) {
			contentKey = LTIUtil.toLongNull(previousPost.getProperty(LTIService.LTI_ID));
		}
		if (contentKey != null) {
			content = ltiService.getContent(contentKey, getSiteId(state));
			if (content == null) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_error";
			}
			toolKey = LTIUtil.toLongNull(content.get(LTIService.LTI_TOOL_ID));
		}
		if (toolKey == null && previousPost != null) {
			toolKey = LTIUtil.toLongNull(previousPost.getProperty(LTIService.LTI_TOOL_ID));
		}
		if (toolKey != null) {
			tool = ltiService.getTool(toolKey, getSiteId(state));
		}

		// No matter what, we must have a tool
		if (tool == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		// Create a POSTable URL back to this application with the right parameters
		// Since the external tool will be setting all the POST data we need to
		// include GET data for things that we might normally have sent as "hidden" data
		/**
		 * The name of the system property that will be used when setting the
		 * value of the session cookie.
		 */
		String suffix = System.getProperty(SAKAI_SERVERID);

		String sessionid = "Missing";
		Session s = SessionManager.getCurrentSession();
		if (s != null) {
			sessionid = s.getId();
		}

		// In this flow we are only asking for one CI/DL Response
		Placement placement = toolManager.getCurrentPlacement();
		String contentReturn = serverConfigurationService.getToolUrl() + "/" + placement.getId()
				+ "/sakai.lti.admin.helper.helper"
				+ "?eventSubmit_doSingleContentItemResponse=Save"
				+ "&" + FLOW_PARAMETER + "=" + flow
				+ "&" + RequestFilter.ATTR_SESSION + "=" + URLEncoder.encode(sessionid + "." + suffix)
				+ "&returnUrl=" + Base64DoubleUrlEncodeSafe.encode(returnUrl)
				+ "&panel=PostContentItem"
				+ "&tool_id=" + tool.get(LTIService.LTI_ID);

		// Add CSRF protection so it actually makes it into the "do" code
		contentReturn = SakaiLTIUtil.addCSRFToken(contentReturn);

		// /acccess/blti/context/tool:12 (does not have a querystring)
		String contentLaunch = ltiService.getToolLaunch(tool, placement.getContext());
		if ( contentLaunch.indexOf("?") > 1 ) {
			contentLaunch += "&flow=" + flow;
		} else {
			contentLaunch += "?flow=" + flow;
		}

		// Can set ContentItemSelection launch values or put in our own data items
		// which will come back later.  Be mindful of GET length limitations enroute
		// to the access servlet.
		Properties contentData = new Properties();
		contentData.setProperty(ContentItem.ACCEPT_MULTIPLE, "false");
		if ( FLOW_PARAMETER_IMPORT.equals(flow) ) {
			contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_CC);
		} else {
			contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_LTILINKITEM);
		}
		contentData.setProperty("flow", flow);  // An example
		contentData.setProperty("remember", "always bring a towel");  // An example

		// Run the contentreturn through the forward servlet
		contentReturn = serverConfigurationService.getServerUrl() + "/imsoidc/lti13/resigncontentitem?forward=" +
			Base64DoubleUrlEncodeSafe.encode(contentReturn) + "&tool_id=" + tool.get(LTIService.LTI_ID);

		contentLaunch = ContentItem.buildLaunch(contentLaunch, contentReturn, contentData);

		Object previousData = null;
		if (content != null) {
			previousData = content;
		} else {
			Properties previousProps = previousPost != null ? previousPost : new Properties();

			String tool_fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
			if (StringUtils.isEmpty(previousProps.getProperty(LTIService.LTI_FA_ICON)) && StringUtils.isNotEmpty(tool_fa_icon) ) {
				previousProps.setProperty(LTIService.LTI_FA_ICON, tool_fa_icon);
			}

			String toolTitle = (String) tool.get(LTIService.LTI_TITLE);
			if (StringUtils.isEmpty(previousProps.getProperty(LTIService.LTI_TITLE)) && StringUtils.isNotEmpty(toolTitle) ) {
				previousProps.setProperty(LTIService.LTI_TITLE, toolTitle);
			}

			String toolDescription = (String) tool.get(LTIService.LTI_DESCRIPTION);
			if (StringUtils.isEmpty(previousProps.getProperty(LTIService.LTI_DESCRIPTION)) && StringUtils.isNotEmpty(toolDescription) ) {
				previousProps.setProperty(LTIService.LTI_DESCRIPTION, toolDescription);
			}

			previousData = previousProps;
		}

		// We will handle the tool_id and SITE_ID fields ourselves in the Velocity code
		String[] contentForm = foorm.filterForm(null, ltiService.getContentModel(toolKey, getSiteId(state)), null, "^tool_id:.*|^SITE_ID:.*");
		if (contentForm == null) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		// Check if we are supposed to let the tool configure itself
		Long allowLinkSelection = LTIUtil.toLong(tool.get(LTIService.LTI_MT_LINKSELECTION));
		Long allowLaunch = LTIUtil.toLong(tool.get(LTIService.LTI_MT_LAUNCH));
		// SAK-47867 - If both are set, prefer DeepLink / Content Item
		if ( allowLinkSelection > 0 ) allowLaunch = 0L;

		context.put("isAdmin", Boolean.valueOf(ltiService.isAdmin(getSiteId(state))));
		context.put("doAction", BUTTON + "doContentPut");
		// Assignment flow does not want a cancel button - just close the modal
		if (!FLOW_PARAMETER_ASSIGNMENT.equals(flow) && !returnUrl.startsWith("about:blank")) {
			context.put("cancelUrl", returnUrl);
		}
		context.put("returnUrl", returnUrl);
		if (allowLinkSelection > 0) {
			context.put("contentLaunch", contentLaunch);
		}
		// If this tool only allows configuration, go straight to Content Item
		if (allowLinkSelection > 0 && allowLaunch < 1) {
			context.put("autoLaunch", contentLaunch);
		}
		context.put(LTIService.LTI_TOOL_ID, toolKey);
		context.put("tool_title", tool.get(LTIService.LTI_TITLE));
		context.put("tool_launch", tool.get(LTIService.LTI_LAUNCH));

		String toolDescription = StringEscapeUtils.escapeHtml4((String) tool.get(LTIService.LTI_DESCRIPTION));
		Optional<String> descriptionField = Arrays.stream(LTIService.TOOL_MODEL).filter(e -> e.startsWith("description")).findFirst();
		if(descriptionField.isPresent()){
			String descriptionFormInput = foorm.formInput(null, descriptionField.get(), rb);
			toolDescription = StringUtils.isNotEmpty(toolDescription) ? StringUtils.replace(descriptionFormInput, "</textarea>", toolDescription + "</textarea>") : descriptionFormInput;
		}
		context.put("tool_description", toolDescription);

		String key = (String) tool.get(LTIService.LTI_CONSUMERKEY);
		String secret = (String) tool.get(LTIService.LTI_SECRET);
		if (LTIService.LTI_SECRET_INCOMPLETE.equals(secret) && LTIService.LTI_SECRET_INCOMPLETE.equals(key)) {
			String keyField = foorm.formInput(null, "consumerkey:text:label=need.tool.key:required=true:maxlength=255", rb);
			context.put("keyField", keyField);
			String secretField = foorm.formInput(null, "secret:text:required=true:label=need.tool.secret:maxlength=255", rb);
			context.put("secretField", secretField);
		}

		String formInput = ltiService.formInput(previousData, contentForm);
		context.put("formInput", formInput);
		context.put("flow", flow);
		context.put("secretonly", secretonly);

		return "lti_content_config";
	}

	// Entry point for when the editor wants to pick one or more LTI tools
	// portal/portal-service-impl/impl/src/java/org/sakaiproject/portal/service/PortalServiceImpl.java
	// getContentItemUrl(Site site)
	// library/src/webapp-filtered/editor/ckeditor.launch.js
	public String buildCKEditorPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		return buildContentItemGenericMainPanelContext(portlet, context, data, state, FLOW_PARAMETER_EDITOR);
	}

	// Entry point for when the Assignments tool wants to pick an LTI tool
	// assignment/tool/src/java/org/sakaiproject/assignment/tool/AssignmentAction.java
	public String buildAssignmentsMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		return buildContentItemGenericMainPanelContext(portlet, context, data, state, FLOW_PARAMETER_ASSIGNMENT);
	}

	// Entry point for when the Lessons tool wants to pick an LTI tool
	// lessonbuilder/tool/src/java/org/sakaiproject/lessonbuildertool/tool/producers/BltiPickerProducer.java
	public String buildLessonsMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		return buildContentItemGenericMainPanelContext(portlet, context, data, state, FLOW_PARAMETER_LESSONS);
	}

	public String buildContentItemMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		String flow = data.getParameters().getString(FLOW_PARAMETER);
		if (flow == null) {
			flow = FLOW_PARAMETER_EDITOR;
			log.debug("Assuming flow = {}", flow);
		}
		return buildContentItemGenericMainPanelContext(portlet, context, data, state, flow);
	}

	// Generic panel for ContentItem support (Lessons, CKEditor or Assignments)
	// Add New: panel=ContentItemGenericMain&tool_id=14
	// Edit existing: panel=ContentItemGenericMain&id=12
	//
	// Based on the value for flow, this assembles a list of tools that
	// are appropriate for the flow.  If there are none, we display an error
	// if there is one or we have a tool_id parameter - we bypass showing the list
	// to allow the user to choose.  If we show a list, the user comes back
	// here when they pick an item.
	//
	// Once we have one tool, we decide if it is a Content Item producer, Deep Link
	// producer or just plain old tool and proceed appropriately.

	// Current 2020-07: Lessons does not call this because it has its own UI to pick amongst the tools so it calls
	// the CI/DL endpoint from Lessons (BltiEntity.java) and when the CI/DL selection is done, the CI/DL response
	// is posted to ContentConfigPanel

	public String buildContentItemGenericMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state, String flow) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_LINE_ITEM);

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		state.removeAttribute(STATE_POST);

		Properties reqProps = data.getParameters().getProperties();
		String returnUrl = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(data.getParameters().getString("returnUrl"));

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		// If we are in the middle..
		if (previousPost != null) {
			String previousFlow = previousPost.getProperty(FLOW_PARAMETER);
			if ( previousFlow != null ) flow = previousFlow;
		}

		Long toolKey = LTIUtil.toLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));

		log.debug("flow={} toolKey={} returnUrl={}", flow, toolKey, returnUrl);

		Placement placement = toolManager.getCurrentPlacement();

		// Get the lauchable and content editor tools...
		List<Map<String, Object>> allTools;
		if ( FLOW_PARAMETER_ASSIGNMENT.equals(flow) ) {
			allTools = ltiService.getToolsAssessmentSelection(placement.getContext());
		} else if ( FLOW_PARAMETER_EDITOR.equals(flow) ) {
			allTools = ltiService.getToolsContentEditor(placement.getContext());
		} else if ( FLOW_PARAMETER_LESSONS.equals(flow) ) {
			allTools = ltiService.getToolsLessonsSelection(placement.getContext());
		} else {
			log.debug("Unhandled flow type {}", flow);
			context.put("alertMessage", "buildContentItemGenericMainPanelContext: Unhandled flow type "+flow);
			return "lti_error";
		}

		// Split between CI/DL tools and direct launch tools
		List<Map<String, Object>> toolsCI = new ArrayList<Map<String, Object>> ();
		List<Map<String, Object>> toolsLaunch = new ArrayList<Map<String, Object>> ();
		for (Map<String, Object> lt : allTools) {
			Long isCI = LTIUtil.toLong(lt.get(LTIService.LTI_MT_LINKSELECTION));
			if ( isCI > 0 ) {
				toolsCI.add(lt);
			}

			if (LTIUtil.toLong(lt.get(LTIService.LTI_MT_LAUNCH)) > 0) {
				toolsLaunch.add(lt);
			}
		}

		// We have constructed a list of tools suitable for this flow
		// It may include ContentItem or DeepLink producers or just a plain old launchable tool

		// If we have no tools at all, punt and tell the user...
		if ((toolsLaunch.size() + toolsCI.size()) < 1) {
			return "lti_editor_select";
		}

		// If there is only one CI/DL tool in our list - pretend the user picked it
		// and directly launch it without presenting a list
		Map<String, Object> tool = null;
		boolean doContent = false;
		if (toolsCI.size() == 1 && toolsLaunch.size() == 0) {
			doContent = true;
			tool = toolsCI.get(0);
		}

		// If we don't already have a tool loaded, look through the CL/DL
		// producers to find the tool_id the user selected from the list
		if (toolKey != null && tool == null) {
			for (Map<String, Object> t : toolsCI) {
				Long editKey = LTIUtil.toLongNull(t.get(LTIService.LTI_ID));
				log.debug("CI/DL editKey={}", editKey);
				if (toolKey.equals(editKey)) {
					doContent = true;
					tool = t;
					break;
				}
			}
		}

		// See if the user selected a regular directly launchable tool...
		if (toolKey != null && tool == null) {
			for (Map<String, Object> t : toolsLaunch) {
				Long editKey = LTIUtil.toLongNull(t.get(LTIService.LTI_ID));
				log.debug("RL editKey={}", editKey);
				if (toolKey.equals(editKey)) {
					doContent = false;
					tool = t;
					break;
				}
			}
		}

		// If at this point, we are not down to a single tool - give the user the list to select from
		// And then come back through this logic
		if (tool == null) {
			// TODO: Convert to Bean usage when working on Assignment and Lessons flows
			// This requires adding Bean versions of getToolsAssessmentSelection(), getToolsContentEditor(), 
			// and getToolsLessonsSelection() to LTIService, plus thorough testing of all flow paths
			context.put("allTools", allTools);
			context.put("returnUrl", Base64DoubleUrlEncodeSafe.encode(returnUrl));
			context.put("flow", flow);
			return "lti_editor_select";
		}

		// Now we have a tool selected from the list of tools
		log.debug("We have a single tool={} doContent={}", tool.get(LTIService.LTI_ID) ,doContent);

		String sessionid = "Missing";
		Session s = SessionManager.getCurrentSession();
		if (s != null) {
			sessionid = s.getId();
		}
		String suffix = System.getProperty(SAKAI_SERVERID);

		// If this is not a CI/DL launch, lets send to the ContentConfig panel
		// to make the content item in Sakai.  We include a URL where ContentConfig
		// will go to afterwards, passing along the flow parameter
		if (!doContent) {
			String postContentConfigUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId()
					+ "/sakai.lti.admin.helper.helper"
					+ "?panel=PostContentConfig"
					+ "&" + FLOW_PARAMETER + "=" + flow
					+ "&returnUrl=" + Base64DoubleUrlEncodeSafe.encode(returnUrl)
					+ "&" + RequestFilter.ATTR_SESSION + "=" + URLEncoder.encode(sessionid + "." + suffix);

			log.debug("postContentConfigUrl = {}", postContentConfigUrl);

			String configUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId()
					+ "/sakai.lti.admin.helper.helper"
					+ "?panel=ContentConfig"
					+ "&tool_id=" + tool.get(LTIService.LTI_ID)
					+ "&" + FLOW_PARAMETER + "=" + flow
					+ "&returnUrl=" + Base64DoubleUrlEncodeSafe.encode(postContentConfigUrl)
					+ "&" + RequestFilter.ATTR_SESSION + "=" + URLEncoder.encode(sessionid + "." + suffix);
			log.debug("Forwarding non CI/DL tool to {}", configUrl);
			context.put("forwardUrl", configUrl);
			return "lti_content_redirect";
		}

		// If the selected tool is a CI/DL tool, we start the CI/DL flow
		String contentReturn = serverConfigurationService.getToolUrl() + "/" + placement.getId()
				+ "/sakai.lti.admin.helper.helper"
				+ "?eventSubmit_doMultipleContentItemResponse=Save"
				+ "&" + FLOW_PARAMETER + "=" + flow
				+ "&returnUrl=" + Base64DoubleUrlEncodeSafe.encode(returnUrl)
				+ "&" + RequestFilter.ATTR_SESSION + "=" + URLEncoder.encode(sessionid + "." + suffix)
				+ "&panel=PostContentItem"
				+ "&tool_id=" + tool.get(LTIService.LTI_ID);

		// Add CSRF protection so it actually makes it into the "do" code
		contentReturn = SakaiLTIUtil.addCSRFToken(contentReturn);

		// /acccess/blti/context/tool:12 (does not have a querystring)
		String contentLaunch = ltiService.getToolLaunch(tool, placement.getContext());
		if ( contentLaunch.indexOf("?") > 1 ) {
			contentLaunch += "&flow=" + flow;
		} else {
			contentLaunch += "?flow=" + flow;
		}

		// Can set ContentItemSelection launch values or put in our own data items
		// which will come back later.  Be mindful of GET length limitations enroute
		// to the access servlet.
		Properties contentData = new Properties();

		// Lessons and Assignments only want one returned value
		if ( flow.equals(FLOW_PARAMETER_ASSIGNMENT) || flow.equals(FLOW_PARAMETER_LESSONS) ) {
			contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_LTILINKITEM);
			contentData.setProperty(ContentItem.ACCEPT_MULTIPLE, "false");
		} else {
			contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_ALL);
			contentData.setProperty(ContentItem.ACCEPT_MULTIPLE, "true");
		}
		contentData.setProperty("remember", "the answer is 42");  // An example

		// Run the contentreturn through the forward servlet
		contentReturn = serverConfigurationService.getServerUrl() + "/imsoidc/lti13/resigncontentitem?forward=" +
			Base64DoubleUrlEncodeSafe.encode(contentReturn) + "&tool_id=" + tool.get(LTIService.LTI_ID);

		// This will forward to AccessServlet / LTISecurityServiceImpl with a tool: url
		// AccessServlet will detect if this is a CI or DL and handle it accordingly using
		// code from SakaiLTIUtil - so we don't need two code paths here.
		contentLaunch = ContentItem.buildLaunch(contentLaunch, contentReturn, contentData);
		log.debug("Redirecting to DL/CI launch to={}", contentLaunch);
		context.put("forwardUrl", contentLaunch);
		return "lti_content_redirect";
	}

	// This is called in the non CI/DL flow when we are done configuring a Sakai content item
	public String buildPostContentConfigPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {

		String flow = data.getParameters().getString(FLOW_PARAMETER);
		String returnUrl = Base64DoubleUrlEncodeSafe.decodeDoubleSafe(data.getParameters().getString("returnUrl"));

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);

		String id = data.getParameters().getString("ltiItemId");
		Long contentKey = null;
		if (id != null && id.startsWith("/blti/")) {
			String cid = id.substring(6);
			contentKey = LTIUtil.toLongNull(cid);
		}

		String contentUrl = null;
		LtiContentBean contentBean = ltiService.getContentAsBean(contentKey, getSiteId(state));

		// Rare: We just made this a few clicks ago...
		if (contentBean == null) {
			log.error("Unable to load content={}", contentKey);
			addAlert(state, rb.getString("error.contentitem.content.launch"));
			return "lti_error";
		}

		contentUrl = ltiService.getContentLaunch(contentBean);
		if (contentUrl != null && contentUrl.startsWith("/")) {
			contentUrl = SakaiLTIUtil.getOurServerUrl() + contentUrl;
		}

		if (contentUrl == null) {
			log.error("Unable to get launch url from contentitem content={}", contentKey);
			addAlert(state, rb.getString("error.contentitem.content.launch"));
			return "lti_error";
		}

		Long key = contentBean.getToolId();
		LtiToolBean toolBean = ltiService.getToolAsBean(key, getSiteId(state));

		JSONArray new_content = new JSONArray();

		JSONObject item = (JSONObject) new JSONObject();
		item.put(LTIConstants.TYPE, ContentItem.TYPE_LTILINKITEM);
		item.put("launch", contentUrl);
		String title = contentBean.getTitle();
		if (title == null) {
			title = rb.getString("contentitem.generic.title");
		}
		item.put(ContentItem.TITLE, title);

		Long contentNewPage = (contentBean.getNewpage() != null && contentBean.getNewpage()) ? Long.valueOf(1) : Long.valueOf(0);
		item.put("content_newpage", contentNewPage);

		Long toolNewPage = Long.valueOf(LTIService.LTI_TOOL_NEWPAGE_CONTENT);
		if ( toolBean != null ) {
			toolNewPage = (toolBean.getNewpage() != null) ? Long.valueOf(toolBean.getNewpage()) : Long.valueOf(LTIService.LTI_TOOL_NEWPAGE_CONTENT);
			item.put("tool_newpage", toolNewPage);
		}

		new_content.add(item);

		context.put("new_content", new_content);

		log.debug("contentKey={} flow={} returnUrl={}", contentKey, flow, returnUrl);

		if ( FLOW_PARAMETER_ASSIGNMENT.equals(flow) ) {
			context.put("contentId",  contentKey);
			context.put("contentTitle", contentBean.getTitle());
			context.put("contentLaunchNewWindow", contentNewPage);
			context.put("contentToolNewpage", toolNewPage);

			SakaiLineItem sakaiLineItem = (SakaiLineItem) state.getAttribute(STATE_LINE_ITEM);
			state.removeAttribute(STATE_LINE_ITEM);
			context.put("lineItem", sakaiLineItem);

			if ( toolBean != null ) {
				context.put("toolTitle", toolBean.getTitle());
			} else {
				context.put("toolTitle", contentBean.getTitle());
			}
			return "lti_assignment_return";
		} else if ( flow.equals(FLOW_PARAMETER_LESSONS) ) {
			if  (contentKey == null ) {
				log.warn("Returning content item to Lessons, but contentKey={}", contentKey);
			}
			if (returnUrl.indexOf("?") > 0) {
			   returnUrl += "&ltiItemId=/blti/" + contentKey;
			} else {
				returnUrl += "?ltiItemId=/blti/" + contentKey;
			}

			log.debug("Lessons flow, redirecting to returnUrl {}", returnUrl);
			context.put("returnUrl", returnUrl);
			return "lti_content_redirect";
		} else if ( flow.equals(FLOW_PARAMETER_EDITOR) ) {
			context.put("new_content", new_content);
			context.put("goodcount", Integer.valueOf(1));
			return "lti_editor_done";
		} else {
			log.warn("Unhandled flow type {}", flow);
			context.put("alertMessage", "buildPostContentConfigPanelContext: Unhandled flow type "+flow);
			return "lti_error";
		}

	}

	public String buildAssignmentDonePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		JSONArray new_content = (JSONArray) state.getAttribute(STATE_CONTENT_ITEM);
		List<String> failures = (List<String>) state.getAttribute(STATE_CONTENT_ITEM_FAILURES);
		state.removeAttribute(STATE_CONTENT_ITEM);
		state.removeAttribute(STATE_CONTENT_ITEM_FAILURES);
		state.removeAttribute(STATE_CONTENT_ITEM_SUCCESSES);
		context.put("new_content", new_content);
		if (failures != null && failures.size() > 0) {
			context.put("failures", failures);
		}

		SakaiLineItem sakaiLineItem = (SakaiLineItem) state.getAttribute(STATE_LINE_ITEM);
		state.removeAttribute(STATE_LINE_ITEM);
		if ( sakaiLineItem != null ) {
			try {
				String lineItemStr = new ObjectMapper().writeValueAsString(sakaiLineItem);
				context.put("lineItem", lineItemStr);
			} catch(com.fasterxml.jackson.core.JsonProcessingException e) {
				log.warn("Unable to serialize sakaiLineItem");
			}
		}

		if ( new_content.size() > 0 ) {
			JSONObject job = (JSONObject) new_content.get(0);
			context.put("contentId", LTIUtil.toLong(job.get("content_key")));
			context.put("contentTitle", (String) job.get("title"));
			context.put("toolTitle", (String) job.get("tool_title"));
		}

		return "lti_assignment_return";
	}


	public String buildContentDeletePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doContentDelete");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_tool_system";
		}
		Long key = Long.valueOf(id);
		org.sakaiproject.lti.beans.LtiContentBean content = ltiService.getContentBean(key, getSiteId(state));
		if (content == null) {
			addAlert(state, rb.getString("error.content.not.found"));
			return "lti_tool_system";
		}
		Long tool_id_long = content.toolId;
		context.put("tool_id_long", tool_id_long);
		context.put("content", content);
		context.put("ltiService", ltiService);

		state.removeAttribute(STATE_SUCCESS);
		return "lti_content_delete";
	}

	// Insert or edit
	public void doContentDelete(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			switchPanel(state, "ToolSite");
			return;
		}
		Long key = Long.valueOf(id);
		// also remove the link
		if (ltiService.deleteContent(key, getSiteId(state))) {
			state.setAttribute(STATE_SUCCESS, rb.getString("success.deleted"));
		} else {
			addAlert(state, rb.getString("error.delete.fail"));
		}

		if (ToolUtils.isInlineRequest(data.getRequest())) {
			switchPanel(state, "ToolSite");
		} else {
			switchPanel(state, "Refresh");
		}
	}

	public String buildLinkAddPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doSiteLink");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_tool_system";
		}
		Long key = Long.valueOf(id);
		org.sakaiproject.lti.beans.LtiContentBean content = ltiService.getContentBean(key, getSiteId(state));
		if (content == null) {
			addAlert(state, rb.getString("error.content.not.found"));
			return "lti_tool_system";
		}
		context.put("content", content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_add";
	}

	// Insert or edit
	public void doSiteLink(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		String button_text = data.getParameters().getString("button_text");

		Object retval = ltiService.insertToolSiteLink(id, button_text, getSiteId(state));
		if (retval instanceof String) {
			String prefix = ((String) retval).substring(0, 2);
			addAlert(state, ((String) retval).substring(2));
			if ("0-".equals(prefix)) {
				switchPanel(state, "Content");
			} else if ("1-".equals(prefix)) {
				switchPanel(state, "Error");
			}
			return;
		}

		state.setAttribute(STATE_SUCCESS, rb.getString("success.link.add"));

		if (ToolUtils.isInlineRequest(data.getRequest())) {
			switchPanel(state, "ToolSite");
		} else {
			switchPanel(state, "Refresh");
		}
	}

	public String buildLinkRemovePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if (!ltiService.isMaintain(getSiteId(state))) {
			addAlert(state, rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doLinkRemove");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if (id == null) {
			addAlert(state, rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = Long.valueOf(id);
		org.sakaiproject.lti.beans.LtiContentBean content = ltiService.getContentBean(key, getSiteId(state));
		if (content == null) {
			addAlert(state, rb.getString("error.content.not.found"));
			return "lti_error";
		}
		context.put("content", content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_remove";
	}

	public void doLinkRemove(RunData data, Context context) {
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String id = data.getParameters().getString(LTIService.LTI_ID);
        Long key = null;
        try {
            key = id == null ? null : Long.valueOf(id);
        } catch (NumberFormatException nfe) {
            addAlert(state, rb.getString("error.id.not.found"));
            switchPanel(state, "Error");
            return;
        }

        String rv = ltiService.deleteContentLink(key, getSiteId(state));
        if (rv != null) {
			// there is error removing the external tool site link
			addAlert(state, rv);
			switchPanel(state, "Error");
			return;
		} else {
			// external tool site link removed successfully
			state.setAttribute(STATE_SUCCESS, rb.getString("success.link.remove"));
			if (ToolUtils.isInlineRequest(data.getRequest())) {
				switchPanel(state, "ToolSite");
			} else {
				switchPanel(state, "Refresh");
			}
		}
	}

	public String buildRefreshPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state) {
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		context.put("messageSuccess", state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_top_refresh";
	}

	//generates a search clause (SEARCH_FIELD_1:SEARCH_VALUE_1[#&#|#\\|#]SEARCH_FIELD_2:SEARCH_VALUE_2[#&#|#\\|#]...[#&#|#\\|#]SEARCH_FIELD_N:SEARCH_VALUE_N) and puts some parameters in the context
	private String buildSearch(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		StringBuilder sb = new StringBuilder();
		Map<String, String> searchMap = (Map<String, String>) state.getAttribute(ATTR_SEARCH_MAP);
		if (searchMap != null) {
			for (String k : searchMap.keySet()) {
				if (sb.length() > 0) {
					sb.append(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND);
				}
				if (StringUtils.isNotEmpty(k) && StringUtils.isNotEmpty((String) searchMap.get(k))) {
					if ("created_at".equals(k)) {
						sb.append(k + ":" + LTIService.LTI_SEARCH_TOKEN_DATE + searchMap.get(k));
					} else {
						sb.append(k + ":" + searchMap.get(k));
						if ("URL".equals(k)) {
							sb.append(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND);
							sb.append("launch:" + LTIService.LTI_SEARCH_TOKEN_NULL);
							sb.append(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR);
							sb.append("launch:" + searchMap.get(k));
						}
					}
				}
			}
			context.put(ATTR_SEARCH_MAP, searchMap);
			context.put(ATTR_SEARCH_LAST_FIELD, state.getAttribute(ATTR_SEARCH_LAST_FIELD));
			if (sb.length() > 0) {
				return sb.toString();
			}
		}
		return null;
	}

	/* Flow from Editor, Assignments, and Lessons

	Editor:

	Starts in PortalServiceImpl.getContentItemUrl()
	buildCKEditorPanelContext
	buildContentItemGenericMainPanelContext
		Make a list of tools - If there is > 1 present list for selection of the tool
		User selects a tool from the list and the UI comes back to buildContentItemGenericMainPanelContext
		For a non-DL / non-CI send to ContentConfig, with return to PostContentConfig
			Allow for multiple items to be selected in the CI/DL launch
			ContentConfig posts to ContentPut -> ContentPutInternal
			ContentPutInternal goes back to the returnUrl (PostContentConfig)
			buildCKEditorDonePanelContext
		For DL/CI - launch to the tool with response sent to doMultipleContentItemResponse
			Mark the CI/DL launch as only allowing a single response
			doMultipleContentItemResponse - loops through graph and makes Sakai content items
			with flow editor send to CKEditorDone
			CKEditorDone sends data to its parent frame and closes itself

	Assignments:

	Starts in AssignmentAction.java
	buildAssignmentsMainPanelContext
	buildContentItemGenericMainPanelContext
		Make a list of tools - If there is > 1 present list for selection of the tool
		User selects a tool from the list and the UI comes back to buildContentItemGenericMainPanelContext
		For a non-DL/non-CI send to ContentConfig, with return to PostContentConfig
			ContentConfig posts to ContentPut -> ContentPutInternal
			ContentPutInternal goes back to the returnUrl (PostContentConfig)
			buildAssignmentDonePanelContext
		For DL/CI- launch to the tool with response sent to doMultipleContentItemResponse
			Only allow a single item to be selected in the CI/DL launch
			doMultipleContentItemResponse - loops through graph and makes Sakai content items
			with flow editor send to AssignmentDone
			AssignmentDone sends data to its parent frame and closes itself

	Lessons:

	Starts in BltiEntity.java
	LTIAdmin.ContentConfig
	buildLessonsMainPanelContext
	buildContentItemGenericMainPanelContext
		Make a list of tools based on placement setting - If there is > 1 present list for selection of the tool
		User selects a tool from the list and the UI comes back to buildContentItemGenericMainPanelContext
		For a non-DL/non-CI send to ContentConfig, with return to PostContentConfig
			ContentConfig posts to ContentPut -> ContentPutInternal
			ContentPutInternal goes back to the returnUrl (PostContentConfig)
			buildAssignmentDonePanelContext
		For DL/CI - launch to the tool with response sent to doMultipleContentItemResponse
			Mark the CI/DL launch as only allowing a single response
			doMultipleContentItemResponse - loops through graph and makes the Sakai content item
			Then augments the returnUrl with the bltiItem and redirects the frame to the
			returnUrl (inside the modal / popup iframe creates by Lessons)

*/
}
