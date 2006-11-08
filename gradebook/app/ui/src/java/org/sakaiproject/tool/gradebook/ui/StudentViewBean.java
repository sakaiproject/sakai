/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.*;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Provides data for the student view of the gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class StudentViewBean extends GradebookDependentBean implements Serializable {
	private static Log logger = LogFactory.getLog(StudentViewBean.class);

    // View maintenance fields - serializable.
    private String userDisplayName;
    private double totalPointsEarned;
    private double totalPointsScored;
    private double percent;
    private boolean courseGradeReleased;
    private String courseGrade;
    private String cumulativeCourseGrade;
    private boolean assignmentsReleased;
    private boolean anyNotCounted;

    private boolean sortAscending;
    private String sortColumn;

    private StringBuffer rowStyles;
    private Map commentMap;


    // Controller fields - transient.
    private transient List assignmentGradeRows;

    private static final Map columnSortMap;
    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_DATE = "dueDate";
    private static final String SORT_BY_POINTS_POSSIBLE = "pointsPossible";
    private static final String SORT_BY_POINTS_EARNED = "pointsEarned";
    private static final String SORT_BY_GRADE = "grade";
    public static Comparator nameComparator;
    public static Comparator dateComparator;
    public static Comparator pointsPossibleComparator;
    public static Comparator pointsEarnedComparator;
    public static Comparator gradeAsPercentageComparator;
    private static Comparator doubleOrNothingComparator;
    static {
        nameComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.nameComparator.compare(((AssignmentGradeRow)o1).getAssignment(), ((AssignmentGradeRow)o2).getAssignment());
            }
        };
        dateComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.dateComparator.compare(((AssignmentGradeRow)o1).getAssignment(), ((AssignmentGradeRow)o2).getAssignment());
            }
        };
        pointsPossibleComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return Assignment.pointsComparator.compare(((AssignmentGradeRow)o1).getAssignment(), ((AssignmentGradeRow)o2).getAssignment());
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

        columnSortMap = new HashMap();
        columnSortMap.put(SORT_BY_NAME, StudentViewBean.nameComparator);
        columnSortMap.put(SORT_BY_DATE, StudentViewBean.dateComparator);
        columnSortMap.put(SORT_BY_POINTS_POSSIBLE, StudentViewBean.pointsPossibleComparator);
        columnSortMap.put(SORT_BY_POINTS_EARNED, StudentViewBean.pointsEarnedComparator);
        columnSortMap.put(SORT_BY_GRADE, StudentViewBean.gradeAsPercentageComparator);
    }

    /**
     * Since this bean does not use the session-scoped preferences bean to keep
     * sort preferences, we need to define the defaults locally.
     */
    public StudentViewBean() {
        sortAscending = true;
        sortColumn = SORT_BY_DATE;
    }

    public class AssignmentGradeRow implements Serializable {

        private Assignment assignment;
        private AssignmentGradeRecord gradeRecord;
        private List comments;



        public AssignmentGradeRow(Assignment assignment) {
        	this.assignment = assignment;
            this.comments = new ArrayList();
        }
        public void setGradeRecord(AssignmentGradeRecord gradeRecord) {
        	this.gradeRecord = gradeRecord;
        }
        public Assignment getAssignment() {
        	return assignment;
        }
        public AssignmentGradeRecord getGradeRecord() {
        	return gradeRecord;
        }
        public String getDisplayGrade() {
        	if (gradeRecord == null) {
        		return FacesUtil.getLocalizedString("score_null_placeholder");
        	} else {
        		return gradeRecord.getDisplayGrade();
        	}
        }

        Double getPointsEarned() {
        	if (gradeRecord == null) {
        		return null;
        	} else {
        		return gradeRecord.getPointsEarned();
        	}
        }
        Double getGradeAsPercentage() {
        	if (gradeRecord == null) {
        		return null;
        	} else {
        		return gradeRecord.getGradeAsPercentage();
        	}
        }

        public List getComments() {
            return comments;
        }

        public void setComments(List comments) {
            this.comments = comments;
        }

    }

    /**
     * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
     */
    public void init() {
        // Get the active gradebook
        Gradebook gradebook = getGradebook();

        // Set the display name
        try {
            userDisplayName = getUserDirectoryService().getUserDisplayName(getUserUid());
        } catch (UnknownUserException e) {
            if(logger.isErrorEnabled())logger.error("User " + getUserUid() + " is unknown but logged in as student in gradebook " + gradebook.getUid());
            userDisplayName = "";
        }
        courseGradeReleased = gradebook.isCourseGradeDisplayed();
        assignmentsReleased = gradebook.isAssignmentsDisplayed();

        // Reset the points, percentages, and row styles
        totalPointsEarned = 0;
        totalPointsScored = 0;
        //percent = 0;
        rowStyles = new StringBuffer();

        // Display course grade if we've been instructed to.
        if (gradebook.isCourseGradeDisplayed()) {
            CourseGradeRecord gradeRecord = getGradebookManager().getStudentCourseGradeRecord(gradebook, getUserUid());
            if (gradeRecord != null) {
                courseGrade = gradeRecord.getDisplayGrade();
                percent = gradeRecord.getSortGrade().doubleValue();
            }
        }
        //get grade comments and load them into a map assignmentId->comment
        commentMap = new HashMap();
        List assignmentComments = getGradebookManager().getStudentAssignmentComments(getUserUid(),getGradebookId());
        logger.debug("number of comments "+assignmentComments.size());
        Iterator iteration = assignmentComments.iterator();
        while (iteration.hasNext()){
            Comment comment = (Comment)iteration.next();
            commentMap.put(comment.getGradableObject().getId(),comment);
        }

        // Don't display any assignments if they have not been released
        if(!gradebook.isAssignmentsDisplayed()) {
            assignmentGradeRows = new ArrayList();
        } else {
            List assignments = getGradebookManager().getAssignments(gradebook.getId());
            if(logger.isDebugEnabled())logger.debug(assignments.size() + " total assignments");
            List gradeRecords = getGradebookManager().getStudentGradeRecords(gradebook.getId(), getUserUid());
            if(logger.isDebugEnabled())logger.debug(gradeRecords.size()  +"  grade records");

            // Create a map of assignments to assignment grade rows
            Map asnMap = new HashMap();
            for(Iterator iter = assignments.iterator(); iter.hasNext();) {

                Assignment asn = (Assignment)iter.next();
                asnMap.put(asn, new AssignmentGradeRow(asn));

                if (asn.isNotCounted()) {
                    anyNotCounted = true;
                }

            }

            for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
                AssignmentGradeRecord asnGr = (AssignmentGradeRecord)iter.next();
                if(logger.isDebugEnabled()) logger.debug("Adding " + asnGr.getPointsEarned() + " to totalPointsEarned");
                if(asnGr.getPointsEarned()!=null && asnGr.getAssignment().isCounted()){
                    totalPointsEarned += asnGr.getPointsEarned().doubleValue();
                }
                // Update the AssignmentGradeRow in the map
                AssignmentGradeRow asnGradeRow = (AssignmentGradeRow)asnMap.get(asnGr.getAssignment());
                asnGradeRow.setGradeRecord(asnGr);
                //update the coments
                try{
                    Comment comment = (Comment)commentMap.get(asnGradeRow.getAssignment().getId());
                    if(comment.getCommentText().length() > 0)asnGradeRow.getComments().add(comment);
                }catch(NullPointerException npe){
                    if(logger.isDebugEnabled())logger.debug("assignment has no associated comment");
                }
            }


            if(logger.isDebugEnabled())logger.debug("calculating total points scored from " +assignments.size() + "assignments");
            for(Iterator it = assignments.iterator(); it.hasNext();){
                Assignment assignment  = (Assignment)it.next();
                if(assignment.isCounted()){
                    totalPointsScored = totalPointsScored +assignment.getPointsPossible().doubleValue();
                }
                if(logger.isDebugEnabled()) logger.debug("total points scored " + totalPointsScored);
            }

            if(logger.isDebugEnabled())logger.debug("total points scored is " +totalPointsScored);



            assignmentGradeRows = new ArrayList(asnMap.values());

            //remove assignments that are not released
            Iterator i = assignmentGradeRows.iterator();
            while(i.hasNext()){
                AssignmentGradeRow assignmentGradeRow = (AssignmentGradeRow)i.next();
                if(!(assignmentGradeRow.getAssignment().isReleased())) i.remove();
            }

            Collections.sort(assignmentGradeRows, (Comparator)columnSortMap.get(sortColumn));
            if(!sortAscending) {
                Collections.reverse(assignmentGradeRows);
            }

            // Set the row css classes
            for(Iterator iter = assignmentGradeRows.iterator(); iter.hasNext();) {
                AssignmentGradeRow gr = (AssignmentGradeRow)iter.next();
                if(gr.getAssignment().isExternallyMaintained()) {
                    rowStyles.append("external");
                } else {
                    rowStyles.append("internal");
                }
                if(iter.hasNext()) {
                    rowStyles.append(",");
                }
            }


        }

    }

    /**
     * @return Returns the assignmentGradeRows.
     */
    public List getAssignmentGradeRows() {
        return assignmentGradeRows;
    }
    /**
     * @return Returns the courseGrade.
     */
    public String getCourseGrade() {
        return courseGrade;
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
     * @return Returns the percent.
     */
    public double getPercent() {
        double pct = 0;
        BigDecimal bd = new BigDecimal(percent);
        bd = bd.setScale(2,BigDecimal.ROUND_DOWN);
        pct = bd.doubleValue();
        return pct;
    }
    /**
     * @return Returns the totalPointsEarned.
     */
    public double getTotalPointsEarned() {
        return totalPointsEarned;
    }
    /**
     * @return Returns the totalPointsScored.
     */
    public double getTotalPointsScored() {
        return totalPointsScored;
    }
    /**
     * @return Returns the userDisplayName.
     */
    public String getUserDisplayName() {
        return userDisplayName;
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

    /**
     * @return True if the gradebook contains any assignments not counted toward
     *         the final course grade.
     */
    public boolean isAnyNotCounted() {
        return anyNotCounted;
    }


    public String getCumulativeCourseGrade() {
        return cumulativeCourseGrade;
    }

    public void setCumulativeCourseGrade(String cumulativeCourseGrade) {

        this.cumulativeCourseGrade = cumulativeCourseGrade;
    }

}



