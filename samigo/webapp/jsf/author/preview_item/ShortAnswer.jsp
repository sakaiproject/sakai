<%-- $Id: ShortAnswer.jsp,v 1.7 2005/04/07 21:24:42 rgollub.stanford.edu Exp $
include file for delivering short answer essay questions
should be included in file importing DeliveryMessages
--%>
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
        <f:verbatim><div class="longtext"></f:verbatim>  
<h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{msg.preview_model_short_answer}" />
          <f:verbatim><br/></f:verbatim>
          <h:outputText escape="false" value="#{answer.text}" />
<f:verbatim></div></f:verbatim> 
        </h:column>
      </h:dataTable>

     
    </h:column>
  </h:dataTable>

<f:verbatim> <div class="longtext"></f:verbatim>
<h:outputLabel rendered="#{answer.text != null && answer.text ne ''}" value="#{msg.preview_model_short_answer}: "/><f:verbatim><br/></f:verbatim>
<h:outputText rendered="#{answer.text != null && answer.text ne ''}" value="#{answer.text}" /><f:verbatim><br/></f:verbatim>
 <h:outputLabel rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" value="#{general_fb}: " />
  <h:outputText rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" value="#{question.itemData.generalItemFeedback}" />
<f:verbatim> </div></f:verbatim>
