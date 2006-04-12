<?xml version="1.0" encoding="UTF-8" ?>
<jspf:root xmlns:jspf="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:cfooter="http://java.sun.com/jsp/jstl/core"
  >
  <jspf:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <script type="text/javascript" ><cfooter:out value="${requestScope.footerScript}" escapeXml="false" /></script>
  <jspf:scriptlet>
    {
    long endofpage = System.currentTimeMillis();
    uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("END Of Page:",endofpage,endofpage);
    }
  </jspf:scriptlet>
</jspf:root>
