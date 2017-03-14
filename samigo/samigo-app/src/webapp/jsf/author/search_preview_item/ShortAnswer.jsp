<%-- $Id$
include file for preview short answer essay questions
should be included in file searchPreview
--%>
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

<h:outputLabel escape="false" value="#{authorMessages.short_answer_essay}" /></br>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
    <h:column>


  <h:outputText escape="false" value="#{item.text}" />
  <!-- ATTACHMENTS -->
  <%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>

  <h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
       
<h:outputLabel rendered="#{answer.textIsNotEmpty}" value="#{authorMessages.preview_model_short_answer}" />
          <f:verbatim><br/></f:verbatim>
          <h:outputText rendered="#{answer.textIsNotEmpty}" escape="false" value="#{answer.text}" />

        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>
</br>
<h:panelGroup rendered="#{item.generalItemFbIsNotEmpty && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2' }">
  <h:outputLabel value="#{commonMessages.feedback}: " />
  <h:outputText value="#{item.generalItemFeedback}" escape="false" />
</h:panelGroup>
<h:panelGroup rendered="#{item.generalItemFbIsNotEmpty && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2' }">
  <h:outputLabel value="#{commonMessages.feedback}: " />
  <h:outputText value="#{item.generalItemFeedback}" escape="false" />
</h:panelGroup>

<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

    </h:column>
</h:dataTable>
