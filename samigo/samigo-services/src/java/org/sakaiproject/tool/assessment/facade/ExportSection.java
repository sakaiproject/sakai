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