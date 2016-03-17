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
