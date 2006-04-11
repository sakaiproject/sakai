<%-- $Id: progressBar.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="progressBar tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>progressBar example</h2>
<hr />
  <h:outputText value="Please stand by. (progress bar test)"/>
  <sakai:progressBar  wait="15" />
<hr />
<h3>progressBar usage:</h3>

<pre>
<FONT COLOR="#000000">  &lt;</FONT><FONT COLOR="#800080">h:outputText</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">value</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"progress bar test."</FONT><FONT COLOR="#000000">/&gt;
  &lt;</FONT><FONT COLOR="#800080">sakai:progressBar</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">wait</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"10"</FONT><FONT COLOR="#000000"> </FONT></FONT><FONT COLOR="#000000"> /&gt;
</FONT>
</pre>
<hr />

</sakai:view>
</f:view>
