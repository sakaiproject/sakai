<h:panelGroup rendered="#{questionScores.hasAssociatedRubric}" >
	<div id='<h:outputText value="#{description.assessmentGradingId}"/>-inputs'></div>
	<div id='<h:outputText value="modal#{description.assessmentGradingId}.#{questionScores.itemId}" />' style="display:none;overflow:initial">
		<sakai-rubric-grading
			id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>'
			site-id='<h:outputText value="#{questionScores.siteId}"/>'
			tool-id="sakai.samigo"
			entity-id='<h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>'
			evaluated-item-id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}" />'
			evaluated-item-owner-id='<h:outputText value="#{description.idString}" />'
			item-id='<h:outputText value="#{description.assessmentGradingId}"/>'
			<h:outputText value="enable-pdf-export" rendered="#{questionScores.enablePdfExport}"/>
		>
		</sakai-rubric-grading>
	</div>
</h:panelGroup>
