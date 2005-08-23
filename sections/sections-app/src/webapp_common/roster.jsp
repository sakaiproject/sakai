<f:view>
<h:form id="overviewForm">

    <x:aliasBean alias="#{viewName}" value="roster">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{rosterBean}"/>
    
    <x:dataTable cellpadding="0" cellspacing="0"
        id="sectionsTable"
        value="#{rosterBean.enrollments}"
        var="enrollment"
        binding="#{rosterBean.rosterDataTable}"
        sortColumn="#{rosterBean.sortColumn}"
        sortAscending="#{rosterBean.sortAscending}"
        styleClass="listHier narrowTable">
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="studentName" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.roster_table_header_name}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{enrollment.user.sortName}"/>
        </h:column>
        <h:column>
            <f:facet name="header">
                <x:commandSortHeader columnName="studentId" immediate="true" arrow="true">
                    <h:outputText value="#{msgs.roster_table_header_id}" />
                </x:commandSortHeader>
            </f:facet>
            <h:outputText value="#{enrollment.user.displayId}"/>
        </h:column>
        
        <%/* A dynamic number of section columns will be appended here by the backing bean */%>
    
    </x:dataTable>

</h:form>
</f:view>
