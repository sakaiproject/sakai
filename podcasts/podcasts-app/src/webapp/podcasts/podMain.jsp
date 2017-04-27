<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view> 
	<sakai:view title="#{msgs.podcast_home_title}" toolCssHref="css/podcaster.css"> 
	<script type="text/javascript" src="scripts/popupscripts.js"></script>

	<h:form>

    <%-- if Resources tool not exist, if instructor, display error message
      		if student, display no podcasts exists --%>
    <h:panelGroup rendered="#{! podHomeBean.resourceToolExists || ! podHomeBean.canAccessFolder}" >
    	<%@ include file="podNoResource.jsp" %>
    </h:panelGroup>

	<h:panelGroup rendered="#{podHomeBean.resourceToolExists && podHomeBean.canAccessFolder}" >
		<sakai:tool_bar rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasNewPerm}">
        	  <sakai:tool_bar_item action="podcastAdd" value="#{msgs.add}" rendered="#{podHomeBean.hasNewPerm || podHomeBean.canUpdateSite}" />
	          <sakai:tool_bar_item action="podcastOptions" value="#{msgs.options}" rendered="#{podHomeBean.canUpdateSite}" />
			  <sakai:tool_bar_item action="#{podHomeBean.processPermissions}" value="#{msgs.permissions}" rendered="#{podHomeBean.canUpdateSite}" /> 
		</sakai:tool_bar>

		<h:outputText value="#{msgs.no_access}" styleClass="validation" rendered="#{! podHomeBean.hasAllGroups && ! podHomeBean.hasReadPerm }" />
      
		<h:panelGroup rendered="#{podHomeBean.hasReadPerm || podHomeBean.hasAllGroups}"> 
			<h:panelGrid>
 	  	  		<h:messages styleClass="alertMessage" id="errorMessages" rendered="#{!empty facesContext.maximumSeverity}"/>
				<h:panelGroup> 
	 	      		<f:verbatim><h3></f:verbatim>
 		        	<h:outputText value="#{msgs.podcast_home_title}" />
 	    			<f:verbatim></h3></f:verbatim>
 	      		</h:panelGroup>
 	  		</h:panelGrid>
 	    </h:panelGroup>

			<h:panelGroup styleClass="instruction indnt1" rendered="#{podHomeBean.hasReadPerm || podHomeBean.hasAllGroups}">
		  		<h:outputText value="#{msgs.podcast_home_sub} " />
	 	   		<h:outputLink styleClass="active" onclick="showPopupHere(this,'podcatcher'); return false;"
            		onmouseover="this.style.cursor='pointer'; return false;" onmouseout="hidePopup('podcatcher');" >
						<h:outputText value="#{msgs.podcatcher}#{msgs.colon}" />
	      		</h:outputLink>
	  		</h:panelGroup>

		<h:panelGroup rendered="#{podHomeBean.hasReadPerm || podHomeBean.hasAllGroups}" styleClass="indnt1"> 
			<h:outputText value="#{podHomeBean.URL}" />
			<h:outputLink value="#{podHomeBean.URL}" styleClass="active indnt2 rssIcon fa fa-rss" target="_blank" />
	  		<f:verbatim><br /></f:verbatim> 

			<h:commandLink action="podfeedRevise" styleClass="indnt2" rendered="#{podHomeBean.canUpdateSite}" >
 	      		<h:outputText value="#{msgs.revise}" />
	 		</h:commandLink>
 	  		<f:verbatim><br /></f:verbatim>

          	<h:outputText  styleClass="instruction" value="#{msgs.no_podcasts}" rendered="#{!podHomeBean.actPodcastsExist}" />
			<f:verbatim><br /></f:verbatim>
 		  </h:panelGroup>
      </h:panelGroup>
 	 
	<!-- if there are podcasts, display their information here -->
      	  <h:dataTable value="#{podHomeBean.contents}" var="eachPodcast" rendered="#{podHomeBean.actPodcastsExist}" styleClass="indnt1" >
          <h:column>
          	<h:panelGrid rendered="#{! eachPodcast.hidden || podHomeBean.hasHidden}" styleClass="#{eachPodcast.styleClass}" >
	            <h:outputText value="#{eachPodcast.displayDate}" styleClass="podDateFormat" />

	            <h:outputText value="#{eachPodcast.title}" styleClass="podTitleFormat" />

	            <h:outputText value="#{eachPodcast.description}" />
            
    	        <h:panelGroup>
	 	           <%--  Download link --%>
	 				<h:outputLink value="#{eachPodcast.fileURL}" styleClass="active" target="#{eachPodcast.newWindow}">
			 			 <h:outputText value="#{msgs.download}" />
					</h:outputLink>
 
	              <h:outputText value=" #{msgs.open_paren}#{eachPodcast.size} #{eachPodcast.type}#{msgs.close_paren}" />

	              <%--  go to Revise page --%>
        	      <h:outputText value=" #{msgs.spacer_bar}" rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasReviseAnyPerm || (podHomeBean.hasReviseOwnPerm && eachPodcast.author == podHomeBean.userName)}" />
            	  <h:commandLink action="podcastRevise" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.revise}" styleClass="active" 
                	    rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasReviseAnyPerm || (podHomeBean.hasReviseOwnPerm && eachPodcast.author == podHomeBean.userName)}" >
	                <f:param name="resourceId" value="#{eachPodcast.resourceId}" />
    	          </h:commandLink>
                 
	              <%--  go to Delete page --%> 
    	          <h:outputText value=" #{msgs.spacer_bar}" rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasDelAnyPerm || (podHomeBean.hasDelOwnPerm && eachPodcast.author == podHomeBean.userName)}" />
        	      <h:commandLink action="podcastDelete" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.delete}" styleClass="active" 
            	        rendered="#{podHomeBean.canUpdateSite || podHomeBean.hasDelAnyPerm || (podHomeBean.hasDelOwnPerm && eachPodcast.author == podHomeBean.userName)}" >
                	<f:param name="resourceId" value="#{eachPodcast.resourceId}" />
	              </h:commandLink>
    	        </h:panelGroup>

	            <h:panelGroup>
    	          <h:outputText value="#{msgs.posted_by}" />
        	      <h:outputText value="#{eachPodcast.author} " />
            	  <h:outputText value="#{msgs.at}" />
	              <h:outputText value="#{eachPodcast.postedDatetime} " />
            	</h:panelGroup>
              </h:panelGrid>
            </h:column>
      	  </h:dataTable>
      </h:form> 
 
    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px;" >
  	  <h:outputText value="#{msgs.popup_text}" />
    </div>
   </sakai:view> 
  </f:view>
 
