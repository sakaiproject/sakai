<p class="navIntraTool">
  <h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminAssessment}">
   <h:commandLink action="author" id="authorlink" immediate="true" rendered="#{authorization.adminAssessment}">
      <h:outputText value="#{msg.link_assessments}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
    <h:outputText value=" | " rendered="#{authorization.adminAssessment}"/>
      <h:outputText value="#{msg.index_templates}" />
    <h:outputText value=" | " rendered="#{authorization.adminQuestionPool}"/>
    <h:commandLink action="poolList" id="poolLink" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{msg.link_pool}" />
    </h:commandLink>
  </h:panelGroup>
</p>
