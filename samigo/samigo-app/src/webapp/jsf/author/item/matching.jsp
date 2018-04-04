<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{authorMessages.item_display_author}"/></title>
      
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
<%--
<script type="text/JavaScript">
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
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
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

  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
  <div style="display:none">
  <h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
  </div>

    <!-- 1 POINTS -->
    <div class="form-group row">
        <h:outputLabel for="answerptr" value="#{authorMessages.answer_point_value}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-2">
            <h:inputText id="answerptr" label="#{authorMessages.pt}" value="#{itemauthor.currentItem.itemScore}" 
                        required="true" disabled="#{author.isEditPoolFlow}" styleClass="form-control ConvertPoint">
                <f:validateDoubleRange minimum="0.00"/>
            </h:inputText>
            <h:message for="answerptr" styleClass="validate"/>
        </div>
    </div>
    
    <div  class="form-group row">
        <h:outputLabel value="#{authorMessages.answer_point_value_display}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-5 samigo-inline-radio">
            <h:selectOneRadio value="#{itemauthor.currentItem.itemScoreDisplayFlag}" >
                <f:selectItem itemValue="true" itemLabel="#{authorMessages.yes}" />
                <f:selectItem itemValue="false" itemLabel="#{authorMessages.no}" />
            </h:selectOneRadio>
        </div>
    </div>

    <!-- 1.2 Min POINTS -->
    <f:subview id="minPoints" rendered="#{itemauthor.allowMinScore}">    
        <div class="form-group row">   
            <h:outputLabel value="#{authorMessages.answer_min_point_value}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-2">
                <h:inputText id="answerminptr" value="#{itemauthor.currentItem.itemMinScore}" styleClass="form-control ConvertPoint">
                    <f:validateDoubleRange/>
                </h:inputText>
                <h:outputText value="#{authorMessages.answer_min_point_info}"  />
                <h:message for="answerminptr" styleClass="validate"/>
            </div>
        </div>
    </f:subview>
    
    <!-- 2 TEXT -->
    <div class="form-group row">
        <h:outputLabel value="#{authorMessages.q_text}" styleClass="col-md-4 col-lg-2 form-control-label"/>  
        <!-- WYSIWYG -->
        <div class="col-md-8">
            <h:panelGrid>
                <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" hasToggle="yes" mode="author">
                    <f:validateLength maximum="60000"/>
                </samigo:wysiwyg>
            </h:panelGrid>
        </div>
    </div>

    <!-- 2a ATTACHMENTS -->
    <%@ include file="/jsf/author/item/attachment.jsp" %>

    <!-- 3 ANSWER -->
    <div class="longtext"> 
        <h:outputLabel value="#{authorMessages.create_pairing} " />
    </div>
    <div>
    <!-- display existing pairs -->

        <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="pairs" value="#{itemauthor.currentItem.matchItemBeanList}" var="pair">
      
        <h:column>
            <f:facet name="header">          
                <h:outputText value=""  />
            </f:facet>
          <h:outputText value="#{pair.sequence}"  />
        </h:column>

        <h:column>
            <f:facet name="header">
                <h:outputText value="#{authorMessages.matching_choice_col}"  />
            </f:facet>
          <h:outputText escape="false" value="#{pair.choice}"  />
        </h:column>

        <h:column>
            <f:facet name="header">
                <h:outputText value="#{authorMessages.matching_match_col}"  />
            </f:facet>
            <h:outputText escape="false" value="#{pair.match}"  />
        </h:column>
        
        <h:column>
            <f:facet name="header">
                <h:outputText value=""/>
            </f:facet>
            <h:panelGrid>
                <h:panelGroup>
                    <h:commandLink rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}" id="modifylink" immediate="true" action="#{itemauthor.currentItem.editMatchPair}">
                        <h:outputText id="modifytext" value="#{commonMessages.edit_action}"/>
                        <f:param name="sequence" value="#{pair.sequence}"/>
                    </h:commandLink>
                    <h:outputText value="#{authorMessages.matching_currently_editing}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence== pair.sequence}"/>
                    <h:outputText value=" #{authorMessages.separator} " rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}"/>
                    <h:commandLink id="removelink" immediate="true" action="#{itemauthor.currentItem.removeMatchPair}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}">
                        <h:outputText id="removetext" value="#{commonMessages.remove_action}"/>
                        <f:param name="sequence" value="#{pair.sequence}"/>
                    </h:commandLink>
                </h:panelGroup>
            </h:panelGrid>
        </h:column>
        </h:dataTable>
        <h:outputLabel value="<p>#{authorMessages.no_matching_pair}</p>" escape="false" rendered="#{itemauthor.currentItem.matchItemBeanList eq '[]'}"/>
    </div>
    
    <!-- WYSIWYG -->
    <div class="tier2">
        <div class="form-group row">
            <h:outputLabel value=" #{authorMessages.matching_choice_col}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg value="#{itemauthor.currentItem.currentMatchPair.choice}"
                        rows="140" hasToggle="yes" mode="author">
                        <f:validateLength maximum="4000" />
                    </samigo:wysiwyg>   
                </h:panelGrid>
            </div>
        </div>
        <div class="form-group row">        
            <h:outputLabel for="controllingSequence" value=" #{authorMessages.matching_match_col}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
        <h:selectOneMenu value="#{itemauthor.currentItem.currentMatchPair.controllingSequence }" id="controllingSequence">
            <f:selectItems value="#{itemauthor.currentItem.selfSequenceList }" />
        </h:selectOneMenu>
        <h:outputLabel id="noFeedbackMsg" value="#{authorMessages.feedback_unavailable}" style="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence ? 'display: none;' : ''}" />
        <h:panelGrid>
            <samigo:wysiwyg value="#{itemauthor.currentItem.currentMatchPair.match}"
                rows="140" hasToggle="yes" mode="author">
                <f:validateLength maximum="4000"/>
            </samigo:wysiwyg>
        </h:panelGrid>
        </div>
        </div>
    </div>

    <!-- Match FEEDBACK -->
    <h:panelGroup id="feedbackContainer" layout="block" rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'))}">
        <!-- WYSIWYG -->
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_match_feedback_opt}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.corrMatchFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
               </h:panelGrid>
            </div>
        </div>
        <!-- WYSIWYG -->
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.incorrect_match_feedback_opt}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.incorrMatchFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
            </div>
        </div>
     </h:panelGroup>

    <f:verbatim><br/></f:verbatim>
    <div class="tier2">
        <h:commandButton value="#{commonMessages.action_save_pair}" action="#{itemauthor.currentItem.addMatchPair}"></h:commandButton>
    </div>
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
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && !author.isEditPoolFlow}">
        <h:outputLabel value="#{authorMessages.assign_to_p}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
                <f:selectItems  value="#{itemauthor.sectionSelectList}" />
            </h:selectOneMenu>
        </div>
    </h:panelGroup>

    <!-- 7 POOL -->
    <h:panelGroup styleClass="form-group row" layout="block" rendered="#{itemauthor.target == 'assessment' && author.isEditPendingAssessmentFlow}">      
        <h:outputLabel value="#{authorMessages.assign_to_question_p}" styleClass="col-md-4 col-lg-2 form-control-label"/>
        <div class="col-md-8">
            <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
                <f:selectItem itemValue="" itemLabel="#{authorMessages.select_a_pool_name}" />
                <f:selectItems value="#{itemauthor.poolSelectList}" />
            </h:selectOneMenu>
       </div>
  </h:panelGroup>

    <!-- 8 FEEDBACK -->    
    <h:panelGroup rendered="#{itemauthor.target == 'questionpool' || (itemauthor.target != 'questionpool' && (author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2') || (!author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'))}">
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_incorrect_an}" styleClass="col-md-12 form-control-label"/>
        </div>
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.correct_answer_opti}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
               </h:panelGrid>
            </div>
        </div>
        <div class="form-group row">
            <h:outputLabel value="#{authorMessages.incorrect_answer_op}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-8">
                <h:panelGrid>
                    <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" hasToggle="yes" mode="author">
                        <f:validateLength maximum="60000"/>
                    </samigo:wysiwyg>
                </h:panelGrid>
           </div>
        </div>
    </h:panelGroup>


    <!-- METADATA -->
    <h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
        <h:outputLabel value="Metadata"/><br/>
        <div class="form-group row">
            <h:outputLabel for="obj" value="#{authorMessages.objective}" styleClass="col-md-4 col-lg-2 form-control-label"/>
            <div class="col-md-5 col-lg-3">
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


<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getOutcome}" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{commonMessages.action_save}" action="#{itemauthor.currentItem.getPoolOutcome}" styleClass="active">
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
</h:form>
<!-- end content -->
</div>

<script type="text/javascript">
applyMenuListener("controllingSequence", "feedbackContainer", "noFeedbackMsg");
</script>
    </body>
  </html>
</f:view>

