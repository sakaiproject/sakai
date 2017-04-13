<!-- $Id: transferPoolTree.jsp 2012-11-10 wang58@iupui.edu -->

<h:inputHidden id="transferPoolIds" value=""/>
<h:dataTable id="TreeTable" value="#{questionpool.transferQpools}" var="pool" cellpadding="0" cellspacing="0" styleClass="listHier" >
    <h:column id="col1">
        <f:facet name="header">
            <h:panelGroup>
                <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" 
                    rendered="#{questionpool.sortTransferPoolProperty == 'title' && questionpool.sortTransferPoolAscending}" 
                    action="#{questionpool.sortTransferPoolByColumnHeader}">
                    <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortTransferPoolProperty == 'title'}" />
                    <f:param name="transferPoolOrderBy" value="title" />
                    <f:param name="transferPoolAscending" value="false" />
                    <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleDescending}" rendered="#{questionpool.sortTransferPoolAscending}" 
                        url="/images/sortascending.gif" />
                </h:commandLink>
                <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" 
                    rendered="#{questionpool.sortTransferPoolProperty == 'title' && !questionpool.sortTransferPoolAscending}" 
                    action="#{questionpool.sortTransferPoolByColumnHeader}">
                    <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortTransferPoolProperty == 'title'}" />
                    <f:param name="transferPoolOrderBy" value="title"/>
                    <f:param name="transferPoolAscending" value="true" />
                    <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleAscending}" rendered="#{!questionpool.sortTransferPoolAscending}" 
                        url="/images/sortdescending.gif"/>
                </h:commandLink>
            </h:panelGroup>
        </f:facet>
        <h:panelGroup id="firstcolumn">
            <h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
            <h:selectManyCheckbox onclick="checkChildrenCheckboxes(this);updateButtonStatusOnCheck(document.getElementById('transferPool:transferpoolSubmit'), document.getElementById('transferPool'));" id="radiobtn"
                value="#{questionpool.transferPools}" styleClass="tier#{questionpool.tree.currentLevel}" disabled="false">
                <f:selectItem itemValue="#{pool.questionPoolId}" itemLabel="#{pool.displayName}" />
            </h:selectManyCheckbox>
        </h:panelGroup>
    </h:column>

    <h:column id="col2">
        <f:facet name="header">
            <h:panelGroup>      
                <h:outputText  value="#{questionPoolMessages.creator}" />
            </h:panelGroup>
        </f:facet>
        <h:panelGroup id="secondcolumn">
            <h:outputText value="#{pool.ownerDisplayName}" />
        </h:panelGroup>
    </h:column>

    <h:column id="col3">
        <f:facet name="header">
            <h:panelGroup>
                <h:outputText value="#{questionPoolMessages.last_mod}" />
            </h:panelGroup>
        </f:facet>
        <h:panelGroup id="thirdcolumn">
            <h:outputText value="#{pool.lastModified}">
                <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
            </h:outputText>
        </h:panelGroup>
    </h:column>

    <h:column id="col4">
        <f:facet name="header">
            <h:panelGroup>
                <h:outputText value="#{questionPoolMessages.qs}" />
            </h:panelGroup>
        </f:facet>
        <h:panelGroup id="fourthcolumn" >
            <h:outputText value="#{pool.data.questionPoolItemSize}" />
        </h:panelGroup>
    </h:column>


    <h:column id="col5">
        <f:facet name="header">
            <h:panelGroup>
                <h:outputText value="#{questionPoolMessages.subps}" />
            </h:panelGroup>
        </f:facet>
        <h:panelGroup id="fifthcolumn">
            <h:outputText value="#{pool.subPoolSize}" />
        </h:panelGroup>
    </h:column>
</h:dataTable>
