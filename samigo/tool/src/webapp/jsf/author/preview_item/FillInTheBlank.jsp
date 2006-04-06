<%-- $Id$
include file for delivering fill in the blank questions
should be included in file importing DeliveryMessages
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->

  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
    </h:column>
  </h:dataTable>

<h:panelGrid columns="2" styleClass="longtext">
  <h:outputLabel value="#{msg.answerKey}: "/>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
<samigo:dataLine value="#{itemText.answerArraySorted}" var="answer"
   separator=", " first="0" rows="100" >
  <h:column>
    <h:outputText escape="false" value="#{answer.text}" />
  </h:column>
</samigo:dataLine>
    </h:column>
  </h:dataTable>

  <h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{msg.preview_model_short_answer}: "/>
  <h:outputText rendered="#{answer.text != null && answer.text ne ''}" escape="false" value="#{answer.text}" />
  <h:outputLabel rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne '' && assessmentSettings.feedbackAuthoring ne '2'}" value="#{msg.correctItemFeedback}: "/>
  <h:outputText rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne '' && assessmentSettings.feedbackAuthoring ne '2'}"
    value="#{question.itemData.correctItemFeedback}" escape="false" />
  <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne '' && assessmentSettings.feedbackAuthoring ne '2'}" value="#{msg.incorrectItemFeedback}: "/>
  <h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne '' && assessmentSettings.feedbackAuthoring ne '2'}"
    value="#{question.itemData.inCorrectItemFeedback}" escape="false" />
</h:panelGrid>
