<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
	PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<f:view>
<html xmlns="http://www.w3.org/1999/xhtml">
<head><%= request.getAttribute("html.head") %>
<title><h:outputText value="#{questionPoolMessages.t_previewPool}"/></title>
<!-- stylesheet and script widgets -->
<samigo:stylesheet path="/css/imageQuestion.author.css"/>
<samigo:script path="/js/selection.author.preview.js"/>
<script type="text/JavaScript">	
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
</script>
</head>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">

<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

<h3 class="insColor insBak insBor">
<h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.displayNameNotCPool}" rendered="#{questionpool.notCurrentPool}"/>
<h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.currentPool.displayName}" rendered="#{!questionpool.notCurrentPool}"/>
</h3>

<div class="tier1">
<h:form id="previewPool">
<p class="navViewAction">
<h:commandLink title="#{questionPoolMessages.t_export}" rendered="#{questionpool.importToAuthoring != 'true'}" id="exportlink" immediate="true" action="#{questionpool.exportPool}">
	<h:outputText id="exportq" value="#{questionPoolMessages.export}"/>
	<f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" #{questionPoolMessages.separator} " />
<h:outputLink id="printlink" rendered="#{questionpool.importToAuthoring != 'true'}" onclick="print(); return false;" title="#{questionPoolMessages.t_print}" >
	<h:outputText id="printq" value="#{questionPoolMessages.print}"/>
</h:outputLink>
</p>
</h:form>
</div>


<h:dataTable id="parts" width="100%"
	value="#{questionpool.itemsBean}" var="question" >

	<h:column>
		<h:panelGrid columns="1" width="100%" styleClass="table table-condensed" columnClasses="navView,navList">
			<h:panelGroup>
				<h:outputText value="#{authorMessages.q} " />
				<h:outputText value="#{question.number}: " />

				<h:panelGroup >
					<h:outputText rendered="#{question.itemData.typeId== 1}" value=" #{commonMessages.multiple_choice_sin}"/>
					<h:outputText rendered="#{question.itemData.typeId== 2}" value=" #{commonMessages.multipl_mc_ms}"/>
					<h:outputText rendered="#{question.itemData.typeId== 12}" value=" #{commonMessages.multipl_mc_ss}"/>
					<h:outputText rendered="#{question.itemData.typeId== 13}" value=" #{authorMessages.matrix_choice_surv}"/>
					<h:outputText rendered="#{question.itemData.typeId== 3}" value=" #{authorMessages.multiple_choice_surv}"/>
					<h:outputText rendered="#{question.itemData.typeId== 4}" value=" #{authorMessages.true_false}"/>
					<h:outputText rendered="#{question.itemData.typeId== 5}" value=" #{authorMessages.short_answer_essay}"/>
					<h:outputText rendered="#{question.itemData.typeId== 8}" value=" #{authorMessages.fill_in_the_blank}"/>
					<h:outputText rendered="#{question.itemData.typeId== 11}" value=" #{authorMessages.fill_in_numeric}"/>
					<h:outputText rendered="#{question.itemData.typeId== 9}" value=" #{authorMessages.matching}"/>
					<h:outputText rendered="#{question.itemData.typeId== 7}" value=" #{authorMessages.audio_recording}"/>
					<h:outputText rendered="#{question.itemData.typeId== 6}" value=" #{authorMessages.file_upload}"/>
					<h:outputText rendered="#{question.itemData.typeId== 14}" value=" #{authorMessages.extended_matching_items}"/>
					<h:outputText rendered="#{question.itemData.typeId== 15}" value=" #{authorMessages.calculated_question}"/><!-- CALCULATED_QUESTION -->
					<h:outputText rendered="#{question.itemData.typeId== 16}" value=" #{authorMessages.image_map_question}"/><!-- IMAGEMAP_QUESTION -->

					<h:outputText value=" #{authorMessages.dash} " />
					
					<h:inputText rendered="#{question.itemData.typeId!= 3}" id="answerptr" value="#{question.updatedScore}" disabled="true" size="6" >
						<f:validateDoubleRange minimum="0.00"/>
					</h:inputText>
		
					<h:outputText rendered="#{question.itemData.typeId== 3}" value="#{question.updatedScore}"/>
						<h:outputText rendered="#{question.itemData.score > 1}" value=" #{authorMessages.points_lower_case}"/>
						<h:outputText rendered="#{question.itemData.score == 1}" value=" #{authorMessages.point_lower_case}"/>
						<h:outputText rendered="#{question.itemData.score == 0}" value=" #{authorMessages.points_lower_case}"/>
					</h:panelGroup>
				</h:panelGroup>
		</h:panelGrid>

		<f:verbatim><div class="samigo-question-callout"></f:verbatim>
			<h:panelGroup rendered="#{question.itemData.typeId == 11}">
				<%@ include file="/jsf/author/preview_item/FillInNumeric.jsp" %>
			</h:panelGroup>
			<h:panelGroup rendered="#{question.itemData.typeId == 9}">
				<%@ include file="/jsf/author/preview_item/Matching.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 8}">
				<%@ include file="/jsf/author/preview_item/FillInTheBlank.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 7}">
				<%@ include file="/jsf/author/preview_item/AudioRecording.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 6}">
				<%@ include file="/jsf/author/preview_item/FileUpload.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 5}">
				<%@ include file="/jsf/author/preview_item/ShortAnswer.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 4}">
				<%@ include file="/jsf/author/preview_item/TrueFalse.jsp" %>
			</h:panelGroup>

			<!-- same as multiple choice single -->
			<h:panelGroup rendered="#{question.itemData.typeId == 3}">
				<%@ include file="/jsf/author/preview_item/MultipleChoiceSurvey.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 2}">
				<%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 1}">
				<%@ include file="/jsf/author/preview_item/MultipleChoiceSingleCorrect.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 12}">
				<%@ include file="/jsf/author/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
			</h:panelGroup>
			<h:panelGroup rendered="#{question.itemData.typeId == 13}">
				<%@ include file="/jsf/author/preview_item/MatrixChoicesSurvey.jsp" %>
			</h:panelGroup>

			<h:panelGroup rendered="#{question.itemData.typeId == 14}">
				<%@ include file="/jsf/author/preview_item/ExtendedMatchingItems.jsp" %>
			</h:panelGroup>
			
			<h:panelGroup rendered="#{question.itemData.typeId == 15}">
				<%@ include file="/jsf/author/preview_item/CalculatedQuestion.jsp" %>
			</h:panelGroup>
			
			<h:panelGroup rendered="#{question.itemData.typeId == 16}">
				<%@ include file="/jsf/author/preview_item/ImageMapQuestion.jsp" %>
			</h:panelGroup>
		<f:verbatim></div></f:verbatim>

	</h:column>
</h:dataTable>

<h:form id="previewPool_cancel">
<p class="act">

<h:commandButton type="submit" immediate="true" id="cancel" value="#{questionPoolMessages.preview_pool_return}" action="#{questionpool.editPool}" />

</p>

</h:form>
</div>
</body>
</html>
</f:view>