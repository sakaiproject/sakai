<f:view>
	<h:form id="gbForm">
		<p>Pick one of the following gradebooks:</p>
		<h:dataTable id="table" value="#{selectGradebookBean.gradebooks}" var="gradebook">
			<h:column>
				<h:outputLink value="../entry">
					<h:outputText value="#{gradebook.name}"/>
					<f:param name="gradebookUid" value="#{gradebook.uid}"/>
				</h:outputLink>
			</h:column>
		</h:dataTable>
	</h:form>
</f:view>
