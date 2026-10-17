<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!--
<!- -
$Id: audioRecordingPopup.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
- ->
***********************************************************************************
*
* Copyright (c) 2008, 2013 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
 -->
  <script>
    <jsp:text>
    $(document).ready(function() {
        var localeLanguage = </jsp:text>
    <h:outputText value="'#{person.localeLanguage}'" escape="false"/>
    <jsp:text>;
        var localeCountry = </jsp:text>
    <h:outputText value="'#{person.localeCountry}'" escape="false"/>
    <jsp:text>;
        var unlimitedString = </jsp:text>
    <h:outputText value="'#{deliveryMessages.unlimited}'" escape="false"/>
    <jsp:text>;
        var agentId = </jsp:text>
    <h:outputText value="'#{person.id}'" escape="false"/>
    <jsp:text>;
        var maxSeconds = parseInt(</jsp:text>
    <h:outputText value="'#{question.duration}'" escape="false"/>
    <jsp:text>);
        var attemptsAllowed = </jsp:text>
    <h:outputText value="'#{question.triesAllowed}'" escape="false"/>
    <jsp:text>;
        var attemptsRemaining = parseInt(</jsp:text>
    <h:outputText value="'#{question.attemptsRemaining}'" escape="false"/>
    <jsp:text>);
        var paramSeq = </jsp:text>
    <h:outputText value="'#{param.sequence}'" escape="false"/>
    <jsp:text>;
        var questionId = </jsp:text>
    <h:outputText value="'#{question.itemData.itemId}'" escape="false"/>
    <jsp:text>;
        var questionNumber = </jsp:text>
    <h:outputText value="'#{question.number}'" escape="false"/>
    <jsp:text>;
        var questionTotal = </jsp:text>
    <h:outputText value="'#{part.questions}'" escape="false"/>
    <jsp:text>;
        var assessmentGrading = </jsp:text>
    <h:outputText value="'#{delivery.assessmentGrading.assessmentGradingId}'" escape="false"/>
    <jsp:text>;
        var postUrl = </jsp:text>
    <h:outputText value="'#{delivery.protocol}/samigo-app/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}_#{question.itemData.itemId}'"/>
    <jsp:text>;
        var deliveryProtocol = </jsp:text>
    <h:outputText value="'#{delivery.protocol}'"/>
    <jsp:text>;
        var messagesSecs = </jsp:text>
    <h:outputText value="'#{deliveryMessages.secs}'"/>
    <jsp:text>;
        var recordedOn = </jsp:text>
    <h:outputText value="'#{deliveryMessages.recorded_on}'"/>
    <jsp:text>;

        var $elem = $(".audioRecordingPopup-" + questionId);
        var sakaiRecorder = new SakaiRecorder($elem);
        sakaiRecorder.init({
            localeLanguage: localeLanguage,
            localeCountry: localeCountry,
            unlimitedString: unlimitedString,
            agentId: agentId,
            maxSeconds: maxSeconds,
            attemptsAllowed: attemptsAllowed,
            attemptsRemaining: attemptsRemaining,
            paramSeq: paramSeq,
            questionId: questionId,
            questionNumber: questionNumber,
            questionTotal: questionTotal,
            assessmentGrading: assessmentGrading,
            postUrl: postUrl,
            deliveryProtocol: deliveryProtocol,
            messagesSecs: messagesSecs,
            recordedOn: recordedOn
        });
    });
</jsp:text>
  </script>
  <h:outputText escape="false" value="&#10;&lt;input type=&quot;hidden&quot; name=&quot;mediaLocation_#{question.itemData.itemId}&quot; value=&quot;jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au&quot;/&gt;"/>
  <jsp:directive.include file="/jsf/delivery/item/audioObject.jsp"/>
</jsp:root>
