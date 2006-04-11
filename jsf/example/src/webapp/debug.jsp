<%-- $Id: debug.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="debug tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>

<h:form id="theForm">
<hr />
<h2>debug tag example</h2>
<hr />

<pre>
    &lt;sakai:debug /&gt;
</pre>
    <sakai:debug /> 

 <br />

<pre>
    &lt;sakai:debug rendered="false" /&gt;
</pre>
    <sakai:debug rendered="false" />

 <br />

</h:form>
</sakai:view>
</f:view>
