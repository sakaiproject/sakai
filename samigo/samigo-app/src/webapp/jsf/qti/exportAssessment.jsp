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
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
   
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorImportExport"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <%-- designed to be in  popup window --%>
      <samigo:stylesheet path="/css/tool.css"/>
      <samigo:stylesheet path="/css/tool_base.css"/>
      <title><h:outputText value="#{msg.export_a}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <div class="tier1">
 <h:form id="exportAssessmentForm">
  <h:outputText escape="false"
      value="<input type='hidden' name='assessmentId' value='#{param.exportAssessmentId}'" />
  <h3 style="insColor insBak"><h:outputText  value="#{msg.export_a}" /></h3>
  <div class="validation">
        <h:outputText value="#{msg.export_instructions}" escape="false" />
  </div>
   <h:panelGrid columns="2" rendered="false">
     <h:outputText value="#{msg.im_ex_version_choose}"/>
     <h:selectOneRadio layout="lineDirection">
       <f:selectItem itemLabel="#{msg.im_ex_version_12}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{msg.im_ex_version_20}"
         itemValue="2"/>
     </h:selectOneRadio>
   </h:panelGrid>
  <p class="act">
    <h:commandButton value="#{msg.export_action}" type="submit" action="xmlDisplay"
        immediate="true" >
        <f:param name="assessmentId" value="lastModifiedDate"/>

      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ExportAssessmentListener" />
    </h:commandButton>
   <h:commandButton value="#{msg.export_cancel_action}" type="reset"
     onclick="window.close()" style="act" action="author" />
  </p>
 </h:form>
 </div>
 <!-- end content -->
      </body>
    </html>
  </f:view>

