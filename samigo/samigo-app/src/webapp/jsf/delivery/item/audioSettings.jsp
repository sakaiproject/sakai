<f:verbatim>
  <PARAM NAME = NAME VALUE = "Record Audio" >
  <PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.5">
  <PARAM NAME = "scriptable" VALUE = "false">
  <PARAM NAME = "saveAu" VALUE="true">
  <PARAM NAME = "saveWave" VALUE="false">
  <PARAM NAME = "saveAiff" VALUE="false">
  <PARAM NAME = "saveToUrl" VALUE="</f:verbatim><h:outputText value="true" rendered="#{delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl'}"/><h:outputText value="false" rendered="#{delivery.actionString!='takeAssessment' && delivery.actionString!='takeAssessmentViaUrl'}"/><f:verbatim>">
  <PARAM NAME = "fileName" VALUE="audio_#{delivery.assessmentGrading.assessmentGradingId}">
  <PARAM NAME = "url" VALUE="</f:verbatim><h:outputText
     value="#{delivery.protocol}/samigo/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}" /><f:verbatim>">
  <PARAM NAME = "imageUrl" VALUE="</f:verbatim><h:outputText value="#{delivery.protocol}/samigo/images/" /><f:verbatim>">
  <PARAM NAME = "compression" VALUE="linear">
  <PARAM NAME = "frequency" VALUE="44100">
  <PARAM NAME = "bits" VALUE="16">
  <PARAM NAME = "signed" VALUE="true">
  <PARAM NAME = "bigendian" VALUE="true">
  <PARAM NAME = "stereo" VALUE="false">
  <PARAM NAME = "agentId" VALUE="</f:verbatim><h:outputText
     value="#{person.id}" escape="false"/><f:verbatim>">
  <PARAM NAME = "maxSeconds" VALUE="</f:verbatim><h:outputText
     value="#{question.duration}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsAllowed" VALUE="</f:verbatim><h:outputText
     value="#{question.triesAllowed}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsRemaining" VALUE="</f:verbatim><h:outputText
     value="#{question.attemptsRemaining}" escape="false"/><f:verbatim>">
  <PARAM NAME = "assessmentGrading" VALUE="</f:verbatim><h:outputText
     value="#{delivery.assessmentGrading.assessmentGradingId}" escape="false"/>
  <f:verbatim> "> </f:verbatim>