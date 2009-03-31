<!-- $Id: assessmentHeading.jsp 11254 2006-06-28 03:38:28Z daisyf@stanford.edu $
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
<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminQuestionPool or authorization.adminTemplate}">
    <h:commandLink accesskey="#{generalMessages.a_assessment}" title="#{generalMessages.t_assessment}" action="author" immediate="true" rendered="#{authorization.adminAssessment}">
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
	  <h:outputText value="#{generalMessages.assessment}" />
    </h:commandLink>
    <h:outputText value=" #{generalMessages.separator} " rendered="#{authorization.adminAssessment}"/>
    <h:commandLink accesskey="#{generalMessages.a_template}" title="#{generalMessages.t_template}" action="template" immediate="true" rendered="#{authorization.adminTemplate}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
      <h:outputText value="#{generalMessages.template}" />
    </h:commandLink>
    <h:outputText value=" #{generalMessages.separator} " rendered="#{authorization.adminTemplate}"/>
    <h:commandLink id="questionPoolsLink" accesskey="#{generalMessages.a_pool}" title="#{generalMessages.t_questionPool}" action="poolList" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.questionPool}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
    </h:commandLink>
  </h:panelGroup>
</p>
