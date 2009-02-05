<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="form" enctype="multipart/form-data">
             <t:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@include file="/inc/appMenu.jspf"%>
            </t:aliasBean>
            <h2><h:outputText value="#{msgs.upload_view_page_title}"/></h2>
            <h3><h:outputText value="#{msgs.upload_view_instructions}" escape="false"/></h3>
            <div class="instruction">
                <h:outputText value="#{msgs.upload_view_instructions_text}" escape="false"/>
            </div>
            <%@include file="/inc/globalMessages.jspf"%>
            <p/>
           
            <f:verbatim>
				<table>
					<tr>
					<td>
			</f:verbatim>
                <h:outputLabel for="title" id="titleLabel" value="#{msgs.upload_view_title}"/>
                <f:verbatim>
           			</td>
           			<td>
           		</f:verbatim>
                <h:inputText id="title" value="#{spreadsheetUploadBean.title}" required="true">
                    <f:validateLength minimum="1" maximum="255"/>
                </h:inputText>
                <f:verbatim>
           			</td>
           			<td>
           		</f:verbatim>
                <h:message for="title" styleClass="validationEmbedded" />
				<f:verbatim>
           			</td>
           			</tr>
           			<tr>
           			<td>
           		</f:verbatim>
                <h:outputLabel for="fileupload">
                    <h:outputText id="fileuploadLabel" value="#{msgs.upload_view_choose_file}   "/>
                </h:outputLabel>
                <f:verbatim>
           			</td>
           			<td>
           		</f:verbatim>
                <t:inputFileUpload id="fileupload" value="#{spreadsheetUploadBean.upFile}" storage="file"required="true" accept="text/csv"/>
                <f:verbatim>
           			</td>
           			<td>
           		</f:verbatim>
                <h:message for="fileupload" styleClass="validationEmbedded" />
                <f:verbatim>
           			</td>
           			</tr>
           			</table>
            	</f:verbatim>
            
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

