<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminTemplate}">
      <h:outputText value="#{msg.global_nav_assessmt}"/>
    <h:outputText value=" | " rendered="#{authorization.adminTemplate}"/>
    <h:commandLink action="template" immediate="true" rendered="#{authorization.adminTemplate}">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{authorization.adminQuestionPool}"/>
    <h:commandLink action="poolList" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </h:panelGroup>
</p>
