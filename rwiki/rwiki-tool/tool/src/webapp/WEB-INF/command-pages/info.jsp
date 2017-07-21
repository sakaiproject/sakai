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
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
  xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	/><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}" />
  <c:set var="rightRenderBean"
    value="${requestScope.rsacMap.infoRightRenderBean}" />
  <c:set var="reverseHistoryHelperBean"
    value="${requestScope.rsacMap.reverseHistoryHelperBean}" />
  <c:set var="permissionsBean"
    value="${requestScope.rsacMap.permissionsBean}" />
  <c:set var="referencesBean"
    value="${requestScope.rsacMap.referencesBean}" />
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="updatePermissionsBean" value="${requestScope.rsacMap.updatePermissionsBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="realmBean" value="${requestScope.rsacMap.realmBean}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_info}"/>: <c:out value="${realmBean.localName}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>  	
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
    	<div id="permissionshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_permissions}"/></h3>
    		<c:out value="${rlb.jsp_page_permissions_help}"/>   		
    	</div>
    	<div id="createhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_create_site_permissions}"/></h3>
    		<c:out value="${rlb.jsp_create_site_permissions_help}"/>
    	</div>
    	<div id="readhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_read_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_read_permission_help}"/>
    	</div>
    	<div id="updatehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_edit_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_edit_permission_help}"/>
    	</div>
    	<div id="deletehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_delete_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_delete_permission_help}"/>
    	</div>
    	<div id="adminhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_admin_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_admin_permission_help}"/>
    	</div>
    	<div id="superadminhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_sadmin_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_sadmin_permission_help}"/>
    	</div>
    	<div id="pageownerhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_owner_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_owner_permission_help}"/>
    	</div>
    	<div id="publichelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_public_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_public_permission_help}"/>
    	</div>
    	<div id="siteenablehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_site_en_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_site_en_permission_help1}"/>
    		<br/>
		<c:out value="${rlb.jsp_page_site_en_permission_help2}"/>
    	</div>
    	<div id="pageownerdisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_owner_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_owner_permission_help}"/>
    	</div>
    	<div id="publicdisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_publicdisp_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_publicdisp_permission_help}"/>	
    	</div>
    	<div id="siteenabledisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_site_en_d_permission}"/></h3>
    		<c:out value="${rlb.jsp_page_site_en_d_permission_help}"/>
    	</div>
    	<div id="incomminghelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_incomming_pages}"/></h3>
<c:out value="${rlb.jsp_incomming_pages_help}"/>
    	</div>
    	<div id="outgoinghelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_outgoing_pages}"/></h3>
<c:out value="${rlb.jsp_outgoing_pages_help}"/>
    	</div>
    	<div id="commenthelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_comment_pages}"/></h3>
<c:out value="${rlb.jsp_comment_pages_help}"/>
    	</div>
    	<div id="ownerhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_owner}"/></h3>
<c:out value="${rlb.jsp_page_owner_help}"/>
    	</div>
    	<div id="realmhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_permission_section}"/></h3>
    		<c:out value="${rlb.jsp_permission_section_help}"/>
    	</div>
    	<div id="idhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_id}"/></h3>
    		<c:out value="${rlb.jsp_page_id_help}"/>
    	</div>
    	<div id="globalhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_page_name}"/></h3>
    		<c:out value="${rlb.jsp_page_name_help}"/>
    	</div>
    	
    	<div id="lastedithelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_last_edited}"/></h3>
    		<c:out value="${rlb.jsp_last_edited_help}"/>
    	</div>
    	<div id="digesthelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_sha1_digest}"/></h3>
<c:out value="${rlb.jsp_sha1_digest_help}"/>
    	</div>
        	<div id="preferenceshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3><c:out value="${rlb.jsp_notification_preferences}"/></h3>
    		<c:out value="${rlb.jsp_notification_preferences_help}"/>
    	</div>
      <div id="rwiki_container">
	<div class="portletBody">
	  <div class="navIntraTool actionToolBar">
	    <form action="?#" method="get" class="rwiki_searchForm">
	     	<rwiki:commandlinks 
							useHomeLink="true"
							useViewLink="true"
							useEditLink="true"
							useInfoLink="true"
							useHistoryLink="true"
							useWatchLink="true"
							withNotification="${requestScope.rsacMap.withnotification}"
							viewLinkName="View"
							homeBean="${homeBean}"
							viewBean="${realmBean}" 
							resourceLoaderBean="${rlb}" 
						        />	      
	      <span class="rwiki_searchBox">
			<c:out value="${rlb.jsp_search}"/>:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
		<input type="hidden" name="panel" value="Main" />
		<input type="text" name="search" />
	      </span>
	    </form>
	  </div>
	<c:choose>
	  <c:when test="${rightRenderBean.hasContent}" >
		<c:set var="rwikiContentStyle"  value="withsidebar" />	
	  </c:when>
	  <c:otherwise>
		<c:set var="rwikiContentStyle"  value="nosidebar" />    
	  </c:otherwise>
	</c:choose>
	  

	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <div id="rwiki_head" >				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
	  </div>
	  
	  <!-- Main page -->
	  <div id="rwiki_content" class="${rwikiContentStyle}" >

	    <h3><c:out value="${rlb.jsp_info}"/>: <c:out value="${realmBean.localName}" /></h3>
	    <c:if test="${fn:length(errorBean.errors) gt 0}">
	      <!-- XXX This is hideous -->
	      <p class="validation" style="clear: none;">

		<c:forEach var="error" items="${errorBean.errors}">
		  <c:out value="${error}"/>
		</c:forEach>
	      </p>
	    </c:if>
	    <form action="?#" method="post">
	    	  <input type="hidden" name="smallchange" value="smallchange" />
	    
	      <div class="rwikirenderedContent">
	   
		<script type="text/javascript" >
	      var yes_val='<c:out value="${rlb.jsp_yes}"/>';
	      var no_val='<c:out value="${rlb.jsp_no}"/>';  
		  <![CDATA[
		  
		  var NUMBER_OF_PERMISSIONS =0;
		  var CREATE = NUMBER_OF_PERMISSIONS++;
		  var READ = NUMBER_OF_PERMISSIONS++;
		  var UPDATE = NUMBER_OF_PERMISSIONS++;
		  var ADMIN = NUMBER_OF_PERMISSIONS++;
		  var SUPERADMIN = NUMBER_OF_PERMISSIONS++;
		  var permissionsMatrix = new Array();
		  var permissionsMatriNCols = 5;
		  var permissionsStem = "permissions_";

		  function setPermissionDisplay(enabledClass,disabledClass,readSwitch,updateSwitch,adminSwitch) {
		    var switches = new Array();

		    // lets try something a bit more magical...
		    switches[CREATE] = true;
		    switches[READ] = readSwitch;
		    switches[UPDATE] = updateSwitch;
		    switches[ADMIN] = adminSwitch;
		    switches[SUPERADMIN] = true;

		    // for each role row
		    for ( rowStart = 0; rowStart < permissionsMatrix.length;  rowStart += NUMBER_OF_PERMISSIONS ) {
		      // determine if each permission should be set:
		      for ( j = 0; j < NUMBER_OF_PERMISSIONS; j++) {
			permissionNumber = rowStart + j;

			permissionArray = permissionsMatrix[permissionNumber];
			var enabled = false;
			// By checking if the switch is set and the lock is set.
			for (i = 0; i < NUMBER_OF_PERMISSIONS; i++) {
			  enabled = enabled || (permissionArray[1].charAt(i) == 'x' && permissionsMatrix[rowStart + i][0] && switches[i]);
			}
		  						
			setEnabledElement(permissionsStem + permissionNumber, enabled);

		      }
		    }
		  }

		  function setEnabledElement(elId, enabled) {
		    var el = null;
		    if ( document.all ) {
		      el = document.all[elId];
		    } else {
		      el = document.getElementById(elId);
		    }
		    if (el != null) {
		      el.innerHTML = enabled ? yes_val : no_val;
		    } 
		  }

		  function setClassName(elId,className) {

		    var el = null;
		    if ( document.all ) {
		      el = document.all[elId];
		    } else {
		      el = document.getElementById(elId);
		    }
		    if ( el != null ) {
		      el.className = className;
		    }
		  }
		  var pmi=0;
		  ]]>
		  <c:forEach var="role" items="${realmBean.roles}">
		    var x = new Array(); 
		    x[0] = <c:out value="${role.secureCreate}" />;
		    x[1] = "x---x";
		    permissionsMatrix[pmi] = x;
		    pmi++;
		    x = new Array(); 
		    x[0] = <c:out value="${role.secureRead}" />;
		    x[1] = "-xxxx";
		    permissionsMatrix[pmi] = x;
		    pmi++;
		    x = new Array(); 
		    x[0] = <c:out value="${role.secureUpdate}" />;
		    x[1] = "--xxx";
		    permissionsMatrix[pmi] = x;
		    pmi++;
		    x = new Array(); 
		    x[0] = <c:out value="${role.secureAdmin}" />;
		    x[1] = "---xx";
		    permissionsMatrix[pmi] = x;
		    pmi++;
		    x = new Array(); 
		    x[0] = <c:out value="${role.secureSuperAdmin}" />;
		    x[1] = "----x";
		    permissionsMatrix[pmi] = x;
		    pmi++;
		  </c:forEach>
		</script>
		<table class="rwiki_info" cellspacing="0">
		  <tbody>
		    <tr id="permissions">
		    	<th><c:out value="${rlb.jsp_page_perms_by_role}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'permissionshelp'); return false;"
		    		onMouseOut="hidePopup('permissionshelp');" >?</a></th>
		    	<td><c:out value="${rlb.jsp_permission_create}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'createhelp'); return false;"
		    		onMouseOut="hidePopup('createhelp');" >?</a></td>
		    	<td><c:out value="${rlb.jsp_permission_read}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'readhelp'); return false;"
		    		onMouseOut="hidePopup('readhelp');" >?</a></td>
		    	<td><c:out value="${rlb.jsp_permission_edit}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'updatehelp'); return false;"
		    		onMouseOut="hidePopup('updatehelp');" >?</a></td>
		    	<!--<td><c:out value="${rlb.jsp_permission_delete}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'deletehelp'); return false;"
		    		onMouseOut="hidePopup('deletehelp');" >?</a></td>-->
		    	<td><c:out value="${rlb.jsp_permission_admin}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'adminhelp'); return false;"
		    		onMouseOut="hidePopup('adminhelp');" >?</a></td>
		    	<td><c:out value="${rlb.jsp_permission_super_admin}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'superadminhelp'); return false;"
		    		onMouseOut="hidePopup('superadminhelp');" >?</a></td>
		    </tr>
		    <c:set var="pmcounter" value="${0}" />		  		
		    <c:forEach var="role" items="${realmBean.roles}">
		      <tr class="permissionsGroupRole">
			<th><c:out value="${role.id}"/></th>
			<td>
			  <jsp:element name="span" >
			    <jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
			    <c:set var="pmcounter" value="${pmcounter+1}" />
			    <jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
			      <rwiki:granted span="false" granted="${role.secureCreate}" resourceLoaderBean="${rlb}" />
			  </jsp:element>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${currentRWikiObject.groupRead}">
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureRead}" resourceLoaderBean="${rlb}" />
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureRead}" resourceLoaderBean="${rlb}" />
			      </jsp:element>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${currentRWikiObject.groupWrite}">
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureUpdate}" resourceLoaderBean="${rlb}" />
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureUpdate}" resourceLoaderBean="${rlb}" />
			      </jsp:element>	
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${currentRWikiObject.groupAdmin}">
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureAdmin}" resourceLoaderBean="${rlb}" />
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureAdmin}" resourceLoaderBean="${rlb}"/>
			      </jsp:element>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <jsp:element name="span" >
			    <jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
			    <c:set var="pmcounter" value="${pmcounter+1}" />
			    <jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
			      <rwiki:granted span="false" granted="${role.secureSuperAdmin}" resourceLoaderBean="${rlb}"/>
			  </jsp:element>
			</td>
		      </tr>
		    </c:forEach>
		    <c:choose>
		      <c:when test="${permissionsBean.adminAllowed}" >
			<tr id="permissionsGroup">
			  <th><c:out value="${rlb.jsp_enable_disable_page}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'siteenablehelp'); return false;"
			      onMouseOut="hidePopup('siteenablehelp');" >?</a></th>
			  <td></td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.groupRead}">
				<input type="checkbox" name="groupRead" checked="checked" 
				  onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="groupRead" onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.groupWrite}">
				<input type="checkbox" name="groupWrite" checked="checked" 
				  onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);" />
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="groupWrite" onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);" />
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.groupAdmin}">
				<input type="checkbox" name="groupAdmin" checked="checked" onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="groupAdmin" onClick="setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',groupRead.checked,groupWrite.checked,groupAdmin.checked);"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td></td>
			</tr>
			<tr id="permissionsAdditional">
			  <th><c:out value="${rlb.jsp_additional_page_permissions}"/></th>
			  <td colspan="5">&#160;</td>
			</tr>
			<tr id="permissionsOwner">
			  <th><c:out value="${rlb.jsp_page_owner}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'pageownerhelp'); return false;"
			      onMouseOut="hidePopup('pageownerhelp');" >?</a></th>
			  <td>&#160;</td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.ownerRead}">
				<input type="checkbox" name="ownerRead" checked="checked"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="ownerRead"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.ownerWrite}">
				<input type="checkbox" name="ownerWrite" checked="checked" onClick="if (checked) ownerRead.checked = checked;"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="ownerWrite" onClick="if (checked) ownerRead.checked = checked;"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.ownerAdmin}">
				<input type="checkbox" name="ownerAdmin" checked="checked" onClick="if ( checked ) { ownerRead.checked = checked; ownerWrite.checked = checked; }"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="ownerAdmin" onClick="if (checked) { ownerRead.checked = checked; ownerWrite.checked = checked; }"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>&#160;</td>
			</tr>
			<tr id="permissionsPublic">
			  <th><c:out value="${rlb.jsp_public}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'publichelp'); return false;"
			      onMouseOut="hidePopup('publichelp');" >?</a></th>
			  <td></td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.publicRead}">
				<input type="checkbox" name="publicRead" checked="checked"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="publicRead"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td>
			    <c:choose>
			      <c:when test="${currentRWikiObject.publicWrite}">
				<input type="checkbox" name="publicWrite" checked="checked" onClick="if ( checked) publicRead.checked = checked;"/>
			      </c:when>
			      <c:otherwise>
				<input type="checkbox" name="publicWrite" onClick="if (checked) publicRead.checked = checked;"/>
			      </c:otherwise>
			    </c:choose>
			  </td>
			  <td></td>
			  <td></td>
			  <td></td>
			</tr>
			<tr>
			  <td colspan="7">
			    <div class="rwiki_editControl">
			      <p class="act">
				<input type="hidden" name="pageName" value="${currentRWikiObject.name}" />
				<input type="hidden" name="panel" value="Main"/>
				<input type="hidden" name="action" value="updatePermissions"/>
				<input type="hidden" name="version" value="${currentRWikiObject.version.time}"/>
				<input type="submit" name="updatePermissions" value="${rlb.jsp_button_save}"/>
				<input type="hidden" name="realm" value="${currentRWikiObject.realm }"/>
				<c:if test="${realmBean.siteUpdateAllowed}">
				  <c:out value="${rlb.jsp_edit_page_permissions_msg1}"/><c:out value=" "/><a href="${realmBean.editRealmUrl}"><c:out value="${rlb.jsp_edit_site_permissions2}"/></a>
				</c:if>
			      </p>
			    </div>
			  </td>
			</tr>
			<!-- EXPERIMENTAL -->
			<c:if test="${requestScope.rsacMap.experimental}" >
				<tr>
			  	<td colspan="7">
			   	  <div class="rwiki_editControl">
			      <p class="act">
				  <c:if test="${realmBean.siteUpdateAllowed}">
				    <c:out value="${rlb.jsp_multi_autzgroups_edit}"/>
				    <a href="${realmBean.editRealmManyUrl}" ><c:out value="${rlb.jsp_edit_multi_realms}"/></a>
				  </c:if>
			      </p>
			    </div>
			  </td>
			  </tr>
			</c:if>
		      </c:when>
		      <!--

		      No Page edit allowed

		      -->


		      <c:otherwise>
			<tr id="permissionsGroup">
			  <th><c:out value="${rlb.jsp_enable_disable_page}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'siteenabledisphelp'); return false;"
			      onMouseOut="hidePopup('siteenabledisphelp');" >?</a></th>
			  <td></td>
			  <td>
			  	<rwiki:granted granted="${currentRWikiObject.groupRead }" resourceLoaderBean="${rlb}" />
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupWrite}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <!--
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupDelete}" resourceLoaderBean="${rlb}"/>
			  </td>
			  -->
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupAdmin}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <td></td>
			</tr>
			<tr id="permissionsAdditional">
			  <th><c:out value="${rlb.jsp_additional_page_permissions}"/></th>
			  <td colspan="5">&#160;</td>
			</tr>
			<tr id="permissionsOwner">
			  <th><c:out value="${rlb.jsp_page_owner}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'pageownerdisphelp'); return false;"
			      onMouseOut="hidePopup('pageownerdisphelp');" >?</a></th>
			  <td>&#160;</td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerRead}" resourceLoaderBean="${rlb}" />
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerWrite}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <!--
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerDelete}" resourceLoaderBean="${rlb}"/>
			  </td>
			  -->
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerAdmin}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <td>&#160;</td>
			</tr>
			<tr id="permissionsPublic">
			  <th><c:out value="${rlb.jsp_public}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'publicdisphelp'); return false;"
			      onMouseOut="hidePopup('publicdisphelp');" >?</a></th>
			  <td></td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.publicRead}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.publicWrite}" resourceLoaderBean="${rlb}"/>
			  </td>
			  <!--<td></td>-->
			  <td></td>
			  <td></td>
			  <td></td>
			</tr>
			<tr>
			  <td colspan="7">
			    <div class="rwiki_editControl">
			      <p class="act">
				<c:if test="${realmBean.siteUpdateAllowed}">
				  <c:out value="${rlb.jsp_edit_site_permissions1}"/><a href="${realmBean.editRealmUrl}"><c:out value="${rlb.jsp_edit_site_permissions2}"/></a>
				</c:if>
			      </p>
			    </div>
			  </td>
			</tr>
		      </c:otherwise>
		    </c:choose>
		    <tr id="incommingStart" >
		    	<th><c:out value="${rlb.jsp_incomming}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'incomminghelp'); return false;"
		    		onMouseOut="hidePopup('incomminghelp');" >?</a></th>
		      <td colspan="6">
			<c:set var="referencingLinks"
			  value="${referencesBean.referencingPageLinks}" />
			<c:if test="${fn:length(referencingLinks) gt 0}">
			  <ul id="referencingLinks">
			    <c:forEach var="item" items="${referencingLinks}">
			      <li><c:out value="${item}" escapeXml="false" /></li>
			    </c:forEach>
			  </ul>
			</c:if>
		      </td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_outgoing}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'outgoinghelp'); return false;"
		    		onMouseOut="hidePopup('outgoinghelp');" >?</a></th>
		      <td colspan="6">
			<c:set var="referencedLinks"
			  value="${referencesBean.referencedPageLinks }" />
			<c:if test="${fn:length(referencedLinks) gt 0}">
			  <ul id="referencedLinks">
			    <c:forEach var="item" items="${referencedLinks }">
			      <li><c:out value="${item }" escapeXml="false" /></li>
			    </c:forEach>
			  </ul>
			</c:if>
		      </td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_comment}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'commenthelp'); return false;"
		    		onMouseOut="hidePopup('outgoinghelp');" >?</a></th>
		      <td colspan="6">
			<c:set var="commentLinks"
			  value="${renderBean.comments }" />
			<c:if test="${fn:length(commentLinks) gt 0}">
			  <ul id="commentLinks">
			    <c:forEach var="comment" items="${renderBean.comments }">
			      <li><c:out value="${comment.CommentPageLink }" escapeXml="false" /></li>
			    </c:forEach>
			  </ul>
			</c:if>
		      </td>
		    </tr>
		    <!-- NOTIFICATION SUPPORT, wiki.notifications to enable -->
			<c:if test="${requestScope.rsacMap.withnotification}" >
		    <tr>
		      <th><c:out value="${rlb.jsp_notification_preferences}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'preferenceshelp'); return false;"
		    		onMouseOut="hidePopup('preferenceshelp');" >?</a></th>
		      <td colspan="7">
			<a href="${realmBean.preferencesUrl}"><c:out value="${rlb.jsp_edit_notification_preferences}"/><c:out value="${realmBean.pageSpace}"/></a>
		      </td>
		    </tr>
		    </c:if>
		    <tr>
		    	<th><c:out value="${rlb.jsp_feeds}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'feedshelp'); return false;"
		    		onMouseOut="hidePopup('feedshelp');" >?</a></th>
		      <td colspan="6">
			  <ul id="feedsLinks">
			  <li>
		    	      <jsp:element name="a"><!--
		--><jsp:attribute name="href"><c:out value="${realmBean.publicViewUrl}"/></jsp:attribute><!--
		--><jsp:attribute name="target">publicview</jsp:attribute><!--
		--><c:out value="${rlb.jsp_public}"/><!--
		--></jsp:element>
			</li>
			<c:set var="feedsLinks"
			  value="${referencesBean.feedsLinks }" />
			<c:if test="${fn:length(feedsLinks) gt 0}">
			    <c:forEach var="item" items="${feedsLinks }">
			      <li><c:out value="${item }" escapeXml="false" /></li>
			    </c:forEach>
			</c:if>
			  </ul>
		      </td>
		    </tr>
		    
		    <tr>
		    	<th><c:out value="${rlb.jsp_owner}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'ownerhelp'); return false;"
		    		onMouseOut="hidePopup('ownerhelp');" >?</a></th>
		      <td colspan="6">
		      	<rwiki:formatDisplayName name="${(currentRWikiObject.owner)}"/>
		      </td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_global_name}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'globalhelp'); return false;"
		    		onMouseOut="hidePopup('globalhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${realmBean.pageName }"/></td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_permission_section}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'realmhelp'); return false;"
		    		onMouseOut="hidePopup('realmhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${currentRWikiObject.realm}"/></td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_id}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'idhelp'); return false;"
		    		onMouseOut="hidePopup('idhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${currentRWikiObject.id}"/></td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_last_edited}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'lastedithelp'); return false;"
		    		onMouseOut="hidePopup('lastedithelp');" >?</a></th>
		      <td colspan="6"><fmt:formatDate type="both" value="${currentRWikiObject.version}"/> <c:out value="${rlb.jsp_by}"/> <rwiki:formatDisplayName name="${(currentRWikiObject.user)}"/></td>
		    </tr>
		    <tr>
		    	<th><c:out value="${rlb.jsp_hash_name}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'digesthelp'); return false;"
		    		onMouseOut="hidePopup('digesthelp');" >?</a></th>
		      <td colspan="6"><c:out value="${currentRWikiObject.sha1}"/></td>
		    </tr>
		  </tbody>
		</table>
		<script type="text/javascript"><![CDATA[<!--
		  setPermissionDisplay('rwiki_info_secure_granted','rwiki_info_secure_denied',]]><c:out value="${currentRWikiObject.groupRead}, ${currentRWikiObject.groupWrite}, ${currentRWikiObject.groupAdmin}"/>);
		  --&gt;
		</script>
	      </div>


	    </form>
	  </div>
	  
	  
	  <!-- Creates the right hand sidebar -->
	  <jsp:directive.include file="sidebar.jsp"/>
	  
	</div>
    </div>
    <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
    
  </html>
</jsp:root>
