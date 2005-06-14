<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: reviewAssessment.jsp,v 1.5 2005/05/24 16:54:49 janderse.umich.edu Exp $ -->
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
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<h:form id="takeAssessmentForm">

<h:outputText value="this is a review " />

<!-- FORM ... note, move these hiddens to whereever they are needed as fparams-->

<font color="red"><h:messages/></font>

<%-- PART/ITEM DATA TABLES --%>
<div class="tier2">
  <h:dataTable value="#{delivery.pageContents.partsContents}" var="part">
    <h:column>
     <f:subview id="parts">
      <f:verbatim><h4 class="tier1"></f:verbatim>
      <h:outputText value="#{msg.p} #{part.number} #{msg.of} #{part.numParts}" />  
      <h:outputText value=" - #{part.text} />
      <!-- h:outputText value="#{part.unansweredQuestions}/#{part.questions} " / -->
      <!-- h:outputText value="#{msg.ans_q}, " / -->
      <!-- h:outputText value="#{part.points}/#{part.maxPoints} #{msg.pt}" / -->
      <f:verbatim></h4><div class="indnt1"></f:verbatim>
      <h:outputText value="#{part.description}" escape="false"/>
      <f:verbatim></div></f:verbatim>
      <h:dataTable value="#{part.itemContents}" columnClasses="tier2"
          var="question">
        <h:column>
          <f:verbatim><h4 class="tier1"></f:verbatim>
          <h:outputText value="#{msg.q} #{question.number} #{msg.of} " />
          <h:outputText value="#{part.itemContentsSize} " />
          <h:outputText value="#{question.points}/#{question.maxPoints} " />
          <h:outputText value="#{msg.pt}"/>
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
        </h:column>
      </h:dataTable>
     </f:subview>
    </h:column>
  </h:dataTable>
</div>
<h:panelGrid columns="3" cellpadding="3" cellspacing="3">
  <h:commandButton type="submit" value="#{msg.button_cancel}" action="select">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
  </h:commandButton>
</h:panelGrid>
</h:form>
<!-- end content -->
    </body>
  </html>
</f:view>
