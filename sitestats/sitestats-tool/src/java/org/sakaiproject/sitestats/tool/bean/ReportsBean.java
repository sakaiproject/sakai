package org.sakaiproject.sitestats.tool.bean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.servlet.ServletContextURIResolver;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.EventInfo;
import org.sakaiproject.sitestats.api.EventParserTip;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.Report;
import org.sakaiproject.sitestats.api.ReportParams;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.ToolInfo;
import org.sakaiproject.sitestats.tool.util.ReportInputSource;
import org.sakaiproject.sitestats.tool.util.ReportXMLReader;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class ReportsBean {
	private static final String				XML_FO_XSL_FILE			= "xmlReportToFo.xsl";
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
	private transient StatsManager			SST_sm					= null;
	private transient UserDirectoryService	M_uds					= null;
	private transient ContentHostingService	M_chs					= null;
	private transient TimeService			M_ts					= null;

	/** FOP */
	private FopFactory						fopFactory				= FopFactory.newInstance();
	private Templates						cachedXmlFoXSLT			= null;

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
		this.SST_sm = serviceBean.getSstStatsManager();
		this.M_uds = serviceBean.getUserDirectoryService();
		this.M_chs = serviceBean.getContentHostingService();
		this.M_ts = serviceBean.getTimeService();
	}	
	
	public void setReportParams(ReportParams reportParams) {
		this.reportParams = reportParams;
		if(!serviceBean.getSiteVisitsEnabled())
			this.reportParams.setWhat(StatsManager.WHAT_EVENTS_BYTOOL);
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
			prefsdata = SST_sm.getPreferences(siteId, false);
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
		
		List<ToolInfo> siteTools = SST_sm.getSiteToolEventsDefinition(serviceBean.getSiteId(), getPrefsdata().isListToolEventsOnlyAvailableInSite());
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
		
		List<ToolInfo> siteTools = SST_sm.getSiteToolEventsDefinition(serviceBean.getSiteId(), getPrefsdata().isListToolEventsOnlyAvailableInSite());
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
		actions.add(new SelectItem(StatsManager.WHAT_RESOURCES_ACTION_NEW, msgs.getString("action_new")));
		actions.add(new SelectItem(StatsManager.WHAT_RESOURCES_ACTION_READ, msgs.getString("action_read")));
		actions.add(new SelectItem(StatsManager.WHAT_RESOURCES_ACTION_REVS, msgs.getString("action_revise")));
		actions.add(new SelectItem(StatsManager.WHAT_RESOURCES_ACTION_DEL, msgs.getString("action_delete")));
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
			String siteCollectionId = M_chs.getSiteCollection(siteId);
			List<ContentResource> rsrcs =  M_chs.getAllResources(siteCollectionId);
			Iterator<ContentResource> iR = rsrcs.iterator();
			while(iR.hasNext()){
				ContentResource cr = iR.next();
				String path = StatsManager.SEPARATOR;
				ContentCollection cc = cr.getContainingCollection();
				while(cc != null && !cc.getId().equals(siteCollectionId) && !M_chs.isRootCollection(cc.getId())){
					path = StatsManager.SEPARATOR + cc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME) + path;
					cc = cc.getContainingCollection();
				}
				// remove /group
				path = path.replaceFirst(StatsManager.SEPARATOR + "group-user","[dropbox]");
				path = path.replaceFirst(StatsManager.SEPARATOR + "group","");
				path = path.replaceFirst(StatsManager.SEPARATOR + "attachment","[attachment]");
				path = path.replaceFirst(StatsManager.SEPARATOR + "user","[workspace]");
				path = path.replaceFirst(StatsManager.SEPARATOR + siteTitle,"");
				
				String crName = path + SST_sm.getResourceName("/content"+cr.getId());			
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
			if(reportParams.getWho().equals(StatsManager.WHO_CUSTOM))
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
		return getReportGenerationDate(report);
	}
	public String getReportGenerationDate(Report report) {
		if(report.getReportGenerationDate() == null)
			report.setReportGenerationDate(M_ts.newTime());
		return report.getReportGenerationDate().toStringLocalFull();
	}

	
	public String getReportActivityBasedOn() {
		return getReportActivityBasedOn(report);
	}
	public String getReportActivityBasedOn(Report report) {
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS))
			return msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS)){
			StringBuffer buff = new StringBuffer();
			buff.append(msgs.getString("report_what_events"));
			if(report.getReportParams().getWhatEventSelType().equals(StatsManager.WHAT_EVENTS_BYTOOL)){
				buff.append(" (");
				buff.append(msgs.getString("report_what_events_bytool"));
				buff.append(")");
			}else{
				buff.append(" (");
				buff.append(msgs.getString("report_what_events_byevent"));
				buff.append(")");
			}
			return buff.toString();
		}else 
			return msgs.getString("report_what_resources");
	}
	
	public String getReportActivitySelectionTitle() {
		return getReportActivitySelectionTitle(report);
	}
	public String getReportActivitySelectionTitle(Report report) {
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS))
			return msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS)){
			if(report.getReportParams().getWhatEventSelType().equals(StatsManager.WHAT_EVENTS_BYTOOL))
				return msgs.getString("reportres_summ_act_tools_selected");
			else 
				return msgs.getString("reportres_summ_act_events_selected");
		}else
			return msgs.getString("reportres_summ_act_rsrc_selected");
	}
	
	public String getReportActivitySelection() {
		return getReportActivitySelection(report);
	}
	public String getReportActivitySelection(Report report) {
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS)){
			// visits
			return null;
		}else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS)){
			if(report.getReportParams().getWhatEventSelType().equals(StatsManager.WHAT_EVENTS_BYTOOL)){
				// tools
				List<String> list = report.getReportParams().getWhatToolIds();
				StringBuffer buff = new StringBuffer();
				for(int i=0; i<list.size() - 1; i++){
					String toolId = list.get(i);
					buff.append(SST_sm.getToolName(toolId));
					buff.append(", ");
				}
				String toolId = list.get(list.size() - 1);
				buff.append(SST_sm.getToolName(toolId));
				return buff.toString();
			}else{
				// events
				List<String> list = report.getReportParams().getWhatEventIds();
				StringBuffer buff = new StringBuffer();
				for(int i=0; i<list.size() - 1; i++){
					String eventId = list.get(i);
					buff.append(SST_sm.getEventName(eventId));
					buff.append(", ");
				}
				String eventId = list.get(list.size() - 1);
				buff.append(SST_sm.getEventName(eventId));
				return buff.toString();
			}
		}else{
			if(!selectedLimitedActivity)
				return null;
			// resources
			List<String> list = report.getReportParams().getWhatResourceIds();
			if(list.contains("all"))
				return msgs.getString("report_what_all");
			StringBuffer buff = new StringBuffer();
			for(int i=0; i<list.size() - 1; i++){
				String resourceId = list.get(i);
				try{
					ContentResource cr = M_chs.getResource(resourceId);
					String crName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
					buff.append(crName);
					buff.append(", ");
				}catch(PermissionException e){
					e.printStackTrace();
				}catch(IdUnusedException e){
					e.printStackTrace();
				}catch(TypeException e){
					e.printStackTrace();
				}
			}
			String resourceId = list.get(list.size() - 1);
			try{
				ContentResource cr = M_chs.getResource(resourceId);
				String crName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);	
				buff.append(crName);
			}catch(PermissionException e){
				e.printStackTrace();
			}catch(IdUnusedException e){
				e.printStackTrace();
			}catch(TypeException e){
				e.printStackTrace();
			}
			return buff.toString();
		}
	}
	
	public String getReportResourceActionTitle() {
		return getReportResourceActionTitle(report);
	}
	public String getReportResourceActionTitle(Report report) {
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)
				&& report.getReportParams().getWhatResourceAction() != null)
				return msgs.getString("reportres_summ_act_rsrc_action");
		return null;
	}
	
	public String getReportResourceAction() {
		return getReportResourceAction(report);
	}
	public String getReportResourceAction(Report report) {
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)
				&& report.getReportParams().getWhatResourceAction() != null){
			return msgs.getString("action_" + report.getReportParams().getWhatResourceAction());
		}else
			return null;
	}
	
	public String getReportTimePeriod() {
		return getReportTimePeriod(report);
	}
	public String getReportTimePeriod(Report report) {
		if(report.getReportParams().getWhen().equals(StatsManager.WHEN_ALL)){
			return msgs.getString("report_when_all");
		}else{
			Time from = M_ts.newTime(report.getReportParams().getWhenFrom().getTime());
			Time to = M_ts.newTime(report.getReportParams().getWhenTo().getTime());
			return from.toStringLocalFull() + " - " + to.toStringLocalFull();
		}
	}
	
	public String getReportUserSelectionType() {
		return getReportUserSelectionType(report);
	}
	public String getReportUserSelectionType(Report report) {
		if(report.getReportParams().getWho().equals(StatsManager.WHO_ALL))
			return msgs.getString("report_who_all");
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_GROUPS))
			return msgs.getString("report_who_group");
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_ROLE))
			return msgs.getString("report_who_role");
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_CUSTOM))
			return msgs.getString("report_who_custom");
		else 
			return msgs.getString("report_who_not_match");
	}
	
	public String getReportUserSelectionTitle() {
		return getReportUserSelectionTitle(report);
	}
	public String getReportUserSelectionTitle(Report report) {
		if(report.getReportParams().getWho().equals(StatsManager.WHO_ALL))
			return null;
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_GROUPS))
			return msgs.getString("reportres_summ_usr_group_selected");
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_ROLE))
			return msgs.getString("reportres_summ_usr_role_selected");
		else if(report.getReportParams().getWho().equals(StatsManager.WHO_CUSTOM))
			return msgs.getString("reportres_summ_usr_users_selected");
		else 
			return null;		
	}
	
	public String getReportUserSelection() {
		return getReportUserSelection(report);
	}
	public String getReportUserSelection(Report report) {
		if(report.getReportParams().getWho().equals(StatsManager.WHO_GROUPS)){
			return serviceBean.getSiteGroupTitle(report.getReportParams().getWhoGroupId());
		}else if(report.getReportParams().getWho().equals(StatsManager.WHO_ROLE)){
			return report.getReportParams().getWhoRoleId();
		}else if(report.getReportParams().getWho().equals(StatsManager.WHO_CUSTOM)){
			// users
			List<String> list = report.getReportParams().getWhoUserIds();
			StringBuffer buff = new StringBuffer();
			for(int i=0; i<list.size() - 1; i++){
				String userId = list.get(i);
				buff.append(serviceBean.getUserDisplayId(userId));
				buff.append(", ");
			}
			String userId = list.get(list.size() - 1);
			buff.append(serviceBean.getUserDisplayId(userId));
			return buff.toString();
		}else
			return null;
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
		Collections.sort(report.getReportData(), getReportDataComparator(getSortColumn(), sortAscending, collator, SST_sm, M_uds));
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
		Collections.sort(report.getReportData(), getReportDataComparator(sortColumn, isSortAscending(), collator, SST_sm, M_uds));
	}

	// ######################################################################################
	// Action methods
	// ######################################################################################
	public String processGenerateReport(){
		String msg = null;	
		// check WHAT
		if(reportParams.getWhat().equals(StatsManager.WHAT_EVENTS)
				&& reportParams.getWhatEventSelType().equals(StatsManager.WHAT_EVENTS_BYTOOL) 
				&& (reportParams.getWhatToolIds() == null || reportParams.getWhatToolIds().size() == 0)){
			msg = msgs.getString("report_err_notools");
		}else if(reportParams.getWhat().equals(StatsManager.WHAT_EVENTS) 
				&& reportParams.getWhatEventSelType().equals(StatsManager.WHAT_EVENTS_BYEVENTS) 
				&& (reportParams.getWhatEventIds() == null || reportParams.getWhatEventIds().size() == 0)) {
			msg = msgs.getString("report_err_noevents");
		}else if(reportParams.getWhat().equals(StatsManager.WHAT_RESOURCES) 
				&& selectedLimitedActivity 
				&& (reportParams.getWhatResourceIds() == null || reportParams.getWhatResourceIds().size() == 0)){
			msg = msgs.getString("report_err_noresources");
			
		// check WHEN
		}else if(reportParams.getWhen().equals(StatsManager.WHEN_CUSTOM)
				&& (reportParams.getWhenFrom() == null || reportParams.getWhenTo() == null)) {
			msg = msgs.getString("report_err_nocustomdates");
			
		// check WHO
		}else if(reportParams.getWho().equals(StatsManager.WHO_ROLE)){
			if(serviceBean.isRoleEmpty(reportParams.getWhoRoleId()))
				msg = msgs.getString("report_err_emptyrole");	
		}else if(reportParams.getWho().equals(StatsManager.WHO_GROUPS)){
			if(reportParams.getWhoGroupId() == null || reportParams.getWhoGroupId().equals(""))
				msg = msgs.getString("report_err_nogroup");
			else if(serviceBean.isSiteGroupEmpty(reportParams.getWhoGroupId()))
				msg = msgs.getString("report_err_emptygroup");	
		}else if(reportParams.getWho().equals(StatsManager.WHO_CUSTOM) 
				&& (reportParams.getWhoUserIds() == null || reportParams.getWhoUserIds().size() == 0)){
			msg = msgs.getString("report_err_nousers");
		}
		
		if(msg != null){
			message = msg;
			showFatalMessage = true;
			return "reports";
		}else{		
			report = SST_sm.getReport(serviceBean.getSiteId(), prefsdata, reportParams);
			setPagerFirstItem(0);
			setPagerTotalItems(report.getReportData().size());
			Collections.sort(report.getReportData(), getReportDataComparator(getSortColumn(), isSortAscending(), collator, SST_sm, M_uds));
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
		String name = null;
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS))
			name = msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS))
			name = msgs.getString("report_what_events");
		else 
			name = msgs.getString("report_what_resources");
		writeAsExcel(getAsExcel(report.getReportData(), name), getFileName(name));
	}

	public void processExportCSV(ActionEvent event) {
		String name = null;
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS))
			name = msgs.getString("report_what_visits");
		else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS))
			name = msgs.getString("report_what_events");
		else 
			name = msgs.getString("report_what_resources");
		writeAsCsv(getAsCsv(report.getReportData(), name), getFileName(name));
	}
	
	public void processExportPDF(ActionEvent event) {
		// PDF filename
		String fileName = null;
		if(report.getReportParams().getWhat().equals(StatsManager.WHAT_VISITS))
			fileName = getFileName(msgs.getString("report_what_visits"));
		else if(report.getReportParams().getWhat().equals(StatsManager.WHAT_EVENTS))
			fileName = getFileName(msgs.getString("report_what_events"));
		else 
			fileName = getFileName(msgs.getString("report_what_resources"));

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		ByteArrayOutputStream out = null;
		OutputStream responseOut = null;
		try{
			// Setup a buffer to obtain the content length
		    out = new ByteArrayOutputStream();
		    
		    ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();		    		    
		    fopFactory.setURIResolver(new ServletContextURIResolver(servletContext));
			
			FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
	        // configure foUserAgent as desired
			File burl = new File(servletContext.getRealPath("/"));
			foUserAgent.setBaseURL("file://"+ burl.getParent()+"/library/");
			
			
            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup XSLT
            if(cachedXmlFoXSLT == null) {
            	InputStream xslt = this.getClass().getResourceAsStream("/org/sakaiproject/sitestats/tool/util/"+XML_FO_XSL_FILE);
            	TransformerFactory factory = TransformerFactory.newInstance();
	            cachedXmlFoXSLT = factory.newTemplates(new StreamSource(xslt));
            }
            Transformer transformer = cachedXmlFoXSLT.newTransformer();
        
            // Setup input for XSLT transformation
            Source src = new SAXSource(new ReportXMLReader(), new ReportInputSource(getReport()));
        
            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);
    		
    		// setup response
    		protectAgainstInstantDeletion(response);
    		response.setContentType(MimeConstants.MIME_PDF);
    		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".pdf");
    		response.setContentLength(out.size());
    		
    		// Send content to Browser
    		responseOut = response.getOutputStream();
    		responseOut.write(out.toByteArray());
    		responseOut.flush();
    	    
		}catch(IOException e){
			LOG.error("IOException while writing SiteStats PDF report", e);
		}catch(TransformerConfigurationException e){
			LOG.error("TransformerConfigurationException while writing SiteStats PDF report", e);
		}catch(FOPException e){
			LOG.error("FOPException while writing SiteStats PDF report", e);
		}catch(TransformerException e){
			LOG.error("TransformerException while writing SiteStats PDF report", e);
		}finally{
			try{
				if(out != null) out.close();
				if(responseOut != null) responseOut.close();
			}catch(IOException e){
				LOG.error("IOException while writing SiteStats PDF report", e);
			}
		}
		faces.responseComplete();
	}

	/**
	 * Constructs an excel workbook document representing the table
	 * @param statsObjects The list of StatsEntry objects to include in the
	 *            spreadsheet
	 * @return The excel workbook
	 */
	private HSSFWorkbook getAsExcel(List statsObjects, String sheetName) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName);
		HSSFRow headerRow = sheet.createRow((short) 0);

		// Add the column headers
		headerRow.createCell((short) (0)).setCellValue(msgs.getString("th_id"));
		headerRow.createCell((short) (1)).setCellValue(msgs.getString("th_user"));
		if(!report.getReportParams().getWho().equals(StatsManager.WHO_NONE)) {
			if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)){
				headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_resource"));
				headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_action"));
				headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_date"));
				headerRow.createCell((short) (5)).setCellValue(msgs.getString("th_total"));
			}else{
				headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_event"));
				headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_date"));
				headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_total"));
			}
		}

		// Fill the spreadsheet cells
		Iterator i = statsObjects.iterator();
		while (i.hasNext()){
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			CommonStatGrpByDate se = (CommonStatGrpByDate) i.next();
			// user name
			String userId = se.getUserId();
			String userEid = serviceBean.getUserDisplayId(userId);
			row.createCell((short) 0).setCellValue(userEid);
			String name = serviceBean.getUserDisplayName(userId);
			row.createCell((short) 1).setCellValue(name);
			if(!report.getReportParams().getWho().equals(StatsManager.WHO_NONE)) {
				if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)){
					// resource name
					row.createCell((short) 2).setCellValue(se.getRef());
					// resource action
					row.createCell((short) 3).setCellValue(se.getRefAction());
					// most recent lastDate
					row.createCell((short) 4).setCellValue(se.getDate().toString());
					// total
					row.createCell((short) 5).setCellValue(se.getCount());
				}else{
					// event name
					row.createCell((short) 2).setCellValue(SST_sm.getEventName(se.getRef()));
					// most recent lastDate
					row.createCell((short) 3).setCellValue(se.getDate().toString());
					// total
					row.createCell((short) 4).setCellValue(se.getCount());
				}
			}
		}

		return wb;
	}

	/**
	 * Constructs a string representing the table.
	 * @param statsObjects The list of StatsEntry objects to include in the
	 *            spreadsheet
	 * @param sheetName The sheet name
	 * @return The csv document
	 */
	private String getAsCsv(List statsObjects, String sheetName) {
		StringBuffer sb = new StringBuffer();

		// Add the headers
		appendQuoted(sb, msgs.getString("th_id"));
		sb.append(",");
		appendQuoted(sb, msgs.getString("th_user"));
		if(!report.getReportParams().getWho().equals(StatsManager.WHO_NONE)) {
			sb.append(",");
			if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)){
				appendQuoted(sb, msgs.getString("th_resource"));
				sb.append(",");
				appendQuoted(sb, msgs.getString("th_action"));
			}else{
				appendQuoted(sb, msgs.getString("th_event"));
			}
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_date"));
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_total"));
		}
		sb.append("\n");

		// Add the data
		Iterator i = statsObjects.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate se = (CommonStatGrpByDate) i.next();
			// user id
			String userId = se.getUserId();
			String userEid = serviceBean.getUserDisplayId(userId);
			appendQuoted(sb, userEid);
			sb.append(",");
			// user name
			String name = serviceBean.getUserDisplayName(userId);
			appendQuoted(sb, name);
			if(!report.getReportParams().getWho().equals(StatsManager.WHO_NONE)) {
				sb.append(",");
				if(report.getReportParams().getWhat().equals(StatsManager.WHAT_RESOURCES)){
					// resource name
					appendQuoted(sb, se.getRef());
					sb.append(",");
					// resource action
					appendQuoted(sb, se.getRefAction());
					sb.append(",");
				}else{
					// event name
					appendQuoted(sb, SST_sm.getEventName(se.getRef()));
					sb.append(",");
				}
				// most recent lastDate
				appendQuoted(sb, se.getDate().toString());
				sb.append(",");
				// total
				appendQuoted(sb, Long.toString(se.getCount()));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private StringBuffer appendQuoted(StringBuffer sb, String toQuote) {
		if((toQuote.indexOf(',') >= 0) || (toQuote.indexOf('"') >= 0)){
			String out = toQuote.replaceAll("\"", "\"\"");
			if(LOG.isDebugEnabled()) LOG.debug("Turning '" + toQuote + "' to '" + out + "'");
			sb.append("\"").append(out).append("\"");
		}else{
			sb.append(toQuote);
		}
		return sb;
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

	private void writeAsExcel(HSSFWorkbook wb, String fileName) {
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("application/vnd.ms-excel ");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

		OutputStream out = null;
		try{
			out = response.getOutputStream();
			// For some reason, you can't write the byte[] as in the csv export.
			// You need to write directly to the output stream from the
			// workbook.
			wb.write(out);
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

	private void writeAsCsv(String csvString, String fileName) {
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		protectAgainstInstantDeletion(response);
		response.setContentType("text/comma-separated-values");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".csv");
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
		if(SST_sm.isEventContextSupported()) {
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
