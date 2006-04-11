<%-- $Id: dynaTable.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="dynaTable tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>dynaTable, multiColumn example</h2>
<hr />
<h:form id="demoDynaTable">
<sakai:dynaTable value="#{examplebean.list}" var="item"
   first="25" rows="25">
   <h:column>
     <h:outputText value=" #{item.last}, #{item.first}  #{item.address}" />
   </h:column>
   <sakai:multiColumn value="#{item.grades}" var="grade">
   <h:column>
     <h:outputText value=" #{grade.name}: #{grade.score}: " />
   </h:column>
   </sakai:multiColumn>
 </sakai:dynaTable>
<hr />
<h3>dynaTable, multiColumn usage:</h3>

<pre>
  <font color="#000000">&lt;</font><font color="#000080">sakai:dynaTable</font><font color="#000000"> </font><font color="#800000"><I>value</I></font><font color="#000000">=</font><font color="#0000ff">"#{examplebean.list}"</font><font color="#000000"> </font><font color="#800000"><I>var</I></font><font color="#000000">=</font><font color="#0000ff">"item"</font><font color="#000000">
   </font><font color="#800000"><I>first</I></font><font color="#000000">=</font><font color="#0000ff">"10"</font><font color="#000000"> </font><font color="#800000"><I>rows</I></font><font color="#000000">=</font><font color="#0000ff">"5"</font><font color="#000000">&gt;
   &lt;</font><font color="#000080">h:column</font><font color="#000000">&gt;
     &lt;</font><font color="#000080">h:outputText</font><font color="#000000"> </font><font color="#800000"><I>value</I></font><font color="#000000">=</font><font color="#0000ff">" #{item.last}, #{item.first}  #{item.address}"</font><font color="#000000"> /&gt;
   &lt;/</font><font color="#000080">h:column</font><font color="#000000">&gt;
   &lt;</font><font color="#000080">sakai:multiColumn</font><font color="#000000"> </font><font color="#800000"><I>value</I></font><font color="#000000">=</font><font color="#0000ff">"#{item.grades}"</font><font color="#000000"> </font><font color="#800000"><I>var</I></font><font color="#000000">=</font><font color="#0000ff">"grade"</font><font color="#000000">&gt;
     &lt;</font><font color="#000080">h:column</font><font color="#000000">&gt;
       &lt;</font><font color="#000080">h:outputText</font><font color="#000000"> </font><font color="#800000"><I>value</I></font><font color="#000000">=</font><font color="#0000ff">" #{grade.name}: #{grade.score}: "</font><font color="#000000"> /&gt;
     &lt;/</font><font color="#000080">h:column</font><font color="#000000">&gt;
   &lt;/</font><font color="#000080">sakai:multiColumn</font><font color="#000000">&gt;
</font><font color="#000000">&lt;</font><font color="#000080">/sakai:dynaTable</font></font>
</pre>
<hr />
</h:form>
</sakai:view>
</f:view>

