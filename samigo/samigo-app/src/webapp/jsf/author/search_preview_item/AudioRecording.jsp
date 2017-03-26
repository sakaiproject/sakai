<%--
include file for preview audio questions
should be included in searchPreview
--%>
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

<h:outputLabel escape="false" value="#{authorMessages.audio_recording}" /></br>

<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
<h:column>

<h:outputText escape="false" value="#{item.text}" />
<!-- ATTACHMENTS -->
<%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>

<h:outputLabel escape="false" value="#{authorMessages.options_text}" /></br>
<h:outputText escape="false" value="#{authorMessages.time_allowed_seconds} #{item.duration}" />
<f:verbatim><br/></f:verbatim>

<h:outputText escape="false" value="#{authorMessages.number_of_tries}: " />
<h:panelGroup rendered="#{item.triesAllowed > 10}">
    <h:outputText escape="false" value="#{authorMessages.unlimited}" />
</h:panelGroup>
<h:panelGroup rendered="#{item.triesAllowed <= 10}">
    <h:outputText escape="false" value="#{item.triesAllowed}" />
</h:panelGroup>
<f:verbatim><br/></f:verbatim>
<h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
    <h:column>
        <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
                <h:outputLabel rendered="#{answer.textIsNotEmpty}" value="#{authorMessages.preview_model_short_answer}" />
                <h:outputText escape="false" value="#{answer.text}" />
            </h:column>
        </h:dataTable>
    </h:column>
</h:dataTable>
<f:verbatim><br/></f:verbatim>
<h:panelGroup rendered="#{item.generalItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{commonMessages.feedback}: " />
    <h:outputText escape="false" value="#{item.generalItemFeedback}" />
</h:panelGroup>
<h:panelGroup rendered="#{item.generalItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{commonMessages.feedback}: " />
    <h:outputText escape="false" value="#{item.generalItemFeedback}" />
</h:panelGroup>
</br>
<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

</h:column>
</h:dataTable>
