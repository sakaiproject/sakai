<f:view>
<h:form id="addSectionsForm">

    <x:aliasBean alias="#{viewName}" value="addSections">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{addSectionsBean}"/>
    
    <h:selectOneMenu
        id="numToAdd"
        value="#{addSectionsBean.numToAdd}"
        valueChangeListener="#{addSectionsBean.processChangeSections}"
        onchange="this.form.submit()">
        <f:selectItem itemValue="1"/>
        <f:selectItem itemValue="2"/>
        <f:selectItem itemValue="3"/>
        <f:selectItem itemValue="4"/>
        <f:selectItem itemValue="5"/>
        <f:selectItem itemValue="6"/>
        <f:selectItem itemValue="7"/>
        <f:selectItem itemValue="8"/>
        <f:selectItem itemValue="9"/>
        <f:selectItem itemValue="10"/>
    </h:selectOneMenu>
    
    <h:selectOneMenu
        id="category"
        value="#{addSectionsBean.category}"
        valueChangeListener="#{addSectionsBean.processChangeSections}"
        onchange="this.form.submit()">
        <f:selectItem itemLabel="#{msgs.add_sections_select_one}" itemValue=""/>
        <f:selectItems value="#{addSectionsBean.categoryItems}"/>
    </h:selectOneMenu>
    
    <x:dataTable
        id="sectionsTable"
        value="#{addSectionsBean.sections}"
        var="section"
        binding="#{addSectionsBean.sectionTable}"/>

    <h:commandButton
        action="#{addSectionsBean.addSections}"
        disabled="#{addSectionsBean.category == null}"
        value="#{msgs.add_sections_add}"/>
    
    <h:commandButton action="overview" value="#{msgs.add_sections_cancel}"/>
    
</h:form>
</f:view>
