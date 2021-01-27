/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.CommentsSectionItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.ContentLinkItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.EmbeddedItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.GenericItemData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.PageData;
import org.sakaiproject.sitestats.api.event.detailed.lessons.TextItemData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import org.sakaiproject.util.api.FormattedText;


/**
 * Resolves LessonBuilder references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class LessonsReferenceResolver
{
    public static final String TOOL_ID = "sakai.lessonbuildertool";

    public enum Type { PAGE, ITEM, COMMENT; }

    /**
     * Resolves a Lessons event reference into meaningful details about the event
     * @param eventRef the event ref string to be processed
     * @param tips the tips used for parsing the event refs
     * @param lsnServ the LessonBuilder service to use for retrieving addition information
     * @return one of the LessonsData variants, or ResolvedEventData.ERROR
     */
    public static ResolvedEventData resolveReference( String eventRef, List<EventParserTip> tips, SimplePageToolDao lsnServ )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || lsnServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        Optional<ParsedLessonsRef> parsedRefOpt = parse( eventRef, tips );
        if( !parsedRefOpt.isPresent() )
        {
            return ResolvedEventData.ERROR;
        }
        ParsedLessonsRef parsedRef = parsedRefOpt.get();

        switch( parsedRef.type )
        {
            case PAGE:
                SimplePage page = lsnServ.getPage( parsedRef.id );
                return page != null ? collectPageEventDetails( page, lsnServ ) : PageData.DELETED_PAGE;
            case ITEM:
                SimplePageItem item = lsnServ.findItem( parsedRef.id );
                return item != null ? collectItemEventDetails( item, lsnServ ) : GenericItemData.DELETED_ITEM;
            case COMMENT:
                SimplePageComment comment = lsnServ.findCommentById( parsedRef.id );
                return comment != null ? collectCommentEventDetails( comment, lsnServ ) : ResolvedEventData.ERROR;
            default:
                log.error( "Invalid ParsedLessonsRef.Type: " + parsedRef.type + " for ref " + eventRef);
                return ResolvedEventData.ERROR;
        }
    }

    private static Optional<ParsedLessonsRef> parse( String eventRef, List<EventParserTip> tips )
    {
        GenericRefParser.GenericEventRef ref = GenericRefParser.parse( eventRef, tips );
        try
        {
            long id = Long.valueOf( ref.entityId );
            if( id < 1 ) // invalid id
            {
                return Optional.empty();
            }
            Type type = Type.valueOf( ref.subContextId.toUpperCase( Locale.ROOT ) );
            return Optional.of( new ParsedLessonsRef( id, type ) );
        }
        catch (IllegalArgumentException e)
        {
            // this is thrown if any of the valueOf() calls above fail, in which case the ref is malformed and cannot be resolved
            log.warn( "Unable to parse, ref is malformed: " + eventRef, e );
            return Optional.empty();
        }
    }

    /**
     * Aggregate all appropriate details of the page.
     * @param page the page object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static PageData collectPageEventDetails( SimplePage page, SimplePageToolDao lsnServ )
    {
        // Add the necessary details for the page
        return new PageData( page.getTitle(), getPageHierarchy( page, lsnServ ) );
    }

    /**
     * Aggregate all appropriate details of the item.
     * @param item the item object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static ResolvedEventData collectItemEventDetails( SimplePageItem item, SimplePageToolDao lsnServ )
    {
        // Attempt to get the parent page and hierarchy
        SimplePage parentPage = lsnServ.getPage( item.getPageId() );
        List<String> hierarchy = getPageHierarchy( parentPage, lsnServ );

        // Build the return object conditionally based on the 'type' of item
        switch( item.getType() )
        {
            case SimplePageItem.TEXT:
                return new TextItemData( ComponentManager.get(FormattedText.class).stripHtmlFromText( item.getHtml(), false, true ),  new PageData( parentPage.getTitle(), hierarchy ) );

            case SimplePageItem.MULTIMEDIA:
                String desc = StringUtils.trimToEmpty( item.getDescription() );
                return new EmbeddedItemData( desc, new PageData( parentPage.getTitle(), hierarchy ) );

            // PROGRAMMER.NOTES indicates that for whatever reason Lessons creates 'content' items as of the resource type,
            // despite the fact that they are in reality URLs. It goes on to say that 'this may be a mistake', however nothing has been done to address this.
            // So for the time being, we need to create 'URL' data objects when the item object is of the type 'resource'...
            case SimplePageItem.RESOURCE:
                return new ContentLinkItemData( item.getName(), new PageData( parentPage.getTitle(), hierarchy ) );
            case SimplePageItem.PAGE:
                return new PageData( item.getName(), hierarchy );  // technically an item, treat as a page
            case SimplePageItem.COMMENTS:
                if( item.getPageId() == -1 )
                {
                    // "forced" comments section on a student page, we can't determine which page
                    return CommentsSectionItemData.FORCED;
                }

                return new CommentsSectionItemData( new PageData( parentPage.getTitle(), hierarchy ) );
            /* Unimplemeted cases:
            case SimplePageItem.ASSESSMENT:
            case SimplePageItem.ASSIGNMENT:
            case SimplePageItem.BLTI:
            case SimplePageItem.FORUM:
            case SimplePageItem.PEEREVAL:
            case SimplePageItem.QUESTION:
            case SimplePageItem.URL:
            case SimplePageItem.STUDENT_CONTENT:
            */
            // Default behaviour for all above unimplemented and not defined cases:
            default:
                return new GenericItemData( item.getName(), new PageData( parentPage.getTitle(), hierarchy ) );
        }
    }

    /**
     * Aggregate all appropriate details of the comment.
     * @param comment the comment object to retrieve data from
     * @param lsnServ the LessonBuilder service to use for additional data retrieval
     * @param userDirServ the UserDirectoryService object used to perform user lookups
     * @return a list of ResolvedRef objects containing details about the event
     */
    private static CommentData collectCommentEventDetails( SimplePageComment comment, SimplePageToolDao lsnServ )
    {
        // Get the top parent
        SimplePage parentPage = lsnServ.getPage( comment.getPageId() );

        return new CommentData( comment.getAuthor(), ComponentManager.get(FormattedText.class).stripHtmlFromText( comment.getComment(), false, true ),
                new PageData( parentPage.getTitle(), getPageHierarchy( parentPage, lsnServ ) ), comment.getTimePosted().toInstant() );
    }

    private static List<String> getPageHierarchy( SimplePage page, SimplePageToolDao lsnServ )
    {
        List<HierarchyPage> hierarchy = new ArrayList<>();
        getPageHierarchyReverse( hierarchy, page, lsnServ );
        List<String> hier = hierarchy.stream().map( p -> p.title ).collect( Collectors.toList() );
        Collections.reverse( hier );
        return hier;
    }

    /**
     * Accumulates a list representing the hierarchy of pages starting from the given page and traversing upwards.
     * @param hierarchy the accumulated list of pages
     * @param page the page we want to traverse the hierarchy from
     * @param lsnServ the LessonBuilder service to use for accessing the hierarchy information
     */
    private static void getPageHierarchyReverse( List<HierarchyPage> hierarchy, SimplePage page, SimplePageToolDao lsnServ)
    {
        if( page != null )
        {
            if( hierarchy.stream().noneMatch( parent -> parent.id == page.getPageId() ) ) // guard against infinite recursion due to cycles in hierarchy
            {
                hierarchy.add( new HierarchyPage( page.getPageId(), page.getTitle() ) );
                Long parentPageID = page.getParent();

                if( parentPageID != null )
                {
                    SimplePage parent = lsnServ.getPage( parentPageID );
                    getPageHierarchyReverse( hierarchy, parent, lsnServ );
                }
            }
        }
        else
        {
            hierarchy.add( HierarchyPage.DELETED_PAGE );
        }
	}

    private static class ParsedLessonsRef
    {
        public final long id;
        public final Type type;

        public ParsedLessonsRef( long id, Type type )
        {
            this.id = id;
            this.type = type;
        }
    }

    private static class HierarchyPage
    {
        public final long id;
        public final String title;
        public static final HierarchyPage DELETED_PAGE = new HierarchyPage( -999L, PageData.DELETED_HIERARCHY_PAGE );

        public HierarchyPage( long id, String title )
        {
            this.id = id;
            this.title = title;
        }
    }
}
