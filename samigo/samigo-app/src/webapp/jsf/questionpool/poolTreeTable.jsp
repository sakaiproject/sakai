<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
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
<div class="table-responsive">
  <h:dataTable styleClass="table table-striped" id="TreeTable" value="#{questionpool.qpools}" var="pool">
    <h:column id="col1">

     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortTitle}" id="sortByTitle" immediate="true"  rendered="#{questionpool.sortProperty !='title'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.p_name}"  rendered="#{questionpool.sortProperty !='title'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortProperty =='title' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='title'}" />
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleDescending}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortProperty =='title' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='title'}" />
          <f:param name="orderBy" value="title"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleAscending}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
<h:panelGroup styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>
<h:outputLink  title="#{questionPoolMessages.t_toggletree}" id="parenttogglelink"  onclick="toggleRows(this)" onkeypress="toggleRows(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
    <h:graphicImage id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink title="#{questionPoolMessages.t_toggletree}" id="togglelink"  value="#" styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
    <h:graphicImage id="spacer_for_mozilla1" style="border:0" width="30" height="14"  value="/images/delivery/spacer.gif" />
</h:outputLink>

<h:commandLink title="#{questionPoolMessages.t_editPool}" id="editlink" immediate="true" action="#{questionpool.editPool}" rendered="#{authorization.editOwnQuestionPool}">
  <h:outputText id="poolnametext" value="#{pool.displayName}" escape="false"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
  <f:param name="orderBy" value="text"/>
  <f:param name="ascending" value="true"/>
  <f:param name="getItems" value="false"/>
  <f:param name="outCome" value="editPool"/>
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
</h:commandLink>

<h:panelGroup rendered="#{!authorization.editOwnQuestionPool}" styleClass="pool-display-name">
  <h:outputText id="poolnametext2" value="#{pool.displayName}" escape="false"/>
</h:panelGroup>

<br/>
<h:graphicImage id="spacer" style="border:0" width="30" height="14" value="/images/delivery/spacer.gif" />
 <span class="itemAction">
 <!-- Add Pool -->
 <h:commandLink title="#{questionPoolMessages.t_addSubpool}" rendered="#{questionpool.importToAuthoring != 'true' && authorization.createQuestionPool && pool.ownerId==questionpool.agentId}"  styleClass="tier#{questionpool.tree.currentLevel}" id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText id="add" value="#{questionPoolMessages.t_addSubpool}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
  <f:param name="outCome" value="poolList"/>
</h:commandLink>
<!-- Copy Pool -->
<h:outputText rendered="#{questionpool.importToAuthoring != 'true' && authorization.copyOwnQuestionPool && pool.ownerId==questionpool.agentId}" value=" #{questionPoolMessages.separator} " />
<h:commandLink title="#{questionPoolMessages.t_copyPool}" rendered="#{questionpool.importToAuthoring != 'true' && authorization.copyOwnQuestionPool}" id="copylink" immediate="true" action="#{questionpool.startCopyPool}">
  <h:outputText id="copy" value="#{questionPoolMessages.copy}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
  <f:param name="outCome" value="poolList"/>
</h:commandLink>
<!-- Unshare Pool -->
<h:outputText rendered="#{questionpool.importToAuthoring != 'true' && pool.ownerId!=questionpool.agentId}" value=" #{questionPoolMessages.separator} " />
<h:commandLink title="#{questionPoolMessages.t_unsharePool}" rendered="#{questionpool.importToAuthoring != 'true' && pool.ownerId!=questionpool.agentId}" id="unsharelink" immediate="true" action="#{questionpool.startUnsharePool}">
  <h:outputText id="unshare" value="#{questionPoolMessages.unshare}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<!-- Move Pool -->
<h:outputText rendered="#{questionpool.importToAuthoring != 'true' && authorization.editOwnQuestionPool && pool.ownerId==questionpool.agentId}" value=" #{questionPoolMessages.separator} " />
<h:commandLink title="#{questionPoolMessages.t_movePool}" rendered="#{questionpool.importToAuthoring != 'true' && authorization.editOwnQuestionPool && pool.ownerId==questionpool.agentId}" id="movelink" immediate="true" action="#{questionpool.startMovePool}">
  <h:outputText id="move" value="#{questionPoolMessages.move}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
  <f:param name="outCome" value="poolList"/>
</h:commandLink>

<!-- Share Pool -->
<h:outputText rendered="#{questionpool.importToAuthoring != 'true' && authorization.editOwnQuestionPool && pool.ownerId==questionpool.agentId && pool.parentPoolId == 0}" value=" #{questionPoolMessages.separator} " />
  <h:commandLink rendered="#{questionpool.importToAuthoring != 'true' && authorization.editOwnQuestionPool && pool.ownerId==questionpool.agentId && pool.parentPoolId == 0}" id="sharelink" immediate="true" action="#{questionpoolshare.startSharePool}" >
  <h:outputText value="#{questionPoolMessages.t_sharePool}" />
  <f:param name="qpid" value="#{pool.questionPoolId}"/>           
</h:commandLink>

<%--
<h:outputText value=" #{questionPoolMessages.separator} " />

<h:commandLink title="" id="exportlink" immediate="true" action="#{questionpool.exportPool}">
  <h:outputText id="export" value=""/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
--%>
 </span>
</h:panelGroup>
    </h:column>

  
    <h:column id="col2">
     <f:facet name="header">
     <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortCreator}" id="sortByOwner" immediate="true"  rendered="#{questionpool.sortProperty !='ownerId'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.creator}"  rendered="#{questionpool.sortProperty !='ownerId'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortProperty =='ownerId' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='ownerId'}" />
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorDescending}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortProperty =='ownerId' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
          <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='ownerId'}" />
          <f:param name="orderBy" value="ownerId"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorAscending}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="secondcolumn">
<%-- 
lydial: in 2.2, use Display Name instead of ownerId, since ownerId now returns the long internal ID, rather than the short login, Will need to fix sorting too, but right now qpool is person scoped so sorting doesn't make any difference 
--%>
        <h:outputText value="#{pool.ownerDisplayName}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col3">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortProperty !='lastModified'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.last_mod}"  rendered="#{questionpool.sortProperty !='lastModified'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortProperty =='lastModified' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='lastModified'}" />
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedDescending}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortProperty =='lastModified' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='lastModified'}" />
          <f:param name="orderBy" value="lastModified"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedAscending}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="thirdcolumn">
        <h:outputText value="#{pool.lastModified}">
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
	</h:outputText>
     </h:panelGroup>
    </h:column>

    <h:column id="col4">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortProperty !='questionSize'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="questionSize"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.qs}"  rendered="#{questionpool.sortProperty !='questionSize'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortProperty =='questionSize' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='questionSize'}" />
          <f:param name="orderBy" value="questionSize"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsDescending}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortProperty =='questionSize' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='questionSize'}" />
          <f:param name="orderBy" value="questionSize"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsAscending}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fourthcolumn">
        <h:outputText value="#{pool.data.questionPoolItemSize}"/>
     </h:panelGroup>
    </h:column>


    <h:column id="col5">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortProperty !='subPoolSize'}" action="#{questionpool.sortByColumnHeader}">
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.subps}"  rendered="#{questionpool.sortProperty !='subPoolSize'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" immediate="true" rendered="#{questionpool.sortProperty =='subPoolSize' && questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='subPoolSize'}" />
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsDescending}" rendered="#{questionpool.sortAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" immediate="true" rendered="#{questionpool.sortProperty =='subPoolSize' && !questionpool.sortAscending }"  action="#{questionpool.sortByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortProperty =='subPoolSize'}" />
          <f:param name="orderBy" value="subPoolSize"/>
          <f:param name="ascending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsAscending}" rendered="#{!questionpool.sortAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col6" rendered="#{questionpool.importToAuthoring == 'false'}" >
     <f:facet name="header">
       <h:outputText value="#{questionPoolMessages.remove_chbox}"/>
     </f:facet>

      <h:selectManyCheckbox onclick="checkUpdate()" onkeypress="checkUpdate()" id="removeCheckbox" value="#{questionpool.destPools}" rendered="#{pool.ownerId==questionpool.agentId}">
	    <f:selectItem itemValue="#{pool.questionPoolId}"  itemLabel=""/>
      </h:selectManyCheckbox>
    </h:column>
  </h:dataTable>
</div>
