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

import java.io.IOException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;
import org.sakaiproject.gradebookng.business.exception.GbImportCommentMissingItemException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportDuplicateColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidColumnException;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.util.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * Upload/Download page
 */
@Slf4j
public class GradeImportUploadStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private final String panelId;

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
		Button continueButton;

		public UploadForm(final String id) {
			super(id);

			setMultiPart(true);
			setMaxSize(Bytes.megabytes(2));

			this.fileUploadField = new FileUploadField("upload");
			this.fileUploadField.add(new OnChangeAjaxBehavior() {
				@Override
				protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
					UploadForm.this.continueButton.setEnabled(true);
					ajaxRequestTarget.add(UploadForm.this.continueButton);
				}
			});
			add(this.fileUploadField);

			this.continueButton = new Button("continuebutton");
			this.continueButton.setEnabled(false);
			add(this.continueButton);

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

				// turn file into list
				// TODO would be nice to capture the values from these exceptions
				ImportedSpreadsheetWrapper spreadsheetWrapper = null;
				try {
					spreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(upload.getInputStream(), upload.getContentType(),
							upload.getClientFileName(), businessService, FormattedText.getDecimalSeparator());
				} catch (final GbImportExportInvalidColumnException e) {
					log.debug("incorrect format", e);
					error(getString("importExport.error.incorrectformat") + " - " + e.getMessage());
					return;
				} catch (final GbImportExportInvalidFileTypeException | InvalidFormatException e) {
					log.debug("incorrect type", e);
					error(getString("importExport.error.incorrecttype") + " - " + e.getMessage());
					return;
				} catch (final GbImportExportDuplicateColumnException e) {
					log.debug("duplicate column", e);
					error(getString("importExport.error.duplicatecolumn") + " - " + e.getMessage());
					return;
				} catch (final IOException e) {
					log.debug("unknown", e);
					error(getString("importExport.error.unknown") + " - " + e.getMessage());
					return;
				}

				if (spreadsheetWrapper == null) {
					error(getString("importExport.error.unknown"));
					return;
				}

				// get existing data
				final List<Assignment> assignments = GradeImportUploadStep.this.businessService.getGradebookAssignments();
				final List<GbStudentGradeInfo> grades = GradeImportUploadStep.this.businessService.buildGradeMatrix(assignments);

				// process file
				List<ProcessedGradeItem> processedGradeItems = null;
				try {
					processedGradeItems = ImportGradesHelper.processImportedGrades(spreadsheetWrapper, assignments, grades);
				} catch (final GbImportCommentMissingItemException e) {
					// TODO would be good if we could show the column here, but would have to return it
					log.debug("commentnoitem", e);
					error(getString("importExport.error.commentnoitem") + " - " + e.getMessage());
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
				importWizardModel.setReport(spreadsheetWrapper.getUserIdentifier().getReport());
				final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId,
						Model.of(importWizardModel));
				newPanel.setOutputMarkupId(true);
				GradeImportUploadStep.this.replaceWith(newPanel);

			}

		}
	}
}
