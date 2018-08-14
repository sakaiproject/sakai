package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Immutable class to hold parameters for sorting in detailed events queries
 *
 * @author bjones86
 */
public final class SortingParams
{
    public final String sortProp;
    public final boolean asc;

    /**
     * Constructor requiring all parameters
     *
     * @param sortProp the property to sort on
     * @param asc sorting order, true = asc, false = desc
     */
    public SortingParams( String sortProp, boolean asc )
    {
        this.sortProp = sortProp;
        this.asc = asc;
    }
}