<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" 
	import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
		org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,
		org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,
        org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     
	<%
		AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		boolean adminPrivilege = authorization.getAdminPrivilege();
		
		if (adminPrivilege) 
		{
			AuthorActionListener authorlistener = new AuthorActionListener();
			authorlistener.processAction(null);
		} 
		else 
		{
			SelectActionListener listener = new SelectActionListener();
			listener.processAction(null);
		}
	%>
   
   <f:view>      
		<h:panelGroup rendered="#{authorization.adminPrivilege}">
			<%@ include file="../author/authorIndex_content.jsp"%>
		</h:panelGroup>
		
		<h:panelGroup rendered="#{!authorization.adminPrivilege}">
			<%@ include file="../select/selectIndex_content.jsp"%>
		</h:panelGroup>
  </f:view>
