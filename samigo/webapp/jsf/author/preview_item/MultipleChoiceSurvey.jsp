<%-- $Id: MultipleChoiceSurvey.jsp,v 1.3 2005/04/12 20:35:29 rgollub.stanford.edu Exp $
include file for delivering multiple choice single correct survey questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <f:verbatim><br/></f:verbatim>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image2"
             alt="#{msg.not_correct}" url="/images/unchecked.gif" >
          </h:graphicImage>
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>

<f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" value="#{msg.generalItemFeedback}: " />
  <h:outputText rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}"
    value="#{question.itemData.generalItemFeedback}" escape="false" />

<f:verbatim><div class="longtext"></f:verbatim>

</h:column>
</h:dataTable>


