<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <!-- $Id:
Headings for delivery pages, needs to have msg=DeliveryMessages.properties, etc.
 -->
  <!--

***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
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
  <!-- BEGIN OF TIMER  -->
  <h:panelGroup rendered="#{(delivery.actionString=='takeAssessment'&#13;&#10;    || delivery.actionString=='takeAssessmentViaUrl')&#13;&#10;    &amp;&amp; (delivery.hasTimeLimit || delivery.turnIntoTimedAssessment)}">
    <h:panelGroup rendered="#{delivery.hasTimeLimit}">
      <f:verbatim>
        <jsp:text><![CDATA[<span id="timer">]]></jsp:text>
      </f:verbatim>
      <f:verbatim>
        <jsp:text><![CDATA[ </span>]]></jsp:text>
      </f:verbatim>
    </h:panelGroup>
    <f:verbatim>
      <jsp:text><![CDATA[ <span id="bar">]]></jsp:text>
    </f:verbatim>
    <h:panelGroup rendered="#{(delivery.timeElapseAfterFileUpload == null || delivery.timeElapseDouble ge delivery.timeElapseAfterFileUploadDouble) &amp;&amp; delivery.hasTimeLimit == true}">
      <samigo:timerBar height="15" width="300" wait="#{delivery.timeLimit}" elapsed="#{delivery.timeElapse}" expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=10*'#{delivery.timeElapse}'; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; "/>
    </h:panelGroup>
    <h:panelGroup rendered="#{delivery.timeElapseAfterFileUpload != null &amp;&amp; delivery.timeElapseDouble lt delivery.timeElapseAfterFileUploadDouble  &amp;&amp; delivery.hasTimeLimit == true}">
      <samigo:timerBar height="15" width="300" wait="#{delivery.timeLimit}" elapsed="#{delivery.timeElapseAfterFileUpload}" expireScript="document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:elapsed'].value=10*'#{delivery.timeElapse}'; document.forms[0].elements['takeAssessmentForm:assessmentDeliveryHeading:outoftime'].value='true'; "/>
    </h:panelGroup>
    <f:verbatim>
      <jsp:text><![CDATA[  </span>]]></jsp:text>
    </f:verbatim>
    <!-- END OF TIMER  -->
    <h:inputHidden id="elapsed" value="#{delivery.timeElapse}"/>
    <h:inputHidden id="outoftime" value="#{delivery.timeOutSubmission}"/>
    <script>
      <jsp:text>&#13;
function isFromLink() {&#13;
  if (</jsp:text>
      <jsp:text>${delivery.actionMode}</jsp:text>
      <jsp:text> == 5) {&#13;
    return true;&#13;
  } else {&#13;
    return false;&#13;
  }&#13;
}&#13;
</jsp:text>
    </script>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.actionString=='previewAssessment' &amp;&amp; delivery.hasTimeLimit}">
    <f:verbatim>
      <jsp:text><![CDATA[<div class="sak-banner-info">]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{deliveryMessages.timer_preview_not_available}"/>
    <f:verbatim>
      <jsp:text><![CDATA[</div>]]></jsp:text>
    </f:verbatim>
  </h:panelGroup>
  <jsp:text>&#13;<![CDATA[
</p>]]>&#13;
</jsp:text>
</jsp:root>
