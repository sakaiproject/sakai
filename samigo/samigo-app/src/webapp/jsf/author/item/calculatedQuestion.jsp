<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id: matching.jsp 59563 2009-04-02 15:18:05Z arwhyte@umich.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
--%>
-->
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <!-- HTMLAREA -->
      <samigo:stylesheet path="/htmlarea/htmlarea.css"/>
      <samigo:script path="/js/jquery-1.3.2.min.js"/>
      <samigo:script path="/htmlarea/htmlarea.js"/>
      <samigo:script path="/htmlarea/lang/en.js"/>
      <samigo:script path="/htmlarea/dialog.js"/>
      <samigo:script path="/htmlarea/popupwin.js"/>
      <samigo:script path="/htmlarea/popups/popup.js"/>
      <samigo:script path="/htmlarea/navigo_js/navigo_editor.js"/>
      <samigo:script path="/jsf/widget/wysiwyg/samigo/wysiwyg.js"/>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
<%--
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/authoring.js" %>
//-->


</script>
--%>
      </head>
<%-- unfortunately have to use a scriptlet here --%>
<body onload="<%= request.getAttribute("html.body.onload") %>">
<%--
      <body onload="javascript:initEditors('<%=request.getContextPath()%>');;<%= request.getAttribute("html.body.onload") %>">
--%>

<div class="portletBody">
<!-- content... -->
<!-- FORM -->

<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
<p class="act">
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{authorMessages.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>
</p>

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
  <div style="display:none">
  <h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
  </div>

  <!-- 1 POINTS -->
  <div class="tier2">
   <div class="shorttext"> <h:outputLabel value="#{authorMessages.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true">
<f:validateDoubleRange/>
</h:inputText>
<br/><h:message for="answerptr" styleClass="validate"/>
  </div>
<br/>



  <%-- 2 QUESTION TEXT --%>
  <div class="longtext"> <h:outputLabel value="#{authorMessages.q_text}" />
  <br/></div>
<div class="tier2">

  <h:outputText value="#{authorMessages.calc_question_define_vars}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_answer_expression}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_answer_variance}" />
  <br/><br/>

<h:outputLink onclick="$('#calcQInstructions').toggle();" value="#"><h:outputText value="#{authorMessages.calc_question_hideshow}"/> </h:outputLink>
<div id="calcQInstructions" style='display:none;'>
  
  <h:outputText value="#{authorMessages.calc_question_answer_decimal}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_operators}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_functions}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_constants}" />
  <br/><br/>
  <h:outputText value="#{authorMessages.calc_question_example1}" />
  <br/>
  <h:outputText value="#{authorMessages.calc_question_example2}" />
  <br/><br/>
  <f:verbatim><b></f:verbatim>
  <h:outputText value="#{authorMessages.calc_question_preview}" />
  <f:verbatim><b></f:verbatim>
</div>
  
  <br/>
  
  <!-- WYSIWYG -->
  <h:panelGrid>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes">
     <f:validateLength maximum="60000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.calc_question_extract_button}" action="calculatedQuestion" styleClass="active">
  		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CalculatedQuestionExtractListener" />
  </h:commandButton>
  
  
  </div>

  <!-- 2a ATTACHMENTS -->
  <%@ include file="/jsf/author/item/attachment.jsp" %>

  <!-- 3 ANSWER -->
<!-- display variables -->
<div class="longtext"> <h:outputLabel value="#{authorMessages.create_calc_variable} " /></div>
<div class="tier2">
<h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="pairs" value="#{itemauthor.currentItem.calculatedQuestion.variablesList}" var="variable">
      
      <h:column>
        <f:facet name="header">
          
          <h:outputText value="" />
        </f:facet>

          <h:outputText value="" />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_varname_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{variable.name}" rendered="#{variable.active }" />
          <h:outputText escape="false" value="#{variable.name}" rendered="#{!variable.active }" style="color:red; text-decoration: line-through;" />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_min}"  />
        </f:facet>
          <h:inputText required="true" value="#{variable.min}" disabled="#{!variable.active }">
          </h:inputText>
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_max}"  />
        </f:facet>
          <h:inputText required="true" value="#{variable.max}" disabled="#{!variable.active }">
          </h:inputText>
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_dec}"  />
        </f:facet>
		  <h:selectOneMenu value="#{variable.decimalPlaces}" disabled="#{!variable.active }">
     		<f:selectItems value="#{itemauthor.decimalPlaceList}" />
  		</h:selectOneMenu>
      </h:column>

     </h:dataTable>
<h:outputLabel value="<p>#{authorMessages.no_variables_defined}</p>" rendered="#{itemauthor.currentItem.calculatedQuestion.variablesList eq '[]'}"/>
</div>

<!-- display formulas -->
<div class="longtext"> <h:outputLabel value="#{authorMessages.create_calc_formula} " /></div>
<div class="tier2">
<h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="formulas" value="#{itemauthor.currentItem.calculatedQuestion.formulasList}" var="formula">
      <h:column>
        <f:facet name="header">
          
          <h:outputText value=""  />
        </f:facet>

          <h:outputText value="" />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_formulaname_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{formula.name}" rendered="#{formula.active }" />
          <h:outputText escape="false" value="#{formula.name}" rendered="#{!formula.active }" style="color:red; text-decoration: line-through;" />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_formula_col}"  />
        </f:facet>
        	<h:inputText value="#{formula.text }" disabled="#{!formula.active }" style="#{(!formula.validated ? 'background-color:#ffeeee' : '')}"/>
        	
      </h:column>
      
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_tolerance}"  />
        </f:facet>
          <h:inputText required="true" value="#{formula.tolerance}"  disabled="#{!formula.active }">
          </h:inputText>
      </h:column>
      
      <h:column>
        <f:facet name="header">
          <h:outputText value="#{authorMessages.calc_question_dec}" />
        </f:facet>
		  <h:selectOneMenu id="assignToPart" value="#{formula.decimalPlaces}" disabled="#{!formula.active }">
     		<f:selectItems  value="#{itemauthor.decimalPlaceList}" />
  		</h:selectOneMenu>
          
      </h:column>
</h:dataTable>
<h:outputLabel value="<p>#{authorMessages.no_formulas_defined}</p>" rendered="#{itemauthor.currentItem.calculatedQuestion.formulasList eq '[]'}"/>

</div>

<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>

<%--
    <!-- 4 RANDOMIZE -->
   <div class="longtext">  <h:outputText value="#{authorMessages.randomize_answers}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.randomized}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>


    <!-- 5 RATIONALE -->
   <div class="longtext"> <h:outputText value="#{authorMessages.req_rationale}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" >
     <f:selectItem itemValue="true"
       itemLabel="#{authorMessages.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{authorMessages.no}" />
    </h:selectOneRadio>
  </div>

--%>
    <!-- 6 PART -->

<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
<f:verbatim>&nbsp;</f:verbatim>
<h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid>

    <!-- 7 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
<f:verbatim>&nbsp;</f:verbatim>  <h:outputLabel value="#{authorMessages.assign_to_question_p}" />
<%-- stub debug --%>
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid><br/>


 <!-- 8 FEEDBACK -->
  <f:verbatim></f:verbatim>
<f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel value="#{authorMessages.correct_incorrect_an}" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}"/>
<f:verbatim><br/></br/></div><div class="tier2"></f:verbatim>

<h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <h:outputText value="#{authorMessages.correct_answer_opti}" />
  <f:verbatim><br/></f:verbatim>
  <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
 </h:panelGrid>

<f:verbatim><br/></f:verbatim>

 <h:panelGrid rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
  <h:outputText value="#{authorMessages.incorrect_answer_op}"/>
  <f:verbatim><br/></f:verbatim>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>
 </h:panelGrid>

<f:verbatim><br/></div></f:verbatim>

<!-- METADATA -->

<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="tier2"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputText value="#{authorMessages.objective}" />
  <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputText value="#{authorMessages.keyword}" />
  <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputText value="#{authorMessages.rubric_colon}" />
  <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</div>



<p class="act">
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='assessment'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton accesskey="#{authorMessages.a_save}" rendered="#{itemauthor.target=='questionpool'}" value="#{authorMessages.button_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton accesskey="#{authorMessages.a_cancel}" rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.cancel_action}" action="editPool" immediate="true">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ResetItemAttachmentListener" />
 </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>

    </body>
  </html>
</f:view>

