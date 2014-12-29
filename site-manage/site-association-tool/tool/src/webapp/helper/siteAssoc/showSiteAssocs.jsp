<f:view>
  <sakai:view title="#{msgs.title_bar}">
  	<t:stylesheet path="/css/siteassociation.css"/>
    <h:form id="siteAssociationForm">
      <sakai:panel_titled>
        <h3>
          <h:outputText value="#{msgs.Manage} #{msgs.site} #{msgs.associations}"/>
        </h3>
        <div class="instruction">
          <h:outputText value="#{msgs.modify_associations_instr}"/>
        </div>
        <h4><h:outputText value="#{SiteAssociationBean.site.title} #{msgs.associations}"/></h4>
        
        
        <f:subview id="spacer" rendered="#{SiteAssociationBean.assocSiteListSize < 6}">
			<f:verbatim>
				<br><br>
			</f:verbatim>
		</f:subview>
     
        
        <f:subview id="noAssocList" rendered="#{SiteAssociationBean.assocSiteListSize < 1}">
        	<t:graphicImage value="/images/empty.png" border="0"/>
          	<h:outputText value="#{msgs.message_no_assoc_sites}"/>
        </f:subview>
        
        <f:subview id="assocList" rendered="#{SiteAssociationBean.assocSiteListSize > 0}">
	        <t:div rendered="#{SiteAssociationBean.assocSiteListSize > 5}" style="float:right;">
	          <sakai:pager totalItems="#{SiteAssociationBean.assocSitesPager.totalItems}"
	                       firstItem="#{SiteAssociationBean.assocSitesPager.firstItem}"
	                       pageSize="#{SiteAssociationBean.assocSitesPager.pageSize}"
	                       immediate="false">
	          </sakai:pager>
	        </t:div>
	        
	        <div>
				<h:commandLink action="#{SiteAssociationBean.removeAllSites}" title="removeAllLink">
	              <h:outputText value="#{msgs.remove} #{msgs.all}"/>
	            </h:commandLink>
			</div>
	        
	        
	        <t:dataTable cellpadding="0" 
	                     cellspacing="0"
	                     styleClass="listHier lines nolines"
	                     value="#{SiteAssociationBean.assocSites}"
	                     first="#{SiteAssociationBean.assocSitesPager.firstItem}"
	                     rows="#{SiteAssociationBean.assocSitesPager.pageSize}"
	                     sortColumn="#{SiteAssociationBean.assocSitesSort.sort}"
	                     sortAscending="#{SiteAssociationBean.assocSitesSort.ascending}"
	                     var="dSite">
	           <h:column>
	            <f:facet name="header">
	              <h:outputText value="#{msgs.remove}"/>
	            </f:facet>
	           	<h:commandLink action="#{SiteAssociationBean.removeSiteFromAssocList}" title="removeSite">
	              <h:outputText value="#{msgs.remove}"/>
	              <f:param name="session.removeSiteId" value="#{dSite.id}"/>
	            </h:commandLink>         
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
	            <h:outputText value="#{dSite.title}"/>
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
	            <h:outputText value="#{dSite.id}"/>
	          </h:column>
	          
	          <h:column>
	            <f:facet name="header">
	              <h:outputText value="#{msgs.description} "/>
	            </f:facet>
	            <h:outputText value="#{dSite.description}" escape="false"/>
	          </h:column>
	          
	           
	          
	        </t:dataTable>
        </f:subview>

		<f:verbatim>
			<br><br>
		</f:verbatim>        
        
        <fieldset class="fieldsetVis">
	  		<legend><h:outputText value="#{msgs.search_sites}"/></legend>
        
        
        <h:panelGroup>          			
			<h:outputText id="textId" value="#{msgs.searchTitle}: "/>
			<h:inputText value="#{SiteAssociationBean.searchSiteParam}" id="searchSiteParamId"/>
			<h:commandButton id="addNewDevelopmentalLevel" value="#{msgs.search}" action="#{SiteAssociationBean.searchForSites}"/>
		
		</h:panelGroup>
		
		<f:verbatim>
			<br><br>
		</f:verbatim>
		
		<h4><h:outputText value="#{msgs.searchHeader}"/></h4>
		   
        
        
        
        <f:subview id="searchList" rendered="#{SiteAssociationBean.searchSiteListSize > 0}">
	        <t:div rendered="#{SiteAssociationBean.searchSiteListSize > 5}" style="float:right;">
	          <sakai:pager totalItems="#{SiteAssociationBean.searchSitesPager.totalItems}"
	                       firstItem="#{SiteAssociationBean.searchSitesPager.firstItem}"
	                       pageSize="#{SiteAssociationBean.searchSitesPager.pageSize}"
	                       immediate="false">
	          </sakai:pager>
	        </t:div>
	        
	     </f:subview>
	     
	     
		<span class="instruction">
			<h:outputText value="#{msgs.search_results}: " rendered="#{SiteAssociationBean.prevSearchParam != ''}"/>
		</span>
		<h:outputText value="#{SiteAssociationBean.prevSearchParam}" rendered="#{SiteAssociationBean.prevSearchParam != ''}"/>
		<f:verbatim>
			<br><br>
		</f:verbatim>
	      
	      
	      
	      
	      <f:subview id="noSearchList" rendered="#{SiteAssociationBean.searchSiteListSize < 1}">
        	<t:graphicImage value="/images/empty.png" border="0"/>
          	<h:outputText value="#{msgs.message_no_search_sites}"/>
          </f:subview>
	      
	      
	      
	      <f:subview id="searchList2" rendered="#{SiteAssociationBean.searchSiteListSize > 0}">
	        
	        <div>
				<h:commandLink action="#{SiteAssociationBean.addAllSites}" title="addAllLink">
	              <h:outputText value="#{msgs.add} #{msgs.all}"/>
	            </h:commandLink>
			</div>
			
	        <t:dataTable cellpadding="0" 
	                     cellspacing="0"
	                     styleClass="listHier lines nolines"
	                     value="#{SiteAssociationBean.searchSites}"
	                     first="#{SiteAssociationBean.searchSitesPager.firstItem}"
	                     rows="#{SiteAssociationBean.searchSitesPager.pageSize}"
	                     sortColumn="#{SiteAssociationBean.searchSitesSort.sort}"
	                     sortAscending="#{SiteAssociationBean.searchSitesSort.ascending}"
	                     var="dSite">
	         
	         <h:column>
	            <f:facet name="header">
	              <h:outputText value="#{msgs.add}"/>
	            </f:facet>
		        <h:commandLink action="#{SiteAssociationBean.addSiteToAssocList}" title="addSite">
		            <h:outputText value="#{msgs.add}"/>
		            <f:param name="session.addSiteId" value="#{dSite.id}"/>
		        </h:commandLink>
	              
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
	            <h:outputText value="#{dSite.title}"/>
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
	            <h:outputText value="#{dSite.id}"/>
	          </h:column>
	          
	          <h:column>
	            <f:facet name="header">
	              <h:outputText value="#{msgs.description} "/>
	            </f:facet>
	            <h:outputText value="#{dSite.description}" escape="false"/>
	          </h:column>
	       </t:dataTable>
	    </f:subview>
		</fieldset>

        <sakai:button_bar>
          <sakai:button_bar_item action="#{SiteAssociationBean.saveChanges}" value="#{msgs.save}"/>
          <sakai:button_bar_item immediate="true" action="#{SiteAssociationBean.cancelChanges}" value="#{msgs.cancel}"/>
        </sakai:button_bar>
      </sakai:panel_titled>      
    </h:form>
  </sakai:view>
</f:view>
