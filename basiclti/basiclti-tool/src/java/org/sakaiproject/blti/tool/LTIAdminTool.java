/********************************************************************************** * $URL$
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
 **********************************************************************************/

package org.sakaiproject.blti.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.lti2.LTI2Config;
import org.tsugi.lti2.LTI2Constants;
import org.tsugi.lti2.LTI2Messages;
import org.tsugi.lti2.ToolProxy;
import org.tsugi.lti2.ContentItem;
import org.tsugi.lti2.ToolProxyBinding;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import static org.tsugi.lti2.LTI2Util.getObject;
import static org.tsugi.lti2.LTI2Util.getString;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
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
import org.sakaiproject.lti2.SakaiLTI2Config;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
// import org.sakaiproject.lti.impl.DBLTIService; // HACK
import org.sakaiproject.util.foorm.SakaiFoorm;

// We need to interact with the RequestFilter
import org.sakaiproject.util.RequestFilter;

/**
 * <p>
 * LTIAdminTool is a Simple Velocity-based Tool
 * </p>
 */
@Slf4j
public class LTIAdminTool extends VelocityPortletPaneledAction
{
	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("ltitool");

	private static String STATE_POST = "lti:state_post";
	private static String STATE_SUCCESS = "lti:state_success";
	private static String STATE_ID = "lti:state_id";
	private static String STATE_TOOL_ID = "lti:state_tool_id";
	private static String STATE_CONTENT_ID = "lti:state_content_id";
	private static String STATE_REDIRECT_URL = "lti:state_redirect_url";
	private static String STATE_LTI2_TOOL_ID = "lti2:state_tool_id";
	private static String STATE_CONTENT_ITEM = "lti:state_content_item";
	private static String STATE_CONTENT_ITEM_FAILURES = "lti:state_content_item_failures";
	private static String STATE_CONTENT_ITEM_SUCCESSES = "lti:state_content_item_successes";

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

	/** Service Implementations */
	protected static ToolManager toolManager = null;
	protected static LTIService ltiService = null;
	protected static ServerConfigurationService serverConfigurationService = null;

	protected static SakaiFoorm foorm = new SakaiFoorm();

	// Should be RequestFilter.SAKAI_SERVERID
	public final static String SAKAI_SERVERID = "sakai.serverId";

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices()
	{
		if ( toolManager == null ) toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");

		/* HACK to save many restarts during development
		   if ( ltiService == null ) {
		   ltiService = (LTIService) new DBLTIService();
		   ((org.sakaiproject.lti.impl.DBLTIService) ltiService).setAutoDdl("true");
		   ((org.sakaiproject.lti.impl.DBLTIService) ltiService).init();
		   }
		   End of HACK */

		if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		if ( serverConfigurationService == null ) serverConfigurationService = (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
	}

	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);
		getServices();

		String siteId = toolManager.getCurrentPlacement().getContext();
		// get site tool mode from tool registry
		if (portlet.getPortletConfig().getInitParameter("helper") != null) {
			String helperSiteId = (String) state.getAttribute(HELPER_ID + ".siteId");
			if (helperSiteId == null) {
				log.warn("No site ID set when in helper mode.");
			} else {
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
			RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_ID);
		state.removeAttribute(STATE_TOOL_ID);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_REDIRECT_URL);
		return "lti_error";
	}

	public String buildMainPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		// default to site view
		return buildToolSystemPanelContext(portlet, context, data, state);
	}

	/**
	 * Filter action : allows to filter tool site list with the given tool_id
	 *
	 * Accepted parameters : id
	 * @param data
	 */
	public void doFilter(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		String id = data.getParameters().getString(PARAM_ID);
		if (StringUtils.isNotEmpty(id)) {
			state.setAttribute(ATTR_FILTER_ID, id);
		}
	}

	/**
	 * Sort action : allows to order the tool site list by the given field (column name). Ascending/Descending order will be detected automatically.
	 *
	 * Accepted parameters : criteria
	 * @param data
	 */
	public void doSort(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		String lastSortedField = (String)state.getAttribute(ATTR_LAST_SORTED_FIELD);
		Boolean ascendingOrder = (Boolean)state.getAttribute(ATTR_ASCENDING_ORDER);

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
	* Accepted parameters : page_event, (optional)pagesize
	* Allowed events : first, prev, next, last
	* @param data
	*/
	public void doChangePage(RunData data)
	{
		//also check if page size has changed
		doChangePageSize(data);

		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		Integer index = (Integer)state.getAttribute(ATTR_SORT_INDEX);
		try {
			if (index == null) {
				index = 0;
			}
			String event = data.getParameters().getString(PARAM_PAGE_EVENT);
			if (StringUtils.isNotEmpty(event)) {
				Integer pageSize = (Integer)state.getAttribute(ATTR_SORT_PAGESIZE);
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
		}
		catch (Exception ex) {}
		state.setAttribute(ATTR_SORT_INDEX, index);
	}

	/**
	* Change page size action : allows to change the page size
	*
	* Accepted parameters : pagesize
	* @param data
	*/
	public void doChangePageSize(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		Integer pageSize = (Integer)state.getAttribute(ATTR_SORT_PAGESIZE);
		try {
			String param = data.getParameters().getString(PARAM_PAGE);
			if (StringUtils.isNotEmpty(param)) {
				pageSize = Integer.parseInt(param);
			}
		}
		catch (Exception ex) {}
		if (pageSize == null || pageSize < 0) {
			pageSize = ELEMENTS_PER_PAGE;
		}
		state.setAttribute(ATTR_SORT_PAGESIZE, pageSize);
	}

	/**
	* Search action : allows to search by a field (column) and value. One action by one search, but multiple search (in different columns) will be accumulative
	*
	* Accepted parameters : field, search
	* @param data
	*/
	public void doSearch(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		String searchField = data.getParameters().getString(PARAM_SEARCH_FIELD);
		String searchValue = data.getParameters().getString(PARAM_SEARCH_VALUE);
		Map<String, String> searchMap = (Map<String, String>)state.getAttribute(ATTR_SEARCH_MAP);
		if (searchMap == null) {
			searchMap = new HashMap<String, String>();
		}
		if (StringUtils.isNotEmpty(searchField)) {
			if (StringUtils.isNotEmpty(searchValue)) {
				searchValue = searchValue.replace(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND, LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_AND);
				searchValue = searchValue.replace(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR, LTIService.ESCAPED_LTI_SEARCH_TOKEN_SEPARATOR_OR);
				searchMap.put(searchField, searchValue);
			}
			else
			{
				searchMap.remove(searchField);
			}
		}
		state.setAttribute(ATTR_SEARCH_MAP, searchMap);
		state.setAttribute(ATTR_SEARCH_LAST_FIELD, searchField);
		state.setAttribute(ATTR_SORT_INDEX, 0);
	}

	/**
	* Reset all paging/sorting/searching fields in the state
	* @param data
	*/
	public void doReset(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		state.setAttribute(ATTR_FILTER_ID, null);
		state.setAttribute(ATTR_SEARCH_MAP, null);
		state.setAttribute(ATTR_SEARCH_LAST_FIELD, null);
		state.setAttribute(ATTR_SORT_INDEX, 0);
		state.setAttribute(ATTR_LAST_SORTED_FIELD, null);
		state.setAttribute(ATTR_ASCENDING_ORDER, true);
	}

	public String buildToolSitePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		context.put("allowMaintainerAddToolSite", serverConfigurationService.getBoolean(ALLOW_MAINTAINER_ADD_TOOL_SITE, true));
		context.put("getContext",toolManager.getCurrentPlacement().getContext());
		context.put("doEndHelper", BUTTON + "doEndHelper");
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);

		String order = (String)state.getAttribute(ATTR_SORT_CRITERIA);

		Integer pageSize = (Integer)state.getAttribute(ATTR_SORT_PAGESIZE);
		if (pageSize == null) {
			pageSize = ELEMENTS_PER_PAGE;
		}

		//build search clause based on parameters and put some of them in the context
		String search = buildSearch(data, context);

		//check for tool filter
		String filterId = (String)state.getAttribute(ATTR_FILTER_ID);
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
		Integer index = (Integer)state.getAttribute(ATTR_SORT_INDEX);
		if (index == null) {
			index = 0;
		}
		if (index == -1) {
			index = (count_contents - 1) / pageSize * pageSize;
		}
		else if (index >= count_contents) {
			index = Math.max(0, count_contents - 1);
		}
		int lastIndex = index + pageSize - 1;

		//put all in the context
		context.put("sortIndex", (index + 1));
		context.put("sortLastIndex", Math.min(lastIndex + 1, count_contents));
		context.put("sortPageSize", pageSize);
		context.put(ATTR_LAST_SORTED_FIELD, state.getAttribute(ATTR_LAST_SORTED_FIELD));
		context.put(ATTR_ASCENDING_ORDER, state.getAttribute(ATTR_ASCENDING_ORDER));

		// this is for the "site tools" panel
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		if (count_contents > 0) {
			Map<String, String> siteURLMap = new HashMap<String, String>(); //cache for site URL
			contents = (List<Map<String, Object>>)ltiService.getContents(search, order, index, lastIndex, getSiteId(state));
			for ( Map<String,Object> content : contents ) {

				Long tool_id_long = null;
				try{
					tool_id_long = new Long(content.get(LTIService.LTI_TOOL_ID).toString());
				}
				catch (Exception e)
				{
					// log the error
					log.error("error parsing tool id {}", content.get(LTIService.LTI_TOOL_ID));
				}
				content.put("tool_id_long", tool_id_long);
				String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
				ToolConfiguration tool = SiteService.findTool(plstr);
				if ( tool == null ) {
					content.put(LTIService.LTI_PLACEMENT, null);
				}

				//get site url based on site id
				String siteId = (String) content.get(LTIService.LTI_SITE_ID);
				try {
					//look for it in the cache
					String url = siteURLMap.get(siteId);
					if(url == null) {
						url = SiteService.getSite(siteId).getUrl();
						siteURLMap.put(siteId, url);
					}
					content.put("site_url", url);
				} catch(Exception e)
				{
					log.error("error getting url for site {}", siteId);
				}

				//get LTI url based on site id and tool id
				content.put("tool_url", "/access/basiclti/site/"+siteId+"/content:"+content.get(LTIService.LTI_ID));
			}
		}
		context.put("contents", contents);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));

		//put velocity date tool in the context
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, rb.getLocale());
		context.put("dateTool", df);

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
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String contextString = toolManager.getCurrentPlacement().getContext();
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		context.put("doEndHelper", BUTTON + "doEndHelper");
		if (ltiService.isAdmin(getSiteId(state)) &&
			serverConfigurationService.getString(SakaiBLTIUtil.BASICLTI_ENCRYPTION_KEY, null) == null) {
                        context.put("configMessage",rb.getString("error.tool.no.encryption.key"));
		}

		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);

		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		// by default, site maintainer can add system-wide LTI tool
		context.put("allowMaintainerAddSystemTool", new Boolean(serverConfigurationService.getBoolean(ALLOW_MAINTAINER_ADD_SYSTEM_TOOL, true)));
		context.put("getContext", contextString);

		// this is for the system tool panel
		List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0, getSiteId(state));
		context.put("ltiTools", tools);

		// top navigation menu
		Menu menu = new MenuImpl(portlet, data, "LTIAdminTool");
		menu.add(new MenuEntry(rb.getString("tool.in.system"), false, "doNav_tool_system"));
		menu.add(new MenuEntry(rb.getString("tool.in.site"), true, "doNav_tool_site"));
		context.put("menu", menu);

		//reset paging/sorting/searching state
		doReset(data);

		return "lti_tool_system";
	}

	public void doEndHelper(RunData data, Context context)
	{
		// Request a shortcut transfer back to the tool we are helping
		// This working depends on SAK-20898
		SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);

		// In case the above fails...
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		switchPanel(state, "Main");
	}

	public String buildToolViewPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.view"));
			return "lti_error";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel(getSiteId(state));
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key, getSiteId(state));
		if (  tool == null ) return "lti_main";

		// Deal with the differences between LTI 1 and LTI 2
		Long version = foorm.getLongNull(tool.get(LTIService.LTI_VERSION));
		boolean isLTI1 = version == null || version == LTIService.LTI_VERSION_1;
		if ( isLTI1 ) {
			mappingForm = foorm.filterForm(mappingForm, null, ".*:only=lti2.*");
		} else {
			mappingForm = foorm.filterForm(mappingForm, null, ".*:only=lti1.*");
		}

		// Extract the version to make it view only
		String fieldInfo = foorm.getFormField(mappingForm, "version");
		fieldInfo = fieldInfo.replace(":hidden=true","");
		String formStatus = ltiService.formOutput(tool, fieldInfo);
		context.put("formStatus", formStatus);

		tool.put(LTIService.LTI_SECRET,LTIService.SECRET_HIDDEN);
		tool.put(LTIService.LTI_CONSUMERKEY,LTIService.SECRET_HIDDEN);
		String formOutput = ltiService.formOutput(tool, mappingForm);
		context.put("formOutput", formOutput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_view";
	}

	public String buildToolEditPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String stateId = (String) state.getAttribute(STATE_ID);
		state.removeAttribute(STATE_ID);
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel(getSiteId(state));
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) id = stateId;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key, getSiteId(state));
		if (  tool == null ) return "lti_main";

		// Hide the old tool secret unless it is incomplete
		if ( ! LTIService.LTI_SECRET_INCOMPLETE.equals(tool.get(LTIService.LTI_SECRET)) ) {
			tool.put(LTIService.LTI_SECRET,LTIService.SECRET_HIDDEN);
		}

		// Deal with the differences between LTI 1 and LTI 2
		Long version = foorm.getLongNull(tool.get(LTIService.LTI_VERSION));
		boolean isLTI1 = version == null || version == LTIService.LTI_VERSION_1;
		if ( isLTI1 ) {
			mappingForm = foorm.filterForm(mappingForm, null, ".*:only=lti2.*");
		} else {
			mappingForm = foorm.filterForm(mappingForm, null, ".*:only=lti1.*");
		}

		// Extract the version to make it view only
		String fieldInfo = foorm.getFormField(mappingForm, "version");
		fieldInfo = fieldInfo.replace(":hidden=true","");
		String formStatus = ltiService.formOutput(tool, fieldInfo);
		context.put("formStatus", formStatus);

		// If we are not admin, hide url, key, and secret
		if ( ! isLTI1 && ! ltiService.isAdmin(getSiteId(state)) ) {
			mappingForm = foorm.filterForm(mappingForm, null, "^launch:.*|^consumerkey:.*|^secret:.*");
		}
		String formInput = ltiService.formInput(tool, mappingForm);

		context.put("formInput", formInput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	public String buildToolDeletePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolDelete");
		String [] mappingForm = foorm.filterForm(ltiService.getToolModel(getSiteId(state)), "^title:.*|^launch:.*|^id:.*", null);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = new Long(id);

		// Retrieve the tool using a WHERE clause so the counts get computed
		List<Map<String,Object>> tools = ltiService.getTools("lti_tools.id = "+key,null,0,0, getSiteId(state));
		if ( tools == null || tools.size() < 1 ) {
			addAlert(state,rb.getString("error.tool.not.found"));
			return "lti_main";
		}

		Map<String,Object> tool  = tools.get(0);
		String formOutput = ltiService.formOutput(tool, mappingForm);
		context.put("formOutput", formOutput);
		context.put("tool",tool);
		context.put("tool_count", tool.get("lti_content_count"));
		context.put("tool_unique_site_count", tool.get("lti_site_count"));

		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_delete";
	}

	public void doToolDelete(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "ToolSystem");
			return;
		}
		Long key = new Long(id);

		// Delete the tool and all associated content items and site links
		List<String> errors = ltiService.deleteToolAndContents(key, getSiteId(state));
		String errorNote = "";
		for(String errstr: errors ) {
			log.error(errstr);
			errorNote += "<br/>"+errstr;
		}

		if ( errors.size() < 1 ) {
			switchPanel(state, "ToolSystem");
		} else {
			addAlert(state,rb.getString("error.delete.fail")+errorNote);
			switchPanel(state, "ToolSystem");
		}
	}

	public String buildToolInsertPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel(getSiteId(state));

		mappingForm = foorm.filterForm(mappingForm, null, ".*:only=edit.*|.*:only=lti2.*");

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = ltiService.formInput(previousPost, mappingForm);
		context.put("formInput",formInput);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	// Insert or edit
	public void doToolPut(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state,"Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();

		String newSecret = reqProps.getProperty(LTIService.LTI_SECRET);
		if ( LTIService.SECRET_HIDDEN.equals(newSecret) ) {
			reqProps.remove(LTIService.LTI_SECRET);
			newSecret = null;
		}

		if ( newSecret != null ) {
			newSecret = SakaiBLTIUtil.encryptSecret(newSecret.trim());
			reqProps.setProperty(LTIService.LTI_SECRET, newSecret);
		}

		String id = data.getParameters().getString(LTIService.LTI_ID);

		String success = null;
		Object retval = null;
		if ( id == null )
		{
			retval = ltiService.insertTool(reqProps, getSiteId(state));
			success = rb.getString("success.created");
		} else {
			Long key = new Long(id);
			retval = ltiService.updateTool(key, reqProps, getSiteId(state));
			success = rb.getString("success.updated");
		}

		if ( retval instanceof String )
		{
			state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			state.setAttribute(STATE_ID,id);
			return;
		}

		state.setAttribute(STATE_SUCCESS,success);
		switchPanel(state, "ToolSystem");
	}

	/** Deployment related methods ------------------------------ */

	public String buildDeployInsertPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.edit"));
			return "lti_error";
		}
		context.put("doDeployAction", BUTTON + "doDeployPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("reg_state",new Integer(0));
		String [] mappingForm = ltiService.getDeployModel();

		mappingForm = foorm.filterForm(mappingForm, null, ".*:hide=insert.*");

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = ltiService.formInput(previousPost, mappingForm);
		context.put("formInput",formInput);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_deploy_insert";
	}

	public String buildDeployViewPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.view"));
			return "lti_error";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getDeployModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = new Long(id);
		Map<String,Object> deploy = ltiService.getDeployDao(key);
		if (  deploy == null ) return "lti_error";

		// Extract the reg_state to make it view only
		String fieldInfo = foorm.getFormField(mappingForm, "reg_state");
		fieldInfo = fieldInfo.replace(":hidden=true","");
		String formStatus = ltiService.formOutput(deploy, fieldInfo);
		context.put("formStatus", formStatus);

		String formOutput = ltiService.formOutput(deploy, mappingForm);
		context.put("formOutput", formOutput);

		Long reg_state = foorm.getLongNull(deploy.get(LTIService.LTI_REG_STATE));
		context.put("reg_state",reg_state);
		context.put("id",id);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_deploy_view";
	}

	public String buildDeployEditPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String stateId = (String) state.getAttribute(STATE_ID);
		state.removeAttribute(STATE_ID);
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.edit"));
			return "lti_error";
		}
		context.put("doDeployAction", BUTTON + "doDeployPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getDeployModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) id = stateId;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = new Long(id);
		Map<String,Object> deploy = ltiService.getDeployDao(key);
		if (  deploy == null ) return "lti_error";

		// Extract the reg_state to make it view only
		String fieldInfo = foorm.getFormField(mappingForm, "reg_state");
		fieldInfo = fieldInfo.replace(":hidden=true","");
		String formStatus = ltiService.formOutput(deploy, fieldInfo);
		context.put("formStatus", formStatus);
		Long reg_state = foorm.getLongNull(deploy.get(LTIService.LTI_REG_STATE));
		context.put("reg_state",reg_state);

		// Remove reg_state from the editable part of the model
		mappingForm = foorm.filterForm(mappingForm, null, "^reg_state:.*");

		String formInput = ltiService.formInput(deploy, mappingForm);

		context.put("formInput", formInput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_deploy_insert";
	}

	// Insert or edit
	public void doDeployPut(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.edit"));
			switchPanel(state,"Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();

		String id = data.getParameters().getString(LTIService.LTI_ID);

		// If we are inserting, fill in the blanks
		if ( id == null ) {
			String oauth_consumer_key = UUID.randomUUID().toString();
			reqProps.setProperty(LTIService.LTI_REG_KEY, oauth_consumer_key);
			// TODO: We should show off and encrypt the REG_PASSWORD too..
			reqProps.setProperty(LTIService.LTI_REG_PASSWORD, UUID.randomUUID().toString());
			reqProps.setProperty(LTIService.LTI_CONSUMERKEY, oauth_consumer_key);
		}

		String success = null;
		Object retval = null;
		boolean lti2Insert = false;
		if ( id == null )
		{
			retval = ltiService.insertDeployDao(reqProps);
			success = rb.getString("success.created");
			lti2Insert = true;
		} else {
			Long key = new Long(id);
			retval = ltiService.updateDeployDao(key, reqProps);
			success = rb.getString("success.updated");
		}

		if ( retval instanceof String )
		{
			state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			state.setAttribute(STATE_ID,id);
			return;
		}

		state.setAttribute(STATE_SUCCESS,success);
		if ( lti2Insert && retval instanceof Long ) {
			Long insertedKey = (Long) retval;
			switchPanel(state, "DeployRegister&id="+insertedKey);
		} else {
			switchPanel(state, "DeploySystem");
		}

	}

	public String buildDeployRegisterPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.activate"));
			return "lti_error";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));

		String [] mappingForm = foorm.filterForm(ltiService.getDeployModel(), "^title:.*|^reg_state:.*|^reg_launch:.*|^id:.*", null);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = new Long(id);
		Map<String,Object> deploy = ltiService.getDeployDao(key);
		if (  deploy == null ) {
			addAlert(state,rb.getString("error.deploy.not.found"));
			return "lti_error";
		}

		Long reg_state = foorm.getLongNull(deploy.get(LTIService.LTI_REG_STATE));
		String reg_key = (String) deploy.get(LTIService.LTI_REG_KEY);
		String reg_password = (String) deploy.get(LTIService.LTI_REG_PASSWORD);
		String consumerkey = (String) deploy.get(LTIService.LTI_CONSUMERKEY);
		String secret = (String) deploy.get(LTIService.LTI_SECRET);

		if ( reg_state == 0 && reg_key != null && reg_password != null && consumerkey != null) {
			// Good news ...
		} else if ( (reg_state == 1 || reg_state == 2 ) && secret != null && consumerkey != null) {
			// Good news ...
		} else {
			addAlert(state,rb.getString("error.register.not.ready"));
			return "lti_error";
		}

		// Extract the reg_state to make it view only
		String fieldInfo = foorm.getFormField(mappingForm, "reg_state");
		fieldInfo = fieldInfo.replace(":hidden=true","");
		String formStatus = ltiService.formOutput(deploy, fieldInfo);
		context.put("formStatus", formStatus);

		String formOutput = ltiService.formOutput(deploy, mappingForm);
		context.put("formOutput", formOutput);
		Placement placement = toolManager.getCurrentPlacement();
		String registerURL = "/access/basiclti/site/~admin/deploy:" + key + "?placement=" + placement.getId();

		context.put("registerURL",registerURL);
		context.put("isInlineRequest",new Boolean(ToolUtils.isInlineRequest(data.getRequest())));
		context.put("id",key);

		state.removeAttribute(STATE_SUCCESS);
		return "lti_deploy_register";
	}

	public String buildPostRegisterPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		return "lti_deploy_post_register";
	}

	public String buildActivatePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{

		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.activate"));
			return "lti_error";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getDeployModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_error";
		}
		Long key = null;
		try {
			key = new Long(id);
		} catch(Exception e) {
			return "Non-numeric id value "+id;
		}
		Map<String,Object> deploy = ltiService.getDeployDao(key);
		if ( deploy == null ) return "lti_error";

		String profileText = (String) deploy.get(LTIService.LTI_REG_PROFILE);
		if ( profileText == null || profileText.length() < 1 ) {
			addAlert(state,rb.getString("error.activate.not.ready"));
			return "lti_error";
		}

		// Load and check the tools from the profile
		List<Map<String,Object>> theTools = new ArrayList<Map<String,Object>> ();
		Properties info = new Properties();
		String retval = prepareValidate(deploy, theTools, info, state);
		if ( retval != null ) return retval;

		context.put("info", info);
		context.put("deploy", deploy);
		context.put("tools", theTools);
		context.put("profile", profileText);

		context.put("doAction", BUTTON + "doActivate");

		return "lti_deploy_activate";
	}

	public void doActivate(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.activate"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "DeploySystem");
			return;
		}

		Long key = new Long(id);
		Map<String,Object> deploy = ltiService.getDeployDao(key);
		if (  deploy == null ) {
			addAlert(state,rb.getString("error.deploy.not.found"));
			switchPanel(state, "DeploySystem");
			return;
		}

		// If we are re-registering, use the new secret for tools that we activate
		Long reg_state = foorm.getLong(deploy.get(LTIService.LTI_REG_STATE));
		String old_secret = (String) deploy.get(LTIService.LTI_SECRET);
		String ack = (String) deploy.get(LTIService.LTI_REG_ACK);

		// We will notify first and then update all the tools later in case the notification fails
		boolean notified = true;
		if ( ack != null && ack.length() > 0 ) {
		    notified = false;
		    log.info("Sending Re-Registration notification to {}", ack);
		    String oauth_consumer_key = (String) deploy.get(LTIService.LTI_CONSUMERKEY);
		    String oauth_secret = old_secret;
		    oauth_secret = SakaiBLTIUtil.decryptSecret(oauth_secret);
		    log.debug("key={} secret={}", oauth_consumer_key, oauth_secret);

		    HttpURLConnection connection = BasicLTIUtil.sendOAuthURL("PUT", ack, oauth_consumer_key, oauth_secret);
		    int responseCode = BasicLTIUtil.getResponseCode(connection);
		    log.info("Re-Registration notification response code {}", responseCode);
		    String return_data = BasicLTIUtil.readHttpResponse(connection);
		    log.info("Re-Registration notification response data {}", return_data);
		    if ( responseCode == HttpURLConnection.HTTP_OK ) {
			notified = true;
		    } else {
			state.setAttribute(STATE_POST,reqProps);
			String oops = "Error return from acknowledgement code="+responseCode+
			    "\nData: "+data;
			addAlert(state, oops);
			switchPanel(state, "DeploySystem");
			return;
		    }
		}

		// We have sent the ACK if needed, update our data structures.
		String new_secret = (String) deploy.get(LTIService.LTI_NEW_SECRET);
		if ( new_secret != null && new_secret.length() < 1 ) new_secret = null;
		if ( new_secret != null ) {
			deploy.put(LTIService.LTI_SECRET,new_secret);
		}

		List<Map<String,Object>> theTools = new ArrayList<Map<String,Object>> ();
		Properties info = new Properties();

		String prepare = prepareValidate(deploy, theTools, info, state);

		log.info("Starting activation process for id={} title={}", key, info.get("title"));

		String failures = "";
		// Update reg_state to indicate we are activated...
		Map<String, Object> deployUpdate = new HashMap<String, Object> ();
		deployUpdate.put(LTIService.LTI_REG_STATE, "2");
		deployUpdate.put(LTIService.LTI_REG_ACK, "");
		if ( new_secret != null ) {
			deployUpdate.put(LTIService.LTI_SECRET, new_secret);
			deployUpdate.put(LTIService.LTI_NEW_SECRET, "");
		}

		// Almost a transaction - at this point update is unlikely to fail
		Object obj = ltiService.updateDeployDao(key, deployUpdate);
		boolean updated = ( obj instanceof Boolean ) && ( (Boolean) obj == Boolean.TRUE);

		if ( ! updated ) {
			String oops = "Unable to update deployment key="+key;
			log.error(oops);
			failures += "\n" + oops;
		}

		// Update the tools...
		int inserts = 0;
		int updates = 0;
		for ( Map<String, Object> theTool : theTools ) {
			Object retval = null;
			Long toolId = foorm.getLongNull(theTool.get(LTIService.LTI_ID));
			theTool.put(LTIService.LTI_VERSION, LTIService.LTI_VERSION_2);
			if ( toolId == null ) {
				retval = ltiService.insertTool(theTool, getSiteId(state));
				if ( retval instanceof String ) {
					String oops = "Unable to insert "+theTool.get(LTIService.LTI_RESOURCE_HANDLER)+" "+retval;
					log.error(oops);
					failures += "\n" + oops;
				} else {
					log.info("Inserted tool={} {}", retval, theTool.get(LTIService.LTI_RESOURCE_HANDLER));
					inserts++;
				}
			} else {
				retval = ltiService.updateTool(toolId, theTool, getSiteId(state));
				if ( retval instanceof String ) {
					String oops = "Unable to update "+theTool.get(LTIService.LTI_RESOURCE_HANDLER)+" "+retval;
					log.error(oops);
					failures += "\n" + oops;
				} else {
					log.info("Updated tool={} {}", toolId, theTool.get(LTIService.LTI_RESOURCE_HANDLER));
					updates++;
				}
			}
		}

		// We can have a combination of successes and failures...
		String success = "";
		if ( inserts > 0 ) success = inserts + " tools inserted ";
		if ( updates > 0 ) success = updates + " tools updated ";
		if ( success.length() > 0 ) state.setAttribute(STATE_SUCCESS,success);

		if ( failures.length() > 0 )
		{
			state.setAttribute(STATE_POST,reqProps);
			addAlert(state, failures);
		}

		switchPanel(state, "DeploySystem");
	}

	public String prepareValidate(Map<String,Object> deploy, List<Map<String,Object>> theTools,
		Properties info, SessionState state)
	{
		Long reg_state = foorm.getLongNull(deploy.get(LTIService.LTI_REG_STATE));
		String profileText = (String) deploy.get(LTIService.LTI_REG_PROFILE);
		if ( profileText == null || profileText.length() < 1 ) {
			addAlert(state,rb.getString("error.activate.not.ready"));
			return "lti_error";
		}

		ToolProxy toolProxy = null;
		try {
			toolProxy = new ToolProxy(profileText);
			log.debug("OBJ:{}", toolProxy);
		} catch (Throwable t ) {
			log.error(t.getMessage(), t);
			log.error("error parsing tool profile {}", profileText);
			addAlert(state,rb.getString("deploy.parse.error"));
			return "lti_error";
		}

		List<Properties> profileTools = new ArrayList<Properties> ();
		try {
			String retval = toolProxy.parseToolProfile(profileTools, info);
			if ( retval != null ) {
				addAlert(state,rb.getString("deploy.parse.error")+" "+retval);
				return "lti_error";
			}
		}
		catch (Exception e ) {
			addAlert(state,rb.getString("deploy.parse.exception")+" "+e.getLocalizedMessage());
			log.error(e.getMessage(), e);
			return "lti_error";
		}

		String instance_guid = (String) info.get("instance_guid");

		if ( profileTools.size() < 1 ) {
			addAlert(state,rb.getString("deploy.activate.notools"));
			return "lti_error";
		}

		// Check them all first
		for ( Properties profileTool : profileTools ) {
			String launch = (String) profileTool.get(LTIService.LTI_LAUNCH);
			if ( ! FormattedText.validateURL(launch) ) {
				addAlert(state,rb.getString("deploy.activate.badlaunch")+" "+launch);
				return "lti_error";
			}
		}

		// Make a copy of the deploy object and clean it up
		Map<String, Object> localDeploy = new HashMap<String, Object> ();
		localDeploy.putAll(deploy);
		localDeploy.remove(LTIService.LTI_ID);
		localDeploy.remove(LTIService.LTI_CREATED_AT);
		localDeploy.remove(LTIService.LTI_UPDATED_AT);
		localDeploy.remove(LTIService.LTI_REG_PROFILE);

		// Loop through all of the tools
		for ( Properties profileTool : profileTools ) {
			String resource_type_code = (String) profileTool.get("resource_type_code");
			String resource_handler = instance_guid;
			if ( ! resource_handler.endsWith("/") && ! resource_handler.startsWith("/") ) resource_handler = resource_handler + "/" ;
			resource_handler = resource_handler + resource_type_code;
			Map<String,Object> tool = ltiService.getToolForResourceHandlerDao(resource_handler);

			// Construct a new tool object
			Map<String, Object> newTool = new HashMap<String, Object> ();
			if ( tool != null ) {
				newTool.putAll(tool);
				newTool.putAll(localDeploy); // New settings from the deployment
			} else {
				newTool.putAll(localDeploy);
			}

			newTool.put(LTIService.LTI_RESOURCE_HANDLER, resource_handler);
			newTool.put(LTIService.LTI_DEPLOYMENT_ID, deploy.get(LTIService.LTI_ID));

			// Copy explicitly in case the parser changes slightly
			if ( profileTool.get(LTIService.LTI_LAUNCH) != null ) newTool.put(LTIService.LTI_LAUNCH, profileTool.get(LTIService.LTI_LAUNCH));
			if ( profileTool.get(LTIService.LTI_TITLE) != null ) newTool.put(LTIService.LTI_TITLE, profileTool.get(LTIService.LTI_TITLE));
			if ( profileTool.get(LTIService.LTI_TITLE) != null ) newTool.put(LTIService.LTI_PAGETITLE, profileTool.get(LTIService.LTI_TITLE)); // Duplicate by default
			if ( profileTool.get("button") != null ) newTool.put(LTIService.LTI_PAGETITLE, profileTool.get("button")); // Note different fields
			if ( profileTool.get(LTIService.LTI_DESCRIPTION) != null ) newTool.put(LTIService.LTI_DESCRIPTION, profileTool.get(LTI2Constants.DESCRIPTION));
			if ( profileTool.get(LTIService.LTI_PARAMETER) != null ) newTool.put(LTIService.LTI_PARAMETER, profileTool.get(LTI2Constants.PARAMETER));

			// Turn on all the UI allow bits so as to allow overriding
			newTool.put(LTIService.LTI_ALLOWTITLE, new Integer(1));
			newTool.put(LTIService.LTI_ALLOWPAGETITLE, new Integer(1));
			// Might only want to do this if we know they do LtiLinkItem Content Item
			newTool.put(LTIService.LTI_ALLOWLAUNCH, new Integer(1));

			String tool_proxy_binding = (String) profileTool.get(LTI2Constants.TOOL_PROXY_BINDING);
			ToolProxyBinding toolProxyBinding = null;
			try {
				toolProxyBinding = new ToolProxyBinding(tool_proxy_binding);
			} catch (Throwable t) {
				addAlert(state,rb.getString("deploy.parse.error")+" tool_proxy_binding");
				return "lti_error";
			}

			JSONObject launchMessage = toolProxyBinding.getMessageOfType(LTI2Messages.BASIC_LTI_LAUNCH_REQUEST);
			JSONObject selectMessage = toolProxyBinding.getMessageOfType(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST);

                        if ( launchMessage != null ) {
                                newTool.put(LTIService.LTI_PL_LAUNCH, new Integer(1));
                        }

			// Look for capabilities for Sakai
			boolean sakaiplacements = false;
                        if ( toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
                                SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTANY ) ) {
                                newTool.put(LTIService.LTI_PL_LINKSELECTION, new Integer(1));
				newTool.put(LTIService.LTI_PL_FILEITEM, new Integer(1));
				newTool.put(LTIService.LTI_PL_IMPORTITEM, new Integer(1));
                                sakaiplacements = true;
                        }

                        if ( toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
                                SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTFILE ) ) {
				newTool.put(LTIService.LTI_PL_FILEITEM, new Integer(1));
                                sakaiplacements = true;
                        }

                        if ( toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
                                SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTLINK ) ) {
                                newTool.put(LTIService.LTI_PL_LINKSELECTION, new Integer(1));
                                sakaiplacements = true;
                        }

                        if ( toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
                                SakaiBLTIUtil.SAKAI_CONTENTITEM_SELECTIMPORT ) ) {
                                newTool.put(LTIService.LTI_PL_IMPORTITEM, new Integer(1));
                                sakaiplacements = true;
                        }

			// If we did not see any Sakai commentary about placements, look to the Canvas variants
			boolean selection = sakaiplacements;
			if ( ! sakaiplacements && toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
				SakaiBLTIUtil.CANVAS_PLACEMENTS_LINKSELECTION ) ) {
				newTool.put(LTIService.LTI_PL_LINKSELECTION, new Integer(1));
				selection = true;
			}

			if ( ! sakaiplacements && toolProxyBinding.enabledCapability(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST,
				SakaiBLTIUtil.CANVAS_PLACEMENTS_CONTENTIMPORT ) ) {
				newTool.put(LTIService.LTI_PL_FILEITEM, new Integer(1));
				selection = true;
			}

			// When in doubt, assume LINKSELECTION
			if ( !selection && toolProxyBinding.getMessageOfType(LTI2Messages.CONTENT_ITEM_SELECTION_REQUEST) != null ) {
				newTool.put(LTIService.LTI_PL_LINKSELECTION, new Integer(1));
			}

			newTool.put(LTIService.LTI_TOOL_PROXY_BINDING, tool_proxy_binding);
			String fa_icon = toolProxyBinding.getIconPath("FontAwesome");
			if ( fa_icon != null ) newTool.put("fa_icon", fa_icon);

			log.info("newTool={}", newTool);
			theTools.add(newTool);
		}
		return null; // Success
	}

	public String buildDeploySystemPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.admin.view"));
			return "lti_error";
		}
		String contextString = toolManager.getCurrentPlacement().getContext();
		context.put("ltiService", ltiService);
		state.removeAttribute(STATE_POST);

		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("getContext", contextString);

		state.removeAttribute(STATE_SUCCESS);

		List<Map<String,Object>> deploys = ltiService.getDeploysDao(null,null,0,5000);
		context.put("deploys", deploys);

		// Check if we are configured
		LTI2Config cnf = new SakaiLTI2Config();
		if ( cnf.getGuid() == null ) {
			context.put("configMessage",rb.getString("error.deploy.not.config"));
			log.error("*********************************************");
			log.error("* LTI2 NOT CONFIGURED - Using Sample Data   *");
			log.error("* Do not use this in production.  Test only *");
			log.error("*********************************************");
		}

		return "lti_deploy_system";
	}

	public String buildDeployDeletePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isAdmin(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doDeployDelete");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_deploy_system";
		}
		Long key = new Long(id);

		// Retrieve the tool using a WHERE clause so the counts get computed
		List<Map<String,Object>> deploys = ltiService.getDeploysDao("lti_deploy.id = "+key,null,0,0);
		if ( deploys == null || deploys.size() < 1 ) {
			addAlert(state,rb.getString("error.deploy.not.found"));
			return "lti_deploy_system";
		}

		// Retrieve all the tools that are related to this deploy
		List<Map<String,Object>> tools = ltiService.getTools("deployment_id = "+key,null,0,0, getSiteId(state));
		context.put("tools", tools);

		Map<String,Object> deploy  = deploys.get(0);
		context.put("deploy",deploy);

		String [] mappingForm = foorm.filterForm(ltiService.getDeployModel(), "^title:.*|^reg_launch:.*|^id:.*", null);
		String formOutput = ltiService.formOutput(deploy, mappingForm);
		context.put("formOutput", formOutput);

		String deployData = rb.getString("deploy.data");
		deployData = deployData.replace(":tools",""+deploy.get("lti_tool_count"))
			.replace(":contents",""+deploy.get("lti_content_count"))
			.replace(":sites",""+deploy.get("lti_site_count"));

		context.put("deployData", deployData);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_deploy_delete";
	}

	public void doDeployDelete(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "Deploy");
			return;
		}
		Long key = new Long(id);

		// Retrieve all the tools that are related to this deploy
		List<Map<String,Object>> tools = ltiService.getTools("deployment_id = "+key,null,0,0, getSiteId(state));
		String errmsg = "";
		for ( Map<String,Object> tool : tools ) {
			Long tool_id = foorm.getLongNull(tool.get("id"));
			String parm = "delete_"+tool_id;
			String val = data.getParameters().getString(parm);
			if ( val == null || ! "on".equals(val) ) continue;
			// Delete the tool and all associated content items and site links
			List<String> errors = ltiService.deleteToolAndContents(tool_id, getSiteId(state));
			for(String errstr: errors ) {
				log.error(errstr);
				errmsg += "<br/>"+errstr;
			}
		}

		if ( errmsg.length() > 0 ) {
			addAlert(state,rb.getString("error.delete.fail")+errmsg);
			switchPanel(state, "DeploySystem");
			return;
		}

		// also remove the link
		if ( ltiService.deleteDeployDao(key) )
		{
			state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));
		} else {
			addAlert(state,rb.getString("error.delete.fail"));
		}
		switchPanel(state, "DeploySystem");
	}


	/** Content related methods ------------------------------ */

	public String buildContentPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.view"));
			return "lti_error";
		}
		List<Map<String,Object>> contents = ltiService.getContents(null,null,0,5000, getSiteId(state));
		for ( Map<String,Object> content : contents ) {
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration tool = SiteService.findTool(plstr);
			if ( tool == null ) {
				content.put(LTIService.LTI_PLACEMENT, null);
			}
		}
		context.put("contents", contents);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		context.put("getContext",toolManager.getCurrentPlacement().getContext());
		state.removeAttribute(STATE_SUCCESS);
		return "lti_content";
	}


	public String buildContentPutPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		String contextString = toolManager.getCurrentPlacement().getContext();
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String stateToolId = (String) state.getAttribute(STATE_TOOL_ID);
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		context.put("doAction", BUTTON + "doContentPut");
		state.removeAttribute(STATE_SUCCESS);

		List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0, getSiteId(state));
		// only list the tools available in the system
		List<Map<String,Object>> systemTools = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> tool:tools)
		{
			String siteId = !tool.containsKey(ltiService.LTI_SITE_ID)?null:StringUtils.trimToNull((String) tool.get(ltiService.LTI_SITE_ID));
			if (siteId == null)
			{
				// add tool for whole system
				systemTools.add(tool);
			}
			else if (siteId.equals(contextString))
			{
				// add the tool for current site only
				systemTools.add(tool);
			}
			else if (ltiService.isAdmin(getSiteId(state)))
			{
				// if in Admin's my workspace, show all tools
				systemTools.add(tool);
			}
		}
		context.put("tools", systemTools);

		Object previousData = null;

		String toolId = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		if ( toolId == null ) toolId = stateToolId;
		// output the tool id value to context
		context.put("tool_id", toolId);

		Long key = null;
		if ( toolId != null ) {
			try {
				key = new Long(toolId);
			}
			catch (NumberFormatException e) {
				//Reset toolId and key
				key=null;
				toolId=null;
			}
		}
		Map<String,Object> tool = null;
		if ( key != null ) {
			tool = ltiService.getTool(key, getSiteId(state));
			if ( tool == null ) {
				addAlert(state, rb.getString("error.tool.not.found"));
				return "lti_content_insert";
			}
		}

		String contentId = data.getParameters().getString(LTIService.LTI_ID);
		if ( contentId == null ) contentId = (String) state.getAttribute(STATE_CONTENT_ID);

		if ( contentId == null ) {  // Insert
			if ( toolId == null ) {
				return "lti_content_insert";
			}
			previousData = (Properties) state.getAttribute(STATE_POST);

			// Edit
		} else {
			Long contentKey = new Long(contentId);
			Map<String,Object> content = ltiService.getContent(contentKey, getSiteId(state));
			if ( content == null ) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_content";
			}

			if ( key == null ) {
				key = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));
				if ( key != null ) tool = ltiService.getTool(key, getSiteId(state));
			}
			previousData = content;

			// whether the content has a site link created already?
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration siteLinkTool = SiteService.findTool(plstr);
			if ( siteLinkTool != null ) {
				context.put(LTIService.LTI_PLACEMENT, plstr);
			}
		}

		// We will handle the tool_id field ourselves in the Velocity code
		String [] contentForm = foorm.filterForm(null,ltiService.getContentModel(key, getSiteId(state)), null, "^tool_id:.*");
        if ( contentForm == null || key == null ) {
                addAlert(state,rb.getString("error.tool.not.found"));
                return "lti_error";
        }

        if (previousData == null) {
        	Properties defaultData = new Properties();
        	defaultData.put("title",tool.get(LTIService.LTI_TITLE));
        	defaultData.put("pagetitle",tool.get(LTIService.LTI_PAGETITLE));
		String fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
		if ( fa_icon != null && fa_icon.length() > 0 ) {
			defaultData.put("fa_icon",tool.get(LTIService.LTI_FA_ICON));
		}
        	previousData = defaultData;

        }

        String formInput = ltiService.formInput(previousData, contentForm);

        context.put("formInput",formInput);
		context.put(LTIService.LTI_TOOL_ID,key);
		if ( tool != null ) {
			context.put("tool_description", tool.get(LTIService.LTI_DESCRIPTION));
			Long visible = foorm.getLong(tool.get(LTIService.LTI_VISIBLE));
			context.put("tool_visible", visible);
		}

		return "lti_content_insert";
	}

	// This has three use cases: (1) This Tool, (2) Lessons, and (3) site-manage
	// Insert or edit depending on whether an id is present or not
	public void doContentPut(RunData data, Context context)
	{
		Properties reqProps = data.getParameters().getProperties();
		doContentPutInternal(data, context, reqProps);
	}

	private void doContentPutInternal(RunData data, Context context, Properties reqProps)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		state.removeAttribute(STATE_POST);

		String id = reqProps.getProperty(LTIService.LTI_ID);
		String toolId = reqProps.getProperty(LTIService.LTI_TOOL_ID);

		// Does an insert when id is null and update when is is not null
		Object retval = ltiService.insertToolContent(id, toolId, reqProps, getSiteId(state));

		Long contentKey = null;
		Map<String,Object> content = null;
		if ( retval instanceof String )
		{
			addAlert(state, (String) retval);
			switchPanel(state, "Error");
			state.setAttribute(STATE_POST,reqProps);
			state.setAttribute(STATE_CONTENT_ID,id);
			return;
		}
		else if ( retval instanceof Boolean )
		{
			//If it's true retrieve the previous content?
			if ((Boolean) retval == true) {
				content = ltiService.getContent(Long.parseLong(id), getSiteId(state));
				if ( content == null ) {
					addAlert(state, rb.getString("error.content.not.found"));
					switchPanel(state, "Error");
					state.setAttribute(STATE_POST,reqProps);
					state.setAttribute(STATE_CONTENT_ID,id);
					return;
				}
			} else {
				// TODO: returns false, should it do anyhing else?
				log.error("insertToolContent returned false for {}", id);
			}
		}
		else
		{
			// the return value is the content key Long value
			id = ((Long) retval).toString();
			contentKey = new Long(id);
			content = ltiService.getContent(contentKey, getSiteId(state));
			if ( content == null ) {
				addAlert(state, rb.getString("error.content.not.found"));
				switchPanel(state, "Error");
				state.setAttribute(STATE_POST,reqProps);
				state.setAttribute(STATE_CONTENT_ID,id);
				return;
			}
		}

		String returnUrl = reqProps.getProperty("returnUrl");
		if ( returnUrl != null )
		{
			if ( id != null ) {
				if ( returnUrl.startsWith("about:blank") ) { // Redirect to the item
					if ( content != null ) {
						String launch = (String) ltiService.getContentLaunch(content);
						if ( launch != null ) returnUrl = launch;
					}
					switchPanel(state, "Forward");
				} else {
					if ( returnUrl.indexOf("?") > 0 ) {
						returnUrl += "&ltiItemId=/blti/" + retval;
					} else {
						returnUrl += "?ltiItemId=/blti/" + retval;
					}
					if ( returnUrl.indexOf("panel=CKEditorPostConfig") > 0 ) {
						switchPanel(state, "Forward");
					} else {
						switchPanel(state, "Redirect");
					}
				}
			}
			state.setAttribute(STATE_REDIRECT_URL,returnUrl);
			return;
		}

		String success = null;
		if ( id == null )
		{
			success = rb.getString("success.created");
		} else {
			success = rb.getString("success.updated");
		}
		state.setAttribute(STATE_SUCCESS,success);

		String title = reqProps.getProperty(LTIService.LTI_PAGETITLE);

		// Take the title from the content (or tool) definition
		if (title == null || title.trim().length() < 1 ) {
			if ( content != null ) {
				title = (String) content.get(ltiService.LTI_PAGETITLE);
			}
		}

		if (reqProps.getProperty("add_site_link") != null)
		{
			// this is to add site link:
			retval = ltiService.insertToolSiteLink(id, title, getSiteId(state));
			if ( retval instanceof String ) {
				String prefix = ((String) retval).substring(0,1);
				addAlert(state, ((String) retval).substring(1));
				if ("0".equals(prefix))
				{
					if ( ToolUtils.isInlineRequest(data.getRequest()) ) {
						switchPanel(state, "ToolSite");
					} else {
						switchPanel(state, "Refresh");
					}
				}
				else if ("1".equals(prefix))
				{
					switchPanel(state, "Error");
				}
				return;
			}
			else if ( retval instanceof Boolean ) {
				if (((Boolean) retval).booleanValue())
				{
					if ( ToolUtils.isInlineRequest(data.getRequest()) ) {
						switchPanel(state, "ToolSite");
					} else {
						switchPanel(state, "Refresh");
					}
				}
				else
				{
					switchPanel(state, "Error");
				}
				return;
			}

			state.setAttribute(STATE_SUCCESS,rb.getString("success.link.add"));
		}

		switchPanel(state, "ToolSite");
	}

	public void doContentItemPut(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// Check for a returned error message from LTI
		String lti_errormsg = data.getParameters().getString("lti_errormsg");
		if ( lti_errormsg != null && lti_errormsg.trim().length() > 0 ) {
			addAlert(state,lti_errormsg);
			switchPanel(state, "Error");
			return;
		}

		// Check for a returned "note" from LTI
		String lti_msg = data.getParameters().getString("lti_msg");
		if ( lti_msg != null ) state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));


		// Sanity check our returnUrl
		String returnUrl = data.getParameters().getString("returnUrl");
		if ( returnUrl == null ) {
			addAlert(state,rb.getString("error.contentitem.missing.returnurl"));
			switchPanel(state, "Error");
                        return;
		}

		// Retrieve the tool associated with the content item
		Long toolKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if ( toolKey == 0 || toolKey < 0 ) {
			addAlert(state,rb.getString("error.contentitem.missing"));
			switchPanel(state, "Error");
                        return;
		}
		Map<String, Object> tool = ltiService.getTool(toolKey, getSiteId(state));
		if ( tool == null ) {
			addAlert(state,rb.getString("error.contentitem.missing"));
			switchPanel(state, "Error");
                        return;
		}

		// Parse and validate the incoming ContentItem
		ContentItem contentItem = null;
		try {
			contentItem = SakaiBLTIUtil.getContentItemFromRequest(tool);
		} catch(Exception e) {
			addAlert(state,rb.getString("error.contentitem.bad")+" ("+e.getMessage()+")");
			switchPanel(state, "Error");
                        return;
		}

		// Example of how to pull back the data Properties we passed in above
		// Properties dataProps = contentItem.getDataProperties();
		// log.debug("dataProps={}", dataProps);
		// dataProps={remember=always bring a towel}

		// Extract the content item data
		JSONObject item = contentItem.getItemOfType(ContentItem.TYPE_LTILINKITEM);
		if ( item == null ) {
			// Compliance with earlier draft
			item = contentItem.getItemOfType(ContentItem.TYPE_LTILINK_OLD);
		}
		if ( item == null ) {
			addAlert(state,rb.getString("error.contentitem.no.ltilink"));
			switchPanel(state, "Error");
                        return;
		}

		// Prepare data for the next phase
		state.removeAttribute(STATE_POST);
		Properties reqProps = extractLTIContentItem(item, tool, toolKey);
		reqProps.setProperty(LTIService.LTI_CONTENTITEM, contentItem.toString());
		reqProps.setProperty("returnUrl", returnUrl);

		String title = reqProps.getProperty(LTIService.LTI_TITLE);
		String url = reqProps.getProperty("launch");

		// If we are not complete, we forward back to the configuration screen
		boolean complete = title != null && url != null;
		if ( ! complete ) {

			log.debug("Forwarding to ContentConfig toolKey={}", toolKey);
			state.setAttribute(STATE_POST,reqProps);
			switchPanel(state, "ContentConfig");
			return;
		}

		// Time to store our content item and redirect back to our helpee
		log.debug("Content Item complete toolKey={}", toolKey);
		doContentPutInternal(data, context, reqProps);

		String sakaiSession = data.getParameters().getString(RequestFilter.ATTR_SESSION);
		if ( sakaiSession != null ) {
			switchPanel(state, "Redirect&"+RequestFilter.ATTR_SESSION+"="+sakaiSession);
		}

	}

	public void doContentItemEditorHandle(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// Check for a returned error message from LTI
		String lti_errormsg = data.getParameters().getString("lti_errormsg");
		if ( lti_errormsg != null && lti_errormsg.trim().length() > 0 ) {
			addAlert(state,lti_errormsg);
			switchPanel(state, "Error");
			return;
		}

		// Check for a returned "note" from LTI
		String lti_msg = data.getParameters().getString("lti_msg");
		if ( lti_msg != null ) state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));


		// Retrieve the tool associated with the content item
		Long toolKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if ( toolKey == 0 || toolKey < 0 ) {
			addAlert(state,rb.getString("error.contentitem.missing"));
			switchPanel(state, "Error");
                        return;
		}
		Map<String, Object> tool = ltiService.getTool(toolKey, getSiteId(state));
		if ( tool == null ) {
			addAlert(state,rb.getString("error.contentitem.missing"));
			switchPanel(state, "Error");
                        return;
		}

		// Parse and validate the incoming ContentItem
		ContentItem contentItem = null;
		try {
			contentItem = SakaiBLTIUtil.getContentItemFromRequest(tool);
		} catch(Exception e) {
			addAlert(state,rb.getString("error.contentitem.bad")+" ("+e.getMessage()+")");
			switchPanel(state, "Error");
                        return;
		}

		// Loop through the array of returned items
		// LTI LinkItems need to be inserted
		JSONArray graph = contentItem.getGraph();
		JSONArray new_content = new JSONArray();
		int goodcount = 0;
		List<String> failures = new ArrayList<String> ();
                for ( Object i : graph ) {
                        if ( ! (i instanceof JSONObject) ) continue;
                        JSONObject item = (JSONObject) i;
                        String type = getString(item,LTI2Constants.TYPE);
                        if ( ! ContentItem.TYPE_LTILINKITEM.equals(type) ) {
				goodcount++;
				new_content.add(item);
				continue;
			}

			// We need to establish resource link ids for the LTILinkItems
			Properties reqProps = extractLTIContentItem(item, tool, toolKey);

			String title = reqProps.getProperty(LTIService.LTI_TITLE);
			if ( title == null ) {
				reqProps.setProperty(LTIService.LTI_TITLE, rb.getString("contentitem.generic.title"));
			}

	                String url = reqProps.getProperty("launch");
			if ( url == null ) {
				log.error("LTILink Item missing launch url {}", toolKey);
				log.debug("{}", item);
                                failures.add(rb.getString("error.contentitem.missing.url"));
				continue;
			}

			// Time to store our content item
			log.debug("Inserting LTILinkItem toolKey={}", toolKey);

			// Does an insert when id is null and update when is is not null
			Object retval = ltiService.insertContent(reqProps, getSiteId(state));
                        if ( retval instanceof String ) {
                                log.error("Unable to insert LTILinkItem tool={}", toolKey);
				log.debug("{}", item);
                                failures.add(rb.getString("error.contentitem.content.insert"));
				continue;
			}
			Long contentKey = (Long) retval;
			String contentUrl = null;
			Map<String,Object> content = ltiService.getContent(contentKey, getSiteId(state));
			if ( content != null ) {
				contentUrl = ltiService.getContentLaunch(content);
				if ( contentUrl != null && contentUrl.startsWith("/") ) contentUrl = SakaiBLTIUtil.getOurServerUrl() + contentUrl;
			}
                        if ( contentUrl == null ) {
                                log.error("Unable to get launch url from contentitem content={}", contentKey);
				log.debug("{}", item);
                                failures.add(rb.getString("error.contentitem.content.launch"));
				continue;
			}
			item.put("launch", contentUrl);
			new_content.add(item);
			goodcount++;
		}
                log.debug("Forwarding to EditorDone");
                state.setAttribute(STATE_CONTENT_ITEM,new_content);
                state.setAttribute(STATE_CONTENT_ITEM_FAILURES,failures);
                state.setAttribute(STATE_CONTENT_ITEM_SUCCESSES,new Integer(goodcount));
                switchPanel(state, "EditorDone");
	}

	public Properties extractLTIContentItem(JSONObject item, Map<String, Object> tool, Long toolKey)
	{

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

		String title = getString(item,ContentItem.TITLE);
		String text = getString(item,ContentItem.TEXT);
		String url = getString(item,ContentItem.URL);
		// If the URL is empty, assume it is the same as the launch URL
		if ( url == null ) {
			url = (String) tool.get(LTIService.LTI_LAUNCH);
		}

		JSONObject lineItem = getObject(item,ContentItem.LINEITEM);
		JSONObject custom = getObject(item,ContentItem.CUSTOM);
		String custom_str = "";
		if ( custom != null ) {
			Iterator<String> i = custom.keySet().iterator();
                        while ( i.hasNext() ) {
                                String key = (String) i.next();
				String value = (String) custom.get(key);
				custom_str += key + "=" + value + "\n";
			}
		}

		// Much prefer this be an icon style like LTI 2.0
		JSONObject iconObject = getObject(item, ContentItem.ICON);
		String icon = getString(iconObject, "fa_icon");
		if ( icon == null ) {
			icon = getString(iconObject, LTI2Constants.JSONLD_ID);
			if ( icon != null ) {
				if ( ! icon.startsWith("fa-") ) icon = null;
			}
		}

		// Prepare data for the next phase
		Properties reqProps = new Properties();
		reqProps.setProperty("tool_id", toolKey+"");
		if ( url != null ) reqProps.setProperty("launch", url);
		if ( title == null ) title = text;
		if ( text == null ) text = title;
		if ( title != null ) reqProps.setProperty(LTIService.LTI_TITLE, title);
		if ( title != null ) reqProps.setProperty(LTIService.LTI_PAGETITLE, title);
		if ( text != null ) reqProps.setProperty(LTIService.LTI_DESCRIPTION, text);
		if ( icon != null ) reqProps.setProperty(LTIService.LTI_FA_ICON, icon);
		if ( custom_str.length() > 0 ) reqProps.setProperty(LTIService.LTI_CUSTOM, custom_str);

		return reqProps;
	}

	public String buildEditorDonePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
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
		if ( failures.size() > 0 ) context.put("failures", failures);
		return "lti_editor_done";
	}

	public String buildRedirectPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if ( returnUrl == null ) return "lti_content_redirect";
		log.debug("Redirecting parent frame back to={}", returnUrl);
		if ( ! returnUrl.startsWith("about:blank") ) context.put("returnUrl",returnUrl);
		return "lti_content_redirect";
	}

	public String buildForwardPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if ( returnUrl == null ) return "lti_content_redirect";
		log.debug("Forwarding frame to={}", returnUrl);
		context.put("forwardUrl",returnUrl);
		return "lti_content_redirect";
	}

	// Special panel for Lesson Builder
	// Add New: panel=ContentConfig&tool_id=14
	// Edit existing: panel=ContentConfig&id=12
	public String buildContentConfigPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		state.removeAttribute(STATE_POST);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		String returnUrl = data.getParameters().getString("returnUrl");
		if ( returnUrl == null && previousPost != null ) returnUrl = previousPost.getProperty("returnUrl");
		if ( returnUrl == null ) {
			addAlert(state,rb.getString("error.missing.return"));
			return "lti_error";
		}

		Map<String,Object> content = null;
		Map<String,Object> tool = null;

		Long toolKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));

		Long contentKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_ID));
		if ( contentKey == null && previousPost != null ) contentKey = foorm.getLongNull(previousPost.getProperty(LTIService.LTI_ID));
		if ( contentKey != null ) {
			content = ltiService.getContent(contentKey, getSiteId(state));
			if ( content == null ) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_error";
			}
			toolKey = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));
		}
		if ( toolKey == null && previousPost != null ) toolKey = foorm.getLongNull(previousPost.getProperty(LTIService.LTI_TOOL_ID));
		if ( toolKey != null ) tool = ltiService.getTool(toolKey, getSiteId(state));

		// No matter what, we must have a tool
		if ( tool == null ) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		// Create a POSTable URL back to this application with the right parameters
		// Since the external tool will be setting all the POST data we need to
		// include GET data for things that we might normally have sent as "hidden" data

		/** The name of the system property that will be used when setting the value of the session cookie. */
		String suffix = System.getProperty(SAKAI_SERVERID);

		String sessionid = "Missing";
		Session s = SessionManager.getCurrentSession();
		if (s != null) {
			sessionid = s.getId();
		}

		Placement placement = toolManager.getCurrentPlacement();
		// String contentReturn = SakaiBLTIUtil.getOurServerUrl() + "/portal/tool/" + placement.getId() +
                String contentReturn = serverConfigurationService.getToolUrl() + "/" + placement.getId() +
			"/sakai.basiclti.admin.helper.helper" +
			"?eventSubmit_doContentItemPut=Save" +
			"&" + RequestFilter.ATTR_SESSION + "=" +  URLEncoder.encode(sessionid+"."+suffix) +
			"&returnUrl=" + URLEncoder.encode(returnUrl) +
			"&panel=PostContentItem" +
			"&tool_id=" + tool.get(LTIService.LTI_ID);

		// Add CSRF protection so it actually makes it into the "do" code
		contentReturn = SakaiBLTIUtil.addCSRFToken(contentReturn);

		// /acccess/blti/context/tool:12 (does not have a querystring)
		String contentLaunch  = ltiService.getToolLaunch(tool, placement.getContext());

		// Can set ContentItemSelection launch values or put in our own data items
		// which will come back later.  Be mindful of GET length limitations enroute
		// to the access servlet.
		Properties contentData = new Properties();
		contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, ContentItem.MEDIA_LTILINKITEM);
		contentData.setProperty("remember", "always bring a towel");  // An example

		contentLaunch = ContentItem.buildLaunch(contentLaunch , contentReturn, contentData);

		Object previousData = null;
		if ( content != null ) {
			previousData = content;
		} else {
			previousData = previousPost;
			if ( previousData == null ) {
				previousData = new Properties();
			}
			String fa_icon = (String) tool.get(LTIService.LTI_FA_ICON);
			if ( ((Properties) previousData).getProperty("fa_icon") == null && fa_icon != null ) {
				((Properties) previousData).setProperty(LTIService.LTI_FA_ICON, fa_icon);
			}
		}

		// We will handle the tool_id field ourselves in the Velocity code
		String [] contentForm = foorm.filterForm(null,ltiService.getContentModel(toolKey, getSiteId(state)), null, "^tool_id:.*|^SITE_ID:.*");
		if ( contentForm == null ) {
			addAlert(state,rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		// Check if we are supposed to let the tool configure itself
		Long allowLinkSelection = foorm.getLong(tool.get(LTIService.LTI_PL_LINKSELECTION));
		Long allowLaunch = foorm.getLong(tool.get(LTIService.LTI_PL_LAUNCH));

		context.put("isAdmin",new Boolean(ltiService.isAdmin(getSiteId(state))) );
		context.put("doAction", BUTTON + "doContentPut");
		if ( ! returnUrl.startsWith("about:blank") ) context.put("cancelUrl", returnUrl);
		context.put("returnUrl", returnUrl);
		if ( allowLinkSelection > 0 ) context.put("contentLaunch", contentLaunch);
		// If this tool only allows configuration, go straight to Content Item
		if ( allowLinkSelection > 0 && allowLaunch < 1 ) context.put("autoLaunch", contentLaunch);
		context.put(LTIService.LTI_TOOL_ID,toolKey);
		context.put("tool_description", tool.get(LTIService.LTI_DESCRIPTION));
		context.put("tool_title", tool.get(LTIService.LTI_TITLE));
		context.put("tool_launch", tool.get(LTIService.LTI_LAUNCH));


		String key = (String) tool.get(LTIService.LTI_CONSUMERKEY);
		String secret = (String) tool.get(LTIService.LTI_SECRET);
		if ( LTIService.LTI_SECRET_INCOMPLETE.equals(secret) && LTIService.LTI_SECRET_INCOMPLETE.equals(key) ) {
			String keyField = foorm.formInput(null,"consumerkey:text:label=need.tool.key:required=true:maxlength=255", rb);
			context.put("keyField", keyField);
			String secretField = foorm.formInput(null,"secret:text:required=true:label=need.tool.secret:maxlength=255", rb);
			context.put("secretField", secretField);
		}

		String formInput = ltiService.formInput(previousData, contentForm);
		context.put("formInput",formInput);

		return "lti_content_config";
	}

	// Special panel for  FCKEditor
	// Add New: panel=Config&tool_id=14
	// Edit existing: panel=Config&id=12
	public String buildCKEditorPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		state.removeAttribute(STATE_POST);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		Placement placement = toolManager.getCurrentPlacement();

		// Get the lauchable and content editor tools...
		List<Map<String, Object>> toolsLaunch = ltiService.getToolsLaunch(placement.getContext());
		List<Map<String, Object>> toolsCI = ltiService.getToolsContentEditor(placement.getContext());

		// If we have not tools at all, tell the user...
		if ( (toolsLaunch.size() + toolsCI.size()) < 1 ) {
			return "lti_editor_select";
		}

		// If there is only one - pick it
		Map<String,Object> tool = null;
		boolean doContent = false;
		if ( toolsCI.size() == 1 && toolsLaunch.size() == 0 ) {
			doContent = true;
			tool  = toolsCI.get(0);
		}

		// See if the user selected a Content Item tool...
		Long toolKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));
		if ( toolKey != null && tool == null ) {
			for (Map<String,Object> t : toolsCI) {
				Long editKey = foorm.getLongNull(t.get(LTIService.LTI_ID));
				if ( toolKey.equals(editKey) ) {
					doContent = true;
					tool = t;
					break;
				}
			}
		}

		// See if the user selected a regular tool...
		if ( toolKey != null && tool == null ) {
			for (Map<String,Object> t : toolsLaunch) {
				Long editKey = foorm.getLongNull(t.get(LTIService.LTI_ID));
				if ( toolKey.equals(editKey) ) {
					// Leave doContent = false;
					tool = t;
					break;
				}
			}
		}

		// Must have more than one and need to select
		if ( tool == null ) {
			context.put("toolsLaunch", toolsLaunch);
			context.put("toolsCI", toolsCI);
			return "lti_editor_select";
		}


		String sessionid = "Missing";
		Session s = SessionManager.getCurrentSession();
		if (s != null) {
			sessionid = s.getId();
		}
		String suffix = System.getProperty(SAKAI_SERVERID);

		// Add New: panel=Config&tool_id=14
		if ( ! doContent ) {
			String returnUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId() +
				"/sakai.basiclti.admin.helper.helper" +
				"?panel=CKEditorPostConfig" +
				"&" + RequestFilter.ATTR_SESSION + "=" +  URLEncoder.encode(sessionid+"."+suffix);

			String configUrl = serverConfigurationService.getToolUrl() + "/" + placement.getId() +
				"/sakai.basiclti.admin.helper.helper" +
				"?panel=ContentConfig" +
				"&returnUrl=" + URLEncoder.encode(returnUrl) +
				"&tool_id=" + tool.get(LTIService.LTI_ID) +
				"&" + RequestFilter.ATTR_SESSION + "=" +  URLEncoder.encode(sessionid+"."+suffix);
			context.put("forwardUrl",configUrl);
			return "lti_content_redirect";
		}

                String contentReturn = serverConfigurationService.getToolUrl() + "/" + placement.getId() +
                        "/sakai.basiclti.admin.helper.helper" +
                        "?eventSubmit_doContentItemEditorHandle=Save" +
                        "&" + RequestFilter.ATTR_SESSION + "=" +  URLEncoder.encode(sessionid+"."+suffix) +
                        "&panel=PostContentItem" +
                        "&tool_id=" + tool.get(LTIService.LTI_ID);

                // Add CSRF protection so it actually makes it into the "do" code
                contentReturn = SakaiBLTIUtil.addCSRFToken(contentReturn);

                // /acccess/blti/context/tool:12 (does not have a querystring)
                String contentLaunch  = ltiService.getToolLaunch(tool, placement.getContext());

                // Can set ContentItemSelection launch values or put in our own data items
                // which will come back later.  Be mindful of GET length limitations enroute
                // to the access servlet.
                Properties contentData = new Properties();

		// TODO: Expand the MIME type
                contentData.setProperty(ContentItem.ACCEPT_MEDIA_TYPES, "*/*");
                contentData.setProperty("remember", "the answer is 42");  // An example

                contentLaunch = ContentItem.buildLaunch(contentLaunch , contentReturn, contentData);

		log.debug("Forwarding frame to={}", contentLaunch);
		context.put("forwardUrl",contentLaunch);
		return "lti_content_redirect";
	}

	public String buildCKEditorPostConfigPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		state.removeAttribute(STATE_SUCCESS);

		String id = data.getParameters().getString("ltiItemId");
		Long contentKey = null;
		if ( id != null && id.startsWith("/blti/") ) {
			String cid = id.substring(6);
			contentKey = foorm.getLongNull(cid);
		}

		String contentUrl = null;
		Map<String,Object> content = ltiService.getContent(contentKey, getSiteId(state));
		if ( content != null ) {
			contentUrl = ltiService.getContentLaunch(content);
			if ( contentUrl != null && contentUrl.startsWith("/") ) contentUrl = SakaiBLTIUtil.getOurServerUrl() + contentUrl;
		}

		if ( contentUrl == null ) {
			log.error("Unable to get launch url from contentitem content={}", contentKey);
			addAlert(state, rb.getString("error.contentitem.content.launch"));
			return "lti_error";
		}

		JSONArray new_content = new JSONArray();

		JSONObject item = (JSONObject) new JSONObject();
		item.put(LTI2Constants.TYPE, ContentItem.TYPE_LTILINKITEM);
		item.put("launch", contentUrl);
		String title = (String) content.get(LTIService.LTI_TITLE);
		if ( title == null ) title = rb.getString("contentitem.generic.title");
		item.put(ContentItem.TITLE, title);

		new_content.add(item);

		context.put("new_content", new_content);
		context.put("goodcount", new Integer(1));
		return "lti_editor_done";
	}

	public String buildContentDeletePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doContentDelete");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_deploy_system";
		}
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key, getSiteId(state));
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_deploy_system";
		}
		Long tool_id_long = null;
		try{
			tool_id_long = new Long(content.get(LTIService.LTI_TOOL_ID).toString());
		}
		catch (Exception e)
		{
			// log the error
			log.error("error parsing tool id {}", content.get(LTIService.LTI_TOOL_ID));
		}
		context.put("tool_id_long", tool_id_long);
		context.put("content",content);
		context.put("ltiService", ltiService);

		state.removeAttribute(STATE_SUCCESS);
		return "lti_content_delete";
	}

	// Insert or edit
	public void doContentDelete(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "ToolSite");
			return;
		}
		Long key = new Long(id);
		// also remove the link
		if ( ltiService.deleteContent(key, getSiteId(state)) )
		{
			state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));
		} else {
			addAlert(state,rb.getString("error.delete.fail"));
		}

		if ( ToolUtils.isInlineRequest(data.getRequest()) ) {
			switchPanel(state, "ToolSite");
		} else {
			switchPanel(state, "Refresh");
		}
	}

	public String buildLinkAddPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doSiteLink");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_deploy_system";
		}
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key, getSiteId(state));
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_deploy_system";
		}
		context.put("content",content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_add";
	}

	// Insert or edit
	public void doSiteLink(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		String button_text = data.getParameters().getString("button_text");

		Object retval = ltiService.insertToolSiteLink(id, button_text, getSiteId(state));
		if ( retval instanceof String ) {
			String prefix = ((String) retval).substring(0,2);
			addAlert(state, ((String) retval).substring(2));
			if ("0-".equals(prefix))
			{
				switchPanel(state, "Content");
			}
			else if ("1-".equals(prefix))
			{
				switchPanel(state, "Error");
			}
			return;
		}

		state.setAttribute(STATE_SUCCESS,rb.getString("success.link.add"));

		if ( ToolUtils.isInlineRequest(data.getRequest()) ) {
			switchPanel(state, "ToolSite");
		} else {
			switchPanel(state, "Refresh");
		}
	}

	public String buildLinkRemovePanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		if ( ! ltiService.isMaintain(getSiteId(state)) ) {
			addAlert(state,rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doLinkRemove");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key, getSiteId(state));
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_main";
		}
		context.put("content",content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_remove";
	}

	public void doLinkRemove(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String id = data.getParameters().getString(LTIService.LTI_ID);
		Long key = id == null ? null:new Long(id);

		String rv = ltiService.deleteContentLink(key, getSiteId(state));
		if (rv != null)
		{
			// there is error removing the external tool site link
			addAlert(state, rv);
			switchPanel(state, "Error");
			return;
		}
		else
		{
			// external tool site link removed successfully
			state.setAttribute(STATE_SUCCESS,rb.getString("success.link.remove"));
			if ( ToolUtils.isInlineRequest(data.getRequest()) ) {
				switchPanel(state, "ToolSite");
			} else {
				switchPanel(state, "Refresh");
			}
		}
	}

	public String buildRefreshPanelContext(VelocityPortlet portlet, Context context,
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("includeLatestJQuery", PortalUtils.includeLatestJQuery("LTIAdminTool"));
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_top_refresh";
	}

	//generates a search clause (SEARCH_FIELD_1:SEARCH_VALUE_1[#&#|#\\|#]SEARCH_FIELD_2:SEARCH_VALUE_2[#&#|#\\|#]...[#&#|#\\|#]SEARCH_FIELD_N:SEARCH_VALUE_N) and puts some parameters in the context
	private String buildSearch(RunData data, Context context) {
        SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
        StringBuilder sb = new StringBuilder();
        Map<String, String> searchMap = (Map<String, String>)state.getAttribute(ATTR_SEARCH_MAP);
        if (searchMap != null) {
            for (String k : searchMap.keySet()) {
                if (sb.length() > 0) {
                    sb.append(LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND);
                }
                if (StringUtils.isNotEmpty(k) && StringUtils.isNotEmpty((String)searchMap.get(k))) {
                	if("created_at".equals(k)) {
                		sb.append(k + ":" + LTIService.LTI_SEARCH_TOKEN_DATE + searchMap.get(k));
                	} else {
	                    sb.append(k + ":" + searchMap.get(k));
	                    if("URL".equals(k)) {
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

}
