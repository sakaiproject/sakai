<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchAdminBean searchAdminBean = searchBeanFactory.newSearchAdminBean(request);
	
	String adminOptionsFormat = "<a href=\"{0}\" {2} >{1}</a>"; 
	String indexStatusFormat = "Last loaded at {0} in {1} <br /> Being indexed by {2} expected fo finish before {3} <br /> Index contains {4} documents and {5} pending  ";
	String masterRowFormat = "<tr><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td></tr>";
	String workerRowFormat = "<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>";
	String segmentInfoRowFormat = "<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>";
	
%>
<html>
  <head>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
     <meta http-equiv="Content-Style-Type" content="text/css" />
     <title>Admin Search: <%= searchAdminBean.getTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); " 
    >
<%@include file="header.jsp"%>
    	<div class="portletBody">    		
    	
    <div class="navIntraTool">
	    <!-- Home Link -->
	    <a href="../index">Search</a>
    		<%= searchAdminBean.getAdminOptions(adminOptionsFormat) %>
    </div>
    

    <p>
    <%= searchAdminBean.getCommandFeedback() %><br />
	<%= searchAdminBean.getIndexStatus(indexStatusFormat) %>
	</p>
	
	<table summary="Master Control Records that control all sites" >
	<tr><th colspan="4">Master Control Records</th></tr>
	<tr><td>Context</td><td>Operation</td><td>Current Status</td><td>Last Update</td></tr>
	<%= searchAdminBean.getGlobalMasterDocuments(masterRowFormat) %>
	</table>
	<table summary="Site Control records that control just this site">
	<tr><th colspan="4">Site Control Records</th></tr>
	<tr><td>Context</td><td>Operation</td><td>Current Status</td><td>Last Update</td></tr>
	<%= searchAdminBean.getSiteMasterDocuments(masterRowFormat) %>
	</table>
	<table summary="A list of index workers" >
	<tr><th colspan="3">Indexer Workers</th></tr>
	<tr><td>Worker Thread</td><td>Due Before</td><td>Status</td></tr>
	<%= searchAdminBean.getWorkers(workerRowFormat) %>
	</table>
	<table summary="A list index segments" >
	<tr><th colspan="3">Segments</th></tr>
	<tr><td>Segment Name</td><td>Size</td><td>Last Update</td></tr>
	<%= searchAdminBean.getSegmentInfo(segmentInfoRowFormat) %>
	</table>
    </div>
<%@include file="footer.jsp"%>
   </body>
</html>
