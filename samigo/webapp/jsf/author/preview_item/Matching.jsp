<%-- $Id: Matching.jsp,v 1.14 2005/04/07 21:24:42 rgollub.stanford.edu Exp $
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>

  <h:outputText escape="false" value="#{question.instruction}" />
  <!-- 1. print out the matching choices -->
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer" 
         rendered="#{itemText.sequence==1}">
        <h:column>
            <h:panelGrid columns="2">
              <h:outputText escape="false" value="#{answer.label}. "/>
              <h:outputText escape="false" value="#{answer.text}" />
            </h:panelGrid>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

  <!-- 2. print out the matching text -->
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:panelGrid columns="2">  
        <h:selectOneMenu id="label" disabled="true">
          <f:selectItem itemValue="" itemLabel="select"/>
          <f:selectItem itemValue="" itemLabel="A"/>
          <f:selectItem itemValue="" itemLabel="B"/>
          <f:selectItem itemValue="" itemLabel="C"/>
        </h:selectOneMenu>
        <h:outputText escape="false" value="#{itemText.sequence}. #{itemText.text}" />

        <h:outputText value="" />
        <h:panelGroup rendered="#{answer.correctAnswerFeedback != null && answer.correctAnswerFeedback ne ''}" styleClass="longtext">
          <h:outputLabel value="#{msg.correct}:" />
          <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
              <h:outputText escape="false" value="#{answer.correctAnswerFeedback}" />
            </h:column>
          </h:dataTable>
        </h:panelGroup>

        <h:outputText value="" />
        <h:panelGroup rendered="#{answer.inCorrectAnswerFeedback != null && answer.inCorrectAnswerFeedback ne ''}" styleClass="longtext">
        <h:outputLabel value="#{msg.incorrect}:" />
          <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
              <h:outputText escape="false" value="#{answer.inCorrectAnswerFeedback}" />
            </h:column>
          </h:dataTable>
        </h:panelGroup>
      </h:panelGrid>
    </h:column>
  </h:dataTable>

      <%-- answer key --%>
 <h:panelGrid columns="2" styleClass="longtext">
      <h:outputLabel value="#{msg.answerKey}: "/>
      <h:outputText escape="false" value="#{question.itemData.answerKey}" />
     
      <h:outputLabel rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{msg.correct}:"/>
      <h:outputText rendered="#{question.itemData.correctItemFeedback != null && question.itemData.correctItemFeedback ne ''}" value="#{question.itemData.correctItemFeedback}" />
     
     <h:outputLabel rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{msg.incorrect}:"/>
      <h:outputText rendered="#{question.itemData.inCorrectItemFeedback != null && question.itemData.inCorrectItemFeedback ne ''}" value="#{question.itemData.inCorrectItemFeedback}"/>
</h:panelGrid>
