<%@ page language="java" contentType="application/opensearchdescription+xml;charset=UTF-8" pageEncoding="UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.OpenSearchBean openSearchBean = 
		searchBeanFactory.newOpenSearchBean(request);
%>
<OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/"
                       xmlns:moz="http://www.mozilla.org/2006/browser/search/">
   <ShortName><%= openSearchBean.getSystemName() %>Sakai: <%= openSearchBean.getSiteName()%></ShortName>
   <Description><%= org.sakaiproject.search.tool.Messages.getString("jsp_sakai_search_for_site") %>: <%= openSearchBean.getSiteName()%></Description>
   <Image height="16" width="16" type="image/x-icon"><%= openSearchBean.getIconUrl()%></Image>
   <Query role="example" searchTerms="sakai" />
   <Developer>sakaiproject.org Development Team</Developer>
   <Attribution><%= openSearchBean.getAttibution() %></Attribution>
   <SyndicationRight><%= openSearchBean.getSindicationRight() %></SyndicationRight>
   <AdultContent><%= openSearchBean.getAdultContent() %></AdultContent>
   <Language><%= org.sakaiproject.search.tool.Messages.getString("jsp_opensearch_lang") %></Language>
   <OutputEncoding>UTF-8</OutputEncoding>
   <InputEncoding>UTF-8</InputEncoding>   
   <Url type="text/html" method="GET" 
   		template="<%= openSearchBean.getHTMLSearchTemplate() %>" >
   </Url>
     <Url type="application/rss+xml"
   		template="<%= openSearchBean.getRSSSearchTemplate() %>" >
   </Url>
<!--
   <Url type="application/x-suggestions+json" template="suggestionURL"/>
-->
   <moz:SearchForm><%= openSearchBean.getHTMLSearchFormUrl() %></moz:SearchForm>
</OpenSearchDescription>
