<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request);
	
	String searchItemFormat = "<p class=\"searchItem\" ><a href=\"{1}\" target=\"searchresult\" >{2}</a><br />"
			+ "{3} <br/> "
			+ "<span class=\"searchScore\" >"
			+ " Score: {4} <a href=\"{1}\" >{1}</a> "
			+ "</span></p>";

	String pagerFormat = "<a href=\"{0}\" class=\"searchPage{2}\" >{1}</a>&#160;";

	String searchHeaderFormat = "<div class=\"searchHeader\">Found {0} to {1} of {2} documents ({3} seconds)</div>";

%>
<html>
  <head>
     <title>Search: <%= searchBean.getSearchTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
      
      <style type="text/css">
    .searchScore {
           font-size: smaller;
    }
    .searchItem {
		margin-top:1em; 
		margin-bottom:2em; 
		clear:both;
		display:block;
      }
     .searchHeader {
     	color:#000;
     	padding:.3em;
     	text-align:right;
     	font-size:.9em;
     	line-height:1.3em;
     	background:#DDDFE4;
     	border-top:1px solid #000;
     	border-bottom:1px solid #fff;
	color:#084A87;
      }
      .searchBox {
	text-align: center;
        padding-top: 4px;
        padding-bottom: 5px;
      }
      .searchPager {
        text-align:center;
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
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); " 
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
	<div class="navPanel">
		<div class="searchBox">
			<form action="?#" method="get" class="inlineForm"  >  
				<label for="search">
					Search:  
				</label>
				<input type="hidden" name="panel" value="Main" />
				<input type="text" id="search"  name="search" size="42" maxlength="1024" value="<%= searchBean.getSearch() %>"/>
				<input type="submit" name="sb" value="Go" />
			</form>
		</div>
	</div>	
	
	<%= searchBean.getHeader(searchHeaderFormat) %>

    <%= searchBean.getSearchResults(searchItemFormat) %>
    
    <table class="listHier" >
    <tr>
	<td>
    <%= searchBean.getPager(pagerFormat) %>
	</td>
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
</div>
</body>
</html>
