<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <f:verbatim rendered="#{questionpool.showTagFilter}">
    <jsp:element name="script">
      <jsp:attribute name="src" trim="false">/samigo-app/js/questionpoolTagFilter.js</jsp:attribute>
      <jsp:body>
      </jsp:body>
    </jsp:element>
  </f:verbatim>
  <h:panelGroup rendered="#{questionpool.showTagFilter}" layout="block" styleClass="b5 mb-2 d-flex gap-1 flex-column align-items-stretch flex-sm-row align-items-sm-start">
    <jsp:text><![CDATA[
  <sakai-tag-selector
      tool="samigo"
      id="tag-search"
      input-id="questionpool:selectedTags"
      class="b5 flex-grow-1"
      selected-ids="]]></jsp:text>
    <h:outputText value="#{questionpool.filterTags.tagIdsCsv}"/>
    <jsp:text>"
      collection-id="</jsp:text>
    <h:outputText value="#{questionpool.agentId}"/>
    <jsp:text>"
      site-id="</jsp:text>
    <h:outputText value="#{author.currentSiteId}"/>
    <jsp:text><![CDATA["
      add-new="false"
  ></sakai-tag-selector>
  ]]></jsp:text>
    <h:inputHidden id="selectedTags" value="#{questionpool.filterTags.tagIdsCsv}"/>
    <!-- TODO Master merge: me-0 can probably removed entirely as it's just overriding an margin-right, else just remove b5 class  -->
    <h:commandButton id="searchByTags" styleClass="b5 me-0" action="questionpool.filterByTags" value="#{questionPoolMessages.tags_search}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.FilterByTagsListener"/>
    </h:commandButton>
    <h:commandButton id="clearTagFilter" styleClass="b5 me-0" value="#{questionPoolMessages.tags_search_clear}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.ClearSelectedTagsListener"/>
    </h:commandButton>
  </h:panelGroup>
</jsp:root>
