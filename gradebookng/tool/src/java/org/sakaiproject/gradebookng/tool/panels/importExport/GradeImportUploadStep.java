package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedGradeWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Upload/Download page
 */
@CommonsLog
public class GradeImportUploadStep extends Panel {

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

		add(new ExportPanel("export"));
		add(new UploadForm("form"));
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

					// couldn't parse
					if(importedGradeWrapper == null) {
						error(getString("importExport.error.badfile"));
						return;
					}

					//get existing data
					final List<Assignment> assignments = GradeImportUploadStep.this.businessService.getGradebookAssignments();
					final List<GbStudentGradeInfo> grades = GradeImportUploadStep.this.businessService.buildGradeMatrix(assignments);

					//process file
					final List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(importedGradeWrapper,assignments, grades);

					// if null, the file was of the incorrect type
					// if empty there are no users
					if (processedGradeItems == null || processedGradeItems.isEmpty()) {
						error(getString("importExport.error.badfile"));
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

		final List<User> users = this.businessService.getUsers(this.businessService.getGradeableUsers());

		final Map<String, String> rval = users.stream().collect(
                Collectors.toMap(User::getEid, User::getId));

		return rval;
	}

	/**
	 * Helper to parse the imported file into an {@link ImportedGradeWrapper} depending on its type
	 * @param is
	 * @param mimetype
	 * @param userMap
	 * @return
	 */
	private ImportedGradeWrapper parseImportedGradeFile(final InputStream is, final String mimetype, final Map<String, String> userMap) {

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
