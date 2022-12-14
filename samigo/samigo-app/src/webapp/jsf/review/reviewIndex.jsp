<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" 
    import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
        org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
        org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
        org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<%
    SelectActionListener listener = new SelectActionListener();
    listener.processAction(null);
%>

<f:view>
    <%@ include file="reviewIndex_content.jsp" %>
</f:view>
