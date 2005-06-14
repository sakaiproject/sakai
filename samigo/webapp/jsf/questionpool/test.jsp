<h:dataTable id="TreeTable" value="#{questionpool.testPools}"
    var="pool" headerClass="unit2heading" >

    <h:column id="col1">

     <f:facet name="header">
       <h:outputText id="c1header" value="#{msg.p_name}"/>
     </f:facet>


<h:panelGroup styleClass="tier#{pool.level}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{pool.trid}"/>
<h:outputLink id="togglelink"  onclick="toggleRows(this)" value="#" styleClass="folder">
<h:graphicImage id="spacer_for_mozilla" style="border:0" width="17" value="../../images/delivery/spacer.gif" />
</h:outputLink>

<h:outputLink id="poolnamelink" value="editPool.faces?id=#{pool.name}">
<h:outputText id="poolnametext" value="#{pool.name}"/>
</h:outputLink>
<f:verbatim><br/></f:verbatim>
<h:graphicImage id="spacer" style="border:0" width="17" value="../../images/delivery/spacer.gif" />
<h:outputLink id="addlink" styleClass="tier#{pool.level}" value="editPool.faces?id=#{pool.name}">
<h:outputText id="add" value="#{msg.add}"/>
</h:outputLink>
<f:verbatim>&nbsp;| &nbsp;</f:verbatim>
<h:outputLink id="copylink" value="editPool.faces?id=#{pool.name}">
<h:outputText id="copy" value="#{msg.copy}"/>
</h:outputLink>
<f:verbatim>&nbsp;| &nbsp;</f:verbatim>
<h:outputLink id="movelink" value="editPool.faces?id=#{pool.name}">
<h:outputText id="move" value="#{msg.move}"/>
</h:outputLink>
<f:verbatim>&nbsp;| &nbsp;</f:verbatim>
<h:outputLink id="exportlink" value="editPool.faces?id=#{pool.name}">
<h:outputText id="export" value="#{msg.export}"/>
</h:outputLink>

</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
       <h:outputText id="c2header" value="#{msg.creator}"/>
     </f:facet>
     <h:panelGroup id="secondcolumn">
        <h:outputText value="#{pool.creator}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col3">
     <f:facet name="header">
       <h:outputText id="c3header" value="#{msg.last_mod}"/>
     </f:facet>
     <h:panelGroup id="thirdcolumn">
        <h:outputText value="#{pool.lastModified}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col4">
     <f:facet name="header">
       <h:outputText value="#{msg.qs}"/>
     </f:facet>
     <h:panelGroup id="fourthcolumn">
        <h:outputText value="#{pool.noQuestions}"/>
     </h:panelGroup>
    </h:column>


    <h:column id="col5">
     <f:facet name="header">
       <h:outputText value="#{msg.subps}"/>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.nosubpools}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col6">
     <f:facet name="header">
       <h:outputText value="#{msg.remove_chbox}"/>
     </f:facet>
<h:selectBooleanCheckbox id="checkboxes" value ="#{questionpoo.destPools}"/>
    </h:column>



  </h:dataTable>

