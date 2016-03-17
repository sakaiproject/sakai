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
