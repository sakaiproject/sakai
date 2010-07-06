/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.roster;

import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.PhotoService;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.event.api.EventTrackingService;

public class ServicesBean {

    static final String MESSAGE_BUNDLE = "org.sakaiproject.tool.roster.bundle.Messages";

    // Our service references
    protected RosterManager rosterManager;
    protected SectionAwareness sectionAwareness;
    protected CourseManagementService cmService;
    protected AuthzGroupService authzService;
    protected SecurityService securityService;
    protected SiteService siteService;
    protected ToolManager toolManager;
    protected UserDirectoryService userDirectoryService;
    protected ServerConfigurationService serverConfigurationService;
    protected ProfileManager profileManager;
    protected PhotoService photoService;
    protected EventTrackingService eventTrackingService;

    // Service injection
    public void setRosterManager(RosterManager rosterManager) {
        this.rosterManager = rosterManager;
    }
    public void setSectionAwareness(SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }
    public void setCmService(CourseManagementService cmService) {
        this.cmService = cmService;
    }
    public void setAuthzService(AuthzGroupService authzService) {
        this.authzService = authzService;
    }
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }
    public void setServerConfigurationService(
            ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }
    public void setPhotoService(PhotoService photoService) {
        this.photoService = photoService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService){
        this.eventTrackingService = eventTrackingService;
    }

    // Don't inject the group provider, since it may not exist.
    public GroupProvider getGroupProvider() {
        return (GroupProvider)ComponentManager.get(GroupProvider.class);
    }

}
