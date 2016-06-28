/**
 * Copyright (c) 2016 Apereo Foundation
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
package org.sakaiproject.elfinder.sakai.samigo;

import cn.bluejoe.elfinder.service.FsItem;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.elfinder.sakai.ReadOnlyFsVolume;
import org.sakaiproject.elfinder.sakai.SiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.SakaiFsService;
import org.sakaiproject.elfinder.sakai.SiteVolume;
import org.sakaiproject.elfinder.sakai.site.SiteFsItem;
import org.sakaiproject.elfinder.sakai.site.SiteFsVolume;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ddelblanco on 03/16.
 */
public class SamSiteVolumeFactory implements SiteVolumeFactory {


    private static final Log LOG = LogFactory.getLog(SamSiteVolumeFactory.class);
    private PublishedAssessmentService publishedAssessmentService;
    private ServerConfigurationService serverConfigurationService;

    public void setPublishedAssessmentService(PublishedAssessmentService publishedAssessmentService) {
        this.publishedAssessmentService = publishedAssessmentService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    @Override
    public String getPrefix() {
        return "samigo";
    }

    @Override
    public SiteVolume getVolume(SakaiFsService sakaiFsService, String siteId) {
        return new SamSiteVolume(sakaiFsService, siteId);
    }

    @Override
    public String getToolId() {
        return "sakai.samigo";
    }

    public class SamSiteVolume extends ReadOnlyFsVolume implements SiteVolume {
        private SakaiFsService service;
        private String siteId;

        public SamSiteVolume(SakaiFsService service, String siteId) {
            this.service = service;
            this.siteId = siteId;
        }

        public String getSiteId() {
            return this.siteId;
        }

        @Override
        public SiteVolumeFactory getSiteVolumeFactory() {
            return SamSiteVolumeFactory.this;
        }

        public boolean exists(FsItem newFile) {
            return false;
        }

        public FsItem fromPath(String relativePath) {
            
            LOG.debug("relativePath=" + relativePath);
            if(relativePath != null && !relativePath.isEmpty()){
                String[] parts = relativePath.split("/");
                if(parts.length == 2 && (parts[0].equals(siteId))){
                    LOG.debug("parts[1]=" + parts[1]);
                    LOG.debug("parts[0]=" + parts[0]);
                    PublishedAssessmentFacade test = publishedAssessmentService.getPublishedAssessment(parts[1]);
                        return new SamFsItem(test.getPublishedAssessmentId().toString(), this);
                }
            }
            return this.getRoot();
        }

        public String getPath(FsItem fsi) throws IOException {
            if(this.getRoot().equals(fsi)) {
                LOG.debug("getPath returns siteId" + ((SamFsItem)fsi).getId());
                return "/samigo/" +siteId;
            } else if(fsi instanceof SamFsItem) {

                SamFsItem samFsItem1 = (SamFsItem)fsi;
                PublishedAssessmentFacade assessment = samFsItem1.getAssessment();
                String alias = assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
                LOG.debug("getPath returns =" + "/samigo/" +siteId + "/" + alias);
                return "/samigo/" +siteId + "/" + alias;

            } else {
                throw new IllegalArgumentException("Wrong type: " + fsi);
            }
        }

        public String getDimensions(FsItem fsi) {
            return null;
        }

        public long getLastModified(FsItem fsi) {
            return 0L;
        }

        public String getMimeType(FsItem fsi) {
            return this.isFolder(fsi)?"directory":"sakai/assessments";
        }

        public String getName() {
            return null;
        }

        public String getName(FsItem fsi) {
            if(this.getRoot().equals(fsi)) {
                //TO DO: I18n
                return "Test & Quizzes";
            } else if(fsi instanceof SamFsItem) {
                SamFsItem samFsItem1 = (SamFsItem)fsi;
                PublishedAssessmentFacade assessment = (PublishedAssessmentFacade)samFsItem1.getAssessment();
                return assessment.getTitle();
            } else {
                throw new IllegalArgumentException("Could not get title for: " + fsi.toString());
            }
        }

        public FsItem getParent(FsItem fsi) {
            if(this.getRoot().equals(fsi)) {
                return service.getSiteVolume(siteId).getRoot();
            } else {
                return this.getRoot();
            }
        }

        public FsItem getRoot() {
            return new SamFsItem("", this);
        }

        public long getSize(FsItem fsi) throws IOException {
            return 0L;
        }

        public String getThumbnailFileName(FsItem fsi) {
            return null;
        }

        public boolean hasChildFolder(FsItem fsi) {
                return false;
        }

        public boolean isFolder(FsItem fsi) {
            if(fsi instanceof SamFsItem && ((SamFsItem)fsi).getId().equals("")){
                return true;
            }else{
                return false;
            }
        }

        public boolean isRoot(FsItem fsi) {
            return false;
        }

        public FsItem[] listChildren(FsItem fsi) {
            List<FsItem> items = new ArrayList<>();
            if(this.getRoot().equals(fsi)) {
                //GET SAMIGO LIST
                List tests = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments("","title",true,this.siteId);
                Iterator testsIterator = tests.iterator();
                while(testsIterator.hasNext()) {
                    PublishedAssessmentFacade pubAssessment = (PublishedAssessmentFacade)testsIterator.next();
                    //we need the FULL data in the assessment, not only the basic info
                    pubAssessment = publishedAssessmentService.getPublishedAssessment(pubAssessment.getPublishedAssessmentId().toString());

                    // TO DO: At this moment getBasicInfoOfAllPublishedAssessments returns
                    // the ones "takeable" for the user (or all for the instructors roles)
                    // Maybe we don't want the students to know the links to the
                    // assessments. Of course they can't access to assessments that
                    // they don't have permissions, so it is not a security problem
                    // but maybe in the future we want to filter this list by the role or other
                    // parameters
                    
                    SamFsItem test = new SamFsItem(pubAssessment, pubAssessment.getPublishedAssessmentId().toString(), this);
                    LOG.debug("listing children " + test.getId() );
                    items.add(test);
                }
            }else if(fsi instanceof SamFsItem){
                                items.add(fsi);
            }
            return items.toArray(new FsItem[0]);
        }


        public InputStream openInputStream(FsItem fsi) throws IOException {
            return null;
        }

        public String getURL(FsItem f) {
            String serverUrlPrefix = serverConfigurationService.getServerUrl();
            if(f instanceof SamFsItem) {
                SamFsItem samFsItem1 = (SamFsItem)f;
                if (!(samFsItem1.getId().equals(""))) {
                    PublishedAssessmentFacade pubAssessment = samFsItem1.getAssessment();
                    String alias = pubAssessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.ALIAS);
                    return serverUrlPrefix + "/samigo-app/servlet/Login?id=" + alias;
                }else{
                    return null;
                }
            }else{
                return null;
            }
        }

        public boolean isWriteable(FsItem fsi) {
            return false;
        }
    }
}
