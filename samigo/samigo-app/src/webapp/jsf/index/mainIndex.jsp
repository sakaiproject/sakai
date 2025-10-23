<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8"
    import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
            org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
            org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
            org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil" %>

<%
    AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    boolean adminPrivilege = authorization.getAdminPrivilege();

    if (adminPrivilege) {
        AuthorActionListener authorListener = new AuthorActionListener();
        authorListener.processAction(null);
%>
<jsp:forward page="/jsf/author/authorIndex_container.xhtml" />
<%
    } else {
        SelectActionListener selectListener = new SelectActionListener();
        selectListener.processAction(null);
%>
<jsp:forward page="/jsf/select/selectIndex_container.xhtml" />
<%
    }
%>
