/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.assignment.impl;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests of spreadsheet exporter.
 */
public class SpreadsheetExporterTest {

    @Test
    public void testSimpleCSV() throws IOException {
        SpreadsheetExporter exporter = SpreadsheetExporter.getInstance(SpreadsheetExporter.Type.CSV, "title", "type", ",");
        exporter.addHeader("header");
        exporter.addRow("row1", "row1");
        exporter.addRow("row2", "row2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exporter.write(output);
        assertNotEquals(0, output.toByteArray().length);
        // Title, 2 for header, 2 rows.
        assertEquals(5, StringUtils.countMatches(output.toString(), "\n"));
    }

    @Test
    public void testSimpleExcel() throws IOException {
        SpreadsheetExporter exporter = SpreadsheetExporter.getInstance(SpreadsheetExporter.Type.EXCEL, "title", "type", ",");
        exporter.addHeader("header");
        exporter.addRow("row1", "row1");
        exporter.addRow("row2", "row2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exporter.write(output);
        assertNotEquals(0, output.toByteArray().length);
        // Hard to do any more easy assertions.
    }

}
