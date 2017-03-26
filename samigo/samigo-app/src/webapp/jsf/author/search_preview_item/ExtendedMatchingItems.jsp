<%--
include file for preview extended matching items
should be included in file searchPreview
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


<h:outputLabel escape="false" value="#{authorMessages.extended_matching_items}" /><f:verbatim><br/></f:verbatim>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
    <h:column>
<!-- THEME TEXT -->
<h:outputLabel escape="false" value="#{authorMessages.question_theme_text}:" /><f:verbatim><br/></f:verbatim>
<h:outputText escape="false" value="#{item.themeText}" /><f:verbatim><br/></br></f:verbatim>

<h:outputLabel escape="false" value="#{authorMessages.options_text}:" /><f:verbatim><br/></f:verbatim>
<!-- SIMPLE TEXT - EMI SIMPLE TEXT OPTIONS-->
<h:dataTable value="#{item.emiAnswerOptions}" var="option"  rendered="#{item.isAnswerOptionsSimple}" cellpadding="3">
    <h:column>
        <h:outputText escape="false" value="#{option.label}. " />
    </h:column>
    <h:column>
        <h:outputText escape="false" value=" #{option.text}" />
    </h:column>
</h:dataTable>

<!-- RICH TEXT - EMI RICH ANSWER OPTIONS-->
<h:outputText value="#{item.emiAnswerOptionsRichText}"  escape="false" rendered="#{item.isAnswerOptionsRich}"/>

<!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS-->
<h:dataTable value="#{item.itemAttachmentList}" var="attach"  rendered="#{item.isAnswerOptionsRich}" cellpadding="4">
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


<f:verbatim><br/></f:verbatim>

<!-- LEAD IN TEXT -->
<h:outputLabel escape="false" value="#{authorMessages.lead_in_statement}:" /><f:verbatim><br/></f:verbatim>
<h:outputText escape="false" value="#{item.leadInText}" /><f:verbatim><br/><br/></f:verbatim>


<!-- EMI ITEMS -->
<h:outputLabel escape="false" value="#{authorMessages.question_answer_combinations}:" /><f:verbatim><br/></f:verbatim>

<h:dataTable value="#{item.emiQuestionAnswerCombinations}" var="itemEMI" styleClass="simpleBorder" cellspacing="0" cellpadding="4">

    <h:column>

        <h:outputText escape="false" value="#{itemEMI.emiCorrectOptionLabels}"/>

        <h:outputText escape="false" value="(#{itemEMI.requiredOptionsCount} #{authorMessages.answers_required})" rendered="#{itemEMI.requiredOptionsCount>0}"/>

    </h:column>

    <h:column>
        <h:panelGroup rendered="#{(itemEMI.text != null && itemEMI.text ne '') || itemEMI.hasAttachment}">
            <h:outputText escape="false" value="#{itemEMI.sequence}. #{item.text}" />

            <!-- ATTACHMENTS BELOW - EMI ITEMTEXT ATTACHMENTS-->
            <h:dataTable value="#{itemEMI.itemTextAttachmentList}" var="attach" styleClass="noBorder">
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
    <h:outputText escape="false" value="#{item.answerKey}" />
    <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
    <h:outputText  value="#{item.correctItemFeedback}" escape="false" />
    <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
    <h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>
<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
    <h:outputText  value="#{item.correctItemFeedback}" escape="false" />
    <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
    <h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

    </h:column>
</h:dataTable>
