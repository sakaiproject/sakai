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
    /><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
</jsp:text>
  
	<c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
	<c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
	
	<c:if test="${!permissionsBean.updateAllowed}">
	<jsp:scriptlet>
		if ( true ) {
		    uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean rlb = 
		       uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ResourceLoaderHelperBean.getResourceLoaderBean();
			throw new uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException(rlb.getString("jsp_not_allowed_edit_page"));
		}
	</jsp:scriptlet>
	</c:if>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <div class="rwiki_help_popup" >
	    <form action="?#" method="post">
	    <!--.AJAX based edit.-->
	    <!--<form action="?#" method="post" onsubmit="ajaxRefPopupPost(this,'?#',2,this); return false;" >-->
	    <nobr><c:out value="${rlb.jsp_edit_comment}"/></nobr><br/>
		<textarea cols="40" rows="10" name="content" id="wiki-textarea-content" ><c:out value="${currentRWikiObject.content}"/></textarea>
		<input type="hidden" name="action" value="commenteditsave"/>
		<input type="hidden" name="panel" value="Main"/>
		<input type="hidden" name="version" value="${currentRWikiObject.version.time}"/>
		<jsp:element name="input">
            <jsp:attribute name="type">hidden</jsp:attribute>
            <jsp:attribute name="name">pageName</jsp:attribute>
            <jsp:attribute name="value"><c:out value="${currentRWikiObject.name}" escapeXml="true"/></jsp:attribute>
        </jsp:element>
        <jsp:element name="input">
            <jsp:attribute name="type">hidden</jsp:attribute>
            <jsp:attribute name="name">realm</jsp:attribute>
            <jsp:attribute name="value"><c:out value="${currentRWikiObject.realm}" escapeXml="true"/></jsp:attribute>
        </jsp:element>
		
		<br/>
		<nobr>
		<span class="act">
			<jsp:element name="input">
				<jsp:attribute name="type">submit</jsp:attribute> 
				<jsp:attribute name="name">save</jsp:attribute>
				<jsp:attribute name="value"><c:out value="${rlb.jsp_button_save}" /></jsp:attribute>
			</jsp:element>
			<jsp:element name="input">
				<jsp:attribute name="type">button</jsp:attribute> 
				<jsp:attribute name="onclick">popupClose(-1);</jsp:attribute>
				<jsp:attribute name="value"><c:out value="${rlb.jsp_button_cancel}" /></jsp:attribute>
			</jsp:element>
		</span>
		</nobr>
	    </form>
	</div>
</jsp:root>
