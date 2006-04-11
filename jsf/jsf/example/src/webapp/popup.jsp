<%-- $Id: popup.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="popup tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>popup example</h2>
<hr />

<sakai:popup id="popup1" url="http://www.sun.com" useButton="true"
  title="popup title" target="_my_custom_target_window"
  toolbar="true"  menubar="false" personalbar="true" scrollbars="false"
  width="250" height="180"/>

<sakai:popup id="popup2" url="http://www.yahoo.com" useButton="false"
  title="popup title"
  toolbar="false"  menubar="true" personalbar="false" scrollbars="true"
  width="300" height="190"/>

<sakai:popup id="popup3" url="http://www.google.com" />

<hr />
<h3>popup usage:</h3>

<pre>

<PRE>
<font COLOR="#800080">&lt;sakai:popup</font> <font color="#800000">id</font>=</font><font COLOR="#0000ff">"popup1"</font><font COLOR="#000000"> <font color="#800000">url</font>=</font><font COLOR="#0000ff">"http://www.sakaiproject.org"</font><font COLOR="#000000"> <font color="#800000">useButton</font>=</font><font COLOR="#0000ff">"true"</font><font COLOR="#000000">
  <font color="#800000">title</font>=</font><font COLOR="#0000ff">"popup title"</font><font COLOR="#000000"> <font color="#800000">target</font>=</font><font COLOR="#0000ff">"_my_custom_target_window"</font><font COLOR="#000000">
  <font color="#800000">toolbar</font>=</font><font COLOR="#0000ff">"true"</font><font COLOR="#000000">  <font color="#800000">menubar</font>=</font><font COLOR="#0000ff">"false"</font><font COLOR="#000000"> <font color="#800000">personalbar</font>=</font><font COLOR="#0000ff">"true"</font><font COLOR="#000000"> <font color="#800000">scrollbars</font>=</font><font COLOR="#0000ff">"false"</font><font COLOR="#000000">
  <font color="#800000">width</font>=</font><font COLOR="#0000ff">"250"</font><font COLOR="#000000"> <font color="#800000">height</font>=</font><font COLOR="#0000ff">"180"</font><font COLOR="#000000">/&gt;
</font>

</PRE>
</pre>
<hr />

</sakai:view>
</f:view>
