<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: exportItem.jsp,v 1.2 2005/06/10 16:49:27 esmiley.stanford.edu Exp $ -->
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
      <title><h:outputText value="#{msg.export_q}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
 <h:form id="exportItemForm">
  <h:outputText escape="false"
      value="<input type='hidden' name='itemId' value='#{param.exportItemId}'" />
  <h3 style="insColor insBak"><h:outputText value="#{msg.export_q}" /></h3>
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
      <f:param name="itemId" value="#{param.exportItemId}"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.ExportItemListener" />
    </h:commandButton>
   <h:commandButton value="#{msg.export_cancel_action}" type="reset"
     onclick="window.close()" style="act" action="author" />
  </p>
 </h:form>

 <!-- end content -->
      </body>
    </html>
  </f:view>

