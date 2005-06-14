<%-- $Id: displayAudioRecording.jsp,v 1.2 2004/12/04 08:31:46 rgollub.stanford.edu Exp $
include file for displaying audio questions
--%>
<h:outputText value="#{question.description}" escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{question.text}"  escape="false"/>
  <h:graphicImage id="image1" alt="#{msg2.audio_recording}."
    url="/images/recordresponse.gif" />
