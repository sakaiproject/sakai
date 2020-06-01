/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener;
import org.sakaiproject.util.api.FormattedText;


@ManagedBean(name = "restoreAssessmentsBean", eager = true)
@SessionScoped
@Data
@Slf4j
public class RestoreAssessmentsBean implements Serializable {

    private static final GradebookServiceHelper gbsHelper = IntegrationContextFactory.getInstance().getGradebookServiceHelper();
    private static final boolean integrated = IntegrationContextFactory.getInstance().isIntegrated();

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
            dataAssessment.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(assessment.getTitle()));
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
            dataAssessment.setTitle(ComponentManager.get(FormattedText.class).convertFormattedTextToPlaintext(publishedAssessment.getTitle()));
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
                    updateGB(dataAssessment.getId());
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

    private void updateGB(Long id) {
        try {
            GradebookExternalAssessmentService g = null;
            if (integrated) {
                g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().getBean("org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
            }
            if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)) {
                PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
                PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(String.valueOf(id));
                PublishedEvaluationModel evaluation = (PublishedEvaluationModel) assessment.getEvaluationModel();
                if (evaluation == null) {
                    evaluation = new PublishedEvaluationModel();
                    evaluation.setAssessmentBase(assessment.getData());
                }
                if (evaluation.getToGradeBook() != null	&& evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {
                    gbsHelper.addToGradebook((PublishedAssessmentData) assessment.getData(), assessment.getData().getCategoryId(), g);
                }
            }
        } catch (Exception e1) {
            log.warn("RestoreAssessmentsBean - Exception thrown in updateGB():" + e1.getMessage());
        }
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
