<f:view>
<h:form id="sampleForm">

	<!-- Manually initialize the bean (replace this with flowstate) -->
	<h:outputFormat value="#{sampleBean.configureBean}"/>

	<p>
		<h:outputText value="#{msgs.sample_create_section}"/>
		<h:inputText value="#{sampleBean.title}"/>
		<h:commandButton value="Create Section" actionListener="#{sampleBean.processCreateSection}"/>
	</p>
	
	<p>
		<h:outputText value="#{msgs.sample_section_list}"/>
		<h:dataTable value="#{sampleBean.sections}" var="section">
			<h:column>
				<h:outputText value="#{section.uuid}"/>
			</h:column>
			<h:column>
				<h:outputText value="#{section.title}"/>
			</h:column>
		</h:dataTable>
	</p>
	
</h:form>
</f:view>
