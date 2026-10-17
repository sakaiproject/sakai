<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/xml;charset=utf-8" trimDirectiveWhitespaces="true" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text>&lt;?xml version="1.0" encoding="UTF-8" ?&gt;
</jsp:text>
  <jsp:directive.page trimDirectiveWhitespaces="true"/>
  <jsp:scriptlet> response.setHeader("Cache-Control", "private"); </jsp:scriptlet>
  <jsp:scriptlet> response.setHeader("Pragma", "cache"); </jsp:scriptlet>
  <jsp:scriptlet> response.setHeader("Content-Disposition", "attachment; filename=exportAssessment.xml"); </jsp:scriptlet>
  <!--
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
 -->
  <f:view>
    <h:outputText value="#{xml.xml}" escape="false"/>
  </f:view>
</jsp:root>
