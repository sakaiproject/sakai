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
 <h:outputText value="<div class='portletBody' style='background:#{delivery.settings.divBgcolor}'>" escape="false"/>

<!-- content... -->
<script language="javascript">
function checkRadio()
{
  for (i=0; i<document.forms[0].elements.length; i++)
  {
    if (document.forms[0].elements[i].type == "radio")
    {
      if (document.forms[0].elements[i].defaultChecked == true)
      {
        document.forms[0].elements[i].click();
      }
    }
  }

}

function noenter(){
return!(window.event && window.event.keyCode == 13);
}

function saveTime()
{
  if((typeof (document.forms[0].elements['takeAssessmentFormTOC:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentFormTOC:elapsed'])!=null) ){
  pauseTiming = 'true';
  document.forms[0].elements['takeAssessmentFormTOC:elapsed'].value=loaded;
 }
 if( (typeof (document.forms[1].elements['tableOfContentsForm:wninFpevcgRanoyrqPurpx']))!=undefined
  && (document.forms[1].elements['tableOfContentsForm:wninFpevcgRanoyrqPurpx'])!=null ){
  document.forms[1].elements['tableOfContentsForm:wninFpevcgRanoyrqPurpx'].value='true';

  }
//alert("document.forms[1].elements['tableOfContentsForm:wninFpevcgRanoyrqPurpx'].value=" +
//document.forms[1].elements['tableOfContentsForm:wninFpevcgRanoyrqPurpx'].value);

}

</script>

<h:form id="takeAssessmentFormTOCTop">


<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished ne 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished eq 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</h:form>

<%-- <h:form onsubmit="saveTime()"> --%>
<%-- </h:form>  --%>

<h3><h:outputText value="#{delivery.assessmentTitle} " /></h3>

<h:panelGroup rendered="#{delivery.previewMode eq 'false' && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="Time Remaining: "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
  <samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['takeAssessmentFormTOC:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentFormTOC:outoftime'].value='true'; document.forms[0].elements['takeAssessmentForm:saveAndExit'].click();" />
<f:verbatim>  </span></f:verbatim>

<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="Hide/Show Time Remaining" />
<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
</h:panelGroup>

<div class="indnt1">
  <f:verbatim><b></f:verbatim><h:outputText value="#{msg.warning}: "/><f:verbatim></b></f:verbatim>
  <h:outputText value="#{msg.instruction_submitGrading}" />
</div>

<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />

<div class="indnt1">
  <h4>
    <h:outputText value="#{msg.table_of_contents} " />
    <h:outputText styleClass="tier10" value="#{msg.tot_score} " />
    <h:outputText value="#{delivery.tableOfContents.maxScore} " />
    <h:outputText value="#{msg.pt}" />
  </h4>
  <h:outputText value="#{msg.table_of_contents} " />
  <h:outputText styleClass="tier10" value="#{msg.tot_score} " />
  <h:outputText value="#{delivery.tableOfContents.maxScore} " />
  <h:outputText value="#{msg.pt}" />
</div>

<div class="indnt2">
  <h5 class="plain">
    <h:outputLabel value="#{msg.key}"/>
  </h5>
  <h:graphicImage  alt="#{msg.unans_q}" url="/images/tree/blank.gif" />
  <h:outputText value="#{msg.unans_q}" /><br>
  <h:graphicImage  alt="#{msg.q_marked}" url="/images/tree/marked.gif" />
  <h:outputText value="#{msg.q_marked}" />

  <h:form id="tableOfContentsForm" onsubmit="saveTime()">
    <h:inputHidden id="wninFpevcgRanoyrqPurpx" value="#{delivery.javaScriptEnabledCheck}" />
    <h:messages styleClass="validation"/>
    <h:dataTable value="#{delivery.tableOfContents.partsContents}" var="part">
      <h:column>
      <h:panelGroup>
        <samigo:hideDivision id="part" title = "#{msg.p} #{part.number} - #{part.nonDefaultText}  -
       #{part.questions-part.unansweredQuestions}/#{part.questions} #{msg.ans_q}, #{part.pointsDisplayString}#{part.maxPoints} #{msg.pt}" >
        <h:dataTable value="#{part.itemContents}" var="question">
          <h:column>
            <f:verbatim><div class="indnt3"></f:verbatim>
            <h:panelGroup>
            <h:graphicImage alt="#{msg.unans_q}" 
               url="/images/tree/blank.gif" rendered="#{question.unanswered}"/>
            <h:graphicImage alt="#{msg.q_marked}"
               url="/images/tree/marked.gif"  rendered="#{question.review}"/>
              <h:commandLink immediate="true" action="takeAssessment" onmouseup="saveTime();">
                <h:outputText value="#{question.sequence}. #{question.strippedText} (#{question.pointsDisplayString}#{question.maxPoints} #{msg.pt}) " escape="false" />
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
                <f:param name="partnumber" value="#{part.number}" />
                <f:param name="questionnumber" value="#{question.number}" />
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
  <!-- check permisison to determine if the button should be displayed -->
  <h:panelGroup rendered="#{delivery.accessViaUrl or (authorization!=null &&  authorization.takeAssessment && authorization.submitAssessmentForGrade)}">
  <h:commandButton type="submit" value="#{msg.button_submit_grading}"
    action="#{delivery.submitForGrade}" styleClass="active"  rendered="#{!delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>
  </h:panelGroup>

 <h:commandButton type="submit" value="#{msg.button_submit}"
    action="#{delivery.submitForGrade}" styleClass="active"   rendered="#{delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_save_x}"
    action="#{delivery.saveAndExit}"
    rendered="#{!delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_quit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>
</p>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished ne 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished eq 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>

