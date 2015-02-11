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
        SpreadsheetExporter exporter = SpreadsheetExporter.getInstance(SpreadsheetExporter.Type.CSV, "title", "type");
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
        SpreadsheetExporter exporter = SpreadsheetExporter.getInstance(SpreadsheetExporter.Type.EXCEL, "title", "type");
        exporter.addHeader("header");
        exporter.addRow("row1", "row1");
        exporter.addRow("row2", "row2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        exporter.write(output);
        assertNotEquals(0, output.toByteArray().length);
        // Hard to do any more easy assertions.
    }

}
