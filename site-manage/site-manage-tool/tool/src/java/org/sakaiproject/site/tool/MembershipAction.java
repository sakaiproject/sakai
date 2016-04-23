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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.site.util.SiteTextEditUtil;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * MembershipAction is a tool which displays Sites and lets the user join and un-join joinable Sites.
 * </p>
 */
@Slf4j
public class MembershipAction extends PagedResourceActionII
{
	private static String STATE_VIEW_MODE = "state_view";

	private static ResourceLoader rb = new ResourceLoader("membership");

	private static String SORT_ASC = "sort_asc";

	private static String JOINABLE_SORT_ASC = "sort_asc";

	private static String STATE_CONFIRM_VIEW_MODE = "state_confirm_view";

	private static String UNJOIN_SITE = "unjoin_site";

	private static String SEARCH_TERM = "search";
	
	private static final String STATE_TOP_PAGE_MESSAGE = "msg-top";
	
	private static UserAuditRegistration userAuditRegistration = (UserAuditRegistration) ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.membership");
	private static UserAuditService userAuditService = (UserAuditService) ComponentManager.get(UserAuditService.class);
	private static UserDirectoryService userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
	private static final CourseManagementService cms = (CourseManagementService) ComponentManager.get( CourseManagementService.class );

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		int size = 0;

		String search = (String) state.getAttribute(SEARCH_TERM);
		if ((search != null) && search.trim().equals(""))
		{
			search = null;
		}

		boolean defaultMode = state.getAttribute(STATE_VIEW_MODE) == null;
		if (defaultMode)
		{
			List unjoinableSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
					null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
			size=unjoinableSites.size();
		}
		else
		{
		List openSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.JOINABLE,
				null, search, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
			
            // bjones86 - SAK-24423 - joinable site settings - filter sites
            JoinableSiteSettings.filterSitesListForMembership( openSites );
		size = openSites.size();
		}

		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#readResourcesPage(org.sakaiproject.service.framework.session.SessionState, int, int)
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		List rv = new Vector();

		String search = (String) state.getAttribute(SEARCH_TERM);
		if ((search != null) && search.trim().equals(""))
		{
			search = null;
		}
		
		boolean defaultMode = state.getAttribute(STATE_VIEW_MODE) == null;
		PagingPosition page = new PagingPosition(first, last);
		
		// check the sort order
		boolean sortAsc = true;
		if (state.getAttribute(SORT_ASC) != null)
		{
			sortAsc =((Boolean) state.getAttribute(SORT_ASC)).booleanValue();
		}
		else
		{
			state.setAttribute(SORT_ASC, Boolean.TRUE);
		}
		
		if (defaultMode)
		{
			if (sortAsc)
			{
				rv = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, search,
						null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, page);
			}
			else
			{
				rv = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, search,
						null, org.sakaiproject.site.api.SiteService.SortType.TITLE_DESC, page);
			}
		}
		else
		{

			if (sortAsc)
			{
				List<Site> sites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.JOINABLE, 
						// null, null, null, org.sakaiproject.service.legacy.site.SiteService.SortType.TITLE_ASC, null);
						null, search, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, page);
				
				// bjones86 - SAK-24423 - filter sites taking into account 'exclude from public list' setting and global toggle
				JoinableSiteSettings.filterSitesListForMembership( sites );
				rv = sites;
			}
			else
			{
				List<Site> sites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.JOINABLE, 
						// null, null, null, org.sakaiproject.service.legacy.site.SiteService.SortType.TITLE_ASC, null);
						null, search, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_DESC, page);
				
				// bjones86 - SAK-24423 - filter sites taking into account 'exclude from public list' setting and global toggle
				JoinableSiteSettings.filterSitesListForMembership( sites );
				rv = sites;
			}
		}

		//PagingPosition page = new PagingPosition(first, last);
		//page.validate(rv.size());
		//rv = rv.subList(page.getFirst() - 1, page.getLast());

		return rv;
	}

	/** the above : paging * */

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// buildMenu(portlet, context, rundata, state);

		String template = (String) getContext(rundata).get("template");

		// read the group ids to join
		if (state.getAttribute(SORT_ASC) == null)
		{
			state.setAttribute(SORT_ASC, Boolean.TRUE);
		}
		Boolean sortAsc = (Boolean) state.getAttribute(SORT_ASC);
		context.put("currentSortAsc", sortAsc);

		if (state.getAttribute(SEARCH_TERM) == null)
		{
			state.setAttribute(SEARCH_TERM, "");
		}
		context.put(SEARCH_TERM, state.getAttribute(SEARCH_TERM));

		boolean defaultMode = state.getAttribute(STATE_VIEW_MODE) == null;
		if (defaultMode)
		{
			// process all the sites the user has access to so can unjoin
			//List unjoinableSites = new Vector();
			List unjoinableSites = prepPage(state);
			pagingInfoToContext(state, context);
			
			/*
			 if (sortAsc.booleanValue())
			{
				unjoinableSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
						null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
			}
			else
			{
				unjoinableSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
						null, org.sakaiproject.site.api.SiteService.SortType.TITLE_DESC, null);
			}
			*/
			context.put("unjoinableSites", unjoinableSites);

			// SAK-29138
			Map<String, String> siteGroupsMap = new HashMap<>();
			Map<String, String> sectionRoles = cms.findSectionRoles( userDirectoryService.getCurrentUser().getEid() );
			for( Object obj : unjoinableSites )
			{
				Site site = (Site) obj;
				List<String> providerIDs = SiteParticipantHelper.getProviderCourseList( site.getId() );
				StringBuilder sectionTitles = new StringBuilder();
				for( String providerID : providerIDs )
				{
					// If the user isn't enrolled in this section, skip it
					if( !sectionRoles.keySet().contains( providerID ) )
					{
						continue;
					}

					if( sectionTitles.length() != 0 )
					{
						sectionTitles.append( ", " );
					}

					try
					{
						Section section = cms.getSection( providerID );
						sectionTitles.append( section.getTitle() );
					}
					catch( IdNotFoundException ex )
					{
					 	log.warn( "cannot find section {}, {}", providerID, ex.getMessage());
					}
				}
				
				siteGroupsMap.put( site.getId(), sectionTitles.toString() );
			}
			context.put( "siteGroupsMap", siteGroupsMap );

			context.put("tlang", rb);

			context.put("SiteService", SiteService.getInstance());

			// if property set in sakai.properties then completely disable 'unjoin' link
			if (ServerConfigurationService.getBoolean("disable.membership.unjoin.selection", false))
			{
				context.put("disableUnjoinSelection", Boolean.TRUE);
			}
			
			if (ServerConfigurationService.getStrings("wsetup.disable.unjoin") != null)
			{
				context.put("disableUnjoinSiteTypes", new ArrayList(Arrays.asList(ServerConfigurationService.getStrings("wsetup.disable.unjoin"))));
			}
		}
		
		else
		{
			template = buildJoinableContext(portlet, context, rundata, state);
		}
		// build confirmation screen context
		if (state.getAttribute(STATE_CONFIRM_VIEW_MODE) != null)
		{
			if (state.getAttribute(STATE_CONFIRM_VIEW_MODE).equals("unjoinconfirm"))
			{
				template = buildUnjoinconfirmContext(portlet, context, rundata, state);
			}
		}
		context.put("tlang", rb);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		context.put("membershipTextEdit", new SiteTextEditUtil());

		return template;

	} // buildMainPanelContext
	
	/**
	 * Navigate to confirmation screen
	 */
	public void doGoto_unjoinconfirm(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String[] id = data.getParameters().getStrings("itemReference");
				
		if (id==null){
			state.setAttribute(STATE_CONFIRM_VIEW_MODE, "noSelectionUnjoin");
			addAlert(state, rb.getString("mb.noselection.unjoin"));
		}
		else
		{
			state.setAttribute(STATE_CONFIRM_VIEW_MODE, "unjoinconfirm");
			
		}
		
		state.setAttribute(UNJOIN_SITE, id);
	}

	/**
	 * Build context for confirmation screen
	 * 
	 * @param portlet
	 * @param context
	 * @param runData
	 * @param state
	 * @return
	 */
	public String buildUnjoinconfirmContext(VelocityPortlet portlet, Context context, RunData runData, SessionState state)
	{

		context.put("tlang", rb);
		if (state.getAttribute(UNJOIN_SITE) != null)
		{
			String[] items=(String[])state.getAttribute(UNJOIN_SITE);
			List unjoinSite=new Vector();

			for (int i=0; i<items.length;i++){
				try
				{
					unjoinSite.add(SiteService.getSite(items[i]).getTitle());
				}
				catch (IdUnusedException e)
				{
				 	log.warn(e.getMessage());
				}
			}
			context.put("unjoinSite", unjoinSite);
			
		}

		String template = (String) getContext(runData).get("template");
		return template + "_confirm";
	}

	/**
	 * process unjoin
	 * 
	 * @param data
	 */
	public void doGoto_unjoinyes(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.removeAttribute(STATE_CONFIRM_VIEW_MODE);
		state.removeAttribute(STATE_TOP_PAGE_MESSAGE);
		doUnjoin(data);
	}

	/**
	 * cancel unjoin of site
	 * 
	 * @param data
	 */
	public void doGoto_unjoincancel(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.removeAttribute(STATE_CONFIRM_VIEW_MODE);
	}

	/**
	 * Setup for the options panel.
	 */
	public String buildJoinableContext(VelocityPortlet portlet, Context context, RunData runData, SessionState state)
	{
		// the sorting sequence
		if (state.getAttribute(JOINABLE_SORT_ASC) == null)
		{
			state.setAttribute(JOINABLE_SORT_ASC, Boolean.TRUE);
		}
		context.put("currentSortAsc", state.getAttribute(JOINABLE_SORT_ASC));

		if (state.getAttribute(SEARCH_TERM) == null)
		{
			state.setAttribute(SEARCH_TERM, "");
		}
		context.put(SEARCH_TERM, state.getAttribute(SEARCH_TERM));

		List openSites = prepPage(state);
		context.put("openSites", openSites);

		pagingInfoToContext(state, context);

		context.put("tlang", rb);

		String template = (String) getContext(runData).get("template");
		return template + "_joinable";
	}

	/**
	 * Handle the eventSubmit_doGoto_unJoinable command to shwo the list of site which are unjoinable.
	 */
	public void doGoto_unjoinable(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.removeAttribute(STATE_VIEW_MODE);
		state.removeAttribute(STATE_PAGESIZE);
		state.removeAttribute(STATE_TOP_PAGE_MESSAGE);
		state.removeAttribute(SEARCH_TERM);		
	}

	/**
	 * Handle the eventSubmit_doGoto_unJoinable command to shwo the list of site which are joinable.
	 */
	public void doGoto_joinable(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_VIEW_MODE, "joinable");
		state.removeAttribute(STATE_PAGESIZE);
		state.removeAttribute(STATE_TOP_PAGE_MESSAGE);
		state.removeAttribute(SEARCH_TERM);
	}

	/**
	 * Handle the eventSubmit_doJoin command to have the user join one or more sites.
	 */
	public void doJoin(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the group ids to join
		String id = data.getParameters().getString("itemReference");
		if (id != null)
		{
			try
			{
				// bjones86 - SAK-24423 - joinable site settings - join the site
				if( JoinableSiteSettings.doJoinForMembership( id ) )
				{
					addAlert( state, rb.getString( "mb.youhave2" ) + " " + SiteService.getSite( id ).getTitle() );
				}
				else
				{
					addAlert( state, rb.getString( "mb.join.notAllowed" ) );
				}
				
				// add to user auditing
				List<String[]> userAuditList = new ArrayList<String[]>();
				String currentUserEid = userDirectoryService.getCurrentUser().getEid();
				String roleId = SiteService.getSite(id).getJoinerRole();
				String[] userAuditString = {id,currentUserEid,roleId,userAuditService.USER_AUDIT_ACTION_ADD,userAuditRegistration.getDatabaseSourceKey(),currentUserEid};
				userAuditList.add(userAuditString);
				if (!userAuditList.isEmpty())
				{
					userAuditRegistration.addToUserAuditing(userAuditList);
				}
			}
			catch (IdUnusedException | PermissionException e)
			{
			 	log.warn(e.getMessage());
			}
			catch (InUseException e)
			{
				addAlert(state, rb.getString("mb.sitebeing"));
			}
		}

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		 schedulePeerFrameRefresh("sitenav");
		
		//scheduleTopRefresh();

	} // doJoin

	/**
	 * Handle the eventSubmit_doUnjoin command to have the user un-join one or more groups.
	 */
	public void doUnjoin(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form / state to figure out which attachment(s) to add.
		// String id = data.getParameters().getString("itemReference");
		String[] id = (String[]) state.getAttribute(UNJOIN_SITE);
		if (id != null)
		{
			String msg = rb.getString("mb.youhave") + " "; 
			
			// add to user auditing
			List<String[]> userAuditList = new ArrayList<String[]>();
			// get the User object since we need a couple of lookups
			User tempUser = userDirectoryService.getCurrentUser();
			String currentUserId = tempUser.getId();
			String currentUserEid = tempUser.getEid();
			
			for(int i=0; i< id.length; i++){

				try
				{
					// Get the user's role before unjoining the site
					String roleId = SiteService.getSite(id[i]).getUserRole(currentUserId).getId();
					
					SiteService.unjoin(id[i]);
					if (i>0)
					{
						msg=msg+", ";
					}
					msg = msg+SiteService.getSite(id[i]).getTitle();
					
					String[] userAuditString = {id[i],currentUserEid,roleId,userAuditService.USER_AUDIT_ACTION_REMOVE,userAuditRegistration.getDatabaseSourceKey(),currentUserEid};
					userAuditList.add(userAuditString);
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException e)
				{
					// This could occur if the user's role is the maintain role for the site, and we don't let the user
					// unjoin sites they are maintainers of
				 	log.warn(e.getMessage());
				}
				catch (InUseException e)
				{
				 	log.warn(e.getMessage());
					addAlert(state, rb.getString("mb.sitebeing"));
				}
			}
			addAlert(state, msg);
			if (!userAuditList.isEmpty())
			{
				userAuditRegistration.addToUserAuditing(userAuditList);
			}
		}

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	} // doUnjoin

	/**
	 * toggle the sort ascending vs decending property in main view
	 */
	public void doToggle_sort(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(SORT_ASC) != null)
		{
			state.setAttribute(SORT_ASC, Boolean.valueOf(!((Boolean) state.getAttribute(SORT_ASC)).booleanValue()));
		}
	} // doToggle_sort

	/**
	 * toggle the sort ascending vs decending property in joinable view
	 */
	public void doToggle_joinable_sort(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(JOINABLE_SORT_ASC) != null)
		{
			state.setAttribute(JOINABLE_SORT_ASC, Boolean.valueOf(!((Boolean) state.getAttribute(JOINABLE_SORT_ASC)).booleanValue()));
		}
	}
}
