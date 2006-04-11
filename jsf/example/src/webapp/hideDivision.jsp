<%-- $Id: hideDivision.jsp,v 1.6 2005/05/28 20:16:18 ggolden.umich.edu Exp $ --%>
<f:view>
<sakai:view title="hideDivision tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>hideDivision example.  Click to hide/unhide</h2>
<hr />

<sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
<sakai:hideDivision title="Click Me" id="div1">
 <%-- this just provides an object to show/hide --%>
 <h:panelGrid columns="3" styleClass="indnt2">
  <h:outputText value="Column 1."/>
  <h:outputText value="Column 2."/>
  <h:outputText value="Column 3."/>
  <h:outputText value="Column 1."/>
  <h:outputText value="Column 2."/>
  <h:outputText value="Column 3."/>
  <h:outputText value="Column 1."/>
  <h:outputText value="Column 2."/>
  <h:outputText value="Column 3."/>
  <h:outputText value="Column 1."/>
  <h:outputText value="Column 2."/>
  <h:outputText value="Column 3."/>
  <h:outputText value="Column 1."/>
  <h:outputText value="Column 2."/>
  <h:outputText value="Column 3."/>
 </h:panelGrid>
</sakai:hideDivision >

<hr />
<h3>hideDivision usage:</h3>
<pre>
&lt;</font><font color="#800080"><b>sakai:hideDivision</b></font><font color="#000000"> </font><font color="#800000">title</font><font color="#000000">=</font><font color="#0000ff">"Hide Me"</font><font color="#000000"> </font><font color="#800000">id</font><font color="#000000">=</font><font color="#0000ff">"hd"</font><font color="#000000">&gt;
  &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"some text"</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080"><b>h:panelGrid</b></font><font color="#800000"> id</font><font color="#000000">=</font><font color="#0000ff">"example_data"</font><font color="#000000"> </font><font color="#800000">columns</font><font color="#000000">=</font><font color="#0000ff">"3"</font><font color="#000000"> </font><font color="#000000">&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 1."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 2."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 3."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 1."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 2."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 3."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 1."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 2."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 3."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 1."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 2."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 3."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 1."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 2."</font><font color="#000000">/&gt;
    &lt;</font><font color="#800080"><b>h:outputText</b></font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Column 3."</font><font color="#000000">/&gt;
  &lt;</font><font color="#800080"><b>/h:panelGrid</b></font><font color="#000000">&gt;
&lt;</font><font color="#800080"><b>/sakai:hideDivision</b></font>
</pre>
<hr />

</sakai:view>
</f:view>
