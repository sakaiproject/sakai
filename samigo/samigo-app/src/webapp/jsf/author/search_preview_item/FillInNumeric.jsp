<%--
include file for preview fill in the blank questions
should be included in file importing previewSearch
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
<h:outputLabel escape="false" value="#{authorMessages.fill_in_numeric}" /></br>
<h:dataTable value="#{searchQuestionBean.getData(param.idString)}" var="item" width="100%">
  <h:column>


<h:outputText escape="false" value="#{item.text}" />

<!-- ATTACHMENTS -->
<%@ include file="/jsf/author/search_preview_item/attachment.jsp" %>

<h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
  <h:column>
  </h:column>
</h:dataTable>
<br/>


<h:outputLabel value="#{authorMessages.answerKey}: "/></br>
<h:dataTable value="#{item.itemTextArraySorted}" var="itemText">
  <h:column>
    <samigo:dataLine value="#{itemText.answerArraySorted}" var="answer"
                     separator=", " first="0" rows="100" >
      <h:column>
        <h:outputText escape="false" value="#{answer.text}" />
      </h:column>
    </samigo:dataLine>
  </h:column>
</h:dataTable>

<h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{authorMessages.preview_model_short_answer}: "/>
<h:outputText rendered="#{answer.text != null && answer.text ne ''}" escape="false" value="#{answer.text}" />

<br/>
<h:outputLabel rendered="#{item.correctItemFeedback != null && item.correctItemFeedback ne '' && author.isEditPendingAssessmentFlow &&  assessmentSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.correctItemFeedback}: "/>
<h:outputText rendered="#{item.correctItemFeedback != null && item.correctItemFeedback ne '' && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}"
              value="#{item.correctItemFeedback}</br>" escape="false" />
<h:outputLabel rendered="#{item.inCorrectItemFeedback != null && item.inCorrectItemFeedback ne '' && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.incorrectItemFeedback}: "/>
<h:outputText rendered="#{item.inCorrectItemFeedback != null && item.inCorrectItemFeedback ne '' && author.isEditPendingAssessmentFlow && assessmentSettings.feedbackAuthoring ne '2'}"
              value="#{item.inCorrectItemFeedback}" escape="false" />

<h:outputLabel rendered="#{item.correctItemFeedback != null && item.correctItemFeedback ne '' && !author.isEditPendingAssessmentFlow &&  publishedSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.correctItemFeedback}: "/>
<h:outputText rendered="#{item.correctItemFeedback != null && item.correctItemFeedback ne '' && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}"
              value="#{item.correctItemFeedback}</br>" escape="false" />
<h:outputLabel rendered="#{item.inCorrectItemFeedback != null && item.inCorrectItemFeedback ne '' && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}" value="#{authorMessages.incorrectItemFeedback}: "/>
<h:outputText rendered="#{item.inCorrectItemFeedback != null && item.inCorrectItemFeedback ne '' && !author.isEditPendingAssessmentFlow && publishedSettings.feedbackAuthoring ne '2'}"
              value="#{item.inCorrectItemFeedback}" escape="false" />



<%@ include file="/jsf/author/search_preview_item/tags.jsp" %>

  </h:column>
</h:dataTable>
