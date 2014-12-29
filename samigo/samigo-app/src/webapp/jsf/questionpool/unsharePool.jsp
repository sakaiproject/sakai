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
      <title><h:outputText value="#{questionPoolMessages.us_p}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
 <h:form id="removePoolForm">
      <h3 style="insColor insBak"> <h:outputText  value="#{questionPoolMessages.us_p_confirm}" /> </h3>
   <h:panelGrid cellpadding="5" cellspacing="3">
     <h:panelGroup>
      <f:verbatim><div class="validation"></f:verbatim>
         <h:outputText value="#{questionPoolMessages.unshare_sure_p}" />
       <f:verbatim></div></f:verbatim>
     </h:panelGroup>
     <div class="tier1">
       <h3><h:outputText value="#{questionPoolMessages.p_name}"/></h3>
       <h:dataTable id ="table" value="#{questionpool.poolToUnshare}"
    var="pool" >
 	 <h:column>
		<h:outputText styleClass="bold" escape="false" value="#{pool.displayName}"/>
	 </h:column>
       </h:dataTable>
    </div>
 </h:panelGrid>
   <p class="act">
      <h:commandButton type="submit" immediate="true" id="Submit" value="#{commonMessages.unshare_action}"
    action="#{questionpool.unsharePool}" styleClass="active">
      </h:commandButton>
     <h:commandButton id="cancel" style="act" value="#{commonMessages.cancel_action}" action="poolList">
	    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
      </h:commandButton>


 </p>

 </h:form>
 <!-- end content -->
</div>

</body>
</html>
</f:view>
