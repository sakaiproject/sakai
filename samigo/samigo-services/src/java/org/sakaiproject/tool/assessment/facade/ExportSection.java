package org.sakaiproject.tool.assessment.facade;

/**
 * Identifies a section of a spreadsheet export. The enum covers two related roles, and not every
 * constant is valid in both:
 * <ul>
 *   <li>as the keys of the map returned by {@link AssessmentGradingFacadeQueriesAPI}'s
 *       {@code getExportResponsesData}, where only {@link #HEADER} and {@link #ROWS} ever appear.
 *       {@link #SHEET_BREAK} is never a key there.</li>
 *   <li>as the kind tag on a row handed to the workbook writer in the tool layer, where
 *       {@link #SHEET_BREAK} additionally marks the start of a new sheet.</li>
 * </ul>
 */
public enum ExportSection {
    SHEET_BREAK,
    HEADER,
    ROWS
}