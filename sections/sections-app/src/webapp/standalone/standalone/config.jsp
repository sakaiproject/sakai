<f:view>
<div class="portletBody">
<h:form id="configForm">

        <h3><h:outputText value="Test mode configuration"/></h3>

		<t:div>
			<h:commandLink action="#{configBean.updateConfigAllManual}">
				<h:outputText value="All Manual, All The Time"/>
			</h:commandLink>
		</t:div>
		
		<t:div>
			<h:commandLink action="#{configBean.updateConfigAllAutomatic}">
				<h:outputText value="All Automatic, All The Time"/>
			</h:commandLink>
		</t:div>

		<t:div>
			<h:commandLink action="#{configBean.updateConfigOptionalAll}">
				<h:outputText value="Optional auto/manual, creates sections for all rosters"/>
			</h:commandLink>
		</t:div>

		<t:div>
			<h:commandLink action="#{configBean.updateConfigOptionalMultiple}">
				<h:outputText value="Optional auto/manual, creates sections for multiple rosters"/>
			</h:commandLink>
		</t:div>

</h:form>
</div>
</f:view>
