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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentDetailsBean extends EnrollmentTableBean {
	/**
	 * The following variable keeps bean initialization from overwriting
	 * input fields from the database.
	 */
	private boolean workInProgress;

	private List scoreRows;
	private List<AssignmentGradeRecord> updatedGradeRecords;
	private List updatedComments;

	private Long assignmentId;
    private GradebookAssignment assignment;
	private GradebookAssignment previousAssignment;
	private GradebookAssignment nextAssignment;

	private String assignmentCategory;
	private String assignmentWeight;

	private boolean isAllCommentsEditable;
	private boolean isAllStudentsViewOnly = true;  // with grader perms, user may be able to grade/comment a selection
													// of the students and view the rest. If all view only, disable
													// the buttons
	private ScoringAgentData scoringAgentData;

    public class ScoreRow implements Serializable {
        private AssignmentGradeRecord gradeRecord;
        private EnrollmentRecord enrollment;
        private Comment comment;
        private List eventRows;
        private boolean userCanGrade;
        // if a ScoringComponent is associated with this assignment, this
        // is the URL for scoring this student
        private String scoringComponentUrl;
        // this url can be called to retrieve grade info for this student
        // from the external scoring service
        private String retrieveScoreUrl;

		public ScoreRow() {
		}
		public ScoreRow(final EnrollmentRecord enrollment, final AssignmentGradeRecord gradeRecord, final Comment comment, final List gradingEvents, final boolean userCanGrade) {
            Collections.sort(gradingEvents);
            this.enrollment = enrollment;
            this.gradeRecord = gradeRecord;
            this.comment = comment;
            this.userCanGrade = userCanGrade;

            this.eventRows = new ArrayList();
            for (final Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	final GradingEvent gradingEvent = (GradingEvent)iter.next();
            	this.eventRows.add(new GradingEventRow(gradingEvent));
            }

            if (isScoringAgentEnabled()) {
            	// show the gradable vs. the view-only version of the scoring component
            	if (userCanGrade && !AssignmentDetailsBean.this.assignment.isExternallyMaintained()) {
            		this.scoringComponentUrl = getScoringAgentManager().
            				getScoreStudentUrl(getGradebookUid(), AssignmentDetailsBean.this.assignmentId, enrollment.getUser().getUserUid());
            		this.retrieveScoreUrl = getScoringAgentManager().
                			getScoreUrl(getGradebookUid(), AssignmentDetailsBean.this.assignmentId, enrollment.getUser().getUserUid());
            	} else {
            		this.scoringComponentUrl = getScoringAgentManager().
            				getViewStudentScoreUrl(getGradebookUid(), AssignmentDetailsBean.this.assignmentId, enrollment.getUser().getUserUid());
            	}
            }
		}

        public void setDroppedFromGrade(final Boolean droppedFromGrade) {
            this.gradeRecord.setDroppedFromGrade(droppedFromGrade);
        }

        public Boolean getDroppedFromGrade() {
            return this.gradeRecord.getDroppedFromGrade();
        }

		public Double getScore() {
			if (getGradeEntryByPercent()) {
				return truncateScore(this.gradeRecord.getPercentEarned());
			} else {
				return truncateScore(this.gradeRecord.getPointsEarned());
			}
		}
		public void setScore(final Double score) {
			if (getGradeEntryByPoints()) {
				Double originalScore = this.gradeRecord.getPointsEarned();
				if (originalScore != null) {
					// truncate to two decimals for more accurate comparison
					originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
				}
				if ( (originalScore != null && !originalScore.equals(score)) ||
						(originalScore == null && score != null) ) {
					this.gradeRecord.setPointsEarned(score);
					AssignmentDetailsBean.this.updatedGradeRecords.add(this.gradeRecord);
				}
			} else if (getGradeEntryByPercent()) {
				Double originalScore = this.gradeRecord.getPercentEarned();
				if (originalScore != null) {
					// truncate to two decimals for more accurate comparison
					originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
				}
				if ( (originalScore != null && !originalScore.equals(score)) ||
						(originalScore == null && score != null) ) {
					this.gradeRecord.setPercentEarned(score);
					AssignmentDetailsBean.this.updatedGradeRecords.add(this.gradeRecord);
				}
			}
		}

		public String getLetterScore() {
			return this.gradeRecord.getLetterEarned();
		}

		public void setLetterScore(String letterScore) {
			if (letterScore != null) {
				letterScore = letterScore.trim();
			}
			final String originalLetterScore = this.gradeRecord.getLetterEarned();
			if ((originalLetterScore != null && !originalLetterScore.equals(letterScore)) ||
					(originalLetterScore == null && letterScore != null)) {
				this.gradeRecord.setLetterEarned(letterScore);
				AssignmentDetailsBean.this.updatedGradeRecords.add(this.gradeRecord);
			}
		}

        public EnrollmentRecord getEnrollment() {
            return this.enrollment;
        }

        public String getCommentText() {
        	return this.comment.getCommentText();
        }
        public void setCommentText(final String commentText) {
        	if (!StringUtils.stripToEmpty(commentText).equals(StringUtils.stripToEmpty(this.comment.getCommentText()))) {
        		this.comment.setCommentText(commentText);
        		AssignmentDetailsBean.this.updatedComments.add(this.comment);
        	}
        }

        public List getEventRows() {
        	return this.eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("assignment_details_log_title", new String[] {this.enrollment.getUser().getDisplayName()});
        }

        public boolean isCommentEditable() {
        	return (AssignmentDetailsBean.this.isAllCommentsEditable && !AssignmentDetailsBean.this.assignment.isExternallyMaintained() && this.userCanGrade);
        }

        public boolean isUserCanGrade() {
        	return this.userCanGrade;
        }
        public void setUserCanGrade(final boolean userCanGrade) {
        	this.userCanGrade = userCanGrade;
        }

        /**
         *
         * @return the URL to launch the ScoringComponent for grading this student
         * via a ScoringAgent, if it exists
         */
        public String getScoringComponentUrl() {
        	return this.scoringComponentUrl;
        }

        /**
         *
         * @return the URL for retrieving this student's grade from the
         * external scoring agent
         */
        public String getRetrieveScoreUrl() {
        	return this.retrieveScoreUrl;
        }
	}

    public boolean isAssignmentCategoryDropsScores() {
        final Category category = this.assignment.getCategory();
        if(category != null) {
            final boolean dropScores = category.isDropScores();
            return dropScores;
        } else {
            return false;
        }
    }


	@Override
	protected void init() {
		if (log.isDebugEnabled()) {
			log.debug("loadData assignment=" + this.assignment + ", previousAssignment=" + this.previousAssignment + ", nextAssignment=" + this.nextAssignment);
		}
		if (log.isDebugEnabled()) {
			log.debug("isNotValidated()=" + isNotValidated());
		}
		if (log.isDebugEnabled()) {
			log.debug("workInProgress=" + this.workInProgress);
		}
		if (this.workInProgress) {
			// Keeping the current form values in memory is a one-shot deal at
			// present. The next time the user does anything, the form will be
			// refreshed from the database.
			this.workInProgress = false;
			final ToolSession session = SessionManager.getCurrentToolSession();
			final String fromPage = (String) session.getAttribute("fromPage");
			if (fromPage != null) {
				setBreadcrumbPage(fromPage);
			}

			return;
		}

		// set the filter value for this page
		super.setSelectedSectionFilterValue(getSelectedSectionFilterValue());
		super.init();

		final ToolSession session = SessionManager.getCurrentToolSession();
		final String fromPage = (String) session.getAttribute("breadcrumbPage");
		if (fromPage != null) {
			setBreadcrumbPage(fromPage);
		}

        // Clear view state.
        this.previousAssignment = null;
        this.nextAssignment = null;
		this.scoreRows = new ArrayList();
		this.updatedComments = new ArrayList();
		this.updatedGradeRecords = new ArrayList();
		this.isAllStudentsViewOnly = true;

		if (this.assignmentId != null) {
			this.assignment = getGradebookManager().getAssignmentWithStats(this.assignmentId);
			if (this.assignment != null) {
                // Get the list of assignments.  If we are sorting by mean, we
                // need to fetch the assignment statistics as well. If categories
				// are enabled, we need to retrieve the categories and extract the assignments
				// b/c the assignments will be grouped by category
				List assignments;
                if(!getCategoriesEnabled() && GradebookAssignment.SORT_BY_MEAN.equals(getAssignmentSortColumn())) {
                    assignments = getGradebookManager().getAssignmentsWithStats(getGradebookId(),
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                } else if (!getCategoriesEnabled()){
                    assignments = getGradebookManager().getAssignments(getGradebookId(),
                            getAssignmentSortColumn(), isAssignmentSortAscending());
                } else {
                	// Categories are enabled, so the assignments are grouped by category
                	assignments = new ArrayList();

                	final List categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending(), getCategorySortColumn(), isCategorySortAscending(), true);

                	// if drop scores, must apply the average total as it was calculated for all assignments within the category
                    if(this.assignment.getCategory() != null && this.assignment.getCategory().isDropScores()) {
                    	for(final Object obj : categoryListWithCG) {
                    	    if(obj instanceof Category) {
                        	    final List<GradebookAssignment> catAssignments = ((Category)obj).getAssignmentList();
                        	    if(catAssignments != null) {
                            	    for(final GradebookAssignment catAssignment : catAssignments) {
                            	        if(catAssignment.equals(this.assignment)) {
                            	            this.assignment.setAverageTotal(catAssignment.getAverageTotal());
                            	            this.assignment.setMean(catAssignment.getMean());
                            	        }

                            	    }
                        	    }
                    	    }
                    	}
                    }

        			List categoryList = new ArrayList();

        			// first, remove the CourseGrade from the Category list
        			for (final Iterator catIter = categoryListWithCG.iterator(); catIter.hasNext();) {
        				final Object catOrCourseGrade = catIter.next();
        				if (catOrCourseGrade instanceof Category) {
        					categoryList.add(catOrCourseGrade);
        				}
        			}

        			if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
					//SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
        				final List<Long> catIds = new ArrayList<Long>();
        				for (final Category category : (List<Category>) categoryList) {
        					catIds.add(category.getId());
        				}
        				final List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUser(getGradebookId(), getUserUid(), catIds);
        				final List<Category> tmpCatList = new ArrayList<Category>();
        				for (final Category category : (List<Category>) categoryList) {
        					if(viewableCats.contains(category.getId())){
        						tmpCatList.add(category);
        					}
        				}
        				categoryList = tmpCatList;
        			}

        			if (categoryList != null) {
                		final Iterator catIter = categoryList.iterator();
                		while (catIter.hasNext()) {
            				final Category myCat = (Category) catIter.next();
            				final List catAssigns = myCat.getAssignmentList();
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
	                	final List assignNoCategory = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(), getAssignmentSortColumn(), isAssignmentSortAscending());
	                	if (assignNoCategory != null) {
	                		assignments.addAll(assignNoCategory);
	                	}
        			}
                }

                // Set up next and previous links, if any.
                final int thisIndex = assignments.indexOf(this.assignment);
				if (thisIndex > 0) {
					this.previousAssignment = (GradebookAssignment)assignments.get(thisIndex - 1);
				}
				if (thisIndex < (assignments.size() - 1)) {
					this.nextAssignment = (GradebookAssignment)assignments.get(thisIndex + 1);
				}

				final Category category = this.assignment.getCategory();
				Long categoryId = null;
				if (category != null) {
					categoryId = category.getId();
				}

				// Set up score rows.
				final Map enrollmentMap = getOrderedEnrollmentMapForItem(categoryId);

				List studentUids = new ArrayList(enrollmentMap.keySet());
				List gradeRecords = new ArrayList();
				if (getGradeEntryByPoints()) {
					gradeRecords = getGradebookManager().getAssignmentGradeRecords(this.assignment, studentUids);
				} else {
					gradeRecords = getGradebookManager().getAssignmentGradeRecordsConverted(this.assignment, studentUids);
				}

				final List<AssignmentGradeRecord> studentGradeRecords = getGradebookManager().getAllAssignmentGradeRecords(getGradebookId(), studentUids);

				getGradebookManager().applyDropScores(studentGradeRecords);

				copyDroppedFromGradeFlag(gradeRecords, studentGradeRecords);

				if (!isEnrollmentSort()) {
					// Need to sort and page based on a scores column.
					final List scoreSortedStudentUids = new ArrayList();
					for(final Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
						final AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
						scoreSortedStudentUids.add(agr.getStudentId());
					}

					// Put enrollments with no scores at the beginning of the final list.
					studentUids.removeAll(scoreSortedStudentUids);

					// Add all sorted enrollments with scores into the final list
					studentUids.addAll(scoreSortedStudentUids);

					studentUids = finalizeSortingAndPaging(studentUids);
				}

                // Get all of the grading events for these enrollments on this assignment
                final GradingEvents allEvents = getGradebookManager().getGradingEvents(this.assignment, studentUids);
                // NOTE: we are no longer converting the events b/c we are
                // storing what the user entered, not just points
                //getGradebookManager().convertGradingEventsConverted(assignment, allEvents, studentUids, getGradebook().getGrade_type());

                final Map gradeRecordMap = new HashMap();
                for (final Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
					final AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)iter.next();
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
                final List comments = getGradebookManager().getComments(this.assignment, studentUids);
                final Map commentMap = new HashMap();
                for (final Iterator iter = comments.iterator(); iter.hasNext(); ) {
					final Comment comment = (Comment)iter.next();
					commentMap.put(comment.getStudentId(), comment);
				}

				for (final Iterator iter = studentUids.iterator(); iter.hasNext(); ) {
					final String studentUid = (String)iter.next();
					final Map enrFunctionMap = (Map) enrollmentMap.get(studentUid);
					final List enrRecList = new ArrayList(enrFunctionMap.keySet());
					final EnrollmentRecord enrollment = (EnrollmentRecord)enrRecList.get(0); // there is only one rec in this map

					AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)gradeRecordMap.get(studentUid);
		            if(gradeRecord == null) {
		                gradeRecord = new AssignmentGradeRecord(this.assignment, studentUid, null);
		                gradeRecords.add(gradeRecord);
		            }

		            Comment comment = (Comment)commentMap.get(studentUid);
		            if (comment == null) {
		            	comment = new Comment(studentUid, null, this.assignment);
		            }

		            boolean userCanGrade = false;
		            final String itemFunction = (String)enrFunctionMap.get(enrollment);
		            if (itemFunction != null && itemFunction.equalsIgnoreCase(GradebookService.gradePermission)) {
						userCanGrade = true;
					}

					this.scoreRows.add(new ScoreRow(enrollment, gradeRecord, comment, allEvents.getEvents(studentUid), userCanGrade));
					if (userCanGrade) {
						this.isAllStudentsViewOnly = false;
					}
				}

				if (getCategoriesEnabled()) {
					if (this.assignment.getCategory() != null) {
                        final List<String> items = new ArrayList<String>();
                        if(this.assignment.getCategory().getDropHighest() != 0) {
                            items.add(getLocalizedString("cat_drop_highest_display", new String[] {this.assignment.getCategory().getDropHighest().toString()}));
                        }
						if (this.assignment.getCategory().getDropLowest() != 0) {
							items.add(getLocalizedString("cat_drop_lowest_display",
									new String[] { this.assignment.getCategory().getDropLowest().toString() }));
                        }
                        String categoryGradeDrops = null;
                        if(items.size() > 0) {
                            categoryGradeDrops = " " + items.toString().replace('[', '(').replace(']', ')');
                        }
						if (getWeightingEnabled()) {
							Double weight = this.assignment.getCategory().getWeight();
							if (weight != null && weight.doubleValue() > 0) {
								weight = new Double(weight.doubleValue() * 100);
							}
							if (weight == null) {
								throw new IllegalStateException(
										"Double weight == null!");
							}
							this.assignmentWeight = weight.toString();
							this.assignmentCategory = this.assignment.getCategory().getName() + " " + getLocalizedString("cat_weight_display", new String[] {this.assignmentWeight});
                            if(categoryGradeDrops != null) {
                                this.assignmentCategory = this.assignmentCategory + categoryGradeDrops;
                            }
						} else {
                            this.assignmentCategory = this.assignment.getCategory().getName();
                            if(categoryGradeDrops != null) {
                                this.assignmentCategory = this.assignmentCategory + categoryGradeDrops;
                            }
						}
					}
					else {
						this.assignmentCategory = getLocalizedString("assignment_details_assign_category");
					}
				}

			} else {
				// The assignment might have been removed since this link was set up.
				if (log.isWarnEnabled()) {
					log.warn("No assignmentId=" + this.assignmentId + " in gradebookUid " + getGradebookUid());
				}
                FacesUtil.addErrorMessage(getLocalizedString("assignment_details_assignment_removed"));
			}
		}

		if (isScoringAgentEnabled()) {
			this.scoringAgentData = initializeScoringAgentData(getGradebookUid(), this.assignmentId, null);
		}
	}

	private void copyDroppedFromGradeFlag(final List<AssignmentGradeRecord> dest, final List<AssignmentGradeRecord> source) {
        for(final AssignmentGradeRecord gradeRecord : dest) {
            final Long id = gradeRecord.getId();
            for(final AssignmentGradeRecord studentGradeRecord : source) {
                if(studentGradeRecord.getId().equals(id)) {
                    gradeRecord.setDroppedFromGrade(studentGradeRecord.getDroppedFromGrade());
                }
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
    @Override
	public Integer getSelectedSectionFilterValue() {
        return getPreferencesBean().getAssignmentDetailsTableSectionFilter();
    }
    @Override
	public void setSelectedSectionFilterValue(final Integer assignmentDetailsTableSectionFilter) {
        getPreferencesBean().setAssignmentDetailsTableSectionFilter(assignmentDetailsTableSectionFilter);
        super.setSelectedSectionFilterValue(assignmentDetailsTableSectionFilter);
    }

	/**
	 * Action listener to view a different assignment.
	 */
	public void processAssignmentIdChange(final ActionEvent event) {
		final Map params = FacesUtil.getEventParameterMap(event);
		if (log.isDebugEnabled()) {
			log.debug("processAssignmentIdAction params=" + params + ", current assignmentId=" + this.assignmentId);
		}
		final Long idParam = (Long)params.get("assignmentId");
		if (idParam != null) {
			setAssignmentId(idParam);
		}
		saveScoresFromPreviousOrNextButtons();
	}

	private void saveScoresFromPreviousOrNextButtons() throws StaleObjectModificationException {
        if (log.isDebugEnabled()) {
			log.debug("saveScores " + this.assignmentId);
		}

        final Set excessiveScores = getGradebookManager().updateAssignmentGradesAndComments(this.assignment, this.updatedGradeRecords, this.updatedComments);

        if (log.isDebugEnabled()) {
			log.debug("About to save " + this.updatedComments.size() + " updated comments");
		}
        if(this.updatedGradeRecords.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.updateItemScores","/gradebook/"+getGradebookId()+"/"+this.updatedGradeRecords.size()+"/"+getAuthzLevel());
        }
        if(this.updatedComments.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.comment","/gradebook/"+getGradebookId()+"/"+this.updatedComments.size()+"/"+getAuthzLevel());
        }
    }

	/**
	 * Action listener to update scores.
	 */
	public void processUpdateScores(final ActionEvent event) {
		try {
			for (final AssignmentGradeRecord agr : this.updatedGradeRecords) {
				getGradebookBean().getEventTrackingService().postEvent("gradebook.updateItemScore","/gradebook/"+getGradebookUid()+"/"+agr.getAssignment().getName()+"/"+agr.getStudentId()+"/"+agr.getPointsEarned()+"/"+getAuthzLevel());
			}
			saveScores();
		} catch (final StaleObjectModificationException e) {
            FacesUtil.addErrorMessage(getLocalizedString("assignment_details_locking_failure"));
		}
	}

	private void saveScores() throws StaleObjectModificationException {
        if (log.isDebugEnabled()) {
			log.debug("saveScores " + this.assignmentId);
		}

        final Set excessiveScores = getGradebookManager().updateAssignmentGradesAndComments(this.assignment, this.updatedGradeRecords, this.updatedComments);

        if (log.isDebugEnabled()) {
			log.debug("About to save " + this.updatedComments.size() + " updated comments");
		}
        if(this.updatedGradeRecords.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.updateItemScores","/gradebook/"+getGradebookId()+"/"+this.updatedGradeRecords.size()+"/"+getAuthzLevel());
        }
        if(this.updatedComments.size() > 0){
            getGradebookBean().getEventTrackingService().postEvent("gradebook.comment","/gradebook/"+getGradebookId()+"/"+this.updatedComments.size()+"/"+getAuthzLevel());
        }

        String messageKey = null;
        if (this.updatedGradeRecords.size() > 0) {
        	if (excessiveScores.size() > 0) {
        		messageKey = "assignment_details_scores_saved_excessive";
        	} else if (this.updatedComments.size() > 0) {
        		messageKey = "assignment_details_scores_comments_saved";
        	} else {
        		messageKey = "assignment_details_scores_saved";
        	}
        } else if (this.updatedComments.size() > 0) {
        	messageKey = "assignment_details_comments_saved";
        }

        // Let the user know.
        if (messageKey != null) {
        	FacesUtil.addMessage(getLocalizedString(messageKey));
        }
    }

    public void toggleEditableComments(final ActionEvent event) {
        // Don't write over any scores the user entered before pressing
        // the "Edit Comments" button.
        if (!this.isAllCommentsEditable) {
            this.workInProgress = true;
        }

        this.isAllCommentsEditable = !this.isAllCommentsEditable;
    }

    /**
     * View maintenance methods.
     */
    public Long getAssignmentId() {
        if (log.isDebugEnabled()) {
			log.debug("getAssignmentId " + this.assignmentId);
		}
        return this.assignmentId;
    }
    public void setAssignmentId(final Long assignmentId) {
        if (log.isDebugEnabled()) {
			log.debug("setAssignmentId " + assignmentId);
		}
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
	public void setAssignmentIdParam(final String assignmentIdParam) {
		if (log.isDebugEnabled()) {
			log.debug("setAssignmentIdParam String " + assignmentIdParam);
		}
		if ((assignmentIdParam != null) && (assignmentIdParam.length() > 0) &&
			!assignmentIdParam.equals("null")) {
			try {
				setAssignmentId(Long.valueOf(assignmentIdParam));
			} catch(final NumberFormatException e) {
				if (log.isWarnEnabled()) {
					log.warn("AssignmentId param set to non-number '" + assignmentIdParam + "'");
				}
			}
		}
	}

	public boolean isFirst() {
		return (this.previousAssignment == null);
	}
	public String getPreviousTitle() {
		return (this.previousAssignment != null) ? this.previousAssignment.getName() : "";
	}

	public boolean isLast() {
		return (this.nextAssignment == null);
	}

	public String getNextTitle() {
		return (this.nextAssignment != null) ? this.nextAssignment.getName() : "";
	}

	public List getScoreRows() {
		return this.scoreRows;
	}
	public void setScoreRows(final List scoreRows) {
		this.scoreRows = scoreRows;
	}

    // A desparate stab at reasonable embedded validation message formatting.
    // If the score column is an input box, it may have a wide message associated
    // with it, and we want the input field left-aligned to match up with
    // the non-erroroneous input fields (even though the actual input values
    // will be right-aligned). On the other hand, if the score column is read-only,
    // then we want to simply right-align the table column.
    public String getScoreColumnAlignment() {
    	if (this.assignment.isExternallyMaintained()) {
    		return "right";
    	} else {
    		return "left";
    	}
    }

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("assignment_details_log_type");
	}

    // Sorting
    @Override
	public boolean isSortAscending() {
        return getPreferencesBean().isAssignmentDetailsTableSortAscending();
    }
    @Override
	public void setSortAscending(final boolean sortAscending) {
        getPreferencesBean().setAssignmentDetailsTableSortAscending(sortAscending);
    }
    @Override
	public String getSortColumn() {
        return getPreferencesBean().getAssignmentDetailsTableSortColumn();
    }
    @Override
	public void setSortColumn(final String sortColumn) {
        getPreferencesBean().setAssignmentDetailsTableSortColumn(sortColumn);
    }
    public GradebookAssignment getAssignment() {
        return this.assignment;
    }
    public void setAssignment(final GradebookAssignment assignment) {
        this.assignment = assignment;
    }
    public GradebookAssignment getNextAssignment() {
        return this.nextAssignment;
    }
    public void setNextAssignment(final GradebookAssignment nextAssignment) {
        this.nextAssignment = nextAssignment;
    }
    public GradebookAssignment getPreviousAssignment() {
        return this.previousAssignment;
    }
    public void setPreviousAssignment(final GradebookAssignment previousAssignment) {
        this.previousAssignment = previousAssignment;
    }
	public String getAssignmentCategory() {
		return this.assignmentCategory;
	}
	public void setAssignmentCategory(final String assignmentCategory) {
		this.assignmentCategory = assignmentCategory;
	}
	public String getAssignmentWeight() {
		return this.assignmentWeight;
	}
	public void setAssignmentWeight(final String assignmentWeight) {
		this.assignmentWeight = assignmentWeight;
	}

    public String getCommentsToggle() {
    	final String messageKey = this.isAllCommentsEditable ?
    			"assignment_details_comments_read" :
				"assignment_details_comments_edit";
    	return getLocalizedString(messageKey);
    }

	public boolean isAllCommentsEditable() {
		return this.isAllCommentsEditable;
	}
	public boolean isAllStudentsViewOnly() {
		return this.isAllStudentsViewOnly;
	}

	public ScoringAgentData getScoringAgentData() {
		return this.scoringAgentData;
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
