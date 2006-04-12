<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{overviewBean}"/>

    <x:aliasBean alias="#{viewName}" value="overview">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>
    
    <h3><h:outputText value="#{msgs.overview_page_header}"/></h3>

    <x:div styleClass="alertMessage">
        <h:outputText value="#{msgs.overview_delete_section_confirmation_pre}"/>
        <x:dataList
            id="deleteSectionsTable"
            value="#{overviewBean.sectionsToDelete}"
            var="section"
            layout="unorderedList">
            <h:outputText value="#{section.title}"/>
        </x:dataList>
    
        <x:div>
            <h:outputText value="#{msgs.overview_delete_section_confirmation_post}"/>
        </x:div>
    
        <x:div styleClass="act deleteButtons">
            <h:commandButton
                action="#{overviewBean.deleteSections}"
                value="#{msgs.overview_delete_short}"
                styleClass="active" />
            <h:commandButton action="overview" value="#{msgs.overview_cancel}"/>
        </x:div>        
    </x:div>

</h:form>
</div>
</f:view>
