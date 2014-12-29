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
  <c:set var="realmBean" value="${requestScope.rsacMap.realmBean}"/>
  <c:set var="realmCollectionBean" value="${requestScope.rsacMap.authZGroupCollectionBean}"/>
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
	  <div id="rwiki_content" class="nosidebar" >

	    <h3><c:out value="${rlb.jsp_edit_acl_title}"/>: <c:out value="${realmBean.pageName}" /></h3>
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
		    <tr id="realms_header">
		      <!--<th><c:out value="${rlb.jsp_path}"/></th>-->
		      <th><c:out value="${rlb.jsp_realm}"/></th>
		    </tr>
		<c:forEach var="realm" items="${realmCollectionBean.realms}">
		  <!--<td><c:out value="${realm.path}"/></td>-->
		  <tr>
		    <td><h4><c:out value="${realm.realmId}"/></h4></td>
		  </tr>
		  <tr>
		    <td>
		      <table class="rwiki_rolestable">
			<tr id="permissions">
			  <th><c:out value="${rlb.jsp_permission_roles}"/></th>
			  <td><c:out value="${rlb.jsp_permission_create}"/></td>
			  <td><c:out value="${rlb.jsp_permission_read}"/></td>
			  <td><c:out value="${rlb.jsp_permission_edit}"/></td>
			  <!--<td><c:out value="${rlb.jsp_permission_delete}"/></td>-->
			  <td><c:out value="${rlb.jsp_permission_admin}"/></td>
			  <td><c:out value="${rlb.jsp_permission_super_admin}"/></td>
			</tr>
			<c:forEach var="role" items="${realm.roles}">
			  <tr class="permissionsGroupRole">
			    <th><c:out value="${role.id}"/></th>
			    <td>
			      <c:choose>
				<c:when test="${role.secureCreate}">
				  <input type="checkbox" name="create_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="create_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			    <td>
			      <c:choose>
				<c:when test="${role.secureRead}">
				  <input type="checkbox" name="read_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="read_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			    <td>
			      <c:choose>
				<c:when test="${role.secureUpdate}">
				  <input type="checkbox" name="update_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="update_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			    <!--
			    <td>
			      <c:choose>
				<c:when test="${role.secureDelete}">
				  <input type="checkbox" name="delete_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="delete_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			    -->
			    <td>
			      <c:choose>
				<c:when test="${role.secureAdmin}">
				  <input type="checkbox" name="admin_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="admin_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			    <td>
			      <c:choose>
				<c:when test="${role.secureSuperAdmin}">
				  <input type="checkbox" name="superadmin_${realm.escapedId}_${role.id}" checked="checked"/>
				</c:when>
				<c:otherwise>
				  <input type="checkbox" name="superadmin_${realm.escapedId}_${role.id}"/>
				</c:otherwise>
			      </c:choose>
			    </td>
			  </tr>
			</c:forEach>
		      </table>
		    </td>
		  </tr>
		</c:forEach>
		  <tr>
		    <td colspan="7">
		      <div class="rwiki_editControl">
			<p class="act">
			  <input type="hidden" name="pageName" value="${realmEditBean.pageName}" />
			  <input type="hidden" name="panel" value="Main"/>
			  <input type="hidden" name="action" value="editRealmMany"/>
			  <input type="submit" name="command_save" value="${rlb.jsp_button_save}"/><c:out value=" "/>
			  <input type="submit" name="command_cancel" value="${rlb.jsp_button_cancel}"/>
			  <input type="hidden" name="realm" value="${realmEditBean.localSpace}"/>
			</p>
		      </div>
		    </td>
		  </tr>
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
