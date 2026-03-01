/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.util.ResourceLoader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.Value;

public class GbGradebookData {

	private final int NULL_SENTINEL = 127;

	private static final String SAK_PROP_SHOW_SET_ZERO_SCORE = "gradebookng.showSetZeroScore";
	private static final boolean SAK_PROP_SHOW_SET_ZERO_SCORE_DEFAULT = true;

	private static final String SAK_PROP_SHOW_COURSE_GRADE_STUDENT = "gradebookng.showDisplayCourseGradeToStudent";
	private static final Boolean SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT = true;

	private static ResourceLoader i18n = new ResourceLoader("gradebookng");

	private final List<StudentDefinition> students;
	private final List<ColumnDefinition> columns;
	private final Long courseGradeId;
	private final Long gradebookId;
	private final List<GbStudentGradeInfo> studentGradeInfoList;
	private final List<CategoryDefinition> categories;
	private final GradebookInformation settings;
	private final GradebookUiSettings uiSettings;
	private final GbRole role;
	private final boolean isUserAbleToEditAssessments;
	private final Map<String, String> toolNameIconCSSMap;
	private final String defaultIconCSS;
	private final Map<String, Double> courseGradeMap;
	private final Map<String, Boolean> hasAssociatedRubricMap;
	private final boolean isStudentNumberVisible;
	private final boolean isSectionsVisible;
	private final String gUid;
	private final Map<Long, CategoryDefinition> categoryMap = new HashMap<>();

	private final Component parent;
	private final GradingService gradingService;

	@Data
	private class StudentDefinition {
		private String eid;
		private String userId;
		private String firstName;
		private String lastName;

		private String hasComments;
		private String hasConcurrentEdit;
		private String readonly;
		private String hasExcuse;
		private String hasCourseGradeComment;
		private String studentNumber;
		private String hasDroppedScores;
		private List<String> sections;
	}

	private interface ColumnDefinition {
		public String getType();

		public Score getValueFor(GbStudentGradeInfo studentGradeInfo, boolean isUserAbleToEditAssessments);
	}

	@Value
	private class AssignmentDefinition implements ColumnDefinition {
		private Long assignmentId;
		private String title;
		private String abbrevTitle;
		private String points;
		private String letter;
		private String dueDate;

		private boolean isReleased;
		private boolean isIncludedInCourseGrade;
		private boolean isExtraCredit;
		private boolean isExternallyMaintained;
		private boolean hasAssociatedRubric;
		private String externalId;
		private String externalAppName;
		private String externalToolTitle;
		private String externalAppIconCSS;

		private String categoryId;
		private String categoryName;
		private String categoryColor;
		private String categoryWeight;
		private boolean isCategoryExtraCredit;
		private boolean isCategoryEqualWeight;

		private boolean hidden;

		@Override
		public String getType() {
			return "assignment";
		}

		@Override
		public Score getValueFor(final GbStudentGradeInfo studentGradeInfo, final boolean isUserAbleToEditAssessments) {
			final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();

			final GbGradeInfo gradeInfo = studentGrades.get(this.assignmentId);

			if (gradeInfo == null) {
				return new ReadOnlyScore(null);
			} else {
				final String grade = gradeInfo.getGrade();
				final boolean excused = gradeInfo.isExcused();

				if (isUserAbleToEditAssessments || gradeInfo.isGradeable()) {
				    final EditableScore score = new EditableScore(grade);
				    score.setExcused(excused);
					return score;
				} else {
				    final ReadOnlyScore score = new ReadOnlyScore(grade);
				    score.setExcused(excused);
					return score;
				}
			}
		}
	}

	@Value
	private class CategoryAverageDefinition implements ColumnDefinition {
		private Long categoryId;
		private String categoryName;
		private String title;
		private String weight;
		private Double totalPoints;
		private boolean getExtraCredit;
		private boolean isEqualWeight;
		private String color;
		private boolean hidden;
		private List<String> dropInfo;

		@Override
		public String getType() {
			return "category";
		}

		@Override
		public Score getValueFor(final GbStudentGradeInfo studentGradeInfo, final boolean isUserAbleToEditAssessments) {
			final Map<Long, Double> categoryAverages = studentGradeInfo.getCategoryAverages();

			final Double average = categoryAverages.get(this.categoryId);

			if (average == null) {
				return new ReadOnlyScore(null);
			} else {
				return new ReadOnlyScore(FormatHelper.formatDoubleToDecimal(average));
			}
		}
	}

	@Value
	private class DataSet {
		private List<StudentDefinition> students;
		private List<ColumnDefinition> columns;
		private List<String[]> courseGrades;
		private String serializedGrades;
		private ResourceLoader i18n;
		private Map<String, Object> settings;
		private Long courseGradeId;
		private Long gradebookId;
		private int rowCount;
		private int columnCount;

		public DataSet(final List<StudentDefinition> students,
				final List<ColumnDefinition> columns,
				final List<String[]> courseGrades,
				final String serializedGrades,
				final ResourceLoader i18n,
				final Map<String, Object> settings,
				final Long courseGradeId,
				final Long gradebookId) {
			this.students = students;
			this.columns = columns;
			this.courseGrades = courseGrades;
			this.serializedGrades = serializedGrades;
			this.i18n = i18n;
			this.settings = settings;
			this.courseGradeId = courseGradeId;
			this.gradebookId = gradebookId;
			this.rowCount = students.size();
			this.columnCount = columns.size();
		}
	}

	public GbGradebookData(final GbGradeTableData gbGradeTableData, final Component parentComponent, GradingService gradingService) {
		this.parent = parentComponent;
		this.gradingService = gradingService;
		this.categories = gbGradeTableData.getCategories();
		buildCategoryMap();

		this.settings = gbGradeTableData.getGradebookInformation();
		this.uiSettings = gbGradeTableData.getUiSettings();
		this.role = gbGradeTableData.getRole();
		this.isUserAbleToEditAssessments = gbGradeTableData.isUserAbleToEditAssessments();

		this.courseGradeMap = gbGradeTableData.getCourseGradeMap();
		this.gUid = gbGradeTableData.getGradebookUid();
		this.courseGradeId = gbGradeTableData.getCourseGradeId();
		this.gradebookId = gbGradeTableData.getGradebookId();
		this.isStudentNumberVisible = gbGradeTableData.isStudentNumberVisible();
		this.isSectionsVisible = gbGradeTableData.isSectionsVisible();

		this.studentGradeInfoList = gbGradeTableData.getGrades();

		this.toolNameIconCSSMap = gbGradeTableData.getToolNameToIconCSS();
		this.defaultIconCSS = gbGradeTableData.getDefaultIconCSS();
		this.hasAssociatedRubricMap = gbGradeTableData.getHasAssociatedRubricMap();

		this.columns = loadColumns(gbGradeTableData.getAssignments());
		this.students = loadStudents(this.studentGradeInfoList);
	}

	/**
	 * Helper to build a map of category ID to category, so we can find the correct category to be displayed in the table later
	 */
	private void buildCategoryMap() {
		this.categories.forEach(c -> {
			this.categoryMap.put(c.getId(), c);
		});
	}

	public String toScript() {
		final ObjectMapper mapper = new ObjectMapper();

		final List<Score> grades = gradeList();

		// if we can't edit one of the items,
		// we need to serialize this into the data
		if (!isUserAbleToEditAssessments() && grades.stream().anyMatch(g -> !g.canEdit())) {
			int i = 0;
			for (final StudentDefinition student : GbGradebookData.this.students) {
				String readonly = "";
				for (final ColumnDefinition column : GbGradebookData.this.columns) {
					final Score score = grades.get(i);
					readonly += score.canEdit() ? "0" : "1";
					i = i + 1;
				}
				student.setReadonly(readonly);
			}
		}

		final DataSet dataset = new DataSet(
				GbGradebookData.this.students,
				GbGradebookData.this.columns,
				courseGrades(),
				serializeGrades(grades),
				GbGradebookData.this.i18n,
				serializeSettings(),
				GbGradebookData.this.courseGradeId,
				GbGradebookData.this.gradebookId);

		try {
            // TODO: Can we serialize Booleans without the get prefix?
			return mapper.writeValueAsString(dataset);
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String serializeGrades(final List<Score> gradeList) {
		if (gradeList.stream().anyMatch(score -> !score.isPackable())) {
			return "json:" + serializeLargeGrades(gradeList);
		} else {
			return "packed:" + serializeSmallGrades(gradeList);
		}
	}

	private String serializeLargeGrades(final List<Score> gradeList) {

		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(settings.getGradeType() == GradeType.LETTER
					? gradeList.stream().map(score -> score.getLetter()).collect(Collectors.toList())
					: gradeList.stream().map(score -> score.getScore()).collect(Collectors.toList()));
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	///
	// Pack our list of scores into as little space as possible.
	//
	// Most scores will be between 0 - 100, so we store small numbers in a
	// single byte. Larger scores will be stored in two bytes, and scores with
	// fractional parts in three bytes.
	//
	// We go to all of this effort because grade tables can be very large. A
	// site with 2,000 students and 100 gradeable items will have 200,000
	// scores. Even if all of those scores were between 0 and 100, encoding
	// them as a JSON array would use around 3 bytes per score (two digits plus
	// a comma separator), for a total of 600KB. We can generally at least
	// halve that number by byte packing the numbers ourselves and unpacking
	// them using JavaScript on the client.
	//
	// Why not just use AJAX and send them a chunk at a time? The GradebookNG
	// design is built around having a single large table of all grades, and
	// firing AJAX requests on scroll events ends up being prohibitively slow.
	// Having all the data available up front helps keep the scroll performance
	// fast.
	//
	private String serializeSmallGrades(final List<Score> gradeList) {
		final StringBuilder sb = new StringBuilder();

		for (final Score score : gradeList) {
			if (score == null || score.isNull()) {
				// No grade set. Use a sentinel value.
				sb.appendCodePoint(this.NULL_SENTINEL);
				continue;
			}

			final double grade = score.getScore();

			if (grade < 0) {
			    throw new IllegalStateException("serializeSmallGrades doesn't support negative scores");
			}


			final boolean hasFraction = ((int) grade != grade);

			if (grade < 127 && !hasFraction) {
				// single byte, no fraction
				//
				// input number like 0nnnnnnn serialized as 0nnnnnnn
				sb.appendCodePoint((int) grade & 0xFF);
			} else if (grade < 16384 && !hasFraction) {
				// two byte, no fraction
				//
				// input number like 00nnnnnn nnnnnnnn serialized as 10nnnnnn nnnnnnnn
				//
				// where leading '10' means 'two bytes, no fraction part'
				sb.appendCodePoint(((int) grade >> 8) | 0b10000000);
				sb.appendCodePoint(((int) grade & 0xFF));
			} else if (grade < 16384) {
				// three byte encoding, fraction
				//
				// input number like 00nnnnnn nnnnnnnn.25 serialized as 11nnnnnn nnnnnnnn 00011001
				//
				// where leading '11' means 'two bytes plus a fraction part',
				// and the fraction part is stored as an integer between 0-99,
				// where 50 represents 0.5, 25 represents .25, etc.

				sb.appendCodePoint(((int) grade >> 8) | 0b11000000);
				sb.appendCodePoint((int) grade & 0xFF);
				sb.appendCodePoint((int) Math.round((grade * 100) - ((int) grade * 100)));
			} else {
				throw new RuntimeException("Grade too large: " + grade);
			}
		}

		try {
			return Base64.getEncoder().encodeToString(sb.toString().getBytes("ISO-8859-1"));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> serializeSettings() {
		final Map<String, Object> result = new HashedMap();

		result.put("isCourseLetterGradeDisplayed", this.settings.getCourseLetterGradeDisplayed());
		result.put("isCourseAverageDisplayed", this.settings.getCourseAverageDisplayed());
		result.put("isCoursePointsDisplayed", this.settings.getCoursePointsDisplayed());
		result.put("isPointsGradeEntry", GradeType.POINTS == this.settings.getGradeType());
		result.put("isPercentageGradeEntry", GradeType.PERCENTAGE == this.settings.getGradeType());
		result.put("isLetterGradeEntry", GradeType.LETTER == this.settings.getGradeType());
		result.put("isCategoriesEnabled", !Objects.equals(this.settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY));
		result.put("isCategoryTypeWeighted", Objects.equals(this.settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY));
		result.put("isStudentOrderedByLastName", this.uiSettings.getNameSortOrder() == GbStudentNameSortOrder.LAST_NAME);
		result.put("isStudentOrderedByFirstName", this.uiSettings.getNameSortOrder() == GbStudentNameSortOrder.FIRST_NAME);
		result.put("isGroupedByCategory", this.uiSettings.isGroupedByCategory());
		result.put("isCourseGradeReleased", this.settings.getCourseGradeDisplayed());
		result.put("getAllowStudentsToCompareGrades", this.settings.getAllowStudentsToCompareGrades());
		result.put("isComparingDisplayStudentNames", this.settings.getComparingDisplayStudentNames());
		result.put("isComparingDisplayStudentSurnames", this.settings.getComparingDisplayStudentSurnames());
		result.put("isComparingDisplayTeacherComments", this.settings.getComparingDisplayTeacherComments());
		result.put("getComparingIncludeAllGrades", this.settings.getComparingIncludeAllGrades());
		result.put("isComparingRandomizeDisplayedData", this.settings.getComparingRandomizeDisplayedData());
		result.put("showPoints", this.uiSettings.getShowPoints());
		result.put("isUserAbleToEditAssessments", isUserAbleToEditAssessments());
		result.put("isStudentNumberVisible", this.isStudentNumberVisible);
		result.put("isSectionsVisible", this.isSectionsVisible && ServerConfigurationService.getBoolean("gradebookng.showSections", true));
		result.put("isSetUngradedToZeroEnabled", ServerConfigurationService.getBoolean(SAK_PROP_SHOW_SET_ZERO_SCORE, SAK_PROP_SHOW_SET_ZERO_SCORE_DEFAULT));
		result.put("isShowDisplayCourseGradeToStudentEnabled", ServerConfigurationService.getBoolean(SAK_PROP_SHOW_COURSE_GRADE_STUDENT, SAK_PROP_SHOW_COURSE_GRADE_STUDENT_DEFAULT));
		result.put("letterGrades", this.settings.getSelectedGradingScaleBottomPercents().keySet());
		result.put("gUid", this.gUid);

		return result;
	};

	private List<String[]> courseGrades() {
		final List<String[]> result = new ArrayList<>();

		final Map<String, Double> gradeMap = this.settings.getSelectedGradingScaleBottomPercents();
		final List<String> ascendingGrades = new ArrayList<>(gradeMap.keySet());
		ascendingGrades.sort(new Comparator<String>() {
			@Override
			public int compare(final String a, final String b) {
				return new CompareToBuilder()
						.append(gradeMap.get(a), gradeMap.get(b))
						.toComparison();
			}
		});

		for (final GbStudentGradeInfo studentGradeInfo : studentGradeInfoList) {
			result.add(getCourseGradeData(studentGradeInfo.getCourseGrade(), courseGradeMap, settings.getGradeType()));
		}

		return result;
	}

	/**
	 * Returns the following data about the course grade, needed by the client-side grades table:
	 * String[0] = A+ (95%) [133/140] -- display string
	 * String[1] = 95 -- raw percentage for sorting
	 * String[2] = 1 -- '1' if an override, '0' if calculated
	 * @param CourseGradeTransferBean the course grade, with an appropriate display string already set
	 * @param courseGradeMap grading scale map in use
	 * @return course grade information
	 */
	public static String[] getCourseGradeData(CourseGradeTransferBean courseGrade, Map<String, Double> courseGradeMap, GradeType gradeType) {
		final String[] gradeData = new String[3];

		if (courseGrade == null) {
			gradeData[0] = "";
			gradeData[1] = "";
			gradeData[2] = "0";
		} else if (StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
			Double mappedGrade = courseGradeMap.get(courseGrade.getEnteredGrade());
			gradeData[0] = courseGrade.getDisplayString();
			gradeData[1] = FormatHelper.formatGradeForDisplay(mappedGrade, gradeType);
			gradeData[2] = "1";
		} else {
			gradeData[0] = courseGrade.getDisplayString();
			gradeData[1] = FormatHelper.formatGradeForDisplay(courseGrade.getCalculatedGrade(), gradeType);
			gradeData[2] = "0";
		}

		return gradeData;
	}

	private List<Score> gradeList() {
		final List<Score> result = new ArrayList<>();

		for (final GbStudentGradeInfo studentGradeInfo : GbGradebookData.this.studentGradeInfoList) {
			for (final ColumnDefinition column : GbGradebookData.this.columns) {
				final Score grade = column.getValueFor(studentGradeInfo, isUserAbleToEditAssessments());
				result.add(grade);
			}
		}

		return result;

	}

	private String getString(final String key) {
		return this.parent.getString(key);
	}

	private List<StudentDefinition> loadStudents(final List<GbStudentGradeInfo> studentInfo) {
		final List<StudentDefinition> result = new ArrayList<>();

		for (final GbStudentGradeInfo student : studentInfo) {
			final StudentDefinition studentDefinition = new StudentDefinition();
			studentDefinition.setEid(student.getStudentEid());
			studentDefinition.setUserId(student.getStudentUuid());
			studentDefinition.setFirstName(student.getStudentFirstName());
			studentDefinition.setLastName(student.getStudentLastName());
			studentDefinition.setHasComments(formatColumnFlags(student, g -> StringUtils.isNotBlank(g.getGradeComment())));
			studentDefinition.setHasDroppedScores(formatColumnFlags(student, g -> g.isDroppedFromCategoryScore()));
			studentDefinition.setHasExcuse(formatColumnFlags(student, g -> g.isExcused()));
			studentDefinition.setHasCourseGradeComment(student.isHasCourseGradeComment() ? "1" : "0");
			if (this.isStudentNumberVisible) {
				studentDefinition.setStudentNumber(student.getStudentNumber());
			}

			// The JavaScript will ultimately set this when it detects
			// concurrent edits. Initialize to zeroo.
			final StringBuilder zeroes = new StringBuilder();
			for (final ColumnDefinition column : GbGradebookData.this.columns) {
				zeroes.append("0");
			}
			studentDefinition.setHasConcurrentEdit(zeroes.toString());

			studentDefinition.setSections(student.getSections());

			result.add(studentDefinition);
		}

		return result;
	}

	private List<ColumnDefinition> loadColumns(final List<Assignment> assignments) {
		final GradebookUiSettings userSettings = ((GradebookPage) this.parent.getPage()).getUiSettings();

		final List<ColumnDefinition> result = new ArrayList<>();

		if (assignments.isEmpty()) {
			return result;
		}

		for (int i = 0; i < assignments.size(); i++) {
			final Assignment a1 = assignments.get(i);

			String idForRubric = a1.getExternallyMaintained() ? AssignmentReferenceReckoner.reckoner().reference(a1.getExternalId()).reckon().getId() : a1.getId().toString();

			final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;

			String categoryWeight = null;
			if (a1.getWeight() != null) {
				categoryWeight = FormatHelper.formatDoubleAsPercentage(a1.getWeight() * 100);
			}

			boolean counted = a1.getCounted();
			// An assignment is not counted if uncategorised and the categories are enabled
			if (!Objects.equals(this.settings.getCategoryType(), GradingConstants.CATEGORY_TYPE_NO_CATEGORY) && a1.getCategoryId() == null) {
				counted = false;
			}

			result.add(new AssignmentDefinition(a1.getId(),
					FormatHelper.stripLineBreaks(a1.getName()),
					a1.getName(),
					FormatHelper.formatDoubleToDecimal(a1.getPoints()),
					settings.getGradeType() == GradeType.LETTER ? gradingService.getMaxLetterGrade(courseGradeMap).orElse("N/A") : null,
					FormatHelper.formatDate(a1.getDueDate(), getString("label.studentsummary.noduedate")),
					a1.getReleased(),
					counted,
					a1.getExtraCredit(),
					a1.getExternallyMaintained(),
					this.hasAssociatedRubricMap.get(idForRubric),
					a1.getExternalId(),
					a1.getExternalAppName(),
					a1.getExternalToolTitle(),
					getIconCSSForExternalAppName(a1.getExternalAppName()),

					nullable(a1.getCategoryId()),
					a1.getCategoryName(),
					userSettings.getCategoryColor(a1.getCategoryName()),
					nullable(categoryWeight),
					a1.getCategoryExtraCredit(),
					a1.getCategoryEqualWeight(),

					!this.uiSettings.isAssignmentVisible(a1.getId())));

			// If we're at the end of the assignment list, or we've just changed
			// categories, put out a total.
			if (userSettings.isGroupedByCategory()
					&& !Objects.equals(this.settings.getCategoryType(),GradingConstants.CATEGORY_TYPE_NO_CATEGORY)
					&& a1.getCategoryId() != null
					&& (a2 == null || !a1.getCategoryId().equals(a2.getCategoryId()))) {
				result.add(new CategoryAverageDefinition(a1.getCategoryId(),
						a1.getCategoryName(),
						(new StringResourceModel("label.gradeitem.categoryaverage").setParameters(a1.getCategoryName()))
								.getString(),
						nullable(categoryWeight),
						getCategoryPoints(a1.getCategoryId()),
						BooleanUtils.toBoolean(a1.getCategoryExtraCredit()),
						BooleanUtils.toBoolean(a1.getCategoryEqualWeight()),
						userSettings.getCategoryColor(a1.getCategoryName()),
						!this.uiSettings.isCategoryScoreVisible(a1.getCategoryName()),
						FormatHelper.formatCategoryDropInfo(this.categories.stream()
								.filter(c -> c.getId().equals(a1.getCategoryId()))
								.findAny().orElse(null))));
			}
		}

		// if group by categories is disabled, then show all catagory scores
		// at the end of the table
		if (!userSettings.isGroupedByCategory()) {
			for (final CategoryDefinition category : this.categories) {
				if (!category.getAssignmentList().isEmpty()) {
					String categoryWeight = null;
					if (category.getWeight() != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
					}
					result.add(new CategoryAverageDefinition(
							category.getId(),
							category.getName(),
							(new StringResourceModel("label.gradeitem.categoryaverage").setParameters(category.getName()))
									.getString(),
							nullable(categoryWeight),
							category.getTotalPoints(settings.getGradeType(), gradingService.getMaxPoints(courseGradeMap).orElse(0D)),
							BooleanUtils.toBoolean(category.getExtraCredit()),
							BooleanUtils.toBoolean(category.getEqualWeight()),
							userSettings.getCategoryColor(category.getName()),
							!this.uiSettings.isCategoryScoreVisible(category.getName()),
							FormatHelper.formatCategoryDropInfo(category)));
				}
			}
		}

		return result;
	}

	private Double getCategoryPoints(final Long categoryId) {
		final CategoryDefinition category = this.categoryMap.get(categoryId);
		if (category != null) {
			return category.getTotalPoints(settings.getGradeType(), gradingService.getMaxPoints(courseGradeMap).orElse(0D));
		}
		return Double.valueOf(0);
	}

	private String formatColumnFlags(final GbStudentGradeInfo student, final Predicate<GbGradeInfo> predicate) {
		final StringBuilder sb = new StringBuilder();

		for (final ColumnDefinition column : this.columns) {
			if (column instanceof AssignmentDefinition) {
				final AssignmentDefinition assignmentColumn = (AssignmentDefinition) column;
				final GbGradeInfo gradeInfo = student.getGrades().get(assignmentColumn.getAssignmentId());
				if (gradeInfo != null && predicate.test(gradeInfo)) {
					sb.append('1');
				} else {
					sb.append('0');
				}
			} else {
				sb.append('0');
			}
		}

		return sb.toString();
	}


	private String nullable(final Object value) {
		if (value == null) {
			return null;
		} else {
			return value.toString();
		}
	}

	private boolean isInstructor() {
		return GbRole.INSTRUCTOR.equals(this.role);
	}

	private boolean isUserAbleToEditAssessments() {
		return this.isUserAbleToEditAssessments;
	}

	private abstract class Score {
		private final String score;
		private boolean isExcused;

		public Score(final String score) {
			this.score = score;
		}

		abstract boolean canEdit();

		public String getLetter() {
			return this.score;
		};

		public Double getScore() {
			return this.score == null ? null : Double.valueOf(this.score);
		};

		public boolean isNull() {
			return this.score == null;
		}

		public boolean isPackable() {
			if (settings.getGradeType() == GradeType.LETTER) {
				return false;
			}
			return isNull() || (Double.valueOf(this.score) >= 0 && Double.valueOf(this.score) < 16384);
		}

        public boolean isExcused() {
            return this.isExcused;
        }

        public void setExcused(final boolean excused) {
            this.isExcused = excused;
        }
    }

	private class EditableScore extends Score {
		public EditableScore(final String score) {
			super(score);
		}

		@Override
		public boolean canEdit() {
			return true;
		}
	}

	private class ReadOnlyScore extends Score {
		public ReadOnlyScore(final String score) {
			super(score);
		}

		@Override
		public boolean canEdit() {
			return false;
		}
	}

	private String getIconCSSForExternalAppName(final String externalAppName) {
		if (this.toolNameIconCSSMap.containsKey(externalAppName)) {
			return this.toolNameIconCSSMap.get(externalAppName);
		}

		return this.defaultIconCSS;
	}
}
