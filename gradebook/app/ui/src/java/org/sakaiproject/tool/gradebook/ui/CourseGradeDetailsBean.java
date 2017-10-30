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

import java.math.BigDecimal;
import java.io.Serializable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterPdf;
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

public class CourseGradeDetailsBean extends EnrollmentTableBean {
	private static final Logger logger = LoggerFactory.getLogger(CourseGradeDetailsBean.class);

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
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public List<SelectItem> getExportFormats() {
		return exportFormats;
	}

	public void setExportFormats(List<SelectItem> exportFormats) {
		this.exportFormats = exportFormats;
	}
    
    public boolean isIncludeUsereid() {
		return includeUsereid;
	}

	public void setIncludeUsereid(boolean includeUsereid) {
		this.includeUsereid = includeUsereid;
	}

	public boolean isIncludeSortname() {
		return includeSortname;
	}

	public void setIncludeSortname(boolean includeSortname) {
		this.includeSortname = includeSortname;
	}

	public boolean isIncludeCoursegrade() {
		return includeCoursegrade;
	}

	public void setIncludeCoursegrade(boolean includeCoursegrade) {
		this.includeCoursegrade = includeCoursegrade;
	}

	public boolean isIncludeFinalscore() {
		return includeFinalscore;
	}

	public void setIncludeFinalscore(boolean includeFinalscore) {
		this.includeFinalscore = includeFinalscore;
	}

	public boolean isIncludeCalculatedgrade() {
		return includeCalculatedgrade;
	}

	public void setIncludeCalculatedgrade(boolean includeCalculatedgrade) {
		this.includeCalculatedgrade = includeCalculatedgrade;
	}

	public boolean isIncludeGradeoverride() {
		return includeGradeoverride;
	}

	public void setIncludeGradeoverride(boolean includeGradeoverride) {
		this.includeGradeoverride = includeGradeoverride;
	}

	public boolean isIncludeLastmodifieddate() {
		return includeLastmodifieddate;
	}

	public void setIncludeLastmodifieddate(boolean includeLastmodifieddate) {
		this.includeLastmodifieddate = includeLastmodifieddate;
	}
	
	

	public class ScoreRow implements Serializable {
        private EnrollmentRecord enrollment;
        private CourseGradeRecord courseGradeRecord;
        private List eventRows;
        private boolean userCanGrade;
    	

		public ScoreRow() {
		}
		public ScoreRow(EnrollmentRecord enrollment, CourseGradeRecord courseGradeRecord, List gradingEvents, boolean userCanGrade) {
            this.enrollment = enrollment;
			this.courseGradeRecord = courseGradeRecord;
			this.userCanGrade = userCanGrade;

            eventRows = new ArrayList();
            for (Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	GradingEvent gradingEvent = (GradingEvent)iter.next();
            	eventRows.add(new GradingEventRow(gradingEvent));
            }
		}

		/**
		 * 
		 * @return letter grade representation of grade or null if no course grade yet
		 */
        public String getCalculatedLetterGrade() {
        	Double grade = courseGradeRecord.getAutoCalculatedGrade();
        	String letterGrade = null;
        	if (grade != null)
        		letterGrade = gradeMapping.getGrade(courseGradeRecord.getNonNullAutoCalculatedGrade());
        	return letterGrade;
        }
        
        /**
         * Because the PrecisePercentageConverter is actually rounding at 2 decimals instead of
         * truncating, do the formatting here. 
         * @return percent representation of grade or null if no grade yet
         */
        public Double getCalculatedPercentGrade() {
        	Double grade = courseGradeRecord.getAutoCalculatedGrade();
        	if (grade != null) {
        		// to emulate the converter, truncate to 4 decimal places, then return 2
        		grade = new Double(FacesUtil.getRoundDown(grade.doubleValue(), 4));
        		BigDecimal bdGrade = (new BigDecimal(grade.toString())).setScale(2, BigDecimal.ROUND_DOWN);
        		grade = new Double(bdGrade.doubleValue());
        	}
        	
        	return grade;
        }

        public CourseGradeRecord getCourseGradeRecord() {
            return courseGradeRecord;
        }
        public void setCourseGradeRecord(CourseGradeRecord courseGradeRecord) {
            this.courseGradeRecord = courseGradeRecord;
        }

        public String getEnteredGrade() {
            return courseGradeRecord.getEnteredGrade();
        }
        public void setEnteredGrade(String enteredGrade) {
        	String originalEnteredGrade = courseGradeRecord.getEnteredGrade();
        	if (!StringUtils.equals(enteredGrade, originalEnteredGrade)) {
        		courseGradeRecord.setEnteredGrade(enteredGrade);
        		updatedGradeRecords.add(courseGradeRecord);
        	}
        }

        public EnrollmentRecord getEnrollment() {
            return enrollment;
        }

        public List getEventRows() {
        	return eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("course_grade_details_log_title", new String[] {enrollment.getUser().getDisplayName()});
        }
        public boolean isUserCanGrade() {
        	return userCanGrade;
        }
	}

	protected void init() {
		super.init();

		// Clear view state.
		scoreRows = new ArrayList();
		courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		updatedGradeRecords = new ArrayList();

        gradeMapping = getGradebook().getSelectedGradeMapping();
        totalPoints = getGradebookManager().getTotalPoints(getGradebookId());
        
		enableCustomExport = ServerConfigurationService.getBoolean("gradebook.institutional.export.enabled",false);
		
		//Default standard export fields
		standardExportDefaultFields = ServerConfigurationService.getString("gradebook.standard.export.default.fields","usereid,sortname,coursegrade");		
		updateExportFieldStatus(standardExportDefaultFields);
		if (exportFormats.isEmpty()){
			exportFormats.add(new SelectItem("CSV"));
			exportFormats.add(new SelectItem("Excel"));
			exportFormats.add(new SelectItem("PDF"));
		}
		exportType = "CSV";
		
		// Set up score rows.
        Map enrollmentMap = getOrderedEnrollmentMapForCourseGrades();  
		List studentUids = new ArrayList(enrollmentMap.keySet());
		List gradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecordsWithStats(courseGrade, studentUids);
		
		if (!isEnrollmentSort()) {
			// Need to sort and page based on a scores column.
			String sortColumn = getSortColumn();
			Comparator comparator = null;
			if (sortColumn.equals(CourseGrade.SORT_BY_CALCULATED_GRADE) ||
                sortColumn.equals(CourseGrade.SORT_BY_POINTS_EARNED)) {
                comparator = CourseGradeRecord.calcComparator;
            } else if (sortColumn.equals(CourseGrade.SORT_BY_OVERRIDE_GRADE)) {
            	comparator = CourseGradeRecord.getOverrideComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            if (comparator != null) {
	            Collections.sort(gradeRecords, comparator);
	        }

			List scoreSortedStudentUids = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
				scoreSortedStudentUids.add(cgr.getStudentId());
			}

			// Put enrollments with no scores at the beginning of the final list.
			studentUids.removeAll(scoreSortedStudentUids);

			// Add all sorted enrollments with scores into the final list
			studentUids.addAll(scoreSortedStudentUids);

			studentUids = finalizeSortingAndPaging(studentUids);
		}

		// Get all of the grading events for these enrollments on this assignment
		GradingEvents allEvents = getGradebookManager().getGradingEvents(courseGrade, studentUids);

		Map gradeRecordMap = new HashMap();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
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
			
		for (Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
			String studentUid = (String)iter.next();
			Map enrFunctionMap = (Map) enrollmentMap.get(studentUid);
			List enrRecList = new ArrayList(enrFunctionMap.keySet());
			EnrollmentRecord enrollment = (EnrollmentRecord)enrRecList.get(0); // there is only one rec in this map
			
			CourseGradeRecord gradeRecord = (CourseGradeRecord)gradeRecordMap.get(studentUid);
            if(gradeRecord == null) {
                gradeRecord = new CourseGradeRecord(courseGrade, studentUid);
                gradeRecords.add(gradeRecord);
            }
            
            boolean userCanGrade = false;
            String itemFunction = (String)enrFunctionMap.get(enrollment);
            if (itemFunction != null && itemFunction.equalsIgnoreCase(GradebookService.gradePermission)) {
            	userCanGrade = true;
            	allStudentsViewOnly = false;
            }
			
			scoreRows.add(new ScoreRow(enrollment, gradeRecord, allEvents.getEvents(studentUid), userCanGrade));
		}
	}

    public CourseGrade getCourseGrade() {
        return courseGrade;
    }
    public String getAverageCourseGrade() {
    	return gradeMapping.getGrade(courseGrade.getMean());
    }
    public double getTotalPoints() {
        return totalPoints;
    }
    
    public boolean isAllStudentsViewOnly() {
    	return allStudentsViewOnly;
    }
    
    /**
	 * @return the value of gradebook.institutional.export.enabled sakai-property
	 */
	public boolean isEnableCustomExport() {
		return enableCustomExport;
	}

	/**
	 * Action listener to update grades.
	 * NOTE: No transient fields are available yet.
	 */
	public void processUpdateGrades(ActionEvent event) {
		try {
			saveGrades();
		} catch (StaleObjectModificationException e) {
            logger.error(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
	}
	
	/**
	 * Action to calculate course grades
	 */
	public String processCalculateCourseGrades() {
		try {
			calculateCourseGrades();
		} catch (StaleObjectModificationException e) {
			logger.error(e.getMessage());
			FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
		return "courseGradeDetails";
	}

	private void saveGrades() throws StaleObjectModificationException {
		getGradebookManager().updateCourseGradeRecords(courseGrade, updatedGradeRecords);
        getGradebookBean().getEventTrackingService().postEvent("gradebook.updateCourseGrades","/gradebook/"+getGradebookId()+"/"+updatedGradeRecords.size()+"/"+getAuthzLevel());
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
	public void export(ActionEvent event){
		//FacesContext facesContext = FacesContext.getCurrentInstance();
        //HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        List<String> fields = new ArrayList<String>();
        if(includeUsereid) fields.add("usereid");
        if(includeSortname) fields.add("sortname");
        if(includeFinalscore) fields.add("finalscore");
        if(includeCalculatedgrade) fields.add("calculatedgrade");
        if(includeLastmodifieddate) fields.add("lastmodifieddate");
        if(includeGradeoverride) fields.add("gradeoverride");
        if(includeCoursegrade) fields.add("coursegrade");
        
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadCourseGrade","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        if(exportType.equalsIgnoreCase("CSV")){
        	 if(logger.isInfoEnabled()) logger.info("exporting course grade as CSV for gradebook " + getGradebookUid());
             SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("csv", fields), 
             		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
             		new SpreadsheetDataFileWriterCsv());
        }
        else if(exportType.equalsIgnoreCase("Excel")){
        	if(logger.isInfoEnabled()) logger.info("exporting course grade as Excel for gradebook " + getGradebookUid());
            SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("excel", fields), 
            		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
            		new SpreadsheetDataFileWriterXls());
        }
        else if(exportType.equalsIgnoreCase("PDF")){
        	if(logger.isInfoEnabled()) logger.info("exporting course grade as PDF for gradebook " + getGradebookUid());
            SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("pdf", fields), 
            		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
            		new SpreadsheetDataFileWriterPdf());
        }
	}
	
	//Action to export institutional customized format of gradebook SAK-22204 
    public void exportCustomCsv(ActionEvent event){
        if(logger.isInfoEnabled()) logger.info("exporting course grade as Institutional CSV for gradebook " + getGradebookUid());
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadCourseGrade","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        
        String defaultFields = "userEid,sortName,courseGrade";
		String stringFields = ServerConfigurationService.getString("gradebook.institutional.export.fields",defaultFields);
		String[] fields = stringFields.replaceAll("\\s","").toLowerCase().split(",");
		if (fields.length == 0) fields = defaultFields.split(",");        
        SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData("customCsv", Arrays.asList(fields)), 
        		getDownloadFileName(getLocalizedString("export_course_grade_prefix")), 
        		new SpreadsheetDataFileWriterCsv());
    }
	
    //Export custom grade label
    public String getExportCustomLabel(){
		return ServerConfigurationService.getString("gradebook.institutional.export.label",getLocalizedString("course_grade_details_export_course_grades_institution"));
    }
    
    private List<List<Object>> getSpreadsheetData(String type, List<String> fields) {
    	// Get the full list of filtered enrollments and scores (not just the current page's worth).
    	List<EnrollmentRecord> filteredEnrollments = new ArrayList(getWorkingEnrollmentsForCourseGrade().keySet());
    	Collections.sort(filteredEnrollments, ENROLLMENT_NAME_COMPARATOR);
    	Set<String> studentUids = new HashSet<String>();
    	for (EnrollmentRecord enrollment : filteredEnrollments) {
    		studentUids.add(enrollment.getUser().getUserUid());
    	}

		CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		List<CourseGradeRecord> courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids);
		Map<String, CourseGradeRecord> filteredGradesMap = new HashMap<String, CourseGradeRecord>();
		getGradebookManager().addToGradeRecordMap(filteredGradesMap, courseGradeRecords);
		CourseGradesToSpreadsheetConverter converter = null;
		//For institutional customized export format SAK-22204
		if (type.startsWith("custom")){
			converter = new CourseGradesToSpreadsheetCustomConverter();
		} else {
			converter = (CourseGradesToSpreadsheetConverter)getGradebookBean().getConfigurationBean().getPlugin(courseGradesConverterPlugin);
		}
		return converter.getSpreadsheetData(filteredEnrollments, courseGrade, filteredGradesMap, fields);
    }

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("course_grade_details_log_type");
	}

    // Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isCourseGradeDetailsTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setCourseGradeDetailsTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getCourseGradeDetailsTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setCourseGradeDetailsTableSortColumn(sortColumn);
    }

	public void setCourseGradesConverterPlugin(String courseGradesConverterPlugin) {
		this.courseGradesConverterPlugin = courseGradesConverterPlugin;
	}
}
