<h:panelGroup rendered="#{questionScores.hasAssociatedRubric}" >
	<div id='<h:outputText value="#{description.assessmentGradingId}"/>-inputs'></div>
	<div id='<h:outputText value="modal#{description.assessmentGradingId}"/>' style="display:none;overflow:initial">
		<sakai-rubric-grading
			id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>'
			token='<h:outputText value="#{submissionStatus.rbcsToken}"/>'
			tool-id="sakai.samigo"
			entity-id='<h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>'
			evaluated-item-id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}" />'
			evaluated-item-owner-id='<h:outputText value="#{description.idString}" />'
			item-id='<h:outputText value="#{description.assessmentGradingId}"/>'
		</sakai-rubric-grading>
	</div>
</h:panelGroup>
