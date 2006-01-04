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
<h:inputHidden id="audioItemId" value="{question.itemData.itemId}" />
<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{msg.time_limit}: "  escape="false"/>
<h:outputText value="#{question.duration} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{msg.NoOfTries}: "  escape="false"/>
<h:outputText value="#{question.triesAllowed} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<f:verbatim><IFRAME></f:verbatim>
<%--   <%@ include file="/applets/audiorecorderjartest.html" %> --%>
<f:verbatim></IFRAME></f:verbatim>
<f:verbatim><br /></f:verbatim>

<h:commandButton value="Upload" action="submit"
    rendered="#{delivery.actionString=='takeAssessment'
             || delivery.actionString=='takeAssessmentViaUrl'}"/>

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

