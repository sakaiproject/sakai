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
 <h:dataTable cellpadding="0" cellspacing="0" value="#{questionpool.allItems}" var="question" styleClass="listHier">


    <h:column>
      <f:facet name="header">      
		<h:panelGroup>
        <h:commandLink title="#{msg.t_sortQuestionText}" id="sortByTitleAction" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty ne 'text'}">
          <h:outputText value="#{msg.q_text}" />
          <f:param name="orderBy" value="text"/>
          <f:param name="ascending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>

        <h:commandLink title="#{msg.t_sortQuestionText}" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty=='text' && questionpool.sortQuestionAscending}">
          <h:outputText value="#{msg.q_text}" />
          <f:param name="orderBy" value="text"/>
          <f:param name="ascending" value="false"/>
          <h:graphicImage alt="#{msg.alt_sortQuestionTextDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>

        <h:commandLink title="#{msg.t_sortQuestionText}" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty=='text' && !questionpool.sortQuestionAscending}">
          <h:outputText value="#{msg.q_text}" />
          <f:param name="orderBy" value="text"/>
          <f:param name="ascending" value="true"/>
          <h:graphicImage alt="#{msg.alt_sortQuestionTextAscending}" rendered="#{author.publishedAscending}" url="/images/sortdescending.gif"/>          
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>
        </h:panelGroup>
      </f:facet>

<h:commandLink title="#{msg.t_editQuestion}" id="modify" action="#{itemauthor.doit}">
    <h:outputText escape="false" value="#{question.textHtmlStripped}" />
    <f:actionListener
      type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
    <f:param name="itemid" value="#{question.itemId}"/>
    <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
    <f:param name="target" value="questionpool"/>
</h:commandLink>

<f:verbatim><br/></f:verbatim>

 <f:verbatim><span class="itemAction"></f:verbatim>
<h:commandLink title="#{msg.t_copyQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="copylink" immediate="true" action="#{questionpool.startCopyQuestion}">
  <h:outputText id="copy" value="#{msg.copy}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
</h:commandLink>
<h:outputText  rendered="#{questionpool.importToAuthoring != 'true'}" value=" #{msg.separator} " />

<h:commandLink title="#{msg.t_moveQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="movelink" immediate="true" action="#{questionpool.startMoveQuestion}">
  <h:outputText id="move" value="#{msg.move}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
</h:commandLink>
<h:outputText rendered="#{questionpool.importToAuthoring != 'true'}"  value=" #{msg.separator} " />
<%-- export to same page --%>
<%--
<h:commandLink title="#{msg.t_exportQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}"  id="exportlink" immediate="true" action="xmlDisplay" target="_qti_export">
  <h:outputText id="export" value="#{msg.export}"/>
  <f:param name="itemId" value="#{question.itemId}"/>
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ExportItemListener" />
</h:commandLink>
--%>
<%-- export to popup --%>
<h:outputLink title="#{msg.t_exportQuestion}" value="#" rendered="#{questionpool.importToAuthoring != 'true'}"
 onclick=
 "window.open( '/samigo/jsf/qti/exportItem.faces?exportItemId=#{question.itemId}','_qti_export', 'toolbar=no,menubar=yes,personalbar=no,width=600,height=190,scrollbars=no,resizable=no');"
onkeypress=
 "window.open( '/samigo/jsf/qti/exportItem.faces?exportItemId=#{question.itemId}','_qti_export', 'toolbar=no,menubar=yes,personalbar=no,width=600,height=190,scrollbars=no,resizable=no');"
  ><h:outputText id="export" value="#{msg.export}"/>
</h:outputLink>

<h:outputText rendered="#{questionpool.importToAuthoring != 'true'}"  value=" #{msg.separator} " />
<h:commandLink title="#{msg.t_previewQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="previewlink" immediate="true" action="previewQuestion">
  <h:outputText id="preview" value="#{msg.preview}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.PreviewAssessmentListener" />
</h:commandLink>
 <f:verbatim></span></f:verbatim>
    </h:column>


    <h:column>
      <f:facet name="header">
        <h:panelGroup>
        <h:commandLink title="#{msg.t_sortQuestionType}" id="sortByTypeAction" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty ne 'keyword'}">
          <h:outputText value="#{msg.q_type}" />
          <f:param name="orderBy" value="keyword"/>
          <f:param name="ascending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>

        <h:commandLink title="#{msg.t_sortQuestionType}" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty=='keyword' && questionpool.sortQuestionAscending}">
          <h:outputText value="#{msg.q_type}" />
          <f:param name="orderBy" value="keyword"/>
          <f:param name="ascending" value="false"/>
          <h:graphicImage alt="#{msg.alt_sortQuestionTypeDescending}" rendered="#{author.publishedAscending}" url="/images/sortascending.gif"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>

        <h:commandLink title="#{msg.t_sortQuestionType}" immediate="true" action="editPool" rendered="#{questionpool.sortQuestionProperty=='keyword' && !questionpool.sortQuestionAscending}">
          <h:outputText value="#{msg.q_type}" />
          <f:param name="orderBy" value="keyword"/>
          <f:param name="ascending" value="true"/>
          <h:graphicImage alt="#{msg.alt_sortQuestionTypeAscending}" rendered="#{author.publishedAscending}" url="/images/sortdescending.gif"/>          
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.SortQuestionListListener" />
        </h:commandLink>
        </h:panelGroup>
      </f:facet>
     <h:outputText rendered="#{question.typeId== 1}" value="#{authmsg.multiple_choice_type}"/>
     <h:outputText rendered="#{question.typeId== 2}" value="#{authmsg.multiple_choice_type}"/>
     <h:outputText rendered="#{question.typeId== 3}" value="#{authmsg.multiple_choice_surv}"/>
     <h:outputText rendered="#{question.typeId== 4}" value="#{authmsg.true_false}"/>
     <h:outputText rendered="#{question.typeId== 5}" value="#{authmsg.short_answer_essay}"/>
     <h:outputText rendered="#{question.typeId== 6}" value="#{authmsg.file_upload}"/>
     <h:outputText rendered="#{question.typeId== 7}" value="#{authmsg.audio_recording}"/>
     <h:outputText rendered="#{question.typeId== 8}" value="#{authmsg.fill_in_the_blank}"/>
     <h:outputText rendered="#{question.typeId== 9}" value="#{authmsg.matching}"/>
     <h:outputText rendered="#{question.typeId== 11}" value="#{authmsg.fill_in_numeric}"/>
     

    </h:column>

    <h:column id="colremove" rendered="#{questionpool.importToAuthoring == 'false'}" >
      <f:facet name="header">
        <h:outputText value="#{msg.remove_chbox}"/>
      </f:facet>
 <h:selectManyCheckbox immediate="true" id="removeCheckbox" onclick="checkUpdate()" onkeypress="checkUpdate()"  value ="#{questionpool.destItems}">
         <f:selectItem itemValue="#{question.itemIdString}" itemLabel=""/>
 </h:selectManyCheckbox>
     </h:column>

    <h:column id="colimport" rendered="#{questionpool.importToAuthoring == 'true'}" >
      <f:facet name="header">
        <h:outputText value="#{msg.impToAuthor}"/>
      </f:facet>
 <h:selectManyCheckbox immediate="true" id="importCheckbox" value ="#{questionpool.destItems}">
         <f:selectItem itemValue="#{question.itemIdString}" itemLabel=""/>
 </h:selectManyCheckbox>
     </h:column>


  </h:dataTable>
