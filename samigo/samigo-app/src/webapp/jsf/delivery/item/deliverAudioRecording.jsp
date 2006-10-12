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
<h:outputText escape="false" value="
<input type=\"hidden\" name=\"mediaLocation_#{question.itemData.itemId}\" value=\"jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.eid}/audio_#{delivery.assessmentGrading.assessmentGradingId}.au\"/>" />

<h:outputText value="#{question.text} "  escape="false"/>
<!-- ATTACHMENTS -->
<%@ include file="/jsf/delivery/item/attachment.jsp" %>

<f:verbatim><br /></f:verbatim>
<f:verbatim><br /></f:verbatim>

<h:panelGroup rendered="#{question!=null and question.mediaArray!=null}">
  <h:dataTable value="#{question.mediaArray}" var="media" cellpadding="10">
    <h:column>
      <h:outputText escape="false" value="
         <embed src=\"#{delivery.protocol}/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}\"
                volume=\"50\" height=\"25\" width=\"300\" autostart=\"false\"/>
         " />

      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{msg.open_bracket}"/>
      <h:outputText value="#{media.duration} sec, recorded on " rendered="#{!media.durationIsOver}" />
      <h:outputText value="#{question.duration} sec, recorded on " rendered="#{media.durationIsOver}" />
      <h:outputText value="#{media.createdDate}">
        <f:convertDateTime pattern="#{msg.delivery_date_format}" />
      </h:outputText>
      <h:outputText value="#{msg.close_bracket}"/>
      <f:verbatim><br /></f:verbatim>
 
      <div>
      <h:outputText value="#{msg.can_you_hear_1}"  escape="false"/>
      <h:outputLink value="#{delivery.protocol}/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}&setMimeType=false">
        <h:outputText value=" #{msg.can_you_hear_2} " escape="false" />
      </h:outputLink>
      <h:outputText value="#{msg.can_you_hear_3}"  escape="false"/>
      </div>
    </h:column>

    <h:column rendered="#{delivery.actionString=='takeAssessment' 
                        || delivery.actionString=='takeAssessmentViaUrl'}">
      <h:commandLink title="#{msg.t_removeMedia}" action="confirmRemoveMedia" immediate="true">
        <h:outputText value="   #{msg.remove}" />
        <f:param name="mediaId" value="#{media.mediaId}"/>
        <f:param name="mediaUrl" value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}"/>
        <f:param name="mediaFilename" value="#{media.filename}"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
      </h:commandLink>
    </h:column>
  </h:dataTable>
</h:panelGroup>

<%@ include file="/jsf/delivery/item/audioObject.jsp" %>
<%@ include file="/jsf/delivery/item/audioApplet.jsp" %>

<f:verbatim><br /></f:verbatim>

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
    <h:outputLabel for="feedSC" value="#{msg.feedback}#{msg.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}#{msg.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" />
  </h:panelGroup>
</h:panelGroup>

