  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<f:loadBundle basename="org.sakaiproject.api.podcasts.bundle.Messages" var="msgs"/>

  <f:view>
        <link href="./css/podcaster.css" type="text/css" rel="stylesheet" media="all" />

    <sakai:view>

    <h:form>
      <h3><h:outputText value="#{msgs.delete_title}" /></h3>
 	  
  	  <h:outputText value="#{msgs.del_confirm}" styleClass="alertMessage" />

      <h:outputText value="#{podHomeBean.selectedPodcast.displayDate}" styleClass="podDelDateFormat" />
 	  <br />

      <h:outputText value="#{podHomeBean.selectedPodcast.title}" styleClass="podDelTitleFormat" />
	  <br />

      <h:outputText value="#{podHomeBean.selectedPodcast.description}" styleClass="podDelDescPosition" />
	  <br />

	 <div class="podDelButtonsPosition">
      <sakai:button_bar >  <!-- Save Changes and Cancel buttons -->
        <sakai:button_bar_item action="#{podHomeBean.processDeletePodcast}" value="#{msgs.delete}" 
          accesskey="s" title="Delete the Podcast" styleClass="active" />
        <sakai:button_bar_item action="#{podHomeBean.processCancelDelete}" value="#{msgs.cancel}" 
          accesskey="c" title="Cancel Changes" />
      </sakai:button_bar>
     </div>
   </h:form>

 </sakai:view>
</f:view>