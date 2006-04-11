<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

