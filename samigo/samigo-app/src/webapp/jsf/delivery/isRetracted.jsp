<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!--
* $Id: timeout.jsp 4069 2005-11-21 19:33:41Z hquinn@stanford.edu $
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
      <title><h:outputText value="#{deliveryMessages.is_retracted_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
  <!-- content... -->
  <h3><h:outputText value="#{deliveryMessages.is_retracted_title}"/></h3>
  <h:outputText value="#{deliveryMessages.is_retracted}" rendered="#{delivery.actionString!='takeAssessmentViaUrl'}"/>
  <h:outputText value="#{deliveryMessages.is_retracted_url}" rendered="#{delivery.actionString=='takeAssessmentViaUrl'}"/>
  <p></p> 
  <h:panelGroup  rendered="#{delivery.publishedAssessment.assessmentAccessControl.retractDate!=null}" >
	<h:outputLabel value="#{deliveryMessages.retract_date}:" />
	<h:outputText value="#{delivery.publishedAssessment.assessmentAccessControl.retractDate}" >
		<f:convertDateTime dateStyle="medium" type="both" locale="#{UserLocale.locale}" timeZone="#{UserTimeZone.userTimeZone}"/>
	</h:outputText>
  </h:panelGroup> 


 <h:form id="isRetracted">
 <p class="act">
       <h:commandButton value="#{deliveryMessages.button_return}" type="submit"
         styleClass="active" action="select" rendered="#{delivery.actionString!='takeAssessmentViaUrl'}">
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>
 </p>
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
