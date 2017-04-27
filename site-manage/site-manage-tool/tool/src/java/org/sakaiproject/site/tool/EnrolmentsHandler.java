/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msub/uwo.ca/site-manage/trunk/site-manage-tool/tool/src/java/org/sakaiproject/site/tool/EnrolmentsHandler.java $
 * $Id: EnrolmentsHandler.java 320558 2015-08-17 15:39:54Z bjones86@uwo.ca $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;

/**
 * Handles most aspects of the 'My Official Course Enrolments' page in the Membership tool.
 * See SAK-32087
 * 
 * @author bjones86
 */
public class EnrolmentsHandler
{
    // Logger
    private static final Log LOG = LogFactory.getLog(EnrolmentsHandler.class );

    // Sakai APIs
    private static final UserDirectoryService       UDS             = (UserDirectoryService)    ComponentManager.get( UserDirectoryService.class );
    private static final CourseManagementService    CMS             = (CourseManagementService) ComponentManager.get( CourseManagementService.class );
    private static final AuthzGroupService          AZGS            = (AuthzGroupService)       ComponentManager.get( AuthzGroupService.class );
    private static final SiteService                SITE_SERV       = (SiteService)             ComponentManager.get( SiteService.class );
    private static final SecurityService            SEC_SERV        = (SecurityService)         ComponentManager.get( SecurityService.class );

    // sakai.properties
    private static final String SAK_PROP_PORTAL_USE_SECTION_TITLE           = "portal.use.sectionTitle";
    private static final String SAK_PROP_PORTAL_USE_SEC_TITLE_PREFERRED_CAT = "portal.use.sectionTitle.preferredCategory";
    private static final String USE_SEC_TITLE_PREFERRED_CAT                 = ServerConfigurationService.getString( SAK_PROP_PORTAL_USE_SEC_TITLE_PREFERRED_CAT, "LEC" );
    private static final boolean USE_SEC_TITLE                              = ServerConfigurationService.getBoolean( SAK_PROP_PORTAL_USE_SECTION_TITLE, false );

    // Academic session helper object
    public static final AcademicSessionHelper SESSION_HELPER = new AcademicSessionHelper( CMS.getAcademicSessions() );

    // Sorting constants
    private static final String SORT_MODE           = "sort_mode";
    private static final String USE_TERM_SORT       = "use_term_sort";
    private static final String USE_SECTION_SORT    = "use_section_sort";
    private static final String USE_SITE_SORT       = "use_site_sort";

    // Cache settings
    private static final int  TTL                        = 5;
    private static final long NANO_SECONDS_PER_MINUTE    = 60000000000L;

    // Permissions and realm prefix constants
    private static final String PERM_VISIT_UNPUB    = "site.visit.unp";
    private static final String PERM_SITE_UPDATE    = "site.upd";
    private static final String GROUP_REALM_PREFIX  = "/group/";
    private static final String SITE_REALM_PREFIX   = "/site/";

    // CMS 'Student' role constant
    private static final String CMS_STUDENT_ROLE = "S";

    // Enrolment cache map and filtered list
    private final Map<String, EnrolmentsWrapper>    enrolmentsCacheMap = new HashMap<>();
    private final List<Enrolment>                   filteredEnrolments = new ArrayList<>();

    // Getters for the enrolment map and filtered list
    public Map<String, EnrolmentsWrapper> getEnrolmentsCacheMap()   { return enrolmentsCacheMap.isEmpty() ? Collections.EMPTY_MAP : enrolmentsCacheMap; }
    public List<Enrolment> getFilteredEnrolments()                  { return filteredEnrolments.isEmpty() ? Collections.EMPTY_LIST : filteredEnrolments; }

    // Common section title comparator
    private static final Comparator<Enrolment> SECTION_COMP = (Enrolment obj1, Enrolment obj2) -> obj1.getSectionTitle().compareTo( obj2.getSectionTitle() );

    /**
     * Get all section memberships for the current user. This algorithm will only 
     * run if the cache for the current user doesn't yet exist, or has expired.
     * 
     * @param currentUserID the internal ID of the current user
     */
    public void getSectionEnrolments( String currentUserID )
    {
        // Only refresh the data for the current user if the cache for the user doesn't exist (first run), or if the user's cache TTL has expired
        boolean userCacheExists = enrolmentsCacheMap.containsKey( currentUserID );
        long currentUserFetchTime = userCacheExists ? enrolmentsCacheMap.get( currentUserID ).getFetchTimeNano() : 0L;
        long elapsedMinutesSinceLastQuery = (System.nanoTime() - currentUserFetchTime) / NANO_SECONDS_PER_MINUTE;
        if( !userCacheExists || (currentUserFetchTime == 0 || elapsedMinutesSinceLastQuery >= TTL) )
        {
            List<Enrolment> enrolmentEntries = new ArrayList<>();
            Set<String> sectionEIDs = CMS.findSectionRoles( UDS.getCurrentUser().getEid() ).keySet();
            for( String sectionEID : sectionEIDs )
            {
                Section section = null;
                try
                {
                    section = CMS.getSection( sectionEID );
                }
                catch( IdNotFoundException ex )
                {
                    LOG.debug( "Couldn't find section with id=" + sectionEID, ex );
                }

                if( section != null )
                {
                    Set<String> realmIDs = AZGS.getAuthzGroupIds( section.getEid() );
                    List<SiteTitleUrlWrapper> siteWrappers = new ArrayList<>();
                    for( String realmID : realmIDs )
                    {
                        // Skip to next iteration if this is a group realm
                        if( realmID.contains( GROUP_REALM_PREFIX ) )
                        {
                            continue;
                        }

                        try
                        {
                            // Only put the site in the map if it exists and it's either published, or the user has the site.visit.unp permission
                            Site site = SITE_SERV.getSite( realmID.replace( SITE_REALM_PREFIX, "" ) );
                            if( site != null && (site.isPublished() || site.isAllowed( currentUserID, PERM_VISIT_UNPUB )) )
                            {
                                siteWrappers.add( new SiteTitleUrlWrapper( SITE_SERV.getUserSpecificSiteTitle( site, UDS.getCurrentUser().getId()), site.getUrl() ) );
                            }
                        }
                        catch( IdUnusedException ex )
                        {
                            LOG.debug( "Couldn't find site with id=" + realmID, ex );
                        }
                    }

                    // Build the SectionWrapper object
                    String sessionEID = CMS.getCourseOffering( section.getCourseOfferingEid() ).getAcademicSession().getEid();
                    enrolmentEntries.add( new Enrolment( section.getTitle(), sessionEID, siteWrappers ) );
                }
            }

            // Remove the old data from the cache, dump the data into the cache, update the fetch time; purge any expired caches
            enrolmentsCacheMap.remove( currentUserID );
            EnrolmentsWrapper enrolmentsWrapper = new EnrolmentsWrapper( System.nanoTime(), enrolmentEntries );
            enrolmentsCacheMap.put( currentUserID, enrolmentsWrapper );
            purgeExpiredCaches();
        }
    }

    /**
     * Purge all entries in the cache that have expired.
     */
    private void purgeExpiredCaches()
    {
        // Remove any entries in the cache map who's TTL has expired
        for( Iterator<Entry<String, EnrolmentsWrapper>> it = enrolmentsCacheMap.entrySet().iterator(); it.hasNext(); )
        {
            Entry<String, EnrolmentsWrapper> entry = it.next();
            long fetchTime = entry.getValue().getFetchTimeNano();
            long elapsedMinutesSinceLastQuery = (System.nanoTime() - fetchTime) / NANO_SECONDS_PER_MINUTE;
            if( elapsedMinutesSinceLastQuery >= TTL )
            {
                it.remove();
            }
        }
    }

    /**
     * Filter the enrolments for the current user based on the search term provided.
     * 
     * @param searchText the search term entered by the user
     * @param currentUserID the internal ID of the current user
     */
    public void filterSectionEnrolments( String searchText, String currentUserID )
    {
        // Filter the results if a search term is provided
        if( StringUtils.isNotBlank( searchText ) )
        {
            // Clear out any previously filtered enrolments; double check the cache
            filteredEnrolments.clear();
            if( enrolmentsCacheMap.get( currentUserID ) == null )
            {
                getSectionEnrolments( currentUserID );
            }

            // Determine if any of the site titles (if the enrolment has any) matches the search term
            for( Enrolment enrolment : enrolmentsCacheMap.get( currentUserID ).getEnrolments() )
            {
                boolean siteTitleMatchesSearch = false;
                for( SiteTitleUrlWrapper site : enrolment.getSiteWrappers() )
                {
                    if( site.getSiteTitle().toLowerCase().contains( searchText.toLowerCase() ) )
                    {
                        siteTitleMatchesSearch = true;
                        break;
                    }
                }

                // If the session title, section title or any of the site title's match the search term, add the enrolment to the filtered list
                if( SESSION_HELPER.getSessionByEID( enrolment.getSessionEID() ).getTitle().toLowerCase().contains( searchText.toLowerCase() )
                        || enrolment.getSectionTitle().toLowerCase().contains( searchText.toLowerCase() ) || siteTitleMatchesSearch )
                {
                    filteredEnrolments.add( enrolment );
                }
            }
        }
    }

    /**
     * Get the requested sort mode from the user and put it into the state.
     * 
     * @param data
     * @param state
     */
    public void getSortModeFromMyEnrolments( RunData data, SessionState state )
    {
        ParameterParser params = data.getParameters();
        String sortParam = params.get( "sortParam" );
        if( StringUtils.isNotBlank( sortParam ) )
        {
            state.setAttribute( SORT_MODE, sortParam );
        }
    }

    /**
     * Set the sort mode for the context
     * 
     * @param state
     * @return the sort mode being used
     */
    public String setSortModeForMyEnrolments( SessionState state )
    {
        String sortMode = USE_TERM_SORT;
        if( state.getAttribute( SORT_MODE ) != null )
        {
            sortMode = (String) state.getAttribute( SORT_MODE );
        }
        else
        {
            state.setAttribute( SORT_MODE, sortMode );
        }

        return sortMode;
    }

    /**
     * Get a sublist of the current user's enrolments that is paged and sorted.
     * 
     * @param page the page requested
     * @param sortMode the sort mode requested
     * @param sortAsc true if sorting ascending; false otherwise
     * @param isFiltered true if the list is filtered based on user's provided search term; false otherwise
     * @return the requested sublist
     */
    public List<Enrolment> getSortedAndPagedEnrolments( PagingPosition page, String sortMode, boolean sortAsc, boolean isFiltered )
    {
        // Get the current user ID; double check the cache; use the appropriate list (filtered or not)
        String currentUserID = UDS.getCurrentUser().getId();
        if( enrolmentsCacheMap.get( currentUserID ) == null )
        {
            getSectionEnrolments( currentUserID );
        }
        List<Enrolment> retVal = isFiltered ? filteredEnrolments : enrolmentsCacheMap.get( currentUserID ).getEnrolments();

        // Apply the requested sort
        if( USE_TERM_SORT.equals( sortMode ) )
        {
            retVal = sortByTerm( retVal, sortAsc );
        }
        else if( USE_SECTION_SORT.equals( sortMode ) )
        {
            Collections.sort( retVal, sortAsc ? SECTION_COMP : Collections.reverseOrder( SECTION_COMP ) );
        }
        else if( USE_SITE_SORT.equals( sortMode ) )
        {
            retVal = sortBySiteTitle( retVal, sortAsc );
        }

        // Return the requested page (sub list)
        return retVal.subList( page.getFirst() > page.getLast() ? 0 : page.getFirst() - 1,
                                page.getLast() > retVal.size() ? retVal.size() : page.getLast() );
    }

    /**
     * Sort the given list of Enrolments by the first site title tied to it (if any)
     * 
     * @param list the list to be sorted
     * @param sortAsc true if sorting ascending; false otherwise
     * @return the sorted list
     */
    private List<Enrolment> sortBySiteTitle( List<Enrolment> list, boolean sortAsc )
    {
        Comparator<Enrolment> comp = (Enrolment obj1, Enrolment obj2) ->
        {
            String siteTitle1 = "";
            for( SiteTitleUrlWrapper site : obj1.getSiteWrappers() )
            {
                siteTitle1 = site.getSiteTitle();
                break;
            }

            String siteTitle2 = "";
            for( SiteTitleUrlWrapper site : obj2.getSiteWrappers() )
            {
                siteTitle2 = site.getSiteTitle();
                break;
            }

            return siteTitle1.compareTo( siteTitle2 );
        };

        Collections.sort( list, sortAsc ? comp : Collections.reverseOrder( comp ) );
        return list;
    }

    /**
     * Sort the given list of Enrolments by term. This sort is multi-factored:
     * First on start date of the term (Academic Session), secondly on section title.
     * 
     * @param list the list of Enrolments to be sorted
     * @param sortAsc true if sorting ascending; false otherwise
     * @return the sorted list
     */
    private List<Enrolment> sortByTerm( List<Enrolment> list, boolean sortAsc )
    {
        // Get a copy of the sessions (ordered by start date by default); reverse sort if necessary
        List<AcademicSession> sessions = new ArrayList<>( SESSION_HELPER.getSessions() );
        if( !sortAsc )
        {
            Collections.reverse( sessions );
        }

        // Create a bucket for each session
        Map<String, List<Enrolment>> buckets = new HashMap<>();
        for( AcademicSession session : sessions )
        {
            buckets.put( session.getEid(), new ArrayList<>() );
        }

        // Sort the SectionWrapper objects into the buckets
        for( Enrolment wrapper : list )
        {
            buckets.get( wrapper.getSessionEID() ).add( wrapper );
        }

        // Sort each bucket by the section title; rebuild the list now that everything is sorted
        List<Enrolment> retVal = new ArrayList<>();
        for( AcademicSession session : sessions )
        {
            Collections.sort( buckets.get( session.getEid() ), SECTION_COMP );
            retVal.addAll( buckets.get( session.getEid() ) );
        }

        return retVal;
    }

    /* Helper Classes */

    /**
     * Wrapper object for Enrolment objects, so we can cache and evict individual user's enrolments.
     */
    public class EnrolmentsWrapper
    {
        // Member variables
        private final long fetchTimeNano;
        private final List<Enrolment> enrolments;

        /**
         * Constructor.
         * 
         * @param fetchTimeNano time (in nanoseconds) when the query was performed
         * @param enrolments the list of enrolments for this cache entry
         */
        public EnrolmentsWrapper( long fetchTimeNano, List<Enrolment> enrolments )
        {
            this.fetchTimeNano = fetchTimeNano;
            this.enrolments = enrolments;
        }

        // Getters
        public long             getFetchTimeNano()  { return fetchTimeNano; }
        public List<Enrolment>  getEnrolments()     { return enrolments.isEmpty() ? Collections.EMPTY_LIST : enrolments; }
    }

    /**
     * Wrapper object used for UI presentation of enrolments.
     */
    public class Enrolment
    {
        // Member variables
        private final String sectionTitle;
        private final String sessionEID;
        private final List<SiteTitleUrlWrapper> siteWrappers;

        /**
         * Constructor.
         * 
         * @param sectionTitle the title of the section
         * @param sessionEID the EID of the academic session the section belongs to
         * @param siteWrappers a list of simple objects, each containing the site title and URL for a specific site
         */
        public Enrolment( String sectionTitle, String sessionEID, List<SiteTitleUrlWrapper> siteWrappers )
        {
            this.sectionTitle = sectionTitle;
            this.sessionEID = sessionEID;
            this.siteWrappers = siteWrappers;
        }

        // Getters
        public String   getSectionTitle()   { return sectionTitle; }
        public String   getSessionEID()     { return StringUtils.trimToEmpty( sessionEID ); }

        // Convenience methods
        public List<SiteTitleUrlWrapper> getSiteWrappers() { return CollectionUtils.isEmpty( siteWrappers ) ? Collections.EMPTY_LIST : siteWrappers; }
    }

    /**
     * Convenience wrapper object to contain a site's title and corresponding URL.
     */
    public class SiteTitleUrlWrapper
    {
        // Member variables
        private final String siteTitle;
        private final String siteURL;

        /**
         * Constructor.
         * 
         * @param siteTitle the title of the site
         * @param siteURL the URL of the site
         */
        public SiteTitleUrlWrapper( String siteTitle, String siteURL )
        {
            this.siteTitle = siteTitle;
            this.siteURL = siteURL;
        }

        // Getters
        public String getSiteTitle()    { return siteTitle; }
        public String getSiteURL()      { return siteURL; }
    }

    /**
     * Convenience object to store and get AcademicSession objects.
     */
    public static class AcademicSessionHelper
    {
        // Member variables
        private final List<AcademicSession> sessions;

        /**
         * Constructor.
         * 
         * @param sessions a list of all Academic Sessions in the database
         */
        public AcademicSessionHelper( List<AcademicSession> sessions )
        {
            this.sessions = sessions;
        }

        // Getters
        public List<AcademicSession> getSessions() { return sessions.isEmpty() ? Collections.EMPTY_LIST : sessions; }

        /**
         * Get an AcademicSession object by it's EID.
         * 
         * @param sessionEID the EID of the requested AcademicSession
         * @return the AcademicSession object requested
         */
        public AcademicSession getSessionByEID( String sessionEID )
        {
            if( sessionEID != null )
            {
                for( AcademicSession session : sessions )
                {
                    if( sessionEID.equals( session.getEid() ) )
                    {
                        return session;
                    }
                }
            }

            return null;
        }
    }
}
