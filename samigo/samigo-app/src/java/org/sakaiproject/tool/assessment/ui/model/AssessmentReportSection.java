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

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssessmentReportSection {


    private Optional<String> title;
    private List<String> tableHeader;
    private List<List<String>> tableData;
    @Builder.Default
    private TableLayout tableLayout = TableLayout.HORIZONTAL;


    public List<List<String>> getTable() {
        List<List<String>> table = new ArrayList<>();

        switch (tableLayout) {
            default:
            case HORIZONTAL:
                Optional.ofNullable(tableHeader).ifPresent(table::add);
                Optional.ofNullable(tableData).ifPresent(table::addAll);
                break;
            case VERTICAL:
                if (tableData != null && tableHeader != null) {
                    int rowCount = tableHeader.size();

                    for (int i = 0; i < rowCount; i++) {
                        List<String> row = new ArrayList<>();

                        // Add one header cell
                        row.add(tableHeader.get(i));

                        // Add data for row -> i-item from each data row
                        for (int j = 0; j < tableData.size(); j++) {
                            List<String> dataRow = tableData.get(j);
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
