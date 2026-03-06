/**
 * Copyright (c) 2023 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReport;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportCell;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportSection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelExportUtil {


    public static String assessmentReportToXslx(AssessmentReport report) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook()) {
            CellStyle boldCellStyle = createBoldCellStyle(workbook);

            List<AssessmentReportSection> sections = report.getSections();
            List<AssessmentReportSection> titledSections = sections.stream()
                    .filter(section -> section.getTitle().isPresent())
                    .collect(Collectors.toList());

            if (titledSections.size() < sections.size()) {
                log.warn("Sections without titles are ignored");
            }

            for (int i = 0; i < titledSections.size(); i++) {
                AssessmentReportSection section = titledSections.get(i);
                List<List<AssessmentReportCell>> table = section.getCellTable();
                Sheet sheet = workbook.createSheet(section.getTitle().orElseThrow());

                // Create title row
                report.getTitle().ifPresent(title -> {
                    int rowIndex = 0;
                    log.debug("Adding row at {} (title)", rowIndex);
                    sheet.createRow(0).createCell(0).setCellValue(title);
                });

                // Create subject row
                report.getSubject().ifPresent(subject -> {
                    int rowIndex = nextRowIndex(sheet);
                    log.debug("Adding row at {} (subject)", rowIndex);
                    sheet.createRow(rowIndex).createCell(0).setCellValue(subject);
                });

                // Create empty row if there is a title or subject
                if (report.getTitle().isPresent() || report.getSubject().isPresent()) {
                    int rowIndex = nextRowIndex(sheet);
                    log.debug("Adding row at {} (empty)", rowIndex);
                    sheet.createRow(rowIndex);
                }

                // Add all the section data
                int rowOffset = nextRowIndex(sheet);
                for (int j = 0; j < table.size(); j++) {
                    int rowIndex = j + rowOffset;
                    log.debug("Adding row at {} (data)", rowIndex);
                    Row row = sheet.createRow(rowIndex);
                    List<AssessmentReportCell> rowData = table.get(j);

                    for (int k = 0; k < rowData.size(); k++) {
                        Cell cell = row.createCell(k);
                        AssessmentReportCell cellData = rowData.get(k);
                        cell.setCellValue(cellData.getValue());
                        if (cellData.isBold()) {
                            cell.setCellStyle(boldCellStyle);
                        }
                    }
                }
            }

            // This block could throw an IOException
            workbook.write(out);

            return out.toString(StandardCharsets.ISO_8859_1);
        }
    }

    private static int nextRowIndex(Sheet sheet) {
        return sheet.getLastRowNum() + 1;
    }

    private static CellStyle createBoldCellStyle(Workbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }
}
