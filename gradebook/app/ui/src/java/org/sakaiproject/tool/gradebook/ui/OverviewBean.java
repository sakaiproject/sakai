/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
	private double totalPoints;

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

	protected void init() {

		gradebookItemList = new ArrayList();
		totalPoints = 0;

		if (getCategoriesEnabled()) {
			/* if categories are enabled, we need to display a table that includes
			 * categories, assignments, and possibly the course grade. Thus,
			 * we use a generic DecoratedGradebookItem
			 */
			CourseGrade courseGrade = new CourseGrade();
			List categoryList = getGradebookManager().getCategoriesWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending(), getCategorySortColumn(), isCategorySortAscending());
			if (categoryList != null && !categoryList.isEmpty()) {
				Iterator catIter = categoryList.iterator();
				while (catIter.hasNext()) {
					Object catOrCourseGrade = catIter.next();
					if (catOrCourseGrade instanceof Category) {
						Category myCat = (Category) catOrCourseGrade;
						gradebookItemList.add(myCat);
						List assignmentList = myCat.getAssignmentList();
						if (assignmentList != null && !assignmentList.isEmpty()) {
							Iterator assignIter = assignmentList.iterator();
							while (assignIter.hasNext()) {
								Assignment assign = (Assignment) assignIter.next();
								gradebookItemList.add(assign);
								if (assign.isCounted()) {
									totalPoints += assign.getPointsPossible().doubleValue();
								}
							}
						}
					}
					else if (catOrCourseGrade instanceof CourseGrade) {
						courseGrade = (CourseGrade) catOrCourseGrade;
					}
				}

			}
			List unassignedList = getGradebookManager().getAssignmentsWithNoCategoryWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending());
			if (unassignedList != null && !unassignedList.isEmpty()) {
				Category unassignedCat = new Category();
				unassignedCat.setAverageScore(new Double(0));
				unassignedCat.setName(getLocalizedString("cat_unassigned"));
				unassignedCat.setAssignmentList(unassignedList);
				unassignedCat.calculateStatistics(unassignedList);
				gradebookItemList.add(unassignedCat);

				Iterator unassignedIter = unassignedList.iterator();
				while (unassignedIter.hasNext()) {
					Assignment assignWithNoCat = (Assignment) unassignedIter.next();
					gradebookItemList.add(assignWithNoCat);
				}
			}
			
			// finally, append the course grade to the list
			if (courseGrade != null) {
				gradebookItemList.add(courseGrade);
			}
	        
		} else {
			// Get the list of assignments for this gradebook, sorted as defined in the overview page.
			List goList = getGradebookManager().getAssignmentsAndCourseGradeWithStats(getGradebookId(),
					getAssignmentSortColumn(), isAssignmentSortAscending());
			if (goList != null && !goList.isEmpty()) {
				Iterator goIter = goList.iterator();
				while (goIter.hasNext()) {
					GradableObject go = (GradableObject) goIter.next();
					if (go.isCourseGrade())
						gradebookItemList.add((CourseGrade)go);
					else {
						Assignment assign = (Assignment) go;
						gradebookItemList.add(assign);
						if (assign.isCounted()) {
							totalPoints += assign.getPointsPossible().doubleValue();
						}
					}
				}
			}
		}
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
    	StringBuffer sb = new StringBuffer();
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
    
	public Double getTotalPoints() {
		return new Double(totalPoints);
	}

}
