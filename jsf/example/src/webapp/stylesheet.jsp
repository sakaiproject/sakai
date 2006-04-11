<%-- $Id: stylesheet.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="stylesheet tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>stylesheet example</h2>
<hr />

<sakai:stylesheet path="/css/sakai.css"/>

<hr />
<h3>stylesheet usage:</h3>

<pre>
      &lt;</font><font COLOR="#800080"><B>sakai:stylesheet</B></font><font COLOR="#000000"> </font><font COLOR="#800000">path</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"/myPath/css/mycss.css"</font><font COLOR="#000000">/&gt;</font>
</pre>
<hr />

</sakai:view>
</f:view>


