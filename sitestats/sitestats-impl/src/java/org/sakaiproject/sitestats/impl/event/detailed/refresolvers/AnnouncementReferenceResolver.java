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

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.announcements.AnnouncementData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;

/**
 * Resolves Announcement references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class AnnouncementReferenceResolver
{
    public static final String TOOL_ID = "sakai.announcements";

    /**
     * Resolves the given event reference into meaningful details
     * @param eventRef the event reference
     * @param tips tips for parsing out the components of the reference
     * @param anncServ the announcement service
     * @return an AnnouncementData object, or ResolvedEventData.ERROR/PERM_ERROR
     */
    public static ResolvedEventData resolveReference( String eventRef, List<EventParserTip> tips, AnnouncementService anncServ )
    {
        if( StringUtils.isBlank( eventRef ) || anncServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        GenericRefParser.GenericEventRef ref = GenericRefParser.parse( eventRef, tips );
        String channelRef = "/announcement/channel/" + ref.contextId + "/" + ref.subContextId;
        try
        {
            AnnouncementChannel channel = anncServ.getAnnouncementChannel( channelRef );
            if( channel != null )
            {
                AnnouncementMessage message = channel.getAnnouncementMessage( ref.entityId );
                if( message != null )
                {
                    return new AnnouncementData( message.getAnnouncementHeader().getSubject() );
                }
            }
        }
        catch( IdUnusedException iue )
        {
            log.warn( "Unable to retrieve channel/message by ref/id.", iue );
        }
        catch( PermissionException pe )
        {
            log.warn( "Permission exception trying to retrieve channel/message.", pe );
            return ResolvedEventData.PERM_ERROR;
        }

        log.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.ERROR;
    }
}
