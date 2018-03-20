package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.util.FormattedText;

import au.com.bytecode.opencsv.CSVWriter;

public class ExportPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private static final String CUSTOM_EXPORT_COLUMN_PREFIX = "# ";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeGradeItemScores = true;
	boolean includeGradeItemComments = true;
	boolean includeCourseGrade = false;
	boolean includePoints = false;
	boolean includeLastLogDate = false;
	boolean includeCalculatedGrade = false;
	boolean includeGradeOverride = false;

	public ExportPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new AjaxCheckBox("includeStudentName", Model.of(this.includeStudentName)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentName = !ExportPanel.this.includeStudentName;
				setDefaultModelObject(ExportPanel.this.includeStudentName);
			}
		});
		add(new AjaxCheckBox("includeStudentId", Model.of(this.includeStudentId)) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentId = !ExportPanel.this.includeStudentId;
				setDefaultModelObject(ExportPanel.this.includeStudentId);
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

		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(false);
			}

		}, buildFileName()).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		add(new DownloadLink("downloadCustomGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(true);
			}

		}, buildFileName()).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));
	}

	private File buildFile(final boolean isCustomExport) {
		File tempFile;

		try {
			tempFile = File.createTempFile("gradebookTemplate", ".csv");
			final FileWriter fw = new FileWriter(tempFile);
			//CSV separator is comma unless the comma is the decimal separator, then is ;
			final CSVWriter csvWriter = new CSVWriter(fw, ".".equals(FormattedText.getDecimalSeparator()) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR);

			// Create csv header
			final List<String> header = new ArrayList<String>();
			if (!isCustomExport || this.includeStudentId) {
				header.add(getString("importExport.export.csv.headers.studentId"));
			}
			if (!isCustomExport || this.includeStudentName) {
				header.add(getString("importExport.export.csv.headers.studentName"));
			}

			// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
			final List<Assignment> assignments = this.businessService.getGradebookAssignments();

			//build column header
			assignments.forEach(assignment -> {
				final String assignmentPoints = assignment.getPoints().toString();
				if (!isCustomExport || this.includeGradeItemScores) {
					header.add(assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, ".0") + "]");
				}
				if (!isCustomExport || this.includeGradeItemComments) {
					header.add("* " + assignment.getName());
				}
			});

			if (isCustomExport && this.includePoints) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.points")));
			}
			if (isCustomExport && this.includeCalculatedGrade) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.calculatedGrade")));
			}
			if (isCustomExport && this.includeCourseGrade) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.courseGrade")));
			}
			if (isCustomExport && this.includeGradeOverride) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.gradeOverride")));
			}
			if (isCustomExport && this.includeLastLogDate) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.lastLogDate")));
			}

			csvWriter.writeNext(header.toArray(new String[] {}));

			// get the grade matrix
			final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments);

			//add grades
			grades.forEach(studentGradeInfo -> {
				final List<String> line = new ArrayList<String>();
				if (!isCustomExport || this.includeStudentId) {
					line.add(studentGradeInfo.getStudentEid());
				}
				if (!isCustomExport ||this.includeStudentName) {
					line.add(studentGradeInfo.getStudentLastName() + ", " + studentGradeInfo.getStudentFirstName());
				}
				if (!isCustomExport || this.includeGradeItemScores || this.includeGradeItemComments) {
					assignments.forEach(assignment -> {
						final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(assignment.getId());
						if (gradeInfo != null) {
							if (!isCustomExport || this.includeGradeItemScores) {
								String grade = FormatHelper.formatGradeForDisplay(gradeInfo.getGrade());
								line.add(StringUtils.removeEnd(grade, FormattedText.getDecimalSeparator()+"0"));
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
					});
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
						line.add(FormatHelper.formatDateTime(courseGrade.getDateRecorded()));
					}
				}

				csvWriter.writeNext(line.toArray(new String[] {}));

			});

			csvWriter.close();
			fw.close();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return tempFile;
	}

	private String buildFileName() {
		final String prefix = "gradebook_export";
		final String extension = this.exportFormat.toString().toLowerCase();
		String gradebookName = this.businessService.getGradebook().getName();

		if (StringUtils.trimToNull(gradebookName) == null) {
			return String.format("%s.%s", gradebookName, extension);
		} else {
			gradebookName = gradebookName.replaceAll("\\s", "_");
			return String.format("%s-%s.%s", prefix, gradebookName, extension);
		}
	}
}
