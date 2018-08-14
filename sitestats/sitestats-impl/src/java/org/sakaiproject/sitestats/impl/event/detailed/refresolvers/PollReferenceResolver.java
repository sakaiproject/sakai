package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.polls.PollData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;

/**
 * Resolves Poll references into meaningful details.
 *
 * @author bjones86
 * @author plukasew
 */
@Slf4j
public class PollReferenceResolver
{
	public static final String TOOL_ID = "sakai.poll";

	/**
	 * Resolves the given event reference into meaningful details
	 * @param eventRef the event references
	 * @param tips tips for parsing out the components of the reference
	 * @param pollServ the polls service
	 * @return a PollData object, or ResolvedEventData.ERROR
	 */
	public static ResolvedEventData resolveReference( String eventRef, List<EventParserTip> tips, PollListManager pollServ )
	{
		// Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || pollServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

		String pollID = GenericRefParser.parse(eventRef, tips).entityId;
        try
        {
            Long id = Long.parseLong( pollID );
			Poll poll = pollServ.getPollById( id, false );
			if( poll != null )
			{
				return new PollData( poll.getText() );
			}
        }
        catch( NumberFormatException ex )
        {
            log.warn( "Cannot parse ID = " + pollID + "; ref = " + eventRef, ex );
        }

        // Failed to retrieve data for the given ref
        log.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.ERROR;
	}
}
