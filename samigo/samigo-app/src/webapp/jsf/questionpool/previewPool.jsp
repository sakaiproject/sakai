<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:corejsf="http://java.sun.com/upload">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[



<!DOCTYPE html
	PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{questionPoolMessages.t_previewPool}"/>
        </title>
        <!-- stylesheet and script widgets  -->
        <link rel="stylesheet" type="text/css" href="/samigo-app/css/imageQuestion.author.css"/>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/selection.author.preview.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>	
	jQuery(window).load(function(){
			
		$('input:hidden[id*=hiddenSerializedCoords_]').each(function(){
			var myregexp = /hiddenSerializedCoords_(\d+)?_(\d+)_(\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = "_";
			if (matches[1] != undefined) {
				sequence = matches[1];
			}
			sequence = sequence + matches[2];
			var label = matches[3];
			
			var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'imageMapContainer_'+sequence);
			try {
				sel.setCoords(jQuery.parseJSON(this.value));
				sel.setText(label);
			}catch(err){}
			
		});	
	});
</jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!-- content...  -->
          <div class="portletBody">
            <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
            <h3 class="insColor insBak insBor">
              <h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.displayNameNotCPool}" rendered="#{questionpool.notCurrentPool eq true}"/>
              <h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.currentPool.displayName}" rendered="#{questionpool.notCurrentPool eq false}"/>
            </h3>
            <div class="tier1">
              <h:form id="previewPool">
                <p class="navViewAction">
                  <h:commandLink title="#{questionPoolMessages.t_exportPool}" rendered="#{questionpool.importToAuthoring eq false}" id="exportPool" immediate="true" action="#{questionpool.startExportPool}">
                    <h:outputText id="exportp" value="#{questionPoolMessages.t_exportPool}"/>
                    <f:param name="action" value="exportPool"/>
                    <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
                    <f:param name="outCome" value="previewPool"/>
                  </h:commandLink>
                  <h:outputText rendered="#{questionpool.importToAuthoring eq false}" value=" #{questionPoolMessages.separator} "/>
                  <h:commandLink title="#{questionPoolMessages.t_export}" rendered="#{questionpool.importToAuthoring eq false}" id="exportlink" immediate="true" action="#{questionpool.exportPool}">
                    <h:outputText id="exportq" value="#{questionPoolMessages.export}"/>
                    <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
                  </h:commandLink>
                  <h:outputText rendered="#{questionpool.importToAuthoring eq false}" value=" #{questionPoolMessages.separator} "/>
                  <h:outputLink id="printlink" rendered="#{questionpool.importToAuthoring eq false}" onclick="print(); return false;" title="#{questionPoolMessages.t_print}">
                    <h:outputText id="printq" value="#{questionPoolMessages.print}"/>
                  </h:outputLink>
                </p>
              </h:form>
            </div>
            <h:dataTable id="parts" width="100%" value="#{questionpool.itemsBean}" var="question">
              <h:column>
                <h:panelGrid columns="1" width="100%" styleClass="table table-condensed" columnClasses="navView,navList">
                  <h:panelGroup>
                    <h:outputText value="#{authorMessages.q} "/>
                    <h:outputText value="#{question.number}: "/>
                    <h:panelGroup>
                      <h:outputText rendered="#{question.itemData.typeId eq 1}" value=" #{commonMessages.multiple_choice_sin}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 2}" value=" #{commonMessages.multipl_mc_ms}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 12}" value=" #{commonMessages.multipl_mc_ss}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 13}" value=" #{authorMessages.matrix_choice_surv}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 3}" value=" #{authorMessages.multiple_choice_surv}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 4}" value=" #{authorMessages.true_false}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 5}" value=" #{authorMessages.short_answer_essay}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 8}" value=" #{authorMessages.fill_in_the_blank}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 11}" value=" #{authorMessages.fill_in_numeric}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 9}" value=" #{authorMessages.matching}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 7}" value=" #{authorMessages.audio_recording}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 6}" value=" #{authorMessages.file_upload}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 14}" value=" #{authorMessages.extended_matching_items}"/>
                      <h:outputText rendered="#{question.itemData.typeId eq 15}" value=" #{authorMessages.calculated_question}"/>
                      <!-- CALCULATED_QUESTION  -->
                      <h:outputText rendered="#{question.itemData.typeId eq 16}" value=" #{authorMessages.image_map_question}"/>
                      <!-- IMAGEMAP_QUESTION  -->
                      <h:outputText value=" #{authorMessages.dash} "/>
                      <h:inputText rendered="#{question.itemData.typeId!= 3}" id="answerptr" value="#{question.updatedScore}" disabled="true" size="6">
                        <f:validateDoubleRange minimum="0.00"/>
                      </h:inputText>
                      <h:outputText rendered="#{question.itemData.typeId eq 3}" value="#{question.updatedScore}"/>
                      <h:outputText rendered="#{question.itemData.score gt 1}" value=" #{authorMessages.points_lower_case}"/>
                      <h:outputText rendered="#{question.itemData.score eq 1}" value=" #{authorMessages.point_lower_case}"/>
                      <h:outputText rendered="#{question.itemData.score eq 0}" value=" #{authorMessages.points_lower_case}"/>
                    </h:panelGroup>
                  </h:panelGroup>
                </h:panelGrid>
                <f:verbatim>
                  <jsp:text><![CDATA[<div class="samigo-question-callout">]]></jsp:text>
                </f:verbatim>
                <h:panelGroup rendered="#{question.itemData.typeId eq 11}">
                  <jsp:directive.include file="/jsf/author/preview_item/FillInNumeric.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 9}">
                  <jsp:directive.include file="/jsf/author/preview_item/Matching.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 8}">
                  <jsp:directive.include file="/jsf/author/preview_item/FillInTheBlank.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 7}">
                  <jsp:directive.include file="/jsf/author/preview_item/AudioRecording.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 6}">
                  <jsp:directive.include file="/jsf/author/preview_item/FileUpload.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 5}">
                  <jsp:directive.include file="/jsf/author/preview_item/ShortAnswer.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 4}">
                  <jsp:directive.include file="/jsf/author/preview_item/TrueFalse.jsp"/>
                </h:panelGroup>
                <!-- same as multiple choice single  -->
                <h:panelGroup rendered="#{question.itemData.typeId eq 3}">
                  <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 2}">
                  <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 1}">
                  <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 12}">
                  <jsp:directive.include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 13}">
                  <jsp:directive.include file="/jsf/author/preview_item/MatrixChoicesSurvey.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 14}">
                  <jsp:directive.include file="/jsf/author/preview_item/ExtendedMatchingItems.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 15}">
                  <jsp:directive.include file="/jsf/author/preview_item/CalculatedQuestion.jsp"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId eq 16}">
                  <jsp:directive.include file="/jsf/author/preview_item/ImageMapQuestion.jsp"/>
                </h:panelGroup>
                <f:verbatim>
                  <jsp:text><![CDATA[</div>]]></jsp:text>
                </f:verbatim>
              </h:column>
            </h:dataTable>
            <h:form id="previewPool_cancel">
              <p class="act">
                <h:commandButton type="submit" immediate="true" id="cancel" value="#{questionPoolMessages.preview_pool_return}" action="#{questionpool.editPool}"/>
              </p>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
