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
<title><h:outputText value="#{evaluationMessages.sent}" /></title>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<div style="margin-left: 10px; margin-right: 10px">
<h3>
<h:outputText value="#{evaluationMessages.sent}" />
</h3>

<p>
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
</p>
<h:panelGrid columns="1" columnClasses="navView,navView" border="0">	
<h:commandButton id="close"onclick="window.close();" onkeypress="window.close();"  accesskey="#{evaluationMessages.a_close}" value="#{evaluationMessages.close}"/>
</h:panelGrid>
</div>	

</body>
</html>
</f:view>