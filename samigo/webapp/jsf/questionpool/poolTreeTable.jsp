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
    var="pool" styleClass="listHier" width="100%" >

    <h:column id="col1">

     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortByTitle" immediate="true"  rendered="#{questionpool.sortProperty !='title'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{msg.p_name}"  rendered="#{questionpool.sortProperty !='title'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.p_name}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='title'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='title' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='title' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
<h:panelGroup styleClass="treetier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
<h:outputLink id="parenttogglelink"  onclick="toggleRows(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
<h:graphicImage id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink id="togglelink"  value="#" styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
<h:graphicImage id="spacer_for_mozilla1" style="border:0" width="30" height="14"  value="/images/delivery/spacer.gif" />
</h:outputLink>

<h:commandLink id="editlink" immediate="true" action="#{questionpool.editPool}">
  <h:outputText id="poolnametext" value="#{pool.displayName}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>

<f:verbatim><br/></f:verbatim>
<h:graphicImage id="spacer" style="border:0" width="30" height="14" value="/images/delivery/spacer.gif" />
 <f:verbatim><span class="itemAction"></f:verbatim>
<h:commandLink rendered="#{questionpool.importToAuthoring != 'true'}"  styleClass="treetier#{questionpool.tree.currentLevel}" id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText id="add" value="#{msg.add}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<h:outputText rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />
<h:commandLink rendered="#{questionpool.importToAuthoring != 'true'}" id="copylink" immediate="true" action="#{questionpool.startCopyPool}">
  <h:outputText id="copy" value="#{msg.copy}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<h:outputText rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />

<h:commandLink rendered="#{questionpool.importToAuthoring != 'true'}" id="movelink" immediate="true" action="#{questionpool.startMovePool}">
  <h:outputText id="move" value="#{msg.move}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>

<%--
<h:outputText value=" | " />

<h:commandLink id="exportlink" immediate="true" action="#{questionpool.exportPool}">
  <h:outputText id="export" value=""/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
--%>
 <f:verbatim></span></f:verbatim>
</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
     <h:panelGroup>
       <h:commandLink id="sortByOwner" immediate="true"  rendered="#{questionpool.sortProperty !='ownerId'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{msg.creator}"  rendered="#{questionpool.sortProperty !='ownerId'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.creator}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='ownerId'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='ownerId' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='ownerId' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="secondcolumn">
        <h:outputText value="#{pool.ownerId}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col3">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortProperty !='lastModified'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{msg.last_mod}"  rendered="#{questionpool.sortProperty !='lastModified'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='lastModified'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='lastModified' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='lastModified' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="thirdcolumn">
        <h:outputText value="#{pool.lastModified}">
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
	</h:outputText>
     </h:panelGroup>
    </h:column>

    <h:column id="col4">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortProperty !='question'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="question"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{msg.qs}"  rendered="#{questionpool.sortProperty !='question'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.qs}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='question'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='question' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="question"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='question' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="question"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fourthcolumn">
        <h:outputText value="#{pool.questionSize}"/>
     </h:panelGroup>
    </h:column>


    <h:column id="col5">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortProperty !='subPoolSize'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{msg.subps}"  rendered="#{questionpool.sortProperty !='subPoolSize'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.subps}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='subPoolSize'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='subPoolSize' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortProperty =='subPoolSize' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col6" rendered="#{questionpool.importToAuthoring == 'false'}" >
     <f:facet name="header">
       <h:outputText value="#{msg.remove_chbox}"/>
     </f:facet>
<h:selectManyCheckbox id="removeCheckbox" value ="#{questionpool.destPools}">
	<f:selectItem itemValue="#{pool.questionPoolId}" itemLabel=""/>
</h:selectManyCheckbox>
    </h:column>



  </h:dataTable>

