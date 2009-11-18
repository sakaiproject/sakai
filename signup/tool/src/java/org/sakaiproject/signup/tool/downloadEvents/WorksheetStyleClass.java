/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Yale University
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *   
 * See the LICENSE.txt distributed with this file.
 *
 **********************************************************************************/
package org.sakaiproject.signup.tool.downloadEvents;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Create a library of cell styles
 * 
 * @author Peter Liu
 */
public class WorksheetStyleClass {
	
	public static Map<String, CellStyle> createStyles(Workbook wb) {
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style;
		Font titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(titleFont);
		styles.put("title", style);

		Font commentTitleFont = wb.createFont();
		commentTitleFont.setFontHeightInPoints((short) 12);
		commentTitleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		commentTitleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(commentTitleFont);
		styles.put("commentTitle", style);

		Font itemFont = wb.createFont();
		itemFont.setFontHeightInPoints((short) 10);
		itemFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setFont(itemFont);
		styles.put("item_left", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("item_left_wrap", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("item_left_wrap_top", style);

		itemFont.setFontHeightInPoints((short) 10);
		itemFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(itemFont);
		styles.put("tabItem_fields", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		style.setFont(itemFont);
		styles.put("item_right", style);

		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("attendee_layout", style);

		Font itemBoldFont = wb.createFont();
		itemBoldFont.setFontHeightInPoints((short) 10);
		itemBoldFont.setFontName("Trebuchet MS");
		itemBoldFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_TOP);
		itemBoldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(itemBoldFont);
		styles.put("item_leftBold", style);

		Font tableFont = wb.createFont();
		tableFont.setFontHeightInPoints((short) 12);
		tableFont.setColor(IndexedColors.WHITE.getIndex());
		tableFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(tableFont);
		styles.put("tabColNames", style);

		tableFont.setFontHeightInPoints((short) 10);
		tableFont.setColor(IndexedColors.WHITE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(tableFont);
		style.setWrapText(true);
		styles.put("header", style);

		Font linkFont = wb.createFont();
		linkFont.setFontHeightInPoints((short) 10);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		linkFont.setUnderline(Font.U_SINGLE);
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(linkFont);
		styles.put("hyperLink", style);

		style = wb.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_THICK);
		style.setTopBorderColor(IndexedColors.DARK_BLUE.getIndex());
		styles.put("tab_endline", style);

		return styles;
	}
}
