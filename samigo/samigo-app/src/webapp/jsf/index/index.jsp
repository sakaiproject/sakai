<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
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

<%-- before going any further, we establish primitive standalone authz --%>
<%@ include file="../security/roleCheckStandaloneStaticInclude.jsp"%>

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{mainIndexMessages.tool_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

  <!-- content... -->
  <h:form id="indexPage">
      <h:commandLink type="submit"
        value="Student entry point: admin" action="#{backingbean.chooseAgentAdmin}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit"
        value="Student entry point: rachel" action="#{backingbean.chooseAgentRachel}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit"
        value="Student entry point: marith" action="#{backingbean.chooseAgentMarith}">
      </h:commandLink>
      <f:verbatim><br /><br /></f:verbatim>

      <h:commandLink type="submit"
        value="Instructor entry point" action="#{backingbean.chooseAgentAdminInstructor}">
      </h:commandLink>

<%-- remove this
      <h:commandLink type="submit"
         value="Instructor entry point" action="author">
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
      </h:commandLink>
--%>
  </h:form>
  <!-- end content -->
      </body>
    </html>
  </f:view>

