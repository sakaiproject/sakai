<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id: ipAccessError.jsp 59354 2009-03-31 17:11:55Z arwhyte@umich.edu $
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
      <title><h:outputText value="#{deliveryMessages.access_denied}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
  <!-- content... -->
  <h3><h:outputText value="#{deliveryMessages.access_denied}"/></h3>
 <h:form id="removeTemplateForm">
   <h:panelGroup>
       <h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false" />
   </h:panelGroup>

   <f:verbatim><p class="act"></f:verbatim>
       <h:commandButton accesskey="#{deliveryMessages.a_return}" value="#{deliveryMessages.button_return}" type="submit"
          style="act" action="select" 
          rendered="#{delivery.actionString=='takeAssessment' || delivery.actionString=='reviewAssessment'}">
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
       </h:commandButton>
   <f:verbatim></p></f:verbatim>
 
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
