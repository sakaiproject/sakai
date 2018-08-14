package org.sakaiproject.sitestats.api.event.detailed.lessons;

/**
 * Data for a text item
 * @author bjones86
 * @author plukasew
 */
public class TextItemData implements LessonsData
{
    // Member variables
    public final String html;
    public final PageData parentPage;

    /**
     * Constructor
     * @param html the html of the item
     * @param parentPage the page the item is on
     */
    public TextItemData( String html, PageData parentPage )
    {
        this.html           = html;
        this.parentPage     = parentPage;
    }
}
