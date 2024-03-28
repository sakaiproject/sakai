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

import org.apache.commons.lang.ObjectUtils;

import org.sakaiproject.scorm.model.api.LearnerExperience;

/**
 * Custom comparator for LearnerExperience objects; used for sorting data tables.
 * @author bjones86
 */
public class LearnerExperienceComparator implements Comparator<LearnerExperience>, Serializable
{
    /**
     * The available comparison types for LearnerExperience objects
     */
    public static enum CompType
    {
        Learner, AttemptDate, Status, NumberOfAttempts
    }

    public static final CompType DEFAULT_COMP = CompType.Learner;
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
    public int compare( LearnerExperience le1, LearnerExperience le2 )
    {
        // Perform different comparison depending on which comparison type has been selected
        switch( compType )
        {
            case Learner:
            {
                return le1.getLearnerName().compareTo( le2.getLearnerName() );
            }
            case AttemptDate:
            {
                return ObjectUtils.compare( le1.getLastAttemptDate(), le2.getLastAttemptDate() );
            }
            case Status:
            {
                return Integer.compare( le1.getStatus(), le2.getStatus() );
            }
            case NumberOfAttempts:
            {
                return Integer.compare( le1.getNumberOfAttempts(), le2.getNumberOfAttempts() );
            }
        }

        return 0;
    }
}
