<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.api.podcasts.bundle.Messages" var="msgs"/>

   <f:view> 
     <sakai:view> 

       <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

       <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>

  
      <h:form>
      <h:panelGroup rendered="#{podHomeBean.resourceToolExists && podHomeBean.canUpdateSite}" >
      <sakai:tool_bar>
          <sakai:tool_bar_item action="podcastAdd" value="#{msgs.add}"  />
          <sakai:tool_bar_item action="podcastOptions" value="#{msgs.options}" />
      </sakai:tool_bar>
      </h:panelGroup>
      
 	  <div>
 	  	  <h:messages styleClass="alertMessage" id="errorMessages"/> 
 	      <h3><h:outputText value="#{msgs.podcast_home_title}" /></h3>
 	     <div class="instruction indnt1">
            <h:outputText value="#{msgs.podcast_home_sub}" />
 
            <span onClick="showPopupHere(this,'podcatcher'); return false;"
            	     onMouseOver="this.style.cursor='pointer'; return false;"
 	              onMouseOut="hidePopup('podcatcher');">
 	        <h:outputLink styleClass="active" onclick="return false">
	             <h:outputText value="#{msgs.podcatcher}#{msgs.colon}" />
	        </h:outputLink>
				<%-- <h:outputText value="#{msgs.colon}" /> --%>
 	        </span>
 	            
	     </div>
 	     <br />

 	       <h:outputText value="#{podHomeBean.URL}" styleClass="indnt1" />
     
 <%--         <h:outputLink value="#{podHomeBean.URL}" styleClass="nolines" target="_blank"> --%>
 		 <a href="<h:outputText value="#{podHomeBean.URL}" />" class="active" target="<h:outputText value="_blank" />" >
 	       <h:graphicImage value="images/rss-feed-icon.png" styleClass="indnt1 rssIcon" width="25px" height="25px" />
		 </a>
<%--          </h:outputLink> --%>
         <br />
 	     
 	     <h:commandLink action="podfeedRevise" styleClass="indnt2" rendered="#{podHomeBean.canUpdateSite}" >
 	         <h:outputText value="#{msgs.revise}" />
 	     </h:commandLink>
 	  </div> 

    <div class="indnt1">
    	<br />
        <h:outputText  styleClass="instruction" value="#{msgs.no_podcasts}" 
               rendered="#{podHomeBean.podcastFolderExists && !podHomeBean.actPodcastsExist}" />
    </div>
 
	<!-- if there are podcasts, display their information here -->
    <div id="podcast_info" class="indnt1" >
      <h:dataTable value="#{podHomeBean.contents}" var="eachPodcast" rendered="#{podHomeBean.actPodcastsExist}" >
        <h:column>
            <h:outputText value="#{eachPodcast.displayDate}" styleClass="podDateFormat" />
			<f:verbatim><br /></f:verbatim>

            <h:outputText value="#{eachPodcast.title}" styleClass="podTitleFormat" />
			<f:verbatim><br /></f:verbatim>

            <h:outputText value="#{eachPodcast.description}" styleClass="podDescFormat" />
			<f:verbatim><br /></f:verbatim>
            
            <%--  Download link --%>
            <f:verbatim><div class="podLinksPosition" ></f:verbatim>

            <%--  7/17/06 Hack to fix if spaces in name. 
                  TODO: redo correctly
                  Below is correct JSP. Problem is when rendering what's sent from bean
            		 it escapes spaces which causes link to fail  
            h:outputLink value="#{eachPodcast.fileURL}" styleClass="active" 
                 h:outputText value="#{msgs.download}" 
            h:outputLink> --%>
 
 			 <f:verbatim><a href="</f:verbatim>
 			 <h:outputText value="#{eachPodcast.fileURL}" />
 			 <f:verbatim>" class="active" alt="Download the file" target="</f:verbatim>
 			 <h:outputText value="#{eachPodcast.newWindow}" />
 			 <f:verbatim>" > </f:verbatim>
 			 
 			 <h:outputText value="#{msgs.download} " />
 			 
 			 <f:verbatim></a></f:verbatim>
 
              <h:outputText value=" #{msgs.open_paren}" />
              <h:outputText value="#{eachPodcast.size}" />
            
              <h:outputText value=" " /> <%--  type --%>
              <h:outputText value="#{eachPodcast.type}" />

              <%--  go to Revise page --%>
              <h:outputText value="#{msgs.close_paren}" /><h:outputText value=" " />
              <h:outputText value=" #{msgs.spacer_bar}" rendered="#{podHomeBean.canUpdateSite}" />
              <h:commandLink action="podcastRevise" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.revise}" styleClass="active" 
                    rendered="#{podHomeBean.canUpdateSite}" >
                <f:param name="resourceId" value="#{eachPodcast.resourceId}" />
              </h:commandLink>
                 
              <%--  go to Delete page --%> 
              <h:outputText value="#{msgs.spacer_bar}" rendered="#{podHomeBean.canUpdateSite}" />
              <h:commandLink action="podcastDelete" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.delete}" styleClass="active" 
                    rendered="#{podHomeBean.canUpdateSite}" >
                <f:param name="resourceId" value="#{eachPodcast.resourceId}" />
              </h:commandLink>
              <f:verbatim></div><br /></f:verbatim>

            <f:verbatim><div class="posted podPosition"></f:verbatim>
              <h:outputText value="#{msgs.posted_by}" />
              <h:outputText value="#{eachPodcast.author} " />
              <h:outputText value="#{msgs.at}" />
              <h:outputText value="#{eachPodcast.postedTime} " />
              <h:outputText value="#{msgs.on}" />
              <h:outputText value="#{eachPodcast.postedDate}" />
            <f:verbatim></div><br /></f:verbatim>
            
        </h:column>
      </h:dataTable>
    </div>
 
    </h:form>
   </sakai:view>
  
    <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px;" >
  	  <h:outputText value="#{msgs.popup_text}" />
    </div>
  </f:view> 
 
