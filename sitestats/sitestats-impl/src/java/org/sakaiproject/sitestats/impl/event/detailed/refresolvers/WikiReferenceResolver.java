package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.wiki.PageData;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser.GenericEventRef;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils.RefResolverUtils;

/**
 * Resolves Wiki references into meaningful details.
 *
 * @author bjones86, plukasew
 */
@Slf4j
public class WikiReferenceResolver
{
    public static final String TOOL_ID = "sakai.rwiki";

    /**
     * Resolves the given event reference into meaningful details
     * @param eventRef the event reference
     * @param devHlprServ the developer helper service (for building urls)
     * @param tips tips for parsing out the components of the reference
     * @param siteServ the site service
     * @return a PageData object, or ResolvedEventData.ERROR;
     */
    public static ResolvedEventData resolveReference( String eventRef, DeveloperHelperService devHlprServ, List<EventParserTip> tips, SiteService siteServ )
    {
        if( StringUtils.isBlank( eventRef ) || devHlprServ == null || siteServ == null )
        {
            log.warn( "Cannot resolve reference. Reference is null/empty or service(s) are not initialized." );
            return ResolvedEventData.ERROR;
        }

        // if necessary, strip trailing period from the event ref
        if( eventRef.endsWith( "." ) )
        {
            eventRef = eventRef.substring( 0, eventRef.length() - 1 );
        }

        GenericEventRef ref = GenericRefParser.parse( eventRef, tips );
        String siteID = ref.contextId;
        String pageName = ref.entityId;
        String url = "";

        // Build the URL to the page
        Optional<Site> site = RefResolverUtils.getSiteByID( siteID, siteServ, log );
        if( site.isPresent() )
        {
            ToolConfiguration toolConfig = site.get().getToolForCommonId( TOOL_ID );
            if( toolConfig != null )
            {
                String pageNameEncoded = RefResolverUtils.urlEncode( pageName, log );
                if( StringUtils.isNotBlank( pageNameEncoded ) )
                {
                    url = devHlprServ.getPortalURL() + "/site/" + siteID
                            + "/tool/" + toolConfig.getId() + "?pageName=/site/" + siteID + "/" + pageNameEncoded
                            + "&action=view&panel=Main&realm=/site/" + siteID;
                }
            }
        }

        if( url.isEmpty() )
        {
            log.warn( "Unable to retrieve data; ref = " + eventRef );
            return ResolvedEventData.ERROR;
        }

        return new PageData(pageName, url);
    }
}
