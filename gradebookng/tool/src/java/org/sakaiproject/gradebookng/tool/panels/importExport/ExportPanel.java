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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.EventHelper;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;

public class ExportPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private static final String IGNORE_COLUMN_PREFIX = "#";
	private static final String COMMENTS_COLUMN_PREFIX = "*";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';
	private static final String BOM = "\uFEFF";

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeStudentNumber = false;
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
		
		final boolean stuNumVisible = businessService.isStudentNumberVisible();
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
				return stuNumVisible;
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
				final GbCategoryType categoryType = ExportPanel.this.businessService.getGradebookCategoryType();
				return categoryType != GbCategoryType.WEIGHTED_CATEGORY;
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
				return ExportPanel.this.businessService.categoriesAreEnabled();
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

		final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
		groups.add(0, this.group);
		add(new DropDownChoice<GbGroup>("groupFilter", Model.of(this.group), groups, new ChoiceRenderer<GbGroup>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getDisplayValue(final GbGroup g) {
				return g.getTitle();
			}

			@Override
			public String getIdValue(final GbGroup g, final int index) {
				return g.getId();
			}
		}).add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				GbGroup value = (GbGroup) ((DropDownChoice) getComponent()).getDefaultModelObject();
				if (value == null) {
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

		}, buildFileName(false)).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));


		this.customDownloadLink = buildCustomDownloadLink();
		add(this.customDownloadLink);
	}

	private Component buildCustomDownloadLink() {
		return new DownloadLink("downloadCustomGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(true);
			}

		}, buildFileName(true)).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true).setOutputMarkupId(true);
	}

	private File buildFile(final boolean isCustomExport) {
		File tempFile;

		try {
			tempFile = File.createTempFile("gradebookTemplate", ".csv");

			//CSV separator is comma unless the comma is the decimal separator, then is ;
			try (OutputStreamWriter fstream = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.ISO_8859_1.name())){
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
				if (isCustomExport && this.includeStudentNumber) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("column.header.studentNumber")));
				}
				if (isCustomExport && this.includeSectionMembership) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("column.header.section")));
				}

				// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
				SortType sortBy = SortType.SORT_BY_SORTING;
				if (this.businessService.categoriesAreEnabled()) {
					sortBy = SortType.SORT_BY_CATEGORY;
				}
				final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);
				final List<CategoryDefinition> categories = this.businessService.getGradebookCategories();

				// no assignments, give a template
				if (assignments.isEmpty()) {
					// with points
					header.add(String.join(" ", getString("importExport.export.csv.headers.example.points"), "[100]"));
					
					// no points
					header.add(getString("importExport.export.csv.headers.example.nopoints"));
					
					// points and comments
					header.add(String.join(" ", COMMENTS_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.pointscomments"), "[50]"));
					
					// ignore
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.ignore")));
				}
				else {
					for (int i = 0; i < assignments.size(); i++) {
						// Pull the next assignment to see if we need to print out a category name 
						final Assignment a1 = assignments.get(i);
						final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;

						final String assignmentPoints = FormatHelper.formatGradeForDisplay(a1.getPoints().toString());
						String externalPrefix = "";
						if (a1.isExternallyMaintained()) {
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
							if (cd != null && this.businessService.getGradebookCategoryType() == GbCategoryType.WEIGHTED_CATEGORY) {
								if (cd.getWeight() != null) {
									catWeightString = "(" + FormatHelper.formatDoubleAsPercentage(cd.getWeight() * 100) + ")";
								}
							}

							// Add the category name plus weight if available
							header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("label.category"), a1.getCategoryName(), catWeightString));

						}
					}
				}


				if (isCustomExport && this.includePoints) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.points")));
				}
				if (isCustomExport && this.includeCalculatedGrade) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.calculatedGrade")));
				}
				if (isCustomExport && this.includeCourseGrade) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.courseGrade")));
				}
				if (isCustomExport && this.includeGradeOverride) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.gradeOverride")));
				}
				if (isCustomExport && this.includeLastLogDate) {
					header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.lastLogDate")));
				}
				
				csvWriter.writeNext(header.toArray(new String[] {}));

				// apply section/group filter
				final GradebookUiSettings settings = new GradebookUiSettings();
				if (isCustomExport && !GbGroup.Type.ALL.equals(this.group.getType())) {
					settings.setGroupFilter(this.group);
				}

				// get the grade matrix
				final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrixForImportExport(assignments, group);

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
					if (isCustomExport && this.includeStudentNumber)
					{
						line.add(studentGradeInfo.getStudentNumber());
					}
					List<String> userSections = studentGradeInfo.getSections();
					if (isCustomExport && this.includeSectionMembership) {
						line.add((userSections.size() > 0) ? userSections.get(0) : getString("sections.label.none"));
					}
					if (!isCustomExport || this.includeGradeItemScores || this.includeGradeItemComments || this.includeCategoryAverages) {
						final Map<Long, Double> categoryAverages = studentGradeInfo.getCategoryAverages();

						for (int i = 0; i < assignments.size(); i++) {
							final Assignment a1 = assignments.get(i);
							final Assignment a2 = ((i + 1) < assignments.size()) ? assignments.get(i + 1) : null;
							final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(a1.getId());

							if (gradeInfo != null) {
								if (!isCustomExport || this.includeGradeItemScores) {
									String grade = FormatHelper.formatGradeForDisplay(gradeInfo.getGrade());
									line.add(StringUtils.removeEnd(grade, formattedText.getDecimalSeparator() + "0"));
								}
								if (!isCustomExport || this.includeGradeItemComments) {
									line.add(gradeInfo.getGradeComment());
								}
							} else {
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
								
								final String formattedAverage = FormatHelper.formatGradeForDisplay(average);
								line.add(StringUtils.removeEnd(formattedAverage, formattedText.getDecimalSeparator() + "0"));
							}

						}
					}

					final GbCourseGrade gbCourseGrade = studentGradeInfo.getCourseGrade();
					final CourseGrade courseGrade = gbCourseGrade.getCourseGrade();

					if (isCustomExport && this.includePoints) {
						line.add(FormatHelper.formatGradeForDisplay(FormatHelper.formatDoubleToDecimal(courseGrade.getPointsEarned())));
					}
					if (isCustomExport && this.includeCalculatedGrade) {
						line.add(FormatHelper.formatGradeForDisplay(courseGrade.getCalculatedGrade()));
					}
					if (isCustomExport && this.includeCourseGrade) {
						line.add(courseGrade.getMappedGrade());
					}
					if (isCustomExport && this.includeGradeOverride) {
						line.add(FormatHelper.formatGradeForDisplay(courseGrade.getEnteredGrade()));
					}
					if (isCustomExport && this.includeLastLogDate) {
						if (courseGrade.getDateRecorded() == null) {
							line.add(null);
						} else {
							line.add(this.businessService.formatDateTime(courseGrade.getDateRecorded()));
						}
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
		final String gradebookName = this.businessService.getGradebook().getName();

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

		final String cleanFilename = Validator.cleanFilename(fileNameComponents.stream().collect(Collectors.joining("-")));

		return String.format("%s.%s", cleanFilename, extension);
	}
}
