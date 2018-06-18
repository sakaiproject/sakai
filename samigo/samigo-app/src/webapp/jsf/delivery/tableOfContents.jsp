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
<f:view>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head><%= request.getAttribute("html.head") %>
    <title><h:outputText value="#{deliveryMessages.table_of_contents}" /></title>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />
    <%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>
    <h:outputText value="#{delivery.mathJaxHeader}" escape="false" rendered="#{delivery.actionString=='takeAssessmentViaUrl' and delivery.isMathJaxEnabled}"/>
    </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">
<!--div class="portletBody"-->

 <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE -->
 <h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false" />

<div class="portletBody">
 <h:outputText value="<div style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>


<!-- content... -->
<script type="text/javascript">

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

function showElements(theForm) {
  str = "Form Elements of form " + theForm.name + ": \n "
  for (i = 0; i < theForm.length; i++)
    str += theForm.elements[i].name + "\n"
  alert(str)
}

function saveTime()
{
  //showElements(document.forms[0]);
  if((typeof (document.forms[0].elements['tableOfContentsForm:elapsed'])!=undefined) && ((document.forms[0].elements['tableOfContentsForm:elapsed'])!=null) ){
  pauseTiming = 'true';
  // loaded is in 1/10th sec and elapsed is in sec, so need to divide by 10
  if (self.loaded) {
	document.forms[0].elements['tableOfContentsForm:elapsed'].value=loaded/10;
  }
 }
}

function clickSubmitForGrade(){
  var newindex = 0;
  for (i=0; i<document.links.length; i++) {
    if(document.links[i].id == "tableOfContentsForm:submitforgrade")
    {
      newindex = i;
      break;
    }
  }
  document.links[newindex].onclick();
}

</script>


<!-- DONE BUTTON FOR PREVIEW ASSESSMENT -->
<h:form id="tableOfContentsForm">

<h:inputHidden id="hasTimeLimit" value="#{delivery.hasTimeLimit}"/>   
<h:inputHidden id="showTimeWarning" value="#{delivery.showTimeWarning}"/>

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h3><h:outputText value="#{delivery.assessmentTitle} " escape="false"/></h3>

<h:panelGroup rendered="#{(delivery.actionString=='takeAssessment'
                           || delivery.actionString=='takeAssessmentViaUrl') 
                        && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="#{deliveryMessages.time_remaining} "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
  <samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    timeUpMessage="#{deliveryMessages.time_up}"
    expireScript="document.forms[0].elements['tableOfContentsForm:elapsed'].value=loaded; document.forms[0].elements['tableOfContentsForm:outoftime'].value='true';" />
<f:verbatim>  </span></f:verbatim>

<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="#{deliveryMessages.hide_show}" />
</h:panelGroup>

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'&& delivery.hasTimeLimit}" >
  <f:verbatim><div style="margin:10px 0px 0px 0px;"><span style="background-color:#bab5b5; padding:5px"></f:verbatim>
  <h:outputText value="#{deliveryMessages.timer_preview_not_available}"/>
  <f:verbatim></div></span></f:verbatim>
</h:panelGroup>

<f:verbatim><br/></span></f:verbatim>

<f:verbatim><div class="tier1"></f:verbatim>
  <f:verbatim><b></f:verbatim><h:outputText value="#{deliveryMessages.warning}#{deliveryMessages.column} "/><f:verbatim></b></f:verbatim>
  <h:outputText value="#{deliveryMessages.instruction_submitGrading}" />
<f:verbatim></div></f:verbatim>

<div class="tier1">
  <h4>
    <h:outputText value="#{deliveryMessages.table_of_contents} " />
    <h:outputText styleClass="tier10" value="#{deliveryMessages.tot_score} " />
    <h:outputText value="#{delivery.tableOfContents.maxScore}">
      <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText value="#{deliveryMessages.pt}" />
  </h4>
 
</div>

<div class="tier2">
  <h5>
    <h:outputLabel value="#{deliveryMessages.key}"/>
  </h5>
  <h:graphicImage  alt="#{deliveryMessages.alt_unans_q}" url="/images/whiteBubble15.png" />
  <h:outputText value="#{deliveryMessages.unans_q}" /><br/>
  <h:graphicImage  alt="#{deliveryMessages.alt_ans_q}" url="/images/blackBubble15.png" />
  <h:outputText value="#{deliveryMessages.ans_q}" /><br/>
  <h:graphicImage  alt="#{deliveryMessages.alt_q_marked}" url="/images/questionMarkBubble15.png" rendered="#{delivery.displayMardForReview}" />
  <h:outputText value="#{deliveryMessages.q_marked}" rendered="#{delivery.displayMardForReview}"/>

<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
<h:commandLink id="submitforgrade" action="#{delivery.submitForGrade}" value="" />

    <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
    <p style="margin-bottom:0"><h:outputText value="#{deliveryMessages.seeOrHide}" /> </p>
    <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
      <h:column>
      <h:panelGroup>
        <samigo:hideDivision id="part" title = "#{deliveryMessages.p} #{part.number} - #{part.nonDefaultText}  -
       #{part.questions-part.unansweredQuestions}/#{part.questions} #{deliveryMessages.ans_q}, #{part.pointsDisplayString}#{part.roundedMaxPoints} #{deliveryMessages.pt}" > 
        <h:dataTable value="#{part.itemContents}" var="question">
          <h:column>
            <f:verbatim><div class="tier3"></f:verbatim>
            <h:panelGroup>
            <h:graphicImage alt="#{deliveryMessages.alt_unans_q}" 
               url="/images/whiteBubble15.png" rendered="#{question.unanswered}"/>
            <h:graphicImage alt="#{deliveryMessages.alt_unans_q}" 
               url="/images/blackBubble15.png" rendered="#{!question.unanswered}"/>
            <h:graphicImage alt="#{deliveryMessages.alt_q_marked}"
               url="/images/questionMarkBubble15.png"  rendered="#{question.review}"/>
              <h:commandLink title="#{deliveryMessages.t_takeAssessment}" immediate="true" action="takeAssessment"> 
                <h:outputText escape="false" value="#{question.sequence}#{deliveryMessages.dot} #{question.strippedText}">
                	<f:convertNumber maxFractionDigits="2"/>
                </h:outputText>
                <h:outputText escape="false" value=" (#{question.pointsDisplayString}#{question.roundedMaxPointsToDisplay} #{deliveryMessages.pt})" rendered="#{(delivery.settings.displayScoreDuringAssessments != '2' && question.itemData.scoreDisplayFlag) || question.pointsDisplayString!=''}">
                	<f:convertNumber maxFractionDigits="2"/>
                </h:outputText>
                <f:param name="partnumber" value="#{part.number}" />
                <f:param name="questionnumber" value="#{question.number}" />
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerFromTOCListener" />
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
              </h:commandLink>
            </h:panelGroup>
            <f:verbatim></div></f:verbatim> 
          </h:column>
        </h:dataTable>
       </samigo:hideDivision>
      </h:panelGroup>
      </h:column>
    </h:dataTable>
</div>

<p class="act">
<!-- SUBMIT FOR GRADE BUTTON FOR TAKE ASSESSMENT AND PREVIEW ASSESSMENT -->
  <!-- check permisison to determine if the button should be displayed -->
  <h:panelGroup rendered="#{delivery.actionString=='previewAssessment'
                         || (delivery.actionString=='takeAssessment' 
                             && authorization!=null 
                             && authorization.takeAssessment 
                             && authorization.submitAssessmentForGrade)}">
    <h:commandButton id="submitForGradeTOC1" type="submit" value="#{deliveryMessages.button_submit_grading}"
      action="#{delivery.confirmSubmitTOC}" styleClass="active"  
      onclick="saveTime()" 
      disabled="#{delivery.actionString=='previewAssessment'}" />
  </h:panelGroup>

<!-- SUBMIT BUTTON FOR TAKE ASSESSMENT VIA URL ONLY -->
  <h:commandButton id="submitForGradeTOC2" type="submit" value="#{deliveryMessages.button_submit_grading}"
    action="#{delivery.confirmSubmitTOC}" styleClass="active"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl'}" />

<!-- SAVE AND EXIT BUTTON FOR TAKE ASSESMENT AND PREVIEW ASSESSMENT-->
  <h:commandButton id="exitTOC1" type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}"
    onclick="saveTime()" 
    rendered="#{(delivery.actionString=='takeAssessment'
             || delivery.actionString=='previewAssessment') && !delivery.hasTimeLimit}" 
    disabled="#{delivery.actionString=='previewAssessment'}" />

<!-- QUIT BUTTON FOR TAKE ASSESSMENT VIA URL -->
  <h:commandButton id="exitTOC2" type="submit" value="#{deliveryMessages.button_exit}"
    action="#{delivery.saveAndExit}"
    onclick="saveTime()" 
    rendered="#{delivery.actionString=='takeAssessmentViaUrl' && !delivery.hasTimeLimit}" >
  </h:commandButton>
</p>

<!-- DONE BUTTON FOR PREVIEW ASSESSMENT ONLY -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="previewMessage"></f:verbatim>
     <h:outputText value="#{deliveryMessages.ass_preview}" />
     <h:commandButton value="#{deliveryMessages.done}" action="#{person.cleanResourceIdListInPreview}" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
<!-- end content -->
  </div>
</div>
    </body>
  </html>
</f:view>

