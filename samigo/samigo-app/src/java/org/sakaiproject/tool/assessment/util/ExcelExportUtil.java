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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReport;
import org.sakaiproject.tool.assessment.ui.model.AssessmentReportSection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelExportUtil {


    public static String assessmentReportToXslx(AssessmentReport report) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Workbook workbook = new XSSFWorkbook();

        List<AssessmentReportSection> sections = report.getSections();
        List<Sheet> sheets = sections.stream()
                .map(AssessmentReportSection::getTitle)
                .flatMap(Optional::stream)
                .map(workbook::createSheet)
                .collect(Collectors.toList());

        if (sheets.size() < sections.size()) {
            log.warn("Sections without titles are ignored");
        }

        for (int i = 0; i < sheets.size(); i++) {
            AssessmentReportSection section = sections.get(i);
            List<List<String>> table = section.getTable();
            Sheet sheet = sheets.get(i);

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
                List<String> rowData = table.get(j);

                for (int k = 0; k < rowData.size(); k++) {
                    Cell cell = row.createCell(k);
                    cell.setCellValue(rowData.get(k));
                }
            }
        }

        // This block could throw an IOException
        workbook.write(out);
        workbook.close();

        return out.toString(StandardCharsets.ISO_8859_1);
    }

    private static int nextRowIndex(Sheet sheet) {
        return sheet.getLastRowNum() + 1;
    }
}
