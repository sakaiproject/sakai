<h:panelGroup rendered="#{questionScores.hasAssociatedRubric}" >
	<div id='<h:outputText value="modal#{description.assessmentGradingId}"/>' style="display:none;overflow:initial">
		<sakai-rubric-grading
			id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>'
			tool-id="sakai.samigo"
			entity-id='<h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>'
			evaluated-item-id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}" />'
			item-id='<h:outputText value="#{description.assessmentGradingId}"/>'
			<h:panelGroup rendered="#{question.rubricStateDetails != ''}">
				state-details='<h:outputText value="#{question.rubricStateDetails}"/>'
			</h:panelGroup>>
		</sakai-rubric-grading>
	</div>
</h:panelGroup>