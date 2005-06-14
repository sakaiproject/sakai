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
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.util.PagingUtilQueriesAPI;

public class InitAction extends HttpServlet{

  private static Log log = LogFactory.getLog(InitAction.class);

  public void init(){
    // store all types in memory
    TypeFacadeQueriesAPI typeFacadeQueries = PersistenceService.getInstance().getTypeFacadeQueries();
    log.info("*****#2 InitAction: typeFacadeQueries ="+typeFacadeQueries);
    if ( typeFacadeQueries != null ){
      typeFacadeQueries.setTypeFacadeMap();
      typeFacadeQueries.setFacadeItemTypes();
    }

    // questionpool facde testing
    QuestionPoolFacadeQueriesAPI questionpoolFacadeQueries = PersistenceService.getInstance().getQuestionPoolFacadeQueries();
    log.info("*****#3  InitAction: questionpoolFacadeQueries ="+questionpoolFacadeQueries);

    // assessment facade testing
    AssessmentFacadeQueriesAPI assessmentFacadeQueries = PersistenceService.getInstance().getAssessmentFacadeQueries();
    log.info("*****#4  InitAction: assessmentFacadeQueries ="+assessmentFacadeQueries);

    // item facade testing
    ItemFacadeQueriesAPI itemFacadeQueries = PersistenceService.getInstance().getItemFacadeQueries();
    log.info("*****#5  InitAction: itemFacadeQueries ="+itemFacadeQueries);

    // section facade testing
    SectionFacadeQueriesAPI sectionFacadeQueries = PersistenceService.getInstance().getSectionFacadeQueries();
    log.info("*****#6  InitAction: sectionFacadeQueries ="+sectionFacadeQueries);

    // published assessment facade testing
    PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries();
    log.info("*****#7  InitAction: publishedAssessmentFacadeQueries ="+publishedAssessmentFacadeQueries);

    // assessment grading facade testing
    AssessmentGradingFacadeQueriesAPI assessmentGradingFacadeQueries = PersistenceService.getInstance().getAssessmentGradingFacadeQueries();
    log.info("*****#8  InitAction: assessmentGradingFacadeQueries ="+assessmentGradingFacadeQueries);

    // authorization facade testing
    AuthorizationFacadeQueriesAPI authorizationFacadeQueries = PersistenceService.getInstance().getAuthorizationFacadeQueries();
    log.info("*****#9  InitAction: authorizationFacadeQueries ="+authorizationFacadeQueries);

    // PagingUtil testing
    PagingUtilQueriesAPI pagingUtilQueries = PersistenceService.getInstance().getPagingUtilQueries();
    log.info("*****#10  InitAction: pagingUtilQueries ="+pagingUtilQueries);

    // authorization facade testing
    AuthzQueriesFacadeAPI authzQueriesFacade = PersistenceService.getInstance().getAuthzQueriesFacade();
    log.info("*****#11  InitAction: authzQueriesFacade ="+authzQueriesFacade);

  }
}
