  <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
  <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
  <%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  <% response.setContentType("text/html; charset=UTF-8"); %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

  <f:view>
    <sakai:view toolCssHref="./css/podcaster.css">
      <script>includeLatestJQuery("podDelete");</script>
      <script>
        $(document).ready(function() {

          var menuLink = $('#podcastMainMenuLink');
          var menuLinkSpan = menuLink.closest('span');
          menuLinkSpan.addClass('current');
          menuLinkSpan.html(menuLink.text());

        });
      </script>
    <h:form>
        <%@ include file="/podcasts/podcastMenu.jsp" %>
        <h:outputText value="#{msgs.del_confirm}" styleClass="sak-banner-warn" />
        <h:panelGroup style="display:block;">
             <h:outputText value="#{podHomeBean.selectedPodcast.title}" />
        </h:panelGroup>
      <sakai:button_bar >  <!-- Save Changes and Cancel buttons -->
        <sakai:button_bar_item action="#{podHomeBean.processDeletePodcast}" value="#{msgs.delete}" 
          accesskey="s" title="Delete the Podcast" styleClass="active" />
        <sakai:button_bar_item action="#{podHomeBean.processCancelDelete}" value="#{msgs.cancel}" 
          accesskey="x" title="Cancel Changes" />
      </sakai:button_bar>
   </h:form>

 </sakai:view>
</f:view>
