  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

  <f:view>
    <sakai:view toolCssHref="./css/podcaster.css">

    <h:form>
      <h3><h:outputText value="#{msgs.delete_title}" /></h3>
 	  
 	  <br />
  	  <h:outputText value="#{msgs.del_confirm}" styleClass="alertMessage" />

	  <br /><br />
	  <h:panelGrid>
	      <h:outputText value="#{podHomeBean.selectedPodcast.displayDate}" styleClass="podDateFormat" />
 	      <h:outputText value="#{podHomeBean.selectedPodcast.title}" styleClass="podTitleFormat" />
	      <h:outputText value="#{podHomeBean.selectedPodcast.description}" />
	  </h:panelGrid>

      <sakai:button_bar >  <!-- Save Changes and Cancel buttons -->
        <sakai:button_bar_item action="#{podHomeBean.processDeletePodcast}" value="#{msgs.delete}" 
          accesskey="s" title="Delete the Podcast" styleClass="active" />
        <sakai:button_bar_item action="#{podHomeBean.processCancelDelete}" value="#{msgs.cancel}" 
          accesskey="c" title="Cancel Changes" />
      </sakai:button_bar>
   </h:form>

 </sakai:view>
</f:view>