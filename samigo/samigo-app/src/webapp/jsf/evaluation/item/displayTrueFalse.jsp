<%-- $Id$
include file for displaying true false questions
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

  <h:outputText value="#{question.text}"  escape="false"/>
  <h:dataTable value="#{question.itemTextArraySorted}" var="itemText">
   <h:column>
   <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
    <h:column>
      <h:graphicImage id="image10" rendered="#{answer.isCorrect}"
        alt="#{evaluationMessages.alt_correct}" url="/images/delivery/checkmark.gif" >
       </h:graphicImage>
      <h:graphicImage id="image11" rendered="#{!answer.isCorrect}"
        alt=" " url="/images/crossmark.gif" >
       </h:graphicImage>
    </h:column>
    <h:column><%-- radio button, select answer --%>
      <h:selectOneRadio value="#{question.hint}" disabled="true"
        rendered="#{question.hint != '***'}">
        <f:selectItem itemLabel="#{answer.label} #{answer.sequence}"
          itemValue="#{answer.sequence}"/>
      </h:selectOneRadio>
    </h:column>
    <h:column>
      <%-- answer --%>
      <h:outputText value="#{evaluationMessages.true_msg}"
        rendered="#{answer.text=='true'}" />
      <h:outputText value="#{evaluationMessages.false_msg}"
        rendered="#{answer.text=='false'}" />
    </h:column>
   </h:dataTable>
   </h:column>
  </h:dataTable>
<%@ include file="/jsf/evaluation/item/displayTags.jsp" %>
