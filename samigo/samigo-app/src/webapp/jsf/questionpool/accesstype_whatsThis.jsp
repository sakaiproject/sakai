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
  <!--
$Id: accesstype_whatsThis.jsp 6643 2012-03-20 19:38:07Z anueda@asic.upv.es $

***********************************************************************************
*
* Copyright (c) 2008 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.whatsThis_body}" escape="false"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <h2 class="western">
            <h:outputText value="#{questionPoolMessages.whatsThis_body}"/>
          </h2>
          <p align="justify" style="margin-bottom: 0.35cm">
            <h:outputText value="#{questionPoolMessages.whatsThis_p1}"/>
          </p>
          <p align="justify" style="margin-bottom: 0.35cm">
            <h:outputText value="#{questionPoolMessages.whatsThis_p2}"/>
          </p>
          <p align="justify" style="margin-bottom: 0.35cm">
            <h:outputText value="#{questionPoolMessages.whatsThis_p3}"/>
          </p>
          <ul>
            <li>
              <p align="justify" style="margin-bottom: 0.35cm">
                <i>
                  <b>
                    <h:outputText value="#{questionPoolMessages.whatsThis_admin}"/>
                  </b>
                </i>
                <h:outputText value="#{questionPoolMessages.whatsThis_admin_p1}"/>
              </p>
            </li>
            <li>
              <p align="justify" style="margin-bottom: 0.35cm">
                <i>
                  <b>
                    <h:outputText value="#{questionPoolMessages.whatsThis_rw}"/>
                  </b>
                </i>
                <h:outputText value="#{questionPoolMessages.whatsThis_rw_p1}"/>
              </p>
              <ul>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_rw_p2}"/>
                  </p>
                </li>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_rw_p3}"/>
                  </p>
                </li>
              </ul>
            </li>
            <li>
              <p align="justify" style="margin-bottom: 0.35cm">
                <i>
                  <b>
                    <h:outputText value="#{questionPoolMessages.whatsThis_up}"/>
                  </b>
                </i>
                <h:outputText value="#{questionPoolMessages.whatsThis_up_p1}"/>
              </p>
              <ul>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_up_p2}"/>
                  </p>
                </li>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_up_p3}"/>
                  </p>
                </li>
              </ul>
            </li>
            <li>
              <p style="margin-bottom: 0.35cm">
                <i>
                  <b>
                    <h:outputText value="#{questionPoolMessages.whatsThis_rd}"/>
                  </b>
                </i>
                <h:outputText value="#{questionPoolMessages.whatsThis_rd_p1}"/>
              </p>
              <ul>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_rd_p2}"/>
                  </p>
                </li>
                <li>
                  <p align="justify" style="margin-bottom: 0.35cm">
                    <h:outputText value="#{questionPoolMessages.whatsThis_rd_p3}"/>
                  </p>
                </li>
              </ul>
            </li>
          </ul>
          <p align="justify" style="margin-bottom: 0.35cm">
            <h:outputText value="#{questionPoolMessages.whatsThis_p4}"/>
          </p>
          <jsp:text><![CDATA[

            <input id="close" type="button" onkeypress="window.close();" onclick="window.close();" value="]]></jsp:text>
          <h:outputText value="#{authorMessages.button_close}"/>
          <jsp:text><![CDATA[" name="close" />
        ]]></jsp:text>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
