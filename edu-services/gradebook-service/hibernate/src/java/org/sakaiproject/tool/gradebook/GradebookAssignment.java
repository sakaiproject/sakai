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

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.service.gradebook.shared.GradebookException;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import lombok.extern.slf4j.Slf4j;

/**
 * An GradebookAssignment is the basic unit that composes a gradebook. It represents a single unit that, when aggregated in a gradebook, can
 * be used as the denomenator in calculating a CourseGradeRecord.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class GradebookAssignment extends GradableObject {

	/**
	 *
	 */
	private static final long serialVersionUID = -526126234461183210L;
	/**
	 * use the SortType enum instead. This remains only for the gradebook tool
	 */
	@Deprecated
	public static String SORT_BY_DATE = "dueDate";
	@Deprecated
	public static String SORT_BY_NAME = "name";
	@Deprecated
	public static String SORT_BY_MEAN = "mean";
	@Deprecated
	public static String SORT_BY_POINTS = "pointsPossible";
	@Deprecated
	public static String SORT_BY_RELEASED = "released";
	@Deprecated
	public static String SORT_BY_COUNTED = "counted";
	@Deprecated
	public static String SORT_BY_EDITOR = "gradeEditor";
	@Deprecated
	public static String SORT_BY_SORTING = "sorting";
	@Deprecated
	public static String DEFAULT_SORT = SORT_BY_SORTING;

	public static String item_type_points = "Points";
	public static String item_type_percentage = "Percentage";
	public static String item_type_letter = "Letter Grade";
	public static String item_type_nonCalc = "Non-calculating";
	public static String item_type_adjustment = "Adjustment";

	public static Comparator dateComparator;
	public static Comparator nameComparator;
	public static Comparator pointsComparator;
	public static Comparator meanComparator;
	public static Comparator releasedComparator;
	public static Comparator countedComparator;
	public static Comparator gradeEditorComparator;
	public static Comparator categoryComparator;

	// In a table per class hierarchy a subclass cannot have NOT NULL constraints so don't use primitives!
	private Double pointsPossible;
	private Date dueDate;
	private Boolean notCounted;
	private Boolean externallyMaintained;
	private String externalStudentLink;
	private String externalInstructorLink;
	private String externalId;
	private String externalAppName;
	private Boolean released;
	private Category category;
	private Double averageTotal;
	private Boolean ungraded;
	private Boolean extraCredit = Boolean.FALSE;
	private Double assignmentWeighting;
	private Boolean countNullsAsZeros;
	private String itemType;
	public String selectedGradeEntryValue;
	private Boolean hideInAllGradesTable = Boolean.FALSE;

	static {
		dateComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by date");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				// Sort by name if no date on either
				if (one.getDueDate() == null && two.getDueDate() == null) {
					return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
				}
				// Null dates are last
				if (one.getDueDate() == null) {
					return 1;
				}
				if (two.getDueDate() == null) {
					return -1;
				}
				// Sort by name if both assignments have the same date
				final int comp = (one.getDueDate().compareTo(two.getDueDate()));
				if (comp == 0) {
					return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.dateComparator";
			}
		};
		nameComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;
				return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
			}

			@Override
			public String toString() {
				return "GradebookAssignment.nameComparator";
			}
		};
		pointsComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by points");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				final int comp = one.getPointsPossible().compareTo(two.getPointsPossible());
				if (comp == 0) {
					return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.pointsComparator";
			}
		};
		meanComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by mean");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				final Double mean1 = one.getMean();
				final Double mean2 = two.getMean();
				if (mean1 == null && mean2 == null) {
					return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
				}
				if (mean1 != null && mean2 == null) {
					return 1;
				}
				if (mean1 == null && mean2 != null) {
					return -1;
				}
				final int comp = mean1.compareTo(mean2);
				if (comp == 0) {
					return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.meanComparator";
			}
		};

		releasedComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by release");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				final int comp = String.valueOf(one.isReleased()).compareTo(String.valueOf(two.isReleased()));
				if (comp == 0) {
					return one.getName().compareTo(two.getName());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.releasedComparator";
			}
		};

		countedComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by counted");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				final int comp = String.valueOf(one.isCounted()).compareTo(String.valueOf(two.isCounted()));
				if (comp == 0) {
					return one.getName().compareTo(two.getName());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.countedComparator";
			}
		};

		gradeEditorComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by grade editor");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				final int comp = String.valueOf(one.getExternalAppName()).compareTo(String.valueOf(two.getExternalAppName()));
				if (comp == 0) {
					return one.getName().compareTo(two.getName());
				} else {
					return comp;
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.gradeEditorComparator";
			}
		};

		categoryComparator = new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				if (log.isDebugEnabled()) {
					log.debug("Comparing assignment + " + o1 + " to " + o2 + " by category ordering");
				}
				final GradebookAssignment one = (GradebookAssignment) o1;
				final GradebookAssignment two = (GradebookAssignment) o2;

				// if categories are null
				if (one.getCategory() == null && two.getCategory() == null) {
					// sort by assignment sort order
					if (one.getSortOrder() == null && two.getSortOrder() == null) {
						// if no sortOrder, then sort based on id
						return one.getId().compareTo(two.getId());
					} else if (one.getSortOrder() == null) {
						return 1;
					} else if (two.getSortOrder() == null) {
						return -1;
					} else {
						return one.getSortOrder().compareTo(two.getSortOrder());
					}
				} else if (one.getCategory() == null) {
					return 1;
				} else if (two.getCategory() == null) {
					return -1;
				}

				// if in the same category, sort by their categorized sort order
				if (one.getCategory().equals(two.getCategory())) {
					// handles null orders by putting them at the end of the list
					if (one.getCategorizedSortOrder() == null) {
						return 1;
					} else if (two.getCategorizedSortOrder() == null) {
						return -1;
					}
					return Integer.compare(one.getCategorizedSortOrder(), two.getCategorizedSortOrder());

					// otherwise, sort by their category order
				} else {
					// check if category has a order (not required)
					if (one.getCategory().getCategoryOrder() == null && two.getCategory().getCategoryOrder() == null) {
						// both orders are null.. so order by A-Z
						if (one.getCategory().getName() == null && two.getCategory().getName() == null) {
							// both names are null so order by id
							return one.getCategory().getId().compareTo(two.getCategory().getId());
						} else if (one.getCategory().getName() == null) {
							return 1;
						} else if (two.getCategory().getName() == null) {
							return -1;
						} else {
							return one.getCategory().getName().compareTo(two.getCategory().getName());
						}
					} else if (one.getCategory().getCategoryOrder() == null) {
						return 1;
					} else if (two.getCategory().getCategoryOrder() == null) {
						return -1;
					} else {
						return one.getCategory().getCategoryOrder().compareTo(two.getCategory().getCategoryOrder());
					}
				}
			}

			@Override
			public String toString() {
				return "GradebookAssignment.categoryComparator";
			}
		};
	}

	public GradebookAssignment() {
		this(null, null, null, null, false);
	}

	public GradebookAssignment(final Gradebook gradebook, final String name, final Double pointsPossible, final Date dueDate) {
		this(gradebook, name, pointsPossible, dueDate, true);
	}

	/**
	 * constructor to support selective release
	 * 
	 * @param gradebook
	 * @param name
	 * @param pointsPossible
	 * @param dueDate
	 * @param released
	 */
	public GradebookAssignment(final Gradebook gradebook, final String name, final Double pointsPossible, final Date dueDate,
			final boolean released) {
		this.gradebook = gradebook;
		this.name = name;
		this.pointsPossible = pointsPossible;
		this.dueDate = dueDate;
		this.released = released;
		this.extraCredit = Boolean.FALSE;
		this.hideInAllGradesTable = Boolean.FALSE;
	}

	/**
	 */
	@Override
	public boolean isCourseGrade() {
		return false;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.GradableObject#isAssignment()
	 */
	@Override
	public boolean isAssignment() {
		return true;
	}

	/**
	 * @see GradableObject#getIsCategory()
	 */
	@Override
	public boolean getIsCategory() {
		return false;
	}

	/**
	 */
	public Date getDateForDisplay() {
		return this.dueDate;
	}

	/**
	 * @return Returns the dueDate.
	 */
	public Date getDueDate() {
		return this.dueDate;
	}

	/**
	 * @param dueDate The dueDate to set.
	 */
	public void setDueDate(final Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 */
	public boolean isNotCounted() {
		return this.notCounted != null ? this.notCounted : false;
	}

	/**
	 */
	public void setNotCounted(final boolean notCounted) {
		this.notCounted = notCounted;
	}

	/**
	 */
	public boolean isCounted() {
		return !isNotCounted();
	}

	/**
	 * This cover is for the benefit of JSF checkboxes.
	 */
	public void setCounted(final boolean counted) {
		setNotCounted(!counted);
	}

	/**
	 * @return Returns the externalInstructorLink.
	 */
	public String getExternalInstructorLink() {
		return this.externalInstructorLink;
	}

	/**
	 * @param externalInstructorLink The externalInstructorLink to set.
	 */
	public void setExternalInstructorLink(final String externalInstructorLink) {
		this.externalInstructorLink = externalInstructorLink;
	}

	/**
	 * @return Returns the externallyMaintained.
	 */
	public boolean isExternallyMaintained() {
		return this.externallyMaintained != null ? this.externallyMaintained : false;
	}

	/**
	 * @param externallyMaintained The externallyMaintained to set.
	 */
	public void setExternallyMaintained(final boolean externallyMaintained) {
		this.externallyMaintained = externallyMaintained;
	}

	/**
	 * @return Returns the externalStudentLink.
	 */
	public String getExternalStudentLink() {
		return this.externalStudentLink;
	}

	/**
	 * @param externalStudentLink The externalStudentLink to set.
	 */
	public void setExternalStudentLink(final String externalStudentLink) {
		this.externalStudentLink = externalStudentLink;
	}

	/**
	 * @return Returns the pointsPossible.
	 */
	public Double getPointsPossible() {
		return this.pointsPossible;
	}

	/**
	 * @param pointsPossible The pointsPossible to set.
	 */
	public void setPointsPossible(final Double pointsPossible) {
		this.pointsPossible = pointsPossible;
	}

	/**
	 * @return Returns the externalId.
	 */
	public String getExternalId() {
		return this.externalId;
	}

	/**
	 * @param externalId The externalId to set.
	 */
	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	/**
	 * @return Returns the externalAppName.
	 */
	public String getExternalAppName() {
		return this.externalAppName;
	}

	/**
	 * @param externalAppName The externalAppName to set.
	 */
	public void setExternalAppName(final String externalAppName) {
		this.externalAppName = externalAppName;
	}

	/**
	 *
	 * @return selective release true or false
	 */

	public boolean isReleased() {
		return this.released != null ? this.released : false;
	}

	/**
	 *
	 * @param released returns wther the assignment has been released to users
	 */
	public void setReleased(final boolean released) {
		this.released = released;
	}

	/**
	 * Calculate the mean score for students with entered grades.
	 */
	public void calculateStatistics(final Collection<AssignmentGradeRecord> gradeRecords) {
		int numScored = 0;
		BigDecimal total = new BigDecimal("0");
		BigDecimal pointsTotal = new BigDecimal("0");
		for (final AssignmentGradeRecord record : gradeRecords) {
			// Skip grade records that don't apply to this gradable object
			if (!record.getGradableObject().equals(this)) {
				continue;
			}

			if (record.getDroppedFromGrade() == null) {
				throw new GradebookException("record.droppedFromGrade cannot be null");
			}

			Double score = null;
			if (!getUngraded() && this.pointsPossible > 0) {
				score = record.getGradeAsPercentage();
			}
			final Double points = record.getPointsEarned();
			if (score == null && points == null || record.getDroppedFromGrade()) {
				continue;
			} else if (score == null) {
				pointsTotal = pointsTotal.add(new BigDecimal(points.toString()));
				numScored++;
			} else {
				total = total.add(new BigDecimal(score.toString()));
				pointsTotal = pointsTotal.add(new BigDecimal(points.toString()));
				numScored++;
			}
		}
		if (numScored == 0) {
			this.mean = null;
			this.averageTotal = null;
		} else {
			final BigDecimal bdNumScored = new BigDecimal(numScored);
			if (!getUngraded() && this.pointsPossible > 0) {
				this.mean = Double.valueOf(total.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
			} else {
				this.mean = null;
			}
			this.averageTotal = Double.valueOf(pointsTotal.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
		}
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(final Category category) {
		this.category = category;
	}

	public Double getAverageTotal() {
		return this.averageTotal;
	}

	public void setAverageTotal(final Double averageTotal) {
		this.averageTotal = averageTotal;
	}

	public boolean getUngraded() {
		return this.ungraded != null ? this.ungraded : false;
	}

	public void setUngraded(final boolean ungraded) {
		this.ungraded = ungraded;
	}

	// these two functions are needed to keep the old API and help JSF and RSF play nicely together. Since isExtraCredit already exists and
	// we can't remove it
	// and JSF expects Boolean values to be "getExtraCredit", this had to be added for JSF. Also, since the external GB create item page is
	// in
	// RSF, you can't name it getExtraCredit and keep isExtraCredit b/c of SAK-14589
	public Boolean getIsExtraCredit() {
		return isExtraCredit();
	}

	public void setIsExtraCredit(final Boolean isExtraCredit) {
		setExtraCredit(isExtraCredit);
	}

	public Boolean isExtraCredit() {
		return this.extraCredit != null ? this.extraCredit : false;
	}

	public void setExtraCredit(final Boolean isExtraCredit) {
		this.extraCredit = isExtraCredit;
	}

	public Double getAssignmentWeighting() {
		return this.assignmentWeighting;
	}

	public void setAssignmentWeighting(final Double assignmentWeighting) {
		this.assignmentWeighting = assignmentWeighting;
	}

	public String getItemType() {
		final Gradebook gb = getGradebook();
		if (gb != null) {
			if (isExtraCredit() != null) {
				if (isExtraCredit()) {
					// if we made it in here, go ahead and return since adjustment item takes priority over the rest
					this.itemType = item_type_adjustment;
					return this.itemType;
				}
			}

			if (getUngraded()) {
				// if we made it in here, go ahead and return since non-calc item takes priority over the rest
				this.itemType = item_type_nonCalc;
				return this.itemType;
			}

			if (gb.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
				this.itemType = item_type_points;
			} else if (gb.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
				this.itemType = item_type_percentage;
			} else if (gb.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
				this.itemType = item_type_letter;
			}
		}
		return this.itemType;
	}

	public void setItemType(final String itemType) {
		this.itemType = itemType;
	}

	public Boolean getCountNullsAsZeros() {
		return this.countNullsAsZeros;
	}

	public void setCountNullsAsZeros(final Boolean countNullsAsZeros) {
		this.countNullsAsZeros = countNullsAsZeros;
	}

	public String getSelectedGradeEntryValue() {
		return this.selectedGradeEntryValue;
	}

	public void setSelectedGradeEntryValue(final String selectedGradeEntryValue) {
		this.selectedGradeEntryValue = selectedGradeEntryValue;
	}

	/**
	 * Convenience method for checking if the grade for the assignment should be included in calculations. This is different from just the
	 * {@link #isCounted()} method for an assignment. This method does a more thorough check using other values, such as if removed,
	 * isExtraCredit, ungraded, etc in addition to the assignment's notCounted property. Now also considers category type. If categories are
	 * configured (setting 2 or 3), uncategorised items are not counted.
	 * 
	 * @return true if grades for this assignment should be included in various calculations.
	 */
	public boolean isIncludedInCalculations() {
		boolean isIncludedInCalculations = false;
		final int categoryType = this.gradebook.getCategory_type();

		if (!this.removed &&
				!getUngraded() &&
				isCounted() &&
				(isExtraCredit() || (this.pointsPossible != null && this.pointsPossible > 0))) {
			isIncludedInCalculations = true;
		}

		if (categoryType != 1 && this.category == null) {
			isIncludedInCalculations = false;
		}

		return isIncludedInCalculations;
	}

	public boolean isHideInAllGradesTable() {
		return this.hideInAllGradesTable != null ? this.hideInAllGradesTable : false;
	}

	public void setHideInAllGradesTable(final boolean hideInAllGradesTable) {
		this.hideInAllGradesTable = hideInAllGradesTable;
	}
}
