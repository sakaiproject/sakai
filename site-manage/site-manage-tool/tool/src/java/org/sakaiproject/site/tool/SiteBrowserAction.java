/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.site.tool;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.SortTool;

import org.sakaiproject.announcement.cover.AnnouncementService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.util.SiteTextEditUtil;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * SiteBrowserAction is the Sakai site browser, showing a searchable list of the defined sites, and details including public resources of each when selected.
 * </p>
 */
@Slf4j
public class SiteBrowserAction extends PagedResourceActionII implements SiteHelper
{
	private static final String INTER_SIZE = "inter_size";

	private org.sakaiproject.coursemanagement.api.CourseManagementService cms = (org.sakaiproject.coursemanagement.api.CourseManagementService) ComponentManager
	.get(org.sakaiproject.coursemanagement.api.CourseManagementService.class);

	private ContentHostingService contentHostingService;

	private static ResourceLoader rb = new ResourceLoader("sitebrowser");
	
	private static final String PREFIX = "sitebrowser.";

	private static final String MODE = PREFIX+ "mode";

	private final static String SITE_TYPE_ANY = "Any";

	private final static String SITE_TERM_ANY = "Any";

	private final static String STATE_TERM_SELECTION = PREFIX+ "termSelection";

	private final static String STATE_SEARCH_SITE_TYPE = PREFIX+ "siteType";

	private final static String STATE_SEARCH_LIST = PREFIX+ "searchList";

	private final static String STATE_PROP_SEARCH_MAP = PREFIX+ "propertyCriteriaMap";

	private final static String SIMPLE_SEARCH_VIEW = "simpleSearch";

	private final static String LIST_VIEW = "list";

	// for the site with extra search criteria
	private final static String SEARCH_TERM_SITE_TYPE = "termSearchSiteType";

	private final static String SEARCH_TERM_PROP = "termProp";

	private static final String NO_SHOW_SEARCH_TYPE = "noshow_search_sitetype";

	/** for navigating between sites in site list */
	private static final String STATE_SITES = PREFIX+ "state_sites";

	private static final String STATE_PREV_SITE = PREFIX+ "state_prev_site";

	private static final String STATE_NEXT_SITE = PREFIX+ "state_next_site";
	
	private static final String STATE_HELPER_DONE = PREFIX+ "helperDone";
	
	private final static String SORT_KEY_SESSION = "worksitesetup.sort.key.session";
	private final static String SORT_ORDER_SESSION = "worksitesetup.sort.order.session";
	
	// SAK-28997
	private final static String SEARCH_TERM_DISPLAY = "sitebrowser.termsearch.display";
	private final static String SEARCH_TERM_TITLE = "title";
	private final static String SEARCH_TERM_EID = "eid";
	private final static String SEARCH_TERM_DESCRIPTION = "description";

	public SiteBrowserAction() {
		 contentHostingService = (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
	}
	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		// See what sort of search.
		org.sakaiproject.site.api.SiteService.SelectionType type =
			(org.sakaiproject.site.api.SiteService.SelectionType)state.getAttribute(SiteHelper.SITE_PICKER_PERMISSION);


		List sites =  SiteService.getSites(type, state
				.getAttribute(STATE_SEARCH_SITE_TYPE), search, (HashMap) state.getAttribute(STATE_PROP_SEARCH_MAP),
				org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, new PagingPosition(first, last));
		
		return sites;

	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		org.sakaiproject.site.api.SiteService.SelectionType type =
			(org.sakaiproject.site.api.SiteService.SelectionType)state.getAttribute(SiteHelper.SITE_PICKER_PERMISSION);
		
		return SiteService.countSites(type, state
				.getAttribute(STATE_SEARCH_SITE_TYPE), search, (HashMap) state.getAttribute(STATE_PROP_SEARCH_MAP));
	}

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		state.setAttribute(STATE_PAGESIZE, Integer.valueOf(DEFAULT_PAGE_SIZE));

		// if site type which requires term search exists
		// get all term-search related data from configuration,
		String termSearchSiteType = ServerConfigurationService.getString("sitebrowser.termsearch.type");
		if (termSearchSiteType != null)
		{
			state.setAttribute(SEARCH_TERM_SITE_TYPE, termSearchSiteType);

			String termSearchProperty = ServerConfigurationService.getString("sitebrowser.termsearch.property");
			state.setAttribute(SEARCH_TERM_PROP, termSearchProperty);
		}

		String[] noSearchSiteTypes = ServerConfigurationService.getStrings("sitesearch.noshow.sitetype");
		if (noSearchSiteTypes != null)
		{
			state.setAttribute(NO_SHOW_SEARCH_TYPE, noSearchSiteTypes);
		}

		// Make sure we have a permission to be looking for.
		if (!(state.getAttribute(SiteHelper.SITE_PICKER_PERMISSION) instanceof org.sakaiproject.site.api.SiteService.SelectionType))
		{
			// The default is pubview.
			state.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.PUBVIEW);
		}

		// setup the observer to notify our main panel
		/*
		 * if (state.getAttribute(STATE_OBSERVER) == null) { // the delivery location for this tool String deliveryId = clientWindowId(state, portlet.getID()); // the html element to update on delivery String elementId =
		 * mainPanelUpdateId(portlet.getID()); // the event resource reference pattern to watch for String pattern = SiteService.siteReference(""); state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern)); } // make
		 * sure the observer is in sync with state updateObservationOfChannel(state, portlet.getID());
		 */
	} // initState

	/**
	 * Setup our observer to be watching for change events for our channel.
	 * 
	 * @param peid
	 *        The portlet id.
	 */
	private void updateObservationOfChannel(SessionState state, String peid)
	{
		/*
		 * EventObservingCourier observer = (EventObservingCourier) state.getAttribute(STATE_OBSERVER); // the delivery location for this tool String deliveryId = clientWindowId(state, peid); observer.setDeliveryId(deliveryId);
		 */
	} // updateObservationOfChannel


	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String template = null;

		// check mode and dispatch
		String mode = (String) state.getAttribute(MODE);
		if ((mode == null) || mode.equals(SIMPLE_SEARCH_VIEW))
		{
			template = buildSimpleSearchContext(state, context);
		}
		else if (mode.equals(LIST_VIEW))
		{
			template = buildListContext(state, context);
		}
		else if ("visit".equals(mode))
		{
			template = buildVisitContext(state, context);
		}
		
		// SAK-24423 - joinable site settings - join from site browser
		else if( JoinableSiteSettings.SITE_BROWSER_JOIN_MODE.equalsIgnoreCase( mode ) )
		{
			if( JoinableSiteSettings.isJoinFromSiteBrowserEnabled() )
			{
				template = JoinableSiteSettings.buildJoinContextForSiteBrowser( state, context, rb );
			}
		else
		{
			 	log.warn("SiteBrowserAction: mode = {}, but site browser join is disabled globally", mode);
				template = buildListContext( state, context );
			}
		}
		
		else
		{
		 	log.warn("SiteBrowserAction: mode: {}", mode);
			template = buildListContext(state, context);
		}

		return (String) getContext(rundata).get("template") + template;

	} // buildMainPanelContext

	/**
	 * Build the context for the main list mode.
	 */
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context (used for allow update calls on each site)
		context.put("service", SiteService.getInstance());
		
		context.put("helperMode", Boolean.valueOf(state.getAttribute(Tool.HELPER_DONE_URL) != null));

		context.put("termProp", (String) state.getAttribute(SEARCH_TERM_PROP));
		context.put("searchText", (String) state.getAttribute(STATE_SEARCH));
		context.put("siteType", (String) state.getAttribute(STATE_SEARCH_SITE_TYPE));
		context.put("termSelection", (String) state.getAttribute(STATE_TERM_SELECTION));
		context.put("siteBrowserTextEdit", new SiteTextEditUtil());

		// String newPageSize = state.getAttribute(STATE_PAGESIZE).toString();
		Integer newPageSize = (Integer) state.getAttribute(INTER_SIZE);
		if (newPageSize != null)
		{
			context.put("pagesize", newPageSize);
			state.setAttribute(STATE_PAGESIZE, newPageSize);
		}
		else
		{
			state.setAttribute(STATE_PAGESIZE, Integer.valueOf(DEFAULT_PAGE_SIZE));
			context.put("pagesize", Integer.valueOf(DEFAULT_PAGE_SIZE));
		}

		// prepare the paging of realms
		List sites = prepPage(state);
		state.setAttribute(STATE_SITES, sites);
		context.put("sites", sites);
		
		// SAK-24423 - joinable site settings - put the necessary info into the context for the list interface
		JoinableSiteSettings.putSiteMapInContextForSiteBrowser( context, sites );
        JoinableSiteSettings.putCurrentUserInContextForSiteBrowser( context );
        JoinableSiteSettings.putIsSiteBrowserJoinEnabledInContext( context );

		if (state.getAttribute(STATE_NUM_MESSAGES) != null)
			context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());

		// find the position of the message that is the top first on the page
		if ((state.getAttribute(STATE_TOP_PAGE_MESSAGE) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
		{
			int topMsgPos = ((Integer) state.getAttribute(STATE_TOP_PAGE_MESSAGE)).intValue() + 1;
			context.put("topMsgPos", Integer.toString(topMsgPos));
			int btmMsgPos = topMsgPos + ((Integer) state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
			if (state.getAttribute(STATE_NUM_MESSAGES) != null)
			{
				int allMsgNumber = ((Integer) state.getAttribute(STATE_NUM_MESSAGES)).intValue();
				if (btmMsgPos > allMsgNumber) btmMsgPos = allMsgNumber;
			}
			context.put("btmMsgPos", Integer.toString(btmMsgPos));
		}

		// build the menu
		Menu bar = new MenuImpl();

		// add the search commands
		// bar.add( new MenuField(FORM_SEARCH, "toolbar", "doSearch", (String) state.getAttribute(STATE_SEARCH)));
		// bar.add( new MenuEntry("Search", null, true, MenuItem.CHECKED_NA, "doSearch", "toolbar"));

		// add the refresh commands
		// %%% we want manual only
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		boolean goPPButton = state.getAttribute(STATE_PREV_PAGE_EXISTS) != null;
		context.put("goPPButton", Boolean.toString(goPPButton));
		boolean goNPButton = state.getAttribute(STATE_NEXT_PAGE_EXISTS) != null;
		context.put("goNPButton", Boolean.toString(goNPButton));

		// }
		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		// justDelivered(state);
		if (cms != null) 
		{
			Map<String, String> smap =new HashMap<String, String>();
			Collection<AcademicSession> sessions = cms.getAcademicSessions();
			
			// SAK-28997
			String searchTermDisplay = ServerConfigurationService.getString( SEARCH_TERM_DISPLAY, SEARCH_TERM_TITLE );
			for( AcademicSession s : sessions )
			{
				if( SEARCH_TERM_TITLE.equalsIgnoreCase( searchTermDisplay ) )
				{
					smap.put( s.getEid(), s.getTitle() );
				}
				else if( SEARCH_TERM_EID.equalsIgnoreCase( searchTermDisplay ) )
				{
					smap.put( s.getEid(), s.getEid() );
				}
				else if( SEARCH_TERM_DESCRIPTION.equalsIgnoreCase( searchTermDisplay ) )
				{
					smap.put( s.getEid(), s.getDescription() );
				}
			}

			context.put("termsmap", smap );
		}

		return "_list";
	} // buildListContext

	/**
	 * Build the context for the simple search mode.
	 */
	private String buildSimpleSearchContext(SessionState state, Context context)
	{
		List newTypes = new Vector();
		if (state.getAttribute(NO_SHOW_SEARCH_TYPE) != null)
		{
			// SAK-19287
			String[] noTypes = (String[]) state.getAttribute(NO_SHOW_SEARCH_TYPE);
			List<String> oldTypes = SiteService.getSiteTypes();
			
			for (int i = 0; i < noTypes.length; i++) {
				if (noTypes[i] != null && noTypes[i].length() > 0) {
					String noType = noTypes[i].trim();
					if (oldTypes.contains(noType)) {
						oldTypes.remove(noType);
					}
				}
			}
			newTypes.addAll(oldTypes);
		}
		else
		{
			newTypes = SiteService.getSiteTypes();
		}

		// remove the "myworkspace" type
		for (Iterator i = newTypes.iterator(); i.hasNext();)
		{
			String t = (String) i.next();
			if ("myworkspace".equalsIgnoreCase(t))
			{
				i.remove();
			}
		}

		context.put("siteTypes", newTypes);

		String termSearchSiteType = (String) state.getAttribute(SEARCH_TERM_SITE_TYPE);
		if (termSearchSiteType != null)
		{
			context.put("termSearchSiteType", termSearchSiteType);
			if (cms != null) 
			{
				// SAK-28997
				String searchTermDisplay = ServerConfigurationService.getString( SEARCH_TERM_DISPLAY, SEARCH_TERM_TITLE );
				Map<String,String> termsMap = new HashMap<String,String>();
				Collection<AcademicSession> academicSessions = sortAcademicSessions( cms.getAcademicSessions() );
				for( AcademicSession as : academicSessions )
				{
					if( SEARCH_TERM_TITLE.equalsIgnoreCase( searchTermDisplay ) )
					{
						termsMap.put( as.getEid(), as.getTitle() );
					}
					else if( SEARCH_TERM_EID.equalsIgnoreCase( searchTermDisplay ) )
					{
						termsMap.put( as.getEid(),as.getEid() );
					}
					else if( SEARCH_TERM_DESCRIPTION.equalsIgnoreCase( searchTermDisplay ) )
					{
						termsMap.put( as.getEid(), as.getDescription() );
					}
				}
				
				context.put( "terms", termsMap );
			}
		}

		return "_simpleSearch";

	} // buildSimpleSearchContext
	
	/**
	 * Helper method for sortCmObject 
	 * by order from sakai properties if specified or 
	 * by default of eid, title
	 * using velocity SortTool
	 * 
	 * @param sessions
	 * @return
	 */
	private Collection sortAcademicSessions(Collection<AcademicSession> sessions) {
		// Get the keys from sakai.properties
		String[] keys = ServerConfigurationService.getStrings(SORT_KEY_SESSION);
		String[] orders = ServerConfigurationService.getStrings(SORT_ORDER_SESSION);

		return sortCmObject(sessions, keys, orders);
	} // sortCourseOffering
	
	/**
	 * Custom sort CM collections using properties provided object has getter & setter for 
	 * properties in keys and orders
	 * defaults to eid & title if none specified
	 * 
	 * @param collection a collection to be sorted
	 * @param keys properties to sort on
	 * @param orders properties on how to sort (asc, dsc)
	 * @return Collection the sorted collection
	 */
	private Collection sortCmObject(Collection collection, String[] keys, String[] orders) {
		if (collection != null && !collection.isEmpty()) {
			// Add them to a list for the SortTool (they must have the form
			// "<key:order>" in this implementation)
			List propsList = new ArrayList();
			
			if (keys == null || orders == null || keys.length == 0 || orders.length == 0) {
				// No keys are specified, so use the default sort order
				propsList.add("eid");
				propsList.add("title");
			} else {
				// Populate propsList
				for (int i = 0; i < Math.min(keys.length, orders.length); i++) {
					String key = keys[i];
					String order = orders[i];
					propsList.add(key + ":" + order);
				}
			}
			// Sort the collection and return
			SortTool sort = new SortTool();
			return sort.sort(collection, propsList);
		}
			
		return Collections.emptyList();

	} // sortCmObject

	/**
	 * Build the context for the visit site mode.
	 */
	private String buildVisitContext(SessionState state, Context context)
	{
		List sites = (List) state.getAttribute(STATE_SITES);
		String siteId = (String) state.getAttribute("siteId");

		try
		{
			Site site = SiteService.getSite(siteId);

			if (sites != null)
			{
				int pos = -1;
				for (int index = 0; index < sites.size() && pos == -1; index++)
				{
					if (((Site) sites.get(index)).getId().equals(siteId))
					{
						pos = index;
					}
				}

				// has any previous site in the list?
				if (pos > 0)
				{
					state.setAttribute(STATE_PREV_SITE, sites.get(pos - 1));
				}
				else
				{
					state.removeAttribute(STATE_PREV_SITE);
				}

				// has any next site in the list?
				if (pos < sites.size() - 1)
				{
					state.setAttribute(STATE_NEXT_SITE, sites.get(pos + 1));
				}
				else
				{
					state.removeAttribute(STATE_NEXT_SITE);
				}
			}

			if (state.getAttribute(STATE_PREV_SITE) != null)
			{
				context.put("prevSite", state.getAttribute(STATE_PREV_SITE));
			}
			if (state.getAttribute(STATE_NEXT_SITE) != null)
			{
				context.put("nextSite", state.getAttribute(STATE_NEXT_SITE));
			}

			context.put("site", site);

			// get the public announcements
			String anncRef = AnnouncementService.channelReference(site.getId(), SiteService.MAIN_CONTAINER);
			List announcements = null;
			try
			{
				announcements = AnnouncementService.getMessages(anncRef, null, 0, true, false, true);
			}
			catch (PermissionException e)
			{
				announcements = new Vector();
			}

			context.put("announcements", announcements);

			// get the public syllabus
			try
			{
				Object syllabusService = ComponentManager.get("org.sakaiproject.api.app.syllabus.SyllabusService");
				Class syllabusServiceClass = syllabusService.getClass();
				Class[] paramsClasses = new Class[1];
				paramsClasses[0] = java.lang.String.class;
				Method getMessages = syllabusServiceClass.getMethod("getMessages", paramsClasses);
				String paramSiteId = site.getId();
				List syllabusList = (ArrayList) getMessages.invoke(syllabusService, new Object[] { paramSiteId });
				context.put("syllabus", syllabusList);
			}
			catch (Exception reflectionEx)
			{
				log.error("Reflection exceptions in SiteBrowserAction for getting public syllabus {}", reflectionEx);
			}

			// get the public resources
			List resources = contentHostingService.getAllResources(contentHostingService.getSiteCollection(site.getId()));
			context.put("resources", resources);

			// the height for the info frame
			context.put("height", "300px");

			// the url for info
			String url = site.getInfoUrl();
			if (url != null)
			{
				url = url.trim();
				url = convertReferenceUrl(url);
				context.put("infoUrl", url);
			}

			context.put("contentTypeImageService", ContentTypeImageService.getInstance());
			
			// SAK-24423 - joinable site settings - put info into the context for the visit UI
			JoinableSiteSettings.putIsSiteBrowserJoinEnabledInContext( context );
			JoinableSiteSettings.putIsCurrentUserAlreadyMemberInContextForSiteBrowser( context, siteId );
			JoinableSiteSettings.putIsSiteExcludedFromPublic( context, siteId );
		}
		catch (IdUnusedException err)
		{
			log.warn(err.getMessage());
		}

		return "_visit";

	} // buildVisitContext

	public void doNavigate_to_site(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = StringUtils.trimToNull(data.getParameters().getString("newSiteId"));
		if (siteId != null)
		{
			state.setAttribute("siteId", siteId);
		}
		else
		{
			doBack(data, context);
		}

	} // doNavigate_to_site

	/**
	 * Handle a request to visit a site.
	 */
	public void doVisit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");

		// get the site
		try
		{
			Site site = SiteService.getSite(id);
			state.setAttribute("siteId", id);
			state.setAttribute(MODE, "visit");

			// disable auto-updates while in view mode
			// ((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).disable();
		}
		catch (IdUnusedException e)
		{
		 	log.warn("SiteBrowserAction.doEdit: site not found: {}", id);

			addAlert(state, rb.getFormattedMessage("notfound", new Object[]{id}));
			state.removeAttribute(MODE);

			// make sure auto-updates are enabled
			// enableObserver(state);
		}

	} // doVisit
	
	/**
	 * Handle a request to join a site.
	 * 
	 * @param data
	 * 				the state to get the settings from
	 * @param context
	 * 				the object to put the settings into
	 */
	public void doJoin( RunData data, Context context )
	{
		SessionState state = ( (JetspeedRunData) data).getPortletSessionState( ( (JetspeedRunData) data).getJs_peid() );
		String message = JoinableSiteSettings.doJoinForSiteBrowser( state, rb, data.getParameters().getString( "id" ) );
		if( message != null && !message.isEmpty() )
		{
			addAlert( state, message );
		}
	} // doJoin

	/**
	 * Handle a request to return to the list.
	 */
	public void doBack(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.removeAttribute("siteId");

		state.setAttribute(MODE, LIST_VIEW);

	} // doBack

	/**
	 * Handle a request to go to Simple Search Mode.
	 */
	public void doShow_simple_search(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute(MODE, SIMPLE_SEARCH_VIEW);

	} // doShow_simple_search

	/**
	 * Handle a request to go to Advanced Search Mode.
	 */
	/*
	 * public void doShowadvsearch(RunData data, Context context) { SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid()); state.setAttribute("mode", ADV_SEARCH_VIEW); } // doShowadvsearch
	 */

	/**
	 * Handle a request to search.
	 */
	public void doSearch(RunData data, Context context)
	{
		super.doSearch(data, context);

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String mode = (String) state.getAttribute(MODE);
		state.setAttribute("searchMode", mode);

		state.removeAttribute(STATE_PROP_SEARCH_MAP);
		state.removeAttribute(STATE_TERM_SELECTION);
		
		// read the search form field into the state object
		String siteType = StringUtils.trimToNull(data.getParameters().getString("siteType"));
		if (siteType != null)
		{
			if (siteType.equalsIgnoreCase("Any"))
				state.setAttribute(STATE_SEARCH_SITE_TYPE, null);
			else
			{
				state.setAttribute(STATE_SEARCH_SITE_TYPE, siteType);

				String termSearchSiteType = (String) state.getAttribute(SEARCH_TERM_SITE_TYPE);
				if (termSearchSiteType != null)
				{
					if (siteType.equals(termSearchSiteType))
					{
						// search parameter - term; term.eid from UI
						String term = StringUtils.trimToNull(data.getParameters().getString("selectTerm"));
						if (term != null)
						{
							state.setAttribute(STATE_TERM_SELECTION, term);

							// property criteria map
							Map pMap = null;
							if (!SITE_TERM_ANY.equals(term))
							{
								pMap = new HashMap();
								pMap.put((String) state.getAttribute(SEARCH_TERM_PROP), term);
								state.setAttribute(STATE_PROP_SEARCH_MAP, pMap);

							}
						}

					}
				}

			}
		}
		else
		{
			state.setAttribute(STATE_SEARCH_SITE_TYPE, null);
		}

		state.setAttribute(MODE, LIST_VIEW);

		state.setAttribute(STATE_PAGESIZE, Integer.valueOf(DEFAULT_PAGE_SIZE));
		state.removeAttribute(INTER_SIZE);

	} // doSearch
	
	public void doCancel(RunData data, Context context) 
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(SiteHelper.SITE_PICKER_CANCELLED, Boolean.TRUE);
		state.setAttribute(MODE, STATE_HELPER_DONE);
	}
	
	public void doSelect(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String siteId = data.getParameters().getString("siteId");
		if (siteId != null && siteId.length() > 0) {
			state.setAttribute(SiteHelper.SITE_PICKER_SITE_ID, siteId);
			state.setAttribute(MODE, STATE_HELPER_DONE);
		} else { 
			addAlert(state, rb.getString("list.not.selected"));
		}
	}

	/**
	 * Return the url unchanged, unless it's a reference, then return the reference url
	 */
	private String convertReferenceUrl(String url)
	{
		// make a reference
		Reference ref = EntityManager.newReference(url);

		// if it didn't recognize this, return it unchanged
		if (!ref.isKnownType()) return url;

		// return the reference's url
		return ref.getUrl();

	} // convertReferenceUrl
	
	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
	throws ToolException
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		SessionState state = getState(req);

		if (STATE_HELPER_DONE.equals(toolSession.getAttribute(MODE)))
		{


			String url = (String) SessionManager.getCurrentToolSession().getAttribute(Tool.HELPER_DONE_URL);

			SessionManager.getCurrentToolSession().removeAttribute(Tool.HELPER_DONE_URL);

			// TODO: Implement cleanup.
			cleanup(state);
			
			if (log.isDebugEnabled())
			{
				log.debug("Sending redirect to: "+ url);
			}
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				log.warn("Problem sending redirect to: "+ url,  e);
			}
			return;
		}
		else if(sendToHelper(req, res, req.getPathInfo()))
		{
			return;
		}
		else
		{
			super.toolModeDispatch(methodBase, methodExt, req, res);
		}
	}

	private void cleanup(SessionState state) {
		
		List<String> names = (List<String>)state.getAttributeNames();
		for (String name : names) {
			if (name.startsWith(PREFIX)) {
				log.debug("Removed attribute: "+ name);
				state.removeAttribute(name);
			}
		}
		
	}
	

} // SiteBrowserAction

