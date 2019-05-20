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
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.news.FeedData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves News references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class NewsReferenceResolver
{
    public static final String TOOL_ID = "sakai.simple.rss";

    /**
     * Resolves the given event reference into meaningful details
     * @param eventRef the event reference
     * @param tips tips for parsing out the components of the reference
     * @param siteServ the site service
     * @return a FeedData object, or ResolvedEventData.ERROR
     */
    public static ResolvedEventData resolveReference( String eventRef, List<EventParserTip> tips, SiteService siteServ )
    {
        // Short circuit if the ref is null, empty, or the service(s) aren't initialized
        if( StringUtils.isBlank( eventRef ) || siteServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        GenericRefParser.GenericEventRef ref = GenericRefParser.parse( eventRef, tips );
        String siteID = ref.contextId;
        String toolID = ref.subContextId;

        // Attempt to get additional details
        Optional<Site> site = RefResolverUtils.getSiteByID( siteID, siteServ, log );
        if( site.isPresent() )
        {
            ToolConfiguration toolConfig = site.get().getTool( toolID );
            if( toolConfig != null )
            {
                String toolTitle = toolConfig.getTitle();
                String pageTitle = toolConfig.getContainingPage().getTitle();

                String title;
                if( toolTitle.equalsIgnoreCase( pageTitle ) )
                {
                    title = pageTitle;
                }
                else
                {
                    title = pageTitle + " - " + toolTitle;
                }

                return new FeedData(title);
            }
        }

        // Failed to retrieve data for the given ref
        log.warn( "Unable to retrieve data; ref = " + eventRef );
        return ResolvedEventData.ERROR;
    }
}
