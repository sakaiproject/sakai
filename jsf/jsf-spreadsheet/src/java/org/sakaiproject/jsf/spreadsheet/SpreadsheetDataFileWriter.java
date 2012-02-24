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

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * A simple utility class for downloading generic spreadsheet-style data
 * in a variety of formats.
 */
public interface SpreadsheetDataFileWriter {
	/**
	 * Put a file describing the spreadsheet-style data into to the HTTP servlet response.
	 * 
	 * @param spreadsheetData a list of rows, beginning with a header row, each being a list
	 * of column objects 
	 * @param fileName
	 * @param response
	 */
	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response);
}
