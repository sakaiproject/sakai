<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/>
  
<!-- this page delivers an DIV suitable for use by AJAX in the browser -->  
  
<c:set var="presenceBean" value="${requestScope.rsacMap.presenceBean}"/>
<div class="rwiki_comments" id="chatmessages" >	
</div>
<div class="rwiki_comments" >	
<form action="?#" method="post" onsubmit="ajaxRefPopupPost(this,'?#',2,this); return false;" >
	    Edit Comment<br/>
		<textarea cols="40" rows="5" name="content" id="content" >
		</textarea>
		<input type="hidden" name="action" value="chateditsave"/>
		<input type="hidden" name="panel" value="Main"/>
		<input type="hidden" name="version" value="${currentRWikiObject.version.time}"/>
		<input type="hidden" name="pageName" value="${currentRWikiObject.name}" />
		<input type="hidden" name="realm" value="${currentRWikiObject.realm }"/>
		<br/>
		<span class="act">
		  <input type="submit" name="save" value="save" />
		  <input type="button" value="cancel" onclick="popupClose(-1);"/>
		</span>
	    </form>
</div>
</jsp:root>
