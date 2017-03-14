<!--
* $Id$
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
<%-- $Id$
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>
<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:panelGroup>
  <h:outputText value="#{evaluationMessages.file}#{evaluationMessages.column} " />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <h:inputText size="50" />
  <h:outputText value="  " />
  <h:commandButton value="#{evaluationMessages.browse}" type="button"/>
  <h:outputText value="  " />
  <h:commandButton value="#{evaluationMessages.upload}" type="button"/>
  <%@ include file="/jsf/evaluation/item/displayTags.jsp" %>
</h:panelGroup>
<f:verbatim><br /></f:verbatim>
