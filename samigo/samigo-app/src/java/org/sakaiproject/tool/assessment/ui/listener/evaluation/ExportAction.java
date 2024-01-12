/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Rectangle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.scilab.forge.jlatexmath.TeXFormula;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ImageMapQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatrixSurveyBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.FinBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class ExportAction implements ActionListener {

    private ContentHostingService contentHostingService = org.sakaiproject.content.cover.ContentHostingService.getInstance();
	private static final ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AuthorMessages");
	private Color gray = new Color(221, 219, 219);
	private Color grayLight = new Color(231, 228, 228);
	private Color green = new Color(9, 215, 71);
	private Font boldFont = new Font(Font.TIMES_ROMAN, 13, Font.BOLD);
	private boolean isTable = false;
	private String LATEX_SEPARATOR_DOLLAR = "$$";
	private String[] LATEX_SEPARATOR_START = {"\\(", "\\["};
	private String[] LATEX_SEPARATOR_FINAL = {"\\)", "\\]"};

	/**
	 * Standard process action method.
	 * @param ae ActionEvent
	 */
	public void processAction(ActionEvent ae) {
		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) faces.getExternalContext().getResponse();
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		StudentScoresBean studentScoreBean = (StudentScoresBean) ContextUtil.lookupBean("studentScores");

		Document document = new Document(PageSize.A4, 20, 20, 20, 20);
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			PdfWriter docWriter = PdfWriter.getInstance(document, outputStream);
			document.open();
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=Report_" + studentScoreBean.getFirstName() + "_" + deliveryBean.getAssessmentTitle() + ".pdf");
			Paragraph studentNameParagraph = new Paragraph(studentScoreBean.getStudentName(), new Font(Font.TIMES_ROMAN, 22, Font.BOLD));
			document.add(studentNameParagraph);
			if (studentScoreBean.getEmail() != null && !StringUtils.equals(studentScoreBean.getEmail(), "")) {
				Paragraph studentEmailParagraph = new Paragraph("(" + studentScoreBean.getEmail() + ")", new Font(Font.TIMES_ROMAN, 8, Font.BOLD, Color.GRAY));
				document.add(studentEmailParagraph);
			}
			
			float[] pdfTableWidth = {2f, 1f};
			PdfPTable shortSummaryTable = new PdfPTable(pdfTableWidth);
			this.addCellToTable(shortSummaryTable, deliveryBean.getAssessmentTitle(), 0, 0);
			double currentScore = deliveryBean.getTableOfContents().getCurrentScore();
			double maxScore = deliveryBean.getTableOfContents().getMaxScore();
			DecimalFormat twoDecimalsFormat = new DecimalFormat("0.00");
			String scorePercentageString = (maxScore == 0) ? "0" : twoDecimalsFormat.format((currentScore / maxScore) * 100);
			this.addCellToTable(shortSummaryTable, rb.getFormattedMessage("score_format", new String[] { twoDecimalsFormat.format(currentScore), twoDecimalsFormat.format(maxScore), scorePercentageString }), 0, 0);
			document.add(shortSummaryTable);
			document.add(new Paragraph(Chunk.NEWLINE));

			int index = 0;
			List<SectionContentsBean> deliveryParts = deliveryBean.getPageContents().getPartsContents();
			for (SectionContentsBean deliveryPart : deliveryParts) {
				List<ItemContentsBean> items =	deliveryPart.getItemContents();
				
				Font blueBoldFont = new Font(Font.TIMES_ROMAN, 16, Font.BOLD);
				blueBoldFont.setColor(new Color(15, 76, 114));
				String partNumber = String.valueOf(deliveryPart.getNumber());
				String partTitle = deliveryPart.getText();
				String answeredQuestions = String.valueOf((deliveryPart.getQuestions() - deliveryPart.getUnansweredQuestions()));
				String questionsNumber = String.valueOf(deliveryPart.getQuestions());
				String partScore = twoDecimalsFormat.format(deliveryPart.getPoints());
				String partMaxScore = twoDecimalsFormat.format(deliveryPart.getMaxPoints());
				document.add(new Paragraph(rb.getFormattedMessage("short_summary.part_title", new String[]{partNumber, partTitle, answeredQuestions, questionsNumber, partScore, partMaxScore}), blueBoldFont));
				
				document.add(new Paragraph("\n"));

				shortSummaryTable = new PdfPTable(new float[]{2f, 0.5f, 0.7f, 0.7f});
				shortSummaryTable.setWidthPercentage(95f);
				this.addCellToTable(shortSummaryTable, rb.getString("short_summary.title"), 1, 0);
				this.addCellToTable(shortSummaryTable, rb.getString("short_summary.type"), 1, 0);
				this.addCellToTable(shortSummaryTable, rb.getString("short_summary.answered"), 1, 0);
				this.addCellToTable(shortSummaryTable, rb.getString("short_summary.score"), 1, 0);
				for (ItemContentsBean item : items) {
					int tableColor = (index % 2 == 0)? 1 : 2;
					PdfPCell questionTextCell = new PdfPCell(this.getQuestionTitle(++index + ". " + item.getText(), false));
					questionTextCell.setMinimumHeight(25f);
					questionTextCell.setPadding(5f);
					questionTextCell.setBorderWidth(0);
					questionTextCell.setBackgroundColor((tableColor == 1)? gray : grayLight);
					shortSummaryTable.addCell(questionTextCell);
					this.addCellToTable(shortSummaryTable, rb.getString("type." + item.getItemData().getTypeId()), 2, tableColor);
					this.addCellToTable(shortSummaryTable, (!item.isUnanswered() ? rb.getString("short_summary.answered.yes") : rb.getString("short_summary.answered.no")), 2, tableColor);
					this.addCellToTable(shortSummaryTable, (twoDecimalsFormat.format(item.getPoints()) + "/" + twoDecimalsFormat.format(item.getMaxPoints())), 2, tableColor);
				}
				document.add(shortSummaryTable);
				document.add(new Paragraph("\n"));
			}

			String comments = studentScoreBean.getComments();
			if (StringUtils.isNotEmpty(comments)) {
				document.add(new Paragraph(rb.getString("comments_for_student"), boldFont));
				document.add(new Paragraph(Chunk.NEWLINE));
				PdfPTable commentsTable = new PdfPTable(1);
				commentsTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
				this.addCellToTable(commentsTable, comments, 3, 0);
				document.add(commentsTable);
			}
			document.newPage();

			int questionsCuantity = index;
			int itemsIndex = 0;
			for (SectionContentsBean deliveryPart : deliveryParts) {
				List<ItemContentsBean> items =	deliveryPart.getItemContents();
				for (ItemContentsBean item : items) {
					Long questionType = item.getItemData().getTypeId();
					PdfPTable questionTable = new PdfPTable(2);
					questionTable.setWidthPercentage(50f);
					questionTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
					this.addCellToTable(questionTable, ( rb.getFormattedMessage("current_question", new String[]{String.valueOf(++itemsIndex), String.valueOf(questionsCuantity)}) ), 3, 1);
					this.addCellToTable(questionTable, (twoDecimalsFormat.format(item.getPoints()) + "/" + twoDecimalsFormat.format(item.getMaxPoints())), 3, 0);
					document.add(new Paragraph(Chunk.NEWLINE));
					document.add(questionTable);
					if (questionType == TypeIfc.FILL_IN_NUMERIC || questionType == TypeIfc.CALCULATED_QUESTION || questionType == TypeIfc.FILL_IN_BLANK) {
						this.processFillInQuestion(document, (questionType != TypeIfc.FILL_IN_BLANK)? item.getFinArray() : item.getFibArray(), (questionType != TypeIfc.FILL_IN_BLANK));
					} else {
						document.add(this.getQuestionTitle(item.getText(), true));
					}
					if (questionType == TypeIfc.ESSAY_QUESTION) {
						PdfPTable responseTable = new PdfPTable(1);
						responseTable.setWidthPercentage(95f);
						this.addCellToTable(responseTable, (item.getResponseText() != null) ? this.cleanText(item.getResponseText()) : item.getResponseText(), 4, 0);
						document.add(responseTable);
					}
					if (questionType == TypeIfc.FILE_UPLOAD) {
						if (item.getMediaArray().size() > 0) {
							document.add(new Paragraph(rb.getString("attachments") + ":"));
							for (MediaData mediaData : item.getMediaArray()) {
								document.add(new Paragraph(rb.getString("attachments.name") + mediaData.getFilename()));
							}
							
						} else {
							document.add(new Paragraph(rb.getString("no_attachments")));
						}
						
					} else if (questionType == TypeIfc.AUDIO_RECORDING) {
						if (item.getMediaArray().size() > 0) {
							document.add(new Paragraph(rb.getString("audio.record")));
						} else {
							document.add(new Paragraph(rb.getString("audio.no_record")));
						}
					}
					List matrixArray = item.getMatrixArray();
					List<Integer> columnsIndex = item.getColumnIndexList();
					String[] columns = item.getColumnArray();

					if (columns != null && columnsIndex != null && matrixArray != null) {
						
						PdfPTable matrixTable = new PdfPTable(columnsIndex.size()+1);
						matrixTable.setWidthPercentage(100f);
						this.addCellToTable(matrixTable, "", 4, 0);

						for (String column : columns) {
							this.addCellToTable(matrixTable, column, 5, 0);
						}
						for (Object matrix : matrixArray) {
							if (questionType == TypeIfc.MATRIX_CHOICES_SURVEY) {
								this.addCellToTable(matrixTable, (((MatrixSurveyBean) matrix).getItemText().getText()), 6, 0);
								for (String answer : ((MatrixSurveyBean) matrix).getAnswerSid()) {
									PdfPCell circleCell = new PdfPCell(new Paragraph(" "));
									circleCell.setBorderWidth(0);
									circleCell.setBorderWidthTop(1);
									circleCell.setMinimumHeight(20f);
									circleCell.setCellEvent(new CircleCellEvent(StringUtils.equals(answer, ((MatrixSurveyBean) matrix).getResponseId()), true));
									matrixTable.addCell(circleCell);
								}
							}
						}
						document.add(matrixTable);
					}
					String imageSrc = ServerConfigurationService.getServerUrl() + item.getImageSrc();
					if (questionType == TypeIfc.IMAGEMAP_QUESTION && imageSrc.length() > 0) {
						document.add(new Paragraph(Chunk.NEWLINE));
						Image image = Image.getInstance(imageSrc);

						PdfPTable tableImage = new PdfPTable(1);
						tableImage.setWidthPercentage(100f);
						PdfPCell cellImage = new PdfPCell();
						cellImage.setBorderWidth(0);
						cellImage.setPadding(0);
						cellImage.addElement(image);
						ArrayList<Rectangle> answerRectangles = new ArrayList<Rectangle>();
						for (Object answer : item.getAnswers()) {
							JSONObject jsonObject = new JSONObject((String) answer);
							answerRectangles.add(new Rectangle(jsonObject.getFloat("x1"), jsonObject.getFloat("y1"), jsonObject.getFloat("x2"), jsonObject.getFloat("y2")));
						}

						List<ItemGradingData> itemsGrading = item.getItemGradingDataArray();
						ArrayList<Circle> answerCircles = new ArrayList<Circle>();
						for (ItemGradingData itemGrading : itemsGrading) {
							if (itemGrading.getAnswerText() != null && !StringUtils.equals(itemGrading.getAnswerText(), "")) {
								JSONObject jsonObject = new JSONObject(itemGrading.getAnswerText());
								boolean xDefined = !StringUtils.equals(jsonObject.optString("x"), "undefined");
								boolean yDefined = !StringUtils.equals(jsonObject.optString("y"), "undefined");
								float x = (xDefined)? jsonObject.getFloat("x") : 0f;
								float y = (yDefined)? jsonObject.getFloat("y") : 0f;
								if (xDefined && yDefined) {
									answerCircles.add(new Circle(x, y, itemGrading.getPublishedItemTextId().intValue()));
								} else {
									answerCircles.add(new Circle(x, y, itemGrading.getPublishedItemTextId().intValue()));
								}
							}
						}
						cellImage.setCellEvent(new ImageMapQuestionCellEvent(answerCircles, answerRectangles, image.getWidth(), image.getHeight()));
						tableImage.addCell(cellImage);
						document.add(tableImage);
					}

					for (Object answer : item.getAnswers()) {
						if (questionType == TypeIfc.MULTIPLE_CHOICE || questionType == TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION 
								|| questionType == TypeIfc.MULTIPLE_CHOICE_SURVEY || questionType == TypeIfc.MULTIPLE_CORRECT) 
							{
							SelectionBean selectionBean = (SelectionBean) answer;
							PdfPTable multipleTable = new PdfPTable(1);
							multipleTable.setWidthPercentage(100f);

							PdfPCell multipleCell = new PdfPCell();
							if (questionType == TypeIfc.MULTIPLE_CHOICE_SURVEY) {
								String answerText = selectionBean.getAnswer().getText();
								if (answerText.matches("-?\\d+")) {
									multipleCell.setPhrase(new Paragraph("  " + answerText));
								} else {
									multipleCell.setPhrase(new Paragraph("  " + rb.getString(this.cleanText(answerText))));
								}
							} else {
								multipleCell.setPhrase(createLatexParagraph("  " + selectionBean.getAnswer().getLabel() + ". " + this.cleanText(selectionBean.getAnswer().getText())));
							}
							multipleCell.setBorderWidth(0);
							if (questionType == TypeIfc.MULTIPLE_CORRECT) {
								multipleCell.setCellEvent(new CheckboxCellEvent(selectionBean.getResponse()));
							} else {
								multipleCell.setCellEvent(new CircleCellEvent(selectionBean.getResponse()));
							}
							PdfPCell finalCell = new PdfPCell(multipleCell);
							if (selectionBean.getAnswer().getIsCorrect() != null && selectionBean.getResponse()) {
								finalCell.setCellEvent(new CheckOrCrossCellEvent(selectionBean.getAnswer().getIsCorrect()));
							}
							multipleTable.addCell(finalCell);
							document.add(multipleTable);
						} else if (questionType == TypeIfc.MATCHING || questionType == TypeIfc.EXTENDED_MATCHING_ITEMS) {
							document.add(new Paragraph(" - " + ((String) answer)));
						} else if (questionType == TypeIfc.TRUE_FALSE) {
							PdfPTable trueFalsequestionTable = new PdfPTable(1);
							trueFalsequestionTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
							SelectItem selectItem = (SelectItem) answer;
							PdfPCell questionCell = new PdfPCell(new Paragraph("  " + selectItem.getLabel()));
							questionCell.setBorderWidth(0);
							questionCell.setCellEvent(new CircleCellEvent(selectItem.getValue().equals(item.getResponseId())));
							PdfPCell finalCell = new PdfPCell(questionCell);
							if (selectItem.getValue().equals(item.getResponseId())) {
								finalCell.setCellEvent(new CheckOrCrossCellEvent(StringUtils.equals(selectItem.getDescription(), "true")));
							}
							trueFalsequestionTable.addCell(finalCell);
							document.add(trueFalsequestionTable);
						}
					}
					List matchingItems = item.getMatchingArray();
					if (matchingItems != null) {
						PdfPTable matchingTable = new PdfPTable(1);
						matchingTable.setWidthPercentage(100f);
						for (Object matchingItem : matchingItems) {
							if (questionType == TypeIfc.IMAGEMAP_QUESTION) {
								PdfPCell matchingCell = new PdfPCell(new Phrase(rb.getString("item") + " " + ((ImageMapQuestionBean) matchingItem).getText()));
								matchingCell.setBorderWidth(0);
								matchingCell.setCellEvent(new CheckOrCrossCellEvent((((ImageMapQuestionBean) matchingItem).getIsCorrect() != null)? ((ImageMapQuestionBean) matchingItem).getIsCorrect() : false));
								matchingTable.addCell(matchingCell);
							} else if (questionType == TypeIfc.MATCHING || questionType == TypeIfc.EXTENDED_MATCHING_ITEMS) {
								for (Object choice : ((MatchingBean) matchingItem).getChoices()) {
									if (((MatchingBean) matchingItem).getResponse() != null && ((MatchingBean) matchingItem).getResponse().equals(((SelectItem) choice).getValue())) {
										PdfPCell matchingCell = new PdfPCell(new Phrase(((SelectItem) choice).getLabel() + " ··> " + ((MatchingBean) matchingItem).getText()));
										matchingCell.setBorderWidth(0);
										matchingCell.setCellEvent(new CheckOrCrossCellEvent(((MatchingBean) matchingItem).getIsCorrect()));
										matchingTable.addCell(matchingCell);
									}
								}
								
							}
						}
						document.add(matchingTable);
					}
					
					Paragraph paragraph = new Paragraph();
					Font redFont = new Font();
					redFont.setColor(Color.RED);
					redFont.setStyle(Font.BOLD);
					if (questionType == TypeIfc.CALCULATED_QUESTION) {
						paragraph.add(new Phrase(rb.getString("correct_response") + ": ", redFont));
						paragraph.add(new Phrase(item.getKey()));
						document.add(paragraph);
					} else if (questionType == TypeIfc.FILL_IN_BLANK || questionType == TypeIfc.FILL_IN_NUMERIC) {
						paragraph.add(new Phrase(rb.getString("correct_response") + ": ", redFont));
						paragraph.add(new Phrase(item.getKey()));
						document.add(paragraph);
					} else if (questionType == TypeIfc.ESSAY_QUESTION) {
						if (item.getModelAnswerIsNotEmpty()) {
							paragraph.add(new Phrase(rb.getString("preview_model_short_answer") + ": ", redFont));
							paragraph.add(new Phrase(item.getKey()));
							document.add(paragraph);
						}
					} else if (questionType != TypeIfc.MULTIPLE_CHOICE_SURVEY && questionType != TypeIfc.MATRIX_CHOICES_SURVEY 
							&& questionType != TypeIfc.FILE_UPLOAD && questionType != TypeIfc.IMAGEMAP_QUESTION 
							&& questionType != TypeIfc.AUDIO_RECORDING)
					{
						paragraph.add(new Phrase(rb.getString("correct_response") + ": ", redFont));
						paragraph.add(new Phrase(item.getAnswerKeyTF()));
						document.add(paragraph);
					} 

					if (item.getGradingCommentIsNotEmpty() || item.getFeedbackIsNotEmpty()) {
						document.add(new Paragraph(rb.getString("comment_for_student"), boldFont));
						document.add(new Paragraph(Chunk.NEWLINE));
						PdfPTable commentTable = new PdfPTable(1);
						commentTable.setWidthPercentage(90f);
						commentTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
						if (item.getGradingCommentIsNotEmpty()) {
							PdfPCell commentCell = new PdfPCell(new Paragraph(createLatexParagraph(this.cleanText(item.getGradingComment()))));
							commentCell.setMinimumHeight(25f);
							commentCell.setPadding(5f);
							commentCell.setBorderColor(gray);
							commentTable.addCell(commentCell);
						}
						if (item.getFeedbackIsNotEmpty()) {
							PdfPCell commentCell = new PdfPCell(new Paragraph(createLatexParagraph(this.cleanText(item.getFeedback()))));
							commentCell.setMinimumHeight(25f);
							commentCell.setPadding(5f);
							commentCell.setBorderColor(gray);
							commentTable.addCell(commentCell);
						}
						document.add(commentTable);
					}
				}
			}

			outputStream.flush();
			outputStream.close();
			faces.responseComplete();
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			document.close();
		}

	}

	/**
	 * Method to create a cell using the sent text and configuration and added to the table sent.
	 * In the configuration variable there are 7 differents configurations. So:
	 *  - 0: create a cell without border, simulating bold black or gray content
	 *  - 1: create a cell without border and bolder text, setting gray color to the background
	 *  - 2: create a cell without border and setting gray colors to the background
	 *  - 3: create a cell and setting gray color to the border and background
	 *  - 4: create a cell, without border and with a special coloured font (black, green, gray or red)
	 *  - 5: create a cell, without border and center align
	 *  - 6: create a cell, without only the top border
	 * 
	 * And some of these configurations, like the 0, 2, 3 and 4 configuration, has selection of colors:
	 *  - 0. Font colors:
	 * 		· Black (color = 1)
	 * 		· Gray (color is something else)
	 *  - 2. BackgroundColor colors:
	 * 		· Gray (color = 1)
	 * 		· Light Gray (color is something else)
	 *  - 3. BackgroundColor colors:
	 * 		· Light Gray (color = 1)
	 * 		· Default color (color is something else)
	 *  - 4. Font colors:
	 * 		· Black (color = 0)
	 * 		· Green (color = 1)
	 * 		· Gray (color = 2)
	 * 		· Red (color is something else)
	 * 
	 * @param table - PdfPTable
	 * @param content - String
	 * @param configuration - int
	 * @param color - int
	 */
	private void addCellToTable(PdfPTable table, String content, int configuration, int color) {
		PdfPCell cell = new PdfPCell();
		cell.setBorderWidth(0);
		switch (configuration) {
			case 0:
				table.setWidthPercentage(100f);
				cell.setPhrase(new Paragraph(content, new Font(Font.TIMES_ROMAN, 12, Font.BOLD, (color == 1)? Color.BLACK : Color.GRAY)));
				break;
			case 1:
				cell.setPhrase(new Paragraph(content, boldFont));
				cell.setMinimumHeight(25f);
				cell.setPadding(5f);
				cell.setBackgroundColor(grayLight);
				break;
			case 2:
				cell.setPhrase(new Paragraph(content));
				cell.setMinimumHeight(25f);
				cell.setPadding(5f);
				cell.setBackgroundColor((color == 1)? gray : grayLight);
				break;
			case 3:
				cell.setPhrase(new Paragraph(content));
				cell.setBorderColor(gray);
				cell.setBorderWidth(1);
				if (color == 1) {
					cell.setBackgroundColor(grayLight);
				}
				cell.setPadding(8f);
				break;
			case 5:
				cell.setPhrase(new Paragraph(content));
				cell.setMinimumHeight(20f);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				break;
			case 6:
				cell.setPhrase(new Paragraph(content));
				cell.setBorderWidthTop(1);
				cell.setMinimumHeight(20f);
				break;
			default:
				cell.setPhrase(new Paragraph(content));
				break;
		}
		table.addCell(cell);
	}

	/**
	 * Method to get the question text and parse if has html and get only the table transforming 
	 * it into a PdfPTable
	 * 
	 * @param questionText - String
	 * @param showAllInformation - boolean
	 * @return - PdfPTable
	 */
	private PdfPTable getQuestionTitle(String questionText, boolean showAllInformation) {
		PdfPTable auxTable = new PdfPTable(1);
		String[] textSeparatedByLineBreak = questionText.split("<br />");
		String finalText = "";
		if (textSeparatedByLineBreak.length > 1) {
			for (String text : textSeparatedByLineBreak) {
				String cleanedText = this.cleanText(text);
				if (StringUtils.isNotEmpty(cleanedText)){
					finalText += cleanedText + "\n";
				}
			}
		} else {
			textSeparatedByLineBreak = (questionText.indexOf("\n") != -1? questionText.split("\n") : textSeparatedByLineBreak);
			for (String text : textSeparatedByLineBreak) {
				String cleanedText = this.cleanText(text);
				if (StringUtils.isNotEmpty(cleanedText)){
					finalText += cleanedText + "\n";
				}
			}
		}
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		if ((finalText.indexOf(LATEX_SEPARATOR_DOLLAR) != -1 || finalText.indexOf(LATEX_SEPARATOR_START[0]) != -1 || finalText.indexOf(LATEX_SEPARATOR_START[1]) != -1) && deliveryBean.getIsMathJaxEnabled()) {
			addLatexFunctionsToTable(finalText, auxTable);
		} else {
			this.addCellToTable(auxTable, finalText, 0, 1);
		}

		if (showAllInformation) {
			addTableElementsToTable(questionText, auxTable);
			addImageElementsToTable(questionText, auxTable);
		}

		PdfPCell finalQuestionCell = new PdfPCell(auxTable);
		finalQuestionCell.setBorderWidth(1);
		finalQuestionCell.setBorderColor(grayLight);
		finalQuestionCell.setPadding(8f);
		PdfPTable finalTable = new PdfPTable(1);
		finalTable.setWidthPercentage(90f);
		finalTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		finalTable.addCell(finalQuestionCell);

		return finalTable;
	}

	/**
	 * Method to add the Table Elements from a String to a PdfPTable
	 * 
	 * @param text - string that contain the latex functions
	 * @param table - PdfPTable where save the resolved text
	 */
	private void addLatexFunctionsToTable(String text, PdfPTable table) {
		Paragraph latexParagraph = createLatexParagraph(text);
		PdfPCell latexCell = new PdfPCell(latexParagraph);
		latexCell.setBorderWidth(0);
		table.addCell(latexCell);
	}

	/**
	 * Method to create a Paragraph with Latex functions.
	 * 
	 * @param text
	 * @return Paragraph latexParagraph
	 */
	private Paragraph createLatexParagraph(String text) {
		Paragraph latexParagraph = new Paragraph();
		String[] searchIndex = {LATEX_SEPARATOR_DOLLAR, LATEX_SEPARATOR_START[0], LATEX_SEPARATOR_START[1]};
		DeliveryBean deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");
		if ((text.indexOf(searchIndex[0]) != -1 || text.indexOf(searchIndex[1]) != -1 || text.indexOf(searchIndex[2]) != -1) && deliveryBean.getIsMathJaxEnabled()) {
			String[] finalSearchIndex = {LATEX_SEPARATOR_DOLLAR, LATEX_SEPARATOR_FINAL[0], LATEX_SEPARATOR_FINAL[1]};
			int currentSearch = 1;
			if (text.indexOf(searchIndex[0]) != -1) {
				currentSearch = 0;
			}
			if (text.indexOf(searchIndex[1]) != -1){
				currentSearch = (text.indexOf(searchIndex[0]) != -1 && text.indexOf(searchIndex[0]) < text.indexOf(searchIndex[1]))? 0 : 1;
			} else if (text.indexOf(searchIndex[2]) != -1) {
				currentSearch = (text.indexOf(searchIndex[0]) != -1 && text.indexOf(searchIndex[0]) < text.indexOf(searchIndex[2]))? 0 : 2;
			}
			
			int latexInitIndex = text.indexOf(searchIndex[currentSearch]);
			int latexFinalIndex = text.indexOf(finalSearchIndex[currentSearch], latexInitIndex + 2);
			while (latexInitIndex != -1 && latexFinalIndex != -1) {
				String textBeforeLatex = text.substring(0, latexInitIndex);
				String latex = text.substring(latexInitIndex + 2, latexFinalIndex).replace(searchIndex[currentSearch], "").replace(finalSearchIndex[currentSearch], "");
				TeXFormula formula = new TeXFormula(latex);
				Image pdfLatexImage = null;
				try {
					pdfLatexImage = Image.getInstance(formula.createBufferedImage(TeXFormula.BOLD, 300, null, null), null);
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
				float finalWidth = formula.createBufferedImage(TeXFormula.BOLD, 10, null, null).getWidth(null);
				float finalHeight = formula.createBufferedImage(TeXFormula.BOLD, 10, null, null).getHeight(null);
				pdfLatexImage.scaleAbsolute(finalWidth, finalHeight);

				latexParagraph.add(new Chunk(textBeforeLatex));
				latexParagraph.add(new Chunk(pdfLatexImage, -1, -2, true));

				currentSearch = 1;
				if (text.indexOf(searchIndex[0], latexFinalIndex + 1) != -1) {
					currentSearch = 0;
					if (text.indexOf(searchIndex[1], latexFinalIndex + 2) != -1) {
						currentSearch = text.indexOf(searchIndex[0], latexFinalIndex + 2) < text.indexOf(searchIndex[1], latexFinalIndex + 2) ? 0 : 1;
					} else if (text.indexOf(searchIndex[2], latexFinalIndex + 2) != -1) {
						currentSearch = text.indexOf(searchIndex[0], latexFinalIndex + 2) < text.indexOf(searchIndex[2], latexFinalIndex + 2)? 0 : 2;
					}
				}

				latexInitIndex = text.indexOf(searchIndex[currentSearch], latexFinalIndex + 1);
				
				if (latexInitIndex != -1) {
					textBeforeLatex = text.substring(latexFinalIndex, latexInitIndex).replace(LATEX_SEPARATOR_DOLLAR, "")
							.replace(LATEX_SEPARATOR_START[0], "").replace(LATEX_SEPARATOR_FINAL[0], "")
							.replace(LATEX_SEPARATOR_START[1], "").replace(LATEX_SEPARATOR_FINAL[1], "");
					latexFinalIndex = text.indexOf(finalSearchIndex[currentSearch], latexInitIndex + 2);
				}
			}
			latexParagraph.add(new Chunk(text.substring(latexFinalIndex).replace(LATEX_SEPARATOR_DOLLAR, "").replace(LATEX_SEPARATOR_START[0], "")
					.replace(LATEX_SEPARATOR_FINAL[0], "").replace(LATEX_SEPARATOR_START[1], "").replace(LATEX_SEPARATOR_FINAL[1], "")));
		} else {
			latexParagraph.add(new Chunk(text));
		}
		return latexParagraph;
	}

	/**
	 * Method to add the Table Elements from a String to a PdfPTable
	 * 
	 * @param text - string that contain the Table Elements functions
	 * @param table - PdfPTable where save the resolved text
	 */
	private void addTableElementsToTable(String text, PdfPTable table){
		try {
			Elements tables = Jsoup.parse(text).select("table");
			for (org.jsoup.nodes.Element tableElement : tables) {
				PdfPTable pdfTable = new PdfPTable(tableElement.select("tr").first().children().size());
				for (org.jsoup.nodes.Element row : tableElement.select("tr")) {
					for (org.jsoup.nodes.Element cell : row.children()) {
						PdfPCell contentCell = new PdfPCell(new Paragraph(cell.text()));
						contentCell.setBorderWidth(0);
						contentCell.setBorderWidthBottom(1);
						
						contentCell.setPadding(5f);
						pdfTable.addCell(contentCell);
					}
				}
				PdfPCell questionCell = new PdfPCell(pdfTable);
				questionCell.setBorderWidth(0);
				table.addCell(questionCell);
				PdfPCell blankLine = new PdfPCell(new Paragraph(Chunk.NEWLINE));
				blankLine.setBorderWidth(0);
				table.addCell(blankLine);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	/**
	 * Method to add the Image Elements from a String to a PdfPTable
	 * 
	 * @param text - string that contain the Image Elements functions
	 * @param table - PdfPTable where save the resolved text
	 */
	private void addImageElementsToTable(String text, PdfPTable table){
		try {
			Elements imageElements = Jsoup.parse(text).select("img");
			for (org.jsoup.nodes.Element imageElement : imageElements) {
				String imageSrc = imageElement.attr("src");
				Image image = Image.getInstance(contentHostingService.getResource(imageSrc.replace(ServerConfigurationService.getAccessUrl() + "/content", "")).getContent());
				float originalWidth = image.getWidth();
				float originalHeight = image.getHeight();
				float newHeight = PageSize.A4.getHeight() * 0.25f;
				float newWidth = (originalWidth * newHeight) / originalHeight;
				if (newWidth > (PageSize.A4.getWidth() * 0.8)) {
					image.scalePercent((PageSize.A4.getWidth() / originalWidth) * 80);
				} else {
					image.scaleAbsoluteHeight(newHeight);
					image.scaleAbsoluteWidth(newWidth);
				}
				PdfPCell imageCell = new PdfPCell(image);
				imageCell.setBorderWidth(0);
				table.addCell(imageCell);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	/**
	 * Method to get only the text without the html tag
	 * 
	 * @param text - String
	 * @return - String
	 */
	private String cleanText(String text) {
		String textAux = text;
		int tableIndex = textAux.indexOf("<table");
		int tableIndexFinal = textAux.indexOf("</table>", tableIndex);
		if (tableIndex != -1) {
			isTable = true;
		}
		if (isTable) {
			if (tableIndexFinal != -1) {
				if (tableIndex != -1) {
					while (tableIndex != -1) {
						textAux = (textAux.substring(0, tableIndex) + textAux.substring(tableIndexFinal));
						tableIndex = textAux.indexOf("<table");
						tableIndexFinal = textAux.indexOf("</table>", tableIndex);
					}
					isTable = false;
				} else {
					isTable = false;
				}
								
			} else {
				textAux = "";
			}
		}
		return Jsoup.parse(textAux).text();
	}
	
	/**
	 * Method to process the fill in questions text
	 * 
	 * @param Document - document
	 * @param List - fillInArray
	 * @param boolean - numeric
	 */
	private void processFillInQuestion(Document document, List fillInArray, boolean numeric) throws Exception {
		PdfPTable fillInTable = new PdfPTable(1);
		fillInTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		int i = 0;
		String questionText = "";
		for (Object fillInObject : fillInArray) {
  			if (numeric) {
				if (i + 1 != fillInArray.size()) {
					questionText += ((FinBean) fillInObject).getText() + "(" + (++i) + ")";
					PdfPCell fillInCell = new PdfPCell(new Phrase((i) + ". " + (StringUtils.equals(((FinBean) fillInObject).getResponse(), "")? rb.getString("no_answer.text") : ((FinBean) fillInObject).getResponse())));
					fillInCell.setBorderWidth(0);
					fillInCell.setCellEvent(new CheckOrCrossCellEvent(((FinBean) fillInObject).getIsCorrect()!= null && ((FinBean) fillInObject).getIsCorrect()));
					fillInTable.addCell(fillInCell);
				} else {
					questionText += ((FinBean) fillInObject).getText();
				}
			} else {
				if (i + 1 != fillInArray.size()) {
					questionText += ((FibBean) fillInObject).getText() + "(" + (++i) + ")";
					PdfPCell fillInCell = new PdfPCell(new Phrase((i) + ". " + (StringUtils.equals(((FibBean) fillInObject).getResponse(), "")? rb.getString("no_answer.text") : ((FibBean) fillInObject).getResponse())));
					fillInCell.setBorderWidth(0);
					fillInCell.setCellEvent(new CheckOrCrossCellEvent(((FibBean) fillInObject).getIsCorrect()!= null && ((FibBean) fillInObject).getIsCorrect()));
					fillInTable.addCell(fillInCell);
				} else {
					questionText += ((FibBean) fillInObject).getText();
				}
			}
		}
		document.add(this.getQuestionTitle(questionText, true));
		document.add(fillInTable);
	}

	/**
	 * Class to handle the CircleCellEvent (equivalent to the radio in html)
	 */
	private static class CircleCellEvent implements PdfPCellEvent {
		private boolean checked = false;
		private boolean centered = false;

		public CircleCellEvent(boolean checked) {
			this.checked = checked;
		}
		public CircleCellEvent(boolean checked, boolean centered) {
			this.checked = checked;
			this.centered = centered;
		}

		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
			PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
			float xAux = (centered) ? (position.getRight() - position.getLeft()) / 2 + position.getLeft() + 1 : position.getLeft() + 1;
			float yAux = position.getTop() - 10;
			float radius = 5f;

			canvas.circle(xAux, yAux, radius);
			canvas.setColorFill(Color.BLACK);
			canvas.fill();
			canvas.circle(xAux, yAux, radius * 0.95f);
			canvas.setColorFill(Color.WHITE);
			canvas.fill();
			if (checked) {
				canvas.circle(xAux, yAux, radius * 0.6f);
				canvas.setColorFill(Color.BLACK);
				canvas.fill();
			}

			canvas.setColorFill(Color.BLACK);
			canvas.setColorStroke(Color.BLACK);
		}
	}

	/**
	 * Class to handle the CheckboxCellEvent (equivalent to the checkbox in html)
	 */
	private static class CheckboxCellEvent implements PdfPCellEvent {
		private boolean checked = false;

		public CheckboxCellEvent(boolean checked) {
			this.checked = checked;
		}

		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
			PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
			float xAux = position.getLeft() - 1;
			float yAux = position.getTop() - 14;

			canvas.rectangle(xAux, yAux, 8, 8);
			canvas.stroke();

			if (checked) {
				canvas.moveTo(xAux + 1.5f, yAux + 1.5f);
				canvas.lineTo(xAux + 6.5f, yAux + 6.5f);
				canvas.moveTo(xAux + 1.5f, yAux + 6.5f);
				canvas.lineTo(xAux + 6.5f, yAux + 1.5f);
				canvas.stroke();
			}
		}
	}

	/**
	 * Class to handle the ImageMapQuestionCellEvent
	 */
	private static class ImageMapQuestionCellEvent implements PdfPCellEvent {
		private ArrayList<Rectangle> answerRectangles = new ArrayList<Rectangle>();
		private ArrayList<Circle> answerCircles = new ArrayList<Circle>();
		private float originalWidth;
		private float originalHeight;

		public ImageMapQuestionCellEvent(ArrayList<Circle> answerCircles, ArrayList<Rectangle> answerRectangles, float originalWidth, float originalHeight) {
			this.answerRectangles = answerRectangles;
			this.answerCircles = answerCircles;
			this.originalWidth = originalWidth;
			this.originalHeight = originalHeight;
		}

		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
			PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
			float x = position.getLeft();
			float y = position.getBottom();
			
			float scaleX = position.getWidth() / (originalWidth);
			float scaleY = position.getHeight() / (originalHeight);
			for (Rectangle answerRectangle : answerRectangles) {
				PdfGState transparentState = new PdfGState();
				transparentState.setFillOpacity(0.65f);
				transparentState.setStrokeOpacity(1f);
				float transformedX = x + answerRectangle.getLeft() * scaleX;
				float transformedY = y + position.getHeight() - answerRectangle.getTop() * scaleY;
				float transformedW = answerRectangle.getWidth() * scaleX;
				float transformedH = answerRectangle.getHeight() * scaleY;
				canvas.rectangle(transformedX, transformedY, transformedW, transformedH);
				canvas.setColorFill(Color.BLUE);
				canvas.setColorStroke(Color.BLACK);
				canvas.setGState(transparentState);
			}
			canvas.fillStroke();
			canvas.fill();
			
			float radius = 3f;
			int smallestValue = (answerCircles.size() > 0)? answerCircles.get(0).getPublishedItemId() + 1 : 0;
			for (Circle answerCircle : answerCircles) {
				PdfGState transparentState = new PdfGState();
				transparentState.setFillOpacity(0.3f);
				transparentState.setStrokeOpacity(0.8f);
				float transformedX = x + answerCircle.getX() * scaleX;
				float transformedY = y + position.getHeight() - answerCircle.getY() * scaleY;
				if (answerCircle.getX() != 0 && answerCircle.getY() != 0) {
					canvas.circle(transformedX, transformedY, radius);
					canvas.setGState(transparentState);
					canvas.circle(transformedX, transformedY, 0.3f);
					canvas.setColorFill(Color.YELLOW);
					canvas.setColorStroke(Color.YELLOW);
				}
				if (answerCircle.getPublishedItemId() < smallestValue) {
					smallestValue = answerCircle.getPublishedItemId();
				}
			}
			canvas.fillStroke();
			canvas.fill();

			int questionIndex = 1;
			for (Rectangle answerRectangle : answerRectangles) {
				PdfGState transparentState = new PdfGState();
				transparentState.setFillOpacity(1f);
				transparentState.setStrokeOpacity(1f);
				float transformedX = x + answerRectangle.getLeft() * scaleX;
				float transformedY = y + position.getHeight() - answerRectangle.getTop() * scaleY;
				float transformedW = answerRectangle.getWidth() * scaleX;
				float transformedH = answerRectangle.getHeight() * scaleY;
				canvas.setGState(transparentState);
				try {
					canvas.beginText();
					canvas.setColorFill(Color.BLUE);
					canvas.setFontAndSize(BaseFont.createFont(), 9);
					canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(questionIndex), transformedX + transformedW + 1, transformedY, 0);
					canvas.endText();
					questionIndex++;
				} catch (Exception ex) {
					log.error("Cannot write the number of the ImageMap. " + ex.getMessage());
				}
			}

			int toReduce = smallestValue;
			for (Circle answerCircle : answerCircles) {
				float transformedX = x + answerCircle.getX() * scaleX;
				float transformedY = y + position.getHeight() - answerCircle.getY() * scaleY;
				if (answerCircle.getX() != 0 && answerCircle.getY() != 0) {
					try {
						int answerIndex = answerCircles.size() - (answerCircle.getPublishedItemId() - toReduce);
						canvas.beginText();
						canvas.setColorFill(Color.YELLOW);
						canvas.setFontAndSize(BaseFont.createFont(), 9);
						canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(answerIndex), transformedX + 4, transformedY - 3, 0);
						canvas.endText();
						canvas.fill();
					} catch (Exception ex) {
						log.error("Cannot write the number of the ImageMap. " + ex.getMessage());
					}
				}
			}

			PdfGState transparentState = new PdfGState();
			transparentState.setFillOpacity(1f);
			transparentState.setStrokeOpacity(1f);
			canvas.setGState(transparentState);
			canvas.setColorFill(Color.BLACK);
			canvas.setColorStroke(Color.BLACK);
		}
	}

	/**
	 * Class to handle the CheckOrCrossCellEvent (equivalent to the check and cross icon)
	 */
	private static class CheckOrCrossCellEvent implements PdfPCellEvent {
		private boolean isCheckIcon = false;

		public CheckOrCrossCellEvent(boolean isCheckIcon) {
			this.isCheckIcon = isCheckIcon;
		}

		public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
			PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
			float xAux = position.getLeft() - (isCheckIcon? 12 : 10);
			float yAux = position.getTop() - (isCheckIcon? 12 : 10);
			canvas.setLineWidth(2.5f);

			if (isCheckIcon) {
				canvas.setColorFill(new Color(9, 215, 71));
				canvas.setColorStroke(new Color(9, 215, 71));
				canvas.moveTo(xAux - 3, yAux + 3);
				canvas.lineTo(xAux, yAux);
				canvas.lineTo(xAux + 6, yAux + 5);
			} else {
				canvas.setColorFill(Color.RED);
				canvas.setColorStroke(Color.RED);
				canvas.moveTo(xAux, yAux);
				canvas.lineTo(xAux - 3, yAux + 3);
				canvas.lineTo(xAux + 3, yAux - 3);
				canvas.moveTo(xAux, yAux);
				canvas.lineTo(xAux + 3, yAux + 3);
				canvas.lineTo(xAux - 3, yAux - 3);
			}
			canvas.stroke();
			canvas.setLineWidth(1f);
			canvas.setColorFill(Color.BLACK);
			canvas.setColorStroke(Color.BLACK);
		}
	}

	/**
	 * Class used to facilitate the use of the circles in the ImageMapQuestionCellEvent
	 */
	private class Circle {
		@Setter @Getter
		private float x;
		@Setter @Getter
		private float y;
		@Setter @Getter
		private int publishedItemId;

		public Circle(float x, float y, int publishedItemId) {
			this.x = x;
			this.y = y;
			this.publishedItemId = publishedItemId;
		}
	}

}
