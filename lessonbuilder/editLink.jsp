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
<%@ page import="org.sakaiproject.site.api.Site" %>
<%@ page import="org.sakaiproject.site.api.ToolConfiguration" %>
<%@ page import="org.sakaiproject.site.cover.SiteService" %>
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


	Site site = null;
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	} catch (Exception impossible) {
	    out.println("Can't find site");
	    return;
	}

	 ToolConfiguration tool = site.getToolForCommonId("sakai.samigo");

         AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBeanFromExternalServlet(
                                                                         "authorization", request, response);
         if (authzBean.getAuthzMap().size()==0){
	     authzBean.addAllPrivilege(site.getId());
         }

	 // context for main assessment list, in case they cancel out of the specific action
         AuthorActionListener authorActionListener = new AuthorActionListener();
         authorActionListener.processAction(null);

	 // requested action
         if (ContextUtil.lookupParam("settings") != null) {
            EditPublishedSettingsListener editPublishedSettingsListener = new EditPublishedSettingsListener();
            editPublishedSettingsListener.processAction(null);
            response.sendRedirect("/portal/tool/" + tool.getId() + "/jsf/author/publishedSettings");
         } else {
            ConfirmEditPublishedAssessmentListener confirmEditPublishedAssessmentListener = new ConfirmEditPublishedAssessmentListener();
            confirmEditPublishedAssessmentListener.processAction(null);
            response.sendRedirect("/portal/tool/" + tool.getId() + "/jsf/author/confirmEditPublishedAssessment");
         }

%>

