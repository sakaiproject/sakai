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
  xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
<div class="rwiki_comments" >
<nobr>
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.newCommentURL}" />',1); return false;</jsp:attribute>
	<jsp:body>
		<c:out value="${rlb.jsp_add_new_comment}"/>
	</jsp:body>
</jsp:element>
</nobr>
<c:forEach var="comment"
		  items="${renderBean.comments}" >
  <jsp:element name="div" >
			<jsp:attribute name="class">rwikicommentbody_<c:out value="${comment.commentLevel}" /></jsp:attribute>
		
	    <div class="rwikicommentheader">
	        <nobr>
	        <c:out value="${rlb.jsp_comment_by}"/>: <rwiki:formatDisplayName name="${(comment.rwikiObject.user)}" /> <c:out value="${rlb.jsp_on}"/> <c:out value="${comment.rwikiObject.version}" /> 
			<jsp:element name="a">
				<jsp:attribute name="href">#</jsp:attribute>
				<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${comment.newCommentURL}" />',1); return false;</jsp:attribute>
				<jsp:body>
					<c:out value="${rlb.jsp_comment}"/>
				</jsp:body>
			</jsp:element>
        	<c:if test="${comment.canEdit}" >
				<jsp:element name="a">
					<jsp:attribute name="href">#</jsp:attribute>
					<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${comment.editCommentURL}" />',1); return false;</jsp:attribute>
					<jsp:body>
						<c:out value="${rlb.jsp_edit}"/>
					</jsp:body>
				</jsp:element>
        	</c:if>
        	</nobr>
        </div>
		 <div class="rwikicomenttext" >
				<c:out value="${comment.renderedPage}" escapeXml="false"/><br/>	    
	      </div>
 </jsp:element>
</c:forEach>
</div>
</jsp:root>
