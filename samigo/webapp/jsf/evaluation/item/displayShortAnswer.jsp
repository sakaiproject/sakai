<%-- $Id: displayShortAnswer.jsp,v 1.4 2005/03/15 17:58:06 rgollub.stanford.edu Exp $
include file for displaying short answer essay questions
--%>
<h:outputText value="#{question.description}" escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{question.text}"  escape="false"/>

    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="answerKeyMC" value="#{msg.model} " />
    <h:outputText id="answerKeyMC" escape="false"
       value="#{question.key}"/>
    <f:verbatim></b></f:verbatim>


