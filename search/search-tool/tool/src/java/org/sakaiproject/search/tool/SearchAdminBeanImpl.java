/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.search.tool;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.api.SearchStatus;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.tool.api.SearchAdminBean;
import org.sakaiproject.search.tool.model.AdminOption;
import org.sakaiproject.search.tool.model.MasterRecord;
import org.sakaiproject.search.tool.model.Segment;
import org.sakaiproject.search.tool.model.WorkerThread;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.FormattedText;

/**
 * @author ieb
 */
public class SearchAdminBeanImpl implements SearchAdminBean
{

	private static final String COMMAND = "command";

	private static final String INDEX_BUILDER_NAME = "indexbuildername";

	private static final String REBUILDSITE = "rebuildsite";

	private static final String COMMAND_REBUILDSITE = "?" + COMMAND + "="
			+ REBUILDSITE;

	private static final String REFRESHSITE = "refreshsite";

	private static final String COMMAND_REFRESHSITE = "?" + COMMAND + "="
			+ REFRESHSITE;

	private static final String REBUILDINSTANCE = "rebuildinstance";

	private static final String COMMAND_REBUILDINSTANCE = "?" + COMMAND + "="
			+ REBUILDINSTANCE;

	private static final String REFRESHINSTANCE = "refreshinstance";

	private static final String COMMAND_REFRESHINSTANCE = "?" + COMMAND + "="
			+ REFRESHINSTANCE;

	private static final String REFRESHSTATUS = "refreshstatus";

	private static final String COMMAND_REFRESHSTATUS = "?" + COMMAND + "="
			+ REFRESHSTATUS;

	private static final String REMOVELOCK = "removelock";

	private static final String COMMAND_REMOVELOCK = "?" + COMMAND + "="
	+ REMOVELOCK;

	private static final String RELOADINDEX = "reloadindex";

	private static final String COMMAND_RELOADINDEX = "?" + COMMAND + "="
	+ RELOADINDEX;

	private static final String DISABLEDIAGNOSTICS = "disablediag";
	
	private static final String COMMAND_DISABLEDIAGNOSTICS = "?" + COMMAND + "="
	+ DISABLEDIAGNOSTICS;

	private static final String ENABLEDIAGNOSTICS = "enablediag";
	
	private static final String COMMAND_ENABLEDIAGNOSTICS = "?" + COMMAND + "="
	+ ENABLEDIAGNOSTICS;

	
	
	private ServerConfigurationService serverConfigurationService;
	
	private SearchService searchService = null;

	private String internCommand = null;

	private String siteId;

	private String commandFeedback = "";

	private boolean superUser = false;
	
	private String userName = null;

	private String siteCheck = null;

	private boolean redirect = false;

	/**
	 * Construct a SearchAdminBean, checking permissions first
	 * 
	 * @param request
	 * @param searchService
	 * @param siteService
	 * @param toolManager
	 * @param sessionManager
	 * @param securityService
	 * @param serverConfigurationService
	 * @throws IdUnusedException
	 * @throws PermissionException
	 */
	public SearchAdminBeanImpl(HttpServletRequest request,
			SearchService searchService, SiteService siteService,
			ToolManager toolManager, SessionManager sessionManager, SecurityService securityService, ServerConfigurationService serverConfigurationService)
			throws IdUnusedException, PermissionException
	{
		this.serverConfigurationService = serverConfigurationService;
		siteId = toolManager.getCurrentPlacement().getContext();
		Site currentSite = siteService.getSite(siteId);
		siteCheck = currentSite.getReference();
		userName = sessionManager.getCurrentSessionUserId();
		superUser = securityService.isSuperUser();
		boolean allow = ( superUser ) || ( "true".equals(serverConfigurationService.getString("search.allow.maintain.admin","false")) &&
						siteService.allowUpdateSite(siteId));
		if ( !allow )
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		this.searchService = searchService;

		// process any commands
		String command = request.getParameter(COMMAND);
		if (command != null)
		{
			internCommand = command.intern();
		}
		String indexBuilderName = request.getParameter(INDEX_BUILDER_NAME);
		doCommand(indexBuilderName);
		internCommand = null;
	}
	
	

	private void doCommand(String indexBuilderName) throws PermissionException
	{
		if (internCommand == null) return;
		if (internCommand == REBUILDSITE)
		{
			doRebuildSite();
			redirect = true;
		}
		else if (internCommand == REFRESHSITE)
		{
			doRefreshSite();
			redirect = true;
		}
		else if (internCommand == REBUILDINSTANCE)
		{
			doRebuildInstance(indexBuilderName);
			redirect = true;
		}
		else if (internCommand == REFRESHINSTANCE)
		{
			doRefreshInstance(indexBuilderName);
			redirect = true;
		}
		else if (internCommand == REFRESHSTATUS)
		{
			doRefreshStatus();
			redirect = true;

		}
		else if (internCommand == REMOVELOCK)
		{
			doRemoveLock();
			redirect = true;

		}
		else if ( internCommand == RELOADINDEX ) {
			doReloadIndex();
			redirect = true;
		} else if ( internCommand == DISABLEDIAGNOSTICS ) {
			searchService.disableDiagnostics();
			redirect = true;
		} else if ( internCommand == ENABLEDIAGNOSTICS ) {
			searchService.enableDiagnostics();
			redirect = true;
		}
		
		internCommand = null;

	}

	private void doReloadIndex()
	{
		searchService.forceReload();
		searchService.reload();
		commandFeedback = Messages.getString("searchadmin_reloadindex");
	}



	private void doRemoveLock()
	{
		if ( !searchService.removeWorkerLock() ) {
			commandFeedback  = Messages.getString("searchadmin_failedremovewl");
		}
		
	}

	/**
	 * Refresh the status of the search engine index, does nothing
	 */
	private void doRefreshStatus()
	{
		commandFeedback = Messages.getString("searchadmin_statok");

	}

	/**
	 * Refresh all the documents in the index
	 * @throws PermissionException 
	 */
	private void doRefreshInstance(String indexBuilderName) throws PermissionException
	{
		if (!superUser)
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		searchService.refreshIndex(indexBuilderName);
		commandFeedback = Messages.getString("searchadmin_statok");;

	}

	/**
	 * Rebuild the index from scratch, this dumps the existing index and reloads
	 * all entities from the EntityContentProviders
	 * @throws PermissionException 
	 */
	private void doRebuildInstance(String indexBuilderName) throws PermissionException
	{
		if (!superUser)
		{
			throw new PermissionException(userName, "site.update", siteCheck);
		}
		searchService.rebuildIndex(indexBuilderName);
		commandFeedback = Messages.getString("searchadmin_statok");;

	}

	/**
	 * Refresh just this suite
	 */
	private void doRefreshSite()
	{
		searchService.refreshSite(siteId);
		commandFeedback = Messages.getString("searchadmin_statok");;

	}

	/**
	 * rebuild just this site
	 */
	private void doRebuildSite()
	{
		searchService.rebuildSite(siteId);
		commandFeedback = Messages.getString("searchadmin_statok");;

	}

	/**
	 * {@inheritDoc}
	 */
	public String getTitle()
	{
		return Messages.getString("searchadmin_title");
	}

	/**
	 * {@inheritDoc}
	 * @throws PermissionException 
	 */
	public String getIndexStatus(String statusFormat) throws PermissionException
	{
		final List<SearchStatus> searchStatus = searchService.getSearchStatus();

		final String allStatusMessages = searchStatus
				.stream()
				.map(ss -> formatSearchStatus(ss, statusFormat))
				.collect(Collectors.joining("<br>"));
		return allStatusMessages;
	}

	private String formatSearchStatus(SearchStatus ss, String format) {
		return MessageFormat.format(format,
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

		final List<SearchStatus> searchStatus = searchService.getSearchStatus();

		final String allWorkerMessages = searchStatus
				.stream()
				.map(ss -> formatSearchStatusWorkerNodes(ss, rowFormat))
				.collect(Collectors.joining("<br>"));
		return allWorkerMessages;
	}

	private String formatSearchStatusWorkerNodes(SearchStatus searchStatus, String format) {
		return (String) searchStatus
				.getWorkerNodes()
				.stream()
				.map(wn -> MessageFormat.format(format, wn)).collect(Collectors.joining(""));
	}

	public String getIndexDocuments( String rowFormat ) {
		StringBuilder sb = new StringBuilder();
		List l = searchService.getAllSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					FormattedText.escapeHtml(sbi.getName(),false), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();
	}
	public String getGlobalMasterDocuments( String rowFormat ) {
		StringBuilder sb = new StringBuilder();
		List l = searchService.getGlobalMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					FormattedText.escapeHtml(sbi.getName(),false), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();		
	}
	
	public String getSiteMasterDocuments( String rowFormat ) {
		StringBuilder sb = new StringBuilder();
		List l = searchService.getSiteMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			sb.append(MessageFormat.format(rowFormat,
					new Object[] {
					sbi.getId(), 
					FormattedText.escapeHtml(sbi.getName(),false), 
					sbi.getContext(), 
					SearchBuilderItem.actions[sbi.getSearchaction().intValue()], 
					sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate().intValue()], 
					sbi.getVersion() }));
		}
		return sb.toString();		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAdminOptions(String adminOptionsFormat)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSTATUS, Messages.getString("searchadmin_cmd_refreshstat"),"" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REBUILDSITE, Messages.getString("searchadmin_cmd_rebuildsiteind"),"" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSITE, Messages.getString("searchadmin_cmd_refreshsiteind"),"" }));
		if (superUser)
		{
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REBUILDINSTANCE, Messages.getString("searchadmin_cmd_rebuildind"),"" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHINSTANCE, Messages.getString("searchadmin_cmd_refreshind"),"" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REMOVELOCK, Messages.getString("searchadmin_cmd_removelock"), 
				"onclick=\"return confirm('"+Messages.getString("searchadmin_cmd_removelockconfirm")+"');\"" }));
			sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
					COMMAND_RELOADINDEX, Messages.getString("searchadmin_cmd_reloadind"),"" }));
			if ( searchService.hasDiagnostics() ) {
				sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
						COMMAND_DISABLEDIAGNOSTICS, Messages.getString("searchadmin_cmd_disablediagnostics"),""} ));
			} else {
				sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
						COMMAND_ENABLEDIAGNOSTICS, Messages.getString("searchadmin_cmd_enablediagnostics"),"" }));
				
			}
		}
		return sb.toString();
	}
	
	public String getCommandFeedback() {
		return commandFeedback;
	}



	public String getSegmentInfo(String rowFormat)
	{
		List segmentInfo = searchService.getSegmentInfo();
		StringBuilder sb = new StringBuilder();
		for ( Iterator i = segmentInfo.iterator(); i.hasNext(); ) {
			sb.append(MessageFormat.format(rowFormat, (Object[]) i.next() ));
		}
		return sb.toString();
	}




	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.SearchAdminBean#getGlobalMasterRecords()
	 */
	public List<MasterRecord> getGlobalMasterRecords()
	{
		List<MasterRecord> masters = new ArrayList<MasterRecord>();
		List l = searchService.getGlobalMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			
			final SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			masters.add(new MasterRecord() {

				public String getContext()
				{
					return sbi.getContext();
				}

				public String getLastUpdate()
				{
					return String.valueOf(sbi.getVersion());
				}

				public String getOperation()
				{
					return SearchBuilderItem.actions[sbi.getSearchaction().intValue()];
				}

				public String getStatus()
				{
					return sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate().intValue()];
				}
				
			});
		}
		return masters;		
	}

    @Override
    public Set<String> getIndexBuilderNames() {
        return searchService.getIndexBuilderNames();
    }

    @Override
    public List<AdminOption> getDefaultOptions() {
		List<AdminOption> o  = new ArrayList<AdminOption>();
		o.add(new AdminOptionImpl(COMMAND_REBUILDSITE, Messages.getString("searchadmin_cmd_rebuildsiteind"),"" ));
		o.add(new AdminOptionImpl(COMMAND_REFRESHSITE, Messages.getString("searchadmin_cmd_refreshsiteind"),"" ));
		return o;
	}

    @Override
    public List<AdminOption> getIndexSpecificOptions() {
		List<AdminOption> o  = new ArrayList<AdminOption>();
		if (superUser) {
			o.add(new AdminOptionImpl(REBUILDINSTANCE, Messages.getString("searchadmin_cmd_rebuildind"),"" ));
			o.add(new AdminOptionImpl(REFRESHINSTANCE, Messages.getString("searchadmin_cmd_refreshind"),"" ));
		}
		return o;
    }


	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.SearchAdminBean#getSegments()
	 */
	public List<Segment> getSegments()
	{
		List<Segment> segments = new ArrayList<Segment>();
		Object[] segmentInfo = searchService.getSegmentInfo().toArray();
		for ( Object ra : segmentInfo ) {
			// name, size, lastup
			final Object[] r  =(Object[]) ra;
			segments.add(new Segment() {

				public String getLastUpdate()
				{
					
					return String.valueOf(r[2]);
				}

				public String getName()
				{
					return String.valueOf(r[0]);
				}

				public String getSize()
				{
					return String.valueOf(r[1]);
				}
				
			});
		}
		return segments;
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.SearchAdminBean#getSiteMasterRecords()
	 */
	public List<MasterRecord> getSiteMasterRecords()
	{
		List<MasterRecord> masters = new ArrayList<MasterRecord>();
		List l = searchService.getSiteMasterSearchItems();
		for ( Iterator i = l.iterator(); i.hasNext(); ) {
			
			final SearchBuilderItem sbi = (SearchBuilderItem) i.next();
			masters.add(new MasterRecord() {

				public String getContext()
				{
					return sbi.getContext();
				}

				public String getLastUpdate()
				{
					return String.valueOf(sbi.getVersion());
				}

				public String getOperation()
				{
					
					return SearchBuilderItem.actions[sbi.getSearchaction().intValue()];
				}

				public String getStatus()
				{
					return sbi.isLocked()?"Locked to "+sbi.getLock():SearchBuilderItem.states[sbi.getSearchstate().intValue()];
				}
				
			});
		}
		return masters;		
	}



	
	public class AdminOptionImpl implements AdminOption {

		private String attributes;
		private String name;
		private String url;
		
		public AdminOptionImpl(String uri, String name, String attributes) {
			this.attributes = attributes;
			this.name = name;
			this.url = uri;
			
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.search.tool.AdminOption#getAttributes()
		 */
		public String getAttributes()
		{
			return attributes;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.search.tool.AdminOption#getName()
		 */
		public String getName()
		{
			return name;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.search.tool.AdminOption#getUrl()
		 */
		public String getUrl()
		{
			return url;
		}
		
	}




	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.SearchAdminBean#getSearchStatus()
	 */
	public List<SearchStatus> getSearchStatus()
	{
		return searchService.getSearchStatus();
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.search.tool.SearchAdminBean#getWorkerThreads()
	 */
	public List<WorkerThread> getWorkerThreads()
	{
		final List<WorkerThread> workerThreads = (List<WorkerThread>)searchService
				.getSearchStatus()
				.stream()
				.flatMap(ss -> ss.getWorkerNodes().stream())
				.map(w -> new WorkerThread() {
					Object[] wa = (Object[]) w;

					public String getEta() {
						return FormattedText.escapeHtml(String.valueOf(wa[1]), false);
					}

					public String getName() {
						return FormattedText.escapeHtml(String.valueOf(wa[0]), false);
					}

					public String getStatus() {
						return FormattedText.escapeHtml(String.valueOf(wa[2]), false);
					}
				})
				.collect(Collectors.toList());
		return workerThreads;
	}



	/**
	 * @return the redirect
	 */
	public boolean isRedirectRequired()
	{
		return redirect;
	}

	public boolean isEnabled()
	{
		return ("true".equals(serverConfigurationService.getString("search.enable",
				"false")));

	}
}
