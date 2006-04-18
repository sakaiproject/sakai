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

// package
package org.sakaiproject.tool.sitebrowser;

// imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.util.java.ResourceLoader;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.menu.Menu;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.session.SessionState;
import org.sakaiproject.service.legacy.announcement.cover.AnnouncementService;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.sakaiproject.service.legacy.content.cover.ContentTypeImageService;
import org.sakaiproject.service.legacy.coursemanagement.cover.CourseManagementService;
import org.sakaiproject.service.legacy.entity.Reference;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;
import org.sakaiproject.util.java.StringUtil;
import org.sakaiproject.service.framework.component.cover.ComponentManager;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
* <p>SiteBrowserAction is the CHEF site browser, showing a searchable list of the defined sites, and details including
* public resources of each when selected.</p>
* 
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class SiteBrowserAction
	extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("sitebrowser");
	private final static String SITE_TYPE_ANY = "Any";
	private final static String SITE_TERM_ANY = "Any";
	private final static String STATE_TERM_SELECTION = "termSelection";
	
	private final static String STATE_SEARCH_SITE_TYPE = "siteType";
	private final static String STATE_SEARCH_LIST = "searchList";
	private final static String STATE_PROP_SEARCH_MAP = "propertyCriteriaMap";
	
	private final static String SIMPLE_SEARCH_VIEW = "simpleSearch";
	private final static String LIST_VIEW = "list";
	
	// for the site with extra search criteria 
	private final static String SEARCH_TERM_SITE_TYPE = "termSearchSiteType";
	private final static String SEARCH_TERM_PROP = "termProp";
	
	private static final String NO_SHOW_SEARCH_TYPE = "noshow_search_sitetype";
	
	/** for navigating between sites in site list */
	private static final String STATE_SITES = "state_sites";
	private static final String STATE_PREV_SITE = "state_prev_site";
	private static final String STATE_NEXT_SITE = "state_next_site";
	
	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
				
		return SiteService.getSites(org.sakaiproject.service.legacy.site.SiteService.SelectionType.PUBVIEW,
			state.getAttribute(STATE_SEARCH_SITE_TYPE), search, (HashMap)state.getAttribute(STATE_PROP_SEARCH_MAP), 
			org.sakaiproject.service.legacy.site.SiteService.SortType.TITLE_ASC, new PagingPosition(first, last));

	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
		
		return SiteService.countSites(org.sakaiproject.service.legacy.site.SiteService.SelectionType.PUBVIEW,
			state.getAttribute(STATE_SEARCH_SITE_TYPE), search, (HashMap)state.getAttribute(STATE_PROP_SEARCH_MAP));
	}

	/**
	* Populate the state object, if needed.
	*/
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);
		
		state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		
		// if site type which requires term search exists
		// get all term-search related data from configuration,
		String termSearchSiteType = ServerConfigurationService.getString("sitebrowser.termsearch.type");
		if (termSearchSiteType != null)
		{
			state.setAttribute(SEARCH_TERM_SITE_TYPE, termSearchSiteType);
			
			String termSearchProperty = ServerConfigurationService.getString("sitebrowser.termsearch.property");
			state.setAttribute(SEARCH_TERM_PROP, termSearchProperty);
		}
		
		String noSearchSiteType = StringUtil.trimToNull(ServerConfigurationService.getString("sitesearch.noshow.sitetype"));
		if (noSearchSiteType != null)
		{
			state.setAttribute(NO_SHOW_SEARCH_TYPE, noSearchSiteType);
		}
		
		// setup the observer to notify our main panel
/*		if (state.getAttribute(STATE_OBSERVER) == null)
		{
			// the delivery location for this tool
			String deliveryId = clientWindowId(state, portlet.getID());
			
			// the html element to update on delivery
			String elementId = mainPanelUpdateId(portlet.getID());
			
			// the event resource reference pattern to watch for
			String pattern = SiteService.siteReference("");

			state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern));
		}

		// make sure the observer is in sync with state
		updateObservationOfChannel(state, portlet.getID());
*/
	}   // initState

	/**
	* Setup our observer to be watching for change events for our channel.
	* @param peid The portlet id.
	*/
	private void updateObservationOfChannel(SessionState state, String peid)
	{
/*		EventObservingCourier observer = (EventObservingCourier) state.getAttribute(STATE_OBSERVER);

		// the delivery location for this tool
		String deliveryId = clientWindowId(state, peid);
		observer.setDeliveryId(deliveryId);
*/		
	}   // updateObservationOfChannel

	/** 
	* build the context
	*/
	public String buildMainPanelContext(VelocityPortlet portlet, 
										Context context,
										RunData rundata,
										SessionState state)
	{
		context.put("tlang",rb);
		String template = null;

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");
		if ((mode == null)||mode.equals(SIMPLE_SEARCH_VIEW))
		{
			template = buildSimpleSearchContext(state, context);
		}
		else if (mode.equals(LIST_VIEW))
		{
			template = buildListContext(state, context);
		}
		//else if (mode.equals(ADV_SEARCH_VIEW))
		//{
		//	template = buildAdvSearchContext(state, context);
		//}
		else if (mode.equals("visit"))
		{
			template = buildVisitContext(state, context);
		}
		else
		{
			Log.warn("chef", "SiteBrowserAction: mode: " + mode);
			template = buildListContext(state, context);
		}
		
		return (String)getContext(rundata).get("template") + template;

	}	// buildMainPanelContext

	/**
	* Build the context for the main list mode.
	*/
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context (used for allow update calls on each site)
		context.put("service", SiteService.getInstance());
		
		context.put("termProp", (String)state.getAttribute(SEARCH_TERM_PROP));
		context.put("searchText", (String)state.getAttribute(STATE_SEARCH));
		context.put("siteType", (String)state.getAttribute(STATE_SEARCH_SITE_TYPE));
		context.put("termSelection", (String)state.getAttribute(STATE_TERM_SELECTION));

		//String newPageSize = state.getAttribute(STATE_PAGESIZE).toString();
		Integer newPageSize = (Integer) state.getAttribute("inter_size");
		if (newPageSize != null)
		{
			context.put("pagesize", newPageSize);	
			state.setAttribute(STATE_PAGESIZE, newPageSize);
		}
		else
		{
			state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
			context.put("pagesize", new Integer(DEFAULT_PAGE_SIZE));
		}
		
		// prepare the paging of realms
		List sites = prepPage(state);
		state.setAttribute(STATE_SITES, sites);
		context.put("sites", sites);
		
		if (state.getAttribute(STATE_NUM_MESSAGES) != null)
			context.put("allMsgNumber", state.getAttribute(STATE_NUM_MESSAGES).toString());

		// find the position of the message that is the top first on the page
		if ((state.getAttribute(STATE_TOP_PAGE_MESSAGE) != null) && (state.getAttribute(STATE_PAGESIZE) != null))
		{
			int topMsgPos = ((Integer)state.getAttribute(STATE_TOP_PAGE_MESSAGE)).intValue() + 1;
			context.put("topMsgPos", Integer.toString(topMsgPos));
			int btmMsgPos = topMsgPos + ((Integer)state.getAttribute(STATE_PAGESIZE)).intValue() - 1;
			if (state.getAttribute(STATE_NUM_MESSAGES) != null)
			{
				int allMsgNumber = ((Integer)state.getAttribute(STATE_NUM_MESSAGES)).intValue();
				if (btmMsgPos > allMsgNumber)
					btmMsgPos = allMsgNumber;
			}
			context.put("btmMsgPos", Integer.toString(btmMsgPos));
		}
		
		// build the menu
		Menu bar = new Menu();
		
		// add the search commands
		//bar.add( new MenuField(FORM_SEARCH, "toolbar", "doSearch", (String) state.getAttribute(STATE_SEARCH)));
		//bar.add( new MenuEntry("Search", null, true, MenuItem.CHECKED_NA, "doSearch", "toolbar"));

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
		
		//}
		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		// justDelivered(state);

		return "_list";

	}	// buildListContext
	
	/**
	* Build the context for the simple search mode.
	*/
	private String buildSimpleSearchContext(SessionState state, Context context)
	{

		List newTypes = new Vector();
		if (state.getAttribute(NO_SHOW_SEARCH_TYPE) != null)
		{
			String noType = state.getAttribute(NO_SHOW_SEARCH_TYPE).toString();
			List oldTypes = SiteService.getSiteTypes();
			for (int i = 0; i < oldTypes.size(); i++)
			{
				String siteType = oldTypes.get(i).toString();
				if ((siteType.indexOf(noType)) == -1)
				{
					newTypes.add(siteType);
				}
			}
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
		
		List terms = CourseManagementService.getTerms();
		
		String termSearchSiteType = (String)state.getAttribute(SEARCH_TERM_SITE_TYPE);
		if (termSearchSiteType != null)
		{
			context.put("termSearchSiteType", termSearchSiteType);
			context.put("terms", terms);
		}
		
		return "_simpleSearch";

	}	// buildSimpleSearchContext
	
	/**
	* Build the context for the visit site mode.
	*/
	private String buildVisitContext(SessionState state, Context context)
		{
			List sites = (List)state.getAttribute(STATE_SITES);
			String siteId = (String) state.getAttribute("siteId");
			
			try
			{
				Site site = SiteService.getSite(siteId);
			
				if (sites != null)
				{
					int pos = -1;
					for (int index=0;index<sites.size() && pos==-1;index++)
					{
						if (((Site) sites.get(index)).getId().equals(siteId))
					 	{
					 		pos = index;
					 	}
					}
					 
					// has any previous site in the list?
					if (pos > 0)
					{
						state.setAttribute(STATE_PREV_SITE, sites.get(pos-1));
					}
					else
					{
					 	state.removeAttribute(STATE_PREV_SITE);
					}
					 
					//has any next site in the list?
					if (pos < sites.size()-1)
					{
						state.setAttribute(STATE_NEXT_SITE,sites.get(pos+1));
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

				//get the public syllabus
 				try
				{
					Object syllabusService = ComponentManager.get("org.sakaiproject.api.app.syllabus.SyllabusService");
					Class syllabusServiceClass = syllabusService.getClass();
					Class[] paramsClasses = new Class[1];
					paramsClasses[0] = java.lang.String.class;
					Method getMessages = syllabusServiceClass.getMethod("getMessages", paramsClasses);
					String paramSiteId = site.getId();
					List syllabusList = (ArrayList) getMessages.invoke(syllabusService, new Object[]{paramSiteId});
					context.put("syllabus", syllabusList);
				}
				catch (Exception reflectionEx)
				{
			  	Log.error("Reflection exceptions in SiteBrowserAction for getting public syllabus" + reflectionEx, "");
			  	reflectionEx.printStackTrace();
				}

				
				// get the public resources
				List resources = ContentHostingService.getAllResources(ContentHostingService.getSiteCollection(site.getId()));
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
			}
			catch(IdUnusedException err)
			{
			}
			
			return "_visit";

		}	// buildVisitContext
	
	public void doNavigate_to_site(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		String siteId = StringUtil.trimToNull(data.getParameters().getString("newSiteId"));
		if (siteId != null)
		{
			state.setAttribute("siteId", siteId);
		}
		else
		{
			doBack(data, context);
		}
	
	}	// doNavigate_to_site

	/**
	* Handle a request to visit a site.
	*/
	public void doVisit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		String id = data.getParameters().getString("id");
		String position = data.getParameters().getString("pos");

		// get the site
		try
		{
			Site site = SiteService.getSite(id); 
			state.setAttribute("siteId", id);
			state.setAttribute("mode", "visit");
			
			int pos = (new Integer(position)).intValue() - 1;
			state.setAttribute(STATE_VIEW_ID, new Integer(pos));

			// disable auto-updates while in view mode
			//	((EventObservingCourier) state.getAttribute(STATE_OBSERVER)).disable();
		}
		catch (IdUnusedException e)
		{
			Log.warn("chef", "SiteBrowserAction.doEdit: site not found: " + id);

			addAlert(state,  rb.getString("site") + " " + id + " " + rb.getString("notfound"));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			// enableObserver(state);
		}

	}	// doVisit

	/**
	* Handle a request to return to the list.
	*/
	public void doBack(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		//state.removeAttribute("mode");
		state.removeAttribute("siteId");
		
		state.setAttribute("mode", LIST_VIEW);

	}	// doBack
		
	/**
	* Handle a request to go to Simple Search Mode.
	*/
	public void doShow_simple_search(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		state.setAttribute("mode", SIMPLE_SEARCH_VIEW);

	}	// doShow_simple_search
	
	/**
	* Handle a request to go to Advanced Search Mode.
	*/
	/*
	public void doShowadvsearch(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		state.setAttribute("mode", ADV_SEARCH_VIEW);

	}	// doShowadvsearch
	*/
	
	/**
	* Handle a request to search.
	*/
	public void doSearch(RunData data, Context context)
	{
		super.doSearch(data, context);
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());
		
		String mode = (String) state.getAttribute("mode");
		state.setAttribute("searchMode", mode);
		
		state.removeAttribute(STATE_PROP_SEARCH_MAP);
		state.removeAttribute(STATE_TERM_SELECTION);
		
		// read the search form field into the state object
		String siteType = StringUtil.trimToNull(data.getParameters().getString("siteType"));
		if (siteType != null)
		{
			if (siteType.equalsIgnoreCase("Any"))
				state.setAttribute(STATE_SEARCH_SITE_TYPE, null);
			else
			{
				state.setAttribute(STATE_SEARCH_SITE_TYPE, siteType);
				
				String termSearchSiteType = (String)state.getAttribute(SEARCH_TERM_SITE_TYPE);
				if (termSearchSiteType != null)
				{
					if (siteType.equals(termSearchSiteType))
					{
						// search parameter - term; termId from UI	
						String term = StringUtil.trimToNull(data.getParameters().getString("selectTerm"));
						if (term != null)
						{
							state.setAttribute(STATE_TERM_SELECTION, term);
							
							// property criteria map
							Map pMap = null;
							if (!SITE_TERM_ANY.equals(term))
							{
								pMap = new HashMap();
								pMap.put((String)state.getAttribute(SEARCH_TERM_PROP), term);
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

		state.setAttribute("mode", LIST_VIEW);
		
		state.setAttribute(STATE_PAGESIZE, new Integer(DEFAULT_PAGE_SIZE));
		state.removeAttribute("inter_size");
		
	}	// doSearch
	

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

	}	// convertReferenceUrl

}	// SiteBrowserAction



