<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"  %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<%
	org.sakaiproject.search.tool.SearchBeanFactory searchBeanFactory = 
	  (org.sakaiproject.search.tool.SearchBeanFactory)
		request.getAttribute(org.sakaiproject.search.tool.SearchBeanFactory.SEARCH_BEAN_FACTORY_ATTR);
	org.sakaiproject.search.tool.SearchAdminBean searchAdminBean = searchBeanFactory.newSearchAdminBean(request);
	
	String adminOptionsFormat = "<a href=\"{0}\" {2} >{1}</a>"; 
	String indexStatusFormat = org.sakaiproject.search.tool.Messages.getString("jsp_searchadmin_index_last_loaded") + " ";
	String masterRowFormat = "<tr><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td></tr>";
	String workerRowFormat = "<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>";
	String segmentInfoRowFormat = "<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>";
	
%>
<html>
  <head>
     <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
     <meta http-equiv="Content-Style-Type" content="text/css" />
     <title><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_adminsearch")%> <%= searchAdminBean.getTitle() %></title>
      <%= request.getAttribute("sakai.html.head") %>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders(); " 
    >
<%@include file="header.jsp"%>
    	<div class="portletBody">    		
    	
    <div class="navIntraTool">
	    <!-- Home Link -->
	    <a href="../index"><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_search")%></a>
    		<%= searchAdminBean.getAdminOptions(adminOptionsFormat) %>
    </div>
    

    <p>
    <%= searchAdminBean.getCommandFeedback() %><br />
	<%= searchAdminBean.getIndexStatus(indexStatusFormat) %>
	</p>
	
	<table summary="<%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_master_control_records")%>" >
	<tr><th colspan="4"><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_master_control_records")%></th></tr>
	<tr>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_context")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_operation")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_current_status")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_last_update")%></td>
	</tr>
	<%= searchAdminBean.getGlobalMasterDocuments(masterRowFormat) %>
	</table>
	<table summary="<%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_site_control_records")%>">
	<tr><th colspan="4"><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_site_control_records")%></th></tr>
	<tr>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_context")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_operation")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_current_status")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_last_update")%></td>
	</tr>
	<%= searchAdminBean.getSiteMasterDocuments(masterRowFormat) %>
	</table>
	<table summary="<%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_list_index")%>">
	<tr><th colspan="3"><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_indexer_workers")%></th></tr>
	<tr>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_worker_thread")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_due_before")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_status")%></td>
	</tr>
	<%= searchAdminBean.getWorkers(workerRowFormat) %>
	</table>
	<table summary="<%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_list_segments")%>" >
	<tr><th colspan="3"><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_segments")%></th></tr>
	<tr>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_segment_name")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_segment_size")%></td>
		<td><%=org.sakaiproject.search.tool.Messages.getString("searchadmin_index_segment_last_update")%></td>
	</tr>
	<%= searchAdminBean.getSegmentInfo(segmentInfoRowFormat) %>
	</table>
    </div>
<%@include file="footer.jsp"%>
   </body>
</html>
