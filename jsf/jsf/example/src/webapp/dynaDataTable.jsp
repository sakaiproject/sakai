<%-- $Id: dynaDataTable.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="dynaDataTable tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>dynaDataTable example</h2>
<hr />

<%-- todo: set up backing bean --%>
<h:form id="demoDataLine">
 <sakai:dynaDataTable value="#{examplebean.list}" var="item" separator=" | "
    first="0" rows="25">
   <h:column>
    <h:outputText value="#{item.last}, #{item.first}" />
    <h:commandLink action="#{item.getAction}" immediate="true">
      <h:outputText value="#{item.text}" />
    </h:commandLink>
   </h:column>
   <h:multiColumn value="#{item.grade}" var="grade">
   <h:column>
    <h:outputText value="#{grade.name}" />
    <h:outputText value="#{grade.score}" />
   </h:column>
   </h:multiColumn>
 </sakai:dynaDataTable>
<hr />
<h3>dynaDataTable usage:</h3>
<%--TODO: fix up --%>
<pre>
&lt;</font><font color="#800080">sakai:dynaDataTable</font><font color="#000000">
</font><font color="#800000">   value</font><font color="#000000">=</font><font color="#0000ff">"#{testlinks.links}"</font><font color="#000000"> </font><font color="#800000">separator</font><font color="#000000">=</font><font color="#0000ff">" | "</font>
<font color="#000000"> </font><font color="#800000">  var</font><font color="#000000">=</font><font color="#0000ff">"link"</font><font color="#000000"> </font><font color="#800000">first</font><font color="#000000">=</font><font color="#0000ff">"0"</font><font color="#000000"> </font><font color="#800000">rows</font><font color="#000000">=</font><font color="#0000ff">"100"</font><font color="#000000">&gt;
  &lt;</font><font color="#800080">h:column</font><font color="#000000">&gt;
    </font>&lt;</font><font color="#800080">h:commandLink</font><font color="#000000"> </font><font color="#800000">action</font><font color="#000000">=</font><font color="#0000ff">"#{link.getAction}"</font><font color="#000000"> </font><font color="#800000">immediate</font><font color="#000000">=</font><font color="#0000ff">"true"</font><font color="#000000">&gt;
      &lt;</font><font color="#800080">h:outputText</font><font color="#000000"> </font><font color="#800000">value</font><font color="#000000">=</font><font color="#0000ff">"#{link.text}"</font><font color="#000000"> /&gt;
    &lt;</font><font color="#800080">/h:commandLink</font><font color="#000000">&gt;
  &lt;</font><font color="#800080">/h:column</font><font color="#000000">&gt;
&lt;</font><font color="#800080">/sakai:dynaDataTable</font><font color="#000000">&gt;</font>
</pre>
<hr />
</h:form>

</sakai:view>
</f:view>
