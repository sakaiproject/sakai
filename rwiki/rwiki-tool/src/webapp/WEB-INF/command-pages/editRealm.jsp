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
  <c:set var="rightRenderBean"
    value="${requestScope.rsacMap.infoRightRenderBean}" />
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="realmEditBean" value="${requestScope.rsacMap.realmEditBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Edit Section: <c:out value="${realmEditBean.localSpace}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
	<div class="portletBody">
	  <!--. No links out of the page except where there are cancel's or save's .-->
	  <!--<div class="navIntraTool">-->
	    <!--<form action="?#" method="get" class="rwiki_searchForm">-->
	      <!--<span class="rwiki_pageLinks">-->
		<!-- Home Link -->
		<!--<jsp:element name="a"><jsp:attribute name="href"><c:out value="${homeBean.homeLinkUrl}"/></jsp:attribute><c:out value="${homeBean.homeLinkValue}"/></jsp:element>-->
		<!-- View Link -->
		<!--<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmEditBean.viewUrl}"/></jsp:attribute>View</jsp:element>-->
		<!-- Info Link -->
		<!--<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmEditBean.infoUrl}"/></jsp:attribute><jsp:attribute name="class">rwiki_currentPage</jsp:attribute>Info</jsp:element>-->
		<!--</span>-->
	      <!--<span class="rwiki_searchBox">-->
		<!--Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />-->
		<!--<input type="hidden" name="panel" value="Main" />-->
		<!--<input type="text" name="search" />-->
		<!--</span>-->
	      <!--</form>-->
	    <!--</div>-->
	  <c:set var="rwikiContentStyle"  value="rwiki_content" />

	  <!--<jsp:directive.include file="breadcrumb.jsp"/>-->
	  <!-- Creates the right hand sidebar -->
	  <!--<jsp:directive.include file="sidebar.jsp"/>-->
	  <!-- Main page -->
	  <div id="${rwikiContentStyle}" >

	    <h3>Edit Section: <c:out value="${realmEditBean.localSpace}" /></h3>
	    <c:if test="${fn:length(errorBean.errors) gt 0}">
	      <!-- XXX This is hideous -->
	      <p class="validation" style="clear: none;">

		<c:forEach var="error" items="${errorBean.errors}">
		  <c:out value="${error}"/>
		</c:forEach>
	      </p>
	    </c:if>
	    <form action="?#" method="post">
	      <div class="rwikirenderedContent">
		<table class="rwiki_info" cellspacing="0">
		  <tbody>
		    <tr id="permissions">
		      <th>Role Permissions</th>
		      <td>Create</td>
		      <td>Read</td>
		      <td>Update</td>
		      <!--<td>Delete</td>-->
		      <td>Admin</td>
		      <td>Super Admin</td>
		    </tr>
		    <c:forEach var="role" items="${realmEditBean.roles}">
		      <tr class="permissionsGroupRole">
			<th><c:out value="${role.id}"/></th>
			<td>
			  <c:choose>
			    <c:when test="${role.secureCreate}">
			      <input type="checkbox" name="create_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="create_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${role.secureRead}">
			      <input type="checkbox" name="read_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="read_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${role.secureUpdate}">
			      <input type="checkbox" name="update_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="update_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
			<!--
			<td>
			  <c:choose>
			    <c:when test="${role.secureDelete}">
			      <input type="checkbox" name="delete_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="delete_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
			-->
			<td>
			  <c:choose>
			    <c:when test="${role.secureAdmin}">
			      <input type="checkbox" name="admin_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="admin_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${role.secureSuperAdmin}">
			      <input type="checkbox" name="superadmin_${role.id}" checked="checked"/>
			    </c:when>
			    <c:otherwise>
			      <input type="checkbox" name="superadmin_${role.id}"/>
			    </c:otherwise>
			  </c:choose>
			</td>
		      </tr>
		    </c:forEach>
		    <tr>
		      <td colspan="7">
			<div class="rwiki_editControl">
			  <p class="act">
			    <input type="hidden" name="pageName" value="${realmEditBean.pageName}" />
			    <input type="hidden" name="panel" value="Main"/>
			    <input type="hidden" name="action" value="editRealm"/>
			    <input type="submit" name="save" value="Save"/><c:out value=" "/>
			    <input type="submit" name="save" value="Cancel"/>
			    <input type="hidden" name="realm" value="${realmEditBean.localSpace}"/>
			  </p>
			</div>
		      </td>
		    </tr>
		  </tbody>
		</table>
	      </div>
	    </form>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
