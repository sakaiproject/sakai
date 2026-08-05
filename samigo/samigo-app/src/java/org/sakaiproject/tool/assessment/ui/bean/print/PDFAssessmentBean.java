/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.bean.print;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.api.pdf.AssessmentPdfService;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentThread;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/* Print to PDF backing bean. */
@Slf4j
@ManagedBean(name="pdfAssessment")
@SessionScoped
public class PDFAssessmentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final FormattedText formattedText = ComponentManager.get(FormattedText.class);

	private String title = "";

	private String actionString = "";

	@Autowired
	private transient AssessmentPdfService assessmentPdfService;

	public PDFAssessmentBean() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		log.debug("Starting PDFAssessementBean with session scope");
	}

	private AssessmentPdfService pdfService() {
		return assessmentPdfService;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Copies delivery parts into their printable form, numbering questions across the whole
	 * assessment and deriving answer sequences from their labels.
	 */
	private static List<SectionContentsBean> toPrintParts(List<org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean> deliveryParts) {
		List<SectionContentsBean> parts = new ArrayList<>();
		int numberQuestion = 1;
		for (org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean part : deliveryParts) {
			SectionContentsBean section = new SectionContentsBean(part);
			List<ItemContentsBean> items = section.getItemContents();

			for (ItemContentsBean itemContents : items) {
				itemContents.setNumber(numberQuestion++);

				List<ItemTextIfc> question = itemContents.getItemData().getItemTextArraySorted();
				for (ItemTextIfc itemtext : question) {
					List<AnswerIfc> answers = itemtext.getAnswerArray();
					for (AnswerIfc answer : answers) {
						if (answer.getLabel() != null && !answer.getLabel().equals("")) {
							answer.setSequence(Long.valueOf(answer.getLabel().charAt(0) - 64));
						}
					}
				}
			}
			parts.add(section);
		}
		return parts;
	}

	public String genName() {
		LocalDateTime now = LocalDateTime.now();
		String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

		String plainTitle = formattedText.convertFormattedTextToPlaintext(title);
		plainTitle = plainTitle.substring(0, Math.min(plainTitle.length(), 9));

		return formattedText.escapeUrl(
				String.format("%s%s.pdf", plainTitle, timestamp)
						.replace(" ", "_")
		);
	}

	public String prepPDF() {
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		deliveryBean.setActionString("previewAssessment");

		setActionString(ContextUtil.lookupParam("actionString"));

		BeginDeliveryActionListener beginDeliveryAL = new BeginDeliveryActionListener();
		DeliveryActionListener deliveryAL = new DeliveryActionListener();

		beginDeliveryAL.processAction(null);
		deliveryAL.processAction(null);

		setTitle(deliveryBean.getAssessmentTitle());
		cleanupPreviewPublishedAssessment(deliveryBean);

		return "print";
	}

	public String applyPrintSettings() {
		return "print";
	}

	private void cleanupPreviewPublishedAssessment(DeliveryBean deliveryBean) {
		if (!deliveryBean.isFromPrint()) {
			return;
		}
		String publishedId = deliveryBean.getAssessmentId();
		if (StringUtils.isBlank(publishedId)) {
			return;
		}
		RemovePublishedAssessmentThread thread = new RemovePublishedAssessmentThread(publishedId, "preview");
		thread.start();
	}

	/**
	 * Generates the printable assessment PDF from the current print settings, resolving the
	 * delivery and print settings beans from the active faces context.
	 *
	 * @return the PDF bytes, or an empty array when there is nothing printable
	 */
	public byte[] generatePrintablePdf() {
		return generatePrintablePdf((DeliveryBean) ContextUtil.lookupBean("delivery"),
				(PrintSettingsBean) ContextUtil.lookupBean("printSettings"));
	}

	/**
	 * Generates the printable assessment PDF for the supplied beans. Callers outside a JSF
	 * request (such as {@code PrintAssessmentPdfServlet}) resolve the beans themselves and
	 * pass them in.
	 *
	 * @return the PDF bytes, or an empty array when there is nothing printable
	 */
	public byte[] generatePrintablePdf(DeliveryBean deliveryBean, PrintSettingsBean printSettings) {
		List<SectionContentsBean> deliveryParts = resolveDeliveryParts(deliveryBean);
		if (deliveryParts.isEmpty()) {
			log.debug("No delivery parts available; nothing to print");
			return new byte[0];
		}
		return pdfService().buildPrintable(buildPrintModel(deliveryBean, deliveryParts, printSettings));
	}

	private AssessmentPrintPdfModel buildPrintModel(DeliveryBean deliveryBean, List<SectionContentsBean> deliveryParts, PrintSettingsBean printSettings) {
		String introHtml = AssessmentPdfSnapshotBuilder.buildIntroHtml(deliveryBean, printSettings, formattedText);
		return AssessmentPdfSnapshotBuilder.buildPrintModel(deliveryBean, deliveryParts, printSettings, introHtml, formattedText);
	}

	/**
	 * Derives the printable parts from the supplied delivery bean. Deliberately keeps no state:
	 * the parts must always belong to the assessment the caller passed in.
	 */
	private List<SectionContentsBean> resolveDeliveryParts(DeliveryBean deliveryBean) {
		if (deliveryBean.getTableOfContents() != null
				&& deliveryBean.getTableOfContents().getPartsContents() != null
				&& !deliveryBean.getTableOfContents().getPartsContents().isEmpty()) {
			return toPrintParts(deliveryBean.getTableOfContents().getPartsContents());
		}
		if (deliveryBean.getPageContents() != null
				&& deliveryBean.getPageContents().getPartsContents() != null
				&& !deliveryBean.getPageContents().getPartsContents().isEmpty()) {
			return toPrintParts(deliveryBean.getPageContents().getPartsContents());
		}
		return List.of();
	}

	public String getPdfPreviewUrl() {
		return SamigoConstants.SERVLET_MAPPING_PRINT_ASSESSMENT_PDF;
	}

	public String getPdfJsViewerUrl() {
		try {
			return "/library/webjars/pdf-js/5.3.31/web/viewer.html?file="
					+ URLEncoder.encode(getPdfPreviewUrl(), StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			log.warn("Could not encode PDF preview URL", e);
			return "/library/webjars/pdf-js/5.3.31/web/viewer.html?file=" + getPdfPreviewUrl();
		}
	}

	public void getPDFAttachment() {
		byte[] pdf = generatePrintablePdf();

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();

		response.reset();
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "attachment; filename=" + genName());
		response.setContentLength(pdf.length);
		try (OutputStream out = response.getOutputStream()) {
			out.write(pdf);
		} catch (IOException e) {
			log.warn("Error writing PDF bytes to response", e);
		}
		faces.responseComplete();
	}

	public String getActionString() {
		return actionString;
	}

	public void setActionString(String actionString) {
		this.actionString = actionString;
	}
}
