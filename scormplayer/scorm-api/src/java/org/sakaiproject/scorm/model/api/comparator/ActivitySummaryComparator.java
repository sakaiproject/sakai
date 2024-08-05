/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.model.api.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.sakaiproject.scorm.model.api.ActivitySummary;

/**
 * Custom comparator for ActivitySummary objects; used for sorting tables.
 * @author bjones86
 */
public class ActivitySummaryComparator implements Comparator<ActivitySummary>, Serializable
{
    /**
     * The available comparison types for LearnerExperience objects
     */
    public static enum CompType
    {
        Title, Score, CompletionStatus, SuccessStatus
    }

    public static final CompType DEFAULT_COMP = CompType.Title;
    private CompType compType = DEFAULT_COMP;

    /**
     * Sets the comparison type the comparator will use. This determines which field of LearnerExperience is used for comparisons.
     * @param value the comparison type
     */
    public void setCompType( CompType value )
    {
        if( value != null )
        {
            compType = value;
        }
    }

    @Override
    public int compare( ActivitySummary as1, ActivitySummary as2 )
    {
        switch( compType )
        {
            case Title:
            {
                return as1.getTitle().compareTo( as2.getTitle() );
            }
            case Score:
            {
                return Double.compare( as1.getScaled(), as2.getScaled() );
            }
            case CompletionStatus:
            {
                return as1.getCompletionStatus().compareTo( as2.getCompletionStatus() );
            }
            case SuccessStatus:
            {
                return as1.getSuccessStatus().compareTo( as2.getSuccessStatus() );
            }
        }

        return 0;
    }
}
