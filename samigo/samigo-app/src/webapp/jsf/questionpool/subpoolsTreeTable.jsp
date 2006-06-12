<!--
* $Id$
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
<h:dataTable id="TreeTable" cellpadding="0" cellspacing="0" value="#{questionpool.sortedSubqpools}"
	 var="pool" styleClass="listHier" >

    <h:column id="col1">
     <f:facet name="header">
      <h:panelGroup>
       <h:commandLink title="#{msg.t_sortTitle}" id="sortByTitle" immediate="true"  rendered="#{questionpool.sortSubPoolProperty !='title'}" action="#{questionpool.sortSubPoolByColumnHeader}">
          <f:param name="subPoolOrderBy" value="title"/>
          <f:param name="subPoolAscending" value="true"/>
          <h:outputText  value="#{msg.p_name}"  rendered="#{questionpool.sortSubPoolProperty !='title'}" />
       </h:commandLink>
      
       <h:commandLink title="#{msg.t_sortTitle}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='title' && questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
          <h:outputText  value="#{msg.p_name}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='title'}" />
          <f:param name="subPoolOrderBy" value="title"/>
          <f:param name="subPoolAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortTitleDescending}" rendered="#{questionpool.sortSubPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{msg.t_sortTitle}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='title' && !questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
           <h:outputText  value="#{msg.p_name}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='title'}" />
          <f:param name="subPoolOrderBy" value="title"/>
          <f:param name="subPoolAscending" value="true" />
          <h:graphicImage alt="#{msg.alt_sortTitleAscending}" rendered="#{!questionpool.sortSubPoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
     </h:panelGroup>
     </f:facet>

<h:panelGroup styleClass="tier#{questionpool.tree.currentLevel-questionpool.parentPoolSize-1}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{questionpool.tree.currentObjectHTMLId}"/>

<h:outputLink title="" id="parenttogglelink"  onclick="toggleRows(this)" value="#" styleClass="treefolder" rendered="#{questionpool.tree.hasChildList}" >
<h:graphicImage alt="#{msg.alt_togglelink}" id="spacer_for_mozilla" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>
<h:outputLink id="togglelink"  styleClass="treedoc" rendered="#{questionpool.tree.hasNoChildList}" >
<h:graphicImage alt="#{msg.togglelink}" id="spacer_for_mozilla1" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
</h:outputLink>



<h:commandLink title="#{msg.t_editPool}" id="editlink" immediate="true" action="#{questionpool.editPool}">
  <h:outputText id="poolnametext" value="#{pool.displayName}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>

<f:verbatim><br/></f:verbatim>
<h:graphicImage id="spacer" style="border:0" height="14" width="30" value="/images/delivery/spacer.gif" />
 <f:verbatim><span class="itemAction"></f:verbatim>
<h:commandLink title="#{msg.t_addSubpool}" rendered="#{questionpool.importToAuthoring != 'true'}"  id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText id="add" value="#{msg.add}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />
<h:commandLink title="#{msg.t_copyPool}" rendered="#{questionpool.importToAuthoring != 'true'}" id="copylink" immediate="true" action="#{questionpool.startCopyPool}">
  <h:outputText id="copy" value="#{msg.copy}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />

<h:commandLink title="#{msg.t_movePool}" rendered="#{questionpool.importToAuthoring != 'true'}"  id="movelink" immediate="true" action="#{questionpool.startMovePool}">
  <h:outputText id="move" value="#{msg.move}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
<%--
<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />
<h:commandLink rendered="#{questionpool.importToAuthoring != 'true'}" id="exportlink" immediate="true" action="#{questionpool.exportPool}">
  <h:outputText id="export" value="#{msg.export}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
--%>

<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" | " />
<h:commandLink title="#{msg.t_removePool}" rendered="#{questionpool.importToAuthoring != 'true'}"  id="removelink" immediate="true" action="#{questionpool.confirmRemovePool}">
  <h:outputText id="remove" value="#{msg.remove}"/>
  <f:param name="qpid" value="#{pool.questionPoolId}"/>
</h:commandLink>
 <f:verbatim></span></f:verbatim>

</h:panelGroup>
    </h:column>
    <h:column id="col2">
     <f:facet name="header">
    <h:panelGroup>
       <h:commandLink title="#{msg.t_sortCreator}" id="sortByOwner" immediate="true"  rendered="#{questionpool.sortSubPoolProperty !='ownerId'}" action="#{questionpool.sortSubPoolByColumnHeader}">
          <f:param name="subPoolOrderBy" value="ownerId"/>
          <f:param name="subPoolAscending" value="true"/>
          <h:outputText  value="#{msg.creator}"  rendered="#{questionpool.sortSubPoolProperty !='ownerId'}" />
       </h:commandLink>
      
       <h:commandLink title="#{msg.t_sortCreator}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='ownerId' && questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
           <h:outputText  value="#{msg.creator}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='ownerId'}" />
          <f:param name="subPoolOrderBy" value="ownerId"/>
          <f:param name="subPoolAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortCreatorDescending}" rendered="#{questionpool.sortSubPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{msg.t_sortCreator}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='ownerId' && !questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
           <h:outputText  value="#{msg.creator}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='ownerId'}" />
          <f:param name="subPoolOrderBy" value="ownerId"/>
          <f:param name="subPoolAscending" value="true" />
          <h:graphicImage alt="#{msg.alt_sortCreatorAscending}" rendered="#{!questionpool.sortSubPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink title="#{msg.t_sortLastModified}" id="sortByLastModified" immediate="true"  rendered="#{questionpool.sortSubPoolProperty !='lastModified'}" action="#{questionpool.sortSubPoolByColumnHeader}">
          <f:param name="subPoolOrderBy" value="lastModified"/>
          <f:param name="subPoolAscending" value="true"/>
          <h:outputText  value="#{msg.last_mod}"  rendered="#{questionpool.sortSubPoolProperty !='lastModified'}" />
       </h:commandLink>
      
       <h:commandLink title="#{msg.t_sortLastModified}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='lastModified' && questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
          <h:outputText  value="#{msg.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='lastModified'}" />
          <f:param name="subPoolOrderBy" value="lastModified"/>
          <f:param name="subPoolAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortLastModifiedDescending}" rendered="#{questionpool.sortSubPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{msg.t_sortLastModified}"  immediate="true" rendered="#{questionpool.sortSubPoolProperty =='lastModified' && !questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
           <h:outputText  value="#{msg.last_mod}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='lastModified'}" />
          <f:param name="subPoolOrderBy" value="lastModified"/>
          <f:param name="subPoolAscending" value="true" />
          <h:graphicImage alt="#{msg.alt_sortLastModifiedAscending}" rendered="#{!questionpool.sortSubPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink title="#{msg.t_sortNumQuestions}" id="sortByQuestion" immediate="true"  rendered="#{questionpool.sortSubPoolProperty !='questionSize'}" action="#{questionpool.sortSubPoolByColumnHeader}">
          <f:param name="subPoolOrderBy" value="questionSize"/>
          <f:param name="subPoolAscending" value="true"/>
          <h:outputText  value="#{msg.qs}"  rendered="#{questionpool.sortSubPoolProperty !='questionSize'}" />
       </h:commandLink>
     
       <h:commandLink title="#{msg.t_sortNumQuestions}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='questionSize' && questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
            <h:outputText  value="#{msg.qs}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='questionSize'}" />
          <f:param name="subPoolOrderBy" value="questionSize"/>
          <f:param name="subPoolAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortNumQuestionsDescending}" rendered="#{questionpool.sortSubPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{msg.t_sortNumQuestions}"  immediate="true" rendered="#{questionpool.sortSubPoolProperty =='questionSize' && !questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
            <h:outputText  value="#{msg.qs}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='questionSize'}" />
          <f:param name="subPoolOrderBy" value="questionSize"/>
          <f:param name="subPoolAscending" value="true" />
          <h:graphicImage alt="#{msg.alt_sortNumQuestionAscending}" rendered="#{!questionpool.sortSubPoolAscending}" url="/images/sortdescending.gif"/>
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
       <h:commandLink title="#{msg.t_sortNumSubpools}" id="sortBySubPool" immediate="true"  rendered="#{questionpool.sortSubPoolProperty !='subPoolSize'}" action="#{questionpool.sortSubPoolByColumnHeader}">
          <f:param name="subPoolOrderBy" value="subPoolSize"/>
          <f:param name="subPoolAscending" value="true"/>
          <h:outputText  value="#{msg.subps}"  rendered="#{questionpool.sortSubPoolProperty !='subPoolSize'}" />
       </h:commandLink>
      
       <h:commandLink title="#{msg.t_sortNumSubpools}"  immediate="true" rendered="#{questionpool.sortSubPoolProperty =='subPoolSize' && questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
           <h:outputText  value="#{msg.subps}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='subPoolSize'}" />
          <f:param name="subPoolOrderBy" value="subPoolSize"/>
          <f:param name="subPoolAscending" value="false" />
          <h:graphicImage alt="#{msg.alt_sortNumSubpoolsDescending}" rendered="#{questionpool.sortSubPoolAscending}" url="/images/sortascending.gif"/>
      </h:commandLink>
      <h:commandLink title="#{msg.t_sortNumSubpools}" immediate="true" rendered="#{questionpool.sortSubPoolProperty =='subPoolSize' && !questionpool.sortSubPoolAscending }"  action="#{questionpool.sortSubPoolByColumnHeader}">
            <h:outputText  value="#{msg.subps}" styleClass="currentSort" rendered="#{questionpool.sortSubPoolProperty =='subPoolSize'}" />
          <f:param name="subPoolOrderBy" value="subPoolSize"/>
          <f:param name="subPoolAscending" value="true" />
          <h:graphicImage alt="#{msg.alt_sortNumSubpoolsAscending}" rendered="#{!questionpool.sortSubPoolAscending}" url="/images/sortdescending.gif"/>
      </h:commandLink>
      </h:panelGroup>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.subPoolSize}"/>
     </h:panelGroup>
    </h:column>


  </h:dataTable>

