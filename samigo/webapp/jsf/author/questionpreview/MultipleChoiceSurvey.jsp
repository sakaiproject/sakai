<%-- $Id: MultipleChoiceSurvey.jsp,v 1.1 2005/02/03 03:08:48 zqingru.stanford.edu Exp $
include file for delivering multiple choice single correct survey questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <f:verbatim><br/></f:verbatim>
  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
        <h:column>
          <h:graphicImage id="image2"
             alt="#{msg.not_correct}" url="/images/unchecked.gif" >
          </h:graphicImage>
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>

      <f:verbatim><br /></f:verbatim>
 
      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{msg.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{itemContents.itemData.generalItemFeedback}" />
 
    </h:column>
  </h:dataTable>

