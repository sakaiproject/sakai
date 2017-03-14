<%-- $Id: ExtendedMatchingItem.jsp 2009-12-10 $
include file for delivering extended matching items
should be included in file importing DeliveryMessages
--%>
<!--
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
  
  
      
  <!-- THEME TEXT -->
  	<f:verbatim><h3></f:verbatim>
      <h:outputText escape="false" value="#{question.itemData.themeText}" />
      <f:verbatim></h3><br/></f:verbatim>

   <!-- SIMPLE TEXT - EMI SIMPLE TEXT OPTIONS-->
      <h:dataTable value="#{question.itemData.emiAnswerOptions}" var="option"  rendered="#{question.itemData.isAnswerOptionsSimple}" cellpadding="3">
        <h:column> 
            <h:outputText escape="false" value="#{option.label}. " /> 
        </h:column>
        <h:column> 
            <h:outputText escape="false" value=" #{option.text}" /> 
        </h:column>
      </h:dataTable>
      

  <!-- RICH TEXT - EMI RICH ANSWER OPTIONS-->
  <h:outputText value="#{question.itemData.emiAnswerOptionsRichText}"  escape="false" rendered="#{question.itemData.isAnswerOptionsRich}"/>
      
  <!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS-->
  <h:dataTable value="#{question.itemData.itemAttachmentList}" var="attach"  rendered="#{question.itemData.isAnswerOptionsRich}" cellpadding="4">
    <h:column>
      <%@ include file="/jsf/shared/mimeicon.jsp" %>
    </h:column>
    <h:column>
      <h:outputLink value="#{attach.location}" target="new_window">
         <h:outputText escape="false" value="#{attach.filename}" />
      </h:outputLink>
    </h:column>
    <h:column>
      <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink}"/>
    </h:column>
  </h:dataTable>
  <!-- ATTACHMENTS ABOVE - EMI RICH ANSWER OPTIONS-->


      
  <!-- LEAD IN TEXT -->
      <f:verbatim><h3></f:verbatim>
      <h:outputText escape="false" value="#{question.itemData.leadInText}" />
      <f:verbatim></h3><br/></f:verbatim>
      
      

  <!-- EMI ITEMS -->
      <h:dataTable value="#{question.itemData.emiQuestionAnswerCombinations}" var="item" styleClass="simpleBorder" cellspacing="0" cellpadding="4">

        <h:column> 
        
            <h:outputText escape="false" value="#{item.emiCorrectOptionLabels}"/>
      
            <h:outputText escape="false" value="(#{item.requiredOptionsCount} #{authorMessages.answers_required})" rendered="#{item.requiredOptionsCount>0}"/>
      
        </h:column>

        <h:column> 
         <h:panelGroup rendered="#{(item.text != null && item.text ne '') || item.hasAttachment}">
          <h:outputText escape="false" value="#{item.sequence}. #{item.text}" />
          
  <!-- ATTACHMENTS BELOW - EMI ITEMTEXT ATTACHMENTS-->
  <h:dataTable value="#{item.itemTextAttachmentList}" var="attach" styleClass="noBorder">
    <h:column>
      <%@ include file="/jsf/shared/mimeicon.jsp" %>
    </h:column>
    <h:column>
      <h:outputLink value="#{attach.location}" target="new_window">
         <h:outputText escape="false" value="#{attach.filename}" />
      </h:outputLink>
    </h:column>
    <h:column>
      <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink}"/>
    </h:column>
  </h:dataTable>
  <!-- ATTACHMENTS ABOVE - EMI ITEMTEXT ATTACHMENTS-->
           
         </h:panelGroup>
         
        </h:column>

      </h:dataTable>
      
      <f:verbatim><br/><br/></f:verbatim>



<h:panelGroup>
  <h:outputLabel value="#{authorMessages.answerKey}: "/>
  <h:outputText escape="false" value="#{question.itemData.answerKey}" />
  <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
  <h:outputText  value="#{question.itemData.correctItemFeedback}" escape="false" />
 <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
  <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.correctItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
  <h:outputText  value="#{question.itemData.correctItemFeedback}" escape="false" />
 <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{question.itemData.incorrectItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
  <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
  <h:outputText value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/preview_item/tags.jsp" %>
