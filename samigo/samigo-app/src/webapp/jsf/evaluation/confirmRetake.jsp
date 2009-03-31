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
      <title><h:outputText value="#{evaluationMessages.retake_heading}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
  <!-- content... -->
 <div class="portletBody">
  <h3><h:outputText value="#{evaluationMessages.retake_assessment}"/></h3>
 <h:form id="retractAssessmentForm">
   <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />

     <div class="validation tier1">
       <h:outputText value="#{evaluationMessages.retake_confirmation_1} #{retakeAssessment.studentName} #{evaluationMessages.retake_confirmation_2}" />
     </div>
       <p class="act">
       <h:commandButton id="retake" accesskey="#{evaluationMessages.a_retake}" value="#{evaluationMessages.retake}" type="submit"
         styleClass="active" action="submissionStatus" >
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.RetakeAssessmentListener" />
          <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
       </h:commandButton>
       <h:commandButton value="#{evaluationMessages.cancel}" type="submit" style="act" action="author" />
       </p>

 </h:form>
  <!-- end content -->
</div>
      </body>
    </html>
  </f:view>
</html>
