<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: test_dataline.jsp,v 1.4 2004/11/29 19:42:40 esmiley.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
      <title>TEST</title>
      <samigo:stylesheet path="/css/main.css"/>
      </head>
      <body>
<!-- content... -->
<h:outputText value=" Test of dataLine TAG " />
<h:form id="fred">
Usage:<br />
<code>
<pre>
&lt;</FONT><FONT COLOR="#800080">samigo:dataLine</FONT><FONT COLOR="#000000">
</FONT><FONT COLOR="#800000">   value</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"#{testlinks.links}"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">separator</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">" | "</FONT>
<FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">  var</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"link"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">first</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"0"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">rows</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"100"</FONT><FONT COLOR="#000000">&gt;
  &lt;</FONT><FONT COLOR="#800080">h:column</FONT><FONT COLOR="#000000">&gt;
    </FONT>&lt;</FONT><FONT COLOR="#800080">h:commandLink</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">action</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"#{link.getAction}"</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">immediate</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"true"</FONT><FONT COLOR="#000000">&gt;
      &lt;</FONT><FONT COLOR="#800080">h:outputText</FONT><FONT COLOR="#000000"> </FONT><FONT COLOR="#800000">value</FONT><FONT COLOR="#000000">=</FONT><FONT COLOR="#0000ff">"#{link.text}"</FONT><FONT COLOR="#000000"> /&gt;
    &lt;</FONT><FONT COLOR="#800080">/h:commandLink</FONT><FONT COLOR="#000000">&gt;
  &lt;</FONT><FONT COLOR="#800080">/h:column</FONT><FONT COLOR="#000000">&gt;
&lt;</FONT><FONT COLOR="#800080">/samigo:dataLine</FONT><FONT COLOR="#000000">&gt;
</FONT>

</pre>
</code>
<hr />
<samigo:dataLine value="#{testlinks.links}" var="link" separator=" | "
  first="0" rows="100">
  <h:column>
    <h:commandLink action="#{link.getAction}" immediate="true">
      <h:outputText value="#{link.text}" />
    </h:commandLink>
  </h:column>
</samigo:dataLine>
<hr />

</h:form>

<!-- end content -->
      </body>
    </html>
  </f:view>
