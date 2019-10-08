/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

 Unless required by applicable law or agr eed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/
package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;

@ManagedBean(name = "restoreAssessmentsBean", eager = true)
@SessionScoped
@Data
@Slf4j
public class RestoreAssessmentsBean implements Serializable {

    List<DataAssessment> deletedAssessmentList;

    public void init() {
        log.debug("RestoreAssessmentsBean: init()");
        String siteId = AgentFacade.getCurrentSiteId();
        deletedAssessmentList = new ArrayList<DataAssessment>();
        AssessmentService assessmentService = new AssessmentService();
        List<AssessmentData> draftDeletedAssessmentList = assessmentService.getDeletedAssessments(siteId);
        for(AssessmentData assessment : draftDeletedAssessmentList) {
            log.debug("Adding deleted assessment to the list {} - {}.", assessment.getAssessmentId(), assessment.getTitle());
            DataAssessment dataAssessment = new DataAssessment();
            dataAssessment.setId(assessment.getAssessmentId());
            dataAssessment.setTitle(assessment.getTitle());
            dataAssessment.setLastModifiedDate(assessment.getLastModifiedDate());
            dataAssessment.setDraft(true);
            dataAssessment.setSelected(false);
            deletedAssessmentList.add(dataAssessment);
        }

        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        List<PublishedAssessmentData> publishedDeletedAssessmentList = publishedAssessmentService.getPublishedDeletedAssessments(siteId);
        for(PublishedAssessmentData publishedAssessment : publishedDeletedAssessmentList) {
            log.debug("Adding deleted published assessment to the list {} - {}.", publishedAssessment.getAssessmentId(), publishedAssessment.getTitle());
            DataAssessment dataAssessment = new DataAssessment();
            dataAssessment.setId(publishedAssessment.getPublishedAssessmentId());
            dataAssessment.setTitle(publishedAssessment.getTitle());
            dataAssessment.setLastModifiedDate(publishedAssessment.getLastModifiedDate());
            dataAssessment.setDraft(false);
            dataAssessment.setSelected(false);
            deletedAssessmentList.add(dataAssessment);
        }

        log.debug("RestoreAssessmentsBean: End of init()");
    }

    public String restoreAssessments() {
        log.debug("RestoreAssessmentsBean: restoreAssessments()");
        AssessmentService assessmentService = new AssessmentService();
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        for(DataAssessment dataAssessment : deletedAssessmentList) {
            if(dataAssessment.isSelected()) {
                if(dataAssessment.isDraft()) {
                    assessmentService.restoreAssessment(dataAssessment.getId());
                } else {
                    publishedAssessmentService.restorePublishedAssessment(dataAssessment.getId());
                }
                log.info(dataAssessment.isDraft() ? "Restoring deleted assessment {} - {}." : "Restoring published assessment {} - {}.", dataAssessment.getId(), dataAssessment.getTitle());
            }
        }
        log.debug("RestoreAssessmentsBean: End of restoreAssessments()");
        AuthorActionListener authorActionListener = new AuthorActionListener();
        authorActionListener.processAction(null);
        return "author";
    }

    public String cancel() {
        log.debug("RestoreAssessmentsBean: cancel()");
        return "author";
    }

    @Data
    @NoArgsConstructor
    public class DataAssessment {
        Long id;
        String title;
        Date lastModifiedDate;
        boolean draft;
        boolean selected;
    }
}
