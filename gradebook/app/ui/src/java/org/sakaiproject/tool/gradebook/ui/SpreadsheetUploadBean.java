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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
	private String csvDelimiter;
	private NumberFormat numberFormat;
	private boolean selectedCategoryDropsScores;
	private String date_entry_format_description;

	// Used for bulk upload of gradebook items
	// Holds list of unknown user ids
	private List unknownUsers = new ArrayList();
	private List unknownAssignments = new ArrayList();

	private static final String CUMULATIVE_GRADE_STRING = "roster_course_grade_column_name";
	private static final String IMPORT_SUCCESS_STRING = "import_entire_success";
	private static final String IMPORT_SOME_SUCCESS_STRING = "import_entire_some_success";
	private static final String IMPORT_NO_CHANGES = "import_entire_no_changes";
	private static final String IMPORT_ASSIGNMENT_NOTSUPPORTED = "import_assignment_entire_notsupported";
	private static final String IMPORT_ASSIGNMENT_NEG_VALUE = "import_assignment_entire_negative_score";
	static final String PICKED_FILE_REFERENCE = "pickedFileReference";
	static final String PICKED_FILE_DESC = "pickedFileDesc";
	static final String IMPORT_TITLE = "gradebookImportTitle";

	public static final String UNASSIGNED_CATEGORY = "unassigned";

	/**
	 * Property set via sakai.properties to limit the file size allowed for spreadsheet uploads. This property is in MB and defaults to 1
	 * MB.
	 */
	public static final String PROPERTY_FILE_SIZE_MAX = "gradebook.upload.max";

	/**
	 * The default max file size, in MB, for a spreadsheet upload.
	 */
	public static final int FILE_SIZE_DEFAULT = 1;

	/**
	 * If an upload contains more than the number of students in the class plus this value, the upload will fail.
	 */
	private static final int MAX_NUM_ROWS_OVER_CLASS_SIZE = 50;

	private String pageName;

	public SpreadsheetUploadBean() {

	}

	@Override
	public void init() {

		this.localGradebook = getGradebook();

		initializeRosterMap();

		if (this.assignment == null) {
			if (this.assignmentId != null) {
				this.assignment = getGradebookManager().getAssignment(this.assignmentId);
			}
			if (this.assignment == null) {
				// it is a new assignment
				this.assignment = new GradebookAssignment();
				this.assignment.setReleased(true);
			}
		}

		// initialization; shouldn't enter here after category drop down changes
		if (this.assignmentCategory == null && !getLocalizedString("cat_unassigned").equalsIgnoreCase(this.selectedCategory)) {
			final Category assignCategory = this.assignment.getCategory();
			if (assignCategory != null) {
				this.selectedCategory = assignCategory.getId().toString();
				this.selectedCategoryDropsScores = assignCategory.isDropScores();
				assignCategory.setAssignmentList(retrieveCategoryAssignmentList(assignCategory));
				this.assignmentCategory = assignCategory;
			} else {
				this.selectedCategory = getLocalizedString("cat_unassigned");
			}
		}

		if (this.selectedCategory == null) {
			this.selectedCategory = AssignmentBean.UNASSIGNED_CATEGORY;
		}
		this.categoriesSelectList = new ArrayList();
		// create comma seperate string representation of the list of EC categories
		this.extraCreditCategories = new ArrayList();
		// The first choice is always "Unassigned"
		this.categoriesSelectList.add(new SelectItem(AssignmentBean.UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		final List gbCategories = getViewableCategories();
		if (gbCategories != null && gbCategories.size() > 0) {
			final Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				final Category cat = (Category) catIter.next();
				this.categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
				if (cat.isExtraCredit()) {
					this.extraCreditCategories.add(cat.getId().toString());
				}
			}
		}
		final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, (new ResourceLoader()).getLocale());
		this.date_entry_format_description = ((SimpleDateFormat) df).toPattern();

	}

	private void initializeRosterMap() {
		// initialize rosteMap which is map of displayid and user objects
		this.rosterMap = new HashMap();
		final List enrollments = new ArrayList(findMatchingEnrollmentsForAllItems(null, null).keySet());
		if (log.isDebugEnabled()) {
			log.debug("enrollment size " + enrollments.size());
		}

		Iterator iter;
		iter = enrollments.iterator();
		while (iter.hasNext()) {
			EnrollmentRecord enr;
			enr = (EnrollmentRecord) iter.next();
			if (log.isDebugEnabled()) {
				log.debug("displayid " + enr.getUser().getDisplayId() + "  userid " + enr.getUser().getUserUid());
			}
			this.rosterMap.put(enr.getUser().getDisplayId().toLowerCase(), enr.getUser());
		}
	}

	public String getTitle() {
		if (this.title == null || "".equals(this.title)) {
			// check the session attribute
			final ToolSession session = SessionManager.getCurrentToolSession();
			if (session.getAttribute(IMPORT_TITLE) != null) {
				this.title = (String) session.getAttribute(IMPORT_TITLE);
			}
		}
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public UploadedFile getUpFile() {
		return this.upFile;
	}

	public void setUpFile(final UploadedFile upFile) {
		if (log.isDebugEnabled()) {
			log.debug("upload file name " + upFile.getName());
		}
		this.upFile = upFile;
	}

	public Spreadsheet getSpreadsheet() {
		return this.spreadsheet;
	}

	public void setSpreadsheet(final Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public Map getRosterMap() {
		return this.rosterMap;
	}

	public void setRosterMap(final Map rosterMap) {
		this.rosterMap = rosterMap;
	}

	public Long getSpreadsheetId() {
		return this.spreadsheetId;
	}

	public void setSpreadsheetId(final Long spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}

	public List getSpreadsheets() {
		setPageName("spreadsheetListing");
		return getGradebookManager().getSpreadsheets(getGradebookId());
	}

	public String deleteItem() {
		return "spreadsheetRemove";
	}

	public List getAssignmentList() {
		return this.assignmentList;
	}

	public void setAssignmentList(final List assignmentList) {
		this.assignmentList = assignmentList;
	}

	public List getStudentRows() {
		return this.studentRows;
	}

	public void setStudentRows(final List studentRows) {
		this.studentRows = studentRows;
	}

	public List getAssignmentHeaders() {
		return this.assignmentHeaders;
	}

	public void setAssignmentHeaders(final List assignmentHeaders) {
		this.assignmentHeaders = assignmentHeaders;
	}

	public Map getSelectedAssignment() {
		return this.selectedAssignment;
	}

	public void setSelectedAssignment(final Map selectedAssignment) {
		this.selectedAssignment = selectedAssignment;
	}

	public List getAssignmentColumnSelectItems() {
		return this.assignmentColumnSelectItems;
	}

	public void setAssignmentColumnSelectItems(final List assignmentColumnSelectItems) {
		this.assignmentColumnSelectItems = assignmentColumnSelectItems;
	}

	public boolean isSaved() {
		return this.saved;
	}

	public void setSaved(final boolean saved) {
		this.saved = saved;
	}

	/**
	 * Returns formatted string with count of assignments imported (columns - 1 {student name})
	 */
	public String getVerifyColumnCount() {
		final int intColumnCount = Integer.parseInt(this.columnCount == null ? "0" : this.columnCount) - 1;
		return FacesUtil.getLocalizedString("import_verify_column_count", new String[] { Integer.toString(intColumnCount) });
	}

	public String getColumnCount() {
		return FacesUtil.getLocalizedString("upload_preview_column_count", new String[] { this.columnCount });
	}

	public void setColumnCount(final String columnCount) {
		this.columnCount = columnCount;
	}

	/**
	 * Returns formatted string with count of number of students imported Added points possible row so need to subtract one from count
	 */
	public String getVerifyRowCount() {
		return FacesUtil.getLocalizedString("import_verify_row_count", new String[] { this.rowCount });
	}

	public String getRowCount() {
		return FacesUtil.getLocalizedString("upload_preview_row_count", new String[] { this.rowCount });
	}

	public void setRowCount(final String rowCount) {
		this.rowCount = rowCount;
	}

	public String getRowStyles() {
		final StringBuilder sb = new StringBuilder();
		if (this.studentRows != null) {
			for (final Iterator iter = this.studentRows.iterator(); iter.hasNext();) {
				final SpreadsheetRow row = (SpreadsheetRow) iter.next();
				if (row.isKnown()) {
					sb.append("internal,");
				} else {
					sb.append("external,");
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(sb.toString());
		}
		return sb.toString();
	}

	/**
	 * Returns List of unknown user names
	 */
	public boolean getHasUnknownUser() {
		return this.hasUnknownUser;
	}

	public Map getScores() {
		return this.scores;
	}

	public void setScores(final Map scores) {
		this.scores = scores;
	}

	public GradebookAssignment getAssignment() {
		return this.assignment;
	}

	public void setAssignment(final GradebookAssignment assignment) {
		this.assignment = assignment;
	}

	public Long getAssignmentId() {
		return this.assignmentId;
	}

	public void setAssignmentId(final Long assignmentId) {
		this.assignmentId = assignmentId;
	}

	/**
	 * Returns list of unknown users
	 */
	public List getUnknownUsers() {
		return this.unknownUsers;
	}

	/**
	 * Sets the list of unknown users
	 */
	public void setUnknownUsers(final List unknownUsers) {
		this.unknownUsers = unknownUsers;
	}

	public boolean isHasUnknownAssignments() {
		return this.hasUnknownAssignments;
	}

	public void setHasUnknownAssignments(final boolean hasUnknownAssignments) {
		this.hasUnknownAssignments = hasUnknownAssignments;
	}

	public List getUnknownAssignments() {
		return this.unknownAssignments;
	}

	public void setUnknownAssignments(final List unknownAssignments) {
		this.unknownAssignments = unknownAssignments;
	}

	/**
	 * Returns the count of unknown users
	 */
	public int getUnknownSize() {
		setPageName("spreadsheetAll");
		return this.unknownUsers.size();
	}

	public List getCategoriesSelectList() {
		return this.categoriesSelectList;
	}

	public String getSelectedCategory() {
		return this.selectedCategory;
	}

	public void setSelectedCategory(final String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	public Gradebook getLocalGradebook() {
		return this.localGradebook;
	}

	/**
	 * For retaining the pageName variable throughout import process
	 */
	@Override
	public String getPageName() {
		return this.pageName;
	}

	@Override
	public void setPageName(final String pageName) {
		this.pageName = pageName;
	}

	public String getExternallyMaintainedImportMsg() {
		if (this.externallyMaintainedImportMsg == null || this.externallyMaintainedImportMsg.length() < 1) {
			return null;
		}

		return this.externallyMaintainedImportMsg.toString();
	}

	/**
	 * Returns the Category associated with selectedCategory If unassigned or not found, returns null
	 *
	 * @return
	 */
	private Category retrieveSelectedCategory() {
		Long catId = null;
		Category category = null;

		if (this.selectedCategory != null && !this.selectedCategory.equals(AssignmentBean.UNASSIGNED_CATEGORY)) {
			try {
				catId = new Long(this.selectedCategory);
			} catch (final Exception e) {
				catId = null;
			}

			if (catId != null) {
				// check to make sure there is a corresponding category
				category = getGradebookManager().getCategory(catId);

				// populate assignments list
				category.setAssignmentList(retrieveCategoryAssignmentList(category));
			}
		}

		return category;
	}

	private List retrieveCategoryAssignmentList(final Category cat) {
		final List assignmentsToUpdate = new ArrayList();
		if (cat != null) {
			List assignments = cat.getAssignmentList();
			if (cat.isDropScores() && (assignments == null || assignments.size() == 0)) { // don't populate, if assignments are already in
																							// category (to improve performance)
				assignments = getGradebookManager().getAssignmentsForCategory(cat.getId());

				// only include assignments which are not adjustments must not update adjustment item pointsPossible
				for (final Object o : assignments) {
					if (o instanceof GradebookAssignment) {
						final GradebookAssignment assignment = (GradebookAssignment) o;
						if (!GradebookAssignment.item_type_adjustment.equals(assignment.getItemType())) {
							assignmentsToUpdate.add(assignment);
						}
					}
				}
			}
		}
		return assignmentsToUpdate;
	}

	// view file from db
	public String viewItem() {

		if (this.rosterMap == null) {
			init();
		}

		if (log.isDebugEnabled()) {
			log.debug("loading viewItem()");
		}

		final org.sakaiproject.tool.gradebook.Spreadsheet sp = getGradebookManager().getSpreadsheet(this.spreadsheetId);
		final StringBuilder sb = new StringBuilder();
		sb.append(sp.getContent());

		final List contents = new ArrayList();

		final String lineitems[] = sb.toString().split("\n");
		for (final String lineitem : lineitems) {
			if (log.isDebugEnabled()) {
				log.debug("line item contents \n" + lineitem);
			}
			contents.add(lineitem);
		}

		if (log.isDebugEnabled()) {
			log.debug(sp.toString());
		}

		this.spreadsheet = new Spreadsheet();
		this.spreadsheet.setTitle(sp.getName());
		this.spreadsheet.setDate(sp.getDateCreated());
		this.spreadsheet.setUserId(sp.getCreator());
		this.spreadsheet.setLineitems(contents);

		this.assignmentList = new ArrayList();
		this.studentRows = new ArrayList();
		this.assignmentColumnSelectItems = new ArrayList();
		//
		// assignmentHeaders = new ArrayList();

		final SpreadsheetHeader header = new SpreadsheetHeader((String) this.spreadsheet.getLineitems().get(0));
		this.assignmentHeaders = header.getHeaderWithoutUser();

		// generate spreadsheet rows
		final Iterator it = this.spreadsheet.getLineitems().iterator();
		int rowcount = 0;
		int unknownusers = 0;
		while (it.hasNext()) {
			final String line = (String) it.next();
			if (rowcount > 0) {
				final SpreadsheetRow row = new SpreadsheetRow(line);
				this.studentRows.add(row);
				// check the number of unkonw users in spreadsheet
				if (!row.isKnown()) {
					unknownusers = unknownusers + 1;
				}
				if (log.isDebugEnabled()) {
					log.debug("row added" + rowcount);
				}
			}
			rowcount++;
		}
		this.rowCount = String.valueOf(rowcount - 1);
		if (unknownusers > 0) {
			this.hasUnknownUser = true;
		}

		// create a numeric list of assignment headers

		if (log.isDebugEnabled()) {
			log.debug("creating assignment List ---------");
		}
		for (int i = 0; i < this.assignmentHeaders.size(); i++) {
			this.assignmentList.add(new Integer(i));
			if (log.isDebugEnabled()) {
				log.debug("col added" + i);
			}
		}
		this.columnCount = String.valueOf(this.assignmentHeaders.size());

		for (int i = 0; i < this.assignmentHeaders.size(); i++) {
			final SelectItem item = new SelectItem(new Integer(i + 1), (String) this.assignmentHeaders.get(i));
			if (log.isDebugEnabled()) {
				log.debug("creating selectItems " + item.getValue());
			}
			this.assignmentColumnSelectItems.add(item);
		}

		if (log.isDebugEnabled()) {
			log.debug("Map initialized " + this.studentRows.size());
		}
		if (log.isDebugEnabled()) {
			log.debug("assignmentList " + this.assignmentList.size());
		}

		return "spreadsheetPreview";
	}

	public boolean isSelectedCategoryDropsScores() {
		return this.selectedCategoryDropsScores;
	}

	public void setSelectedCategoryDropsScores(final boolean selectedCategoryDropsScores) {
		this.selectedCategoryDropsScores = selectedCategoryDropsScores;
	}

	public Category getAssignmentCategory() {
		return this.assignmentCategory;
	}

	public void setAssignmentCategory(final Category assignmentCategory) {
		this.assignmentCategory = assignmentCategory;
	}

	public String processCategoryChangeInImport(final ValueChangeEvent vce) {
		final String changeCategory = (String) vce.getNewValue();
		this.selectedCategory = changeCategory;
		this.extraCreditCatSelected = this.extraCreditCategories.contains(this.selectedCategory);
		if (vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue())) {
			if (changeCategory.equals(UNASSIGNED_CATEGORY)) {
				this.selectedCategoryDropsScores = false;
				this.assignmentCategory = null;
				this.selectedCategory = getLocalizedString("cat_unassigned");
			} else {
				final List<Category> categories = getGradebookManager().getCategories(getGradebookId());
				if (categories != null && categories.size() > 0) {
					for (final Category category : categories) {
						if (changeCategory.equals(category.getId().toString())) {
							this.selectedCategoryDropsScores = category.isDropScores();
							category.setAssignmentList(retrieveCategoryAssignmentList(category));
							this.assignmentCategory = category;
							this.assignment.setPointsPossible(this.assignmentCategory.getItemValue());
							this.selectedCategory = category.getId().toString();
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
		} catch (final NumberFormatException nfe) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid property set for gradebook max file size");
			}
			maxFileSizeInMB = FILE_SIZE_DEFAULT;
		}
		final long maxFileSizeInBytes = 1024L * 1024L * maxFileSizeInMB;

		boolean isXlsImport = false;
		boolean isOOXMLimport = false;

		if (this.upFile != null) {
			if (this.upFile != null && log.isDebugEnabled()) {
				log.debug("file size " + this.upFile.getSize() + "file name " + this.upFile.getName() + "file Content Type "
						+ this.upFile.getContentType() + "");
			}

			if (log.isDebugEnabled()) {
				log.debug("check that the file type is allowed");
			}

			if (this.upFile.getName().endsWith("csv")) {
				isXlsImport = false;
			} else if (this.upFile.getName().endsWith("xls")) {
				isXlsImport = true;
			} else if (this.upFile.getName().endsWith("xlsx")) {
				isOOXMLimport = true;
			} else {
				FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error", new String[] { this.upFile.getName() }));
				return null;
			}

			if (this.upFile.getSize() > maxFileSizeInBytes) {
				FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] { maxFileSizeInMB + "" }));
				return null;
			}

			fileName = this.upFile.getName();
			inputStream = new BufferedInputStream(this.upFile.getInputStream());
		} else {
			savePickedUploadFile();

			if (this.pickedFileReference != null) {
				if (log.isDebugEnabled()) {
					log.debug("check that the file is csv file");
				}

				if (this.pickedFileDesc == null) {
					FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error", new String[] { this.pickedFileDesc }));
					return null;
				}

				if (log.isDebugEnabled()) {
					log.debug("check that the file type is allowed");
				}

				if (this.pickedFileDesc.endsWith("csv")) {
					isXlsImport = false;
				} else if (this.pickedFileDesc.endsWith("xls")) {
					isXlsImport = true;
				} else if (this.pickedFileDesc.endsWith("xlsx")) {
					isOOXMLimport = true;
				} else {
					FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error", new String[] { this.pickedFileDesc }));
					return null;
				}

				fileName = this.pickedFileDesc;

				final ContentResource resource = getPickedContentResource();
				if (resource != null) {
					// double check the file size does not exceed our limit
					if (resource.getContentLength() > maxFileSizeInBytes) {
						FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] { maxFileSizeInMB + "" }));
						return null;
					}
				}
				inputStream = getPickedFileStream(resource);

				clearPickedFile();
			} else {
				// all null - no uploaded or picked file
				if (log.isDebugEnabled()) {
					log.debug("uploaded file not initialized");
				}
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
		} catch (final IOException ioe) {
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
			return null;
		} finally {
			inputStream.close();
		}

		// double check that the number of rows in this spreadsheet is reasonable
		final int numStudentsInSite = getNumStudentsInSite();
		if (contents.size() > (numStudentsInSite + MAX_NUM_ROWS_OVER_CLASS_SIZE)) {
			FacesUtil.addErrorMessage(
					getLocalizedString("upload_view_filerows_error", new String[] { contents.size() + "", numStudentsInSite + "" }));
			return null;
		}

		// reset error lists
		this.unknownUsers = new ArrayList();
		this.unknownAssignments = new ArrayList();

		this.spreadsheet = new Spreadsheet();
		this.spreadsheet.setDate(new Date());
		this.spreadsheet.setTitle(getTitle());
		this.spreadsheet.setFilename(fileName);
		this.spreadsheet.setLineitems(contents);

		this.assignmentList = new ArrayList();
		this.studentRows = new ArrayList();
		this.assignmentColumnSelectItems = new ArrayList();

		SpreadsheetHeader header;
		try {
			header = new SpreadsheetHeader((String) this.spreadsheet.getLineitems().get(0));
			this.assignmentHeaders = header.getHeaderWithoutUserAndCumulativeGrade();
		} catch (final IndexOutOfBoundsException ioe) {
			if (log.isDebugEnabled()) {
				log.debug(ioe + " there is a problem with the uploaded spreadsheet");
			}
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
			return null;
		}

		// check for blank header titles - don't want assignments added w/ blank titles
		final Iterator headerIter = this.assignmentHeaders.iterator();
		// skip the name col
		headerIter.next();
		while (headerIter.hasNext()) {
			final String itemTitle = (String) headerIter.next();
			if (itemTitle == null || itemTitle.trim().length() == 0) {
				FacesUtil.addErrorMessage(getLocalizedString("import_assignment_entire_missing_title"));
				return null;
			}
		}

		// generate spreadsheet rows
		final Iterator it = this.spreadsheet.getLineitems().iterator();
		int rowcount = 0;
		int unknownusers = 0;
		final int headerCount = this.assignmentHeaders.size();
		while (it.hasNext()) {
			final String line = (String) it.next();
			if (rowcount > 0) {
				final SpreadsheetRow row = new SpreadsheetRow(line);

				final List rowData = row.getRowcontent();

				// if Cumulative column was in spreadsheet, need to filter it out
				// here
				if (header.isHasCumulative()) {
					rowData.remove(rowData.size() - 1);
				}

				// if the number of cols in the student row is less than the # headers,
				// we need to append blank placeholders
				if (rowData.size() < headerCount) {
					while (rowData.size() < (headerCount)) {
						rowData.add("");
					}
				}
				this.studentRows.add(row);
				// check the number of unknown users in spreadsheet
				if (!row.isKnown()) {
					unknownusers = unknownusers + 1;
					this.unknownUsers.add(row.getUserId());
				}
				if (log.isDebugEnabled()) {
					log.debug("row added" + rowcount);
				}
			}
			rowcount++;
		}
		this.rowCount = String.valueOf(rowcount - 1); // subtract header
		if (unknownusers > 0) {
			this.hasUnknownUser = true;
			return null;
		}

		// create a numeric list of assignment headers

		if (log.isDebugEnabled()) {
			log.debug("creating assignment List ---------");
		}
		for (int i = 0; i < this.assignmentHeaders.size() - 1; i++) {
			this.assignmentList.add(new Integer(i));
			if (log.isDebugEnabled()) {
				log.debug("col added" + i);
			}
		}
		this.columnCount = String.valueOf(this.assignmentHeaders.size());

		for (int i = 0; i < this.assignmentHeaders.size(); i++) {
			final SelectItem item = new SelectItem(new Integer(i + 1), (String) this.assignmentHeaders.get(i));
			if (log.isDebugEnabled()) {
				log.debug("creating selectItems " + item.getValue());
			}
			this.assignmentColumnSelectItems.add(item);
		}

		if (log.isDebugEnabled()) {
			log.debug("Map initialized " + this.studentRows.size());
		}
		if (log.isDebugEnabled()) {
			log.debug("assignmentList " + this.assignmentList.size());
		}

		if (this.studentRows.size() < 1) {
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
		} catch (final NumberFormatException nfe) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid property set for gradebook max file size");
			}
			maxFileSizeInMB = FILE_SIZE_DEFAULT;
		}
		final long maxFileSizeInBytes = 1024L * 1024L * maxFileSizeInMB;
		boolean isXlsImport = false;
		boolean isOOXMLimport = false;

		if (this.upFile != null) {
			if (this.upFile != null && log.isDebugEnabled()) {
				log.debug("file size " + this.upFile.getSize() + "file name " + this.upFile.getName() + "file Content Type "
						+ this.upFile.getContentType() + "");
			}

			if (log.isDebugEnabled()) {
				log.debug("check that the file type is allowed");
			}

			if (this.upFile.getName().endsWith("csv")) {
				isXlsImport = false;
			} else if (this.upFile.getName().endsWith("xls")) {
				isXlsImport = true;
			} else if (this.upFile.getName().endsWith("xlsx")) {
				isOOXMLimport = true;
			} else {
				FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error", new String[] { this.upFile.getName() }));
				return null;
			}

			if (this.upFile.getSize() > maxFileSizeInBytes) {
				FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] { maxFileSizeInMB + "" }));
				return null;
			}

			fileName = this.upFile.getName();
			inputStream = new BufferedInputStream(this.upFile.getInputStream());
		} else {
			savePickedUploadFile();

			if (this.pickedFileReference != null) {
				if (log.isDebugEnabled()) {
					log.debug("check that the file is csv file");
				}

				if (this.pickedFileDesc == null) {
					FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error", new String[] { this.pickedFileDesc }));
					return null;
				}

				if (log.isDebugEnabled()) {
					log.debug("check that the file type is allowed");
				}

				if (this.pickedFileDesc.endsWith("csv")) {
					isXlsImport = false;
				} else if (this.pickedFileDesc.endsWith("xls")) {
					isXlsImport = true;
				} else if (this.pickedFileDesc.endsWith("xlsx")) {
					isOOXMLimport = true;
				} else {
					FacesUtil.addErrorMessage(getLocalizedString("import_entire_filetype_error", new String[] { this.pickedFileDesc }));
					return null;
				}

				fileName = this.pickedFileDesc;

				final ContentResource resource = getPickedContentResource();
				if (resource != null) {
					// double check the file size does not exceed our limit
					if (resource.getContentLength() > maxFileSizeInBytes) {
						FacesUtil.addErrorMessage(getLocalizedString("upload_view_filesize_error", new String[] { maxFileSizeInMB + "" }));
						return null;
					}
				}
				inputStream = getPickedFileStream(resource);

				clearPickedFile();
			} else {
				// all null - no uploaded or picked file
				if (log.isDebugEnabled()) {
					log.debug("uploaded file not initialized");
				}
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
		} catch (final IOException ioe) {
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));
			return null;
		}
		// SAK-23610 POI doesn't support Excel 5
		catch (final OldExcelFormatException oex) {
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_oldformat_error"));
			return null;
		} finally {
			inputStream.close();
		}

		// double check that the number of rows in this spreadsheet is reasonable
		final int numStudentsInSite = getNumStudentsInSite();
		if (contents.size() > (numStudentsInSite + MAX_NUM_ROWS_OVER_CLASS_SIZE)) {
			FacesUtil.addErrorMessage(
					getLocalizedString("upload_view_filerows_error", new String[] { contents.size() + "", numStudentsInSite + "" }));
			return null;
		}

		this.spreadsheet = new Spreadsheet();
		this.spreadsheet.setDate(new Date());
		// SAK-14173 - get title from session if available
		// and then clear the value from the session
		this.spreadsheet.setTitle(getTitle());
		clearImportTitle();
		this.spreadsheet.setFilename(fileName);
		this.spreadsheet.setLineitems(contents);
		this.assignmentList = new ArrayList();
		this.studentRows = new ArrayList();
		this.assignmentColumnSelectItems = new ArrayList();
		//
		// assignmentHeaders = new ArrayList();

		SpreadsheetHeader header;
		try {
			header = new SpreadsheetHeader((String) this.spreadsheet.getLineitems().get(0));
			this.assignmentHeaders = header.getHeaderWithoutUser();
		} catch (final IndexOutOfBoundsException ioe) {
			if (log.isDebugEnabled()) {
				log.debug(ioe + " there is a problem with the uploaded spreadsheet");
			}
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
			return null;
		}

		// generate spreadsheet rows
		final Iterator it = this.spreadsheet.getLineitems().iterator();
		int rowcount = 0;
		int unknownusers = 0;
		while (it.hasNext()) {
			final String line = (String) it.next();
			if (rowcount > 0) {
				final SpreadsheetRow row = new SpreadsheetRow(line);
				this.studentRows.add(row);
				// check the number of unkonw users in spreadsheet
				if (!row.isKnown()) {
					unknownusers = unknownusers + 1;
				}
				if (log.isDebugEnabled()) {
					log.debug("row added" + rowcount);
				}
			}
			rowcount++;
		}
		this.rowCount = String.valueOf(rowcount - 1);
		if (unknownusers > 0) {
			this.hasUnknownUser = true;
		}

		// create a numeric list of assignment headers

		if (log.isDebugEnabled()) {
			log.debug("creating assignment List ---------");
		}
		for (int i = 0; i < this.assignmentHeaders.size(); i++) {
			this.assignmentList.add(new Integer(i));
			if (log.isDebugEnabled()) {
				log.debug("col added" + i);
			}
		}
		this.columnCount = String.valueOf(this.assignmentHeaders.size());

		for (int i = 0; i < this.assignmentHeaders.size(); i++) {
			final SelectItem item = new SelectItem(new Integer(i + 1), (String) this.assignmentHeaders.get(i));
			if (log.isDebugEnabled()) {
				log.debug("creating selectItems " + item.getValue());
			}
			this.assignmentColumnSelectItems.add(item);
		}

		if (log.isDebugEnabled()) {
			log.debug("Map initialized " + this.studentRows.size());
		}
		if (log.isDebugEnabled()) {
			log.debug("assignmentList " + this.assignmentList.size());
		}

		if (this.studentRows.size() < 1) {
			FacesUtil.addErrorMessage(getLocalizedString("upload_view_filecontent_error"));
			return null;
		}

		return "spreadsheetUploadPreview";
	}

	/**
	 * method converts an input stream to an List consist of strings representing a line
	 *
	 * @param inputStream
	 * @return contents
	 */
	private List csvtoArray(final InputStream inputStream) throws IOException {

		/**
		 * TODO this well probably be removed
		 */

		if (log.isDebugEnabled()) {
			log.debug("csvtoArray()");
		}
		final List contents = new ArrayList();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = reader.readLine()) != null) {
			// log.debug("contents of line: "+line);
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
	public String saveFile() {

		final StringBuilder sb = new StringBuilder();
		final List contents = this.spreadsheet.getLineitems();
		final Iterator it = contents.iterator();
		while (it.hasNext()) {
			final String line = (String) it.next();
			sb.append(line + '\n');
		}

		final String filename = this.spreadsheet.getFilename();

		/**
		 * temporary presistence code
		 *
		 */
		if (log.isDebugEnabled()) {
			log.debug("string to save " + sb.toString());
		}
		try {
			getGradebookManager().createSpreadsheet(getGradebookId(), this.spreadsheet.getTitle(),
					getGradebookBean().getUserDisplayName(getUserUid()), new Date(), sb.toString());
		} catch (final ConflictingSpreadsheetNameException e) {
			log.debug(e.getMessage());
			FacesUtil.addErrorMessage(getLocalizedString("upload_preview_save_failure"));
			return null;
		}
		FacesUtil.addRedirectSafeMessage(getLocalizedString("upload_preview_save_confirmation", new String[] { filename }));

		setPageName("spreadsheetListing");
		return "spreadsheetListing";
	}

	/**
	 * If user has selected column to import, grab the values and
	 */
	public String importData() {

		final FacesContext facesContext = FacesContext.getCurrentInstance();
		final HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

		if (log.isDebugEnabled()) {
			log.debug("processFile()");
		}
		final String selectedColumn = request.getParameter("form:assignment");
		if (log.isDebugEnabled()) {
			log.debug("the selected column is " + selectedColumn);
		}

		this.selectedAssignment = new HashMap();
		try {
			this.selectedAssignment.put("GradebookAssignment", this.assignmentHeaders.get(Integer.parseInt(selectedColumn) - 1));
		} catch (final Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("no assignment selected");
			}
			FacesUtil.addErrorMessage(getLocalizedString("import_preview_assignment_selection_failure"));
			return null;
		}

		final Iterator it = this.studentRows.iterator();
		if (log.isDebugEnabled()) {
			log.debug("number of student rows " + this.studentRows.size());
		}
		int i = 0;
		while (it.hasNext()) {

			if (log.isDebugEnabled()) {
				log.debug("row " + i);
			}
			final SpreadsheetRow row = (SpreadsheetRow) it.next();
			final List line = row.getRowcontent();

			String userid = "";
			final String user = (String) line.get(0);
			try {
				userid = ((User) this.rosterMap.get(line.get(0))).getUserUid();
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("user " + user + "is not known to the system");
				}
				userid = "";
			}

			String points;
			try {
				final int index = Integer.parseInt(selectedColumn);
				if (line.size() > index) {
					points = (String) line.get(index);
				} else {
					log.info("unable to find any points for " + userid + " in spreadsheet");
					points = "";
				}
			} catch (final NumberFormatException e) {
				log.debug(e.getMessage());
				points = "";
			}
			if (log.isDebugEnabled()) {
				log.debug("user " + user + " userid " + userid + " points " + points);
			}
			if (!"".equals(points) && (!"".equals(userid))) {
				this.selectedAssignment.put(userid, points);
			}
			i++;
		}
		if (log.isDebugEnabled()) {
			log.debug("scores to import " + i);
		}

		this.spreadsheet.setSelectedAssignment(this.selectedAssignment);

		if (this.assignment == null) {
			this.assignment = new GradebookAssignment();
			this.assignment.setReleased(true);
		}

		try {
			this.scores = this.spreadsheet.getSelectedAssignment();
			this.assignment.setName((String) this.scores.get("GradebookAssignment"));
		} catch (final NullPointerException npe) {
			if (log.isDebugEnabled()) {
				log.debug("scores not set");
			}
		}

		return "spreadsheetImport";
	}

	/**
	 * Takes the spreadsheet data imported and updates/adds to what is in the database
	 */
	public String importDataAndSaveAll() {
		boolean gbUpdated = false;
		this.hasUnknownAssignments = false;
		this.externallyMaintainedImportMsg = new StringBuilder();

		LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
		if (getGradeEntryByLetter()) {
			lgpm = getGradebookManager().getLetterGradePercentMapping(this.localGradebook);
		}

		if (log.isDebugEnabled()) {
			log.debug("importDataAll()");
		}

		// first, verify imported data is valid
		if (!verifyImportedData(lgpm)) {
			return "spreadsheetVerify";
		}

		final List grAssignments = getGradebookManager().getAssignments(getGradebook().getId());
		final Iterator assignIter = this.assignmentHeaders.iterator();

		// since the first two columns are user ids and name, skip over
		int index = 1;
		if (assignIter.hasNext()) {
			assignIter.next();
		}

		while (assignIter.hasNext()) {
			String assignmentName = (String) assignIter.next();
			String pointsPossibleAsString = null;

			final String[] parsedAssignmentName = assignmentName.split(" \\[");

			assignmentName = parsedAssignmentName[0].trim();
			Double pointsPossible = null;
			if (parsedAssignmentName.length > 1) {
				final String[] parsedPointsPossible = parsedAssignmentName[1].split("\\]");
				if (parsedPointsPossible.length > 0) {
					pointsPossibleAsString = parsedPointsPossible[0].trim();
					try {
						pointsPossible = convertStringToDouble(pointsPossibleAsString);
						pointsPossible = Double.valueOf(FacesUtil.getRoundDown(pointsPossible.doubleValue(), 2));
						if (pointsPossible <= 0) {
							pointsPossibleAsString = null;
						}
					} catch (final ParseException e) {
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
					this.assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignmentName, pointsPossible, null,
							Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
					assignment = getGradebookManager().getAssignment(this.assignmentId);
				} else {
					// for this version, display error message saying non-match between import
					// and current gradebook assignments
					this.hasUnknownAssignments = true;
					this.unknownAssignments.add(assignmentName);

					continue;
				}

				final Iterator it = this.studentRows.iterator();

				if (log.isDebugEnabled()) {
					log.debug("number of student rows " + this.studentRows.size());
				}
				int i = 1;

				while (it.hasNext()) {
					if (log.isDebugEnabled()) {
						log.debug("row " + i);
					}
					final SpreadsheetRow row = (SpreadsheetRow) it.next();
					final List line = row.getRowcontent();

					String userid = "";
					final String user = (String) line.get(0);
					try {
						userid = ((User) this.rosterMap.get(user)).getUserUid();
					} catch (final Exception e) {
						if (log.isDebugEnabled()) {
							log.debug("user " + user + "is not known to the system");
						}
						userid = "";
						// checked when imported from user's system so should not happen at this point.
					}

					if (line.size() > index) {
						final String inputScore = (String) line.get(index);

						if (inputScore != null && inputScore.trim().length() > 0) {
							Double scoreAsDouble = null;
							String scoreAsString = inputScore.trim();

							final AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment, userid, null);

							// truncate input points/% to 2 decimal places
							if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
								try {
									scoreAsDouble = convertStringToDouble(scoreAsString);
									scoreAsDouble = Double.valueOf(FacesUtil.getRoundDown(scoreAsDouble.doubleValue(), 2));
									asnGradeRecord.setPercentEarned(scoreAsDouble);
									asnGradeRecord.setPointsEarned(scoreAsDouble);
									gradeRecords.add(asnGradeRecord);
									log.debug("user " + user + " userid " + userid + " score " + inputScore);

								} catch (final ParseException pe) {
									// this should have been caught during validation, so there is a problem
									log.error("ParseException encountered parsing " + scoreAsString);
								}
							} else if (getGradeEntryByLetter()) {
								scoreAsString = inputScore;

								asnGradeRecord.setLetterEarned(scoreAsString.trim());
								gradeRecords.add(asnGradeRecord);
								if (log.isDebugEnabled()) {
									log.debug("user " + user + " userid " + userid + " score " + inputScore);
								}
							}
						}
					}

					i++;
				}

				gbUpdated = true;
			} else {
				if (!assignment.getPointsPossible().equals(pointsPossible)) {
					if (assignment.isExternallyMaintained()) {
						this.externallyMaintainedImportMsg.append(getLocalizedString("import_assignment_externally_maintained_settings",
								new String[] { Validator.escapeHtml(assignment.getName()),
										Validator.escapeHtml(assignment.getExternalAppName()) })
								+ "<br />");
					} else if (pointsPossible != null) {
						assignment.setPointsPossible(pointsPossible);
						getGradebookManager().updateAssignment(assignment);
						gbUpdated = true;
					}
				}

				gradeRecords = gradeChanges(assignment, this.studentRows, index + 1, lgpm);

				if (gradeRecords.size() == 0) {
					continue; // no changes to current grade record so go to next one
				} else {
					gbUpdated = true;
				}
			}

			getGradebookManager().updateAssignmentGradeRecords(assignment, gradeRecords, getGradebook().getGrade_type());
			getGradebookBean().postEvent("gradebook.importEntire",
					"/gradebook/" + getGradebookId() + "/" + assignment.getName() + "/" + getAuthzLevel(), true);
		}

		// just in case previous attempt had unknown users
		this.hasUnknownUser = false;
		if (gbUpdated) {
			if (!this.hasUnknownAssignments) {
				FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_SUCCESS_STRING));
			} else {
				FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_SOME_SUCCESS_STRING));
			}
		} else if (!this.hasUnknownAssignments) {
			FacesUtil.addRedirectSafeMessage(getLocalizedString(IMPORT_NO_CHANGES));
		}

		setPageName("spreadsheetAll");
		return "spreadsheetAll";
	}

	/**
	 * Returns TRUE if specific value imported from spreadsheet is valid
	 *
	 * @return false if the spreadsheet contains a invalid points possible, an invalid score value, more than one column for the same gb
	 *         item, more than one row for the same student
	 */
	private boolean verifyImportedData(final LetterGradePercentMapping lgpm) {
		if (this.studentRows == null || this.studentRows.isEmpty()) {
			return true;
		}

		if (getGradeEntryByLetter() && (lgpm == null || lgpm.getGradeMap() == null)) {
			FacesUtil.addErrorMessage(getLocalizedString("gb_setup_no_grade_entry_scale"));
			return false;
		}

		// determine the index of the "cumulative" column
		int indexOfCumColumn = -1;
		if (this.assignmentHeaders != null && !this.assignmentHeaders.isEmpty()) {
			// add one b/c assignmentHeaders does not include the username col but studentRows does
			indexOfCumColumn = this.assignmentHeaders.indexOf(getLocalizedString(CUMULATIVE_GRADE_STRING)) + 1;

			// we need to double check that there aren't any duplicate assignments
			// in the spreadsheet. the first column is the student name, so skip
			final List<String> assignmentNames = new ArrayList<String>();
			if (this.assignmentHeaders.size() > 1) {
				for (int i = 1; i < this.assignmentHeaders.size(); i++) {
					if (i == indexOfCumColumn) {
						continue;
					}

					final String header = (String) this.assignmentHeaders.get(i);
					final String[] parsedAssignmentName = header.split(" \\[");
					final String assignmentName = parsedAssignmentName[0].trim();
					if (assignmentNames.contains(assignmentName)) {
						FacesUtil
								.addErrorMessage(getLocalizedString("import_assignment_duplicate_titles", new String[] { assignmentName }));
						return false;
					}

					assignmentNames.add(assignmentName);
				}
			}
		}

		final List<String> uploadedStudents = new ArrayList<String>();

		for (int row = 0; row < this.studentRows.size(); row++) {
			final SpreadsheetRow scoreRow = (SpreadsheetRow) this.studentRows.get(row);
			final List studentScores = scoreRow.getRowcontent();

			// verify that a student doesn't appear more than once in the ss
			if (studentScores != null) {
				final String username = (String) studentScores.get(0);
				if (username != null) {
					if (uploadedStudents.contains(username)) {
						FacesUtil.addErrorMessage(getLocalizedString("import_assignment_duplicate_student", new String[] { username }));
						return false;
					}

					uploadedStudents.add(username);
				}
			}

			// start with col 2 b/c the first two are eid and name
			if (studentScores != null && studentScores.size() > 2) {
				for (int i = 2; i < studentScores.size(); i++) {
					if (i == indexOfCumColumn) {
						continue;
					}

					final String scoreAsString = ((String) studentScores.get(i)).trim();
					if (scoreAsString != null && scoreAsString.length() > 0) {

						if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
							Double scoreAsDouble;

							try {
								if (log.isDebugEnabled()) {
									log.debug("checking if " + scoreAsString + " is a numeric value");
								}

								scoreAsDouble = convertStringToDouble(scoreAsString);
								// check for negative values
								if (scoreAsDouble.doubleValue() < 0) {
									if (log.isDebugEnabled()) {
										log.debug(scoreAsString + " is not a positive value");
									}
									FacesUtil.addErrorMessage(getLocalizedString(IMPORT_ASSIGNMENT_NEG_VALUE));

									return false;
								}
							} catch (final ParseException e) {
								if (log.isDebugEnabled()) {
									log.debug(scoreAsString + " is not a numeric value");
								}
								FacesUtil.addErrorMessage(getLocalizedString(IMPORT_ASSIGNMENT_NOTSUPPORTED));

								return false;
							}

						} else if (getGradeEntryByLetter()) {
							final String standardizedLetterGrade = lgpm.standardizeInputGrade(scoreAsString);
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
	 * Returns a list of AssignmentGradeRecords that are different than in current Gradebook. Returns empty List if no differences found.
	 *
	 * @param assignment The GradebookAssignment object whose grades need to be checked
	 * @param fromSpreadsheet The rows of grades from the imported spreadsheet
	 * @param index The column of spreadsheet to check
	 *
	 * @return List containing AssignmentGradeRecords for those student's grades that have changed
	 */
	private List gradeChanges(final GradebookAssignment assignment, final List fromSpreadsheet, final int index,
			final LetterGradePercentMapping lgpm) {
		final List updatedGradeRecords = new ArrayList();
		final List studentUids = new ArrayList();
		final List studentRowsWithUids = new ArrayList();

		Iterator it = fromSpreadsheet.iterator();

		while (it.hasNext()) {
			final SpreadsheetRow row = (SpreadsheetRow) it.next();
			final List line = row.getRowcontent();

			String userid = "";
			final String user = ((String) line.get(0)).toLowerCase();
			try {
				userid = ((User) this.rosterMap.get(user)).getUserUid();

				// create list of uids to get current grades from gradebook
				studentUids.add(userid);

				// add uid to each row so can check spreadsheet value against currently stored
				final List linePlus = new ArrayList();
				linePlus.add(userid);
				linePlus.addAll(line);
				studentRowsWithUids.add(linePlus);

			} catch (final Exception e) {
				// Weirdness. Should be caught when importing, not here
			}
		}

		final List gbGrades = getGradebookManager().getAssignmentGradeRecordsConverted(assignment, studentUids);

		// now do the actual comparison
		it = studentRowsWithUids.iterator();

		boolean updatingExternalGrade = false;
		while (it.hasNext()) {
			final List aRow = (List) it.next();

			final String userid = (String) aRow.get(0);

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
					} catch (final ParseException pe) {
						// this should have already been validated at this point, so there is
						// something wrong if we made it here
						log.error("ParseException encountered while checking for grade updates with score: " + score);
						updateScore = false;
					}

					// truncate to 2 decimal places
					if (scoreEarned != null) {
						scoreEarned = new Double(FacesUtil.getRoundDown(scoreEarned.doubleValue(), 2));
					}
				}

				if (updateScore) {
					if (gr == null) {
						if (scoreEarned != null) {
							if (!assignment.isExternallyMaintained()) {
								gr = new AssignmentGradeRecord(assignment, userid, scoreEarned);
								gr.setPercentEarned(scoreEarned); // manager will handle if % vs point grading
								updatedGradeRecords.add(gr);
							} else {
								updatingExternalGrade = true;
							}
						}
					} else {
						// we need to truncate points earned to 2 decimal places to more accurately
						// see if it was changed - scores that are entered as % can be stored with
						// unlimited decimal places in db
						Double gbScoreEarned = null;
						if (getGradeEntryByPercent()) {
							gbScoreEarned = gr.getPercentEarned();
						} else {
							gbScoreEarned = gr.getPointsEarned();
						}

						if (gbScoreEarned != null) {
							gbScoreEarned = new Double(FacesUtil.getRoundDown(gbScoreEarned.doubleValue(), 2));
						}

						// 3 ways points earned different: 1 null other not (both ways) or actual
						// values different
						if ((gbScoreEarned == null && scoreEarned != null) ||
								(gbScoreEarned != null && scoreEarned == null) ||
								(gbScoreEarned != null && scoreEarned != null
										&& gbScoreEarned.doubleValue() != scoreEarned.doubleValue())) {

							gr.setPointsEarned(scoreEarned); // manager will use correct field depending on grade entry method
							gr.setPercentEarned(scoreEarned);
							if (!assignment.isExternallyMaintained()) {
								updatedGradeRecords.add(gr);
							} else {
								updatingExternalGrade = true;
							}
						}
					}
				}
			} else if (getGradeEntryByLetter()) {
				if (lgpm == null || lgpm.getGradeMap() == null) {
					return null;
				}
				if (score != null && score.length() > 0) {
					score = lgpm.standardizeInputGrade(score);
				}

				if (gr == null) {
					if (score != null && score.length() > 0) {
						if (!assignment.isExternallyMaintained()) {
							gr = new AssignmentGradeRecord(assignment, userid, null);
							gr.setLetterEarned(score);
							updatedGradeRecords.add(gr);
						} else {
							updatingExternalGrade = true;
						}
					}
				} else {
					final String gbLetterEarned = gr.getLetterEarned();

					if ((gbLetterEarned != null && !gbLetterEarned.equals(score)) ||
							(gbLetterEarned == null && score != null)) {

						gr.setLetterEarned(score);
						if (!assignment.isExternallyMaintained()) {
							updatedGradeRecords.add(gr);
						} else {
							updatingExternalGrade = true;
						}
					}
				}
			}
		}

		if (updatingExternalGrade) {
			this.externallyMaintainedImportMsg.append(getLocalizedString("import_assignment_externally_maintained_grades",
					new String[] { Validator.escapeHtml(assignment.getName()), Validator.escapeHtml(assignment.getExternalAppName()) })
					+ "<br/>");
		}

		return updatedGradeRecords;
	}

	/**
	 * Finds the AssignmentGradeRecord for userid passed in (if it exists).
	 *
	 * @param gbGrades List of AssignmentGradeRecord objects.
	 * @param userid String of user id to find.
	 *
	 * @return AssignmentGradeRecord if it's student id matches id passed in. NULL otherwise.
	 */
	private AssignmentGradeRecord findGradeRecord(final List gbGrades, final String userid) {
		final Iterator it = gbGrades.iterator();

		while (it.hasNext()) {
			final AssignmentGradeRecord agr = (AssignmentGradeRecord) it.next();

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
	private GradebookAssignment getAssignmentByName(final List assignList, final String name) {
		for (final Iterator assignIter = assignList.iterator(); assignIter.hasNext();) {
			final GradebookAssignment assignment = (GradebookAssignment) assignIter.next();

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
		this.hasUnknownUser = false;
		this.hasUnknownAssignments = false;
		return "spreadsheetAll";
	}

	// save grades and comments
	public String saveGrades() {

		if (log.isDebugEnabled()) {
			log.debug("create assignment and save grades");
		}
		if (log.isDebugEnabled()) {
			log.debug("first check if all variables are numeric");
		}

		log.debug("********************" + this.scores);

		LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
		if (getGradeEntryByLetter()) {
			lgpm = getGradebookManager().getLetterGradePercentMapping(getGradebook());
		}

		final Iterator iter = this.scores.entrySet().iterator();
		while (iter.hasNext()) {
			final Map.Entry entry = (Map.Entry) iter.next();
			if (!entry.getKey().equals("GradebookAssignment")) {
				if (getGradeEntryByPoints() || getGradeEntryByPercent()) {
					final String points = (String) entry.getValue();
					try {
						if (log.isDebugEnabled()) {
							log.debug("checking if " + points + " is a numeric value");
						}

						final double score = convertStringToDouble(points);

						if (score < 0) {
							FacesUtil.addErrorMessage(getLocalizedString("import_assignment_negative"));
							return "spreadsheetPreview";
						}

					} catch (final ParseException e) {
						if (log.isDebugEnabled()) {
							log.debug(points + " is not a numeric value");
						}
						FacesUtil.addErrorMessage(getLocalizedString("import_assignment_notsupported"));

						return "spreadsheetPreview";
					}
				} else if (getGradeEntryByLetter()) {
					if (lgpm.getGradeMap() == null) {
						FacesUtil.addErrorMessage(getLocalizedString("gb_setup_no_grade_entry_scale"));
						return "spreadsheetPreview";
					}
					final String letterScore = (String) entry.getValue();
					if (letterScore != null && letterScore.length() > 0) {
						final String formattedLetterScore = lgpm.standardizeInputGrade(letterScore);
						if (formattedLetterScore == null) {
							FacesUtil.addErrorMessage(getLocalizedString("import_assignment_invalid_letter"));
							return "spreadsheetPreview";
						}
					}
				}
			}
		}

		try {
			final Category newCategory = retrieveSelectedCategory();
			if (newCategory != null) {
				this.assignmentId = getGradebookManager().createAssignmentForCategory(getGradebookId(), newCategory.getId(),
						this.assignment.getName(), this.assignment.getPointsPossible(), this.assignment.getDueDate(),
						Boolean.valueOf(this.assignment.isNotCounted()), Boolean.valueOf(this.assignment.isReleased()),
						this.assignment.isExtraCredit());
			} else {
				this.assignmentId = getGradebookManager().createAssignment(getGradebookId(), this.assignment.getName(),
						this.assignment.getPointsPossible(), this.assignment.getDueDate(), Boolean.valueOf(this.assignment.isNotCounted()),
						Boolean.valueOf(this.assignment.isReleased()), this.assignment.isExtraCredit());
			}

			FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] { this.assignment.getName() }));

			this.assignment = getGradebookManager().getAssignment(this.assignmentId);
			final List gradeRecords = new ArrayList();

			// initialize comment List
			List comments = new ArrayList();
			// check if a comments column is selected for the defalt select item value is
			// 0 which mean no comments to be imported
			if (this.selectedCommentsColumnId != null && this.selectedCommentsColumnId > 0) {
				comments = createCommentList(this.assignment);
			}

			log.debug("remove title entry form map");

			this.scores.remove("GradebookAssignment");
			log.debug("iterate through scores and and save assignment grades");

			final Iterator it = this.scores.entrySet().iterator();
			while (it.hasNext()) {

				final Map.Entry entry = (Map.Entry) it.next();
				final String uid = (String) entry.getKey();
				final String scoreAsString = (String) entry.getValue();
				if (scoreAsString != null && scoreAsString.trim().length() > 0) {
					if (getGradeEntryByPercent() || getGradeEntryByPoints()) {
						try {
							Double scoreAsDouble;
							scoreAsDouble = convertStringToDouble(scoreAsString);
							if (scoreAsDouble != null) {
								scoreAsDouble = Double.valueOf(FacesUtil.getRoundDown(scoreAsDouble.doubleValue(), 2));
							}
							final AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(this.assignment, uid, scoreAsDouble);
							asnGradeRecord.setPercentEarned(scoreAsDouble); // in case gb entry by % - sorted out in manager
							gradeRecords.add(asnGradeRecord);
							log.debug("added grades for {} - score {}", uid, scoreAsString);
						} catch (final ParseException pe) {
							// the score should have already been validated at this point, so
							// there is something wrong
							log.error("ParseException encountered while parsing value: {} Score was not updated.", scoreAsString);
						}
					} else if (getGradeEntryByLetter()) {
						final AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(this.assignment, uid, null);
						asnGradeRecord.setLetterEarned(lgpm.standardizeInputGrade(scoreAsString));
						gradeRecords.add(asnGradeRecord);
					}
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("persist grade records to database");
			}
			getGradebookManager().updateAssignmentGradesAndComments(this.assignment, gradeRecords, comments);
			getGradebookBean().postEvent("gradebook.importItem",
					"/gradebook/" + getGradebookId() + "/" + this.assignment.getName() + "/" + getAuthzLevel(), true);

			return "spreadsheetListing";

		} catch (final ConflictingAssignmentNameException e) {
			log.error(e.getMessage());
			FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
		}

		return null;
	}

	/**
	 * method creates a collection of comment objects from a the saved spreadsheet and selected column. requires an assignment as parameter
	 * to set the gradableObject property of the comment
	 *
	 * @param assignmentTobeCommented
	 * @return List of comment comment objects
	 */
	public List createCommentList(final GradebookAssignment assignmentTobeCommented) {

		final List comments = new ArrayList();
		final Iterator it = this.studentRows.iterator();
		while (it.hasNext()) {
			final SpreadsheetRow row = (SpreadsheetRow) it.next();
			final List line = row.getRowcontent();

			String userid = "";
			final String user = (String) line.get(0);
			try {
				userid = ((User) this.rosterMap.get(line.get(0))).getUserUid();
				final String commentText = (String) line.get(this.selectedCommentsColumnId.intValue());
				if ((!commentText.equals(""))) {
					final Comment comment = new Comment(userid, commentText, assignmentTobeCommented);
					comments.add(comment);
				}
			} catch (final Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("student is required  and " + user + "is not known to this gradebook");
				}
			}
		}
		return comments;
	}

	public Integer getSelectedCommentsColumnId() {
		return this.selectedCommentsColumnId;
	}

	public void setSelectedCommentsColumnId(final Integer selectedCommentsColumnId) {
		this.selectedCommentsColumnId = selectedCommentsColumnId;
	}

	/**
	 *
	 */

	public class Spreadsheet implements Serializable {

		@Getter
		@Setter
		private String title;

		@Getter
		@Setter
		private Date date;

		@Getter
		@Setter
		private String userId;

		@Getter
		@Setter
		private String contents;

		@Getter
		@Setter
		private String displayName;

		@Getter
		@Setter
		private Long gradebookId;

		@Getter
		@Setter
		private String filename;

		@Getter
		@Setter
		private List lineitems;

		@Getter
		@Setter
		private Map selectedAssignment;

		public Spreadsheet(final String title, final Date date, final String userId, final String contents) {

			log.debug("loading Spreadsheet()");

			this.title = title;
			this.date = date;
			this.userId = userId;
			this.contents = contents;
		}

		public Spreadsheet() {
			log.debug("loading Spreadsheet()");
		}

		@Override
		public String toString() {
			return "Spreadsheet{" +
					"title='" + this.title + '\'' +
					", date=" + this.date +
					", userId='" + this.userId + '\'' +
					", contents='" + this.contents + '\'' +
					", displayName='" + this.displayName + '\'' +
					", gradebookId=" + this.gradebookId +
					", filename='" + this.filename + '\'' +
					", lineitems=" + this.lineitems +
					", selectedAssignment=" + this.selectedAssignment +
					'}';
		}
	}

	/**
	 * spreadsheet header class
	 */
	public class SpreadsheetHeader implements Serializable {

		@Getter
		@Setter
		private List header;

		@Getter
		@Setter
		private int columnCount;

		@Getter
		@Setter
		private boolean hasCumulative;

		public List getHeaderWithoutUser() {
			final List head = this.header;
			head.remove(0);
			return head;
		}

		public List getHeaderWithoutUserAndCumulativeGrade() {
			final List head = getHeaderWithoutUser();
			// If CSV from Roster page, last column will be Cumulative Grade
			// so remove from header list
			if (head.get(head.size() - 1).equals("Cumulative")) {
				head.remove(head.size() - 1);
			}

			return head;
		}

		public SpreadsheetHeader(final String source) {

			log.debug("creating header from " + source);

			this.header = new ArrayList();
			final CSV csv = new CSV();
			this.header = csv.parse(source);
			this.columnCount = this.header.size();
			this.hasCumulative = this.header.get(this.columnCount - 1).equals("Cumulative");
		}

	}

	/**
	 * spreadsheetRow class
	 */
	public class SpreadsheetRow implements Serializable {

		@Getter
		@Setter
		private List rowcontent;

		@Getter
		@Setter
		private int columnCount;

		@Getter
		@Setter
		private String userDisplayName;

		@Getter
		@Setter
		private String userId;

		@Getter
		@Setter
		private String userUid;

		@Getter
		@Setter
		private boolean isKnown;

		public SpreadsheetRow(final String source) {

			// this may be instantiated before SpreadsheetUploadBean is initialized, so make sure
			// the rosterMap is populated
			if (SpreadsheetUploadBean.this.rosterMap == null) {
				initializeRosterMap();
			}

			if (log.isDebugEnabled()) {
				log.debug("creating row from string " + source);
			}
			this.rowcontent = new ArrayList();
			final CSV csv = new CSV();
			this.rowcontent = csv.parse(source);

			// derive the user information
			final String userContent = (String) this.rowcontent.get(0);
			if (userContent != null) {
				this.userId = userContent.toLowerCase();

				// check to see if this student is in the roster
				if (SpreadsheetUploadBean.this.rosterMap.containsKey(this.userId)) {
					this.isKnown = true;
					final User user = (User) SpreadsheetUploadBean.this.rosterMap.get(this.userId);
					this.userDisplayName = user.getDisplayName();
					this.userUid = user.getUserUid();

					if (log.isDebugEnabled()) {
						log.debug("get userid " + this.userId + "username is " + this.userDisplayName);
					}
				} else {
					this.isKnown = false;
					this.userDisplayName = getLocalizedString("import_preview_unknown_name");
					this.userUid = null;

					if (log.isDebugEnabled()) {
						log.debug("User " + this.userId + " is unknown to this gradebook");
					}
				}
			} else {
				this.isKnown = false;
				this.userDisplayName = getLocalizedString("import_preview_unknown_name");
				this.userUid = null;
				this.userId = null;

				if (log.isDebugEnabled()) {
					log.debug("Null userId in spreadsheet");
				}
			}

		}
	}

	// csv class to handle

	/**
	 * Parse comma-separated values (CSV), a common Windows file format. Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
	 * <p>
	 * Inner logic adapted from a C++ original that was Copyright (C) 1999 Lucent Technologies Excerpted from 'The Practice of Programming'
	 * by Brian W. Kernighan and Rob Pike.
	 * <p>
	 * Included by permission of the http://tpop.awl.com/ web site, which says: "You may use this code for any purpose, as long as you leave
	 * the copyright notice and book citation attached." I have done so.
	 *
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

		/**
		 * Construct a CSV parser with a given separator.
		 *
		 * @param sep The single char for the separator (not a list of separator characters)
		 */
		public CSV(final char sep) {
			this.fieldSep = sep;
		}

		/** The fields in the current String */
		protected List list = new ArrayList();

		/** the separator char for this parser */
		protected char fieldSep;

		/**
		 * parse: break the input String into fields
		 *
		 * @return java.util.Iterator containing each field from the original as a String, in order.
		 */
		public List parse(final String line) {
			final StringBuilder sb = new StringBuilder();
			this.list.clear(); // recycle to initial state
			int i = 0;

			if (line.length() == 0) {
				this.list.add(line);
				return this.list;
			}

			do {
				sb.setLength(0);
				if (i < line.length() && line.charAt(i) == '"') {
					i = advQuoted(line, sb, ++i); // skip quote
				} else {
					i = advPlain(line, sb, i);
				}
				this.list.add(sb.toString());
				i++;
			} while (i < line.length());
			if (log.isDebugEnabled()) {
				final StringBuilder logBuffer = new StringBuilder("Parsed " + line + " as: ");
				for (final Iterator iter = this.list.iterator(); iter.hasNext();) {
					logBuffer.append(iter.next());
					if (iter.hasNext()) {
						logBuffer.append(", ");
					}
				}
				log.debug("Parsed source string " + line + " as " + logBuffer.toString() + ", length=" + this.list.size());
			}
			return this.list;

		}

		/** advQuoted: quoted field; return index of next separator */
		protected int advQuoted(final String s, final StringBuilder sb, final int i) {
			int j;
			final int len = s.length();
			for (j = i; j < len; j++) {
				if (s.charAt(j) == '"' && j + 1 < len) {
					if (s.charAt(j + 1) == '"') {
						j++; // skip escape char
					} else if (s.charAt(j + 1) == this.fieldSep) { // next delimeter
						j++; // skip end quotes
						break;
					}
				} else if (s.charAt(j) == '"' && j + 1 == len) { // end quotes at end of line
					break; // done
				}
				sb.append(s.charAt(j)); // regular character.
			}
			return j;
		}

		/** advPlain: unquoted field; return index of next separator */
		protected int advPlain(final String s, final StringBuilder sb, final int i) {
			int j;

			j = s.indexOf(this.fieldSep, i); // look for separator
			if (j == -1) { // none found
				sb.append(s.substring(i));
				return s.length();
			} else {
				sb.append(s.substring(i, j));
				return j;
			}
		}
	}

	// ************************ EXCEL file parsing *****************************
	/**
	 * method converts an input stream to an List consisting of strings representing a line. The input stream must be for an xls file.
	 *
	 * @param inputStream
	 * @return contents
	 */
	private List excelToArray(final InputStream inputStreams) throws IOException {
		final HSSFWorkbook wb = new HSSFWorkbook(inputStreams);

		// Convert an Excel file to csv
		final HSSFSheet sheet = wb.getSheetAt(0);
		final List array = new ArrayList();
		final Iterator it = sheet.rowIterator();
		while (it.hasNext()) {
			final HSSFRow row = (HSSFRow) it.next();
			final String rowAsString = fromHSSFRowtoCSV(row);
			if (rowAsString.replaceAll(",", "").replaceAll("\"", "").equals("")) {
				continue;
			}
			array.add(fromHSSFRowtoCSV(row));
		}
		return array;
	}

	private String fromHSSFRowtoCSV(final HSSFRow row) {
		final StringBuffer csvRow = new StringBuffer();
		final int l = row.getLastCellNum();
		for (int i = 0; i < l; i++) {
			final HSSFCell cell = row.getCell((short) i);
			String cellValue = "";
			if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
				cellValue = "";
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				cellValue = "\"" + cell.getStringCellValue() + "\"";
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				final double value = cell.getNumericCellValue();
				cellValue = getNumberFormat().format(value);
				cellValue = "\"" + cellValue + "\"";
			}

			csvRow.append(cellValue);

			if (i < l) {
				csvRow.append(getCsvDelimiter().toCharArray()[0]);
			}
		}
		return csvRow.toString();

	}

	/**
	 * Parse newer OOXML Excel Spreadsheets
	 *
	 * @param inputStreams
	 * @return
	 * @throws IOException
	 */
	private List<String> excelOOXMLToArray(final InputStream inputStreams) throws IOException {
		final XSSFWorkbook wb = new XSSFWorkbook(inputStreams);

		// Convert an Excel OOXML (.xslx) file to csv
		final XSSFSheet sheet = wb.getSheetAt(0);
		final List<String> array = new ArrayList<String>();
		final Iterator it = sheet.rowIterator();
		while (it.hasNext()) {
			final XSSFRow row = (XSSFRow) it.next();
			final String rowAsString = fromXSSFRowtoCSV(row);
			if (rowAsString.replaceAll(",", "").replaceAll("\"", "").equals("")) {
				continue;
			}
			array.add(fromXSSFRowtoCSV(row));
		}
		return array;
	}

	private String fromXSSFRowtoCSV(final XSSFRow row) {
		final StringBuffer csvRow = new StringBuffer();
		final int l = row.getLastCellNum();
		for (int i = 0; i < l; i++) {
			final XSSFCell cell = row.getCell((short) i);
			String cellValue = "";
			if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
				cellValue = "";
			} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				cellValue = "\"" + cell.getStringCellValue() + "\"";
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				final double value = cell.getNumericCellValue();
				cellValue = getNumberFormat().format(value);
				cellValue = "\"" + cellValue + "\"";
			}

			csvRow.append(cellValue);

			if (i < l) {
				csvRow.append(getCsvDelimiter().toCharArray()[0]);
			}
		}
		return csvRow.toString();
	}

	/**
	 * Process an upload ActionEvent from spreadsheetUpload.jsp or spreadsheetEntireGBImport.jsp Source of this action is the Upload button
	 * on either page
	 *
	 * @param event
	 */
	public void launchFilePicker(final ActionEvent event) {
		try {
			final String titleText = FacesUtil.getLocalizedString("upload_view_page_title");
			final String instructionText = FacesUtil.getLocalizedString("upload_view_instructions_text");

			final ToolSession currentToolSession = SessionManager.getCurrentToolSession();
			// SAK-14173 - store title
			currentToolSession.setAttribute(IMPORT_TITLE, this.title);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS, FilePickerHelper.CARDINALITY_SINGLE);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, titleText);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT, instructionText);
			currentToolSession.setAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER,
					ComponentManager.get("org.sakaiproject.content.api.ContentResourceFilter.spreadsheetCsvFile"));
			final ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("sakai.filepicker.helper/tool");
		} catch (final Exception e) {
			log.error(this + ".launchFilePicker - " + e);
		}
	}

	/**
	 * SAK-14173 - This event will cause the title to be stored in the bean so that it can be stored in the session when the user goes to
	 * choose a file and will not be lost
	 *
	 * @param event JSF value change event
	 */
	public void changeTitle(final ValueChangeEvent event) {
		this.title = (String) event.getNewValue();
	}

	/**
	 * SAK-14173 - Clears all the session variables stored as part of the spreadsheet upload code
	 *
	 * @param event JSF event
	 */
	public void cancelAndClearSession(final ActionEvent event) {
		clearImportTitle();
		clearPickedFile();
	}

	/**
	 * SAK-14173 - Clears the import title from the session
	 */
	private void clearImportTitle() {
		final ToolSession currentToolSession = SessionManager.getCurrentToolSession();
		currentToolSession.removeAttribute(IMPORT_TITLE);
	}

	/**
	 * Extract and store the reference to the uploaded file. Only process the first reference - don't do multiple uploads.
	 *
	 */
	public void savePickedUploadFile() {
		final ToolSession session = SessionManager.getCurrentToolSession();
		if (session.getAttribute(PICKED_FILE_REFERENCE) != null) {
			this.pickedFileReference = (String) session.getAttribute(PICKED_FILE_REFERENCE);
			this.pickedFileDesc = (String) session.getAttribute(PICKED_FILE_DESC);
		}
	}

	/**
	 * Use the uploaded file reference to get an InputStream. Must be a reference to a ContentResource
	 *
	 * @param resource the ContentResource associated with {@link #pickedFileReference}. see {@link #getPickedContentResource()}
	 * @return InputStream
	 */
	private InputStream getPickedFileStream(final ContentResource resource) {
		InputStream inStream = null;

		if (resource != null) {
			try {
				inStream = resource.streamContent();
			} catch (final ServerOverloadException soe) {
				log.error(soe.getMessage(), soe);
			}
		}

		return inStream;
	}

	/**
	 *
	 * @return the ContentResource associated with the {@link #pickedFileReference} property. returns null if the entity is not a
	 *         ContentResource
	 */
	private ContentResource getPickedContentResource() {
		ContentResource resource = null;
		if (this.pickedFileReference != null) {
			final Reference ref = EntityManager.newReference(this.pickedFileReference);
			if (ref != null) {
				final Entity ent = ref.getEntity();
				if (ent instanceof ContentResource) {
					// entity came from file picker, so it should be a content resource
					resource = (ContentResource) ent;
				}
			}
		}

		return resource;
	}

	private void clearPickedFile() {
		final ToolSession session = SessionManager.getCurrentToolSession();
		session.removeAttribute(PICKED_FILE_REFERENCE);
		session.removeAttribute(PICKED_FILE_DESC);

		this.pickedFileDesc = null;
		this.pickedFileReference = null;
	}

	/**
	 * @return the pickedFileDesc
	 */
	public String getPickedFileDesc() {
		// check the session attribute first
		final ToolSession session = SessionManager.getCurrentToolSession();
		if (session.getAttribute(PICKED_FILE_DESC) != null) {
			this.pickedFileDesc = (String) session.getAttribute(PICKED_FILE_DESC);
		}

		return this.pickedFileDesc;
	}

	/**
	 * @param pickedFileDesc the pickedFileDesc to set
	 */
	public void setPickedFileDesc(final String pickedFileDesc) {
		this.pickedFileDesc = pickedFileDesc;
	}

	/**
	 *
	 * @return the number of users with the student-type role enrolled in the current site
	 */
	private int getNumStudentsInSite() {
		final List enrollments = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
		return enrollments != null ? enrollments.size() : 0;
	}

	private String getCsvDelimiter() {
		if (this.csvDelimiter == null) {
			this.csvDelimiter = ServerConfigurationService.getString("csv.separator", ",");
		}

		return this.csvDelimiter;
	}

	/**
	 *
	 * @param doubleAsString
	 * @return a locale-aware Double value representation of the given String
	 * @throws ParseException
	 */
	private Double convertStringToDouble(final String doubleAsString) throws ParseException {
		Double scoreAsDouble = null;
		if (doubleAsString != null) {
			final Number numericScore = getNumberFormat().parse(doubleAsString.trim());
			scoreAsDouble = numericScore.doubleValue();
		}

		return scoreAsDouble;
	}

	private NumberFormat getNumberFormat() {
		if (this.numberFormat == null) {
			this.numberFormat = NumberFormat.getInstance(new ResourceLoader().getLocale());
		}

		return this.numberFormat;
	}

	public String getDateEntryFormatDescription() {
		return this.date_entry_format_description;
	}

	public Boolean getExtraCreditCatSelected() {
		return this.extraCreditCatSelected;
	}
}
