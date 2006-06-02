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

  <f:loadBundle
   basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
   var="msg"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head><%= request.getAttribute("html.head") %>
    <title><h:outputText value="#{msg.table_of_contents}" /></title>
    <samigo:script path="/jsf/widget/hideDivision/hideDivision.js" />
    </head>
    <body onload="hideUnhideAllDivsExceptFirst('none');;<%= request.getAttribute("html.body.onload") %>">
<!--div class="portletBody"-->

 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

 <!--h:outputText value="<div class='portletBody' style='background:#{delivery.settings.divBgcolor};background-image:url(http://www.w3.org/WAI/UA/TS/html401/images/test-background.gif)'>" escape="false"/-->
 
<!-- content... -->
<script language="javascript">

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
  document.forms[0].elements['tableOfContentsForm:elapsed'].value=loaded/10;
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

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton accesskey="#{msg.a_done}" value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h3><h:outputText value="#{delivery.assessmentTitle} " /></h3>

<h:panelGroup rendered="#{(delivery.actionString=='takeAssessment'
                           || delivery.actionString=='takeAssessmentViaUrl' 
                           || delivery.actionString=='previewAssessment')
                        && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="#{msg.time_remaining} "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
  <samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['tableOfContentsForm:elapsed'].value=loaded; document.forms[0].elements['tableOfContentsForm:outoftime'].value='true'; clickSubmitForGrade();" />
<f:verbatim>  </span></f:verbatim>

<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" onkeypress="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="Hide/Show Time Remaining" />
</h:panelGroup>


<div class="tier1">
  <f:verbatim><b></f:verbatim><h:outputText value="#{msg.warning}#{msg.column} "/><f:verbatim></b></f:verbatim>
  <h:outputText value="#{msg.instruction_submitGrading}" />
</div>

<div class="tier1">
  <h4>
    <h:outputText value="#{msg.table_of_contents} " />
    <h:outputText styleClass="tier10" value="#{msg.tot_score} " />
    <h:outputText value="#{delivery.tableOfContents.maxScore}">
      <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText value="#{msg.pt}" />
  </h4>
 
</div>

<div class="tier2">
  <h5>
    <h:outputLabel value="#{msg.key}"/>
  </h5>
  <h:graphicImage  alt="#{msg.alt_unans_q}" url="/images/tree/blank.gif" />
  <h:outputText value="#{msg.unans_q}" /><br/>
  <h:graphicImage  alt="#{msg.alt_q_marked}" url="/images/tree/marked.gif" />
  <h:outputText value="#{msg.q_marked}" />

<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
<h:commandLink id="submitforgrade" action="#{delivery.submitForGrade}" value="" />

    <h:messages styleClass="validation"/>
    <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
      <h:column>
      <h:panelGroup>
        <samigo:hideDivision id="part" title = "#{msg.p} #{part.number} - #{part.nonDefaultText}  -
       #{part.questions-part.unansweredQuestions}/#{part.questions} #{msg.ans_q}, #{part.pointsDisplayString}#{part.roundedMaxPoints} #{msg.pt}" > 
        <h:dataTable value="#{part.itemContents}" var="question">
          <h:column>
            <f:verbatim><div class="tier3"></f:verbatim>
            <h:panelGroup>
            <h:graphicImage alt="#{msg.alt_unans_q}" 
               url="/images/tree/blank.gif" rendered="#{question.unanswered}"/>
            <h:graphicImage alt="#{msg.alt_q_marked}"
               url="/images/tree/marked.gif"  rendered="#{question.review}"/>
              <h:commandLink title="#{msg.t_takeAssessment}" immediate="true" action="takeAssessment"> 
                <h:outputText escape="false" value="#{question.sequence}#{msg.dot} #{question.strippedText} (#{question.pointsDisplayString}#{question.roundedMaxPoints} #{msg.pt})">
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
    <h:commandButton accesskey="#{msg.a_submit}" type="submit" value="#{msg.button_submit_grading}"
      action="#{delivery.submitForGrade}" styleClass="active"  
      onclick="javascript:saveTime()" onkeypress="javascript:saveTime()"
      disabled="#{delivery.actionString=='previewAssessment'}" />
  </h:panelGroup>

<!-- SUBMIT BUTTON FOR TAKE ASSESSMENT VIA URL ONLY -->
  <h:commandButton accesskey="#{msg.a_submit}" type="submit" value="#{msg.button_submit}"
    action="#{delivery.submitForGrade}" styleClass="active"   
    rendered="#{delivery.actionString=='takeAssessmentViaUrl'}" />

<!-- SAVE AND EXIT BUTTON FOR TAKE ASSESMENT AND PREVIEW ASSESSMENT-->
  <h:commandButton accesskey="#{msg.a_saveAndExit}" type="submit" value="#{msg.button_save_x}"
    action="#{delivery.saveAndExit}"
    onclick="javascript:saveTime()" onkeypress="javascript:saveTime()"
    rendered="#{delivery.actionString=='takeAssessment'
             || delivery.actionString=='previewAssessment'}" 
    disabled="#{delivery.actionString=='previewAssessment'}" />

<!-- QUIT BUTTON FOR TAKE ASSESSMENT VIA URL -->
  <h:commandButton accesskey="#{msg.a_quit}" type="submit" value="#{msg.button_quit}"
    action="#{delivery.saveAndExit}" id="quit"
    onclick="javascript:saveTime()" onkeypress="javascript:saveTime()"
    rendered="#{delivery.actionString=='takeAssessmentViaUrl'}" >
  </h:commandButton>
</p>

<!-- DONE BUTTON FOR PREVIEW ASSESSMENT ONLY -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton accesskey="#{msg.a_done}" value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>

