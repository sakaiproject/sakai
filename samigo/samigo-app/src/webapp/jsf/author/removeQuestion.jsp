<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8"
	language="java"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo"%>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id$
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
<title><h:outputText value="#{authorMessages.remove_q_conf}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">

	<div class="portletBody">
		<!-- content... -->

		<h:form>
			<h3>
				<h:outputText value="#{authorMessages.remove_q_conf}" />
			</h3>
			<div class="validation tier1">
				<h:outputText value="#{authorMessages.sure_rem_q}" />
			</div>

			<p class="act">
				<h:commandButton immediate="true" value="#{commonMessages.remove_action}" action="editAssessment" type="submit" styleClass="active">
					<f:actionListener
						type="org.sakaiproject.tool.assessment.ui.listener.author.ItemRemoveListener" />
					<f:param name="itemid" value="#{question.itemData.itemIdString}" />
				</h:commandButton>
				<h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editAssessment" rendered="#{itemauthor.target=='assessment'}" />
				<h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editPool" rendered="#{itemauthor.target=='questionpool'}" />
			</p>

		</h:form>
		<!-- end content -->
	</div>
</body>
	</html>
</f:view>

