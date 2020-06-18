<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai"%>
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
<title>
<h:outputText value="#{evaluationMessages.title_create_new_email}#{evaluationMessages.colon} #{param.assessmentName} #{commonMessages.feedback}" rendered="#{param.fromEmailLinkClick == 'true'}"/>  
<h:outputText value="#{evaluationMessages.title_create_new_email}#{evaluationMessages.colon} #{email.subject}" rendered="#{param.fromEmailLinkClick != 'true'}"/>
</title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">

<div style="margin-left: 10px; margin-right: 10px">
<h:form id="mainForm" >
<script>

</script>
<h3>
<h:outputText value="#{evaluationMessages.title_create_new_email}" />
</h3>

<h5>
<h:outputText value="#{evaluationMessages.email_warning}" />
</h5>

<h:panelGrid columns="1" columnClasses="navView,navView" border="0">	
	<h:panelGrid columns="2" columnClasses="navView" border="0">	
	<h:outputText value="#{evaluationMessages.from}" />
	<h:outputText value="#{param.fromName} <#{param.fromEmailAddress}>" rendered="#{param.fromEmailLinkClick == 'true'}"/>  
	<h:outputText value="#{email.fromName} <#{email.fromEmailAddress}>" rendered="#{param.fromEmailLinkClick != 'true'}"/>

	<h:outputText value="#{evaluationMessages.to}" />  
	<h:outputText value="#{param.toName} <#{param.toEmailAddress}>" rendered="#{param.fromEmailLinkClick == 'true'}"/>  
	<h:outputText value="#{email.toName} <#{email.toEmailAddress}>" rendered="#{param.fromEmailLinkClick != 'true'}"/>

	<h:outputText value="#{evaluationMessages.subject}" />  
	<h:outputText id="subject1" value="#{param.assessmentName} #{commonMessages.feedback}" rendered="#{param.fromEmailLinkClick == 'true'}"/>  
	<h:outputText id="subject2" value="#{email.subject}" rendered="#{param.fromEmailLinkClick != 'true'}"/>

	<h:outputText value="#{evaluationMessages.cc_me}" />
	<h:selectOneRadio value="#{email.ccMe}">
		<f:selectItem itemLabel="#{evaluationMessages.yes}" itemValue="yes"/>
		<f:selectItem itemLabel="#{evaluationMessages.no}" itemValue="no"/>
	</h:selectOneRadio>

	<h:outputText value="#{evaluationMessages.message}" />
	<h:outputText value="" />
	</h:panelGrid>

	<f:facet name="footer">
		<h:panelGrid columns="1" columnClasses="navView" border="0">	
			<h:panelGroup>
				<samigo:wysiwyg rows="140" value="#{email.message}" reset="#{param.fromEmailLinkClick}">
					<f:validateLength maximum="4000"/>
				</samigo:wysiwyg>
			</h:panelGroup>

			<h:panelGrid columns="1" border="0">
		    <%@ include file="/jsf/evaluation/emailAttachment.jsp" %>
			</h:panelGrid>

			<h:panelGrid columns="2" border="0">
				<h:commandButton id="send" styleClass="active" value="#{evaluationMessages.send}" action="#{email.send}" type="submit" />
				<h:commandButton id="cancel" onclick="window.close();" onkeypress="window.close();"  value="#{commonMessages.cancel_action}" action="#{email.cancel}"/>
			</h:panelGrid>
		</h:panelGrid>
	</f:facet>
</h:panelGrid>

</h:form>
</div>	

</body>
</html>
</f:view>
