/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.web.action;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.SectionFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.authz.AuthorizationFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SectionServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.MediaServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;

public class InitAction extends HttpServlet{

  private static Log log = LogFactory.getLog(InitAction.class);

  public void init(){
    // store all types in memory
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    log.info("*****#1 InitAction: "+PersistenceService.getInstance());
    log.info("*****#2 InitAction: typeFacadeQueries ="+typeFacadeQueries);
    if ( typeFacadeQueries != null ){
      typeFacadeQueries.setTypeFacadeMap();
      typeFacadeQueries.setFacadeItemTypes();
    }

    // questionpool facade
    QuestionPoolFacadeQueriesAPI questionpoolFacadeQueries = PersistenceService.getInstance().getQuestionPoolFacadeQueries();
    log.info("*****#3  InitAction: questionpoolFacadeQueries ="+questionpoolFacadeQueries);

    // assessment facade
    AssessmentFacadeQueriesAPI assessmentFacadeQueries = PersistenceService.getInstance().getAssessmentFacadeQueries();
    log.info("*****#4  InitAction: assessmentFacadeQueries ="+assessmentFacadeQueries);

    // item facade
    ItemFacadeQueriesAPI itemFacadeQueries = PersistenceService.getInstance().getItemFacadeQueries();
    log.info("*****#5  InitAction: itemFacadeQueries ="+itemFacadeQueries);

    // section facade
    SectionFacadeQueriesAPI sectionFacadeQueries = PersistenceService.getInstance().getSectionFacadeQueries();
    log.info("*****#6  InitAction: sectionFacadeQueries ="+sectionFacadeQueries);

    // published assessment facade
    PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries();
    log.info("*****#7  InitAction: publishedAssessmentFacadeQueries ="+publishedAssessmentFacadeQueries);

    // assessment grading facade
    AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries = PersistenceService.getInstance().getAssessmentGradingFacadeQueries();
    log.info("*****#8  InitAction: assessmentGradingFacadeQueries ="+assessmentGradingFacadeQueries);

    // authorization facade
    AuthorizationFacadeQueriesAPI authorizationFacadeQueries = PersistenceService.getInstance().getAuthorizationFacadeQueries();
    log.info("*****#9  InitAction: authorizationFacadeQueries ="+authorizationFacadeQueries);

    // PagingUtil
    PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
    log.info("*****#10  InitAction: pagingUtilQueries ="+pagingUtilQueries);

    // authorization facade
    AuthzQueriesFacadeAPI authzQueriesFacade = PersistenceService.getInstance().getAuthzQueriesFacade();
    log.info("*****#11  InitAction: authzQueriesFacade ="+authzQueriesFacade);

    log.info("*** LOADING EXTERNAL API ***");
    log.info("*****#12  InitAction: SamigoApiFactory.getInstance()=" + SamigoApiFactory.getInstance());

    AssessmentServiceAPI assessmentServiceAPI = SamigoApiFactory.getInstance().getAssessmentServiceAPI();
    log.info("AssessmentServiceAPI: " + assessmentServiceAPI);

    GradebookServiceAPI gradebookServiceAPI = SamigoApiFactory.getInstance().getGradebookServiceAPI();
    log.info("GradebookServiceAPI: " + gradebookServiceAPI);

    GradingServiceAPI gradingServiceAPI = SamigoApiFactory.getInstance().getGradingServiceAPI();
    log.info("gradingServiceAPI: " + gradingServiceAPI);

    ItemServiceAPI itemServiceAPI = SamigoApiFactory.getInstance().getItemServiceAPI();
    log.info("ItemServiceAPI: " + itemServiceAPI);

    MediaServiceAPI mediaServiceAPI = SamigoApiFactory.getInstance().getMediaServiceAPI();
    log.info("MediaServiceAPI: " + mediaServiceAPI);

    PublishedAssessmentServiceAPI publishedAssessmentServiceAPI = SamigoApiFactory.getInstance().getPublishedAssessmentServiceAPI();
    log.info("PublishedAssessmentServiceAPI: " + publishedAssessmentServiceAPI);

    QTIServiceAPI qtiServiceAPI = SamigoApiFactory.getInstance().getQtiServiceAPI();
    log.info("QtiServiceAPI: " + qtiServiceAPI);

    QuestionPoolServiceAPI questionPoolServiceAPI = SamigoApiFactory.getInstance().getQuestionPoolServiceAPI();
    log.info("QuestionPoolServiceAPI: " + questionPoolServiceAPI);

    SectionServiceAPI sectionServiceAPI = SamigoApiFactory.getInstance().getSectionServiceAPI();
    log.info("SectionServiceAPI: " + sectionServiceAPI);

    TypeServiceAPI typeServiceAPI = SamigoApiFactory.getInstance().getTypeServiceAPI();
    log.info("TypeServiceAPI: " + typeServiceAPI);

  }
}
