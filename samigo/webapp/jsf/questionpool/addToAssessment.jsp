<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.add_to_assmt}"/></title>
			<!-- stylesheet and script widgets go here -->
			<samigo:script path="/js/treeJavascript.js" />
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
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
      </body>
    </html>
  </f:view>
