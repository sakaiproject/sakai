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
* $Id: audioObject.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $

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

 -->
  <h:panelGroup rendered="#{delivery.actionString != 'reviewAssessment'}">
    <h:panelGroup layout="block" styleClass="audio-recorder-header">
      <h:outputFormat value="#{deliveryMessages.audio_recorder_header}" escape="false"/>
    </h:panelGroup>
    <h:panelGroup layout="block" styleClass="audio-recorder-container" id="audio-recorder">
      <h:panelGroup styleClass="sakai-recorder-error sak-banner-error" layout="block" style="display:none">
        <h:outputFormat value="#{deliveryMessages.audio_not_allowed} " escape="false"/>
      </h:panelGroup>
      <f:verbatim>
        <jsp:text><![CDATA[<div class="audio-popup-question">]]></jsp:text>
      </f:verbatim>
      <h:outputFormat value="#{deliveryMessages.q} "/>
      <h:outputText value="#{question.number}" escape="false"/>
      <h:outputFormat value=" #{deliveryMessages.of} "/>
      <h:outputText value="#{part.questions}" escape="false"/>
      <f:verbatim>
        <jsp:text><![CDATA[</div>]]></jsp:text>
      </f:verbatim>
      <h:panelGroup styleClass="audio-popup-question-text" layout="block">
        <h:outputText value="#{question.text} " escape="false"/>
      </h:panelGroup>
      <f:verbatim>
        <jsp:text><![CDATA[<div class="audio-recorder-intro">]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[<div class="time-allowed">]]></jsp:text>
      </f:verbatim>
      <h:outputFormat value=" #{deliveryMessages.audio_recorder_timelimit}" escape="false">
        <jsp:text> </jsp:text>
        <f:param value="&lt;span class=&quot;audio-time-allowed&quot;&gt; &lt;/span&gt;"/>
        <jsp:text> </jsp:text>
      </h:outputFormat>
      <f:verbatim>
        <jsp:text><![CDATA[</div>]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[<div class="attempts-allowed">]]></jsp:text>
      </f:verbatim>
      <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_allowed} " escape="false"/>
      <jsp:text> </jsp:text>
      <f:verbatim>
        <jsp:element name="span">
          <jsp:attribute name="class" trim="false">audio-attempts-allowed</jsp:attribute>
          <jsp:body>
            <jsp:text> </jsp:text>
          </jsp:body>
        </jsp:element>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[</div>]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[<div class="attempts-remaining">]]></jsp:text>
      </f:verbatim>
      <h:outputFormat value=" #{deliveryMessages.audio_recorder_attempts_remaining} " escape="false"/>
      <jsp:text> </jsp:text>
      <f:verbatim>
        <jsp:element name="span">
          <jsp:attribute name="class" trim="false">audio-attempts</jsp:attribute>
          <jsp:body>
            <jsp:text> </jsp:text>
          </jsp:body>
        </jsp:element>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[<span class="audio-last-attempt" style="display:none">]]></jsp:text>
      </f:verbatim>
      <jsp:text> </jsp:text>
      <h:outputFormat value=" #{deliveryMessages.audio_recorder_last_attempt} " escape="false"/>
      <jsp:text> </jsp:text>
      <f:verbatim>
        <jsp:text><![CDATA[</span>]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[</div>]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[</div>]]></jsp:text>
      </f:verbatim>
      <h:panelGroup layout="block" styleClass="audio-browser-plea" style="display:none">
        <h:outputFormat value=" #{deliveryMessages.audio_recorder_browser_plea}"/>
      </h:panelGroup>
      <h:panelGroup layout="block" styleClass="audio-visual-container">
        <h:panelGroup layout="block" id="audio-analyzer" styleClass="audio-analyzer"/>
        <h:panelGroup layout="block" id="playback-analyzer" styleClass="playback-analyzer" style="display:none"/>
        <f:verbatim>
          <jsp:text><![CDATA[
  <div class="audio-controls">
    ]]></jsp:text>
          <jsp:element name="div">
            <jsp:attribute name="class" trim="false">audio-statusbar</jsp:attribute>
            <jsp:attribute name="style" trim="false">display:none</jsp:attribute>
            <jsp:body>
              <jsp:text> </jsp:text>
            </jsp:body>
          </jsp:element>
          <jsp:element name="div">
            <jsp:attribute name="class" trim="false">audio-levelbar</jsp:attribute>
            <jsp:body>
              <jsp:text> </jsp:text>
            </jsp:body>
          </jsp:element>
          <div class="audio-timer-wrapper">
            <span class="audio-timer">
              <jsp:text>0:00</jsp:text>
            </span>
            <jsp:element name="div">
              <jsp:attribute name="class" trim="false">audio-scrubber</jsp:attribute>
              <jsp:body>
                <jsp:text> </jsp:text>
              </jsp:body>
            </jsp:element>
            <span class="audio-max-time">
              <jsp:text>30</jsp:text>
            </span>
          </div>
          <jsp:text><![CDATA[
    <button class="audio-record">]]></jsp:text>
        </f:verbatim>
        <h:outputFormat value=" #{deliveryMessages.audio_recorder_record}"/>
        <f:verbatim>
          <jsp:text><![CDATA[</button>
    <button class="audio-stop" disabled>]]></jsp:text>
        </f:verbatim>
        <h:outputFormat value=" #{deliveryMessages.audio_recorder_stop}"/>
        <f:verbatim>
          <jsp:text><![CDATA[</button>
    <button class="audio-play" disabled>]]></jsp:text>
        </f:verbatim>
        <h:outputFormat value=" #{deliveryMessages.audio_recorder_play}"/>
        <f:verbatim>
          <jsp:text><![CDATA[</button>
    <button class="btn btn-primary text-white audio-upload" disabled>]]></jsp:text>
        </f:verbatim>
        <h:outputFormat value=" #{deliveryMessages.audio_recorder_post}"/>
        <f:verbatim>
          <jsp:text><![CDATA[</button>
  </div>

  ]]></jsp:text>
          <!-- SAM-2317 We're going to keep the mic check hidden until more browsers support it  -->
          <jsp:text><![CDATA[
  <div class="audio-mic-check" style="display:none">
    ]]></jsp:text>
          <jsp:element name="canvas">
            <jsp:attribute name="class" trim="false">volumemeter</jsp:attribute>
            <jsp:attribute name="width" trim="false">16</jsp:attribute>
            <jsp:attribute name="height" trim="false">54</jsp:attribute>
            <jsp:attribute name="style" trim="false">background-color:#555;display:none</jsp:attribute>
            <jsp:body>
            </jsp:body>
          </jsp:element>
          <jsp:element name="button">
            <jsp:attribute name="class" trim="false">mic-check</jsp:attribute>
            <jsp:attribute name="onclick" trim="false">microphoneCheck(this)</jsp:attribute>
            <jsp:attribute name="disabled" trim="false">disabled</jsp:attribute>
            <jsp:body>
              <jsp:text> </jsp:text>
            </jsp:body>
          </jsp:element>
          <jsp:text><![CDATA[
    <span>]]></jsp:text>
        </f:verbatim>
        <h:outputFormat value=" #{deliveryMessages.audio_mic_check}"/>
        <f:verbatim>
          <jsp:text><![CDATA[</span>
  </div>]]></jsp:text>
        </f:verbatim>
      </h:panelGroup>
      <f:verbatim>
        <jsp:element name="audio">
          <jsp:attribute name="class" trim="false">audio-html5</jsp:attribute>
          <jsp:attribute name="style" trim="false">display:none</jsp:attribute>
          <jsp:body>
            <jsp:text> </jsp:text>
          </jsp:body>
        </jsp:element>
        <div class="audio-debug-log" style="display:none">
          <h2>
            <jsp:text>Log</jsp:text>
          </h2>
          <jsp:element name="pre">
            <jsp:attribute name="class" trim="false">log</jsp:attribute>
            <jsp:body>
            </jsp:body>
          </jsp:element>
        </div>
      </f:verbatim>
    </h:panelGroup>
    <!-- End audio-recorder-container  -->
  </h:panelGroup>
</jsp:root>
