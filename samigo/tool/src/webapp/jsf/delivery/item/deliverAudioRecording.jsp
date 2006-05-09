<%-- $Id$
include file for delivering audio questions
should be included in file importing DeliveryMessages
--%>
<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
<f:verbatim><br /></f:verbatim>

<%-- this invisible text is a trick to get the value set in the component tree
     without displaying it; audioMediaUploadPath will get this to the back end
--%>
<h:outputText escape="false" value="
<input type=\"hidden\" name=\"mediaLocation_#{question.itemData.itemId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.id}/audio_#{delivery.timeStamp}.au\"/>" />

<h:outputText value="#{question.text} "  escape="false"/>

<h:panelGroup rendered="#{question!=null and question.mediaArray!=null}">
  <h:dataTable value="#{question.mediaArray}" var="media" cellpadding="10">
    <h:column>
      <h:outputText escape="false" value="
         <embed src=\"/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}\" 
                volume=\"50\" height=\"25\" width=\"300\" autostart=\"false\"/>
         " />
      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{msg.open_bracket}"/>
      <h:outputText value="#{media.duration} sec, recorded on " />
      <h:outputText value="#{media.createdDate}">
        <f:convertDateTime pattern="#{msg.delivery_date_format}" />
      </h:outputText>
      <h:outputText value="#{msg.close_bracket}"/>
    </h:column>
    <h:column rendered="#{delivery.actionString=='takeAssessment' 
                        || delivery.actionString=='takeAssessmentViaUrl'}">
      <h:commandLink title="#{msg.t_removeMedia}" action="confirmRemoveMedia" immediate="true">
        <h:outputText value="   #{msg.remove}" />
        <f:param name="mediaId" value="#{media.mediaId}"/>
        <f:param name="mediaUrl" value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}"/>
        <f:param name="mediaFilename" value="#{media.filename}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
      </h:commandLink>
    </h:column>
  </h:dataTable>
</h:panelGroup>

<f:verbatim>
<object
  classid = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93"
  codebase = "http://java.sun.com/update/1.5.0/jinstall-1_5-windows-i586.cab#Version=1,5,0,0"
  WIDTH = "500" HEIGHT = "350" NAME = "Test Audio Applet" ALIGN = "middle" VSPACE = "2" HSPACE = "2" >
  <PARAM NAME = CODE VALUE = "org.sakaiproject.tool.assessment.audio.AudioRecorderApplet.class" >
  <PARAM NAME = ARCHIVE VALUE = "sakai-samigo-audio-dev.jar" >
  <PARAM NAME = CODEBASE VALUE = "/samigo/applets/" >
  <PARAM NAME = NAME VALUE = "Record Audio" >
  <PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.5">
  <PARAM NAME = "scriptable" VALUE = "false">
  <PARAM NAME = "enablePlay" VALUE="true">
  <PARAM NAME = "enableRecord" VALUE="true">
  <PARAM NAME = "enablePause" VALUE="true">
  <PARAM NAME = "enableLoad" VALUE="false">
  <PARAM NAME = "saveAu" VALUE="true">
  <PARAM NAME = "saveWave" VALUE="false">
  <PARAM NAME = "saveAiff" VALUE="false">
  <PARAM NAME = "saveToFile" VALUE="false">
  <PARAM NAME = "saveToUrl" VALUE="true">
  <PARAM NAME = "fileName" VALUE="audio">
  <PARAM NAME = "url" VALUE="</f:verbatim><h:outputText
     value="#{delivery.protocol}/samigo/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.id}/audio_#{delivery.timeStamp}.au" /><f:verbatim>">
  <PARAM NAME = "compression" VALUE="linear">
  <PARAM NAME = "frequency" VALUE="44100">
  <PARAM NAME = "bits" VALUE="16">
  <PARAM NAME = "signed" VALUE="true">
  <PARAM NAME = "bigendian" VALUE="true">
  <PARAM NAME = "stereo" VALUE="false">
  <PARAM NAME = "maxSeconds" VALUE="</f:verbatim><h:outputText
     value="#{question.duration}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsAllowed" VALUE="</f:verbatim><h:outputText
     value="#{question.triesAllowed}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsRemaining" VALUE="</f:verbatim><h:outputText
     value="#{question.attemptsRemaining}" escape="false"/><f:verbatim>">
  <PARAM NAME = "assessmentGrading" VALUE="</f:verbatim><h:outputText
     value="#{delivery.assessmentGrading.assessmentGradingId}" escape="false"/><f:verbatim>">
  <comment>
   <embed
      type = "application/x-java-applet;version=1.5" \
      CODE = "org.sakaiproject.tool.assessment.audio.AudioRecorderApplet.class" \
      JAVA_CODEBASE = "/samigo/applets/" \
      ARCHIVE = "sakai-samigo-audio-dev.jar"
      NAME = "Record Audio" \
      WIDTH = "500" \
      HEIGHT = "350" \
      ALIGN = "middle" \
      VSPACE = "2" \
      HSPACE = "2" \
      enablePlay ="true" \
      enableRecord ="true" \
      enablePause ="true" \
      enableLoad ="false" \
      saveAu ="true" \
      saveWave ="false" \
      saveAiff ="false" \
      saveToFile ="false" \
      saveToUrl ="true" \
      fileName ="audio" \
      url ="</f:verbatim><h:outputText
     value="#{delivery.protocol}/samigo/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.id}/audio_#{delivery.timeStamp}.au" /><f:verbatim>" \
      compression ="linear" \
      frequency ="44100" \
      bits ="16" \
      signed ="true" \
      bigendian ="true6" \
      stereo ="false" \
      maxSeconds ="</f:verbatim><h:outputText
         value="#{question.duration}" escape="false"/><f:verbatim>" \
      attemptsAllowed ="</f:verbatim><h:outputText
         value="#{question.triesAllowed}" escape="false"/><f:verbatim>" \
      attemptsRemaining ="</f:verbatim><h:outputText
         value="#{question.attemptsRemaining}" escape="false"/><f:verbatim>" \
      aassessmentGrading ="</f:verbatim><h:outputText
         value="#{delivery.assessmentGrading.assessmentGradingId}" escape="false"/><f:verbatim>" \
      scriptable = false
      pluginspage = "http://java.sun.com/products/plugin/index.html#download">
      <noembed>

      </noembed>
   </embed>
  </comment>
</object>

</f:verbatim>
<f:verbatim><br /></f:verbatim>

<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review"
   rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1' }" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}"
  rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}#{msg.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}#{msg.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" />
  </h:panelGroup>
</h:panelGroup>

