<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- 
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorImportExport.import_qp}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">
<!-- content... -->
 <h:form id="importPoolForm" enctype="multipart/form-data">

   <h:inputHidden value="#{xmlImport.importType}" />
   <h3><h:outputText  value="#{authorImportExport.import_qp}" /></h3>
    <div class="tier1">
     <div class="form_label">
      <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
      <%-- currently import pool mirrors import assessment --%>
      <h:outputText value="#{authorImportExport.import_pool_instructions}" escape="false"/>
    </div>
    <br />
   <div class="tier2">
   <h:outputLabel  styleClass="form_label" value="#{authorImportExport.choose_file}"/>
    <%-- target represents location where import will be temporarily stored
        check valueChangeListener for final destination --%>
    <corejsf:upload target="jsf/upload_tmp/qti_imports/#{person.id}"
      valueChangeListener="#{xmlImport.importPoolFromQti}"/>
   </div>
    <br/>
    <br/>
     <%-- activates the valueChangeListener --%>
     <h:commandButton id="questionPoolsLink" value="#{authorImportExport.import_action}" type="submit"
       style="act" action="poolList" >
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
	 </h:commandButton>
     <%-- immediate=true bypasses the valueChangeListener --%>
     <h:commandButton value="#{commonMessages.cancel_action}" type="submit"
       style="act" action="poolList" immediate="true"/>
  </div>

 </h:form>
</div>

 <!-- end content -->
      </body>
    </html>
  </f:view>
