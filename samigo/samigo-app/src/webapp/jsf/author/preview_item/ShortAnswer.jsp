<%-- $Id$
include file for delivering short answer essay questions
should be included in file importing DeliveryMessages
--%>
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

  <h:outputText escape="false" value="#{question.itemData.text}" />

  <h:dataTable value="#{question.itemData.itemAttachmentList}" var="attach">
    <h:column>
      <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
      <h:outputLink value="#{attach.location}" target="new_window">
        <h:outputText escape="false" value="#{attach.filename}" />
      </h:outputLink>
    </h:column>
  </h:dataTable>
  
  <h:dataTable value="#{question.itemData.itemTextArraySorted}" var="itemText">
    <h:column>
      <h:dataTable value="#{itemText.answerArray}" var="answer">
        <h:column>
       
<h:outputLabel rendered="#{answer.textIsNotEmpty}" value="#{msg.preview_model_short_answer}" />
          <f:verbatim><br/></f:verbatim>
          <h:outputText rendered="#{answer.textIsNotEmpty}" escape="false" value="#{answer.text}" />

        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>



<h:panelGroup rendered="#{question.itemData.generalItemFbIsNotEmpty && assessmentSettings.feedbackAuthoring ne '2' }">
  <h:outputLabel value="#{msg.feedback}: " />
  <h:outputText value="#{question.itemData.generalItemFeedback}" escape="false" />

</h:panelGroup>