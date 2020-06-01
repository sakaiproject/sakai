/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.site.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.comparator.GroupTitleComparator;
import org.sakaiproject.util.comparator.RoleIdComparator;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles providing and managing entries and selections of the 'View' filter drop down in 'Manage Participants'
 *
 * @author bjones86
 */
@Slf4j
public class ParticipantFilterHandler
{
    // APIs
    private static final CourseManagementService CMS = (CourseManagementService) ComponentManager.get( CourseManagementService.class );

    // Comparators
    private static final Comparator<Role>       SORT_BY_ROLE_TITLE      = new RoleIdComparator();
    private static final Comparator<Group>      SORT_BY_GROUP_TITLE     = new GroupTitleComparator();
    private static final Comparator<Section>    SORT_BY_SECTION_TITLE   = (Section section1, Section section2)  -> section1.getTitle().compareTo( section2.getTitle() );

    /**
     * Put the possible filter entries into the context in the desired order (all, sections, roles, then groups).
     * 
     * @param context
     * @param rl
     * @param site
     */
    public static void putFilterEntriesInContext( Context context, ResourceLoader rl, Site site )
    {
        // Add the 'all' entry
        LinkedHashMap<String, String> entries = new LinkedHashMap<>();
        entries.put( SiteConstants.PARTICIPANT_FILTER_TYPE_ALL, rl.getString( "sitegen.siteinfolist.filter.all" ) );

        // Add all available sections into the list of entries
        addAllSectionEntries( entries, site );

        // Add all available roles into the list of entries
        addAllRoleEntries( entries, site, rl.getString( "sitegen.siteinfolist.filter.role.postfix" ) );

        // Add all available groups into the list of entries
        addAllGroupEntries( entries, site, rl.getString( "sitegen.siteinfolist.filter.group.postfix" ) );

        // Put the entries into the context for consumption
        context.put( "filterEntries", entries );
    }

    /**
     * Get all participants, with any filtering options applied.
     * 
     * @param siteID
     * @param providerCourseIDs
     * @param selectedFilter
     * @return
     */
    public static Collection<Participant> prepareParticipantsWithFilter( String siteID, List<String> providerCourseIDs, String selectedFilter )
    {
        if( StringUtils.isNotBlank( selectedFilter ) )
        {
            // Identify the selected filter type and ID
            String[] parts = selectedFilter.split( "]" );

            // Only perform the filtering if the filter string has the required information
            if( parts != null && parts.length == 2 )
            {
                String filterType = identifyFilterType( selectedFilter );
                String filterID = SiteConstants.PARTICIPANT_FILTER_TYPE_ALL.equals( filterType ) ? "" : parts[1];

                // Get the participants using the selected filter
                return SiteParticipantHelper.prepareParticipants( siteID, providerCourseIDs, filterType, filterID );
            }
        }

        return SiteParticipantHelper.prepareParticipants( siteID, providerCourseIDs );
    }

    /**
     * Puts the user's selected filter choice into the context for the velocity template.
     *
     * @param state
     * @param context
     */
    public static void putSelectedFilterIntoContext( SessionState state, Context context )
    {
        String selectedFilter = (String) state.getAttribute( SiteAction.STATE_SITE_PARTICIPANT_FILTER );
        if( StringUtils.isBlank( selectedFilter ) )
        {
            selectedFilter = SiteConstants.PARTICIPANT_FILTER_TYPE_ALL;
        }
        context.put( "selectedFilter", selectedFilter );
    }

    /**
     * Puts the user's selected filter choice into the state for retrieval.
     * 
     * @param data
     */
    public static void putSelectedFilterIntoState( RunData data )
    {
        SessionState state = ((JetspeedRunData) data).getPortletSessionState( ((JetspeedRunData) data).getJs_peid() );
        String filter = StringUtils.trimToNull( Validator.escapeHtml( data.getParameters().getString( "view" ) ) );
        if( StringUtils.isNotBlank( filter ) )
        {
            state.setAttribute( SiteAction.STATE_SITE_PARTICIPANT_FILTER, filter );
        }
        else
        {
            state.removeAttribute( SiteAction.STATE_SITE_PARTICIPANT_FILTER );
        }
    }

    /**
     * Identify the filter type (all, section, role, or group) based on the selected filter option.
     *
     * @param selectedFilter the option selected by the user
     * @return the identified filter type
     */
    private static String identifyFilterType( String selectedFilter )
    {
        if( selectedFilter.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP;
        }
        else if( selectedFilter.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION;
        }
        else if( selectedFilter.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE;
        }

        return SiteConstants.PARTICIPANT_FILTER_TYPE_ALL;
    }

    /**
     * Utility method to add all groups as entries in the map.
     *
     * @param entries the map to add entries to
     * @param site the currently active site
     * @param groupTitlePostfix the postfix to use for groups in the dropdown
     */
    private static void addAllGroupEntries( LinkedHashMap<String, String> entries, Site site, String groupTitlePostfix )
    {
        if( site != null )
        {
            List<Group> groups = (List<Group>) site.getGroups();
            if( groups != null )
            {
                // Sort the groups before adding to the map
                Collections.sort( groups, SORT_BY_GROUP_TITLE );
                for( Group group : groups )
                {
                    String prop = group.getProperties().getProperty( SiteConstants.GROUP_PROP_WSETUP_CREATED );
                    if( Boolean.TRUE.toString().equals( prop ) )
                    {
                        entries.put( SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP + group.getId(), group.getTitle() + " " + groupTitlePostfix );
                    }
                }
            }
        }
    }

    /**
     * Utility method to add all roles as entries in the map.
     *
     * @param entries the map to add entries to
     * @param site the currently active site
     * @param roleTitlePostfix the postfix to use for roles in the dropdown
     */
    private static void addAllRoleEntries( LinkedHashMap<String, String> entries, Site site, String roleTitlePostfix )
    {
        if( site != null )
        {
            // Sort the roles before adding to the map
            List<Role> roles = new ArrayList<>( site.getRoles() );
            Collections.sort( roles, SORT_BY_ROLE_TITLE );
            for( Role role : roles )
            {
                entries.put( SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE + role.getId(), role.getId() + " " + roleTitlePostfix );
            }
        }
    }

    /**
     * Utility method to add all sections as entries in the map.
     *
     * @param entries the map to add entries to
     * @param site the currently active site
     */
    private static void addAllSectionEntries( LinkedHashMap<String, String> entries, Site site )
    {
        if( site != null )
        {
            List<String> sectionIDs = SiteParticipantHelper.getProviderCourseList( site.getId() );
            if( !sectionIDs.isEmpty() )
            {
                List<Section> sections = new ArrayList<>( sectionIDs.size() );
                for( String sectionID : sectionIDs )
                {
                    try
                    {
                        Section section = CMS.getSection( sectionID );
                        if( section != null )
                        {
                            sections.add( section );
                        }
                    }
                    catch( IdNotFoundException ex )
                    {
                        log.warn( "Can't find section {}", sectionID, ex);
                    }
                }

                // Sort the sections by readable title before adding to the map
                Collections.sort( sections, SORT_BY_SECTION_TITLE );
                for( Section section : sections )
                {
                    entries.put( SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION + section.getEid(), section.getTitle() );
                }
            }
        }
    }
}
