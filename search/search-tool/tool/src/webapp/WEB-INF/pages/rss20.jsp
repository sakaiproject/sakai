<%@ page language="java" contentType="text/xml;charset=UTF-8"  %><?xml version="1.0" encoding="UTF-8"?>
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchBean searchBean = searchBeanFactory.newSearchBean(request,"dateRelevanceSort","normal");
	
	String searchItemFormat = 
	  "<item><title>{2}</title><link>{1}</link><description> " 
	+ "<p class=\"searchItem\" ><a href=\"{1}\" target=\"searchresult\" >{2}</a><br />"
	+ "{3} <br/> "
	+ "<span class=\"searchScore\" > Hit: {0} "
	+ " Score: {4} <a href=\"{1}\" target=\"searchresult\" >{1}</a> "
	+ "</span></p>"
	+ "</description>"
	+ "</item>";
	String errorMessageFormat = 
	  "<item><title>Error</title><description> " 
	+ "<p class=\"alertMessage\" >{0}</p>"
	+ "</description>"
	+ "</item>";
%>
<rss version="2.0">
  <channel>
     <title>
      Sakai Search RSS Feed <%= searchBean.getSearchTitle() %>
     </title>
     <description>
      Sakai Search RSS Feed <%= searchBean.getSearchTitle() %>
     </description>
     <link>
     <%= request.getRequestURL() %>
     </link>
     <lastBuildDate>
     <%= (new java.util.Date()).toString() %>
     </lastBuildDate>
     <generator>Sakai Search RSS Generator</generator>
    		<%= searchBean.getSearchResults(searchItemFormat,errorMessageFormat) %>
     </channel>
</rss>

