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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.sakaiproject.util.Validator;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Abstracts away writing to a CSV or Excel file.
 */
public abstract class SpreadsheetExporter {

    enum Type {CSV, EXCEL};

    /**
     * Factory method for getting a spreadsheet exporter.
     * @param type The type of spreadsheet.
     * @param title The title of the spreadsheet.
     * @param gradeType The grade type being exported.
     * @return A new SpreadsheetExporter that can be used.
     */
    public static SpreadsheetExporter getInstance(Type type, String title, String gradeType, String csvSep) {
        if (Type.CSV.equals(type)) {
            return new CsvExporter(title, gradeType, csvSep);
        } else if (Type.EXCEL.equals(type)) {
            return new ExcelExporter(title, gradeType);
        } else {
            throw new IllegalArgumentException("Unsupported type: "+ type);
        }
    }

    /**
     * Add a header row to the spreadsheet.
     * @param values The values of the cells.
     * @return The current spreadsheet exporter to allow chaining of method calls.
     */
    public abstract SpreadsheetExporter addHeader(String... values);

    /**
     * Add a normal row to the spreadsheet.
     * @param values The values of the cells.
     * @return The current spreadsheet exporter to allow chaining of method calls.
     */
    public abstract SpreadsheetExporter addRow(String... values);

    /**
     * Writes the spreadsheet contents to a stream. We don't close the stream after writing.
     * @param outputStream The output stream to write the content to.
     * @throws IOException If there is a problem writing the stream.
     */
    public abstract void write(OutputStream outputStream) throws IOException;

    /**
     * The standard file extension for this type of spreadsheet.
     * @return A file extension (eg. csv).
     */
    public abstract String getFileExtension();

}

class CsvExporter extends SpreadsheetExporter {

    private final ByteArrayOutputStream gradesBAOS;
    private final CSVWriter gradesBuffer;

    CsvExporter(String title, String gradeType, String csvSep) {
        gradesBAOS = new ByteArrayOutputStream();
        gradesBuffer = new CSVWriter(new OutputStreamWriter(gradesBAOS), csvSep.charAt(0));
        addRow(title, gradeType);
        addRow("");
    }


    @Override
    public SpreadsheetExporter addHeader(String... values) {
        gradesBuffer.writeNext(values);
        return this;
    }

    @Override
    public SpreadsheetExporter addRow(String... values) {
        gradesBuffer.writeNext(values);
        return this;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        gradesBuffer.close();
        outputStream.write(gradesBAOS.toByteArray());
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}

class ExcelExporter extends SpreadsheetExporter {

    private final Workbook gradesWorkbook;
    private final Sheet dataSheet;
    private int rowCount = 0;

    ExcelExporter(String title, String gradeType) {
        gradesWorkbook = new HSSFWorkbook();
        dataSheet = gradesWorkbook.createSheet(Validator.escapeZipEntry(title));
    }

    private CellStyle createHeaderStyle(){
        //TO-DO read style information from sakai.properties
        Font font = gradesWorkbook.createFont();
        font.setFontName(HSSFFont.FONT_ARIAL);
        font.setColor(IndexedColors.PLUM.getIndex());
        font.setBold(true);
        CellStyle cellStyle = gradesWorkbook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    @Override
    public SpreadsheetExporter addHeader(String... values) {
        CellStyle cellStyle = createHeaderStyle();
        Row headerRow = dataSheet.createRow(rowCount++);
        for (int i = 0; i < values.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new HSSFRichTextString(values[i]));
        }
        return this;
    }

    @Override
    public SpreadsheetExporter addRow(String... values) {
        Row dataRow = dataSheet.createRow(rowCount++);
        for (int i = 0; i < values.length; i++) {
            Cell cell = dataRow.createCell(i);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(values[i]);
        }
        return this;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        gradesWorkbook.write(outputStream);
    }

    @Override
    public String getFileExtension() {
        return "xls";
    }
}

