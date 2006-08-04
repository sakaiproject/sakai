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

package org.sakaiproject.tool.postem;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.util.MessageUtils;
import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.StudentGrades;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

public class PostemTool {
	protected GradebookManager gradebookManager;
	
	// protected Logger logger = Logger.getLogger(PostemTool.class);

	protected ArrayList gradebooks;

	protected Gradebook currentGradebook;

	protected Gradebook oldGradebook;

	protected String userId;

	protected String filename = null;

	protected String csv = null;

	protected String siteId = null;

	protected UIData gradebookTable;

	protected String title;

	protected String editAble;

	protected boolean editable;

	protected String newTemplate;

	protected ArrayList students;
	
	protected String delimiter;
	
	private static final int TEMPLATE_MAX_LENGTH = 4000;
	
	private static final String COMMA_DELIM_STR = "comma";
	private static final String TAB_DELIM_STR = "tab";

	protected Logger logger = null;

	// protected String release = "yes";

	protected String selectedStudent;

	protected boolean withHeader = true;

	protected int column = 0;

	public static final String messageBundle = "org.sakaiproject.tool.postem.bundle.Messages";
	public ResourceLoader msgs = new ResourceLoader(messageBundle);

	private static final Log LOG = LogFactory.getLog(PostemTool.class);
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	/*
	 * public void setRelease(String release) { this.release = release; }
	 * 
	 * public String getRelease() { return release; }
	 */
	public ArrayList getGradebooks() {
		if (userId == null) {
			userId = SessionManager.getCurrentSessionUserId();
		}

		Placement placement = ToolManager.getCurrentPlacement();
		String currentSiteId = placement.getContext();

		siteId = currentSiteId;
		try {
			if (checkAccess()) {
				// logger.info("**** Getting by context!");
				gradebooks = new ArrayList(gradebookManager
						.getGradebooksByContext(siteId));
			} else {
				// logger.info("**** Getting RELEASED by context!");
				gradebooks = new ArrayList(gradebookManager
						.getReleasedGradebooksByContext(siteId));
			}
		} catch (Exception e) {
			gradebooks = null;
		}

		return gradebooks;

	}

	public String getCsv() {
		return csv;
	}

	public void setCsv(String csv) {
		this.csv = csv;
	}

	public String getDelimiter() {
		return delimiter;
	}
	
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public String getNewTemplate() {
		return newTemplate;
	}

	public void setNewTemplate(String newTemplate) {
		this.newTemplate = newTemplate;
	}

	public GradebookManager getGradebookManager() {
		return this.gradebookManager;
	}

	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}

	public UIData getGradebookTable() {
		return gradebookTable;
	}

	public void setGradebookTable(UIData gradebookTable) {
		this.gradebookTable = gradebookTable;
	}

	public Gradebook getCurrentGradebook() {
		return currentGradebook;
	}

	public void setCurrentGradebook(Gradebook currentGradebook) {
		this.currentGradebook = currentGradebook;
	}

	public String getSelectedStudent() {
		return selectedStudent;
	}

	public void setSelectedStudent(String selectedStudent) {
		this.selectedStudent = selectedStudent;
	}

	public boolean getWithHeader() {
		return withHeader;
	}

	public void setWithHeader(boolean withHeader) {
		this.withHeader = withHeader;
	}

	public String getCurrentStudentGrades() {
		this.userId = SessionManager.getCurrentSessionUserId();
		/*
		 * if (checkAccess()) { if (currentGradebook.getTemplate() != null) { return
		 * currentGradebook.getTemplate().getTemplateCode(); } else { return "<p>No
		 * template currently exists for this gradebook.</p>"; } }
		 */
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}
		if (!currentGradebook.hasStudent(this.userId)) {
			return "<p>" + msgs.getString("no_grades_for_user") + " " + currentGradebook.getTitle() + ".</p>";
		}
		return currentGradebook.studentGrades(this.userId).formatGrades();
	}

	public String getFirstStudentGrades() {
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}
		Set students = currentGradebook.getStudents();
		if (students.size() == 0) {
			return "<p>" + msgs.getString("no_grades_in_gradebook") + " " + currentGradebook.getTitle() + ".</p>";
		}
		StudentGrades student = (StudentGrades) students.iterator().next();
		return student.formatGrades();
	}

	public String getSelectedStudentGrades() {
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}
		Set students = currentGradebook.getStudents();
		if (students.size() == 0) {
			return "<p>" + msgs.getString("no_grades_in_gradebook") + " " + currentGradebook.getTitle() + ".</p>";
		}
		if (selectedStudent == null) {
			return msgs.getString("select_participant");
		}

		Iterator iter = students.iterator();
		while (iter.hasNext()) {
			StudentGrades student = (StudentGrades) iter.next();
			if (selectedStudent.equals(student.getUsername())) {
				return student.formatGrades();
			}
		}
		return msgs.getString("select_participant");
	}

	public String processCreateNew() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		this.userId = SessionManager.getCurrentSessionUserId();
		this.siteId = ToolManager.getCurrentPlacement().getContext();
		this.currentGradebook = gradebookManager.createEmptyGradebook(this.userId,
				this.siteId);
		this.oldGradebook = gradebookManager.createEmptyGradebook(this.userId,
				this.siteId);
		this.csv = null;
		this.newTemplate = null;
		this.delimiter = COMMA_DELIM_STR;

		return "create_gradebook";
	}

	public String processGradebookUpdate() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			this.currentGradebook = null;
			this.csv = null;
			this.newTemplate = null;
			return "permission_error";
		}
		this.userId = SessionManager.getCurrentSessionUserId();
		this.siteId = ToolManager.getCurrentPlacement().getContext();
		currentGradebook = (Gradebook) gradebookTable.getRowData();
		oldGradebook = gradebookManager.createEmptyGradebook(currentGradebook
				.getCreator(), currentGradebook.getContext());
		oldGradebook.setId(currentGradebook.getId());

		gradebooks = null;

		/*
		 * if(new Boolean(true).equals(currentGradebook.getReleased())) {
		 * this.release = "Yes"; } else { this.release = "No"; }
		 */
		this.csv = null;
		this.newTemplate = null;
		this.delimiter = COMMA_DELIM_STR;

		return "create_gradebook";

	}

	public static void populateMessage(FacesMessage.Severity severity,
			String messageId, Object[] args) {
		ApplicationFactory factory = (ApplicationFactory) FactoryFinder
				.getFactory(FactoryFinder.APPLICATION_FACTORY);
		factory.getApplication().setMessageBundle(messageBundle);
		FacesContext.getCurrentInstance().addMessage(null,
				MessageUtils.getMessage(severity, messageId, args));
	}

	public String processCreate() {

		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			this.currentGradebook = null;
			this.csv = null;
			this.newTemplate = null;
			// this.release = null;
			return "permission_error";
		}
		if (currentGradebook.getId() == null) {
			ArrayList gb = getGradebooks();
			Iterator gi = gb.iterator();
			while (gi.hasNext()) {
				if (((Gradebook) gi.next()).getTitle().equals(
						currentGradebook.getTitle())) {
					PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
							"duplicate_title", new Object[] {});
					return "create_gradebook";
				}
			}
		}
		if (currentGradebook.getTitle() == null
				|| currentGradebook.getTitle().equals("")) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "missing_title",
					new Object[] {});
			return "create_gradebook";
		}
		
		if (this.csv == null || this.csv.trim().length() <= 0) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "missing_csv", new Object[] {});
			return "create_gradebook";
		}
		
		if (!this.delimiter.equals(COMMA_DELIM_STR) && !this.delimiter.equals(TAB_DELIM_STR)) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "invalid_delim", new Object[] {});
			return "create_gradebook";
		}

		if (this.csv != null && this.csv.trim().length() > 0) {
			// logger.info("*** Non-Empty CSV!");
			try {
				
				char csv_delim = CSV.COMMA_DELIM;
				if(this.delimiter.equals(TAB_DELIM_STR)) {
					csv_delim = CSV.TAB_DELIM;
				}
				
				CSV grades = new CSV(csv, withHeader, csv_delim);
				
				if (withHeader == true) {
					if (grades.getHeaders() != null) {
						// Make sure there are no blank headings
						List headingList = grades.getHeaders();
						for(int col=0; col < headingList.size(); col++) {
							String heading = (String)headingList.get(col).toString().trim();			
							if(heading.equals("") || heading == null) {
								PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
										"blank_headings", new Object[] {});
								return "create_gradebook";
							}
						}
					}
				}
				
				if (grades.getStudents() != null) {
				  if(!usernamesValid(grades)) {
					  return "create_gradebook";
				  }
				}
				
				if (this.newTemplate != null && this.newTemplate.trim().length() > 0) {
					if(this.newTemplate.trim().length() > TEMPLATE_MAX_LENGTH) {
						PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "template_too_long",
								new Object[] { new Integer(this.newTemplate.trim().length()), new Integer(TEMPLATE_MAX_LENGTH)});
						return "create_gradebook";
					}
				}
				
				if (withHeader == true) {
					if (grades.getHeaders() != null) {	
						PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
								"has_headers", new Object[] {});
					}
				}
				if (grades.getStudents() != null) {	
					PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
							"has_students", new Object[] { new Integer(grades.getStudents()
									.size()) });
				}
				if (withHeader == true) {
					currentGradebook.setHeadings(grades.getHeaders());
				}
				List slist = grades.getStudents();

				if (oldGradebook.getId() != null) {
					Set oldStudents = currentGradebook.getStudents();
					oldGradebook.setStudents(oldStudents);
				}

				currentGradebook.setStudents(new TreeSet());
				// gradebookManager.saveGradebook(currentGradebook);
				Iterator si = slist.iterator();
				while (si.hasNext()) {
					List ss = (List) si.next();
					String uname = (String) ss.remove(0);
					// logger.info("[POSTEM] processCreate -- adding student " +
					// uname);
					gradebookManager.createStudentGradesInGradebook(uname, ss,
							currentGradebook);
				}
			} catch (DataFormatException exception) {
				/*
				 * TODO: properly subclass exception in order to allow for localized
				 * messages (add getRowNumber/setRowNumber). Set exception message to be
				 * key in .properties file
				 */
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, exception
						.getMessage(), new Object[] {});
				return "create_gradebook";
			}
		} else if (this.csv != null) {
			// logger.info("**** Non Null Empty CSV!");
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "has_students",
					new Object[] { new Integer(0) });
			currentGradebook.setHeadings(new ArrayList());
			if (oldGradebook.getId() != null) {
				Set oldStudents = currentGradebook.getStudents();
				oldGradebook.setStudents(oldStudents);
			}

			currentGradebook.setStudents(new TreeSet());
		}

		if (this.newTemplate != null && this.newTemplate.trim().length() > 0) {
			currentGradebook
					.setTemplate(gradebookManager.createTemplate(newTemplate.trim()));
		} else if (this.newTemplate != null) {
			// logger.info("*** Non Null Empty Template!");
			currentGradebook.setTemplate(null);
		}

		/*
		 * if("No".equals(this.release)) { currentGradebook.setReleased(new
		 * Boolean(false)); //logger.info("Set to No, " +
		 * currentGradebook.getReleased()); } else {
		 * currentGradebook.setReleased(new Boolean(true)); //logger.info("Set to
		 * Yes, " + currentGradebook.getReleased()); }
		 */

		// gradebookManager.saveGradebook(currentGradebook);
		// logger.info(currentGradebook.getId());
		// currentGradebook = null;
		if ((this.csv != null && this.csv.trim().length() > 0)
				|| (this.newTemplate != null && this.newTemplate.trim().length() > 0)) {
			this.csv = null;
			this.newTemplate = null;
			return "verify";
		}

		Iterator oi = oldGradebook.getStudents().iterator();
		while (oi.hasNext()) {
			gradebookManager.deleteStudentGrades((StudentGrades) oi.next());
		}
		this.userId = SessionManager.getCurrentSessionUserId();
		currentGradebook.setLastUpdated(new Timestamp(new Date().getTime()));
		currentGradebook.setLastUpdater(this.userId);
		gradebookManager.saveGradebook(currentGradebook);

		this.currentGradebook = null;
		this.oldGradebook = null;
		this.withHeader = true;
		// this.gradebooks = null;
		return "main";
	}

	public String processCreateOk() {
		Iterator oi = oldGradebook.getStudents().iterator();
		while (oi.hasNext()) {
			gradebookManager.deleteStudentGrades((StudentGrades) oi.next());
		}
		this.userId = SessionManager.getCurrentSessionUserId();
		currentGradebook.setLastUpdated(new Timestamp(new Date().getTime()));
		currentGradebook.setLastUpdater(this.userId);
		gradebookManager.saveGradebook(currentGradebook);

		this.currentGradebook = null;
		this.oldGradebook = null;
		this.withHeader = true;
		// this.gradebooks = null;
		return "main";
	}

	public String processCreateBack() {
		if (currentGradebook.getId() == null) {
			this.csv = null;
			currentGradebook.setStudents(null);
		}
		return "create_gradebook";
	}

	public String processCancelNew() {
		this.currentGradebook = null;
		this.csv = null;
		this.newTemplate = null;
		return "main";
	}

	public String processCancelView() {
		currentGradebook = null;
		csv = null;
		newTemplate = null;
		students = null;
		return "main";
	}

	public String processGradebookView() {
		currentGradebook = (Gradebook) gradebookTable.getRowData();
		// logger.info("[POSTEM] processGradebookView -- " +
		// currentGradebook.getTitle());
		this.userId = SessionManager.getCurrentSessionUserId();
		if (currentGradebook.hasStudent(this.userId)) {
			currentGradebook.studentGrades(this.userId).setLastChecked(
					new Timestamp(new Date().getTime()));
			gradebookManager.saveGradebook(currentGradebook);
		}
		if (checkAccess()) {
			TreeMap ss = currentGradebook.getStudentMap();
			setSelectedStudent((String) ss.firstKey());
			return "view_student";
		} else {
			return "view_grades";
		}
	}

	public String processInstructorView() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		currentGradebook = (Gradebook) gradebookTable.getRowData();
		students = new ArrayList(currentGradebook.getStudents());

		return "view_gradebook";
	}

	public String processGradebookDelete() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		currentGradebook = (Gradebook) gradebookTable.getRowData();

		return "delete_confirm";

	}

	public String processDelete() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		gradebookManager.deleteGradebook(currentGradebook);
		gradebooks = null;
		currentGradebook = null;
		csv = null;
		newTemplate = null;
		students = null;
		return "main";
	}

	public String processCsvDownload() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		currentGradebook = (Gradebook) gradebookTable.getRowData();

		List csvContents = new ArrayList();
		if (currentGradebook.getHeadings().size() > 0) {
			csvContents.add(currentGradebook.getHeadings());
		}
		Iterator si = currentGradebook.getStudents().iterator();
		while (si.hasNext()) {
			List sgl = new ArrayList();
			StudentGrades sg = (StudentGrades) si.next();
			sgl.add(sg.getUsername());
			sgl.addAll(sg.getGrades());
			csvContents.add(sgl);
		}

		CSV newCsv = new CSV(csvContents, currentGradebook.getHeadings().size() > 0);

		this.csv = newCsv.getCsv();
		return "download_csv";
	}

	public String processTemplateDownload() {
		try {
			if (!this.checkAccess()) {
				throw new PermissionException(SessionManager.getCurrentSessionUserId(),
						"syllabus_access_athz", "");
			}

		} catch (PermissionException e) {
			// logger.info(this + ".getEntries() in PostemTool " + e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					MessageUtils.getMessage(FacesMessage.SEVERITY_ERROR,
							"error_permission", (new Object[] { e.toString() }), FacesContext
									.getCurrentInstance()));
			return "permission_error";
		}
		currentGradebook = (Gradebook) gradebookTable.getRowData();

		return "download_template";

	}

	public ArrayList getStudents() {
		return students;

	}

	public String getTitle() {
		return ToolManager.getCurrentTool().getTitle();
	}

	public boolean getEditable() {
		/*
		 * if(checkAccess()) { this.editable = "true"; } else { this.editable =
		 * null; } return this.editable;
		 */
		// return true;
		return checkAccess();

	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean checkAccess() {
		// return true;
		return SiteService.allowUpdateSite(ToolManager.getCurrentPlacement()
				.getContext());
	}

	// perhaps this should be moved to GradebookImpl
	public Map getStatsColumns() {

		Map columns = new TreeMap();

		if (currentGradebook == null) {
			return columns;
		}
		Set students = currentGradebook.getStudents();
		if (students.size() == 0) {
			return columns;
		}
		StudentGrades student = (StudentGrades) students.iterator().next();

		int size = student.getGrades().size();
		for (int current = 0; current < size; current++) {
			Column nc = new Column(currentGradebook, current);
			// logger.info("** Checking if column " + current + " is stat-able!");
			if (nc.getSummary() != null) {
				if (nc.getHasName()) {
					columns.put(nc.getName(), new Integer(current));
				} else {
					columns.put(Integer.toString(current), new Integer(current));
				}
			}
		}
		return columns;
	}

	public Column getCurrentColumn() {
		return new Column(currentGradebook, column);
	}
	
	public boolean usernamesValid(CSV studentGrades) {
		boolean usersAreValid = true;
		List blankRows = new ArrayList();
		List invalidUsernames = new ArrayList();
		int row=1;
		
		List studentList = studentGrades.getStudents();
		Iterator studentIter = studentList.iterator();
		while (studentIter.hasNext()) {
			row++;
			List s1 = (List) studentIter.next();
			String usr = ((String) s1.get(0)).trim();
			
			if(usr.equals("") || usr == null) {
				usersAreValid = false;
				blankRows.add(new Integer(row));
			} else if(!isSiteMember(getUserId(usr))) {
				  usersAreValid = false;
				  invalidUsernames.add(usr);
			}	
		}
		
		if (blankRows.size() == 1) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"missing_single_username", new Object[] { });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"missing_location", new Object[] { blankRows.get(0) });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"missing_username_dir", new Object[] { });
		} else if (blankRows.size() > 1) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"missing_mult_usernames", new Object[] { });
			for(int i=0; i < blankRows.size(); i++) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
						"missing_location", new Object[] { blankRows.get(i) });
			}
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"missing_username_dir", new Object[] { });
		}
		
		if (invalidUsernames.size() == 1) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"blank", new Object[] { });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"single_invalid_username", new Object[] { });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"invalid_username", new Object[] { invalidUsernames.get(0) });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"single_invalid_username_dir", new Object[] { invalidUsernames.get(0) });
		} else if (invalidUsernames.size() > 1) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"blank", new Object[] { });
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"mult_invalid_usernames", new Object[] { });
			for(int j=0; j < invalidUsernames.size(); j++) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
						"invalid_username", new Object[] { invalidUsernames.get(j) });
			}	
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"mult_invalid_usernames_dir", new Object[] { });
		}
	  return usersAreValid;
	}
	
	private boolean isSiteMember(String uid)
	{
		AuthzGroup realm;
		try	{
			realm = AuthzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
			return realm.getUsers().contains(uid);
		}
		catch (GroupNotDefinedException e) {
			LOG.error("IdUnusedException:", e);
		}		
		return false;
	}
	
	private String getCurrentSiteId()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getContext();
	}
	
	private String getUserId(String usr)
	{
		try	{
			return UserDirectoryService.getUserId(usr);
		} 
		catch (UserNotDefinedException e) {
			return usr;
		}
	}
}
