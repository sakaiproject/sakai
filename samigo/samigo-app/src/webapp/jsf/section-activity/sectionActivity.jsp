<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.assessment.ui.bean.author.*"%>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{sectionActivityMessages.section_activity_report}"/></title>
      <samigo:script path="/js/eventInfo.js"/>
      <samigo:stylesheet path="/css/tool_sam.css"/>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody container-fluid">
  <h:form id="sectionActivityId">
    <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/section-activity/sectionActivityHeadings.jsp" %>
  
  <div class="page-header">
 	<h1>
      <h:outputText value="#{sectionActivityMessages.section_activity_report_colon}"/>
      <small>
        <h:outputText value="#{sectionActivity.selectedUserDisplayName}"/>
      </small>
    </h1>
  </div>

  <h:panelGroup layout="block" styleClass="form-group">
 		 <h:outputLabel value="#{sectionActivityMessages.view_student}" style="instruction"/>
 		 <h:outputText escape="false" value="&#160;" />			
 		 <h:selectOneMenu value="#{sectionActivity.selectedUser}" id="studentName" required="true" onchange="document.forms[0].submit();">
      	 	 <f:selectItems value="#{sectionActivity.displayNamesList}" />  
      	 	 <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />  
    	 </h:selectOneMenu>
  </h:panelGroup>	 
  <br/>
  <div class="tier1">
   <h:dataTable styleClass="table table-striped" value="#{sectionActivity.sectionActivityDataList}" var="pageData">
	 <!-- Title.. -->
	 <h:column rendered="#{sectionActivity.sortType != 'assessmentName'}">
	  <f:facet name="header">
	  <h:commandLink title="#{sectionActivityMessages.t_sortTitle}" id="title" action="sectionActitity" >
          <h:outputText value="#{sectionActivityMessages.assessment_name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
        <f:param name="sortBy" value="assessmentName" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
     <h:panelGroup rendered="#{!pageData.anonymousGrading}">
		<h:commandLink title ="#{sectionActivityMessages.assessment_name}" action="gradeStudentResultFromSectionActivity" immediate="true" >
		  <h:outputText value="#{pageData.assessmentName}"/>
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
    	   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
    	      <f:param name="publishedId" value="#{pageData.assessmentId}" />
        	  <f:param name="studentid" value="#{sectionActivity.selectedUser}" />
       		  <f:param name="publishedIdd" value="#{pageData.assessmentId}" />
      		  <f:param name="gradingData" value="#{pageData.assessmentGradingId}" />	
	    </h:commandLink>
     </h:panelGroup>
     <h:panelGroup rendered="#{pageData.anonymousGrading}">
         <h:outputText value="#{pageData.assessmentName}"/>
         <f:verbatim><span class="info"></f:verbatim>
            <h:graphicImage url="/images/info_icon.gif" alt="" styleClass="infoDiv"/>
            <h:panelGroup styleClass="makeLogInfo" style="display:none;z-index:2000;" >
                <h:outputText value="#{sectionActivityMessages.anon_grading_info}"/>
            </h:panelGroup>
        <f:verbatim></span></f:verbatim>
     </h:panelGroup>
     </h:column>
     
       <h:column rendered="#{sectionActivity.sortType eq 'assessmentName' && sectionActivity.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{sectionActivityMessages.t_sortTitle}" action="sectionActitity">
          <h:outputText value="#{sectionActivityMessages.assessment_name}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{sectionActivityMessages.alt_sortTitleAscending}" rendered="#{sectionActivity.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
          </h:commandLink>    
      </f:facet>
      <h:panelGroup rendered="#{!pageData.anonymousGrading}">
		<h:commandLink title ="#{sectionActivityMessages.assessment_name}" action="gradeStudentResultFromSectionActivity" immediate="true" >
		  <h:outputText value="#{pageData.assessmentName}"/>
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
    	   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
    	      <f:param name="publishedId" value="#{pageData.assessmentId}" />
        	  <f:param name="studentid" value="#{sectionActivity.selectedUser}" />
       		  <f:param name="publishedIdd" value="#{pageData.assessmentId}" />
      		  <f:param name="gradingData" value="#{pageData.assessmentGradingId}" />	
	    </h:commandLink>	
     </h:panelGroup>
     <h:panelGroup rendered="#{pageData.anonymousGrading}">
         <h:outputText value="#{pageData.assessmentName}"/>
         <f:verbatim><span class="info"></f:verbatim>
            <h:graphicImage url="/images/info_icon.gif" alt="" styleClass="infoDiv"/>
            <h:panelGroup styleClass="makeLogInfo" style="display:none;z-index:2000;" >
                <h:outputText value="#{sectionActivityMessages.anon_grading_info}"/>
            </h:panelGroup>
        <f:verbatim></span></f:verbatim>
     </h:panelGroup>
    </h:column>
    
    <h:column rendered="#{sectionActivity.sortType eq 'assessmentName' && !sectionActivity.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{sectionActivityMessages.t_sortTitle}" action="sectionActitity">
         <h:outputText value="#{sectionActivityMessages.assessment_name}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{sectionActivityMessages.alt_sortTitleDescending}" rendered="#{!sectionActivity.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
      </h:commandLink> 
      </f:facet>
	 <h:panelGroup rendered="#{!pageData.anonymousGrading}">
		<h:commandLink title ="#{sectionActivityMessages.assessment_name}" action="gradeStudentResultFromSectionActivity" immediate="true" >
		  <h:outputText value="#{pageData.assessmentName}"/>
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
		   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
    	   <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
    	      <f:param name="publishedId" value="#{pageData.assessmentId}" />
        	  <f:param name="studentid" value="#{sectionActivity.selectedUser}" />
       		  <f:param name="publishedIdd" value="#{pageData.assessmentId}" />
      		  <f:param name="gradingData" value="#{pageData.assessmentGradingId}" />	
	    </h:commandLink>	
     </h:panelGroup>
     <h:panelGroup rendered="#{pageData.anonymousGrading}">
         <h:outputText value="#{pageData.assessmentName}"/>
         <f:verbatim><span class="info"></f:verbatim>
            <h:graphicImage url="/images/info_icon.gif" alt="" styleClass="infoDiv"/>
            <h:panelGroup styleClass="makeLogInfo" style="display:none;z-index:2000;" >
                <h:outputText value="#{sectionActivityMessages.anon_grading_info}"/>
            </h:panelGroup>
        <f:verbatim></span></f:verbatim>
     </h:panelGroup>
	</h:column>
	<!-- Assessment ID... -->
	 <h:column rendered="#{sectionActivity.sortType != 'assessmentId'}">
	  <f:facet name="header">
	  <h:commandLink title="#{sectionActivityMessages.t_sortAssessmentId}" id="assessmentId" action="sectionActitity" >
          <h:outputText value="#{sectionActivityMessages.assessment_id}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
        <f:param name="sortBy" value="assessmentId" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
	 <h:panelGroup>
	  <h:outputText value="#{pageData.assessmentId}"/>
     </h:panelGroup>
	</h:column>
	
	  <h:column rendered="#{sectionActivity.sortType eq 'assessmentId' && sectionActivity.sortAscending}">
	<f:facet name="header">
        <h:commandLink title="#{sectionActivityMessages.t_sortAssessmentId}" action="sectionActitity">
          <h:outputText value="#{sectionActivityMessages.assessment_id}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{sectionActivityMessages.alt_sortAssessmentIdAscending}" rendered="#{sectionActivity.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
          </h:commandLink>    
      </f:facet>
      <h:panelGroup>
	  <h:outputText value="#{pageData.assessmentId}"/>
     </h:panelGroup>
	</h:column>
	
	  <h:column rendered="#{sectionActivity.sortType eq 'assessmentId' && !sectionActivity.sortAscending}">
	<f:facet name="header">
        <h:commandLink title="#{sectionActivityMessages.t_sortAssessmentId}" action="sectionActitity">
          <h:outputText value="#{sectionActivityMessages.assessment_id}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{sectionActivityMessages.alt_sortAssessmentIdDescending}" rendered="#{!sectionActivity.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
          </h:commandLink>    
      </f:facet>
      <h:panelGroup>
	  <h:outputText value="#{pageData.assessmentId}"/>
     </h:panelGroup>
	</h:column>
		
	 <!-- Submit Date... -->	 
	  <h:column rendered="#{sectionActivity.sortType != 'submitDate'}">
	  <f:facet name="header">
	  <h:commandLink title="#{sectionActivityMessages.t_submitDate}" id="submitDate" action="sectionActitity" >
          <h:outputText value="#{sectionActivityMessages.date_completed}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
        <f:param name="sortBy" value="submitDate" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
	 <h:panelGroup>
	  <h:outputText value="#{pageData.submitDate}">
	  <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
	  </h:outputText>	
     </h:panelGroup>
	</h:column>
	
	  <h:column rendered="#{sectionActivity.sortType eq 'submitDate' && sectionActivity.sortAscending}">
	<f:facet name="header">
        <h:commandLink title="#{sectionActivityMessages.t_submitDate}" action="sectionActitity">
          <h:outputText value="#{sectionActivityMessages.date_completed}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{sectionActivityMessages.alt_sortSubmitDateAscending}" rendered="#{sectionActivity.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
	  <h:outputText value="#{pageData.submitDate}">
	  <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
	  </h:outputText>	
     </h:panelGroup>
	</h:column>
	
	<h:column rendered="#{sectionActivity.sortType eq 'submitDate' && !sectionActivity.sortAscending}">
	<f:facet name="header">
        <h:commandLink title="#{sectionActivityMessages.t_submitDate}" action="sectionActitity">
          <h:outputText value="#{sectionActivityMessages.date_completed}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{sectionActivityMessages.alt_sortSubmitDateDescending}" rendered="#{!sectionActivity.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
          </h:commandLink>    
      </f:facet>
       <h:panelGroup>
	  <h:outputText value="#{pageData.submitDate}">
	  <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
	  </h:outputText>	
     </h:panelGroup>
	</h:column>

	<!-- Percentage... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{sectionActivityMessages.percent_correct}"/>
	  </f:facet>

	 <h:panelGroup rendered="#{!pageData.notAvailableGrade}">
	  <h:outputText value="#{pageData.roundedPercentage}"/>
	   <h:outputText value="%"/>
     </h:panelGroup>
      <h:panelGroup rendered="#{pageData.notAvailableGrade}">
	  <h:outputText value="#{sectionActivityMessages.not_available}"/>
     </h:panelGroup>
	</h:column>
	
	<!-- Score/Total Points... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{sectionActivityMessages.correct_possible}"/>
	  </f:facet>

	 <h:panelGroup rendered="#{!pageData.notAvailableGrade}">
	   <h:outputText value="#{pageData.roundedFinalScore}"/>
	   <h:outputText value="/"/>
	   <h:outputText value="#{pageData.roundedMaxScore}"/>
     </h:panelGroup>
     
      <h:panelGroup rendered="#{pageData.notAvailableGrade}">
	   <h:outputText value="#{sectionActivityMessages.not_available}"/>
     </h:panelGroup>
	</h:column>
	
	</h:dataTable>
</div>
<!-- end content -->
</h:form>
</div>
</body>
</html>
</f:view>

