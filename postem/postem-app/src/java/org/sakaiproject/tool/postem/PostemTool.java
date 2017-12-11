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

package org.sakaiproject.tool.postem;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.DataFormatException;

import javax.faces.FactoryFinder;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIData;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.myfaces.shared_impl.util.MessageUtils;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class PostemTool {
	
	protected GradebookManager gradebookManager;
	protected ArrayList gradebooks;

	protected Gradebook currentGradebook;

	protected TreeMap studentMap;

	protected Gradebook oldGradebook;

	protected String userId;
	
	protected String userEid;

	protected String filename = null;

	protected String csv = null;

	protected String siteId = null;

	protected UIData gradebookTable;

	protected String title;

	protected String newTemplate;

	protected ArrayList students;
	
	protected String delimiter;
	
	protected boolean ascending = true;
	
	protected String sortBy = Gradebook.SORT_BY_TITLE;
	
	protected boolean displayErrors;

	protected boolean userPressedBack = false;
	
	protected boolean gradebooksExist = true;
	
	private static final int TEMPLATE_MAX_LENGTH = 4000;
	private static final int TITLE_MAX_LENGTH = 255;
	private static final int HEADING_MAX_LENGTH = 500;
	
	private static final String COMMA_DELIM_STR = "comma";
	private static final String TAB_DELIM_STR = "tab";

	protected StudentGrades currentStudent;

	// protected String release = "yes";

	protected String selectedStudent;

	protected boolean withHeader = true;

	protected int column = 0;

	public static final String messageBundle = "org.sakaiproject.tool.postem.bundle.Messages";
	public ResourceLoader msgs = new ResourceLoader(messageBundle);

	private ContentHostingService contentHostingService;

	private AuthzGroupService authzGroupService;

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
			
			if (userId != null) {
				try {
					userEid = UserDirectoryService.getUserEid(userId);
				} catch (UserNotDefinedException e) {
					log.error("UserNotDefinedException", e);
				}
			}
		}

		Placement placement = ToolManager.getCurrentPlacement();
		String currentSiteId = placement.getContext();

		siteId = currentSiteId;
		try {
			if (checkAccess()) {
				// logger.info("**** Getting by context!");
				gradebooks = new ArrayList(gradebookManager
						.getGradebooksByContext(siteId, sortBy, ascending));
			} else {
				// logger.info("**** Getting RELEASED by context!");
				gradebooks = new ArrayList(gradebookManager
						.getReleasedGradebooksByContext(siteId, sortBy, ascending));
			}
		} catch (Exception e) {
			gradebooks = null;
		}
		
		if (gradebooks != null && gradebooks.size() > 0)
			gradebooksExist = true;
		else
			gradebooksExist = false;

		return gradebooks;

	}
	
	public boolean getGradebooksExist() {
		return gradebooksExist;
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

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
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
	
	public boolean getDisplayErrors() {	
		return FacesContext.getCurrentInstance().getMessages().hasNext();
	}
	
	public TreeMap getStudentMap() {
		return studentMap;
	}
	
	public void setDisplayErrors(boolean displayErrors) {
		this.displayErrors = displayErrors;
	}

	public String getCurrentStudentGrades() {
		
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}
		if (currentStudent == null) {	
			return "<p>" + msgs.getFormattedMessage("no_grades_for_user", 
					new Object[]{StringEscapeUtils.escapeHtml(currentGradebook.getTitle())}) + "</p>";
		}
		return currentStudent.formatGrades();
		
	}

	public String getFirstStudentGrades() {
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}
		Set students = currentGradebook.getStudents();
		if (students.size() == 0) {
			return "<p>" + msgs.getFormattedMessage("no_grades_in_gradebook", 
					new Object[]{StringEscapeUtils.escapeHtml(currentGradebook.getTitle())}) + "</p>";
		}
		if (currentGradebook.getFirstUploadedUsername() != null) {
			StudentGrades student = currentGradebook.studentGrades(currentGradebook.getFirstUploadedUsername());
			return student.formatGrades();
		} else {
			StudentGrades student = (StudentGrades) students.iterator().next();
			return student.formatGrades();
		}
	}

	public String getSelectedStudentGrades() {
		if (currentGradebook == null) {
			return "<p>" + msgs.getString("no_gradebook_selected") + "</p>";
		}

		if (currentGradebook.getUsernames() == null || currentGradebook.getUsernames().isEmpty()) {
			return "<p>" + msgs.getFormattedMessage("no_grades_in_gradebook", 
					new Object[]{StringEscapeUtils.escapeHtml(currentGradebook.getTitle())}) + "</p>";
		}
		
		if (selectedStudent == null || selectedStudent.equals("")) {
			return msgs.getString("select_participant");
		}
		
		StudentGrades selStudent = gradebookManager.getStudentByGBAndUsername(currentGradebook, selectedStudent);
		if (selStudent != null) {
			selStudent.setGradebook(currentGradebook);
			return selStudent.formatGrades();
		} 
		
		return msgs.getString("select_participant");
	}
	
	public void toggleSort(String sortByType) {
		if (sortBy.equals(sortByType)) {
	       if (ascending) {
	    	   ascending = false;
	       } else {
	    	   ascending = true;
	       }
	    } else {
	    	sortBy = sortByType;
	    	ascending = true;
	    }
	}
	
	public String toggleTitleSort()	{
		toggleSort(Gradebook.SORT_BY_TITLE);
	    return "main";
	}
	
	public String toggleCreatorSort()	{
		toggleSort(Gradebook.SORT_BY_CREATOR);
	    return "main";
	}
	
	public String toggleModBySort()	{    
		toggleSort(Gradebook.SORT_BY_MOD_BY);
	    return "main";
	}
	    
	public String toggleModDateSort()	{    
		toggleSort(Gradebook.SORT_BY_MOD_DATE);
	    return "main";
	}
	
	public String toggleReleasedSort()	{    
		toggleSort(Gradebook.SORT_BY_RELEASED);	    
	    return "main";
	}
	
	public boolean isTitleSort() {
		if (sortBy.equals(Gradebook.SORT_BY_TITLE))
			return true;
		return false;
	}
		
	public boolean isCreatorSort() {
		if (sortBy.equals(Gradebook.SORT_BY_CREATOR))
			return true;
		return false;
	}
		
	public boolean isModBySort() {
		if (sortBy.equals(Gradebook.SORT_BY_MOD_BY))
			return true;
		return false;
	}
	
	public boolean isModDateSort() {
		if (sortBy.equals(Gradebook.SORT_BY_MOD_DATE))
			return true;
		return false;
	}
	
	public boolean isReleasedSort() {
		if (sortBy.equals(Gradebook.SORT_BY_RELEASED))
			return true;
		return false;
	}
	
	public boolean isAscending() {
		return ascending;
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
		
		Long currentGbId = ((Gradebook) gradebookTable.getRowData()).getId();
		currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(currentGbId);
		
		oldGradebook = gradebookManager.createEmptyGradebook(currentGradebook
				.getCreator(), currentGradebook.getContext());
		oldGradebook.setId(currentGradebook.getId());
		oldGradebook.setStudents(currentGradebook.getStudents());

		gradebooks = null;

		/*
		 * if(new Boolean(true).equals(currentGradebook.getReleased())) {
		 * this.release = "Yes"; } else { this.release = "No"; }
		 */
		
		if (currentGradebook.getFileReference() != null) {
			attachment = EntityManager.newReference(contentHostingService.getReference(currentGradebook.getFileReference()));
		}

		this.csv = null;
		this.newTemplate = null;
		this.delimiter = COMMA_DELIM_STR;

		return "create_gradebook";

	}
	
	public static void populateMessage(FacesMessage.Severity severity,
			String messageId, Object[] args) {
		final ResourceLoader rb = new ResourceLoader(messageBundle);
		FacesContext.getCurrentInstance().addMessage(null, 
		        new FacesMessage(rb.getFormattedMessage(messageId, args)));
	}
	
	protected static void clearMessages() {
    	Iterator iter = FacesContext.getCurrentInstance().getMessages(null);
    	while (iter.hasNext()) {
    		iter.next();
    		iter.remove();
    	}
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
					//To stay consistent, remove current messages when adding a new message
					//so as to only display one error message before returning
					PostemTool.clearMessages();
					PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
							"duplicate_title", new Object[] {});
					return "create_gradebook";
				}
			}
		}
		if (currentGradebook.getTitle() == null
				|| currentGradebook.getTitle().equals("")) {
			//To stay consistent, remove current messages when adding a new message
			//so as to only display one error message before returning
			PostemTool.clearMessages();
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "missing_title",
					new Object[] {});
			return "create_gradebook";
		}
		else if(currentGradebook.getTitle().trim().length() > TITLE_MAX_LENGTH) {
			PostemTool.clearMessages();
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "title_too_long",
					new Object[] { new Integer(currentGradebook.getTitle().trim().length()), new Integer(TITLE_MAX_LENGTH)});
			return "create_gradebook";
		}
		
		Reference attachment = getAttachmentReference();
		if (attachment == null){			
			return "create_gradebook";
		}
		
		if (!this.delimiter.equals(COMMA_DELIM_STR) && !this.delimiter.equals(TAB_DELIM_STR)) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "invalid_delim", new Object[] {});
			return "create_gradebook";
		}

		if (attachment != null) {
			// logger.info("*** Non-Empty CSV!");
			try {
				
				char csv_delim = CSV.COMMA_DELIM;
				if(this.delimiter.equals(TAB_DELIM_STR)) {
					csv_delim = CSV.TAB_DELIM;
				}
				
				//Read the data
				
				ContentResource cr = contentHostingService.getResource(attachment.getId());
				//Check the type
				if (ResourceProperties.TYPE_URL.equalsIgnoreCase(cr.getContentType())) {
					//Going to need to read from a stream
					String csvURL = new String(cr.getContent());
					//Load the URL
					csv = URLConnectionReader.getText(csvURL); 
					if (log.isDebugEnabled()) {
						log.debug(csv);
					}
				}
				else {
					csv = new String(cr.getContent());
					if (log.isDebugEnabled()) {
						log.debug(csv);
					}
				}
				CSV grades = new CSV(csv, withHeader, csv_delim);
				
				if (withHeader == true) {
					if (grades.getHeaders() != null) {

						List headingList = grades.getHeaders();
						for(int col=0; col < headingList.size(); col++) {
							String heading = (String)headingList.get(col).toString().trim();	
							// Make sure there are no blank headings
							if(heading == null || heading.equals("")) {
								PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
										"blank_headings", new Object[] {});
								return "create_gradebook";
							}
							// Make sure the headings don't exceed max limit
							if (heading.length() > HEADING_MAX_LENGTH) {
								PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, "heading_too_long", new Object[] {new Integer(HEADING_MAX_LENGTH)});
								return "create_gradebook";
							}
						}
					}
				}
				
				if (grades.getStudents() != null) {
				  if(!usernamesValid(grades)) {
					  return "create_gradebook";
				  }
				  
				  if (hasADuplicateUsername(grades)) {
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
						PostemTool.populateMessage(FacesMessage.SEVERITY_INFO,
								"has_headers", new Object[] {});
					}
				}
				if (grades.getStudents() != null) {	
					PostemTool.populateMessage(FacesMessage.SEVERITY_INFO,
							"has_students", new Object[] { new Integer(grades.getStudents()
									.size()) });
				}
				if (withHeader == true) {
					currentGradebook.setHeadings(grades.getHeaders());
				}
				List slist = grades.getStudents();

				if (oldGradebook.getId() != null && !this.userPressedBack) {
					Set oldStudents = currentGradebook.getStudents();
					oldGradebook.setStudents(oldStudents);
				}

				currentGradebook.setStudents(new TreeSet());
				// gradebookManager.saveGradebook(currentGradebook);
				Iterator si = slist.iterator();
				while (si.hasNext()) {
					List ss = (List) si.next();
					String uname = ((String) ss.remove(0)).trim();
					// logger.info("[POSTEM] processCreate -- adding student " +
					// uname);
					gradebookManager.createStudentGradesInGradebook(uname, ss,
							currentGradebook);
					if (currentGradebook.getStudents().size() == 1) {
						currentGradebook.setFirstUploadedUsername(uname);  //otherwise, the verify screen shows first in ABC order
					}
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
			} catch (IdUnusedException e) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, e
						.getMessage(), new Object[] {});
				return "create_gradebook";
			} catch (TypeException e) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, e
						.getMessage(), new Object[] {});
				return "create_gradebook";
			} catch (PermissionException e) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, e
						.getMessage(), new Object[] {});
				return "create_gradebook";
			} catch (ServerOverloadException e) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, e
						.getMessage(), new Object[] {});
				return "create_gradebook";
			} catch (IOException e) {
				PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR, e
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
		currentGradebook.setFileReference(attachment.getId());
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
		this.userPressedBack = true;
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
		Long currentGbId = ((Gradebook) gradebookTable.getRowData()).getId();
		
		// if instructor, we need to load all students
		if (isEditable()) {
			currentGradebook = gradebookManager.getGradebookByIdWithHeadings(currentGbId);
			currentGradebook.setUsernames(gradebookManager.getUsernamesInGradebook(currentGradebook));
			studentMap = currentGradebook.getStudentMap();
			setSelectedStudent((String) studentMap.firstKey());
			return "view_student";
		}
		
		// otherwise, just load what we need for the current user
		currentGradebook = gradebookManager.getGradebookByIdWithHeadings(currentGbId);
		this.userId = SessionManager.getCurrentSessionUserId();
		
		currentStudent = gradebookManager.getStudentByGBAndUsername(currentGradebook, this.userEid);
		if (currentStudent != null) {
			currentStudent.setLastChecked(new Timestamp(new Date().getTime()));
			gradebookManager.updateStudent(currentStudent);
			
			currentStudent.setGradebook(currentGradebook);
		}
		
		return "view_grades";
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
		Long currentGbId = ((Gradebook) gradebookTable.getRowData()).getId();
		currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(currentGbId);
		
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
		Long currentGbId = ((Gradebook) gradebookTable.getRowData()).getId();
		currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(currentGbId);

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
		Long currentGbId = ((Gradebook) gradebookTable.getRowData()).getId();
		currentGradebook = gradebookManager.getGradebookByIdWithHeadingsAndStudents(currentGbId);

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

	private Boolean editable;

	private List filePickerList;

	private String currentRediredUrl;

	private Reference attachment;

	public boolean isEditable() {
		if (editable == null) {
			editable = checkAccess();
		}
		return editable;

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
	
	private boolean hasADuplicateUsername(CSV studentGrades) {
		List usernameList = studentGrades.getStudentUsernames();
		List duplicatesList = new ArrayList();
		
		while (usernameList.size() > 0) {
			String username = (String)usernameList.get(0);
			usernameList.remove(username);
			if (usernameList.contains(username)
					&& !duplicatesList.contains(username)) {
				duplicatesList.add(username);
			}
		}
		
		if (duplicatesList.size() <= 0) {
			return false;
		}
		
		if (duplicatesList.size() == 1) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"single_duplicate_username", new Object[] { });
		} else {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"mult_duplicate_usernames", new Object[] { });
		}
		
		for (int i=0; i < duplicatesList.size(); i++) {
			PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
					"duplicate_username", new Object[] { duplicatesList.get(i) });
		}
		
		PostemTool.populateMessage(FacesMessage.SEVERITY_ERROR,
				"duplicate_username_dir", new Object[] { });
		
		return true;
	}
	
	private boolean usernamesValid(CSV studentGrades) {
		boolean usersAreValid = true;
		List blankRows = new ArrayList();
		List invalidUsernames = new ArrayList();
		int row=1;
		
		List siteMembers = getSiteMembers();
		
		List studentList = studentGrades.getStudentUsernames();
		Iterator studentIter = studentList.iterator();
		while (studentIter.hasNext()) {
			row++;
			String usr = (String) studentIter.next();
			
			if (log.isDebugEnabled()) {
				log.debug("usernamesValid : username=" + usr);
				log.debug("usernamesValid : siteMembers" + siteMembers);
			}
			if (usr == null || usr.equals("")) {

				usersAreValid = false;
				blankRows.add(new Integer(row));
			} else if(siteMembers == null || (siteMembers != null && !siteMembers.contains(getUserDefined(usr)))) {
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
					"single_invalid_username_dir", new Object[] { });
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
			realm = authzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
			return realm.getUsers().contains(uid);
		}
		catch (GroupNotDefinedException e) {
			log.error("IdUnusedException:", e);
		}		
		return false;
	}
	
	private String getCurrentSiteId()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		return placement.getContext();
	}
	
	//Returns getUser and getUserByEid on the input string
	//@return Either the id of the user, or the same string if not defined
	private String getUserDefined(String usr)
	{
		//Set the original user id
		String userId = usr;
		User userinfo;
		try	{
			userinfo = UserDirectoryService.getUser(usr);
			userId = userinfo.getId();
			if (log.isDebugEnabled()) {
				log.debug("getUserDefined: username for " + usr + " is " + userId);
			}
			return userId;
		} 
		catch (UserNotDefinedException e) {
			try
			{
				// try with the user eid
				userinfo = UserDirectoryService.getUserByEid(usr);
				userId = userinfo.getId();
			}
			catch (UserNotDefinedException ee)
			{
				//This is mostly expected behavior, don't need to notify about it, the UI can handle it
				if (log.isDebugEnabled()) {
					log.debug("getUserDefined: User Not Defined" + userId);
				}
			}
		}
		return userId;
	}
	
	private List getSiteMembers() {
		List siteMembers = new ArrayList();
		try	{
			AuthzGroup realm = authzGroupService.getAuthzGroup("/site/" + getCurrentSiteId());
			siteMembers = new ArrayList(realm.getUsers());
		}
		catch (GroupNotDefinedException e) {
			log.error("GroupNotDefinedException:", e);
		}
		
		return siteMembers;
	}
	
	  public String processAddAttachRedirect()
	  {
	    try
	    {
	      filePickerList = EntityManager.newReferenceList();
	      ToolSession currentToolSession = SessionManager.getCurrentToolSession();
	      currentToolSession.setAttribute("filepicker.attach_cardinality",FilePickerHelper.CARDINALITY_SINGLE);	      
	      currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);
	      ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
	      context.redirect("sakai.filepicker.helper/tool");
	      return null;
}
	    catch(Exception e)
	    {
	      log.error(this + ".processAddAttachRedirect - " + e);
	      return null;
	    }
	  }

	  public String getCurrentRediredUrl()
	  {
	    return currentRediredUrl;
	  }

	  public void setCurrentRediredUrl(String currentRediredUrl)
	  {
	    this.currentRediredUrl = currentRediredUrl;
	  }

	  public Reference getAttachmentReference()
	  {
	    ToolSession session = SessionManager.getCurrentToolSession();
	    if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null &&
	        session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null)
	    {
	      List refs = (List)session.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
	      Reference ref = null;

	      if (refs.size() == 1)
	      {
	        ref = (Reference) refs.get(0);
            attachment=ref;
	        }
	      }
	    session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
	    session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
	    if(filePickerList != null)
	      filePickerList.clear();

	    return attachment;
	  }
	  
	  public String getAttachmentTitle () {
		  return getReferenceTitle(getAttachmentReference());
	  }
	  
	  public void setAttachment(Reference attachment)
	  {
	    this.attachment = attachment;
	  }

	  public String getReferenceTitle (Reference ref) {
		  if (ref != null && ref.getProperties() != null) {
			  return (String) ref.getProperties().getProperty(ref.getProperties().getNamePropDisplayName());
}
		  return null;
	  }
}
