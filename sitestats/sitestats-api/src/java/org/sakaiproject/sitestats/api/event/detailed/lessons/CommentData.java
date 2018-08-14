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
