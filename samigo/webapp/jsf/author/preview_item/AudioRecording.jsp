<%-- $Id: AudioRecording.jsp,v 1.7 2005/04/13 21:36:52 lydial.stanford.edu Exp $
include file for delivering audio questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
    <h:graphicImage id="image" alt="#{msg.audio_recording}."
       url="/images/recordresponse.gif" />

  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
  <h:outputText escape="false" value="#{msg.time_allowed_seconds} #{question.itemData.duration}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>

  <h:outputText escape="false" value="#{msg.number_of_tries}: " />
  <h:panelGroup rendered="#{question.itemData.triesAllowed > 10}">
    <h:outputText escape="false" value="Unlimited" />
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.triesAllowed <= 10}">
    <h:outputText escape="false" value="#{question.itemData.triesAllowed}" />
  </h:panelGroup>

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:outputText escape="false" value="#{msg.preview_model_short_answer}" />
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

<f:verbatim> <div class="longtext"></f:verbatim>

 <h:outputText rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" escape="false" value="#{msg.general_fb}: " />
  <h:outputText escape="false" value="#{question.itemData.generalItemFeedback}" />
<f:verbatim> </div></f:verbatim>

