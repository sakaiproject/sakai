<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<!DOCTYPE html>
<!-- $Id: importAssessment.jsp 20403 2007-01-18 04:15:06Z ktsao@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2007 The Sakai Foundation.
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
      <title><h:outputText value="#{authorImportExport.export_a} #{authorImportExport.dash} #{assessmentBean.title}" /></title>
<script type="text/JavaScript">
function getSelectedType(qtiUrl, cpUrl){
  if ( $("#exportAssessmentForm\\:exportType\\:0").prop("checked") ) {
    window.open( qtiUrl, '_qti_export', 'toolbar=yes,menubar=yes,personalbar=no,width=600,height=500,scrollbars=yes,resizable=yes');
  }
  else {
    window.location = cpUrl;
  }
}
</script>
</head>

<body onload="<%= request.getAttribute("html.body.onload") %>">
<div class="portletBody container-fluid">
<!-- content... -->
<h:form id="exportAssessmentForm">
  <h:inputHidden id="assessmentBaseId" value="#{assessmentBean.assessmentId}" />
  <h1>
    <h:outputText value="#{authorImportExport.export_a}" escape="false" />
    <small>
      <h:outputText value="#{authorImportExport.dash} #{assessmentBean.title}" escape="false"/>
    </small>
  </h1>

  <div class="tier1">
    <div class="form_label">
      <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
      <p class="text-info">
        <h:outputText value="#{authorImportExport.choose_type_1}" escape="true" />
        <h:outputText value="&#160;" escape="false" />
        <h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/question/')" onkeypress="window.open('http://www.imsglobal.org/question/')">
          <h:outputText value="#{authorImportExport.ims_qti}"/>
        </h:outputLink>
        <h:outputText value="&#160;" escape="false" />
        <h:outputText value="#{authorImportExport.choose_type_2}" escape="true" />
        <h:outputText value="&#160;" escape="false" />
        <h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/content/packaging/')" onkeypress="window.open('http://www.imsglobal.org/content/packaging/')">
          <h:outputText value="#{authorImportExport.ims_cp}"/>
        </h:outputLink>
        <h:outputText value="&#160;" escape="false" />
        <h:outputText value="#{authorImportExport.choose_type_3}" escape="true" />
      </p>
      <p class="text-warning">
        <h:outputText value="#{authorImportExport.export_imagemap_message}" escape="false" />
      </p>
    </div>
    <br />
    <h:panelGroup layout="block">
     <h:outputText value="#{authorImportExport.choose_export_type}"/>
     <t:selectOneRadio id="exportType" layout="spread" value="1">
       <f:selectItem itemLabel="#{authorImportExport.qti12}" itemValue="1"/>
       <f:selectItem itemLabel="#{authorImportExport.content_packaging}" itemValue="2"/>
     </t:selectOneRadio>
     <ul class="export-type">
       <li><t:radio for="exportType" index="0" /></li>
       <li><t:radio for="exportType" index="1" /></li>
     </ul>
     <!-- For formatting -->
     <div class="text-warning">
       <h:outputText value="#{authorImportExport.cp_message}"/>
     </div>
    </h:panelGroup>

    <br/>
    <br/>
     <%-- activates the valueChangeListener --%>
     <h:commandButton value="#{authorImportExport.export}" type="submit"
       style="act" onclick="getSelectedType( '../qti/exportAssessment.faces?exportAssessmentId=#{assessmentBean.assessmentId}','/samigo-app/servlet/DownloadCP?&assessmentId=#{assessmentBean.assessmentId}'); return false;" />
     <%-- immediate=true bypasses the valueChangeListener --%>
     <h:commandButton value="#{commonMessages.cancel_action}" type="submit"
       style="act" action="author" immediate="true"/>

  </div>
 </h:form>
</div>
 <!-- end content -->
      </body>
    </html>
  </f:view>
