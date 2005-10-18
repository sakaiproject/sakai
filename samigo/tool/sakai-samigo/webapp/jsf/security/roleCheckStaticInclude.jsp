<%@ page import="org.sakaiproject.service.legacy.site.cover.SiteService,
                 org.sakaiproject.spring.SpringBeanLocator,
                 org.sakaiproject.tool.assessment.services.authz.AuthorizationService,
                 org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
                 org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil,
                 org.sakaiproject.service.framework.portal.cover.PortalService"
%>
<%
  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
                         "authorization");
  AuthorizationService service = authzBean.getAuthzService();
  if (authzBean.getAuthzService()==null){ 
    service = (AuthorizationService) SpringBeanLocator.getInstance().
                                   getBean("AuthorizationService");
    authzBean.setAuthzService(service);
  }
  System.out.println("***** roleCheck: authorizationService="+service);

  if (service!=null && !service.allowAdminAssessment(PortalService.getCurrentSiteId()))
  {
          SelectActionListener listener = new SelectActionListener();
          listener.processAction(null);
	  request.getRequestDispatcher("../select/selectIndex.faces").forward(request, response);
  }    
  else
  {
     AuthorActionListener authorlistener = new AuthorActionListener();
     authorlistener.processAction(null);
     request.getRequestDispatcher("../author/authorIndex.faces").forward(request, response);
  }
%>