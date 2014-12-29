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
<h:panelGrid  rendered="#{questionpool.actionType == 'pool'}" columns="2">
	<h:selectOneRadio onclick="uncheckOthers(this);" onkeypress="uncheckOthers(this);" id="radiobtn"
		layout="pageDirection" value="#{questionpool.destPool}">
       		<f:selectItem itemValue="0" itemLabel=""/>
	</h:selectOneRadio>

	<h:outputText value="#{questionPoolMessages.q_mgr}"/>
</h:panelGrid>

<h:dataTable cellpadding="0" cellspacing="0" id="TreeTable" value="#{questionpool.moveQpools}"
   var="pool" styleClass="listHier" >

    <h:column  id="radiocol">
<f:facet name="header">
<h:outputText value=" "/>
</f:facet>

<h:selectOneRadio rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}" onclick="uncheckOthers(this);" onkeypress="uncheckOthers(this);" id="radiobtn" layout="pageDirection"
		value="#{questionpool.destPool}">
                <f:selectItem itemValue="#{pool.questionPoolId}" itemLabel=""/>
</h:selectOneRadio>

    </h:column>

    <h:column id="col1">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortTitle}" id="sortByTitle" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='title'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.p_name}"  rendered="#{questionpool.sortMovePoolProperty !='title'}" />
       </h:commandLink>
      
       <h:commandLink  title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='title' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='title'}" />
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleDescending}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='title' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='title'}" />
          <f:param name="movePoolOrderBy" value="title"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleAscending}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>


<h:panelGroup rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}" styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">

<%--
<h:panelGroup styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">
--%>
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>

<h:outputLink title="" id="parenttogglelink"  onclick="toggleRowsForSelectList(this)" onkeypress="toggleRowsForSelectList(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
<h:graphicImage alt="#{questionPoolMessages.alt_togglelink}" id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink title="" id="togglelink" value="#" styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
<h:graphicImage alt="#{questionPoolMessages.alt_togglelink}" id="spacer_for_mozilla1" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>



  <h:outputText id="poolnametext" value="#{pool.displayName}" escape="false"/>

</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink  title="#{questionPoolMessages.t_sortCreator}" id="sortByOwner" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='ownerId'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.creator}"  rendered="#{questionpool.sortMovePoolProperty !='ownerId'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='ownerId' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='ownerId'}" />
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorDescending}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='ownerId' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='ownerId'}" />
          <f:param name="movePoolOrderBy" value="ownerId"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorAscending}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="secondcolumn" rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}">
        <h:outputText value="#{pool.ownerDisplayName}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col3">
     <f:facet name="header">
       <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='lastModified'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.last_mod}"  rendered="#{questionpool.sortMovePoolProperty !='lastModified'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='lastModified' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='lastModified'}" />
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedDescending}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='lastModified' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='lastModified'}" />
          <f:param name="movePoolOrderBy" value="lastModified"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedAscending}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="thirdcolumn" rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}">
        <h:outputText value="#{pool.lastModified}">
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
     </h:panelGroup>
    </h:column>

    <h:column id="col4">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='questionSize'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="questionSize"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.qs}"  rendered="#{questionpool.sortMovePoolProperty !='questionSize'}" />
       </h:commandLink>
     
       <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='questionSize' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
            <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='questionSize'}" />
          <f:param name="movePoolOrderBy" value="questionSize"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsDescending}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='questionSize' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='questionSize'}" />
          <f:param name="movePoolOrderBy" value="questionSize"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsAscending}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fourthcolumn" rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}">
        <h:outputText value="#{pool.data.questionPoolItemSize}"/>
     </h:panelGroup>
    </h:column>


    <h:column id="col5">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.subps}" id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortMovePoolProperty !='subPoolSize'}" action="#{questionpool.sortMovePoolByColumnHeader}">
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.subps}"  rendered="#{questionpool.sortMovePoolProperty !='subPoolSize'}" />
       </h:commandLink>
       
       <h:commandLink title="#{questionPoolMessages.subps}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize' && questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize'}" />
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsDescending}" rendered="#{questionpool.sortMovePoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.subps}" immediate="true" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize' && !questionpool.sortMovePoolAscending }"  action="#{questionpool.sortMovePoolByColumnHeader}">
          <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortMovePoolProperty =='subPoolSize'}" />
          <f:param name="movePoolOrderBy" value="subPoolSize"/>
          <f:param name="movePoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsAscending}" rendered="#{!questionpool.sortMovePoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
      </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn" rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>

</h:dataTable>

