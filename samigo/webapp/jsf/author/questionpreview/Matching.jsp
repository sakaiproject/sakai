<%-- $Id: Matching.jsp,v 1.2 2005/02/15 18:36:34 zqingru.stanford.edu Exp $
include file for delivering matching questions
should be included in file importing DeliveryMessages
--%>

  <h:outputText escape="false" value="#{itemContents.instruction}" />
  <!-- 1. print out the matching choices -->
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
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
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
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
        <h:panelGroup>
          <h:outputText value="Correct Feedback:" />
          <h:dataTable value="#{itemText.answerArray}" var="answer">
            <h:column>
              <h:outputText escape="false" value="#{answer.correctAnswerFeedback}" />
            </h:column>
          </h:dataTable>
        </h:panelGroup>

        <h:outputText value="" />
        <h:panelGroup>
          <h:outputText value="Incorrect Feedback:" />
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

      <h:outputText escape="false" value="Answer Key: #{itemContents.itemData.answerKey}" />
      <f:verbatim><br/></f:verbatim>
      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{msg.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.correct}:  #{itemContents.itemData.correctItemFeedback}" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.incorrect}:  #{itemContents.itemData.inCorrectItemFeedback}"/>

