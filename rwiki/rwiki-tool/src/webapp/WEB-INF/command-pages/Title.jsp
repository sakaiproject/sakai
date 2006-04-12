<?xml version="1.0" encoding="UTF-8" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  >
  <jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
  <jsp:text><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
      <title>Sakai RWiki</title>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload">
	<jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep, ignoreCourier); callAllLoaders();
      </jsp:attribute>
      <table border="0" cellpadding="0" cellspacing="0" width="100%" class="toolTitle" summary="layout">
	<tr>
	  <td class="title">
	    RWiki			
	  </td>
	  <td class="action" id="pageName">
	  </td>
	</tr>
      </table>
    </jsp:element>
  </html>
</jsp:root>
