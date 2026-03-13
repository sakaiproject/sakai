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
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="${rlb.jsp_xml_lang}">
    <head>
      <title><c:out value="${rlb.jsp_info}"/>: <c:out value="${historyBean.localName}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression> callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
        	<div id="versionhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_versions}"/></h3>
  		<c:out value="${rlb.jsp_history_versions_help}"/>
  	</div>
  	<div id="userhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_user}"/></h3>
  		<c:out value="${rlb.jsp_history_user_help}"/>
  	</div>
  	<div id="datehelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_date}"/></h3>
  		<c:out value="${rlb.jsp_history_date_help}"/>	
  	</div>
  	<div id="changeshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_changes}"/></h3>
  		<c:out value="${rlb.jsp_history_changes_help}"/>
  	</div>
  	<div id="reverthelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_revert}"/></h3>
  		<c:out value="${rlb.jsp_history_revert_help}"/>
  	</div>
  	<div id="changedhelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  		class="rwiki_help_popup" >
  		<h3><c:out value="${rlb.jsp_history_content}"/></h3>
  		<c:out value="${rlb.jsp_history_content_help}"/>
  	</div>
  	<div id="permissionshelp" style=" position: absolute; top: -1000px; left: -1000px; visibility: hidden;" 
  	     class="rwiki_help_popup" >
  		<pre><c:out value="${rlb.jsp_history_permissions_help}"/></pre>
  	</div>
      
      <div id="rwiki_container">
      	<div class="portletBody">
      		
	<div class="navIntraTool actionToolBar">
	  <div class="rwiki_searchForm">
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
							resourceLoaderBean="${rlb}"
						        />
	    <span class="rwiki_searchBox">
	      <button type="button" class="btn btn-secondary" onclick="openWikiSearch()"><c:out value="${rlb.jsp_search}"/></button>
	    </span>
	  </div>
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
		
	  <!-- Creates the right hand sidebar -->
	  <div id="rwiki_head" >				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
	  </div>
	  
	  <!-- Main page -->
	  <div id="rwiki_content" class="${rwikiContentStyle}" >

	    <h3><c:out value="${rlb.jsp_history}"/>: <c:out value="${historyBean.localName}" /></h3>
	    <div class="rwikirenderedContent">
	      <table class="rwiki_history">
		<tr>
			<th><c:out value="${rlb.jsp_history_version}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'versionhelp'); return false;"
				onMouseOut="hidePopup('versionhelp');" >?</a></th>
			<th><c:out value="${rlb.jsp_history_user}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'userhelp'); return false;"
				onMouseOut="hidePopup('userhelp');" >?</a></th>
			<th><c:out value="${rlb.jsp_history_date}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'datehelp'); return false;"
				onMouseOut="hidePopup('datehelp');" >?</a></th>
			<th colspan="2"><c:out value="${rlb.jsp_history_changes}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'changeshelp'); return false;"
				onMouseOut="hidePopup('changeshelp');" >?</a></th>
		  <th>
		  	<c:choose>
		  		<c:when test="${permissionsBean.updateAllowed}">
		  			<c:out value="${rlb.jsp_history_revert}"/><a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'reverthelp'); return false;"
		  				onMouseOut="hidePopup('reverthelp');" >?</a>
		  		</c:when>
		  		<c:otherwise>
		  			&#160;
		  		</c:otherwise>
		  	</c:choose>
		  </th>
			<th><c:out value="${rlb.jsp_history_changed}"/> <a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'changedhelp'); return false;"
				onMouseOut="hidePopup('changedhelp');" >?</a>
			</th>
			<th><c:out value="${rlb.jsp_history_permissions}"/><c:out value=" "/> <a href="#" class="rwiki_help_popup_link" onClick="showPopupHere(this,'permissionshelp'); return false;"
				onMouseOut="hidePopup('permissionshelp');" >?</a>
			</th>
		</tr>
		<tr>
		  <td>
		    <jsp:element name="a">
		      <jsp:attribute name="href"><c:out value="${historyBean.viewUrl}"/></jsp:attribute>
		      <c:out value="${rlb.jsp_history_current}"/><c:out value=" "/> (<c:out value="${renderBean.rwikiObject.revision}"/>)
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
			<c:out value="${rlb.jsp_history_to_current}"/>
		      </jsp:element>
		    </td>
		    <td>
		      <c:if test="${historyObject.revision gt 0}">
			<jsp:element name="a">
			  <jsp:attribute name="href">
			    <c:out value="${historyBean.diffToPreviousUrl}" />
			  </jsp:attribute>
			  <c:out value="${rlb.jsp_history_to_previous}"/>To Previous
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
			    	return confirm('<c:out value="${rlb.jsp_history_version_msg1}"/><c:out value="${historyObject.revision}" /> <c:out value="${rlb.jsp_history_version_msg2}"/>');
			    </jsp:attribute>
			     <c:out value="${rlb.jsp_history_revert_this_version}"/>Revert to this version
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
	    </div> <!-- rwikirenderedcontent -->
	    
	    
	  </div> <!-- rwiki_content -->
	  <jsp:directive.include file="sidebar.jsp"/>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
