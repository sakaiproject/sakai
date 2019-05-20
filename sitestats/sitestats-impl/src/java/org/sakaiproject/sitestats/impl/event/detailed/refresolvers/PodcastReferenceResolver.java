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

import java.time.Instant;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.podcasts.PodcastData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.PodcastRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves Podcast references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class PodcastReferenceResolver
{
    public static final String TOOL_ID = "sakai.podcasts";

    /**
     * Resolves the given event reference into meaningful details
     * @param eventRef the event reference
     * @param tips tips for parsing out the components of the reference
     * @param podServ the podcast service
     * @return a PodcastData object, or ResolvedEventData.ERROR/PERM_ERROR
     */
    public static ResolvedEventData resolveReference( String eventRef, List<EventParserTip> tips, PodcastService podServ )
    {
        if( StringUtils.isBlank( eventRef ) || podServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        GenericRefParser.GenericEventRef ref = PodcastRefParser.parse( eventRef, tips );

        if( StringUtils.isNotBlank( ref.contextId ) )
        {
            try
            {
                List<ContentResource> podcasts = podServ.getPodcasts( ref.contextId );
                for( ContentResource resource : podcasts )
                {
                    if( resource.getId().equals( ref.entityId ) )
                    {
                        ContentCollection parent = resource.getContainingCollection();
                        String podcastTitle = RefResolverUtils.getResourceName( resource );
                        Instant publishDateTime = getPodcastPublishDateTime( resource, podServ );

                        return new PodcastData(podcastTitle, publishDateTime, parent.getUrl());
                    }
                }
            }
            catch( PermissionException ex )
            {
                log.warn( "Permission exception retrieving podcasts for site: " + ref.contextId, ex );
                return ResolvedEventData.PERM_ERROR;
            }
            catch( InUseException | IdInvalidException | InconsistentException | IdUsedException ex )
            {
                log.warn( "Could not retrieve podcasts for site: " + ref.contextId, ex );
            }
        }

        log.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.ERROR;
    }

    /**
     * Utility method to retrieve the 'published' date of a given Podcast.
     * @param podcastResource The podcast resource
     * @param podServ The Podcast service object
     * @return A string representation of the podcast's publish date/time, matching the format from the Podcast tool
     */
    private static Instant getPodcastPublishDateTime( ContentResource podcastResource, PodcastService podServ )
    {
        if( podcastResource == null )
        {
            return Instant.EPOCH;
        }

        Date releaseDateTime;
        if( podcastResource.getReleaseDate() == null )
        {
            try
            {
                ResourceProperties podcastProperties = podcastResource.getProperties();
                releaseDateTime = podServ.getGMTdate( podcastProperties.getTimeProperty( PodcastService.DISPLAY_DATE ).getTime() );
            }
            catch( EntityPropertyNotDefinedException | EntityPropertyTypeException ex )
            {
                log.warn( "Unable to retrieve release date for resource with ID = " + podcastResource.getId(), ex );
                return Instant.EPOCH;
            }
        }
        else
        {
            releaseDateTime = new Date( podcastResource.getReleaseDate().getTime() );
        }

        return releaseDateTime.toInstant();
    }
}
