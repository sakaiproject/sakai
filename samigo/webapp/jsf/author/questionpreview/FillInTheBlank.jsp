<%-- $Id: FillInTheBlank.jsp,v 1.1 2005/02/03 03:08:48 zqingru.stanford.edu Exp $
include file for delivering fill in the blank questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{msg.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.correct}:  #{itemContents.itemData.correctItemFeedback}" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{msg.incorrect}:  #{itemContents.itemData.inCorrectItemFeedback}"/>
<%--
      <h:outputText escape="false" value="#{itemContents.itemData.generalItemFeedback}" />
--%>
    </h:column>
  </h:dataTable>

