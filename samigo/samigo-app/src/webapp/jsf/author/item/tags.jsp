<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<script type="module" src="/samigo-app/js/tag-display.js"></script>

<f:subview id="delete" rendered="#{itemauthor.deleteTagsAllowed == true}">
    <div class="longtext" id="tags_div" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">
        <h:outputLabel value="#{authorMessages.tag_question}"/><br/>
        <div class="samigo-tag-picker">
            <input type="hidden" class="samigo-tag-site" value="<h:outputText value="#{author.currentSiteId}"/>"/>
            <textarea hidden class="samigo-tag-data"><h:outputText value="#{itemauthor.tagsListToJson}" escape="true"/></textarea>
            <f:verbatim><select hidden name="tag_selector[]" multiple="multiple"></f:verbatim>
            <t:dataList value="#{itemauthor.tagSelectorTags}" var="selectedTag" layout="simple">
                <f:verbatim><option selected="selected" value="</f:verbatim><h:outputText value="#{selectedTag.tagId()}"/><f:verbatim>"></f:verbatim><h:outputText value="#{selectedTag.tagLabel()}"/><f:verbatim></option></f:verbatim>
            </t:dataList>
            <f:verbatim></select></f:verbatim>
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
            <textarea hidden class="samigo-tag-data"><h:outputText value="#{itemauthor.tagsListToJson}" escape="true"/></textarea>
        </div>
        <i><h:outputLabel value="#{authorMessages.tag_question_new}"/></i><br/>
        <div class="samigo-tag-picker">
            <input type="hidden" class="samigo-tag-site" value="<h:outputText value="#{author.currentSiteId}"/>"/>
            <textarea hidden class="samigo-tag-data"><h:outputText value="#{itemauthor.tagsTempListToJson}" escape="true"/></textarea>
            <f:verbatim><select hidden name="tag_selector[]" multiple="multiple"></f:verbatim>
            <t:dataList value="#{itemauthor.tagSelectorTags}" var="selectedTag" layout="simple">
                <f:verbatim><option selected="selected" value="</f:verbatim><h:outputText value="#{selectedTag.tagId()}"/><f:verbatim>"></f:verbatim><h:outputText value="#{selectedTag.tagLabel()}"/><f:verbatim></option></f:verbatim>
            </t:dataList>
            <f:verbatim></select></f:verbatim>
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
