<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id: deliverExtendedMatchingItems.jsp 62978 2009-05-27 19:29:02Z ktsao@stanford.edu $
include file for delivering extended matching items questions
should be included in file importing DeliveryMessages
 -->
  <!--
* $Id: deliverExtendedMatchingItems.jsp 62978 2009-05-27 19:29:02Z ktsao@stanford.edu $

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
  <jsp:element name="script">
    <jsp:attribute name="src" trim="false">/samigo-app/js/utils-emi.js</jsp:attribute>
    <jsp:body>
    </jsp:body>
  </jsp:element>
  <f:verbatim>
    <jsp:text><![CDATA[</h5><h3>]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{question.themeText}" escape="false"/>
  <f:verbatim>
    <jsp:text><![CDATA[</h3>]]></jsp:text>
    <br/>
  </f:verbatim>
  <h:dataTable value="#{question.itemData.emiAnswerOptions}" var="option" styleClass="noBorder" rendered="#{question.itemData.isAnswerOptionsSimple}">
    <h:column>
      <h:outputText escape="false" value="#{option.label}. "/>
    </h:column>
    <h:column>
      <h:outputText escape="false" value=" #{option.text}"/>
    </h:column>
  </h:dataTable>
  <!-- RICH TEXT - EMI RICH ANSWER OPTIONS -->
  <h:outputText value="#{question.itemData.emiAnswerOptionsRichText}" escape="false" rendered="#{question.itemData.isAnswerOptionsRich}"/>
  <!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS -->
  <h:dataTable value="#{question.itemData.itemAttachmentList}" var="attach" rendered="#{question.itemData.isAnswerOptionsRich}">
    <h:column rendered="#{!attach.isMedia}">
      <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
    </h:column>
    <h:column>
      <h:outputText escape="false" value="&#10;&#9;    &lt;embed src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; volume=&quot;50&quot; height=&quot;350&quot; width=&quot;400&quot; autostart=&quot;false&quot;/&gt;" rendered="#{attach.isInlineVideo}"/>
      <h:outputText escape="false" value="&#10;&#9;    &lt;img src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; /&gt;" rendered="#{attach.isInlineImage}"/>
      <h:outputLink value="#{attach.location}" target="new_window" rendered="#{!attach.isMedia}">
        <h:outputText escape="false" value="#{attach.filename}"/>
      </h:outputLink>
    </h:column>
    <h:column>
      <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink &amp;&amp; !attach.isMedia}"/>
    </h:column>
  </h:dataTable>
  <!-- ATTACHMENTS ABOVE - EMI RICH ANSWER OPTIONS -->
  <f:verbatim>
    <jsp:text><![CDATA[<h3>]]></jsp:text>
  </f:verbatim>
  <h:outputText value="#{question.leadInText}" escape="false"/>
  <f:verbatim>
    <jsp:text><![CDATA[</h3>]]></jsp:text>
    <br/>
  </f:verbatim>
  <!--

  <h:dataTable value="#{question.answers}" var="answer">
   <h:column>
     <h:outputText value="#{answer}" escape="false" />
   </h:column>
  </h:dataTable>

 -->
  <h:dataTable value="#{question.matchingArray}" var="matching" styleClass="simpleBorder" cellspacing="0">
    <h:column rendered="#{question.isMultipleItems}">
      <h:outputText value="#{matching.itemSequence}" escape="false"/>
    </h:column>
    <h:column>
      <h:dataTable value="#{matching.emiResponseAndCorrectStatusList}" var="responseAndCorrectStatus" styleClass="noBorder" cellspacing="0" rendered="#{delivery.actionString=='reviewAssessment'&#10;             || delivery.actionString=='gradeAssessment'}">
        <h:column rendered="#{(delivery.feedback eq 'true' &amp;&amp; delivery.feedbackComponent.showCorrectResponse &amp;&amp; &#10;              delivery.feedbackComponent.showCorrection &amp;&amp; !delivery.noFeedback=='true') &#10;              || delivery.actionString=='gradeAssessment'}">
          <h:graphicImage id="image" rendered="#{responseAndCorrectStatus.isCorrect eq 'true'}" alt="#{deliveryMessages.alt_correct}" url="/images/checkmark.gif">
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{responseAndCorrectStatus.isCorrect eq 'false'}" alt="#{deliveryMessages.alt_incorrect}" url="/images/crossmark.gif">
          </h:graphicImage>
        </h:column>
        <h:column>
          <h:outputText value="#{responseAndCorrectStatus.answerLabel}" escape="false"/>
        </h:column>
      </h:dataTable>
      <h:inputText id="responseAnswer" value="#{matching.response}" size="3" style="text-transform:uppercase;" rendered="#{delivery.actionString ne 'reviewAssessment' &amp;&amp; delivery.actionString ne 'gradeAssessment'}" validator="#{matching.validateEmiResponse}" onkeypress="return checkEMIOptions(this, '#{question.itemData.emiAnswerOptionLabels}', event)" maxlength="#{matching.itemText.requiredOptionsCount}">
      </h:inputText>
    </h:column>
    <h:column>
      <h:outputText value="#{matching.text}" escape="false"/>
      <!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS -->
      <h:dataTable value="#{matching.itemText.itemTextAttachmentList}" var="attach" styleClass="noBorder">
        <h:column rendered="#{!attach.isMedia}">
          <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
        </h:column>
        <h:column>
          <h:outputText escape="false" value="&#10;&#9;    &lt;embed src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; volume=&quot;50&quot; height=&quot;350&quot; width=&quot;400&quot; autostart=&quot;false&quot;/&gt;" rendered="#{attach.isInlineVideo}"/>
          <h:outputText escape="false" value="&#10;&#9;    &lt;img src=&quot;#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?actionMode=preview&amp;resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}&quot; /&gt;" rendered="#{attach.isInlineImage}"/>
          <h:outputLink value="#{attach.location}" target="new_window" rendered="#{!attach.isMedia}">
            <h:outputText escape="false" value="#{attach.filename}"/>
          </h:outputLink>
        </h:column>
        <h:column>
          <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink &amp;&amp; !attach.isMedia}"/>
        </h:column>
      </h:dataTable>
      <!-- ATTACHMENTS ABOVE - EMI RICH ANSWER OPTIONS -->
    </h:column>
  </h:dataTable>
  <f:verbatim>
    <br/>
  </f:verbatim>
  <h:panelGroup rendered="#{(delivery.actionString=='previewAssessment'&#10;                || delivery.actionString=='takeAssessment' &#10;                || delivery.actionString=='takeAssessmentViaUrl')&#10;             &amp;&amp; delivery.navigation ne '1' &amp;&amp; delivery.displayMardForReview }">
    <h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review"/>
    <h:outputLabel for="mark_for_review" value="#{deliveryMessages.mark}"/>
    <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('/samigo-app/jsf/author/markForReviewPopUp.faces','MarkForReview','width=350,height=280,scrollbars=yes, resizable=yes');event.preventDefault();">
      <h:outputText value=" #{assessmentSettingsMessages.whats_this_link}"/>
    </h:outputLink>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedback eq 'true'}">
    <f:verbatim>
      <br/>
    </f:verbatim>
    <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse &amp;&amp; delivery.feedbackComponent.showCorrection &amp;&amp; !delivery.noFeedback=='true'}">
      <f:verbatim>
        <jsp:text><![CDATA[<b>]]></jsp:text>
      </f:verbatim>
      <h:outputLabel for="answerKeyMC" value="#{deliveryMessages.ans_key}: "/>
      <f:verbatim>
        <jsp:text><![CDATA[</b>]]></jsp:text>
      </f:verbatim>
      <h:outputText id="answerKeyMC" value="#{question.key}" escape="false"/>
    </h:panelGroup>
    <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel &amp;&amp; !delivery.noFeedback=='true' &amp;&amp; question.feedbackIsNotEmpty}">
      <f:verbatim>
        <br/>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[<b>]]></jsp:text>
      </f:verbatim>
      <h:outputLabel for="feedSC" value="#{deliveryMessages.feedback}: "/>
      <f:verbatim>
        <jsp:text><![CDATA[</b>]]></jsp:text>
      </f:verbatim>
      <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
    </h:panelGroup>
    <h:panelGrid rendered="#{delivery.actionString !='gradeAssessment' &amp;&amp; delivery.feedbackComponent.showGraderComment &amp;&amp; !delivery.noFeedback=='true' &amp;&amp; (question.gradingCommentIsNotEmpty || question.hasItemGradingAttachment)}" columns="2" border="0">
      <h:outputLabel for="commentSC" value="&lt;b&gt;#{deliveryMessages.comment}#{deliveryMessages.column} &lt;/b&gt;"/>
      <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" rendered="#{question.gradingCommentIsNotEmpty}"/>
      <h:outputText value=" " rendered="#{question.gradingCommentIsNotEmpty}"/>
      <h:panelGroup rendered="#{question.hasItemGradingAttachment}">
        <h:dataTable value="#{question.itemGradingAttachmentList}" var="attach">
          <h:column>
            <jsp:directive.include file="/jsf/shared/mimeicon.jsp"/>
          </h:column>
          <h:column>
            <h:outputLink value="#{attach.location}" target="new_window">
              <h:outputText escape="false" value="#{attach.filename}"/>
            </h:outputLink>
          </h:column>
          <h:column>
            <h:outputText escape="false" value="(#{attach.fileSize}kb)" rendered="#{!attach.isLink}"/>
          </h:column>
        </h:dataTable>
      </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>
</jsp:root>
