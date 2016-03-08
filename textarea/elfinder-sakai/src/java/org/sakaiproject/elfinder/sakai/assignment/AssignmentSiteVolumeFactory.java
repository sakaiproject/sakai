package org.sakaiproject.elfinder.sakai.assignment;

import cn.bluejoe.elfinder.service.FsItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by neelam on 11-Jan-16.
 */
public class AssignmentSiteVolumeFactory implements SiteVolumeFactory {

    private static final Log LOG = LogFactory.getLog(AssignmentSiteVolumeFactory.class);
    private AssignmentService assignmentService;
    private UserDirectoryService userDirectoryService;

    public void setAssignmentService(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
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
                        LOG.warn("Unexpected IdUnusedException for assignment in " + e.getClass().getName() + ": " + e.getMessage());
                    } catch (PermissionException e) {
                        LOG.warn("Unexpected Permission Exception for assignment in  " + e.getClass().getName() + ": " + e.getMessage());
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
                Iterator assignmentIterator = assignmentService.getAssignmentsForContext(this.siteId, userDirectoryService.getCurrentUser().getId());
                long nowMs = System.currentTimeMillis();
                while(assignmentIterator.hasNext()){
                    Assignment thisAssignment = (Assignment) assignmentIterator.next();
                    Time thisAssignmentCloseTime = thisAssignment.getCloseTime();
                    boolean assignmentClosed = false;
                    if(thisAssignmentCloseTime!=null){
                        if(thisAssignmentCloseTime.getTime() < nowMs){
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
            return null;
        }
    }
}
