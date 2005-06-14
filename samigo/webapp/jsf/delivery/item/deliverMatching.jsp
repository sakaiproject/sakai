<%-- $Id: deliverMatching.jsp,v 1.20 2005/05/06 00:26:59 zqingru.stanford.edu Exp $
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText value="#{question.text}"  escape="false"/>
  <h:dataTable value="#{question.answers}" var="answer">
   <h:column>
     <h:outputText value="#{answer}" escape="false" />
   </h:column>
  </h:dataTable>
  <h:dataTable value="#{question.matchingArray}" var="matching"> 
    <h:column rendered="#{delivery.feedback eq 'true' &&
       delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}">
      <h:graphicImage id="image"
        rendered="#{matching.isCorrect}"
        alt="#{msg.correct}" url="/images/checkmark.gif" >
      </h:graphicImage>
      <h:graphicImage id="image2"
        rendered="#{matching.isCorrect}"
        width="16" height="16"
        alt="#{msg.not_correct}" url="/images/delivery/spacer.gif">
      </h:graphicImage>
   </h:column>
   <h:column>
    <h:selectOneMenu value="#{matching.response}"
      disabled="#{delivery.previewMode eq 'true'}" >
        <f:selectItems value="#{matching.choices}" />
    </h:selectOneMenu>
   </h:column>
   <h:column>
     <h:outputText value="#{matching.text}" escape="false"/>
     <h:panelGroup rendered="#{delivery.feedback eq 'true' &&
       delivery.feedbackComponent.showSelectionLevel}" >
       <f:verbatim><br /></f:verbatim>
       <h:outputText value="#{msg.feedback}: " rendered="#{matching.feedback ne ''}" />
       <h:outputText value="#{matching.feedback}" escape="false" />
     </h:panelGroup>
  </h:column>
  </h:dataTable>

<f:verbatim><br /></f:verbatim>
<h:selectBooleanCheckbox value="#{question.review}" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" id="mark_for_review" />
<h:outputLabel for="mark_for_review" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" value="#{msg.mark}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <f:verbatim><br /></f:verbatim>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{msg.ans_key}: " />
     <f:verbatim></b></f:verbatim>
    <h:outputText id="answerKeyMC"
       value="#{question.key}" escape="false" />
   
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedback ne '' && question.feedback != null && !delivery.noFeedback=='true'}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingComment ne '' && question.gradingComment != null && !delivery.noFeedback=='true'}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}"
      escape="false" />
  </h:panelGroup>
</h:panelGroup>
