<%-- $Id: applet.jsp,v 1.8 2005/05/04 23:35:57 esmiley.stanford.edu Exp $ --%>
<f:view>
<sakai:view title="applet tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>applet example</h2>
<hr />
<sakai:applet
  javaClass = "Clock.class"
  codebase="/example"
  width = "400"
  height = "250"
/>

<hr />
<h3>applet usage:</h3>

<pre>
 <font color="#000000">&lt;</font><font color="#800080">sakai:applet</font><font color="#000000">
  </font><font color="#800000">code</font><font color="#000000"> = </font><font color="#0000ff">"CapturePlaybackApplet.class"</font><font color="#000000">
  </font><font color="#800000">archive</font><font color="#000000"> = </font><font color="#0000ff">"CapturePlayback.jar"</font><font color="#000000">
  </font><font color="#800000">width</font><font color="#000000"> = </font><font color="#0000ff">"400"</font><font color="#000000">
  </font><font color="#800000">height</font><font color="#000000"> = </font><font color="#0000ff">"250"</font><font color="#000000">
  </font><font color="#800000">hspace</font><font color="#000000">=</font><font color="#0000ff">"2"</font><font color="#000000">
  </font><font color="#800000">vspace</font><font color="#000000">=</font><font color="#0000ff">"2"</font><font color="#000000">
  </font><font color="#800000">jreversion</font><font color="#000000">=</font><font color="#0000ff">"1.4"</font><font color="#000000">
  </font><font color="#800000">nspluginurl</font><font color="#000000">=</font><font color="#0000ff">"http://java.sun.com/products/plugin/1.4/plugin-install.html"</font><font color="#000000">
  </font><font color="#800000">iepluginurl</font><font color="#000000">=</font><font color="#0000ff">"http://java.sun.com/products/plugin/1.4/jinstall-14-win32.cab#Version=1,4,0,mn"</font><font color="#000000">&gt;
  </font><font color="#800000">paramList</font><font color="#000000">=</font><font color="#0000ff">"edible=false,frangible=true,data=ninety nine bottles of beer on the wall"</font><font color="#000000">
/&gt;
</font>
</pre>
<hr />
</sakai:view>
</f:view>



