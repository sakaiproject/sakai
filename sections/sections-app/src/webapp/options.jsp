<f:view>
<div class="portletBody">
<h:form id="optionsForm">

    <script type="text/javascript">
        var button_ok = "<h:outputText value="#{msgs.confirm}"/>";
        var button_cancel = "<h:outputText value="#{msgs.cancel}"/>";
    </script>

    <sakai:flowState bean="#{optionsBean}"/>

    <t:aliasBean alias="#{viewName}" value="options">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

        <div class="page-header">
            <h1><h:outputText value="#{msgs.options_page_header}"/></h1>
        </div>
        <h4><h:outputText value="#{msgs.options_page_subheader}"/></h4>
        
        <%@ include file="/inc/globalMessages.jspf"%>
        
        <t:div rendered="#{optionsBean.confirmMode}" styleClass="validation">
        	<h:panelGrid columns="1">
	        	<h:outputText value="#{msgs.options_confirm}"/>
	        	<h:panelGroup>
		        	<h:commandButton action="#{optionsBean.confirmExternallyManaged}" value="#{msgs.options_automatically_manage}"/>
		        	<h:commandButton action="options" value="#{msgs.cancel}"/>
	        	</h:panelGroup>
        	</h:panelGrid>
        </t:div>

		<div id="dialog-confirm" title="<h:outputText value="#{msgs.options_manually_manage}"/>" class="displayNone">
			<p><span class="ui-icon ui-icon-alert dialogConfirm"></span><h:outputText value="#{msgs.options_confirmInternal}"/></p>
		</div>
		
		<t:selectOneRadio id="externallyManaged" layout="spread" value="#{optionsBean.management}"
			disabled="#{optionsBean.confirmMode}"
			onclick="updateOptionBoxes(this);">
			<f:selectItem itemValue="external" itemLabel=""/>
			<f:selectItem itemValue="internal" itemLabel=""/>
		</t:selectOneRadio>

        <t:div>
        	<h:panelGrid columns="2">
			<t:radio for="externallyManaged" index="0" rendered="#{optionsBean.managementToggleEnabled}" id="externallyManagedIndex0" />
			<h:outputLabel for="externallyManagedIndex0" value="#{msgs.options_externally_managed_description}"/>
        	</h:panelGrid>
        </t:div>

        <t:div>
        	<h:panelGrid columns="2">
			<t:radio for="externallyManaged" index="1" rendered="#{optionsBean.managementToggleEnabled}" id="externallyManagedIndex1" />
			<h:outputLabel for="externallyManagedIndex1" value="#{msgs.options_internally_managed_description}"/>
        	</h:panelGrid>
	        <t:div styleClass="indent">
	            <h:selectBooleanCheckbox id="selfRegister" value="#{optionsBean.selfRegister}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
	            <h:outputLabel for="selfRegister" value="#{msgs.options_self_register_label}"/>
	        </t:div>
	        <t:div styleClass="indent">
	            <h:selectBooleanCheckbox id="selfSwitch" value="#{optionsBean.selfSwitch}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
	            <h:outputLabel for="selfSwitch" value="#{msgs.options_self_switch_label}"/>
	        </t:div>
			<t:div styleClass="indent">
                <h:selectBooleanCheckbox id="openSwitch" value="#{optionsBean.openSwitch}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
                <h:outputLabel for="openSwitch" value="#{msgs.section_open_info}"/>
                <h:inputText id="openDate" value="#{optionsBean.openDate}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
			</t:div>
        </t:div>
    
        <t:div styleClass="act verticalPadding">
            <h:commandButton                
                value="#{msgs.update}"
                styleClass="active"
                rendered="#{optionsBean.sectionOptionsManagementEnabled and optionsBean.management == 'internal'}"
                disabled="#{optionsBean.confirmMode}"
                onclick="return proceedBasedOnManagement('internal');" />
            <h:commandButton                
                value="#{msgs.update}"
                styleClass="active"
                rendered="#{optionsBean.sectionOptionsManagementEnabled and optionsBean.management == 'external'}"
                disabled="#{optionsBean.confirmMode}"
                onclick="return proceedBasedOnManagement('external');" />
            <h:commandButton
                action="overview"
                value="#{msgs.cancel}"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"
                disabled="#{optionsBean.confirmMode}" />
            <h:commandButton
                action="overview"
                value="#{msgs.options_done}"
                rendered="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
            <h:commandButton
                id="updateSectionsButton"
                action="#{optionsBean.updateOptions}"
                value="#{msgs.confirm}"
                style="display:none" />
            <h:commandButton
                id="confirmExternallyManagedButton" 
                action="#{optionsBean.confirmExternallyManaged}"
                value="#{msgs.confirm}"
                style="display:none" />
        </t:div>

		<t:div style="height:340px">
                <script type="text/javascript">
                localDatePicker({
                    input: '#optionsForm\\:openDate',
                    useTime: 1,
                    parseFormat: 'YYYY-MM-DD HH:mm:ss',
                    val: '<h:outputText value="#{optionsBean.openDate}" />',
                    ashidden: { iso8601: 'openDateISO8601' }
                });
                </script>
		</t:div>

</h:form>
</div>
</f:view>
