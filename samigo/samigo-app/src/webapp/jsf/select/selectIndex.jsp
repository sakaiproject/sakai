<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
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
--%>
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{selectIndexMessages.page_title}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<!--JAVASCRIPT -->
<script language="javascript" type="text/JavaScript">
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
<div class="portletBody">
  <h:form id="selectIndexForm">
  <h3>
    <h:outputText value="#{selectIndexMessages.page_heading}"/>
  </h3>
  <!-- SELECT -->
 <div class="tier1">
  <h4><h:outputText value="#{selectIndexMessages.take_assessment}" /></h4>
  <p class="tier2">
    <h:outputText rendered="#{select.isThereAssessmentToTake eq 'true'}" value="#{selectIndexMessages.take_assessment_notes}" />
<h:outputText rendered="#{select.isThereAssessmentToTake eq 'false'}" value="#{selectIndexMessages.take_assessment_notAvailable}" />
  </p>
<%-- pager controls NOT required by mockups, not implemented
  <span class="rightNav">
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      prevText="Previous" nextText="Next"
      firstItem="#{select.takePager.firstItem}"
      lastItem="#{select.takePager.lastItem}"
      totalItems="#{select.takePager.totalItems}"
      numItems="#{select.takePager.numItems}" />
  </span>
  <br />
  <br />
  <br />
--%>


<%--
sorting actions for table:

* Sort by: Title
* Sort by: Date Due
--%>
  <!-- SELECT TABLE -->
  <div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" id="selectTable" value="#{select.takeableAssessments}"
    var="takeable" styleClass="listHier" summary="#{selectIndexMessages.sum_availableAssessment}">
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortTitle}" id="taketitle1" rendered="#{select.takeableSortOrder!='title'}" onmouseup="disableLinks(this);">
          <f:param name="takeableSortType" value="title" />
          <f:param name="takeAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText  value="#{selectIndexMessages.title} "  rendered="#{select.takeableSortOrder!='title'}" />
        </h:commandLink>
          <h:outputText  value="#{selectIndexMessages.title} " styleClass="currentSort" rendered="#{select.takeableSortOrder=='title'}" />
          <h:commandLink title="#{selectIndexMessages.t_sortTitle}" id="taketitle2" rendered="#{select.takeableSortOrder=='title' && select.takeableAscending }" onmouseup="disableLinks(this);">
           <f:param name="takeAscending" value="false" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
             <h:graphicImage alt="#{selectIndexMessages.alt_sortTitleDescending}" rendered="#{select.takeableAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{selectIndexMessages.t_sortTitle}" id="taketitle3" rendered="#{select.takeableSortOrder=='title'&& !select.takeableAscending }" onmouseup="disableLinks(this);">
           <f:param name="takeAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
           <h:graphicImage alt="#{selectIndexMessages.alt_sortTitleAscending}" rendered="#{!select.takeableAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
       </h:panelGroup>
      </f:facet>
      <h:commandLink title="#{selectIndexMessages.t_takeAssessment}" id="takeAssessment" action="beginAssessment" onmouseup="disableLinks(this);">
        <f:param name="publishedId" value="#{takeable.assessmentId}" />
        <f:param name="actionString" value="takeAssessment"/>
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
        <h:outputText value="#{takeable.assessmentTitle}" escape="false"/>
      </h:commandLink>
	  <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText value="#{selectIndexMessages.need_resubmit}" rendered="#{takeable.needResubmit}" styleClass="validate" />		
    </h:column>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortDueDate}" id="takedue1" rendered="#{select.takeableSortOrder!='due'}" onmouseup="disableLinks(this);">
          <f:param name="takeableSortType" value="due" />
          <f:param name="takeAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.date_due} " rendered="#{select.takeableSortOrder!='due'}" />
        </h:commandLink>
        <h:outputText value="#{selectIndexMessages.date_due} " styleClass="currentSort" rendered="#{select.takeableSortOrder=='due'}" />
        <h:commandLink title="#{selectIndexMessages.t_sortDueDate}" id="takedue2" rendered="#{select.takeableSortOrder=='due' && select.takeableAscending }" onmouseup="disableLinks(this);">
          <f:param name="takeAscending" value="false" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortDueDateDescending}" rendered="#{select.takeableAscending}" url="/images/sortascending.gif"/>
        </h:commandLink>
        <h:commandLink title="#{selectIndexMessages.t_sortDueDate}" id="takedue3" rendered="#{select.takeableSortOrder=='due'&& !select.takeableAscending }" onmouseup="disableLinks(this);">
           <f:param name="takeAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortDueDateAscending}" rendered="#{!select.takeableAscending}" url="/images/sortdescending.gif"/>
        </h:commandLink>
       </h:panelGroup>
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
<div class="tier1">

<h4> <h:outputText value="#{selectIndexMessages.submitted_assessments}" /></h4>
  <p class="tier2">
   
<h:outputText rendered="#{select.isThereAssessmentToReview eq 'true'}" value="#{selectIndexMessages.review_assessment_notes}" />
<h:outputText rendered="#{select.isThereAssessmentToReview eq 'false'}" value="#{selectIndexMessages.review_assessment_notAvailable}" />
  </p>

<%-- pager controls NOT required by mockups, not implemented
  <span class="rightNav">
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      prevText="Previous" nextText="Next"
      firstItem="#{select.reviewPager.firstItem}"
      lastItem="#{select.reviewPager.lastItem}"
      totalItems="#{select.reviewPager.totalItems}"
      numItems="#{select.reviewPager.numItems}" />
  </span>
  <br />
  <br />
  <br />
--%>

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
  <div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="reviewTable" value="#{select.reviewableAssessments}"
       var="reviewable" summary="#{selectIndexMessages.sum_submittedAssessment}">

<%-- TITLE --%>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortTitle}" id="reviewtitle"  rendered="#{select.reviewableSortOrder!='title'}" onmouseup="disableLinks(this);">
          <f:param name="reviewableSortType" value="title" />
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.title} "  rendered="#{select.reviewableSortOrder!='title'}" />
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.title} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='title'}" />
        <h:commandLink title="#{selectIndexMessages.t_sortTitle}" rendered="#{select.reviewableSortOrder=='title' && select.reviewableAscending } " onmouseup="disableLinks(this);">
          <f:param name="reviewableAscending" value="false" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortTitleDescending}" rendered="#{select.reviewableSortOrder=='title' && select.reviewableAscending}"
            url="/images/sortascending.gif"/>
        </h:commandLink>
       <h:commandLink title="#{selectIndexMessages.t_sortTitle}" rendered="#{select.reviewableSortOrder=='title' && !select.reviewableAscending } " onmouseup="disableLinks(this);">
          <f:param name="reviewableAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortTitleAscending}" rendered="#{select.reviewableSortOrder=='title' && !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
      </h:panelGroup>
      </f:facet>


    <h:outputText value="#{reviewable.assessmentTitle}" rendered="#{reviewable.feedback != 'true' || reviewable.isAssessmentRetractForEdit}" escape="false"/>

    
    <h:commandLink title="#{selectIndexMessages.t_reviewAssessment}" id="karen"  action="#{delivery.getOutcome}" immediate="true"  
        rendered="#{reviewable.feedback == 'true' && !reviewable.isAssessmentRetractForEdit}" onmouseup="disableLinks(this);">
        <f:param name="publishedId" value="#{reviewable.assessmentId}" />
        <f:param name="nofeedback" value="false"/>
        <f:param name="actionString" value="reviewAssessment"/>
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
        <h:outputText value="#{reviewable.assessmentTitle}" escape="false"/> 
    </h:commandLink>

	<h:outputText value="#{selectIndexMessages.asterisk_2}" rendered="#{reviewable.feedback == 'true' && !reviewable.isAssessmentRetractForEdit && reviewable.hasAssessmentBeenModified}" styleClass="validate"/> 

  <f:verbatim><br /></f:verbatim>
       <h:commandLink title="#{selectIndexMessages.t_histogram}" id="histogram"  action="#{delivery.getOutcome}" immediate="true"  
        rendered="#{reviewable.feedback ne 'false' && reviewable.statistics && !reviewable.hasRandomDrawPart && !reviewable.isAssessmentRetractForEdit}" onmouseup="disableLinks(this);">
        <f:param name="publishedId" value="#{reviewable.assessmentId}" />
        <f:param name="hasNav" value="false"/>
        <f:param name="allSubmissions" value="true" />
        <f:param name="actionString" value="reviewAssessment"/>
        <f:param name="isFromStudent" value="true"/>
        <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
        <h:outputText value="#{selectIndexMessages.stats} "/>
       </h:commandLink>

       <h:outputText value="#{selectIndexMessages.assessmentRetractedForEdit}" rendered="#{reviewable.isAssessmentRetractForEdit}" styleClass="validate" />

<%-- OK, Marc wants links for all submitted assessments regardless if feedback is available, see SAM-229
     So, command these out for 1.5
      <h:commandLink title="#{selectIndexMessages.t_reviewAssessment}" action="takeAssessment"
        rendered="#{reviewable.feedback eq 'true'}" >
        <f:param name="publishedId" value="#{reviewable.assessmentId}" />
        <f:param name="review" value="true" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
        <h:outputText value="#{reviewable.assessmentTitle} "/>
      </h:commandLink>
        <h:outputText value="#{reviewable.assessmentTitle}"
          rendered="#{reviewable.feedback eq 'false'}" />
--%>
    </h:column>

<%-- FEEDBACK DATE --%>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortFbDate}" id="feedbackDate" rendered="#{select.reviewableSortOrder!='feedbackDate'}" onmouseup="disableLinks(this);">
          <f:param name="reviewableSortType" value="feedbackDate"/>
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.feedback_date} " rendered="#{select.reviewableSortOrder!='feedbackDate'}" />
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.feedback_date} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='feedbackDate'}" />
        <h:commandLink title="#{selectIndexMessages.t_sortFbDate}" rendered="#{select.reviewableSortOrder=='feedbackDate' && select.reviewableAscending}" onmouseup="disableLinks(this);">
           <f:param name="reviewableAscending" value="false" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortFbDateDescending}"
            rendered="#{select.reviewableSortOrder=='feedbackDate' && select.reviewableAscending}"
            url="/images/sortascending.gif"/>
        </h:commandLink>
        <h:commandLink title="#{selectIndexMessages.t_sortFbDate}" rendered="#{select.reviewableSortOrder=='feedbackDate' && !select.reviewableAscending } " onmouseup="disableLinks(this);">
          <f:param name="reviewableAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortFbDateAscending}"
            rendered="#{select.reviewableSortOrder=='feedbackDate' && !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{reviewable.feedbackDateString}" 
        rendered="#{reviewable.feedbackDelivery eq '2' && !reviewable.isAssessmentRetractForEdit}" >
      </h:outputText>
      <h:outputText value="#{selectIndexMessages.immediate}" 
        rendered="#{(reviewable.feedbackDelivery eq '1' || reviewable.feedbackDelivery eq '4') && !reviewable.isAssessmentRetractForEdit}" />

       <h:outputText value="#{selectIndexMessages.not_applicable}" 
        rendered="#{reviewable.feedbackDelivery==null || reviewable.feedbackDelivery eq '3' || reviewable.isAssessmentRetractForEdit}" />
    </h:column>

<%-- STATISTICS --%>
<%-- no statistic for 1.5 release
    <h:column>
      <f:facet name="header">
        <h:commandLink id="reviewstats">
          <f:param name="reviewableSortType" value="stats" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.statistics} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='stats'}"/>
          <h:outputText value="#{selectIndexMessages.statistics} " rendered="#{select.reviewableSortOrder!='stats'}"/>
           <h:commandLink rendered="#{select.reviewableSortOrder=='stats'}">
          <h:graphicImage alt="#{selectIndexMessages.asc}"
            rendered="#{!select.reviewableAscending}"
            url="/images/sortascending.gif"/>
          <h:graphicImage alt="#{selectIndexMessages.desc}"
            rendered="#{select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
        </h:commandLink>
      </f:facet>
      <h:panelGroup>
        <h:commandLink action="personalStats" id="personalStats"
          rendered="#{reviewable.statsAvailable}">
          <h:inputHidden id="statsAssessmentId" value="#{reviewable.assessmentId}"/>
          <h:outputText value="#{selectIndexMessages.view} "/>
        </h:commandLink>
        <h:outputText value="#{selectIndexMessages.not_applicable}"
          rendered="#{!reviewable.statsAvailable}"/>
      </h:panelGroup>
    </h:column>
--%>


<%-- GRADE --%>
<%--
    <h:column>
      <f:facet name="header">
        <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" id="reviewgrade"  rendered="#{select.reviewableSortOrder!='grade'}">
          <f:param name="reviewableSortType" value="grade" />
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.grade} "  rendered="#{select.reviewableSortOrder!='grade'}" />
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.grade} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='grade'}" />
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" rendered="#{select.reviewableSortOrder=='grade' && select.reviewableAscending  }">
           <f:param name="reviewableAscending" value="false" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortScoreDescending}"
            rendered="#{select.reviewableSortOrder=='grade' && select.reviewableAscending}"
            url="/images/sortascending.gif"/>
        </h:commandLink>
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" rendered="#{select.reviewableSortOrder=='grade' && !select.reviewableAscending  }">
           <f:param name="reviewableAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortScoreAscending}"
            rendered="#{select.reviewableSortOrder=='grade' && !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
        </h:commandLink>
        </h:panelGroup>
      </f:facet>
      <h:panelGroup>
        <h:outputText value="#{reviewable.grade} " rendered="#{reviewable.graded}"/>
        <h:outputText value="#{selectIndexMessages.not_applicable}" rendered="#{!reviewable.graded}"/>
      </h:panelGroup>
    </h:column>
--%>

<%-- SCORE --%>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" id="reviewraw" rendered="#{select.reviewableSortOrder!='raw'}" onmouseup="disableLinks(this);">
          <f:param name="reviewableSortType" value="raw"/>
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.score} " rendered="#{select.reviewableSortOrder!='raw'}"/>
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.score} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='raw'}"/>
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" rendered="#{select.reviewableSortOrder=='raw' && select.reviewableAscending}" onmouseup="disableLinks(this);">
           <f:param name="reviewableAscending" value="false"/>
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortScoreDescending}"
            rendered="#{select.reviewableSortOrder=='raw' && select.reviewableAscending}"
            url="/images/sortascending.gif"/>
        </h:commandLink>
        <h:commandLink title="#{selectIndexMessages.t_sortScore}" rendered="#{select.reviewableSortOrder=='raw' && !select.reviewableAscending}" onmouseup="disableLinks(this);">
           <f:param name="reviewableAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortScoreAscending}"
            rendered="#{select.reviewableSortOrder=='raw' && !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
        </h:panelGroup>
      </f:facet>
      <h:outputText value="#{reviewable.roundedRawScore} " rendered="#{reviewable.showScore eq 'true'  && !reviewable.isAssessmentRetractForEdit}" />
      <h:outputText value="#{selectIndexMessages.not_applicable}" rendered="#{reviewable.showScore eq 'false' || reviewable.isAssessmentRetractForEdit}" />
        
<h:outputText value="#{selectIndexMessages.asterisk}" rendered="#{reviewable.multipleSubmissions eq 'true' && reviewable.scoringOption eq '1'}"/>
      
    </h:column>

<%-- TIME --%>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortTime}" id="reviewtime" rendered="#{select.reviewableSortOrder!='time'}" onmouseup="disableLinks(this);">
          <f:param name="reviewableSortType" value="time" />
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.time} "  rendered="#{select.reviewableSortOrder!='time'}"/>
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.time} "  styleClass="currentSort" rendered="#{select.reviewableSortOrder=='time'}"/>
        <h:commandLink title="#{selectIndexMessages.t_sortTime}" rendered="#{select.reviewableSortOrder=='time'&& select.reviewableAscending }" onmouseup="disableLinks(this);">
           <f:param name="reviewableAscending" value="false" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortTimeDescending}" rendered="#{select.reviewableSortOrder=='time' && select.reviewableAscending}"
           url="/images/sortascending.gif"/>
        </h:commandLink>
        <h:commandLink title="#{selectIndexMessages.t_sortTime}" rendered="#{select.reviewableSortOrder=='time'&& !select.reviewableAscending }" onmouseup="disableLinks(this);">
           <f:param name="reviewableAscending" value="true" />
           <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortTimeAscending}"
            rendered="#{select.reviewableSortOrder=='time'&& !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
        </h:panelGroup>
      </f:facet>
      <h:panelGroup>

        <h:outputText id="timeElapse" value="#{reviewable.timeElapse}" />
<%--
        <h:outputText value="#{selectIndexMessages.na}"
          rendered="#{reviewable.submitted && reviewable.submissionHours == 'n/a' && reviewable.submissionMinutes == 'n/a'}" />

 <h:outputText value="#{reviewable.submissionHours} #{selectIndexMessages.hours}, #{reviewable.submissionMinutes} #{selectIndexMessages.minutes}"
          rendered="#{reviewable.submitted && reviewable.submissionHours ne 'n/a' || reviewable.submissionMinutes ne 'n/a'}" />
        <h:outputText value="#{selectIndexMessages.not_applicable}"
          rendered="#{!reviewable.submitted}"/>
--%>
      </h:panelGroup>
    </h:column>

<%-- SUBMITTED --%>
    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{selectIndexMessages.t_sortSubmittedDate}" id="reviewsubmitted" rendered="#{select.reviewableSortOrder!='submitted'}" onmouseup="disableLinks(this);">
          <f:param name="reviewableSortType" value="submitted"/>
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:outputText value="#{selectIndexMessages.submitted} " rendered="#{select.reviewableSortOrder!='submitted'}"/>
        </h:commandLink>
          <h:outputText value="#{selectIndexMessages.submitted} " styleClass="currentSort" rendered="#{select.reviewableSortOrder=='submitted'}"/>
        <h:commandLink title="#{selectIndexMessages.t_sortSubmittedDate}" rendered="#{select.reviewableSortOrder=='submitted' && select.reviewableAscending }" onmouseup="disableLinks(this);">
          <f:param name="reviewableAscending" value="false" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortSubmittedDateDescending}"
            rendered="#{select.reviewableSortOrder=='submitted' && select.reviewableAscending}"
            url="/images/sortascending.gif"/>
        </h:commandLink>


        <h:commandLink title="#{selectIndexMessages.t_sortSubmittedDate}" rendered="#{select.reviewableSortOrder=='submitted' && !select.reviewableAscending }" onmouseup="disableLinks(this);">
          <f:param name="reviewableAscending" value="true" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
          <h:graphicImage alt="#{selectIndexMessages.alt_sortSubmittedDateAscending}"
            rendered="#{select.reviewableSortOrder=='submitted' && !select.reviewableAscending}"
            url="/images/sortdescending.gif"/>
          </h:commandLink>
        </h:panelGroup>
      </f:facet>

      <h:outputText value="#{reviewable.submissionDateString}" >
      </h:outputText>
    </h:column>
  </h:dataTable>

<h:panelGrid columns="1">
  <h:outputText value="#{selectIndexMessages.asterisk} #{selectIndexMessages.highest_note}" rendered="#{select.hasHighestMultipleSubmission}"/>
  <h:outputText value="#{selectIndexMessages.asterisk_2} #{selectIndexMessages.has_been_modified}" rendered="#{select.hasAnyAssessmentBeenModified}" styleClass="validate"/>
</h:panelGrid>

  </div></div>
  </h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
