<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
  xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp"
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
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Info: <c:out value="${realmBean.localName}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>  	
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
    	<div id="permissionshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Permissions</h3>
    		Each page has a set of page permissions that confer
    		rights on the Page owner, members of the site and the public    		
    	</div>
    	<div id="createhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Create Page Site Permission</h3>
    		This is a site member permission, independant of the page,
    		that allows a user who has been granted it the ability to 
    		create a page. To change it you must use the role permissions editor. 
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission.
    		
    	</div>
    	<div id="readhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Read Page Permission</h3>
    		Once granted, a user may read this page.
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission.
    	</div>
    	<div id="updatehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Update Page Permission</h3>
    		Once granted, a user may edit the content of this page.
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission.
    	</div>
    	<div id="deletehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Delete Page Permission</h3>
    		Once granted, a user may delete this page.
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission.
    	</div>
    	<div id="adminhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Admin</h3>
    		Once granted, a user may edit the permissions of the page, and restore 
    		older versions. Granting admin permission also grants read permission.    		
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission.
    	</div>
    	<div id="superadminhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Super Admin Site Permission</h3>
    		A use that has Super Admin permission in their worksite role is allowed 
    		to do anything to any page.
    		If you have permission, you can tick the checkbox to grant that permission.
    		If you do not have permission, you will see the state of the permission
    	</div>
    	<div id="pageownerhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Owner Permissions</h3>
    		Each page has an owner, this defaults to the user who created the page.
    		Permissions that are ticked in this row (or displayed as 'yes') are granted
    		to the page owner.
    	</div>
    	<div id="publichelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Public Permissions</h3>
    		Where a user is not the page owner and not a member of the worksite
    		they are a 'public' user. Granting Public permissions on the page 
    		gives a 'public' user the permission to read or update the page.
    	</div>
    	<div id="siteenablehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Site Enable Page permissions</h3>
    		When a site enabled page permission is granted, the worksite roles are 
    		consulted to see if a user has permission to perform the action. Where 
    		the specific site enabled page permission is not granted, the role 
    		permissions are not granted for this page.
    		<br/>
		When a page permission is granted using the checkboxes, worksite roles 
		are consulted to see whether that role has permission to perform the 
		action. If that worksite role does not have permission to perform the 
		action, it cannot be granted at a page level.  Page permissions cascade 
		downwards, so roles with admin permissions will also have read permissions.
    	</div>
    	<div id="pageownerdisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Owner Permissions</h3>
    		Each page has an owner, this defaults to the user who created the page.    		
    	</div>
    	<div id="publicdisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Public Permissions</h3>
    		Where a user is not the page owner and not a member of the worksite
    		they are a 'public' user. Granting Public permissions on the page 
    		gives a 'public' user the permission to read or update the page.    		
    	</div>
    	<div id="siteenabledisphelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Site Enable Page permissions</h3>
    		When a site enabled page permission is granted, the worksite roles
    		are consulted to see if a user has permission to perform the action. 
    		Where the specific site enable page permission is not granted, the 
    		role permissions are not enabled for this page.
    	</div>
    	<div id="incomminghelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Incoming Pages</h3>
This is  a list of pages that reference or link to this page.    		
    	</div>
    	<div id="outgoinghelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Outgoing Pages</h3>
This is a list of pages which this page references or links to.    		
    	</div>
    	<div id="ownerhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Owner</h3>
Each page has an owner; this is normally the user who create the page.    		
    	</div>
    	<div id="realmhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Permission Section</h3>
    		This is the site permissions realm that is used to determin site permissions
    		on this page. Normally the realm is the default realm for the site containing
    		the page.    		
    	</div>
    	<div id="idhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Id</h3>
    		Every page is given a unique ID.
    	</div>
    	<div id="globalhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Page Name</h3>
    		Every page has a name, this name is used in the wiki links. If pages between 
    		different worksites are to be used, the full page name should be used in the link.
    	</div>
    	
    	<div id="lastedithelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
    		class="rwiki_help_popup" >
    		<h3>Last Edited</h3>
    		The date and time when the page was last edited.
    	</div>
    	<div id="digesthelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>SHA1 Digest</h3>
Each page has a SHA1 digest. If the digests of 2 pages are the same, the content of
both pages are identical. Any change in the page, will change the digest on the page.    		
    	</div>
        	<div id="preferenceshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden; " 
    		class="rwiki_help_popup" >
    		<h3>Notification Preferences</h3>
    		When a page is changed you will recieve an email notifying you of the change. If you want to 
    		change the volume or nature of the emails you are recieving, you can do this with Notification
    		Preferences. The preferences are controlled on a Site by Site basis so you can elect to be notified
    		of changes to some sited, whilst watching a digest in annother site and ignoring all notifications
    		in annother site.
    	</div>
      <div id="rwiki_container">
	<div class="portletBody">
	  <div class="navIntraTool">
	    <form action="?#" method="get" class="rwiki_searchForm">
	      <span class="rwiki_pageLinks">
		<!-- Home Link -->
		<jsp:element name="a"><jsp:attribute name="href"><c:out value="${homeBean.homeLinkUrl}"/></jsp:attribute><c:out value="${homeBean.homeLinkValue}"/></jsp:element>
		<!-- View Link -->
		<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmBean.viewUrl}"/></jsp:attribute>View</jsp:element>
		<!-- Edit Link -->
		<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmBean.editUrl}"/></jsp:attribute>Edit</jsp:element>
		<!-- Info Link -->
		<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmBean.infoUrl}"/></jsp:attribute><jsp:attribute name="class">rwiki_currentPage</jsp:attribute>Info</jsp:element>
		<!-- History Link -->
		<jsp:element name="a"><jsp:attribute name="href"><c:out value="${realmBean.historyUrl}"/></jsp:attribute>History</jsp:element>
	      </span>
	      
	      <span class="rwiki_searchBox">
		Search:	<input type="hidden" name="action" value="${requestScope.rsacMap.searchTarget}" />
		<input type="hidden" name="panel" value="Main" />
		<input type="text" name="search" />
	      </span>
	    </form>
	  </div>
	  <c:set var="rwikiContentStyle"  value="rwiki_content" />

	  <jsp:directive.include file="breadcrumb.jsp"/>
	  <!-- Creates the right hand sidebar -->
	  <jsp:directive.include file="sidebar.jsp"/>
	  <!-- Main page -->
	  <div id="${rwikiContentStyle}" >

	    <h3>Info: <c:out value="${realmBean.localName}" /></h3>
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
		      el.innerHTML = enabled ? "yes" : "no";
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
		    	<th>Page Permissions by role<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'permissionshelp'); return false;"
		    		onMouseOut="hidePopup('permissionshelp');" >?</a></th>
		    	<td>Create<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'createhelp'); return false;"
		    		onMouseOut="hidePopup('createhelp');" >?</a></td>
		    	<td>Read<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'readhelp'); return false;"
		    		onMouseOut="hidePopup('readhelp');" >?</a></td>
		    	<td>Update<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'updatehelp'); return false;"
		    		onMouseOut="hidePopup('updatehelp');" >?</a></td>
		    	<!--<td>Delete<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'deletehelp'); return false;"
		    		onMouseOut="hidePopup('deletehelp');" >?</a></td>-->
		    	<td>Admin<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'adminhelp'); return false;"
		    		onMouseOut="hidePopup('adminhelp');" >?</a></td>
		    	<td>Super Admin<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'superadminhelp'); return false;"
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
			      <rwiki:granted span="false" granted="${role.secureCreate}"/>
			  </jsp:element>
			</td>
			<td>
			  <c:choose>
			    <c:when test="${currentRWikiObject.groupRead}">
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureRead}"/>
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureRead}"/>
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
				  <rwiki:granted span="false" granted="${role.secureUpdate}"/>
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureUpdate}"/>
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
				  <rwiki:granted span="false" granted="${role.secureAdmin}"/>
			      </jsp:element>
			    </c:when>
			    <c:otherwise>
			      <jsp:element name="span" >
				<jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
				<c:set var="pmcounter" value="${pmcounter+1}" />
				<jsp:attribute name="class">rwiki_info_secure_denied</jsp:attribute>
				  <rwiki:granted span="false" granted="${role.secureAdmin}"/>
			      </jsp:element>
			    </c:otherwise>
			  </c:choose>
			</td>
			<td>
			  <jsp:element name="span" >
			    <jsp:attribute name="id" >permissions_<c:out value="${pmcounter}" /></jsp:attribute>
			    <c:set var="pmcounter" value="${pmcounter+1}" />
			    <jsp:attribute name="class">rwiki_info_secure_granted</jsp:attribute>
			      <rwiki:granted span="false" granted="${role.secureSuperAdmin}"/>
			  </jsp:element>
			</td>
		      </tr>
		    </c:forEach>
		    <c:choose>
		      <c:when test="${permissionsBean.adminAllowed}" >
			<tr id="permissionsGroup">
			  <th>Enable/Disable on this page<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'siteenablehelp'); return false;"
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
			  <th>Additional Page Permissions</th>
			  <td colspan="5">&#160;</td>
			</tr>
			<tr id="permissionsOwner">
			  <th>Page Owner<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'pageownerhelp'); return false;"
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
			  <th>Public<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'publichelp'); return false;"
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
				<input type="submit" name="updatePermissions" value="Save"/>
				<input type="hidden" name="realm" value="${currentRWikiObject.realm }"/>
				<c:if test="${realmBean.siteUpdateAllowed}">
				  In addition to editing the page permissions you may <a href="${realmBean.editRealmUrl}">edit site permissions</a>
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
				    (Experimental) Multiple AuthZGroups Edit
				    <a href="${realmBean.editRealmManyUrl}" >Edit Multi Realms</a>
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
			  <th>Enabled/Disabled on this page<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'siteenabledisphelp'); return false;"
			      onMouseOut="hidePopup('siteenabledisphelp');" >?</a></th>
			  <td></td>
			  <td>
			  	<rwiki:granted granted="${currentRWikiObject.groupRead }"/>
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupWrite}"/>
			  </td>
			  <!--
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupDelete}"/>
			  </td>
			  -->
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.groupAdmin}"/>
			  </td>
			  <td></td>
			</tr>
			<tr id="permissionsAdditional">
			  <th>Additional Permissions</th>
			  <td colspan="5">&#160;</td>
			</tr>
			<tr id="permissionsOwner">
			  <th>Page Owner<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'pageownerdisphelp'); return false;"
			      onMouseOut="hidePopup('pageownerdisphelp');" >?</a></th>
			  <td>&#160;</td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerRead}"/>
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerWrite}"/>
			  </td>
			  <!--
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerDelete}"/>
			  </td>
			  -->
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.ownerAdmin}"/>
			  </td>
			  <td>&#160;</td>
			</tr>
			<tr id="permissionsPublic">
			  <th>Public<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'publicdisphelp'); return false;"
			      onMouseOut="hidePopup('publicdisphelp');" >?</a></th>
			  <td></td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.publicRead}"/>
			  </td>
			  <td>
			      <rwiki:granted granted="${currentRWikiObject.publicWrite}"/>
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
				  You may <a href="${realmBean.editRealmUrl}">edit site permissions</a>
				</c:if>
			      </p>
			    </div>
			  </td>
			</tr>
		      </c:otherwise>
		    </c:choose>
		    <tr id="incommingStart" >
		    	<th>Incoming<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'incomminghelp'); return false;"
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
		    	<th>Outgoing<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'outgoinghelp'); return false;"
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
		    <!-- NOTIFICATION SUPPORT, wiki.notifications to enable -->
			<c:if test="${requestScope.rsacMap.withnotification}" >
		    <tr>
		      <th>Notification Preferences<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'preferenceshelp'); return false;"
		    		onMouseOut="hidePopup('preferenceshelp');" >?</a></th>
		      <td colspan="7">
			<a href="${realmBean.preferencesUrl}">Edit Notification Preferences for <c:out value="${realmBean.pageSpace}"/></a>
		      </td>
		    </tr>
		    </c:if>
		    <tr>
		    	<th>Feeds<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'feedshelp'); return false;"
		    		onMouseOut="hidePopup('feedshelp');" >?</a></th>
		      <td colspan="6">
			  <ul id="feedsLinks">
			  <li>
		    	      <jsp:element name="a"><!--
		--><jsp:attribute name="href"><c:out value="${realmBean.publicViewUrl}"/></jsp:attribute><!--
		--><jsp:attribute name="target">publicview</jsp:attribute><!--
		-->Printer Friendly<!--
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
		    	<th>Owner<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'ownerhelp'); return false;"
		    		onMouseOut="hidePopup('ownerhelp');" >?</a></th>
		      <td colspan="6">
		      	<rwiki:formatDisplayName name="${(currentRWikiObject.owner)}"/>
		      </td>
		    </tr>
		    <tr>
		    	<th>Global Name<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'globalhelp'); return false;"
		    		onMouseOut="hidePopup('globalhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${realmBean.pageName }"/></td>
		    </tr>
		    <tr>
		    	<th>Permission Section<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'realmhelp'); return false;"
		    		onMouseOut="hidePopup('realmhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${currentRWikiObject.realm}"/></td>
		    </tr>
		    <tr>
		    	<th>Id<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'idhelp'); return false;"
		    		onMouseOut="hidePopup('idhelp');" >?</a></th>
		      <td colspan="6"><c:out value="${currentRWikiObject.id}"/></td>
		    </tr>
		    <tr>
		    	<th>Last Edited<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'lastedithelp'); return false;"
		    		onMouseOut="hidePopup('lastedithelp');" >?</a></th>
		      <td colspan="6"><fmt:formatDate type="both" value="${currentRWikiObject.version}"/> by <rwiki:formatDisplayName name="${(currentRWikiObject.user)}"/></td>
		    </tr>
		    <tr>
		    	<th>SHA-1<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'digesthelp'); return false;"
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
	</div>
      </div>
    <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
    
  </html>
</jsp:root>
