<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmEditPublishedAssessmentListener"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.services.PersistenceService"/>
  <jsp:directive.page import="org.sakaiproject.tool.cover.ToolManager"/>
  <jsp:directive.page import="org.sakaiproject.site.api.Site"/>
  <jsp:directive.page import="org.sakaiproject.site.api.ToolConfiguration"/>
  <jsp:directive.page import="org.sakaiproject.site.cover.SiteService"/>
  <jsp:directive.page import="jakarta.faces.context.FacesContext"/>
  <jsp:scriptlet>

         DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBeanFromExternalServlet("delivery", request, response, application);
         PersonBean person = (PersonBean) ContextUtil.lookupBeanFromExternalServlet("person", request, response, application);
         AuthorBean author = (AuthorBean) ContextUtil.lookupBeanFromExternalServlet("author", request, response, application);
         author.setIsEditPendingAssessmentFlow(false);
         String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
         AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBeanFromExternalServlet(
                "assessmentBean", request, response, application);
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
                                                                         "authorization", request, response, application);
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

</jsp:scriptlet>
</jsp:root>
