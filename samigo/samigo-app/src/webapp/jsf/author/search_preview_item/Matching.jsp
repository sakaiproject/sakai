<%--
include file for preview matching questions
should be included in file previewSearch
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

<h:outputLabel escape="false" value="#{authorMessages.matching}" /></br>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
    <h:column>

<!-- ATTACHMENTS -->
<%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>

<h:outputText escape="false" value="#{itemValue.instruction}" /></br></br>
<!-- 1. print out the matching choices -->
<h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
    <h:column>
        <h:dataTable value="#{itemText.answerArraySorted}" var="answer"
                     rendered="#{itemText.sequence==1}">
            <h:column>
                <h:panelGrid columns="2">
                    <h:outputText escape="false" value="#{answer.label}. "/>
                    <h:outputText escape="false" value="#{answer.text}" />
                </h:panelGrid>
            </h:column>
        </h:dataTable>
    </h:column>
</h:dataTable>
<br/>
<!-- 2. print out the matching text -->
<h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
    <h:column>

        <h:outputText escape="false" value="#{itemText.sequence}. #{itemText.text}" />

        <h:outputText value="" />

        <%-- show correct & incorrect answer feedback, only need to show the set that is attached
             to the correct answer. Look at the data in the table and you may understand this part
             better -daisyf --%>
        <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
                <h:panelGroup rendered="#{answer.isCorrect && answer.correctAnswerFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
                    <h:outputLabel value="#{authorMessages.correct}: " />
                    <h:outputText escape="false" value="#{answer.correctAnswerFeedback}" />
                </h:panelGroup>
                <h:panelGroup rendered="#{answer.isCorrect && answer.correctAnswerFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
                    <h:outputLabel value="#{authorMessages.correct}: " />
                    <h:outputText escape="false" value="#{answer.correctAnswerFeedback}" />
                </h:panelGroup>
            </h:column>
        </h:dataTable>

        <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
                <h:panelGroup rendered="#{answer.isCorrect && answer.incorrectAnswerFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
                    <h:outputLabel value="#{authorMessages.incorrect}: " />
                    <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}" />
                </h:panelGroup>
                <h:panelGroup rendered="#{answer.isCorrect && answer.incorrectAnswerFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '1'}" styleClass="longtext">
                    <h:outputLabel value="#{authorMessages.incorrect}: " />
                    <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}" />
                </h:panelGroup>
            </h:column>
        </h:dataTable>

    </h:column>
</h:dataTable>
<br/>
<%-- answer key --%>
<h:panelGroup>
    <h:outputLabel value="#{authorMessages.answerKey}: "/>
    <h:outputText escape="false" value="#{item.answerKey}" />
    <f:verbatim><br/><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correct}:"/>
    <h:outputText value="#{item.correctItemFeedback}" escape="false" />
    <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrect}:"/>
    <h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.correct}:"/>
    <h:outputText value="#{item.correctItemFeedback}" escape="false" />
    <f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
    <h:outputLabel value="#{authorMessages.incorrect}:"/>
    <h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

    </h:column>
</h:dataTable>
