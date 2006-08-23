/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.tool;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

/**
 * @author ieb
 */
public class SearchAdminBeanImpl implements SearchAdminBean
{

	private static final String COMMAND = "command";

	private static final String REBUILDSITE = "rebuildsite";

	private static final Object COMMAND_REBUILDSITE = "?" + COMMAND + "="
			+ REBUILDSITE;

	private static final String REFRESHSITE = "refreshsite";

	private static final Object COMMAND_REFRESHSITE = "?" + COMMAND + "="
			+ REFRESHSITE;

	private static final String REBUILDINSTANCE = "rebuildinstance";

	private static final Object COMMAND_REBUILDINSTANCE = "?" + COMMAND + "="
			+ REBUILDINSTANCE;

	private static final String REFRESHINSTNACE = "refreshinstance";

	private static final String COMMAND_REFRESHINSTANCE = "?" + COMMAND + "="
			+ REFRESHINSTNACE;

	private static final String REFRESHSTATUS = "refreshstatus";

	private static final String COMMAND_REFRESHSTATUS = "?" + COMMAND + "="
			+ REFRESHSTATUS;

	private static final String REMOVELOCK = "removelock";

	private static final String COMMAND_REMOVELOCK = "?" + COMMAND + "="
	+ REMOVELOCK;

	private static final String RELOADINDEX = "reloadindex";

	private static final String COMMAND_RELOADINDEX = "?" + COMMAND + "="
	+ RELOADINDEX;

	private SearchService searchService = null;

	private SiteService siteService = null;

	private String internCommand = null;

	private String siteId;

	private String commandFeedback = "";

	private boolean superUser = false;
	
	private String userName = null;

	private String siteCheck = null;

	/**
	 * Construct a SearchAdminBean, checking permissions first
	 * 
	 * @param request
	 * @param searchService
	 * @param siteService
	 * @param portalService
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public SearchAdminBeanImpl(HttpServletRequest request,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager, SessionManager sessionManager)
			throws IdUnusedException, PermissionException
	{
		siteId = toolManager.getCurrentPlacement().getContext();
		Site currentSite = siteService.getSite(siteId);
		siteCheck = currentSite.getReference();
		userName = sessionManager.getCurrentSessionUserId();
		superUser = SecurityService.isSuperUser();
		boolean allow = ( superUser ) || ( "true".equals(ServerConfigurationService.getString("search.alow.maintain.admin","false")) &&
						siteService.allowUpdateSite(siteId));
		if ( !allow )
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		this.searchService = searchService;
		this.siteService = siteService;

		// process any commands
		String command = request.getParameter(COMMAND);
		if (command != null)
		{
			internCommand = command.intern();
		}

	}
	
	

	private void doCommand() throws PermissionException
	{
		if (internCommand == null) return;
		if (internCommand == REBUILDSITE)
		{
			doRebuildSite();
		}
		else if (internCommand == REFRESHSITE)
		{
			doRefreshSite();
		}
		else if (internCommand == REBUILDINSTANCE)
		{
			doRebuildInstance();
		}
		else if (internCommand == REFRESHINSTNACE)
		{
			doRefreshInstance();
		}
		else if (internCommand == REFRESHSTATUS)
		{
			doRefreshStatus();

		}
		else if (internCommand == REMOVELOCK)
		{
			doRemoveLock();

		}
		else if ( internCommand == RELOADINDEX ) {
			doReloadIndex();
		}
		internCommand = null;

	}

	private void doReloadIndex()
	{
		searchService.forceReload();
		searchService.reload();
		commandFeedback = "Reloaded Index";
	}



	private void doRemoveLock()
	{
		if ( !searchService.removeWorkerLock() ) {
			commandFeedback  = "Failed to remove worker lock";
		}
		
	}

	/**
	 * Refresh the status of the search engine index, does nothing
	 */
	private void doRefreshStatus()
	{
		commandFeedback = "Ok";

	}

	/**
	 * Refresh all the documents in the index
	 * @throws PermissionException 
	 */
	private void doRefreshInstance() throws PermissionException
	{
		if (!superUser)
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		searchService.refreshInstance();
		commandFeedback = "Ok";

	}

	/**
	 * Rebuild the index from scratch, this dumps the existing index and reloads
	 * all entities from the EntityContentProviders
	 * @throws PermissionException 
	 */
	private void doRebuildInstance() throws PermissionException
	{
		if (!superUser)
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		searchService.rebuildInstance();
		commandFeedback = "Ok";

	}

	/**
	 * Refresh just this suite
	 */
	private void doRefreshSite()
	{
		searchService.refreshSite(siteId);
		commandFeedback = "Ok";

	}

	/**
	 * rebuild just this site
	 */
	private void doRebuildSite()
	{
		searchService.rebuildSite(siteId);
		commandFeedback = "Ok";

	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle()
	{
		return "Search Administration";
	}

	/**
	 * {@inheritDoc}
	 * @throws PermissionException 
	 */
	public String getIndexStatus(String statusFormat) throws PermissionException
	{
		doCommand();
		SearchStatus ss = searchService.getSearchStatus();
		
			
		return MessageFormat.format(statusFormat,
				new Object[] {
				ss.getLastLoad(),
				ss.getLoadTime(),
				ss.getCurrentWorker(),
				ss.getCurrentWorkerETC(),
				ss.getNDocuments(),
				ss.getPDocuments()
		});
	}

	public String getWorkers(String rowFormat) {
		SearchStatus ss = searchService.getSearchStatus();
		StringBuffer sb = new StringBuffer();
		List l = ss.getWorkerNodes();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			Object[] worker = (Object[]) i.next();
			sb.append(MessageFormat.format(rowFormat,
					worker));
		}
		return sb.toString();
	}

	public String getIndexDocuments( String rowFormat ) {
		StringBuffer sb = new StringBuffer();
		List l = searchService.getAllSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					sbi.getName(), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();
	}
	public String getGlobalMasterDocuments( String rowFormat ) {
		StringBuffer sb = new StringBuffer();
		List l = searchService.getGlobalMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					sbi.getName(), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();		
	}
	
	public String getSiteMasterDocuments( String rowFormat ) {
		StringBuffer sb = new StringBuffer();
		List l = searchService.getSiteMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					sbi.getName(), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAdminOptions(String adminOptionsFormat)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSTATUS, "Refresh Status","" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REBUILDSITE, "Rebuild Site Index","" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSITE, "Refresh Site Index","" }));
		if (superUser)
		{
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REBUILDINSTANCE, "Rebuild Whole Index","" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHINSTANCE, "Refresh Whole Index","" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REMOVELOCK, "Remove Lock", 
				"onClick=\"return confirm('Are you sure you want to remove the lock\\n Check there are no indexers running');\"" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
					COMMAND_RELOADINDEX, "Reload Index","" }));
		}
		return sb.toString();
	}
	
	public String getCommandFeedback() {
		return commandFeedback;
	}



	public String getSegmentInfo(String rowFormat)
	{
		List segmentInfo = searchService.getSegmentInfo();
		StringBuffer sb = new StringBuffer();
		for ( Iterator i = segmentInfo.iterator(); i.hasNext(); ) {
			sb.append(MessageFormat.format(rowFormat, (Object[]) i.next() ));
		}
		return sb.toString();
	}

}
