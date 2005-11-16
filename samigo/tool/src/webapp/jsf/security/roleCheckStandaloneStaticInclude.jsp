<%@ page import="org.sakaiproject.service.legacy.site.cover.SiteService,
                 org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
                 org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil,
                 org.sakaiproject.service.framework.portal.cover.PortalService"
%>
<%
  // standalone authorization, pretty basic really.
  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
                         "authorization");
  System.out.println("***** standalone roleCheck: authzBean="+authzBean);
  System.out.println(
    "***** standalone roleCheck: authzBean.getAuthzMap().size()==0=" +
    authzBean.getAuthzMap().size()==0);
  // in general
  if (authzBean.getAuthzMap().size()==0){
    authzBean.addAllPrivilege(PortalService.getCurrentSiteId());
  }
  //  authzBean.addAllPrivilege(PortalService.getCurrentSiteId()) ;
%>