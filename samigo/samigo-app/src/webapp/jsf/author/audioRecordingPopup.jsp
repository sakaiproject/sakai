<%--
<!--
$Id: audioRecordingPopup.jsp 6643 2006-03-13 19:38:07Z hquinn@stanford.edu $
-->
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
--%>

<script type="text/javascript">
    $(document).ready(function() {
        var localeLanguage = <h:outputText value="'#{person.localeLanguage}'" escape="false"/>;
        var localeCountry = <h:outputText value="'#{person.localeCountry}'" escape="false"/>;
        var unlimitedString = <h:outputText value="'#{deliveryMessages.unlimited}'" escape="false"/>;
        var agentId = <h:outputText value="'#{person.id}'" escape="false"/>;
        var maxSeconds = parseInt(<h:outputText value="'#{question.duration}'" escape="false"/>);
        var attemptsAllowed = <h:outputText value="'#{question.triesAllowed}'" escape="false"/>;
        var attemptsRemaining = parseInt(<h:outputText value="'#{question.attemptsRemaining}'" escape="false"/>);
        var paramSeq = <h:outputText value="'#{param.sequence}'" escape="false"/>;
        var questionId = <h:outputText value="'#{question.itemData.itemId}'" escape="false"/>;
        var questionNumber = <h:outputText value="'#{question.number}'" escape="false"/>;
        var questionTotal = <h:outputText value="'#{part.questions}'" escape="false"/>;
        var assessmentGrading = <h:outputText value="'#{delivery.assessmentGrading.assessmentGradingId}'" escape="false"/>;
        var postUrl = <h:outputText value="'#{delivery.protocol}/samigo-app/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}_#{question.itemData.itemId}'" />;
        var deliveryProtocol = <h:outputText value="'#{delivery.protocol}'"/>;
        var messagesSecs = <h:outputText value="'#{deliveryMessages.secs}'"/>;
        var recordedOn = <h:outputText value="'#{deliveryMessages.recorded_on}'"/>;
        var dateFormat = <h:outputText value="'#{deliveryMessages.delivery_date_format}'"/>;

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
            recordedOn: recordedOn,
            dateFormat: dateFormat
        });
    });
</script>

<h:outputText escape="false" value="
<input type=\"hidden\" name=\"mediaLocation_#{question.itemData.itemId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au\"/>" />

<%@ include file="/jsf/delivery/item/audioObject.jsp" %>
