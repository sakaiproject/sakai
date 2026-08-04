<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<% pageContext.setAttribute("now", new java.util.Date()); %>

<html>
  
  <head>
    <title>Pluto Portal</title>
    <style type="text/css" title="currentStyle" media="screen">
      @import "<c:out value="${pageContext.request.contextPath}"/>/pluto.css";
    </style>
    <script type="text/javascript"
            src="<c:out value="${pageContext.request.contextPath}"/>/pluto.js">
    </script>
  </head>

  <body>
    <div id="portal" style="width: 600px;">
      <div id="header">
        <h1>Apache Pluto</h1>
        <p>A Apache Portals Project</p>
      </div>
      <div id="content">
        <c:if test='${param.error == "1"}'>
          <p style="color:red;text-align:center">
            Invalid credentials. Please try again
          </p>
        </c:if>
        <form method="POST" action="j_security_check">
          <fieldset>
            <legend>Login to Pluto</legend>
            <div>
              <label for="j_username">User Name</label>
              <input type="text" name="j_username" id="j_username"/>
            </div>
            <div>
              <label for="j_password">Password</label>
              <input type="password" name="j_password" id="j_password"/>
            </div>
            <div>
              <label for="j_login"></label>
              <input type="submit" value="Login" name="login" id="j_login"/>
            </div>
          </fieldset>
        </form>
      </div>
      
      <div id="footer">
        &copy; 2003-<fmt:formatDate value="${now}" pattern="yyyy"/> Apache Software Foundation
      </div>
      
    </div>
  
  </body>
  
</html>


