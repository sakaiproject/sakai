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
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
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

import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.util.EventHelper;
import org.sakaiproject.gradebookng.business.util.ExportTempFile;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;

public class ExportPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private static final String IGNORE_COLUMN_PREFIX = "#";
	private static final String COMMENTS_COLUMN_PREFIX = "*";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeStudentNumber = false;
	boolean includeStudentDisplayId = false;
	boolean includeGradeItemScores = true;
	boolean includeGradeItemComments = true;
	boolean includeCourseGrade = false;
	boolean includePoints = false;
	boolean includeLastLogDate = false;
	boolean includeCalculatedGrade = false;
	boolean includeGradeOverride = false;
	boolean includeSection = true;
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

			tempFile = ExportTempFile.createTempFile(buildFileName(), ".csv");
			final FileWriter fw = new FileWriter(tempFile);
			//CSV separator is comma unless the comma is the decimal separator, then is ;
			final CSVWriter csvWriter = new CSVWriter(fw, ".".equals(FormattedText.getDecimalSeparator()) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR);

			final Map<String, List<String>> userSectionMap = this.businessService.getUserSections();

			// Create csv header
			final List<String> header = new ArrayList<String>();
			if (!isCustomExport || this.includeStudentId) {
				header.add(getString("importExport.export.csv.headers.studentId"));
			}
			if (!isCustomExport || this.includeStudentName) {
				header.add(getString("importExport.export.csv.headers.studentName"));
			}
			if ((!isCustomExport || this.includeSection) && userSectionMap != null) {
				header.add(getString("importExport.export.csv.headers.section"));
			}

			if (isCustomExport && this.includeStudentNumber)
			{
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.studentNumber")));
			}

			// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment
			// from the map
			final List<Assignment> assignments = this.businessService.getGradebookAssignments();

			// no assignments, give a template
			if (assignments.isEmpty()) {
				// with points
				header.add(String.join(" ", getString("importExport.export.csv.headers.example.points"), "[100]"));

				// no points
				header.add(getString("importExport.export.csv.headers.example.nopoints"));

				// points and comments
				header.add(String.join(" ", COMMENTS_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.pointscomments"),
						"[50]"));

				// ignore
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.example.ignore")));
			}

			// build column header
			boolean categoriesEnabled = businessService.categoriesAreEnabled();
			if (categoriesEnabled) {
				Collections.sort(assignments, new sortByCatOrder());
				Collections.sort(assignments, new sortByAssignmentOrder());
			}
			assignments.forEach(assignment -> {
				final String assignmentPoints = assignment.getPoints().toString();
				if (!isCustomExport || this.includeGradeItemScores) {
					header.add(assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, ".0") + "]");
				}
				if (!isCustomExport || this.includeGradeItemComments) {
					header.add(String.join(" ", COMMENTS_COLUMN_PREFIX, assignment.getName()));
				}
			});

			if (isCustomExport && this.includePoints) {
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.points")));
			}
			if (isCustomExport && this.includeCalculatedGrade) {
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.calculatedGrade")));
			}
			if (isCustomExport && this.includeCourseGrade) {
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.courseGrade")));
			}
			if (isCustomExport && this.includeLastLogDate) {
				header.add(String.join(" ", IGNORE_COLUMN_PREFIX, getString("importExport.export.csv.headers.lastLogDate")));
			}

			csvWriter.writeNext(header.toArray(new String[] {}));
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

	private String buildFileNamePrefix() {
		final String prefix = "gradebook_export";
		String gradebookName = this.businessService.getCurrentSiteId();

		if (StringUtils.trimToNull(gradebookName) == null) {
			return String.format("%s", gradebookName);
		} else {
			gradebookName = gradebookName.replaceAll("\\s", "_");
			gradebookName = StringUtils.trimToNull(gradebookName);
			return String.format("%s-%s", prefix, gradebookName);
		}
	}


	private String buildFileName(){
			return buildFileName(false);
		}


	}

	class sortByCatOrder implements Comparator<Assignment> {
		public int compare(Assignment a, Assignment b) {
			if (a.getCategoryOrder() == null && b.getCategoryOrder() == null) {
				return 0;
			} else if (a.getCategoryOrder() == null) {
				return 1;
			} else if (b.getCategoryOrder() == null) {
				return -1;
			} else {
				return (a.getCategoryOrder()- b.getCategoryOrder());
			}
		}
	}

	class sortByAssignmentOrder implements Comparator<Assignment> {
		public int compare(Assignment a, Assignment b) {
			if (!StringUtils.equals(a.getCategoryName(), b.getCategoryName())) {
				return 0;
			}  else if (a.getCategorizedSortOrder() == null && b.getCategorizedSortOrder() == null) {
				return 0;
			}  else {
				return (a.getCategorizedSortOrder()- b.getCategorizedSortOrder());
			}
		}
	}
