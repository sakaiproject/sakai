<f:view>
<div class="portletBody">
<h:form id="optionsForm">

    <sakai:flowState bean="#{optionsBean}"/>

    <x:aliasBean alias="#{viewName}" value="options">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

        <h2><h:outputText value="#{msgs.options_page_header}"/></h2>
        <h4><h:outputText value="#{msgs.options_page_subheader}"/></h4>
        
        <%@include file="/inc/globalMessages.jspf"%>

        <h:selectBooleanCheckbox id="selfRegister" value="#{optionsBean.selfRegister}" disabled="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
        <h:outputLabel for="selfRegister" value="#{msgs.options_self_register_label}"/>
        
        <br/>
        
        <h:selectBooleanCheckbox id="selfSwitch" value="#{optionsBean.selfSwitch}" disabled="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>
        <h:outputLabel for="selfSwitch" value="#{msgs.options_self_switch_label}"/>
    
        <br/>
        
        <h:commandButton
            action="#{optionsBean.update}"
            value="#{msgs.options_update}"
            rendered="#{optionsBean.sectionOptionsManagementEnabled}"/>
    
        <h:commandButton
            action="overview"
            value="#{msgs.options_cancel}"
            rendered="#{optionsBean.sectionOptionsManagementEnabled}"/>

        <h:commandButton
            action="overview"
            value="#{msgs.options_done}"
            rendered="#{ ! optionsBean.sectionOptionsManagementEnabled}"/>

</h:form>
</div>
</f:view>
