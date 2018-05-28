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
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.FirstNameComparator;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.SettingsHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.GbCourseGradeChart;
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
	WebMarkupContainer statsWrap;
	ListView<GbGradingSchemaEntry> schemaView;
	private List<GradeMappingDefinition> gradeMappings;
	boolean expanded;
	String gradingSchemaName;
	DescriptiveStatistics statistics;
	Label modifiedSchema;
	Label unsavedSchema;
	Label duplicateEntries;

	GbCourseGradeChart chart;

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
	private Map<String, CourseGrade> courseGradeMap;

	/**
	 * Count of grades for the chart
	 */
	int total;

	/**
	 * Has the schema been modified from the default percentages?
	 */
	boolean schemaModifiedFromDefault;

	/**
	 * Are there unsaved changes?
	 */
	boolean dirty;

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

		// get the course grade map
		this.courseGradeMap = getCourseGrades();

		// get the total number of course grades
		this.total = getTotalCourseGrades(this.courseGradeMap);

		// is the schema modified from the defaults?
		this.schemaModifiedFromDefault = isModified();

		// create map of grading scales to use for the dropdown
		final Map<String, String> gradeMappingMap = new LinkedHashMap<>();
		for (final GradeMappingDefinition gradeMapping : this.gradeMappings) {
			gradeMappingMap.put(gradeMapping.getId(), gradeMapping.getName());
		}

		final WebMarkupContainer settingsGradingSchemaPanel = new WebMarkupContainer("settingsGradingSchemaPanel");
		// Preserve the expand/collapse state of the panel
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsGradingSchemaPanel.this.expanded = true;
			}
		});
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			private static final long serialVersionUID = 1L;

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

		//add warning if the schema has been modified
		this.modifiedSchema = new Label("modifiedSchema", new ResourceModel("settingspage.gradingschema.modified.note"));
		this.modifiedSchema.setVisible(SettingsGradingSchemaPanel.this.schemaModifiedFromDefault);
		this.modifiedSchema.setOutputMarkupPlaceholderTag(true);
		settingsGradingSchemaPanel.add(this.modifiedSchema);

		// add warning if the schema is dirty. hidden by default
		this.unsavedSchema = new Label("unsavedSchema", new ResourceModel("settingspage.gradingschema.modified.warning"));
		this.unsavedSchema.setVisible(false);
		this.unsavedSchema.setOutputMarkupPlaceholderTag(true);
		settingsGradingSchemaPanel.add(this.unsavedSchema);

		// warning for duplicates
		this.duplicateEntries = new Label("duplicateEntries", new ResourceModel("settingspage.gradingschema.duplicates.warning"));
		this.duplicateEntries.setVisible(false);
		this.duplicateEntries.setOutputMarkupPlaceholderTag(true);
		settingsGradingSchemaPanel.add(this.duplicateEntries);

		// render the grading schema table
		this.schemaWrap = new WebMarkupContainer("schemaWrap");
		this.schemaView = new ListView<GbGradingSchemaEntry>("schemaView",
				new PropertyModel<List<GbGradingSchemaEntry>>(this.model, "gradingSchemaEntries")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {

				final GbGradingSchemaEntry entry = item.getModelObject();

				// grade
				final TextField<Double> grade = new TextField<>("grade", new PropertyModel<Double>(entry, "grade"));
				item.add(grade);

				// minpercent
				final TextField<Double> minPercent = new TextField<>("minPercent", new PropertyModel<Double>(entry, "minPercent"));
				item.add(minPercent);

				// attach the onchange behaviours
				minPercent.add(new GradingSchemaChangeBehaviour(GradingSchemaChangeBehaviour.ONCHANGE));
				grade.add(new GradingSchemaChangeBehaviour(GradingSchemaChangeBehaviour.ONCHANGE));

				// remove button
				final AjaxButton remove = new AjaxButton("remove") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {

						// remove this entry from the model data
						final GbGradingSchemaEntry current = item.getModelObject();
						SettingsGradingSchemaPanel.this.model.getObject().getGradingSchemaEntries().remove(current);

						// repaint table
						target.add(SettingsGradingSchemaPanel.this.schemaWrap);

						// repaint chart
						refreshCourseGradeChart(target);
					}

				};
				remove.setDefaultFormProcessing(false);
				item.add(remove);
			}
		};
		this.schemaView.setOutputMarkupId(true);
		this.schemaWrap.setOutputMarkupId(true);
		this.schemaWrap.add(this.schemaView);
		settingsGradingSchemaPanel.add(this.schemaWrap);

		// handle updates on the schema type chooser
		typeChooser.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				// set current selection
				SettingsGradingSchemaPanel.this.currentGradeMappingId = (String) typeChooser.getDefaultModelObject();

				// refresh data
				SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(getGradingSchemaEntries());

				// repaint table
				target.add(SettingsGradingSchemaPanel.this.schemaWrap);

				// set the warning if required
				SettingsGradingSchemaPanel.this.schemaModifiedFromDefault = isModified();
				SettingsGradingSchemaPanel.this.modifiedSchema.setVisible(SettingsGradingSchemaPanel.this.schemaModifiedFromDefault);
				target.add(SettingsGradingSchemaPanel.this.modifiedSchema);

				// refresh chart
				refreshCourseGradeChart(target);
			}
		});

		// button to add a mapping
		final GbAjaxButton addMapping = new GbAjaxButton("addMapping") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> f) {

				// add a new empty mapping to the model data
				final List<GbGradingSchemaEntry> entries = getGradingSchemaList();
				entries.add(stubGradingSchemaMapping());
				SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(entries);

				// repaint table
				target.add(SettingsGradingSchemaPanel.this.schemaWrap);

				// Note that we don't need to worry about showing warnings about modifications here as the change notifications will handle
				// that once a value has been added to the schema
			}
		};
		addMapping.setDefaultFormProcessing(false);
		this.schemaWrap.add(addMapping);

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
		this.statsWrap = new WebMarkupContainer("statsWrap");
		this.statsWrap.setOutputMarkupId(true);
		settingsGradingSchemaPanel.add(this.statsWrap);

		this.statistics = calculateStatistics();

		this.statsWrap.add(new Label("averagegpa", getAverageGPA()) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return StringUtils.equals(SettingsGradingSchemaPanel.this.gradingSchemaName, "Grade Points");
			}
		});
		this.statsWrap.add(new Label("average", getMean(this.statistics)));
		this.statsWrap.add(new Label("median", getMedian(this.statistics)));
		this.statsWrap.add(new Label("lowest", getMin(this.statistics)));
		this.statsWrap.add(new Label("highest", getMax(this.statistics)));
		this.statsWrap.add(new Label("deviation", getStandardDeviation(this.statistics)));
		this.statsWrap.add(new Label("graded", String.valueOf(this.total)));

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

		// chart
		this.chart = new GbCourseGradeChart("gradingSchemaChart", getCurrentSiteId());
		settingsGradingSchemaPanel.add(this.chart);
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
		Map<String, Double> bottomPercents;

		// note that we sort based on name so we need to pull the right name out of the list of mappings, for both cases
		this.gradingSchemaName = getGradingSchema(this.currentGradeMappingId).getName();

		if (StringUtils.equals(this.currentGradeMappingId, this.configuredGradeMappingId)) {
			// get the values from the configured grading scale (sorted by the service)
			bottomPercents = this.model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents();
		} else {
			// get the default values for the chosen grading scale and sort them
			bottomPercents = GradeMappingDefinition.sortGradeMapping(
					getGradingSchema(this.currentGradeMappingId).getDefaultBottomPercents());
		}

		return SettingsHelper.asList(bottomPercents);
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
		return this.businessService.getCourseGrades(studentUuids);
	}

	/**
	 * Calculates stats based on the calculated course grade values, excluding any empty grades
	 *
	 * @return {@link DescriptiveStatistics}
	 */
	private DescriptiveStatistics calculateStatistics() {

		final List<Double> grades = this.courseGradeMap.values().stream().filter(c -> StringUtils.isNotBlank(c.getMappedGrade()))
				.map(c -> NumberUtils.toDouble(c.getCalculatedGrade()))
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
	 * Get the total number of course grades, excluding empty grades
	 *
	 * @param map
	 * @return
	 */
	private int getTotalCourseGrades(final Map<String, CourseGrade> map) {
		return map.values().stream().filter(c -> StringUtils.isNotBlank(c.getMappedGrade()))
				.collect(Collectors.toList()).size();
	}

	/**
	 * Find a {@link GradeMappingDefinition} in the list of {@link GradeMappingDefinition} based on the id
	 *
	 * @param mappingId the id of the schema we want to pick out
	 * @return {@ link GradeMappingDefinition} or null
	 */
	private GradeMappingDefinition getGradingSchema(final String mappingId) {
		return this.gradeMappings
				.stream()
				.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), mappingId))
				.findFirst()
				.get();
	}

	/**
	 * Has the stored grade mapping been modified from the defaults?
	 *
	 * @return
	 */
	private boolean isModified() {
		final GradeMappingDefinition gradeMapping = getGradingSchema(this.currentGradeMappingId);
		return gradeMapping.isModified();
	}

	/**
	 * Has the page model's grade mapping been changed from the stored one?
	 *
	 * @return
	 */
	private boolean isDirty() {

		// Note that the maps must be HashMaps for the comparison to work properly due to TreeMap.equals() != HashMap.equals().

		//get current values
		final List<GbGradingSchemaEntry> currentValues = SettingsGradingSchemaPanel.this.model.getObject().getGradingSchemaEntries();
		final Map<String, Double> currentGradeMapping = new HashMap<>(SettingsHelper.asMap(currentValues));

		// get stored values
		final GradeMappingDefinition storedValues = getGradingSchema(this.currentGradeMappingId);
		final Map<String, Double> storedGradeMapping = new HashMap<>(storedValues.getGradeMap());

		return !currentGradeMapping.equals(storedGradeMapping);
	}




	/**
	 * Class to encapsulate the refresh of components when a change is made to the grading schema
	 */
	class GradingSchemaChangeBehaviour extends AjaxFormComponentUpdatingBehavior {

		private static final long serialVersionUID = 1L;

		private transient AjaxRequestTarget target;

		public static final String ONCHANGE = "onchange";

		public GradingSchemaChangeBehaviour(final String event) {
			super(event);
		}

		@Override
		protected void onUpdate(final AjaxRequestTarget t) {
			this.target = t;
			refreshGradingSchemaTable();
			refreshCourseGradeChart(this.target);
			refreshMessages();
		}

		/**
		 * Refresh the grading schema table
		 *
		 * @param target
		 */
		private void refreshGradingSchemaTable() {
			// fetch current data from model, sort and refresh the table
			final List<GbGradingSchemaEntry> schemaList = getGradingSchemaList();

			SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(schemaList);
			this.target.add(SettingsGradingSchemaPanel.this.schemaWrap);
		}


		/**
		 * Refresh messages
		 *
		 * @param target
		 */
		private void refreshMessages() {

			// check if schema has changed from the persistent values and show the warning
			SettingsGradingSchemaPanel.this.unsavedSchema.setVisible(isDirty());
			this.target.add(SettingsGradingSchemaPanel.this.unsavedSchema);
		}

	}

	/**
	 * Helper to get the gradingschema list from the model
	 *
	 * @return
	 */
	private List<GbGradingSchemaEntry> getGradingSchemaList() {
		final List<GbGradingSchemaEntry> schemaList = SettingsGradingSchemaPanel.this.model.getObject().getGradingSchemaEntries();
		schemaList.sort(Collections.reverseOrder());
		return schemaList;
	}

	/**
	 * Create a new grading schema entry stub
	 *
	 * @return {@link GbGradingSchemaEntry}
	 */
	private GbGradingSchemaEntry stubGradingSchemaMapping() {
		final GbGradingSchemaEntry entry = new GbGradingSchemaEntry(null, null);
		return entry;
	}

	/**
	 * Refresh the course grade chart
	 *
	 * @param target
	 */
	private void refreshCourseGradeChart(final AjaxRequestTarget target) {
		// we need the current data from model
		final List<GbGradingSchemaEntry> schemaList = getGradingSchemaList();

		// add warning for duplicates
		this.duplicateEntries.setVisible(SettingsHelper.hasDuplicates(schemaList));
		target.add(this.duplicateEntries);

		// refresh the chart
		Map<String, Double> schemaMap = SettingsHelper.asMap(schemaList);
		schemaMap = GradeMappingDefinition.sortGradeMapping(schemaMap);
		this.chart.refresh(target, schemaMap);
	}

}
