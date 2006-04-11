<%-- $Id: example_template.jsp,v 1.5 2005/05/28 20:16:18 ggolden.umich.edu Exp $ --%>
<f:view>
<sakai:view title="xxx tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<p>
this is a template for setting up the example pages
for a hypothetical xxx widget
they are full html pages, so they can stand alone,
they have a simple example, with whatever additional tags are needed
followed by pure html usage documentation.
</p>

<hr />
<h2>xxx example</h2>
<hr />
<sakai:script path="/sakai-jsf-resource/xxx/xxx.js"/>
<h:form id="xxxForm">
  <sakai:xxx value="" size="10" id="myxxxId"/>
  <h:commandButton type="submit" id="myButtonId" value="Submit"/>
<hr />
<h3>xxx usage:</h3>

<pre>
HTML USAGE GOES HERE
</pre>
<hr />
</h:form>
</sakai:view>
</f:view>

