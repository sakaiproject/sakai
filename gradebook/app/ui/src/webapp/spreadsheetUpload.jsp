<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="form" enctype="multipart/form-data">
            <%@include file="/inc/appMenu.jspf"%>
            <h2><h:outputText value="#{msgs.upload_view_page_title}"/></h2>
            <h3><h:outputText value="#{msgs.upload_view_instructions}" escape="false"/></h3>
            <div class="instruction">
                <h:outputText value="#{msgs.upload_view_instructions_text}" escape="false"/>
            </div>
            <%@include file="/inc/globalMessages.jspf"%>
            <p/>
            <h:panelGrid cellpadding="0" cellspacing="0" columns="3" columnClasses="itemName" styleClass="itemSummary">
                <h:outputLabel for="title" id="titleLabel" value="Title"/>
                <h:inputText id="title" value="#{spreadsheetUploadBean.title}" required="true">
                    <f:validateLength minimum="1" maximum="255"/>
                </h:inputText>
                <h:message for="title" styleClass="validationEmbedded" />

                <h:outputLabel for="fileupload">
                    <h:outputText id="fileuploadLabel" value="Choose a File"/>
                </h:outputLabel>
                <t:inputFileUpload id="fileupload" value="#{spreadsheetUploadBean.upFile}" storage="file"required="true" accept="text/csv"/>
                <h:message for="fileupload" styleClass="validationEmbedded" />
            </h:panelGrid>
            <p>
                <h:commandButton
                        id="saveButton"
                        styleClass="active"
                        value="#{msgs.upload_view_save}"
                        action="#{spreadsheetUploadBean.processFile}"/>
                <h:commandButton
                        value="#{msgs.upload_view_cancel}"
                        action="spreadsheetListing" immediate="true"/>
            </p>
        </h:form>
    </div>
</f:view>

