<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id: requireRemoveSubmissions.jsp 2011-06-23 13:58:35Z yq12@txstate.edu $
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
      <title><h:outputText value="#{authorMessages.remove_assessment_co}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
        <div class="portletBody">
         <!-- content... -->
         <h:form id="requireRemoveSubmissionsForm">
           <h:inputHidden id="publishedId" value="#{publishedassessment.publishedID}"/>
           <h3><h:outputText  value="#{authorMessages.remove_assessment_check}" /></h3>
           <div class="validation tier1">
               <h:outputFormat value="#{authorMessages.cert_rem_submissions}" escape="false">
                  <f:param value="#{publishedassessment.title}"></f:param>
               </h:outputFormat>
           </div>
           <p class="act">
               <h:commandButton accesskey="#{authorMessages.a_remove}" value="#{authorMessages.button_remove_submissions}" type="submit" styleClass="active" action="totalScores" >
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
               </h:commandButton>
               <h:commandButton accesskey="#{authorMessages.a_cancel}" value="#{authorMessages.button_cancel}" type="submit" action="author" />
            </p>
         </h:form>
         <!-- end content -->
        </div>
      </body>
    </html>
  </f:view>
