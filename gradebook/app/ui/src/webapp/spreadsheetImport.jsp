<f:view>
    <div class="portletBody">
        <h:form id="gbForm">
            <%@include file="/inc/appMenu.jspf"%>
            <sakai:flowState bean="#{spreadsheetUploadBean}" />
            <h2><h:outputText value="#{msgs.import_assignment_page_title}"/></h2>
            <div class="instruction">
                <h:outputText value="#{msgs.import_assignment_instruction}" escape="false"/>
            </div>
            <p/>
            <%@include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="#{msgs.import_assignment_header}"/></h4>

            <h:panelGrid cellpadding="0" cellspacing="0" columns="3" columnClasses="itemName" styleClass="itemSummary">
                <h:outputLabel for="title" id="titleLabel" value="#{msgs.import_assignment_title}"/>

                <h:inputText id="title" value="#{spreadsheetUploadBean.assignment.name}" required="true" >
                    <f:validateLength minimum="1" maximum="255"/>
                </h:inputText>
                <h:message for="title" styleClass="validationEmbedded" />

                <h:outputLabel for="points" id="pointsLabel" value="#{msgs.import_assignment_points}"/>
                <h:inputText id="points" value="#{spreadsheetUploadBean.assignment.pointsPossible}" required="true">
                    <f:validateDoubleRange minimum="0.01" />
                </h:inputText>
                <h:message for="points" styleClass="validationEmbedded" />

                <h:panelGroup>
                    <h:outputLabel for="dueDate" id="dueDateLabel" value="#{msgs.import_assignment_due_date}"/>
                    <h:outputText style="font-weight:normal;" value=" #{msgs.date_entry_format_description}"/>
                </h:panelGroup>
                <x:inputCalendar id="dueDate" value="#{spreadsheetUploadBean.assignment.dueDate}" renderAsPopup="true" renderPopupButtonAsImage="true" popupTodayString="#{msgs.date_entry_today_is}" popupWeekString="#{msgs.date_entry_week_header}" />
                <h:message for="dueDate" styleClass="validationEmbedded" />
            </h:panelGrid>

         <%/*
            This would be positioned directly under the Point Value entry if
            only JSF supported "colspan"....
        */%>
            <h:panelGrid columns="2" columnClasses="prefixedCheckbox">
                <h:selectBooleanCheckbox id="countAssignment" value="#{spreadsheetUploadBean.assignment.counted}"/>
                <h:outputLabel for="countAssignment" value="#{msgs.import_assignment_counted}" />

               <h:selectBooleanCheckbox id="released" value="#{spreadsheetUploadBean.assignment.released}"/>
                <h:outputLabel for="released" value="#{msgs.add_assignment_released}" />
            </h:panelGrid>

            <p class="act calendarPadding">
                <h:commandButton
                        id="saveButton"
                        styleClass="active"
                        value="#{msgs.import_assignment_submit}"
                        action="#{spreadsheetUploadBean.saveGrades}"/>
                <h:commandButton
                        value="#{msgs.import_assignment_cancel}"
                        action="spreadsheetPreview" />
            </p>
        </h:form>
    </div>
</f:view>