<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editSectionBean}"/>

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>
    
    <h3><h:outputText value="#{msgs.edit_section_page_header}"/></h3>
        
    <%@include file="/inc/globalMessages.jspf"%>

	<x:aliasBean alias="#{bean}" value="#{editSectionBean}">
		<%@include file="/inc/sectionEditor.jspf"%>
	</x:aliasBean>

    <x:div styleClass="act">
        <h:commandButton
        	action="#{editSectionBean.update}"
        	value="#{msgs.section_update}"
        	styleClass="active" />

        <h:commandButton action="overview" value="#{msgs.section_cancel}" immediate="true" />
    </x:div>
</h:form>
</div>
</f:view>
