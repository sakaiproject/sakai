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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <style type="text/css">
        .TableColumn {
          text-align: center
        }
        .TableClass {
          border-style: dotted;
          border-width: 0.5px;
          border-color: light grey;
        }
      </style>
      <title><h:outputText
        value="#{commonMessages.total_scores}" /></title>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />

      </head>
  <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content... -->
<script type="text/javascript">
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

</script>

 <div class="portletBody">
<h:form id="editStudentResults">
  <h:inputHidden id="publishedIdd" value="#{studentScores.publishedId}" />
  <h:inputHidden id="publishedId" value="#{studentScores.publishedId}" />
  <h:inputHidden id="studentid" value="#{studentScores.studentId}" />
  <h:inputHidden id="studentName" value="#{studentScores.studentName}" />
  <h:inputHidden id="gradingData" value="#{studentScores.assessmentGradingId}" />
  <h:inputHidden id="itemId" value="#{studentScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{studentScores.studentName}" rendered="#{totalScores.anonymous eq 'false'}"/>
    <h:outputText value="#{evaluationMessages.submission_id}#{deliveryMessages.column} #{studentScores.assessmentGradingId}" rendered="#{totalScores.anonymous eq 'true'}"/>
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
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne ''}"  />
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.q_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}"  />
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>

  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<f:verbatim><h4></f:verbatim>
<h:outputText value="#{totalScores.assessmentName}" escape="false"/>
<f:verbatim></h4></f:verbatim>
<div class="tier3">
<h:panelGrid columns="2">
   <h:outputText value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
   <h:inputTextarea value="#{studentScores.comments}" rows="3" cols="30"/>
   </h:panelGrid>
</div>
<f:verbatim><h4></f:verbatim>
<h:outputText value="#{deliveryMessages.table_of_contents}" />
<f:verbatim></h4></f:verbatim>

<div class="tier2">
  <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
  <h:column>
    <h:panelGroup>
      <samigo:hideDivision id="part" title = " #{deliveryMessages.p} #{part.number} #{evaluationMessages.dash} #{part.text} #{evaluationMessages.dash}
       #{part.questions-part.unansweredQuestions}#{evaluationMessages.splash}#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString} #{part.maxPoints} #{deliveryMessages.pt}" > 
        <h:dataTable value="#{part.itemContents}" var="question">
          <h:column>
            <f:verbatim><h4 class="tier3"></f:verbatim>
              <h:panelGroup>
                <h:outputLink value="##{part.number}#{deliveryMessages.underscore}#{question.number}"> 
                  <h:outputText escape="false" value="#{question.number}#{deliveryMessages.dot} #{question.strippedText} #{question.maxPoints} #{deliveryMessages.pt} ">
				  </h:outputText>
                </h:outputLink>
              </h:panelGroup>
            <f:verbatim></h4></f:verbatim> 
          </h:column>
        </h:dataTable>
      </samigo:hideDivision>
     </h:panelGroup>
   </h:column>
  </h:dataTable>
</div>


<div class="tier2">
  <h:dataTable value="#{delivery.pageContents.partsContents}" var="part" width="100%" border="0">
    <h:column>
      <f:verbatim><h4 class="tier1"></f:verbatim>
      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{deliveryMessages.ans_q}, " / -->
      <!-- h:outputText value="#{part.points}/#{part.maxPoints} #{deliveryMessages.pt}" / -->
      <f:verbatim></h4><div class="tier1"></f:verbatim>
      <h:outputText value="#{part.text}" escape="false" rendered="#{part.numParts ne '1'}" />
      <f:verbatim></div></f:verbatim>
      <f:verbatim></h4><div class="tier2"></f:verbatim>
     <h:outputText value="#{evaluationMessages.no_questions}" escape="false" rendered="#{part.noQuestions}"/>
      <f:verbatim></div></f:verbatim>

      <h:dataTable value="#{part.itemContents}" columnClasses="tier2"
          var="question" border="0" width="100%">
        <h:column>
          <h:outputText value="<a name=\"" escape="false" />
          <h:outputText value="#{part.number}_#{question.number}\"></a>"
            escape="false" />
          <f:verbatim><h4 class="tier2"></f:verbatim>
            <h:outputText value="#{deliveryMessages.q} #{question.number} #{deliveryMessages.of} " />
            <h:outputText value="#{part.questions}#{deliveryMessages.column}  " />
            <h:inputText id="adjustedScore" value="#{question.pointsForEdit}" onchange="toPoint(this.id);" >
<f:validateDoubleRange/>
<%--SAK-3776    <f:convertNumber maxFractionDigits="2"/> --%>
            </h:inputText>
            <h:outputText value=" #{deliveryMessages.splash} #{question.maxPoints} " />
            <h:outputText value="#{deliveryMessages.pt}"/>
          <f:verbatim><br/></f:verbatim>
<h:message for="adjustedScore" style="color:red"/>
          <f:verbatim></h4></f:verbatim>

          <f:verbatim><div class="tier3"></f:verbatim>
            
            <h:panelGroup rendered="#{question.itemData.typeId == 7}">
              <f:subview id="deliverAudioRecording">
               <%@ include file="/jsf/evaluation/item/displayAudioRecording.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 6}">
              <f:subview id="deliverFileUpload">
                <%@ include file="/jsf/evaluation/item/displayFileUpload.jsp" %>
              </f:subview>
            </h:panelGroup>

   			<h:panelGroup rendered="#{question.itemData.typeId == 11}">
	      	<f:subview id="deliverFillInNumeric">
	        <%@ include file="/jsf/delivery/item/deliverFillInNumeric.jsp" %>
	      </f:subview>
            </h:panelGroup>


            <h:panelGroup rendered="#{question.itemData.typeId == 8}">
              <f:subview id="deliverFillInTheBlank">
                <%@ include file="/jsf/delivery/item/deliverFillInTheBlank.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 9}">
              <f:subview id="deliverMatching">
                <%@ include file="/jsf/delivery/item/deliverMatching.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 15}"><!-- // CALCULATED_QUESTION -->
              <f:subview id="deliverCalculatedQuestion">
                <%@ include file="/jsf/delivery/item/deliverCalculatedQuestion.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup
              rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 12 || question.itemData.typeId == 3}">
              <f:subview id="deliverMultipleChoiceSingleCorrect">
                <%@ include file="/jsf/delivery/item/deliverMultipleChoiceSingleCorrect.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 2}">
              <f:subview id="deliverMultipleChoiceMultipleCorrect">
                <%@ include file="/jsf/delivery/item/deliverMultipleChoiceMultipleCorrect.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 5}">
              <f:subview id="deliverShortAnswer">
                <%@ include file="/jsf/delivery/item/deliverShortAnswerLink.jsp" %>
              </f:subview>
            </h:panelGroup>

            <h:panelGroup rendered="#{question.itemData.typeId == 4}">
              <f:subview id="deliverTrueFalse">
                <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
              </f:subview>
            </h:panelGroup>
            <h:panelGroup rendered="#{question.itemData.typeId == 13}">
              <f:subview id="deliverMatrixChoicesSurvey">
                <%@ include file="/jsf/delivery/item/deliverMatrixChoicesSurvey.jsp" %>
              </f:subview>
            </h:panelGroup>
          <f:verbatim></div></f:verbatim>

          <f:verbatim><div class="tier2"></f:verbatim>
          <h:panelGrid columns="2" border="0" >
            <h:outputText value="#{evaluationMessages.comment_for_student}#{deliveryMessages.column}"/>
            <h:inputTextarea value="#{question.gradingComment}" rows="3" cols="30"/>
            <h:outputText value=" "/>
    	    <%@ include file="/jsf/evaluation/gradeStudentResultAttachment.jsp" %>
          </h:panelGrid>
          <f:verbatim></div></f:verbatim>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>
</div>

<h:outputText value="#{author.updateFormTime}" />
<h:inputHidden id="currentFormTime" value="#{author.currentFormTime}" />
<%
  org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean author = (org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean) session.getAttribute("author");
  out.println("<script>document.getElementById('editStudentResults:currentFormTime').value = " + author.getCurrentFormTime() + ";</script>");
%>

<h:panelGroup rendered="#{totalScores.anonymous eq 'false' && studentScores.email != null && studentScores.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}">
  <h:outputText value="<a href=\"mailto:" escape="false" />
  <h:outputText value="#{studentScores.email}" escape="false" />
  <h:outputText value="?subject=" escape="false" />
  <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
  <h:outputText value="  #{evaluationMessages.email} #{studentScores.firstName}" escape="false"/>
  <h:outputText value="</a>" escape="false" />
</h:panelGroup>

<p class="act">
   <h:commandButton styleClass="active" value="#{evaluationMessages.save_cont}" action="totalScores" type="submit">
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreUpdateListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
   <h:commandButton value="#{commonMessages.cancel_action}" action="totalScores" immediate="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
   </h:commandButton>
</p>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
