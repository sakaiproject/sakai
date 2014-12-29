<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************/
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:c="http://java.sun.com/jsp/jstl/core" version="2.0">
  <jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
  <c:set var="permissionsBeanRight" value="${requestScope.rsacMap.permissionsBean}"/>
  <c:set var="permissionsBeanObject" value="${permissionsBeanRight.rwikiObject}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  <c:if test="${rightRenderBean.hasContent}" >
  <div style="display: block;" id="rwiki_sidebar">
    <div class="rwiki_renderedContent">
      <c:out value="${rightRenderBean.renderedPage}" escapeXml="false"/>
    </div>
  	<c:set target="${permissionsBeanRight}" property="rwikiObject" value="${rightRenderBean.rwikiObject}"/>
  	<c:if test="${permissionsBeanRight.updateAllowed}">
    	<span class="instruction" >
    	<jsp:element name="a">
    		<jsp:attribute name="href"><c:out value="${rightRenderBean.editUrl}"/></jsp:attribute>
    		<jsp:body>
    			<c:out value="${rlb.jsp_edit}"/>: <c:out value="${rightRenderBean.localisedPageName}"/>
    		</jsp:body>
    	</jsp:element>
    	</span> 
  	</c:if>
  	<c:set target="${permissionsBeanRight}" property="rwikiObject" value="${permissionsBeanObject}"/>
  </div>
  </c:if>
</jsp:root>
