<%-- $Id: inputDate.jsp,v 1.14 2005/05/28 20:16:18 ggolden.umich.edu Exp $ --%>
<f:view>
<sakai:view title="inputDate tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>




<%-- <sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/inputDate.js"/> --%>
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar1.js"/>
<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar2.js"/>

<hr />
<h2>inputDate example</h2>
<hr />
<h:form id="dateForm">
  Pick date and time:
  <sakai:inputDate value="#{simpleprops.date1}" id="myInputDateId1"
    showDate="true" showTime="true"></sakai:inputDate><br />
  Pick date:
  <sakai:inputDate value="#{simpleprops.date2}" id="myInputDateId2"
    showDate="true" showTime="false"></sakai:inputDate><br />
  Pick time:
  <sakai:inputDate value="#{simpleprops.date3}" id="myInputDateId3"
    showDate="false" showTime="true"></sakai:inputDate>
  <h:commandButton type="submit" id="myButtonId" value="Submit"/>
</h:form>

<hr />
<h3>inputDate usage:</h3>

<pre>
<FONT COLOR="#000000">&lt;h:form id=</FONT><FONT COLOR="#0000ff">"dateForm"</FONT><FONT COLOR="#000000">&gt;
  Pick date:
  &lt;sakai:inputDate value=</FONT><FONT COLOR="#0000ff">""</FONT><FONT COLOR="#000000">
    showCalendar=</FONT><FONT COLOR="#0000ff">"true"</FONT><FONT COLOR="#000000"> showDate=</FONT><FONT COLOR="#0000ff">"true"</FONT><FONT COLOR="#000000"> showTime=</FONT><FONT COLOR="#0000ff">"true"</FONT><FONT COLOR="#000000"> showSeconds=</FONT><FONT COLOR="#0000ff">"true"</FONT><FONT COLOR="#000000">
    title=</FONT><FONT COLOR="#0000ff">"inputDate example"</FONT><FONT COLOR="#000000"> id=</FONT><FONT COLOR="#0000ff">"myInputDateId"</FONT><FONT COLOR="#000000">/&gt;
  &lt;h:commandButton type=</FONT><FONT COLOR="#0000ff">"submit"</FONT><FONT COLOR="#000000"> id=</FONT><FONT COLOR="#0000ff">"myButtonId"</FONT><FONT COLOR="#000000"> value=</FONT><FONT COLOR="#0000ff">"Submit"</FONT><FONT COLOR="#000000">/&gt;
&lt;/h:form&gt;
</FONT>
</pre>
<hr />

</sakai:view>
</f:view>
