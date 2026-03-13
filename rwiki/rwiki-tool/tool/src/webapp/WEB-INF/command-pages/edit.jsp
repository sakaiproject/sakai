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
   xmlns:rwiki="urn:jsptld:/WEB-INF/rwiki.tld"
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    /><jsp:text
    ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
	<c:set var="permissionsBean" value="${requestScope.rsacMap.permissionsBean}"/>
	<c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
	
	<c:if test="${!permissionsBean.updateAllowed}">
	<jsp:scriptlet>
		if ( true ) {
		    uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean rlb = 
		       uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ResourceLoaderHelperBean.getResourceLoaderBean();
			throw new uk.ac.cam.caret.sakai.rwiki.service.exception.UpdatePermissionException(rlb.getString("jsp_not_allowed_edit_page"));
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
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >
    <head>
      <title><c:out value="${rlb.jsp_edit}"/>: <c:out value="${viewBean.localName}"/></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">setFocus(focus_path); callAllLoaders(); </jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
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
									viewBean="${viewBean}"
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
		    
		    
		    
		    
		    <!-- Main page -->
		    
	<div id="rwiki_head" >
		<div id="rwiki_tabholder">
			<ul class="tabs" >
			<li id="edit" class="tabHeadOn" >
				<jsp:element name="p" >
				<jsp:attribute name="class" >tabhead</jsp:attribute>
				<jsp:attribute name="title" ><c:out value="${rlb.jsp_edit}"/></jsp:attribute>
				<jsp:body>
					<a href="#" onClick="selectTabs('previewTab','tabOn','tabOff','editTab','tabOff','tabOn','preview','tabHeadOn','tabHeadOff','edit','tabHeadOff','tabHeadOn'); return false;"><c:out value="${rlb.jsp_edit}"/></a>
				</jsp:body>
				</jsp:element>
			</li>
			<li id="preview" class="tabHeadOff"  >
				<jsp:element name="p" >
					<jsp:attribute name="class" >tabhead</jsp:attribute>
					<jsp:attribute name="title" ><c:out value="${rlb.jsp_preview}"/></jsp:attribute>
					<jsp:body>
						<a href="#" onClick="selectTabs('previewTab','tabOff','tabOn','editTab','tabOn','tabOff','preview','tabHeadOff','tabHeadOn','edit','tabHeadOn','tabHeadOff'); previewContent('wiki-textarea-content','previewContent', 'pageVersion', 'realm','pageName','?' ); return false;">
					</jsp:body>
				</jsp:element>
			</li>
			</ul>
		</div>
				    
		<jsp:directive.include file="sidebar-switcher.jsp"/>		     
		 
	 </div>
		       
		       
	  <div id="rwiki_content" class="${rwikiContentStyle}" >
		       
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
						<p class="longtext"><label for="submittedContent"><c:out value="${rlb.jsp_submitted_content}"/></label>
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
						<p class="longtext"><label for="submittedContent"><c:out value="${rlb.jsp_submitted_prior_content}"/></label>
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
						<p class="longtext"><label for="submittedContent"><c:out value="${rlb.jsp_submitted_prior_content_attach}"/></label>
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
			      
				      <div>
						<div id="textarea_outer_sizing_divx">
						  <div id="textarea_inner_sizing_divx">
						    <jsp:directive.include file="edittoolbar.jsp"/>
						    <textarea cols="60" rows="25" name="content" id="wiki-textarea-content" onselect="storeCaret(this)" onclick="storeCaret(this)" onkeyup="storeCaret(this)" >
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
					  			<input type="checkbox" name="smallchange" value="smallchange" /> <c:out value="${rlb.jsp_minor_change}"/> <br />
					  		</c:if>
					 		<input id="saveButton" class="active" type="submit" name="command_save" value="Save"  /><c:out value=" "/>
					  		<c:if test="${((editBean.saveType eq 'preview' or fn:startsWith(editBean.saveType, 'attach')) and nameHelperBean.submittedContent != null) or (editBean.saveType ne null and editBean.saveType ne 'preview' and not(fn:startsWith(editBean.saveType, 'attach')))}">
					    		<jsp:element name="input">
									<jsp:attribute name="id">saveButton</jsp:attribute> 
									<jsp:attribute name="type">submit</jsp:attribute> 
									<jsp:attribute name="name">save</jsp:attribute>
									<jsp:attribute name="value"><c:out value="${rlb.jsp_button_overwrite}" /></jsp:attribute>
								</jsp:element>
					    		<c:out value=" "/>
					  		</c:if>
					  		<!--
					    		<jsp:element name="input">
									<jsp:attribute name="type">submit</jsp:attribute> 
									<jsp:attribute name="name">save</jsp:attribute>
									<jsp:attribute name="value"><c:out value="${rlb.jsp_button_preview}" /></jsp:attribute>
								</jsp:element>
					  		    <c:out value=" "/>
					  		-->
	    					<jsp:element name="input">
								<jsp:attribute name="type">submit</jsp:attribute> 
								<jsp:attribute name="name">save</jsp:attribute>
								<jsp:attribute name="value"><c:out value="${rlb.jsp_button_cancel}" /></jsp:attribute>
							</jsp:element>
						</p>
				      </div>
			    	</form>
		    	</div> <!-- end of edit tab -->
		    	
		    	
		        <div id="previewTab" class="tabOff" >	        
					<div class="rwikiRenderedContent" id="previewContent" >
			      		<c:if test="${editBean.saveType eq 'preview'}">
				  			<c:set var="currentContent" value="${currentRWikiObject.content}"/>
				  			<c:set target="${currentRWikiObject}" property="content" value="${editBean.previousContent}"/>	    
				  			<c:out value="${renderBean.previewPage}" escapeXml="false"/><br/>
				  			<c:set target="${currentRWikiObject}" property="content" value="${currentContent}"/>	    
			        	</c:if>
					</div>
		        </div> <!-- end of previewTab -->
				
	  </div> <!-- end of content div -->
  		
  	  <jsp:directive.include file="sidebar.jsp"/>
	  
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
	  <div id="logdiv" >
      </div>
    </jsp:element>
  </html>
</jsp:root>
