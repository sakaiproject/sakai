<%-- $Id:
Headings for delivery pages, needs to have msg=DeliveryMessages.properties, etc.
--%>
<%-- TITLE --%>
<h3 style="insColor insBak">
   <h:outputText value="#{delivery.assessmentTitle}" />
</h3>
<%-- NAV BAR --%>
<p class="navIntraTool">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showImmediate &&
                            delivery.previewMode ne 'true'}">
    <h:commandLink action="takeAssessment" onmouseup="saveTime();" rendered="#{delivery.previewAssessment ne 'true'}" >
     <h:outputText value="#{msg.show_feedback}" />
     <f:param name="showfeedbacknow" value="true" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
    </h:commandLink>
    <h:commandLink action="takeAssessment" onmouseup="saveTime();" rendered="#{delivery.previewAssessment eq 'true'}" >
     <h:outputText value="#{msg.show_feedback}" />
     <f:param name="showfeedbacknow" value="true" />
     <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
    </h:commandLink>

    <h:outputText value=" | " 
      rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}"/>
  </h:panelGroup >
  <h:commandLink action="tableOfContents" onmouseup="saveTime();"
    rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'&& delivery.previewAssessment ne 'true'}">
    <h:outputText value="#{msg.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.SubmitToGradingActionListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandLink>
 <h:commandLink action="tableOfContents" onmouseup="saveTime();"
    rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'&& delivery.previewAssessment eq 'true'}">
    <h:outputText value="#{msg.table_of_contents}" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.DeliveryActionListener" />
  </h:commandLink>
  <h:commandLink action="select" rendered="#{delivery.previewMode eq 'true'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.select.SelectActionListener" />
    <h:outputText value="#{msg.button_return_select}" />
  </h:commandLink>
</p>

<h:outputText rendered="#{delivery.feedbackComponent.showGraderComment && delivery.previewMode eq 'true' && delivery.graderComment ne ''}" value="<b>#{msg.comments}</b> #{delivery.graderComment}" escape="false" />

<h:panelGroup rendered="#{delivery.previewMode eq 'false' && delivery.hasTimeLimit}" >
<f:verbatim><span id="remText"></f:verbatim><h:outputText value="Time Remaining: "/><f:verbatim></span></f:verbatim>
<f:verbatim><span id="timer"></f:verbatim><f:verbatim> </span></f:verbatim>

<f:verbatim> <span id="bar"></f:verbatim>
<samigo:timerBar height="15" width="300"
    wait="#{delivery.timeLimit}"
    elapsed="#{delivery.timeElapse}"
    expireMessage="Your session has expired."
    expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=loaded; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; document.forms[0].elements['takeAssessmentForm:saveAndExit'].click();" />

<f:verbatim>  </span></f:verbatim>
<h:commandButton type="button" onclick="document.getElementById('remText').style.display=document.getElementById('remText').style.display=='none' ? '': 'none';document.getElementById('timer').style.display=document.getElementById('timer').style.display=='none' ? '': 'none';document.getElementById('bar').style.display=document.getElementById('bar').style.display=='none' ? '': 'none'" value="Hide/Show Time Remaining" />
<h:inputHidden id="elapsed" value="#{delivery.timeElapse}" />
<h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
</h:panelGroup>

