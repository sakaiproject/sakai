<%-- $Id: script.jsp,v 1.5 2005/05/28 20:16:18 ggolden.umich.edu Exp $ --%>
<f:view>
<sakai:view title="script tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>script example</h2>
<hr />

<sakai:script path="/sakai-jsf-resource/inputDate/inputDate.js"/>

<hr />
<h3>script usage:</h3>

<pre>
<pre>
      &lt;</font><font COLOR="#800080"><B>sakai:script</B></font><font COLOR="#000000"> </font><font COLOR="#800000">path</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"/myPath/js/myscript.js"</font><font COLOR="#000000">/&gt;</font>
</pre></pre>
<hr />

</sakai:view>
</f:view>
