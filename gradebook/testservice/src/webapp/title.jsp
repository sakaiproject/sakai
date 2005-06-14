<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ page import="java.util.List, java.util.Iterator" %>
<html>
	<head> <title>Test Gradebook Service</title>
		<%= request.getAttribute("sakai.html.head") %>
	</head>
	<body onload="<%= request.getAttribute("sakai.html.body.onload") %>">
 		<f:view>
			<h:outputText value="Test Gradebook Service" />
		</f:view>
	</body>
</html>
