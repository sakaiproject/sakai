<f:view>
<h:form id="memberForm">

    <sakai:flowState bean="#{editStudentsBean}"/>

    <x:aliasBean alias="#{viewName}" value="editStudents">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

    <x:div styleClass="portletBody">
        <h2><h:outputText value="#{msgs.edit_student_page_header}"/></h2>
        
        <%@include file="/inc/globalMessages.jspf"%>

        <h:panelGrid columns="3">
        
            <h:panelGroup>
                <x:div>
                    <h:selectOneMenu value="#{editStudentsBean.availableSectionUuid}" valueChangeListener="#{editStudentsBean.processChangeSection}" onchange="this.form.submit()">
                        <f:selectItems value="#{editStudentsBean.availableSectionItems}"/>
                    </h:selectOneMenu>
                </x:div>
                <x:div>
                	<h:selectManyListbox id="availableUsers" size="20" style="width:200px;">
                		<f:selectItems value="#{editStudentsBean.availableUsers}"/>
                	</h:selectManyListbox>
                </x:div>
            </h:panelGroup>
        
            <%@include file="/inc/transferButtons.jspf"%>
        	
            <h:panelGroup>
                <x:div>
                    <h:outputFormat value="#{editStudentsBean.sectionTitle}"/>
                </x:div>
                <x:div id="max" rendered="#{editStudentsBean.sectionMax != null}">
                    <h:outputFormat value="#{msgs.edit_student_selected_title}">
                        <f:param value="#{editStudentsBean.sectionMax}"/>
                    </h:outputFormat>
                </x:div>
            
            	<h:selectManyListbox id="selectedUsers" size="20" style="width:200px;">
                    <f:selectItems value="#{editStudentsBean.selectedUsers}"/>
            	</h:selectManyListbox>
            </h:panelGroup>
    
        </h:panelGrid>
        
        <h:commandButton
            action="#{editStudentsBean.update}"
            onclick="highlightUsers()"
            value="#{msgs.edit_student_update}"/>
    
        <h:commandButton
            action="overview"
            value="#{msgs.edit_student_cancel}"/>
    </x:div>
</h:form>
</f:view>
