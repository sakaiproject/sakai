<html>
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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{templateMessages.remove_heading}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
 <div class="portletBody">
  <h3><h:outputText value="#{templateMessages.remove_conf}"/></h3>
 <h:form id="removeTemplateForm">
   <h:inputHidden id="templateId" value="#{template.idString}"/>

     <div class="validation tier1">
       <h:outputText value="#{templateMessages.remove_fer_sure}" />
       <h:outputText value=" \"" />
       <h:outputText value="#{template.templateName}"/>
       <h:outputText value=" \"?" />

       </div>
       <p class="act">
       <h:commandButton id="remove" accesskey="#{templateMessages.a_remove}" value="#{templateMessages.index_button_remove}" type="submit"
         styleClass="active" action="template" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.author.DeleteTemplateListener" />
       </h:commandButton>
       <h:commandButton value="#{templateMessages.cancel}" type="submit"
         style="act" action="template" />
       </p>

 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
