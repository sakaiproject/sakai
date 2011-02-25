<%@ page contentType="text/html" isELIgnored="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>

<portlet:defineObjects /> 

<div class="simple-rss-portlet">

	<div class="portlet-msg-error">
		<h2><c:out value="${errorHeading}" /></h2>
		<br class="clear"> 
		<p><c:out value="${errorMessage}" /></p>
	</div>
	
</div>
	

