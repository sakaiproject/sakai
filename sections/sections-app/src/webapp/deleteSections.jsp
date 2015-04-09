<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{overviewBean}"/>

    <t:aliasBean alias="#{viewName}" value="overview">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

	<h3>
	    <h:outputFormat value="#{msgs.overview_page_header}">
	        <f:param value="#{overviewBean.siteRole}"/>
	    </h:outputFormat>
	</h3>

    <t:div styleClass="alertMessage">
        <h:outputText value="#{msgs.overview_delete_section_confirmation_pre}"/>
        <t:dataList
            id="deleteSectionsTable"
            value="#{overviewBean.sectionsToDelete}"
            var="section"
            layout="unorderedList">
            <h:outputText value="#{section.title}"/>
        </t:dataList>
    
        <t:div>
            <h:outputText value="#{msgs.overview_delete_section_confirmation_post}"/>
        </t:div>
    </t:div>    
    <t:div styleClass="act deleteButtons">
        <h:commandButton
            action="#{overviewBean.deleteSections}"
            value="#{msgs.overview_delete_short}"
            styleClass="active" />
        <h:commandButton action="overview" value="#{msgs.cancel}"/>
    </t:div>        


</h:form>
</div>
</f:view>
