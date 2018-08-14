package org.sakaiproject.sitestats.api.event.detailed.lessons;

/**
 * Catch-all generic item data object, should be eventually phased out once all item types are explicitly handled
 * @author bjones86
 * @author plukasew
 */
public class GenericItemData implements LessonsData
{
    // Member variables
    public final String title;
    public final PageData parentPage;

    public static final DeletedItem DELETED_ITEM = new GenericItemData.DeletedItem();

    /**
     * Constructor
     * @param title the title of the item
     * @param parentPage the page the item is on
     */
    public GenericItemData( String title, PageData parentPage )
    {
        this.title          = title;
        this.parentPage     = parentPage;
    }

	// a deleted item (no further info is available)
	public static final class DeletedItem implements LessonsData { }
}
