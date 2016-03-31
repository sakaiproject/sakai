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
 <h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminQuestionPool or authorization.adminTemplate}">
<ul class="navIntraTool actionToolbar" role="menu">
  <h:panelGroup rendered="#{authorization.adminAssessment}">
    <li role="menuitem" class="firstToolBarItem">
      <span>
        <h:commandLink title="#{generalMessages.t_assessment}" action="author" immediate="true">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
          <h:outputText value="#{generalMessages.assessment}" />
        </h:commandLink>
      </span>
    </li>
  </h:panelGroup>
  <h:panelGroup rendered="#{authorization.adminTemplate and template.showAssessmentTypes}">
    <li role="menuitem">
      <span> 
        <h:commandLink title="#{generalMessages.t_template}" action="template" immediate="true">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{generalMessages.template}" />
        </h:commandLink>
      </span>
    </li>
  </h:panelGroup>
  <h:panelGroup rendered="#{authorization.adminQuestionPool}">
    <li role="menuitem">
      <span>
        <h:commandLink id="questionPoolsLink" title="#{generalMessages.t_questionPool}" action="poolList" immediate="true">
          <h:outputText value="#{generalMessages.questionPool}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
        </h:commandLink>
      </span>
    </li>
  </h:panelGroup>
  <h:panelGroup rendered="#{authorization.adminQuestionPool}">
    <li role="menuitem">
      <span>
        <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true">
          <h:outputText value="#{generalMessages.eventLog}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
        </h:commandLink>
      </span>
    </li>
  </h:panelGroup>
  <h:panelGroup rendered="#{authorization.adminQuestionPool}">
    <li role="menuitem">
      <span> 
        <h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true">
          <h:outputText value="#{generalMessages.section_activity}" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
        </h:commandLink>
      </span>
    </li> 
  </h:panelGroup>
</ul>
</h:panelGroup>
