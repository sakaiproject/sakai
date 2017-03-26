<br/><h:outputLabel value="#{questionPoolMessages.t_tags}" /></br>

<t:dataList value="#{itemValue.itemTagSet.toArray()}" var="tag" layout="unorderedList">
    <f:verbatim><span></f:verbatim>
    <h:outputText value="#{tag.tagLabel}"/>
    <f:verbatim><span class="collection"></f:verbatim>
    (<h:outputText value="#{tag.tagCollectionName}"/>)
    <f:verbatim></span></span></br>  </f:verbatim>
</t:dataList>
