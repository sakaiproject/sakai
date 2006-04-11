<%-- $Id: timerBar.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="timerBar tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>timerBar example</h2>
<hr />

<h:form id="timerBarForm">
  <sakai:timerBar height="15" width="300" wait="60" elapsed="30"
     expireMessage="this is the expire message"
     expireScript="var universe=42; alert('this is the expire script');"
     />
  <h:commandButton type="submit" id="myButtonId" value="Submit"/>
</h:form>

<hr />
<h3>timerBar usage:</h3>
<pre>

<FONT COLOR="#000000">  &lt;</FONT><FONT COLOR="#800080">h:outputText</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">value</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"Timer bar test."</FONT><FONT COLOR="#000000">/&gt;
  &lt;</FONT><FONT COLOR="#800080">sakai:timerBar</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">height</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"15"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">width</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"300"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">wait</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"60"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">elapsed</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"30"</FONT><FONT COLOR="#000000">
    </FONT><FONT COLOR="#800000">expireMessage</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"this is the expire message"</FONT><FONT COLOR="#000000">
    </FONT><FONT COLOR="#800000">expireScript</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"var universe=42; alert('this is the expire script');"</FONT><FONT COLOR="#000000"> /&gt;
</FONT>
</pre>
<hr />

</sakai:view>
</f:view>
