<%-- $Id: TrueFalse.jsp,v 1.1 2005/02/03 03:08:48 zqingru.stanford.edu Exp $
include file for delivering true false questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image1" rendered="#{answer.isCorrect}"
             alt="#{msg.correct}" url="/images/radiochecked.gif" >
          </h:graphicImage>
          <h:graphicImage id="image2" rendered="#{!answer.isCorrect}"
             alt="#{msg.not_correct}" url="/images/radiounchecked.gif" >
          </h:graphicImage>
          <h:outputText value="#{answer.text}" />
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

