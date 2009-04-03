/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
public class OverviewBean extends GradebookDependentBean implements Serializable  {
	private static final Log logger = LogFactory.getLog(OverviewBean.class);

    private static final Map columnSortMap;

	private List gradebookItemList;
	private CourseGrade courseGrade;
	
	private boolean displayGradeEditorCol = false;

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(Assignment.SORT_BY_NAME, Assignment.nameComparator);
        columnSortMap.put(Assignment.SORT_BY_DATE, Assignment.dateComparator);
        columnSortMap.put(Assignment.SORT_BY_RELEASED,Assignment.releasedComparator);
        columnSortMap.put(Assignment.SORT_BY_MEAN, Assignment.meanComparator);
        columnSortMap.put(Assignment.SORT_BY_POINTS, Assignment.pointsComparator);
        columnSortMap.put(Assignment.SORT_BY_COUNTED, Assignment.countedComparator);
        columnSortMap.put(Assignment.SORT_BY_EDITOR, Assignment.gradeEditorComparator);

    }

	public List getGradebookItemList() {
		return gradebookItemList;
	}
	public void setGradebookItemList(List gradebookItemList) {
		this.gradebookItemList = gradebookItemList;
	}
	
	public CourseGrade getCourseGrade() {
		return courseGrade;
	}
	public void setCourseGrade(CourseGrade courseGrade) {
		this.courseGrade = courseGrade;
	}
	
	public String getAvgCourseGradeLetter() {
		String letterGrade = "";
		if (courseGrade != null) {
			letterGrade = getGradebook().getSelectedGradeMapping().getGrade(courseGrade.getMean());
		}
		
		return letterGrade;
	}

	protected void init() {

		gradebookItemList = new ArrayList();
		courseGrade = new CourseGrade();

		if (getCategoriesEnabled()) {
			/* if categories are enabled, we need to display a table that includes
			 * categories, assignments, and the course grade.
			 */
			List categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending(), getCategorySortColumn(), isCategorySortAscending());
			List categoryList = new ArrayList();
			
			// first, remove the CourseGrade from the Category list
			for (Iterator catIter = categoryListWithCG.iterator(); catIter.hasNext();) {
				Object catOrCourseGrade = catIter.next();
				if (catOrCourseGrade instanceof Category) {
					categoryList.add((Category)catOrCourseGrade);
				} else if (catOrCourseGrade instanceof CourseGrade) {
					courseGrade = (CourseGrade) catOrCourseGrade;
				}
			}
			
			// then, we need to check for special grader permissions that may limit which categories may be viewed
			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
				categoryList = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), categoryList, getGradebook().getCategory_type());
			}

			if (categoryList != null && !categoryList.isEmpty()) {
				Iterator catIter = categoryList.iterator();
				while (catIter.hasNext()) {
					Category myCat = (Category)catIter.next();

					gradebookItemList.add(myCat);
					List assignmentList = myCat.getAssignmentList();
					if (assignmentList != null && !assignmentList.isEmpty()) {
						Iterator assignIter = assignmentList.iterator();
						while (assignIter.hasNext()) {
							Assignment assign = (Assignment) assignIter.next();
							if (assign.isExternallyMaintained())
								displayGradeEditorCol = true;
							gradebookItemList.add(assign);
						}
					}
				}
			}
			
			if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions() && !getGradebookPermissionService().getPermissionForUserForAllAssignment(getGradebookId(), getUserUid()))) {
				// is not authorized to view the "Unassigned" Category
			} else {
				List unassignedList = getGradebookManager().getAssignmentsWithNoCategoryWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending());
				if (unassignedList != null && !unassignedList.isEmpty()) {
					Category unassignedCat = new Category();
					unassignedCat.setGradebook(getGradebook());
					unassignedCat.setAverageScore(new Double(0));
					unassignedCat.setName(getLocalizedString("cat_unassigned"));
					unassignedCat.setAssignmentList(unassignedList);
					if (!getWeightingEnabled())
						unassignedCat.calculateStatistics(unassignedList);
					gradebookItemList.add(unassignedCat);
	
					Iterator unassignedIter = unassignedList.iterator();
					while (unassignedIter.hasNext()) {
						Assignment assignWithNoCat = (Assignment) unassignedIter.next();
						if (assignWithNoCat.isExternallyMaintained())
							displayGradeEditorCol = true;
						gradebookItemList.add(assignWithNoCat);
					}
				}
			}
	        
		} else {
			// Get the list of assignments for this gradebook, sorted as defined in the overview page.
			List goList = getGradebookManager().getAssignmentsAndCourseGradeWithStats(getGradebookId(),
					getAssignmentSortColumn(), isAssignmentSortAscending());
			if (goList != null && !goList.isEmpty()) {
				Iterator goIter = goList.iterator();
				while (goIter.hasNext()) {
					GradableObject go = (GradableObject) goIter.next();
					if (go.isCourseGrade()) {
						courseGrade = (CourseGrade) go;
					} else {
						Assignment assign = (Assignment) go;
						if (assign.isExternallyMaintained())
							displayGradeEditorCol = true;
						gradebookItemList.add(assign);
					}
				}
			}
		}
		
		// Set up navigation
		ToolSession session = SessionManager.getCurrentToolSession();
		session.setAttribute("breadcrumbPage", "overview");
		session.removeAttribute("adding");
		session.removeAttribute("editing");
		session.removeAttribute("middle");
	}

    // Delegated sort methods
	public String getAssignmentSortColumn() {
        return getPreferencesBean().getAssignmentSortColumn();
	}
	public void setAssignmentSortColumn(String assignmentSortColumn) {
        getPreferencesBean().setAssignmentSortColumn(assignmentSortColumn);
    }
    public boolean isAssignmentSortAscending() {
        return getPreferencesBean().isAssignmentSortAscending();
	}
    public void setAssignmentSortAscending(boolean sortAscending) {
        getPreferencesBean().setAssignmentSortAscending(sortAscending);
    }
    public String getCategorySortColumn() {
        return getPreferencesBean().getCategorySortColumn();
	}
	public void setCategorySortColumn(String categorySortColumn) {
        getPreferencesBean().setCategorySortColumn(categorySortColumn);
    }
    public boolean isCategorySortAscending() {
        return getPreferencesBean().isCategorySortAscending();
	}
    public void setCategorySortAscending(boolean sortAscending) {
        getPreferencesBean().setCategorySortAscending(sortAscending);
    }

	/**
     * @return The comma-separated list of css styles to use in displaying the rows
     */
    public String getRowStyles() {
    	StringBuilder sb = new StringBuilder();
    	for(Iterator iter = gradebookItemList.iterator(); iter.hasNext();) {
    		Object gradebookItem = iter.next();
    		if (gradebookItem instanceof GradableObject) {
    			GradableObject go = (GradableObject)gradebookItem;
    			if(go.isCourseGrade()) {
    				sb.append("internal");
    				break;
    			} else {
    				Assignment asn = (Assignment)go;
    				if(asn.isExternallyMaintained()) {
    					sb.append("external,");
    				} else {
    					sb.append("internal,");
    				}
    			}
    		} else {
    			sb.append("internal,");
    		}
    		
    	}
    	return sb.toString();
    }

    public String getGradeOptionSummary() {
    	String gradeOptionSummary;
    	Gradebook gradebook = getGradebook();
    	String gradeMappingName = gradebook.getSelectedGradeMapping().getName();
    	if (gradebook.isAssignmentsDisplayed()) {
    		if (gradebook.isCourseGradeDisplayed()) {
    			gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_all_viewable", new String[] {gradeMappingName});
    		} else {
    			gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_assignments_viewable");
    		}
    	} else if (gradebook.isCourseGradeDisplayed()) {
    		gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_course_grade_viewable", new String[] {gradeMappingName});
    	} else {
    		gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_none_viewable");
    	}
    	return gradeOptionSummary;
    }
    
    public boolean isDisplayGradeEditorCol() {
    	return displayGradeEditorCol;
    }

    /**
     * Set state when navigating to edit page directly from overview page.
     */
	public String navigateToEdit() {
		setNav("overview","true","false","false", null);
		
		return "editAssignment";
	}
	
    /**
     * Set state when navigating to add page directly from overview page
     */
	public String navigateToAddAssignment() {
		setNav("overview", "false", "true", "false", null);
				
		return "addAssignment";
	}
	
    /**
     * Set state when navigating to assignment details page directly from overview page
     */
	public String navigateToAssignmentDetails() {
		setNav("overview", "false", "false", "false", null);
				
		return "assignmentDetails";		
	}
	
    /**
     * Set state when navigating to spreadsheet dock page directly from overview page.
     */
	public String navigateToSpreadsheet() {
		setNav("overview", "false", "false", "false", "spreadsheetListing");

		return "spreadsheetListing";
	}

	/**
	 * Since Gradebook Items (Overview) is the default page, to deal with the case where
	 * navigating from another tool/clicked refresh, reset the navigation to "overview" 
	 */
	public void setBreadcrumbPageParam(String breadcrumbPageParam) {
		if (SessionManager.getCurrentToolSession().getAttribute(BREADCRUMBPAGE) != null) {
			if ((breadcrumbPageParam != null) && !breadcrumbPageParam.equals("null")) {
				setBreadcrumbPage(breadcrumbPageParam);
				if (!"".equals(breadcrumbPageParam)) SessionManager.getCurrentToolSession().setAttribute(BREADCRUMBPAGE, breadcrumbPageParam);
			}
			else {
				setBreadcrumbPage("overview");
			}
		}
	}
	
	public boolean getIsLetterGrade()
	{
		if(isUserAbleToEditAssessments())
		{
			Gradebook gb = getGradebookManager().getGradebookWithGradeMappings(getGradebookId());
			if(gb != null && gb.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
			{
				return true;
			}
			return false;
		}
		return false;
	}
}
