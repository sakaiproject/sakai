<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
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
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request);
	
	String searchItemFormat = "<p class=\"searchItem\" ><a href=\"{1}\" >{2}</a><br />"
			+ "{3} <br/> "
			+ "<span style=\"font-size: smaller;\" > "
			+ " Score: {4} <a href=\"{1}\" >{1}</a> "
			+ "</span></p>";

	String pagerFormat = "<a href=\"{0}\" class=\"searchPage{2}\" >{1}</a>&#160;";

	String searchHeaderFormat = "<p class=\"searchHeader\">Found {0} to {1} of {2} documents ({3} seconds)</p>";

%>
<html>
  <head>
     <title>Search: <%= searchBean.getSearchTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
      
      <style>
      .searchForm{
		margin-top:5em; 
      }
      .searchItem {
		margin-top:1em; 
		margin-bottom:2em; 
		clear:both;display:block
      }
      .searchHeader {
      	color:#000;
      	padding:.3em;
      	margin:-.3em -2.2em;
      	text-align:right;
      	font-size:.9em;
      	line-height:1.3em;
      	background:#DDDFE4;
      	border-bottom:1px solid #fff;color:#084A87;
      }
      .searchBox {
      }
      .searchPager {
        text-aligh:center;
      }
      .searchPage0 {
      	background: url("/images/pager0.gif") no-repeat; 
		float: left; 
		height: 14px; 
		width: 26px; 
		padding-right: 3px
      }
      .searchPage1 {
      	background: url("/images/pager1.gif") no-repeat; 
		float: left; 
		height: 14px; 
		width: 26px; 
		padding-right: 3px
      }
      .searchPage2 {
      	background: url("/images/pager2.gif") no-repeat; 
		float: left; 
		height: 14px; 
		width: 26px; 
		padding-right: 3px
      }
   
      </style>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); " 
    >
<%@include file="header.jsp"%>
    	<div class="portletBody">    		
    	
    <div class="navIntraTool">
	  <span class="rwiki_pageLinks">
	  <% if ( searchBean.hasAdmin() ) { %>
	    <a href="<%= searchBean.getToolUrl() %>/admin/index">Admin</a>
	  <% } %>
	  </span>
    </div>
    <%
    if ( searchBean.isEnabled() ) 
    {
    %>
    
	<form action="?#" method="get" class="searchForm"  >    
	    <input type="hidden" name="panel" value="Main" />
	  <span class="searchBox">
	    <input type="text" name="search" value="<%= searchBean.getSearch() %>"/>
	  </span>
	</form>
	
	<%= searchBean.getHeader(searchHeaderFormat) %>

    <%= searchBean.getSearchResults(searchItemFormat) %>
    
    <table class="searchPager" >
    <tr>
    <%= searchBean.getPager(pagerFormat) %>
    </tr>
    </table>
    <%
    }
    else
    {
    %>
    <p>
    The search tool is not enabled, please ask your administrator to set search.experimental = true in sakai.properties
    </p>
    <%
    }
    %>

<%@include file="footer.jsp"%>
   </body>
</html>
