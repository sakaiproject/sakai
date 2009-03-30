<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
      <%= request.getAttribute("sakai.html.head") %>
      <title><%= org.sakaiproject.search.tool.Messages.getString("jsp_sakai_search_title") %></title>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep, ignoreCourier); callAllLoaders();" 
    >
      <table border="0" cellpadding="0" cellspacing="0" width="100%" class="toolTitle" summary="layout">
	<tr>
	  <td class="title">
	    Search			
	  </td>
	  <td class="action" id="pageName">
	  </td>
	</tr>
      </table>
  </body>
</html>
