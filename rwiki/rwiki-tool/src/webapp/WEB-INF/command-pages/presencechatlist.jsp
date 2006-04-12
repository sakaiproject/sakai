<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
<c:set var="presenceBean" value="${requestScope.rsacMap.presenceBean}"/>
<div class="rwikicommentbody_0" >	
	<c:forEach var="messages"
		  items="${presenceBean.spaceMessages}" >
		<div class="rwikicomenttext" >
			<c:out value="${presence.message}" />   		  
			<c:out value="${presence.user}" />&#160;   
			<c:out value="${presence.age}" /> ago
	     </div>
	</c:forEach>
</div>
</jsp:root>
