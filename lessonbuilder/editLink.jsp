<%@ page import="org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmEditPublishedAssessmentListener" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" %>
<%@ page import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" %>
<%@ page import="org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI" %>
<%@ page import="org.sakaiproject.tool.assessment.services.PersistenceService" %>
<%@ page import="org.sakaiproject.tool.cover.ToolManager" %>
<%@ page import="javax.faces.context.FacesContext" %>

<%

         DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", request, response);
         PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet("person", request, response);
         AuthorBean author = (AuthorBean) ContextUtil.lookupBeanFromExternalServlet("author", request, response);
         author.setIsEditPendingAssessmentFlow(false);
         String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
         AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBeanFromExternalServlet(
                "assessmentBean", request, response);
         assessmentBean.setAssessmentId(publishedAssessmentId);

         AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBeanFromExternalServlet(
                                                                         "authorization", request, response);
         if (authzBean.getAuthzMap().size()==0){
           authzBean.addAllPrivilege("45d48248-ba23-4829-914a-7219c3ced2dd");
         }

	 // context for main assessment list, in case they cancel out of the specific action
         AuthorActionListener authorActionListener = new AuthorActionListener();
         authorActionListener.processAction(null);

	 // requested action
         if (ContextUtil.lookupParam("settings") != null) {
            EditPublishedSettingsListener editPublishedSettingsListener = new EditPublishedSettingsListener();
            editPublishedSettingsListener.processAction(null);
            response.sendRedirect("/portal/tool/319adb61-d321-4665-9dec-8d905ec13cbb/jsf/author/publishedSettings");
         } else {
            ConfirmEditPublishedAssessmentListener confirmEditPublishedAssessmentListener = new ConfirmEditPublishedAssessmentListener();
            confirmEditPublishedAssessmentListener.processAction(null);
            response.sendRedirect("/portal/tool/319adb61-d321-4665-9dec-8d905ec13cbb/jsf/author/confirmEditPublishedAssessment");
         }

//	 AuthzQueriesFacadeAPI authz = PersistenceService.getInstance()
//                                .getAuthzQueriesFacade();
//         out.println(" authz " + authz);
//         Object authorizations = authz.getAuthorizationByFunctionAndQualifier(
//                                "TAKE_PUBLISHED_ASSESSMENT", "22");
//         out.println(" auth " + authorizations);

// https://heidelberg.rutgers.edu/portal/tool/319adb61-d321-4665-9dec-8d905ec13cbb/jsf/index/test?publishedAssessmentId=22


// confirmEditPublishedAssessment

// https://heidelberg.rutgers.edu/portal/tool/3f7ffde4-bee1-4dcf-b03d-1f64c1d9700d/assessment_settings/0A/1
// https://heidelberg.rutgers.edu/portal/tool/3f7ffde4-bee1-4dcf-b03d-1f64c1d9700d/assessment_edit/0A/1

// https://heidelberg.rutgers.edu/portal/tool/83af90fc-f534-426a-b585-27a825bdf9cc?assignmentId=/assignment/a/45d48248-ba23-4829-914a-7219c3ced2dd/88f77778-560f-4b27-83d8-117112c13f4b&panel=Main&sakai_action=doEdit_assignment

%>

