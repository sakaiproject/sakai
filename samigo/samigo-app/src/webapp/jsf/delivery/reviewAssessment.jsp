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
      <title><h:outputText value="#{deliveryMessages.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<h:form id="takeAssessmentForm">

<h:outputText value="#{deliveryMessages.review} " />

<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->

<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<%-- PART/ITEM DATA TABLES --%>
<div class="tier2">
  <h:dataTable value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
     <f:subview id="parts">
      <f:verbatim><h4 class="tier1"></f:verbatim>
      <h:outputText value="#{deliveryMessages.p} #{part.number} #{deliveryMessages.of} #{part.numParts}" />
      <h:outputText value=" #{deliveryMessages.dash} #{part.text} />
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{deliveryMessages.ans_q}, " / -->
      <!-- h:outputText value="#{part.points}/#{part.roundedMaxPoints} #{deliveryMessages.pt}" / -->
      <f:verbatim></h4><div class="tier1"></f:verbatim>
      <h:outputText value="#{part.description}" escape="false"/>
      <f:verbatim></div></f:verbatim>
      <h:dataTable value="#{part.itemContents}" columnClasses="tier2"
          var="question">
        <h:column>
          <f:verbatim><h4 class="tier1"></f:verbatim>
          <h:outputText value="#{deliveryMessages.q} #{question.number} #{deliveryMessages.of} " />
          <h:outputText value="#{part.itemContentsSize} " />
          <h:outputText value="#{question.points}#{deliveryMessages.splash}#{question.roundedMaxPoints} " />
          <h:outputText value="#{deliveryMessages.pt}"/>
          <f:verbatim></h4></f:verbatim>
          <h:outputText value="#{question.itemData.description}" escape="false"/>
          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
           <f:subview id="deliverAudioRecording">
           <%@ include file="/jsf/delivery/itemreview/deliverAudioRecording.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
           <f:subview id="deliverFileUpload">
           <%@ include file="/jsf/delivery/itemreview/deliverFileUpload.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 11}">
	       <f:subview id="deliverFillInNumeric">
	       <%@ include file="/jsf/delivery/item/deliverFillInNumeric.jsp" %>
	       </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 8}">
           <f:subview id="deliverFillInTheBlank">
           <!-- %@ include file="/jsf/delivery/item/deliverFillInTheBlank.jsp" % -->
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 9}">
           <f:subview id="deliverMatching">
           <!-- %@ include file="/jsf/delivery/item/deliverMatching.jsp" % -->
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 15}"><!-- // CALCULATED_QUESTION -->
           <f:subview id="deliverCalculatedQuestion">
           <!-- %@ include file="/jsf/delivery/item/deliverCalculatedQuestion.jsp" % -->
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 16}"><!-- // IMAGEMAP_QUESTION -->
           <f:subview id="deliverImageMapQuestion">
           <!-- %@ include file="/jsf/delivery/item/deliverImageMapQuestion.jsp" % -->
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
           <%@ include file="/jsf/delivery/itemreview/deliverTrueFalse.jsp" %>
           </f:subview>
          </h:panelGroup>
          <h:panelGroup rendered="#{question.itemData.typeId == 13}">
            <f:subview id="deliverTrueFalse">
            <%@ include file="/jsf/delivery/itemreview/deliverMatrixChoicesSurvey.jsp" %>
            </f:subview>
          </h:panelGroup>
        </h:column>
      </h:dataTable>
     </f:subview>
    </h:column>
  </h:dataTable>
</div>
<h:panelGrid columns="3" cellpadding="3" cellspacing="3">
  <h:commandButton type="submit" value="#{commonMessages.cancel_action}" action="select">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>
</h:panelGrid>
</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>
