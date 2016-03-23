<!-- $Id$
<%--
***********************************************************************************
* Portions Copyright (c) 2004 Sun Microsystems from the Java Series, Core Java ServerFaces
* source freely distributable.
* see http://www.sun.com/books/java_series.html
 ***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
<html>
   <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
   <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
   <%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>

   <f:view>
      <head><%= request.getAttribute("html.head") %>
         <title>A file upload test</title>
      </head>
      <body>
         <h:form enctype="multipart/form-data">
            Upload a file:
            <corejsf:upload target="assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/"/>
            <h:commandButton value="Upload" action="submit"/>
         </h:form>
      </body>
   </f:view>
</html>