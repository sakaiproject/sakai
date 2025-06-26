<!-- $Id: eventLogHeading.jsp  2010-11-18 wang58@iupui.edu $

-->
<h:panelGroup>
    <ul class="navIntraTool actionToolbar" role="menu">
        <h:panelGroup rendered="#{authorization.createAssessment or authorization.editAnyAssessment or authorization.editOwnAssessment or authorization.gradeAnyAssessment or authorization.gradeOwnAssessment}">
            <li role="menuitem">
                <span>
                    <h:commandLink accesskey="#{generalMessages.a_assessment}" title="#{generalMessages.t_assessment}" action="author" id="authorlink" immediate="true">
                        <h:outputText value="#{generalMessages.assessment}" />
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
                    <h:commandLink accesskey="#{generalMessages.a_template}" title="#{generalMessages.t_template}" action="template" immediate="true">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
                        <h:outputText value="#{generalMessages.template}" />
                    </h:commandLink>
               </span>
           </li>
        </h:panelGroup>
        <h:panelGroup rendered="#{authorization.adminQuestionPool}">
            <li role="menuitem">
                <span>
                    <h:commandLink id="questionPoolsLink"  accesskey="#{generalMessages.a_pool}" title="#{generalMessages.t_questionPool}" action="poolList" immediate="true">
                        <h:outputText value="#{generalMessages.questionPool}" />
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
                    </h:commandLink>
                </span>
            </li>
        </h:panelGroup>
        <li role="menuitem">
            <span class="current">
                <h:outputText value="#{generalMessages.eventLog}" />
            </span>
        </li>
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
