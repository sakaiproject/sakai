package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.helpers.ImportGradesHelper;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedGradeWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Upload/Download page
 */
public class GradeImportUploadStep extends Panel {
	private static final Logger log = Logger.getLogger(GradeImportUploadStep.class);
	private static final long serialVersionUID = 1L;
	
	// list of mimetypes for each category. Must be compatible with the parser
	private static final String[] XLS_MIME_TYPES = { "application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" };
	private static final String[] CSV_MIME_TYPES = { "text/csv" };

	private final String panelId;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public GradeImportUploadStep(final String id) {
		super(id);
		this.panelId = id;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected File load() {
				return buildFile(true);

			}
		}).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		add(new UploadForm("form"));
	}

	private File buildFile(final boolean includeGrades) {
		File tempFile;
		try {
			// TODO - add the site name to the file?
			tempFile = File.createTempFile("gradebookTemplate", ".csv");
			final FileWriter fw = new FileWriter(tempFile);
			final CSVWriter csvWriter = new CSVWriter(fw);

			// Create csv header
			final List<String> header = new ArrayList<String>();
			header.add("Student ID");
			header.add("Student Name");
			
			// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
			List<Assignment> assignments = this.businessService.getGradebookAssignments();

			//build column header
			assignments.forEach(assignment -> {
				final String assignmentPoints = assignment.getPoints().toString();
				header.add(assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, ".0") + "]");
				header.add("*/ " + assignment.getName() + " Comments */");
			});

			csvWriter.writeNext(header.toArray(new String[] {}));
			
			// get the grade matrix
			List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments);

			//add grades
			grades.forEach(studentGradeInfo -> {
				final List<String> line = new ArrayList<String>();
				line.add(studentGradeInfo.getStudentEid());
				line.add(studentGradeInfo.getStudentLastName() + ", " + studentGradeInfo.getStudentFirstName());
				if (includeGrades) {
					
					assignments.forEach(assignment -> {
						final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(assignment.getId());
						if (gradeInfo != null) {
							line.add(StringUtils.removeEnd(gradeInfo.getGrade(), ".0"));
							line.add(gradeInfo.getGradeComment());
						} else {
							// Need to account for no grades
							line.add(null);
							line.add(null);
						}
					});
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

	/*
	 * Upload form
	 */
	private class UploadForm extends Form<Void> {

		FileUploadField fileUploadField;

		public UploadForm(final String id) {
			super(id);

			setMultiPart(true);
			setMaxSize(Bytes.megabytes(2));

			this.fileUploadField = new FileUploadField("upload");
			add(this.fileUploadField);

			add(new Button("continuebutton"));

			final Button cancel = new Button("cancelbutton") {
				@Override
				public void onSubmit() {
					setResponsePage(new GradebookPage());
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		@Override
		public void onSubmit() {

			final FileUpload upload = this.fileUploadField.getFileUpload();
			if (upload != null) {

				try {
					log.debug("file upload success");
					
					// get all users
					final Map<String, String> userMap = getUserMap();

					// turn file into list
					final ImportedGradeWrapper importedGradeWrapper = parseImportedGradeFile(upload.getInputStream(),
							upload.getContentType(), userMap);
					
					//get existing data
					List<Assignment> assignments = GradeImportUploadStep.this.businessService.getGradebookAssignments();
					List<GbStudentGradeInfo> grades = GradeImportUploadStep.this.businessService.buildGradeMatrix(assignments);

					//process file
					final List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedGradeWrapper,assignments, grades);

					// if null, the file was of the incorrect type
					// if empty there are no users
					if (processedGradeItems == null || processedGradeItems.isEmpty()) {
						error(getString("error.parse.upload"));
					} else {
						// GO TO NEXT PAGE
						log.debug(processedGradeItems.size());

						// repaint panel
						final ImportWizardModel importWizardModel = new ImportWizardModel();
						importWizardModel.setProcessedGradeItems(processedGradeItems);
						final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId,
								Model.of(importWizardModel));
						newPanel.setOutputMarkupId(true);
						GradeImportUploadStep.this.replaceWith(newPanel);

					}

				} catch (final IOException e) {
					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * Create a map so that we can use the user's eid (from the imported file) to lookup their uuid (used to store the grade by the backend
	 * service)
	 *
	 * @return Map where the user's eid is the key and the uuid is the value
	 */
	private Map<String, String> getUserMap() {

		List<User> users = this.businessService.getUsers(this.businessService.getGradeableUsers());
				
		final Map<String, String> rval = users.stream().collect(
                Collectors.toMap(User::getEid, User::getId));
	
		return rval;
	}

	public ImportedGradeWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final Map<String, String> userMap) {

		// determine file type and delegate
		if (ArrayUtils.contains(CSV_MIME_TYPES, mimetype)) {
			return ImportGradesHelper.parseCsv(is, userMap);
		} else if (ArrayUtils.contains(XLS_MIME_TYPES, mimetype)) {
			return ImportGradesHelper.parseXls(is, userMap);
		} else {
			log.error("Invalid file type for grade import: " + mimetype);
		}
		return null;
	}
}
