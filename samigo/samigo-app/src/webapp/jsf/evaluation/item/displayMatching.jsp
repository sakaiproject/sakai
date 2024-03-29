<%-- $Id$
include file for displaying matching questions
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
   <h:outputText value="#{itemText.sequence}#{evaluationMessages.dot} #{itemText.text}" escape="false" />
   <h:dataTable value="#{itemText.answerArrayWithDistractorSorted}" var="answer">
     <h:column>
       <h:outputText value="" rendered="#{answer.isCorrect}" title="#{evaluationMessages.alt_correct}" styleClass="si si-check-lg" />
       <h:outputText value="" rendered="#{!answer.isCorrect}" title="#{evaluationMessages.alt_incorrect}" styleClass="si si-remove feedBackCross" />
     </h:column>
     <h:column>
		 <h:outputText value="#{evaluationMessages.none_above}" rendered="#{answer.text eq 'none_above'}" />	
		 <h:outputText value="#{answer.text}" escape="false" rendered="#{answer.text ne 'none_above'}" />
     </h:column>
   </h:dataTable>
 </h:column>
</h:dataTable>
<%@ include file="/jsf/evaluation/item/displayTags.jsp" %>

