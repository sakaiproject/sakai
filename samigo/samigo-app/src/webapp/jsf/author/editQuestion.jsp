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
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- FORM -->
<h:form id="itemForm">

<h:inputHidden id="assessmentID" value="#{item.assessmentID}"/>
<h:inputHidden id="assessTitle" value="#{item.assessTitle}" />
<h:inputHidden id="ItemIdent" value="#{item.ItemIdent}"/>
<h:inputHidden id="ItemIdent" value="#{item.itemNo}"/>
<h:inputHidden id="currentSection" value="#{item.currentSection}"/>
<h:inputHidden id="insertPosition" value="#{item.insertPosition}"/>


<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
  <!-- QUESTION PROPERTIES -->

<!-- TODO:
This will have all the other files in ./item included here.
We need to find a strategy for doing this.

something like if type true false render include true false etc.
-->
<h:panelGrid columns="2" cellpadding="3" cellspacing="3">
  <h:commandButton type="submit" value="#{commonMessages.action_save}" action="editAssessment">
    <f:actionListener
      type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorQuestionListener" />
  </h:commandButton>
  <h:commandButton type="submit" value="#{commonMessages.cancel_action}" action="editAssessment">
    <f:actionListener
      type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorQuestionListener" />
  </h:commandButton>
</h:panelGrid>

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>
