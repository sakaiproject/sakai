package org.sakaiproject.tool.assessment.ui.bean.print;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Reader;
import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAnswer;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedItemText;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.pdf.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.PageSize;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
import org.sakaiproject.tool.assessment.ui.bean.print.settings.PrintSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import org.sakaiproject.util.FormattedText;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * 
 * @author Joshua Ryan <a href="mailto:joshua.ryan@asu.edu">joshua.ryan@asu.edu</a>
 *
 * This class is basically just a conveinceince class for abstracting the creation of
 * PDF's from assessments
 * 
 */
public class PDFAssessmentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(PDFAssessmentBean.class);

	private static ResourceBundle printMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.PrintMessages");

	private static ResourceBundle authorMessages = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.AuthorMessages");

	private String intro = "";

	private String title = "";

	private ArrayList parts = null;

	private ArrayList deliveryParts = null;

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
		this.intro = FormattedText.unEscapeHtml(intro);
	}

	/**
	 * gets the delivery bean parts of the assessment
	 * @return
	 */
	public ArrayList getDeliveryParts() {  
		return deliveryParts;
	}

	/**
	 * gets the parts of the assessment
	 * @return
	 */
	public ArrayList getParts() {  
		return parts;
	}

	/**
	 * gets what should be the full set of html chunks for an assessment
	 * @return
	 */
	public ArrayList getHtmlChunks() {
		return parts;
	}

	/**
	 * sets the delivery parts
	 * @param deliveryParts
	 */
	public void setDeliveryParts(ArrayList deliveryParts) {
		ArrayList parts = new ArrayList();
		int numberQuestion = 1;
		for (int i=0; i<deliveryParts.size(); i++) {
			SectionContentsBean section = new SectionContentsBean((org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean)deliveryParts.get(i));
			ArrayList items = section.getItemContents();

			// Renumbering
			for (int j=0; items != null && j<items.size(); j++) {
				ItemContentsBean itemContents = (ItemContentsBean)items.get(j);

				itemContents.setNumber(numberQuestion++);

				// Order answers in order (A, B, C, D)
				ArrayList question = itemContents.getItemData().getItemTextArraySorted();
				for (int k=0; k<question.size(); k++) {
					PublishedItemText itemtext = (PublishedItemText)question.get(k);
					ArrayList answers = itemtext.getAnswerArray();
					for (int t=0; t<answers.size(); t++) {
						PublishedAnswer answer = (PublishedAnswer)answers.get(t);
						if (answer.getLabel() != null && !answer.getLabel().equals(""))
							answer.setSequence(new Long(answer.getLabel().charAt(0) - 64));
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
	public void setParts(ArrayList parts) {
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

		if (log.isWarnEnabled())
			log.warn(fTitle.substring(0, end) + year + month + day + hour + min + sec + ".pdf");

		return (fTitle.substring(0, end) + year + month + day + hour + min + sec + ".pdf").replace(" ", "_");
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

		if (printSetting.getShowPartIntros().booleanValue() && deliveryBean.getInstructorMessage() != null)
			setIntro(deliveryBean.getInstructorMessage());

		ArrayList pdfParts = new ArrayList();

		//for each part in an assessment we add a pdfPart to the pdfBean
		for (int i = 0; i < deliveryParts.size(); i++) {
			//get the current item
			SectionContentsBean section = (SectionContentsBean) deliveryParts.get(i);
			ArrayList items = section.getItemContents();
			ArrayList resources = new ArrayList();

			//create a new part and empty list to fill with items
			PDFPartBean pdfPart = new PDFPartBean();
			pdfPart.setSectionId(section.getSectionId());
			if (printSetting.getShowPartIntros().booleanValue() && deliveryParts.size() > 1) 
				pdfPart.setIntro("<h2>" + authorMessages.getString("p") + " " + (i+1) + ": " + section.getTitle() + "</h2>");
			ArrayList pdfItems = new ArrayList();

			//for each item in a section we add a blank pdfItem to the pdfPart
			for (int j = 0; j < items.size(); j++) {
				PDFItemBean pdfItem = new PDFItemBean();

				ItemContentsBean item = (ItemContentsBean) items.get(j);

				String legacy = "<h3>" + item.getNumber() +"</h3>";
				pdfItem.setItemId(item.getItemData().getItemId());

				String content = "<br />" + item.getItemData().getText() + "<br />";

				if (item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING)) {
					content += printMessages.getString("time_allowed_seconds") + ": " + item.getItemData().getDuration();
					content += "<br />" + printMessages.getString("number_of_tries") + ": " + item.getItemData().getTriesAllowed();
					content += "<br />";
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {
					content += printMessages.getString("upload_instruction") + "<br />";

					content += printMessages.getString("file") + ": ";
					content += "<input type='text' size='50' />";
					content += "<input type='button' value='" + printMessages.getString("browse") + ":' />";
					content += "<input type='button' value='" + printMessages.getString("upload") + ":' />";
					content += "<br />";
				}

				if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
						item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
						item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE)) {

					ArrayList question = item.getItemData().getItemTextArraySorted();
					for (int k=0; k<question.size(); k++) {
						PublishedItemText itemtext = (PublishedItemText)question.get(k);
						ArrayList answers = itemtext.getAnswerArraySorted();

						content += "<table cols='20' width='100%'>";
						for (int t=0; t<answers.size(); t++) {
							PublishedAnswer answer = (PublishedAnswer)answers.get(t);
							if (answer.getText() == null) break;

							content += "<tr>" + getContentAnswer(item, answer, printSetting) + "</tr>";
						}
						content += "</table>";
					}
				}
				if (item.getItemData().getTypeId().equals(TypeIfc.MATCHING)) {

					content += "<table cols='20' width='100%'>";
					ArrayList question = item.getMatchingArray();
					for (int k=0; k<question.size(); k++) {
						MatchingBean matching = (MatchingBean)question.get(k);
						String answer = (String)item.getAnswers().get(k);

						if (matching.getText() == null) break;

						content += "<tr><td colspan='10'>";
						content += matching.getText();
						content += "</td>";
						content += "<td colspan='10'>";		  
						content += answer + "</td></tr>";
					}
					content += "</table>";
				}
				if (printSetting.getShowKeys().booleanValue() || printSetting.getShowKeysFeedback().booleanValue()) {
					content += "<br />" + getContentQuestion(item, printSetting);
				}

				pdfItem.setContent(content);

				if (legacy != null) {
					pdfItem.setMeta(legacy); 
				}

				pdfItems.add(pdfItem);
			}

			pdfPart.setQuestions(pdfItems);
			pdfPart.setResources(resources);
			if (resources.size() > 0)
				pdfPart.setHasResources(new Boolean(true));
			pdfParts.add(pdfPart);

		}

		//set the new colleciton of PDF beans to be the contents of the pdfassessment
		setParts(pdfParts);

		setTitle(deliveryBean.getAssessmentTitle());

		return "print";
	}

	private String getContentAnswer(ItemContentsBean item, PublishedAnswer answer, PrintSettingsBean printSetting) {
		String content = "";

		if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) ||
				item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION)) {

			if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT))
				content += "<td colspan='1'><img src='/samigo-app/images/unchecked.gif' /></td>";
			else
				content += "<td colspan='1'><img src='/samigo-app/images/radiounchecked.gif' /></td>";
			content += "<td colspan='10'>";
			if (!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) 
				content += answer.getLabel() + ". ";
			content += answer.getText();
			content += "</td>";
			content += "<td colspan='9'>";
			if (printSetting.getShowKeysFeedback()) {
				content += "<h6>" + printMessages.getString("feedback") + ": ";
				if (answer.getGeneralAnswerFeedback() != null && !answer.getGeneralAnswerFeedback().equals(""))
					content += answer.getGeneralAnswerFeedback();
				else 
					content += "--------";
				content += "</h6>";
			}
			content += "</td>";
		}

		if (item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE)) {
			content += "<td colspan='1'><img src='/samigo-app/images/radiounchecked.gif' /></td>";
			content += "<td colspan='19'>";
			content += answer.getText();
			content += "</td>";
		}

		return content;
	}

	private String getContentQuestion(ItemContentsBean item, PrintSettingsBean printSetting) {
		String content = "<h6>";

		content += printMessages.getString("answer_point") + ": " + item.getItemData().getScore();
		content +=  " " + authorMessages.getString("points_lower_case");

		if (!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY) &&
				!item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING) &&
				!item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {

			content += "<br />";
			if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION))
				content += printMessages.getString("answer_model") + ": ";
			else
				content += printMessages.getString("answer_key") + ": ";

			if (item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) || 
					item.getItemData().getTypeId().equals(TypeIfc.MATCHING))
				content += item.getKey();
			else if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)) {
				if (item.getKey() != null && !item.getKey().equals(""))
					content += item.getKey();
				else
					content += "--------";
			}
			else
				content += item.getItemData().getAnswerKey();
		}

		if (printSetting.getShowKeysFeedback().booleanValue()) {

			if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION) ||
					item.getItemData().getTypeId().equals(TypeIfc.AUDIO_RECORDING) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILE_UPLOAD)) {

				content += "<br />";
				content += printMessages.getString("feedback") + ": ";
				if (item.getItemData().getGeneralItemFeedback() != null && !item.getItemData().getGeneralItemFeedback().equals(""))
					content += item.getItemData().getGeneralItemFeedback();
				else 
					content += "--------";
			}

			if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT) ||
					item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE) ||
					item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT_SINGLE_SELECTION) ||
					item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK) ||
					item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) ||
					item.getItemData().getTypeId().equals(TypeIfc.MATCHING)) {

				content += "<br />";
				content += printMessages.getString("correct_feedback") + ": ";
				if (item.getItemData().getCorrectItemFeedback() != null && !item.getItemData().getCorrectItemFeedback().equals(""))
					content += item.getItemData().getCorrectItemFeedback();
				else 
					content += "--------";
				content += "<br />" + printMessages.getString("incorrect_feedback") + ": ";
				if (item.getItemData().getInCorrectItemFeedback() != null && !item.getItemData().getInCorrectItemFeedback().equals(""))
					content += item.getItemData().getInCorrectItemFeedback();
				else 
					content += "--------";
			}

		}

		return content + "</h6>";
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
			log.error(e);
			e.printStackTrace();
		} 
		finally {
			try {
				if (out != null) 
					out.close();
			} 
			catch (IOException e) {
				log.error(e);
				e.printStackTrace();
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
		input = input.replaceAll("<h1", "<div><font color='#01a5cb' size='" + (int)(baseFontSize * 1.1) + "'");
		input = input.replaceAll("<h2", "<div><font color='#01a5cb' size='" + (int)(baseFontSize * 1.1) + "'");
		input = input.replaceAll("<h3", "<div><font color='#CCCCCC' size='" + (int)(baseFontSize * 1) + "'");
		input = input.replaceAll("<h4", "<div><font size='" + (int)(baseFontSize * .85) + "'");
		input = input.replaceAll("<h5", "<div><font size='" + (int)(baseFontSize * .8) + "'");
		input = input.replaceAll("<h6", "<div><font color='#333333' size='" + (int)(baseFontSize * .6) + "'");

		input = input.replaceAll("</h.>", "</font></div>");

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

			props.put("img_baseurl", ServerConfigurationService.getServerUrl());
			worker.setInterfaceProps(props);

			//TODO make a real style sheet
			StyleSheet style = null;

			String head = printMessages.getString("print_name_form");
			head += "<br />";
			head += printMessages.getString("print_score_form");
			head += "<br /><br />";
			head += "<h1>" + title + "</h1><br />";
			head += intro + "<br />";
			//head = head.replaceAll("[ \t\n\f\r]+", " ");

			//parse out the elements from the html
			ArrayList elementBuffer = HTMLWorker.parseToList(safeReader(head), style, props);
			float[] singleWidth = {1f};
			PdfPTable single = new PdfPTable(singleWidth);
			single.setWidthPercentage(100f);
			PdfPCell cell = new PdfPCell();
			cell.setBorderWidth(0);
			for (int k = 0; k < elementBuffer.size(); k++) {    
				cell.addElement((Element)elementBuffer.get(k));          
			}
			single.addCell(cell);

			prevs += single.getTotalHeight() % document.getPageSize().height();
			//TODO do we want a new page here ... thus giving the cover page look?

			document.add(single);
			document.add(Chunk.NEWLINE);

			//extract the html and parse it into pdf
			ArrayList parts = getHtmlChunks();
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

					prevs += single.getTotalHeight() % document.getPageSize().height();
					document.add(single);
				}  

				ArrayList items = pBean.getQuestions();

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

					if (table.getTotalHeight() + prevs > document.getPageSize().height())
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
			e.printStackTrace();
			System.err.println("document: " + e.getMessage());
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

}
