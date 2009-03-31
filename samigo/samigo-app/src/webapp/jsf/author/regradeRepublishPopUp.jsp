<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
$Id: regradeRepublishPopUp.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
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
			<title><h:outputText value="#{authorMessages.regrade_republish}" /></title>
		</head>
		<body onload="<%= request.getAttribute("html.body.onload") %>">
		    <h:panelGrid columns="1" border="0">
				<h:panelGroup>
					<h:outputText value="#{authorMessages.what_is_republish_1}"/>
					<f:verbatim><b></f:verbatim>
					<h:outputText value=" #{authorMessages.what_is_republish_2} "/>
					<f:verbatim></b></f:verbatim>
					<h:outputText value="#{authorMessages.what_is_republish_3}"/>
				</h:panelGroup>
				<f:verbatim><br /></f:verbatim>
				<h:panelGroup>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_1}"/>
					<f:verbatim><b></f:verbatim>
					<h:outputText value=" #{authorMessages.what_is_regrade_republish_2} "/>
					<f:verbatim></b></f:verbatim>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_3}"/>
				</h:panelGroup>

				<h:panelGrid columns="1" border="0">
					<h:outputText value="#{authorMessages.what_is_regrade_republish_4}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_5}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_6}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_7}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_8}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_9}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_10}"/>
					<h:outputText value="#{authorMessages.what_is_regrade_republish_11}"/>
					<h:outputText value=" " />
	                    		<h:outputText value="#{authorMessages.what_is_regrade_republish_12}"/>
					</h:panelGrid>
				<f:verbatim><br /></f:verbatim>
				<h:commandButton id="close" onclick="window.close();" onkeypress="window.close();" value="#{authorMessages.button_close}"/>
		    </h:panelGrid>
		</body>
	</html>
</f:view>
