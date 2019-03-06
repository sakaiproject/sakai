<div class="form-group row">
    <div class="col-sm-12">
        <sakai-rubric-association
            token='<h:outputText value="#{itemauthor.rbcsToken}" />'
            dontAssociateLabel='<h:outputText value="#{assessmentSettingsMessages.dont_associate_label} "/>'
            dontAssociateValue="0"
            associateLabel='<h:outputText value="#{assessmentSettingsMessages.associate_label} "/>'
            associateValue="1"
            toolId="sakai.samigo"
            <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'AssessmentFacade' and itemauthor.itemId != ''}">
                entityId='<h:outputText value="#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>'
            </h:panelGroup>
            <h:panelGroup rendered="#{assessmentBean.assessment['class'].simpleName == 'PublishedAssessmentFacade' and itemauthor.itemId != ''}">
                entityId='<h:outputText value="pub.#{assessmentBean.assessmentId}.#{itemauthor.itemId}"/>'
            </h:panelGroup>
            <h:panelGroup rendered="#{itemauthor.rubricStateDetails != ''}">
                stateDetails='<h:outputText value="#{itemauthor.rubricStateDetails}"/>'
            </h:panelGroup>
            fineTunePoints='<h:outputText value="#{assessmentSettingsMessages.option_pointsoverride}"/>'
            hideStudentPreview='<h:outputText value="#{assessmentSettingsMessages.option_studentpreview}"/>'
        </sakai-rubric-association>
    </div>
</div>
