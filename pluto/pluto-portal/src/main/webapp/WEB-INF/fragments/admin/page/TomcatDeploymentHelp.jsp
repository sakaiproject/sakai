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
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<div class="portlet-section-header">Deploying Portlet Applications in Tomcat</div>

<div class="portlet-section-subheader">Adding New Portlets to a Portal Page</div>

<p class="portlet-font">
The Page Administrator Portlet works with deployed portlet applications. This deployment can be done via the Pluto 
Maven plugin or Ant task. Alternately, you can deploy the war using the 'Upload and deploy portlet war' 
link on the bottom of the Page Administrator Portlet that points to the app server's war deployer application. 
Clicking on that link opens up a new browser window, so you should disable any popup blockers for this page.
</p>

<div class="portlet-section-subheader">Uploading and deploying using Tomcat's Manager Application</div>
<p class="portlet-font">
The binary distribution of Pluto is built on Tomcat. Tomcat'’s deployer application is the manager application. 
Use that application to upload and deploy the war. A properly deployed portlet on Tomcat requires that the war 
have a context.xml file in META-INF containing a Context element and the crossContext attribute set to true 
like this:<br/>
&lt;Context path="HelloWorldPortlet" docBase="HelloWorldPortlet" crossContext="true"&gt;
</p>

<p class="portlet-font">
The logged in user also needs to have a manager role also, 
which is configured in conf/tomcat-users.xml. In the binary distribution, the tomcat and pluto users have the manager 
role already set.  
</p>
<p class="portlet-font">
When the portlet application has been deployed, restart Pluto and use the Page Administrator Portlet to add the new portlet to a page. 
If you want to put the portlet on a new page, you must do so by manually adding a page element as a child of the 
render-config element in pluto-portal-driver-config.xml before you restart Pluto. See the Help mode for more details
on doing this process.
</p>

<div class="portlet-section-subheader">Deployment in Another Application Server</div>
<p class="portlet-font">
	The 'Upload and deploy portlet war' link can be changed to an appropriate administrative page when Pluto
	is deployed inside another (non-Tomcat) app server. This is done by changing the appserver.deployer.url property
	inside AdminPortlet.properties located in the WEB-INF/classes directory to point to the URL of an appropriate administrative
	page. The 'Help' link can also be changed to another help page using the appserver.deployer.help.page property. 
	That help file needs to reside in this directory (WEB-INF/fragments/admin/page).
</p>


<fmt:bundle basename="AdminPortlet">
	<fmt:message key="appserver.deployer.url" var="deployerURL"/>
</fmt:bundle> 

<p class="portlet-font">
<a href='<c:out value="${deployerURL}"/>' target="_blank">Upload and deploy a new portlet war</a> 
</p>
<p class="portlet-font">
<a href='<portlet:renderURL portletMode="view"/>'>Page Administrator Portlet</a> 
</p>
<p class="portlet-font">
<a href='<portlet:renderURL portletMode="help"/>'>Page Administrator Portlet Help</a> 
</p>
