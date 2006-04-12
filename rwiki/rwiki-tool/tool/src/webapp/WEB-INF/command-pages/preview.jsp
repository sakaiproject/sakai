<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/><jsp:text
	><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="nameHelperBean" value="${requestScope.rsacMap.nameHelperBean}"/>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.previewRightRenderBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <c:set var="editBean" value="${requestScope.rsacMap.editBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Preview Changes To: <c:out value="${viewBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
      	<div class="portletBody">
      		<div class="navIntraTool">
	  <form action="?#" method="get" class="rwiki_searchForm">
	    <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.viewUrl}"/></jsp:attribute>View</jsp:element>
	    <jsp:element name="a"><jsp:attribute name="href"><c:out value="${viewBean.infoUrl}"/></jsp:attribute>Info</jsp:element>
	    Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
	    <input type="hidden" name="panel" value="Main" />
	    <input type="text" name="search" />
	  </form>
	</div>
		<c:set var="rwikiContentStyle"  value="rwiki_content" />
		
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <!-- Creates the right hand sidebar -->
	  <jsp:directive.include file="sidebar.jsp"/>
	  <!-- Main page -->
		<div id="${rwikiContentStyle}" >
			<h3 title="Preview Changes: ${viewBean.pageName}">Preview Changes: <c:out value="${viewBean.localName}"/></h3>
	    <div class="rwikiRenderBody">
	      <div class="rwikiRenderedContent"> 
		<c:set var="currentContent" value="${currentRWikiObject.content}"/>
		<c:set target="${currentRWikiObject}" property="content" value="${nameHelperBean.content}"/>	    
		<c:out value="${renderBean.previewPage}" escapeXml="false"/><br/>
		<c:set target="${currentRWikiObject}" property="content" value="${currentContent}"/>	    
	      </div>
	    </div>
	    <form action="?#" method="post" >
	      <jsp:element name="input">
		<jsp:attribute name="type">hidden</jsp:attribute>
		<jsp:attribute name="name">pageName</jsp:attribute>
		<jsp:attribute name="value"><c:out value="${viewBean.pageName}"/></jsp:attribute>
	      </jsp:element>
	      <input type="hidden" name="panel" value="Main"/>
	      <jsp:element name="input">
		<jsp:attribute name="type">hidden</jsp:attribute>
		<jsp:attribute name="name">content</jsp:attribute>
		<jsp:attribute name="value"><c:out value="${nameHelperBean.content}"/></jsp:attribute>
	      </jsp:element>
	      <input type="hidden" name="save" value="preview"/>

	      <p class="act">
		<input type="hidden" name="action" value="edit" />
		<input type="submit" name="preview" value="Back to Edit"/>
	      </p>

	      <div class="rwiki_docdetails">
		<h3 onClick="expandcontent(this, 'pagedetails')" class="expandable" id="pagedetailsh3">
		  <span class="showstate">&#160;</span>Document Properties
		</h3>
		<div id="pagedetails" class="expandablecontent">
		  <table cellspacing="0">
		    <tbody>
		      <tr>
			<th>global page name</th>
			<td><c:out value="${viewBean.pageName}"/></td>
		      </tr>
		      <tr>
			<th>page realm</th>
			<td><c:out value="${currentRWikiObject.realm}"/></td>
		      </tr>
		      <tr>
			<th>id</th>
			<td><c:out value="${currentRWikiObject.id}"/></td>
		      </tr>
		      <tr>
			<th>last edited</th>
			<td><c:out value="${currentRWikiObject.version}"/></td>
		      </tr>
		    </tbody>
		  </table>
		</div>
		<script type="text/javascript">
		  <![CDATA[
		  <!--
		  hidecontent('pagedetailsh3','pagedetails');
		  -->
		  ]]>
		</script>
	      </div>
	    </form>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
