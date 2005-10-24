<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.create_modify_a}" /></title>
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
<body onload="document.forms[0].reset(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- some back end stuff stubbed -->
<%
	String commentOutFileUpload = org.sakaiproject.service.framework.config.cover.ServerConfigurationService.getString("sam_file_upload_comment_out");
%>
<h:form id="assesssmentForm">
<h:messages/>
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
  <p class="navIntraTool">
    <h:commandLink action="author" immediate="true">
      <h:outputText value="#{msg.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </p>
  <div align="left">
    <h3>
       <h:outputText value="#{msg.qs}" />
       <h:outputText value=": " />
       <h:outputText value="#{assessmentBean.title}" />
    </h3>
  </div><div align="right">
    <h:outputText value="#{assessmentBean.questionSize} #{msg.existing_qs}" />
    <h:outputText value=" | " />
    <h:outputText value="#{assessmentBean.totalScore} #{msg.total_pt}" />
 </div>
  <p class="navModeAction">
    <h:commandLink id="addPart" action="editPart" immediate="true">
      <h:outputText value="#{msg.subnav_add_part}" />
      <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorPartListener" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink id="editAssessmentSettings" action="editAssessmentSettings" immediate="true">
      <h:outputText value="#{msg.subnav_settings}" />
      <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorSettingsListener" />
    </h:commandLink>
    <h:outputText value=" | " />
      <h:commandLink  action="beginAssessment">
        <h:outputText value="#{msg.subnav_preview}"/>
        <f:param name="assessmentId" value="#{assessmentBean.assessmentId}"/>
        <f:param name="previewAssessment" value="true"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.BeginDeliveryActionListener" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
      </h:commandLink>
  </p>
<div class="longtext">
  <h:outputLabel value="#{msg.add_q}   "/>
<h:selectOneMenu onchange="clickInsertLink(this);"
  value="#{itemauthor.itemType}" id="changeQType">
<%--
  <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
--%>

  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue="" />
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
<%--
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7"/>
--%>
<% if(!commentOutFileUpload.equalsIgnoreCase("true")){ %>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
<%}  %>  
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10"/>
</h:selectOneMenu>
</div>
<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
  <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
</h:commandLink>

<div class="indnt1">
<h:dataTable id="parts" width="100%" headerClass="regHeading"
      value="#{assessmentBean.sections}" var="partBean">

 <%-- note that partBean is ui/delivery/SectionContentsBean not ui/author/SectionBean --%>
  <h:column>
<f:verbatim><h4></f:verbatim>
    <h:panelGrid columns="2" width="100%">
      <h:panelGroup>
       <f:verbatim><b></f:verbatim> <h:outputText value="#{msg.p} " /> <f:verbatim></b></f:verbatim>
        <h:selectOneMenu id="number" value="#{partBean.number}" onchange="document.forms[0].submit();" >
          <f:selectItems value="#{assessmentBean.partNumbers}" />
          <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.ReorderPartsListener" />
        </h:selectOneMenu>

        <h:outputText rendered="#{partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString == '1'}" value="#{partBean.title} - #{partBean.questions} #{msg.question_s_lower_case}" />
        <h:outputText rendered="#{partBean.sectionAuthorType!= null &&partBean.sectionAuthorTypeString == '2'}" value="#{msg.random_draw_type} <#{partBean.poolNameToBeDrawn}> - #{partBean.numberToBeDrawnString} #{msg.question_s_lower_case}" />
      </h:panelGroup>
      <h:panelGroup>

        <h:commandLink action="confirmRemovePart" immediate="true"
          rendered="#{partBean.number ne 1}">
          <h:outputText value="#{msg.remove_part}" />
          <!-- use this to set the sectionBean.sectionId in ConfirmRemovePartListener -->
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmRemovePartListener" />
        </h:commandLink>
          <h:outputText value=" | " rendered="#{partBean.number ne 1}"/>

        <h:commandLink id="editPart" immediate="true" action="editPart">
          <h:outputText value="#{msg.button_modify}" />
          <f:param name="sectionId" value="#{partBean.sectionId}"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditPartListener" />
        </h:commandLink>
      </h:panelGroup>
        <h:outputText escape="false" value="#{partBean.description}" />
    </h:panelGrid>
<f:verbatim></h4><div class="indnt2"></f:verbatim>


        <h:outputText rendered="#{partBean.sectionAuthorType!= null && partBean.sectionAuthorTypeString == '2'}" value="#{msg.random_draw_msg}" />

<!-- this insert should only show up when there are no questions in this part -->
<h:panelGroup rendered="#{partBean.itemContentsSize eq '0'}">
    <f:verbatim>    <div class="longtext"> </f:verbatim> <h:outputLabel value="#{msg.ins_new_q} "/>

<!-- each selectItem stores the itemtype, current sequence -->

<h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);"  value="#{itemauthor.itemTypeString}" >

  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />

  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1,#{partBean.number},0"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3,#{partBean.number},0"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5,#{partBean.number},0"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8,#{partBean.number},0"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9, #{partBean.number},0"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4,#{partBean.number},0"/>
<%--
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7,#{partBean.number},0"/>
--%>
<% if(!commentOutFileUpload.equalsIgnoreCase("true")){ %>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6,#{partBean.number},0"/>
<%}  %>  
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10,#{partBean.number},0"/>

</h:selectOneMenu>
 <f:verbatim>    </div> </f:verbatim>
<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
</h:commandLink>

</h:panelGroup>


  <h:dataTable id="parts" width="100%" headerClass="regHeading"
        value="#{partBean.itemContents}" var="question" rendered="#{partBean.sectionAuthorType== null || partBean.sectionAuthorTypeString ==  '1'}" >

      <h:column>
<f:verbatim><h5></f:verbatim>
        <h:panelGrid columns="2" width="100%">
          <h:panelGroup>
           <f:verbatim><b></f:verbatim>   <h:outputText value="#{msg.q} " /> <f:verbatim></b></f:verbatim>
            <h:inputHidden id="currItemId" value="#{question.itemData.itemIdString}"/>
            <h:selectOneMenu id="number" onchange="document.forms[0].submit();" value="#{question.number}">
              <f:selectItems value="#{partBean.questionNumbers}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.ReorderQuestionsListener" />
            </h:selectOneMenu>
            <h:outputText value=" #{question.itemData.type.keyword} -  #{question.itemData.score} #{msg.points_lower_case}" />
          </h:panelGroup>
          <h:panelGroup>
            <h:commandLink styleClass="alignRight" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}">
              <h:outputText value="#{msg.button_remove}" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
            </h:commandLink>
            <h:outputText value=" | " />
            <h:commandLink id="modify" action="#{itemauthor.doit}" immediate="true">
              <h:outputText value="#{msg.button_modify}" />
              <f:actionListener
                  type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
              <f:param name="itemid" value="#{question.itemData.itemIdString}"/>
              <f:param name="target" value="assessment"/>
            </h:commandLink>
          </h:panelGroup>
        </h:panelGrid>
<f:verbatim></h5></f:verbatim>
     <f:verbatim> <div class="indnt3"></f:verbatim>
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
<f:verbatim> </div></f:verbatim>
    <f:verbatim>    <div class="longtext"> </f:verbatim> <h:outputLabel value="#{msg.ins_new_q} "/>

<!-- each selectItem stores the itemtype, current sequence -->

<h:selectOneMenu id="changeQType" onchange="clickInsertLink(this);"  value="#{itemauthor.itemTypeString}" >

  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.StartInsertItemListener" />

  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1,#{partBean.number},#{question.itemData.sequence}"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3,#{partBean.number},#{question.itemData.sequence}"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5,#{partBean.number},#{question.itemData.sequence}"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8,#{partBean.number},#{question.itemData.sequence}"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9, #{partBean.number},#{question.itemData.sequence}"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4,#{partBean.number},#{question.itemData.sequence}"/>
<%--
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7,#{partBean.number},#{question.itemData.sequence}"/>
--%>
<% if(!commentOutFileUpload.equalsIgnoreCase("true")){ %>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6,#{partBean.number},#{question.itemData.sequence}"/>
<%}  %>  
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10,#{partBean.number},#{question.itemData.sequence}"/>

</h:selectOneMenu>
 <f:verbatim>    </div> </f:verbatim>
<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
</h:commandLink>
</h:column>
</h:dataTable>
<f:verbatim>    </div> </f:verbatim>
  </h:column>
</h:dataTable>

<h:outputText rendered="#{assessmentBean.hasRandomDrawPart}" value="#{msg.random_draw_total_score}"/>
</div>


</h:form>
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>

