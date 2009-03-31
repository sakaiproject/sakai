<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorImportExport.export_a} #{authorImportExport.dash} #{assessmentBean.title}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<script language="javascript" style="text/JavaScript">
function getSelectedType(qtiUrl, cpUrl){
 var tables= document.getElementsByTagName("TABLE");
  for (var i = 0; i < tables.length; i++) {
    if ( tables[i].id.indexOf("exportType") >=0){
	  if (tables[i].getElementsByTagName("INPUT")[0].checked) {
		//alert("qti");
		window.open( qtiUrl, '_qti_export', 'toolbar=no,menubar=yes,personalbar=no,width=600,height=190,scrollbars=no,resizable=no');
	  }
	  else {
	    //alert("cp.....");
		window.location = cpUrl;
	  }
	}
  }
}
</script>


 <div class="portletBody">
<!-- content... -->
 <h:form id="exportAssessmentForm">          
    <h:inputHidden id="assessmentBaseId" value="#{assessmentBean.assessmentId}" />
   <h3><h:outputText  value="#{authorImportExport.export_a} #{authorImportExport.dash} #{assessmentBean.title}" escape="false"/></h3>
    <div class="tier1">
     <div class="form_label">
      <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
        <h:outputText value="#{authorImportExport.choose_type_1}" escape="true" />
		<h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/question/')" onkeypress="window.open('http://www.imsglobal.org/question/')">
		  <h:outputText value="#{authorImportExport.ims_qti}"/>
		</h:outputLink>
		<h:outputText value="#{authorImportExport.choose_type_2}" escape="true" />
		<h:outputLink value="#" onclick="window.open('http://www.imsglobal.org/content/packaging/')" onkeypress="window.open('http://www.imsglobal.org/content/packaging/')">
		  <h:outputText value="#{authorImportExport.ims_cp}"/>
		</h:outputLink>
		<h:outputText value="#{authorImportExport.choose_type_3}" escape="true" />
    </div>
    <br />
    <h:panelGrid columns="2">
     <h:outputText value="#{authorImportExport.choose_export_type}"/>
     <h:selectOneRadio id="exportType" layout="lineDirection" value="1">
       <f:selectItem itemLabel="#{authorImportExport.qti12}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{authorImportExport.content_packaging}"
         itemValue="2"/>
     </h:selectOneRadio>
    </h:panelGrid>

    <br/>
    <br/>
     <%-- activates the valueChangeListener --%>
     <h:commandButton value="#{authorImportExport.export}" type="submit"
       style="act" onclick="getSelectedType( '/samigo/jsf/qti/exportAssessment.xml?exportAssessmentId=#{assessmentBean.assessmentId}','/samigo/servlet/DownloadCP?&assessmentId=#{assessmentBean.assessmentId}'); return false;" />
     <%-- immediate=true bypasses the valueChangeListener --%>
     <h:commandButton value="#{authorImportExport.export_cancel_action}" type="submit"
       style="act" action="author" immediate="true"/>
  </div>
 </h:form>
</div>
 <!-- end content -->
      </body>
    </html>
  </f:view>
