package org.sakaiproject.tool.assessment.elfinder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.elfinder.FsType;
import org.sakaiproject.elfinder.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssessmentToolFsVolumeFactory implements ToolFsVolumeFactory {

    private PublishedAssessmentService publishedAssessmentService;
    @Setter private SakaiFsService sakaiFsService;
    @Setter private ServerConfigurationService serverConfigurationService;

    public void init() {
        publishedAssessmentService = new PublishedAssessmentService();
        sakaiFsService.registerToolVolume(this);
    }

    @Override
    public String getPrefix() {
        return FsType.ASSESSMENT.toString();
    }

    @Override
    public ToolFsVolume getVolume(String siteId) {
        return new AssessmentToolFsVolume(sakaiFsService, siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.samigo";
    }

    public class AssessmentToolFsVolume extends ReadOnlyFsVolume implements ToolFsVolume {
        public static final String ASSESSMENT_URL_PREFIX = "/samigo-app/servlet/Login?id=";
        private SakaiFsService service;
        private String siteId;

        public AssessmentToolFsVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        @Override
        public String getSiteId() {
            return siteId;
        }

        @Override
        public ToolFsVolumeFactory getToolVolumeFactory() {
            return AssessmentToolFsVolumeFactory.this;
        }

        @Override
        public boolean isWriteable(SakaiFsItem item) {
            return false;
        }

        @Override
        public boolean exists(SakaiFsItem newFile) {
            return false;
        }

        @Override
        public SakaiFsItem fromPath(String relativePath) {
            log.debug("relativePath = {}", relativePath);
            if (StringUtils.isNotBlank(relativePath)) {
                String[] parts = relativePath.split("/");
                if (parts.length == 2 && (parts[0].equals(siteId))) {
                    log.debug("parts[1]=" + parts[1]);
                    log.debug("parts[0]=" + parts[0]);
                    PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(parts[1]);
                    return new SakaiFsItem(assessment.getPublishedAssessmentId().toString(), assessment.getTitle(), this, FsType.ASSESSMENT);
                }
            }
            return this.getRoot();
        }

        @Override
        public String getDimensions(SakaiFsItem fsi) {
            return null;
        }

        @Override
        public long getLastModified(SakaiFsItem fsi) {
            return 0;
        }

        @Override
        public String getMimeType(SakaiFsItem fsi) {
            return this.isFolder(fsi) ? "directory" : "sakai/assessments";
        }

        @Override
        public String getName() {
            // TODO i18n
            return "Tests & Quizzes";
        }

        @Override
        public String getName(SakaiFsItem fsi) {
            if (this.getRoot().equals(fsi)) {
                return getName();
            } else if (FsType.ASSESSMENT.equals(fsi.getType())) {
                return fsi.getTitle();
            } else {
                throw new IllegalArgumentException("Could not get title for: " + fsi.toString());
            }
        }

        @Override
        public SakaiFsItem getParent(SakaiFsItem fsi) {
            if (this.getRoot().equals(fsi)) {
                return service.getSiteVolume(siteId).getRoot();
            } else if (FsType.ASSESSMENT.equals(fsi.getType())) {
                return this.getRoot();
            }
            return null;
        }

        @Override
        public String getPath(SakaiFsItem fsi) throws IOException {
            if(this.getRoot().equals(fsi)) {
                return "/" + getPrefix() + "/" + siteId;
            } else if (FsType.ASSESSMENT.equals(fsi.getType())) {
                PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(fsi.getId());
                String alias = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
                String path = "/" + getPrefix() + "/" + siteId + "/" + alias;
                log.debug("getPath returns = {}", path);
                return path;
            } else {
                throw new IllegalArgumentException("Wrong type: " + fsi);
            }
        }

        @Override
        public SakaiFsItem getRoot() {
            return new SakaiFsItem("", "", this, FsType.ASSESSMENT);
        }

        @Override
        public long getSize(SakaiFsItem fsi) throws IOException {
            return 0;
        }

        @Override
        public String getThumbnailFileName(SakaiFsItem fsi) {
            return null;
        }

        @Override
        public boolean hasChildFolder(SakaiFsItem fsi) {
            return false;
        }

        @Override
        public boolean isFolder(SakaiFsItem fsi) {
            return FsType.ASSESSMENT.equals(fsi.getType()) && fsi.getId().equals("");
        }

        @Override
        public boolean isRoot(SakaiFsItem fsi) {
            return false;
        }

        @Override
        public SakaiFsItem[] listChildren(SakaiFsItem fsi) throws PermissionException {
            List<SakaiFsItem> items = new ArrayList<>();
            if (this.getRoot().equals(fsi)) {
                List<PublishedAssessmentFacade> assessments = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments("", "title", true, siteId);
                for ( PublishedAssessmentFacade assessment : assessments) {
                    // we need the FULL data in the assessment, not only the basic info
                    PublishedAssessmentFacade pAssessment = publishedAssessmentService.getPublishedAssessment(assessment.getPublishedAssessmentId().toString());

                    // TO DO: At this moment getBasicInfoOfAllPublishedAssessments returns
                    // the ones "takeable" for the user (or all for the instructors roles)
                    // Maybe we don't want the students to know the links to the
                    // assessments. Of course they can't access to assessments that
                    // they don't have permissions, so it is not a security problem
                    // but maybe in the future we want to filter this list by the role or other
                    // parameters

                    SakaiFsItem fsItem = new SakaiFsItem(pAssessment.getPublishedAssessmentId().toString(), pAssessment.getTitle(), this, FsType.ASSESSMENT);
                    log.debug("listing children {}", fsItem.getId());
                    items.add(fsItem);
                }
            } else if (FsType.ASSESSMENT.equals(fsi.getType())) {
                items.add(fsi);
            }
            return items.toArray(new SakaiFsItem[0]);
        }

        @Override
        public InputStream openInputStream(SakaiFsItem fsi) throws IOException {
            return null;
        }

        @Override
        public String getURL(SakaiFsItem fsi) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if (FsType.ASSESSMENT.equals(fsi.getType()) && StringUtils.isNotBlank(fsi.getId())) {
                PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(fsi.getId());
                String alias = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
                return serverUrlPrefix + ASSESSMENT_URL_PREFIX + alias;
            }
            return null;
        }
    }
}
