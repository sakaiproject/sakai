<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: submitted.jsp,v 1.23 2005/06/10 14:09:55 daisyf.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
     var="msg"/>
     <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.submission}" /></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->
<h3><h:outputText value="#{msg.submission}" /></h3>
<div class="indnt1">
<h4>
  <h:outputText value="#{delivery.assessmentTitle} " />
  <h:outputText value="#{msg.submission_info}" />
</h4>

<h:form id="submittedForm">
<font color="red"><h:messages /></font>

    <h:outputText value="#{msg.submission_confirmation_message_1}" />
    <h:outputText escape="false" value="#{delivery.submissionMessage}" />
 
  <f:verbatim><p/></f:verbatim>
  <h:panelGrid columns="2" columnClasses="longtext">

    <h:outputLabel for="course_name" value="#{msg.course_name}"/>
    <h:outputText value="#{delivery.courseName}" />

    <h:outputLabel for="creator" value="#{msg.creator}" />
    <h:outputText value="#{delivery.creatorName}"/>

    <h:outputLabel for="asessment_title" value="#{msg.assessment_title}" />
    <h:outputText value="#{delivery.assessmentTitle}" />

    <h:outputLabel for="sub_remain" value="#{msg.number_of_sub_remain}" />
    <h:panelGroup>
      <h:outputText value="#{delivery.submissionsRemaining} out of #{delivery.settings.maxAttempts}"
          rendered="#{!delivery.settings.unlimitedAttempts}"/>
      <h:outputText value="#{msg.unlimited_}"
          rendered="#{delivery.settings.unlimitedAttempts}"/>
    </h:panelGroup>

    <h:outputLabel for="conf_num" value="#{msg.conf_num}" />
    <h:outputText value="#{delivery.confirmation}" />

    <h:outputLabel for="sub_date" value="#{msg.submission_dttm}" />
    <h:outputText value="#{delivery.submissionDate}">
        <f:convertDateTime pattern="#{genMsg.output_date_picker}" />
     </h:outputText>

    <h:outputLabel for="final_page" value="#{msg.final_page}" rendered="#{delivery.url!=null && delivery.url!=''}"/>
    <h:outputLink value="#" rendered="#{delivery.url!=null && delivery.url!=''}"
       onclick="window.open('#{delivery.url}','new_window');">
        <h:outputText value="#{delivery.url}" />
    </h:outputLink>
    
  </h:panelGrid>
</div>

<br /><br />
<div class="tier1">
  <h:panelGrid columns="2" cellpadding="3" cellspacing="3">
    <h:commandButton type="submit" value="#{msg.button_return}" action="select" 
       rendered="#{!delivery.accessViaUrl}" />
    <h:commandButton value="#{msg.button_ok}" type="button" rendered="#{delivery.accessViaUrl}"
       style="act" onclick="javascript:window.open('login.faces','_top')" />
  </h:panelGrid>
</div>

</h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>

