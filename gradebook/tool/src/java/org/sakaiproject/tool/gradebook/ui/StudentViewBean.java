/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
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
    private int percent;
    private boolean courseGradeReleased;
    private String courseGrade;
    private boolean assignmentsReleased;
    private boolean anyNotCounted;

    private boolean sortAscending;
    private String sortColumn;

    private StringBuffer rowStyles;

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

        public AssignmentGradeRow(Assignment assignment) {
        	this.assignment = assignment;
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
    }

    /**
     * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
     */
    public void init() {
        // Get the active gradebook
        Gradebook gradebook = getGradebook();
        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();

        // Set the display name
        try {
	        userDisplayName = getUserDirectoryService().getUserDisplayName(getUserUid());
	    } catch (UnknownUserException e) {
	    	logger.error("User " + getUserUid() + " is unknown but logged in as student in gradebook " + gradebook.getUid());
	    	userDisplayName = "";
	    }
        courseGradeReleased = gradebook.isCourseGradeDisplayed();
        assignmentsReleased = gradebook.isAssignmentsDisplayed();

        // Reset the points, percentages, and row styles
        totalPointsEarned = 0;
        totalPointsScored = 0;
        percent = 0;
        rowStyles = new StringBuffer();

        // Display course grade if we've been instructed to.
        if (gradebook.isCourseGradeDisplayed()) {
        	CourseGradeRecord gradeRecord = getGradeManager().getStudentCourseGradeRecord(gradebook, getUserUid());
        	if (gradeRecord != null) {
	        	courseGrade = gradeRecord.getDisplayGrade();
	        }
        }

        // Don't display any assignments if they have not been released
        if(!gradebook.isAssignmentsDisplayed()) {
            assignmentGradeRows = new ArrayList();
        } else {
            List assignments = getGradeManager().getAssignments(gradebook.getId());
            List gradeRecords = getGradeManager().getStudentGradeRecords(gradebook.getId(), getUserUid());

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
				if(asnGr.getPointsEarned() != null) {
					if(logger.isDebugEnabled()) logger.debug("Adding " + asnGr.getPointsEarned() + " to totalPointsEarned");
					totalPointsEarned += asnGr.getPointsEarned().doubleValue();
					if(logger.isDebugEnabled()) logger.debug("Adding " + asnGr.getAssignment().getPointsPossible() + " to totalPointsPossible");
					totalPointsScored += asnGr.getAssignment().getPointsPossible().doubleValue();
				}

				// Put the letter grade into the grade record
				asnGr.setDisplayGrade(gradeMapping.getGrade(asnGr.getGradeAsPercentage()));

				// Update the AssignmentGradeRow in the map
				AssignmentGradeRow asnGradeRow = (AssignmentGradeRow)asnMap.get(asnGr.getAssignment());
				asnGradeRow.setGradeRecord(asnGr);
            }

            assignmentGradeRows = new ArrayList(asnMap.values());

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

            // Protect from division by zero
            if(totalPointsScored != 0) {
                if(logger.isDebugEnabled()) logger.debug("totalPointsEarned / totalPointsScored = " + totalPointsEarned + "/" + totalPointsScored);
                percent = (int)(totalPointsEarned / totalPointsScored * 100);
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
	public int getPercent() {
		return percent;
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

}



