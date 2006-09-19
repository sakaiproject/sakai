<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<jspiii:root xmlns:jspiii="http://java.sun.com/JSP/Page" version="2.0">
  <jspiii:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" />
<div id="sidebar_switcher">
  <div id="sidebar_switch_on">
    <jspiii:element name="a"><jspiii:attribute name="href">#</jspiii:attribute><jspiii:attribute name="onclick">showSidebar('<jspiii:expression>request.getAttribute("sakai.tool.placement.id")</jspiii:expression>')</jspiii:attribute>show the help sidebar</jspiii:element>
  </div>
  <div id="sidebar_switch_off">
    <jspiii:element name="a"><jspiii:attribute name="href">#</jspiii:attribute><jspiii:attribute name="onclick">hideSidebar('<jspiii:expression>request.getAttribute("sakai.tool.placement.id")</jspiii:expression>')</jspiii:attribute>hide the help sidebar</jspiii:element>
  </div>
</div>
</jspiii:root>
