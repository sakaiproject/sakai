<%-- $Id: deliverFileUpload.jsp,v 1.36 2005/06/12 23:47:47 daisyf.stanford.edu Exp $
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>
<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:panelGroup>
  <h:outputText value="#{msg.file}" />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <corejsf:upload 
    target="/jsf/upload_tmp/assessment#{delivery.assessmentId}/question#{question.itemData.itemId}/#{backingbean.prop1}" 
    valueChangeListener="#{delivery.addMediaToItemGrading}" />
  <f:verbatim>&nbsp;&nbsp;</f:verbatim>
  <h:commandButton value="Upload" action="submit"/>
</h:panelGroup>
<f:verbatim><br /></f:verbatim>

      <%-- media list, note that question is ItemContentBean --%>

      <h:dataTable value="#{question.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}" target="new_window">
             <h:outputText escape="false" value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="("/>
         <h:outputText value="#{media.createdDate}">
           <f:convertDateTime pattern="MM/dd/yyyy" />
         </h:outputText>
         <h:outputText value=")"/>
        </h:column>
        <h:column>
          <h:commandLink action="confirmRemoveMedia" immediate="true">
            <h:outputText value="#{msg.remove}" />
            <f:param name="mediaId" value="#{media.mediaId}"/>
            <f:param name="mediaUrl" value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}"/>
            <f:param name="mediaFilename" value="#{media.filename}"/>
            <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.ConfirmRemoveMediaListener" />
          </h:commandLink>
        </h:column>
      </h:dataTable>

<h:selectBooleanCheckbox value="#{question.review}" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" id="mark_for_review" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}" 
  rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}" />

<h:panelGroup rendered="#{delivery.feedback eq 'true'}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel && question.feedback ne '' && question.feedback !=null}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false" />
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingComment ne '' && question.gradingComment != null}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}"
      escape="false" />
  </h:panelGroup>
</h:panelGroup>
