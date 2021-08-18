<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id:  $
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
<!-- currentItem- ItemBean -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      <script src="/samigo-app/js/authoring.js"></script>
      <script>
      	function textCounter(field,cntfield,maxlimit) {
          	
      		if (field.value.length > maxlimit) // if too long...trim it!
      		field.value = field.value.substring(0, maxlimit);
     		 // otherwise, update 'characters left' counter
      		else
      			cntfield.value = maxlimit - field.value.length;
      	}
      	</script>
     <style type="text/css">
      .panelGridColumn td {
    	  text-align: right;
      }
      </style>
      </head>
    <body onload="countNum();<%= request.getAttribute("html.body.onload") %>">


<div class="portletBody container-fluid">
<%-- content... --%>
<%-- FORM --%>

<%-- HEADING --%>
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm" >
<p class="act">
 <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton  rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
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

    <div>
    
    <!-- 1 POINTS --> 
    <div class="form-group row"> 
        <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-2">
            <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" 
                        required="true" disabled="#{author.isEditPoolFlow}" size="6" styleClass="form-control ConvertPoint">
                <f:validateDoubleRange />
            </h:inputText>
            <h:message for="answerptr" styleClass="validate" />
        </div>
   </div>
   
    <div class="form-group row">
        <h:outputLabel for="itemScore" value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-2 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" id="itemScore">
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>

        <!-- Extra Credit -->
        <%@ include file="/jsf/author/inc/extraCreditSetting.jspf" %>

    <!-- 2 TEXT -->
    <div class="form-group row">
        <h:outputLabel for="questionItemText_textinput" value="#{authorMessages.q_text}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <!-- WYSIWYG -->
        <div class="col-md-8">
            <h:panelGrid>
                <samigo:wysiwyg identity="questionItemText" rows="140" value="#{itemauthor.currentItem.itemText}" hasToggle="yes" mode="author">
                    <f:validateLength maximum="4000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

    <!-- 2a ATTACHMENTS -->
    <%@ include file="/jsf/author/item/attachment.jsp" %>

    <!-- 3 ANSWER -->
    <div class="tier2">
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.answer} " styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-6">
                <h:graphicImage alt="#{evaluationMessages.alt_matrixChoicesSurvey}" url="/images/matrixChoicesSurvey.gif" />
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="rowData" value="#{authorMessages.rowchoices}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:inputTextarea id="rowData" value="#{itemauthor.currentItem.rowChoices}" rows="6" cols="54" />
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="columnData" value="#{authorMessages.columnchoices}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:inputTextarea id="columnData" value="#{itemauthor.currentItem.columnChoices}" rows="6" 
                                cols="54"  immediate="true"/>
            </div>
        </div>
        
        <div class="samigo-checkbox">
            <h:selectBooleanCheckbox id="forceRankingCheckbox" value="#{itemauthor.currentItem.forceRanking}"/>
            <h:outputLabel for="forceRankingCheckbox" value="#{authorMessages.forceRanking}" />
        </div>
        <div class="samigo-checkbox">
            <h:selectBooleanCheckbox id="addCommentCheckbox" immediate = "true" value="#{itemauthor.currentItem.addComment}"
                                onchange="this.form.submit();" valueChangeListener="#{itemauthor.currentItem.toggleAddComment}" />
            <h:outputLabel for="addCommentCheckbox" value="#{authorMessages.addComment}" />
        </div>
        
        <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.currentItem.addComment}" >
            <h:outputLabel for="commentField" value="#{authorMessages.commentField}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid columns="1" >
                    <h:inputTextarea id="commentField" 
                                value="#{itemauthor.currentItem.commentField}" 
                                rows="6" 
                                cols="48"
                                onkeydown="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);"
                                onkeyup="javascript:textCounter(document.getElementById('itemForm:commentField'),document.getElementById('itemForm:remLen2'), 200);"
                                onclick="javascript:if(this.value=='#{authorMessages.character_limit}') this.value='';"/>
                    <h:panelGroup>
                        <h:outputLabel value="#{authorMessages.character_count}" styleClass="alert-info"/>
                        <h:outputText value=" " />
                        <h:inputText readonly="true" id="remLen2" size="2" maxlength="3" value="#{200 - itemauthor.currentItem.commentFieldLenght}"/>
                    </h:panelGroup>
                </h:panelGrid>
           </div>
        </h:panelGroup><br/>
        <div class="form-group row">
            <h:outputLabel for="relativeWidth" value="#{authorMessages.relativeWidthOfColumns}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:selectOneMenu id="relativeWidth" value="#{itemauthor.currentItem.selectedRelativeWidth}">
                    <f:selectItems  value="#{itemauthor.selectRelativeWidthList}" />
                </h:selectOneMenu>
            </div>
        </div>
    </div>


    <%-- 3 PART --%>
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
        <h:outputLabel for="assignToPart" value="#{authorMessages.assign_to_p}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                <f:selectItems  value="#{itemauthor.sectionSelectList}" />
                <%-- use this in real  value="#{section.sectionNumberList}" --%>
            </h:selectOneMenu>
        </div>
    </h:panelGroup>
    
    <%-- 5 POOL --%>
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">
        <h:outputLabel for="assignToPool" value="#{authorMessages.assign_to_question_p}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
                <f:selectItems value="#{itemauthor.poolSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <%-- FEEDBACK --%>
    <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
        <div class="form-group row">
            <h:outputLabel for="questionFeedbackGeneral_textinput" value="#{commonMessages.feedback_optional}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <!-- WYSIWYG -->
                <h:panelGrid>
                    <samigo:wysiwyg identity="questionFeedbackGeneral" rows="140" value="#{itemauthor.currentItem.generalFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
    </h:panelGroup>

    <%-- METADATA --%>
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
        <h:outputLabel value="Metadata"/><br/>
        <div class="form-group row">
            <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-5 col-lg-3 ">
                <h:inputText size="30" id="obj" value="#{itemauthor.currentItem.objective}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="keyword" value="#{authorMessages.keyword}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-5 col-lg-3">
                <h:inputText size="30" id="keyword" value="#{itemauthor.currentItem.keyword}" styleClass="form-control"/>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel for="rubric" value="#{authorMessages.rubric_colon}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-5 col-lg-3">
                <h:inputText size="30" id="rubric" value="#{itemauthor.currentItem.rubric}" styleClass="form-control"/>
            </div>
       </div>
    </h:panelGroup>

        <%@ include file="/jsf/author/item/tags.jsp" %>

</div>
<%-- BUTTONS --%>
<p class="act">

 <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton  rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>

  <h:commandButton  rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.cancel_action}" action="editAssessment" immediate="true">
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
<%-- end content --%>
</div>
    </body>
  </html>
</f:view>
