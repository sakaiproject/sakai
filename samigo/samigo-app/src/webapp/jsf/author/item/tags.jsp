<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <script>
    <jsp:text>includeWebjarLibrary('select2');</jsp:text>
  </script>
  <jsp:text><![CDATA[
<script src="/library/webjars/select2/4.0.13/js/i18n/]]></jsp:text>
  <h:outputText value="#{itemauthor.language}" rendered="#{not empty itemauthor.language}"/>
  <h:outputText value="en" rendered="#{empty itemauthor.language}"/>
  <jsp:text><![CDATA[.js"></script>
]]></jsp:text>
  <f:subview id="delete" rendered="#{itemauthor.deleteTagsAllowed == true}">
    <jsp:text><![CDATA[
    <input type="hidden" id="item-tag-data" value="]]></jsp:text>
    <h:outputText value="#{itemauthor.tagsListToJson}"/>
    <jsp:text><![CDATA["/>
    ]]></jsp:text>
    <script type="module">
      <jsp:text>
        import { tagLabel } from "/samigo-app/js/tag-display.js";


        $(document).ready(function () {
            $(".tag_selector").select2({
                width: '50%',
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
                            page: params.page
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
                                    collection: item.collectionName
                                }
                            }),
                            pagination: {more: (params.page * 30) < data.data.total}
                        };
                    },
                    cache: true
                },
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                minimumInputLength: 1,
                templateResult: formatRepo,
                templateSelection: formatRepoSelection
            })


            //Load the item actual tags
            var arr =JSON.parse(document.getElementById('item-tag-data').value);
            for (var i = 0; i < arr.length; i++) {
                var obj = arr[i];
                var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
                $(".tag_selector").append(newOption);
                $('.tag_selector option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
            }
            $(".tag_selector").trigger('change');

            $('.tag_selector').on("select2:unselect", function (e) {
                $('.tag_selector option').each(function () {
                    if ($(this).val() == e.params.data.id) {
                        $(this).remove();
                    }
                });
            }).trigger('change');
        });

        function formatRepo(repo) {
            var $tag_formatted = $(tagLabel(repo.text, repo.collection));
            return $tag_formatted;
        }

        function formatRepoSelection(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $(tagLabel(repo.text, repo.collection));
            } else {
                var collection = $('.tag_selector option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $(tagLabel(repo.text, collection));
                } else {
                    var $tag_formatted = $(tagLabel(repo.text));
                }
            }
            return $tag_formatted;
        }

    ]]></jsp:text>
    </script>
    <jsp:text><![CDATA[

    <div class="longtext" id="tags_div" name="tags_div" style="]]></jsp:text>
    <h:outputText value="#{itemauthor.showTagsStyle}"/>
    <jsp:text><![CDATA[">
        ]]></jsp:text>
    <h:outputLabel value="#{authorMessages.tag_question}"/>
    <br/>
    <h:panelGroup id="tags_panel_previous">
      <jsp:element name="select">
        <jsp:attribute name="hidden" trim="false"></jsp:attribute>
        <jsp:attribute name="class" trim="false">tag_selector_previous</jsp:attribute>
        <jsp:attribute name="name" trim="false">tag_selector_previous[]</jsp:attribute>
        <jsp:attribute name="id" trim="false">tag_selector_previous</jsp:attribute>
        <jsp:attribute name="multiple" trim="false">multiple</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:panelGroup>
    <h:panelGroup id="tags_panel">
      <jsp:element name="select">
        <jsp:attribute name="class" trim="false">tag_selector</jsp:attribute>
        <jsp:attribute name="name" trim="false">tag_selector[]</jsp:attribute>
        <jsp:attribute name="id" trim="false">tag_selector</jsp:attribute>
        <jsp:attribute name="multiple" trim="false">multiple</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:panelGroup>
    <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
      <jsp:text><![CDATA[
        </br><input name="multiTagsSingleCheck" id="multiTagsSingleCheck" type="checkbox" value="true"
        ]]></jsp:text>
    </f:verbatim>
    <h:outputText value="checked" rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true) &amp;&amp; (itemauthor.multiTagsSingleQuestion == true)}"/>
    <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
      <jsp:text><![CDATA[
        >
        ]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{authorMessages.tag_multitag_singlequestion_all} " rendered="#{(itemauthor.multiTagsSingleQuestion == true)}"/>
    <h:outputText value="#{authorMessages.tag_multitag_singlequestion}" rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}"/>
    <br/>
    <jsp:text><![CDATA[

    </div>

]]></jsp:text>
  </f:subview>
  <f:subview id="nodelete" rendered="#{itemauthor.deleteTagsAllowed == false}">
    <jsp:text><![CDATA[

    <input type="hidden" id="item-tag-data" value="]]></jsp:text>
    <h:outputText value="#{itemauthor.tagsListToJson}"/>
    <jsp:text><![CDATA["/>
    ]]></jsp:text>
    <script type="module">
      <jsp:text>
        import { tagLabel } from "/samigo-app/js/tag-display.js";


        $(document).ready(function () {
            $(".tag_selector").select2({
                width: '50%',
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
                            page: params.page
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
                                    collection: item.collectionName
                                }
                            }),
                            pagination: {more: (params.page * 30) < data.data.total}
                        };
                    },
                    cache: true
                },
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                minimumInputLength: 1,
                templateResult: formatRepo,
                templateSelection: formatRepoSelection
            })

            $(".tag_selector_previous").select2({
                width: '50%',
                language: "]]></jsp:text>
      <h:outputText value="#{itemauthor.language}"/>
      <jsp:text><![CDATA[",
                disabled: true,
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                templateResult: formatRepo,
                templateSelection: formatRepoSelection_previous
            })


            //Load the item actual tags
            var arr =JSON.parse(document.getElementById('item-tag-data').value);
            for (var i = 0; i < arr.length; i++) {
                var obj = arr[i];
                var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
                $(".tag_selector_previous").append(newOption);
                $('.tag_selector_previous option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
            }
            $(".tag_selector_previous").trigger('change');

            //Load the unsaved tags
            var arr =]]></jsp:text>
      <h:outputText value="#{itemauthor.tagsTempListToJson}" escape="false"/>
      <jsp:text><![CDATA[;
            for (var i = 0; i < arr.length; i++) {
                var obj = arr[i];
                var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
                $(".tag_selector").append(newOption);
                $('.tag_selector option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
            }
            $(".tag_selector").trigger('change');

            $('.tag_selector').on("select2:unselect", function (e) {
                $('.tag_selector option').each(function () {
                    if ($(this).val() == e.params.data.id) {
                        $(this).remove();
                    }
                });
            }).trigger('change');
        });

        function formatRepo(repo) {
            var $tag_formatted = $(tagLabel(repo.text, repo.collection));
            return $tag_formatted;
        }

        function formatRepoSelection(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $(tagLabel(repo.text, repo.collection));
            } else {
                var collection = $('.tag_selector option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $(tagLabel(repo.text, collection));
                } else {
                    var $tag_formatted = $(tagLabel(repo.text));
                }
            }
            return $tag_formatted;
        }

        function formatRepoSelection_previous(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $(tagLabel(repo.text, repo.collection));
            } else {
                var collection = $('.tag_selector_previous option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $(tagLabel(repo.text, collection));
                } else {
                    var $tag_formatted = $(tagLabel(repo.text));
                }
            }
            return $tag_formatted;
        }

    ]]></jsp:text>
    </script>
    <jsp:text><![CDATA[

    <div class="longtext" id="tags_div" name="tags_div" style="]]></jsp:text>
    <h:outputText value="#{itemauthor.showTagsStyle}"/>
    <jsp:text><![CDATA[">

        ]]></jsp:text>
    <h:outputLabel value="#{authorMessages.tag_title}"/>
    <br/>
    <i>
      <h:outputLabel value="#{authorMessages.tag_previous}"/>
    </i>
    <br/>
    <h:panelGroup id="tags_panel_previous">
      <jsp:element name="select">
        <jsp:attribute name="class" trim="false">tag_selector_previous</jsp:attribute>
        <jsp:attribute name="name" trim="false">tag_selector_previous[]</jsp:attribute>
        <jsp:attribute name="id" trim="false">tag_selector_previous</jsp:attribute>
        <jsp:attribute name="multiple" trim="false">multiple</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:panelGroup>
    <br/>
    <br/>
    <i>
      <h:outputLabel value="#{authorMessages.tag_question_new}"/>
    </i>
    <br/>
    <h:panelGroup id="tags_panel">
      <jsp:element name="select">
        <jsp:attribute name="class" trim="false">tag_selector</jsp:attribute>
        <jsp:attribute name="name" trim="false">tag_selector[]</jsp:attribute>
        <jsp:attribute name="id" trim="false">tag_selector</jsp:attribute>
        <jsp:attribute name="multiple" trim="false">multiple</jsp:attribute>
        <jsp:body>
        </jsp:body>
      </jsp:element>
    </h:panelGroup>
    <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
      <jsp:text><![CDATA[
        </br><input name="multiTagsSingleCheck" id="multiTagsSingleCheck" type="checkbox" value="true"
        ]]></jsp:text>
    </f:verbatim>
    <h:outputText value="checked" rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true) &amp;&amp; (itemauthor.multiTagsSingleQuestion == true)}"/>
    <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
      <jsp:text><![CDATA[
        >
        ]]></jsp:text>
    </f:verbatim>
    <h:outputText value="#{authorMessages.tag_multitag_singlequestion_all} " rendered="#{(itemauthor.multiTagsSingleQuestion == true)}"/>
    <h:outputText value="#{authorMessages.tag_multitag_singlequestion}" rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}"/>
    <br/>
    <jsp:text><![CDATA[


    </div>
]]></jsp:text>
  </f:subview>
</jsp:root>
