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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
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
        columnSortMap.put(Assignment.SORT_BY_NAME, GradableObject.nameComparator);
        columnSortMap.put(Assignment.SORT_BY_DATE, GradableObject.dateComparator);
        columnSortMap.put(Assignment.SORT_BY_RELEASED,Assignment.releasedComparator);
        columnSortMap.put(Assignment.SORT_BY_MEAN, GradableObject.meanComparator);
        columnSortMap.put(Assignment.SORT_BY_POINTS, Assignment.pointsComparator);
        columnSortMap.put(Assignment.SORT_BY_COUNTED, Assignment.countedComparator);
        columnSortMap.put(Assignment.SORT_BY_EDITOR, Assignment.gradeEditorComparator);
        columnSortMap.put(Assignment.SORT_BY_SORTING, GradableObject.sortingComparator);
        columnSortMap.put("default", GradableObject.defaultComparator);
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
						ensureAssignmentsSorted(assignmentList, GradableObject.sortingComparator, false);
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
                ensureAssignmentsSorted(unassignedList, GradableObject.sortingComparator, false);
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
                ensureAssignmentsSorted(goList, GradableObject.sortingComparator, false);
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

	@SuppressWarnings("unchecked")
    public void sortUp() {
	    Long assignmentId = getAssignmentIdFromParam();
	    if (logger.isDebugEnabled()) {
	        logger.debug("Sort: sorting up: " + assignmentId);
	    }
        List<Assignment> assignments = getGradebookManager().getAssignments(getGradebookId());
        if (assignments.size() > 1) {
            ensureAssignmentsSorted(assignments, GradableObject.sortingComparator, true);
            // now adjust the numbering
            for (int i = 0; i < assignments.size(); i++) {
                Assignment a1 = assignments.get(i);
                if (a1.getId().equals(assignmentId)) {
                    if (i > 0) {
                        Assignment a2 = assignments.get(i-1);
                        // only swap items which are in the same category
                        if ( (a1.getCategory() == null && a2.getCategory() == null)
                                || (a1.getCategory().equals(a2.getCategory())) ) {
                            // swap the ordering of this item and the item below it
                            Integer holder = a1.getSortOrder();
                            a1.setSortOrder(a2.getSortOrder());
                            a2.setSortOrder(holder);
                            logger.info("Sort: UP swapping: "+a1.getId()+" (to "+a1.getSortOrder()+" from "+holder+") with "+a2.getId());
                            // save the new orders
                            getGradebookManager().updateAssignment(a1);
                            getGradebookManager().updateAssignment(a2);
                        } else {
                            logger.info("Sort: UP: unable to swap items ("+a1.getId()+","+a2.getId()+") in different categories");
                        }

                    }
                    break;
                }
            }
        }
	}

    @SuppressWarnings("unchecked")
    public void sortDown() {
        Long assignmentId = getAssignmentIdFromParam();
        if (logger.isDebugEnabled()) {
            logger.debug("Sort: sorting down: " + assignmentId);
        }
        List<Assignment> assignments = getGradebookManager().getAssignments(getGradebookId());
        if (assignments.size() > 1) {
            ensureAssignmentsSorted(assignments, GradableObject.sortingComparator, true);
            // now adjust the numbering
            for (int i = 0; i < assignments.size(); i++) {
                Assignment a1 = assignments.get(i);
                if (a1.getId().equals(assignmentId)) {
                    if (i < (assignments.size() - 1)) {
                        Assignment a2 = assignments.get(i+1);
                        // only swap items which are in the same category
                        if ( (a1.getCategory() == null && a2.getCategory() == null)
                                || (a1.getCategory().equals(a2.getCategory())) ) {
                            // swap the ordering of this item and the item below it
                            Integer holder = a1.getSortOrder();
                            a1.setSortOrder(a2.getSortOrder());
                            a2.setSortOrder(holder);
                            logger.info("Sort: DOWN swapping: "+a1.getId()+" (to "+a1.getSortOrder()+" from "+holder+") with "+a2.getId());
                            // save the new orders
                            getGradebookManager().updateAssignment(a1);
                            getGradebookManager().updateAssignment(a2);
                        } else {
                            logger.info("Sort: DOWN: unable to swap items ("+a1.getId()+","+a2.getId()+") in different categories");
                        }
                    }
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void saveCurrentSort() {
        String sortColumn = getAssignmentSortColumn();
        if (sortColumn == null) {
            sortColumn = Assignment.DEFAULT_SORT;
        }
        boolean ascending = isAssignmentSortAscending();
        if (logger.isDebugEnabled()) {
            logger.debug("saveCurrentSort: saving current sort order ("+sortColumn+", "+ascending+") for gradebook: " + getGradebookId());
        }
        List<Assignment> assignments = getGradebookManager().getAssignmentsAndCourseGradeWithStats(getGradebookId(), sortColumn, ascending); //getAssignmentsWithNoCategoryWithStats(getGradebookId(), sortColumn, ascending);
        if (logger.isDebugEnabled()) {
            logger.debug("saveCurrentSort: current order ("+assignments.size()+"): " + Arrays.toString(assignments.toArray()));
        }
        // now ensure the numbering is set and correct
        ensureAssignmentsSorted(assignments, new NoChangeMarkerComparator(), true);
        if (logger.isDebugEnabled()) {
            logger.debug("saveCurrentSort: final order ("+assignments.size()+"): " + Arrays.toString(assignments.toArray()));
        }
        logger.info("Sort: Saved current sort order ("+sortColumn+") [asc="+ascending+"] for gradebook ("+assignments.size()+" items): " + getGradebookId());
        // force sorting back to defaults
        setAssignmentSortColumn(Assignment.DEFAULT_SORT);
        setAssignmentSortAscending(true);
    }

    @SuppressWarnings("unchecked")
    public boolean getEnabledSaveSort() {
        boolean enabled = false;
        String sortColumn = getAssignmentSortColumn();
        boolean ascending = isAssignmentSortAscending();
        // if default sort is set, no save allowed
        if (sortColumn != null 
                && (! Assignment.DEFAULT_SORT.equals(sortColumn)
                    || (Assignment.DEFAULT_SORT.equals(sortColumn) && !ascending) ) ) {
            enabled = true;
        }
        if (enabled) {
            // if allowed then check that there are enough assignments
            List<Assignment> assignments = getGradebookManager().getAssignments(getGradebookId());
            if (assignments.size() > 2) { // factor out the category
                enabled = true;
            } else {
                enabled = false;
            }
        }
        return enabled;
    }

	private Long getAssignmentIdFromParam() {
        FacesContext context = FacesContext.getCurrentInstance();
        String[] assignmentIds = (String[]) context.getExternalContext().getRequestParameterValuesMap().get("assignmentId");
        if (assignmentIds == null || assignmentIds.length == 0) {
            throw new IllegalArgumentException("assignmentId must be set");
        }
        Long assignmentId = Long.valueOf(assignmentIds[0]);
        return assignmentId;
	}

	@SuppressWarnings("unchecked")
    private void ensureAssignmentsSorted(List assignments, Comparator comparator, boolean save) {
	    if (logger.isDebugEnabled()) {
	        logger.debug("ensureAssignmentsSorted: comparator="+comparator+", save="+save+", assignments= "+Arrays.toString(assignments.toArray()));
	    }
	    // remove any non-assignments first
	    List gradeables = new ArrayList();
        for (Iterator iterator = assignments.iterator(); iterator.hasNext();) {
            GradableObject go = (GradableObject) iterator.next();
            if (! (go instanceof Assignment)) { // ! go.isAssignment()) {
                gradeables.add(go);
                iterator.remove();
            }
        }
        Collections.sort(gradeables, GradableObject.nameComparator);

        // put everything in the established sort order first (if needed)
        if (comparator == null) {
            comparator = GradableObject.dateComparator;
            if (logger.isDebugEnabled()) {
                logger.debug("ensureAssignmentsSorted: setting default comparator="+comparator);
            }
        }
        if (! NoChangeMarkerComparator.class.isAssignableFrom(comparator.getClass())) {
            // only sort if this is not the no-sort marker
            if (logger.isDebugEnabled()) {
                logger.debug("ensureAssignmentsSorted: sorting with comparator="+comparator);
            }
            Collections.sort(assignments, comparator);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("ensureAssignmentsSorted: no sort, using NoChangeMarkerComparator="+comparator);
            }
        }
        // always need to sort by category
        Collections.sort(assignments, GradableObject.categoryComparator);
        // now ensure the numbering is set and correct
        int saveCount = 0;
        int updateCount = 0;
        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment = (Assignment) assignments.get(i);
            Integer curOrder = assignment.getSortOrder();
            if (logger.isDebugEnabled()) {
                logger.debug("ensureAssignmentsSorted: checking if current order ("+curOrder+") matches correct order ("+i+") for assignment: "+assignment);
            }
            if (curOrder == null || i != curOrder.intValue()) {
                // no match so we need to update it (else it is already set correctly, only save if needed)
                assignment.setSortOrder(i);
                updateCount++;
                if (logger.isDebugEnabled()) {
                    logger.debug("ensureAssignmentsSorted: setting sort order ("+i+") for assignment: "+assignment);
                }
                if (save) {
                    getGradebookManager().updateAssignment(assignment);
                    saveCount++;
                    if (logger.isDebugEnabled()) {
                        logger.debug("ensureAssignmentsSorted: saving assignment: "+assignment);
                    }
                }
            }
        }

        // set the ordering up in the assignment with support for categories
        Map<String, List<Assignment>> categoryAssignments = new LinkedHashMap<String, List<Assignment>>();
        for (Assignment assignment : (List<Assignment>) assignments) {
            String category = "NULL";
            if (assignment.getCategory() != null) {
                category = assignment.getCategory().getName();
            }
            if (! categoryAssignments.containsKey(category)) {
                categoryAssignments.put(category, new ArrayList<Assignment>());
            }
            categoryAssignments.get(category).add(assignment);
            //assignment.assignSorting(assignments.size(), i);
        }
        for (Entry<String, List<Assignment>> entry : categoryAssignments.entrySet()) {
            List<Assignment> l = entry.getValue();
            for (int i = 0; i < l.size(); i++) {
                Assignment assignment = l.get(i);
                // assign the counter for ordering
                assignment.assignSorting(l.size(), i);
                if (logger.isDebugEnabled()) {
                    logger.debug("ensureAssignmentsSorted: ordered: "+i+" : "+assignment);
                }
            }
        }
        // add back in the gradeables to the end
        for (Object gradeable : gradeables) {
            assignments.add(gradeable);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ensureAssignmentsSorted: sorted assignments (updated="+updateCount+", saved="+saveCount+"): "+Arrays.toString(assignments.toArray()));
        }
    }

	/**
	 * Special marker class to preserve the order when saving the sort order
	 */
	public static class NoChangeMarkerComparator implements Comparator<GradableObject> {
        public int compare(GradableObject o1, GradableObject o2) {
            return -1; // preserve the existing order
        }
        @Override
        public String toString() {
            return "NoChangeMarkerComparator";
        }
	}

}
