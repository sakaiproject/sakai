/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.importExport;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedColumn.Type;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Used to validate grades in an imported file.
 *
 * TODO: if letter grades are enabled in the future, more work will be necessary here
 * to perform the validation against the letter grade mapping scale.
 *
 * @author plukasew, bjones86
 */
public class GradeValidator
{
    private GradeValidationReport report;
    private final GradebookNgBusinessService bus;

    public GradeValidator(GradebookNgBusinessService service)
    {
        bus = service;
    }

    /**
     * Validate the grades contained within the list of imported rows.
     *
     * @param rows the list of data parsed from the input file
     * @param columns the list of parsed columns, so we can access the column type
     * @return the {@link GradeValidationReport}
     */
    public GradeValidationReport validate(List<ImportedRow> rows, List<ImportedColumn> columns)
    {
        report = new GradeValidationReport();

        for (ImportedColumn column : columns)
        {
            Type columnType = column.getType();
            String columnTitle = column.getColumnTitle();
            if (columnType == Type.GB_ITEM_WITH_POINTS || columnType == Type.GB_ITEM_WITHOUT_POINTS)
            {
                for (ImportedRow row : rows)
                {
                    ImportedCell cell = row.getCellMap().get(columnTitle);
                    if (cell != null)
                    {
                        String studentIdentifier = row.getStudentEid();

                        // Validation is locale-aware, so use the raw score that the user input in their own locale
                        validateGrade( columnTitle, studentIdentifier, cell.getRawScore() );
                    }
                }
            }
        }

        return report;
    }

    /**
     * Validates the given grade for the user. Grades are expected to be numeric.
     *
     * @param userID
     * @param grade
     */
    private void validateGrade(String columnTitle, String userID, String grade)
    {
        // Empty grades are valid
        if (StringUtils.isBlank(grade))
        {
            return;
        }
        // TODO: when/if letter grades are introduce, determine if grade is numeric
        // or alphabetical here and call/write the appropriate business service method.

        if (!bus.isValidNumericGrade(grade))
        {
            report.addInvalidNumericGrade(columnTitle, userID, grade);
        }
    }
}
