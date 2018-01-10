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

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import org.sakaiproject.gradebookng.business.DoubleComparator;
import org.sakaiproject.gradebookng.business.FirstNameComparator;
import org.sakaiproject.gradebookng.business.LetterGradeComparator;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.component.JFreeChartImageWithToolTip;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.user.api.User;

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

				// if grade is F or NP, set disabled
				if (ArrayUtils.contains(new String[] { "F", "NP", "F (0)" }, entry.getGrade())) {
					minPercent.setEnabled(false);
				}

				item.add(minPercent);
			}
		};
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

		// chart
		final JFreeChart chartData = getChartData();
		final JFreeChartImageWithToolTip chart = new JFreeChartImageWithToolTip("chart", Model.of(chartData), "tooltip", 700, 400);

		// chart is only visible if there are grades
		chart.setVisible(this.total > 0);
		settingsGradingSchemaPanel.add(chart);

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
				return StringUtils.equals(gradingSchemaName, "Grade Points");
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
	 * Helper to sort the bottom percents maps. Caters for both letter grade and P/NP types
	 *
	 * @param gradingScaleName name of the grading schema so we know how to sort.
	 * @param percents
	 * @return
	 */
	private Map<String, Double> sortBottomPercents(final String gradingScaleName, final Map<String, Double> percents) {

		Map<String, Double> rval = null;

		if (StringUtils.equals(gradingScaleName, "Pass / Not Pass")) {
			rval = new TreeMap<>(Collections.reverseOrder()); // P before NP.
		} else if (StringUtils.contains(gradingScaleName, "Letter Grades") || StringUtils.contains(gradingScaleName, "Grade Points")) {
			rval = new TreeMap<>(new LetterGradeComparator()); // letter grade mappings
		} else {
			// Order by percent.
			DoubleComparator doubleComparator = new DoubleComparator(percents);
			rval = new TreeMap<String, Double>(doubleComparator);
		}
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
			// get the values from the configured grading scale in this gradebook and sort accordingly
			bottomPercents = sortBottomPercents(gradingSchemaName,
					this.model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents());
		} else {
			// get the default values for the chosen grading scale and sort accordingly
			bottomPercents = sortBottomPercents(gradingSchemaName,
					this.gradeMappings.stream()
							.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), this.currentGradeMappingId))
							.findFirst()
							.get()
							.getDefaultBottomPercents());
		}

		// convert map into list of objects which is easier to work with in the views
		final List<GbGradingSchemaEntry> rval = new ArrayList<>();
		for (final Map.Entry<String, Double> entry : bottomPercents.entrySet()) {
			rval.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}

		return rval;
	}

	public boolean isExpanded() {
		return this.expanded;
	}

	/**
	 * Build the data for the chart
	 * 
	 * @return
	 */
	private JFreeChart getChartData() {

		// just need the list
		final List<CourseGrade> courseGrades = this.courseGradeMap.values().stream().collect(Collectors.toList());

		// get current grading schema (from model so that it reflects current state)
		final List<GbGradingSchemaEntry> gradingSchemaEntries = this.model.getObject().getGradingSchemaEntries();

		final DefaultCategoryDataset data = new DefaultCategoryDataset();
		final Map<String, Integer> counts = new LinkedHashMap<>(); // must retain order so graph can be printed correctly

		// add all schema entries (these will be sorted according to {@link LetterGradeComparator})
		gradingSchemaEntries.forEach(e -> {
			counts.put(e.getGrade(), 0);
		});

		// now add the count of each course grade for those schema entries
		this.total = 0;
		for (final CourseGrade g : courseGrades) {

			// course grade may not be released so we have to skip it
			if (StringUtils.isBlank(g.getMappedGrade())) {
				continue;
			}

			counts.put(g.getMappedGrade(), counts.get(g.getMappedGrade()) + 1);
			this.total++;
		}

		// build the data
		final ListIterator<String> iter = new ArrayList<>(counts.keySet()).listIterator(0);
		while (iter.hasNext()) {
			final String c = iter.next();
			data.addValue(counts.get(c), "count", c);
		}

		final JFreeChart chart = ChartFactory.createBarChart(
				null, // the chart title
				getString("settingspage.gradingschema.chart.xaxis"), // the label for the category (x) axis
				getString("label.statistics.chart.yaxis"), // the label for the value (y) axis
				data, // the dataset for the chart
				PlotOrientation.HORIZONTAL, // the plot orientation
				false, // show legend
				true, // show tooltips
				false); // show urls

		chart.getCategoryPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		chart.setBorderVisible(false);
		chart.setAntiAlias(false);

		final CategoryPlot plot = chart.getCategoryPlot();
		final BarRenderer br = (BarRenderer) plot.getRenderer();

		br.setItemMargin(0);
		br.setMinimumBarLength(0.05);
		br.setMaximumBarWidth(0.1);
		br.setSeriesPaint(0, new Color(51, 122, 183));
		br.setBarPainter(new StandardBarPainter());
		br.setShadowPaint(new Color(220, 220, 220));
		BarRenderer.setDefaultShadowsVisible(true);

		br.setBaseToolTipGenerator(
				new StandardCategoryToolTipGenerator(getString("label.statistics.chart.tooltip"), NumberFormat.getInstance()));

		plot.setRenderer(br);

		// show only integers in the count axis
		plot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));

		// make x-axis wide enough so we don't get ... suffix
		plot.getDomainAxis().setMaximumCategoryLabelWidthRatio(2.0f);

		plot.setBackgroundPaint(Color.white);

		chart.setTitle(getString("settingspage.gradingschema.chart.heading"));

		return chart;
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
			Map<String, Double> gpaScoresMap = getGPAScoresMap();

			// get all of the non null mapped grades
			// mapped grades will be null if the student doesn't have a course grade yet.
			final List<String> mappedGrades = this.courseGradeMap.values().stream().filter(c -> c.getMappedGrade() != null)
					.map(c -> (c.getMappedGrade())).collect(Collectors.toList());
			Double averageGPA = 0.0;
			for (String mappedGrade : mappedGrades) {
				// Note to developers. If you changed GradePointsMapping without changing gpaScoresMap, the average will be incorrect.
				// As per GradePointsMapping, both must be kept in sync
				Double grade = gpaScoresMap.get(mappedGrade);
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
	private String getMean(DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMean()) : "-";
	}

	/**
	 * Calculates the median grade for the course
	 * 
	 * @return String median grade
	 */
	private String getMedian(DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getPercentile(50)) : "-";
	}

	/**
	 * Calculates the min grade for the course
	 * 
	 * @return String min grade
	 */
	private String getMin(DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMin()) : "-";
	}

	/**
	 * Calculates the max grade for the course
	 * 
	 * @return String max grade
	 */
	private String getMax(DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getMax()) : "-";
	}

	/**
	 * Calculates the standard deviation for the course
	 * 
	 * @return String standard deviation
	 */
	private String getStandardDeviation(DescriptiveStatistics stats) {
		return this.total > 0 ? String.format("%.2f", stats.getStandardDeviation()) : "-";
	}

	/**
	 * 
	 */
	private Map<String, Double> getGPAScoresMap() {
		Map<String, Double> gpaScoresMap = new HashMap<>();
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
}
