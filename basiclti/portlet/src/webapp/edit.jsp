<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page import="javax.portlet.PortletSession" %>
<%@ page import="java.util.Properties" %>
<%@ page session="false" %>
<%!
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

String whatURL = rResp.encodeURL(rReq.getContextPath()+"/whatisthis.htm");

PortletURL actionURL = rResp.createActionURL();

String errorOutput = (String) pSession.getAttribute("error.output");
if ( errorOutput != null ) out.println(errorOutput);

Properties ov = (Properties) rReq.getAttribute("imsti.oldvalues");

Properties sp = (Properties) rReq.getAttribute("imsti.properties");

%>
<portlet:defineObjects/>

<a href="<%=viewURL.toString()%>">Exit Edit Mode</a>
 | 
<a href="<%=clearURL.toString()%>">Clear Session Settings</a>
 | 
<a href="<%=resetURL.toString()%>">Clear Stored Preferences</a>
<p/>
<% if ( allow(sp,"launch") || allow(sp,"secret") || 
        allow(sp,"xml") ||
        allow(sp,"pagetitle") || allow(sp,"tooltitle") ||
        allow(sp,"resource") || allow(sp,"preferwidget") || allow(sp,"height") || allow(sp,"width") || allow(sp,"frameheight") ) { %>
<form method="post" action="<%=launchURL.toString()%>">
<% if ( allow(sp,"launch") || allow(sp,"secret") || allow(sp,"xml") ) { %>
<fieldset>
<legend>Required Information</legend>
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
  <option value="URL">URL and Password</option>
<% if ( ov.getProperty("imsti.xml",null) != null ) { %>
  <option selected="selected" value="XML">XML Paste</option>
<% } else { %>
  <option value="XML">XML Paste</option>
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
Remote Tool Url: <input type="text" size="100" name="imsti.launch" value="<%=ov.getProperty("imsti.launch","")%>">
</p>
</div>
<% } %>
<% if ( allow(sp,"secret") ) { %>
<p>
Remote Tool Password: <input type="password" name="imsti.secret"> (Must re-enter every time)
</p>
<% } %>
<p>
<input type="submit" value="Update Options">
</p>
</fieldset>
<% } %>
<script type="text/javascript">
if ( document.getElementById("UISwitcher") ) switchui();
</script>
<% if ( allow(sp,"pagetitle") || allow(sp,"tooltitle") ) { %>
<fieldset>
<legend>Display Information</legend>
<% if ( allow(sp,"pagetitle") ) { %>
<p>
Set Page Title: <input type="text" name="imsti.pagetitle" value="<%=ov.getProperty("imsti.pagetitle","")%>"> (Button text)
</p>
<% } %>
<% if ( allow(sp,"tooltitle") ) { %>
<p>
Set Tool Title: <input type="text" name="imsti.tooltitle" value="<%=ov.getProperty("imsti.tooltitle","")%>"> (Above the tool)
</p>
<% } %>
<p>
<input type="submit" value="Update Options">
</p>
</fieldset>
<% } %>
<% if ( allow(sp,"frameheight") || allow(sp, "debug") ) { %>
<fieldset>
<legend>Optional Launch Information</legend>
<% if ( allow(sp,"frameheight") ) { %>
<p>
iFrame Height: <input type="text" size="10" name="imsti.frameheight" value="<%=ov.getProperty("imsti.frameheight","")%>"> 
(<a href="#" onclick="window.open('<%=whatURL%>','name','width=480,height=400,menubar=no,resizable=no,status=no,toolbar=no');">What's this?</a>)
</p>
<% } %>
<% if ( allow(sp,"debug") ) { %>
<p>
Debug Launch: <input type="checkbox" size="10" name="imsti.debug" 
<% if ( ov.getProperty("imsti.debug",null) != null ) { %>
  checked="yes" />
<% } else { %>
   />
<% } %>
(<a href="#" onclick="window.open('<%=whatURL%>','name','width=480,height=400,menubar=no,resizable=no,status=no,toolbar=no');">What's this?</a>)
</p>
<% } %>
<p>
<input type="submit" value="Update Options">
</p>
</fieldset>
<% } %>
</form>
<% } else { %>
<p>Configuration has been pre-set and cannot be edited for this placement.</p>
<% } %>
