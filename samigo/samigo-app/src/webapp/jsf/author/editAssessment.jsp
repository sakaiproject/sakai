<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.create_modify_a}" /></title>
<script language="javascript" style="text/JavaScript">
<%@ include file="/js/samigotree.js" %>
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
<body onload="document.forms[0].reset(); disableIt(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<h:form id="assesssmentForm">

<h:messages styleClass="validation"/>
  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="validation">
    <h:panelGrid  columns="1">
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}" />
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
    </h:panelGrid>
  </h:panelGroup>

  <h:inputHidden id="assessmentId" value="#{assessmentBean.assessmentId}"/>
  <h:inputHidden id="showCompleteAssessment" value="#{author.showCompleteAssessment}"/>
  <h:inputHidden id="title" value="#{assessmentBean.title}" />
<%-- NOTE!
     add JavaScript to handle events that effect a part or question and
     set the value of these when a particular part or question is affected
     and the "current section" or "current part" needs to be changed
     other alternative maybe value changed listener
--%>
  <h:inputHidden id="SectionIdent" value="#{author.currentSection}"/>
  <h:inputHidden id="ItemIdent" value="#{author.currentItem}"/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

  <div class="navView">
    <h3>
       <h:outputText value="#{authorMessages.qs}#{authorMessages.column} #{assessmentBean.title}" escape="false" />
    </h3>
  </div><div class="navList">
    <h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize > 1}" />
	<h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_q} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 1}" />
	<h:outputText value="#{assessmentBean.questionSize} #{authorMessages.existing_qs} #{authorMessages.dash} " rendered="#{assessmentBean.questionSize == 0}" />
    <h:outputText value="#{assessmentBean.totalScore}">
  <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore > 1}" />
    <h:outputText value="#{authorMessages.total_pt}" rendered="#{assessmentBean.totalScore == 1}" />
    <h:outputText value="#{authorMessages.total_pts}" rendered="#{assessmentBean.totalScore == 0}" />

 </div>
  <p class="navViewAction">
    <h:commandLink title="#{authorMessages.t_addPart}" id="addPart" action="editPart" immediate="true" rendered="#{author.isEditPendingAssessmentFlow}">
      <h:outputText value="#{authorMessages.subnav_add_part}" />
      <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorPartListener" />
    </h:commandLink>
    <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow}"/>
    <h:commandLink title="#{authorMessages.t_settings}" id="editAssessmentSettings_editAssessment" action="editAssessmentSettings" immediate="true" rendered="#{author.isEditPendingAssessmentFlow}">
      <h:outputText value="#{authorMessages.subnav_settings}" />
      <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
    </h:commandLink>
	<h:commandLink title="#{authorFrontDoorMessages.t_editSettings}" id="editPublishedAssessmentSettings_editAssessment" immediate="true"
          rendered="#{!author.isEditPendingAssessmentFlow}"
          action="#{author.getOutcome}">
        <h:outputText  value="#{authorFrontDoorMessages.link_settings}" />
        <f:param name="publishedAssessmentId" value="#{assessmentBean.assessmentId}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPublishedSettingsListener" />
      </h:commandLink>
    <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow}"/>
      <h:commandLink  title="#{authorMessages.t_preview}" action="beginAssessment" rendered="#{author.isEditPendingAssessmentFlow}">
        <h:outputText value="#{authorMessages.subnav_preview}"/>
        <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
        <f:param name="actionString" value="previewAssessment" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
      </h:commandLink>
	  <h:outputText value=" #{authorMessages.separator} " rendered="#{assessmentBean.showPrintLink eq 'true'}"/>
      <h:commandLink action="#{itemauthor.print}" rendered="#{assessmentBean.showPrintLink eq 'true'}">
        <h:outputText value="#{authorMessages.subnav_print}" escape="false" />
     </h:commandLink>
  </p>

<h:panelGrid columns="2" width="100%" columnClasses="shortText,navList" border="0">
<h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
<h:outputLabel for="changeQType" value="#{authorMessages.add_q}   "/>
<h:selectOneMenu onchange="clickInsertLink(this);"
  value="#{itemauthor.itemType}" id="changeQType">
  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
  <f:selectItem itemLabel="#{authorMessages.import_from_q}" itemValue="10"/>
</h:selectOneMenu>
</h:panelGroup>

<h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" />

<h:panelGroup>
  <h:commandButton id="republish" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}"
      action="#{author.getOutcome}" >
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
  </h:commandButton>

  <h:commandButton id="republishRegrade" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}"
      action="#{author.getOutcome}" >
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
  </h:commandButton>

</h:panelGroup>
</h:panelGrid>

<h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}">
  <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
  </h:outputLink>
</h:panelGrid>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
  <f:param name="itemSequence" value="0"/>
</h:commandLink>

<div class="tier1">
<h:dataTable id="parts" width="100%"
      value="#{assessmentBean.sections}" var="partBean" border="0">

 <%-- note that partBean is ui/delivery/SectionContentsBean not ui/author/SectionBean --%>
  <h:column>
<f:verbatim><h4></f:verbatim>
 <h:panelGrid columns="2" width="100%" columnClasses="navView,navList" border="0">
      <h:panelGroup >
		<h:outputText value="#{authorMessages.p}" /> <f:verbatim>&nbsp; </b></f:verbatim>
        <h:selectOneMenu id="number" value="#{partBean.number}" onchange="document.forms[0].submit();" rendered="#{author.isEditPendingAssessmentFlow}" >
          <f:selectItems value="#{assessmentBean.partNumbers}" />
          <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.ReorderPartsListener" />
        </h:selectOneMenu>
        <h:outputText value="#{partBean.number}: " rendered="#{!author.isEditPendingAssessmentFlow}"/>
		 <f:verbatim>&nbsp; </f:verbatim>
	  <h:panelGroup >
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions > 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions == 1}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.question_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1') && partBean.questions == 0}" value="#{partBean.title} #{authorMessages.dash} #{partBean.questions} #{authorMessages.questions_lower_case}" escape="false"/>

		<h:outputText rendered="#{(partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2') && partBean.numberToBeDrawnString > 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.questions_lower_case}" escape="false"/>
		<h:outputText rendered="#{(partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2') && partBean.numberToBeDrawnString == 1}" value="#{authorMessages.random_draw_type} #{partBean.poolNameToBeDrawn} - #{partBean.numberToBeDrawnString} #{authorMessages.question_lower_case}" escape="false"/>

	  </h:panelGroup>
      </h:panelGroup>

	  <h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
		<h:commandLink title="#{authorMessages.copy_to_pool}" id="copyToPool" immediate="true" action="#{questionpool.startCopyFromAssessment}">
          <h:outputText value="#{authorMessages.copy_to_pool}" rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '1'}"/>
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
        </h:commandLink>

		<h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '1'}"/>

        <h:commandLink title="#{authorMessages.t_removeP}" action="confirmRemovePart" immediate="true"
          rendered="#{partBean.number ne 1}">
          <h:outputText value="#{authorMessages.remove_part}" />
          <!-- use this to set the sectionBean.sectionId in ConfirmRemovePartListener -->
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePartListener" />
        </h:commandLink>
          <h:outputText value=" #{authorMessages.separator} " rendered="#{partBean.number ne 1}"/>

        <h:commandLink title="#{authorMessages.t_editP}" id="editPart" immediate="true" action="editPart">
          <h:outputText value="#{authorMessages.button_modify}" />
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPartListener" />
        </h:commandLink>

      </h:panelGroup>
	  
    </h:panelGrid>
      <f:verbatim></h4></f:verbatim>
        <h:outputText escape="false" value="#{partBean.description}" />
<f:verbatim><div class="tier2"></f:verbatim>
        <!-- PART ATTACHMENTS -->
        <%@ include file="/jsf/author/part_attachment.jsp" %>
<f:verbatim><div class="tier2"></f:verbatim>

        <h:outputText rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2'}" value="#{authorMessages.random_draw_msg}" />

<!-- this insert should only show up when there are no questions in this part -->
<h:panelGroup rendered="#{partBean.itemContentsSize eq '0' && author.isEditPendingAssessmentFlow}">
    <f:verbatim>    <div class="longtext"> </f:verbatim> <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>

<!-- each selectItem stores the itemtype, current sequence -->

<h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);"  value="#{itemauthor.itemTypeString}" >

  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />

  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
  <f:selectItem itemLabel="#{authorMessages.import_from_q}" itemValue="10,#{partBean.number},0"/>
</h:selectOneMenu>
 <f:verbatim>    </div> </f:verbatim>
<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
<f:param name="itemSequence" value="0"/>
</h:commandLink>

</h:panelGroup>


  <h:dataTable id="parts" width="100%"
        value="#{partBean.itemContents}" var="question" rendered="#{partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString ==  '1'}" >

      <h:column>
<f:verbatim><h5></f:verbatim>
         <h:panelGrid columns="2" width="100%" columnClasses="navView,navList">
          <h:panelGroup>
          <h:outputText value="#{authorMessages.q} " />
            <h:inputHidden id="currItemId" value="#{question.itemData.itemIdString}"/>
            <h:selectOneMenu id="number" onchange="document.forms[0].submit();" value="#{question.number}" rendered="#{author.isEditPendingAssessmentFlow}">
              <f:selectItems value="#{partBean.questionNumbers}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.ReorderQuestionsListener" />
            </h:selectOneMenu>
          <h:outputText value="#{question.number}: " rendered="#{!author.isEditPendingAssessmentFlow}"/>

	<h:panelGroup >
     <h:outputText rendered="#{question.itemData.typeId== 1}" value=" #{authorMessages.multiple_choice_sin}"/>
     <h:outputText rendered="#{question.itemData.typeId== 2}" value=" #{authorMessages.multiple_correct_ms}"/>
     <h:outputText rendered="#{question.itemData.typeId== 12}" value=" #{authorMessages.multiple_correct_ss}"/>
     <h:outputText rendered="#{question.itemData.typeId== 3}" value=" #{authorMessages.multiple_choice_surv}"/>
     <h:outputText rendered="#{question.itemData.typeId== 4}" value=" #{authorMessages.true_false}"/>
     <h:outputText rendered="#{question.itemData.typeId== 5}" value=" #{authorMessages.short_answer_essay}"/>
     <h:outputText rendered="#{question.itemData.typeId== 8}" value=" #{authorMessages.fill_in_the_blank}"/>
     <h:outputText rendered="#{question.itemData.typeId== 11}" value=" #{authorMessages.fill_in_numeric}"/>
     <h:outputText rendered="#{question.itemData.typeId== 9}" value=" #{authorMessages.matching}"/>
     <h:outputText rendered="#{question.itemData.typeId== 7}" value=" #{authorMessages.audio_recording}"/>
     <h:outputText rendered="#{question.itemData.typeId== 6}" value=" #{authorMessages.file_upload}"/>

     <h:outputText value=" #{authorMessages.dash} " />
     <h:inputText id="answerptr" value="#{question.updatedScore}" required="true" size="6" onkeydown="inIt()" >
	<f:validateDoubleRange /></h:inputText>

		<h:outputText rendered="#{question.itemData.score > 1}" value=" #{authorMessages.points_lower_case}"/>
		<h:outputText rendered="#{question.itemData.score == 1}" value=" #{authorMessages.point_lower_case}"/>
		<h:outputText rendered="#{question.itemData.score == 0}" value=" #{authorMessages.points_lower_case}"/>
	</h:panelGroup>


        </h:panelGroup>
          <h:panelGroup>
            <h:commandLink title="#{authorMessages.t_removeQ}" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}" rendered="#{author.isEditPendingAssessmentFlow}">
              <h:outputText value="#{authorMessages.button_remove}" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
            </h:commandLink>
            <h:outputText value=" #{authorMessages.separator} " rendered="#{author.isEditPendingAssessmentFlow}"/>
            <h:commandLink title="#{authorMessages.t_editQ}" id="modify" action="#{itemauthor.doit}" immediate="true">
              <h:outputText value="#{authorMessages.button_modify}" />
              <f:actionListener
                  type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
              <f:param name="target" value="assessment"/>
            </h:commandLink>
          </h:panelGroup>
        </h:panelGrid>
<f:verbatim></h5></f:verbatim>


     <f:verbatim> <div class="tier3"></f:verbatim>
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
<f:verbatim> </div></f:verbatim>
<h:panelGroup rendered="#{author.isEditPendingAssessmentFlow}">
    <f:verbatim>    <div class="longtext"> </f:verbatim> <h:outputLabel for="changeQType" value="#{authorMessages.ins_new_q} "/>

<!-- each selectItem stores the itemtype, current sequence -->

<h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);"  value="#{itemauthor.itemTypeString}" >

  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />

  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
  <f:selectItem itemLabel="#{authorMessages.import_from_q}" itemValue="10,#{partBean.number},#{question.itemData.sequence}"/>
</h:selectOneMenu>
</h:panelGroup>

<f:verbatim>    </div> </f:verbatim>
<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
  <f:param name="itemSequence" value="#{question.itemData.sequence}"/>
</h:commandLink>
</h:column>
</h:dataTable>
<f:verbatim>    </div> </f:verbatim>
  </h:column>
</h:dataTable>

<h:commandButton value="Update points" id="pointsUpdate" action="editAssessment" />

</div>

<p class="navList">
<h:panelGroup>
  <h:commandButton id="republish1" value="#{authorMessages.button_republish}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow}"
      action="#{author.getOutcome}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
  </h:commandButton>

  <h:commandButton id="republishRegrade1" value="#{authorMessages.button_republish_and_regrade}" type="submit" styleClass="active" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}"
      action="#{author.getOutcome}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRepublishAssessmentListener" />
  </h:commandButton>
</h:panelGroup>
</p>

<h:panelGrid columns="1" width="100%" columnClasses="navList" border="0" rendered="#{!author.isEditPendingAssessmentFlow && assessmentBean.hasGradingData}">
  <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('regradeRepublishPopUp.faces','RegradeRepublish','width=400,height=400,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('regradeRepublishPopUp.faces','RegradeRepublishPopUp','width=400,height=400,scrollbars=yes, resizable=yes');" >
    <h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
  </h:outputLink>
</h:panelGrid>


  <h:panelGroup rendered="#{!author.isEditPendingAssessmentFlow}" styleClass="validation">
    <h:panelGrid  columns="1">
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_1}" />
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_21}" rendered="#{assessmentBean.hasGradingData}"/>
	  <h:outputText value="#{authorMessages.edit_published_assessment_warn_22}" rendered="#{!assessmentBean.hasGradingData}"/>
    </h:panelGrid>
  </h:panelGroup>

</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>

