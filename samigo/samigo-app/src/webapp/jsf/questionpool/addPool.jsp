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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.add_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">


 <h:form id="questionpool">
<h3 class="insColor insBak insBor">
<h:outputText value="#{questionPoolMessages.add_p}"/>
</h3>
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
<h:outputText value="#{questionPoolMessages.add_p_required}"/>
 <div class="tier1">
 <h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel for="namefield" value="#{questionPoolMessages.p_name}#{questionPoolMessages.star}"/>
  
  <h:inputText id="namefield" size="30" value="#{questionpool.currentPool.displayName}"/>

  <h:outputLabel for="ownerfield" value="#{questionPoolMessages.creator}"/>
  <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>
 
  <h:outputLabel for="orgfield" value="#{questionPoolMessages.dept} "/>
  <h:inputText id="orgfield" size="30" value="#{questionpool.currentPool.organizationName}"/>

  <h:outputLabel for="descfield" value="#{questionPoolMessages.desc}"/>
  <h:inputTextarea id="descfield" value="#{questionpool.currentPool.description}" cols="30" rows="5"/>

  <h:outputLabel for="objfield" value="#{questionPoolMessages.obj} "/>
  <h:inputText id="objfield" size="30" value="#{questionpool.currentPool.objectives}"/>
 
  <h:outputLabel for="keyfield" value="#{questionPoolMessages.keywords} "/>
  <h:inputText id="keyfield" size="30" value="#{questionpool.currentPool.keywords}"/>
</h:panelGrid>
 </p>


<p class="act">
  <h:commandButton accesskey="#{questionPoolMessages.a_save}" id="submit"  action="#{questionpool.doit}"
	value="#{questionPoolMessages.save}" styleClass="active">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" style="act" value="#{questionPoolMessages.cancel}" action="poolList" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
  </h:commandButton>

</p>
 </h:form>
</div>
<!-- end content -->
      </body>
    </html>
  </f:view>



