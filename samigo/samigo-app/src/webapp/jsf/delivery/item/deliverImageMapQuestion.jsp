<%-- $Id: deliverMatching.jsp 104419 2012-02-06 16:06:52Z aaronz@vt.edu $
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!--
* $Id: deliverMatching.jsp 104419 2012-02-06 16:06:52Z aaronz@vt.edu $
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
  <f:verbatim><br /></f:verbatim>
  <h:outputText value="#{question.text}"  escape="false"/>
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/delivery/item/attachment.jsp" %>

  <h:outputText escape="false" value="<hr style='border:none;border-top:solid black 1px'/>" />
  <f:verbatim><h4 class="imagemap_legend"></f:verbatim><h:outputText value="#{deliveryMessages.imagemap_legend1}"  escape="false"/><f:verbatim></h4></f:verbatim>

<f:verbatim>
	<div id='sectionImageMap_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>'>	
</f:verbatim>
		<h:inputHidden id="serializedImageMap" value="#{question.serializedImageMap}" /> 
<f:verbatim> 
	
		<div id='imageMapTemplate_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>' style='display:none'>	
			<input type='hidden' name='id_' />
			
			<span name='position_'></span>
			<span id='btnSelect_'></span>
			<span name='value_'/>
		</div>
</f:verbatim>
<h:panelGroup rendered="#{delivery.actionString=='previewAssessment' || delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl' }">
	<f:verbatim> 		
			<div class='resetButtonContainer'>
				<input type='button' id='btnReset_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>' value='</f:verbatim><h:outputText value="#{deliveryMessages.reset_selection}"/><f:verbatim>' onclick="resetImageMap(this.id.replace('btnReset_', ''))" />
                                <span class="imagemap_legend"></f:verbatim><h:outputText value="#{deliveryMessages.imagemap_legend2}"  escape="false"/><f:verbatim></span>
			</div>
		</div> 
	</f:verbatim>
</h:panelGroup>
<f:verbatim> 
	<div id="imageMapContainer_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>" class='studentImageContainer'>
		<img id='img' src='</f:verbatim><h:outputText value="#{question.imageSrc}" /><f:verbatim>' />
	</div>
</f:verbatim>

<f:verbatim><br /></f:verbatim>
<h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'
                || delivery.actionString=='takeAssessment' 
                || delivery.actionString=='takeAssessmentViaUrl')
             && delivery.navigation ne '1' && delivery.displayMardForReview }">
<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review" />
	<h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}" />
	<h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('../author/markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" >
		<h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
	</h:outputLink>
</h:panelGroup>

<!-- Donï¿½t needed KEY in this question type or key should be the image with some sqares on it -->
<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGrid rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
	<h:outputLabel for="answerKeyMC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}: " />
	
	<h:dataTable value="#{question.matchingArray}" var="matching">
		<h:column>
		  <h:panelGroup id="image"
			rendered="#{matching.isCorrect}"
			styleClass="icon-sakai--check feedBackCheck" >
		  </h:panelGroup>
		  <h:panelGroup id="ximage"
			rendered="#{!matching.isCorrect}"
			styleClass="icon-sakai--delete feedBackCross">
		  </h:panelGroup>
		  <h:graphicImage id="image2"
			width="16" height="16"
			alt="#{deliveryMessages.alt_incorrect}" url="/images/delivery/spacer.gif">
		  </h:graphicImage>
		</h:column>
		<h:column>
		 <h:outputText value="#{matching.text}" escape="false"/>		 
		</h:column>
	</h:dataTable>
	
    <h:panelGroup>
		<h:dataTable value="#{question.answers}" var="answer" binding="#{table}">
			<h:column>
				<h:outputText escape="false" value="<input type='hidden' id='hiddenSerializedCoords_#{part.number}_#{question.sequence}_#{table.rowIndex}' value='#{answer}' />" /> 
			</h:column>
		</h:dataTable>

		<f:verbatim> 
			<div id="answerImageMapContainer_</f:verbatim><h:outputText value="#{part.number}_#{question.sequence}"/><f:verbatim>" class='authorImageContainer'>
				<img id='img' src='</f:verbatim><h:outputText value="#{question.imageSrc}" /><f:verbatim>' />
			</div>
		</f:verbatim>
    </h:panelGroup>
    <h:outputText value=" "/>
  </h:panelGrid>
 
  <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel && !delivery.noFeedback=='true' && question.feedbackIsNotEmpty}">
    <h:panelGroup>
      <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}: " />
      <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
    </h:panelGroup>
    <h:outputText value=" "/>
  </h:panelGrid>
    
  <h:panelGrid rendered="#{delivery.actionString !='gradeAssessment' && delivery.feedbackComponent.showGraderComment && !delivery.noFeedback=='true' && (question.gradingCommentIsNotEmpty || question.hasItemGradingAttachment)}" columns="1" border="0">
    <h:panelGroup>
      <h:outputLabel for="commentSC" styleClass="answerkeyFeedbackCommentLabel"  value="#{deliveryMessages.comment}#{deliveryMessages.column} " />
 	  <h:outputText id="commentSC" styleClass="answerkeyFeedbackCommentLabel" value="#{question.gradingComment}" escape="false" rendered="#{question.gradingCommentIsNotEmpty}"/>
    </h:panelGroup>
    
	<h:panelGroup rendered="#{question.hasItemGradingAttachment}">
      <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
        <h:column>
          <%@ include file="/jsf/shared/mimeicon.jsp" %>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="#{attach.location}" target="new_window">
            <h:outputText escape="false" value="#{attach.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
        </h:column>
      </h:dataTable>
    </h:panelGroup>
  </h:panelGrid>
</h:panelGroup>