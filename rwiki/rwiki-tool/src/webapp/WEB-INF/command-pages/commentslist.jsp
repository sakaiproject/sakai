<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
<div class="rwiki_comments" >
<jsp:element name="a">
	<jsp:attribute name="href">#</jsp:attribute>
	<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${renderBean.newCommentURL}" />',1); return false;</jsp:attribute>
	Add new comment</jsp:element>
<c:forEach var="comment"
		  items="${renderBean.comments}" >
  <jsp:element name="div" >
			<jsp:attribute name="class">rwikicommentbody_<c:out value="${comment.commentLevel}" /></jsp:attribute>
		
	    <div class="rwikicommentheader">
	        Comment by: <rwiki:formatDisplayName name="${(comment.rwikiObject.user)}" /> on <c:out value="${comment.rwikiObject.version}" /> 
			<jsp:element name="a">
				<jsp:attribute name="href">#</jsp:attribute>
				<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${comment.newCommentURL}" />',1); return false;</jsp:attribute>
				Comment</jsp:element>
        		<c:if test="${comment.canEdit}" >
			<jsp:element name="a">
				<jsp:attribute name="href">#</jsp:attribute>
				<jsp:attribute name="onclick">ajaxRefPopup(this,'<c:out value="${comment.editCommentURL}" />',1); return false;</jsp:attribute>
				Edit</jsp:element>
        		</c:if>
        	</div>
		 <div class="rwikicomenttext" >
				<c:out value="${comment.renderedPage}" escapeXml="false"/><br/>	    
	      </div>
 </jsp:element>
</c:forEach>
</div>
</jsp:root>
