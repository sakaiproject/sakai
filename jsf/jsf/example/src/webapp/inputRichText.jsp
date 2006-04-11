<%-- $Id: inputRichText.jsp,v 1.8 2005/05/09 19:22:38 esmiley.stanford.edu Exp $ --%>
<f:view>
<sakai:view title="inputRichText tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>inputRichText example</h2>
<hr />

<h:form id="inputRichTextForm">
 <h:panelGrid id="inputRichTextEditorTable"columns="2" border="1">
    <sakai:inputRichText rows="3" cols="60" buttonSet="small" value="#{simpleprops.prop1}" />
    <sakai:inputRichText rows="3" cols="60" buttonSet="small" value="#{simpleprops.prop2}" />
    <sakai:inputRichText rows="5" cols="60" buttonSet="medium" value="#{simpleprops.prop3}" />
    <sakai:inputRichText rows="5" cols="60" buttonSet="medium" value="#{simpleprops.prop4}." />
    <sakai:inputRichText rows="7" cols="60" buttonSet="large" value="#{simpleprops.prop5}" />
    <sakai:inputRichText rows="7" cols="60" buttonSet="large" value="#{simpleprops.prop6}" />
  </h:panelGrid>
  <h:commandButton type="submit" id="myButtonId" value="Submit"/>
</h:form>

<hr />
<h3>inputRichText usage:</h3>
<pre>
<font color="#000000"> &lt;</font><font color="#800080">h:panelGrid</font><font color="#000000"> </font><font color="#800000">id</font><font color="#000000">=</font><font color="#0000ff">"inputRichTextEditorTable"</font><font color="#800000">columns</font><font color="#000000">=</font><font color="#0000ff">"2"</font><font color="#000000"> </font><font color="#800000">border</font><font color="#000000">=</font><font color="#0000ff">"1"</font><font color="#000000">&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"10"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"small"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"10"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"small"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"12"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"medium"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"12"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"medium"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"15"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"large"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
  &lt;</font><font color="#800080">sakai:inputRichText</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"15"</font><font color="#000000"> <font color="#800000">cols</font>=</font><font color="#0000ff">"60"</font><font color="#000000"> <font color="#800000">buttonSet</font>=</font><font color="#0000ff">"large"</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"This is a JSF test."</font><font color="#000000"> /&gt;
&lt;</font><font color="#800080">/h:panelGrid</font><font color="#000000">&gt;
</font>
</pre>
<hr />

</sakai:view>
</f:view>
