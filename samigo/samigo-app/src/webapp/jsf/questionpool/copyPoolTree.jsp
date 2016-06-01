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

	<h:selectManyCheckbox id="checkboxes" layout="pageDirection" value="#{questionpool.destPools}" onclick="updateButtonStatusOnCheck(document.getElementById('copyPool:copypoolsubmit'), document.getElementById('copyPool'));">
		<f:selectItem itemValue="0" itemLabel=""/>
	</h:selectManyCheckbox>

        <h:outputText value="#{questionPoolMessages.q_mgr}"/>
</h:panelGrid>


<h:dataTable id="TreeTable" value="#{questionpool.copyQpools}"
     var="pool" cellpadding="0" cellspacing="0" styleClass="listHier" >
  

    <h:column  id="radiocol">
<f:facet name="header">
<h:outputText value=" "/>
</f:facet>

<h:selectManyCheckbox  rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}" id="checkboxes" layout="pageDirection"
					   value="#{questionpool.destPools}" onclick="updateButtonStatusesOnCheck([document.getElementById('copyPool:copypoolsubmit'), document.getElementById('copyPool:copyitemsubmit')], document.getElementById('copyPool'));">
                <f:selectItem itemValue="#{pool.questionPoolId}" itemLabel=""/>
</h:selectManyCheckbox>

    </h:column>

    <h:column id="col1">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{questionPoolMessages.t_sortTitle}" id="sortByTitle" immediate="true"  rendered="#{questionpool.sortCopyPoolProperty !='title'}" action="#{questionpool.sortCopyPoolByColumnHeader}">
          <f:param name="copyPoolOrderBy" value="title"/>
          <f:param name="copyPoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.p_name}"  rendered="#{questionpool.sortCopyPoolProperty !='title'}" />
       </h:commandLink>

       

       <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='title' && questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
          <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='title'}" />
          <f:param name="copyPoolOrderBy" value="title"/>
          <f:param name="copyPoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleDescending}" rendered="#{questionpool.sortCopyPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortTitle}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='title' && !questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
          <h:outputText  value="#{questionPoolMessages.p_name}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='title'}" />
          <f:param name="copyPoolOrderBy" value="title"/>
          <f:param name="copyPoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortTitleAscending}"  rendered="#{!questionpool.sortCopyPoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>


<h:panelGroup rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}" styleClass="tier#{questionpool.tree.currentLevel}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>

<h:outputLink title="" id="parenttogglelink"  onclick="toggleRowsForSelectList(this)" onkeypress="toggleRowsForSelectList(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
<h:graphicImage alt="#{questionPoolMessages.alt_togglelink}" id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink id="togglelink" title="" value="#" styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
<h:graphicImage id="spacer_for_mozilla1" alt="#{questionPoolMessages.alt_togglelink}" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>



  <h:outputText id="poolnametext" value="#{pool.displayName}" escape="false"/>

</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink  title="#{questionPoolMessages.t_sortCreator}" id="sortByOwner" immediate="true"  rendered="#{questionpool.sortCopyPoolProperty !='ownerId'}" action="#{questionpool.sortCopyPoolByColumnHeader}">
          <f:param name="copyPoolOrderBy" value="ownerId"/>
          <f:param name="copyPoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.creator}"  rendered="#{questionpool.sortCopyPoolProperty !='ownerId'}" />
       </h:commandLink>
      
       <h:commandLink  title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='ownerId' && questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='ownerId'}" />
          <f:param name="copyPoolOrderBy" value="ownerId"/>
          <f:param name="copyPoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorDescending}" rendered="#{questionpool.sortCopyPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortCreator}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='ownerId' && !questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.creator}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='ownerId'}" />
          <f:param name="copyPoolOrderBy" value="ownerId"/>
          <f:param name="copyPoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortCreatorDescending}" rendered="#{!questionpool.sortCopyPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink  title="#{questionPoolMessages.t_sortLastModified}" id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortCopyPoolProperty !='lastModified'}" action="#{questionpool.sortCopyPoolByColumnHeader}">
          <f:param name="copyPoolOrderBy" value="lastModified"/>
          <f:param name="copyPoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.last_mod}"  rendered="#{questionpool.sortCopyPoolProperty !='lastModified'}" />
       </h:commandLink>
     
       <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='lastModified' && questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
            <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='lastModified'}" />
          <f:param name="copyPoolOrderBy" value="lastModified"/>
          <f:param name="copyPoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedDescending}" rendered="#{questionpool.sortCopyPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='lastModified' && !questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
            <h:outputText  value="#{questionPoolMessages.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='lastModified'}" />
          <f:param name="copyPoolOrderBy" value="lastModified"/>
          <f:param name="copyPoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortLastModifiedAscending}" rendered="#{!questionpool.sortCopyPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink  title="#{questionPoolMessages.t_sortNumQuestions}" id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortCopyPoolProperty !='questionSize'}" action="#{questionpool.sortCopyPoolByColumnHeader}">
          <f:param name="copyPoolOrderBy" value="questionSize"/>
          <f:param name="copyPoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.qs}"  rendered="#{questionpool.sortCopyPoolProperty !='questionSize'}" />
       </h:commandLink>
      
       <h:commandLink  title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='questionSize' && questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='questionSize'}" />
          <f:param name="copyPoolOrderBy" value="questionSize"/>
          <f:param name="copyPoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsDescending}" rendered="#{questionpool.sortCopyPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink  title="#{questionPoolMessages.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='questionSize' && !questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.qs}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='questionSize'}" />
          <f:param name="copyPoolOrderBy" value="questionSize"/>
          <f:param name="copyPoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumQuestionsAscending}" rendered="#{!questionpool.sortCopyPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortCopyPoolProperty !='subPoolSize'}" action="#{questionpool.sortCopyPoolByColumnHeader}">
          <f:param name="copyPoolOrderBy" value="subPoolSize"/>
          <f:param name="copyPoolAscending" value="true"/>
          <h:outputText  value="#{questionPoolMessages.subps}"  rendered="#{questionpool.sortCopyPoolProperty !='subPoolSize'}" />
       </h:commandLink>
      
       <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='subPoolSize' && questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='subPoolSize'}" />
          <f:param name="copyPoolOrderBy" value="subPoolSize"/>
          <f:param name="copyPoolAscending" value="false" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsDescending}" rendered="#{questionpool.sortCopyPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{questionPoolMessages.t_sortNumSubpools}" immediate="true" rendered="#{questionpool.sortCopyPoolProperty =='subPoolSize' && !questionpool.sortCopyPoolAscending }"  action="#{questionpool.sortCopyPoolByColumnHeader}">
           <h:outputText  value="#{questionPoolMessages.subps}" styleClass="currentSort" rendered="#{questionpool.sortCopyPoolProperty =='subPoolSize'}" />
          <f:param name="copyPoolOrderBy" value="subPoolSize"/>
          <f:param name="copyPoolAscending" value="true" />
          <h:graphicImage alt="#{questionPoolMessages.alt_sortNumSubpoolsAscending}" rendered="#{!questionpool.sortCopyPoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn" rendered="#{!(questionpool.selfOrDescendant && questionpool.actionType == 'pool')}">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>


</h:dataTable>

