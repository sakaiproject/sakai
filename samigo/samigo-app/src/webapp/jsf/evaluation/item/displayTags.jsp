<samigo:script path="/../library/webjars/select2/4.0.3/dist/js/select2.full.min.js"/>
<samigo:stylesheet path="/../library/webjars/select2/4.0.3/dist/css/select2.css"/>

<script type="text/javascript">

    $(document).ready(function () {
        $(".tag_selector_<h:outputText value="#{question.itemId}"/>").select2({
            width: '50%',
            disabled: true,
            escapeMarkup: function (markup) {
                return markup;
            }, // let our custom formatter work
            templateResult: formatRepo_<h:outputText value="#{question.itemId}"/>,
            templateSelection: formatRepoSelection_<h:outputText value="#{question.itemId}"/>
        })

        //Load the item actual tags
        var arr_<h:outputText value="#{question.itemId}"/> =<h:outputText value="#{question.tagListToJsonString}" escape="false"/>;
        if (arr_<h:outputText value="#{question.itemId}"/>.length < 1) {
            $(".tag_div_<h:outputText value="#{question.itemId}"/>").hide();
        }
        for (var i = 0; i < arr_<h:outputText value="#{question.itemId}"/>.length; i++) {
            var obj = arr_<h:outputText value="#{question.itemId}"/>[i];
            var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
            $(".tag_selector_<h:outputText value="#{question.itemId}"/>").append(newOption);
            $('.tag_selector_<h:outputText value="#{question.itemId}"/> option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
        }
        $(".tag_selector_<h:outputText value="#{question.itemId}"/>").trigger('change');
        $(".temporalhide_<h:outputText value="#{question.itemId}"/>").show();
    });

    function formatRepo_<h:outputText value="#{question.itemId}"/>(repo) {
        var $tag_formatted = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
        return $tag_formatted;
    }

    function formatRepoSelection_<h:outputText value="#{question.itemId}"/>(repo) {
        if (typeof repo.collection != 'undefined') {
            var $tag_formatted_<h:outputText value="#{question.itemId}"/> = $("<span>" + repo.text + " <span class='collection'>(" + repo.collection + ")</span></span>");
        } else {
            var collection_<h:outputText value="#{question.itemId}"/> = $('.tag_selector_<h:outputText value="#{question.itemId}"/> option[value=' + repo.id + ']').attr("title");
            if (typeof collection_<h:outputText value="#{question.itemId}"/> != 'undefined') {
                var $tag_formatted_<h:outputText value="#{question.itemId}"/> = $("<span>" + repo.text + " <span class='collection'>(" + collection_<h:outputText value="#{question.itemId}"/> + ")</span></span>");
            } else {
                var $tag_formatted_<h:outputText value="#{question.itemId}"/> = $("<span>" + repo.text + "</span>");
            }
        }
        return $tag_formatted_<h:outputText value="#{question.itemId}"/>;
    }

</script>
<div class="temporalhide_<h:outputText value="#{question.itemId}"/>" style="display:none">
    <div class="longtext tag_div_<h:outputText value="#{question.itemId}"/>"
         style="<h:outputText value="#{questionScores.showTagsInEvaluationStyle}"/>">
        <h:outputLabel value="#{authorMessages.tag_title}"/><br/>

        <h:panelGroup>
            <select class="tag_selector_<h:outputText value="#{question.itemId}"/> "
                    name="tag_selector_<h:outputText value="#{question.itemId}"/>[]"
                    id="tag_selector_<h:outputText value="#{question.itemId}"/>" multiple="multiple">
            </select>
        </h:panelGroup>

    </div>
</div>
