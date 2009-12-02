<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
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

<script language="javascript" style="text/JavaScript">

function clickPendingSelectActionLink(field){
var insertlinkid= field.id.replace("pendingSelectAction", "pendingHiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

function clickPublishedSelectActionLink(field){
var id = field.id;
var insertlinkid= field.id.replace(/publishedSelectAction./, "publishedHiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

function clickInactivePublishedSelectActionLink(field){
var insertlinkid= field.id.replace(/inactivePublishedSelectAction./, "inactivePublishedHiddenlink");
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}


</script>

<script type="text/javascript" language="JavaScript" src="/library/js/jquery-1.1.2.js"></script>
<script type="text/javascript" language="JavaScript" src="/samigo-app/js/info.js"></script>

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

 <h:panelGrid columns="3" border="0" rendered="#{authorization.createAssessment}">
    <h:panelGroup>
	  <f:verbatim><span class="new_assessment"></f:verbatim>
      <h:outputText value="#{authorFrontDoorMessages.assessment_create}"/>
      <f:verbatim></span></f:verbatim>
	</h:panelGroup>
    <h:outputText value=" "/>
    <h:panelGroup>
	  <h:inputText id="title" value="#{author.assessTitle}" size="32" />
      <h:commandButton type="submit" value="#{authorFrontDoorMessages.button_create}" action="#{author.getOutcome}" accesskey="#{authorFrontDoorMessages.a_create}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
      </h:commandButton>
    </h:panelGroup>

    <h:outputText value=" "/>
    <h:outputText value=" "/>
	<h:selectOneRadio layout="lineDirection" value="#{author.assessCreationMode}">
      <f:selectItem itemValue="1" itemLabel="#{authorFrontDoorMessages.assessmentBuild}" />
      <f:selectItem itemValue="2" itemLabel="#{authorFrontDoorMessages.markupText}" />
    </h:selectOneRadio>

    <h:outputText value=" " rendered="#{author.showTemplateList}"/>
    <h:outputText value=" " rendered="#{author.showTemplateList}"/>
	<h:panelGroup rendered="#{author.showTemplateList}">
  	  <h:outputText value="#{authorFrontDoorMessages.assessment_choose} " />
	  <h:selectOneMenu id="assessmentTemplate" accesskey="#{authorFrontDoorMessages.a_select}" value="#{author.assessmentTemplateId}" >
        <f:selectItem itemValue="" itemLabel="#{generalMessages.select_menu}"/>
        <f:selectItems value="#{author.assessmentTemplateList}" />
      </h:selectOneMenu>
    </h:panelGroup>

    <h:panelGroup>
	  <f:verbatim><span class="new_assessment"></f:verbatim>
      <h:outputText value="#{authorFrontDoorMessages.label_or}"/>
      <f:verbatim></span></f:verbatim>
	</h:panelGroup>
    <h:outputText value=" "/>
    <h:outputText value=" "/>

    <h:outputText value=" "/>
    <h:outputText value=" "/>
    <h:outputText value=" "/>

    <h:panelGroup>
	  <f:verbatim><span class="new_assessment"></f:verbatim>
      <h:outputText value="#{authorFrontDoorMessages.assessment_import}"/>
      <f:verbatim></span></f:verbatim>
	</h:panelGroup>
    <h:outputText value=" "/>
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
  <t:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="coreAssessments" value="#{author.assessments}" var="coreAssessment" rendered="#{authorization.adminCoreAssessment}" summary="#{authorFrontDoorMessages.sum_coreAssessment}">
    <t:column headerstyleClass="selectAction" styleClass="selectAction">
      <f:facet name="header" >
	   <h:outputText value="#{authorFrontDoorMessages.select_action}"/>
	  </f:facet>
	  <h:selectOneMenu id="pendingSelectAction" value="select" onchange="clickPendingSelectActionLink(this);" >
		<f:selectItems value="#{author.pendingSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:commandLink id="pendingHiddenlink" action="#{author.getOutcome}" value="" >
	    <f:param name="editType" value="pendingAssessment" />
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
	  </h:commandLink>
	</t:column>

    <t:column headerstyleClass="titlePending" styleClass="titlePending">
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

      <h:outputText id="assessmentTitle2" value="#{coreAssessment.title}" />
    </t:column>
    <t:column headerstyleClass="lastModified" styleClass="lastModified">
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>
  	  <h:outputText value="#{coreAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{coreAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
      </h:outputText>
      
    </t:column>
  </t:dataTable>
</div>

	<!-- PUBLISHED ASSESSMENTS-->
 <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<h4>"/>
  <h:outputText value="#{authorFrontDoorMessages.assessment_pub}" rendered="#{authorization.adminPublishedAssessment}"/>
 <h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="</h4>"/>
<div class="tier2">
<h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="<h5>"/>
  <h:outputText value="#{authorFrontDoorMessages.assessment_active}" rendered="#{authorization.adminPublishedAssessment}"/>
<h:outputText escape="false" rendered="#{authorization.adminPublishedAssessment}" value="</h5>"/>
  <t:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" rendered="#{authorization.adminPublishedAssessment}"
    value="#{author.publishedAssessments}" var="publishedAssessment" summary="#{authorFrontDoorMessages.sum_publishedAssessment}">
    <t:column headerstyleClass="selectAction" styleClass="selectAction">
	  <f:facet name="header" >
	   <h:outputText value="#{authorFrontDoorMessages.select_action}"/>
	  </f:facet>
	  <%/* Because selectItem has no rendered attribute, we have to put this in selectOneMenu. So there are four set
	  of selectOneMenu because there are four cases. 
	  Note: I have tried itemDisabled but it doesn't work in IE. Javascript workaround is needed. I decide to replicate the code as 
	  this is what in the original spec */%>
	  <h:selectOneMenu id="publishedSelectAction1" value="select" onchange="clickPublishedSelectActionLink(this);" rendered="#{(author.isGradeable && publishedAssessment.submittedCount > 0) && (author.isEditable && (!author.editPubAssessmentRestricted || !publishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_scores}" itemValue="scores" />
		<f:selectItem itemLabel="#{authorMessages.action_edit}" itemValue="edit_published" />
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="publishedSelectAction2" value="select" onchange="clickPublishedSelectActionLink(this);" rendered="#{(author.isGradeable && publishedAssessment.submittedCount > 0) && !(author.isEditable && (!author.editPubAssessmentRestricted || !publishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_scores}" itemValue="scores"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="publishedSelectAction3" value="select" onchange="clickPublishedSelectActionLink(this);" rendered="#{!(author.isGradeable && publishedAssessment.submittedCount > 0) && (author.isEditable && (!author.editPubAssessmentRestricted || !publishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_edit}" itemValue="edit_published"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="publishedSelectAction4" value="select" onchange="clickPublishedSelectActionLink(this);" rendered="#{!(author.isGradeable && publishedAssessment.submittedCount > 0) && (author.isEditable && !(!author.editPubAssessmentRestricted || !publishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>

	  <h:commandLink id="publishedHiddenlink" action="#{author.getOutcome}" value="" >
	    <f:param name="editType" value="publishedAssessment" />
        <f:param name="assessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
		<f:param name="publishedId" value="#{publishedAssessment.publishedAssessmentId}" />
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:param name="allSubmissionsT" value="3"/>
	  </h:commandLink>
	</t:column>

	<t:column headerstyleClass="titlePub" styleClass="titlePub">
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

      <h:outputText id="publishedAssessmentTitle2" value="#{publishedAssessment.title}" />
    </t:column>

	<%/* In Progress */%>
	<t:column headerstyleClass="inProgress" styleClass="inProgress">
	  <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.assessment_in_progress}"/>
	  </f:facet>

	 <h:panelGroup>
	  <h:outputText value="#{publishedAssessment.inProgressCount}"/>
     </h:panelGroup>
	</t:column>

	<%/* Submitted */%>
	<t:column headerstyleClass="submitted" styleClass="submitted">
	  <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.assessment_submitted}"/>
	  </f:facet>

	 <h:panelGroup>
 	   <h:panelGroup rendered="#{publishedAssessment.submittedCount==0 or !(authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
	    <h:outputText value="#{publishedAssessment.submittedCount}"/>
       </h:panelGroup>

 	   <h:panelGroup rendered="#{publishedAssessment.submittedCount>0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
         <h:commandLink title="#{authorFrontDoorMessages.t_score}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore1" >
		   <h:outputText value="#{publishedAssessment.submittedCount}" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
           <f:param name="publishedId" value="#{publishedAssessment.publishedAssessmentId}" />
           <f:param name="allSubmissionsT" value="3"/>
           </h:commandLink>
       </h:panelGroup>
     </h:panelGroup>
    </t:column>

	<t:column headerstyleClass="releaseTo" styleClass="releaseTo">
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


      <h:outputText value="#{authorFrontDoorMessages.entire_site}" rendered="#{publishedAssessment.releaseTo ne 'Anonymous Users' && publishedAssessment.releaseTo ne 'Selected Groups'}" />
      <h:outputText value="#{authorFrontDoorMessages.selected_groups}" rendered="#{publishedAssessment.releaseTo eq 'Selected Groups'}"/>
	  <f:verbatim><span class="info"></f:verbatim>
	  <h:graphicImage id="infoImg" url="/images/info_icon.gif" alt="" rendered="#{publishedAssessment.releaseTo eq 'Selected Groups'}" styleClass="infoDiv"/>

	  <t:dataTable var="releaseToGroups" styleClass="makeInfo" style="display:none;z-index:2000;" value="#{publishedAssessment.releaseToGroupsList}" 
		rendered="#{publishedAssessment.releaseTo eq 'Selected Groups'}">
		<t:column>
	 	    <f:verbatim><span class="whiteSpaceNoWrap"></f:verbatim>
			<h:outputText value="#{releaseToGroups}" />
		    <f:verbatim></span></f:verbatim>
		</t:column>
	  </t:dataTable>
	  <f:verbatim></span></f:verbatim>

    </t:column>

    <t:column headerstyleClass="releaseDate" styleClass="releaseDate">
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
    </t:column>
   
	<t:column headerstyleClass="dueDate" styleClass="dueDate">
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
    </t:column>

    <t:column headerstyleClass="lastModified" styleClass="lastModified">
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>

  	  <h:outputText value="#{publishedAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{publishedAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
      </h:outputText>
    </t:column>

  </t:dataTable>

  <!--inactive-->
<h5>
  <h:outputText value="#{authorFrontDoorMessages.assessment_inactive}" rendered="#{authorization.adminPublishedAssessment}"/>
</h5>
  <t:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" rendered="#{authorization.adminPublishedAssessment}"
     value="#{author.inactivePublishedAssessments}" var="inactivePublishedAssessment" summary="#{authorFrontDoorMessages.sum_inactiveAssessment}"
     id="inactivePublishedAssessments">
	 <t:column headerstyleClass="selectAction" styleClass="selectAction">
      <f:facet name="header" >
	   <h:outputText value="#{authorFrontDoorMessages.select_action}"/>
	  </f:facet>
	<h:selectOneMenu id="inactivePublishedSelectAction1" value="select" onchange="clickInactivePublishedSelectActionLink(this);" rendered="#{(author.isGradeable && inactivePublishedAssessment.submittedCount > 0) && (author.isEditable && (!author.editPubAssessmentRestricted || !inactivePublishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_scores}" itemValue="scores" />
		<f:selectItem itemLabel="#{authorMessages.action_edit}" itemValue="edit_published" />
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="inactivePublishedSelectAction2" value="select" onchange="clickInactivePublishedSelectActionLink(this);" rendered="#{(author.isGradeable && inactivePublishedAssessment.submittedCount > 0) && !(author.isEditable && (!author.editPubAssessmentRestricted || !inactivePublishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_scores}" itemValue="scores"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="inactivePublishedSelectAction3" value="select" onchange="clickInactivePublishedSelectActionLink(this);" rendered="#{!(author.isGradeable && inactivePublishedAssessment.submittedCount > 0) && (author.isEditable && (!author.editPubAssessmentRestricted || !inactivePublishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItem itemLabel="#{authorMessages.action_edit}" itemValue="edit_published"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>
	  <h:selectOneMenu id="inactivePublishedSelectAction4" value="select" onchange="clickInactivePublishedSelectActionLink(this);" rendered="#{!(author.isGradeable && inactivePublishedAssessment.submittedCount > 0) && (author.isEditable && !(!author.editPubAssessmentRestricted || !inactivePublishedAssessment.hasAssessmentGradingData))}">
		<f:selectItem itemLabel="#{authorMessages.select_action}" itemValue="select"/>
		<f:selectItems value="#{author.publishedSelectActionList}" />
		<f:valueChangeListener	type="org.sakaiproject.tool.assessment.ui.listener.author.ActionSelectListener" />
	  </h:selectOneMenu>

	  <h:commandLink id="inactivePublishedHiddenlink" action="#{author.getOutcome}" value="" >
	    <f:param name="editType" value="publishedAssessment" />
        <f:param name="assessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
		<f:param name="publishedId" value="#{inactivePublishedAssessment.publishedAssessmentId}" />
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:param name="allSubmissionsT" value="3"/>
	  </h:commandLink>
	</t:column>

	<t:column headerstyleClass="titlePub" styleClass="titlePub">
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

      <h:outputText id="inactivePublishedAssessmentTitle2" value="#{inactivePublishedAssessment.title}" />
	  <h:outputText value="#{authorFrontDoorMessages.asterisk_2}" rendered="#{inactivePublishedAssessment.status == 3}" styleClass="validate"/>
    </t:column>

	<%/* In Progress */%>
	<t:column headerstyleClass="inProgress" styleClass="inProgress">
	  <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.assessment_in_progress}"/>
	  </f:facet>

	 <h:panelGroup>
	  <h:outputText value="#{inactivePublishedAssessment.inProgressCount}"/>
     </h:panelGroup>
    </t:column>

	<%/* Submitted */%>
	<t:column headerstyleClass="submitted" styleClass="submitted">
	  <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.assessment_submitted}"/>
	  </f:facet>

	 <h:panelGroup>
 	   <h:panelGroup rendered="#{inactivePublishedAssessment.submittedCount==0 or !(authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
	    <h:outputText value="#{inactivePublishedAssessment.submittedCount}"/>
       </h:panelGroup>

 	   <h:panelGroup rendered="#{inactivePublishedAssessment.submittedCount>0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
         <h:commandLink title="#{authorFrontDoorMessages.t_score}" action="#{author.getOutcome}" immediate="true" id="authorIndexToScore2" >
		   <h:outputText value="#{inactivePublishedAssessment.submittedCount}" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
           <f:param name="publishedId" value="#{inactivePublishedAssessment.publishedAssessmentId}" />
           <f:param name="allSubmissionsT" value="3"/>
           </h:commandLink>
       </h:panelGroup>
     </h:panelGroup>
    </t:column>

	<t:column headerstyleClass="releaseTo" styleClass="releaseTo">
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

	  <h:outputText value="#{authorFrontDoorMessages.entire_site}" rendered="#{inactivePublishedAssessment.releaseTo ne 'Anonymous Users' && inactivePublishedAssessment.releaseTo ne 'Selected Groups'}" />
      <h:outputText value="#{authorFrontDoorMessages.selected_groups}" rendered="#{inactivePublishedAssessment.releaseTo eq 'Selected Groups'}" />
  	  <f:verbatim><span class="info"></f:verbatim>
	  <h:graphicImage id="infoImg" url="/images/info_icon.gif" alt="" rendered="#{inactivePublishedAssessment.releaseTo eq 'Selected Groups'}" styleClass="infoDiv"/>

	  <t:dataTable var="releaseToGroups" styleClass="makeInfo" style="display:none;z-index:2000;" value="#{inactivePublishedAssessment.releaseToGroupsList}" 
		rendered="#{inactivePublishedAssessment.releaseTo eq 'Selected Groups'}">
		<t:column>
	 	    <f:verbatim><span class="whiteSpaceNoWrap"></f:verbatim>
			<h:outputText value="#{releaseToGroups}" />
			<f:verbatim></span></f:verbatim>
		</t:column>
	  </t:dataTable>
	  <f:verbatim></span></f:verbatim>

    </t:column>

	<t:column headerstyleClass="releaseDate" styleClass="releaseDate">
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
    </t:column>
	
	<t:column headerstyleClass="dueDate" styleClass="dueDate">
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
    </t:column>

    <t:column headerstyleClass="lastModified" styleClass="lastModified">
      <f:facet name="header">
        <h:outputText value="#{authorFrontDoorMessages.header_last_modified}"/>
	  </f:facet>

  	  <h:outputText value="#{inactivePublishedAssessment.lastModifiedBy}" />
      <h:outputText escape="false" value="<br />"/>
      <h:outputText value="#{inactivePublishedAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
      </h:outputText>
    </t:column>
  </t:dataTable>

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
