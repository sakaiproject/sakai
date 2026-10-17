<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:directive.page language="java"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <!-- $Id$

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

 -->
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{authorMessages.search_question}"/>
        </title>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/samigo-app/js/authoring.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:directive.include file="/js/samigotree.js"/>
          <jsp:text><![CDATA[

            function textCounter(field, maxlimit) {
                if (field.value.length > maxlimit) // if too long...trim it!
                    field.value = field.value.substring(0, maxlimit);
            }

            jQuery(window).load(function(){
                //If we need to do something on load
            });
        ]]></jsp:text>
        </script>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/webjars/jquery/1.12.4/jquery.min.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <jsp:element name="script">
          <jsp:attribute name="src" trim="false">/library/js/spinner.js</jsp:attribute>
          <jsp:body>
          </jsp:body>
        </jsp:element>
        <script>
          <jsp:text>includeWebjarLibrary('select2');</jsp:text>
        </script>
        <script>
          <jsp:text>
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
                    if ("</jsp:text>
          <h:outputText value="#{searchQuestionBean.lastSearchType}"/>
          <jsp:text>"  == "text"){
                        document.getElementById('editform:textToSearch').focus();
                    }else{
                        $(".tag_selector").focus();
                     }
                }

            }
            window.onload = initPage;
        </jsp:text>
        </script>
        <script type="module">
          <jsp:text>
            import { tagLabel } from "/samigo-app/js/tag-display.js";


            $(document).ready(function() {
                $('#editform\\:textToSearch').keypress(function(event){
                    if(event.keyCode == 13){
                        $('#editform\\:searchByTextAND').click();
                    }
                });
                $(".tag_selector").select2({
                    width: '100%',
                    language: "</jsp:text>
          <h:outputText value="#{itemauthor.language}"/>
          <jsp:text>",
                    closeOnSelect: false,
                    ajax: {
                        url: "/direct/tagservice/getTagsPaginatedByPrefixInLabel.json",
                        dataType: 'json',
                        delay: 500,
                        data: function (params) {
                            return {
                                siteId: "</jsp:text>
          <h:outputText value="#{author.currentSiteId}"/>
          <jsp:text><![CDATA[",
                            prefix: params.term || "", // search term
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
                var $tag_formatted = $(tagLabel(repo.text, repo.collection));
                return $tag_formatted;
            }

            function formatRepoSelection (repo) {
                if (typeof repo.collection != 'undefined') {
                    var $tag_formatted = $(tagLabel(repo.text, repo.collection));
                }else{
                    var $tag_formatted = $(tagLabel(repo.text));
                }
                return $tag_formatted;
            }


        ]]></jsp:text>
        </script>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false">document.forms[0].reset(); disableIt(); resetSelectMenus(); ;<jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <div class="portletBody container-fluid">
            <h:form id="editform">
              <jsp:directive.include file="/jsf/author/allHeadings.jsp"/>
              <h2>
                <h:outputText value="#{authorMessages.search_question}"/>
              </h2>
              <jsp:text><![CDATA[

            <div class="row" ]]></jsp:text>
              <h:outputText value="#{searchQuestionBean.tagDisabled}"/>
              <jsp:text><![CDATA[>
                ]]></jsp:text>
              <div class="col-xs-12 col-sm-12 col-md-12">
                <h3>
                  <h:outputText value="#{authorMessages.tag_search}"/>
                </h3>
              </div>
              <br/>
              <div class="col-xs-12 col-md-6">
                <jsp:element name="select">
                  <jsp:attribute name="class" trim="false">tag_selector</jsp:attribute>
                  <jsp:attribute name="name" trim="false">tag_selector[]</jsp:attribute>
                  <jsp:attribute name="id" trim="false">tag_selector</jsp:attribute>
                  <jsp:attribute name="multiple" trim="false">multiple</jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </div>
              <jsp:text><![CDATA[
                </br>
                ]]></jsp:text>
              <div class="col-xs-12 col-md-6">
                <h:commandButton id="searchByTagAND" action="#{searchQuestionBean.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_tag_and}">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByTag"/>
                </h:commandButton>
                <h:commandButton id="searchByTagOR" action="#{searchQuestionBean.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_tag_or}">
                  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByTag"/>
                </h:commandButton>
              </div>
              <jsp:text><![CDATA[
            </div>

            ]]></jsp:text>
              <div class=" row">
                <div class="col-xs-12 col-sm-12 col-md-12">
                  <h3>
                    <h:outputText value="#{authorMessages.text_search}"/>
                  </h3>
                </div>
                <div class="col-xs-12 col-sm-12 col-md-12">
                  <h:outputText value="#{authorMessages.search_question_by_text_tip}"/>
                </div>
                <div class="col-xs-12 col-md-6">
                  <h:inputText id="textToSearch" label="#{authorMessages.text_search}" value="#{searchQuestionBean.textToSearch}" styleClass="form-control ">
                  </h:inputText>
                </div>
                <div class="col-xs-12 col-md-6">
                  <h:commandButton id="searchByTextAND" action="#{searchQuestionBean.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_text_and}">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByText"/>
                  </h:commandButton>
                  <h:commandButton id="searchByTextOR" action="#{searchQuestionBean.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_search_questions_by_text_or}">
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SearchQuestionByText"/>
                  </h:commandButton>
                </div>
              </div>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <h:panelGroup id="text_searched" rendered="#{searchQuestionBean.lastSearchType == 'text'}">
                <h4>
                  <h:outputLabel value="#{authorMessages.searched_text}"/>
                  <jsp:text><![CDATA[&nbsp;]]></jsp:text>
                  <small>
                    <h:outputLabel value="#{searchQuestionBean.textToSearch}"/>
                  </small>
                </h4>
                <h3>
                  <h:outputLabel value="#{authorMessages.no_results}" rendered="#{searchQuestionBean.resultsSize == 0 }"/>
                </h3>
              </h:panelGroup>
              <h:panelGroup id="tags_searched" rendered="#{searchQuestionBean.lastSearchType == 'tag'}">
                <h4>
                  <h:outputLabel value="#{authorMessages.searched_tags}"/>
                  <jsp:text><![CDATA[ &nbsp;]]></jsp:text>
                  <small>
                    <h:outputLabel value="#{searchQuestionBean.tagToSearchLabel}"/>
                  </small>
                </h4>
                <h3>
                  <h:outputLabel value="#{authorMessages.no_results}" rendered="#{searchQuestionBean.resultsSize == 0 }"/>
                </h3>
              </h:panelGroup>
              <h:panelGroup layout="block" rendered="#{searchQuestionBean.resultsSize &gt; 0 }">
                <jsp:directive.include file="/jsf/author/searchResults.jsp"/>
              </h:panelGroup>
              <jsp:text><![CDATA[

            </br>

            ]]></jsp:text>
              <h:commandButton id="import" action="#{searchQuestionBean.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{authorMessages.button_add_questions}">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ImportQuestionsToAuthoringFromSearch"/>
              </h:commandButton>
              <h:commandButton style="act" value="#{commonMessages.cancel_action}" action="#{searchQuestionBean.cancelSearchQuestion}" onclick="SPNR.disableControlsAndSpin(this, null);">
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.CancelSearchQuestion"/>
              </h:commandButton>
            </h:form>
          </div>
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
