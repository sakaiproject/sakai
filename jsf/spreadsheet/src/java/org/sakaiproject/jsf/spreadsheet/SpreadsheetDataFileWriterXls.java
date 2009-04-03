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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 */
public class SpreadsheetDataFileWriterXls implements SpreadsheetDataFileWriter {
	private static final Log log = LogFactory.getLog(SpreadsheetDataFileWriter.class);

	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			getAsWorkbook(spreadsheetData).write(out);
			out.flush();
		} catch (IOException e) {
			if (log.isErrorEnabled()) log.error(e);
		} finally {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				if (log.isErrorEnabled()) log.error(e);
			}
		}
	}
	
	private HSSFWorkbook getAsWorkbook(List<List<Object>> spreadsheetData) {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		Iterator<List<Object>> dataIter = spreadsheetData.iterator();
		
		// By convention, the first list in the list contains column headers.
		HSSFRow headerRow = sheet.createRow((short)0);
		List<Object> headerList = dataIter.next();
		for (short i = 0; i < headerList.size(); i++) {
			createCell(headerRow, i).setCellValue((String)headerList.get(i));
		}
		
		short rowPos = 1;
		while (dataIter.hasNext()) {
			List<Object> rowData = dataIter.next();
			HSSFRow row = sheet.createRow(rowPos++);
			for (short i = 0; i < rowData.size(); i++) {
				HSSFCell cell = createCell(row, i);
				Object data = rowData.get(i);
				if (data != null) {
					if (data instanceof Double) {
						cell.setCellValue(((Double)data).doubleValue());
					} else {
						cell.setCellValue(data.toString());
					}
				}
			}
		}
		
		return wb;
	}

	private HSSFCell createCell(HSSFRow row, short column) {
		HSSFCell cell = row.createCell(column);
		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
		return cell;
	}

}
