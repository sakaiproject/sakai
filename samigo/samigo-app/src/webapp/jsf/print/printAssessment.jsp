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
  
  <html xmlns="http://www.w3.org/1999/xhtml">
  <head>
  <%= request.getAttribute("html.head") %>
  <title>
  <h:outputText value="Quiz: #{pdfAssessmentBean.title}" />
  </title>
  
  <samigo:stylesheet path="/css/print/print.css"/>
  
  <script language="javascript" style="text/JavaScript">
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

</head>




<body 
  onload="document.forms[0].reset(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>; qb_init('print');"
  id="qb_print"
  class="view_student">
    
  <!-- content... -->
  <!-- some back end stuff stubbed -->
  
  <h:form id="assessmentForm">
  
  <!-- HEADINGS (NOT PRINTED) -->
      <p class="navIntraTool">        
        <h:commandLink action="#{pdfAssessment.getActionString}">
          <h:outputText value="#{printMessages.back_to_assessmt}" rendered="#{pdfAssessment.actionString == 'editAssessment'}" escape="false" />
		  <h:outputText value="#{printMessages.back_to_landingpage}" rendered="#{pdfAssessment.actionString != 'editAssessment'}" escape="false" />
        </h:commandLink>
      </p>
      
    <h:messages/>
    
    <div id="header">
      <p class="navModeAction"> 
        
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
        
        <h:outputText value="#{printMessages.font_size}:" />
        <h:selectOneMenu id="fontSize" value="#{printSettings.fontSize}">
          <f:selectItem itemLabel="#{printMessages.size_xsmall}" itemValue="1" />
          <f:selectItem itemLabel="#{printMessages.size_small}" itemValue="2" />
          <f:selectItem itemLabel="#{printMessages.size_medium}" itemValue="3" />
          <f:selectItem itemLabel="#{printMessages.size_large}" itemValue="4" />
          <f:selectItem itemLabel="#{printMessages.size_xlarge}" itemValue="5" />
        </h:selectOneMenu>
        
        &nbsp;&nbsp;&nbsp;
        
        <h:commandButton action="#{pdfAssessment.prepDocumentPDF}" value="#{printMessages.apply_settings}" />
        <br />
        
        <h:outputText value="<input type='button' onclick='print(); return false;' value='#{printMessages.print_html}' />" escape="false" />
        <h:commandButton action="#{pdfAssessment.getPDFAttachment}" value="#{printMessages.print_pdf}" />
      </p>
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
	  <h:outputText id="assessmentIntro" value="#{pdfAssessment.intro}" 
	          escape="false" rendered="#{printSettings.showPartIntros && pdfAssessment.intro != ''}" />
	</div>
      
      <h:dataTable id="parts" width="100%" value="#{pdfAssessment.deliveryParts}" var="partBean">
        <%-- note that partBean is ui/delivery/SectionContentsBean not ui/author/SectionBean --%>
        <h:column>
          <h:panelGroup id="fullTitle">
		    <h:panelGroup id="partIntro" styleClass="part_title" rendered="#{printSettings.showPartIntros && pdfAssessment.sizeDeliveryParts > 1}">
		      <h:outputText id="number" value="#{authorMessages.p} #{partBean.number}: " escape="false" />
		      <h:outputText id="title" value="#{partBean.title}" escape="false" />
            </h:panelGroup>
          </h:panelGroup>
          
          <!-- BEGIN ASSESSMENT PARTS & QUESTIONS -->
          <h:dataTable id="items" width="100%" headerClass="regHeading" value="#{partBean.itemContents}" var="question"
                                columnClasses="col-printQNum, col-printQues" rowClasses="item" >
            
            <h:column>
              <h:outputText value="<h3>" escape="false" />
                <h:outputText id="number" escape="false" value="#{question.number}" />
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
                <h:outputText escape="false" value="<hr />"
					rendered="#{!(partBean.number == pdfAssessment.sizeDeliveryParts && question.number == pdfAssessment.totalQuestions) && (printSettings.showKeys || printSettings.showKeysFeedback) }" />
              </h:panelGroup>
            </h:column>
          </h:dataTable>
        </h:column>
      </h:dataTable>

  </h:form>
  </body>
  </html>
</f:view>