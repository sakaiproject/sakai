<h:dataTable id="TreeTable" value="#{questionpool.qpools}"
    var="pool" headerClass="unit2heading" >

    <h:column id="chbox">
        <h:selectManyCheckbox value ="#{questionpool.selectedPools}"/>
		<f:selectItem itemValue="#{pool.id}" itemLabel=""/>
    </h:column>

    <h:column id="col1">


<h:panelGroup styleClass="treetier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
<h:outputLink id="togglelink"  onclick="toggleRows(this)" value="#" styleClass="treefolder">
<h:graphicImage id="spacer_for_mozilla" style="border:0" width="17" value="/images/delivery/spacer.gif" />
</h:outputLink>

<h:commandLink id="editlink" immediate="true" action="#{questionpool.editPool}">
  <h:outputText id="poolnametext" value="#{pool.data.title}"/>
  <f:param name="qpid" value="#{pool.id}"/>
</h:commandLink>



</h:panelGroup>
    </h:column>

</h:dataTable>

