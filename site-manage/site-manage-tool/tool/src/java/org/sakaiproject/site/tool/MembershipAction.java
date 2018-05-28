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
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.tool.EnrolmentsHandler.Enrolment;
import org.sakaiproject.site.tool.EnrolmentsHandler.EnrolmentsWrapper;
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
	private static final ResourceLoader RB = new ResourceLoader("membership");
	private static final String STATE_VIEW_MODE = "state_view";
	private static final String SORT_ASC = "sort_asc";
	private static final String STATE_CONFIRM_VIEW_MODE = "state_confirm_view";
	private static final String UNJOIN_SITE = "unjoin_site";
	private static final String SEARCH_TERM = "search";
	private static final String STATE_TOP_PAGE_MESSAGE = "msg-top";

	private static final UserAuditRegistration userAuditRegistration = (UserAuditRegistration) ComponentManager.get("org.sakaiproject.userauditservice.api.UserAuditRegistration.membership");
	private static final UserDirectoryService userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);

	// SAK-32087
	private static final ServerConfigurationService SCS = (ServerConfigurationService) ComponentManager.get( ServerConfigurationService.class );
	private static final SiteService SITE_SERV = (SiteService) ComponentManager.get( SiteService.class );
	private static final EnrolmentsHandler ENROLMENTS_HANDLER = new EnrolmentsHandler();
	private static final String SAK_PROP_ENROLMENTS_BLURB = "membership.enrolments.blurb";
	private static final String ENROLMENTS_BLURB = SCS.getString( SAK_PROP_ENROLMENTS_BLURB, "" );
	private static final String MY_ENROLMENTS_MODE = "my_enrolments";
	private static final String JOINABLE_MODE = "joinable";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		int size;

		String search = (String) state.getAttribute(SEARCH_TERM);
		if ((search != null) && search.trim().equals(""))
		{
			search = null;
		}

		String mode = (String) state.getAttribute( STATE_VIEW_MODE );
		if( MY_ENROLMENTS_MODE.equals( mode ) )
		{
			String currentUserID = userDirectoryService.getCurrentUser().getId();
			ENROLMENTS_HANDLER.getSectionEnrolments( currentUserID );

			// If a search is provided, filter the results
			if( StringUtils.isNotBlank( search ) )
			{
				ENROLMENTS_HANDLER.filterSectionEnrolments( search, currentUserID );
				size = ENROLMENTS_HANDLER.getFilteredEnrolments().size();
			}
			else
			{
				if( ENROLMENTS_HANDLER.getEnrolmentsCacheMap().get( currentUserID ) == null )
				{
					ENROLMENTS_HANDLER.getSectionEnrolments( currentUserID );
				}

				EnrolmentsWrapper wrapper = ENROLMENTS_HANDLER.getEnrolmentsCacheMap().get( currentUserID );
				if( wrapper != null )
				{
					List<Enrolment> enrolments = wrapper.getEnrolments();
					size = enrolments != null ? enrolments.size() : 0;
				}
				else
				{
					size = 0;
				}
			}
		}
		else if( JOINABLE_MODE.equals( mode ) )
		{
			List openSites = SITE_SERV.getSites(SelectionType.JOINABLE, null, search, null, SortType.TITLE_ASC, null);

			// SAK-24423 - joinable site settings - filter sites
			JoinableSiteSettings.filterSitesListForMembership( openSites );
			size = openSites.size();
		}
		else
		{
			size = SITE_SERV.countSites(SelectionType.ACCESS, null, search, null);
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
		List rv;

		String search = (String) state.getAttribute(SEARCH_TERM);
		if ((search != null) && search.trim().equals(""))
		{
			search = null;
		}

		String mode = (String) state.getAttribute( STATE_VIEW_MODE );
		PagingPosition page = new PagingPosition(first, last);

		// check the sort order
		boolean sortAsc = true;
		if (state.getAttribute(SORT_ASC) != null)
		{
			sortAsc =((Boolean) state.getAttribute(SORT_ASC));
		}
		else
		{
			state.setAttribute(SORT_ASC, Boolean.TRUE);
		}

		if( MY_ENROLMENTS_MODE.equals( mode ) )
		{
			String sortMode = ENROLMENTS_HANDLER.setSortModeForMyEnrolments( state );
			ENROLMENTS_HANDLER.getSectionEnrolments( userDirectoryService.getCurrentUser().getId() );
			rv = ENROLMENTS_HANDLER.getSortedAndPagedEnrolments( page, sortMode, sortAsc, StringUtils.isNotBlank( search ) );
		}
		else if( JOINABLE_MODE.equals( mode ) )
		{
			if (sortAsc)
			{
				List<Site> sites = SITE_SERV.getSites(SelectionType.JOINABLE, null, search, null, SortType.TITLE_ASC, page);

				// SAK-24423 - filter sites taking into account 'exclude from public list' setting and global toggle
				JoinableSiteSettings.filterSitesListForMembership( sites );
				rv = sites;
			}
			else
			{
				List<Site> sites = SITE_SERV.getSites(SelectionType.JOINABLE, null, search, null, SortType.TITLE_DESC, page);

				// SAK-24423 - filter sites taking into account 'exclude from public list' setting and global toggle
				JoinableSiteSettings.filterSitesListForMembership( sites );
				rv = sites;
			}
		}
		else
		{
			if (sortAsc)
			{
				rv = SITE_SERV.getSites(SelectionType.ACCESS, null, search, null, SortType.TITLE_ASC, page);
			}
			else
			{
				rv = SITE_SERV.getSites(SelectionType.ACCESS, null, search, null, SortType.TITLE_DESC, page);
			}
		}

		return rv;
	}

	/**
	 * build the context
	 * @param portlet
	 * @param context
	 * @param rundata
	 * @param state
	 * @return 
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
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

		String mode = (String) state.getAttribute( STATE_VIEW_MODE );
		if( MY_ENROLMENTS_MODE.equals( mode ) )
		{
			template = buildMyEnrolmentsContext( portlet, context, rundata, state );
		}
		else if( JOINABLE_MODE.equals( mode ) )
		{
			template = buildJoinableContext(portlet, context, rundata, state);
		}
		else
		{
			// process all the sites the user has access to so can unjoin
			List<Site> unjoinableSites = prepPage(state);
			for (Site site : unjoinableSites)
			{
				site.setTitle(SITE_SERV.getUserSpecificSiteTitle(site, userDirectoryService.getCurrentUser().getId()));
			}
			pagingInfoToContext(state, context);
			context.put("unjoinableSites", unjoinableSites);
			context.put("tlang", RB);
			context.put("SiteService", SITE_SERV);

			// if property set in sakai.properties then completely disable 'unjoin' link
			if (SCS.getBoolean("disable.membership.unjoin.selection", false))
			{
				context.put("disableUnjoinSelection", Boolean.TRUE);
			}
			
			if (SCS.getStrings("wsetup.disable.unjoin") != null)
			{
				context.put("disableUnjoinSiteTypes", new ArrayList(Arrays.asList(SCS.getStrings("wsetup.disable.unjoin"))));
			}
		}

		// build confirmation screen context
		if (state.getAttribute(STATE_CONFIRM_VIEW_MODE) != null)
		{
			if (state.getAttribute(STATE_CONFIRM_VIEW_MODE).equals("unjoinconfirm"))
			{
				template = buildUnjoinconfirmContext(portlet, context, rundata, state);
			}
		}

		context.put("tlang", RB);
		context.put("alertMessage", state.getAttribute(STATE_MESSAGE));
		context.put("membershipTextEdit", new SiteTextEditUtil());

		return template;

	} // buildMainPanelContext

	/**
	 * Build the context for the 'My Official Course Enrolments' page.
	 * SAK-32087
	 * @param portlet
	 * @param context
	 * @param runData
	 * @param state
	 * @return 
	 */
	public String buildMyEnrolmentsContext( VelocityPortlet portlet, Context context, RunData runData, SessionState state )
	{
		// Get the sorting sequence (ascending/descending)
		if( state.getAttribute( SORT_ASC ) == null )
		{
			state.setAttribute( SORT_ASC, Boolean.TRUE );
		}
		context.put( "currentSortAsc", state.getAttribute( SORT_ASC ) );

		// Get the sort mode
		String sortMode = ENROLMENTS_HANDLER.setSortModeForMyEnrolments( state );
		context.put( "sortMode", sortMode );

		// Get the search string (if any)
		if( state.getAttribute( SEARCH_TERM ) == null )
		{
			state.setAttribute( SEARCH_TERM, "" );
		}
		context.put( SEARCH_TERM, state.getAttribute( SEARCH_TERM ) );

		// Get the enrolments for the user, taking into consideration sorting, paging and filtering
		List<Enrolment> currentUserEnrolments = prepPage( state );
		context.put( "enrolments", currentUserEnrolments );
		context.put( "sessionHelper", EnrolmentsHandler.SESSION_HELPER );
		context.put( "hasBlurb", StringUtils.isNotBlank( ENROLMENTS_BLURB ) );
		context.put( "enrolmentsBlurb", ENROLMENTS_BLURB );

		Object[] replacements = new Object[] { SCS.getString( "ui.service", "Sakai" ) };
		String noEnrolments = RB.getFormattedMessage( "mb.enrolments.noEnrolments", replacements );
		String summary = RB.getFormattedMessage( "mb.enrolments.summary", replacements );
		context.put( "noEnrolments", noEnrolments );
		context.put( "summary", summary );

		// Put the paging info into the context and return the template name
		pagingInfoToContext( state, context );
		String template = (String) getContext( runData ).get( "template" );
		return template + "_enrolments";
	}

	/**
	 * Navigate to confirmation screen
	 * @param data
	 */
	public void doGoto_unjoinconfirm(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String[] id = data.getParameters().getStrings("itemReference");

		if (id==null){
			state.setAttribute(STATE_CONFIRM_VIEW_MODE, "noSelectionUnjoin");
			addAlert(state, RB.getString("mb.noselection.unjoin"));
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
		context.put("tlang", RB);
		if (state.getAttribute(UNJOIN_SITE) != null)
		{
			String[] items=(String[])state.getAttribute(UNJOIN_SITE);
			List unjoinSite=new ArrayList();

			for( String item : items ){
				try
				{
					unjoinSite.add(SITE_SERV.getSite(item).getTitle());
				}catch (IdUnusedException e)
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
	 * @param portlet
	 * @param context
	 * @param runData
	 * @param state
	 * @return 
	 */
	public String buildJoinableContext(VelocityPortlet portlet, Context context, RunData runData, SessionState state)
	{
		// the sorting sequence
		if (state.getAttribute(SORT_ASC) == null)
		{
			state.setAttribute(SORT_ASC, Boolean.TRUE);
		}
		context.put("currentSortAsc", state.getAttribute(SORT_ASC));

		if (state.getAttribute(SEARCH_TERM) == null)
		{
			state.setAttribute(SEARCH_TERM, "");
		}
		context.put(SEARCH_TERM, state.getAttribute(SEARCH_TERM));

		List<Site> openSites = prepPage(state);
		for (Site site : openSites)
		{
			site.setTitle(SITE_SERV.getUserSpecificSiteTitle(site, userDirectoryService.getCurrentUser().getId()));
		}
		context.put("openSites", openSites);

		pagingInfoToContext(state, context);

		context.put("tlang", RB);

		String template = (String) getContext(runData).get("template");
		return template + "_joinable";
	}

	/**
	 * Handle the eventSubmit_doGoto_unJoinable command to shwo the list of site which are unjoinable.
	 * @param data
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
	 * @param data
	 */
	public void doGoto_joinable(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_VIEW_MODE, JOINABLE_MODE);
		state.removeAttribute(STATE_PAGESIZE);
		state.removeAttribute(STATE_TOP_PAGE_MESSAGE);
		state.removeAttribute(SEARCH_TERM);
	}

	/**
	 * Handle the eventSubmit_doGoto_enrolments command to show the list of enrolments for the current user.
	 * SAK-32087
	 * @param data 
	 */
	public void doGoto_enrolments( RunData data )
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState( ((JetspeedRunData) data).getJs_peid() );
		state.setAttribute( STATE_VIEW_MODE, MY_ENROLMENTS_MODE );
		state.removeAttribute( STATE_PAGESIZE );
		state.removeAttribute( STATE_TOP_PAGE_MESSAGE );
		state.removeAttribute( SEARCH_TERM );
	}

	/**
	 * Handle the eventSubmit_doJoin command to have the user join one or more sites.
	 * @param data
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
				// SAK-24423 - joinable site settings - join the site
				if( JoinableSiteSettings.doJoinForMembership( id ) )
				{
					addAlert( state, RB.getString( "mb.youhave2" ) + " " + SITE_SERV.getSite( id ).getTitle() );
				}
				else
				{
					addAlert( state, RB.getString( "mb.join.notAllowed" ) );
				}

				// add to user auditing
				List<String[]> userAuditList = new ArrayList<>();
				String currentUserEid = userDirectoryService.getCurrentUser().getEid();
				String roleId = SITE_SERV.getSite(id).getJoinerRole();
				String[] userAuditString = {id,currentUserEid,roleId,UserAuditService.USER_AUDIT_ACTION_ADD,userAuditRegistration.getDatabaseSourceKey(),currentUserEid};
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
				addAlert(state, RB.getString("mb.sitebeing"));
			}
		}

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");
	} // doJoin

	/**
	 * Handle the eventSubmit_doUnjoin command to have the user un-join one or more groups.
	 * @param data
	 */
	public void doUnjoin(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form / state to figure out which attachment(s) to add.
		// String id = data.getParameters().getString("itemReference");
		String[] id = (String[]) state.getAttribute(UNJOIN_SITE);
		if (id != null)
		{
			String msg = RB.getString("mb.youhave") + " "; 
			
			// add to user auditing
			List<String[]> userAuditList = new ArrayList<>();
			// get the User object since we need a couple of lookups
			User tempUser = userDirectoryService.getCurrentUser();
			String currentUserId = tempUser.getId();
			String currentUserEid = tempUser.getEid();

			for(int i=0; i< id.length; i++){

				try
				{
					// Get the user's role before unjoining the site
					String roleId = SITE_SERV.getSite(id[i]).getUserRole(currentUserId).getId();

					SITE_SERV.unjoin(id[i]);
					if (i>0)
					{
						msg=msg+", ";
					}
					msg = msg+SITE_SERV.getSite(id[i]).getTitle();

					String[] userAuditString = {id[i],currentUserEid,roleId,UserAuditService.USER_AUDIT_ACTION_REMOVE,userAuditRegistration.getDatabaseSourceKey(),currentUserEid};
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
	 * toggle the sort ascending vs descending property in main view
	 * @param data
	 */
	public void doToggle_sort(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		if (state.getAttribute(SORT_ASC) != null)
		{
			state.setAttribute(SORT_ASC, !((Boolean) state.getAttribute(SORT_ASC)));
		}
	} // doToggle_sort
}
