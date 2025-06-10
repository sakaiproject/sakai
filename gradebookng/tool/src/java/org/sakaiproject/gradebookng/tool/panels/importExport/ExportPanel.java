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
package org.sakaiproject.gradebookng.tool.panels.importExport;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.util.EventHelper;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.CourseGradeTransferBean;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private static final String IGNORE_COLUMN_PREFIX = "#";
	private static final String COMMENTS_COLUMN_PREFIX = "*";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';
	private static final String BOM = "\uFEFF";
	private static final String TXT_EXTENSION = ".txt";

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeStudentNumber = true;
	private boolean includeSectionMembership = false;
	boolean includeStudentDisplayId = false;
	boolean includeGradeItemScores = true;
	boolean includeGradeItemComments = true;
	boolean includeCategoryAverages = false;
	boolean includeCourseGrade = false;
	boolean includePoints = false;
	boolean includeLastLogDate = false;
	boolean includeCalculatedGrade = false;
	boolean includeGradeOverride = false;
	boolean stuNumVisible = false;
	GbGroup group;

	private Component customDownloadLink;

	public ExportPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new AjaxCheckBox("includeStudentId", Model.of(this.includeStudentId)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentId = !ExportPanel.this.includeStudentId;
				setDefaultModelObject(ExportPanel.this.includeStudentId);
			}
		});

		add(new AjaxCheckBox("includeStudentDisplayId", Model.of(this.includeStudentDisplayId)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentDisplayId = !ExportPanel.this.includeStudentDisplayId;
				setDefaultModelObject(ExportPanel.this.includeStudentDisplayId);
			}
		});

		add(new AjaxCheckBox("includeStudentName", Model.of(this.includeStudentName)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentName = !ExportPanel.this.includeStudentName;
				setDefaultModelObject(ExportPanel.this.includeStudentName);
			}
		});
		
		this.stuNumVisible = businessService.isStudentNumberVisible(currentSiteId);
		add(new AjaxCheckBox("includeStudentNumber", Model.of(this.includeStudentNumber)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentNumber = !ExportPanel.this.includeStudentNumber;
				setDefaultModelObject(ExportPanel.this.includeStudentNumber);
			}

			@Override
			public boolean isVisible()
			{
				return ExportPanel.this.stuNumVisible;
			}
		});

		add(new AjaxCheckBox("includeSectionMembership", Model.of(this.includeSectionMembership)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeSectionMembership = !ExportPanel.this.includeSectionMembership;
				setDefaultModelObject(ExportPanel.this.includeSectionMembership);
			}
		});

		add(new AjaxCheckBox("includeGradeItemScores", Model.of(this.includeGradeItemScores)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeItemScores = !ExportPanel.this.includeGradeItemScores;
				setDefaultModelObject(ExportPanel.this.includeGradeItemScores);
			}
		});
		add(new AjaxCheckBox("includeGradeItemComments", Model.of(this.includeGradeItemComments)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeItemComments = !ExportPanel.this.includeGradeItemComments;
				setDefaultModelObject(ExportPanel.this.includeGradeItemComments);
			}
		});
		add(new AjaxCheckBox("includePoints", Model.of(this.includePoints)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includePoints = !ExportPanel.this.includePoints;
				setDefaultModelObject(ExportPanel.this.includePoints);
			}

			@Override
			public boolean isVisible() {
				// only allow option if categories are not weighted
				final Integer categoryType = ExportPanel.this.businessService.getGradebookCategoryType(currentGradebookUid, currentSiteId);
				return !Objects.equals(categoryType, GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY);
			}
		});
		add(new AjaxCheckBox("includeLastLogDate", Model.of(this.includeLastLogDate)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeLastLogDate = !ExportPanel.this.includeLastLogDate;
				setDefaultModelObject(ExportPanel.this.includeLastLogDate);
			}
		});
		add(new AjaxCheckBox("includeCategoryAverages", Model.of(this.includeCategoryAverages)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeCategoryAverages = !ExportPanel.this.includeCategoryAverages;
				setDefaultModelObject(ExportPanel.this.includeCategoryAverages);
			}

			@Override
			public boolean isVisible() {
				return ExportPanel.this.businessService.categoriesAreEnabled(currentGradebookUid, currentSiteId);
			}
		});
		add(new AjaxCheckBox("includeCourseGrade", Model.of(this.includeCourseGrade)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeCourseGrade = !ExportPanel.this.includeCourseGrade;
				setDefaultModelObject(ExportPanel.this.includeCourseGrade);
			}
		});
		add(new AjaxCheckBox("includeCalculatedGrade", Model.of(this.includeCalculatedGrade)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeCalculatedGrade = !ExportPanel.this.includeCalculatedGrade;
				setDefaultModelObject(ExportPanel.this.includeCalculatedGrade);
			}
		});
		add(new AjaxCheckBox("includeGradeOverride", Model.of(this.includeGradeOverride)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeOverride = !ExportPanel.this.includeGradeOverride;
				setDefaultModelObject(ExportPanel.this.includeGradeOverride);
			}
		});

		this.group = new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL);

		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups(currentGradebookUid, currentSiteId);
		if (currentGradebookUid.equals(currentSiteId)) {
			groups.add(0, this.group);
		} else { // group instance gb, list will have one and only one
			this.group = groups.get(0);
		}
		add(new DropDownChoice<GbGroup>("groupFilter", Model.of(this.group), groups, new ChoiceRenderer<GbGroup>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(final GbGroup g) {
				return g.getTitle();
			}

			@Override
			public String getIdValue(final GbGroup g, final int index) {
				return g.getId() != null ? g.getId() : "";
			}
		}).add(new AjaxFormComponentUpdatingBehavior("change") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				GbGroup value = (GbGroup) ((DropDownChoice) getComponent()).getDefaultModelObject();
				if (value == null && currentGradebookUid.equals(currentSiteId)) {
					ExportPanel.this.group = new GbGroup(null, getString("groups.all"), null, GbGroup.Type.ALL);
				} else {
					ExportPanel.this.group = (GbGroup) ((DropDownChoice) getComponent()).getDefaultModelObject();
				}
				// Rebuild the custom download link so it has a filename including the selected group
				Component updatedCustomDownloadLink = buildCustomDownloadLink();
				ExportPanel.this.customDownloadLink.replaceWith(updatedCustomDownloadLink);
				ExportPanel.this.customDownloadLink = updatedCustomDownloadLink;
				target.add(ExportPanel.this.customDownloadLink);
			}
		}));

		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(false);
			}

		}, buildFileName(false)).setCacheDuration(Duration.ZERO).setDeleteAfterDownload(true));


		this.customDownloadLink = buildCustomDownloadLink();
		add(this.customDownloadLink);

		// Add Osiris export components if enabled
		boolean osirisExportEnabled =this.serverConfigService.getBoolean(SAK_PROP_ENABLE_OSIRIS_EXPORT,
				SAK_PROP_ENABLE_OSIRIS_EXPORT_DEFAULT);

		WebMarkupContainer osirisExportContainer = new WebMarkupContainer("osirisExportContainer");
		osirisExportContainer.setVisible(osirisExportEnabled);
		add(osirisExportContainer);
		osirisExportContainer.add(new DownloadLink("downloadOsirisExport", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 2L;

			@Override
			protected File load() {
				return buildOsirisExportFile();
			}

		}, buildOsirisExportFileName()).setCacheDuration(Duration.ZERO).setDeleteAfterDownload(true));

	}

	private Component buildCustomDownloadLink() {
		return new DownloadLink("downloadCustomGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(true);
			}

		}, buildFileName(true)).setCacheDuration(Duration.ZERO).setDeleteAfterDownload(true).setOutputMarkupId(true);
	}

	private File buildFile(final boolean isCustomExport) {
		File tempFile;

		try {
			tempFile = File.createTempFile("gradebookTemplate", ".csv");

			//CSV separator is comma unless the comma is the decimal separator, then is ;
			try (OutputStreamWriter fstream = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.ISO_8859_1)){
				FormattedText formattedText = ComponentManager.get(FormattedText.class);
				CSVWriter csvWriter = new CSVWriter(fstream, ".".equals(formattedText.getDecimalSeparator()) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
				
				// Create csv header
				final List<String> header = new ArrayList<>();
				if (!isCustomExport || this.includeStudentId) {
					header.add(getString("importExport.export.csv.headers.studentId"));
				}
				if (!isCustomExport || this.includeStudentName) {
					header.add(getString("importExport.export.csv.headers.studentName"));
				}
				if (isCustomExport && this.includeStudentDisplayId) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.studentDisplayId")));
				}
				if (this.stuNumVisible && (!isCustomExport || this.includeStudentNumber)) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.studentNumber")));
				}
				if (isCustomExport && this.includeSectionMembership) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("column.header.section")));
				}
				if (isCustomExport && this.includePoints) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.points")));
				}
				if (isCustomExport && this.includeCourseGrade) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.courseGrade")));
				}
				if (isCustomExport && this.includeCalculatedGrade) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.calculatedGrade")));
				}
				if (isCustomExport && this.includeGradeOverride) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.gradeOverride")));
				}
				if (isCustomExport && this.includeLastLogDate) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.lastLogDate")));
				}

				// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
				SortType sortBy = SortType.SORT_BY_SORTING;
				if (this.businessService.categoriesAreEnabled(currentGradebookUid, currentSiteId)) {
					sortBy = SortType.SORT_BY_CATEGORY;
				}
				final List<Assignment> assignments = this.businessService.getGradebookAssignments(currentGradebookUid, currentSiteId, sortBy);
				final List<CategoryDefinition> categories = this.businessService.getGradebookCategories(currentGradebookUid, currentSiteId);

				final GradeType gradeType = businessService.getGradebookSettings(currentGradebookUid, currentSiteId).getGradeType();

				// no assignments, give a template
				if (assignments.isEmpty()) {
					if (!isCustomExport || this.includeGradeItemScores) {
						header.add(String.join(" ", getString("importExport.export.csv.headers.example.points"), "[100]"));
					}
					if (!isCustomExport || this.includeGradeItemComments) {
						header.add(getString("importExport.export.csv.headers.example.nopoints"));
						header.add(String.join(" ", COMMENTS_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.pointscomments"), "[50]"));
					}
					// ignore
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.ignore")));
				}
				else {
					for (int i = 0; i < assignments.size(); i++) {
						// Pull the next assignment to see if we need to print out a category name 
						final Assignment a1 = assignments.get(i);
						final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;

						final String assignmentPoints = FormatHelper.formatGradeForDisplay(a1.getPoints().toString(), gradeType);
						String externalPrefix = "";
						if (a1.getExternallyMaintained()) {
							externalPrefix = IGNORE_COLUMN_PREFIX;
						}
						if (!isCustomExport || this.includeGradeItemScores) {
							header.add(externalPrefix + a1.getName() + " [" + StringUtils.removeEnd(assignmentPoints, formattedText.getDecimalSeparator() + "0") + "]");
						}
						if (!isCustomExport || this.includeGradeItemComments) {
							header.add(String.join(" ", externalPrefix, COMMENTS_COLUMN_PREFIX, a1.getName()));
						}
						
						if (isCustomExport && this.includeCategoryAverages
								&& a1.getCategoryId() != null && (a2 == null || !a1.getCategoryId().equals(a2.getCategoryId()))) {
							// Find the correct category in the ArrayList to extract the points
							final CategoryDefinition cd = categories.stream().filter(cat -> a1.getCategoryId().equals(cat.getId())).findAny().orElse(null);
							String catWeightString = "";
							if (cd != null && Objects.equals(this.businessService.getGradebookCategoryType(currentGradebookUid, currentSiteId), GradingConstants.CATEGORY_TYPE_WEIGHTED_CATEGORY)) {
								if (cd.getWeight() != null) {
									catWeightString = "(" + FormatHelper.formatDoubleAsPercentage(cd.getWeight() * 100) + ")";
								}
							}

							// Add the category name plus weight if available
							header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("label.category"), a1.getCategoryName(), catWeightString));

						}
					}
				}

				// Add ignore column header when assignments exist to match data alignment
				if (!assignments.isEmpty()) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.ignore")));
				}

				csvWriter.writeNext(header.toArray(new String[] {}));

				// apply section/group filter
				final GradebookUiSettings settings = new GradebookUiSettings();
				if (isCustomExport && !GbGroup.Type.ALL.equals(this.group.getType())) {
					settings.setGroupFilter(this.group);
				}

				// get the grade matrix
				String selectedGroup = (group != null && !GbGroup.Type.ALL.equals(group.getType())) ? group.getId() : null;
				final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrixForImportExport(currentGradebookUid, currentSiteId, assignments, selectedGroup);

				// add grades
				grades.forEach(studentGradeInfo -> {
					final List<String> line = new ArrayList<>();
					if (!isCustomExport || this.includeStudentId) {
						line.add(studentGradeInfo.getStudentEid());
					}
					if (!isCustomExport || this.includeStudentName) {
						line.add(FormatHelper.htmlUnescape(studentGradeInfo.getStudentLastName()) + ", " + FormatHelper.htmlUnescape(studentGradeInfo.getStudentFirstName()));
					}
					if (isCustomExport && this.includeStudentDisplayId) {
						line.add(studentGradeInfo.getStudentDisplayId());
					}
					if (this.stuNumVisible && (!isCustomExport || this.includeStudentNumber))
					{
						line.add(studentGradeInfo.getStudentNumber());
					}
					List<String> userSections = studentGradeInfo.getSections();
					if (isCustomExport && this.includeSectionMembership) {
						line.add((userSections.size() > 0) ? userSections.get(0) : getString("sections.label.none"));
					}

					final CourseGradeTransferBean courseGrade = studentGradeInfo.getCourseGrade();

					if (isCustomExport && this.includePoints) {
						line.add(FormatHelper.formatGradeForDisplay(FormatHelper.formatDoubleToDecimal(courseGrade.getPointsEarned()), gradeType));
					}
					if (isCustomExport && this.includeCourseGrade) {
						line.add(courseGrade.getMappedGrade());
					}
					if (isCustomExport && this.includeCalculatedGrade) {
						line.add(FormatHelper.formatGradeForDisplay(courseGrade.getCalculatedGrade(), gradeType));
					}
					if (isCustomExport && this.includeGradeOverride) {
						if (courseGrade.getEnteredGrade() != null) {
							line.add(courseGrade.getEnteredGrade());
						} else {
							line.add(null);
						}
					}
					if (isCustomExport && this.includeLastLogDate) {
						if (courseGrade.getDateRecorded() == null) {
							line.add(null);
						} else {
							line.add(this.businessService.formatDateTime(courseGrade.getDateRecorded()));
						}
					}

					if (!isCustomExport || this.includeGradeItemScores || this.includeGradeItemComments || this.includeCategoryAverages) {
						if (assignments.isEmpty()) {
							// Add empty values for example columns
							if (!isCustomExport || this.includeGradeItemScores) {
								line.add(null);
							}
							if (!isCustomExport || this.includeGradeItemComments) {
								line.add(null);
								line.add(null);
							}
							// Add ignore column value to match template header
							line.add(null);
						}
						else {
							final Map<Long, Double> categoryAverages = studentGradeInfo.getCategoryAverages();

							for (int i = 0; i < assignments.size(); i++) {
								final Assignment a1 = assignments.get(i);
								final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;
								final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(a1.getId());

								if (gradeInfo != null) {
									if (!isCustomExport || this.includeGradeItemScores) {
										String grade = FormatHelper.formatGradeForDisplay(gradeInfo.getGrade(), gradeType);
										line.add(StringUtils.removeEnd(grade, formattedText.getDecimalSeparator() + "0"));
									}
									if (!isCustomExport || this.includeGradeItemComments) {
										line.add(gradeInfo.getGradeComment());
									}
								}
							 	else {
									// Need to account for no grades
									if (!isCustomExport || this.includeGradeItemScores) {
										line.add(null);
									}
									if (!isCustomExport || this.includeGradeItemComments) {
										line.add(null);
									}
								}

							if (isCustomExport && this.includeCategoryAverages
									&& a1.getCategoryId() != null && (a2 == null || !a1.getCategoryId().equals(a2.getCategoryId()))) {
								final Double average = categoryAverages.get(a1.getCategoryId());
								
								final String formattedAverage = FormatHelper.formatGradeForDisplay(average, gradeType);
								line.add(StringUtils.removeEnd(formattedAverage, formattedText.getDecimalSeparator() + "0"));
								}
							}
						}
					}

					// Add the "ignore" column value to keep alignment, but only if assignments exist
					// (when assignments are empty, the template already includes an ignore column)
					if (!assignments.isEmpty()) {
						line.add(null); // for the ignore column
					}
					
					csvWriter.writeNext(line.toArray(new String[] {}));
				});
				csvWriter.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		EventHelper.postExportEvent(getGradebook(), isCustomExport);

		return tempFile;
	}


	private String buildFileName(final boolean customDownload) {
		final String prefix = getString("importExport.download.filenameprefix");
		final String extension = this.exportFormat.toString().toLowerCase();
		final String gradebookName = this.businessService.getGradebook(currentGradebookUid, currentSiteId).getName();

		// File name contains the prefix
		final List<String> fileNameComponents = new ArrayList<>();
		fileNameComponents.add(prefix);

		// Add gradebook name/site id to filename
		if (StringUtils.trimToNull(gradebookName) != null) {
			fileNameComponents.add(gradebookName.replaceAll("\\s", "_"));
		}

		// If custom download for all sections, append 'ALL' to filename
		if (customDownload && (this.group == null || this.group.getId() == null)) {
			fileNameComponents.add(getString("importExport.download.filenameallsuffix"));

		// If group/section filter is selected, add group title to filename
		} else if (this.group != null && this.group.getId() != null && StringUtils.isNotBlank(this.group.getTitle())) {
			fileNameComponents.add(this.group.getTitle());
		}

		final String cleanFilename = Validator.cleanFilename(String.join("-", fileNameComponents));

		return String.format("%s.%s", cleanFilename, extension);
	}

	private File buildOsirisExportFile() {
		List<String> userIds = this.businessService.getGradeableUsers(currentGradebookUid, currentSiteId, null);

		Map<String, String> userEids = this.businessService.getGbUsers(currentSiteId, userIds).stream()
				.collect(Collectors.toMap(GbUser::getUserUuid, GbUser::getDisplayId));

		Map<String, CourseGradeTransferBean> courseGrades = this.businessService.getCourseGrades(currentGradebookUid, currentSiteId, userIds, null);

		try {
			File tempFile = File.createTempFile("gradebookExport", TXT_EXTENSION);

			try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
				for (String userId : userIds) {
					String userEid = userEids.get(userId);
					if (StringUtils.isEmpty(userEid)) {
						log.debug("Skipping user {} from export, beacuse eid is empty", userId);
						continue;
					}

					CourseGradeTransferBean courseGrade = courseGrades.get(userId);
					if (courseGrade == null || StringUtils.isEmpty(courseGrade.getCalculatedGrade())) {
						log.debug("Skipping user {} from export, beacuse display grade is empty", userId);
						continue;
					}

					DecimalFormatSymbols.getInstance(Locale.US);
					String grade = NumberUtils.toScaledBigDecimal(courseGrade.getCalculatedGrade(), 0, RoundingMode.HALF_UP).toString();
					
					writer.println(StringUtils.trim(userEid) + " " + grade);
				}
			}

			EventHelper.postOsirisExportEvent(getGradebook());

			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException("Failed to create Osiris export", e);
		}
	}

	private String buildOsirisExportFileName() {
		final String prefix = getString("importExport.download.filenameprefix");

		final List<String> fileNameComponents = new ArrayList<>();
		fileNameComponents.add(prefix);

		// Add gradebook name/site id to filename
		final String gradebookName = this.businessService.getGradebook(currentGradebookUid, currentSiteId).getName();
		if (StringUtils.isNotBlank(gradebookName)) {
			fileNameComponents.add(StringUtils.replace(gradebookName, " ", "_"));
		}

		fileNameComponents.add("osiris");

		final String cleanFilename = Validator.cleanFilename(StringUtils.join(fileNameComponents, "-"));

		return cleanFilename + TXT_EXTENSION;
	}
}
