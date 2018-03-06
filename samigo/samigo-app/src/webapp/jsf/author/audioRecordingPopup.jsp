<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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

<f:view>
	<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
		<head><%= request.getAttribute("html.head") %>
			<title><h:outputText value="#{assessmentSettingsMessages.audio_recording}" /></title>
			<samigo:script path="/../library/js/swfobject/swfobject.js"/>
			<samigo:script path="/../library/js/recorder/recorder.js"/>
			<samigo:script path="/../library/js/recorder/jRecorder.js"/>
			<samigo:script path="/../library/js/sakai-recorder.js"/>
			<script type="text/javascript">
				var userMediaSupport = true;
				var timer;
				var timeRemaining;
				var maxWidth;

				var audio_context;
				var recorder;

				<f:verbatim>var localeLanguage = "</f:verbatim><h:outputText value="#{person.localeLanguage}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var localeCountry = "</f:verbatim><h:outputText value="#{person.localeCountry}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var unlimitedString = "</f:verbatim><h:outputText value="#{deliveryMessages.unlimited}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var agentId = "</f:verbatim><h:outputText value="#{person.id}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var maxSeconds = parseInt(</f:verbatim><h:outputText value="#{param.duration}" escape="false"/><f:verbatim>);</f:verbatim>
				<f:verbatim>var attemptsAllowed = </f:verbatim><h:outputText value="#{param.triesAllowed}" escape="false"/><f:verbatim>;</f:verbatim>
				<f:verbatim>var attemptsRemaining = parseInt(</f:verbatim><h:outputText value="#{param.attemptsRemaining}" escape="false"/><f:verbatim>);</f:verbatim>
				<f:verbatim>var paramSeq = "</f:verbatim><h:outputText value="#{param.sequence}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var questionId = "</f:verbatim><h:outputText value="#{param.questionId}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var questionNumber = "</f:verbatim><h:outputText value="#{param.questionNumber}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var questionTotal = "</f:verbatim><h:outputText value="#{param.questionTotal}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var assessmentGrading = "</f:verbatim><h:outputText value="#{delivery.assessmentGrading.assessmentGradingId}" escape="false"/><f:verbatim>";</f:verbatim>
				<f:verbatim>var postUrl = "</f:verbatim><h:outputText value="#{delivery.protocol}/samigo-app/servlet/UploadAudio?media=jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{param.questionId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}_#{param.questionId}" /><f:verbatim>";</f:verbatim>

				$(document).ready(function() {
					$('#audio-popup-question-number').text(questionNumber);
					$('#audio-popup-question-total').text(questionTotal);
				});

			</script>
		</head>
		<body>
		    <h:outputText escape="false" value="
			<input type=\"hidden\" name=\"mediaLocation_#{param.questionId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{param.questionId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au\"/>" />

		    <%@ include file="/jsf/delivery/item/audioObject.jsp" %>
		</body>
	</html>
</f:view>
