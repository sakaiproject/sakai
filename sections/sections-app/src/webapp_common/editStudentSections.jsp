<f:view>
<h:form id="editSectionForm">

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{editSectionBean}"/>
    
    <h:commandButton action="#{editStudentSections.update}" value="Update"/>
    
    <h:commandButton action="roster" value="Cancel"/>
    
</h:form>
</f:view>
