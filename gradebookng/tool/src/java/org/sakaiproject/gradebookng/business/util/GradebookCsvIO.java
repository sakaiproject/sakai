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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Opens Gradebook CSV readers and writers with the encoding and delimiter conventions used by Excel.
 */
public final class GradebookCsvIO {

	private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
	private static final char CSV_SEMICOLON_SEPARATOR = ';';
	private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

	private GradebookCsvIO() {
	}

	/**
	 * Opens a UTF-8 CSV writer that emits a BOM for spreadsheet compatibility.
	 */
	public static CSVWriter openWriter(final File file, final String decimalSeparator) throws IOException {
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
		try {
			outputStreamWriter.write('\uFEFF');
			return newCsvWriter(outputStreamWriter, decimalSeparator);
		} catch (final IOException | RuntimeException e) {
			try {
				outputStreamWriter.close();
			} catch (final IOException closeException) {
				e.addSuppressed(closeException);
			}
			throw e;
		}
	}

	/**
	 * Opens a CSV reader using a UTF-8 BOM when present and Windows-1252 for legacy exports otherwise.
	 */
	public static CSVReader openReader(final InputStream inputStream, final String decimalSeparator) throws IOException {
		final PushbackInputStream bomInputStream = new PushbackInputStream(inputStream, UTF_8_BOM.length);
		final byte[] prefix = bomInputStream.readNBytes(UTF_8_BOM.length);
		final boolean hasUtf8Bom = Arrays.equals(prefix, UTF_8_BOM);
		if (!hasUtf8Bom) {
			bomInputStream.unread(prefix);
		}
		final Charset charset = hasUtf8Bom ? StandardCharsets.UTF_8 : WINDOWS_1252;
		final InputStreamReader inputStreamReader = new InputStreamReader(bomInputStream, charset.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.onUnmappableCharacter(CodingErrorAction.REPORT));
		final CSVParser parser = new CSVParserBuilder()
				.withSeparator(separatorFor(decimalSeparator))
				.build();
		return new CSVReaderBuilder(inputStreamReader)
				.withCSVParser(parser)
				.build();
	}

	/**
	 * Excel uses semicolon field separators when comma is the locale decimal separator, so numeric
	 * grades such as {@code 7,5} are not split across columns.
	 */
	private static char separatorFor(final String decimalSeparator) {
		return ",".equals(decimalSeparator) ? CSV_SEMICOLON_SEPARATOR : CSVWriter.DEFAULT_SEPARATOR;
	}

	private static CSVWriter newCsvWriter(final OutputStreamWriter outputStreamWriter, final String decimalSeparator) {
		return new CSVWriter(outputStreamWriter, separatorFor(decimalSeparator), CSVWriter.DEFAULT_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.RFC4180_LINE_END);
	}
}
