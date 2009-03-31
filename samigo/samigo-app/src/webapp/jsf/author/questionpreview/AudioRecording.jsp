<%-- $Id$
include file for delivering audio questions
should be included in file importing DeliveryMessages
--%>
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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

  <h:outputText escape="false" value="#{itemContents.itemData.text}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
  <h:outputLink
     value="javascript:window.open('/samigo/jsp/aam/applet/soundRecorder.jsp','ha_fullscreen','toolbar=no,location=no,directories=no,status=no,menubar=yes,'scrollbars=yes,resizable=yes,width=640,height=480');">
    <h:graphicImage id="image" alt="#{authorMessages.audio_recording}."
       url="/images/recordresponse.gif" />
  </h:outputLink>

  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
  <h:outputText escape="false" value="#{authorMessages.time_allowed_seconds} #{itemContents.itemData.duration}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>

  <h:outputText escape="false" value="#{authorMessages.number_of_tries}: " />
  <h:panelGroup rendered="#{itemContents.itemData.triesAllowed > 10}">
    <h:outputText escape="false" value="Unlimited" />
  </h:panelGroup>
  <h:panelGroup rendered="#{itemContents.itemData.triesAllowed <= 10}">
    <h:outputText escape="false" value="#{itemContents.itemData.triesAllowed}" />
  </h:panelGroup>

  <h:dataTable value="#{itemContents.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:outputText escape="false" value="#{authorMessages.preview_model_short_answer}" />
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>

      <%-- question level feedback --%>
      <h:outputText escape="false" value="#{authorMessages.q_level_feedb}:" />
      <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputText escape="false" value="#{itemContents.itemData.generalItemFeedback}" />
    </h:column>
  </h:dataTable>

