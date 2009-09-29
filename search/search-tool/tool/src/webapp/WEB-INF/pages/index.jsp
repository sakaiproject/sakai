<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request);
	
	String errorMessageFormat = "<div class=\"alertMessage\" >{0}</div>";
	String searchItemFormat = 
	          "<!--is--><div class=\"searchItem\" > " 
	        + "<span class=\"searchTool\">{0}:</span> "
	        + "<a href=\"{1}\" target=\"searchresult\" class=\"searchTopLink\" >{2}</a>"
			+ "<div class=\"searchItemBody\" >{3}</div> "
			+ "<a href=\"{1}\" target=\"searchresult\" class=\"searchBottonLink\" >{1}</a> "
			+ "</div><!--ie-->";

	String pagerFormat = "<a href=\"{0}\" class=\"searchPage\" >{1}</a></li>";
	String singlePageFormat = " ";
	String searchTerms = searchBean.getSearch();
	String rssURL = "";
	String rssLink = "";
	if ( searchTerms != null && searchTerms.length() > 0 ) {
		rssURL = searchBean.getToolUrl()+"/rss20?search="+java.net.URLEncoder.encode(searchBean.getSearch(),"UTF-8");
		rssLink = "<link rel=\"alternate\" title=\""+org.sakaiproject.search.tool.Messages.getString("jsp_search_for")+": "+searchTerms+" \" " 
 			+ " href=\""+rssURL+"\" type=\"application/rss+xml\" /> ";
    
	}
	String searchHeaderFormat = 
	        "<a href="+rssURL+"\" target=\"rss\" id=\"rssLink\" > " +
	        "<img src=\"/library/image/transparent.gif\" title=\"RSS\" alt=\"RSS\" border=\"0\" /> " +
	        "</a>" +
			org.sakaiproject.search.tool.Messages.getString("jsp_found_line");
	
	String termsFormat = "<span style=\"font-size:{1}em;\" ><a href=\"?panel=Main&search={0}\" >{0}</a></span> ";

%>
<html>
  <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
     <title><%= org.sakaiproject.search.tool.Messages.getString("jsp_search") %>: <%= searchBean.getSearchTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
      <%= rssLink %>  
      <link rel="search"
           type="application/opensearchdescription+xml" 
           href="<%= searchBean.getOpenSearchUrl() %>"
           title="<%= org.sakaiproject.search.tool.Messages.getString("jsp_worksite_search") %>" />
    <script type="text/javascript" >
        function searchLocalAddSherlock() {
        	addSherlockButton(
        	    "<%= searchBean.getSiteTitle() %>",
        		"<%= org.sakaiproject.search.tool.Messages.getString("jsp_sakai_search") %>",
        		"<%= searchBean.getBaseUrl() %>");
        }
        appendLoader(searchLocalAddSherlock);
    </script>
    </head>
    <body 
    onload="callAllLoaders(); setMainFrameHeightNoScroll('<%= request.getAttribute("sakai.tool.placement.id") %>');parent.updCourier(doubleDeep,ignoreCourier);  "
    >  
<%@include file="header.jsp"%>
    	<div class="portletBody">    		
    	
	  <% if ( searchBean.hasAdmin() ) { %>
    <div class="navIntraTool">
	  <span class="rwiki_pageLinks">
	    <a href="<%= searchBean.getToolUrl() %>/admin/index"><%= org.sakaiproject.search.tool.Messages.getString("jsp_admin") %></a>
	  </span>
    </div>
	  <% } %>
    <%
    if ( searchBean.isEnabled() ) 
    {
    %>
	<div class="navPanel">
		<div class="searchBox">
			<form action="?#" method="get" class="inlineForm"  >  
				<label for="search">
					<%= org.sakaiproject.search.tool.Messages.getString("jsp_search") %>:  
				</label>
				<input type="hidden" name="panel" value="Main" />
				<input type="text" id="search"  name="search" size="42" maxlength="1024" value="<%= searchBean.getSearch() %>"/>
				<select name="scope">
					<option value="SITE"><%= org.sakaiproject.search.tool.Messages.getString("jsp_current_site") %></option>
					<option value="MINE"><%= org.sakaiproject.search.tool.Messages.getString("jsp_all_my_sites") %></option>
				</select>
				<input type="submit" name="sb" value="<%= org.sakaiproject.search.tool.Messages.getString("jsp_search") %>" />
			</form>
		</div>
	</div>	
	
	
	<div class="searchHeader">
	<span id="sherlockButtonHolder" >
<!--
	    <a href="#" id="addSherlockButton" >
	    	<img src="/library/image/transparent.gif" 
	    		border="0"   
	    		title="<%= org.sakaiproject.search.tool.Messages.getString("jsp_install_plugin") %>" 
	    		alt="<%= org.sakaiproject.search.tool.Messages.getString("jsp_install_plugin") %>" />
	    </a>
-->
	</span>
	<%= searchBean.getHeader(searchHeaderFormat) %>
	</div>

    <%
    	if ( searchBean.hasResults() ) 
    	{
    %>
    <div class="searchPageContainer" >
    	<%= searchBean.getPager(pagerFormat, singlePageFormat) %>
    </div>
    <div class="searchTabsContainer" >
	   <span id="results" class="tabHeadOn" >        	
		  <p class="tabhead" title="Results" ><a href="#" onclick="selectTabs('tagsTab','tabOn','tabOff','resultsTab','tabOff','tabOn','tags','tabHeadOn','tabHeadOff','results','tabHeadOff','tabHeadOn'); setMainFrameHeightNoScroll('<%= request.getAttribute("sakai.tool.placement.id") %>'); return false;" ><%= org.sakaiproject.search.tool.Messages.getString("jsp_results") %></a></p>
       </span>
	   <span id="tags" class="tabHeadOff" >        	
		  <p class="tabhead" title="Tags" ><a href="#" onclick="selectTabs('tagsTab','tabOff','tabOn','resultsTab','tabOn','tabOff','tags','tabHeadOff','tabHeadOn','results','tabHeadOn','tabHeadOff'); setMainFrameHeightNoScroll('<%= request.getAttribute("sakai.tool.placement.id") %>'); return false;" ><%= org.sakaiproject.search.tool.Messages.getString("jsp_tags") %></a></p>
    	</span>
    </div>
    <div class="searchResultsContainer" >
		  <div id="resultsTab" class="tabOn" >
		   <!--ls--> 
    <%= searchBean.getSearchResults(searchItemFormat,errorMessageFormat) %>
    	   <!--le--> 
          </div>
		  <div id="tagsTab" class="tabOff" >
		     <div id="aboutTabs" >
		     <%= org.sakaiproject.search.tool.Messages.getString("jsp_about_tags") %> 
		     </div>
    	     <%= searchBean.getTerms(termsFormat,100,10,3,true) %>
    	  </div>
    </div>
    <div class="searchPageContainer" >
    	<%= searchBean.getPager(pagerFormat, singlePageFormat) %>
    </div>
    <%
    	}
    }
    else
    {
    %>
    <p>
    <%= org.sakaiproject.search.tool.Messages.getString("jsp_search_off_msg") %>
    </p>
    <%
    }
    %>

<%@include file="footer.jsp"%>
</div>
</body>
</html>
