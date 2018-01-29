<samigo:script path="/../library/webjars/select2/4.0.3/dist/js/select2.full.min.js"/>
<samigo:stylesheet path="/../library/webjars/select2/4.0.3/dist/css/select2.css"/>
<sakai:script path="/../library/webjars/select2/4.0.3/dist/js/i18n/#{itemauthor.language}.js"/>
<f:subview id="delete" rendered="#{itemauthor.deleteTagsAllowed == true}">
    <script type="text/javascript">


        $(document).ready(function () {
            $(".tag_selector").select2({
                width: '50%',
                language: "<h:outputText value="#{itemauthor.language}"/>",
                closeOnSelect: false,
                ajax: {
                    url: "/direct/tagservice/getTagsPaginatedByPrefixInLabel.json",
                    dataType: 'json',
                    delay: 500,
                    data: function (params) {
                        return {
                            prefix: params.term, // search term
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
            var arr =<h:outputText value="#{itemauthor.tagsListToJson}" escape="false"/>;
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
            var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
            return $tag_formatted;
        }

        function formatRepoSelection(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
            } else {
                var collection = $('.tag_selector option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + collection + ")</span></span>");
                } else {
                    var $tag_formatted = $("<span>" + repo.text + "</span>");
                }
            }
            return $tag_formatted;
        }

    </script>

    <div class="longtext" id="tags_div" name="tags_div" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">
        <h:outputLabel value="#{authorMessages.tag_question}"/><br/>

        <h:panelGroup id="tags_panel_previous">
            <select hidden class="tag_selector_previous" name="tag_selector_previous[]" id="tag_selector_previous"
                    multiple="multiple">
            </select>
        </h:panelGroup>

        <h:panelGroup id="tags_panel">


            <select class="tag_selector" name="tag_selector[]" id="tag_selector" multiple="multiple">
            </select>
        </h:panelGroup>
        <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
        </br><input name="multiTagsSingleCheck" id="multiTagsSingleCheck" type="checkbox" value="true"
        </f:verbatim>
            <h:outputText value="checked"
                          rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true) && (itemauthor.multiTagsSingleQuestion == true)}"/>
        <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
        >
        </f:verbatim>
        <h:outputText value="#{authorMessages.tag_multitag_singlequestion_all} "
                      rendered="#{(itemauthor.multiTagsSingleQuestion == true)}"/>
        <h:outputText value="#{authorMessages.tag_multitag_singlequestion}"
                      rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}"/><br/>

    </div>

</f:subview>
<f:subview id="nodelete" rendered="#{itemauthor.deleteTagsAllowed == false}">

    <script type="text/javascript">


        $(document).ready(function () {
            $(".tag_selector").select2({
                width: '50%',
                language: "<h:outputText value="#{itemauthor.language}"/>",
                closeOnSelect: false,
                ajax: {
                    url: "/direct/tagservice/getTagsPaginatedByPrefixInLabel.json",
                    dataType: 'json',
                    delay: 500,
                    data: function (params) {
                        return {
                            prefix: params.term, // search term
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
                language: "<h:outputText value="#{itemauthor.language}"/>",
                disabled: true,
                escapeMarkup: function (markup) {
                    return markup;
                }, // let our custom formatter work
                templateResult: formatRepo,
                templateSelection: formatRepoSelection_previous
            })


            //Load the item actual tags
            var arr =<h:outputText value="#{itemauthor.tagsListToJson}" escape="false"/>;
            for (var i = 0; i < arr.length; i++) {
                var obj = arr[i];
                var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
                $(".tag_selector_previous").append(newOption);
                $('.tag_selector_previous option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
            }
            $(".tag_selector_previous").trigger('change');

            //Load the unsaved tags
            var arr =<h:outputText value="#{itemauthor.tagsTempListToJson}" escape="false"/>;
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
            var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
            return $tag_formatted;
        }

        function formatRepoSelection(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
            } else {
                var collection = $('.tag_selector option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + collection + ")</span></span>");
                } else {
                    var $tag_formatted = $("<span>" + repo.text + "</span>");
                }
            }
            return $tag_formatted;
        }

        function formatRepoSelection_previous(repo) {
            if (typeof repo.collection != 'undefined') {
                var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
            } else {
                var collection = $('.tag_selector_previous option[value=' + repo.id + ']').attr("title");
                if (typeof collection != 'undefined') {
                    var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + collection + ")</span></span>");
                } else {
                    var $tag_formatted = $("<span>" + repo.text + "</span>");
                }
            }
            return $tag_formatted;
        }

    </script>

    <div class="longtext" id="tags_div" name="tags_div" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">

        <h:outputLabel value="#{authorMessages.tag_title}"/><br/>
        <i><h:outputLabel value="#{authorMessages.tag_previous}"/></i><br/>


        <h:panelGroup id="tags_panel_previous">
            <select class="tag_selector_previous" name="tag_selector_previous[]" id="tag_selector_previous"
                    multiple="multiple">
            </select>
        </h:panelGroup><br/><br/>

        <i><h:outputLabel value="#{authorMessages.tag_question_new}"/></i><br/>
        <h:panelGroup id="tags_panel">
            <select class="tag_selector" name="tag_selector[]" id="tag_selector" multiple="multiple">
            </select>
        </h:panelGroup>

        <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
        </br><input name="multiTagsSingleCheck" id="multiTagsSingleCheck" type="checkbox" value="true"
        </f:verbatim>
            <h:outputText value="checked"
                          rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true) && (itemauthor.multiTagsSingleQuestion == true)}"/>
        <f:verbatim rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}">
        >
        </f:verbatim>
        <h:outputText value="#{authorMessages.tag_multitag_singlequestion_all} "
                      rendered="#{(itemauthor.multiTagsSingleQuestion == true)}"/>
        <h:outputText value="#{authorMessages.tag_multitag_singlequestion}"
                      rendered="#{(itemauthor.multiTagsSingleQuestionCheck == true)}"/><br/>


    </div>
</f:subview>
