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

import java.io.Serializable;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.Getter;

/**
 * Contains the data related to heading validation (duplicated headings, invalid headings, empty headings).
 *
 * @author plukasew, bjones86
 */
public class HeadingValidationReport implements Serializable
{
    @Getter
    private final SortedSet<String> duplicateHeadings;

    @Getter
    private final SortedSet<String> invalidHeadings;

    @Getter
    private final SortedSet<String> orphanedCommentHeadings;

    @Getter
    private int blankHeaderTitleCount;

    public HeadingValidationReport()
    {
        duplicateHeadings = new ConcurrentSkipListSet<>();
        invalidHeadings = new ConcurrentSkipListSet<>();
        orphanedCommentHeadings = new ConcurrentSkipListSet<>();
        blankHeaderTitleCount = 0;
    }

    public void addDuplicateHeading(String heading)
    {
        duplicateHeadings.add(heading);
    }

    public void addOrphanedCommentHeading(String heading)
    {
        orphanedCommentHeadings.add(heading);
    }

    public void addInvalidHeading(String heading)
    {
        invalidHeadings.add(heading);
    }

    public void incrementBlankHeaderTitleCount()
    {
        ++blankHeaderTitleCount;
    }
}
