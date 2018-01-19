<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2016 The Sakai Foundation.
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
<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head><%= request.getAttribute("html.head") %>
        <title><h:outputText value="#{authorMessages.search_question}" /></title>
        <samigo:script path="/js/authoring.js"/>


        <script type="text/JavaScript">
            <%@ include file="/js/samigotree.js" %>

            function textCounter(field, maxlimit) {
                if (field.value.length > maxlimit) // if too long...trim it!
                    field.value = field.value.substring(0, maxlimit);
            }

            jQuery(window).load(function(){
                //If we need to do something on load
            });
        </script>

        <samigo:script path="/../library/webjars/jquery/1.12.4/jquery.min.js"/>
        <samigo:script path="/../library/js/spinner.js" type="text/javascript"/>
        <samigo:script path="/../library/webjars/select2/4.0.3/dist/js/select2.full.min.js"/>
        <samigo:stylesheet path="/../library/webjars/select2/4.0.3/dist/css/select2.css"/>

        <script type="text/javascript">
            function initPage()
            {
                disableIt();
                // TODO do we need this? checkUpdate();
                var importButton = document.getElementById('editform:import');
                if (importButton !== null)
                {
                    importButton.disabled=true;
                }

                if ( $('#editform\\:searchquestions-questions\\:importSelectAllCheck').length) {
                    $('#editform\\:searchquestions-questions\\:importSelectAllCheck').focus();
                }else{
                    if ("<h:outputText value="#{searchQuestionBean.lastSearchType}"/>"  == "text"){
                        document.getElementById('editform:textToSearch').focus();
                    }else{
                        $(".tag_selector").focus();
                     }
                }

            }
            window.onload = initPage;


            $(document).ready(function() {
                $('#editform\\:textToSearch').keypress(function(event){
                    if(event.keyCode == 13){
                        $('#editform\\:searchByTextAND').click();
                    }
                });
                $(".tag_selector").select2({
                    width: '100%',
                    language: "<h:outputText value="#{itemauthor.language}"/>",
                    closeOnSelect: false,
                    ajax: {
                        url: "/direct/tagservice/getTagsPaginatedByPrefixInLabel.json",
                        dataType: 'json',
                        delay: 500,
                        data: function (params) {
                            return {
                                prefix: params.term, // search term
                                page:params.page
                            };
                        },
                        processResults: function (data, params) {
                            // parse the results into the format expected by Select2
                            // since we are using custom formatting functions we do not need to
                            // alter the remote JSON data, except to indicate that infinite
                            // scrolling can be used
                            params.page = params.page || 1;

                            return {
                                results: $.map(data.data.tags, function (item) {
                                    return {
                                        text: item.tagLabel,
                                        description: item.tagDescription,
                                        id: item.tagId,
                                        collection:item.collectionName
                                    }
                                }),
                                pagination: {more: (params.page * 30) < data.data.total}
                            };
                        },
                        cache: true
                    },
                    escapeMarkup: function (markup) { return markup; }, // let our custom formatter work
                    minimumInputLength: 1,
                    templateResult: formatRepo,
                    templateSelection: formatRepoSelection
                })

                $('.tag_selector').on("select2:unselect", function(e){
                    $('.tag_selector option').each(function() {
                        if ( $(this).val() == e.params.data.id ) {
                            $(this).remove();
                        }
                    });
                }).trigger('change');
            });

            function formatRepo (repo) {
                var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
                return $tag_formatted;
            }

            function formatRepoSelection (repo) {
                if (typeof repo.collection != 'undefined') {
                    var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
                }else{
                    var $tag_formatted = $("<span>" + repo.text + "</span>");
                }
                return $tag_formatted;
            }


        </script>
    </head>

    <body onload="document.forms[0].reset(); disableIt(); resetSelectMenus(); ;<%= request.getAttribute("html.body.onload") %>">

    <div class="portletBody container-fluid">
        <h:form id="editform">
            <%@ include file="/jsf/author/allHeadings.jsp" %>
            <h2><h:outputText value="#{authorMessages.search_question}"/></h2>

            <div class="row" <h:outputText value="#{searchQuestionBean.tagDisabled}"/>>
                <div class="col-xs-12 col-sm-12 col-md-12">
                    <h3><h:outputText value="#{authorMessages.tag_search}"/></h3>
                </div>
                <br/>
                <div class="col-xs-12 col-md-6">
                    <select class="tag_selector" name="tag_selector[]" id="tag_selector" multiple="multiple">
                    </select>
                </div>
                </br>
                <div class="col-xs-12 col-md-6">
                    <h:commandButton id="searchByTagAND"  action="#{searchQuestionBean.doit}"
                                     onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_tag_and}">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByTag" />
                    </h:commandButton>
                    <h:commandButton id="searchByTagOR"  action="#{searchQuestionBean.doit}"
                                     onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_tag_or}">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByTag" />
                    </h:commandButton>
                </div>
            </div>

            <div class=" row">
                <div class="col-xs-12 col-sm-12 col-md-12">
                    <h3><h:outputText value="#{authorMessages.text_search}" /></h3>
                </div>
                <div class="col-xs-12 col-sm-12 col-md-12"><h:outputText value="#{authorMessages.search_question_by_text_tip}" /></div>
                <div class="col-xs-12 col-md-6">
                    <h:inputText id="textToSearch" label="#{authorMessages.text_search}" value="#{searchQuestionBean.textToSearch}"
                                 styleClass="form-control ">
                    </h:inputText>
                </div>
                <div class="col-xs-12 col-md-6">
                    <h:commandButton id="searchByTextAND"  action="#{searchQuestionBean.doit}"
                                     onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_text_and}">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByText" />
                    </h:commandButton>
                    <h:commandButton id="searchByTextOR"  action="#{searchQuestionBean.doit}"
                                     onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_text_or}">
                        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByText" />
                    </h:commandButton>

                </div>
            </div>
            <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

            <h:panelGroup id="text_searched" rendered="#{searchQuestionBean.lastSearchType == 'text'}">
                <h4><h:outputLabel value="#{authorMessages.searched_text}" />&nbsp;<small><h:outputLabel value="#{searchQuestionBean.textToSearch}" /></small></h4>
                <h3><h:outputLabel value="#{authorMessages.no_results}" rendered="#{searchQuestionBean.resultsSize == 0 }"/></h3>
            </h:panelGroup>

            <h:panelGroup id="tags_searched" rendered="#{searchQuestionBean.lastSearchType == 'tag'}">
                <h4><h:outputLabel value="#{authorMessages.searched_tags}"/> &nbsp;<small><h:outputLabel value="#{searchQuestionBean.tagToSearchLabel}" /></small></h4>
                <h3><h:outputLabel value="#{authorMessages.no_results}" rendered="#{searchQuestionBean.resultsSize == 0 }"/></h3>
            </h:panelGroup>


            <h:panelGroup layout="block" rendered="#{searchQuestionBean.resultsSize > 0 }">
                <%@ include file="/jsf/author/searchResults.jsp" %>
            </h:panelGroup>

            </br>

            <h:commandButton id="import"  action="#{searchQuestionBean.doit}"
                             onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_add_questions}">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ImportQuestionsToAuthoringFromSearch" />
            </h:commandButton>

            <h:commandButton style="act" value="#{commonMessages.cancel_action}" action="#{searchQuestionBean.cancelSearchQuestion}" onclick="SPNR.disableControlsAndSpin(this, null);">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CancelSearchQuestion" />
            </h:commandButton>

        </h:form>
    </div>

    </body>
    </html>
</f:view>
