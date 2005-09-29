<f:view>
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editStudentSectionsBean}"/>

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <x:div styleClass="portletBody">
        <h2>
            <h:outputFormat value="#{msgs.edit_student_sections_page_header}">
                <f:param value="#{editStudentSectionsBean.studentName}"/>
            </h:outputFormat>
        </h2>
        
        <%@include file="/inc/globalMessages.jspf"%>
    
        <h:dataTable
            value="#{editStudentSectionsBean.usedCategories}"
            var="category"
            cellspacing="10">
            <h:column>
                <h:selectOneRadio
                    value="#{editStudentSectionsBean.sectionEnrollment[category]}"
                    layout="pageDirection">
                    <f:selectItems value="#{editStudentSectionsBean.sectionItems[category]}"/>
                    <f:selectItem itemValue="#{editStudentSectionsBean.unassignedValue}" itemLabel="#{msgs.edit_student_sections_unassigned}"/>
                </h:selectOneRadio>
            </h:column>
        </h:dataTable>
    
        <h:commandButton action="#{editStudentSectionsBean.update}" value="#{msgs.edit_student_sections_update}"/>
        <h:commandButton action="roster" value="#{msgs.edit_student_sections_cancel}"/>
    </x:div>
</h:form>
</f:view>
