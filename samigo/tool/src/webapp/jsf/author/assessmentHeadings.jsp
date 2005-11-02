<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminTemplate}">
      <h:outputText value="#{msg.global_nav_assessmt}"/>
    <h:outputText value=" | " />
    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </h:panelGroup>
</p>
