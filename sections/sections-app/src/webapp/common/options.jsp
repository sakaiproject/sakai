<f:view>
<div class="portletBody">
<h:form id="optionsForm">

    <sakai:flowState bean="#{optionsBean}"/>

    <t:aliasBean alias="#{viewName}" value="options">
        <%@include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

        <h3><h:outputText value="#{msgs.options_page_header}"/></h3>
        <h4><h:outputText value="#{msgs.options_page_subheader}"/></h4>
        
        <%@include file="/inc/globalMessages.jspf"%>
        
        <t:div rendered="#{optionsBean.confirmMode}" styleClass="validation">
        	<h:panelGrid columns="1">
	        	<h:outputText value="#{msgs.options_confirm}"/>
	        	<h:panelGroup>
		        	<h:commandButton action="#{optionsBean.confirmExternallyManaged}" value="#{msgs.options_automatically_manage}"/>
		        	<h:commandButton action="options" value="#{msgs.options_cancel}"/>
	        	</h:panelGroup>
        	</h:panelGrid>
        </t:div>

		<t:selectOneRadio id="externallyManaged" layout="spread" value="#{optionsBean.management}"
			disabled="#{optionsBean.confirmMode}"
			onclick="updateOptionBoxes(this);">
			<f:selectItem itemValue="external" itemLabel="#{msgs.options_externally_managed_description}"/>
			<f:selectItem itemValue="internal" itemLabel="#{msgs.options_internally_managed_description}"/>
		</t:selectOneRadio>

        <t:div>
			<t:radio for="externallyManaged" index="0" rendered="#{optionsBean.managementToggleEnabled}" />
        </t:div>

        <t:div>
			<t:radio for="externallyManaged" index="1" rendered="#{optionsBean.managementToggleEnabled}" />
	        <t:div styleClass="indent">
	            <h:selectBooleanCheckbox id="selfRegister" value="#{optionsBean.selfRegister}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
	            <h:outputLabel for="selfRegister" value="#{msgs.options_self_register_label}"/>
	        </t:div>
	        <t:div styleClass="indent">
	            <h:selectBooleanCheckbox id="selfSwitch" value="#{optionsBean.selfSwitch}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
	            <h:outputLabel for="selfSwitch" value="#{msgs.options_self_switch_label}"/>
	        </t:div>
        </t:div>
    
        <t:div styleClass="act verticalPadding">
            <h:commandButton
                action="#{optionsBean.update}"
                value="#{msgs.options_update}"
                styleClass="active"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"
                disabled="#{optionsBean.confirmMode}" />
            <h:commandButton
                action="overview"
                value="#{msgs.options_cancel}"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"
                disabled="#{optionsBean.confirmMode}" />
            <h:commandButton
                action="overview"
                value="#{msgs.options_done}"
                rendered="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
        </t:div>

</h:form>
</div>
</f:view>
