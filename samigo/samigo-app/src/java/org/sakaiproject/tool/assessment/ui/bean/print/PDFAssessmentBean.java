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
import com.lowagie.text.Rectangle;
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

	private String intro = "";

	private String title = "";

	private ArrayList parts = null;

	private ArrayList deliveryParts = null;

	private int baseFontSize = 8;

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
		this.intro = FormattedText.convertFormattedTextToPlaintext(intro);
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

		// Call all the listeners needed to populate the deliveryBean...
		BeginDeliveryActionListener beginDeliveryAL = new BeginDeliveryActionListener();
		DeliveryActionListener deliveryAL = new DeliveryActionListener();

		beginDeliveryAL.processAction(null);
		deliveryAL.processAction(null);

		deliveryBean = (DeliveryBean) ContextUtil.lookupBean("delivery");

		ResourceBundle resource = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.PrintMessages");
		intro = resource.getString("print_name_form");
		intro += "<br />";
		intro += resource.getString("print_score_form");
		intro += "<br />";

		PrintSettingsBean printSetting = (PrintSettingsBean) ContextUtil.lookupBean("printSettings");
		if (printSetting.getShowPartIntros().booleanValue())
			intro += "<br />" +deliveryBean.getInstructorMessage();

		title = deliveryBean.getAssessmentTitle();

		setDeliveryParts(deliveryBean.getTableOfContents().getPartsContents());
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
			ArrayList pdfItems = new ArrayList();

			//for each item in a section we add a blank pdfItem to the pdfPart
			for (int j = 0; j < items.size(); j++) {
				PDFItemBean pdfItem = new PDFItemBean();

				ItemContentsBean item = (ItemContentsBean) items.get(j);

				String legacy = "<h5>" + item.getNumber() +"</h5>";
				pdfItem.setItemId(item.getItemData().getItemId());

				String content = "<br />" + item.getItemData().getText() + "<br />";
				ArrayList question = item.getItemData().getItemTextArraySorted();
				for (int k=0; k<question.size(); k++) {
					PublishedItemText itemtext = (PublishedItemText)question.get(k);
					ArrayList answers = itemtext.getAnswerArraySorted();
					for (int t=0; t<answers.size(); t++) {
						PublishedAnswer answer = (PublishedAnswer)answers.get(t);

						if (!item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_BLANK) &&
								!item.getItemData().getTypeId().equals(TypeIfc.FILL_IN_NUMERIC) &&
								!item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)) {

							String srcImage = "/samigo-app/images/radiounchecked.gif";
							if (item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CORRECT)) {
								srcImage = "/samigo-app/images/unchecked.gif";
							}

							content += "<table cols='20' width='100%'><tr><td colspan='1'><img src='" + srcImage +"' /></td>";
							content += "<td colspan='19'>";
							if (!item.getItemData().getTypeId().equals(TypeIfc.TRUE_FALSE) &&
									!item.getItemData().getTypeId().equals(TypeIfc.MULTIPLE_CHOICE_SURVEY)) {

								content += answer.getLabel() + ". ";
							}
							content += answer.getText() + "</td></tr></table>";
						}

						if (item.getItemData().getTypeId().equals(TypeIfc.ESSAY_QUESTION)) {
							content += "__________________________________________________________________________";
							content += "<br /><br /><br /><br /><br />";
							content += "__________________________________________________________________________";
							content += "<br />";
						}
					}
				}
				pdfItem.setContent(content);

				if (legacy != null) {
					pdfItem.setMeta(legacy); 
				}

				pdfItems.add(pdfItem);
			}

			pdfPart.setIntro(section.getDescription());
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

	public void getPDFAttachment() {
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
		input = input.replaceAll("<h1", "<div><font size='" + (int)(baseFontSize * 2.2) + "'");
		input = input.replaceAll("<h2", "<div><font size='" + (int)(baseFontSize * 1.6) + "'");
		input = input.replaceAll("<h3", "<div><font size='" + (int)(baseFontSize * 1.3) + "'");
		input = input.replaceAll("<h4", "<div><font size='" + baseFontSize + "'");
		input = input.replaceAll("<h5", "<div><font size='" + (int)(baseFontSize * .85) + "'");
		input = input.replaceAll("<h6", "<div><font size='" + (int)(baseFontSize * .6) + "'");

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
			output = new StringReader(input + "<br>");
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

			if (intro != null)
				intro = intro.replaceAll("[ \t\n\f\r]+", " ");

			//parse out the elements from the html
			ArrayList elementBuffer = HTMLWorker.parseToList(safeReader(getIntro()), style, props);
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

				//worker.parse(safeReader(pBean.getIntro()));
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

}