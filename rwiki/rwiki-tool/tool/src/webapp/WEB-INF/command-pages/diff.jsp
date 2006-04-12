<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
  xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp"
	/><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="historyBean" value="${requestScope.rsacMap.historyBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.diffRightRenderBean}"/>
  <c:set var="diffBean" value="${requestScope.rsacMap.diffBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>Diff: <c:out value="${historyBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
      	<div class="portletBody">
      		<div class="navIntraTool">
	  <form action="?#" method="get" class="rwiki_searchForm">
	    <span class="rwiki_pageLinks">
	      <!-- Home Link -->
	      <jsp:element name="a"><jsp:attribute name="href"><c:out value="${homeBean.homeLinkUrl}"/></jsp:attribute><c:out value="${homeBean.homeLinkValue}"/></jsp:element>
	    <jsp:element name="a"><jsp:attribute name="href"><c:out value="${historyBean.viewUrl}"/></jsp:attribute>View Current</jsp:element>
	    <jsp:element name="a"><jsp:attribute name="href"><c:out value="${historyBean.historyUrl}"/></jsp:attribute>History</jsp:element>
	    </span>
	    <span class="rwiki_searchBox">
	    Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
	    <input type="hidden" name="panel" value="Main" />
	    <input type="text" name="search" />
	    </span>
	  </form>
	</div>
	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <!-- Creates the right hand sidebar -->
	  <!--<jsp:directive.include file="sidebar.jsp"/>-->
	  <!-- Main page -->
	  <div id="rwiki_content" class="nosidebar">
	    <h3>
	      Page Differences: <c:out value="${historyBean.localName}"/>
	      (Version <c:out value="${diffBean.left.revision}"/>
	      vs <c:out value="${diffBean.right.revision}"/>)
	    </h3>
	    <div class="differences">
	      <table class="colordiff">
		<tr>
		  <td class="pageLeft">
		    <jsp:setProperty name="historyBean" property="interestedRevision" value="${diffBean.left.revision}"/>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewRevisionUrl}"/></jsp:attribute>
		      Version <c:out value="${diffBean.left.revision}"/>
		    </jsp:element>
		    <br/>
		    (modified: <fmt:formatDate type="both" value="${diffBean.left.version}"/> by <rwiki:formatDisplayName name="${(diffBean.left.user)}"/>)
		  </td>
		  <td class="pageRight">
		    <jsp:setProperty name="historyBean" property="interestedRevision" value="${diffBean.right.revision}"/>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewRevisionUrl}"/></jsp:attribute>
		      Version <c:out value="${diffBean.right.revision}"/>
		    </jsp:element>
		    <br/>
		    (modified: <fmt:formatDate type="both" value="${diffBean.right.version}"/> by <rwiki:formatDisplayName name="${(diffBean.right.user)}"/>)
		  </td>
		</tr>
		<c:out value="${diffBean.genericDiffBean.colorDiffTable}" escapeXml="false"/>
	      </table>
	    </div>
	    <table border="0" cellpadding="0" cellspacing="0" class="keytable">
	      <tr>
		<td colspan="2" class="keytablehead">Key</td>
	      </tr>
	      <tr>
		<td width="50%" class="deletedLeft">Deleted</td>
		<td width="50%" class="deletedRight">&#160;</td>
	      </tr>
	      <tr>
		<td colspan="2" class="changedLeft"><div align="center">Changed</div></td>
	      </tr>
	      <tr>
		<td width="50%" class="addedLeft">&#160;</td>
		<td width="50%" class="addedRight">Added</td>
	      </tr>
	    </table>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
