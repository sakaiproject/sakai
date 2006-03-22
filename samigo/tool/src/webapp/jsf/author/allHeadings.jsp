<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminQuestionPool or authorization.adminTemplate}">
    <h:commandLink title="#{genMsg.t_assessment}" action="author" immediate="true" rendered="#{authorization.adminAssessment}">
      <h:outputText value="#{genMsg.assessment}" />
    </h:commandLink>
    <h:outputText value=" #{genMsg.separator} " rendered="#{authorization.adminAssessment}"/>
    <h:commandLink title="#{genMsg.t_template}" action="template" immediate="true" rendered="#{authorization.adminTemplate}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
      <h:outputText value="#{genMsg.template}" />
    </h:commandLink>
    <h:outputText value=" #{genMsg.separator} " rendered="#{authorization.adminTemplate}"/>
    <h:commandLink title="#{genMsg.t_questionPool}" action="poolList" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{genMsg.questionPool}" />
    </h:commandLink>
  </h:panelGroup>
</p>
