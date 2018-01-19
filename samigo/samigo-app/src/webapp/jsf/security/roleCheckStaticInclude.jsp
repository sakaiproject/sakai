<%@ page import="org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
                 org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil,
                 org.sakaiproject.tool.cover.ToolManager"
%>
<%
  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
                         "authorization");
  //if (authzBean.getAuthzMap().size()==0){ 
    authzBean.addAllPrivilege(ToolManager.getCurrentPlacement().getContext());
  //}
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
