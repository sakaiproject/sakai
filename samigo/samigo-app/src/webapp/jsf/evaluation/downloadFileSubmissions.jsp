<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
 <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.title_download_file_submissions}" /></title>
		<style type="text/css">
			.disabled
			{
				background-color: #f1f1f1;
			}
		</style> 
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">

<script>
	$(document).ready(function(){
		$(".allSections").hide();
		// The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
		var currentLink = $('#downloadFileSubmissions\\:downloadFileSubmissionsMenuLink');
		currentLink.addClass('current');
		// Remove the link of the current option
		currentLink.html(currentLink.find('a').text());
	});
    
	function toggleChecked() {
		$('input[type=checkbox]').each( function() {
			//alert('name = ' + this.name);
			if (this.name.match(/downloadFileSubmissions.*questionCheckbox/) != null) {
				$(this).attr("checked", "checked");
			}
		})
	}

	

	function showHideReleaseSections(){
		var releaseRadio =$("input[name='downloadFileSubmissions:siteSection']:checked");
		//alert("releaseRadio.val(): " + releaseRadio.val());
		if(releaseRadio.val() === "sections") {
			$(".allSections").show(); 
		}
		else {
			$(".allSections").hide();
		}
	}
	
</script>
 <!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<!-- content... -->
<h:form id="downloadFileSubmissions">
  <h:inputHidden id="publishedId" value="#{downloadFileSubmissions.publishedAssessmentId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

    <h:panelGroup layout="block" styleClass="page-header">
      <h1>
        <h:outputText value="#{evaluationMessages.title_download_file_submissions}#{evaluationMessages.column} " escape="false"/>
        <small><h:outputText value="(#{totalScores.assessmentName}) " escape="false"/></small>
      </h1>
  </h:panelGroup>

  <!-- EVALUATION SUBMENU -->
  <%@ include file="/jsf/evaluation/evaluationSubmenu.jsp" %>

  <f:verbatim><div class="tier1"></f:verbatim>
  <h:messages infoClass="sak-banner-info" warnClass="sak-banner-warn" errorClass="sak-banner-error" fatalClass="sak-banner-error"/>
  
  <h:outputText value="#{evaluationMessages.download_responses_to}" escape="false"/>
  <f:verbatim></div></f:verbatim>
  
  <f:verbatim><div style="margin-left: 30px;"></f:verbatim>
  <h:panelGrid columns="1">
  <h:outputLink title="#{evaluationMessages.select_all}" onclick="toggleChecked()" value="#" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
          <h:outputText value="#{evaluationMessages.select_all}" />        
  </h:outputLink>
  </h:panelGrid>
  <f:verbatim></div></f:verbatim>
  
  <f:verbatim><div style="margin-left: 24px;"></f:verbatim>
  <h:dataTable value="#{downloadFileSubmissions.fileUploadQuestionList}" var="question" columnClasses="downloanQuestionCheckbox,downloanQuestionDescription">
    <h:column>
      <h:selectManyCheckbox value="" id="questionCheckbox" rendered="#{downloadFileSubmissions.fileUploadQuestionListSize > 1}">
      	<f:selectItem itemValue="#{question.itemIdString}" />
       </h:selectManyCheckbox>
    </h:column>

    <h:column>
      <h:panelGrid columnClasses="samLeftNav," border="0">
        <h:outputText value="<b>#{evaluationMessages.part} #{question.section.sequence}: #{evaluationMessages.question} #{question.section.sequence}</b> - #{evaluationMessages.q_fu}" escape="false"/>
        <h:outputText value="#{question.text}" escape="false"/>
      </h:panelGrid>
    </h:column>
  </h:dataTable>
  <f:verbatim></div></f:verbatim>

  <f:verbatim><div style="margin-left: 2px;"></f:verbatim>
  	 <h:panelGrid border="0">
      <h:selectOneRadio id="siteSection" layout="pagedirection" value="#{downloadFileSubmissions.firstTargetSelected}" onclick="showHideReleaseSections();"
        required="true" rendered="#{downloadFileSubmissions.availableSectionSize > 0 }">
        <f:selectItems value="#{downloadFileSubmissions.siteSectionItems}" />
      </h:selectOneRadio>
    </h:panelGrid>
  <f:verbatim></div></f:verbatim>
    
  <f:verbatim><div class="allSections" style="margin-left: 24px;"></f:verbatim>
  <h:selectManyCheckbox id="sectionsForSite" layout="pagedirection" value="#{downloadFileSubmissions.sectionsSelected}" rendered="#{downloadFileSubmissions.availableSectionSize > 1 }">
     <f:selectItems value="#{downloadFileSubmissions.availableSectionItems}" />
    </h:selectManyCheckbox>
  <f:verbatim></div></f:verbatim>
  
    <f:verbatim><div class="tier1"></f:verbatim> 
<p class="act">

   <%-- <h:commandButton value="#{evaluationMessages.save_exit}" action="author"/> --%> 
   <h:commandButton	value="#{evaluationMessages.download}" actionListener="#{downloadFileSubmissions.downloadFiles}" styleClass="active" />
   
</p>
<f:verbatim></div></f:verbatim>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
