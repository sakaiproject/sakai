<p class="navIntraTool">
<h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminTemplate}">

<h:commandLink title="#{genMsg.t_assessment}" action="author" id="authorlink" immediate="true" rendered="#{authorization.adminAssessment}">
  <h:outputText id="myassessment" value="#{genMsg.assessment}"/>
  <f:actionListener
    type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
</h:commandLink>


<h:outputText value=" #{genMsg.separator} " rendered="#{authorization.adminAssessment}"/>


    <h:commandLink title="#{genMsg.t_template}" action="template" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{genMsg.template}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
    </h:commandLink>
    <h:outputText value=" #{genMsg.separator} " rendered="#{authorization.adminQuestionPool}"/>
    <h:commandLink title=" #{genMsg.t_questionPool} " action="poolList" immediate="true">
      <h:outputText value="#{genMsg.questionPool}" />
    </h:commandLink>
</h:panelGroup>
</p>