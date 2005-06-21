<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="Remove Pool"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <h:form id="removePoolForm">
      <h3 style="insColor insBak"> <h:outputText  value="#{msg.rm_p_confirm}" /> </h3>
   <h:panelGrid cellpadding="5" cellspacing="3">
     <h:panelGroup>
      <f:verbatim><div class="validation"></f:verbatim>
         <h:outputText value="#{msg.remove_sure_p}" />
       <f:verbatim></div></f:verbatim>
     </h:panelGroup>
     <div class="indnt1">
       <h3><h:outputText value="#{msg.p_names}"/></h3>
       <h:dataTable id ="table" value="#{questionpool.poolsToDelete}"
    var="pool" >
 	 <h:column>
		<h:outputText styleClass="bold" escape="false" value="#{pool.displayName}"/>
	 </h:column>
       </h:dataTable>
    </div>
 </h:panelGrid>
   <p class="act">
      <h:commandButton type="submit" immediate="true" id="Submit" value="#{msg.remove}"
    action="#{questionpool.removePool}" styleClass="active">
      </h:commandButton>
      <h:commandButton style="act" value="#{msg.cancel}" action="poolList"/>

 </p>

 </h:form>
 <!-- end content -->


</body>
</html>
</f:view>
