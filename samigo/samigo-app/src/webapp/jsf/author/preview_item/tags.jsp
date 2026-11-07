<script type="module" src="/samigo-app/js/tag-display.js"></script>
<div class="longtext" style="<h:outputText value="#{itemauthor.showTagsStyle}"/>">
    <div class="samigo-readonly-tags" hidden>
        <h:outputLabel value="#{authorMessages.tag_title}"/>
        <textarea hidden class="samigo-tag-data"><h:outputText value="#{question.tagsListToJson}" escape="true"/></textarea>
    </div>
</div>
