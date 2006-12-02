<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!--
$Id: createNewEmail.jsp 18063 2006-11-09 00:00:17Z ktsao@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
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
<f:loadBundle basename="org.sakaiproject.tool.assessment.bundle.EvaluationMessages" var="msg"/>
<f:loadBundle basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages" var="genMsg"/>
<html xmlns="http://www.w3.org/1999/xhtml">
<head><%= request.getAttribute("html.head") %>
<title><h:outputText value="#{msg.title_create_new_email}#{msg.colon} <Assessment Title> #{msg.feedback}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">

<div style="margin-left: 10px; margin-right: 10px">
<h:form id="mainForm" >
<script language="javascript" type="text/JavaScript">

</script>
<h3>
<h:outputText value="#{msg.title_create_new_email}" />
</h3>

<h:panelGrid columns="1" columnClasses="navView,navView" border="0">	
	<h:panelGrid columns="2" columnClasses="navView" border="0">	
	<h:outputText value="#{msg.from}" />
	<h:outputText value="#{email.fromName} <#{email.fromEmailAddress}>" />

	<h:outputText value="#{msg.to}" />    
	<h:outputText value="#{email.toName} <#{email.toEmailAddress}>" />  

	<h:outputText value="#{msg.subject}" />  
	<h:outputText id="subject" value="#{email.subject}" />    

	<h:outputText value="#{msg.cc_me}" />
	<h:selectOneRadio value="#{email.ccMe}">
		<f:selectItem itemLabel="#{msg.yes}" itemValue="yes"/>
		<f:selectItem itemLabel="#{msg.no}" itemValue="no"/>
	</h:selectOneRadio>

	<h:outputText value="#{msg.message}" />
	<h:outputText value="" />
	</h:panelGrid>

	<f:facet name="footer">
		<h:panelGrid columns="1" columnClasses="navView" border="0">	
			<h:panelGroup>
				<samigo:wysiwyg rows="140" value="#{email.message}">
					<f:validateLength minimum="1" maximum="4000"/>
				</samigo:wysiwyg>
			</h:panelGroup>

			<h:panelGrid columns="1" border="0">
		    <%@ include file="/jsf/evaluation/emailAttachment.jsp" %>
			</h:panelGrid>

			<h:panelGrid columns="2" border="0">
				<h:commandButton id="send" accesskey="#{msg.a_send}" styleClass="active" value="#{msg.send}" action="#{email.send}" type="submit" >
				          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
				</h:commandButton>
				<h:commandButton id="cancel"onclick="window.close();" onkeypress="window.close();"  accesskey="#{msg.a_cancel}" value="#{msg.cancel}" action="#{email.cancel}" immediate="true">
				</h:commandButton>
			</h:panelGrid>
		</h:panelGrid>
	</f:facet>
</h:panelGrid>

</h:form>
</div>	

</body>
</html>
</f:view>