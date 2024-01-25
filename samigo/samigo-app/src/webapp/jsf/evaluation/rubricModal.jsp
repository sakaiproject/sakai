<h:panelGroup rendered="#{questionScores.hasAssociatedRubric}" >
	<div id='<h:outputText value="#{description.assessmentGradingId}"/>-inputs'></div>
	<div id='<h:outputText value="modal#{description.assessmentGradingId}.#{questionScores.itemId}" />' style="display:none;overflow:initial">
		<h:panelGroup rendered="#{questionScores.associatedRubricType == '1'}" >
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
		</h:panelGroup>
		<h:panelGroup rendered="#{questionScores.associatedRubricType == '2'}" >
			<sakai-dynamic-rubric
				id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}-pub.#{questionScores.publishedId}.#{questionScores.itemId}.#{description.assessmentGradingId}"/>'
				grading-id='<h:outputText value="#{description.assessmentGradingId}.#{questionScores.itemId}"/>'
				entity-id='<h:outputText value="pub.#{questionScores.publishedId}.#{questionScores.itemId}"/>'
				site-id='<h:outputText value="#{questionScores.siteId}"/>'
				evaluated-item-owner-id='<h:outputText value="#{description.idString}" />'
				previous-grade='<h:outputText value="#{description.roundedTotalAutoScore}"/>'
				origin='questionScore'>
			</sakai-dynamic-rubric>
		</h:panelGroup>
	</div>
</h:panelGroup>
