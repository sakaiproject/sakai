<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression>request.getAttribute("html.head")</jsp:expression>
        <title>
          <h:outputText value="#{authorMessages.example_emi_question}" escape="false"/>
        </title>
        <script>
          <jsp:text>
	$(document).ready(function() {
		$('#closeBut').click(function() {
			window.close();
		});
	});
</jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression>request.getAttribute("html.body.onload")</jsp:expression></jsp:attribute>
        <jsp:body>
          <h3 align="center">
            <h:outputText value="#{authorMessages.example_emi_question}" escape="false"/>
          </h3>
          <br/>
          <table width="95%" align="center" border="0" cellspacing="5" class="act greyBox">
            <jsp:element name="tr">
              <jsp:attribute name="bgcolor" trim="false"><jsp:expression>"point".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"</jsp:expression></jsp:attribute>
              <jsp:body>
                <td width="30%" align="center" valign="top">
                  <b>
                    <a name="point">
                      <h:outputText value="#{authorMessages.emi_whats_answer_point_title}" escape="false"/>
                    </a>
                  </b>
                </td>
                <td>
                  <h:outputText value="#{authorMessages.emi_whats_answer_point}" escape="false"/>
                </td>
              </jsp:body>
            </jsp:element>
            <jsp:element name="tr">
              <jsp:attribute name="bgcolor" trim="false"><jsp:expression>"theme".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"</jsp:expression></jsp:attribute>
              <jsp:body>
                <td align="center" valign="top">
                  <b>
                    <a name="theme">
                      <h:outputText value="#{authorMessages.emi_whats_theme_title}" escape="false"/>
                    </a>
                  </b>
                </td>
                <td>
                  <h:outputText value="#{authorMessages.emi_whats_theme}" escape="false"/>
                </td>
              </jsp:body>
            </jsp:element>
            <jsp:element name="tr">
              <jsp:attribute name="bgcolor" trim="false"><jsp:expression>"options".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"</jsp:expression></jsp:attribute>
              <jsp:body>
                <td align="center" valign="top">
                  <b>
                    <a name="options">
                      <h:outputText value="#{authorMessages.emi_whats_options_title}" escape="false"/>
                    </a>
                  </b>
                </td>
                <td>
                  <h:outputText value="#{authorMessages.emi_whats_options}" escape="false"/>
                </td>
              </jsp:body>
            </jsp:element>
            <jsp:element name="tr">
              <jsp:attribute name="bgcolor" trim="false"><jsp:expression>"leadin".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"</jsp:expression></jsp:attribute>
              <jsp:body>
                <td align="center" valign="top">
                  <b>
                    <a name="leadin">
                      <h:outputText value="#{authorMessages.emi_whats_leadin_title}" escape="false"/>
                    </a>
                  </b>
                </td>
                <td>
                  <h:outputText value="#{authorMessages.emi_whats_leadin}" escape="false"/>
                </td>
              </jsp:body>
            </jsp:element>
            <jsp:element name="tr">
              <jsp:attribute name="bgcolor" trim="false"><jsp:expression>"items".equals(request.getParameter("item"))
						? "lightgrey"
						: "white"</jsp:expression></jsp:attribute>
              <jsp:body>
                <td align="center" valign="top">
                  <b>
                    <a name="items">
                      <h:outputText value="#{authorMessages.emi_whats_items_title}" escape="false"/>
                    </a>
                  </b>
                </td>
                <td>
                  <h:outputText value="#{authorMessages.emi_whats_items}" escape="false"/>
                </td>
              </jsp:body>
            </jsp:element>
          </table>
          <br/>
          <div align="center">
            <h:commandButton id="closeBut" value="#{authorMessages.button_close}" styleClass="active"/>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
