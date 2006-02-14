<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
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
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
      </head>
<body onload="resetInsertAnswerSelectMenus();countNum();<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
<!-- content... -->
<!-- FORM -->
<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm" onsubmit="return false;">

  <!-- NOTE:  Had to call this.form.onsubmit(); when toggling between single  -->
  <!-- and multiple choice, or adding additional answer choices.  -->
  <!-- to invoke the onsubmit() function for htmlarea to save the htmlarea contents to bean -->
  <!-- otherwise, when toggleing or adding more choices, all contents in wywisyg editor are lost -->

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <!-- 1 POINTS -->
<div class="indnt2">

 <span id="num1" class="number"></span>
     <div class="shorttext"> <h:outputLabel value="#{msg.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}"required="true" size="6" >
<f:validateDoubleRange /></h:inputText>

<h:message for="answerptr" styleClass="validate"/>
</div>
<br/>
  <!-- 2 TEXT -->

  <span id="num2" class="number"></span>
   <div class="longtext"><h:outputLabel value="#{msg.q_text}" />
</div>
  <!-- WYSIWYG -->
   
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.itemText}" >
     <f:validateLength minimum="1" maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>

  <!-- 3 ANSWER -->
 <span id="num3" class="number"></span>
  <div class="longtext">
    <h:outputLabel value="#{msg.answer} " />  </div>
  <!-- need to add a listener, for the radio button below,to toggle between single and multiple correct-->
<div class="indnt2">
    <h:selectOneRadio layout="lineDirection"
		onclick="this.form.onsubmit();this.form.submit();"
           value="#{itemauthor.currentItem.multipleCorrectString}"
	valueChangeListener="#{itemauthor.currentItem.toggleChoiceTypes}" >
      <f:selectItem itemValue="1"
        itemLabel="#{msg.single}" />
      <f:selectItem itemValue="2"
        itemLabel="#{msg.multipl_mc}" />
    </h:selectOneRadio>
</div>

	<!-- dynamicaly generate rows of answers based on number of answers-->
<div class="indnt2">
 <h:dataTable id="mcchoices" value="#{itemauthor.currentItem.multipleChoiceAnswers}" var="answer" headerClass="alignLeft longtext">
<h:column>
<h:panelGrid columns="2">
<h:panelGroup>
      

	
<h:outputText value="#{msg.correct_answer}"  />
<f:verbatim><br/></f:verbatim>
<!-- if multiple correct, use checkboxes -->
        <h:selectManyCheckbox value="#{itemauthor.currentItem.corrAnswers}" id="mccheckboxes"
	rendered="#{itemauthor.currentItem.multipleCorrect}">
	<f:selectItem itemValue="#{answer.label}" itemLabel="#{answer.label}"/>
        </h:selectManyCheckbox>
         
	<!-- if single correct, use radiobuttons -->



<h:selectOneRadio onclick="uncheckOthers(this);" id="mcradiobtn"
	layout="pageDirection"
	value="#{itemauthor.currentItem.corrAnswer}"
	rendered="#{!itemauthor.currentItem.multipleCorrect}">

	<f:selectItem itemValue="#{answer.label}" itemLabel="#{answer.label}"/>
</h:selectOneRadio>


<h:commandLink id="removelink" onfocus="document.forms[1].onsubmit();" action="#{itemauthor.currentItem.removeChoices}">
  <h:outputText id="text" value="#{msg.button_remove}"/>
  <f:param name="answerid" value="#{answer.label}"/>
</h:commandLink>
 </h:panelGroup>
        <!-- WYSIWYG -->

   <samigo:wysiwyg rows="140" value="#{answer.text}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
 
 
          <h:outputText value="#{msg.feedback_optional}" rendered="#{assessmentSettings.feedbackAuthoring== '2'}" />
    
        <!-- WYSIWYG -->

<h:panelGroup rendered="#{assessmentSettings.feedbackAuthoring== '2'}">
         <samigo:wysiwyg rows="140" value="#{answer.feedback}">
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
  </h:panelGroup>

        </h:panelGrid>
</h:column>
</h:dataTable>

</div>
<h:inputHidden id="selectedRadioBtn" value="#{itemauthor.currentItem.corrAnswer}" />
<div class="shorttext indnt2">
  <h:outputText value="#{msg.insert_additional_a}" />
<h:selectOneMenu  id="insertAdditionalAnswerSelectMenu"  onchange="this.form.onsubmit(); clickAddChoiceLink();" value="#{itemauthor.currentItem.additionalChoices}" >
  <f:selectItem itemLabel="#{msg.select_menu}" itemValue="0"/>
  <f:selectItem itemLabel="1" itemValue="1"/>
  <f:selectItem itemLabel="2" itemValue="2"/>
  <f:selectItem itemLabel="3" itemValue="3"/>
  <f:selectItem itemLabel="4" itemValue="4"/>
  <f:selectItem itemLabel="5" itemValue="5"/>
  <f:selectItem itemLabel="6" itemValue="6"/>
</h:selectOneMenu>
<h:commandLink id="hiddenAddChoicelink" action="#{itemauthor.currentItem.addChoicesAction}" value="">
</h:commandLink>
</div>
<br/>
    <!-- 4 RANDOMIZE -->
 <span id="num4" class="number"></span>
  <div class="longtext">
    <h:outputLabel value="#{msg.randomize_answers}" />    </div>
<div class="indnt3">
    <h:selectOneRadio value="#{itemauthor.currentItem.randomized}" >
     <f:selectItem itemValue="true"
       itemLabel="#{msg.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{msg.no}" />
    </h:selectOneRadio>
  </div>

    <!-- 5 RATIONALE -->
    <span id="num5" class="number"></span>
   <div class="longtext">
 <h:outputLabel value="#{msg.require_rationale}" /></div>
<div class="indnt3">
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" >
     <f:selectItem itemValue="true" itemLabel="#{msg.yes}"/>
     <f:selectItem itemValue="false" itemLabel="#{msg.no}" />
    </h:selectOneRadio>
</div>
    <!-- 6 PART -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim><span id="num6" class="number"></span></f:verbatim>
<h:outputLabel value="#{msg.assign_to_p} " />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
</h:panelGrid>


    <!-- 7 POOL -->

<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>  <span id="num7" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p} " />
  <h:selectOneMenu rendered="#{itemauthor.target == 'assessment'}" id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

</h:panelGrid>


 <!-- 8 FEEDBACK -->
 <span id="num8" class="number"></span>
  <div class="longtext">
  <h:outputLabel value="#{msg.correct_incorrect_an}" />
</div>
<div class="indnt2">
  <h:outputText value="#{msg.correct_answer_opti}" />
<br/>
  <!-- WYSIWYG -->
  

<h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>
<br/>
 <h:outputText value="#{msg.incorrect_answer_op}" />

  <!-- WYSIWYG -->
   <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}"  >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
</h:panelGrid>
</div>
 <!-- METADATA -->

<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim><span id="num9" class="number"></span></f:verbatim>
<h:outputLabel value="Metadata"/><br/>


<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputText value="#{msg.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputText value="#{msg.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputText value="#{msg.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
</h:panelGroup>
</div>

<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment' && !itemauthor.currentItem.multipleCorrect}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
 

 <h:commandButton rendered="#{itemauthor.target=='assessment' && itemauthor.currentItem.multipleCorrect}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getOutcome}"  styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>


  <h:commandButton rendered="#{itemauthor.target=='questionpool' && !itemauthor.currentItem.multipleCorrect}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}"  styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='questionpool' && itemauthor.currentItem.multipleCorrect}" value="#{msg.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_cancel}" action="editAssessment" immediate="true"/>
 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_cancel}" action="editPool" immediate="true"/>

</p>
</h:form>
<!-- end content -->
</div>

    </body>
  </html>
</f:view>

