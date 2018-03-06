/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterPdf;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradesToSpreadsheetConverter;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CourseGradeDetailsBean extends EnrollmentTableBean {
	// View maintenance fields - serializable.
	private List scoreRows;
	private CourseGrade courseGrade;
	private List updatedGradeRecords;
	private GradeMapping gradeMapping;
	private double totalPoints;
	private String courseGradesConverterPlugin;
	private String standardExportDefaultFields;
	private boolean allStudentsViewOnly = true;
	private boolean enableCustomExport;

	//Export Field data options
	private boolean includeUsereid;
	private boolean includeSortname;
	private boolean includeCoursegrade;
	private boolean includeFinalscore;
	private boolean includeCalculatedgrade;
	private boolean includeGradeoverride;
	private boolean includeLastmodifieddate;

	private String exportType;
	private List<SelectItem> exportFormats = new ArrayList<SelectItem>();

	public String getExportType() {
		return this.exportType;
	}

	public void setExportType(final String exportType) {
		this.exportType = exportType;
	}

	public List<SelectItem> getExportFormats() {
		return this.exportFormats;
	}

	public void setExportFormats(final List<SelectItem> exportFormats) {
		this.exportFormats = exportFormats;
	}

    public boolean isIncludeUsereid() {
		return this.includeUsereid;
	}

	public void setIncludeUsereid(final boolean includeUsereid) {
		this.includeUsereid = includeUsereid;
	}

	public boolean isIncludeSortname() {
		return this.includeSortname;
	}

	public void setIncludeSortname(final boolean includeSortname) {
		this.includeSortname = includeSortname;
	}

	public boolean isIncludeCoursegrade() {
		return this.includeCoursegrade;
	}

	public void setIncludeCoursegrade(final boolean includeCoursegrade) {
		this.includeCoursegrade = includeCoursegrade;
	}

	public boolean isIncludeFinalscore() {
		return this.includeFinalscore;
	}

	public void setIncludeFinalscore(final boolean includeFinalscore) {
		this.includeFinalscore = includeFinalscore;
	}

	public boolean isIncludeCalculatedgrade() {
		return this.includeCalculatedgrade;
	}

	public void setIncludeCalculatedgrade(final boolean includeCalculatedgrade) {
		this.includeCalculatedgrade = includeCalculatedgrade;
	}

	public boolean isIncludeGradeoverride() {
		return this.includeGradeoverride;
	}

	public void setIncludeGradeoverride(final boolean includeGradeoverride) {
		this.includeGradeoverride = includeGradeoverride;
	}

	public boolean isIncludeLastmodifieddate() {
		return this.includeLastmodifieddate;
	}

	public void setIncludeLastmodifieddate(final boolean includeLastmodifieddate) {
		this.includeLastmodifieddate = includeLastmodifieddate;
	}



	public class ScoreRow implements Serializable {
        private EnrollmentRecord enrollment;
        private CourseGradeRecord courseGradeRecord;
        private List eventRows;
        private boolean userCanGrade;


		public ScoreRow() {
		}
		public ScoreRow(final EnrollmentRecord enrollment, final CourseGradeRecord courseGradeRecord, final List gradingEvents, final boolean userCanGrade) {
            this.enrollment = enrollment;
			this.courseGradeRecord = courseGradeRecord;
			this.userCanGrade = userCanGrade;

            this.eventRows = new ArrayList();
            for (final Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	final GradingEvent gradingEvent = (GradingEvent)iter.next();
            	this.eventRows.add(new GradingEventRow(gradingEvent));
            }
		}

		/**
		 *
		 * @return letter grade representation of grade or null if no course grade yet
		 */
        public String getCalculatedLetterGrade() {
        	final Double grade = this.courseGradeRecord.getAutoCalculatedGrade();
        	String letterGrade = null;
        	if (grade != null) {
				letterGrade = CourseGradeDetailsBean.this.gradeMapping.getMappedGrade(this.courseGradeRecord.getNonNullAutoCalculatedGrade());
			}
        	return letterGrade;
        }

        /**
         * Because the PrecisePercentageConverter is actually rounding at 2 decimals instead of
         * truncating, do the formatting here.
         * @return percent representation of grade or null if no grade yet
         */
        public Double getCalculatedPercentGrade() {
        	Double grade = this.courseGradeRecord.getAutoCalculatedGrade();
        	if (grade != null) {
        		// to emulate the converter, truncate to 4 decimal places, then return 2
        		grade = new Double(FacesUtil.getRoundDown(grade.doubleValue(), 4));
        		final BigDecimal bdGrade = (new BigDecimal(grade.toString())).setScale(2, BigDecimal.ROUND_DOWN);
        		grade = new Double(bdGrade.doubleValue());
        	}

        	return grade;
        }

        public CourseGradeRecord getCourseGradeRecord() {
            return this.courseGradeRecord;
        }
        public void setCourseGradeRecord(final CourseGradeRecord courseGradeRecord) {
            this.courseGradeRecord = courseGradeRecord;
        }

        public String getEnteredGrade() {
            return this.courseGradeRecord.getEnteredGrade();
        }
        public void setEnteredGrade(final String enteredGrade) {
        	final String originalEnteredGrade = this.courseGradeRecord.getEnteredGrade();
        	if (!StringUtils.equals(enteredGrade, originalEnteredGrade)) {
        		this.courseGradeRecord.setEnteredGrade(enteredGrade);
        		CourseGradeDetailsBean.this.updatedGradeRecords.add(this.courseGradeRecord);
        	}
        }

        public EnrollmentRecord getEnrollment() {
            return this.enrollment;
        }

        public List getEventRows() {
        	return this.eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("course_grade_details_log_title", new String[] {this.enrollment.getUser().getDisplayName()});
        }
        public boolean isUserCanGrade() {
        	return this.userCanGrade;
        }
	}

	@Override
	protected void init() {
		super.init();

		// Clear view state.
		this.scoreRows = new ArrayList();
		this.courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		this.updatedGradeRecords = new ArrayList();

        this.gradeMapping = getGradebook().getSelectedGradeMapping();
        this.totalPoints = getGradebookManager().getTotalPoints(getGradebookId());

		this.enableCustomExport = ServerConfigurationService.getBoolean("gradebook.institutional.export.enabled",false);

		//Default standard export fields
		this.standardExportDefaultFields = ServerConfigurationService.getString("gradebook.standard.export.default.fields","usereid,sortname,coursegrade");
		updateExportFieldStatus(this.standardExportDefaultFields);
		if (this.exportFormats.isEmpty()){
			this.exportFormats.add(new SelectItem("CSV"));
			this.exportFormats.add(new SelectItem("Excel"));
			this.exportFormats.add(new SelectItem("PDF"));
		}
		this.exportType = "CSV";

		// Set up score rows.
        final Map enrollmentMap = getOrderedEnrollmentMapForCourseGrades();
		List studentUids = new ArrayList(enrollmentMap.keySet());
		List gradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecordsWithStats(this.courseGrade, studentUids);

		if (!isEnrollmentSort()) {
			// Need to sort and page based on a scores column.
			final String sortColumn = getSortColumn();
			Comparator comparator = null;
			if (sortColumn.equals(CourseGrade.SORT_BY_CALCULATED_GRADE) ||
                sortColumn.equals(CourseGrade.SORT_BY_POINTS_EARNED)) {
                comparator = CourseGradeRecord.calcComparator;
            } else if (sortColumn.equals(CourseGrade.SORT_BY_OVERRIDE_GRADE)) {
            	comparator = CourseGradeRecord.getOverrideComparator(this.courseGrade.getGradebook().getSelectedGradeMapping());
            }
            if (comparator != null) {
	            Collections.sort(gradeRecords, comparator);
	        }

			final List scoreSortedStudentUids = new ArrayList();
			for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				final CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
				scoreSortedStudentUids.add(cgr.getStudentId());
			}

			// Put enrollments with no scores at the beginning of the final list.
			studentUids.removeAll(scoreSortedStudentUids);

			// Add all sorted enrollments with scores into the final list
			studentUids.addAll(scoreSortedStudentUids);

			studentUids = finalizeSortingAndPaging(studentUids);
		}

		// Get all of the grading events for these enrollments on this assignment
		final GradingEvents allEvents = getGradebookManager().getGradingEvents(this.courseGrade, studentUids);

		final Map gradeRecordMap = new HashMap();
		for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			final CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
			if (studentUids.contains(gradeRecord.getStudentId())) {
				gradeRecordMap.put(gradeRecord.getStudentId(), gradeRecord);
			}
		}

        // If the table is not being sorted by enrollment information, then
        // we had to gather grade records for all students to set up the
        // current page. In that case, eliminate the undisplayed grade records
        // to reduce data contention.
        if (!isEnrollmentSort()) {
        	gradeRecords = new ArrayList(gradeRecordMap.values());
        }

		for (final Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
			final String studentUid = (String)iter.next();
			final Map enrFunctionMap = (Map) enrollmentMap.get(studentUid);
			final List enrRecList = new ArrayList(enrFunctionMap.keySet());
			final EnrollmentRecord enrollment = (EnrollmentRecord)enrRecList.get(0); // there is only one rec in this map

			CourseGradeRecord gradeRecord = (CourseGradeRecord)gradeRecordMap.get(studentUid);
            if(gradeRecord == null) {
                gradeRecord = new CourseGradeRecord(this.courseGrade, studentUid);
                gradeRecords.add(gradeRecord);
            }

            boolean userCanGrade = false;
            final String itemFunction = (String)enrFunctionMap.get(enrollment);
            if (itemFunction != null && itemFunction.equalsIgnoreCase(GradebookService.gradePermission)) {
            	userCanGrade = true;
            	this.allStudentsViewOnly = false;
            }

			this.scoreRows.add(new ScoreRow(enrollment, gradeRecord, allEvents.getEvents(studentUid), userCanGrade));
		}
	}

    public CourseGrade getCourseGrade() {
        return this.courseGrade;
    }
    public String getAverageCourseGrade() {
		return this.gradeMapping.getMappedGrade(this.courseGrade.getMean());
    }
    public double getTotalPoints() {
        return this.totalPoints;
    }

    public boolean isAllStudentsViewOnly() {
    	return this.allStudentsViewOnly;
    }

    /**
	 * @return the value of gradebook.institutional.export.enabled sakai-property
	 */
	public boolean isEnableCustomExport() {
		return this.enableCustomExport;
	}

	/**
	 * Action listener to update grades.
	 * NOTE: No transient fields are available yet.
	 */
	public void processUpdateGrades(final ActionEvent event) {
		try {
			saveGrades();
		} catch (final StaleObjectModificationException e) {
            log.error(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
	}

	/**
	 * Action to calculate course grades
	 */
	public String processCalculateCourseGrades() {
		try {
			calculateCourseGrades();
		} catch (final StaleObjectModificationException e) {
			log.error(e.getMessage());
			FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
		return "courseGradeDetails";
	}

	private void saveGrades() throws StaleObjectModificationException {
		getGradebookManager().updateCourseGradeRecords(this.courseGrade, this.updatedGradeRecords);
        getGradebookBean().getEventTrackingService().postEvent("gradebook.updateCourseGrades","/gradebook/"+getGradebookId()+"/"+this.updatedGradeRecords.size()+"/"+getAuthzLevel());
        // Let the user know.
		FacesUtil.addMessage(getLocalizedString("course_grade_details_grades_saved"));
	}

	private void calculateCourseGrades() {
		getGradebookManager().fillInZeroForNullGradeRecords(getGradebook());
		FacesUtil.addMessage(getLocalizedString("calculate_course_grade_done"));
	}

	public void updateExportFieldStatus(String standardExportFields){

		if(standardExportFields != null && standardExportFields.length()>1){
			standardExportFields = standardExportFields.toLowerCase();
			if(standardExportFields.contains("usereid")){
				setIncludeUsereid(true);
			}
			if(standardExportFields.contains("sortname")){
				setIncludeSortname(true);
			}
			if(standardExportFields.contains("coursegrade")){
				setIncludeCoursegrade(true);
			}
			if(standardExportFields.contains("calculatedgrade")){
				setIncludeCalculatedgrade(true);
			}
			if(standardExportFields.contains("gradeoverride")){
				setIncludeGradeoverride(true);
			}
			if(standardExportFields.contains("finalscore")){
				setIncludeFinalscore(true);
			}
			if(standardExportFields.contains("lastmodifieddate")){
				setIncludeLastmodifieddate(true);
			}
		}
	}

	//Action to export
	public void export(final ActionEvent event){
		//FacesContext facesContext = FacesContext.getCurrentInstance();
        //HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        final List<String> fields = new ArrayList<String>();
        if(this.includeUsereid) {
			fields.add("usereid");
		}
        if(this.includeSortname) {
			fields.add("sortname");
		}
        if(this.includeFinalscore) {
			fields.add("finalscore");
		}
        if(this.includeCalculatedgrade) {
			fields.add("calculatedgrade");
		}
        if(this.includeLastmodifieddate) {
			fields.add("lastmodifieddate");
		}
        if(this.includeGradeoverride) {
			fields.add("gradeoverride");
		}
        if(this.includeCoursegrade) {
			fields.add("coursegrade");
		}

        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadCourseGrade","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        if(this.exportType.equalsIgnoreCase("CSV")){
        	 if(log.isInfoEnabled()) {
				log.info("exporting course grade as CSV for gradebook " + getGradebookUid());
			}
             SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("csv", fields),
             		getDownloadFileName(getLocalizedString("export_course_grade_prefix")),
             		new SpreadsheetDataFileWriterCsv());
        }
        else if(this.exportType.equalsIgnoreCase("Excel")){
        	if(log.isInfoEnabled()) {
				log.info("exporting course grade as Excel for gradebook " + getGradebookUid());
			}
            SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("excel", fields),
            		getDownloadFileName(getLocalizedString("export_course_grade_prefix")),
            		new SpreadsheetDataFileWriterXls());
        }
        else if(this.exportType.equalsIgnoreCase("PDF")){
        	if(log.isInfoEnabled()) {
				log.info("exporting course grade as PDF for gradebook " + getGradebookUid());
			}
            SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("pdf", fields),
            		getDownloadFileName(getLocalizedString("export_course_grade_prefix")),
            		new SpreadsheetDataFileWriterPdf());
        }
	}

	//Action to export institutional customized format of gradebook SAK-22204
	public void exportCustomCsv(final ActionEvent event){
        if(log.isInfoEnabled()) {
			log.info("exporting course grade as Institutional CSV for gradebook " + getGradebookUid());
		}
		getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadCourseGrade","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());

		final String defaultFields = "userEid,sortName,courseGrade";
		final String stringFields = ServerConfigurationService.getString("gradebook.institutional.export.fields",defaultFields);
		String[] fields = stringFields.replaceAll("\\s","").toLowerCase().split(",");
		if (fields.length == 0) {
			fields = defaultFields.split(",");
		}
		SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("customCsv", Arrays.asList(fields)),
        		getDownloadFileName(getLocalizedString("export_course_grade_prefix")),
        		new SpreadsheetDataFileWriterCsv());
	}

	//Export custom grade label
	public String getExportCustomLabel(){
		return ServerConfigurationService.getString("gradebook.institutional.export.label",getLocalizedString("course_grade_details_export_course_grades_institution"));
	}

	private List<List<Object>> getSpreadsheetData(final String type, final List<String> fields) {
		// Get the full list of filtered enrollments and scores (not just the current page's worth).
		final List<EnrollmentRecord> filteredEnrollments = new ArrayList(getWorkingEnrollmentsForCourseGrade().keySet());
		Collections.sort(filteredEnrollments, ENROLLMENT_NAME_COMPARATOR);
		final Set<String> studentUids = new HashSet<String>();
		for (final EnrollmentRecord enrollment : filteredEnrollments) {
			studentUids.add(enrollment.getUser().getUserUid());
		}

		final CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		final List<CourseGradeRecord> courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
		final Map<String, CourseGradeRecord> filteredGradesMap = new HashMap<String, CourseGradeRecord>();
		getGradebookManager().addToGradeRecordMap(filteredGradesMap, courseGradeRecords);
		CourseGradesToSpreadsheetConverter converter = null;
		//For institutional customized export format SAK-22204
		if (type.startsWith("custom")){
			converter = new CourseGradesToSpreadsheetCustomConverter();
		} else {
			converter = (CourseGradesToSpreadsheetConverter)getGradebookBean().getConfigurationBean().getPlugin(this.courseGradesConverterPlugin);
		}
		return converter.getSpreadsheetData(filteredEnrollments, courseGrade, filteredGradesMap, fields);
	}

	public List getScoreRows() {
		return this.scoreRows;
	}
	public void setScoreRows(final List scoreRows) {
		this.scoreRows = scoreRows;
	}

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("course_grade_details_log_type");
	}

	// Sorting
	@Override
	public boolean isSortAscending() {
		return getPreferencesBean().isCourseGradeDetailsTableSortAscending();
	}
	@Override
	public void setSortAscending(final boolean sortAscending) {
		getPreferencesBean().setCourseGradeDetailsTableSortAscending(sortAscending);
	}
	@Override
	public String getSortColumn() {
		return getPreferencesBean().getCourseGradeDetailsTableSortColumn();
	}
	@Override
	public void setSortColumn(final String sortColumn) {
		getPreferencesBean().setCourseGradeDetailsTableSortColumn(sortColumn);
	}

	public void setCourseGradesConverterPlugin(final String courseGradesConverterPlugin) {
		this.courseGradesConverterPlugin = courseGradesConverterPlugin;
	}
}
