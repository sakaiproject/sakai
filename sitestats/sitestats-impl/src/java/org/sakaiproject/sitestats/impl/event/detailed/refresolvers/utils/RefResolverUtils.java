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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.slf4j.Logger;

/**
 * Convenience class to house common routines used by several reference resolvers.
 *
 * @author bjones86
 */
public class RefResolverUtils
{
    /**
     * Convenience method to get a Site object by its ID
     * @param siteID the site ID of the object to retrieve
     * @param siteServ the SiteService used to do the retrieval
     * @param log the logger to log exceptions to
     * @return the site object, or null if it can't be found
     */
    public static Optional<Site> getSiteByID( String siteID, SiteService siteServ, Logger log )
    {
        try
        {
            return Optional.ofNullable( siteServ.getSite( siteID ) );
        }
        catch( IdUnusedException ex )
        {
            log.warn( "Unable to get site by ID: " + siteID, ex );
            return Optional.empty();
        }
    }

    /**
     * Convenience method to extract the file name out of the resource ID (ref) given.
     * @param resourceID the resource ref (ID) to parse the file name from
     * @return the file name parsed from the resource ref string, or null
     */
    public static String getResourceFileName( String resourceID )
    {
        if( resourceID == null )
        {
            return null;
        }

        String[] delimiters = { "/", "\\" };
        for( String delimiter : delimiters )
        {
            int lastIndex = resourceID.lastIndexOf( delimiter );
            if( lastIndex >= 0 )
            {
                resourceID = resourceID.substring( lastIndex + 1 );
            }
        }

        return resourceID;
    }

    /**
     * Gets the resource's display name; if it doesn't exist, falls back to the filename
     * @param resource The resource object to retrieve it's pretty name
     * @return The pretty name of the resource object requested
     */
    public static String getResourceName( ContentResource resource )
    {
        ResourceProperties resourceProps = resource.getProperties();
        String displayName = (String) resourceProps.get( ResourceProperties.PROP_DISPLAY_NAME );
        if( StringUtils.isBlank( displayName ) )
        {
            return getResourceFileName( resource.getId() );
        }

        return displayName;
    }

    /**
     * Convenience method to encode the given string to UTF-8 compliant URL string.
     * @param toEncode the string to encode
     * @param log the logger to log exceptions to
     * @return the encoded string
     */
    public static String urlEncode( String toEncode, Logger log )
    {
        try
        {
            return URLEncoder.encode( toEncode, "UTF-8" );
        }
        catch( UnsupportedEncodingException ex )
        {
            log.warn( "Unable to encode string to UTF-8 URL: " + toEncode, ex );
            return null;
        }
    }

    /**
     * Convenience method to decode the given UTF-8 compliant URL string.
     * @param toDecode the string to decode
     * @param log the logger to log exceptions to
     * @return the decoded string
     */
    public static String urlDecode( String toDecode, Logger log )
    {
        try
        {
            return URLDecoder.decode( toDecode, "UTF-8" );
        }
        catch( UnsupportedEncodingException ex )
        {
            log.warn( "Unable to decode string from UTF-8 URL: " + toDecode, ex );
            return null;
        }
    }
}
