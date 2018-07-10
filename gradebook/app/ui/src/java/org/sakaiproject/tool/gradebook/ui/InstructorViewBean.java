/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides data for the instructor's view of a student's grades in the gradebook.
 */
@Slf4j
public class InstructorViewBean extends ViewByStudentBean implements Serializable {
	private EnrollmentRecord previousStudent;
	private EnrollmentRecord nextStudent;
	private EnrollmentRecord currentStudent;

	private String studentEmailAddress;
	private String studentSections;

	private List orderedEnrollmentList;

	// parameters passed to the page
	private String returnToPage;
	private String assignmentId; // for returning to specific gradebook item

	private static final String ROSTER_PAGE = "roster";
	private static final String ASSIGN_DETAILS_PAGE = "assignmentDetails";

	/**
	 * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
	 */
	@Override
	public void init() {
		this.previousStudent = null;
		this.nextStudent = null;
		this.studentSections = null;

		final String sortByCol = getSortColumn();
		final boolean sortAsc = isSortAscending();

		setIsInstructorView(true);

		if (getStudentUid() != null) {
			super.init();

			this.studentEmailAddress = getGradebookBean().getUserEmailAddress(getStudentUid());
			this.studentSections = getStudentSectionsForDisplay();

			// set up the "next" and "previous" student navigation
			// TODO preserve filter/sort status from originating page
			if (this.orderedEnrollmentList == null) {
				this.orderedEnrollmentList = new ArrayList();
				if (this.returnToPage.equals(ROSTER_PAGE)) {
					super.setSelectedSectionFilterValue(getPreferencesBean().getRosterTableSectionFilter());
					this.maxDisplayedScoreRows = 0;
					this.orderedEnrollmentList = getOrderedEnrolleesFromRosterPage();
				} else if (this.returnToPage.equals(ASSIGN_DETAILS_PAGE)) {
					super.setSelectedSectionFilterValue(getPreferencesBean().getAssignmentDetailsTableSectionFilter());
					this.maxDisplayedScoreRows = 0;
					this.orderedEnrollmentList = getOrderedEnrolleesFromAssignDetailsPage();
				}
			}

			if (this.orderedEnrollmentList != null && this.orderedEnrollmentList.size() > 1) {
				int index = 0;
				while (index < this.orderedEnrollmentList.size()) {
					final EnrollmentRecord enrollee = (EnrollmentRecord)this.orderedEnrollmentList.get(index);
					if (enrollee.getUser().getUserUid().equals(getStudentUid())) {
						this.currentStudent = enrollee;
						if (index-1 >= 0) {
							this.previousStudent = (EnrollmentRecord) this.orderedEnrollmentList.get(index-1);
						}
						if (index+1 < this.orderedEnrollmentList.size()) {
							this.nextStudent = (EnrollmentRecord) this.orderedEnrollmentList.get(index+1);
						}
						break;
					}
					index++;
				}
			}

			setSortColumn(sortByCol);
			setSortAscending(sortAsc);

		}
	}

	// Navigation
	public EnrollmentRecord getPreviousStudent() {
		return this.previousStudent;
	}
	public EnrollmentRecord getNextStudent() {
		return this.nextStudent;
	}

	public boolean isFirst() {
		return this.previousStudent == null;
	}
	public boolean isLast() {
		return this.nextStudent == null;
	}

	public EnrollmentRecord getCurrentStudent() {
		return this.currentStudent;
	}

	/**
	 * @return text for "Return to" button
	 */
	public String getReturnToPageButtonName() {
		String pageTitle;
		if (ASSIGN_DETAILS_PAGE.equals(this.returnToPage)) {
			pageTitle = getLocalizedString("assignment_details_page_title");
		} else {
			pageTitle = getLocalizedString("roster_page_title");
		}

		return getLocalizedString("inst_view_return_to", new String[] {pageTitle});
	}

	/**
	 *
	 * @return page title of originating page
	 */
	public String getReturnToPageName() {
		if (this.returnToPage.equals(ASSIGN_DETAILS_PAGE)) {
			return getLocalizedString("assignment_details_page_title");
		} else {
			return getLocalizedString("roster_page_title");
		}
	}

	/**
	 *
	 * @return comma-separated string of user's section/group memberships
	 */
	public String getStudentSections() {
		return this.studentSections;
	}

    /**
     * @return studentEmailAddress
     */
    public String getStudentEmailAddress() {
        return this.studentEmailAddress;
    }

	/**
	 * Action listener to view a different student
	 */
	public void processStudentUidChange(final ActionEvent event) {
		final Map params = FacesUtil.getEventParameterMap(event);
		if (log.isDebugEnabled()) {
			log.debug("processStudentUidChange params=" + params + ", current studentUid=" + getStudentUid());
		}
		// run the updates before changing the student id
		processUpdateScoresForPreNextStudent();
		final String idParam = (String)params.get("studentUid");
		if (idParam != null) {
			setStudentUid(idParam);
		}
	}

	public void processUpdateScoresForPreNextStudent() {
		try {
			saveScoresWithoutConfirmation();
		} catch (final StaleObjectModificationException e) {
			FacesUtil.addErrorMessage(getLocalizedString("assignment_details_locking_failure"));
		}
	}

	/**
	 * Save the input scores for the user
	 * @throws StaleObjectModificationException
	 */
	public void saveScoresWithoutConfirmation() throws StaleObjectModificationException {
        if (log.isInfoEnabled()) {
			log.info("saveScores for " + getStudentUid());
		}

		// first, determine which scores were updated
		final List updatedGradeRecords = new ArrayList();
		if (getGradebookItems() != null) {
			final Iterator itemIter = getGradebookItems().iterator();
			while (itemIter.hasNext()) {
				final Object item = itemIter.next();
				if (item instanceof AssignmentGradeRow) {
					final AssignmentGradeRow gradeRow = (AssignmentGradeRow) item;
					AssignmentGradeRecord gradeRecord = gradeRow.getGradeRecord();

					if (gradeRecord == null && (gradeRow.getScore() != null || gradeRow.getLetterScore() != null)) {
						// this is a new grade
						gradeRecord = new AssignmentGradeRecord(gradeRow.getAssociatedAssignment(), getStudentUid(), null);
					}
					if (gradeRecord != null) {
						if (getGradeEntryByPoints()) {
							Double originalScore = null;
							originalScore = gradeRecord.getPointsEarned();

							if (originalScore != null) {
								// truncate to two decimals for more accurate comparison
								originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
							}
							final Double newScore = gradeRow.getScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setPointsEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
							}
						} else if(getGradeEntryByPercent()) {
							Double originalScore = null;
							originalScore = gradeRecord.getPercentEarned();

							if (originalScore != null) {
								// truncate to two decimals for more accurate comparison
								originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
							}
							final Double newScore = gradeRow.getScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setPercentEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
							}

						}	else if (getGradeEntryByLetter()) {

							final String originalScore = gradeRecord.getLetterEarned();
							final String newScore = gradeRow.getLetterScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setLetterEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
							}
						}
					}
				}
			}
		}

		if (updatedGradeRecords.isEmpty()) {
			getGradebookBean().postEvent("gradebook.updateItemScores",
					"/gradebook/" + getGradebookId() + "/" + updatedGradeRecords.size() + "/" + getAuthzLevel(), true);
		}
	}

	/**
	 * Action listener to update scores.
	 */
	public void processUpdateScores() {
		try {
			saveScores();
		} catch (final StaleObjectModificationException e) {
			FacesUtil.addErrorMessage(getLocalizedString("assignment_details_locking_failure"));
		}
	}

	/**
	 * Save the input scores for the user
	 * @throws StaleObjectModificationException
	 */
	public void saveScores() throws StaleObjectModificationException {
        if (log.isInfoEnabled()) {
			log.info("saveScores for " + getStudentUid());
		}

		// first, determine which scores were updated
		final List updatedGradeRecords = new ArrayList();
		if (getGradebookItems() != null) {
			final Iterator itemIter = getGradebookItems().iterator();
			while (itemIter.hasNext()) {
				final Object item = itemIter.next();
				if (item instanceof AssignmentGradeRow) {
					final AssignmentGradeRow gradeRow = (AssignmentGradeRow) item;
					AssignmentGradeRecord gradeRecord = gradeRow.getGradeRecord();

					if (gradeRecord == null && (gradeRow.getScore() != null || gradeRow.getLetterScore() != null)) {
						// this is a new grade
						gradeRecord = new AssignmentGradeRecord(gradeRow.getAssociatedAssignment(), getStudentUid(), null);
					}
					if (gradeRecord != null) {
						if (getGradeEntryByPoints()) {
							Double originalScore = null;
							originalScore = gradeRecord.getPointsEarned();

							if (originalScore != null) {
								// truncate to two decimals for more accurate comparison
								originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
							}
							final Double newScore = gradeRow.getScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setPointsEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
								getGradebookBean().postEvent("gradebook.updateItemScore",
										"/gradebook/" + getGradebookUid() + "/" + gradeRecord.getAssignment().getName() + "/"
												+ gradeRecord.getStudentId() + "/" + gradeRecord.getPointsEarned() + "/" + getAuthzLevel(),
										true);
							}
						} else if(getGradeEntryByPercent()) {
							Double originalScore = null;
							originalScore = gradeRecord.getPercentEarned();

							if (originalScore != null) {
								// truncate to two decimals for more accurate comparison
								originalScore = new Double(FacesUtil.getRoundDown(originalScore.doubleValue(), 2));
							}
							final Double newScore = gradeRow.getScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setPercentEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
							}

						}	else if (getGradeEntryByLetter()) {

							final String originalScore = gradeRecord.getLetterEarned();
							final String newScore = gradeRow.getLetterScore();
							if ( (originalScore != null && !originalScore.equals(newScore)) ||
									(originalScore == null && newScore != null) ) {
								gradeRecord.setLetterEarned(newScore);
								updatedGradeRecords.add(gradeRecord);
							}
						}
					}
				}
			}
		}

		final Set excessiveScores = getGradebookManager().updateStudentGradeRecords(updatedGradeRecords, getGradebook().getGrade_type(), getStudentUid());

		if(updatedGradeRecords.size() > 0){
			getGradebookBean().postEvent("gradebook.updateItemScores",
					"/gradebook/" + getGradebookId() + "/" + updatedGradeRecords.size() + "/" + getAuthzLevel(), true);
			final String messageKey = (excessiveScores.size() > 0) ?
					"inst_view_scores_saved_excessive" :
						"inst_view_scores_saved";

			// Let the user know.
			FacesUtil.addMessage(getLocalizedString(messageKey));
		}


	}

	private String getColumnHeader(final GradableObject gradableObject) {
		if (gradableObject.isCourseGrade()) {
			return getLocalizedString("roster_course_grade_column_name");
		} else {
			return ((GradebookAssignment)gradableObject).getName();
		}
	}

	/**
	 * If we came to the instructor view from the roster page, we need to
	 * set the previous and next student info according to the order and filter
	 * on the roster page
	 * @return
	 */
	private List getOrderedEnrolleesFromRosterPage() {
		// it may be sorted by name, id, cumulative score, or any of the individual
		// assignments
		setSortColumn(getPreferencesBean().getRosterTableSortColumn());
		setSortAscending(getPreferencesBean().isRosterTableSortAscending());

		final Map enrollmentMap = getOrderedEnrollmentMapForAllItems();

		List workingEnrollments = new ArrayList(enrollmentMap.keySet());

		if (isEnrollmentSort()) {
			return workingEnrollments;
		}

		final Map studentIdItemIdFunctionMap = new HashMap();
		final Map studentIdEnrRecMap = new HashMap();
		for (final Iterator enrIter = workingEnrollments.iterator(); enrIter.hasNext();) {
        	final EnrollmentRecord enr = (EnrollmentRecord) enrIter.next();
        	if (enr != null) {
        		final String studentId = enr.getUser().getUserUid();
        		final Map itemFunctionMap = (Map)enrollmentMap.get(enr);

				studentIdItemIdFunctionMap.put(studentId, itemFunctionMap);
				studentIdEnrRecMap.put(studentId, enr);
        	}
        }

		List rosterGradeRecords = getGradebookManager().getAllAssignmentGradeRecords(getGradebookId(), studentIdItemIdFunctionMap.keySet());
		final Map gradeRecordMap = new HashMap();

        if (!isUserAbleToGradeAll() && isUserHasGraderPermissions()) {
			getGradebookManager().addToGradeRecordMap(gradeRecordMap, rosterGradeRecords, studentIdItemIdFunctionMap);
			// we need to re-sort these records b/c some may actually be null based upon permissions.
			// retrieve updated grade recs from gradeRecordMap
			final List updatedGradeRecs = new ArrayList();
			for (final Iterator<Map.Entry<String, Map>> iter = gradeRecordMap.entrySet().iterator(); iter.hasNext();) {
                final Map.Entry<String, Map> entry = iter.next();
				final String studentId = entry.getKey();
				final Map itemIdGradeRecMap = (Map)gradeRecordMap.get(studentId);
				if (!itemIdGradeRecMap.isEmpty()) {
					updatedGradeRecs.addAll(itemIdGradeRecMap.values());
				}
			}
			Collections.sort(updatedGradeRecs, AssignmentGradeRecord.calcComparator);
			rosterGradeRecords = updatedGradeRecs;
		} else {
			getGradebookManager().addToGradeRecordMap(gradeRecordMap, rosterGradeRecords);
		}
		if (log.isDebugEnabled()) {
			log.debug("init - gradeRecordMap.keySet().size() = " + gradeRecordMap.keySet().size());
		}

		List assignments = null;
		final String selectedCategoryUid = getSelectedCategoryUid();
		final CourseGrade courseGrade = getGradebookManager().getCourseGrade(getGradebookId());
		if(selectedCategoryUid == null) {
			assignments = getGradebookManager().getAssignments(getGradebookId());
		} else {
			assignments = getGradebookManager().getAssignmentsForCategory(new Long(getSelectedSectionFilterValue().longValue()));
		}

		final List courseGradeRecords = getGradebookManager().getPointsEarnedCourseGradeRecords(courseGrade, studentIdItemIdFunctionMap.keySet(), assignments, gradeRecordMap);
		Collections.sort(courseGradeRecords, CourseGradeRecord.calcComparator);
		getGradebookManager().addToGradeRecordMap(gradeRecordMap, courseGradeRecords);
		rosterGradeRecords.addAll(courseGradeRecords);

		//do category results
		final Map categoryResultMap = new HashMap();
		final List categories = getGradebookManager().getCategories(getGradebookId());
		getGradebookManager().addToCategoryResultMap(categoryResultMap, categories, gradeRecordMap, studentIdEnrRecMap);
		if (log.isDebugEnabled()) {
			log.debug("init - categoryResultMap.keySet().size() = " + categoryResultMap.keySet().size());
		}


		// Need to sort and page based on a scores column.
		final String sortColumn = getSortColumn();
		final List scoreSortedEnrollments = new ArrayList();
		for(final Iterator iter = rosterGradeRecords.iterator(); iter.hasNext();) {
			final AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
			if(getColumnHeader(agr.getGradableObject()).equals(sortColumn)) {
				scoreSortedEnrollments.add(studentIdEnrRecMap.get(agr.getStudentId()));
			}
		}

		// Put enrollments with no scores at the beginning of the final list.
		workingEnrollments.removeAll(scoreSortedEnrollments);

		// Add all sorted enrollments with scores into the final list
		workingEnrollments.addAll(scoreSortedEnrollments);

		workingEnrollments = finalizeSortingAndPaging(workingEnrollments);

		return workingEnrollments;
	}

	/**
	 * If we came to the instructor view from the assign details page, we need to
	 * set the previous and next student info according to the order and filter
	 * on the assign details page
	 * @return
	 */
	private List getOrderedEnrolleesFromAssignDetailsPage() {
		setSortColumn(getPreferencesBean().getAssignmentDetailsTableSortColumn());
		setSortAscending(getPreferencesBean().isAssignmentDetailsTableSortAscending());

		List assignGradeRecords = new ArrayList();
		final List enrollments = new ArrayList();

		final Long assignmentIdAsLong = getAssignmentIdAsLong();
		if (assignmentIdAsLong != null) {
			final GradebookAssignment prevAssignment = getGradebookManager().getAssignment(assignmentIdAsLong);
			final Category category = prevAssignment.getCategory();
			Long catId = null;
			if (category != null) {
				catId = category.getId();
			}

			final Map enrollmentMap = getOrderedStudentIdEnrollmentMapForItem(catId);
			if (isEnrollmentSort()) {
				return new ArrayList(enrollmentMap.values());
			}

			List studentUids = new ArrayList(enrollmentMap.keySet());

			if (getGradeEntryByPoints()) {
				assignGradeRecords = getGradebookManager().getAssignmentGradeRecords(prevAssignment, studentUids);
			} else if (getGradeEntryByPercent() || getGradeEntryByLetter()) {
				assignGradeRecords = getGradebookManager().getAssignmentGradeRecordsConverted(prevAssignment, studentUids);
			}

			// Need to sort and page based on a scores column.
			final List scoreSortedStudentUids = new ArrayList();
			for(final Iterator iter = assignGradeRecords.iterator(); iter.hasNext();) {
				final AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
				scoreSortedStudentUids.add(agr.getStudentId());
			}

			// Put enrollments with no scores at the beginning of the final list.
			studentUids.removeAll(scoreSortedStudentUids);

			// Add all sorted enrollments with scores into the final list
			studentUids.addAll(scoreSortedStudentUids);

			studentUids = finalizeSortingAndPaging(studentUids);

			if (studentUids != null) {
				final Iterator studentIter = studentUids.iterator();
				while (studentIter.hasNext()) {
					final String studentId = (String) studentIter.next();
					final EnrollmentRecord enrollee = (EnrollmentRecord)enrollmentMap.get(studentId);
					if (enrollee != null) {
						enrollments.add(enrollee);
					}
				}
			}
		}

		return enrollments;
	}

	/**
	 *
	 * @return String representation of the student's sections/groups
	 */
	private String getStudentSectionsForDisplay() {
		final StringBuilder sectionList = new StringBuilder();
		final List studentMemberships = getGradebookBean().getAuthzService().getStudentSectionMembershipNames(getGradebookUid(), getStudentUid());
		if (studentMemberships != null && !studentMemberships.isEmpty()) {
			Collections.sort(studentMemberships);
			for (int i=0; i < studentMemberships.size(); i++) {
				final String sectionName = (String)studentMemberships.get(i);
				if (i == (studentMemberships.size()-1)) {
					sectionList.append(sectionName);
				} else {
					sectionList.append(getLocalizedString("inst_view_sections_list", new String[] {sectionName}) + " ");
				}
			}
		}

		return sectionList.toString();
	}

	/**
	 * View maintenance methods.
	 */
	public String getReturnToPage() {
		if (this.returnToPage == null) {
			this.returnToPage = ROSTER_PAGE;
		}
		return this.returnToPage;
	}
	public void setReturnToPage(final String returnToPage) {
		this.returnToPage = returnToPage;
	}
	public String getAssignmentId() {
		return this.assignmentId;
	}
	public void setAssignmentId(final String assignmentId) {
		this.assignmentId = assignmentId;
	}

	private Long getAssignmentIdAsLong() {
		Long id = null;
		if (this.assignmentId == null) {
			return id;
		}

		try {
			id = new Long(this.assignmentId);

		} catch (final Exception e) {
		}

		return id;
	}

	/**
	 * Go to assignment details page. Need to override here
	 * because on other pages, may need to return to where
	 * called from while here we want to go directly to
	 * assignment details.
	 */
	public String navigateToAssignmentDetails() {
		setNav(null, null, null, "false", null);

		return "assignmentDetails";
	}

	/**
	 * Go to either Roster or GradebookAssignment Details page.
	 */
	@Override
	public String processCancel() {
		if (new Boolean((String) SessionManager.getCurrentToolSession().getAttribute("middle")).booleanValue()) {
			final AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
			assignmentDetailsBean.setAssignmentId(new Long(this.assignmentId));

			return navigateToAssignmentDetails();
		}
		else {
			return getBreadcrumbPage();
		}

	}
}
