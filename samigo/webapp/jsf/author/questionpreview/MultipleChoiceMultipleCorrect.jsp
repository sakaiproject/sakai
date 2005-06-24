<%-- $Id$
include file for delivering multiple choice questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image1" rendered="#{answer.isCorrect}"
             alt="#{msg.correct}" url="/images/checked.gif" >
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{!answer.isCorrect}"
             alt="#{msg.not_correct}" url="/images/unchecked.gif" >
          </h:graphicImage>
          <h:outputText escape="false" value="#{answer.label}. #{answer.text}" />
        </h:column>
      </h:dataTable>

      <f:verbatim><br /></f:verbatim>
      <%-- answer --%>
      <h:outputText escape="false" value="#{msg.s_level_feedb}:" />
      <%-- answer level feedback --%>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputText escape="false" value="#{answer.label}. #{answer.generalAnswerFeedback}" />
        </h:column>
      </h:dataTable>

      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{msg.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.correct}:  #{itemContents.itemData.correctItemFeedback}" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.incorrect}:  #{itemContents.itemData.inCorrectItemFeedback}"/>
    </h:column>
  </h:dataTable>

