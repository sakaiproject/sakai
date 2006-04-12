<f:view>
<div class="portletBody">
<h:form id="optionsForm">

    <sakai:flowState bean="#{optionsBean}"/>

    <x:aliasBean alias="#{viewName}" value="options">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

        <h3><h:outputText value="#{msgs.options_page_header}"/></h3>
        <h4><h:outputText value="#{msgs.options_page_subheader}"/></h4>
        
        <%@include file="/inc/globalMessages.jspf"%>

        <x:div>
            <h:selectBooleanCheckbox id="selfRegister" value="#{optionsBean.selfRegister}" disabled="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
            <h:outputLabel for="selfRegister" value="#{msgs.options_self_register_label}"/>
        </x:div>
        
        <x:div>
            <h:selectBooleanCheckbox id="selfSwitch" value="#{optionsBean.selfSwitch}" disabled="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
            <h:outputLabel for="selfSwitch" value="#{msgs.options_self_switch_label}"/>
        </x:div>
    
        <x:div styleClass="act verticalPadding">
            <h:commandButton
                action="#{optionsBean.update}"
                value="#{msgs.options_update}"
                styleClass="active"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"/>
            <h:commandButton
                action="overview"
                value="#{msgs.options_cancel}"
                rendered="#{optionsBean.sectionOptionsManagementEnabled}"/>
            <h:commandButton
                action="overview"
                value="#{msgs.options_done}"
                rendered="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
        </x:div>

</h:form>
</div>
</f:view>
