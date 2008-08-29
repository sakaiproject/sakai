package org.sakaiproject.sitestats.tool.bean;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.event.parser.EventParserTip;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class ReportsBean {
	private static final String				SORT_ID					= "id";
	private static final String				SORT_USER				= "user";
	private static final String				SORT_EVENT				= "event";
	private static final String				SORT_RESOURCE			= "resource";
	private static final String				SORT_ACTION				= "action";
	private static final String				SORT_DATE				= "date";
	private static final String				SORT_TOTAL				= "total";
	private static final int				PAGER_DEF_SIZE			= 20;

	/** Our log (commons). */
	private static Log						LOG						= LogFactory.getLog(ReportsBean.class);

	/** Resource bundle */
	private String							bundleName				= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader					msgs					= new ResourceLoader(bundleName);

	/** What: UI behavior vars */
	private boolean							selectedLimitedActivity	= false;
	private boolean							selectedLimitedAction	= false;
	private boolean							resourcesLoaded			= false;
	private String							resourcesLoadedForSite	= "";
	private List<SelectItem>				resourcesList			= new ArrayList<SelectItem>();
	/** When: UI behavior vars */
	/** Who: UI behavior vars */
	private List<SelectItem>				siteRoles;
	private String							siteRolesForSite		= "";
	private List<SelectItem>				siteGroups;
	private String							siteGroupsForSite		= "";
	private boolean							usersLoaded				= false;
	private String							usersLoadedForSite		= "";
	private List<SelectItem>				siteUsers				= new ArrayList<SelectItem>();

	/** Report related */
	private Report							report					= null;
	private ReportParams					reportParams			= null;																		// new
																																				// ReportParamsBean();
	/** Sorting */
	private boolean							sortAscending			= true;
	private String							sortColumn				= SORT_USER;
	/** Pager related */
	private int								pagerTotalItems			= -1;
	private int								pagerFirstItem			= 0;
	private int								pagerSize				= PAGER_DEF_SIZE;
	private boolean							pagerRendered			= true;

	/** Statistics Manager object */
	private transient ServiceBean			serviceBean				= null;

	/** Other */
	private String							previousSiteId			= "";
	private PrefsData						prefsdata				= null;
	private long							prefsLastModified		= 0;
	private boolean							showFatalMessage		= false;
	private String							message					= null;
	private Collator						collator				= Collator.getInstance();
	private final 							ReentrantLock lock 		= new ReentrantLock();	
	
	
	// ######################################################################################
	// ManagedBean property methods
	// ######################################################################################	
	public void setServiceBean(ServiceBean serviceBean){
		this.serviceBean = serviceBean;
	}	
	
	public void setReportParams(ReportParams reportParams) {
		this.reportParams = reportParams;
		if(!serviceBean.getSiteVisitsEnabled())
			this.reportParams.setWhat(ReportManager.WHAT_EVENTS_BYTOOL);
	}

	// ################################################################
	// Bean methods
	// ################################################################		
	public ReportsBean() {
	}
	
	private PrefsData getPrefsdata() {
		String siteId = serviceBean.getSiteId();
		if(prefsdata == null || prefsLastModified < serviceBean.getPreferencesLastModified() || !previousSiteId.equals(siteId)){
			previousSiteId = siteId;
			prefsdata = serviceBean.getSstStatsManager().getPreferences(siteId, false);
			prefsLastModified = serviceBean.getPreferencesLastModified();
		}
		return prefsdata;
	}
	
	public ReportParams getParams(){
		return reportParams;
	}
	
	public String getMessages(){
		if(showFatalMessage){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
			showFatalMessage = false;
			message = null;
		}
		return "";
	}
	
	// ######################################################################################
	// Bean methods: WHAT related 
	// ######################################################################################
	public List<SelectItem> getTools() {
		List<SelectItem> tools = new ArrayList<SelectItem>();
		
		List<ToolInfo> siteTools = serviceBean.getSstStatsManager().getSiteToolEventsDefinition(serviceBean.getSiteId(), getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Iterator<ToolInfo> i = siteTools.iterator();
		while(i.hasNext()){
			ToolInfo toolInfo = i.next();
			if(isToolSupported(toolInfo)) {
				tools.add(new SelectItem(toolInfo.getToolId(), toolInfo.getToolName()));
			}
		}
		return tools;
	}
	
	public List<SelectItemGroup> getEvents() {
		List<SelectItemGroup> tools = new ArrayList<SelectItemGroup>();
		
		List<ToolInfo> siteTools = serviceBean.getSstStatsManager().getSiteToolEventsDefinition(serviceBean.getSiteId(), getPrefsdata().isListToolEventsOnlyAvailableInSite());
		Iterator<ToolInfo> i = siteTools.iterator();
		while(i.hasNext()){
			ToolInfo toolInfo = i.next();
			if(isToolSupported(toolInfo)) {
				List<EventInfo> eventInfos = toolInfo.getEvents();
				SelectItem[] eventsSelectItem = new SelectItem[eventInfos.size()];
				int eCount = 0;
				Iterator<EventInfo> iE = eventInfos.iterator();
				while(iE.hasNext()){
					EventInfo e = iE.next();
					eventsSelectItem[eCount++] = new SelectItem(e.getEventId(), e.getEventName());
				}
				SelectItemGroup siGroup = new SelectItemGroup(toolInfo.getToolName(), toolInfo.getToolName(), true, eventsSelectItem);
				tools.add(siGroup);
			}
		}
		
		return tools;
	}
	
	public boolean getSelectedLimitedAction(){
		return selectedLimitedAction;
	}
	
	public void setSelectedLimitedAction(boolean selectedLimitedAction) {
		this.selectedLimitedAction = selectedLimitedAction;
		if(!selectedLimitedAction)
			reportParams.setWhatResourceAction(null);
	}
	
	public List<SelectItem> getResourceActions() {
		List<SelectItem> actions = new ArrayList<SelectItem>();
		actions.add(new SelectItem(ReportManager.WHAT_RESOURCES_ACTION_NEW, msgs.getString("action_new")));
		actions.add(new SelectItem(ReportManager.WHAT_RESOURCES_ACTION_READ, msgs.getString("action_read")));
		actions.add(new SelectItem(ReportManager.WHAT_RESOURCES_ACTION_REVS, msgs.getString("action_revise")));
		actions.add(new SelectItem(ReportManager.WHAT_RESOURCES_ACTION_DEL, msgs.getString("action_delete")));
		return actions;
	}
	
	public boolean getSelectedLimitedActivity(){
		if(selectedLimitedActivity)
			processLoadResources(null);
		return selectedLimitedActivity;
	}
	
	public void setSelectedLimitedActivity(boolean selectedLimitedActivity) {
		this.selectedLimitedActivity = selectedLimitedActivity;
		if(!selectedLimitedActivity)
			reportParams.setWhatResourceIds(null);
	}
	
	public List<SelectItem> getResources() {
		String siteId = serviceBean.getSiteId();
		if(!resourcesLoaded || !resourcesLoadedForSite.equals(siteId))
			return new ArrayList<SelectItem>();
		else
			return resourcesList;
	}
	
	public boolean isResourcesLoaded() {
		return resourcesLoaded;
	}
	
	public void processLoadResources(ActionEvent e) {
		String siteId = serviceBean.getSiteId();
		if(!resourcesLoaded || !resourcesLoadedForSite.equals(siteId)){
			resourcesList = new ArrayList<SelectItem>();
			String siteTitle = serviceBean.getSite().getTitle();
			String siteCollectionId = serviceBean.getContentHostingService().getSiteCollection(siteId);
			List<ContentResource> rsrcs = serviceBean.getContentHostingService().getAllResources(siteCollectionId);
			Iterator<ContentResource> iR = rsrcs.iterator();
			while(iR.hasNext()){
				ContentResource cr = iR.next();
				String path = StatsManager.SEPARATOR;
				ContentCollection cc = cr.getContainingCollection();
				while(cc != null && !cc.getId().equals(siteCollectionId) && !serviceBean.getContentHostingService().isRootCollection(cc.getId())){
					path = StatsManager.SEPARATOR + cc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME) + path;
					cc = cc.getContainingCollection();
				}
				// remove /group
				path = path.replaceFirst(StatsManager.SEPARATOR + "group-user","[dropbox]");
				path = path.replaceFirst(StatsManager.SEPARATOR + "group","");
				path = path.replaceFirst(StatsManager.SEPARATOR + "attachment","[attachment]");
				path = path.replaceFirst(StatsManager.SEPARATOR + "user","[workspace]");
				path = path.replaceFirst(StatsManager.SEPARATOR + siteTitle,"");
				
				String crName = path + serviceBean.getSstStatsManager().getResourceName("/content"+cr.getId());			
				resourcesList.add(new SelectItem(cr.getId(), crName));
			}
			Collections.sort(resourcesList, getSelectItemComparator(collator));
			resourcesLoaded = true;
			resourcesLoadedForSite = siteId;
		}
	}
	
	// ######################################################################################
	// Bean methods: WHEN related 
	// ######################################################################################

	
	// ######################################################################################
	// Bean methods: WHO related 
	// ######################################################################################
	public List<SelectItem> getRoles() {
		String siteId = serviceBean.getSiteId();
		if(siteRoles == null || !siteRolesForSite.equals(siteId)) {
			siteRoles = new ArrayList<SelectItem>();
			List<String> aRoles = serviceBean.getSiteRolesAsString();
			Iterator<String> iR = aRoles.iterator();
			while(iR.hasNext()){
				String r = iR.next();
				siteRoles.add(new SelectItem(r, r));
			}
			siteRolesForSite = siteId;
			Collections.sort(siteRoles, getSelectItemComparator(collator));
		}
		return siteRoles;
	}
	
	public List<SelectItem> getGroups() {
		String siteId = serviceBean.getSiteId();
		if(siteGroups == null || !siteGroupsForSite.equals(siteId)) {
			siteGroups = new ArrayList<SelectItem>();
			Collection<Group> aGroups = serviceBean.getSiteGroups();
			Iterator<Group> iG = aGroups.iterator();
			while(iG.hasNext()){
				Group g = iG.next();
				siteGroups.add(new SelectItem(g.getId(), g.getTitle()));
			}
			siteGroupsForSite = siteId;
			Collections.sort(siteGroups, getSelectItemComparator(collator));
		}
		return siteGroups;
	}
	
	public boolean isSiteWithNoGroups() {
		if(siteGroups == null || !siteGroupsForSite.equals(serviceBean.getSiteId()))
			getGroups();
		return siteGroups == null || siteGroups.size() == 0;
		
	}
	
	public List<SelectItem> getUsers() {
		if(!(usersLoaded && usersLoadedForSite.equals(serviceBean.getSiteId())))
			if(reportParams.getWho().equals(ReportManager.WHO_CUSTOM))
				processLoadUsers(null);
			else
				return new ArrayList<SelectItem>();
		return siteUsers;
	}
	
	public boolean isUsersLoaded() {
		return usersLoaded && usersLoadedForSite.equals(serviceBean.getSiteId());
	}
	
	public void processLoadUsers(ActionEvent e) {
		if(!(usersLoaded && usersLoadedForSite.equals(serviceBean.getSiteId()))){
			lock.lock();
			try{
				siteUsers.clear();
				List<User> aUsers = serviceBean.getSiteUsers();
				Iterator<User> iU = aUsers.iterator();
				while(iU.hasNext()){
					User u = iU.next();
					siteUsers.add(new SelectItem(u.getId(), u.getDisplayName() + " (" + u.getDisplayId() + ")"));
				}
				Collections.sort(siteUsers, getSelectItemComparator(collator));
				usersLoaded = true;
				usersLoadedForSite = serviceBean.getSiteId();
			}finally{
				lock.unlock();
			}
		}
	}
	
	// ######################################################################################
	// Report results: SUMMARY 
	// ######################################################################################	
	public String getReportGenerationDate() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportGenerationDate(report);
	}
	
	public String getReportActivityBasedOn() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportActivityBasedOn(report);
	}
	
	public String getReportActivitySelectionTitle() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportActivitySelectionTitle(report);
	}
	
	public String getReportActivitySelection() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportActivitySelection(report);
	}
	
	public String getReportResourceActionTitle() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportResourceActionTitle(report);
	}
	
	public String getReportResourceAction() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportResourceAction(report);
	}
	
	public String getReportTimePeriod() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportTimePeriod(report);
	}
	
	public String getReportUserSelectionType() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportUserSelectionType(report);
	}
	
	public String getReportUserSelectionTitle() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportUserSelectionTitle(report);
	}
	
	public String getReportUserSelection() {
		return serviceBean.getSstReportManager().getReportFormattedParams().getReportUserSelection(report);
	}
	
	// ######################################################################################
	// Report results: REPORT DATA
	// ######################################################################################	
	public Report getReport(){
		return report;
	}
	
	public boolean isReportNotEmpty() {
		return report != null && report.getReportData() != null && report.getReportData().size() > 0;
	}
	
	public boolean isPrintVersion(){
		Map map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		if(map.containsKey("printVersion")){
			String printV = (String) map.get("printVersion");
			if(printV.equals("true"))
				return true;
		}
		return false;
	}
	
	public int getPagerFirstItem() {
		return pagerFirstItem;
	}
	
	public void setPagerFirstItem(int pagerFirstItem) {
		this.pagerFirstItem = pagerFirstItem;
	}

	public boolean isPagerRendered() {
		return pagerRendered;
	}

	public int getPagerSize() {
		return pagerSize;
	}

	public void setPagerSize(int pagerSize) {
		this.pagerSize = pagerSize;
	}

	public int getPagerTotalItems() {
		return pagerTotalItems;
	}	

	public void setPagerTotalItems(int pagerTotalItems) {
		this.pagerTotalItems = pagerTotalItems;
	}	

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
		Collections.sort(report.getReportData(), getReportDataComparator(getSortColumn(), sortAscending, collator, serviceBean.getSstStatsManager(), serviceBean.getUserDirectoryService()));
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
		Collections.sort(report.getReportData(), getReportDataComparator(sortColumn, isSortAscending(), collator, serviceBean.getSstStatsManager(), serviceBean.getUserDirectoryService()));
	}

	// ######################################################################################
	// Action methods
	// ######################################################################################
	public String processGenerateReport(){
		String msg = null;	
		// check WHAT
		if(reportParams.getWhat().equals(ReportManager.WHAT_EVENTS)
				&& reportParams.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYTOOL) 
				&& (reportParams.getWhatToolIds() == null || reportParams.getWhatToolIds().size() == 0)){
			msg = msgs.getString("report_err_notools");
		}else if(reportParams.getWhat().equals(ReportManager.WHAT_EVENTS) 
				&& reportParams.getWhatEventSelType().equals(ReportManager.WHAT_EVENTS_BYEVENTS) 
				&& (reportParams.getWhatEventIds() == null || reportParams.getWhatEventIds().size() == 0)) {
			msg = msgs.getString("report_err_noevents");
		}else if(reportParams.getWhat().equals(ReportManager.WHAT_RESOURCES) 
				&& selectedLimitedActivity 
				&& (reportParams.getWhatResourceIds() == null || reportParams.getWhatResourceIds().size() == 0)){
			msg = msgs.getString("report_err_noresources");
			
		// check WHEN
		}else if(reportParams.getWhen().equals(ReportManager.WHEN_CUSTOM)
				&& (reportParams.getWhenFrom() == null || reportParams.getWhenTo() == null)) {
			msg = msgs.getString("report_err_nocustomdates");
			
		// check WHO
		}else if(reportParams.getWho().equals(ReportManager.WHO_ROLE)){
			if(serviceBean.isRoleEmpty(reportParams.getWhoRoleId()))
				msg = msgs.getString("report_err_emptyrole");	
		}else if(reportParams.getWho().equals(ReportManager.WHO_GROUPS)){
			if(reportParams.getWhoGroupId() == null || reportParams.getWhoGroupId().equals(""))
				msg = msgs.getString("report_err_nogroup");
			else if(serviceBean.isSiteGroupEmpty(reportParams.getWhoGroupId()))
				msg = msgs.getString("report_err_emptygroup");	
		}else if(reportParams.getWho().equals(ReportManager.WHO_CUSTOM) 
				&& (reportParams.getWhoUserIds() == null || reportParams.getWhoUserIds().size() == 0)){
			msg = msgs.getString("report_err_nousers");
		}
		
		if(msg != null){
			message = msg;
			showFatalMessage = true;
			return "reports";
		}else{		
			report = serviceBean.getSstReportManager().getReport(serviceBean.getSiteId(), prefsdata, reportParams);
			setPagerFirstItem(0);
			setPagerTotalItems(report.getReportData().size());
			Collections.sort(report.getReportData(), getReportDataComparator(getSortColumn(), isSortAscending(), collator, serviceBean.getSstStatsManager(), serviceBean.getUserDirectoryService()));
			return "report-results";
		}
	}
	
	public String processReportGoBack(){
		return "reports";
	}


	// ######################################################################################
	// Printing; Excel, CSV, PDF Export
	// ######################################################################################
	public void processExportExcel(ActionEvent event) {
		String sheetName = null;
		if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
			sheetName = msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS))
			sheetName = msgs.getString("report_what_events");
		else 
			sheetName = msgs.getString("report_what_resources");
		
		byte[] hssfWorkbookBytes = serviceBean.getSstReportManager().getReportAsExcel(report, sheetName);
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("application/vnd.ms-excel ");
		response.setHeader("Content-disposition", "attachment; filename=" + sheetName + ".xls");

		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(hssfWorkbookBytes);
			out.flush();
		}catch(IOException e){
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
		faces.responseComplete();
	}

	public void processExportCSV(ActionEvent event) {
		String name = null;
		if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
			name = msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS))
			name = msgs.getString("report_what_events");
		else 
			name = msgs.getString("report_what_resources");
		
		String csvString = serviceBean.getSstReportManager().getReportAsCsv(report); 
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("text/comma-separated-values");
		response.setHeader("Content-disposition", "attachment; filename=" + name + ".csv");
		response.setContentLength(csvString.length());
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(csvString.getBytes());
			out.flush();
		}catch(IOException e){
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
		faces.responseComplete();
	}
	
	public void processExportPDF(ActionEvent event) {
		String fileName = null;
		if(report.getReportParams().getWhat().equals(ReportManager.WHAT_VISITS))
			fileName = getFileName(msgs.getString("report_what_visits"));
		else if(report.getReportParams().getWhat().equals(ReportManager.WHAT_EVENTS))
			fileName = getFileName(msgs.getString("report_what_events"));
		else 
			fileName = getFileName(msgs.getString("report_what_resources"));

		byte[] pdf = serviceBean.getSstReportManager().getReportAsPDF(report);
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".pdf");
		response.setContentLength(pdf.length);		
		OutputStream out = null;
		try{
			out = response.getOutputStream();
			out.write(pdf);
			out.flush();
		}catch(IOException e){
			LOG.error(e);
			e.printStackTrace();
		}finally{
			try{
				if(out != null) out.close();
			}catch(IOException e){
				LOG.error(e);
				e.printStackTrace();
			}
		}
		faces.responseComplete();
	}

	/**
	 * Gets the filename for the export
	 * @param prefix Filenameprefix
	 * @return The appropriate filename for the export
	 */
	private String getFileName(String prefix) {
		Date now = new Date();
		DateFormat df = new SimpleDateFormat(msgs.getString("export_filename_date_format"));
		StringBuffer fileName = new StringBuffer(prefix);
		fileName.append("-");
		fileName.append(df.format(now));
		return fileName.toString();
	}

	/**
	 * Try to head off a problem with downloading files from a secure HTTPS
	 * connection to Internet Explorer. When IE sees it's talking to a secure
	 * server, it decides to treat all hints or instructions about caching as
	 * strictly as possible. Immediately upon finishing the download, it throws
	 * the data away. Unfortunately, the way IE sends a downloaded file on to a
	 * helper application is to use the cached copy. Having just deleted the
	 * file, it naturally isn't able to find it in the cache. Whereupon it
	 * delivers a very misleading error message like: "Internet Explorer cannot
	 * download roster from sakai.yoursite.edu. Internet Explorer was not able
	 * to open this Internet site. The requested site is either unavailable or
	 * cannot be found. Please try again later." There are several ways to turn
	 * caching off, and so to be safe we use several ways to turn it back on
	 * again. This current workaround should let IE users save the files to
	 * disk. Unfortunately, errors may still occur if a user attempts to open
	 * the file directly in a helper application from a secure web server. TODO
	 * Keep checking on the status of this.
	 */
	public static void protectAgainstInstantDeletion(HttpServletResponse response) {
		response.reset(); // Eliminate the added-on stuff
		response.setHeader("Pragma", "public"); // Override old-style cache
												// control
		response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0"); // New-style
	}
		
	private boolean isToolSupported(ToolInfo toolInfo) {
		if(serviceBean.getSstStatsManager().isEventContextSupported()) {
			return true;
		} else {
			EventParserTip parserTip = toolInfo.getEventParserTip();
			if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID))
				return true;
		}
		return false;
	}

	
	public static final Comparator<SelectItem> getSelectItemComparator(final Collator collator){
		return new Comparator<SelectItem>(){
			public int compare(SelectItem o1, SelectItem o2) {
				return collator.compare(o1.getLabel(), o2.getLabel());
			}		
		};
	}
	
	public static final Comparator<CommonStatGrpByDate> getReportDataComparator(final String fieldName, final boolean sortAscending, final Collator collator,
			final StatsManager SST_sm, final UserDirectoryService M_uds) {
		return new Comparator<CommonStatGrpByDate>() {

			public int compare(CommonStatGrpByDate r1, CommonStatGrpByDate r2) {
				try{
					if(fieldName.equals(SORT_ID)){
						String s1 = M_uds.getUser(r1.getUserId()).getDisplayId();
						String s2 = M_uds.getUser(r2.getUserId()).getDisplayId();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_USER)){
						String s1 = M_uds.getUser(r1.getUserId()).getDisplayName().toLowerCase();
						String s2 = M_uds.getUser(r2.getUserId()).getDisplayName().toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_EVENT)){
						String s1 = SST_sm.getEventName(r1.getRef()).toLowerCase();
						String s2 = SST_sm.getEventName(r2.getRef()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_RESOURCE)){
						String s1 = SST_sm.getResourceName(r1.getRef()).toLowerCase();
						String s2 = SST_sm.getResourceName(r2.getRef()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_ACTION)){
						String s1 = ((String) r1.getRefAction()).toLowerCase();
						String s2 = ((String) r2.getRefAction()).toLowerCase();
						int res = collator.compare(s1, s2);
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_DATE)){
						int res = r1.getDate().compareTo(r2.getDate());
						if(sortAscending) return res;
						else return -res;
					}else if(fieldName.equals(SORT_TOTAL)){
						int res = new Long(r1.getCount()).compareTo(new Long(r2.getCount()));
						if(sortAscending) return res;
						else return -res;
					}
					return 0;
				}catch(Exception e){
					return 0;
				}
			}
		};
	}
}
