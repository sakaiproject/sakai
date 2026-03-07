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

package org.sakaiproject.tool.assessment.ui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentReportSection {


    private Optional<String> title;
    private List<String> tableHeader;
    private List<List<String>> tableData;
    private List<AssessmentReportCell> tableHeaderCells;
    private List<List<AssessmentReportCell>> tableDataCells;
    @Builder.Default
    private TableLayout tableLayout = TableLayout.HORIZONTAL;


    public List<List<String>> getTable() {
        List<List<String>> table = new ArrayList<>();
        List<String> header = tableHeader;
        List<List<String>> data = tableData;
        if (header == null && tableHeaderCells != null) {
            header = tableHeaderCells.stream()
                    .map(AssessmentReportCell::getValue)
                    .collect(Collectors.toList());
        }
        if (data == null && tableDataCells != null) {
            data = tableDataCells.stream()
                    .map(row -> row.stream()
                            .map(AssessmentReportCell::getValue)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }

        switch (tableLayout) {
            default:
            case HORIZONTAL:
                Optional.ofNullable(header).ifPresent(table::add);
                Optional.ofNullable(data).ifPresent(table::addAll);
                break;
            case VERTICAL:
                if (data != null && header != null) {
                    int rowCount = header.size();

                    for (int i = 0; i < rowCount; i++) {
                        List<String> row = new ArrayList<>();

                        // Add one header cell
                        row.add(header.get(i));

                        // Add data for row -> i-item from each data row
                        for (int j = 0; j < data.size(); j++) {
                            List<String> dataRow = data.get(j);
                            if (i < dataRow.size()) {
                                row.add(dataRow.get(i));
                            }
                        }

                        table.add(row);
                    }
                }
                break;
            }
        return table;
    }

    public List<List<AssessmentReportCell>> getCellTable() {
        List<List<AssessmentReportCell>> table = new ArrayList<>();
        List<AssessmentReportCell> header = tableHeaderCells;
        List<List<AssessmentReportCell>> data = tableDataCells;
        if (header == null && tableHeader != null) {
            header = tableHeader.stream()
                    .map(AssessmentReportCell::text)
                    .collect(Collectors.toList());
        }
        if (data == null && tableData != null) {
            data = tableData.stream()
                    .map(row -> row.stream()
                            .map(AssessmentReportCell::text)
                            .collect(Collectors.toList()))
                    .collect(Collectors.toList());
        }

        switch (tableLayout) {
            default:
            case HORIZONTAL:
                Optional.ofNullable(header).ifPresent(table::add);
                Optional.ofNullable(data).ifPresent(table::addAll);
                break;
            case VERTICAL:
                if (data != null && header != null) {
                    int rowCount = header.size();

                    for (int i = 0; i < rowCount; i++) {
                        List<AssessmentReportCell> row = new ArrayList<>();

                        // Add one header cell
                        row.add(header.get(i));

                        // Add data for row -> i-item from each data row
                        for (int j = 0; j < data.size(); j++) {
                            List<AssessmentReportCell> dataRow = data.get(j);
                            if (i < dataRow.size()) {
                                row.add(dataRow.get(i));
                            }
                        }

                        table.add(row);
                    }
                }
                break;
            }
        return table;
    }

    @SuppressWarnings("unused")
    public static class AssessmentReportSectionBuilder {


        private Optional<String> title = Optional.empty();


        public AssessmentReportSectionBuilder title(String title) {
            this.title = Optional.ofNullable(title);
            return this;
        }
    }


    public enum TableLayout {
        HORIZONTAL,
        VERTICAL,
    }
}
