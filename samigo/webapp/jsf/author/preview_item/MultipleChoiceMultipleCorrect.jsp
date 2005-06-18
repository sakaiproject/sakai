<%-- $Id: MultipleChoiceMultipleCorrect.jsp,v 1.14 2005/04/12 20:35:29 rgollub.stanford.edu Exp $
include file for delivering multiple choice questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image1" rendered="#{answer.isCorrect}"
             alt="#{msg.correct}" url="/images/checked.gif" >
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{!answer.isCorrect}"
             alt="#{msg.not_correct}" url="/images/unchecked.gif" >
          </h:graphicImage>
          <h:outputText escape="false" value="#{answer.label}. #{answer.text}" /> </h:column><h:column>
       <f:verbatim><b></f:verbatim>  <h:outputText rendered="#{answer.generalAnswerFeedback != null && answer.generalAnswerFeedback ne ''}" value="#{          msg.feedback}: " /> <f:verbatim></b></f:verbatim>

 <h:outputText value="#{answer.generalAnswerFeedback}" />
        </h:column>
      </h:dataTable>

    </h:column>
  </h:dataTable>

<h:panelGrid columns="2" styleClass="longtext">
  <h:outputLabel value="#{msg.answerKey}: "/>
  <h:outputText value="#{question.itemData.answerKey}" />

<%-- OLD ANSWER KEY
<h:panelGrid columns="2" styleClass="longtext">
<h:outputLabel value="#{msg.answerKey}: "/>
   <h:panelGroup>
    <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer2">
        <h:column>
          <h:outputText rendered="#{answer2.isCorrect}" escape="false" value="#{answer2.label}" />
        </h:column>
      </h:dataTable>
        </h:column>
      </h:dataTable>
</h:panelGroup>

 END OLD ANSWER KEY --%>

     <h:outputLabel rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{msg.correctItemFeedback}: "/>
     <h:outputText rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}"
       value="#{question.itemData.correctItemFeedback}" escape="false" />

     <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{msg.incorrectItemFeedback}: "/>
     <h:outputText  rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}"
       value="#{question.itemData.inCorrectItemFeedback}" escape="false" />

</h:panelGrid>

