<!-- $Id: eventLogHeading.jsp  2010-11-18 wang58@iupui.edu $

-->
<h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminTemplate}">
  <ul class="navIntraTool actionToolbar" role="menu">
    <h:panelGroup rendered="#{authorization.adminAssessment}">
      <li role="menuitem" class="firstToolBarItem">
        <span>
          <h:commandLink accesskey="#{generalMessages.a_assessment}" title="#{generalMessages.t_assessment}" action="author" id="authorlink" immediate="true">
            <h:outputText value="#{generalMessages.assessment}" />
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
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
  </ul>
</h:panelGroup>
