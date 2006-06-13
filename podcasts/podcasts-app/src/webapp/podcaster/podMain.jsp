  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.tool.podcaster.bundle.Messages" var="msgs"/>

  <f:view>
    <sakai:view>
        <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

        <script type="text/javascript" language="JavaScript" src="scripts/popupscripts.js"></script>
    <h:form>
      <div class="navIntraTool">
          <h:commandLink  action="addPodcast">
            <h:outputText value="#{msgs.podcast_add}" rendered="#{podHomeBean.resourceToolExists}" />
          </h:commandLink>
          <h:commandLink action="podcastOptions">
            <h:outputText value="#{msgs.podcast_options}" rendered="#{podHomeBean.resourceToolExists}" />
          </h:commandLink>
          <h:commandLink action="podcastPermissions">
            <h:outputText value="#{msgs.podcast_permissions}" rendered="#{podHomeBean.resourceToolExists}" />
          </h:commandLink>
          <!-- **** JUST FOR TESTING REVISE, TAKE OUT BEFORE COMMITTING *** -->
          <h:commandLink action="podcastRevise">
            <h:outputText value="Revise" />
          </h:commandLink>
      </div>

 	  <div>
		  <h:messages styleClass="alertMessage" id="errorMessages"/> 
 	      <h3><h:outputText value="#{msgs.podcast_home_title}" /></h3>
 	     <div styleClass="instruction" class="indnt1">
            <h:outputText value="#{msgs.podcast_home_sub}" />
 
            <span onmouseover="showPopupHere(this,'podcatcher'); return false;" 
 	              onmouseout="hidePopup('podcatcher');" style="color: #0099cc;" class="active">
 	           <h:outputText value="#{msgs.podcast_home_podcatcher}" />
 	        </span>

 	     </div>
 	     <br />
 	     <b class="indnt1">http://url to be determined</b>
 	  </div>

      <!-- if there are no podcasts, display this -->
       	  
      <div class="indnt1" style="position:relative; top:20px;">
         <h:outputText  styleClass="instruction" value="#{msgs.podcast_no_podcasts}" rendered="! #{podHomeBean.podcastFolderExists}" />
      </div>
 
	<!-- TODO: if there are podcasts, display their information here -->
      <div class="">
        <h:outputText value="" />
      </div>
    </h:form>
  </sakai:view>
  
  <!-- This is the div for the popup definition. It is not displayed until the element is moused over -->
    <div id="podcatcher" class="podcatcher_popup" 
        style="position:absolute; top: -1000px; left: -1000px; visibility:hidden;">
  	  A podcatcher is a computer program used to automatically download
  	  podcasts.  iTunes is a popular podcatcher, but is not the only
      software available for this purpose.
    </div>
</f:view>