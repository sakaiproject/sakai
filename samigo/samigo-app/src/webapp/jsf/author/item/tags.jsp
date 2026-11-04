<script type="module" src="/samigo-app/js/tag-display.js"></script>

<f:subview id="delete" rendered="#{itemauthor.deleteTagsAllowed == true}">
    <div class="longtext" id="tags_div" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">
        <h:outputLabel value="#{authorMessages.tag_question}"/><br/>
        <div class="samigo-tag-picker">
            <input type="hidden" class="samigo-tag-site" value="<h:outputText value="#{author.currentSiteId}"/>"/>
            <input type="hidden" class="samigo-tag-data" value="<h:outputText value="#{itemauthor.tagsListToJson}"/>"/>
            <select hidden name="tag_selector[]" multiple="multiple"></select>
            <sakai-tag-selector label="<h:outputText value="#{authorMessages.tag_question}"/>"></sakai-tag-selector>
        </div>
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
    <div class="longtext" id="tags_div" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">
        <h:outputLabel value="#{authorMessages.tag_title}"/><br/>
        <i><h:outputLabel value="#{authorMessages.tag_previous}"/></i><br/>
        <div class="samigo-readonly-tags">
            <input type="hidden" class="samigo-tag-data" value="<h:outputText value="#{itemauthor.tagsListToJson}"/>"/>
        </div>
        <i><h:outputLabel value="#{authorMessages.tag_question_new}"/></i><br/>
        <div class="samigo-tag-picker">
            <input type="hidden" class="samigo-tag-site" value="<h:outputText value="#{author.currentSiteId}"/>"/>
            <input type="hidden" class="samigo-tag-data" value="<h:outputText value="#{itemauthor.tagsTempListToJson}"/>"/>
            <select hidden name="tag_selector[]" multiple="multiple"></select>
            <sakai-tag-selector label="<h:outputText value="#{authorMessages.tag_question_new}"/>"></sakai-tag-selector>
        </div>
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
