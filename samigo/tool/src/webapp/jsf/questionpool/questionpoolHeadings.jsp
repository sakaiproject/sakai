<p class="navIntraTool" style="background-color:DDE3EB">
<h:panelGroup rendered="#{authorization.adminAssessment or authorization.adminTemplate}">

<h:commandLink action="author" id="authorlink" immediate="true" rendered="#{authorization.adminAssessment}">
  <h:outputText id="myassessment" value="#{msg.my_assessments}"/>
  <f:actionListener
    type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
</h:commandLink>


<h:outputText value=" | " rendered="#{authorization.adminAssessment}"/>


    <h:commandLink action="template" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{msg.my_templates}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{authorization.adminQuestionPool}"/>
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.qps}" />
    </h:commandLink>
</h:panelGroup>
</p>