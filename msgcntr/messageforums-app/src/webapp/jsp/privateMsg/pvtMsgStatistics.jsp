<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="Received Private Messages">
	<sakai:view_content>
		<h:form id="pvtMsgStatistics">
<!--jsp/privateMsg/pvtMsgStatistics.jsp-->
<hr />
<h2>Private Messgae- Statistics</h2>
<hr />

<h:form id="timerBarForm">
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
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

        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processPvtMsgCancel}" value="Cancel" />
        </sakai:button_bar>         
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>

