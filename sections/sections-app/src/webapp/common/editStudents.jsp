<f:view>
<div class="portletBody">
<h:form id="memberForm">

    <sakai:flowState bean="#{editStudentsBean}"/>

    <x:aliasBean alias="#{viewName}" value="editStudents">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

        <h2><h:outputText value="#{msgs.edit_student_page_header}"/></h2>
        <h4><h:outputText value="#{editStudentsBean.sectionDescription}"/></h4>

        <%@include file="/inc/globalMessages.jspf"%>

        <h:panelGrid id="transferTable" columns="3" columnClasses="available,transferButtons,selected">
        
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
                <h:panelGrid styleClass="sectionContainerNav" columns="3" columnClasses="sectionLeftNav,sectionRightNav,sectionRightNav">
                    <x:div>
                        <h:outputFormat value="#{editStudentsBean.sectionTitle}"/>
                    </x:div>
                    <x:div>
                        <h:outputFormat value="#{msgs.edit_student_section_size}"/>
                    </x:div>
                    <x:div id="max">
                        <h:outputFormat value="#{msgs.edit_student_selected_title}" rendered="#{editStudentsBean.sectionMax != null}">
                            <f:param value="#{editStudentsBean.sectionMax}"/>
                        </h:outputFormat>
                    </x:div>
                </h:panelGrid>
            
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
</h:form>
</div>
</f:view>
