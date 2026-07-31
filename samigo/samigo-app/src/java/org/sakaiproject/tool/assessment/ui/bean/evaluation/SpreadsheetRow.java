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
