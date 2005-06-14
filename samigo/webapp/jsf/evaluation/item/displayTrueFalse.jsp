<%-- $Id: displayTrueFalse.jsp,v 1.6 2005/03/09 19:53:12 rgollub.stanford.edu Exp $
include file for displaying true false questions
--%>
  <h:outputText value="#{question.description}" escape="false"/>
  <f:verbatim><br /></f:verbatim>
  <h:outputText value="#{question.text}"  escape="false"/>
  <h:dataTable value="#{question.itemTextArraySorted}" var="itemText">
   <h:column>
   <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
    <h:column>
      <h:graphicImage id="image10" rendered="#{answer.isCorrect}"
        alt="#{msg.correct}" url="/images/delivery/checkmark.gif" >
       </h:graphicImage>
      <h:graphicImage id="image11" rendered="#{!answer.isCorrect}"
        alt="#{msg.not_correct}" url="/images/delivery/spacer.gif" >
       </h:graphicImage>
    </h:column>
    <h:column><%-- radio button, select answer --%>
      <h:selectOneRadio value="#{question.hint}" disabled="true"
        rendered="#{question.hint != '***'}">
        <f:selectItem itemLabel="#{answer.label} #{answer.sequence}"
          itemValue="#{answer.sequence}"/>
      </h:selectOneRadio>
    </h:column>
    <h:column>
      <%-- answer --%>
      <h:outputText value="#{msg.true_msg}"
        rendered="#{answer.text=='true'}" />
      <h:outputText value="#{msg.false_msg}"
        rendered="#{answer.text=='false'}" />
    </h:column>
   </h:dataTable>
   </h:column>
  </h:dataTable>
