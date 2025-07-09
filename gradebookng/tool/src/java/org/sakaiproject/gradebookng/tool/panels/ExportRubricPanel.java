/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.panels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.tool.chart.CourseGradeChart;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.stats.CourseGradeStatistics;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.GradebookInformation;
import org.sakaiproject.grading.api.MessageHelper;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Renders the course grade graph in a modal window
 */
@Slf4j
public class ExportRubricPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.rubrics.api.repository.AssociationRepository")
	private AssociationRepository associationRepository;

	@SpringBean(name = "org.sakaiproject.user.api.UserDirectoryService")
	private UserDirectoryService userDirectoryService;

	@SuppressWarnings("unchecked")
	private static ResourceLoader RL = new ResourceLoader();

	private JsonNode receivedParams;
	private Rubric rubric;
	private String toolId = "sakai.gradebookng";
	private String gradebookAssignmentId;

	private final ModalWindow window;

	public ExportRubricPanel(final String id, final IModel<Long> model, final ModalWindow window, JsonNode params) {
		super(id);
		this.window = window;
		this.receivedParams = params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		super.onInitialize();

		this.gradebookAssignmentId = receivedParams.get("assignmentId").asText();
		Optional<ToolItemRubricAssociation> optAssociation = associationRepository.findByToolIdAndItemId(toolId, gradebookAssignmentId);
		this.rubric = optAssociation.isPresent()? optAssociation.get().getRubric(): null;

		if (rubric != null) {
			add(new DownloadLink("exportAllRubrics", new LoadableDetachableModel<File>() {
				private static final long serialVersionUID = 1L;

				@Override
				protected File load() {
					return buildFile();
				}


			}, MessageHelper.getString("export.zip.template.name", RL.getLocale(), rubric.getTitle().replace(" ", "-")) + ".zip").setCacheDuration(Duration.ZERO).setDeleteAfterDownload(true));
		} else {
			WebMarkupContainer placeholder = new WebMarkupContainer("exportAllRubrics");
			placeholder.setVisible(false);
			add(placeholder);
		}

		ExportRubricPanel.this.window.setTitle(MessageHelper.getString("export.zip.template.button", RL.getLocale()));
	}

	private File buildFile() {
		final String[] userIds = receivedParams.get("userIds").asText().split(",");

		if (rubric == null) {
			return null;
		}

		try {
			Long rubricId = rubric.getId();
			File tempFile = File.createTempFile("tempZip", ".zip");

			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(tempFile))) {
				for (String userId : userIds) {
					String evaluatedItemId = rubricsService.getRubricEvaluationObjectId(
						gradebookAssignmentId, userId, toolId, currentSiteId);
					String name = userDirectoryService.getUser(userId).getEid();
					Assignment assignment = businessService.getAssignment(
						currentGradebookUid, currentSiteId, Long.parseLong(gradebookAssignmentId));
					String title = assignment.getName();
					byte[] pdf = rubricsService.createPdf(
						currentSiteId, rubricId, toolId, gradebookAssignmentId, evaluatedItemId);

					final ZipEntry zipEntryPdf = new ZipEntry(name + "_" + title + ".pdf");
					out.putNextEntry(zipEntryPdf);
					out.write(pdf);
					out.closeEntry();
				}
			} // ZipOutputStream is automatically closed here

			return tempFile;

		} catch (final Exception e) {
			log.error("Error occurred while building zip file: " + e.toString(), e);
			return null;
		}
	}

}