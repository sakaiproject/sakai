/**
 * Copyright (c) 2003-2024 The Apereo Foundation
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

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.panels.BasePanel;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.grading.api.Assignment;
import java.time.Duration;
import org.sakaiproject.user.api.UserDirectoryService;

public class ExportToZipPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.rubrics.api.repository.AssociationRepository")
	private AssociationRepository associationRepository;

	@SpringBean(name = "org.sakaiproject.user.api.UserDirectoryService")
	private UserDirectoryService userDirectoryService;
	
	private JsonNode receivedParams;
	private Rubric rubric;
	private String toolId = "sakai.gradebookng";
	private String gradebookAssignmentId;

	private final ModalWindow window;
	
	public ExportToZipPanel(final String id, final IModel<Long> model, final ModalWindow window, JsonNode params) {
		super(id, model);
		this.window = window;
		this.receivedParams = params;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		this.gradebookAssignmentId = receivedParams.get("assignmentId").asText();
		Optional<ToolItemRubricAssociation> optAssociation = associationRepository.findByToolIdAndItemId(toolId, gradebookAssignmentId);
		this.rubric = optAssociation.isPresent()? optAssociation.get().getRubric(): null;
		if (rubric != null) {
			add(new DownloadLink("exportRubrics", new LoadableDetachableModel<File>() {
				private static final long serialVersionUID = 1L;

				@Override
				protected File load() {
					return buildFile();
				}

			}, MessageHelper.getString("export.zip.template.name", rubric.getTitle().replace(" ", "-")) + ".zip").setCacheDuration(Duration.ZERO).setDeleteAfterDownload(true));
		}
	}

	private File buildFile() {				
		String siteId = receivedParams.get("siteId").asText();
		final String[] userIds = receivedParams.get("userIds").asText().split(",");

		File tempFile;

		try {
			if (rubric != null) {
				Long rubricId = rubric.getId();
				tempFile = File.createTempFile("tempZip", ".zip");
				ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile));
				for (String userId : userIds) {
					String evaluatedItemId = rubricsService.getRubricEvaluationObjectId(gradebookAssignmentId, userId, toolId, siteId);
					String name = userDirectoryService.getUser(userId).getEid(); //Should be the name
					Assignment assignment = businessService.getAssignment(Long.parseLong(gradebookAssignmentId));
					String title = assignment.getName();
					byte[] pdf = rubricsService.createPdf(siteId, rubricId, toolId, gradebookAssignmentId, evaluatedItemId);
					final ZipEntry zipEntryPdf = new ZipEntry(name + "_" + title + ".pdf");

					out.putNextEntry(zipEntryPdf);
					out.write(pdf);
					out.closeEntry();
				}

				out.finish();
				out.flush();
				out.close();
				return tempFile;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
