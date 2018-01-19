/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.coursemanagement.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteTitleAdvisor;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * SAK-29138 - Default implementation of SiteTitleAdvisor, used to retrieve the 
 * site or section title conditionally.
 * 
 * @author bjones86
 */
@Slf4j
public class SiteTitleAdvisorCMS implements SiteTitleAdvisor
{
    // Services
    @Getter @Setter private static UserDirectoryService uds;
    @Getter @Setter private static SecurityService ss;
    @Getter @Setter private static CourseManagementService cms;
    @Getter @Setter private static ServerConfigurationService scs;
    @Getter @Setter private static AuthzGroupService azgs;

    // Constants
    private static final String DEFAULT_PREFERRED_CAT = "LEC";
    private static final String SITE_UPDATE_PERMISSION = "site.upd";
    private static final String CM_STUDENT_ROLE_KEY = "S";

    // sakai.properties
    private static final String SAK_PROP_PORTAL_USE_SECTION_TITLE = "portal.use.sectionTitle";
    private static final String SAK_PROP_PORTAL_USE_SEC_TITLE_PREFERRED_CAT = "portal.use.sectionTitle.preferredCategory";
    private static boolean portalUseSectionTitle;
    private static String portalUseSecTitlePreferredCategory;

    /**
     * Initialization
     */
    public void init()
    {
        portalUseSectionTitle = scs.getBoolean( SAK_PROP_PORTAL_USE_SECTION_TITLE, false );
        portalUseSecTitlePreferredCategory = scs.getString( SAK_PROP_PORTAL_USE_SEC_TITLE_PREFERRED_CAT, DEFAULT_PREFERRED_CAT );
    }

    /**
     * {@inheritDoc}
     */
    public String getUserSpecificSiteTitle( Site site, String userID, List<String> siteProviders )
    {
        // Short circuit - only continue if sakai.property set to true
        if( portalUseSectionTitle )
        {
            // Get the user by the ID supplied
            User currentUser = null;
            if( StringUtils.isNotBlank( userID ) )
            {
                try
                {
                    currentUser = uds.getUser( userID );
                }
                catch( UserNotDefinedException ex )
                {
                    log.warn( "Can't find user with ID = " + userID, ex );
                }
            }

            // If we couldn't get the user, try to get the 'current' user
            if( currentUser == null )
            {
                currentUser = uds.getCurrentUser();
            }

            // Short circuit - only continue if user is not null and user does not have the site.upd permission in the given site
            if( currentUser != null && !ss.unlock( currentUser, SITE_UPDATE_PERMISSION, site.getReference() ) )
            {
                Collection<String> providerIDs = siteProviders;
                if( providerIDs == null )
                {
                    String realmID = site.getReference();
                    providerIDs = azgs.getProviderIDsForRealms( ((List<String>) Arrays.asList( new String[] {realmID} )) ).get( realmID );
                }

                // Short circuit - only continue if there are more than one provider ID (cross listed site)
                if( CollectionUtils.isNotEmpty( providerIDs ) )
                {
                    // Get the current user's section membership/role map
                    Map<String, String> sectionRoles = cms.findSectionRoles( currentUser.getEid() );

                    // Iterate over the section IDs for the site; find the first matching section ID
                    for( String sectionID : providerIDs )
                    {
                        // If the user is enrolled in the current section of the site AND is a student
                        String sectionRole = sectionRoles.get( sectionID );
                        if( CM_STUDENT_ROLE_KEY.equals( sectionRole ) )
                        {
                            try
                            {
                                // If the section is of the preferred type, use the section title instead of the site title
                                Section section = cms.getSection( sectionID );
                                if( portalUseSecTitlePreferredCategory.equals( section.getCategory() ) )
                                {
                                    return section.getTitle();
                                }
                            }
                            catch( IdNotFoundException ex )
                            {
                                log.warn( "SiteTitleAdvisorCMS.getSiteOrSectionTitle: section not found " + sectionID, ex );
                            }
                        }
                    }
                }
            }
        }

        // If the section title could not be found, the algorithm hit one of the short circuits,
        // sakai property not set or set to false, or didn't find section or preferred type; use default behaviour
        return site.getTitle();
    }
}
