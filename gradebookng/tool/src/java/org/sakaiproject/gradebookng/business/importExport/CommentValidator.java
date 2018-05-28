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

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.model.ImportedCell;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedColumn.Type;
import org.sakaiproject.gradebookng.business.model.ImportedRow;

/**
 * Used to validate comments in an imported file.
 *
 * @author bjones86
 */
public class CommentValidator
{
    private CommentValidationReport report;

    private static final String SAK_PROP_MAX_COMMENT_LENGTH = "gradebookng.maxCommentLength";
    private static final int SAK_PROP_MAX_COMMENT_LENGTH_DEFAULT = 20000;
    public static final int MAX_COMMENT_LENGTH = ServerConfigurationService.getInt(SAK_PROP_MAX_COMMENT_LENGTH, SAK_PROP_MAX_COMMENT_LENGTH_DEFAULT);

    /**
     * Validate the comments contained within the list of imported rows.
     *
     * @param rows the list of data parsed from the input file
     * @param columns the list of parsed columns, so we can access the column type and name
     * @return the {@link CommentValidationReport}
     */
    public CommentValidationReport validate( List<ImportedRow> rows, List<ImportedColumn> columns )
    {
        report = new CommentValidationReport();

        for( ImportedColumn column : columns )
        {
            Type columnType = column.getType();
            String columnTitle = column.getColumnTitle();
            if( columnType == Type.COMMENTS )
            {
                for( ImportedRow row : rows )
                {
                    ImportedCell cell = row.getCellMap().get( columnTitle );
                    if( cell != null )
                    {
                        String studentIdentifier = row.getStudentEid();
                        validateComment( columnTitle, studentIdentifier, cell.getComment() );
                    }
                }
            }
        }

        return report;
    }

    /**
     * Validates the given comment for the user/gradebook item.
     *
     * @param columnTitle
     * @param studentIdentifer
     * @param comment
     */
    private void validateComment( String columnTitle, String studentIdentifer, String comment )
    {
        // Empty comments are valid
        if( StringUtils.isBlank( comment ) )
        {
            return;
        }

        if( isCommentInvalid( comment ) )
        {
            report.addInvalidComment( columnTitle, studentIdentifer );
        }
    }

    /**
     * Test the given comment string for validity (character length).
     * Max length is defined by sakai.property 'gradebookng.maxCommentLength'; defaults to 20,000
     *
     * @param comment the comment string to test
     * @return true if the given comment is invalid; false otherwise
     */
    public static boolean isCommentInvalid( String comment )
    {
        return StringUtils.length(comment) > MAX_COMMENT_LENGTH;
    }
}
