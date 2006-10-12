<h:panelGroup rendered="#{person.isMacNetscapeBrowser && delivery.actionString != 'reviewAssessment'}">
<f:verbatim>
<applet
  codebase = "/samigo/applets/"
  code = "org.sakaiproject.tool.assessment.audio.AudioRecorderApplet.class"
  archive = "sakai-samigo-audio-1.3-dev.jar"
  WIDTH = "500" HEIGHT = "350" ALIGN = "middle" VSPACE = "2" HSPACE = "2" >
</f:verbatim>

  <%@ include file="/jsf/delivery/item/audioSettings.jsp" %>

<f:verbatim>
</applet>
</f:verbatim>
</h:panelGroup>