/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.assignment;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.bluejoe.elfinder.service.FsItem;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Created by neelam on 11-Jan-16.
 */
@Slf4j
public class AssignmentSiteVolumeFactory implements SiteVolumeFactory {

    private AssignmentService assignmentService;
    private UserDirectoryService userDirectoryService;
    private ServerConfigurationService serverConfigurationService;
    private static final String ASSIGNMENT_URL_PREFIX = "/direct/assignment/";

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public String getPrefix() {
        return "assignment";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new AssignmentSiteVolume(sakaiFsService,siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.assignment.grades";
    }
    
    public class AssignmentSiteVolume extends ReadOnlyFsVolume implements SiteVolume{
        private SakaiFsService service;
        private String siteId;

        public AssignmentSiteVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return this.siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return AssignmentSiteVolumeFactory.this;
        }

        @Override
        public boolean isWriteable(FsItem item) {
            return false;
        }

        @Override
        public boolean exists(FsItem fsItem) {
            return false;
        }

        @Override
        public FsItem fromPath(String path){
            if(path != null && !path.isEmpty()){
                String[] parts = path.split("/");
                if(parts.length > 2 && (getPrefix().equals(parts[1]))){
                    try {
                        Assignment assignment = assignmentService.getAssignment(parts[2]);
                        return new AssignmentFsItem(this, assignment.getId(), assignment.getTitle());
                    } catch (IdUnusedException e) {
                        log.warn("Unexpected IdUnusedException for assignment in " + e.getClass().getName() + ": " + e.getMessage());
                    } catch (PermissionException e) {
                        log.warn("Unexpected Permission Exception for assignment in  " + e.getClass().getName() + ": " + e.getMessage());
                    }

                }
            }
            return this.getRoot();
        }

        @Override
        public String getDimensions(FsItem fsItem) {
            return null;
        }

        @Override
        public long getLastModified(FsItem fsItem) {
            return 0L;
        }

        @Override
        public String getMimeType(FsItem fsItem) {
            return this.isFolder(fsItem)?"directory":"sakai/assignments";
        }

        @Override
        public String getName() {
            return "Assignments";
        }

        @Override
        public String getName(FsItem fsItem) {
            if(this.getRoot().equals(fsItem)) {
                return getName();
            }
            else if(fsItem instanceof AssignmentFsItem){
                return ((AssignmentFsItem)fsItem).getTitle();
            }
            else{
                throw new IllegalArgumentException("Could not get title for: " + fsItem.toString());
            }
        }

        @Override
        public FsItem getParent(FsItem fsItem) {
            if(this.getRoot().equals(fsItem)){
                return service.getSiteVolume(siteId).getRoot();
            }
            else if(fsItem instanceof AssignmentFsItem){
                return this.getRoot();
            }
            return null;
        }

        @Override
        public String getPath(FsItem fsItem) throws IOException {
            if(this.getRoot().equals(fsItem)) {
                return "";
            }
            else if(fsItem instanceof AssignmentFsItem){
                AssignmentFsItem assignmentFsItem = (AssignmentFsItem)fsItem;
                return "/"+ getPrefix() +"/" + assignmentFsItem.getId();
            }
            else{
                throw new IllegalArgumentException("Wrong Type: " + fsItem.toString());
            }
        }

        @Override
        public FsItem getRoot() {
            return new AssignmentFsItem(this, "", "");
        }

        @Override
        public long getSize(FsItem fsItem) throws IOException {
            return 0;
        }

        @Override
        public String getThumbnailFileName(FsItem fsItem) {
            return null;
        }

        @Override
        public boolean hasChildFolder(FsItem fsItem) {
            return fsItem instanceof AssignmentFsItem;
        }

        @Override
        public boolean isFolder(FsItem fsItem) {
            if(fsItem instanceof AssignmentFsItem && ((AssignmentFsItem)fsItem).getTitle().equals(""))
                return true;
            return false;
        }

        @Override
        public boolean isRoot(FsItem fsItem) {
            return false;
        }

        @Override
        public FsItem[] listChildren(FsItem fsItem) {
            List<FsItem> items = new ArrayList<>();
            if(this.getRoot().equals(fsItem)){
                for (Assignment thisAssignment : assignmentService.getAssignmentsForContext(this.siteId)) {
                    Instant thisAssignmentCloseTime = thisAssignment.getCloseDate();
                    boolean assignmentClosed = false;
                    if(thisAssignmentCloseTime!=null){
                        if (thisAssignmentCloseTime.isBefore(Instant.now())) {
                            assignmentClosed=true;
                        }
                    }
                    //Not displaying closed assignment or assignment in draft state
                    if(thisAssignment.getDraft() || assignmentClosed ) continue;
                    items.add(new AssignmentFsItem(this, thisAssignment.getId(), thisAssignment.getTitle()));
                }
                
            }
            else if(fsItem instanceof AssignmentFsItem){
                items.add(fsItem);
            }
            
            return items.toArray(new FsItem[0]);
        }

        @Override
        public InputStream openInputStream(FsItem fsItem) throws IOException {
            return null;
        }

        @Override
        public String getURL(FsItem fsItem) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if(fsItem instanceof AssignmentFsItem){
                AssignmentFsItem assignmentFsItem = (AssignmentFsItem)fsItem;
                return serverUrlPrefix + ASSIGNMENT_URL_PREFIX + assignmentFsItem.getId();
            }
            return null;
        }
    }
}
