<%--
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
  <h:outputText escape="false" value="#{question.itemData.text}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
    <h:graphicImage id="image" alt="#{msg.audio_recording}."
       url="/images/recordresponse.gif" />

  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
  <h:outputText escape="false" value="#{msg.time_allowed_seconds} #{question.itemData.duration}" />
  <f:verbatim><br/>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>

  <h:outputText escape="false" value="#{msg.number_of_tries}: " />
  <h:panelGroup rendered="#{question.itemData.triesAllowed > 10}">
    <h:outputText escape="false" value="Unlimited" />
  </h:panelGroup>
  <h:panelGroup rendered="#{question.itemData.triesAllowed <= 10}">
    <h:outputText escape="false" value="#{question.itemData.triesAllowed}" />
  </h:panelGroup>

  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
          <h:outputText escape="false" value="#{msg.preview_model_short_answer}" />
          <h:outputText escape="false" value="#{answer.text}" />
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>

<f:verbatim> <div class="longtext"></f:verbatim>

 <h:outputText rendered="#{question.itemData.generalItemFeedback != null && question.itemData.generalItemFeedback ne ''}" escape="false" value="#{msg.general_fb}: " />
  <h:outputText escape="false" value="#{question.itemData.generalItemFeedback}" />
<f:verbatim> </div></f:verbatim>

