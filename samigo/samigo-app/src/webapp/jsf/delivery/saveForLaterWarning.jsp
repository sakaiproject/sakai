<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!--
* $Id: saveForLaterWarning.jsp 4069 2005-11-21 19:33:41Z hquinn@stanford.edu $
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
      <title><h:outputText value="#{deliveryMessages.save_for_later_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- JAVASCRIPT -->
<%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>

<h:form id="saveForLater">
<!-- DONE BUTTON FOR PREVIEW -->
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment'}">
  <div class="sak-banner-info mb-5">
    <h:outputText value="#{deliveryMessages.ass_preview}" escape="false" />
    <br/>
    <h:commandButton value="#{deliveryMessages.exit_preview}"
      styleClass="exit-preview-button"
      action="#{person.cleanResourceIdListInPreview}"
      type="submit"
      onclick="return returnToHostUrl(\"#{delivery.selectURL}\");" />
    </div>
</h:panelGroup>

<div class="portletBody">
<h3 style="insColor insBak">
   <h:outputText value="#{deliveryMessages.assessment_exit_warning_title}" />
</h3>
<br/>
  <!-- content... -->
  <h:panelGroup styleClass="sak-banner-warn">
  <h:panelGrid border="0">
      <h:outputText value="#{deliveryMessages.save_for_later_warning_1}" escape="false"/>
      <h:outputText value="#{deliveryMessages.save_for_later_warning_2}" escape="false"/>
      <h:outputText value="#{deliveryMessages.save_for_later_warning_3}" escape="false"/>
  </h:panelGrid>
  </h:panelGroup>

 <p class="act">
  <h:commandButton id="returnToAssessment" value="#{deliveryMessages.button_return_to_assessment}" 
    action="#{delivery.validate}" type="submit" styleClass="active">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

       <h:commandButton id="continue" value="#{deliveryMessages.button_continue}" type="submit"
        action="select" disabled="#{delivery.actionString=='previewAssessment'}">
       </h:commandButton>
 </p> 
  <!-- end content -->
</div>

 </h:form>
      </body>
    </html>
  </f:view>
</html>
