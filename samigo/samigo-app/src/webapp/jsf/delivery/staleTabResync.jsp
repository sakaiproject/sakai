<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
<%--
***********************************************************************************
*
* Copyright (c) 2026 The Apereo Foundation
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://opensource.org/licenses/ecl2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* SAK-44349: non-fatal landing page when a stale tab's post was rejected by
* the delivery state guard. Saved answers are untouched; one click resumes
* the attempt at its current position.
**********************************************************************************/
--%>
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{deliveryMessages.stale_tab_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
  <!-- content... -->
  <div id="samigo-stale-tab-marker" class="d-none">staleTabResync</div>
  <h:panelGroup rendered="#{delivery.assessmentSubmitted}">
    <f:verbatim><div id="samigo-attempt-submitted-marker" class="d-none">submitted</div></f:verbatim>
  </h:panelGroup>
  <h3><h:outputText value="#{deliveryMessages.stale_tab_title}"/></h3>
  <div class="sak-banner-warn">
    <h:outputText value="#{deliveryMessages.stale_tab_1}"/>
    <br/><br/>
    <h:outputText value="#{deliveryMessages.stale_tab_2}"/>
  </div>

 <h:form id="staleTabResync">
 <p class="act">
       <h:commandButton id="continueAssessment1" value="#{deliveryMessages.stale_tab_continue}"
           action="#{delivery.validate}" type="submit" styleClass="active"
           rendered="#{(delivery.actionString=='takeAssessment'
                    || delivery.actionString=='takeAssessmentViaUrl')
                    && delivery.navigation != 1}">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
       </h:commandButton>
       <h:commandButton id="continueAssessment2" value="#{deliveryMessages.stale_tab_continue}"
           action="#{delivery.validate}" type="submit" styleClass="active"
           rendered="#{(delivery.actionString=='takeAssessment'
                    || delivery.actionString=='takeAssessmentViaUrl')
                    && delivery.navigation == 1}">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.LinearAccessDeliveryActionListener" />
       </h:commandButton>
       <h:commandButton value="#{deliveryMessages.data_discrepancy_button}" type="submit" action="select"
           rendered="#{delivery.actionString!='takeAssessmentViaUrl'}">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>
 </p>
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
