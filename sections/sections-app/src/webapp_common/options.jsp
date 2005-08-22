<f:view>
<h:form id="memberForm">

    <x:aliasBean alias="#{viewName}" value="options">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{optionsBean}"/>

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

</h:form>
</f:view>
