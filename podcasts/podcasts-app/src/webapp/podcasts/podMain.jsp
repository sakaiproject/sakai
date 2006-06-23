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
          <sakai:tool_bar_item action="addPodcast" value="#{msgs.add}" rendered="#{podHomeBean.resourceToolExists}" />
          <sakai:tool_bar_item action="podcastOptions" value="#{msgs.options}" rendered="#{podHomeBean.resourceToolExists}" />
          <sakai:tool_bar_item action="podcastPermissions" value="#{msgs.permissions}" rendered="#{podHomeBean.resourceToolExists}" />
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
	     or possibly return this part from previous tag  -->
      <sakai:flat_list value="#{podHomeBean.contents}" var="eachPodcast" >
        <h:column>
            <h:outputText value="#{eachPodcast.displayDate}" rendered="#{podHomeBean.actPodcastsExist}"/>

            <h:outputText value="#{eachPodcast.title}" rendered="#{podHomeBean.actPodcastsExist}"/>

            <h:outputText value="#{eachPodcast.description}" rendered="#{podHomeBean.actPodcastsExist}"/>
            
            <!--  Download link -->
            <h:commandLink action="#{podHomeBean.downloadFileFilter}" value="#{msgs.download}" styleClass="active" 
                 rendered="#{podHomeBean.actPodcastsExist}" />

            <h:outputText value=" (" rendered="#{podHomeBean.actPodcastsExist}" />
            <h:outputText value="#{eachPodcast.size}" rendered="#{podHomeBean.actPodcastsExist}"/>
            <h:outputText value=" " rendered="#{podHomeBean.actPodcastsExist}"/>
            <h:outputText value="#{eachPodcast.type}" rendered="#{podHomeBean.actPodcastsExist}"/>

            <!--  go to Revise page -->
            <h:outputText value=") | " rendered="#{podHomeBean.actPodcastsExist}" />
            <h:commandLink action="revise" value="#{msgs.revise}" styleClass="active"
                 rendered="#{podHomeBean.actPodcastsExist}"/>
                 
            <!--  go to Delete page --> 
            <h:outputText value=" | " rendered="#{podHomeBean.actPodcastsExist}" />
            <h:commandLink action="delete" value="#{msgs.delete}" styleClass="active"
                 rendered="#{podHomeBean.actPodcastsExist}"/>
        </h:column>
      </sakai:flat_list>
    </h:form>
  </sakai:view>
  
  <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px; visibility:hidden;">
  	  <h:outputText value="#{msgs.popup_text}" />
    </div>
</f:view>