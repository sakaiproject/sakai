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
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/><jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="historyBean" value="${requestScope.rsacMap.historyBean}" />
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
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Info: <c:out value="${historyBean.localName}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
        	<div id="versionhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>Versions</h3>
  		Each change in the page generates a new version.
  		Versions start at version 0, when the page is
  		first created and then increase by one each time
  		the page is edited
  		
  	</div>
  	<div id="userhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>User</h3>
  		When a page is changed, either the content 
  		or any of the other information associated
  		with the page (eg permissions). The user
  		that made the change is recorded against
  		the version.
  	</div>
  	<div id="datehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>Date</h3>
  		This is the date of the modification  		
  	</div>
  	<div id="changeshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>Changes</h3>
  		By clicking on the To Current or To Previous link,
  		you will be shown the changes to the previous version or
  		to the current version
  	</div>
  	<div id="reverthelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>Revert</h3>
  		By clicking the revert link against a past version of the page, the version
  		 in question will be used to create a new version. The result is that the 
  		 current page will contain the content and permissions of the version in 
  		 question. 
  	</div>
  	<div id="changedhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3>Content</h3>
  		If the content was not changed between versions
  		this column will indicate that the content was not changed
  		This will be because the permissions, owner or realm were
  		changed in the version.
  		
  	</div>
  	<div id="permissionshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  	     class="rwiki_help_popup" >
  		<pre>o--- s--- p-- = all disabled
orwa srwa prw = all enabled
|||| |||| ||Public Write granted
|||| |||| |Public Read granted
|||| |||| Public page permissions group
|||| |||| 
|||| |||Admin Role Permissions enabled
|||| ||Write Role Permissions enabled
|||| |Read Role Permissions enabled
|||| Site Roler Permissions group
||||
|||Owner admin granted
||Owner Write granted
|Owner Read granted
Page Owner Permissions Group
 
- means permssion denied

  		</pre>
  	</div>
      
      <div id="rwiki_container">
      	<div class="portletBody">
      		
	<div class="navIntraTool">
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
							viewBean="${historyBean}"
						        />
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

	    <h3>History: <c:out value="${historyBean.localName}" /></h3>
	    <div class="rwikirenderedContent">
	      <table class="rwiki_history">
		<tr>
			<th>Version<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'versionhelp'); return false;"
				onMouseOut="hidePopup('versionhelp');" >?</a></th>
			<th>User<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'userhelp'); return false;"
				onMouseOut="hidePopup('userhelp');" >?</a></th>
			<th>Date<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'datehelp'); return false;"
				onMouseOut="hidePopup('datehelp');" >?</a></th>
			<th colspan="2">Changes<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'changeshelp'); return false;"
				onMouseOut="hidePopup('changeshelp');" >?</a></th>
		  <th>
		  	<c:choose>
		  		<c:when test="${permissionsBean.updateAllowed}">
		  			Revert<a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'reverthelp'); return false;"
		  				onMouseOut="hidePopup('reverthelp');" >?</a>
		  		</c:when>
		  		<c:otherwise>
		  			&#160;
		  		</c:otherwise>
		  	</c:choose>
		  </th>
			<th>Changed <a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'changedhelp'); return false;"
				onMouseOut="hidePopup('changedhelp');" >?</a>
			</th>
			<th>Permissions <a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'permissionshelp'); return false;"
				onMouseOut="hidePopup('permissionshelp');" >?</a>
			</th>
		</tr>
		<tr>
		  <td>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewUrl}"/></jsp:attribute>
		      CURRENT (<c:out value="${renderBean.rwikiObject.revision}"/>)
		    </jsp:element>
		  </td>
		  <td>
		    <rwiki:formatDisplayName name="${(renderBean.rwikiObject.user)}"/>
		  </td>
		  <td>&#160;</td>
		  <td colspan="2">&#160;</td>
		  <td>&#160;</td>
		</tr>
		<c:forEach var="historyObject"
		  items="${reverseHistoryHelperBean.reverseHistory}"
		  varStatus="historyObjectStatus">
		  <c:set target="${historyBean}" property="interestedRevision"
		    value="${historyObject.revision}" />
		  <tr>
		    <td>
		      <jsp:element name="a">
			<jsp:attribute name="href">
			  <c:out value="${historyBean.viewRevisionUrl}" />
			</jsp:attribute>
			V.<c:out value="${historyObject.revision}" />
		      </jsp:element>
		    </td>
		    <td><rwiki:formatDisplayName name="${(historyObject.user)}" /></td>
		    <td><fmt:formatDate type="both" value="${historyObject.version}" /></td>
		    <td>
		      <jsp:element name="a">
			<jsp:attribute name="href">
			  <c:out value="${historyBean.diffToCurrentUrl}" />
			</jsp:attribute>
			To Current
		      </jsp:element>
		    </td>
		    <td>
		      <c:if test="${historyObject.revision gt 0}">
			<jsp:element name="a">
			  <jsp:attribute name="href">
			    <c:out value="${historyBean.diffToPreviousUrl}" />
			  </jsp:attribute>
			  To Previous
			</jsp:element>
		      </c:if>
		    </td>
		    <td>
		      <c:choose>
			<c:when test="${permissionsBean.updateAllowed}">
			  <jsp:element name="a">
			    <jsp:attribute name="href">
			      <c:out value="${historyBean.revertToRevisionUrl}" />
			    </jsp:attribute>
			    <jsp:attribute name="onclick">
			    	return confirm('This will cause version V.<c:out value="${historyObject.revision}" /> to become the current version.\nAre you sure you want to do this?');
			    </jsp:attribute>
			    Revert to this version
			  </jsp:element>
			</c:when>
			<c:otherwise>
			  &#160;
			</c:otherwise>
		      </c:choose>
		    </td>
		  	<td>
		  		
		  			<c:if test="${reverseHistoryHelperBean.theSame}" >
		  				Content same as V.<c:out value="${historyObject.revision - 1}" />
		  			</c:if>
		  	</td>
		  	<td>
		  		
		  		<c:out value="${historyObject.permissions}" />
		  	</td>
		  </tr>
		</c:forEach>
	      </table>
	    </div>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
