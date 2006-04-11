<%@ page import="org.sakaiproject.service.legacy.site.cover.SiteService,
                 org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
                 org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil,
                 org.sakaiproject.service.framework.portal.cover.PortalService"
%>
<%
  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
                         "authorization");
  System.out.println("***** roleCheck: authzBean="+authzBean);
  if (authzBean.getAuthzMap().size()==0){ 
    authzBean.addAllPrivilege(PortalService.getCurrentSiteId());
  }
  boolean adminPrivilege = authzBean.getAdminPrivilege();

  if (!adminPrivilege)
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