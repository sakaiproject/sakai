
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="gbForm">
            <t:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@ include file="/inc/appMenu.jspf"%>
           		<%@ include file="/inc/breadcrumb.jspf" %>
            </t:aliasBean>
 <%--           <h2><h:outputText value="#{msgs.loading_dock_page_title}"/></h2> --%>
            <div class="instruction">
                <h:outputText value="#{msgs.loading_dock_instructions}" escape="false"/>
            </div>

            <h:panelGroup rendered="#{spreadsheetUploadBean.userAbleToEditAssessments}">
                <h:commandLink action="spreadsheetUpload" immediate="true">
                    <h:outputText value="#{msgs.loading_dock_upload_link_text}"/>
                </h:commandLink>
            </h:panelGroup>
            <p/>
            <p/>
            <%@ include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="#{msgs.loading_dock_table_header}"/></h4>
            <t:dataTable id="table1" value="#{spreadsheetUploadBean.spreadsheets}" var="row" rowIndexVar="rowIndex"
                         columnClasses="left,left,rightpadded,rightpadded,rightpadded"                         
                         styleClass="listHier narrowTable">

                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_title}"/>
                    </f:facet>
                    <h:outputText value="#{row.name}"/>
                </t:column>
                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_creator}"/>
                    </f:facet>
                    <h:outputText value="#{row.creator}"/>
                </t:column>


                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_datecreated}"/>
                    </f:facet>
                    <h:outputText value="#{row.dateCreated}">
                        <f:convertDateTime pattern="d MMM yyyy  H:mm:ss"/>
                    </h:outputText>
                </t:column>

                <t:column>
                    <h:commandLink action="#{spreadsheetUploadBean.viewItem}">
                        <h:outputText value="#{msgs.loading_dock_table_view}"/>
                        <f:param name="spreadsheetId" value="#{row.id}"/>
                    </h:commandLink>
                </t:column>
                <t:column>
                    <h:commandLink action="spreadsheetRemove">
                        <h:outputText value="#{msgs.loading_dock_table_delete}"/>
                        <f:param name="spreadsheetId" value="#{row.id}"/>
                    </h:commandLink>
                </t:column>
            </t:dataTable>
        </h:form>
    </div>
</f:view>
