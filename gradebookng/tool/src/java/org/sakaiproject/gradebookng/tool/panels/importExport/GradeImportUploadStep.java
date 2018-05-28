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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.util.FormattedText;

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
		AjaxButton continueButton;

		public UploadForm(final String id) {
			super(id);

			setMultiPart(true);
			setMaxSize(Bytes.megabytes(2));

			this.fileUploadField = new FileUploadField("upload");
			this.fileUploadField.add(new AjaxFormSubmitBehavior("onchange") {
				@Override
				protected void onSubmit(final AjaxRequestTarget target) {
					FileUpload file = fileUploadField.getFileUpload();
					final ImportExportPage page = (ImportExportPage) getPage();
					if (file == null) {
						error(getString("importExport.error.nullFile"));
						continueButton.setEnabled(false);
					} else {
						String fileName = file.getClientFileName();
						String mimeType = file.getContentType();
						if((StringUtils.endsWithAny(fileName, ImportGradesHelper.CSV_FILE_EXTS) || ArrayUtils.contains(ImportGradesHelper.CSV_MIME_TYPES, mimeType))
								|| (StringUtils.endsWithAny(fileName, ImportGradesHelper.XLS_FILE_EXTS) || ArrayUtils.contains(ImportGradesHelper.XLS_MIME_TYPES, mimeType))) {
							continueButton.setEnabled(true);
							page.clearFeedback();
						} else {
							error(getString("importExport.error.incorrecttype"));
							continueButton.setEnabled(false);
						}
					}

					page.updateFeedback(target);
					target.add(continueButton);
				}
			});
			add(this.fileUploadField);

			this.continueButton = new AjaxButton("continuebutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form<?> form) {
					processUploadedFile(target);
				}
			};
			continueButton.setOutputMarkupId(true);
			this.continueButton.setEnabled(false);
			add(this.continueButton);

			final AjaxButton cancel = new AjaxButton("cancelbutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form<?> form) {
					setResponsePage(GradebookPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		public void processUploadedFile(AjaxRequestTarget target) {
			final ImportExportPage page = (ImportExportPage) getPage();
			final FileUpload upload = fileUploadField.getFileUpload();
			if (upload != null) {

				log.debug("file upload success");

				// turn file into list
				ImportedSpreadsheetWrapper spreadsheetWrapper;
				try {
					spreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(upload.getInputStream(), upload.getContentType(), 
																					upload.getClientFileName(), businessService, FormattedText.getDecimalSeparator());
				} catch (final GbImportExportInvalidFileTypeException | InvalidFormatException e) {
					log.debug("incorrect type", e);
					error(getString("importExport.error.incorrecttype"));
					page.updateFeedback(target);
					return;
				} catch (final IOException e) {
					log.debug("unknown", e);
					error(getString("importExport.error.unknown"));
					page.updateFeedback(target);
					return;
				}

				final ImportWizardModel importWizardModel = new ImportWizardModel();
				importWizardModel.setSpreadsheetWrapper(spreadsheetWrapper);
				boolean uploadSuccess = ImportGradesHelper.setupImportWizardModelForSelectionStep(page, GradeImportUploadStep.this, importWizardModel, businessService, target);

				// For whatever issues encountered, ImportGradesHelper.setupImportWizardModelForSelectionStep() will have updated the feedbackPanels; just return
				if (!uploadSuccess) {
					return;
				}

				final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId, Model.of(importWizardModel));
				newPanel.setOutputMarkupId(true);

				// AJAX the new panel into place
				WebMarkupContainer container = page.container;
				container.addOrReplace(newPanel);
				target.add(container);
			} else {
				error(getString("importExport.error.noFileSelected"));
				page.updateFeedback(target);
			}
		}
	}
}
