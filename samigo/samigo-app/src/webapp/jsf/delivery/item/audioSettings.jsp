<!--
* $Id: audioSettings.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
***********************************************************************************/
--%>
-->
<f:verbatim>
  <PARAM NAME = NAME VALUE = "Record Audio" >
  <PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.5">
  <PARAM NAME = "scriptable" VALUE = "true">
  <PARAM NAME = "mayscript" VALUE = "true">
  <PARAM NAME = "saveAu" VALUE="true">
  <PARAM NAME = "saveWave" VALUE="false">
  <PARAM NAME = "saveAiff" VALUE="false">
  <PARAM NAME = "saveToUrl" VALUE="</f:verbatim><h:outputText value="true" rendered="#{delivery.actionString=='takeAssessment' || delivery.actionString=='takeAssessmentViaUrl'}"/><h:outputText value="false" rendered="#{delivery.actionString!='takeAssessment' && delivery.actionString!='takeAssessmentViaUrl'}"/><f:verbatim>">
  <PARAM NAME = "fileName" VALUE="audio_#{delivery.assessmentGrading.assessmentGradingId}">
  <PARAM NAME = "url" VALUE="</f:verbatim><h:outputText
     value="#{delivery.protocol}/samigo/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{param.questionId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}" /><f:verbatim>">
  <PARAM NAME = "imageUrl" VALUE="</f:verbatim><h:outputText value="#{delivery.protocol}/samigo/images/" /><f:verbatim>">
  <PARAM NAME = "compression" VALUE="linear">
  <PARAM NAME = "frequency" VALUE="16000">
  <PARAM NAME = "bits" VALUE="8">
  <PARAM NAME = "signed" VALUE="true">
  <PARAM NAME = "bigendian" VALUE="true">
  <PARAM NAME = "stereo" VALUE="false">
  <PARAM NAME = "localeLanguage" VALUE="</f:verbatim><h:outputText
     value="#{person.localeLanguage}" escape="false"/><f:verbatim>">
  <PARAM NAME = "localeCountry" VALUE="</f:verbatim><h:outputText
     value="#{person.localeCountry}" escape="false"/><f:verbatim>">
  <PARAM NAME = "agentId" VALUE="</f:verbatim><h:outputText
     value="#{person.id}" escape="false"/><f:verbatim>">
  <PARAM NAME = "maxSeconds" VALUE="</f:verbatim><h:outputText
     value="#{param.duration}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsAllowed" VALUE="</f:verbatim><h:outputText
     value="#{param.triesAllowed}" escape="false"/><f:verbatim>">
  <PARAM NAME = "attemptsRemaining" VALUE="</f:verbatim><h:outputText
     value="#{param.attemptsRemaining}" escape="false"/><f:verbatim>">
  <PARAM NAME = "questionId" VALUE="</f:verbatim><h:outputText
	 value="#{param.questionId}" escape="false"/><f:verbatim>">
  <PARAM NAME = "assessmentGrading" VALUE="</f:verbatim><h:outputText
     value="#{delivery.assessmentGrading.assessmentGradingId}" escape="false"/>
  <f:verbatim> "> </f:verbatim>