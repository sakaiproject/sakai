<html>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id: confirmAssessmentRetract.jsp 17095 2006-10-12 22:32:50Z ktsao@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
      <title><h:outputText value="#{authorMessages.edit_published_assessment_heading_conf}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
 <div class="portletBody">
  <h3><h:outputText value="#{authorMessages.edit_published_assessment_heading_conf}"/></h3>
 <h:form id="editPublishedAssessmentForm">
     <div class="validation tier1">
       <h:outputText value="#{authorMessages.warning}" />
   	   <br/>
       <h:outputText value="#{authorMessages.edit_published_assessment_heading_conf_info_1}" />
	   <br/>
       <h:outputText value="#{authorMessages.edit_published_assessment_heading_conf_info_2}" />
	   <br/>
	   <h:outputText value="#{authorMessages.edit_published_assessment_heading_conf_info_3}" />
	   <br/>
	   <h:outputText value="#{authorMessages.edit_published_assessment_heading_conf_info_4}" />
     </div>
       <p class="act">
       <h:commandButton id="edit" accesskey="#{assessmentSettingsMessages.a_edit}" value="#{authorMessages.button_edit}" type="submit"
         styleClass="active" action="#{author.getOutcome}" >
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
       </h:commandButton>

       <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit" style="act" action="author" >
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
       </h:commandButton>

       </p>
 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
