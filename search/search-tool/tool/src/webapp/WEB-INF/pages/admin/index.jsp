<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchAdminBean searchAdminBean = searchBeanFactory.newSearchAdminBean(request);
	
	String adminOptionsFormat = "<li><a href=\"{0}\" >{1}</a></li>"; 
	String indexStatusFormat = "Last loaded at {0} in {1} <br /> Being indexed by {2} expected fo finish before {3} <br /> Index contains {4} documents and {5} pending  ";
	String masterRowFormat = "<tr><td>{2}</td><td>{3}</td><td>{4}</td></tr>";
	String workerRowFormat = "<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>";
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
	<p>
	
	<p>
	Master Control Records
	</p>
	<table>
	<tr><td>Context</td><td>Operation</td><td>Last Expected</td></tr>";
	<%= searchAdminBean.getGlobalMasterDocuments(masterRowFormat) %>
	</table>
	<p>
	Site Control Records
	</p>
	<table>
	<tr><td>Context</td><td>Operation</td><td>Last Expected</td></tr>";
	<%= searchAdminBean.getSiteMasterDocuments(masterRowFormat) %>
	</table>
	Workers
	</p>
	<table>
	<tr><td>Worker Thread</td><td>Last Update</td><td>Rus Status</td></tr>";
	
	<%= searchAdminBean.getWorkers(workerRowFormat) %>
	</table>
<%@include file="footer.jsp"%>
   </body>
</html>
