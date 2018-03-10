<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<f:view>
  <f:verbatim>
    <!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  </f:verbatim>
  
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
  <%= request.getAttribute("html.head") %>
  <title>
  <h:outputText value="Quiz: #{pdfAssessmentBean.title}" />
  </title>
  
  <samigo:stylesheet path="/css/print/print.css"/>
  
    
  <%@ include file="/jsf/delivery/deliveryjQuery.jsp" %>
  
	<samigo:script path="/js/selection.author.preview.js"/>
		
	<samigo:stylesheet path="/css/imageQuestion.author.css"/>
		
	<script type="text/JavaScript">		
		jQuery(window).load(function(){
			
			$('input:hidden[id^=hiddenSerializedCoords_]').each(function(){
				var myregexp = /hiddenSerializedCoords_(\d+_\d+)_(\d+)/
				var matches = myregexp.exec(this.id);
				var sequence = matches[1];
				var label = parseInt(matches[2])+1;
				
				var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'answerImageMapContainer_'+sequence);
				try {
					sel.setCoords(jQuery.parseJSON(this.value));
					sel.setText(label);
				}catch(err){}
				
			});	
		});
	</script>
	    
  
  <script type="text/JavaScript">
<!--
function resetSelectMenus(){
  var selectlist = document.getElementsByTagName("SELECT");

  for (var i = 0; i < selectlist.length; i++) {
        if ( selectlist[i].id.indexOf("changeQType") >=0){
          selectlist[i].value = "";
        }
  }
}

function clickInsertLink(field){
var insertlinkid= field.id.replace("changeQType", "hiddenlink");

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == insertlinkid)
  {
    newindex = i;
    break;
  }  
}
 
document.links[newindex].onclick(); 
}
 
//-->
</script>

<style type="text/css">
        .TableColumn {
          text-align: center
        }
       .TableClass {
         border-style: dotted;
         border-width: 0.5px;
         border-color: light grey;
       }
</style>
</head>




<body 
  onload="document.forms[0].reset(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>; qb_init('print');"
  id="qb_print"
  class="view_student">
    
  <!-- content... -->
  <!-- some back end stuff stubbed -->
  
  <h:form id="assessmentForm">
  
  <!-- HEADINGS (NOT PRINTED) -->
      <p>        
        <h:commandLink action="#{pdfAssessment.getActionString}">
          <h:outputText value="#{printMessages.back_to_assessmt}" rendered="#{pdfAssessment.actionString == 'editAssessment'}" escape="false" />
		  <h:outputText value="#{printMessages.back_to_landingpage}" rendered="#{pdfAssessment.actionString != 'editAssessment'}" escape="false" />
        </h:commandLink>
      </p>
      
    <h:messages/>
    
    <div id="header">
      <div class="navModeAction"> 
        
        <label>
          <h:selectBooleanCheckbox id="showKeys" value="#{printSettings.showKeys}" />
          <h:outputText value="#{printMessages.show_answer_key}" />
        </label>
        
        &nbsp;&nbsp;&nbsp;
        
        <label>
		  <h:selectBooleanCheckbox id="showFeedback" value="#{printSettings.showKeysFeedback}" />
		  <h:outputText value="#{printMessages.show_answer_feedback}" />
		</label>
        
        &nbsp;&nbsp;&nbsp;
        
		<label>
          <h:selectBooleanCheckbox id="showPartIntros" value="#{printSettings.showPartIntros}" />
          <h:outputText value="#{printMessages.show_intros_titles}" />
        </label>
        
        &nbsp;&nbsp;&nbsp;
        
        <label>
		  <h:selectBooleanCheckbox id="showSequence" value="#{printSettings.showSequence}" />
		  <h:outputText value="#{printMessages.show_answer_sequence}" />
		</label>
		      
		&nbsp;&nbsp;&nbsp;
        
        <div>
            <h:outputLabel for="fontSize" value="#{printMessages.font_size}:" />
            <h:selectOneMenu id="fontSize" value="#{printSettings.fontSize}">
                <f:selectItem itemLabel="#{printMessages.size_xsmall}" itemValue="1" />
                <f:selectItem itemLabel="#{printMessages.size_small}" itemValue="2" />
                <f:selectItem itemLabel="#{printMessages.size_medium}" itemValue="3" />
                <f:selectItem itemLabel="#{printMessages.size_large}" itemValue="4" />
                <f:selectItem itemLabel="#{printMessages.size_xlarge}" itemValue="5" />
            </h:selectOneMenu>
        </div>
        
        <br />
        
        <h:commandButton action="#{pdfAssessment.prepDocumentPDF}" value="#{printMessages.apply_settings}" styleClass="noActionButton" />
        <h:outputText value="<input type='button' onclick='print(); return false;' value='#{printMessages.print_html}' class='noActionButton' />" escape="false" />
        <h:commandButton action="#{pdfAssessment.getPDFAttachment}" value="#{printMessages.print_pdf}" styleClass="noActionButton" />
      </div>
    </div>
    <!-- END HEADINGS -->
    
    <h:outputText escape="false" value="<div id='quizWrapper' style='font-size: 10px;'>" rendered="#{printSettings.fontSize == '1'}" />
    <h:outputText escape="false" value="<div id='quizWrapper' style='font-size: 13px;'>" rendered="#{printSettings.fontSize == '2'}" />
    <h:outputText escape="false" value="<div id='quizWrapper' style='font-size: 16px;'>" rendered="#{printSettings.fontSize == '3'}" />
    <h:outputText escape="false" value="<div id='quizWrapper' style='font-size: 21px;'>" rendered="#{printSettings.fontSize == '4'}" />
    <h:outputText escape="false" value="<div id='quizWrapper' style='font-size: 26px;'>" rendered="#{printSettings.fontSize == '5'}" />
    
    <div id="assessmentForm:meta" class="assessment_meta">
      <p>
        <h:outputText value="#{printMessages.print_name_form}" />
        <br />
        <h:outputText value="#{printMessages.print_score_form}" />
        <br />
      </p>
    </div>
    
    <div id="assessmentForm:title" class="assessment_title">
	  <h:outputText value="#{pdfAssessment.title}" escape="false"/>
	</div>
	    
	<div class="assessment_intro, quiz">
	  <h:outputText id="assessmentIntro" value="#{delivery.instructorMessage}" 
	          escape="false" rendered="#{printSettings.showPartIntros && delivery.instructorMessage != null && delivery.instructorMessage != ''}" />
	</div>
    
    <h:panelGrid columns="2" border="0" rendered="#{printSettings.showPartIntros && delivery.hasAttachment}">
      <h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;" escape="false"/>
      <f:subview id="assessmentAttachment">
        <%@ include file="/jsf/delivery/assessment_attachment.jsp" %>
      </f:subview>
	</h:panelGrid>
      <h:dataTable id="parts" width="100%" value="#{pdfAssessment.deliveryParts}" var="part" border="0">
        <%-- note that part is ui/delivery/SectionContentsBean not ui/author/SectionBean --%>
        <h:column>
          <h:panelGroup id="fullTitle">
		    <h:panelGroup id="partIntro" rendered="#{pdfAssessment.sizeDeliveryParts >= 1}">
		      <h:panelGrid border="0">
		        <h:panelGroup>
		          <h:outputText id="number" value="#{authorMessages.p} #{part.number}" escape="false" styleClass="part_title_text" />
		          <h:outputText id="title" value=": #{part.title}" escape="false" styleClass="part_title" rendered="#{printSettings.showPartIntros && part.title ne 'Default' && part.title ne 'default'}"/>
		        </h:panelGroup>
		        <h:outputText value="&nbsp;" escape="false"/>
		        <h:outputText id="description" value="#{part.description}" escape="false" styleClass="part_info" rendered="#{printSettings.showPartIntros && part.description != null && part.description != ''}"/>
		      </h:panelGrid>
            </h:panelGroup>
          </h:panelGroup>
          
          <h:panelGrid columns="2" border="0" rendered="#{printSettings.showPartIntros && part.hasAttachment}">
            <h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;" escape="false"/>
            <f:subview id="partAttachment">
          	<%@ include file="/jsf/delivery/part_attachment.jsp" %>
          	</f:subview>
		  </h:panelGrid>
		  
          <!-- BEGIN ASSESSMENT PARTS & QUESTIONS -->
          <h:dataTable id="items" width="100%" headerClass="regHeading" value="#{part.itemContents}" var="question"
                                columnClasses="col-printQNum, col-printQues" rowClasses="item" border="0">           
            <h:column>
              <h:outputText value="<h3>" escape="false" />
                <h:outputText id="number" escape="false" value="#{question.sequence}" rendered="#{printSettings.showSequence}"/>
              <h:outputText value="</h3>" escape="false" />
            </h:column>
              
            <h:column>
              <h:panelGroup id="fullText">
                <h:outputText value="<div class='question type-#{question.itemData.typeId}'>" escape="false" />
                <h:panelGroup rendered="#{question.itemData.typeId == 9}">
                  <%@ include file="/jsf/print/preview_item/Matching.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 8}">
                  <%@ include file="/jsf/print/preview_item/FillInTheBlank.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 11}">
                  <%@ include file="/jsf/print/preview_item/FillInNumeric.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 7}">
                  <%@ include file="/jsf/print/preview_item/AudioRecording.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 6}">
                  <%@ include file="/jsf/print/preview_item/FileUpload.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 5}">
                  <%@ include file="/jsf/print/preview_item/ShortAnswer.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 4}">
                  <%@ include file="/jsf/print/preview_item/TrueFalse.jsp" %>
                </h:panelGroup>
                <!-- same as multiple choice single -->
                <h:panelGroup rendered="#{question.itemData.typeId == 3}">
                  <%@ include file="/jsf/print/preview_item/MultipleChoiceSurvey.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 2}">
                  <%@ include file="/jsf/print/preview_item/MultipleChoiceMultipleCorrect.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 1 || question.itemData.typeId == 12}">
                  <%@ include file="/jsf/print/preview_item/MultipleChoiceSingleCorrect.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 13}">
                  <%@ include file="/jsf/print/preview_item/MatrixChoicesSurvey.jsp" %>
                </h:panelGroup>
                <h:panelGroup rendered="#{question.itemData.typeId == 16}">
                  <%@ include file="/jsf/print/preview_item/ImageMapQuestion.jsp" %>
                </h:panelGroup>
				<h:panelGroup rendered="#{question.itemData.typeId == 14}">
		           <%@ include file="/jsf/print/preview_item/ExtendedMatchingItems.jsp" %>
		        </h:panelGroup>
				<h:panelGroup rendered="#{question.itemData.typeId == 15}">
		           <%@ include file="/jsf/print/preview_item/CalculatedQuestion.jsp" %>
		        </h:panelGroup>		        
                <h:outputText escape="false" value="<hr />"
					rendered="#{!(part.number == pdfAssessment.sizeDeliveryParts && question.number == pdfAssessment.totalQuestions) && (printSettings.showKeys || printSettings.showKeysFeedback) }" />
              </h:panelGroup>
            </h:column>
          </h:dataTable>
        </h:column>
      </h:dataTable>

  </h:form>
  </body>
  </html>
</f:view>
