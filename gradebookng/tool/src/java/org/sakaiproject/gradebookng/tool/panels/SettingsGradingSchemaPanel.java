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
package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.DoubleComparator;
import org.sakaiproject.gradebookng.business.FirstNameComparator;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.user.api.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SettingsGradingSchemaPanel extends BasePanel implements IFormModelUpdateListener {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;

	WebMarkupContainer schemaWrap;
	ListView<GbGradingSchemaEntry> schemaView;
	List<GradeMappingDefinition> gradeMappings;
	private boolean expanded;
	String gradingSchemaName;

	/**
	 * This is the currently PERSISTED grade mapping id that is persisted for this gradebook
	 */
	String configuredGradeMappingId;

	/**
	 * This is the currently SELECTED grade mapping, from the dropdown
	 */
	String currentGradeMappingId;

	/**
	 * List of {@link CourseGrade} cached here as it is used by a few components
	 */
	Map<String, CourseGrade> courseGradeMap;

	/**
	 * Count of grades for the chart
	 */
	int total;

	public SettingsGradingSchemaPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get all mappings available for this gradebook
		this.gradeMappings = this.model.getObject().getGradebookInformation().getGradeMappings();

		// get current one
		this.configuredGradeMappingId = this.model.getObject().getGradebookInformation().getSelectedGradeMappingId();

		// set the value for the dropdown
		this.currentGradeMappingId = this.configuredGradeMappingId;

		// setup the grading scale schema entries
		this.model.getObject().setGradingSchemaEntries(getGradingSchemaEntries());

		// create map of grading scales to use for the dropdown
		final Map<String, String> gradeMappingMap = new LinkedHashMap<>();
		for (final GradeMappingDefinition gradeMapping : this.gradeMappings) {
			gradeMappingMap.put(gradeMapping.getId(), gradeMapping.getName());
		}

		final WebMarkupContainer settingsGradingSchemaPanel = new WebMarkupContainer("settingsGradingSchemaPanel");
		// Preserve the expand/collapse state of the panel
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsGradingSchemaPanel.this.expanded = true;
			}
		});
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				SettingsGradingSchemaPanel.this.expanded = false;
			}
		});
		if (this.expanded) {
			settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsGradingSchemaPanel);

		// grading scale type chooser
		final List<String> gradingSchemaList = new ArrayList<String>(gradeMappingMap.keySet());
		final DropDownChoice<String> typeChooser = new DropDownChoice<String>("type",
				new PropertyModel<String>(this.model, "gradebookInformation.selectedGradeMappingId"), gradingSchemaList,
				new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final String gradeMappingId) {
						return gradeMappingMap.get(gradeMappingId);
					}

					@Override
					public String getIdValue(final String gradeMappingId, final int index) {
						return gradeMappingId;
					}
				});
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(this.currentGradeMappingId);
		settingsGradingSchemaPanel.add(typeChooser);

		// render the grading schema table
		this.schemaWrap = new WebMarkupContainer("schemaWrap");
		this.schemaView = new ListView<GbGradingSchemaEntry>("schemaView",
				new PropertyModel<List<GbGradingSchemaEntry>>(this.model, "gradingSchemaEntries")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {

				final GbGradingSchemaEntry entry = item.getModelObject();

				// grade
				final Label grade = new Label("grade", new PropertyModel<String>(entry, "grade"));
				item.add(grade);

				// minpercent
				final TextField<Double> minPercent = new TextField<Double>("minPercent", new PropertyModel<Double>(entry, "minPercent"));
				item.add(minPercent);

				// when minpercent is updated, reorder the listview
				minPercent.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {

						// fetch current data from model, sort and refresh
						final List<GbGradingSchemaEntry> data = SettingsGradingSchemaPanel.this.model.getObject().getGradingSchemaEntries();
						data.sort(Collections.reverseOrder());
						SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(data);
						target.add(SettingsGradingSchemaPanel.this.schemaWrap);

					}
				});
			}
		};
		this.schemaView.setOutputMarkupId(true);
		this.schemaWrap.add(this.schemaView);
		this.schemaWrap.setOutputMarkupId(true);
		settingsGradingSchemaPanel.add(this.schemaWrap);

		// handle updates on the schema type chooser, to repaint the table
		typeChooser.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				// set current selection
				SettingsGradingSchemaPanel.this.currentGradeMappingId = (String) typeChooser.getDefaultModelObject();

				// refresh data
				SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(getGradingSchemaEntries());

				// repaint
				target.add(SettingsGradingSchemaPanel.this.schemaWrap);
			}
		});

		// get the course grade map as we are about to use it a lot
		this.courseGradeMap = getCourseGrades();

		// if there are no grades, display message instead of chart
		settingsGradingSchemaPanel
				.add(new Label("noStudentsWithGradesMessage", new ResourceModel("settingspage.gradingschema.emptychart")) {
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isVisible() {
						return SettingsGradingSchemaPanel.this.total == 0;
					}
				});

		// other stats
		// TODO this could be in a panel/fragment of its own
		final DescriptiveStatistics stats = calculateStatistics();

		settingsGradingSchemaPanel.add(new Label("averagegpa", getAverageGPA()) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return StringUtils.equals(SettingsGradingSchemaPanel.this.gradingSchemaName, "Grade Points");
			}
		});
		settingsGradingSchemaPanel.add(new Label("average", getMean(stats)));
		settingsGradingSchemaPanel.add(new Label("median", getMedian(stats)));
		settingsGradingSchemaPanel.add(new Label("lowest", getMin(stats)));
		settingsGradingSchemaPanel.add(new Label("highest", getMax(stats)));
		settingsGradingSchemaPanel.add(new Label("deviation", getStandardDeviation(stats)));
		settingsGradingSchemaPanel.add(new Label("graded", String.valueOf(this.total)));

		// if there are course grade overrides, add the list of students
		final List<GbUser> usersWithOverrides = getStudentsWithCourseGradeOverrides();
		settingsGradingSchemaPanel.add(new ListView<GbUser>("studentsWithCourseGradeOverrides", getStudentsWithCourseGradeOverrides()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbUser> item) {
				item.add(new Label("name", new PropertyModel<String>(item.getModel(), "displayName")));
			}

			@Override
			public boolean isVisible() {
				return !usersWithOverrides.isEmpty();
			}
		});

	}

	/**
	 * Helper to sort the bottom percents maps.
	 *
	 * @param percents
	 * @return
	 */
	private Map<String, Double> sortBottomPercents(final Map<String, Double> percents) {

		Map<String, Double> rval = null;

		// we only ever order by bottom percents now
		final DoubleComparator doubleComparator = new DoubleComparator(percents);
		rval = new TreeMap<String, Double>(doubleComparator);
		rval.putAll(percents);

		return rval;
	}

	/**
	 * Sync up the custom list we are using for the list view, back into the GradebookInformation object
	 */
	@Override
	public void updateModel() {

		final List<GbGradingSchemaEntry> schemaEntries = this.schemaView.getModelObject();

		final Map<String, Double> bottomPercents = new HashMap<>();
		for (final GbGradingSchemaEntry schemaEntry : schemaEntries) {
			bottomPercents.put(schemaEntry.getGrade(), schemaEntry.getMinPercent());
		}

		this.model.getObject().getGradebookInformation().setSelectedGradingScaleBottomPercents(bottomPercents);

		this.configuredGradeMappingId = this.currentGradeMappingId;
	}

	/**
	 * Helper to determine and return the applicable grading schema entries, depending on current state
	 *
	 * @return the list of {@link GbGradingSchemaEntry} for the currently selected grading schema id
	 */
	private List<GbGradingSchemaEntry> getGradingSchemaEntries() {

		// get configured values or defaults
		// need to retain insertion order
		Map<String, Double> bottomPercents = new LinkedHashMap<>();

		// note that we sort based on name so we need to pull the right name out of the list of mappings, for both cases
		this.gradingSchemaName = this.gradeMappings.stream()
				.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), this.currentGradeMappingId))
				.findFirst()
				.get()
				.getName();

		if (StringUtils.equals(this.currentGradeMappingId, this.configuredGradeMappingId)) {
			// get the values from the configured grading scale in this gradebook and sort them
			bottomPercents = sortBottomPercents(this.model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents());
		} else {
			// get the default values for the chosen grading scale and sort them
			bottomPercents = sortBottomPercents(
					this.gradeMappings.stream()
							.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), this.currentGradeMappingId))
							.findFirst()
							.get()
							.getDefaultBottomPercents());
		}

		return asList(bottomPercents);
	}

	public boolean isExpanded() {
		return this.expanded;
	}

	/**
	 * Get a List of {@link GbUser}'s with course grade overrides.
	 *
	 * @return
	 */
	private List<GbUser> getStudentsWithCourseGradeOverrides() {

		// get all users with course grade overrides
		final List<String> userUuids = this.courseGradeMap.entrySet().stream()
				.filter(c -> StringUtils.isNotBlank(c.getValue().getEnteredGrade()))
				.map(c -> c.getKey())
				.collect(Collectors.toList());

		final List<User> users = this.businessService.getUsers(userUuids);
		Collections.sort(users, new FirstNameComparator());

		final List<GbUser> rval = new ArrayList<>();
		users.forEach(u -> {
			rval.add(new GbUser(u));
		});

		return rval;
	}

	/**
	 * Get the map of userId to {@link CourseGrade} for the students in this gradebook
	 *
	 * @return
	 */
	private Map<String, CourseGrade> getCourseGrades() {

		final List<String> studentUuids = this.businessService.getGradeableUsers();
		final Map<String, CourseGrade> rval = this.businessService.getCourseGrades(studentUuids);
		return rval;
	}

	/**
	 * Calculates stats based on the calculated course grade values
	 *
	 * @return
	 */
	private DescriptiveStatistics calculateStatistics() {

		final List<Double> grades = this.courseGradeMap.values().stream().map(c -> NumberUtils.toDouble(c.getCalculatedGrade()))
				.collect(Collectors.toList());

		final DescriptiveStatistics stats = new DescriptiveStatistics();

		grades.forEach(g -> {
			stats.addValue(g);
		});

		return stats;
	}

	/**
	 * Calculates the average GPA for the course
	 *
	 * @return String average GPA
	 */
	private String getAverageGPA() {

		if (this.total < 1 && StringUtils.equals(this.gradingSchemaName, "Grade Points")) {
			return "-";
		} else if (StringUtils.equals(this.gradingSchemaName, "Grade Points")) {
			final Map<String, Double> gpaScoresMap = getGPAScoresMap();

			// get all of the non null mapped grades
			// mapped grades will be null if the student doesn't have a course grade yet.
			final List<String> mappedGrades = this.courseGradeMap.values().stream().filter(c -> c.getMappedGrade() != null)
					.map(c -> (c.getMappedGrade())).collect(Collectors.toList());
			Double averageGPA = 0.0;
			for (final String mappedGrade : mappedGrades) {
				// Note to developers. If you changed GradePointsMapping without changing gpaScoresMap, the average will be incorrect.
				// As per GradePointsMapping, both must be kept in sync
				final Double grade = gpaScoresMap.get(mappedGrade);
				if (grade != null) {
					averageGPA += grade;
				} else {
					log.debug(
							"Grade skipped when calculating course average GPA: " + mappedGrade + ". Calculated value will be incorrect.");
				}
			}
			averageGPA /= mappedGrades.size();

			return String.format("%.2f", averageGPA);
		} else {
			return null;
		}
	}

	/**
	 * Calculates the mean grade for the course
	 *
	 * @return String mean grade
	 */
	private String getMean(final DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMean()) : "-";
	}

	/**
	 * Calculates the median grade for the course
	 *
	 * @return String median grade
	 */
	private String getMedian(final DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getPercentile(50)) : "-";
	}

	/**
	 * Calculates the min grade for the course
	 *
	 * @return String min grade
	 */
	private String getMin(final DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMin()) : "-";
	}

	/**
	 * Calculates the max grade for the course
	 *
	 * @return String max grade
	 */
	private String getMax(final DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMax()) : "-";
	}

	/**
	 * Calculates the standard deviation for the course
	 *
	 * @return String standard deviation
	 */
	private String getStandardDeviation(final DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getStandardDeviation()) : "-";
	}

	/**
	 *
	 */
	private Map<String, Double> getGPAScoresMap() {
		final Map<String, Double> gpaScoresMap = new HashMap<>();
		gpaScoresMap.put("A (4.0)", Double.valueOf("4.0"));
		gpaScoresMap.put("A- (3.67)", Double.valueOf("3.67"));
		gpaScoresMap.put("B+ (3.33)", Double.valueOf("3.33"));
		gpaScoresMap.put("B (3.0)", Double.valueOf("3.0"));
		gpaScoresMap.put("B- (2.67)", Double.valueOf("2.67"));
		gpaScoresMap.put("C+ (2.33)", Double.valueOf("2.33"));
		gpaScoresMap.put("C (2.0)", Double.valueOf("2.0"));
		gpaScoresMap.put("C- (1.67)", Double.valueOf("1.67"));
		gpaScoresMap.put("D (1.0)", Double.valueOf("1.0"));
		gpaScoresMap.put("F (0)", Double.valueOf("0"));

		return gpaScoresMap;
	}

	/**
	 * Convert map into list of objects which is easier to work with in the views
	 *
	 * @param bottomPercents map
	 * @return list of {@link GbGradingSchemaEntry}
	 */
	private List<GbGradingSchemaEntry> asList(final Map<String, Double> bottomPercents) {
		final List<GbGradingSchemaEntry> rval = new ArrayList<>();
		for (final Map.Entry<String, Double> entry : bottomPercents.entrySet()) {
			rval.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}
		return rval;
	}

}
