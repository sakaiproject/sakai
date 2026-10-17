<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page import="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener,&#10;&#9;&#9;org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener,&#10;&#9;&#9;org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean,&#10;        org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil"/>
  <jsp:text><![CDATA[





<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
     
	]]></jsp:text>
  <jsp:scriptlet>
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
	</jsp:scriptlet>
  <f:view>
    <h:panelGroup rendered="#{authorization.adminPrivilege}">
      <jsp:directive.include file="../author/authorIndex_content.jsp"/>
    </h:panelGroup>
    <h:panelGroup rendered="#{!authorization.adminPrivilege}">
      <jsp:directive.include file="../select/selectIndex_content.jsp"/>
    </h:panelGroup>
  </f:view>
</jsp:root>
