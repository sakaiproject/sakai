<h:panelGroup rendered="#{person.isMacNetscapeBrowser}">
<f:verbatim>
<applet
  codebase = "/samigo/applets/"
  code = "org.sakaiproject.tool.assessment.audio.AudioRecorderApplet.class"
  archive = "sakai-samigo-audio-dev.jar"
  WIDTH = "500" HEIGHT = "350" ALIGN = "middle" VSPACE = "2" HSPACE = "2" >
</f:verbatim>

  <%@ include file="/jsf/delivery/item/audioSettings.jsp" %>

<f:verbatim>
</applet>
</f:verbatim>
</h:panelGroup>