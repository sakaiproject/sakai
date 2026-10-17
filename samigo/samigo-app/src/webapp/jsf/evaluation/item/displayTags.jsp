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

<input type="hidden" id="tag-data_]]></jsp:text>
  <h:outputText value="#{question.itemId}"/>
  <jsp:text>" value="</jsp:text>
  <h:outputText value="#{question.tagListToJsonString}"/>
  <jsp:text><![CDATA["/>
]]></jsp:text>
  <script type="module">
    <jsp:text>
    import { tagLabel } from "/samigo-app/js/tag-display.js";

    $(document).ready(function () {
        $(".tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>").select2({
            width: '50%',
            disabled: true,
            escapeMarkup: function (markup) {
                return markup;
            }, // let our custom formatter work
            templateResult: formatRepo_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>,
            templateSelection: formatRepoSelection_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>
        })

        //Load the item actual tags
        var arr_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> =JSON.parse(document.getElementById('tag-data_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>').value);
        if (arr_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text><![CDATA[.length < 1) {
            $(".tag_div_]]></jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text><![CDATA[").hide();
        }
        for (var i = 0; i < arr_]]></jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>.length; i++) {
            var obj = arr_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>[i];
            var newOption = new Option(obj["tagLabel"], obj["tagId"], true, true);
            $(".tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>").append(newOption);
            $('.tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> option[value=' + obj["tagId"] + ']').attr("title", obj["tagCollectionName"]);
        }
        $(".tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>").trigger('change');
        $(".temporalhide_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>").show();
    });

    function formatRepo_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>(repo) {
        var $tag_formatted = $(tagLabel(repo.text, repo.collection));
        return $tag_formatted;
    }

    function formatRepoSelection_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>(repo) {
        if (typeof repo.collection != 'undefined') {
            var $tag_formatted_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> = $(tagLabel(repo.text, repo.collection));
        } else {
            var collection_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> = $('.tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> option[value=' + repo.id + ']').attr("title");
            if (typeof collection_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> != 'undefined') {
                var $tag_formatted_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> = $(tagLabel(repo.text, collection_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>));
            } else {
                var $tag_formatted_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> = $(tagLabel(repo.text));
            }
        }
        return $tag_formatted_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>;
    }

</jsp:text>
  </script>
  <jsp:text><![CDATA[
<div class="temporalhide_]]></jsp:text>
  <h:outputText value="#{question.itemId}"/>
  <jsp:text><![CDATA[" style="display:none">
    <div class="longtext tag_div_]]></jsp:text>
  <h:outputText value="#{question.itemId}"/>
  <jsp:text>"
         style="</jsp:text>
  <h:outputText value="#{questionScores.showTagsInEvaluationStyle}"/>
  <jsp:text><![CDATA[">
        ]]></jsp:text>
  <h:outputLabel value="#{authorMessages.tag_title}"/>
  <br/>
  <h:panelGroup>
    <jsp:text><![CDATA[
            <select class="tag_selector_]]></jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text> "
                    name="tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text>[]"
                    id="tag_selector_</jsp:text>
    <h:outputText value="#{question.itemId}"/>
    <jsp:text><![CDATA[" multiple="multiple">
            </select>
        ]]></jsp:text>
  </h:panelGroup>
  <jsp:text><![CDATA[

    </div>
</div>
]]></jsp:text>
</jsp:root>
