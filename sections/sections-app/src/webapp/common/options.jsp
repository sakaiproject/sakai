<f:view>
<div class="portletBody">
<h:form id="optionsForm">

    <sakai:flowState bean="#{optionsBean}"/>

    <t:aliasBean alias="#{viewName}" value="options">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

        <h3><h:outputText value="#{msgs.options_page_header}"/></h3>
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
			<t:div styleClass="indent">
            	<h:selectBooleanCheckbox id="openSwitch" value="#{optionsBean.openSwitch}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
                <h:outputText value="#{msgs.section_open_info}"/>
                <h:inputText id="openDate" value="#{optionsBean.openDate}" disabled="#{optionsBean.confirmMode ||  ! optionsBean.sectionOptionsManagementEnabled}"/>
                <script src="/sakai-sections-tool/widget/datepicker/datepicker.js" type="text/javascript"></script>
                <img  id="calendar" onclick="javascript:var cal = new calendar2(document.getElementById('optionsForm:openDate'), cal_gen_date2_dm, cal_prs_date2_dm);cal.year_scroll = true;cal.time_comp = true;cal.popup('','/sakai-sections-tool/html/');"
                      width="16"  height="16"  style="cursor:pointer;"   src="/sakai-sections-tool/images/calendar/cal.gif"  border="0" />
			</t:div>
        </t:div>
    
        <t:div styleClass="act verticalPadding">
            <h:commandButton
                action="#{optionsBean.update}"
                value="#{msgs.update}"
                styleClass="active"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"
                disabled="#{optionsBean.confirmMode}" />
            <h:commandButton
                action="overview"
                value="#{msgs.cancel}"
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
