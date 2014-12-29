<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id: reviewAssessmentError.jsp 22618 2007-03-14 19:58:35Z ktsao@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2008 The Sakai Foundation.
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
      <title> <h:outputText value="#{delivery.assessmentTitle}"/>
      </title>
      </head>
       <body onload="<%= request.getAttribute("html.body.onload") %>;">

      <h:outputText value="<a name='top'></a>" escape="false" />
 <h:outputText value="<div class='portletBody' style='#{delivery.settings.divBgcolor};#{delivery.settings.divBackground}'>" escape="false"/>

<!-- content... -->
<h:form id="takeAssessmentForm" enctype="multipart/form-data">

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<!-- HEADING -->
<f:subview id="assessmentDeliveryHeading">
<%@ include file="/jsf/delivery/assessmentDeliveryHeading.jsp" %>
</f:subview>

  <h:panelGroup styleClass="messageSamigo">
    <h:panelGrid  columns="1">
	   <h:outputText value="#{deliveryMessages.review_error_1}" />
       <h:outputText value="#{deliveryMessages.review_error_2}"/>
    </h:panelGrid>
  </h:panelGroup>

</h:form>
<!-- end content -->
</div>
    </body>
  </html>
</f:view>
