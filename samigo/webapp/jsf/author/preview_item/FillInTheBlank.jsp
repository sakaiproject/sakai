<%-- $Id: FillInTheBlank.jsp,v 1.7 2005/04/07 21:24:42 rgollub.stanford.edu Exp $
include file for delivering fill in the blank questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
    </h:column>
  </h:dataTable>
<h:panelGrid columns="2" styleClass="longtext">

<h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{msg.preview_model_short_answer}: "/>
<h:outputText rendered="#{answer.text != null && answer.text ne ''}" escape="false" value="#{answer.text}" />
<h:outputLabel rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{msg.correctItemFeedback}: "/>
 <h:outputText rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{question.itemData.correctItemFeedback}" />

      <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{msg.incorrectItemFeedback}: "/>
<h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{question.itemData.inCorrectItemFeedback}"/>
</h:panelGrid>