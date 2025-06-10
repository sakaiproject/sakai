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
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ooxml.POIXMLException;
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
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.gradebookng.business.exception.GbImportExportInvalidFileTypeException;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.grading.api.MessageHelper;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * Upload/Download page
 */
@Slf4j
public class GradeImportUploadStep extends BasePanel {

	private static final long serialVersionUID = 1L;

	private static final String SAK_PROP_MAX_IMPORT_FILE_SIZE = "gradebook.import.maxSize";
	private static final int SAK_PROP_MAX_IMPORT_FILE_SIZE_DFTL = 2;

	private final String panelId;
	private final int maxUploadFileSize;

	@SuppressWarnings("unchecked")
	private static ResourceLoader RL = new ResourceLoader();

	public GradeImportUploadStep(final String id) {
		super(id);
		this.panelId = id;
		this.maxUploadFileSize = businessService.getServerConfigService().getInt(SAK_PROP_MAX_IMPORT_FILE_SIZE, SAK_PROP_MAX_IMPORT_FILE_SIZE_DFTL);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		ExportPanel ep = new ExportPanel("export");
		add(ep);
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
			setMaxSize(Bytes.megabytes(maxUploadFileSize));

			this.fileUploadField = new FileUploadField("upload");
			this.fileUploadField.add(new AjaxFormSubmitBehavior("change") {
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

				@Override
				protected void onError(AjaxRequestTarget target) {
					final ImportExportPage page = (ImportExportPage) getPage();
					page.updateFeedback(target);
					target.add(continueButton);
				}
			});
			add(this.fileUploadField);

			this.continueButton = new AjaxButton("continuebutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					processUploadedFile(target);
				}
			};
			continueButton.setOutputMarkupId(true);
			this.continueButton.setEnabled(false);
			add(this.continueButton);

			final AjaxButton cancel = new AjaxButton("cancelbutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target) {
					setResponsePage(GradebookPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		@Override
		protected void onFileUploadException(FileUploadException e, Map<String, Object> model) {
			if (e instanceof FileUploadBase.SizeLimitExceededException) {
				error(MessageHelper.getString("importExport.error.fileTooBig", RL.getLocale(), maxUploadFileSize));
				continueButton.setEnabled(false);
			}
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
																					upload.getClientFileName(), businessService, ComponentManager.get(FormattedText.class).getDecimalSeparator(), currentGradebookUid, currentSiteId);
				} catch (final POIXMLException e) {
					log.debug("strict OOXML workbook encountered", e);
					error(getString("importExport.error.strictOOXML"));
					page.updateFeedback(target);
					return;
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
				importWizardModel.setGradeType(businessService.getGradebookSettings(currentGradebookUid, currentSiteId).getGradeType());
				importWizardModel.setSpreadsheetWrapper(spreadsheetWrapper);
				boolean uploadSuccess = ImportGradesHelper.setupImportWizardModelForSelectionStep(page, GradeImportUploadStep.this, importWizardModel, businessService, target, currentGradebookUid, currentSiteId);

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
