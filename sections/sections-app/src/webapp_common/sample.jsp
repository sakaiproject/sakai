<f:view>
<h:form id="sampleForm">

    <x:aliasBean alias="#{viewName}" value="sample">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

	<!-- Manually initialize the bean (replace this with flowstate) -->
	<h:outputFormat value="#{sampleBean.configureBean}"/>

	<p>
		<h:outputText value="Username: #{sampleBean.userName}"/>
	</p>
	
	<p>
		<h:outputText value="Context: #{sampleBean.siteContext}"/>
	</p>

		<!-- Include this, since we don't have flowstate -->
		<h:inputHidden value="#{sampleBean.courseOfferingUuid}"/>	

	<p>
		<h:outputLabel for="title" value="#{msgs.sample_create_section}"/>
		<h:inputText id="title" value="#{sampleBean.title}"/>
		<h:selectOneMenu value="#{sampleBean.category}">
			<f:selectItems value="#{sampleBean.categoryItems}"/>
		</h:selectOneMenu>
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
			<h:column>
				<h:outputText value="#{section.categoryForDisplay}"/>
			</h:column>
			<h:column>
				<h:outputText value="#{section.meetingTimes}"/>
			</h:column>
		</h:dataTable>
	</p>
    
    <p>
        <h:commandLink action="overview" value="Overview"/>
    </p>

    <p>
        <h:commandLink action="studentView" value="Student View"/>
    </p>

</h:form>
</f:view>
