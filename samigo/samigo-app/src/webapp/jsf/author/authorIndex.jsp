<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
   
<!--
* $Id$
<%--
**********************************************************************************
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
      <title><h:outputText value="#{authorFrontDoorMessages.auth_front_door}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
      <div class="portletBody">

<!-- content... -->

<h:form id="authorIndexForm">
  <!-- HEADINGS -->
   <%@ include file="/jsf/author/assessmentHeadings.jsp" %>

  <h3>
    <h:outputText value="#{authorFrontDoorMessages.assessments}"/>
  </h3>
<p>

  <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>


</p>

<div class="tier1">
 <h:outputText escape="false" rendered="#{authorization.createAssessment}" value="<h4>"/>	
<h:outputText value="#{authorFrontDoorMessages.assessment_new}" rendered="#{authorization.createAssessment}" />
 <h:outputText escape="false" rendered="#{authorization.createAssessment}" value="</h4>"/>
 <h:outputText escape="false" rendered="#{authorization.createAssessment}" value="<div class=\"tier2\">"/>
<h:outputText escape="false" rendered="#{authorization.createAssessment}" value="<h5>"/>
    <h:outputText value="#{authorFrontDoorMessages.assessment_create}" styleClass="form_label" rendered="#{authorization.createAssessment}" />
   <h:outputText escape="false" rendered="#{authorization.createAssessment}" value="</h5>"/>
   <h:panelGrid columns="2">
    <h:outputLabel for="assessmentTemplate" value="#{authorFrontDoorMessages.assessment_choose}" styleClass="form_label" 
       rendered="#{authorization.createAssessment && author.showTemplateList}" />
<h:panelGroup rendered="#{authorization.createAssessment && author.showTemplateList}">
      <h:selectOneMenu id="assessmentTemplate" accesskey="#{authorFrontDoorMessages.a_select}"     
        value="#{author.assessmentTemplateId}">
         <f:selectItem itemValue="" itemLabel="#{generalMessages.select_menu}"/>
         <f:selectItems value="#{author.assessmentTemplateList}" />
      </h:selectOneMenu>

      <h:outputText value="#{authorFrontDoorMessages.optional_paren}" styleClass="form_label" />
</h:panelGroup>
    
    <h:outputLabel for="title"  value="#{authorFrontDoorMessages.assessment_title}" rendered="#{authorization.createAssessment}"/>
<h:panelGroup rendered="#{authorization.createAssessment}">
    <h:inputText id="title" value="#{author.assessTitle}" size="32" />
    <!-- AuthorAssessmentListener.createAssessment() read param from AuthorBean to
      create the assessment  -->

	<h:commandButton id="samLiteCreate" value="#{authorFrontDoorMessages.button_samlite}" immediate="true" type="submit" 
      rendered="#{samLiteBean.visible}" accesskey="#{authorFrontDoorMessages.a_samlite}"
      action="#{author.getOutcome}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.samlite.NameListener" />
    </h:commandButton>

    <!-- action=createAssessment if privilege is granted, otherwise =author --> 
    <h:commandButton type="submit" value="#{authorFrontDoorMessages.button_create}" action="#{author.getOutcome}" accesskey="#{authorFrontDoorMessages.a_create}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
    </h:commandButton>
</h:panelGroup>
   
    <h:outputLabel for="import" value="#{authorFrontDoorMessages.assessment_import}" rendered="#{authorization.createAssessment}"/>

    <h:commandButton id="import" value="#{authorFrontDoorMessages.button_import}" immediate="true" type="submit" 
      rendered="#{authorization.createAssessment}" accesskey="#{authorFrontDoorMessages.a_import}"
      action="importAssessment">
    </h:commandButton>
</h:panelGrid>

 <h:outputText escape="false" rendered="#{authorization.createAssessment}" value="</div>"/>
	<!-- CORE ASSESSMENTS-->

  <h:outputText escape="false" rendered="#{authorization.adminCoreAssessment}" value="<h4>"/>
<h:outputText value="#{authorFrontDoorMessages.assessment_pending}" rendered="#{authorization.adminCoreAssessment}"/>
 <h:outputText escape="false" rendered="#{authorization.adminCoreAssessment}" value="</h4>"/>
<div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="coreAssessments" value="#{author.assessments}" var="coreAssessment" rendered="#{authorization.adminCoreAssessment}" summary="#{authorFrontDoorMessages.sum_coreAssessment}">
    <h:column>
      <f:facet name="header">
          <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" id="sortCoreByTitleAction" immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy!='title'}">
  <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort"/>
          <f:param name="coreSortType" value="title"/>
          <f:param name="coreAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
        </h:commandLink>

         <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='title' && author.coreAscending }">
          <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort"/>
          
           <f:param name="coreAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleDescending}" rendered="#{author.coreAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='title'&& !author.coreAscending }">
  <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort"/>
           <f:param name="coreAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleAscending}" rendered="#{!author.coreAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <!-- action=editAssessment if pass authz -->
      <h:commandLink title="#{authorFrontDoorMessages.t_editAssessment}" id="editAssessment" immediate="true" action="#{author.getOutcome}"
        rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}" >
        <h:outputText id="assessmentTitle" value="#{coreAssessment.title}" styleClass="currentSort" escape="false"/>
        <f:param name="editType" value="pendingAssessment" />
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
      </h:commandLink>
      <h:outputText id="assessmentTitle2" value="#{coreAssessment.title}"  escape="false"
        rendered="#{!authorization.editAnyAssessment and !authorization.editOwnAssessment}" />
 <h:outputText escape="false" rendered="#{authorization.adminCoreAssessment}" value="<br />"/>

      <!-- AuthorBean.editAssessmentSettings() prepare the edit page -->
      <!-- action=editAssessmentSettings if pass authz -->
      <span class="itemAction">

      <h:commandLink title="#{authorFrontDoorMessages.t_copyAssessment}" id="copyAssessment" immediate="true" 
        rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"
        action="confirmCopyAssessment">
        <h:outputText id="linkCopy" value="#{authorFrontDoorMessages.link_copy}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmCopyAssessmentListener" />
      </h:commandLink>

	  <h:outputText value=" #{authorFrontDoorMessages.separator} " 
          rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"/>

      <h:commandLink title="#{authorFrontDoorMessages.t_exportAssessment}" id="exportAssessment" immediate="true" 
        rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}"
        action="chooseExportType">
        <h:outputText id="linkExport" value="#{authorFrontDoorMessages.link_export}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ChooseExportTypeListener" />
      </h:commandLink>
      
	  <h:outputText value=" #{authorFrontDoorMessages.separator} " 
          rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"/>

      <!-- action=confirmRemoveAssessment if pass authz -->
      <h:commandLink title="#{authorFrontDoorMessages.t_removeAssessment}" id="confirmRemoveAssessment" immediate="true" 
        rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}"
        action="#{author.getOutcome}">
        <h:outputText id="linkRemove" value="#{authorFrontDoorMessages.link_remove}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemoveAssessmentListener" />
      </h:commandLink>
        
	  <h:outputText value=" #{authorFrontDoorMessages.separator} " 
          rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}" />

	  <h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editAssessmentSettings_author" immediate="true" action="#{author.getOutcome}"
         rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}">
        <h:outputText id="linkSettings" value="#{authorFrontDoorMessages.link_settings}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
      </h:commandLink>

</span>
    </h:column>
    <h:column>
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>
  	  <h:outputText value="#{coreAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{coreAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_no_sec}"/>
      </h:outputText>
      
    </h:column>
  </h:dataTable>
</div>

	<!-- PUBLISHED ASSESSMENTS-->
 <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<h4>"/>
  <h:outputText value="#{authorFrontDoorMessages.assessment_pub}" rendered="#{authorization.adminPublishedAssessment}"/>
 <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="</h4>"/>
  <!-- active -->
<!--
  <p>
  <span class="rightNav">
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      firstItem="1" lastItem="10" totalItems="50"
      prevText="Previous" nextText="Next" numItems="10" />
  </span>
  </p>
-->
<div class="tier2">
<h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<h5>"/>
  <h:outputText value="#{authorFrontDoorMessages.assessment_active}" rendered="#{authorization.adminPublishedAssessment}"/>
<h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="</h5>"/>
  <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" rendered="#{authorization.adminPublishedAssessment}"
    value="#{author.publishedAssessments}" var="publishedAssessment" summary="#{authorFrontDoorMessages.sum_publishedAssessment}">
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" id="sortPubByTitleAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='title'}">
          <h:outputText value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
          <f:param name="pubSortType" value="title"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>

        <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='title' && author.publishedAscending }">
         <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
         
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='title'&& !author.publishedAscending }">
           <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleAscending}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>

	  <!-- action=editAssessment if pass authz -->
      <h:commandLink title="#{authorFrontDoorMessages.t_editPublishedAssessment}" id="editPublishedAssessment" immediate="true" action="confirmEditPublishedAssessment"
        rendered="#{(authorization.editAnyAssessment or authorization.editOwnAssessment) and (!author.editPubAssessmentRestricted or !publishedAssessment.hasAssessmentGradingData)}" >
        <h:outputText id="publishedAssessmentTitle" value="#{publishedAssessment.title}" styleClass="currentSort" escape="false"/>
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmEditPublishedAssessmentListener" />
      </h:commandLink>

      <h:outputText id="publishedAssessmentTitle2" value="#{publishedAssessment.title}" escape="false"
        rendered="#{(!authorization.editAnyAssessment and !authorization.editOwnAssessment) or (author.editPubAssessmentRestricted and publishedAssessment.hasAssessmentGradingData)}" />
      <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<br />"/>

 <span class="itemAction">
      <!-- if passAuth, action=editPublishedAssessmentSettings -->
      <h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editPublishedAssessmentSettings_author" immediate="true"
          rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"
          action="#{author.getOutcome}">
        <h:outputText  value="#{authorFrontDoorMessages.link_settings}" />
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>

      <h:outputText value=" #{authorFrontDoorMessages.separator} " />

	  <h:commandLink title="#{authorFrontDoorMessages.t_removeAssessment}" id="confirmRemovePublishedAssessment" immediate="true" 
        rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}"
        action="#{author.getOutcome}">
        <h:outputText id="linkRemove" value="#{authorFrontDoorMessages.link_remove}"/>
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePublishedAssessmentListener" />
      </h:commandLink>

      <h:outputText value=" #{authorFrontDoorMessages.separator} " 
         rendered="#{publishedAssessment.submissionSize >0 and (authorization.editAnyAssessment or authorization.editOwnAssessment)}"/>
      <h:commandLink title="#{authorFrontDoorMessages.t_score}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore1" 
         rendered="#{publishedAssessment.submissionSize >0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">

        <h:outputText value="#{authorFrontDoorMessages.link_scores}" />
        <f:param name="actionString" value="gradeAssessment" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{publishedAssessment.publishedAssessmentId}" />
        <f:param name="allSubmissionsT" value="3"/>
      </h:commandLink>
</span>
    </h:column>

    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" id="sortPubByreleaseToAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='releaseTo'}">
          <h:outputText value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort" />
          <f:param name="pubSortType" value="releaseTo"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>

<h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='releaseTo' && author.publishedAscending }">

        <h:outputText  value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort" />
        
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseToDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='releaseTo'&& !author.publishedAscending }">
  <h:outputText  value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort" />
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseToAscending}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>

      <h:outputText value="#{publishedAssessment.releaseTo}" rendered="#{publishedAssessment.releaseTo ne 'Anonymous Users' && publishedAssessment.releaseTo ne 'Selected Groups'}" />
      <h:outputText value="#{assessmentSettingsMessages.anonymous_users}" rendered="#{publishedAssessment.releaseTo eq 'Anonymous Users'}"/>
      <h:outputText value="#{assessmentSettingsMessages.selected_group}" rendered="#{publishedAssessment.releaseTo eq 'Selected Groups'}" />
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" id="sortPubByStartDateAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='startDate'}" >
          <h:outputText value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort" />
          <f:param name="pubSortType" value="startDate"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>

 <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='startDate' && author.publishedAscending }">
        <h:outputText  value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort" />
         
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseDateDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='startDate'&& !author.publishedAscending }">
            <h:outputText  value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort" />
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseDateAscending}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{publishedAssessment.startDate}" >
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" id="sortPubByDueDateAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='dueDate'}">
          <h:outputText value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort" />
          <f:param name="pubSortType" value="dueDate"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>
 <h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='dueDate' && author.publishedAscending }">
         <h:outputText  value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort"  />
         
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortDueDateDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='dueDate'&& !author.publishedAscending }">
            <h:outputText  value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort"  />
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortDueDateAscending}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{publishedAssessment.dueDate}" >
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
      </h:outputText>
    </h:column>

	<h:column>
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>

  	  <h:outputText value="#{publishedAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{publishedAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_no_sec}"/>
      </h:outputText>
    </h:column>

  </h:dataTable>

  <!--inactive-->
  <p>
<!--
  <span class="rightNav">
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      firstItem="1" lastItem="10" totalItems="50"
      prevText="Previous" nextText="Next" numItems="10" />
  </span>
-->
  </p>

<h5>
  <h:outputText value="#{authorFrontDoorMessages.assessment_inactive}" rendered="#{authorization.adminPublishedAssessment}"/>
</h5>
  <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier"
     rendered="#{authorization.adminPublishedAssessment}"
     value="#{author.inactivePublishedAssessments}" var="inactivePublishedAssessment" summary="#{authorFrontDoorMessages.sum_inactiveAssessment}"
     id="inactivePublishedAssessments">
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" id="sortInactiveByTitleAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='title'}" >
          <h:outputText value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
          <f:param name="inactiveSortType" value="title"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
<h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='title' && author.inactivePublishedAscending }">
          <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
          
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleDescending}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortTitle}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='title'&& !author.inactivePublishedAscending }">
            <h:outputText  value="#{authorFrontDoorMessages.assessment_title} " styleClass="currentSort" />
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortTitleAscending}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
  	  <!-- action=editAssessment if pass authz -->
      <h:commandLink title="#{authorFrontDoorMessages.t_editInactivePublishedAssessment}" id="editInactivePublishedAssessment" immediate="true" action="confirmEditPublishedAssessment"
        rendered="#{(authorization.editAnyAssessment or authorization.editOwnAssessment) and (!author.editPubAssessmentRestricted or !inactivePublishedAssessment.hasAssessmentGradingData)}" >
        <h:outputText id="inactivePublishedAssessmentTitle" value="#{inactivePublishedAssessment.title}" styleClass="currentSort" escape="false" />
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmEditPublishedAssessmentListener" />
      </h:commandLink>
      <h:outputText id="inactivePublishedAssessmentTitle2" value="#{inactivePublishedAssessment.title}" escape="false"
        rendered="#{(!authorization.editAnyAssessment and !authorization.editOwnAssessment) or (author.editPubAssessmentRestricted and inactivePublishedAssessment.hasAssessmentGradingData)}" />
     
	  <h:outputText value="#{authorFrontDoorMessages.asterisk_2}" rendered="#{inactivePublishedAssessment.status == 3}" styleClass="validate"/>
      <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<br />"/>

        <span class="itemAction"> 
      <!-- if passAuth, action=editPublishedAssessmentSettings -->
      <h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editPublishedAssessmentSettings_author" immediate="true"
          rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"
          action="#{author.getOutcome}">
        <h:outputText  value="#{authorFrontDoorMessages.link_settings}" />
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>

      <h:outputText value=" #{authorFrontDoorMessages.separator} " />

	  <h:commandLink title="#{authorFrontDoorMessages.t_removeAssessment}" id="confirmRemovePublishedAssessment" immediate="true" 
        rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}"
        action="#{author.getOutcome}">
        <h:outputText id="linkRemove" value="#{authorFrontDoorMessages.link_remove}"/>
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePublishedAssessmentListener" />
      </h:commandLink>

      <h:outputText value=" #{authorFrontDoorMessages.separator} "
          rendered="#{inactivePublishedAssessment.submissionSize >0 and (authorization.editAnyAssessment or authorization.editOwnAssessment)}"
      />
      <h:commandLink title="#{authorFrontDoorMessages.t_score}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore2" 
         rendered="#{inactivePublishedAssessment.submissionSize >0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
        <h:outputText value="#{authorFrontDoorMessages.link_scores}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{inactivePublishedAssessment.publishedAssessmentId}" />
      </h:commandLink>
  </span> 
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" id="sortInactivePubByreleaseToAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='releaseTo'}"  >
          <h:outputText value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort"/>
          <f:param name="inactiveSortType" value="releaseTo"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>

<h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='releaseTo' && author.inactivePublishedAscending }">
        <h:outputText  value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort" />
          
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseToDescending}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseTo}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='releaseTo' && !author.inactivePublishedAscending }">
            <h:outputText  value="#{authorFrontDoorMessages.assessment_release} " styleClass="currentSort" />
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseToAscending}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>

      <h:outputText value="#{inactivePublishedAssessment.releaseTo}" rendered="#{inactivePublishedAssessment.releaseTo ne 'Anonymous Users'}">
	       <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
	  </h:outputText>
      <h:outputText value="#{assessmentSettingsMessages.anonymous_users}" rendered="#{inactivePublishedAssessment.releaseTo eq 'Anonymous Users'}">
           <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
      </h:outputText>

    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" id="sortInactivePubByStartDateAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='startDate'}">
          <h:outputText value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort"/>
          <f:param name="inactiveSortType" value="startDate"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
 <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='startDate' && author.inactivePublishedAscending }">
        <h:outputText  value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort" />
         
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseDateDescending}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortReleaseDate}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='startDate' && !author.inactivePublishedAscending }">
              <h:outputText  value="#{authorFrontDoorMessages.assessment_date} " styleClass="currentSort" />
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortReleaseDateAscending}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{inactivePublishedAssessment.startDate}" >
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" id="sortInactiveByDueDateAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='dueDate'}">
          <h:outputText value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort"/>
          <f:param name="inactiveSortType" value="dueDate"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
<h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='dueDate' && author.inactivePublishedAscending }">
        <h:outputText  value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort"/>
          
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortDueDateDescending}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{authorFrontDoorMessages.t_sortDueDate}" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='dueDate'&& !author.inactivePublishedAscending }">
           <h:outputText  value="#{authorFrontDoorMessages.assessment_due} " styleClass="currentSort"/>
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{authorFrontDoorMessages.alt_sortDueDateAscending}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{inactivePublishedAssessment.dueDate}" >
        <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>

	<h:column>
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>

  	  <h:outputText value="#{inactivePublishedAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{inactivePublishedAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_no_sec}"/>
      </h:outputText>
    </h:column>
  </h:dataTable>

  <h:panelGrid columns="1">
    <h:outputText value="#{authorFrontDoorMessages.asterisk_2} #{authorFrontDoorMessages.retracted_for_edit}" rendered="#{author.isAnyAssessmentRetractForEdit == true}" styleClass="validate"/>
  </h:panelGrid>

</div>
</div>

</h:form>
<!-- end content -->
	  </div>
      </body>
    </html>
  </f:view>
