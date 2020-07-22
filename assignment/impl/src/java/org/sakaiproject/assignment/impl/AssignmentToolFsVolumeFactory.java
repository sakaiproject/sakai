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
package org.sakaiproject.assignment.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssignmentToolFsVolumeFactory implements ToolFsVolumeFactory {

    @Setter private AssignmentService assignmentService;
    @Setter private SakaiFsService sakaiFsService;
    @Setter private ServerConfigurationService serverConfigurationService;

    public void init() {
        sakaiFsService.registerToolVolume(this);
    }

    @Override
    public String getPrefix() {
        return FsType.ASSIGNMENT.toString();
    }

    @Override
    public ToolFsVolume getVolume(String siteId) {
        return new AssignmentToolFsVolume(sakaiFsService, siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.assignment.grades";
    }
    
    public class AssignmentToolFsVolume extends ReadOnlyFsVolume implements ToolFsVolume {
        private static final String ASSIGNMENT_URL_PREFIX = "/direct/assignment/";
        private SakaiFsService service;
        private String siteId;

        public AssignmentToolFsVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return siteId;
        }

        @Override
        public ToolFsVolumeFactory getToolVolumeFactory() {
            return AssignmentToolFsVolumeFactory.this;
        }

        @Override
        public boolean isWriteable(SakaiFsItem item) {
            return false;
        }

        @Override
        public boolean exists(SakaiFsItem fsItem) {
            return false;
        }

        @Override
        public SakaiFsItem fromPath(String path){
            if (StringUtils.isNotBlank(path)) {
                String[] parts = path.split("/");
                if (parts.length > 2 && (getPrefix().equals(parts[1]))) {
                    try {
                        Assignment assignment = assignmentService.getAssignment(parts[2]);
                        return new SakaiFsItem(assignment.getId(), assignment.getTitle(), this, FsType.ASSIGNMENT);
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
        public String getDimensions(SakaiFsItem fsItem) {
            return null;
        }

        @Override
        public long getLastModified(SakaiFsItem fsItem) {
            return 0L;
        }

        @Override
        public String getMimeType(SakaiFsItem fsItem) {
            return this.isFolder(fsItem) ? "directory" : "sakai/assignments";
        }

        @Override
        public String getName() {
            // TODO i18n
            return "Assignments";
        }

        @Override
        public String getName(SakaiFsItem fsItem) {
            if (this.getRoot().equals(fsItem)) {
                return getName();
            } else if (FsType.ASSIGNMENT.equals(fsItem.getType())) {
                return fsItem.getTitle();
            } else {
                throw new IllegalArgumentException("Could not get title for: " + fsItem.toString());
            }
        }

        @Override
        public SakaiFsItem getParent(SakaiFsItem fsItem) {
            if(this.getRoot().equals(fsItem)){
                return service.getSiteVolume(siteId).getRoot();
            } else if(FsType.ASSIGNMENT.equals(fsItem.getType())){
                return this.getRoot();
            }
            return null;
        }

        @Override
        public String getPath(SakaiFsItem fsi) throws IOException {
            if (this.getRoot().equals(fsi)) {
                return "/" + getPrefix() + "/" + siteId;
            } else if (FsType.ASSIGNMENT.equals(fsi.getType())) {
                return "/" + getPrefix() + "/" + siteId + "/" + fsi.getId();
            } else {
                throw new IllegalArgumentException("Wrong Type: " + fsi.toString());
            }
        }

        @Override
        public SakaiFsItem getRoot() {
            return new SakaiFsItem("", "", this, FsType.ASSIGNMENT);
        }

        @Override
        public long getSize(SakaiFsItem fsItem) throws IOException {
            return 0;
        }

        @Override
        public String getThumbnailFileName(SakaiFsItem fsItem) {
            return null;
        }

        @Override
        public boolean hasChildFolder(SakaiFsItem fsItem) {
            return false;
        }

        @Override
        public boolean isFolder(SakaiFsItem fsItem) {
            return FsType.ASSIGNMENT.equals(fsItem.getType()) && fsItem.getTitle().equals("");
        }

        @Override
        public boolean isRoot(SakaiFsItem fsItem) {
            return false;
        }

        @Override
        public SakaiFsItem[] listChildren(SakaiFsItem fsItem) {
            List<SakaiFsItem> items = new ArrayList<>();
            if (this.getRoot().equals(fsItem)) {
                for (Assignment thisAssignment : assignmentService.getAssignmentsForContext(this.siteId)) {
                    Instant thisAssignmentCloseTime = thisAssignment.getCloseDate();
                    boolean assignmentClosed = false;
                    if (thisAssignmentCloseTime != null && thisAssignmentCloseTime.isBefore(Instant.now())) {
                        assignmentClosed = true;
                    }
                    //Not displaying closed assignment or assignment in draft state
                    if (thisAssignment.getDraft() || assignmentClosed) continue;
                    items.add(new SakaiFsItem(thisAssignment.getId(), thisAssignment.getTitle(), this, FsType.ASSIGNMENT));
                }

            } else if (FsType.ASSIGNMENT.equals(fsItem.getType())) {
                items.add(fsItem);
            }

            return items.toArray(new SakaiFsItem[0]);
        }

        @Override
        public InputStream openInputStream(SakaiFsItem fsItem) throws IOException {
            return null;
        }

        @Override
        public String getURL(SakaiFsItem fsItem) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if (FsType.ASSIGNMENT.equals(fsItem.getType())) {
                return serverUrlPrefix + ASSIGNMENT_URL_PREFIX + fsItem.getId();
            }
            return null;
        }
    }
}
