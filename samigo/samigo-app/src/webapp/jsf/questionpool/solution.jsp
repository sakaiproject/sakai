<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text>&#13;
&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
&#13;
</jsp:text>
  <!-- $Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression>request.getAttribute("html.head")</jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.sol_q}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>&#13;
document.addEventListener('DOMContentLoaded', function () {&#13;
  // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.&#13;
  document.getElementById('solution:questionPoolsLink').parentElement.classList.add('current');&#13;
}, false);&#13;
</jsp:text>
        </script>
      </head>
      <body>
        <h:form id="solution">
          <!-- HEADINGS  -->
          <jsp:directive.include file="/jsf/evaluation/evaluationHeadings.jsp"/>
          <div class="tier2">
            <div class="page-header">
              <h4 class="part-header">
                <h:outputText value="#{questionPoolMessages.solution_calculated_question}"/>
              </h4>
            </div>
            <t:dataList value="#{questionpool.itemsBean}" var="question" itemStyleClass="page-header question-box" styleClass="question-wrapper" layout="unorderedList">
              <h:panelGroup layout="block" styleClass="row #{delivery.actionString}">
                <h:panelGroup layout="block" styleClass="col-sm-6">
                  <h:panelGroup layout="block" styleClass="row">
                    <h:panelGroup layout="block" styleClass="col-sm-12 input-group">
                      <p class="input-group-addon m-0">
                        <h:outputText value="#{deliveryMessages.q} 1 #{deliveryMessages.of} "/>
                        <h:outputText value="1"/>
                      </p>
                      <h:inputText readonly="true" styleClass="form-control adjustedScore#{studentScores.assessmentGradingId}.#{question.itemData.itemId}" id="adjustedScore" value="#{question.pointsForEdit}">
                      </h:inputText>
                    </h:panelGroup>
                    <h:panelGroup layout="block" styleClass="col-sm-12 input-group">
                      <p class="samigo-input-group-addon">
                        <h:outputText value=" #{deliveryMessages.splash} #{question.roundedMaxPointsToDisplay} "/>
                        <h:outputText value="#{deliveryMessages.pt}"/>
                        <h:message for="adjustedScore" styleClass="sak-banner-error"/>
                        <h:outputText styleClass="extraCreditLabel" rendered="#{question.itemData.isExtraCredit == true}" value=" #{deliveryMessages.extra_credit_preview}"/>
                      </p>
                    </h:panelGroup>
                  </h:panelGroup>
                </h:panelGroup>
              </h:panelGroup>
              <h:panelGroup rendered="#{question.itemData.typeId == 15}">
                <div class="samigo-question-callout">
                  <f:subview id="checkSolutionCalculatedQuestion">
                    <jsp:directive.include file="/jsf/questionpool/checkSolutionCalculatedQuestion.jsp"/>
                  </f:subview>
                </div>
              </h:panelGroup>
              <p class="act">
                <h:commandButton id="anotherSolution" styleClass="active" value="#{questionPoolMessages.another_solution}" action="#{questionpool.checkSolution}">
                  <f:param name="itemid" value="#{question.itemData.itemId}"/>
                  <f:param name="outCome" value="editPool"/>
                </h:commandButton>
              </p>
            </t:dataList>
            <h:commandButton style="act" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" onclick="SPNR.disableControlsAndSpin(this, null);">
              <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener"/>
              <f:attribute name="returnToParentPool" value="false"/>
            </h:commandButton>
          </div>
        </h:form>
      </body>
    </html>
  </f:view>
</jsp:root>
