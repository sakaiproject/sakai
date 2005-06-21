<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<!-- $Id: importAssessment.jsp,v 1.21 2005/06/07 22:44:02 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorImportExport"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.import_a}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <h:form id="importAssessmentForm" enctype="multipart/form-data">
   <h:inputHidden value="#{xmlImport.importType}" />
   <h3><h:outputText  value="#{msg.import_a}" /></h3>
   <div class="validation">
     <font color="red"><h:messages /></font>
     <h:outputText value="#{msg.import_instructions}" />
   </div>
   <br />
   <h:panelGrid columns="2" rendered="false">
     <h:outputText value="#{msg.im_ex_version_choose}"/>
     <h:selectOneRadio layout="lineDirection" value="#{xmlImport.qtiVersion}">
       <f:selectItem itemLabel="#{msg.im_ex_version_12}"
         itemValue="1"/>
       <f:selectItem itemLabel="#{msg.im_ex_version_20}"
         itemValue="2"/>
     </h:selectOneRadio>
   </h:panelGrid>
   <%-- target represents location where import will be temporarily stored
        check valueChangeListener for final destination --%>
   <corejsf:upload target="/jsf/upload_tmp/qti_imports"
     valueChangeListener="#{xmlImport.importFromQti}" />

   <h:commandButton value="#{msg.import_action}" type="submit"
       style="act" />
 </h:form>

 <h:form id="doneForm">
   <h:commandButton value="#{msg.import_cancel_action}" type="submit"
      style="act" action="author" immediate="true"/>
 </h:form>
 <!-- end content -->
      </body>
    </html>
  </f:view>
