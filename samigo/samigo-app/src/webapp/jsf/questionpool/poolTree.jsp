<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
<h:dataTable id="TreeTable" value="#{questionpool.qpools}"
    var="pool" headerClass="unit2heading" >

    <h:column id="chbox">
        <h:selectManyCheckbox value ="#{questionpool.selectedPools}"/>
		<f:selectItem itemValue="#{pool.id}" itemLabel=""/>
    </h:column>

    <h:column id="col1">


<h:panelGroup styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
<h:outputLink title="#{msg.t_toggletree}" id="togglelink"  onclick="toggleRows(this)" onkeypress="toggleRows(this)" value="#" styleClass="treefolder">
<h:graphicImage id="spacer_for_mozilla" style="border:0" width="17" value="/images/delivery/spacer.gif" />
</h:outputLink>

<h:commandLink title="#{msg.t_editPool}" id="editlink" immediate="true" action="#{questionpool.editPool}">
  <h:outputText id="poolnametext" value="#{pool.data.title}"/>
  <f:param name="qpid" value="#{pool.id}"/>
</h:commandLink>



</h:panelGroup>
    </h:column>

</h:dataTable>

