<f:verbatim rendered="#{questionpool.showTagFilter}">
    <script src="/samigo-app/js/questionpoolTagFilter.js"></script>
</f:verbatim>
<h:panelGroup rendered="#{questionpool.showTagFilter}" layout="block"
    styleClass="b5 mb-2 d-flex gap-1 flex-column align-items-stretch flex-sm-row align-items-sm-start">
  <sakai-tag-selector
      id="tag-search"
      class="b5 flex-grow-1"
      selected-temp="<h:outputText value='#{questionpool.filterTags.tagIdsCsv}'/>"
      collection-id="<h:outputText value='#{questionpool.ownerId}'/>"
      site-id="<h:outputText value='#{author.currentSiteId}'/>"
      add-new="false"
  ></sakai-tag-selector>
  <h:inputHidden id="selectedTags" value="#{questionpool.filterTags.tagIdsCsv}"/>
  <%-- TODO Master merge: me-0 can probably removed entirely as it's just overriding an margin-right, else just remove b5 class --%>
  <h:commandButton id="searchByTags" styleClass="b5 me-0" action="questionpool.filterByTags" value="#{questionPoolMessages.tags_search}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.FilterByTagsListener" />
  </h:commandButton>
  <h:commandButton id="clearTagFilter" styleClass="b5 me-0" value="#{questionPoolMessages.tags_search_clear}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.ClearSelectedTagsListener" />
  </h:commandButton>
</h:panelGroup>
