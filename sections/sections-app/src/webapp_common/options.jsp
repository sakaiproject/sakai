<f:view>
<h:form id="optionsForm">

    <sakai:flowState bean="#{optionsBean}"/>

    <x:aliasBean alias="#{viewName}" value="options">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <x:div styleClass="portletBody">
        <h2><h:outputText value="#{msgs.options_page_header}"/></h2>
        <h4><h:outputText value="#{msgs.options_page_subheader}"/></h4>
        
        <%@include file="/inc/globalMessages.jspf"%>

        <h:selectBooleanCheckbox id="selfRegister" value="#{optionsBean.selfRegister}"/>
        <h:outputLabel for="selfRegister" value="#{msgs.options_self_register_label}"/>
        
        <br/>
        
        <h:selectBooleanCheckbox id="selfSwitch" value="#{optionsBean.selfSwitch}"/>
        <h:outputLabel for="selfSwitch" value="#{msgs.options_self_switch_label}"/>
    
        <br/>
        
        <h:commandButton
            action="#{optionsBean.update}"
            value="#{msgs.options_update}"/>
    
        <h:commandButton
            action="overview"
            value="#{msgs.options_cancel}"/>
    </x:div>
</h:form>
</f:view>
