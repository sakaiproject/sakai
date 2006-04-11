<%-- $Id: inputColor.jsp,v 1.5 2005/05/28 20:16:18 ggolden.umich.edu Exp $ --%>
<f:view>
<sakai:view title="inputColor tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>

<sakai:script contextBase="/sakai-jsf-resource/" path="/inputColor/inputColor.js"/>

<hr />
<h2>inputColor example</h2>
<hr />

<h:form id="colorForm">
  Pick color:
  <sakai:inputColor value="" id="myInputColorId"/>
  <h:commandButton type="submit" id="myButtonId" value="Submit"/>
</h:form>

<hr />
<h3>inputColor usage:</h3>

<pre>
</font>&lt;<font COLOR="#800080"><B>h:form</B></font><font COLOR="#000000"> </font><font COLOR="#800000">id</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"colorForm"</font><font COLOR="#000000">&gt;
    Pick color:
    &lt;</font><font COLOR="#800080"><B>sakai:inputColor</B></font><font COLOR="#000000"> </font><font COLOR="#800000">value</font><font COLOR="#000000">=</font><font COLOR="#0000ff">""</font><font COLOR="#000000"> </font><font COLOR="#800000">size</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"10"</font><font COLOR="#000000"> </font><font COLOR="#800000">id</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"myInputColorId"</font><font COLOR="#000000">/&gt;
    &lt;</font><font COLOR="#800080"><B>h:commandButton</B></font><font COLOR="#000000"> </font><font COLOR="#800000">type</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"submit"</font><font COLOR="#000000"> </font><font COLOR="#800000">id</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"myButtonId"</font><font COLOR="#000000"> </font><font COLOR="#800000">value</font><font COLOR="#000000">=</font><font COLOR="#0000ff">"Submit"</font><font COLOR="#000000"> </font>/&gt;
&lt;</font><font COLOR="#800080"><B>/h:form</B></font><font COLOR="#000000">&gt;</font>
</pre>
<hr />


</sakai:view>
</f:view>
