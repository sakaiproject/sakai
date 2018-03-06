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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedSectionAttachment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.pdf.HTMLWorker;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.component.cover.ComponentManager;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactoryImp;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a>
 *
 * This class is basically just a conveinceince class for abstracting the creation of
 * PDF's from assessments
 * 
 */
@Slf4j
public class PDFAssessmentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static org.sakaiproject.util.api.FormattedText formattedText = (org.sakaiproject.util.api.FormattedText)ComponentManager.get(org.sakaiproject.util.api.FormattedText.class);
	
	private ResourceLoader printMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.PrintMessages");

	private ResourceLoader authorMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");

	private ResourceLoader deliveryMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.DeliveryMessages");

	private ResourceLoader commonMessages = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.CommonMessages");

	private String intro = "";

	private String title = "";

	private List parts = null;

	private List deliveryParts = null;

	private int baseFontSize = 5;

	private String actionString = "";

	public PDFAssessmentBean() {
		if (log.isInfoEnabled())
			log.info("Starting PDFAssessementBean with session scope");

	}


	/**
	 * Gets the pdf assessments intro
	 * @return assessment intor in html
	 */
	public String getIntro() {
		return intro;
	}

	/**
	 * sets the pdf assessments intro
	 * @param intro in html
	 */
	public void setIntro(String intro) {
		this.intro = convertFormattedText(FormattedText.unEscapeHtml(intro));
	}

	/**
	 * gets the delivery bean parts of the assessment
	 * @return
	 */
	public List getDeliveryParts() {
		return deliveryParts;
	}

	/**
	 * gets the parts of the assessment
	 * @return
	 */
	public List getParts() {
		return parts;
	}

	/**
	 * gets what should be the full set of html chunks for an assessment
	 * @return
	 */
	public List getHtmlChunks() {
		return parts;
	}

	/**
	 * sets the delivery parts
	 * @param deliveryParts
	 */
	public void setDeliveryParts(List deliveryParts) {
		List parts = new ArrayList();
		int numberQuestion = 1;
		for (int i=0; i<deliveryParts.size(); i++) {
			SectionContentsBean section = new SectionContentsBean((org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean)deliveryParts.get(i));
			List items = section.getItemContents();

			// Renumbering
			for (int j=0; items != null && j<items.size(); j++) {
				ItemContentsBean itemContents = (ItemContentsBean)items.get(j);

				itemContents.setNumber(numberQuestion++);

				// Order answers in order (A, B, C, D)
				List question = itemContents.getItemData().getItemTextArraySorted();
				for (int k=0; k<question.size(); k++) {
					PublishedItemText itemtext = (PublishedItemText)question.get(k);
					List answers = itemtext.getAnswerArray();
					for (int t=0; t<answers.size(); t++) {
						PublishedAnswer answer = (PublishedAnswer)answers.get(t);
						if (answer.getLabel() != null && !answer.getLabel().equals(""))
							answer.setSequence(Long.valueOf(answer.getLabel().charAt(0) - 64));
					}
				}
			}
			parts.add(section);
		}
		this.deliveryParts = parts;
	}

	/**
  /**
	 * sets the parts
	 * @param parts
	 */
	public void setParts(List parts) {
		this.parts = parts;
	}

	/**
	 * gets the Title
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * sets the Title
	 * @param title
	 */
	public void setTitle(String title) {  
		this.title = title;
	}

	/**
	 * generates the pdf file name
	 * @return pdf file name
	 */
	public String genName() {
		//There has got to be a cleaner way to get a good time stamp in java?
		Calendar cal = new GregorianCalendar();

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);

		String fTitle = FormattedText.convertFormattedTextToPlaintext(title);
		int end = Math.min(fTitle.length(), 9);

		StringBuffer name = new StringBuffer(fTitle.substring(0, end));
		name.append(year);
		name.append(month);
		name.append(day);
		name.append(hour);
		name.append(min);
		name.append(sec);
		name.append(".pdf");
		
		if (log.isWarnEnabled())
			log.warn(name.toString());

		return formattedText.escapeUrl(name.toString().replace(" ", "_"));
	}

	public String prepPDF() {

		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		deliveryBean.setActionString("previewAssessment");

		setActionString(ContextUtil.lookupParam("actionString"));

		// Call all the listeners needed to populate the deliveryBean...
		BeginDeliveryActionListener beginDeliveryAL = new BeginDeliveryActionListener();
		DeliveryActionListener deliveryAL = new DeliveryActionListener();

		beginDeliveryAL.processAction(null);
		deliveryAL.processAction(null);

		setDeliveryParts(deliveryBean.getTableOfContents().getPartsContents());

		prepDocumentPDF();

		return "print";
	}

	public String prepDocumentPDF() {

		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");

		PrintSettingsBean printSetting = (PrintSettingsBean) ContextUtil.lookupBean("printSettings");
		setBaseFontSize(printSetting.getFontSize());

		if (printSetting.getShowPartIntros().booleanValue()) {
			StringBuffer assessmentIntros = new StringBuffer();
			if (deliveryBean.getInstructorMessage() != null && !"".equals(deliveryBean.getInstructorMessage())) {
				assessmentIntros.append(deliveryBean.getInstructorMessage());
				assessmentIntros.append("<br />");
			}
			
			if (deliveryBean.getAttachmentList() != null && deliveryBean.getAttachmentList().size() > 0) {
				assessmentIntros.append("<br />");
				assessmentIntros.append(printMessages.getString("attachments"));

				List assessmentAttachmentList = deliveryBean.getAttachmentList();
				Iterator assessmentAttachmentIter = assessmentAttachmentList.iterator();
				while (assessmentAttachmentIter.hasNext()) {
					assessmentIntros.append("<br />");
					PublishedAssessmentAttachment assessmentAttachment = (PublishedAssessmentAttachment) assessmentAttachmentIter.next();
					if (assessmentAttachment.getMimeType().equalsIgnoreCase("image/jpeg") || 
							assessmentAttachment.getMimeType().equalsIgnoreCase("image/pjpeg") || 
							assessmentAttachment.getMimeType().equalsIgnoreCase("image/gif") || 
							assessmentAttachment.getMimeType().equalsIgnoreCase("image/png")) {
						assessmentIntros.append("  <img src=\"/samigo");
						assessmentIntros.append(assessmentAttachment.getResourceId());
						assessmentIntros.append("\" />");
					}
					else {
						assessmentIntros.append("  ");
						assessmentIntros.append(assessmentAttachment.getFilename());
					}
				}
			}
			
			setIntro(assessmentIntros.toString());	
		}
		else {
			setIntro("");
		}

		List pdfParts = new ArrayList();

		//for each part in an assessment we add a pdfPart to the pdfBean
		for (int i = 0; i < deliveryParts.size(); i++) {
			//get the current item
			SectionContentsBean section = (SectionContentsBean) deliveryParts.get(i);
			List items = section.getItemContents();
			List resources = new ArrayList();

			//create a new part and empty list to fill with items
			PDFPartBean pdfPart = new PDFPartBean();
			pdfPart.setSectionId(section.getSectionId());

			StringBuffer partIntros = new StringBuffer();
			partIntros.append("<h2>");
			partIntros.append(authorMessages.getString("p"));
			partIntros.append(" ");
			partIntros.append(i+1);
			if (!printSetting.getShowPartIntros().booleanValue()) {
				partIntros.append("</h2>");
			}
			else {
				if ("Default".equalsIgnoreCase(section.getTitle())) {
					partIntros.append("</h2>");
				}
				else {
					partIntros.append(": ");
					partIntros.append(section.getTitle());
					partIntros.append("</h2>");
				}
				partIntros.append("<br />");
				if (section.getDescription() != null) {
					partIntros.append(section.getDescription());
				}
				partIntros.append("<br />");

				
				if (section.getAttachmentList() != null && section.getAttachmentList().size() > 0) {
					partIntros.append("<br />");
					partIntros.append(printMessages.getString("attachments"));

					List partAttachmentList = section.getAttachmentList();
					Iterator partAttachmentIter = partAttachmentList.iterator();
					while (partAttachmentIter.hasNext()) {
						partIntros.append("<br />");
						PublishedSectionAttachment partAttachment = (PublishedSectionAttachment) partAttachmentIter.next();
						if (partAttachment.getMimeType().equalsIgnoreCase("image/jpeg") || 
								partAttachment.getMimeType().equalsIgnoreCase("image/pjpeg") || 
								partAttachment.getMimeType().equalsIgnoreCase("image/gif") || 
								partAttachment.getMimeType().equalsIgnoreCase("image/png")) {
							partIntros.append("  <img src=\"/samigo");
							partIntros.append(partAttachment.getResourceId());
							partIntros.append("\" />");
						}
						else {
							partIntros.append("  ");
							partIntros.append(partAttachment.getFilename());
						}
					}
				}
			}
			pdfPart.setIntro(partIntros.toString());

			List pdfItems = new ArrayList();

			//for each item in a section we add a blank pdfItem to the pdfPart
			for (int j = 0; j < items.size(); j++) {
				PDFItemBean pdfItem = new PDFItemBean();

				ItemContentsBean item = (ItemContentsBean) items.get(j);
				
				StringBuffer legacy = new StringBuffer("<h3>");
				if (printSetting.getShowSequence().booleanValue()) {
					legacy.append(item.getSequence());
				}
				legacy.append("</h3>");

				pdfItem.setItemId(item.getItemData().getItemId());

				StringBuffer contentBuffer = new StringBuffer(); 

				if (!(TypeIfc.FILL_IN_BLANK.equals(item.getItemData().getTypeId()) || TypeIfc.FILL_IN_NUMERIC.equals(item.getItemData().getTypeId())  
					    || TypeIfc.CALCULATED_QUESTION.equals(item.getItemData().getTypeId()))) {
					contentBuffer.append("<br />");
					contentBuffer.append(convertFormattedText(item.getItemData().getText()));
					contentBuffer.append("<br />");
				}
				if (item.getItemData().getItemAttachmentList() != null && item.getItemData().getItemAttachmentList().size() > 0) {
					contentBuffer.append("<br />");
					contentBuffer.append(printMessages.getString("attachments"));
					contentBuffer.append("<br />");
					List itemAttachmentList = item.getItemData().getItemAttachmentList();
					Iterator itemAttachmentIter = itemAttachmentList.iterator();
					while (itemAttachmentIter.hasNext()) {
						PublishedItemAttachment itemAttachment = (PublishedItemAttachment) itemAttachmentIter.next();
						if (itemAttachment.getMimeType().equalsIgnoreCase("image/jpeg") || 
							itemAttachment.getMimeType().equalsIgnoreCase("image/pjpeg") || 
							itemAttachment.getMimeType().equalsIgnoreCase("image/gif") || 
							itemAttachment.getMimeType().equalsIgnoreCase("image/png")) {
							contentBuffer.append("  <img src=\"/samigo");
							contentBuffer.append(itemAttachment.getResourceId());
							contentBuffer.append("\" />");
						}
						else {
							contentBuffer.append("  ");
							contentBuffer.append(itemAttachment.getFilename());
						}
						contentBuffer.append("<br />");
						
					}
				}
				if (TypeIfc.FILL_IN_BLANK.equals(item.getItemData().getTypeId()) || TypeIfc.FILL_IN_NUMERIC.equals(item.getItemData().getTypeId())
						|| TypeIfc.CALCULATED_QUESTION.equals(item.getItemData().getTypeId())) {
					if (item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC)) {
						contentBuffer.append("<br />");
						contentBuffer.append(deliveryMessages.getString("fin_accepted_instruction"));
						contentBuffer.append("<br />");
					}
					contentBuffer.append("<br />");
					contentBuffer.append(convertFormattedText(item.getItemData().getText()));
					contentBuffer.append("<br />");
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING)) {
					contentBuffer.append(printMessages.getString("time_allowed_seconds"));
					contentBuffer.append(":");
					contentBuffer.append(item.getItemData().getDuration());
					contentBuffer.append("<br />");
					contentBuffer.append(printMessages.getString("number_of_tries"));
					contentBuffer.append(":");
					contentBuffer.append(item.getItemData().getTriesAllowed());
					contentBuffer.append("<br />");
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {
					contentBuffer.append(printMessages.getString("upload_instruction"));
					contentBuffer.append("<br />");
					contentBuffer.append(printMessages.getString("file"));
					contentBuffer.append(":");
					contentBuffer.append("<input type='text' size='50' />");
					contentBuffer.append("<input type='button' value='");
					contentBuffer.append(printMessages.getString("browse"));
					contentBuffer.append(":' />");
					contentBuffer.append("<input type='button' value='");
					contentBuffer.append(printMessages.getString("upload"));
					contentBuffer.append(":' />");
					contentBuffer.append("<br />");
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
						item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE) ||
						item.getItemData().getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY)) {

					List question = item.getItemData().getItemTextArraySorted();
					for (int k=0; k<question.size(); k++) {
						PublishedItemText itemtext = (PublishedItemText)question.get(k);
						List answers = itemtext.getAnswerArraySorted();

						contentBuffer.append("<table cols='20' width='100%'>");
						for (int t=0; t<answers.size(); t++) {
							PublishedAnswer answer = (PublishedAnswer)answers.get(t);
							if (StringUtils.isBlank(answer.getText())) break;
							contentBuffer.append("<tr>");
							contentBuffer.append(getContentAnswer(item, answer, printSetting));
							contentBuffer.append("</tr>");
						}
						contentBuffer.append("</table>");
					}
				}
				if (item.getItemData().getTypeId().equals(TypeIfc.MATCHING)) {
					contentBuffer.append("<table cols='20' width='100%'>");
					List question = item.getMatchingArray();
					for (int k=0; k<question.size(); k++) {
						MatchingBean matching = (MatchingBean)question.get(k);
						
						// if there are distractors or shared matches, answers will
						// have fewer answers that matches.
						String answer = "";
						if (k < item.getAnswers().size()) {
							answer = (String)item.getAnswers().get(k);
						}

						if (matching.getText() == null) break;
						
						contentBuffer.append("<tr><td colspan='10'>");
						contentBuffer.append(convertFormattedText(matching.getText()));
						contentBuffer.append("</td>");
						contentBuffer.append("<td colspan='10'>");
						contentBuffer.append(convertFormattedText(answer));
						contentBuffer.append("</td></tr>");
					}

					contentBuffer.append("</table>");
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.EXTENDED_MATCHING_ITEMS)) {
					contentBuffer.append("<table cols='20' width='100%'>");

					contentBuffer.append("<tr><td colspan='20'>");
					contentBuffer.append(item.getItemData().getThemeText());
					contentBuffer.append("</td></tr>");

					if (item.getItemData().getIsAnswerOptionsSimple()) {
						List<AnswerIfc> emiAnswerOptions = item.getItemData().getEmiAnswerOptions();
						for (AnswerIfc answerIfc : emiAnswerOptions) {
							contentBuffer.append("<tr><td colspan='20'>");
							contentBuffer.append(answerIfc.getLabel() + ". " + answerIfc.getText());
							contentBuffer.append("</td></tr>");
						}
					}

					if (item.getItemData().getIsAnswerOptionsRich()) {
						contentBuffer.append("<tr><td colspan='20'>");
						contentBuffer.append(item.getItemData().getEmiAnswerOptionsRichText());
						contentBuffer.append("</td></tr>");
					}

					contentBuffer.append("<tr><td colspan='20'>");
					contentBuffer.append(item.getItemData().getLeadInText());
					contentBuffer.append("</td></tr>");

					List<ItemTextIfc> questionAnswerCombinations = item.getItemData().getEmiQuestionAnswerCombinations();

					for (ItemTextIfc itemTextIfc : questionAnswerCombinations) {
						if (!itemTextIfc.getText().isEmpty()) {
							contentBuffer.append("<tr><td colspan='20'>");
							contentBuffer.append(itemTextIfc.getSequence() + ". " + itemTextIfc.getText() + "  ____");
							contentBuffer.append("</td></tr>");
						}
					}
					contentBuffer.append("<br />");
					contentBuffer.append("</table>");
				}
				if (printSetting.getShowKeys().booleanValue() || printSetting.getShowKeysFeedback().booleanValue()) {
					contentBuffer.append("<br />");
					contentBuffer.append(getContentQuestion(item, printSetting));
				}

				pdfItem.setContent(contentBuffer.toString());

				if (legacy != null) {
					pdfItem.setMeta(legacy.toString()); 
				}

				pdfItems.add(pdfItem);
			}

			pdfPart.setQuestions(pdfItems);
			pdfPart.setResources(resources);
			if (resources.size() > 0)
				pdfPart.setHasResources(Boolean.valueOf(true));
			pdfParts.add(pdfPart);

		}

		//set the new colleciton of PDF beans to be the contents of the pdfassessment
		setParts(pdfParts);

		setTitle(deliveryBean.getAssessmentTitle());

		return "print";
	}

	private String getContentAnswer(ItemContentsBean item, PublishedAnswer answer, PrintSettingsBean printSetting) {
		StringBuffer contentBuffer = new StringBuffer();
		
		if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {

			if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT))
				contentBuffer.append("<td colspan='1'><img src='/samigo-app/images/unchecked.gif' /></td>");
			else
				contentBuffer.append("<td colspan='1'><img src='/samigo-app/images/radiounchecked.gif' /></td>");
				
			if (printSetting.getShowKeysFeedback()) {
				contentBuffer.append("<td colspan='10'>");
				if (!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) {
					contentBuffer.append(answer.getLabel());
					contentBuffer.append(". ");
				}
				
				contentBuffer.append(convertFormattedText(answer.getText()));
				contentBuffer.append("</td>");
				contentBuffer.append("<td colspan='9'>");
				contentBuffer.append("<h6>");
				contentBuffer.append(commonMessages.getString("feedback"));
				contentBuffer.append(": ");
				if (answer.getGeneralAnswerFeedback() != null && !answer.getGeneralAnswerFeedback().equals(""))
					contentBuffer.append(convertFormattedText(answer.getGeneralAnswerFeedback()));
				else 
					contentBuffer.append("--------");
				contentBuffer.append("</h6>");
			}
			else {
				contentBuffer.append("<td colspan='19'>");
				if (!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) {
					contentBuffer.append(answer.getLabel());
					contentBuffer.append(". ");
				}
				
				AnswerSurveyConverter conv = new AnswerSurveyConverter();
				contentBuffer.append(convertFormattedText(conv.getAsString(null, null, answer.getText())));
				contentBuffer.append("</td>");
			}
			contentBuffer.append("</td>");
		}

		if (item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE)) {
			contentBuffer.append("<td colspan='1'><img src='/samigo-app/images/radiounchecked.gif' /></td>");
			contentBuffer.append("<td colspan='19'>");
			contentBuffer.append(convertFormattedText(answer.getText()));
			contentBuffer.append("</td>");
		}

		return contentBuffer.toString();
	}

	private String getContentQuestion(ItemContentsBean item, PrintSettingsBean printSetting) {
		StringBuffer contentBuffer = new StringBuffer("<h6>");
		
		contentBuffer.append(printMessages.getString("answer_point"));
		contentBuffer.append(": ");
		contentBuffer.append(item.getItemData().getScore());
		contentBuffer.append(" ");
		contentBuffer.append(authorMessages.getString("points_lower_case"));

		if (!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) &&
				!item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING) &&
				!item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {
			contentBuffer.append("<br />");
			if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)) {
				contentBuffer.append(printMessages.getString("answer_model"));
				contentBuffer.append(": ");
			}
			else {
				contentBuffer.append(printMessages.getString("answer_key"));
				contentBuffer.append(": ");
			}
				
			if (item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) || 
					item.getItemData().getTypeId().equals(TypeIfc.MATCHING))
				contentBuffer.append(item.getKey());
			else if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)) {
				if (item.getKey() != null && !item.getKey().equals("") && !item.getKey().equals("null"))
					contentBuffer.append(item.getKey());
				else
					contentBuffer.append("--------");
			}else if (item.getItemData().getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION)) {
				contentBuffer.append("<br />");
				//look for path on metadata
				String imsrc=item.getItemData().getItemMetaDataByLabel("IMAGE_MAP_SRC");
				if (imsrc==null)
					imsrc="";
					
				imsrc=imsrc.replaceAll(" ", "%20");
				
				String ext = "";		
				if (imsrc.lastIndexOf('.') > 0)
					ext = imsrc.substring(imsrc.lastIndexOf('.')+1);
				
				BufferedImage img_in=null;
				int w=-1;
				int h=-1;
				try {
					URL url = new URL(ServerConfigurationService.getServerUrl()+imsrc);
					img_in = ImageIO.read(url);
					w = img_in.getWidth(null);
					h = img_in.getHeight(null);					
				} catch (IOException e) {
					log.error(e.getMessage(), e);
			}
				
				Color c = new Color(27, 148, 224, 80);
				java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.BOLD, 10);
				
				//print areas over the image
				BufferedImage img_out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				try {
					Graphics g = img_out.getGraphics();
					g.setFont(font);
					g.drawImage(img_in, 0, 0, null);
					
					List question = item.getItemData().getItemTextArraySorted();
					for (int k=0; k<question.size(); k++) {
						PublishedItemText itemtext = (PublishedItemText)question.get(k);
						List answers=itemtext.getAnswerArray();
						PublishedAnswer answer = (PublishedAnswer)answers.get(0);
						
						String area = answer.getText();
						Integer areax1=Integer.valueOf(area.substring(area.indexOf("\"x1\":")+5,area.indexOf(",", area.indexOf("\"x1\":"))));
						Integer areay1=Integer.valueOf(area.substring(area.indexOf("\"y1\":")+5,area.indexOf(",", area.indexOf("\"y1\":"))));
						Integer areax2=Integer.valueOf(area.substring(area.indexOf("\"x2\":")+5,area.indexOf(",", area.indexOf("\"x2\":"))));
						Integer areay2=Integer.valueOf(area.substring(area.indexOf("\"y2\":")+5,area.indexOf("}", area.indexOf("\"y2\":"))));
						
						g.setColor(c);
						g.fillRect(areax1, areay1, (areax2-areax1), (areay2-areay1));
						
						g.setColor(Color.WHITE);
						g.drawRect(areax1, areay1, (areax2-areax1), (areay2-areay1));						

						g.setColor(Color.BLACK);
						g.drawString(""+(k+1), areax2-13, areay1+13);
					}
					g.dispose();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}			
					

				String destSrc = "temp://";
				try {
					File temp = File.createTempFile("imgtemp", "."+ext);
					ImageIO.write(img_out, ext, temp);
					destSrc+=temp.getCanonicalPath();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
				
				//print it
				contentBuffer.append("  <img src=\"");
				contentBuffer.append(destSrc);
				contentBuffer.append("\" />");
			}
			else if(TypeIfc.CALCULATED_QUESTION.equals( item.getItemData().getTypeId() )){
				contentBuffer.append(item.getAnswerKeyCalcQuestion());
			}
			else
				contentBuffer.append(item.getItemData().getAnswerKey());
		}

		if (printSetting.getShowKeysFeedback().booleanValue()) {

			if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION) ||
					item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {
				contentBuffer.append("<br />");
				contentBuffer.append(commonMessages.getString("feedback"));
				contentBuffer.append(": ");
				if (item.getItemData().getGeneralItemFeedback() != null && !item.getItemData().getGeneralItemFeedback().equals(""))
					contentBuffer.append(convertFormattedText(item.getItemData().getGeneralItemFeedback()));
				else 
					contentBuffer.append("--------");
			}

			if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
					item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
					item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
					item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) ||
					TypeIfc.CALCULATED_QUESTION.equals(item.getItemData().getTypeId()) ||
					item.getItemData().getTypeId().equals(TypeIfc.MATCHING)||
					item.getItemData().getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION)){
				contentBuffer.append("<br />");
				contentBuffer.append(printMessages.getString("correct_feedback"));
				contentBuffer.append(": ");
				if (item.getItemData().getCorrectItemFeedback() != null && !item.getItemData().getCorrectItemFeedback().equals(""))
					contentBuffer.append(convertFormattedText(item.getItemData().getCorrectItemFeedback()));
				else 
					contentBuffer.append("--------");
				contentBuffer.append("<br />");
				contentBuffer.append(printMessages.getString("incorrect_feedback"));
				contentBuffer.append(": ");
				if (item.getItemData().getInCorrectItemFeedback() != null && !item.getItemData().getInCorrectItemFeedback().equals(""))
					contentBuffer.append(convertFormattedText(item.getItemData().getInCorrectItemFeedback()));
				else 
					contentBuffer.append("--------");
			}

		}
		contentBuffer.append("</h6>");
		return contentBuffer.toString();
	}

	public String convertFormattedText(String text) {
	    //To preserve old behavior, set this to true
	    //Possibly consider using jsoup with a whitelist so some formatted text gets though, I'm not sure why this was here in the first place
	    if (ServerConfigurationService.getBoolean("samigo.pdf.convertformattedtext",false)) {
	        return FormattedText.convertPlaintextToFormattedText(FormattedText.convertFormattedTextToPlaintext(text));
	    }
	    return text;
	}
	
	public void getPDFAttachment() {
		prepDocumentPDF();
		ByteArrayOutputStream pdf = getStream();

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();

		response.reset();
		response.setHeader("Pragma", "public"); 
		response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0"); 

		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "attachment; filename=" + genName());   
		response.setContentLength(pdf.toByteArray().length);
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(pdf.toByteArray());
			out.flush();
		} 
		catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		finally {
			try {
				if (out != null) 
					out.close();
			} 
			catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		faces.responseComplete();
	}

	/**
	 * Converts all nice new school html into old school
	 * font tagged up html that HTMLWorker will actually
	 * parse right
	 * 
	 * @param input
	 */
	private String oldschoolIfy(String input) {

		if (log.isDebugEnabled())
			log.debug("starting oldschoolify with: " + input);
		
		
		StringBuffer text1 = new StringBuffer("<div><font color='#01a5cb' size='");
		int size1 = (int)(baseFontSize * 1.1);
		text1.append(size1);
		text1.append("'");
		input = input.replaceAll("<h1", text1.toString());
		input = input.replaceAll("<h2", text1.toString());
		
		StringBuffer text2 = new StringBuffer("<div><font color='#CCCCCC' size='");
		int size2 = (int)(baseFontSize * 1);
		text2.append(size2);
		text2.append("'");	
		input = input.replaceAll("<h3", text2.toString());

		StringBuffer text3 = new StringBuffer("<div><font size='");
		int size3 = (int)(baseFontSize * .85);
		text3.append(size3);
		text3.append("'");
		input = input.replaceAll("<h4", text3.toString());
		
		StringBuffer text4 = new StringBuffer("<div><font size='");
		int size4 = (int)(baseFontSize * .8);
		text4.append(size4);
		text4.append("'");
		input = input.replaceAll("<h5", text4.toString());
		
		StringBuffer text5 = new StringBuffer("<div><font color='#333333' size='");
		int size5 = (int)(baseFontSize * .6);
		text5.append(size5);
		text5.append("'");
		input = input.replaceAll("<h6", text5.toString());

		input = input.replaceAll("</h.>", "</font></div>");
		if(!input.startsWith("<div><font")){
			input = "<div><font size='"+baseFontSize+"'>#</font></div>".replace("#", input);
		}

		return input;
	}

	/**
	 * Turns a string into a StringReader with out the fuss of an IOException
	 * 
	 * @param input
	 * @return StringReader
	 */
	private Reader safeReader(String input) {
		StringReader output = null;
		if (input == null) {
			input = "";
		}
		else {
			input = oldschoolIfy(input);
		}

		try {
			output = new StringReader(input + "<br/>");
		}
		catch(Exception e) {
			log.error("could not get StringReader for String " + input + " due to : " + e);
		}
		return output;
	}

	public ByteArrayOutputStream getStream() {

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		try {

			if (log.isInfoEnabled())
				log.info("starting PDF generation" );

			Document document = new Document(PageSize.A4, 20, 20, 20, 20);
			PdfWriter docWriter = PdfWriter.getInstance(document, output);

			document.open();
			document.resetPageCount();

			HTMLWorker worker = new HTMLWorker(document);

			HashMap props = worker.getInterfaceProps();
			if (props == null) {
				props = new HashMap();
			}

			float prevs = 0;

			props.put("font_factory", new CustomFontFactory());
			props.put("img_baseurl", ServerConfigurationService.getServerUrl());
			worker.setInterfaceProps(props);

			//TODO make a real style sheet
			StyleSheet style = null;
			StringBuffer head = new StringBuffer(printMessages.getString("print_name_form"));
			head.append("<br />");
			head.append(printMessages.getString("print_score_form"));
			head.append("<br /><br /><h1>");
			head.append(title);
			head.append("</h1><br />");
			head.append(intro);
			head.append("<br />");

			//head = head.replaceAll("[ \t\n\f\r]+", " ");

			//parse out the elements from the html
			List elementBuffer = HTMLWorker.parseToList(safeReader(head.toString()), style, props);
			float[] singleWidth = {1f};
			PdfPTable single = new PdfPTable(singleWidth);
			single.setWidthPercentage(100f);
			PdfPCell cell = new PdfPCell();
			cell.setBorderWidth(0);
			for (int k = 0; k < elementBuffer.size(); k++) {    
				cell.addElement((Element)elementBuffer.get(k));          
			}
			single.addCell(cell);

			prevs += single.getTotalHeight() % document.getPageSize().getHeight();
			//TODO do we want a new page here ... thus giving the cover page look?

			document.add(single);
			document.add(Chunk.NEWLINE);

			//extract the html and parse it into pdf
			List parts = getHtmlChunks();
			for (int i = 0; i < parts.size(); i++) {
				//add new page to start each new part
				if (i > 0) {
					document.newPage();
				}

				PDFPartBean pBean = (PDFPartBean) parts.get(i);
				if (pBean.getIntro() != null && !"".equals(pBean.getIntro())) {
					elementBuffer = HTMLWorker.parseToList(safeReader(pBean.getIntro()), style, props);
					single = new PdfPTable(singleWidth);
					single.setWidthPercentage(100f);
					cell = new PdfPCell();
					cell.setBorderWidth(0);
					for (int k = 0; k < elementBuffer.size(); k++) {    
						cell.addElement((Element)elementBuffer.get(k));          
					}
					single.addCell(cell);

					prevs += single.getTotalHeight() % document.getPageSize().getHeight();
					document.add(single);
				}  

				List items = pBean.getQuestions();

				for (int j = 0; j < items.size(); j++) {
					PDFItemBean iBean = (PDFItemBean) items.get(j);

					float[] widths = {0.1f, 0.9f};
					PdfPTable table = new PdfPTable(widths);
					table.setWidthPercentage(100f);
					PdfPCell leftCell = new PdfPCell();
					PdfPCell rightCell = new PdfPCell();
					leftCell.setBorderWidth(0);
					leftCell.setPadding(0);
					leftCell.setLeading(0.00f, 0.00f);
					leftCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					leftCell.setVerticalAlignment(Element.ALIGN_TOP);
					rightCell.setBorderWidth(0);
					elementBuffer = HTMLWorker.parseToList(safeReader(iBean.getMeta()), style, props);
					for (int k = 0; k < elementBuffer.size(); k++) {
						Element element = (Element)elementBuffer.get(k);
						if (element instanceof Paragraph) {
							Paragraph p = (Paragraph)element;
							p.getFont().setColor(Color.GRAY);
							p.setAlignment(Paragraph.ALIGN_CENTER);
						}
						leftCell.addElement(element);
					}

					table.addCell(leftCell);

					elementBuffer = HTMLWorker.parseToList(safeReader(iBean.getContent()), style, props);
					for (int k = 0; k < elementBuffer.size(); k++) {
						Element element = (Element)elementBuffer.get(k);

						rightCell.addElement(element);
					}
					table.addCell(rightCell);

					if (table.getTotalHeight() + prevs > document.getPageSize().getHeight())
						document.newPage();

					document.add(table);
					document.add(Chunk.NEWLINE);

					//TODO add PDFTable and a collumn

					//worker.parse(safeReader(iBean.getMeta()));
					//TODO column break
					//worker.parse(safeReader(iBean.getContent()));
					//TODO end column and table
				}
			}

			document.close();
			docWriter.close();

		}
		catch(Exception e) {
			log.error(e.getMessage(), e);
		}

		return output;
	}

	public String getBaseFontSize() {
		return "" + baseFontSize;
	}

	public void setBaseFontSize(String baseFontSize) {
		this.baseFontSize = Integer.parseInt(baseFontSize);
	}


	/**
	 * @return the actionString
	 */
	public String getActionString() {
		return actionString;
	}


	/**
	 * @param actionString the actionString to set
	 */
	public void setActionString(String actionString) {
		this.actionString = actionString;
	}

	public String getSizeDeliveryParts() {

		return "" + deliveryParts.size();
	}

	public String getTotalQuestions() {

		int items = 0;
		for (int i=0; i<deliveryParts.size(); i++) {
			SectionContentsBean section = (SectionContentsBean) deliveryParts.get(i);
			items += section.getItemContents().size();

		}
		return "" + items;
	}

	/**
	 * This class pulls in the default font from sakai.properties
	 */
	protected class CustomFontFactory extends FontFactoryImp {
		private final String defaultFontname;

		public CustomFontFactory() {
			super();
			
			defaultFontname = ServerConfigurationService.getString("pdf.default.font", "DejaVu Sans");
			registerDirectory(this.getClass().getResource("fonts").getFile());
		}

		@Override
		public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color, boolean cached) {
			return super.getFont(defaultFontname, BaseFont.IDENTITY_H, embedded, size, style, color, cached);
		}
	}
}
