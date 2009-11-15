/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.tool.gradebook.ui.AssignmentGradeRow;

/**
 * Provides data for the student view of the gradebook. Is used by both the
 * instructor and student views. Based upon original StudentViewBean
 *
 */
public class ViewByStudentBean extends EnrollmentTableBean implements Serializable {
	private static Log logger = LogFactory.getLog(ViewByStudentBean.class);

    // View maintenance fields - serializable.
    private String userDisplayName;
    private boolean courseGradeReleased;
    private CourseGradeRecord courseGrade;
    private String courseGradeLetter;
    private boolean assignmentsReleased;
    private boolean anyNotCounted;
    private boolean anyExternallyMaintained = false;
    private boolean isAllItemsViewOnly = true;

    private boolean sortAscending;
    private String sortColumn;
    
    private boolean isInstructorView = false;

    private StringBuilder rowStyles;
    private Map commentMap;

    private List gradebookItems;
    private String studentUid;
    private Gradebook gradebook;

    private static final Map columnSortMap;
    private static final String SORT_BY_NAME = "name";
    protected static final String SORT_BY_DATE = "dueDate";
    protected static final String SORT_BY_POINTS_POSSIBLE = "pointsPossible";
    protected static final String SORT_BY_POINTS_EARNED = "pointsEarned";
    protected static final String SORT_BY_GRADE = "grade";
    protected static final String SORT_BY_ITEM_VALUE = "itemValue";
    protected static final String SORT_BY_SORTING = "sorting";
    public static Comparator nameComparator;
    public static Comparator dateComparator;
    public static Comparator pointsPossibleComparator;
    public static Comparator pointsEarnedComparator;
    public static Comparator gradeAsPercentageComparator;
    private static Comparator doubleOrNothingComparator;
    private static Comparator itemValueComparator;
    private static Comparator gradeEditorComparator;
    public static Comparator sortingComparator;
    static {
        sortingComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return GradableObject.sortingComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment(), ((AssignmentGradeRow)o2).getAssociatedAssignment());
            }
        };
        nameComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.nameComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment(), ((AssignmentGradeRow)o2).getAssociatedAssignment());
            }
        };
        dateComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.dateComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment(), ((AssignmentGradeRow)o2).getAssociatedAssignment());
            }
        };
        pointsPossibleComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.pointsComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment(), ((AssignmentGradeRow)o2).getAssociatedAssignment());
            }
        };

        doubleOrNothingComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                Double double1 = (Double)o1;
                Double double2 = (Double)o2;

                if(double1 == null && double2 == null) {
                    return 0;
                } else if(double1 == null && double2 != null) {
                    return -1;
                } else if(double1 != null && double2 == null) {
                    return 1;
                } else {
                    return double1.compareTo(double2);
                }
            }
        };

        pointsEarnedComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
            	int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow)o1).getPointsEarned(), ((AssignmentGradeRow)o2).getPointsEarned());
                if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
            }
        };
        gradeAsPercentageComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
            	int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow)o1).getGradeAsPercentage(), ((AssignmentGradeRow)o2).getGradeAsPercentage());
                if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
            }
        };
        itemValueComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
            	int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment().getPointsPossible(), ((AssignmentGradeRow)o2).getAssociatedAssignment().getPointsPossible());
                if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
            }
        };
        gradeEditorComparator = new Comparator() {
        	public int compare(Object o1, Object o2) {
        		return Assignment.gradeEditorComparator.compare(((AssignmentGradeRow)o1).getAssociatedAssignment(), ((AssignmentGradeRow)o2).getAssociatedAssignment());
        	}
        };

        columnSortMap = new HashMap();
        columnSortMap.put(SORT_BY_SORTING, ViewByStudentBean.sortingComparator);
        columnSortMap.put(SORT_BY_NAME, ViewByStudentBean.nameComparator);
        columnSortMap.put(SORT_BY_DATE, ViewByStudentBean.dateComparator);
        columnSortMap.put(SORT_BY_POINTS_POSSIBLE, ViewByStudentBean.pointsPossibleComparator);
        columnSortMap.put(SORT_BY_POINTS_EARNED, ViewByStudentBean.pointsEarnedComparator);
        columnSortMap.put(SORT_BY_GRADE, ViewByStudentBean.gradeAsPercentageComparator);
        columnSortMap.put(SORT_BY_ITEM_VALUE, ViewByStudentBean.itemValueComparator);
        columnSortMap.put(Assignment.SORT_BY_EDITOR, ViewByStudentBean.gradeEditorComparator);
    }
    
    /**
     * Since this bean does not use the session-scoped preferences bean to keep
     * sort preferences, we need to define the defaults locally.
     */
    public ViewByStudentBean() {
        // SAK-15311 - setup so students view can use sorting if configured
        boolean useSort = getGradebookBean().getConfigurationBean().getBooleanConfig("gradebook.students.use.sorting", false);
        if (useSort) {
            // use sorting order
            sortAscending = true;
            sortColumn = SORT_BY_SORTING;
        } else {
            // use old default
            sortAscending = true;
            sortColumn = SORT_BY_DATE;
        }
    }

    /**
     * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
     */
    public void init() {
    	// Get the active gradebook
    	gradebook = getGradebook();
    	
    	isAllItemsViewOnly = true;

    	// Set the display name
    	try {
    		userDisplayName = getUserDirectoryService().getUserDisplayName(studentUid);
    	} catch (UnknownUserException e) {
    		if(logger.isErrorEnabled())logger.error("User " + studentUid + " is unknown but referenced in gradebook " + gradebook.getUid());
    		userDisplayName = "";
    	}
    	
    	courseGradeReleased = gradebook.isCourseGradeDisplayed();
    	assignmentsReleased = gradebook.isAssignmentsDisplayed();

    	// Reset the row styles
    	rowStyles = new StringBuilder();

    	// Display course grade if we've been instructed to.
    	CourseGradeRecord gradeRecord = getGradebookManager().getStudentCourseGradeRecord(gradebook, studentUid);
    	if (gradeRecord != null) {
    		if (courseGradeReleased || isInstructorView) {
    			courseGrade = gradeRecord;
    			courseGradeLetter = gradeRecord.getDisplayGrade();
    		}
    	}
    	
    	initializeStudentGradeData();
    }


    /**
     * @return Returns the gradebookItems. Can include AssignmentGradeRows and Categories
     */
    public List getGradebookItems() {
        return gradebookItems;
    }
    /**
     * @return Returns the CourseGradeRecord for this student
     */
    public CourseGradeRecord getCourseGrade() {
        return courseGrade;
    }
    /**
     * 
     * @return letter representation of course grade
     */
    public String getCourseGradeLetter() {
    	return courseGradeLetter;
    }
    /**
     * @return Returns the courseGradeReleased.
     */
    public boolean isCourseGradeReleased() {
        return courseGradeReleased;
    }

    public boolean isAssignmentsReleased() {
        return assignmentsReleased;
    }

    /**
     * @return Returns the userDisplayName.
     */
    public String getUserDisplayName() {
        return userDisplayName;
    }
    /**
     * Sets userDisplayName
     * @param userDisplayName
     */
    public void setUserDisplayName(String userDisplayName) {
    	this.userDisplayName = userDisplayName;
    }

    // Sorting
    public boolean isSortAscending() {
        return sortAscending;
    }
    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
    public String getSortColumn() {
        return sortColumn;
    }
    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * @return The comma-separated list of css styles to use in displaying the rows
     */
    public String getRowStyles() {
        if(rowStyles == null) {
            return null;
        } else {
            return rowStyles.toString();
        }
    }
    
    public String getEventsLogType() {
		return getLocalizedString("inst_view_log_type");
	}

    /**
     * @return True if the gradebook contains any assignments not counted toward
     *         the final course grade.
     */
    public boolean isAnyNotCounted() {
        return anyNotCounted;
    }
    
    /**
     * if all items are "view-only", we need to disable the action buttons
     * @return
     */
    public boolean isAllItemsViewOnly() {
    	return isAllItemsViewOnly;
    }
    
    /**
     * 
     * @return true if the gradebook contains any externally maintained assignments
     */
    public boolean isAnyExternallyMaintained() {
    	return anyExternallyMaintained;
    }
    
    public void setStudentUid(String studentUid) {
    	this.studentUid = studentUid;
    }
    public String getStudentUid() {
    	return studentUid;
    }
    
    
    /**
     * Instructor view will include some features that aren't appropriate
     * for student view
     * @param includeNotCountedInCategoryAvg
     */
    public void setIsInstructorView(boolean isInstructorView) {
    	this.isInstructorView = isInstructorView;
    }
    
    /**
     * Create the AssignmentGradeRows for the passed assignments list
     * @param assignments
     * @param gradeRecords
     * @return
     */
    private List retrieveGradeRows(List assignments, List gradeRecords) {
    	List gradeRows = new ArrayList();

    	// Don't display any assignments if they have not been released
    	if(!assignmentsReleased && !isInstructorView) 
    		return gradeRows;
    	
    	if (assignments == null)
    		return gradeRows;

    	if(logger.isDebugEnabled()) {
    		logger.debug(assignments.size() + " total assignments");
    		logger.debug(gradeRecords.size()  +"  grade records");
    	}
    	
		boolean userHasGraderPerms;
		if (isUserAbleToGradeAll())
			userHasGraderPerms = false;
		else if (isUserHasGraderPermissions())
			userHasGraderPerms = true;
		else
			userHasGraderPerms = false;
		
		Map viewableAssignmentsMap = new HashMap();
		if (userHasGraderPerms) {
			viewableAssignmentsMap = getGradebookPermissionService().getAvailableItemsForStudent(gradebook.getId(), getUserUid(), studentUid, getAllSections());
		}

    	// Create a map of assignments to assignment grade rows
    	Map asnMap = new HashMap();
    	for(Iterator iter = assignments.iterator(); iter.hasNext();) {

    		Assignment asn = (Assignment)iter.next();

    		if (userHasGraderPerms) {
    			String function = (String)viewableAssignmentsMap.get(asn.getId());
    			if (function != null) {
    				boolean userCanGrade = function.equalsIgnoreCase(GradebookService.gradePermission);
    				if (userCanGrade)
    					isAllItemsViewOnly = false;
    				asnMap.put(asn, new AssignmentGradeRow(asn, gradebook, userCanGrade));
    			}
    		} else {
    			asnMap.put(asn, new AssignmentGradeRow(asn, gradebook, true));
    			isAllItemsViewOnly = false;
    		}
    	}
    	
    	assignments = new ArrayList(asnMap.keySet());

    	for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
    		AssignmentGradeRecord asnGr = (AssignmentGradeRecord)iter.next();

    		// Update the AssignmentGradeRow in the map
    		AssignmentGradeRow asnGradeRow = (AssignmentGradeRow)asnMap.get(asnGr.getAssignment());
    		if (asnGradeRow != null) {
    			Assignment asnGrAssignment = asnGr.getAssignment();
    			
    			// if weighted gb and no category for assignment,
    			// it is not counted toward course grade
    			boolean counted = asnGrAssignment.isCounted();
    			if (counted && getWeightingEnabled()) {
    				Category assignCategory = asnGrAssignment.getCategory();
    				if (assignCategory == null)
    					counted = false;
    			}
    			
    			asnGradeRow.setGradeRecord(asnGr);
    			
    			if (asnGr != null) {
    				if (getGradeEntryByPercent())
    					asnGradeRow.setScore(truncateScore(asnGr.getPercentEarned()));
    				else if(getGradeEntryByPoints())
    					asnGradeRow.setScore(truncateScore(asnGr.getPointsEarned())); 
    				else if (getGradeEntryByLetter())
    					asnGradeRow.setLetterScore(asnGr.getLetterEarned());
    			}
    		}
    		
    	}

    	Map goEventListMap = getGradebookManager().getGradingEventsForStudent(studentUid, assignments);
		 // NOTE: we are no longer converting the events b/c we are
         // storing what the user entered, not just points

    	//iterate through the assignments and update the comments and grading events
    	Iterator assignmentIterator = assignments.iterator();
    	while(assignmentIterator.hasNext()){
    		Assignment assignment = (Assignment) assignmentIterator.next();
    		
    		AssignmentGradeRow asnGradeRow = (AssignmentGradeRow)asnMap.get(assignment);
    		
    		// Grading events
    		if (isInstructorView) {
        		List assignEventList = new ArrayList();
        		if (goEventListMap != null) {
        			assignEventList = (List) goEventListMap.get(assignment);
        		}
	    		
	    		if (assignEventList != null && !assignEventList.isEmpty()) {
	    			List eventRows = new ArrayList();
	    			for (Iterator iter = assignEventList.iterator(); iter.hasNext();) {
	    				GradingEvent gradingEvent = (GradingEvent)iter.next();
	    				eventRows.add(new GradingEventRow(gradingEvent));
	    			}
	    			asnGradeRow.setEventRows(eventRows);
	    			asnGradeRow.setEventsLogTitle(getLocalizedString("inst_view_log_title", new String[] {getUserDisplayName()}));
	    		}
    		}
    		
    		// Comments
    		try{
    			Comment comment = (Comment)commentMap.get(asnGradeRow.getAssociatedAssignment().getId());
    			if(comment.getCommentText().length() > 0)
    				asnGradeRow.setCommentText(comment.getCommentText());
    		}catch(NullPointerException npe){
    			if(logger.isDebugEnabled())
    				logger.debug("assignment has no associated comment");
    		}
    	}

    	gradeRows = new ArrayList(asnMap.values());

    	//remove assignments that are not released
    	Iterator i = gradeRows.iterator();
    	while(i.hasNext()){
    		AssignmentGradeRow assignmentGradeRow = (AssignmentGradeRow)i.next();
    		if(!assignmentGradeRow.getAssociatedAssignment().isReleased() && !isInstructorView) 
    			i.remove();
    	}
    	
    	if (!sortColumn.equals(Category.SORT_BY_WEIGHT)) {
	    	Collections.sort(gradeRows, (Comparator)columnSortMap.get(sortColumn));
	    	if(!sortAscending) {
	    		Collections.reverse(gradeRows);
	    	}
    	}
    	
    	return gradeRows;	
    }
    
    /**
     * Sets up the grade/category rows for student
     * @param userUid
     * @param gradebook
     */
    private void initializeStudentGradeData() {
    	
    	// do not retrieve assignments if not displayed for students
    	if (assignmentsReleased || isInstructorView) {
    		
    		//get grade comments and load them into a map assignmentId->comment
    		commentMap = new HashMap();
    		List assignmentComments = getGradebookManager().getStudentAssignmentComments(studentUid, gradebook.getId());
    		logger.debug("number of comments "+assignmentComments.size());
    		Iterator iteration = assignmentComments.iterator();
    		while (iteration.hasNext()){
    			Comment comment = (Comment)iteration.next();
    			commentMap.put(comment.getGradableObject().getId(),comment);
    		}

    		// get the student grade records
    		List gradeRecords = getGradebookManager().getStudentGradeRecordsConverted(gradebook.getId(), studentUid);

    		// The display may include categories and assignments, so we need a generic list
    		gradebookItems = new ArrayList();

    		if (getCategoriesEnabled()) {
    			// we will also have to determine the student's category avg - the category stats
    			// are for class avg
    			List categoryListWithCG = new ArrayList();
    			if (sortColumn.equals(Category.SORT_BY_WEIGHT))
    				categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), Assignment.DEFAULT_SORT, true, sortColumn, sortAscending);
    			else
    				categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), Assignment.DEFAULT_SORT, true, Category.SORT_BY_NAME, true);
  
    			List categoryList = new ArrayList();
    			
    			// first, remove the CourseGrade from the Category list
    			for (Iterator catIter = categoryListWithCG.iterator(); catIter.hasNext();) {
    				Object catOrCourseGrade = catIter.next();
    				if (catOrCourseGrade instanceof Category) {
    					categoryList.add((Category)catOrCourseGrade);
    				} 
    			}
    			
    			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
    				categoryList = getGradebookPermissionService().getCategoriesForUserForStudentView(getGradebookId(), getUserUid(), studentUid, categoryList, getGradebook().getCategory_type(), getViewableSectionIds());
    			}
    			
    			// first, we deal with the categories and their associated assignments
    			if (categoryList != null && !categoryList.isEmpty()) {
    				Iterator catIter = categoryList.iterator();
    				while (catIter.hasNext()) {
    					Object catObject = catIter.next();
    					if (catObject instanceof Category) {
    						Category category = (Category) catObject;
    						gradebookItems.add(category);
    						List catAssign = category.getAssignmentList();
    						if (catAssign != null && !catAssign.isEmpty()) {
    							// we want to create the grade rows for these assignments
    							List gradeRows = retrieveGradeRows(catAssign, gradeRecords);
    							category.calculateStatisticsPerStudent(gradeRecords, studentUid);
    							if (gradeRows != null && !gradeRows.isEmpty()) {
    								gradebookItems.addAll(gradeRows);
    							}
    						}
    					}
    				}
    			}
    			
    			// now we need to grab all of the assignments w/o a category
    			if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions() && !getGradebookPermissionService().getPermissionForUserForAllAssignmentForStudent(getGradebookId(), getUserUid(), studentUid, getViewableSectionIds()))) {
    				// user is not authorized to view/grade the unassigned category for the current student
    			} else {
    				List assignNoCat = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(), Assignment.DEFAULT_SORT, true);
    				if (assignNoCat != null && !assignNoCat.isEmpty()) {
    					Category unassignedCat = new Category();
    					unassignedCat.setGradebook(gradebook);
    					unassignedCat.setName(getLocalizedString("cat_unassigned"));
    					unassignedCat.setAssignmentList(assignNoCat);

    					//add this category to our list
    					gradebookItems.add(unassignedCat);

    					// now create grade rows for the unassigned assignments
    					List gradeRows = retrieveGradeRows(assignNoCat, gradeRecords);
    					/*  we display N/A for the category avg for unassigned category,
    					 * so don't need to calc this anymore
    				 if (!getWeightingEnabled())
    					unassignedCat.calculateStatisticsPerStudent(gradeRecords, studentUid);*/
    					if (gradeRows != null && !gradeRows.isEmpty()) {
    						gradebookItems.addAll(gradeRows);
    					}
    				}
    			}

    		} else {
    			// there are no categories, so we will be returning a list of grade rows
    			List assignList = getGradebookManager().getAssignments(getGradebookId());
    			if (assignList != null && !assignList.isEmpty()) {
    				List gradeRows = retrieveGradeRows(assignList, gradeRecords);
    				if (gradeRows != null && !gradeRows.isEmpty()) {
    					gradebookItems.addAll(gradeRows);
    				}
    			}
    		}

    		for(Iterator iter = gradebookItems.iterator(); iter.hasNext();) {
    			Object gradebookItem = iter.next();
    			if (gradebookItem instanceof AssignmentGradeRow) {
    				AssignmentGradeRow gr = (AssignmentGradeRow)gradebookItem;
    				if(gr.getAssociatedAssignment().isExternallyMaintained()) {
    					rowStyles.append("external");
    					anyExternallyMaintained = true;
    				} else {
    					rowStyles.append("internal");
    				}
    			} 

    			if(iter.hasNext()) {
    				rowStyles.append(",");
    			}
    		}
    	}
    }
}



