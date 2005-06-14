<%-- $Id: deliverTrueFalse.jsp,v 1.22 2005/05/06 00:30:57 zqingru.stanford.edu Exp $
include file for delivering multiple choice single correct survey questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText value="#{question.text}"  escape="false"/>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
   <h:column rendered="#{delivery.feedback eq 'true' &&
           delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}">
      <h:dataTable value="#{question.answers}" var="answer">
        <h:column>
          <h:graphicImage id="image" rendered="#{answer.description eq 'true' && question.responseId eq answer.value}"
            alt="#{msg.correct}" url="/images/checkmark.gif" >
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{answer.description ne 'true' && question.responseId eq answer.value}
"
            width="16" height="16"
            alt="#{msg.not_correct}" url="/images/delivery/spacer.gif">
          </h:graphicImage>
       </h:column>
     </h:dataTable>
   </h:column>
   <h:column>
      <h:selectOneRadio id="question" value="#{question.responseId}" layout="pagedirection" disabled="#{delivery.previewMode eq 'true'}" >
        <f:selectItems value="#{question.answers}" />
      </h:selectOneRadio>
   </h:column>
  </h:dataTable>

  <h:panelGroup rendered="#{question.itemData.hasRationale}" >
    <f:verbatim><br /></f:verbatim>
    <h:outputLabel for="rationale" value="#{msg.rationale}" />
    <f:verbatim><br /></f:verbatim>
    <h:inputTextarea id="rationale" value="#{question.rationale}" rows="5" cols="40" disabled="#{delivery.previewMode eq 'true'}" />
  </h:panelGroup>

<h:selectBooleanCheckbox value="#{question.review}" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" id="mark_for_review" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}" 
  rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">

  <h:panelGroup rendered="#{delivery.feedbackComponent.showCorrectResponse && !delivery.noFeedback=='true'}" >
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{msg.ans_key}: " />
     <f:verbatim></b></f:verbatim>
    <h:outputText id="answerKeyMC" escape="false"
       value="#{question.key}"/>

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
