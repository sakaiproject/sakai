  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcasts.bundle.Messages" var="msgs"/>

  <f:view>
    <sakai:view>
        <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

        <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>
    <h:form>
      <sakai:tool_bar>
          <sakai:tool_bar_item action="podcastAdd" value="#{msgs.add}" rendered="#{podHomeBean.resourceToolExists && podHomeBean.canUpdateSite}" />
          <sakai:tool_bar_item action="podcastOptions" value="#{msgs.options}" rendered="#{podHomeBean.resourceToolExists && podHomeBean.canUpdateSite}" />
          <sakai:tool_bar_item action="podcastPermissions" value="#{msgs.permissions}" rendered="#{podHomeBean.resourceToolExists && podHomeBean.canUpdateSite}" />
      </sakai:tool_bar>

 	  <div>
		  <h:messages styleClass="alertMessage" id="errorMessages"/> 
 	      <h3><h:outputText value="#{msgs.podcast_home_title}" /></h3>
 	     <div styleClass="instruction" class="indnt1">
            <h:outputText value="#{msgs.podcast_home_sub}" />
 
            <span onmouseover="showPopupHere(this,'podcatcher'); return false;" 
 	              onmouseout="hidePopup('podcatcher');" style="color: #0099cc;" class="active">
 	           <h:outputText value="#{msgs.podcatcher}" />
 	        </span>

 	     </div>
 	     <br />
 	     <b class="indnt1"><h:outputText value="#{podHomeBean.URL}" /></b>
 	  </div>

      <div class="indnt1" style="position:relative; top:20px;">
         <h:outputText  styleClass="instruction" value="#{msgs.no_podcasts}" 
                rendered="#{podHomeBean.podcastFolderExists && !podHomeBean.actPodcastsExist}" />
      </div>
 
	  <!-- TODO: if there are podcasts, display their information here 
	     or possibly return this part from previous tag -->
      <div class="indnt1" style="position:relative; top:20px;">
      <h:dataTable value="#{podHomeBean.contents}" var="eachPodcast"  rendered="#{podHomeBean.actPodcastsExist}" >
        <h:column>
            <h:outputText value="#{eachPodcast.displayDate}" styleClass="podDateFormat" />
			<f:verbatim><br /></f:verbatim>

            <h:outputText value="#{eachPodcast.title}" styleClass="podTitleFormat" />
			<f:verbatim><br /></f:verbatim>

            <h:outputText value="#{eachPodcast.description}" styleClass="podDescFormat" />
			<f:verbatim><br /></f:verbatim>
            
            <!--  Download link -->
            <f:verbatim><div class="podLinksPosition" ></f:verbatim>

            <!--  7/13/06 Hack to fix if spaces in name. TODO: redo correctly -->
            <!--  Below is correct JSP. Problem is when rendering what's sent from bean -->
<!--             h:outputLink value="#{eachPodcast.fileURL}" styleClass="active" 
                 h:outputText value="#{msgs.download}" 
                 h:outputLink -->
 
              <f:verbatim><a  class="active" href="</f:verbatim>
                  <h:outputText value="#{eachPodcast.fileURL}"  />
              <f:verbatim> "> </f:verbatim>
				<h:outputText value="#{msgs.download}" />
			  <f:verbatim></a></f:verbatim>

              <h:outputText value=" (" />
              <h:outputText value="#{eachPodcast.size}" />
            
              <h:outputText value=" " /> <!--  type -->
              <h:outputText value="#{eachPodcast.type}" />

              <!--  go to Revise page -->
              <h:outputText value=") | " />
              <h:commandLink action="podcastRevise" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.revise}" styleClass="active" rendered="#{podHomeBean.canUpdateSite}" >
                <f:param name="resourceId" value="#{eachPodcast.resourceId}" />
              </h:commandLink>
                 
              <!--  go to Delete page --> 
              <h:outputText value=" | " />
              <h:commandLink action="podcastDelete" actionListener="#{podHomeBean.podMainListener}" value="#{msgs.delete}" styleClass="active" rendered="#{podHomeBean.canUpdateSite}" >
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
        style="position:absolute; top: -1000px; left: -1000px; visibility:hidden;">
  	  <h:outputText value="#{msgs.popup_text}" />
    </div>
</f:view>