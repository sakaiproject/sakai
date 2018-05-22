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

import lombok.Data;
import lombok.Value;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

import org.apache.wicket.Component;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradingType;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GbGradebookData {

	private final int NULL_SENTINEL = 127;

	@Data
	private class StudentDefinition {
		private String eid;
		private String userId;
		private String firstName;
		private String lastName;

		private String hasComments;
		private String hasConcurrentEdit;
		private String readonly;

		private String studentNumber;
		private String hasDroppedScores;
	}

	private interface ColumnDefinition {
		public String getType();

		public Score getValueFor(GbStudentGradeInfo studentGradeInfo, boolean isInstructor);
	}

	@Value
	private class AssignmentDefinition implements ColumnDefinition {
		private Long assignmentId;
		private String title;
		private String abbrevTitle;
		private String points;
		private String dueDate;

		private boolean isReleased;
		private boolean isIncludedInCourseGrade;
		private boolean isExtraCredit;
		private boolean isExternallyMaintained;
		private String externalId;
		private String externalAppName;
		private String externalAppIconCSS;

		private String categoryId;
		private String categoryName;
		private String categoryColor;
		private String categoryWeight;
		private boolean isCategoryExtraCredit;

		private boolean hidden;

		@Override
		public String getType() {
			return "assignment";
		}

		@Override
		public Score getValueFor(final GbStudentGradeInfo studentGradeInfo, final boolean isInstructor) {
			final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();

			final GbGradeInfo gradeInfo = studentGrades.get(assignmentId);

			if (gradeInfo == null) {
				return new ReadOnlyScore(null);
			} else {
				final String grade = gradeInfo.getGrade();

				if (isInstructor || gradeInfo.isGradeable()) {
					return new EditableScore(grade);
				} else {
					return new ReadOnlyScore(grade);
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
		private boolean isExtraCredit;
		private String color;
		private boolean hidden;
		private List<String> dropInfo;

		@Override
		public String getType() {
			return "category";
		}

		@Override
		public Score getValueFor(final GbStudentGradeInfo studentGradeInfo, final boolean isInstructor) {
			final Map<Long, Double> categoryAverages = studentGradeInfo.getCategoryAverages();

			final Double average = categoryAverages.get(categoryId);

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
		private Map<String, Object> settings;

		private int rowCount;
		private int columnCount;

		public DataSet(final List<StudentDefinition> students,
				final List<ColumnDefinition> columns,
				final List<String[]> courseGrades,
				final String serializedGrades,
				final Map<String, Object> settings) {
			this.students = students;
			this.columns = columns;
			this.courseGrades = courseGrades;
			this.serializedGrades = serializedGrades;
			this.settings = settings;

			this.rowCount = students.size();
			this.columnCount = columns.size();
		}
	}

	private List<StudentDefinition> students;
	private List<ColumnDefinition> columns;
	private List<GbStudentGradeInfo> studentGradeInfoList;
	private List<CategoryDefinition> categories;
	private GradebookInformation settings;
	private GradebookUiSettings uiSettings;
	private GbRole role;
	private Map<String, String> toolNameIconCSSMap;
	private String defaultIconCSS;
	private Map<String, Double> courseGradeMap;
	private boolean isStudentNumberVisible;

	private Component parent;

	public GbGradebookData(final GbGradeTableData gbGradeTableData, final Component parentComponent) {
		this.parent = parentComponent;
		this.categories = gbGradeTableData.getCategories();
		this.settings = gbGradeTableData.getGradebookInformation();
		this.uiSettings = gbGradeTableData.getUiSettings();
		this.role = gbGradeTableData.getRole();

		this.courseGradeMap = gbGradeTableData.getCourseGradeMap();

		this.isStudentNumberVisible = gbGradeTableData.isStudentNumberVisible();

		this.studentGradeInfoList = gbGradeTableData.getGrades();

		this.toolNameIconCSSMap = gbGradeTableData.getToolNameToIconCSS();
		this.defaultIconCSS = gbGradeTableData.getDefaultIconCSS();

		this.columns = loadColumns(gbGradeTableData.getAssignments());
		this.students = loadStudents(studentGradeInfoList);
	}

	public String toScript() {
		final ObjectMapper mapper = new ObjectMapper();

		final List<Score> grades = gradeList();

		// if we can't edit one of the items,
		// we need to serialize this into the data
		if (!isInstructor() && grades.stream().anyMatch(g -> !g.canEdit())) {
			int i = 0;
			for (StudentDefinition student : GbGradebookData.this.students) {
				String readonly = "";
				for (ColumnDefinition column : GbGradebookData.this.columns) {
					Score score = grades.get(i);
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
				serializeSettings());

		try {
			return mapper.writeValueAsString(dataset);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private String serializeGrades(final List<Score> gradeList) {
		if (gradeList.stream().anyMatch(score -> score.isLarge())) {
			return "json:" + serializeLargeGrades(gradeList);
		} else {
			return "packed:" + serializeSmallGrades(gradeList);
		}
	}

	private String serializeLargeGrades(final List<Score> gradeList) {
		final List<Double> scores = gradeList.stream().map(score -> score.isNull() ? -1 : score.getScore()).collect(Collectors.toList());

		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(scores);
		} catch (JsonProcessingException e) {
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

		for (Score score : gradeList) {
			if (score == null || score.isNull()) {
				// No grade set. Use a sentinel value.
				sb.appendCodePoint(NULL_SENTINEL);
				continue;
			}

			final double grade = score.getScore();

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
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> serializeSettings() {
		final Map<String, Object> result = new HashedMap();

		result.put("isCourseLetterGradeDisplayed", settings.isCourseLetterGradeDisplayed());
		result.put("isCourseAverageDisplayed", settings.isCourseAverageDisplayed());
		result.put("isCoursePointsDisplayed", settings.isCoursePointsDisplayed());
		result.put("isPointsGradeEntry", GradingType.valueOf(settings.getGradeType()).equals(GradingType.POINTS));
		result.put("isPercentageGradeEntry", GradingType.valueOf(settings.getGradeType()).equals(GradingType.PERCENTAGE));
		result.put("isCategoriesEnabled", GbCategoryType.valueOf(settings.getCategoryType()) != GbCategoryType.NO_CATEGORY);
		result.put("isCategoryTypeWeighted", GbCategoryType.valueOf(settings.getCategoryType()) == GbCategoryType.WEIGHTED_CATEGORY);
		result.put("isStudentOrderedByLastName", uiSettings.getNameSortOrder() == GbStudentNameSortOrder.LAST_NAME);
		result.put("isStudentOrderedByFirstName", uiSettings.getNameSortOrder() == GbStudentNameSortOrder.FIRST_NAME);
		result.put("isGroupedByCategory", uiSettings.isGroupedByCategory());
		result.put("isCourseGradeReleased", settings.isCourseGradeDisplayed());
		result.put("showPoints", uiSettings.getShowPoints());
		result.put("instructor", isInstructor());
		result.put("isStudentNumberVisible", this.isStudentNumberVisible);

		return result;
	};

	private List<String[]> courseGrades() {
		final List<String[]> result = new ArrayList<>();

		final Map<String, Double> gradeMap = settings.getSelectedGradingScaleBottomPercents();
		final List<String> ascendingGrades = new ArrayList<>(gradeMap.keySet());
		ascendingGrades.sort(new Comparator<String>() {
			@Override
			public int compare(final String a, final String b) {
				return new CompareToBuilder()
						.append(gradeMap.get(a), gradeMap.get(b))
						.toComparison();
			}
		});

		for (GbStudentGradeInfo studentGradeInfo : GbGradebookData.this.studentGradeInfoList) {
			// String[0] = A+ (95%) [133/140] -- display string
			// String[1] = 95 -- raw percentage for sorting
			// String[2] = 1 -- '1' if an override, '0' if calculated
			final String[] gradeData = new String[3];

			final GbCourseGrade gbCourseGrade = studentGradeInfo.getCourseGrade();
			final CourseGrade courseGrade = gbCourseGrade.getCourseGrade();

			gradeData[0] = gbCourseGrade.getDisplayString();

			if (StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
				gradeData[2] = "1";
			} else {
				gradeData[2] = "0";
			}

			if (StringUtils.isNotBlank(courseGrade.getEnteredGrade())) {
				Double mappedGrade = courseGradeMap.get(courseGrade.getEnteredGrade());
				if (mappedGrade == null) {
					mappedGrade = new Double(0);
				}
				gradeData[1] = FormatHelper.formatGradeForDisplay(mappedGrade);
			} else {
				if (courseGrade.getPointsEarned() == null) {
					gradeData[1] = "0";
				} else {
					gradeData[1] = FormatHelper.formatGradeForDisplay(courseGrade.getCalculatedGrade());
				}
			}

			result.add(gradeData);
		}

		return result;
	}

	private List<Score> gradeList() {
		final List<Score> result = new ArrayList<>();

		for (GbStudentGradeInfo studentGradeInfo : GbGradebookData.this.studentGradeInfoList) {
			for (ColumnDefinition column : GbGradebookData.this.columns) {
				final Score grade = column.getValueFor(studentGradeInfo, isInstructor());
				result.add(grade);
			}
		}

		return result;

	}

	private String getString(final String key) {
		return parent.getString(key);
	}

	private List<StudentDefinition> loadStudents(final List<GbStudentGradeInfo> studentInfo) {
		final List<StudentDefinition> result = new ArrayList<>();

		for (GbStudentGradeInfo student : studentInfo) {
			final StudentDefinition studentDefinition = new StudentDefinition();
			studentDefinition.setEid(student.getStudentEid());
			studentDefinition.setUserId(student.getStudentUuid());
			studentDefinition.setFirstName(student.getStudentFirstName());
			studentDefinition.setLastName(student.getStudentLastName());
			studentDefinition.setHasComments(formatColumnFlags(student, g -> StringUtils.isNotBlank(g.getGradeComment())));
			studentDefinition.setHasDroppedScores(formatColumnFlags(student, g -> g.isDroppedFromCategoryScore()));

			if (this.isStudentNumberVisible) {
				studentDefinition.setStudentNumber(student.getStudentNumber());
			}

			// The JavaScript will ultimately set this when it detects
			// concurrent edits. Initialize to zeroo.
			final StringBuilder zeroes = new StringBuilder();
			for (ColumnDefinition column : GbGradebookData.this.columns) {
				zeroes.append("0");
			}
			studentDefinition.setHasConcurrentEdit(zeroes.toString());

			result.add(studentDefinition);
		}

		return result;
	}

	private List<ColumnDefinition> loadColumns(final List<Assignment> assignments) {
		final GradebookUiSettings userSettings = ((GradebookPage) parent.getPage()).getUiSettings();

		final List<ColumnDefinition> result = new ArrayList<>();

		if (assignments.isEmpty()) {
			return result;
		}

		for (int i = 0; i < assignments.size(); i++) {
			final Assignment a1 = assignments.get(i);
			final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;

			String categoryWeight = null;
			if (a1.getWeight() != null) {
				categoryWeight = FormatHelper.formatDoubleAsPercentage(a1.getWeight() * 100);
			}

			boolean counted = a1.isCounted();
			// An assignment is not counted if uncategorised and the categories are enabled
			if ((GbCategoryType.valueOf(settings.getCategoryType()) != GbCategoryType.NO_CATEGORY) &&
					a1.getCategoryId() == null) {
				counted = false;
			}
			result.add(new AssignmentDefinition(a1.getId(),
					a1.getName(),
					FormatHelper.abbreviateMiddle(a1.getName()),
					FormatHelper.formatDoubleToDecimal(a1.getPoints()),
					FormatHelper.formatDate(a1.getDueDate(), getString("label.studentsummary.noduedate")),

					a1.isReleased(),
					counted,
					a1.isExtraCredit(),
					a1.isExternallyMaintained(),
					a1.getExternalId(),
					a1.getExternalAppName(),
					getIconCSSForExternalAppName(a1.getExternalAppName()),

					nullable(a1.getCategoryId()),
					a1.getCategoryName(),
					userSettings.getCategoryColor(a1.getCategoryName(), a1.getCategoryId()),
					nullable(categoryWeight),
					a1.isCategoryExtraCredit(),

					!uiSettings.isAssignmentVisible(a1.getId())));

			// If we're at the end of the assignment list, or we've just changed
			// categories, put out a total.
			if (userSettings.isGroupedByCategory() &&
					a1.getCategoryId() != null &&
					(a2 == null || !a1.getCategoryId().equals(a2.getCategoryId()))) {
				result.add(new CategoryAverageDefinition(a1.getCategoryId(),
						a1.getCategoryName(),
						(new StringResourceModel("label.gradeitem.categoryaverage", null, new Object[] { a1.getCategoryName() }))
								.getString(),
						nullable(categoryWeight),
						a1.isCategoryExtraCredit(),
						userSettings.getCategoryColor(a1.getCategoryName(), a1.getCategoryId()),
						!uiSettings.isCategoryScoreVisible(a1.getCategoryName()),
						FormatHelper.formatCategoryDropInfo(categories.stream()
								.filter(c -> c.getId().equals(a1.getCategoryId()))
								.findAny().orElse(null))));
			}
		}

		// if group by categories is disabled, then show all catagory scores
		// at the end of the table
		if (!userSettings.isGroupedByCategory()) {
			for (CategoryDefinition category : categories) {
				if (!category.getAssignmentList().isEmpty()) {
					String categoryWeight = null;
					if (category.getWeight() != null) {
						categoryWeight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
					}
					result.add(new CategoryAverageDefinition(
							category.getId(),
							category.getName(),
							(new StringResourceModel("label.gradeitem.categoryaverage", null, new Object[] { category.getName() }))
									.getString(),
							nullable(categoryWeight),
							category.isExtraCredit(),
							userSettings.getCategoryColor(category.getName(), category.getId()),
							!uiSettings.isCategoryScoreVisible(category.getName()),
							FormatHelper.formatCategoryDropInfo(category)));
				}
			}
		}

		return result;
	}

	private String formatColumnFlags(final GbStudentGradeInfo student, Predicate<GbGradeInfo> predicate) {
		final StringBuilder sb = new StringBuilder();

		for (ColumnDefinition column : columns) {
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
		return GbRole.INSTRUCTOR.equals(role);
	}

	private abstract class Score {
		private String score;

		public Score(final String score) {
			this.score = score;
		}

		abstract boolean canEdit();

		// We assume you'll check isNull() prior to calling this
		public double getScore() {
			return Double.valueOf(score);
		};

		public boolean isNull() {
			return score == null;
		}

		public boolean isLarge() {
			return score != null && Double.valueOf(score) > 16384;
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
		if (toolNameIconCSSMap.containsKey(externalAppName)) {
			return toolNameIconCSSMap.get(externalAppName);
		}

		return defaultIconCSS;
	}
}
