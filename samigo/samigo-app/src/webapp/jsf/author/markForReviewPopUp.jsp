<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id: fullShortAnswer.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
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
	<html xmlns="http://www.w3.org/1999/xhtml">
		<head><%= request.getAttribute("html.head") %>
			<title><h:outputText value="#{assessmentSettingsMessages.mark_for_review}" /></title>
		</head>
		<body onload="<%= request.getAttribute("html.body.onload") %>">
		    <h:panelGrid columns="1" border="0">
				<h:outputText value="#{assessmentSettingsMessages.mark_for_review_pop_up_text_1}"/>
				<h:outputText value="&nbsp;" escape="false"/>
				<h:panelGroup>
				<f:verbatim><b></f:verbatim>
				<h:outputText value="#{assessmentSettingsMessages.mark_for_review_pop_up_text_2}"/>
				<f:verbatim></b></f:verbatim>	
				</h:panelGroup>
				<h:outputText value="&nbsp;" escape="false"/>
				<h:commandButton id="close" onclick="window.close();" onkeypress="window.close();" value="#{assessmentSettingsMessages.button_close}"/>
		    </h:panelGrid>
		</body>
	</html>
</f:view>
