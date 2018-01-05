/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.spreadsheet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 *
 */
@Slf4j
public class SpreadsheetDataFileWriterXlsx implements SpreadsheetDataFileWriter {
	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		SpreadsheetUtil.setEscapedAttachmentHeader(response, fileName + ".xlsx");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(spreadsheetData).write(out);
			out.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	private Workbook getAsWorkbook(List<List<Object>> spreadsheetData) {
		Workbook wb = new SXSSFWorkbook();
		Sheet sheet = wb.createSheet();
		CellStyle headerCs = wb.createCellStyle();
		Iterator<List<Object>> dataIter = spreadsheetData.iterator();
		
		// Set the header style
		headerCs.setBorderBottom(BorderStyle.THICK);
		headerCs.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());


		// Set the font
		CellStyle cellStyle = null;
		String fontName = ServerConfigurationService.getString("spreadsheet.font");
		if (fontName != null) {
			Font font = wb.createFont();
			font.setFontName(fontName);
			headerCs.setFont(font);
			cellStyle = wb.createCellStyle();
			cellStyle.setFont(font);
		}

		// By convention, the first list in the list contains column headers.
		Row headerRow = sheet.createRow((short)0);
		List<Object> headerList = dataIter.next();
		for (short i = 0; i < headerList.size(); i++) {
			Cell headerCell = createCell(headerRow, i);
			headerCell.setCellValue((String)headerList.get(i));
			headerCell.setCellStyle(headerCs);
			//TODO
			//sheet.autoSizeColumn(i);
		}
		
		short rowPos = 1;
		while (dataIter.hasNext()) {
			List<Object> rowData = dataIter.next();
			Row row = sheet.createRow(rowPos++);
			for (short i = 0; i < rowData.size(); i++) {
				Cell cell = createCell(row, i);
				Object data = rowData.get(i);
				if (data != null) {
					if (data instanceof Double) {
						cell.setCellValue(((Double)data).doubleValue());
					} else {
						cell.setCellValue(data.toString());
					}
					if (cellStyle != null) {
						cell.setCellStyle(cellStyle);
					}
				}
			}
		}
		
		return wb;
	}

	private Cell createCell(Row row, short column) {
		Cell cell = row.createCell(Integer.valueOf(column).intValue());
		return cell;
	}

}
