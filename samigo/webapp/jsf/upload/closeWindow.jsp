<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <f:view>
      <head><%= request.getAttribute("html.head") %>
         <title>Close file upload window</title>
      </head>
      <body onload="javascript:self.close()">
      </body>
   </f:view>
</html>