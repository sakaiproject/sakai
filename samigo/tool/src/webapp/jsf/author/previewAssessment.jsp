<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.create_modify_a}" /></title>
      </head>
<body onload="document.forms[0].reset();;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<h:form id="assesssmentForm">

<h:messages/>

 <div align="left">
    <h3>
        <h:outputText value="#{assessmentBean.title}" />
    </h3>
 </div>

<h:dataTable border="1" styleClass="listHier" id="parts" width="100%" headerClass="regHeading" value="#{assessmentBean.sections}" var="partBean">
  <%-- note that partBean is ui/delivery/SectionContentBean not ui/author/SectionBean --%>
  <h:column>
    <h:panelGrid columns="2" width="100%">
      <h:panelGroup>
        <h:outputText styleClass="tier1" value="#{msg.p}" />
        <h:outputText styleClass="tier1" value="#{partBean.number}" />
        <h:outputText styleClass="tier1" value="#{partBean.title}" />
        <f:verbatim>&nbsp;- </f:verbatim>
        <h:outputText rendered="#{partBean.sectionAuthorTypeString == null || partBean.sectionAuthorTypeString == '1'}" styleClass="tier1" value="#{partBean.questions}" />
        <h:outputText rendered="#{partBean.sectionAuthorTypeString != null && partBean.sectionAuthorTypeString == '2'}" styleClass="tier1" value="#{partBean.numberToBeDrawnString}" />
        <h:outputText styleClass="tier1" value="#{msg.questions_lower_case}" />
      </h:panelGroup>

    </h:panelGrid>

    <h:dataTable border="1" styleClass="listHier" id="parts" width="100%" headerClass="regHeading" value="#{partBean.itemContents}" var="question">

      <h:column>
        <h:panelGrid columns="2" border="1" width="100%">
          <h:panelGroup>
            <h:outputText styleClass="tier1" value="#{msg.q}" />
            <h:outputText styleClass="tier1" value="#{question.number}" />
            <h:outputText styleClass="tier1" value="#{question.itemData.type.keyword}" />
            <h:outputText styleClass="tier1" value="#{question.itemData.score}" />
            <h:outputText styleClass="tier1" value="#{msg.points_lower_case}" />
          </h:panelGroup>

        </h:panelGrid>

        <h:panelGrid>
          <h:panelGroup rendered="#{question.itemData.typeId == 9}">
            <%@ include file="/jsf/author/preview_item/Matching.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 8}">
            <%@ include file="/jsf/author/preview_item/FillInTheBlank.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 7}">
            <%@ include file="/jsf/author/preview_item/AudioRecording.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 6}">
            <%@ include file="/jsf/author/preview_item/FileUpload.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 5}">
            <%@ include file="/jsf/author/preview_item/ShortAnswer.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 4}">
            <%@ include file="/jsf/author/preview_item/TrueFalse.jsp" %>
          </h:panelGroup>

          <!-- same as multiple choice single -->
          <h:panelGroup rendered="#{question.itemData.typeId == 3}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 2}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{question.itemData.typeId == 1}">
            <%@ include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp" %>
          </h:panelGroup>

        </h:panelGrid>

</h:column>
</h:dataTable>
  </h:column>
</h:dataTable>
<p class="act">
 <h:commandButton value="#{msg.button_back}"  action="editAssessment" type="submit" styleClass="active"/>
</p>
</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>

