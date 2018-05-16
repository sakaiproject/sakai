<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ECL-2.0
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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{selectIndexMessages.page_title}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
      
      <!-- IF A SECURE DELIVERY MODULES ARE AVAILABLE, INJECT THEIR INITIAL HTML FRAGMENTS HERE -->
	  <h:outputText  value="#{select.secureDeliveryHTMLFragments}" escape="false" />

<!--JAVASCRIPT -->
<script type="text/JavaScript">
var linksDisabled = 'false';
function disableLinks(clickedLink){
	//alert("clickedLink id = " + clickedLink.id);
	if (linksDisabled == 'false') {
		linksDisabled = 'true';
		//alert("document.links.length" + document.links.length);
		var linkIds = new Array();
		for (var i=0; i < document.links.length; i++){
			//alert("document.links[" + i + "].id=" + document.links[i].id);
			linkIds[i] = document.links[i].id;
		}

		for (var i=0; i < linkIds.length; i++){
			if (linkIds[i].indexOf('selectIndexForm') >= 0) {
				//alert("disabling..." + linkIds[i]);
				if (linkIds[i] != clickedLink.id) {
					//alert("disabling..." + linkIds[i]);
					var obj = document.getElementById(linkIds[i]);
					var onclick = obj.getAttribute("onclick");
					if(onclick != null)
					{
						obj.setAttribute('onclick_back', onclick);
						obj.setAttribute('onclick', "void(0);");
					}
					obj.removeAttribute('href'); 
				}
			}
		}
    }
	else {
		//alert('linksDisabled == true');
		var onclick = clickedLink.getAttribute("onclick");
		if(onclick != null)
		{
			clickedLink.setAttribute('onclick_back', onclick);
			clickedLink.setAttribute('onclick', "void(0);");
		}
		clickedLink.removeAttribute('href');
	}
}
</script>

<!-- content... -->
<div class="portletBody container-fluid">
  <div class="page-header">
    <h1>
      <h:outputText value="#{selectIndexMessages.page_heading}"/>
    </h1>
  </div>

  <h:form id="selectIndexForm">
  <!-- SELECT -->
  <div class="submission-container">
    <h2>
      <h:outputText value="#{selectIndexMessages.take_assessment}" />
    </h2>

    <div class="info-text">
      <h:outputText rendered="#{select.isThereAssessmentToTake eq 'true'}" value="#{selectIndexMessages.take_assessment_notes}" />
      <h:outputText rendered="#{select.isThereAssessmentToTake eq 'false'}" value="#{selectIndexMessages.take_assessment_notAvailable}" />
    </div>

<%--
sorting actions for table:

* Sort by: Title
* Sort by: Date Due
--%>
  <!-- SELECT TABLE -->
  <div class="tier2">
  <h:dataTable id="selectTable" rendered="#{select.isThereAssessmentToTake eq 'true'}" value="#{select.takeableAssessments}" var="takeable" styleClass="table table-striped" summary="#{selectIndexMessages.sum_availableAssessment}">
    <h:column headerClass="assessmentTitleHeader">
      <f:facet name="header">
          <h:outputText  value="#{selectIndexMessages.title} " />
      </f:facet>

      <h:commandLink title="#{selectIndexMessages.t_takeAssessment}" id="takeAssessment" action="beginAssessment" onmouseup="disableLinks(this);">
        <f:param name="publishedId" value="#{takeable.assessmentId}" />
        <f:param name="actionString" value="takeAssessment"/>
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
        <h:outputText value="#{takeable.assessmentTitle}" escape="false"/>
      </h:commandLink>
      <h:outputText value="#{selectIndexMessages.assessment_updated_need_resubmit}" rendered="#{takeable.assessmentUpdatedNeedResubmit}" styleClass="validate" />	
      <h:outputText value="#{selectIndexMessages.assessment_updated}" rendered="#{takeable.assessmentUpdated}" styleClass="validate" />		
    </h:column>
    <h:column headerClass="assessmentTimeLimitHeader">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{selectIndexMessages.t_time_limit} " styleClass="currentSort"  />
        </h:panelGroup>
      </f:facet>
	
	<h:outputText value="#{takeable.timeLimit_hour} #{selectIndexMessages.hour} #{takeable.timeLimit_minute} #{selectIndexMessages.minutes}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour != 0 && takeable.timeLimit_minute != 0}"  escape="false"/>
	<h:outputText value="#{takeable.timeLimit_hour} #{selectIndexMessages.hour}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour != 0 && takeable.timeLimit_minute == 0}"  escape="false"/>
	<h:outputText value="#{takeable.timeLimit_minute} #{selectIndexMessages.minutes}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour == 0 && takeable.timeLimit_minute != 0}"  escape="false"/>
	<h:outputText value="#{selectIndexMessages.na}" styleClass="currentSort"  rendered="#{takeable.timeLimit_hour == 0 && takeable.timeLimit_minute == 0}"  escape="false"/>
	
   </h:column>
    <h:column headerClass="assessmentDueDateHeader">
      <f:facet name="header">
          <h:outputText value="#{selectIndexMessages.date_due} " />
      </f:facet>
      <h:outputText value="#{selectIndexMessages.na}" rendered="#{takeable.dueDate == null}" />
      <h:outputText value="#{takeable.dueDateString}" style="color: red;" rendered="#{takeable.pastDue}">
      </h:outputText>
      <h:outputText value="#{takeable.dueDateString}" rendered="#{!takeable.pastDue}">
      </h:outputText>
    </h:column>
  </h:dataTable>
  </div></div>
  
<!-- SUBMITTED ASSESMENTS -->
  <h2> 
    <h:outputText value="#{selectIndexMessages.submitted_assessments}" />
  </h2>
  <div class="info-text">
	  <h:outputText rendered="#{select.isThereAssessmentToReview eq 'true'}" value="#{selectIndexMessages.review_assessment_notes}" />
	  <h:outputText rendered="#{select.isThereAssessmentToReview ne 'true'}" value="#{selectIndexMessages.review_assessment_notAvailable}" />
  </div>
	  
<t:div rendered="#{select.isThereAssessmentToReview eq 'true'}" styleClass="panel panel-default sam-submittedPanel">
	<t:div rendered="#{select.displayAllAssessments == 2}" styleClass="panel-heading sam-reviewHeaderTabs"> <%-- on the all submissions/score tab --%>
		<span><h:outputText value="#{selectIndexMessages.review_assessment_all}" rendered="#{select.displayAllAssessments == 2}" /></span>
		<h:commandLink 
			id="some"
			value="#{selectIndexMessages.review_assessment_recorded}"
			rendered="#{select.displayAllAssessments == 2}" styleClass="sam-leftSep"
			onmouseup="disableLinks(this);submit();">
			<f:param name="selectSubmissions" value="1" />
			<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
		</h:commandLink>
	</t:div>
	<t:div rendered="#{select.displayAllAssessments == 1}" styleClass="panel-heading sam-reviewHeaderTabs"> <%-- on the only recorded scores tab --%>
		<h:commandLink
			id="all"
			value="#{selectIndexMessages.review_assessment_all}" rendered="#{select.displayAllAssessments == 1}" onmouseup="disableLinks(this);submit();">
			<f:param name="selectSubmissions" value="2" />
			<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
		</h:commandLink>
		<span class="sam-leftSep"><h:outputText value="#{selectIndexMessages.review_assessment_recorded}" rendered="#{select.displayAllAssessments == 1}" /></span>
	</t:div>

  <!-- REVIEW TABLE -->
<%--
sorting actions for table:

* Sort by: Grade
* Sort by: Raw Score
* Sort by: Statistics
* Sort by: Submitted
* Sort by: Time
* Sort by: Title
--%>
  <div class="table-responsive">
    <h:dataTable styleClass="table table-striped" id="reviewTable" value="#{select.reviewableAssessments}" var="reviewable" summary="#{selectIndexMessages.sum_submittedAssessment}">

<%-- TITLE --%>
    <h:column>
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{selectIndexMessages.title} " styleClass="currentSort"  />
        </h:panelGroup>
      </f:facet>
	
	<h:outputText value="#{reviewable.assessmentTitle}" styleClass="currentSort"  rendered="#{reviewable.isRecordedAssessment}"  escape="false"/>
	<h:outputText value="#{selectIndexMessages.asterisk}" rendered="#{reviewable.isRecordedAssessment && reviewable.feedback == 'show' && !reviewable.isAssessmentRetractForEdit && reviewable.hasAssessmentBeenModified && select.warnUserOfModification}" styleClass="validate"/> 
	<h:outputText value="#{selectIndexMessages.asterisk_2}" rendered="#{reviewable.isRecordedAssessment && reviewable.isAssessmentRetractForEdit}" styleClass="validate" />
	
   </h:column>
	
	<!-- STATS creating separate column for stats -->
	<h:column>
	  <f:facet name="header"> 
	       <h:panelGroup>
	          <h:outputText value="#{selectIndexMessages.stats}" styleClass="currentSort"  />
	      </h:panelGroup>
	  </f:facet> 
	  <h:panelGroup>
	    <h:commandLink title="#{selectIndexMessages.t_histogram}" id="histogram"  action="#{delivery.getOutcome}" immediate="true"  
	        rendered="#{reviewable.feedback eq 'show' && reviewable.feedbackComponentOption == '2' && reviewable.statistics && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}" onmouseup="disableLinks(this);">
          <f:param name="publishedAssessmentId" value="#{reviewable.assessmentId}" />
          <f:param name="hasNav" value="false"/>
          <f:param name="allSubmissions" value="true" />
          <f:param name="actionString" value="reviewAssessment"/>
          <f:param name="isFromStudent" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
          <h:outputText value="#{selectIndexMessages.stats} "/>
        </h:commandLink>
	  </h:panelGroup>
	   <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedback eq 'na' ||  reviewable.feedbackComponentOption == '1' || reviewable.isAssessmentRetractForEdit || !reviewable.statistics) && reviewable.isRecordedAssessment}" />
	</h:column>
	<!-- created separate column for statistics  -->
   	
    <%-- Recorded SCORE --%>
	<h:column>
	  <f:facet name="header">
	    <h:panelGroup>
	      <h:outputText value="#{selectIndexMessages.recorded_score}" styleClass="currentSort" />
	    </h:panelGroup>
	  </f:facet>
	  
	  <h:outputText value="#{reviewable.roundedRawScoreToDisplay} " styleClass="currentSort" rendered="#{reviewable.showScore eq 'show' && reviewable.isRecordedAssessment && !reviewable.isAssessmentRetractForEdit}" />
	  <h:outputText value="" rendered="#{!reviewable.isRecordedAssessment && reviewable.showScore eq 'show' && !reviewable.isAssessmentRetractForEdit}"/>   
	  <h:outputText value="#{selectIndexMessages.highest_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '1' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/> 
	  <h:outputText value="#{selectIndexMessages.last_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '2' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/>
	  <h:outputText value="#{selectIndexMessages.average_score}" rendered="#{(reviewable.multipleSubmissions eq 'true' && reviewable.isRecordedAssessment && reviewable.scoringOption eq '4' && (reviewable.showScore eq 'show' || reviewable.showScore eq 'blank')) && !reviewable.isAssessmentRetractForEdit}"/>
	  <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) && reviewable.isRecordedAssessment}" />
    </h:column>

<%-- FEEDBACK DATE --%>
    <h:column>
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{selectIndexMessages.feedback_date}" styleClass="currentSort"  />
        </h:panelGroup>
      </f:facet>

	  <h:outputText value="#{reviewable.feedbackDateString}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  && reviewable.feedbackDelivery eq '2' && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}" />
      <h:outputText value="#{selectIndexMessages.immediate}" styleClass="currentSort" rendered="#{reviewable.feedbackComponentOption == '2'  && (reviewable.feedbackDelivery eq '1' || reviewable.feedbackDelivery eq '4') && !reviewable.isAssessmentRetractForEdit && reviewable.isRecordedAssessment}" />    
	  <h:outputText value="#{selectIndexMessages.not_applicable}" styleClass="currentSort" rendered="#{(reviewable.feedbackComponentOption == '1' || reviewable.feedbackDelivery==null  || reviewable.feedbackDelivery eq '3' || reviewable.isAssessmentRetractForEdit) && reviewable.isRecordedAssessment}" />
	  
	   <!-- mustansar -->
	  <h:commandLink title="#{selectIndexMessages.t_reviewAssessment}" action="#{delivery.getOutcome}" immediate="true"  
	        rendered="#{reviewable.feedback == 'show' && reviewable.feedbackComponentOption == '2' && !reviewable.isAssessmentRetractForEdit && select.displayAllAssessments != '1' && !reviewable.isRecordedAssessment }" onmouseup="disableLinks(this);">
	    <f:param name="publishedId" value="#{reviewable.assessmentId}" />
	    <f:param name="assessmentGradingId" value="#{reviewable.assessmentGradingId}" />
	    <f:param name="nofeedback" value="false"/>
	    <f:param name="actionString" value="reviewAssessment"/>
	    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
		<h:outputText styleClass="currentSort" value="#{commonMessages.feedback}" rendered="#{reviewable.isRecordedAssessment && select.displayAllAssessments != '1' }" escape="false"/> 
		<h:outputText value="#{commonMessages.feedback}" rendered="#{!reviewable.isRecordedAssessment }" escape="false"/> 
	  </h:commandLink> 
      <!-- mustansar --> 
    </h:column>

<%-- SCORE --%>
    <h:column rendered="#{select.displayAllAssessments != '1'}">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{selectIndexMessages.individual_score}" styleClass="currentSort" />
        </h:panelGroup>
      </f:facet>

      <h:outputText value="#{reviewable.roundedRawScoreToDisplay} " rendered="#{(reviewable.showScore eq 'show' && !reviewable.isAssessmentRetractForEdit) && !reviewable.isRecordedAssessment}" />
      <h:outputText value="#{selectIndexMessages.not_applicable}" rendered="#{(reviewable.showScore eq 'na' || reviewable.isAssessmentRetractForEdit) && !reviewable.isRecordedAssessment}" />
    </h:column>

<%-- TIME --%>
    <h:column rendered="#{select.displayAllAssessments != '1'}">
      <f:facet name="header">
       <h:panelGroup>
        <h:outputText value="#{selectIndexMessages.time} " styleClass="currentSort"  />
        </h:panelGroup>
      </f:facet>

      <h:panelGroup>
        <h:outputText id="timeElapse" value="#{reviewable.timeElapse}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}" />
        <h:outputText value="#{reviewable.timeElapse}" rendered="#{!reviewable.isRecordedAssessment}" />
      </h:panelGroup>
    </h:column>

<%-- SUBMITTED --%>
    <h:column rendered="#{select.displayAllAssessments != '1'}" >
      <f:facet name="header">
       <h:panelGroup>
        <h:outputText value="#{selectIndexMessages.submitted} " styleClass="currentSort"  />
        </h:panelGroup>
      </f:facet>

      <h:outputText value="#{reviewable.submissionDateString}" styleClass="currentSort" rendered="#{reviewable.isRecordedAssessment}" />
	  <h:outputText value="#{reviewable.submissionDateString}" rendered="#{!reviewable.isRecordedAssessment}" />
	</h:column>
	    
  </h:dataTable>

  <t:div styleClass="sam-asterisks-row" rendered="#{(select.hasAnyAssessmentBeenModified && select.warnUserOfModification) || select.hasAnyAssessmentRetractForEdit}">
  <h:outputText value="#{selectIndexMessages.asterisk} #{selectIndexMessages.has_been_modified}" rendered="#{select.hasAnyAssessmentBeenModified && select.warnUserOfModification}" styleClass="validate"/>
  <h:outputText value="#{selectIndexMessages.asterisk_2} #{selectIndexMessages.currently_being_edited}" rendered="#{select.hasAnyAssessmentRetractForEdit}" styleClass="validate"/>
  </t:div>

  </div></t:div>
  </h:form>
</div>
  <!-- end content -->
      </body>
    </html>
