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

  <h:panelGroup layout="block" styleClass="audio-recorder-header">
    <h:outputFormat value="#{deliveryMessages.audio_recorder_header}" escape="false" />
  </h:panelGroup>

  <h:panelGroup layout="block" styleClass="audio-recorder-container" id="audio-recorder">

<h:panelGroup styleClass="sakai-recorder-error sak-banner-error" layout="block" style="display:none">
    <h:outputFormat value="#{deliveryMessages.audio_not_allowed} " escape="false"/> 
</h:panelGroup>

<f:verbatim><div class="audio-popup-question"></f:verbatim>
  <h:outputFormat value="#{deliveryMessages.q} " />
  <h:outputText value="#{question.number}" escape="false"/>
  <h:outputFormat value=" #{deliveryMessages.of} " /> 
  <h:outputText value="#{part.questions}" escape="false"/>
<f:verbatim></div></f:verbatim>

<h:panelGroup styleClass="audio-popup-question-text" layout="block">
  <h:outputText value="#{question.text} "  escape="false"/>
</h:panelGroup>

<f:verbatim><div class="audio-recorder-intro"></f:verbatim>
  <f:verbatim><div class="time-allowed"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_timelimit}" escape="false"> <f:param value="<span class=\"audio-time-allowed\"> </span>" /> </h:outputFormat>
  <f:verbatim></div></f:verbatim>
  <f:verbatim><div class="attempts-allowed"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_allowed} " escape="false"/> <f:verbatim><span class="audio-attempts-allowed"> </span></f:verbatim>
  <f:verbatim></div></f:verbatim>
  <f:verbatim><div class="attempts-remaining"></f:verbatim>
    <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_remaining} " escape="false"/> <f:verbatim><span class="audio-attempts"> </span></f:verbatim>
    <f:verbatim><span class="audio-last-attempt" style="display:none"></f:verbatim> <h:outputFormat value=" #{deliveryMessages.audio_recorder_last_attempt} " escape="false"/> <f:verbatim></span></f:verbatim>
  <f:verbatim></div></f:verbatim>
<f:verbatim></div></f:verbatim>

<h:panelGroup layout="block" styleClass="audio-browser-plea" style="display:none"><h:outputFormat value=" #{deliveryMessages.audio_recorder_browser_plea}" /></h:panelGroup>
<h:panelGroup layout="block" styleClass="audio-visual-container">
  <h:panelGroup layout="block" id="audio-analyzer" styleClass="audio-analyzer" />
  <h:panelGroup layout="block" id="playback-analyzer" styleClass="playback-analyzer" style="display:none" />

  <f:verbatim>
  <div class="audio-controls">
    <div class="audio-statusbar" style="display:none"> </div>
    <div class="audio-levelbar"> </div>
    <div class="audio-timer-wrapper">
      <span class="audio-timer">0:00</span>
      <div class="audio-scrubber"> </div>
      <span class="audio-max-time">30</span>
    </div>
    <button class="audio-record"></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_record}" /><f:verbatim></button>
    <button class="audio-stop" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_stop}" /><f:verbatim></button>
    <button class="audio-play" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_play}" /><f:verbatim></button>
    <button class="audio-upload" disabled></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_recorder_post}" /><f:verbatim></button>
  </div>

  <%-- SAM-2317 We're going to keep the mic check hidden until more browsers support it --%>
  <div class="audio-mic-check" style="display:none">
    <canvas class="volumemeter" width="16" height="54" style="background-color:#555;display:none"></canvas>
    <button class="mic-check" onclick="microphoneCheck(this)" disabled="disabled"> </button>
    <span></f:verbatim><h:outputFormat value=" #{deliveryMessages.audio_mic_check}" /><f:verbatim></span>
  </div></f:verbatim>
  </h:panelGroup>


  <f:verbatim>
  <audio class="audio-html5" style="display:none"> </audio>
  <div class="audio-debug-log" style="display:none">
    <h2>Log</h2>
    <pre class="log"></pre>
  </div>
  </f:verbatim>
  </h:panelGroup><!-- End audio-recorder-container -->
</h:panelGroup>
