<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminQuestionPool or authorization.adminTemplate}">
    <h:commandLink action="author" immediate="true"  rendered="#{authorization.adminAssessment}">
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
      <h:outputText value="#{msg.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{authorization.adminTemplate}" />
    <h:commandLink action="template" immediate="true" rendered="#{authorization.adminTemplate}">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | "  rendered="#{authorization.adminQuestionPool}"/>
    <h:commandLink action="poolList" immediate="true"  rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </h:panelGroup>
</p>
