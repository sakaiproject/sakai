<%-- $Id: FillInTheBlank.jsp,v 1.8 2005/06/14 21:54:24 lydial.stanford.edu Exp $
include file for delivering fill in the blank questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
    </h:column>
  </h:dataTable>

<h:panelGrid columns="2" styleClass="longtext">
  <h:outputLabel value="#{msg.answerKey}: "/>
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
<samigo:dataLine value="#{itemText.answerArraySorted}" var="answer"
   separator=", " first="0" rows="100" >
  <h:column>
    <h:outputText value="#{answer.text}" />
  </h:column>
</samigo:dataLine>
    </h:column>
  </h:dataTable>

<h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{msg.preview_model_short_answer}: "/>
<h:outputText rendered="#{answer.text != null && answer.text ne ''}" escape="false" value="#{answer.text}" />
<h:outputLabel rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{msg.correctItemFeedback}: "/>
 <h:outputText rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{question.itemData.correctItemFeedback}" />

      <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{msg.incorrectItemFeedback}: "/>
<h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{question.itemData.inCorrectItemFeedback}"/>
</h:panelGrid>
