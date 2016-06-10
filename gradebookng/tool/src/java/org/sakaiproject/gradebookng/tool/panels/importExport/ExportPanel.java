package org.sakaiproject.gradebookng.tool.panels.importExport;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExportPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeGradeItemScores = true;
	boolean includeGradeItemComments = true;
	boolean includeCourseGrade = true;
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

		add(new AjaxCheckBox("includeStudentName", Model.of(includeStudentName)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeStudentName = !includeStudentName;
				setDefaultModelObject(includeStudentName);
			}
		});
		add(new AjaxCheckBox("includeStudentId", Model.of(includeStudentId)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeStudentId = !includeStudentId;
				setDefaultModelObject(includeStudentId);
			}
		});
		add(new AjaxCheckBox("includeGradeItemScores", Model.of(includeGradeItemScores)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeGradeItemScores = !includeGradeItemScores;
				setDefaultModelObject(includeGradeItemScores);
			}
		});
		add(new AjaxCheckBox("includeGradeItemComments", Model.of(includeGradeItemComments)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeGradeItemComments = !includeGradeItemComments;
				setDefaultModelObject(includeGradeItemComments);
			}
		});
		add(new AjaxCheckBox("includePoints", Model.of(includePoints)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includePoints = !includePoints;
				setDefaultModelObject(includePoints);
			}

			@Override
			public boolean isVisible() {
				// only allow option if categories are not weighted
				GbCategoryType categoryType = ExportPanel.this.businessService.getGradebookCategoryType();
				return categoryType != GbCategoryType.WEIGHTED_CATEGORY;
			}
		});
		add(new AjaxCheckBox("includeLastLogDate", Model.of(includeLastLogDate)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeLastLogDate = !includeLastLogDate;
				setDefaultModelObject(includeLastLogDate);
			}
		});
		add(new AjaxCheckBox("includeCourseGrade", Model.of(includeCourseGrade)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeCourseGrade = !includeCourseGrade;
				setDefaultModelObject(includeCourseGrade);
			}
		});
		add(new AjaxCheckBox("includeCalculatedGrade", Model.of(includeCalculatedGrade)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeCalculatedGrade = !includeCalculatedGrade;
				setDefaultModelObject(includeCalculatedGrade);
			}
		});
		add(new AjaxCheckBox("includeGradeOverride", Model.of(includeGradeOverride)) {
			@Override
			protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
				includeGradeOverride = !includeGradeOverride;
				setDefaultModelObject(includeGradeOverride);
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
			final CSVWriter csvWriter = new CSVWriter(fw);

			// Create csv header
			final List<String> header = new ArrayList<String>();
			if (!isCustomExport || includeStudentId) {
				header.add(getString("importExport.export.csv.headers.studentId"));
			}
			if (!isCustomExport || includeStudentName) {
				header.add(getString("importExport.export.csv.headers.studentName"));
			}

			// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
			List<Assignment> assignments = this.businessService.getGradebookAssignments();

			//build column header
			assignments.forEach(assignment -> {
				final String assignmentPoints = assignment.getPoints().toString();
				if (!isCustomExport || includeGradeItemScores) {
					header.add(assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, ".0") + "]");
				}
				if (!isCustomExport || includeGradeItemComments) {
					header.add("*/ " + assignment.getName() + " Comments */");
				}
			});

			if (isCustomExport && includePoints) {
				header.add(getString("importExport.export.csv.headers.points"));
			}
			if (isCustomExport && includeCalculatedGrade) {
				header.add(getString("importExport.export.csv.headers.calculatedGrade"));
			}
			if (isCustomExport && includeCourseGrade) {
				header.add(getString("importExport.export.csv.headers.courseGrade"));
			}
			if (isCustomExport && includeGradeOverride) {
				header.add(getString("importExport.export.csv.headers.gradeOverride"));
			}
			if (isCustomExport && includeLastLogDate) {
				header.add(getString("importExport.export.csv.headers.lastLogDate"));
			}

			csvWriter.writeNext(header.toArray(new String[] {}));

			// get the grade matrix
			List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments);

			//add grades
			grades.forEach(studentGradeInfo -> {
				final List<String> line = new ArrayList<String>();
				if (!isCustomExport || includeStudentId) {
					line.add(studentGradeInfo.getStudentEid());
				}
				if (!isCustomExport ||includeStudentName) {
					line.add(studentGradeInfo.getStudentLastName() + ", " + studentGradeInfo.getStudentFirstName());
				}
				if (!isCustomExport || includeGradeItemScores || includeGradeItemComments) {
					assignments.forEach(assignment -> {
						final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(assignment.getId());
						if (gradeInfo != null) {
							if (!isCustomExport || includeGradeItemScores) {
								line.add(StringUtils.removeEnd(gradeInfo.getGrade(), ".0"));
							}
							if (!isCustomExport || includeGradeItemComments) {
								line.add(gradeInfo.getGradeComment());
							}
						} else {
							// Need to account for no grades
							if (!isCustomExport || includeGradeItemScores) {
								line.add(null);
							}
							if (!isCustomExport || includeGradeItemComments) {
								line.add(null);
							}
						}
					});
				}

				GbCourseGrade gbCourseGrade = studentGradeInfo.getCourseGrade();
				CourseGrade courseGrade = gbCourseGrade.getCourseGrade();

				if (isCustomExport && includePoints) {
					line.add(FormatHelper.formatDoubleToTwoDecimalPlaces(courseGrade.getPointsEarned()));
				}
				if (isCustomExport && includeCalculatedGrade) {
					line.add(courseGrade.getCalculatedGrade());
				}
				if (isCustomExport && includeCourseGrade) {
					line.add(courseGrade.getMappedGrade());
				}
				if (isCustomExport && includeGradeOverride) {
					line.add(courseGrade.getEnteredGrade());
				}
				if (isCustomExport && includeLastLogDate) {
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
		String prefix = "gradebook_export";
		String extension = exportFormat.toString().toLowerCase();
		String gradebookName = this.businessService.getGradebook().getName();

		if (StringUtils.trimToNull(gradebookName) == null) {
			return String.format("%s.%s", gradebookName, extension);
		} else {
			gradebookName = gradebookName.replaceAll("\\s", "_");
			return String.format("%s-%s.%s", prefix, gradebookName, extension);
		}
	}
}
