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
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentThread;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
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

	private List<SectionContentsBean> deliveryParts = null;

	private String actionString = "";

	private transient byte[] cachedPreviewPdfBytes;

	private long cachedPreviewPdfTimestamp;

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

	public List<SectionContentsBean> getDeliveryParts() {
		return deliveryParts;
	}

	public void setDeliveryParts(List<org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean> deliveryParts) {
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
		this.deliveryParts = parts;
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

		setDeliveryParts(deliveryBean.getTableOfContents().getPartsContents());
		setTitle(deliveryBean.getAssessmentTitle());
		updateCachedPreviewPdf();
		cleanupPreviewPublishedAssessment(deliveryBean);

		return "print";
	}

	public String applyPrintSettings() {
		updateCachedPreviewPdf();
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

	public void updateCachedPreviewPdf() {
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		PrintSettingsBean printSettings = (PrintSettingsBean) ContextUtil.lookupBean("printSettings");
		ensureDeliveryPartsLoaded(deliveryBean);
		if (deliveryParts == null || deliveryParts.isEmpty()) {
			cachedPreviewPdfBytes = null;
			storePreviewPdfInSession(null);
			return;
		}
		AssessmentPrintPdfModel model = buildPrintModel(deliveryBean, printSettings);
		cachedPreviewPdfBytes = pdfService().buildPrintable(model);
		cachedPreviewPdfTimestamp = System.currentTimeMillis();
		storePreviewPdfInSession(cachedPreviewPdfBytes);
	}

	private AssessmentPrintPdfModel buildPrintModel(DeliveryBean deliveryBean, PrintSettingsBean printSettings) {
		String introHtml = AssessmentPdfSnapshotBuilder.buildIntroHtml(deliveryBean, printSettings, formattedText);
		return AssessmentPdfSnapshotBuilder.buildPrintModel(deliveryBean, deliveryParts, printSettings, introHtml, formattedText);
	}

	private void storePreviewPdfInSession(byte[] pdfBytes) {
		Placement placement = ToolManager.getCurrentPlacement();
		if (placement == null) {
			log.warn("No current tool placement; cannot store print preview PDF");
			return;
		}
		ToolSession toolSession = SessionManager.getCurrentSession().getToolSession(placement.getId());
		if (pdfBytes == null || pdfBytes.length == 0) {
			toolSession.removeAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_BYTES);
			toolSession.removeAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_FILENAME);
			toolSession.removeAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_TIMESTAMP);
			return;
		}
		toolSession.setAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_BYTES, pdfBytes);
		toolSession.setAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_FILENAME, genName());
		toolSession.setAttribute(SamigoConstants.SESSION_ATTR_PRINT_PREVIEW_PDF_TIMESTAMP, cachedPreviewPdfTimestamp);
	}

	public byte[] getCachedPreviewPdfBytes() {
		return cachedPreviewPdfBytes;
	}

	public long getCachedPreviewPdfTimestamp() {
		return cachedPreviewPdfTimestamp;
	}

	public void ensureDeliveryPartsLoaded(DeliveryBean deliveryBean) {
		if (deliveryParts != null && !deliveryParts.isEmpty()) {
			return;
		}
		if (deliveryBean.getTableOfContents() != null
				&& deliveryBean.getTableOfContents().getPartsContents() != null
				&& !deliveryBean.getTableOfContents().getPartsContents().isEmpty()) {
			setDeliveryParts(deliveryBean.getTableOfContents().getPartsContents());
		} else if (deliveryBean.getPageContents() != null
				&& deliveryBean.getPageContents().getPartsContents() != null
				&& !deliveryBean.getPageContents().getPartsContents().isEmpty()) {
			setDeliveryParts(deliveryBean.getPageContents().getPartsContents());
		}
		if (StringUtils.isBlank(title) && StringUtils.isNotBlank(deliveryBean.getAssessmentTitle())) {
			setTitle(deliveryBean.getAssessmentTitle());
		}
	}

	public String getPdfPreviewUrl() {
		StringBuilder url = new StringBuilder(SamigoConstants.SERVLET_MAPPING_PRINT_ASSESSMENT_PDF);
		Placement placement = ToolManager.getCurrentPlacement();
		if (placement != null) {
			url.append('?')
					.append(SamigoConstants.PARAM_PRINT_PREVIEW_PLACEMENT)
					.append('=')
					.append(placement.getId());
			if (cachedPreviewPdfTimestamp > 0) {
				url.append("&t=").append(cachedPreviewPdfTimestamp);
			}
		} else if (cachedPreviewPdfTimestamp > 0) {
			url.append("?t=").append(cachedPreviewPdfTimestamp);
		}
		return url.toString();
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
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		PrintSettingsBean printSettings = (PrintSettingsBean) ContextUtil.lookupBean("printSettings");
		updateCachedPreviewPdf();
		byte[] pdf = cachedPreviewPdfBytes != null ? cachedPreviewPdfBytes
				: pdfService().buildPrintable(buildPrintModel(deliveryBean, printSettings));

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();

		response.reset();
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "attachment; filename=" + genName());
		response.setContentLength(pdf.length);
		try (OutputStream out = response.getOutputStream()) {
			out.write(pdf);
			out.flush();
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

	public String getSizeDeliveryParts() {
		return deliveryParts == null ? "0" : "" + deliveryParts.size();
	}

	public String getTotalQuestions() {
		if (deliveryParts == null) {
			return "0";
		}
		int items = 0;
		for (SectionContentsBean section : deliveryParts) {
			items += section.getItemContents().size();
		}
		return String.valueOf(items);
	}
}
