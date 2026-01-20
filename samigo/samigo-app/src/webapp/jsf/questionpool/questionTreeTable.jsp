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

<script>includeWebjarLibrary('datatables');</script>
<script>includeWebjarLibrary('datatables-plugins');</script>
<script src="/samigo-app/js/dataTables.js"></script>
<script src="/samigo-app/js/sortHelper.js"></script>

<div class="table-responsive">
  <t:dataTable value="#{questionpool.allItems}" var="question" styleClass="table table-striped table-hover table-bordered" id="questionpool-questions" rowIndexVar="row">

<h:column id="colremove" rendered="#{questionpool.importToAuthoring eq false}" headerClass="columnCheckDelete">
  <f:facet name="header">
    <h:selectManyCheckbox immediate="true" id="selectall" onclick="toggleRemove();checkUpdate()" title="#{questionPoolMessages.t_checkAll}" styleClass="checkall checkboxTable">
      <f:selectItem itemValue="1" itemLabel="<span class=\"hidden\">Select All</span>" escape="false" />
    </h:selectManyCheckbox>
  </f:facet>
  <h:selectManyCheckbox immediate="true" id="removeCheckbox" onclick="checkUpdate()" onkeypress="checkUpdate()" value="#{questionpool.destItems}" styleClass="checkboxTable">
    <f:selectItem itemValue="#{question.itemIdString}" itemLabel=""/>
  </h:selectManyCheckbox>
</h:column>

    <h:column headerClass="columnQuestionText">
      <f:facet name="header">
		<h:panelGroup>
          <h:outputText value="#{questionPoolMessages.q_text}" />
        </h:panelGroup>
      </f:facet>

<h:commandLink title="#{questionPoolMessages.t_editQuestion}" id="modify" action="#{itemauthor.doit}" rendered="#{questionpool.canEditQuestions eq true}">

    <h:outputText escape="false" value="#{questionPoolMessages.t_editQuestion} #{row + 1} : #{question.themeText}" rendered="#{question.typeId eq 14}"/>
    <h:outputText escape="false" value="#{questionPoolMessages.t_editQuestion} #{row + 1} : #{itemContents.htmlStripped[question.text]}" rendered="#{question.typeId ne 14}"/>
    <f:actionListener
      type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
    <f:param name="itemid" value="#{question.itemId}"/>
    <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
    <f:param name="target" value="questionpool"/>
</h:commandLink>
<h:outputText escape="false" value="#{questionPoolMessages.t_editQuestion} #{questionpool.rowIndex} : #{question.themeText}" rendered="#{question.typeId eq 14 and questionpool.canEditQuestions eq false}"/>
<h:outputText escape="false" value="#{questionPoolMessages.t_editQuestion} #{questionpool.rowIndex} : #{itemContents.htmlStripped[question.text]}" rendered="#{question.typeId ne 14 and questionpool.canEditQuestions eq false}"/>

<f:verbatim><br/></f:verbatim>

 <f:verbatim><span class="itemAction"></f:verbatim>
 <h:panelGroup rendered ="#{questionpool.currentPool.numberOfQuestions ge 1}">
     <h:commandLink title="#{questionPoolMessages.t_copyQuestion}" rendered="#{questionpool.importToAuthoring eq false and questionpool.canCopyQuestions eq true}" id="copylink" immediate="true" action="#{questionpool.startCopyQuestion}">
         <h:outputText id="copy" value="#{questionPoolMessages.copy}"/>
         <f:param name="itemid" value="#{question.itemId}"/>
         <f:param name="outCome" value="editPool"/>
     </h:commandLink>
<h:outputText rendered="#{questionpool.importToAuthoring eq false and questionpool.canMoveQuestions eq true and questionpool.canCopyQuestions eq true}" value=" #{questionPoolMessages.separator} " />

<h:commandLink title="#{questionPoolMessages.t_moveQuestion}" rendered="#{questionpool.importToAuthoring eq false and questionpool.canMoveQuestions eq true}" id="movelink" immediate="true" action="#{questionpool.startMoveQuestion}">
  <h:outputText id="move" value="#{questionPoolMessages.move}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
    <f:param name="outCome" value="editPool"/>
</h:commandLink>

<h:outputText rendered="#{questionpool.importToAuthoring eq false and question.typeId eq 15}" value=" #{questionPoolMessages.separator} " />

<h:commandLink title="#{questionPoolMessages.t_showSolution}" rendered="#{questionpool.importToAuthoring eq false and question.typeId eq 15}" id="solutionlink" immediate="true" action="#{questionpool.checkSolution}">
  <h:outputText id="solution" value="#{questionPoolMessages.sol_q}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
    <f:param name="outCome" value="editPool"/>
</h:commandLink>

<h:outputText rendered="#{questionpool.importToAuthoring eq false and (questionpool.canMoveQuestions eq true or questionpool.canCopyQuestions eq true) and questionpool.canPreviewQuestions eq true}" value=" #{questionPoolMessages.separator} " />

<h:commandLink title="#{questionPoolMessages.t_previewQuestion}" rendered="#{questionpool.importToAuthoring eq false and questionpool.canPreviewQuestions eq true}" id="previewlink" immediate="true" action="#{questionpool.startPreviewQuestion}">
    <h:outputText id="preview" value="#{questionPoolMessages.preview}"/>
    <f:param name="itemid" value="#{question.itemId}"/>
</h:commandLink>
</h:panelGroup>

 <f:verbatim></span></f:verbatim>
 <f:verbatim><span class="hidden"></f:verbatim>
    <h:outputText value='#{question.getItemMetaDataByLabel("KEYWORD")}'/>
 <f:verbatim></span></f:verbatim>
    </h:column>


      <h:column rendered="#{questionpool.showTags eq true}">
          <f:facet name="header">
              <h:panelGroup>
                  <h:outputText value="#{questionPoolMessages.t_tags}" />
              </h:panelGroup>
          </f:facet>
          <t:dataList value="#{question.itemTagSet.toArray()}" var="tag" layout="unorderedList">
              <f:verbatim><span></f:verbatim>
              <h:outputText value="#{tag.tagLabel}"/>
              <f:verbatim><span class="collection"></f:verbatim>
              (<h:outputText value="#{tag.tagCollectionName}"/>)
              <f:verbatim></span></span></br> </f:verbatim>
          </t:dataList>
          <h:panelGroup rendered='#{question.getItemMetaDataByLabel("KEYWORD") ne null}'>
              <f:verbatim><ul><li><span></f:verbatim>
              <h:outputText value='#{question.getItemMetaDataByLabel("KEYWORD")}'/>
              <f:verbatim><span class="collection"></f:verbatim>
                  (<h:outputText value="#{assessmentSettingsMessages.metadata_keywords}"/>)
              <f:verbatim></span></span></br> </f:verbatim>
          </h:panelGroup>
      </h:column>



      <h:column>
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{questionPoolMessages.q_type}" />
        </h:panelGroup>
      </f:facet>
     <h:outputText rendered="#{question.typeId eq 1}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{question.typeId eq 2}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{question.typeId eq 3}" value="#{authorMessages.multiple_choice_surv}"/>
     <h:outputText rendered="#{question.typeId eq 4}" value="#{authorMessages.true_false}"/>
     <h:outputText rendered="#{question.typeId eq 5}" value="#{authorMessages.short_answer_essay}"/>
     <h:outputText rendered="#{question.typeId eq 6}" value="#{authorMessages.file_upload}"/>
     <h:outputText rendered="#{question.typeId eq 7}" value="#{authorMessages.audio_recording}"/>
     <h:outputText rendered="#{question.typeId eq 8}" value="#{authorMessages.fill_in_the_blank}"/>
     <h:outputText rendered="#{question.typeId eq 9}" value="#{authorMessages.matching}"/>
     <h:outputText rendered="#{question.typeId eq 11}" value="#{authorMessages.fill_in_numeric}"/>
     <h:outputText rendered="#{question.typeId eq 12}" value="#{authorMessages.multiple_choice_type}"/>
     <h:outputText rendered="#{question.typeId eq 14}" value="#{authorMessages.extended_matching_items}"/>
     <h:outputText rendered="#{question.typeId eq 13}" value="#{authorMessages.matrix_choice_surv}"/>
     <h:outputText rendered="#{question.typeId eq 15}" value="#{authorMessages.calculated_question}"/><!-- // CALCULATED_QUESTION -->
     <h:outputText rendered="#{question.typeId eq 16}" value="#{authorMessages.image_map_question}"/><!-- // IMAGEMAP_QUESTION -->

    </h:column>
    
    <h:column headerClass="columnPoints">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{questionPoolMessages.q_points}" />
        </h:panelGroup>
      </f:facet>
       <h:outputText value="#{question.score}" />
    </h:column>

    <h:column headerClass="columnDate">
      <f:facet name="header">
        <h:panelGroup>
          <h:outputText value="#{questionPoolMessages.last_mod}" />
        </h:panelGroup>
      </f:facet>
       <h:panelGroup styleClass="">
            <h:outputText value="#{question.lastModifiedDate}">
                <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
            </h:outputText>
            <h:outputText value="#{question.lastModifiedDate}" styleClass="hidden spanValue">
                <f:convertDateTime pattern="yyyyMMddHHmmss" />
            </h:outputText>
       </h:panelGroup>
    </h:column>

    <h:column id="colimport" rendered="#{questionpool.importToAuthoring eq true}" headerClass="columnCheckImport">
      <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="#{questionPoolMessages.impToAuthor} "/>
            <h:outputText value="#{questionPoolMessages.select_all}"/>
            <h:selectBooleanCheckbox id="importSelectAllCheck" onclick="toggleCheckboxes(this,'importCheckbox');updateButtonStatusOnCheck(document.getElementById('editform:import'), document.getElementById('editform'));" value="" />
        </h:panelGroup>
      </f:facet>
      <h:selectManyCheckbox immediate="true" id="importCheckbox" value="#{questionpool.destItems}" onclick="toggleSelectAllCheck(this,'importSelectAllCheck');updateButtonStatusOnCheck(document.getElementById('editform:import'), document.getElementById('editform'));" styleClass="checkboxTable">
        <f:selectItem itemValue="#{question.itemIdString}" itemLabel=""/>
      </h:selectManyCheckbox>
    </h:column>


  </t:dataTable>

<script>
    // Function to normalize search text
    window.normalizeSearchText = function(text) {
        return text
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");
    };

    $(document).ready(function() {
        const dataTableConfig = JSON.parse('<h:outputText value="#{questionpool.dataTableConfig.json}" />');

        const column_checkDelete = document.getElementById('editform:questionpool-questions:selectall');
        dataTableConfig['order'] = [[(column_checkDelete) ? 1 : 0, "asc"]];

        const dataTable = setupDataTable("editform:questionpool-questions", dataTableConfig);

        $(document).ready(function() {
            const table = $('#editform\\:questionpool-questions').DataTable();
            const searchInput = document.querySelector('#editform\\:questionpool-questions_filter input');

            if (table && searchInput) {
                if (searchInput.hasCustomSearch) {
                    return;
                }
                searchInput.hasCustomSearch = true;

                let lastSearchTerm = '';

                $(searchInput).off();
                searchInput.removeAttribute('data-dt-search');

                const customSearchFunction = function(settings, searchData, index, rowData, counter) {
                    if (settings.nTable.id !== 'editform:questionpool-questions') {
                        return true;
                    }

                    if (!lastSearchTerm || lastSearchTerm.trim() === '') {
                        return true;
                    }

                    const normalizedSearch = window.normalizeSearchText(lastSearchTerm);

                    return searchData.some(cellData => {
                        if (cellData && typeof cellData === 'string') {
                            const cleanCellData = cellData.replace(/<[^>]*>/g, '');
                            const normalizedCell = window.normalizeSearchText(cleanCellData);
                            return normalizedCell.includes(normalizedSearch);
                        }
                        return false;
                    });
                };

                $.fn.dataTable.ext.search.push(customSearchFunction);

                const handleSearch = function() {
                    lastSearchTerm = this.value;
                    table.draw();
                };

                searchInput.addEventListener('input', handleSearch);
                searchInput.addEventListener('keyup', handleSearch);

                if (searchInput.value) {
                    lastSearchTerm = searchInput.value;
                    table.draw();
                }
            }
        });

        dataTable.on( 'draw.dt', function () {
            checkUpdate();
        });
    });
</script>
</div>
