<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!--
$Id$
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
        value="#{evaluationMessages.title_question}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

 <!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script>
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

function clickEmailLink(field, fromName, fromEmailAddress, toName, toEmailAddress, assessmentName){
var emaillinkid= field.id.replace("createEmail", "hiddenlink");
//fromName = escapeApostrophe(fromName);
//to = escapeApostrophe(toName);
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == emaillinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
window.open("../evaluation/createNewEmail.faces?fromEmailLinkClick=true&fromName=" + fromName + "&fromEmailAddress=" + fromEmailAddress + "&toName=" + toName + "&toEmailAddress=" + toEmailAddress +  "&assessmentName=" + assessmentName,'createEmail','width=600,height=600,scrollbars=yes, resizable=yes');

document.location='../evaluation/questionScore';
}
</script>

<!-- content... -->
 <div class="portletBody">
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{questionScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{questionScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
   
  <h:outputText value="#{evaluationMessages.part} #{questionScores.partName}#{evaluationMessages.comma} #{evaluationMessages.question} #{questionScores.itemName} (#{totalScores.assessmentName}) " escape="false"/>
  </h3>
  <p class="navViewAction">
    <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true">
      <h:outputText value="#{evaluationMessages.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " />
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{evaluationMessages.title_total}" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " />
    <h:outputText value="#{evaluationMessages.q_view}" />

	<h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>


	<h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{evaluationMessages.item_analysis}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>


    <h:outputText value=" #{evaluationMessages.separator} " />
    <h:commandLink title="#{evaluationMessages.t_export}" action="exportResponses" immediate="true">
      <h:outputText value="#{evaluationMessages.export}" />
  	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
    </h:commandLink>


  </p>
<div class="tier1">
  <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

  <h:dataTable value="#{questionScores.sections}" var="partinit">
    <h:column>
      <h:outputText value="#{evaluationMessages.part} #{partinit.partNumber}#{evaluationMessages.column}" />
    </h:column>
    <h:column>
      <samigo:dataLine value="#{partinit.questionNumberList}" var="iteminit" separator=" | " first="0" rows="#{partinit.numberQuestionsTotal}" rendered="#{!partinit.isRandomDrawPart}" >
        <h:column>
          <h:commandLink title="#{evaluationMessages.t_questionScores}"action="questionScores" immediate="true" >
            <h:outputText value="#{evaluationMessages.q}&nbsp;#{iteminit.partNumber} " escape="false"/>
			<f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
            <f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
            <f:param name="newItemId" value="#{iteminit.id}" />
          </h:commandLink>
	    </h:column>
      </samigo:dataLine>

	  <h:outputText value="#{evaluationMessages.random_draw_info_1} #{partinit.numberQuestionsDraw} #{evaluationMessages.random_draw_info_2} #{partinit.numberQuestionsTotal} #{evaluationMessages.random_draw_info_3} " rendered="#{partinit.isRandomDrawPart}" />
	  <samigo:dataLine value="#{partinit.questionNumberList}" var="iteminit" separator=" | " first="0" rows="#{partinit.numberQuestionsTotal}" rendered="#{partinit.isRandomDrawPart}" >
        <h:column>
          <h:commandLink title="#{evaluationMessages.t_questionScores}"action="questionScores" immediate="true" >
            <h:outputText value="#{evaluationMessages.q} #{iteminit.partNumber} "/>
			<f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
            <f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
            <f:param name="newItemId" value="#{iteminit.id}" />
          </h:commandLink>
	    </h:column>
      </samigo:dataLine>

	  <h:outputText value="#{evaluationMessages.no_questions} " rendered="#{partinit.noQuestions}"/>
    </h:column>
  </h:dataTable>

<!--h:panelGrid styleClass="navModeQuestion" columns="2" columnClasses="alignLeft,alignCenter" width="100%" -->
<h4>
  <h:dataTable value="#{questionScores.deliveryItem}" var="question" border="0" >
    <h:column>
     <h:panelGroup rendered="#{questionScores.typeId == '7'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_aud}"/>
     </h:panelGroup>
      <h:panelGroup rendered="#{questionScores.typeId == '6'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_fu}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '8'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_fib}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '11'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_fin}"/>
     </h:panelGroup>
     
      <h:panelGroup rendered="#{questionScores.typeId == '9'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_match}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '2'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_mult_mult_ms}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '4'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_tf}"/>
     </h:panelGroup>

     <h:panelGroup rendered="#{questionScores.typeId == '5'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_short_ess}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '3'}">
         <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_mult_surv}"/>
     </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '1'}">
    <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_mult_sing}"/>
      </h:panelGroup>
     <h:panelGroup rendered="#{questionScores.typeId == '12'}">
    <h:outputText value="#{evaluationMessages.question}#{question.sequence} - #{evaluationMessages.q_mult_mult_ss}"/>
      </h:panelGroup>
 </h:column>

  <!-- following columns are for formatting -->
  <h:column></h:column>
  <h:column></h:column>
  <h:column></h:column>
  <h:column></h:column>

  <h:column>
     <h:outputText value="#{questionScores.maxPoint}" style="instruction"/>
  </h:column>
  </h:dataTable>
</h4>

  <h:dataTable value="#{questionScores.deliveryItem}" var="question">
  <h:column>
  <h:panelGroup rendered="#{questionScores.typeId == '7'}">
    <f:subview id="displayAudioRecording">
      <%@ include file="/jsf/evaluation/item/displayAudioRecordingQuestion.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '6'}">
    <f:subview id="displayFileUpload">
    <%@ include file="/jsf/evaluation/item/displayFileUploadQuestion.jsp" %>
    </f:subview>
  </h:panelGroup>
    <h:panelGroup rendered="#{questionScores.typeId == '11'}">
      <f:subview id="displayFillInNumeric">
      <%@ include file="/jsf/evaluation/item/displayFillInNumeric.jsp" %>
      </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '8'}">
    <f:subview id="displayFillInTheBlank">
    <%@ include file="/jsf/evaluation/item/displayFillInTheBlank.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '9'}">
    <f:subview id="displayMatching">
    <%@ include file="/jsf/evaluation/item/displayMatching.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '2'}">
    <f:subview id="displayMultipleChoiceMultipleCorrect">
  <%@ include file="/jsf/evaluation/item/displayMultipleChoiceMultipleCorrect.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup
    rendered="#{questionScores.typeId == '1' || questionScores.typeId == '12' ||
                questionScores.typeId == '3'}">
    <f:subview id="displayMultipleChoiceSingleCorrect">
    <%@ include file="/jsf/evaluation/item/displayMultipleChoiceSingleCorrect.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '5'}">
    <f:subview id="displayShortAnswer">
   <%@ include file="/jsf/evaluation/item/displayShortAnswer.jsp" %>
    </f:subview>
  </h:panelGroup>
  <h:panelGroup rendered="#{questionScores.typeId == '4'}">
    <f:subview id="displayTrueFalse">
    <%@ include file="/jsf/evaluation/item/displayTrueFalse.jsp" %>
    </f:subview>
  </h:panelGroup>
  </h:column>
  </h:dataTable>

<h4>
<h:panelGrid columns="2" columnClasses="navView,navList" width="100%">	
	<h:panelGroup>
	 <p class=" navView navModeAction">
	<h:outputText value="#{evaluationMessages.responses}"/>
	</p>
	</h:panelGroup>
	<h:panelGroup rendered="#{questionScores.typeId == '6'}">
		<h:outputLink title="#{evaluationMessages.t_fileUpload}" value="/samigo/servlet/DownloadAllMedia?publishedId=#{questionScores.publishedId}&publishedItemId=#{questionScores.itemId}&createdBy=#{question.itemData.createdBy}&partNumber=#{part.partNumber}&anonymous=#{totalScores.anonymous}&scoringType=#{questionScores.allSubmissions}">
		<h:outputText escape="false" value="#{evaluationMessages.download_all}" />
		</h:outputLink>
	 </h:panelGroup>
</h:panelGrid>
</h4>

<sakai:flowState bean="#{questionScores}" />
<h:panelGrid columns="2" columnClasses="samLeftNav,samRightNav" width="100%" rendered="#{totalScores.anonymous eq 'false'}">
  <h:panelGroup>
    <h:panelGrid columns="1" columnClasses="samLeftNav" width="100%">
	  <h:panelGroup>
        <!-- SECTION AWARE -->
        <h:outputText value="#{evaluationMessages.view}"/>
        <h:outputText value="#{evaluationMessages.column}"/>
        <h:selectOneMenu value="#{questionScores.selectedSectionFilterValue}" id="sectionpicker" required="true" onchange="document.forms[0].submit();">
          <f:selectItems value="#{totalScores.sectionFilterSelectItems}"/>
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener"/>
        </h:selectOneMenu>

	
        <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsL1"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'  && totalScores.multipleSubmissionsAllowed eq 'true'  }">
          <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
          <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu>

        <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsH1"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'  && totalScores.multipleSubmissionsAllowed eq 'true' }">
          <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
          <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu>

        <h:selectOneMenu value="#{questionScores.selectedSARationaleView}" id="inlinepopup1" 
         rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4'  || questionScores.typeId == '5')}" 
       	 required="true" onchange="document.forms[0].submit();" >
           <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.responses_popup}" />
           <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.responses_inline}" />
           <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu> 
      </h:panelGroup>

	  <h:panelGroup>
 	        <h:outputText value="#{evaluationMessages.search}"/>
            <h:outputText value="#{evaluationMessages.column}"/>
			<h:inputText
				id="searchString"
				value="#{questionScores.searchString}"
				onfocus="clearIfDefaultString(this, '#{evaluationMessages.search_default_student_search_string}')"
				onkeypress="return submitOnEnter(event, 'editTotalResults:searchSubmitButton');"/>
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{questionScores.search}" value="#{evaluationMessages.search_find}" id="searchSubmitButton" />
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{questionScores.clear}" value="#{evaluationMessages.search_clear}"/>
	  </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
   
   <h:panelGroup>
	<sakai:pager id="pager1" totalItems="#{questionScores.dataRows}" firstItem="#{questionScores.firstRow}" pageSize="#{questionScores.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" >
		  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
	</sakai:pager>
  </h:panelGroup>
</h:panelGrid>

<h:panelGrid columns="2" columnClasses="samLeftNav,samRightNav" width="100%" rendered="#{totalScores.anonymous eq 'true'}">
  <h:panelGroup>
    <h:panelGrid columns="1" columnClasses="samLeftNav" width="100%">
      <h:panelGroup>
        <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsL2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'  && totalScores.multipleSubmissionsAllowed eq 'true' }">
        <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
        <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
        <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu>

        <h:selectOneMenu value="#{questionScores.allSubmissions}" id="allSubmissionsH2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'  && totalScores.multipleSubmissionsAllowed eq 'true' }">
          <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
          <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu>

		<h:selectOneMenu value="#{questionScores.selectedSARationaleView}" id="inlinepopup2" 
         rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4'  || questionScores.typeId == '5')}" 
       	 required="true" onchange="document.forms[0].submit();" >
           <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.responses_popup}" />
           <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.responses_inline}" />
           <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:selectOneMenu> 

      </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
  
  <h:panelGroup>
	<sakai:pager id="pager2" totalItems="#{questionScores.dataRows}" firstItem="#{questionScores.firstRow}" pageSize="#{questionScores.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" >
	  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScorePagerListener" />
	</sakai:pager>
  </h:panelGroup>
</h:panelGrid>


</div>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" id="questionScoreTable" value="#{questionScores.agents}"
    var="description" styleClass="listHier" columnClasses="textTable">

    <!-- NAME/SUBMISSION ID -->
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" id="lastName" action="questionScores">
          <h:outputText value="#{evaluationMessages.name}" />
        <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="lastName" />
        <f:param name="sortAscending" value="true" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
 		 <f:param name="email" value="#{description.email}" />
       </h:commandLink>
	   <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail1" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\", '#{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	   </span>
     </h:panelGroup>
	 <h:commandLink id="hiddenlink1" value="" action="questionScores">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	</h:commandLink>
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'lastName' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="questionScores">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
		 <f:param name="email" value="#{description.email}" />
       </h:commandLink>
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail2" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\",' #{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	   </span>
     </h:panelGroup>
	<h:commandLink id="hiddenlink2" value="" action="questionScores">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	</h:commandLink>
    </h:column>    
    
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'lastName' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="questionScores">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
		 <f:param name="email" value="#{description.email}" />
       </h:commandLink>
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail3" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\", '#{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	   </span>
     </h:panelGroup>
	<h:commandLink id="hiddenlink3" value="" action="questionScores">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	</h:commandLink>
    </h:column>  


	<!-- SUBMISSION ID -->
    <h:column rendered="#{questionScores.anonymous eq 'true' && questionScores.sortType ne 'assessmentGradingId'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="questionScores">
          <h:outputText value="#{evaluationMessages.sub_id}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="assessmentGradingId" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'true' && questionScores.sortType eq 'assessmentGradingId' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="questionScores">
          <h:outputText value="#{evaluationMessages.sub_id}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionIdDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>    
    
    <h:column rendered="#{questionScores.anonymous eq 'true' && questionScores.sortType eq 'assessmentGradingId' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="questionScores">
          <h:outputText value="#{evaluationMessages.sub_id}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionIdAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:panelGroup>
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{questionScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
    </h:column>  


   <!-- STUDENT ID -->
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType!='agentEid'}" >
     <f:facet name="header">
       <h:commandLink title="#{evaluationMessages.t_sortUserId}" id="agentEid" action="questionScores" >
          <h:outputText value="#{evaluationMessages.uid}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="agentEid" />
        <f:param name="sortAscending" value="true" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>

    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'agentEid' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="questionScores">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>    
    
    <h:column rendered="#{questionScores.anonymous eq 'false' && questionScores.sortType eq 'agentEid' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="questionScores">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>      


    <!-- ROLE -->
    <h:column rendered="#{questionScores.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{evaluationMessages.t_sortRole}" id="role" action="questionScores">
          <h:outputText value="#{evaluationMessages.role}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="role" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <h:column rendered="#{questionScores.sortType eq 'role' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="questionScores">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>    
    
    <h:column rendered="#{questionScores.sortType eq 'role' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="questionScores">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>      


    <!-- DATE -->
    <h:column rendered="#{questionScores.sortType!='submittedDate'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" id="submittedDate" action="questionScores">
          <h:outputText value="#{evaluationMessages.date}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>

    <h:column rendered="#{questionScores.sortType eq 'submittedDate' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="questionScores">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.submittedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>    
    
    <h:column rendered="#{questionScores.sortType eq 'submittedDate' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="questionScores">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.submittedDate}">
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
    </h:column>    


    <!-- SCORE -->
    <h:column rendered="#{questionScores.sortType!='totalAutoScore'}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortScore}" id="score" action="questionScores">
          <h:outputText value="#{evaluationMessages.score}" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <f:param name="sortBy" value="totalAutoScore" />
        <f:param name="sortAscending" value="true" />
        </h:commandLink>
      </f:facet>
      <h:inputText value="#{description.totalAutoScore}" size="5" id="qscore" required="false" onchange="toPoint(this.id);">
<f:validateDoubleRange/>
</h:inputText>
<br />
 <h:message for="qscore" style="color:red"/>
 </h:column>
 
 
    <h:column rendered="#{questionScores.sortType eq 'totalAutoScore' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortScore}" action="questionScores">
          <h:outputText value="#{evaluationMessages.score}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortAdjustScoreDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
	  <h:inputText value="#{description.totalAutoScore}" size="5"  id="qscore2" required="false" onchange="toPoint(this.id);">
	  	<f:validateDoubleRange/>
	  </h:inputText>
	  <h:message for="qscore2" style="color:red"/>
    </h:column>    
    
    <h:column rendered="#{questionScores.sortType eq 'totalAutoScore' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortScore}" action="questionScores">
          <h:outputText value="#{evaluationMessages.score}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortAdjustScoreAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
      </f:facet>
	  <h:inputText value="#{description.totalAutoScore}" size="5"  id="qscore3" required="false" onchange="toPoint(this.id);">
	  	<f:validateDoubleRange/>
	  </h:inputText>
	  <h:message for="qscore2" style="color:red"/>
    </h:column>    
 

    <!-- ANSWER -->
    <h:column rendered="#{questionScores.sortType!='answer'}">
      <f:facet name="header">
        <h:panelGroup>
		  <h:outputText value="#{evaluationMessages.stud_resp}" 
             rendered="#{questionScores.typeId == '6' || questionScores.typeId == '7' }"/>
          <h:commandLink title="#{evaluationMessages.t_sortResponse}" id="answer" action="questionScores" >
            <h:outputText value="#{evaluationMessages.stud_resp}" 
               rendered="#{questionScores.typeId != '6' && questionScores.typeId != '7' }"/>
            <f:actionListener
               type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
            <f:actionListener
               type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
            <f:param name="sortBy" value="answer" />
            <f:param name="sortAscending" value="true" />
          </h:commandLink>
        </h:panelGroup>
      </f:facet>
      <!-- display of answer to file upload question is diffenent from other types - daisyf -->
      <h:outputText value="#{description.answer}" escape="false" rendered="#{questionScores.typeId != '6' && questionScores.typeId != '7' && questionScores.typeId != '5'}" />
     <f:verbatim><br/></f:verbatim>
   <!--h:outputLink rendered="#{questionScores.typeId == '5'}" value="#" onclick="javascript:window.alert('#{description.fullAnswer}');"-->

    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '1' && questionScores.typeId == '5'}">
    <h:outputText value="#{description.answer}" escape="false"/>
     <f:verbatim><br/></f:verbatim>
		<h:outputLink title="#{evaluationMessages.t_fullShortAnswer}"   value="#" onclick="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_shortAnswer})" rendered="#{description.answer != 'No Answer'}"/>
    	</h:outputLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '2' && questionScores.typeId == '5'}">
		<h:outputText  escape="false" value="#{description.fullAnswer}"/>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '1'}">
		<h:outputLink title="#{evaluationMessages.t_rationale}"  value="#" onclick="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_rationale})" />
    	</h:outputLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '2'}">
		<h:outputText escape="false" value="#{description.rationale}"/>
    </h:panelGroup>
    

    
<%--
    <h:outputLink title="#{evaluationMessages.t_rationale}"
      rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12 || questionScores.typeId == '4') && description.rationale ne ''}" 
      value="#" onclick="javascript:window.alert('#{description.rationale}');" onkeypress="javascript:window.alert('#{description.rationale}');" >
    <h:outputText  value="(#{evaluationMessages.click_rationale})"/>
    </h:outputLink>
--%>
      <h:panelGroup rendered="#{questionScores.typeId == '6'}">
        <f:subview id="displayFileUpload2">
          <%@ include file="/jsf/evaluation/item/displayFileUploadAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>

      <h:panelGroup rendered="#{questionScores.typeId == '7'}">
        <f:subview id="displayAudioRecording2">
          <%@ include file="/jsf/evaluation/item/displayAudioRecordingAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>
    </h:column>

    <h:column rendered="#{questionScores.sortType eq 'answer' && questionScores.sortAscending}">
      <f:facet name="header">
      <h:panelGroup>
		  <h:outputText value="#{evaluationMessages.stud_resp}" 
             rendered="#{questionScores.typeId == '6' || questionScores.typeId == '7' }"/>
          <h:commandLink title="#{evaluationMessages.t_sortResponse}" action="questionScores" >
            <h:outputText value="#{evaluationMessages.stud_resp}" 
               rendered="#{questionScores.typeId != '6' && questionScores.typeId != '7' }"/>
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortResponseDescending}" rendered="#{questionScores.sortAscending && questionScores.typeId != '6' && questionScores.typeId != '7'}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>  
          </h:panelGroup>  
      </f:facet>
      <h:outputText value="#{description.answer}" escape="false" rendered="#{questionScores.typeId != '6' and questionScores.typeId != '7' && questionScores.typeId != '5'}" />
     <f:verbatim><br/></f:verbatim>
   	<!--h:outputLink rendered="#{questionScores.typeId == '5'}" value="#" onclick="javascript:window.alert('#{description.fullAnswer}');"-->


    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '1' && questionScores.typeId == '5'}">
    <h:outputText value="#{description.answer}" escape="false"/>
	<f:verbatim><br/></f:verbatim>
		<h:outputLink title="#{evaluationMessages.t_fullShortAnswer}"   value="#" onclick="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_shortAnswer})" rendered="#{description.answer != 'No Answer'}"/>
    	</h:outputLink>
    </h:panelGroup>

    
    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '2' && questionScores.typeId == '5'}">
		<h:outputText  escape="false" value="#{description.fullAnswer}"/>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '1'}">
		<h:outputLink title="#{evaluationMessages.t_rationale}"  
	value="#" onclick="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_rationale})" />
    	</h:outputLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '2'}">
		<h:outputText escape="false" value="#{description.rationale}"/>
    </h:panelGroup>

    
    <h:panelGroup rendered="#{questionScores.typeId == '6'}">
        <f:subview id="displayFileUpload3">
          <%@ include file="/jsf/evaluation/item/displayFileUploadAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>

      <h:panelGroup rendered="#{questionScores.typeId == '7'}">
        <f:subview id="displayAudioRecording3">
          <%@ include file="/jsf/evaluation/item/displayAudioRecordingAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>
    </h:column>    
    
    <h:column rendered="#{questionScores.sortType eq 'answer' && !questionScores.sortAscending}">
      <f:facet name="header">
		  <h:panelGroup>
		  <h:outputText value="#{evaluationMessages.stud_resp}" 
             rendered="#{questionScores.typeId == '6' || questionScores.typeId == '7' }"/>
          <h:commandLink title="#{evaluationMessages.t_sortResponse}" action="questionScores" >
            <h:outputText value="#{evaluationMessages.stud_resp}" 
               rendered="#{questionScores.typeId != '6' && questionScores.typeId != '7' }"/>
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortResponseAscending}" rendered="#{!questionScores.sortAscending && questionScores.typeId != '6' && questionScores.typeId != '7'}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
          </h:commandLink>    
          </h:panelGroup>
      </f:facet>
	<h:outputText value="#{description.answer}" escape="false" rendered="#{questionScores.typeId != '6' and questionScores.typeId != '7' && questionScores.typeId != '5'}" />
     <f:verbatim><br/></f:verbatim>
   	<!--h:outputLink rendered="#{questionScores.typeId == '5'}" value="#" onclick="javascript:window.alert('#{description.fullAnswer}');"-->


    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '1' && questionScores.typeId == '5'}">
    <h:outputText value="#{description.answer}" escape="false"/>
	<f:verbatim><br/></f:verbatim>
		<h:outputLink title="#{evaluationMessages.t_fullShortAnswer}"   value="#" onclick="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('fullShortAnswer.faces?idString=#{description.assessmentGradingId}','fullShortAnswer','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_shortAnswer})" rendered="#{description.answer != 'No Answer'}" />
    	</h:outputLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{questionScores.selectedSARationaleView == '2' && questionScores.typeId == '5'}">
		<h:outputText  escape="false" value="#{description.fullAnswer}"/>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '1'}">
		<h:outputLink title="#{evaluationMessages.t_rationale}"  
	value="#" onclick="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('rationale.faces?idString=#{description.assessmentGradingId}','rationale','width=600,height=600,scrollbars=yes, resizable=yes');">
    		<h:outputText  value="(#{evaluationMessages.click_rationale})" />
    	</h:outputLink>
    </h:panelGroup>
    
    <h:panelGroup rendered="#{(questionScores.typeId == '1' || questionScores.typeId == '2' || questionScores.typeId == '12' || questionScores.typeId == '4') && description.rationale ne '' && questionScores.selectedSARationaleView == '2'}">
		<h:outputText escape="false" value="#{description.rationale}"/>
    </h:panelGroup>
    
          <h:panelGroup rendered="#{questionScores.typeId == '6'}">
        <f:subview id="displayFileUpload4">
          <%@ include file="/jsf/evaluation/item/displayFileUploadAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>

      <h:panelGroup rendered="#{questionScores.typeId == '7'}">
        <f:subview id="displayAudioRecording4">
          <%@ include file="/jsf/evaluation/item/displayAudioRecordingAnswer.jsp" %>
        </f:subview>
      </h:panelGroup>
    </h:column> 


    <!-- COMMENT -->
    <h:column rendered="#{questionScores.sortType!='comments'}">
     <f:facet name="header">
      <h:panelGroup>
      <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" id="comments" action="questionScores">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        <h:outputText value="#{evaluationMessages.comment_for_student}"/>
        <f:param name="sortBy" value="comments" />
      </h:commandLink>
	  <h:outputText value="&nbsp;&nbsp;" escape="false"/>
	  
      <h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
        <h:outputText  value="#{evaluationMessages.whats_this_link}"/>
      </h:outputLink>
	  </h:panelGroup>
     </f:facet>
     <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>
     <%@ include file="/jsf/evaluation/questionScoreAttachment.jsp" %>
    </h:column>

    <h:column rendered="#{questionScores.sortType eq 'comments' && questionScores.sortAscending}">
      <f:facet name="header">
        <h:panelGroup>
        <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" action="questionScores">
          <h:outputText value="#{evaluationMessages.comment_for_student}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortCommentDescending}" rendered="#{questionScores.sortAscending}" url="/images/sortascending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:commandLink>
		<h:outputText value="&nbsp;&nbsp;" escape="false"/>

		<h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
			<h:outputText  value="#{evaluationMessages.whats_this_link}"/>
		</h:outputLink>
	  </h:panelGroup>
      </f:facet>

      <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>
      <%@ include file="/jsf/evaluation/questionScoreAttachment.jsp" %>
    </h:column>    
    
    <h:column rendered="#{questionScores.sortType eq 'comments' && !questionScores.sortAscending}">
      <f:facet name="header">
        <h:panelGroup>
        <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" action="questionScores">
          <h:outputText value="#{evaluationMessages.comment_for_student}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortCommentAscending}" rendered="#{!questionScores.sortAscending}" url="/images/sortdescending.gif"/>
      	  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
          <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
        </h:commandLink>
		<h:outputText value="&nbsp;&nbsp;" escape="false"/>

		<h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
			<h:outputText  value="#{evaluationMessages.whats_this_link}"/>
		</h:outputLink>
	  </h:panelGroup>
      </f:facet>
     <h:inputTextarea value="#{description.comments}" rows="3" cols="30"/>
     <%@ include file="/jsf/evaluation/questionScoreAttachment.jsp" %>
    </h:column> 

  </h:dataTable>
</div>

<h:outputText value="#{author.updateFormTime}" />
<h:inputHidden value="#{author.currentFormTime}" />

<p class="act">
   <%-- <h:commandButton value="#{evaluationMessages.save_exit}" action="author"/> --%>
   <h:commandButton accesskey="#{evaluationMessages.a_update}" styleClass="active" value="#{evaluationMessages.save_cont}" action="questionScores" type="submit" >
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
   </h:commandButton>
   <h:commandButton accesskey="#{evaluationMessages.a_cancel}" value="#{evaluationMessages.cancel}" action="totalScores" immediate="true"/>
</div>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
