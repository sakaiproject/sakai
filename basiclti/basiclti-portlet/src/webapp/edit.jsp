<%--
    $URL$
    $Id$
    
    Copyright (c) 2009 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page import="javax.portlet.PortletSession" %>
<%@ page import="java.util.Properties" %>
<%@ page import="org.sakaiproject.util.ResourceLoader" %>

<%@ page session="false" %>
<%!

private static ResourceLoader rb = new ResourceLoader("basiclti");

// Allow the user to set unless final.launch etc. is true
public boolean allow(Properties props, String s) {
  if ( props == null || s == null ) return false;
  String prop = props.getProperty("final."+s); 
  if ( prop == null ) return true;
  if ( "true".equals(prop) ) return false;
  return true;
}
%>
<%
RenderResponse rResp = (RenderResponse)request.getAttribute("javax.portlet.response");
RenderRequest rReq = (RenderRequest)request.getAttribute("javax.portlet.request");
PortletSession pSession = rReq.getPortletSession(true);

PortletURL viewURL = rResp.createActionURL();
viewURL.setParameter("sakai.action","main");

PortletURL clearURL = rResp.createActionURL();
clearURL.setParameter("sakai.action","edit.clear");

PortletURL resetURL = rResp.createActionURL();
resetURL.setParameter("sakai.action","edit.reset");

PortletURL launchURL = rResp.createActionURL();
launchURL.setParameter("sakai.action","edit.save");

PortletURL actionURL = rResp.createActionURL();

String errorOutput = (String) pSession.getAttribute("error.output");
if ( errorOutput != null ) out.println(errorOutput);

Properties ov = (Properties) rReq.getAttribute("imsti.oldvalues");

Properties sp = (Properties) rReq.getAttribute("imsti.properties");

%>
<portlet:defineObjects/>

<a href="<%=viewURL.toString()%>"><%=rb.getString("edit.exit")%></a>
 | 
<a href="<%=clearURL.toString()%>"><%=rb.getString("edit.clear.session")%></a>
 | 
<a href="<%=resetURL.toString()%>"><%=rb.getString("edit.clear.prefs")%></a>
<p/>
<% if ( allow(sp,"launch") || allow(sp,"key") || allow(sp,"secret") || 
        allow(sp,"xml") ||
        allow(sp,"pagetitle") || allow(sp,"tooltitle") ||
        allow(sp,"resource") || allow(sp,"preferwidget") || allow(sp,"height") || allow(sp,"width") || 
        allow(sp,"frameheight") || allow(sp,"custom") || allow(sp, "releasename") || allow(sp,"releaseemail") ) { %>
<form method="post" action="<%=launchURL.toString()%>">
<% if ( allow(sp,"launch") || allow(sp,"key") || allow(sp,"secret") || allow(sp,"xml") ) { %>
<fieldset>
<legend><%=rb.getString("required.information") %></legend>
<% if ( allow(sp,"launch") && allow(sp,"xml") ) { %>
<script type="text/javascript">
function switchui()
{
  var x=document.getElementById("UISwitcher");
  if ( x.selectedIndex == 0 ) {
    document.getElementById("xml.paste").style.display = "none";
    document.getElementById("url.input").style.display = "block";
  } else {
    document.getElementById("url.input").style.display = "none";
    document.getElementById("xml.paste").style.display = "block";
  }
}
</script>
<select id="UISwitcher" name="imsti.type" onchange="switchui(); return false;">
  <option value="URL"><%=rb.getString("select.url") %></option>
<% if ( ov.getProperty("imsti.xml",null) != null ) { %>
  <option selected="selected" value="XML"><%=rb.getString("select.xml") %></option>
<% } else { %>
  <option value="XML"><%=rb.getString("select.xml")%></option>
<% } %>
</select>
<% } %>
<% if ( allow(sp,"xml") ) { %>
<div id="xml.paste">
<textarea rows="20" cols="60"  name="imsti.xml" >
<%=ov.getProperty("imsti.xml","")%>
</textarea>
</div>
<% } %>
<% if ( allow(sp,"launch") ) { %>
<div id="url.input">
<p>
<%=rb.getString("tool.url") %>
<input type="text" size="80" name="imsti.launch" value="<%=ov.getProperty("imsti.launch","")%>">
<%=rb.getString("tool.url.detail") %>
</p>
</div>
<% } %>
<% if ( allow(sp,"key") ) { %>
<p>
<%=rb.getString("tool.key") %>
<input type="text" name="imsti.key" value="<%=ov.getProperty("imsti.key","")%>"> 
<%=rb.getString("tool.key.detail") %>
</p>
<% } %>
<% if ( allow(sp,"secret") ) { %>
<p>
<%=rb.getString("tool.secret") %>
<input type="password" name="imsti.secret"> 
<%=rb.getString("tool.secret.detail") %>
</p>
<% } %>
</fieldset>
<% } %>
<script type="text/javascript">
if ( document.getElementById("UISwitcher") ) switchui();
</script>

<% if ( allow(sp,"pagetitle") || allow(sp,"tooltitle") ) { %>
<fieldset>
<legend><%=rb.getString("display.information") %></legend>
<% if ( allow(sp,"pagetitle") ) { %>
<p>
<%=rb.getString("page.title") %>
<input type="text" name="imsti.pagetitle" value="<%=ov.getProperty("imsti.pagetitle","")%>"> 
<%=rb.getString("page.title.detail") %>
</p>
<% } %>
<% if ( allow(sp,"tooltitle") ) { %>
<p>
<%=rb.getString("tool.title") %>
<input type="text" name="imsti.tooltitle" size="40" value="<%=ov.getProperty("imsti.tooltitle","")%>"> 
<%=rb.getString("tool.title.detail") %>
</p>
<% } %>
</fieldset>
<% } %>
<% if ( allow(sp,"frameheight") || allow(sp, "debug") ) { %>
<fieldset>
<legend><%=rb.getString("launch.information") %></legend>
<% if ( allow(sp,"frameheight") ) { %>
<p>
<%=rb.getString("iframe.height") %>
<input type="text" size="10" name="imsti.frameheight" value="<%=ov.getProperty("imsti.frameheight","")%>"> 
<%=rb.getString("iframe.height.detail") %>
</p>
<% } %>
<% if ( allow(sp,"debug") ) { %>
<p>
<%=rb.getString("debug.launch") %>
<input type="checkbox" size="10" name="imsti.debug" 
<% if ( ov.getProperty("imsti.debug",null) != null ) { %>
  checked="yes" />
<% } else { %>
   />
<% } %>
<%=rb.getString("debug.launch.detail") %>
</p>
<% } %>
</fieldset>
<% } %>

<% if ( allow(sp,"releasename") || allow(sp, "releaseemail") ) { %>
<fieldset>
<legend><%=rb.getString("launch.privacy") %></legend>
<% if ( allow(sp,"releasename") ) { %>
<p>
<%=rb.getString("privacy.releasename") %>
<input type="checkbox" size="10" name="imsti.releasename" 
<% if ( ov.getProperty("imsti.releasename",null) != null ) { %>
  checked="yes" />
<% } else { %>
   />
<% } %>
<% } %>
<% if ( allow(sp,"releaseemail") ) { %>
<p>
<%=rb.getString("privacy.releaseemail") %>
<input type="checkbox" size="10" name="imsti.releaseemail" 
<% if ( ov.getProperty("imsti.releaseemail",null) != null ) { %>
  checked="yes" />
<% } else { %>
   />
<% } %>
<%=rb.getString("launch.privacy.detail") %>
</p>
<% } %>
</fieldset>
<% } %>
<% if ( allow(sp,"custom") ) { %>
<fieldset>
<legend><%=rb.getString("launch.custom") %></legend>
<p>
<textarea rows="10" cols="60"  name="imsti.custom" >
<%=ov.getProperty("imsti.custom","")%>
</textarea>
<%=rb.getString("launch.custom.detail") %>
</p>
</fieldset>
<% } %>
<p>
<input type="submit" value="<%=rb.getString("update.options")%>">
<input type="submit" value="<%=rb.getString("edit.exit")%>" 
    onclick="window.location='<%=viewURL.toString()%>'; return false;"/>
</p>
</form>
<% } else { %>
<p>Configuration has been pre-set and cannot be edited for this placement.</p>
<% } %>
