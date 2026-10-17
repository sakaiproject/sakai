<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: deliverMatching.jsp 104419 2012-02-06 16:06:52Z aaronz@vt.edu $
include file for delivering matching questions
should be included in file importing DeliveryMessages
 -->
  <!--
* $Id: deliverMatching.jsp 104419 2012-02-06 16:06:52Z aaronz@vt.edu $

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

 -->
  <f:verbatim>
    <br/>
  </f:verbatim>
  <h:outputText value="#{question.text}" escape="false">
    <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.SecureContentWrapper"/>
  </h:outputText>
  <!-- ATTACHMENTS  -->
  <jsp:directive.include file="/jsf/delivery/item/attachment.jsp"/>
  <h:outputText escape="false" value="&lt;hr style='border:none;border-top:solid black 1px'/&gt;"/>
  <f:verbatim>
    <jsp:text><![CDATA[<h4 class="imagemap_legend">]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{deliveryMessages.imagemap_legend1}" escape="false"/>
  <f:verbatim>
    <jsp:text><![CDATA[</h4>]]></jsp:text>
  </f:verbatim>
  <f:verbatim>
    <jsp:text><![CDATA[
	<div id='sectionImageMap_]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{part.number}_#{question.sequence}"/>
  <f:verbatim>
    <jsp:text><![CDATA['>	
]]></jsp:text>
  </f:verbatim>
  <h:inputHidden id="serializedImageMap" value="#{question.serializedImageMap}"/>
  <f:verbatim>
    <jsp:text><![CDATA[ 
	
		<div id='imageMapTemplate_]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{part.number}_#{question.sequence}"/>
  <f:verbatim>
    <jsp:text><![CDATA[' style='display:none'>	
			]]></jsp:text>
    <input type="hidden" name="id_"/>
    <jsp:element name="span">
      <jsp:attribute name="name" trim="false">position_</jsp:attribute>
      <jsp:body>
      </jsp:body>
    </jsp:element>
    <jsp:element name="span">
      <jsp:attribute name="id" trim="false">btnSelect_</jsp:attribute>
      <jsp:body>
      </jsp:body>
    </jsp:element>
    <jsp:text><![CDATA[
			<span name='value_'/>
		</div>
]]></jsp:text>
  </f:verbatim>
  <h:panelGroup rendered="#{delivery.actionString=='previewAssessment' || delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl' }">
    <f:verbatim>
      <jsp:text><![CDATA[ 		
			<div class='resetButtonContainer'>
				<input type='button' id='btnReset_]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{part.number}_#{question.sequence}"/>
    <f:verbatim>
      <jsp:text>' value='</jsp:text>
    </f:verbatim>
    <h:outputText value="#{deliveryMessages.reset_selection}"/>
    <f:verbatim>
      <jsp:text><![CDATA[' onclick="resetImageMap(this.id.replace('btnReset_', ''))" />
                                <span class="imagemap_legend">]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{deliveryMessages.imagemap_legend2}" escape="false"/>
    <f:verbatim>
      <jsp:text><![CDATA[</span>
			</div>
		</div> 
	]]></jsp:text>
    </f:verbatim>
  </h:panelGroup>
  <jsp:text><![CDATA[
    <div id="imageMapContainer_]]></jsp:text>
  <h:outputText value="#{part.number}_#{question.sequence}"/>
  <jsp:text><![CDATA[" class='studentImageContainer'>
        <img id='img' src=']]></jsp:text>
  <h:outputText value="#{question.imageSrc}"/>
  <jsp:text>' alt='</jsp:text>
  <h:outputText value="#{question.imageAltText}"/>
  <jsp:text><![CDATA['>
    </div>
    ]]></jsp:text>
  <br/>
  <h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'&#10;                || delivery.actionString=='takeAssessment' &#10;                || delivery.actionString=='takeAssessmentViaUrl')&#10;             &amp;&amp; delivery.navigation ne '1' &amp;&amp; delivery.displayMardForReview }">
    <h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review"/>
    <h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}"/>
    <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/markForReviewPopUp.faces','MarkForReview','width=350,height=280,scrollbars=yes, resizable=yes');event.preventDefault();">
      <h:outputText value=" #{assessmentSettingsMessages.whats_this_link}"/>
    </h:outputLink>
  </h:panelGroup>
  <!-- DonÃ¯Â¿Â½t needed KEY in this question type or key should be the image with some sqares on it  -->
  <h:panelGroup rendered="#{delivery.feedback eq 'true'}">
    <h:panelGrid rendered="#{(delivery.feedbackComponent.showCorrectResponse &amp;&amp; delivery.feedbackComponent.showCorrection &amp;&amp; !delivery.noFeedback=='true') || delivery.actionString=='gradeAssessment'}">
      <h:outputLabel styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.ans_key}: "/>
      <h:dataTable value="#{question.matchingArray}" var="matching">
        <h:column>
          <h:panelGroup id="image" rendered="#{matching.isCorrect}" styleClass="si si-check-lg">
          </h:panelGroup>
          <h:panelGroup id="ximage" rendered="#{!matching.isCorrect}" styleClass="si si-remove feedBackCross">
          </h:panelGroup>
          <h:graphicImage id="image2" width="16" height="16" alt="#{deliveryMessages.alt_incorrect}" url="/images/delivery/spacer.gif">
          </h:graphicImage>
        </h:column>
        <h:column>
          <h:outputText value="#{matching.text}" escape="false"/>
        </h:column>
      </h:dataTable>
      <h:panelGroup>
        <h:dataTable value="#{question.answers}" var="answer" binding="#{table}">
          <h:column>
            <h:outputText escape="false" value="&lt;input type='hidden' id='hiddenSerializedCoords_#{part.number}_#{question.sequence}_#{table.rowIndex}' value='#{answer}' /&gt;"/>
          </h:column>
        </h:dataTable>
        <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrection}">
          <jsp:text><![CDATA[
        <div id="answerImageMapContainer_]]></jsp:text>
          <h:outputText value="#{part.number}_#{question.sequence}"/>
          <jsp:text><![CDATA[" class='authorImageContainer'>
            <img id='img' src=']]></jsp:text>
          <h:outputText value="#{question.imageSrc}"/>
          <jsp:text>' alt='</jsp:text>
          <h:outputText value="#{question.imageAltText}"/>
          <jsp:text><![CDATA['/>
        </div>
      ]]></jsp:text>
        </h:panelGroup>
      </h:panelGroup>
      <h:outputText value=" "/>
    </h:panelGrid>
    <h:panelGrid rendered="#{delivery.feedbackComponent.showItemLevel &amp;&amp; !delivery.noFeedback=='true' &amp;&amp; question.feedbackIsNotEmpty}">
      <h:panelGroup>
        <h:outputLabel for="feedSC" styleClass="answerkeyFeedbackCommentLabel" value="#{commonMessages.feedback}: "/>
        <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
      </h:panelGroup>
      <h:outputText value=" "/>
    </h:panelGrid>
    <h:panelGrid rendered="#{delivery.actionString !='gradeAssessment' &amp;&amp; delivery.feedbackComponent.showGraderComment &amp;&amp; !delivery.noFeedback=='true' &amp;&amp; (question.gradingCommentIsNotEmpty || question.hasItemGradingAttachment)}" columns="1" border="0">
      <h:panelGroup>
        <h:outputLabel for="commentSC" styleClass="answerkeyFeedbackCommentLabel" value="#{deliveryMessages.comment}#{deliveryMessages.column} "/>
        <h:outputText id="commentSC" styleClass="answerkeyFeedbackCommentLabel" value="#{question.gradingComment}" escape="false" rendered="#{question.gradingCommentIsNotEmpty}"/>
      </h:panelGroup>
      <h:panelGroup rendered="#{question.hasItemGradingAttachment}">
        <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
          <h:column>
            <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
          </h:column>
          <h:column>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputLink value="#{attach.location}" target="new_window">
              <h:outputText escape="false" value="#{attach.filename}"/>
            </h:outputLink>
          </h:column>
          <h:column>
            <f:verbatim>
              <jsp:text><![CDATA[&nbsp;&nbsp;&nbsp;&nbsp;]]></jsp:text>
            </f:verbatim>
            <h:outputText escape="false" value="(#{attach.fileSize} #{generalMessages.kb})" rendered="#{!attach.isLink}"/>
          </h:column>
        </h:dataTable>
      </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
</jsp:root>
