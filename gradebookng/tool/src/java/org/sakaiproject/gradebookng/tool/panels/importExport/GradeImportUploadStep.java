package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
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
import org.sakaiproject.gradebookng.business.exception.GbImportCommentMissingItemException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportDuplicateColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportUnknownStudentException;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.user.api.User;

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

/**
 * Upload/Download page
 */
@Slf4j
public class GradeImportUploadStep extends Panel {
	private static final long serialVersionUID = 1L;

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
					setResponsePage(GradebookPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		@Override
		public void onSubmit() {

			final FileUpload upload = this.fileUploadField.getFileUpload();
			if (upload != null) {

				log.debug("file upload success");

				// get all users
				final Map<String, String> userMap = getUserMap();

				// turn file into list
				// TODO would be nice to capture the values from these exceptions
				ImportedSpreadsheetWrapper spreadsheetWrapper = null;
				try {
					spreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(upload.getInputStream(), upload.getContentType(), upload.getClientFileName(), userMap);
				} catch (final GbImportExportInvalidColumnException e) {
					error(getString("importExport.error.incorrectformat"));
					return;
				} catch (final GbImportExportInvalidFileTypeException | InvalidFormatException e) {
					error(getString("importExport.error.incorrecttype"));
					return;
				} catch (final GbImportExportUnknownStudentException e) {
					error(getString("importExport.error.unknownstudent"));
					return;
				} catch (final GbImportExportDuplicateColumnException e) {
					error(getString("importExport.error.duplicatecolumn"));
					return;
				} catch (final IOException e) {
					error(getString("importExport.error.unknown"));
					return;
				}

				if(spreadsheetWrapper == null) {
					error(getString("importExport.error.unknown"));
					return;
				}

				//get existing data
				final List<Assignment> assignments = GradeImportUploadStep.this.businessService.getGradebookAssignments();
				final List<GbStudentGradeInfo> grades = GradeImportUploadStep.this.businessService.buildGradeMatrix(assignments);

				// process file
				List<ProcessedGradeItem> processedGradeItems = null;
				try {
					processedGradeItems = ImportGradesHelper.processImportedGrades(spreadsheetWrapper, assignments, grades);
				} catch (final GbImportCommentMissingItemException e) {
					// TODO would be good if we could show the column here, but would have to return it
					error(getString("importExport.error.commentnoitem"));
					return;
				}
				// if empty there are no users
				if (processedGradeItems.isEmpty()) {
					error(getString("importExport.error.empty"));
					return;
				}

				// OK, GO TO NEXT PAGE

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

				// repaint panel
				final ImportWizardModel importWizardModel = new ImportWizardModel();
				importWizardModel.setProcessedGradeItems(processedGradeItems);
				final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId, Model.of(importWizardModel));
				newPanel.setOutputMarkupId(true);
				GradeImportUploadStep.this.replaceWith(newPanel);

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

}
