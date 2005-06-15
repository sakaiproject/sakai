<%-- $Id: displayShortAnswer.jsp,v 1.5 2005/06/15 00:07:59 zqingru.stanford.edu Exp $
include file for displaying short answer essay questions
--%>
<h:outputText value="#{question.description}" escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{question.text}"  escape="false"/>
