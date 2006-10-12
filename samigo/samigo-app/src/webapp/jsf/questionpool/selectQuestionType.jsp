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
     var="qpmsg"/>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<div class="portletBody">
<!-- content... -->
<!-- FORM -->
<h:form>

<!-- CHANGE TYPE -->
 
<h:panelGrid columns="2">

  <h:outputText styleClass="number" value="1" />
<h:panelGroup>
<h:outputText value="#{qpmsg.sel_q_type} "/>
<h:selectOneMenu id="selType" value="#{itemauthor.itemType}" required="true">
  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
</h:selectOneMenu>
<h:message for="selType" styleClass="validate"/>
</h:panelGroup>
  <h:outputText styleClass="number" value="2" />
<h:outputText value="#{qpmsg.click_save}"/>
</h:panelGrid>

<p class="act">
<h:commandButton accesskey="#{msg.a_save}" type="submit"  action="#{itemauthor.doit}" value="#{msg.button_save}" styleClass="active">
   <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
   <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandButton>

  <h:commandButton accesskey="#{msg.a_cancel}" type="button" id="Cancel" value="#{msg.button_cancel}" immediate="true"
    onclick="document.location='editPool.faces'" onkeypress="document.location='editPool.faces'"/>
</p>

</h:form>
</div>

<!-- end content -->
    </body>
  </html>
</f:view>
