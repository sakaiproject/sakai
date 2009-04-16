<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	/><jsp:text
	><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="rightRenderBean"
    value="${requestScope.rsacMap.infoRightRenderBean}" />
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="realmEditBean" value="${requestScope.rsacMap.realmEditBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_edit_section}"/>: <c:out value="${realmEditBean.localSpace}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
	<div class="portletBody">

	  <!-- Main page -->
	  <div id="rwiki_content" class="nosiderbar" >

	    <h3><c:out value="${rlb.jsp_edit_section}"/>: <c:out value="${realmEditBean.localSpace}" /></h3>
	    <c:if test="${fn:length(errorBean.errors) gt 0}">
	      <!-- XXX This is hideous -->
	      <p class="validation" style="clear: none;">
		<c:forEach var="error" items="${errorBean.errors}">
		  <c:out value="${error}"/><br/>
		</c:forEach>
	      </p>
	    </c:if>
	      <div class="rwikirenderedContent">
		<p><c:out value="${rlb.jsp_section_being_edited}"/></p>
		<p><c:out value="${rlb.jsp_section_being_edited_msg1}"/>
		<a href="${realmEditBean.infoUrl}">
		<c:out value="${rlb.jsp_section_being_edited_msg2}"/>
		</a> 
		<c:out value="${rlb.jsp_section_being_edited_msg3}"/> 
		<a href="${realmEditBean.realmEditUrl}">
		<c:out value="${rlb.jsp_section_being_edited_msg4}"/>
		</a>.
		</p>
	      </div>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
