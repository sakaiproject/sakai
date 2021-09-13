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

package org.sakaiproject.jsf2.spreadsheet;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * CSV writer that uses opencsv
 * Calling class may override the separator (',' is the default).
 * Calling class may also override the representation of null values.  Default is to write out as "null".
 * For example, with an input list of <code>Collections.singletonList(Arrays.asList("asdf", "qwerty", "foobar", null))</code>
 * using the default of {@code NULL_AS.NULL} will result in a csv like ths following:
 * <pre>
 *     asdf,qwerty,foobar,null
 * </pre>
 * And, using that same list, but writing the nulls out as empty ({@code NULL_AS.EMPTY}), the result will be this:
 * <pre>
 *     asdf,qwerty,foobar,
 * </pre>
 * <p>
 * NOTE: CSV export capabilities are extremely limited! UTF-16 text (such as
 * Chinese) is not supported correctly, for example. Use Excel-formatted output if at all
 * possible.
 * </p>
 */
@Slf4j
public class SpreadsheetDataFileWriterOpenCsv implements SpreadsheetDataFileWriter {

	public enum NULL_AS {
		EMPTY,
		NULL
	}

	private NULL_AS nullValueRepresentation = NULL_AS.NULL;
	private char separatorChar = ',';

	/**
	 * Default constructor.  Writes nulls as 'null' and uses ',' as the separator
	 */
	public SpreadsheetDataFileWriterOpenCsv() {
	}

	/**
	 * Override the defaults for how null values are represented, as well as the separator character
	 * @param nullValueRepresentation
	 * @param separatorChar
	 */
	public SpreadsheetDataFileWriterOpenCsv(NULL_AS nullValueRepresentation, char separatorChar) {
		this.nullValueRepresentation = nullValueRepresentation;
		this.separatorChar = separatorChar;
	}

	public void writeDataToResponse(List<List<Object>> spreadsheetData, String fileName, HttpServletResponse response) {
		response.setContentType("text/comma-separated-values");
		SpreadsheetUtil.setEscapedAttachmentHeader(response, fileName + ".csv");

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			writeData(spreadsheetData, out);
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

	/**
	 * Write the data to the output stream
	 * @param data
	 * @param out
	 * @throws IOException
	 */
	private void writeData(List<List<Object>> data, OutputStream out) throws IOException {
		BufferedWriter buff = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		ICSVWriter writer = new CSVWriterBuilder(buff).withSeparator(separatorChar).build();

		for (List<Object> row : data) {
			writer.writeNext(convertList(row), false);
		}
		writer.close();
	}

	/**
	 * Convert a list of Objects to a string array, as that's what opencsv expects
	 * @param list Row of data to be written to csv
	 * @return String[] of data, with nulls optionally converted
	 */
	private String[] convertList(List<Object> list) {
		if (NULL_AS.EMPTY == nullValueRepresentation) {
			list.replaceAll(item -> Objects.isNull(item) ? "" : item);
		}
		return list.stream()
				.map(String::valueOf)
				.toArray(String[]::new);
	}

}
