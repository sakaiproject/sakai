<?xml version="1.0" encoding="UTF-8" ?>
<jsph:root xmlns:jsph="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:ch="http://java.sun.com/jsp/jstl/core"
  >
  <jsph:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <jsph:scriptlet>
  	String hss = (String)request.getAttribute("headerScriptSource");
  	if ( hss != null &amp;&amp; hss.trim().length() &gt; 0 ) {
  </jsph:scriptlet>
  <script  type="text/javascript" src="${requestScope.headerScriptSource}"> <!-- don't reduce this! --></script>
  <jsph:scriptlet>
  	}
  </jsph:scriptlet>
<script type="text/javascript" >
var placementid = "Main<jsph:expression>request.getAttribute("sakai.tool.placement.id").toString().replace('-','x')</jsph:expression>";
</script>  
</jsph:root>
