/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

@Slf4j
public class SpreadsheetUploadBean extends GradebookDependentBean implements Serializable {

    private String title;
    private UploadedFile upFile;
    private String pickedFileReference;
    private String pickedFileDesc;
    private Spreadsheet spreadsheet;
    private Map rosterMap;
    private List assignmentList;
    private List studentRows;
    private List assignmentHeaders;
    private Map selectedAssignment;
    private List assignmentColumnSelectItems;
    private boolean saved = false;
    private String columnCount;
    private String rowCount;
    private boolean hasUnknownUser;
    private boolean hasUnknownAssignments;
    private Long spreadsheetId;
    private Map scores;
    private GradebookAssignment assignment;
    private Long assignmentId;
    private Integer selectedCommentsColumnId = 0;
    private List categoriesSelectList;
    private List extraCreditCategories;
    private Boolean extraCreditCatSelected = Boolean.FALSE;
    private Category assignmentCategory;
    private String selectedCategory;
    private Gradebook localGradebook;
    private StringBuilder externallyMaintainedImportMsg;
    private UIComponent uploadButton;
    private String csvDelimiter;
    private NumberFormat numberFormat;
    private boolean selectedCategoryDropsScores;
    private String date_entry_format_description;

    // Used for bulk upload of gradebook items
    // Holds list of unknown user ids
    private List unknownUsers = new ArrayList();
    private List unknownAssignments = new ArrayList();

    private static final String POINTS_POSSIBLE_STRING = "export_points_possible";
    private static final String CUMULATIVE_GRADE_STRING = "roster_course_grade_column_name";
    private static final String IMPORT_SUCCESS_STRING = "import_entire_success";
    private static final String IMPORT_SOME_SUCCESS_STRING = "import_entire_some_success";
    private static final String IMPORT_NO_CHANGES = "import_entire_no_changes";
    private static final String IMPORT_ASSIGNMENT_NOTSUPPORTED= "import_assignment_entire_notsupported";
    private static final String IMPORT_ASSIGNMENT_NEG_VALUE = "import_assignment_entire_negative_score";
    private static final int MAX_FILE_PICKER_UPLOADS = 1;
    static final String PICKED_FILE_REFERENCE = "pickedFileReference";
    static final String PICKED_FILE_DESC = "pickedFileDesc";
    static final String IMPORT_TITLE = "gradebookImportTitle";
    
    public static final String UNASSIGNED_CATEGORY = "unassigned";

    /**
     * Property set via sakai.properties to limit the file size allowed for spreadsheet
     * uploads. This property is in MB and defaults to 1 MB.
     */
    public static final String PROPERTY_FILE_SIZE_MAX = "gradebook.upload.max";

    /**
     * The default max file size, in MB, for a spreadsheet upload.
     */
    public static final int FILE_SIZE_DEFAULT = 1;

    /**
     * If an upload contains more than the number of students in the class plus
     * this value, the upload will fail.
     */
    private static final int MAX_NUM_ROWS_OVER_CLASS_SIZE = 50;

    private String pageName;

    public SpreadsheetUploadBean() {

    }

    public void init() {

    	localGradebook = getGradebook();

        initializeRosterMap();      

        if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new GradebookAssignment();
				assignment.setReleased(true);
			}
		}
        
		// initialization; shouldn't enter here after category drop down changes
        if(assignmentCategory == null && !getLocalizedString("cat_unassigned").equalsIgnoreCase(selectedCategory)) {
            Category assignCategory = assignment.getCategory();
            if (assignCategory != null) {
            	selectedCategory = assignCategory.getId().toString();
                selectedCategoryDropsScores = assignCategory.isDropScores();
                assignCategory.setAssignmentList(retrieveCategoryAssignmentList(assignCategory));
                assignmentCategory = assignCategory;
            }
            else {
            	selectedCategory = getLocalizedString("cat_unassigned");
            }
        }

        if (selectedCategory==null)
            selectedCategory = AssignmentBean.UNASSIGNED_CATEGORY;
        categoriesSelectList = new ArrayList();
        //create comma seperate string representation of the list of EC categories
        extraCreditCategories = new ArrayList();
		// The first choice is always "Unassigned"
		categoriesSelectList.add(new SelectItem(AssignmentBean.UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		List gbCategories = getViewableCategories();
		if (gbCategories != null && gbCategories.size() > 0)
		{
			Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				Category cat = (Category) catIter.next();
				categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
				if(cat.isExtraCredit()){
					extraCreditCategories.add(cat.getId().toString());
				}
			}
		}
		DateFormat df = DateFormat.getDateInstance( DateFormat.SHORT, (new ResourceLoader()).getLocale() ); 
		date_entry_format_description = ((SimpleDateFormat)df).toPattern();

    }

    private void initializeRosterMap() {
      //initialize rosteMap which is map of displayid and user objects
        rosterMap = new HashMap();
        List  enrollments = new ArrayList(findMatchingEnrollmentsForAllItems(null, null).keySet());
        if(log.isDebugEnabled()) log.debug("enrollment size " +enrollments.size());

        Iterator iter;
        iter = enrollments.iterator();
        while(iter.hasNext()){
            EnrollmentRecord enr;
            enr = (EnrollmentRecord)iter.next();
            if(log.isDebugEnabled()) log.debug("displayid "+enr.getUser().getDisplayId() + "  userid "+enr.getUser().getUserUid());
            rosterMap.put(enr.getUser().getDisplayId().toLowerCase(),enr.getUser());
        }
    }

    public String getTitle() {
        if (title == null || "".equals(title)) {
            // check the session attribute
            ToolSession session = SessionManager.getCurrentToolSession();
            if (session.getAttribute(IMPORT_TITLE) != null) {
                title = (String) session.getAttribute(IMPORT_TITLE);
            }
        }
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UploadedFile getUpFile() {
        return upFile;
    }

    public void setUpFile(UploadedFile upFile) {
        if(log.isDebugEnabled()) log.debug("upload file name " + upFile.getName());
        this.upFile = upFile;
    }


    public Spreadsheet getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public Map getRosterMap() {
        return rosterMap;
    }

    public void setRosterMap(Map rosterMap) {
        this.rosterMap = rosterMap;
    }
    public Long getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(Long spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }


    public List getSpreadsheets() {
        this.setPageName("spreadsheetListing");
        return getGradebookManager().getSpreadsheets(getGradebookId());
    }

    public String deleteItem(){
        return "spreadsheetRemove";
    }

    public List getAssignmentList() {
        return assignmentList;
    }

    public void setAssignmentList(List assignmentList) {
        this.assignmentList = assignmentList;
    }

    public List getStudentRows() {
        return studentRows;
    }

    public void setStudentRows(List studentRows) {
        this.studentRows = studentRows;
    }

    public List getAssignmentHeaders() {
        return assignmentHeaders;
    }

    public void setAssignmentHeaders(List assignmentHeaders) {
        this.assignmentHeaders = assignmentHeaders;
    }

    public Map getSelectedAssignment() {
        return selectedAssignment;
    }

    public void setSelectedAssignment(Map selectedAssignment) {
        this.selectedAssignment = selectedAssignment;
    }

    public List getAssignmentColumnSelectItems() {
        return assignmentColumnSelectItems;
    }

    public void setAssignmentColumnSelectItems(List assignmentColumnSelectItems) {
        this.assignmentColumnSelectItems = assignmentColumnSelectItems;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Returns formatted string with count of assignments imported (columns - 1 {student name})
     */
    public String getVerifyColumnCount() {
    	final int intColumnCount = new Integer(columnCount == null ? "0" : columnCount).intValue() - 1;
        return FacesUtil.getLocalizedString("import_verify_column_count",new String[] {Integer.toString(intColumnCount)});
    }

    public String getColumnCount() {
        return FacesUtil.getLocalizedString("upload_preview_column_count",new String[] {columnCount});
    }

    public void setColumnCount(String columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Returns formatted string with count of number of students imported
     * Added points possible row so need to subtract one from count
     */
    public String getVerifyRowCount() {
        return FacesUtil.getLocalizedString("import_verify_row_count",new String[] {rowCount});
    }

    public String getRowCount() {
        return FacesUtil.getLocalizedString("upload_preview_row_count",new String[] {rowCount});
    }

    public void setRowCount(String rowCount) {
        this.rowCount = rowCount;
    }


    public String getRowStyles() {
    	StringBuilder sb = new StringBuilder();
    	if (studentRows != null) {
    		for(Iterator iter = studentRows.iterator(); iter.hasNext();){
    			SpreadsheetRow row = (SpreadsheetRow)iter.next();
    			if(row.isKnown()){
    				sb.append("internal,");
    			}else{
    				sb.append("external,");
    			}
    		}
    	}
    	if(log.isDebugEnabled())log.debug(sb.toString());
        return sb.toString();
    }

    /**
     * Returns List of unknown user names
     */
    public boolean getHasUnknownUser() {
        return hasUnknownUser;
    }

    public Map getScores() {
        return scores;
    }

    public void setScores(Map scores) {
        this.scores = scores;
    }

    public GradebookAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(GradebookAssignment assignment) {
        this.assignment = assignment;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * Returns list of unknown users
     */
    public List getUnknownUsers() {
		return unknownUsers;
	}

    /**
     * Sets the list of unknown users
     */
	public void setUnknownUsers(List unknownUsers) {
		this.unknownUsers = unknownUsers;
	}

	public boolean isHasUnknownAssignments() {
		return hasUnknownAssignments;
	}

	public void setHasUnknownAssignments(boolean hasUnknownAssignments) {
		this.hasUnknownAssignments = hasUnknownAssignments;
	}

	public List getUnknownAssignments() {
		return unknownAssignments;
	}

	public void setUnknownAssignments(List unknownAssignments) {
		this.unknownAssignments = unknownAssignments;
	}

	/**
	 * Returns the count of unknown users
	 */
	public int getUnknownSize() {
		this.setPageName("spreadsheetAll");
		return unknownUsers.size();
	}

	public List getCategoriesSelectList() {
    	return categoriesSelectList;
    }

    public String getSelectedCategory() {
    	return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
    	this.selectedCategory = selectedCategory;
    }

    public Gradebook getLocalGradebook() {
    	return localGradebook;
    }

    /**
	 * For retaining the pageName variable throughout import process
	 */
	public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getExternallyMaintainedImportMsg() {
    	if (externallyMaintainedImportMsg == null || externallyMaintainedImportMsg.length() < 1)
    		return null;

    	return externallyMaintainedImportMsg.toString();
    }

    /**
     * Returns the Category associated with selectedCategory
     * If unassigned or not found, returns null
     * @return
     */
    private Category retrieveSelectedCategory() {
    	Long catId = null;
    	Category category = null;

		if (selectedCategory != null && !selectedCategory.equals(AssignmentBean.UNASSIGNED_CATEGORY)) {
			try {
				catId = new Long(selectedCategory);
			}
			catch (Exception e) {
				catId = null;
			}

			if (catId != null)
			{
				// check to make sure there is a corresponding category
				category = getGradebookManager().getCategory(catId);

				//populate assignments list
				category.setAssignmentList(retrieveCategoryAssignmentList(category));
			}
		}
		
		return category;
    }    
    
    private List retrieveCategoryAssignmentList(Category cat){
    	List assignmentsToUpdate = new ArrayList();
    	if(cat != null){
    		List assignments = cat.getAssignmentList();
    		if(cat.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in category (to improve performance)
    			assignments = getGradebookManager().getAssignmentsForCategory(cat.getId());

    			// only include assignments which are not adjustments must not update adjustment item pointsPossible
    			for(Object o : assignments) { 
    				if(o instanceof GradebookAssignment) {
    					GradebookAssignment assignment = (GradebookAssignment)o;
    					if(!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
    						assignmentsToUpdate.add(assignment);
    					}
    				}
    			}
    		}
    	}
		return assignmentsToUpdate;
    }

	//view file from db
    public String viewItem(){

        if(rosterMap == null){
    	    init();
        }
    	
        if(log.isDebugEnabled())log.debug("loading viewItem()");

        org.sakaiproject.tool.gradebook.Spreadsheet sp = getGradebookManager().getSpreadsheet(spreadsheetId);
        StringBuilder sb = new StringBuilder();
        sb.append(sp.getContent());

        List contents = new ArrayList();

        String lineitems[] = sb.toString().split("\n");
        for(int i = 0;i<lineitems.length;i++){
            if(log.isDebugEnabled())log.debug("line item contents \n" + lineitems[i]);
            contents.add(lineitems[i]);
        }

        if(log.isDebugEnabled())log.debug(sp.toString());

        spreadsheet = new Spreadsheet();
        spreadsheet.setTitle(sp.getName());
        spreadsheet.setDate(sp.getDateCreated());
        spreadsheet.setUserId(sp.getCreator());
        spreadsheet.setLineitems(contents);

        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        //
        // assignmentHeaders = new ArrayList();

        SpreadsheetHeader header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
        assignmentHeaders = header.getHeaderWithoutUser();


        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line);
                studentRows.add(row);
                //check the number of unkonw users in spreadsheet
                if(!row.isKnown())unknownusers = unknownusers + 1;
                if(log.isDebugEnabled()) log.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);
        if(unknownusers > 0){
            this.hasUnknownUser = true;
        }

        //create a numeric list of assignment headers

        if(log.isDebugEnabled())log.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            if(log.isDebugEnabled()) log.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(log.isDebugEnabled()) log.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(log.isDebugEnabled()) log.debug("Map initialized " +studentRows.size());
        if(log.isDebugEnabled()) log.debug("assignmentList " +assignmentList.size());


        return "spreadsheetPreview";
    }

    public boolean isSelectedCategoryDropsScores() {
		return selectedCategoryDropsScores;
	}

	public void setSelectedCategoryDropsScores(boolean selectedCategoryDropsScores) {
		this.selectedCategoryDropsScores = selectedCategoryDropsScores;
	}

    public Category getAssignmentCategory() {
		return assignmentCategory;
	}

	public void setAssignmentCategory(Category assignmentCategory) {
		this.assignmentCategory = assignmentCategory;
	}

    public String processCategoryChangeInImport(ValueChangeEvent vce)
    {
        String changeCategory = (String) vce.getNewValue();
        selectedCategory = changeCategory;
        extraCreditCatSelected = extraCreditCategories.contains(selectedCategory);
        if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))  
        {
            if(changeCategory.equals(UNASSIGNED_CATEGORY)) {
                selectedCategoryDropsScores = false;
                assignmentCategory = null;
                selectedCategory = getLocalizedString("cat_unassigned");
            } else {
                List<Category> categories = getGradebookManager().getCategories(getGradebookId());
                if (categories != null && categories.size() > 0)
                {
                    for (Category category : categories) {
                        if(changeCategory.equals(category.getId().toString())) {
                            selectedCategoryDropsScores = category.isDropScores();
                            category.setAssignmentList(retrieveCategoryAssignmentList(category));
                            assignmentCategory = category;
                            assignment.setPointsPossible(assignmentCategory.getItemValue());
                            selectedCategory = category.getId().toString();
                            break;
                        }
                    }
                }
            }
        }
        return "spreadsheetImport";
    }


    /**
     * Bulk import of grades from a freshly imported spreadsheet
     *
     * @return String for navigation
     *
     * @throws Exception
     */
    public String processFileEntire() throws Exception {
    	InputStream inputStream = null;
    	String fileName = null;
	    List contents = null;

	    int maxFileSizeInMB;
	    try {
	        maxFileSizeInMB = ServerConfigurationService.getInt(PROPERTY_FILE_SIZE_MAX, FILE_SIZE_DEFAULT);
	    } catch (NumberFormatException nfe) {
	        if (log.isDebugEnabled()) log.debug("Invalid property set for gradebook max file size");
	        maxFileSizeInMB = FILE_SIZE_DEFAULT;
	    }
        long maxFileSizeInBytes = 1024L * 1024L * maxFileSizeInMB;

        boolean isXlsImport = false;
        boolean isOOXMLimport = false;

        if (upFile != null) {
	        if (upFile != null && log.isDebugEnabled()) {
	            log.debug("file size " + upFile.getSize() + "file name " + upFile.getName() + "file Content Type " + upFile.getContentType() + "");
	        }

	        if(log.isDebugEnabled()) log.debug("check that the file type is allowed");

	        if (upFile.getName().endsWith("csv")) {
	            isXlsImport = false;
	        } else if (upFile.getName().endsWith("xls")) {
	            isXlsImport = true;
	        } else if (upFile.getName().endsWith("xlsx")) {
	            isOOXMLimport = true;
	        } else {
	            FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error",new String[] {upFile.getName()}));
	            return null;
	        }

	        if (upFile.getSize() > maxFileSizeInBytes) {
	            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error",new String[] {maxFileSizeInMB + ""}));
                return null;
	        }

	        fileName = upFile.getName();
	        inputStream = new BufferedInputStream(upFile.getInputStream());
        }
        else {
        	savePickedUploadFile();

        	if (pickedFileReference != null) {
		        if (log.isDebugEnabled()) log.debug("check that the file is csv file");

		        if (pickedFileDesc == null) {
		            FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error", new String[] {pickedFileDesc}));
		            return null;
		        }

		        if(log.isDebugEnabled()) log.debug("check that the file type is allowed");

	            if (pickedFileDesc.endsWith("csv")) {
	                isXlsImport = false;
	            } else if (pickedFileDesc.endsWith("xls")) {
	                isXlsImport = true;
	            } else if (pickedFileDesc.endsWith("xlsx")) {
	                isOOXMLimport = true;
	            } else {
	                FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error",new String[] {pickedFileDesc}));
	                return null;
	            }

		        fileName = pickedFileDesc;

		        ContentResource resource = getPickedContentResource();
		        if (resource != null) {
		            // double check the file size does not exceed our limit
		            if (resource.getContentLength() > maxFileSizeInBytes) {
		                FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] {maxFileSizeInMB + ""}));
		                return null;
		            }
		        }
		        inputStream = getPickedFileStream(resource);

		        clearPickedFile();
        	}
            else {
            	// all null - no uploaded or picked file
                if (log.isDebugEnabled()) log.debug("uploaded file not initialized");
                FacesUtil.addErrorMessage(getLocalizedString("import_entire_file_missing"));
                return null;
            }
        }

		if (inputStream == null) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
            return null;
		}

		try {
		    if (isXlsImport) {
		        contents = excelToArray(inputStream);
		    } else if (isOOXMLimport) {
		        contents = excelOOXMLToArray(inputStream);
		    } else {
		        contents = csvtoArray(inputStream);
		    }
		}
		catch(IOException ioe) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
            return null;
		}
		finally {
		    if (inputStream != null) {
		        inputStream.close();
		    }
		}

		// double check that the number of rows in this spreadsheet is reasonable
        int numStudentsInSite = getNumStudentsInSite();
        if (contents.size() > (numStudentsInSite + MAX_NUM_ROWS_OVER_CLASS_SIZE)) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filerows_error", new String[] {contents.size() + "", numStudentsInSite + ""}));
            return null;
        }

        // reset error lists
        unknownUsers = new ArrayList();
        unknownAssignments = new ArrayList();

        spreadsheet = new Spreadsheet();
        spreadsheet.setDate(new Date());
        spreadsheet.setTitle(this.getTitle());
        spreadsheet.setFilename(fileName);
        spreadsheet.setLineitems(contents);

        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();

        SpreadsheetHeader header;
        try{
            header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
            assignmentHeaders = header.getHeaderWithoutUserAndCumulativeGrade();
        }
        catch(IndexOutOfBoundsException ioe) {
            if(log.isDebugEnabled())log.debug(ioe + " there is a problem with the uploaded spreadsheet");
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }

        // check for blank header titles - don't want assignments added w/ blank titles
        Iterator headerIter = assignmentHeaders.iterator();
        // skip the name col
        headerIter.next();
        while (headerIter.hasNext()) {
        	String itemTitle = (String) headerIter.next();
        	if (itemTitle == null || itemTitle.trim().length() == 0) {
        		FacesUtil.addErrorMessage(getLocalizedString("import_assignment_entire_missing_title"));
        		return null;
        	}
        }

        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        int headerCount = assignmentHeaders.size();
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line);

                List rowData = row.getRowcontent();

                // if Cumulative column was in spreadsheet, need to filter it out
                // here
                if (header.isHasCumulative()) rowData.remove(rowData.size()-1);

                // if the number of cols in the student row is less than the # headers,
                // we need to append blank placeholders
                if (rowData.size() < headerCount) {
                	while (rowData.size() < (headerCount)) {
                		rowData.add("");
                	}
                }
                studentRows.add(row);
                //check the number of unknown users in spreadsheet
                if(!row.isKnown()) {
                	unknownusers = unknownusers + 1;
                	unknownUsers.add(row.getUserId());
                }
                if(log.isDebugEnabled()) log.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1); // subtract header
        if(unknownusers > 0){
            this.hasUnknownUser = true;
            return null;
        }

        //create a numeric list of assignment headers

        if(log.isDebugEnabled())log.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size()-1;i++){
            assignmentList.add(new Integer(i));
            if(log.isDebugEnabled()) log.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());

        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(log.isDebugEnabled()) log.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(log.isDebugEnabled()) log.debug("Map initialized " +studentRows.size());
        if(log.isDebugEnabled()) log.debug("assignmentList " +assignmentList.size());

        if(studentRows.size() < 1){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }

   		return "spreadsheetVerify";
    }

    public String processFile() throws Exception {
    	InputStream inputStream = null;
    	String fileName = null;
	    List contents = null;

	    int maxFileSizeInMB;
        try {
            maxFileSizeInMB = ServerConfigurationService.getInt(PROPERTY_FILE_SIZE_MAX, FILE_SIZE_DEFAULT);
        } catch (NumberFormatException nfe) {
            if (log.isDebugEnabled()) log.debug("Invalid property set for gradebook max file size");
            maxFileSizeInMB = FILE_SIZE_DEFAULT;
        }
        long maxFileSizeInBytes = 1024L * 1024L * maxFileSizeInMB;
        boolean isXlsImport = false;
        boolean isOOXMLimport = false;

	    if (upFile != null) {
	        if (upFile != null && log.isDebugEnabled()) {
	            log.debug("file size " + upFile.getSize() + "file name " + upFile.getName() + "file Content Type " + upFile.getContentType() + "");
	        }

	        if(log.isDebugEnabled()) log.debug("check that the file type is allowed");

	        if (upFile.getName().endsWith("csv")) {
	            isXlsImport = false;
	        } else if (upFile.getName().endsWith("xls")) {
	            isXlsImport = true;
	        } else if (upFile.getName().endsWith("xlsx")) {
	            isOOXMLimport = true;
	        } else {
	            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error",new String[] {upFile.getName()}));
	            return null;
	        }

	        if (upFile.getSize() > maxFileSizeInBytes) {
                FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error",new String[] {maxFileSizeInMB + ""}));
                return null;
            }

	        fileName = upFile.getName();
	        inputStream = new BufferedInputStream(upFile.getInputStream());
        }
        else {
        	savePickedUploadFile();

        	if (pickedFileReference != null) {
		        if (log.isDebugEnabled()) log.debug("check that the file is csv file");

		        if (pickedFileDesc == null) {
		            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error", new String[] {pickedFileDesc}));
		            return null;
		        }

		        if(log.isDebugEnabled()) log.debug("check that the file type is allowed");

		        if (pickedFileDesc.endsWith("csv")) {
		            isXlsImport = false;
		        } else if (pickedFileDesc.endsWith("xls")) {
		            isXlsImport = true;
		        } else if (pickedFileDesc.endsWith("xlsx")) {
		        	isOOXMLimport = true;
		        } else {
		            FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error",new String[] {pickedFileDesc}));
		            return null;
		        }

		        fileName = pickedFileDesc;

		        ContentResource resource = getPickedContentResource();
                if (resource != null) {
                    // double check the file size does not exceed our limit
                    if (resource.getContentLength() > maxFileSizeInBytes) {
                        FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] {maxFileSizeInMB + ""}));
                        return null;
                    }
                }
                inputStream = getPickedFileStream(resource);

		        clearPickedFile();
        	}
            else {
            	// all null - no uploaded or picked file
                if (log.isDebugEnabled()) log.debug("uploaded file not initialized");
                FacesUtil.addErrorMessage(getLocalizedString("upload_view_failure"));
                return null;
            }
        }

		if (inputStream == null) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
            return null;
		}

		try {
		    if (isXlsImport) {
                contents = excelToArray(inputStream);
		    } else if (isOOXMLimport) {
                contents = excelOOXMLToArray(inputStream);
		    } else {
                contents = csvtoArray(inputStream);
            }
		}
		catch(IOException ioe) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
            return null;
		}
		//SAK-23610 POI doesn't support Excel 5 
		catch (OldExcelFormatException oex) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_oldformat_error"));
            return null;
		}
		finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

	    // double check that the number of rows in this spreadsheet is reasonable
		int numStudentsInSite = getNumStudentsInSite();
        if (contents.size() > (numStudentsInSite + MAX_NUM_ROWS_OVER_CLASS_SIZE)) {
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filerows_error",new String[] {contents.size() + "", numStudentsInSite + ""}));
            return null;
        }

		spreadsheet = new Spreadsheet();
        spreadsheet.setDate(new Date());
        // SAK-14173 - get title from session if available
        // and then clear the value from the session
        spreadsheet.setTitle(this.getTitle());
        clearImportTitle();
        spreadsheet.setFilename(fileName);
        spreadsheet.setLineitems(contents);
        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        //
        // assignmentHeaders = new ArrayList();

        SpreadsheetHeader header;
        try{
            header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
            assignmentHeaders = header.getHeaderWithoutUser();
        }catch(IndexOutOfBoundsException ioe){
            if(log.isDebugEnabled())log.debug(ioe + " there is a problem with the uploaded spreadsheet");
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }

        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line);
                studentRows.add(row);
                //check the number of unkonw users in spreadsheet
                if(!row.isKnown())unknownusers = unknownusers + 1;
                if(log.isDebugEnabled()) log.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);
        if(unknownusers > 0){
            this.hasUnknownUser = true;
        }

        //create a numeric list of assignment headers

        if(log.isDebugEnabled())log.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            if(log.isDebugEnabled()) log.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(log.isDebugEnabled()) log.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(log.isDebugEnabled()) log.debug("Map initialized " +studentRows.size());
        if(log.isDebugEnabled()) log.debug("assignmentList " +assignmentList.size());

        if(studentRows.size() < 1){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
            return null;
        }

        return "spreadsheetUploadPreview";
    }

    /**
     * method converts an input stream to an List consist of strings
     * representing a line
     *
     * @param inputStream
     * @return contents
     */
    private List csvtoArray(InputStream inputStream) throws IOException{

        /**
         * TODO this well probably be removed
         */

        if(log.isDebugEnabled()) log.debug("csvtoArray()");
        List contents = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine())!=null){
            //log.debug("contents of line: "+line);
            if (line.replaceAll(",", "").replaceAll("\"", "").equals("")) {
                continue;
            }

            contents.add(line);
        }
        return contents;

    }

    /**
     * method to save CSV to database
     *
     * @return String
     */
    public String saveFile(){

        StringBuilder sb = new StringBuilder();
        List contents =  spreadsheet.getLineitems();
        Iterator it = contents.iterator();
        while(it.hasNext()){
            String line = (String) it.next();
            sb.append(line + '\n');
        }

        String filename = spreadsheet.getFilename();

        /** temporary presistence code
         *
         */
        if(log.isDebugEnabled())log.debug("string to save "+sb.toString());
        try{
            getGradebookManager().createSpreadsheet(getGradebookId(),spreadsheet.getTitle(),this.getUserDirectoryService().getUserDisplayName(getUserUid()),new Date(),sb.toString());
        }catch(ConflictingSpreadsheetNameException e){
            log.debug(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("upload_preview_save_failure"));
            return null;
        }
        FacesUtil.addRedirectSafeMessage(getLocalizedString("upload_preview_save_confirmation", new String[] {filename}));

        this.setPageName("spreadsheetListing");
        return "spreadsheetListing";
    }

    /**
     * If user has selected column to import, grab the values and
     */
    public String importData(){

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        if(log.isDebugEnabled())log.debug("processFile()");
        String selectedColumn =  request.getParameter("form:assignment");
        if(log.isDebugEnabled())log.debug("the selected column is " + selectedColumn);

        selectedAssignment = new HashMap();
        try{
            selectedAssignment.put("GradebookAssignment", assignmentHeaders.get(Integer.parseInt(selectedColumn) - 1));
        }catch(Exception e){
            if(log.isDebugEnabled())log.debug("no assignment selected");
            FacesUtil.addErrorMessage(getLocalizedString("import_preview_assignment_selection_failure"));
            return null;
        }

        Iterator it = studentRows.iterator();
        if(log.isDebugEnabled())log.debug("number of student rows "+studentRows.size() );
         int i = 0;
         while(it.hasNext()){

             if(log.isDebugEnabled())log.debug("row " + i);
             SpreadsheetRow row = (SpreadsheetRow) it.next();
             List line = row.getRowcontent();

             String userid = "";
             String user = (String)line.get(0);
             try{
                 userid = ((User)rosterMap.get(line.get(0))).getUserUid();
             }catch(Exception e){
                 if(log.isDebugEnabled())log.debug("user "+ user + "is not known to the system");
                 userid = "";
             }

             String points;
             try{
            	 int index = Integer.parseInt(selectedColumn);
            	 if(line.size() > index) {
                     points = (String) line.get(index);
            	 } else {
            		 log.info("unable to find any points for " + userid + " in spreadsheet");
            		 points = "";
            	 }
             }catch(NumberFormatException e){
                 log.debug(e.getMessage());
                 points = "";
             }
             if(log.isDebugEnabled())log.debug("user "+user + " userid " + userid +" points "+points);
             if(!"".equals(points) && (!"".equals(userid))){
                 selectedAssignment.put(userid,points);
             }
             i++;
         }
        if(log.isDebugEnabled())log.debug("scores to import "+ i);

        spreadsheet.setSelectedAssignment(selectedAssignment);

        if(assignment == null) {
            assignment = new GradebookAssignment();
            assignment.setReleased(true);
        }

        try{
            scores =  spreadsheet.getSelectedAssignment();
            assignment.setName((String) scores.get("GradebookAssignment"));
        }catch(NullPointerException npe){
            if(log.isDebugEnabled()) log.debug("scores not set");
        }

        return "spreadsheetImport";
    }

    /**
     * Takes the spreadsheet data imported and updates/adds to what is in the database
     */
    public String importDataAndSaveAll(){
    	boolean gbUpdated = false;
    	hasUnknownAssignments = false;
    	externallyMaintainedImportMsg = new StringBuilder();

    	LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
    	if (getGradeEntryByLetter()) {
    		lgpm = getGradebookManager().getLetterGradePercentMapping(localGradebook);
    	}

        if(log.isDebugEnabled())log.debug("importDataAll()");

        // first, verify imported data is valid
        if (!verifyImportedData(lgpm))
        	return "spreadsheetVerify";

        List grAssignments = getGradebookManager().getAssignments(getGradebook().getId());
        Iterator assignIter = assignmentHeaders.iterator();

        // since the first two columns are user ids and name, skip over
        int index = 1;
        if (assignIter.hasNext()) assignIter.next();

        while (assignIter.hasNext()) {
        	String assignmentName = (String) assignIter.next();
        	String pointsPossibleAsString = null;

        	String [] parsedAssignmentName = assignmentName.split(" \\[");

        	assignmentName = parsedAssignmentName[0].trim();
        	Double pointsPossible = null;
        	if (parsedAssignmentName.length > 1) {
        	    String [] parsedPointsPossible = parsedAssignmentName[1].split("\\]");
        	    if (parsedPointsPossible.length > 0) {
        	        pointsPossibleAsString = parsedPointsPossible[0].trim();
        	        try{
        	            pointsPossible = convertStringToDouble(pointsPossibleAsString);
        	            pointsPossible = new Double(FacesUtil.getRoundDown(pointsPossible.doubleValue(), 2));
        	            if(pointsPossible <= 0)
        	                pointsPossibleAsString = null;
        	        }catch(ParseException e){
        	            pointsPossibleAsString = null;
        	        }
        	    }
        	}

        	// probably last column but not sure, so continue
        	if (getLocalizedString(CUMULATIVE_GRADE_STRING).equals(assignmentName)) {
        		continue;
        	}

        	index++;

        	// Get GradebookAssignment object from assignment name
        	GradebookAssignment assignment = getAssignmentByName(grAssignments, assignmentName);
            List gradeRecords = new ArrayList();

        	// if assignment == null, need to create a new one plus all the grade records
        	// if exists, need find those that are changed and only apply those
        	if (assignment == null) {

        		if (pointsPossible != null) {
        			// params: gradebook id, name of assignment, points possible, due date, NOT counted, is released
        			assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignmentName, pointsPossible, null, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
        			assignment = getGradebookManager().getAssignment(assignmentId);
        		}
        		else {
            		// for this version, display error message saying non-match between import
            		// and current gradebook assignments
            		hasUnknownAssignments = true;
            		unknownAssignments.add(assignmentName);

        			continue;
        		}

        		Iterator it = studentRows.iterator();

           		if(log.isDebugEnabled())log.debug("number of student rows "+studentRows.size() );
           		int i = 1;

           		while(it.hasNext()){
           			if(log.isDebugEnabled())log.debug("row " + i);
           			SpreadsheetRow row = (SpreadsheetRow) it.next();
           			List line = row.getRowcontent();

           			String userid = "";
           			String user = (String)line.get(0);
           			try {
           				userid = ((User)rosterMap.get(user)).getUserUid();
           			}
           			catch(Exception e) {
           				if(log.isDebugEnabled())log.debug("user "+ user + "is not known to the system");
           				userid = "";
           				// checked when imported from user's system so should not happen at this point.
           			}

           			if(line.size() > index) {
               			String inputScore = (String)line.get(index);

               			if (inputScore != null && inputScore.trim().length() > 0) {
           					Double scoreAsDouble = null;
           					String scoreAsString = inputScore.trim();

                            AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,userid, null);

                            // truncate input points/% to 2 decimal places
                            if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
                                try {
                                    scoreAsDouble = convertStringToDouble(scoreAsString);
                                    scoreAsDouble = new Double(FacesUtil.getRoundDown(scoreAsDouble.doubleValue(), 2));
                                    asnGradeRecord.setPercentEarned(scoreAsDouble);
                                    asnGradeRecord.setPointsEarned(scoreAsDouble);
                                    gradeRecords.add(asnGradeRecord);
                                    if(log.isDebugEnabled())log.debug("user "+user + " userid " + userid +" score "+inputScore.toString());
                                } catch (ParseException pe) {
                                    // this should have been caught during validation, so there is a problem
                                    log.error("ParseException encountered parsing " + scoreAsString);
                                }
                            } else if (getGradeEntryByLetter()){
                                scoreAsString = (String)inputScore;

                                asnGradeRecord.setLetterEarned(scoreAsString.trim());
                                gradeRecords.add(asnGradeRecord);
                                if(log.isDebugEnabled())log.debug("user "+user + " userid " + userid +" score "+inputScore.toString());
                            }
           				}
           			}

           			i++;
           		}

           		gbUpdated = true;
         	}
        	else {
        		if (! assignment.getPointsPossible().equals(pointsPossible)) {
        			if (assignment.isExternallyMaintained()) {
        				externallyMaintainedImportMsg.append(getLocalizedString("import_assignment_externally_maintained_settings",
        						new String[] {Validator.escapeHtml(assignment.getName()), Validator.escapeHtml(assignment.getExternalAppName())}) + "<br />");
        			} else if (pointsPossible != null) {
        				assignment.setPointsPossible(pointsPossible);
        				getGradebookManager().updateAssignment(assignment);
        				gbUpdated = true;
        			}
        		}

        		gradeRecords = gradeChanges(assignment, studentRows, index+1, lgpm);

        		if (gradeRecords.size() == 0)
        			continue; // no changes to current grade record so go to next one
        		else {
        			gbUpdated = true;
        		}
        	}

			getGradebookManager().updateAssignmentGradeRecords(assignment, gradeRecords, getGradebook().getGrade_type());
			getGradebookBean().getEventTrackingService().postEvent("gradebook.importEntire","/gradebook/"+getGradebookId()+"/"+assignment.getName()+"/"+getAuthzLevel());
       }

        // just in case previous attempt had unknown users
        hasUnknownUser = false;
       	if (gbUpdated) {
       		if (! hasUnknownAssignments) {
               	FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_SUCCESS_STRING));
            }
            else {
               	FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_SOME_SUCCESS_STRING));
            }
        }
        else if (! hasUnknownAssignments) {
        	FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_NO_CHANGES));
        }

        this.setPageName("spreadsheetAll");
        return "spreadsheetAll";
    }

    /**
     * Returns TRUE if specific value imported from spreadsheet is valid
     *
     * @return false if the spreadsheet contains a invalid points possible, an invalid
     * score value, more than one column for the same gb item, more than one row for the same student
     */
    private boolean verifyImportedData(LetterGradePercentMapping lgpm) {
    	if (studentRows == null || studentRows.isEmpty()) {
    		return true;
    	}

    	if (getGradeEntryByLetter() && (lgpm == null || lgpm.getGradeMap() == null)) {
    		FacesUtil.addErrorMessage(getLocalizedString("gb_setup_no_grade_entry_scale"));
    		return false;
    	}

    	// determine the index of the "cumulative" column
    	int indexOfCumColumn = -1;
    	if (assignmentHeaders != null && !assignmentHeaders.isEmpty()) {
    		// add one b/c assignmentHeaders does not include the username col but studentRows does
    		indexOfCumColumn = assignmentHeaders.indexOf(getLocalizedString(CUMULATIVE_GRADE_STRING)) + 1;

    		// we need to double check that there aren't any duplicate assignments
    		// in the spreadsheet. the first column is the student name, so skip
    		List<String> assignmentNames = new ArrayList<String>();
    		if (assignmentHeaders.size() > 1) {
    		    for (int i=1; i < assignmentHeaders.size(); i++) {
    		        if (i==indexOfCumColumn) {
    		            continue;
    		        }

    		        String header = (String)assignmentHeaders.get(i);
    		        String [] parsedAssignmentName = header.split(" \\[");
    	            String assignmentName = parsedAssignmentName[0].trim();
    	            if (assignmentNames.contains(assignmentName)) {
    	                FacesUtil.addErrorMessage(getLocalizedString("import_assignment_duplicate_titles", new String[] {assignmentName}));
    	                return false;
    	            }

    	            assignmentNames.add(assignmentName);
    		    }
    		}
    	}

    	List<String> uploadedStudents = new ArrayList<String>();

    	for (int row=0; row < studentRows.size(); row++) {
    		SpreadsheetRow scoreRow = (SpreadsheetRow) studentRows.get(row);
    		List studentScores = scoreRow.getRowcontent();

    		// verify that a student doesn't appear more than once in the ss
    		if(studentScores != null) {
        		String username = (String)studentScores.get(0);
        		if (username != null) {
        		    if (uploadedStudents.contains(username)) {
        		        FacesUtil.addErrorMessage(getLocalizedString("import_assignment_duplicate_student", new String[] {username}));
                        return false;
        		    }
    
        		    uploadedStudents.add(username);
        		}
    		}
    		
    		// start with col 2 b/c the first two are eid and name
    		if (studentScores != null && studentScores.size() > 2) {
    			for (int i=2; i < studentScores.size(); i++) {
    				if (i == indexOfCumColumn)
    					continue;

    				String scoreAsString = ((String)studentScores.get(i)).trim();
    				if (scoreAsString != null && scoreAsString.length() > 0) {

    					if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
    						Double scoreAsDouble;

    						try {
    							if(log.isDebugEnabled()) log.debug("checking if " +scoreAsString +" is a numeric value");

    							scoreAsDouble = convertStringToDouble(scoreAsString);
    							// check for negative values
    							if (scoreAsDouble.doubleValue() < 0) {
    								if(log.isDebugEnabled()) log.debug(scoreAsString + " is not a positive value");
    								FacesUtil.addErrorMessage(getLocalizedString(IMPORT_ASSIGNMENT_NEG_VALUE));

    								return false;
    							}
    						} catch(ParseException e){
    							if(log.isDebugEnabled()) log.debug(scoreAsString + " is not a numeric value");
    							FacesUtil.addErrorMessage(getLocalizedString(IMPORT_ASSIGNMENT_NOTSUPPORTED));

    							return false;
    						}

    					} else if (getGradeEntryByLetter()) {
    						String standardizedLetterGrade = lgpm.standardizeInputGrade(scoreAsString);
    						if (standardizedLetterGrade == null) {
    							FacesUtil.addErrorMessage(getLocalizedString("import_entire_invalid_letter"));
    							return false;
    						}
    					}
    				}
    			}
    		}
    	}

    	return true;
    }

    /**
     * Returns a list of AssignmentGradeRecords that are different than in current
     * Gradebook. Returns empty List if no differences found.
     *
     * @param assignment
     * 			The GradebookAssignment object whose grades need to be checked
     * @param fromSpreadsheet
     * 			The rows of grades from the imported spreadsheet
     * @param index
     * 			The column of spreadsheet to check
     *
     * @return
     * 			List containing AssignmentGradeRecords for those student's grades that
     * 			have changed
     */
    private List gradeChanges(GradebookAssignment assignment, List fromSpreadsheet, int index, LetterGradePercentMapping lgpm) {
    	List updatedGradeRecords = new ArrayList();
     	List studentUids = new ArrayList();
    	List studentRowsWithUids = new ArrayList();

   		Iterator it = fromSpreadsheet.iterator();

   		while(it.hasNext()) {
   			final SpreadsheetRow row = (SpreadsheetRow) it.next();
   			List line = row.getRowcontent();

   			String userid = "";
   			final String user = ((String)line.get(0)).toLowerCase();
   			try {
   				userid = ((User)rosterMap.get(user)).getUserUid();

   				// create list of uids to get current grades from gradebook
   				studentUids.add(userid);

   				// add uid to each row so can check spreadsheet value against currently stored
   				List linePlus = new ArrayList();
   				linePlus.add(userid);
   				linePlus.addAll(line);
   				studentRowsWithUids.add(linePlus);

   			}
   			catch(Exception e) {
   				// Weirdness. Should be caught when importing, not here
   			}
  		}

  		List gbGrades = getGradebookManager().getAssignmentGradeRecordsConverted(assignment, studentUids);

		// now do the actual comparison
		it = studentRowsWithUids.iterator();

		boolean updatingExternalGrade = false;
		while(it.hasNext()) {
			final List aRow = (List) it.next();

			final String userid = (String) aRow.get(0);
			final String user = (String) aRow.get(1);

			AssignmentGradeRecord gr = findGradeRecord(gbGrades, userid);

			String score = null;
			if (index < aRow.size()) {
				score = ((String) aRow.get(index)).trim();
			}

			if (getGradeEntryByPercent() || getGradeEntryByPoints()) {
				Double scoreEarned = null;
				boolean updateScore = true;
				if (score != null && !"".equals(score)) {
				    try {
				        scoreEarned = convertStringToDouble(score);
				    } catch (ParseException pe) {
				        // this should have already been validated at this point, so there is
				        // something wrong if we made it here
				        log.error("ParseException encountered while checking for grade updates with score: " + score);
				        updateScore = false;
				    }

					// truncate to 2 decimal places
					if (scoreEarned != null)
						scoreEarned = new Double(FacesUtil.getRoundDown(scoreEarned.doubleValue(), 2));
				}

				if (updateScore) {
				    if (gr == null) {
				        if (scoreEarned != null) {
				            if (!assignment.isExternallyMaintained()) {
				                gr = new AssignmentGradeRecord(assignment,userid,scoreEarned);
				                gr.setPercentEarned(scoreEarned);  // manager will handle if % vs point grading
				                updatedGradeRecords.add(gr);
				            } else {
				                updatingExternalGrade = true;
				            }
				        }
				    }
				    else {
				        // we need to truncate points earned to 2 decimal places to more accurately
				        // see if it was changed - scores that are entered as % can be stored with
				        // unlimited decimal places in db
				        Double gbScoreEarned = null;
				        if (getGradeEntryByPercent())
				            gbScoreEarned = gr.getPercentEarned();
				        else
				            gbScoreEarned = gr.getPointsEarned();

				        if (gbScoreEarned != null)
				            gbScoreEarned = new Double(FacesUtil.getRoundDown(gbScoreEarned.doubleValue(), 2));

				        // 3 ways points earned different: 1 null other not (both ways) or actual
				        // values different
				        if ((gbScoreEarned == null && scoreEarned != null) ||
				                (gbScoreEarned != null && scoreEarned == null) ||
				                (gbScoreEarned != null && scoreEarned != null && gbScoreEarned.doubleValue() != scoreEarned.doubleValue())) {

				            gr.setPointsEarned(scoreEarned); //manager will use correct field depending on grade entry method
				            gr.setPercentEarned(scoreEarned);
				            if (!assignment.isExternallyMaintained())
				                updatedGradeRecords.add(gr);
				            else
				                updatingExternalGrade = true;
				        }
				    }
				}
			} else if (getGradeEntryByLetter()) {
				if (lgpm == null || lgpm.getGradeMap() == null)
					return null;
				if (score != null && score.length() > 0) {
					score = lgpm.standardizeInputGrade(score);
				}

				if (gr == null) {
					if (score != null && score.length() > 0) {
						if (!assignment.isExternallyMaintained()) {
							gr = new AssignmentGradeRecord(assignment,userid,null);
							gr.setLetterEarned(score);
							updatedGradeRecords.add(gr);
						} else {
							updatingExternalGrade = true;
						}
					}
				}
				else {
					String gbLetterEarned = gr.getLetterEarned();

					if ((gbLetterEarned != null && !gbLetterEarned.equals(score)) ||
							(gbLetterEarned == null && score != null))  {

						gr.setLetterEarned(score);
						if (!assignment.isExternallyMaintained())
							updatedGradeRecords.add(gr);
						else
							updatingExternalGrade = true;
					}
				}
			}
		}

		if (updatingExternalGrade)
			externallyMaintainedImportMsg.append(getLocalizedString("import_assignment_externally_maintained_grades",
					new String[] {Validator.escapeHtml(assignment.getName()), Validator.escapeHtml(assignment.getExternalAppName())}) + "<br/>");

		return updatedGradeRecords;
    }

    /**
     * Finds the AssignmentGradeRecord for userid passed in (if it exists).
     *
     * @param gbGrades
     * 			List of AssignmentGradeRecord objects.
     * @param userid
     * 			String of user id to find.
     *
     * @return
     * 			AssignmentGradeRecord if it's student id matches id passed in. NULL otherwise.
     */
    private AssignmentGradeRecord findGradeRecord(List gbGrades, String userid) {
    	Iterator it = gbGrades.iterator();

    	while (it.hasNext()) {
    		AssignmentGradeRecord agr = (AssignmentGradeRecord) it.next();

    		if (agr.getStudentId().equals(userid)) {
    			it.remove(); // for efficiency
    			return agr;
    		}
    	}

    	return null;
    }

    /**
     * Used to find assignment in gradebook by its name
     *
     * @param list The list of gradebook assignments
     * @param name The String to look for
     *
     * @return GradebookAssignment object if found, null otherwise
     */
    private GradebookAssignment getAssignmentByName(List assignList, String name) {
    	for (Iterator assignIter = assignList.iterator(); assignIter.hasNext(); ) {
    		GradebookAssignment assignment = (GradebookAssignment) assignIter.next();

    		if (assignment.getName().trim().equalsIgnoreCase(name.trim())) {
    			// remove for performance
    			assignIter.remove();
    			return assignment;
    		}
    	}

    	return null;
    }

    /**
     * Cancel import and return to Import Grades page.
     *
     * @return String to navigate to Import Grades page.
     */
    public String processImportAllCancel() {
    	hasUnknownUser = false;
    	hasUnknownAssignments = false;
    	return "spreadsheetAll";
    }

    //save grades and comments
    public String saveGrades(){

        if(log.isDebugEnabled())log.debug("create assignment and save grades");
        if(log.isDebugEnabled()) log.debug("first check if all variables are numeric");

        log.debug("********************" + scores);

        LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
        if (getGradeEntryByLetter()) {
        	lgpm = getGradebookManager().getLetterGradePercentMapping(getGradebook());
        }

        Iterator iter = scores.entrySet().iterator();
        while(iter.hasNext()){
        	Map.Entry entry  = (Map.Entry) iter.next();
        	if(!entry.getKey().equals("GradebookAssignment")) {
        		if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
        			String points =  (String) entry.getValue();
        			try{
        				if(log.isDebugEnabled()) log.debug("checking if " +points +" is a numeric value");

        				double score = convertStringToDouble(points);

        				if (score < 0) {
        					FacesUtil.addErrorMessage(getLocalizedString("import_assignment_negative"));
        					return "spreadsheetPreview";
        				}

        			}catch(ParseException e){
        				if(log.isDebugEnabled()) log.debug(points + " is not a numeric value");
        				FacesUtil.addErrorMessage(getLocalizedString("import_assignment_notsupported"));

        				return "spreadsheetPreview";
        			}
        		} else if (getGradeEntryByLetter()) {
        			if (lgpm.getGradeMap() == null) {
        				FacesUtil.addErrorMessage(getLocalizedString("gb_setup_no_grade_entry_scale"));
        				return "spreadsheetPreview";
        			}
        			String letterScore = (String) entry.getValue();
        			if (letterScore != null && letterScore.length() > 0) {
        				String formattedLetterScore = lgpm.standardizeInputGrade(letterScore);
        				if (formattedLetterScore == null) {
        					FacesUtil.addErrorMessage(getLocalizedString("import_assignment_invalid_letter"));
        					return "spreadsheetPreview";
        				}
        			}
        		}
        	}
        }

        try {
        	Category newCategory = retrieveSelectedCategory();
        	if (newCategory != null) {
        		assignmentId = getGradebookManager().createAssignmentForCategory(getGradebookId(), newCategory.getId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()), new Boolean(assignment.isExtraCredit()));
        	} else {
        		assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()), new Boolean(assignment.isExtraCredit()));
        	}

            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));

            assignment = getGradebookManager().getAssignment(assignmentId);
            List gradeRecords = new ArrayList();

            //initialize comment List
            List comments = new ArrayList();
            //check if a comments column is selected for the defalt select item value is
            // 0 which mean no comments to be imported
            if(selectedCommentsColumnId!=null && selectedCommentsColumnId > 0) comments = createCommentList(assignment);

            if(log.isDebugEnabled())log.debug("remove title entry form map");
            scores.remove("GradebookAssignment");
            if(log.isDebugEnabled())log.debug("iterate through scores and and save assignment grades");

            Iterator it = scores.entrySet().iterator();
            while(it.hasNext()){

                Map.Entry entry = (Map.Entry) it.next();
                String uid = (String) entry.getKey();
                String scoreAsString = (String) entry.getValue();
                if (scoreAsString != null && scoreAsString.trim().length() > 0) {
                	if (getGradeEntryByPercent() || getGradeEntryByPoints()) {
                	    try {
                	        Double scoreAsDouble;
                	        scoreAsDouble = convertStringToDouble(scoreAsString);
                	        if (scoreAsDouble != null)
                	            scoreAsDouble = new Double(FacesUtil.getRoundDown(scoreAsDouble.doubleValue(), 2));
                	        AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,uid,scoreAsDouble);
                	        asnGradeRecord.setPercentEarned(scoreAsDouble); // in case gb entry by % - sorted out in manager
                	        gradeRecords.add(asnGradeRecord);
                	        log.debug("added grades for {} - score {}", uid, scoreAsString);
                	    } catch (ParseException pe) {
                	        // the score should have already been validated at this point, so
                	        // there is something wrong
                	        log.error("ParseException encountered while parsing value: {} Score was not updated.", scoreAsString);
                	    }
                	} else if (getGradeEntryByLetter()) {
                		AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,uid,null);
                		asnGradeRecord.setLetterEarned(lgpm.standardizeInputGrade(scoreAsString));
                		gradeRecords.add(asnGradeRecord);
                	}
                }
            }

            if(log.isDebugEnabled())log.debug("persist grade records to database");
            getGradebookManager().updateAssignmentGradesAndComments(assignment,gradeRecords,comments);
            getGradebookBean().getEventTrackingService().postEvent("gradebook.importItem","/gradebook/"+getGradebookId()+"/"+assignment.getName()+"/"+getAuthzLevel());

    		return "spreadsheetListing";

        } catch (ConflictingAssignmentNameException e) {
            log.error(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
        }


        return null;
    }


    /**
     * method creates a collection of comment
     * objects from a the saved spreadsheet and
     * selected column. requires an assignment as parameter to set the
     * gradableObject property of the comment
     *
     * @param assignmentTobeCommented
     * @return  List of comment comment objects
     */
    public List createCommentList(GradebookAssignment assignmentTobeCommented){

        List comments = new ArrayList();
        Iterator it = studentRows.iterator();
        while(it.hasNext()){
            SpreadsheetRow row = (SpreadsheetRow) it.next();
            List line = row.getRowcontent();

            String userid = "";
            String user = (String)line.get(0);
            try{
                userid = ((User)rosterMap.get(line.get(0))).getUserUid();
                String commentText = (String) line.get( selectedCommentsColumnId.intValue());
                if((!commentText.equals(""))){
                    Comment comment = new Comment(userid,commentText,assignmentTobeCommented);
                    comments.add(comment);
                }
            }catch(Exception e){
                if(log.isDebugEnabled())log.debug("student is required  and "+ user + "is not known to this gradebook");
            }
        }
        return comments;
    }

    public Integer getSelectedCommentsColumnId() {
        return selectedCommentsColumnId;
    }

    public void setSelectedCommentsColumnId(Integer selectedCommentsColumnId) {
        this.selectedCommentsColumnId = selectedCommentsColumnId;
    }

    /**
     *
     */

    public class Spreadsheet  implements Serializable {

        private String title;
        private Date date;
        private String userId;
        private String contents;
        private String displayName;
        private Long gradebookId;
        private String filename;
        private List lineitems;
        private Map selectedAssignment;



        public Spreadsheet(String title, Date date, String userId, String contents) {

            if(log.isDebugEnabled())log.debug("loading Spreadsheet()");
            this.title = title;
            this.date = date;
            this.userId = userId;
            this.contents = contents;
        }

        public Spreadsheet() {
            if(log.isDebugEnabled())log.debug("loading Spreadsheet()");
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContents() {
            return contents;
        }

        public void setContents(String contents) {
            this.contents = contents;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Long getGradebookId() {
            return gradebookId;
        }

        public void setGradebookId(Long gradebookId) {
            this.gradebookId = gradebookId;
        }


        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }


        public List getLineitems() {
            return lineitems;
        }

        public void setLineitems(List lineitems) {
            this.lineitems = lineitems;
        }


        public Map getSelectedAssignment() {
            //log.debug(this.selectedAssignment);
            return selectedAssignment;
        }

        public void setSelectedAssignment(Map selectedAssignment) {
            this.selectedAssignment = selectedAssignment;
            //log.debug(selectedAssignment);
        }

        public String toString() {
            return "Spreadsheet{" +
                    "title='" + title + '\'' +
                    ", date=" + date +
                    ", userId='" + userId + '\'' +
                    ", contents='" + contents + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", gradebookId=" + gradebookId +
                    ", filename='" + filename + '\'' +
                    ", lineitems=" + lineitems +
                    ", selectedAssignment=" + selectedAssignment +
                    '}';
        }
    }




    /**
     * spreadsheet header class
     */

    public class SpreadsheetHeader implements Serializable{

        private List header;
        private int columnCount;
        private boolean hasCumulative;

        public List getHeader() {
            return header;
        }

        public void setHeader(List header) {
            this.header = header;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }


        public boolean isHasCumulative() {
			return hasCumulative;
		}

		public void setHasCumulative(boolean hasCumulative) {
			this.hasCumulative = hasCumulative;
		}

		public List getHeaderWithoutUser() {
            List head = header;
            head.remove(0);
            return head;
        }

        public List getHeaderWithoutUserAndCumulativeGrade() {
        	List head = getHeaderWithoutUser();
        	// If CSV from Roster page, last column will be Cumulative Grade
        	// so remove from header list
        	if (head.get(head.size()-1).equals("Cumulative")) {
        		head.remove(head.size()-1);
        	}

        	return head;
        }

        public SpreadsheetHeader(String source) {


            if(log.isDebugEnabled()) log.debug("creating header from "+source);
            header = new ArrayList();
            CSV csv = new CSV();
            header = csv.parse(source);
            columnCount = header.size();
            hasCumulative = header.get(columnCount-1).equals("Cumulative");
        }

    }

    /**
     * spreadsheetRow class
     */
    public class SpreadsheetRow implements Serializable {

        private List rowcontent;
        private int columnCount;
        private String userDisplayName;
        private String userId;
        private String userUid;
        private boolean isKnown;

        public SpreadsheetRow(String source) {

            // this may be instantiated before SpreadsheetUploadBean is initialized, so make sure
            // the rosterMap is populated
            if (rosterMap == null) {
                initializeRosterMap();
            }

            if(log.isDebugEnabled()) log.debug("creating row from string " + source);
            rowcontent = new ArrayList();
            CSV csv = new CSV();
            rowcontent = csv.parse(source);

            // derive the user information
            String userContent = (String) rowcontent.get(0);
            if (userContent != null) {
                userId = userContent.toLowerCase();

                // check to see if this student is in the roster
                if (rosterMap.containsKey(userId)) {
                    isKnown = true;
                    User user = (User)rosterMap.get(userId);
                    userDisplayName = user.getDisplayName();
                    userUid = user.getUserUid();

                    if(log.isDebugEnabled())log.debug("get userid "+ userId + "username is "+userDisplayName);
                } else {
                    isKnown = false;
                    userDisplayName = getLocalizedString("import_preview_unknown_name");
                    userUid = null;

                    if (log.isDebugEnabled()) log.debug("User " + userId + " is unknown to this gradebook");
                }
            } else {
                isKnown = false;
                userDisplayName = getLocalizedString("import_preview_unknown_name");
                userUid = null;
                userId = null;

                if(log.isDebugEnabled())log.debug("Null userId in spreadsheet");
            }

        }

        public List getRowcontent() {

            return rowcontent;
        }

        public void setRowcontent(List rowcontent) {
            this.rowcontent = rowcontent;
        }


        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getUserDisplayName() {
            return userDisplayName;
        }

        public void setUserDisplayName(String userDisplayName) {
            this.userDisplayName = userDisplayName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public boolean isKnown() {
            return isKnown;
        }

        public void setKnown(boolean known) {
            isKnown = known;
        }
    }

    //csv class to handle

    /** Parse comma-separated values (CSV), a common Windows file format.
     * Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
     * <p>
     * Inner logic adapted from a C++ original that was
     * Copyright (C) 1999 Lucent Technologies
     * Excerpted from 'The Practice of Programming'
     * by Brian W. Kernighan and Rob Pike.
     * <p>
     * Included by permission of the http://tpop.awl.com/ web site,
     * which says:
     * "You may use this code for any purpose, as long as you leave
     * the copyright notice and book citation attached." I have done so.
     * @author Brian W. Kernighan and Rob Pike (C++ original)
     * @author Ian F. Darwin (translation into Java and removal of I/O)
     * @author Ben Ballard (rewrote advQuoted to handle '""' and for readability)
     */
    class CSV {

        public static final char DEFAULT_SEP = ',';

        /** Construct a CSV parser, with the default separator (`,'). */
        public CSV() {
            this(DEFAULT_SEP);
        }

        /** Construct a CSV parser with a given separator.
         * @param sep The single char for the separator (not a list of
         * separator characters)
         */
        public CSV(char sep) {
            fieldSep = sep;
        }

        /** The fields in the current String */
        protected List list = new ArrayList();

        /** the separator char for this parser */
        protected char fieldSep;

        /** parse: break the input String into fields
         * @return java.util.Iterator containing each field
         * from the original as a String, in order.
         */
        public List parse(String line)
        {
            StringBuilder sb = new StringBuilder();
            list.clear();      // recycle to initial state
            int i = 0;

            if (line.length() == 0) {
                list.add(line);
                return list;
            }

            do {
                sb.setLength(0);
                if (i < line.length() && line.charAt(i) == '"')
                    i = advQuoted(line, sb, ++i);  // skip quote
                else
                    i = advPlain(line, sb, i);
                list.add(sb.toString());
                i++;
            } while (i < line.length());
            if(log.isDebugEnabled()) {
                StringBuilder logBuffer = new StringBuilder("Parsed " + line + " as: ");
                for(Iterator iter = list.iterator(); iter.hasNext();) {
                	logBuffer.append(iter.next());
                	if(iter.hasNext()) {
                		logBuffer.append(", ");
                	}
                }
                log.debug("Parsed source string " + line + " as " + logBuffer.toString() + ", length=" + list.size());
            }
            return list;

        }

        /** advQuoted: quoted field; return index of next separator */
        protected int advQuoted(String s, StringBuilder sb, int i)
        {
            int j;
            int len= s.length();
            for (j=i; j<len; j++) {
                if (s.charAt(j) == '"' && j+1 < len) {
                    if (s.charAt(j+1) == '"') {
                        j++; // skip escape char
                    } else if (s.charAt(j+1) == fieldSep) { //next delimeter
                        j++; // skip end quotes
                        break;
                    }
                } else if (s.charAt(j) == '"' && j+1 == len) { // end quotes at end of line
                    break; //done
                }
                sb.append(s.charAt(j));  // regular character.
            }
            return j;
        }

        /** advPlain: unquoted field; return index of next separator */
        protected int advPlain(String s, StringBuilder sb, int i)
        {
            int j;

            j = s.indexOf(fieldSep, i); // look for separator
            if (j == -1) {                 // none found
                sb.append(s.substring(i));
                return s.length();
            } else {
                sb.append(s.substring(i, j));
                return j;
            }
        }
    }

    //************************ EXCEL file parsing *****************************
    /**
     * method converts an input stream to an List consisting of strings
     * representing a line.  The input stream must be for an xls file.
     *
     * @param inputStream
     * @return contents
     */
    private List excelToArray(InputStream inputStreams) throws IOException {
        HSSFWorkbook wb  = new HSSFWorkbook(inputStreams);

        //Convert an Excel file to csv
        HSSFSheet sheet = wb.getSheetAt(0);
        List array = new ArrayList();
        Iterator it = sheet.rowIterator();
        while (it.hasNext()){
            HSSFRow row = (HSSFRow) it.next();
            String rowAsString = fromHSSFRowtoCSV(row);
            if (rowAsString.replaceAll(",", "").replaceAll("\"", "").equals("")) {
                continue;
            }
            array.add(fromHSSFRowtoCSV(row));
        }
        return array;
    }

    private String fromHSSFRowtoCSV(HSSFRow row){
        StringBuffer csvRow = new StringBuffer();
        int l = row.getLastCellNum();
        for (int i=0;i<l;i++){
            HSSFCell cell = row.getCell((short)i);
            String cellValue = "";
            if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                cellValue = "";
            } else if (cell.getCellType()== HSSFCell.CELL_TYPE_STRING){
                cellValue = "\"" + cell.getStringCellValue() + "\"";
            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                double value = cell.getNumericCellValue();
                cellValue = getNumberFormat().format(value);
                cellValue = "\"" + cellValue + "\"";
            }

            csvRow.append(cellValue);

            if (i<l){
                csvRow.append(getCsvDelimiter().toCharArray()[0]);
            }
        }
        return csvRow.toString();

    }

    /**
     * Parse newer OOXML Excel Spreadsheets
     * @param inputStreams
     * @return
     * @throws IOException
     */
    private List<String> excelOOXMLToArray(InputStream inputStreams) throws IOException {
    	XSSFWorkbook wb  = new XSSFWorkbook(inputStreams);

        //Convert an Excel OOXML (.xslx) file to csv
    	XSSFSheet sheet = wb.getSheetAt(0);
        List<String> array = new ArrayList<String>();
        Iterator it = sheet.rowIterator();
        while (it.hasNext()){
        	XSSFRow row = (XSSFRow) it.next();
            String rowAsString = fromXSSFRowtoCSV(row);
            if (rowAsString.replaceAll(",", "").replaceAll("\"", "").equals("")) {
                continue;
            }
            array.add(fromXSSFRowtoCSV(row));
        }
        return array;
    }

    private String fromXSSFRowtoCSV(XSSFRow row){
        StringBuffer csvRow = new StringBuffer();
        int l = row.getLastCellNum();
        for (int i=0;i<l;i++){
            XSSFCell cell = row.getCell((short)i);
            String cellValue = "";
            if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                cellValue = "";
            } else if (cell.getCellType()== HSSFCell.CELL_TYPE_STRING){
                cellValue = "\"" + cell.getStringCellValue() + "\"";
            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
                double value = cell.getNumericCellValue();
                cellValue = getNumberFormat().format(value);
                cellValue = "\"" + cellValue + "\"";
            }

            csvRow.append(cellValue);

            if (i<l){
                csvRow.append(getCsvDelimiter().toCharArray()[0]);
            }
        }
        return csvRow.toString();
    }

    /**
     * Process an upload ActionEvent from spreadsheetUpload.jsp or spreadsheetEntireGBImport.jsp
     * Source of this action is the Upload button on either page
     *
     * @param event
     */
    public void launchFilePicker(ActionEvent event) {
		try  {
			String titleText = FacesUtil.getLocalizedString("upload_view_page_title");
			String instructionText = FacesUtil.getLocalizedString("upload_view_instructions_text");

			ToolSession currentToolSession = SessionManager.getCurrentToolSession();
	        // SAK-14173 - store title
	        currentToolSession.setAttribute(IMPORT_TITLE, this.title);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_SINGLE);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, titleText);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, instructionText);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER,
				ComponentManager.get("org.sakaiproject.content.api.ContentResourceFilter.spreadsheetCsvFile"));
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("sakai.filepicker.helper/tool");
		}
		catch(Exception e) {
			log.error(this + ".launchFilePicker - " + e);
		}
    }

    /**
     * SAK-14173 - This event will cause the title to be stored in the bean so that it can be stored
     * in the session when the user goes to choose a file and will not be lost
     * @param event JSF value change event
     */
    public void changeTitle(ValueChangeEvent event) {
        this.title = (String) event.getNewValue();
    }

    /**
     * SAK-14173 - Clears all the session variables stored as part of the spreadsheet upload code
     * @param event JSF event
     */
    public void cancelAndClearSession(ActionEvent event) {
        clearImportTitle();
        clearPickedFile();
    }

    /**
     * SAK-14173 - Clears the import title from the session
     */
    private void clearImportTitle() {
        ToolSession currentToolSession = SessionManager.getCurrentToolSession();
        currentToolSession.removeAttribute(IMPORT_TITLE);
    }

    /**
     * Extract and store the reference to the uploaded file.
     * Only process the first reference - don't do multiple uploads.
     *
     */
    public void savePickedUploadFile() {
      ToolSession session = SessionManager.getCurrentToolSession();
      if (session.getAttribute(PICKED_FILE_REFERENCE) != null) {
    	  pickedFileReference = (String) session.getAttribute(PICKED_FILE_REFERENCE);
    	  pickedFileDesc = (String) session.getAttribute(PICKED_FILE_DESC);
      }
    }

    /**
     * Use the uploaded file reference to get an InputStream.
     * Must be a reference to a ContentResource
     * @param resource the ContentResource associated with {@link #pickedFileReference}.
     * see {@link #getPickedContentResource()}
     * @return InputStream
     */
    private InputStream getPickedFileStream(ContentResource resource) {
        InputStream inStream = null;

        if (resource != null) {
            try {
                inStream = resource.streamContent();
            }
            catch(ServerOverloadException soe) {
                log.error(soe.getMessage(), soe);
            }
        }

        return inStream;
    }

    /**
     *
     * @return the ContentResource associated with the {@link #pickedFileReference} property.
     * returns null if the entity is not a ContentResource
     */
    private ContentResource getPickedContentResource() {
        ContentResource resource = null;
        if (pickedFileReference != null) {
            Reference ref = EntityManager.newReference(pickedFileReference);
            if (ref != null) {
                Entity ent = ref.getEntity();
                if (ent instanceof ContentResource) {
                    // entity came from file picker, so it should be a content resource
                    resource = (ContentResource) ent;
                }
            }
        }

        return resource;
    }

    private void clearPickedFile() {
        ToolSession session = SessionManager.getCurrentToolSession();
    	session.removeAttribute(PICKED_FILE_REFERENCE);
    	session.removeAttribute(PICKED_FILE_DESC);

    	pickedFileDesc = null;
    	pickedFileReference = null;
    }

	/**
	 * @return the pickedFileDesc
	 */
	public String getPickedFileDesc() {
		// check the session attribute first
		ToolSession session = SessionManager.getCurrentToolSession();
	    if (session.getAttribute(PICKED_FILE_DESC) != null) {
	    	pickedFileDesc = (String) session.getAttribute(PICKED_FILE_DESC);
	    }

		return pickedFileDesc;
	}

	/**
	 * @param pickedFileDesc the pickedFileDesc to set
	 */
	public void setPickedFileDesc(String pickedFileDesc) {
		this.pickedFileDesc = pickedFileDesc;
	}

	/**
	 *
	 * @return the number of users with the student-type role enrolled in the current site
	 */
	private int getNumStudentsInSite() {
	    List enrollments = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
        int numStudentsInSite = enrollments != null ? enrollments.size() : 0;
        return numStudentsInSite;
	}

	private String getCsvDelimiter() {
	    if (csvDelimiter == null) {
	        csvDelimiter = ServerConfigurationService.getString("csv.separator", ",");
	    }

	    return csvDelimiter;
	}

	/**
	 *
	 * @param doubleAsString
	 * @return a locale-aware Double value representation of the given String
	 * @throws ParseException
	 */
	private Double convertStringToDouble(String doubleAsString) throws ParseException {
	    Double scoreAsDouble = null;
	    if (doubleAsString != null) {
	        Number numericScore = getNumberFormat().parse(doubleAsString.trim());
	        scoreAsDouble = numericScore.doubleValue();
	    }

	    return scoreAsDouble;
	}

	private NumberFormat getNumberFormat() {
	    if (numberFormat == null) {
	        numberFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
	    }

	    return numberFormat;
	}

	public String getDateEntryFormatDescription(){
		return this.date_entry_format_description;
	}
	
    public Boolean getExtraCreditCatSelected() {
		return extraCreditCatSelected;
	}
}



