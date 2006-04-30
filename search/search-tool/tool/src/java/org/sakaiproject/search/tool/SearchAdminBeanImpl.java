/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
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

package org.sakaiproject.search.tool;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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

	private static final Object COMMAND_REFRESHINSTANCE = "?" + COMMAND + "="
			+ REFRESHINSTNACE;

	private static final String REFRESHSTATUS = "refreshstatus";

	private static final Object COMMAND_REFRESHSTATUS = "?" + COMMAND + "="
			+ REFRESHSTATUS;

	private SearchService searchService = null;

	private SiteService siteService = null;

	private String internCommand = null;

	private String siteId;

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
		String siteCheck = currentSite.getReference();
		if (!siteService.allowUpdateSite(siteId))
		{
			String userName = sessionManager.getCurrentSessionUserId();
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

	private void doCommand()
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
		internCommand = null;

	}

	/**
	 * Refresh the status of the search engine index, does nothing
	 */
	private void doRefreshStatus()
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Refresh all the documents in the index
	 */
	private void doRefreshInstance()
	{
		searchService.refreshInstance();
	}

	/**
	 * Rebuild the index from scratch, this dumps the existing index and reloads
	 * all entities from the EntityContentProviders
	 */
	private void doRebuildInstance()
	{
		searchService.rebuildInstance();
	}

	/**
	 * Refresh just this suite
	 */
	private void doRefreshSite()
	{
		searchService.refreshSite(siteId);
	}

	/**
	 * rebuild just this site
	 */
	private void doRebuildSite()
	{
		searchService.rebuildSite(siteId);
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
	 */
	public String getIndexStatus(String statusFormat)
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
				COMMAND_REBUILDSITE, "Rebuild Site Index" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSITE, "Refresh Site Index" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REBUILDINSTANCE, "Rebuild Whole Index" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHINSTANCE, "Refresh Whole Index" }));
		sb.append(MessageFormat.format(adminOptionsFormat, new Object[] {
				COMMAND_REFRESHSTATUS, "Refresh Status" }));
		return sb.toString();
	}

}
