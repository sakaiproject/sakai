<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
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
     basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      </head>
      <h:outputText value="<body #{delivery.settings.bgcolor} #{delivery.settings.background} onLoad='checkRadio();'>" escape="false" />
      <h:outputText value="<a name='top'></a>" escape="false" />

<div class="portletBody">
<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data"
   onsubmit="saveTime()">

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
  if((typeof (document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=undefined) && ((document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'])!=null) ){
  pauseTiming = 'true';
  document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded/10;
 }
}

</script>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished ne 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment" type="submit"/>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<h:panelGroup rendered="#{delivery.previewAssessment eq 'true' && delivery.notPublished eq 'true'}">
 <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.ass_preview}" />
     <h:commandButton value="#{msg.done}" action="editAssessment">
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
     </h:commandButton>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

<!-- HEADING -->

<f:subview id="assessmentDeliveryHeading">
<%@ include file="/jsf/delivery/assessmentDeliveryHeading.jsp" %>
</f:subview>
<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->
<font color="red"><h:messages/></font>
<h:inputHidden id="assessmentID" value="#{delivery.assessmentId}"/>
<h:inputHidden id="assessTitle" value="#{delivery.assessmentTitle}" />
<!-- h:inputHidden id="ItemIdent" value="#{item.ItemIdent}"/ -->
<!-- h:inputHidden id="ItemIdent2" value="#{item.itemNo}"/ -->
<!-- h:inputHidden id="currentSection" value="#{item.currentSection}"/ -->
<!-- h:inputHidden id="insertPosition" value="#{item.insertPosition}"/ -->
<%-- PART/ITEM DATA TABLES --%>
<div class="tier2">
  <h:dataTable value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
     <!-- f:subview id="parts" -->
      <f:verbatim><h4 class="tier1"></f:verbatim>
      <h:outputText value="#{msg.p} #{part.number} #{msg.of} #{part.numParts}" />
      <h:outputText value=" - #{part.nonDefaultText}" escape="false"/>
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{msg.ans_q}, " / -->
      <!-- h:outputText value="#{part.points}/#{part.maxPoints} #{msg.pt}" / -->
      <f:verbatim></h4><div class="indnt1"></f:verbatim>
      <h:outputText value="#{part.description}" escape="false" rendered="#{part.numParts ne '1'}" />
      <f:verbatim></div></f:verbatim>
      <h:dataTable value="#{part.itemContents}" columnClasses="tier2"
          var="question">
        <h:column>
          <f:verbatim><h4 class="tier2"></f:verbatim>
           <h:outputText value="<a name=p#{part.number}q#{question.number}></a>" escape="false" />

        <h:outputText value="#{msg.q} #{question.sequence} #{msg.of} #{part.numbering} : #{question.maxPoints} #{msg.pt}"/>

          <f:verbatim></h4><div class="indnt3"></f:verbatim>
          <h:outputText value="#{question.itemData.description}" escape="false"/>
          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
           <f:subview id="deliverAudioRecording">
           <%@ include file="/jsf/delivery/item/deliverAudioRecording.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
           <f:subview id="deliverFileUpload">
           <%@ include file="/jsf/delivery/item/deliverFileUpload.jsp" %>
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
          <h:panelGroup
            rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 3}">
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
           <%@ include file="/jsf/delivery/item/deliverShortAnswer.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 4}">
           <f:subview id="deliverTrueFalse">
           <%@ include file="/jsf/delivery/item/deliverTrueFalse.jsp" %>
           </f:subview>
           <f:verbatim></div></f:verbatim>
          </h:panelGroup>
        </h:column>
      </h:dataTable>
     <!-- /f:subview -->
    </h:column>
  </h:dataTable>
</div>
<p class="act">

  <h:commandButton type="submit" value="#{msg.save_and_continue}"
    action="#{delivery.next_page}" styleClass="active"
    rendered="#{delivery.previewMode ne 'true' && delivery.continue}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.save_and_continue}"
    action="tableOfContents" styleClass="active"
    rendered="#{delivery.previewAssessment ne 'true' && delivery.previewMode ne 'true' && delivery.navigation ne '1' && !delivery.continue}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
 </h:commandButton>

 <h:commandButton type="submit" value="#{msg.save_and_continue}"
    action="tableOfContents" styleClass="active"
    rendered="#{delivery.previewAssessment eq 'true' && delivery.previewMode ne 'true' && delivery.navigation ne '1' && !delivery.continue}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
 </h:commandButton>

  <h:commandButton type="submit" value="#{msg.previous}"
    action="#{delivery.previous}"
    rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1' && delivery.previous}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_submit_grading}"
    action="#{delivery.submitForGrade}"  id="submitForm" styleClass="active"
    rendered="#{delivery.accessViaUrl || (delivery.previewMode ne 'true' && delivery.navigation eq '1' && !delivery.continue) }" disabled="#{delivery.previewAssessment eq 'true'}"
    onclick="pauseTiming='true'">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_save_x}"
    action="#{delivery.saveAndExit}" id="saveAndExit"
    rendered="#{delivery.previewMode ne 'true' && (delivery.navigation ne '1'&& delivery.continue) && !delivery.accessViaUrl}"  onclick="pauseTiming='true'" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

  <h:commandButton type="submit" value="#{msg.button_quit}"
    action="#{delivery.saveAndExit}" id="quit"
    rendered="#{delivery.previewMode ne 'true' && delivery.accessViaUrl}" onclick="pauseTiming='true'" disabled="#{delivery.previewAssessment eq 'true'}">
  </h:commandButton>

<h:commandButton type="submit" value="#{msg.button_save_x}"
    action="#{delivery.saveAndExit}" id="saveAndExit2"
    rendered="#{delivery.previewMode ne 'true' && (delivery.navigation eq '1'||!delivery.continue) && !delivery.accessViaUrl}" disabled="#{delivery.previewAssessment eq 'true'}">
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
     <h:commandButton value="#{msg.done}" action="editAssessment">
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePublishedAssessmentListener" />
     </h:commandButton>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>
