<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request);
	
	String searchItemFormat = "<p class=\"searchItem\" ><a href=\"{1}\" target=\"searchresult\" >{2}</a><br />"
			+ "{3} <br/> "
			+ "<span class=\"searchScore\" > Hit: {0} "
			+ " Score: {4} <a href=\"{1}\" target=\"searchresult\" >{1}</a> "
			+ "</span></p>";

	String pagerFormat = "<td class=\"searchPage{2}\" ><a href=\"{0}\" class=\"searchPage{2}\" ><img src=\"/sakai-search-tool/images/pager{2}.gif\" border=\"0\" alt=\"Page {1}\" /><br />{1}</a></td>";
	String singlePageFormat = "<td><div class=\"singleSearchPage\" >&#160;</div></td>";
	String searchTerms = searchBean.getSearch();
	String rssURL = "";
	String rssLink = "";
	if ( searchTerms != null && searchTerms.length() > 0 ) {
		rssURL = searchBean.getToolUrl()+"/rss20?search="+java.net.URLEncoder.encode(searchBean.getSearch());
		rssLink = "<link rel=\"alternate\" title=\"Sakai RSS Search for: "+searchTerms+" \" " 
 			+ " href=\""+rssURL+"\" type=\"application/rss+xml\" /> ";
    
	}
	String searchHeaderFormat = "<div class=\"searchHeader\">Found {0} to {1} of {2} documents ({3} seconds) <a href=\""+rssURL+"\" target=\"rss\" ><img src=\"/sakai-search-tool/images/rss.gif\" alt=\"RSS\" border=\"0\" /></a></div>";

%>
<html>
  <head>
     <title>Search: <%= searchBean.getSearchTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
      <%= rssLink %>  
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

    <table cellspacing="0" cellpadding="0" >
    <tr valign="top">
    <%= searchBean.getPager(pagerFormat, singlePageFormat) %>
    </tr>
    </table>

    <%= searchBean.getSearchResults(searchItemFormat) %>
    
    <table cellspacing="0" cellpadding="0" >
    <tr valign="top" >
    <%= searchBean.getPager(pagerFormat, singlePageFormat) %>
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
