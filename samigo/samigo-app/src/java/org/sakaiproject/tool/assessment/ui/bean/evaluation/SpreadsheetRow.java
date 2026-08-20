/*
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
package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.util.List;

import org.sakaiproject.tool.assessment.facade.CellValue;
import org.sakaiproject.tool.assessment.facade.ExportSection;

/**
 * A single row destined for the exported workbook, tagged with the kind of row it is so
 * {@link ExportResponsesBean#getAsWorkbook} can render it without duck-typing on marker
 * strings or cell content.
 */
public record SpreadsheetRow(ExportSection section, List<CellValue<?>> cells) {

    public static SpreadsheetRow sheetBreak(String sheetName) {
        return new SpreadsheetRow(ExportSection.SHEET_BREAK, List.of(CellValue.STRING(sheetName)));
    }

    public static SpreadsheetRow header(List<CellValue<?>> cells) {
        return new SpreadsheetRow(ExportSection.HEADER, cells);
    }

    public static SpreadsheetRow data(List<CellValue<?>> cells) {
        return new SpreadsheetRow(ExportSection.ROWS, cells);
    }
}
