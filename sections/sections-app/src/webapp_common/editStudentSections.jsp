<f:view>
<h:form id="editSectionForm">

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <sakai:flowState bean="#{editStudentSectionsBean}"/>
    
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
</h:form>
</f:view>
