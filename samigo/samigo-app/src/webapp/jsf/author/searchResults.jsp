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

    <h:panelGroup id="text_searched_results" rendered="#{not empty searchQuestionBean.textToSearch}">
        <h3><h:outputLabel value="#{authorMessages.results_text}" /></h3>
    </h:panelGroup>

    <h:panelGroup id="tags_searched_results" rendered="#{not empty searchQuestionBean.tagToSearchLabel}">
        <h3><h:outputLabel value="#{authorMessages.results_tag}"/></h3>
    </h:panelGroup>
    <div class="col-xs-12 col-sm-12 col-md-12 instruction"><h:outputText value="#{authorMessages.preview_tip}" /></div>

    <script>
        $(function() {
            $('a.uimodal').on('click', function() {
                var href = $(this).attr('href');
                var title = $(this).attr('title');
                var loading = $(this).attr('loading');
                $('<div id=QuestionPreview>').html(loading).dialog({
                    modal: true,
                    open: function () {
                        $.get(href).done(function(data, status, XHR){
                            handleData(data);
                        });
                    },

                    close: function (e) {
                        $(this).empty();
                        $(this).dialog('destroy');
                    },
                    height: 350,
                    width: 540,
                    title: title

                });
                return false;
            });
        });


        function handleData( responseData ) {
            $("#QuestionPreview").html(responseData);
        }



    </script>

    <t:dataTable value="#{searchQuestionBean.results.entrySet().toArray()}" var="item_search_result" styleClass="table table-striped tablesorter" id="searchquestions-questions" rowIndexVar="row">

    <h:column>
    <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="" />
        </h:panelGroup>
    </f:facet>


    <f:verbatim><span><a href="/portal/tool/</f:verbatim>
<h:outputText value="#{requestScope['sakai.tool.placement.id']}" />
<f:verbatim>/jsf/author/searchPreview.faces?idString=</f:verbatim><h:outputText value="#{item_search_result.value.idString}" />
<f:verbatim>&typeId=</f:verbatim><h:outputText value="#{item_search_result.value.typeId}" />
<f:verbatim>" class="uimodal" loading="</f:verbatim><h:outputText value="#{authorMessages.loading}" /><f:verbatim>" title="</f:verbatim><h:outputText value="#{authorMessages.preview}" /><f:verbatim>"   ><i class="fa fa-eye" aria-hidden="true"></i></a></f:verbatim>

               </h:column>


      <h:column>
          <f:facet name="header">
              <h:panelGroup>
                  <h:outputText value="#{questionPoolMessages.q_type}" />
              </h:panelGroup>
          </f:facet>
          <h:outputText rendered="#{item_search_result.value.typeId== '1'}" value="#{authorMessages.multiple_choice_type}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '2'}" value="#{authorMessages.multiple_choice_type}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '3'}" value="#{authorMessages.multiple_choice_surv}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '4'}" value="#{authorMessages.true_false}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '5'}" value="#{authorMessages.short_answer_essay}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '6'}" value="#{authorMessages.file_upload}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '7'}" value="#{authorMessages.audio_recording}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '8'}" value="#{authorMessages.fill_in_the_blank}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '9'}" value="#{authorMessages.matching}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '11'}" value="#{authorMessages.fill_in_numeric}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '12'}" value="#{authorMessages.multiple_choice_type}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '14'}" value="#{authorMessages.extended_matching_items}"/>
          <h:outputText rendered="#{item_search_result.value.typeId== '13'}" value="#{authorMessages.matrix_choice_surv}"/>
    <h:outputText rendered="#{item_search_result.value.typeId== '15'}" value="#{authorMessages.calculated_question}"/><!-- // CALCULATED_QUESTION -->
    <h:outputText rendered="#{item_search_result.value.typeId== '16'}" value="#{authorMessages.image_map_question}"/><!-- // IMAGEMAP_QUESTION -->

      </h:column>

    <h:column>
      <f:facet name="header">      
		<h:panelGroup>
          <h:outputText value="#{questionPoolMessages.q_text}" />
        </h:panelGroup>
      </f:facet>
        <h:outputText escape="false" value="#{row + 1} : #{item_search_result.value.qText}" />

    </h:column>


      <h:column rendered="#{searchQuestionBean.showTags == 'true'}" >
          <f:facet name="header">
              <h:panelGroup>
                  <h:outputText value="#{questionPoolMessages.t_tags}" />
              </h:panelGroup>
          </f:facet>
          <t:dataList value="#{item_search_result.value.tagSet.toArray()}" var="tag" layout="unorderedList">
              <f:verbatim><span></f:verbatim>
              <h:outputText value="#{tag}"/>
        <f:verbatim></span></br>  </f:verbatim>
          </t:dataList>
      </h:column>

      <h:column>
          <f:facet name="header">
              <h:panelGroup>
                  <h:outputText value="#{authorMessages.origin}" />
              </h:panelGroup>
          </f:facet>
          <t:dataList value="#{item_search_result.value.origin}" var="origin">
              <f:verbatim><span></f:verbatim>
              <h:outputText value="#{origin}"/>
              <f:verbatim></span></br>  </f:verbatim>
          </t:dataList>
      </h:column>


    <h:column id="colimport" >
      <f:facet name="header">
        <h:panelGroup>
            <h:outputText value="#{questionPoolMessages.impToAuthor} "/>
            <h:outputText value="#{questionPoolMessages.select_all}"/>&nbsp;
            <h:selectBooleanCheckbox id="importSelectAllCheck" onclick="toggleCheckboxes(this,'importCheckbox');updateButtonStatusOnCheck(document.getElementById('editform:import'), document.getElementById('editform'));" value="" />
        </h:panelGroup>
      </f:facet>
      <h:selectManyCheckbox immediate="true" id="importCheckbox" value="#{searchQuestionBean.destItems}" onclick="toggleSelectAllCheck(this,'importSelectAllCheck');updateButtonStatusOnCheck(document.getElementById('editform:import'), document.getElementById('editform'));">
        <f:selectItem itemValue="#{item_search_result.value.idString}" itemLabel=""/>
      </h:selectManyCheckbox>
    </h:column>


  </t:dataTable>
</div>
