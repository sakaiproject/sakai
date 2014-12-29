<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
    <div class="portletBody">
        <h:form id="form">
            <t:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@ include file="/inc/appMenu.jspf"%>
            </t:aliasBean>

            <h2><h:outputText value="#{msgs.upload_preview_page_title}"/></h2>

            <sakai:flowState bean="#{spreadsheetUploadBean}" />

            <div class="instruction">
                <h:outputText value="#{msgs.upload_preview_instructions}" escape="false"/>
                <f:verbatim><ul></f:verbatim>
                <h:outputText value="#{spreadsheetUploadBean.columnCount}" escape="false"/>
                <h:outputText value="#{spreadsheetUploadBean.rowCount}" escape="false"/>
                <f:verbatim><ul></f:verbatim>
            </div>
            <p class="instruction">
                <h:outputText value="#{msgs.upload_preview_additional_text}" escape="false"/>
            </p>
            <%@ include file="/inc/globalMessages.jspf"%>
            <t:dataTable id="table1" value="#{spreadsheetUploadBean.studentRows}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">
                <t:column styleClass="left">
                    <f:facet name="header">
                        <t:outputText value="#{msgs.upload_preview_student_id}"/>
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:columns value="#{spreadsheetUploadBean.assignmentList}" var="colIndex" >
                    <f:facet name="header">
                        <h:panelGrid>
                            <t:outputText value="#{spreadsheetUploadBean.assignmentHeaders[colIndex]}" />
                        </h:panelGrid>
                    </f:facet>
                    <h:outputText value="#{row.rowcontent[colIndex + 1]}" />
                </t:columns>
            </t:dataTable>
            <f:verbatim><br></f:verbatim>
            <p>
                <h:commandButton
                        id="saveButton"
                        styleClass="active"
                        value="#{msgs.upload_preview_ok}"
                        action="#{spreadsheetUploadBean.saveFile}"/>


                <h:commandButton
                        value="#{msgs.upload_preview_back}"
                        action="spreadsheetUpload" immediate="true"/>
            </p>
        </h:form>
    </div>
</f:view>