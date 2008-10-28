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
      <title><h:outputText value="#{deliveryMessages.time_expired_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
  <!-- content... -->
  <h3><h:outputText value="#{deliveryMessages.no_submission_left_title}"/></h3>
  <h:outputText value="#{deliveryMessages.no_submission_left}"/>
  <h:outputLabel value="#{deliveryMessages.num_subs}" />
  <h:outputText value="#{delivery.settings.maxAttempts} (#{delivery.submissionsRemaining} #{deliveryMessages.remaining})"
       rendered="#{!delivery.settings.unlimitedAttempts}"/>
    <h:outputText value="#{deliveryMessages.unlimited_}"
       rendered="#{delivery.settings.unlimitedAttempts}"/>


 <h:form id="noSubmissionLeft">
 <p class="act">
       <h:commandButton accesskey="#{deliveryMessages.a_return}" value="#{deliveryMessages.button_return}" type="submit"
         styleClass="active" action="select" rendered="#{delivery.actionString!='takeAssessmentViaUrl'">
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>

	   <h:commandButton value="#{deliveryMessages.button_continue}" type="button" 
	     rendered="#{delivery.actionString=='takeAssessmentViaUrl'}" style="act" onclick="javascript:window.open('#{delivery.selectURL}','_top')" onkeypress="javascript:window.open('#{delivery.selectURL}','_top')" />
 </p>
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
