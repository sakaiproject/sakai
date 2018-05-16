<!--
* $Id: audioObject.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2005, 2006, 2013 The Sakai Foundation.
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
***********************************************************************************/
--%>
-->
<h:panelGroup rendered="#{delivery.actionString != 'reviewAssessment'}">

<f:verbatim><div id="audio-recorder-header"></f:verbatim> <h:outputFormat value="#{deliveryMessages.audio_recorder_header}" escape="false" /> <f:verbatim></div></f:verbatim>

<f:verbatim><div id="audio-recorder-container"></f:verbatim>

<f:verbatim><div id="sakai-recorder-error" style="display:none" class="messageSamigo"></f:verbatim>
    <h:outputFormat value="#{deliveryMessages.audio_not_allowed} " escape="false"/> 
<f:verbatim></div></f:verbatim>

<f:verbatim><div id="audio-popup-question"></f:verbatim>
  <h:outputFormat value="#{deliveryMessages.q} " />
  <f:verbatim> <span id="audio-popup-question-number">1</span> </f:verbatim> 
  <h:outputFormat value=" #{deliveryMessages.of} " /> 
  <f:verbatim> <span id="audio-popup-question-total">100</span> </f:verbatim>
<f:verbatim></div></f:verbatim>

<f:verbatim><div id="audio-recorder-intro"></f:verbatim>
  <f:verbatim><div class="time-allowed"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_timelimit}" escape="false"> <f:param value="<span id=\"audio-time-allowed\"> </span>" /> </h:outputFormat>
  <f:verbatim></div></f:verbatim>
  <f:verbatim><div class="attempts-allowed"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_allowed} " escape="false"/> <f:verbatim><span id="audio-attempts-allowed"> </span></f:verbatim>
  <f:verbatim></div></f:verbatim>
  <f:verbatim><div class="attempts-remaining"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_remaining} " escape="false"/> <f:verbatim><span id="audio-attempts"> </span></f:verbatim>
    <f:verbatim><span id="audio-last-attempt" style="display:none"></f:verbatim> <h:outputFormat value=" #{deliveryMessages.audio_recorder_last_attempt} " escape="false"/> <f:verbatim></span></f:verbatim>
  <f:verbatim></div></f:verbatim>
<f:verbatim></div></f:verbatim>

<f:verbatim>
<div id="audio-browser-plea" style="display:none"></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_browser_plea}" /><f:verbatim></div>
<div id="flashrecarea"> </div>
<div id="audio-visual-container">
  <canvas id="audio-analyzer"></canvas>

  <div id="audio-controls">
    <div id="audio-statusbar" style="display:none"> </div>
    <div id="audio-levelbar"> </div>
    <div id="audio-timer-wrapper">
      <span id="audio-timer">0:00</span>
      <div id="audio-scrubber"> </div>
      <span id="audio-max-time">30</span>
    </div>
    <button id="audio-record"></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_record}" /><f:verbatim></button>
    <button id="audio-stop" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_stop}" /><f:verbatim></button>
    <button id="audio-play" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_play}" /><f:verbatim></button>
    <button id="audio-upload" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_post}" /><f:verbatim></button>
  </div>

  <%-- SAM-2317 We're going to keep the mic check hidden until more browsers support it --%>
  <div id="audio-mic-check" style="display:none">
    <canvas id="volumemeter" width="16" height="54" style="background-color:#555;display:none"></canvas>
    <button id="mic-check" onclick="microphoneCheck(this)" disabled="disabled"> </button>
    <span></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_mic_check}" /><f:verbatim></span>
  </div>
</div>

  <audio id="audio-html5" style="display:none"> </audio>
  <div id="audio-debug-log" style="display:none">
    <h2>Log</h2>
    <pre id="log"></pre>
  </div>
</div><!-- End audio-recorder-container -->
</f:verbatim>
</h:panelGroup>
