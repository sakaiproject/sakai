<%-- $Id: outputDate.jsp,v 1.9 2005/05/07 01:50:38 esmiley.stanford.edu Exp $ --%>
<f:view>
<sakai:view title="outputDate tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>outputDate example</h2>
<hr />
<h:outputText value="Full date and time: "/><sakai:outputDate id = "datetimesecs" value="#{simplebean.date1}"  showDate="true" showTime="true" showSeconds="true"/>
<br />
<h:outputText value="Full date and time w/o secs: "/><sakai:outputDate id = "datetime" value="#{simplebean.date2}"  showDate="true" showTime="true" showSeconds="false"/>
<br />
<h:outputText value="Time only: "/><sakai:outputDate id="time" value="#{simplebean.date3}"  showDate="false" showTime="true" showSeconds="true"/>
<hr />
<h3>outputDate usage:</h3>

<pre><span style='color:black'>&lt;sakai:outputDate value=</span><span
style='color:blue'>&quot;#{simplebean.date1}&quot;</span><span
style='color:black'><span style='mso-spacerun:yes'>  </span>showDate=</span><span
style='color:blue'>&quot;true&quot;</span><span style='color:black'> showTime=</span><span
style='color:blue'>&quot;true&quot;</span><span style='color:black'> showSeconds=</span><span
style='color:blue'>&quot;true&quot;</span><span style='color:black'>/&gt;<o:p></o:p></span></pre>
<hr />
</sakai:view>
</f:view>
