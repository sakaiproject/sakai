/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.util;

import com.opencsv.CSVWriter;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writes Gradebook CSV exports as UTF-8 with a byte order mark for spreadsheet compatibility.
 */
public final class GradebookCsvWriter implements Closeable {

	private static final String BOM = "\uFEFF";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';

	private final CSVWriter writer;

	public GradebookCsvWriter(final File file, final String decimalSeparator) throws IOException {
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		outputStreamWriter.write(BOM);
		final char csvSeparator = ".".equals(decimalSeparator) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR;
		this.writer = new CSVWriter(outputStreamWriter, csvSeparator, CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
	}

	public void writeRow(final List<String> row) {
		this.writer.writeNext(row.toArray(new String[] {}));
	}

	@Override
	public void close() throws IOException {
		this.writer.close();
	}
}
