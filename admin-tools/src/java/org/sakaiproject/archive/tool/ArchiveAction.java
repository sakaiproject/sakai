/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.archive.tool;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.archive.tool.model.SparseFile;
import org.sakaiproject.archive.tool.model.SparseSite;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.*;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ResourceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

/**
* <p>ArchiveAction is the Sakai archive tool.</p>
*/
@Slf4j
public class ArchiveAction
	extends VelocityPortletPaneledAction
{

	private static final long serialVersionUID = 1L;
	private static final String STATE_MODE = "mode";
	private static final String BATCH_MODE = "batch";
	private static final String BATCH_ARCHIVE_CONFIRM_MODE = "batch-archive-confirm";
	private static final String SINGLE_MODE = "single";
	private static final String DOWNLOAD_MODE = "download";

	
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("archive");
	
	private CourseManagementService courseManagementService;
	private SiteService siteService;
	private SessionManager sessionManager;
	private UserDirectoryService userDirectoryService;
	private AuthzGroupService authzGroupService;
	private ServerConfigurationService serverConfigurationService;
	private ArchiveService archiveService;
	private SecurityService securityService;
	private IdManager idManager;
	
	// for batch archive
    private long batchArchiveStarted = 0;
    private long maxJobTime;
    private static int PAUSE_TIME_MS = 1000*10; // 5 seconds
    private static int MAX_JOB_TIME_DEFAULT = 1000*60*30; // 30 minutes
    private static int NUM_SITES_PER_BATCH = 10;
    private static final String STATUS_COMPLETE = "COMPLETE";
    private String batchArchiveStatus = null;
    private String batchArchiveMessage = null;

	public ArchiveAction() {
		super();
		courseManagementService = ComponentManager.get(CourseManagementService.class);
		siteService = ComponentManager.get(SiteService.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class);
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
		archiveService = ComponentManager.get(ArchiveService.class);
		securityService = ComponentManager.get(SecurityService.class);
		idManager = ComponentManager.get(IdManager.class);
	}
	
	/**
	 * override init so we can lookup our dependencies
	 */
	protected void initState(SessionState state, HttpServletRequest req, HttpServletResponse res) {
		super.initState(state, req, res);

		// SAK-28087 configurable value for max job time. A large term at a large institution may take 24 hours
		maxJobTime = Long.valueOf(serverConfigurationService.getInt("archive.max.job.time", MAX_JOB_TIME_DEFAULT));
		
		state.setAttribute(STATE_MODE, SINGLE_MODE);
	}

   
	/**
	* build the context
	*/
    public String buildMainPanelContext(VelocityPortlet portlet, 
			Context context,
			RunData rundata,
			SessionState state)
	{
		String template = null;

		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			context.put("tlang",rb);
			return (String) getContext(rundata).get("template") + "_noaccess";
		}
		
		// check mode and dispatch
		String mode = (String) state.getAttribute(STATE_MODE);
		
		if (StringUtils.equals(mode, SINGLE_MODE))
		{
			template = buildSingleModeContext(portlet, context, rundata, state);
		}
		else if (StringUtils.equals(mode, BATCH_MODE))
		{
			template = buildBatchModeContext(portlet, context, rundata, state);
		}
		else if (StringUtils.equals(mode, BATCH_ARCHIVE_CONFIRM_MODE))
		{
			template = buildBatchModeArchiveConfirmContext(portlet, context, rundata, state);
		}
		else if (StringUtils.equals(mode, DOWNLOAD_MODE))
		{
			template = buildDownloadContext(portlet, context, rundata, state);
		}
		
		return (String)getContext(rundata).get("template") + template;
		
	}	// buildMainPanelContext
    
    /**
	* build the context for single mode import/export
	*/
	public String buildSingleModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)									
	{
		context.put("tlang",rb);
		buildMenu(context);
		
		return "";

	}

	/**
	* build the context for batch import/export
	*/
	public String buildBatchModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang",rb);
		buildMenu(context);
		
		//check if we are already running. Template will render just the message if so
		String statusMessage = getCurrentBatchArchiveStatusMessage();
		if(StringUtils.isNotBlank(statusMessage)) {
			context.put("isRunning", true);
			context.put("statusMessage", statusMessage);
		}
		
		//get list of terms
		List<AcademicSession> terms = courseManagementService.getAcademicSessions();
		context.put("terms", terms);
		
		return "-batch";
	}
	
	/**
	* build the context for batch archive confirm
	*/
	public String buildBatchModeArchiveConfirmContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang",rb);
		
		//go to template
		return "-batch-archive-confirm";
	}
	
	/**
	* build the context for batch archive confirm
	*/
	public String buildDownloadContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang",rb);
		buildMenu(context);
		
		//get list of existing archives
		Collection<File> files = Collections.<File>emptySet();
		Path sakaiHome = Paths.get(serverConfigurationService.getSakaiHomePath());
		// Either relative to sakai.home or absolute
		Path archivePath = sakaiHome.resolve(serverConfigurationService.getString("archive.storage.path", "archive"));
		File archiveBaseDir = archivePath.toFile();

		if (archiveBaseDir.exists() && archiveBaseDir.isDirectory()) {
			files = FileUtils.listFiles(archiveBaseDir, new SuffixFileFilter(".zip"), null);
		}
		
		List<SparseFile> zips = new ArrayList<SparseFile>();
		
		SimpleDateFormat dateFormatIn = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat dateFormatOut = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar calendar = Calendar.getInstance();
		
		//porcess the list. also get the hash for the file if it exists 
		for(File f: files) {
			
			String absolutePath = f.getAbsolutePath();
			
			SparseFile sf = new SparseFile();
			sf.setFilename(f.getName());
			sf.setAbsolutePath(absolutePath);
			sf.setSize(FileUtils.byteCountToDisplaySize(f.length()));
			
			//get the datetime string, its the last part of the file name, convert back to a date that we can display
			String dateTimeStr = StringUtils.substringAfterLast(StringUtils.removeEnd(f.getName(), ".zip"), "-");
			
			try {
				Date date = dateFormatIn.parse(dateTimeStr);
				sf.setDateCreated(dateFormatOut.format(date));
			} catch (ParseException pe) {
				//ignore, just don't set the date
			}
			
			//get siteId, first part of name
			String siteId = StringUtils.substringBeforeLast(f.getName(), "-");
			sf.setSiteId(siteId);
			
			//try to get site title if the site still exists
			try {
				Site site = siteService.getSite(siteId);
				sf.setSiteTitle(site.getTitle());
			} catch (IdUnusedException e) {
				//ignore, no site available
			}

			//get the hash. need to read it from the file. Same filename but diff extension
			String hashFilePath = StringUtils.removeEnd(absolutePath, ".zip");
			hashFilePath = hashFilePath + ".sha1";
			
			File hashFile = new File(hashFilePath);
			try {
				String hash = FileUtils.readFileToString(hashFile);
				sf.setHash(hash);
			} catch (IOException e) {
				//ignore, dont use the hash
			}
			
			zips.add(sf);
		}
		
		context.put("archives", zips);		

		return "-download";
	}
	
	
	/**
	* doArchive called when "eventSubmit_doArchive" is in the request parameters
	* to run the archive.
	*/
	public void doArchive(RunData data, Context context) throws IOException {
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!securityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.limited"));
			return;
		}

		String id = data.getParameters().getString("archive-id");
		boolean zip = data.getParameters().getBoolean("zip-id");
		if (StringUtils.isNotBlank(id))
		{
			String msg;
			if(zip) {
				msg = archiveService.archiveAndZip(id.trim());
			} else {
				msg = archiveService.archive(id.trim());
			}
			addAlert(state, rb.getFormattedMessage("archive", new Object[]{id}) + " \n " + msg);
		}
		else
		{
			addAlert(state, rb.getString("archive.please"));
		}

	}	// doArchive

	/**
	* doImport called when "eventSubmit_doImport" is in the request parameters
	* to run an import.
	*/
	public void doImport(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!securityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.import"));
			return;
		}

		String id = data.getParameters().getString("import-id");
		String file = data.getParameters().getString("import-file");
		if (	(id != null) && (id.trim().length() > 0)
			&&	(file != null) && (file.trim().length() > 0))
		{
			String msg = archiveService.merge(file.trim(), id.trim(), null);
			addAlert(state, rb.getFormattedMessage("archive.import2", new Object[]{file, id}) + msg);
		}
		else
		{
			addAlert(state, rb.getString("archive.file"));
		}

	}	// doImport
	
	/**
	* doImport called when "eventSubmit_doBatch_Import" is in the request parameters
	* to run an import.
	*/
	public void doBatch_Import(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		Hashtable fTable = new Hashtable();
		
		if (!securityService.isSuperUser())
		{
			addAlert(state, rb.getString("archive.batch.auth"));
			return;
		}
		
		FileItem fi = data.getParameters().getFileItem ("importFile");
		if (fi == null)
		{
			addAlert(state, rb.getString("archive.batch.missingname"));
		}
		else
		{
			// get content
			try (BufferedReader stringReader = new BufferedReader(new InputStreamReader(fi.getInputStream()))) {

				stringReader.lines().forEach(line -> {
					String[] lineContents = line.split("\t");
					if (lineContents.length == 2) {
						fTable.put(lineContents[0], lineContents[1]);
					} else {
						addAlert(state, rb.getString("archive.batch.wrongformat"));
					}
				});
			} catch (IOException e) {
				log.warn("Failed to close stream.", e);
			}
		}
		
		if (!fTable.isEmpty())
		{
			Enumeration importFileName = fTable.keys();
			int count = 1;
			while (importFileName.hasMoreElements())
			{
				String path = StringUtils.trimToNull((String) importFileName.nextElement());
				String siteCreatorName = StringUtils.trimToNull((String) fTable.get(path));
				if (path != null && siteCreatorName != null)
				{
					String nSiteId = idManager.createUuid();
					
					try
					{
						Object[] params = new Object[]{count, path, nSiteId, siteCreatorName};
						addAlert(state, rb.getFormattedMessage("archive.import1", params));
						addAlert(state, archiveService.merge(path, nSiteId, siteCreatorName));
						
					}
					catch (Exception ignore)
					{
					}
				}
				
				count++;
			}
		}
	}
	
	/**
	* doImport called when "eventSubmit_doBatch_Archive_PreProcess" is in the request parameters
	* to do the prep work for archiving a bunch of sites that match the criteria
	*/
	public void doBatch_Archive_PreProcess(RunData data, Context context) {
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!securityService.isSuperUser()) {
			addAlert(state, rb.getString("archive.batch.auth"));
			return;
		}
		
		//if we have a selected term then use that as the batch export param
		String selectedTerm = data.getParameters().getString("archive-term");
		
		log.debug("selectedTerm: " + selectedTerm);
		
		if(StringUtils.isBlank(selectedTerm)) {
			addAlert(state, rb.getString("archive.batch.term.text.missingterm"));
			state.setAttribute(STATE_MODE, BATCH_MODE);
			return;
		}
		
		//set the message
		state.setAttribute("confirmString", rb.getFormattedMessage("archive.batch.term.text.confirm.1", new Object[]{selectedTerm}));
		
		//get the sites that match the criteria
		Map<String, String> propertyCriteria = new HashMap<String,String>();
		propertyCriteria.put("term_eid", selectedTerm);
		List<Site> sites = siteService.getSites(SelectionType.ANY, null, null, propertyCriteria, SortType.TITLE_ASC, null);		
				
		if(sites.isEmpty()) {
			addAlert(state, rb.getFormattedMessage("archive.batch.term.text.nosites", new Object[]{selectedTerm}));
			state.setAttribute(STATE_MODE, BATCH_MODE);
			return;
		}
		
		//convert to new list so that we dont load entire sites into context
		List<SparseSite> ssites = new ArrayList<SparseSite>();
		for(Site s: sites) {
			ssites.add(new SparseSite(s.getId(), s.getTitle()));
		}
		
		state.setAttribute("sites", ssites);
		
		//put into state for next pass
		state.setAttribute("selectedTerm", selectedTerm);
				
		//set mode so we go to next template
		state.setAttribute(STATE_MODE, BATCH_ARCHIVE_CONFIRM_MODE);
	}
	
	
	/**
	* doImport called when "eventSubmit_doBatch_Archive" is in the request parameters
	* to archive a bunch of sites that match the criteria
	* NOTE. This performs exactly as per a normal archive. Ie if you archive a site twice, you get two copies of the resources.
	* A change needs to be made to the archive service to clean out the archives before each run. 
	*/
	public void doBatch_Archive(RunData data, Context context) {
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		if (!securityService.isSuperUser()) {
			addAlert(state, rb.getString("archive.batch.auth"));
			return;
		}
		
		final String selectedTerm = (String)state.getAttribute("selectedTerm");
		final List<SparseSite> sites = (List<SparseSite>)state.getAttribute("sites");
		final Session currentSession = sessionManager.getCurrentSession(); //need to pass this into the new thread
		final User currentUser = userDirectoryService.getCurrentUser(); //need to pass this into the new thread
		
		//do the archive in a new thread
        Runnable backgroundRunner = new Runnable() {
            public void run() {
                try {
                    archiveSites(sites, selectedTerm, currentSession, currentUser);
                } catch (IllegalStateException e) {
                    throw e; 
                } catch (Exception e) {
                    log.error("Batch Archive background runner thread died: " + e, e);
                }
            }
        };
        Thread backgroundThread = new Thread(backgroundRunner);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
		
		state.setAttribute(STATE_MODE, BATCH_MODE);
	}
	
	
	/**
	* doImport called when "eventSubmit_doBatch_Archive_Cancel" is in the request parameters
	* to cancel the current process
	*/
	public void doBatch_Archive_Cancel(RunData data, Context context) {
		
		SessionState state = ((JetspeedRunData)data).getPortletSessionState(((JetspeedRunData)data).getJs_peid());

		state.removeAttribute("selectedTerm");
		state.removeAttribute("sites");
		state.removeAttribute("confirmString");
		
		state.setAttribute(STATE_MODE, BATCH_MODE);
	}
	
	/**
	 * Set the state so the main panel renderer knows what to do
	 * @param data RunData
	 */
	public void doView_single(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, SINGLE_MODE);
	}
	
	/**
	 * Set the state so the main panel renderer knows what to do
	 * @param data RunData
	 */
	public void doView_batch(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, BATCH_MODE);
	}
	
	/**
	 * Set the state so the main panel renderer knows what to do
	 * @param data RunData
	 */
	public void doView_download(RunData data){
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_MODE, DOWNLOAD_MODE);
	}

	/**
	 * Process that archives the sites
	 * @param sites list of SparseSite
	 * @throws InterruptedException
	 */
	private void archiveSites(List<SparseSite> sites, String selectedTerm, Session currentSession, User currentUser) throws InterruptedException {
		
		if (isLocked()) {
            throw new IllegalStateException("Cannot run batch archive, an archive job is already in progress");
        }
		batchArchiveStarted = System.currentTimeMillis();
		batchArchiveMessage = rb.getFormattedMessage("archive.batch.term.text.statusmessage.start", new Object[]{sites.size(), selectedTerm, 0});
		batchArchiveStatus = "RUNNING";
		
		log.info("Batch archive started for term: " + selectedTerm + ". Archiving " + sites.size() + " sites.");
		
        Session threadSession = sessionManager.getCurrentSession();
        if (threadSession == null) {
        	threadSession = sessionManager.startSession();
        }
        threadSession.setUserId(currentUser.getId());
        threadSession.setActive();
        sessionManager.setCurrentSession(threadSession);
        authzGroupService.refreshUser(currentUser.getId());
        
        //counters so we can run this in batches if we have a number of sites to process
        int archiveCount = 0;
        
        try {
        	
        	for(SparseSite s: sites) {
        		
        		log.info("Processing site: " + s.getTitle());
        		
        		//archive the site
        		archiveService.archive(s.getId());
        		
        		//compress it
        		//TODO check return value? do we care?
        		try {
        			archiveService.archiveAndZip(s.getId());
				} catch (IOException e) {
					log.error("Failed to archive and compress it the site with id {}", s.getId(), e);
				}
        		
        		archiveCount++;
        		
        		// update message
                if (archiveCount % 1 == 0) {
                    int percentComplete = (int) (archiveCount * 100) / sites.size();
            		batchArchiveMessage = rb.getFormattedMessage("archive.batch.term.text.statusmessage.update", new Object[]{sites.size(), selectedTerm, archiveCount, percentComplete});
                }
        		
        		// sleep if we need to and keep sessions alive
        		if (archiveCount > 0 && archiveCount % NUM_SITES_PER_BATCH == 0) {
                    log.info("Sleeping for " + PAUSE_TIME_MS + "ms");
                    Thread.sleep(PAUSE_TIME_MS);
                    threadSession.setActive();
                    currentSession.setActive();
                }
        		
        		//check timeout 
        		if (!isLocked()) {
        			throw new RuntimeException("Timeout occurred while running batch archive");
        		}
        		
        		
        		
        	}
        	
        	//complete
    		batchArchiveMessage = rb.getFormattedMessage("archive.batch.term.text.statusmessage.complete", new Object[]{sites.size(), selectedTerm});
    		
    		log.info("Batch archive complete.");
    		
        } finally {
        	// reset 
        	batchArchiveStatus = STATUS_COMPLETE;
        	batchArchiveStarted = 0;
        	threadSession.clear();
        	threadSession.invalidate();
        }
	
	}
	
	/**
	 * Check if a batch archive job is already running
	 * @return
	 */
	public synchronized boolean isLocked() {
        boolean locked = false;
        if (batchArchiveStarted > 0) {
            if (System.currentTimeMillis() > (batchArchiveStarted + maxJobTime)) {
                // max time reached for this update so reset
            	batchArchiveStarted = 0;
            	batchArchiveStatus = STATUS_COMPLETE;
            	batchArchiveMessage = "Max time exceeded for this batch archive. Aborting.";
            } else {
                locked = true;
            }
        }
        return locked;
    }
	
	/**
	 * Gets the message for the user if a batch archive job is running
	 * @return
	 */
	public synchronized String getCurrentBatchArchiveStatusMessage() {
        String message = batchArchiveMessage;
        isLocked(); //update in case we are done.
        if (StringUtils.equals(batchArchiveStatus, STATUS_COMPLETE)) {
        	batchArchiveStatus = null;
        	batchArchiveMessage = null;
        }
        return message;
    }
	
    
    /**
     * Build the top level menu
     * @param context
     */
    private void buildMenu(Context context) {
    	//build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("archive.button.single"), "doView_single"));
		bar.add(new MenuEntry(rb.getString("archive.button.batch"), "doView_batch"));
		bar.add(new MenuEntry(rb.getString("archive.button.download"), "doView_download"));

		context.put(Menu.CONTEXT_MENU, bar);
		context.put (Menu.CONTEXT_ACTION, "ArchiveAction");
    }
	
	
	
}	// ArchiveAction



