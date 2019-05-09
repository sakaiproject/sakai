/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.time.Instant;

/**
 * Data for a comment in lessons
 * @author bjones86
 * @author plukasew
 */
public class CommentData implements LessonsData
{
    // Member variables
    public final String    author;
    public final String    comment;
    public final PageData  parent;
    public final Instant   timePosted;

    /**
     * Constructor
     * @param author the author of the comment
     * @param comment text of the comment
     * @param parent the page the comment was posted on
     * @param timePosted the timestamp when the comment was made
     */
    public CommentData( String author, String comment, PageData parent, Instant timePosted )
    {
        this.author         = author;
        this.comment        = comment;
        this.parent         = parent;
        this.timePosted     = timePosted;
    }
}
