<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editSectionBean}"/>

    <t:aliasBean alias="#{viewName}" value="editSection">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

    <div class="page-header">
        <h1><h:outputText value="#{msgs.edit_section_page_header}"/></h1>
    </div>

    <%@ include file="/inc/globalMessages.jspf"%>

	<t:aliasBean alias="#{bean}" value="#{editSectionBean}">
		<%@ include file="/inc/sectionEditor.jspf"%>
	</t:aliasBean>

    <t:div styleClass="act">
        <h:commandButton
        	action="#{editSectionBean.update}"
        	value="#{msgs.update}"
        	styleClass="active"
        	onclick="reEnableLimits();" />

        <h:commandButton action="overview" value="#{msgs.cancel}" immediate="true" />
    </t:div>
</h:form>
</div>
</f:view>
