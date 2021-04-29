<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/xml;charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<% response.setHeader("Cache-Control", "private"); %>
<% response.setHeader("Pragma", "cache"); %>
<% response.setHeader("Content-Disposition", "attachment; filename=exported-assessment.xml"); %>

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
<f:view>
  <h:outputText value="#{xml.xml}" escape="false" />
</f:view>
