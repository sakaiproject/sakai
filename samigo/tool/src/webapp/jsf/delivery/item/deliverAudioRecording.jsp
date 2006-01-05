<%-- $Id$
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
<f:verbatim><br /></f:verbatim>

<%-- this invisible text is a trick to get the value set in the component tree
     without displaying it; audioMediaUploadPath will get this to the back end
--%>
<h:outputText id="audioMediaUploadPath" style="display:none"
escape="false"
value="/tmp/jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.id}/audio.au"
/>

<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputLabel value="#{msg.time_limit}: "  />
<h:outputText value="#{question.duration} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputLabel value="#{msg.NoOfTries}: " />
<h:outputText value="#{question.triesAllowed} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<f:verbatim><IFRAME></f:verbatim>
<%--   <%@ include file="/applets/audiorecorderjartest.html" %>
--%>
<f:verbatim></IFRAME></f:verbatim>
<f:verbatim><br /></f:verbatim>

<%-- debugging code, take this out later --%>
<h:commandButton value="Debug" type="submit"  >
  <f:actionListener
   type="org.sakaiproject.tool.assessment.ui.listener.delivery.AudioUploadActionListener" />
</h:commandButton>

<%-- delivery, actual upload action --%>
<h:commandButton value="Upload" type="submit"
    rendered="#{delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl'}" >
  <f:actionListener
   type="org.sakaiproject.tool.assessment.ui.listener.delivery.AudioUploadActionListener" />
</h:commandButton>

<%-- preview, simulated upload action --%>
<h:commandButton value="Upload" type="button"
  rendered="#{delivery.actionString=='previewAssessment'
                       || delivery.actionString=='reviewAssessment'
                       || delivery.actionString=='gradeAssessment'}" />

<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review"
   rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1' }" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}"
  rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" />
  </h:panelGroup>
</h:panelGroup>

