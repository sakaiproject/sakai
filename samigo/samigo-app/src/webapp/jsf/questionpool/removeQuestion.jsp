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
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
   
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.rm_q}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<div class="portletBody">
 <h:form id="removePoolForm">
   <h:panelGrid cellpadding="5" cellspacing="3">
     <h:panelGroup>
       <f:verbatim><h3 style="insColor insBak"></f:verbatim>
       <h:outputText  value="#{msg.rm_q_confirm}" />
       <f:verbatim></h3></f:verbatim>
     </h:panelGroup>
     <h:outputText styleClass="validation" value="#{msg.remove_sure_q}" />

<h:dataTable value="#{questionpool.itemsToDelete}" var="question">
      <h:column>
       <h:outputText escape="false" value="#{question.text}" />
      </h:column>
</h:dataTable>

     <h:panelGrid columns="2" cellpadding="3" cellspacing="3">
  <h:commandButton accesskey="#{msg.a_remove}" type="submit" immediate="true" id="Submit" value="#{msg.remove}"
    action="#{questionpool.removeQuestionsFromPool}" >
  </h:commandButton>

<h:commandButton id="cancel" accesskey="#{msg.a_cancel}" style="act" value="#{msg.cancel}" action="poolList"/>

     </h:panelGrid>
   </h:panelGrid>

 </h:form>
 <!-- end content -->


</div>

</body>
</html>
</f:view>
