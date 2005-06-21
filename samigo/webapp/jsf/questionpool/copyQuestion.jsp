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
      <title><h:outputText value="Copy Questions"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->

<div class="heading">Copy Question</div>


<h2>Question Text:</h2>
<br>
<logic:iterate id="qpool" collection='<%=session.getAttribute("selectedItems")%>'>
<bean:write name="qpool" property="itemText" />
<br>
</logic:iterate>

<h:panelGrid columns="2" >

<h:outputText styleClass="number" value="1"/>
<h:outputText value="#{msg.copy_q_to}"/>

<h:outputText value=""/>
<h:panelGroup>
<%@ include file="/jsf/questionpool/test.jsp" %>
</h:panelGroup>


<h:outputText styleClass="number" value="2"/>
<h:outputText value="#{msg.click_copy}"/>

</h:panelGrid>
  <center>
  <h:commandButton type="submit" id="Submit" value="#{msg.save}"
    action="editPool"/>
  <h:commandButton type="cancel" id="Cancel" value="#{msg.cancel}"
    action="cancelEditPool"/>

  </center>

</body>
</html>
</f:view>
