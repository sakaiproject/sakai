<%-- $Id: deliverShortAnswer.jsp,v 1.24 2005/05/13 23:43:05 zqingru.stanford.edu Exp $
include file for delivering short answer essay questions
should be included in file importing DeliveryMessages
--%>
<h:outputText value="#{question.text}"  escape="false"/>

<f:verbatim><br/></f:verbatim>
<h:inputTextarea rows="20" cols="80" value="#{question.responseText}" disabled="#{delivery.previewMode eq 'true'}" />

<f:verbatim><br /></f:verbatim>
<h:selectBooleanCheckbox value="#{question.review}" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" id="mark_for_review" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}"
  rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <f:verbatim><br /></f:verbatim>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{msg.model} " />
     <f:verbatim></b></f:verbatim>
    <h:outputLink  value="#" onclick="javascript:window.alert('#{question.key}');" >
    <h:outputText value="#{dmsg.click}" />
    </h:outputLink>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedback ne '' && question.feedback != null && !delivery.noFeedback=='true'}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingComment ne ''&& question.gradingComment != null && !delivery.noFeedback=='true'}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}"
      escape="false" />
  </h:panelGroup>
</h:panelGroup>
