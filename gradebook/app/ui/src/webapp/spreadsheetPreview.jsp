<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
    <div class="portletBody">
        <h:form id="form">

              <t:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@ include file="/inc/appMenu.jspf"%>
            </t:aliasBean>

            <sakai:flowState bean="#{spreadsheetUploadBean}" />

            <h2><h:outputText value="#{msgs.import_preview_page_title}"/></h2>

            <div class="instruction">
                <h:outputText value="#{msgs.import_preview_instructions}" escape="false"/>
            </div>
            <p class="instruction">
                <h:outputText value="#{msgs.import_preview_additional_text}" escape="false"/>
            </p>

             <p class="instruction">
                <h:outputText value="#{msgs.import_preview_nomatch}" escape="false" rendered="#{spreadsheetUploadBean.hasUnknownUser}"/>
            </p>

            <%@ include file="/inc/globalMessages.jspf"%>
            <p/>
            <t:selectOneRadio id="assignment" layout="spread" converter="javax.faces.Integer" value="1">
                <f:selectItems  value="#{spreadsheetUploadBean.assignmentColumnSelectItems}" />
            </t:selectOneRadio>
            <t:dataTable id="table1"
                         value="#{spreadsheetUploadBean.studentRows}"
                         var="row"
                         rowIndexVar="rowIndex"
                         styleClass="listHier"
                         columnClasses="center"
                         rowClasses="#{spreadsheetUploadBean.rowStyles}">
                <t:column styleClass="left">
                    <f:facet name="header">
                        <t:outputText value="#{msgs.upload_preview_student_id}"/>
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:column styleClass="left" >
                    <f:facet name="header">
                        <t:outputText value="#{msgs.upload_preview_student_name}"/>
                    </f:facet>
                    <h:outputText value="#{row.userDisplayName}"/>
                </t:column>

                <t:columns value="#{spreadsheetUploadBean.assignmentList}" var="colIndex" >
                    <f:facet name="header">
                        <h:panelGrid>
                            <t:radio for=":form:assignment" index="#{colIndex}" />
                        </h:panelGrid>
                    </f:facet>
                    <h:outputText value="#{row.rowcontent[colIndex + 1]}" />
                </t:columns>
            </t:dataTable>

            <f:verbatim><br></f:verbatim>
            <p>
                <h:commandButton
                        id="importButton"
                        styleClass="active"
                        value="#{msgs.import_preview_import_selected}"
                        action="#{spreadsheetUploadBean.importData}"/>

                <h:commandButton
                        value="#{msgs.import_preview_cancel}"
                        action="spreadsheetListing" immediate="true"/>

            </p>
        </h:form>
    </div>
</f:view>