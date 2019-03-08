<div class="form-group row">
    <div class="col-sm-12">
        <sakai-rubric-association
            token='<h:outputText value="#{itemauthor.rbcsToken}" />'
            dont-associate-label='<h:outputText value="#{assessmentSettingsMessages.dont_associate_label} "/>'
            dont-associate-value="0"
            associate-label='<h:outputText value="#{assessmentSettingsMessages.associate_label} "/>'
            associate-value="1"
            tool-id="sakai.samigo"
            <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'AssessmentFacade' and itemauthor.itemId != ''}">
                entity-id='<h:outputText value="#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>'
            </h:panelGroup>
            <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'PublishedAssessmentFacade' and itemauthor.itemId != ''}">
                entity-id='<h:outputText value="pub.#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>'
            </h:panelGroup>
            <h:panelGroup rendered="#{itemauthor.rubricStateDetails != ''}">
                state-details='<h:outputText value="#{itemauthor.rubricStateDetails}"/>'
            </h:panelGroup>
            fine-tune-points='<h:outputText value="#{assessmentSettingsMessages.option_pointsoverride}"/>'
            hide-student-preview='<h:outputText value="#{assessmentSettingsMessages.option_studentpreview}"/>'
        </sakai-rubric-association>
    </div>
</div>
