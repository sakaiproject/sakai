<%-- $Id$
include file for delivering true false questions
should be included in file importing DeliveryMessages
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

  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image1" rendered="#{answer.isCorrect}"
             alt="#{authorMessages.correct}" url="/images/radiochecked.gif" >
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{!answer.isCorrect}"
             alt="#{authorMessages.not_correct}" url="/images/radiounchecked.gif" >
          </h:graphicImage>
          <h:outputText value="#{authorMessages.true_msg}" rendered="#{answer.text eq 'true'}"/>
          <h:outputText value="#{authorMessages.false_msg}" rendered="#{answer.text eq 'false'}"/>
        </h:column>
      </h:dataTable>

      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{authorMessages.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{authorMessages.correct}:  #{itemContents.itemData.correctItemFeedback}" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{authorMessages.incorrect}:  #{itemContents.itemData.inCorrectItemFeedback}"/>
    </h:column>
  </h:dataTable>

