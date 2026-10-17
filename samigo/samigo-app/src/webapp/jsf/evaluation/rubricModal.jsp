<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <h:panelGroup rendered="#{questionScores.hasAssociatedRubric}">
    <jsp:text><![CDATA[
	<div id=']]></jsp:text>
    <h:outputText value="#{description.assessmentGradingId}"/>
    <jsp:text><![CDATA[-inputs'></div>
	<div id=']]></jsp:text>
    <h:outputText value="modal#{description.assessmentGradingId}.#{questionScores.itemId}"/>
    <jsp:text><![CDATA[' style="display:none;overflow:initial">
		]]></jsp:text>
    <h:panelGroup rendered="#{questionScores.associatedRubricType == '1'}">
      <jsp:text><![CDATA[
			<sakai-rubric-grading
				id=']]></jsp:text>
      <h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>
      <jsp:text>'
				site-id='</jsp:text>
      <h:outputText value="#{questionScores.siteId}"/>
      <jsp:text>'
				tool-id="sakai.samigo"
				entity-id='</jsp:text>
      <h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>
      <jsp:text>'
				evaluated-item-id='</jsp:text>
      <h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}"/>
      <jsp:text>'
				evaluated-item-owner-id='</jsp:text>
      <h:outputText value="#{description.idString}"/>
      <jsp:text>'
				item-id='</jsp:text>
      <h:outputText value="#{description.assessmentGradingId}"/>
      <jsp:text>'
				</jsp:text>
      <h:outputText value="enable-pdf-export" rendered="#{questionScores.enablePdfExport}"/>
      <jsp:text><![CDATA[
			>
			</sakai-rubric-grading>
		]]></jsp:text>
    </h:panelGroup>
    <h:panelGroup rendered="#{questionScores.associatedRubricType == '2'}">
      <jsp:text><![CDATA[
			<sakai-dynamic-rubric
				id=']]></jsp:text>
      <h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>
      <jsp:text>'
				grading-id='</jsp:text>
      <h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}"/>
      <jsp:text>'
				entity-id='</jsp:text>
      <h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>
      <jsp:text>'
				site-id='</jsp:text>
      <h:outputText value="#{questionScores.siteId}"/>
      <jsp:text>'
				evaluated-item-owner-id='</jsp:text>
      <h:outputText value="#{description.idString}"/>
      <jsp:text>'
				previous-grade='</jsp:text>
      <h:outputText value="#{description.roundedTotalAutoScore}"/>
      <jsp:text><![CDATA['
				origin='questionScore'>
			</sakai-dynamic-rubric>
		]]></jsp:text>
    </h:panelGroup>
    <jsp:text><![CDATA[
	</div>
]]></jsp:text>
  </h:panelGroup>
</jsp:root>
