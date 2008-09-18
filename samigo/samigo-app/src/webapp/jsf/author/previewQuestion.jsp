<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
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
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
      <title><h:outputText value="#{authorMessages.create_modify_a}" /></title>
      </head>
<body onload="document.forms[0].reset();;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<h:form id="assesssmentForm">

<h:messages styleClass="validation"/>
 <h:panelGrid columns="2" border="1" width="100%">
    <h:panelGroup>
       <h:outputText styleClass="tier1" value="#{itemContents.itemData.type.keyword}" />
       <h:outputText styleClass="tier1" value="#{itemContents.itemData.score}" />
       <h:outputText styleClass="tier1" value="#{authorMessages.points_lower_case}" />
    </h:panelGroup>

 </h:panelGrid>

        <h:panelGrid>
          <h:panelGroup rendered="#{itemContents.itemData.typeId == 9}">
            <%@ include file="/jsf/author/questionpreview/Matching.jsp" %>
          </h:panelGroup>
 <h:panelGroup rendered="#{itemContents.itemData.typeId == 11}">
            <%@ include file="/jsf/author/questionpreview/FillInNumeric.jsp" %>
          </h:panelGroup>


          <h:panelGroup rendered="#{itemContents.itemData.typeId == 8}">
            <%@ include file="/jsf/author/questionpreview/FillInTheBlank.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 7}">
            <%@ include file="/jsf/author/questionpreview/AudioRecording.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 6}">
            <%@ include file="/jsf/author/questionpreview/FileUpload.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 5}">
            <%@ include file="/jsf/author/questionpreview/ShortAnswer.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 4}">
            <%@ include file="/jsf/author/questionpreview/TrueFalse.jsp" %>
          </h:panelGroup>

          <!-- same as multiple choice single -->
          <h:panelGroup rendered="#{itemContents.itemData.typeId == 1}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceSingleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 2}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

		   <h:panelGroup rendered="#{itemContents.itemData.typeId == 12}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceMultipleCorrect.jsp" %>
          </h:panelGroup>

          <h:panelGroup rendered="#{itemContents.itemData.typeId == 3}">
            <%@ include file="/jsf/author/questionpreview/MultipleChoiceSurvey.jsp" %>
          </h:panelGroup>

        </h:panelGrid>
<p class="act">
  <h:commandButton id="back" accesskey="#{authorMessages.a_back}" value="#{authorMessages.button_back}"  action="editPool" type="submit" styleClass="active"/>

</p>

</h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>

