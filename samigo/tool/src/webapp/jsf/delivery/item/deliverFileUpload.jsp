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
<%-- $Id$
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>

<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:panelGroup rendered="#{delivery.actionString=='takeAssessment' 
                       || delivery.actionString=='takeAssessmentViaUrl'}">
  <h:messages id="file_upload_error" layout="table" style="color:red"/>
  <h:outputText value="#{msg.file}" />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <corejsf:upload
    target="jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{person.id}"
    valueChangeListener="#{delivery.addMediaToItemGrading}" />
  <f:verbatim>&nbsp;&nbsp;</f:verbatim>
  <h:commandButton accesskey="#{msg.a_upload}" value="#{msg.upload}" action="#{delivery.getOutcome}" />
</h:panelGroup>

<h:panelGroup rendered="#{delivery.actionString=='previewAssessment' 
                       || delivery.actionString=='reviewAssessment' 
                       || delivery.actionString=='gradeAssessment'}">
  <h:outputText value="#{msg.file}" />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <h:inputText size="50" />
  <h:outputText value="  " />
  <h:commandButton accesskey="#{msg.a_browse}" value="#{msg.browse}" type="button"/>
  <h:outputText value="  " />
  <h:commandButton accesskey="#{msg.a_upload}" value="#{msg.upload}" type="button"/>
</h:panelGroup>

<f:verbatim><br /></f:verbatim>

      <%-- media list, note that question is ItemContentBean --%>
<h:panelGroup rendered="#{question!=null and question.mediaArray!=null}">
      <h:dataTable value="#{question.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}&sam_fileupload_siteId=#{delivery.siteId}" target="new_window">
             <h:outputText escape="false" value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="#{msg.open_bracket}"/>
         <h:outputText value="#{media.createdDate}">
           <f:convertDateTime pattern="#{msg.delivery_date_no_time_format}" />
         </h:outputText>
         <h:outputText value="#{msg.close_bracket}"/>
        </h:column>
        <h:column rendered="#{delivery.actionString=='takeAssessment' 
                           || delivery.actionString=='takeAssessmentViaUrl'}">
          <h:commandLink title="#{msg.t_removeMedia}" action="confirmRemoveMedia" immediate="true">
            <h:outputText value="#{msg.remove}" />
            <f:param name="mediaId" value="#{media.mediaId}"/>
            <f:param name="mediaUrl" value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}"/>
            <f:param name="mediaFilename" value="#{media.filename}"/>
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
          </h:commandLink>
        </h:column>
      </h:dataTable>
</h:panelGroup>

<h:selectBooleanCheckbox value="#{question.review}" id="mark_for_review" 
   rendered="#{delivery.actionString=='takeAssessment'
            || delivery.actionString=='takeAssessmentViaUrl'}" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}"
  rendered="#{(delivery.actionString=='takeAssessment'|| delivery.actionString=='takeAssessmentViaUrl')
            && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedbackIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingCommentIsNotEmpty}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}#{msg.column} " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}"
      escape="false" />
  </h:panelGroup>
</h:panelGroup>
