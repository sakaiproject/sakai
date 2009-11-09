<f:view>
  <sakai:view title="#{msgs.title_bar}">
    <h:form>
      <sakai:panel_titled>
        <h3>
          <h:outputText value="#{msgs.confirm} #{msgs.site} #{msgs.association} #{msgs.changes}"/>
        </h3>
        <div class="instruction">
          <h:outputText value="#{msgs.confirm_changes_instr}" escape="false"/>
        </div>
        <h:outputText styleClass="alertMessage"
                      value="#{(SiteAssociationBean.showUnassociateWarning) ? 
                                  msgs.message_confirm_unassociate : msgs.message_confirm_changes}"/>
        <t:dataTable cellpadding="0" 
                     cellspacing="0"
                     styleClass="listHier lines nolines"
                     value="#{SiteAssociationBean.confirmSites}"
                     sortColumn="#{SiteAssociationBean.confirmSort.sort}"
                     sortAscending="#{SiteAssociationBean.confirmSort.ascending}"
                     var="dSite">
          <h:column>
            <f:facet name="header">
              <h:outputText value="#{msgs.associated}? "/>
            </f:facet>
            <h:outputText value="#{dSite.associated}"/>
          </h:column>
          <h:column>
            <f:facet name="header">
              <t:commandSortHeader columnName="id" title="#{msgs.sortby_site_id}"
                immediate="false">
                <f:facet name="ascending">
                  <h:graphicImage rendered="true" value="images/sortascending.gif"/>
                </f:facet>
                <f:facet name="descending">
                  <h:graphicImage rendered="true" value="images/sortdescending.gif"/>
                </f:facet>
                <h:outputText value="#{msgs.site} #{msgs.id} "/>
              </t:commandSortHeader>
            </f:facet>
            <h:outputText value="#{dSite.site.id}"/>
          </h:column>
          <h:column>
            <f:facet name="header">
              <t:commandSortHeader columnName="title" title="#{msgs.sortby_title}"
                immediate="false">
                <f:facet name="ascending">
                  <h:graphicImage rendered="true" value="images/sortascending.gif"/>
                </f:facet>
                <f:facet name="descending">
                  <h:graphicImage rendered="true" value="images/sortdescending.gif"/>
                </f:facet>
                <h:outputText value="#{msgs.title} "/>
              </t:commandSortHeader>
            </f:facet>
            <h:outputText value="#{dSite.site.title}"/>
          </h:column>
          <h:column>
            <f:facet name="header">
              <h:outputText value="#{msgs.description} "/>
            </f:facet>
            <h:outputText value="#{dSite.site.description}" escape="false"/>
          </h:column>
        </t:dataTable>
        <sakai:button_bar>
          <sakai:button_bar_item action="#{SiteAssociationBean.updateSites}" value="#{msgs.yes}"/>
          <sakai:button_bar_item immediate="true" action="cancel" value="#{msgs.no}"/>
        </sakai:button_bar>
      </sakai:panel_titled>
    </h:form>
  </sakai:view>
</f:view>
