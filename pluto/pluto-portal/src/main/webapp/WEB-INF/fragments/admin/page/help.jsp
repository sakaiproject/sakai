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

<div class="portlet-section-header">Page Adminstrator Portlet Help</div>

<p class="portlet-font">
The Page Administrator Portlet is used to add and remove pages and portlets from portal pages. The portlet
also persist these changes to pluto-portal-driver-config.xml in pluto's WEB-INF directory.
</p>

<div class="portlet-section-subheader"><a name="ConfiguringPortletApp">Configuring a Portlet Application</a></div>
<p class="portlet-font">
The portlet application needs to be bundled in a war file as per the Java Portlet Specification. 
The war also needs to include proper PortletServlet servlet and servlet-mapping records in WEB-INF/web.xml.
An assembly process has been developed to add these records to web.xml using Maven 2 (the
pluto:assemble goal in maven-pluto-plugin) or Ant (AssembleTask). 
See the testsuite web.xml file for an example how the servlet and servlet-mapping
records should look like after assembly (other items the developer adds to web.xml should be 
carried forward into the updated file). 
</p>

<p class="portlet-font">
A custom portlet war deployed into the bundled distribution of Pluto also needs a Tomcat context descriptor 
with the same name as the portlet app war name (a.k.a. context name). This context file needs to be 
in the META-INF directory of the war file. Here's an example of one for a portlet bundled for a 
HelloWorldPortlet context (file name is HelloWorldPortlet.xml): 
<pre>
&lt;Context path="/HelloWorldPortlet" 
	docBase="HelloWorldPortlet" crossContext="true"/&gt; 
</pre>  
The crossContext attribute allows Pluto, which is deployed in its own Tomcat context, to work with this custom portlet. 
</p>

<div class="portlet-section-subheader">Deploying a Portlet Application</div>
<p class="portlet-font">
	The way to deploy a portlet application depends on the app server Pluto is running in. In the bundled distribution
	Pluto is deployed in Tomcat. In this case, you can use the Tomcat manager app to deploy the portlet
	war. There is a 'Upload and deploy portlet war' link at the bottom of the Page Administrator portlet that points to 
	the manager app	in the bundled distribution (this link can be changed for other app servers -- see the 
	adjacent Help link). Use of the manager application requires you to be logged into Pluto in a manager role (pluto or
	tomcat user). 
</p>
	
<p class="portlet-font">
	In the bundled distribution of Pluto, you can also deploy a properly configured portlet application by simply dropping
	the war file into the webapp directory (see <a href="#ConfiguringPortletApp">Configuring a Portlet Application</a> above).
	You will need to restart Pluto in order for the Page Administrator Portlet to see the newly deployed portlet so it
	can be added to a portal page.
</p>

<div class="portlet-section-subheader">Adding Portal Pages</div>
<p class="portlet-font">
Adding a new portal page using the Pluto Page Administrator portlet involves inputing the page name into the text box adjacent 
to the Add Page button and clicking on the button. The new page is created with the default 'theme', which lays out the 
portlets in two columns (see /WEB-INF/themes/pluto-default-theme.jsp in the pluto webapp for details). Once a page
has been added, portlets will need to be added to the page (see <a href="#AddingPortlets">Adding Portlets to a Portal Page</a>).
</p>

<p class="portlet-font">
Please note that adding a new Page with the name of an existing page will replace the existing page with the
new page. This is equivalent to removing the page and adding it again with the same name.
</p>

<div class="portlet-section-subheader">Removing Portal Pages</div>
<p class="portlet-font">
Removing a portal page using the Pluto Page Administrator portlet involves selecting a page in the drop down above 
the Remove Page button and clicking on the button. You are not allowed to remove the default page 
(default attribute of the render-config element in pluto-portal-driver-config.xml) and the Pluto Admin page.
</p>

<div class="portlet-section-subheader"><a name="AddingPortlets">Adding Portlets to a Portal Page</a></div>
<p class="portlet-font">
Adding portlets to a portal page using the Pluto Page Administrator portlet involves first selecting a portal 
page in the Portal Pages drop-down (above the Remove Page button) and then selecting a portlet application 
using the Portlet Applications drop-down and finally a portlet in the adjacent drop down. When this is done, 
click the Add Portlet button.
</p>

<div class="portlet-section-subheader">Removing Portlets from a Portal Page</div>
<p class="portlet-font">
Removing portlets from a portal page involves selecting a portal page on the Portal Pages drop-down 
(above the Remove Page button), selecting the portlet in the adjacent list and then clicking on the 
Remove Portlet button.
</p>


<div class="portlet-section-subheader">Manually Updating the Configuration File</div>
<p class="portlet-font">
The pluto-portal-driver-config.xml file holds changes made by the Page Administrator Portlet. You can manually 
update this file to add pages or portlets. If this is done, please be careful of the structure of the 
render-config child elements. 
</p>

<p class="portlet-font">
You can also change the theme of portlets and the default page -- the portal's 
home page after login -- in this config file. The theme will require a proper URI to a file containing
the new theme. Use the default theme (/WEB-INF/themes/pluto-default-theme.jsp in the pluto webapp) for 
an example theme elements.

Again, be careful to not modify the XML structure of the 
config file if you choose to change the theme or default 
</p>


<%-- Properties for link to app server deployer and help mode file --%>
<fmt:bundle basename="AdminPortlet">
	<fmt:message key="appserver.deployer.help.page" var="deployerHelp"/>
</fmt:bundle> 
<portlet:renderURL portletMode="help" var="deployerhelpURL">
	<portlet-el:param name="helpPage" value="${deployerHelp}"/>
</portlet:renderURL>

<p class="portlet-font">
<a href='<c:out value="${deployerhelpURL}"/>'>Upload and Deployment in App Server Help</a>
</p>

<p class="portlet-font">
<a href='<portlet:renderURL portletMode="view"/>'>Page Administrator Portlet</a> 
</p>
