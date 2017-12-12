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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.text.NumberFormat;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.UIParameter;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import lombok.extern.slf4j.Slf4j;

import org.apache.myfaces.component.html.ext.HtmlDataTable;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterPdf;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.AssignmentPointsConverter;
import org.sakaiproject.tool.gradebook.jsf.CategoryPointsConverter;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.util.ResourceLoader;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
@Slf4j
public class RosterBean extends EnrollmentTableBean implements Serializable, Paging {
	// Used to generate IDs for the dynamically created assignment columns.
	private static final String ASSIGNMENT_COLUMN_PREFIX = "asg_";
	private static final String CATEGORY_COLUMN_PREFIX = "_categoryCol_";

	// View maintenance fields - serializable.
	private List gradableObjectColumns;	// Needed to build table columns
	private List workingEnrollments;
	private Map enrollmentMap;
    
	private CourseGrade avgCourseGrade;
    
	private HtmlDataTable originalRosterDataTable = null;

	private boolean selectedCategoryDropsScores;
    
	public class GradableObjectColumn implements Serializable {
		private Long id;
		private String name;
		private Boolean categoryColumn = false;
		private Boolean assignmentColumn = false;
		private Long assignmentId;
		private Boolean inactive = false;
		private Boolean hideInAllGradesTable = false;
	        private Boolean hiddenChanged = false;
		
		public GradableObjectColumn() {
		}
		public GradableObjectColumn(GradableObject gradableObject) {
			id = gradableObject.getId();
			name = getColumnHeader(gradableObject);
			categoryColumn = false;
			assignmentId = getColumnHeaderAssignmentId(gradableObject);
			assignmentColumn = !gradableObject.isCourseGrade();
			inactive = (!gradableObject.isCourseGrade() && !((GradebookAssignment)gradableObject).isReleased() ? true : false);
			hideInAllGradesTable = assignmentColumn ? ((GradebookAssignment) gradableObject).isHideInAllGradesTable() : false;
			hiddenChanged = hideInAllGradesTable;
		}

		@Override
		public String toString() {
		    return name+":("+id+"):"+assignmentId; // AZ - better debugging
		}

		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Boolean getCategoryColumn() {
			return categoryColumn;
		}
		public void setCategoryColumn(Boolean categoryColumn){
			this.categoryColumn = categoryColumn;
		}
		public Long getAssignmentId() {
			return assignmentId;
		}
		public void setAssignmentId(Long assignmentId) {
			this.assignmentId = assignmentId;
		}
		public Boolean getAssignmentColumn() {
			return assignmentColumn;
		}
		public void setAssignmentColumn(Boolean assignmentColumn) {
			this.assignmentColumn = assignmentColumn;
		}
		public Boolean getInactive() {
			return this.inactive;
		}
		public void setInactive(Boolean inactive) {
			this.inactive = inactive;
		}
		public Boolean getHideInAllGradesTable() {
			return hideInAllGradesTable;
		}
		public void setHideInAllGradesTable(Boolean hideInAllGradesTable) {
			this.hideInAllGradesTable = hideInAllGradesTable;
		}
		
		public boolean hasHiddenChanged(){
			return hiddenChanged != hideInAllGradesTable;
		}
	}

	// Controller fields - transient.
	private transient List studentRows;
	private transient Map gradeRecordMap;
	private transient Map categoryResultMap;
	
	public class StudentRow implements Serializable {
        private EnrollmentRecord enrollment;

		public StudentRow() {
		}
		public StudentRow(EnrollmentRecord enrollment) {
            this.enrollment = enrollment;
		}

		public String getStudentUid() {
			return enrollment.getUser().getUserUid();
		}
		public String getSortName() {
			return enrollment.getUser().getSortName();
		}
		public String getDisplayId() {
			return enrollment.getUser().getDisplayId();
		}

		public Map getScores() {
			return (Map)gradeRecordMap.get(enrollment.getUser().getUserUid());
		}
		
		public Map getCategoryResults() {
			return (Map)categoryResultMap.get(enrollment.getUser().getUserUid());
		}
	}

	protected void init() {
		// set the roster filter
		super.setSelectedSectionFilterValue(this.getSelectedSectionFilterValue());
		super.init();
		//get array to hold columns
		gradableObjectColumns = new ArrayList();
		
		avgCourseGrade = new CourseGrade();
		
		//get the selected categoryUID 
		String selectedCategoryUid = getSelectedCategoryUid();
		
		if(selectedCategoryUid != null) {
		    Category selectedCategory = getSelectedCategory();
            selectedCategoryDropsScores = selectedCategory.isDropScores();
		}
		
		CourseGrade courseGrade = null;
		if (isUserAbleToGradeAll()) {
			courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
			// first add Cumulative if not a selected category
			if(selectedCategoryUid == null){
				gradableObjectColumns.add(new GradableObjectColumn(courseGrade));
			}	
		}
		
		// make sure the numeric display is locale-aware
		NumberFormat nf = NumberFormat.getInstance(new ResourceLoader().getLocale());
		
		List<Category> categories = new ArrayList<Category>();
        List<GradebookAssignment> allAssignments = new ArrayList<GradebookAssignment>();

		// get all of the assignments and categories
		List assignCategoryCGList = getGradebookManager().getAssignmentsCategoriesAndCourseGradeWithStats(getGradebookId(),
                GradebookAssignment.DEFAULT_SORT, true, Category.SORT_BY_NAME, true);

		// let's filter these into assignment list, category list, and course grade
		for (Iterator listIter = assignCategoryCGList.iterator(); listIter.hasNext();) {
			Object assignCatOrCourseGrade = listIter.next();
			if (assignCatOrCourseGrade instanceof Category) {
				categories.add((Category)assignCatOrCourseGrade);
			} else if (assignCatOrCourseGrade instanceof CourseGrade) {
				avgCourseGrade = (CourseGrade)assignCatOrCourseGrade;
			} else if (assignCatOrCourseGrade instanceof GradebookAssignment) {
			    allAssignments.add((GradebookAssignment)assignCatOrCourseGrade);
			}
		}
		
		if (getCategoriesEnabled()) {
			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
				//SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
				List<Long> catIds = new ArrayList<Long>();
				for (Category category : categories) {
					catIds.add(category.getId());
				}
				List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), catIds);
				List<Category> tmpCatList = new ArrayList<Category>();
				for (Category category : categories) {
					if(viewableCats.contains(category.getId())){
						tmpCatList.add(category);
					}
				}
				categories = tmpCatList;
			}

			int categoryCount = categories.size();
			
			for (Iterator iter = categories.iterator(); iter.hasNext(); ){
				Category cat = (Category) iter.next();
	
				if(selectedCategoryUid == null || selectedCategoryUid.equals(cat.getId().toString())){
				
					//get the category column
					GradableObjectColumn categoryColumn = new GradableObjectColumn();
					String name = cat.getName();
					if(getWeightingEnabled()){
						//if weighting is enabled, then add "(weight)" to column
						Double value = (Double) ((Number)cat.getWeight());
						name = name + " (" +  nf.format(value * 100.0) + "%)";
						//name = name + " (" + Integer.toString(cat.getWeight() * 100) + "%)";
					}
					categoryColumn.setName(name);
					categoryColumn.setId(cat.getId());
					categoryColumn.setCategoryColumn(true);
					
					//if selectedCategoryUID, then we want the category first, otherwise after
					if(selectedCategoryUid != null) {
						gradableObjectColumns.add(categoryColumn);
					}
					
					//add assignments
					//List assignments = getGradebookManager().getAssignmentsForCategory(cat.getId());
					List assignments = cat.getAssignmentList();
					if (assignments != null && !assignments.isEmpty()){
						for (Iterator assignmentsIter = assignments.iterator(); assignmentsIter.hasNext();){
							gradableObjectColumns.add(new GradableObjectColumn((GradableObject)assignmentsIter.next()));
						}
						//if not selectedCategoryUID, then add category field after
						if(selectedCategoryUid == null) {
							gradableObjectColumns.add(categoryColumn);
						}
					}
				}
			}
			if(selectedCategoryUid == null){
				if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions() && !getGradebookPermissionService().getPermissionForUserForAllAssignment(getGradebookId(), getUserUid()))) {
					// not allowed to view the unassigned category
				} else {
					//get Assignments with no category
					List unassignedAssignments = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(), GradebookAssignment.DEFAULT_SORT, true);
					int unassignedAssignmentCount = unassignedAssignments.size();
					for (Iterator assignmentsIter = unassignedAssignments.iterator(); assignmentsIter.hasNext(); ){
						gradableObjectColumns.add(new GradableObjectColumn((GradableObject) assignmentsIter.next()));
					}
					//If there are categories and there are unassigned assignments, then display Unassigned Category column
					if (getCategoriesEnabled() && unassignedAssignmentCount > 0){
						//add Unassigned column
						GradableObjectColumn unassignedCategoryColumn = new GradableObjectColumn();
						unassignedCategoryColumn.setName(FacesUtil.getLocalizedString("cat_unassigned"));
						unassignedCategoryColumn.setCategoryColumn(true);
						gradableObjectColumns.add(unassignedCategoryColumn);
					}
				}
			}
		}
		
		if (isRefreshRoster()) {
			enrollmentMap = getOrderedEnrollmentMapForAllItems(); // Map of EnrollmentRecord --> Map of Item --> function (grade/view)
			setRefreshRoster(false);
		}
		Map studentIdEnrRecMap = new HashMap();
        Map studentIdItemIdFunctionMap = new HashMap();
        
        // get all of the items included in the item --> function map for each viewable enrollee
		List viewableAssignmentIds = new ArrayList();
        for (Iterator enrIter = enrollmentMap.keySet().iterator(); enrIter.hasNext();) {
        	EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
        	if (enr != null) {
        		String studentId = enr.getUser().getUserUid();
        		studentIdEnrRecMap.put(studentId, enr);
        		
        		Map itemFunctionMap = (Map)enrollmentMap.get(enr);
        		
				studentIdItemIdFunctionMap.put(studentId, itemFunctionMap);
				if (itemFunctionMap != null) {
	        		for (Iterator itemIter = itemFunctionMap.keySet().iterator(); itemIter.hasNext();) {
	        			Long itemId = (Long) itemIter.next();
	        			if (itemId != null) {
	        				viewableAssignmentIds.add(itemId);
	        			}
	        		}
				}
        	}
        }
        
        List viewableAssignmentList = new ArrayList();
        //Map viewableAssignmentMap = new HashMap();
        if (!allAssignments.isEmpty()) {
        	for (Iterator assignIter = allAssignments.iterator(); assignIter.hasNext();) {
        		Object obj = assignIter.next();
        		if (obj instanceof GradebookAssignment){
	        		GradebookAssignment assignment = (GradebookAssignment) obj;
	        		if (assignment != null) {
	        			Long assignId = assignment.getId();
	        			if (viewableAssignmentIds.contains(assignId)) {
	        				viewableAssignmentList.add(assignment);
	        				//viewableAssignmentMap.put(assignId, assignment);
	        			}
	        		}
        		}
        	}
        }
        
        List assignments = viewableAssignmentList;
		List gradeRecords = getGradebookManager().getAllAssignmentGradeRecordsConverted(getGradebookId(), new ArrayList(studentIdEnrRecMap.keySet()));
        
		if (!getCategoriesEnabled()) {
			int unassignedAssignmentCount = assignments.size();
			for (Iterator assignmentsIter = assignments.iterator(); assignmentsIter.hasNext(); ){
				gradableObjectColumns.add(new GradableObjectColumn((GradableObject) assignmentsIter.next()));
			}
		}
		
		workingEnrollments = new ArrayList(enrollmentMap.keySet());

        gradeRecordMap = new HashMap();
        if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
        	// first, add dummy grade records for all of the missing "unviewable" grade records
        	// this will allow us to display grade entries that are not viewable differently
        	// than null grade records
        	for (Iterator studentIter = studentIdItemIdFunctionMap.keySet().iterator(); studentIter.hasNext();) {
        		String studentId = (String)studentIter.next();
        		if (studentId != null) {
        			Map itemIdFunctionMap = (Map)studentIdItemIdFunctionMap.get(studentId);
        			for (Iterator itemIter = allAssignments.iterator(); itemIter.hasNext();) {
        				Object obj = itemIter.next();
        				if (obj instanceof GradebookAssignment){
        					GradebookAssignment assignment = (GradebookAssignment) obj;
        					if (assignment != null) {
        						Long itemId = assignment.getId();
        						if (itemIdFunctionMap == null || itemIdFunctionMap.get(itemId) == null){
        							AssignmentGradeRecord agr = new AssignmentGradeRecord(assignment, studentId, null);
        							gradeRecords.add(agr);
        						}
        					}
        				}
        			}
        		}
        	}
        	
        	getGradebookManager().addToGradeRecordMap(gradeRecordMap, gradeRecords, studentIdItemIdFunctionMap);
        	
        	
        } else {
        	getGradebookManager().addToGradeRecordMap(gradeRecordMap, gradeRecords);
        }
		if (log.isDebugEnabled()) log.debug("init - gradeRecordMap.keySet().size() = " + gradeRecordMap.keySet().size());
		
		if (!isEnrollmentSort() && !isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
			// we need to re-sort these records b/c some may actually be null based upon permissions.
			// retrieve updated grade recs from gradeRecordMap
			List updatedGradeRecs = new ArrayList();
			for (Iterator iter = gradeRecordMap.keySet().iterator(); iter.hasNext();) {
				String studentId = (String)iter.next();
				Map itemIdGradeRecMap = (Map)gradeRecordMap.get(studentId);
				if (!itemIdGradeRecMap.isEmpty()) {
					updatedGradeRecs.addAll(itemIdGradeRecMap.values());
				}
			}
			Collections.sort(updatedGradeRecs, AssignmentGradeRecord.calcComparator);
			gradeRecords = updatedGradeRecs;
		}
		
		// only display course grade if user has "grade all" perm
		if (isUserAbleToGradeAll()) {
			List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentIdEnrRecMap.keySet(), assignments, gradeRecordMap);
			Collections.sort(courseGradeRecords, CourseGradeRecord.calcComparator);
	        getGradebookManager().addToGradeRecordMap(gradeRecordMap, courseGradeRecords);
	        gradeRecords.addAll(courseGradeRecords);
		}
        
        //do category results
        categoryResultMap = new HashMap();
        getGradebookManager().addToCategoryResultMap(categoryResultMap, categories, gradeRecordMap, studentIdEnrRecMap);
        if (log.isDebugEnabled()) log.debug("init - categoryResultMap.keySet().size() = " + categoryResultMap.keySet().size());

        if (!isEnrollmentSort()) {
        	// Need to sort and page based on a scores column.
        	String sortColumn = getSortColumn();
        	List scoreSortedEnrollments = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
				if(getColumnHeader(agr.getGradableObject()).equals(sortColumn)) {
					scoreSortedEnrollments.add(studentIdEnrRecMap.get(agr.getStudentId()));
				}
			}

			//In order to order by category score, first create 2 lists: students who have no score (null)
			//and a map of student Id's and their category score.
			//Next, sort the existing score then and put the sorted students in the scoreSortedEnrollments list 
			if(sortColumn.startsWith(CATEGORY_COLUMN_PREFIX) && sortColumn.length() > CATEGORY_COLUMN_PREFIX.length()){
				Map<String, Double> studentCatScore = new HashMap<String, Double>();
				String sortColumnIdStr = sortColumn.substring(CATEGORY_COLUMN_PREFIX.length());
				Long sortColumnId = null;
				try{
					sortColumnId = Long.parseLong(sortColumnIdStr);
				}catch (NumberFormatException e) {
				}
				
				List emptyCatList = new ArrayList();
				if(sortColumnId != null){
					for(Iterator iterator = categoryResultMap.entrySet().iterator(); iterator.hasNext();){
						Entry entry = (Entry) iterator.next();
						Map catMap = (Map) entry.getValue();
						String studentId = (String) entry.getKey();
						if(catMap.containsKey(sortColumnId)){
							Map sortCat = (Map) catMap.get(sortColumnId);
							//break up the students into two categories: scores and no score
							if(sortCat.containsKey("studentMean") && sortCat.get("studentMean") != null){
								studentCatScore.put(studentId, (Double) sortCat.get("studentMean"));
							}else{
								emptyCatList.add(studentIdEnrRecMap.get(studentId));
							}
						}
					}
				}
				
				//sort category scores:
				List studentCatEntrySet = new LinkedList(studentCatScore.entrySet());
				Collections.sort(studentCatEntrySet, new Comparator() {
			          public int compare(Object o1, Object o2) {
			               return ((Comparable) ((Map.Entry) (o1)).getValue())
			              .compareTo(((Map.Entry) (o2)).getValue());
			          }
			     });
				
				
				//add it to the scoreSortedEnrollments list now that it has been ordered
				for (Iterator it = studentCatEntrySet.iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry)it.next();
					scoreSortedEnrollments.add(studentIdEnrRecMap.get(entry.getKey()));
				}

				// remove and re-add the empty score users
	            workingEnrollments.removeAll(emptyCatList);

	            // by adding it back in, they will be in a group together (in order)
	            workingEnrollments.addAll(emptyCatList);
				
			}

            // Put enrollments with no scores at the beginning of the final list.
            workingEnrollments.removeAll(scoreSortedEnrollments);

            // Add all sorted enrollments with scores into the final list
            workingEnrollments.addAll(scoreSortedEnrollments);

            workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
		}

		studentRows = new ArrayList(workingEnrollments.size());
        for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
            EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
            studentRows.add(new StudentRow(enrollment));
        }

        // set breadcrumb page for navigation
//		SessionManager.getCurrentToolSession().setAttribute("breadcrumbPage", "roster");
		
	}
	
	private String getColumnHeader(GradableObject gradableObject) {
		if (gradableObject.isCourseGrade()) {
			return getLocalizedString("roster_course_grade_column_name");
		} else {
			return ((GradebookAssignment)gradableObject).getName();
		}
	}
	
	private Long getColumnHeaderAssignmentId(GradableObject gradableObject) {
		if (gradableObject.isCourseGrade()) {
			return new Long(-1);
		} else {
			return ((GradebookAssignment)gradableObject).getId();
		}
	}

	// The roster table uses assignments as columns, and therefore the component
	// model needs to have those columns added dynamically, based on the current
	// state of the gradebook.
	// In JSF 1.1, dynamic data table columns are managed by binding the component
	// tag to a bean property.

	// It's not exactly intuitive, but the convention is for the bean to return
	// null, so that JSF can create and manage the UIData component itself.
	public HtmlDataTable getRosterDataTable() {
		if (log.isDebugEnabled()) log.debug("getRosterDataTable");
		return null;
	}

	public void setRosterDataTable(HtmlDataTable rosterDataTable) {
		if (log.isDebugEnabled()) {
			log.debug("setRosterDataTable gradableObjectColumns=" + gradableObjectColumns + ", rosterDataTable=" + rosterDataTable);
			if (rosterDataTable != null) {
				log.debug("  data children=" + rosterDataTable.getChildren());
			}
		}
		if (rosterDataTable == null)
			throw new IllegalArgumentException(
					"HtmlDataTable rosterDataTable == null!");
		
		//check if columns of changed due to categories
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		Map paramMap = context.getRequestParameterMap();
		String catId = (String) paramMap.get("gbForm:selectCategoryFilter");
		//due to this set method getting called before all others, including the setSelectCategoryFilterValue, 
		// we have to manually set the value, then call init to get the new gradableObjectColumns array
		if(catId != null && !catId.equals(getSelectedCategoryUid())) {
			this.setSelectedCategoryFilterValue(new Integer(catId));
			init();
			//now destroy all of the columns to be readded
			rosterDataTable.getChildren().removeAll( rosterDataTable.getChildren().subList(2, rosterDataTable.getChildren().size()));
		}
    

        // Set the columnClasses on the data table
        StringBuilder colClasses = new StringBuilder("left,left,");
        for(Iterator iter = gradableObjectColumns.iterator(); iter.hasNext();) {
        	iter.next();
            colClasses.append("center");
            if(iter.hasNext()) {
                colClasses.append(",");
            }
        }
        rosterDataTable.setColumnClasses(colClasses.toString());

		if (rosterDataTable.findComponent(ASSIGNMENT_COLUMN_PREFIX + "0") == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			// Add columns for each assignment. Be sure to create unique IDs
			// for all child components.
			int colpos = 0;
			for (Iterator iter = gradableObjectColumns.iterator(); iter.hasNext(); colpos++) {
				GradableObjectColumn columnData = (GradableObjectColumn)iter.next();

				UIColumn col = new UIColumn();
				col.setId(ASSIGNMENT_COLUMN_PREFIX + colpos);
				if(columnData.getHideInAllGradesTable()){
					col.setRendered(false);
				}

				if(!columnData.getCategoryColumn()){
	                HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
	                sortHeader.setId(ASSIGNMENT_COLUMN_PREFIX + "sorthdr_" + colpos);
	                sortHeader.setRendererType("org.apache.myfaces.SortHeader");	// Yes, this is necessary.
	                sortHeader.setArrow(true);
	                sortHeader.setColumnName(columnData.getName());
	                sortHeader.setActionListener(app.createMethodBinding("#{rosterBean.sort}", new Class[] {ActionEvent.class}));
	                // Allow word-wrapping on assignment name columns.
	                if(columnData.getInactive()){
	                	sortHeader.setStyleClass("inactive-column allowWrap");
	                } else {
	                	sortHeader.setStyleClass("allowWrap");
	                }
	
					HtmlOutputText headerText = new HtmlOutputText();
					headerText.setId(ASSIGNMENT_COLUMN_PREFIX + "hdr_" + colpos);
					// Try straight setValue rather than setValueBinding.
					headerText.setValue(columnData.getName());
	
	                sortHeader.getChildren().add(headerText);
	                
	                if(columnData.getAssignmentColumn()){		                
		                //get details link
		                HtmlCommandLink detailsLink = new HtmlCommandLink();
		                detailsLink.setAction(app.createMethodBinding("#{rosterBean.navigateToAssignmentDetails}", new Class[] {}));
		                detailsLink.setId(ASSIGNMENT_COLUMN_PREFIX + "hdr_link_" + colpos);
		                HtmlOutputText detailsText = new HtmlOutputText();
		                detailsText.setId(ASSIGNMENT_COLUMN_PREFIX + "hdr_details_" + colpos);
		                detailsText.setValue("<em>" + getLocalizedString("roster_details") + "</em>");
		                detailsText.setEscape(false);
		                detailsText.setStyle("font-size: 80%");
		                detailsLink.getChildren().add(detailsText);
		                
		                UIParameter param = new UIParameter();
		                param.setName("assignmentId");
		                param.setValue(columnData.getAssignmentId());
		                detailsLink.getChildren().add(param);
		                
/*		                UIParameter param2 = new UIParameter();
		                param2.setName("breadcrumbPage");
		                param2.setValue("roster");
		                detailsLink.getChildren().add(param2);
*/		                
		                HtmlOutputText br = new HtmlOutputText();
		                br.setValue("<br />");
		                br.setEscape(false);
		                
		                //make a panel group to add link 
		                HtmlPanelGroup pg = new HtmlPanelGroup();
		                pg.getChildren().add(sortHeader);
		                pg.getChildren().add(br);
		                pg.getChildren().add(detailsLink);
		                
		                col.setHeader(pg);
	                } else {
	                	col.setHeader(sortHeader);	
	                }
				} else {
					//if we are dealing with a category
					String categoryId = CATEGORY_COLUMN_PREFIX;
					if(columnData.getId() != null){
						categoryId += columnData.getId().toString();

						HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
						sortHeader.setId(ASSIGNMENT_COLUMN_PREFIX + "sorthdr_" + colpos);
						sortHeader.setRendererType("org.apache.myfaces.SortHeader");	// Yes, this is necessary.
						sortHeader.setArrow(true);
						sortHeader.setColumnName(categoryId);
						sortHeader.setActionListener(app.createMethodBinding("#{rosterBean.sort}", new Class[] {ActionEvent.class}));
						// Allow word-wrapping on assignment name columns.
						if(columnData.getInactive()){
							sortHeader.setStyleClass("inactive-column allowWrap");
						} else {
							sortHeader.setStyleClass("allowWrap");
						}

						HtmlOutputText headerText = new HtmlOutputText();
						headerText.setId(ASSIGNMENT_COLUMN_PREFIX + "hdr_" + colpos);
						// Try straight setValue rather than setValueBinding.
						headerText.setValue(columnData.getName());

						sortHeader.getChildren().add(headerText);
						col.setHeader(sortHeader);
					}else{
						//Unassigned Category
						HtmlOutputText headerText = new HtmlOutputText();
						headerText.setId(ASSIGNMENT_COLUMN_PREFIX + "hrd_" + colpos);
						headerText.setValue(columnData.getName());

						col.setHeader(headerText);
					}
				}

				HtmlOutputText contents = new HtmlOutputText();
				contents.setEscape(false);
				contents.setId(ASSIGNMENT_COLUMN_PREFIX + "cell_" + colpos);
				if(!columnData.getCategoryColumn()){
					contents.setValueBinding("value",
							app.createValueBinding("#{row.scores[rosterBean.gradableObjectColumns[" + colpos + "].id]}"));
					contents.setConverter(new AssignmentPointsConverter());
				} else {
					contents.setValueBinding("value",
							app.createValueBinding("#{row.categoryResults[rosterBean.gradableObjectColumns[" + colpos + "].id]}"));
					contents.setConverter(new CategoryPointsConverter());
				}
                

                // Distinguish the "Cumulative" score for the course, which, by convention,
                // is always the first column. Only viewable if user has Grade All perm
                if (colpos == 0 && (isUserAbleToGradeAll() || getSelectedCategoryUid() != null)) {
                	contents.setStyleClass("courseGrade center");
                }

				col.getChildren().add(contents);

				rosterDataTable.getChildren().add(col);
			}
		}
	}

	public List getGradableObjectColumns() {
		return gradableObjectColumns;
	}
	public void setGradableObjectColumns(List gradableObjectColumns) {
		this.gradableObjectColumns = gradableObjectColumns;
	}

	public List getStudentRows() {
		return studentRows;
	}
	
	public String getColLock() {
		if (isUserAbleToGradeAll() || getSelectedCategoryUid() != null)
			return "3";
		else
			return "2";
	}

	// Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isRosterTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setRosterTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getRosterTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setRosterTableSortColumn(sortColumn);
    }
    
    public Category getSelectedCategory() {
    	String selectedUid = getSelectedCategoryUid();
    	Category selectedCat = null;
    	
    	//if selectedUid is not null (not All Categories) then proceed
    	if (selectedUid != null){
    		//get a list of all the categories with the stats
	    	List categories = getGradebookManager().getCategoriesWithStats(getGradebookId(),
                    GradebookAssignment.DEFAULT_SORT, true, Category.SORT_BY_NAME, true);
	    	for (Iterator iter = categories.iterator(); iter.hasNext(); ){
	    		Object obj = iter.next();
	    		//last item of list is the CourseGrade, so ignore
	    		if (!(obj instanceof Category)) { continue; }
	    		Category cat = (Category) obj;
	    		if (cat.getId() == Long.parseLong(selectedUid)){
	    			selectedCat = cat;
	    		}
	    	
	    	}
	    }
       	return selectedCat;
    }
    
    // Filtering
    public Integer getSelectedSectionFilterValue() {
        return getPreferencesBean().getRosterTableSectionFilter();
    }
    public void setSelectedSectionFilterValue(Integer rosterTableSectionFilter) {
        getPreferencesBean().setRosterTableSectionFilter(rosterTableSectionFilter);
        super.setSelectedSectionFilterValue(rosterTableSectionFilter);
    }
    
    public CourseGrade getAvgCourseGrade() {
		return avgCourseGrade;
	}
	public void setAvgCourseGrade(CourseGrade courseGrade) {
		this.avgCourseGrade = courseGrade;
	}
    
    public String getAvgCourseGradeLetter() {
		String letterGrade = "";
		if (avgCourseGrade != null) {
			letterGrade = getGradebook().getSelectedGradeMapping().getGrade(avgCourseGrade.getMean());
		}
		
		return letterGrade;
	}
    
    public void setSelectedCategoryDropsScores(boolean selectedCategoryDropsScores) {
        this.selectedCategoryDropsScores = selectedCategoryDropsScores;
    }

    public boolean isSelectedCategoryDropsScores() {
        return selectedCategoryDropsScores;
    }

    public void exportXlsNoCourseGrade(ActionEvent event){
        if(log.isInfoEnabled()) log.info("exporting gradebook " + getGradebookUid() + " as Excel");
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadRoster","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(false, false), 
        		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
        		new SpreadsheetDataFileWriterXls());
    }

    public void exportCsvNoCourseGrade(ActionEvent event){
        if(log.isInfoEnabled()) log.info("exporting gradebook " + getGradebookUid() + " as CSV");
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadRoster","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(false, true), 
        		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
        		new SpreadsheetDataFileWriterCsv());
    }

    public void exportCsv(ActionEvent event){
        if(log.isInfoEnabled()) log.info("exporting roster as CSV for gradebook " + getGradebookUid());
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadRoster","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        if (isUserAbleToGradeAll()) {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(true, true), 
        		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
        		new SpreadsheetDataFileWriterCsv());
        } else {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(false, true), 
            		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
            		new SpreadsheetDataFileWriterCsv());
        }
    }

    public void exportExcel(ActionEvent event){
        if(log.isInfoEnabled()) log.info("exporting roster as Excel for gradebook " + getGradebookUid());
        String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadRoster","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        if (isUserAbleToGradeAll()) {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(true, false), 
        		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
        		new SpreadsheetDataFileWriterXls());
        } else {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(false, false), 
            		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
            		new SpreadsheetDataFileWriterXls());
        }
    }
    
    public void exportPdf(ActionEvent event){
        if(log.isInfoEnabled()) log.info("exporting roster as Pdf for gradebook " + getGradebookUid());
        String authzLevel = (getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid())) ?"instructor" : "TA";
        getGradebookBean().getEventTrackingService().postEvent("gradebook.downloadRoster","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
        if (isUserAbleToGradeAll()) {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(true, true), 
        		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
        		new SpreadsheetDataFileWriterPdf());
        } else {
        	SpreadsheetUtil.downloadSpreadsheetData(getSpreadsheetData(false, true), 
            		getDownloadFileName(getLocalizedString("export_gradebook_prefix")), 
            		new SpreadsheetDataFileWriterPdf());
        }
    }
    
    private List<List<Object>> getSpreadsheetData(boolean includeCourseGrade, boolean localizeScores) {
    	// Get the full list of filtered enrollments and scores (not just the current page's worth).
    	Map enrRecItemIdFunctionMap = getWorkingEnrollmentsForAllItems();
    	List filteredEnrollments = new ArrayList(enrRecItemIdFunctionMap.keySet());  
    	Collections.sort(filteredEnrollments, ENROLLMENT_NAME_COMPARATOR);
    	Set<String> studentUids = new HashSet<String>();
    	
    	Map studentIdItemIdFunctionMap = new HashMap();
    	List availableItems = new ArrayList();
    	for (Iterator iter = filteredEnrollments.iterator(); iter.hasNext(); ) {
    		EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
    		String studentUid = enrollment.getUser().getUserUid();
    		studentUids.add(studentUid);
    		
    		Map itemIdFunctionMap = (Map)enrRecItemIdFunctionMap.get(enrollment);
    		studentIdItemIdFunctionMap.put(studentUid, itemIdFunctionMap);
    		// get the actual items to determine the gradable objects
    		if (!itemIdFunctionMap.isEmpty()) {
    			availableItems.addAll(itemIdFunctionMap.keySet());
    		}
    	}

		Map filteredGradesMap = new HashMap();
		List gradeRecords = getGradebookManager().getAllAssignmentGradeRecordsConverted(getGradebookId(), studentUids);
		
		if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
			getGradebookManager().addToGradeRecordMap(filteredGradesMap, gradeRecords, studentIdItemIdFunctionMap);
		} else {
			getGradebookManager().addToGradeRecordMap(filteredGradesMap, gradeRecords);
		}
		
		Category selCategoryView = getSelectedCategory();
        
		List gradableObjects = new ArrayList();
		List allAssignments = new ArrayList(); 
		List categoriesFilter = new ArrayList();
		if (getCategoriesEnabled()) {
			List categoryList = getGradebookManager().getCategoriesWithStats(getGradebookId(), getPreferencesBean().getAssignmentSortColumn(), 
									getPreferencesBean().isAssignmentSortAscending(), getPreferencesBean().getCategorySortColumn(), getPreferencesBean().isCategorySortAscending());
			
			// filter out the CourseGrade from the Category list to prevent errors
			for (Iterator catIter = categoryList.iterator(); catIter.hasNext();) {
				Object catOrCourseGrade = catIter.next();
				if (catOrCourseGrade instanceof Category) {
					categoriesFilter.add((Category)catOrCourseGrade);
				}
			}

			// then, we need to check for special grader permissions that may limit which categories may be viewed
			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
				//SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
				List<Long> catIds = new ArrayList<Long>();
				for (Category category : (List<Category>) categoriesFilter) {
					catIds.add(category.getId());
				}
				List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), catIds);
				List<Category> tmpCatList = new ArrayList<Category>();
				for (Category category : (List<Category>) categoriesFilter) {
					if(viewableCats.contains(category.getId())){
						tmpCatList.add(category);
					}
				}
				categoryList = tmpCatList;
			}

			if (categoryList != null && !categoryList.isEmpty()) {
				Iterator catIter = categoryList.iterator();
				while (catIter.hasNext()) {
					Object myCat = catIter.next();

					if (myCat instanceof Category) {
						List assignmentList = ((Category)myCat).getAssignmentList();
						if (assignmentList != null && !assignmentList.isEmpty()) {
							Iterator assignIter = assignmentList.iterator();
							while (assignIter.hasNext()) {
								GradebookAssignment assign = (GradebookAssignment) assignIter.next();
								allAssignments.add(assign);
							}
						}
					}
				}
			}

			if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions() && !getGradebookPermissionService().getPermissionForUserForAllAssignment(getGradebookId(), getUserUid()))) {
				// is not authorized to view the "Unassigned" Category
			} else {
				List unassignedList = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(), getPreferencesBean().getAssignmentSortColumn(), getPreferencesBean().isAssignmentSortAscending());
				if (unassignedList != null && !unassignedList.isEmpty()) {	
					Iterator unassignedIter = unassignedList.iterator();
					while (unassignedIter.hasNext()) {
						GradebookAssignment assignWithNoCat = (GradebookAssignment) unassignedIter.next();
						allAssignments.add(assignWithNoCat);
					}
				}
			}
		}
		else {
			allAssignments = getGradebookManager().getAssignments(getGradebookId());
		}
		
		if (!allAssignments.isEmpty()) {
			for (Iterator assignIter = allAssignments.iterator(); assignIter.hasNext();) {
				GradebookAssignment assign = (GradebookAssignment) assignIter.next();
				if (availableItems.contains(assign.getId()) && (selCategoryView == null || (assign.getCategory() != null && (assign.getCategory()).getId().equals(selCategoryView.getId())))) {
					gradableObjects.add(assign);
				}
			}
		}
		
		// don't include the course grade column if the user doesn't have grade all perm
		// or if the view is filtered by category
		if (!isUserAbleToGradeAll() || selCategoryView != null) {
			includeCourseGrade = false;
		}
		
		if (includeCourseGrade) {
			CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
			List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentUids, gradableObjects, filteredGradesMap);
	        getGradebookManager().addToGradeRecordMap(filteredGradesMap, courseGradeRecords);
	        gradableObjects.add(courseGrade);
		}
    	return getSpreadsheetData(filteredEnrollments, filteredGradesMap, gradableObjects, includeCourseGrade, localizeScores);
    }
 
    /**
     * Creates the actual 'spreadsheet' List needed from gradebook objects
     * Modified to export without Course Grade column if desired
     * Format:
     * 	Header Row: Student id, Student Name, GradebookAssignment(s) (with [points possible] after title)
     *  Student Rows
     * 
     * @param enrollments
     * @param gradesMap
     * @param gradableObjects
     * @param includeCourseGrade
     * @return
     */
    private List<List<Object>> getSpreadsheetData(List enrollments, Map gradesMap, List gradableObjects,
    												boolean includeCourseGrade, boolean localizeScores) {
    	List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();

    	NumberFormat nf = NumberFormat.getInstance(new ResourceLoader().getLocale());
    	// Build column headers and points possible rows.
        List<Object> headerRow = new ArrayList<Object>();
        List<Object> pointsPossibleRow = new ArrayList<Object>();
        
        headerRow.add(getLocalizedString("export_student_id"));
        headerRow.add(getLocalizedString("export_student_name"));
        
        for (Object gradableObject : gradableObjects) {
        	String colName = null;
        	Double ptsPossible = 0.0;

        	if (gradableObject instanceof GradebookAssignment) {
         		ptsPossible = new Double(((GradebookAssignment) gradableObject).getPointsPossible());
         		colName = ((GradebookAssignment)gradableObject).getName() + " [" + nf.format(ptsPossible) + "]";
         	} else if (gradableObject instanceof CourseGrade && includeCourseGrade) {
         		colName = getLocalizedString("roster_course_grade_column_name");
         		if(ServerConfigurationService.getBoolean("gradebook.roster.showCourseGradePoints", false)
    					&& ((CourseGrade) gradableObject).getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
         			colName += " " + getLocalizedString("roster_export_percentage");
         			//add total points header
         			headerRow.add(getLocalizedString("roster_course_grade_column_name") + " " + getLocalizedString("roster_export_points"));
         		}
         	}

         	headerRow.add(colName);
        }
        spreadsheetData.add(headerRow);

        // Build student score rows.
        for (Object enrollment : enrollments) {
        	User student = ((EnrollmentRecord)enrollment).getUser();
        	String studentUid = student.getUserUid();
        	Map studentMap = (Map)gradesMap.get(studentUid);
        	List<Object> row = new ArrayList<Object>();
        	row.add(student.getDisplayId());
        	row.add(student.getSortName());
        	for (Object gradableObject : gradableObjects) {
        		Object score = null;
        		String letterScore = null;
        		boolean droppedScore = false;
        		if (studentMap != null) {
        			Long gradableObjectId = ((GradableObject)gradableObject).getId();
        			
        			AbstractGradeRecord gradeRecord = (AbstractGradeRecord)studentMap.get(gradableObjectId); 

        			if (gradeRecord != null) {
        				if (gradeRecord.isCourseGradeRecord()) { 
        				    if (includeCourseGrade) {
        				        score = gradeRecord.getGradeAsPercentage();
        				        if(((CourseGradeRecord)gradeRecord).getEnteredGrade() != null){
        				        	score = "*" + score;
        				        }
        				        if(ServerConfigurationService.getBoolean("gradebook.roster.showCourseGradePoints", false)
        		    					&& ((CourseGrade) gradableObject).getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
        		         			//add total points
        				        	row.add(gradeRecord.getPointsEarned());
        				        }
        				    }
        				} else {
        					if(gradeRecord instanceof AssignmentGradeRecord){
        						droppedScore = ((AssignmentGradeRecord)gradeRecord).getDroppedFromGrade();
        					}
        					if (getGradeEntryByPoints()) {
        						score = gradeRecord.getPointsEarned();
        					} else if (getGradeEntryByPercent()) {
        						score = ((AssignmentGradeRecord)gradeRecord).getPercentEarned();
        					}	else if (getGradeEntryByLetter()) {
        						score = ((AssignmentGradeRecord)gradeRecord).getLetterEarned();
        					}
        				}
        			}
        		}
        		if (score != null && score instanceof Double) {
        			score = new Double(FacesUtil.getRoundDown(((Double)score).doubleValue(), 2));
        			// SAK-19849: do NOT localize the score if exporting to Excel. Let Excel localize it!
        			if (localizeScores) {
        				score = nf.format(score);
        			}
        		}
    			if(droppedScore){
    				score = score.toString() + " (" + getLocalizedString("export_dropped") + ")";
    			}
        		row.add(score);
        	}
        	spreadsheetData.add(row);
        }
    	
    	return spreadsheetData;
    }
    
    public String assignmentDetails(){
    	return "assignmentDetails";
    }

	/**
	 * Set Tool session navigation values when navigating
	 * to assignment details page.
	 */
	public String navigateToAssignmentDetails() {
		setNav("roster", "false", "false", "false", null);
		
		return "assignmentDetails";
	}
	
	public String saveHidden(){
		for (Iterator listIter = gradableObjectColumns.iterator(); listIter.hasNext();) {
			GradableObjectColumn col = (GradableObjectColumn) listIter.next();
			if(col.hasHiddenChanged()){
				//save
				GradebookAssignment assignment = getGradebookManager().getAssignment(col.getAssignmentId());
				assignment.setHideInAllGradesTable(col.getHideInAllGradesTable());
				getGradebookManager().updateAssignment(assignment);
			}
		}
		return null;
	}
}
