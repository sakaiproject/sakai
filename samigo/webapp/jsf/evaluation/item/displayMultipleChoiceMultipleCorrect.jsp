<%-- $Id: displayMultipleChoiceMultipleCorrect.jsp,v 1.5 2005/02/01 18:42:42 rgollub.stanford.edu Exp $
include file for displaying multiple choice questions
--%>
  <h:outputText value="#{question.description}" escape="false"/>
  <f:verbatim><br /></f:verbatim>
  <h:outputText value="#{question.text}"  escape="false"/>
  <h:dataTable value="#{question.itemTextArray}" var="itemText">
   <h:column>
   <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
    <h:column>
      <h:graphicImage id="image6" rendered="#{answer.isCorrect}"
        alt="#{msg.correct}" url="/images/delivery/checkmark.gif" >
       </h:graphicImage>
      <h:graphicImage id="image7" rendered="#{!answer.isCorrect}"
        alt="#{msg.not_correct}" url="/images/delivery/spacer.gif" >
       </h:graphicImage>
    </h:column>
    <h:column>
      <h:outputText value="#{answer.label}" escape="false"
        rendered="#{question.hint == '***'}" />
    </h:column>
    <h:column><%-- checkbox or radio button, select answer --%>
      <h:selectManyCheckbox value="#{question.hint}" disabled="true"
          rendered="#{question.hint != '***'}">
        <f:selectItem itemLabel="#{answer.label}"
          itemValue="#{answer.sequence}"/>
      </h:selectManyCheckbox>
    </h:column>
    <h:column>
      <h:outputText value="#{answer.text}" escape="false" />
    </h:column>
   </h:dataTable>
   </h:column>
  </h:dataTable>

