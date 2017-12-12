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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
@Slf4j
public class OverviewBean extends GradebookDependentBean implements Serializable  {
    private static final Map columnSortMap;

	private List gradebookItemList;
	private CourseGrade courseGrade;
	
	private boolean displayGradeEditorCol = false;

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(GradebookAssignment.SORT_BY_NAME, GradableObject.nameComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_DATE, GradableObject.dateComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_RELEASED, GradebookAssignment.releasedComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_MEAN, GradableObject.meanComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_POINTS, GradebookAssignment.pointsComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_COUNTED, GradebookAssignment.countedComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_EDITOR, GradebookAssignment.gradeEditorComparator);
        columnSortMap.put(GradebookAssignment.SORT_BY_SORTING, GradableObject.sortingComparator);
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

    /**
     * Controls the display of the "Total Points" column on the overview screen.
     * DEFAULT: false (Total Points column is not shown)
     */
    private Boolean displayTotalPoints;
    public boolean isDisplayTotalPoints() {
        if (displayTotalPoints == null) {
            displayTotalPoints = ServerConfigurationService.getBoolean("gradebook.display.total.points", false);
        }
        return displayTotalPoints;
    }

	protected void init() {

		gradebookItemList = new ArrayList();
		courseGrade = new CourseGrade();

		if (getCategoriesEnabled()) {
			/* if categories are enabled, we need to display a table that includes
			 * categories, assignments, and the course grade.
			 */
			List categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending(), getCategorySortColumn(), isCategorySortAscending());
			List<Category> categoryList = new ArrayList<Category>();
			
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
				//SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
				List<Long> catIds = new ArrayList<Long>();
				for (Category category : (List<Category>) categoryList) {
					catIds.add(category.getId());
				}
				List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), catIds);
				List<Category> tmpCatList = new ArrayList<Category>();
				for (Category category : (List<Category>) categoryList) {
					if(viewableCats.contains(category.getId())){
						tmpCatList.add(category);
					}
				}
				categoryList = tmpCatList;
			}

			if (categoryList != null && !categoryList.isEmpty()) {
				Comparator catComparator = null;
				if(GradebookAssignment.SORT_BY_MEAN.equals(getAssignmentSortColumn())){
					catComparator = Category.averageScoreComparator;
				}else if(Category.SORT_BY_WEIGHT.equals(getAssignmentSortColumn())){
					catComparator = Category.weightComparator;
				}else if(Category.SORT_BY_NAME.equals(getAssignmentSortColumn())){
					catComparator = Category.nameComparator;
				}
				if(catComparator != null){
					Collections.sort(categoryList, catComparator);
					if(!isAssignmentSortAscending()){
						Collections.reverse(categoryList);
					}
				}				
				
				Iterator catIter = categoryList.iterator();
				while (catIter.hasNext()) {
					Category myCat = (Category)catIter.next();

					gradebookItemList.add(myCat);
					List assignmentList = myCat.getAssignmentList();
					if (assignmentList != null && !assignmentList.isEmpty()) {
						Iterator assignIter = assignmentList.iterator();
						while (assignIter.hasNext()) {
							GradebookAssignment assign = (GradebookAssignment) assignIter.next();
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
						GradebookAssignment assignWithNoCat = (GradebookAssignment) unassignedIter.next();
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
						GradebookAssignment assign = (GradebookAssignment) go;
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
    				GradebookAssignment asn = (GradebookAssignment)go;
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
	    if (log.isDebugEnabled()) {
	        log.debug("Sort: sorting up: " + assignmentId);
	    }
        List<GradebookAssignment> assignments = getGradebookManager().getAssignments(getGradebookId());
        if (assignments.size() > 1) {
            ensureAssignmentsSorted(assignments, GradableObject.sortingComparator, true);
            // now adjust the numbering
            for (int i = 0; i < assignments.size(); i++) {
                GradebookAssignment a1 = assignments.get(i);
                if (a1.getId().equals(assignmentId)) {
                    if (i > 0) {
                        GradebookAssignment a2 = assignments.get(i-1);
                        // only swap items which are in the same category
                        if ( (a1.getCategory() == null && a2.getCategory() == null)
                                || (a1.getCategory().equals(a2.getCategory())) ) {
                            // swap the ordering of this item and the item below it
                            Integer holder = a1.getSortOrder();
                            a1.setSortOrder(a2.getSortOrder());
                            a2.setSortOrder(holder);
                            log.info("Sort: UP swapping: "+a1.getId()+" (to "+a1.getSortOrder()+" from "+holder+") with "+a2.getId());
                            // save the new orders
                            getGradebookManager().updateAssignment(a1);
                            getGradebookManager().updateAssignment(a2);
                        } else {
                            log.info("Sort: UP: unable to swap items ("+a1.getId()+","+a2.getId()+") in different categories");
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
        if (log.isDebugEnabled()) {
            log.debug("Sort: sorting down: " + assignmentId);
        }
        List<GradebookAssignment> assignments = getGradebookManager().getAssignments(getGradebookId());
        if (assignments.size() > 1) {
            ensureAssignmentsSorted(assignments, GradableObject.sortingComparator, true);
            // now adjust the numbering
            for (int i = 0; i < assignments.size(); i++) {
                GradebookAssignment a1 = assignments.get(i);
                if (a1.getId().equals(assignmentId)) {
                    if (i < (assignments.size() - 1)) {
                        GradebookAssignment a2 = assignments.get(i+1);
                        // only swap items which are in the same category
                        if ( (a1.getCategory() == null && a2.getCategory() == null)
                                || (a1.getCategory().equals(a2.getCategory())) ) {
                            // swap the ordering of this item and the item below it
                            Integer holder = a1.getSortOrder();
                            a1.setSortOrder(a2.getSortOrder());
                            a2.setSortOrder(holder);
                            log.info("Sort: DOWN swapping: "+a1.getId()+" (to "+a1.getSortOrder()+" from "+holder+") with "+a2.getId());
                            // save the new orders
                            getGradebookManager().updateAssignment(a1);
                            getGradebookManager().updateAssignment(a2);
                        } else {
                            log.info("Sort: DOWN: unable to swap items ("+a1.getId()+","+a2.getId()+") in different categories");
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
            sortColumn = GradebookAssignment.DEFAULT_SORT;
        }
        boolean ascending = isAssignmentSortAscending();
        if (log.isDebugEnabled()) {
            log.debug("saveCurrentSort: saving current sort order ("+sortColumn+", "+ascending+") for gradebook: " + getGradebookId());
        }
        List<GradebookAssignment> assignments = getGradebookManager().getAssignmentsAndCourseGradeWithStats(getGradebookId(), sortColumn, ascending); //getAssignmentsWithNoCategoryWithStats(getGradebookId(), sortColumn, ascending);
        if (log.isDebugEnabled()) {
            log.debug("saveCurrentSort: current order ("+assignments.size()+"): " + Arrays.toString(assignments.toArray()));
        }
        // now ensure the numbering is set and correct
        ensureAssignmentsSorted(assignments, new NoChangeMarkerComparator(), true);
        if (log.isDebugEnabled()) {
            log.debug("saveCurrentSort: final order ("+assignments.size()+"): " + Arrays.toString(assignments.toArray()));
        }
        log.info("Sort: Saved current sort order ("+sortColumn+") [asc="+ascending+"] for gradebook ("+assignments.size()+" items): " + getGradebookId());
        // force sorting back to defaults
        setAssignmentSortColumn(GradebookAssignment.DEFAULT_SORT);
        setAssignmentSortAscending(true);
    }

    @SuppressWarnings("unchecked")
    public boolean getEnabledSaveSort() {
        boolean enabled = false;
        String sortColumn = getAssignmentSortColumn();
        boolean ascending = isAssignmentSortAscending();
        // if default sort is set, no save allowed
        if (sortColumn != null 
                && (! GradebookAssignment.DEFAULT_SORT.equals(sortColumn)
                    || (GradebookAssignment.DEFAULT_SORT.equals(sortColumn) && !ascending) ) ) {
            enabled = true;
        }
        if (enabled) {
            // if allowed then check that there are enough assignments
            List<GradebookAssignment> assignments = getGradebookManager().getAssignments(getGradebookId());
            if (assignments.size() > 1) { // factor out the category
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
	    if (log.isDebugEnabled()) {
	        log.debug("ensureAssignmentsSorted: comparator="+comparator+", save="+save+", assignments= "+Arrays.toString(assignments.toArray()));
	    }
	    // remove any non-assignments first
	    List gradeables = new ArrayList();
        for (Iterator iterator = assignments.iterator(); iterator.hasNext();) {
            GradableObject go = (GradableObject) iterator.next();
            if (! (go instanceof GradebookAssignment)) { // ! go.isAssignment()) {
                gradeables.add(go);
                iterator.remove();
            }
        }
        Collections.sort(gradeables, GradableObject.nameComparator);

        // put everything in the established sort order first (if needed)
        if (comparator == null) {
            comparator = GradableObject.dateComparator;
            if (log.isDebugEnabled()) {
                log.debug("ensureAssignmentsSorted: setting default comparator="+comparator);
            }
        }
        if (! NoChangeMarkerComparator.class.isAssignableFrom(comparator.getClass())) {
            // only sort if this is not the no-sort marker
            if (log.isDebugEnabled()) {
                log.debug("ensureAssignmentsSorted: sorting with comparator="+comparator);
            }
            Collections.sort(assignments, comparator);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("ensureAssignmentsSorted: no sort, using NoChangeMarkerComparator="+comparator);
            }
        }
        // always need to sort by category
        Collections.sort(assignments, GradableObject.categoryComparator);
        // now ensure the numbering is set and correct
        int saveCount = 0;
        int updateCount = 0;
        for (int i = 0; i < assignments.size(); i++) {
            GradebookAssignment assignment = (GradebookAssignment) assignments.get(i);
            Integer curOrder = assignment.getSortOrder();
            if (log.isDebugEnabled()) {
                log.debug("ensureAssignmentsSorted: checking if current order ("+curOrder+") matches correct order ("+i+") for assignment: "+assignment);
            }
            if (curOrder == null || i != curOrder.intValue()) {
                // no match so we need to update it (else it is already set correctly, only save if needed)
                assignment.setSortOrder(i);
                updateCount++;
                if (log.isDebugEnabled()) {
                    log.debug("ensureAssignmentsSorted: setting sort order ("+i+") for assignment: "+assignment);
                }
                if (save) {
                    getGradebookManager().updateAssignment(assignment);
                    saveCount++;
                    if (log.isDebugEnabled()) {
                        log.debug("ensureAssignmentsSorted: saving assignment: "+assignment);
                    }
                }
            }
        }

        // set the ordering up in the assignment with support for categories
        Map<String, List<GradebookAssignment>> categoryAssignments = new LinkedHashMap<String, List<GradebookAssignment>>();
        for (GradebookAssignment assignment : (List<GradebookAssignment>) assignments) {
            String category = "NULL";
            if (assignment.getCategory() != null) {
                category = assignment.getCategory().getName();
            }
            if (! categoryAssignments.containsKey(category)) {
                categoryAssignments.put(category, new ArrayList<GradebookAssignment>());
            }
            categoryAssignments.get(category).add(assignment);
            //assignment.assignSorting(assignments.size(), i);
        }
        for (Entry<String, List<GradebookAssignment>> entry : categoryAssignments.entrySet()) {
            List<GradebookAssignment> l = entry.getValue();
            for (int i = 0; i < l.size(); i++) {
                GradebookAssignment assignment = l.get(i);
                // assign the counter for ordering
                assignment.assignSorting(l.size(), i);
                if (log.isDebugEnabled()) {
                    log.debug("ensureAssignmentsSorted: ordered: "+i+" : "+assignment);
                }
            }
        }
        // add back in the gradeables to the end
        for (Object gradeable : gradeables) {
            assignments.add(gradeable);
        }
        if (log.isDebugEnabled()) {
            log.debug("ensureAssignmentsSorted: sorted assignments (updated="+updateCount+", saved="+saveCount+"): "+Arrays.toString(assignments.toArray()));
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
