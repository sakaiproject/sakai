<p class="navIntraTool" style="background-color:DDE3EB">

<h:commandLink action="author" id="authorlink" immediate="true">
  <h:outputText id="myassessment" value="#{msg.my_assessments}"/>
  <f:actionListener
    type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
</h:commandLink>


<h:outputText value=" | " />


    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.my_templates}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.qps}" />
    </h:commandLink>

</p>