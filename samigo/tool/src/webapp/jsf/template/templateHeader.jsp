   <p class="navIntraTool">
   <h:commandLink action="author" id="authorlink" immediate="true">
      <h:outputText value="#{msg.link_assessments}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
    <h:outputText value=" | " />
      <h:outputText value="#{msg.index_templates}" />
    <h:outputText value=" | " />
    <h:commandLink action="poolList" id="poolLink" immediate="true">
      <h:outputText value="#{msg.link_pool}" />
    </h:commandLink>
   </p>
