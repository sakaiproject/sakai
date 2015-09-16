<f:view>
<div class="portletBody">
<h:form id="memberForm">

    <sakai:flowState bean="#{editStudentsBean}"/>

    <t:aliasBean alias="#{viewName}" value="editStudents">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

        <h3><h:outputText value="#{msgs.edit_student_page_header}"/></h3>
        <h4><h:outputText value="#{editStudentsBean.sectionDescription}"/></h4>

        <%@ include file="/inc/globalMessages.jspf"%>

        <h:panelGrid id="transferTable" columns="3" columnClasses="available,transferButtons,selected">
        
            <h:panelGroup>
                <t:div>
                    <h:selectOneMenu value="#{editStudentsBean.availableSectionUuid}" valueChangeListener="#{editStudentsBean.processChangeSection}" onchange="this.form.submit()">
                        <f:selectItems value="#{editStudentsBean.availableSectionItems}"/>
                    </h:selectOneMenu>
                </t:div>
                <t:div>
                    <h:selectManyListbox id="availableUsers" size="20" style="width:250px;">
                        <f:selectItems value="#{editStudentsBean.availableUsers}"/>
                    </h:selectManyListbox>
                </t:div>
            </h:panelGroup>

            <%@ include file="/inc/transferButtons.jspf"%>
            
            <h:panelGroup>
                <h:panelGrid styleClass="sectionContainerNav" columns="3" columnClasses="sectionLeftNav,sectionRightNav,sectionRightNav">
                        <h:outputFormat value="#{editStudentsBean.abbreviatedSectionTitle}"/>
                        <h:outputFormat value="#{msgs.edit_student_section_size}"/>
                    <t:div id="max">
                        <h:outputFormat value="#{msgs.edit_student_selected_title}" rendered="#{editStudentsBean.sectionMax != null}">
                            <f:param value="#{editStudentsBean.sectionMax}"/>
                        </h:outputFormat>
                    </t:div>
                </h:panelGrid>
            
                <h:selectManyListbox id="selectedUsers" size="20" style="width:250px;">
                    <f:selectItems value="#{editStudentsBean.selectedUsers}"/>
                </h:selectManyListbox>
            </h:panelGroup>
    
        </h:panelGrid>
        
        <t:div styleClass="act">
            <h:commandButton
                action="#{editStudentsBean.update}"
                onclick="highlightUsers()"
                value="#{msgs.edit_student_update}"
                immediate="true"
                styleClass="active" />
        
            <h:commandButton
                action="overview"
                immediate="true"
                value="#{msgs.cancel}"/>
        </t:div>
</h:form>
</div>
</f:view>
