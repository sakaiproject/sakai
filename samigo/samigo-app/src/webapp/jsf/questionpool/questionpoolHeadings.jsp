<!-- $Id: questionpoolHeadings.jsp 7641 2006-04-11 21:38:28Z hquinn@stanford.edu $
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
<h:panelGroup>
    <ul class="navIntraTool actionToolbar" role="menu">
        <h:panelGroup rendered="#{authorization.createAssessment or authorization.editAnyAssessment or authorization.editOwnAssessment or authorization.gradeAnyAssessment or authorization.gradeOwnAssessment}">
            <li role="menuitem">
                <span>
                    <h:commandLink title="#{generalMessages.t_assessment}" action="author" id="authorlink" immediate="true">
                        <h:outputText value="#{generalMessages.assessment}"/>
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetResultsCalculatedListener" />
                    </h:commandLink>
                </span>
            </li>
        </h:panelGroup>
        <h:panelGroup rendered="#{authorization.createAssessment}">
            <li role="menuitem">
                <span>
                    <h:commandLink title="#{generalMessages.add}" action="#{author.getOutcome}" immediate="true">
                        <f:param name="action" value="create_assessment_title" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorAssessmentListener" />
                        <h:outputText value="#{generalMessages.add}" />
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
        <li role="menuitem">
            <span class="current">
                <h:commandLink title=" #{generalMessages.t_questionPool} " action="poolList" immediate="true">
                    <h:outputText value="#{generalMessages.questionPool}" />
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
                </h:commandLink>
            </span>
        </li>
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
        <h:panelGroup rendered="#{authorization.adminAssessment}">
            <li role="menuitem">
                <span>
                    <h:commandLink id="restoreAssessments" accesskey="#{generalMessages.a_restore_assessments}" title="#{generalMessages.t_restore_assessments}" action="restoreAssessments" immediate="true">
                        <h:outputText value="#{generalMessages.restore_assessments}" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RestoreAssessmentsListener" />
                    </h:commandLink>
                </span>
            </li>
        </h:panelGroup>
    </ul>
</h:panelGroup>
