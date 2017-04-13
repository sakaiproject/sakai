<%-- $Id$
include file for displaying short answer essay questions
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

<h:panelGrid columns="1" width="100%">	
<h:outputText value="#{question.text}"  escape="false"/>
<h:panelGroup rendered="#{questionScores.haveModelShortAnswer}">
<h:outputText value="#{evaluationMessages.model}"  escape="false"/>
<h:dataTable value="#{question.itemTextArray}" var="itemText">
    <h:column>
        <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
            <h:column>
                <h:outputText escape="false" value="#{answer.text}"/>
            </h:column>
        </h:dataTable>
    </h:column>
</h:dataTable>
</h:panelGroup>
</h:panelGrid>
<%@ include file="/jsf/evaluation/item/displayTags.jsp" %>


