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

import org.sakaiproject.scorm.model.api.Interaction;

/**
 * Custom comparator for Interaction objects; used for sorting data tables;
 * @author bjones86
 */
public class InteractionComparator implements Comparator<Interaction>, Serializable
{
    /**
     * The available comparison types for LearnerExperience objects
     */
    public static enum CompType
    {
        InteractionID, Description, Type, Result
    }

    public static final CompType DEFAULT_COMP = CompType.InteractionID;
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
    public int compare( Interaction int1, Interaction int2 )
    {
        switch( compType )
        {
            case InteractionID:
            {
                return int1.getInteractionId().compareTo( int2.getInteractionId() );
            }
            case Description:
            {
                return int1.getDescription().compareTo( int2.getDescription() );
            }
            case Type:
            {
                return int1.getType().compareTo( int2.getType() );
            }
            case Result:
            {
                return int1.getResult().compareTo( int2.getResult() );
            }
        }

        return 0;
    }
}
