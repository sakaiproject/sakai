/**
 * Copyright (c) 2007-2014 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.downloadEvents;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
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
		
		//Title
		Font titleFont = wb.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBold(true);
		titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(titleFont);
		styles.put("title", style);

		Font commentTitleFont = wb.createFont();
		commentTitleFont.setFontHeightInPoints((short) 12);
		commentTitleFont.setBold(true);
		commentTitleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(commentTitleFont);
		styles.put("commentTitle", style);

		Font itemFont = wb.createFont();
		itemFont.setFontHeightInPoints((short) 10);
		itemFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setFont(itemFont);
		styles.put("item_left", style);

		style = wb.createCellStyle();;
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("item_left_wrap", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("item_left_wrap_top", style);

		itemFont.setFontHeightInPoints((short) 10);
		itemFont.setFontName("Trebuchet MS");
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(itemFont);
		styles.put("tabItem_fields", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setFont(itemFont);
		styles.put("item_right", style);

		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(itemFont);
		style.setWrapText(true);
		styles.put("attendee_layout", style);

		Font itemBoldFont = wb.createFont();
		itemBoldFont.setFontHeightInPoints((short) 10);
		itemBoldFont.setFontName("Trebuchet MS");
		itemBoldFont.setColor(IndexedColors.DARK_BLUE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		itemBoldFont.setBold(true);
		style.setFont(itemBoldFont);
		styles.put("item_leftBold", style);

		Font tableFont = wb.createFont();
		tableFont.setFontHeightInPoints((short) 12);
		tableFont.setColor(IndexedColors.WHITE.getIndex());
		tableFont.setBold(true);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(tableFont);
		styles.put("tabColNames", style);

		tableFont.setFontHeightInPoints((short) 10);
		tableFont.setColor(IndexedColors.WHITE.getIndex());
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setFont(tableFont);
		style.setWrapText(true);
		styles.put("header", style);

		Font linkFont = wb.createFont();
		linkFont.setFontHeightInPoints((short) 10);
		linkFont.setColor(IndexedColors.BLUE.getIndex());
		linkFont.setUnderline(Font.U_SINGLE);
		style = wb.createCellStyle();
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setFont(linkFont);
		styles.put("hyperLink", style);

		style = wb.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.DARK_BLUE.getIndex());
		styles.put("tab_endline", style);

		return styles;
	}
}
