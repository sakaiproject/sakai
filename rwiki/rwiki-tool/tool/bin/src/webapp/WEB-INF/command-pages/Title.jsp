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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  >
  <jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="${rlb.jsp_lang}" xml:lang="${rlb.jsp_xml_lang}" >

    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
      <title><c:out value="${rlb.jsp_rwiki_tool}"/></title>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">
	<jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep, ignoreCourier); callAllLoaders();
      </jsp:attribute>
      <table border="0" cellpadding="0" cellspacing="0" width="100%" class="toolTitle">
	<tr>
	  <td class="title">
	    <c:out value="${rlb.jsp_rwiki_tool}"/>	
	  </td>
	  <td class="action" id="pageName">
	  </td>
	</tr>
      </table>
    </jsp:element>
  </html>
</jsp:root>
