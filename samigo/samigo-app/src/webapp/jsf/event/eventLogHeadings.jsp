<!-- $Id: eventLogHeading.jsp  2010-11-18 wang58@iupui.edu $

-->
<h:panelGroup rendered="#{authorization.adminQuestionPool or authorization.adminTemplate}">
<f:verbatim><ul class="navIntraTool actionToolbar" role="menu"> 
<li role="menuitem" class="firstToolBarItem"> <span></f:verbatim>
    <h:commandLink accesskey="#{generalMessages.a_assessment}" title="#{generalMessages.t_assessment}" action="author" id="authorlink" immediate="true" rendered="#{authorization.adminAssessment}">
       <h:outputText value="#{generalMessages.assessment}" />
       <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
    <h:commandLink accesskey="#{generalMessages.a_template}" title="#{generalMessages.t_template}" action="template" immediate="true" rendered="#{authorization.adminTemplate}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
      <h:outputText value="#{generalMessages.template}" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
    <h:commandLink id="questionPoolsLink"  accesskey="#{generalMessages.a_pool}" title="#{generalMessages.t_questionPool}" action="poolList" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.questionPool}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
      <h:outputText value="#{generalMessages.eventLog}" />
<f:verbatim></span></li>
</ul></f:verbatim>

</h:panelGroup>
