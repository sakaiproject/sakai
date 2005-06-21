<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: confirmTempRemove.jsp,v 1.14 2005/05/24 16:54:49 janderse.umich.edu Exp $ -->
  <f:view>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.remove_heading}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
  <h3><h:outputText value="#{msg.remove_conf}"/></h3>
 <h:form id="removeTemplateForm">
   <h:inputHidden id="templateId" value="#{template.idString}"/>

     <div class="validation indnt1">
       <h:outputText value="#{msg.remove_fer_sure}" />
       <h:outputText value=" &quot;" />
       <h:outputText value="#{template.templateName}"/>
       <h:outputText value="&quot;?" />

       </div>
       <p class="act">
       <h:commandButton value="#{msg.index_button_remove}" type="submit"
         styleClass="active" action="template" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.author.DeleteTemplateListener" />
       </h:commandButton>
       <h:commandButton value="#{msg.cancel}" type="submit"
         style="act" action="template" />
       </p>

 </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>
</html>
