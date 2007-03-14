<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
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
      <title><h:outputText value="#{questionPoolMessages.add_to_assmt}"/></title>
			<!-- stylesheet and script widgets go here -->
			<samigo:script path="/js/treeJavascript.js" />
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<div class="portletBody">
<div class="heading"><h:outputText value="#{questionPoolMessages.add_q}"></div>
 <f:verbatim><br /></f:verbatim>
 <h:form id="questionpool">
 <f:verbatim><br /></f:verbatim>
 <h2>Question Text</h2>
 <f:verbatim><br /></f:verbatim>
  <!-- this may need to be fixed, as this was exptected in session?? -->
  <h:dataTable
    styleClass="tblMain" headerClass="altBackground" rowClasses="trEven, trOdd"
    value="#{questionpool.currentPool.properties.selectedItems}" var="question">
    <h:column>
			<h:outputText value="#{question.itemText}" escape="false" />
    </h:column>
  </h:dataTable>
  <f:verbatim><br /><br /></f:verbatim>
  <h:panelGrid columns="2" columnClasses="number,instructionsSteps">

  <h:outputText value="1"/>
  <h:outputText value="#{questionPoolMessages.add_q_to_assmt}"/>

  <!-- datasource here too needs to be fixed -->
  <h:outputText value=" "/>
  <h:panelGroup>
    <h:outputText value="#{questionPoolMessages.assmt_title}" />
    <h:selectOneMenu>
      <f:selectItems value="#{allAssets.assessmentID}" />
    </h:selectOneMenu>
  </h:panelGroup>

  <h:outputText value="2"/>
  <h:outputText value="#{questionPoolMessages.click_save}"/>

 <f:verbatim><br /><br />
  <center>
  <h:commandButton accesskey="#{questionPoolMessages.a_save}" type="submit" id="Submit" value="#{questionPoolMessages.save}"
    action="addToAssessment"/>
  <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" type="cancel" id="Cancel" value="#{questionPoolMessages.cancel}"
    action="canceladdToAssessment"/>
  </center></f:verbatim>
 </h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
