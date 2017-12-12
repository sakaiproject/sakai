package org.sakaiproject.jsf.spreadsheet;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.component.cover.ServerConfigurationService;

@Slf4j
public class SpreadsheetDataFileWriterPdf implements SpreadsheetDataFileWriter {
	private List<List<String>> studentInfo = new ArrayList<List<String>>();
	private static final int MAX_COLUMNS = 8;
	
	private static Font font, boldFont;

	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		response.setContentType("application/pdf");
		SpreadsheetUtil.setEscapedAttachmentHeader(response, fileName + ".pdf");
		
		Document document = new Document(new Rectangle(842.0f, 595.0f), 0, 0, 0, 0);
		//document.setMargins(0, 0, 0, 0);
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			
			if(spreadsheetData != null && spreadsheetData.size() > 0){
				int rows = spreadsheetData.size();
				int cols = spreadsheetData.get(0).size();
				//keeps track of where we left off when switching to a new table
				int startIndex = 0;
				
				while(startIndex < cols){
					document.add(new Paragraph("\n\n"));
					int subCols;
					if(startIndex == 0){
						subCols = (cols - startIndex > MAX_COLUMNS) ? MAX_COLUMNS : cols - startIndex;
					}else{
						//need to take into account that we tack on the first two columns (userId and name),
						subCols = (cols - startIndex + 2 > MAX_COLUMNS) ? MAX_COLUMNS - 2 : cols - startIndex;
					}
				
					Table t = new Table(startIndex == 0 ? subCols : subCols + 2, rows);
					t.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
					t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					//t.setBorderColor(new Color(220, 255, 100));
					t.setPadding(4);
					//t.setSpacing(5);
					t.setBorderWidth(1);
					
					for (int rowDataIndex = 0; rowDataIndex < spreadsheetData.size(); rowDataIndex++){
						List<Object> rowData = spreadsheetData.get(rowDataIndex);
						if(startIndex > 0){
							//need to add the studentId and name columns:
							Cell c1, c2; 
							if(rowDataIndex == 0){
								//set header cells:
								c1 = getHeaderCell(studentInfo.get(rowDataIndex).get(0));
								c2 = getHeaderCell(studentInfo.get(rowDataIndex).get(1));
							}else{
								c1 = new Cell(new Chunk(studentInfo.get(rowDataIndex).get(0), getBoldFont()));
								c2 = new Cell(new Chunk(studentInfo.get(rowDataIndex).get(1), getBoldFont()));
							}
							t.addCell(c1);
							t.addCell(c2);
						}else{
							//save the studentId and name columns for later use:							
							List<String> info = new ArrayList<String>();
							info.add(rowData.get(0).toString());
							info.add(rowData.get(1).toString());
							studentInfo.add(info);												
						}
						for(int i = 0; i < subCols; i++){
							StringBuilder sb = new StringBuilder();
							Object data = rowData.get(startIndex + i);
							if (data != null) {
								sb.append(data.toString());
							}
							Cell c;
							if(rowDataIndex == 0){
								//set header cells:								
								c = getHeaderCell(sb.toString());
								
							}else{
								if(startIndex == 0 && (i == 0 || i == 1)){
									c = new Cell(new Chunk(sb.toString(), getBoldFont()));
								}else{
									c = new Cell(new Chunk(sb.toString(), getFont()));	
								}							
							}
							t.addCell(c);
						}
					}
					startIndex += subCols;

					document.add(t);
					document.newPage();
					
				}
			}else{
				document.add(new Paragraph("There are no grade records to display"));
			}
			
		}catch (DocumentException | IOException e) {
			log.error(e.getMessage());
		}finally{
			document.close();
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

	}
	
	private Cell getHeaderCell(String headerTxt) throws BadElementException{
		//set header cells:								
		Cell c = new Cell(new Chunk(headerTxt, getBoldFont()));
		c.setHeader(true);
		c.setBackgroundColor(Color.LIGHT_GRAY);
		
		return c;
	}
	
	private static void initFont() {
		String fontName = ServerConfigurationService.getString("pdf.default.font");
		if (StringUtils.isNotBlank(fontName)) {
			FontFactory.registerDirectories();
			if (FontFactory.isRegistered(fontName)) {
				font = FontFactory.getFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				boldFont = FontFactory.getFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 10, Font.BOLD);
			} else {
				log.warn("Can not find font: " + fontName);
			}
		}
		if (font == null) {
			font = new Font();
			boldFont = new Font(Font.COURIER, 10, Font.BOLD);
		}
	}
	
	private static Font getFont() {
		if (font == null) {
			initFont();
		}
		return font;
	}
	
	private static Font getBoldFont() {
		if (boldFont == null) {
			initFont();
		}
		return boldFont;
	}
	
}
