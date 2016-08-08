/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.lti.impl;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;

import org.sakaiproject.lti.api.LTIExporter;

public class ExporterExcel implements LTIExporter {
	private final String sheetName = "LTI";
	
	private int rowNum, cellNum;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row = null;
	
	private static Log M_log = LogFactory.getLog(ExporterExcel.class);
	
	public ExporterExcel() {
		wb = new HSSFWorkbook();
		sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
		
		rowNum = 0;
		cellNum = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#newLine()
	 */
	@Override
	public void newLine() {
		row = sheet.createRow(rowNum++);
		cellNum = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#addCell(java.lang.String)
	 */
	@Override
	public void addCell(String text) {
		if(row != null)
			row.createCell(cellNum++).setCellValue(text);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#write()
	 */
	@Override
	public void write(OutputStream out) {
		try
		{
			wb.write(out);
		}
		catch (Exception e)
		{
			M_log.warn("Error exporting to Excel : "+e.getMessage());
		}
	}
}
