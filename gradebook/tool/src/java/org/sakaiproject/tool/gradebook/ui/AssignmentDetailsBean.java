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
import java.text.SimpleDateFormat;
import java.util.*;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class AssignmentDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(AssignmentDetailsBean.class);

	private List scoreRows;
	private GradeRecordSet scores;

	private Long assignmentId;
    private Assignment assignment;
	private Assignment previousAssignment;
	private Assignment nextAssignment;

    private SimpleDateFormat dateFormat;

	public class ScoreRow implements Serializable {
        private AssignmentGradeRecord gradeRecord;
        private EnrollmentRecord enrollment;
        private StringBuffer eventsString;
        private static final char EVENT_SEP_CHAR = '|';

		public ScoreRow() {
		}
		public ScoreRow(EnrollmentRecord enrollment, AssignmentGradeRecord gradeRecord, List gradingEvents) {
            Collections.sort(gradingEvents);
            this.enrollment = enrollment;
            if(gradeRecord == null) {
                this.gradeRecord = new AssignmentGradeRecord(assignment, enrollment.getUser().getUserUid(), null);
                scores.addGradeRecord(this.gradeRecord);
            } else {
                this.gradeRecord = gradeRecord;
            }

            eventsString = new StringBuffer();
            for(Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
                GradingEvent gradingEvent = (GradingEvent)iter.next();
                String graderName;
                try {
                    graderName = getUserDirectoryService().getUserDisplayName(gradingEvent.getGraderId());
                } catch (UnknownUserException e) {
                    logger.warn("Unable to find user with uid=" + gradingEvent.getGraderId());
                    graderName = gradingEvent.getGraderId();
                }
                eventsString.append(dateFormat.format(gradingEvent.getDateGraded()));
                eventsString.append(EVENT_SEP_CHAR);
                eventsString.append(gradingEvent.getGrade());
                eventsString.append(EVENT_SEP_CHAR);
                eventsString.append(graderName);
                if(iter.hasNext()) {
                    eventsString.append(EVENT_SEP_CHAR);
                }
            }
		}

		public Double getScore() {
			return gradeRecord.getPointsEarned();
		}
		public void setScore(Double score) {
            gradeRecord.setPointsEarned(score);
		}
        public EnrollmentRecord getEnrollment() {
            return enrollment;
        }
        public String getEventsString() {
            return eventsString.toString();
        }
	}

	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("loadData assignment=" + assignment + ", previousAssignment=" + previousAssignment + ", nextAssignment=" + nextAssignment);

		super.init();

        dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm:ss", FacesContext.getCurrentInstance().getExternalContext().getRequestLocale());

        // Clear view state.
        previousAssignment = null;
        nextAssignment = null;
		scoreRows = new ArrayList();

		// We'll need a full enrollment list to filter dropped students out
		// of the statistics.

		if (assignmentId != null) {
			List allEnrollments = getEnrollments();
			Set enrollmentUids = FacadeUtils.getStudentUids(allEnrollments);
			assignment = (Assignment)getGradeManager().getGradableObjectWithStats(assignmentId, enrollmentUids);
			if (assignment != null) {
                scores = new GradeRecordSet(assignment);

                // Get the list of assignments.  If we are sorting by mean, we
                // need to fetch the assignment statistics as well.
				List assignments;
                if(Assignment.SORT_BY_MEAN.equals(getAssignmentSortColumn())) {
                    assignments = getGradeManager().getAssignmentsWithStats(getGradebookId(), enrollmentUids,
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                } else {
                    assignments = getGradeManager().getAssignments(getGradebookId(),
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                }

                // Set up next and previous links, if any.
                int thisIndex = assignments.indexOf(assignment);
				if (thisIndex > 0) {
					previousAssignment = (Assignment)assignments.get(thisIndex - 1);
				}
				if (thisIndex < (assignments.size() - 1)) {
					nextAssignment = (Assignment)assignments.get(thisIndex + 1);
				}

				// Set up score rows.
				// TODO Avoid another DB query by passing all enrollments in.
				Map enrollmentMap = getOrderedEnrollmentMap(allEnrollments);

				List gradeRecords;

				if (isFilteredSearch() || isEnrollmentSort()) {
					gradeRecords = getGradeManager().getPointsEarnedSortedGradeRecords(assignment, enrollmentMap.keySet());
				} else {
					gradeRecords = getGradeManager().getPointsEarnedSortedGradeRecords(assignment);
				}

				List workingEnrollments = new ArrayList(enrollmentMap.values());

				if (!isEnrollmentSort()) {
					// Need to sort and page based on a scores column.
					List scoreSortedEnrollments = new ArrayList();
					for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
						AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
						scoreSortedEnrollments.add(enrollmentMap.get(agr.getStudentId()));
					}

					// Put enrollments with no scores at the beginning of the final list.
					workingEnrollments.removeAll(scoreSortedEnrollments);

					// Add all sorted enrollments with scores into the final list
					workingEnrollments.addAll(scoreSortedEnrollments);

					workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
				}

                // Get all of the grading events for these enrollments on this assignment
                GradingEvents allEvents = getGradeManager().getGradingEvents(assignment, workingEnrollments);

                for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
					AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)iter.next();
					scores.addGradeRecord(gradeRecord);
				}
				for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
					EnrollmentRecord enrollment = (EnrollmentRecord)iter.next();
                    AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)scores.getGradeRecord(enrollment.getUser().getUserUid());
					scoreRows.add(new ScoreRow(enrollment, gradeRecord, allEvents.getEvents(enrollment.getUser().getUserUid())));
				}

			} else {
				// The assignment might have been removed since this link was set up.
				if (logger.isWarnEnabled()) logger.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());
                FacesUtil.addErrorMessage(getLocalizedString("assignment_details_assignment_removed"));
			}
		}
	}

	// Delegated sort methods for read-only assignment sort order
    public String getAssignmentSortColumn() {
        return getPreferencesBean().getAssignmentSortColumn();
    }
    public boolean isAssignmentSortAscending() {
        return getPreferencesBean().isAssignmentSortAscending();
    }

	/**
	 * Action listener to view a different assignment.
	 */
	public void processAssignmentIdChange(ActionEvent event) {
		Map params = FacesUtil.getEventParameterMap(event);
		if (logger.isDebugEnabled()) logger.debug("processAssignmentIdAction params=" + params + ", current assignmentId=" + assignmentId);
		Long idParam = (Long)params.get("assignmentId");
		if (idParam != null) {
			setAssignmentId(idParam);
		}
	}

	/**
	 * Action listener to update scores.
	 */
	public void processUpdateScores(ActionEvent event) {
		try {
			saveScores();
		} catch (StaleObjectModificationException e) {
            FacesUtil.addErrorMessage(getLocalizedString("assignment_details_locking_failure"));
		}
	}

	private void saveScores() throws StaleObjectModificationException {
		if (logger.isInfoEnabled()) logger.info("saveScores " + assignmentId);
		Set excessiveScores = getGradeManager().updateAssignmentGradeRecords(scores);

		String messageKey = (excessiveScores.size() > 0) ?
			"assignment_details_scores_saved_excessive" :
			"assignment_details_scores_saved";

        // Let the user know.
        FacesUtil.addMessage(getLocalizedString(messageKey));
	}

    /**
	 * View maintenance methods.
	 */
	public Long getAssignmentId() {
		if (logger.isDebugEnabled()) logger.debug("getAssignmentId " + assignmentId);
		return assignmentId;
	}
	public void setAssignmentId(Long assignmentId) {
		if (logger.isDebugEnabled()) logger.debug("setAssignmentId " + assignmentId);
		this.assignmentId = assignmentId;
	}

	public boolean isFirst() {
		return (previousAssignment == null);
	}
	public String getPreviousTitle() {
		return (previousAssignment != null) ? previousAssignment.getName() : "";
	}

	public boolean isLast() {
		return (nextAssignment == null);
	}
	public String getNextTitle() {
		return (nextAssignment != null) ? nextAssignment.getName() : "";
	}

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

    public boolean isEmptyEnrollments() {
        return emptyEnrollments;
    }

    // A desparate stab at reasonable embedded validation message formatting.
    // If the score column is an input box, it may have a wide message associated
    // with it, and we want the input field left-aligned to match up with
    // the non-erroroneous input fields (even though the actual input values
    // will be right-aligned). On the other hand, if the score column is read-only,
    // then we want to simply right-align the table column.
    public String getScoreColumnAlignment() {
    	if (assignment.isExternallyMaintained()) {
    		return "right";
    	} else {
    		return "left";
    	}
    }

    // Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isAssignmentDetailsTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setAssignmentDetailsTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getAssignmentDetailsTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setAssignmentDetailsTableSortColumn(sortColumn);
    }
    public Assignment getAssignment() {
        return assignment;
    }
    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
    public Assignment getNextAssignment() {
        return nextAssignment;
    }
    public void setNextAssignment(Assignment nextAssignment) {
        this.nextAssignment = nextAssignment;
    }
    public Assignment getPreviousAssignment() {
        return previousAssignment;
    }
    public void setPreviousAssignment(Assignment previousAssignment) {
        this.previousAssignment = previousAssignment;
    }
    public GradeRecordSet getScores() {
        return scores;
    }
}



