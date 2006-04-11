<%-- $Id: selectCommand.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="selectCommand tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<!--
todo: define bean and listenmrs method
-->

<hr />
<h2>selectCommand example</h2>
<hr />
<f:view>
<h:form id="selectCommandForm">
<h:outputText value="Pick an action"/>
  <sakai:selectCommand id="mySelectCommand"
     title="Pick an action" actionListener="examplebean.handleAction"
     value="#{itemauthor.itemType}" required="true">
    <f:selectItem itemLabel="Do 1" itemValue="action1"/>
    <f:selectItem itemLabel="Do 2" itemValue="action2"/>
    <f:selectItem itemLabel="Do 3" itemValue="action3"/>
    <f:selectItem itemLabel="Do 4" itemValue="action4"/>
  </sakai:selectCommand>
</h:form>
<hr />
<h3>selectCommand usage:</h3>
<pre>
<font color="#000000">&lt;</font><font color="#800080">h:outputText</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"Pick an action"</font><font color="#000000">/&gt;
&lt;</font><font color="#800080">sakai:selectCommand</font><font color="#800000"> id</font><font color="#000000">=</font><font color="#0000ff">"mySelectCommand"</font>
     <font color="#800000">title</font><font color="#000000">=</font><font color="#0000ff">"Pick an action"</font> <font color="#800000">actionListener</font><font color="#000000">=</font><font color="#0000ff">"examplebean.handleSelectCommand"</font>
    <font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"#{itemauthor.itemType}"</font><font color="#000000"> </font><font color="#800000">required</font><font color="#000000">=</font><font color="#0000ff">"true"</font><font color="#000000">&gt;
  &lt;</font><font color="#800080">f:selectItem</font><font color="#000000"> </font><font color="#800000">itemLabel</font><font color="#000000">=</font><font color="#0000ff">"Do 1"</font><font color="#000000"> </font><font color="#800000">itemValue</font><font color="#000000">=</font><font color="#0000ff">"action1"</font><font color="#000000">/&gt;
  &lt;</font><font color="#800080">f:selectItem</font><font color="#000000"> </font><font color="#800000">itemLabel</font><font color="#000000">=</font><font color="#0000ff">"Do 2"</font><font color="#000000"> </font><font color="#800000">itemValue</font><font color="#000000">=</font><font color="#0000ff">"action2"</font><font color="#000000">/&gt;
  &lt;</font><font color="#800080">f:selectItem</font><font color="#000000"> </font><font color="#800000">itemLabel</font><font color="#000000">=</font><font color="#0000ff">"Do 3"</font><font color="#000000"> </font><font color="#800000">itemValue</font><font color="#000000">=</font><font color="#0000ff">"action3"</font><font color="#000000">/&gt;
  &lt;</font><font color="#800080">f:selectItem</font><font color="#000000"> </font><font color="#800000">itemLabel</font><font color="#000000">=</font><font color="#0000ff">"Do 4"</font><font color="#000000"> </font><font color="#800000">itemValue</font><font color="#000000">=</font><font color="#0000ff">"action4"</font><font color="#000000">/&gt;
&lt;</font><font color="#800080">/sakai:selectCommand</font><font color="#000000">&gt;
</font>
</pre>
<hr />

</sakai:view>
</f:view>
