<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" 
	import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,
		org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     
   <f:view>      
		<h:panelGroup rendered="#{authorization.adminPrivilege}">
			<%
				AuthorActionListener authorListener = new AuthorActionListener();
				authorListener.processAction(null);
			%>
			<%@ include file="../author/authorIndex_content.jsp"%>
		</h:panelGroup>
		
		<h:panelGroup rendered="#{!authorization.adminPrivilege}">
			<%
				SelectActionListener listener = new SelectActionListener();
				listener.processAction(null);
			%>
			<%@ include file="../select/selectIndex_content.jsp"%>
		</h:panelGroup>
  </f:view>
