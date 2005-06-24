<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<!-- $Id$ -->
<html>
 <head><%= request.getAttribute("html.head") %>
  <meta http-equiv="refresh"
   content="0;url=<%=request.getContextPath()%>/jsf/qti/importAssessment.faces">
 </head>
 <body onload="<%= request.getAttribute("html.body.onload") %>"><!-- this JSP page is a kludge for xtunnel & should redirect --></body>
</html>