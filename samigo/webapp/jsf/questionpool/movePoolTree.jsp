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
<h:panelGrid  rendered="#{questionpool.actionType == 'pool'}" columns="2">
	<h:selectOneRadio onclick="uncheckOthers(this);" id="radiobtn"
		layout="pageDirection" value="#{questionpool.destPool}">
       		<f:selectItem itemValue="0" itemLabel=""/>
	</h:selectOneRadio>

	<h:outputText value="#{msg.q_mgr}"/>
</h:panelGrid>




<h:dataTable id="TreeTable" value="#{questionpool.moveQpools}"
   var="pool"  width="100%" styleClass="listHier" >


    <h:column  id="radiocol">
<h:selectOneRadio onclick="uncheckOthers(this);" id="radiobtn" layout="pageDirection"
		value="#{questionpool.destPool}">
                <f:selectItem itemValue="#{pool.questionPoolId}" itemLabel=""/>
</h:selectOneRadio>

    </h:column>

    <h:column id="col1">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortByTitle" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='title'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{msg.p_name}"  rendered="#{questionpool.sortMovePoolProperty !='title'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.p_name}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='title'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='title' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='title' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>


<h:panelGroup styleClass="treetier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>

<h:outputLink id="parenttogglelink"  onclick="toggleRowsForSelectList(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
<h:graphicImage id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink id="togglelink"  styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
<h:graphicImage id="spacer_for_mozilla1" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>



  <h:outputText id="poolnametext" value="#{pool.displayName}"/>

</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink id="sortByOwner" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='ownerId'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{msg.creator}"  rendered="#{questionpool.sortMovePoolProperty !='ownerId'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.creator}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='ownerId'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='ownerId' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='ownerId' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='lastModified'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{msg.last_mod}"  rendered="#{questionpool.sortMovePoolProperty !='lastModified'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='lastModified'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='lastModified' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='lastModified' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='question'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="question"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{msg.qs}"  rendered="#{questionpool.sortMovePoolProperty !='question'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.qs}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='question'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='question' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="question"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='question' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="question"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='subPoolSize'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{msg.subps}"  rendered="#{questionpool.sortMovePoolProperty !='subPoolSize'}" />
       </h:commandLink>
       <h:outputText  value="#{msg.subps}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize'}" />
       <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  immediate="true" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{msg.asc}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
      </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>

</h:dataTable>

