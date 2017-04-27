<%--
include file for preview image map questions
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
<h:outputLabel escape="false" value="#{authorMessages.image_map_question}" /></br>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
	<h:column>


<!-- ATTACHMENTS -->
<%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>
<h:outputText escape="false" value="#{itemValue.instruction}" /></br>

<f:verbatim>
	<div id="imageMapContainer" class="authorImageContainer">
	<img id='img' src='</f:verbatim><h:outputText value="#{item.imageMapSrc}" /><f:verbatim>' />
	</div>
</f:verbatim>

<h:dataTable id="items" value="#{item.itemTextArraySorted}" var="itemText">
	<h:column>
		<h:panelGrid columns="1">
			<h:outputText escape="false" value="#{itemText.sequence}. #{itemText.text}" />
		</h:panelGrid>
	</h:column>
	<h:column>
		<h:dataTable id="answers" value="#{itemText.answerArraySorted}" var="answer">
			<h:column>
				<h:outputText escape='false' value='#{answer.text.substring(0,answer.text.indexOf("{\\\"x1"))}'/>
			</h:column>
		</h:dataTable>
	</h:column>
	<h:column>
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
	</h:column>
	<h:column>
		<h:outputText value="" />

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



<!-- 2. print out the matching text (none)-->
</br>
<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
	<h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
	<h:outputText value="#{item.correctItemFeedback}" escape="false" />
	<f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}">
	<h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
	<h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<h:panelGroup rendered="#{item.correctItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
	<h:outputLabel value="#{authorMessages.correctItemFeedback}: "/>
	<h:outputText value="#{item.correctItemFeedback}" escape="false" />
	<f:verbatim><br/></f:verbatim>
</h:panelGroup>
<h:panelGroup rendered="#{item.incorrectItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}">
	<h:outputLabel value="#{authorMessages.incorrectItemFeedback}: "/>
	<h:outputText value="#{item.inCorrectItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

	</h:column>
</h:dataTable>
