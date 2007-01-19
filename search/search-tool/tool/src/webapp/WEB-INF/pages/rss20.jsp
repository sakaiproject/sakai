<%@ page language="java" contentType="text/xml;charset=UTF-8" pageEncoding="UTF-8"  %><?xml version="1.0" encoding="UTF-8"?>
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request,"dateRelevanceSort","normal");
	
	String searchItemFormat = 
	  "<item><title>{0}: {2}</title><link>{1}</link><description> " 
	        + "<div class=\"searchItem\" > " 
	        + "<span class=\"searchTool\">{0}:</span> "
	        + "<a href=\"{1}\" target=\"searchresult\" class=\"searchTopLink\" >{2}</a>"
			+ "<div class=\"searchItemBody\" >{3}</div> "
			+ "<a href=\"{1}\" target=\"searchresult\" class=\"searchBottonLink\" >{1}</a> "
			+ "</div>"
	+ "</description>"
	+ "</item>";
	String errorMessageFormat = 
	  "<item><title>Error</title><description> " 
	+ "<p class=\"alertMessage\" >{0}</p>"
	+ "</description>"
	+ "</item>";
	String systemName = searchBean.getSystemName();
%>
<rss version="2.0">
  <channel>
     <title>
      <%= systemName %> <%= org.sakaiproject.search.tool.Messages.getString("jsp_search_rss_feed") %> <%= searchBean.getSearchTitle() %>
     </title>
     <description>
      <%= systemName %> <%= org.sakaiproject.search.tool.Messages.getString("jsp_search_rss_feed") %> <%= searchBean.getSearchTitle() %>
     </description>
     <link>
     <%= request.getRequestURL() %>
     </link>
     <lastBuildDate>
     <%= (new java.util.Date()).toString() %>
     </lastBuildDate>
     <generator><%= systemName %> <%= org.sakaiproject.search.tool.Messages.getString("jsp_search_rss_generator") %></generator>
    		<%= searchBean.getSearchResults(searchItemFormat,errorMessageFormat) %>
     </channel>
</rss>

