/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.bean;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitestats.api.CommonStatGrpByDate;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class BaseFilteringBean extends InitializableBean {
	private static final long	serialVersionUID	= 7571936487988102788L;
	public static final String	NO_DATE				= "-";
	public static final String	SORT_ID				= "id";
	public static final String	SORT_USER			= "user";
	public static final String	SORT_EVENT			= "event";
	public static final String	SORT_RESOURCE		= "resource";
	public static final String	SORT_ACTION			= "action";
	public static final String	SORT_DATE			= "date";
	public static final String	SORT_TOTAL			= "total";

	protected static ResourceLoader	msgs			= new ResourceLoader("org.sakaiproject.sitestats.tool.bundle.Messages");
	private static Log			LOG					= LogFactory.getLog(BaseFilteringBean.class);
	public String				ALL					= msgs.getString("all");

	/** Private vars */
	private List				eventIds			= null;
	private List				events				= null;
	private boolean				eventIdsChanged		= true;

	/** Sorting */
	private boolean				sortAscending		= true;
	private String				sortColumn			= SORT_USER;

	/** Groups */
	private String				selectedGroup		= null;
	private List				groups				= null;

	/** Date related vars */
	private Date				initialActivityDate	= null;
	private String				selectedDateType	= "0";
	private List				years				= null;
	private List				months				= null;
	private List				days				= null;
	private String				fromYear			= null;
	private String				fromMonth			= null;
	private String				fromDay				= null;
	private String				toYear				= null;
	private String				toMonth				= null;
	private String				toDay				= null;

	/** Pager related */
	private int					totalItems			= -1;
	private int					firstItem			= 0;
	private int					pageSize			= 20;
	private boolean				renderPager			= true;

	/** Search related */
	private String				searchKeyword		= null;

	/** Statistics Manager object */
	private BaseBean			baseBean			= null;
	private StatsManager		sm					= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	private Collator			collator			= Collator.getInstance();

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("BaseFilteringBean.init()");

		initializeBaseBean();

		if(baseBean.isAllowed()){
			// initialize ids
			if(selectedGroup == null) selectedGroup = ALL;
			if(searchKeyword == null) searchKeyword = msgs.getString("search_int");

			// initial values for lastDate selectors
			if(fromYear == null) initializeDates();
		}
	}

	private void initializeBaseBean() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		baseBean = (BaseBean) facesContext.getApplication().createValueBinding("#{BaseBean}").getValue(facesContext);
	}

	private BaseBean getBaseBean() {
		if(baseBean == null) initializeBaseBean();
		return baseBean;
	}

	private void initializeDates() {
		Calendar c = Calendar.getInstance();
		toYear = Integer.toString(c.get(Calendar.YEAR));
		toMonth = toTwoDigitString(c.get(Calendar.MONTH) + 1);
		toDay = toTwoDigitString(c.get(Calendar.DAY_OF_MONTH));
		initialActivityDate = sm.getInitialActivityDate(getBaseBean().getSiteId());
		c.setTimeInMillis(initialActivityDate.getTime());
		fromYear = Integer.toString(c.get(Calendar.YEAR));
		fromMonth = toTwoDigitString(c.get(Calendar.MONTH) + 1);
		fromDay = toTwoDigitString(c.get(Calendar.DAY_OF_MONTH));
	}

	// ######################################################################################
	// ActionListener methods
	// ######################################################################################
	public void processActionSearchChangeListener(ValueChangeEvent event) {
		String newValue = (String) event.getNewValue();
		if(!searchKeyword.equals(newValue)) firstItem = 0;
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public List getEventIds() {
		eventIds = sm.getSiteConfiguredEventIds(getBaseBean().getSiteId(), StatsManager.PREFS_EVENTS_PAGE);
		return eventIds;
	}

	public boolean isEventIdsChanged() {
		List l = sm.getSiteConfiguredEventIds(getBaseBean().getSiteId(), StatsManager.PREFS_EVENTS_PAGE);
		eventIdsChanged = !l.equals(eventIds);
		eventIds = l;
		return this.eventIdsChanged;
	}

	public List getEvents() {
		return this.events;
	}

	public void setEvents(List events) {
		this.events = events;
	}

	public String getEventName(String eventId) {
		return sm.getEventName(eventId);
	}

	public String getUserEid(String userId) {
		String userEid = null;
		try{
			userEid = UserDirectoryService.getUserEid(userId);
			;
		}catch(UserNotDefinedException e1){
			userEid = userId;
		}
		return userEid;
	}

	public static String getUserName(String userId) {
		String name = userId;
		try{
			User u = UserDirectoryService.getUser(userId);
			name = u.getEid();
			String lName = u.getLastName();
			String fName = u.getFirstName();
			if(lName == null || lName.equals("")) name = fName != null ? fName : "";
			else if(fName == null || fName.equals("")) name = lName != null ? lName : "";
			else name = lName + ", " + fName;
		}catch(UserNotDefinedException e1){
			name = "";
		}
		return name;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	public String getSortColumn() {
		return sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}

	public boolean isEmptyList() {
		events = getEvents();
		return (events == null || events.size() == 0);
	}

	public String getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(String selectedGroup) {
		if(!selectedGroup.equals(this.selectedGroup)){
			this.selectedGroup = selectedGroup;
			this.firstItem = 0;
		}
	}

	public List getGroups() {
		groups = new ArrayList();
		try{
			Collection grps = Collections.synchronizedCollection(SiteService.getSite(getBaseBean().getSiteId()).getGroups());
			Iterator ig = grps.iterator();
			while (ig.hasNext()){
				Group g = (Group) ig.next();
				groups.add(new SelectItem(g.getId(), g.getTitle()));
			}
			Collections.sort(groups, getComboItemsComparator(collator));
			groups.add(0, new SelectItem(ALL));
		}catch(IdUnusedException e2){
			LOG.warn("Unable to retrieve group list for site id: " + getBaseBean().getSiteId() + ". Site no longer existent?");
		}
		return groups;
	}

	public void setGroups(List groups) {
		this.groups = groups;
	}

	public int getGroupsSize() {
		if(groups != null && groups.size() > 0) return groups.size();
		else return 0;
	}

	public String getSearchKeyword() {
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	// ######################################################################################
	// Pager related methods
	// ######################################################################################
	public int getFirstItem() {
		return firstItem;
	}

	public void setFirstItem(int firstItem) {
		this.firstItem = firstItem;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalItems() {
		return this.totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public boolean isRenderPager() {
		return renderPager;
	}

	public void setRenderPager(boolean render) {
		this.renderPager = render;
	}

	// ######################################################################################
	// Date related methods
	// ######################################################################################
	public String getSelectedDateType() {
		return selectedDateType;
	}

	public void setSelectedDateType(String selectedDateType) {
		this.selectedDateType = selectedDateType;
	}

	public String getFromDay() {
		return fromDay;
	}

	public void setFromDay(String fromDay) {
		this.fromDay = fromDay;
	}

	public String getFromMonth() {
		return fromMonth;
	}

	public void setFromMonth(String fromMonth) {
		this.fromMonth = fromMonth;
	}

	public String getFromYear() {
		return fromYear;
	}

	public void setFromYear(String fromYear) {
		this.fromYear = fromYear;
	}

	public String getToDay() {
		return toDay;
	}

	public void setToDay(String toDay) {
		this.toDay = toDay;
	}

	public String getToMonth() {
		return toMonth;
	}

	public void setToMonth(String toMonth) {
		this.toMonth = toMonth;
	}

	public String getToYear() {
		return toYear;
	}

	public void setToYear(String toYear) {
		this.toYear = toYear;
	}

	protected String toTwoDigitString(int n){
		if(n < 10)
			return "0" + String.valueOf(n);
		return String.valueOf(n);
	}
	
	public List getDays() {
		if(days == null){
			days = new ArrayList();
			days.add(new SelectItem(NO_DATE));
			for(int i = 0; i < 31; i++)
				days.add(new SelectItem(toTwoDigitString(i + 1)));
		}
		return days;
	}

	public List getMonths() {
		if(months == null){
			months = new ArrayList();
			months.add(new SelectItem(NO_DATE));
			for(int i = 0; i < 12; i++)
				months.add(new SelectItem(toTwoDigitString(i + 1)));
		}
		return months;
	}

	public List getYears() {
		if(years == null){
			years = new ArrayList();
			Calendar c = Calendar.getInstance();
			int currYear = c.get(Calendar.YEAR);
			if(initialActivityDate == null) initializeDates();
			c.setTimeInMillis(initialActivityDate.getTime());
			int firstYear = c.get(Calendar.YEAR);
			years.add(new SelectItem(NO_DATE));
			for(int i = firstYear; i <= currYear; i++)
				years.add(new SelectItem(i + ""));
		}
		return years;
	}

	public String getDateTypeAllStr() {
		return msgs.getString("date_type_all");
	}

	public String getDateFromStr() {
		return msgs.getString("from_date");
	}

	public String getDateToStr() {
		return msgs.getString("to_date");
	}

	// ######################################################################################
	// Class comparators
	// ######################################################################################
	public static final Comparator getCommonStatGrpByDateComparator(final String fieldName, final boolean sortAscending, final Collator collator) {
		return new Comparator() {
			private StatsManager	sm	= (StatsManager) ComponentManager.get(StatsManager.class.getName());

			public int compare(Object o1, Object o2) {
				if(o1 instanceof CommonStatGrpByDate && o2 instanceof CommonStatGrpByDate){
					CommonStatGrpByDate r1 = (CommonStatGrpByDate) o1;
					CommonStatGrpByDate r2 = (CommonStatGrpByDate) o2;
					try{
						if(fieldName.equals(SORT_ID)){
							String s1 = ((String) r1.getUserId()).toLowerCase();
							String s2 = ((String) r2.getUserId()).toLowerCase();
							int res = collator.compare(s1, s2);
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_USER)){
							String s1 = getUserName(r1.getUserId()).toLowerCase();
							String s2 = getUserName(r2.getUserId()).toLowerCase();
							int res = collator.compare(s1, s2);
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_EVENT)){
							String s1 = sm.getEventName(r1.getRef()).toLowerCase();
							String s2 = sm.getEventName(r2.getRef()).toLowerCase();
							int res = collator.compare(s1, s2);
							if(sortAscending) return res;
							else return -res;
						}else if(fieldName.equals(SORT_RESOURCE)){
							String s1 = sm.getResourceName(r1.getRef()).toLowerCase();
							String s2 = sm.getResourceName(r2.getRef()).toLowerCase();
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
					}catch(Exception e){
					}
				}
				return 0;
			}
		};
	}

	public static final Comparator getComboItemsComparator(final Collator collator) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof SelectItem && o2 instanceof SelectItem){
					SelectItem r1 = (SelectItem) o1;
					SelectItem r2 = (SelectItem) o2;
					return collator.compare(r1.getLabel().toLowerCase(), r2.getLabel().toLowerCase());
				}
				return 0;
			}
		};
	}

	// ######################################################################################
	// Results filter
	// ######################################################################################
	public List filterUser(List list, String userId) {
		String regexp = ".*" + userId.toLowerCase() + ".*";
		List newList = new ArrayList();
		Iterator i = list.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate e = (CommonStatGrpByDate) i.next();
			String un = getUserName(e.getUserId());
			if(un.toLowerCase().matches(regexp)) newList.add(e);
		}
		return newList;
	}

	public List filterGroup2(List list, String groupId) {
		Group grp = getBaseBean().getSite().getGroup(groupId);
		List newList = new ArrayList();
		Iterator i = list.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate e = (CommonStatGrpByDate) i.next();
			String userId = e.getUserId();
			if(grp.getMember(userId) != null) newList.add(e);
		}
		return newList;
	}

	public List filterGroup(List list, String groupId) {
		Group grp = getBaseBean().getSite().getGroup(groupId);
		if(grp == null) return list;

		// List of member ids
		List members = new ArrayList();
		for(Iterator i = grp.getMembers().iterator(); i.hasNext();){
			members.add(((Member) i.next()).getUserId());
		}

		List newList = new ArrayList();
		Iterator i = list.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate e = (CommonStatGrpByDate) i.next();
			if(members.contains(e.getUserId())) newList.add(e);
		}
		return newList;
	}

	public Date convertToDate(String fY, String fM, String fD) {
		Calendar c = Calendar.getInstance();
		Date date;
		if(fY.equals(NO_DATE) || fM.equals(NO_DATE) || fD.equals(NO_DATE)) date = null;
		else{
			c.set(Calendar.YEAR, Integer.parseInt(fY));
			c.set(Calendar.MONTH, Integer.parseInt(fM) - 1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(fD));
			date = c.getTime();
		}
		return date;
	}

	// ######################################################################################
	// Excel export
	// ######################################################################################
	public void exportEventsXls(ActionEvent event) {
		String events = msgs.getString("menu_events");
		writeAsExcel(getAsExcel(getEvents(), events), getFileName(events));
	}

	public void exportResourcesXls(ActionEvent event) {
		String events = msgs.getString("menu_resources");
		writeAsExcel(getAsExcel(getEvents(), events), getFileName(events));
	}

	public void exportEventsCsv(ActionEvent event) {
		String events = msgs.getString("menu_events");
		writeAsCsv(getAsCsv(getEvents(), events), getFileName(events));
	}

	public void exportResourcesCsv(ActionEvent event) {
		String events = msgs.getString("menu_resources");
		writeAsCsv(getAsCsv(getEvents(), events), getFileName(events));
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
		if(sheetName.equals(msgs.getString("menu_events"))){
			headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_event"));
			headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_date"));
			headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_total"));
		}else if(sheetName.equals(msgs.getString("menu_resources"))){
			headerRow.createCell((short) (2)).setCellValue(msgs.getString("th_resource"));
			headerRow.createCell((short) (3)).setCellValue(msgs.getString("th_action"));
			headerRow.createCell((short) (4)).setCellValue(msgs.getString("th_date"));
			headerRow.createCell((short) (5)).setCellValue(msgs.getString("th_total"));
		}

		// Fill the spreadsheet cells
		Map eventNames = sm.getEventNameMap();
		Iterator i = statsObjects.iterator();
		while (i.hasNext()){
			HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
			CommonStatGrpByDate se = (CommonStatGrpByDate) i.next();
			// user name
			String userId = se.getUserId();
			String userEid = getUserEid(userId);
			row.createCell((short) 0).setCellValue(userEid);
			String name = getUserName(userId);
			row.createCell((short) 1).setCellValue(name);
			if(sheetName.equals(msgs.getString("menu_events"))){
				// event name
				row.createCell((short) 2).setCellValue((String) eventNames.get(se.getRef()));
				// most recent lastDate
				row.createCell((short) 3).setCellValue(se.getDate().toString());
				// total
				row.createCell((short) 4).setCellValue(se.getCount());
			}else if(sheetName.equals(msgs.getString("menu_resources"))){
				// resource name
				row.createCell((short) 2).setCellValue(se.getRef());
				// resource action
				row.createCell((short) 3).setCellValue(se.getRefAction());
				// most recent lastDate
				row.createCell((short) 4).setCellValue(se.getDate().toString());
				// total
				row.createCell((short) 5).setCellValue(se.getCount());
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
		sb.append(",");
		if(sheetName.equals(msgs.getString("menu_events"))){
			appendQuoted(sb, msgs.getString("th_event"));
		}else if(sheetName.equals(msgs.getString("menu_resources"))){
			appendQuoted(sb, msgs.getString("th_resource"));
			sb.append(",");
			appendQuoted(sb, msgs.getString("th_action"));
		}
		sb.append(",");
		appendQuoted(sb, msgs.getString("th_date"));
		sb.append(",");
		appendQuoted(sb, msgs.getString("th_total"));
		sb.append("\n");

		// Add the data
		Map eventNames = sm.getEventNameMap();
		Iterator i = statsObjects.iterator();
		while (i.hasNext()){
			CommonStatGrpByDate se = (CommonStatGrpByDate) i.next();
			// user name
			String userId = se.getUserId();
			String userEid = getUserEid(userId);
			appendQuoted(sb, userEid);
			sb.append(",");
			String name = getUserName(userId);
			appendQuoted(sb, name);
			sb.append(",");
			if(sheetName.equals(msgs.getString("menu_events"))){
				// event name
				appendQuoted(sb, (String) eventNames.get(se.getRef()));
				sb.append(",");
			}else if(sheetName.equals(msgs.getString("menu_resources"))){
				// resource name
				appendQuoted(sb, se.getRef());
				sb.append(",");
				// resource action
				appendQuoted(sb, se.getRefAction());
				sb.append(",");
			}
			// most recent lastDate
			appendQuoted(sb, se.getDate().toString());
			sb.append(",");
			// total
			appendQuoted(sb, Long.toString(se.getCount()));
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
}
