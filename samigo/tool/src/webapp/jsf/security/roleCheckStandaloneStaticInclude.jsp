<%@ page import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
                 org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
                 org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
                 org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil"
%>
<%
  // standalone authorization, pretty basic really.
  AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean(
                         "authorization");

  // debugging code
  System.out.println("***** standalone roleCheck: authzBean="+authzBean);
  System.out.println(
    "***** standalone roleCheck: authzBean.getAuthzMap().size()==0=" +
    authzBean.getAuthzMap().size()==0);

  // in general, probably will be empty, but we provide for this
  // possibility
  if (authzBean.getAuthzMap().size()==0){
    authzBean.addAllPrivilege("Samigo Site");
  }
%>