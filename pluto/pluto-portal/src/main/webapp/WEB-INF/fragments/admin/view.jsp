<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<div>
  <h2>Portlet Applications</h2>
  <p>
    <ul>
      <c:forEach items="${driverConfig.portletApplications}" var="app">
        <li>
          <c:out value="${app.contextPath}"/>
          <ul>
            <c:forEach items="${app.portlets}" var="portlet">
              <c:out value="${portlet.portletName}"/>
            </c:forEach>
          </ul>
        </li>
      </c:forEach>
    </ul>
  </p>
</div>

<div>
  <h2>Portal Pages</h2>
  <p>
    <ul>
      <c:forEach items="${driverConfig.pages}" var="page">
        <li>
          <c:out value="${page.name}"/><br/>
          &nbsp;&nbsp;<small><c:out value="${page.uri}"/></small>
        </li>
      </c:forEach>
    </ul>
  </p>
</div>


