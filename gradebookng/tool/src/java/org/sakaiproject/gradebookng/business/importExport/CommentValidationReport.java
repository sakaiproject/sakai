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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Getter;

/**
 * Contains the data relevant to comment validation (comments that are too long).
 *
 * @author bjones86
 */
public class CommentValidationReport
{
    @Getter
    private final SortedMap<String, List<String>> invalidComments;

    public CommentValidationReport()
    {
        invalidComments = new ConcurrentSkipListMap<>();
    }

    public void addInvalidComment( String columnTitle, String studentIdentifier )
    {
        List<String> columnStudentsWithInvalidComments = invalidComments.get( columnTitle );
        if( columnStudentsWithInvalidComments == null )
        {
            columnStudentsWithInvalidComments = new ArrayList<>();
            columnStudentsWithInvalidComments.add( studentIdentifier );
            invalidComments.put( columnTitle, columnStudentsWithInvalidComments );
        }
        else
        {
            columnStudentsWithInvalidComments.add( studentIdentifier );
        }
    }
}
