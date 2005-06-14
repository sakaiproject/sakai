<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>

   <f:view>
      <head>                  
         <title>A file upload test</title>
      </head>
      <body>
         <h:form enctype="multipart/form-data">
            Upload a file: 
            <corejsf:upload target="assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{delivery.username}/"/>
            <h:commandButton value="Upload" action="submit"/>
         </h:form>
      </body>
   </f:view>
</html>