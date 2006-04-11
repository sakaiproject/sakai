/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    log.debug("*** LOADING INTERNAL API ***");
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

    // assessment grading facade
    AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries = PersistenceService.getInstance().getAssessmentGradingFacadeQueries();
    log.debug("*****#8  InitAction: assessmentGradingFacadeQueries ="+assessmentGradingFacadeQueries);

    // authorization facade
    AuthorizationFacadeQueriesAPI authorizationFacadeQueries = PersistenceService.getInstance().getAuthorizationFacadeQueries();
    log.debug("*****#9  InitAction: authorizationFacadeQueries ="+authorizationFacadeQueries);

    // PagingUtil
    PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
    log.debug("*****#10  InitAction: pagingUtilQueries ="+pagingUtilQueries);

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

  }
}
