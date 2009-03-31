<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  >
  <jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <jsp:scriptlet>
  	String hss = (String)request.getAttribute("headerScriptSource");
  	if ( hss != null &amp;&amp; hss.trim().length() &gt; 0 ) {
  </jsp:scriptlet>
  <script  type="text/javascript" src="${requestScope.headerScriptSource}"> <!-- don't reduce this! --></script>
  <jsp:scriptlet>
  	}
  </jsp:scriptlet>
<script type="text/javascript" >
var placementid = "Main<jsp:expression>request.getAttribute("sakai.tool.placement.id").toString().replace('-','x')</jsp:expression>";
</script>  
</jsp:root>
