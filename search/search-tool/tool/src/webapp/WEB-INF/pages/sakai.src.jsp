<%@ page language="java" contentType="application/opensearchdescription+xml;charset=UTF-8" pageEncoding="UTF-8"  %><%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SherlockSearchBean sSearchBean = 
		searchBeanFactory.newSherlockSearchBean(request);
%>
# Status: working beta
# Mozilla/Netscape 6+ plugin for Sakai search tool
# by <ian@__NOSPAMPLEASE_caret.cam.ac.uk>
#  
# Created: Nov 13, 2006
# Last updated: Nov 13, 2006
#
# Known issues:
#
# 1. Something doesn't always work
# 2. Something isn't supported
# 3. Any notes you may have.
<search 
   name="<%= sSearchBean.getSystemName() %>: <%= sSearchBean.getSiteName() %>"
   description="<%= org.sakaiproject.search.tool.Messages.getString("jsp_sakai_search_for_site") %> <%= sSearchBean.getSiteName() %>"
   method="GET"
   action="<%= sSearchBean.getSearchURL() %>"
   queryCharset="UTF-8"
>
<input name="search" user>
<input name="sourceid" value="Mozilla-search">
<input name="panel" value="Main" >
<interpret 
    browserResultType="result" 
    resultListStart="<!--ls-->" 
    resultListEnd="<!--le-->" 
    resultItemStart="<!--is-->" 
    resultItemEnd="<!--ie-->"
>
</search>

<BROWSER
   update="<%= sSearchBean.getUpdateURL() %>" 
   updateIcon="<%= sSearchBean.getUpdateIcon() %>" 
   updateCheckDays="7"
>