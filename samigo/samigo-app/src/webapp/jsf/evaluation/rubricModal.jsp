<h:panelGroup rendered="#{questionScores.hasAssociatedRubric}" >
	<div id='<h:outputText value="#{description.assessmentGradingId}"/>-inputs'></div>
	<div id='<h:outputText value="modal#{description.assessmentGradingId}"/>' style="display:none;overflow:initial">
		<sakai-rubric-grading
			id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>'
			token='<h:outputText value="#{submissionStatus.rbcsToken}"/>'
			toolId="sakai.samigo"
			entityId='<h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>'
			evaluatedItemId='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}" />'
			itemId='<h:outputText value="#{description.assessmentGradingId}"/>'
			<h:panelGroup rendered="#{question.rubricStateDetails != ''}">
				stateDetails='<h:outputText value="#{question.rubricStateDetails}"/>'
			</h:panelGroup>>
		</sakai-rubric-grading>
	</div>
</h:panelGroup>
