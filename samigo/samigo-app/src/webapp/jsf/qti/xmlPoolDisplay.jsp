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
  <jsp:scriptlet> response.setHeader("Content-Disposition", "attachment; filename=exportPool.xml"); </jsp:scriptlet>
  <!--
/**
 * Copyright (c) 2023 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 -->
  <f:view>
    <h:outputText value="#{xml.xml}" escape="false"/>
  </f:view>
</jsp:root>
