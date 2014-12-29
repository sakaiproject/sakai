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
      <%-- designed to be in  popup window --%>
      <head>
      <%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorImportExport.export_q}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <div class="tier1">
 <h:form id="exportItemForm">
  <h:outputText escape="false"
      value="<input type='hidden' name='itemId' value='#{param.exportItemId}'" />
  <h3 style="insColor insBak"><h:outputText value="#{authorImportExport.export_q}" /></h3>
  <div class="validation">
        <h:outputText value="#{authorImportExport.export_instructions}" escape="false" />
  </div>
   <h:panelGrid columns="2" rendered="false">
     <h:outputText value="#{authorImportExport.im_ex_version_choose}"/>
     <h:selectOneRadio layout="lineDirection">
       <f:selectItem itemLabel="#{authorImportExport.im_ex_version_12}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{authorImportExport.im_ex_version_20}"
         itemValue="2"/>
     </h:selectOneRadio>
   </h:panelGrid>
  <p class="act">
    <h:commandButton value="#{authorImportExport.export_action}" type="submit" action="xmlDisplay"
        immediate="true" >
      <f:param name="itemId" value="#{param.exportItemId}"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.ExportItemListener" />
    </h:commandButton>
   <h:commandButton value="#{commonMessages.cancel_action}" type="reset"
     onclick="window.close()" style="act" action="author" />
  </p>
 </h:form>
 </div>
 <!-- end content -->
      </body>
    </html>
  </f:view>

