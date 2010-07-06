/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.api.app.roster;

import java.util.List;

import org.sakaiproject.section.api.coursemanagement.CourseSection;


/**
 * @author rshastri 
 */
public interface RosterManager
{
  // Roster filters
  public static final String VIEW_ALL_SECT = "roster_all_sections";
  public static final String VIEW_SECT_CATEGORY_PREFIX = "roster_category_";

    public void init();

    public void destroy();

    /**
     * Check for export permission (roster.export)
     * @return
     */
    public boolean currentUserHasExportPerm();

    /**
     *  Get the sections viewable by current user
     * @return
     */
    public List<CourseSection> getViewableSectionsForCurrentUser();

    /**
     *  Get the sections which are viewable by current user and for which the current
     *  user has roster.viewenrollmentstatus
     * @return
     */
    public List<CourseSection> getViewableEnrollmentStatusSectionsForCurrentUser();

    /**
     * @return An unfiltered List of viewable (to current user) Participants in the site.
     */
    public List<Participant> getRoster();
  
    /**
     * @param groupReference The Reference string for the group
     * @return An List of viewable (to current user) Participants in a single group.
     */
    public List<Participant> getRoster(String groupReference);

    /**
     * Returns a participant by the id
     * @param participantId
     * @return
     */
    public Participant getParticipantById(String participantId);

    /**
     * Are user profiles viewable for the current user in this site?
     *
     * @return
     */
    public boolean isProfilesViewable();

    /**
     * Are users official photos viewable for the current user in this site?
     * @return
     */
    public boolean isOfficialPhotosViewable();

    /**
     * Display section/group dropdown filter when site has only a single group or section defined: true or false
     * @return true or false     
     */   
    
    /**
     * Is there a group defined in the site for the group membership link to be displayed?
     * @return
     */
    public boolean isGroupMembershipViewable();
}
