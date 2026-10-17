<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <jsp:text><![CDATA[



<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
]]></jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <jsp:expression> request.getAttribute("html.head") </jsp:expression>
      <title>
        <h:outputText value="#{selectIndexMessages.page_title}"/>
      </title>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
      <jsp:body>
        <!-- IF A SECURE DELIVERY MODULES ARE AVAILABLE, INJECT THEIR INITIAL HTML FRAGMENTS HERE  -->
        <h:outputText value="#{select.secureDeliveryHTMLFragments}" escape="false"/>
        <!--JAVASCRIPT  -->
        <div class="portletBody container-fluid">
          <h:form id="selectIndexForm">
            <!-- SUBMISSIONS  -->
            <h2>
              <h:outputText value="#{selectIndexMessages.submissions_for_assessment} #{select.reviewAssessmentTitle}"/>
            </h2>
            <t:div rendered="#{select.isThereAssessmentToReview eq 'true'}" styleClass="panel panel-default sam-submittedPanel">
              <!-- Include REVIEW TABLE  -->
              <jsp:directive.include file="../select/selectIndex_review_table.jsp"/>
            </t:div>
          </h:form>
        </div>
        <!-- end content  -->
      </jsp:body>
    </jsp:element>
  </html>
</jsp:root>
