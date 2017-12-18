/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.ItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.FavoriteColChoicesFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedItemFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedSectionFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.SectionFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.TypeFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.authz.AuthorizationFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.util.PagingUtilQueriesAPI;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer;
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

@Slf4j
public class InitAction extends HttpServlet{

  /**
	 * 
	 */
	private static final long serialVersionUID = 8101462284850616249L;

  public void init(){
    // store all types in memory
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    log.debug("*****#1 InitAction: "+PersistenceService.getInstance());
    log.debug("*****#2 InitAction: typeFacadeQueries ="+typeFacadeQueries);
    if ( typeFacadeQueries != null ){
      typeFacadeQueries.setTypeFacadeMap();
      typeFacadeQueries.setFacadeItemTypes();
    }

    // questionpool facade
    QuestionPoolFacadeQueriesAPI questionpoolFacadeQueries = PersistenceService.getInstance().getQuestionPoolFacadeQueries();
    log.debug("*****#3  InitAction: questionpoolFacadeQueries ="+questionpoolFacadeQueries);

    // assessment facade
    AssessmentFacadeQueriesAPI assessmentFacadeQueries = PersistenceService.getInstance().getAssessmentFacadeQueries();
    log.debug("*****#4  InitAction: assessmentFacadeQueries ="+assessmentFacadeQueries);

    // item facade
    ItemFacadeQueriesAPI itemFacadeQueries = PersistenceService.getInstance().getItemFacadeQueries();
    log.debug("*****#5  InitAction: itemFacadeQueries ="+itemFacadeQueries);

    // section facade
    SectionFacadeQueriesAPI sectionFacadeQueries = PersistenceService.getInstance().getSectionFacadeQueries();
    log.debug("*****#6  InitAction: sectionFacadeQueries ="+sectionFacadeQueries);

    // published assessment facade
    PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries();
    log.debug("*****#7  InitAction: publishedAssessmentFacadeQueries ="+publishedAssessmentFacadeQueries);

    // published item facade
    PublishedItemFacadeQueriesAPI publishedItemFacadeQueries = PersistenceService.getInstance().getPublishedItemFacadeQueries();
    log.debug("*****#5  InitAction: publishedItemFacadeQueries ="+publishedItemFacadeQueries);

    // published section facade
    PublishedSectionFacadeQueriesAPI publishedSectionFacadeQueries = PersistenceService.getInstance().getPublishedSectionFacadeQueries();
    log.debug("*****#6  InitAction: publishedSectionFacadeQueries ="+publishedSectionFacadeQueries);

    // assessment grading facade
    AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries = PersistenceService.getInstance().getAssessmentGradingFacadeQueries();
    log.debug("*****#8  InitAction: assessmentGradingFacadeQueries ="+assessmentGradingFacadeQueries);

    // authorization facade
    AuthorizationFacadeQueriesAPI authorizationFacadeQueries = PersistenceService.getInstance().getAuthorizationFacadeQueries();
    log.debug("*****#9  InitAction: authorizationFacadeQueries ="+authorizationFacadeQueries);

    // PagingUtil
    PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
    log.debug("*****#10  InitAction: pagingUtilQueries ="+pagingUtilQueries);

    FavoriteColChoicesFacadeQueriesAPI favoriteColChoicesQueries = PersistenceService.getInstance().getFavoriteColChoicesFacadeQueries();
    log.debug("*****#13  InitAction: favoriteColChoicesQueries ="+favoriteColChoicesQueries);

    // authorization facade
    AuthzQueriesFacadeAPI authzQueriesFacade = PersistenceService.getInstance().getAuthzQueriesFacade();
    log.debug("*****#11  InitAction: authzQueriesFacade ="+authzQueriesFacade);

    log.debug("*** LOADING EXTERNAL API ***");
    log.debug("*****#12  InitAction: SamigoApiFactory.getInstance()=" + SamigoApiFactory.getInstance());

    AssessmentServiceAPI assessmentServiceAPI = SamigoApiFactory.getInstance().getAssessmentServiceAPI();
    log.debug("AssessmentServiceAPI: " + assessmentServiceAPI);

    GradebookServiceAPI gradebookServiceAPI = SamigoApiFactory.getInstance().getGradebookServiceAPI();
    log.debug("GradebookServiceAPI: " + gradebookServiceAPI);

    GradingServiceAPI gradingServiceAPI = SamigoApiFactory.getInstance().getGradingServiceAPI();
    log.debug("gradingServiceAPI: " + gradingServiceAPI);

    ItemServiceAPI itemServiceAPI = SamigoApiFactory.getInstance().getItemServiceAPI();
    log.debug("ItemServiceAPI: " + itemServiceAPI);

    MediaServiceAPI mediaServiceAPI = SamigoApiFactory.getInstance().getMediaServiceAPI();
    log.debug("MediaServiceAPI: " + mediaServiceAPI);

    PublishedAssessmentServiceAPI publishedAssessmentServiceAPI = SamigoApiFactory.getInstance().getPublishedAssessmentServiceAPI();
    log.debug("PublishedAssessmentServiceAPI: " + publishedAssessmentServiceAPI);

    QTIServiceAPI qtiServiceAPI = SamigoApiFactory.getInstance().getQtiServiceAPI();
    log.debug("QtiServiceAPI: " + qtiServiceAPI);

    QuestionPoolServiceAPI questionPoolServiceAPI = SamigoApiFactory.getInstance().getQuestionPoolServiceAPI();
    log.debug("QuestionPoolServiceAPI: " + questionPoolServiceAPI);

    SectionServiceAPI sectionServiceAPI = SamigoApiFactory.getInstance().getSectionServiceAPI();
    log.debug("SectionServiceAPI: " + sectionServiceAPI);

    TypeServiceAPI typeServiceAPI = SamigoApiFactory.getInstance().getTypeServiceAPI();
    log.debug("TypeServiceAPI: " + typeServiceAPI);

    AssessmentEntityProducer producer = (AssessmentEntityProducer) ComponentManager.get("org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer");
    log.debug("AssessmentEntityProducer: "+producer);
  }
}
