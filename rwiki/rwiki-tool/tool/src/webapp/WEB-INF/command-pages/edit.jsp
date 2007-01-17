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
 FIXME: i18n
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
   xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    errorPage="/WEB-INF/command-pages/errorpage.jsp" 
    /><jsp:text
    ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
	<c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
	<c:if test="${!permissionsBean.updateAllowed}">
	<jsp:scriptlet>
		if ( true ) {
			throw new uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException("You are not allowed to edit this page");
		}
	</jsp:scriptlet>
	</c:if>
  <c:set var="viewBean" value="${requestScope.rsacMap.viewBean}"/>
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="renderBean" value="${requestScope.rsacMap.renderBean}"/>
  <c:set var="rightRenderBean" value="${requestScope.rsacMap.editRightRenderBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <c:set var="editBean" value="${requestScope.rsacMap.editBean}"/>
  <c:set var="nameHelperBean" value="${requestScope.rsacMap.nameHelperBean}"/>
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Edit: <c:out value="${viewBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">setMainFrameHeightNoScroll('<jsp:expression>request.getAttribute("sakai.tool.placement.id")</jsp:expression>');autoSaveOn('pageName','pageVersion','content','restoreContent','restoreVersion','restoreDate','autosave','tabHeadOff','<c:out value="${requestScope.rsacMap.loadAutoSave}" />');setFocus(focus_path);parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); </jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
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
									viewBean="${viewBean}"
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
		    <c:if test="${rightRenderBean.hasContent}">
		      <jsp:directive.include file="sidebar-switcher.jsp"/>
		    </c:if>
		    <jsp:directive.include file="sidebar.jsp"/>
		    <!-- Main page -->
	  <div id="${rwikiContentStyle}" >
	  <!--
	  The bread crumbs tell us which page we are on
	    <div class="pageName" title="${viewBean.pageName}"><c:out value="${viewBean.localName}"/></div>
	  -->
        <div class="tabcontainer" >
	        <ul class="tabs" >
				<li id="edit" class="tabHeadOn" >        	
		       <p class="tabhead" title="Edit" ><a href="#" onClick="selectTabs('autosaveTab','tabOn','tabOff','previewTab','tabOn','tabOff','editTab','tabOff','tabOn','autosave','tabHeadOn','tabHeadOff','preview','tabHeadOn','tabHeadOff','edit','tabHeadOff','tabHeadOn'); return false;" >Edit</a></p>
		       <div id="editTab" class="tabOn" >
			       <form action="?#" method="post" id="editForm"  >
				      <c:if test="${fn:length(errorBean.errors) gt 0}">
						<!-- XXX This is hideous -->
						<p class="validation" style="clear: none;">
				
						  <c:forEach var="error" items="${errorBean.errors}">
						    <c:out value="${error}"/>
						  </c:forEach>
						</p>
				      </c:if>
			
				      <c:if test="${editBean.saveType != null and editBean.saveType ne 'preview' and not(fn:startsWith(editBean.saveType, 'attach'))}">
						<c:if test="${editBean.saveType eq 'revert'}">
						    <c:set target="${editBean}" property="previousContent" value="${currentRWikiObject.history[editBean.previousRevision].content}"/>
						</c:if>
						<p class="longtext"><label for="submittedContent">Submitted Content</label>
						  <jsp:element name="input" id="submittedContent" >
						    <jsp:attribute name="type">hidden</jsp:attribute>
						    <jsp:attribute name="name">submittedContent</jsp:attribute>
						    <jsp:attribute name="value"><c:out value="${editBean.previousContent }"/></jsp:attribute>
						  </jsp:element>
						</p>
						<pre class="rwiki_previousContent">
						  <c:out value="${editBean.previousContent}"/>
						</pre>
				      </c:if>
				      <c:if test="${editBean.saveType eq 'preview' and nameHelperBean.submittedContent != null}">
						<p class="longtext"><label for="submittedContent">Submitted Content prior to Preview</label>
						  <jsp:element name="input">
						    <jsp:attribute name="type">hidden</jsp:attribute>
						    <jsp:attribute name="name">submittedContent</jsp:attribute>
						    <jsp:attribute name="value"><c:out value="${nameHelperBean.submittedContent}"/></jsp:attribute>
						  </jsp:element>
						</p>
						<pre class="rwiki_previousContent">
						  <c:out value="${nameHelperBean.submittedContent}"/>
						</pre>
				      </c:if>
				      <c:if test="${fn:startsWith(editBean.saveType, 'attach') and nameHelperBean.submittedContent != null}">
						<p class="longtext"><label for="submittedContent">Submitted Content prior to Attach</label>
						  <jsp:element name="input">
						    <jsp:attribute name="type">hidden</jsp:attribute>
						    <jsp:attribute name="name">submittedContent</jsp:attribute>
						    <jsp:attribute name="value"><c:out value="${nameHelperBean.submittedContent}"/></jsp:attribute>
						  </jsp:element>
						</p>
						<pre class="rwiki_previousContent">
						  <c:out value="${nameHelperBean.submittedContent}"/>
						</pre>
				      </c:if>
			      
				      <div class="longtext">
						<div id="textarea_outer_sizing_divx">
						  <div id="textarea_inner_sizing_divx">
						    <jsp:directive.include file="edittoolbar.jsp"/>
						    <textarea cols="60" rows="25" name="content" id="content" onselect="storeCaret(this)" onclick="storeCaret(this)" onkeyup="storeCaret(this)" >
						      <c:choose>
							   <c:when test="${editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach')}">
							    <c:out value="${editBean.previousContent}"/>
						 	   </c:when>
							   <c:otherwise>
							     <c:out value="${currentRWikiObject.content}"/>
								</c:otherwise>
						      </c:choose>
						    </textarea>
						   </div>
						 </div>
				   
					
						<input type="hidden" name="action" value="save"/>
						<input type="hidden" name="panel" value="Main"/>
						<input type="hidden" name="version" id="pageVersion" value="${(editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach') )? editBean.previousVersion : currentRWikiObject.version.time}"/>
						<jsp:element name="input">
						  <jsp:attribute name="type">hidden</jsp:attribute>
						  <jsp:attribute name="name">pageName</jsp:attribute>
						  <jsp:attribute name="id">pageName</jsp:attribute>
						  <jsp:attribute name="value"><c:out value="${currentRWikiObject.name}"/></jsp:attribute>
						</jsp:element>
					    <jsp:element name="input">
					      <jsp:attribute name="type">hidden</jsp:attribute>
					      <jsp:attribute name="name">realm</jsp:attribute>
					      <jsp:attribute name="id">realm</jsp:attribute>
					      <jsp:attribute name="value"><c:out value="${currentRWikiObject.realm}"/></jsp:attribute>		
					    </jsp:element>
				      </div>
				      <div class="rwiki_editControl" id="editControl">
						<p class="act">
					  		<c:if test="${requestScope.rsacMap.withnotification}" >
					  			<input type="checkbox" name="smallchange" value="smallchange" /> Minor Change <br />
					  		</c:if>
					 		<input id="saveButton" type="submit" name="save" value="Save"  /><c:out value=" "/>
					  		<c:if test="${((editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach')) and nameHelperBean.submittedContent != null) or (editBean.saveType ne null and editBean.saveType ne 'preview' and not(fn:startsWith(editBean.saveType, 'attach')))}">
					    		<input id="saveButton" type="submit" name="save" value="Overwrite"/><c:out value=" "/>
					  		</c:if>
					  		<!--
					  		<input type="submit" name="save" value="Preview"/><c:out value=" "/>
					  		-->
					  		<input type="submit" name="save" value="Cancel"/>
						</p>
				      </div>
			    	</form>
		    	</div>
		    </li>
		    <li id="preview" class="tabHeadOff"  >
				<p class="tabhead" title="Preview"><a href="#" onClick="selectTabs('autosaveTab','tabOn','tabOff','previewTab','tabOff','tabOn','editTab','tabOn','tabOff','autosave','tabHeadOn','tabHeadOff','preview','tabHeadOff','tabHeadOn','edit','tabHeadOn','tabHeadOff'); previewContent('content','previewContent', 'pageVersion', 'realm','pageName','?' ); return false;" >Preview</a></p>
		        <div id="previewTab" class="tabOff" >	        
					<div class="rwikiRenderedContent" id="previewContent" >
			      		<c:if test="${editBean.saveType eq 'preview'}">
				  			<c:set var="currentContent" value="${currentRWikiObject.content}"/>
				  			<c:set target="${currentRWikiObject}" property="content" value="${editBean.previousContent}"/>	    
				  			<c:out value="${renderBean.previewPage}" escapeXml="false"/><br/>
				  			<c:set target="${currentRWikiObject}" property="content" value="${currentContent}"/>	    
			        	</c:if>
					</div>
		        </div>      
		    </li>
		    <li id="autosave" class="autoSaveOffClass" >
		    
				<p class="tabhead" title="Recovered Content"><a href="#" onClick="selectTabs('autosaveTab','tabOff','tabOn','previewTab','tabOn','tabOff','editTab','tabOn','tabOff','autosave','tabHeadOff','tabHeadOn','preview','tabHeadOn','tabHeadOff','edit','tabHeadOn','tabHeadOff'); return false;" >Recovered Content</a></p>
				<div id="autosaveTab" class="tabOff" >
		    	   	<p class="shorttext">
		   				<input type="button" name="restoreButton" id="restoreButton" value="Restore Saved Edit"
		   				onClick="restoreSavedContent('pageVersion', 'content', 'restoreContent','restoreVersion','restoreDate','autosave','autoSaveOffClass' ); selectTabs('autosaveTab','tabOn','tabOff','previewTab','tabOn','tabOff','editTab','tabOff','tabOn','autosave','tabHeadOn','tabHeadOff','preview','tabHeadOn','tabHeadOff','edit','tabHeadOff','tabHeadOn'); return false;"
		   				/>
		   			</p>
		    	   	<p class="shorttext">
		   				<label for="restoreVersion" >Auto Save Version</label>
		   				<input type="input" name="restoreVersion" id="restoreVersion" value="none"/>
		   			</p>
		   			<p class="shorttext">
		   				<label for="restoreDate" >Auto Save Date</label>
		   				<input type="input" name="restoreDate" id="restoreDate" value="none"/>
		   			</p>
		   			<div class="longtext">
			    		<textarea cols="60" rows="25" name="restoreContent" id="restoreContent"  readonly="readonly" >no restored content
			    		</textarea>
					</div>
				</div>
			</li>
		   </ul>
	   </div>
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
	  <div id="logdiv" >
      </div>
    </jsp:element>
  </html>
</jsp:root>
