<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: authorIndex.jsp,v 1.80 2005/05/30 06:31:09 esmiley.stanford.edu Exp $ -->
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
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->

<h:form id="authorIndexForm">
  <!-- HEADINGS -->
  <p class="navIntraTool">
      <h:outputText value="#{msg.global_nav_assessmt}"/>
    <h:outputText value=" | " />
    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </p>
  <h3>
    <h:outputText value="#{msg.assessments}"/>
  </h3>
     <div class="indnt1">
	<h4><h:outputText value="#{msg.assessment_new}" /></h4>
  <div class="indnt2">
<h5 class="plain">

    <h:outputText value="#{msg.assessment_create}" styleClass="form_label" />
   </h5>
   <div class="shorttext">
    <h:outputLabel value="#{msg.assessment_choose}" styleClass="form_label" />

      <h:selectOneMenu id="assessmentTemplate"
        value="#{author.assessmentTemplateId}">
         <f:selectItem itemValue="" itemLabel="select..."/>
         <f:selectItems value="#{author.assessmentTemplateList}" />
      </h:selectOneMenu>

      <h:outputText value="#{msg.optional_paren}" styleClass="form_label" />
      <br/>
   </div>
<div class="shorttext">
    <h:outputLabel value="#{msg.assessment_title}" />
    <h:inputText id="title" value="#{author.assessTitle}" size="32" required="true">
    <!-- AuthorAssessmentListener.createAssessment() read param from AuthorBean to
      create the assessment  -->
    </h:inputText>
    <h:commandButton type="submit" value="#{msg.button_create}" action="createAssessment">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
    </h:commandButton>
    <br />
    <br />
    <h:outputLabel value="#{msg.assessment_import}" />
    <h:commandButton value="#{msg.button_import}" immediate="true" type="submit"
      action="importAssessment">
    </h:commandButton>


	<!-- CORE ASSESSMENTS-->
<div class="indnt1">
  <h4><h:outputText value="#{msg.assessment_core}" /></h4>
<div class="indnt2">
  <h:dataTable styleClass="listHier" id="coreAssessments" value="#{author.assessments}" var="coreAssessment">
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
      <h:commandLink id="editAssessment" immediate="true" action="editAssessment">
        <h:outputText id="assessmentTitle" value="#{coreAssessment.title}" />
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
      </h:commandLink>
      <f:verbatim><br/></f:verbatim>
      <!-- AuthorBean.editAssessmentSettings() prepare the edit page -->
      <f:verbatim><span class="itemAction"></f:verbatim>
      <h:commandLink id="editAssessmentSettings" immediate="true" action="editAssessmentSettings">
        <h:outputText id="linkSettings" value="#{msg.link_settings}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
      </h:commandLink>
        <h:outputText value=" | " />
      <h:commandLink id="confirmRemoveAssessment" immediate="true" action="confirmRemoveAssessment">
        <h:outputText id="linkRemove" value="#{msg.link_remove}"/>
        <f:param name="assessmentId" value="#{coreAssessment.assessmentBaseId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemoveAssessmentListener" />
      </h:commandLink>
        <h:outputText value=" | " />

    <h:outputLink value="#"
      onclick=
      "window.open( '/samigo/jsf/qti/exportAssessment.faces?exportAssessmentId=#{coreAssessment.assessmentBaseId}','_qti_import', 'toolbar=no,menubar=yes,personalbar=no,width=650,height=375,scrollbars=no,resizable=no');"
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
</div>

	<!-- PUBLISHED ASSESSMENTS-->
<div class="indnt1">
  <h4><h:outputText value="#{msg.assessment_pub}" /></h4>
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
  <h:outputText value="#{msg.assessment_active}" />
</h5>
  <h:dataTable  styleClass="listHier" width="100%"
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
      <h:commandLink id="editPublishedAssessmentSettings" immediate="true"
          action="editPublishedAssessmentSettings">
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
      <h:outputText rendered="#{publishedAssessment.submissionSize >0}" value=" | "/>
      <h:commandLink action="totalScores" immediate="true" rendered="#{publishedAssessment.submissionSize >0}">

        <h:outputText value="Scores" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{publishedAssessment.publishedAssessmentId}" />
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
  <h:outputText value="#{msg.assessment_inactive}" />
</h5>
  <h:dataTable  styleClass="listHier" width="100%" headerClass="regHeading"
    value="#{author.inactivePublishedAssessments}" var="inactivePublishedAssessment" id="inactivePublishedAssessments">
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
      <h:commandLink id="editPublishedAssessmentSettings" immediate="true"
          action="editPublishedAssessmentSettings">
        <h:outputText  value="Settings" />
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>
 <f:verbatim></span></f:verbatim>
<%-- This is a convenient link for Daisy, hide it for now
       <h:outputText value=" | " />
      <h:commandLink id="removeAssessment" immediate="true" action="removeAssessment">
        <h:outputText id="linkRemove" value="#{msg.link_remove}"/>
        <f:param name="publishedAssessmentId" value="#{inactivePublishedAssessment.publishedAssessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
      </h:commandLink>
--%>
      <h:outputText rendered="#{publishedAssessment.submissionSize >0}" value="|"/>
      <h:commandLink action="totalScores" immediate="true" rendered="#{publishedAssessment.submissionSize >0}">

        <h:outputText value="Scores" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="publishedId" value="#{inactivePublishedAssessment.publishedAssessmentId}" />
      </h:commandLink>
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
</div></div>

</h:form>
<!-- end content -->

      </body>
    </html>
  </f:view>
