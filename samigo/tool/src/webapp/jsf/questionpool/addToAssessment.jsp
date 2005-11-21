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
   
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.add_to_assmt}"/></title>
			<!-- stylesheet and script widgets go here -->
			<samigo:script path="/js/treeJavascript.js" />
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<div class="portletBody">
<div class="heading"><h:outputText value="#{msg.add_q}"></div>
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
  <h:outputText value="#{msg.add_q_to_assmt}"/>

  <!-- datasource here too needs to be fixed -->
  <h:outputText value=" "/>
  <h:panelGroup>
    <h:outputText value="#{msg.assmt_title}" />
    <h:selectOneMenu>
      <f:selectItems value="#{allAssets.assessmentID}" />
    </h:selectOneMenu>
  </h:panelGroup>

  <h:outputText value="2"/>
  <h:outputText value="#{msg.click_save}"/>

 <f:verbatim><br /><br />
  <center>
  <h:commandButton type="submit" id="Submit" value="#{msg.save}"
    action="addToAssessment"/>
  <h:commandButton type="cancel" id="Cancel" value="#{msg.cancel}"
    action="canceladdToAssessment"/>
  </center></f:verbatim>
 </h:form>
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
