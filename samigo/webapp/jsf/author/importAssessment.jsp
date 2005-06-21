
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- NOTE: THIS IS AN UNHOLY MESS, RIGHT NOW I AM INCLUDING STRUTS TO GET UPLOAD
     TODO: write file upload control.
-->
<%@ page import="org.apache.struts.action.*,
                 java.util.Iterator,
                 org.navigoproject.ui.web.asi.importing.UploadForm "%>

<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-template.tld" prefix="template" %>
<!-- $Id: importAssessment.jsp,v 1.7 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.import_an_assessment} " /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->

<!-- SCRIPTLET... ...UGH... Find out if the maximum length has been exceeded. -->
<logic:present name="<%= Action.ERROR_KEY %>" scope="request">
    <%
        ActionErrors errors = (ActionErrors) request.getAttribute(Action.ERROR_KEY);
        //note that this error is created in the validate() method of UploadForm
        Iterator iterator = errors.get(UploadForm.ERROR_PROPERTY_MAX_LENGTH_EXCEEDED);
        //there's only one possible error in this
        ActionError error = (ActionError) iterator.next();
        pageContext.setAttribute("maxlength.error", error, PageContext.REQUEST_SCOPE);
    %>
</logic:present>
<!-- If there was an error, print it out -->
<logic:present name="maxlength.error" scope="request">
    <font color="red"><bean:message name="maxlength.error" property="key" /></font>
</logic:present>
<logic:notPresent name="maxlength.error" scope="request">
<!--    Note that the maximum allowed size of an uploaded file for this application is two megabytes.-->
<!--See the /WEB-INF/struts-config.xml file for this application to change it. -->
</logic:notPresent>

<%-- HEADINGS IN JSF --%>
<h:panelGrid cellpadding="6" cellspacing="4">
 <h:panelGroup>
   <f:verbatim><h3 style="insColor insBak"></f:verbatim>
   <h:outputText value="#{msg.import_an_assessment}" />
   <f:verbatim></h3></f:verbatim>
 </h:panelGroup>
  <h:outputText value="#{msg.import_from_a_file}" />
</h:panelGrid>
<%-- STRUTS UGH: STRUTS FORM FOR UPLOAD  :O  --%>
<table cellpadding="6" cellspacing="4"><tr><td>
<html:form action="/asi/select/upload.do?queryParam=Successful" enctype="multipart/form-data">
  <html:file property="theFile" />
  <html:submit value="Import Assessment" /><%-- we'll get this from resource later --%>
</html:form>
</table></tr></td>
<%-- 	PURE JSF FOR CANCEL: THESE TWO h:panelGrids WILL BE MERGED LATER IN ONE h:form --%>
<h:panelGrid cellpadding="6" cellspacing="4">
<h:form>
  <h:commandButton action="author" type="submit" value="#{msg.button_cancel}" />
</h:form>
</h:panelGrid>
<!-- end content -->
      </body>
    </html>
  </f:view>
