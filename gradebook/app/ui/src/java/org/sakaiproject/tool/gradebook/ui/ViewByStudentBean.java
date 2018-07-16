/*******************************************************************************
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.GradingEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides data for the student view of the gradebook. Is used by both the instructor and student views. Based upon original
 * StudentViewBean
 *
 */
@Slf4j
public class ViewByStudentBean extends EnrollmentTableBean implements Serializable {
	// View maintenance fields - serializable.
	private String userDisplayName;
	private boolean courseGradeReleased;
	private boolean coursePointsReleased;
	private CourseGradeRecord courseGradeRecord;
	private String courseGradeLetter;
	private boolean assignmentsReleased;
	private boolean anyNotCounted;
	private boolean anyExternallyMaintained = false;
	private boolean isAllItemsViewOnly = true;
	private double totalPoints;
	private double pointsEarned;

	private boolean sortAscending;
	private String sortColumn;

	private boolean isInstructorView = false;

	private StringBuilder rowStyles;
	private Map commentMap;

	private List gradebookItems;
	private String studentUid;
	private Gradebook gradebook;

	private ScoringAgentData scoringAgentData;

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
			@Override
			public int compare(final Object o1, final Object o2) {
				return GradableObject.sortingComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment(),
						((AssignmentGradeRow) o2).getAssociatedAssignment());
			}
		};
		nameComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return GradebookAssignment.nameComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment(),
						((AssignmentGradeRow) o2).getAssociatedAssignment());
			}
		};
		dateComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return GradebookAssignment.dateComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment(),
						((AssignmentGradeRow) o2).getAssociatedAssignment());
			}
		};
		pointsPossibleComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return GradebookAssignment.pointsComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment(),
						((AssignmentGradeRow) o2).getAssociatedAssignment());
			}
		};

		doubleOrNothingComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				final Double double1 = (Double) o1;
				final Double double2 = (Double) o2;

				if (double1 == null && double2 == null) {
					return 0;
				} else if (double1 == null && double2 != null) {
					return -1;
				} else if (double1 != null && double2 == null) {
					return 1;
				} else {
					return double1.compareTo(double2);
				}
			}
		};

		pointsEarnedComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				final int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow) o1).getPointsEarned(),
						((AssignmentGradeRow) o2).getPointsEarned());
				if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
			}
		};
		gradeAsPercentageComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				final int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow) o1).getGradeAsPercentage(),
						((AssignmentGradeRow) o2).getGradeAsPercentage());
				if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
			}
		};
		itemValueComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				final int comp = doubleOrNothingComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment().getPointsPossible(),
						((AssignmentGradeRow) o2).getAssociatedAssignment().getPointsPossible());
				if (comp == 0) {
					return nameComparator.compare(o1, o2);
				} else {
					return comp;
				}
			}
		};
		gradeEditorComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return GradebookAssignment.gradeEditorComparator.compare(((AssignmentGradeRow) o1).getAssociatedAssignment(),
						((AssignmentGradeRow) o2).getAssociatedAssignment());
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
		columnSortMap.put(GradebookAssignment.SORT_BY_EDITOR, ViewByStudentBean.gradeEditorComparator);
	}

	/**
	 * Since this bean does not use the session-scoped preferences bean to keep sort preferences, we need to define the defaults locally.
	 */
	public ViewByStudentBean() {
		// SAK-15311 - setup so students view can use sorting if configured
		final boolean useSort = getGradebookBean().getConfigurationBean().getBooleanConfig("gradebook.students.use.sorting", false);
		if (useSort) {
			// use sorting order
			this.sortAscending = true;
			this.sortColumn = SORT_BY_SORTING;
		} else {
			// use old default
			this.sortAscending = true;
			this.sortColumn = SORT_BY_DATE;
		}
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.ui.InitializableBean#init()
	 */
	@Override
	public void init() {
		// Get the active gradebook
		this.gradebook = getGradebook();
		final CourseGrade cg = getGradebookManager().getCourseGrade(getGradebookId());

		this.isAllItemsViewOnly = true;

		// Set the display name
		try {
			this.userDisplayName = getGradebookBean().getUserDisplayName(this.studentUid);
		} catch (final UnknownUserException e) {
			if (log.isErrorEnabled()) {
				log.error("User " + this.studentUid + " is unknown but referenced in gradebook " + this.gradebook.getUid());
			}
			this.userDisplayName = "";
		}

		this.courseGradeReleased = this.gradebook.isCourseGradeDisplayed();
		this.coursePointsReleased = getShowCoursePoints() && this.gradebook.isCoursePointsDisplayed();
		this.assignmentsReleased = this.gradebook.isAssignmentsDisplayed();

		// Reset the row styles
		this.rowStyles = new StringBuilder();

		// Display course grade if we've been instructed to.
		final CourseGradeRecord gradeRecord = getGradebookManager().getPointsEarnedCourseGradeRecords(cg, this.studentUid);
		if (gradeRecord != null) {
			if (this.courseGradeReleased || this.isInstructorView) {
				this.courseGradeRecord = gradeRecord;
				this.courseGradeLetter = gradeRecord.getDisplayGrade();
			}
			if (gradeRecord.getPointsEarned() != null) {
				this.pointsEarned = gradeRecord.getPointsEarned();
			}
		}

		final List<AssignmentGradeRecord> studentGradeRecs = getGradebookManager().getStudentGradeRecords(this.gradebook.getId(),
				this.studentUid);
		getGradebookManager().applyDropScores(studentGradeRecs);

		final List<GradebookAssignment> assignments = getGradebookManager().getAssignments(this.gradebook.getId());
		final List<GradebookAssignment> countedAssigns = new ArrayList<GradebookAssignment>();
		// let's filter the passed assignments to make sure they are all counted
		if (assignments != null) {
			for (final GradebookAssignment assign : assignments) {
				if (assign.isIncludedInCalculations()) {
					countedAssigns.add(assign);
				}
			}
		}
		this.totalPoints = getGradebookManager().getTotalPointsInternal(this.gradebook,
				getGradebookManager().getCategories(this.gradebook.getId()),
				this.studentUid, studentGradeRecs, countedAssigns, true);
		// getTotalPointsInternal(gradebook, categories, studentUid, studentGradeRecs, countedAssigns);

		initializeStudentGradeData();

		if (isScoringAgentEnabled()) {
			this.scoringAgentData = initializeScoringAgentData(getGradebookUid(), null, this.studentUid);
		}
	}

	/**
	 * @return Returns the gradebookItems. Can include AssignmentGradeRows and Categories
	 */
	public List getGradebookItems() {
		return this.gradebookItems;
	}

	/**
	 * @return Returns the CourseGradeRecord for this student
	 */
	public CourseGradeRecord getCourseGradeRecord() {
		return this.courseGradeRecord;
	}

	/**
	 *
	 * @return letter representation of course grade
	 */
	public String getCourseGradeLetter() {
		return this.courseGradeLetter;
	}

	/**
	 * @return Returns the courseGradeReleased.
	 */
	public boolean isCourseGradeReleased() {
		return this.courseGradeReleased;
	}

	public boolean isCoursePointsReleased() {
		return this.coursePointsReleased;
	}

	public boolean isAssignmentsReleased() {
		return this.assignmentsReleased;
	}

	/**
	 * @return Returns the userDisplayName.
	 */
	public String getUserDisplayName() {
		return this.userDisplayName;
	}

	/**
	 * Sets userDisplayName
	 * 
	 * @param userDisplayName
	 */
	public void setUserDisplayName(final String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	// Sorting
	@Override
	public boolean isSortAscending() {
		return this.sortAscending;
	}

	@Override
	public void setSortAscending(final boolean sortAscending) {
		this.sortAscending = sortAscending;
	}

	@Override
	public String getSortColumn() {
		return this.sortColumn;
	}

	@Override
	public void setSortColumn(final String sortColumn) {
		this.sortColumn = sortColumn;
	}

	/**
	 * @return The comma-separated list of css styles to use in displaying the rows
	 */
	public String getRowStyles() {
		if (this.rowStyles == null) {
			return null;
		} else {
			return this.rowStyles.toString();
		}
	}

	public String getEventsLogType() {
		return getLocalizedString("inst_view_log_type");
	}

	/**
	 * @return True if the gradebook contains any assignments not counted toward the final course grade.
	 */
	public boolean isAnyNotCounted() {
		return this.anyNotCounted;
	}

	/**
	 * if all items are "view-only", we need to disable the action buttons
	 * 
	 * @return
	 */
	public boolean isAllItemsViewOnly() {
		return this.isAllItemsViewOnly;
	}

	/**
	 *
	 * @return true if the gradebook contains any externally maintained assignments
	 */
	public boolean isAnyExternallyMaintained() {
		return this.anyExternallyMaintained;
	}

	public void setStudentUid(final String studentUid) {
		this.studentUid = studentUid;
	}

	public String getStudentUid() {
		return this.studentUid;
	}

	public double getTotalPoints() {
		return this.totalPoints;
	}

	public double getPointsEarned() {
		return this.pointsEarned;
	}

	/**
	 * Instructor view will include some features that aren't appropriate for student view
	 * 
	 * @param includeNotCountedInCategoryAvg
	 */
	public void setIsInstructorView(final boolean isInstructorView) {
		this.isInstructorView = isInstructorView;
	}

	public ScoringAgentData getScoringAgentData() {
		return this.scoringAgentData;
	}

	/**
	 * Create the AssignmentGradeRows for the passed assignments list
	 * 
	 * @param assignments
	 * @param gradeRecords
	 * @return
	 */
	private List retrieveGradeRows(final List assignments, final List gradeRecords) {
		List gradeRows = new ArrayList();

		// Don't display any assignments if they have not been released
		if (!this.assignmentsReleased && !this.isInstructorView) {
			return gradeRows;
		}

		if (assignments == null) {
			return gradeRows;
		}

		log.debug(assignments.size() + " total assignments");
		log.debug(gradeRecords.size() + "  grade records");

		boolean userHasGraderPerms;
		if (isUserAbleToGradeAll()) {
			userHasGraderPerms = false;
		} else if (isUserHasGraderPermissions()) {
			userHasGraderPerms = true;
		} else {
			userHasGraderPerms = false;
		}

		Map viewableAssignmentsMap = new HashMap();
		if (userHasGraderPerms) {
			viewableAssignmentsMap = getGradebookPermissionService().getAvailableItemsForStudent(this.gradebook.getId(), getUserUid(),
					this.studentUid, getAllSections());
		}

		// Create a map of assignments to assignment grade rows
		final Map asnMap = new HashMap();
		for (final Iterator iter = assignments.iterator(); iter.hasNext();) {

			final GradebookAssignment asn = (GradebookAssignment) iter.next();

			if (userHasGraderPerms) {
				final String function = (String) viewableAssignmentsMap.get(asn.getId());
				if (function != null) {
					final boolean userCanGrade = function.equalsIgnoreCase(GradebookService.gradePermission);
					if (userCanGrade) {
						this.isAllItemsViewOnly = false;
					}
					asnMap.put(asn, new AssignmentGradeRow(asn, this.gradebook, userCanGrade));
				}
			} else {
				asnMap.put(asn, new AssignmentGradeRow(asn, this.gradebook, true));
				this.isAllItemsViewOnly = false;
			}
		}

		final List<GradebookAssignment> gradebookAssignments = new ArrayList(asnMap.keySet());

		for (final Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
			final AssignmentGradeRecord asnGr = (AssignmentGradeRecord) iter.next();

			if (asnGr != null) {
				// Update the AssignmentGradeRow in the map
				final AssignmentGradeRow asnGradeRow = (AssignmentGradeRow) asnMap.get(asnGr.getAssignment());
				if (asnGradeRow != null) {
					final GradebookAssignment asnGrAssignment = asnGr.getAssignment();

					// if weighted gb and no category for assignment,
					// it is not counted toward course grade
					boolean counted = asnGrAssignment.isCounted();
					if (counted && getWeightingEnabled()) {
						final Category assignCategory = asnGrAssignment.getCategory();
						if (assignCategory == null) {
							counted = false;
						}
					}

					asnGradeRow.setGradeRecord(asnGr);

					if (getGradeEntryByPercent()) {
						asnGradeRow.setScore(truncateScore(asnGr.getPercentEarned()));
					} else if (getGradeEntryByPoints()) {
						asnGradeRow.setScore(truncateScore(asnGr.getPointsEarned()));
					} else if (getGradeEntryByLetter()) {
						asnGradeRow.setLetterScore(asnGr.getLetterEarned());
					}
				}
			}
		}

		final Map goEventListMap = getGradebookManager().getGradingEventsForStudent(this.studentUid, gradebookAssignments);
		// NOTE: we are no longer converting the events b/c we are
		// storing what the user entered, not just points

		// iterate through the assignments and update the comments and grading events
		final Iterator assignmentIterator = gradebookAssignments.iterator();
		while (assignmentIterator.hasNext()) {
			final GradebookAssignment assignment = (GradebookAssignment) assignmentIterator.next();

			final AssignmentGradeRow asnGradeRow = (AssignmentGradeRow) asnMap.get(assignment);

			// Grading events
			if (this.isInstructorView) {
				List assignEventList = new ArrayList();
				if (goEventListMap != null) {
					assignEventList = (List) goEventListMap.get(assignment);
				}

				if (assignEventList != null && !assignEventList.isEmpty()) {
					final List eventRows = new ArrayList();
					for (final Iterator iter = assignEventList.iterator(); iter.hasNext();) {
						final GradingEvent gradingEvent = (GradingEvent) iter.next();
						eventRows.add(new GradingEventRow(gradingEvent));
					}
					asnGradeRow.setEventRows(eventRows);
					asnGradeRow.setEventsLogTitle(getLocalizedString("inst_view_log_title", new String[] { getUserDisplayName() }));
				}
			}

			// Comments
			try {
				final Comment comment = (Comment) this.commentMap.get(asnGradeRow.getAssociatedAssignment().getId());
				if (comment.getCommentText().length() > 0) {
					asnGradeRow.setCommentText(comment.getCommentText());
				}
			} catch (final NullPointerException npe) {
				if (log.isDebugEnabled()) {
					log.debug("assignment has no associated comment");
				}
			}
		}

		gradeRows = new ArrayList(asnMap.values());

		// remove assignments that are not released
		Iterator i = gradeRows.iterator();
		while (i.hasNext()) {
			final AssignmentGradeRow assignmentGradeRow = (AssignmentGradeRow) i.next();
			if (!assignmentGradeRow.getAssociatedAssignment().isReleased() && !this.isInstructorView) {
				i.remove();
			}
		}

		i = gradeRows.iterator();
		final GradebookExternalAssessmentService gext = getGradebookExternalAssessmentService();
		Map<String, String> externalAssignments = null;
		if (this.isInstructorView) {
			final Map<String, List<String>> visible = gext.getVisibleExternalAssignments(this.gradebook.getUid(),
					Arrays.asList(this.studentUid));
			if (visible.containsKey(this.studentUid)) {
				externalAssignments = new HashMap<String, String>();
				for (final String externalId : visible.get(this.studentUid)) {
					// FIXME: Take one of the following options for consistency:
					// 1. Strip off the appKey from the single-user query
					// 2. Add a layer to the all-user return to identify the appKey
					externalAssignments.put(externalId, "");
				}
			}
		} else {
			externalAssignments = gext.getExternalAssignmentsForCurrentUser(this.gradebook.getUid());
		}

		while (i.hasNext()) {
			final GradebookAssignment assignment = ((AssignmentGradeRow) i.next()).getAssociatedAssignment();

			if (assignment.isExternallyMaintained() && !externalAssignments.containsKey(assignment.getExternalId())) {
				i.remove();
			}
		}

		if (!this.sortColumn.equals(Category.SORT_BY_WEIGHT)) {
			Collections.sort(gradeRows, (Comparator) columnSortMap.get(this.sortColumn));
			if (!this.sortAscending) {
				Collections.reverse(gradeRows);
			}
		}

		return gradeRows;
	}

	/**
	 * Sets up the grade/category rows for student
	 * 
	 * @param userUid
	 * @param gradebook
	 */
	private void initializeStudentGradeData() {

		// do not retrieve assignments if not displayed for students
		if (this.assignmentsReleased || this.isInstructorView) {

			// get grade comments and load them into a map assignmentId->comment
			this.commentMap = new HashMap();
			final List assignmentComments = getGradebookManager().getStudentAssignmentComments(this.studentUid, this.gradebook.getId());
			log.debug("number of comments " + assignmentComments.size());
			final Iterator iteration = assignmentComments.iterator();
			while (iteration.hasNext()) {
				final Comment comment = (Comment) iteration.next();
				this.commentMap.put(comment.getGradableObject().getId(), comment);
			}

			// get the student grade records
			final List gradeRecords = getGradebookManager().getStudentGradeRecordsConverted(this.gradebook.getId(), this.studentUid);
			getGradebookManager().applyDropScores(gradeRecords);

			// The display may include categories and assignments, so we need a generic list
			this.gradebookItems = new ArrayList();

			if (getCategoriesEnabled()) {
				// we will also have to determine the student's category avg - the category stats
				// are for class avg
				List categoryListWithCG = new ArrayList();
				final Set studentUids = new HashSet<String>();
				studentUids.add(this.studentUid);
				if (this.sortColumn.equals(Category.SORT_BY_WEIGHT)) {
					categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), GradebookAssignment.DEFAULT_SORT,
							true, this.sortColumn, this.sortAscending, false, studentUids);
				} else {
					categoryListWithCG = getGradebookManager().getCategoriesWithStats(getGradebookId(), GradebookAssignment.DEFAULT_SORT,
							true, Category.SORT_BY_NAME, true, false, studentUids);
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
					// SAK-19896, eduservice's can't share the same "Category" class, so just pass the ID's
					final List<Long> catIds = new ArrayList<Long>();
					for (final Category category : (List<Category>) categoryList) {
						catIds.add(category.getId());
					}
					final List<Long> viewableCats = getGradebookPermissionService().getCategoriesForUserForStudentView(getGradebookId(),
							getUserUid(), this.studentUid, catIds, getViewableSectionIds());
					final List<Category> tmpCatList = new ArrayList<Category>();
					for (final Category category : (List<Category>) categoryList) {
						if (viewableCats.contains(category.getId())) {
							tmpCatList.add(category);
						}
					}
					categoryList = tmpCatList;
				}

				// first, we deal with the categories and their associated assignments
				if (!categoryList.isEmpty()) {
					Comparator catComparator = null;
					if (SORT_BY_POINTS_EARNED.equals(this.sortColumn)) {
						// need to figure out the weight for the student before you can order by this:
						final Iterator catIter = categoryList.iterator();
						while (catIter.hasNext()) {
							final Object catObject = catIter.next();
							if (catObject instanceof Category) {
								final Category category = (Category) catObject;
								final List catAssign = category.getAssignmentList();
								if (catAssign != null && !catAssign.isEmpty()) {
									// we want to create the grade rows for these assignments
									category.calculateStatisticsPerStudent(gradeRecords, this.studentUid);
								}
							}
						}
						catComparator = Category.averageScoreComparator;
					} else if (Category.SORT_BY_WEIGHT.equals(this.sortColumn)) {
						catComparator = Category.weightComparator;
					} else if (Category.SORT_BY_NAME.equals(this.sortColumn)) {
						catComparator = Category.nameComparator;
					}
					if (catComparator != null) {
						Collections.sort(categoryList, catComparator);
						if (!this.sortAscending) {
							Collections.reverse(categoryList);
						}
					}

					final Iterator catIter = categoryList.iterator();
					while (catIter.hasNext()) {
						final Object catObject = catIter.next();
						if (catObject instanceof Category) {
							final Category category = (Category) catObject;
							this.gradebookItems.add(category);
							final List catAssign = category.getAssignmentList();
							if (catAssign != null && !catAssign.isEmpty()) {
								// we want to create the grade rows for these assignments
								final List gradeRows = retrieveGradeRows(catAssign, gradeRecords);
								category.calculateStatisticsPerStudent(gradeRecords, this.studentUid);
								if (gradeRows != null && !gradeRows.isEmpty()) {
									this.gradebookItems.addAll(gradeRows);
								}
							}
						}
					}
				}

				// now we need to grab all of the assignments w/o a category
				if (!isUserAbleToGradeAll() && (isUserHasGraderPermissions()
						&& !getGradebookPermissionService().getPermissionForUserForAllAssignmentForStudent(getGradebookId(), getUserUid(),
								this.studentUid, getViewableSectionIds()))) {
					// user is not authorized to view/grade the unassigned category for the current student
				} else {
					final List assignNoCat = getGradebookManager().getAssignmentsWithNoCategory(getGradebookId(),
							GradebookAssignment.DEFAULT_SORT, true);
					if (assignNoCat != null && !assignNoCat.isEmpty()) {
						final Category unassignedCat = new Category();
						unassignedCat.setGradebook(this.gradebook);
						unassignedCat.setName(getLocalizedString("cat_unassigned"));
						unassignedCat.setAssignmentList(assignNoCat);

						// add this category to our list
						this.gradebookItems.add(unassignedCat);

						// now create grade rows for the unassigned assignments
						final List gradeRows = retrieveGradeRows(assignNoCat, gradeRecords);
						/*
						 * we display N/A for the category avg for unassigned category, so don't need to calc this anymore if
						 * (!getWeightingEnabled()) unassignedCat.calculateStatisticsPerStudent(gradeRecords, studentUid);
						 */
						if (gradeRows != null && !gradeRows.isEmpty()) {
							this.gradebookItems.addAll(gradeRows);
						}
					}
				}

			} else {
				// there are no categories, so we will be returning a list of grade rows
				final List assignList = getGradebookManager().getAssignments(getGradebookId());
				if (assignList != null && !assignList.isEmpty()) {
					final List gradeRows = retrieveGradeRows(assignList, gradeRecords);
					if (gradeRows != null && !gradeRows.isEmpty()) {
						this.gradebookItems.addAll(gradeRows);
					}
				}
			}

			for (final Iterator iter = this.gradebookItems.iterator(); iter.hasNext();) {
				final Object gradebookItem = iter.next();
				if (gradebookItem instanceof AssignmentGradeRow) {
					final AssignmentGradeRow gr = (AssignmentGradeRow) gradebookItem;
					if (gr.getAssociatedAssignment().isExternallyMaintained()) {
						this.rowStyles.append("external");
						this.anyExternallyMaintained = true;
					} else {
						this.rowStyles.append("internal");
					}

					// if scoring agent is enabled for this gradebook
					if (isScoringAgentEnabled()) {
						// check to see if a ScoringComponent has been associated
						// with this gradebook item
						if (getScoringAgentManager()
								.isScoringComponentEnabledForGbItem(getGradebookUid(), gr.getAssociatedAssignment().getId())) {
							gr.setScoringComponentEnabled(true);

							// if the user can grade, add the grading url. otherwise, view only
							String url;
							final Long gbItemId = gr.getAssociatedAssignment().getId();
							if (gr.isUserCanGrade() && this.isInstructorView) {
								url = getScoringAgentManager()
										.getScoreStudentUrl(getGradebookUid(), gbItemId, this.studentUid);
								gr.setRetrieveScoreUrl(getScoringAgentManager().getScoreUrl(getGradebookUid(), gbItemId, this.studentUid));
							} else {
								// show the view-only score in the scoring component for this student
								url = getScoringAgentManager()
										.getViewStudentScoreUrl(getGradebookUid(), gbItemId, this.studentUid);
							}

							gr.setScoringComponentUrl(url);
						} else {
							gr.setScoringComponentEnabled(false);
						}
					}
				}

				if (iter.hasNext()) {
					this.rowStyles.append(",");
				}
			}
		}
	}
}
