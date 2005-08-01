<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
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
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
 <!-- content... -->
<h3><h:outputText value="#{msg.submission}" /></h3>
<div class="indnt1">
<h4>
  <h:outputText value="#{delivery.assessmentTitle} " />
  <h:outputText value="#{msg.submission_info}" />
</h4>

<h:form id="submittedForm">
<font color="red"><h:messages /></font>

    <h:outputText value="#{msg.submission_confirmation_message_1}" /> <br />
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
</div>

      </body>
    </html>
  </f:view>

