<html>
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
      <title><h:outputText value="#{deliveryMessages.save_for_later_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<h3 style="insColor insBak">
   <h:outputText value="#{deliveryMessages.save_for_later_title}" />
</h3>
<br/>
  <!-- content... -->
    <h:messages globalOnly="true" styleClass="validation" />
  <div class="validation">
       <h:outputText value="#{deliveryMessages.warning_2}" />
	   <br/>
       <h:outputText value="#{deliveryMessages.save_for_later_warning}"/>
   </div>

<h:form id="isRetracted">
 <p class="act">
  <h:commandButton id="beginAssessment1" value="#{deliveryMessages.button_return_to_assignment}" 
    action="#{delivery.validate}" type="submit" styleClass="active">
	<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandButton>

       <h:commandButton value="#{deliveryMessages.button_continue}" type="submit"
         styleClass="active" action="select" >
       </h:commandButton>
 </p> 
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
