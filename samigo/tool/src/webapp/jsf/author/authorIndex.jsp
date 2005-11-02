<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorFrontDoorMessages"
     var="msg"/>
     <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.auth_front_door}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
      <div class="portletBody">

<!-- content... -->

<h:form id="authorIndexForm">
  <!-- HEADINGS -->
   <%@ include file="/jsf/author/assessmentHeadings.jsp" %>

  <h3>
    <h:outputText value="#{msg.assessments}"/>
  </h3>
<p>
<f:verbatim><font color="red"></f:verbatim>
  <h:messages styleClass="validation"/>
<f:verbatim></font></f:verbatim>

</p>

<h:message for="authorIndexForm:create_assessment_denied" styleClass="validate"/>
<h:message for="authorIndexForm:edit_assessment_denied" styleClass="validate"/>
<h:message for="authorIndexForm:grade_assessment_denied" styleClass="validate"/>

<div class="indnt1">
	<h4><h:outputText value="#{msg.assessment_new}" rendered="#{authorization.createAssessment}" /></h4>
  <div class="indnt2">
<h5 class="plain">

    <h:outputText value="#{msg.assessment_create}" styleClass="form_label" rendered="#{authorization.createAssessment}" />
   </h5>
   <div class="shorttext">
    <h:outputLabel value="#{msg.assessment_choose}" styleClass="form_label" rendered="#{authorization.createAssessment}" />

      <h:selectOneMenu id="assessmentTemplate" rendered="#{authorization.createAssessment}"
        value="#{author.assessmentTemplateId}">
         <f:selectItem itemValue="" itemLabel="select..."/>
         <f:selectItems value="#{author.assessmentTemplateList}" />
      </h:selectOneMenu>

      <h:outputText value="#{msg.optional_paren}" styleClass="form_label" rendered="#{authorization.createAssessment}" />
      <br/>
   </div>
<div class="shorttext">
    <h:outputLabel value="#{msg.assessment_title}" rendered="#{authorization.createAssessment}"/>
    <h:inputText id="title" value="#{author.assessTitle}" size="32" required="true" rendered="#{authorization.createAssessment}">
    <!-- AuthorAssessmentListener.createAssessment() read param from AuthorBean to
      create the assessment  -->
    </h:inputText>
    <!-- action=createAssessment if privilege is granted, otherwise =author --> 
    <h:commandButton type="submit" value="#{msg.button_create}" action="#{author.getOutcome}" rendered="#{authorization.createAssessment}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
    </h:commandButton>
    <br />
    <br />
    <h:outputLabel value="#{msg.assessment_import}" rendered="#{authorization.createAssessment}"/>
    <h:commandButton value="#{msg.button_import}" immediate="true" type="submit" 
      rendered="#{authorization.createAssessment}"
      action="importAssessment">
    </h:commandButton>
</div></div>
	<!-- CORE ASSESSMENTS-->

  <h4><h:outputText value="#{msg.assessment_core}" rendered="#{authorization.adminCoreAssessment}"/></h4>
<div class="indnt2">
  <h:dataTable styleClass="listHier" id="coreAssessments" value="#{author.assessments}" 
     var="coreAssessment" rendered="#{authorization.adminCoreAssessment}">
    <h:column>
      <f:facet name="header">
          <h:panelGroup>
        <h:commandLink id="sortCoreByTitleAction" immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy!='title'}">
          <h:outputText value="#{msg.assessment_title} " rendered="#{author.coreAssessmentOrderBy!='title'}" />
          <f:param name="coreSortType" value="title"/>
          <f:param name="coreAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
        </h:commandLink>
          <h:outputText  value="#{msg.assessment_title} " styleClass="currentSort" rendered="#{author.coreAssessmentOrderBy=='title'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='title' && author.coreAscending }">
           <f:param name="coreAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.coreAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='title'&& !author.coreAscending }">
           <f:param name="coreAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.coreAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <!-- action=editAssessment if pass authz -->
      <h:commandLink id="editAssessment" immediate="true" action="#{author.getOutcome}"
        rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}" >
        <h:outputText id="assessmentTitle" value="#{coreAssessment.title}" />
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
      </h:commandLink>
      <h:outputText id="assessmentTitle2" value="#{coreAssessment.title}" 
        rendered="#{!authorization.editAnyAssessment and !authorization.editOwnAssessment}" />

      <f:verbatim><br/></f:verbatim>
      <!-- AuthorBean.editAssessmentSettings() prepare the edit page -->
      <!-- action=editAssessmentSettings if pass authz -->
      <f:verbatim><span class="itemAction"></f:verbatim>
      <h:commandLink id="editAssessmentSettings" immediate="true" action="#{author.getOutcome}"
         rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}">
        <h:outputText id="linkSettings" value="#{msg.link_settings}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
      </h:commandLink>
        <h:outputText value=" | " 
          rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}"/>

      <!-- action=confirmRemoveAssessment if pass authz -->
      <h:commandLink id="confirmRemoveAssessment" immediate="true" 
        rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}"
        action="#{author.getOutcome}">
        <h:outputText id="linkRemove" value="#{msg.link_remove}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemoveAssessmentListener" />
      </h:commandLink>
        <h:outputText value=" | " 
          rendered="#{authorization.deleteAnyAssessment or authorization.deleteOwnAssessment}" />

    <h:outputLink value="#"
      rendered="#{authorization.editAnyAssessment or authorization.editOwnAssessment}" 
      onclick=
      "window.open( '/samigo/jsf/qti/exportAssessment.xml?exportAssessmentId=#{coreAssessment.assessmentBaseId}','_qti_export', 'toolbar=no,menubar=yes,personalbar=no,width=600,height=190,scrollbars=no,resizable=no');"
       ><h:outputText id="linkExport" value="#{msg.link_export}"/>
      </h:outputLink>
 <f:verbatim></span></f:verbatim>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortCoreByLastModifiedDateActionA" immediate="true" action="sort"  rendered="#{author.coreAssessmentOrderBy!='lastModifiedDate'}">
          <h:outputText value="#{msg.header_last_modified_date} " rendered="#{author.coreAssessmentOrderBy!='lastModifiedDate'}" />
          <f:param name="coreSortType" value="lastModifiedDate"/>
          <f:param name="coreAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
        </h:commandLink>
       <h:outputText  value="#{msg.header_last_modified_date} " styleClass="currentSort" rendered="#{author.coreAssessmentOrderBy=='lastModifiedDate'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='lastModifiedDate' && author.coreAscending }">
           <f:param name="coreAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.coreAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.coreAssessmentOrderBy=='lastModifiedDate'&& !author.coreAscending }">
           <f:param name="coreAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortCoreAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.coreAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <!--h:panelGrid columns="4"-->
        <h:outputText id="lastModifiedDate" value="#{coreAssessment.lastModifiedDate}">
         <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
      <!--/h:panelGrid-->
    </h:column>
  </h:dataTable>
</div>

	<!-- PUBLISHED ASSESSMENTS-->
  <h4><h:outputText value="#{msg.assessment_pub}" rendered="#{authorization.adminPublishedAssessment}"/></h4>
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
<div class="indnt2">
<h5 class="plain">
  <h:outputText value="#{msg.assessment_active}" rendered="#{authorization.adminPublishedAssessment}"/>
</h5>
  <h:dataTable  styleClass="listHier" rendered="#{authorization.adminPublishedAssessment}"
    value="#{author.publishedAssessments}" var="publishedAssessment">
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortPubByTitleAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='title'}">
          <h:outputText value="#{msg.assessment_title} "  rendered="#{author.publishedAssessmentOrderBy!='title'}"/>
          <f:param name="pubSortType" value="title"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>
         <h:outputText  value="#{msg.assessment_title} " styleClass="currentSort" rendered="#{author.publishedAssessmentOrderBy=='title'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='title' && author.publishedAscending }">
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='title'&& !author.publishedAscending }">
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText id="publishedAssessmentTitle" value="#{publishedAssessment.title}" />
      <f:verbatim><br /></f:verbatim>
 <f:verbatim><span class="itemAction"></f:verbatim>
      <!-- if passAuth, action=editPublishedAssessmentSettings -->
      <h:commandLink id="editPublishedAssessmentSettings" immediate="true"
          rendered="#{authorization.publishAnyAssessment or authorization.publishOwnAssessment}"
          action="#{author.getOutcome}">
        <h:outputText  value="Settings" />
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>
<%-- This is a convenient link for Daisy, hide it for now
       <h:outputText value=" | " />
      <h:commandLink id="removeAssessment" immediate="true" action="removeAssessment">
        <h:outputText id="linkRemove" value="#{msg.link_remove}"/>
        <f:param name="publishedAssessmentId" value="#{publishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
      </h:commandLink>
--%>
      <h:outputText value=" | " 
         rendered="#{publishedAssessment.submissionSize >0 and (authorization.publishAnyAssessment or authorization.publishOwnAssessment)}"/>
      <h:commandLink action="#{author.getOutcome}" immediate="true" 
         rendered="#{publishedAssessment.submissionSize >0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">

        <h:outputText value="Scores" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{publishedAssessment.publishedAssessmentId}" />
        <f:param name="allSubmissionsT" value="3"/>
      </h:commandLink>
 <f:verbatim></span></f:verbatim>
    </h:column>

    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortPubByreleaseToAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='releaseTo'}">
          <h:outputText value="#{msg.assessment_release} " rendered="#{author.publishedAssessmentOrderBy!='releaseTo'}"/>
          <f:param name="pubSortType" value="releaseTo"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>
        <h:outputText  value="#{msg.assessment_release} " styleClass="currentSort" rendered="#{author.publishedAssessmentOrderBy=='releaseTo'}" />
        <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='releaseTo' && author.publishedAscending }">
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='releaseTo'&& !author.publishedAscending }">
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{publishedAssessment.releaseTo} " >
           <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
      </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortPubByStartDateAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='startDate'}" >
          <h:outputText value="#{msg.assessment_date} " />
          <f:param name="pubSortType" value="startDate"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>
        <h:outputText  value="#{msg.assessment_date} " styleClass="currentSort" rendered="#{author.publishedAssessmentOrderBy=='startDate'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='startDate' && author.publishedAscending }">
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='startDate'&& !author.publishedAscending }">
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{publishedAssessment.startDate}" >
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortPubByDueDateAction" immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy!='dueDate'}">
          <h:outputText value="#{msg.assessment_due} " rendered="#{author.publishedAssessmentOrderBy!='dueDate'}" />
          <f:param name="pubSortType" value="dueDate"/>
          <f:param name="publishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
        </h:commandLink>
         <h:outputText  value="#{msg.assessment_due} " styleClass="currentSort" rendered="#{author.publishedAssessmentOrderBy=='dueDate'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='dueDate' && author.publishedAscending }">
           <f:param name="publishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.publishedAssessmentOrderBy=='dueDate'&& !author.publishedAscending }">
           <f:param name="publishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortPublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.publishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{publishedAssessment.dueDate}" >
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
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

<h5 class="plain">
  <h:outputText value="#{msg.assessment_inactive}" rendered="#{authorization.adminPublishedAssessment}"/>
</h5>
  <h:dataTable  styleClass="listHier" headerClass="regHeading" 
     rendered="#{authorization.adminPublishedAssessment}"
     value="#{author.inactivePublishedAssessments}" var="inactivePublishedAssessment" 
     id="inactivePublishedAssessments">
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortInactiveByTitleAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='title'}" >
          <h:outputText value="#{msg.assessment_title} " rendered="#{author.inactivePublishedAssessmentOrderBy!='title'}"/>
          <f:param name="inactiveSortType" value="title"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
          <h:outputText  value="#{msg.assessment_title} " styleClass="currentSort" rendered="#{author.inactivePublishedAssessmentOrderBy=='title'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='title' && author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='title'&& !author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText id="inactivePublishedAssessmentTitle" value="#{inactivePublishedAssessment.title}" />
      <f:verbatim><br /></f:verbatim>
       <f:verbatim><span class="itemAction"></f:verbatim>
      <!-- if passAuth, action=editPublishedAssessmentSettings -->
      <h:commandLink id="editPublishedAssessmentSettings" immediate="true"
          rendered="#{authorization.publishAnyAssessment or authorization.publishOwnAssessment}"
          action="#{author.getOutcome}">
        <h:outputText  value="Settings" />
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>
<%-- This is a convenient link for Daisy, hide it for now
       <h:outputText value=" | " />
      <h:commandLink id="removeAssessment" immediate="true" action="removeAssessment">
        <h:outputText id="linkRemove" value="#{msg.link_remove}"/>
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
      </h:commandLink>
--%>
      <h:outputText value=" | "
          rendered="#{inactivePublishedAssessment.submissionSize >0 and (authorization.publishAnyAssessment or authorization.publishOwnAssessment)}"
      />
      <h:commandLink action="#{author.getOutcome}" immediate="true" 
         rendered="#{publishedAssessment.submissionSize >0 and (authorization.gradeAnyAssessment or authorization.gradeOwnAssessment)}">
        <h:outputText value="Scores" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{inactivePublishedAssessment.publishedAssessmentId}" />
      </h:commandLink>
 <f:verbatim></span></f:verbatim>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortInactivePubByreleaseToAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='releaseTo'}"  >
          <h:outputText value="#{msg.assessment_release} " rendered="#{author.inactivePublishedAssessmentOrderBy!='releaseTo'}" />
          <f:param name="inactiveSortType" value="releaseTo"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
        <h:outputText  value="#{msg.assessment_release} " styleClass="currentSort" rendered="#{author.inactivePublishedAssessmentOrderBy=='releaseTo'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='releaseTo' && author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='releaseTo' && !author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{inactivePublishedAssessment.releaseTo}" >
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
      </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortInactivePubByStartDateAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='startDate'}">
          <h:outputText value="#{msg.assessment_date} " rendered="#{author.inactivePublishedAssessmentOrderBy!='startDate'}"/>
          <f:param name="inactiveSortType" value="startDate"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
        <h:outputText  value="#{msg.assessment_date} " styleClass="currentSort" rendered="#{author.inactivePublishedAssessmentOrderBy=='startDate'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='startDate' && author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='startDate' && !author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{inactivePublishedAssessment.startDate}" >
         <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink id="sortInactiveByDueDateAction" immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy!='dueDate'}">
          <h:outputText value="#{msg.assessment_due} " rendered="#{author.inactivePublishedAssessmentOrderBy!='dueDate'}"/>
          <f:param name="inactiveSortType" value="dueDate"/>
          <f:param name="inactivePublishedAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
        </h:commandLink>
        <h:outputText  value="#{msg.assessment_due} " styleClass="currentSort" rendered="#{author.inactivePublishedAssessmentOrderBy=='dueDate'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='dueDate' && author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="false" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{author.inactivePublishedAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{author.inactivePublishedAssessmentOrderBy=='dueDate'&& !author.inactivePublishedAscending }">
           <f:param name="inactivePublishedAscending" value="true" />
           <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.SortInactivePublishedAssessmentListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!author.inactivePublishedAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{inactivePublishedAssessment.dueDate}" >
        <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>

    </h:column>
  </h:dataTable>
</div>
</div>

</h:form>
<!-- end content -->
	  </div>
      </body>
    </html>
  </f:view>
