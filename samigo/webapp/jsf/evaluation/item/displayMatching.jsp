<%-- $Id$
include file for displaying matching questions
--%>
<h:outputText value="#{question.description}" escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{question.text}"  escape="false"/>
<h:dataTable value="#{question.itemTextArraySorted}" var="itemText">
 <h:column>
   <h:outputText value="#{itemText.sequence}. #{itemText.text}" escape="false" />
   <h:dataTable value="#{itemText.answerArraySorted}" var="answer">
     <h:column>
      <h:graphicImage id="image4" rendered="#{answer.isCorrect}"
        alt="#{msg.correct}" url="/images/delivery/checkmark.gif" >
       </h:graphicImage>
      <h:graphicImage id="image5" rendered="#{!answer.isCorrect}"
        alt="#{msg.not_correct}" url="/images/delivery/spacer.gif" >
       </h:graphicImage>
     </h:column>
     <h:column>
       <h:outputText value="#{answer.text}" escape="false" />
     </h:column>
   </h:dataTable>
 </h:column>
</h:dataTable>

