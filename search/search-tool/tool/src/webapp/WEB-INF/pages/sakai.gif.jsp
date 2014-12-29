<%@ page language="java" 
contentType="image/gif"  %><%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SherlockSearchBean sSearchBean = 
		searchBeanFactory.newSherlockSearchBean(request);
	sSearchBean.sendIcon(response);
	return;
%>