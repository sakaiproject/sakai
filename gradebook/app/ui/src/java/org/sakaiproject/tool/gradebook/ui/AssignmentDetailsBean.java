/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation, the MIT Corporation
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
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class AssignmentDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(AssignmentDetailsBean.class);

	/**
	 * The following variable keeps bean initialization from overwriting
	 * input fields from the database.
	 */
	private boolean workInProgress;

	private List scoreRows;
	private List updatedGradeRecords;
	private List updatedComments;

	private Long assignmentId;
    private Assignment assignment;
	private Assignment previousAssignment;
	private Assignment nextAssignment;
	
	private String assignmentCategory;
	private String assignmentWeight;

	private boolean isAllCommentsEditable;
	private boolean isAllStudentsViewOnly = true;  // with grader perms, user may be able to grade/comment a selection
													// of the students and view the rest. If all view only, disable
													// the buttons

    public class ScoreRow implements Serializable {
        private AssignmentGradeRecord gradeRecord;
        private EnrollmentRecord enrollment;
        private Comment comment;
        private List eventRows;
        private boolean userCanGrade;
 
		public ScoreRow() {
		}
		public ScoreRow(EnrollmentRecord enrollment, AssignmentGradeRecord gradeRecord, Comment comment, List gradingEvents, boolean userCanGrade) {
            Collections.sort(gradingEvents);
            this.enrollment = enrollment;
            this.gradeRecord = gradeRecord;
            this.comment = comment;
            this.userCanGrade = userCanGrade;
 
            eventRows = new ArrayList();
            for (Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	GradingEvent gradingEvent = (GradingEvent)iter.next();
            	eventRows.add(new GradingEventRow(gradingEvent));
            }
		}

		public Double getScore() {
			if (getGradeEntryByPercent())
				return truncateScore(gradeRecord.getPercentEarned());
			else
				return truncateScore(gradeRecord.getPointsEarned());
		}
		public void setScore(Double score) {
			if (getGradeEntryByPoints()) {
				Double originalScore = gradeRecord.getPointsEarned();
				if (originalScore != null) {
					// truncate to two decimals for more accurate comparison
					originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
				}
				if ( (originalScore != null && !originalScore.equals(score)) ||
						(originalScore == null && score != null) ) {
					gradeRecord.setPointsEarned(score);
					updatedGradeRecords.add(gradeRecord);
				}
			} else if (getGradeEntryByPercent()) {
				Double originalScore = gradeRecord.getPercentEarned();
				if (originalScore != null) {
					// truncate to two decimals for more accurate comparison
					originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
				}
				if ( (originalScore != null && !originalScore.equals(score)) ||
						(originalScore == null && score != null) ) {
					gradeRecord.setPercentEarned(score);
					updatedGradeRecords.add(gradeRecord);
				}
			}
		}
		
		public String getLetterScore() {
			return gradeRecord.getLetterEarned();
		}
		
		public void setLetterScore(String letterScore) {
			if (letterScore != null)
				letterScore = letterScore.trim();
			String originalLetterScore = gradeRecord.getLetterEarned();
			if ((originalLetterScore != null && !originalLetterScore.equals(letterScore)) ||
					(originalLetterScore == null && letterScore != null)) {
				gradeRecord.setLetterEarned(letterScore);
				updatedGradeRecords.add(gradeRecord);
			}
		}

        public EnrollmentRecord getEnrollment() {
            return enrollment;
        }

        public String getCommentText() {
        	return comment.getCommentText();
        }
        public void setCommentText(String commentText) {
        	if (!StringUtils.stripToEmpty(commentText).equals(StringUtils.stripToEmpty(comment.getCommentText()))) {
        		comment.setCommentText(commentText);
        		updatedComments.add(comment);
        	}
        }

        public List getEventRows() {
        	return eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("assignment_details_log_title", new String[] {enrollment.getUser().getDisplayName()});
        }

        public boolean isCommentEditable() {
        	return (isAllCommentsEditable && !assignment.isExternallyMaintained() && userCanGrade);
        }
        
        public boolean isUserCanGrade() {
        	return userCanGrade;
        }
        public void setUserCanGrade(boolean userCanGrade) {
        	this.userCanGrade = userCanGrade;
        }
	}

	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("loadData assignment=" + assignment + ", previousAssignment=" + previousAssignment + ", nextAssignment=" + nextAssignment);
		if (logger.isDebugEnabled()) logger.debug("isNotValidated()=" + isNotValidated());
		if (logger.isDebugEnabled()) logger.debug("workInProgress=" + workInProgress);
		if (workInProgress) {			
			// Keeping the current form values in memory is a one-shot deal at
			// present. The next time the user does anything, the form will be
			// refreshed from the database.
			workInProgress = false;
			ToolSession session = SessionManager.getCurrentToolSession();
			final String fromPage = (String) session.getAttribute("fromPage");
			if (fromPage != null) {
				setBreadcrumbPage(fromPage);
			}

			return;
		}
		
		// set the filter value for this page
		super.setSelectedSectionFilterValue(this.getSelectedSectionFilterValue());
		super.init();

		ToolSession session = SessionManager.getCurrentToolSession();
		final String fromPage = (String) session.getAttribute("breadcrumbPage");
		if (fromPage != null) {
			setBreadcrumbPage(fromPage);
		}

        // Clear view state.
        previousAssignment = null;
        nextAssignment = null;
		scoreRows = new ArrayList();
		updatedComments = new ArrayList();
		updatedGradeRecords = new ArrayList();
		isAllStudentsViewOnly = true;

		if (assignmentId != null) {
			assignment = getGradebookManager().getAssignmentWithStats(assignmentId);
			if (assignment != null) {
                // Get the list of assignments.  If we are sorting by mean, we
                // need to fetch the assignment statistics as well. If categories
				// are enabled, we need to retrieve the categories and extract the assignments
				// b/c the assignments will be grouped by category
				List assignments;
                if(!getCategoriesEnabled() && Assignment.SORT_BY_MEAN.equals(getAssignmentSortColumn())) {
                    assignments = getGradebookManager().getAssignmentsWithStats(getGradebookId(),
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                } else if (!getCategoriesEnabled()){
                    assignments = getGradebookManager().getAssignments(getGradebookId(),
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                } else {
                	// Categories are enabled, so the assignments are grouped by category
                	assignments = new ArrayList();
                	
                	List categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending(), getCategorySortColumn(), isCategorySortAscending());
        			List categoryList = new ArrayList();
        			
        			// first, remove the CourseGrade from the Category list
        			for (Iterator catIter = categoryListWithCG.iterator(); catIter.hasNext();) {
        				Object catOrCourseGrade = catIter.next();
        				if (catOrCourseGrade instanceof Category) {
        					categoryList.add((Category)catOrCourseGrade);
        				} 
        			}
        			
        			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
        				categoryList = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), categoryList, getGradebook().getCategory_type());
        			}
                	
        			if (categoryList != null) {
                		Iterator catIter = categoryList.iterator();
                		while (catIter.hasNext()) {
            				Category myCat = (Category) catIter.next();
            				List catAssigns = myCat.getAssignmentList();
            				if (catAssigns != null) {
            					assignments.addAll(catAssigns);
            				}
            			}
                	}
                	// we also need to retrieve all of the assignments that have not
                	// yet been assigned a category
        			if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions() && !getGradebookPermissionService().getPermissionForUserForAllAssignment(getGradebookId(), getUserUid()))) {
        				// is not authorized to view the "Unassigned" Category
        			} else {
	                	List assignNoCategory = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending());
	                	if (assignNoCategory != null) {
	                		assignments.addAll(assignNoCategory);
	                	}
        			}
                }

                // Set up next and previous links, if any.
                int thisIndex = assignments.indexOf(assignment);
				if (thisIndex > 0) {
					previousAssignment = (Assignment)assignments.get(thisIndex - 1);
				}
				if (thisIndex < (assignments.size() - 1)) {
					nextAssignment = (Assignment)assignments.get(thisIndex + 1);
				}

				Category category = assignment.getCategory();
				Long categoryId = null;
				if (category != null)
					categoryId = category.getId();
				
				// Set up score rows.
				Map enrollmentMap = getOrderedEnrollmentMapForItem(categoryId);
				
				List studentUids = new ArrayList(enrollmentMap.keySet());
				List gradeRecords = new ArrayList();
				if (getGradeEntryByPoints())
					gradeRecords = getGradebookManager().getAssignmentGradeRecords(assignment, studentUids);
				else 
					gradeRecords = getGradebookManager().getAssignmentGradeRecordsConverted(assignment, studentUids);
				
				if (!isEnrollmentSort()) {
					// Need to sort and page based on a scores column.
					List scoreSortedStudentUids = new ArrayList();
					for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
						AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
						scoreSortedStudentUids.add(agr.getStudentId());
					}

					// Put enrollments with no scores at the beginning of the final list.
					studentUids.removeAll(scoreSortedStudentUids);

					// Add all sorted enrollments with scores into the final list
					studentUids.addAll(scoreSortedStudentUids);

					studentUids = finalizeSortingAndPaging(studentUids);
				}

                // Get all of the grading events for these enrollments on this assignment
                GradingEvents allEvents = getGradebookManager().getGradingEvents(assignment, studentUids);
                // NOTE: we are no longer converting the events b/c we are
                // storing what the user entered, not just points
                //getGradebookManager().convertGradingEventsConverted(assignment, allEvents, studentUids, getGradebook().getGrade_type());
                
                Map gradeRecordMap = new HashMap();
                for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
					AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)iter.next();
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

                // Get all of the comments for these enrollments on this assignment.
                List comments = getGradebookManager().getComments(assignment, studentUids);
                Map commentMap = new HashMap();
                for (Iterator iter = comments.iterator(); iter.hasNext(); ) {
					Comment comment = (Comment)iter.next();
					commentMap.put(comment.getStudentId(), comment);
				}

				for (Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
					String studentUid = (String)iter.next();
					Map enrFunctionMap = (Map) enrollmentMap.get(studentUid);
					List enrRecList = new ArrayList(enrFunctionMap.keySet());
					EnrollmentRecord enrollment = (EnrollmentRecord)enrRecList.get(0); // there is only one rec in this map
					
					AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)gradeRecordMap.get(studentUid);
		            if(gradeRecord == null) {
		                gradeRecord = new AssignmentGradeRecord(assignment, studentUid, null);
		                gradeRecords.add(gradeRecord);
		            }

		            Comment comment = (Comment)commentMap.get(studentUid);
		            if (comment == null) {
		            	comment = new Comment(studentUid, null, assignment);
		            }
		            
		            boolean userCanGrade = false;
		            String itemFunction = (String)enrFunctionMap.get(enrollment);
		            if (itemFunction != null && itemFunction.equalsIgnoreCase(GradebookService.gradePermission))
		            	userCanGrade = true;

					scoreRows.add(new ScoreRow(enrollment, gradeRecord, comment, allEvents.getEvents(studentUid), userCanGrade));
					if (userCanGrade)
						isAllStudentsViewOnly = false;
				}
				
				if (getCategoriesEnabled()) {
					if (assignment.getCategory() != null) {
						if (getWeightingEnabled()) {
							Double weight = assignment.getCategory().getWeight();
							if (weight != null && weight.doubleValue() > 0)
								weight = new Double(weight.doubleValue() * 100);
							if (weight == null)
								throw new IllegalStateException(
										"Double weight == null!");
							assignmentWeight = weight.toString();
							assignmentCategory = assignment.getCategory().getName() + " " + getLocalizedString("cat_weight_display", new String[] {assignmentWeight});
						} else {
							assignmentCategory = assignment.getCategory().getName();
						}
					}
					else {
						assignmentCategory = getLocalizedString("assignment_details_assign_category");
					}
				}

			} else {
				// The assignment might have been removed since this link was set up.
				if (logger.isWarnEnabled()) logger.warn("No assignmentId=" + assignmentId + " in gradebookUid " + getGradebookUid());
                FacesUtil.addErrorMessage(getLocalizedString("assignment_details_assignment_removed"));
			}
		}
	}

	// Delegated sort methods for read-only assignment & category sort order
    public String getAssignmentSortColumn() {
        return getPreferencesBean().getAssignmentSortColumn();
    }
    public boolean isAssignmentSortAscending() {
        return getPreferencesBean().isAssignmentSortAscending();
    }
    public String getCategorySortColumn() {
        return getPreferencesBean().getCategorySortColumn();
    }
    public boolean isCategorySortAscending() {
        return getPreferencesBean().isCategorySortAscending();
    }
    
    //  Filtering
    public Integer getSelectedSectionFilterValue() {
        return getPreferencesBean().getAssignmentDetailsTableSectionFilter();
    }
    public void setSelectedSectionFilterValue(Integer assignmentDetailsTableSectionFilter) {
        getPreferencesBean().setAssignmentDetailsTableSectionFilter(assignmentDetailsTableSectionFilter);
        super.setSelectedSectionFilterValue(assignmentDetailsTableSectionFilter);
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
		
        Set excessiveScores = getGradebookManager().updateAssignmentGradesAndComments(assignment, updatedGradeRecords, updatedComments);

        if (logger.isDebugEnabled()) logger.debug("About to save " + updatedComments.size() + " updated comments");
        if(updatedGradeRecords.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.updateItemScores","/gradebook/"+getGradebookId()+"/"+updatedGradeRecords.size()+"/"+getAuthzLevel());
        }
        if(updatedComments.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.comment","/gradebook/"+getGradebookId()+"/"+updatedComments.size()+"/"+getAuthzLevel());
        }
        
        String messageKey = null;
        if (updatedGradeRecords.size() > 0) {
        	if (excessiveScores.size() > 0) {
        		messageKey = "assignment_details_scores_saved_excessive";
        	} else if (updatedComments.size() > 0) {
        		messageKey = "assignment_details_scores_comments_saved";
        	} else {
        		messageKey = "assignment_details_scores_saved";
        	}
        } else if (updatedComments.size() > 0) {
        	messageKey = "assignment_details_comments_saved";
        }
        
        // Let the user know.
        if (messageKey != null) {
        	FacesUtil.addMessage(getLocalizedString(messageKey));
        }
    }

    public void toggleEditableComments(ActionEvent event) {
        // Don't write over any scores the user entered before pressing
        // the "Edit Comments" button.
        if (!isAllCommentsEditable) {
            workInProgress = true;
        }

        isAllCommentsEditable = !isAllCommentsEditable;
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

	/**
	 * In IE (but not Mozilla/Firefox) empty request parameters may be returned
	 * to JSF as the string "null". JSF always "restores" some idea of the
	 * last view, even if that idea is always going to be null because a redirect
	 * has occurred. Put these two things together, and you end up with
	 * a class cast exception when redirecting from this request-scoped
	 * bean to a static page.
	 */
	public void setAssignmentIdParam(String assignmentIdParam) {
		if (logger.isDebugEnabled()) logger.debug("setAssignmentIdParam String " + assignmentIdParam);
		if ((assignmentIdParam != null) && (assignmentIdParam.length() > 0) &&
			!assignmentIdParam.equals("null")) {
			try {
				setAssignmentId(Long.valueOf(assignmentIdParam));
			} catch(NumberFormatException e) {
				if (logger.isWarnEnabled()) logger.warn("AssignmentId param set to non-number '" + assignmentIdParam + "'");
			}
		}
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

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("assignment_details_log_type");
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
	public String getAssignmentCategory() {
		return assignmentCategory;
	}
	public void setAssignmentCategory(String assignmentCategory) {
		this.assignmentCategory = assignmentCategory;
	}
	public String getAssignmentWeight() {
		return assignmentWeight;
	}
	public void setAssignmentWeight(String assignmentWeight) {
		this.assignmentWeight = assignmentWeight;
	}

    public String getCommentsToggle() {
    	String messageKey = isAllCommentsEditable ?
    			"assignment_details_comments_read" :
				"assignment_details_comments_edit";
    	return getLocalizedString(messageKey);
    }

	public boolean isAllCommentsEditable() {
		return isAllCommentsEditable;
	}
	public boolean isAllStudentsViewOnly() {
		return isAllStudentsViewOnly;
	}

	/**
	 * Go to instructor view. State saved in tool
	 * session so need this method.
	 */
	public String navigateToInstructorView() {
		setNav(null,"","","true",null);
		
		return "instructorView";
	}
}
