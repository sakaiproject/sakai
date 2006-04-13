<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchAdminBean searchAdminBean = searchBeanFactory.newSearchAdminBean(request);
	
	String adminOptionsFormat = "<li><a href=\"{0}\" >{1}</a></li>"; 
	String indexStatusFormat = "Index Status: {0} <br /> {1} documents and {2} pending ";
%>
<html>
  <head>
     <title>Admin Search: <%= searchAdminBean.getTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); " 
    >
<%@include file="header.jsp"%>
    	<div class="portletBody">    		
    	
    <div class="navIntraTool">
	  <span class="rwiki_pageLinks">
	    <!-- Home Link -->
	    <a href="../index">Search</a>
	  </span>
    </div>
    <ul>
    
    <li>
    <%= searchAdminBean.getAdminOptions(adminOptionsFormat) %>
    </li>

    <p>
	<%= searchAdminBean.getIndexStatus(indexStatusFormat) %>
	</p>
<%@include file="footer.jsp"%>
   </body>
</html>
